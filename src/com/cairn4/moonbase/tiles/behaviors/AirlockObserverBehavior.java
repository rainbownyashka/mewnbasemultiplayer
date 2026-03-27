/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.Airlock;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;
import java.util.Observable;

public class AirlockObserverBehavior
extends Observable
implements Behavior {
    public boolean loaded = false;
    boolean airlockOpen = false;
    Airlock airlock;

    public AirlockObserverBehavior(Airlock a) {
        this.airlock = a;
        this.airlockOpen = this.airlock.toggleBehavior.on;
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
        if (this.airlockOpen != this.airlock.toggleBehavior.on) {
            this.airlockOpen = this.airlock.toggleBehavior.on;
            System.out.println("AIRLOCK STATE CHANGED");
            this.setChanged();
            this.notifyObservers("airlock");
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
        return null;
    }
}

