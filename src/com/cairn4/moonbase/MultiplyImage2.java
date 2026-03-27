/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class MultiplyImage2
extends Image {
    public static int srcFunc;
    public static int dstFunc;

    public static void setSrc(String src) {
        switch (src) {
            default: 
        }
    }

    public MultiplyImage2(Drawable drawable) {
        super(drawable);
    }

    public MultiplyImage2(Texture texture) {
        super(texture);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setBlendFunction(774, 771);
        super.draw(batch, parentAlpha);
        batch.setBlendFunction(770, 771);
    }
}

