/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.bitfire.postprocessing.utils.PingPongBuffer;

public abstract class MultipassFilter {
    public abstract void rebind();

    public abstract void render(PingPongBuffer var1);
}

