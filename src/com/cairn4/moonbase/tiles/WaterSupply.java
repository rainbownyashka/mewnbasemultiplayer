/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.behaviors.BaseResourceStorageBehavior;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.DropItemOnBaseBehavior;
import com.cairn4.moonbase.tiles.behaviors.ResourceMeterBehavior;

public class WaterSupply
extends HabitatModule {
    private Image waterMeterBg;
    private Image waterMeter;
    private Image waterMeterTop;
    private Image waterLight;
    private DropItemOnBaseBehavior dropItemOnBaseBehavior;
    public BaseResourceStorageBehavior waterStorageBehavior;
    private Image supplyImage;

    public WaterSupply(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 3.0f;
        this.componentGroup.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.componentGroup.setOrigin(0.0f, -50.0f);
        this.waterMeterBg = new Image(world.gameScreen.skin.getDrawable("white"));
        this.waterMeterBg.setSize(10.0f, 40.0f);
        this.waterMeterBg.setColor(0.0f, 0.0f, 0.0f, 0.7f);
        this.waterMeterBg.setPosition(-20.0f, -2.0f, 16);
        this.componentGroup.addActor(this.waterMeterBg);
        this.waterMeter = new Image(world.gameScreen.skin.getDrawable("white"));
        this.waterMeter.setSize(10.0f, 35.0f);
        this.waterMeter.setColor(0.007843138f, 0.7176471f, 1.0f, 1.0f);
        this.waterMeter.setPosition(-20.0f, -1.0f, 16);
        this.componentGroup.addActor(this.waterMeter);
        this.waterLight = new Image(world.gameScreen.skin.getDrawable("watersupply-light"));
        this.waterLight.setSize(this.waterLight.getWidth() * 0.6f, this.waterLight.getHeight() * 0.6f);
        this.waterLight.setColor(0.0f, 1.0f, 0.0f, 1.0f);
        this.waterLight.setPosition(22.0f, 2.0f);
        this.componentGroup.addActor(this.waterLight);
        this.supplyImage = new Image(world.gameScreen.skin.getDrawable("watersupply"));
        this.supplyImage.setSize(TILE_SIZE * 0.65f, TILE_SIZE * 0.65f);
        this.supplyImage.setPosition(0.0f, 0.0f, 1);
        this.componentGroup.addActor(this.supplyImage);
        this.updateBases();
        this.dropItemOnBaseBehavior = new DropItemOnBaseBehavior();
        this.dropItemOnBaseBehavior.baseModule = this;
        this.dropItemOnBaseBehavior.acceptedItemIds.add("ice");
        this.dropItemOnBaseBehavior.acceptedItemIds.add("drink-water");
        this.behaviorList.add(this.dropItemOnBaseBehavior);
        this.waterStorageBehavior = new BaseResourceStorageBehavior();
        this.waterStorageBehavior.setId("waterStorage");
        this.waterStorageBehavior.type = BaseResources.water;
        this.waterStorageBehavior.amount = 0.0f;
        this.waterStorageBehavior.capacity = 600.0f;
        this.behaviorList.add(this.waterStorageBehavior);
        ResourceMeterBehavior resourceMeterBehavior = new ResourceMeterBehavior();
        resourceMeterBehavior.meterSprite = this.waterMeter;
        resourceMeterBehavior.baseModule = this;
        resourceMeterBehavior.resourceStorageBehavior = this.waterStorageBehavior;
        resourceMeterBehavior.vertical = true;
        this.behaviorList.add(resourceMeterBehavior);
    }

    @Override
    public void startBehaviors() {
        super.startBehaviors();
        this.waterStorageBehavior.capacity = 600.0f;
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "watersupply";
    }

    @Override
    public void interactMain(Player player) {
        this.dropItemOnBaseBehavior.interact(player);
    }

    @Override
    public void handleDroppedItem(PlayerInventory playerInventory, ItemStack itemStack) {
        Gdx.app.debug("MewnBase", "WaterSupply: stored: " + this.getBaseGroup().getTotalWaterStored() + " -- total: " + this.getBaseGroup().getTotalMaxWaterStored());
        if (this.getBaseGroup().getTotalWaterStored() < this.getBaseGroup().getTotalMaxWaterStored()) {
            playerInventory.consumeItem(itemStack, 1);
            if (itemStack.getId().equals("ice")) {
                this.waterStorageBehavior.storeAll(30.0f);
            }
            if (itemStack.getId().equals("drink-water")) {
                this.waterStorageBehavior.storeAll(60.0f);
            }
            this.componentGroup.addAction(Actions.sequence((Action)Actions.scaleTo(1.05f, 0.95f, 0.08f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.15f)));
            float pitch = MathUtils.random(1.0f, 1.2f);
            Audio.getInstance().playSound("sounds/refillWaterSupply.ogg", 0.8f, pitch);
        }
    }

    @Override
    public void setPower(boolean on) {
        if (this.hasPower != on && this.supplyImage != null) {
            if (!this.hasPower) {
                this.animateColor(this.supplyImage, Color.WHITE, 0.25f);
            } else {
                this.animateColor(this.supplyImage, POWER_OFF_COLOR, 0.25f);
            }
        }
        super.setPower(on);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        if (this.waterStorageBehavior.amount > 0.0f) {
            this.waterLight.setColor(Color.GREEN);
        } else {
            this.waterLight.setColor(Color.RED);
        }
    }
}

