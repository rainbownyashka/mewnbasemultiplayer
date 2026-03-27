/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.ClippingAttachment;
import com.esotericsoftware.spine.utils.Triangulator;

public class SkeletonClipping {
    private final Triangulator triangulator = new Triangulator();
    private final FloatArray clippingPolygon = new FloatArray();
    private final FloatArray clipOutput = new FloatArray(128);
    private final FloatArray clippedVertices = new FloatArray(128);
    private final ShortArray clippedTriangles = new ShortArray(128);
    private final FloatArray scratch = new FloatArray();
    private ClippingAttachment clipAttachment;
    private Array<FloatArray> clippingPolygons;

    public int clipStart(Slot slot, ClippingAttachment clip) {
        if (this.clipAttachment != null) {
            return 0;
        }
        int n = clip.getWorldVerticesLength();
        if (n < 6) {
            return 0;
        }
        this.clipAttachment = clip;
        float[] vertices = this.clippingPolygon.setSize(n);
        clip.computeWorldVertices(slot, 0, n, vertices, 0, 2);
        SkeletonClipping.makeClockwise(this.clippingPolygon);
        ShortArray triangles = this.triangulator.triangulate(this.clippingPolygon);
        this.clippingPolygons = this.triangulator.decompose(this.clippingPolygon, triangles);
        for (FloatArray polygon : this.clippingPolygons) {
            SkeletonClipping.makeClockwise(polygon);
            polygon.add(polygon.items[0]);
            polygon.add(polygon.items[1]);
        }
        return this.clippingPolygons.size;
    }

    public void clipEnd(Slot slot) {
        if (this.clipAttachment != null && this.clipAttachment.getEndSlot() == slot.getData()) {
            this.clipEnd();
        }
    }

    public void clipEnd() {
        if (this.clipAttachment == null) {
            return;
        }
        this.clipAttachment = null;
        this.clippingPolygons = null;
        this.clippedVertices.clear();
        this.clippedTriangles.clear();
        this.clippingPolygon.clear();
    }

    public boolean isClipping() {
        return this.clipAttachment != null;
    }

