/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.Conduit;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.ToggleBehavior;

public class LightSensor
extends Conduit {
    Image switchImage;
    Image toggleImage;
    ToggleBehavior toggleBehavior;
    private boolean enoughSun;
    private final Color offColor = new Color(0.5f, 0.5f, 0.5f, 1.0f);

    public LightSensor(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.toggleBehavior = new ToggleBehavior();
        this.toggleBehavior.on = true;
        this.behaviorList.add(this.toggleBehavior);
        this.setLightColor(0.7f, 0.7f, 0.6f, 0.4f);
    }

    @Override
    protected void createDrawables() {
        super.createDrawables();
        this.switchImage = new Image(this.world.gameScreen.skin.getDrawable("lightsensor"));
        this.switchImage.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.switchImage.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.group.addActor(this.switchImage);
        this.toggleImage = new Image(this.world.gameScreen.skin.getDrawable("lightsensor-day"));
        this.toggleImage.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f - 5.0f, 1);
        this.group.addActor(this.toggleImage);
    }

    @Override
    public void startBehaviors() {
        if (this.toggleBehavior.on) {
            this.toggleImage.setDrawable(this.world.gameScreen.skin.getDrawable("lightsensor-day"));
        } else {
            this.toggleImage.setDrawable(this.world.gameScreen.skin.getDrawable("lightsensor-night"));
        }
        this.enoughSun = this.enoughDaylight();
        if (this.enoughSun) {
            this.toggleImage.setColor(Color.WHITE);
        } else {
            this.toggleImage.setColor(this.offColor);
        }
        this.updateBases();
    }

    @Override
    public void setupLight() {
        this.light = new PointLight(this.world.rayHandler, 16, this.getLightOnColor(), 0.3125f, this.getXCenter() / 256.0f, this.getYCenter() / 256.0f);
        this.light.setSoftnessLength(0.0390625f);
        this.light.attachToBody(this.body);
        this.light.setIgnoreAttachedBody(true);
        this.light.setStaticLight(true);
        this.light.setContactFilter(this.categoryBits, this.categoryBits, this.maskBits);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.enoughSun != this.enoughDaylight()) {
            this.enoughSun = this.enoughDaylight();
            this.updateBases();
        }
        if (this.toggleBehavior.on) {
            if (this.enoughSun) {
                this.toggleImage.setColor(Color.WHITE);
            } else {
                this.toggleImage.setColor(this.offColor);
            }
        } else if (this.enoughSun) {
            this.toggleImage.setColor(this.offColor);
        } else {
            this.toggleImage.setColor(Color.WHITE);
        }
    }

    @Override
    public void interact(Player player) {
        this.toggle();
    }

    private void toggle() {
        boolean bl = this.toggleBehavior.on = !this.toggleBehavior.on;
        if (this.toggleBehavior.on) {
            this.toggleImage.setDrawable(this.world.gameScreen.skin.getDrawable("lightsensor-day"));
        } else {
            this.toggleImage.setDrawable(this.world.gameScreen.skin.getDrawable("lightsensor-night"));
        }
        this.updateBases();
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "lightsensor";
    }

    @Override
    public boolean usesBaseGroup() {
        if (this.toggleBehavior != null) {
            if (this.toggleBehavior.on) {
                return this.enoughDaylight();
            }
            return !this.enoughDaylight();
        }
        return this.enoughDaylight();
    }

    private boolean enoughDaylight() {
        return this.world.dayCycle.getDayLight() > 0.7f;
    }

    @Override
    public boolean canInteract(Player player) {
        return true;
    }
}

