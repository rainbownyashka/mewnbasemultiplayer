/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.AdditiveImage;
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
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.entities.LightAnimator;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.entities.VehicleBattery;
import com.cairn4.moonbase.entities.Wheel;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.HudNotificationData;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import java.util.HashMap;

public class Buggie
extends Vehicle
implements VehicleBattery {
    public Image imageBase;
    public float charge;
    public float chargeDrainRate = 0.0f;
    public boolean recharging = false;
    public boolean prevRechargeCheck;
    public boolean prevEnginePowerAvailable;
    public boolean wheel0func = true;
    public boolean wheel1func = true;
    public boolean wheel2func = true;
    public boolean wheel3func = true;

    public Buggie(World world, Chunk c, int tileX, int tileY) {
        this(world, (float)(c.chunkX * 10 + tileX) * Tile.TILE_SIZE, (float)(c.chunkY * 10 + tileY) * Tile.TILE_SIZE, 0.0f);
    }

    public Buggie(World world, float worldX, float worldY, float rotation) {
        super(world, worldX, worldY, rotation, "buggie");
        this.charge = this.vd.maxBatteryCharge;
        this.setState(Vehicle.STATES.empty);
    }

    @Override
    public void doneLoading() {
        super.doneLoading();
        if (this.inventoryItemDataList.size > 0) {
            for (InventoryItemData iid : this.inventoryItemDataList) {
                for (int i = 0; i < iid.amount; ++i) {
                    Item newItem = ItemFactory.getInstance().createItem(iid.itemId);
                    new ItemPickup(this.world, this.getWorldX(), this.getWorldY(), newItem);
                }
            }
        }
        this.inventoryItemDataList.clear();
        Gdx.app.debug("MewnBase", "Buggie: has data items: " + this.inventoryItemDataList.size);
        if (!this.wheel0func) {
            ((Wheel)this.wheels.get(0)).damage();
        }
        if (!this.wheel1func) {
            ((Wheel)this.wheels.get(1)).damage();
        }
        if (!this.wheel2func) {
            ((Wheel)this.wheels.get(2)).damage();
        }
        if (!this.wheel3func) {
            ((Wheel)this.wheels.get(3)).damage();
        }
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
        properties.put("wheel0func", ((Wheel)this.wheels.get((int)0)).functioning);
        properties.put("wheel1func", ((Wheel)this.wheels.get((int)1)).functioning);
        properties.put("wheel2func", ((Wheel)this.wheels.get((int)2)).functioning);
        properties.put("wheel3func", ((Wheel)this.wheels.get((int)3)).functioning);
        properties.put("paintColorIndex", this.paintColorIndex);
        Gdx.app.debug("MewnBase", "Buggie inventoryItemDataList: " + this.inventoryItemDataList.size);
        return properties;
    }

    @Override
    public void createDrawable(String sprite) {
        super.createDrawable(sprite);
        this.imageBase = new Image(this.world.gameScreen.skin.getDrawable("buggie-base-default"));
        this.imageBase.setSize(128.0f, 128.0f);
        this.imageBase.setOrigin(1);
        this.imageBase.setPosition(0.0f, 0.0f, 1);
        this.chassisGroup.addActor(this.imageBase);
        this.image = new Image(this.world.gameScreen.skin.getDrawable(sprite));
        this.image.setSize(128.0f, 128.0f);
        this.image.setOrigin(1);
        this.image.setPosition(0.0f, 0.0f, 1);
        this.chassisGroup.addActor(this.image);
        this.image.setTouchable(Touchable.enabled);
        this.image.addListener(new ClickListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.debug("MewnBase", "Buggie image click touchdown");
                Buggie.this.setClicked();
                return false;
            }
        });
        this.headLightSprite = new AdditiveImage(this.world.gameScreen.skin.getDrawable("buggie-headlights"));
        this.headLightSprite.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.headLightSprite.setOrigin(1);
        this.headLightSprite.setPosition(0.0f, 0.0f, 1);
        this.headLightSprite.setTouchable(Touchable.disabled);
        this.chassisGroup.addActor(this.headLightSprite);
        this.tailLightSprite = new AdditiveImage(this.world.gameScreen.skin.getDrawable("buggie-taillight"));
        this.tailLightSprite.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.tailLightSprite.setOrigin(1);
        this.tailLightSprite.setPosition(0.0f, 0.0f, 1);
        this.tailLightSprite.setTouchable(Touchable.disabled);
        this.chassisGroup.addActor(this.tailLightSprite);
    }

    @Override
    public void physicalHitAnim() {
        this.chassisGroup.addAction(Actions.parallel((Action)Actions.sequence((Action)Actions.color(Color.GRAY), (Action)Actions.color(this.paintColor, 0.3f)), (Action)Actions.sequence(Actions.rotateTo(10.0f, 0.03f), Actions.rotateTo(-10.0f, 0.03f), Actions.rotateTo(-6.0f, 0.03f), Actions.rotateTo(6.0f, 0.03f), Actions.rotateTo(-3.0f, 0.03f), Actions.rotateTo(2.0f, 0.03f))));
    }

    @Override
    public void setSpecialAbility(boolean b) {
        super.setSpecialAbility(b);
        this.setPowerSlide(b);
    }

    @Override
    public void setDamageSprite() {
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable("buggie-damaged"));
    }

    @Override
    public void setFixedSprite() {
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable(this.vd.id));
    }

    @Override
    public void setState(Vehicle.STATES newState) {
        if (newState == Vehicle.STATES.inUse) {
            this.toggleDriverHead(true);
            this.toggleBuggiePower(this.charge > 0.0f);
        } else if (newState == Vehicle.STATES.empty) {
            this.setAcceleration(Vehicle.acceleration.none);
            this.toggleBuggiePower(false);
            this.toggleDriverHead(false);
        }
        super.setState(newState);
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
        PointLight fireLight = new PointLight(this.world.rayHandler, 24, Color.YELLOW, Tile.TILE_SIZE * 3.5f / 256.0f, this.getXPos() / 256.0f, this.getYPos() / 256.0f);
        fireLight.setXray(true);
        LightAnimator fireLightAnimator = new LightAnimator(this.world, this.getXPos(), this.getYPos());
        fireLightAnimator.light = fireLight;
        fireLightAnimator.animFade(0.0f, 3.0f);
        fireLightAnimator.animDistance(Tile.TILE_SIZE * 3.0f, Tile.TILE_SIZE, 3.0f);
        fireLightAnimator.removeWhenFinished = true;
        ParticleActor fireParticle = new ParticleActor(Gdx.files.internal("particles/base-fire.p"), this.world.gameScreen.atlas, true);
        fireParticle.pfx.start();
        fxGroup.addActor(fireParticle);
        this.world.gameScreen.cameraShake.shake(10.0f, 60.0f, 0.4f);
        super.remove();
    }

    @Override
    public void dropItemsOnDeath() {
        Item newItem;
        if (MathUtils.randomBoolean()) {
            newItem = ItemFactory.getInstance().createItem("scrap");
            new ItemPickup(this.world, this.getWorldX(), this.getWorldY(), newItem);
        }
        if (MathUtils.randomBoolean()) {
            newItem = ItemFactory.getInstance().createItem("component");
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
                this.chargeDrainRate = 1.0f;
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
    }

    @Override
    public void update(float delta) {
        this.recharging = false;
        super.update(delta);
        float pitch = this.getSpeedKMH() / this.vd.maxSpeed + 0.3f;
        this.engineSound.setPitch(this.engineSoundId, pitch);
        this.dustTrail.setPosition(this.group.getX(), this.group.getY());
        if (this.health < this.vd.damagedThreshold) {
            this.damageSmoke.setPosition(this.group.getX(), this.group.getY());
        }
        if (this.currentState == Vehicle.STATES.inUse) {
            if (this.charge > 0.0f) {
                this.enginePowerAvailable = true;
                if (!this.prevEnginePowerAvailable) {
                    this.toggleBuggiePower(true);
                }
                this.charge -= this.chargeDrainRate * delta;
            } else {
                this.enginePowerAvailable = false;
                if (this.prevEnginePowerAvailable) {
                    this.toggleBuggiePower(false);
                }
            }
        }
        this.prevEnginePowerAvailable = this.enginePowerAvailable;
        this.prevRechargeCheck = this.recharging;
    }

    @Override
    protected void repair(float delta) {
        super.repair(delta);
        this.recharge(delta);
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
    public boolean isRecharging() {
        return this.recharging && this.prevRechargeCheck;
    }

    @Override
    public float getChargePercent() {
        return this.charge / this.vd.maxBatteryCharge;
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
    public void setColor(int colorIndex) {
        if (colorIndex == 0) {
            this.imageBase.setDrawable(this.world.gameScreen.skin.getDrawable("buggie-base-default"));
        } else {
            this.imageBase.setDrawable(this.world.gameScreen.skin.getDrawable("buggie-base"));
        }
        Color c = Vars.vehicleColors[colorIndex];
        float tintStrength = 0.25f;
        float r = MathUtils.lerp(c.r, 1.0f, tintStrength);
        float g = MathUtils.lerp(c.g, 1.0f, tintStrength);
        float b = MathUtils.lerp(c.b, 1.0f, tintStrength);
        this.imageBase.setColor(new Color(r, g, b, 1.0f));
        this.paintColorIndex = colorIndex;
    }
}

