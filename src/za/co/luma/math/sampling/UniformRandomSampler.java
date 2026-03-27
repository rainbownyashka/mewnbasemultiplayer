/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.math.sampling;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.math.sampling.Sampler;

public class UniformRandomSampler
implements Sampler<Vector2DDouble> {
    private int count;
    private double x0;
    private double x1;
    private double y0;
    private double y1;
    private Random random;

    public UniformRandomSampler(double x0, double y0, double x1, double y1, int count) {
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
        this.count = count;
        this.random = new Random();
    }

    @Override
    public List<Vector2DDouble> sample() {
        LinkedList<Vector2DDouble> sampleList = new LinkedList<Vector2DDouble>();
        for (int i = 0; i < this.count; ++i) {
            double rx = this.x0 + this.random.nextDouble() * (this.x1 - this.x0);
            double ry = this.y0 + this.random.nextDouble() * (this.y1 - this.y0);
            sampleList.add(new Vector2DDouble(rx, ry));
        }
        return sampleList;
    }
}

