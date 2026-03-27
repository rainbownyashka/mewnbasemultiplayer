/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.utils;

public final class ArithmeticUtils {
    private ArithmeticUtils() {
    }

    public static float wrapAngleAroundZero(float a) {
        if (a >= 0.0f) {
            float rotation = a % ((float)Math.PI * 2);
            if (rotation > (float)Math.PI) {
                rotation -= (float)Math.PI * 2;
            }
            return rotation;
        }
        float rotation = -a % ((float)Math.PI * 2);
        if (rotation > (float)Math.PI) {
            rotation -= (float)Math.PI * 2;
        }
        return -rotation;
    }

    public static int gcdPositive(int a, int b) {
        int shift;
        if (a == 0) {
            return b;
        }
        if (b == 0) {
            return a;
        }
        int aTwos = Integer.numberOfTrailingZeros(a);
        a >>= aTwos;
        int bTwos = Integer.numberOfTrailingZeros(b);
        b >>= bTwos;
        int n = shift = aTwos <= bTwos ? aTwos : bTwos;
        while (a != b) {
            int delta = a - b;
            b = a <= b ? a : b;
            a = delta < 0 ? -delta : delta;
            a >>= Integer.numberOfTrailingZeros(a);
        }
        return a << shift;
    }

    public static int lcmPositive(int a, int b) throws ArithmeticException {
        if (a == 0 || b == 0) {
            return 0;
        }
        int lcm = Math.abs(ArithmeticUtils.mulAndCheck(a / ArithmeticUtils.gcdPositive(a, b), b));
        if (lcm == Integer.MIN_VALUE) {
            throw new ArithmeticException("overflow: lcm(" + a + ", " + b + ") > 2^31");
        }
        return lcm;
    }

    public static int gcdPositive(int ... args) {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("gcdPositive requires at least two arguments");
        }
        int result = args[0];
        int n = args.length;
        for (int i = 1; i < n; ++i) {
            result = ArithmeticUtils.gcdPositive(result, args[i]);
        }
        return result;
    }

    public static int lcmPositive(int ... args) {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException("lcmPositive requires at least two arguments");
        }
        int result = args[0];
        int n = args.length;
        for (int i = 1; i < n; ++i) {
            result = ArithmeticUtils.lcmPositive(result, args[i]);
        }
        return result;
    }

    public static int mulAndCheck(int x, int y) throws ArithmeticException {
        long m = (long)x * (long)y;
        if (m < Integer.MIN_VALUE || m > Integer.MAX_VALUE) {
            throw new ArithmeticException();
        }
        return (int)m;
    }
}

