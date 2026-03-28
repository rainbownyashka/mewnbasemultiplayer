package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Timer;
import com.cairn4.moonbase.ui.GameScreen;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior;

/**
 * Unified handler for network messages for client and server.
 * Provides common methods for processing messages that can be used
 * both on client and server side.
 */
public class MultiplayerNetworkHelper {
    private static final HashSet<Integer> pendingSpawnRemote = new HashSet<Integer>();

    /**
     * Handles SPAWNREMOTE message, creating a remote player.
     * @param gameScreen game screen for adding a player
     * @param message full message to process
     * @param srcId sender identifier
     * @return true if the message was processed successfully
     */
    public static boolean handleSpawnRemote(GameScreen gameScreen, String message, int srcId) {
        try {
            int id;
            if ("SPAWNREMOTE".equals(message)) {
                id = srcId;
            } else if (message.startsWith("SPAWNREMOTE:")) {
                id = safeParseInt(message.substring("SPAWNREMOTE:".length()), Integer.MIN_VALUE);
            } else {
                return false;
            }
            
            if (id != Integer.MIN_VALUE && gameScreen != null) {
                Gdx.app.log("NetworkHelper", "Adding remote player with ID: " + id);
                final int playerId = id;
                if (MoonBase.getCurrentMission() == null || gameScreen.game == null || gameScreen.game.getCurrentMission() == null) {
                    if (!pendingSpawnRemote.contains(playerId)) {
                        pendingSpawnRemote.add(playerId);
                        Timer.schedule(new Timer.Task(){
                            int tries = 0;
                            @Override
                            public void run() {
                                if (MoonBase.getCurrentMission() != null && gameScreen.game != null && gameScreen.game.getCurrentMission() != null) {
                                    pendingSpawnRemote.remove(playerId);
                                    Gdx.app.postRunnable(() -> {
                                        try {
                                            gameScreen.addPlayer(playerId, 0, 0);
                                            Gdx.app.log("NetworkHelper", "Successfully added player with ID: " + playerId);
                                        } catch (Exception e) {
                                            Gdx.app.error("NetworkHelper", "Failed to add player with ID: " + playerId, e);
                                        }
                                    });
                                    cancel();
                                } else if (++tries > 100) {
                                    pendingSpawnRemote.remove(playerId);
                                    Gdx.app.error("NetworkHelper", "Timed out waiting for mission init; remote player not added: " + playerId);
                                    cancel();
                                }
                            }
                        }, 0.2f, 0.2f);
                    }
                } else {
                    Gdx.app.postRunnable(() -> { 
                        try { 
                            gameScreen.addPlayer(playerId, 0, 0); 
                            Gdx.app.log("NetworkHelper", "Successfully added player with ID: " + playerId);
                        } catch (Exception e) { 
                            Gdx.app.error("NetworkHelper", "Failed to add player with ID: " + playerId, e);
                        } 
                    });
                }
                return true;
            }
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling SPAWNREMOTE message", e);
        }
        return false;
    }

    /**
     * Handles APPEARANCE message, updating player appearance.
     * @param gameScreen game screen for updating appearance
     * @param message message to process
     * @param srcId sender identifier
     * @return true if the message was processed successfully
     */
    public static boolean handleAppearance(GameScreen gameScreen, String message, int srcId) {
        try {
            // Check if this is a direct APPEARANCE message or with prefix
            if (message.startsWith("APPEARANCE:")) {
                message = message.substring("APPEARANCE:".length());
            } else {
                return false;
            }

            String rest = message;
            int idx = rest.indexOf(":");
            if (idx > 0) {
                final int id = safeParseInt(rest.substring(0, idx), -1);
                final String data = rest.substring(idx + 1);

                String[] parts = data.split("\\|");
                final int face = safeParseInt((parts.length > 0) ? parts[0] : "0", 0);
                final String color = (parts.length > 1) ? parts[1] : "";
                
                if (gameScreen != null) {
                    Gdx.app.postRunnable(() -> {
                        try {
                            gameScreen.updatePlayerAppearance(id, face, color);
                            try {
                                String nick = (parts.length > 2) ? java.net.URLDecoder.decode(parts[2], "UTF-8") : "";
                                gameScreen.setPlayerDisplayName(id, nick);
                            } catch (Exception ignored) {}
                        } catch (Exception e) {
                            Gdx.app.error("NetworkHelper", "Failed to update appearance on render thread", e);
                        }
                    });
                    return true;
                }
            }
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling APPEARANCE message", e);
        }
        return false;
    }

