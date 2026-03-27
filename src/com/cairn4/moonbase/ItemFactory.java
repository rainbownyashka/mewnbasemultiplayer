/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemDataSet;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.worlddata.CraftingBonusData;
import java.util.ArrayList;

public class ItemFactory {
    private static ItemFactory instance;
    public static ArrayList<ItemData> ITEMDATALIST;

    public static synchronized ItemFactory getInstance() {
        if (instance == null) {
            instance = new ItemFactory();
        }
        return instance;
    }

    private ItemFactory() {
        FileHandle itemDataFile = Gdx.files.local(MoonBase.coreFolder + "items.json");
        if (!itemDataFile.exists()) {
            Gdx.app.error("MewnBase", "ItemFactory: .json file does not exist");
        } else {
            Gdx.app.debug("MewnBase", "ItemFactory: Reading tile data");
            Json json = new Json();
            String fileText = itemDataFile.readString();
            ItemDataSet itemDataSet = json.fromJson(ItemDataSet.class, fileText);
            ITEMDATALIST = itemDataSet.itemDataList;
            Gdx.app.log("MewnBase", "ItemFactory: " + itemDataSet.itemDataList.size() + " items loaded.");
        }
    }

    public ArrayList<String> getResearchUnlocksFor(String techReq) {
        ArrayList<String> unlockedIds = new ArrayList<String>();
        for (ItemData itemData : ITEMDATALIST) {
            if (itemData.techReq == null || !itemData.techReq.equals(techReq)) continue;
            unlockedIds.add(itemData.id);
        }
        return unlockedIds;
    }

    public Item createItem(String id) {
        ItemData itemData = null;
        for (ItemData i : ITEMDATALIST) {
            if (!i.id.equals(id)) continue;
            itemData = i;
            break;
        }
        if (itemData == null) {
            Gdx.app.error("MewnBase", "ItemFactory:createItem : Can't find itemData id: " + id);
        }
        return this.createItem(itemData);
    }

    public Item createItem(ItemData itemData) {
        return new Item(itemData.id, itemData.sprite, itemData.actions, itemData.rotatable, itemData.durability, itemData.durability);
    }

    public static String getItemSprite(String id) {
        String s = "";
        for (ItemData i : ITEMDATALIST) {
            if (!i.id.equals(id)) continue;
            s = i.sprite;
        }
        if (s.length() == 0) {
            s = "missing";
        }
        return s;
    }

    public static String getUseAnimName(String id) {
        String s = "";
        for (ItemData i : ITEMDATALIST) {
            if (!i.id.equals(id)) continue;
            s = i.useAnimName;
        }
        return s;
    }

    public static ItemData getItemData(String id) {
        ItemData data = null;
        for (ItemData i : ITEMDATALIST) {
            if (!i.id.equals(id)) continue;
            data = i;
        }
        if (data == null) {
            MoonBase.error("Can't find item data for " + id);
        }
        return data;
    }

    public ArrayList<CraftingBonusData> getCraftingBonus(String id) {
        for (ItemData i : ITEMDATALIST) {
            if (!i.id.equals(id)) continue;
            return i.craftingBonusItems;
        }
        return null;
    }

    public String getItemTechReq(String id) {
        for (ItemData i : ITEMDATALIST) {
            if (!i.id.equals(id)) continue;
            return i.techReq;
        }
        return null;
    }

    public int getResearchPoints(String id) {
        for (ItemData i : ITEMDATALIST) {
            if (!i.id.equals(id)) continue;
            return i.researchPoints;
        }
        return 0;
    }

    public static int getDurability(String id) {
        for (ItemData i : ITEMDATALIST) {
            if (!i.id.equals(id)) continue;
            return i.durability;
        }
        return 0;
    }

    static {
        ITEMDATALIST = new ArrayList();
    }
}

