/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.Vignetting;

public final class Vignette
extends PostProcessorEffect {
    private Vignetting vignetting;
    private boolean controlSaturation;
    private float oneOnW;
    private float oneOnH;

    public Vignette(int viewportWidth, int viewportHeight, boolean controlSaturation) {
        this.controlSaturation = controlSaturation;
        this.oneOnW = 1.0f / (float)viewportWidth;
        this.oneOnH = 1.0f / (float)viewportHeight;
        this.vignetting = new Vignetting(controlSaturation);
    }

    @Override
    public void dispose() {
        this.vignetting.dispose();
    }

    public boolean doesSaturationControl() {
        return this.controlSaturation;
    }

    public void setIntensity(float intensity) {
        this.vignetting.setIntensity(intensity);
    }

    public void setCoords(float x, float y) {
        this.vignetting.setCoords(x, y);
    }

    public void setX(float x) {
        this.vignetting.setX(x);
    }

    public void setY(float y) {
        this.vignetting.setY(y);
    }

    public void setSaturation(float saturation) {
        this.vignetting.setSaturation(saturation);
    }

    public void setSaturationMul(float saturationMul) {
        this.vignetting.setSaturationMul(saturationMul);
    }

    public void setLutTexture(Texture texture) {
        this.vignetting.setLut(texture);
    }

    public void setLutIntensity(float value) {
        this.vignetting.setLutIntensity(value);
    }

    public void setLutIndexVal(int index, int value) {
        this.vignetting.setLutIndexVal(index, value);
    }

    public void setLutIndexOffset(float value) {
        this.vignetting.setLutIndexOffset(value);
    }

    public void setCenter(float x, float y) {
        this.vignetting.setCenter(x * this.oneOnW, 1.0f - y * this.oneOnH);
    }

    public float getIntensity() {
        return this.vignetting.getIntensity();
    }

    public float getLutIntensity() {
        return this.vignetting.getLutIntensity();
    }

    public int getLutIndexVal(int index) {
        return this.vignetting.getLutIndexVal(index);
    }

    public Texture getLut() {
        return this.vignetting.getLut();
    }

    public float getCenterX() {
        return this.vignetting.getCenterX();
    }

    public float getCenterY() {
        return this.vignetting.getCenterY();
    }

    public float getCoordsX() {
        return this.vignetting.getX();
    }

    public float getCoordsY() {
        return this.vignetting.getY();
    }

    public float getSaturation() {
        return this.vignetting.getSaturation();
    }

    public float getSaturationMul() {
        return this.vignetting.getSaturationMul();
    }

    public boolean isGradientMappingEnabled() {
        return this.vignetting.isGradientMappingEnabled();
    }

    @Override
    public void rebind() {
        this.vignetting.rebind();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest) {
        this.restoreViewport(dest);
        ((Vignetting)((Vignetting)this.vignetting.setInput(src)).setOutput(dest)).render();
    }
}

