/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;

public class MagicPower
extends BaseModule {
    private Image shadow;
    private ParticleActor particles;

    public MagicPower(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerGenRate = 9999999.0f;
        this.powerDrawRate = 0.0f;
        this.updateBases();
    }

    @Override
    public boolean blocksWind() {
        return false;
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("magic", this.world.gameScreen.mainGroup);
        this.image.setColor(0.4f, 0.0f, 0.9f, 1.0f);
        this.image.addAction(Actions.forever(Actions.sequence((Action)Actions.moveBy(0.0f, 5.0f, 1.0f, Interpolation.sine), (Action)Actions.moveBy(0.0f, -5.0f, 1.0f, Interpolation.sine))));
        this.image.setSize(Tile.TILE_SIZE * 0.7f, Tile.TILE_SIZE * 0.7f);
        this.image.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f + 10.0f, 1);
        this.shadow = new Image(this.world.gameScreen.skin.getDrawable("small-gradient-circle"));
        this.shadow.setSize(Tile.TILE_SIZE * 0.74f, Tile.TILE_SIZE * 0.55f);
        this.shadow.setOrigin(1);
        this.shadow.setColor(0.21f, 0.0f, 0.7f, 0.34f);
        this.shadow.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f - 65.0f, 4);
        this.group.addActor(this.shadow);
        this.shadow.toBack();
        this.shadow.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.02f, 1.02f, 1.0f, Interpolation.sine), (Action)Actions.scaleTo(0.98f, 0.98f, 1.0f, Interpolation.sine))));
        this.group.setUserObject(Float.valueOf(this.getYCenter()));
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/lightning-prestrike.p", ParticleEffect.class));
        this.particles = new ParticleActor(p, false);
        this.particles.pfx.start();
        this.particles.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f);
        this.group.addActor(this.particles);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "magic-power";
    }

    @Override
    public void setupLight() {
        Color c = new Color(0.5f, 0.1f, 1.0f, 1.0f);
        this.light = new PointLight(this.world.rayHandler, 32, c, 0.8984375f, this.getXCenter() / 256.0f, this.getYCenter() / 256.0f);
        this.light.setXray(true);
    }

    @Override
    public void setPower(boolean on) {
        super.setPower(true);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}

