/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class RadialBlur
extends Filter<RadialBlur> {
    private int blur_len;
    private float strength;
    private float x;
    private float y;
    private float zoom;

    public RadialBlur(Quality quality) {
        super(ShaderLoader.fromFile("radial-blur", "radial-blur", "#define BLUR_LENGTH " + quality.length + "\n#define ONE_ON_BLUR_LENGTH " + 1.0f / (float)quality.length));
        this.blur_len = quality.length;
        this.rebind();
        this.setOrigin(0.5f, 0.5f);
        this.setStrength(0.5f);
        this.setZoom(1.0f);
    }

    public RadialBlur() {
        this(Quality.Low);
    }

    public void setOrigin(float x, float y) {
        this.x = x;
        this.y = y;
        this.setParams((Filter.Parameter)Param.OffsetX, x);
        this.setParams((Filter.Parameter)Param.OffsetY, y);
        this.endParams();
    }

    public void setStrength(float strength) {
        this.strength = strength;
        this.setParam((Filter.Parameter)Param.BlurDiv, strength / (float)this.blur_len);
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

    public float getStrength() {
        return this.strength;
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture, 0);
        this.setParams((Filter.Parameter)Param.BlurDiv, this.strength / (float)this.blur_len);
        this.setParams((Filter.Parameter)Param.OffsetX, this.x);
        this.setParams((Filter.Parameter)Param.OffsetY, this.y);
        this.setParams((Filter.Parameter)Param.Zoom, this.zoom);
        this.endParams();
    }

    public static enum Param implements Filter.Parameter
    {
        Texture("u_texture0", 0),
        BlurDiv("blur_div", 0),
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

    public static enum Quality {
        VeryHigh(16),
        High(8),
        Normal(5),
        Medium(4),
        Low(2);

        final int length;

        private Quality(int value) {
            this.length = value;
        }
    }
}

