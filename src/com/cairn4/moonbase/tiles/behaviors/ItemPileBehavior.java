/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.ItemPile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class ItemPileBehavior
implements Behavior {
    public ItemPile itemPile;
    public String behaviorId;
    public boolean loaded;
    public String itemId;
    public int itemNum;
    public int itemDurability;
    public int maxDurability;

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = true;
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public void setId(String s) {
        this.behaviorId = s;
    }

    @Override
    public String getId() {
        return this.behaviorId;
    }

    @Override
    public void start() {
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void interact(Player player) {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public BehaviorData getData() {
        BehaviorData bd = new BehaviorData();
        bd.behaviorClass = this.getClass().getName();
        this.itemId = this.itemPile.getItemId();
        this.itemNum = this.itemPile.getItem().getAmount();
        this.itemDurability = this.itemPile.getItem().item.durability;
        this.maxDurability = this.itemPile.getItem().item.maxDurability;
        bd.properties.put("itemId", this.itemId);
        bd.properties.put("itemNum", this.itemNum);
        bd.properties.put("itemDurability", this.itemDurability);
        bd.properties.put("maxDurability", this.maxDurability);
        return bd;
    }
}

