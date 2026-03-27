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
import java.util.ArrayList;

public class TwoByTwoBase
extends BaseModule {
    ProxyTile proxyTile = null;

    public TwoByTwoBase(World world, Chunk chunk, int x, int y) {
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
        this.builderId = "twobytwo";
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
        super.createDrawables("temp-2x2", this.world.gameScreen.floorGroup);
        this.image.setWidth(Tile.TILE_SIZE * 2.0f);
        this.image.setHeight(Tile.TILE_SIZE * 2.0f);
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
        ArrayList<GridPoint2> pCoords = new ArrayList<GridPoint2>();
        pCoords.add(new GridPoint2(1, 0));
        pCoords.add(new GridPoint2(1, 1));
        pCoords.add(new GridPoint2(0, 1));
        if (this.proxyTile == null) {
            for (GridPoint2 gp : pCoords) {
                GridPoint2 cGp2 = World.gridPointPool.obtain();
                World.convertWorldPosToChunkCoord(cGp2, this.worldX + gp.x, this.worldY + gp.y);
                Chunk proxyChunk = this.world.getChunk(cGp2.x, cGp2.y);
                GridPoint2 localPos = World.gridPointPool.obtain();
                World.convertWorldToLocal(localPos, this.worldX + gp.x, this.worldY + gp.y);
                MoonBase.debug("Adding proxy from (" + this.x + ", " + this.y + ")  to -> " + localPos);
                ProxyTile proxyTile = new ProxyTile(this.world, proxyChunk, localPos.x, localPos.y, this);
            }
        }
    }
}

