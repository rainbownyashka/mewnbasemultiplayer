/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class PowerStorageBehavior
implements Behavior {
    public boolean loaded = false;
    public float powerStored;
    public float maxPowerStored;

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
        bd.properties.put("amount", Float.valueOf(this.powerStored));
        bd.properties.put("capacity", Float.valueOf(this.maxPowerStored));
        return bd;
    }

    public void consumeStoredPower(float amount) {
        this.powerStored -= amount;
        if (this.powerStored < 0.0f) {
            this.powerStored = 0.0f;
        }
    }

    public float storePower(float v) {
        if (this.powerStored < this.maxPowerStored) {
            float spaceAvailable = this.maxPowerStored - this.powerStored;
            if (spaceAvailable > v) {
                this.powerStored += v;
                return 0.0f;
            }
            this.powerStored += spaceAvailable;
            return v -= spaceAvailable;
        }
        return v;
    }
}

