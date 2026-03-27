/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.ProxyTile;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.worlddata.GroundTileData;
import com.cairn4.moonbase.worlddata.TileData;
import java.util.HashMap;

public class ChunkData {
    public int x;
    public int y;
    public HashMap<String, GroundTileData> groundTiles = new HashMap();
    public HashMap<String, TileData> floorTiles = new HashMap();
    public HashMap<String, TileData> tiles = new HashMap();
    public GroundTile.Biomes[] gtBiomeArray = new GroundTile.Biomes[100];
    public boolean[] gtDiscoveryArray = new boolean[100];

    public ChunkData() {
    }

    public ChunkData(Chunk chunk) {
        this.x = chunk.chunkX;
        this.y = chunk.chunkY;
        for (GroundTile gt : chunk.groundTiles.values()) {
            this.groundTiles.put("gt_" + gt.x + "," + gt.y, new GroundTileData(gt));
        }
        for (Tile t : chunk.floorTiles.values()) {
            this.floorTiles.put("t_" + t.x + "," + t.y, new TileData(t));
        }
        for (Tile t : chunk.tiles.values()) {
            if (t instanceof ProxyTile) continue;
            this.tiles.put("t_" + t.x + "," + t.y, new TileData(t));
        }
        this.gtBiomeArray = (GroundTile.Biomes[])chunk.gtBiomeArray.clone();
        this.gtDiscoveryArray = (boolean[])chunk.gtDiscoveryArray.clone();
    }

    public void dispose() {
        this.groundTiles.clear();
        this.groundTiles = null;
        this.floorTiles.clear();
        this.floorTiles = null;
        this.tiles.clear();
        this.tiles = null;
    }
}

