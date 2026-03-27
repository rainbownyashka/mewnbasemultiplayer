/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BooleanArray;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ShortArray;

class Triangulator {
    private final Array<FloatArray> convexPolygons = new Array();
    private final Array<ShortArray> convexPolygonsIndices = new Array();
    private final ShortArray indicesArray = new ShortArray();
    private final BooleanArray isConcaveArray = new BooleanArray();
    private final ShortArray triangles = new ShortArray();
    private final Pool<FloatArray> polygonPool = new Pool(){

        protected FloatArray newObject() {
            return new FloatArray(16);
        }
    };
    private final Pool<ShortArray> polygonIndicesPool = new Pool(){

        protected ShortArray newObject() {
            return new ShortArray(16);
        }
    };

    Triangulator() {
    }

    /*
     * Unable to fully structure code
     */
    public ShortArray triangulate(FloatArray verticesArray) {
        vertices = verticesArray.items;
        vertexCount = verticesArray.size >> 1;
        indicesArray = this.indicesArray;
        indicesArray.clear();
        indices = indicesArray.setSize(vertexCount);
        for (i = 0; i < vertexCount; i = (int)((short)(i + 1))) {
            indices[i] = i;
        }
        isConcaveArray = this.isConcaveArray;
        isConcave = isConcaveArray.setSize(vertexCount);
        n = vertexCount;
        for (i = 0; i < n; ++i) {
            isConcave[i] = Triangulator.isConcave(i, vertexCount, vertices, indices);
        }
        triangles = this.triangles;
        triangles.clear();
        triangles.ensureCapacity(Math.max(0, vertexCount - 2) << 2);
        while (vertexCount > 3) {
            previous = vertexCount - 1;
            i = 0;
            next = 1;
            block3: while (true) {
                if (isConcave[i]) ** GOTO lbl39
                p1 = indices[previous] << 1;
                p2 = indices[i] << 1;
                p3 = indices[next] << 1;
                p1x = vertices[p1];
                p1y = vertices[p1 + 1];
                p2x = vertices[p2];
                p2y = vertices[p2 + 1];
                p3x = vertices[p3];
                p3y = vertices[p3 + 1];
                ii = (next + 1) % vertexCount;
                while (ii != previous) {
                    if (!(isConcave[ii] && Triangulator.positiveArea(p3x, p3y, p1x, p1y, vx = vertices[v = indices[ii] << 1], vy = vertices[v + 1]) && Triangulator.positiveArea(p1x, p1y, p2x, p2y, vx, vy) && Triangulator.positiveArea(p2x, p2y, p3x, p3y, vx, vy))) {
                        ii = (ii + 1) % vertexCount;
                        continue;
                    }
lbl39:
                    // 3 sources

                    if (next == 0) {
                        while (isConcave[i] && --i > 0) {
                        }
                        break block3;
                    }
                    previous = i;
                    i = next;
                    next = (next + 1) % vertexCount;
                    continue block3;
                }
                break;
            }
            triangles.add(indices[(vertexCount + i - 1) % vertexCount]);
            triangles.add(indices[i]);
            triangles.add(indices[(i + 1) % vertexCount]);
            indicesArray.removeIndex(i);
            isConcaveArray.removeIndex(i);
            previousIndex = (--vertexCount + i - 1) % vertexCount;
            nextIndex = i == vertexCount ? 0 : i;
            isConcave[previousIndex] = Triangulator.isConcave(previousIndex, vertexCount, vertices, indices);
            isConcave[nextIndex] = Triangulator.isConcave(nextIndex, vertexCount, vertices, indices);
        }
        if (vertexCount == 3) {
            triangles.add(indices[2]);
            triangles.add(indices[0]);
            triangles.add(indices[1]);
        }
        return triangles;
    }

