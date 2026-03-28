/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.PerformanceCounter;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.utils.ShaderLoader;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.ConsoleExecutor;
import com.cairn4.moonbase.GameLoader;
import com.cairn4.moonbase.HeatDistortEffect;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Server;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.PlayerInputProcessor;
import com.cairn4.moonbase.RichPresenceState;
import com.cairn4.moonbase.SettingsData;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.Tutorial;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.ZSortComparator;
import com.cairn4.moonbase.dialog.DialogController;
import com.cairn4.moonbase.entities.Creature;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.CameraLag;
import com.cairn4.moonbase.ui.CameraShake;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.UIEdit;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.strongjoshua.console.GUIConsole;
import io.anuke.gif.GifRecorder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.cairn4.moonbase.worlddata.PlayerData;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import com.cairn4.moonbase.worlddata.GameSaveData;
import com.cairn4.moonbase.worlddata.PlanetData;

public class GameScreen
extends BaseScreen
implements Telegraph {
    public GameLoader gameLoader;
    public Group[] bgGroupLayers;
    public Stage worldUIStage;
    OrthographicCamera hudCamera;
    Viewport hudViewport;
    public Stage hudStage;
    public Hud hud;
    public CameraLag cameraLag;
    public CameraShake cameraShake;
    public boolean invalidZSort = true;
    public InputMultiplexer inputMulti;
    public UIEdit uiEdit;
    public HeatDistortEffect heatFx;
    private long lastLoad;
    public TextureAtlas tileAtlas;
    public TextureAtlas wallsAtlas;
    public TextureAtlas staticAtlas;
    public TextureAtlas headAtlas;
    public TextureAtlas techTreeAtlas;
    private boolean worldStageAct = true;
    private boolean worldStageDraw = true;
    private boolean physicsUpdate = true;
    private boolean worldUpdate = true;
    public inputModes inputMode;
    public ArrayList<Menu> menuStack = new ArrayList();
    public Menu menuFocus;
    public World world;
    public Tutorial tutorial;
    public Box2DDebugRenderer b2dr;
    public OrthographicCamera b2dCam;
    public static ShapeRenderer shapeRenderer;
    public SkeletonRenderer skeletonRenderer;
    public SkeletonRendererDebug skeletonRendererDebug;
    public PostProcessor postProcessor;
    public Bloom healthBlur;
    public boolean newGame;
    public PerformanceCounter updateCounter;
    public PerformanceCounter worldRenderCounter;
    public PerformanceCounter uiRenderCounter;
    public PerformanceCounter physicsCounter;
    public PerformanceCounter perfCounter;
    public PerformanceCounter playerUpdateCounter;
    public ZSortComparator zSortComparator = new ZSortComparator();
    public GifRecorder gifRecorder;
    public Object client;
    private Map<Integer, com.cairn4.moonbase.Player> remotePlayers = new HashMap<>();
    // Animation test mode (0=normal, 1=controller-first, 2=skeleton-only, 3=delayed-apply)
    public int animTestMode = 0;

    public void setAnimTestMode(int mode) { this.animTestMode = mode; }
    public int getAnimTestMode() { return this.animTestMode; }

    public void requestPlanetTravel() {
        try {
            if (MoonBase.isMultiplayer) {
                if (this.hud != null && this.hud.hudNotifications != null) {
                    this.hud.hudNotifications.newMessage("Planet travel is disabled in multiplayer (for now).");
                }
                return;
            }
            if (MoonBase.pendingPlanetSwitch != null) {
                return;
            }
            // Ensure loading screen assets are present (they may have been unloaded after previous load).
            try {
                com.cairn4.moonbase.AssetManagerSingleton.getInstance().load("loading.atlas", com.badlogic.gdx.graphics.g2d.TextureAtlas.class);
                com.cairn4.moonbase.AssetManagerSingleton.getInstance().load("loading-bg.png", com.badlogic.gdx.graphics.Texture.class);
                com.cairn4.moonbase.AssetManagerSingleton.getInstance().finishLoading();
            } catch (Exception ignored) {}
            // Save current planet state
            try { this.gameLoader.saveGame(this.world, false); } catch (Exception ignored) {}

            // Stash player + tech transfer
            try { MoonBase.pendingPlayerTransfer = new PlayerData(this.world.player); } catch (Exception ignored) {}
            try { MoonBase.pendingUnlockedTech = this.world.techManager.getSaveData(); } catch (Exception ignored) {}
            try { MoonBase.pendingTechSamples = this.world.techManager.samples; } catch (Exception ignored) {}

            int nextPlanetId = computeNextPlanetId();
            String nextPlanetType = pickPlanetType(nextPlanetId);
            String nextPlanetName = null;
            try {
                Mission m = MoonBase.getCurrentMission();
                if (m != null) {
                    nextPlanetName = m.generatePlanetName();
                }
            } catch (Exception ignored) {}
            if (nextPlanetName == null || nextPlanetName.length() == 0) {
                nextPlanetName = "Planet-" + (nextPlanetId + 1);
            }

            MoonBase.pendingPlanetSwitch = new MoonBase.PendingPlanetSwitch(nextPlanetId, nextPlanetName, nextPlanetType);
            // Update mission in memory before new world generation
            try {
                Mission m = MoonBase.getCurrentMission();
                if (m != null) {
                    m.planetId = nextPlanetId;
                    m.planetType = nextPlanetType;
                    m.setPlanetName(nextPlanetName);
                }
            } catch (Exception ignored) {}

            this.game.setScreen(new LoadingScreen(this.game, true));
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to start planet travel", e);
        }
    }

    private int computeNextPlanetId() {
        int nextId = 1;
        try {
            GameSaveData gsd = GameLoader.getGameSaveData(MoonBase.currentSaveFolder);
            if (gsd != null && gsd.planets != null) {
                int max = 0;
                for (PlanetData pd : gsd.planets) {
                    if (pd != null && pd.planetId > max) max = pd.planetId;
                }
                nextId = max + 1;
            }
        } catch (Exception ignored) {}
        return nextId;
    }

    private String pickPlanetType(int planetId) {
        // Alternate between two distinct profiles for now
        if (planetId % 2 == 1) return "crimson";
        return "ash";
    }

    private void applyPendingPlanetSwitch() {
        try {
            if (MoonBase.pendingPlanetSwitch == null) return;
            // Apply player transfer (inventory + stats) to the fresh world
            PlayerData pd = MoonBase.pendingPlayerTransfer;
            if (pd != null && this.world != null && this.world.player != null) {
                try {
                    this.world.player.getPlayerInventory().itemList.clear();
                    for (InventoryItemData invItemData : pd.inventoryItemDataList) {
                        try {
                            ItemStack stack = new ItemStack(invItemData.itemId, invItemData.amount);
                            int durabilityForThisType = ItemFactory.getDurability(invItemData.itemId);
                            if (durabilityForThisType > 0 && invItemData.durability == 0) {
                                invItemData.durability = durabilityForThisType;
                            }
                            stack.item.durability = invItemData.durability;
                            this.world.player.playerInventory.addItemNoOrg(stack);
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
                try { this.world.player.setCustomizationOptions(pd.characterFaceOption, pd.characterSuitColor); } catch (Exception ignored) {}
                try { this.world.player.setSuitLevel(pd.suitLevel, false); } catch (Exception ignored) {}
                try { this.world.player.playerStatus.setHunger(pd.hunger); } catch (Exception ignored) {}
                try { this.world.player.playerStatus.setSuitPower(pd.suitPower); } catch (Exception ignored) {}
                try { this.world.player.playerStatus.setAir(pd.air); } catch (Exception ignored) {}
                try { this.world.player.playerStatus.setFlashlight(pd.flashlight); } catch (Exception ignored) {}
                try { this.world.player.playerStatus.setHealth(pd.health <= 0.0f ? 100.0f : pd.health); } catch (Exception ignored) {}
                try { this.world.player.markedMapLocations = pd.markedMapLocations; } catch (Exception ignored) {}
                try { this.world.player.inventoryUpdate(); } catch (Exception ignored) {}
            }
            // Apply tech transfer
            try {
                if (MoonBase.pendingUnlockedTech != null && this.world != null && this.world.techManager != null) {
                    for (String s : MoonBase.pendingUnlockedTech) {
                        try {
                            com.cairn4.moonbase.techtree.TechUpgrade t = this.world.techManager.getTech(s);
                            if (t != null) t.unlocked = true;
                        } catch (Exception ignored) {}
                    }
                    this.world.techManager.setSamples(MoonBase.pendingTechSamples);
                }
            } catch (Exception ignored) {}
            // Persist new planet state immediately so worldData_pX is created
            try { this.gameLoader.saveGame(this.world, false); } catch (Exception ignored) {}
        } finally {
            MoonBase.pendingPlanetSwitch = null;
            MoonBase.pendingPlayerTransfer = null;
            MoonBase.pendingUnlockedTech = null;
            MoonBase.pendingTechSamples = 0;
        }
    }

    // Accessor for remote players map entries (safe public getter)
    public com.cairn4.moonbase.Player getRemotePlayer(int clientId) {
        return this.remotePlayers.get(clientId);
    }

    public java.util.Collection<com.cairn4.moonbase.Player> getRemotePlayers() {
        return this.remotePlayers.values();
    }

    public void setupCameraLag(Player player) {
        this.cameraLag = new CameraLag(player, this.camera, 60.0f, 40.0f);
    }

    public void changeCameraLag(float x, float y) {
        this.cameraLag.xThreshold = x;
        this.cameraLag.yThreshold = y;
        this.cameraLag.offset.setZero();
        this.cameraLag.camera.update();
    }

    public GameScreen(MoonBase game, Boolean newGame) {
        super(game);
        this.updateCounter = new PerformanceCounter("update");
        this.worldRenderCounter = new PerformanceCounter("renderWorld");
        this.uiRenderCounter = new PerformanceCounter("renderUI");
        this.physicsCounter = new PerformanceCounter("physics");
        this.perfCounter = new PerformanceCounter("perf");
        this.playerUpdateCounter = new PerformanceCounter("player");
        this.newGame = newGame;
        this.atlas = AssetManagerSingleton.getInstance().get("game.atlas", TextureAtlas.class);
        this.skin.addRegions(this.atlas);
        this.wallsAtlas = AssetManagerSingleton.getInstance().get("walls.atlas", TextureAtlas.class);
        this.skin.addRegions(this.wallsAtlas);
        this.staticAtlas = AssetManagerSingleton.getInstance().get("static.atlas", TextureAtlas.class);
        this.skin.addRegions(this.staticAtlas);
        this.tileAtlas = AssetManagerSingleton.getInstance().get("Tiles/tiles.atlas", TextureAtlas.class);
        for (Texture t : this.tileAtlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        this.skin.addRegions(this.tileAtlas);
        this.headAtlas = AssetManagerSingleton.getInstance().get("playerheads.atlas", TextureAtlas.class);
        this.skin.addRegions(this.headAtlas);
        this.techTreeAtlas = AssetManagerSingleton.getInstance().get("techtree.atlas", TextureAtlas.class);
        this.skin.addRegions(this.techTreeAtlas);
        TextureAtlas researchObjectsAtlas = AssetManagerSingleton.getInstance().get("researchobjects.atlas", TextureAtlas.class);
        this.skin.addRegions(researchObjectsAtlas);
        TextureAtlas frontendAtlas = AssetManagerSingleton.getInstance().get("frontend.atlas", TextureAtlas.class);
        this.skin.addRegions(frontendAtlas);
        ShaderLoader.BasePath = "shaders/bitfire_shaders/";
        this.postProcessor = new PostProcessor(false, false, true);
        this.postProcessor.setEnabled(true);
        this.heatFx = new HeatDistortEffect();
        Texture heatTex = new Texture(Gdx.files.internal("heat.png"));
        heatTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.heatFx.setDistortTex(heatTex);
        this.postProcessor.addEffect(this.heatFx);
        this.skeletonRenderer = new SkeletonRenderer();
        this.skeletonRenderer.setPremultipliedAlpha(false);
        this.skeletonRendererDebug = new SkeletonRendererDebug();
        this.skeletonRendererDebug.setRegionAttachments(false);
        this.skeletonRendererDebug.setBoundingBoxes(false);
        this.gameLoader = new GameLoader();
        Audio.getInstance().stopMusic();
        this.camera.zoom = CameraLag.WALKZOOM;
        this.cameraShake = new CameraShake(this.camera);
        this.hudCamera = new OrthographicCamera();
        this.hudViewport = new FitViewport(MoonBase.SCREEN_WIDTH, (float)MoonBase.SCREEN_HEIGHT, this.hudCamera);
        this.hudStage = new Stage(this.hudViewport);
        this.worldUIStage = new Stage(this.viewport);
        this.inputMode = inputModes.player;
        game.console = new GUIConsole();
        game.console.setDisplayKeyID(SettingsData.getValue(SettingsLoader.getInstance().settingsData.KEYS_CONSOLE));
        ConsoleExecutor ce = new ConsoleExecutor(this);
        game.console.setCommandExecutor(ce);
        game.console.log("Thanks for checking out MewnBase!");
        game.console.log("Created by Steve Forde - @cairn4");
        game.console.log("Build version 1.0.1");
        game.console.log("\"help\" to see list of commands");
        game.console.log("Press " + SettingsLoader.getInstance().settingsData.KEYS_CONSOLE + " key to close");
        game.console.log("");
        this.setInputProcessor();
        int bgGroups = 5;
        this.bgGroupLayers = new Group[bgGroups];
        for (int i = 0; i < bgGroups; ++i) {
            this.bgGroupLayers[i] = new Group();
            this.bgGroup.addActor(this.bgGroupLayers[i]);
        }
        this.world = new World(this, newGame);
        this.hud = new Hud(this, this.world);
        this.applyPendingPlanetSwitch();
        DialogController.getInstance().setGameScreen(this);
        if (!newGame.booleanValue()) {
            this.hud.activate(this.world);
        }
        try {
            if (SettingsLoader.getInstance().settingsData.FULLHD) {
                this.camera.zoom = 1.0f * CameraLag.HDMODEMUL;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        ce.setup(this);
        this.setInputProcessor();
        this.b2dr = new Box2DDebugRenderer();
        this.b2dCam = new OrthographicCamera();
        this.b2dCam.setToOrtho(false, (float)MoonBase.SCREEN_WIDTH / 256.0f, (float)MoonBase.SCREEN_HEIGHT / 256.0f);
        this.b2dCam.update();
        this.b2dr.setDrawVelocities(true);
        this.b2dr.setDrawContacts(true);
        this.b2dr.setDrawInactiveBodies(true);
        this.b2dr.setDrawJoints(true);
        shapeRenderer = new ShapeRenderer();
        this.gifRecorder = new GifRecorder(this.spriteBatch);
        this.gifRecorder.setOpenKey(139);
        this.gifRecorder.setRecordKey(140);
        MessageManager.getInstance().addListener(this, 1);
        MessageManager.getInstance().addListener(this, 2);
        this.updateRichPresence();
        this.uiEdit = new UIEdit(this, this.hudStage);
        if (MoonBase.isMultiplayer) {
            try {
                // Use reflection to avoid a hard compile-time dependency on Client class
                Class<?> clientClass = Class.forName("com.cairn4.moonbase.Client");
                java.lang.reflect.Constructor<?> ctor = clientClass.getConstructor(String.class, int.class, GameScreen.class);
                Object clientObj = ctor.newInstance(MoonBase.multiplayerHost, MoonBase.multiplayerPort, this);
                java.lang.reflect.Method connectMethod = clientClass.getMethod("connect");
                connectMethod.invoke(clientObj);
                this.client = clientObj;
                Gdx.app.log("GameScreen", "Started multiplayer client to " + MoonBase.multiplayerHost + ":" + MoonBase.multiplayerPort);
            } catch (ClassNotFoundException cnf) {
                Gdx.app.error("GameScreen", "Client class not found at runtime; multiplayer disabled.", cnf);
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Failed to start multiplayer client", e);
            }
        }
        this.lastLoad = new Date().getTime();
            // If an in-process server was started via JVM property, attach this GameScreen so
            // the server can apply host-origin messages locally (spawns, appearance, pos, etc).
            try {
                Server active = Server.getActiveServer();
                if (active != null) {
                    active.setGameScreen(this);
                    Gdx.app.log("GameScreen", "Attached active server to GameScreen (host mode)");
                }
            } catch (Throwable t) {
                Gdx.app.error("GameScreen", "Error while attaching active server", t);
            }
    }

    // Host POS broadcast helper state (adaptive send rate)
    private float lastHostSentX = Float.NaN;
    private float lastHostSentY = Float.NaN;
    private long lastHostSentMillis = 0L;
    // Increase thresholds to reduce server-origin POS traffic (client was occasionally overwhelmed)
    private final float HOST_POSITION_THRESHOLD = 2.0f; // world units (was 1.0)
    private final long HOST_MAX_INTERVAL = 100L; // ms (100 ms => 10 Hz host broadcasts)

    public long getLastLoad() {
        return this.lastLoad;
    }

    public void updateRichPresence() {
        RichPresenceState rpstate = new RichPresenceState();
        rpstate.state = RichPresenceState.States.playing;
        GameScreen gameScreen = this;
        if (gameScreen.game.getCurrentMission() == null) {
            Gdx.app.log("GameScreen", "updateRichPresence: mission not initialized yet");
            return;
        }
        rpstate.missionType = gameScreen.game.getCurrentMission().missionType;
        if (this.world != null && this.world.dayCycle != null) {
            rpstate.dayNum = this.world.dayCycle.getDay();
        }
        this.game.platformAdapter.updateRPC(rpstate);
    }

    @Override
    protected void setInputProcessor() {
        this.inputMulti = new InputMultiplexer();
        PlayerInputProcessor input = new PlayerInputProcessor();
        this.inputMulti.addProcessor(input);
        this.inputMulti.addProcessor(this.stage);
        this.inputMulti.addProcessor(this.hudStage);
        Gdx.input.setInputProcessor(this.inputMulti);
        if (this.game.console != null) {
            this.game.console.setDisplayKeyID(SettingsData.getValue(SettingsLoader.getInstance().settingsData.KEYS_CONSOLE));
            this.game.console.resetInputProcessing();
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
        MoonBase.log("GameScreen:hide");
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        MessageManager.getInstance().update();
        // If running as in-process server, periodically broadcast host POS to connected clients
        try {
            com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
            // Only broadcast host POS from server when there is no local client object (i.e., authoritative server).
            if (false && s != null && this.world != null && this.world.player != null && this.client == null) {
                long now = System.currentTimeMillis();
                float x = this.world.player.getXPos();
                float y = this.world.player.getYPos();
                boolean moved = Float.isNaN(this.lastHostSentX) || Math.abs(x - this.lastHostSentX) >= HOST_POSITION_THRESHOLD || Math.abs(y - this.lastHostSentY) >= HOST_POSITION_THRESHOLD;
                boolean timeElapsed = (now - this.lastHostSentMillis) >= HOST_MAX_INTERVAL;
                if (moved || timeElapsed) {
                    this.lastHostSentX = x;
                    this.lastHostSentY = y;
                    this.lastHostSentMillis = now;
                    String payload = "POS:" + x + ":" + y;
                    s.broadcastFromServer(payload);
                }
            }
        } catch (Exception ignored) {}
        if (this.worldUpdate) {
            this.world.update(delta);
        }
        if (this.world.player != null) {
            this.cameraLag.update(delta);
            this.b2dCam.position.set(this.camera.position.cpy().scl(0.00390625f));
            this.b2dCam.zoom = this.camera.zoom;
            this.b2dCam.update();
        }
        this.cameraShake.update(delta);
        this.uiEdit.update();
        if (this.world.gameState == World.GameStates.playing) {
            this.hud.update(delta);
            if (this.inputMode == inputModes.dialog) {
                this.world.player.handleDialogInput();
            } else if (this.inputMode != inputModes.menu) {
                this.world.player.handleInput();
            }
        } else {
            this.hud.update(delta);
        }
        if (this.menuStack.size() > 0) {
            this.menuStack.get(this.menuStack.size() - 1).update(delta);
            this.menuStack.get(this.menuStack.size() - 1).handleInput();
        }

        // Update remote players
        for (com.cairn4.moonbase.Player remotePlayer : this.remotePlayers.values()) {
            try {
                remotePlayer.update(delta);
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error updating remote player", e);
            }
        }
        // Enforce persistent FLIP overrides each frame for remote players
        try {
            // First, if running an in-process server, consult its flipOverrides
            com.cairn4.moonbase.Server activeServer = com.cairn4.moonbase.Server.getActiveServer();
            Map<Integer, Float> serverFlipMap = null;
            if (activeServer != null) {
                try {
                    java.lang.reflect.Field ff = com.cairn4.moonbase.Server.class.getDeclaredField("flipOverrides");
                    ff.setAccessible(true);
                    Object fmap = ff.get(activeServer);
                    if (fmap instanceof Map) serverFlipMap = (Map<Integer, Float>)fmap;
                } catch (Exception ignored) {}
            }
            // Next, if connected as a client, consult its flipOverrides map
            Map<Integer, Float> clientFlipMap = null;
            if (this.client != null) {
                try {
                    java.lang.reflect.Field cf = this.client.getClass().getDeclaredField("flipOverrides");
                    cf.setAccessible(true);
                    Object cmap = cf.get(this.client);
                    if (cmap instanceof Map) clientFlipMap = (Map<Integer, Float>)cmap;
                } catch (Exception ignored) {}
            }

            // Prefer client-side map if present, otherwise server map
            Map<Integer, Float> activeFlipMap = clientFlipMap != null ? clientFlipMap : serverFlipMap;
                        if (activeFlipMap != null && !activeFlipMap.isEmpty()) {
                for (Map.Entry<Integer, Float> e : activeFlipMap.entrySet()) {
                    try {
                        Integer owner = e.getKey(); Float scale = e.getValue();
                        if (owner == null || scale == null) continue;
                        com.cairn4.moonbase.Player remote = this.getRemotePlayer(owner);
                        if (remote == null) continue;
                        boolean applied = false;
                        try {
                            java.lang.reflect.Field pacField = com.cairn4.moonbase.Player.class.getDeclaredField("playerAnimController");
                            pacField.setAccessible(true);
                            Object pac = pacField.get(remote);
                            if (pac != null) {
                                try {
                                                java.lang.reflect.Method setForced = pac.getClass().getMethod("setForcedScale", float.class);
                                                setForced.invoke(pac, scale);
                                                // If scale is negative, also try to emulate official left-facing logic by
                                                // forcing the controller's direction/walk update to left so animations and
                                                // controller state align with the forced flip.
                                                if (scale < 0.0f) {
                                                    try {
                                                        java.lang.reflect.Method setDir = pac.getClass().getMethod("setDirection", float.class, float.class);
                                                        setDir.invoke(pac, -1.0f, 0.0f);
                                                    } catch (NoSuchMethodException nsme) {
                                                        try {
                                                            java.lang.reflect.Method updWalk = pac.getClass().getMethod("updateWalkDirection", float.class, float.class);
                                                            updWalk.invoke(pac, -1.0f, 0.0f);
                                                        } catch (NoSuchMethodException nsme2) {
                                                            // ignore if controller doesn't expose those overloads
                                                        }
                                                    }
                                                }
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
                                                applied = true;
                                            } catch (NoSuchMethodException nsme) {}
                                        }
                                    } catch (NoSuchFieldException | IllegalAccessException ignored) {}
                                }
                            } catch (Exception ignored) {}
                        }
                        if (!applied) {
                            try {
                                java.lang.reflect.Field groupField = com.cairn4.moonbase.Player.class.getDeclaredField("group");
                                groupField.setAccessible(true);
                                Object grp = groupField.get(remote);
                                if (grp instanceof com.badlogic.gdx.scenes.scene2d.Group) {
                                    ((com.badlogic.gdx.scenes.scene2d.Group)grp).setScale(scale, ((com.badlogic.gdx.scenes.scene2d.Group)grp).getScaleY());
                                }
                            } catch (Exception ignored) {}
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void render(float delta) {
        this.heatFx.setTime(delta);
        if (this.world.player != null) {
            float playerX = this.world.player.getXPos();
            float playerY = this.world.player.getYPos();
            float temp = this.world.getTempAtPosition(playerX, playerY);
            this.heatFx.setTemperature(temp);
        } else {
            this.heatFx.setTemperature(0.0f);
        }
        try {
            Arrays.sort(this.floorGroup.getChildren().begin(), this.zSortComparator);
            Arrays.sort(this.mainGroup.getChildren().begin(), this.zSortComparator);
            this.invalidZSort = false;
        }
        catch (IllegalArgumentException e) {
            Gdx.app.error("MewnBase", "Z-sort fail! Comparison method violates its general contract!");
        }
        this.mainGroup.getChildren().end();
        this.postProcessor.capture();
        this.updateCounter.start();
        this.update(delta);
        this.updateCounter.stop();
        this.worldRenderCounter.start();
        this.spriteBatch.begin();
        if (this.worldStageAct) {
            this.stage.act();
        }
        if (this.worldStageDraw) {
            this.stage.draw();
        }
        this.spriteBatch.end();
        this.postProcessor.captureEnd();
        this.postProcessor.render();
        this.world.rayHandler.setCombinedMatrix(this.b2dCam);
        this.world.rayHandler.updateAndRender();
        this.worldRenderCounter.stop();
        if (MoonBase.DEBUG_INFO) {
            shapeRenderer.setProjectionMatrix(this.camera.combined);
            this.world.enemySpawner.renderCreatureEnclosures();
            for (Entity e : this.world.entityList) {
                if (e instanceof Creature) {
                    ((Creature)e).renderPathFinding();
                }
                if (e instanceof Npc) {
                    ((Npc)e).renderPathFinding();
                }
                if (!(e instanceof Vehicle)) continue;
                ((Vehicle)e).drawDebug();
            }
            this.b2dCam.update();
            this.b2dr.render(this.world.b2dWorld, this.b2dCam.combined);
        }
        this.uiRenderCounter.start();
        this.spriteBatch.begin();
        this.worldUIStage.act();
        this.worldUIStage.draw();
        this.hudStage.act();
        this.hudStage.draw();
        if (this.inputMode == inputModes.menu && !this.menuStack.isEmpty()) {
            this.menuStack.get(this.menuStack.size() - 1).render(delta);
        }
        this.hud.performanceGraph.render(this.spriteBatch);
        this.spriteBatch.end();
        if (this.uiEdit.isOn()) {
            this.uiEdit.render(delta);
        }
        this.gifRecorder.update();
        this.game.console.draw();
        this.uiRenderCounter.stop();
        this.physicsCounter.start();
        if (this.world.gameState == World.GameStates.playing && this.physicsUpdate) {
            this.world.doPhysicsStep(delta);
        }
        this.physicsCounter.stop();
        this.updateCounter.tick();
        this.worldRenderCounter.tick();
        this.uiRenderCounter.tick();
        this.physicsCounter.tick();
        this.perfCounter.start();
        this.perfCounter.stop();
        this.perfCounter.tick();
    }

    @Override
    public void resize(int width, int height) {
        this.hudViewport.update(width, height);
        this.hudCamera.update();
        super.resize(width, height);
        for (Menu m : this.menuStack) {
            m.resize(width, height);
        }
        this.postProcessor.setViewport(new Rectangle(this.viewport.getScreenX(), this.viewport.getScreenY(), this.viewport.getScreenWidth(), this.viewport.getScreenHeight()));
        this.world.rayHandler.useCustomViewport(this.viewport.getLeftGutterWidth(), this.viewport.getBottomGutterHeight(), this.viewport.getScreenWidth(), this.viewport.getScreenHeight());
        this.game.console.refresh();
    }

    @Override
    public void showMenu(Menu menu) {
        PlayerInput.clearAll();
        this.menuStack.add(menu);
        this.inputMode = inputModes.menu;
        MoonBase.debug("GameScreen MenuStack size: " + this.menuStack.size());
        if (this.world.player != null) {
            this.world.player.handleWalkInput();
        }
    }

    @Override
    public void exitMenu() {
        if (!this.menuStack.isEmpty()) {
            this.menuStack.remove(this.menuStack.size() - 1);
        }
        MoonBase.debug("GameScreen: ExitMenu(): menu stack size is now " + this.menuStack.size());
        if (this.menuStack.isEmpty()) {
            this.inputMode = inputModes.player;
            this.setInputProcessor();
        } else {
            this.menuStack.get(this.menuStack.size() - 1).setInputFocus();
            this.menuStack.get(this.menuStack.size() - 1).returned();
        }
    }

    @Override
    public void dispose() {
        // If a multiplayer client was created via reflection, try to disconnect it cleanly
        if (this.client != null) {
            try {
                Class<?> clientClass = this.client.getClass();
                java.lang.reflect.Method disconnect = clientClass.getMethod("disconnect");
                disconnect.invoke(this.client);
            } catch (NoSuchMethodException nsme) {
                // ignore - client may not implement disconnect
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error disconnecting client", e);
            }
        }
        this.game.console.dispose();
        this.worldUIStage.dispose();
        this.hudStage.dispose();
        shapeRenderer.dispose();
        this.world.dispose();
        this.postProcessor.dispose();
        super.dispose();
    }

    @Override
    public void resume() {
        this.postProcessor.rebind();
    }

    public void addToStage(Image i, Group layerGroup) {
        layerGroup.addActor(i);
        this.invalidZSort = true;
    }

    public void addToStage(Group group, Group layerGroup) {
        layerGroup.addActor(group);
        this.invalidZSort = true;
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        if (msg.message == 2) {
            boolean inputLock = (Boolean)msg.extraInfo;
            if (inputLock) {
                this.world.gameScreen.cameraLag.setZoom(0.85f);
                this.inputMode = inputModes.dialog;
                this.hud.showLetterBox();
            } else {
                this.cameraLag.clearSpeakerFocus();
                this.world.gameScreen.cameraLag.setZoom(CameraLag.WALKZOOM);
                this.inputMode = inputModes.player;
                MoonBase.debug("GameScreen: CLEAR SPEAKER FOCUS");
                this.hud.hideLetterbox();
            }
        }
        return false;
    }

    public void debugToggleWorldUpdate(boolean worldUpdate) {
        this.worldUpdate = worldUpdate;
    }

    public void debugToggleWorldStageOptions(boolean act, boolean draw) {
        this.worldStageAct = act;
        this.worldStageDraw = draw;
    }

    public void togglePhysics(boolean physicsOn) {
        this.physicsUpdate = physicsOn;
    }

    public static enum inputModes {
        player,
        dialog,
        menu;

    }

    // Remote player management (used by Client message handling)
    public void addPlayer(int clientId, float x, float y) {
        if (this.world.player != null && this.world.player.ownerId == clientId) return; // Don't add self
        if (remotePlayers.containsKey(clientId)) return; // Already exists
        Gdx.app.log("GameScreen", "Adding remote player " + clientId);
    // incoming x,y are world pixel coordinates (client sends getXPos/getYPos)
    // Player constructor expects world tile coordinates, so convert.
    int tileX = com.badlogic.gdx.math.MathUtils.floor(x / com.cairn4.moonbase.tiles.Tile.GRID_SIZE);
    int tileY = com.badlogic.gdx.math.MathUtils.floor(y / com.cairn4.moonbase.tiles.Tile.GRID_SIZE);
    com.cairn4.moonbase.Player remotePlayer = new com.cairn4.moonbase.Player(this.world, tileX, tileY);
        try {
            // Set body position directly to pixel coordinates (important for physics sync)
            java.lang.reflect.Method setXPosMethod = com.cairn4.moonbase.Player.class.getDeclaredMethod("setXPos", float.class);
            setXPosMethod.setAccessible(true);
            setXPosMethod.invoke(remotePlayer, x);
            java.lang.reflect.Method setYPosMethod = com.cairn4.moonbase.Player.class.getDeclaredMethod("setYPos", float.class);
            setYPosMethod.setAccessible(true);
            setYPosMethod.invoke(remotePlayer, y);
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error setting remote player initial physics position", e);
        }
        try {
            java.lang.reflect.Field isRemoteField = com.cairn4.moonbase.Player.class.getDeclaredField("isRemote");
            isRemoteField.setAccessible(true);
            isRemoteField.setBoolean(remotePlayer, true);
        } catch (Exception ignored) {
            // older/newer Player may not have isRemote field
        }
        remotePlayer.ownerId = clientId;
        try {
            // initialize render/position so remote player appears at the provided pixel coordinates
            try {
                java.lang.reflect.Field targetPosField = com.cairn4.moonbase.Player.class.getDeclaredField("targetPosition");
                targetPosField.setAccessible(true);
                Object vec = targetPosField.get(remotePlayer);
                if (vec instanceof com.badlogic.gdx.math.Vector2) {
                    ((com.badlogic.gdx.math.Vector2)vec).set(x, y);
                }
            } catch (NoSuchFieldException nsf) {
                // ignore
            }
            // set actor group position immediately for visual feedback
            try {
                // group is not public; use reflection to set its position
                try {
                    java.lang.reflect.Field groupField = com.cairn4.moonbase.Player.class.getDeclaredField("group");
                    groupField.setAccessible(true);
                    Object grp = groupField.get(remotePlayer);
                    if (grp instanceof com.badlogic.gdx.scenes.scene2d.Group) {
                        ((com.badlogic.gdx.scenes.scene2d.Group)grp).setPosition(x, y - 30.0f);
                    }
                } catch (Exception ignored) {}
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error setting remote player group position", e);
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error initializing remote player position", e);
        }
        remotePlayers.put(clientId, remotePlayer);
    }

    public void removePlayer(int clientId) {
        Gdx.app.log("GameScreen", "Removing remote player " + clientId);
        com.cairn4.moonbase.Player remotePlayer = remotePlayers.remove(clientId);
        if (remotePlayer != null) {
            try {
                java.lang.reflect.Method dispose = com.cairn4.moonbase.Player.class.getMethod("dispose");
                dispose.invoke(remotePlayer);
            } catch (NoSuchMethodException nsme) {
                // ignore if not present
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error disposing remote player", e);
            }
        }
    }

    public void updatePlayerPosition(int clientId, float x, float y) {
        com.cairn4.moonbase.Player remotePlayer = remotePlayers.get(clientId);
        if (remotePlayer != null) {
            try {
                java.lang.reflect.Field targetPosField = com.cairn4.moonbase.Player.class.getDeclaredField("targetPosition");
                targetPosField.setAccessible(true);
                Object vec = targetPosField.get(remotePlayer);
                if (vec instanceof com.badlogic.gdx.math.Vector2) {
                    com.badlogic.gdx.math.Vector2 v = (com.badlogic.gdx.math.Vector2)vec;
                    v.set(x, y);
                } else {
                    // fallback: set position directly via moveToTile-like logic
                    java.lang.reflect.Method moveTo = com.cairn4.moonbase.Player.class.getMethod("moveToTile", int.class, int.class);
                    moveTo.invoke(remotePlayer, (int)(x / com.cairn4.moonbase.tiles.Tile.GRID_SIZE), (int)(y / com.cairn4.moonbase.tiles.Tile.GRID_SIZE));
                }
            } catch (NoSuchFieldException nsf) {
                // fallback: set body transform directly if possible
                try {
                    java.lang.reflect.Method setPos = com.cairn4.moonbase.Player.class.getDeclaredMethod("forcePositionUpdate");
                    setPos.setAccessible(true);
                    // attempt to set via reflection fields
                    java.lang.reflect.Field xPosF = com.cairn4.moonbase.Player.class.getDeclaredField("xPos");
                    java.lang.reflect.Field yPosF = com.cairn4.moonbase.Player.class.getDeclaredField("yPos");
                    xPosF.setAccessible(true);
                    yPosF.setAccessible(true);
                    xPosF.setFloat(remotePlayer, x);
                    yPosF.setFloat(remotePlayer, y);
                    setPos.invoke(remotePlayer);
                } catch (Exception e) {
                    // give up quietly
                }
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error updating remote player position", e);
            }
        }
    }

    public void updatePlayerAppearance(int clientId, int face, String suitColor) {
        com.cairn4.moonbase.Player remotePlayer = remotePlayers.get(clientId);
        if (remotePlayer != null) {
            try {
                remotePlayer.setCustomizationOptions(face, suitColor);
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error updating remote player appearance", e);
            }
        }
    }

    public void setPlayerDisplayName(int clientId, String displayName) {
        com.cairn4.moonbase.Player remotePlayer = remotePlayers.get(clientId);
        if (remotePlayer != null) {
            try {
                java.lang.reflect.Method setName = com.cairn4.moonbase.Player.class.getMethod("setDisplayName", String.class);
                setName.invoke(remotePlayer, displayName);
            } catch (NoSuchMethodException nsme) {
                // ignore if Player doesn't support display name
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Error setting remote player display name", e);
            }
        }
    }
}
