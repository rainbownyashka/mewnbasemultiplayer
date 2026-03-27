/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.RecipieRequirement;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class ItemRecycler
implements Behavior {
    public boolean loaded = false;
    public BaseModule baseModule;
    public String deconstructorId;
    public Array<ItemStack> spentItemBufferList = new Array();
    public Array<String> buildQueue = new Array();
    public float buildTimer;
    public static final float buildDuration = 2.0f;
    public boolean building;
    public Array<ItemStack> itemsToCollect = new Array();
    public Player player;
    public PlayerInventory playerInventory;
    private Image buildFinishedIcon;
    public String soundFinishBuilding = "";
    public float soundFinishVolume = 0.6f;

    public boolean isDeconstructing() {
        return this.building;
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

    @Override
    public void update(float delta) {
        if (this.building && this.buildQueue.size >= 1) {
            this.buildTimer += delta;
            while (this.buildQueue.size > 0 && this.buildTimer >= 2.0f) {
                float tempBuildTimer = this.buildTimer - 2.0f;
                this.finishedBuilding();
                this.buildTimer = tempBuildTimer;
            }
        } else if (this.buildQueue.size > 0) {
            this.nextBuildItem();
        }
    }

    @Override
    public void interact(Player player) {
        this.player = player;
        this.playerInventory = this.player.getPlayerInventory();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public BehaviorData getData() {
        return null;
    }

    public boolean isItemDeconstructable(String itemId) {
        ItemData id = ItemFactory.getItemData(itemId);
        return id.recyclable && !id.requires.isEmpty();
    }

    public void addItemToQueue(String itemId) {
        if (!this.isDeconstructing()) {
            this.buildQueue.add(itemId);
            ItemStack newStack = new ItemStack(ItemFactory.getInstance().createItem(itemId), 1);
            this.spentItemBufferList.add(newStack);
        }
    }

    public String getCurrentBuildItem() {
        return this.buildQueue.get(0);
    }

    public void finishedBuilding() {
        MoonBase.log("ItemDeconstructor: Done with " + this.getCurrentBuildItem());
        if (!this.soundFinishBuilding.equals("")) {
            float pan = Audio.getInstance().playerDistancePan(this.baseModule.world, this.baseModule.getXCenter(), this.baseModule.getYCenter());
            float rPitch = MathUtils.random(0.9f, 1.1f);
            float volume = Audio.getInstance().playerDistanceMultiplier(this.baseModule.world, this.baseModule.getXCenter(), this.baseModule.getYCenter(), Tile.TILE_SIZE * 6.0f, this.soundFinishVolume);
            Audio.getInstance().playSound(this.soundFinishBuilding, volume, rPitch, pan);
        }
        ItemData id = ItemFactory.getItemData(this.buildQueue.get(0));
        if (id.durability > 0) {
            for (RecipieRequirement r : id.requires) {
                int half;
                if (!r.id.equals("scrap") && !r.id.equals("metal") && !r.id.equals("crystal") || (half = MathUtils.floor((float)r.quantity / 2.0f)) <= 0) continue;
                this.itemsToCollect.add(new ItemStack(ItemFactory.getInstance().createItem(r.id), half));
            }
        } else {
            for (RecipieRequirement r : id.requires) {
                this.itemsToCollect.add(new ItemStack(ItemFactory.getInstance().createItem(r.id), r.quantity));
            }
        }
        this.buildQueue.removeIndex(0);
        if (this.spentItemBufferList.size > 0) {
            this.spentItemBufferList.removeIndex(0);
        }
        this.buildTimer = 0.0f;
        this.showFinishedIcon(true);
        this.nextBuildItem();
    }

    private void nextBuildItem() {
        if (this.buildQueue.size > 0) {
            this.building = true;
            this.buildTimer = 0.0f;
            MoonBase.log("ItemDeconstructor: Now deconstructing " + this.buildQueue.first());
        } else {
            this.building = false;
            MoonBase.log("ItemDeconstructor: Queue empty");
        }
    }

    public void setFinishedIcon(Image i) {
        this.buildFinishedIcon = i;
        this.buildFinishedIcon.setVisible(false);
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
        return canCollect;
    }
}

