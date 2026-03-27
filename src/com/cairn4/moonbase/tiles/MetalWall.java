/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BarrierWall;

public class MetalWall
extends BarrierWall {
    protected AdditiveImage glow;
    protected PointLight light;

    @Override
    protected String getBaseDrawableName() {
        return "metalwall-";
    }

    public MetalWall(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
    }

    @Override
    protected void changeType(BarrierWall.WallTypes newType) {
        this.wallType = newType;
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "metalwall";
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}

