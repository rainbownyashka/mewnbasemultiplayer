/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Json;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.ItemDropper;
import com.cairn4.moonbase.tiles.ResearchObject;
import com.cairn4.moonbase.worlddata.ItemDropperData;
import com.cairn4.moonbase.worlddata.ItemDropperSet;
import com.cairn4.moonbase.worlddata.MiningResourceData;
import java.util.ArrayList;

public class ItemDropperFactory {
    private static ItemDropperFactory instance;
    private FileHandle file;
    public ArrayList<ItemDropperData> itemDropperDataList = new ArrayList();
    public ArrayList<MiningResourceData> miningResources = new ArrayList();

    private ItemDropperFactory() {
        this.load();
    }

    public static synchronized ItemDropperFactory getInstance() {
        if (instance == null) {
            instance = new ItemDropperFactory();
        }
        return instance;
    }

    public void load() {
        this.file = Gdx.files.local(MoonBase.coreFolder + "resource_tiles.json");
        if (this.file.exists()) {
            Gdx.app.log("MoonBase", "ItemDropperFactory: Loading Item Droppers");
            Json json = new Json();
            String fileText = this.file.readString();
            ItemDropperSet ids = json.fromJson(ItemDropperSet.class, fileText);
            this.itemDropperDataList = ids.itemDropperList;
            this.miningResources = ids.miningResourceData;
        } else {
            Gdx.app.error("MoonBase", "ItemDropperFactory: Item dropper json file not found!");
        }
    }

    public ItemDropperData getItemDropperData(String id) {
        for (ItemDropperData idd : this.itemDropperDataList) {
            if (!idd.id.equals(id)) continue;
            return idd;
        }
        Gdx.app.error("MoonBase", "ItemDropperFactory: No such ItemDropperData id (" + id + ")");
        return null;
    }

    public ItemDropper newDropper(World world, int worldX, int worldY, String dropperId) {
        int chunkX = Chunk.getChunkX(worldX);
        int chunkY = Chunk.getChunkY(worldY);
        Chunk chunk = world.getChunk(chunkX, chunkY);
        GridPoint2 localTile = World.convertWorldToLocal(World.gridPointPool.obtain(), worldX, worldY);
        return this.newDropper(world, chunk, localTile.x, localTile.y, dropperId);
    }

    public ItemDropper newDropper(World world, Chunk chunk, int x, int y, String dropperId) {
        ItemDropperData idd = this.getItemDropperData(dropperId);
        return new ItemDropper(world, chunk, x, y, idd);
    }

    public ResearchObject newResearchDropper(World world, Chunk chunk, int x, int y, String dropperId) {
        ItemDropperData idd = this.getItemDropperData(dropperId);
        return new ResearchObject(world, chunk, x, y, idd);
    }
}

