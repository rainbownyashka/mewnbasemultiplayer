/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.math.function;

import za.co.iocom.math.MathUtil;
import za.co.luma.math.function.RealFunction2DDouble;

public class Falloff
extends RealFunction2DDouble {
    private double centerX;
    private double centerY;
    private double outputMin;
    private double outputMax;
    private double inputMax;

    public Falloff(double x, double y, double inputMax, double outputMin, double outputMax) {
        this.centerX = x;
        this.centerY = y;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
        this.inputMax = inputMax;
    }

    @Override
    public double getDouble(double x, double y) {
        double dx = this.centerX - x;
        double dy = this.centerY - y;
        return MathUtil.lerp(Math.sqrt(dx * dx + dy * dy), 0.0, this.inputMax, this.outputMin, this.outputMax);
    }
}

