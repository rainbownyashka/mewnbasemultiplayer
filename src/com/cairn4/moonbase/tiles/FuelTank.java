/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.BaseResourceStorageBehavior;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.ResourceMeterBehavior;

public class FuelTank
extends BaseModule {
    private Image meterBg;
    private Image meter;
    public BaseResourceStorageBehavior fuelStorageBehavior;

    public FuelTank(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 0.0f;
        this.componentGroup.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.componentGroup.setOrigin(0.0f, -50.0f);
        this.componentGroup.toBack();
        this.meter = new Image(world.gameScreen.skin.getDrawable("white"));
        this.meter.setSize(50.0f * Tile.SCALE, 88.0f * Tile.SCALE);
        this.meter.setColor(Color.valueOf("e08c2aff"));
        this.meter.setPosition(0.0f, -41.0f, 4);
        this.componentGroup.addActor(this.meter);
        this.updateBases();
        this.fuelStorageBehavior = new BaseResourceStorageBehavior();
        this.fuelStorageBehavior.setId("fuelStorage");
        this.fuelStorageBehavior.type = BaseResources.fuel;
        this.fuelStorageBehavior.amount = 0.0f;
        this.fuelStorageBehavior.capacity = 100.0f;
        this.behaviorList.add(this.fuelStorageBehavior);
        ResourceMeterBehavior resourceMeterBehavior = new ResourceMeterBehavior();
        resourceMeterBehavior.meterSprite = this.meter;
        resourceMeterBehavior.baseModule = this;
        resourceMeterBehavior.resourceStorageBehavior = this.fuelStorageBehavior;
        resourceMeterBehavior.vertical = true;
        this.behaviorList.add(resourceMeterBehavior);
        this.meterBg.toBack();
        this.setupPhysics("rock");
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("fueltank", this.world.gameScreen.mainGroup);
        this.image.setSize(256.0f * Tile.SCALE, 280.0f * Tile.SCALE);
        this.image.setOrigin(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 3.0f);
        this.image.setPosition(Tile.TILE_SIZE / 2.0f, 0.0f, 4);
        this.meterBg = new Image(this.world.gameScreen.skin.getDrawable("fueltank-back"));
        this.meterBg.setSize(70.0f * Tile.SCALE, 124.0f * Tile.SCALE);
        this.meterBg.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f - 25.0f, 1);
        this.group.addActor(this.meterBg);
        this.meterBg.toBack();
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "fueltank";
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
    }
}

