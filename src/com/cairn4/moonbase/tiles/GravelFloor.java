/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;

public class GravelFloor
extends BaseModule {
    public GravelFloor(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.blocker = false;
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("gravel");
    }

    @Override
    public void setupLight() {
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "gravel";
    }

    @Override
    public boolean usesBaseGroup() {
        return false;
    }

    @Override
    public boolean visuallyConnectToConduits() {
        return false;
    }
}

