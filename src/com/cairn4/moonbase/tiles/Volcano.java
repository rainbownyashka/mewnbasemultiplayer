/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.ProjectileFactory;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Projectile;
import com.cairn4.moonbase.entities.TemperatureDealer;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.Localization;

public class Volcano
extends Tile {
    private float shootTimer = 0.0f;
    private float shootDelay = 3.0f;
    private float minShootDelay = 5.0f;
    private float maxShootDelay = 15.0f;
    private float minShootRadius = 1.5f * Tile.TILE_SIZE;
    private float maxShootRadius = 3.0f * Tile.TILE_SIZE;
    private PointLight light;
    private Color lightOnColor = new Color(1.0f, 0.25f, 0.05f, 1.0f);
    private Image lavaImage;
    private ParticleActor smokeParticles;
    private TemperatureDealer temperatureDealer;
    private volcanoStates currentState;

    @Override
    public String getMapName() {
        return Localization.get("volcano");
    }

    public Volcano(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.setupPhysics("volcano");
        this.setupLight();
        this.smokeParticles = new ParticleActor(new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/volcano.p", ParticleEffect.class)), true);
        this.smokeParticles.setPosition(this.getXCenter(), this.getYCenter());
        this.smokeParticles.pfx.start();
        world.gameScreen.mainGroup.addActor(this.smokeParticles);
        this.temperatureDealer = new TemperatureDealer(world, this.getXCenter(), this.getYCenter());
        this.temperatureDealer.radius = Tile.TILE_SIZE * 4.0f;
        this.temperatureDealer.temp = 7.0f;
        this.temperatureDealer.duration = 0.0f;
        this.currentState = volcanoStates.sleeping;
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("volcano", this.world.gameScreen.floorGroup);
        this.image.setSize(Tile.TILE_SIZE * 3.0f, Tile.TILE_SIZE * 3.0f);
        this.image.setOrigin(1);
        this.image.setPosition(Tile.TILE_SIZE / 2.0f - 10.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.group.setUserObject(Float.valueOf(this.getYCenter()));
        this.lavaImage = new Image(this.world.gameScreen.skin.getDrawable("volcano-lava"));
        this.lavaImage.setOrigin(1);
        this.lavaImage.setPosition(Tile.TILE_SIZE / 2.0f + 2.0f, Tile.TILE_SIZE / 2.0f - 4.0f, 1);
        this.group.addActor(this.lavaImage);
        this.lavaImage.toBack();
        this.lavaImage.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.05f, 0.97f, 1.5f, Interpolation.sine), (Action)Actions.scaleTo(1.0f, 1.0f, 1.5f, Interpolation.sine))));
    }

    public void setupLight() {
        this.light = new PointLight(this.world.rayHandler, 32, this.lightOnColor, 2.34375f, this.getXCenter() / 256.0f, this.getYCenter() / 256.0f);
        this.light.setXray(true);
        this.light.setStaticLight(true);
    }

    @Override
    public void update(float delta) {
        switch (this.currentState) {
            case active: {
                this.shootLava(delta);
                break;
            }
        }
    }

    private void shootLava(float delta) {
        this.shootTimer += delta;
        if (this.shootTimer > this.shootDelay) {
            this.shootTimer = 0.0f;
            this.shootDelay = MathUtils.random(this.minShootDelay, this.maxShootDelay);
            Vector2 thisPos = new Vector2(this.getXCenter(), this.getYCenter());
            Vector2 playerPos = new Vector2(this.world.player.getXPos(), this.world.player.getYPos());
            if (playerPos.dst(thisPos) < 1200.0f) {
                Vector2 targetPos = new Vector2(this.getXCenter(), this.getYCenter());
                float xOffset = MathUtils.random(this.minShootRadius, this.maxShootRadius);
                float yOffset = MathUtils.random(this.minShootRadius, this.maxShootRadius);
                targetPos.x += (xOffset *= (float)MathUtils.randomSign());
                targetPos.y += (yOffset *= (float)MathUtils.randomSign());
                Projectile p = ProjectileFactory.getInstance().newProjectile(this.world, "volcanoBlob");
                p.setSpawnPosition(this.getXCenter(), this.getYCenter());
                p.worldTargetPos = targetPos;
                p.init();
            }
        }
    }

    @Override
    public Color getMinimapColor() {
        return Color.RED;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            this.currentState = volcanoStates.active;
            this.world.gameScreen.addToStage(this.group, this.world.gameScreen.floorGroup);
            this.world.gameScreen.mainGroup.addActor(this.smokeParticles);
            this.group.setVisible(true);
            this.light.setActive(true);
        } else {
            this.currentState = volcanoStates.sleeping;
            this.group.remove();
            this.group.setVisible(false);
            this.light.setActive(false);
            this.smokeParticles.remove();
        }
        if (this.hasPhysicsBody()) {
            if (visible) {
                this.body.setActive(true);
            } else {
                this.body.setActive(false);
            }
        }
    }

    private static enum volcanoStates {
        active,
        sleeping;

    }
}

