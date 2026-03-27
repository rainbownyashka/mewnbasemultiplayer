/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class Convolve1D
extends Filter<Convolve1D> {
    public int length;
    public float[] weights;
    public float[] offsets;

    public Convolve1D(int length) {
        this(length, new float[length], new float[length * 2]);
    }

    public Convolve1D(int length, float[] weights_data) {
        this(length, weights_data, new float[length * 2]);
    }

    public Convolve1D(int length, float[] weights_data, float[] offsets) {
        super(ShaderLoader.fromFile("screenspace", "convolve-1d", "#define LENGTH " + length));
        this.setWeights(length, weights_data, offsets);
        this.rebind();
    }

    public void setWeights(int length, float[] weights, float[] offsets) {
        this.weights = weights;
        this.length = length;
        this.offsets = offsets;
    }

    @Override
    public void dispose() {
        super.dispose();
        this.weights = null;
        this.offsets = null;
        this.length = 0;
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture, 0);
        this.setParamsv(Param.SampleWeights, this.weights, 0, this.length);
        this.setParamsv(Param.SampleOffsets, this.offsets, 0, this.length * 2);
        this.endParams();
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
    }

    public static enum Param implements Filter.Parameter
    {
        Texture("u_texture0", 0),
        SampleWeights("SampleWeights", 1),
        SampleOffsets("SampleOffsets", 2);

        private String mnemonic;
        private int elementSize;

        private Param(String mnemonic, int arrayElementSize) {
            this.mnemonic = mnemonic;
            this.elementSize = arrayElementSize;
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

