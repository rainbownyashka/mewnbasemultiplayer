/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.utils.C4Utils;

public class Box2dLocation
implements Location<Vector2> {
    Vector2 position = new Vector2();
    float orientation = 0.0f;

    @Override
    public Vector2 getPosition() {
        return this.position;
    }

    @Override
    public float getOrientation() {
        return this.orientation;
    }

    @Override
    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    @Override
    public Location<Vector2> newLocation() {
        return new Box2dLocation();
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return C4Utils.vectorToAngle(vector);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        return C4Utils.angleToVector(outVector, angle);
    }
}

