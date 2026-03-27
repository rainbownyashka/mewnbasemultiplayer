/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;
import com.bitfire.postprocessing.PostProcessor;

public abstract class PostProcessorEffect
implements Disposable {
    protected boolean enabled = true;

    public abstract void rebind();

    public abstract void render(FrameBuffer var1, FrameBuffer var2);

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected void restoreViewport(FrameBuffer dest) {
        PostProcessor.restoreViewport(dest);
    }
}

