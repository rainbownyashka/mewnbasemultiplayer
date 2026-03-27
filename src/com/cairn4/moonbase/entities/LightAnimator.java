/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import box2dLight.Light;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;

public class LightAnimator
extends Entity {
    Light light;
    boolean changingAlpha = false;
    boolean changingRadius = false;
    float startAlpha;
    float endAlpha;
    float startRadius;
    float endRadius;
    float alphaTimer;
    float alphaDuration;
    float radiusTimer;
    float radiusDuration;
    Color lightColor = Color.WHITE;
    public boolean removeWhenFinished = false;

    public LightAnimator(World world, float xPos, float yPos) {
        super(world, xPos, yPos);
    }

    public void setLight(Light light) {
        this.light = light;
    }

    public void setAlpha(float alpha) {
        this.light.getColor().a = alpha;
    }

    public void animFade(float endAlpha, float duration) {
        this.lightColor = this.light.getColor();
        this.startAlpha = this.light.getColor().a;
        this.endAlpha = endAlpha;
        this.alphaDuration = duration;
        this.alphaTimer = 0.0f;
        this.changingAlpha = true;
    }

    public void animDistance(float startRadius, float endRadius, float duration) {
        this.startRadius = startRadius;
        this.endRadius = endRadius;
        this.radiusDuration = duration;
        this.radiusTimer = 0.0f;
        this.changingRadius = true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.light == null) {
            this.readyToRemove = true;
        } else {
            if (this.changingAlpha) {
                this.alphaTimer += delta;
                float a = MathUtils.lerp(this.startAlpha, this.endAlpha, this.alphaTimer / this.alphaDuration);
                if (this.alphaTimer > this.alphaDuration) {
                    this.changingAlpha = false;
                    a = this.endAlpha;
                    if (this.removeWhenFinished) {
                        this.readyToRemove = true;
                        this.light.remove();
                    }
                }
                this.lightColor.a = a;
                this.light.setColor(this.lightColor);
                if (this.lightColor.a == 0.0f && this.light.isActive()) {
                    this.light.setActive(false);
                } else if (this.lightColor.a > 0.0f && !this.light.isActive()) {
                    this.light.setActive(true);
                }
            }
            if (this.changingRadius) {
                this.radiusTimer += delta;
                float r = MathUtils.lerp(this.startRadius, this.endRadius, this.radiusTimer / this.radiusDuration);
                if (this.radiusTimer > this.radiusDuration) {
                    this.changingRadius = false;
                    r = this.endRadius;
                }
                this.light.setDistance(r / 256.0f);
            }
        }
    }

    @Override
    public boolean isSaved() {
        return false;
    }

    @Override
    public float getXPos() {
        if (this.light != null) {
            return this.light.getX() * 256.0f;
        }
        return this.spawnPosX;
    }

    @Override
    public float getYPos() {
        if (this.light != null) {
            return this.light.getY() * 256.0f;
        }
        return this.spawnPosY;
    }
}

