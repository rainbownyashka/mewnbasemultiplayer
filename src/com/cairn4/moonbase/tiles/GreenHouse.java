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
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.CooldownCallback;
import com.cairn4.moonbase.tiles.behaviors.CooldownTimer;
import com.cairn4.moonbase.tiles.behaviors.DropItemOnBaseBehavior;
import com.cairn4.moonbase.tiles.behaviors.GreenHouseItemCrafter;
import com.cairn4.moonbase.tiles.behaviors.GreenHouseSpriteBehavior;

public class GreenHouse
extends HabitatModule {
    Image greenHouseImage;
    Player player;
    PlayerInventory playerInventory;
    private Image buildFinishedIcon;
    private GreenHouseItemCrafter itemCrafter;
    private DropItemOnBaseBehavior dropItemOnBaseBehavior;
    private GreenHouseSpriteBehavior ghsp;
    private CooldownTimer manualWaterCooldown;
    private boolean upgraded = false;
    private static float NORMAL_WATER_RATE = 0.125f;
    private static float UPGRADED_WATER_RATE = NORMAL_WATER_RATE * 0.75f;

    public GreenHouse(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerPriority = 5;
        this.waterConsumptionRate = NORMAL_WATER_RATE;
        this.powerDrawRate = 3.0f;
        this.componentGroup.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.componentGroup.setOrigin(0.0f, -50.0f);
        this.greenHouseImage = new Image(world.gameScreen.skin.getDrawable("greenhouse"));
        this.greenHouseImage.setSize(TILE_SIZE * 0.7f, TILE_SIZE * 0.7f);
        this.greenHouseImage.setPosition(0.0f, 0.0f, 1);
        this.componentGroup.addActor(this.greenHouseImage);
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
                GreenHouse.this.forceSetWater(false);
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
        this.itemCrafter.requireWaterToCraft = true;
        this.itemCrafter.requireOxygenToCraft = false;
        this.itemCrafter.requirePowerToCraft = true;
        this.itemCrafter.setupBuildables(this.builderId);
        this.itemCrafter.soundFinishBuilding = "sounds/planthit1.ogg";
        this.itemCrafter.finishedParticles = finishedParticleEffect;
        this.behaviorList.add(this.itemCrafter);
        this.ghsp = new GreenHouseSpriteBehavior(this.world.gameScreen, this.itemCrafter, this.componentGroup);
        this.behaviorList.add(this.ghsp);
        finishedParticleEffect.toFront();
        this.forceSetWater(this.manualWaterCooldown.isRunning());
    }

    @Override
    public void startBehaviors() {
        super.startBehaviors();
        this.itemCrafter.start();
        this.ghsp.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public float getWaterConsumptionRate() {
        if (this.itemCrafter != null && this.itemCrafter.isBuilding() && !this.manualWaterCooldown.isRunning()) {
            return this.waterConsumptionRate;
        }
        return 0.0f;
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "greenhouse";
    }

    @Override
    public void interactMain(Player player) {
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
    }

    private void componentInteractAnim() {
        this.componentGroup.addAction(Actions.sequence((Action)Actions.scaleTo(1.05f, 0.95f, 0.08f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.15f)));
    }

    public void forceSetWater(boolean newState) {
        if (this.greenHouseImage != null) {
            if (newState) {
                this.noWaterIcon(false);
                this.greenHouseImage.setDrawable(this.world.gameScreen.skin.getDrawable("greenhouse"));
            } else {
                this.noWaterIcon(this.itemCrafter.isBuilding());
                this.greenHouseImage.setDrawable(this.world.gameScreen.skin.getDrawable("greenhouse-dry"));
            }
            this.hasWater = newState;
            Gdx.app.debug("MewnBase", "GreenHouse: forced water to state :" + newState);
        }
    }

    private boolean shouldBeWet() {
        boolean wet = false;
        if (this.manualWaterCooldown != null && this.manualWaterCooldown.isRunning()) {
            wet = true;
        }
        if (!wet && this.getBaseGroup().getTotalWaterStored() > 0.0f) {
            wet = true;
        }
        return wet;
    }

    @Override
    public void setWater(boolean on) {
        boolean wet;
        this.hasWater = wet = this.shouldBeWet();
        if (this.greenHouseImage != null) {
            if (wet) {
                if (this.greenHouseImage.getDrawable().toString().equals("greenhouse-dry")) {
                    this.greenHouseImage.setDrawable(this.world.gameScreen.skin.getDrawable("greenhouse"));
                }
                if (this.noWaterIcon.hasActions()) {
                    this.noWaterIcon(false);
                }
            } else {
                if (this.greenHouseImage.getDrawable().toString().equals("greenhouse")) {
                    this.greenHouseImage.setDrawable(this.world.gameScreen.skin.getDrawable("greenhouse-dry"));
                }
                if (this.itemCrafter.isBuilding()) {
                    if (!this.noWaterIcon.hasActions()) {
                        this.noWaterIcon(true);
                    }
                } else {
                    this.noWaterIcon(false);
                }
            }
        }
    }

    public void upgradeWaterUsage() {
        this.upgraded = true;
        this.waterConsumptionRate = UPGRADED_WATER_RATE;
    }
}

