/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import box2dLight.RayHandler;
import com.aurelienribon.bodyeditor.BodyEditorLoader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.DefaultTimepiece;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.Timepiece;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.B2DContactListener;
import com.cairn4.moonbase.BaseManager;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.DayCycle;
import com.cairn4.moonbase.EnemySpawner;
import com.cairn4.moonbase.Fog;
import com.cairn4.moonbase.GameOverReasons;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.Lights;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.RegrowthManager;
import com.cairn4.moonbase.TerrainGen;
import com.cairn4.moonbase.Tutorial;
import com.cairn4.moonbase.WeatherManager;
import com.cairn4.moonbase.dialog.DialogController;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.Meteor;
import com.cairn4.moonbase.entities.NpcBonuses;
import com.cairn4.moonbase.entities.TemperatureDealer;
import com.cairn4.moonbase.techtree.TechManager;
import com.cairn4.moonbase.tiles.DecorativeTile;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.Lander;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.FrontendScreen;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.PausePopup;
import com.cairn4.moonbase.worlddata.ChunkLoader;
import com.cairn4.moonbase.worlddata.ItemDropperData;
import com.cairn4.moonbase.worlddata.ItemDropperFactory;
import com.cairn4.moonbase.worlddata.ItemDropperSpawnBiome;
import com.cairn4.moonbase.worlddata.PlanetGenConfig;
import com.cairn4.moonbase.worlddata.WeatherData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.cairn4.moonbase.tiles.ProtoBase;
import com.cairn4.moonbase.ui.BuildingPlacementCursor;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.tiles.ItemPile;
import java.net.URLDecoder;

public class World {
    private Timepiece timepiece;
    TerrainGen terrainGen;
    public ChunkLoader chunkLoader;
    public HashMap<String, Chunk> worldChunks = new HashMap();
    public GameScreen gameScreen;
    public Lander lander;
    public Player player;
    public GameStates gameState;
    private boolean pendingMpLoad = false;
    private long pendingMpStartMs = 0L;
    public BaseManager baseManager;
    public TechManager techManager;
    public RegrowthManager regrowthManager;
    public ArrayList<Entity> entityList = new ArrayList();
    public BodyEditorLoader bodyEditorLoader;
    public EnemySpawner enemySpawner;
    public static float METERS_PER_TILE = 4.0f;
    public long nextEntityId = 0L;
    public DayCycle dayCycle;
    public WeatherManager weatherManager;
    public PlanetGenConfig planetGenConfig;
    public Lights lights;
    public Fog fog;
    // When true, Chunk will not broadcast tile change events (used when applying remote updates)
    public boolean suppressNetworkTileEvents = false;
    public com.badlogic.gdx.physics.box2d.World b2dWorld;
    private float box2DStepAccumulator = 0.0f;
    private static final float TIME_STEP = 0.016666668f;
    public RayHandler rayHandler;
    public Sound ambientWorldSound;
    public long ambientWorldSoundId;
    public final Pool<GroundTile> groundTilePool = Pools.get(GroundTile.class);
    public static final Pool<GridPoint2> gridPointPool = Pools.get(GridPoint2.class);
    public static final Pool<Vector2> vector2Pool = Pools.get(Vector2.class);
    boolean timeUpdateOn = true;
    float timeSkipDelta = 0.0f;
    boolean needToApplyTimeskip = false;
    private boolean debugIceOnce = false;
    private boolean debugIceOverlayOnce = false;
    private float meteorTimer = 0.0f;
    private boolean mpRespawnInProgress = false;

    public Player getPlayer() {
        if (this.player != null) {
            return this.player;
        }
        return null;
    }

    public TerrainGen getTerrainGen() {
        return this.terrainGen;
    }

    public static GridPoint2 getGridPointFromPoolAndSet(int x, int y) {
        GridPoint2 gp = gridPointPool.obtain();
        gp.set(x, y);
        return gp;
    }

