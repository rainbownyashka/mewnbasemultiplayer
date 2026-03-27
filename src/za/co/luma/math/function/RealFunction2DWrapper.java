/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.math.function;

import za.co.iocom.math.MathUtil;
import za.co.luma.math.function.RealFunction2D;
import za.co.luma.math.function.RealFunction2DDouble;

public class RealFunction2DWrapper
extends RealFunction2DDouble {
    private RealFunction2D function;
    private double outputMin;
    private double outputMax;
    private double inputMax;
    private double inputMin;

    public RealFunction2DWrapper(RealFunction2D function, double inputMin, double inputMax, double outputMin, double outputMax) {
        this.function = function;
        this.outputMin = outputMin;
        this.outputMax = outputMax;
        this.inputMax = inputMax;
        this.inputMin = inputMin;
    }

    @Override
    public double getDouble(double x, double y) {
        return MathUtil.lerp(this.function.getDouble((int)x, (int)y), this.inputMin, this.inputMax, this.outputMin, this.outputMax);
    }
}

