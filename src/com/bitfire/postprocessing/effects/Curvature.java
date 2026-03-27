/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.RadialDistortion;

public final class Curvature
extends PostProcessorEffect {
    private RadialDistortion distort = new RadialDistortion();

    @Override
    public void dispose() {
        this.distort.dispose();
    }

    public void setDistortion(float distortion) {
        this.distort.setDistortion(distortion);
    }

    public void setZoom(float zoom) {
        this.distort.setZoom(zoom);
    }

    public float getDistortion() {
        return this.distort.getDistortion();
    }

    public float getZoom() {
        return this.distort.getZoom();
    }

    @Override
    public void rebind() {
        this.distort.rebind();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest) {
        this.restoreViewport(dest);
        ((RadialDistortion)((RadialDistortion)this.distort.setInput(src)).setOutput(dest)).render();
    }
}

