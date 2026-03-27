/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.bitfire.postprocessing.filters.Convolve1D;
import com.bitfire.postprocessing.filters.MultipassFilter;
import com.bitfire.postprocessing.utils.PingPongBuffer;

public final class Convolve2D
extends MultipassFilter {
    public final int radius;
    public final int length;
    public final float[] weights;
    public final float[] offsetsHor;
    public final float[] offsetsVert;
    private Convolve1D hor;
    private Convolve1D vert;

    public Convolve2D(int radius) {
        this.radius = radius;
        this.length = radius * 2 + 1;
        this.hor = new Convolve1D(this.length);
        this.vert = new Convolve1D(this.length, this.hor.weights);
        this.weights = this.hor.weights;
        this.offsetsHor = this.hor.offsets;
        this.offsetsVert = this.vert.offsets;
    }

    public void dispose() {
        this.hor.dispose();
        this.vert.dispose();
    }

    public void upload() {
        this.rebind();
    }

    @Override
    public void rebind() {
        this.hor.rebind();
        this.vert.rebind();
    }

    @Override
    public void render(PingPongBuffer buffer) {
        ((Convolve1D)this.hor.setInput(buffer.capture())).render();
        ((Convolve1D)this.vert.setInput(buffer.capture())).render();
    }
}

