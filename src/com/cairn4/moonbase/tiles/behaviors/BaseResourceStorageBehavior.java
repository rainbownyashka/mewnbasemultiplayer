/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class BaseResourceStorageBehavior
implements Behavior {
    public boolean loaded = false;
    public BaseResources type;
    public float amount;
    public float capacity;
    public String id;

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
        this.id = s;
    }

    @Override
    public String getId() {
        return this.id;
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
        bd.properties.put("type", (Object)this.type);
        bd.properties.put("amount", Float.valueOf(this.amount));
        bd.properties.put("id", this.id);
        return bd;
    }

    public void consume(float amount) {
        this.amount -= amount;
        if (this.amount < 0.0f) {
            this.amount = 0.0f;
        }
    }

    public float store(float v) {
        if (this.amount < this.capacity) {
            float spaceAvailable = this.capacity - this.amount;
            if (spaceAvailable > v) {
                this.amount += v;
                return 0.0f;
            }
            this.amount += spaceAvailable;
            return v -= spaceAvailable;
        }
        return v;
    }

    public void storeAll(float v) {
        if (this.amount < this.capacity) {
            this.amount += v;
        }
    }
}

