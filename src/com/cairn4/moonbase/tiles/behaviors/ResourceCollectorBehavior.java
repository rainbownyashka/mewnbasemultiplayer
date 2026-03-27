/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.CooldownCallback;
import com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior;
import com.cairn4.moonbase.worlddata.BehaviorData;
import com.cairn4.moonbase.worlddata.ItemDropperFactory;
import com.cairn4.moonbase.worlddata.ItemDropperItemData;
import com.cairn4.moonbase.worlddata.MiningResourceData;

public class ResourceCollectorBehavior
implements Behavior {
    public boolean loaded = false;
    public BaseModule baseModule;
    public ItemStorageBehavior itemStorageBehavior;
    public boolean requirePower = false;
    public float timePerCollect = 1.0f;
    public float collectTimer;
    public CooldownCallback collectCallback;
    public String itemIdReadyToCollect;
    public boolean inventorySlotAvailable = false;
    public GroundTile.Biomes biome;
    private boolean running;

    public boolean isRunning() {
        return this.running;
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
        this.running = true;
    }

    @Override
    public void update(float delta) {
        if (this.baseModule != null) {
            boolean canRun = true;
            if (this.requirePower && !this.baseModule.hasPower) {
                canRun = false;
            }
            if (canRun && this.inventorySlotAvailable) {
                this.running = true;
                this.collectTimer += delta;
                if (this.collectTimer > this.timePerCollect) {
                    this.collectTimer = 0.0f;
                    this.itemIdReadyToCollect = this.chooseItemToCollect();
                    if (this.collectCallback != null) {
                        this.collectCallback.callback();
                    }
                }
            } else {
                this.running = false;
            }
        }
    }

    public String getItemId() {
        return this.itemIdReadyToCollect;
    }

    private String chooseItemToCollect() {
        for (MiningResourceData mrd : ItemDropperFactory.getInstance().miningResources) {
            if (!mrd.biome.equals(this.biome.name()) || mrd.items.size() <= 0) continue;
            return this.getItemFromBiome(mrd);
        }
        return null;
    }

    private String getItemFromBiome(MiningResourceData mrd) {
        String itemId = "";
        float totalChance = 0.0f;
        for (ItemDropperItemData idid : mrd.items) {
            totalChance += idid.dropChance;
        }
        float r = MathUtils.random(0.0f, totalChance);
        float base = 0.0f;
        for (ItemDropperItemData idid : mrd.items) {
            if (r >= base && r < idid.dropChance + base) {
                return idid.itemId;
            }
            base += idid.dropChance;
        }
        MoonBase.error("ResourceCollectorBehavior: error selecting random item");
        return null;
    }

    public void skipItem() {
        this.itemIdReadyToCollect = null;
    }

    public void setStorageAvailable(boolean hasRoom) {
        this.inventorySlotAvailable = hasRoom;
    }

    public boolean getStorageAvailable() {
        return this.inventorySlotAvailable;
    }

    @Override
    public void interact(Player player) {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public BehaviorData getData() {
        BehaviorData bd = new BehaviorData();
        bd.behaviorClass = this.getClass().getName();
        bd.properties.put("collectTimer", Float.valueOf(this.collectTimer));
        return bd;
    }

    public float getProgress() {
        return this.collectTimer / this.timePerCollect;
    }
}

