/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.ShortArray;
import com.esotericsoftware.spine.BlendMode;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.ClippingAttachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.esotericsoftware.spine.attachments.SkeletonAttachment;
import com.esotericsoftware.spine.utils.SkeletonClipping;
import com.esotericsoftware.spine.utils.TwoColorPolygonBatch;

public class SkeletonRenderer {
    private static final short[] quadTriangles = new short[]{0, 1, 2, 2, 3, 0};
    private boolean premultipliedAlpha;
    private final FloatArray vertices = new FloatArray(32);
    private final SkeletonClipping clipper = new SkeletonClipping();
    private VertexEffect vertexEffect;
    private final Vector2 temp = new Vector2();
    private final Vector2 temp2 = new Vector2();
    private final Color temp3 = new Color();
    private final Color temp4 = new Color();
    private final Color temp5 = new Color();
    private final Color temp6 = new Color();

    public void draw(Batch batch, Skeleton skeleton) {
        if (batch instanceof TwoColorPolygonBatch) {
            this.draw((TwoColorPolygonBatch)batch, skeleton);
            return;
        }
        if (batch instanceof PolygonSpriteBatch) {
            this.draw((PolygonSpriteBatch)batch, skeleton);
            return;
        }
        if (batch == null) {
            throw new IllegalArgumentException("batch cannot be null.");
        }
        if (skeleton == null) {
            throw new IllegalArgumentException("skeleton cannot be null.");
        }
        VertexEffect vertexEffect = this.vertexEffect;
        if (vertexEffect != null) {
            vertexEffect.begin(skeleton);
        }
        boolean premultipliedAlpha = this.premultipliedAlpha;
        BlendMode blendMode = null;
        float[] vertices = this.vertices.items;
        Color skeletonColor = skeleton.color;
        float r = skeletonColor.r;
        float g = skeletonColor.g;
        float b = skeletonColor.b;
        float a = skeletonColor.a;
        Array<Slot> drawOrder = skeleton.drawOrder;
        int n = drawOrder.size;
        for (int i = 0; i < n; ++i) {
            Slot slot = drawOrder.get(i);
            if (!slot.bone.active) continue;
            Attachment attachment = slot.attachment;
            if (attachment instanceof RegionAttachment) {
                RegionAttachment region = (RegionAttachment)attachment;
                region.computeWorldVertices(slot.getBone(), vertices, 0, 5);
                Color color = region.getColor();
                Color slotColor = slot.getColor();
                float alpha = a * slotColor.a * color.a * 255.0f;
                float multiplier = premultipliedAlpha ? alpha : 255.0f;
                BlendMode slotBlendMode = slot.data.getBlendMode();
                if (slotBlendMode != blendMode) {
                    if (slotBlendMode == BlendMode.additive && premultipliedAlpha) {
                        slotBlendMode = BlendMode.normal;
                        alpha = 0.0f;
                    }
                    blendMode = slotBlendMode;
                    batch.setBlendFunction(blendMode.getSource(premultipliedAlpha), blendMode.getDest());
                }
                float c = NumberUtils.intToFloatColor((int)alpha << 24 | (int)(b * slotColor.b * color.b * multiplier) << 16 | (int)(g * slotColor.g * color.g * multiplier) << 8 | (int)(r * slotColor.r * color.r * multiplier));
                float[] uvs = region.getUVs();
                int u = 0;
                int v = 2;
                while (u < 8) {
                    vertices[v] = c;
                    vertices[v + 1] = uvs[u];
                    vertices[v + 2] = uvs[u + 1];
                    u += 2;
                    v += 5;
                }
                if (vertexEffect != null) {
                    this.applyVertexEffect(vertices, 20, 5, c, 0.0f);
                }
                batch.draw(region.getRegion().getTexture(), vertices, 0, 20);
            } else {
                Skeleton attachmentSkeleton;
                if (attachment instanceof ClippingAttachment) {
                    this.clipper.clipStart(slot, (ClippingAttachment)attachment);
                    continue;
                }
                if (attachment instanceof MeshAttachment) {
                    throw new RuntimeException(batch.getClass().getSimpleName() + " cannot render meshes, PolygonSpriteBatch or TwoColorPolygonBatch is required.");
                }
                if (attachment instanceof SkeletonAttachment && (attachmentSkeleton = ((SkeletonAttachment)attachment).getSkeleton()) != null) {
                    this.draw(batch, attachmentSkeleton);
                }
            }
            this.clipper.clipEnd(slot);
        }
        this.clipper.clipEnd();
        if (vertexEffect != null) {
            vertexEffect.end();
        }
    }

