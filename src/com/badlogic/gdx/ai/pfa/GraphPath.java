/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.pfa;

public interface GraphPath<N>
extends Iterable<N> {
    public int getCount();

    public N get(int var1);

    public void add(N var1);

    public void clear();

    public void reverse();
}

