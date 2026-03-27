/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.utils;

public class RollingAverage {
    private int size;
    private float total = 0.0f;
    private int index = 0;
    private float[] samples;

    public RollingAverage(int size) {
        this.size = size;
        this.samples = new float[size];
        for (int i = 0; i < size; ++i) {
            this.samples[i] = 0.0f;
        }
    }

    public void add(float x) {
        this.total -= this.samples[this.index];
        this.samples[this.index] = x;
        this.total += x;
        if (++this.index == this.size) {
            this.index = 0;
        }
    }

    public float getAverage() {
        return this.total / (float)this.size;
    }
}

