/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.attachments;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.attachments.Attachment;

public class PointAttachment
extends Attachment {
    float x;
    float y;
    float rotation;
    final Color color = new Color(0.9451f, 0.9451f, 0.0f, 1.0f);

    public PointAttachment(String name) {
        super(name);
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getRotation() {
        return this.rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public Color getColor() {
        return this.color;
    }

    public Vector2 computeWorldPosition(Bone bone, Vector2 point) {
        point.x = this.x * bone.getA() + this.y * bone.getB() + bone.getWorldX();
        point.y = this.x * bone.getC() + this.y * bone.getD() + bone.getWorldY();
        return point;
    }

    public float computeWorldRotation(Bone bone) {
        float cos = MathUtils.cosDeg(this.rotation);
        float sin = MathUtils.sinDeg(this.rotation);
        float x = cos * bone.getA() + sin * bone.getB();
        float y = cos * bone.getC() + sin * bone.getD();
        return (float)Math.atan2(y, x) * 57.295776f;
    }

    @Override
    public Attachment copy() {
        PointAttachment copy = new PointAttachment(this.name);
        copy.x = this.x;
        copy.y = this.y;
        copy.rotation = this.rotation;
        copy.color.set(this.color);
        return copy;
    }
}

