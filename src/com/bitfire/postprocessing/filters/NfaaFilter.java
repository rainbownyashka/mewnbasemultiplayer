/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.math.Vector2;
import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class NfaaFilter
extends Filter<NfaaFilter> {
    private Vector2 viewportInverse;

    public NfaaFilter(int viewportWidth, int viewportHeight) {
        this(new Vector2(viewportWidth, viewportHeight));
    }

    public NfaaFilter(Vector2 viewportSize) {
        super(ShaderLoader.fromFile("screenspace", "nfaa"));
        this.viewportInverse = viewportSize;
        this.viewportInverse.x = 1.0f / this.viewportInverse.x;
        this.viewportInverse.y = 1.0f / this.viewportInverse.y;
        this.rebind();
    }

    public void setViewportSize(float width, float height) {
        this.viewportInverse.set(1.0f / width, 1.0f / height);
        this.setParam((Filter.Parameter)Param.ViewportInverse, this.viewportInverse);
    }

    public Vector2 getViewportSize() {
        return this.viewportInverse;
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture, 0);
        this.setParams((Filter.Parameter)Param.ViewportInverse, this.viewportInverse);
        this.endParams();
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
    }

    public static enum Param implements Filter.Parameter
    {
        Texture("u_texture0", 0),
        ViewportInverse("u_viewportInverse", 2);

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

