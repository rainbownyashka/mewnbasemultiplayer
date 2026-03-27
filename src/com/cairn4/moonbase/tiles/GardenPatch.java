/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.CooldownCallback;
import com.cairn4.moonbase.tiles.behaviors.CooldownTimer;
import com.cairn4.moonbase.tiles.behaviors.DropItemOnBaseBehavior;
import com.cairn4.moonbase.tiles.behaviors.GreenHouseItemCrafter;
import com.cairn4.moonbase.tiles.behaviors.GreenHouseSpriteBehavior;
import java.util.Observable;
import java.util.Observer;

public class GardenPatch
extends BaseModule
implements Observer {
    Player player;
    PlayerInventory playerInventory;
    private Image buildFinishedIcon;
    private GreenHouseItemCrafter itemCrafter;
    private DropItemOnBaseBehavior dropItemOnBaseBehavior;
    private GreenHouseSpriteBehavior ghsp;
    private CooldownTimer manualWaterCooldown;

    public GardenPatch(final World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        world.weatherManager.addObserver(this);
        this.waterConsumptionRate = 0.125f;
        this.powerDrawRate = 0.0f;
        this.componentGroup.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.componentGroup.setOrigin(0.0f, -50.0f);
        this.dropItemOnBaseBehavior = new DropItemOnBaseBehavior();
        this.dropItemOnBaseBehavior.baseModule = this;
        this.dropItemOnBaseBehavior.acceptedItemIds.add("seed");
        this.dropItemOnBaseBehavior.acceptedItemIds.add("root-seed");
        this.dropItemOnBaseBehavior.acceptedItemIds.add("blueshroom-seed");
        this.dropItemOnBaseBehavior.acceptedItemIds.add("drink-water");
        this.behaviorList.add(this.dropItemOnBaseBehavior);
        this.manualWaterCooldown = new CooldownTimer(new CooldownCallback(){

            @Override
            public void callback() {
                if (world.weatherManager.getCurrentData().rainRate > 0.0f) {
                    GardenPatch.this.manualWaterCooldown.trigger();
                } else {
                    GardenPatch.this.forceSetWater(false);
                }
            }
        });
        this.manualWaterCooldown.length = 370.0f;
        this.manualWaterCooldown.start();
        this.behaviorList.add(this.manualWaterCooldown);
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/plant-finished.p", ParticleEffect.class));
        ParticleActor finishedParticleEffect = new ParticleActor(p, false);
        this.componentGroup.addActor(finishedParticleEffect);
        finishedParticleEffect.pfx.allowCompletion();
        this.itemCrafter = new GreenHouseItemCrafter();
        this.itemCrafter.baseModule = this;
        this.itemCrafter.buildQueueSizeLimit = 1;
        this.itemCrafter.buildSpeedMultiplier = 0.8f;
        this.itemCrafter.requireWaterToCraft = true;
        this.itemCrafter.requirePowerToCraft = false;
        this.itemCrafter.requireOxygenToCraft = false;
        this.itemCrafter.soundFinishBuilding = "sounds/planthit1.ogg";
        this.itemCrafter.setupBuildables(this.builderId);
        this.itemCrafter.finishedParticles = finishedParticleEffect;
        this.behaviorList.add(this.itemCrafter);
        this.ghsp = new GreenHouseSpriteBehavior(this.world.gameScreen, this.itemCrafter, this.componentGroup);
        this.behaviorList.add(this.ghsp);
        finishedParticleEffect.toFront();
        if (world.weatherManager.getCurrentData().rainRate > 0.0f) {
            this.manualWaterCooldown.trigger();
        }
    }

    @Override
    public boolean usesBaseGroup() {
        return false;
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    public boolean visuallyConnectToConduits() {
        return false;
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("gardenpatch-dry", this.world.gameScreen.floorGroup);
    }

    @Override
    public void setupLight() {
    }

    @Override
    public void startBehaviors() {
        this.itemCrafter.start();
        this.ghsp.start();
        this.forceSetWater(this.manualWaterCooldown.isRunning());
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        if (this.manualWaterCooldown != null) {
            this.setWater(this.manualWaterCooldown.isRunning());
        }
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "gardenpatch";
    }

    @Override
    public void interact(Player player) {
        super.interact(player);
        this.player = player;
        this.playerInventory = player.getPlayerInventory();
        this.itemCrafter.player = player;
        this.itemCrafter.playerInventory = this.playerInventory;
        if (this.itemCrafter != null) {
            if (this.itemCrafter.itemsToCollect.size > 0) {
                this.itemCrafter.collectItem();
            } else {
                for (Behavior b : this.behaviorList) {
                    b.interact(player);
                }
            }
        } else {
            Gdx.app.error("MoonBase", "CraftingStation: no ItemCrafter behavior");
        }
    }

    @Override
    public void dropPickup() {
        this.itemCrafter.dropItems(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos());
        super.dropPickup();
    }

    @Override
    public void destroy() {
        this.world.weatherManager.deleteObserver(this);
        super.destroy();
    }

    @Override
    public void handleDroppedItem(PlayerInventory playerInventory, ItemStack itemStack) {
        if (itemStack.getId().equals("drink-water")) {
            if (this.manualWaterCooldown.isReady()) {
                this.componentInteractAnim();
                playerInventory.consumeItem(itemStack, 1);
                this.manualWaterCooldown.trigger();
                float pitch = MathUtils.random(1.0f, 1.2f);
                Audio.getInstance().playSound("sounds/refillWaterSupply.ogg", 0.8f, pitch);
            }
        } else if (!this.itemCrafter.isQueueFull()) {
            this.componentInteractAnim();
            this.itemCrafter.playerInventory = playerInventory;
            this.itemCrafter.player = this.player;
            String growsIntoId = itemStack.item.getData().growsInto;
            if (!growsIntoId.equals("")) {
                this.itemCrafter.addItemToBuildQueue(ItemFactory.getItemData(growsIntoId), true);
            } else {
                Gdx.app.error("MewnBase", "GardenPatch: issue planting seed, it might not have its growsInto defined");
            }
        }
        this.forceSetWater(this.manualWaterCooldown.running);
    }

    private void componentInteractAnim() {
        this.componentGroup.addAction(Actions.sequence((Action)Actions.scaleTo(1.05f, 0.95f, 0.08f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.15f)));
    }

    public void forceSetWater(boolean newState) {
        if (this.image != null) {
            if (newState) {
                this.noWaterIcon(false);
                this.image.setDrawable(this.world.gameScreen.skin.getDrawable("gardenpatch"));
            } else {
                this.noWaterIcon(this.itemCrafter.isBuilding());
                this.image.setDrawable(this.world.gameScreen.skin.getDrawable("gardenpatch-dry"));
            }
            this.hasWater = newState;
        }
    }

    @Override
    public void setWater(boolean newState) {
        if (this.image != null && newState != this.hasWater) {
            if (newState) {
                this.noWaterIcon(false);
                this.image.setDrawable(this.world.gameScreen.skin.getDrawable("gardenpatch"));
            } else {
                this.noWaterIcon(this.itemCrafter.building);
                this.image.setDrawable(this.world.gameScreen.skin.getDrawable("gardenpatch-dry"));
            }
            this.hasWater = newState;
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o == "rainStart") {
            this.manualWaterCooldown.trigger();
        }
        if (o == "rainStop") {
            this.manualWaterCooldown.trigger();
        }
        this.forceSetWater(this.manualWaterCooldown.isRunning());
    }
}

