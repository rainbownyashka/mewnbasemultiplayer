/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.entities.Vehicle;

public class CreatureRayCastCallback
implements RayCastCallback {
    float closetFraction = 10.0f;
    float playerFraction = 10.0f;
    Player player = null;
    Vehicle vehicle = null;
    float vehicleFraction = 10.0f;

    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        if (fixture.getBody() != null) {
            short catBits = fixture.getFilterData().categoryBits;
            if (!fixture.isSensor()) {
                if (catBits == 2 || catBits == 4 || catBits == 64) {
                    Body b;
                    if (fraction <= this.closetFraction) {
                        this.closetFraction = fraction;
                    }
                    if (catBits == 4 && (b = fixture.getBody()).getUserData() instanceof Player) {
                        this.player = (Player)b.getUserData();
                        this.playerFraction = fraction;
                    }
                    if (catBits == 64 && (b = fixture.getBody()).getUserData() instanceof Vehicle) {
                        this.vehicle = (Vehicle)b.getUserData();
                        this.vehicleFraction = fraction;
                    }
                }
            } else {
                MoonBase.debug("hit something that's a sensor: " + fraction);
                return 1.0f;
            }
        }
        return fraction;
    }

    public boolean hitPlayerFirst() {
        if (this.playerFraction <= this.closetFraction && this.playerFraction <= this.vehicleFraction) {
            return this.player != null;
        }
        return false;
    }

    public boolean hitVehicleFirst() {
        if (this.vehicleFraction <= this.closetFraction && this.vehicleFraction <= this.playerFraction) {
            return this.vehicle != null;
        }
        return false;
    }
}

