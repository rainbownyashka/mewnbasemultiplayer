/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class Lens2
extends Filter<Lens2> {
    private Vector2 viewportInverse;
    private int ghosts;
    private float haloWidth;
    private Texture lensColorTexture;

    public Lens2(int width, int height) {
        super(ShaderLoader.fromFile("screenspace", "lensflare2"));
        this.viewportInverse = new Vector2(1.0f / (float)width, 1.0f / (float)height);
        this.rebind();
    }

    public void setViewportSize(float width, float height) {
        this.viewportInverse.set(1.0f / width, 1.0f / height);
        this.setParam((Filter.Parameter)Param.ViewportInverse, this.viewportInverse);
    }

    public int getGhosts() {
        return this.ghosts;
    }

    public void setGhosts(int ghosts) {
        this.ghosts = ghosts;
        this.setParam((Filter.Parameter)Param.Ghosts, ghosts);
    }

    public float getHaloWidth() {
        return this.haloWidth;
    }

    public void setHaloWidth(float haloWidth) {
        this.haloWidth = haloWidth;
        this.setParam((Filter.Parameter)Param.HaloWidth, haloWidth);
    }

    public void setLensColorTexture(Texture tex) {
        this.lensColorTexture = tex;
        this.setParam((Filter.Parameter)Param.LensColor, 1);
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture, 0);
        this.setParams((Filter.Parameter)Param.LensColor, 1);
        this.setParams((Filter.Parameter)Param.ViewportInverse, this.viewportInverse);
        this.setParams((Filter.Parameter)Param.Ghosts, this.ghosts);
        this.setParams((Filter.Parameter)Param.HaloWidth, this.haloWidth);
        this.endParams();
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
        this.lensColorTexture.bind(1);
    }

    public static enum Param implements Filter.Parameter
    {
        Texture("u_texture0", 0),
        LensColor("u_texture1", 0),
        ViewportInverse("u_viewportInverse", 2),
        Ghosts("u_ghosts", 0),
        HaloWidth("u_haloWidth", 0);

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

