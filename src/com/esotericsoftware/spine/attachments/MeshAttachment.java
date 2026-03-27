/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.attachments;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.VertexAttachment;
import com.esotericsoftware.spine.utils.SpineUtils;

public class MeshAttachment
extends VertexAttachment {
    private TextureRegion region;
    private String path;
    private float[] regionUVs;
    private float[] uvs;
    private short[] triangles;
    private final Color color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    private int hullLength;
    private MeshAttachment parentMesh;
    private short[] edges;
    private float width;
    private float height;

    public MeshAttachment(String name) {
        super(name);
    }

    public void setRegion(TextureRegion region) {
        if (region == null) {
            throw new IllegalArgumentException("region cannot be null.");
        }
        this.region = region;
    }

    public TextureRegion getRegion() {
        if (this.region == null) {
            throw new IllegalStateException("Region has not been set: " + this);
        }
        return this.region;
    }

    public void updateUVs() {
        float height;
        float width;
        float v;
        float u;
        float[] regionUVs = this.regionUVs;
        if (this.uvs == null || this.uvs.length != regionUVs.length) {
            this.uvs = new float[regionUVs.length];
        }
        float[] uvs = this.uvs;
        int n = uvs.length;
        if (this.region instanceof TextureAtlas.AtlasRegion) {
            u = this.region.getU();
            v = this.region.getV();
            TextureAtlas.AtlasRegion region = (TextureAtlas.AtlasRegion)this.region;
            float textureWidth = region.getTexture().getWidth();
            float textureHeight = region.getTexture().getHeight();
            switch (region.degrees) {
                case 90: {
                    u -= ((float)region.originalHeight - region.offsetY - (float)region.packedWidth) / textureWidth;
                    v -= ((float)region.originalWidth - region.offsetX - (float)region.packedHeight) / textureHeight;
                    float width2 = (float)region.originalHeight / textureWidth;
                    float height2 = (float)region.originalWidth / textureHeight;
                    for (int i = 0; i < n; i += 2) {
                        uvs[i] = u + regionUVs[i + 1] * width2;
                        uvs[i + 1] = v + (1.0f - regionUVs[i]) * height2;
                    }
                    return;
                }
                case 180: {
                    u -= ((float)region.originalWidth - region.offsetX - (float)region.packedWidth) / textureWidth;
                    v -= region.offsetY / textureHeight;
                    float width3 = (float)region.originalWidth / textureWidth;
                    float height3 = (float)region.originalHeight / textureHeight;
                    for (int i = 0; i < n; i += 2) {
                        uvs[i] = u + (1.0f - regionUVs[i]) * width3;
                        uvs[i + 1] = v + (1.0f - regionUVs[i + 1]) * height3;
                    }
                    return;
                }
                case 270: {
                    u -= region.offsetY / textureWidth;
                    v -= region.offsetX / textureHeight;
                    float width4 = (float)region.originalHeight / textureWidth;
                    float height4 = (float)region.originalWidth / textureHeight;
                    for (int i = 0; i < n; i += 2) {
                        uvs[i] = u + (1.0f - regionUVs[i + 1]) * width4;
                        uvs[i + 1] = v + regionUVs[i] * height4;
                    }
                    return;
                }
            }
            u -= region.offsetX / textureWidth;
            v -= ((float)region.originalHeight - region.offsetY - (float)region.packedHeight) / textureHeight;
            width = (float)region.originalWidth / textureWidth;
            height = (float)region.originalHeight / textureHeight;
        } else if (this.region == null) {
            v = 0.0f;
            u = 0.0f;
            height = 1.0f;
            width = 1.0f;
        } else {
            u = this.region.getU();
            v = this.region.getV();
            width = this.region.getU2() - u;
            height = this.region.getV2() - v;
        }
        for (int i = 0; i < n; i += 2) {
            uvs[i] = u + regionUVs[i] * width;
            uvs[i + 1] = v + regionUVs[i + 1] * height;
        }
    }

    public short[] getTriangles() {
        return this.triangles;
    }

    public void setTriangles(short[] triangles) {
        this.triangles = triangles;
    }

    public float[] getRegionUVs() {
        return this.regionUVs;
    }

    public void setRegionUVs(float[] regionUVs) {
        this.regionUVs = regionUVs;
    }

    public float[] getUVs() {
        return this.uvs;
    }

    public void setUVs(float[] uvs) {
        this.uvs = uvs;
    }

    public Color getColor() {
        return this.color;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getHullLength() {
        return this.hullLength;
    }

    public void setHullLength(int hullLength) {
        this.hullLength = hullLength;
    }

    public void setEdges(short[] edges) {
        this.edges = edges;
    }

    public short[] getEdges() {
        return this.edges;
    }

    public float getWidth() {
        return this.width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return this.height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public MeshAttachment getParentMesh() {
        return this.parentMesh;
    }

    public void setParentMesh(MeshAttachment parentMesh) {
        this.parentMesh = parentMesh;
        if (parentMesh != null) {
            this.bones = parentMesh.bones;
            this.vertices = parentMesh.vertices;
            this.regionUVs = parentMesh.regionUVs;
            this.triangles = parentMesh.triangles;
            this.hullLength = parentMesh.hullLength;
            this.worldVerticesLength = parentMesh.worldVerticesLength;
            this.edges = parentMesh.edges;
            this.width = parentMesh.width;
            this.height = parentMesh.height;
        }
    }

    @Override
    public Attachment copy() {
        if (this.parentMesh != null) {
            return this.newLinkedMesh();
        }
        MeshAttachment copy = new MeshAttachment(this.name);
        copy.region = this.region;
        copy.path = this.path;
        copy.color.set(this.color);
        this.copyTo(copy);
        copy.regionUVs = new float[this.regionUVs.length];
        SpineUtils.arraycopy(this.regionUVs, 0, copy.regionUVs, 0, this.regionUVs.length);
        copy.uvs = new float[this.uvs.length];
        SpineUtils.arraycopy(this.uvs, 0, copy.uvs, 0, this.uvs.length);
        copy.triangles = new short[this.triangles.length];
        SpineUtils.arraycopy(this.triangles, 0, copy.triangles, 0, this.triangles.length);
        copy.hullLength = this.hullLength;
        if (this.edges != null) {
            copy.edges = new short[this.edges.length];
            SpineUtils.arraycopy(this.edges, 0, copy.edges, 0, this.edges.length);
        }
        copy.width = this.width;
        copy.height = this.height;
        return copy;
    }

    public MeshAttachment newLinkedMesh() {
        MeshAttachment mesh = new MeshAttachment(this.name);
        mesh.region = this.region;
        mesh.path = this.path;
        mesh.color.set(this.color);
        mesh.deformAttachment = this.deformAttachment;
        mesh.setParentMesh(this.parentMesh != null ? this.parentMesh : this);
        mesh.updateUVs();
        return mesh;
    }
}

