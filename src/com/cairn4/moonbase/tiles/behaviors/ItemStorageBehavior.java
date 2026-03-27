/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.CooldownCallback;
import com.cairn4.moonbase.worlddata.BehaviorData;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import java.util.ArrayList;

public class ItemStorageBehavior
implements Behavior {
    public boolean loaded = false;
    public BaseModule baseModule;
    public int maxItems;
    ArrayList<ItemStack> itemList = new ArrayList();
    public Array<InventoryItemData> storageDataList = new Array();
    public CooldownCallback updateCallback;
    public boolean canDeposit = true;
    // Multiplayer lock for storage UI interactions (-1 = unlocked)
    public int inventoryLockOwnerId = -1;

    public ArrayList<ItemStack> getItemList() {
        return this.itemList;
    }

    public void addTo(ItemStack newStack) {
        int firstEmptySlot;
        int totalCollected = 0;
        for (ItemStack stack : this.itemList) {
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
        MoonBase.debug("storing into existing stacks: " + totalCollected);
        MoonBase.debug("newStack size remaining: " + newStack.getAmount());
        for (int i = firstEmptySlot = this.itemList.size(); i < this.maxItems && newStack.getAmount() > 0; ++i) {
            MoonBase.debug("new stack has " + newStack.getAmount());
            if (newStack.getAmount() > newStack.getMaxCarry()) {
                ItemStack subStack = new ItemStack(newStack.getId(), newStack.getMaxCarry());
                newStack.remove(newStack.getMaxCarry());
                this.itemList.add(subStack);
                totalCollected += newStack.getMaxCarry();
                continue;
            }
            int toMove = newStack.getAmount();
            ItemStack subStack = new ItemStack(newStack.getId(), toMove);
            if (newStack.item.durability != 0) {
                subStack.item.durability = newStack.item.durability;
            }
            newStack.remove(toMove);
            this.itemList.add(subStack);
            totalCollected += toMove;
        }
        this.itemsChanged();
    }

    public int getTotalOfItemId(String itemId) {
        int total = 0;
        for (ItemStack stack : this.itemList) {
            if (!stack.getId().equals(itemId)) continue;
            total += stack.getAmount();
        }
        return total;
    }

    public void addSingle(Item newItem) {
        Item i = Item.copy(newItem);
        this.addTo(new ItemStack(i));
    }

    public boolean isFull() {
        return this.itemList.size() == this.maxItems;
    }

    public boolean hasSlotAvailable(String itemId) {
        for (ItemStack stack : this.itemList) {
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
            if (i < this.itemList.size()) {
                ItemStack stack = this.itemList.get(i);
                if (!itemId.equals(stack.getId()) || !stack.item.canStackMultiples()) continue;
                totalSpace += maxPerSlot - stack.getAmount();
                continue;
            }
            totalSpace += maxPerSlot;
        }
        return totalSpace;
    }

    public void removeSingle(int index) {
        for (int i = this.itemList.size() - 1; i >= 0; --i) {
            if (i != index) continue;
            this.itemList.get(index).remove(1);
        }
        this.removeEmpties();
        this.itemsChanged();
    }

    public void remove(int index) {
        for (int i = this.itemList.size() - 1; i >= 0; --i) {
            if (i != index) continue;
            this.itemList.remove(index);
        }
        this.removeEmpties();
        this.itemsChanged();
    }

    public void removeEmpties() {
        for (int i = this.itemList.size() - 1; i >= 0; --i) {
            if (this.itemList.get(i).getAmount() > 0) continue;
            this.itemList.remove(i);
        }
        this.itemsChanged();
    }

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public void setId(String s) {
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void start() {
        for (InventoryItemData iid : this.storageDataList) {
            int durabilityForThisType = ItemFactory.getDurability(iid.itemId);
            if (durabilityForThisType > 0 && iid.durability == 0) {
                iid.durability = durabilityForThisType;
            }
            try {
                ItemStack newStack = new ItemStack(iid.itemId, iid.amount, iid.durability);
                this.itemList.add(newStack);
            }
            catch (Exception e) {
                MoonBase.error("ItemStorageBehavior: Couldn't create item: " + iid.itemId);
            }
        }
        this.storageDataList.clear();
        this.itemsChanged();
    }

    private void itemsChanged() {
        MessageManager.getInstance().dispatchMessage(32, this.baseModule);
    }

    public ArrayList<InventoryItemData> buildItemDataList() {
        ArrayList<InventoryItemData> list = new ArrayList<InventoryItemData>();
        try {
            for (ItemStack stack : this.itemList) {
                InventoryItemData iid = new InventoryItemData();
                iid.itemId = stack.getId();
                iid.amount = stack.getAmount();
                try { iid.durability = stack.item.durability; } catch (Exception ignored) {}
                list.add(iid);
            }
        } catch (Exception ignored) {}
        return list;
    }

    public void applyItemDataList(ArrayList<InventoryItemData> list) {
        if (list == null) return;
        try {
            this.itemList.clear();
            for (InventoryItemData iid : list) {
                int durabilityForThisType = ItemFactory.getDurability(iid.itemId);
                if (durabilityForThisType > 0 && iid.durability == 0) {
                    iid.durability = durabilityForThisType;
                }
                try {
                    ItemStack newStack = new ItemStack(iid.itemId, iid.amount, iid.durability);
                    this.itemList.add(newStack);
                } catch (Exception ignored) {}
            }
            this.itemsChanged();
        } catch (Exception ignored) {}
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
        this.storageDataList.clear();
        for (ItemStack stack : this.itemList) {
            InventoryItemData iid = new InventoryItemData();
            iid.itemId = stack.getId();
            iid.amount = stack.getAmount();
            iid.durability = stack.item.durability;
            this.storageDataList.add(iid);
        }
        BehaviorData bd = new BehaviorData();
        bd.behaviorClass = this.getClass().getName();
        bd.properties.put("storageDataList", this.storageDataList);
        return bd;
    }

    public void setCanDeposit(boolean enabled) {
        this.canDeposit = enabled;
    }

    public boolean getCanDeposit() {
        return this.canDeposit;
    }
}
