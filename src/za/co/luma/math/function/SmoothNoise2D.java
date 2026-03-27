/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.math.function;

import za.co.iocom.math.MathUtil;

public class SmoothNoise2D {
    private double[][] noise;

    public SmoothNoise2D(int maxWidth, int maxHeight) {
        this.noise = new double[maxWidth][maxHeight];
        for (int i = 0; i < maxWidth; ++i) {
            for (int j = 0; j < maxHeight; ++j) {
                this.noise[i][j] = MathUtil.random.nextDouble();
            }
        }
    }

    public double getNoise(int x, int y, int samplingPeriod) {
        int xx = x / samplingPeriod;
        int noiseX1 = xx * samplingPeriod;
        int noiseX2 = (xx + 1) * samplingPeriod;
        int yy = y / samplingPeriod;
        int noiseY1 = yy * samplingPeriod;
        int noiseY2 = (yy + 1) * samplingPeriod;
        double N1 = this.noise[noiseX1][noiseY1];
        double N2 = this.noise[noiseX1][noiseY2];
        double N3 = this.noise[noiseX2][noiseY1];
        double N4 = this.noise[noiseX2][noiseY2];
        int gg = samplingPeriod * samplingPeriod;
        int gx = samplingPeriod * (x - noiseX1);
        int gy = samplingPeriod * (y - noiseY1);
        int xy = (x - noiseX1) * (y - noiseY1);
        double noiseTmp = (double)(gg - gx - gy + xy) * N1 + (double)(gy - xy) * N2 + (double)(gx - xy) * N3 + (double)xy * N4;
        return noiseTmp / (double)gg;
    }
}

