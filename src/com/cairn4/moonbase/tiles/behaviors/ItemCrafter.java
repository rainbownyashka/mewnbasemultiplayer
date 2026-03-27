/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.RecipieRequirement;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Observable;

public class ItemCrafter
extends Observable
implements Behavior {
    public boolean loaded = false;
    public BaseModule baseModule;
    public String crafterId;
    public boolean requirePowerToCraft;
    public boolean requireOxygenToCraft;
    public boolean requireWaterToCraft;
    public String soundFinishBuilding = "";
    public float soundFinishVolume = 0.6f;
    public ArrayList<String> buildables = new ArrayList();
    public boolean building;
    public Array<ItemStack> itemsToCollect = new Array();
    public Player player;
    public PlayerInventory playerInventory;
    public Array<Array<ItemStack>> spentItemBufferList = new Array();
    public int buildQueueSizeLimit = 4;
    public Array<ItemData> buildQueue = new Array();
    public float buildTimer;
    private Image buildFinishedIcon;
    public String lastSelectedItemId = "";
    public float buildSpeedMultiplier = 1.0f;

    public boolean isBuilding() {
        return this.building;
    }

    public ItemData getCurrentBuildItem() {
        return this.buildQueue.get(0);
    }

    public void setupBuildables(String crafterId) {
        this.crafterId = crafterId;
        this.buildables.clear();
        for (ItemData id : ItemFactory.ITEMDATALIST) {
            for (String s : id.craftedIn) {
                if (!s.equals(this.crafterId)) continue;
                this.addBuildable(id.id);
            }
        }
        Gdx.app.log("MewnBase", "ItemCrafter: " + this.crafterId + " (" + this.buildables.size() + ")");
    }

    @Override
    public BehaviorData getData() {
        BehaviorData bd = new BehaviorData();
        bd.behaviorClass = this.getClass().getName();
        bd.properties.put("building", this.building);
        bd.properties.put("buildTimer", Float.valueOf(this.buildTimer));
        bd.properties.put("buildQueue", this.buildQueue);
        bd.properties.put("spentItemBufferList", this.spentItemBufferList);
        bd.properties.put("itemsToCollect", this.itemsToCollect);
        bd.properties.put("lastSelectedItemId", this.lastSelectedItemId);
        return bd;
    }

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
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
        if (this.itemsToCollect.size > 0) {
            this.showFinishedIcon(true);
        }
    }

    protected boolean hasBaseRequirementsForBuilding() {
        boolean hasReq = true;
        if (this.requireOxygenToCraft && !this.baseModule.hasAir) {
            hasReq = false;
        }
        if (this.requirePowerToCraft && !this.baseModule.hasPower) {
            hasReq = false;
        }
        if (this.requireWaterToCraft && !this.baseModule.isWatered()) {
            hasReq = false;
        }
        return hasReq;
    }

    @Override
    public void update(float delta) {
        if (this.building && this.buildQueue.size >= 1) {
            if (this.hasBaseRequirementsForBuilding()) {
                this.buildTimer += delta * this.buildSpeedMultiplier;
            }
            while (this.buildQueue.size > 0 && this.buildTimer >= this.buildQueue.get((int)0).craftTime) {
                float tempBuildTimer = this.buildTimer - this.buildQueue.get((int)0).craftTime;
                this.finishedBuilding();
                this.buildTimer = tempBuildTimer;
            }
        } else if (this.buildQueue.size > 0) {
            this.nextBuildItem();
        }
    }

    public void setFinishedIcon(Image i) {
        this.buildFinishedIcon = i;
        this.buildFinishedIcon.setVisible(false);
    }

    private void nextBuildItem() {
        this.building = true;
        this.buildTimer = 0.0f;
        this.setChanged();
        this.notifyObservers("updatedBuildQueue");
    }

    @Override
    public void onDestroy() {
        this.deleteObservers();
    }

    public void addBuildable(String itemId) {
        this.buildables.add(itemId);
    }

    public ArrayList<String> getBuildables() {
        return this.buildables;
    }

    public ItemData getBuildableItem(String id) {
        ItemFactory.getInstance();
        for (ItemData i : ItemFactory.ITEMDATALIST) {
            if (!i.id.equals(id)) continue;
            return i;
        }
        return null;
    }

    public ItemData getBuildableItem(int index) {
        String id = this.buildables.get(index);
        ItemFactory.getInstance();
        for (ItemData i : ItemFactory.ITEMDATALIST) {
            if (!i.id.equals(id)) continue;
            return i;
        }
        return null;
    }

    public boolean hasResources(ItemData itemData) {
        boolean result = true;
        if (MoonBase.getCurrentMission().missionType == Mission.MissionTypes.creative) {
            return true;
        }
        if (this.playerInventory.getItemList().size() > 0) {
            for (RecipieRequirement r : itemData.requires) {
                boolean hasEnough = false;
                int ingredientAmount = this.player.playerInventory.getAmountOfItem(r.id);
                if (ingredientAmount >= r.quantity) {
                    hasEnough = true;
                    MoonBase.debug("Has enough " + r.id);
                } else {
                    hasEnough = false;
                    MoonBase.debug("Doesn't have enough " + r.id);
                }
                if (hasEnough) continue;
                return false;
            }
        } else {
            result = false;
        }
        return result;
    }

    public boolean hasResources(ArrayList<ItemStack> ingredientsAvailable, ItemData itemData) {
        boolean result = true;
        if (MoonBase.getCurrentMission().missionType == Mission.MissionTypes.creative) {
            return true;
        }
        if (ingredientsAvailable.size() > 0) {
            for (RecipieRequirement r : itemData.requires) {
                boolean hasEnough = false;
                int need = r.quantity;
                for (ItemStack stack : ingredientsAvailable) {
                    if (!r.id.equals(stack.getId())) continue;
                    if (need <= stack.getAmount()) {
                        hasEnough = true;
                        break;
                    }
                    need -= stack.getAmount();
                }
                if (need <= 0) {
                    hasEnough = true;
                }
                if (hasEnough) continue;
                return false;
            }
        } else {
            result = false;
        }
        return result;
    }

    public void spendResources(ItemData itemData) {
        Array<ItemStack> spentItemBuffer = new Array<ItemStack>();
        ArrayList availableResources = new ArrayList();
        MoonBase.log("ItemCrafter: Spending resources...");
        for (RecipieRequirement r : itemData.requires) {
            ItemStack bufferStack = new ItemStack(r.id, 0);
            int amountNeeded = r.quantity;
            for (int i = this.playerInventory.itemList.size() - 1; i >= 0; --i) {
                int move;
                ItemStack stack = this.playerInventory.itemList.get(i);
                if (!r.id.equals(stack.getId())) continue;
                if (amountNeeded <= 0) break;
                if (amountNeeded > stack.getAmount()) {
                    move = stack.getAmount();
                    amountNeeded -= move;
                    bufferStack.add(move);
                    if (MoonBase.getCurrentMission().missionType == Mission.MissionTypes.creative) continue;
                    this.playerInventory.consumeItem(stack, move);
                    continue;
                }
                move = amountNeeded;
                bufferStack.add(move);
                amountNeeded -= move;
                if (MoonBase.getCurrentMission().missionType == Mission.MissionTypes.creative) break;
                this.playerInventory.consumeItem(stack, move);
                break;
            }
            spentItemBuffer.add(bufferStack);
        }
        this.spentItemBufferList.add(spentItemBuffer);
        this.playerInventory.removeEmpties();
        this.player.inventoryUpdate();
    }

    public void sortStacksByQuantity(ArrayList<ItemStack> stackList) {
        Collections.sort(stackList, new Comparator<ItemStack>(){

            @Override
            public int compare(ItemStack s1, ItemStack s2) {
                int amount2;
                int amount1 = s1.getAmount();
                if (amount1 > (amount2 = s2.getAmount())) {
                    return 1;
                }
                if (amount1 < amount2) {
                    return -1;
                }
                return 0;
            }
        });
    }

    public void spendResources(ItemData itemData, ArrayList<ItemStack> resourcePool) {
        Array<ItemStack> spentItemBuffer = new Array<ItemStack>();
        MoonBase.log("Autocrafter Sending resources...");
        block0: for (RecipieRequirement r : itemData.requires) {
            for (ItemStack stack : resourcePool) {
                if (!r.id.equals(stack.getId())) continue;
                ItemStack bufferStack = new ItemStack(r.id, r.quantity);
                spentItemBuffer.add(bufferStack);
                if (MoonBase.getCurrentMission().missionType == Mission.MissionTypes.creative) continue block0;
                continue block0;
            }
        }
        this.spentItemBufferList.add(spentItemBuffer);
    }

    public boolean isQueueFull() {
        return this.buildQueue.size >= this.buildQueueSizeLimit;
    }

    public void addItemToBuildQueue(ItemData itemData, boolean spendResources) {
        if (!this.isQueueFull()) {
            System.out.println("Start crafting " + itemData.getName());
            if (spendResources) {
                this.spendResources(itemData);
            }
            this.buildQueue.add(itemData);
            this.setChanged();
            this.notifyObservers("updatedBuildQueue");
        } else {
            System.out.println("ItemCrafter: Build queue full");
        }
        this.building = this.buildQueue.size > 0;
    }

    public void addItemToBuildQueue(ItemData itemData, ArrayList<ItemStack> resourcePool) {
        if (!this.isQueueFull()) {
            System.out.println("Start crafting " + itemData.getName());
            this.spendResources(itemData, resourcePool);
            this.buildQueue.add(itemData);
            this.setChanged();
            this.notifyObservers("updatedBuildQueue");
        } else {
            System.out.println("ItemCrafter: Build queue full");
        }
        this.building = this.buildQueue.size > 0;
    }

    public void finishedBuilding() {
        MoonBase.log("ItemCrafter: Done building " + this.getCurrentBuildItem().getName());
        if (!this.soundFinishBuilding.equals("")) {
            float pan = Audio.getInstance().playerDistancePan(this.baseModule.world, this.baseModule.getXCenter(), this.baseModule.getYCenter());
            float rPitch = MathUtils.random(0.9f, 1.1f);
            float volume = Audio.getInstance().playerDistanceMultiplier(this.baseModule.world, this.baseModule.getXCenter(), this.baseModule.getYCenter(), Tile.TILE_SIZE * 6.0f, this.soundFinishVolume);
            Audio.getInstance().playSound(this.soundFinishBuilding, volume, rPitch, pan);
        }
        int amount = 1;
        if (this.getCurrentBuildItem().craftingQuantity != 0) {
            amount = this.getCurrentBuildItem().craftingQuantity;
        }
        this.itemsToCollect.add(new ItemStack(ItemFactory.getInstance().createItem(this.getCurrentBuildItem()), amount));
        this.buildQueue.removeIndex(0);
        if (this.spentItemBufferList.size > 0) {
            this.spentItemBufferList.removeIndex(0);
        }
        this.buildTimer = 0.0f;
        if (this.buildQueue.size == 0) {
            this.building = false;
            MoonBase.log("ItemCrafter: Queue empty");
        } else {
            this.nextBuildItem();
            MoonBase.log("ItemCrafter: Now building " + this.getCurrentBuildItem().getName());
        }
        this.showFinishedIcon(true);
        this.setChanged();
        this.notifyObservers("finishedBuilding");
    }

    public void showFinishedIcon(boolean visible) {
        if (this.buildFinishedIcon != null) {
            this.buildFinishedIcon.clearActions();
            this.buildFinishedIcon.setVisible(visible);
            if (visible) {
                this.buildFinishedIcon.addAction(Actions.sequence((Action)Actions.scaleTo(0.0f, 0.0f), (Action)Actions.alpha(0.0f), (Action)Actions.parallel((Action)Actions.fadeIn(0.25f), (Action)Actions.scaleTo(0.32f, 0.32f, 0.25f, Interpolation.sineOut)), (Action)Actions.forever(Actions.sequence((Action)Actions.scaleTo(0.3f, 0.3f, 0.5f, Interpolation.sine), (Action)Actions.scaleTo(0.32f, 0.32f, 0.5f, Interpolation.sine)))));
            }
        }
    }

    public void cancel(int queueIndex) {
        if (this.building && queueIndex < this.buildQueue.size) {
            this.buildTimer = 0.0f;
            MoonBase.log("ItemCrafter: Cancelling index " + queueIndex);
            for (ItemStack bs : this.spentItemBufferList.get(queueIndex)) {
                int spaceForBs = this.player.playerInventory.getSpaceAvailableFor(bs.getId());
                if (spaceForBs >= bs.getAmount()) {
                    this.player.collect(bs);
                    continue;
                }
                int overage = bs.getAmount() - spaceForBs;
                for (int i = 0; i < overage; ++i) {
                    new ItemPickup(this.baseModule.world, this.baseModule.chunk, this.baseModule.getWorldXPos(), this.baseModule.getWorldYPos(), bs.item);
                }
            }
            this.spentItemBufferList.removeIndex(queueIndex);
            this.buildQueue.removeIndex(queueIndex);
            if (this.buildQueue.size > 0) {
                if (queueIndex == 0) {
                    this.nextBuildItem();
                }
            } else {
                this.building = false;
            }
            this.setChanged();
            this.notifyObservers("updatedBuildQueue");
        }
    }

    @Override
    public void interact(Player player) {
        this.player = player;
        this.playerInventory = this.player.getPlayerInventory();
    }

    public ItemStack collectItem(int index) {
        ItemStack itemStack = this.itemsToCollect.get(index);
        this.itemsToCollect.removeIndex(0);
        if (this.itemsToCollect.size == 0) {
            this.showFinishedIcon(false);
        }
        this.setChanged();
        this.notifyObservers();
        return itemStack;
    }

    public boolean collectItem() {
        boolean canCollect = true;
        this.consolidateFinished();
        this.itemsToCollect.reverse();
        for (int i = this.itemsToCollect.size - 1; i >= 0; --i) {
            System.out.println("ItemCrafter: trying to collect item: " + this.itemsToCollect.get((int)i).item.getName());
            int roomToHold = this.playerInventory.getSpaceAvailableFor(this.itemsToCollect.get(i).getId());
            MoonBase.log("-----------------");
            MoonBase.log("room to hold = " + roomToHold);
            if (roomToHold >= 1) {
                MoonBase.achievementAdapter.collectCraftedItem(this.itemsToCollect.get(i).getId());
                this.player.collect(this.itemsToCollect.get(i));
                if (this.itemsToCollect.get(i).getAmount() != 0) continue;
                this.itemsToCollect.removeIndex(i);
                continue;
            }
            System.out.println("ItemCrafter: can't collect " + this.itemsToCollect.get((int)0).item.getName());
            canCollect = false;
        }
        this.itemsToCollect.reverse();
        if (this.itemsToCollect.size == 0) {
            this.showFinishedIcon(false);
        }
        this.setChanged();
        this.notifyObservers();
        return canCollect;
    }

    private void consolidateFinished() {
        int count = this.itemsToCollect.size;
        for (int i = this.itemsToCollect.size - 2; i >= 0; --i) {
            ItemStack iCurrent = this.itemsToCollect.get(i);
            ItemStack iLast = this.itemsToCollect.get(i + 1);
            if (!iLast.getId().equals(iCurrent.getId()) || !iLast.item.canStackMultiples()) continue;
            iCurrent.add(iLast.getAmount());
            this.itemsToCollect.removeIndex(i + 1);
        }
        int finalCount = this.itemsToCollect.size;
        int diff = count - finalCount;
        MoonBase.log("ItemCrafter: consolidated " + diff);
    }

    public float getBuildProgress() {
        ItemData itemData;
        float p = 0.0f;
        if (this.buildQueue.size >= 1 && (itemData = this.getCurrentBuildItem()) != null) {
            p = this.buildTimer / itemData.craftTime;
        }
        return p;
    }

    public int getPlayerQuantity(RecipieRequirement r) {
        int quantity = 0;
        for (ItemStack stack : this.playerInventory.getItemList()) {
            if (!stack.getId().equals(r.id)) continue;
            quantity += stack.getAmount();
        }
        return quantity;
    }

    public void rememberSelectedBuildable(String buildableId) {
        this.lastSelectedItemId = buildableId;
    }

    public String getLastSelectedItemId() {
        return this.lastSelectedItemId;
    }

    public void dropItems(World world, Chunk chunk, float worldXPos, float worldYPos) {
        while (this.spentItemBufferList.size > 0) {
            for (ItemStack bs : this.spentItemBufferList.get(0)) {
                for (int i = 0; i < bs.getAmount(); ++i) {
                    new ItemPickup(world, chunk, worldXPos, worldYPos, ItemFactory.getInstance().createItem(bs.getId()));
                }
            }
            this.spentItemBufferList.removeIndex(0);
        }
        while (this.itemsToCollect.size > 0) {
            for (int i = 0; i < this.itemsToCollect.get(0).getAmount(); ++i) {
                new ItemPickup(world, chunk, worldXPos, worldYPos, this.itemsToCollect.get((int)0).item);
            }
            this.itemsToCollect.removeIndex(0);
        }
    }
}

