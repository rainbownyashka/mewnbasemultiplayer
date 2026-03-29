/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BaseGroup;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Footprints;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemActions;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.PlayerAnimController;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.PlayerStatus;
import com.cairn4.moonbase.PlayerStatusEffect;
import com.cairn4.moonbase.Scanner;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.SpineActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.dialog.DialogController;
import com.cairn4.moonbase.entities.Box2dLocation;
import com.cairn4.moonbase.entities.Buggie;
import com.cairn4.moonbase.entities.Creature;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.PlayerAttackRayCastCallback;
import com.cairn4.moonbase.entities.Speaker;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.tiles.Airlock;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.StorageColorOptions;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.CameraLag;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.HudNotificationData;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.MiniMap;
import com.cairn4.moonbase.ui.TechTreePopup;
import com.cairn4.moonbase.worlddata.TileOwners;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Observable;
import com.cairn4.moonbase.NetworkHelper;

public class Player
extends Observable
implements Speaker {
    int x;
    int y;
    int attackRadius = 110;
    public int minMeleeDamage = 2;
    public int maxMeleeDamage = 4;
    public static float BODY_RADIUS = 24.0f;
    int prevX;
    int prevY;
    int chunkX;
    int chunkY;
    int prevChunkX;
    int prevChunkY;
    Image image;
    World world;
    public int ownerId = TileOwners.PLAYER;
    Group group;
    Footprints footprints;
    public PlayerStatus playerStatus;
    Vector2 velocity;
    private Vector2 _lastReportedVelocity; // Added for multiplayer velocity reporting
    public static boolean altWalkSpeed = false;
    static final float WALK_ACCEL = 960.0f;
    static final float MAX_WALK_SPEED = Tile.GRID_SIZE * 1.75f;
    private float walkSpeedMultiplier = 1.0f;
    protected boolean busyInteracting = false;
    private static float windSpeedSlowWalkThreshold = 0.9f;
    private Vector2 facingVector;
    DecimalFormat df;
    private boolean indoors;
    private boolean stunned = false;
    private float stunTimer = 0.0f;
    public PlayerInventory playerInventory;
    public boolean changedChunks = false;
    protected boolean attacking = false;
    protected float attackingTimer = 0.0f;
    protected final float attackLength = 0.5f;
    public SpineActor spineActor;
    // Multiplayer helper fields
    public boolean isRemote = false;
    public com.badlogic.gdx.math.Vector2 targetPosition = new com.badlogic.gdx.math.Vector2();
    // Timed playback buffer for network positions (reduces snap at stops with small delay)
    private java.util.ArrayDeque<com.badlogic.gdx.math.Vector2> netPosQueue = new java.util.ArrayDeque<com.badlogic.gdx.math.Vector2>(8);
    private float netQueueTimer = 0f;
    private static final float NET_QUEUE_INTERVAL = 0.075f; // ~75ms buffer cadence
    // Player display name
    public String name = "";
    private com.badlogic.gdx.scenes.scene2d.ui.Label nameLabel;
    ShapeRenderer shapeRenderer;
    ConeLight light;
    PointLight flashlightGlow;
    PointLight footLight;
    PointLight tabletLight;
    ParticleActor eatParticles;
    ParticleActor fireParticles;
    Bone lightBone;
    Bone mouthBone;
    Bone rootBone;
    Bone handBone;
    public PlayerAnimController playerAnimController;
    Scanner scanner;
    protected short categoryBits;
    protected short maskBits;
    protected Body body;
    Vehicle vehicle;
    private boolean flyingRocket;
    public ArrayList<Integer> researchObjectsDiscovered = new ArrayList();
    public int suitLevel;
    private int paintColorIndex = 0;
    public Array<GridPoint2> markedMapLocations = new Array();
    private final int markedLocationLimit = 1;
    Tile interactTile;
    public int characterFaceOption;
    public String characterSuitColor;
    public static int faceOptions = 5;
    public static String[] suitColorList = new String[]{"ee662f", "c13d46", "f9dc3b", "5abf66", "36a4e6", "4152fb", "905aff", "393666", "e584db"};
    float normalDampening = 0.5f;
    float stunnedDampening = 10.0f;
    private Vector2 entityPosCheck;
    private float xPos;
    private float yPos;
    private Vector2 mouse = new Vector2(0.0f, 0.0f);
    private double xd;
    private double yd;
    private int xx;
    private int yy;
    private GridPoint2 interactPoint = new GridPoint2();
    int diffX;
    int diffY;
    private BaseGroup currentBase;
    private HabitatModule currentHabitatModule;

    public float getAttackRadius() {
        return this.attackRadius;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public boolean isIndoors() {
        return this.indoors;
    }

    public PlayerInventory getPlayerInventory() {
        return this.playerInventory;
    }

    @Override
    public float getXPos() {
        if (this.vehicle == null) {
            if (this.body == null) return 0.0f;
            return this.body.getPosition().x * 256.0f;
        }
        return this.vehicle.getWorldX();
    }

    @Override
    public float getYPos() {
        if (this.vehicle == null) {
            if (this.body == null) return 0.0f;
            return this.body.getPosition().y * 256.0f;
        }
        return this.vehicle.getWorldY();
    }

    @Override
    public float getHeight() {
        return 70.0f;
    }

    private void setXPos(float x) {
        this.body.setTransform(x / 256.0f, this.body.getPosition().y, 0.0f);
    }

    private void setYPos(float y) {
        this.body.setTransform(this.body.getPosition().x / 256.0f, y / 256.0f, 0.0f);
    }

    public boolean canInteractHere() {
        return true;
    }

    public Scanner getScanner() {
        return this.scanner;
    }

    public boolean isDrivingVehicle() {
        return this.vehicle != null;
    }

    public Vehicle getVehicle() {
        return this.vehicle;
    }

    // Multiplayer helper: enter vehicle without camera changes (remote players)
    public void enterVehicleRemote(Vehicle v, boolean asDriver) {
        try {
            if (v == null) return;
            this.setFlashLight(false);
            this.spineActor.setVisible(false);
            this.vehicle = v;
            if (asDriver) v.setDriver(this.ownerId);
            else v.setPassenger(this.ownerId);
            try { this.body.getFixtureList().get(0).setSensor(true); } catch (Exception ignored) {}
            this.setChanged();
            this.notifyObservers("enterVehicle");
        } catch (Exception ignored) {}
    }

    public void exitVehicleRemote() {
        try {
            if (this.vehicle != null) {
                this.vehicle.clearSeat(this.ownerId);
                this.vehicle = null;
            }
            try { this.body.getFixtureList().get(0).setSensor(false); } catch (Exception ignored) {}
            try { this.spineActor.setVisible(true); } catch (Exception ignored) {}
            try { if (this.playerStatus != null && this.playerStatus.getFlashlight()) this.light.setActive(true); } catch (Exception ignored) {}
            this.setChanged();
            this.notifyObservers("exitVehicle");
        } catch (Exception ignored) {}
    }

    public boolean isFlyingRocket() {
        return this.flyingRocket;
    }

    public void setFlyingRocket(boolean flying) {
        this.flyingRocket = flying;
        this.spineActor.setVisible(!flying);
        this.setFlashLight(false);
    }

    public void setPaintColorIndex(int i) {
        this.paintColorIndex = i;
        this.playerAnimController.updatePainttoolColor(StorageColorOptions.colors[this.paintColorIndex]);
    }

    public int getPaintColorIndex() {
        return this.paintColorIndex;
    }

    public void discoverResearchObject(int id) {
        int points = 5;
        System.out.println("Earned " + points + " research points");
        this.world.techManager.addSamples(points);
        this.setChanged();
        this.notifyObservers("earnedResearchPoint");
        boolean newObject = true;
        for (Integer i : this.researchObjectsDiscovered) {
            if (!i.equals(id)) continue;
            newObject = false;
        }
        if (newObject) {
            this.researchObjectsDiscovered.add(id);
            System.out.println("Research object discovered: " + id);
        }
        MoonBase.achievementAdapter.artifactResearched(this.researchObjectsDiscovered.size());
    }

    public void setSuitLevel(int suitLevel, boolean maxValues) {
        this.suitLevel = suitLevel;
        this.playerStatus.setSuitLevel(this.suitLevel, maxValues);
        switch (suitLevel) {
            case 0: {
                this.spineActor.skeleton.setSkin("mk1");
                break;
            }
            case 1: {
                this.spineActor.skeleton.setSkin("mk2");
                break;
            }
            default: {
                this.spineActor.skeleton.setSkin("mk1");
            }
        }
    }

    public Location<Vector2> getLocation() {
        return new Box2dLocation();
    }

    public void handleDialogInput() {
        if (Gdx.input.justTouched()) {
            DialogController.getInstance().forward(this);
        }
        PlayerInput.update();
    }

    public Vector2 getVelocity() {
        return this.body.getLinearVelocity().scl(256.0f);
    }

    // Added for multiplayer to get the actual velocity being sent
    public Vector2 getLastReportedVelocity() {
        return this._lastReportedVelocity;
    }

    public void forcePositionUpdate() {
        this.group.setPosition(this.getXPos(), this.getYPos() - 30.0f);
    }

    public boolean isOnFoot() {
        return !this.isFlyingRocket() && !this.isDrivingVehicle();
    }

    public void addMarkedLocation(int tileX, int tileY) {
        this.markedMapLocations.add(new GridPoint2(tileX, tileY));
        while (this.markedMapLocations.size > 1) {
            this.markedMapLocations.removeIndex(0);
        }
        MessageManager.getInstance().dispatchMessage(40);
    }

    public void removeMarkedLocation(int tileX, int tileY) {
        for (int i = this.markedMapLocations.size - 1; i >= 0; --i) {
            GridPoint2 gp = this.markedMapLocations.get(i);
            if (gp.x != tileX || gp.y != tileY) continue;
            MoonBase.log("Removed marked location");
            this.markedMapLocations.removeIndex(i);
            MoonBase.log("Size now: " + this.markedMapLocations.size);
            MessageManager.getInstance().dispatchMessage(41);
            break;
        }
    }

    public void setCustomizationOptions(int faceIndex, String suitColorHex) {
        this.characterFaceOption = MathUtils.clamp(faceIndex, 0, faceOptions);
        this.characterSuitColor = suitColorHex;
        Slot visorSlot = this.spineActor.skeleton.findSlot("visor");
        Color c = Color.valueOf(suitColorList[0]);
        try {
            c = Color.valueOf(this.characterSuitColor);
        }
        catch (Exception e) {
            MoonBase.error("Player:setCustomizationOptions: Invalid suit color in gameSave.json (" + this.characterSuitColor + "), resetting to default");
            this.characterSuitColor = suitColorList[0];
        }
        visorSlot.getColor().set(c);
        Slot chestColorSlot = this.spineActor.skeleton.findSlot("torso-color");
        chestColorSlot.getColor().set(c);
        Slot faceSlot = this.spineActor.skeleton.findSlot("face");
        Attachment a = this.spineActor.skeleton.getAttachment("face", "player/face" + this.characterFaceOption);
        faceSlot.setAttachment(a);
        Slot eyeSlot = this.spineActor.skeleton.findSlot("eyes-slot");
        Color eyeColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);
        if (eyeSlot != null) {
            eyeSlot.getColor().set(eyeColor);
        } else {
            MoonBase.error("cant find eyes");
        }
    }

    public Player(World world, Chunk chunk, int locX, int locY) {
        this(world, chunk.chunkX * 10 + locX, chunk.chunkY * 10 + locY);
    }

    public Player(World world, int x, int y) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.chunkX = Chunk.getChunkX(this.x);
        this.chunkY = Chunk.getChunkY(this.y);
        this.changedChunks = true;
        this.playerStatus = new PlayerStatus(this);
        this.setupPhysics();
        this.createDrawables();
        this.footprints = new Footprints(this.world.gameScreen, this);
        this.playerInventory = new PlayerInventory(this);
        this.velocity = new Vector2(0.0f, 0.0f);
        this._lastReportedVelocity = new Vector2(0.0f, 0.0f); // Initialize the new field
        this.shapeRenderer = new ShapeRenderer();
        this.shapeRenderer.setAutoShapeType(true);
        this.shapeRenderer.begin();
        this.light = new ConeLight(world.rayHandler, 100, new Color(1.0f, 0.9f, 0.8f, 0.8f), 1.7578125f, this.group.getX() / 256.0f, this.group.getY() / 256.0f, 0.0f, 48.0f);
        this.light.setContactFilter(this.categoryBits, this.categoryBits, this.maskBits);
        this.light.setIgnoreAttachedBody(true);
        this.light.setSoft(true);
        this.light.setSoftnessLength(0.5859375f);
        this.light.setActive(this.playerStatus.getFlashlight());
        this.flashlightGlow = new PointLight(world.rayHandler, 50, new Color(1.0f, 0.9f, 0.8f, 1.0f), 0.2734375f, this.group.getX() / 256.0f, this.group.getY() / 256.0f);
        this.flashlightGlow.setSoft(true);
        this.flashlightGlow.setPosition(this.getLightX() / 256.0f, this.getLightY() / 256.0f);
        this.flashlightGlow.setIgnoreAttachedBody(true);
        this.flashlightGlow.setXray(true);
        this.flashlightGlow.setActive(this.playerStatus.getFlashlight());
        this.footLight = new PointLight(world.rayHandler, 100, new Color(1.0f, 0.9f, 0.8f, 0.15f), 1.171875f, this.group.getX() / 256.0f, this.group.getY() / 256.0f);
        this.footLight.setSoft(true);
        this.footLight.attachToBody(this.body);
        this.footLight.setIgnoreAttachedBody(true);
        this.footLight.setXray(true);
        this.tabletLight = new PointLight(world.rayHandler, 20, new Color(Menu.MENU_COLOR), 0.234375f, this.group.getX() / 256.0f, this.group.getY() / 256.0f);
        this.tabletLight.setActive(false);
        this.tabletLight.setXray(true);
        int lightMask = 2;
        boolean zero = false;
        this.spineActor = new SpineActor("player", 0.27f, world.gameScreen.skeletonRenderer);
        this.spineActor.setPosition(0.0f, 0.0f);
        this.group.addActor(this.spineActor);
        this.spineActor.stateData = new AnimationStateData(this.spineActor.skeletonData);
        this.spineActor.stateData.setDefaultMix(0.1f);
        this.spineActor.stateData.setMix("idle", "walk", 0.2f);
        this.spineActor.stateData.setMix("walk", "idle", 0.2f);
        this.spineActor.stateData.setMix("eat", "idle", 0.2f);
        this.lightBone = this.spineActor.skeleton.findBone("light");
        this.rootBone = this.spineActor.skeleton.findBone("spine");
        this.mouthBone = this.spineActor.skeleton.findBone("mouth");
        this.handBone = this.spineActor.skeleton.findBone("tabletScreen");
        this.spineActor.state = new AnimationState(this.spineActor.stateData);
        Slot faceSlot = this.spineActor.skeleton.findSlot("face");
        Attachment a = this.spineActor.skeleton.getAttachment("face", "player/face3");
        faceSlot.setAttachment(a);
        this.spineActor.state.addAnimation(0, "idle", true, 0.0f);
        this.playerAnimController = new PlayerAnimController(this);
        this.playerAnimController.setupBlink();
        this.playerAnimController.setFlashlight(this.playerStatus.getFlashlight());
        this.playerAnimController.showEquippedItem(this.playerInventory.getEquippedItem());
        this.spineActor.skeleton.setSkin("mk1");
        this.scanner = new Scanner(this);
        this.df = new DecimalFormat("#.#");
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/player-eat.p", ParticleEffect.class));
        this.eatParticles = new ParticleActor(p, false);
        world.gameScreen.topGroup.addActor(this.eatParticles);
        ParticleEffect pFire = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/player-fire.p", ParticleEffect.class));
        this.fireParticles = new ParticleActor(pFire, false);
        this.fireParticles.pfx.allowCompletion();
        world.gameScreen.mainGroup.addActor(this.fireParticles);
    }

    protected void setupPhysics() {
        this.categoryBits = (short)4;
        this.maskBits = (short)82;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        CircleShape shape = new CircleShape();
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = this.categoryBits;
        fdef.filter.maskBits = (short)(this.maskBits + this.categoryBits);
        fdef.density = 1.0f;
        fdef.friction = 0.1f;
        fdef.restitution = 0.1f;
        Vector2 pos = new Vector2(((float)this.getX() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f) / 256.0f, ((float)this.getY() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f) / 256.0f);
        this.body = this.world.b2dWorld.createBody(bodyDef);
        this.body.setTransform(new Vector2(pos.x, pos.y), 0.0f);
        this.body.setFixedRotation(true);
        this.body.setLinearDamping(0.5f);
        this.body.setUserData(this);
        shape.setRadius(BODY_RADIUS / 256.0f);
        this.body.createFixture(fdef);
    }

    protected void createDrawables() {
        this.group = new Group();
        this.group.setUserObject(Float.valueOf(this.getYPos() - 30.0f));
        this.group.setPosition(this.getXPos(), this.getYPos() - 30.0f);
        this.world.gameScreen.addToStage(this.group, this.world.gameScreen.mainGroup);
        // create name label if name present
        try {
            if (this.nameLabel == null) {
                com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle ls = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
                ls.font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
                ls.fontColor = com.badlogic.gdx.graphics.Color.WHITE;
                this.nameLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label((CharSequence)"", ls);
                this.nameLabel.setFontScale(1.1f);
                this.nameLabel.setPosition(0, -40);
                this.group.addActor(this.nameLabel);
            }
            if (this.name != null && !this.name.isEmpty()) this.nameLabel.setText(this.name);
        } catch (Exception e) {
            // ignore UI failures
        }
    }

    public void setDisplayName(String s) {
        this.name = s;
        try {
            if (this.nameLabel != null) this.nameLabel.setText(s);
        } catch (Exception e) {
            // ignore
        }
    }

    public Chunk getCurrentChunk() {
        this.chunkX = Chunk.getChunkX(this.x);
        this.chunkY = Chunk.getChunkY(this.y);
        return this.world.getChunk(this.chunkX, this.chunkY);
    }

    public float getLightX() {
        if (this.lightBone != null) {
            return this.lightBone.getWorldX() + this.getXPos();
        }
        return 0.0f;
    }

    public float getLightY() {
        if (this.lightBone != null) {
            return this.lightBone.getWorldY() + this.getYPos() - 30.0f;
        }
        return 0.0f;
    }

    public float getHandX() {
        if (this.handBone != null) {
            return this.handBone.getWorldX() + this.getXPos();
        }
        return 0.0f;
    }

    public float getHandY() {
        if (this.handBone != null) {
            return this.handBone.getWorldY() + this.getYPos() - 30.0f;
        }
        return 0.0f;
    }

    public float getMouthX() {
        if (this.mouthBone != null) {
            return this.mouthBone.getWorldX() + this.getXPos();
        }
        return 0.0f;
    }

    public float getMouthY() {
        if (this.mouthBone != null) {
            return this.mouthBone.getWorldY() + this.getYPos() - 30.0f;
        }
        return 0.0f;
    }

    public float getRootBoneX() {
        if (this.rootBone != null) {
            return this.rootBone.getWorldX() + this.spineActor.skeleton.getX();
        }
        return 0.0f;
    }

    public float getRootBoneY() {
        if (this.rootBone != null) {
            return this.rootBone.getWorldY() + this.getYPos();
        }
        return 0.0f;
    }

    public void exitRocket() {
        this.spineActor.setVisible(true);
        this.spineActor.state.setAnimation(0, "exit_lander", false);
        this.spineActor.state.addAnimation(0, "idle", false, 0.0f);
    }

    public void exitLander() {
        Gdx.app.log("MoonBase", "Player: Playing exit lander anim");
        this.spineActor.state.setAnimation(0, "exit_lander", false);
        this.spineActor.state.addListener(new AnimationState.AnimationStateListener(){

            @Override
            public void start(AnimationState.TrackEntry entry) {
            }

            @Override
            public void interrupt(AnimationState.TrackEntry entry) {
            }

            @Override
            public void end(AnimationState.TrackEntry entry) {
            }

            @Override
            public void dispose(AnimationState.TrackEntry entry) {
            }

            @Override
            public void complete(AnimationState.TrackEntry entry) {
                Player.this.world.gameScreen.gameLoader.saveGame(Player.this.world, false);
                Player.this.world.gameState = World.GameStates.playing;
                Player.this.world.gameScreen.hud.activate(Player.this.world);
                Player.this.spineActor.state.clearListeners();
            }

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
            }
        });
    }

    public void update(float delta) {
        // If this is a remote player, interpolate towards targetPosition instead of using physics
        if (this.isRemote) {
            try {
                // consume queued network targets at a fixed cadence to add small buffer
                try {
                    this.netQueueTimer += delta;
                    while (this.netQueueTimer >= NET_QUEUE_INTERVAL && !this.netPosQueue.isEmpty()) {
                        com.badlogic.gdx.math.Vector2 q = this.netPosQueue.pollFirst();
                        if (q != null) this.targetPosition.set(q);
                        this.netQueueTimer -= NET_QUEUE_INTERVAL;
                    }
                } catch (Exception ignored) {}
                float curX = this.getXPos();
                float curY = this.getYPos();
                // compute error and speed
                float tx = this.targetPosition.x;
                float ty = this.targetPosition.y;
                float err = com.badlogic.gdx.math.Vector2.dst(curX, curY, tx, ty);
                float sp = (Math.abs(this.body.getLinearVelocity().x) + Math.abs(this.body.getLinearVelocity().y)) * 256.0f;
                // choose interpolation rate
                float alpha;
                if (err > 200f) {
                    // huge desync -> snap
                    alpha = 1f;
                } else if (sp < 1.0f) {
                    // stopping: converge быстрее, но без рывков
                    alpha = Math.min(1f, delta * 14f);
                } else {
                    // обычное движение
                    alpha = Math.min(1f, delta * 8f);
                }
                float nx = com.badlogic.gdx.math.MathUtils.lerp(curX, tx, alpha);
                float ny = com.badlogic.gdx.math.MathUtils.lerp(curY, ty, alpha);
                // set body transform directly
                try {
                    this.body.setTransform(nx / 256.0f, ny / 256.0f, 0.0f);
                    // near target and stopping: zero velocity to avoid overshoot
                    if (err < 2.0f && sp < 1.0f) {
                        this.body.setLinearVelocity(0f, 0f);
                    }
                } catch (Exception e) {
                    // ignore if body unavailable
                }
                this.group.setPosition(nx, ny - 30.0f);
            } catch (Exception e) {
                // Fall back to default
                this.group.setPosition(this.getXPos(), this.getYPos() - 30.0f);
            }
        } else {
            this.group.setPosition(this.getXPos(), this.getYPos() - 30.0f);
        }
        if (this.stunTimer > 0.0f) {
            this.stunTimer -= delta;
        } else {
            this.stunned = false;
            this.body.setLinearDamping(this.normalDampening);
        }
        if (this.attackingTimer > 0.0f) {
            this.attackingTimer -= delta;
            if (this.attackingTimer <= 0.0f) {
                this.attacking = false;
            }
        }
        if (!MoonBase.GOD_MODE) {
            this.playerStatus.update(delta);
        }
        this.playerStatus.updateStatusEffects(delta);
        if (this.vehicle != null) {
            this.buggieMove();
        }
        this.world.gameScreen.hud.updateInteractCursor();
        this.updateLight();
        this.eatParticles.setPosition(this.getMouthX(), this.getMouthY());
        this.fireParticles.setPosition(this.getMouthX(), this.getMouthY());
        this.fireParticles.setUserObject(Float.valueOf(this.getYPos() + 2.0f));
        this.scanner.update(delta);
        if (this.interactTile != null) {
            this.interactTile.updateInteract(this, delta);
        }
        this.playerAnimController.updateWindyWeatherPose(this.world.weatherManager.getWindSpeed(), this.walkSpeedMultiplier);
        if (this.vehicle == null && !this.flyingRocket && !this.world.isTileBase(this.x, this.y) && this.velocity.len() > 0.0f) {
            this.footprints.update(this.velocity);
        }
        this.toggleInsideOutside();
        this.spineActor.update(delta);
    }

    // Enqueue next network position for buffered playback
    public void enqueueNetPos(float x, float y) {
        try {
            if (this.netPosQueue.size() >= 6) {
                this.netPosQueue.pollFirst();
            }
            this.netPosQueue.addLast(new com.badlogic.gdx.math.Vector2(x, y));
        } catch (Exception ignored) {}
    }

    public void dispose() {
        try {
            if (this.group != null) this.group.remove();
        } catch (Exception e) {
            // ignore
        }
        try {
            if (this.body != null && this.body.getWorld() != null) this.body.getWorld().destroyBody(this.body);
        } catch (Exception e) {
            // ignore
        }
        this.body = null;
    }

    private void updateLight() {
        this.light.setPosition(this.getLightX() / 256.0f, this.getLightY() / 256.0f);
        this.flashlightGlow.setPosition(this.getLightX() / 256.0f, this.getLightY() / 256.0f);
        this.footLight.setPosition(this.getXPos() / 256.0f, this.getYPos() / 256.0f);
        this.tabletLight.setPosition(this.getHandX() / 256.0f, this.getHandY() / 256.0f);
    }

    public void clearInteractingTile(boolean instantStopAnim) {
        this.busyInteracting = false;
        this.playerAnimController.clearInteractionAnim(instantStopAnim);
        this.world.gameScreen.hud.removeTileProgressBar(this.interactTile);
        this.interactTile = null;
    }

    public void handleInput() {
        if (!this.isFlyingRocket()) {
            this.handleGeneralInput();
            if (this.vehicle != null) {
                this.handleBuggieInput();
            } else {
                this.handleWalkInput();
            }
        } else {
            this.handleLanderControls();
        }
        PlayerInput.update();
        this.world.resetEntityClicks();
    }

    private void handleLanderControls() {
        if (PlayerInput.wasPressed(6) && this.world.gameScreen.menuStack.isEmpty()) {
            this.world.gameScreen.hud.hideLaunchControls();
        }
    }

    private void handleGeneralInput() {
        if (PlayerInput.wasPressed(7)) {
            this.playerInventory.changeSelection(-1);
        }
        if (PlayerInput.wasPressed(8)) {
            this.playerInventory.changeSelection(1);
        }
        if (PlayerInput.scrolledDown()) {
            this.clearInteractingTile(true);
            this.playerInventory.changeSelection(-1);
            this.playerInventory.equipItem();
            Audio.getInstance().playSound("sounds/pop.mp3", 0.3f, 1.3f);
        }
        if (PlayerInput.scrolledUp()) {
            this.clearInteractingTile(true);
            this.playerInventory.changeSelection(1);
            this.playerInventory.equipItem();
            Audio.getInstance().playSound("sounds/pop.mp3", 0.3f, 1.5f);
        }
        if (PlayerInput.wasPressed(12)) {
            this.handleInventoryNumInput(0);
        }
        if (PlayerInput.wasPressed(13)) {
            this.handleInventoryNumInput(1);
        }
        if (PlayerInput.wasPressed(14)) {
            this.handleInventoryNumInput(2);
        }
        if (PlayerInput.wasPressed(15)) {
            this.handleInventoryNumInput(3);
        }
        if (PlayerInput.wasPressed(16)) {
            this.handleInventoryNumInput(4);
        }
        if (PlayerInput.wasPressed(17)) {
            this.handleInventoryNumInput(5);
        }
        if (PlayerInput.wasPressed(18)) {
            this.handleInventoryNumInput(6);
        }
        if (PlayerInput.wasPressed(19)) {
            this.handleInventoryNumInput(7);
        }
        if (PlayerInput.wasPressed(20)) {
            this.handleInventoryNumInput(8);
        }
        if (PlayerInput.wasPressed(21)) {
            this.handleInventoryNumInput(9);
        }
        if (PlayerInput.wasPressed(24)) {
            this.clearInteractingTile(true);
            this.toggleVehicle();
        }
        if (PlayerInput.wasPressed(6) && this.world.gameScreen.menuStack.isEmpty()) {
            this.world.pauseGame();
        }
        if (PlayerInput.wasPressed(26)) {
            this.world.gameScreen.game.toggleFullScreen();
        }
        if (PlayerInput.wasPressed(27)) {
            this.world.gameScreen.showMenu(new TechTreePopup(this.world.gameScreen));
            this.world.player.playerAnimController.openTablet();
        }
    }

    public void handleWalkInput() {
        float dX = 0.0f;
        float dY = 0.0f;
        if (PlayerInput.isDown(0)) {
            dX -= 1.0f;
        }
        if (PlayerInput.isDown(1)) {
            dX += 1.0f;
        }
        if (PlayerInput.isDown(2)) {
            dY += 1.0f;
        }
        if (PlayerInput.isDown(3)) {
            dY -= 1.0f;
        }
        if (dX == 0.0f && PlayerInput.stickMoveX != 0.0f) {
            dX = PlayerInput.stickMoveX;
        }
        if (dY == 0.0f && PlayerInput.stickMoveY != 0.0f) {
            dY = PlayerInput.stickMoveY;
        }
        this.playerAnimController.setDirection(dX, dY);
        if (!this.stunned) {
            if (dX != 0.0f || dY != 0.0f) {
                this.playerAnimController.updateWalkDirection(dX, dY);
                this.walkMove(dX, dY);
            } else {
                this.velocity.set(0.0f, 0.0f);
                this.walkMove(0.0f, 0.0f);
            }
        }
        if (dX != 0.0f || dY != 0.0f) {
            this.light.setDirection(this.velocity.angle());
        }
        float facingX = 0.0f;
        float facingY = 0.0f;
        if (PlayerInput.stick2MoveX != 0.0f) {
            facingX = PlayerInput.stick2MoveX;
        }
        if (PlayerInput.stick2MoveY != 0.0f) {
            facingY = PlayerInput.stick2MoveY;
        }
        if (facingX != 0.0f || facingY != 0.0f) {
            if (this.facingVector == null) {
                this.facingVector = new Vector2(0.0f, 0.0f);
            }
            this.facingVector.set(facingX, facingY);
            this.setFlashlightDirection(this.facingVector.angle());
        }
        if (PlayerInput.wasPressed(22)) {
            this.openMap();
        }
        if (PlayerInput.wasPressed(9)) {
            ItemStack droppedStack = null;
            if (Gdx.input.isKeyPressed(59)) {
                droppedStack = this.playerInventory.getSelectedItem();
                if (droppedStack != null) { droppedStack = new ItemStack(droppedStack.item.id, 1); }
                this.playerInventory.dropItem();
            } else {
                droppedStack = this.playerInventory.getSelectedItem();
                this.playerInventory.dropStack();
            }

            if (droppedStack != null) {
                try {
                    String encodedItemId = java.net.URLEncoder.encode(droppedStack.getId(), "UTF-8");
                    String payload = "ITEM_DROP:" + this.x + ":" + this.y + ":" + encodedItemId + ":" + droppedStack.getAmount();
                    NetworkHelper.sendPayload(this.world.gameScreen, payload);
                } catch (Exception e) {
                    Gdx.app.error("Player", "Failed to send ITEM_DROP to server", e);
                }
            }

            GameScreen gameScreen = this.world.gameScreen;
            if (gameScreen.game.getCurrentMission().missionType == Mission.MissionTypes.tutorial) {
                this.setChanged();
                this.notifyObservers("droppedItem");
            }
        }
        if (PlayerInput.wasPressed(11)) {
            this.toggleFlashlight();
        }
        if (PlayerInput.wasPressed(23)) {
            this.playerInventory.rotatePlacementOrientation();
        }
        if (Gdx.input.justTouched()) {
            float inputX = Gdx.input.getX();
            float inputY = Gdx.input.getY();
            Vector2 stagePos = this.world.gameScreen.hudStage.screenToStageCoordinates(new Vector2(inputX, inputY));
            Actor hitUI = this.world.gameScreen.hudStage.hit(stagePos.x, stagePos.y, true);
            if (hitUI == null) {
                Entity entity;
                if (!this.busyInteracting) {
                    this.clearInteractingTile(true);
                    this.interactTile = null;
                }
                if (Gdx.input.isButtonPressed(0)) {
                    boolean attack = false;
                    if (!this.attacking && !this.stunned) {
                        attack = this.attackCreature();
                    }
                    if (!attack) {
                        this.interact();
                    }
                }
                if (Gdx.input.isButtonPressed(1) && (entity = this.checkForEntityClick()) == null) {
                    this.interact_secondary();
                }
            } else {
                MoonBase.log("----------------\nHit a ui thing! " + hitUI.getClass().getSimpleName());
            }
        }
        if (Gdx.input.isButtonPressed(1) && dX == 0.0f && dY == 0.0f) {
            Entity entity = this.checkForEntityClick();
            if (entity != null) {
                entity.playerActionSecondary(this);
            } else {
                this.basePickup();
            }
        }
        if (PlayerInput.wasPressed(28)) {
            System.out.println("Controller->Player Interact!");
            this.interact();
        }
        if (PlayerInput.isDown(29) && dX == 0.0f && dY == 0.0f) {
            System.out.println("interact secondary pressed");
            Entity entity = this.checkForEntityClick();
            if (entity != null) {
                entity.playerActionSecondary(this);
            } else {
                System.out.println("base pickup...");
                this.basePickup();
            }
        } else if (PlayerInput.wasPressed(29)) {
            System.out.println("Controller->Player secondary Interact!");
            this.interact_secondary();
        }
    }

    public void openMap() {
        if (this.world.gameScreen.inputMode != GameScreen.inputModes.menu) {
            this.playerAnimController.openTablet();
            Timer timer = new Timer();
            timer.scheduleTask(new Timer.Task(){

                @Override
                public void run() {
                    Player.this.world.gameScreen.showMenu(new MiniMap(Player.this.world.gameScreen));
                }
            }, 0.65f);
        }
    }

    private boolean attackCreature() {
        Creature creatureToHit = null;
        if (this.entityPosCheck == null) {
            this.entityPosCheck = new Vector2(0.0f, 0.0f);
        }
        if (this.mouse == null) {
            this.mouse = new Vector2(0.0f, 0.0f);
        }
        this.mouse.set(Gdx.input.getX(), Gdx.input.getY());
        this.mouse = this.world.gameScreen.stage.screenToStageCoordinates(this.mouse);
        float dist = this.mouse.dst(this.getXPos(), this.getYPos());
        if (dist < (float)this.attackRadius) {
            for (Entity e : this.world.entityList) {
                Creature c;
                if (!(e instanceof Creature) || !(c = (Creature)e).isAlive()) continue;
                this.entityPosCheck.set(e.getXPos(), e.getYPos());
                Circle eCircle = new Circle(this.entityPosCheck, c.creatureData.hitRadius);
                if (!eCircle.contains(this.mouse)) continue;
                if (dist < c.creatureData.hitRadius) {
                    creatureToHit = c;
                    break;
                }
                if (!this.attackRaycastCheck(c)) continue;
                creatureToHit = c;
                break;
            }
            if (creatureToHit != null) {
                this.attacking = true;
                this.attackingTimer = 0.5f;
                this.playerAnimController.attackSwing(creatureToHit);
                MoonBase.log("Attacking = true");
            }
        }
        return creatureToHit != null;
    }

    private boolean attackRaycastCheck(Creature creature) {
        PlayerAttackRayCastCallback callback = new PlayerAttackRayCastCallback();
        this.body.getWorld().rayCast(callback, this.body.getTransform().getPosition(), creature.getBody().getTransform().getPosition());
        return callback.hitCreatureFirst();
    }

    private void setFlashLight(boolean on) {
        this.playerStatus.setFlashlight(on);
        this.flashlightGlow.setActive(on);
        this.light.setActive(on);
    }

    private void toggleFlashlight() {
        this.playerStatus.setFlashlight(!this.playerStatus.getFlashlight());
        this.light.setActive(this.playerStatus.getFlashlight());
        this.flashlightGlow.setActive(this.playerStatus.getFlashlight());
    }

    private void handleBuggieInput() {
        if (this.vehicle != null && !this.vehicle.isDriver(this.ownerId)) {
            // Passenger: no driving input
            return;
        }
        Entity entity;
        boolean steering2 = false;
        if (PlayerInput.isDown(0)) {
            steering2 = true;
            this.vehicle.setSteering(Vehicle.steering.left);
        }
        if (PlayerInput.isDown(1)) {
            steering2 = true;
            this.vehicle.setSteering(Vehicle.steering.right);
        }
        if (PlayerInput.stickMoveX < 0.0f) {
            steering2 = true;
            this.vehicle.setSteering(Vehicle.steering.left);
        }
        if (PlayerInput.stickMoveX > 0.0f) {
            steering2 = true;
            this.vehicle.setSteering(Vehicle.steering.right);
        }
        if (SettingsLoader.getInstance().settingsData.VIRTUALJOYSTICK) {
            float mouseY;
            float mouseX = Gdx.input.getX();
            Vector2 mouseOffset = this.vehicle.group.screenToLocalCoordinates(new Vector2(mouseX, mouseY = (float)Gdx.input.getY()));
            float a = mouseOffset.angle();
            if (a > 210.0f && a < 335.0f) {
                steering2 = true;
                this.vehicle.setSteering(Vehicle.steering.right);
            } else if (a > 30.0f && a < 150.0f) {
                steering2 = true;
                this.vehicle.setSteering(Vehicle.steering.left);
            }
        }
        if (!steering2) {
            this.vehicle.setSteering(Vehicle.steering.none);
        }
        boolean accel = false;
        if (SettingsLoader.getInstance().settingsData.VIRTUALJOYSTICK) {
            if (Gdx.input.isButtonPressed(0)) {
                accel = true;
                this.vehicle.setAcceleration(Vehicle.acceleration.acceleration);
            }
            if (Gdx.input.isButtonPressed(1)) {
                accel = true;
                this.vehicle.setAcceleration(Vehicle.acceleration.brake);
            }
        }
        if (PlayerInput.isDown(2) || PlayerInput.isDown(28)) {
            accel = true;
            this.vehicle.setAcceleration(Vehicle.acceleration.acceleration);
        }
        if (PlayerInput.isDown(3) || PlayerInput.isDown(29)) {
            accel = true;
            this.vehicle.setAcceleration(Vehicle.acceleration.brake);
        }
        if (!accel) {
            this.vehicle.setAcceleration(Vehicle.acceleration.none);
        }
        if (this.vehicle != null) {
            if (PlayerInput.isDown(25)) {
                this.vehicle.setSpecialAbility(true);
            } else {
                this.vehicle.setSpecialAbility(false);
            }
        }
        if (PlayerInput.wasPressed(22)) {
            this.vehicle.setAcceleration(Vehicle.acceleration.none);
            this.world.gameScreen.showMenu(new MiniMap(this.world.gameScreen));
        }
        if (Gdx.input.isButtonPressed(0) && (entity = this.checkForEntityClick()) != null && entity.equals(this.vehicle)) {
            entity.playerAction(this);
        }
        if (Gdx.input.isButtonPressed(1) && (entity = this.checkForEntityClick()) != null && entity.equals(this.vehicle)) {
            entity.playerActionSecondary(this);
        }
    }

    private void handleInventoryNumInput(int inputIndex) {
        this.clearInteractingTile(true);
        if (this.world.gameScreen.menuStack.isEmpty()) {
            this.playerInventory.setSelection(inputIndex);
            this.playerInventory.equipItem();
        }
    }

    private void basePickup() {
        this.clearInteractingTile(true);
        if (this.interactWithinRange()) {
            BaseModule b;
            GridPoint2 interact = this.getInteractPoint2();
            if (this.world.getTile(interact.x, interact.y) instanceof BaseModule) {
                BaseModule b2 = (BaseModule)this.world.getTile(interact.x, interact.y);
                if (b2.canPickup()) {
                    b2.pickingUpProgress();
                }
            } else if (this.world.getFloorTile(interact.x, interact.y) instanceof BaseModule && (b = (BaseModule)this.world.getFloorTile(interact.x, interact.y)).canPickup()) {
                b.pickingUpProgress();
            }
        }
    }

    public void interact() {
        this.clearInteractingTile(true);
        if (this.interactWithinRange()) {
            GridPoint2 interact = this.getInteractPoint2();
            MoonBase.debug("Player.interact() at tile " + interact.x + ", " + interact.y);
            Entity entity = this.checkForEntityClick();
            if (entity != null) {
                entity.playerAction(this);
            } else if (!this.world.isTileEmpty(interact.x, interact.y)) {
                Tile t = this.world.getTile(interact.x, interact.y);
                if (t != null && t.canInteract(this)) {
                    this.interactTile = t;
                    this.world.gameScreen.hud.addTileProgressBar(this.interactTile);
                    this.playerAnimController.interactAnim(t);
                }
                MoonBase.debug("Player.interact() with Tile");
            } else if (this.world.getFloorTile(interact.x, interact.y) != null) {
                Tile t = this.world.getFloorTile(interact.x, interact.y);
                if (t != null && t.canInteract(this)) {
                    this.interactTile = t;
                    this.world.gameScreen.hud.addTileProgressBar(this.interactTile);
                    this.playerAnimController.interactAnim(t);
                }
                MoonBase.debug("Player.interact() with Floor Tile");
            }
        } else {
            MoonBase.debug("Player: Too far to interact");
        }
    }

    private void interact_secondary() {
        ItemStack stack = this.playerInventory.getSelectedItem();
        if (stack != null && stack.item != null) {
            boolean proxyWithinRange = this.proxyPlacementInRange(stack.item.getData(), this.interactPoint.x, this.interactPoint.y);
            if (this.interactWithinRange() || proxyWithinRange) {
                boolean buildable = false;
                for (ItemActions ia : stack.item.actions) {
                    if (!ia.type.equals(Item.ActionTypes.createBuilding.toString()) && !ia.type.equals(Item.ActionTypes.createFloor.toString()) && !ia.type.equals(Item.ActionTypes.createItemDropper.toString())) continue;
                    buildable = true;
                }
                if (buildable) {
                    boolean canPlaceHere = this.locationClearToBuild(stack.item.getData());
                    if (canPlaceHere) {
                        this.playerInventory.useItem();
                        this.setChanged();
                        this.notifyObservers("placedBuilding");
                    } else {
                        HudNotificationData msg = new HudNotificationData();
                        msg.message = Localization.get("cantBuildHere");
                        MessageManager.getInstance().dispatchMessage(3, msg);
                    }
                }
            }
        }
    }

    public boolean proxyPlacementInRange(ItemData itemData, int placementX, int placementY) {
        if (itemData != null && itemData.proxyTiles.size() > 0) {
            for (GridPoint2 gp : itemData.proxyTiles) {
                int x = placementX + gp.x;
                int y = placementY + gp.y;
                int diffX = Math.abs(x - this.world.getPlayer().getX());
                int diffY = Math.abs(y - this.world.getPlayer().getY());
                if (diffX > 1 || diffY > 1) continue;
                return true;
            }
        }
        return false;
    }

    private boolean locationClearToBuild(ItemData itemData) {
        this.interactPoint = this.getInteractPoint2();
        Tile t = this.world.getTile(this.interactPoint.x, this.interactPoint.y);
        if (t != null) {
            return false;
        }
        if (itemData != null) {
            for (GridPoint2 gp : itemData.proxyTiles) {
                int x = this.interactPoint.x + gp.x;
                int y = this.interactPoint.y + gp.y;
                if (this.world.isTileEmpty(x, y)) continue;
                return false;
            }
        }
        return true;
    }

    private Entity checkForEntityClick() {
        for (Entity e : this.world.entityList) {
            if (!e.isClicked()) continue;
            return e;
        }
        return null;
    }

    public void moveToTile(int newX, int newY) {
        this.x = newX;
        this.y = newY;
        float xPos = (float)newX * Tile.GRID_SIZE + Tile.TILE_SIZE / 2.0f;
        float yPos = (float)newY * Tile.GRID_SIZE + Tile.TILE_SIZE / 2.0f;
        this.body.setTransform(xPos / 256.0f, yPos / 256.0f, 0.0f);
        this.chunkX = Chunk.getChunkX(this.x);
        this.chunkY = Chunk.getChunkY(this.y);
        this.setChanged();
        this.notifyObservers("playerMoved");
        this.updateWorldDiscovery();
    }

    public void walkMove(float dX, float dY) {
        Tile floor = this.world.getFloorTile(this.x, this.y);
        if (floor != null) {
            this.walkSpeedMultiplier = floor.getMovementMultiplier();
        } else {
            GroundTile gt = this.world.getGroundTile(this.x, this.y);
            if (gt != null) {
                this.walkSpeedMultiplier = gt.getMovementMultiplier();
            }
        }
        if (!this.isIndoors() && this.world.weatherManager.getWindSpeed() > windSpeedSlowWalkThreshold) {
            this.walkSpeedMultiplier *= 0.66f;
        }
        for (PlayerStatusEffect pse : this.playerStatus.statusEffects) {
            this.walkSpeedMultiplier = pse.applyWalkSpeedModifier(this.walkSpeedMultiplier);
        }
        float delta = Gdx.graphics.getDeltaTime();
        if (altWalkSpeed) {
            this.velocity.set(dX * this.walkSpeedMultiplier * MAX_WALK_SPEED * delta, dY * this.walkSpeedMultiplier * MAX_WALK_SPEED * delta);
        } else {
            this.velocity.add(dX * 960.0f * this.walkSpeedMultiplier * delta, dY * 960.0f * this.walkSpeedMultiplier * delta);
        }
        this.velocity.limit(MAX_WALK_SPEED * this.walkSpeedMultiplier);
        this.body.setLinearVelocity(this.velocity.x / 256.0f, this.velocity.y / 256.0f);
        this._lastReportedVelocity.set(this.velocity); // Update the new field
        this.move(dX, dY);
    }

    private void buggieMove() {
        this.body.setTransform(this.vehicle.body.getPosition().x, this.vehicle.body.getPosition().y, this.vehicle.body.getAngle());
        this.move(-1.0f, -1.0f);
    }

    public void move(float dX, float dY) {
        if (this.interactTile != null && (dX != 0.0f || dY != 0.0f)) {
            this.clearInteractingTile(true);
            if (this.scanner.isScanning()) {
                this.scanner.cancel();
            }
        }
        float xPos = this.body.getPosition().x * 256.0f - Tile.TILE_SIZE / 2.0f;
        float yPos = this.body.getPosition().y * 256.0f - Tile.TILE_SIZE / 2.0f;
        this.x = Math.round(xPos / Tile.GRID_SIZE);
        this.y = Math.round(yPos / Tile.GRID_SIZE);
        this.setChanged();
        this.notifyObservers("playerMoved");
        this.group.setUserObject(Float.valueOf(this.getYPos()));
        if (this.x != this.prevX || this.y != this.prevY) {
            this.updateWorldDiscovery();
            this.playerStatus.updateCurrentHabitatModule();
            this.updateCurrentBase();
        }
        this.chunkX = Chunk.getChunkX(this.x);
        this.chunkY = Chunk.getChunkY(this.y);
        if (this.chunkX != this.prevChunkX || this.chunkY != this.prevChunkY) {
            Gdx.app.log("MoonBase", "Player: changed chunks!: " + this.chunkX + ", " + this.chunkY);
            this.changedChunks = true;
        }
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevChunkX = this.chunkX;
        this.prevChunkY = this.chunkY;
    }

    public void updateWorldDiscovery() {
        this.world.updateDiscovery(this.x, this.y, 4);
        this.world.updateNearbyTileVisibility();
    }

    public String getPositionInfo() {
        GridPoint2 i = this.getInteractPoint2();
        String r = "";
        r = r + "Pos: " + this.x + ", " + this.y + "\n";
        r = r + "Interaction: " + i.x + ", " + i.y + "\n";
        if (this.world.getGroundTile(this.x, this.y) != null) {
            r = r + "Biome: " + (Object)((Object)this.world.getGroundTile(this.x, this.y).getBiome()) + "\n";
            r = r + "Chunk: " + this.chunkX + ", " + this.chunkY + "\n";
        }
        return r;
    }

    public void collect(Item item) {
        this.playerInventory.add(new ItemStack(item), true);
        this.inventoryUpdate();
    }

    public void collect(ItemStack stack) {
        this.playerInventory.add(stack, true);
        this.inventoryUpdate();
    }

    public void collect(ItemStack stack, boolean hudNotify) {
        this.playerInventory.add(stack, false);
        this.inventoryUpdate();
    }

    public void inventorySelectionChange() {
        this.clearInteractingTile(true);
        this.playerAnimController.showEquippedItem(this.playerInventory.getEquippedItem());
        this.setChanged();
        this.notifyObservers("inventorySelectionChanged");
    }

    public void inventoryUpdate() {
        this.playerAnimController.showEquippedItem(this.playerInventory.getEquippedItem());
        this.setChanged();
        this.notifyObservers("updateInventory");
    }

    public void notifyUpdate(String arg) {
        this.setChanged();
        this.notifyObservers(arg);
    }

    public GridPoint2 getInteractPoint2() {
        float cX = PlayerInput.stick2MoveX * Tile.TILE_SIZE + this.getXPos();
        float cY = PlayerInput.stick2MoveY * Tile.TILE_SIZE + this.getYPos();
        this.mouse.set(cX, cY);
        if (PlayerInput.lastUsedMouse) {
            this.mouse.set(Gdx.input.getX(), Gdx.input.getY());
            this.mouse = this.world.gameScreen.stage.screenToStageCoordinates(this.mouse);
        }
        this.xd = Math.floor(this.mouse.x / Tile.GRID_SIZE);
        this.yd = Math.floor(this.mouse.y / Tile.GRID_SIZE);
        this.xx = Math.round((float)this.xd);
        this.yy = Math.round((float)this.yd);
        this.interactPoint.set(this.xx, this.yy);
        return this.interactPoint;
    }

    public boolean interactWithinRange() {
        this.interactPoint = this.getInteractPoint2();
        this.diffX = Math.abs(this.x - this.interactPoint.x);
        this.diffY = Math.abs(this.y - this.interactPoint.y);
        return this.diffX <= 1 && this.diffY <= 1;
    }

    private void updateCurrentBase() {
        BaseGroup base = this.getCurrentBase();
        if (base != this.currentBase) {
            this.currentBase = base;
            MessageManager.getInstance().dispatchMessage(50);
        }
    }

    public BaseGroup getCurrentBase() {
        Tile t = this.world.getTile(this.x, this.y);
        if (t != null && (t.getType() == Tile.types.base || t.getType() == Tile.types.habitat)) {
            BaseModule b = (BaseModule)t;
            return b.getBaseGroup();
        }
        return null;
    }

    public HabitatModule getHabitatModule() {
        Tile t = this.world.getTile(this.x, this.y);
        if (t != null && t instanceof HabitatModule) {
            return (HabitatModule)t;
        }
        return null;
    }

    public void scan() {
        this.spineActor.state.setAnimation(0, "interact", true);
        this.getScanner().startScan();
    }

    public void finishedScanning() {
        this.spineActor.state.setAnimation(0, "idle", true);
    }

    public void buildingRotationUpdate() {
        this.setChanged();
        this.notifyObservers("rotateBuildingPlacement");
    }

    public void toggleVehicle() {
        if (this.vehicle == null) {
            Vehicle closestBuggie = null;
            float minDistance = Buggie.interactDistance;
            for (Entity e : this.world.entityList) {
                Vehicle v;
                if (!(e instanceof Vehicle) || !(v = (Vehicle)e).isDrivable()) continue;
                Vector2 worldPos = new Vector2(v.getXPos(), v.getYPos());
                float distance = worldPos.dst(this.getXPos(), this.getYPos());
                Gdx.app.debug("MewnBase", "Player: Found vehicle, distance: " + distance);
                if (!(distance < minDistance)) continue;
                minDistance = distance;
                closestBuggie = v;
            }
            if (closestBuggie != null) {
                if (!closestBuggie.hasFreeSeat()) {
                    MoonBase.log("Player: Vehicle is full");
                    return;
                }
                this.setFlashLight(false);
                this.spineActor.setVisible(false);
                this.vehicle = closestBuggie;
                this.vehicle.setState(Vehicle.STATES.inUse);
                if (!this.vehicle.hasDriver()) {
                    this.vehicle.setDriver(this.ownerId);
                } else if (!this.vehicle.hasPassenger()) {
                    this.vehicle.setPassenger(this.ownerId);
                }
                this.body.getFixtureList().get(0).setSensor(true);
                Gdx.app.debug("MewnBase", "Player: Enter closest vehicle (distance = " + minDistance + ")");
                this.setChanged();
                this.notifyObservers("enterVehicle");
                this.world.gameScreen.cameraLag.setZoom(CameraLag.DRIVEZOOM);
                this.world.gameScreen.cameraLag.toggleBuggie();
                try {
                    if (this.world != null && this.world.gameScreen != null) {
                        if (this.world.gameScreen.client != null) {
                            NetworkHelper.sendPayload(this.world.gameScreen, "VEH_ENTER:" + this.vehicle.id);
                        } else {
                            com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                            if (s != null) {
                                String occ = "VEH_OCCUPY:" + this.vehicle.id + ":" + this.vehicle.driverOwnerId + ":" + this.vehicle.passengerOwnerId;
                                s.broadcastFromServer(occ);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        } else {
            double xd = Math.floor(this.vehicle.body.getTransform().getPosition().x * 256.0f / Tile.GRID_SIZE);
            double yd = Math.floor(this.vehicle.body.getTransform().getPosition().y * 256.0f / Tile.GRID_SIZE);
            int xx = Math.round((float)xd);
            int yy = Math.round((float)yd);
            ArrayList<GridPoint2> adjacent = Tile.GET_ADJACENT_COORDS(xx, yy, true);
            ArrayList<GridPoint2> emptyTiles = new ArrayList<GridPoint2>();
            for (GridPoint2 a : adjacent) {
                if (this.world.getTile(a.x, a.y) == null) {
                    emptyTiles.add(a);
                    continue;
                }
                if (this.world.getTile((int)a.x, (int)a.y).blocker) continue;
                emptyTiles.add(a);
            }
            float closetToCursorDist = 1.0E8f;
            GridPoint2 closetGP = null;
            Vector2 mousePos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            for (GridPoint2 gp : emptyTiles) {
                Vector2 stagePos = new Vector2((float)gp.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)gp.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
                Vector2 screenPos = this.world.gameScreen.stage.stageToScreenCoordinates(stagePos);
                float dist = mousePos.dst(screenPos);
                if (dist < closetToCursorDist) {
                    closetToCursorDist = dist;
                    closetGP = gp;
                }
                MoonBase.log("gp: " + gp + " - dist: " + dist + " / " + closetToCursorDist);
            }
            if (closetGP != null) {
                int exitedVehicleId = -1;
                int exitedDriver = -1;
                int exitedPassenger = -1;
                try {
                    if (this.vehicle != null) {
                        exitedVehicleId = (int)this.vehicle.id;
                    }
                } catch (Exception ignored) {}
                try {
                    if (this.vehicle != null) {
                        this.vehicle.clearSeat(this.ownerId);
                        if (this.vehicle.driverOwnerId < 0 && this.vehicle.passengerOwnerId >= 0) {
                            this.vehicle.promotePassengerToDriver();
                        }
                        exitedDriver = this.vehicle.driverOwnerId;
                        exitedPassenger = this.vehicle.passengerOwnerId;
                    }
                } catch (Exception ignored) {}
                this.vehicle = null;
                this.body.getFixtureList().get(0).setSensor(false);
                MoonBase.log("Player: Exit vehicle at " + closetGP);
                this.moveToTile(closetGP.x, closetGP.y);
                this.world.gameScreen.cameraLag.setZoom(CameraLag.WALKZOOM);
                this.world.gameScreen.cameraLag.toggleBuggie();
                if (this.playerStatus.getFlashlight()) {
                    this.light.setActive(true);
                }
                this.spineActor.setVisible(true);
                this.setChanged();
                this.notifyObservers("exitVehicle");
                try {
                    if (exitedVehicleId >= 0 && this.world != null && this.world.gameScreen != null) {
                        if (this.world.gameScreen.client != null) {
                            NetworkHelper.sendPayload(this.world.gameScreen, "VEH_EXIT:" + exitedVehicleId);
                        } else {
                            com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                            if (s != null) {
                                String occ = "VEH_OCCUPY:" + exitedVehicleId + ":" + exitedDriver + ":" + exitedPassenger;
                                s.broadcastFromServer(occ);
                            }
                        }
                    }
                } catch (Exception ignored) {}
            } else {
                MoonBase.log("Player: Cant exit vehicle here.");
            }
        }
    }

    public void toggleInsideOutside() {
        boolean currentIndoors = this.checkIndoors();
        if (this.indoors != currentIndoors) {
            this.indoors = currentIndoors;
            Audio.getInstance().stopLoopSound(this.world.ambientWorldSound, this.world.ambientWorldSoundId);
            if (this.indoors) {
                this.world.ambientWorldSound = Audio.getInstance().getSound("music/wind_indoors.ogg");
                this.world.ambientWorldSoundId = Audio.getInstance().playSoundLoop(this.world.ambientWorldSound, 0.5f, 1.0f, 0.0f);
            } else {
                this.world.ambientWorldSound = Audio.getInstance().getSound("music/wind.ogg");
                this.world.ambientWorldSoundId = Audio.getInstance().playSoundLoop(this.world.ambientWorldSound, 0.5f, 1.0f, 0.0f);
            }
        }
    }

    private boolean checkIndoors() {
        if (this.getCurrentBase() != null) {
            this.currentHabitatModule = this.getHabitatModule();
            if (this.currentHabitatModule != null) {
                if (this.currentHabitatModule instanceof Airlock) {
                    Airlock a = (Airlock)this.currentHabitatModule;
                    return !a.toggleBehavior.on;
                }
                return true;
            }
        }
        return false;
    }

    public boolean isOnHabitat() {
        if (this.getCurrentBase() != null) {
            this.currentHabitatModule = this.getHabitatModule();
            if (this.currentHabitatModule instanceof HabitatModule) {
                return true;
            }
        }
        return false;
    }

    protected void setFlashlightDirection(float angle) {
        this.light.setDirection(angle);
    }

    public void applyKnockback(float xSource, float ySource, float strength) {
        Vector2 force = new Vector2(this.getXPos() - xSource, this.getYPos() - ySource);
        force.setLength(0.05f);
        this.body.applyLinearImpulse(force.x, force.y, this.getXPos(), this.getYPos(), true);
        this.stunned = true;
        this.stunTimer = 0.3f;
        this.body.setLinearDamping(this.stunnedDampening);
        this.playerAnimController.setStun();
    }

    public static enum DIRECTIONS {
        up,
        down,
        left,
        right;

    }
}
