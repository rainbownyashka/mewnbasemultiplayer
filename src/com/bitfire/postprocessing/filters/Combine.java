/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class Combine
extends Filter<Combine> {
    private float s1i = 1.0f;
    private float s1s = 1.0f;
    private float s2i = 1.0f;
    private float s2s = 1.0f;
    private Texture inputTexture2 = null;

    public Combine() {
        super(ShaderLoader.fromFile("screenspace", "combine"));
        this.rebind();
    }

    public Combine setInput(FrameBuffer buffer1, FrameBuffer buffer2) {
        this.inputTexture = (Texture)buffer1.getColorBufferTexture();
        this.inputTexture2 = (Texture)buffer2.getColorBufferTexture();
        return this;
    }

    public Combine setInput(Texture texture1, Texture texture2) {
        this.inputTexture = texture1;
        this.inputTexture2 = texture2;
        return this;
    }

    public void setSource1Intensity(float intensity) {
        this.s1i = intensity;
        this.setParam((Filter.Parameter)Param.Source1Intensity, intensity);
    }

    public void setSource2Intensity(float intensity) {
        this.s2i = intensity;
        this.setParam((Filter.Parameter)Param.Source2Intensity, intensity);
    }

    public void setSource1Saturation(float saturation) {
        this.s1s = saturation;
        this.setParam((Filter.Parameter)Param.Source1Saturation, saturation);
    }

    public void setSource2Saturation(float saturation) {
        this.s2s = saturation;
        this.setParam((Filter.Parameter)Param.Source2Saturation, saturation);
    }

    public float getSource1Intensity() {
        return this.s1i;
    }

    public float getSource2Intensity() {
        return this.s2i;
    }

    public float getSource1Saturation() {
        return this.s1s;
    }

    public float getSource2Saturation() {
        return this.s2s;
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture0, 0);
        this.setParams((Filter.Parameter)Param.Texture1, 1);
        this.setParams((Filter.Parameter)Param.Source1Intensity, this.s1i);
        this.setParams((Filter.Parameter)Param.Source2Intensity, this.s2i);
        this.setParams((Filter.Parameter)Param.Source1Saturation, this.s1s);
        this.setParams((Filter.Parameter)Param.Source2Saturation, this.s2s);
        this.endParams();
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
        this.inputTexture2.bind(1);
    }

    public static enum Param implements Filter.Parameter
    {
        Texture0("u_texture0", 0),
        Texture1("u_texture1", 0),
        Source1Intensity("Src1Intensity", 0),
        Source1Saturation("Src1Saturation", 0),
        Source2Intensity("Src2Intensity", 0),
        Source2Saturation("Src2Saturation", 0);

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