    public void draw(PolygonSpriteBatch batch, Skeleton skeleton) {
        if (batch == null) {
            throw new IllegalArgumentException("batch cannot be null.");
        }
        if (skeleton == null) {
            throw new IllegalArgumentException("skeleton cannot be null.");
        }
        Vector2 tempPosition = this.temp;
        Vector2 tempUV = this.temp2;
        Color tempLight1 = this.temp3;
        Color tempDark1 = this.temp4;
        Color tempLight2 = this.temp5;
        Color tempDark2 = this.temp6;
        VertexEffect vertexEffect = this.vertexEffect;
        if (vertexEffect != null) {
            vertexEffect.begin(skeleton);
        }
        boolean premultipliedAlpha = this.premultipliedAlpha;
        BlendMode blendMode = null;
        int verticesLength = 0;
        float[] vertices = null;
        float[] uvs = null;
        short[] triangles = null;
        Color color = null;
        Color skeletonColor = skeleton.color;
        float r = skeletonColor.r;
        float g = skeletonColor.g;
        float b = skeletonColor.b;
        float a = skeletonColor.a;
        Array<Slot> drawOrder = skeleton.drawOrder;
        int n = drawOrder.size;
        for (int i = 0; i < n; ++i) {
            Slot slot = drawOrder.get(i);
            if (!slot.bone.active) continue;
            Texture texture = null;
            int vertexSize = this.clipper.isClipping() ? 2 : 5;
            Attachment attachment = slot.attachment;
            if (attachment instanceof RegionAttachment) {
                RegionAttachment region = (RegionAttachment)attachment;
                verticesLength = vertexSize << 2;
                vertices = this.vertices.items;
                region.computeWorldVertices(slot.getBone(), vertices, 0, vertexSize);
                triangles = quadTriangles;
                texture = region.getRegion().getTexture();
                uvs = region.getUVs();
                color = region.getColor();
            } else if (attachment instanceof MeshAttachment) {
                MeshAttachment mesh = (MeshAttachment)attachment;
                int count = mesh.getWorldVerticesLength();
                verticesLength = (count >> 1) * vertexSize;
                vertices = this.vertices.setSize(verticesLength);
                mesh.computeWorldVertices(slot, 0, count, vertices, 0, vertexSize);
                triangles = mesh.getTriangles();
                texture = mesh.getRegion().getTexture();
                uvs = mesh.getUVs();
                color = mesh.getColor();
            } else {
                Skeleton attachmentSkeleton;
                if (attachment instanceof ClippingAttachment) {
                    ClippingAttachment clip = (ClippingAttachment)attachment;
                    this.clipper.clipStart(slot, clip);
                    continue;
                }
                if (attachment instanceof SkeletonAttachment && (attachmentSkeleton = ((SkeletonAttachment)attachment).getSkeleton()) != null) {
                    this.draw(batch, attachmentSkeleton);
                }
            }
            if (texture != null) {
                Color slotColor = slot.getColor();
                float alpha = a * slotColor.a * color.a * 255.0f;
                float multiplier = premultipliedAlpha ? alpha : 255.0f;
                BlendMode slotBlendMode = slot.data.getBlendMode();
                if (slotBlendMode != blendMode) {
                    if (slotBlendMode == BlendMode.additive && premultipliedAlpha) {
                        slotBlendMode = BlendMode.normal;
                        alpha = 0.0f;
                    }
                    blendMode = slotBlendMode;
                    batch.setBlendFunction(blendMode.getSource(premultipliedAlpha), blendMode.getDest());
                }
                float c = NumberUtils.intToFloatColor((int)alpha << 24 | (int)(b * slotColor.b * color.b * multiplier) << 16 | (int)(g * slotColor.g * color.g * multiplier) << 8 | (int)(r * slotColor.r * color.r * multiplier));
                if (this.clipper.isClipping()) {
                    this.clipper.clipTriangles(vertices, verticesLength, triangles, triangles.length, uvs, c, 0.0f, false);
                    FloatArray clippedVertices = this.clipper.getClippedVertices();
                    ShortArray clippedTriangles = this.clipper.getClippedTriangles();
                    if (vertexEffect != null) {
                        this.applyVertexEffect(clippedVertices.items, clippedVertices.size, 5, c, 0.0f);
                    }
                    batch.draw(texture, clippedVertices.items, 0, clippedVertices.size, clippedTriangles.items, 0, clippedTriangles.size);
                } else {
                    int u;
                    int v;
                    if (vertexEffect != null) {
                        tempLight1.set(NumberUtils.floatToIntColor(c));
                        tempDark1.set(0);
                        v = 0;
                        u = 0;
                        while (v < verticesLength) {
                            tempPosition.x = vertices[v];
                            tempPosition.y = vertices[v + 1];
                            tempLight2.set(tempLight1);
                            tempDark2.set(tempDark1);
                            tempUV.x = uvs[u];
                            tempUV.y = uvs[u + 1];
                            vertexEffect.transform(tempPosition, tempUV, tempLight2, tempDark2);
                            vertices[v] = tempPosition.x;
                            vertices[v + 1] = tempPosition.y;
                            vertices[v + 2] = tempLight2.toFloatBits();
                            vertices[v + 3] = tempUV.x;
                            vertices[v + 4] = tempUV.y;
                            v += 5;
                            u += 2;
                        }
                    } else {
                        v = 2;
                        u = 0;
                        while (v < verticesLength) {
                            vertices[v] = c;
                            vertices[v + 1] = uvs[u];
                            vertices[v + 2] = uvs[u + 1];
                            v += 5;
                            u += 2;
                        }
                    }
                    batch.draw(texture, vertices, 0, verticesLength, triangles, 0, triangles.length);
                }
            }
            this.clipper.clipEnd(slot);
        }
        this.clipper.clipEnd();
        if (vertexEffect != null) {
            vertexEffect.end();
        }
    }

