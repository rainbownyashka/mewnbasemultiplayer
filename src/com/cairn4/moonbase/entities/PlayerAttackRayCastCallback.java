/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.entities.Creature;

public class PlayerAttackRayCastCallback
implements RayCastCallback {
    float closetFraction = 1.0f;
    float playerFraction = 1.0f;
    Creature creature = null;

    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        if (fixture.getBody() != null) {
            Body b;
            if (fixture.getFilterData().categoryBits == 2) {
                MoonBase.log("hit wall: " + fraction + " - " + point);
                if (fraction <= this.closetFraction) {
                    this.closetFraction = fraction;
                }
            }
            if (fixture.getFilterData().categoryBits == 8 && (b = fixture.getBody()).getUserData() instanceof Creature) {
                this.creature = (Creature)b.getUserData();
                this.playerFraction = fraction;
            }
        }
        return fraction;
    }

    public boolean hitCreatureFirst() {
        if (this.playerFraction <= this.closetFraction) {
            return this.creature != null;
        }
        return false;
    }
}