    public Array<FloatArray> decompose(FloatArray verticesArray, ShortArray triangles) {
        int i;
        float[] vertices = verticesArray.items;
        Array<FloatArray> convexPolygons = this.convexPolygons;
        this.polygonPool.freeAll(convexPolygons);
        convexPolygons.clear();
        Array<ShortArray> convexPolygonsIndices = this.convexPolygonsIndices;
        this.polygonIndicesPool.freeAll(convexPolygonsIndices);
        convexPolygonsIndices.clear();
        ShortArray polygonIndices = this.polygonIndicesPool.obtain();
        polygonIndices.clear();
        FloatArray polygon = this.polygonPool.obtain();
        polygon.clear();
        int fanBaseIndex = -1;
        int lastWinding = 0;
        short[] trianglesItems = triangles.items;
        int n = triangles.size;
        for (i = 0; i < n; i += 3) {
            int t1 = trianglesItems[i] << 1;
            int t2 = trianglesItems[i + 1] << 1;
            int t3 = trianglesItems[i + 2] << 1;
            float x1 = vertices[t1];
            float y1 = vertices[t1 + 1];
            float x2 = vertices[t2];
            float y2 = vertices[t2 + 1];
            float x3 = vertices[t3];
            float y3 = vertices[t3 + 1];
            boolean merged = false;
            if (fanBaseIndex == t1) {
                int o = polygon.size - 4;
                float[] p = polygon.items;
                int winding1 = Triangulator.winding(p[o], p[o + 1], p[o + 2], p[o + 3], x3, y3);
                int winding2 = Triangulator.winding(x3, y3, p[0], p[1], p[2], p[3]);
                if (winding1 == lastWinding && winding2 == lastWinding) {
                    polygon.add(x3);
                    polygon.add(y3);
                    polygonIndices.add(t3);
                    merged = true;
                }
            }
            if (merged) continue;
            if (polygon.size > 0) {
                convexPolygons.add(polygon);
                convexPolygonsIndices.add(polygonIndices);
            } else {
                this.polygonPool.free(polygon);
                this.polygonIndicesPool.free(polygonIndices);
            }
            polygon = this.polygonPool.obtain();
            polygon.clear();
            polygon.add(x1);
            polygon.add(y1);
            polygon.add(x2);
            polygon.add(y2);
            polygon.add(x3);
            polygon.add(y3);
            polygonIndices = this.polygonIndicesPool.obtain();
            polygonIndices.clear();
            polygonIndices.add(t1);
            polygonIndices.add(t2);
            polygonIndices.add(t3);
            lastWinding = Triangulator.winding(x1, y1, x2, y2, x3, y3);
            fanBaseIndex = t1;
        }
        if (polygon.size > 0) {
            convexPolygons.add(polygon);
            convexPolygonsIndices.add(polygonIndices);
        }
        n = convexPolygons.size;
        for (i = 0; i < n; ++i) {
            polygonIndices = convexPolygonsIndices.get(i);
            if (polygonIndices.size == 0) continue;
            short firstIndex = polygonIndices.get(0);
            short lastIndex = polygonIndices.get(polygonIndices.size - 1);
            polygon = convexPolygons.get(i);
            int o = polygon.size - 4;
            float[] p = polygon.items;
            float prevPrevX = p[o];
            float prevPrevY = p[o + 1];
            float prevX = p[o + 2];
            float prevY = p[o + 3];
            float firstX = p[0];
            float firstY = p[1];
            float secondX = p[2];
            float secondY = p[3];
            int winding = Triangulator.winding(prevPrevX, prevPrevY, prevX, prevY, firstX, firstY);
            for (int ii = 0; ii < n; ++ii) {
                if (ii == i) continue;
                ShortArray otherIndices = convexPolygonsIndices.get(ii);
                if (otherIndices.size != 3) continue;
                short otherFirstIndex = otherIndices.get(0);
                short otherSecondIndex = otherIndices.get(1);
                short otherLastIndex = otherIndices.get(2);
                FloatArray otherPoly = convexPolygons.get(ii);
                float x3 = otherPoly.get(otherPoly.size - 2);
                float y3 = otherPoly.get(otherPoly.size - 1);
                if (otherFirstIndex != firstIndex || otherSecondIndex != lastIndex) continue;
                int winding1 = Triangulator.winding(prevPrevX, prevPrevY, prevX, prevY, x3, y3);
                int winding2 = Triangulator.winding(x3, y3, firstX, firstY, secondX, secondY);
                if (winding1 != winding || winding2 != winding) continue;
                otherPoly.clear();
                otherIndices.clear();
                polygon.add(x3);
                polygon.add(y3);
                polygonIndices.add((int)otherLastIndex);
                prevPrevX = prevX;
                prevPrevY = prevY;
                prevX = x3;
                prevY = y3;
                ii = 0;
            }
        }
        for (i = convexPolygons.size - 1; i >= 0; --i) {
            polygon = convexPolygons.get(i);
            if (polygon.size != 0) continue;
            convexPolygons.removeIndex(i);
            this.polygonPool.free(polygon);
            polygonIndices = convexPolygonsIndices.removeIndex(i);
            this.polygonIndicesPool.free(polygonIndices);
        }
        return convexPolygons;
    }

    private static boolean isConcave(int index, int vertexCount, float[] vertices, short[] indices) {
        int previous = indices[(vertexCount + index - 1) % vertexCount] << 1;
        int current = indices[index] << 1;
        int next = indices[(index + 1) % vertexCount] << 1;
        return !Triangulator.positiveArea(vertices[previous], vertices[previous + 1], vertices[current], vertices[current + 1], vertices[next], vertices[next + 1]);
    }

    private static boolean positiveArea(float p1x, float p1y, float p2x, float p2y, float p3x, float p3y) {
        return p1x * (p3y - p2y) + p2x * (p1y - p3y) + p3x * (p2y - p1y) >= 0.0f;
    }

    private static int winding(float p1x, float p1y, float p2x, float p2y, float p3x, float p3y) {
        float py = p2y - p1y;
        float px = p2x - p1x;
        return p3x * py - p3y * px + px * p1y - p1x * py >= 0.0f ? 1 : -1;
    }
}

