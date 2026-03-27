/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.Lens;

public final class LensFlare
extends PostProcessorEffect {
    private Lens lens = null;

    public LensFlare(int viewportWidth, int viewportHeight) {
        this.setup(viewportWidth, viewportHeight);
    }

    private void setup(int viewportWidth, int viewportHeight) {
        this.lens = new Lens(viewportWidth, viewportHeight);
    }

    public void setIntensity(float intensity) {
        this.lens.setIntensity(intensity);
    }

    public float getIntensity() {
        return this.lens.getIntensity();
    }

    public void setColor(float r, float g, float b) {
        this.lens.setColor(r, g, b);
    }

    public void setLightPosition(float x, float y) {
        this.lens.setLightPosition(x, y);
    }

    @Override
    public void dispose() {
        if (this.lens != null) {
            this.lens.dispose();
            this.lens = null;
        }
    }

    @Override
    public void rebind() {
        this.lens.rebind();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest) {
        this.restoreViewport(dest);
        ((Lens)((Lens)this.lens.setInput(src)).setOutput(dest)).render();
    }
}

