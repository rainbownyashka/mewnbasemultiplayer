/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.RecipieRequirement;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.ItemPile;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.BuildingPlacementCursor;
import com.cairn4.moonbase.ui.HudNotificationData;
import com.cairn4.moonbase.ui.Localization;
import java.util.ArrayList;

public class PlayerInventory {
    public static final int MAX_ITEMS = 12;
    private Player player;
    public ArrayList<ItemStack> itemList = new ArrayList();
    private int selectedIndex;
    public BuildingPlacementCursor.ORIENTATIONS placementOrientation;

    public ArrayList<ItemStack> getItemList() {
        return this.itemList;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public PlayerInventory(Player player) {
        this.player = player;
        this.setPlacementRotation(BuildingPlacementCursor.ORIENTATIONS.west);
    }

    public Player getPlayer() {
        return this.player;
    }

    public void addItemNoOrg(ItemStack newStack) {
        this.itemList.add(newStack);
        this.player.inventoryUpdate();
    }

    public void add(ItemStack newStack, boolean hudNotify) {
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
        MoonBase.log("collected into existing stacks: " + totalCollected);
        MoonBase.log("newStack size remaining: " + newStack.getAmount());
        for (int i = firstEmptySlot = this.itemList.size(); i < 12 && newStack.getAmount() > 0; ++i) {
            MoonBase.log("new stack has " + newStack.getAmount());
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
        this.player.inventoryUpdate();
        if (this.player.world.gameScreen.hud != null && hudNotify) {
            this.player.world.gameScreen.hud.hudNotifications.newPickupMessage(newStack.item, totalCollected, null);
            MoonBase.log("Added " + newStack.getId() + " (" + totalCollected + ") to player inventory");
        }
    }

    public boolean isFull() {
        return this.itemList.size() == 12;
    }

    public boolean hasSlotAvailable(String itemId) {
        for (ItemStack stack : this.itemList) {
            if (!stack.getId().equals(itemId) || !stack.item.canStackMultiples() || stack.getAmount() >= stack.getMaxCarry()) continue;
            return true;
        }
        return !this.isFull();
    }

    public boolean hasSlotAvailableAfterCrafting(ItemData itemData) {
        boolean result = true;
        for (RecipieRequirement r : itemData.requires) {
            boolean wouldMakeRoomForItem = true;
            for (ItemStack stack : this.itemList) {
                if (!r.id.equals(stack.getId()) || stack.getAmount() <= r.quantity) continue;
                wouldMakeRoomForItem = false;
            }
            result = wouldMakeRoomForItem;
            if (!result) continue;
            break;
        }
        return result;
    }

    public void setSelection(int index) {
        this.selectedIndex = index;
        this.player.inventorySelectionChange();
    }

    public void changeSelection(int dir) {
        if (this.itemList.size() > 0) {
            if (dir == 1) {
                ++this.selectedIndex;
                if (this.selectedIndex >= this.itemList.size()) {
                    this.selectedIndex = 0;
                }
            } else {
                --this.selectedIndex;
                if (this.selectedIndex < 0) {
                    this.selectedIndex = this.itemList.size() - 1;
                }
                if (this.selectedIndex >= this.itemList.size()) {
                    this.selectedIndex = this.itemList.size() - 1;
                }
            }
            this.player.inventorySelectionChange();
            MoonBase.debug("PlayerInventory: selection is now : " + this.selectedIndex);
        } else {
            MoonBase.debug("PlayerInventory: Inventory is empty, setting index to 0");
            this.selectedIndex = 0;
        }
        this.equipItem();
    }

    public ItemStack getSelectedItem() {
        if (this.itemList.size() > 0 && this.selectedIndex < this.itemList.size()) {
            return this.itemList.get(this.selectedIndex);
        }
        return null;
    }

    @Deprecated
    public Item getSingleSelectedItem() {
        ItemStack selected = this.itemList.get(this.selectedIndex);
        Item single = ItemFactory.getInstance().createItem(selected.getId());
        return single;
    }

    public void consumeItem(ItemStack stack, int amount) {
        int index = this.itemList.indexOf(stack);
        this.itemList.get(index).remove(amount);
        this.removeEmpties();
        this.player.inventoryUpdate();
    }

    public void removeItem(int index) {
        for (int i = this.itemList.size() - 1; i >= 0; --i) {
            if (index != i) continue;
            this.itemList.remove(index);
        }
        this.adjustSelectionAfterRemoval(index);
        this.player.inventoryUpdate();
    }

    public void removeAmountOfItemId(int removeAmount, String id) {
        int remaining = removeAmount;
        for (ItemStack s : this.itemList) {
            if (remaining <= 0) break;
            if (!s.getId().equals(id)) continue;
            if (s.getAmount() >= remaining) {
                s.remove(remaining);
                break;
            }
            remaining -= s.getAmount();
            s.remove(s.getAmount());
        }
        this.removeEmpties();
        this.player.inventoryUpdate();
    }

    public void removeEmpties() {
        for (int i = this.itemList.size() - 1; i >= 0; --i) {
            if (this.itemList.get(i).getAmount() > 0) continue;
            this.itemList.remove(i);
            this.adjustSelectionAfterRemoval(i);
        }
    }

    private void adjustSelectionAfterRemoval(int indexRemoved) {
        if (indexRemoved < this.selectedIndex && this.selectedIndex != 0) {
            this.changeSelection(-1);
        } else if (indexRemoved != this.selectedIndex || this.selectedIndex == 0) {
            // empty if block
        }
        if (this.selectedIndex >= this.itemList.size() && this.itemList.size() > 0) {
            this.selectedIndex = this.itemList.size() - 1;
        }
        if (this.itemList.size() == 0 && this.selectedIndex > 0) {
            this.selectedIndex = 0;
        }
    }

    public void dropItem() {
        if (!this.itemList.isEmpty()) {
            ItemStack stack = this.getSelectedItem(); 
            GridPoint2 localPos = new GridPoint2(0, 0);
            localPos = World.convertWorldToLocal(localPos, this.player.x, this.player.y);
            if (this.player.world.isTileEmpty(this.player.x, this.player.y)) {
                // create pile locally
                this.dropItemOnEmptyTile(localPos, stack);
                try {
                    int wx = (int)this.player.x;
                    int wy = (int)this.player.y;
                    int amt = 1;
                    try { amt = stack.getAmount(); } catch (Exception ignored) {}
                    String payload = "ITEM_DROP:" + wx + ":" + wy + ":" + stack.getId() + ":" + amt;
                    com.cairn4.moonbase.NetworkHelper.sendPayload(this.player.world.gameScreen, payload);
                } catch (Exception ignored) {}
            } else if (stack.item.canStackMultiples()) {
                Tile tile = this.player.world.getTile(this.player.x, this.player.y);
                if (tile.type == Tile.types.pile) {
                    ItemPile itemPile = (ItemPile)tile;
                    if (itemPile.getItemId().equals(stack.getId())) {
                        int maxStackAmount = stack.getMaxCarry();
                        if (itemPile.getItem().getAmount() < maxStackAmount) {
                            itemPile.addTo(new ItemStack(this.getSingleSelectedItem()));
                            MoonBase.debug("PlayerInventory: Item dropped into existing pile");
                            stack.remove(1);
                            this.dropItemSoundFx();
                        }
                    } else {
                        MoonBase.debug("PlayerInventory: Item pile here doesn't match");
                    }
                } else {
                    MoonBase.debug("PlayerInventory: Tile already here, can't drop item");
                }
            }
            this.removeEmpties();
            this.player.inventoryUpdate();
        }
    }

    public void dropStack() {
        if (this.itemList.size() > 0) {
            ItemStack i = this.getSelectedItem();
            if (i != null) {
                if (this.getEquippedItem() == i) {
                    this.equipItem();
                }
                GridPoint2 localPos = new GridPoint2(0, 0);
                localPos = World.convertWorldToLocal(localPos, this.player.x, this.player.y);
                if (this.player.world.isTileEmpty(this.player.x, this.player.y)) {
                    this.removeItem(this.getSelectedIndex());
                    new ItemPile(this.player.world, this.player.getCurrentChunk(), localPos.x, localPos.y, i);
                    this.dropItemSoundFx();
                    try {
                        int wx = (int)this.player.x;
                        int wy = (int)this.player.y;
                        int amt = 1; try { amt = i.getAmount(); } catch (Exception ignored) {}
                        String payload = "ITEM_DROP:" + wx + ":" + wy + ":" + i.getId() + ":" + amt;
                        com.cairn4.moonbase.NetworkHelper.sendPayload(this.player.world.gameScreen, payload);
                    } catch (Exception ignored) {}
                                try {
                                    int wx = (int)this.player.x;
                                    int wy = (int)this.player.y;
                                    int amt = 1; try { amt = i.getAmount(); } catch (Exception ignored) {}
                                    String payload = "ITEM_DROP:" + wx + ":" + wy + ":" + i.getId() + ":" + amt;
                                    com.cairn4.moonbase.NetworkHelper.sendPayload(this.player.world.gameScreen, payload);
                                } catch (Exception ignored) {}
                    MoonBase.debug("PlayerInventory: Space clear, dropping item into a new pile");
                } else if (i.item.canStackMultiples()) {
                    Tile tile = this.player.world.getTile(this.player.x, this.player.y);
                    if (tile.type == Tile.types.pile) {
                        ItemPile itemPile = (ItemPile)tile;
                        if (itemPile.getItemId().equals(i.getId())) {
                            int maxStackAmount = i.getMaxCarry();
                            int spaceAvailable = maxStackAmount - itemPile.getItem().getAmount();
                            if (itemPile.getItem().getAmount() < maxStackAmount) {
                                if (i.getAmount() > spaceAvailable) {
                                    int amountToMove = spaceAvailable;
                                    i.remove(amountToMove);
                                    itemPile.addTo(amountToMove);
                                } else {
                                    itemPile.addTo(i);
                                    this.removeItem(this.getSelectedIndex());
                                }
                                this.dropItemSoundFx();
                                    try {
                                        int wx = (int)this.player.x;
                                        int wy = (int)this.player.y;
                                        String payload = "ITEM_DROP:" + wx + ":" + wy + ":" + i.getId();
                                        com.cairn4.moonbase.NetworkHelper.sendPayload(this.player.world.gameScreen, payload);
                                    } catch (Exception ignored) {}
                                MoonBase.debug("PlayerInventory: Item dropped into existing pile");
                            }
                        } else {
                            Gdx.app.debug("MewnBase", "PlayerInventory: Item pile here doesn't match");
                        }
                    } else {
                        MoonBase.debug("PlayerInventory: Tile already here, can't drop item");
                    }
                }
                this.removeEmpties();
                this.player.inventoryUpdate();
            } else {
                MoonBase.debug("Can't drop null item");
            }
        }
    }

    private void dropItemOnEmptyTile(GridPoint2 localPos, ItemStack stack) {
        Item singleItem = this.getSingleSelectedItem();
        singleItem.durability = stack.item.durability;
        stack.remove(1);
        new ItemPile(this.player.world, this.player.getCurrentChunk(), localPos.x, localPos.y, new ItemStack(singleItem));
        this.dropItemSoundFx();
        Gdx.app.debug("MewnBase", "PlayerInventory: Space clear, dropping item into a new pile");
    }

    private void findAdjacentTileToDrop() {
        ItemStack stack = this.getSelectedItem();
        ArrayList<GridPoint2> adjacentTiles = Tile.GET_ADJACENT_COORDS(this.player.x, this.player.y, false);
        GridPoint2 best = null;
        for (GridPoint2 gp : adjacentTiles) {
            if (this.player.world.isTileEmpty(gp.x, gp.y)) {
                if (best != null) continue;
                best = gp;
                continue;
            }
            if (this.player.world.getTile((int)gp.x, (int)gp.y).type != Tile.types.pile || !stack.item.canStackMultiples() || !((ItemPile)this.player.world.getTile(gp.x, gp.y)).getItemId().equals(stack.getId())) continue;
            best = gp;
            break;
        }
        if (best == null || this.player.world.isTileEmpty(best.x, best.y)) {
            // empty if block
        }
    }

    private void dropItemSoundFx() {
        Audio.getInstance().playSound("sounds/pop.mp3", 0.2f, 0.6f + MathUtils.random(0.0f, 0.1f));
    }

    public void equipItem() {
        if (this.getSelectedItem() != null) {
            Gdx.app.debug("MewnBase", "PlayerInventory: Equip item: " + this.getSelectedItem().getId());
        }
        this.setPlacementRotation(BuildingPlacementCursor.ORIENTATIONS.west);
        this.player.inventoryUpdate();
    }

    public void useItem() {
        ItemStack stack = this.getSelectedItem();
        if (stack != null) {
            stack.use(this.player);
            this.removeEmpties();
            this.player.inventoryUpdate();
        }
    }

    public ItemStack getEquippedItem() {
        if (this.selectedIndex < this.itemList.size()) {
            return this.itemList.get(this.selectedIndex);
        }
        return null;
    }

    public String getEquippedItemId() {
        if (this.getEquippedItem() != null) {
            return this.getEquippedItem().getId();
        }
        return "";
    }

    public boolean hasResources(String itemId, int quantity) {
        for (ItemStack stack : this.itemList) {
            if (!stack.getId().equals(itemId) || stack.getAmount() < quantity) continue;
            return true;
        }
        return false;
    }

    public int getAmountOfItem(String itemId) {
        int total = 0;
        for (ItemStack stack : this.itemList) {
            if (!stack.getId().equals(itemId)) continue;
            total += stack.getAmount();
        }
        return total;
    }

    public void setPlacementRotation(BuildingPlacementCursor.ORIENTATIONS o) {
        this.placementOrientation = o;
        this.player.buildingRotationUpdate();
    }

    public void rotatePlacementOrientation() {
        if (this.getEquippedItem() != null) {
            if (this.getEquippedItem().item.getRotatable()) {
                switch (this.placementOrientation) {
                    case north: {
                        this.placementOrientation = BuildingPlacementCursor.ORIENTATIONS.east;
                        break;
                    }
                    case south: {
                        this.placementOrientation = BuildingPlacementCursor.ORIENTATIONS.west;
                        break;
                    }
                    case east: {
                        this.placementOrientation = BuildingPlacementCursor.ORIENTATIONS.south;
                        break;
                    }
                    case west: {
                        this.placementOrientation = BuildingPlacementCursor.ORIENTATIONS.north;
                    }
                }
                this.player.buildingRotationUpdate();
            } else {
                this.placementOrientation = BuildingPlacementCursor.ORIENTATIONS.west;
            }
        }
    }

    public void swapSlots(int moveObjectFromIndex, int moveToIndex) {
        if (moveObjectFromIndex != moveToIndex) {
            ItemStack i1 = this.itemList.get(moveObjectFromIndex);
            ItemStack i2 = this.itemList.get(moveToIndex);
            boolean stackable = i1.item.canStackMultiples();
            if (i1.getId().equals(i2.getId()) && stackable) {
                int targetStackRoom = i2.getMaxCarry() - i2.getAmount();
                if (targetStackRoom > 0) {
                    if (targetStackRoom >= i1.getAmount()) {
                        i2.add(i1.getAmount());
                        i1.remove(i1.getAmount());
                    } else {
                        int moving = targetStackRoom;
                        i1.remove(moving);
                        i2.add(moving);
                    }
                }
            } else {
                this.itemList.set(moveToIndex, i1);
                this.itemList.set(moveObjectFromIndex, i2);
            }
            this.removeEmpties();
            this.player.inventoryUpdate();
            if (moveObjectFromIndex == this.getSelectedIndex()) {
                this.setSelection(moveToIndex);
            }
        }
    }

    public int getItemIndex(String itemId) {
        int index = 0;
        for (ItemStack stack : this.itemList) {
            if (stack.getId().equals(itemId)) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    public void reduceToolDurability(int i) {
        Item item;
        ItemStack itemStack = this.getEquippedItem();
        if (itemStack != null && (item = itemStack.item) != null) {
            MoonBase.log("PlayerInventory: reduce durability: " + this.getEquippedItem().item.durability + " / " + item.getData().durability);
            if (item.getData().durability > 0) {
                this.getEquippedItem().item.reduceDurability(1);
                this.player.inventoryUpdate();
            }
        }
    }

    public void checkToolDurability() {
        Item item;
        ItemStack itemStack = this.getEquippedItem();
        if (itemStack != null && (item = itemStack.item) != null && this.getEquippedItem().item.durability <= 0) {
            MoonBase.log("Tool broke");
            this.getEquippedItem().remove(1);
            this.player.playerAnimController.toolBrokeAnim();
            this.sendToolBrokeNotification(item);
            Audio.getInstance().playSound("sounds/tool-break.ogg", 0.5f);
            if (this.getEquippedItem().getAmount() > 0) {
                this.getEquippedItem().item.resetDurability();
            }
            this.removeEmpties();
            this.player.clearInteractingTile(true);
        }
    }

    private void sendToolBrokeNotification(Item item) {
        HudNotificationData msg = new HudNotificationData();
        msg.icon = item.getSprite();
        msg.message = Localization.format("item_broke", this.getEquippedItem().getName());
        msg.textColor = Vars.WARNING_RED;
        MessageManager.getInstance().dispatchMessage(3, msg);
    }

    public boolean hasResources(ItemData itemData) {
        boolean result = true;
        if (MoonBase.getCurrentMission().missionType == Mission.MissionTypes.creative) {
            return true;
        }
        if (this.getItemList().size() > 0) {
            for (RecipieRequirement r : itemData.requires) {
                boolean hasItem = false;
                for (ItemStack stack : this.getItemList()) {
                    if (!r.id.equals(stack.getId())) continue;
                    if (r.quantity <= stack.getAmount()) {
                        hasItem = true;
                        break;
                    }
                    result = false;
                    break;
                }
                if (hasItem) continue;
                return false;
            }
        } else {
            result = false;
        }
        return result;
    }

    public void spendResources(ItemData itemData) {
        Array<ItemStack> spentItemBuffer = new Array<ItemStack>();
        System.out.println("Sending resources...");
        block0: for (RecipieRequirement r : itemData.requires) {
            for (ItemStack stack : this.getItemList()) {
                if (!r.id.equals(stack.getId())) continue;
                ItemStack bufferStack = new ItemStack(r.id, r.quantity);
                spentItemBuffer.add(bufferStack);
                this.consumeItem(stack, r.quantity);
                continue block0;
            }
        }
        this.removeEmpties();
        this.player.inventoryUpdate();
    }

    public boolean hasWeaponEquipped() {
        if (this.getEquippedItem() != null) {
            return ItemFactory.getItemData((String)this.getEquippedItem().getId()).minMeleeDamage > 0;
        }
        return false;
    }

    public int getSpaceAvailableFor(String itemId) {
        int totalSpace = 0;
        ItemData id = ItemFactory.getItemData(itemId);
        int maxPerSlot = id.maxCarry;
        if (!id.canStackMultiples()) {
            maxPerSlot = 1;
        }
        for (int i = 0; i < 12; ++i) {
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
}

