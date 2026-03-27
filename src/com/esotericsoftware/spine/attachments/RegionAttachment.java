/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.attachments;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.utils.SpineUtils;

public class RegionAttachment
extends Attachment {
    public static final int BLX = 0;
    public static final int BLY = 1;
    public static final int ULX = 2;
    public static final int ULY = 3;
    public static final int URX = 4;
    public static final int URY = 5;
    public static final int BRX = 6;
    public static final int BRY = 7;
    private TextureRegion region;
    private String path;
    private float x;
    private float y;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float rotation;
    private float width;
    private float height;
    private final float[] uvs = new float[8];
    private final float[] offset = new float[8];
    private final Color color = new Color(1.0f, 1.0f, 1.0f, 1.0f);

    public RegionAttachment(String name) {
        super(name);
    }

    public void updateOffset() {
        float width = this.getWidth();
        float height = this.getHeight();
        float localX2 = width / 2.0f;
        float localY2 = height / 2.0f;
        float localX = -localX2;
        float localY = -localY2;
        if (this.region instanceof TextureAtlas.AtlasRegion) {
            TextureAtlas.AtlasRegion region = (TextureAtlas.AtlasRegion)this.region;
            localX += region.offsetX / (float)region.originalWidth * width;
            localY += region.offsetY / (float)region.originalHeight * height;
            if (region.rotate) {
                localX2 -= ((float)region.originalWidth - region.offsetX - (float)region.packedHeight) / (float)region.originalWidth * width;
                localY2 -= ((float)region.originalHeight - region.offsetY - (float)region.packedWidth) / (float)region.originalHeight * height;
            } else {
                localX2 -= ((float)region.originalWidth - region.offsetX - (float)region.packedWidth) / (float)region.originalWidth * width;
                localY2 -= ((float)region.originalHeight - region.offsetY - (float)region.packedHeight) / (float)region.originalHeight * height;
            }
        }
        float scaleX = this.getScaleX();
        float scaleY = this.getScaleY();
        localX *= scaleX;
        localY *= scaleY;
        localX2 *= scaleX;
        localY2 *= scaleY;
        float rotation = this.getRotation();
        float cos = (float)Math.cos((float)Math.PI / 180 * rotation);
        float sin = (float)Math.sin((float)Math.PI / 180 * rotation);
        float x = this.getX();
        float y = this.getY();
        float localXCos = localX * cos + x;
        float localXSin = localX * sin;
        float localYCos = localY * cos + y;
        float localYSin = localY * sin;
        float localX2Cos = localX2 * cos + x;
        float localX2Sin = localX2 * sin;
        float localY2Cos = localY2 * cos + y;
        float localY2Sin = localY2 * sin;
        float[] offset = this.offset;
        offset[0] = localXCos - localYSin;
        offset[1] = localYCos + localXSin;
        offset[2] = localXCos - localY2Sin;
        offset[3] = localY2Cos + localXSin;
        offset[4] = localX2Cos - localY2Sin;
        offset[5] = localY2Cos + localX2Sin;
        offset[6] = localX2Cos - localYSin;
        offset[7] = localYCos + localX2Sin;
    }

    public void setRegion(TextureRegion region) {
        if (region == null) {
            throw new IllegalArgumentException("region cannot be null.");
        }
        this.region = region;
        float[] uvs = this.uvs;
        if (region instanceof TextureAtlas.AtlasRegion && ((TextureAtlas.AtlasRegion)region).rotate) {
            uvs[4] = region.getU();
            uvs[5] = region.getV2();
            uvs[6] = region.getU();
            uvs[7] = region.getV();
            uvs[0] = region.getU2();
            uvs[1] = region.getV();
            uvs[2] = region.getU2();
            uvs[3] = region.getV2();
        } else {
            uvs[2] = region.getU();
            uvs[3] = region.getV2();
            uvs[4] = region.getU();
            uvs[5] = region.getV();
            uvs[6] = region.getU2();
            uvs[7] = region.getV();
            uvs[0] = region.getU2();
            uvs[1] = region.getV2();
        }
    }

    public TextureRegion getRegion() {
        if (this.region == null) {
            throw new IllegalStateException("Region has not been set: " + this);
        }
        return this.region;
    }

    public void computeWorldVertices(Bone bone, float[] worldVertices, int offset, int stride) {
        float[] vertexOffset = this.offset;
        float x = bone.getWorldX();
        float y = bone.getWorldY();
        float a = bone.getA();
        float b = bone.getB();
        float c = bone.getC();
        float d = bone.getD();
        float offsetX = vertexOffset[6];
        float offsetY = vertexOffset[7];
        worldVertices[offset] = offsetX * a + offsetY * b + x;
        worldVertices[offset + 1] = offsetX * c + offsetY * d + y;
        offsetX = vertexOffset[0];
        offsetY = vertexOffset[1];
        worldVertices[offset += stride] = offsetX * a + offsetY * b + x;
        worldVertices[offset + 1] = offsetX * c + offsetY * d + y;
        offsetX = vertexOffset[2];
        offsetY = vertexOffset[3];
        worldVertices[offset += stride] = offsetX * a + offsetY * b + x;
        worldVertices[offset + 1] = offsetX * c + offsetY * d + y;
        offsetX = vertexOffset[4];
        offsetY = vertexOffset[5];
        worldVertices[offset += stride] = offsetX * a + offsetY * b + x;
        worldVertices[offset + 1] = offsetX * c + offsetY * d + y;
    }

    public float[] getOffset() {
        return this.offset;
    }

    public float[] getUVs() {
        return this.uvs;
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getScaleX() {
        return this.scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return this.scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float getRotation() {
        return this.rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
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

    public Color getColor() {
        return this.color;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public Attachment copy() {
        RegionAttachment copy = new RegionAttachment(this.name);
        copy.region = this.region;
        copy.path = this.path;
        copy.x = this.x;
        copy.y = this.y;
        copy.scaleX = this.scaleX;
        copy.scaleY = this.scaleY;
        copy.rotation = this.rotation;
        copy.width = this.width;
        copy.height = this.height;
        SpineUtils.arraycopy(this.uvs, 0, copy.uvs, 0, 8);
        SpineUtils.arraycopy(this.offset, 0, copy.offset, 0, 8);
        copy.color.set(this.color);
        return copy;
    }
}