    public void clipTriangles(float[] vertices, int verticesLength, short[] triangles, int trianglesLength, float[] uvs, float light, float dark, boolean twoColor) {
        FloatArray clipOutput = this.clipOutput;
        FloatArray clippedVertices = this.clippedVertices;
        ShortArray clippedTriangles = this.clippedTriangles;
        T[] polygons = this.clippingPolygons.items;
        int polygonsCount = this.clippingPolygons.size;
        int vertexSize = twoColor ? 6 : 5;
        short index = 0;
        clippedVertices.clear();
        clippedTriangles.clear();
        block0: for (int i = 0; i < trianglesLength; i += 3) {
            int vertexOffset = triangles[i] << 1;
            float x1 = vertices[vertexOffset];
            float y1 = vertices[vertexOffset + 1];
            float u1 = uvs[vertexOffset];
            float v1 = uvs[vertexOffset + 1];
            vertexOffset = triangles[i + 1] << 1;
            float x2 = vertices[vertexOffset];
            float y2 = vertices[vertexOffset + 1];
            float u2 = uvs[vertexOffset];
            float v2 = uvs[vertexOffset + 1];
            vertexOffset = triangles[i + 2] << 1;
            float x3 = vertices[vertexOffset];
            float y3 = vertices[vertexOffset + 1];
            float u3 = uvs[vertexOffset];
            float v3 = uvs[vertexOffset + 1];
            for (int p = 0; p < polygonsCount; ++p) {
                int s = clippedVertices.size;
                if (this.clip(x1, y1, x2, y2, x3, y3, (FloatArray)polygons[p], clipOutput)) {
                    int clipOutputLength = clipOutput.size;
                    if (clipOutputLength == 0) continue;
                    float d0 = y2 - y3;
                    float d1 = x3 - x2;
                    float d2 = x1 - x3;
                    float d4 = y3 - y1;
                    float d = 1.0f / (d0 * d2 + d1 * (y1 - y3));
                    int clipOutputCount = clipOutputLength >> 1;
                    float[] clipOutputItems = clipOutput.items;
                    float[] clippedVerticesItems = clippedVertices.setSize(s + clipOutputCount * vertexSize);
                    for (int ii = 0; ii < clipOutputLength; ii += 2) {
                        float x = clipOutputItems[ii];
                        float y = clipOutputItems[ii + 1];
                        clippedVerticesItems[s] = x;
                        clippedVerticesItems[s + 1] = y;
                        clippedVerticesItems[s + 2] = light;
                        if (twoColor) {
                            clippedVerticesItems[s + 3] = dark;
                            s += 4;
                        } else {
                            s += 3;
                        }
                        float c0 = x - x3;
                        float c1 = y - y3;
                        float a = (d0 * c0 + d1 * c1) * d;
                        float b = (d4 * c0 + d2 * c1) * d;
                        float c = 1.0f - a - b;
                        clippedVerticesItems[s] = u1 * a + u2 * b + u3 * c;
                        clippedVerticesItems[s + 1] = v1 * a + v2 * b + v3 * c;
                        s += 2;
                    }
                    s = clippedTriangles.size;
                    short[] clippedTrianglesItems = clippedTriangles.setSize(s + 3 * (clipOutputCount - 2));
                    --clipOutputCount;
                    for (int ii = 1; ii < clipOutputCount; ++ii) {
                        clippedTrianglesItems[s] = index;
                        clippedTrianglesItems[s + 1] = (short)(index + ii);
                        clippedTrianglesItems[s + 2] = (short)(index + ii + 1);
                        s += 3;
                    }
                    index = (short)(index + (clipOutputCount + 1));
                    continue;
                }
                float[] clippedVerticesItems = clippedVertices.setSize(s + 3 * vertexSize);
                clippedVerticesItems[s] = x1;
                clippedVerticesItems[s + 1] = y1;
                clippedVerticesItems[s + 2] = light;
                if (!twoColor) {
                    clippedVerticesItems[s + 3] = u1;
                    clippedVerticesItems[s + 4] = v1;
                    clippedVerticesItems[s + 5] = x2;
                    clippedVerticesItems[s + 6] = y2;
                    clippedVerticesItems[s + 7] = light;
                    clippedVerticesItems[s + 8] = u2;
                    clippedVerticesItems[s + 9] = v2;
                    clippedVerticesItems[s + 10] = x3;
                    clippedVerticesItems[s + 11] = y3;
                    clippedVerticesItems[s + 12] = light;
                    clippedVerticesItems[s + 13] = u3;
                    clippedVerticesItems[s + 14] = v3;
                } else {
                    clippedVerticesItems[s + 3] = dark;
                    clippedVerticesItems[s + 4] = u1;
                    clippedVerticesItems[s + 5] = v1;
                    clippedVerticesItems[s + 6] = x2;
                    clippedVerticesItems[s + 7] = y2;
                    clippedVerticesItems[s + 8] = light;
                    clippedVerticesItems[s + 9] = dark;
                    clippedVerticesItems[s + 10] = u2;
                    clippedVerticesItems[s + 11] = v2;
                    clippedVerticesItems[s + 12] = x3;
                    clippedVerticesItems[s + 13] = y3;
                    clippedVerticesItems[s + 14] = light;
                    clippedVerticesItems[s + 15] = dark;
                    clippedVerticesItems[s + 16] = u3;
                    clippedVerticesItems[s + 17] = v3;
                }
                s = clippedTriangles.size;
                short[] clippedTrianglesItems = clippedTriangles.setSize(s + 3);
                clippedTrianglesItems[s] = index;
                clippedTrianglesItems[s + 1] = (short)(index + 1);
                clippedTrianglesItems[s + 2] = (short)(index + 2);
                index = (short)(index + 3);
                continue block0;
            }
        }
    }

