/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.BaseResourceStorageBehavior;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.ResourceMeterBehavior;
import com.cairn4.moonbase.ui.BasePowerPopup;

public class Battery2
extends BaseModule {
    protected Image powerMeterBg;
    protected Image powerMeter;
    public ResourceMeterBehavior resourceMeterBehavior;
    public BaseResourceStorageBehavior powerStorageBehavior;

    public Battery2(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.blocker = true;
        this.hasAir = false;
        this.hasPower = true;
        this.powerGenRate = 0.0f;
        this.powerDrawRate = 0.0f;
        this.setupPhysics("battery2");
        this.updateBases();
        this.powerStorageBehavior = new BaseResourceStorageBehavior();
        this.powerStorageBehavior.amount = 0.0f;
        this.powerStorageBehavior.capacity = 2100.0f;
        this.powerStorageBehavior.type = BaseResources.power;
        this.behaviorList.add(this.powerStorageBehavior);
        this.resourceMeterBehavior = new ResourceMeterBehavior();
        this.resourceMeterBehavior.meterSprite = this.powerMeter;
        this.resourceMeterBehavior.baseModule = this;
        this.resourceMeterBehavior.resourceStorageBehavior = this.powerStorageBehavior;
        this.behaviorList.add(this.resourceMeterBehavior);
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("battery2", this.world.gameScreen.mainGroup);
        this.powerMeter = new Image(this.world.gameScreen.skin.getDrawable("white"));
        this.powerMeter.setSize(80.0f, 15.0f);
        this.powerMeter.setColor(0.007843138f, 0.7176471f, 1.0f, 1.0f);
        this.powerMeter.setPosition(TILE_SIZE / 2.0f, 23.0f, 4);
        this.group.addActor(this.powerMeter);
        this.powerMeter.toBack();
        this.powerMeterBg = new Image(this.world.gameScreen.skin.getDrawable("white"));
        this.powerMeterBg.setSize(80.0f, 15.0f);
        this.powerMeterBg.setColor(Color.BLACK);
        this.powerMeterBg.setPosition(TILE_SIZE / 2.0f, 23.0f, 4);
        this.group.addActor(this.powerMeterBg);
        this.powerMeterBg.toBack();
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "battery2";
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        if (this.powerStorageBehavior.amount == 0.0f && this.light.isActive()) {
            this.light.setActive(false);
        } else if (this.powerStorageBehavior.amount > 0.0f && !this.light.isActive()) {
            this.light.setActive(true);
        }
    }

    @Override
    public void interact(Player player) {
        if (this.baseDisaster != null) {
            this.baseDisaster.fix();
        } else {
            BasePowerPopup basePowerPopup = new BasePowerPopup(this.world.gameScreen, this);
            this.world.gameScreen.showMenu(basePowerPopup);
            super.interact(player);
        }
    }
}

