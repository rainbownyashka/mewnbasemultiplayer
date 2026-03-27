/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.ui.BuildingPlacementCursor;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class ProtoBaseBehavior
implements Behavior {
    public boolean loaded;
    public float timer;
    public float buildTime;
    public String itemId;
    public String base;
    public BuildingPlacementCursor.ORIENTATIONS orientation;
    public boolean playerClear;

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
        bd.properties.put("timer", Float.valueOf(this.timer));
        bd.properties.put("buildTime", Float.valueOf(this.buildTime));
        bd.properties.put("itemId", this.itemId);
        bd.properties.put("base", this.base);
        bd.properties.put("orientation", (Object)this.orientation);
        bd.properties.put("playerClear", this.playerClear);
        return bd;
    }
}