    boolean clip(float x1, float y1, float x2, float y2, float x3, float y3, FloatArray clippingArea, FloatArray output) {
        FloatArray originalOutput = output;
        boolean clipped = false;
        FloatArray input = null;
        if (clippingArea.size % 4 >= 2) {
            input = output;
            output = this.scratch;
        } else {
            input = this.scratch;
        }
        input.clear();
        input.add(x1);
        input.add(y1);
        input.add(x2);
        input.add(y2);
        input.add(x3);
        input.add(y3);
        input.add(x1);
        input.add(y1);
        output.clear();
        float[] clippingVertices = clippingArea.items;
        int clippingVerticesLast = clippingArea.size - 4;
        int i = 0;
        while (true) {
            float edgeX = clippingVertices[i];
            float edgeY = clippingVertices[i + 1];
            float edgeX2 = clippingVertices[i + 2];
            float edgeY2 = clippingVertices[i + 3];
            float deltaX = edgeX - edgeX2;
            float deltaY = edgeY - edgeY2;
            float[] inputVertices = input.items;
            int inputVerticesLength = input.size - 2;
            int outputStart = output.size;
            for (int ii = 0; ii < inputVerticesLength; ii += 2) {
                float ua;
                float s;
                float c2;
                float c0;
                boolean side2;
                float inputX = inputVertices[ii];
                float inputY = inputVertices[ii + 1];
                float inputY2 = inputVertices[ii + 3];
                float inputX2 = inputVertices[ii + 2];
                boolean bl = side2 = deltaX * (inputY2 - edgeY2) - deltaY * (inputX2 - edgeX2) > 0.0f;
                if (deltaX * (inputY - edgeY2) - deltaY * (inputX - edgeX2) > 0.0f) {
                    if (side2) {
                        output.add(inputX2);
                        output.add(inputY2);
                        continue;
                    }
                    c0 = inputY2 - inputY;
                    c2 = inputX2 - inputX;
                    s = c0 * (edgeX2 - edgeX) - c2 * (edgeY2 - edgeY);
                    if (Math.abs(s) > 1.0E-6f) {
                        ua = (c2 * (edgeY - inputY) - c0 * (edgeX - inputX)) / s;
                        output.add(edgeX + (edgeX2 - edgeX) * ua);
                        output.add(edgeY + (edgeY2 - edgeY) * ua);
                    } else {
                        output.add(edgeX);
                        output.add(edgeY);
                    }
                } else if (side2) {
                    c0 = inputY2 - inputY;
                    c2 = inputX2 - inputX;
                    s = c0 * (edgeX2 - edgeX) - c2 * (edgeY2 - edgeY);
                    if (Math.abs(s) > 1.0E-6f) {
                        ua = (c2 * (edgeY - inputY) - c0 * (edgeX - inputX)) / s;
                        output.add(edgeX + (edgeX2 - edgeX) * ua);
                        output.add(edgeY + (edgeY2 - edgeY) * ua);
                    } else {
                        output.add(edgeX);
                        output.add(edgeY);
                    }
                    output.add(inputX2);
                    output.add(inputY2);
                }
                clipped = true;
            }
            if (outputStart == output.size) {
                originalOutput.clear();
                return true;
            }
            output.add(output.items[0]);
            output.add(output.items[1]);
            if (i == clippingVerticesLast) break;
            FloatArray temp = output;
            output = input;
            output.clear();
            input = temp;
            i += 2;
        }
        if (originalOutput != output) {
            originalOutput.clear();
            originalOutput.addAll(output.items, 0, output.size - 2);
        } else {
            originalOutput.setSize(originalOutput.size - 2);
        }
        return clipped;
    }

    public FloatArray getClippedVertices() {
        return this.clippedVertices;
    }

    public ShortArray getClippedTriangles() {
        return this.clippedTriangles;
    }

    static void makeClockwise(FloatArray polygon) {
        int i;
        float[] vertices = polygon.items;
        int verticeslength = polygon.size;
        float area = vertices[verticeslength - 2] * vertices[1] - vertices[0] * vertices[verticeslength - 1];
        int n = verticeslength - 3;
        for (i = 0; i < n; i += 2) {
            float p1x = vertices[i];
            float p1y = vertices[i + 1];
            float p2x = vertices[i + 2];
            float p2y = vertices[i + 3];
            area += p1x * p2y - p2x * p1y;
        }
        if (area < 0.0f) {
            return;
        }
        int lastX = verticeslength - 2;
        int n2 = verticeslength >> 1;
        for (i = 0; i < n2; i += 2) {
            float x = vertices[i];
            float y = vertices[i + 1];
            int other = lastX - i;
            vertices[i] = vertices[other];
            vertices[i + 1] = vertices[other + 1];
            vertices[other] = x;
            vertices[other + 1] = y;
        }
    }
}

