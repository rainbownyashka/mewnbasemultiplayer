/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.cairn4.moonbase.tiles.Airlock;
import com.cairn4.moonbase.tiles.AutoAirlock;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.ItemDropper;
import com.cairn4.moonbase.tiles.ItemPile;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.WallLight;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import java.util.ArrayList;

public class TileData {
    public int x;
    public int y;
    public String name;
    public String itemId;
    public int itemNum;
    public float interactTime;
    public String itemDropperId;
    public ArrayList<InventoryItemData> storageDataList = new ArrayList();
    public String orientation;
    public ArrayList<BehaviorData> behaviorDataList = new ArrayList();
    public ArrayList<String> disasterList = new ArrayList();
    public int owner;

    public TileData() {
    }

    public TileData(Tile t) {
        this.x = t.x;
        this.y = t.y;
        this.name = t.getClass().getName();
        if (t.owner != 0) {
            this.owner = t.owner;
        }
        if (t instanceof ItemPile) {
            ItemPile i = (ItemPile)t;
            this.itemId = i.getItemId();
            this.itemNum = i.getItem().getAmount();
            this.interactTime = i.getInteractDuration();
        }
        if (t instanceof ItemDropper) {
            this.itemDropperId = ((ItemDropper)t).rdData.id;
        }
        if (t instanceof Airlock || t instanceof AutoAirlock) {
            this.orientation = ((Airlock)t).getOuterDoorDirection().toString();
        }
        if (t instanceof WallLight) {
            this.orientation = ((WallLight)t).getOrientation().toString();
        }
        for (Behavior b : t.behaviorList) {
            BehaviorData bd = b.getData();
            if (bd == null) continue;
            this.behaviorDataList.add(bd);
        }
        if (t instanceof BaseModule && ((BaseModule)t).baseDisaster != null) {
            this.disasterList.add(((BaseModule)t).baseDisaster.getClass().getName());
        }
    }
}

