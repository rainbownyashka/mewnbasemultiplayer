/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.StorageColorOptions;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.IntValueBehavior;
import com.cairn4.moonbase.tiles.behaviors.ToggleBehavior;

public class CrystalLamp
extends BaseModule {
    public IntValueBehavior colorIndexBehavior;
    public ToggleBehavior toggleBehavior;
    private final float maxPowerDraw = 1.0f;
    private Image rearImage;
    private PointLight light2;
    private AdditiveImage glowImage;
    public IntValueBehavior intColorBehavior;

    public CrystalLamp(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 1.0f;
        this.powerGenRate = 0.0f;
        this.setupPhysics("crystallamp");
        this.toggleBehavior = new ToggleBehavior();
        this.toggleBehavior.on = true;
        this.behaviorList.add(this.toggleBehavior);
        this.intColorBehavior = new IntValueBehavior();
        this.intColorBehavior.value = 0;
        this.intColorBehavior.setId("intColorBehavior");
        this.behaviorList.add(this.intColorBehavior);
        this.changeColor(0);
        this.updateBases();
    }

    @Override
    public void startBehaviors() {
        super.startBehaviors();
        this.updateColor();
        this.updateLightState();
    }

    private void updateLightState() {
        boolean on = this.toggleBehavior.on;
        this.light.setActive(on);
        this.powerDrawRate = on ? this.maxPowerDraw : 0.0f;
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("crystal-lamp", this.world.gameScreen.mainGroup);
        this.group.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + TILE_SIZE / 2.0f));
        this.rearImage = new Image(this.world.gameScreen.skin.getDrawable("crystal-lamp-rear"));
        this.rearImage.setSize(this.image.getWidth(), this.image.getHeight());
        this.group.addActor(this.rearImage);
        this.rearImage.setPosition(this.image.getX(), this.image.getY());
        this.rearImage.setColor(Color.CYAN);
        this.rearImage.toBack();
        this.glowImage = new AdditiveImage(this.world.gameScreen.skin.getDrawable("small-gradient-circle"));
        this.glowImage.setSize(TILE_SIZE * 1.4f, TILE_SIZE * 1.4f);
        this.glowImage.setOrigin(1);
        this.glowImage.setPosition(TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f + 20.0f, 1);
        this.glowImage.setColor(Color.CYAN);
        this.glowImage.getColor().a = 0.5f;
        this.group.addActor(this.glowImage);
    }

    @Override
    public void setupLight() {
        this.light = new PointLight(this.world.rayHandler, 24, Color.WHITE, Tile.TILE_SIZE * 3.5f / 256.0f, this.getXCenter() / 256.0f, (this.getYCenter() + 20.0f) / 256.0f);
        this.light.setStaticLight(true);
        this.light.setXray(true);
        this.light.setIgnoreAttachedBody(true);
        this.light2 = new PointLight(this.world.rayHandler, 12, Color.WHITE, Tile.TILE_SIZE / 1.6f / 256.0f, this.getXCenter() / 256.0f, (this.getYCenter() + 15.0f + 20.0f) / 256.0f);
        this.light2.setStaticLight(true);
        this.light2.setXray(true);
        this.light2.setIgnoreAttachedBody(true);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "crystallamp";
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    public void interact(Player player) {
        if (player.playerInventory.getEquippedItemId().equals("paintbrush")) {
            this.changeColor(player.getPaintColorIndex());
            this.paintPuffFx();
        } else {
            super.interact(player);
            this.toggleBehavior.interact(player);
            this.updateLightState();
        }
    }

    @Override
    public void destroy() {
        this.light2.remove();
        super.destroy();
    }

    public void changeColor(int colorIndex) {
        this.intColorBehavior.value = colorIndex;
        if (this.intColorBehavior.value >= StorageColorOptions.colors.length || this.intColorBehavior.value < 0) {
            MoonBase.error("Invalid color index (max = " + (StorageColorOptions.colors.length - 1) + ")");
            this.intColorBehavior.value = 0;
        }
        this.updateColor();
    }

    private void updateColor() {
        float tintStrength = 0.35f;
        Color c = StorageColorOptions.colors[this.intColorBehavior.value];
        float r = MathUtils.lerp(c.r, 1.0f, tintStrength);
        float g = MathUtils.lerp(c.g, 1.0f, tintStrength);
        float b = MathUtils.lerp(c.b, 1.0f, tintStrength);
        if (!this.hasPower) {
            r *= CrystalLamp.POWER_OFF_COLOR.r;
            g *= CrystalLamp.POWER_OFF_COLOR.g;
            b *= CrystalLamp.POWER_OFF_COLOR.b;
        }
        this.rearImage.setColor(new Color(c.r, c.g, c.b, 1.0f));
        this.light.setColor(r, g, b, 1.0f);
        this.light2.setColor(r, g, b, 1.0f);
        this.glowImage.setColor(r, g, b, 0.5f);
    }

    public void paintPuffFx() {
        Audio.getInstance().playSound("sounds/paintbrush-splat.ogg", 0.5f, 1.2f);
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/paintbrush-splat.p", ParticleEffect.class));
        ParticleActor paintPuff = new ParticleActor(p, true);
        paintPuff.setPosition(this.getXCenter(), this.getYCenter());
        this.world.gameScreen.topGroup.addActor(paintPuff);
        for (ParticleEmitter e : paintPuff.pfx.getEmitters()) {
            ParticleEmitter.GradientColorValue gradient = e.getTint();
            Color newColor = StorageColorOptions.colors[this.intColorBehavior.value];
            gradient.getColors()[0] = newColor.r;
            gradient.getColors()[1] = newColor.g;
            gradient.getColors()[2] = newColor.b;
        }
        paintPuff.pfx.start();
    }

    @Override
    protected void setLightColor(float r1, float g1, float b1, float a1) {
        float tintStrength = 0.35f;
        Color c = StorageColorOptions.colors[this.intColorBehavior.value];
        float r = MathUtils.lerp(c.r, 1.0f, tintStrength);
        float g = MathUtils.lerp(c.g, 1.0f, tintStrength);
        float b = MathUtils.lerp(c.b, 1.0f, tintStrength);
        if (!this.hasPower) {
            r *= CrystalLamp.POWER_OFF_COLOR.r;
            g *= CrystalLamp.POWER_OFF_COLOR.g;
            b *= CrystalLamp.POWER_OFF_COLOR.b;
        }
        if (this.light != null) {
            this.light.setColor(r1, g1, b1, a1);
        }
        if (this.light2 != null) {
            this.light2.setColor(r1, g1, b1, a1);
        }
    }

    @Override
    protected void setLight(boolean on) {
        super.setLight(on);
        if (this.light2 != null) {
            this.light2.setActive(on);
        }
        this.glowImage.setVisible(on);
    }

    @Override
    public Color getLightOnColor() {
        return StorageColorOptions.colors[this.intColorBehavior.value];
    }

    @Override
    public void setPower(boolean on) {
        super.setPower(on);
        if (this.toggleBehavior.on && on) {
            this.rearImage.setColor(this.getPoweredOffColor());
            this.light.setActive(on);
            this.light2.setActive(on);
            this.glowImage.setVisible(on);
            this.rearImage.setColor(this.getLightOnColor());
            this.rearImage.getColor().a = 1.0f;
        } else {
            this.rearImage.setColor(this.getPoweredOffColor());
            this.light.setActive(false);
            this.light2.setActive(false);
            this.glowImage.setVisible(false);
        }
    }
}

