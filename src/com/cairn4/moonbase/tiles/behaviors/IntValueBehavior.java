/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class IntValueBehavior
implements Behavior {
    public int value = 0;
    public boolean loaded = false;
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
        bd.properties.put("value", this.value);
        bd.properties.put("id", this.id);
        return bd;
    }
}

