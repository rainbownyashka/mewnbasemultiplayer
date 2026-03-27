/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.graphics.Texture;
import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public class MotionFilter
extends Filter<MotionFilter> {
    private float blurOpacity = 0.5f;
    private Texture lastFrameTex;

    public MotionFilter() {
        super(ShaderLoader.fromFile("screenspace", "motionblur"));
        this.rebind();
    }

    public void setBlurOpacity(float blurOpacity) {
        this.blurOpacity = blurOpacity;
        this.setParam((Filter.Parameter)Param.BlurOpacity, this.blurOpacity);
    }

    public void setLastFrameTexture(Texture tex) {
        this.lastFrameTex = tex;
        if (this.lastFrameTex != null) {
            this.setParam((Filter.Parameter)Param.LastFrame, 1);
        }
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture, 0);
        if (this.lastFrameTex != null) {
            this.setParams((Filter.Parameter)Param.LastFrame, 1);
        }
        this.setParams((Filter.Parameter)Param.BlurOpacity, this.blurOpacity);
        this.endParams();
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
        if (this.lastFrameTex != null) {
            this.lastFrameTex.bind(1);
        }
    }

    public static enum Param implements Filter.Parameter
    {
        Texture("u_texture0", 0),
        LastFrame("u_texture1", 0),
        BlurOpacity("u_blurOpacity", 0);

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

