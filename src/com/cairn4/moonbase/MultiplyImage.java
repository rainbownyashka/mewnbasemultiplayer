/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class MultiplyImage
extends Image {
    public MultiplyImage(Drawable drawable) {
        super(drawable);
    }

    public MultiplyImage(Texture texture) {
        super(texture);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setBlendFunction(774, 0);
        super.draw(batch, parentAlpha);
        batch.setBlendFunction(770, 771);
    }
}

