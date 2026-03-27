/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class Zoom
extends Filter<Zoom> {
    private float x;
    private float y;
    private float zoom;

    public Zoom() {
        super(ShaderLoader.fromFile("zoom", "zoom"));
        this.rebind();
        this.setOrigin(0.5f, 0.5f);
        this.setZoom(1.0f);
    }

    public void setOrigin(float x, float y) {
        this.x = x;
        this.y = y;
        this.setParams((Filter.Parameter)Param.OffsetX, this.x);
        this.setParams((Filter.Parameter)Param.OffsetY, this.y);
        this.endParams();
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        this.setParam((Filter.Parameter)Param.Zoom, this.zoom);
    }

    public float getZoom() {
        return this.zoom;
    }

    public float getOriginX() {
        return this.x;
    }

    public float getOriginY() {
        return this.y;
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture, 0);
        this.setParams((Filter.Parameter)Param.OffsetX, this.x);
        this.setParams((Filter.Parameter)Param.OffsetY, this.y);
        this.setParams((Filter.Parameter)Param.Zoom, this.zoom);
        this.endParams();
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
    }

    public static enum Param implements Filter.Parameter
    {
        Texture("u_texture0", 0),
        OffsetX("offset_x", 0),
        OffsetY("offset_y", 0),
        Zoom("zoom", 0);

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

