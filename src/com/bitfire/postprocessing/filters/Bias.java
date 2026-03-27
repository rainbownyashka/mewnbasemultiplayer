/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class Bias
extends Filter<Bias> {
    private float bias;

    public Bias() {
        super(ShaderLoader.fromFile("screenspace", "bias"));
        this.rebind();
    }

    public void setBias(float bias) {
        this.bias = bias;
        this.setParam((Filter.Parameter)Param.Bias, this.bias);
    }

    public float getBias() {
        return this.bias;
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture, 0);
        this.setBias(this.bias);
    }

    public static enum Param implements Filter.Parameter
    {
        Texture("u_texture0", 0),
        Bias("u_bias", 0);

        private String mnemonic;
        private int elementSize;

        private Param(String mnemonic, int elementSize) {
            this.mnemonic = mnemonic;
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

