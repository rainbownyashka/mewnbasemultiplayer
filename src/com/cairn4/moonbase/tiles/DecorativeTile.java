/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.Tile;

public class DecorativeTile
extends Tile {
    public DecorativeTile(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.createDrawables("fruit", world.gameScreen.floorGroup);
    }

    @Override
    public boolean isFloorTile() {
        return true;
    }
}

