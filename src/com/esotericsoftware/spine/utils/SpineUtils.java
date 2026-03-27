/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.utils;

import java.lang.reflect.Array;

public class SpineUtils {
    public static final float PI = (float)Math.PI;
    public static final float PI2 = (float)Math.PI * 2;
    public static final float radiansToDegrees = 57.295776f;
    public static final float radDeg = 57.295776f;
    public static final float degreesToRadians = (float)Math.PI / 180;
    public static final float degRad = (float)Math.PI / 180;

    public static float cosDeg(float angle) {
        return (float)Math.cos(angle * ((float)Math.PI / 180));
    }

    public static float sinDeg(float angle) {
        return (float)Math.sin(angle * ((float)Math.PI / 180));
    }

    public static float cos(float angle) {
        return (float)Math.cos(angle);
    }

    public static float sin(float angle) {
        return (float)Math.sin(angle);
    }

    public static float atan2(float y, float x) {
        return (float)Math.atan2(y, x);
    }

    public static void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        if (src == null) {
            throw new IllegalArgumentException("src cannot be null.");
        }
        if (dest == null) {
            throw new IllegalArgumentException("dest cannot be null.");
        }
        try {
            System.arraycopy(src, srcPos, dest, destPos, length);
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            throw new ArrayIndexOutOfBoundsException("Src: " + Array.getLength(src) + ", " + srcPos + ", dest: " + Array.getLength(dest) + ", " + destPos + ", count: " + length);
        }
    }
}

