/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cairn4.moonbase.Lights;

public class Light {
    private float x;
    private float y;
    protected Lights owner;
    private boolean on;
    private Color color;
    protected float size;
    int offsetX;
    int offsetY;
    private float fade = 0.0f;

    public Light(float x, float y, int offsetX, int offsetY, float size, Lights owner) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.owner = owner;
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.setOn(true);
    }

    public void setOn(boolean value) {
        if (value != this.on) {
            this.on = value;
            if (this.on) {
                this.owner.addLight(this);
            } else {
                this.owner.removeLight(this);
            }
        }
    }

    public void setX(float worldX) {
        this.x = worldX;
    }

    public void setY(float worldY) {
        this.y = worldY;
    }

    public void setColor(Color color) {
        this.color.set(color.r, color.g, color.b, color.a);
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public boolean isOn() {
        return this.on;
    }

    public Color getColor() {
        return this.color;
    }

    public float getSize() {
        return this.size;
    }

    public void setPosition(float x, float y) {
        this.x = x + (float)this.offsetX;
        this.y = y + (float)this.offsetY;
    }

    public void setFade(float fade) {
        this.fade = fade;
    }

    public void update() {
        if (this.fade != 0.0f) {
            this.color.a *= 1.0f - this.fade;
            if ((double)this.color.a < 0.01) {
                this.setOn(false);
            }
        }
    }

    public void draw(SpriteBatch lightBatch) {
        lightBatch.draw(this.owner.lightTexture, this.getX() - this.getSize() / 2.0f, this.getY() - this.getSize() / 2.0f, this.getSize(), this.getSize(), 0, 0, 128, 128, false, false);
    }
}