    public void draw(TwoColorPolygonBatch batch, Skeleton skeleton) {
        if (batch == null) {
            throw new IllegalArgumentException("batch cannot be null.");
        }
        if (skeleton == null) {
            throw new IllegalArgumentException("skeleton cannot be null.");
        }
        Vector2 tempPosition = this.temp;
        Vector2 tempUV = this.temp2;
        Color tempLight1 = this.temp3;
        Color tempDark1 = this.temp4;
        Color tempLight2 = this.temp5;
        Color tempDark2 = this.temp6;
        VertexEffect vertexEffect = this.vertexEffect;
        if (vertexEffect != null) {
            vertexEffect.begin(skeleton);
        }
        boolean premultipliedAlpha = this.premultipliedAlpha;
        batch.setPremultipliedAlpha(premultipliedAlpha);
        BlendMode blendMode = null;
        int verticesLength = 0;
        float[] vertices = null;
        float[] uvs = null;
        short[] triangles = null;
        Color color = null;
        Color skeletonColor = skeleton.color;
        float r = skeletonColor.r;
        float g = skeletonColor.g;
        float b = skeletonColor.b;
        float a = skeletonColor.a;
        Array<Slot> drawOrder = skeleton.drawOrder;
        int n = drawOrder.size;
        for (int i = 0; i < n; ++i) {
            Slot slot = drawOrder.get(i);
            if (!slot.bone.active) continue;
            Texture texture = null;
            int vertexSize = this.clipper.isClipping() ? 2 : 6;
            Attachment attachment = slot.attachment;
            if (attachment instanceof RegionAttachment) {
                RegionAttachment region = (RegionAttachment)attachment;
                verticesLength = vertexSize << 2;
                vertices = this.vertices.items;
                region.computeWorldVertices(slot.getBone(), vertices, 0, vertexSize);
                triangles = quadTriangles;
                texture = region.getRegion().getTexture();
                uvs = region.getUVs();
                color = region.getColor();
            } else if (attachment instanceof MeshAttachment) {
                MeshAttachment mesh = (MeshAttachment)attachment;
                int count = mesh.getWorldVerticesLength();
                verticesLength = (count >> 1) * vertexSize;
                vertices = this.vertices.setSize(verticesLength);
                mesh.computeWorldVertices(slot, 0, count, vertices, 0, vertexSize);
                triangles = mesh.getTriangles();
                texture = mesh.getRegion().getTexture();
                uvs = mesh.getUVs();
                color = mesh.getColor();
            } else {
                Skeleton attachmentSkeleton;
                if (attachment instanceof ClippingAttachment) {
                    ClippingAttachment clip = (ClippingAttachment)attachment;
                    this.clipper.clipStart(slot, clip);
                    continue;
                }
                if (attachment instanceof SkeletonAttachment && (attachmentSkeleton = ((SkeletonAttachment)attachment).getSkeleton()) != null) {
                    this.draw(batch, attachmentSkeleton);
                }
            }
            if (texture != null) {
                float dark;
                Color lightColor = slot.getColor();
                float alpha = a * lightColor.a * color.a * 255.0f;
                float multiplier = premultipliedAlpha ? alpha : 255.0f;
                BlendMode slotBlendMode = slot.data.getBlendMode();
                if (slotBlendMode != blendMode) {
                    if (slotBlendMode == BlendMode.additive && premultipliedAlpha) {
                        slotBlendMode = BlendMode.normal;
                        alpha = 0.0f;
                    }
                    blendMode = slotBlendMode;
                    batch.setBlendFunction(blendMode.getSource(premultipliedAlpha), blendMode.getDest());
                }
                float red = r * color.r * multiplier;
                float green = g * color.g * multiplier;
                float blue = b * color.b * multiplier;
                float light = NumberUtils.intToFloatColor((int)alpha << 24 | (int)(blue * lightColor.b) << 16 | (int)(green * lightColor.g) << 8 | (int)(red * lightColor.r));
                Color darkColor = slot.getDarkColor();
                float f = dark = darkColor == null ? 0.0f : NumberUtils.intToFloatColor((int)(blue * darkColor.b) << 16 | (int)(green * darkColor.g) << 8 | (int)(red * darkColor.r));
                if (this.clipper.isClipping()) {
                    this.clipper.clipTriangles(vertices, verticesLength, triangles, triangles.length, uvs, light, dark, true);
                    FloatArray clippedVertices = this.clipper.getClippedVertices();
                    ShortArray clippedTriangles = this.clipper.getClippedTriangles();
                    if (vertexEffect != null) {
                        this.applyVertexEffect(clippedVertices.items, clippedVertices.size, 6, light, dark);
                    }
                    batch.drawTwoColor(texture, clippedVertices.items, 0, clippedVertices.size, clippedTriangles.items, 0, clippedTriangles.size);
                } else {
                    int u;
                    int v;
                    if (vertexEffect != null) {
                        tempLight1.set(NumberUtils.floatToIntColor(light));
                        tempDark1.set(NumberUtils.floatToIntColor(dark));
                        v = 0;
                        u = 0;
                        while (v < verticesLength) {
                            tempPosition.x = vertices[v];
                            tempPosition.y = vertices[v + 1];
                            tempLight2.set(tempLight1);
                            tempDark2.set(tempDark1);
                            tempUV.x = uvs[u];
                            tempUV.y = uvs[u + 1];
                            vertexEffect.transform(tempPosition, tempUV, tempLight2, tempDark2);
                            vertices[v] = tempPosition.x;
                            vertices[v + 1] = tempPosition.y;
                            vertices[v + 2] = tempLight2.toFloatBits();
                            vertices[v + 3] = tempDark2.toFloatBits();
                            vertices[v + 4] = tempUV.x;
                            vertices[v + 5] = tempUV.y;
                            v += 6;
                            u += 2;
                        }
                    } else {
                        v = 2;
                        u = 0;
                        while (v < verticesLength) {
                            vertices[v] = light;
                            vertices[v + 1] = dark;
                            vertices[v + 2] = uvs[u];
                            vertices[v + 3] = uvs[u + 1];
                            v += 6;
                            u += 2;
                        }
                    }
                    batch.drawTwoColor(texture, vertices, 0, verticesLength, triangles, 0, triangles.length);
                }
            }
            this.clipper.clipEnd(slot);
        }
        this.clipper.clipEnd();
        if (vertexEffect != null) {
            vertexEffect.end();
        }
    }

