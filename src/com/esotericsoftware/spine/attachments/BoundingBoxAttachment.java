/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.attachments;

import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.VertexAttachment;

public class BoundingBoxAttachment
extends VertexAttachment {
    final Color color = new Color(0.38f, 0.94f, 0.0f, 1.0f);

    public BoundingBoxAttachment(String name) {
        super(name);
    }

    public Color getColor() {
        return this.color;
    }

    @Override
    public Attachment copy() {
        BoundingBoxAttachment copy = new BoundingBoxAttachment(this.name);
        this.copyTo(copy);
        copy.color.set(this.color);
        return copy;
    }
}

