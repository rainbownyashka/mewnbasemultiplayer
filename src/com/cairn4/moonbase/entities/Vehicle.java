/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.DamageTaker;
import com.cairn4.moonbase.ItemActions;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.Repairable;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.BuggieTrunk;
import com.cairn4.moonbase.entities.Creature;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.VehicleBattery;
import com.cairn4.moonbase.entities.Wheel;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Garage;
import com.cairn4.moonbase.tiles.StorageColorOptions;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.BuggieTrunkUI;
import com.cairn4.moonbase.ui.HudNotificationData;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.worlddata.DamageDef;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import com.cairn4.moonbase.worlddata.VehicleData;
import com.cairn4.moonbase.worlddata.VehicleDataList;
import com.cairn4.moonbase.worlddata.WheelData;
import java.util.ArrayList;
import java.util.HashMap;

public class Vehicle
extends Entity
implements Repairable,
DamageTaker {
    public Vehicle parentVehicle;
    public Vehicle childVehicle;
    public long parentVehicleId = -1L;
    public static ArrayList<VehicleData> vehicleDataList = new ArrayList();
    protected VehicleData vd;
    Group chassisGroup;
    public static float interactDistance = 200.0f;
    public float health;
    protected float steeringAngle = 0.0f;
    Sound engineSound;
    protected long engineSoundId;
    private float shakeTimer = 0.0f;
    protected float rotation;
    protected ParticleActor dustTrail;
    protected ParticleActor damageSmoke;
    protected ParticleActor repairParticles;
    AdditiveImage headLightSprite;
    ConeLight headlights;
    AdditiveImage tailLightSprite;
    PointLight tailLight;
    protected Image shadow;
    protected float shadowOffset = -14.0f;
    protected Group drivingHead;
    protected Image playerDrivingImage;
    protected Image playerDrivingVisor;
    protected Image playerDrivingFace;
    public Body body;
    protected short categoryBits;
    protected short maskBits;
    protected Color paintColor = Color.WHITE;
    public int paintColorIndex;
    public DamageDef damageDef;
    steering currentSteering;
    acceleration currentAccelration;
    public STATES currentState;
    public boolean enginePowerAvailable = true;
    private DamageStates currentDamageState;
    boolean powerSliding = false;
    private boolean specialAbility = false;
    // Multiplayer seat ownership (ownerId values). -1 means empty.
    public int driverOwnerId = -1;
    public int passengerOwnerId = -1;
    // Multiplayer interpolation (remote-controlled vehicles)
    public boolean netControlled = false;
    private boolean netHasTarget = false;
    private float netTargetX = 0.0f;
    private float netTargetY = 0.0f;
    private float netTargetRot = 0.0f;
    protected ArrayList<Wheel> wheels = new ArrayList();
    public BuggieTrunkUI trunkUI;
    public BuggieTrunk trunk;
    public Array<InventoryItemData> inventoryItemDataList = new Array();
    public ArrayList<ItemStack> trunkItems = new ArrayList();
    // Multiplayer lock for inventory UI interactions (-1 = unlocked)
    public int inventoryLockOwnerId = -1;
    float startCrashSpeed = 0.0f;
    private boolean repairing = false;
    private boolean prevRepairCheck;
    float minDelay = 0.1f;
    float maxDelay = 0.25f;
    float animTimer;

    public boolean isDrivable() {
        return this.vd.drivable;
    }

    public void setDrift(float d) {
        this.vd.drift = d;
    }

    public String getHealth() {
        return Localization.format("vehicle_health", MathUtils.round(this.health / this.vd.maxHealth * 100.0f));
    }

    protected void setPowerSlide(boolean b) {
        this.powerSliding = b;
    }

    public void setSpecialAbility(boolean b) {
        this.specialAbility = b;
    }

    public boolean isSpecialAbilityEnabled() {
        return this.specialAbility;
    }

    public boolean hasDriver() { return this.driverOwnerId >= 0; }
    public boolean hasPassenger() { return this.passengerOwnerId >= 0; }
    public boolean hasFreeSeat() { return !hasDriver() || !hasPassenger(); }
    public boolean isDriver(int ownerId) { return ownerId >= 0 && this.driverOwnerId == ownerId; }
    public boolean isPassenger(int ownerId) { return ownerId >= 0 && this.passengerOwnerId == ownerId; }

    public void setNetTarget(float x, float y, float rot) {
        this.netTargetX = x;
        this.netTargetY = y;
        this.netTargetRot = rot;
        this.netHasTarget = true;
    }

    public void setDriver(int ownerId) {
        this.driverOwnerId = ownerId;
        if (this.currentState != STATES.inUse) this.setState(STATES.inUse);
    }

    public void setPassenger(int ownerId) {
        this.passengerOwnerId = ownerId;
        if (this.currentState != STATES.inUse) this.setState(STATES.inUse);
    }

    public void clearSeat(int ownerId) {
        if (this.driverOwnerId == ownerId) this.driverOwnerId = -1;
        if (this.passengerOwnerId == ownerId) this.passengerOwnerId = -1;
        if (!hasDriver() && !hasPassenger()) {
            this.setState(STATES.empty);
        }
    }

    public void clearAllSeats() {
        this.driverOwnerId = -1;
        this.passengerOwnerId = -1;
        this.setState(STATES.empty);
    }

    public void promotePassengerToDriver() {
        if (!hasDriver() && hasPassenger()) {
            this.driverOwnerId = this.passengerOwnerId;
            this.passengerOwnerId = -1;
            if (this.currentState != STATES.inUse) this.setState(STATES.inUse);
        }
    }

    public Vehicle(World world, float xPos, float yPos, float rotation, String vehicleId) {
        super(world, xPos, yPos);
        this.loadVehicleData(vehicleId);
        MoonBase.log("Spawning " + vehicleId);
        this.rotation = rotation;
        this.zSort = true;
        this.createDrawable(this.vd.id);
        if (this.vd.trailerHitch) {
            this.addTrailerHitch();
        }
        this.addDriverHead();
        this.setupPhysics(this.vd.physicsBody);
        this.addWheels();
        this.addLights();
        this.addParticles();
        this.addEngineSound();
        this.animTimer = 0.0f;
        this.health = this.vd.maxHealth;
        this.currentAccelration = acceleration.none;
        this.currentSteering = steering.none;
        if (this.vd.trunkStorageSize > 0) {
            this.trunk = new BuggieTrunk(this, this.vd.trunkStorageSize);
        }
        this.currentState = STATES.empty;
        this.damageDef = new DamageDef();
        this.damageDef.damageType = DamageDef.DamageTypes.physical;
    }

    @Override
    public void doneLoading() {
        super.doneLoading();
        this.setColor(this.paintColorIndex);
    }

    @Override
    public boolean repair(ItemStack itemStack) {
        if (this.health < this.vd.maxHealth) {
            ItemActions repairAction = null;
            for (ItemActions ia : itemStack.item.actions) {
                if (!ia.type.equals("repair")) continue;
                repairAction = ia;
                try {
                    this.repairInstant(Float.valueOf(repairAction.value).floatValue());
                    HudNotificationData msg = new HudNotificationData();
                    msg.icon = "health-particle-red";
                    msg.message = Localization.format("vehicle_health", Vars.wholeNum.format(this.health / this.vd.maxHealth * 100.0f));
                    MessageManager.getInstance().dispatchMessage(3, msg);
                    return true;
                }
                catch (Exception e) {
                    MoonBase.error("Non integer repair action value in items.json");
                }
            }
        } else {
            HudNotificationData msg = new HudNotificationData();
            msg.message = Localization.get("vehicle_alreadyRepaired");
            MessageManager.getInstance().dispatchMessage(3, msg);
        }
        return false;
    }

    public void drawDebug() {
    }

    private void loadVehicleData(String idToLoad) {
        if (vehicleDataList.size() == 0) {
            FileHandle fileHandle = Gdx.files.local(MoonBase.coreFolder + "vehicles.json");
            if (!fileHandle.exists()) {
                Gdx.app.error("MewnBase", "vehicles.json does not exist");
            } else {
                Gdx.app.log("MewnBase", "Vehicle: Reading vehicle data");
                Json json = new Json();
                String fileText = fileHandle.readString();
                vehicleDataList = json.fromJson(VehicleDataList.class, (String)fileText).vehicleDataList;
                Gdx.app.debug("MewnBase", "vehicles: list size: " + vehicleDataList.size());
            }
        }
        if (vehicleDataList.size() != 0) {
            for (VehicleData vehicleData : vehicleDataList) {
                if (!vehicleData.id.equals(idToLoad)) continue;
                this.vd = vehicleData;
                break;
            }
        }
    }

    protected void addLights() {
        if (this.vd.headlightDistance > 0.0f) {
            this.headlights = new ConeLight(this.world.rayHandler, 100, new Color(1.0f, 1.0f, 1.0f, 1.0f), this.vd.headlightDistance / 256.0f, this.group.getX() / 256.0f, this.group.getY() / 256.0f, 0.0f, this.vd.headlightAngle);
            this.headlights.setContactFilter((short)4, (short)4, (short)2);
            this.headlights.attachToBody(this.body);
            this.headlights.setSoft(true);
            this.headlights.setSoftnessLength(1.5625f);
            this.headlights.setIgnoreAttachedBody(true);
        }
        this.tailLight = new PointLight(this.world.rayHandler, 100, new Color(1.0f, 0.0f, 0.0f, 0.5f), 0.15625f, this.group.getX() / 256.0f, this.group.getY() / 256.0f);
        this.tailLight.attachToBody(this.body, -0.234375f, 0.0f);
        this.tailLight.setIgnoreAttachedBody(true);
        this.tailLight.setXray(true);
    }

    protected void addParticles() {
        this.damageSmoke = new ParticleActor(Gdx.files.internal("particles/buggie-damage-smoke.p"), this.world.gameScreen.atlas, false);
        this.world.gameScreen.mainGroup.addActor(this.damageSmoke);
        this.damageSmoke.toBack();
        this.dustTrail = new ParticleActor(Gdx.files.internal("particles/buggie_dust.p"), this.world.gameScreen.atlas, false);
        this.world.gameScreen.mainGroup.addActor(this.dustTrail);
        this.dustTrail.toBack();
        this.dustTrail.pfx.allowCompletion();
        this.repairParticles = new ParticleActor(Gdx.files.internal("particles/buggie-repair.p"), this.world.gameScreen.atlas, false);
        this.world.gameScreen.mainGroup.addActor(this.repairParticles);
        this.repairParticles.toFront();
    }

    public void createDrawable(String sprite) {
        this.shadow = new Image(this.world.gameScreen.skin.getDrawable("vehicle-shadow"));
        this.shadow.setSize(160.0f, 160.0f);
        this.shadow.setColor(0.0f, 0.0f, 0.0f, 0.3f);
        this.shadow.setTouchable(Touchable.disabled);
        this.shadow.setOrigin(1);
        this.world.gameScreen.addToStage(this.shadow, this.world.gameScreen.floorGroup);
        this.group = new Group();
        this.group.setPosition(this.spawnPosX, this.spawnPosY);
        this.group.setUserObject(Float.valueOf(this.getYPos()));
        this.world.gameScreen.addToStage(this.group, this.world.gameScreen.mainGroup);
        this.chassisGroup = new Group();
        this.group.addActor(this.chassisGroup);
    }

    public void spawnAnim() {
        this.group.addAction(Actions.sequence((Action)Actions.scaleTo(1.1f, 1.1f), (Action)Actions.alpha(0.0f), (Action)Actions.parallel((Action)Actions.fadeIn(0.2f), (Action)Actions.sequence((Action)Actions.scaleTo(0.9f, 0.9f, 0.2f), (Action)Actions.scaleTo(1.02f, 1.02f, 0.06f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.06f)))));
    }

    public void addTrailerHitch() {
        Image hitch = new Image(this.world.gameScreen.skin.getDrawable("trailer-hitch"));
        hitch.setPosition(-65.0f, 0.0f, 1);
        hitch.setColor(0.7f, 0.7f, 0.7f, 1.0f);
        this.group.addActor(hitch);
        hitch.toBack();
    }

    protected void addDriverHead() {
        if (this.vd.driverHead) {
            this.drivingHead = new Group();
            this.drivingHead.setOrigin(50.0f, 0.0f);
            this.drivingHead.setPosition(this.vd.driverHeadX, this.vd.driverHeadY);
            this.chassisGroup.addActor(this.drivingHead);
            this.drivingHead.setVisible(false);
            this.playerDrivingFace = new Image(this.world.gameScreen.skin.getDrawable("buggie-face" + this.world.player.characterFaceOption));
            this.playerDrivingFace.setSize(29.0f, 28.5f);
            this.playerDrivingFace.setPosition(4.0f, 0.0f, 1);
            this.playerDrivingFace.setTouchable(Touchable.disabled);
            this.drivingHead.addActor(this.playerDrivingFace);
            this.playerDrivingVisor = new Image(this.world.gameScreen.skin.getDrawable("buggie-head-visor"));
            this.playerDrivingVisor.setSize(21.0f, 42.0f);
            this.playerDrivingVisor.setPosition(-3.0f, 0.0f, 1);
            this.playerDrivingVisor.setTouchable(Touchable.disabled);
            this.playerDrivingVisor.setColor(Color.valueOf(this.world.player.characterSuitColor));
            this.drivingHead.addActor(this.playerDrivingVisor);
            this.playerDrivingImage = new Image(this.world.gameScreen.skin.getDrawable("buggie-head-main"));
            this.playerDrivingImage.setSize(50.0f, 55.0f);
            this.playerDrivingImage.setPosition(0.0f, 0.0f, 1);
            this.playerDrivingImage.setTouchable(Touchable.disabled);
            this.drivingHead.addActor(this.playerDrivingImage);
        }
    }

    public void setupPhysics(String loaderObject) {
        this.categoryBits = (short)64;
        this.maskBits = (short)14;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        FixtureDef fd = new FixtureDef();
        fd.density = this.vd.physicsDensity;
        fd.friction = this.vd.physicsFriction;
        fd.restitution = 0.1f;
        fd.filter.categoryBits = this.categoryBits;
        fd.filter.maskBits = (short)(this.maskBits + this.categoryBits);
        this.body = this.world.b2dWorld.createBody(bodyDef);
        this.body.setTransform(new Vector2(this.spawnPosX / 256.0f, this.spawnPosY / 256.0f), this.rotation * ((float)Math.PI / 180));
        this.body.setFixedRotation(false);
        this.body.setLinearDamping(2.0f);
        this.body.setAngularDamping(this.vd.physicsAngularDampening);
        this.body.setBullet(true);
        this.body.setUserData(this);
        this.world.bodyEditorLoader.attachFixture(this.body, loaderObject, fd, 0.5f);
    }

    protected void addEngineSound() {
        if (this.vd.engineSoundFile != null && !this.vd.engineSoundFile.equals("")) {
            this.engineSound = AssetManagerSingleton.getInstance().get(this.vd.engineSoundFile, Sound.class);
        }
    }

    public void toggleDriverHead(boolean on) {
        if (this.drivingHead != null) {
            this.drivingHead.clearActions();
            if (on) {
                this.drivingHead.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.5f, 0.5f), (Action)Actions.moveTo(this.vd.driverHeadX - 5.0f, this.vd.driverHeadY), (Action)Actions.parallel((Action)Actions.visible(true), (Action)Actions.moveTo(this.vd.driverHeadX, this.vd.driverHeadY, 0.2f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.2f), (Action)Actions.fadeIn(0.15f))));
                this.chassisGroup.clearActions();
                this.chassisGroup.addAction(Actions.sequence((Action)Actions.scaleTo(0.9f, 0.9f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.2f)));
            } else {
                this.drivingHead.setVisible(false);
            }
        }
    }

    public void toggleAllLights(boolean on) {
        if (this.headlights != null) {
            this.headlights.setActive(on);
        }
        if (this.headLightSprite != null) {
            this.headLightSprite.setVisible(on);
        }
        if (this.tailLight != null) {
            this.tailLight.setActive(on);
        }
        if (this.tailLightSprite != null) {
            this.tailLightSprite.setVisible(on);
        }
    }

    public void addWheels() {
        for (WheelData wd : this.vd.wheelDataArrayList) {
            this.wheels.add(new Wheel(this, new Vector2(wd.x, wd.y), wd.turning, wd.powered, wd.reversedTurning));
        }
    }

    public void setState(STATES newState) {
        this.currentState = newState;
    }

    public void setSteering(steering s) {
        this.currentSteering = s;
    }

    public void setAcceleration(acceleration a) {
        this.currentAccelration = a;
    }

    public float getWorldX() {
        return this.body.getTransform().getPosition().x * 256.0f;
    }

    public float getWorldY() {
        return this.body.getTransform().getPosition().y * 256.0f;
    }

    @Override
    public void playerAction(Player player) {
        ItemStack itemStack;
        boolean usingTool = false;
        boolean didRepair = false;
        boolean didRecharge = false;
        if (this.currentState == STATES.empty && (itemStack = player.playerInventory.getEquippedItem()) != null) {
            ItemData id = ItemFactory.getItemData(itemStack.getId());
            for (ItemActions ia : id.actions) {
                if (ia.type.equals("paint")) {
                    usingTool = true;
                    player.playerAnimController.paint();
                    this.setColor(player.getPaintColorIndex());
                    this.paintPuffFx();
                }
                if (ia.type.equals("repair")) {
                    usingTool = true;
                    didRepair = this.repair(itemStack);
                    if (didRepair) {
                        player.playerInventory.consumeItem(itemStack, 1);
                    }
                }
                if (ia.type.equals("rechargeVehicle")) {
                    if (!(this instanceof VehicleBattery)) continue;
                    usingTool = true;
                    VehicleBattery v = (VehicleBattery)((Object)this);
                    didRecharge = v.recharge(itemStack, player);
                    if (!didRecharge) continue;
                    player.playerInventory.consumeItem(itemStack, 1);
                    continue;
                }
                MoonBase.log(this.getClass().getSimpleName());
            }
        }
        if (!usingTool) {
            player.toggleVehicle();
        }
    }

    public void setColor(int colorIndex) {
        this.image.setColor(Vars.vehicleColors[colorIndex]);
        this.image.getColor().a = 1.0f;
        this.paintColorIndex = colorIndex;
    }

    public ArrayList<InventoryItemData> buildTrunkItemDataList() {
        ArrayList<InventoryItemData> list = new ArrayList<InventoryItemData>();
        try {
            for (ItemStack s : this.trunkItems) {
                InventoryItemData iid = new InventoryItemData();
                iid.itemId = s.getId();
                iid.amount = s.getAmount();
                try { iid.durability = s.item.durability; } catch (Exception ignored) {}
                list.add(iid);
            }
        } catch (Exception ignored) {}
        return list;
    }

    public void applyTrunkItemDataList(ArrayList<InventoryItemData> list) {
        if (list == null) return;
        try {
            this.trunkItems.clear();
            for (InventoryItemData iid : list) {
                try {
                    ItemStack newStack = new ItemStack(iid.itemId, iid.amount, iid.durability);
                    int durabilityForThisType = ItemFactory.getDurability(iid.itemId);
                    if (durabilityForThisType > 0 && iid.durability == 0) {
                        iid.durability = durabilityForThisType;
                        try { newStack.item.durability = iid.durability; } catch (Exception ignored) {}
                    }
                    this.trunkItems.add(newStack);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    // Network state snapshot for multiplayer sync (vehicle-only).
    public HashMap<String, Object> getNetState() {
        HashMap<String, Object> s = new HashMap<String, Object>();
        s.put("health", Float.valueOf(this.health));
        s.put("paintColorIndex", Integer.valueOf(this.paintColorIndex));
        s.put("enginePowerAvailable", Boolean.valueOf(this.enginePowerAvailable));
        if (this.currentState != null) s.put("state", this.currentState.toString());
        s.put("specialAbility", Boolean.valueOf(this.specialAbility));
        return s;
    }

    public void applyNetState(HashMap<String, Object> s) {
        if (s == null) return;
        try {
            if (s.containsKey("paintColorIndex")) {
                int idx = safeInt(s.get("paintColorIndex"), this.paintColorIndex);
                if (idx != this.paintColorIndex) this.setColor(idx);
            }
            if (s.containsKey("health")) {
                float h = safeFloat(s.get("health"), this.health);
                this.health = h;
                if (this.health < this.vd.damagedThreshold) {
                    this.setDamageSprite();
                    try { this.damageSmoke.pfx.start(); } catch (Exception ignored) {}
                } else {
                    this.setFixedSprite();
                    try { this.damageSmoke.pfx.allowCompletion(); } catch (Exception ignored) {}
                }
            }
            if (s.containsKey("enginePowerAvailable")) {
                this.enginePowerAvailable = safeBool(s.get("enginePowerAvailable"), this.enginePowerAvailable);
            }
            if (s.containsKey("state")) {
                try {
                    String st = String.valueOf(s.get("state"));
                    if (st != null && st.length() > 0) {
                        STATES ns = STATES.valueOf(st);
                        if (this.currentState != ns) this.setState(ns);
                    }
                } catch (Exception ignored) {}
            }
            if (s.containsKey("specialAbility")) {
                boolean b = safeBool(s.get("specialAbility"), this.specialAbility);
                if (b != this.specialAbility) this.setSpecialAbility(b);
            }
        } catch (Exception ignored) {}
    }

    protected int safeInt(Object o, int def) {
        try {
            if (o instanceof Number) return ((Number)o).intValue();
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) { return def; }
    }

    protected float safeFloat(Object o, float def) {
        try {
            if (o instanceof Number) return ((Number)o).floatValue();
            return Float.parseFloat(String.valueOf(o));
        } catch (Exception e) { return def; }
    }

    protected boolean safeBool(Object o, boolean def) {
        try {
            if (o instanceof Boolean) return ((Boolean)o).booleanValue();
            return Boolean.parseBoolean(String.valueOf(o));
        } catch (Exception e) { return def; }
    }

    protected long safeLong(Object o, long def) {
        try {
            if (o instanceof Number) return ((Number)o).longValue();
            return Long.parseLong(String.valueOf(o));
        } catch (Exception e) { return def; }
    }

    public void startCollision() {
        this.startCrashSpeed = this.getSpeedKMH();
    }

    private void checkForGarageRepair(float delta) {
        int worldY;
        double xd = Math.floor(this.body.getTransform().getPosition().x * 256.0f / Tile.GRID_SIZE);
        double yd = Math.floor(this.body.getTransform().getPosition().y * 256.0f / Tile.GRID_SIZE);
        int worldX = Math.round((float)xd);
        Tile t = this.world.getTile(worldX, worldY = Math.round((float)yd));
        if (t != null && t instanceof Garage && t.hasPower) {
            this.repair(delta);
        }
    }

    protected void repairInstant(float percentToRepair) {
        if (this.health < this.vd.maxHealth) {
            this.health += this.vd.maxHealth * percentToRepair;
        }
        if (this.health > this.vd.maxHealth) {
            this.health = this.vd.maxHealth;
        }
        this.updateDamageStates();
        ParticleActor hitFx = new ParticleActor(Gdx.files.internal("particles/repair-instant.p"), this.world.gameScreen.atlas, true);
        hitFx.setPosition(this.getWorldX(), this.getWorldY());
        hitFx.pfx.start();
        this.world.gameScreen.topGroup.addActor(hitFx);
        Audio.getInstance().playSound("sounds/repair.ogg", 0.75f);
    }

    protected void repair(float delta) {
        this.repairing = false;
        if (this.health < this.vd.maxHealth) {
            this.health += delta * 4.0f;
            this.repairing = true;
        }
        if (this.health > this.vd.maxHealth) {
            this.health = this.vd.maxHealth;
            Gdx.app.debug("MewnBase", "Buggie: Finished repairing!");
            this.repairing = false;
        }
        this.updateDamageStates();
        if (this.repairing && !this.prevRepairCheck) {
            this.damageSmoke.setPosition(this.group.getX(), this.group.getY());
            this.repairParticles.pfx.start();
        } else if (!this.repairing && this.prevRepairCheck) {
            this.repairParticles.pfx.allowCompletion();
        }
        this.prevRepairCheck = this.repairing;
    }

    protected void updateDamageStates() {
        if (this.health > this.vd.veryDamagedThreshold) {
            this.currentDamageState = DamageStates.damaged;
            if (this.health > this.vd.damagedThreshold) {
                this.currentDamageState = DamageStates.good;
                this.setFixedSprite();
                this.damageSmoke.pfx.allowCompletion();
                for (Wheel w : this.wheels) {
                    w.fix();
                }
            }
        }
    }

    protected Vector2 getLocalVelocity() {
        return this.body.getLocalVector(this.body.getLinearVelocityFromLocalPoint(Vector2.Zero));
    }

    public void setSpeed(float speed) {
        Vector2 velocity = this.body.getLinearVelocity().nor();
        velocity = new Vector2(velocity.x * (speed * 1000.0f / 3600.0f), velocity.y * (speed * 1000.0f / 3600.0f));
        this.body.setLinearVelocity(velocity);
    }

    public float getSpeedKMH() {
        Vector2 velocity = this.body.getLinearVelocity();
        float len = velocity.len();
        return len / 1000.0f * 3600.0f;
    }

    public float getSpeedFraction() {
        return this.getSpeedKMH() / this.vd.maxSpeed;
    }

    @Override
    public float getXPos() {
        if (this.body != null) {
            return this.body.getPosition().x * 256.0f;
        }
        return 0.0f;
    }

    @Override
    public float getYPos() {
        if (this.body != null) {
            return this.body.getPosition().y * 256.0f;
        }
        return 0.0f;
    }

    @Override
    public void update(float delta) {
        if (this.netControlled && this.netHasTarget) {
            try {
                float curX = this.getXPos();
                float curY = this.getYPos();
                float curRot = this.group.getRotation();
                float alpha = Math.min(1.0f, delta * 10.0f);
                float rotAlpha = Math.min(1.0f, delta * 8.0f);
                float nx = MathUtils.lerp(curX, this.netTargetX, alpha);
                float ny = MathUtils.lerp(curY, this.netTargetY, alpha);
                float nrot = MathUtils.lerpAngleDeg(curRot, this.netTargetRot, rotAlpha);
                if (this.body != null) {
                    this.body.setTransform(nx / 256.0f, ny / 256.0f, nrot * MathUtils.degreesToRadians);
                    this.body.setLinearVelocity(0.0f, 0.0f);
                }
            } catch (Exception ignored) {}
        } else {
            this.updatePhysics(delta);
        }
        super.update(delta);
        this.group.setPosition(this.getXPos(), this.getYPos());
        this.group.setRotation(57.295776f * this.body.getAngle());
        this.shadow.setPosition(this.group.getX(), this.group.getY() + this.shadowOffset, 1);
        this.shadow.setRotation(this.group.getRotation());
        if (this.currentState == STATES.inUse) {
            this.vehicleShake(delta);
        }
        if (this.getSpeedKMH() > 0.1f) {
            this.updateWheels(this.getSpeedKMH() / this.vd.maxSpeed, delta);
        }
        for (Wheel w : this.wheels) {
            w.tracks.update(w.getWorldRotation());
        }
        if (this.getSpeedKMH() < 0.1f) {
            this.checkForGarageRepair(delta);
        } else {
            this.repairing = false;
        }
        if (this.prevRepairCheck) {
            this.repairParticles.setPosition(this.group.getX(), this.group.getY());
            if (!this.repairing) {
                this.repairParticles.pfx.allowCompletion();
                this.prevRepairCheck = false;
            }
        }
    }

    protected void updatePhysics(float delta) {
        if (this.powerSliding) {
            for (Wheel w : this.wheels) {
                if (!w.turning) continue;
                w.killSidewaysVelocity(this.vd.drift);
            }
        } else {
            for (Wheel w : this.wheels) {
                w.killSidewaysVelocity(1.0f);
            }
        }
        if (this.currentSteering == steering.left) {
            if (this.steeringAngle < this.vd.maxSteeringAngle) {
                this.steeringAngle += this.vd.steeringRate * delta;
            }
        } else if (this.currentSteering == steering.right) {
            if (this.steeringAngle > -this.vd.maxSteeringAngle) {
                this.steeringAngle -= this.vd.steeringRate * delta;
            }
        } else {
            this.steeringAngle = 0.0f;
        }
        for (Wheel w : this.wheels) {
            if (!w.turning || !w.functioning) continue;
            w.setAngle(this.steeringAngle);
        }
        Vector2 base_vect = new Vector2(0.0f, 0.0f);
        this.updateDampening();
        if (this.currentAccelration == acceleration.acceleration && this.getSpeedKMH() < this.vd.maxSpeed) {
            base_vect.set(1.0f, 0.0f);
            this.setTailLightAlpha(0.5f);
        } else if (this.currentAccelration == acceleration.brake) {
            this.setTailLightAlpha(1.0f);
            if (this.getLocalVelocity().x > 0.0f) {
                base_vect.set(-0.7f, 0.0f);
            } else if (this.getSpeedKMH() < this.vd.maxSpeed * 0.4f) {
                base_vect.set(-0.5f, 0.0f);
            }
        } else {
            this.setTailLightAlpha(0.5f);
            base_vect.set(0.0f, 0.0f);
        }
        Vector2 fvect = new Vector2(0.0f, 0.0f);
        if (this.enginePowerAvailable) {
            fvect.set(base_vect.x * this.vd.wheelPower, base_vect.y * this.vd.wheelPower);
        } else {
            fvect.set(0.0f, 0.0f);
        }
        for (Wheel w : this.wheels) {
            if (!w.isPowered || !w.functioning) continue;
            w.wheel.applyForce(w.wheel.getWorldVector(fvect), w.wheel.getWorldCenter(), true);
        }
        if (this.powerSliding) {
            this.setTailLightAlpha(1.0f);
        }
    }

    public void updateDampening() {
        if (this.currentAccelration == acceleration.acceleration) {
            this.body.setLinearDamping(0.5f);
        } else {
            this.body.setLinearDamping(2.0f);
        }
    }

    public float getDampening() {
        return this.body.getLinearDamping();
    }

    protected void setTailLightAlpha(float a) {
        if (this.tailLightSprite != null) {
            this.tailLightSprite.getColor().a = a;
        }
        if (this.tailLight != null) {
            this.tailLight.setColor(1.0f, 0.0f, 0.0f, a);
        }
    }

    public void updateWheels(float speedFactor, float delta) {
        this.animTimer += delta;
        if (this.animTimer >= this.maxDelay - (speedFactor * (this.maxDelay - this.minDelay) + this.minDelay)) {
            this.updateWheelFrameAnim();
            this.animTimer = 0.0f;
        }
    }

    protected void updateWheelFrameAnim() {
        for (Wheel w : this.wheels) {
            if (!w.functioning) continue;
            w.changeWheelFrame();
        }
    }

    private void breakRandomWheel() {
        int r = MathUtils.random(0, this.wheels.size() - 1);
        this.wheels.get(r).damage();
        if (this.world.gameScreen.hud != null) {
            this.world.gameScreen.hud.hudNotifications.newMessage(Localization.get("buggie_brokenWheel"), Color.RED);
        }
    }

    public void finishCrash(final Object o) {
        float endCrashSpeed = this.getSpeedKMH();
        final float diff = Math.abs(this.startCrashSpeed - endCrashSpeed);
        boolean damage = false;
        if (diff > 3.5f && endCrashSpeed > 1.5f) {
            damage = true;
            Audio.getInstance().playSound("sounds/buggie-crash2.ogg", diff * 0.05f, MathUtils.random(0.8f, 1.1f));
        } else if (!(diff > 2.5f) || endCrashSpeed > 1.5f) {
            // empty if block
        }
        if (damage) {
            System.out.println("Do damage");
            this.takeDamage(diff, true);
            if (o instanceof BaseModule) {
                Gdx.app.log("MewnBase", "Damaging " + o.getClass().getSimpleName());
                ((BaseModule)o).damage(diff);
            }
            ParticleActor hitFx = new ParticleActor(Gdx.files.internal("particles/buggie-impact-sparks.p"), this.world.gameScreen.atlas, true);
            hitFx.setPosition(this.getWorldX(), this.getWorldY());
            hitFx.pfx.start();
            this.world.gameScreen.topGroup.addActor(hitFx);
        }
        if (diff > 0.5f && endCrashSpeed > 4.0f && o instanceof Creature) {
            Audio.getInstance().playSound("sounds/buggie-crash2.ogg", diff * 0.05f, MathUtils.random(0.8f, 1.1f));
            MoonBase.debug("Hit a baddie");
            Gdx.app.postRunnable(new Runnable(){

                @Override
                public void run() {
                    ((Creature)o).takeDamage(10.0f * diff, Vehicle.this.damageDef);
                }
            });
        }
    }

    public void physicalHitAnim() {
    }

    protected void takeDamage(float damageAmount, boolean ignoreArmor) {
        this.physicalHitAnim();
        if (MoonBase.getCurrentMission().missionType != Mission.MissionTypes.creative) {
            this.world.gameScreen.hud.showLoosingHealthGlow(true);
            this.health = ignoreArmor ? (this.health -= damageAmount) : (this.health -= damageAmount * this.vd.armorMul);
            if (this.health < this.vd.damagedThreshold && this.currentDamageState == DamageStates.good) {
                this.currentDamageState = DamageStates.damaged;
                this.breakRandomWheel();
                this.setDamageSprite();
                this.damageSmoke.pfx.start();
            }
            if (this.health < this.vd.veryDamagedThreshold && this.currentDamageState != DamageStates.veryDamaged) {
                this.currentDamageState = DamageStates.veryDamaged;
                this.breakRandomWheel();
            }
            if (this.health <= 0.0f) {
                this.image.setColor(0.3f, 0.3f, 0.3f, 1.0f);
                this.readyToRemove = true;
                Gdx.app.log("MewnBase", "Buggie destroyed. RIP.");
            }
        }
    }

    public void setDamageSprite() {
    }

    public void setFixedSprite() {
    }

    private void vehicleShake(float delta) {
        this.shakeTimer += delta;
        if (this.shakeTimer > 10.0f) {
            this.shakeTimer = 0.0f;
        }
        float speedFactor = this.getSpeedKMH() / this.vd.maxSpeed;
        float freq = this.shakeTimer * 50.0f;
        this.image.setPosition(0.0f, MathUtils.cos(freq) * (0.34f * speedFactor + 0.5f), 1);
    }

    @Override
    public void remove() {
        this.world.b2dWorld.destroyBody(this.body);
        for (Wheel w : this.wheels) {
            w.destroy();
        }
        this.shadow.remove();
        this.group.addAction(Actions.sequence((Action)Actions.delay(1.0f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                Vehicle.this.dropItemsOnDeath();
            }
        }), (Action)Actions.fadeOut(3.0f), (Action)Actions.removeActor()));
        if (this.world.player != null) {
            this.world.player.toggleVehicle();
        }
        this.setState(STATES.empty);
    }

    public void dropItemsOnDeath() {
    }

    @Override
    public void dispose() {
        if (this.engineSound != null) {
            this.engineSound.stop();
        }
    }

    public void paintPuffFx() {
        Audio.getInstance().playSound("sounds/paintbrush-splat.ogg", 0.5f, 1.2f);
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/paintbrush-splat.p", ParticleEffect.class));
        ParticleActor paintPuff = new ParticleActor(p, true);
        paintPuff.setPosition(this.getXPos(), this.getYPos());
        this.world.gameScreen.topGroup.addActor(paintPuff);
        for (ParticleEmitter e : paintPuff.pfx.getEmitters()) {
            ParticleEmitter.GradientColorValue gradient = e.getTint();
            Color newColor = StorageColorOptions.colors[this.paintColorIndex];
            gradient.getColors()[0] = newColor.r;
            gradient.getColors()[1] = newColor.g;
            gradient.getColors()[2] = newColor.b;
        }
        paintPuff.pfx.start();
    }

    @Override
    public void takeDamage(float amount, DamageDef damageDef) {
        this.takeDamage(amount, false);
    }

    private static enum DamageStates {
        good,
        damaged,
        veryDamaged;

    }

    public static enum STATES {
        empty,
        inUse;

    }

    public static enum acceleration {
        none,
        acceleration,
        brake;

    }

    public static enum steering {
        none,
        left,
        right;

    }
}
