/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.Bias;
import com.bitfire.postprocessing.filters.Blur;
import com.bitfire.postprocessing.filters.Combine;
import com.bitfire.postprocessing.filters.Lens2;
import com.bitfire.postprocessing.utils.PingPongBuffer;

public final class LensFlare2
extends PostProcessorEffect {
    private PingPongBuffer pingPongBuffer;
    private Lens2 lens;
    private Blur blur;
    private Bias bias;
    private Combine combine;
    private Settings settings;
    private boolean blending = false;
    private int sfactor;
    private int dfactor;

    public LensFlare2(int fboWidth, int fboHeight) {
        this.pingPongBuffer = PostProcessor.newPingPongBuffer(fboWidth, fboHeight, PostProcessor.getFramebufferFormat(), false);
        this.lens = new Lens2(fboWidth, fboHeight);
        this.blur = new Blur(fboWidth, fboHeight);
        this.bias = new Bias();
        this.combine = new Combine();
        this.setSettings(new Settings("default", 2, -0.9f, 1.0f, 1.0f, 0.7f, 1.0f, 8, 0.5f));
    }

    @Override
    public void dispose() {
        this.combine.dispose();
        this.bias.dispose();
        this.blur.dispose();
        this.pingPongBuffer.dispose();
    }

    public void setBaseIntesity(float intensity) {
        this.combine.setSource1Intensity(intensity);
    }

    public void setBaseSaturation(float saturation) {
        this.combine.setSource1Saturation(saturation);
    }

    public void setFlareIntesity(float intensity) {
        this.combine.setSource2Intensity(intensity);
    }

    public void setFlareSaturation(float saturation) {
        this.combine.setSource2Saturation(saturation);
    }

    public void setBias(float b) {
        this.bias.setBias(b);
    }

    public void setGhosts(int ghosts) {
        this.lens.setGhosts(ghosts);
    }

    public void setHaloWidth(float haloWidth) {
        this.lens.setHaloWidth(haloWidth);
    }

    public void setLensColorTexture(Texture tex) {
        this.lens.setLensColorTexture(tex);
    }

    public void enableBlending(int sfactor, int dfactor) {
        this.blending = true;
        this.sfactor = sfactor;
        this.dfactor = dfactor;
    }

    public void disableBlending() {
        this.blending = false;
    }

    public void setBlurType(Blur.BlurType type) {
        this.blur.setType(type);
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        this.setBias(settings.flareBias);
        this.setBaseIntesity(settings.baseIntensity);
        this.setBaseSaturation(settings.baseSaturation);
        this.setFlareIntesity(settings.flareIntensity);
        this.setFlareSaturation(settings.flareSaturation);
        this.setBlurPasses(settings.blurPasses);
        this.setBlurAmount(settings.blurAmount);
        this.setBlurType(settings.blurType);
        this.setGhosts(settings.ghosts);
    }

    public void setBlurPasses(int passes) {
        this.blur.setPasses(passes);
    }

    public void setBlurAmount(float amount) {
        this.blur.setAmount(amount);
    }

    public float getBias() {
        return this.bias.getBias();
    }

    public float getBaseIntensity() {
        return this.combine.getSource1Intensity();
    }

    public float getBaseSaturation() {
        return this.combine.getSource1Saturation();
    }

    public float getFlareIntensity() {
        return this.combine.getSource2Intensity();
    }

    public float getFlareSaturation() {
        return this.combine.getSource2Saturation();
    }

    public int getGhosts() {
        return this.lens.getGhosts();
    }

    public boolean isBlendingEnabled() {
        return this.blending;
    }

    public int getBlendingSourceFactor() {
        return this.sfactor;
    }

    public int getBlendingDestFactor() {
        return this.dfactor;
    }

    public Blur.BlurType getBlurType() {
        return this.blur.getType();
    }

    public Settings getSettings() {
        return this.settings;
    }

    public int getBlurPasses() {
        return this.blur.getPasses();
    }

    public float getBlurAmount() {
        return this.blur.getAmount();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest) {
        Texture texsrc = (Texture)src.getColorBufferTexture();
        boolean blendingWasEnabled = PostProcessor.isStateEnabled(3042);
        Gdx.gl.glDisable(3042);
        this.pingPongBuffer.begin();
        ((Bias)((Bias)this.bias.setInput(texsrc)).setOutput(this.pingPongBuffer.getSourceBuffer())).render();
        ((Lens2)((Lens2)this.lens.setInput(this.pingPongBuffer.getSourceBuffer())).setOutput(this.pingPongBuffer.getResultBuffer())).render();
        this.pingPongBuffer.set(this.pingPongBuffer.getResultBuffer(), this.pingPongBuffer.getSourceBuffer());
        this.blur.render(this.pingPongBuffer);
        this.pingPongBuffer.end();
        if (this.blending || blendingWasEnabled) {
            Gdx.gl.glEnable(3042);
        }
        if (this.blending) {
            Gdx.gl.glBlendFunc(this.sfactor, this.dfactor);
        }
        this.restoreViewport(dest);
        ((Combine)this.combine.setOutput(dest)).setInput(texsrc, this.pingPongBuffer.getResultTexture()).render();
    }

    @Override
    public void rebind() {
        this.blur.rebind();
        this.bias.rebind();
        this.combine.rebind();
        this.pingPongBuffer.rebind();
    }

    public static class Settings {
        public final String name;
        public final Blur.BlurType blurType;
        public final int blurPasses;
        public final float blurAmount;
        public final float flareBias;
        public final float flareIntensity;
        public final float flareSaturation;
        public final float baseIntensity;
        public final float baseSaturation;
        public final int ghosts;
        public final float haloWidth;

        public Settings(String name, Blur.BlurType blurType, int blurPasses, float blurAmount, float flareBias, float baseIntensity, float baseSaturation, float flareIntensity, float flareSaturation, int ghosts, float haloWidth) {
            this.name = name;
            this.blurType = blurType;
            this.blurPasses = blurPasses;
            this.blurAmount = blurAmount;
            this.flareBias = flareBias;
            this.baseIntensity = baseIntensity;
            this.baseSaturation = baseSaturation;
            this.flareIntensity = flareIntensity;
            this.flareSaturation = flareSaturation;
            this.ghosts = ghosts;
            this.haloWidth = haloWidth;
        }

        public Settings(String name, int blurPasses, float flareBias, float baseIntensity, float baseSaturation, float flareIntensity, float flareSaturation, int ghosts, float haloWidth) {
            this(name, Blur.BlurType.Gaussian5x5b, blurPasses, 0.0f, flareBias, baseIntensity, baseSaturation, flareIntensity, flareSaturation, ghosts, haloWidth);
        }

        public Settings(Settings other) {
            this.name = other.name;
            this.blurType = other.blurType;
            this.blurPasses = other.blurPasses;
            this.blurAmount = other.blurAmount;
            this.flareBias = other.flareBias;
            this.baseIntensity = other.baseIntensity;
            this.baseSaturation = other.baseSaturation;
            this.flareIntensity = other.flareIntensity;
            this.flareSaturation = other.flareSaturation;
            this.ghosts = other.ghosts;
            this.haloWidth = other.haloWidth;
        }
    }
}

