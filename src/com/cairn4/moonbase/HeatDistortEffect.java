/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.cairn4.moonbase.HeatDistort;

public class HeatDistortEffect
extends PostProcessorEffect {
    private HeatDistort distort = new HeatDistort();

    public HeatDistortEffect() {
        this.setTemperature(1.0f);
    }

    public void setTemperature(float amount) {
        this.distort.setTemperature(amount);
    }

    public void setTime(float delta) {
        this.distort.setTime(delta);
    }

    public void setDistortTex(Texture tex) {
        this.distort.setDistortTex(tex);
    }

    @Override
    public void rebind() {
        this.distort.rebind();
    }

    @Override
    public void dispose() {
        this.distort.dispose();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest) {
        this.restoreViewport(dest);
        ((HeatDistort)((HeatDistort)this.distort.setInput(src)).setOutput(dest)).render();
    }
}

