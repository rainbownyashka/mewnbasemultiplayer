/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;
import com.esotericsoftware.spine.attachments.ClippingAttachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.PathAttachment;
import com.esotericsoftware.spine.attachments.PointAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;

public class SkeletonRendererDebug {
    private static final Color boneLineColor = Color.RED;
    private static final Color boneOriginColor = Color.GREEN;
    private static final Color attachmentLineColor = new Color(0.0f, 0.0f, 1.0f, 0.5f);
    private static final Color triangleLineColor = new Color(1.0f, 0.64f, 0.0f, 0.5f);
    private static final Color aabbColor = new Color(0.0f, 1.0f, 0.0f, 0.5f);
    private final ShapeRenderer shapes;
    private boolean drawBones = true;
    private boolean drawRegionAttachments = true;
    private boolean drawBoundingBoxes = true;
    private boolean drawPoints = true;
    private boolean drawMeshHull = true;
    private boolean drawMeshTriangles = true;
    private boolean drawPaths = true;
    private boolean drawClipping = true;
    private final SkeletonBounds bounds = new SkeletonBounds();
    private final FloatArray vertices = new FloatArray(32);
    private float scale = 1.0f;
    private float boneWidth = 2.0f;
    private boolean premultipliedAlpha;
    private final Vector2 temp1 = new Vector2();
    private final Vector2 temp2 = new Vector2();

    public SkeletonRendererDebug() {
        this.shapes = new ShapeRenderer();
    }

    public SkeletonRendererDebug(ShapeRenderer shapes) {
        if (shapes == null) {
            throw new IllegalArgumentException("shapes cannot be null.");
        }
        this.shapes = shapes;
    }

