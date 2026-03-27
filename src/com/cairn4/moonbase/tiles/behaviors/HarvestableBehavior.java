/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.HarvestCallback;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class HarvestableBehavior
implements Behavior {
    public boolean loaded;
    public float health;
    public boolean harvesting;
    public boolean harvested;
    public HarvestCallback harvestCallback;

    public void startHarvesting() {
        this.harvesting = true;
    }

    public void stopHarvesting() {
        this.harvesting = false;
    }

    public void harvest(float delta) {
        this.health -= delta;
        this.checkFinished();
    }

    private void checkFinished() {
        if (this.health <= 0.0f && !this.harvested) {
            if (this.harvestCallback != null) {
                this.harvestCallback.harvestFinished();
                this.harvested = true;
            } else {
                MoonBase.error("Null harvest callback.");
            }
        }
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
        bd.properties.put("harvested", this.harvested);
        bd.properties.put("health", Float.valueOf(this.health));
        return bd;
    }
}

