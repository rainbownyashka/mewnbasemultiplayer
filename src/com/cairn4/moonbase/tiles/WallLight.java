/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;

public class WallLight
extends BaseModule {
    BaseModule.ORIENTATIONS orientation;
    private float maxPowerDraw = 0.5f;
    private boolean canBePowered;

    public WallLight(World world, Chunk chunk, int x, int y, BaseModule.ORIENTATIONS o) {
        super(world, chunk, x, y);
        this.powerDrawRate = 0.5f;
        this.setRotation(o);
    }

    public void setRotation(BaseModule.ORIENTATIONS o) {
        this.orientation = o;
        switch (this.orientation) {
            case north: {
                this.body.setTransform(this.body.getPosition(), -1.5707964f);
                this.group.setRotation(-90.0f);
                break;
            }
            case east: {
                this.body.setTransform(this.body.getPosition(), (float)Math.PI);
                this.group.setRotation(180.0f);
                break;
            }
            case south: {
                this.body.setTransform(this.body.getPosition(), 1.5707964f);
                this.group.setRotation(90.0f);
                break;
            }
            case west: {
                this.body.setTransform(this.body.getPosition(), 0.0f);
                this.group.setRotation(0.0f);
            }
        }
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("walllight");
        this.group.setOrigin(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.image.setOrigin(1);
        this.image.setPosition(TILE_SIZE / 2.0f, TILE_SIZE - 3.0f, 1);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "walllight";
    }

    @Override
    public void setupLight() {
        this.setupPhysics("walllight");
        Color c = new Color(0.9f, 0.9f, 1.0f, 0.5f);
        this.light = new PointLight(this.world.rayHandler, 50, c, 1.09375f, this.getXCenter() / 256.0f, this.getYCenter() / 256.0f);
        this.light.setSoftnessLength(0.0390625f);
        this.light.attachToBody(this.body);
        this.light.setIgnoreAttachedBody(true);
        this.light.setContactFilter((short)2, (short)2, (short)2);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        this.checkForAttachedWall();
        this.light.setActive(this.canBePowered);
    }

    private void checkForAttachedWall() {
        this.canBePowered = false;
        Tile t = null;
        switch (this.orientation) {
            case north: {
                t = this.checkDirectionForWall(this.worldX, this.worldY + 1);
                break;
            }
            case east: {
                t = this.checkDirectionForWall(this.worldX + 1, this.worldY);
                break;
            }
            case south: {
                t = this.checkDirectionForWall(this.worldX, this.worldY - 1);
                break;
            }
            case west: {
                t = this.checkDirectionForWall(this.worldX - 1, this.worldY);
            }
        }
        this.canBePowered = t != null;
    }

    private Tile checkDirectionForWall(int x, int y) {
        Tile t = this.world.getTile(this.worldX, this.worldY + 1);
        if (t != null && t.hasPower) {
            return t;
        }
        return null;
    }

    public BaseModule.ORIENTATIONS getOrientation() {
        return this.orientation;
    }
}

