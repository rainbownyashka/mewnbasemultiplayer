/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.geom;

import java.awt.geom.Point2D;
import za.co.iocom.math.MathUtil;

public class Vector2DDouble
extends Point2D {
    public double x;
    public double y;

    public Vector2DDouble(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "<" + this.x + " " + this.y + ">";
    }

    public double size() {
        return Math.sqrt(MathUtil.sqr(this.x) + MathUtil.sqr(this.y));
    }

    public void scale(double r) {
        this.x *= r;
        this.y *= r;
    }

    public Vector2DDouble unitVector() {
        Vector2DDouble v = new Vector2DDouble(this.x, this.y);
        v.scale(1.0 / this.size());
        return v;
    }

    public static double distance(Vector2DDouble p1, Vector2DDouble p2) {
        double x = p2.x - p1.x;
        double y = p2.y - p1.y;
        return Math.sqrt(x * x + y * y);
    }

    public static double scalarCross(Vector2DDouble p1, Vector2DDouble p2) {
        return p1.x * p2.y - p1.y * p2.x;
    }

    public static boolean sameSide(Vector2DDouble p1, Vector2DDouble p2, Vector2DDouble a, Vector2DDouble b) {
        double cp2;
        double cp1 = Vector2DDouble.scalarCross(Vector2DDouble.minus(b, a), Vector2DDouble.minus(p1, a));
        return cp1 * (cp2 = Vector2DDouble.scalarCross(Vector2DDouble.minus(b, a), Vector2DDouble.minus(p2, a))) >= 0.0;
    }

    public static boolean pointInTriangle(Vector2DDouble p, Vector2DDouble a, Vector2DDouble b, Vector2DDouble c) {
        return Vector2DDouble.sameSide(p, a, b, c) && Vector2DDouble.sameSide(p, b, a, c) && Vector2DDouble.sameSide(p, c, a, b);
    }

    public static Vector2DDouble minus(Vector2DDouble p1, Vector2DDouble p2) {
        return new Vector2DDouble(p1.x - p2.x, p1.y - p2.y);
    }

    public static Vector2DDouble add(Vector2DDouble p1, Vector2DDouble p2) {
        return new Vector2DDouble(p1.getX() + p2.getX(), p1.getY() + p2.getY());
    }

    public static Vector2DDouble getPointBetween(Vector2DDouble p1, Vector2DDouble p2, double r) {
        Vector2DDouble u = Vector2DDouble.scale(Vector2DDouble.subtract(p2, p1), r);
        return Vector2DDouble.add(p1, u);
    }

    public static Vector2DDouble orthoUnit(Vector2DDouble p) {
        double size = Vector2DDouble.size(p);
        return new Vector2DDouble(p.getY() / size, -p.getX() / size);
    }

    public static Vector2DDouble scale(Vector2DDouble p, double r) {
        return new Vector2DDouble(p.getX() * r, p.getY() * r);
    }

    public static double size(Vector2DDouble p) {
        return Math.sqrt(MathUtil.sqr(p.getX()) + MathUtil.sqr(p.getY()));
    }

    public static Vector2DDouble subtract(Vector2DDouble p1, Vector2DDouble p2) {
        return new Vector2DDouble(p1.getX() - p2.getX(), p1.getY() - p2.getY());
    }

    public static Vector2DDouble unit(Vector2DDouble p1) {
        return Vector2DDouble.scale(p1, 1.0 / Vector2DDouble.size(p1));
    }

    public void setSize(double size) {
        if (this.size() != 0.0) {
            this.scale(size / this.size());
        } else {
            this.x = 0.0;
            this.y = size;
        }
    }

    public void setAngle(double angleInRad) {
        double r = this.size();
        this.x = r * Math.cos(angleInRad);
        this.y = r * Math.sin(angleInRad);
    }

    public double angle() {
        if (this.x > 0.0) {
            return Math.atan(this.y / this.x);
        }
        if (this.x < 0.0) {
            return Math.atan(this.y / this.x) - Math.PI;
        }
        if (this.y > 0.0) {
            return 1.5707963267948966;
        }
        if (this.y < 0.0) {
            return -1.5707963267948966;
        }
        return 0.0;
    }
}

