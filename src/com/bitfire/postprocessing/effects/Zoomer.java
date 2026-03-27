/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.RadialBlur;
import com.bitfire.postprocessing.filters.Zoom;

public final class Zoomer
extends PostProcessorEffect {
    private boolean doRadial = false;
    private RadialBlur radialBlur = null;
    private Zoom zoom = null;
    private float oneOnW;
    private float oneOnH;
    private float userOriginX;
    private float userOriginY;

    public Zoomer(int viewportWidth, int viewportHeight, RadialBlur.Quality quality) {
        this.setup(viewportWidth, viewportHeight, new RadialBlur(quality));
    }

    public Zoomer(int viewportWidth, int viewportHeight) {
        this.setup(viewportWidth, viewportHeight, null);
    }

    private void setup(int viewportWidth, int viewportHeight, RadialBlur radialBlurFilter) {
        this.radialBlur = radialBlurFilter;
        if (this.radialBlur != null) {
            this.doRadial = true;
            this.zoom = null;
        } else {
            this.doRadial = false;
            this.zoom = new Zoom();
        }
        this.oneOnW = 1.0f / (float)viewportWidth;
        this.oneOnH = 1.0f / (float)viewportHeight;
    }

    public void setOrigin(Vector2 o) {
        this.setOrigin(o.x, o.y);
    }

    public void setOrigin(float x, float y) {
        this.userOriginX = x;
        this.userOriginY = y;
        if (this.doRadial) {
            this.radialBlur.setOrigin(x * this.oneOnW, 1.0f - y * this.oneOnH);
        } else {
            this.zoom.setOrigin(x * this.oneOnW, 1.0f - y * this.oneOnH);
        }
    }

    public void setBlurStrength(float strength) {
        if (this.doRadial) {
            this.radialBlur.setStrength(strength);
        }
    }

    public void setZoom(float zoom) {
        if (this.doRadial) {
            this.radialBlur.setZoom(1.0f / zoom);
        } else {
            this.zoom.setZoom(1.0f / zoom);
        }
    }

    public float getZoom() {
        if (this.doRadial) {
            return 1.0f / this.radialBlur.getZoom();
        }
        return 1.0f / this.zoom.getZoom();
    }

    public float getBlurStrength() {
        if (this.doRadial) {
            return this.radialBlur.getStrength();
        }
        return -1.0f;
    }

    public float getOriginX() {
        return this.userOriginX;
    }

    public float getOriginY() {
        return this.userOriginY;
    }

    @Override
    public void dispose() {
        if (this.radialBlur != null) {
            this.radialBlur.dispose();
            this.radialBlur = null;
        }
        if (this.zoom != null) {
            this.zoom.dispose();
            this.zoom = null;
        }
    }

    @Override
    public void rebind() {
        this.radialBlur.rebind();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest) {
        this.restoreViewport(dest);
        if (this.doRadial) {
            ((RadialBlur)((RadialBlur)this.radialBlur.setInput(src)).setOutput(dest)).render();
        } else {
            ((Zoom)((Zoom)this.zoom.setInput(src)).setOutput(dest)).render();
        }
    }
}

