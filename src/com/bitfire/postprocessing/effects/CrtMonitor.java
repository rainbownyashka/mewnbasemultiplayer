/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.Blur;
import com.bitfire.postprocessing.filters.Combine;
import com.bitfire.postprocessing.filters.CrtScreen;
import com.bitfire.postprocessing.utils.PingPongBuffer;

public final class CrtMonitor
extends PostProcessorEffect {
    private PingPongBuffer pingPongBuffer = null;
    private FrameBuffer buffer = null;
    private CrtScreen crt;
    private Blur blur;
    private Combine combine;
    private boolean doblur;
    private boolean blending = false;
    private int sfactor;
    private int dfactor;

    public CrtMonitor(int fboWidth, int fboHeight, boolean barrelDistortion, boolean performBlur, CrtScreen.RgbMode mode, int effectsSupport) {
        this.doblur = performBlur;
        if (this.doblur) {
            this.pingPongBuffer = PostProcessor.newPingPongBuffer(fboWidth, fboHeight, PostProcessor.getFramebufferFormat(), false);
            this.blur = new Blur(fboWidth, fboHeight);
            this.blur.setPasses(1);
            this.blur.setAmount(1.0f);
            this.blur.setType(Blur.BlurType.Gaussian3x3);
        } else {
            this.buffer = new FrameBuffer(PostProcessor.getFramebufferFormat(), fboWidth, fboHeight, false);
        }
        this.combine = new Combine();
        this.crt = new CrtScreen(barrelDistortion, mode, effectsSupport);
    }

    @Override
    public void dispose() {
        this.crt.dispose();
        this.combine.dispose();
        if (this.doblur) {
            this.blur.dispose();
        }
        if (this.buffer != null) {
            this.buffer.dispose();
        }
        if (this.pingPongBuffer != null) {
            this.pingPongBuffer.dispose();
        }
    }

    public void enableBlending(int sfactor, int dfactor) {
        this.blending = true;
        this.sfactor = sfactor;
        this.dfactor = dfactor;
    }

    public void disableBlending() {
        this.blending = false;
    }

    public void setTime(float elapsedSecs) {
        this.crt.setTime(elapsedSecs);
    }

    public void setColorOffset(float offset) {
        this.crt.setColorOffset(offset);
    }

    public void setChromaticDispersion(float redCyan, float blueYellow) {
        this.crt.setChromaticDispersion(redCyan, blueYellow);
    }

    public void setChromaticDispersionRC(float redCyan) {
        this.crt.setChromaticDispersionRC(redCyan);
    }

    public void setChromaticDispersionBY(float blueYellow) {
        this.crt.setChromaticDispersionBY(blueYellow);
    }

    public void setTint(Color tint) {
        this.crt.setTint(tint);
    }

    public void setTint(float r, float g, float b) {
        this.crt.setTint(r, g, b);
    }

    public void setDistortion(float distortion) {
        this.crt.setDistortion(distortion);
    }

    public void setZoom(float zoom) {
        this.crt.setZoom(zoom);
    }

    public void setRgbMode(CrtScreen.RgbMode mode) {
        this.crt.setRgbMode(mode);
    }

    public Combine getCombinePass() {
        return this.combine;
    }

    public float getOffset() {
        return this.crt.getOffset();
    }

    public Vector2 getChromaticDispersion() {
        return this.crt.getChromaticDispersion();
    }

    public float getZoom() {
        return this.crt.getZoom();
    }

    public Color getTint() {
        return this.crt.getTint();
    }

    public CrtScreen.RgbMode getRgbMode() {
        return this.crt.getRgbMode();
    }

    @Override
    public void rebind() {
        this.crt.rebind();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest) {
        Texture in = (Texture)src.getColorBufferTexture();
        boolean blendingWasEnabled = PostProcessor.isStateEnabled(3042);
        Gdx.gl.glDisable(3042);
        Texture out = null;
        if (this.doblur) {
            this.pingPongBuffer.begin();
            ((CrtScreen)((CrtScreen)this.crt.setInput(in)).setOutput(this.pingPongBuffer.getSourceBuffer())).render();
            this.blur.render(this.pingPongBuffer);
            this.pingPongBuffer.end();
            out = this.pingPongBuffer.getResultTexture();
        } else {
            ((CrtScreen)((CrtScreen)this.crt.setInput(in)).setOutput(this.buffer)).render();
            out = (Texture)this.buffer.getColorBufferTexture();
        }
        if (this.blending || blendingWasEnabled) {
            Gdx.gl.glEnable(3042);
        }
        if (this.blending) {
            Gdx.gl.glBlendFunc(this.sfactor, this.dfactor);
        }
        this.restoreViewport(dest);
        ((Combine)this.combine.setOutput(dest)).setInput(in, out).render();
    }
}

