/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BaseGroup;
import com.cairn4.moonbase.DayCycle;
import com.cairn4.moonbase.GameOverReasons;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemActions;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.PlayerStatus;
import com.cairn4.moonbase.PlayerStatusEffect;
import com.cairn4.moonbase.ResourceIndicator;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.Tutorial;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Creature;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.entities.VehicleBattery;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.Lander;
import com.cairn4.moonbase.tiles.LaunchPad;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.BaseStatsInfo;
import com.cairn4.moonbase.ui.BuildingPlacementCursor;
import com.cairn4.moonbase.ui.ColorMixer;
import com.cairn4.moonbase.ui.CommsUI;
import com.cairn4.moonbase.ui.DamageFlyoff;
import com.cairn4.moonbase.ui.GameOverMenu;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.GameWinMenu;
import com.cairn4.moonbase.ui.HudInventory;
import com.cairn4.moonbase.ui.HudNotifications;
import com.cairn4.moonbase.ui.HudWaypoints;
import com.cairn4.moonbase.ui.ItemButton;
import com.cairn4.moonbase.ui.LaunchControls;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.PaintSelector;
import com.cairn4.moonbase.ui.PerformanceGraph;
import com.cairn4.moonbase.ui.ReturnToLanderUI;
import com.cairn4.moonbase.ui.StatusEffectUI;
import com.cairn4.moonbase.ui.TechTreePopup;
import com.cairn4.moonbase.ui.TilePickupProgressBar;
import com.cairn4.moonbase.ui.TileProgressBar;
import com.cairn4.moonbase.ui.ToolTipWatchable;
import com.cairn4.moonbase.ui.Tooltip;
import com.cairn4.moonbase.ui.TutorialUI;
import com.cairn4.moonbase.ui.VirtualJoystick;
import com.cairn4.utils.controller.JoystickTestUI;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Hud
extends Menu
implements Observer {
    public ArrayList<DamageFlyoff> damageFlyoffList = new ArrayList();
    public final Pool<DamageFlyoff> damageFlyoffPool = Pools.get(DamageFlyoff.class);
    public final int DAMAGE_FLYOFF_X_OFFSET = 10;
    public static final int SPRITE_SCALE = 4;
    private final float hudTimerAlpha = 0.75f;
    public GameScreen gameScreen;
    World world;
    Player player;
    Group menuGroup;
    Group hudGroup;
    Group inventoryGroup;
    Group worldInfoGroup;
    public TutorialUI tutorialUI;
    public LaunchControls launchControls;
    Image letterboxTop;
    Image letterboxBottom;
    Group lowHealthGroup;
    Image lowHealthBorder;
    private Group topLeft;
    private Image topLeftBg;
    private Group topRight;
    private Image topRightBg;
    private ProgressBar.ProgressBarStyle progressBarStyle;
    private ProgressBar.ProgressBarStyle progressBarStyleRight;
    private ProgressBar.ProgressBarStyle oxygenProgressBarStyle;
    private ProgressBar.ProgressBarStyle powerProgressBarStyle;
    private ProgressBar.ProgressBarStyle hungerProgressBarStyle;
    private ProgressBar.ProgressBarStyle healthProgressBarStyle;
    private ProgressBar.ProgressBarStyle boostLeftBarStyle;
    private ProgressBar.ProgressBarStyle boostRightBarStyle;
    private Group airGroup;
    private Image airIcon;
    private Image airBarBorder;
    private ProgressBar airProgress;
    private ProgressBar airProgressIncrease;
    private Image airBarBorderGlow;
    private boolean displayedLowAirFlash = false;
    private boolean displayedLowAirFlash2 = false;
    private Image hungerBarBorderGlow;
    private boolean displayedLowHungerFlash = false;
    private Group powerGroup;
    private Image powerIcon;
    private ProgressBar powerProgress;
    private ProgressBar powerProgressIncrease;
    private Image powerBarBorder;
    private Group healthGroup;
    private Image healthIcon;
    private Image healthBarBorder;
    private ProgressBar healthProgress;
    private ProgressBar healthProgressIncrease;
    private Image healthChangeIndicator;
    private Group hungerGroup;
    private Image hungerIcon;
    private Image hungerBarBorder;
    private ProgressBar hungerProgress;
    private ProgressBar hungerProgressIncrease;
    Label playerStateLabel;
    private Group vehicleBatGroup;
    private Image vehicleBatBg;
    private Image vehicleBatNeedle;
    private Label vehicleStatusLabel;
    private Image vehicleChargeIndicator;
    private ProgressBar.ProgressBarStyle dayCycleProgressStyle;
    private ProgressBar dayCycleProgress;
    private Image dayIcon;
    private Group persistantDayGroup;
    private Label dayTimeLabel;
    private VirtualJoystick virtualJoystick;
    public Image vignette;
    private DayIconStates dayIconState;
    BaseStatsInfo baseStatsInfo;
    CommsUI commsUI;
    Label inventoryLabel;
    Image overlayTint;
    Label gameOverLabel;
    Label gameOverReasonLabel;
    Group dayGroup;
    Label dayLabel;
    Label daysLeftLabel;
    Label clockLabel;
    Label debugLabel;
    public HudInventory hudInventory;
    private Image flashlightIcon;
    private Image flashlightIconOn;
    private Button mapButton;
    private Button menuButton;
    private Button missionButton;
    private Group researchButtonWrapper;
    public Button researchButton;
    private AdditiveImage researchPulseFx;
    private Label researchPointLabel;
    Label duskWarningLabel;
    ArrayList<ResourceIndicator> resourceIndicatorList = new ArrayList();
    ArrayList<TileProgressBar> tileProgressBars = new ArrayList();
    BuildingPlacementCursor buildingPlacementCursor;
    Tooltip tooltip;
    ItemButton equippedItem;
    public HudNotifications hudNotifications;
    public HudWaypoints hudWaypoints;
    private Group discoveryGroup;
    private Label newDiscoveryLabel;
    private Label sciencePointsLabel;
    private Label suitPowerOfflineLabel;
    private boolean buildingEquipped;
    private Image airWarningIcon;
    private GridPoint2 interactCursorPoint;
    private Tile cursorTile;
    Image interactCursor;
    Group statusEffectGroup;
    public ArrayList<StatusEffectUI> statusEffectUIList = new ArrayList();
    private Image fullScreenFader;
    private Label creatureLabel;
    private String debugText;
    private JoystickTestUI joystickTestUI;
    public PerformanceGraph performanceGraph;
    private String prevDayTimeString;
    private String dayTimeString;
    private String dayString;
    private String prevHourString;
    private String clockString;
    private float dateTimeUpdate;
    private StringBuilder dateTimeSB;
    private Vector2 entityLabelMouse;
    private Circle entityEllipse;
    private Vector2 entityEllipsePos;
    ReturnToLanderUI returnUI;
    public boolean showingPaintSelector = false;
    private boolean activated = false;

    public boolean isHudVisible() {
        return this.hudGroup.isVisible();
    }

    public Hud(GameScreen gameScreen, World world) {
        super(gameScreen);
        this.world = world;
        // Ensure we only attach to a valid player instance when present
        if (world.player != null) {
            this.player = world.player;
            try { this.player.addObserver(this); } catch (Exception ignored) {}
        } else {
            this.player = null;
        }
        this.dateTimeSB = new StringBuilder();
        this.gameScreen = gameScreen;
        this.menuGroup = new Group();
        this.gameScreen.hudStage.addActor(this.menuGroup);
        this.hudGroup = new Group();
        this.hudGroup.getColor().a = 0.0f;
        this.gameScreen.hudStage.addActor(this.hudGroup);
        this.inventoryGroup = new Group();
        this.gameScreen.hudStage.addActor(this.inventoryGroup);
        this.worldInfoGroup = new Group();
        this.gameScreen.worldUiGroup.addActor(this.worldInfoGroup);
        this.hudInventory = new HudInventory(this);
        this.hudNotifications = new HudNotifications(this, this.hudGroup);
        this.progressBarStyle = new ProgressBar.ProgressBarStyle();
        this.progressBarStyle.knobAfter = this.skin.getDrawable("hud-progress-empty");
        this.progressBarStyle.knobBefore = this.skin.getDrawable("hud-progress-filled");
        this.progressBarStyle.knob = this.skin.getDrawable("hud-progress-knob");
        this.progressBarStyleRight = new ProgressBar.ProgressBarStyle();
        this.progressBarStyleRight.knobAfter = this.skin.getDrawable("hud-progress-filled");
        this.progressBarStyleRight.knobBefore = this.skin.getDrawable("hud-progress-empty");
        this.progressBarStyleRight.knob = this.skin.getDrawable("hud-progress-knob");
        this.oxygenProgressBarStyle = new ProgressBar.ProgressBarStyle();
        this.oxygenProgressBarStyle.knobAfter = this.skin.getDrawable("hud-progress-blank");
        this.oxygenProgressBarStyle.knobBefore = this.skin.getDrawable("hud-progress-blue-filled");
        this.oxygenProgressBarStyle.knob = this.skin.getDrawable("hud-progress-knob");
        this.healthProgressBarStyle = new ProgressBar.ProgressBarStyle();
        this.healthProgressBarStyle.knobAfter = this.skin.getDrawable("hud-progress-red-filled");
        this.healthProgressBarStyle.knobBefore = this.skin.getDrawable("hud-progress-blank");
        this.healthProgressBarStyle.knob = this.skin.getDrawable("hud-progress-knob");
        this.powerProgressBarStyle = new ProgressBar.ProgressBarStyle();
        this.powerProgressBarStyle.knobAfter = this.skin.getDrawable("hud-progress-empty");
        this.powerProgressBarStyle.knobBefore = this.skin.getDrawable("hud-progress-filled");
        this.powerProgressBarStyle.knob = this.skin.getDrawable("hud-progress-knob");
        this.hungerProgressBarStyle = new ProgressBar.ProgressBarStyle();
        this.hungerProgressBarStyle.knobAfter = this.skin.getDrawable("hud-progress-brown-filled");
        this.hungerProgressBarStyle.knobBefore = this.skin.getDrawable("hud-progress-blank");
        this.hungerProgressBarStyle.knob = this.skin.getDrawable("hud-progress-knob");
        this.boostLeftBarStyle = new ProgressBar.ProgressBarStyle();
        this.boostLeftBarStyle.knobBefore = this.skin.getDrawable("hud-progress-increase-filled");
        this.boostLeftBarStyle.knobAfter = this.skin.getDrawable("hud-progress-empty");
        this.boostLeftBarStyle.knob = this.skin.getDrawable("hud-progress-knob-blank");
        this.boostRightBarStyle = new ProgressBar.ProgressBarStyle();
        this.boostRightBarStyle.knobAfter = this.skin.getDrawable("hud-progress-increase-filled");
        this.boostRightBarStyle.knobBefore = this.skin.getDrawable("hud-progress-empty");
        this.boostRightBarStyle.knob = this.skin.getDrawable("hud-progress-knob-blank");
        this.dayCycleProgressStyle = new ProgressBar.ProgressBarStyle();
        this.dayCycleProgressStyle.knobAfter = this.skin.getDrawable("hud-timeprogress-night");
        this.dayCycleProgressStyle.knobBefore = this.skin.getDrawable("hud-timeprogress-day");
        this.dayCycleProgressStyle.knob = this.skin.getDrawable("hud-timeprogress-knob");
        this.buildingEquipped = false;
        this.setup();
        this.introFadeIn();
        this.performanceGraph = new PerformanceGraph(this.gameScreen.hudStage, this.gameScreen);
        this.performanceGraph.setVisible(false);
        this.joystickTestUI = new JoystickTestUI(this.skin, this.labelStyle, this.hudGroup);
    }

    private void introFadeIn() {
        this.fullScreenFader = new Image(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
        this.fullScreenFader.setTouchable(Touchable.disabled);
        this.fullScreenFader.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        this.fullScreenFader.setColor(0.0f, 0.0f, 0.0f, 1.0f);
        this.gameScreen.hudStage.addActor(this.fullScreenFader);
        this.fullScreenFader.addAction(Actions.sequence((Action)Actions.fadeOut(0.5f)));
        this.fullScreenFader.setUserObject(false);
    }

    public void activate(World world) {
        this.world = world;
        if (this.player == null && this.world != null) {
            this.player = this.world.player;
        }
        // Update HUD elements that don't require a player first
        try {
            if (this.world != null && this.world.dayCycle != null) {
                this.showDay(this.world.dayCycle.getDay());
                try {
                    this.dayCycleProgress.setValue(world.dayCycle.dayNightPercent());
                } catch (Exception e) {
                    Gdx.app.log("Hud", "dayNightPercent not ready yet: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Gdx.app.log("Hud", "showDay/dayCycleProgress guard: " + e.getMessage());
        }
        this.updateDateTimeLabel();
        this.updateDebugText();

        if (this.player == null) {
            Gdx.app.log("Hud", "activate: player is not yet present, deferring player-specific init");
            try { if (this.interactCursor != null) this.interactCursor.setVisible(false); } catch (Exception ignored) {}
            return;
        }
        if (this.activated) {
            return;
        }
        this.activated = true;
        if (this.player != null) {
            try { this.player.addObserver(this); } catch (Exception ignored) {}
            try { this.player.playerStatus.update(0.0f); } catch (Exception ignored) {}
            this.addStatusEffects();
            if (this.player.playerStatus.getSuitPower() > 0.0f) {
                this.restoreHudPower();
            } else {
                this.looseHudPower();
            }
            try { this.hudInventory.update(this.player.playerInventory); } catch (Exception ignored) {}
            GameScreen gameScreen = world.gameScreen;
            if (gameScreen.game.getCurrentMission().missionType == Mission.MissionTypes.tutorial) {
                GameScreen gameScreen2 = world.gameScreen;
                if (!gameScreen2.game.getCurrentMission().tutorialFinished) {
                    this.gameScreen.tutorial = new Tutorial(this.player);
                    this.setupTutorialUI();
                }
            }
            GameScreen gameScreen3 = world.gameScreen;
            if (gameScreen3.game.getCurrentMission().missionType != Mission.MissionTypes.tutorial && this.gameScreen.newGame) {
                this.gameScreen.hud.hudNotifications.newMessage(Localization.get("findSupplyCrates"));
            }
        }
        // Ensure letterbox (black bars) are hidden after HUD init
        try { this.hideLetterbox(); } catch (Exception ignored) {}

        if (this.gameScreen.world.techManager != null) {
            this.gameScreen.world.techManager.addObserver(this);
            this.gameScreen.world.techManager.notifyHud();
        }
        GameScreen gameScreen4 = world.gameScreen;
        if (gameScreen4.game.getCurrentMission() != null) {
            gameScreen4.game.getCurrentMission().addObserver(this);
        } else {
            Gdx.app.log("Hud", "activate: current mission not initialized yet, deferring mission observer attach");
        }
    }

    private void setupLowHealthBorder() {
        this.lowHealthGroup = new Group();
        this.menuGroup.addActor(this.lowHealthGroup);
        this.lowHealthGroup.addAction(Actions.forever(Actions.sequence((Action)Actions.alpha(0.75f, 0.25f, Interpolation.sineIn), (Action)Actions.alpha(1.0f, 0.25f, Interpolation.sineOut))));
        this.lowHealthBorder = new Image(this.skin.getDrawable("hud-injured-border"));
        this.lowHealthBorder.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        this.lowHealthBorder.setPosition(0.0f, 0.0f);
        this.lowHealthBorder.setColor(0.8f, 0.0f, 0.0f, 1.0f);
        this.lowHealthBorder.setVisible(false);
        this.lowHealthBorder.setTouchable(Touchable.disabled);
        this.lowHealthGroup.addActor(this.lowHealthBorder);
        this.lowHealthBorder.toBack();
    }

    public void setupTutorialUI() {
        this.tutorialUI = new TutorialUI(this, this.menuGroup, this.gameScreen.tutorial);
        if (this.gameScreen.tutorial != null) {
            this.gameScreen.tutorial.registerUI(this.tutorialUI);
        }
    }

    public void hideLetterbox() {
        this.letterboxTop.toFront();
        this.letterboxBottom.toFront();
        this.letterboxTop.addAction(Actions.sequence((Action)Actions.scaleTo(1.0f, 0.0f, 0.5f, Interpolation.sineOut), (Action)Actions.visible(false)));
        this.letterboxBottom.addAction(Actions.sequence((Action)Actions.scaleTo(1.0f, 0.0f, 0.5f, Interpolation.sineOut), (Action)Actions.visible(false)));
    }

    public void showLetterBox() {
        this.letterboxTop.toFront();
        this.letterboxBottom.toFront();
        this.letterboxTop.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.scaleTo(1.0f, 0.5f, 0.5f, Interpolation.sineOut)));
        this.letterboxBottom.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.scaleTo(1.0f, 0.5f, 0.5f, Interpolation.sineOut)));
    }

    @Override
    public void back() {
        System.out.println("Don't want to be able to exit the Hud");
    }

    @Override
    public void setup() {
        super.setup();
        this.hudWaypoints = new HudWaypoints(this.gameScreen.world, this, this.hudGroup);
        NinePatch edges = new NinePatch(this.gameScreen.skin.getRegion("dark-edge"), 90, 90, 90, 90);
        this.vignette = new Image(edges);
        this.vignette.setTouchable(Touchable.disabled);
        this.vignette.setColor(1.0f, 1.0f, 1.0f, 0.25f);
        this.vignette.setSize(0.0f + (float)MoonBase.SCREEN_WIDTH, 0.0f + (float)MoonBase.SCREEN_HEIGHT);
        this.gameScreen.hudStage.addActor(this.vignette);
        this.vignette.toBack();
        this.setupLowHealthBorder();
        this.letterboxTop = new Image(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
        this.letterboxTop.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT / 8 + 10);
        this.letterboxTop.setColor(Color.BLACK);
        this.letterboxTop.setOrigin(10);
        this.letterboxTop.setPosition(0.0f, MoonBase.SCREEN_HEIGHT, 10);
        this.gameScreen.hudStage.addActor(this.letterboxTop);
        this.letterboxBottom = new Image(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
        this.letterboxBottom.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT / 8 + 10);
        this.letterboxBottom.setColor(Color.BLACK);
        this.letterboxBottom.setOrigin(12);
        this.letterboxBottom.setPosition(0.0f, 0.0f, 12);
        this.gameScreen.hudStage.addActor(this.letterboxBottom);
        this.buildingPlacementCursor = new BuildingPlacementCursor(this);
        this.interactCursor = new Image(this.gameScreen.skin.getDrawable("white"));
        this.interactCursor.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.interactCursor.setColor(0.5f, 0.5f, 1.0f, 0.1f);
        this.gameScreen.worldUiGroup.addActor(this.interactCursor);
        this.topLeft = new Group();
        this.topLeft.setPosition(0.0f, MoonBase.SCREEN_HEIGHT);
        this.hudGroup.addActor(this.topLeft);
        this.topLeftBg = new Image(this.skin.getDrawable("hud-topleft2"));
        this.topLeftBg.setPosition(0.0f, 0.0f, 10);
        this.topLeft.addActor(this.topLeftBg);
        this.airGroup = new Group();
        this.airGroup.setPosition(0.0f, -16.0f);
        this.topLeft.addActor(this.airGroup);
        this.airIcon = new Image(this.skin.getDrawable("staticon-oxygen"));
        this.airIcon.setPosition(18.0f, 8.0f, 10);
        this.airIcon.setOrigin(1);
        this.airGroup.addActor(this.airIcon);
        this.airProgressIncrease = new ProgressBar(0.0f, 1.0f, 0.01f, false, this.boostLeftBarStyle);
        this.airProgressIncrease.setSize(204.0f, 22.0f);
        this.airProgressIncrease.setPosition(62.0f, 0.0f, 10);
        this.airGroup.addActor(this.airProgressIncrease);
        this.airProgress = new ProgressBar(0.0f, 1.0f, 0.01f, false, this.oxygenProgressBarStyle);
        this.airProgress.setSize(204.0f, 22.0f);
        this.airProgress.setPosition(62.0f, 0.0f, 10);
        this.airGroup.addActor(this.airProgress);
        this.airBarBorder = new Image(this.gameScreen.skin.getDrawable("hud-progress-wide-border"));
        this.airBarBorder.setSize(212.0f, 30.0f);
        this.airBarBorder.setPosition(this.airProgress.getX(1), this.airProgress.getY(1), 1);
        this.airGroup.addActor(this.airBarBorder);
        this.airBarBorderGlow = new Image(this.gameScreen.skin.getDrawable("hud-progress-wide-border-white"));
        this.airBarBorderGlow.setSize(212.0f, 30.0f);
        this.airBarBorderGlow.setPosition(this.airProgress.getX(1), this.airProgress.getY(1), 1);
        this.airBarBorderGlow.setTouchable(Touchable.disabled);
        this.airBarBorderGlow.setColor(0.8f, 0.0f, 0.0f, 1.0f);
        this.airBarBorderGlow.setVisible(false);
        this.airGroup.addActor(this.airBarBorderGlow);
        this.airBarBorder.addListener(new ClickListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Hud.this.createTooltip("Oxygen", new ToolTipWatchable(){
                    StringBuilder sb;

                    @Override
                    public String getTooltipText() {
                        if (this.sb == null) {
                            this.sb = new StringBuilder();
                        }
                        this.sb.setLength(0);
                        this.sb.append(Localization.get("oxygen"));
                        this.sb.append(": ");
                        this.sb.append(Vars.wholeNum.format(Hud.this.player.playerStatus.getAir()));
                        this.sb.append(" / ");
                        this.sb.append(Vars.wholeNum.format(Hud.this.player.playerStatus.getMaxAir()));
                        return this.sb.toString();
                    }
                });
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Hud.this.removeTooltip();
            }
        });
        this.powerGroup = new Group();
        this.powerGroup.setPosition(0.0f, -60.0f);
        this.topLeft.addActor(this.powerGroup);
        this.powerIcon = new Image(this.skin.getDrawable("staticon-power"));
        this.powerIcon.setPosition(18.0f, 8.0f, 10);
        this.powerGroup.addActor(this.powerIcon);
        this.powerProgress = new ProgressBar(0.0f, 1.0f, 0.01f, false, this.powerProgressBarStyle);
        this.powerProgress.setSize(179.0f, 18.0f);
        this.powerProgress.setPosition(62.0f, 0.0f, 10);
        this.powerGroup.addActor(this.powerProgress);
        this.powerBarBorder = new Image(this.gameScreen.skin.getDrawable("hud-progress-border"));
        this.powerBarBorder.setSize(188.0f, 30.0f);
        this.powerBarBorder.setPosition(this.powerProgress.getX(1), this.powerProgress.getY(1), 1);
        this.powerGroup.addActor(this.powerBarBorder);
        this.powerBarBorder.addListener(new ClickListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Hud.this.createTooltip("Power", new ToolTipWatchable(){

                    @Override
                    public String getTooltipText() {
                        return Localization.get("power") + ": " + Vars.wholeNum.format(Hud.this.player.playerStatus.getSuitPower()) + " / " + Vars.wholeNum.format(Hud.this.player.playerStatus.getMaxSuitPower());
                    }
                });
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Hud.this.removeTooltip();
            }
        });
        this.airWarningIcon = new Image(this.gameScreen.skin.getDrawable("base-warning"));
        this.airWarningIcon.setSize(68.0f, 60.0f);
        this.airWarningIcon.setPosition(this.airBarBorder.getX(16) + 45.0f, this.airBarBorder.getY(1) - 45.0f);
        this.airGroup.addActor(this.airWarningIcon);
        this.airWarningIcon.setVisible(false);
        this.airWarningIcon.addAction(Actions.forever(Actions.sequence((Action)Actions.alpha(0.4f, 0.5f), (Action)Actions.alpha(1.0f, 0.5f))));
        this.topRight = new Group();
        this.topRight.setPosition(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        this.hudGroup.addActor(this.topRight);
        this.topRightBg = new Image(this.skin.getDrawable("hud-topleft2"));
        this.topRightBg.setOrigin(1);
        this.topRightBg.setScaleX(-1.0f);
        this.topRightBg.setPosition(0.0f, 0.0f, 18);
        this.topRight.addActor(this.topRightBg);
        this.healthGroup = new Group();
        this.healthGroup.setPosition(0.0f, -16.0f);
        this.topRight.addActor(this.healthGroup);
        this.healthIcon = new Image(this.skin.getDrawable("staticon-health"));
        this.healthIcon.setOrigin(1);
        this.healthIcon.setPosition(-18.0f, 4.0f, 18);
        this.healthGroup.addActor(this.healthIcon);
        this.healthProgressIncrease = new ProgressBar(0.0f, 1.0f, 0.01f, false, this.boostRightBarStyle);
        this.healthProgressIncrease.setSize(204.0f, 22.0f);
        this.healthProgressIncrease.setPosition(-62.0f, 0.0f, 18);
        this.healthProgressIncrease.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.healthGroup.addActor(this.healthProgressIncrease);
        this.healthProgress = new ProgressBar(0.0f, 1.0f, 0.01f, false, this.healthProgressBarStyle);
        this.healthProgress.setSize(204.0f, 22.0f);
        this.healthProgress.setPosition(-62.0f, 0.0f, 18);
        this.healthGroup.addActor(this.healthProgress);
        this.healthBarBorder = new Image(this.gameScreen.skin.getDrawable("hud-progress-wide-border"));
        this.healthBarBorder.setSize(212.0f, 30.0f);
        this.healthBarBorder.setPosition(this.healthProgress.getX(1), this.healthProgress.getY(1), 1);
        this.healthGroup.addActor(this.healthBarBorder);
        this.healthBarBorder.addListener(new ClickListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Hud.this.createTooltip("Oxygen", new ToolTipWatchable(){

                    @Override
                    public String getTooltipText() {
                        return Localization.get("playerHealth") + ": " + Vars.wholeNum.format(Hud.this.player.playerStatus.getHealth()) + " / " + Vars.wholeNum.format(Hud.this.player.playerStatus.getMaxHealth());
                    }
                });
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Hud.this.removeTooltip();
            }
        });
        Group healthChangeGroup = new Group();
        healthChangeGroup.setPosition(this.healthProgress.getX(8) - 20.0f, this.healthProgress.getY(1));
        this.healthGroup.addActor(healthChangeGroup);
        this.healthChangeIndicator = new Image(this.skin.getDrawable("flow-arrow"));
        this.healthChangeIndicator.setOrigin(1);
        this.healthChangeIndicator.setScale(0.6f, 0.4f);
        this.healthChangeIndicator.setPosition(0.0f, 0.0f, 1);
        healthChangeGroup.addActor(this.healthChangeIndicator);
        this.healthChangeIndicator.addAction(Actions.forever(Actions.sequence((Action)Actions.moveToAligned(0.0f, 1.25f, 1, 0.75f, Interpolation.sine), (Action)Actions.moveToAligned(0.0f, -1.25f, 1, 0.75f, Interpolation.sine))));
        this.hungerGroup = new Group();
        this.hungerGroup.setPosition(0.0f, -60.0f);
        this.topRight.addActor(this.hungerGroup);
        this.hungerIcon = new Image(this.skin.getDrawable("staticon-hunger"));
        this.hungerIcon.setOrigin(1);
        this.hungerIcon.setPosition(-18.0f, 8.0f, 18);
        this.hungerGroup.addActor(this.hungerIcon);
        this.hungerProgressIncrease = new ProgressBar(0.0f, 1.0f, 0.01f, false, this.boostRightBarStyle);
        this.hungerProgressIncrease.setSize(179.0f, 18.0f);
        this.hungerProgressIncrease.setPosition(-62.0f, 0.0f, 18);
        this.hungerProgressIncrease.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.hungerGroup.addActor(this.hungerProgressIncrease);
        this.hungerProgress = new ProgressBar(0.0f, 1.0f, 0.01f, false, this.hungerProgressBarStyle);
        this.hungerProgress.setSize(179.0f, 18.0f);
        this.hungerProgress.setPosition(-62.0f, 0.0f, 18);
        this.hungerGroup.addActor(this.hungerProgress);
        this.hungerBarBorder = new Image(this.gameScreen.skin.getDrawable("hud-progress-border"));
        this.hungerBarBorder.setSize(188.0f, 30.0f);
        this.hungerBarBorder.setPosition(this.hungerProgress.getX(1), this.hungerProgress.getY(1), 1);
        this.hungerGroup.addActor(this.hungerBarBorder);
        this.hungerGroup.addListener(new ClickListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Hud.this.createTooltip("Hunger", new ToolTipWatchable(){

                    @Override
                    public String getTooltipText() {
                        return Localization.get("playerHunger") + ": " + Vars.wholeNum.format(Hud.this.player.playerStatus.getHunger()) + " / " + Vars.wholeNum.format(Hud.this.player.playerStatus.getMaxHunger());
                    }
                });
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Hud.this.removeTooltip();
            }
        });
        this.hungerBarBorderGlow = new Image(this.gameScreen.skin.getDrawable("hud-progress-border-white"));
        this.hungerBarBorderGlow.setSize(188.0f, 30.0f);
        this.hungerBarBorderGlow.setPosition(this.hungerProgress.getX(1), this.hungerProgress.getY(1), 1);
        this.hungerBarBorderGlow.setTouchable(Touchable.disabled);
        this.hungerBarBorderGlow.setColor(0.8f, 0.0f, 0.0f, 1.0f);
        this.hungerBarBorderGlow.setVisible(false);
        this.hungerGroup.addActor(this.hungerBarBorderGlow);
        this.vehicleBatGroup = new Group();
        this.vehicleBatGroup.setVisible(false);
        this.hudGroup.addActor(this.vehicleBatGroup);
        this.vehicleBatGroup.setPosition(MoonBase.SCREEN_WIDTH - 100, 160.0f);
        this.vehicleBatBg = new Image(this.gameScreen.skin.getDrawable("vehicle-bat-bg"));
        this.vehicleBatGroup.addActor(this.vehicleBatBg);
        this.vehicleBatBg.setPosition(0.0f, 0.0f, 4);
        this.vehicleBatBg.addListener(new ClickListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (Hud.this.player.isDrivingVehicle()) {
                    Vehicle v = Hud.this.player.getVehicle();
                    String msg = Localization.get("buggie_repairRechargeTip");
                    Hud.this.gameScreen.hud.createTooltip(msg);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Hud.this.gameScreen.hud.removeTooltip();
            }
        });
        this.vehicleBatNeedle = new Image(this.gameScreen.skin.getDrawable("vehicle-bat-needle"));
        this.vehicleBatGroup.addActor(this.vehicleBatNeedle);
        this.vehicleBatNeedle.setOrigin(10.0f, 10.0f);
        this.vehicleBatNeedle.setPosition(-10.0f, 33.0f, 8);
        this.vehicleBatNeedle.setRotation(90.0f);
        this.vehicleBatNeedle.setTouchable(Touchable.disabled);
        this.vehicleStatusLabel = new Label((CharSequence)Localization.format("vehicle_health", "--"), this.labelStyle);
        this.vehicleStatusLabel.setFontScale(0.35f);
        this.vehicleStatusLabel.setColor(MENU_COLOR);
        this.vehicleStatusLabel.setAlignment(16, 18);
        this.vehicleStatusLabel.setPosition(50.0f, 10.0f, 18);
        this.vehicleBatGroup.addActor(this.vehicleStatusLabel);
        this.vehicleChargeIndicator = new Image(this.skin.getDrawable("flow-arrow"));
        this.vehicleChargeIndicator.setVisible(false);
        this.vehicleChargeIndicator.setOrigin(1);
        this.vehicleChargeIndicator.setScale(0.7f, 0.5f);
        this.vehicleChargeIndicator.setPosition(55.0f, 80.0f, 1);
        this.vehicleBatGroup.addActor(this.vehicleChargeIndicator);
        this.vehicleChargeIndicator.addAction(Actions.forever(Actions.sequence((Action)Actions.moveToAligned(55.0f, 80.0f, 1, 0.75f, Interpolation.sine), (Action)Actions.moveToAligned(55.0f, 82.5f, 1, 0.75f, Interpolation.sine))));
        this.creatureLabel = new Label((CharSequence)"Attack", this.gameScreen.labelStyle);
        this.creatureLabel.setFontScale(0.4f);
        this.creatureLabel.setAlignment(1, 1);
        this.creatureLabel.setColor(Menu.MENU_COLOR);
        this.stage.addActor(this.creatureLabel);
        this.creatureLabel.setTouchable(Touchable.disabled);
        this.creatureLabel.setPosition(400.0f, 300.0f, 1);
        this.creatureLabel.setVisible(false);
        this.debugLabel = new Label((CharSequence)"DEBUG", this.gameScreen.labelStyle);
        this.debugLabel.setWidth(600.0f);
        this.debugLabel.setFontScale(0.3f);
        this.debugLabel.setAlignment(20);
        this.debugLabel.setPosition(MoonBase.SCREEN_WIDTH - 20, 80.0f, 20);
        this.debugLabel.setColor(Color.WHITE);
        this.debugLabel.setVisible(MoonBase.DEBUG_INFO);
        this.menuGroup.addActor(this.debugLabel);
        this.playerStateLabel = new Label((CharSequence)"", this.gameScreen.labelStyle);
        this.playerStateLabel.setFontScale(0.4f);
        this.playerStateLabel.setColor(MENU_COLOR);
        this.playerStateLabel.getColor().a = 0.75f;
        this.playerStateLabel.setAlignment(10, 10);
        this.playerStateLabel.setPosition(37.0f, this.airGroup.getY() - 110.0f, 10);
        this.hudGroup.addActor(this.playerStateLabel);
        this.statusEffectGroup = new Group();
        this.hudGroup.addActor(this.statusEffectGroup);
        this.statusEffectGroup.setPosition(MoonBase.SCREEN_WIDTH - 52 - 75, MoonBase.SCREEN_HEIGHT - 150);
        this.persistantDayGroup = new Group();
        this.persistantDayGroup.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT - 20);
        this.hudGroup.addActor(this.persistantDayGroup);
        Image timerL = new Image(this.skin.getDrawable("hud-timer-side"));
        timerL.setOrigin(1);
        timerL.setPosition(-118.0f, 0.0f, 1);
        timerL.getColor().a = 0.75f;
        this.persistantDayGroup.addActor(timerL);
        Image timerR = new Image(this.skin.getDrawable("hud-timer-side"));
        timerR.setOrigin(1);
        timerR.setPosition(118.0f, 0.0f, 1);
        timerR.getColor().a = 0.75f;
        this.persistantDayGroup.addActor(timerR);
        this.dayCycleProgress = new ProgressBar(0.0f, 1.0f, 0.01f, false, this.dayCycleProgressStyle);
        this.dayCycleProgress.getColor().a = 0.35f;
        this.dayCycleProgress.setSize(201.0f, 5.0f);
        this.dayCycleProgress.setPosition(0.0f, 0.0f, 1);
        this.persistantDayGroup.addActor(this.dayCycleProgress);
        this.dayIcon = new Image(this.skin.getDrawable("hud-time-sun"));
        this.dayIcon.getColor().a = 0.75f;
        this.dayIcon.setOrigin(1);
        this.dayIcon.setPosition(0.0f, 0.0f, 1);
        this.persistantDayGroup.addActor(this.dayIcon);
        this.dayTimeLabel = new Label((CharSequence)"Day X / X   00:00", this.gameScreen.labelStyle);
        this.dayTimeLabel.setWrap(false);
        this.dayTimeLabel.setFontScale(0.4f);
        this.dayTimeLabel.setSize(380.0f, 35.0f);
        this.dayTimeLabel.setColor(MENU_COLOR);
        this.dayTimeLabel.setAlignment(2, 1);
        this.dayTimeLabel.setPosition(0.0f, -10.0f, 2);
        this.persistantDayGroup.addActor(this.dayTimeLabel);
        this.dayTimeLabel.addListener(new ClickListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Hud.this.createTooltip("Days", new ToolTipWatchable(){

                    @Override
                    public String getTooltipText() {
                        return Localization.format("dayHud", Hud.this.world.dayCycle.getDay(), MoonBase.getCurrentMission().getMissionDayLength());
                    }
                });
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Hud.this.removeTooltip();
            }
        });
        this.baseStatsInfo = new BaseStatsInfo(this, this.hudGroup);
        this.commsUI = new CommsUI(this, this.hudGroup);
        this.suitPowerOfflineLabel = new Label((CharSequence)Localization.get("suitPowerOffline"), this.gameScreen.labelStyle);
        this.suitPowerOfflineLabel.setFontScale(0.45f);
        this.suitPowerOfflineLabel.setColor(Color.RED);
        this.suitPowerOfflineLabel.setAlignment(1);
        this.suitPowerOfflineLabel.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT - 10, 2);
        this.suitPowerOfflineLabel.setVisible(false);
        this.menuGroup.addActor(this.suitPowerOfflineLabel);
        this.dayGroup = new Group();
        this.dayGroup.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT - 90);
        this.dayGroup.setVisible(false);
        this.dayGroup.setDebug(true);
        this.hudGroup.addActor(this.dayGroup);
        this.dayLabel = new Label((CharSequence)Localization.format("dayNum", 0), this.gameScreen.labelStyle);
        this.dayLabel.setFontScale(1.0f);
        this.dayLabel.setColor(MENU_COLOR);
        this.dayLabel.setPosition(0.0f, 0.0f, 1);
        this.dayLabel.setAlignment(1);
        this.dayGroup.addActor(this.dayLabel);
        this.daysLeftLabel = new Label((CharSequence)Localization.format("daysLeft", 1), this.gameScreen.labelStyle);
        this.daysLeftLabel.setFontScale(0.5f);
        this.daysLeftLabel.setColor(MENU_COLOR);
        this.daysLeftLabel.setPosition(0.0f, -40.0f, 1);
        this.daysLeftLabel.setAlignment(1);
        this.dayGroup.addActor(this.daysLeftLabel);
        this.discoveryGroup = new Group();
        this.discoveryGroup.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2);
        this.discoveryGroup.setVisible(false);
        this.hudGroup.addActor(this.discoveryGroup);
        this.newDiscoveryLabel = new Label((CharSequence)Localization.get("newDiscovery"), this.gameScreen.labelStyle);
        this.newDiscoveryLabel.setFontScale(1.0f);
        this.newDiscoveryLabel.setColor(MENU_COLOR);
        this.newDiscoveryLabel.setPosition(0.0f, 280.0f, 1);
        this.newDiscoveryLabel.setAlignment(1);
        this.discoveryGroup.addActor(this.newDiscoveryLabel);
        this.sciencePointsLabel = new Label((CharSequence)Localization.format("sciencePointGain", 1), this.gameScreen.labelStyle);
        this.sciencePointsLabel.setFontScale(0.5f);
        this.sciencePointsLabel.setColor(MENU_COLOR);
        this.sciencePointsLabel.setPosition(0.0f, 240.0f, 1);
        this.sciencePointsLabel.setAlignment(1);
        this.discoveryGroup.addActor(this.sciencePointsLabel);
        this.addTopButtons();
        this.virtualJoystick = new VirtualJoystick(this, this.hudGroup);
        this.hideProgressBarIncrease();
    }

    private void addTopButtons() {
        Group topButtonGroup = new Group();
        topButtonGroup.setPosition(50.0f, MoonBase.SCREEN_HEIGHT - 150);
        this.hudGroup.addActor(topButtonGroup);
        Group rightButtonGroup = new Group();
        rightButtonGroup.setPosition(MoonBase.SCREEN_WIDTH - 50, MoonBase.SCREEN_HEIGHT - 150);
        this.hudGroup.addActor(rightButtonGroup);
        Button.ButtonStyle mapBS = new Button.ButtonStyle();
        mapBS.up = this.skin.getDrawable("hud/map-icon");
        mapBS.over = this.skin.getDrawable("hud/map-icon-hover");
        mapBS.down = this.skin.getDrawable("hud/map-icon-pressed");
        this.mapButton = new Button(mapBS);
        this.mapButton.setSize(48.0f, 48.0f);
        this.mapButton.setPosition(0.0f, 0.0f, 1);
        topButtonGroup.addActor(this.mapButton);
        this.mapButton.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                Hud.this.gameScreen.world.player.openMap();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Hud.this.createTooltip(Localization.get("controls_map") + " [" + SettingsLoader.getInstance().settingsData.KEYS_MAP.toUpperCase() + "]");
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Hud.this.removeTooltip();
            }
        });
        Button.ButtonStyle menuBS = new Button.ButtonStyle();
        menuBS.up = this.skin.getDrawable("hud/menu-icon");
        menuBS.over = this.skin.getDrawable("hud/menu-icon-hover");
        menuBS.down = this.skin.getDrawable("hud/menu-icon-pressed");
        this.menuButton = new Button(menuBS);
        this.menuButton.setSize(48.0f, 48.0f);
        this.menuButton.setPosition(0.0f, 0.0f, 1);
        rightButtonGroup.addActor(this.menuButton);
        this.menuButton.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (Hud.this.world.gameScreen.menuStack.isEmpty()) {
                    Hud.this.world.pauseGame();
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Hud.this.createTooltip(Localization.get("menu") + " [" + SettingsLoader.getInstance().settingsData.KEYS_MENU_BACK.toUpperCase() + "]");
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Hud.this.removeTooltip();
            }
        });
        Button.ButtonStyle missionBS = new Button.ButtonStyle();
        missionBS.up = this.skin.getDrawable("hud/mission-icon");
        missionBS.over = this.skin.getDrawable("hud/mission-icon-hover");
        missionBS.down = this.skin.getDrawable("hud/mission-icon-pressed");
        Button.ButtonStyle researchBS = new Button.ButtonStyle();
        researchBS.up = this.skin.getDrawable("hud/research-icon");
        researchBS.over = this.skin.getDrawable("hud/research-icon-hover");
        researchBS.down = this.skin.getDrawable("hud/research-icon-pressed");
        this.researchButtonWrapper = new Group();
        this.researchButtonWrapper.setPosition(71.0f, 0.0f);
        topButtonGroup.addActor(this.researchButtonWrapper);
        this.researchButton = new Button(researchBS);
        this.researchButton.setSize(48.0f, 48.0f);
        this.researchButton.setPosition(0.0f, 0.0f, 1);
        this.researchButtonWrapper.addActor(this.researchButton);
        this.researchButton.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                Hud.this.gameScreen.world.player.playerAnimController.openTablet();
                Hud.this.gameScreen.showMenu(new TechTreePopup(Hud.this.world.gameScreen));
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Hud.this.createTooltip(Localization.get("techUpgrades") + " [" + SettingsLoader.getInstance().settingsData.KEYS_RESEARCH.toUpperCase() + "]");
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Hud.this.removeTooltip();
            }
        });
        this.researchPointLabel = new Label((CharSequence)"999", this.gameScreen.labelStyle);
        this.researchPointLabel.setFontScale(0.4f);
        this.researchPointLabel.setAlignment(1, 1);
        this.researchPointLabel.setColor(Menu.MENU_COLOR);
        this.researchButtonWrapper.addActor(this.researchPointLabel);
        this.researchPointLabel.setTouchable(Touchable.disabled);
        this.researchPointLabel.setPosition(0.0f, -30.0f, 1);
        this.researchPointLabel.setVisible(false);
        this.researchPulseFx = new AdditiveImage(this.skin.getDrawable("small-gradient-circle"));
        this.researchPulseFx.setSize(100.0f, 100.0f);
        this.researchPulseFx.setOrigin(1);
        this.researchPulseFx.setColor(Color.valueOf("ff3900"));
        this.researchPulseFx.setVisible(false);
        this.researchPulseFx.setPosition(71.0f, 0.0f, 1);
        topButtonGroup.addActor(this.researchPulseFx);
        this.researchPulseFx.toBack();
        Group flashlightGroup = new Group();
        flashlightGroup.setColor(1.0f, 1.0f, 1.0f, 0.9f);
        topButtonGroup.addActor(flashlightGroup);
        flashlightGroup.setPosition(145.0f, 0.0f);
        flashlightGroup.setScale(-0.6f);
        this.flashlightIcon = new Image(this.gameScreen.skin.getDrawable("flashlight-icon"));
        this.flashlightIcon.setPosition(0.0f, 0.0f, 1);
        flashlightGroup.addActor(this.flashlightIcon);
        this.flashlightIcon.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (Hud.this.world.player != null && Hud.this.player.getVehicle() == null) {
                    Hud.this.player.playerStatus.setFlashlight(!Hud.this.player.playerStatus.getFlashlight());
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Hud.this.createTooltip(Localization.get("controls_flashlight") + " [" + SettingsLoader.getInstance().settingsData.KEYS_FLASHLIGHT.toUpperCase() + "]");
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Hud.this.removeTooltip();
            }
        });
        this.flashlightIconOn = new Image(this.gameScreen.skin.getDrawable("flashlight-icon-on"));
        this.flashlightIconOn.setPosition(this.flashlightIcon.getX() - 25.0f, 0.0f, 1);
        flashlightGroup.addActor(this.flashlightIconOn);
        this.flashlightIconOn.setTouchable(Touchable.disabled);
    }

    private void updateDateTimeLabel() {
        this.dateTimeUpdate += Gdx.graphics.getDeltaTime();
        if (this.dateTimeUpdate >= 0.5f) {
            this.dateTimeUpdate = 0.0f;
            this.dateTimeSB.setLength(0);
            this.dateTimeSB.append(Localization.format("dayHudEndless", this.world.dayCycle.getDay()));
            this.dateTimeSB.append("   ");
            this.dateTimeSB.append(this.world.dayCycle.getClockTime());
            this.dayTimeString = this.dateTimeSB.toString();
            if (!this.dayTimeString.equals(this.prevDayTimeString)) {
                this.dayTimeLabel.setText(this.dayTimeString);
                this.prevDayTimeString = this.dayTimeString;
            }
            float p = this.world.dayCycle.getDayProgress();
            this.dayIcon.setPosition(-100.0f + 200.0f * p, 0.0f, 1);
            if (this.dayIconState == null) {
                if (this.world.dayCycle.currentPeriod == DayCycle.Periods.day) {
                    this.dayIcon.setDrawable(this.skin.getDrawable("hud-time-sun"));
                    this.dayIconState = DayIconStates.day;
                } else {
                    this.dayIcon.setDrawable(this.skin.getDrawable("hud-time-moon"));
                    this.dayIconState = DayIconStates.night;
                }
            }
            if (p > this.world.dayCycle.dayNightPercent()) {
                if (this.dayIconState == DayIconStates.day) {
                    this.dayIconPulseAnim();
                    this.dayIcon.setDrawable(this.skin.getDrawable("hud-time-moon"));
                    this.dayIconState = DayIconStates.night;
                }
            } else if (this.dayIconState == DayIconStates.night) {
                this.dayIconPulseAnim();
                this.dayIcon.setDrawable(this.skin.getDrawable("hud-time-sun"));
                this.dayIconState = DayIconStates.day;
            }
        }
    }

    private void dayIconPulseAnim() {
        this.dayIcon.clearActions();
        this.dayIcon.addAction(Actions.sequence((Action)Actions.scaleTo(0.24f, 0.24f), (Action)Actions.scaleTo(1.2f, 1.2f, 0.2f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.1f)));
    }

    private void dayIconHourPulseAnim() {
        this.dayIcon.clearActions();
        this.dayIcon.addAction(Actions.sequence((Action)Actions.alpha(1.0f, 0.1f), (Action)Actions.alpha(0.75f, 0.5f)));
    }

    public void showDay(int day) {
        // Guard against being called before the game's current mission is initialized
        if (this.gameScreen == null || this.gameScreen.game == null || this.gameScreen.game.getCurrentMission() == null) {
            Gdx.app.log("Hud", "showDay: current mission not initialized yet, deferring showDay(" + day + ")");
            return;
        }
        GameScreen gameScreen = this.gameScreen;
        int left = gameScreen.game.getCurrentMission().getMissionDayLength() - day;
        if (this.dayLabel != null) this.dayLabel.setText(Localization.format("dayNum", day));
        GameScreen gameScreen2 = this.gameScreen;
        if (gameScreen2.game.getCurrentMission().missionType != Mission.MissionTypes.endless) {
            if (left > 0) {
                if (this.daysLeftLabel != null) { this.daysLeftLabel.setText(Localization.format("daysLeft", left)); this.daysLeftLabel.setVisible(true); }
            } else if (left == 0) {
                if (this.daysLeftLabel != null) { this.daysLeftLabel.setText(Localization.get("finalDay")); this.daysLeftLabel.setVisible(true); }
            } else {
                if (this.daysLeftLabel != null) this.daysLeftLabel.setVisible(false);
            }
            Object[] objectArray = new Object[2];
            objectArray[0] = day;
            GameScreen gameScreen3 = this.gameScreen;
            objectArray[1] = gameScreen3.game.getCurrentMission().getMissionDayLength();
            if (this.dayTimeLabel != null) this.dayTimeLabel.setText(Localization.format("dayHud", objectArray));
        } else {
            if (this.daysLeftLabel != null) this.daysLeftLabel.setVisible(false);
            if (this.dayTimeLabel != null) this.dayTimeLabel.setText(Localization.format("dayHudEndless", day));
        }
        if (this.dayGroup != null) {
            this.dayGroup.clearActions();
            this.dayGroup.addAction(Actions.sequence(Actions.alpha(0.0f), Actions.visible(true), Actions.fadeIn(0.5f), Actions.delay(2.0f), Actions.fadeOut(1.0f), Actions.visible(false)));
        }
    }

    public void foundScience(int points) {
        this.showDiscovery(points);
    }

    private void showDiscovery(int amount) {
        this.sciencePointsLabel.setText(Localization.format("sciencePointGain", amount));
        this.discoveryGroup.addAction(Actions.sequence(Actions.alpha(0.0f), Actions.visible(true), Actions.fadeIn(0.5f), Actions.delay(1.0f), Actions.fadeOut(1.0f), Actions.visible(false)));
    }

    public void showDebug(boolean visible) {
        this.debugLabel.setVisible(visible);
        this.joystickTestUI.setVisible(visible);
    }

    public void showLaunchControls(Lander lander) {
        this.hide();
        if (this.launchControls == null) {
            this.launchControls = new LaunchControls(this);
        }
        this.gameScreen.hudStage.addActor(this.launchControls.group);
        this.launchControls.show(lander);
    }

    public void showLaunchControls(LaunchPad launchPad) {
        this.hide();
        if (this.launchControls != null) {
            this.launchControls.group.remove();
        }
        this.launchControls = new LaunchControls(this);
        this.gameScreen.hudStage.addActor(this.launchControls.group);
        this.launchControls.show(launchPad);
    }

    public void hideLaunchControls() {
        this.show();
        if (this.launchControls != null) {
            this.launchControls.hide();
        }
        if (this.launchControls.lander != null) {
            this.launchControls.lander.openDoor();
        }
        this.player.setFlyingRocket(false);
    }

    public void hideHudForLaunch() {
        this.stage.getRoot().setTouchable(Touchable.disabled);
        this.hudGroup.addAction(Actions.sequence((Action)Actions.fadeOut(0.5f)));
        this.inventoryGroup.addAction(Actions.sequence((Action)Actions.fadeOut(0.5f)));
        this.showLetterBox();
    }

    public void showHudAfterLaunch() {
        this.show();
        this.stage.getRoot().setTouchable(Touchable.enabled);
        this.hudGroup.addAction(Actions.sequence((Action)Actions.fadeIn(0.5f)));
        this.inventoryGroup.addAction(Actions.sequence((Action)Actions.fadeIn(0.5f)));
        this.hideLetterbox();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (!this.activated && this.world != null && this.world.player != null) {
            this.activate(this.world);
        }
        if (this.player != null) {
            for (TileProgressBar tpb : this.tileProgressBars) {
                tpb.update();
            }
            this.updateDateTimeLabel();
            this.updateBaseInfo();
            this.updateDebugText();
            for (StatusEffectUI sei : this.statusEffectUIList) {
                sei.update(delta);
            }
            this.commsUI.update(delta, this.player);
            this.hudWaypoints.update(delta);
            if (MoonBase.DEBUG_INFO) {
                this.joystickTestUI.update();
            }
            MoonBase cfr_ignored_0 = this.gameScreen.game;
            if (MoonBase.PERF_GRAPH) {
                this.performanceGraph.setVisible(true);
                this.performanceGraph.update(delta);
            } else {
                this.performanceGraph.setVisible(false);
            }
            this.virtualJoystick.update(delta);
        }
        if (this.launchControls != null) {
            this.launchControls.update(delta);
        }
        for (int i = this.damageFlyoffList.size() - 1; i >= 0; --i) {
            if (!this.damageFlyoffList.get(i).isReadyToDestroy()) continue;
            this.damageFlyoffPool.free(this.damageFlyoffList.get(i));
            this.damageFlyoffList.remove(i);
        }
    }

    private void updateEntityLabel() {
        if (this.player.playerInventory.hasWeaponEquipped()) {
            if (this.entityLabelMouse == null) {
                this.entityLabelMouse = new Vector2(0.0f, 0.0f);
            }
            this.entityLabelMouse.set(Gdx.input.getX(), Gdx.input.getY());
            if (this.entityEllipse == null) {
                this.entityEllipse = new Circle(0.0f, 0.0f, 0.0f);
            }
            if (this.entityEllipsePos == null) {
                this.entityEllipsePos = new Vector2(0.0f, 0.0f);
            }
            this.entityLabelMouse = this.gameScreen.stage.screenToStageCoordinates(this.entityLabelMouse);
            this.creatureLabel.setVisible(false);
            Circle playerCircle = new Circle(this.player.getXPos(), this.player.getYPos(), this.player.getAttackRadius());
            if (playerCircle.contains(this.entityLabelMouse)) {
                for (Entity e : this.world.entityList) {
                    Creature c;
                    if (!(e instanceof Creature) || !(c = (Creature)e).isAlive() || c.creatureData == null) continue;
                    this.entityEllipsePos.set(c.getXPos(), c.getYPos());
                    this.entityEllipse.set(c.getXPos(), c.getYPos(), c.creatureData.physicsRadius);
                    if (!this.entityEllipse.contains(this.entityLabelMouse)) continue;
                    Vector2 creaturePos = new Vector2(c.getXPos(), c.getYPos());
                    Vector2 screenPos = this.gameScreen.stage.stageToScreenCoordinates(creaturePos);
                    screenPos = this.gameScreen.hudStage.screenToStageCoordinates(screenPos);
                    this.creatureLabel.setPosition(screenPos.x, screenPos.y + 70.0f, 1);
                    this.creatureLabel.setVisible(true);
                    break;
                }
            }
        } else {
            this.creatureLabel.setVisible(false);
        }
    }

    public void pulseResearchIcon() {
        this.researchPulseFx.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.scaleTo(2.0f, 2.0f), (Action)Actions.alpha(1.0f), (Action)Actions.parallel((Action)Actions.fadeOut(1.5f), (Action)Actions.scaleTo(0.5f, 0.5f, 1.5f)), (Action)Actions.visible(false)));
        this.researchButtonWrapper.addAction(Actions.sequence((Action)Actions.scaleTo(1.0f, 1.0f), (Action)Actions.scaleTo(1.5f, 1.5f, 0.3f, Interpolation.sineOut), (Action)Actions.scaleTo(1.0f, 1.0f, 1.2f, Interpolation.bounceOut)));
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o == "earnedResearchPoint") {
            this.pulseResearchIcon();
        }
        if (o == "updateAir") {
            this.airProgress.setValue(this.player.playerStatus.getAirPercent());
            if (this.player.playerStatus.getAir() / this.player.playerStatus.getMaxAir() < 0.2f) {
                // empty if block
            }
        }
        if (o == "updateSuitPower") {
            float v = this.player.playerStatus.getSuitPower() / this.player.playerStatus.getMaxSuitPower();
            this.powerProgress.setValue(v);
            if (this.player.playerStatus.getSuitPower() / this.player.playerStatus.getMaxSuitPower() < 0.2f) {
                this.powerProgress.setColor(Vars.WARNING_RED);
            } else {
                this.powerProgress.setColor(Color.WHITE);
            }
        }
        if (o == "suitLostPower") {
            this.looseHudPower();
        }
        if (o == "suitPowerRestored") {
            this.restoreHudPower();
        }
        if (o == "playerMoved") {
            this.updateInteractCursor();
            MoonBase cfr_ignored_0 = this.gameScreen.game;
            if (MoonBase.DEBUG_INFO) {
                this.updateDebugText();
            }
            this.hudWaypoints.updatePositions();
        }
        if (o == "updateInventory") {
            this.updateInventoryText();
            this.hideProgressBarIncrease();
        }
        if (o == "inventorySelectionChanged") {
            this.changeInventorySelection();
        }
        if (o == "enterVehicle") {
            this.buildingPlacementCursor.hide();
        }
        if (o == "exitVehicle") {
            this.addBuildingPlacementSprite();
        }
        if (o == "statusUpdate") {
            float h1 = 1.0f - this.player.playerStatus.getHealthPercent();
            this.healthProgress.setValue(h1);
            if (this.player.playerStatus.getAirPercent() <= 0.0f) {
                if (!this.airIcon.hasActions()) {
                    this.airIcon.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.2f, 1.2f, 0.4f, Interpolation.sineOut), (Action)Actions.scaleTo(1.0f, 1.0f, 0.4f, Interpolation.sineIn))));
                }
            } else if (this.airIcon.hasActions()) {
                this.airIcon.clearActions();
            }
            this.updateLowOxygenWarning();
            this.updateLowHungerWarning();
            if (this.player.playerStatus.getHealthPercent() < 0.25f) {
                if (!this.healthIcon.hasActions()) {
                    this.healthIcon.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.3f, 1.3f, 0.1f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.1f), (Action)Actions.scaleTo(1.2f, 1.2f, 0.1f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.6f, Interpolation.sine))));
                }
            } else if (this.healthIcon.hasActions()) {
                this.healthIcon.clearActions();
            }
            switch (this.player.playerStatus.healthChanging) {
                case -1: {
                    this.healthChangeIndicator.setVisible(true);
                    this.healthChangeIndicator.setScaleY(-0.4f);
                    this.healthChangeIndicator.setColor(1.0f, 0.0f, 0.0f, 1.0f);
                    this.showLoosingHealthGlow(true);
                    break;
                }
                case 1: {
                    this.healthChangeIndicator.setVisible(true);
                    this.healthChangeIndicator.setScaleY(0.4f);
                    this.healthChangeIndicator.setColor(1.0f, 1.0f, 1.0f, 0.4f);
                    this.showLoosingHealthGlow(false);
                    if (!(this.player.playerStatus.getHealthPercent() >= 1.0f)) break;
                    this.healthChangeIndicator.setVisible(false);
                    break;
                }
                default: {
                    this.healthChangeIndicator.setVisible(false);
                    this.showLoosingHealthGlow(false);
                }
            }
            float h2 = 1.0f - this.player.playerStatus.getHunger() / this.player.playerStatus.getMaxHunger();
            this.hungerProgress.setValue(h2);
            if (this.player.playerStatus.getHungerState() == PlayerStatus.HungerStates.starving) {
                if (!this.hungerIcon.hasActions()) {
                    this.hungerIcon.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.2f, 1.2f, 0.4f, Interpolation.sineOut), (Action)Actions.scaleTo(1.0f, 1.0f, 0.4f, Interpolation.sineIn))));
                }
            } else if (this.hungerIcon.hasActions()) {
                this.hungerIcon.clearActions();
                this.hungerIcon.setScale(1.0f);
            }
        }
        if (o == "addStatusEffects") {
            this.addStatusEffects();
        }
        if (o == "removeStatusEffects") {
            this.removeStatusEffects();
        }
        if (o == "flashlightUpdate") {
            this.updateFlashlight();
        }
        if (o == "rotateBuildingPlacement") {
            this.buildingPlacementCursor.setOrientation(this.player.getPlayerInventory().placementOrientation);
        }
        if (o == "researchSamplesUpdate") {
            this.researchPointLabel.setText("" + this.world.techManager.samples);
            this.researchPointLabel.setVisible(this.world.techManager.samples > 0);
        }
        if (o == "mission_complete_ready") {
            MoonBase.log("Hud mission_complete_ready message recieved");
            this.showMissionReadyMsg();
        }
    }

    public void showMissionReadyMsg() {
        if (this.returnUI != null) {
            this.returnUI.group.remove();
            this.returnUI = null;
        }
        this.returnUI = new ReturnToLanderUI(this, this.menuGroup);
        this.returnUI.setText(Localization.get("buildARocket"));
        this.returnUI.show();
    }

    private void updateLowOxygenWarning() {
        float airPercent = this.player.playerStatus.getAirPercent();
        if (airPercent < 0.125f) {
            if (!this.displayedLowAirFlash && !this.displayedLowAirFlash2) {
                this.displayedLowAirFlash = true;
                this.airBarBorderGlow.clearActions();
                this.airBarBorderGlow.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.fadeIn(0.25f), (Action)Actions.forever(Actions.sequence((Action)Actions.alpha(0.25f, 0.5f), (Action)Actions.alpha(0.75f, 0.5f)))));
                Audio.getInstance().playSound("sounds/gameStart.ogg", 0.5f, 0.5f);
            }
        } else if (airPercent >= 0.125f) {
            this.displayedLowAirFlash = false;
            this.displayedLowAirFlash2 = false;
            if (this.airBarBorderGlow.isVisible()) {
                this.displayedLowAirFlash = false;
                this.displayedLowAirFlash2 = false;
                this.airBarBorderGlow.clearActions();
                this.airBarBorderGlow.addAction(Actions.sequence((Action)Actions.fadeOut(0.5f), (Action)Actions.visible(false)));
            }
        }
    }

    private void updateLowHungerWarning() {
        float hungerPercent = this.player.playerStatus.getHungerPercent();
        if (hungerPercent < 0.0725f) {
            if (!this.displayedLowHungerFlash) {
                this.displayedLowHungerFlash = true;
                this.hungerBarBorderGlow.clearActions();
                this.hungerBarBorderGlow.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.fadeIn(0.25f), (Action)Actions.forever(Actions.sequence((Action)Actions.alpha(0.25f, 0.5f), (Action)Actions.alpha(0.75f, 0.5f)))));
                Audio.getInstance().playSound("sounds/gameStart.ogg", 0.5f, 0.5f);
            }
        } else if (hungerPercent >= 0.0725f) {
            this.displayedLowHungerFlash = false;
            if (this.hungerBarBorderGlow.isVisible()) {
                this.displayedLowHungerFlash = false;
                this.hungerBarBorderGlow.clearActions();
                this.hungerBarBorderGlow.addAction(Actions.sequence((Action)Actions.fadeOut(0.5f), (Action)Actions.visible(false)));
            }
        }
    }

    private void addStatusEffects() {
        ArrayList<PlayerStatusEffect> pseToAdd = new ArrayList<PlayerStatusEffect>();
        if (this.world.player != null) {
            for (PlayerStatusEffect pse : this.world.player.playerStatus.statusEffects) {
                boolean exists = false;
                for (StatusEffectUI sei : this.statusEffectUIList) {
                    if (pse != sei.statusEffect) continue;
                    exists = true;
                    break;
                }
                if (exists) continue;
                pseToAdd.add(pse);
            }
        }
        for (PlayerStatusEffect pse : pseToAdd) {
            this.statusEffectUIList.add(new StatusEffectUI(pse, this.statusEffectGroup, this.gameScreen, this));
        }
        this.reorderStatusEffectPosition();
    }

    private void removeStatusEffects() {
        if (this.world.player != null) {
            for (int i = this.statusEffectUIList.size() - 1; i >= 0; --i) {
                StatusEffectUI sui = this.statusEffectUIList.get(i);
                if (this.player.playerStatus.statusEffects.contains(sui.statusEffect)) continue;
                this.statusEffectUIList.remove(i);
                sui.remove();
            }
        }
        this.reorderStatusEffectPosition();
    }

    public void reorderStatusEffectPosition() {
        boolean x = false;
        for (int i = 0; i < this.statusEffectUIList.size(); ++i) {
            this.statusEffectUIList.get(i).setPosition((float)i * -50.0f - (float)i * 10.0f, 0.0f);
        }
    }

    public void showLoosingHealthGlow(boolean on) {
        if (!this.lowHealthBorder.hasActions() && this.lowHealthBorder.isVisible() && !on) {
            this.lowHealthBorder.addAction(Actions.sequence((Action)Actions.fadeOut(0.5f), (Action)Actions.visible(false)));
        }
        if (on) {
            this.lowHealthBorder.clearActions();
            this.lowHealthBorder.setVisible(on);
            this.lowHealthBorder.getColor().a = 0.7f;
        }
    }

    private void setLowHealthAlpha(float healthPercent) {
        float minHealthThreshold = 0.4f;
        float maxAlpha = 0.7f;
        if (healthPercent == 1.0f) {
            this.lowHealthBorder.setVisible(false);
        } else if (healthPercent >= 0.4f) {
            this.lowHealthBorder.setVisible(true);
            this.lowHealthBorder.getColor().a = (1.0f - (healthPercent - minHealthThreshold) / 0.6f) * maxAlpha;
        } else {
            this.lowHealthBorder.setVisible(true);
            this.lowHealthBorder.getColor().a = maxAlpha;
        }
    }

    private void updateFlashlight() {
        if (this.player.playerStatus.getFlashlight()) {
            this.flashlightIcon.setDrawable(this.skin.getDrawable("flashlight-icon"));
            this.flashlightIconOn.setVisible(true);
        } else {
            this.flashlightIcon.setDrawable(this.skin.getDrawable("flashlight-icon-off"));
            this.flashlightIconOn.setVisible(false);
        }
    }

    private void updateBaseInfo() {
        if (this.player != null) {
            BaseGroup b = this.player.getCurrentBase();
            if (b != null && !this.player.isDrivingVehicle()) {
                if (!this.baseStatsInfo.visible) {
                    this.baseStatsInfo.setVisible(this.world.techManager, true);
                }
                this.baseStatsInfo.update(b);
                this.baseStatsInfo.setVisible(this.world.techManager, true);
                this.vehicleBatGroup.setVisible(false);
            } else {
                this.baseStatsInfo.setVisible(this.world.techManager, false);
                if (this.player.isDrivingVehicle()) {
                    if (this.player.getVehicle() instanceof VehicleBattery) {
                        this.vehicleBatGroup.setVisible(true);
                        this.vehicleStatusLabel.setText(this.player.getVehicle().getHealth());
                        VehicleBattery vbat = (VehicleBattery)((Object)this.player.getVehicle());
                        float p = vbat.getChargePercent() * 230.0f;
                        this.vehicleBatNeedle.setRotation(205.0f - p);
                        this.vehicleChargeIndicator.setVisible(vbat.isRecharging());
                        if (vbat.getChargePercent() < 0.2f) {
                            this.vehicleBatBg.setColor(Color.RED);
                            this.vehicleBatNeedle.setColor(Color.RED);
                        } else {
                            this.vehicleBatBg.setColor(Color.WHITE);
                            this.vehicleBatNeedle.setColor(Color.WHITE);
                        }
                    }
                } else {
                    this.vehicleBatGroup.setVisible(false);
                }
            }
        }
    }

    public void updateInteractCursor() {
        if (this.player != null) {
            this.interactCursorPoint = this.player.getInteractPoint2();
            this.buildingPlacementCursor.setPosition(this.interactCursorPoint.x, this.interactCursorPoint.y);
            boolean proxyInRange = this.buildingPlacementCursor.proxyPlacementInRange(this.gameScreen.world, this.interactCursorPoint.x, this.interactCursorPoint.y);
            if (this.player.interactWithinRange() || proxyInRange) {
                boolean mouseMoved = false;
                try {
                    mouseMoved = Gdx.input.getDeltaX() != 0 || Gdx.input.getDeltaY() != 0;
                } catch (Exception ignored) {}
                if (!SettingsLoader.getInstance().settingsData.USE_CONTROLLER || PlayerInput.lastUsedMouse || mouseMoved) {
                    this.interactCursor.setVisible(false);
                } else {
                    this.interactCursor.setVisible(true);
                    GridPoint2 point2 = this.player.getInteractPoint2();
                    this.interactCursor.setPosition((float)point2.x * Tile.TILE_SIZE, (float)point2.y * Tile.TILE_SIZE);
                    this.interactCursor.setColor(0.5f, 0.5f, 1.0f, 0.2f);
                }
                this.cursorTile = this.world.getTile(this.interactCursorPoint.x, this.interactCursorPoint.y);
                float temperature = this.world.getTempAtTile(this.interactCursorPoint.x, this.interactCursorPoint.y);
                if (this.cursorTile != null || temperature > 2.0f) {
                    this.buildingPlacementCursor.setValid(false);
                    this.buildingPlacementCursor.clearAdjacentValid();
                    MoonBase.setCursor(true);
                } else {
                    boolean proxyClear = this.buildingPlacementCursor.checkProxyChecks(this.gameScreen.world, this.interactCursorPoint.x, this.interactCursorPoint.y);
                    if (proxyClear && temperature <= 2.0f) {
                        this.buildingPlacementCursor.setValid(true);
                        this.checkBuildAdjacentWarning(this.interactCursorPoint.x, this.interactCursorPoint.y);
                        MoonBase.setCursor(false);
                    } else {
                        this.buildingPlacementCursor.setValid(false);
                        MoonBase.setCursor(false);
                    }
                }
            } else {
                this.interactCursor.setVisible(false);
                this.buildingPlacementCursor.setValid(false);
                this.buildingPlacementCursor.clearAdjacentValid();
                MoonBase.setCursor(false);
            }
        }
    }

    private void checkBuildAdjacentWarning(int worldX, int worldY) {
        if (this.buildingPlacementCursor.warnAdjacent) {
            boolean n = this.adjacentTileBlocksWind(worldX, worldY + 1);
            boolean ne = this.adjacentTileBlocksWind(worldX + 1, worldY + 1);
            boolean e = this.adjacentTileBlocksWind(worldX + 1, worldY);
            boolean se = this.adjacentTileBlocksWind(worldX + 1, worldY - 1);
            boolean s = this.adjacentTileBlocksWind(worldX, worldY - 1);
            boolean sw = this.adjacentTileBlocksWind(worldX - 1, worldY - 1);
            boolean w = this.adjacentTileBlocksWind(worldX - 1, worldY);
            boolean nw = this.adjacentTileBlocksWind(worldX - 1, worldY + 1);
            this.buildingPlacementCursor.setAdjacenteValid(n, ne, e, se, s, sw, w, nw);
        }
    }

    private boolean adjacentTileBlocksWind(int worldX, int worldY) {
        Tile t = this.world.getTile(worldX, worldY);
        if (t != null) {
            return t.blocksWind();
        }
        return false;
    }

    private void looseHudPower() {
        System.out.println("HUD: looseHudPower()");
        this.hudGroup.addAction(Actions.sequence(Actions.alpha(0.0f), Actions.delay(0.05f), Actions.alpha(0.7f), Actions.delay(0.1f), Actions.alpha(0.0f), Actions.delay(0.15f), Actions.alpha(0.5f), Actions.delay(0.15f), Actions.alpha(0.0f), Actions.delay(0.15f), Actions.alpha(0.25f), Actions.delay(0.05f), Actions.alpha(0.0f)));
        this.suitPowerOfflineLabel.setVisible(true);
        this.suitPowerOfflineLabel.addAction(Actions.forever(Actions.sequence((Action)Actions.alpha(0.4f, 0.8f), (Action)Actions.alpha(1.0f, 0.8f))));
    }

    private void restoreHudPower() {
        Gdx.app.debug("MewnBase", "HUD: restoreHudPower()");
        this.hudGroup.clearActions();
        this.hudGroup.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.fadeIn(0.5f)));
        this.suitPowerOfflineLabel.setVisible(false);
        this.suitPowerOfflineLabel.clearActions();
    }

    private void updateInventoryText() {
        MoonBase.debug("Hud: Updating inventory bar");
        this.buildingPlacementCursor.hide();
        this.buildingPlacementCursor.clearAdjacentValid();
        this.hudInventory.update(this.player.getPlayerInventory());
        this.removeTooltip();
        if (this.player.isOnFoot()) {
            this.addBuildingPlacementSprite();
        }
    }

    private void changeInventorySelection() {
        MoonBase.debug("Hud: Change inventory selection");
        this.buildingPlacementCursor.hide();
        this.buildingPlacementCursor.clearAdjacentValid();
        this.hudInventory.changeSelection(this.player.getPlayerInventory());
        this.removeTooltip();
        if (this.player.isOnFoot()) {
            this.addBuildingPlacementSprite();
        }
    }

    private void addBuildingPlacementSprite() {
        boolean buildingSprite = false;
        ItemStack stack = this.player.getPlayerInventory().getEquippedItem();
        if (stack != null && stack.item.hasActions() && stack.getAmount() > 0) {
            for (ItemActions a : stack.item.actions) {
                ItemData data;
                if (!a.type.equals(Item.ActionTypes.createBuilding.toString()) && !a.type.equals(Item.ActionTypes.createFloor.toString()) && !a.type.equals(Item.ActionTypes.createItemDropper.toString())) continue;
                this.buildingPlacementCursor.setSprite(ItemFactory.getItemSprite(this.player.getPlayerInventory().getEquippedItem().getId()));
                buildingSprite = true;
                this.buildingEquipped = true;
                this.buildingPlacementCursor.setWarnAdjacent(a.buildNoAdjacentIndicator);
                this.buildingPlacementCursor.currentItemData = data = stack.item.getData();
                this.buildingPlacementCursor.setSpriteScale(data.placementSpriteScale);
                this.buildingPlacementCursor.setSpriteOffset(data.placementSpriteOffset);
                this.buildingPlacementCursor.show();
                break;
            }
        }
        if (!buildingSprite) {
            this.buildingPlacementCursor.setSprite(null);
            this.buildingEquipped = false;
            this.buildingPlacementCursor.hide();
            this.buildingPlacementCursor.clearAdjacentValid();
        }
    }

    private void updateDebugText() {
        if (this.world == null || this.world.player == null) {
            this.debugLabel.setText("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Entities: ");
        sb.append(this.world.entityList.size());
        sb.append("\n");
        sb.append("Player Temp: ");
        sb.append(this.world.player.playerStatus.getTemperature());
        sb.append("\n");
        sb.append("Pos: " + Vars.wholeNum.format(this.world.player.getXPos()) + ", " + Vars.wholeNum.format(this.world.player.getYPos()));
        sb.append("\n");
        sb.append("Weather: ");
        sb.append(this.world.weatherManager.getCurrentData().id);
        sb.append(" - mode: " + MoonBase.getCurrentMission().weatherMode.toString() + "");
        sb.append("\n");
        sb.append("Wind: ");
        sb.append(this.world.weatherManager.getWindSpeed());
        sb.append("\n");
        sb.append("Chunks: ");
        sb.append(this.world.worldChunks.size());
        sb.append("\n");
        sb.append("MEM: ");
        sb.append((float)Gdx.app.getNativeHeap() * Vars.BYTES_TO_MEGABYTES);
        sb.append("\n");
        sb.append("FPS: ");
        sb.append(Gdx.graphics.getFramesPerSecond());
        sb.append("\n");
        if (this.player != null) {
            sb.append("\n");
            sb.append(this.player.getPositionInfo());
        }
        this.debugLabel.setText(sb.toString());
    }

    public void duskWarning() {
        System.out.println("Show dusk warning");
        this.duskWarningLabel = new Label((CharSequence)"Return to base.\nNight is coming.", this.gameScreen.labelStyle);
        this.duskWarningLabel.setFontScale(0.5f);
        this.duskWarningLabel.setAlignment(1);
        this.duskWarningLabel.setWidth(600.0f);
        this.duskWarningLabel.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 150, 1);
        this.duskWarningLabel.setColor(1.0f, 0.2f, 0.2f, 1.0f);
        this.hudGroup.addActor(this.duskWarningLabel);
        this.duskWarningLabel.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.fadeIn(0.2f), (Action)Actions.delay(3.0f), (Action)Actions.fadeOut(1.0f), (Action)Actions.removeActor()));
    }

    public void addResourceIndicator(GroundTile gt) {
        boolean exists = false;
        for (ResourceIndicator r : this.resourceIndicatorList) {
            if (r.groundTile != gt) continue;
            exists = true;
        }
        if (!exists) {
            this.resourceIndicatorList.add(new ResourceIndicator(this, this.worldInfoGroup, gt));
            System.out.println("Creating indicator at : " + gt.worldX + " " + gt.worldY);
        }
    }

    public void detectResources() {
        Image i = new Image(this.gameScreen.skin.getDrawable("detector-ring"));
        i.setSize(31.0f, 31.0f);
        i.setOrigin(1);
        float x = this.world.player.getXPos();
        float y = this.world.player.getYPos();
        i.setPosition(x, y, 1);
        this.gameScreen.worldUiGroup.addActor(i);
        i.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.5f, 0.5f), (Action)Actions.parallel((Action)Actions.scaleTo(9.0f, 9.0f, 1.0f, Interpolation.sineOut), (Action)Actions.sequence((Action)Actions.alpha(0.3f, 0.2f), (Action)Actions.fadeOut(0.8f))), (Action)Actions.removeActor()));
    }

    public void removeResourceIndicator(GroundTile groundTile) {
        for (int i = this.resourceIndicatorList.size() - 1; i >= 0; --i) {
            GroundTile g = this.resourceIndicatorList.get((int)i).groundTile;
            if (g != groundTile) continue;
            this.resourceIndicatorList.get(i).destroy();
            this.resourceIndicatorList.remove(i);
        }
    }

    @Override
    public void show() {
        this.menuGroup.setVisible(true);
        this.hudGroup.setVisible(true);
        this.hudInventory.show();
        this.worldInfoGroup.setVisible(true);
    }

    @Override
    public void hide() {
        this.menuGroup.setVisible(false);
        this.hudGroup.setVisible(false);
        this.hudInventory.hide();
        this.worldInfoGroup.setVisible(false);
    }

    public void createTooltip(String tip, ToolTipWatchable watchable) {
        if (this.tooltip != null) {
            this.removeTooltip();
        }
        this.tooltip = new Tooltip(this.gameScreen, this.gameScreen.hudStage, tip);
        this.tooltip.watchable = watchable;
        this.gameScreen.hudStage.addActor(this.tooltip);
    }

    @Override
    public void createTooltip(String tip) {
        if (this.tooltip != null) {
            this.removeTooltip();
        }
        this.tooltip = new Tooltip(this.gameScreen, this.gameScreen.hudStage, tip);
        this.gameScreen.hudStage.addActor(this.tooltip);
    }

    public Tooltip createTooltip(String tip, String tip2) {
        if (this.tooltip != null) {
            this.removeTooltip();
        }
        this.tooltip = new Tooltip(this.gameScreen, this.gameScreen.hudStage, tip, tip2);
        this.gameScreen.hudStage.addActor(this.tooltip);
        return this.tooltip;
    }

    public void createTooltip(String tip, String tip2, Tooltip.TooltipStyle style) {
        if (this.tooltip != null) {
            this.removeTooltip();
        }
        this.tooltip = new Tooltip(this.gameScreen, this.gameScreen.hudStage, tip, tip2, style);
        this.gameScreen.hudStage.addActor(this.tooltip);
    }

    @Override
    public void removeTooltip() {
        if (this.tooltip != null) {
            this.tooltip.remove();
        }
    }

    public void addTileProgressBar(Tile t) {
        this.tileProgressBars.add(new TileProgressBar(this, t));
    }

    public void addTilePickupProgressBar(BaseModule baseModule) {
        this.tileProgressBars.add(new TilePickupProgressBar(this, baseModule));
    }

    public void removeTileProgressBar(Tile t) {
        for (int i = this.tileProgressBars.size() - 1; i >= 0; --i) {
            TileProgressBar tpb = this.tileProgressBars.get(i);
            if (tpb.tile != t) continue;
            tpb.remove();
            this.tileProgressBars.remove(i);
        }
    }

    public void gameResult(boolean win, GameOverReasons.REASONS gameOverReason) {
        if (win) {
            this.gameScreen.showMenu(new GameWinMenu(this.gameScreen));
        } else {
            this.gameScreen.showMenu(new GameOverMenu(this.gameScreen, gameOverReason));
        }
    }

    public void simpleFadeOut(float startDelay, float length) {
        if (this.fullScreenFader == null) {
            this.fullScreenFader = new Image(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
            this.fullScreenFader.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
            this.gameScreen.hudStage.addActor(this.fullScreenFader);
        }
        this.fullScreenFader.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.delay(startDelay), (Action)Actions.fadeIn(length)));
    }

    public void simpleFadeIn(float startDelay, float length) {
        System.out.println("Doing fadein");
        if (this.fullScreenFader == null) {
            this.fullScreenFader = new Image(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
            this.fullScreenFader.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
            this.gameScreen.hudStage.addActor(this.fullScreenFader);
        }
        this.fullScreenFader.addAction(Actions.sequence((Action)Actions.alpha(1.0f), (Action)Actions.delay(startDelay), (Action)Actions.fadeOut(length)));
    }

    public void updateHungerProgressBarIncrease(float value) {
        this.hungerProgressIncrease.setValue(1.0f - value);
    }

    public void updateAirProgressBarIncrease(float value) {
        this.airProgressIncrease.setValue(value);
    }

    public void updateHealthProgressBarIncrease(float value) {
        this.healthProgressIncrease.setValue(1.0f - value);
    }

    public void hideProgressBarIncrease() {
        this.airProgressIncrease.setValue(0.0f);
        this.hungerProgressIncrease.setValue(1.0f);
        this.healthProgressIncrease.setValue(1.0f);
    }

    public void newDamageFlyoff(float amount, float x, float y, Color c) {
        int damage = MathUtils.round(amount);
        int offsetX = MathUtils.random(-10, 10);
        DamageFlyoff d = this.damageFlyoffPool.obtain();
        d = new DamageFlyoff();
        d.play(this.gameScreen, this.gameScreen.worldUIStage.getRoot(), x + (float)offsetX, y, "" + damage, c);
    }

    public void showPaintSelector() {
        this.showingPaintSelector = true;
        PaintSelector p = new PaintSelector(this.gameScreen, this.hudGroup);
    }

    public void showColorMixer(String set, int colorNum) {
        ColorMixer cm = new ColorMixer(this, this.menuGroup, set, colorNum);
        cm.show();
    }

    private static enum DayIconStates {
        day,
        night;

    }
}
