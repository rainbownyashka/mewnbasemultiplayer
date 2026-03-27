/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.vertexeffects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.utils.SpineUtils;

public class SwirlEffect
implements SkeletonRenderer.VertexEffect {
    private float worldX;
    private float worldY;
    private float radius;
    private float angle;
    private Interpolation interpolation = Interpolation.pow2Out;
    private float centerX;
    private float centerY;

    public SwirlEffect(float radius) {
        this.radius = radius;
    }

    @Override
    public void begin(Skeleton skeleton) {
        this.worldX = skeleton.getX() + this.centerX;
        this.worldY = skeleton.getY() + this.centerY;
    }

    @Override
    public void transform(Vector2 position, Vector2 uv, Color light, Color dark) {
        float x = position.x - this.worldX;
        float y = position.y - this.worldY;
        float dist = (float)Math.sqrt(x * x + y * y);
        if (dist < this.radius) {
            float theta = this.interpolation.apply(0.0f, this.angle, (this.radius - dist) / this.radius);
            float cos = SpineUtils.cos(theta);
            float sin = SpineUtils.sin(theta);
            position.x = cos * x - sin * y + this.worldX;
            position.y = sin * x + cos * y + this.worldY;
        }
    }

    @Override
    public void end() {
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setCenter(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }

    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }

    public void setAngle(float degrees) {
        this.angle = degrees * ((float)Math.PI / 180);
    }

    public Interpolation getInterpolation() {
        return this.interpolation;
    }

    public void setInterpolation(Interpolation interpolation) {
        this.interpolation = interpolation;
    }
}

