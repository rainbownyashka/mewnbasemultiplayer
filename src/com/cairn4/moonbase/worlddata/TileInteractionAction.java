/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.cairn4.moonbase.worlddata.InventoryItemData;
import com.cairn4.moonbase.worlddata.ItemDropperItemData;
import java.util.ArrayList;

public class TileInteractionAction {
    public String type;
    public String particleEffect;
    public ArrayList<String> spineAnimations;
    public boolean onlyIfNotRegrowing;
    public int minDrop;
    public int maxDrop;
    public ArrayList<ItemDropperItemData> itemList = new ArrayList();
    public ArrayList<InventoryItemData> guaranteedDrops = new ArrayList();
}

