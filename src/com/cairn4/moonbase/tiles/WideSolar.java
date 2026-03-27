/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.ProxyTile;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;

public class WideSolar
extends BaseModule {
    ProxyTile proxyTile = null;

    public WideSolar(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.lightOnColor = new Color(0.1f, 0.2f, 1.0f, 0.3f);
        this.setupPhysics("solar");
        this.powerDrawRate = 3.0f;
        this.powerGenRate = 0.0f;
        this.updateBases();
        this.light.setColor(this.lightOnColor);
        this.light.setXray(true);
        chunk.tilesThatNeedProxies.add(this);
    }

    @Override
    public void startBehaviors() {
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "wide-solar";
    }

    @Override
    public boolean usesComms() {
        return true;
    }

    @Override
    public boolean blocksWind() {
        return false;
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("temp-2x1", this.world.gameScreen.floorGroup);
        this.image.setWidth(Tile.TILE_SIZE * 2.0f);
        this.group.setUserObject(Float.valueOf(this.getYCenter()));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
    }

    @Override
    public void addProxies() {
        if (this.proxyTile == null) {
            GridPoint2 cGp2 = World.gridPointPool.obtain();
            World.convertWorldPosToChunkCoord(cGp2, this.worldX + 1, this.worldY);
            Chunk proxyChunk = this.world.getChunk(cGp2.x, cGp2.y);
            GridPoint2 localPos = World.gridPointPool.obtain();
            World.convertWorldToLocal(localPos, this.worldX + 1, this.worldY);
            MoonBase.log("Adding proxy from (" + this.x + ", " + this.y + ")  to -> " + localPos);
            ProxyTile proxyTile = new ProxyTile(this.world, proxyChunk, localPos.x, localPos.y, this);
        }
    }
}

