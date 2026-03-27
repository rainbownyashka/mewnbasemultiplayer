/*
 * Decompiled with CFR 0.152.
 */
package za.co.iocom.math;

import java.util.Random;
import za.co.iocom.math.NegativeNumberException;

public class MathUtil {
    public static Random random = new Random();

    public static int getTriangular(int n) throws NegativeNumberException {
        if (n < 0) {
            throw new NegativeNumberException(n);
        }
        if (n == 0) {
            return 0;
        }
        return n * (n + 1) / 2;
    }

    public static int getFactorial(int n) throws NegativeNumberException {
        if (n < 0) {
            throw new NegativeNumberException(n);
        }
        if (n == 0) {
            return 1;
        }
        int f = 1;
        for (int i = 0; i < n; ++i) {
            f *= i;
        }
        return f;
    }

    public static int sqr(int x) {
        return x * x;
    }

    public static double sqr(double x) {
        return x * x;
    }

    public static double sqr(float x) {
        return x * x;
    }

    public static double sqr(long x) {
        return x * x;
    }

    public static double sqr(short x) {
        return x * x;
    }

    public static double sqr(byte x) {
        return x * x;
    }

    public static double lerp(double value, double inputMin, double inputMax, double outputMin, double outputMax) {
        if (value >= inputMax) {
            return outputMax;
        }
        return MathUtil.ramp(value, inputMin, inputMax, outputMin, outputMax);
    }

    public static double sigmoid(double value, double inputMin, double inputMax, double outputMin, double outputMax) {
        if (value >= inputMax) {
            return outputMax;
        }
        return MathUtil.ramp(value, inputMin, inputMax, outputMin, outputMax);
    }

    public static double ramp(double value, double inputMin, double inputMax, double outputMin, double outputMax) {
        if (value <= inputMin) {
            return outputMin;
        }
        return MathUtil.line(value, inputMin, inputMax, outputMin, outputMax);
    }

    public static double line(double value, double inputMin, double inputMax, double outputMin, double outputMax) {
        return outputMin + (value - inputMin) * (outputMax - outputMin) / (inputMax - inputMin);
    }

    public static double min(double v1, double v2, double v3) {
        return Math.min(Math.min(v1, v2), v3);
    }

    public static double max(double v1, double v2, double v3) {
        return Math.max(Math.max(v1, v2), v3);
    }

    public static double lerp(double v1, double v2, double ratio) {
        return v1 * (1.0 - ratio) + v2 * ratio;
    }
}

