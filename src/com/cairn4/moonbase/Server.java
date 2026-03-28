package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.cairn4.moonbase.ui.GameScreen;
import com.badlogic.gdx.math.GridPoint2;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.io.Writer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.cairn4.moonbase.net.ProtocolV2;
//sdawd
public class Server {
    // active server reference for in-process access
    private static Server activeServer = null;
    private int port;
    private ServerSocket serverSocket;
    private Map<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    // Track used display names to ensure uniqueness
    private Map<String, Integer> usedNames = new ConcurrentHashMap<>();
    // Flip overrides stored by ownerId -> scale
    private Map<Integer, Float> flipOverrides = new ConcurrentHashMap<>();
    // Persisted multiplayer player states (keyed by nick or fallback id)
    private Map<String, String> savedPlayerStates = new ConcurrentHashMap<>();
    private final Object playerStateLock = new Object();
    private volatile boolean playerStatesLoaded = false;
    private volatile boolean playerStateDirty = false;
    private volatile long lastPlayerStatePersistMs = 0L;
    private static final long PLAYER_STATE_PERSIST_INTERVAL_MS = 5000L;
    private boolean running = false;
    private volatile boolean listening = false;
    private int nextClientId = 1;
    // Rate-limiting for client-origin POS broadcasts to avoid flooding
    private static final float CLIENT_POS_THRESHOLD = 1.0f; // world units
    private static final long CLIENT_POS_MAX_INTERVAL = 100L; // ms
    // When server is started from inside the game, a GameScreen can be supplied
    // so the server can apply received world updates (host authoritative installs)
    private GameScreen gameScreen = null;
    private volatile boolean hostPosThreadStarted = false;
    private volatile long lastTimeWeatherSyncMs = 0L;
    private static final long TIME_WEATHER_SYNC_INTERVAL_MS = 60000L;
    private int lastTwDay = -1;
    private String lastTwPeriod = "";
    private float lastTwPeriodTime = -9999.0f;
    private String lastTwDayMode = "";
    private String lastTwWeatherMode = "";
    private String lastTwWeatherId = "";
    private float lastTwWeatherTime = -9999.0f;
    private float lastTwWeatherDur = -9999.0f;
    private volatile long lastHostVehMetaSentMs = 0L;
    private final java.util.HashMap<Long, String> lastHostVehMeta = new java.util.HashMap<Long, String>();
    private volatile long lastCreatureStateSentMs = 0L;
    private final Object serverPropsLock = new Object();
    private Properties serverProperties = new Properties();
    private volatile boolean serverPropsLoaded = false;
    private volatile boolean techSyncEnabled = true;
    private String serverPropsPath = null;

    public Server(int port) {
        this.port = port;
    // register active server so GameScreen/ConsoleExecutor can find it when running in-process
    try { activeServer = this; } catch (Throwable ignored) {}
    // preload player states if possible
    try { ensurePlayerStatesLoaded(); } catch (Exception ignored) {}
    try { ensureServerPropertiesLoaded(); } catch (Exception ignored) {}
    }

