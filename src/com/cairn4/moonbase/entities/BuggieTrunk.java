/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.ItemStorage;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.entities.Vehicle;
import java.util.ArrayList;

public class BuggieTrunk
implements ItemStorage {
    public Vehicle buggie;
    public int maxItems;

    public BuggieTrunk(Vehicle vehicle, int maxItems) {
        this.maxItems = maxItems;
        this.buggie = vehicle;
    }

    @Override
    public ArrayList<ItemStack> getItemList() {
        return this.buggie.trunkItems;
    }

    @Override
    public boolean isFull() {
        return this.buggie.trunkItems.size() == this.maxItems;
    }

    @Override
    public void addTo(ItemStack newStack) {
        int firstEmptySlot;
        int totalCollected = 0;
        for (ItemStack stack : this.buggie.trunkItems) {
            if (newStack.getAmount() <= 0) break;
            if (!stack.getId().equals(newStack.getId()) || !stack.item.canStackMultiples()) continue;
            MoonBase.log("newStack size: " + newStack.getAmount());
            int stackRoom = stack.getMaxCarry() - stack.getAmount();
            if (stackRoom <= 0) continue;
            int toMove = 0;
            toMove = newStack.getAmount() >= stackRoom ? stackRoom : newStack.getAmount();
            stack.add(toMove);
            newStack.remove(toMove);
            totalCollected += toMove;
        }
        MoonBase.log("collected into existing stacks: " + totalCollected);
        MoonBase.log("newStack size remaining: " + newStack.getAmount());
        for (int i = firstEmptySlot = this.buggie.trunkItems.size(); i < this.maxItems && newStack.getAmount() > 0; ++i) {
            MoonBase.log("new stack has " + newStack.getAmount());
            if (newStack.getAmount() > newStack.getMaxCarry()) {
                ItemStack subStack = new ItemStack(newStack.getId(), newStack.getMaxCarry());
                newStack.remove(newStack.getMaxCarry());
                this.buggie.trunkItems.add(subStack);
                totalCollected += newStack.getMaxCarry();
                continue;
            }
            int toMove = newStack.getAmount();
            ItemStack subStack = new ItemStack(newStack.getId(), toMove);
            if (newStack.item.durability != 0) {
                subStack.item.durability = newStack.item.durability;
            }
            newStack.remove(toMove);
            this.buggie.trunkItems.add(subStack);
            totalCollected += toMove;
        }
        this.trunkChanged();
    }

    private void trunkChanged() {
        MessageManager.getInstance().dispatchMessage(31);
    }

    @Override
    public void addSingle(Item newItem) {
        Item i = Item.copy(newItem);
        this.addTo(new ItemStack(i));
    }

    public void addSingle(ItemStack stack) {
        ItemStack s = new ItemStack(stack.item);
        this.addTo(s);
    }

    @Override
    public void remove(int index) {
        for (int i = this.buggie.trunkItems.size() - 1; i >= 0; --i) {
            if (i != index) continue;
            this.buggie.trunkItems.remove(index);
        }
        this.removeEmpties();
        this.trunkChanged();
    }

    @Override
    public void removeSingle(int index) {
        for (int i = this.buggie.trunkItems.size() - 1; i >= 0; --i) {
            if (i != index) continue;
            this.buggie.trunkItems.get(index).remove(1);
        }
        this.removeEmpties();
        this.trunkChanged();
    }

    @Override
    public void removeEmpties() {
        for (int i = this.buggie.trunkItems.size() - 1; i >= 0; --i) {
            if (this.buggie.trunkItems.get(i).getAmount() > 0) continue;
            this.buggie.trunkItems.remove(i);
        }
    }

    @Override
    public boolean hasSlotAvailable(String itemId) {
        for (ItemStack stack : this.buggie.trunkItems) {
            if (!stack.getId().equals(itemId) || !stack.item.canStackMultiples()) continue;
            return true;
        }
        return !this.isFull();
    }

    public int getSpaceAvailableFor(String itemId) {
        int totalSpace = 0;
        ItemData id = ItemFactory.getItemData(itemId);
        int maxPerSlot = id.maxCarry;
        if (!id.canStackMultiples()) {
            maxPerSlot = 1;
        }
        for (int i = 0; i < this.maxItems; ++i) {
            if (i < this.buggie.trunkItems.size()) {
                ItemStack stack = this.buggie.trunkItems.get(i);
                if (!itemId.equals(stack.getId()) || !stack.item.canStackMultiples()) continue;
                totalSpace += maxPerSlot - stack.getAmount();
                continue;
            }
            totalSpace += maxPerSlot;
        }
        return totalSpace;
    }
}

