/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.pfa;

public interface Connection<N> {
    public float getCost();

    public N getFromNode();

    public N getToNode();
}

