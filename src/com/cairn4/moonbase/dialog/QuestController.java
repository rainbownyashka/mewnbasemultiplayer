/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.dialog.Quest;
import com.cairn4.moonbase.dialog.QuestData;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.TileFactory;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import com.cairn4.moonbase.worlddata.TileData;
import com.cairn4.moonbase.worlddata.TileOwners;
import java.util.ArrayList;

public class QuestController {
    public ArrayList<Quest> questlist = new ArrayList();
    private static QuestController instance;

    public static QuestController getInstance() {
        if (instance == null) {
            instance = new QuestController();
        }
        return instance;
    }

    private QuestController() {
        this.loadData();
    }

    private void loadData() {
        if (this.questlist.size() == 0) {
            FileHandle fileHandle = Gdx.files.local(MoonBase.coreFolder + "quests.json");
            if (!fileHandle.exists()) {
                Gdx.app.error("MewnBase", "quests.json does not exist");
            } else {
                Gdx.app.log("MewnBase", "QuestController: Reading data");
                Json json = new Json();
                String fileText = fileHandle.readString();
                this.questlist = json.fromJson(QuestData.class, (String)fileText).questList;
                Gdx.app.log("MewnBase", "QuestController: List size: " + this.questlist.size());
            }
        }
    }

    public Quest getQuest(String npcId) {
        for (Quest q : this.questlist) {
            if (!q.npcId.equals(npcId)) continue;
            return q;
        }
        Gdx.app.error("MewnBase", "QuestController: no quest for npc " + npcId);
        return null;
    }

    public void initQuest(Quest quest, Npc npc) {
        if (quest.npcTileCoord != null) {
            float x = (float)quest.npcTileCoord.x * Tile.TILE_SIZE + (float)(npc.getCurrentChunk().chunkX * 10) * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f;
            float y = (float)quest.npcTileCoord.y * Tile.TILE_SIZE + (float)(npc.getCurrentChunk().chunkY * 10) * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f;
            npc.setWorldPos(x, y);
        }
        if (quest.npcTiles != null && !quest.npcTiles.modules.isEmpty()) {
            this.setupQuestTiles(quest, npc);
        }
    }

    public void setupQuestTiles(Quest quest, Npc npc) {
        Chunk c = npc.getCurrentChunk();
        if (c != null) {
            System.out.println("adding npc quest tiles: " + quest.npcTiles.modules.size());
            for (TileData td : quest.npcTiles.modules) {
                Tile t = null;
                try {
                    t = TileFactory.getInstance().createTile(c, td);
                    t.owner = TileOwners.NPCS;
                    if (!(t instanceof BaseModule)) continue;
                    BaseModule b = (BaseModule)t;
                    b.disablePickup(true);
                }
                catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isQuestComplete(Quest quest, Player player) {
        boolean isComplete = false;
        if (quest.fetchItems.size() > 0) {
            boolean playerHasItems = true;
            for (InventoryItemData idd : quest.fetchItems) {
                System.out.println("Checking for " + idd.itemId + " x" + idd.amount);
                if (player.getPlayerInventory().hasResources(idd.itemId, idd.amount)) continue;
                playerHasItems = false;
                break;
            }
            if (playerHasItems) {
                isComplete = true;
            }
        }
        return isComplete;
    }

    public void handleQuestComplete(Quest quest, Player player) {
        if (quest.fetchItems.size() > 0) {
            for (InventoryItemData idd : quest.fetchItems) {
                int index = player.getPlayerInventory().getItemIndex(idd.itemId);
                ItemStack is = player.getPlayerInventory().getItemList().get(index);
                player.getPlayerInventory().consumeItem(is, idd.amount);
            }
        }
    }
}

