/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import com.cairn4.moonbase.worlddata.ItemDropperItemData;
import com.cairn4.moonbase.worlddata.TileInteractionAction;

public class RockPile
extends BaseModule {
    public RockPile(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.setupPhysics("rtg");
        this.interactDuration = 3.0f;
        this.powerDrawRate = 0.0f;
        this.powerGenRate = 0.0f;
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("rock-pile", this.world.gameScreen.mainGroup);
        this.image.setWidth(TILE_SIZE - 8.0f);
    }

    @Override
    public void setupLight() {
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "rockpile";
    }

    @Override
    public boolean usesBaseGroup() {
        return false;
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    public boolean visuallyConnectToConduits() {
        return false;
    }

    @Override
    public boolean canInteract(Player player) {
        return true;
    }

    @Override
    public void finishInteraction(Player player) {
        MoonBase.log("Drop 20 rocks");
        TileInteractionAction action = new TileInteractionAction();
        InventoryItemData iid = new InventoryItemData();
        iid.itemId = "rock";
        iid.amount = 20;
        action.guaranteedDrops.add(iid);
        this.dropItems(action);
        this.readyToRemove = true;
        super.finishInteraction(player);
    }

    public void dropItems(TileInteractionAction action) {
        for (InventoryItemData specialDrop : action.guaranteedDrops) {
            for (int i = 0; i < specialDrop.amount; ++i) {
                Item newItem = ItemFactory.getInstance().createItem(specialDrop.itemId);
                new ItemPickup(this.world, this.chunk, this.getWorldXPos() + TILE_SIZE / 2.0f, this.getWorldYPos() + TILE_SIZE / 2.0f, newItem);
            }
        }
        int quantity = MathUtils.random(action.minDrop, action.maxDrop);
        block2: for (int i = 0; i < quantity; ++i) {
            float base = 0.0f;
            float rand = MathUtils.random();
            int itemIndex = 0;
            for (ItemDropperItemData item : action.itemList) {
                if (rand > base && rand <= item.dropChance + base) {
                    Item newItem = ItemFactory.getInstance().createItem(item.itemId);
                    new ItemPickup(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos(), newItem);
                    continue block2;
                }
                base += action.itemList.get((int)itemIndex).dropChance;
                ++itemIndex;
            }
        }
    }
}

