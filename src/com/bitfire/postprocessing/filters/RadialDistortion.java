/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class RadialDistortion
extends Filter<RadialDistortion> {
    private float zoom;
    private float distortion;

    public RadialDistortion() {
        super(ShaderLoader.fromFile("screenspace", "radial-distortion"));
        this.rebind();
        this.setDistortion(0.3f);
        this.setZoom(1.0f);
    }

    public void setDistortion(float distortion) {
        this.distortion = distortion;
        this.setParam((Filter.Parameter)Param.Distortion, this.distortion);
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        this.setParam((Filter.Parameter)Param.Zoom, this.zoom);
    }

    public float getDistortion() {
        return this.distortion;
    }

    public float getZoom() {
        return this.zoom;
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture0, 0);
        this.setParams((Filter.Parameter)Param.Distortion, this.distortion);
        this.setParams((Filter.Parameter)Param.Zoom, this.zoom);
        this.endParams();
    }

    public static enum Param implements Filter.Parameter
    {
        Texture0("u_texture0", 0),
        Distortion("distortion", 0),
        Zoom("zoom", 0);

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

