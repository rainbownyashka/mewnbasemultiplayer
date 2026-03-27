/*
 * Decompiled with CFR 0.152.
 */
package box2dLight;

import com.badlogic.gdx.utils.StringBuilder;

public class Spinor {
    float real;
    float complex;
    private static final float COSINE_THRESHOLD = 0.001f;

    public Spinor() {
    }

    public Spinor(float angle) {
        this.set(angle);
    }

    public Spinor(Spinor copyFrom) {
        this.set(copyFrom);
    }

    public Spinor(float real, float complex) {
        this.set(real, complex);
    }

    public Spinor set(float angle) {
        this.set((float)Math.cos(angle /= 2.0f), (float)Math.sin(angle));
        return this;
    }

    public Spinor set(Spinor copyFrom) {
        this.set(copyFrom.real, copyFrom.complex);
        return this;
    }

    public Spinor set(float real, float complex) {
        this.real = real;
        this.complex = complex;
        return this;
    }

    public Spinor scale(float t) {
        this.real *= t;
        this.complex *= t;
        return this;
    }

    public Spinor invert() {
        this.complex = -this.complex;
        this.scale(this.len2());
        return this;
    }

    public Spinor add(Spinor other) {
        this.real += other.real;
        this.complex += other.complex;
        return this;
    }

    public Spinor add(float angle) {
        this.real = (float)((double)this.real + Math.cos(angle /= 2.0f));
        this.complex = (float)((double)this.complex + Math.sin(angle));
        return this;
    }

    public Spinor sub(Spinor other) {
        this.real -= other.real;
        this.complex -= other.complex;
        return this;
    }

    public Spinor sub(float angle) {
        this.real = (float)((double)this.real - Math.cos(angle /= 2.0f));
        this.complex = (float)((double)this.complex - Math.sin(angle));
        return this;
    }

    public float len() {
        return (float)Math.sqrt(this.real * this.real + this.complex * this.complex);
    }

    public float len2() {
        return this.real * this.real + this.complex * this.complex;
    }

    public Spinor mul(Spinor other) {
        this.set(this.real * other.real - this.complex * other.complex, this.real * other.complex + this.complex * other.real);
        return this;
    }

    public Spinor nor() {
        float length = this.len();
        this.real /= length;
        this.complex /= length;
        return this;
    }

    public float angle() {
        return (float)Math.atan2(this.complex, this.real) * 2.0f;
    }

    public Spinor lerp(Spinor end, float alpha, Spinor tmp) {
        this.scale(1.0f - alpha);
        tmp.set(end).scale(alpha);
        this.add(tmp);
        this.nor();
        return this;
    }

    public Spinor slerp(Spinor dest, float t) {
        float scale1;
        float scale0;
        float tr;
        float tc;
        float cosom = this.real * dest.real + this.complex * dest.complex;
        if (cosom < 0.0f) {
            cosom = -cosom;
            tc = -dest.complex;
            tr = -dest.real;
        } else {
            tc = dest.complex;
            tr = dest.real;
        }
        if (1.0f - cosom > 0.001f) {
            float omega = (float)Math.acos(cosom);
            float sinom = (float)Math.sin(omega);
            scale0 = (float)Math.sin((1.0f - t) * omega) / sinom;
            scale1 = (float)Math.sin(t * omega) / sinom;
        } else {
            scale0 = 1.0f - t;
            scale1 = t;
        }
        this.complex = scale0 * this.complex + scale1 * tc;
        this.real = scale0 * this.real + scale1 * tr;
        return this;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        float radians = this.angle();
        result.append("radians: ");
        result.append(radians);
        result.append(", degrees: ");
        result.append(radians * 57.295776f);
        return result.toString();
    }
}

