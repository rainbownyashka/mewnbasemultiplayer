/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.Blur;
import com.bitfire.postprocessing.filters.Combine;
import com.bitfire.postprocessing.filters.Threshold;
import com.bitfire.postprocessing.utils.PingPongBuffer;

public final class Bloom
extends PostProcessorEffect {
    private PingPongBuffer pingPongBuffer;
    private Blur blur;
    private Threshold threshold;
    private Combine combine;
    private Settings settings;
    private boolean blending = false;
    private int sfactor;
    private int dfactor;

    public Bloom(int fboWidth, int fboHeight) {
        this.pingPongBuffer = PostProcessor.newPingPongBuffer(fboWidth, fboHeight, PostProcessor.getFramebufferFormat(), false);
        this.blur = new Blur(fboWidth, fboHeight);
        this.threshold = new Threshold();
        this.combine = new Combine();
        this.setSettings(new Settings("default", 2, 0.277f, 1.0f, 0.85f, 1.1f, 0.85f));
    }

    @Override
    public void dispose() {
        this.combine.dispose();
        this.threshold.dispose();
        this.blur.dispose();
        this.pingPongBuffer.dispose();
    }

    public void setBaseIntesity(float intensity) {
        this.combine.setSource1Intensity(intensity);
    }

    public void setBaseSaturation(float saturation) {
        this.combine.setSource1Saturation(saturation);
    }

    public void setBloomIntesity(float intensity) {
        this.combine.setSource2Intensity(intensity);
    }

    public void setBloomSaturation(float saturation) {
        this.combine.setSource2Saturation(saturation);
    }

    public void setThreshold(float gamma) {
        this.threshold.setTreshold(gamma);
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
        this.setThreshold(settings.bloomThreshold);
        this.setBaseIntesity(settings.baseIntensity);
        this.setBaseSaturation(settings.baseSaturation);
        this.setBloomIntesity(settings.bloomIntensity);
        this.setBloomSaturation(settings.bloomSaturation);
        this.setBlurPasses(settings.blurPasses);
        this.setBlurAmount(settings.blurAmount);
        this.setBlurType(settings.blurType);
    }

    public void setBlurPasses(int passes) {
        this.blur.setPasses(passes);
    }

    public void setBlurAmount(float amount) {
        this.blur.setAmount(amount);
    }

    public float getThreshold() {
        return this.threshold.getThreshold();
    }

    public float getBaseIntensity() {
        return this.combine.getSource1Intensity();
    }

    public float getBaseSaturation() {
        return this.combine.getSource1Saturation();
    }

    public float getBloomIntensity() {
        return this.combine.getSource2Intensity();
    }

    public float getBloomSaturation() {
        return this.combine.getSource2Saturation();
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
        ((Threshold)((Threshold)this.threshold.setInput(texsrc)).setOutput(this.pingPongBuffer.getSourceBuffer())).render();
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
        this.threshold.rebind();
        this.combine.rebind();
        this.pingPongBuffer.rebind();
    }

    public static class Settings {
        public final String name;
        public final Blur.BlurType blurType;
        public final int blurPasses;
        public final float blurAmount;
        public final float bloomThreshold;
        public final float bloomIntensity;
        public final float bloomSaturation;
        public final float baseIntensity;
        public final float baseSaturation;

        public Settings(String name, Blur.BlurType blurType, int blurPasses, float blurAmount, float bloomThreshold, float baseIntensity, float baseSaturation, float bloomIntensity, float bloomSaturation) {
            this.name = name;
            this.blurType = blurType;
            this.blurPasses = blurPasses;
            this.blurAmount = blurAmount;
            this.bloomThreshold = bloomThreshold;
            this.baseIntensity = baseIntensity;
            this.baseSaturation = baseSaturation;
            this.bloomIntensity = bloomIntensity;
            this.bloomSaturation = bloomSaturation;
        }

        public Settings(String name, int blurPasses, float bloomThreshold, float baseIntensity, float baseSaturation, float bloomIntensity, float bloomSaturation) {
            this(name, Blur.BlurType.Gaussian5x5b, blurPasses, 0.0f, bloomThreshold, baseIntensity, baseSaturation, bloomIntensity, bloomSaturation);
        }

        public Settings(Settings other) {
            this.name = other.name;
            this.blurType = other.blurType;
            this.blurPasses = other.blurPasses;
            this.blurAmount = other.blurAmount;
            this.bloomThreshold = other.bloomThreshold;
            this.baseIntensity = other.baseIntensity;
            this.baseSaturation = other.baseSaturation;
            this.bloomIntensity = other.bloomIntensity;
            this.bloomSaturation = other.bloomSaturation;
        }
    }
}

