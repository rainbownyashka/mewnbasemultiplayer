/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.utils.random;

import com.badlogic.gdx.ai.utils.random.LongDistribution;

public final class ConstantLongDistribution
extends LongDistribution {
    public static final ConstantLongDistribution NEGATIVE_ONE = new ConstantLongDistribution(-1L);
    public static final ConstantLongDistribution ZERO = new ConstantLongDistribution(0L);
    public static final ConstantLongDistribution ONE = new ConstantLongDistribution(1L);
    private final long value;

    public ConstantLongDistribution(long value) {
        this.value = value;
    }

    @Override
    public long nextLong() {
        return this.value;
    }

    public long getValue() {
        return this.value;
    }
}

