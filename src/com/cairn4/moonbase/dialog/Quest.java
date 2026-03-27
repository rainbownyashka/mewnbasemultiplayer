/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.dialog;

import com.badlogic.gdx.math.GridPoint2;
import com.cairn4.moonbase.dialog.NpcTiles;
import com.cairn4.moonbase.dialog.QuestTileRequirement;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import java.util.ArrayList;

public class Quest {
    public String questId;
    public String npcId;
    public boolean complete = false;
    public ArrayList<InventoryItemData> fetchItems = new ArrayList();
    public ArrayList<InventoryItemData> rewardItems = new ArrayList();
    public boolean rewardBonus;
    public GridPoint2 npcTileCoord;
    public NpcTiles npcTiles;
    public QuestTileRequirement tileRequirement;
}

