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
     private int clientId = -1;
     private Socket socket;
     private DataInputStream in;
     private DataOutputStream out;
     private final Object outLock = new Object();
     private Thread senderThread;
     private Thread readerThread;
     private volatile boolean running = false;
     private boolean sendEnabled = true;
     private float lastSentX = Float.NaN;
     private float lastSentY = Float.NaN;
     private long lastSentMillis = 0L;
     private boolean diagnosticMode = false;
     private String host;
     private int port;

     public Client(String host, int port, GameScreen screen) {
         this.host = host;
         this.port = port;
         this.screen = screen;
     }

     public void connect() {
         if (this.running) {
             return;
         }
         this.running = true;
         new Thread(() -> {
             try {
                 this.socket = new Socket();
                 this.socket.connect(new InetSocketAddress(this.host, this.port), 5000);
                 this.socket.setTcpNoDelay(true);
                 this.in = new DataInputStream(this.socket.getInputStream());
                 this.out = new DataOutputStream(this.socket.getOutputStream());
                 try {
                     try {
                         this.clientId = this.in.readInt();
                         Gdx.app.log("Client", "Assigned clientId from server: " + this.clientId);
                         try { System.out.println("[Client] connect: received clientId=" + this.clientId); } catch (Exception ignored) {}
                         // Request initial appearance from server for all known players including host
                         NetworkHelper.sendPayload(this.screen, "0:REQUEST_APPEARANCE");
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
                     try {
                         int gameSaveLen = this.in.readInt();
                         if (gameSaveLen > 0) {
                             // Ensure multiplayer_received directory exists
                             String saveDir = "saves/multiplayer_received/";
                             com.badlogic.gdx.Gdx.files.local(saveDir).deleteDirectory();
                             com.badlogic.gdx.Gdx.files.local(saveDir).mkdirs();
                             
                             byte[] gameSaveBytes = new byte[gameSaveLen];
                             this.in.readFully(gameSaveBytes);
                             
                             com.badlogic.gdx.files.FileHandle receivedGameSave = com.badlogic.gdx.Gdx.files.local(saveDir + "gameSave.json");
                             receivedGameSave.writeBytes(gameSaveBytes, false);
                             Gdx.app.log("Client", "gameSave.json received (" + gameSaveLen + " bytes).");
                             try { System.out.println("[Client] connect: wrote gameSave.json, len=" + gameSaveLen); } catch (Exception ignored) {}
                             
                             // Set current save folder to multiplayer_received for loading
                             try { com.cairn4.moonbase.MoonBase.currentSaveFolder = "multiplayer_received"; } catch (Exception ignored) {}
                             try { Gdx.app.log("Client", "Set currentSaveFolder to: " + com.cairn4.moonbase.MoonBase.currentSaveFolder); } catch (Exception ignored) {}
                         }
                     } catch (Exception e) {
                         Gdx.app.error("Client", "Failed reading initial gameSave blob (continuing)", e);
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
                             try { com.cairn4.moonbase.MoonBase.currentSaveFolder = "multiplayer_received"; } catch (Exception ignored) {}
                         }
                     } catch (Exception e) {
                         Gdx.app.error("Client", "Failed reading initial worldData blob (continuing)", e);
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
                 startSender();
                 // Inform server we are ready to receive initial live messages
                 try { System.out.println("[Client] connect: sending READY"); sendMessage("READY"); } catch (Exception ignored) {}
             } catch (Exception e) {
                 Gdx.app.error("Client", "Failed to connect to " + this.host + ":" + this.port, e);
                 this.running = false;
             }
         }, "MewnBase-Client-Connector").start();
     }

     private void handleMessage(String msg) {
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
         try {
             // NOTE: Do not globally dedupe messages by exact string; handle throttling per-type if needed
             appendDebugLog("RECEIVED: " + msg);
         } catch (Exception exception) {
         }
         try {
             flushPendingSpawns();
             if (msg.startsWith("CONNECTED:")) {
                 final int id = safeParseInt(msg.substring("CONNECTED:".length()).trim(), -1);
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
                         NetworkHelper.sendPayload(this.screen, "SPAWNREMOTE");
                     } else {
                         sendMessage("APPEARANCE:0||");
                         sendMessage("SPAWNREMOTE");
                     }
                 } catch (Exception e) {
                     Gdx.app.error("Client", "Failed to announce SPAWNREMOTE after CONNECTED", e);
                 }
                 if (this.screen != null && this.screen.game != null && this.screen.game.console != null) {
                     final String text = "Player " + id + " connected";
                     Gdx.app.postRunnable(() -> {
                         try {
                             this.screen.hud.hudNotifications.newMessage((String) null, text, Color.valueOf("25addb"));
                         } catch (Exception exception) {}
                     });
                 }
                 return;
             }
             if (msg.startsWith("DISCONNECTED:")) {
                 final int id = safeParseInt(msg.substring("DISCONNECTED:".length()).trim(), -1);
                 if (this.screen != null)
                     Gdx.app.postRunnable(() -> {
                         try {
                             this.screen.removePlayer(id);
                         } catch (Exception e) {
                             Gdx.app.error("Client", "Failed to remove player on render thread", e);
                         }
                     });
                 final String text = "Player " + id + " disconnected";
                 if (this.screen != null)
                     Gdx.app.postRunnable(() -> {
                         try {
                             this.screen.hud.hudNotifications.newMessage((String) null, text, Color.valueOf("e33e46"));
                         } catch (Exception exception) {}
                     });
                 return;
             }
             //
             if (msg.startsWith("APPEARANCE:")) {
                 try {
                     if (MultiplayerNetworkHelper.handleAppearance(this.screen, msg, -1)) {
                         return;
                     }
                 } catch (Exception e) {
                     Gdx.app.error("Client", "Exception handling APPEARANCE message", e);
                 }
             }
             if (msg.startsWith("PING:")) {
                 try {
                     System.out.println("[Client] PING: " + msg.substring("PING:".length()).toUpperCase());
                 } catch (Exception exception) {
                 }
                 return;
             }
             // Обработка сообщений от других клиентов (формат: ID:сообщение)
             int colon = msg.indexOf(":");
             if (colon <= 0) {
                 return;
             }
             String idPart = msg.substring(0, colon);
             String payload = msg.substring(colon + 1);
             int srcId;
             try {
                 srcId = Integer.parseInt(idPart);
             } catch (NumberFormatException nfe) {
                 return;
             }

            //
            if ("SPAWNREMOTE".equals(payload) || payload.startsWith("SPAWNREMOTE:")) {
                if (MultiplayerNetworkHelper.handleSpawnRemote(screen, payload, srcId)) {
                    return;
                }
                final int id = "SPAWNREMOTE".equals(payload) ? srcId : safeParseInt(payload.substring("SPAWNREMOTE:".length()), Integer.MIN_VALUE);
                if (id != Integer.MIN_VALUE) {
                    pendingSpawns.add(id);
                }
                return;
            }

             if (payload.startsWith("PING:")) {
                 try {
                     System.out.println("[Client] PING from " + srcId + ": " + payload.substring("PING:".length()).toUpperCase());
                 } catch (Exception exception) {
                 }
                 return;
             }
             String frag = payload;
             if (frag != null && frag.length() != 0) {
                 try {
                         if (frag.startsWith("CHAT:")) {
                             // 
                             if (!MultiplayerNetworkHelper.handleChat(this.screen, frag, srcId)) {
                                 Gdx.app.error("Client", "Failed to handle CHAT payload via helper", null);
                             }
                         } else if (frag.startsWith("APPEARANCE:")) {
                             try {
                                 MultiplayerNetworkHelper.handleAppearance(this.screen, frag, srcId);
                             } catch (Exception ignored) {}
                         } else if (frag.startsWith("INVENTORY_UPDATE:")) {
                             try {
                                 String enc = frag.substring("INVENTORY_UPDATE:".length());
                                 String json = java.net.URLDecoder.decode(enc, "UTF-8");
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
                         } else if (frag.startsWith("ANIM:")) {
                             String rest = frag.substring("ANIM:".length());
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
                         } else if (frag.startsWith("REQUEST_APPEARANCE:")) {
                             try {
                                 String rest = frag.substring("REQUEST_APPEARANCE:".length());
                                 final int id = safeParseInt(rest, Integer.MIN_VALUE);
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
                         } else if (frag.startsWith("POS:")) {
                             // Use unified handler for POS
                             try {
                                try {
                                    long now = System.currentTimeMillis();
                                    if ((now % 1000L) < 50L) {
                                        Gdx.app.log("Client", "[POS:recv] " + frag);
                                    }
                                } catch (Exception ignored) {}
                                 if (!MultiplayerNetworkHelper.handlePosition(this.screen, frag, srcId)) {
                                     Gdx.app.error("Client", "Error parsing POS via helper", null);
                                 }
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Exception while handling POS message", e);
                             }
                         } else if (frag.startsWith("TILE_BUILD_START:")) {
                             // Use the unified handler for TILE_BUILD_START
                             try {
                                 if (!MultiplayerNetworkHelper.handleTileBuildStart(this.screen, frag, srcId)) {
                                     Gdx.app.error("Client", "Error handling TILE_BUILD_START via helper", null);
                                 }
                             } catch (Exception e) {
                                 Gdx.app.error("Client", "Exception while handling TILE_BUILD_START", e);
                             }
                         } else if (frag.startsWith("TILE_") || frag.startsWith("ITEM_")) {
                             try {
                                 if (this.screen != null && this.screen.world != null) {
                                     final String f = frag;
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

     private float safeParseFloat(String s, float def) {
         try {
             return Float.parseFloat(s.trim());
         } catch (Exception e) {
             return def;
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

     public void sendMessage(String payload) {
         synchronized (this.outLock) {
             if (this.out == null) {
                 return;
             }
             try {
                 this.out.writeUTF(payload);
                 this.out.flush();
                 appendDebugLog("SENT: " + payload);
             } catch (Exception e) {
                 Gdx.app.error("Client", "Failed to send payload", e);
             }
         }
     }

     private void startSender() {
         if (this.senderThread != null && this.senderThread.isAlive()) {
             return;
         }
         this.senderThread = new Thread(() -> {
             while (this.running) {
                 try {
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
                             String payload = "POS:PLAYER:" + this.clientId + ":" + x + ":" + y + ":" + vx + ":" + vy;
                             try {
                                 if ((now / 1000L) != ((this.lastSentMillis - 50L) / 1000L)) {
                                     Gdx.app.log("Client", "[POS:send] x=" + x + " y=" + y + " vx=" + vx + " vy=" + vy);
                                 }
                             } catch (Exception ignored) {}
                             sendMessage(this.clientId + ":" + payload);
                         }
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

 }
