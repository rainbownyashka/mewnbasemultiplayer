/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.BaseResourceStorageBehavior;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.CooldownTimer;
import com.cairn4.moonbase.tiles.behaviors.DropItemOnBaseBehavior;
import com.cairn4.moonbase.tiles.behaviors.ResourceMeterBehavior;
import com.cairn4.moonbase.ui.BasePowerPopup;

public class Generator
extends BaseModule {
    private CooldownTimer fueledCooldown;
    private DropItemOnBaseBehavior dropItemOnBaseBehavior;
    private Image onImage;
    private static float woodFuelValue = 200.0f;
    private static float plantFuelValue = 100.0f;
    private static float rootFuelValue = 100.0f;
    private static float fruitFuelValue = 50.0f;
    private static float seedFuelValue = 30.0f;
    private static float maxFuelCapacity = 200.0f;
    ParticleActor smoke;
    private ResourceMeterBehavior resourceMeterBehavior;
    private Image fuelMeterBg;
    private Image fuelMeter;
    private Image fuelMeterTop;
    public BaseResourceStorageBehavior fuelStorageBehavior;
    Sound sound;
    long soundId;
    float volume;
    float pan;

    public Generator(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.hasPower = false;
        this.maxPowerGen = 10.01f;
        this.powerGenRate = 0.0f;
        this.powerDrawRate = 0.0f;
        this.light.setActive(false);
        this.dropItemOnBaseBehavior = new DropItemOnBaseBehavior();
        this.dropItemOnBaseBehavior.baseModule = this;
        this.dropItemOnBaseBehavior.acceptedItemIds.add("wood");
        this.dropItemOnBaseBehavior.acceptedItemIds.add("plant");
        this.dropItemOnBaseBehavior.acceptedItemIds.add("root");
        this.dropItemOnBaseBehavior.acceptedItemIds.add("fruit");
        this.behaviorList.add(this.dropItemOnBaseBehavior);
        this.fueledCooldown = new CooldownTimer();
        this.behaviorList.add(this.fueledCooldown);
        this.updateBases();
        this.sound = Gdx.audio.newSound(Gdx.files.internal("sounds/engine-loop.ogg"));
        this.soundId = Audio.getInstance().playSoundLoop(this.sound, 0.0f, 0.5f, 0.0f);
        this.setupPhysics("smelter");
        this.addSmoke();
        this.fuelMeterBg = new Image(world.gameScreen.skin.getDrawable("white"));
        this.fuelMeterBg.setSize(18.0f, 36.5f);
        this.fuelMeterBg.setColor(0.08627451f, 0.08627451f, 0.08627451f, 1.0f);
        this.fuelMeterBg.setPosition(92.5f, 19.0f);
        this.componentGroup.addActor(this.fuelMeterBg);
        this.fuelMeter = new Image(world.gameScreen.skin.getDrawable("white"));
        this.fuelMeter.setSize(18.0f, 36.5f);
        this.fuelMeter.setColor(0.0f, 0.73333335f, 0.21176471f, 1.0f);
        this.fuelMeter.setPosition(92.5f, 19.0f);
        this.componentGroup.addActor(this.fuelMeter);
        this.componentGroup.toBack();
        this.fuelStorageBehavior = new BaseResourceStorageBehavior();
        this.fuelStorageBehavior.setId("fuelStorage");
        this.fuelStorageBehavior.type = BaseResources.biofuel;
        this.fuelStorageBehavior.amount = 0.0f;
        this.fuelStorageBehavior.capacity = maxFuelCapacity;
        this.behaviorList.add(this.fuelStorageBehavior);
        this.resourceMeterBehavior = new ResourceMeterBehavior();
        this.resourceMeterBehavior.meterSprite = this.fuelMeter;
        this.resourceMeterBehavior.baseModule = this;
        this.resourceMeterBehavior.resourceStorageBehavior = this.fuelStorageBehavior;
        this.resourceMeterBehavior.vertical = true;
        this.behaviorList.add(this.resourceMeterBehavior);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "generator";
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("generator", this.world.gameScreen.mainGroup);
        this.group.setOrigin(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f - 30.0f);
        this.onImage = new Image(this.world.gameScreen.skin.getDrawable("generator-light"));
        this.onImage.setSize(18.0f, 10.0f);
        this.onImage.setPosition(70.0f, 65.0f);
        this.onImage.setColor(0.0f, 1.0f, 0.0f, 0.0f);
        this.onImage.setVisible(false);
        this.group.addActor(this.onImage);
    }

    @Override
    public void startBehaviors() {
        this.togglePower(this.fuelStorageBehavior.amount > 0.0f);
        if (this.hasPower && this.group != null) {
            this.group.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.0f, 0.98f, 0.13f), (Action)Actions.scaleTo(1.0f, 1.01f, 0.13f))));
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        if (this.baseDisaster != null) {
            this.powerGenRate = 0.0f;
        } else if (this.fuelStorageBehavior.amount > 0.0f) {
            this.fuelStorageBehavior.consume(1.0f * delta);
            this.powerGenRate = this.maxPowerGen;
        } else {
            this.powerGenRate = 0.0f;
        }
        if (this.powerGenRate <= 0.0f && this.hasPower) {
            this.togglePower(false);
        } else if (this.powerGenRate > 0.0f && !this.hasPower) {
            this.togglePower(true);
        }
        this.updateVolume();
    }

    private void updateVolume() {
        if (this.hasPower) {
            this.volume = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 400.0f, 0.3f);
            this.pan = Audio.getInstance().playerDistancePan(this.world, new Vector2(this.getXCenter(), this.getYCenter()));
        } else {
            this.volume = 0.0f;
        }
        if (this.volume <= 0.0f) {
            this.sound.pause(this.soundId);
        } else {
            this.sound.resume(this.soundId);
            Audio.getInstance().updateLoopingSoundVolume(this.sound, this.soundId, this.volume, this.pan);
        }
    }

    @Override
    public void setPower(boolean on) {
    }

    private void togglePower(boolean on) {
        if (on) {
            System.out.println("generator on");
            this.onImage.setVisible(true);
            this.smoke.pfx.reset();
            this.smoke.pfx.start();
            this.light.setActive(true);
        } else {
            System.out.println("generator off");
            this.onImage.setVisible(false);
            this.smoke.pfx.allowCompletion();
            this.light.setActive(false);
            this.group.clearActions();
        }
        this.hasPower = on;
    }

    public void addSmoke() {
        this.smoke = new ParticleActor(Gdx.files.internal("particles/smelter.p"), this.world.gameScreen.atlas, false);
        this.smoke.setPosition(TILE_SIZE / 2.0f - 25.0f, TILE_SIZE - 20.0f);
        this.smoke.pfx.allowCompletion();
        this.group.addActor(this.smoke);
    }

    @Override
    public void interact(Player player) {
        if (this.baseDisaster != null) {
            this.baseDisaster.fix();
        } else {
            for (Behavior b : this.behaviorList) {
                b.interact(player);
            }
        }
    }

    @Override
    public void handleDroppedItem(PlayerInventory playerInventory, ItemStack stack) {
        boolean showPowerPopup = true;
        if (this.fuelStorageBehavior.amount + 30.0f < this.fuelStorageBehavior.capacity) {
            if (stack.getId().equals("wood")) {
                showPowerPopup = false;
                this.fuelStorageBehavior.store(woodFuelValue);
                playerInventory.consumeItem(stack, 1);
                this.handleDropItemAnimation();
                try {
                    // Broadcast fuel update to clients
                    float amt = this.fuelStorageBehavior.amount;
                    String payload = "GENERATOR_FUEL:" + this.worldX + ":" + this.worldY + ":" + amt;
                    com.cairn4.moonbase.NetworkHelper.sendPayload(this.world.gameScreen, payload);
                } catch (Exception ignored) {}
            }
            if (stack.getId().equals("plant")) {
                showPowerPopup = false;
                this.fuelStorageBehavior.store(plantFuelValue);
                playerInventory.consumeItem(stack, 1);
                this.handleDropItemAnimation();
                try {
                    float amt = this.fuelStorageBehavior.amount;
                    String payload = "GENERATOR_FUEL:" + this.worldX + ":" + this.worldY + ":" + amt;
                    com.cairn4.moonbase.NetworkHelper.sendPayload(this.world.gameScreen, payload);
                } catch (Exception ignored) {}
            }
            if (stack.getId().equals("root")) {
                showPowerPopup = false;
                this.fuelStorageBehavior.store(rootFuelValue);
                playerInventory.consumeItem(stack, 1);
                this.handleDropItemAnimation();
                try {
                    float amt = this.fuelStorageBehavior.amount;
                    String payload = "GENERATOR_FUEL:" + this.worldX + ":" + this.worldY + ":" + amt;
                    com.cairn4.moonbase.NetworkHelper.sendPayload(this.world.gameScreen, payload);
                } catch (Exception ignored) {}
            }
            if (stack.getId().equals("fruit")) {
                showPowerPopup = false;
                this.fuelStorageBehavior.store(fruitFuelValue);
                playerInventory.consumeItem(stack, 1);
                this.handleDropItemAnimation();
                try {
                    float amt = this.fuelStorageBehavior.amount;
                    String payload = "GENERATOR_FUEL:" + this.worldX + ":" + this.worldY + ":" + amt;
                    com.cairn4.moonbase.NetworkHelper.sendPayload(this.world.gameScreen, payload);
                } catch (Exception ignored) {}
            }
            if (stack.getId().equals("seed") || stack.getId().equals("root-seed")) {
                showPowerPopup = false;
                this.fuelStorageBehavior.store(seedFuelValue);
                playerInventory.consumeItem(stack, 1);
                this.handleDropItemAnimation();
                try {
                    float amt = this.fuelStorageBehavior.amount;
                    String payload = "GENERATOR_FUEL:" + this.worldX + ":" + this.worldY + ":" + amt;
                    com.cairn4.moonbase.NetworkHelper.sendPayload(this.world.gameScreen, payload);
                } catch (Exception ignored) {}
            }
        }
        if (showPowerPopup) {
            BasePowerPopup basePowerPopup = new BasePowerPopup(this.world.gameScreen, this);
            this.world.gameScreen.showMenu(basePowerPopup);
        }
    }

    private void handleDropItemAnimation() {
        this.group.addAction(Actions.sequence((Action)Actions.scaleTo(1.05f, 0.95f, 0.08f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.15f), (Action)Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.0f, 0.98f, 0.13f), (Action)Actions.scaleTo(1.0f, 1.01f, 0.13f)))));
    }

    @Override
    public void destroy() {
        this.sound.stop(this.soundId);
        super.destroy();
    }
}

