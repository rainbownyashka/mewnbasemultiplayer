/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.effects.Antialiasing;
import com.bitfire.postprocessing.filters.FxaaFilter;

public final class Fxaa
extends Antialiasing {
    private FxaaFilter fxaaFilter = null;

    public Fxaa(int viewportWidth, int viewportHeight) {
        this.setup(viewportWidth, viewportHeight);
    }

    private void setup(int viewportWidth, int viewportHeight) {
        this.fxaaFilter = new FxaaFilter(viewportWidth, viewportHeight);
    }

    @Override
    public void setViewportSize(int width, int height) {
        this.fxaaFilter.setViewportSize(width, height);
    }

    public void setSpanMax(float value) {
        this.fxaaFilter.setFxaaSpanMax(value);
    }

    @Override
    public void dispose() {
        if (this.fxaaFilter != null) {
            this.fxaaFilter.dispose();
            this.fxaaFilter = null;
        }
    }

    @Override
    public void rebind() {
        this.fxaaFilter.rebind();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest) {
        this.restoreViewport(dest);
        ((FxaaFilter)((FxaaFilter)this.fxaaFilter.setInput(src)).setOutput(dest)).render();
    }
}

