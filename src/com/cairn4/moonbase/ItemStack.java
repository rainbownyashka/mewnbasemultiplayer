/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemActions;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.Player;

public class ItemStack {
    public Item item;
    private int amount;

    public ItemStack() {
    }

    public ItemStack(Item item) {
        this.item = item;
        this.setAmount(1);
    }

    public ItemStack(Item item, int newAmount) {
        this.item = item;
        if (this.item.canStackMultiples()) {
            this.setAmount(newAmount);
        } else {
            this.setAmount(1);
        }
    }

    public ItemStack(String itemId, int newAmount) {
        this.item = ItemFactory.getInstance().createItem(itemId);
        if (this.item.canStackMultiples()) {
            this.setAmount(newAmount);
        } else {
            this.setAmount(newAmount);
        }
    }

    public ItemStack(String itemId, int newAmount, int durability) {
        this.item = ItemFactory.getInstance().createItem(itemId);
        this.setAmount(newAmount);
        this.item.durability = durability;
    }

    public String getId() {
        return this.item.id;
    }

    public String getName() {
        return this.item.getName();
    }

    public String getSprite() {
        return ItemFactory.getItemSprite(this.getId());
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int newAmount) {
        this.amount = newAmount;
    }

    public int getMaxCarry() {
        return this.item.getData().maxCarry;
    }

    public void add(int add) {
        this.amount += add;
    }

    public void remove(int remove) {
        this.amount -= remove;
    }

    public void use(Player player) {
        boolean removeAfterUse = false;
        for (ItemActions action : this.item.actions) {
            Gdx.app.log("MewnBase", "Using item: " + this.item.getName() + ": action: " + action.type + " - " + action.value);
            boolean result = this.item.actionHandler(action, player);
            if (!result) continue;
            removeAfterUse = true;
        }
        if (removeAfterUse) {
            this.remove(1);
        }
    }
}

