/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.math.Vector2;
import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class FxaaFilter
extends Filter<FxaaFilter> {
    private Vector2 viewportInverse;
    private float FXAA_REDUCE_MIN;
    private float FXAA_REDUCE_MUL;
    private float FXAA_SPAN_MAX;

    public FxaaFilter(int viewportWidth, int viewportHeight) {
        this(new Vector2(viewportWidth, viewportHeight), 0.0078125f, 0.125f, 8.0f);
    }

    public FxaaFilter(int viewportWidth, int viewportHeight, float fxaa_reduce_min, float fxaa_reduce_mul, float fxaa_span_max) {
        this(new Vector2(viewportWidth, viewportHeight), fxaa_reduce_min, fxaa_reduce_mul, fxaa_span_max);
    }

    public FxaaFilter(Vector2 viewportSize, float fxaa_reduce_min, float fxaa_reduce_mul, float fxaa_span_max) {
        super(ShaderLoader.fromFile("screenspace", "fxaa"));
        this.viewportInverse = viewportSize;
        this.viewportInverse.x = 1.0f / this.viewportInverse.x;
        this.viewportInverse.y = 1.0f / this.viewportInverse.y;
        this.FXAA_REDUCE_MIN = fxaa_reduce_min;
        this.FXAA_REDUCE_MUL = fxaa_reduce_mul;
        this.FXAA_SPAN_MAX = fxaa_span_max;
        this.rebind();
    }

    public void setViewportSize(float width, float height) {
        this.viewportInverse.set(1.0f / width, 1.0f / height);
        this.setParam((Filter.Parameter)Param.ViewportInverse, this.viewportInverse);
    }

    public void setFxaaReduceMin(float value) {
        this.FXAA_REDUCE_MIN = value;
        this.setParam((Filter.Parameter)Param.FxaaReduceMin, this.FXAA_REDUCE_MIN);
    }

    public void setFxaaReduceMul(float value) {
        this.FXAA_REDUCE_MUL = value;
        this.setParam((Filter.Parameter)Param.FxaaReduceMul, this.FXAA_REDUCE_MUL);
    }

    public void setFxaaSpanMax(float value) {
        this.FXAA_SPAN_MAX = value;
        this.setParam((Filter.Parameter)Param.FxaaSpanMax, this.FXAA_SPAN_MAX);
    }

    public Vector2 getViewportSize() {
        return this.viewportInverse;
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture, 0);
        this.setParams((Filter.Parameter)Param.ViewportInverse, this.viewportInverse);
        this.setParams((Filter.Parameter)Param.FxaaReduceMin, this.FXAA_REDUCE_MIN);
        this.setParams((Filter.Parameter)Param.FxaaReduceMul, this.FXAA_REDUCE_MUL);
        this.setParams((Filter.Parameter)Param.FxaaSpanMax, this.FXAA_SPAN_MAX);
        this.endParams();
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
    }

    public static enum Param implements Filter.Parameter
    {
        Texture("u_texture0", 0),
        ViewportInverse("u_viewportInverse", 2),
        FxaaReduceMin("FXAA_REDUCE_MIN", 0),
        FxaaReduceMul("FXAA_REDUCE_MUL", 0),
        FxaaSpanMax("FXAA_SPAN_MAX", 0);

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

