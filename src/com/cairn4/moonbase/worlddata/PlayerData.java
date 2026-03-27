/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerStatusEffect;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import com.cairn4.moonbase.worlddata.StatusEffectData;
import java.util.ArrayList;

public class PlayerData {
    public int characterFaceOption;
    public String characterSuitColor;
    public int chunkX;
    public int chunkY;
    public int localX;
    public int localY;
    public int worldX;
    public int worldY;
    public ArrayList<InventoryItemData> inventoryItemDataList = new ArrayList();
    public float air;
    public float suitPower;
    public float hunger;
    public float health;
    public boolean flashlight;
    public ArrayList<StatusEffectData> statusEffeectDataList = new ArrayList();
    public ArrayList<Integer> researchItemsDiscovered = new ArrayList();
    public int suitLevel;
    public Array<GridPoint2> markedMapLocations = new Array();

    public PlayerData() {
    }

    public PlayerData(Player p) {
        this.characterFaceOption = p.characterFaceOption;
        this.characterSuitColor = p.characterSuitColor;
        this.chunkX = p.getCurrentChunk().chunkX;
        this.chunkY = p.getCurrentChunk().chunkY;
        GridPoint2 localPos = new GridPoint2(0, 0);
        localPos = World.convertWorldToLocal(localPos, p.getX(), p.getY());
        this.localX = localPos.x;
        this.localY = localPos.y;
        this.worldX = p.getX();
        this.worldY = p.getY();
        this.air = p.playerStatus.getAir();
        this.suitPower = p.playerStatus.getSuitPower();
        this.hunger = p.playerStatus.getHunger();
        this.health = p.playerStatus.getHealth();
        this.flashlight = p.playerStatus.getFlashlight();
        for (ItemStack stack : p.getPlayerInventory().itemList) {
            InventoryItemData id = new InventoryItemData();
            id.itemId = stack.getId();
            id.amount = stack.getAmount();
            id.durability = stack.item.durability;
            this.inventoryItemDataList.add(id);
        }
        for (PlayerStatusEffect s : p.playerStatus.statusEffects) {
            StatusEffectData sed = s.getData();
            if (sed == null) continue;
            this.statusEffeectDataList.add(sed);
        }
        for (Integer i : p.researchObjectsDiscovered) {
            this.researchItemsDiscovered.add(i);
        }
        this.suitLevel = p.suitLevel;
        this.markedMapLocations = p.markedMapLocations;
    }
}

