/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.graphics.Texture;
import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class Vignetting
extends Filter<Vignetting> {
    private float x;
    private float y;
    private float intensity;
    private float saturation;
    private float saturationMul;
    private Texture texLut;
    private boolean dolut = false;
    private boolean dosat;
    private float lutintensity;
    private int[] lutindex;
    private float lutStep;
    private float lutStepOffset;
    private float lutIndexOffset;
    private float centerX;
    private float centerY;

    public Vignetting(boolean controlSaturation) {
        super(ShaderLoader.fromFile("screenspace", "vignetting", controlSaturation ? "#define CONTROL_SATURATION\n#define ENABLE_GRADIENT_MAPPING" : "#define ENABLE_GRADIENT_MAPPING"));
        this.dosat = controlSaturation;
        this.texLut = null;
        this.lutindex = new int[2];
        this.lutindex[0] = -1;
        this.lutindex[1] = -1;
        this.lutintensity = 1.0f;
        this.lutIndexOffset = 0.0f;
        this.rebind();
        this.setCoords(0.8f, 0.25f);
        this.setCenter(0.5f, 0.5f);
        this.setIntensity(1.0f);
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
        this.setParam((Filter.Parameter)Param.VignetteIntensity, intensity);
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
        if (this.dosat) {
            this.setParam((Filter.Parameter)Param.Saturation, saturation);
        }
    }

    public void setSaturationMul(float saturationMul) {
        this.saturationMul = saturationMul;
        if (this.dosat) {
            this.setParam((Filter.Parameter)Param.SaturationMul, saturationMul);
        }
    }

    public void setCoords(float x, float y) {
        this.x = x;
        this.y = y;
        this.setParams((Filter.Parameter)Param.VignetteX, x);
        this.setParams((Filter.Parameter)Param.VignetteY, y);
        this.endParams();
    }

    public void setX(float x) {
        this.x = x;
        this.setParam((Filter.Parameter)Param.VignetteX, x);
    }

    public void setY(float y) {
        this.y = y;
        this.setParam((Filter.Parameter)Param.VignetteY, y);
    }

    public void setLut(Texture texture) {
        this.texLut = texture;
        boolean bl = this.dolut = this.texLut != null;
        if (this.dolut) {
            this.lutStep = 1.0f / (float)texture.getHeight();
            this.lutStepOffset = this.lutStep / 2.0f;
            this.setParams((Filter.Parameter)Param.TexLUT, 1);
            this.setParams((Filter.Parameter)Param.LutStep, this.lutStep);
            ((Vignetting)this.setParams((Filter.Parameter)Param.LutStepOffset, this.lutStepOffset)).endParams();
        }
    }

    public void setLutIntensity(float value) {
        this.lutintensity = value;
        this.setParam((Filter.Parameter)Param.LutIntensity, this.lutintensity);
    }

    public void setLutIndexVal(int index, int value) {
        this.lutindex[index] = value;
        switch (index) {
            case 0: {
                this.setParam((Filter.Parameter)Param.LutIndex, this.lutindex[0]);
                break;
            }
            case 1: {
                this.setParam((Filter.Parameter)Param.LutIndex2, this.lutindex[1]);
            }
        }
    }

    public void setLutIndexOffset(float value) {
        this.lutIndexOffset = value;
        this.setParam((Filter.Parameter)Param.LutIndexOffset, this.lutIndexOffset);
    }

    public void setCenter(float x, float y) {
        this.centerX = x;
        this.centerY = y;
        this.setParams((Filter.Parameter)Param.CenterX, this.centerX);
        ((Vignetting)this.setParams((Filter.Parameter)Param.CenterY, this.centerY)).endParams();
    }

    public float getCenterX() {
        return this.centerX;
    }

    public float getCenterY() {
        return this.centerY;
    }

    public int getLutIndexVal(int index) {
        return this.lutindex[index];
    }

    public float getLutIntensity() {
        return this.lutintensity;
    }

    public Texture getLut() {
        return this.texLut;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getIntensity() {
        return this.intensity;
    }

    public float getSaturation() {
        return this.saturation;
    }

    public float getSaturationMul() {
        return this.saturationMul;
    }

    public boolean isGradientMappingEnabled() {
        return this.dolut;
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture0, 0);
        this.setParams((Filter.Parameter)Param.LutIndex, this.lutindex[0]);
        this.setParams((Filter.Parameter)Param.LutIndex2, this.lutindex[1]);
        this.setParams((Filter.Parameter)Param.LutIndexOffset, this.lutIndexOffset);
        this.setParams((Filter.Parameter)Param.TexLUT, 1);
        this.setParams((Filter.Parameter)Param.LutIntensity, this.lutintensity);
        this.setParams((Filter.Parameter)Param.LutStep, this.lutStep);
        this.setParams((Filter.Parameter)Param.LutStepOffset, this.lutStepOffset);
        if (this.dosat) {
            this.setParams((Filter.Parameter)Param.Saturation, this.saturation);
            this.setParams((Filter.Parameter)Param.SaturationMul, this.saturationMul);
        }
        this.setParams((Filter.Parameter)Param.VignetteIntensity, this.intensity);
        this.setParams((Filter.Parameter)Param.VignetteX, this.x);
        this.setParams((Filter.Parameter)Param.VignetteY, this.y);
        this.setParams((Filter.Parameter)Param.CenterX, this.centerX);
        this.setParams((Filter.Parameter)Param.CenterY, this.centerY);
        this.endParams();
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
        if (this.dolut) {
            this.texLut.bind(1);
        }
    }

    public static enum Param implements Filter.Parameter
    {
        Texture0("u_texture0", 0),
        TexLUT("u_texture1", 0),
        VignetteIntensity("VignetteIntensity", 0),
        VignetteX("VignetteX", 0),
        VignetteY("VignetteY", 0),
        Saturation("Saturation", 0),
        SaturationMul("SaturationMul", 0),
        LutIntensity("LutIntensity", 0),
        LutIndex("LutIndex", 0),
        LutIndex2("LutIndex2", 0),
        LutIndexOffset("LutIndexOffset", 0),
        LutStep("LutStep", 0),
        LutStepOffset("LutStepOffset", 0),
        CenterX("CenterX", 0),
        CenterY("CenterY", 0);

        private final String mnemonic;
        private int elementSize;

        private Param(String m, int elementSize) {
            this.mnemonic = m;
            this.elementSize = elementSize;
        }

        @Override
        public String mnemonic() {
            return this.mnemonic;
        }

        @Override
        public int arrayElementSize() {
            return this.elementSize;
        }
    }
}