    public void draw(Skeleton skeleton) {
        Slot slot;
        Bone bone;
        int i;
        int n;
        if (skeleton == null) {
            throw new IllegalArgumentException("skeleton cannot be null.");
        }
        Gdx.gl.glEnable(3042);
        int srcFunc = this.premultipliedAlpha ? 1 : 770;
        Gdx.gl.glBlendFunc(srcFunc, 771);
        ShapeRenderer shapes = this.shapes;
        Array<Bone> bones = skeleton.getBones();
        Array<Slot> slots = skeleton.getSlots();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (this.drawBones) {
            n = bones.size;
            for (i = 0; i < n; ++i) {
                bone = bones.get(i);
                if (bone.parent == null || !bone.active) continue;
                float length = bone.data.length;
                float width = this.boneWidth;
                if (length == 0.0f) {
                    length = 8.0f;
                    width /= 2.0f;
                    shapes.setColor(boneOriginColor);
                } else {
                    shapes.setColor(boneLineColor);
                }
                float x = length * bone.a + bone.worldX;
                float y = length * bone.c + bone.worldY;
                shapes.rectLine(bone.worldX, bone.worldY, x, y, width * this.scale);
            }
            shapes.x(skeleton.getX(), skeleton.getY(), 4.0f * this.scale);
        }
        if (this.drawPoints) {
            shapes.setColor(boneOriginColor);
            n = slots.size;
            for (i = 0; i < n; ++i) {
                slot = slots.get(i);
                Attachment attachment = slot.attachment;
                if (!(attachment instanceof PointAttachment)) continue;
                PointAttachment point = (PointAttachment)attachment;
                point.computeWorldPosition(slot.getBone(), this.temp1);
                this.temp2.set(8.0f, 0.0f).rotate(point.computeWorldRotation(slot.getBone()));
                shapes.rectLine(this.temp1, this.temp2, this.boneWidth / 2.0f * this.scale);
            }
        }
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        if (this.drawRegionAttachments) {
            shapes.setColor(attachmentLineColor);
            n = slots.size;
            for (i = 0; i < n; ++i) {
                slot = slots.get(i);
                Attachment attachment = slot.attachment;
                if (!(attachment instanceof RegionAttachment)) continue;
                RegionAttachment region = (RegionAttachment)attachment;
                float[] vertices = this.vertices.items;
                region.computeWorldVertices(slot.getBone(), vertices, 0, 2);
                shapes.line(vertices[0], vertices[1], vertices[2], vertices[3]);
                shapes.line(vertices[2], vertices[3], vertices[4], vertices[5]);
                shapes.line(vertices[4], vertices[5], vertices[6], vertices[7]);
                shapes.line(vertices[6], vertices[7], vertices[0], vertices[1]);
            }
        }
        if (this.drawMeshHull || this.drawMeshTriangles) {
            n = slots.size;
            for (i = 0; i < n; ++i) {
                slot = slots.get(i);
                Attachment attachment = slot.attachment;
                if (!(attachment instanceof MeshAttachment)) continue;
                MeshAttachment mesh = (MeshAttachment)attachment;
                float[] vertices = this.vertices.setSize(mesh.getWorldVerticesLength());
                mesh.computeWorldVertices(slot, 0, mesh.getWorldVerticesLength(), vertices, 0, 2);
                short[] triangles = mesh.getTriangles();
                int hullLength = mesh.getHullLength();
                if (this.drawMeshTriangles) {
                    shapes.setColor(triangleLineColor);
                    int nn = triangles.length;
                    for (int ii = 0; ii < nn; ii += 3) {
                        int v1 = triangles[ii] * 2;
                        int v2 = triangles[ii + 1] * 2;
                        int v3 = triangles[ii + 2] * 2;
                        shapes.triangle(vertices[v1], vertices[v1 + 1], vertices[v2], vertices[v2 + 1], vertices[v3], vertices[v3 + 1]);
                    }
                }
                if (!this.drawMeshHull || hullLength <= 0) continue;
                shapes.setColor(attachmentLineColor);
                float lastX = vertices[hullLength - 2];
                float lastY = vertices[hullLength - 1];
                int nn = hullLength;
                for (int ii = 0; ii < nn; ii += 2) {
                    float x = vertices[ii];
                    float y = vertices[ii + 1];
                    shapes.line(x, y, lastX, lastY);
                    lastX = x;
                    lastY = y;
                }
            }
        }
        if (this.drawBoundingBoxes) {
            SkeletonBounds bounds = this.bounds;
            bounds.update(skeleton, true);
            shapes.setColor(aabbColor);
            shapes.rect(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
            Array<FloatArray> polygons = bounds.getPolygons();
            Array<BoundingBoxAttachment> boxes = bounds.getBoundingBoxes();
            int n2 = polygons.size;
            for (int i2 = 0; i2 < n2; ++i2) {
                FloatArray polygon = polygons.get(i2);
                shapes.setColor(boxes.get(i2).getColor());
                shapes.polygon(polygon.items, 0, polygon.size);
            }
        }
        if (this.drawClipping) {
            int n3 = slots.size;
            for (int i3 = 0; i3 < n3; ++i3) {
                slot = slots.get(i3);
                Attachment attachment = slot.attachment;
                if (!(attachment instanceof ClippingAttachment)) continue;
                ClippingAttachment clip = (ClippingAttachment)attachment;
                int nn = clip.getWorldVerticesLength();
                float[] vertices = this.vertices.setSize(nn);
                clip.computeWorldVertices(slot, 0, nn, vertices, 0, 2);
                shapes.setColor(clip.getColor());
                for (int ii = 2; ii < nn; ii += 2) {
                    shapes.line(vertices[ii - 2], vertices[ii - 1], vertices[ii], vertices[ii + 1]);
                }
                shapes.line(vertices[0], vertices[1], vertices[nn - 2], vertices[nn - 1]);
            }
        }
        if (this.drawPaths) {
            int n4 = slots.size;
            for (int i4 = 0; i4 < n4; ++i4) {
                slot = slots.get(i4);
                Attachment attachment = slot.attachment;
                if (!(attachment instanceof PathAttachment)) continue;
                PathAttachment path = (PathAttachment)attachment;
                int nn = path.getWorldVerticesLength();
                float[] vertices = this.vertices.setSize(nn);
                path.computeWorldVertices(slot, 0, nn, vertices, 0, 2);
                Color color = path.getColor();
                float x1 = vertices[2];
                float y1 = vertices[3];
                float x2 = 0.0f;
                float y2 = 0.0f;
                if (path.getClosed()) {
                    shapes.setColor(color);
                    float cx1 = vertices[0];
                    float cy1 = vertices[1];
                    float cx2 = vertices[nn - 2];
                    float cy2 = vertices[nn - 1];
                    x2 = vertices[nn - 4];
                    y2 = vertices[nn - 3];
                    shapes.curve(x1, y1, cx1, cy1, cx2, cy2, x2, y2, 32);
                    shapes.setColor(Color.LIGHT_GRAY);
                    shapes.line(x1, y1, cx1, cy1);
                    shapes.line(x2, y2, cx2, cy2);
                }
                nn -= 4;
                for (int ii = 4; ii < nn; ii += 6) {
                    float cx1 = vertices[ii];
                    float cy1 = vertices[ii + 1];
                    float cx2 = vertices[ii + 2];
                    float cy2 = vertices[ii + 3];
                    x2 = vertices[ii + 4];
                    y2 = vertices[ii + 5];
                    shapes.setColor(color);
                    shapes.curve(x1, y1, cx1, cy1, cx2, cy2, x2, y2, 32);
                    shapes.setColor(Color.LIGHT_GRAY);
                    shapes.line(x1, y1, cx1, cy1);
                    shapes.line(x2, y2, cx2, cy2);
                    x1 = x2;
                    y1 = y2;
                }
            }
        }
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (this.drawBones) {
            shapes.setColor(boneOriginColor);
            int n5 = bones.size;
            for (int i5 = 0; i5 < n5; ++i5) {
                bone = bones.get(i5);
                if (!bone.active) continue;
                shapes.circle(bone.worldX, bone.worldY, 3.0f * this.scale, 8);
            }
        }
        if (this.drawPoints) {
            shapes.setColor(boneOriginColor);
            int n6 = slots.size;
            for (int i6 = 0; i6 < n6; ++i6) {
                slot = slots.get(i6);
                Attachment attachment = slot.attachment;
                if (!(attachment instanceof PointAttachment)) continue;
                PointAttachment point = (PointAttachment)attachment;
                point.computeWorldPosition(slot.getBone(), this.temp1);
                shapes.circle(this.temp1.x, this.temp1.y, 3.0f * this.scale, 8);
            }
        }
        shapes.end();
    }

    public ShapeRenderer getShapeRenderer() {
        return this.shapes;
    }

    public void setBones(boolean bones) {
        this.drawBones = bones;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setRegionAttachments(boolean regionAttachments) {
        this.drawRegionAttachments = regionAttachments;
    }

    public void setBoundingBoxes(boolean boundingBoxes) {
        this.drawBoundingBoxes = boundingBoxes;
    }

    public void setMeshHull(boolean meshHull) {
        this.drawMeshHull = meshHull;
    }

    public void setMeshTriangles(boolean meshTriangles) {
        this.drawMeshTriangles = meshTriangles;
    }

    public void setPaths(boolean paths) {
        this.drawPaths = paths;
    }

    public void setPoints(boolean points) {
        this.drawPoints = points;
    }

    public void setClipping(boolean clipping) {
        this.drawClipping = clipping;
    }

    public void setPremultipliedAlpha(boolean premultipliedAlpha) {
        this.premultipliedAlpha = premultipliedAlpha;
    }
}

