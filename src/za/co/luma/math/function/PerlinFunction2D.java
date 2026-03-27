/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.math.function;

import za.co.luma.math.function.RealFunction2D;
import za.co.luma.math.function.SmoothNoise2D;

public class PerlinFunction2D
extends RealFunction2D {
    private static final double DEFAULT_PERSISTANCE = 0.9;
    private final int octaves;
    private SmoothNoise2D[] textures;
    double persistence;
    int samplingPeriodMax;
    boolean normalize;

    public PerlinFunction2D(int width, int height, int octaves) {
        this(width, height, octaves, 0.9, true);
    }

    public PerlinFunction2D(int width, int height, int octaves, double persistence, boolean normalize) {
        this.octaves = octaves;
        this.textures = new SmoothNoise2D[octaves];
        this.samplingPeriodMax = width >> octaves;
        this.persistence = persistence;
        this.normalize = normalize;
        for (int i = 0; i < octaves; ++i) {
            this.textures[i] = new SmoothNoise2D(width + this.samplingPeriodMax + 1, height + this.samplingPeriodMax + 1);
        }
    }

    @Override
    public double getDouble(int u, int v) {
        double amplitude = 1.0;
        double totalAmplitude = 0.0;
        double col = 0.0;
        int samplingPeriod = this.samplingPeriodMax;
        for (int i = 0; i < this.octaves; ++i) {
            totalAmplitude += (amplitude *= this.persistence);
            col += this.textures[i].getNoise(u, v, samplingPeriod) * amplitude;
            samplingPeriod /= 2;
        }
        if (this.normalize) {
            col /= totalAmplitude;
        } else if (col > 1.0) {
            col = 1.0;
        } else if (col < 0.0) {
            col = 0.0;
        }
        return col;
    }
}