    // allow in-game server startup code to attach the running GameScreen
    public void setGameScreen(GameScreen gs) {
        this.gameScreen = gs;
        try {
            // ensure host player ownerId is consistent (use 0 for host)
            if (this.gameScreen != null && this.gameScreen.world != null && this.gameScreen.world.player != null) {
                try { this.gameScreen.world.player.ownerId = 0; } catch (Exception ignored) {}
                try {
                    String hostNick = com.cairn4.moonbase.MoonBase.multiplayerNick != null ? com.cairn4.moonbase.MoonBase.multiplayerNick : "";
                    if (hostNick != null && hostNick.length() > 0) this.usedNames.put(hostNick, Integer.valueOf(1));
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        try { ensureServerPropertiesLoaded(); } catch (Exception ignored) {}
        startHostPosThreadIfNeeded();
    }

    public void start() {
        running = true;
        try { ensureServerPropertiesLoaded(); } catch (Exception ignored) {}
        startHostPosThreadIfNeeded();
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                // mark that the server socket is bound and listening
                listening = true;
                Gdx.app.log("Server", "Server started on port " + port);

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        int clientId = nextClientId++;
                        Gdx.app.log("Server", "Client connected: " + clientSocket.getInetAddress().getHostAddress() + " with ID " + clientId);
                        ClientHandler clientHandler = new ClientHandler(clientSocket, this, clientId);
                        new Thread(clientHandler).start(); 
                    } catch (IOException e) {
                        if (running) {
                            Gdx.app.error("Server", "Error accepting client connection", e);
                        }
                    }
                }
            } catch (IOException e) {
                Gdx.app.error("Server", "Could not start server on port " + port, e);
            } finally {
                stop();
            }
        }).start();
    }

    private void startHostPosThreadIfNeeded() {
        if (hostPosThreadStarted) return;
        hostPosThreadStarted = true;
        try {
            Thread hostPosThread = new Thread(() -> {
                while (true) {
                    try {
                        if (Server.this.running && Server.this.gameScreen != null && Server.this.gameScreen.world != null && Server.this.gameScreen.world.player != null) {
                            float x = 0f, y = 0f, vx = 0f, vy = 0f;
                            try { x = Server.this.gameScreen.world.player.getXPos(); } catch (Exception ignored2) {}
                            try { y = Server.this.gameScreen.world.player.getYPos(); } catch (Exception ignored2) {}
                            try { vx = Server.this.gameScreen.world.player.getVelocity().x; } catch (Exception ignored2) {}
                            try { vy = Server.this.gameScreen.world.player.getVelocity().y; } catch (Exception ignored2) {}
                            String payload = "POS:PLAYER:0:" + x + ":" + y + ":" + vx + ":" + vy;
                            try { Server.this.broadcast("0:" + payload, null); } catch (Exception ignored2) {}

                            // Periodic time/weather sync from host world to all clients
                            try {
                                long now = System.currentTimeMillis();
                                try {
                                    com.cairn4.moonbase.World w = Server.this.gameScreen.world;
                                    com.cairn4.moonbase.DayCycle dc = w.dayCycle;
                                    com.cairn4.moonbase.WeatherManager wm = w.weatherManager;
                                    String period = (dc != null && dc.currentPeriod != null) ? dc.currentPeriod.toString() : "day";
                                    float periodTime = (dc != null) ? dc.timer : 0.0f;
                                    int day = (dc != null) ? dc.getDay() : 0;
                                    String dayMode = "defaultDay";
                                    try { if (dc != null) dayMode = dc.dayCycleMode.toString(); } catch (Exception ignored) {}
                                    String weatherMode = "normal";
                                    try { if (w.gameScreen != null && w.gameScreen.game != null && w.gameScreen.game.getCurrentMission() != null) weatherMode = w.gameScreen.game.getCurrentMission().weatherMode.toString(); } catch (Exception ignored) {}
                                    String weatherId = (wm != null && wm.getCurrentData() != null) ? wm.getCurrentData().id : "clear";
                                    float weatherTime = (wm != null) ? wm.getTimer() : 0.0f;
                                    float weatherDur = (wm != null) ? wm.getDuration() : 0.0f;
                                    boolean changed = false;
                                    if (day != Server.this.lastTwDay) changed = true;
                                    else if (!period.equals(Server.this.lastTwPeriod)) changed = true;
                                    else if (Math.abs(periodTime - Server.this.lastTwPeriodTime) > 5.0f) changed = true;
                                    else if (!dayMode.equals(Server.this.lastTwDayMode)) changed = true;
                                    else if (!weatherMode.equals(Server.this.lastTwWeatherMode)) changed = true;
                                    else if (!weatherId.equals(Server.this.lastTwWeatherId)) changed = true;
                                    else if (Math.abs(weatherTime - Server.this.lastTwWeatherTime) > 5.0f) changed = true;
                                    else if (Math.abs(weatherDur - Server.this.lastTwWeatherDur) > 5.0f) changed = true;
                                    if (changed || (now - Server.this.lastTimeWeatherSyncMs >= TIME_WEATHER_SYNC_INTERVAL_MS)) {
                                        Server.this.lastTwDay = day;
                                        Server.this.lastTwPeriod = period;
                                        Server.this.lastTwPeriodTime = periodTime;
                                        Server.this.lastTwDayMode = dayMode;
                                        Server.this.lastTwWeatherMode = weatherMode;
                                        Server.this.lastTwWeatherId = weatherId;
                                        Server.this.lastTwWeatherTime = weatherTime;
                                        Server.this.lastTwWeatherDur = weatherDur;
                                        Server.this.lastTimeWeatherSyncMs = now;
                                        String tw = "TIMEWEATHER:" + day + ":" + period + ":" + periodTime + ":" + dayMode + ":" + weatherMode + ":" + weatherId + ":" + weatherTime + ":" + weatherDur;
                                        Server.this.broadcast("0:" + tw, null);
                                    }
                                } catch (Exception ignored) {}

                                // Vehicle state sync from host-driven vehicles
                                try {
                                    for (com.cairn4.moonbase.entities.Entity e : Server.this.gameScreen.world.entityList) {
                                        if (!(e instanceof com.cairn4.moonbase.entities.Vehicle)) continue;
                                        com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                                        if (v.driverOwnerId != 0) continue;
                                        float vvx = 0f, vvy = 0f, vrot = 0f;
                                        try { vrot = v.getRotation(); } catch (Exception ignored) {}
                                        try {
                                            if (v.body != null) {
                                                vvx = v.body.getLinearVelocity().x * 256.0f;
                                                vvy = v.body.getLinearVelocity().y * 256.0f;
                                            }
                                        } catch (Exception ignored) {}
                                        String vs = "VEH_STATE:" + v.id + ":" + v.getXPos() + ":" + v.getYPos() + ":" + vrot + ":" + vvx + ":" + vvy;
                                        Server.this.broadcast("0:" + vs, null);
                                    }
                                } catch (Exception ignored) {}

                                // Vehicle meta sync from host-driven vehicles (drill, charge, wheels, etc.)
                                try {
                                    long nowMeta = System.currentTimeMillis();
                                    if (nowMeta - Server.this.lastHostVehMetaSentMs >= 200L) {
                                        for (com.cairn4.moonbase.entities.Entity e : Server.this.gameScreen.world.entityList) {
                                            if (!(e instanceof com.cairn4.moonbase.entities.Vehicle)) continue;
                                            com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                                            if (v.driverOwnerId != 0) continue;
                                            String meta = MultiplayerNetworkHelper.buildVehicleMeta(v);
                                            if (meta == null) continue;
                                            if (meta.startsWith("VEH_META:")) meta = meta.substring("VEH_META:".length());
                                            String prev = Server.this.lastHostVehMeta.get(Long.valueOf(v.id));
                                            if (prev == null || !prev.equals(meta) || (nowMeta - Server.this.lastHostVehMetaSentMs) >= 1000L) {
                                                Server.this.lastHostVehMeta.put(Long.valueOf(v.id), meta);
                                                Server.this.broadcast("0:VEH_META:" + meta, null);
                                            }
                                        }
                                        Server.this.lastHostVehMetaSentMs = nowMeta;
                                    }
                                } catch (Exception ignored) {}

                                // Creature state sync from host (position + state)
                                try {
                                    long nowCreature = System.currentTimeMillis();
                                    if (nowCreature - Server.this.lastCreatureStateSentMs >= 100L) {
                                        for (com.cairn4.moonbase.entities.Entity e : Server.this.gameScreen.world.entityList) {
                                            if (!(e instanceof com.cairn4.moonbase.entities.Creature)) continue;
                                            com.cairn4.moonbase.entities.Creature c = (com.cairn4.moonbase.entities.Creature)e;
                                            String type = "";
                                            try { if (c.creatureData != null && c.creatureData.id != null) type = c.creatureData.id; } catch (Exception ignored2) {}
                                            if (type == null || type.length() == 0) continue;
                                            String state = "";
                                            try { if (c.stateMachine != null && c.stateMachine.getCurrentState() != null) state = c.stateMachine.getCurrentState().name(); } catch (Exception ignored2) {}
                                            float rot = 0f;
                                            try { rot = c.group.getRotation(); } catch (Exception ignored2) {}
                                            String cs = "CREATURE_STATE:" + type + ":" + c.id + ":" + c.getXPos() + ":" + c.getYPos() + ":" + rot + ":" + state;
                                            Server.this.broadcast("0:" + cs, null);
                                        }
                                        Server.this.lastCreatureStateSentMs = nowCreature;
                                    }
                                } catch (Exception ignored) {}
                            } catch (Exception ignored2) {}
                        }
                        try { Server.this.maybePersistPlayerStates(); } catch (Exception ignored2) {}
                        try { Thread.sleep(100L); } catch (InterruptedException ignored2) {}
                    } catch (Exception ignored2) {}
                }
            }, "MewnBase-Server-HostPos");
            hostPosThread.setDaemon(true);
            hostPosThread.start();
        } catch (Exception ignored) {}
    }

    // Query whether the server has bound and is listening for connections
    public boolean isListening() {
        return listening;
    }

    public void stop() {
        running = false;
        try {
            for (ClientHandler client : clients.values()) {
                client.close();
            }
            clients.clear();
            try { persistPlayerStatesNow(); } catch (Exception ignored) {}
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                Gdx.app.log("Server", "Server stopped.");
                listening = false;
            }
        } catch (IOException e) {
            Gdx.app.error("Server", "Error stopping server", e);
        }
    }

    public boolean isTechSyncEnabled() {
        return this.techSyncEnabled;
    }

    private String getServerPropertiesPath() {
        try {
            String folder = com.cairn4.moonbase.MoonBase.currentSaveFolder;
            if (folder == null || folder.trim().length() == 0) {
                return "server.properties";
            }
            return "saves/" + folder + "/server.properties";
        } catch (Exception ignored) {}
        return "server.properties";
    }

    private void ensureServerPropertiesLoaded() {
        synchronized (serverPropsLock) {
            String path = getServerPropertiesPath();
            if (serverPropsLoaded && path != null && path.equals(serverPropsPath)) return;
            serverPropsLoaded = true;
            serverPropsPath = path;
            try {
                com.badlogic.gdx.files.FileHandle fh = com.badlogic.gdx.Gdx.files.local(path);
                if (!fh.exists()) {
                    // defaults
                    serverProperties.setProperty("syncTech", "true");
                    techSyncEnabled = true;
                    try {
                        try { fh.parent().mkdirs(); } catch (Exception ignored) {}
                        Writer writer = fh.writer(false, "UTF-8");
                        serverProperties.store(writer, "MewnBase server settings");
                        writer.close();
                    } catch (Exception ignored) {}
                } else {
                    try {
                        java.io.Reader reader = fh.reader("UTF-8");
                        serverProperties.load(reader);
                        reader.close();
                    } catch (Exception ignored) {}
                    String syncTech = serverProperties.getProperty("syncTech", "true");
                    techSyncEnabled = "true".equalsIgnoreCase(syncTech.trim());
                }
            } catch (Exception ignored) {}
        }
    }

    private String buildTechSyncPayload() {
        try {
            if (this.gameScreen == null || this.gameScreen.world == null || this.gameScreen.world.techManager == null) {
                return null;
            }
            com.cairn4.moonbase.techtree.TechManager tm = this.gameScreen.world.techManager;
            java.util.ArrayList<String> unlocked = tm.getSaveData();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < unlocked.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(unlocked.get(i));
            }
            String csv = sb.toString();
            return tm.samples + ":" + java.net.URLEncoder.encode(csv, "UTF-8");
        } catch (Exception ignored) {}
        return null;
    }

    private void broadcastTechSync() {
        if (!this.techSyncEnabled) return;
        String payload = buildTechSyncPayload();
        if (payload == null) return;
        try { this.broadcast("0:TECH_SYNC:" + payload, null); } catch (Exception ignored) {}
    }

    public synchronized void broadcast(String message, ClientHandler exclude) {
        int excludeId = -1;
        try { if (exclude != null) excludeId = exclude.clientId; } catch (Exception ignored) {}
        // Do not print/log messages that contain POS to avoid console spam; still perform broadcast
        boolean containsPos = (message != null && message.contains("POS"));
        if (!containsPos) {
            Gdx.app.log("Server", "Broadcasting message (exclude=" + excludeId + "): " + message);
            try {
                long now = System.currentTimeMillis();
                if ((now % 1000L) < 50L) {
                    System.out.println("Server: broadcast called on " + this + " excludeId=" + excludeId + " message=" + message);
                    System.out.println("Server: client count=" + (clients == null ? 0 : clients.size()));
                }
            } catch (Exception ignored) {}
        }
        String frame = message;
        ProtocolV2.Decoded d = decodeLegacyOrFrame(message, 0);
        if (d != null) {
            frame = encodeFrame(d);
        }
        if (frame == null) return;
        for (ClientHandler client : clients.values()) {
            if (client != exclude) {
                try {
            // Avoid printing per-client send when message contains POS
            if (!containsPos) {
                try {
                    long now = System.currentTimeMillis();
                    if ((now % 1000L) < 50L) {
                        System.out.println("Server: sending to clientId=" + client.clientId + " message=" + frame);
                    }
                } catch (Exception ignored) {}
            }
            client.sendMessage(frame);
                } catch (Exception e) {
                    Gdx.app.error("Server", "Error sending message to client " + client.clientId + ": " + message, e);
                    try { System.out.println("Server: exception sending to client " + client.clientId + " : " + e); } catch (Exception ignored) {}
                }
            }
        }
    }

    // Helper to retrieve the active in-process server, if any
    public static Server getActiveServer() {
        return activeServer;
    }

    // Unified API: relay player animation events (ANIM/ANIMPLAY/FLIP) from host or any client
    public void relayPlayerAnim(String payloadWithPlayer) {
        try {
            // For host-origin, prefix with 0:
            broadcast("0:" + payloadWithPlayer, null);
        } catch (Exception e) {
            Gdx.app.error("Server", "relayPlayerAnim failed", e);
        }
    }

    // Broadcast a payload as if sent from the server (prefix with 0: so clients parse it as "<id>:payload")
    public void broadcastFromServer(String payload) {
        try {
            try {
                if (payload != null) {
                    if (payload.startsWith("VEH_INV_SYNC:") || payload.startsWith("VEH_LOCK:") || payload.startsWith("VEH_UNLOCK:")) {
                        if (handleLocalVehiclePayload(payload)) {
                            return;
                        }
                    }
                    if (payload.startsWith("BASE_INV_SYNC:") || payload.startsWith("BASE_LOCK:") || payload.startsWith("BASE_UNLOCK:")) {
                        if (handleLocalBasePayload(payload)) {
                            return;
                        }
                    }
                }
            } catch (Exception ignored) {}
            // prefix with 0 to indicate server origin
            try { Gdx.app.log("Server", "Server-origin broadcast"); } catch (Exception ignored) {}
            // If payload is a chat message and we have a host GameScreen, also post it locally
            try {
                if (payload != null && payload.startsWith("CHAT:") && this.gameScreen != null && this.gameScreen.hud != null && this.gameScreen.hud.hudNotifications != null) {
                    try {
                        String rest = payload.substring("CHAT:".length());
                        int idx = rest.indexOf(":");
                        final String encNick = idx >= 0 ? rest.substring(0, idx) : "";
                        final String encText = idx >= 0 ? rest.substring(idx + 1) : rest;
                        final String nick = java.net.URLDecoder.decode(encNick == null ? "" : encNick, "UTF-8");
                        final String text = java.net.URLDecoder.decode(encText == null ? "" : encText, "UTF-8");
                        com.badlogic.gdx.Gdx.app.postRunnable(new Runnable() {
                            @Override public void run() {
                                try { Server.this.gameScreen.hud.hudNotifications.newChatMessage(nick, text); } catch (Exception ignored) {}
                            }
                        });
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
            this.broadcast("0:" + payload, null);
            // concise log
            try { Gdx.app.log("Server", "[Server] Server-origin broadcast"); } catch (Exception ignored) {}
        } catch (Exception e) {
            Gdx.app.error("Server", "Failed server-origin broadcast", e);
        }
    }

    private boolean handleLocalVehiclePayload(String payload) {
        try {
            if (this.gameScreen == null || this.gameScreen.world == null) return false;
            final int senderId = 0;
            if (payload.startsWith("VEH_INV_SYNC:")) {
                try {
                    int idx1 = payload.indexOf(':');
                    int idx2 = payload.indexOf(':', idx1 + 1);
                    if (idx2 > 0) {
                        int vehId = safeParseInt(payload.substring(idx1 + 1, idx2), -1);
                        com.cairn4.moonbase.entities.Vehicle v = findVehicleForOwnerOrNear(this.gameScreen, vehId, senderId, Float.NaN, Float.NaN);
                        if (v == null) return true;
                        if (v.inventoryLockOwnerId != senderId) return true;
                        String rest = payload.substring(idx2 + 1);
                        String relayPayload = "VEH_INV_SYNC:" + v.id + ":" + rest;
                        this.broadcast("0:" + relayPayload, null);
                        try { MultiplayerNetworkHelper.handleVehicleInvSync(this.gameScreen, relayPayload, senderId); } catch (Exception ignored) {}
                        return true;
                    }
                } catch (Exception ignored) {}
                return true;
            }
            if (payload.startsWith("VEH_LOCK:")) {
                try {
                    String[] parts = payload.split(":", 3);
                    if (parts.length >= 2) {
                        int vehId = safeParseInt(parts[1], -1);
                        com.cairn4.moonbase.entities.Vehicle v = findVehicleForOwnerOrNear(this.gameScreen, vehId, senderId, Float.NaN, Float.NaN);
                        if (v == null) return true;
                        if (v.inventoryLockOwnerId < 0 || v.inventoryLockOwnerId == senderId) {
                            v.inventoryLockOwnerId = senderId;
                            String ok = "VEH_LOCK:" + v.id + ":" + senderId;
                            this.broadcast("0:" + ok, null);
                            try { MultiplayerNetworkHelper.handleVehicleLock(this.gameScreen, ok, senderId); } catch (Exception ignored) {}
                        } else {
                            String deny = "VEH_LOCK_DENY:" + v.id + ":" + senderId;
                            try { MultiplayerNetworkHelper.handleVehicleLock(this.gameScreen, deny, senderId); } catch (Exception ignored) {}
                        }
                    }
                } catch (Exception ignored) {}
                return true;
            }
            if (payload.startsWith("VEH_UNLOCK:")) {
                try {
                    String[] parts = payload.split(":", 3);
                    if (parts.length >= 2) {
                        int vehId = safeParseInt(parts[1], -1);
                        com.cairn4.moonbase.entities.Vehicle v = findVehicleForOwnerOrNear(this.gameScreen, vehId, senderId, Float.NaN, Float.NaN);
                        if (v == null) return true;
                        if (v.inventoryLockOwnerId == senderId) {
                            v.inventoryLockOwnerId = -1;
                            String ok = "VEH_UNLOCK:" + v.id + ":" + senderId;
                            this.broadcast("0:" + ok, null);
                            try { MultiplayerNetworkHelper.handleVehicleLock(this.gameScreen, ok, senderId); } catch (Exception ignored) {}
                        }
                    }
                } catch (Exception ignored) {}
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean handleLocalBasePayload(String payload) {
        try {
            if (this.gameScreen == null || this.gameScreen.world == null) return false;
            final int senderId = 0;
            if (payload.startsWith("BASE_INV_SYNC:")) {
                try {
                    this.broadcast("0:" + payload, null);
                    try { MultiplayerNetworkHelper.handleBaseInvSync(this.gameScreen, payload, senderId); } catch (Exception ignored) {}
                } catch (Exception ignored) {}
                return true;
            }
            if (payload.startsWith("BASE_LOCK:")) {
                try {
                    String[] parts = payload.split(":", 4);
                    if (parts.length >= 3) {
                        int wx = safeParseInt(parts[1], Integer.MIN_VALUE);
                        int wy = safeParseInt(parts[2], Integer.MIN_VALUE);
                        if (wx != Integer.MIN_VALUE && wy != Integer.MIN_VALUE) {
                            com.cairn4.moonbase.tiles.Tile t = this.gameScreen.world.getTile(wx, wy);
                            if (t instanceof com.cairn4.moonbase.tiles.BaseModule) {
                                com.cairn4.moonbase.tiles.BaseModule bm = (com.cairn4.moonbase.tiles.BaseModule)t;
                                com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior isb =
                                        (com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior) bm.getBehavior(com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior.class);
                                if (isb != null) {
                                    if (isb.inventoryLockOwnerId < 0 || isb.inventoryLockOwnerId == senderId) {
                                        isb.inventoryLockOwnerId = senderId;
                                        String ok = "BASE_LOCK:" + wx + ":" + wy + ":" + senderId;
                                        this.broadcast("0:" + ok, null);
                                        try { MultiplayerNetworkHelper.handleBaseLock(this.gameScreen, ok, senderId); } catch (Exception ignored) {}
                                    } else {
                                        String deny = "BASE_LOCK_DENY:" + wx + ":" + wy + ":" + senderId;
                                        try { MultiplayerNetworkHelper.handleBaseLock(this.gameScreen, deny, senderId); } catch (Exception ignored) {}
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
                return true;
            }
            if (payload.startsWith("BASE_UNLOCK:")) {
                try {
                    String[] parts = payload.split(":", 4);
                    if (parts.length >= 3) {
                        int wx = safeParseInt(parts[1], Integer.MIN_VALUE);
                        int wy = safeParseInt(parts[2], Integer.MIN_VALUE);
                        if (wx != Integer.MIN_VALUE && wy != Integer.MIN_VALUE) {
                            com.cairn4.moonbase.tiles.Tile t = this.gameScreen.world.getTile(wx, wy);
                            if (t instanceof com.cairn4.moonbase.tiles.BaseModule) {
                                com.cairn4.moonbase.tiles.BaseModule bm = (com.cairn4.moonbase.tiles.BaseModule)t;
                                com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior isb =
                                        (com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior) bm.getBehavior(com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior.class);
                                if (isb != null && isb.inventoryLockOwnerId == senderId) {
                                    isb.inventoryLockOwnerId = -1;
                                    String ok = "BASE_UNLOCK:" + wx + ":" + wy + ":" + senderId;
                                    this.broadcast("0:" + ok, null);
                                    try { MultiplayerNetworkHelper.handleBaseLock(this.gameScreen, ok, senderId); } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    // small helper to safely parse integers with a fallback value
    private static int safeParseInt(String s, int fallback) { try { return Integer.parseInt(s); } catch (Exception e) { return fallback; } }
    private static float safeParseFloat(String s, float fallback) { try { return Float.parseFloat(s); } catch (Exception e) { return fallback; } }
    
    public synchronized void removeClient(int clientId) {
        ClientHandler removedClient = clients.remove(clientId);
        if (removedClient != null) {
            Gdx.app.log("Server", "Client " + clientId + " disconnected.");
            try { persistPlayerStatesNow(); } catch (Exception ignored) {}
            try {
                if (removedClient.appearanceData != null) {
                    String[] ps = removedClient.appearanceData.split("\\|");
                    if (ps.length > 2) {
                        String decNick = java.net.URLDecoder.decode(ps[2], "UTF-8");
                        if (decNick != null) {
                            Integer cnt = usedNames.get(decNick);
                            if (cnt != null) {
                                int n = cnt.intValue() - 1;
                                if (n <= 0) usedNames.remove(decNick);
                                else usedNames.put(decNick, Integer.valueOf(n));
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
            try {
                if (removedClient.announced) {
                    broadcast("DISCONNECTED:" + clientId, null);
                }
            } catch (Exception ignored) {}
            // Clear any vehicle seats held by this client
            try {
                if (this.gameScreen != null && this.gameScreen.world != null) {
                    final int rid = clientId;
                    com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){ @Override public void run() {
                        try {
                            for (com.cairn4.moonbase.entities.Entity e : Server.this.gameScreen.world.entityList) {
                                if (!(e instanceof com.cairn4.moonbase.entities.Vehicle)) continue;
                                com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                                boolean changed = false;
                                if (v.driverOwnerId == rid) { v.driverOwnerId = -1; changed = true; }
                                if (v.passengerOwnerId == rid) { v.passengerOwnerId = -1; changed = true; }
                                if (v.driverOwnerId < 0 && v.passengerOwnerId >= 0) { v.promotePassengerToDriver(); changed = true; }
                                if (changed) {
                                    if (!v.hasDriver() && !v.hasPassenger()) v.setState(com.cairn4.moonbase.entities.Vehicle.STATES.empty);
                                    String occ = "VEH_OCCUPY:" + v.id + ":" + v.driverOwnerId + ":" + v.passengerOwnerId;
                                    Server.this.broadcast("0:" + occ, null);
                                    try { MultiplayerNetworkHelper.handleVehicleOccupy(Server.this.gameScreen, occ, 0); } catch (Exception ignored) {}
                                }
                            }
                        } catch (Exception ignored) {}
                    }});
                }
            } catch (Exception ignored) {}
            // Remove remote player from host GameScreen if present
            try {
                if (this.gameScreen != null) {
                    final int rid = clientId;
                    com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){
                        @Override
                        public void run() {
                            try { Server.this.gameScreen.removePlayer(rid); } catch (Exception ignored) {}
                        }
                    });
                }
            } catch (Exception ignored) {}
        }
    }

    private String getPlayerStateFilePath() {
        try {
            String folder = com.cairn4.moonbase.MoonBase.currentSaveFolder;
            if (folder == null || folder.length() == 0) return null;
            return "saves/" + folder + "/multiplayer_players.json";
        } catch (Exception e) {
            return null;
        }
    }

    private void ensurePlayerStatesLoaded() {
        if (playerStatesLoaded) return;
        synchronized (playerStateLock) {
            if (playerStatesLoaded) return;
            String path = getPlayerStateFilePath();
            if (path == null) {
                playerStatesLoaded = true;
                return;
            }
            try {
                com.badlogic.gdx.files.FileHandle fh = com.badlogic.gdx.Gdx.files.local(path);
                if (fh.exists()) {
                    String text = fh.readString("UTF-8");
                    try {
                        com.badlogic.gdx.utils.JsonReader jr = new com.badlogic.gdx.utils.JsonReader();
                        com.badlogic.gdx.utils.JsonValue root = jr.parse(text);
                        com.badlogic.gdx.utils.JsonValue child = root.child;
                        while (child != null) {
                            if (child.name != null) {
                                savedPlayerStates.put(child.name, child.toString());
                            }
                            child = child.next;
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
            playerStatesLoaded = true;
        }
    }

    private void maybePersistPlayerStates() {
        if (!playerStateDirty) return;
        long now = System.currentTimeMillis();
        if (now - lastPlayerStatePersistMs < PLAYER_STATE_PERSIST_INTERVAL_MS) return;
        persistPlayerStatesNow();
    }

    private void persistPlayerStatesNow() {
        synchronized (playerStateLock) {
            if (!playerStateDirty && (System.currentTimeMillis() - lastPlayerStatePersistMs) < PLAYER_STATE_PERSIST_INTERVAL_MS) {
                return;
            }
            String path = getPlayerStateFilePath();
            if (path == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            for (Map.Entry<String, String> e : savedPlayerStates.entrySet()) {
                String key = e.getKey();
                String val = e.getValue();
                if (key == null || val == null) continue;
                if (!first) sb.append(",");
                first = false;
                sb.append("\n  \"").append(escapeJson(key)).append("\": ").append(val);
            }
            if (!savedPlayerStates.isEmpty()) sb.append("\n");
            sb.append("}");
            try {
                com.badlogic.gdx.files.FileHandle fh = com.badlogic.gdx.Gdx.files.local(path);
                fh.writeString(sb.toString(), false, "UTF-8");
            } catch (Exception ignored) {}
            playerStateDirty = false;
            lastPlayerStatePersistMs = System.currentTimeMillis();
        }
    }

    private String getSavedPlayerState(String key) {
        if (key == null) return null;
        ensurePlayerStatesLoaded();
        return savedPlayerStates.get(key);
    }

    private boolean hasSavedPlayerState(String key) {
        if (key == null) return false;
        ensurePlayerStatesLoaded();
        return savedPlayerStates.containsKey(key);
    }

    private void movePlayerState(String fromKey, String toKey) {
        if (fromKey == null || toKey == null) return;
        ensurePlayerStatesLoaded();
        if (!savedPlayerStates.containsKey(fromKey)) return;
        if (savedPlayerStates.containsKey(toKey)) return;
        savedPlayerStates.put(toKey, savedPlayerStates.remove(fromKey));
        playerStateDirty = true;
    }

    private void storePlayerState(String key, String json) {
        if (key == null || json == null || json.length() == 0) return;
        ensurePlayerStatesLoaded();
        savedPlayerStates.put(key, json);
        playerStateDirty = true;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"': sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c); break;
            }
        }
        return sb.toString();
    }

    private static ProtocolV2.Decoded decodeLegacyOrFrame(String message, int defaultFromId) {
        try {
            if (message == null) return null;
            if (message.startsWith(ProtocolV2.PREFIX + "|")) {
                return ProtocolV2.decode(message);
            }
            int fromId = defaultFromId;
            String legacy = message;
            int firstColon = legacy.indexOf(':');
            if (firstColon > 0) {
                try {
                    int id = Integer.parseInt(legacy.substring(0, firstColon));
                    String rest = legacy.substring(firstColon + 1);
                    if (rest.indexOf(':') > 0) {
                        fromId = id;
                        legacy = rest;
                    }
                } catch (Exception ignored) {}
            }
            int idx = legacy.indexOf(':');
            if (idx < 0) return null;
            String type = legacy.substring(0, idx);
            String payload = legacy.substring(idx + 1);
            return new ProtocolV2.Decoded(fromId, type, payload);
        } catch (Exception e) {
            return null;
        }
    }

    private static String encodeFrame(ProtocolV2.Decoded d) {
        if (d == null) return null;
        return ProtocolV2.encode(d.fromId, d.type, d.payload);
    }

    private static com.cairn4.moonbase.entities.Vehicle findVehicleById(com.cairn4.moonbase.World world, int vehId) {
        try {
            if (world == null || vehId < 0) return null;
            com.cairn4.moonbase.entities.Entity ent = world.getEntityById(vehId);
            if (ent instanceof com.cairn4.moonbase.entities.Vehicle) return (com.cairn4.moonbase.entities.Vehicle) ent;
        } catch (Exception ignored) {}
        return null;
    }

    private static com.cairn4.moonbase.entities.Vehicle findVehicleByOwner(com.cairn4.moonbase.World world, int ownerId) {
        try {
            if (world == null || ownerId < 0) return null;
            for (com.cairn4.moonbase.entities.Entity e : world.entityList) {
                if (!(e instanceof com.cairn4.moonbase.entities.Vehicle)) continue;
                com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                if (v.driverOwnerId == ownerId || v.passengerOwnerId == ownerId) return v;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static com.cairn4.moonbase.entities.Vehicle findVehicleNear(com.cairn4.moonbase.World world, float x, float y, float maxDist) {
        try {
            if (world == null || Float.isNaN(x) || Float.isNaN(y)) return null;
            float best = maxDist;
            com.cairn4.moonbase.entities.Vehicle bestV = null;
            for (com.cairn4.moonbase.entities.Entity e : world.entityList) {
                if (!(e instanceof com.cairn4.moonbase.entities.Vehicle)) continue;
                com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                float dx = v.getXPos() - x;
                float dy = v.getYPos() - y;
                float d = (float)Math.sqrt(dx * dx + dy * dy);
                if (d < best) {
                    best = d;
                    bestV = v;
                }
            }
            return bestV;
        } catch (Exception ignored) {}
        return null;
    }

    private static com.cairn4.moonbase.entities.Vehicle findVehicleForOwnerOrNear(GameScreen gs, int vehId, int ownerId, float x, float y) {
        try {
            if (gs == null || gs.world == null) return null;
            com.cairn4.moonbase.entities.Vehicle v = findVehicleById(gs.world, vehId);
            if (v != null) return v;
            v = findVehicleByOwner(gs.world, ownerId);
            if (v != null) return v;
            float px = x, py = y;
            try {
                com.cairn4.moonbase.Player rp = gs.getRemotePlayer(ownerId);
                if (rp != null) {
                    px = rp.getXPos();
                    py = rp.getYPos();
                } else {
                    try {
                        if (gs.world != null && gs.world.player != null && gs.world.player.ownerId == ownerId) {
                            px = gs.world.player.getXPos();
                            py = gs.world.player.getYPos();
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
            return findVehicleNear(gs.world, px, py, 600.0f);
        } catch (Exception ignored) {}
        return null;
    }

    private static String stripSenderPrefix(String message, String expectedPrefix) {
        try {
            if (message == null) return null;
            if (message.startsWith(expectedPrefix)) return message;
            int idx = message.indexOf(':');
            if (idx > 0) {
                String after = message.substring(idx + 1);
                if (after.startsWith(expectedPrefix)) return after;
            }
        } catch (Exception ignored) {}
        return message;
    }


    private static class ClientHandler implements Runnable {
        private Socket socket;
        private Server server;
        private int clientId;
        private DataOutputStream out;
        private DataInputStream in;
        private String appearanceData;
        private final java.util.concurrent.ConcurrentHashMap<Integer, PendingReliable> pendingReliable = new java.util.concurrent.ConcurrentHashMap<>();
        private final java.util.concurrent.atomic.AtomicInteger reliableSeq = new java.util.concurrent.atomic.AtomicInteger(1);
        private long lastReliableSweep = 0L;
        private static final int RELIABLE_RESEND_MS = 600;
        private static final int RELIABLE_MAX_ATTEMPTS = 6;
        private boolean announced = false;
        private volatile boolean readyForMessages = false;
        private volatile boolean sentSavedState = false;
        private final Object outLock = new Object(); // Add this line
        // rate-limiting state for POS broadcasts from this client
        private volatile float lastBroadcastPosX = Float.NaN;
        private volatile float lastBroadcastPosY = Float.NaN;
        private volatile long lastBroadcastPosMillis = 0L;

        private static class PendingReliable {
            final String frame;
            long lastSent;
            int attempts;
            PendingReliable(String frame, long lastSent) {
                this.frame = frame;
                this.lastSent = lastSent;
                this.attempts = 1;
            }
        }

        private static class ParsedReliable {
            final int seq;
            final String payload;
            ParsedReliable(int seq, String payload) { this.seq = seq; this.payload = payload; }
        }

        private boolean isReliableType(String type) {
            if (type == null) return false;
            return "APPEARANCE".equals(type)
                    || "SPAWNREMOTE".equals(type)
                    || "REQUEST_APPEARANCE".equals(type)
                    || "CONNECTED".equals(type)
                    || "TECH_SYNC".equals(type)
                    || "TECH_RESEARCH".equals(type)
                    || "TECH_SAMPLES_ADD".equals(type)
                    || "VEH_LOCK".equals(type)
                    || "VEH_UNLOCK".equals(type)
                    || "VEH_LOCK_DENY".equals(type)
                    || "VEH_INV_SYNC".equals(type)
                    || "BASE_LOCK".equals(type)
                    || "BASE_UNLOCK".equals(type)
                    || "BASE_LOCK_DENY".equals(type)
                    || "BASE_INV_SYNC".equals(type);
        }

        private ParsedReliable parseReliablePayload(String payload) {
            if (payload == null) return new ParsedReliable(-1, "");
            if (payload.startsWith("SEQ:")) {
                int idx = payload.indexOf(':', 4);
                if (idx > 4) {
                    int seq = safeParseInt(payload.substring(4, idx), -1);
                    String rest = payload.substring(idx + 1);
                    return new ParsedReliable(seq, rest);
                }
            }
            return new ParsedReliable(-1, payload);
        }

        private void sendAck(int seq) {
            try {
                if (seq < 0) return;
                String frame = ProtocolV2.encode(this.clientId, "ACK", Integer.toString(seq));
                if (frame != null) {
                    synchronized (this.outLock) {
                        if (this.out != null) {
                            this.out.writeUTF(frame);
                            this.out.flush();
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        private void startReliableResender() {
            Thread t = new Thread(() -> {
                while (server != null && server.running && readyForMessages) {
                    try {
                        long now = System.currentTimeMillis();
                        if (now - lastReliableSweep > 200L) {
                            lastReliableSweep = now;
                            for (java.util.Map.Entry<Integer, PendingReliable> ent : pendingReliable.entrySet()) {
                                PendingReliable pr = ent.getValue();
                                if (pr == null) continue;
                                if (now - pr.lastSent >= RELIABLE_RESEND_MS) {
                                    if (pr.attempts >= RELIABLE_MAX_ATTEMPTS) {
                                        pendingReliable.remove(ent.getKey());
                                    } else {
                                        pr.attempts++;
                                        pr.lastSent = now;
                                        synchronized (outLock) {
                                            if (out != null) {
                                                out.writeUTF(pr.frame);
                                                out.flush();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Thread.sleep(100L);
                    } catch (Exception ignored) {}
                }
            }, "MewnBase-Client-Resend-" + this.clientId);
            t.setDaemon(true);
            t.start();
        }

        public ClientHandler(Socket socket, Server server, int clientId) {
            this.socket = socket;
            this.server = server;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try {
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());
                
                sendInitialWorldData();
                // Signal end of binary init payload so client can safely start reading UTF messages
                try {
                    this.out.writeUTF("INIT_DONE");
                    this.out.flush();
                } catch (Exception ignored) {}

                // Wait for client READY to avoid sending live messages before it is listening
                try {
                    try { this.socket.setSoTimeout(10000); } catch (Exception ignored) {}
                    try {
                        String first = in.readUTF();
                        ProtocolV2.Decoded d = ProtocolV2.decode(first);
                        if (d == null || !"READY".equals(d.type)) {
                            Gdx.app.error("Server", "Expected READY from client " + this.clientId + " but got: " + first);
                        }
                    } catch (java.net.SocketTimeoutException ste) {
                        Gdx.app.error("Server", "Timed out waiting for READY from client " + this.clientId, ste);
                    } catch (Exception e) {
                        Gdx.app.error("Server", "Error waiting for READY from client " + this.clientId, e);
                    } finally {
                        try { this.socket.setSoTimeout(0); } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}

                // Now mark ready and add to broadcast list
                this.readyForMessages = true;
                try { server.clients.put(this.clientId, this); } catch (Exception ignored) {}
                try { startReliableResender(); } catch (Exception ignored) {}

                // Announce new player to everyone else
                server.broadcast("CONNECTED:" + clientId, this);
                // Also send CONNECTED to the new client so it can send APPEARANCE/SPAWNREMOTE
                try { sendMessage("0:CONNECTED:" + clientId); } catch (Exception ignored) {}

                // If running with an attached GameScreen, add the new client as a remote player on the render thread
                try {
                    if (server.gameScreen != null) {
                        final int newId = this.clientId;
                        final float sx = server.gameScreen.world != null && server.gameScreen.world.player != null ? server.gameScreen.world.player.getXPos() : 0f;
                        final float sy = server.gameScreen.world != null && server.gameScreen.world.player != null ? server.gameScreen.world.player.getYPos() : 0f;
                        com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){
                            @Override
                            public void run() {
                                try {
                                    // Only add if a remote player with this ID does not already exist
                                    try {
                                        if (server.gameScreen.getRemotePlayer(newId) == null) {
                                            server.gameScreen.addPlayer(newId, sx, sy);
                                        } else {
                                            Gdx.app.log("Server", "Remote player " + newId + " already exists on host, skipping addPlayer.");
                                        }
                                    } catch (Exception e) {
                                        // Fallback to attempt add if getRemotePlayer not available
                                        try { server.gameScreen.addPlayer(newId, sx, sy); } catch (Exception ignored) {}
                                    }
                                } catch (Exception e) {
                                    Gdx.app.error("Server", "Failed to add connected client to host GameScreen", e);
                                }
                            }
                        });
                    }
                } catch (Exception ignored) {}

                // Inform the new player about existing players
                for (ClientHandler otherClient : server.clients.values()) {
                    if (otherClient != this) {
                        sendMessage("CONNECTED:" + otherClient.clientId);
                        // Also send their appearance
                        if (otherClient.appearanceData != null) {
                            sendMessage("APPEARANCE:" + otherClient.clientId + ":" + otherClient.appearanceData);
                        }
                        // Ask them to resend appearance targeting the new client
                        try { otherClient.sendMessage("0:REQUEST_APPEARANCE:" + otherClient.clientId); } catch (Exception ignored) {}
                        // If we have a flip override for that client, inform the new client
                        try {
                            Float fo = server.flipOverrides.get(otherClient.clientId);
                            if (fo != null) {
                                sendMessage("FLIP_OVERRIDE:PLAYER:" + otherClient.clientId + ":" + fo);
                            }
                        } catch (Exception ignored) {}
                    }
                }
                // Send host info (spawn + appearance) to the new client so it sees the host even though host is not a network client
                    try {
                        if (server.gameScreen != null) {
                            try {
                                sendMessage("0:SPAWNREMOTE:0");
                            } catch (Exception ignored) {}
                        try {
                            int face = 0; String color = ""; String nick = "";
                            try { face = server.gameScreen.world.player.characterFaceOption; } catch (Exception ignored) {}
                            try { color = server.gameScreen.world.player.characterSuitColor != null ? server.gameScreen.world.player.characterSuitColor : ""; } catch (Exception ignored) {}
                            try { nick = com.cairn4.moonbase.MoonBase.multiplayerNick != null ? com.cairn4.moonbase.MoonBase.multiplayerNick : ""; } catch (Exception ignored) {}
                            String app = "0:APPEARANCE:0:" + face + "|" + URLEncoder.encode(color == null ? "" : color, "UTF-8") + "|" + URLEncoder.encode(nick == null ? "" : nick, "UTF-8");
                            sendMessage(app);
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}

                // Send current vehicle occupancy to the new client
                try {
                    if (server.gameScreen != null && server.gameScreen.world != null) {
                        for (com.cairn4.moonbase.entities.Entity e : server.gameScreen.world.entityList) {
                            if (!(e instanceof com.cairn4.moonbase.entities.Vehicle)) continue;
                            com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                            if (v.hasDriver() || v.hasPassenger()) {
                                String occ = "0:VEH_OCCUPY:" + v.id + ":" + v.driverOwnerId + ":" + v.passengerOwnerId;
                                sendMessage(occ);
                            }
                            if (v.inventoryLockOwnerId >= 0) {
                                String lock = "0:VEH_LOCK:" + v.id + ":" + v.inventoryLockOwnerId;
                                sendMessage(lock);
                            }
                        }
                    }
                } catch (Exception ignored) {}

                // Send current base storage locks to the new client
                try {
                    if (server.gameScreen != null && server.gameScreen.world != null) {
                        for (com.cairn4.moonbase.Chunk c : server.gameScreen.world.worldChunks.values()) {
                            for (com.cairn4.moonbase.tiles.Tile t : c.tiles.values()) {
                                if (!(t instanceof com.cairn4.moonbase.tiles.BaseModule)) continue;
                                com.cairn4.moonbase.tiles.BaseModule bm = (com.cairn4.moonbase.tiles.BaseModule)t;
                                com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior isb =
                                        (com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior) bm.getBehavior(com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior.class);
                                if (isb != null && isb.inventoryLockOwnerId >= 0) {
                                    String lock = "0:BASE_LOCK:" + bm.worldX + ":" + bm.worldY + ":" + isb.inventoryLockOwnerId;
                                    sendMessage(lock);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}

                // Request the new client to send its APPEARANCE back to the server
                try {
                    try { System.out.println("Server: requesting appearance from new client " + this.clientId); } catch (Exception ignored) {}
                    // Prefix with 0: so client treats it as a server-origin request, include target id
                    sendMessage("0:REQUEST_APPEARANCE:" + this.clientId);
                } catch (Exception ignored) {}

                // If we already have a saved state (fallback key), send it now
                try { sendSavedStateIfAvailable(); } catch (Exception ignored) {}
                // Send tech sync snapshot after initial state if enabled
                try {
                    if (server != null && server.isTechSyncEnabled()) {
                        String payload = server.buildTechSyncPayload();
                        if (payload != null) {
                            sendMessage("TECH_SYNC:" + payload);
                        }
                    }
                } catch (Exception ignored) {}

                while (server.running) {
                    String raw = in.readUTF();
                    Gdx.app.log("Server", "Received from client " + this.clientId + ": " + raw);
                    ProtocolV2.Decoded d = ProtocolV2.decode(raw);
                    if (d == null) {
                        continue;
                    }
                    String type = d.type != null ? d.type : "";
                    String msgPayload = d.payload == null ? "" : d.payload;
                    if ("ACK".equals(type)) {
                        try {
                            int seq = safeParseInt(msgPayload.trim(), -1);
                            if (seq >= 0) pendingReliable.remove(seq);
                        } catch (Exception ignored) {}
                        continue;
                    }
                    if (isReliableType(type)) {
                        ParsedReliable pr = parseReliablePayload(msgPayload);
                        if (pr.seq >= 0) {
                            sendAck(pr.seq);
                            msgPayload = pr.payload;
                        }
                    }
                    String message = type + ":" + msgPayload;
                    if ("TECH_RESEARCH".equals(type) && server.gameScreen != null && server.isTechSyncEnabled()) {
                        final String techId = msgPayload != null ? msgPayload.trim() : "";
                        if (techId.length() > 0) {
                            com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){
                                @Override public void run() {
                                    try {
                                        if (server.gameScreen != null && server.gameScreen.world != null && server.gameScreen.world.techManager != null) {
                                            com.cairn4.moonbase.techtree.TechManager tm = server.gameScreen.world.techManager;
                                            if (tm.canResearch(techId)) {
                                                tm.research(techId);
                                                try { server.broadcastTechSync(); } catch (Exception ignored) {}
                                            }
                                        }
                                    } catch (Exception ignored) {}
                                }
                            });
                        }
                        continue;
                    }
                    if ("TECH_SAMPLES_ADD".equals(type) && server.gameScreen != null && server.isTechSyncEnabled()) {
                        final int amt = safeParseInt(msgPayload, 0);
                        if (amt != 0) {
                            com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){
                                @Override public void run() {
                                    try {
                                        if (server.gameScreen != null && server.gameScreen.world != null && server.gameScreen.world.techManager != null) {
                                            server.gameScreen.world.techManager.addSamples(amt);
                                            try { server.broadcastTechSync(); } catch (Exception ignored) {}
                                        }
                                    } catch (Exception ignored) {}
                                }
                            });
                        }
                        continue;
                    }
                    // If client is broadcasting a player animation play request, rebroadcast and apply locally when possible
                        if (message.startsWith("ANIMPLAY:") && server.gameScreen != null) {
                        try {
                            // Broadcast including origin so sender sees it too
                            server.broadcast(this.clientId + ":" + message, null);
                            // Also apply locally on render thread
                            final String animMessage = message;
                            final int origin = this.clientId;
                            com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){
                                @Override public void run() {
                                    try {
                                        try {
                                            String rest = animMessage.substring("ANIMPLAY:".length());
                                            String[] parts = rest.split(":", 6);
                                                if (parts.length >= 3 && "PLAYER".equals(parts[0])) {
                                                final int owner = safeParseInt(parts[1], Integer.MIN_VALUE);
                                                final String anim = parts[2];
                                                final boolean loop = parts.length > 3 ? Boolean.parseBoolean(parts[3]) : true;
                                                final float scale = parts.length > 4 ? safeParseFloat(parts[4], 1.0f) : 1.0f;
                                                // Don't apply to host's local player if owner matches host's id
                                                if (server.gameScreen.world != null && server.gameScreen.world.player != null && server.gameScreen.world.player.ownerId == owner) return;
                                                com.cairn4.moonbase.Player remote = server.gameScreen.getRemotePlayer(owner);
                                                if (remote == null) return;
                                                // Apply scale/flip and animation according to server's animTestMode
                                                int mode = 0;
                                                try { mode = server.gameScreen.getAnimTestMode(); } catch (Exception ignored) {}
                                                if (mode == 1) {
                                                    // controller-first
                                                    try {
                                                        java.lang.reflect.Field pacField = com.cairn4.moonbase.Player.class.getDeclaredField("playerAnimController");
                                                        pacField.setAccessible(true);
                                                        Object pac = pacField.get(remote);
                                                        if (pac != null) {
                                                            try {
                                                                java.lang.reflect.Method test = pac.getClass().getMethod("testAnim", String.class, boolean.class);
                                                                test.invoke(pac, anim, loop);
                                                            } catch (NoSuchMethodException nsme) {}
                                                        }
                                                    } catch (Exception ignored) {}
                                                    return;
                                                }
                                                if (mode == 2) {
                                                    // skeleton-only apply
                                                    try {
                                                        java.lang.reflect.Field spineFieldForScale = com.cairn4.moonbase.Player.class.getDeclaredField("spineActor");
                                                        spineFieldForScale.setAccessible(true);
                                                        Object spineForScale = spineFieldForScale.get(remote);
                                                        if (spineForScale != null) {
                                                            try {
                                                                java.lang.reflect.Field skeletonField = spineForScale.getClass().getDeclaredField("skeleton");
                                                                skeletonField.setAccessible(true);
                                                                Object skel = skeletonField.get(spineForScale);
                                                                if (skel != null) {
                                                                    try {
                                                                        java.lang.reflect.Method setScaleX = skel.getClass().getMethod("setScaleX", float.class);
                                                                        setScaleX.invoke(skel, scale);
                                                                    } catch (NoSuchMethodException nsme) {}
                                                                }
                                                            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                                                        }
                                                    } catch (Exception ignored) {}
                                                    return;
                                                }
                                                if (mode == 3) {
                                                    // delayed
                                                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                                                        try {
                                                            java.lang.reflect.Field spineFieldForScale = com.cairn4.moonbase.Player.class.getDeclaredField("spineActor");
                                                            spineFieldForScale.setAccessible(true);
                                                            Object spineForScale = spineFieldForScale.get(remote);
                                                            if (spineForScale != null) {
                                                                try {
                                                                    java.lang.reflect.Field skeletonField = spineForScale.getClass().getDeclaredField("skeleton");
                                                                    skeletonField.setAccessible(true);
                                                                    Object skel = skeletonField.get(spineForScale);
                                                                    if (skel != null) {
                                                                        try {
                                                                            java.lang.reflect.Method setScaleX = skel.getClass().getMethod("setScaleX", float.class);
                                                                            setScaleX.invoke(skel, scale);
                                                                        } catch (NoSuchMethodException nsme) {}
                                                                    }
                                                                } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                                                            }
                                                        } catch (Exception ignored) {}
                                                    });
                                                    return;
                                                }
                                                // Default mode: attempt controller then state set
                                                try {
                                                    java.lang.reflect.Field pacField = com.cairn4.moonbase.Player.class.getDeclaredField("playerAnimController");
                                                    pacField.setAccessible(true);
                                                    Object pac = pacField.get(remote);
                                                    if (pac != null) {
                                                        try {
                                                            java.lang.reflect.Method test = pac.getClass().getMethod("testAnim", String.class, boolean.class);
                                                            test.invoke(pac, anim, loop);
                                                        } catch (NoSuchMethodException nsme) {}
                                                    }
                                                } catch (Exception ignored) {}
                                                try {
                                                    java.lang.reflect.Field spineField = com.cairn4.moonbase.Player.class.getDeclaredField("spineActor");
                                                    spineField.setAccessible(true);
                                                    Object spine = spineField.get(remote);
                                                    if (spine != null) {
                                                        try {
                                                            java.lang.reflect.Field stateField = spine.getClass().getDeclaredField("state");
                                                            stateField.setAccessible(true);
                                                            Object state = stateField.get(spine);
                                                            if (state != null) {
                                                                java.lang.reflect.Method setAnim = state.getClass().getMethod("setAnimation", int.class, String.class, boolean.class);
                                                                setAnim.invoke(state, 0, anim, loop);
                                                            }
                                                        } catch (Exception ignored) {}
                                                    }
                                                } catch (Exception ignored) {}
                                            }
                                        } catch (Exception ignored) {}
                                    } catch (Exception ignored) {}
                                }
                            });
                        } catch (Exception ignored) {}
                        continue;
                    }
                    // Handle FLIP messages: rebroadcast + apply locally when hosting
                    if (message.startsWith("FLIP:") && server.gameScreen != null) {
                        try {
                            server.broadcast(this.clientId + ":" + message, null);
                            try { if (!message.startsWith("0:")) server.relayPlayerAnim(message); } catch (Exception ignored) {}
                            final String flipMessage = message; final int origin = this.clientId;
                            com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){ @Override public void run() {
                                try {
                                    try {
                                        String rest = flipMessage.substring("FLIP:".length());
                                        String[] parts = rest.split(":", 4);
                                        if (parts.length >= 3 && "PLAYER".equals(parts[0])) {
                                            final int owner = safeParseInt(parts[1], Integer.MIN_VALUE);
                                            final float scale = parts.length > 2 ? safeParseFloat(parts[2], 1.0f) : 1.0f;
                                            if (server.gameScreen.world != null && server.gameScreen.world.player != null && server.gameScreen.world.player.ownerId == owner) return;
                                            com.cairn4.moonbase.Player remote = server.gameScreen.getRemotePlayer(owner);
                                            if (remote == null) return;
                                            boolean applied = false;
                                            try {
                                                java.lang.reflect.Field pacField = com.cairn4.moonbase.Player.class.getDeclaredField("playerAnimController");
                                                pacField.setAccessible(true);
                                                Object pac = pacField.get(remote);
                                                if (pac != null) {
                                                    try {
                                                        java.lang.reflect.Method setForced = pac.getClass().getMethod("setForcedScale", float.class);
                                                        setForced.invoke(pac, scale);
                                                        applied = true;
                                                    } catch (NoSuchMethodException nsme) {
                                                        // fallthrough
                                                    }
                                                }
                                            } catch (Exception ignored) {}
                                            if (!applied) {
                                                try {
                                                    java.lang.reflect.Field spineField = com.cairn4.moonbase.Player.class.getDeclaredField("spineActor");
                                                    spineField.setAccessible(true);
                                                    Object spine = spineField.get(remote);
                                                    if (spine != null) {
                                                        try {
                                                            java.lang.reflect.Field skF = spine.getClass().getDeclaredField("skeleton");
                                                            skF.setAccessible(true);
                                                            Object sk = skF.get(spine);
                                                            if (sk != null) {
                                                                try {
                                                                    java.lang.reflect.Method setScaleX = sk.getClass().getMethod("setScaleX", float.class);
                                                                    setScaleX.invoke(sk, scale);
                                                                } catch (NoSuchMethodException nsme) {}
                                                            }
                                                        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                                                    }
                                                } catch (Exception ignored) {}
                                            }
                                        }
                                    } catch (Exception ignored) {}
                                } catch (Exception ignored) {}
                            }});
                        } catch (Exception ignored) {}
                        continue;
                    }
                    // Handle FLIP_OVERRIDE: store override on server and broadcast to all clients
                    if (message.startsWith("FLIP_OVERRIDE:") && server.gameScreen != null) {
                        try {
                            // Broadcast with origin so sender sees it too
                            server.broadcast(this.clientId + ":" + message, null);
                            String rest = message.substring("FLIP_OVERRIDE:".length());
                            String[] parts = rest.split(":", 4);
                            if (parts.length >= 3 && "PLAYER".equals(parts[0])) {
                                final int owner = safeParseInt(parts[1], Integer.MIN_VALUE);
                                final float scale = parts.length > 2 ? safeParseFloat(parts[2], 1.0f) : 1.0f;
                                // store override
                                try { server.flipOverrides.put(owner, scale); } catch (Exception ignored) {}
                                // Also apply locally on host GameScreen
                                com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){ @Override public void run() {
                                    try {
                                        if (server.gameScreen.world != null && server.gameScreen.world.player != null && server.gameScreen.world.player.ownerId == owner) return;
                                        com.cairn4.moonbase.Player remote = server.gameScreen.getRemotePlayer(owner);
                                        if (remote == null) return;
                                        boolean applied = false;
                                        try {
                                            java.lang.reflect.Field pacField = com.cairn4.moonbase.Player.class.getDeclaredField("playerAnimController");
                                            pacField.setAccessible(true);
                                            Object pac = pacField.get(remote);
                                            if (pac != null) {
                                                try {
                                                    java.lang.reflect.Method setForced = pac.getClass().getMethod("setForcedScale", float.class);
                                                    setForced.invoke(pac, scale);
                                                    applied = true;
                                                } catch (NoSuchMethodException nsme) {}
                                            }
                                        } catch (Exception ignored) {}
                                        if (!applied) {
                                            try {
                                                java.lang.reflect.Field spineField = com.cairn4.moonbase.Player.class.getDeclaredField("spineActor");
                                                spineField.setAccessible(true);
                                                Object spine = spineField.get(remote);
                                                if (spine != null) {
                                                    try {
                                                        java.lang.reflect.Field skF = spine.getClass().getDeclaredField("skeleton");
                                                        skF.setAccessible(true);
                                                        Object sk = skF.get(spine);
                                                        if (sk != null) {
                                                            try {
                                                                java.lang.reflect.Method setScaleX = sk.getClass().getMethod("setScaleX", float.class);
                                                                setScaleX.invoke(sk, scale);
                                                            } catch (NoSuchMethodException nsme) {}
                                                        }
                                                    } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                                                }
                                            } catch (Exception ignored) {}
                                        }
                                    } catch (Exception ignored) {}
                                }});
                            }
                        } catch (Exception ignored) {}
                        continue;
                    }
                    // Handle FLIP_OVERRIDE_CLEAR: remove stored override and broadcast to clients
                    if (message.startsWith("FLIP_OVERRIDE_CLEAR:")) {
                        try {
                            server.broadcast(this.clientId + ":" + message, null);
                            String rest = message.substring("FLIP_OVERRIDE_CLEAR:".length());
                            String[] parts = rest.split(":", 3);
                            if (parts.length >= 2 && "PLAYER".equals(parts[0])) {
                                final int owner = safeParseInt(parts[1], Integer.MIN_VALUE);
                                try { server.flipOverrides.remove(owner); } catch (Exception ignored) {}
                                // Also apply local reset on host GameScreen
                                com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){ @Override public void run() {
                                    try {
                                        if (server.gameScreen == null) return;
                                        if (server.gameScreen.world != null && server.gameScreen.world.player != null && server.gameScreen.world.player.ownerId == owner) return;
                                        com.cairn4.moonbase.Player remote = server.gameScreen.getRemotePlayer(owner);
                                        if (remote == null) return;
                                        boolean applied = false;
                                        try {
                                            java.lang.reflect.Field pacField = com.cairn4.moonbase.Player.class.getDeclaredField("playerAnimController");
                                            pacField.setAccessible(true);
                                            Object pac = pacField.get(remote);
                                            if (pac != null) {
                                                try {
                                                    java.lang.reflect.Method setForced = pac.getClass().getMethod("setForcedScale", float.class);
                                                    setForced.invoke(pac, 1.0f);
                                                    applied = true;
                                                } catch (NoSuchMethodException nsme) {}
                                            }
                                        } catch (Exception ignored) {}
                                        if (!applied) {
                                            try {
                                                java.lang.reflect.Field spineField = com.cairn4.moonbase.Player.class.getDeclaredField("spineActor");
                                                spineField.setAccessible(true);
                                                Object spine = spineField.get(remote);
                                                if (spine != null) {
                                                    try {
                                                        java.lang.reflect.Field skF = spine.getClass().getDeclaredField("skeleton");
                                                        skF.setAccessible(true);
                                                        Object sk = skF.get(spine);
                                                        if (sk != null) {
                                                            try {
                                                                java.lang.reflect.Method setScaleX = sk.getClass().getMethod("setScaleX", float.class);
                                                                setScaleX.invoke(sk, 1.0f);
                                                            } catch (NoSuchMethodException nsme) {}
                                                        }
                                                    } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                                                }
                                            } catch (Exception ignored) {}
                                        }
                                    } catch (Exception ignored) {}
                                }});
                            }
                        } catch (Exception ignored) {}
                        continue;
                    }
                    // If this message is an ANIM request and we have a GameScreen, handle specially before other processing
                    if (message.startsWith("ANIM:") && server.gameScreen != null) {
                        try {
                            // Broadcast including origin so sender sees it too
                            server.broadcast(this.clientId + ":" + message, null);
                            // Also allow host-origin relay via unified API if needed
                            try { if (!message.startsWith("0:")) server.relayPlayerAnim(message); } catch (Exception ignored) {}
                            // Schedule local application on render thread
                            final String animMessage = message;
                            com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){
                                @Override public void run() {
                                    try {
                                        try {
                                            String rest = animMessage.substring("ANIM:".length());
                                            String[] parts = rest.split(":" , 4);
                                            final int fwx = parts.length > 1 ? safeParseInt(parts[1], Integer.MIN_VALUE) : Integer.MIN_VALUE;
                                            final int fwy = parts.length > 2 ? safeParseInt(parts[2], Integer.MIN_VALUE) : Integer.MIN_VALUE;
                                            // try tile spawn
                                            com.cairn4.moonbase.tiles.Tile t = server.gameScreen.world.getTile(fwx, fwy);
                                            if (t != null) {
                                                try {
                                                    java.lang.reflect.Method gm = t.getClass().getMethod("getSpawnAnimation");
                                                    Object act = gm.invoke(t);
                                                    if (act instanceof com.badlogic.gdx.scenes.scene2d.Action) {
                                                        try {
                                                            java.lang.reflect.Field gf = t.getClass().getDeclaredField("group");
                                                            gf.setAccessible(true);
                                                            Object go = gf.get(t);
                                                            if (go instanceof com.badlogic.gdx.scenes.scene2d.Group) {
                                                                ((com.badlogic.gdx.scenes.scene2d.Group)go).addAction((com.badlogic.gdx.scenes.scene2d.Action)act);
                                                            }
                                                        } catch (NoSuchFieldException | IllegalAccessException e) {
                                                            // ignore access issues
                                                        }
                                                    }
                                                } catch (NoSuchMethodException ignored) {}
                                            }
                                            // try entities
                                            for (com.cairn4.moonbase.entities.Entity e : server.gameScreen.world.entityList) {
                                                try {
                                                    int ex = (int)e.getXPos(); int ey = (int)e.getYPos();
                                                    if (Math.abs(ex - fwx) <= 1 && Math.abs(ey - fwy) <= 1) {
                                                        try {
                                                            java.lang.reflect.Method m = e.getClass().getMethod("spawnAnim");
                                                            m.invoke(e);
                                                        } catch (NoSuchMethodException ignored) {}
                                                    }
                                                } catch (Exception ignored) {}
                                            }
                                        } catch (Exception ignored) {}
                                    } catch (Exception ignored) {}
                                }
                            });
                        } catch (Exception ignored) {}
                        // we've already handled this message
                        continue;
                    }
                    if (message.startsWith("APPEARANCE:")) {
                        this.appearanceData = message.substring("APPEARANCE:".length());
                        // Ensure unique nick (third part) by appending suffix when duplicate
                        try {
                            String[] ps = this.appearanceData.split("\\|");
                            if (ps.length > 2) {
                                String decNick = java.net.URLDecoder.decode(ps[2], "UTF-8");
                                if (decNick == null) decNick = "";
                                Integer cnt = server.usedNames.get(decNick);
                                if (cnt == null) {
                                    server.usedNames.put(decNick, Integer.valueOf(1));
                                } else {
                                    cnt = Integer.valueOf(cnt.intValue() + 1);
                                    server.usedNames.put(decNick, cnt);
                                    String newNick = decNick + cnt;
                                    ps[2] = java.net.URLEncoder.encode(newNick, "UTF-8");
                                    this.appearanceData = ps[0] + "|" + ps[1] + "|" + ps[2];
                                }
                            }
                        } catch (Exception ignored) {}
                        server.broadcast("APPEARANCE:" + this.clientId + ":" + this.appearanceData, this);
                        // Migrate any fallback-saved state to nick key, then send saved state to client
                        try {
                            String nickKey = getPlayerStateKey();
                            String fallbackKey = "id:" + this.clientId;
                            if (!fallbackKey.equals(nickKey)) {
                                if (server.hasSavedPlayerState(fallbackKey) && !server.hasSavedPlayerState(nickKey)) {
                                    server.movePlayerState(fallbackKey, nickKey);
                                }
                            }
                            sendSavedStateIfAvailable();
                        } catch (Exception ignored) {}
                        // Also apply appearance to host GameScreen if present
                        try {
                            if (server.gameScreen != null && this.appearanceData != null) {
                                final int aid = this.clientId;
                                final String ad = this.appearanceData;
                                com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){
                                    @Override
                                    public void run() {
                                        try {
                                            String[] parts = ad.split("\\|");
                                            int face = 0; String color = "";
                                            try { face = Integer.parseInt(parts[0]); } catch (Exception ignored) {}
                                            try { color = parts.length > 1 ? java.net.URLDecoder.decode(parts[1], "UTF-8") : ""; } catch (Exception ignored) {}
                                            server.gameScreen.updatePlayerAppearance(aid, face, color);
                                            // Also set display name if provided (third part)
                                            try { if (parts.length > 2) server.gameScreen.setPlayerDisplayName(aid, java.net.URLDecoder.decode(parts[2], "UTF-8")); } catch (Exception ignored) {}
                                        } catch (Exception e) {
                                            Gdx.app.error("Server", "Failed to apply appearance to host GameScreen", e);
                                        }
                                    }
                                });
                            }
                        } catch (Exception ignored) {}
                    } else {
                        // If this server was started in-process and has a GameScreen, try to apply TILE_BUILD_START locally
                        // Special handling: detect SPAWNREMOTE from a client — mark them announced and relay to others
                        if (message.startsWith("SPAWNREMOTE")) {
                            // Mark this client announced so future disconnects will be broadcast
                            try { this.announced = true; } catch (Exception ignored) {}
                            // Broadcast to others that this client spawned
                            server.broadcast(this.clientId + ":" + "SPAWNREMOTE", this);
                            // If we already have this client's appearance, broadcast it immediately to avoid blank nick puppet
                            try { if (this.appearanceData != null) server.broadcast("APPEARANCE:" + this.clientId + ":" + this.appearanceData, this); } catch (Exception ignored) {}
                            
                            // Если у сервера есть GameScreen, обработаем SPAWNREMOTE локально 
                            // для создания игрока на стороне хоста
                            if (server.gameScreen != null) {
                                final int cid = this.clientId;
                                Gdx.app.postRunnable(() -> {
                                    try {
                                        MultiplayerNetworkHelper.handleSpawnRemote(server.gameScreen, "SPAWNREMOTE", cid);
                                    } catch (Exception e) {
                                        Gdx.app.error("Server", "Failed to handle SPAWNREMOTE locally", e);
                                    }
                                });
                            }
                            continue;
                        }
                        if (message.startsWith("TILE_BUILD_START:") && server.gameScreen != null) {
                            try {
                                // Broadcast the message to all clients first
                                server.broadcast(this.clientId + ":" + message, null);

                                // Then use the unified handler to apply locally
                                if (!MultiplayerNetworkHelper.handleTileBuildStart(server.gameScreen, message, this.clientId)) {
                                    Gdx.app.error("Server", "Failed to handle TILE_BUILD_START via helper", null);
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to schedule TILE_BUILD_START application", e);
                            }
                        }
                        // Apply client-initiated ITEM_DROP locally and rebroadcast
                        if (message.startsWith("ITEM_DROP:") && server.gameScreen != null) {
                            try {
                                // Broadcast to other clients
                                server.broadcast(this.clientId + ":" + message, this);
                                String[] parts = message.split(":", 5);
                                if (parts.length >= 5) {
                                    final int wx = safeParseInt(parts[1], Integer.MIN_VALUE);
                                    final int wy = safeParseInt(parts[2], Integer.MIN_VALUE);
                                    String encItem = parts[3];
                                    String itemId = null;
                                    final int amount = safeParseInt(parts[4], 1);
                                    try { 
                                        itemId = java.net.URLDecoder.decode(encItem, "UTF-8"); 
                                    } catch (Exception e) { 
                                        // fallback to raw
                                        itemId = encItem; 
                                    }
                                    final String finalItemId = itemId;
                                    com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){ @Override public void run() {
                                        try {
                                            com.cairn4.moonbase.Chunk ch = server.gameScreen.world.ensureChunkLoadedForNetwork(wx, wy);
                                            com.badlogic.gdx.math.GridPoint2 local = com.cairn4.moonbase.World.convertWorldToLocal(new com.badlogic.gdx.math.GridPoint2(0,0), wx, wy);
                                            com.cairn4.moonbase.ItemStack stack = new com.cairn4.moonbase.ItemStack(finalItemId, amount);
                                            new com.cairn4.moonbase.tiles.ItemPile(server.gameScreen.world, ch, local.x, local.y, stack);
                                        } catch (Exception ignored) {}
                                    }});
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to process ITEM_DROP", e);
                            }
                            // handled
                            continue;
                        }
                        // Handle client-initiated TILE_REMOVE: let host apply locally when running in-process
                        if (message.startsWith("TILE_REMOVE:") && server.gameScreen != null) {
                            try {
                                String[] parts = message.split(":");
                                if (parts.length >= 3) {
                                    final int fwx = Integer.parseInt(parts[1]);
                                    final int fwy = Integer.parseInt(parts[2]);
                                    com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){
                                        @Override
                                        public void run() {
                                            try {
                                                // Use removeline's method 2 to ensure physics and visuals are cleaned up
                                                server.gameScreen.world.suppressNetworkTileEvents = true;
                                                try {
                                                    server.gameScreen.world.ensureChunkLoadedForNetwork(fwx, fwy);
                                                    com.cairn4.moonbase.tiles.Tile t = server.gameScreen.world.getTile(fwx, fwy);
                                                    if (t != null) t.readyToRemove = true;
                                                } catch (Exception ignored) {}
                                                try {
                                                    for (com.cairn4.moonbase.Chunk c : server.gameScreen.world.worldChunks.values()) {
                                                        try { c.updateTiles(0.016f); } catch (Exception ignored) {}
                                                    }
                                                } catch (Exception ignored) {}
                                                com.badlogic.gdx.Gdx.app.log("Server", "Applied TILE_REMOVE locally from client " + ClientHandler.this.clientId + " for (" + fwx + "," + fwy + ") via readyToRemove+updateTiles");
                                            } catch (Exception e) {
                                                com.badlogic.gdx.Gdx.app.error("Server", "Failed to apply TILE_REMOVE in render thread", e);
                                            } finally {
                                                server.gameScreen.world.suppressNetworkTileEvents = false;
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to schedule TILE_REMOVE application", e);
                            }
                        }
                        // Vehicle enter/exit and state sync
                        if ((message.startsWith("VEH_ENTER:") || (message.indexOf(':') > 0 && message.substring(message.indexOf(':') + 1).startsWith("VEH_ENTER:"))) && server.gameScreen != null) {
                            try {
                                final String parseMsg = stripSenderPrefix(message, "VEH_ENTER:");
                                final int vehId = safeParseInt(parseMsg.substring("VEH_ENTER:".length()), -1);
                                if (vehId >= 0) {
                                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                                        try {
                                            com.cairn4.moonbase.entities.Vehicle v = findVehicleForOwnerOrNear(server.gameScreen, vehId, ClientHandler.this.clientId, Float.NaN, Float.NaN);
                                            if (v != null) {
                                                boolean changed = false;
                                                if (!v.hasDriver()) {
                                                    v.setDriver(ClientHandler.this.clientId);
                                                    changed = true;
                                                } else if (!v.hasPassenger()) {
                                                    v.setPassenger(ClientHandler.this.clientId);
                                                    changed = true;
                                                }
                                                if (changed) {
                                                    String occ = "VEH_OCCUPY:" + v.id + ":" + v.driverOwnerId + ":" + v.passengerOwnerId;
                                                    server.broadcast("0:" + occ, null);
                                                    try { MultiplayerNetworkHelper.handleVehicleOccupy(server.gameScreen, occ, 0); } catch (Exception ignored) {}
                                                }
                                            }
                                        } catch (Exception e2) {
                                            Gdx.app.error("Server", "Failed to handle VEH_ENTER", e2);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to schedule VEH_ENTER", e);
                            }
                            continue;
                        }
                        if ((message.startsWith("VEH_EXIT:") || (message.indexOf(':') > 0 && message.substring(message.indexOf(':') + 1).startsWith("VEH_EXIT:"))) && server.gameScreen != null) {
                            try {
                                final String parseMsg = stripSenderPrefix(message, "VEH_EXIT:");
                                final int vehId = safeParseInt(parseMsg.substring("VEH_EXIT:".length()), -1);
                                if (vehId >= 0) {
                                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                                        try {
                                            com.cairn4.moonbase.entities.Vehicle v = findVehicleForOwnerOrNear(server.gameScreen, vehId, ClientHandler.this.clientId, Float.NaN, Float.NaN);
                                            if (v != null) {
                                                boolean changed = false;
                                                if (v.driverOwnerId == ClientHandler.this.clientId) {
                                                    v.driverOwnerId = -1;
                                                    changed = true;
                                                }
                                                if (v.passengerOwnerId == ClientHandler.this.clientId) {
                                                    v.passengerOwnerId = -1;
                                                    changed = true;
                                                }
                                                if (v.driverOwnerId < 0 && v.passengerOwnerId >= 0) {
                                                    v.promotePassengerToDriver();
                                                    changed = true;
                                                }
                                                if (changed) {
                                                    if (!v.hasDriver() && !v.hasPassenger()) v.setState(com.cairn4.moonbase.entities.Vehicle.STATES.empty);
                                                    String occ = "VEH_OCCUPY:" + v.id + ":" + v.driverOwnerId + ":" + v.passengerOwnerId;
                                                    server.broadcast("0:" + occ, null);
                                                    try { MultiplayerNetworkHelper.handleVehicleOccupy(server.gameScreen, occ, 0); } catch (Exception ignored) {}
                                                }
                                            }
                                        } catch (Exception e2) {
                                            Gdx.app.error("Server", "Failed to handle VEH_EXIT", e2);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to schedule VEH_EXIT", e);
                            }
                            continue;
                        }
                        if (message.startsWith("VEH_STATE:") || (message.indexOf(':') > 0 && message.substring(message.indexOf(':') + 1).startsWith("VEH_STATE:"))) {
                            try {
                                final String payload = stripSenderPrefix(message, "VEH_STATE:");
                                final int senderId = this.clientId;
                                // validate driver before relay/apply
                                boolean allow = false;
                                int resolvedVehId = -1;
                                com.cairn4.moonbase.entities.Vehicle resolvedVeh = null;
                                try {
                                    if (server.gameScreen != null && server.gameScreen.world != null) {
                                        String[] parts = payload.split(":", 7);
                                        int vehId = (parts.length >= 2) ? safeParseInt(parts[1], -1) : -1;
                                        float px = (parts.length >= 4) ? safeParseFloat(parts[2], Float.NaN) : Float.NaN;
                                        float py = (parts.length >= 4) ? safeParseFloat(parts[3], Float.NaN) : Float.NaN;
                                        resolvedVeh = findVehicleForOwnerOrNear(server.gameScreen, vehId, senderId, px, py);
                                        if (resolvedVeh != null) {
                                            resolvedVehId = (int)resolvedVeh.id;
                                            allow = (resolvedVeh.driverOwnerId == senderId);
                                        }
                                    }
                                } catch (Exception ignored) {}
                                if (!allow) {
                                    continue;
                                }
                                // relay to others (validated)
                                String relayPayload = payload;
                                try {
                                    if (resolvedVehId > 0) {
                                        String[] parts = payload.split(":", 7);
                                        if (parts.length >= 7) {
                                            relayPayload = "VEH_STATE:" + resolvedVehId + ":" + parts[2] + ":" + parts[3] + ":" + parts[4] + ":" + parts[5] + ":" + parts[6];
                                        }
                                    }
                                } catch (Exception ignored) {}
                                final String relayPayloadFinal = relayPayload;
                                server.broadcast("0:" + relayPayloadFinal, this);
                                if (server.gameScreen != null) {
                                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                                        try {
                                            String usePayload = relayPayloadFinal;
                                            if (!usePayload.startsWith("VEH_STATE:")) return;
                                            String[] parts = usePayload.split(":", 7);
                                            if (parts.length < 7) return;
                                            int vehId = safeParseInt(parts[1], -1);
                                            float x = safeParseFloat(parts[2], Float.NaN);
                                            float y = safeParseFloat(parts[3], Float.NaN);
                                            float rot = safeParseFloat(parts[4], 0.0f);
                                            float vx = safeParseFloat(parts[5], 0.0f);
                                            float vy = safeParseFloat(parts[6], 0.0f);
                                            if (vehId < 0) return;
                                            com.cairn4.moonbase.entities.Vehicle v = findVehicleById(server.gameScreen.world, vehId);
                                            if (v != null) {
                                                if (v.driverOwnerId != senderId) return;
                                                if (v.body != null) {
                                                    try { v.body.setTransform(x / 256.0f, y / 256.0f, rot * com.badlogic.gdx.math.MathUtils.degreesToRadians); } catch (Exception ignored) {}
                                                    try { v.body.setLinearVelocity(vx / 256.0f, vy / 256.0f); } catch (Exception ignored) {}
                                                } else {
                                                    try { v.setWorldPos(x, y); } catch (Exception ignored) {}
                                                }
                                            }
                                        } catch (Exception e2) {
                                            Gdx.app.error("Server", "Failed to apply VEH_STATE locally", e2);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to relay VEH_STATE", e);
                            }
                            continue;
                        }
                        if (message.startsWith("VEH_META:") || (message.indexOf(':') > 0 && message.substring(message.indexOf(':') + 1).startsWith("VEH_META:"))) {
                            try {
                                final String payload = stripSenderPrefix(message, "VEH_META:");
                                final int senderId = this.clientId;
                                // validate driver before relay/apply
                                boolean allow = false;
                                int resolvedVehId = -1;
                                try {
                                    if (server.gameScreen != null && server.gameScreen.world != null) {
                                        int idx1 = payload.indexOf(':');
                                        int idx2 = payload.indexOf(':', idx1 + 1);
                                        if (idx2 > 0) {
                                            int vehId = safeParseInt(payload.substring(idx1 + 1, idx2), -1);
                                            com.cairn4.moonbase.entities.Vehicle v = findVehicleForOwnerOrNear(server.gameScreen, vehId, senderId, Float.NaN, Float.NaN);
                                            if (v != null) {
                                                resolvedVehId = (int)v.id;
                                                allow = (v.driverOwnerId == senderId);
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {}
                                if (!allow) {
                                    continue;
                                }
                                // relay to others (validated)
                                String relayPayload = payload;
                                try {
                                    if (resolvedVehId > 0) {
                                        int idx1 = payload.indexOf(':');
                                        int idx2 = payload.indexOf(':', idx1 + 1);
                                        String meta = payload.substring(idx2 + 1);
                                        relayPayload = "VEH_META:" + resolvedVehId + ":" + meta;
                                    }
                                } catch (Exception ignored) {}
                                final String relayPayloadFinal = relayPayload;
                                server.broadcast("0:" + relayPayloadFinal, this);
                                if (server.gameScreen != null) {
                                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                                        try {
                                            String usePayload = relayPayloadFinal;
                                            if (!usePayload.startsWith("VEH_META:")) return;
                                            int idx1 = usePayload.indexOf(':');
                                            int idx2 = usePayload.indexOf(':', idx1 + 1);
                                            if (idx2 < 0) return;
                                            String idStr = usePayload.substring(idx1 + 1, idx2);
                                            int vehId = safeParseInt(idStr, -1);
                                            if (vehId < 0) return;
                                            com.cairn4.moonbase.entities.Entity e = server.gameScreen.world.getEntityById(vehId);
                                            if (e instanceof com.cairn4.moonbase.entities.Vehicle) {
                                                com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                                                if (v.driverOwnerId != senderId) return;
                                                MultiplayerNetworkHelper.handleVehicleMeta(server.gameScreen, usePayload, senderId);
                                            }
                                        } catch (Exception e2) {
                                            Gdx.app.error("Server", "Failed to apply VEH_META locally", e2);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to relay VEH_META", e);
                            }
                            continue;
                        }
                        if (message.startsWith("VEH_INV_SYNC:")) {
                            try {
                                final String payload = message;
                                final int senderId = this.clientId;
                                boolean allow = false;
                                String relayPayload = payload;
                                try {
                                    if (server.gameScreen != null && server.gameScreen.world != null) {
                                        int idx1 = payload.indexOf(':');
                                        int idx2 = payload.indexOf(':', idx1 + 1);
                                        if (idx2 > 0) {
                                            int vehId = safeParseInt(payload.substring(idx1 + 1, idx2), -1);
                                            com.cairn4.moonbase.entities.Vehicle v = findVehicleForOwnerOrNear(server.gameScreen, vehId, senderId, Float.NaN, Float.NaN);
                                            if (v != null) {
                                                allow = (v.inventoryLockOwnerId == senderId);
                                                if (v.id != vehId) {
                                                    String rest = payload.substring(idx2 + 1);
                                                    relayPayload = "VEH_INV_SYNC:" + v.id + ":" + rest;
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {}
                                if (!allow) {
                                    continue;
                                }
                                final String relayPayloadFinal = relayPayload;
                                server.broadcast("0:" + relayPayloadFinal, null);
                                if (server.gameScreen != null) {
                                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                                        try {
                                            MultiplayerNetworkHelper.handleVehicleInvSync(server.gameScreen, relayPayloadFinal, senderId);
                                        } catch (Exception e2) {
                                            Gdx.app.error("Server", "Failed to apply VEH_INV_SYNC locally", e2);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to relay VEH_INV_SYNC", e);
                            }
                            continue;
                        }
                        if (message.startsWith("VEH_LOCK:")) {
                            try {
                                final String payload = message;
                                final int senderId = this.clientId;
                                boolean granted = false;
                                try {
                                    String[] parts = payload.split(":", 3);
                                    if (parts.length >= 2 && server.gameScreen != null && server.gameScreen.world != null) {
                                        int vehId = safeParseInt(parts[1], -1);
                                        com.cairn4.moonbase.entities.Vehicle v = findVehicleForOwnerOrNear(server.gameScreen, vehId, senderId, Float.NaN, Float.NaN);
                                        if (v != null) {
                                            if (v.inventoryLockOwnerId < 0 || v.inventoryLockOwnerId == senderId) {
                                                v.inventoryLockOwnerId = senderId;
                                                granted = true;
                                                String ok = "VEH_LOCK:" + v.id + ":" + senderId;
                                                server.broadcast("0:" + ok, null);
                                                try { MultiplayerNetworkHelper.handleVehicleLock(server.gameScreen, ok, senderId); } catch (Exception ignored) {}
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {}
                                if (!granted) {
                                    try {
                                        String[] parts = payload.split(":", 3);
                                        int vehId = parts.length >= 2 ? safeParseInt(parts[1], -1) : -1;
                                        com.cairn4.moonbase.entities.Vehicle v = findVehicleForOwnerOrNear(server.gameScreen, vehId, senderId, Float.NaN, Float.NaN);
                                        if (v != null) vehId = (int)v.id;
                                        String deny = "VEH_LOCK_DENY:" + vehId + ":" + senderId;
                                        sendMessage("0:" + deny);
                                    } catch (Exception ignored) {}
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to handle VEH_LOCK", e);
                            }
                            continue;
                        }
                        if (message.startsWith("VEH_UNLOCK:")) {
                            try {
                                final String payload = message;
                                final int senderId = this.clientId;
                                try {
                                    String[] parts = payload.split(":", 3);
                                    if (parts.length >= 2 && server.gameScreen != null && server.gameScreen.world != null) {
                                        int vehId = safeParseInt(parts[1], -1);
                                        com.cairn4.moonbase.entities.Vehicle v = findVehicleForOwnerOrNear(server.gameScreen, vehId, senderId, Float.NaN, Float.NaN);
                                        if (v != null) {
                                            if (v.inventoryLockOwnerId == senderId) {
                                                v.inventoryLockOwnerId = -1;
                                                String ok = "VEH_UNLOCK:" + v.id + ":" + senderId;
                                                server.broadcast("0:" + ok, null);
                                                try { MultiplayerNetworkHelper.handleVehicleLock(server.gameScreen, ok, senderId); } catch (Exception ignored) {}
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {}
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to handle VEH_UNLOCK", e);
                            }
                            continue;
                        }
                        if (message.startsWith("BASE_INV_SYNC:")) {
                            try {
                                final String payload = message;
                                final int senderId = this.clientId;
                                boolean allow = false;
                                try {
                                    String[] parts = payload.split(":", 4);
                                    if (parts.length >= 3 && server.gameScreen != null && server.gameScreen.world != null) {
                                        int wx = safeParseInt(parts[1], Integer.MIN_VALUE);
                                        int wy = safeParseInt(parts[2], Integer.MIN_VALUE);
                                        if (wx != Integer.MIN_VALUE && wy != Integer.MIN_VALUE) {
                                            com.cairn4.moonbase.tiles.Tile t = server.gameScreen.world.getTile(wx, wy);
                                            if (t instanceof com.cairn4.moonbase.tiles.BaseModule) {
                                                com.cairn4.moonbase.tiles.BaseModule bm = (com.cairn4.moonbase.tiles.BaseModule)t;
                                                com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior isb =
                                                        (com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior) bm.getBehavior(com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior.class);
                                                if (isb != null && isb.inventoryLockOwnerId == senderId) {
                                                    allow = true;
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {}
                                if (!allow) {
                                    continue;
                                }
                                server.broadcast("0:" + payload, null);
                                if (server.gameScreen != null) {
                                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                                        try {
                                            MultiplayerNetworkHelper.handleBaseInvSync(server.gameScreen, payload, senderId);
                                        } catch (Exception e2) {
                                            Gdx.app.error("Server", "Failed to apply BASE_INV_SYNC locally", e2);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to relay BASE_INV_SYNC", e);
                            }
                            continue;
                        }
                        if (message.startsWith("BASE_LOCK:")) {
                            try {
                                final String payload = message;
                                final int senderId = this.clientId;
                                boolean granted = false;
                                try {
                                    String[] parts = payload.split(":", 4);
                                    if (parts.length >= 3 && server.gameScreen != null && server.gameScreen.world != null) {
                                        int wx = safeParseInt(parts[1], Integer.MIN_VALUE);
                                        int wy = safeParseInt(parts[2], Integer.MIN_VALUE);
                                        if (wx != Integer.MIN_VALUE && wy != Integer.MIN_VALUE) {
                                            com.cairn4.moonbase.tiles.Tile t = server.gameScreen.world.getTile(wx, wy);
                                            if (t instanceof com.cairn4.moonbase.tiles.BaseModule) {
                                                com.cairn4.moonbase.tiles.BaseModule bm = (com.cairn4.moonbase.tiles.BaseModule)t;
                                                com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior isb =
                                                        (com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior) bm.getBehavior(com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior.class);
                                                if (isb != null && (isb.inventoryLockOwnerId < 0 || isb.inventoryLockOwnerId == senderId)) {
                                                    isb.inventoryLockOwnerId = senderId;
                                                    granted = true;
                                                    String ok = "BASE_LOCK:" + bm.worldX + ":" + bm.worldY + ":" + senderId;
                                                    server.broadcast("0:" + ok, null);
                                                    try { MultiplayerNetworkHelper.handleBaseLock(server.gameScreen, ok, senderId); } catch (Exception ignored) {}
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {}
                                if (!granted) {
                                    try {
                                        String[] parts = payload.split(":", 4);
                                        int wx = parts.length >= 3 ? safeParseInt(parts[1], Integer.MIN_VALUE) : Integer.MIN_VALUE;
                                        int wy = parts.length >= 3 ? safeParseInt(parts[2], Integer.MIN_VALUE) : Integer.MIN_VALUE;
                                        String deny = "BASE_LOCK_DENY:" + wx + ":" + wy + ":" + senderId;
                                        sendMessage("0:" + deny);
                                    } catch (Exception ignored) {}
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to handle BASE_LOCK", e);
                            }
                            continue;
                        }
                        if (message.startsWith("BASE_UNLOCK:")) {
                            try {
                                final String payload = message;
                                final int senderId = this.clientId;
                                try {
                                    String[] parts = payload.split(":", 4);
                                    if (parts.length >= 3 && server.gameScreen != null && server.gameScreen.world != null) {
                                        int wx = safeParseInt(parts[1], Integer.MIN_VALUE);
                                        int wy = safeParseInt(parts[2], Integer.MIN_VALUE);
                                        if (wx != Integer.MIN_VALUE && wy != Integer.MIN_VALUE) {
                                            com.cairn4.moonbase.tiles.Tile t = server.gameScreen.world.getTile(wx, wy);
                                            if (t instanceof com.cairn4.moonbase.tiles.BaseModule) {
                                                com.cairn4.moonbase.tiles.BaseModule bm = (com.cairn4.moonbase.tiles.BaseModule)t;
                                                com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior isb =
                                                        (com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior) bm.getBehavior(com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior.class);
                                                if (isb != null && isb.inventoryLockOwnerId == senderId) {
                                                    isb.inventoryLockOwnerId = -1;
                                                    String ok = "BASE_UNLOCK:" + bm.worldX + ":" + bm.worldY + ":" + senderId;
                                                    server.broadcast("0:" + ok, null);
                                                    try { MultiplayerNetworkHelper.handleBaseLock(server.gameScreen, ok, senderId); } catch (Exception ignored) {}
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {}
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to handle BASE_UNLOCK", e);
                            }
                            continue;
                        }
                        if (message.startsWith("VEH_SPAWN:")) {
                            try {
                                final String payload = message;
                                server.broadcast("0:" + payload, this);
                                if (server.gameScreen != null) {
                                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                                        try {
                                            MultiplayerNetworkHelper.handleVehicleSpawn(server.gameScreen, payload, this.clientId);
                                        } catch (Exception e2) {
                                            Gdx.app.error("Server", "Failed to apply VEH_SPAWN locally", e2);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to relay VEH_SPAWN", e);
                            }
                            continue;
                        }
                        if (message.startsWith("ENTITY_SPAWN:")) {
                            try {
                                final String payload = message;
                                server.broadcast("0:" + payload, this);
                                if (server.gameScreen != null) {
                                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                                        try {
                                            MultiplayerNetworkHelper.handleEntitySpawn(server.gameScreen, payload, this.clientId);
                                        } catch (Exception e2) {
                                            Gdx.app.error("Server", "Failed to apply ENTITY_SPAWN locally", e2);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to relay ENTITY_SPAWN", e);
                            }
                            continue;
                        }
                        if (message.startsWith("GENERATOR_FUEL:")) {
                            try {
                                final String payload = message;
                                server.broadcast("0:" + payload, this);
                                if (server.gameScreen != null) {
                                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                                        try {
                                            MultiplayerNetworkHelper.handleGeneratorFuel(server.gameScreen, payload, this.clientId);
                                        } catch (Exception e2) {
                                            Gdx.app.error("Server", "Failed to apply GENERATOR_FUEL locally", e2);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Server", "Failed to relay GENERATOR_FUEL", e);
                            }
                            continue;
                        }
                        // If client sends POS updates, parse once, apply to host GameScreen (if present)
                        // and perform server-side rate-limiting before rebroadcasting to other clients.
                        if (message.startsWith("POS:") || (message.indexOf(':') > 0 && message.substring(message.indexOf(':') + 1).startsWith("POS:"))) {
                            try { // This try block was misplaced
                                String parseMsg = message;
                                // Strip leading "<id>:" if present
                                int firstColon = parseMsg.indexOf(':');
                                if (firstColon > 0 && !parseMsg.startsWith("POS:")) {
                                    String after = parseMsg.substring(firstColon + 1);
                                    if (after.startsWith("POS:")) parseMsg = after;
                                }
                                String rest = parseMsg.substring("POS:".length());
                                String[] parts = rest.split(":");
                                final int pid;
                                final float px;
                                final float py;
                                final float vx;
                                final float vy;
                                // Support both formats: POS:x:y and POS:PLAYER:id:x:y:vx:vy
                                if (parts.length >= 2 && !"PLAYER".equals(parts[0])) {
                                    px = safeParseFloat(parts[0], Float.NaN);
                                    py = safeParseFloat(parts[1], Float.NaN);
                                    pid = this.clientId;
                                    vx = 0.0f;
                                    vy = 0.0f;
                                } else if (parts.length >= 5 && "PLAYER".equals(parts[0])) {
                                    pid = safeParseInt(parts[1], this.clientId);
                                    px = safeParseFloat(parts[2], Float.NaN);
                                    py = safeParseFloat(parts[3], Float.NaN);
                                    vx = safeParseFloat(parts[4], 0.0f);
                                    vy = (parts.length > 5) ? safeParseFloat(parts[5], 0.0f) : 0.0f;
                                } else {
                                    // Unrecognized format
                                    continue;
                                }
                                // Throttled POS logging (max once per second)
                                try {
                                    long nowLog = System.currentTimeMillis();
                                    if (nowLog - this.lastBroadcastPosMillis >= 1000L) {
                                        Gdx.app.log("Server", "[POS] from " + pid + ": x=" + px + " y=" + py + " vx=" + vx + " vy=" + vy);
                                    }
                                } catch (Exception ignored) {}
                                     // Always update host GameScreen position if present so host sees movement locally
                                     if (server.gameScreen != null) {
                                         com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){
                                             @Override
                                             public void run() {
                                                 try {
                                                     // Prefer same path as clients: update targetPosition and velocity
                                                     com.cairn4.moonbase.Player remote = server.gameScreen.getRemotePlayer(pid);
                                                     if (remote != null) {
                                                         try {
                                                             java.lang.reflect.Field targetPosField = com.cairn4.moonbase.Player.class.getDeclaredField("targetPosition");
                                                             targetPosField.setAccessible(true);
                                                             Object vec = targetPosField.get(remote);
                                                             if (vec instanceof com.badlogic.gdx.math.Vector2) {
                                                                 ((com.badlogic.gdx.math.Vector2)vec).set(px, py);
                                                             }
                                                         } catch (Exception ignored) {}
                                                         try { remote.body.setLinearVelocity(vx / 256.0f, vy / 256.0f); } catch (Exception ignored) {}
                                                         try { remote.forcePositionUpdate(); } catch (Exception ignored) {}
                                                     } else {
                                                         // If not yet present, add and then set
                                                         try { server.gameScreen.addPlayer(pid, px, py); } catch (Exception ignored) {}
                                                     }
                                                 } catch (Exception e) {
                                                     Gdx.app.error("Server", "Failed to update host player position for client " + pid, e);
                                                 }
                                             }
                                         });
                                     }
 
                                     // Rate-limit rebroadcasts: only broadcast if moved beyond threshold or max interval elapsed
                                     boolean shouldBroadcast = false;
                                     long now = System.currentTimeMillis();
                                     try {
                                         if (Float.isNaN(this.lastBroadcastPosX) || Float.isNaN(this.lastBroadcastPosY)) {
                                             shouldBroadcast = true;
                                         } else {
                                             float dx = Math.abs(px - this.lastBroadcastPosX);
                                             float dy = Math.abs(py - this.lastBroadcastPosY);
                                             boolean moved = dx >= CLIENT_POS_THRESHOLD || dy >= CLIENT_POS_THRESHOLD;
                                             boolean timeElapsed = (now - this.lastBroadcastPosMillis) >= CLIENT_POS_MAX_INTERVAL;
                                             if (moved || timeElapsed) shouldBroadcast = true;
                                         }
                                     } catch (Exception ignored) { shouldBroadcast = true; }
 
                                     if (shouldBroadcast) {
                                         try {
                                             // Reformat broadcast to unified PLAYER form so all clients handle consistently
                                             String payload = "POS:PLAYER:" + pid + ":" + px + ":" + py + ":" + vx + ":" + vy;
                                             server.broadcast(this.clientId + ":" + payload, this);
                                             this.lastBroadcastPosX = px;
                                             this.lastBroadcastPosY = py;
                                             this.lastBroadcastPosMillis = now;
                                         } catch (Exception e) {
                                             Gdx.app.error("Server", "Failed to broadcast POS for client " + this.clientId, e);
                                         }
                                     } else {
                                         // suppressed frequent POS: occasionally log to help debugging
                                         try {
                                             if ((now % 5000L) < 50L) {
                                                 Gdx.app.log("Server", "Suppressed POS broadcast from client " + this.clientId + " (dx=" + Math.abs(px - this.lastBroadcastPosX) + ", dy=" + Math.abs(py - this.lastBroadcastPosY) + ")");
                                             }
                                         } catch (Exception ignored) {}
                                     }
                            } catch (Exception ignored) {} // This catch block was associated with the misplaced try
                            // we've handled POS (including possible rebroadcast) so skip default broadcasting below
                            continue;
                        }
                        // Prepend the client's ID to the message before broadcasting
                        // Special-case PING: echo to everyone (including origin) so the sender sees the reply
                        // Suppress broadcasting of ITEM_DROP for builder items (modules) so only the breaker receives the builder locally.
                        if (message.startsWith("ITEM_DROP:")) {
                            try {
                                String[] parts = message.split(":");
                                if (parts.length >= 4) {
                                    String itemId = parts[3];
                                    try { itemId = java.net.URLDecoder.decode(itemId, "UTF-8"); } catch (Exception ignored) {}
                                    if (itemId != null && itemId.endsWith("-builder")) {
                                        Gdx.app.log("Server", "Suppressing broadcast of builder ITEM_DROP from client " + this.clientId + " for item " + itemId);
                                        // Optionally, if running as host with a GameScreen, we could apply this locally here.
                                        continue;
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                        if (message.startsWith("PING:")) {
                            server.broadcast(this.clientId + ":" + message, null);
                        } else if (message.startsWith("CHAT:")) {
                            // Ensure chat is echoed back to the sender so they see their own messages
                            try {
                                // message is CHAT:<encNick>:<encText>; replace encNick with server-resolved nick for this.clientId
                                String rest = message.substring("CHAT:".length());
                                int idx = rest.indexOf(":");
                                String encNick = idx >= 0 ? rest.substring(0, idx) : "";
                                String encText = idx >= 0 ? rest.substring(idx + 1) : rest;
                                String serverNick = encNick;
                                try {
                                    String decided = null;
                                    // reuse appearanceData's nick if available and possibly adjusted
                                    if (this.appearanceData != null) {
                                        String[] ps = this.appearanceData.split("\\|");
                                        if (ps.length > 2) decided = ps[2];
                                    }
                                    if (decided != null && decided.length() > 0) serverNick = decided; // already URL-encoded
                                } catch (Exception ignored) {}
                                String rebuilt = "CHAT:" + serverNick + ":" + encText;
                                server.broadcast(this.clientId + ":" + rebuilt, null);
                            } catch (Exception ignored) {
                                server.broadcast(this.clientId + ":" + message, null);
                            }
                            // If this server is running in-process with a GameScreen attached, also
                            // post the chat to the host's HUD so the host sees it locally (server
                            // rebroadcast will be sent to the local client path, but when running
                            // headless/local this ensures display).
                            try {
                                if (server.gameScreen != null) {
                                    final String rest = message.substring("CHAT:".length());
                                    int idx = rest.indexOf(":");
                                    final String encNick = idx >= 0 ? rest.substring(0, idx) : "";
                                    final String encText = idx >= 0 ? rest.substring(idx + 1) : rest;
                                    String resolvedEncNick = encNick;
                                    try { if (this.appearanceData != null) { String[] ps = this.appearanceData.split("\\|"); if (ps.length > 2) resolvedEncNick = ps[2]; } } catch (Exception ignored) {}
                                    final String nick = java.net.URLDecoder.decode(resolvedEncNick == null ? "" : resolvedEncNick, "UTF-8");
                                    final String text = java.net.URLDecoder.decode(encText == null ? "" : encText, "UTF-8");
                                    com.badlogic.gdx.Gdx.app.postRunnable(new Runnable() {
                                        @Override public void run() {
                                            try {
                                                if (server.gameScreen != null && server.gameScreen.hud != null && server.gameScreen.hud.hudNotifications != null) {
                                                    server.gameScreen.hud.hudNotifications.newChatMessage(nick, text);
                                                }
                                            } catch (Exception ignored) {}
                                        }
                                    });
                                }
                            } catch (Exception ignored) {}
                        } else {
                            // Handle inventory updates from clients and persist
                            if (message.startsWith("INVENTORY_UPDATE:")) {
                                try {
                                    String data = message.substring("INVENTORY_UPDATE:".length());
                                    // Expect URL-encoded JSON string per client
                                    String json = java.net.URLDecoder.decode(data, "UTF-8");
                                    try {
                                        com.badlogic.gdx.files.FileHandle fh = com.badlogic.gdx.Gdx.files.local("saves/" + com.cairn4.moonbase.MoonBase.currentSaveFolder + "/multiplayersdata.json");
                                        String existing = fh.exists() ? fh.readString("UTF-8") : "{}";
                                        // Very simple merge: write per-client id key to file (string replace style)
                                        // For decompiled context, avoid JSON lib: store blocks as \n  "<id>": <json>
                                        String key = "\n  \"" + this.clientId + "\": ";
                                        String content;
                                        if (existing.contains(key)) {
                                            // replace existing client's block
                                            int idx = existing.indexOf(key);
                                            int end = existing.indexOf("\n  \"", idx + 1);
                                            if (end < 0) end = existing.lastIndexOf('}');
                                            content = existing.substring(0, idx) + key + json + (end >= 0 ? existing.substring(end) : "\n}");
                                        } else {
                                            String body = existing.trim();
                                            if (body.equals("{}")) content = "{" + key + json + "\n}"; else content = body.substring(0, body.lastIndexOf('}')) + "," + key + json + "\n}";
                                        }
                                        fh.writeString(content, false, "UTF-8");
                                    } catch (Exception e) { Gdx.app.error("Server", "Failed to persist inventory", e); }
                                } catch (Exception ignored) {}
                                continue;
                            }
                            if (message.startsWith("PLAYER_STATE:")) {
                                try {
                                    String data = message.substring("PLAYER_STATE:".length());
                                    String json = java.net.URLDecoder.decode(data, "UTF-8");
                                    String key = getPlayerStateKey();
                                    server.storePlayerState(key, json);
                                } catch (Exception ignored) {}
                                continue;
                            }
                            server.broadcast(this.clientId + ":" + message, this);
                        }
                    }
        } // End of while (server.running) loop
    } catch (IOException e) {
        // Client disconnected or error
    } finally {
        server.removeClient(this.clientId);
        close();
    }
}

        private String getPlayerStateKey() {
            try {
                if (this.appearanceData != null) {
                    String[] ps = this.appearanceData.split("\\|");
                    if (ps.length > 2) {
                        String decNick = java.net.URLDecoder.decode(ps[2], "UTF-8");
                        if (decNick != null && decNick.trim().length() > 0) return decNick.trim();
                    }
                }
            } catch (Exception ignored) {}
            return "id:" + this.clientId;
        }

        private void sendSavedStateIfAvailable() {
            if (sentSavedState) return;
            try {
                String key = getPlayerStateKey();
                String json = server.getSavedPlayerState(key);
                if (json == null || json.length() == 0) return;
                String enc = java.net.URLEncoder.encode(json, "UTF-8");
                sendMessage("INVENTORY_UPDATE:" + enc);
                sentSavedState = true;
            } catch (Exception ignored) {}
        }

        private void sendInitialWorldData() {
            try {
                // If hosting in-process and a GameScreen is attached, synchronously save the world on the render thread
                try {
                    if (this.server != null && this.server.gameScreen != null && this.server.gameScreen.world != null) {
                        final Server serverRef = this.server;
                        final CountDownLatch latch = new CountDownLatch(1);
                        com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){ @Override public void run() {
                            try {
                                try { if (serverRef.gameScreen != null) serverRef.gameScreen.gameLoader.saveGame(serverRef.gameScreen.world, false); } catch (Exception ignored) {}
                            } finally {
                                latch.countDown();
                            }
                        }});
                        try {
                            // Wait up to 5 seconds for save to complete; proceed even if timeout
                            latch.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException ignored) {}
                    }
                } catch (Exception ignored) {}

                out.writeInt(this.clientId);

                com.badlogic.gdx.files.FileHandle gameSaveFile = com.badlogic.gdx.Gdx.files.local("saves/" + com.cairn4.moonbase.MoonBase.currentSaveFolder + "/gameSave.json");
                if (gameSaveFile.exists()) {
                    byte[] gameSaveBytes = gameSaveFile.readBytes();
                    out.writeInt(gameSaveBytes.length);
                    out.write(gameSaveBytes);
                } else {
                    out.writeInt(0);
                }

                com.badlogic.gdx.files.FileHandle worldDataFile = com.badlogic.gdx.Gdx.files.local("saves/" + com.cairn4.moonbase.MoonBase.currentSaveFolder + "/worldData.json");
                if (worldDataFile.exists()) {
                    byte[] worldDataBytes = worldDataFile.readBytes();
                    out.writeInt(worldDataBytes.length);
                    out.write(worldDataBytes);
                } else {
                    out.writeInt(0);
                }
                out.flush();
                Gdx.app.log("ClientHandler", "Initial world data sent to client " + this.clientId);
            } catch (IOException e) {
                Gdx.app.error("ClientHandler", "Failed to send world data to client " + this.clientId, e);
            }
        }

        public void sendMessage(String message) {
            synchronized (this.outLock) {
                if (this.out == null) {
                    return;
                }
                String frame = message;
                ProtocolV2.Decoded d = decodeLegacyOrFrame(message, 0);
                if (d != null) {
                    if (isReliableType(d.type) && !"ACK".equals(d.type)) {
                        int seq = reliableSeq.getAndIncrement();
                        if (seq <= 0) seq = reliableSeq.getAndIncrement();
                        String relPayload = "SEQ:" + seq + ":" + (d.payload == null ? "" : d.payload);
                        frame = ProtocolV2.encode(d.fromId, d.type, relPayload);
                        if (frame != null) {
                            pendingReliable.put(seq, new PendingReliable(frame, System.currentTimeMillis()));
                        }
                    } else {
                        frame = encodeFrame(d);
                    }
                }
                if (frame == null) return;
                try {
                    long now = System.currentTimeMillis();
                    if ((now % 1000L) < 50L) {
                        System.out.println("ClientHandler.sendMessage: clientId=" + this.clientId + " message=" + frame);
                    }
                } catch (Exception ignored) {}
                try {
                    this.out.writeUTF(frame);
                    this.out.flush();
                    try {
                        long now2 = System.currentTimeMillis();
                        if ((now2 % 1000L) < 50L) {
                            appendDebugLog("SENT: " + frame);
                        }
                    } catch (Exception ignored) {}
                } catch (Exception e) {
                    Gdx.app.error("ClientHandler", "Failed to send payload", e);
                }
            }
        }

        private void appendDebugLog(String line) {
            // Implement or remove this method based on actual usage
            // For now, it will just log to Gdx.app.log
            Gdx.app.log("ClientHandler-Debug", line);
        }

        public void close() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                // Release vehicle inventory locks held by this client
                try {
                    if (this.server != null && this.server.gameScreen != null && this.server.gameScreen.world != null) {
                        for (com.cairn4.moonbase.entities.Entity e : this.server.gameScreen.world.entityList) {
                            if (e instanceof com.cairn4.moonbase.entities.Vehicle) {
                                com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                                if (v.inventoryLockOwnerId == this.clientId) {
                                    v.inventoryLockOwnerId = -1;
                                    String ok = "VEH_UNLOCK:" + v.id + ":" + this.clientId;
                                    try { this.server.broadcast("0:" + ok, null); } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
                // Release base storage locks held by this client
                try {
                    if (this.server != null && this.server.gameScreen != null && this.server.gameScreen.world != null) {
                        for (com.cairn4.moonbase.Chunk c : this.server.gameScreen.world.worldChunks.values()) {
                            for (com.cairn4.moonbase.tiles.Tile t : c.tiles.values()) {
                                if (!(t instanceof com.cairn4.moonbase.tiles.BaseModule)) continue;
                                com.cairn4.moonbase.tiles.BaseModule bm = (com.cairn4.moonbase.tiles.BaseModule)t;
                                com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior isb =
                                        (com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior) bm.getBehavior(com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior.class);
                                if (isb != null && isb.inventoryLockOwnerId == this.clientId) {
                                    isb.inventoryLockOwnerId = -1;
                                    String ok = "BASE_UNLOCK:" + bm.worldX + ":" + bm.worldY + ":" + this.clientId;
                                    try { this.server.broadcast("0:" + ok, null); } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
                // Cleanup on close: remove puppet from host GameScreen and free nick count
                try {
                    if (this.server != null && this.server.gameScreen != null) {
                        final int rid = this.clientId;
                        com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){ @Override public void run() {
                            try { ClientHandler.this.server.gameScreen.removePlayer(rid); } catch (Exception ignored) {}
                        }});
                    }
                } catch (Exception ignored) {}
                try {
                    if (this.appearanceData != null) {
                        String[] ps = this.appearanceData.split("\\|");
                        if (ps.length > 2) {
                            String decNick = java.net.URLDecoder.decode(ps[2], "UTF-8");
                            Integer cnt = (this.server != null) ? this.server.usedNames.get(decNick) : null;
                            if (cnt != null) {
                                int n = cnt.intValue() - 1;
                                if (this.server != null) {
                                    if (n <= 0) this.server.usedNames.remove(decNick); else this.server.usedNames.put(decNick, Integer.valueOf(n));
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
