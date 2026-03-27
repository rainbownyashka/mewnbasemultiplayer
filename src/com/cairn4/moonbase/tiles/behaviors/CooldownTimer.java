/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.CooldownCallback;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class CooldownTimer
implements Behavior {
    public boolean loaded = false;
    public float timer;
    public float length;
    public boolean running;
    public boolean ready;
    public boolean triggerOnInteract = false;
    public CooldownCallback callback;

    public CooldownTimer() {
        this(null);
    }

    public CooldownTimer(CooldownCallback c) {
        this.callback = c;
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
        this.running = false;
        this.ready = true;
        this.timer = 0.0f;
    }

    @Override
    public void update(float delta) {
        if (this.running) {
            this.timer += delta;
            if (this.timer >= this.length) {
                this.running = false;
                this.ready = true;
                MoonBase.log("Finished cooldown!!");
                if (this.callback != null) {
                    this.callback.callback();
                }
            }
        }
    }

    public void trigger() {
        this.running = true;
        this.ready = false;
        this.timer = 0.0f;
    }

    @Override
    public void interact(Player player) {
        if (this.triggerOnInteract) {
            if (!this.running && this.ready) {
                this.trigger();
                MoonBase.debug("Starting cooldown");
            } else {
                MoonBase.debug("Not doing anything");
            }
        }
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public BehaviorData getData() {
        BehaviorData bd = new BehaviorData();
        bd.behaviorClass = this.getClass().getName();
        bd.properties.put("running", this.running);
        bd.properties.put("ready", this.ready);
        bd.properties.put("timer", Float.valueOf(this.timer));
        bd.properties.put("length", Float.valueOf(this.length));
        bd.properties.put("triggerOnInteract", this.triggerOnInteract);
        return bd;
    }

    public boolean isReady() {
        return this.ready;
    }

    public boolean isRunning() {
        return this.running;
    }
}

