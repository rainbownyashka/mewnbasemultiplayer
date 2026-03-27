/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.TemperatureDealer;
import com.cairn4.moonbase.tiles.Tile;

public class LavaFlow
extends Tile {
    final int bS = 1;
    final int bE = 2;
    final int bN = 4;
    final int bW = 8;
    protected Fixture wall_n_fix;
    protected Fixture wall_e_fix;
    protected Fixture wall_s_fix;
    protected Fixture wall_w_fix;
    private PointLight light;
    private TemperatureDealer temperatureDealer;
    private ParticleActor lavaParticles;
    private float sleepTimer;
    private static float sleepDelay = 1.0f;
    public static final float sleepDistance = Tile.TILE_SIZE * 15.0f;

    public LavaFlow(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.setupLight();
        this.addParticles();
        this.temperatureDealer = new TemperatureDealer(world, this.getXCenter(), this.getYCenter());
        this.temperatureDealer.radius = Tile.TILE_SIZE * 5.0f;
        this.temperatureDealer.temp = 4.0f;
        this.temperatureDealer.duration = 0.0f;
        this.setupPhysics(null);
    }

    @Override
    protected void setupPhysics(String loader) {
        super.setupPhysics(null);
        FixtureDef fd = new FixtureDef();
        fd.density = 1.0f;
        fd.friction = 0.5f;
        fd.restitution = 0.3f;
        fd.filter.categoryBits = this.categoryBits;
        fd.filter.maskBits = (short)(this.maskBits + this.categoryBits);
        this.world.bodyEditorLoader.attachFixture(this.body, "lava-north", fd, 0.5f);
        this.wall_n_fix = this.body.getFixtureList().get(0);
        this.world.bodyEditorLoader.attachFixture(this.body, "lava-east", fd, 0.5f);
        this.wall_e_fix = this.body.getFixtureList().get(1);
        this.world.bodyEditorLoader.attachFixture(this.body, "lava-south", fd, 0.5f);
        this.wall_s_fix = this.body.getFixtureList().get(2);
        this.world.bodyEditorLoader.attachFixture(this.body, "lava-west", fd, 0.5f);
        this.wall_w_fix = this.body.getFixtureList().get(3);
    }

    private void setupLight() {
        float a = MathUtils.random(0.3f, 0.6f);
        Color c = new Color(1.0f, 0.1f, 0.1f, a);
        this.light = new PointLight(this.world.rayHandler, 32, c, 0.78125f, this.getXCenter() / 256.0f, this.getYCenter() / 256.0f);
        this.light.setXray(true);
        this.light.setStaticLight(true);
    }

    private void addParticles() {
        this.lavaParticles = new ParticleActor(new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/lavaflow.p", ParticleEffect.class)), true);
        this.lavaParticles.setPosition(this.getXCenter(), this.getYCenter());
        this.lavaParticles.pfx.start();
        this.world.gameScreen.mainGroup.addActor(this.lavaParticles);
    }

    @Override
    public void update(float delta) {
        this.sleepTimer += delta;
        if (this.sleepTimer > sleepDelay) {
            this.sleepTimer = 0.0f;
            this.checkForSleep();
        }
    }

    private void checkForSleep() {
        Player p = this.world.getPlayer();
        if (p != null) {
            float distToPlayer = Vector2.dst(this.getXCenter(), this.getYCenter(), p.getXPos(), p.getYPos());
            if (distToPlayer > sleepDistance) {
                this.light.setActive(false);
                this.lavaParticles.pfx.allowCompletion();
            } else {
                this.light.setActive(true);
                this.lavaParticles.pfx.start();
            }
        }
    }

    public void autoTile() {
        Tile north = this.world.getTile(this.worldX, this.worldY + 1);
        Tile south = this.world.getTile(this.worldX, this.worldY - 1);
        Tile east = this.world.getTile(this.worldX + 1, this.worldY);
        Tile west = this.world.getTile(this.worldX - 1, this.worldY);
        int S = 0;
        int E = 0;
        int N = 0;
        int W = 0;
        N = this.checkTile(north, 4);
        S = this.checkTile(south, 1);
        int total = S + (E = this.checkTile(east, 2)) + N + (W = this.checkTile(west, 8));
        if (total == 0) {
            total = 15;
        }
        if (total > 0 && total < 15) {
            String fileName = "" + total;
            this.image.setDrawable(this.world.gameScreen.skin.getDrawable("test/lava-" + total));
        } else {
            this.readyToRemove = true;
        }
        this.updatePhysics(total);
    }

    private void updatePhysics(int b) {
        if (b == 1 || b == 4 || b == 5) {
            this.setSensors(true, false, true, false);
        }
        if (b == 2 || b == 8 || b == 10) {
            this.setSensors(false, true, false, true);
        }
        if (b == 12) {
            this.setSensors(true, false, false, true);
        }
        if (b == 6) {
            this.setSensors(true, true, false, false);
        }
        if (b == 9) {
            this.setSensors(false, false, true, true);
        }
        if (b == 3) {
            this.setSensors(false, true, true, false);
        }
    }

    private void setSensors(boolean n, boolean e, boolean s, boolean w) {
        this.wall_n_fix.setSensor(!n);
        this.wall_e_fix.setSensor(!e);
        this.wall_s_fix.setSensor(!s);
        this.wall_w_fix.setSensor(!w);
    }

    private int checkTile(Tile t, int directionBit) {
        if (t != null && t instanceof LavaFlow) {
            return directionBit;
        }
        return 0;
    }

    @Override
    public void destroy() {
        if (this.light != null) {
            this.light.remove();
        }
        if (this.temperatureDealer != null) {
            this.temperatureDealer.readyToRemove = true;
        }
        super.destroy();
    }

    @Override
    public Color getMinimapColor() {
        return Color.RED;
    }
}

