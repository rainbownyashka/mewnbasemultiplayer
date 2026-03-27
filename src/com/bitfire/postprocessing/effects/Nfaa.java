/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.postprocessing.effects.Antialiasing;
import com.bitfire.postprocessing.filters.NfaaFilter;

public final class Nfaa
extends Antialiasing {
    private NfaaFilter nfaaFilter = null;

    public Nfaa(int viewportWidth, int viewportHeight) {
        this.setup(viewportWidth, viewportHeight);
    }

    private void setup(int viewportWidth, int viewportHeight) {
        this.nfaaFilter = new NfaaFilter(viewportWidth, viewportHeight);
    }

    @Override
    public void setViewportSize(int width, int height) {
        this.nfaaFilter.setViewportSize(width, height);
    }

    @Override
    public void dispose() {
        if (this.nfaaFilter != null) {
            this.nfaaFilter.dispose();
            this.nfaaFilter = null;
        }
    }

    @Override
    public void rebind() {
        this.nfaaFilter.rebind();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest) {
        this.restoreViewport(dest);
        ((NfaaFilter)((NfaaFilter)this.nfaaFilter.setInput(src)).setOutput(dest)).render();
    }
}

