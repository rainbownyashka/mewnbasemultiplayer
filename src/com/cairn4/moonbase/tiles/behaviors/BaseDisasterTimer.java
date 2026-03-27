/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class BaseDisasterTimer
implements Behavior {
    public boolean loaded = false;
    public BaseModule baseModule;
    public float minDelay;
    public float maxDelay;
    public float currentDelay;
    public float timer;
    public float chance;

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
        this.newDelay();
        this.timer = 0.0f;
    }

    public void newDelay() {
        this.currentDelay = MathUtils.random(this.minDelay, this.maxDelay);
    }

    @Override
    public void update(float delta) {
        if (this.baseModule.baseDisaster == null) {
            this.timer += delta;
            if (this.timer > this.currentDelay) {
                if (MathUtils.random() > this.chance) {
                    this.baseModule.triggerDisaster();
                } else {
                    this.start();
                }
            }
        }
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
        bd.properties.put("currentDelay", Float.valueOf(this.currentDelay));
        bd.properties.put("timer", Float.valueOf(this.timer));
        return bd;
    }
}

