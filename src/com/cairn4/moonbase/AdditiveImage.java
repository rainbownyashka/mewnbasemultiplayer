/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class AdditiveImage
extends Image {
    public AdditiveImage(Drawable drawable) {
        super(drawable);
    }

    public AdditiveImage(Texture texture) {
        super(texture);
    }

    public AdditiveImage(NinePatch ninePatch) {
        super(ninePatch);
    }

    public AdditiveImage(TextureRegion textureRegion) {
        super(textureRegion);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setBlendFunction(770, 1);
        super.draw(batch, parentAlpha);
        batch.setBlendFunction(770, 771);
    }
}

