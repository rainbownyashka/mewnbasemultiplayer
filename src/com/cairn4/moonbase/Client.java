package com.cairn4.moonbase;

 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.math.GridPoint2;
 import com.badlogic.gdx.scenes.scene2d.Action;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.cairn4.moonbase.Chunk;
 import com.cairn4.moonbase.ItemStack;
 import com.cairn4.moonbase.MoonBase;
 import com.cairn4.moonbase.NetworkHelper;
 import com.cairn4.moonbase.Player;
 import com.cairn4.moonbase.World;
 import com.cairn4.moonbase.entities.Entity;
 import com.cairn4.moonbase.tiles.ItemPile;
 import com.cairn4.moonbase.tiles.ProtoBase;
 import com.cairn4.moonbase.tiles.Tile;
 import com.cairn4.moonbase.tiles.TileFactory;
 import com.cairn4.moonbase.ui.BuildingPlacementCursor;
 import com.cairn4.moonbase.ui.GameScreen;
 import com.cairn4.moonbase.worlddata.TileData;
import com.cairn4.moonbase.net.ProtocolV2;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentSkipListSet;

 public class Client {
     public GameScreen screen;
    private Map<String, Long> recentMessages = new ConcurrentHashMap<>();
    private static final long DUPLICATE_WINDOW_MS = 500L;
    private Map<Integer, Float> flipOverrides = new ConcurrentHashMap<>();
    private Set<Integer> pendingSpawns = new ConcurrentSkipListSet<>();
    private Set<Integer> pendingConnectNotify = new ConcurrentSkipListSet<>();
    private Map<Integer, String> knownNicks = new ConcurrentHashMap<>();
    private volatile String pendingTechSyncPayload = null;
     private int clientId = -1;
     private Socket socket;
     private DataInputStream in;
     private DataOutputStream out;
     private final Object outLock = new Object();
    private Thread senderThread;
    private Thread readerThread;
    private volatile boolean running = false;
    private final java.util.concurrent.ConcurrentHashMap<Integer, PendingReliable> pendingReliable = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicInteger reliableSeq = new java.util.concurrent.atomic.AtomicInteger(1);
    private long lastReliableSweep = 0L;
    private static final int RELIABLE_RESEND_MS = 600;
    private static final int RELIABLE_MAX_ATTEMPTS = 6;
     private boolean sendEnabled = true;
    private float lastSentX = Float.NaN;
    private float lastSentY = Float.NaN;
    private long lastSentMillis = 0L;
     private long lastVehSentMillis = 0L;
     private long lastVehMetaSentMillis = 0L;
     private String lastVehMeta = null;
    private long lastPlayerStateSentMillis = 0L;
     private boolean diagnosticMode = false;
     private String host;
     private int port;

     public Client(String host, int port, GameScreen screen) {
         this.host = host;
         this.port = port;
         this.screen = screen;
     }

    private void failInitialSync(String reason) {
        try { Gdx.app.error("Client", "Initial sync failed: " + reason, null); } catch (Exception ignored) {}
        try {
            if (this.screen != null) {
                final String msg = reason;
                Gdx.app.postRunnable(() -> {
                    try {
                        if (Client.this.screen != null && Client.this.screen.hud != null && Client.this.screen.hud.hudNotifications != null) {
                            Client.this.screen.hud.hudNotifications.newMessageInstant(msg);
                        }
                    } catch (Exception ignored) {}
                    try {
                        Client.this.screen.errorReturnToMainMenu(com.cairn4.moonbase.LoadingErrors.loadingWorldData);
                    } catch (Exception ignored) {}
                });
            }
        } catch (Exception ignored) {}
        try { disconnect(); } catch (Exception ignored) {}
    }

    public void connect() {
        if (this.running) {
            return;
        }
        try {
            String saveDir = "saves/multiplayer_received/";
            com.badlogic.gdx.files.FileHandle dir = com.badlogic.gdx.Gdx.files.local(saveDir);
            dir.deleteDirectory();
            dir.mkdirs();
            try { com.badlogic.gdx.Gdx.files.local(saveDir + ".sync_done").delete(); } catch (Exception ignored) {}
            try { com.cairn4.moonbase.MoonBase.multiplayerSyncReady = false; } catch (Exception ignored) {}
        } catch (Exception ignored) {}
        this.running = true;
        new Thread(() -> {
            try {
                this.socket = new Socket();
                this.socket.connect(new InetSocketAddress(this.host, this.port), 5000);
                 this.socket.setTcpNoDelay(true);
                 try { this.socket.setSoTimeout(15000); } catch (Exception ignored) {} // avoid black-screen hangs during blob sync
                 this.in = new DataInputStream(this.socket.getInputStream());
                 this.out = new DataOutputStream(this.socket.getOutputStream());
                 try {
                     try {
                         this.clientId = this.in.readInt();
                         Gdx.app.log("Client", "Assigned clientId from server: " + this.clientId);
                         try { System.out.println("[Client] connect: received clientId=" + this.clientId); } catch (Exception ignored) {}
                         // Server will send existing appearances during connect; no need to request here
                         // Align local player's ownerId with assigned clientId and set local display name
                         try {
                             if (this.screen != null && this.screen.world != null && this.screen.world.player != null) {
                                 final int cid = this.clientId;
                                 com.badlogic.gdx.Gdx.app.postRunnable(new Runnable(){ @Override public void run() {
                                     try {
                                         try { Client.this.screen.world.player.ownerId = cid; } catch (Exception ignored) {}
                                         try {
                                             String nick = com.cairn4.moonbase.MoonBase.multiplayerNick != null ? com.cairn4.moonbase.MoonBase.multiplayerNick : "";
                                             try { com.cairn4.moonbase.Player p = Client.this.screen.world.player; if (p != null) p.setDisplayName(nick); } catch (Exception ignored) {}
                                         } catch (Exception ignored) {}
                                     } catch (Exception ignored) {}
                                 }});
                             }
                         } catch (Exception ignored) {}
                     } catch (Exception e) {
                         Gdx.app.error("Client", "Failed reading assigned clientId (continuing)", e);
                     }
                     boolean gotGameSave = false;
                     boolean gotWorldData = false;
                     try {
                         int gameSaveLen = this.in.readInt();
                        if (gameSaveLen > 0) {
                            // Ensure multiplayer_received directory exists
                            String saveDir = "saves/multiplayer_received/";
                            com.badlogic.gdx.Gdx.files.local(saveDir).mkdirs();
                            try { com.badlogic.gdx.Gdx.files.local(saveDir + ".sync_done").delete(); } catch (Exception ignored) {}
                             
                             byte[] gameSaveBytes = new byte[gameSaveLen];
                             this.in.readFully(gameSaveBytes);
                             
                             com.badlogic.gdx.files.FileHandle receivedGameSave = com.badlogic.gdx.Gdx.files.local(saveDir + "gameSave.json");
                             receivedGameSave.writeBytes(gameSaveBytes, false);
                             Gdx.app.log("Client", "gameSave.json received (" + gameSaveLen + " bytes).");
                             try { System.out.println("[Client] connect: wrote gameSave.json, len=" + gameSaveLen); } catch (Exception ignored) {}
                             gotGameSave = true;
                             
                             // Set current save folder to multiplayer_received for loading
                             try { com.cairn4.moonbase.MoonBase.currentSaveFolder = "multiplayer_received"; } catch (Exception ignored) {}
                             try { Gdx.app.log("Client", "Set currentSaveFolder to: " + com.cairn4.moonbase.MoonBase.currentSaveFolder); } catch (Exception ignored) {}
                         }
                     } catch (java.net.SocketTimeoutException ste) {
                         failInitialSync("Timed out receiving gameSave.json from server.");
                         return;
                     } catch (Exception e) {
                         Gdx.app.error("Client", "Failed reading initial gameSave blob", e);
                         failInitialSync("Failed receiving gameSave.json from server.");
                         return;
                     }
                     try {
                         int worldDataLen = this.in.readInt();
                        if (worldDataLen > 0) {
                            String saveDir = "saves/multiplayer_received/";
                            byte[] worldDataBytes = new byte[worldDataLen];
                            this.in.readFully(worldDataBytes);

                            com.badlogic.gdx.files.FileHandle receivedWorldData = com.badlogic.gdx.Gdx.files.local(saveDir + "worldData.json");
                            receivedWorldData.writeBytes(worldDataBytes, false);
                            Gdx.app.log("Client", "worldData.json received (" + worldDataLen + " bytes).");
                            try { System.out.println("[Client] connect: wrote worldData.json, len=" + worldDataLen); } catch (Exception ignored) {}
                            try { com.badlogic.gdx.Gdx.files.local(saveDir + ".sync_done").writeString("ok", false); } catch (Exception ignored) {}
                            try { com.cairn4.moonbase.MoonBase.currentSaveFolder = "multiplayer_received"; } catch (Exception ignored) {}
                            gotWorldData = true;
                        } else {
                            failInitialSync("Server did not provide worldData.json (len=0).");
                            return;
                        }
                    } catch (java.net.SocketTimeoutException ste) {
                        failInitialSync("Timed out receiving worldData.json from server.");
                        return;
                    } catch (Exception e) {
                        Gdx.app.error("Client", "Failed reading initial worldData blob", e);
                        failInitialSync("Failed receiving worldData.json from server.");
                        return;
                    }
                    if (gotGameSave && gotWorldData) {
                        try { com.cairn4.moonbase.MoonBase.multiplayerSyncReady = true; } catch (Exception ignored) {}
                        Gdx.app.log("Client", "Initial sync complete (gameSave + worldData).");
                    }
                 } catch (Exception ex) {
                     Gdx.app.error("Client", "Error consuming initial server blobs", ex);
                 }
                 // Wait for server INIT_DONE marker before starting text protocol to avoid binary/text interleaving
                 try {
                     try { System.out.println("[Client] connect: waiting for INIT_DONE from server..."); } catch (Exception ignored) {}
                     try { this.socket.setSoTimeout(15000); } catch (Exception ignored) {}
                     try {
                         String init = this.in.readUTF();
                         try { System.out.println("[Client] connect: received init marker='" + init + "'"); } catch (Exception ignored) {}
                         if (!"INIT_DONE".equals(init)) {
                             Gdx.app.error("Client", "Unexpected init marker from server: " + init);
                         }
                     } catch (java.net.SocketTimeoutException ste) {
                         Gdx.app.error("Client", "Timed out waiting for INIT_DONE from server", ste);
                     } catch (Exception e) {
                         Gdx.app.error("Client", "Error reading INIT_DONE marker (continuing)", e);
                     } finally {
                         try { this.socket.setSoTimeout(0); } catch (Exception ignored) {}
                     }
                 } catch (Exception ignored) {}
                 try { this.socket.setSoTimeout(0); } catch (Exception ignored) {}

                 try { System.out.println("[Client] connect: initializing reader thread"); } catch (Exception ignored) {}
                 this.readerThread = new Thread(() -> {
                     try {
                         System.out.println("[Client] Reader thread started");
                         while (this.running && this.socket != null && !this.socket.isClosed()) {
                             try {
                                 String msg = this.in.readUTF();
                                 if (msg == null)
                                     break;
                                 try { System.out.println("[Client] Reader got raw: " + msg); } catch (Exception ignored) {}
                                 handleMessage(msg);
                             } catch (java.io.EOFException eof) {
                                 System.out.println("[Client] Reader EOF");
                                 break;
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Reader thread error", e);
                                 try {
                                     Thread.sleep(100);
                                 } catch (InterruptedException interruptedException) {
                                 }
                             }
                         }
                         System.out.println("[Client] Reader thread exiting loop");
                     } finally {
                         try {
                             disconnect();
                         } catch (Exception exception) {
                         }
                     }
                 }, "MewnBase-Client-Reader");
                this.readerThread.setDaemon(true);
                this.readerThread.start();
                try { System.out.println("[Client] connect: reader thread started (connector)"); } catch (Exception ignored) {}
                // Inform server we are ready to receive initial live messages
                try { System.out.println("[Client] connect: sending READY"); sendFrame(ProtocolV2.encode(this.clientId, "READY", ProtocolV2.VERSION)); } catch (Exception ignored) {}
                startSender();
            } catch (Exception e) {
                Gdx.app.error("Client", "Failed to connect to " + this.host + ":" + this.port, e);
                this.running = false;
            }
         }, "MewnBase-Client-Connector").start();
     }

    private void handleMessage(String msg) {
        if (this.diagnosticMode) {
            Gdx.app.log("Client", "Received: " + msg);
            try {
                System.out.println("[Client] Received: " + msg);
            } catch (Exception exception) {
            }
            if (this.screen != null && this.screen.game != null && this.screen.game.console != null) {
                final String raw = msg;
                Gdx.app.postRunnable(() -> {
                    try {
                        this.screen.game.console.log("[NET] " + raw);
                    } catch (Exception exception) {
                    }
                });
            }
        }
         try {
             // NOTE: Do not globally dedupe messages by exact string; handle throttling per-type if needed
             appendDebugLog("RECEIVED: " + msg);
         } catch (Exception exception) {
         }
         try {
             flushPendingSpawns();
             flushPendingTechSync();
             ProtocolV2.Decoded decoded = ProtocolV2.decode(msg);
             if (decoded == null) {
                 return;
             }
            final int srcId = decoded.fromId;
            final String type = decoded.type != null ? decoded.type : "";
            String payload = decoded.payload != null ? decoded.payload : "";
            if ("ACK".equals(type)) {
                try {
                    int seq = safeParseInt(payload.trim(), -1);
                    if (seq >= 0) pendingReliable.remove(seq);
                } catch (Exception ignored) {}
                return;
            }
            if (isReliableType(type)) {
                ParsedReliable pr = parseReliablePayload(payload);
                if (pr.seq >= 0) {
                    sendAck(pr.seq);
                    payload = pr.payload;
                }
            }
            if ("CONNECTED".equals(type)) {
                final int id = safeParseInt(payload.trim(), -1);
                Gdx.app.log("Client", "Noted CONNECTED:" + id + " (will send SPAWNREMOTE)");
                 try {
                     // First send our APPEARANCE so others don't spawn an empty puppet
                     if (this.screen != null && this.screen.world != null && this.screen.world.player != null) {
                         int face = 0; String color = ""; String nick = "";
                         try { face = this.screen.world.player.characterFaceOption; } catch (Exception ignored) {}
                         try { color = this.screen.world.player.characterSuitColor != null ? this.screen.world.player.characterSuitColor : ""; } catch (Exception ignored) {}
                         try { nick = com.cairn4.moonbase.MoonBase.multiplayerNick != null ? com.cairn4.moonbase.MoonBase.multiplayerNick : ""; } catch (Exception ignored) {}
                         String payloadAppearance = "APPEARANCE:" + face + "|" + java.net.URLEncoder.encode(color == null ? "" : color, "UTF-8") + "|" + java.net.URLEncoder.encode(nick == null ? "" : nick, "UTF-8");
                         NetworkHelper.sendPayload(this.screen, payloadAppearance);
                         // Then announce spawn
                         NetworkHelper.sendPayload(this.screen, "SPAWNREMOTE:" + id);
                     } else {
                        sendMessage("APPEARANCE:0||");
                        sendMessage("SPAWNREMOTE:" + id);
                     }
                 } catch (Exception e) {
                     Gdx.app.error("Client", "Failed to announce SPAWNREMOTE after CONNECTED", e);
                }
                if (id >= 0) {
                    pendingConnectNotify.add(id);
                }
                return;
            }
            if ("DISCONNECTED".equals(type)) {
                final int id = safeParseInt(payload.trim(), -1);
                if (this.screen != null)
                    Gdx.app.postRunnable(() -> {
                         try {
                             this.screen.removePlayer(id);
                         } catch (Exception e) {
                             Gdx.app.error("Client", "Failed to remove player on render thread", e);
                        }
                    });
                String nick = null;
                try { nick = knownNicks.get(id); } catch (Exception ignored) {}
                final String text = (nick != null && nick.length() > 0) ? (nick + " disconnected") : ("Player " + id + " disconnected");
                if (this.screen != null)
                    Gdx.app.postRunnable(() -> {
                        try {
                            this.screen.hud.hudNotifications.newMessage((String) null, text, Color.valueOf("e33e46"));
                        } catch (Exception exception) {}
                    });
                try { knownNicks.remove(id); } catch (Exception ignored) {}
                try { pendingConnectNotify.remove(id); } catch (Exception ignored) {}
                return;
            }
             //
            if ("APPEARANCE".equals(type)) {
                try {
                    if (MultiplayerNetworkHelper.handleAppearance(this.screen, "APPEARANCE:" + payload, -1)) {
                        try {
                            String[] parts = payload.split(":", 2);
                            if (parts.length >= 2) {
                                int pid = safeParseInt(parts[0], -1);
                                String data = parts[1];
                                String[] ap = data.split("\\|");
                                String nick = (ap.length > 2) ? java.net.URLDecoder.decode(ap[2], "UTF-8") : "";
                                if (pid >= 0) {
                                    try { knownNicks.put(pid, nick); } catch (Exception ignored) {}
                                    if (pendingConnectNotify.remove(pid) && pid != this.clientId) {
                                        final String text = (nick != null && nick.length() > 0) ? (nick + " connected") : ("Player " + pid + " connected");
                                        if (this.screen != null) {
                                            Gdx.app.postRunnable(() -> {
                                                try {
                                                    this.screen.hud.hudNotifications.newMessage((String) null, text, Color.valueOf("25addb"));
                                                } catch (Exception ignored) {}
                                            });
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                        return;
                    }
                } catch (Exception e) {
                    Gdx.app.error("Client", "Exception handling APPEARANCE message", e);
                }
            }
             if ("PING".equals(type)) {
                 try {
                     System.out.println("[Client] PING: " + payload.toUpperCase());
                 } catch (Exception exception) {
                 }
                 return;
             }
             // Обработка сообщений от других клиентов (формат: ID:сообщение)
            if ("SPAWNREMOTE".equals(type)) {
                String spawnPayload = "SPAWNREMOTE:" + (payload == null || payload.length() == 0 ? Integer.toString(srcId) : payload);
                if (MultiplayerNetworkHelper.handleSpawnRemote(screen, spawnPayload, srcId)) {
                    return;
                }
                final int id = safeParseInt(payload, Integer.MIN_VALUE);
                if (id != Integer.MIN_VALUE) {
                    pendingSpawns.add(id);
                }
                return;
            }

             if ("PING".equals(type)) {
                 try {
                     System.out.println("[Client] PING from " + srcId + ": " + payload.toUpperCase());
                 } catch (Exception exception) {
                 }
                 return;
             }
            if ("TECH_SYNC".equals(type)) {
                try {
                    applyTechSync(payload);
                } catch (Exception e) {
                    Gdx.app.error("Client", "Failed to apply TECH_SYNC", e);
                }
                return;
            }
            if ("PVP_DAMAGE".equals(type)) {
                try {
                    String[] parts = payload.split(":");
                    if (parts.length >= 2 && this.screen != null && this.screen.world != null && this.screen.world.player != null) {
                        int targetId = safeParseInt(parts[0], -1);
                        float dmg = safeParseFloat(parts[1], 0.0f);
                        int attackerId = (parts.length >= 3) ? safeParseInt(parts[2], -1) : -1;
                        String kind = (parts.length >= 4) ? parts[3] : "";
                        if (targetId == this.clientId || targetId == this.screen.world.player.ownerId) {
                            final float fdmg = dmg;
                            final int atk = attackerId;
                            final String k = kind;
                            Gdx.app.postRunnable(() -> {
                                try {
                                    this.screen.world.player.playerStatus.takeHitDamage(fdmg);
                                    String msg2 = (k != null && k.length() > 0) ? ("Hit by " + k) : "Hit!";
                                    if (atk >= 0) {
                                        String nick = knownNicks.get(atk);
                                        if (nick != null && nick.length() > 0) msg2 = nick + " hit you";
                                    }
                                    this.screen.hud.hudNotifications.newMessage((String)null, msg2, Color.valueOf("e33e46"));
                                } catch (Exception ignored) {}
                            });
                        }
                    }
                } catch (Exception ignored) {}
                return;
            }
             String frag = type + ":" + payload;
             if (frag != null && frag.length() != 0) {
                 try {
                         if ("CHAT".equals(type)) {
                             // 
                             if (!MultiplayerNetworkHelper.handleChat(this.screen, "CHAT:" + payload, srcId)) {
                                 Gdx.app.error("Client", "Failed to handle CHAT payload via helper", null);
                             }
                        } else if ("CHATRAW".equals(type)) {
                            if (!MultiplayerNetworkHelper.handleChatRaw(this.screen, "CHATRAW:" + payload, srcId)) {
                                Gdx.app.error("Client", "Failed to handle CHATRAW payload via helper", null);
                            }
                        } else if ("APPEARANCE".equals(type)) {
                            try {
                                MultiplayerNetworkHelper.handleAppearance(this.screen, "APPEARANCE:" + payload, srcId);
                            } catch (Exception ignored) {}
                        } else if ("TIMEWEATHER".equals(type)) {
                            try {
                                String rest = payload;
                                String[] parts = rest.split(":", 8);
                                if (parts.length >= 8 && this.screen != null) {
                                    final int day = safeParseInt(parts[0], 0);
                                    final String period = parts[1];
                                    final float periodTime = safeParseFloat(parts[2], 0.0f);
                                    final String dayMode = parts[3];
                                    final String weatherMode = parts[4];
                                    final String weatherId = parts[5];
                                    final float weatherTime = safeParseFloat(parts[6], 0.0f);
                                    final float weatherDur = safeParseFloat(parts[7], 0.0f);
                                    Gdx.app.postRunnable(() -> {
                                        try {
                                            if (this.screen.world != null) {
                                                try {
                                                    if (this.screen.world.dayCycle != null) {
                                                        try { this.screen.world.dayCycle.setDayCycleMode(com.cairn4.moonbase.Mission.dayCycleModes.valueOf(dayMode)); } catch (Exception ignored) {}
                                                        try {
                                                            if (this.screen.world.dayCycle.getDay() != day) {
                                                                this.screen.world.dayCycle.setDay(day);
                                                            }
                                                        } catch (Exception ignored) {}
                                                        try {
                                                            if (this.screen.world.dayCycle.currentPeriod == null || !this.screen.world.dayCycle.currentPeriod.toString().equals(period)) {
                                                                this.screen.world.dayCycle.setPeriod(period);
                                                            }
                                                        } catch (Exception ignored) {}
                                                        try {
                                                            if (Math.abs(this.screen.world.dayCycle.timer - periodTime) > 0.5f) {
                                                                this.screen.world.dayCycle.setTime(periodTime);
                                                            }
                                                        } catch (Exception ignored) {}
                                                    }
                                                    if (this.screen.world.weatherManager != null) {
                                                        try { this.screen.world.weatherManager.setMode(com.cairn4.moonbase.Mission.weatherModes.valueOf(weatherMode)); } catch (Exception ignored) {}
                                                        try {
                                                            String curId = this.screen.world.weatherManager.getCurrentData() != null ? this.screen.world.weatherManager.getCurrentData().id : "";
                                                            if (curId == null || !curId.equals(weatherId)) {
                                                                this.screen.world.weatherManager.setWeather(weatherId);
                                                            }
                                                        } catch (Exception ignored) {}
                                                        try {
                                                            if (Math.abs(this.screen.world.weatherManager.getTimer() - weatherTime) > 1.0f) {
                                                                this.screen.world.weatherManager.setTimer(weatherTime);
                                                            }
                                                        } catch (Exception ignored) {}
                                                        try {
                                                            if (Math.abs(this.screen.world.weatherManager.getDuration() - weatherDur) > 1.0f) {
                                                                this.screen.world.weatherManager.setDuration(weatherDur);
                                                            }
                                                        } catch (Exception ignored) {}
                                                    }
                                                } catch (Exception ignored) {}
                                            }
                                        } catch (Exception ignored) {}
                                    });
                                    return;
                                }
                            } catch (Exception ignored) {}
                        } else if ("VEH_OCCUPY".equals(type)) {
                            try {
                                if (!MultiplayerNetworkHelper.handleVehicleOccupy(this.screen, "VEH_OCCUPY:" + payload, srcId)) {
                                    Gdx.app.error("Client", "Failed to handle VEH_OCCUPY", null);
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Client", "Exception handling VEH_OCCUPY", e);
                            }
                        } else if ("VEH_SPAWN".equals(type)) {
                            try {
                                if (!MultiplayerNetworkHelper.handleVehicleSpawn(this.screen, "VEH_SPAWN:" + payload, srcId)) {
                                    Gdx.app.error("Client", "Failed to handle VEH_SPAWN", null);
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Client", "Exception handling VEH_SPAWN", e);
                            }
                        } else if ("ENTITY_SPAWN".equals(type)) {
                            try {
                                if (!MultiplayerNetworkHelper.handleEntitySpawn(this.screen, "ENTITY_SPAWN:" + payload, srcId)) {
                                    Gdx.app.error("Client", "Failed to handle ENTITY_SPAWN", null);
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Client", "Exception handling ENTITY_SPAWN", e);
                            }
                        } else if ("VEH_STATE".equals(type)) {
                            try {
                                if (!MultiplayerNetworkHelper.handleVehicleState(this.screen, "VEH_STATE:" + payload, srcId)) {
                                    Gdx.app.error("Client", "Failed to handle VEH_STATE", null);
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Client", "Exception handling VEH_STATE", e);
                            }
                        } else if ("VEH_META".equals(type)) {
                            try {
                                if (!MultiplayerNetworkHelper.handleVehicleMeta(this.screen, "VEH_META:" + payload, srcId)) {
                                    Gdx.app.error("Client", "Failed to handle VEH_META", null);
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Client", "Exception handling VEH_META", e);
                            }
                        } else if ("CREATURE_STATE".equals(type)) {
                            try {
                                if (!MultiplayerNetworkHelper.handleCreatureState(this.screen, "CREATURE_STATE:" + payload, srcId)) {
                                    Gdx.app.error("Client", "Failed to handle CREATURE_STATE", null);
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Client", "Exception handling CREATURE_STATE", e);
                            }
                        } else if ("VEH_INV_SYNC".equals(type)) {
                            try {
                                if (!MultiplayerNetworkHelper.handleVehicleInvSync(this.screen, "VEH_INV_SYNC:" + payload, srcId)) {
                                    Gdx.app.error("Client", "Failed to handle VEH_INV_SYNC", null);
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Client", "Exception handling VEH_INV_SYNC", e);
                            }
                        } else if ("VEH_LOCK".equals(type) || "VEH_UNLOCK".equals(type) || "VEH_LOCK_DENY".equals(type)) {
                            try {
                                if (!MultiplayerNetworkHelper.handleVehicleLock(this.screen, type + ":" + payload, srcId)) {
                                    Gdx.app.error("Client", "Failed to handle VEH_LOCK", null);
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Client", "Exception handling VEH_LOCK", e);
                            }
                        } else if ("BASE_INV_SYNC".equals(type)) {
                            try {
                                if (!MultiplayerNetworkHelper.handleBaseInvSync(this.screen, "BASE_INV_SYNC:" + payload, srcId)) {
                                    Gdx.app.error("Client", "Failed to handle BASE_INV_SYNC", null);
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Client", "Exception handling BASE_INV_SYNC", e);
                            }
                        } else if ("BASE_LOCK".equals(type) || "BASE_UNLOCK".equals(type) || "BASE_LOCK_DENY".equals(type)) {
                            try {
                                if (!MultiplayerNetworkHelper.handleBaseLock(this.screen, type + ":" + payload, srcId)) {
                                    Gdx.app.error("Client", "Failed to handle BASE_LOCK", null);
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Client", "Exception handling BASE_LOCK", e);
                            }
                        } else if ("GENERATOR_FUEL".equals(type)) {
                            try {
                                if (!MultiplayerNetworkHelper.handleGeneratorFuel(this.screen, "GENERATOR_FUEL:" + payload, srcId)) {
                                    Gdx.app.error("Client", "Failed to handle GENERATOR_FUEL", null);
                                }
                            } catch (Exception e) {
                                Gdx.app.error("Client", "Exception handling GENERATOR_FUEL", e);
                            }
                        } else if ("INVENTORY_UPDATE".equals(type)) {
                             try {
                                 String json = payload;
                                 Gdx.app.log("Client", "Received INVENTORY_UPDATE for " + srcId + ": " + json);
                                 // Save to local save file
                                 try {
                                     String saveDir = "saves/" + com.cairn4.moonbase.MoonBase.currentSaveFolder + "/";
                                     com.badlogic.gdx.files.FileHandle dir = com.badlogic.gdx.Gdx.files.local(saveDir);
                                     if (!dir.exists()) dir.mkdirs();
                                     com.badlogic.gdx.files.FileHandle fh = com.badlogic.gdx.Gdx.files.local(saveDir + "multiplayersdata_" + srcId + ".json");
                                     fh.writeString(json, false, "UTF-8");
                                 } catch (Exception e) { Gdx.app.error("Client", "Failed to save INVENTORY_UPDATE file", e); }

                                 // Apply to local player if this update is for our clientId
                                 if (this.clientId == srcId && this.screen != null && this.screen.world != null && this.screen.world.player != null) {
                                     final String payloadJson = json;
                                     com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                                         try {
                                             com.cairn4.moonbase.Player p = this.screen.world.player;
                                             // Parse simple JSON for fields: air, suitPower, hunger, health, flashlight, suitLevel, inventory
                                             try {
                                                 float air = parseFloatField(payloadJson, "air", p.playerStatus.getAir());
                                                 float suitPower = parseFloatField(payloadJson, "suitPower", p.playerStatus.getSuitPower());
                                                 float hunger = parseFloatField(payloadJson, "hunger", p.playerStatus.getHunger());
                                                 float health = parseFloatField(payloadJson, "health", p.playerStatus.getHealth());
                                                 boolean flashlight = parseBooleanField(payloadJson, "flashlight", p.playerStatus.getFlashlight());
                                                 int suitLevel = parseIntField(payloadJson, "suitLevel", p.suitLevel);
                                                 try { p.playerStatus.setAir(air); } catch (Exception ignored) {}
                                                 try { p.playerStatus.setSuitPower(suitPower); } catch (Exception ignored) {}
                                                 try { p.playerStatus.setHunger(hunger); } catch (Exception ignored) {}
                                                 try { p.playerStatus.setHealth(health); } catch (Exception ignored) {}
                                                 try { p.playerStatus.setFlashlight(flashlight); } catch (Exception ignored) {}
                                                 try { p.setSuitLevel(suitLevel, false); } catch (Exception ignored) {}
                                                 try {
                                                     float px = parseFloatField(payloadJson, "x", Float.NaN);
                                                     float py = parseFloatField(payloadJson, "y", Float.NaN);
                                                     if (!Float.isNaN(px) && !Float.isNaN(py)) {
                                                         try {
                                                             java.lang.reflect.Method mx = com.cairn4.moonbase.Player.class.getDeclaredMethod("setXPos", new Class[]{float.class});
                                                             java.lang.reflect.Method my = com.cairn4.moonbase.Player.class.getDeclaredMethod("setYPos", new Class[]{float.class});
                                                             mx.setAccessible(true);
                                                             my.setAccessible(true);
                                                             mx.invoke(p, new Object[]{Float.valueOf(px)});
                                                             my.invoke(p, new Object[]{Float.valueOf(py)});
                                                         } catch (Exception ignored) {}
                                                     }
                                                 } catch (Exception ignored) {}
                                             } catch (Exception ignored) {}
                                             // Inventory: clear and add
                                             try {
                                                 java.util.List<java.util.Map<String, String>> items = parseInventoryArray(payloadJson);
                                                 p.getPlayerInventory().itemList.clear();
                                                 for (java.util.Map<String, String> item : items) {
                                                     String id = item.get("id");
                                                     int amount = Integer.parseInt(item.getOrDefault("amount", "1"));
                                                     int durability = Integer.parseInt(item.getOrDefault("durability", "0"));
                                                     com.cairn4.moonbase.ItemStack stack = new com.cairn4.moonbase.ItemStack(id, amount);
                                                     try { stack.item.durability = durability; } catch (Exception ignored) {}
                                                     p.getPlayerInventory().itemList.add(stack);
                                                 }
                                                 p.inventoryUpdate();
                                             } catch (Exception ignored) {}
                                         } catch (Exception e) {
                                             Gdx.app.error("Client", "Failed applying INVENTORY_UPDATE", e);
                                         }
                                     });
                                 }
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Error parsing INVENTORY_UPDATE", e);
                             }
                        } else if ("TP".equals(type)) {
                            try {
                                String[] parts = payload.split(":", 3);
                                if (parts.length >= 2 && this.screen != null && this.screen.world != null && this.screen.world.player != null) {
                                    final int tx = safeParseInt(parts[0], Integer.MIN_VALUE);
                                    final int ty = safeParseInt(parts[1], Integer.MIN_VALUE);
                                    if (tx != Integer.MIN_VALUE && ty != Integer.MIN_VALUE) {
                                        Gdx.app.postRunnable(() -> {
                                            try {
                                                com.cairn4.moonbase.Player p = this.screen.world.player;
                                                if (p != null) {
                                                    p.exitVehicleRemote();
                                                    p.moveToTile(tx, ty);
                                                    p.forcePositionUpdate();
                                                    try {
                                                        float wx = tx * com.cairn4.moonbase.tiles.Tile.TILE_SIZE;
                                                        float wy = ty * com.cairn4.moonbase.tiles.Tile.TILE_SIZE;
                                                        String pos = "POS:PLAYER:" + p.ownerId + ":" + wx + ":" + wy + ":0:0";
                                                        com.cairn4.moonbase.NetworkHelper.sendPayload(this.screen, pos);
                                                    } catch (Exception ignored) {}
                                                }
                                            } catch (Exception ignored) {}
                                        });
                                    }
                                }
                            } catch (Exception ignored) {}
                        } else if ("XFER_GIVE".equals(type)) {
                            try {
                                String[] parts = payload.split(":", 3);
                                if (parts.length >= 2 && this.screen != null && this.screen.world != null && this.screen.world.player != null) {
                                    String item = java.net.URLDecoder.decode(parts[0], "UTF-8");
                                    int amt = safeParseInt(parts[1], 1);
                                    if (amt <= 0) amt = 1;
                                    final String fitem = item;
                                    final int famt = amt;
                                    Gdx.app.postRunnable(() -> {
                                        try {
                                            com.cairn4.moonbase.Player p = this.screen.world.player;
                                            if (p != null) {
                                                p.playerInventory.add(new com.cairn4.moonbase.ItemStack(fitem, famt), true);
                                                p.inventoryUpdate();
                                            }
                                        } catch (Exception ignored) {}
                                    });
                                }
                            } catch (Exception ignored) {}
                        } else if ("XFER_TAKE".equals(type)) {
                            try {
                                String[] parts = payload.split(":", 3);
                                if (parts.length >= 2 && this.screen != null && this.screen.world != null && this.screen.world.player != null) {
                                    String item = java.net.URLDecoder.decode(parts[0], "UTF-8");
                                    int amt = safeParseInt(parts[1], 1);
                                    if (amt <= 0) amt = 1;
                                    final String fitem = item;
                                    final int famt = amt;
                                    Gdx.app.postRunnable(() -> {
                                        try {
                                            com.cairn4.moonbase.Player p = this.screen.world.player;
                                            if (p != null) {
                                                p.playerInventory.removeAmountOfItemId(famt, fitem);
                                                p.inventoryUpdate();
                                            }
                                        } catch (Exception ignored) {}
                                    });
                                }
                            } catch (Exception ignored) {}
                        } else if ("ANIM".equals(type)) {
                         } else if ("ANIM".equals(type)) {
                             String rest = payload;
                             String[] parts = rest.split(":", 4);
                             final int wx = (parts.length > 1) ? safeParseInt(parts[1], Integer.MIN_VALUE) : Integer.MIN_VALUE;
                             final int wy = (parts.length > 2) ? safeParseInt(parts[2], Integer.MIN_VALUE) : Integer.MIN_VALUE;
                             if (this.screen != null && this.screen.world != null)
                                 Gdx.app.postRunnable(() -> {
                                     try {
                                         for (Entity e : this.screen.world.entityList) {
                                             try {
                                                 int ex = (int) e.getXPos();
                                                 int ey = (int) e.getYPos();
                                                 if (Math.abs(ex - wx) <= 1 && Math.abs(ey - wy) <= 1)
                                                     try {
                                                         Method m = e.getClass().getMethod("spawnAnim", new Class[0]);
                                                         m.invoke(e, new Object[0]);
                                                     } catch (NoSuchMethodException noSuchMethodException) {
                                                     }
                                             } catch (Exception exception) {
                                             }
                                         }
                                         try {
                                             Tile t = this.screen.world.getTile(wx, wy);
                                             if (t != null)
                                                 try {
                                                     Method gm = t.getClass().getMethod("getSpawnAnimation", new Class[0]);
                                                     Object act = gm.invoke(t, new Object[0]);
                                                     if (act instanceof Action)
                                                         try {
                                                             Field gf = t.getClass().getDeclaredField("group");
                                                             gf.setAccessible(true);
                                                             Object go = gf.get(t);
                                                             if (go instanceof Group)
                                                                 ((Group) go).addAction((Action) act);
                                                         } catch (Exception exception) {
                                                         }
                                                 } catch (NoSuchMethodException noSuchMethodException) {
                                                 }
                                         } catch (Exception exception) {
                                         }
                                     } catch (Exception e) {
                                         Gdx.app.error("Client", "Failed applying ANIM payload", e);
                                     }
                                 });
                         } else if (frag.startsWith("ANIMPLAY:")) {
                             try {
                                 String rest = frag.substring("ANIMPLAY:".length());
                                 String[] parts = rest.split(":", 5);
                                 if (parts.length >= 3 && "PLAYER".equals(parts[0])) {
                                     final int owner = safeParseInt(parts[1], Integer.MIN_VALUE);
                                     final String anim = parts[2];
                                     final boolean loop = (parts.length > 3) ? Boolean.parseBoolean(parts[3]) : true;
                                     final float scale = (parts.length > 4) ? safeParseFloat(parts[4], 1.0F) : 1.0F;
                                     if (this.screen != null)
                                         Gdx.app.postRunnable(() -> {
                                             try {
                                                 if (this.screen.world != null && this.screen.world.player != null && this.screen.world.player.ownerId == owner) {
                                                     return;
                                                 }
                                                Player remote = this.screen.getRemotePlayer(owner);
                                                if (remote == null) {
                                                    // cache flip so it applies when the remote player spawns
                                                    try { this.flipOverrides.put(Integer.valueOf(owner), Float.valueOf(scale)); } catch (Exception ignored) {}
                                                    return;
                                                }
                                                 int mode = this.screen.getAnimTestMode();
                                                 if (mode == 1) {
                                                     try {
                                                         Field pacField = Player.class.getDeclaredField("playerAnimController");
                                                         pacField.setAccessible(true);
                                                         Object pac = pacField.get(remote);
                                                         if (pac != null)
                                                             try {
                                                                 Method test = pac.getClass().getMethod("testAnim", new Class[]{String.class, boolean.class});
                                                                 test.invoke(pac, new Object[]{anim, Boolean.valueOf(loop)});
                                                             } catch (NoSuchMethodException noSuchMethodException) {
                                                             }
                                                     } catch (Exception exception) {
                                                     }
                                                     return;
                                                 }
                                                 if (mode == 2) {
                                                     try {
                                                         Field spineFieldForScale = Player.class.getDeclaredField("spineActor");
                                                         spineFieldForScale.setAccessible(true);
                                                         Object spineForScale = spineFieldForScale.get(remote);
                                                         if (spineForScale != null)
                                                             try {
                                                                 Field skeletonField = spineForScale.getClass().getDeclaredField("skeleton");
                                                                 skeletonField.setAccessible(true);
                                                                 Object skel = skeletonField.get(spineForScale);
                                                                 if (skel != null)
                                                                     try {
                                                                         Method setScaleX = skel.getClass().getMethod("setScaleX", new Class[]{float.class});
                                                                         setScaleX.invoke(skel, new Object[]{Float.valueOf(scale)});
                                                                     } catch (NoSuchMethodException noSuchMethodException) {
                                                                     }
                                                             } catch (Exception exception) {
                                                             }
                                                     } catch (Exception exception) {
                                                     }
                                                     return;
                                                 }
                                                 try {
                                                     Field pacField = Player.class.getDeclaredField("playerAnimController");
                                                     pacField.setAccessible(true);
                                                     Object pac = pacField.get(remote);
                                                     if (pac != null)
                                                         try {
                                                             Method test = pac.getClass().getMethod("testAnim", new Class[]{String.class, boolean.class});
                                                             test.invoke(pac, new Object[]{anim, Boolean.valueOf(loop)});
                                                         } catch (NoSuchMethodException noSuchMethodException) {
                                                         }
                                                 } catch (Exception exception) {
                                                 }
                                                 try {
                                                     Field spineField = Player.class.getDeclaredField("spineActor");
                                                     spineField.setAccessible(true);
                                                     Object spine = spineField.get(remote);
                                                     if (spine != null)
                                                         try {
                                                             Field stateField = spine.getClass().getDeclaredField("state");
                                                             stateField.setAccessible(true);
                                                             Object state = stateField.get(spine);
                                                             if (state != null) {
                                                                 Method setAnim = state.getClass().getMethod("setAnimation", new Class[]{int.class, String.class, boolean.class});
                                                                 setAnim.invoke(state, new Object[]{Integer.valueOf(0), anim, Boolean.valueOf(loop)});
                                                             }
                                                         } catch (Exception exception) {
                                                         }
                                                 } catch (Exception exception) {
                                                 }
                                             } catch (Exception e) {
                                                 Gdx.app.error("Client", "Failed applying ANIMPLAY", e);
                                             }
                                         });
                                 }
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Error parsing ANIMPLAY", e);
                             }
                         } else if (frag.startsWith("FLIP:")) {
                             try {
                                 String rest = frag.substring("FLIP:".length());
                                 String[] parts = rest.split(":", 4);
                                 if (parts.length >= 3 && "PLAYER".equals(parts[0])) {
                                     final int owner = safeParseInt(parts[1], Integer.MIN_VALUE);
                                     final float scale = (parts.length > 2) ? safeParseFloat(parts[2], 1.0F) : 1.0F;
                                     if (this.screen != null)
                                         Gdx.app.postRunnable(() -> {
                                             try {
                                                 if (this.screen.world != null && this.screen.world.player != null && this.screen.world.player.ownerId == owner) {
                                                     return;
                                                 }
                                                 Player remote = this.screen.getRemotePlayer(owner);
                                                 if (remote == null) {
                                                     return;
                                                 }
                                                 Float fo = this.flipOverrides.get(Integer.valueOf(owner));
                                                 final float useScale = (fo != null) ? fo.floatValue() : scale;
                                                 boolean applied = false;
                                                 try {
                                                     Field pacField = Player.class.getDeclaredField("playerAnimController");
                                                     pacField.setAccessible(true);
                                                     Object pac = pacField.get(remote);
                                                     if (pac != null)
                                                         try {
                                                             Method setForced = pac.getClass().getMethod("setForcedScale", new Class[]{float.class});
                                                             setForced.invoke(pac, new Object[]{Float.valueOf(useScale)});
                                                             applied = true;
                                                         } catch (NoSuchMethodException noSuchMethodException) {
                                                         }
                                                 } catch (Exception exception) {
                                                 }
                                                 if (!applied)
                                                     try {
                                                         Field spineField = Player.class.getDeclaredField("spineActor");
                                                         spineField.setAccessible(true);
                                                         Object spine = spineField.get(remote);
                                                         if (spine != null)
                                                             try {
                                                                 Field skF = spine.getClass().getDeclaredField("skeleton");
                                                                 skF.setAccessible(true);
                                                                 Object sk = skF.get(spine);
                                                                 if (sk != null)
                                                                     try {
                                                                         Method setScaleX = sk.getClass().getMethod("setScaleX", new Class[]{float.class});
                                                                         setScaleX.invoke(sk, new Object[]{Float.valueOf(useScale)});
                                                                     } catch (NoSuchMethodException noSuchMethodException) {
                                                                     }
                                                             } catch (Exception exception) {
                                                             }
                                                     } catch (Exception exception) {
                                                     }
                                             } catch (Exception e) {
                                                 Gdx.app.error("Client", "Failed applying FLIP", e);
                                             }
                                         });
                                 }
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Error parsing FLIP", e);
                             }
                         } else if (frag.startsWith("FLIP_OVERRIDE:")) {
                             try {
                                 String rest = frag.substring("FLIP_OVERRIDE:".length());
                                 String[] parts = rest.split(":", 4);
                                 if (parts.length >= 3 && "PLAYER".equals(parts[0])) {
                                     final int owner = safeParseInt(parts[1], Integer.MIN_VALUE);
                                     final float scale = (parts.length > 2) ? safeParseFloat(parts[2], 1.0F) : 1.0F;
                                     this.flipOverrides.put(Integer.valueOf(owner), Float.valueOf(scale));
                                     if (this.screen != null)
                                         Gdx.app.postRunnable(() -> {
                                             try {
                                                 if (this.screen.world != null && this.screen.world.player != null && this.screen.world.player.ownerId == owner) {
                                                     return;
                                                 }
                                                 Player remote = this.screen.getRemotePlayer(owner);
                                                 if (remote == null) {
                                                     return;
                                                 }
                                                 boolean applied = false;
                                                 try {
                                                     Field pacField = Player.class.getDeclaredField("playerAnimController");
                                                     pacField.setAccessible(true);
                                                     Object pac = pacField.get(remote);
                                                     if (pac != null) {
                                                         try {
                                                             Method setForced = pac.getClass().getMethod("setForcedScale", new Class[]{float.class});
                                                             setForced.invoke(pac, new Object[]{Float.valueOf(scale)});
                                                             applied = true;
                                                         } catch (NoSuchMethodException noSuchMethodException) {
                                                         }
                                                         if (scale < 0.0F)
                                                             try {
                                                                 Method setDir = pac.getClass().getMethod("setDirection", new Class[]{float.class, float.class});
                                                                 setDir.invoke(pac, new Object[]{Float.valueOf(-1.0F), Float.valueOf(0.0F)});
                                                             } catch (NoSuchMethodException nsme) {
                                                                 try {
                                                                     Method upd = pac.getClass().getMethod("updateWalkDirection", new Class[]{float.class, float.class});
                                                                     upd.invoke(pac, new Object[]{Float.valueOf(-1.0F), Float.valueOf(0.0F)});
                                                                 } catch (NoSuchMethodException noSuchMethodException) {
                                                                 }
                                                             }
                                                     }
                                                 } catch (Exception exception) {
                                                 }
                                                 if (!applied)
                                                     try {
                                                         Field spineField = Player.class.getDeclaredField("spineActor");
                                                         spineField.setAccessible(true);
                                                         Object spine = spineField.get(remote);
                                                         if (spine != null)
                                                             try {
                                                                 Field skF = spine.getClass().getDeclaredField("skeleton");
                                                                 skF.setAccessible(true);
                                                                 Object sk = skF.get(spine);
                                                                 if (sk != null)
                                                                     try {
                                                                         Method setScaleX = sk.getClass().getMethod("setScaleX", new Class[]{float.class});
                                                                         setScaleX.invoke(sk, new Object[]{Float.valueOf(scale)});
                                                                     } catch (NoSuchMethodException noSuchMethodException) {
                                                                     }
                                                             } catch (Exception exception) {
                                                             }
                                                     } catch (Exception exception) {
                                                     }
                                             } catch (Exception e) {
                                                 Gdx.app.error("Client", "Failed applying FLIP_OVERRIDE", e);
                                             }
                                         });
                                 }
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Error parsing FLIP_OVERRIDE", e);
                             }
                         } else if (frag.startsWith("FLIP_OVERRIDE_CLEAR:")) {
                             try {
                                 String rest = frag.substring("FLIP_OVERRIDE_CLEAR:".length());
                                 String[] parts = rest.split(":", 3);
                                 if (parts.length >= 2 && "PLAYER".equals(parts[0])) {
                                     final int owner = safeParseInt(parts[1], Integer.MIN_VALUE);
                                     this.flipOverrides.remove(Integer.valueOf(owner));
                                     if (this.screen != null)
                                         Gdx.app.postRunnable(() -> {
                                             try {
                                                 if (this.screen.world != null && this.screen.world.player != null && this.screen.world.player.ownerId == owner) {
                                                     return;
                                                 }
                                                 Player remote = this.screen.getRemotePlayer(owner);
                                                 if (remote == null) {
                                                     return;
                                                 }
                                                 try {
                                                     Field pacField = Player.class.getDeclaredField("playerAnimController");
                                                     pacField.setAccessible(true);
                                                     Object pac = pacField.get(remote);
                                                     if (pac != null)
                                                         try {
                                                             Method setForced = pac.getClass().getMethod("setForcedScale", new Class[]{float.class});
                                                             setForced.invoke(pac, new Object[]{Float.valueOf(1.0F)});
                                                         } catch (NoSuchMethodException noSuchMethodException) {
                                                         }
                                                 } catch (Exception exception) {
                                                 }
                                                 try {
                                                     Field spineField = Player.class.getDeclaredField("spineActor");
                                                     spineField.setAccessible(true);
                                                     Object spine = spineField.get(remote);
                                                     if (spine != null)
                                                         try {
                                                             Field skF = spine.getClass().getDeclaredField("skeleton");
                                                             skF.setAccessible(true);
                                                             Object sk = skF.get(spine);
                                                             if (sk != null)
                                                                 try {
                                                                     Method setScaleX = sk.getClass().getMethod("setScaleX", new Class[]{float.class});
                                                                     setScaleX.invoke(sk, new Object[]{Float.valueOf(1.0F)});
                                                                 } catch (NoSuchMethodException noSuchMethodException) {
                                                                 }
                                                         } catch (Exception exception) {
                                                         }
                                                 } catch (Exception exception) {
                                                 }
                                             } catch (Exception e) {
                                                 Gdx.app.error("Client", "Failed applying FLIP_OVERRIDE_CLEAR", e);
                                             }
                                         });
                                 }
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Error parsing FLIP_OVERRIDE_CLEAR", e);
                             }
                         } else if (frag.startsWith("ANIMTEST:")) {
                             try {
                                 String rest = frag.substring("ANIMTEST:".length());
                                 String[] parts = rest.split(":", 3);
                                 if (parts.length >= 2 && "PLAYER".equals(parts[0])) {
                                     final int owner = safeParseInt(parts[1], Integer.MIN_VALUE);
                                     if (this.screen != null)
                                         Gdx.app.postRunnable(() -> {
                                             try {
                                                 this.screen.setAnimTestMode(owner);
                                             } catch (Exception exception) {
                                             }
                                         });
                                 }
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Error parsing ANIMTEST", e);
                             }
                         } else if ("REQUEST_APPEARANCE".equals(type)) {
                             try {
                                 final int id = safeParseInt(payload, Integer.MIN_VALUE);
                                 if (id != Integer.MIN_VALUE && this.screen != null)
                                     Gdx.app.postRunnable(() -> {
                                         try {
                                             if (this.screen.world != null && this.screen.world.player != null && this.screen.world.player.ownerId == id) {
        int face = 0; String color = ""; String nick = "";
        try { if (screen != null && screen.world != null && screen.world.player != null) face = screen.world.player.characterFaceOption; } catch (Exception ignored) {}
        try { if (screen != null && screen.world != null && screen.world.player != null) color = screen.world.player.characterSuitColor != null ? screen.world.player.characterSuitColor : ""; } catch (Exception ignored) {}
        try { nick = com.cairn4.moonbase.MoonBase.multiplayerNick != null ? com.cairn4.moonbase.MoonBase.multiplayerNick : ""; } catch (Exception ignored) {}
        String payloadAppearance = "APPEARANCE:" + face + "|" + java.net.URLEncoder.encode(color == null ? "" : color, "UTF-8") + "|" + java.net.URLEncoder.encode(nick == null ? "" : nick, "UTF-8");
        NetworkHelper.sendPayload(screen, payloadAppearance);
    }
                                         } catch (Exception exception) {
                                         }
                                     });
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Error parsing REQUEST_APPEARANCE", e);
                             }
                         } else if ("POS".equals(type)) {
                             // Use unified handler for POS
                             try {
                                try {
                                    long now = System.currentTimeMillis();
                                    if ((now % 1000L) < 50L) {
                                        Gdx.app.log("Client", "[POS:recv] " + frag);
                                    }
                                } catch (Exception ignored) {}
                                 if (!MultiplayerNetworkHelper.handlePosition(this.screen, "POS:" + payload, srcId)) {
                                     Gdx.app.error("Client", "Error parsing POS via helper", null);
                                 }
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Exception while handling POS message", e);
                             }
                         } else if ("TILE_BUILD_START".equals(type)) {
                             // Use the unified handler for TILE_BUILD_START
                             try {
                                 if (!MultiplayerNetworkHelper.handleTileBuildStart(this.screen, "TILE_BUILD_START:" + payload, srcId)) {
                                     Gdx.app.error("Client", "Error handling TILE_BUILD_START via helper", null);
                                 }
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Exception while handling TILE_BUILD_START", e);
                             }
                         } else if (type.startsWith("TILE_") || type.startsWith("ITEM_")) {
                             try {
                                 if (this.screen != null && this.screen.world != null) {
                                     final String f = type + ":" + payload;
                                     Gdx.app.postRunnable(() -> {
                                         try {
                                             try {
                                                 Method m = this.screen.world.getClass().getMethod("handleIncomingNetworkEvent", new Class[]{String.class});
                                                 m.invoke(this.screen.world, new Object[]{f});
                                             } catch (NoSuchMethodException noSuchMethodException) {
                                             }
                                         } catch (Exception e) {
                                             Gdx.app.error("Client", "Failed reinjecting tile/item event", e);
                                         }
                                     });
                                 }
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Error parsing TILE_/ITEM_ event", e);
                             }
                         } else {
                             appendDebugLog("Unhandled payload: " + frag);
                         }
                 } catch (Exception e) {
                     Gdx.app.error("Client", "Error handling payload: " + frag, e);
                 }
             }
         } catch (Exception e) {
             Gdx.app.error("Client", "Error handling message: " + msg, e);
         }
     }

    private int safeParseInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private void flushPendingSpawns() {
        if (this.screen == null || pendingSpawns.isEmpty()) {
            return;
        }
        java.util.Iterator<Integer> it = pendingSpawns.iterator();
        while (it.hasNext()) {
            int id = it.next();
            if (id == this.clientId) {
                it.remove();
                continue;
            }
            if (MultiplayerNetworkHelper.handleSpawnRemote(this.screen, "SPAWNREMOTE:" + id, id)) {
                it.remove();
            }
        }
    }

    private void flushPendingTechSync() {
        try {
            if (pendingTechSyncPayload == null) return;
            if (this.screen == null || this.screen.world == null || this.screen.world.techManager == null) return;
            String payload = pendingTechSyncPayload;
            pendingTechSyncPayload = null;
            applyTechSync(payload);
        } catch (Exception ignored) {}
    }

    private void applyTechSync(String payload) {
        if (payload == null) return;
        if (this.screen == null || this.screen.world == null || this.screen.world.techManager == null) {
            pendingTechSyncPayload = payload;
            return;
        }
        final String payloadFinal = payload;
        com.badlogic.gdx.Gdx.app.postRunnable(() -> {
            try {
                if (this.screen == null || this.screen.world == null || this.screen.world.techManager == null) {
                    pendingTechSyncPayload = payloadFinal;
                    return;
                }
                int idx = payloadFinal.indexOf(':');
                if (idx <= 0) return;
                int samples = safeParseInt(payloadFinal.substring(0, idx), 0);
                String csvEnc = payloadFinal.substring(idx + 1);
                String csv = "";
                try { csv = java.net.URLDecoder.decode(csvEnc, "UTF-8"); } catch (Exception ignored) {}
                java.util.HashSet<String> unlocked = new java.util.HashSet<String>();
                if (csv != null && csv.length() > 0) {
                    String[] parts = csv.split(",");
                    for (String p : parts) {
                        String t = p.trim();
                        if (t.length() > 0) unlocked.add(t);
                    }
                }
                com.cairn4.moonbase.techtree.TechManager tm = this.screen.world.techManager;
                if (tm.techTree != null && tm.techTree.upgrades != null) {
                    for (com.cairn4.moonbase.techtree.TechUpgrade tu : tm.techTree.upgrades) {
                        if (tu == null || tu.id == null) continue;
                        tu.unlocked = unlocked.contains(tu.id);
                    }
                }
                tm.setSamples(samples);
                tm.notifyHud();
            } catch (Exception e) {
                Gdx.app.error("Client", "Failed to apply tech sync on render thread", e);
            }
        });
    }

    private float safeParseFloat(String s, float def) {
        try {
            return Float.parseFloat(s.trim());
        } catch (Exception e) {
            return def;
        }
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

    private static class ParsedReliable {
        final int seq;
        final String payload;
        ParsedReliable(int seq, String payload) { this.seq = seq; this.payload = payload; }
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
            if (frame != null) sendFrame(frame);
        } catch (Exception ignored) {}
    }

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

     private void appendDebugLog(String line) {
         try {
             if (this.diagnosticMode && this.screen != null && this.screen.game != null && this.screen.game.console != null)
                 Gdx.app.postRunnable(() -> {
                     try {
                         this.screen.game.console.log(line);
                     } catch (Exception exception) {
                     }
                 });
         } catch (Exception exception) {
         }
     }

    private void sendFrame(String frame) {
        synchronized (this.outLock) {
            if (this.out == null) {
                return;
            }
            try {
                this.out.writeUTF(frame);
                this.out.flush();
                appendDebugLog("SENT: " + frame);
            } catch (Exception e) {
                Gdx.app.error("Client", "Failed to send payload", e);
            }
        }
    }

    public void send(String type, String payload) {
        if (type == null) return;
        if ("ACK".equals(type)) {
            String frame = ProtocolV2.encode(this.clientId, type, payload);
            if (frame != null) sendFrame(frame);
            return;
        }
        if (isReliableType(type)) {
            int seq = reliableSeq.getAndIncrement();
            if (seq <= 0) seq = reliableSeq.getAndIncrement();
            String relPayload = "SEQ:" + seq + ":" + (payload == null ? "" : payload);
            String frame = ProtocolV2.encode(this.clientId, type, relPayload);
            if (frame != null) {
                pendingReliable.put(seq, new PendingReliable(frame, System.currentTimeMillis()));
                sendFrame(frame);
            }
            return;
        }
        String frame = ProtocolV2.encode(this.clientId, type, payload);
        if (frame != null) {
            sendFrame(frame);
        }
    }

    // Legacy-compatible send (payload formatted as TYPE:...); wraps into MB2 frame.
    public void sendMessage(String payload) {
        if (payload == null) return;
        if (payload.startsWith(ProtocolV2.PREFIX + "|")) {
            sendFrame(payload);
            return;
        }
        String[] tp = splitTypePayload(payload);
        if (tp == null) return;
        send(tp[0], tp[1]);
    }

     private void startSender() {
         if (this.senderThread != null && this.senderThread.isAlive()) {
             return;
         }
            this.senderThread = new Thread(() -> {
                while (this.running) {
                    try {
                        long nowSweep = System.currentTimeMillis();
                        if (nowSweep - this.lastReliableSweep > 200L) {
                            this.lastReliableSweep = nowSweep;
                            for (java.util.Map.Entry<Integer, PendingReliable> ent : this.pendingReliable.entrySet()) {
                                PendingReliable pr = ent.getValue();
                                if (pr == null) continue;
                                if (nowSweep - pr.lastSent >= RELIABLE_RESEND_MS) {
                                    if (pr.attempts >= RELIABLE_MAX_ATTEMPTS) {
                                        this.pendingReliable.remove(ent.getKey());
                                    } else {
                                        pr.attempts++;
                                        pr.lastSent = nowSweep;
                                        sendFrame(pr.frame);
                                    }
                                }
                            }
                        }
                        if (this.sendEnabled && this.screen != null && this.screen.world != null && this.screen.world.player != null) {
                            float x = this.screen.world.player.getXPos();
                            float y = this.screen.world.player.getYPos();
                            long now = System.currentTimeMillis();
                            if (Float.isNaN(this.lastSentX) || Float.isNaN(this.lastSentY) || now - this.lastSentMillis >= 50L) {
                             this.lastSentX = x;
                             this.lastSentY = y;
                             this.lastSentMillis = now;
                             float vx = 0f, vy = 0f;
                             try { vx = this.screen.world.player.getLastReportedVelocity().x; } catch (Exception ignored) {}
                             try { vy = this.screen.world.player.getLastReportedVelocity().y; } catch (Exception ignored) {}
                             String payload = "PLAYER:" + this.clientId + ":" + x + ":" + y + ":" + vx + ":" + vy;
                             try {
                                 if ((now / 1000L) != ((this.lastSentMillis - 50L) / 1000L)) {
                                     Gdx.app.log("Client", "[POS:send] x=" + x + " y=" + y + " vx=" + vx + " vy=" + vy);
                                 }
                             } catch (Exception ignored) {}
                                send("POS", payload);
                            }

                            // Vehicle state sync when driving (driver only)
                            try {
                                if (this.screen.world.player.isDrivingVehicle()) {
                                    com.cairn4.moonbase.entities.Vehicle v = this.screen.world.player.getVehicle();
                                    if (v != null && v.isDriver(this.screen.world.player.ownerId)) {
                                        if (now - this.lastVehSentMillis >= 50L) {
                                            this.lastVehSentMillis = now;
                                            float vx = 0f, vy = 0f, rot = 0f;
                                            try { rot = v.getRotation(); } catch (Exception ignored) {}
                                            try {
                                                if (v.body != null) {
                                                    vx = v.body.getLinearVelocity().x * 256.0f;
                                                    vy = v.body.getLinearVelocity().y * 256.0f;
                                                }
                                            } catch (Exception ignored) {}
                                            String vp = v.id + ":" + v.getXPos() + ":" + v.getYPos() + ":" + rot + ":" + vx + ":" + vy;
                                            send("VEH_STATE", vp);
                                        }
                                    }
                                    // Vehicle meta/state sync (damage, power, abilities, etc.)
                                    if (now - this.lastVehMetaSentMillis >= 200L) {
                                        String meta = MultiplayerNetworkHelper.buildVehicleMeta(v);
                                        if (meta != null && (!meta.equals(this.lastVehMeta) || now - this.lastVehMetaSentMillis >= 1000L)) {
                                            this.lastVehMeta = meta;
                                            this.lastVehMetaSentMillis = now;
                                            if (meta.startsWith("VEH_META:")) meta = meta.substring("VEH_META:".length());
                                            send("VEH_META", meta);
                                        } else {
                                            this.lastVehMetaSentMillis = now;
                                        }
                                    }
                                }
                            } catch (Exception ignored) {}

                            // Periodic player state snapshot for persistence
                            try {
                                if (now - this.lastPlayerStateSentMillis >= 2000L) {
                                    String stateJson = buildPlayerStateJson();
                                    if (stateJson != null && stateJson.length() > 0) {
                                        try {
                                            String enc = java.net.URLEncoder.encode(stateJson, "UTF-8");
                                            send("PLAYER_STATE", enc);
                                        } catch (Exception ignored) {}
                                    }
                                    this.lastPlayerStateSentMillis = now;
                                }
                            } catch (Exception ignored) {}
                        }
                     try {
                         Thread.sleep(50L);
                     } catch (InterruptedException interruptedException) {
                     }
                 } catch (Exception e) {
                     Gdx.app.error("Client", "Sender thread error", e);
                     try {
                         Thread.sleep(50L);
                     } catch (InterruptedException interruptedException) {
                     }
                 }
             }
         }, "MewnBase-Client-Sender");
         this.senderThread.setDaemon(true);
         this.senderThread.start();
     }

    public void disconnect() {
        this.running = false;
        try { this.pendingReliable.clear(); } catch (Exception ignored) {}
        try {
            if (this.socket != null)
                this.socket.close();
         } catch (Exception exception) {
         }
         try {
             if (this.in != null)
                 this.in.close();
         } catch (Exception exception) {
         }
         try {
             if (this.out != null)
                 this.out.close();
        } catch (Exception exception) {
        }
    }

    private void sendSelfAppearanceAndSpawn() {
        try {
            if (this.clientId <= 0) return;
            if (this.screen == null || this.screen.world == null || this.screen.world.player == null) return;
            int face = 0; String color = ""; String nick = "";
            try { face = this.screen.world.player.characterFaceOption; } catch (Exception ignored) {}
            try { color = this.screen.world.player.characterSuitColor != null ? this.screen.world.player.characterSuitColor : ""; } catch (Exception ignored) {}
            try { nick = com.cairn4.moonbase.MoonBase.multiplayerNick != null ? com.cairn4.moonbase.MoonBase.multiplayerNick : ""; } catch (Exception ignored) {}
            String payloadAppearance = "APPEARANCE:" + face + "|" + java.net.URLEncoder.encode(color == null ? "" : color, "UTF-8") + "|" + java.net.URLEncoder.encode(nick == null ? "" : nick, "UTF-8");
            sendMessage(payloadAppearance);
            sendMessage("SPAWNREMOTE:" + this.clientId);
        } catch (Exception ignored) {}
    }

     public void pauseSend() {
         this.sendEnabled = false;
     }

     public void resumeSend() {
         this.sendEnabled = true;
     }

     public void setDiagnosticMode(boolean v) {
         this.diagnosticMode = v;
     }

     private static float parseFloatField(String json, String key, float def) {
         try {
             int idx = json.indexOf('"' + key + '"');
             if (idx < 0) return def;
             int colon = json.indexOf(':', idx);
             int comma = json.indexOf(',', colon);
             int end = comma < 0 ? json.indexOf('}', colon) : comma;
             String num = json.substring(colon + 1, end).trim();
             return Float.parseFloat(num);
         } catch (Exception e) { return def; }
     }

     private static boolean parseBooleanField(String json, String key, boolean def) {
         try {
             int idx = json.indexOf('"' + key + '"');
             if (idx < 0) return def;
             int colon = json.indexOf(':', idx);
             int comma = json.indexOf(',', colon);
             int end = comma < 0 ? json.indexOf('}', colon) : comma;
             String v = json.substring(colon + 1, end).trim();
             return Boolean.parseBoolean(v);
         } catch (Exception e) { return def; }
     }

    private static int parseIntField(String json, String key, int def) {
        try { return (int)parseFloatField(json, key, def); } catch (Exception e) { return def; }
    }

    private static java.util.List<java.util.Map<String, String>> parseInventoryArray(String json) {
        java.util.List<java.util.Map<String, String>> res = new java.util.ArrayList<>();
         try {
             int idx = json.indexOf("\"inventory\"");
             if (idx < 0) return res;
             int start = json.indexOf('[', idx);
             int end = json.indexOf(']', start);
             if (start < 0 || end < 0) return res;
             String body = json.substring(start + 1, end);
             String[] items = body.split("\\},");
             for (String it : items) {
                 String item = it.replace("{", "").replace("}", "").trim();
                 java.util.Map<String, String> map = new java.util.HashMap<>();
                 if (item.length() == 0) continue;
                 String[] parts = item.split(",");
                 for (String p : parts) {
                     int c = p.indexOf(':');
                     if (c < 0) continue;
                     String k = p.substring(0, c).replaceAll("[\" ]", "");
                     String v = p.substring(c + 1).replaceAll("[\"]", "").trim();
                     map.put(k, v);
                 }
                 if (!map.isEmpty()) res.add(map);
             }
         } catch (Exception ignored) {}
        return res;
    }

    private static String[] splitTypePayload(String message) {
        try {
            String legacy = message;
            int firstColon = legacy.indexOf(':');
            if (firstColon > 0) {
                try {
                    int id = Integer.parseInt(legacy.substring(0, firstColon));
                    String rest = legacy.substring(firstColon + 1);
                    if (rest.indexOf(':') > 0) {
                        legacy = rest;
                    }
                } catch (Exception ignored) {}
            }
            int idx = legacy.indexOf(':');
            if (idx < 0) return null;
            String type = legacy.substring(0, idx);
            String payload = legacy.substring(idx + 1);
            return new String[]{type, payload};
        } catch (Exception e) {
            return null;
        }
    }

    private String buildPlayerStateJson() {
        try {
            if (this.screen == null || this.screen.world == null || this.screen.world.player == null) return null;
            com.cairn4.moonbase.Player p = this.screen.world.player;
            String nick = com.cairn4.moonbase.MoonBase.multiplayerNick != null ? com.cairn4.moonbase.MoonBase.multiplayerNick : "";
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"nick\":\"").append(escapeJson(nick)).append("\",");
            sb.append("\"ownerId\":").append(this.clientId).append(",");
            try { sb.append("\"x\":").append(p.getXPos()).append(","); } catch (Exception ignored) {}
            try { sb.append("\"y\":").append(p.getYPos()).append(","); } catch (Exception ignored) {}
            try { sb.append("\"air\":").append(p.playerStatus.getAir()).append(","); } catch (Exception ignored) {}
            try { sb.append("\"suitPower\":").append(p.playerStatus.getSuitPower()).append(","); } catch (Exception ignored) {}
            try { sb.append("\"hunger\":").append(p.playerStatus.getHunger()).append(","); } catch (Exception ignored) {}
            try { sb.append("\"health\":").append(p.playerStatus.getHealth()).append(","); } catch (Exception ignored) {}
            try { sb.append("\"flashlight\":").append(p.playerStatus.getFlashlight()).append(","); } catch (Exception ignored) {}
            try { sb.append("\"suitLevel\":").append(p.suitLevel).append(","); } catch (Exception ignored) {}
            sb.append("\"inventory\":[");
            boolean first = true;
            try {
                for (com.cairn4.moonbase.ItemStack stack : p.getPlayerInventory().itemList) {
                    if (stack == null) continue;
                    if (!first) sb.append(",");
                    first = false;
                    String id = stack.getId();
                    int amt = stack.getAmount();
                    int dur = 0;
                    try { dur = stack.item != null ? stack.item.durability : 0; } catch (Exception ignored) {}
                    sb.append("{\"id\":\"").append(escapeJson(id)).append("\",\"amount\":").append(amt).append(",\"durability\":").append(dur).append("}");
                }
            } catch (Exception ignored) {}
            sb.append("]}");
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
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

 }