    public World(GameScreen gameScreen, boolean newGame) {
        this.gameScreen = gameScreen;
        this.timepiece = new DefaultTimepiece();
        GdxAI.setTimepiece(this.timepiece);
        ItemFactory.getInstance();
        this.lights = new Lights(this.gameScreen);
        this.lights.setDefaultAmbientColor(new Color(0.4f, 0.4f, 0.4f, 1.0f));
        this.gameState = GameStates.start;
        this.chunkLoader = new ChunkLoader(this);
        this.baseManager = new BaseManager();
        this.enemySpawner = new EnemySpawner(this);
        NpcBonuses.getInstance();
        NpcBonuses.init();
        this.b2dWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(0.0f, 0.0f), true);
        this.bodyEditorLoader = new BodyEditorLoader(Gdx.files.internal("physics.json"));
        this.b2dWorld.setContactListener(new B2DContactListener());
        RayHandler.useDiffuseLight(true);
        this.rayHandler = new RayHandler(this.b2dWorld);
        this.rayHandler.setCulling(true);
        this.rayHandler.diffuseBlendFunc.set(774, 0);
        this.rayHandler.shadowBlendFunc.set(0, 768);
        this.rayHandler.useCustomViewport(0, 0, gameScreen.viewport.getScreenWidth(), gameScreen.viewport.getScreenHeight());
        this.rayHandler.setShadows(true);
        this.dayCycle = new DayCycle(this);
        this.weatherManager = new WeatherManager(this);
        this.terrainGen = new TerrainGen(this);
        this.techManager = new TechManager(this);
        this.regrowthManager = new RegrowthManager(this);
        if (newGame) {
            GameScreen gameScreen2 = gameScreen;
            if (gameScreen2.game.getCurrentMission().seed == "") {
                Gdx.app.log("MewnBase", "World: Generating random seed");
                this.terrainGen.setSeed(MathUtils.random(10000));
            } else {
                try {
                    GameScreen gameScreen3 = gameScreen;
                    int s = Integer.valueOf(gameScreen3.game.getCurrentMission().seed);
                    Gdx.app.log("MewnBase", "World: Player provided seed as Integerg");
                    this.terrainGen.setSeed(s);
                }
                catch (NumberFormatException ex) {
                    Gdx.app.log("MewnBase", "World: seed is a string, hash it.");
                    GameScreen gameScreen4 = gameScreen;
                    this.terrainGen.setSeed(gameScreen4.game.getCurrentMission().seed.hashCode());
                }
            }
            this.makeEmptyWorld();
            GameScreen gameScreen5 = gameScreen;
            this.dayCycle.setDayCycleMode(gameScreen5.game.getCurrentMission().dayCycleMode);
        } else {
            // If we're a multiplayer client loading into the "multiplayer_received" slot,
            // the client may not have received the initial save blobs yet. In that case,
            // defer loading until the files arrive to avoid immediate failure.
            try {
                String cur = com.cairn4.moonbase.MoonBase.currentSaveFolder;
                if (com.cairn4.moonbase.MoonBase.isMultiplayer && "multiplayer_received".equals(cur)) {
                    Gdx.app.log("MewnBase", "World: multiplayer client waiting for initial sync before load...");
                    this.gameState = GameStates.start;
                    this.pendingMpLoad = true;
                    this.pendingMpStartMs = com.badlogic.gdx.utils.TimeUtils.millis();
                } else {
                    gameScreen.gameLoader.loadGame(this);
                    this.gameState = GameStates.playing;
                }
            } catch (Exception e) {
                Gdx.app.error("MewnBase", "World: unexpected error during load decision", e);
                try { gameScreen.gameLoader.loadGame(this); this.gameState = GameStates.playing; } catch (Exception ex) { Gdx.app.error("MewnBase", "World: fallback load failed", ex); }
            }
        }
        this.ambientWorldSound = Audio.getInstance().getSound("music/wind.ogg");
        this.ambientWorldSoundId = Audio.getInstance().playSoundLoop(this.ambientWorldSound, 0.5f, 1.0f, 0.0f);
        this.fog = new Fog(this.gameScreen, this);
    }

    private boolean multiplayerReceivedFilesReady() {
        try {
            com.badlogic.gdx.files.FileHandle g = Gdx.files.local("saves/multiplayer_received/gameSave.json");
            com.badlogic.gdx.files.FileHandle d = Gdx.files.local("saves/multiplayer_received/gameSave.data");
            if (!g.exists() && !d.exists()) {
                return false;
            }
            int pid = 0;
            try { pid = com.cairn4.moonbase.GameLoader.readPlanetIdFromSaveFolder("multiplayer_received"); } catch (Exception ignored) {}
            com.badlogic.gdx.files.FileHandle w = Gdx.files.local("saves/multiplayer_received/" + com.cairn4.moonbase.GameLoader.getWorldDataFilename(pid));
            if (w.exists()) {
                return true;
            }
            return Gdx.files.local("saves/multiplayer_received/worldData.json").exists();
        } catch (Exception ignored) {}
        return false;
    }

    private void tryDeferredMultiplayerLoad() {
        if (!this.pendingMpLoad) return;
        boolean filesReady = this.multiplayerReceivedFilesReady();
        if (!com.cairn4.moonbase.MoonBase.multiplayerSyncReady && !filesReady) {
            return;
        }
        if (filesReady) {
            try {
                this.gameScreen.gameLoader.loadGame(this);
                this.gameState = GameStates.playing;
            } catch (Exception e) {
                Gdx.app.error("MewnBase", "World: failed to load multiplayer_received after sync", e);
                this.gameState = GameStates.start;
            } finally {
                this.pendingMpLoad = false;
            }
            return;
        }
        long elapsed = com.badlogic.gdx.utils.TimeUtils.millis() - this.pendingMpStartMs;
        if (elapsed > 60000L) {
            Gdx.app.error("MewnBase", "World: timed out waiting for multiplayer_received files; returning to menu");
            this.pendingMpLoad = false;
            try {
                this.gameScreen.errorReturnToMainMenu(com.cairn4.moonbase.LoadingErrors.loadingWorldData);
            } catch (Exception ignored) {}
        }
    }

    private void makeEmptyWorld() {
        int center = 50;
        this.createChunk(center, center);
        this.chunkLoader.changeChunks(center, center);
        Chunk startingChunk = this.getChunk(center, center);
        startingChunk.clearBlockingTerrain();
        GameScreen gameScreen = this.gameScreen;
        if (gameScreen.game.getCurrentMission().missionType == Mission.MissionTypes.tutorial) {
            startingChunk.clearTiles();
            Tutorial.addTutorialBase(this);
            if (startingChunk.getTile(3, 5) != null) {
                startingChunk.getTile((int)3, (int)5).readyToRemove = true;
            }
        }
        this.lander = new Lander(this, startingChunk, 0, 6);
        this.lander.showLandingSite();
        this.lander.land();
        GameScreen gameScreen2 = this.gameScreen;
        if (gameScreen2.game.getCurrentMission().missionType != Mission.MissionTypes.tutorial) {
            this.dropSupplyCrates(startingChunk, this.lander.worldX, this.lander.worldY - 1, "supplycrate1");
            this.dropSupplyCrates(startingChunk, this.lander.worldX, this.lander.worldY - 1, "supplycrate2");
        }
    }

    private void dropSupplyCrates(Chunk startingChunk, int spawnWorldX, int spawnWorldY, String dropperId) {
        int radius = 5;
        ItemDropperData dropperData = ItemDropperFactory.getInstance().getItemDropperData(dropperId);
        boolean foundSpot = false;
        int tries = 100;
        while (!foundSpot) {
            --tries;
            int randomWorldX = MathUtils.random(spawnWorldX - radius, spawnWorldX + radius);
            int randomWorldY = MathUtils.random(spawnWorldY - radius, spawnWorldY + radius);
            if (randomWorldX != spawnWorldX && randomWorldY != spawnWorldY && this.isTileEmpty(randomWorldX, randomWorldY)) {
                GroundTile gt = this.getGroundTile(randomWorldX, randomWorldY);
                if (gt != null) {
                    for (ItemDropperSpawnBiome biome : dropperData.spawnBiomes) {
                        GroundTile.Biomes b = GroundTile.Biomes.valueOf(biome.biome);
                        if (gt.getBiome() != b) continue;
                        ItemDropperFactory.getInstance().newDropper(this, gt.chunk, gt.x, gt.y, dropperId);
                        foundSpot = true;
                    }
                } else {
                    System.out.println("no ground tile available here at " + randomWorldX + ", " + randomWorldY);
                }
            }
            if (tries > 0) continue;
            foundSpot = true;
            Gdx.app.error("MewnBase", "World: can't find valid spots to put supply crates");
        }
    }

    public void spawnPlayer(Chunk chunk, int x, int y, boolean newGame) {
        this.player = new Player(this, chunk, x, y);
        this.gameScreen.setupCameraLag(this.player);
        if (newGame) {
            Mission mission = MoonBase.getCurrentMission();
            if (mission.techTreeMode == Mission.techTreeModes.allTechUnlocked) {
                this.techManager.unlockAll();
            }
            this.player.setCustomizationOptions(mission.characterFaceOption, mission.characterSuitColor);
            if (mission.missionType != Mission.MissionTypes.tutorial) {
                // empty if block
            }
            this.player.exitLander();
        } else {
            this.gameState = GameStates.playing;
        }
    }

    public void toggleTimeSkip() {
        if (this.timeUpdateOn) {
            this.timeSkipDelta = 0.0f;
        } else {
            this.needToApplyTimeskip = true;
        }
        this.timeUpdateOn = !this.timeUpdateOn;
        MoonBase.log("TimeUpdateOn now " + this.timeUpdateOn);
    }

    public void update(float delta) {
        if (this.pendingMpLoad) {
            this.tryDeferredMultiplayerLoad();
        }
        this.tryDebugIceOnce();
        this.tryDebugIceOverlayOnce();
        this.rayHandler.setCombinedMatrix(this.gameScreen.b2dCam);
        switch (this.gameState) {
            case start: {
                if (this.lander == null) {
                    break;
                }
                this.gameScreen.camera.position.set(this.lander.getXCenter(), this.lander.getYCenter() - Tile.TILE_SIZE, 0.0f);
                this.updateChunks(delta);
                this.updatePlayerSpine(delta);
                break;
            }
            case playing: {
                int i;
                if (DialogController.getInstance().currentNpc != null) {
                    if (DialogController.getInstance().currentNpc.spineActor != null) {
                        this.timepiece.update(0.0f);
                        this.player.spineActor.update(delta);
                        DialogController.getInstance().currentNpc.spineActor.update(delta);
                    }
                } else {
                    this.timepiece.update(delta);
                }
                float worldDelta = GdxAI.getTimepiece().getDeltaTime();
                if (this.timeUpdateOn) {
                    if (this.needToApplyTimeskip) {
                        worldDelta = this.timeSkipDelta;
                        MoonBase.log("Applying time skip: " + this.timeSkipDelta);
                        this.timeSkipDelta = 0.0f;
                        this.needToApplyTimeskip = false;
                    }
                } else {
                    this.timeSkipDelta += worldDelta;
                    worldDelta = 0.0f;
                }
                this.baseManager.update(worldDelta);
                this.regrowthManager.update(worldDelta);
                this.enemySpawner.update(worldDelta);
                this.gameScreen.playerUpdateCounter.start();
                this.player.update(worldDelta);
                this.gameScreen.playerUpdateCounter.stop();
                this.dayCycle.update(worldDelta);
                this.weatherManager.update(worldDelta);
                this.updateMeteors(worldDelta);
                if (this.player.changedChunks) {
                    this.chunkLoader.changeChunks(this.player.chunkX, this.player.chunkY);
                    this.player.changedChunks = false;
                    this.updateNearbyTileVisibility();
                }
                for (i = 0; i < this.entityList.size(); ++i) {
                    this.entityList.get(i).update(worldDelta);
                }
                for (i = this.entityList.size() - 1; i >= 0; --i) {
                    if (!this.entityList.get((int)i).readyToRemove) continue;
                    if (this.entityList.get(i).getBody() != null) {
                        Body b = this.entityList.get(i).getBody();
                        MoonBase.debug("World:EntityList update: " + this.entityList.get(i).getClass().getSimpleName() + " destroying physics body");
                        this.b2dWorld.destroyBody(b);
                    }
                    this.entityList.get(i).remove();
                    this.entityList.remove(i);
                }
                this.updateChunks(worldDelta);
                this.gameScreen.playerUpdateCounter.tick();
                break;
            }
            case paused: {
                break;
            }
            case gameOver: {
                this.updatePlayerSpine(delta);
                break;
            }
            case gameOverFinished: {
                this.gameScreen.dispose();
                this.gameScreen.game.setScreen(new FrontendScreen(this.gameScreen.game));
                break;
            }
            case winMission: {
                this.gameScreen.physicsCounter.start();
                this.baseManager.update(delta);
                this.regrowthManager.update(delta);
                this.updateChunks(delta);
                break;
            }
            case winMissionFinished: {
                this.gameScreen.dispose();
                this.gameScreen.game.setScreen(new FrontendScreen(this.gameScreen.game));
            }
        }
    }

    private void updatePlayerSpine(float delta) {
        if (this.player != null && this.player.spineActor != null) {
            this.player.spineActor.update(delta);
        }
    }

    public void doPhysicsStep(float deltaTime) {
        float frameTime = Math.min(deltaTime, 0.25f);
        this.box2DStepAccumulator += frameTime;
        while (this.box2DStepAccumulator >= 0.016666668f) {
            this.b2dWorld.step(0.016666668f, 4, 2);
            this.box2DStepAccumulator -= 0.016666668f;
        }
    }

    public void resetEntityClicks() {
        for (Entity e : this.entityList) {
            e.resetClicked();
        }
    }

    public void pauseSimulation(boolean paused) {
        MoonBase.log("World: pauseSimulation -> " + paused);
        this.gameState = paused ? GameStates.paused : GameStates.playing;
    }

    public void pauseGame() {
        if (this.gameState == GameStates.playing) {
            this.pauseSimulation(true);
            this.gameScreen.showMenu(new PausePopup(this.gameScreen));
        } else {
            this.pauseSimulation(false);
        }
    }

    @Deprecated
    public void removeChunk(int chunkX, int chunkY) {
        this.worldChunks.remove("" + chunkX + "," + chunkY);
    }

    private void updateChunks(float delta) {
        for (Chunk c : this.worldChunks.values()) {
            c.updateTiles(delta);
        }
    }

    public Tile getTile(int x, int y) {
        String chunkKey = World.convertWorldTileToChunkKey(x, y);
        if (this.worldChunks.containsKey(chunkKey)) {
            Chunk c = this.worldChunks.get(chunkKey);
            GridPoint2 local = gridPointPool.obtain();
            local = World.convertWorldToLocal(local, x, y);
            Tile t = c.getTile(local.x, local.y);
            gridPointPool.free(local);
            if (t != null) {
                if (t instanceof DecorativeTile) {
                    return null;
                }
                return t;
            }
            return null;
        }
        return null;
    }

    public Tile getFloorTile(int x, int y) {
        String chunkKey = World.convertWorldTileToChunkKey(x, y);
        if (this.worldChunks.containsKey(chunkKey)) {
            Chunk c = this.worldChunks.get(chunkKey);
            GridPoint2 local = gridPointPool.obtain();
            local = World.convertWorldToLocal(local, x, y);
            Tile t = c.getFloorTile(local.x, local.y);
            gridPointPool.free(local);
            return t != null ? t : null;
        }
        return null;
    }

    public GroundTile getGroundTile(int x, int y) {
        String chunkKey = World.convertWorldTileToChunkKey(x, y);
        if (this.worldChunks.containsKey(chunkKey)) {
            Chunk c = this.worldChunks.get(chunkKey);
            GridPoint2 local = gridPointPool.obtain();
            local = World.convertWorldToLocal(local, x, y);
            GroundTile gt = c.getGroundTile(local.x, local.y);
            gridPointPool.free(local);
            if (gt != null) {
                return gt;
            }
            return null;
        }
        return null;
    }

    public boolean isTileBase(int x, int y) {
        return this.getTile(x, y) != null && this.getTile((int)x, (int)y).type == Tile.types.base;
    }

    public void gameWin() {
        this.gameScreen.exitMenu();
        this.gameState = GameStates.winMission;
        this.gameScreen.hud.gameResult(true, GameOverReasons.REASONS.success);
    }

    public void gameOver(GameOverReasons.REASONS reason) {
        if (MoonBase.isMultiplayer) {
            this.handleMultiplayerDeath(reason);
            return;
        }
        this.gameScreen.exitMenu();
        this.gameState = GameStates.gameOver;
        this.gameScreen.hud.gameResult(false, reason);
    }

    private void handleMultiplayerDeath(GameOverReasons.REASONS reason) {
        if (this.mpRespawnInProgress) return;
        this.mpRespawnInProgress = true;
        try {
            if (this.player == null) return;
            try { this.gameScreen.exitMenu(); } catch (Exception ignored) {}
            try {
                if (this.player.isDrivingVehicle()) {
                    this.player.exitVehicleRemote();
                    try {
                        this.gameScreen.cameraLag.setZoom(com.cairn4.moonbase.ui.CameraLag.WALKZOOM);
                        this.gameScreen.cameraLag.toggleBuggie();
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
            try { this.player.playerInventory.dropAllItemsAt(this.player.getX(), this.player.getY()); } catch (Exception ignored) {}
            GridPoint2 respawn = this.findRespawnTile();
            if (respawn == null) respawn = new GridPoint2(500, 500);
            try { this.ensureChunkLoadedForNetwork(respawn.x, respawn.y); } catch (Exception ignored) {}
            try { this.player.moveToTile(respawn.x, respawn.y); } catch (Exception ignored) {}
            try { this.player.forcePositionUpdate(); } catch (Exception ignored) {}
            try { this.player.playerStatus.resetForMultiplayerRespawn(); } catch (Exception ignored) {}
            try { this.player.playerAnimController.respawnReset(); } catch (Exception ignored) {}
            try {
                this.gameState = GameStates.playing;
            } catch (Exception ignored) {}
            try {
                float vx = 0f, vy = 0f;
                String payload = "POS:PLAYER:" + this.player.ownerId + ":" + this.player.getXPos() + ":" + this.player.getYPos() + ":" + vx + ":" + vy;
                com.cairn4.moonbase.NetworkHelper.sendPayload(this.gameScreen, payload);
            } catch (Exception ignored) {}
        } finally {
            this.mpRespawnInProgress = false;
        }
    }

    private GridPoint2 findRespawnTile() {
        try {
            int fallbackX = 500;
            int fallbackY = 500;
            int px = this.player != null ? this.player.getX() : fallbackX;
            int py = this.player != null ? this.player.getY() : fallbackY;
            try { this.baseManager.updateBases(this); } catch (Exception ignored) {}
            ArrayList<BaseGroup> groups = null;
            try { groups = this.baseManager.getBaseGroupList(); } catch (Exception ignored) {}
            if (groups != null && groups.size() > 0) {
                BaseModule closest = null;
                float best = Float.MAX_VALUE;
                for (BaseGroup bg : groups) {
                    if (bg == null || bg.baseModuleList == null) continue;
                    for (BaseModule bm : bg.baseModuleList) {
                        if (bm == null) continue;
                        float dx = bm.worldX - px;
                        float dy = bm.worldY - py;
                        float dist = dx * dx + dy * dy;
                        if (dist < best) {
                            best = dist;
                            closest = bm;
                        }
                    }
                }
                if (closest != null) {
                    int bx = closest.worldX;
                    int by = closest.worldY;
                    if (this.isTileEmpty(bx, by)) return new GridPoint2(bx, by);
                    ArrayList<GridPoint2> adjacent = Tile.GET_ADJACENT_COORDS(bx, by, true);
                    for (GridPoint2 gp : adjacent) {
                        if (this.isTileEmpty(gp.x, gp.y)) return gp;
                    }
                }
            }
            return new GridPoint2(fallbackX, fallbackY);
        } catch (Exception ignored) {}
        return new GridPoint2(500, 500);
    }

    public boolean isTileEmpty(int x, int y) {
        Tile t = this.getTile(x, y);
        return t == null;
    }

    public boolean isFloorEmpty(int x, int y) {
        Tile t = this.getFloorTile(x, y);
        if (t == null) {
            return true;
        }
        return t instanceof DecorativeTile;
    }

    public void updateDiscovery(int x, int y, int radius) {
        ArrayList<GridPoint2> nearby = Tile.GET_NEARBY_COORDS(x, y, radius);
        for (GridPoint2 a : nearby) {
            GroundTile gt = this.getGroundTile(a.x, a.y);
            if (gt == null || gt.isDiscovered()) continue;
            gt.setDiscovered(true);
        }
        for (int i = nearby.size() - 1; i >= 0; --i) {
            gridPointPool.free(nearby.get(i));
        }
        nearby.clear();
    }

    public void cheatDiscoverArea(int chunkRadius) {
        int cX = this.player.getCurrentChunk().chunkX;
        int cY = this.player.getCurrentChunk().chunkY;
        int minX = cX - chunkRadius;
        int maxX = cX + chunkRadius;
        int minY = cY - chunkRadius;
        int maxY = cY + chunkRadius;
        for (int x = minX; x < maxX; ++x) {
            for (int y = minY; y < maxY; ++y) {
                Chunk c = this.getChunk(x, y);
                if (c == null) {
                    c = this.createChunk(x, y);
                }
                this.cheatDiscoverEntireChunk(c);
            }
        }
    }

    private void cheatDiscoverEntireChunk(Chunk c) {
        for (GroundTile gt : c.groundTiles.values()) {
            gt.setDiscovered(true);
        }
    }

    public void updateNearbyTileVisibility() {
        for (Chunk c : this.worldChunks.values()) {
            if (!c.isVisible()) continue;
            c.updateTileVisibility();
        }
    }

    public void addTileToChunk(Tile tile, String chunkKey, int x, int y) {
        if (this.worldChunks.containsKey(chunkKey)) {
            GridPoint2 localPos = gridPointPool.obtain();
            localPos = World.convertWorldToLocal(localPos, x, y);
            Gdx.app.log("World", "addTileToChunk: world(" + x + "," + y + ") -> chunk=" + chunkKey + " local=" + localPos.x + "," + localPos.y + " tileClass=" + (tile != null ? tile.getClass().getName() : "null"));
            // Defensive: if a tile of the same class already exists at this world coord, skip to avoid duplicates
            try {
                Tile existing = this.getTile(x, y);
                if (existing != null) {
                    String existClass = existing.getClass().getName();
                    String newClass = tile != null ? tile.getClass().getName() : "null";
                    if (existClass.equals(newClass)) {
                        Gdx.app.log("World", "addTileToChunk: SKIP adding duplicate tile of class=" + newClass + " at world(" + x + "," + y + ")");
                        gridPointPool.free(localPos);
                        return;
                    } else {
                        Gdx.app.log("World", "addTileToChunk: REPLACING existing tile class=" + existClass + " with " + newClass + " at world(" + x + "," + y + ")");
                        try { existing.readyToRemove = true; } catch (Exception ignored) {}
                    }
                }
            } catch (Throwable t) {
                Gdx.app.error("World", "addTileToChunk: error checking existing tile", t);
            }
            this.worldChunks.get(chunkKey).setTile(tile, localPos.x, localPos.y);
            Gdx.app.log("World", "addTileToChunk: finished setting tile at world(" + x + "," + y + ") chunk=" + chunkKey);
        } else {
            Gdx.app.error("MoonBase", "World: Chunk " + chunkKey + " does not exist");
        }
    }

    public void removeTile(int x, int y) {
        String chunkKey = World.convertWorldTileToChunkKey(x, y);
        if (this.worldChunks.containsKey(chunkKey)) {
            GridPoint2 localPos = gridPointPool.obtain();
            localPos = World.convertWorldToLocal(localPos, x, y);
            Gdx.app.log("World", "removeTile: request world(" + x + "," + y + ") -> chunk=" + chunkKey + " local=" + localPos.x + "," + localPos.y);
            // Pass local chunk coordinates to Chunk.removeTile (Chunk expects local coords)
            this.worldChunks.get(chunkKey).removeTile(localPos.x, localPos.y);
            Gdx.app.log("World", "removeTile: finished removeTile for world(" + x + "," + y + ")");
        } else {
            Gdx.app.error("MoonBase", "World: RemoveTile: Chunk " + chunkKey + " does not exist");
        }
    }

    public static String convertWorldTileToChunkKey(int x, int y) {
        int chunkX = MathUtils.floor((float)x / 10.0f);
        int chunkY = MathUtils.floor((float)y / 10.0f);
        return "" + chunkX + "," + chunkY;
    }

    public static GridPoint2 convertWorldToLocal(GridPoint2 gp, int x, int y) {
        int chunkX = MathUtils.floor((float)x / 10.0f);
        int chunkY = MathUtils.floor((float)y / 10.0f);
        chunkX *= 10;
        chunkX = Math.abs(chunkX);
        chunkY *= 10;
        chunkY = Math.abs(chunkY);
        int resultX = x - chunkX;
        if (x < 0) {
            resultX = x + chunkX;
        }
        int resultY = y - chunkY;
        if (y < 0) {
            resultY = y + chunkY;
        }
        return gp.set(resultX, resultY);
    }

    public static GridPoint2 convertWorldPosToChunkCoord(GridPoint2 gp, int worldX, int worldY) {
        int chunkX = MathUtils.floor((float)worldX / 10.0f);
        int chunkY = MathUtils.floor((float)worldY / 10.0f);
        gp.set(chunkX, chunkY);
        return gp;
    }

    public boolean chunkExists(int chunkX, int chunkY) {
        String key = "" + chunkX + "," + chunkY;
        return this.worldChunks.containsKey(key);
    }

    public Chunk getChunk(int chunkX, int chunkY) {
        String key = "" + chunkX + "," + chunkY;
        if (this.worldChunks.containsKey(key)) {
            return this.worldChunks.get(key);
        }
        return null;
    }

    public Chunk createChunk(int x, int y) {
        Chunk c = new Chunk(this, x, y);
        this.putChunk(c);
        return c;
    }

    // Ensure a chunk exists and is loaded with saved data for network-applied changes.
    public Chunk ensureChunkLoadedForNetwork(int worldX, int worldY) {
        try {
            String chunkKey = World.convertWorldTileToChunkKey(worldX, worldY);
            if (this.worldChunks.containsKey(chunkKey)) {
                return this.worldChunks.get(chunkKey);
            }
            int ckx = Integer.parseInt(chunkKey.split(",")[0]);
            int cky = Integer.parseInt(chunkKey.split(",")[1]);
            try {
                if (this.gameScreen != null && this.gameScreen.gameLoader != null &&
                    this.gameScreen.gameLoader.worldData != null &&
                    this.gameScreen.gameLoader.worldData.chunkDataMap != null) {
                    String key = "chunk_" + ckx + "," + cky;
                    com.cairn4.moonbase.worlddata.ChunkData cd = this.gameScreen.gameLoader.worldData.chunkDataMap.get(key);
                    if (cd != null) {
                        Chunk c = new Chunk(this, cd.x, cd.y, true);
                        try {
                            if (cd.gtBiomeArray[0] == null) {
                                for (com.cairn4.moonbase.worlddata.GroundTileData gtd : cd.groundTiles.values()) {
                                    c.gtDiscoveryArray[gtd.y * 10 + gtd.x] = gtd.disovered;
                                    c.gtBiomeArray[gtd.y * 10 + gtd.x] = com.cairn4.moonbase.tiles.GroundTile.Biomes.valueOf(gtd.biome);
                                }
                            } else {
                                c.gtBiomeArray = cd.gtBiomeArray;
                                c.gtDiscoveryArray = cd.gtDiscoveryArray;
                            }
                        } catch (Exception ignored) {}
                        try { c.parseGroundTileData(cd); } catch (Exception ignored) {}
                        try { c.parseFloorTileData(cd); } catch (Exception ignored) {}
                        try { c.parseTileData(cd); } catch (Exception ignored) {}
                        try { c.setMapDirty(false); } catch (Exception ignored) {}
                        try { c.hide(); } catch (Exception ignored) {}
                        this.putChunk(c);
                        return c;
                    }
                }
            } catch (Exception ignored) {}
            return this.createChunk(ckx, cky);
        } catch (Exception ignored) {}
        return null;
    }

    // Register a network-spawned entity id so all clients use the same id space.
    public void registerNetworkEntity(com.cairn4.moonbase.entities.Entity e, long id) {
        if (e == null || id <= 0) return;
        try {
            e.id = id;
            if (this.nextEntityId < id) {
                this.nextEntityId = id;
            }
        } catch (Exception ignored) {}
    }

    public void addChunk(Chunk chunk) {
        Gdx.app.debug("MoonBase", "World: Adding loaded chunk: " + chunk.chunkX + ", " + chunk.chunkY);
        this.putChunk(chunk);
    }

    private void putChunk(Chunk c) {
        this.worldChunks.put("" + c.chunkX + "," + c.chunkY, c);
    }

    public void updateAllWalls() {
        for (Chunk c : this.worldChunks.values()) {
            c.updateAllWalls();
        }
    }

    public void dispose() {
        Iterator<Map.Entry<String, Chunk>> chunkIterator = this.worldChunks.entrySet().iterator();
        while (chunkIterator.hasNext()) {
            Map.Entry<String, Chunk> entry = chunkIterator.next();
            entry.getValue().dispose();
            chunkIterator.remove();
        }
        for (Entity e : this.entityList) {
            e.dispose();
        }
        this.weatherManager.dispose();
        this.ambientWorldSound.stop();
        try { if (this.rayHandler != null) this.rayHandler.dispose(); } catch (Exception ignored) {}
        try { if (this.b2dWorld != null) this.b2dWorld.dispose(); } catch (Exception ignored) {}
    }

    public void changeWeather(WeatherData weatherData) {
        for (Chunk c : this.worldChunks.values()) {
            c.setWeatherFx(weatherData);
        }
    }

    private void tryDebugIceOnce() {
        if (this.debugIceOnce) return;
        if (this.player == null) return;
        String prop = null;
        try { prop = System.getProperty("mewnbase.debugIce"); } catch (Exception ignored) {}
        if (prop == null || prop.trim().length() == 0) return;
        String radiusProp = null;
        int radius = 8;
        try { radiusProp = System.getProperty("mewnbase.debugIceRadius"); } catch (Exception ignored) {}
        try { if (radiusProp != null) radius = Integer.parseInt(radiusProp.trim()); } catch (Exception ignored) {}
        this.debugIceOnce = true;
        int px = (int)(this.player.getXPos() / Tile.TILE_SIZE);
        int py = (int)(this.player.getYPos() / Tile.TILE_SIZE);
        int iceCount = 0;
        float minTemp = 999f, maxTemp = -999f;
        float minAlpha = 999f, maxAlpha = -999f;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                GroundTile gt = this.getGroundTile(px + dx, py + dy);
                if (gt == null) continue;
                if (gt.getBiome() != GroundTile.Biomes.ice) continue;
                iceCount++;
                float t = gt.temperature;
                float a = gt.computeIceOverlayAlpha();
                if (t < minTemp) minTemp = t;
                if (t > maxTemp) maxTemp = t;
                if (a < minAlpha) minAlpha = a;
                if (a > maxAlpha) maxAlpha = a;
            }
        }
        GroundTile here = this.getGroundTile(px, py);
        float hereTemp = here != null ? here.temperature : 0f;
        float hereAlpha = here != null ? here.computeIceOverlayAlpha() : 0f;
        MoonBase.log("DEBUG_ICE: pos=" + px + "," + py +
            " radius=" + radius +
            " iceCount=" + iceCount +
            " temp[min,max]=[" + minTemp + "," + maxTemp + "]" +
            " alpha[min,max]=[" + minAlpha + "," + maxAlpha + "]" +
            " hereTemp=" + hereTemp +
            " hereAlpha=" + hereAlpha +
            " bias=" + GroundTile.ICE_ALPHA_BIAS +
            " scale=" + GroundTile.ICE_ALPHA_SCALE +
            " gamma=" + GroundTile.ICE_ALPHA_GAMMA +
            " override=" + GroundTile.ICE_ALPHA_OVERRIDE);
    }

    private void tryDebugIceOverlayOnce() {
        if (this.debugIceOverlayOnce) return;
        if (this.player == null) return;
        String prop = null;
        try { prop = System.getProperty("mewnbase.debugIceOverlay"); } catch (Exception ignored) {}
        if (prop == null || prop.trim().length() == 0) return;
        this.debugIceOverlayOnce = true;
        int px = (int)(this.player.getXPos() / Tile.TILE_SIZE);
        int py = (int)(this.player.getYPos() / Tile.TILE_SIZE);
        int radius = 12;
        int iceTiles = 0;
        int overlays = 0;
        int overlaysOnIce = 0;
        float minAlpha = 999f, maxAlpha = -999f;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                GroundTile gt = this.getGroundTile(px + dx, py + dy);
                if (gt == null) continue;
                boolean isIce = gt.getBiome() == GroundTile.Biomes.ice;
                if (isIce) iceTiles++;
                float a = gt.lastIceAlpha;
                if (a < minAlpha) minAlpha = a;
                if (a > maxAlpha) maxAlpha = a;
                if (gt.iceOverlayAdded) {
                    overlays++;
                    if (isIce) overlaysOnIce++;
                }
            }
        }
        MoonBase.log("DEBUG_ICE_OVERLAY: pos=" + px + "," + py +
            " radius=" + radius +
            " iceTiles=" + iceTiles +
            " overlays=" + overlays +
            " overlaysOnIce=" + overlaysOnIce +
            " alpha[min,max]=[" + minAlpha + "," + maxAlpha + "]");
    }

    public long getEntityId() {
        ++this.nextEntityId;
        return this.nextEntityId;
    }

    public Entity getEntityById(long id) {
        for (Entity e : this.entityList) {
            if (e.id != id) continue;
            return e;
        }
        return null;
    }

    public float getTileTemp(int worldX, int worldY) {
        float baseTemp = 0.5f;
        GroundTile gt = this.getGroundTile(worldX, worldY);
        if (gt != null) {
            baseTemp = gt.getTemperature();
        }
        // Apply dynamic climate offsets (day/night + weather)
        if (this.planetGenConfig != null) {
            float dayAmp = this.planetGenConfig.tempDayNightAmplitude;
            if (dayAmp != 0.0f && this.dayCycle != null) {
                float dayLight = this.dayCycle.getDayLight();
                float daySigned = (dayLight - 0.5f) * 2.0f; // -1..1
                baseTemp += daySigned * dayAmp;
            }
            float weatherInfluence = this.planetGenConfig.weatherTempInfluence;
            if (weatherInfluence != 0.0f && this.weatherManager != null) {
                float rain = this.weatherManager.getRainRate();
                float dust = this.weatherManager.getDustRate();
                float wind = this.weatherManager.getWindSpeed();
                float weatherDelta = (-rain * 0.6f) + (-dust * 0.2f) + (-wind * 0.1f);
                baseTemp += weatherDelta * weatherInfluence;
            }
        }
        return baseTemp;
    }

    public float getTempAtTile(int worldX, int worldY) {
        float xPos = (float)worldX * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f;
        float yPos = (float)worldY * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f;
        return this.getTempAtPosition(xPos, yPos);
    }

    public float getTempAtPosition(float xPos, float yPos) {
        int tileX = MathUtils.floor(xPos / Tile.TILE_SIZE);
        int tileY = MathUtils.floor(yPos / Tile.TILE_SIZE);
        float tempValue = this.getTileTemp(tileX, tileY);
        float entityHeatTotal = 0.0f;
        int heatEntities = 0;
        float highestTempEntity = 0.0f;
        for (Entity e : this.entityList) {
            if (!(e instanceof TemperatureDealer)) continue;
            ++heatEntities;
            TemperatureDealer td = (TemperatureDealer)e;
            float dist = Vector2.dst(xPos, yPos, td.getXPos(), td.getYPos());
            if (!(dist < td.radius)) continue;
            entityHeatTotal += td.getTemp(dist);
            float t = td.getTemp(dist);
            if (!(t > highestTempEntity)) continue;
            highestTempEntity = t;
        }
        return tempValue += highestTempEntity;
    }

    private void updateMeteors(float delta) {
        if (this.planetGenConfig == null) return;
        if (this.planetGenConfig.meteorRatePerMinute <= 0.0f) return;
        if (this.player == null || this.gameScreen == null) return;
        float interval = 60.0f / this.planetGenConfig.meteorRatePerMinute;
        this.meteorTimer += delta;
        if (this.meteorTimer < interval) return;
        this.meteorTimer -= interval;
        int px = this.player.getX();
        int py = this.player.getY();
        int radius = 10;
        int wx = px + MathUtils.random(-radius, radius);
        int wy = py + MathUtils.random(-radius, radius);
        new Meteor(this, this.gameScreen.mainGroup, wx, wy);
    }

    // network: handle incoming tile/item events from multiplayer
    
        public void handleIncomingNetworkEvent(String event) {
        try {
            if (event.startsWith("TILE_BUILD_START:")) {
                String[] parts = event.split(":", 8);
                if (parts.length >= 7) {
                    final int fwx = Integer.parseInt(parts[1]);
                    final int fwy = Integer.parseInt(parts[2]);
                    final String encName = parts[3];
                    final String encItemId = parts[4];
                    final int fbuildTime = Integer.parseInt(parts[5]);
                    final String encOrientation = parts[6];
                    final String className = java.net.URLDecoder.decode(encName, "UTF-8");
                    final String itemId = java.net.URLDecoder.decode(encItemId, "UTF-8");

                    com.badlogic.gdx.math.GridPoint2 local = com.cairn4.moonbase.World.getGridPointFromPoolAndSet(0,0);
                    local = com.cairn4.moonbase.World.convertWorldToLocal(local, fwx, fwy);
                    com.cairn4.moonbase.Chunk chunk = this.ensureChunkLoadedForNetwork(fwx, fwy);
                        
                    com.cairn4.moonbase.ui.BuildingPlacementCursor.ORIENTATIONS orientation = com.cairn4.moonbase.ui.BuildingPlacementCursor.ORIENTATIONS.north;
                    try { 
                        String decOrient = java.net.URLDecoder.decode(encOrientation, "UTF-8"); 
                        if (decOrient != null && decOrient.length() > 0) {
                            orientation = com.cairn4.moonbase.ui.BuildingPlacementCursor.ORIENTATIONS.valueOf(decOrient); 
                        }
                    } catch (Exception ignore) {}
                    
                    new com.cairn4.moonbase.tiles.ProtoBase(this, chunk, local.x, local.y, itemId, className, fbuildTime, orientation);
                    Gdx.app.log("World", "Applied TILE_BUILD_START from network for " + className + " at (" + fwx + "," + fwy + ")");
                }
            } else if (event.startsWith("TILE_REMOVE:")) {
                String[] parts = event.split(":");
                if (parts.length >= 3) {
                    final int fwx = Integer.parseInt(parts[1]);
                    final int fwy = Integer.parseInt(parts[2]);
                    
                    com.cairn4.moonbase.Chunk chunk = this.ensureChunkLoadedForNetwork(fwx, fwy);
                    com.cairn4.moonbase.tiles.Tile t = this.getTile(fwx, fwy);
                    if (t != null) {
                        t.readyToRemove = true;
                    }
                }
            } else if (event.startsWith("ITEM_DROP:")) {
                String[] parts = event.split(":", 5);
                if (parts.length >= 5) {
                    final int wx = Integer.parseInt(parts[1]);
                    final int wy = Integer.parseInt(parts[2]);
                    final String itemId = java.net.URLDecoder.decode(parts[3], "UTF-8");
                    final int amount = Integer.parseInt(parts[4]);

                    com.cairn4.moonbase.Chunk ch = this.ensureChunkLoadedForNetwork(wx, wy);
                    com.badlogic.gdx.math.GridPoint2 local = com.cairn4.moonbase.World.convertWorldToLocal(new com.badlogic.gdx.math.GridPoint2(0,0), wx, wy);
                    com.cairn4.moonbase.ItemStack stack = new com.cairn4.moonbase.ItemStack(itemId, amount);
                    new com.cairn4.moonbase.tiles.ItemPile(this, ch, local.x, local.y, stack);
                }
            } else if (event.startsWith("ITEM_PICKUP:")) {
                String[] parts = event.split(":", 4);
                if (parts.length >= 4) {
                    final int wx = Integer.parseInt(parts[1]);
                    final int wy = Integer.parseInt(parts[2]);
                    final String encItemId = parts[3];
                    final String itemId = java.net.URLDecoder.decode(encItemId, "UTF-8");

                    com.cairn4.moonbase.Chunk ch = this.ensureChunkLoadedForNetwork(wx, wy);
                    com.badlogic.gdx.math.GridPoint2 local = com.cairn4.moonbase.World.convertWorldToLocal(new com.badlogic.gdx.math.GridPoint2(0,0), wx, wy);
                    com.cairn4.moonbase.tiles.Tile t = ch.getTile(local.x, local.y, true);
                    if (t instanceof com.cairn4.moonbase.tiles.ItemPile) {
                        try {
                            com.cairn4.moonbase.tiles.ItemPile ip = (com.cairn4.moonbase.tiles.ItemPile)t;
                            if (ip != null) {
                                ip.markPickedUpByNetwork(itemId);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("World", "Error handling incoming network event: " + event, e);
        }
    }

    public static enum GameStates {
        start,
        playing,
        paused,
        gameOver,
        gameOverFinished,
        winMission,
        winMissionFinished;

    }
}
