/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.physics.box2d;

import com.badlogic.gdx.utils.Disposable;

public abstract class Shape
implements Disposable {
    protected long addr;

    public abstract Type getType();

    public float getRadius() {
        return this.jniGetRadius(this.addr);
    }

    private native float jniGetRadius(long var1);

    public void setRadius(float radius) {
        this.jniSetRadius(this.addr, radius);
    }

    private native void jniSetRadius(long var1, float var3);

    @Override
    public void dispose() {
        this.jniDispose(this.addr);
    }

    private native void jniDispose(long var1);

    protected static native int jniGetType(long var0);

    public int getChildCount() {
        return this.jniGetChildCount(this.addr);
    }

    private native int jniGetChildCount(long var1);

    public static enum Type {
        Circle,
        Edge,
        Polygon,
        Chain;

    }
}

