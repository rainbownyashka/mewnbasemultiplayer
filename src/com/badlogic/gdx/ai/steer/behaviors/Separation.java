/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.steer.behaviors;

import com.badlogic.gdx.ai.steer.GroupBehavior;
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.math.Vector;

public class Separation<T extends Vector<T>>
extends GroupBehavior<T>
implements Proximity.ProximityCallback<T> {
    float decayCoefficient = 1.0f;
    private T toAgent;
    private T linear;

    public Separation(Steerable<T> owner, Proximity<T> proximity) {
        super(owner, proximity);
        this.toAgent = this.newVector(owner);
    }

    @Override
    protected SteeringAcceleration<T> calculateRealSteering(SteeringAcceleration<T> steering2) {
        steering2.setZero();
        this.linear = steering2.linear;
        this.proximity.findNeighbors(this);
        return steering2;
    }

    @Override
    public boolean reportNeighbor(Steerable<T> neighbor) {
        this.toAgent.set(this.owner.getPosition()).sub(neighbor.getPosition());
        float distanceSqr = this.toAgent.len2();
        if (distanceSqr == 0.0f) {
            return true;
        }
        float maxAcceleration = this.getActualLimiter().getMaxLinearAcceleration();
        float strength = this.getDecayCoefficient() / distanceSqr;
        if (strength > maxAcceleration) {
            strength = maxAcceleration;
        }
        this.linear.mulAdd(this.toAgent, (float)(strength / (float)Math.sqrt(distanceSqr)));
        return true;
    }

    public float getDecayCoefficient() {
        return this.decayCoefficient;
    }

    public Separation<T> setDecayCoefficient(float decayCoefficient) {
        this.decayCoefficient = decayCoefficient;
        return this;
    }

    @Override
    public Separation<T> setOwner(Steerable<T> owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public Separation<T> setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public Separation<T> setLimiter(Limiter limiter) {
        this.limiter = limiter;
        return this;
    }
}

