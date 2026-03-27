/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public class Copy
extends Filter<Copy> {
    public Copy() {
        super(ShaderLoader.fromFile("screenspace", "copy"));
    }

    @Override
    public void rebind() {
        this.setParam((Filter.Parameter)Param.Texture0, 0);
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
    }

    public static enum Param implements Filter.Parameter
    {
        Texture0("u_texture0", 0);

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

