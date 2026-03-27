/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.attachments;

import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.spine.SlotData;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.VertexAttachment;

public class ClippingAttachment
extends VertexAttachment {
    SlotData endSlot;
    final Color color = new Color(0.2275f, 0.2275f, 0.8078f, 1.0f);

    public ClippingAttachment(String name) {
        super(name);
    }

    public SlotData getEndSlot() {
        return this.endSlot;
    }

    public void setEndSlot(SlotData endSlot) {
        this.endSlot = endSlot;
    }

    public Color getColor() {
        return this.color;
    }

    @Override
    public Attachment copy() {
        ClippingAttachment copy = new ClippingAttachment(this.name);
        this.copyTo(copy);
        copy.endSlot = this.endSlot;
        copy.color.set(this.color);
        return copy;
    }
}