    /**
     * Handles player position messages (POS)
     * @param gameScreen game screen for position updates
     * @param message position message
     * @param srcId sender identifier
     * @return true if message was processed successfully
     */
    public static boolean handlePosition(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!message.startsWith("POS:")) {
                return false;
            }

            String rest = message.substring("POS:".length());
            String[] parts = rest.split(":", 6);
            
            // Check both formats: "POS:x:y" and "POS:PLAYER:id:x:y:vx:vy"
            if (parts.length >= 2 && !"PLAYER".equals(parts[0])) {
                // Simple format POS:x:y (from server)
                try {
                    final float px = safeParseFloat(parts[0], Float.NaN);
                    final float py = safeParseFloat(parts[1], Float.NaN);
                    final int owner = srcId; // Use sender's ID
                    
                    if (gameScreen != null) {
                        Gdx.app.postRunnable(() -> {
                            try {
                                // Don't update local player position
                                if (gameScreen.world != null && gameScreen.world.player != null && 
                                    gameScreen.world.player.ownerId == owner) {
                                    return;
                                }
                                
                                // Update remote player position
                                Player remote = gameScreen.getRemotePlayer(owner);
                                if (remote == null) {
                                    return;
                                }
                                
                                // Set targetPosition for interpolation
                                try {
                                    java.lang.reflect.Field targetPosField = Player.class.getDeclaredField("targetPosition");
                                    targetPosField.setAccessible(true);
                                    Object vec = targetPosField.get(remote);
                                    if (vec instanceof com.badlogic.gdx.math.Vector2) {
                                        ((com.badlogic.gdx.math.Vector2)vec).set(px, py);
                                    }
                                } catch (Exception ignored) {}
                                // On simple POS we have no velocities; rely on interpolation only to avoid snaps
                            } catch (Exception e) {
                                Gdx.app.error("NetworkHelper", "Failed applying simple POS", e);
                            }
                        });
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    Gdx.app.error("NetworkHelper", "Error parsing simple POS format", e);
                    return false;
                }
            } else if (parts.length >= 5 && "PLAYER".equals(parts[0])) {
                final int owner = safeParseInt(parts[1], Integer.MIN_VALUE);
                final float px = safeParseFloat(parts[2], Float.NaN);
                final float py = safeParseFloat(parts[3], Float.NaN);
                final float vx = safeParseFloat(parts[4], 0.0F);
                final float vy = (parts.length > 5) ? safeParseFloat(parts[5], 0.0F) : 0.0F;
                
                if (gameScreen != null) {
                    Gdx.app.postRunnable(() -> {
                        try {
                            // Don't update local player position
                            if (gameScreen.world != null && gameScreen.world.player != null && 
                                gameScreen.world.player.ownerId == owner) {
                                return;
                            }
                            
                            // Update remote player position
                            Player remote = gameScreen.getRemotePlayer(owner);
                            if (remote == null) {
                                return;
                            }
                            
                            // Push target into small buffer for interpolation playback
                            try {
                                java.lang.reflect.Field targetPosField = Player.class.getDeclaredField("targetPosition");
                                targetPosField.setAccessible(true);
                                Object vec = targetPosField.get(remote);
                                if (vec instanceof com.badlogic.gdx.math.Vector2) {
                                    try {
                                        java.lang.reflect.Method enq = Player.class.getMethod("enqueueNetPos", float.class, float.class);
                                        enq.invoke(remote, px, py);
                                    } catch (Exception ignored2) {
                                        ((com.badlogic.gdx.math.Vector2)vec).set(px, py);
                                    }
                                }
                            } catch (Exception ignored) {}
                            // Apply velocity only when speed is meaningful to avoid stop-snaps
                            float speed = Math.abs(vx) + Math.abs(vy);
                            if (speed > 1.0f) {
                                try {
                                    java.lang.reflect.Field velField = Player.class.getDeclaredField("velX");
                                    velField.setAccessible(true);
                                    velField.setFloat(remote, vx);
                                } catch (Exception ignored) {}
                                try {
                                    java.lang.reflect.Field velField = Player.class.getDeclaredField("velY");
                                    velField.setAccessible(true);
                                    velField.setFloat(remote, vy);
                                } catch (Exception ignored) {}
                                try { remote.body.setLinearVelocity(vx / 256.0f, vy / 256.0f); } catch (Exception ignored) {}
                            } else {
                                try { remote.body.setLinearVelocity(0f, 0f); } catch (Exception ignored) {}
                            }
                        } catch (Exception e) {
                            Gdx.app.error("NetworkHelper", "Failed applying POS", e);
                        }
                    });
                    return true;
                }
            }
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling POS message", e);
        }
        return false;
    }

    /**
     * Handles chat messages
     * @param gameScreen game screen for displaying messages
     * @param message chat message
     * @param srcId sender identifier
     * @return true if message was processed successfully
     */
    public static boolean handleChat(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!message.startsWith("CHAT:")) {
                return false;
            }

            String rest = message.substring("CHAT:".length());
            int idx = rest.indexOf(":");
            final String encNick = (idx >= 0) ? rest.substring(0, idx) : "";
            final String encText = (idx >= 0) ? rest.substring(idx + 1) : rest;
            
            final String nick = URLDecoder.decode((encNick == null) ? "" : encNick, "UTF-8");
            final String text = URLDecoder.decode((encText == null) ? "" : encText, "UTF-8");
            
            if (gameScreen != null && gameScreen.game != null && gameScreen.game.console != null) {
                Gdx.app.postRunnable(() -> {
                    try {
                        gameScreen.game.console.log("[CHAT] " + nick + ": " + text);
                    } catch (Exception ignored) {}
                });
                
                final String fnick = (nick == null) ? "" : nick;
                final String ftext = (text == null) ? "" : text;
                
                Gdx.app.postRunnable(() -> {
                    try {
                        gameScreen.hud.hudNotifications.newChatMessage(fnick, ftext);
                    } catch (Exception ignored) {}
                });
                return true;
            }
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Failed to handle CHAT payload", e);
        }
        return false;
    }
    
    /**
     * Handles tile building start messages (TILE_BUILD_START)
     * @param gameScreen game screen for building creation
     * @param message message with building data
     * @param srcId sender identifier
     * @return true if message was handled successfully
     */
    public static boolean handleTileBuildStart(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!message.startsWith("TILE_BUILD_START:")) {
                return false;
            }
            
            String[] parts = message.split(":", 8);
            if (parts.length >= 7 && gameScreen != null) {
                final int fwx = safeParseInt(parts[1], Integer.MIN_VALUE);
                final int fwy = safeParseInt(parts[2], Integer.MIN_VALUE);
                final String encName = parts[3];
                final String encItemId = parts[4];
                final int fbuildTime = safeParseInt(parts[5], 0);
                final String encOrientation = parts[6];
                
                try {
                    final String className = URLDecoder.decode(encName, "UTF-8");
                    final String itemId = URLDecoder.decode(encItemId, "UTF-8");
                    
                    // Execute on render thread
                    Gdx.app.postRunnable(() -> {
                        try {
                            com.badlogic.gdx.math.GridPoint2 local = new com.badlogic.gdx.math.GridPoint2(0,0);
                            local = com.cairn4.moonbase.World.convertWorldToLocal(local, fwx, fwy);
                            com.cairn4.moonbase.Chunk chunk = gameScreen.world.ensureChunkLoadedForNetwork(fwx, fwy);
                                
                            // Decode orientation and use it when creating the ProtoBase
                            com.cairn4.moonbase.ui.BuildingPlacementCursor.ORIENTATIONS orientation = com.cairn4.moonbase.ui.BuildingPlacementCursor.ORIENTATIONS.north;
                            try { 
                                String decOrient = URLDecoder.decode(encOrientation, "UTF-8"); 
                                if (decOrient != null && decOrient.length() > 0) {
                                    orientation = com.cairn4.moonbase.ui.BuildingPlacementCursor.ORIENTATIONS.valueOf(decOrient); 
                                }
                            } catch (Exception ignore) {}
                            
                            new com.cairn4.moonbase.tiles.ProtoBase(gameScreen.world, chunk, local.x, local.y, itemId, className, fbuildTime, orientation);
                            Gdx.app.log("NetworkHelper", "Applied TILE_BUILD_START for " + className + " at (" + fwx + "," + fwy + ") buildTime=" + fbuildTime + " orientation=" + orientation);
                        } catch (Exception e) {
                            Gdx.app.error("NetworkHelper", "Failed to apply TILE_BUILD_START in render thread", e);
                        }
                    });
                    return true;
                } catch (Exception e) {
                    Gdx.app.error("NetworkHelper", "Error decoding TILE_BUILD_START params", e);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling TILE_BUILD_START message", e);
        }
        return false;
    }

    /**
     * Handles vehicle occupancy sync (VEH_OCCUPY:vehId:driverId:passengerId)
     */
    public static boolean handleVehicleOccupy(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!message.startsWith("VEH_OCCUPY:")) return false;
            String[] parts = message.split(":", 4);
            if (parts.length < 4 || gameScreen == null || gameScreen.world == null) return false;
            final int vehId = safeParseInt(parts[1], -1);
            final int driverId = safeParseInt(parts[2], -1);
            final int passengerId = safeParseInt(parts[3], -1);
            if (vehId < 0) return false;
            Gdx.app.postRunnable(() -> {
                try {
                    com.cairn4.moonbase.entities.Entity e = gameScreen.world.getEntityById(vehId);
                    if (!(e instanceof com.cairn4.moonbase.entities.Vehicle)) return;
                    com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                    int oldDriver = v.driverOwnerId;
                    int oldPassenger = v.passengerOwnerId;

                    // Clear seats then reapply
                    v.clearAllSeats();
                    if (driverId >= 0) v.setDriver(driverId);
                    if (passengerId >= 0) v.setPassenger(passengerId);

                    // Handle old occupants leaving (only if they are not in new occupant set)
                    if (oldDriver >= 0 && oldDriver != driverId && oldDriver != passengerId) {
                        com.cairn4.moonbase.Player p = (oldDriver == gameScreen.world.player.ownerId) ? gameScreen.world.player : gameScreen.getRemotePlayer(oldDriver);
                        if (p != null) p.exitVehicleRemote();
                    }
                    if (oldPassenger >= 0 && oldPassenger != passengerId && oldPassenger != driverId) {
                        com.cairn4.moonbase.Player p = (oldPassenger == gameScreen.world.player.ownerId) ? gameScreen.world.player : gameScreen.getRemotePlayer(oldPassenger);
                        if (p != null) p.exitVehicleRemote();
                    }

                    // Apply new occupants (skip local player to avoid camera changes)
                    if (driverId >= 0 && driverId != gameScreen.world.player.ownerId) {
                        com.cairn4.moonbase.Player rp = gameScreen.getRemotePlayer(driverId);
                        if (rp != null && rp.getVehicle() != v) rp.enterVehicleRemote(v, true);
                    }
                    if (passengerId >= 0 && passengerId != gameScreen.world.player.ownerId) {
                        com.cairn4.moonbase.Player rp = gameScreen.getRemotePlayer(passengerId);
                        if (rp != null && rp.getVehicle() != v) rp.enterVehicleRemote(v, false);
                    }

                    // Force-exit any player still attached to this vehicle but not listed in occupants
                    int dId = driverId;
                    int pId = passengerId;
                    try {
                        if (gameScreen.world != null && gameScreen.world.player != null) {
                            com.cairn4.moonbase.Player lp = gameScreen.world.player;
                            if (lp.getVehicle() == v && lp.ownerId != dId && lp.ownerId != pId) {
                                lp.exitVehicleRemote();
                            }
                        }
                    } catch (Exception ignored) {}
                    try {
                        java.lang.reflect.Field f = gameScreen.getClass().getDeclaredField("remotePlayers");
                        f.setAccessible(true);
                        Object mapObj = f.get(gameScreen);
                        if (mapObj instanceof java.util.Map) {
                            java.util.Map<?, ?> map = (java.util.Map<?, ?>)mapObj;
                            for (Object val : map.values()) {
                                if (val instanceof com.cairn4.moonbase.Player) {
                                    com.cairn4.moonbase.Player rp = (com.cairn4.moonbase.Player)val;
                                    if (rp.getVehicle() == v && rp.ownerId != dId && rp.ownerId != pId) {
                                        rp.exitVehicleRemote();
                                    }
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                } catch (Exception e) {
                    Gdx.app.error("NetworkHelper", "Failed to apply VEH_OCCUPY", e);
                }
            });
            return true;
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling VEH_OCCUPY", e);
        }
        return false;
    }

    /**
     * Handles vehicle state sync (VEH_STATE:vehId:x:y:rot:vx:vy)
     */
    public static boolean handleVehicleState(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!message.startsWith("VEH_STATE:")) return false;
            String[] parts = message.split(":", 7);
            if (parts.length < 7 || gameScreen == null || gameScreen.world == null) return false;
            final int vehId = safeParseInt(parts[1], -1);
            final float x = safeParseFloat(parts[2], Float.NaN);
            final float y = safeParseFloat(parts[3], Float.NaN);
            final float rot = safeParseFloat(parts[4], 0.0f);
            final float vx = safeParseFloat(parts[5], 0.0f);
            final float vy = safeParseFloat(parts[6], 0.0f);
            if (vehId < 0) return false;
            Gdx.app.postRunnable(() -> {
                try {
                    com.cairn4.moonbase.entities.Entity e = gameScreen.world.getEntityById(vehId);
                    if (!(e instanceof com.cairn4.moonbase.entities.Vehicle)) return;
                    com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                    try {
                        if (gameScreen.world.player != null && v.isDriver(gameScreen.world.player.ownerId)) {
                            return; // local driver ignores remote state
                        }
                    } catch (Exception ignored) {}
                    if (v.body != null) {
                        try { v.body.setTransform(x / 256.0f, y / 256.0f, rot * com.badlogic.gdx.math.MathUtils.degreesToRadians); } catch (Exception ignored) {}
                        try { v.body.setLinearVelocity(vx / 256.0f, vy / 256.0f); } catch (Exception ignored) {}
                    } else {
                        try { v.setWorldPos(x, y); } catch (Exception ignored) {}
                    }
                } catch (Exception e) {
                    Gdx.app.error("NetworkHelper", "Failed to apply VEH_STATE", e);
                }
            });
            return true;
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling VEH_STATE", e);
        }
        return false;
    }

    /**
     * Builds vehicle meta payload (VEH_META:vehId:<json>)
     */
    public static String buildVehicleMeta(com.cairn4.moonbase.entities.Vehicle v) {
        try {
            if (v == null) return null;
            Json json = new Json();
            String data = json.toJson(v.getNetState());
            return "VEH_META:" + v.id + ":" + data;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Handles vehicle meta/state sync (VEH_META:vehId:<json>)
     */
    public static boolean handleVehicleMeta(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!message.startsWith("VEH_META:")) return false;
            int idx1 = message.indexOf(':');
            int idx2 = message.indexOf(':', idx1 + 1);
            if (idx2 < 0 || gameScreen == null || gameScreen.world == null) return false;
            String idStr = message.substring(idx1 + 1, idx2);
            final int vehId = safeParseInt(idStr, -1);
            if (vehId < 0) return false;
            final String jsonStr = message.substring(idx2 + 1);
            Gdx.app.postRunnable(() -> {
                try {
                    com.cairn4.moonbase.entities.Entity e = gameScreen.world.getEntityById(vehId);
                    if (!(e instanceof com.cairn4.moonbase.entities.Vehicle)) return;
                    com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                    try {
                        if (gameScreen.world.player != null && v.isDriver(gameScreen.world.player.ownerId)) {
                            return; // local driver ignores remote meta
                        }
                    } catch (Exception ignored) {}
                    Json json = new Json();
                    @SuppressWarnings("unchecked")
                    java.util.HashMap<String, Object> state = json.fromJson(java.util.HashMap.class, jsonStr);
                    v.applyNetState(state);
                } catch (Exception e2) {
                    Gdx.app.error("NetworkHelper", "Failed to apply VEH_META", e2);
                }
            });
            return true;
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling VEH_META", e);
        }
        return false;
    }

    /**
     * Builds vehicle inventory sync payload (VEH_INV_SYNC:vehId:<urlencoded json>)
     */
    public static String buildVehicleInvSync(com.cairn4.moonbase.entities.Vehicle v) {
        try {
            if (v == null || v.trunk == null) return null;
            Json json = new Json();
            ArrayList<InventoryItemData> list = v.buildTrunkItemDataList();
            String data = json.toJson(list);
            String enc = URLEncoder.encode(data, "UTF-8");
            return "VEH_INV_SYNC:" + v.id + ":" + enc;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Handles vehicle inventory sync (VEH_INV_SYNC:vehId:<urlencoded json>)
     */
    public static boolean handleVehicleInvSync(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!message.startsWith("VEH_INV_SYNC:")) return false;
            int idx1 = message.indexOf(':');
            int idx2 = message.indexOf(':', idx1 + 1);
            if (idx2 < 0 || gameScreen == null || gameScreen.world == null) return false;
            String idStr = message.substring(idx1 + 1, idx2);
            final int vehId = safeParseInt(idStr, -1);
            if (vehId < 0) return false;
            final String enc = message.substring(idx2 + 1);
            final String jsonStr = URLDecoder.decode(enc == null ? "" : enc, "UTF-8");
            Gdx.app.postRunnable(() -> {
                try {
                    com.cairn4.moonbase.entities.Entity e = gameScreen.world.getEntityById(vehId);
                    if (!(e instanceof com.cairn4.moonbase.entities.Vehicle)) return;
                    com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                    if (v.trunk == null) return;
                    Json json = new Json();
                    @SuppressWarnings("unchecked")
                    ArrayList<InventoryItemData> list = json.fromJson(ArrayList.class, InventoryItemData.class, jsonStr);
                    v.applyTrunkItemDataList(list);
                    try { com.badlogic.gdx.ai.msg.MessageManager.getInstance().dispatchMessage(31); } catch (Exception ignored) {}
                    try { com.cairn4.moonbase.ui.BuggieTrunkUI.refreshIfActive(vehId); } catch (Exception ignored) {}
                } catch (Exception e2) {
                    Gdx.app.error("NetworkHelper", "Failed to apply VEH_INV_SYNC", e2);
                }
            });
            return true;
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling VEH_INV_SYNC", e);
        }
        return false;
    }

    /**
     * Handles vehicle inventory lock/unlock (VEH_LOCK/VEH_UNLOCK/VEH_LOCK_DENY)
     */
    public static boolean handleVehicleLock(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!(message.startsWith("VEH_LOCK:") || message.startsWith("VEH_UNLOCK:") || message.startsWith("VEH_LOCK_DENY:"))) return false;
            String[] parts = message.split(":", 4);
            if (parts.length < 3 || gameScreen == null || gameScreen.world == null) return false;
            final String kind = parts[0];
            final int vehId = safeParseInt(parts[1], -1);
            final int ownerId = safeParseInt(parts[2], -1);
            if (vehId < 0) return false;
            Gdx.app.postRunnable(() -> {
                try {
                    com.cairn4.moonbase.entities.Entity e = gameScreen.world.getEntityById(vehId);
                    if (e instanceof com.cairn4.moonbase.entities.Vehicle) {
                        com.cairn4.moonbase.entities.Vehicle v = (com.cairn4.moonbase.entities.Vehicle)e;
                        if ("VEH_LOCK".equals(kind)) {
                            v.inventoryLockOwnerId = ownerId;
                        } else if ("VEH_UNLOCK".equals(kind)) {
                            if (v.inventoryLockOwnerId == ownerId) v.inventoryLockOwnerId = -1;
                        } else if ("VEH_LOCK_DENY".equals(kind)) {
                            // handled on client UI side
                            try { com.cairn4.moonbase.ui.BuggieTrunkUI.handleLockDenied(vehId, ownerId, gameScreen); } catch (Exception ignored) {}
                        }
                    }
                } catch (Exception e2) {
                    Gdx.app.error("NetworkHelper", "Failed to apply VEH_LOCK", e2);
                }
            });
            return true;
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling VEH_LOCK", e);
        }
        return false;
    }

    /**
     * Builds base storage inventory sync payload (BASE_INV_SYNC:x:y:<urlencoded json>)
     */
    public static String buildBaseInvSync(BaseModule bm, ItemStorageBehavior isb) {
        try {
            if (bm == null || isb == null) return null;
            Json json = new Json();
            ArrayList<InventoryItemData> list = isb.buildItemDataList();
            String data = json.toJson(list);
            String enc = URLEncoder.encode(data, "UTF-8");
            return "BASE_INV_SYNC:" + bm.worldX + ":" + bm.worldY + ":" + enc;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Handles base storage inventory sync (BASE_INV_SYNC:x:y:<urlencoded json>)
     */
    public static boolean handleBaseInvSync(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!message.startsWith("BASE_INV_SYNC:")) return false;
            String[] parts = message.split(":", 4);
            if (parts.length < 4 || gameScreen == null || gameScreen.world == null) return false;
            final int wx = safeParseInt(parts[1], Integer.MIN_VALUE);
            final int wy = safeParseInt(parts[2], Integer.MIN_VALUE);
            if (wx == Integer.MIN_VALUE || wy == Integer.MIN_VALUE) return false;
            final String jsonStr = URLDecoder.decode(parts[3], "UTF-8");
            Gdx.app.postRunnable(() -> {
                try {
                    com.cairn4.moonbase.tiles.Tile t = gameScreen.world.getTile(wx, wy);
                    if (!(t instanceof BaseModule)) return;
                    BaseModule bm = (BaseModule)t;
                    ItemStorageBehavior isb = (ItemStorageBehavior) bm.getBehavior(ItemStorageBehavior.class);
                    if (isb == null) return;
                    Json json = new Json();
                    @SuppressWarnings("unchecked")
                    ArrayList<InventoryItemData> list = json.fromJson(ArrayList.class, InventoryItemData.class, jsonStr);
                    isb.applyItemDataList(list);
                    try {
                        com.cairn4.moonbase.ui.StorageUI.refreshIfActive(wx, wy);
                        com.cairn4.moonbase.ui.MiningRigUI.refreshIfActive(wx, wy);
                    } catch (Exception ignored) {}
                } catch (Exception e2) {
                    Gdx.app.error("NetworkHelper", "Failed to apply BASE_INV_SYNC", e2);
                }
            });
            return true;
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling BASE_INV_SYNC", e);
        }
        return false;
    }

    /**
     * Handles base storage lock/unlock (BASE_LOCK/BASE_UNLOCK/BASE_LOCK_DENY)
     */
    public static boolean handleBaseLock(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!(message.startsWith("BASE_LOCK:") || message.startsWith("BASE_UNLOCK:") || message.startsWith("BASE_LOCK_DENY:"))) return false;
            String[] parts = message.split(":", 4);
            if (parts.length < 4 || gameScreen == null || gameScreen.world == null) return false;
            final String kind = parts[0];
            final int wx = safeParseInt(parts[1], Integer.MIN_VALUE);
            final int wy = safeParseInt(parts[2], Integer.MIN_VALUE);
            final int ownerId = safeParseInt(parts[3], -1);
            if (wx == Integer.MIN_VALUE || wy == Integer.MIN_VALUE) return false;
            Gdx.app.postRunnable(() -> {
                try {
                    com.cairn4.moonbase.tiles.Tile t = gameScreen.world.getTile(wx, wy);
                    if (!(t instanceof BaseModule)) return;
                    BaseModule bm = (BaseModule)t;
                    ItemStorageBehavior isb = (ItemStorageBehavior) bm.getBehavior(ItemStorageBehavior.class);
                    if (isb == null) return;
                    if ("BASE_LOCK".equals(kind)) {
                        isb.inventoryLockOwnerId = ownerId;
                        try {
                            int localId = (gameScreen.world != null && gameScreen.world.player != null) ? gameScreen.world.player.ownerId : -1;
                            if (ownerId != localId) {
                                if (t instanceof com.cairn4.moonbase.tiles.StorageCrate) {
                                    ((com.cairn4.moonbase.tiles.StorageCrate)t).openLidAnimRemote();
                                } else if (t instanceof com.cairn4.moonbase.tiles.StoragePile) {
                                    ((com.cairn4.moonbase.tiles.StoragePile)t).openLidAnimRemote();
                                }
                            }
                        } catch (Exception ignored) {}
                    } else if ("BASE_UNLOCK".equals(kind)) {
                        if (isb.inventoryLockOwnerId == ownerId) isb.inventoryLockOwnerId = -1;
                        try {
                            int localId = (gameScreen.world != null && gameScreen.world.player != null) ? gameScreen.world.player.ownerId : -1;
                            if (ownerId != localId) {
                                if (t instanceof com.cairn4.moonbase.tiles.StorageCrate) {
                                    ((com.cairn4.moonbase.tiles.StorageCrate)t).closeLidAnim();
                                } else if (t instanceof com.cairn4.moonbase.tiles.StoragePile) {
                                    ((com.cairn4.moonbase.tiles.StoragePile)t).closeLidAnim();
                                }
                            }
                        } catch (Exception ignored) {}
                    } else if ("BASE_LOCK_DENY".equals(kind)) {
                        try { com.cairn4.moonbase.ui.StorageUI.handleLockDenied(wx, wy, ownerId, gameScreen); } catch (Exception ignored) {}
                        try { com.cairn4.moonbase.ui.MiningRigUI.handleLockDenied(wx, wy, ownerId, gameScreen); } catch (Exception ignored) {}
                    }
                } catch (Exception e2) {
                    Gdx.app.error("NetworkHelper", "Failed to apply BASE_LOCK", e2);
                }
            });
            return true;
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling BASE_LOCK", e);
        }
        return false;
    }

    /**
     * Handles vehicle spawn sync (VEH_SPAWN:type:id:x:y:rot)
     */
    public static boolean handleVehicleSpawn(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!message.startsWith("VEH_SPAWN:")) return false;
            String[] parts = message.split(":", 6);
            if (parts.length < 6 || gameScreen == null || gameScreen.world == null) return false;
            final String type = parts[1];
            final long entId = Long.parseLong(parts[2]);
            final float x = safeParseFloat(parts[3], Float.NaN);
            final float y = safeParseFloat(parts[4], Float.NaN);
            final float rot = safeParseFloat(parts[5], 0.0f);
            if (entId <= 0) return false;
            Gdx.app.postRunnable(() -> {
                try {
                    if (gameScreen.world.getEntityById(entId) != null) {
                        Gdx.app.log("NetworkHelper", "ENTITY_SPAWN skipped, id already exists: " + entId + " type=" + type);
                        return;
                    }
                    com.cairn4.moonbase.entities.Vehicle v = null;
                    if ("buggie".equals(type)) {
                        v = new com.cairn4.moonbase.entities.Buggie(gameScreen.world, x, y, rot);
                    } else if ("tank".equals(type)) {
                        v = new com.cairn4.moonbase.entities.Tank(gameScreen.world, x, y, rot);
                    } else if ("rover".equals(type)) {
                        v = new com.cairn4.moonbase.entities.Vehicle2(gameScreen.world, x, y, rot);
                    } else if ("trailer".equals(type)) {
                        v = new com.cairn4.moonbase.entities.VehicleTrailer(gameScreen.world, x, y, rot);
                    }
                    if (v != null) {
                        gameScreen.world.registerNetworkEntity(v, entId);
                        try {
                            java.lang.reflect.Method m = v.getClass().getMethod("spawnAnim");
                            m.invoke(v);
                        } catch (Exception ignored) {}
                    }
                } catch (Exception e) {
                    Gdx.app.error("NetworkHelper", "Failed to apply VEH_SPAWN", e);
                }
            });
            return true;
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling VEH_SPAWN", e);
        }
        return false;
    }

    /**
     * Handles generic entity spawn sync (ENTITY_SPAWN:type:id:x:y:rot)
     * Whitelist only known safe types.
     */
    public static boolean handleEntitySpawn(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!message.startsWith("ENTITY_SPAWN:")) return false;
            String[] parts = message.split(":", 6);
            if (parts.length < 6 || gameScreen == null || gameScreen.world == null) return false;
            final String type = parts[1];
            final long entId = Long.parseLong(parts[2]);
            final float x = safeParseFloat(parts[3], Float.NaN);
            final float y = safeParseFloat(parts[4], Float.NaN);
            final float rot = safeParseFloat(parts[5], 0.0f);
            if (entId <= 0) return false;
            Gdx.app.postRunnable(() -> {
                try {
                    if (gameScreen.world.getEntityById(entId) != null) return;
                    com.cairn4.moonbase.entities.Entity ent = null;
                    if ("buggie".equals(type)) {
                        ent = new com.cairn4.moonbase.entities.Buggie(gameScreen.world, x, y, rot);
                    } else if ("tank".equals(type)) {
                        ent = new com.cairn4.moonbase.entities.Tank(gameScreen.world, x, y, rot);
                    } else if ("rover".equals(type)) {
                        ent = new com.cairn4.moonbase.entities.Vehicle2(gameScreen.world, x, y, rot);
                    } else if ("trailer".equals(type)) {
                        ent = new com.cairn4.moonbase.entities.VehicleTrailer(gameScreen.world, x, y, rot);
                    }
                    if (ent != null) {
                        gameScreen.world.registerNetworkEntity(ent, entId);
                        try {
                            java.lang.reflect.Method m = ent.getClass().getMethod("spawnAnim");
                            m.invoke(ent);
                        } catch (Exception ignored) {}
                        Gdx.app.log("NetworkHelper", "ENTITY_SPAWN created type=" + type + " id=" + entId + " x=" + x + " y=" + y + " rot=" + rot);
                    }
                } catch (Exception e) {
                    Gdx.app.error("NetworkHelper", "Failed to apply ENTITY_SPAWN", e);
                }
            });
            return true;
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling ENTITY_SPAWN", e);
        }
        return false;
    }

    /**
     * Handles generator fuel sync (GENERATOR_FUEL:wx:wy:amount)
     */
    public static boolean handleGeneratorFuel(GameScreen gameScreen, String message, int srcId) {
        try {
            if (!message.startsWith("GENERATOR_FUEL:")) return false;
            String[] parts = message.split(":", 4);
            if (parts.length < 4 || gameScreen == null || gameScreen.world == null) return false;
            final int wx = safeParseInt(parts[1], Integer.MIN_VALUE);
            final int wy = safeParseInt(parts[2], Integer.MIN_VALUE);
            final float amt = safeParseFloat(parts[3], -1.0f);
            if (wx == Integer.MIN_VALUE || wy == Integer.MIN_VALUE || amt < 0) return false;
            Gdx.app.postRunnable(() -> {
                try {
                    gameScreen.world.ensureChunkLoadedForNetwork(wx, wy);
                    com.cairn4.moonbase.tiles.Tile t = gameScreen.world.getTile(wx, wy);
                    if (t instanceof com.cairn4.moonbase.tiles.Generator) {
                        com.cairn4.moonbase.tiles.Generator g = (com.cairn4.moonbase.tiles.Generator)t;
                        if (g.fuelStorageBehavior != null) {
                            g.fuelStorageBehavior.amount = amt;
                        }
                    }
                } catch (Exception e) {
                    Gdx.app.error("NetworkHelper", "Failed to apply GENERATOR_FUEL", e);
                }
            });
            return true;
        } catch (Exception e) {
            Gdx.app.error("NetworkHelper", "Error handling GENERATOR_FUEL", e);
        }
        return false;
    }
    
    /**
     * Utility for safe parsing of int with default value
     */
    private static int safeParseInt(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }
    
    /**
     * Utility for safe parsing of float with default value
     */
    private static float safeParseFloat(String s, float def) {
        try {
            return Float.parseFloat(s.trim());
        } catch (Exception e) {
            return def;
        }
    }
}