    private void applyVertexEffect(float[] vertices, int verticesLength, int stride, float light, float dark) {
        Vector2 tempPosition = this.temp;
        Vector2 tempUV = this.temp2;
        Color tempLight1 = this.temp3;
        Color tempDark1 = this.temp4;
        Color tempLight2 = this.temp5;
        Color tempDark2 = this.temp6;
        VertexEffect vertexEffect = this.vertexEffect;
        tempLight1.set(NumberUtils.floatToIntColor(light));
        tempDark1.set(NumberUtils.floatToIntColor(dark));
        if (stride == 5) {
            for (int v = 0; v < verticesLength; v += stride) {
                tempPosition.x = vertices[v];
                tempPosition.y = vertices[v + 1];
                tempUV.x = vertices[v + 3];
                tempUV.y = vertices[v + 4];
                tempLight2.set(tempLight1);
                tempDark2.set(tempDark1);
                vertexEffect.transform(tempPosition, tempUV, tempLight2, tempDark2);
                vertices[v] = tempPosition.x;
                vertices[v + 1] = tempPosition.y;
                vertices[v + 2] = tempLight2.toFloatBits();
                vertices[v + 3] = tempUV.x;
                vertices[v + 4] = tempUV.y;
            }
        } else {
            for (int v = 0; v < verticesLength; v += stride) {
                tempPosition.x = vertices[v];
                tempPosition.y = vertices[v + 1];
                tempUV.x = vertices[v + 4];
                tempUV.y = vertices[v + 5];
                tempLight2.set(tempLight1);
                tempDark2.set(tempDark1);
                vertexEffect.transform(tempPosition, tempUV, tempLight2, tempDark2);
                vertices[v] = tempPosition.x;
                vertices[v + 1] = tempPosition.y;
                vertices[v + 2] = tempLight2.toFloatBits();
                vertices[v + 3] = tempDark2.toFloatBits();
                vertices[v + 4] = tempUV.x;
                vertices[v + 5] = tempUV.y;
            }
        }
    }

    public boolean getPremultipliedAlpha() {
        return this.premultipliedAlpha;
    }

    public void setPremultipliedAlpha(boolean premultipliedAlpha) {
        this.premultipliedAlpha = premultipliedAlpha;
    }

    public VertexEffect getVertexEffect() {
        return this.vertexEffect;
    }

    public void setVertexEffect(VertexEffect vertexEffect) {
        this.vertexEffect = vertexEffect;
    }

    public static interface VertexEffect {
        public void begin(Skeleton var1);

        public void transform(Vector2 var1, Vector2 var2, Color var3, Color var4);

        public void end();
    }
}

