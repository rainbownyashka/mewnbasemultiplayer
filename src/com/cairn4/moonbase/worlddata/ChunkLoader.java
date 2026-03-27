/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Json;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.GameLoader;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.worlddata.ChunkData;
import com.cairn4.moonbase.worlddata.GroundTileData;
import com.cairn4.moonbase.worlddata.WorldData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChunkLoader {
    private FileHandle file;
    World world;
    ArrayList<GridPoint2> adjacentChunks = new ArrayList();

    public ChunkLoader(World world) {
        this.world = world;
    }

    public void saveChunk(Chunk chunk) {
        ChunkData cd = new ChunkData(chunk);
        this.world.gameScreen.gameLoader.worldData.chunkDataMap.put("chunk_" + chunk.chunkX + "," + chunk.chunkY, cd);
    }

    private void writeFile(ChunkData cd) {
        GameLoader cfr_ignored_0 = this.world.gameScreen.gameLoader;
        Json json = GameLoader.jsonPool.obtain();
        this.file.writeString(json.prettyPrint(cd), false);
        Gdx.app.debug("MoonBase", "ChunkLoader: Writing chunk " + cd.x + "-" + cd.y + " to disk.");
        GameLoader cfr_ignored_1 = this.world.gameScreen.gameLoader;
        GameLoader.jsonPool.free(json);
    }

    public void loadGroundTiles(Chunk c) {
        WorldData wd = this.world.gameScreen.gameLoader.worldData;
        String key = "chunk_" + c.chunkX + "," + c.chunkY;
        if (wd.chunkDataMap.containsKey(key)) {
            ChunkData cd = wd.chunkDataMap.get(key);
            if (cd != null) {
                Gdx.app.debug("MewnBase", "ChunkLoader: loading ground tiles for " + cd.x + ", " + cd.y);
                c.parseGroundTileData(cd);
            } else {
                Gdx.app.log("MewnBase", "Chunk data is null!? " + cd.x + ", " + cd.y);
            }
        } else {
            Gdx.app.error("MewnBase", "ChunkLoader: couldn't find chunk with key: " + key);
        }
    }

    public static ChunkData getChunkData(Chunk c) {
        if (c.groundTiles.isEmpty()) {
            FileHandle file = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/chunk" + c.chunkX + "-" + c.chunkY + ".json");
            if (file.exists()) {
                Json json = new Json();
                String fileText = file.readString();
                return json.fromJson(ChunkData.class, fileText);
            }
            Object var1_1 = null;
        }
        return new ChunkData(c);
    }

    public void changeChunks(int newChunkX, int newChunkY) {
        Gdx.app.debug("MewnBase", "Ground tile pool free: " + this.world.groundTilePool.getFree());
        this.adjacentChunks = Tile.GET_ADJACENT_COORDS(newChunkX, newChunkY);
        for (Map.Entry<String, Chunk> entry : this.world.worldChunks.entrySet()) {
            int cx = entry.getValue().chunkX;
            int cy = entry.getValue().chunkY;
            GridPoint2 chunkCoord = World.getGridPointFromPoolAndSet(cx, cy);
            boolean isAdjacent = false;
            for (GridPoint2 gp : this.adjacentChunks) {
                if (!gp.equals(chunkCoord)) continue;
                isAdjacent = true;
                break;
            }
            if (!isAdjacent && entry.getValue().isVisible()) {
                this.saveChunk(entry.getValue());
                entry.getValue().hide();
                entry.getValue().unloadGroundTiles();
            }
            World.gridPointPool.free(chunkCoord);
            chunkCoord = null;
        }
        for (GridPoint2 gp : this.adjacentChunks) {
            String key = "" + gp.x + "," + gp.y;
            if (this.world.worldChunks.containsKey(key)) {
                if (this.world.worldChunks.get(key).isVisible()) continue;
                Chunk c = this.world.worldChunks.get(key);
                this.loadGroundTiles(c);
                c.addEnvironmentFx();
                c.show();
                continue;
            }
            Gdx.app.log("MewnBase", "ChunkLoader: Creating new chunk: " + key);
            Chunk chunk = this.world.createChunk(gp.x, gp.y);
        }
        for (GridPoint2 gp : this.adjacentChunks) {
            String key = "" + gp.x + "," + gp.y;
            this.world.worldChunks.get(key).updateAutoTile();
        }
        this.adjacentChunks = null;
    }

    public void removeChunk(int chunkX, int chunkY) {
        String key = chunkX + "-" + chunkY;
        Gdx.app.debug("MoonBase", "ChunkLoader: Chunk removed " + key);
        this.world.worldChunks.remove(key);
    }

    public void restoreAllAvailable() {
    }

    private void quickSaveChunk(Chunk chunk) {
        ChunkData cd_GroundTiles = ChunkLoader.getChunkData(chunk);
        ChunkData cd = new ChunkData(chunk);
        cd.groundTiles.putAll(cd_GroundTiles.groundTiles);
        cd_GroundTiles.dispose();
        cd_GroundTiles = null;
        this.file = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/chunk" + chunk.chunkX + "-" + chunk.chunkY + ".json");
        this.writeFile(cd);
        cd.dispose();
        cd = null;
    }

    public ChunkData getQuickSaveData(Chunk chunk) {
        ChunkData cd_GroundTiles = ChunkLoader.getChunkData(chunk);
        ChunkData cd = new ChunkData(chunk);
        cd.groundTiles.putAll(cd_GroundTiles.groundTiles);
        return cd;
    }

    public HashMap<String, Chunk> loadAllChunks(WorldData wd) {
        HashMap<String, Chunk> allChunks = new HashMap<String, Chunk>();
        for (ChunkData cd : wd.chunkDataMap.values()) {
            Chunk c = new Chunk(this.world, cd.x, cd.y, true);
            if (cd.gtBiomeArray[0] == null) {
                Gdx.app.log("MewnBase", "ChunkLoader: create the new arrays for older chunk: " + cd.x + ", " + cd.y);
                for (GroundTileData gtd : cd.groundTiles.values()) {
                    c.gtDiscoveryArray[gtd.y * 10 + gtd.x] = gtd.disovered;
                    c.gtBiomeArray[gtd.y * 10 + gtd.x] = GroundTile.Biomes.valueOf(gtd.biome);
                }
            } else {
                c.gtBiomeArray = cd.gtBiomeArray;
                c.gtDiscoveryArray = cd.gtDiscoveryArray;
            }
            c.parseGroundTileData(cd);
            c.parseFloorTileData(cd);
            c.parseTileData(cd);
            c.setMapDirty(false);
            c.hide();
            allChunks.put("" + c.chunkX + "," + c.chunkY, c);
        }
        return allChunks;
    }

    public int[] getMinMaxChunkCoords() {
        int minX = 999999999;
        int minY = 999999999;
        int maxX = -999999999;
        int maxY = -999999999;
        for (Map.Entry<String, Chunk> entry : this.world.worldChunks.entrySet()) {
            if (entry.getValue().chunkX < minX) {
                minX = entry.getValue().chunkX;
            }
            if (entry.getValue().chunkY < minY) {
                minY = entry.getValue().chunkY;
            }
            if (entry.getValue().chunkX > maxX) {
                maxX = entry.getValue().chunkX;
            }
            if (entry.getValue().chunkY <= maxY) continue;
            maxY = entry.getValue().chunkY;
        }
        return new int[]{minX, minY, maxX, maxY};
    }
}

