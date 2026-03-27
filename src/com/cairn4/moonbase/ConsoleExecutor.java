/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;
import java.util.Collections;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.math.MathUtils;
import java.net.URLEncoder;
import com.cairn4.moonbase.AreaDamage;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerStatus;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Buggie;
import com.cairn4.moonbase.entities.Creature;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.LightningStrike;
import com.cairn4.moonbase.entities.Meteor;
import com.cairn4.moonbase.entities.Tank;
import com.cairn4.moonbase.entities.VehicleTrailer;
import com.cairn4.moonbase.techtree.TechUpgrade;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.ItemDropper;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.FrontendScreen;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.worlddata.DamageDef;
import com.strongjoshua.console.CommandExecutor;
import com.strongjoshua.console.annotation.ConsoleDoc;
import com.strongjoshua.console.annotation.HiddenCommand;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsoleExecutor
extends CommandExecutor {
    GameScreen gameScreen;
    World world;

    public ConsoleExecutor(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.world = this.gameScreen.world;
    }

    public void setup(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.world = gameScreen.world;
    }

    public void toggleCreatures() {
        MoonBase.SPAWN_CREATURES = !MoonBase.SPAWN_CREATURES;
        this.gameScreen.game.console.log("SPAWN_CREATURES:" + MoonBase.SPAWN_CREATURES);
    }

    @HiddenCommand
    public void setTimeSpeed(float mul) {
        this.gameScreen.world.dayCycle.timeMultiplier = mul;
    }

    public void getSeed() {
        this.gameScreen.game.console.log("Seed: " + this.gameScreen.world.terrainGen.getSeed());
    }

    public void toggleFx() {
        this.gameScreen.postProcessor.setEnabled(!this.gameScreen.postProcessor.isEnabled());
    }

    public void setHealth(float health) {
        this.gameScreen.world.player.playerStatus.health = health;
        this.gameScreen.world.player.notifyUpdate("statusUpdate");
    }

    @HiddenCommand
    public void altWalk(boolean on) {
        Player.altWalkSpeed = on;
    }

    public void setWeather(String weather) {
        this.gameScreen.world.weatherManager.setWeather(weather);
    }

    /**
     * Send chat message to other peers (or server) and log locally.
     * Builds payload: CHAT:<urlencodedNick>:<urlencodedText>
     */
    /**
     * Send chat message to other peers (or server) and log locally.
     * Builds payload: CHAT:<urlencodedNick>:<urlencodedText>
     * This version accepts a single string argument which matches the console callers
     * that pass the entire message as one parameter (avoids varargs parsing issues).
     */
    public void chat(String text) {
        try {
            String nick = MoonBase.multiplayerNick == null ? "" : MoonBase.multiplayerNick;
            String encNick = URLEncoder.encode(nick == null ? "" : nick, "UTF-8");
            String message = text == null ? "" : text;
            int nicknameLength = encNick.length()*2;
            String spaces = String.join("", Collections.nCopies(nicknameLength, " "));
            // System.out.println("'" + spaces + "'"); // Выведет: '     '
            message = spaces + message;
            String encText = URLEncoder.encode(message, "UTF-8");
            String payload = "CHAT:" + encNick + ":" + encText;
            if (this.gameScreen != null) {
                com.cairn4.moonbase.NetworkHelper.sendPayload(this.gameScreen, payload);
            }
            // When connected as a client, avoid local duplicate console output; otherwise show locally
            if (this.gameScreen != null) {
                boolean hasClient = false;
                try { hasClient = this.gameScreen.client != null; } catch (Exception ignored) {}
                if (!hasClient && this.gameScreen.game != null && this.gameScreen.game.console != null) {
                    final String display = "[CHAT] " + nick + ": " + message;
                    com.badlogic.gdx.Gdx.app.postRunnable(new Runnable() { public void run() { try { gameScreen.game.console.log(display); } catch (Exception ignored) {} } });
                }
            }
        } catch (Exception e) {
            Gdx.app.error("ConsoleExecutor", "Failed to send chat", e);
        }
    }

    /**
     * chatraw: a console helper that accepts the raw text including multiple spaces.
     * Some console frontends split on whitespace before calling commands; use `chatraw` when
     * you need to preserve multiple internal spaces (e.g., `chatraw Hello   world`).
     */
    public void chatraw(String text) {
        // Directly delegate to chat; the text here is taken verbatim by the console if provided.
        this.chat(text);
    }

    public void god() {
        MoonBase.GOD_MODE = !MoonBase.GOD_MODE;
        this.gameScreen.game.console.log("GOD_MODE: " + MoonBase.GOD_MODE);
    }

    @ConsoleDoc(description="Toggles all the game UI")
    public void hud() {
        if (this.gameScreen.hud.isHudVisible()) {
            this.gameScreen.hud.hide();
        } else {
            this.gameScreen.hud.show();
        }
    }

    @HiddenCommand
    public void cullTiles() {
        MoonBase.CULL_TILES = !MoonBase.CULL_TILES;
        this.gameScreen.game.console.log("CULL_TILES: " + MoonBase.CULL_TILES);
    }

    public void setDrawDistance(float distance) {
        MoonBase.DRAW_DISTANCE = distance;
        this.gameScreen.game.console.log("DRAW_DISTANCE: " + MoonBase.DRAW_DISTANCE);
    }

    public void setZoom(float zoom) {
        this.gameScreen.camera.zoom = zoom;
        this.gameScreen.camera.update();
        this.gameScreen.b2dCam.zoom = zoom;
        this.gameScreen.b2dCam.update();
    }

    public void giveItem(String itemId) {
        this.giveItem(itemId, 1);
    }

    public void giveItem(String itemId, int amount) {
        ItemStack i = new ItemStack(ItemFactory.getInstance().createItem(itemId));
        i.setAmount(amount);
        this.world.player.collect(i);
        this.gameScreen.game.console.log("Creating item \"" + i.getId() + "\" at (" + this.world.player.x + ", " + this.world.player.y + ")");
    }

    @ConsoleDoc(description="Moves player to a new tile position. Lander is at 500, 500", paramDescriptions={"X-tile position", "Y-tile position"})
    public void movePlayer(int tileX, int tileY) {
        this.world.player.moveToTile(tileX, tileY);
        this.gameScreen.game.console.log("Moving player to " + tileX + ", " + tileY);
    }

    public void die() {
        this.setHealth(0.0f);
        this.world.player.playerStatus.setAir(0.0f);
    }

    @ConsoleDoc(description="Changes the day-night cycle", paramDescriptions={"Valid values: dawn, day, dusk, night"})
    public void setTime(String time) {
        this.world.dayCycle.setPeriod(time);
        this.gameScreen.game.console.log("Setting day cycle to " + time);
    }

    @HiddenCommand
    public void giveBases() {
        this.giveItem("habitat-builder", 20);
        this.giveItem("airlock-builder", 10);
        this.giveItem("solar-builder", 10);
    }

    public void setHunger(float hunger) {
        this.world.player.playerStatus.setHunger(hunger);
    }

    public void setOxygen(float oxygen) {
        this.world.player.playerStatus.setAir(oxygen);
    }

    public void setSuitPower(float power) {
        this.world.player.playerStatus.setSuitPower(power);
    }

    public void fullbars() {
        this.gameScreen.world.player.playerStatus.setHealth(1000.0f);
        this.gameScreen.world.player.playerStatus.setAir(1000.0f);
        this.gameScreen.world.player.playerStatus.setSuitPower(1000.0f);
        this.gameScreen.world.player.playerStatus.setHunger(1000.0f);
    }

    public void debug() {
        MoonBase.setDebugInfo(!MoonBase.DEBUG_INFO);
        this.gameScreen.hud.showDebug(MoonBase.DEBUG_INFO);
        this.debugOutput(MoonBase.DEBUG_INFO);
        this.gameScreen.game.console.log("DEBUG_INFO:" + MoonBase.DEBUG_INFO);
    }

    public void perfgraph() {
        this.gameScreen.game.setPerfGraph(!MoonBase.PERF_GRAPH);
        this.gameScreen.game.console.log("PERF_GRAPH:" + MoonBase.PERF_GRAPH);
    }

    public void debugOutput(boolean on) {
        if (on) {
            Gdx.app.setLogLevel(3);
        } else {
            Gdx.app.setLogLevel(2);
        }
    }

    public void setDay(int day) {
        this.gameScreen.world.dayCycle.setDay(day);
    }

    public void autoSave() {
        this.gameScreen.game.console.log("Saving game...");
        this.gameScreen.gameLoader.autoSave(this.gameScreen.world);
    }

    public void saveQuit() {
        this.gameScreen.gameLoader.saveGame(this.gameScreen.world, false);
        this.gameScreen.dispose();
        this.gameScreen.game.setScreen(new FrontendScreen(this.gameScreen.game));
    }

    public void listBases() {
        this.gameScreen.game.console.log("Listing base modules");
        for (BaseModule b : this.world.baseManager.getMasterListCopy()) {
            this.gameScreen.game.console.log(" - " + b.getClass().getSimpleName() + " (" + b.worldX + ", " + b.worldY);
        }
    }

    public void fullScreen() {
        this.gameScreen.game.console.log("Toggling FullScreen");
        this.gameScreen.game.toggleFullScreen();
    }

    public void testPlayerAnim(String name, int loop) {
        boolean looping = loop == 1;
        this.gameScreen.world.player.playerAnimController.testAnim(name, looping);
    }

    @ConsoleDoc(description="Run inversion tests for ANIMPLAY sending/mirroring (testNum 1..6)", paramDescriptions={"testNum","animName"})
    public void inverse(final int testNum, final String animName) {
        if (this.gameScreen == null || this.world == null || this.world.player == null) {
            if (this.gameScreen != null) this.gameScreen.game.console.log("inverse: no game/player available");
            return;
        }
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    int owner = 0; try { owner = ConsoleExecutor.this.world.player.ownerId; } catch (Exception ignored) {}
                    // best-effort read of current skeleton scale via reflection
                    float scale = 1.0f;
                    try {
                        java.lang.reflect.Field spineF = com.cairn4.moonbase.Player.class.getDeclaredField("spineActor");
                        spineF.setAccessible(true);
                        Object spine = spineF.get(ConsoleExecutor.this.world.player);
                        if (spine != null) {
                            try {
                                java.lang.reflect.Field skF = spine.getClass().getDeclaredField("skeleton");
                                skF.setAccessible(true);
                                Object sk = skF.get(spine);
                                if (sk != null) {
                                    try {
                                        java.lang.reflect.Method getScaleX = sk.getClass().getMethod("getScaleX");
                                        Object val = getScaleX.invoke(sk);
                                        if (val instanceof Float) scale = (Float)val;
                                    } catch (NoSuchMethodException nsme) {
                                        // try getScale
                                        try { java.lang.reflect.Method getScale = sk.getClass().getMethod("getScaleX"); Object v = getScale.invoke(sk); if (v instanceof Float) scale = (Float)v; } catch (Exception ignored) {}
                                    }
                                }
                            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                        }
                    } catch (Exception ignored) {}

                    String payload = "ANIMPLAY:PLAYER:" + owner + ":" + (animName == null ? "walk_right" : animName) + ":true:" + scale;

                    switch (testNum) {
                        case 1:
                            // Use centralized NetworkHelper (normal path)
                            com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, payload);
                            ConsoleExecutor.this.gameScreen.game.console.log("inverse: sent via NetworkHelper: " + payload);
                            break;
                        case 2:
                            // Send via client.sendMessage reflection
                            try {
                                if (ConsoleExecutor.this.gameScreen != null && ConsoleExecutor.this.gameScreen.client != null) {
                                    Class<?> cc = ConsoleExecutor.this.gameScreen.client.getClass();
                                    java.lang.reflect.Method send = cc.getMethod("sendMessage", String.class);
                                    send.invoke(ConsoleExecutor.this.gameScreen.client, payload);
                                    ConsoleExecutor.this.gameScreen.game.console.log("inverse: sent via client.sendMessage: " + payload);
                                } else {
                                    ConsoleExecutor.this.gameScreen.game.console.log("inverse: no client to send via direct method");
                                }
                            } catch (NoSuchMethodException nsme) {
                                ConsoleExecutor.this.gameScreen.game.console.log("inverse: client.sendMessage not available");
                            } catch (Exception e) {
                                ConsoleExecutor.this.gameScreen.game.console.log("inverse: failed direct client send: " + e);
                            }
                            break;
                        case 3:
                            // Use active server broadcastFromServer
                            try {
                                com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                                if (s != null) {
                                    s.broadcastFromServer(payload);
                                    ConsoleExecutor.this.gameScreen.game.console.log("inverse: server.broadcastFromServer: " + payload);
                                } else {
                                    ConsoleExecutor.this.gameScreen.game.console.log("inverse: no active server to broadcast");
                                }
                            } catch (Exception e) {
                                ConsoleExecutor.this.gameScreen.game.console.log("inverse: failed server broadcast: " + e);
                            }
                            break;
                        case 4:
                            // Send an ANIM spawn packet (different handling path) at player's coords
                            try {
                                int wx = Math.round(ConsoleExecutor.this.world.player.getXPos());
                                int wy = Math.round(ConsoleExecutor.this.world.player.getYPos());
                                String spawnPayload = "ANIM:spawn:" + wx + ":" + wy;
                                com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, spawnPayload);
                                ConsoleExecutor.this.gameScreen.game.console.log("inverse: sent ANIM spawn via NetworkHelper: " + spawnPayload);
                            } catch (Exception e) {
                                ConsoleExecutor.this.gameScreen.game.console.log("inverse: failed sending ANIM spawn: " + e);
                            }
                            break;
                        case 5:
                            // Send ANIMPLAY with owner=0 (server/host style) to test ownership handling
                            try {
                                String p0 = "ANIMPLAY:PLAYER:0:" + (animName == null ? "walk_right" : animName) + ":true:" + scale;
                                com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, p0);
                                ConsoleExecutor.this.gameScreen.game.console.log("inverse: sent ANIMPLAY owner=0: " + p0);
                            } catch (Exception e) {
                                ConsoleExecutor.this.gameScreen.game.console.log("inverse: failed sending owner=0: " + e);
                            }
                            break;
                        case 6:
                            // Apply locally first (flip/animation) then broadcast via NetworkHelper
                            try {
                                // Try to apply locally via playerAnimController.testAnim
                                try {
                                    java.lang.reflect.Field pacField = com.cairn4.moonbase.Player.class.getDeclaredField("playerAnimController");
                                    pacField.setAccessible(true);
                                    Object pac = pacField.get(ConsoleExecutor.this.world.player);
                                    if (pac != null) {
                                        try {
                                            java.lang.reflect.Method test = pac.getClass().getMethod("testAnim", String.class, boolean.class);
                                            test.invoke(pac, animName, true);
                                        } catch (NoSuchMethodException nsme) {}
                                    }
                                } catch (Exception ignored) {}
                                // Then broadcast
                                com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, payload);
                                ConsoleExecutor.this.gameScreen.game.console.log("inverse: applied locally then sent: " + payload);
                            } catch (Exception e) {
                                ConsoleExecutor.this.gameScreen.game.console.log("inverse: failed local-apply+send: " + e);
                            }
                            break;
                        default:
                            ConsoleExecutor.this.gameScreen.game.console.log("inverse: unknown test number " + testNum);
                    }
                } catch (Exception e) {
                    ConsoleExecutor.this.gameScreen.game.console.log("inverse: unexpected error: " + e);
                }
            }
        });
    }

    @ConsoleDoc(description="Run mirror-only flip tests (testNum 1..5)", paramDescriptions={"testNum"})
    public void mirrorTest(final int testNum) {
        if (this.gameScreen == null || this.world == null || this.world.player == null) return;
        Gdx.app.postRunnable(new Runnable() {
            @Override public void run() {
                try {
                    int owner = 0; try { owner = ConsoleExecutor.this.world.player.ownerId; } catch (Exception ignored) {}
                    float scale = 1.0f; try { ConsoleExecutor.this.world.player.spineActor.skeleton.setScaleX(1.0f); } catch (Exception ignored) {}
                    // decide scale flip test (toggle negative)
                    scale = -1.0f;
                    String payload = "FLIP:PLAYER:" + owner + ":" + scale;
                    switch (testNum) {
                        case 1:
                            // NetworkHelper
                            com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, payload);
                            ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: sent via NetworkHelper: " + payload);
                            try {
                                // Also send ANIMTEST so receivers know which test mode to exercise (mode from GameScreen.animTestMode)
                                int mode = 0; try { mode = ConsoleExecutor.this.gameScreen.getAnimTestMode(); } catch (Exception ignored) {}
                                String anim = "idle";
                                String animTestPayload = "ANIMTEST:" + mode + ":PLAYER:" + owner + ":" + anim + ":true:" + scale;
                                com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, animTestPayload);
                                ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: sent ANIMTEST: " + animTestPayload);
                            } catch (Exception ignored) {}
                            try {
                                String flipOverride = "FLIP_OVERRIDE:PLAYER:" + owner + ":" + scale;
                                com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, flipOverride);
                                ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: sent FLIP_OVERRIDE: " + flipOverride);
                            } catch (Exception ignored) {}
                            break;
                        case 2:
                            // direct client send
                            try {
                                if (ConsoleExecutor.this.gameScreen.client != null) {
                                    Class<?> cc = ConsoleExecutor.this.gameScreen.client.getClass();
                                    java.lang.reflect.Method send = cc.getMethod("sendMessage", String.class);
                                    send.invoke(ConsoleExecutor.this.gameScreen.client, payload);
                                    ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: sent via client.sendMessage: " + payload);
                                } else ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: no client available");
                            } catch (Exception e) { ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: failed direct send: " + e); }
                            try {
                                int mode = 0; try { mode = ConsoleExecutor.this.gameScreen.getAnimTestMode(); } catch (Exception ignored) {}
                                String anim = "idle";
                                String animTestPayload = "ANIMTEST:" + mode + ":PLAYER:" + owner + ":" + anim + ":true:" + scale;
                                if (ConsoleExecutor.this.gameScreen.client != null) {
                                    Class<?> cc = ConsoleExecutor.this.gameScreen.client.getClass();
                                    java.lang.reflect.Method send = cc.getMethod("sendMessage", String.class);
                                    send.invoke(ConsoleExecutor.this.gameScreen.client, animTestPayload);
                                    ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: sent ANIMTEST via client: " + animTestPayload);
                                }
                            } catch (Exception ignored) {}
                            try {
                                if (ConsoleExecutor.this.gameScreen.client != null) {
                                    Class<?> cc2 = ConsoleExecutor.this.gameScreen.client.getClass();
                                    java.lang.reflect.Method send2 = cc2.getMethod("sendMessage", String.class);
                                    String flipOverride = "FLIP_OVERRIDE:PLAYER:" + owner + ":" + scale;
                                    send2.invoke(ConsoleExecutor.this.gameScreen.client, flipOverride);
                                    ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: sent FLIP_OVERRIDE via client: " + flipOverride);
                                }
                            } catch (Exception ignored) {}
                            break;
                        case 3:
                            // server broadcast
                            try { com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer(); if (s != null) { s.broadcastFromServer(payload); ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: server broadcast: " + payload); } else ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: no server"); } catch (Exception e) { ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: failed server broadcast: " + e); }
                            try { com.cairn4.moonbase.Server s3 = com.cairn4.moonbase.Server.getActiveServer(); if (s3 != null) { String flipOverride = "FLIP_OVERRIDE:PLAYER:" + owner + ":" + scale; s3.broadcastFromServer(flipOverride); ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: server broadcast FLIP_OVERRIDE: " + flipOverride); } } catch (Exception ignored) {}
                            break;
                        case 4:
                            try { com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer(); if (s != null) { s.broadcastFromServer(payload); ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: server broadcast: " + payload); try { int mode = ConsoleExecutor.this.gameScreen.getAnimTestMode(); String anim = "idle"; String animTestPayload = "ANIMTEST:" + mode + ":PLAYER:" + owner + ":" + anim + ":true:" + scale; s.broadcastFromServer(animTestPayload); ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: server broadcast ANIMTEST: " + animTestPayload); } catch (Exception ignored) {} } else ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: no server"); } catch (Exception e) { ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: failed server broadcast: " + e); }
                            try { com.cairn4.moonbase.Server s4 = com.cairn4.moonbase.Server.getActiveServer(); if (s4 != null) { String flipOverride = "FLIP_OVERRIDE:PLAYER:" + owner + ":" + scale; s4.broadcastFromServer(flipOverride); ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: server broadcast FLIP_OVERRIDE: " + flipOverride); } } catch (Exception ignored) {}
                            try { String p = "ANIMPLAY:PLAYER:" + owner + ":idle:true:-1.0"; com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, p); ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: sent ANIMPLAY with scale: " + p); } catch (Exception e) { ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: failed send: " + e); }
                            break;
                        case 5:
                            // apply locally then broadcast flip-only
                            try {
                            try {
                                int mode = 0; try { mode = ConsoleExecutor.this.gameScreen.getAnimTestMode(); } catch (Exception ignored) {}
                                String animTestPayload = "ANIMTEST:" + mode + ":PLAYER:" + owner + ":idle:true:-1.0";
                                com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, animTestPayload);
                                ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: sent ANIMTEST: " + animTestPayload);
                            } catch (Exception ignored) {}
                            try {
                                String flipOverride = "FLIP_OVERRIDE:PLAYER:" + owner + ":" + scale;
                                com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, flipOverride);
                                ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: sent FLIP_OVERRIDE: " + flipOverride);
                            } catch (Exception ignored) {}
                                try { java.lang.reflect.Field spineField = com.cairn4.moonbase.Player.class.getDeclaredField("spineActor"); spineField.setAccessible(true); Object spine = spineField.get(ConsoleExecutor.this.world.player); if (spine != null) { java.lang.reflect.Field skF = spine.getClass().getDeclaredField("skeleton"); skF.setAccessible(true); Object sk = skF.get(spine); if (sk != null) { java.lang.reflect.Method setScaleX = sk.getClass().getMethod("setScaleX", float.class); setScaleX.invoke(sk, -1.0f); } } } catch (Exception ignored) {}
                                com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, payload);
                                ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: applied locally then sent flip: " + payload);
                                try {
                                    int mode = 0; try { mode = ConsoleExecutor.this.gameScreen.getAnimTestMode(); } catch (Exception ignored) {}
                                    String animTestPayload = "ANIMTEST:" + mode + ":PLAYER:" + owner + ":idle:true:" + scale;
                                    com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, animTestPayload);
                                    ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: sent ANIMTEST: " + animTestPayload);
                                } catch (Exception ignored) {}
                            } catch (Exception e) { ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: failed apply+send: " + e); }
                            break;
                        default:
                            ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: unknown test " + testNum);
                    }
                } catch (Exception e) { ConsoleExecutor.this.gameScreen.game.console.log("mirrorTest: unexpected: " + e); }
            }
        });
    }

    public void setDrift(float value) {
        for (Entity e : this.gameScreen.world.entityList) {
            if (!(e instanceof Buggie)) continue;
            Buggie b = (Buggie)e;
            b.setDrift(value);
        }
    }

    public void listItems() {
        this.gameScreen.game.console.log("Listing item ids: (Found " + ItemFactory.ITEMDATALIST.size() + " items)");
        this.gameScreen.game.console.log("--------");
        for (ItemData id : ItemFactory.ITEMDATALIST) {
            this.gameScreen.game.console.log("\t" + id.id);
        }
        this.gameScreen.game.console.log("--------");
    }

    @HiddenCommand
    public void meteor(int x, int y) {
        new Meteor(this.world, this.world.gameScreen.mainGroup, x, y);
    }

    public void lightning() {
        if (this.world.player != null) {
            LightningStrike ls = new LightningStrike(this.world, this.world.player.getXPos(), this.world.player.getYPos());
            ls.setDecoy(false);
        } else {
            this.gameScreen.game.console.log("Can't find player");
        }
    }

    public void lightning(int x, int y) {
        LightningStrike ls = new LightningStrike(this.world, (float)x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
        ls.setDecoy(false);
    }

    @HiddenCommand
    public void lightning(final int x, final int y, int secDelay) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){

            @Override
            public void run() {
                ConsoleExecutor.this.lightning(x, y);
            }
        }, secDelay * 1000);
    }

    @HiddenCommand
    public void listResolutionModes() {
        this.gameScreen.game.console.log("Display resolutions:");
        Graphics.DisplayMode[] displayModes = Gdx.graphics.getDisplayModes(Gdx.graphics.getPrimaryMonitor());
        int i = 0;
        for (Graphics.DisplayMode d : displayModes) {
            this.gameScreen.game.console.log("DisplayMode " + i + ": " + d.toString());
            ++i;
        }
    }

    public void quit() {
        Gdx.app.exit();
    }
    
    @ConsoleDoc(description="Temporarily pause client's outgoing sends for N seconds to test receiving while not sending. Usage: recvonly <seconds>")
    public void recvonly(int seconds) {
        if (this.gameScreen == null) return;
        Object clientObj = this.gameScreen.client;
        if (clientObj == null) { this.gameScreen.game.console.log("No client connected"); return; }
        try {
            Class<?> clientClass = clientObj.getClass();
            java.lang.reflect.Method pause = clientClass.getMethod("pauseSend", int.class);
            pause.invoke(clientObj, seconds);
            this.gameScreen.game.console.log("Client sends paused for " + seconds + "s (recv-only)");
        } catch (NoSuchMethodException nsme) {
            // fallback: try resumeSend if seconds <= 0
            try {
                if (seconds <= 0) {
                    Class<?> clientClass = clientObj.getClass();
                    java.lang.reflect.Method resume = clientClass.getMethod("resumeSend");
                    resume.invoke(clientObj);
                    this.gameScreen.game.console.log("Client sends resumed");
                    return;
                }
            } catch (Exception ignored) {}
            this.gameScreen.game.console.log("Client does not support pauseSend/resumeSend methods");
        } catch (Exception e) {
            this.gameScreen.game.console.log("Failed to pause client sends: " + e.getMessage());
        }
    }

    @ConsoleDoc(description="Toggle client diagnostic raw-read logging", paramDescriptions={"on|off"})
    public void diagclient(String state) {
        if (this.gameScreen == null) return;
        Object clientObj = this.gameScreen.client;
        if (clientObj == null) { this.gameScreen.game.console.log("No client connected"); return; }
        try {
            Class<?> clientClass = clientObj.getClass();
            if (state != null && (state.equalsIgnoreCase("on") || state.equals("1") || state.equalsIgnoreCase("enable"))) {
                java.lang.reflect.Method m = clientClass.getMethod("enableDiagnosticMode");
                m.invoke(clientObj);
                this.gameScreen.game.console.log("Client diagnostic mode ENABLED");
            } else {
                java.lang.reflect.Method m = clientClass.getMethod("disableDiagnosticMode");
                m.invoke(clientObj);
                this.gameScreen.game.console.log("Client diagnostic mode DISABLED");
            }
        } catch (NoSuchMethodException nsme) {
            this.gameScreen.game.console.log("Client does not support diagnostic methods");
        } catch (Exception e) {
            this.gameScreen.game.console.log("Failed to toggle diagnostic mode: " + e.getMessage());
        }
    }

    @ConsoleDoc(description="Set animation test mode: 0=normal,1=controller-first,2=skeleton-only,3=delayed-apply", paramDescriptions={"mode"})
    public void animtest(int mode) {
        if (this.gameScreen == null) return;
        try {
            this.gameScreen.setAnimTestMode(mode);
            this.gameScreen.game.console.log("animtest: set mode to " + mode);
        } catch (Exception e) {
            this.gameScreen.game.console.log("animtest: failed to set mode: " + e.getMessage());
        }
    }

    @ConsoleDoc(description="Clear flip override for an owner (server will broadcast the clear).", paramDescriptions={"ownerId"})
    public void flipclear(int ownerId) {
        if (this.gameScreen == null) return;
        try {
            String payload = "FLIP_OVERRIDE_CLEAR:PLAYER:" + ownerId;
            com.cairn4.moonbase.NetworkHelper.sendPayload(this.gameScreen, payload);
            this.gameScreen.game.console.log("flipclear: sent " + payload);
        } catch (Exception e) {
            this.gameScreen.game.console.log("flipclear: failed to send: " + e);
        }
    }

    public void noSaveAndQuit() {
        this.gameScreen.dispose();
        this.gameScreen.game.loadFrontendAssets();
        this.gameScreen.game.setScreen(new FrontendScreen(this.gameScreen.game));
    }

    public void unlockTech(String id) {
        this.world.techManager.research(id);
    }

    public void unlockAllTech() {
        this.world.techManager.unlockAll();
    }

    public void resetTech() {
        for (TechUpgrade tech : this.world.techManager.techTree.upgrades) {
            tech.unlocked = false;
        }
    }

    public void setTechSamples(int newTotal) {
        this.world.techManager.setSamples(newTotal);
    }

    public void getResearchItem(int id) {
        this.world.player.discoverResearchObject(id);
    }

    public void revealMap(int chunkRadius) {
        this.gameScreen.world.cheatDiscoverArea(chunkRadius);
    }

    @HiddenCommand
    public void countResources() {
        ArrayList<String> droppers = new ArrayList<String>();
        int[] quantities = new int[50];
        for (Chunk c : this.gameScreen.world.worldChunks.values()) {
            for (Tile t : c.tiles.values()) {
                if (!(t instanceof ItemDropper)) continue;
                ItemDropper d = (ItemDropper)t;
                if (!droppers.contains(d.rdData.id)) {
                    droppers.add(d.rdData.id);
                }
                int n = droppers.indexOf(d.rdData.id);
                quantities[n] = quantities[n] + 1;
            }
        }
        for (int i = 0; i < droppers.size(); ++i) {
            this.gameScreen.game.console.log((String)droppers.get(i) + ": " + quantities[i]);
            MoonBase.log((String)droppers.get(i) + ": " + quantities[i]);
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    public void setSuitLevel(int level) {
        if (level >= 0) {
            PlayerStatus cfr_ignored_0 = this.world.player.playerStatus;
            if (level < PlayerStatus.suitUpgradeData.suitLevels.size()) {
                this.world.player.setSuitLevel(level, false);
                this.gameScreen.game.console.log("Suit level set to " + level);
                this.gameScreen.game.console.log("New suit stats: \nMax air: " + this.gameScreen.world.player.playerStatus.maxAir + "\nMax suit power: " + this.gameScreen.world.player.playerStatus.maxSuitPower + "\nMax hunger: " + this.gameScreen.world.player.playerStatus.maxHunger + "\nFood consumption rate: " + this.gameScreen.world.player.playerStatus.foodConsumptionRate + "\nMax health: " + this.gameScreen.world.player.playerStatus.maxHealth);
                return;
            }
        }
        PlayerStatus cfr_ignored_1 = this.world.player.playerStatus;
        int maxLevel = PlayerStatus.suitUpgradeData.suitLevels.size() - 1;
        this.gameScreen.game.console.log("Invalid suit level. Min is 0, max is " + maxLevel);
    }

    public void giveBuggieStuff() {
        this.unlockAllTech();
        this.giveItem("metal", 10);
        this.giveItem("component", 10);
        this.giveItem("engine", 5);
        this.giveItem("garage-builder", 1);
        this.giveItem("solar-builder", 1);
    }

    public void buggie() {
        Gdx.app.postRunnable(new Runnable(){

            @Override
            public void run() {
                Buggie v = new Buggie(ConsoleExecutor.this.world, ConsoleExecutor.this.world.player.getXPos(), ConsoleExecutor.this.world.player.getYPos() - 200.0f, -90.0f);
                v.spawnAnim();
                try {
                    int wx = Math.round(v.getXPos()); int wy = Math.round(v.getYPos());
                    String payload = "ANIM:spawn:" + wx + ":" + wy;
                    if (ConsoleExecutor.this.gameScreen != null) com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, payload);
                } catch (Exception ignored) {}
            }
        });
    }

    @ConsoleDoc(description="Starts a multiplayer server on the specified port")
    public void createserver(int port) {
        try {
            // Use reflection so this compiles even if Server isn't available to the compiler toolchain
            Class<?> serverClass = Class.forName("com.cairn4.moonbase.Server");
            java.lang.reflect.Constructor<?> ctor = serverClass.getConstructor(int.class);
            Object serverObj = ctor.newInstance(port);
                java.lang.reflect.Method start = serverClass.getMethod("start");
                // If possible, pass the current GameScreen to the server so it can operate in-process
                try {
                    java.lang.reflect.Method setGS = serverClass.getMethod("setGameScreen", com.cairn4.moonbase.ui.GameScreen.class);
                    setGS.invoke(serverObj, this.gameScreen);
                } catch (NoSuchMethodException ignored) {
                    // older server doesn't accept GameScreen, ignore
                }
                start.invoke(serverObj);
            this.gameScreen.game.console.log("Server started on port " + port);
        } catch (ClassNotFoundException cnf) {
            // Reflection failed; fall back to a minimal inline server implementation so command still works.
            this.gameScreen.game.console.log("Server class not found; starting fallback server on port " + port);
            new Thread(() -> {
                Map<Integer, Object> clients = new ConcurrentHashMap<>();
                int[] nextClientId = new int[]{1};
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(port);
                    this.gameScreen.game.console.log("Fallback server listening on " + port);
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            int clientId = nextClientId[0]++;
                            this.gameScreen.game.console.log("Fallback: client connected " + clientSocket.getInetAddress().getHostAddress() + " id=" + clientId);
                            // Start simple handler thread
                            Thread t = new Thread(() -> {
                                try (DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                                     DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {
                                    // Send initial ID and zero lengths (no save transfer in fallback)
                                    out.writeInt(clientId);
                                    out.writeInt(0);
                                    out.writeInt(0);
                                    out.flush();
                                    while (!clientSocket.isClosed()) {
                                        try {
                                            String msg = in.readUTF();
                                            // Echo to console for now
                                            this.gameScreen.game.console.log("[server] " + clientId + ": " + msg);
                                        } catch (IOException e) {
                                            break;
                                        }
                                    }
                                } catch (IOException e) {
                                    // ignore
                                } finally {
                                    try { clientSocket.close(); } catch (IOException ignored) {}
                                }
                            });
                            t.setDaemon(true);
                            t.start();
                        } catch (IOException e) {
                            this.gameScreen.game.console.log("Fallback server accept error: " + e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    this.gameScreen.game.console.log("Fallback server failed to start: " + e.getMessage());
                } finally {
                    if (serverSocket != null) try { serverSocket.close(); } catch (IOException ignored) {}
                }
            }, "MewnBase-Fallback-Server").start();
        } catch (Exception e) {
            this.gameScreen.game.console.log("Failed to start server: " + e.getMessage());
        }
    }

    public void trailer() {
        Gdx.app.postRunnable(new Runnable(){

            @Override
            public void run() {
                VehicleTrailer v = new VehicleTrailer(ConsoleExecutor.this.world, ConsoleExecutor.this.world.player.getXPos(), ConsoleExecutor.this.world.player.getYPos() - 200.0f, -90.0f);
                v.spawnAnim();
                try {
                    int wx = Math.round(v.getXPos()); int wy = Math.round(v.getYPos());
                    String payload = "ANIM:spawn:" + wx + ":" + wy;
                    if (ConsoleExecutor.this.gameScreen != null) com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, payload);
                } catch (Exception ignored) {}
            }
        });
    }

    public void dozer() {
        Gdx.app.postRunnable(new Runnable(){

            @Override
            public void run() {
                Tank v = new Tank(ConsoleExecutor.this.world, ConsoleExecutor.this.world.player.getXPos(), ConsoleExecutor.this.world.player.getYPos() - 200.0f, -90.0f);
                v.spawnAnim();
                try {
                    int wx = Math.round(v.getXPos()); int wy = Math.round(v.getYPos());
                    String payload = "ANIM:spawn:" + wx + ":" + wy;
                    if (ConsoleExecutor.this.gameScreen != null) com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, payload);
                } catch (Exception ignored) {}
            }
        });
    }

    public void giveRocketStuff() {
        this.unlockAllTech();
        this.giveItem("flightcontrols", 1);
        this.giveItem("rockethull", 2);
        this.giveItem("rocketengine", 2);
        this.giveItem("launchpad-builder", 1);
        this.giveItem("fueltank-builder", 1);
        this.giveItem("refinery-builder", 1);
    }

    public void cameraLag(float x, float y) {
        this.world.gameScreen.changeCameraLag(x, y);
    }

    public void splode(int x, int y, float r, float d) {
        DamageDef damageDef = new DamageDef();
        damageDef.damageType = DamageDef.DamageTypes.physical;
        AreaDamage.damage(this.world, (float)x * Tile.TILE_SIZE, (float)y * Tile.TILE_SIZE, r, d, damageDef);
    }

    public void soundCheck(String file, float volume, float pitch) {
        Audio.getInstance().playSound("sounds/" + file + ".ogg", volume, pitch);
    }

    public void fillInventory() {
        int slotsToFill = 12 - this.world.player.playerInventory.itemList.size();
        for (int i = 0; i < slotsToFill; ++i) {
            int rand = MathUtils.random(0, ItemFactory.ITEMDATALIST.size() - 1);
            for (int tries = 0; !this.world.player.playerInventory.hasResources(ItemFactory.ITEMDATALIST.get((int)rand).id, 1) && tries < 90; ++tries) {
                this.world.player.collect(new ItemStack(ItemFactory.ITEMDATALIST.get((int)rand).id, 1));
            }
        }
    }

    @HiddenCommand
    public void resetSteamAchievementsStats(String pass) {
        if (pass.equals("please")) {
            this.world.gameScreen.game.platformAdapter.resetAchievementsAndStats();
            this.world.gameScreen.game.console.log("Resetting stats and achievements.");
        } else {
            this.world.gameScreen.game.console.log("Wrong password");
        }
    }

    public void spawnCreature(String creatureId) {
        new Creature(this.world, this.world.player.getXPos() + 300.0f, this.world.player.getYPos(), creatureId);
    }

    @ConsoleDoc(description="Spawn a fake remote player for testing multiplayer visuals", paramDescriptions={"clientId"})
    public void spawnremote(int clientId) {
        if (this.gameScreen == null) return;
        Gdx.app.postRunnable(new Runnable(){
            @Override
            public void run() {
                try {
                    GameScreen gs = ConsoleExecutor.this.gameScreen;
                    gs.addPlayer(clientId, ConsoleExecutor.this.world.player.getXPos(), ConsoleExecutor.this.world.player.getYPos());
                    gs.setPlayerDisplayName(clientId, "Test" + clientId);
                    Gdx.app.log("ConsoleExecutor", "Spawned remote player " + clientId);
                } catch (Exception e) {
                    Gdx.app.error("ConsoleExecutor", "Failed to spawn remote player", e);
                }
            }
        });
    }

    @ConsoleDoc(description="Test placing a tile at world coords using multiple methods and log results", paramDescriptions={"worldX","worldY","className"})
    public void testplace(final int worldX, final int worldY, final String className) {
        Gdx.app.postRunnable(new Runnable(){
            @Override
            public void run() {
                try {
                    ConsoleExecutor.this.gameScreen.game.console.log("TESTPLACE: Starting tests for " + className + " at (" + worldX + "," + worldY + ")");

                    // Basic direct TileFactory attempt
                    try {
                        com.cairn4.moonbase.worlddata.TileData td = new com.cairn4.moonbase.worlddata.TileData();
                        td.x = worldX;
                        td.y = worldY;
                        td.name = className;
                        int chunkX = com.cairn4.moonbase.Chunk.getChunkX(worldX);
                        int chunkY = com.cairn4.moonbase.Chunk.getChunkY(worldY);
                        com.cairn4.moonbase.Chunk c = ConsoleExecutor.this.world.getChunk(chunkX, chunkY);
                        if (c == null) {
                            ConsoleExecutor.this.gameScreen.game.console.log("TESTPLACE: chunk not loaded for " + chunkX + "," + chunkY + " - creating chunk");
                            c = ConsoleExecutor.this.world.createChunk(chunkX, chunkY);
                        }
                        com.cairn4.moonbase.tiles.Tile created = com.cairn4.moonbase.tiles.TileFactory.getInstance().createTile(c, td);
                        if (created != null) {
                            ConsoleExecutor.this.gameScreen.game.console.log("TESTPLACE: TileFactory created tile instance: " + created.getClass().getName() + " at local(" + created.x + "," + created.y + ")");
                            String chunkKey = com.cairn4.moonbase.World.convertWorldTileToChunkKey(worldX, worldY);
                            ConsoleExecutor.this.world.addTileToChunk(created, chunkKey, worldX, worldY);
                            ConsoleExecutor.this.gameScreen.game.console.log("TESTPLACE: world.addTileToChunk called");
                        } else {
                            ConsoleExecutor.this.gameScreen.game.console.log("TESTPLACE: TileFactory returned null for " + className);
                        }
                    } catch (Exception e) {
                        ConsoleExecutor.this.gameScreen.game.console.log("TESTPLACE: Direct TileFactory/world attempt failed: " + e);
                    }
                    ConsoleExecutor.this.gameScreen.game.console.log("TESTPLACE: finished tests for " + className + " at (" + worldX + "," + worldY + ")");
                } catch (Exception ex) {
                    ConsoleExecutor.this.gameScreen.game.console.log("TESTPLACE: unexpected error: " + ex);
                }
            }
        });
    }

    @ConsoleDoc(description="Test different drop methods for multiplayer debugging", paramDescriptions={"method(1=local+send,2=send-only,3=server-broadcast-only,4=legacy)", "itemId", "amount"})
    public void testdrop(final int method, final String itemId, final int amount) {
        if (this.gameScreen == null || this.world == null || this.world.player == null) {
            this.gameScreen.game.console.log("testdrop: no game/player available");
            return;
        }
        Gdx.app.postRunnable(new Runnable(){
            @Override
            public void run() {
                try {
                    int wx = (int)ConsoleExecutor.this.world.player.x;
                    int wy = (int)ConsoleExecutor.this.world.player.y;
                    String payload = "ITEM_DROP:" + wx + ":" + wy + ":" + itemId + ":" + amount;
                    String legacy = "13---" + wx + "---" + wy + "---" + itemId + "---" + amount;
                    // local create helper
                    Runnable localCreate = new Runnable() {
                        public void run() {
                            try {
                                String chunkKey = World.convertWorldTileToChunkKey(wx, wy);
                                int ckx = Integer.parseInt(chunkKey.split(",")[0]); int cky = Integer.parseInt(chunkKey.split(",")[1]);
                                Chunk ch = ConsoleExecutor.this.world.getChunk(ckx, cky); if (ch == null) ch = ConsoleExecutor.this.world.createChunk(ckx, cky);
                                com.badlogic.gdx.math.GridPoint2 local = World.convertWorldToLocal(new com.badlogic.gdx.math.GridPoint2(0,0), wx, wy);
                                com.cairn4.moonbase.ItemStack stack = new com.cairn4.moonbase.ItemStack(itemId, amount);
                                new com.cairn4.moonbase.tiles.ItemPile(ConsoleExecutor.this.world, ch, local.x, local.y, stack);
                                ConsoleExecutor.this.gameScreen.game.console.log("testdrop: created local ItemPile " + itemId + " x" + amount + " at " + wx + "," + wy);
                            } catch (Exception e) { ConsoleExecutor.this.gameScreen.game.console.log("testdrop: local create failed: " + e); }
                        }
                    };

                    switch (method) {
                        case 1:
                            // local create + send
                            localCreate.run();
                            try {
                                if (ConsoleExecutor.this.gameScreen != null && ConsoleExecutor.this.gameScreen.client != null) {
                                    Class<?> cc = ConsoleExecutor.this.gameScreen.client.getClass();
                                    java.lang.reflect.Method send = cc.getMethod("sendMessage", String.class);
                                    send.invoke(ConsoleExecutor.this.gameScreen.client, payload);
                                    ConsoleExecutor.this.gameScreen.game.console.log("testdrop: sent payload via client: " + payload);
                                } else {
                                    com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                                    if (s != null) {
                                        s.broadcastFromServer(payload);
                                        ConsoleExecutor.this.gameScreen.game.console.log("testdrop: server broadcast payload: " + payload);
                                    } else {
                                        ConsoleExecutor.this.gameScreen.game.console.log("testdrop: no client or server to send payload");
                                    }
                                }
                            } catch (NoSuchMethodException nsme) {
                                ConsoleExecutor.this.gameScreen.game.console.log("testdrop: client.sendMessage not available");
                            } catch (Exception e) {
                                ConsoleExecutor.this.gameScreen.game.console.log("testdrop: failed sending payload: " + e);
                            }
                            break;
                        case 2:
                            // send only via client or server
                            try {
                                if (ConsoleExecutor.this.gameScreen != null && ConsoleExecutor.this.gameScreen.client != null) {
                                    Class<?> cc = ConsoleExecutor.this.gameScreen.client.getClass();
                                    java.lang.reflect.Method send = cc.getMethod("sendMessage", String.class);
                                    send.invoke(ConsoleExecutor.this.gameScreen.client, payload);
                                    ConsoleExecutor.this.gameScreen.game.console.log("testdrop: sent payload via client: " + payload);
                                } else {
                                    com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                                    if (s != null) {
                                        s.broadcastFromServer(payload);
                                        ConsoleExecutor.this.gameScreen.game.console.log("testdrop: server broadcast payload: " + payload);
                                    } else {
                                        ConsoleExecutor.this.gameScreen.game.console.log("testdrop: no client or server to send payload");
                                    }
                                }
                            } catch (NoSuchMethodException nsme) {
                                ConsoleExecutor.this.gameScreen.game.console.log("testdrop: client.sendMessage not available");
                            } catch (Exception e) {
                                ConsoleExecutor.this.gameScreen.game.console.log("testdrop: failed sending payload: " + e);
                            }
                            break;
                        case 3:
                            // server broadcast only
                            try {
                                com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                                if (s != null) {
                                    s.broadcastFromServer(payload);
                                    ConsoleExecutor.this.gameScreen.game.console.log("testdrop: server broadcast payload: " + payload);
                                } else {
                                    ConsoleExecutor.this.gameScreen.game.console.log("testdrop: no active server to broadcast");
                                }
                            } catch (Exception e) {
                                ConsoleExecutor.this.gameScreen.game.console.log("testdrop: server broadcast failed: " + e);
                            }
                            break;
                        case 4:
                            // try legacy format
                            try {
                                if (ConsoleExecutor.this.gameScreen != null && ConsoleExecutor.this.gameScreen.client != null) {
                                    Class<?> cc = ConsoleExecutor.this.gameScreen.client.getClass();
                                    java.lang.reflect.Method send = cc.getMethod("sendMessage", String.class);
                                    send.invoke(ConsoleExecutor.this.gameScreen.client, legacy);
                                    ConsoleExecutor.this.gameScreen.game.console.log("testdrop: sent legacy payload via client: " + legacy);
                                } else {
                                    com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                                    if (s != null) {
                                        s.broadcastFromServer(legacy);
                                        ConsoleExecutor.this.gameScreen.game.console.log("testdrop: server broadcast legacy payload: " + legacy);
                                    } else {
                                        ConsoleExecutor.this.gameScreen.game.console.log("testdrop: no client or server to send legacy payload");
                                    }
                                }
                            } catch (Exception e) {
                                ConsoleExecutor.this.gameScreen.game.console.log("testdrop: failed sending legacy payload: " + e);
                            }
                            break;
                        default:
                            ConsoleExecutor.this.gameScreen.game.console.log("testdrop: unknown method " + method);
                    }
                } catch (Throwable t) {
                    ConsoleExecutor.this.gameScreen.game.console.log("testdrop: unexpected error: " + t);
                }
            }
        });
    }

    @ConsoleDoc(description="Run all drop test methods in sequence for debugging", paramDescriptions={"itemId","amount"})
    public void testdrop_all(final String itemId, final int amount) {
        // Run in background thread to avoid blocking render thread; each attempt spaced by 600ms
        new Thread(() -> {
            try {
                for (int method = 1; method <= 4; method++) {
                    final int m = method;
                    com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                        try {
                            ConsoleExecutor.this.testdrop(m, itemId, amount);
                        } catch (Exception e) {
                            try { ConsoleExecutor.this.gameScreen.game.console.log("testdrop_all: method " + m + " failed: " + e); } catch (Exception ignored) {}
                        }
                    });
                    try { Thread.sleep(600); } catch (InterruptedException ignored) {}
                }
                try { ConsoleExecutor.this.gameScreen.game.console.log("testdrop_all: finished all methods for " + itemId); } catch (Exception ignored) {}
            } catch (Throwable t) {
                try { ConsoleExecutor.this.gameScreen.game.console.log("testdrop_all: unexpected error: " + t); } catch (Exception ignored) {}
            }
        }, "MewnBase-TestDropRunner").start();
    }

    @ConsoleDoc(description="Request a 'natural' drop via the network (server rebroadcast will create piles)", paramDescriptions={"itemId","amount"})
    public void testdrop_nat(final String itemId, final int amount) {
        if (this.gameScreen == null || this.world == null || this.world.player == null) {
            if (this.gameScreen != null) this.gameScreen.game.console.log("testdrop_nat: no game/player available");
            return;
        }
        Gdx.app.postRunnable(new Runnable(){
            @Override
            public void run() {
                try {
                    int wx = (int)ConsoleExecutor.this.world.player.x;
                    int wy = (int)ConsoleExecutor.this.world.player.y;
                    String payload = "ITEM_DROP:" + wx + ":" + wy + ":" + itemId + ":" + amount;
                    // Use centralized helper to send (will choose client or server as appropriate)
                    com.cairn4.moonbase.NetworkHelper.sendPayload(ConsoleExecutor.this.gameScreen, payload);
                    ConsoleExecutor.this.gameScreen.game.console.log("testdrop_nat: sent payload: " + payload);
                } catch (Exception e) {
                    ConsoleExecutor.this.gameScreen.game.console.log("testdrop_nat: failed to send: " + e);
                }
            }
        });
    }

    @ConsoleDoc(description="Simulate the game's action-build flow (ProtoBase) at world coords", paramDescriptions={"worldX","worldY","className"})
    public void testplace_actionbuild(final int worldX, final int worldY, final String className) {
        Gdx.app.postRunnable(new Runnable(){
            @Override
            public void run() {
                try {
                    int wx = worldX;
                    int wy = worldY;
                    com.badlogic.gdx.math.GridPoint2 local = new com.badlogic.gdx.math.GridPoint2(0, 0);
                    local = World.convertWorldToLocal(local, wx, wy);
                    String chunkKey = World.convertWorldTileToChunkKey(wx, wy);
                    com.cairn4.moonbase.Chunk chunk = ConsoleExecutor.this.world.worldChunks.get(chunkKey);
                    if (chunk == null) {
                        chunk = ConsoleExecutor.this.world.createChunk(com.cairn4.moonbase.Chunk.getChunkX(wx), com.cairn4.moonbase.Chunk.getChunkY(wy));
                    }
                    String builderItemId = "habitat-builder";
                    int buildTime = 5;
                    new com.cairn4.moonbase.tiles.ProtoBase(ConsoleExecutor.this.world, chunk, local.x, local.y, builderItemId, className, buildTime, ConsoleExecutor.this.world.player.playerInventory.placementOrientation);
                // If multiplayer client exists, broadcast TILE_BUILD_START so other clients/host simulate the action-build
                    try {
                        String encName = java.net.URLEncoder.encode(className == null ? "" : className, "UTF-8");
                        String encItemId = java.net.URLEncoder.encode(builderItemId == null ? "" : builderItemId, "UTF-8");
                        int itemNum = 1;
                        int owner = 0;
                        // Format: TILE_BUILD_START:wx:wy:encName:encItemId:buildTime:orientation:owner
                        String payload = "TILE_BUILD_START:" + wx + ":" + wy + ":" + encName + ":" + encItemId + ":" + buildTime + ":" + java.net.URLEncoder.encode(ConsoleExecutor.this.world.player.playerInventory.placementOrientation.toString(), "UTF-8") + ":" + owner;
                        // If we have a client object, send via client (normal client mode)
                        if (ConsoleExecutor.this.gameScreen != null && ConsoleExecutor.this.gameScreen.client != null) {
                            try {
                                Class<?> cc = ConsoleExecutor.this.gameScreen.client.getClass();
                                java.lang.reflect.Method send = cc.getMethod("sendMessage", String.class);
                                send.invoke(ConsoleExecutor.this.gameScreen.client, payload);
                                ConsoleExecutor.this.gameScreen.game.console.log("TESTPLACE: sent TILE_BUILD_START: " + payload);
                            } catch (NoSuchMethodException nsme) {
                                // client doesn't expose sendMessage via reflection, ignore
                            }
                        } else {
                            // If we're running as an in-process server, notify connected clients via active server
                            try {
                                com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                                if (s != null) {
                                    s.broadcastFromServer(payload);
                                    ConsoleExecutor.this.gameScreen.game.console.log("TESTPLACE: server broadcast TILE_BUILD_START: " + payload);
                                }
                            } catch (Exception ignored) {}
                        }
                    } catch (Exception ignored) {
                    }
                } catch (Exception ignored) {
                    // Intentionally silent to keep behavior identical to in-game action
                }
            }
        });
    }

    @ConsoleDoc(description="Place three modules in a line for quick multiplayer testing", paramDescriptions={"startX","startY","className"})
    public void placeline(final int startX, final int startY, final String className) {
        Gdx.app.postRunnable(new Runnable(){
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 3; i++) {
                        int wx = startX + i;
                        int wy = startY;
                        com.cairn4.moonbase.worlddata.TileData td = new com.cairn4.moonbase.worlddata.TileData();
                        td.x = wx;
                        td.y = wy;
                        td.name = className;
                        int chunkX = com.cairn4.moonbase.Chunk.getChunkX(wx);
                        int chunkY = com.cairn4.moonbase.Chunk.getChunkY(wy);
                        com.cairn4.moonbase.Chunk c = ConsoleExecutor.this.world.getChunk(chunkX, chunkY);
                        if (c == null) c = ConsoleExecutor.this.world.createChunk(chunkX, chunkY);
                        com.cairn4.moonbase.tiles.Tile created = com.cairn4.moonbase.tiles.TileFactory.getInstance().createTile(c, td);
                        if (created != null) {
                            String chunkKey = com.cairn4.moonbase.World.convertWorldTileToChunkKey(wx, wy);
                            ConsoleExecutor.this.world.addTileToChunk(created, chunkKey, wx, wy);
                        }
                    }
                    ConsoleExecutor.this.gameScreen.game.console.log("placeline: placed 3 x " + className + " starting at (" + startX + "," + startY + ")");
                } catch (Exception e) { ConsoleExecutor.this.gameScreen.game.console.log("placeline failed: " + e); }
            }
        });
    }

    @ConsoleDoc(description="Attempt to remove three modules in a line using different methods", paramDescriptions={"startX","startY"})
    public void removeline(final int startX, final int startY) {
        Gdx.app.postRunnable(new Runnable(){
            @Override
            public void run() {
                try {
                    // Method 1: call Tile.destroy() directly
                    for (int i = 0; i < 3; i++) {
                        int wx = startX + i;
                        int wy = startY;
                        com.cairn4.moonbase.tiles.Tile t = ConsoleExecutor.this.world.getTile(wx, wy);
                        if (t != null) {
                            t.destroy();
                        }
                    }
                    // Method 2: set readyToRemove and call chunk.updateTiles
                    for (int i = 0; i < 3; i++) {
                        int wx = startX + i;
                        int wy = startY;
                        com.cairn4.moonbase.tiles.Tile t = ConsoleExecutor.this.world.getTile(wx, wy);
                        if (t != null) {
                            t.readyToRemove = true;
                        }
                    }
                    // Force update on visible chunks to process removals
                    for (com.cairn4.moonbase.Chunk c : ConsoleExecutor.this.world.worldChunks.values()) {
                        c.updateTiles(0.016f);
                    }
                    // Method 3: call World.removeTile for the three tiles (simulates network command)
                    for (int i = 0; i < 3; i++) {
                        int wx = startX + i;
                        int wy = startY;
                        ConsoleExecutor.this.world.removeTile(wx, wy);
                    }
                    ConsoleExecutor.this.gameScreen.game.console.log("removeline: attempted 3 removal methods at (" + startX + "," + startY + ")");
                } catch (Exception e) { ConsoleExecutor.this.gameScreen.game.console.log("removeline failed: " + e); }
            }
        });
    }

    public void crab() {
        new Creature(this.world, this.world.player.getXPos() + 250.0f, this.world.player.getYPos(), "crab");
    }

    @ConsoleDoc(description="Sets your multiplayer nickname", paramDescriptions={"nickname"})
    public void setnick(String nick) {
        if (nick == null) return;
        nick = nick.trim();
        if (nick.length() == 0) return;
        com.cairn4.moonbase.MoonBase.multiplayerNick = nick;
        this.gameScreen.game.console.log("Nickname set to: " + nick);
        try {
            if (this.world != null && this.world.player != null) {
                try {
                    java.lang.reflect.Method setName = com.cairn4.moonbase.Player.class.getMethod("setDisplayName", String.class);
                    setName.invoke(this.world.player, nick);
                } catch (NoSuchMethodException nsme) {
                    // ignore if Player doesn't support display name
                }
            }
            // notify server if we have a client (send face|color|nick)
            if (this.gameScreen != null && this.gameScreen.client != null) {
                try {
                    int face = 0;
                    String color = "";
                    try { if (this.world != null && this.world.player != null) face = this.world.player.characterFaceOption; } catch (Exception ignored) {}
                    try { if (this.world != null && this.world.player != null) color = this.world.player.characterSuitColor != null ? this.world.player.characterSuitColor : ""; } catch (Exception ignored) {}
                    String payload = "APPEARANCE:" + face + "|" + java.net.URLEncoder.encode(color == null ? "" : color, "UTF-8") + "|" + java.net.URLEncoder.encode(nick, "UTF-8");
                    Class<?> clientClass = this.gameScreen.client.getClass();
                    java.lang.reflect.Method send = clientClass.getMethod("sendMessage", String.class);
                    send.invoke(this.gameScreen.client, payload);
                } catch (Exception e) {
                    // ignore
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public void firecrab() {
        new Creature(this.world, this.world.player.getXPos() + 250.0f, this.world.player.getYPos(), "firecrab");
    }

    public void slug() {
        new Creature(this.world, this.world.player.getXPos() + 250.0f, this.world.player.getYPos(), "slug");
    }

    public void slimeslug() {
        new Creature(this.world, this.world.player.getXPos() + 250.0f, this.world.player.getYPos(), "slimeslug");
    }

    public void fireslug() {
        new Creature(this.world, this.world.player.getXPos() + 250.0f, this.world.player.getYPos(), "fireslug");
    }

    @HiddenCommand
    public void lavaFlow(int worldX, int worldY) {
        this.world.terrainGen.spawnLavaFlow(worldX, worldY);
    }

    public void giveAllCrafters() {
        ArrayList<String> crafters = new ArrayList<String>();
        for (ItemData id : ItemFactory.ITEMDATALIST) {
            for (String s : id.craftedIn) {
                if (crafters.contains(s)) continue;
                crafters.add(s);
            }
        }
        for (String s : crafters) {
            this.giveItem(s + "-builder");
        }
    }

    @HiddenCommand
    public void setColorVar(String set, int colorNum) {
        this.gameScreen.hud.showColorMixer(set, colorNum);
    }

    public void miningrig() {
        this.giveItem("miningrig-builder");
    }

    @ConsoleDoc(description="Send a debugging PING to other peers (prints in caps on recipients)", paramDescriptions={"text"})
    public void ping(String text) {
        // allow ping with no argument: auto-select a remote player id if available
        String target = text;
        try {
            if (target == null || target.trim().length() == 0) {
                // pick a remote player if any
                try {
                    java.lang.reflect.Field rpField = com.cairn4.moonbase.ui.GameScreen.class.getDeclaredField("remotePlayers");
                    rpField.setAccessible(true);
                    Object rpObj = rpField.get(this.gameScreen);
                    if (rpObj instanceof java.util.Map) {
                        java.util.Map<?,?> rp = (java.util.Map<?,?>)rpObj;
                        if (!rp.isEmpty()) {
                            Object firstKey = rp.keySet().iterator().next();
                            target = String.valueOf(firstKey);
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        if (target == null) target = "";
        String payload = "PING:" + target;
        try {
            if (this.gameScreen != null && this.gameScreen.client != null) {
                try {
                    Class<?> cc = this.gameScreen.client.getClass();
                    java.lang.reflect.Method send = cc.getMethod("sendMessage", String.class);
                    send.invoke(this.gameScreen.client, payload);
                    this.gameScreen.game.console.log("PING sent via client: " + (target.length() > 0 ? target : "(broadcast)"));
                    return;
                } catch (NoSuchMethodException nsme) {
                    // fallthrough
                }
            }
            // If no client object, broadcast via active in-process server
            try {
                com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                if (s != null) {
            s.broadcastFromServer(payload);
            this.gameScreen.game.console.log("PING broadcast from server: " + (target.length() > 0 ? target : "(broadcast)"));
                    return;
                }
            } catch (Exception ignored) {}
        this.gameScreen.game.console.log("No network to send PING");
        } catch (Exception e) {
            this.gameScreen.game.console.log("Failed to send PING: " + e.getMessage());
        }
    }
}

