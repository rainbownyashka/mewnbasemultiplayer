/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.geom;

import za.co.iocom.math.MathUtil;

public class Vector3DDouble {
    public double x;
    public double y;
    public double z;

    public Vector3DDouble(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public void setLocation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String toString() {
        return "<" + this.x + " " + this.y + " " + this.z + ">";
    }

    public double size() {
        return Math.sqrt(MathUtil.sqr(this.x) + MathUtil.sqr(this.y) + MathUtil.sqr(this.z));
    }

    public void scale(double r) {
        this.x *= r;
        this.y *= r;
        this.z *= r;
    }

    public Vector3DDouble unitVector() {
        Vector3DDouble v = new Vector3DDouble(this.x, this.y, this.z);
        v.scale(1.0 / this.size());
        return v;
    }

    public static double distance(Vector3DDouble p1, Vector3DDouble p2) {
        double x = p2.x - p1.x;
        double y = p2.y - p1.y;
        double z = p2.z - p1.z;
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static double scalarCross(Vector3DDouble p1, Vector3DDouble p2) {
        return p1.x * p2.y - p1.y * p2.x;
    }

    public static boolean sameSide(Vector3DDouble p1, Vector3DDouble p2, Vector3DDouble a, Vector3DDouble b) {
        double cp2;
        double cp1 = Vector3DDouble.scalarCross(Vector3DDouble.subtract(b, a), Vector3DDouble.subtract(p1, a));
        return cp1 * (cp2 = Vector3DDouble.scalarCross(Vector3DDouble.subtract(b, a), Vector3DDouble.subtract(p2, a))) >= 0.0;
    }

    public static boolean pointInTriangle(Vector3DDouble p, Vector3DDouble a, Vector3DDouble b, Vector3DDouble c) {
        return Vector3DDouble.sameSide(p, a, b, c) && Vector3DDouble.sameSide(p, b, a, c) && Vector3DDouble.sameSide(p, c, a, b);
    }

    public static Vector3DDouble add(Vector3DDouble p1, Vector3DDouble p2) {
        return new Vector3DDouble(p1.getX() + p2.getX(), p1.getY() + p2.getY(), p1.getZ() + p2.getZ());
    }

    public static Vector3DDouble getPointBetween(Vector3DDouble p1, Vector3DDouble p2, double r) {
        Vector3DDouble u = Vector3DDouble.scale(Vector3DDouble.subtract(p2, p1), r);
        return Vector3DDouble.add(p1, u);
    }

    public static Vector3DDouble scale(Vector3DDouble p, double r) {
        return new Vector3DDouble(p.getX() * r, p.getY() * r, p.getZ());
    }

    public static double size(Vector3DDouble p) {
        return Math.sqrt(MathUtil.sqr(p.getX()) + MathUtil.sqr(p.getY()) + MathUtil.sqr(p.getZ()));
    }

    public static Vector3DDouble subtract(Vector3DDouble p1, Vector3DDouble p2) {
        return new Vector3DDouble(p1.getX() - p2.getX(), p1.getY() - p2.getY(), p1.getZ() - p2.getZ());
    }

    public static Vector3DDouble unit(Vector3DDouble p1) {
        return Vector3DDouble.scale(p1, 1.0 / Vector3DDouble.size(p1));
    }

    public static double getHeight(double x, double y, Vector3DDouble p, Vector3DDouble q) {
        double alpha = (x * q.y - y * q.x) / (p.x * q.y - p.y * q.x);
        double beta = (x - alpha * p.x) / q.x;
        double z = alpha * p.z + beta * q.z;
        return z;
    }
}

