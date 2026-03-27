/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class Threshold
extends Filter<Threshold> {
    private float gamma = 0.0f;

    public Threshold() {
        super(ShaderLoader.fromFile("screenspace", "threshold"));
        this.rebind();
    }

    public void setTreshold(float gamma) {
        this.gamma = gamma;
        this.setParams((Filter.Parameter)Param.Threshold, gamma);
        ((Threshold)this.setParams((Filter.Parameter)Param.ThresholdInvTx, 1.0f / (1.0f - gamma))).endParams();
    }

    public float getThreshold() {
        return this.gamma;
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture, 0);
        this.setTreshold(this.gamma);
    }

    public static enum Param implements Filter.Parameter
    {
        Texture("u_texture0", 0),
        Threshold("treshold", 0),
        ThresholdInvTx("tresholdInvTx", 0);

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

