/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.cairn4.moonbase.AchievementAdapter;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.GameLoader;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.PlatformAdapter;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.RichPresenceState;
import com.cairn4.moonbase.SaveFixer;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.ShaderHelper;
import com.cairn4.moonbase.UpdateCheck;
import com.cairn4.moonbase.ui.LoadingScreen;
import com.cairn4.moonbase.ui.Splash;
import com.cairn4.moonbase.worlddata.GameSaveData;
import com.strongjoshua.console.Console;
import java.util.ArrayList;

public class MoonBase
extends Game
implements Telegraph {
    public static MoonBase instance = null;
    public static final int SAVE_DATA_VERSION = 9;
    public static int SCREEN_WIDTH = 1280;
    public static int SCREEN_HEIGHT = 720;
    private static final int SCREEN_WIDTH_NORM = 1280;
    private static final int SCREEN_HEIGHT_NORM = 720;
    private static final int SCREEN_WIDTH_HD = 1920;
    private static final int SCREEN_HEIGHT_HD = 1080;
    public static final String PARTICLE_FX_ATLAS = "game.atlas";
    private static float HD_MODE_SCALER = 1.0f;
    public static float DRAW_DISTANCE = 2000.0f;
    public static boolean CULL_TILES = true;
    public static boolean GOD_MODE = false;
    public static boolean SPAWN_CREATURES = true;
    public static final boolean SPAWN_NPCS = false;
    public static boolean NO_FOG = true;
    public static boolean DEBUG_INFO = false;
    public static boolean PERF_GRAPH = false;
    public static final String VERSION = "1.0.1";
    public static final String BUILD_VERSION = "Build 1";
    public static final String BUILD_DATE = "11/19/2023";
    public static final boolean RELEASE_CANDIDATE = false;
    public static final boolean TEST_VERSION = false;
    public static boolean NEEDS_RESTART = false;
    public static boolean STEAM_VERSION = true;
    public static boolean DISCORD_ON = false;
    public static final String discordInviteURL = "https://discord.gg/mewnbase";
    public static String currentSaveFolder = null;
    public static boolean isMultiplayer = false;
    public static String multiplayerHost = "127.0.0.1";
    public static int multiplayerPort = 7777;
    // multiplayer nickname (set via menu or console)
    public static String multiplayerNick = System.getProperty("user.name", "Player");
    public static String coreFolder = "";
    public static boolean encodedSaves = false;
    public static boolean INSTANT_RUN = false;
    public static boolean SERVER_ONLY = false;
    public static boolean HEADLESS = false;
    // Pending planet switch data (single-player only)
    public static class PendingPlanetSwitch {
        public int planetId;
        public String planetName;
        public String planetType;
        public PendingPlanetSwitch(int planetId, String planetName, String planetType) {
            this.planetId = planetId;
            this.planetName = planetName;
            this.planetType = planetType;
        }
    }
    public static PendingPlanetSwitch pendingPlanetSwitch = null;
    public static com.cairn4.moonbase.worlddata.PlayerData pendingPlayerTransfer = null;
    public static java.util.ArrayList<String> pendingUnlockedTech = null;
    public static int pendingTechSamples = 0;
    public PlatformAdapter platformAdapter;
    public static AchievementAdapter achievementAdapter;
    public Color clearColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    public Console console;
    private static Mission mission;
    public String prevScreenClass;
    private static Cursor cursorNormal;
    private static Cursor cursorInteract;
    private static Cursor cursorHidden;
    private static Boolean cursorState;
    private static Boolean cursorVisible;
    public ArrayList<String> saveFilesWithErrors;
    public ArrayList<String> saveFilesThatMightBeFixable;

    public void setMission(Mission mission) {
        MoonBase.mission = mission;
    }

    public static Mission getCurrentMission() {
        return mission;
    }

    public MoonBase(PlatformAdapter a, AchievementAdapter achievementAdapter, String coreFolder) {
        this.platformAdapter = a;
        MoonBase.achievementAdapter = achievementAdapter;
        MoonBase.coreFolder = coreFolder;
        MoonBase.instance = this;
    }

    @Override
    public void create() {
        // Optional file logger to avoid stdout buffering when redirected.
        try {
            String logFile = System.getProperty("mewnbase.logfile");
            if (logFile != null && !logFile.trim().isEmpty()) {
                Gdx.app.setApplicationLogger(new com.cairn4.moonbase.debug.FileLogger(logFile.trim()));
                Gdx.app.log("MewnBase", "File logger enabled: " + logFile.trim());
            }
        } catch (Throwable t) {
            try { Gdx.app.error("MewnBase", "Failed to enable file logger", t); } catch (Exception ignored) {}
        }
        MoonBase.log("MoonBase.coreFolder = " + coreFolder);
        if (!Gdx.files.local(coreFolder).exists()) {
            GdxRuntimeException e = new GdxRuntimeException("Can't find the data folder here:" + coreFolder);
            throw e;
        }
        try {
            String headless = System.getProperty("mewnbase.headless");
            String serverOnly = System.getProperty("mewnbase.serverOnly");
            if ((headless != null && (headless.equals("1") || headless.equalsIgnoreCase("true"))) ||
                (serverOnly != null && (serverOnly.equals("1") || serverOnly.equalsIgnoreCase("true")))) {
                HEADLESS = true;
            }
        } catch (Exception ignored) {}
        try {
            SERVER_ONLY = "1".equals(System.getProperty("mewnbase.serverOnly"));
            if (SERVER_ONLY) {
                DISCORD_ON = false;
                if (!INSTANT_RUN) {
                    INSTANT_RUN = true;
                }
                Gdx.app.log("MoonBase", "Server-only mode enabled");
            }
        } catch (Exception ignored) {}
        // Optional dangerous runtime eval for local debugging only.
        try {
            com.cairn4.moonbase.debug.RuntimeEval.setContext(this);
            com.cairn4.moonbase.debug.RuntimeEval.startIfEnabled();
        } catch (Throwable t) {
            try { Gdx.app.error("RuntimeEval", "Failed to init runtime eval", t); } catch (Exception ignored) {}
        }
        try {
            com.cairn4.moonbase.debug.UiTestServer.startIfEnabled();
        } catch (Throwable t) {
            try { Gdx.app.error("UiTest", "Failed to init UI test server", t); } catch (Exception ignored) {}
        }
            // Check for JVM system properties to allow command-line automation.
            // Supported properties (pass with -Dmewnbase.save=saveName etc):
            // -Dmewnbase.save=<saveFolder>    -> set the current save to load
            // -Dmewnbase.startServer=<port>   -> start an in-process server on the given port
            // -Dmewnbase.connect=<host:port>  -> auto-connect client to host:port
            try {
                String startServer = System.getProperty("mewnbase.startServer");
                if ((startServer == null || startServer.isEmpty()) && HEADLESS) {
                    System.setProperty("mewnbase.startServer", "7777");
                    startServer = "7777";
                }
                if (startServer != null && !startServer.isEmpty()) {
                    try {
                        int port = Integer.parseInt(startServer);
                        // Create and start an in-process server and wait until it's listening.
                        final int serverPort = port;
                        try {
                            Server s = new Server(serverPort);
                            s.start();
                            // Wait for listening with timeout (max 3 seconds)
                            int waited = 0;
                            int pollMs = 100;
                            while (!s.isListening() && waited < 3000) {
                                try { Thread.sleep(pollMs); } catch (InterruptedException ie) { break; }
                                waited += pollMs;
                            }
                            if (s.isListening()) {
                                Gdx.app.log("MoonBase", "Auto-started server on port " + serverPort + " (listening)");
                            } else {
                                Gdx.app.error("MoonBase", "Auto-started server on port " + serverPort + " but it is not yet listening after timeout");
                            }
                        } catch (Exception e) {
                            Gdx.app.error("MoonBase", "Failed to auto-start server", e);
                        }
                    } catch (NumberFormatException nfe) {
                        Gdx.app.error("MoonBase", "Invalid port for mewnbase.startServer: " + startServer, nfe);
                    }
                }

                String connect = System.getProperty("mewnbase.connect");
                boolean connectRequested = false;
                if (connect != null && !connect.isEmpty()) {
                    // Expect host:port
                    String[] parts = connect.split(":");
                    if (parts.length == 2) {
                        String host = parts[0];
                        try {
                            int port = Integer.parseInt(parts[1]);
                            if (port < 1 || port > 65535) {
                                throw new NumberFormatException("port out of range");
                            }
                            connectRequested = true;
                            // Route legacy connect args through the new menu-based flow.
                            System.setProperty("mewnbase.autoMenu", "multiplayer");
                            System.setProperty("mewnbase.mp.host", host);
                            System.setProperty("mewnbase.mp.port", Integer.toString(port));
                            if (System.getProperty("mewnbase.mp.autoconnect") == null) {
                                System.setProperty("mewnbase.mp.autoconnect", "1");
                            }
                            Gdx.app.log("MoonBase", "Legacy connect mapped to menu auto-connect: " + host + ":" + port);
                        } catch (NumberFormatException nfe) {
                            Gdx.app.error("MoonBase", "Invalid port in mewnbase.connect: " + connect, nfe);
                        }
                    } else {
                        Gdx.app.error("MoonBase", "Invalid format for mewnbase.connect (expected host:port): " + connect);
                    }
                }

                String save = System.getProperty("mewnbase.save");
                if (save != null && !save.isEmpty()) {
                    if (connectRequested) {
                        Gdx.app.log("MoonBase", "Ignoring mewnbase.save for legacy connect; using menu-based multiplayer flow.");
                    } else {
                        currentSaveFolder = save;
                        INSTANT_RUN = true; // trigger immediate load of the save
                        Gdx.app.log("MoonBase", "Auto-set save folder from JVM property: " + save);
                    }
                } else if (HEADLESS) {
                    INSTANT_RUN = true;
                }
            } catch (Throwable t) {
                // Avoid breaking startup if system properties are inaccessible
                Gdx.app.error("MoonBase", "Error while reading JVM properties for auto-start", t);
            }
        this.platformAdapter.packTextures();
        Gdx.gl.glClearColor(this.clearColor.r, this.clearColor.g, this.clearColor.b, this.clearColor.a);
        SettingsLoader.getInstance();
        this.loadDisplaySetings();
        this.loadFrontendAssets();
        ShaderHelper.load();
        cursorNormal = Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("cursor-normal.png")), 5, 5);
        cursorInteract = Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("cursor-gold.png")), 5, 5);
        cursorHidden = Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("cursor-hidden.png")), 5, 5);
        cursorState = true;
        MoonBase.setCursor(false);
        Audio.setup(this);
        if (!STEAM_VERSION) {
            UpdateCheck.sendRequest();
        }
        UpdateCheck.whatsNewCheck();
        SaveFixer.moveSaveFolder();
        this.saveFixCheck();
        if (INSTANT_RUN) {
            ArrayList<GameSaveData> saves = GameLoader.getSavedGames();
            if (saves.size() > 0) {
                AssetManagerSingleton.getInstance().load("playerheads.atlas", TextureAtlas.class);
                AssetManagerSingleton.getInstance().load("loading.atlas", TextureAtlas.class);
                AssetManagerSingleton.getInstance().load("loading-bg.png", Texture.class);
                AssetManagerSingleton.getInstance().finishLoading();
                // If a specific save was requested via JVM property, prefer it.
                if (currentSaveFolder == null || currentSaveFolder.isEmpty()) {
                    currentSaveFolder = saves.get(0).saveFolder;
                } else {
                    boolean found = false;
                    for (GameSaveData gsd : saves) {
                        if (gsd.saveFolder.equals(currentSaveFolder)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        MoonBase.error("INSTANT RUN: requested save not found: " + currentSaveFolder + " - falling back to first save");
                        currentSaveFolder = saves.get(0).saveFolder;
                    }
                }
                this.setScreen(new LoadingScreen(this, false));
            } else {
                MoonBase.error("INSTANT RUN: no saves to load...");
                this.setScreen(new Splash(this));
            }
        } else {
            this.setScreen(new Splash(this));
        }
        if (STEAM_VERSION) {
            this.platformAdapter.steamInit();
        }
        String os = System.getProperty("os.name");
        MoonBase.log(os);
        if (os.toLowerCase().indexOf("mac") >= 0) {
            DISCORD_ON = false;
        }
        if (HEADLESS) {
            DISCORD_ON = false;
        }
        if (DISCORD_ON) {
            this.platformAdapter.initRPC();
            RichPresenceState rpstate = new RichPresenceState();
            rpstate.state = RichPresenceState.States.menu;
            this.platformAdapter.updateRPC(rpstate);
        }
        MessageManager.getInstance().addListener(this, 999);
        MessageManager.getInstance().addListener(this, 1000);
    }

    @Override
    public void setScreen(Screen screen) {
        if (this.getScreen() != null) {
            this.prevScreenClass = this.getScreen().getClass().getSimpleName();
        }
        super.setScreen(screen);
        if (this.prevScreenClass != null) {
            MoonBase.log("prev screen now : " + this.prevScreenClass);
        }
    }

    public void saveFixCheck() {
        this.saveFilesWithErrors = GameLoader.getSavesWithErrors();
        this.saveFilesThatMightBeFixable = SaveFixer.testEntityNameFix(this.saveFilesWithErrors);
    }

    public void loadFrontendAssets() {
        AssetManagerSingleton.getInstance().load("frontend.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("menu.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("static.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("smallfont1.fnt", BitmapFont.class);
        AssetManagerSingleton.getInstance().load("headingfont1.fnt", BitmapFont.class);
        AssetManagerSingleton.getInstance().load("MissionBg.png", Texture.class);
        AssetManagerSingleton.getInstance().load("menubg/space-bg.png", Texture.class);
        AssetManagerSingleton.getInstance().load("light.png", Texture.class);
        AssetManagerSingleton.getInstance().load("menubg/planet.png", Texture.class);
        AssetManagerSingleton.getInstance().load("menubg/planet-atmosphere.png", Texture.class);
        AssetManagerSingleton.getInstance().load("menubg/planet-circle.png", Texture.class);
        AssetManagerSingleton.getInstance().load("largeQuad.png", Texture.class);
        AssetManagerSingleton.getInstance().load("music/enchanted_tiki_86_0.mp3", Music.class);
        AssetManagerSingleton.getInstance().load("sounds/gameStart.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/menuForward_gameStart.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/menuForward1.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/menuBack.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/menubutton_down.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/menubutton_up.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/textAnimator2.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("bg-cloud1.png", Texture.class);
        AssetManagerSingleton.getInstance().load("bg-cloud2.png", Texture.class);
        AssetManagerSingleton.getInstance().finishLoading();
    }

    public void loadGameAssets() {
        AssetManagerSingleton.getInstance().load(PARTICLE_FX_ATLAS, TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("walls.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("static.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("Tiles/tiles.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("Tiles/ice.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("researchobjects.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("playerheads.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("techtree.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("minimap-blank.png", Texture.class);
        AssetManagerSingleton.getInstance().load("map-grid.png", Texture.class);
        AssetManagerSingleton.getInstance().load("sounds/gameOverButton.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("music/wind.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("music/wind_indoors.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/rain.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/sandstorm.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/airleak.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/commstower.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/drone-wakeup.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/crafter-metal.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/crafter-bubbles.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/footstep0.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/footstep1.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/footstep_indoors0.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/footstep_indoors1.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/footstep-mud.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/footstep-wood.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/footstep-pavement.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/lander-rocketdown.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/lander-hitground.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/repair.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/poweroff.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/refillWaterSupply.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/jackhammer.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/pop.mp3", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/food_chew.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/placeObject.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/interact_hit2.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/engine-loop.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/buggie-crash.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/buggie-crash2.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/gameStart.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/menuBack.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/firestart.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/buildComplete.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/techUnlocked.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/warningPulse.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/thunder-distant1.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/thunder-near1.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/electric-zap.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/hungry.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/storage-open.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/storage-close.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/tool-break.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/lowair-breath.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/woodchop1.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/woodchop2.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/rockhit1.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/rockhit2.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/icehit1.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/icehit2.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/planthit1.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/planthit2.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/planthit3.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/crafting-arm-1.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/crafting-arm-2.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/harvester-grind.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/punch1.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/bloodSplat.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/bloodSplat2.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/bloodSplat3.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/paintbrush-splat.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/research_start.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/research_complete.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/shovel-swing-hit.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/shovel-swing.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/shovel-hit.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/gate-open.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/miningrig-collected.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/rocket-launch.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/countdown-blip.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/launchpad-towers-open.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/launchpad-tower1.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/launchpad-tower2.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/rocket-door-close.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/launchpad-fuelup.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/launchpad-build-loop.ogg", Sound.class);
        AssetManagerSingleton.getInstance().load("sounds/launchpad-build-complete.ogg", Sound.class);
        ParticleEffectLoader.ParticleEffectParameter p = new ParticleEffectLoader.ParticleEffectParameter();
        p.atlasFile = PARTICLE_FX_ATLAS;
        AssetManagerSingleton.getInstance().load("particles/dust.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/rain.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/sandstorm.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/lander-smoke.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/airleak.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/windturbine-broken.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/item-dropper.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/item-dropper-generic.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/item-dropper-hit.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/solarpanel-clean.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/bloodsplat.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/buggie_dust.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/itemBlowAway.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/construction-sparks.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/miningrig-sparks.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/lightning-prestrike.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/meteor-explosion.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/meteor-flames.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/smelter.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/smelter-fire.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/torch.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/build-dust.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/chemistrylab.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/volcano.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/geyser-smoke.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/lava-spray.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/player-eat.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/player-fire.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/lander-ready.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/lander-takeoff.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/repair-bolts.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/repair-instant.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/recharge-instant.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/tank-drill-dust.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/tank-drill-sparks.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/lavaflow.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/rocket-fueled.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/launchpad-flames.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/paintbrush-splat.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("particles/plant-finished.p", ParticleEffect.class, p);
        AssetManagerSingleton.getInstance().load("constructionyard/skeleton.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("advancedfabricator/skeleton.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("miningrig/skeleton.atlas", TextureAtlas.class);
    }

    private void loadMusicTracks() {
        AssetManagerSingleton.getInstance().load("music/cynicmusic-mysterious-ambience-song21.mp3", Music.class);
        AssetManagerSingleton.getInstance().load("music/dfs/dfs - In search for.mp3", Music.class);
        AssetManagerSingleton.getInstance().load("music/dfs/dfs - Onirik.mp3", Music.class);
        AssetManagerSingleton.getInstance().load("music/dfs/dfs - Stars above.mp3", Music.class);
        AssetManagerSingleton.getInstance().load("music/gs/gs - Middle.mp3", Music.class);
        AssetManagerSingleton.getInstance().load("music/gs/gs - Early.mp3", Music.class);
        for (int i = 1; i <= 10; ++i) {
            String musicPath = "music/pps/pps_sfa2_" + i + ".mp3";
            AssetManagerSingleton.getInstance().load(musicPath, Music.class);
        }
    }

    public static void setCursor(boolean interact) {
        if (cursorVisible.booleanValue()) {
            if (interact) {
                if (!cursorState.booleanValue()) {
                    Gdx.graphics.setCursor(cursorInteract);
                    cursorState = true;
                }
            } else if (cursorState.booleanValue()) {
                Gdx.graphics.setCursor(cursorNormal);
                cursorState = false;
            }
        }
    }

    public static void setCursorVisible(boolean show) {
        cursorVisible = show;
        if (cursorVisible.booleanValue()) {
            Gdx.graphics.setCursor(cursorNormal);
        } else {
            Gdx.graphics.setCursor(cursorHidden);
        }
    }

    @Override
    public void render() {
        if (STEAM_VERSION) {
            this.platformAdapter.steamAPIupdate();
        }
        Gdx.gl.glClear(16384);
        super.render();
    }

    private void loadDisplaySetings() {
        this.setVsync(SettingsLoader.getInstance().settingsData.VSYNC);
        this.setHdMode(SettingsLoader.getInstance().settingsData.FULLHD);
        if (SettingsLoader.getInstance().settingsData.FULL_SCREEN) {
            try {
                Graphics.DisplayMode d = Gdx.graphics.getDisplayMode(Gdx.graphics.getPrimaryMonitor());
                Gdx.graphics.setFullscreenMode(d);
            }
            catch (Exception e) {
                MoonBase.error("Error switching to full screen mode");
            }
        }
    }

    public void toggleFullScreen() {
        PlayerInput.clearAll();
        if (!Gdx.graphics.isFullscreen()) {
            try {
                MoonBase.debug("trying to set to full screen");
                Graphics.DisplayMode d = Gdx.graphics.getDisplayMode(Gdx.graphics.getPrimaryMonitor());
                Gdx.graphics.setFullscreenMode(d);
                SettingsLoader.getInstance().settingsData.FULL_SCREEN = true;
                SettingsLoader.getInstance().save();
            }
            catch (Exception e) {
                MoonBase.error("Error switching to full screen mode");
            }
        } else {
            this.setWindowedMode();
        }
        Gdx.graphics.setVSync(SettingsLoader.getInstance().settingsData.VSYNC);
    }

    public void toggleFullHd() {
    }

    public void setVsync(boolean newState) {
        Gdx.graphics.setVSync(newState);
        MoonBase.log("Vsync: " + newState);
        SettingsLoader.getInstance().settingsData.VSYNC = newState;
        SettingsLoader.getInstance().save();
    }

    public void setHdMode(boolean newState) {
        MoonBase.log("Using HD mode: " + newState);
        if (newState) {
            SCREEN_WIDTH = 1920;
            SCREEN_HEIGHT = 1080;
            HD_MODE_SCALER = 1.5f;
        } else {
            SCREEN_WIDTH = 1280;
            SCREEN_HEIGHT = 720;
            HD_MODE_SCALER = 1.0f;
        }
    }

    public void saveHdMode(boolean newState) {
        SettingsLoader.getInstance().settingsData.FULLHD = newState;
        SettingsLoader.getInstance().save();
        MoonBase.log("Saved HD Mode = " + newState);
        NEEDS_RESTART = true;
    }

    public void setFullScreen(int mode) {
        Graphics.DisplayMode[] modes2 = Gdx.graphics.getDisplayModes(Gdx.graphics.getPrimaryMonitor());
        MoonBase.log("trying to setFullScreen mode " + mode);
        if (mode < modes2.length) {
            Gdx.graphics.setFullscreenMode(modes2[mode]);
            SettingsLoader.getInstance().settingsData.FULL_SCREEN = true;
            SettingsLoader.getInstance().settingsData.DISPLAY_MODE = mode;
            SettingsLoader.getInstance().save();
        } else {
            MoonBase.error("invalid display mode index");
            this.setWindowedMode();
        }
        for (Graphics.DisplayMode d : Gdx.graphics.getDisplayModes()) {
            MoonBase.debug(" - mode: " + d.toString());
        }
    }

    public void setWindowedMode() {
        MoonBase.log("Changing to Windowed Mode");
        Gdx.graphics.setWindowedMode(SCREEN_WIDTH, SCREEN_HEIGHT);
        SettingsLoader.getInstance().settingsData.FULL_SCREEN = false;
        SettingsLoader.getInstance().save();
    }

    public static float GET_SCREENSIZE_MULTIPLIER() {
        return HD_MODE_SCALER;
    }

    public static void log(String s) {
        Gdx.app.log("MewnBase", s);
    }

    public static void debug(String s) {
        Gdx.app.debug("MewnBase", s);
    }

    public static void error(String s) {
        Gdx.app.error("MewnBase", s);
    }

    public static void setDebugInfo(boolean b) {
        DEBUG_INFO = b;
        MessageManager.getInstance().dispatchMessage(1, DEBUG_INFO);
    }

    public void setPerfGraph(boolean v) {
        PERF_GRAPH = v;
    }

    @Override
    public void dispose() {
        try {
            com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
            if (s != null) {
                s.stop();
            }
        } catch (Exception ignored) {}
        AssetManagerSingleton.getInstance().clear();
        AssetManagerSingleton.getInstance().dispose();
        this.platformAdapter.dispose();
        super.dispose();
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        if (msg.message == 999 && this.console != null) {
            this.console.log((String)msg.extraInfo);
        }
        if (msg.message == 1000) {
            this.platformAdapter.writeErrorLog((Throwable)msg.extraInfo);
        }
        return false;
    }

    static {
        cursorVisible = true;
    }
}
