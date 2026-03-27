/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.attachments;

import com.badlogic.gdx.utils.FloatArray;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.utils.SpineUtils;

public abstract class VertexAttachment
extends Attachment {
    private static int nextID;
    private final int id = (VertexAttachment.nextID() & 0xFFFF) << 11;
    int[] bones;
    float[] vertices;
    int worldVerticesLength;
    VertexAttachment deformAttachment = this;

    public VertexAttachment(String name) {
        super(name);
    }

    public void computeWorldVertices(Slot slot, int start, int count, float[] worldVertices, int offset, int stride) {
        count = offset + (count >> 1) * stride;
        Skeleton skeleton = slot.getSkeleton();
        FloatArray deformArray = slot.getDeform();
        float[] vertices = this.vertices;
        int[] bones = this.bones;
        if (bones == null) {
            if (deformArray.size > 0) {
                vertices = deformArray.items;
            }
            Bone bone = slot.getBone();
            float x = bone.getWorldX();
            float y = bone.getWorldY();
            float a = bone.getA();
            float b = bone.getB();
            float c = bone.getC();
            float d = bone.getD();
            int v = start;
            for (int w = offset; w < count; w += stride) {
                float vx = vertices[v];
                float vy = vertices[v + 1];
                worldVertices[w] = vx * a + vy * b + x;
                worldVertices[w + 1] = vx * c + vy * d + y;
                v += 2;
            }
            return;
        }
        int v = 0;
        int skip = 0;
        for (int i = 0; i < start; i += 2) {
            int n = bones[v];
            v += n + 1;
            skip += n;
        }
        T[] skeletonBones = skeleton.getBones().items;
        if (deformArray.size == 0) {
            int b = skip * 3;
            for (int w = offset; w < count; w += stride) {
                float wx = 0.0f;
                float wy = 0.0f;
                int n = bones[v++];
                n += v;
                while (v < n) {
                    Bone bone = (Bone)skeletonBones[bones[v]];
                    float vx = vertices[b];
                    float vy = vertices[b + 1];
                    float weight = vertices[b + 2];
                    wx += (vx * bone.getA() + vy * bone.getB() + bone.getWorldX()) * weight;
                    wy += (vx * bone.getC() + vy * bone.getD() + bone.getWorldY()) * weight;
                    ++v;
                    b += 3;
                }
                worldVertices[w] = wx;
                worldVertices[w + 1] = wy;
            }
        } else {
            float[] deform = deformArray.items;
            int b = skip * 3;
            int f = skip << 1;
            for (int w = offset; w < count; w += stride) {
                float wx = 0.0f;
                float wy = 0.0f;
                int n = bones[v++];
                n += v;
                while (v < n) {
                    Bone bone = (Bone)skeletonBones[bones[v]];
                    float vx = vertices[b] + deform[f];
                    float vy = vertices[b + 1] + deform[f + 1];
                    float weight = vertices[b + 2];
                    wx += (vx * bone.getA() + vy * bone.getB() + bone.getWorldX()) * weight;
                    wy += (vx * bone.getC() + vy * bone.getD() + bone.getWorldY()) * weight;
                    ++v;
                    b += 3;
                    f += 2;
                }
                worldVertices[w] = wx;
                worldVertices[w + 1] = wy;
            }
        }
    }

    public VertexAttachment getDeformAttachment() {
        return this.deformAttachment;
    }

    public void setDeformAttachment(VertexAttachment deformAttachment) {
        this.deformAttachment = deformAttachment;
    }

    public int[] getBones() {
        return this.bones;
    }

    public void setBones(int[] bones) {
        this.bones = bones;
    }

    public float[] getVertices() {
        return this.vertices;
    }

    public void setVertices(float[] vertices) {
        this.vertices = vertices;
    }

    public int getWorldVerticesLength() {
        return this.worldVerticesLength;
    }

    public void setWorldVerticesLength(int worldVerticesLength) {
        this.worldVerticesLength = worldVerticesLength;
    }

    public int getId() {
        return this.id;
    }

    void copyTo(VertexAttachment attachment) {
        if (this.bones != null) {
            attachment.bones = new int[this.bones.length];
            SpineUtils.arraycopy(this.bones, 0, attachment.bones, 0, this.bones.length);
        } else {
            attachment.bones = null;
        }
        if (this.vertices != null) {
            attachment.vertices = new float[this.vertices.length];
            SpineUtils.arraycopy(this.vertices, 0, attachment.vertices, 0, this.vertices.length);
        } else {
            attachment.vertices = null;
        }
        attachment.worldVerticesLength = this.worldVerticesLength;
        attachment.deformAttachment = this.deformAttachment;
    }

    private static synchronized int nextID() {
        return nextID++;
    }
}

