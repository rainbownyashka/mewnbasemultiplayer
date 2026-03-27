/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.steer.proximities;

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector;

public abstract class ProximityBase<T extends Vector<T>>
implements Proximity<T> {
    protected Steerable<T> owner;
    protected Iterable<? extends Steerable<T>> agents;

    public ProximityBase(Steerable<T> owner, Iterable<? extends Steerable<T>> agents) {
        this.owner = owner;
        this.agents = agents;
    }

    @Override
    public Steerable<T> getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(Steerable<T> owner) {
        this.owner = owner;
    }

    public Iterable<? extends Steerable<T>> getAgents() {
        return this.agents;
    }

    public void setAgents(Iterable<Steerable<T>> agents) {
        this.agents = agents;
    }
}

