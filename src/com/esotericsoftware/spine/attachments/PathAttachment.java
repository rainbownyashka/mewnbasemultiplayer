/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.attachments;

import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.VertexAttachment;
import com.esotericsoftware.spine.utils.SpineUtils;

public class PathAttachment
extends VertexAttachment {
    float[] lengths;
    boolean closed;
    boolean constantSpeed;
    final Color color = new Color(1.0f, 0.5f, 0.0f, 1.0f);

    public PathAttachment(String name) {
        super(name);
    }

    public boolean getClosed() {
        return this.closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean getConstantSpeed() {
        return this.constantSpeed;
    }

    public void setConstantSpeed(boolean constantSpeed) {
        this.constantSpeed = constantSpeed;
    }

    public float[] getLengths() {
        return this.lengths;
    }

    public void setLengths(float[] lengths) {
        this.lengths = lengths;
    }

    public Color getColor() {
        return this.color;
    }

    @Override
    public Attachment copy() {
        PathAttachment copy = new PathAttachment(this.name);
        this.copyTo(copy);
        copy.lengths = new float[this.lengths.length];
        SpineUtils.arraycopy(this.lengths, 0, copy.lengths, 0, this.lengths.length);
        copy.closed = this.closed;
        copy.constantSpeed = this.constantSpeed;
        copy.color.set(this.color);
        return copy;
    }
}

