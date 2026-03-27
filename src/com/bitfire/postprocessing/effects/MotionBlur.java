/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.Copy;
import com.bitfire.postprocessing.filters.MotionFilter;

public class MotionBlur
extends PostProcessorEffect {
    private MotionFilter motionFilter = new MotionFilter();
    private Copy copyFilter = new Copy();
    private FrameBuffer fbo;

    public void setBlurOpacity(float blurOpacity) {
        this.motionFilter.setBlurOpacity(blurOpacity);
    }

    @Override
    public void dispose() {
        if (this.motionFilter != null) {
            this.motionFilter.dispose();
            this.motionFilter = null;
        }
    }

    @Override
    public void rebind() {
        this.motionFilter.rebind();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest) {
        this.restoreViewport(dest);
        if (dest != null) {
            ((MotionFilter)((MotionFilter)this.motionFilter.setInput(src)).setOutput(dest)).render();
            this.fbo = dest;
        } else {
            if (this.fbo == null) {
                this.fbo = new FrameBuffer(Pixmap.Format.RGBA8888, src.getWidth(), src.getHeight(), false);
            }
            ((MotionFilter)((MotionFilter)this.motionFilter.setInput(src)).setOutput(this.fbo)).render();
            ((Copy)((Copy)this.copyFilter.setInput(this.fbo)).setOutput(dest)).render();
        }
        this.motionFilter.setLastFrameTexture((Texture)this.fbo.getColorBufferTexture());
    }
}

