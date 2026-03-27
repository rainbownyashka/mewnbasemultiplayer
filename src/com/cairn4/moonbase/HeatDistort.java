/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.graphics.Texture;
import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public class HeatDistort
extends Filter<HeatDistort> {
    public Texture distortionTexture;
    public float temperature;
    public float time;

    public void setTemperature(float t) {
        this.temperature = t;
        this.setParam((Filter.Parameter)Param.Temperature, this.temperature);
    }

    public void setTime(float delta) {
        this.time += delta;
        this.setParam((Filter.Parameter)Param.Time, this.time);
    }

    public void setDistortTex(Texture tex) {
        this.distortionTexture = tex;
        this.setParam((Filter.Parameter)Param.DistortionTex, 1);
    }

    public HeatDistort() {
        super(ShaderLoader.fromFile("screenspace", "heat"));
        this.rebind();
        this.time = 0.0f;
        this.setTemperature(0.0f);
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
        this.distortionTexture.bind(1);
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture0, 0);
        this.setParams((Filter.Parameter)Param.DistortionTex, 1);
        this.endParams();
    }

    public static enum Param implements Filter.Parameter
    {
        Texture0("u_texture0", 0),
        DistortionTex("u_texture1", 0),
        Temperature("temperature", 0),
        Time("time", 0);

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

