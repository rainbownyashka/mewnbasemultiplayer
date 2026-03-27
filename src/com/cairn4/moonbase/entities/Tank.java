/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemActions;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.BuggieTrunk;
import com.cairn4.moonbase.entities.Creature;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.entities.LightAnimator;
import com.cairn4.moonbase.entities.TankDrill;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.entities.VehicleBattery;
import com.cairn4.moonbase.entities.VehicleCabin;
import com.cairn4.moonbase.entities.Wheel;
import com.cairn4.moonbase.tiles.ItemDropper;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.BuggieTrunkUI;
import com.cairn4.moonbase.ui.HudNotificationData;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import java.util.ArrayList;
import java.util.HashMap;

public class Tank
extends Vehicle
implements VehicleBattery,
VehicleCabin {
    public Image imageBase;
    public Image floorImage;
    public Image treadL;
    public Image treadR;
    private int treadImageLeft = 1;
    private int treadImageRight = 1;
    public Group topGroup;
    private Image teeth1;
    private Image teeth2;
    public float charge;
    public float chargeDrainRate = 0.0f;
    public boolean recharging = false;
    public boolean prevRechargeCheck;
    public boolean prevEnginePowerAvailable;
    public boolean drillOn;
    private Action teeth1Action;
    private Action teeth2Action;
    private ParticleActor drillSparks;
    private ParticleActor drillDust;
    private TankDrill tankDrill;
    private boolean overheated = false;
    private float drillHeat;
    private static final float drillHeatLimit = 5.0f;
    private static final float drillheatDecay = 1.0f;
    private static final float drillheatGain = 1.0f;
    private float drillOverheatCooldown;
    private float drillOverheatCooldownLength = 10.0f;
    public ArrayList<ItemDropper> tilesDrilling = new ArrayList();
    public ArrayList<Creature> creaturesDrilling = new ArrayList();
    private float drillCreatureTimer = 0.0f;
    private float drillCreatureDelay = 0.35f;
    Sound grindSound;
    protected long grindSoundId;
    Image drillTarget;
    Vector2 creaturePosition;
    float minDelay = 0.02f;
    float maxDelay = 0.2f;
    float animTimerLeft;
    float animTimerRight;
    Vector2 wheelForceLeft;
    Vector2 wheelForceRight;
    private boolean hasOxygen = false;

    public Tank(World world, Chunk c, int tileX, int tileY) {
        this(world, (float)(c.chunkX * 10 + tileX) * Tile.TILE_SIZE, (float)(c.chunkY * 10 + tileY) * Tile.TILE_SIZE, 0.0f);
    }

    public Tank(World world, float worldX, float worldY, float rotation) {
        super(world, worldX, worldY, rotation, "tank");
        this.trunk = new BuggieTrunk(this, 9);
        this.grindSound = AssetManagerSingleton.getInstance().get("sounds/harvester-grind.ogg", Sound.class);
        this.addRoof();
        this.charge = this.vd.maxBatteryCharge;
        this.setState(Vehicle.STATES.empty);
        this.drillOn = false;
        this.drillTarget = new Image(world.gameScreen.skin.getDrawable("waypoint-node"));
        this.drillTarget.setOrigin(1);
        this.drillTarget.setVisible(false);
        world.gameScreen.stage.addActor(this.drillTarget);
        this.setColor(0);
    }

    @Override
    public void setupPhysics(String loaderObject) {
        super.setupPhysics(loaderObject);
        int sensorCatBits = 2;
        int sensorMaskBits = 10;
        FixtureDef fd = new FixtureDef();
        fd.filter.categoryBits = this.categoryBits;
        fd.filter.maskBits = (short)(this.maskBits + this.categoryBits);
        fd.isSensor = true;
        this.world.bodyEditorLoader.attachFixture(this.body, "tank-sensor", fd, 0.5f);
        Fixture f = this.body.getFixtureList().get(this.body.getFixtureList().size - 1);
        this.tankDrill = new TankDrill(this);
        f.setUserData(this.tankDrill);
        Filter drillFilter = new Filter();
        drillFilter.categoryBits = (short)sensorCatBits;
        drillFilter.maskBits = (short)sensorMaskBits;
        f.setFilterData(drillFilter);
        this.body.setUserData(this);
    }

    @Override
    public void doneLoading() {
        super.doneLoading();
        for (InventoryItemData iid : this.inventoryItemDataList) {
            ItemStack newStack = new ItemStack(ItemFactory.getInstance().createItem(iid.itemId), iid.amount);
            int durabilityForThisType = ItemFactory.getDurability(iid.itemId);
            if (durabilityForThisType > 0 && iid.durability == 0) {
                iid.durability = durabilityForThisType;
            }
            newStack.item.durability = iid.durability;
            this.trunkItems.add(newStack);
        }
        MoonBase.log("Tank: has data items: " + this.inventoryItemDataList.size);
        if (this.health < this.vd.damagedThreshold) {
            this.setDamageSprite();
        }
    }

    @Override
    public HashMap<String, Object> getProperties() {
        this.inventoryItemDataList.clear();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("health", Float.valueOf(this.health));
        properties.put("id", this.id);
        properties.put("charge", Float.valueOf(this.charge));
        properties.put("paintColorIndex", this.paintColorIndex);
        for (ItemStack s : this.trunkItems) {
            InventoryItemData iid = new InventoryItemData();
            iid.itemId = s.getId();
            iid.amount = s.getAmount();
            iid.durability = s.item.durability;
            this.inventoryItemDataList.add(iid);
        }
        properties.put("inventoryItemDataList", this.inventoryItemDataList);
        Gdx.app.debug("MewnBase", "Tank inventoryItemDataList: " + this.inventoryItemDataList.size);
        return properties;
    }

    @Override
    public void createDrawable(String sprite) {
        super.createDrawable(sprite);
        this.group.setDebug(true, true);
        this.treadL = new Image(this.world.gameScreen.skin.getDrawable("tank/tank-treads1"));
        this.treadL.setSize(135.5f, 47.5f);
        this.treadL.setPosition(0.0f, 50.0f, 1);
        this.group.addActor(this.treadL);
        this.treadL.toBack();
        this.treadR = new Image(this.world.gameScreen.skin.getDrawable("tank/tank-treads1"));
        this.treadR.setSize(135.5f, 47.5f);
        this.treadR.setPosition(0.0f, -50.0f, 1);
        this.group.addActor(this.treadR);
        this.treadR.toBack();
        this.floorImage = new Image(this.world.gameScreen.skin.getDrawable("tank/tank-bottom"));
        this.floorImage.setSize(87.5f, 110.5f);
        this.floorImage.setPosition(36.0f, 0.0f, 1);
        this.chassisGroup.addActor(this.floorImage);
        this.teeth1 = new Image(this.world.gameScreen.skin.getDrawable("tank/tank-teeth1"));
        this.teeth1.setSize(26.0f, 63.5f);
        this.teeth1.setPosition(76.0f, 0.0f, 1);
        this.chassisGroup.addActor(this.teeth1);
        this.teeth2 = new Image(this.world.gameScreen.skin.getDrawable("tank/tank-teeth2"));
        this.teeth2.setSize(26.0f, 89.0f);
        this.teeth2.setPosition(76.0f, 0.0f, 1);
        this.chassisGroup.addActor(this.teeth2);
    }

    public void addRoof() {
        ParticleEffect drillSparksPE = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/tank-drill-sparks.p", ParticleEffect.class));
        this.drillSparks = new ParticleActor(drillSparksPE, false);
        this.drillSparks.setPosition(85.0f, 0.0f);
        this.drillSparks.pfx.allowCompletion();
        this.chassisGroup.addActor(this.drillSparks);
        this.topGroup = new Group();
        this.chassisGroup.addActor(this.topGroup);
        this.imageBase = new Image(this.world.gameScreen.skin.getDrawable("tank/tank-base"));
        this.imageBase.setSize(167.5f, 128.0f);
        this.imageBase.setOrigin(1);
        this.imageBase.setPosition(0.0f, 0.0f, 1);
        this.topGroup.addActor(this.imageBase);
        this.image = new Image(this.world.gameScreen.skin.getDrawable("tank/tank"));
        this.image.setSize(167.5f, 128.0f);
        this.image.setOrigin(1);
        this.image.setPosition(0.0f, 0.0f, 1);
        this.topGroup.addActor(this.image);
        this.image.setTouchable(Touchable.enabled);
        this.image.addListener(new ClickListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.debug("MewnBase", "Tank image click touchdown");
                Tank.this.setClicked();
                return false;
            }
        });
        this.headLightSprite = new AdditiveImage(this.world.gameScreen.skin.getDrawable("tank/tank-headlights"));
        this.headLightSprite.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.headLightSprite.setOrigin(1);
        this.headLightSprite.setPosition(0.0f, 0.0f, 1);
        this.headLightSprite.setTouchable(Touchable.disabled);
        this.topGroup.addActor(this.headLightSprite);
        this.tailLightSprite = new AdditiveImage(this.world.gameScreen.skin.getDrawable("buggie-taillight"));
        this.tailLightSprite.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.tailLightSprite.setOrigin(1);
        this.tailLightSprite.setPosition(0.0f, 0.0f, 1);
        this.tailLightSprite.setTouchable(Touchable.disabled);
        this.topGroup.addActor(this.tailLightSprite);
        ParticleEffect drillDustPE = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/tank-drill-dust.p", ParticleEffect.class));
        this.drillDust = new ParticleActor(drillDustPE, false);
        this.drillDust.setPosition(85.0f, 0.0f);
        this.drillDust.pfx.allowCompletion();
        this.group.addActor(this.drillDust);
        this.repairParticles.toFront();
    }

    @Override
    public void setState(Vehicle.STATES newState) {
        if (newState == Vehicle.STATES.inUse) {
            this.enterCabinSoundFx();
            this.toggleDriverHead(true);
            this.toggleBuggiePower(this.charge > 0.0f);
        } else if (newState == Vehicle.STATES.empty) {
            this.setAcceleration(Vehicle.acceleration.none);
            this.setSteering(Vehicle.steering.none);
            this.toggleBuggiePower(false);
            this.toggleDriverHead(false);
        }
        super.setState(newState);
    }

    @Override
    public void setSpecialAbility(boolean b) {
        super.setSpecialAbility(b);
        this.setDrillOn(b);
    }

    private void setDrillOn(boolean b) {
        if (b && !this.overheated) {
            if (!this.drillOn) {
                MoonBase.log("start drill anim");
                this.setupTeethAnim();
                this.drillDust.pfx.start();
                this.grindSoundId = Audio.getInstance().playSoundLoop(this.grindSound, 0.3f, 0.7f, 0.0f);
            }
        } else {
            this.teeth1.clearActions();
            this.teeth2.clearActions();
            this.drillSparks.pfx.allowCompletion();
            this.drillDust.pfx.allowCompletion();
            this.grindSound.stop();
        }
        this.drillOn = b;
        if (this.tilesDrilling.size() <= 0) {
            this.drillSparks.pfx.allowCompletion();
        } else if (this.drillOn && this.drillSparks.pfx.isComplete()) {
            this.drillSparks.pfx.start();
        }
    }

    private void enterCabinSoundFx() {
        float v = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getWorldX(), this.getWorldY()), 300.0f, 0.4f);
        Audio.getInstance().playSound("sounds/textAnimator2.ogg", v, 1.5f);
    }

    public void toggleBuggiePower(boolean on) {
        if (on) {
            this.engineSoundId = Audio.getInstance().playSoundLoop(this.engineSound, 0.35f, 1.0f, 0.0f);
            this.toggleAllLights(true);
            this.dustTrail.pfx.start();
        } else {
            this.engineSound.stop();
            this.toggleAllLights(false);
            this.dustTrail.pfx.allowCompletion();
        }
    }

    @Override
    public void remove() {
        this.damageSmoke.pfx.allowCompletion();
        Group fxGroup = new Group();
        this.world.gameScreen.topGroup.addActor(fxGroup);
        fxGroup.setPosition(this.getXPos(), this.getYPos());
        fxGroup.addAction(Actions.sequence((Action)Actions.delay(3.0f), (Action)Actions.removeActor()));
        ParticleActor particleExplosion = new ParticleActor(Gdx.files.internal("particles/meteor-explosion.p"), this.world.gameScreen.atlas, true);
        particleExplosion.pfx.start();
        fxGroup.addActor(particleExplosion);
        ParticleActor fireParticle = new ParticleActor(Gdx.files.internal("particles/base-fire.p"), this.world.gameScreen.atlas, true);
        fireParticle.pfx.start();
        fxGroup.addActor(fireParticle);
        this.deathExplosionLight();
        this.world.gameScreen.cameraShake.shake(10.0f, 60.0f, 0.4f);
        super.remove();
    }

    private void deathExplosionLight() {
        PointLight fireLight = new PointLight(this.world.rayHandler, 24, Color.YELLOW, Tile.TILE_SIZE * 3.5f / 256.0f, this.getXPos() / 256.0f, this.getYPos() / 256.0f);
        fireLight.setXray(true);
        LightAnimator fireLightAnimator = new LightAnimator(this.world, this.getXPos(), this.getYPos());
        fireLightAnimator.light = fireLight;
        fireLightAnimator.animFade(0.0f, 3.0f);
        fireLightAnimator.animDistance(Tile.TILE_SIZE * 3.0f, Tile.TILE_SIZE, 3.0f);
        fireLightAnimator.removeWhenFinished = true;
    }

    @Override
    public void dropItemsOnDeath() {
        Item newItem;
        if (MathUtils.randomBoolean()) {
            newItem = ItemFactory.getInstance().createItem("engine");
            new ItemPickup(this.world, this.getWorldX(), this.getWorldY(), newItem);
        }
        if (MathUtils.randomBoolean()) {
            newItem = ItemFactory.getInstance().createItem("scrap");
            new ItemPickup(this.world, this.getWorldX(), this.getWorldY(), newItem);
        }
        if (MathUtils.randomBoolean()) {
            newItem = ItemFactory.getInstance().createItem("metal");
            new ItemPickup(this.world, this.getWorldX(), this.getWorldY(), newItem);
        }
    }

    @Override
    public void setSteering(Vehicle.steering s) {
        Vehicle.steering oldSteering = this.currentSteering;
        super.setSteering(s);
        if (oldSteering != this.currentSteering) {
            float angle = 0.0f;
            switch (this.currentSteering) {
                case left: {
                    angle = 5.0f;
                    break;
                }
                case right: {
                    angle = -5.0f;
                    break;
                }
            }
            this.drivingHead.addAction(Actions.sequence((Action)Actions.rotateTo(angle, 0.15f, Interpolation.sineIn)));
        }
    }

    @Override
    public void setAcceleration(Vehicle.acceleration a) {
        super.setAcceleration(a);
        switch (a) {
            case acceleration: {
                this.chargeDrainRate = 0.5f;
                break;
            }
            case brake: {
                if (this.getLocalVelocity().x > 0.0f) {
                    float c = this.getSpeedFraction();
                    this.chargeDrainRate = -c * 0.5f;
                    break;
                }
                this.chargeDrainRate = 1.0f;
                break;
            }
            case none: {
                float c = this.getSpeedFraction();
                this.chargeDrainRate = -c * 0.1f;
            }
        }
        if (this.drillOn) {
            this.chargeDrainRate += 0.5f;
        }
        this.chargeDrainRate = this.currentState == Vehicle.STATES.inUse ? (this.chargeDrainRate += 1.0f) : 0.0f;
    }

    @Override
    public void update(float delta) {
        if (this.creaturePosition == null) {
            this.creaturePosition = new Vector2(0.0f, 0.0f);
        }
        this.recharging = false;
        super.update(delta);
        this.updateTreads(this.getSpeedKMH() / this.vd.maxSpeed, delta);
        Vector2 drillPoint = new Vector2(75.0f, 0.0f);
        Vector2 worldDrillPoint = this.group.localToStageCoordinates(drillPoint);
        this.drillTarget.setPosition(worldDrillPoint.x, worldDrillPoint.y, 1);
        double xd = Math.floor(worldDrillPoint.x / Tile.TILE_SIZE);
        double yd = Math.floor(worldDrillPoint.y / Tile.TILE_SIZE);
        int xx = Math.round((float)xd);
        int yy = Math.round((float)yd);
        for (int i = this.tilesDrilling.size() - 1; i >= 0; --i) {
            if (this.tilesDrilling.get((int)i).readyToRemove) {
                this.tilesDrilling.remove(i);
                continue;
            }
            if (this.tilesDrilling.get((int)i).worldX == xx && this.tilesDrilling.get((int)i).worldY == yy || this.tilesDrilling.get(i).hasPhysicsBody()) continue;
            MoonBase.log("Removing tile from tilesDrilling");
            this.tilesDrilling.get(i).stopHarvesting();
            this.tilesDrilling.remove(i);
        }
        this.drillCreatureTimer += delta;
        if (this.drillOn && this.drillCreatureTimer > this.drillCreatureDelay) {
            this.drillCreatureTimer = 0.0f;
            for (int c = this.world.entityList.size() - 1; c >= 0; --c) {
                if (!(this.world.entityList.get(c) instanceof Creature)) continue;
                Creature creature = (Creature)this.world.entityList.get(c);
                if (creature.creatureData == null) continue;
                if (creature.readyToRemove) {
                    this.creaturesDrilling.remove(creature);
                    continue;
                }
                this.creaturePosition.set(creature.getXPos(), creature.getYPos());
                float dist = drillPoint.dst(this.creaturePosition) - creature.creatureData.physicsRadius;
                if (!(dist < 35.0f)) continue;
                creature.takeDamage(5.0f, this.damageDef);
            }
        }
        float pitch = this.getSpeedKMH() / this.vd.maxSpeed + 0.3f;
        this.engineSound.setPitch(this.engineSoundId, pitch);
        this.dustTrail.setPosition(this.tailLight.getPosition().x * 256.0f, this.tailLight.getPosition().y * 256.0f);
        if (this.health < this.vd.damagedThreshold) {
            this.damageSmoke.setPosition(this.group.getX(), this.group.getY());
        }
        if (this.currentState == Vehicle.STATES.inUse) {
            if (this.charge > 0.0f) {
                ItemDropper dropper;
                this.enginePowerAvailable = true;
                if (!this.prevEnginePowerAvailable) {
                    this.toggleBuggiePower(true);
                }
                this.charge -= this.chargeDrainRate * delta;
                this.teeth1.setSize(26.0f, 63.5f);
                this.teeth1.setPosition(76.0f, 0.0f, 1);
                Rectangle r = new Rectangle(49.0f, -65.0f, 52.0f, 130.0f);
                this.drillTarget.setColor(Color.WHITE);
                Tile drillTile = this.world.getTile(xx, yy);
                boolean canDrillhere = false;
                if (drillTile != null && drillTile instanceof ItemDropper && (dropper = (ItemDropper)drillTile).canHarvest()) {
                    canDrillhere = true;
                    this.drillTarget.setColor(Color.RED);
                    if (!this.tankDrill.tank.tilesDrilling.contains(dropper)) {
                        this.tankDrill.tank.tilesDrilling.add(dropper);
                    }
                }
                if (!canDrillhere) {
                    for (ItemDropper dropper2 : this.tilesDrilling) {
                        if (!dropper2.harvestableBehavior.harvesting) continue;
                        dropper2.stopHarvesting();
                    }
                }
                if (this.drillOn) {
                    for (ItemDropper dropper2 : this.tilesDrilling) {
                        if (!dropper2.harvestableBehavior.harvesting) {
                            dropper2.startHarvest();
                        }
                        dropper2.harvestableBehavior.harvest(delta);
                    }
                } else {
                    for (ItemDropper dropper2 : this.tilesDrilling) {
                        if (!dropper2.harvestableBehavior.harvesting) continue;
                        dropper2.stopHarvesting();
                    }
                }
                this.setOxygen(true);
            } else {
                this.enginePowerAvailable = false;
                if (this.prevEnginePowerAvailable) {
                    this.toggleBuggiePower(false);
                }
                this.setOxygen(false);
            }
        }
        this.prevEnginePowerAvailable = this.enginePowerAvailable;
        this.prevRechargeCheck = this.recharging;
    }

    private void updateOverheat(float delta) {
        if (this.drillOn) {
            if (this.overheated) {
                this.drillOverheatCooldown -= delta;
                if (this.drillOverheatCooldown <= 0.0f) {
                    this.overheated = false;
                    this.drillHeat = 0.0f;
                }
            } else {
                this.drillHeat += 1.0f * delta;
                if (this.drillHeat > 5.0f) {
                    this.overheated = true;
                    this.drillOverheatCooldown = this.drillOverheatCooldownLength;
                    this.takeDamage(100.0f, true);
                }
            }
        } else if (!this.overheated) {
            this.drillHeat -= 1.0f * delta;
            if (this.drillHeat < 0.0f) {
                this.drillHeat = 0.0f;
            }
        } else {
            this.drillOverheatCooldown -= delta;
            if (this.drillOverheatCooldown <= 0.0f) {
                this.overheated = false;
                this.drillHeat = 0.0f;
            }
        }
        System.out.println("overheat: " + this.drillHeat + " / " + 5.0f + " -- cooldown: " + this.drillOverheatCooldown);
    }

    @Override
    protected void repair(float delta) {
        super.repair(delta);
        this.recharge(delta);
    }

    public void updateTreads(float speedFactor, float delta) {
        this.animTimerLeft += delta;
        this.animTimerRight += delta;
        float leftSpeed = ((Wheel)this.wheels.get(0)).getLocalVelocity().len() / 1000.0f * 3600.0f;
        float rightSpeed = ((Wheel)this.wheels.get(1)).getLocalVelocity().len() / 1000.0f * 3600.0f;
        if (Math.abs(leftSpeed) > 0.1f && this.animTimerLeft >= this.maxDelay - leftSpeed / this.vd.maxSpeed * (this.maxDelay - this.minDelay) + this.minDelay) {
            this.updateTreadFrameLeft();
            this.animTimerLeft = 0.0f;
        }
        if (Math.abs(rightSpeed) > 0.1f && this.animTimerRight >= this.maxDelay - rightSpeed / this.vd.maxSpeed * (this.maxDelay - this.minDelay) + this.minDelay) {
            this.updateTreadFrameRight();
            this.animTimerRight = 0.0f;
        }
    }

    @Override
    public void updateWheels(float speedFactor, float delta) {
    }

    public void updateTreadFrameLeft() {
        this.treadImageLeft += ((Wheel)this.wheels.get((int)0)).getLocalVelocity().x > 0.0f ? 1 : -1;
        if (this.treadImageLeft < 1) {
            this.treadImageLeft = 3;
        }
        if (this.treadImageLeft > 3) {
            this.treadImageLeft = 1;
        }
        this.treadL.setDrawable(this.world.gameScreen.skin.getDrawable("tank/tank-treads" + this.treadImageLeft));
    }

    public void updateTreadFrameRight() {
        this.treadImageRight += ((Wheel)this.wheels.get((int)1)).getLocalVelocity().x > 0.0f ? 1 : -1;
        if (this.treadImageRight < 1) {
            this.treadImageRight = 3;
        }
        if (this.treadImageRight > 3) {
            this.treadImageRight = 1;
        }
        this.treadR.setDrawable(this.world.gameScreen.skin.getDrawable("tank/tank-treads" + this.treadImageRight));
    }

    private void setupTeethAnim() {
        this.teeth1.setPosition(76.0f, 0.0f, 1);
        Interpolation i = Interpolation.sine;
        Interpolation iIn = Interpolation.sineIn;
        Interpolation iOut = Interpolation.sineOut;
        float startingX = 76.0f;
        float speed = 0.2f;
        this.teeth1.addAction(Actions.forever(Actions.sequence((Action)Actions.parallel((Action)Actions.moveToAligned(startingX + 3.0f, 0.0f, 1, speed, i), (Action)Actions.sequence((Action)Actions.color(Color.WHITE, speed / 2.0f, iIn), (Action)Actions.color(Color.LIGHT_GRAY, speed / 2.0f, iOut))), (Action)Actions.parallel((Action)Actions.moveToAligned(startingX - 3.0f, 0.0f, 1, speed, i), (Action)Actions.sequence((Action)Actions.color(Color.GRAY, speed / 2.0f, iIn), (Action)Actions.color(Color.LIGHT_GRAY, speed / 2.0f, iOut))))));
        this.teeth2.addAction(Actions.forever(Actions.sequence((Action)Actions.parallel((Action)Actions.moveToAligned(startingX - 2.0f, 0.0f, 1, speed, i), (Action)Actions.sequence((Action)Actions.color(Color.GRAY, speed / 2.0f, iIn), (Action)Actions.color(Color.LIGHT_GRAY, speed / 2.0f, iOut))), (Action)Actions.parallel((Action)Actions.moveToAligned(startingX + 2.0f, 0.0f, 1, speed, i), (Action)Actions.sequence((Action)Actions.color(Color.WHITE, speed / 2.0f, iIn), (Action)Actions.color(Color.LIGHT_GRAY, speed / 2.0f, iOut))))));
    }

    @Override
    public void playerActionSecondary(Player player) {
        Vector2 worldPos = new Vector2(player.getXPos(), player.getYPos());
        float distance = worldPos.dst(this.getXPos(), this.getYPos());
        if (distance <= interactDistance) {
            Gdx.app.debug("MewnBase", "Tank right clicked : " + this.clicked);
            if (this.trunkUI == null) {
                this.trunkUI = new BuggieTrunkUI(this.world.gameScreen, this.trunk, player.playerInventory);
                this.world.gameScreen.showMenu(this.trunkUI);
                this.trunkUI.storageLabel.setText(Localization.get("item_tank"));
                this.trunkUI = null;
            }
        }
    }

    @Override
    public void recharge(float delta) {
        if (this.charge < this.vd.maxBatteryCharge) {
            this.charge += delta * this.vd.rechargeRate;
            this.recharging = true;
        }
        if (this.charge > this.vd.maxBatteryCharge) {
            this.charge = this.vd.maxBatteryCharge;
            this.recharging = false;
            Gdx.app.debug("MewnBase", "Buggie: Finished recharging!");
        }
        if (this.recharging && !this.prevRechargeCheck) {
            this.repairParticles.pfx.start();
        } else if (!this.recharging && this.prevRechargeCheck) {
            this.repairParticles.pfx.allowCompletion();
        }
    }

    @Override
    public boolean recharge(ItemStack itemStack, Player player) {
        float batPercent = this.charge / this.vd.maxBatteryCharge;
        if (batPercent < 0.95f) {
            ItemActions rechargeAction = null;
            for (ItemActions ia : itemStack.item.actions) {
                if (!ia.type.equals("rechargeVehicle")) continue;
                rechargeAction = ia;
                try {
                    this.instantRecharge(Float.valueOf(rechargeAction.value).floatValue());
                    HudNotificationData msg = new HudNotificationData();
                    msg.icon = "no-power";
                    msg.message = Localization.format("vehicle_battery", Vars.wholeNum.format(this.charge / this.vd.maxBatteryCharge * 100.0f));
                    MessageManager.getInstance().dispatchMessage(3, msg);
                    ParticleActor hitFx = new ParticleActor(Gdx.files.internal("particles/recharge-instant.p"), this.world.gameScreen.atlas, true);
                    hitFx.setPosition(this.getWorldX(), this.getWorldY());
                    hitFx.pfx.start();
                    this.world.gameScreen.topGroup.addActor(hitFx);
                    return true;
                }
                catch (Exception e) {
                    MoonBase.error("Non integer repair action value in items.json");
                }
            }
        } else {
            HudNotificationData msg = new HudNotificationData();
            msg.message = Localization.get("vehicle_alreadyCharged");
            MessageManager.getInstance().dispatchMessage(3, msg);
        }
        return false;
    }

    @Override
    public void instantRecharge(float percent) {
        this.charge += this.vd.maxBatteryCharge * percent;
        if (this.charge > this.vd.maxBatteryCharge) {
            this.charge = this.vd.maxBatteryCharge;
        }
    }

    @Override
    public boolean isRecharging() {
        return this.recharging && this.prevRechargeCheck;
    }

    @Override
    public float getChargePercent() {
        return this.charge / this.vd.maxBatteryCharge;
    }

    @Override
    public void setColor(int colorIndex) {
        if (colorIndex == 0) {
            this.imageBase.setDrawable(this.world.gameScreen.skin.getDrawable("tank/tank-base-default"));
        } else {
            this.imageBase.setDrawable(this.world.gameScreen.skin.getDrawable("tank/tank-base"));
        }
        Color c = Vars.vehicleColors[colorIndex];
        float tintStrength = 0.25f;
        float r = MathUtils.lerp(c.r, 1.0f, tintStrength);
        float g = MathUtils.lerp(c.g, 1.0f, tintStrength);
        float b = MathUtils.lerp(c.b, 1.0f, tintStrength);
        this.imageBase.setColor(new Color(r, g, b, 1.0f));
        this.paintColorIndex = colorIndex;
    }

    @Override
    protected void updatePhysics(float delta) {
        if (this.wheelForceLeft == null) {
            this.wheelForceLeft = new Vector2(0.0f, 0.0f);
        }
        if (this.wheelForceRight == null) {
            this.wheelForceRight = new Vector2(0.0f, 0.0f);
        }
        for (Wheel w : this.wheels) {
            w.killSidewaysVelocity(1.0f);
        }
        this.updateDampening();
        Vector2 base_vect = new Vector2(0.0f, 0.0f);
        if (this.currentAccelration == Vehicle.acceleration.acceleration && this.getSpeedKMH() < this.vd.maxSpeed) {
            base_vect.set(1.0f, 0.0f);
            this.setTailLightAlpha(0.5f);
        } else if (this.currentAccelration == Vehicle.acceleration.brake) {
            this.setTailLightAlpha(1.0f);
            if (this.getLocalVelocity().x > 0.0f) {
                base_vect.set(-0.7f, 0.0f);
            } else if (this.getSpeedKMH() < this.vd.maxSpeed * 0.4f) {
                base_vect.set(-1.0f, 0.0f);
            }
        } else {
            this.setTailLightAlpha(0.5f);
            base_vect.set(0.0f, 0.0f);
        }
        if (this.enginePowerAvailable) {
            if (this.currentAccelration == Vehicle.acceleration.none) {
                float spinPower = 0.5f;
                if (this.currentSteering == Vehicle.steering.left) {
                    this.wheelForceLeft.set(spinPower * this.vd.wheelPower, 0.0f);
                    this.wheelForceRight.set(-spinPower * this.vd.wheelPower, 0.0f);
                }
                if (this.currentSteering == Vehicle.steering.right) {
                    this.wheelForceLeft.set(-spinPower * this.vd.wheelPower, 0.0f);
                    this.wheelForceRight.set(spinPower * this.vd.wheelPower, 0.0f);
                }
                if (this.currentSteering == Vehicle.steering.none) {
                    this.wheelForceLeft.set(0.0f, 0.0f);
                    this.wheelForceRight.set(0.0f, 0.0f);
                }
            } else if (this.currentAccelration == Vehicle.acceleration.acceleration || this.currentAccelration == Vehicle.acceleration.brake) {
                if (this.currentSteering == Vehicle.steering.left) {
                    this.wheelForceLeft.set(base_vect.x * this.vd.wheelPower * 1.2f, 0.0f);
                    this.wheelForceRight.set(base_vect.x * this.vd.wheelPower * -0.2f, 0.0f);
                }
                if (this.currentSteering == Vehicle.steering.right) {
                    this.wheelForceLeft.set(base_vect.x * this.vd.wheelPower * -0.2f, 0.0f);
                    this.wheelForceRight.set(base_vect.x * this.vd.wheelPower * 1.2f, 0.0f);
                }
                if (this.currentSteering == Vehicle.steering.none) {
                    this.wheelForceLeft.set(base_vect.x * this.vd.wheelPower, 0.0f);
                    this.wheelForceRight.set(base_vect.x * this.vd.wheelPower, 0.0f);
                }
            }
        } else {
            this.wheelForceLeft.set(0.0f, 0.0f);
            this.wheelForceRight.set(0.0f, 0.0f);
        }
        for (int u = 0; u < this.wheels.size(); ++u) {
            Wheel w = (Wheel)this.wheels.get(u);
            if (!w.isPowered || !w.functioning) continue;
            if (u % 2 != 0) {
                w.wheel.applyForce(w.wheel.getWorldVector(this.wheelForceLeft), w.wheel.getWorldCenter(), true);
                continue;
            }
            w.wheel.applyForce(w.wheel.getWorldVector(this.wheelForceRight), w.wheel.getWorldCenter(), true);
        }
    }

    @Override
    public void updateDampening() {
        if (this.currentSteering == Vehicle.steering.none) {
            this.body.setAngularDamping(4.0f);
        } else {
            this.body.setAngularDamping(1.0f);
        }
        if (this.currentAccelration == Vehicle.acceleration.acceleration) {
            this.body.setLinearDamping(0.5f);
        } else {
            this.body.setLinearDamping(2.0f);
        }
    }

    @Override
    public void drawDebug() {
    }

    @Override
    public void setOxygen(boolean oxygen) {
        this.hasOxygen = oxygen;
    }

    @Override
    public boolean hasOxygen() {
        return this.hasOxygen;
    }
}

