/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.CooldownCallback;
import com.cairn4.moonbase.tiles.behaviors.CooldownTimer;

public class LightningCollector
extends BaseModule {
    public static int TILERADIUS = 10;
    public boolean consumedLightning = true;
    private float MAXPOWER = 140.0f;
    private CooldownTimer zappedCooldown = new CooldownTimer(new CooldownCallback(){

        @Override
        public void callback() {
            LightningCollector.this.powerGenRate = 0.0f;
        }
    });

    public LightningCollector(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.behaviorList.add(this.zappedCooldown);
        this.zappedCooldown.length = 1.0f;
        this.zappedCooldown.start();
        this.setupPhysics("battery");
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "lightningcollector";
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("lightningcollector", this.world.gameScreen.mainGroup);
        this.image.setSize(TILE_SIZE, 415.0f * SCALE);
        this.image.setOrigin(1);
        this.image.setPosition(0.0f, 0.0f);
        this.group.setUserObject(Float.valueOf(this.getYCenter()));
    }

    public void hitByLightning() {
        System.out.println("Hit collector!");
        this.powerGenRate = this.MAXPOWER;
        this.zappedCooldown.trigger();
    }

    @Override
    public void update(float delta) {
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        super.update(delta);
    }
}

