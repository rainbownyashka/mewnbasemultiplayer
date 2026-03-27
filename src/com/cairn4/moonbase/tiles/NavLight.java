/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.LightPulseBehavior;

public class NavLight
extends BaseModule {
    private final float maxPowerDraw = 0.0f;

    public NavLight(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        LightPulseBehavior lp = new LightPulseBehavior();
        lp.light = this.light;
        this.behaviorList.add(lp);
        this.blocker = false;
    }

    @Override
    public boolean usesBaseGroup() {
        return false;
    }

    @Override
    public boolean visuallyConnectToConduits() {
        return false;
    }

    @Override
    public boolean blocksWind() {
        return false;
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("navlight");
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "navlight";
    }

    @Override
    public void setupLight() {
        Color c = new Color(1.0f, 0.1f, 0.1f, 0.5f);
        this.light = new PointLight(this.world.rayHandler, 32, c, 0.3125f, this.getXCenter() / 256.0f, this.getYCenter() / 256.0f);
        this.light.setXray(true);
    }

    @Override
    public void setPower(boolean on) {
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
    }
}

