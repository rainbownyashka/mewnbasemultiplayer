/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.Tile;
import java.util.Random;

public class Crater
extends Tile {
    public Crater(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.type = Tile.types.blockingTerrain;
        this.blocker = true;
        this.setupPhysics("rock");
    }

    @Override
    protected void createDrawables() {
        String spriteName = "crater3";
        if (this.chunk.getGroundTile(this.x, this.y).getBiome() == GroundTile.Biomes.ground) {
            int i = new Random().nextInt(5);
            if (i > 3) {
                spriteName = "rocks-big";
            }
        } else {
            int i = new Random().nextInt(2);
            spriteName = i == 1 ? "rocks-big2" : "rocks-big";
        }
        super.createDrawables(spriteName);
        if (Math.random() > 0.5) {
            this.image.setOrigin(1);
            this.image.setScaleX(-1.0f);
        }
    }
}

