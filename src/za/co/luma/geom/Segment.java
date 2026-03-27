/*
 * Decompiled with CFR 0.152.
 */
package za.co.luma.geom;

import za.co.luma.geom.Vector2DDouble;

public class Segment {
    private Vector2DDouble start;
    private Vector2DDouble end;

    public Segment(Vector2DDouble start, Vector2DDouble end) {
        this.start = start;
        this.end = end;
    }

    public Vector2DDouble getEnd() {
        return this.end;
    }

    public void setEnd(Vector2DDouble end) {
        this.end = end;
    }

    public Vector2DDouble getStart() {
        return this.start;
    }

    public void setStart(Vector2DDouble start) {
        this.start = start;
    }

    public String toString() {
        return "[" + this.start + ", " + this.end + "]";
    }
}

