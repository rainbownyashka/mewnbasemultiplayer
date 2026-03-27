/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.FloatArray;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SlotData;
import com.esotericsoftware.spine.attachments.Attachment;

public class Slot {
    final SlotData data;
    final Bone bone;
    final Color color = new Color();
    final Color darkColor;
    Attachment attachment;
    private float attachmentTime;
    private FloatArray deform = new FloatArray();

    public Slot(SlotData data, Bone bone) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null.");
        }
        if (bone == null) {
            throw new IllegalArgumentException("bone cannot be null.");
        }
        this.data = data;
        this.bone = bone;
        this.darkColor = data.darkColor == null ? null : new Color();
        this.setToSetupPose();
    }

    public Slot(Slot slot, Bone bone) {
        if (slot == null) {
            throw new IllegalArgumentException("slot cannot be null.");
        }
        if (bone == null) {
            throw new IllegalArgumentException("bone cannot be null.");
        }
        this.data = slot.data;
        this.bone = bone;
        this.color.set(slot.color);
        this.darkColor = slot.darkColor == null ? null : new Color(slot.darkColor);
        this.attachment = slot.attachment;
        this.attachmentTime = slot.attachmentTime;
        this.deform.addAll(slot.deform);
    }

    public SlotData getData() {
        return this.data;
    }

    public Bone getBone() {
        return this.bone;
    }

    public Skeleton getSkeleton() {
        return this.bone.skeleton;
    }

    public Color getColor() {
        return this.color;
    }

    public Color getDarkColor() {
        return this.darkColor;
    }

    public Attachment getAttachment() {
        return this.attachment;
    }

    public void setAttachment(Attachment attachment) {
        if (this.attachment == attachment) {
            return;
        }
        this.attachment = attachment;
        this.attachmentTime = this.bone.skeleton.time;
        this.deform.clear();
    }

    public float getAttachmentTime() {
        return this.bone.skeleton.time - this.attachmentTime;
    }

    public void setAttachmentTime(float time) {
        this.attachmentTime = this.bone.skeleton.time - time;
    }

    public FloatArray getDeform() {
        return this.deform;
    }

    public void setDeform(FloatArray deform) {
        if (deform == null) {
            throw new IllegalArgumentException("deform cannot be null.");
        }
        this.deform = deform;
    }

    public void setToSetupPose() {
        this.color.set(this.data.color);
        if (this.darkColor != null) {
            this.darkColor.set(this.data.darkColor);
        }
        if (this.data.attachmentName == null) {
            this.setAttachment(null);
        } else {
            this.attachment = null;
            this.setAttachment(this.bone.skeleton.getAttachment(this.data.index, this.data.attachmentName));
        }
    }

    public String toString() {
        return this.data.name;
    }
}

