/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.cairn4.moonbase.Light;
import com.cairn4.moonbase.Lights;

public class DirectionalLight
extends Light {
    float rotation = 0.0f;

    public DirectionalLight(float x, float y, int offsetX, int offsetY, float size, Lights owner) {
        super(x, y, offsetX, offsetY, size, owner);
    }

    public void setRotation(float degrees) {
        this.rotation = degrees;
    }

    @Override
    public void draw(SpriteBatch lightBatch) {
        lightBatch.draw(this.owner.gameScreen.skin.getRegion("flashlight"), this.getX() - this.getSize() / 2.0f, this.getY() - this.getSize() / 2.0f, this.getSize() / 2.0f, this.getSize() / 2.0f, this.getSize(), this.getSize(), 1.0f, 1.0f, this.rotation);
    }
}

