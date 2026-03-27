/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class WaterStorageBehavior
implements Behavior {
    public boolean loaded = false;
    public float waterStored;
    public float maxWaterStored;

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
        BehaviorData bd = new BehaviorData();
        bd.behaviorClass = this.getClass().getName();
        bd.properties.put("waterStored", Float.valueOf(this.waterStored));
        bd.properties.put("maxWaterStored", Float.valueOf(this.maxWaterStored));
        return bd;
    }

    public void consumeStoredWater(float amount) {
        this.waterStored -= amount;
        if (this.waterStored < 0.0f) {
            this.waterStored = 0.0f;
        }
    }

    public float storeWater(float v) {
        if (this.waterStored < this.maxWaterStored) {
            float spaceAvailable = this.maxWaterStored - this.waterStored;
            if (spaceAvailable > v) {
                this.waterStored += v;
                return 0.0f;
            }
            this.waterStored += spaceAvailable;
            return v -= spaceAvailable;
        }
        return v;
    }
}

