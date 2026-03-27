/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.steer.proximities;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.proximities.ProximityBase;
import com.badlogic.gdx.math.Vector;

public class RadiusProximity<T extends Vector<T>>
extends ProximityBase<T> {
    protected float radius;
    private float lastTime;

    public RadiusProximity(Steerable<T> owner, Iterable<? extends Steerable<T>> agents, float radius) {
        super(owner, agents);
        this.radius = radius;
        this.lastTime = 0.0f;
    }

    public float getRadius() {
        return this.radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public int findNeighbors(Proximity.ProximityCallback<T> callback) {
        int neighborCount = 0;
        float currentTime = GdxAI.getTimepiece().getTime();
        if (this.lastTime != currentTime) {
            this.lastTime = currentTime;
            Object ownerPosition = this.owner.getPosition();
            for (Steerable currentAgent : this.agents) {
                float range;
                float squareDistance;
                if (currentAgent != this.owner && (squareDistance = ownerPosition.dst2(currentAgent.getPosition())) < (range = this.radius + currentAgent.getBoundingRadius()) * range && callback.reportNeighbor(currentAgent)) {
                    currentAgent.setTagged(true);
                    ++neighborCount;
                    continue;
                }
                currentAgent.setTagged(false);
            }
        } else {
            for (Steerable currentAgent : this.agents) {
                if (currentAgent == this.owner || !currentAgent.isTagged() || !callback.reportNeighbor(currentAgent)) continue;
                ++neighborCount;
            }
        }
        return neighborCount;
    }
}

