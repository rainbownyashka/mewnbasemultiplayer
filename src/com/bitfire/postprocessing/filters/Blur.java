/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.utils.IntMap;
import com.bitfire.postprocessing.filters.Convolve2D;
import com.bitfire.postprocessing.filters.MultipassFilter;
import com.bitfire.postprocessing.utils.PingPongBuffer;

public final class Blur
extends MultipassFilter {
    private BlurType type;
    private float amount;
    private int passes;
    private float invWidth;
    private float invHeight;
    private final IntMap<Convolve2D> convolve = new IntMap(Tap.values().length);

    public Blur(int width, int height) {
        this.invWidth = 1.0f / (float)width;
        this.invHeight = 1.0f / (float)height;
        this.passes = 1;
        this.amount = 1.0f;
        for (Tap tap : Tap.values()) {
            this.convolve.put(tap.radius, new Convolve2D(tap.radius));
        }
        this.setType(BlurType.Gaussian5x5);
    }

    public void dispose() {
        for (Convolve2D c : this.convolve.values()) {
            c.dispose();
        }
    }

    public void setPasses(int passes) {
        this.passes = passes;
    }

    public void setType(BlurType type) {
        if (this.type != type) {
            this.type = type;
            this.computeBlurWeightings();
        }
    }

    public void setAmount(float amount) {
        this.amount = amount;
        this.computeBlurWeightings();
    }

    public int getPasses() {
        return this.passes;
    }

    public BlurType getType() {
        return this.type;
    }

    public float getAmount() {
        return this.amount;
    }

    @Override
    public void render(PingPongBuffer buffer) {
        Convolve2D c = this.convolve.get(this.type.tap.radius);
        for (int i = 0; i < this.passes; ++i) {
            c.render(buffer);
        }
    }

    private void computeBlurWeightings() {
        boolean hasdata = true;
        Convolve2D c = this.convolve.get(this.type.tap.radius);
        float[] outWeights = c.weights;
        float[] outOffsetsH = c.offsetsHor;
        float[] outOffsetsV = c.offsetsVert;
        float dx = this.invWidth;
        float dy = this.invHeight;
        switch (this.type) {
            case Gaussian3x3: 
            case Gaussian5x5: {
                this.computeKernel(this.type.tap.radius, this.amount, outWeights);
                this.computeOffsets(this.type.tap.radius, this.invWidth, this.invHeight, outOffsetsH, outOffsetsV);
                break;
            }
            case Gaussian3x3b: {
                outWeights[0] = 0.352941f;
                outWeights[1] = 0.294118f;
                outWeights[2] = 0.352941f;
                outOffsetsH[0] = -1.33333f;
                outOffsetsH[1] = 0.0f;
                outOffsetsH[2] = 0.0f;
                outOffsetsH[3] = 0.0f;
                outOffsetsH[4] = 1.33333f;
                outOffsetsH[5] = 0.0f;
                outOffsetsV[0] = 0.0f;
                outOffsetsV[1] = -1.33333f;
                outOffsetsV[2] = 0.0f;
                outOffsetsV[3] = 0.0f;
                outOffsetsV[4] = 0.0f;
                outOffsetsV[5] = 1.33333f;
                int i = 0;
                while (i < c.length * 2) {
                    int n = i;
                    outOffsetsH[n] = outOffsetsH[n] * dx;
                    int n2 = i++;
                    outOffsetsV[n2] = outOffsetsV[n2] * dy;
                }
                break;
            }
            case Gaussian5x5b: {
                outWeights[0] = 0.0702703f;
                outWeights[1] = 0.316216f;
                outWeights[2] = 0.227027f;
                outWeights[3] = 0.316216f;
                outWeights[4] = 0.0702703f;
                outOffsetsH[0] = -3.23077f;
                outOffsetsH[1] = 0.0f;
                outOffsetsH[2] = -1.38462f;
                outOffsetsH[3] = 0.0f;
                outOffsetsH[4] = 0.0f;
                outOffsetsH[5] = 0.0f;
                outOffsetsH[6] = 1.38462f;
                outOffsetsH[7] = 0.0f;
                outOffsetsH[8] = 3.23077f;
                outOffsetsH[9] = 0.0f;
                outOffsetsV[0] = 0.0f;
                outOffsetsV[1] = -3.23077f;
                outOffsetsV[2] = 0.0f;
                outOffsetsV[3] = -1.38462f;
                outOffsetsV[4] = 0.0f;
                outOffsetsV[5] = 0.0f;
                outOffsetsV[6] = 0.0f;
                outOffsetsV[7] = 1.38462f;
                outOffsetsV[8] = 0.0f;
                outOffsetsV[9] = 3.23077f;
                int i = 0;
                while (i < c.length * 2) {
                    int n = i;
                    outOffsetsH[n] = outOffsetsH[n] * dx;
                    int n3 = i++;
                    outOffsetsV[n3] = outOffsetsV[n3] * dy;
                }
                break;
            }
            default: {
                hasdata = false;
            }
        }
        if (hasdata) {
            c.upload();
        }
    }

    private void computeKernel(int blurRadius, float blurAmount, float[] outKernel) {
        int radius = blurRadius;
        float sigma = blurAmount;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float)Math.sqrt((double)twoSigmaSquare * Math.PI);
        float total = 0.0f;
        float distance = 0.0f;
        int index = 0;
        for (int i = -radius; i <= radius; ++i) {
            distance = i * i;
            index = i + radius;
            outKernel[index] = (float)Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += outKernel[index];
        }
        int size = radius * 2 + 1;
        int i = 0;
        while (i < size) {
            int n = i++;
            outKernel[n] = outKernel[n] / total;
        }
    }

    private void computeOffsets(int blurRadius, float dx, float dy, float[] outOffsetH, float[] outOffsetV) {
        int radius = blurRadius;
        boolean X = false;
        boolean Y = true;
        int i = -radius;
        int j = 0;
        while (i <= radius) {
            outOffsetH[j + 0] = (float)i * dx;
            outOffsetH[j + 1] = 0.0f;
            outOffsetV[j + 0] = 0.0f;
            outOffsetV[j + 1] = (float)i * dy;
            ++i;
            j += 2;
        }
    }

    @Override
    public void rebind() {
        this.computeBlurWeightings();
    }

    public static enum BlurType {
        Gaussian3x3(Tap.Tap3x3),
        Gaussian3x3b(Tap.Tap3x3),
        Gaussian5x5(Tap.Tap5x5),
        Gaussian5x5b(Tap.Tap5x5);

        public final Tap tap;

        private BlurType(Tap tap) {
            this.tap = tap;
        }
    }

    private static enum Tap {
        Tap3x3(1),
        Tap5x5(2);

        public final int radius;

        private Tap(int radius) {
            this.radius = radius;
        }
    }
}

