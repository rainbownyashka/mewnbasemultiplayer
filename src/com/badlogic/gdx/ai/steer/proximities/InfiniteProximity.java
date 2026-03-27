/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.steer.proximities;

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.proximities.ProximityBase;
import com.badlogic.gdx.math.Vector;

public class InfiniteProximity<T extends Vector<T>>
extends ProximityBase<T> {
    public InfiniteProximity(Steerable<T> owner, Iterable<? extends Steerable<T>> agents) {
        super(owner, agents);
    }

    @Override
    public int findNeighbors(Proximity.ProximityCallback<T> callback) {
        int neighborCount = 0;
        for (Steerable currentAgent : this.agents) {
            if (currentAgent == this.owner || !callback.reportNeighbor(currentAgent)) continue;
            ++neighborCount;
        }
        return neighborCount;
    }
}

