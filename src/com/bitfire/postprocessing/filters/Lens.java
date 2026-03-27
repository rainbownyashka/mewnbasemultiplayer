/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class Lens
extends Filter<Lens> {
    private Vector2 lightPosition = new Vector2();
    private float intensity;
    private Vector3 color = new Vector3();
    private Vector2 viewport = new Vector2();

    public Lens(float width, float height) {
        super(ShaderLoader.fromFile("screenspace", "lensflare"));
        this.viewport.set(width, height);
        this.rebind();
    }

    public void setLightPosition(float x, float y) {
        this.lightPosition.set(x, y);
        this.setParam((Filter.Parameter)Param.LightPosition, this.lightPosition);
    }

    public Vector2 getLightPosition() {
        return this.lightPosition;
    }

    public void setLightPosition(Vector2 lightPosition) {
        this.setLightPosition(lightPosition.x, lightPosition.y);
    }

    public float getIntensity() {
        return this.intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
        this.setParam((Filter.Parameter)Param.Intensity, intensity);
    }

    public Vector3 getColor() {
        return this.color;
    }

    public void setColor(float r, float g, float b) {
        this.color.set(r, g, b);
        this.setParam((Filter.Parameter)Param.Color, this.color);
    }

    public void setViewport(float width, float height) {
        this.viewport.set(width, height);
        this.setParams((Filter.Parameter)Param.Viewport, this.viewport);
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture, 0);
        this.setParams((Filter.Parameter)Param.LightPosition, this.lightPosition);
        this.setParams((Filter.Parameter)Param.Intensity, this.intensity);
        this.setParams((Filter.Parameter)Param.Color, this.color);
        this.setParams((Filter.Parameter)Param.Viewport, this.viewport);
        this.endParams();
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
    }

    public static enum Param implements Filter.Parameter
    {
        Texture("u_texture0", 0),
        LightPosition("u_lightPosition", 2),
        Intensity("u_intensity", 0),
        Color("u_color", 3),
        Viewport("u_viewport", 2);

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

