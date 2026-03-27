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
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.ToggleBehavior;

public class FloodLight
extends BaseModule {
    Image glowImage;
    PointLight bigLight;
    private final float maxPowerDraw = 1.0f;
    public ToggleBehavior toggleBehavior;
    protected Color lightOnColor;
    private PointLight light2;
    private PointLight light3;
    private final float yOffset = 0.0f;

    public FloodLight(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 1.0f;
        this.powerGenRate = 0.0f;
        this.setupPhysics("floodlight");
        this.toggleBehavior = new ToggleBehavior();
        this.toggleBehavior.on = true;
        this.behaviorList.add(this.toggleBehavior);
        this.updateBases();
    }

    @Override
    public void startBehaviors() {
        this.updateLightState();
    }

    @Override
    public void setupLight() {
        this.lightOnColor = new Color(0.3f, 0.7f, 1.0f, 1.0f);
        this.light = new PointLight(this.world.rayHandler, 24, this.lightOnColor, 2.734375f, this.getXCenter() / 256.0f, (this.getYCenter() + 0.0f) / 256.0f);
        this.light.setStaticLight(true);
        this.light.setXray(true);
        this.light.setIgnoreAttachedBody(true);
        this.light2 = new PointLight(this.world.rayHandler, 12, this.lightOnColor, Tile.TILE_SIZE / 2.0f / 256.0f, (this.getXCenter() - 35.0f) / 256.0f, (this.getYCenter() + 35.0f + 0.0f) / 256.0f);
        this.light2.setStaticLight(true);
        this.light2.setXray(true);
        this.light2.setIgnoreAttachedBody(true);
        this.light3 = new PointLight(this.world.rayHandler, 12, this.lightOnColor, Tile.TILE_SIZE / 2.0f / 256.0f, (this.getXCenter() + 35.0f) / 256.0f, (this.getYCenter() + 35.0f + 0.0f) / 256.0f);
        this.light3.setStaticLight(true);
        this.light3.setXray(true);
        this.light3.setIgnoreAttachedBody(true);
    }

    @Override
    public Color getLightOnColor() {
        return this.lightOnColor;
    }

    @Override
    public void setPower(boolean on) {
        super.setPower(on);
        if (this.toggleBehavior.on && on) {
            this.glowImage.setVisible(on);
            this.light.setActive(on);
            this.light2.setActive(on);
            this.light3.setActive(on);
        } else {
            this.glowImage.setVisible(false);
            this.light.setActive(false);
            this.light2.setActive(false);
            this.light3.setActive(false);
        }
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("floodlight", this.world.gameScreen.mainGroup);
        this.group.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + TILE_SIZE / 2.0f));
        this.image.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f + 0.0f, 1);
        this.glowImage = new Image(this.world.gameScreen.skin.getDrawable("floodlight-on"));
        this.glowImage.setSize(TILE_SIZE, TILE_SIZE);
        this.glowImage.setOrigin(1);
        this.glowImage.setPosition(TILE_SPACING, 0.0f);
        this.group.addActor(this.glowImage);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "floodlight";
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
    }

    @Override
    public void interact(Player player) {
        super.interact(player);
        this.toggleBehavior.interact(player);
        this.updateLightState();
    }

    @Override
    public void destroy() {
        this.light2.remove();
        this.light3.remove();
        super.destroy();
    }

    private void updateLightState() {
        boolean on = this.toggleBehavior.on;
        this.light.setActive(on);
        this.light2.setActive(on);
        this.light3.setActive(on);
        this.glowImage.setVisible(on);
        this.powerDrawRate = on ? this.maxPowerDraw : 0.0f;
    }
}

