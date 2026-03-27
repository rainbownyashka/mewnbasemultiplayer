/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.RecipieRequirement;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.CraftingBot;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior;
import com.cairn4.moonbase.worlddata.BehaviorData;
import com.cairn4.moonbase.worlddata.CraftingBonusData;
import com.cairn4.moonbase.worlddata.WorkOrderData;
import java.util.ArrayList;
import java.util.Observable;

public class CraftingBotBehavior
extends Observable
implements Behavior {
    public static int maxQuantity = 99;
    public static int maxJobs = 10;
    public boolean loaded = false;
    public CraftingBot baseModule;
    public Label debugLabel;
    private boolean busyWithAnim = false;
    private float stepTimer = 2.0f;
    private float stepDelay = 1.8f;
    private WorkOrder itemsHoldingForOrder;
    public ArrayList<ItemStack> itemsHolding = new ArrayList();
    public Array<WorkOrderData> workQueueData = new Array();
    public ArrayList<WorkOrder> workQueue = new ArrayList();
    public ArrayList<Tile> adjacentTiles = new ArrayList();
    public ArrayList<ItemCrafter> availableCrafters = new ArrayList();
    public ArrayList<ItemStorageBehavior> storageBehaviorList = new ArrayList();
    public ArrayList<ItemStack> combinedItemStorageList = new ArrayList();
    public static String ADD_REMOVE_JOB_MSG = "add_remove_job_msg";
    ArrayList<String> botBrainLog = new ArrayList();
    public Group armGroup;
    int startIndex = 0;

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
        this.workQueue.clear();
        for (WorkOrderData wod : this.workQueueData) {
            WorkOrder wo = new WorkOrder(wod.itemId, wod.quantity);
            this.workQueue.add(wo);
            MoonBase.log("CraftingBot: loaded job: " + wo.itemId + " (" + wo.quantity + ")");
        }
        this.workQueueData.clear();
    }

    @Override
    public void update(float delta) {
        this.checkWorkOrders();
        if (this.baseModule.hasPower) {
            this.stepTimer += delta;
            if (this.stepTimer > this.stepDelay) {
                this.stepTimer = 0.0f;
                this.updateAdjacentTiles();
                this.updateCrafterList();
                this.updateStorageList();
                boolean fetched = this.fetchReadyItem();
                if (fetched) {
                    this.stepTimer = 0.0f;
                }
                if (!fetched) {
                    if (this.itemsHoldingForOrder != null) {
                        ItemData id = ItemFactory.getItemData(this.itemsHoldingForOrder.itemId);
                        if (this.itemsHoldingForOrder.craftingIn.hasResources(this.itemsHolding, id)) {
                            this.itemsHoldingForOrder.craftingIn.addItemToBuildQueue(id, this.itemsHolding);
                            this.itemsHolding.clear();
                            this.itemsHoldingForOrder = null;
                        } else {
                            this.grabIngredientsFromStorage(this.itemsHoldingForOrder);
                            this.stepTimer = 0.0f;
                        }
                    } else {
                        this.tryToStartOrder();
                    }
                    this.updateDebugText();
                }
            }
            boolean running = false;
            for (WorkOrder wo : this.workQueue) {
                if (!wo.inProgress || wo.satisfied || wo.blockedByStorage || wo.blockedByNoAvailableCrafter || wo.notEnoughIngredients) continue;
                running = true;
                break;
            }
            if (running) {
                this.baseModule.animatePanelLights(delta);
            } else {
                this.baseModule.turnOffPanel();
            }
        }
    }

    @Override
    public BehaviorData getData() {
        BehaviorData bd = new BehaviorData();
        bd.behaviorClass = this.getClass().getName();
        this.workQueueData.clear();
        MoonBase.log("workQueue size: " + this.workQueue.size());
        for (WorkOrder wo : this.workQueue) {
            WorkOrderData wod = new WorkOrderData(wo);
            this.workQueueData.add(wod);
            MoonBase.log("-- saving work order: " + wod.itemId);
        }
        MoonBase.log("workQueue size is now: " + this.workQueue.size());
        bd.properties.put("workQueueData", this.workQueueData);
        return bd;
    }

    public String updateDebugText() {
        String text = "";
        for (int i = this.botBrainLog.size() - 1; i >= 0; --i) {
            text = text + "\n";
            text = text + this.botBrainLog.get(i);
        }
        return text;
    }

    private void grabIngredientsFromStorage(WorkOrder wo) {
        ItemData id = ItemFactory.getItemData(wo.itemId);
        for (RecipieRequirement r : id.requires) {
            int totalNeeded;
            if (r.id.equals("drink-water")) {
                for (int waterCount = 0; waterCount < r.quantity; ++waterCount) {
                    float totalBaseWater = this.baseModule.getBaseGroup().getTotalWaterStored();
                    if (!(totalBaseWater >= 60.0f)) continue;
                    this.baseModule.getBaseGroup().consumeResource(60.0f, BaseResources.water, Float.valueOf(this.baseModule.getBaseGroup().getTotalWaterStored()));
                    ItemStack waterStack = new ItemStack("drink-water", 1);
                    this.itemsHolding.add(waterStack);
                    MoonBase.log("drink-water added to bot's item holding");
                }
            }
            if ((totalNeeded = r.quantity - this.getTotalHolding(r.id)) > 0) {
                for (ItemStorageBehavior s : this.storageBehaviorList) {
                    for (int stackIndex = s.itemList.size() - 1; stackIndex >= 0; --stackIndex) {
                        ItemStack movingStack;
                        ItemStack stack = s.itemList.get(stackIndex);
                        if (!stack.getId().equals(r.id)) continue;
                        this.baseModule.animateArm(r.id, s.baseModule, wo.craftingIn.baseModule);
                        this.stepTimer = 0.0f;
                        if (stack.getAmount() < totalNeeded) {
                            this.addThought("Grabbing ingredient " + totalNeeded + " x " + wo.itemId + " (but need more)");
                            movingStack = new ItemStack(stack.getId(), stack.getAmount());
                            this.itemsHolding.add(movingStack);
                            totalNeeded -= movingStack.getAmount();
                            s.remove(stackIndex);
                            continue;
                        }
                        this.addThought("Grabbing ingredient " + totalNeeded + " x " + wo.itemId);
                        movingStack = new ItemStack(stack.getId(), totalNeeded);
                        this.itemsHolding.add(movingStack);
                        stack.remove(totalNeeded);
                        totalNeeded -= movingStack.getAmount();
                        s.removeEmpties();
                        break;
                    }
                    s.removeEmpties();
                }
                continue;
            }
            this.addThought("Already holding enough " + r.id);
        }
    }

    @Override
    public void interact(Player player) {
    }

    @Override
    public void onDestroy() {
    }

    private void checkWorkOrders() {
        boolean deletedJob = false;
        for (int i = this.workQueue.size() - 1; i >= 0; --i) {
            if (this.workQueue.get((int)i).readyToRemove) {
                this.workQueue.remove(i);
                deletedJob = true;
                MoonBase.debug("Removing autocrafter job");
            }
            if (i < maxJobs) continue;
            this.workQueue.remove(i);
            deletedJob = true;
            MoonBase.debug("Too many jobs! Removing autocrafter job");
        }
        for (WorkOrder wo : this.workQueue) {
            int totalAmount = 0;
            for (ItemStorageBehavior isb : this.storageBehaviorList) {
                wo.totalStored = totalAmount += isb.getTotalOfItemId(wo.itemId);
            }
            if (totalAmount > wo.quantity) {
                wo.satisfied = true;
                wo.notEnoughIngredients = false;
                wo.blockedByNoAvailableCrafter = false;
                wo.blockedByStorage = false;
                wo.inProgress = false;
                continue;
            }
            boolean needsMoreMade = this.doesItNeedMoreMade(wo);
            if (needsMoreMade) {
                wo.satisfied = false;
                ItemStorageBehavior bestStorage = this.getBestStorage(wo.itemId);
                wo.blockedByStorage = bestStorage == null;
                ItemCrafter bestCrafter = this.getBestCrafter(wo);
                wo.blockedByNoAvailableCrafter = bestCrafter == null;
                boolean hasIngredients = this.hasIngredientsInStorage(wo);
                wo.notEnoughIngredients = !hasIngredients;
                continue;
            }
            wo.satisfied = true;
        }
        if (deletedJob) {
            this.setChanged();
            this.notifyObservers(ADD_REMOVE_JOB_MSG);
        }
    }

    private boolean doesItNeedMoreMade(WorkOrder wo) {
        int totalStored = wo.totalStored;
        int totalBeingCrafted = 0;
        for (ItemCrafter ic : this.availableCrafters) {
            for (ItemData itemBeingBuilt : ic.buildQueue) {
                if (!itemBeingBuilt.id.equals(wo.itemId)) continue;
                int amount = 1;
                if (itemBeingBuilt.craftingQuantity != 0) {
                    amount = itemBeingBuilt.craftingQuantity;
                }
                totalBeingCrafted += amount;
            }
        }
        return totalStored + totalBeingCrafted < wo.quantity;
    }

    private void tryToStartOrder() {
        if (this.startIndex >= this.workQueue.size()) {
            this.startIndex = 0;
        }
        if (this.workQueue.size() > 0) {
            WorkOrder wo = this.workQueue.get(this.startIndex);
            if (!wo.satisfied && !wo.readyToRemove) {
                ItemStorageBehavior bestStorage = this.getBestStorage(wo.itemId);
                ItemCrafter bestCrafter = this.getBestCrafter(wo);
                if (bestCrafter != null && bestStorage != null) {
                    boolean hasIngredients = this.hasIngredientsInStorage(wo);
                    if (hasIngredients) {
                        this.itemsHoldingForOrder = wo;
                        wo.craftingIn = bestCrafter;
                        wo.storingIn = bestStorage;
                        wo.inProgress = true;
                        wo.blockedByStorage = false;
                        wo.blockedByNoAvailableCrafter = false;
                        wo.notEnoughIngredients = false;
                        this.addThought("Starting work order: " + wo.itemId + " (" + wo.quantity + ")");
                    } else {
                        int totalStored = wo.totalStored;
                        int totalBeingCrafted = 0;
                        for (ItemCrafter ic : this.availableCrafters) {
                            for (ItemData itemBeingBuilt : ic.buildQueue) {
                                if (!itemBeingBuilt.id.equals(wo.itemId)) continue;
                                totalBeingCrafted += itemBeingBuilt.craftingQuantity;
                            }
                        }
                        wo.notEnoughIngredients = totalStored + totalBeingCrafted < wo.quantity;
                    }
                } else {
                    if (bestStorage == null) {
                        wo.blockedByStorage = true;
                    }
                    if (bestCrafter == null) {
                        wo.blockedByNoAvailableCrafter = true;
                    }
                }
            }
        }
        ++this.startIndex;
    }

    private boolean hasIngredientsInStorage(WorkOrder wo) {
        boolean hasStuff = true;
        ItemData id = ItemFactory.getItemData(wo.itemId);
        for (RecipieRequirement r : id.requires) {
            if (r.id.equals("drink-water") && this.baseModule.getBaseGroup().getTotalWaterStored() >= 60.0f) {
                MoonBase.log("has water for 1 bottle");
                continue;
            }
            int total = this.getTotalStored(r.id);
            if (total >= r.quantity) continue;
            hasStuff = false;
            break;
        }
        return hasStuff;
    }

    public int getTotalStored(String id) {
        int total = 0;
        for (ItemStack stack : this.combinedItemStorageList) {
            if (!stack.getId().equals(id)) continue;
            total += stack.getAmount();
        }
        return total;
    }

    private int getTotalHolding(String id) {
        int total = 0;
        for (ItemStack stack : this.itemsHolding) {
            if (!stack.getId().equals(id)) continue;
            total += stack.getAmount();
        }
        return total;
    }

    protected void pickupRecipieItems() {
    }

    protected void updateAdjacentTiles() {
        this.adjacentTiles.clear();
        ArrayList<GridPoint2> adjacentCoords = Tile.GET_ADJACENT_COORDS(this.baseModule.worldX, this.baseModule.worldY, false);
        for (GridPoint2 gp : adjacentCoords) {
            Tile t = this.baseModule.world.getTile(gp.x, gp.y);
            if (t == null) continue;
            this.adjacentTiles.add(t);
        }
    }

    private void updateCrafterList() {
        this.availableCrafters.clear();
        for (Tile t : this.adjacentTiles) {
            BaseModule b;
            Behavior behavior;
            if (!(t instanceof BaseModule) || (behavior = (b = (BaseModule)t).getBehavior(ItemCrafter.class)) == null) continue;
            ItemCrafter ic = (ItemCrafter)behavior;
            this.availableCrafters.add(ic);
        }
    }

    protected ItemCrafter getBestCrafter(WorkOrder order) {
        ItemData itemData = ItemFactory.getItemData(order.itemId);
        ArrayList<ItemCrafter> craftersForThisItem = new ArrayList<ItemCrafter>();
        for (String s : itemData.craftedIn) {
            for (ItemCrafter ic : this.availableCrafters) {
                if (!s.equals(ic.crafterId)) continue;
                craftersForThisItem.add(ic);
            }
        }
        ItemCrafter bestCrafter = null;
        float shortestDelay = 2.1474836E9f;
        for (ItemCrafter ic : craftersForThisItem) {
            if (ic.buildQueue.size >= ic.buildQueueSizeLimit) continue;
            boolean hasBuildReq = ic.hasBaseRequirementsForBuilding();
            if (!ic.isBuilding() && hasBuildReq) {
                bestCrafter = ic;
                break;
            }
            float delayToStartBuild = 0.0f;
            if (!hasBuildReq) {
                delayToStartBuild += 1000.0f;
            }
            for (int buildIndex = 0; buildIndex < ic.buildQueue.size; ++buildIndex) {
                ItemData id = ic.buildQueue.get(buildIndex);
                if (buildIndex == 0) {
                    delayToStartBuild += id.craftTime - ic.buildTimer;
                    continue;
                }
                delayToStartBuild += id.craftTime;
            }
            if (!(delayToStartBuild < shortestDelay)) continue;
            bestCrafter = ic;
            shortestDelay = delayToStartBuild;
        }
        return bestCrafter;
    }

    public void updateStorageList() {
        this.storageBehaviorList.clear();
        this.combinedItemStorageList.clear();
        for (Tile t : this.adjacentTiles) {
            BaseModule b;
            Behavior behavior;
            if (!(t instanceof BaseModule) || (behavior = (b = (BaseModule)t).getBehavior(ItemStorageBehavior.class)) == null) continue;
            ItemStorageBehavior isb = (ItemStorageBehavior)behavior;
            this.storageBehaviorList.add(isb);
            this.combinedItemStorageList.addAll(isb.getItemList());
        }
    }

    public ItemStorageBehavior getBestStorage(String itemId) {
        ItemStorageBehavior isb = null;
        int bestScore = 0;
        for (ItemStorageBehavior storage : this.storageBehaviorList) {
            if (!storage.getCanDeposit()) continue;
            int storageScore = 0;
            if (!storage.isFull()) {
                ++storageScore;
            }
            if (storage.hasSlotAvailable(itemId)) {
                ++storageScore;
            }
            if (storageScore <= bestScore) continue;
            isb = storage;
            bestScore = storageScore;
        }
        return isb;
    }

    public boolean fetchReadyItem() {
        ItemStack collectedItem = null;
        for (ItemCrafter ic : this.availableCrafters) {
            if (ic.itemsToCollect.size <= 0) continue;
            for (int index = 0; index < ic.itemsToCollect.size; ++index) {
                for (WorkOrder wo : this.workQueue) {
                    ItemStorageBehavior isb;
                    int totalStored;
                    ItemStack finishedItemStack = ic.itemsToCollect.get(index);
                    if (!wo.itemId.equals(finishedItemStack.getId()) || (totalStored = this.getTotalStored(wo.itemId)) >= wo.quantity || (isb = this.getBestStorage(ic.itemsToCollect.get(index).getId())) == null || (collectedItem = ic.collectItem(index)) == null) continue;
                    isb.addTo(collectedItem);
                    this.baseModule.animateArm(collectedItem.getId(), ic.baseModule, isb.baseModule);
                    this.fetchBonusItems(collectedItem, isb);
                    return true;
                }
            }
        }
        return false;
    }

    public void fetchBonusItems(ItemStack mainItemStack, ItemStorageBehavior isb) {
        ArrayList<CraftingBonusData> bonusItemList = ItemFactory.getInstance().getCraftingBonus(mainItemStack.getId());
        for (CraftingBonusData iid : bonusItemList) {
            if (iid.probability != 0.0f && !(MathUtils.random() > iid.probability)) continue;
            if (isb.hasSlotAvailable(iid.itemId)) {
                MoonBase.debug("Fetching bonus item: " + iid.itemId + " (" + iid.amount + ")");
                Item newItem = ItemFactory.getInstance().createItem(iid.itemId);
                ItemStack stack = new ItemStack(newItem);
                stack.setAmount(iid.amount);
                isb.addTo(stack);
                continue;
            }
            MoonBase.debug("Dropping bonus item: " + iid.itemId + " (" + iid.amount + ")");
            for (int count = 0; count < iid.amount; ++count) {
                Item newItem = ItemFactory.getInstance().createItem(iid.itemId);
                new ItemPickup(this.baseModule.world, this.baseModule.chunk, this.baseModule.getWorldXPos(), this.baseModule.getWorldYPos(), newItem);
            }
        }
    }

    public WorkOrder addWorkJob(String id, int quantity) {
        if (this.workQueue.size() < maxJobs - 1) {
            WorkOrder wo = new WorkOrder(id, quantity);
            this.workQueue.add(wo);
            this.setChanged();
            this.notifyObservers(ADD_REMOVE_JOB_MSG);
            return wo;
        }
        return null;
    }

    private void addThought(String s) {
        this.botBrainLog.add(0, s);
        MoonBase.debug("CraftingBot: " + s);
        for (int i = this.botBrainLog.size() - 1; i >= 3; --i) {
            this.botBrainLog.remove(i);
        }
    }

    public void changeQuantity(WorkOrder wo, int amountBy) {
        for (WorkOrder wo2 : this.workQueue) {
            if (wo != wo2) continue;
            wo.changeQuantity(amountBy);
        }
        this.setChanged();
        this.notifyObservers(ADD_REMOVE_JOB_MSG);
    }

    public class WorkOrder {
        public String itemId;
        public int quantity;
        public boolean satisfied;
        public boolean inProgress;
        public ItemCrafter craftingIn;
        public ItemStorageBehavior storingIn;
        public int totalStored;
        public boolean notEnoughIngredients;
        public boolean blockedByStorage;
        public boolean blockedByNoAvailableCrafter;
        public boolean readyToRemove;

        public WorkOrder(String itemId, int quantity) {
            this.itemId = itemId;
            this.quantity = quantity;
            this.satisfied = false;
            this.inProgress = false;
            this.readyToRemove = false;
        }

        public void change(String itemId, int i) {
            this.itemId = itemId;
            this.quantity = i;
            this.satisfied = false;
            this.inProgress = false;
            this.readyToRemove = false;
        }

        public void changeQuantity(int i) {
            this.quantity += i;
            if (this.quantity < 0) {
                this.quantity = 0;
            }
            if (this.quantity > maxQuantity) {
                this.quantity = maxQuantity;
            }
        }

        public void setReadyToRemove(boolean b) {
            this.readyToRemove = b;
        }
    }
}

