/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.NumberUtils;
import com.esotericsoftware.spine.utils.SpineUtils;

public class TwoColorPolygonBatch
implements PolygonBatch {
    static final int VERTEX_SIZE = 6;
    static final int SPRITE_SIZE = 24;
    private final Mesh mesh;
    private final float[] vertices;
    private final short[] triangles;
    private final Matrix4 transformMatrix = new Matrix4();
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 combinedMatrix = new Matrix4();
    private boolean blendingDisabled;
    private final ShaderProgram defaultShader;
    private ShaderProgram shader;
    private int vertexIndex;
    private int triangleIndex;
    private Texture lastTexture;
    private float invTexWidth = 0.0f;
    private float invTexHeight = 0.0f;
    private boolean drawing;
    private int blendSrcFunc = 770;
    private int blendDstFunc = 771;
    private int blendSrcFuncAlpha = 770;
    private int blendDstFuncAlpha = 771;
    private boolean premultipliedAlpha;
    private final Color light = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    private final Color dark = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    private float lightPacked = Color.WHITE.toFloatBits();
    private float darkPacked = Color.BLACK.toFloatBits();
    public int totalRenderCalls = 0;

    public TwoColorPolygonBatch() {
        this(2000);
    }

    public TwoColorPolygonBatch(int size) {
        this(size, size * 2);
    }

    public TwoColorPolygonBatch(int maxVertices, int maxTriangles) {
        if (maxVertices > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Can't have more than 32767 vertices per batch: " + maxTriangles);
        }
        Mesh.VertexDataType vertexDataType = Mesh.VertexDataType.VertexArray;
        if (Gdx.gl30 != null) {
            vertexDataType = Mesh.VertexDataType.VertexBufferObjectWithVAO;
        }
        this.mesh = new Mesh(vertexDataType, false, maxVertices, maxTriangles * 3, new VertexAttribute(1, 2, "a_position"), new VertexAttribute(4, 4, "a_light"), new VertexAttribute(4, 4, "a_dark"), new VertexAttribute(16, 2, "a_texCoord0"));
        this.vertices = new float[maxVertices * 6];
        this.triangles = new short[maxTriangles * 3];
        this.shader = this.defaultShader = this.createDefaultShader();
        this.projectionMatrix.setToOrtho2D(0.0f, 0.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void begin() {
        if (this.drawing) {
            throw new IllegalStateException("end must be called before begin.");
        }
        Gdx.gl.glDepthMask(false);
        this.shader.begin();
        this.setupMatrices();
        this.drawing = true;
    }

    @Override
    public void end() {
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before end.");
        }
        if (this.vertexIndex > 0) {
            this.flush();
        }
        this.shader.end();
        Gdx.gl.glDepthMask(true);
        if (this.isBlendingEnabled()) {
            Gdx.gl.glDisable(3042);
        }
        this.lastTexture = null;
        this.drawing = false;
    }

    @Override
    public void setColor(Color tint) {
        this.light.set(tint);
        this.lightPacked = tint.toFloatBits();
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        this.light.set(r, g, b, a);
        this.lightPacked = this.light.toFloatBits();
    }

    @Override
    public void setPackedColor(float packedColor) {
        Color.rgba8888ToColor(this.light, NumberUtils.floatToIntColor(packedColor));
        this.lightPacked = packedColor;
    }

    @Override
    public Color getColor() {
        return this.light;
    }

    @Override
    public float getPackedColor() {
        return this.lightPacked;
    }

    public void setDarkColor(Color tint) {
        this.dark.set(tint);
        this.darkPacked = tint.toFloatBits();
    }

    public void setDarkColor(float r, float g, float b, float a) {
        this.dark.set(r, g, b, a);
        this.darkPacked = this.dark.toFloatBits();
    }

    public void setPackedDarkColor(float packedColor) {
        Color.rgba8888ToColor(this.dark, NumberUtils.floatToIntColor(packedColor));
        this.darkPacked = packedColor;
    }

    public Color getDarkColor() {
        return this.dark;
    }

    public float getPackedDarkColor() {
        return this.darkPacked;
    }

    @Override
    public void draw(PolygonRegion region, float x, float y) {
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        short[] regionTriangles = region.getTriangles();
        int regionTrianglesLength = regionTriangles.length;
        float[] regionVertices = region.getVertices();
        int regionVerticesLength = regionVertices.length;
        Texture texture = region.getRegion().getTexture();
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + regionTrianglesLength > triangles.length || this.vertexIndex + regionVerticesLength * 6 / 2 > this.vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int vertexIndex = this.vertexIndex;
        int startVertex = vertexIndex / 6;
        for (int i = 0; i < regionTrianglesLength; ++i) {
            triangles[triangleIndex++] = (short)(regionTriangles[i] + startVertex);
        }
        this.triangleIndex = triangleIndex;
        float[] vertices = this.vertices;
        float light = this.lightPacked;
        float dark = this.darkPacked;
        float[] textureCoords = region.getTextureCoords();
        for (int i = 0; i < regionVerticesLength; i += 2) {
            vertices[vertexIndex++] = regionVertices[i] + x;
            vertices[vertexIndex++] = regionVertices[i + 1] + y;
            vertices[vertexIndex++] = light;
            vertices[vertexIndex++] = dark;
            vertices[vertexIndex++] = textureCoords[i];
            vertices[vertexIndex++] = textureCoords[i + 1];
        }
        this.vertexIndex = vertexIndex;
    }

    @Override
    public void draw(PolygonRegion region, float x, float y, float width, float height) {
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        short[] regionTriangles = region.getTriangles();
        int regionTrianglesLength = regionTriangles.length;
        float[] regionVertices = region.getVertices();
        int regionVerticesLength = regionVertices.length;
        TextureRegion textureRegion = region.getRegion();
        Texture texture = textureRegion.getTexture();
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + regionTrianglesLength > triangles.length || this.vertexIndex + regionVerticesLength * 6 / 2 > this.vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int vertexIndex = this.vertexIndex;
        int startVertex = vertexIndex / 6;
        int n = regionTriangles.length;
        for (int i = 0; i < n; ++i) {
            triangles[triangleIndex++] = (short)(regionTriangles[i] + startVertex);
        }
        this.triangleIndex = triangleIndex;
        float[] vertices = this.vertices;
        float light = this.lightPacked;
        float dark = this.darkPacked;
        float[] textureCoords = region.getTextureCoords();
        float sX = width / (float)textureRegion.getRegionWidth();
        float sY = height / (float)textureRegion.getRegionHeight();
        for (int i = 0; i < regionVerticesLength; i += 2) {
            vertices[vertexIndex++] = regionVertices[i] * sX + x;
            vertices[vertexIndex++] = regionVertices[i + 1] * sY + y;
            vertices[vertexIndex++] = light;
            vertices[vertexIndex++] = dark;
            vertices[vertexIndex++] = textureCoords[i];
            vertices[vertexIndex++] = textureCoords[i + 1];
        }
        this.vertexIndex = vertexIndex;
    }

    @Override
    public void draw(PolygonRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        short[] regionTriangles = region.getTriangles();
        int regionTrianglesLength = regionTriangles.length;
        float[] regionVertices = region.getVertices();
        int regionVerticesLength = regionVertices.length;
        TextureRegion textureRegion = region.getRegion();
        Texture texture = textureRegion.getTexture();
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + regionTrianglesLength > triangles.length || this.vertexIndex + regionVerticesLength * 6 / 2 > this.vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int vertexIndex = this.vertexIndex;
        int startVertex = vertexIndex / 6;
        for (int i = 0; i < regionTrianglesLength; ++i) {
            triangles[triangleIndex++] = (short)(regionTriangles[i] + startVertex);
        }
        this.triangleIndex = triangleIndex;
        float[] vertices = this.vertices;
        float light = this.lightPacked;
        float dark = this.darkPacked;
        float[] textureCoords = region.getTextureCoords();
        float worldOriginX = x + originX;
        float worldOriginY = y + originY;
        float sX = width / (float)textureRegion.getRegionWidth();
        float sY = height / (float)textureRegion.getRegionHeight();
        float cos = MathUtils.cosDeg(rotation);
        float sin = MathUtils.sinDeg(rotation);
        for (int i = 0; i < regionVerticesLength; i += 2) {
            float fx = (regionVertices[i] * sX - originX) * scaleX;
            float fy = (regionVertices[i + 1] * sY - originY) * scaleY;
            vertices[vertexIndex++] = cos * fx - sin * fy + worldOriginX;
            vertices[vertexIndex++] = sin * fx + cos * fy + worldOriginY;
            vertices[vertexIndex++] = light;
            vertices[vertexIndex++] = dark;
            vertices[vertexIndex++] = textureCoords[i];
            vertices[vertexIndex++] = textureCoords[i + 1];
        }
        this.vertexIndex = vertexIndex;
    }

    @Override
    public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        float tmp;
        float y4;
        float x4;
        float y3;
        float x3;
        float y2;
        float x2;
        float y1;
        float x1;
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 24 > vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int startVertex = this.vertexIndex / 6;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;
        float worldOriginX = x + originX;
        float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;
        if (scaleX != 1.0f || scaleY != 1.0f) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }
        float p1x = fx;
        float p1y = fy;
        float p2x = fx;
        float p2y = fy2;
        float p3x = fx2;
        float p3y = fy2;
        float p4x = fx2;
        float p4y = fy;
        if (rotation != 0.0f) {
            float cos = MathUtils.cosDeg(rotation);
            float sin = MathUtils.sinDeg(rotation);
            x1 = cos * p1x - sin * p1y;
            y1 = sin * p1x + cos * p1y;
            x2 = cos * p2x - sin * p2y;
            y2 = sin * p2x + cos * p2y;
            x3 = cos * p3x - sin * p3y;
            y3 = sin * p3x + cos * p3y;
            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = p1x;
            y1 = p1y;
            x2 = p2x;
            y2 = p2y;
            x3 = p3x;
            y3 = p3y;
            x4 = p4x;
            y4 = p4y;
        }
        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;
        float u = (float)srcX * this.invTexWidth;
        float v = (float)(srcY + srcHeight) * this.invTexHeight;
        float u2 = (float)(srcX + srcWidth) * this.invTexWidth;
        float v2 = (float)srcY * this.invTexHeight;
        if (flipX) {
            tmp = u;
            u = u2;
            u2 = tmp;
        }
        if (flipY) {
            tmp = v;
            v = v2;
            v2 = tmp;
        }
        float light = this.lightPacked;
        float dark = this.darkPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = x2;
        vertices[idx++] = y2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = x3;
        vertices[idx++] = y3;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = x4;
        vertices[idx++] = y4;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        this.vertexIndex = idx;
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        float tmp;
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 24 > vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int startVertex = this.vertexIndex / 6;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;
        float u = (float)srcX * this.invTexWidth;
        float v = (float)(srcY + srcHeight) * this.invTexHeight;
        float u2 = (float)(srcX + srcWidth) * this.invTexWidth;
        float v2 = (float)srcY * this.invTexHeight;
        float fx2 = x + width;
        float fy2 = y + height;
        if (flipX) {
            tmp = u;
            u = u2;
            u2 = tmp;
        }
        if (flipY) {
            tmp = v;
            v = v2;
            v2 = tmp;
        }
        float light = this.lightPacked;
        float dark = this.darkPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        this.vertexIndex = idx;
    }

    @Override
    public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 24 > vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int startVertex = this.vertexIndex / 6;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;
        float u = (float)srcX * this.invTexWidth;
        float v = (float)(srcY + srcHeight) * this.invTexHeight;
        float u2 = (float)(srcX + srcWidth) * this.invTexWidth;
        float v2 = (float)srcY * this.invTexHeight;
        float fx2 = x + (float)srcWidth;
        float fy2 = y + (float)srcHeight;
        float light = this.lightPacked;
        float dark = this.darkPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        this.vertexIndex = idx;
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 24 > vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int startVertex = this.vertexIndex / 6;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;
        float fx2 = x + width;
        float fy2 = y + height;
        float light = this.lightPacked;
        float dark = this.darkPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        this.vertexIndex = idx;
    }

    @Override
    public void draw(Texture texture, float x, float y) {
        this.draw(texture, x, y, (float)texture.getWidth(), (float)texture.getHeight());
    }

    @Override
    public void draw(Texture texture, float x, float y, float width, float height) {
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 24 > vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int startVertex = this.vertexIndex / 6;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;
        float fx2 = x + width;
        float fy2 = y + height;
        float u = 0.0f;
        float v = 1.0f;
        float u2 = 1.0f;
        float v2 = 0.0f;
        float light = this.lightPacked;
        float dark = this.darkPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = 0.0f;
        vertices[idx++] = 1.0f;
        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = 0.0f;
        vertices[idx++] = 0.0f;
        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = 1.0f;
        vertices[idx++] = 0.0f;
        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = 1.0f;
        vertices[idx++] = 1.0f;
        this.vertexIndex = idx;
    }

    public void drawTwoColor(Texture texture, float[] polygonVertices, int verticesOffset, int verticesCount, short[] polygonTriangles, int trianglesOffset, int trianglesCount) {
        int i;
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + trianglesCount > triangles.length || this.vertexIndex + verticesCount > vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int vertexIndex = this.vertexIndex;
        int startVertex = vertexIndex / 6;
        int n = i + trianglesCount;
        for (i = trianglesOffset; i < n; ++i) {
            triangles[triangleIndex++] = (short)(polygonTriangles[i] + startVertex);
        }
        this.triangleIndex = triangleIndex;
        SpineUtils.arraycopy(polygonVertices, verticesOffset, vertices, vertexIndex, verticesCount);
        this.vertexIndex += verticesCount;
    }

    @Override
    public void draw(Texture texture, float[] polygonVertices, int verticesOffset, int verticesCount, short[] polygonTriangles, int trianglesOffset, int trianglesCount) {
        int i;
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + trianglesCount > triangles.length || this.vertexIndex + verticesCount / 5 * 6 > vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int vertexIndex = this.vertexIndex;
        int startVertex = vertexIndex / 6;
        int n = i + trianglesCount;
        for (i = trianglesOffset; i < n; ++i) {
            triangles[triangleIndex++] = (short)(polygonTriangles[i] + startVertex);
        }
        this.triangleIndex = triangleIndex;
        int idx = this.vertexIndex;
        int n2 = verticesOffset + verticesCount;
        for (int i2 = verticesOffset; i2 < n2; i2 += 5) {
            vertices[idx++] = polygonVertices[i2];
            vertices[idx++] = polygonVertices[i2 + 1];
            vertices[idx++] = polygonVertices[i2 + 2];
            vertices[idx++] = 0.0f;
            vertices[idx++] = polygonVertices[i2 + 3];
            vertices[idx++] = polygonVertices[i2 + 4];
        }
        this.vertexIndex = idx;
    }

    public void drawTwoColor(Texture texture, float[] spriteVertices, int offset, int count) {
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        int triangleCount = count / 24 * 6;
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + triangleCount > triangles.length || this.vertexIndex + count > vertices.length) {
            this.flush();
        }
        int vertexIndex = this.vertexIndex;
        int triangleIndex = this.triangleIndex;
        short vertex = (short)(vertexIndex / 6);
        int n = triangleIndex + triangleCount;
        while (triangleIndex < n) {
            triangles[triangleIndex] = vertex;
            triangles[triangleIndex + 1] = (short)(vertex + 1);
            triangles[triangleIndex + 2] = (short)(vertex + 2);
            triangles[triangleIndex + 3] = (short)(vertex + 2);
            triangles[triangleIndex + 4] = (short)(vertex + 3);
            triangles[triangleIndex + 5] = vertex;
            triangleIndex += 6;
            vertex = (short)(vertex + 4);
        }
        this.triangleIndex = triangleIndex;
        SpineUtils.arraycopy(spriteVertices, offset, vertices, vertexIndex, count);
        this.vertexIndex += count;
    }

    @Override
    public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        int triangleCount = count / 20 * 6;
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + triangleCount > triangles.length || this.vertexIndex + count / 5 * 6 > vertices.length) {
            this.flush();
        }
        int vertexIndex = this.vertexIndex;
        int triangleIndex = this.triangleIndex;
        short vertex = (short)(vertexIndex / 6);
        int n = triangleIndex + triangleCount;
        while (triangleIndex < n) {
            triangles[triangleIndex] = vertex;
            triangles[triangleIndex + 1] = (short)(vertex + 1);
            triangles[triangleIndex + 2] = (short)(vertex + 2);
            triangles[triangleIndex + 3] = (short)(vertex + 2);
            triangles[triangleIndex + 4] = (short)(vertex + 3);
            triangles[triangleIndex + 5] = vertex;
            triangleIndex += 6;
            vertex = (short)(vertex + 4);
        }
        this.triangleIndex = triangleIndex;
        int idx = this.vertexIndex;
        int n2 = offset + count;
        for (int i = offset; i < n2; i += 5) {
            vertices[idx++] = spriteVertices[i];
            vertices[idx++] = spriteVertices[i + 1];
            vertices[idx++] = spriteVertices[i + 2];
            vertices[idx++] = 0.0f;
            vertices[idx++] = spriteVertices[i + 3];
            vertices[idx++] = spriteVertices[i + 4];
        }
        this.vertexIndex = idx;
    }

    @Override
    public void draw(TextureRegion region, float x, float y) {
        this.draw(region, x, y, (float)region.getRegionWidth(), (float)region.getRegionHeight());
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float width, float height) {
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        Texture texture = region.getTexture();
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 24 > vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int startVertex = this.vertexIndex / 6;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;
        float fx2 = x + width;
        float fy2 = y + height;
        float u = region.getU();
        float v = region.getV2();
        float u2 = region.getU2();
        float v2 = region.getV();
        float light = this.lightPacked;
        float dark = this.darkPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        this.vertexIndex = idx;
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
        float y4;
        float x4;
        float y3;
        float x3;
        float y2;
        float x2;
        float y1;
        float x1;
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        Texture texture = region.getTexture();
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 24 > vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int startVertex = this.vertexIndex / 6;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;
        float worldOriginX = x + originX;
        float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;
        if (scaleX != 1.0f || scaleY != 1.0f) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }
        float p1x = fx;
        float p1y = fy;
        float p2x = fx;
        float p2y = fy2;
        float p3x = fx2;
        float p3y = fy2;
        float p4x = fx2;
        float p4y = fy;
        if (rotation != 0.0f) {
            float cos = MathUtils.cosDeg(rotation);
            float sin = MathUtils.sinDeg(rotation);
            x1 = cos * p1x - sin * p1y;
            y1 = sin * p1x + cos * p1y;
            x2 = cos * p2x - sin * p2y;
            y2 = sin * p2x + cos * p2y;
            x3 = cos * p3x - sin * p3y;
            y3 = sin * p3x + cos * p3y;
            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = p1x;
            y1 = p1y;
            x2 = p2x;
            y2 = p2y;
            x3 = p3x;
            y3 = p3y;
            x4 = p4x;
            y4 = p4y;
        }
        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;
        float u = region.getU();
        float v = region.getV2();
        float u2 = region.getU2();
        float v2 = region.getV();
        float light = this.lightPacked;
        float dark = this.darkPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = x2;
        vertices[idx++] = y2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = x3;
        vertices[idx++] = y3;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = x4;
        vertices[idx++] = y4;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        this.vertexIndex = idx;
    }

    @Override
    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, boolean clockwise) {
        float v4;
        float u4;
        float v3;
        float u3;
        float v2;
        float u2;
        float v1;
        float u1;
        float y4;
        float x4;
        float y3;
        float x3;
        float y2;
        float x2;
        float y1;
        float x1;
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        Texture texture = region.getTexture();
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 24 > vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int startVertex = this.vertexIndex / 6;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;
        float worldOriginX = x + originX;
        float worldOriginY = y + originY;
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;
        if (scaleX != 1.0f || scaleY != 1.0f) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }
        float p1x = fx;
        float p1y = fy;
        float p2x = fx;
        float p2y = fy2;
        float p3x = fx2;
        float p3y = fy2;
        float p4x = fx2;
        float p4y = fy;
        if (rotation != 0.0f) {
            float cos = MathUtils.cosDeg(rotation);
            float sin = MathUtils.sinDeg(rotation);
            x1 = cos * p1x - sin * p1y;
            y1 = sin * p1x + cos * p1y;
            x2 = cos * p2x - sin * p2y;
            y2 = sin * p2x + cos * p2y;
            x3 = cos * p3x - sin * p3y;
            y3 = sin * p3x + cos * p3y;
            x4 = x1 + (x3 - x2);
            y4 = y3 - (y2 - y1);
        } else {
            x1 = p1x;
            y1 = p1y;
            x2 = p2x;
            y2 = p2y;
            x3 = p3x;
            y3 = p3y;
            x4 = p4x;
            y4 = p4y;
        }
        x1 += worldOriginX;
        y1 += worldOriginY;
        x2 += worldOriginX;
        y2 += worldOriginY;
        x3 += worldOriginX;
        y3 += worldOriginY;
        x4 += worldOriginX;
        y4 += worldOriginY;
        if (clockwise) {
            u1 = region.getU2();
            v1 = region.getV2();
            u2 = region.getU();
            v2 = region.getV2();
            u3 = region.getU();
            v3 = region.getV();
            u4 = region.getU2();
            v4 = region.getV();
        } else {
            u1 = region.getU();
            v1 = region.getV();
            u2 = region.getU2();
            v2 = region.getV();
            u3 = region.getU2();
            v3 = region.getV2();
            u4 = region.getU();
            v4 = region.getV2();
        }
        float light = this.lightPacked;
        float dark = this.darkPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u1;
        vertices[idx++] = v1;
        vertices[idx++] = x2;
        vertices[idx++] = y2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = x3;
        vertices[idx++] = y3;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u3;
        vertices[idx++] = v3;
        vertices[idx++] = x4;
        vertices[idx++] = y4;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u4;
        vertices[idx++] = v4;
        this.vertexIndex = idx;
    }

    @Override
    public void draw(TextureRegion region, float width, float height, Affine2 transform) {
        if (!this.drawing) {
            throw new IllegalStateException("begin must be called before draw.");
        }
        short[] triangles = this.triangles;
        float[] vertices = this.vertices;
        Texture texture = region.getTexture();
        if (texture != this.lastTexture) {
            this.switchTexture(texture);
        } else if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 24 > vertices.length) {
            this.flush();
        }
        int triangleIndex = this.triangleIndex;
        int startVertex = this.vertexIndex / 6;
        triangles[triangleIndex++] = (short)startVertex;
        triangles[triangleIndex++] = (short)(startVertex + 1);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 2);
        triangles[triangleIndex++] = (short)(startVertex + 3);
        triangles[triangleIndex++] = (short)startVertex;
        this.triangleIndex = triangleIndex;
        float x1 = transform.m02;
        float y1 = transform.m12;
        float x2 = transform.m01 * height + transform.m02;
        float y2 = transform.m11 * height + transform.m12;
        float x3 = transform.m00 * width + transform.m01 * height + transform.m02;
        float y3 = transform.m10 * width + transform.m11 * height + transform.m12;
        float x4 = transform.m00 * width + transform.m02;
        float y4 = transform.m10 * width + transform.m12;
        float u = region.getU();
        float v = region.getV2();
        float u2 = region.getU2();
        float v2 = region.getV();
        float light = this.lightPacked;
        float dark = this.darkPacked;
        int idx = this.vertexIndex;
        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v;
        vertices[idx++] = x2;
        vertices[idx++] = y2;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u;
        vertices[idx++] = v2;
        vertices[idx++] = x3;
        vertices[idx++] = y3;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v2;
        vertices[idx++] = x4;
        vertices[idx++] = y4;
        vertices[idx++] = light;
        vertices[idx++] = dark;
        vertices[idx++] = u2;
        vertices[idx++] = v;
        this.vertexIndex = idx;
    }

    @Override
    public void flush() {
        if (this.vertexIndex == 0) {
            return;
        }
        ++this.totalRenderCalls;
        this.lastTexture.bind();
        Mesh mesh = this.mesh;
        mesh.setVertices(this.vertices, 0, this.vertexIndex);
        mesh.setIndices(this.triangles, 0, this.triangleIndex);
        Gdx.gl.glEnable(3042);
        if (this.blendSrcFunc != -1) {
            Gdx.gl.glBlendFuncSeparate(this.blendSrcFunc, this.blendDstFunc, this.blendSrcFuncAlpha, this.blendDstFuncAlpha);
        }
        mesh.render(this.shader, 4, 0, this.triangleIndex);
        this.vertexIndex = 0;
        this.triangleIndex = 0;
    }

    @Override
    public void disableBlending() {
        this.flush();
        this.blendingDisabled = true;
    }

    @Override
    public void enableBlending() {
        this.flush();
        this.blendingDisabled = false;
    }

    @Override
    public void dispose() {
        this.mesh.dispose();
        this.shader.dispose();
    }

    @Override
    public Matrix4 getProjectionMatrix() {
        return this.projectionMatrix;
    }

    @Override
    public Matrix4 getTransformMatrix() {
        return this.transformMatrix;
    }

    @Override
    public void setProjectionMatrix(Matrix4 projection) {
        if (this.drawing) {
            this.flush();
        }
        this.projectionMatrix.set(projection);
        if (this.drawing) {
            this.setupMatrices();
        }
    }

    @Override
    public void setTransformMatrix(Matrix4 transform) {
        if (this.drawing) {
            this.flush();
        }
        this.transformMatrix.set(transform);
        if (this.drawing) {
            this.setupMatrices();
        }
    }

    public void setPremultipliedAlpha(boolean premultipliedAlpha) {
        if (this.premultipliedAlpha == premultipliedAlpha) {
            return;
        }
        if (this.drawing) {
            this.flush();
        }
        this.premultipliedAlpha = premultipliedAlpha;
        if (this.drawing) {
            this.setupMatrices();
        }
    }

    private void setupMatrices() {
        this.combinedMatrix.set(this.projectionMatrix).mul(this.transformMatrix);
        this.shader.setUniformf("u_pma", this.premultipliedAlpha ? 1.0f : 0.0f);
        this.shader.setUniformMatrix("u_projTrans", this.combinedMatrix);
        this.shader.setUniformi("u_texture", 0);
    }

    private void switchTexture(Texture texture) {
        this.flush();
        this.lastTexture = texture;
        this.invTexWidth = 1.0f / (float)texture.getWidth();
        this.invTexHeight = 1.0f / (float)texture.getHeight();
    }

    @Override
    public void setShader(ShaderProgram newShader) {
        if (this.shader == newShader) {
            return;
        }
        if (this.drawing) {
            this.flush();
            this.shader.end();
        }
        ShaderProgram shaderProgram = this.shader = newShader == null ? this.defaultShader : newShader;
        if (this.drawing) {
            this.shader.begin();
            this.setupMatrices();
        }
    }

    @Override
    public ShaderProgram getShader() {
        return this.shader;
    }

    @Override
    public boolean isBlendingEnabled() {
        return !this.blendingDisabled;
    }

    @Override
    public boolean isDrawing() {
        return this.drawing;
    }

    @Override
    public void setBlendFunction(int srcFunc, int dstFunc) {
        this.setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
    }

    @Override
    public void setBlendFunctionSeparate(int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
        if (this.blendSrcFunc == srcFuncColor && this.blendDstFunc == dstFuncColor && this.blendSrcFuncAlpha == srcFuncAlpha && this.blendDstFuncAlpha == dstFuncAlpha) {
            return;
        }
        this.flush();
        this.blendSrcFunc = srcFuncColor;
        this.blendDstFunc = dstFuncColor;
        this.blendSrcFuncAlpha = srcFuncAlpha;
        this.blendDstFuncAlpha = dstFuncAlpha;
    }

    @Override
    public int getBlendSrcFunc() {
        return this.blendSrcFunc;
    }

    @Override
    public int getBlendDstFunc() {
        return this.blendDstFunc;
    }

    @Override
    public int getBlendSrcFuncAlpha() {
        return this.blendSrcFuncAlpha;
    }

    @Override
    public int getBlendDstFuncAlpha() {
        return this.blendDstFuncAlpha;
    }

    private ShaderProgram createDefaultShader() {
        String vertexShader = "attribute vec4 a_position;\nattribute vec4 a_light;\nattribute vec4 a_dark;\nattribute vec2 a_texCoord0;\nuniform mat4 u_projTrans;\nvarying vec4 v_light;\nvarying vec4 v_dark;\nvarying vec2 v_texCoords;\n\nvoid main()\n{\n   v_light = a_light;\n   v_light.a = v_light.a * (255.0/254.0);\n   v_dark = a_dark;\n   v_texCoords = a_texCoord0;\n   gl_Position =  u_projTrans * a_position;\n}\n";
        String fragmentShader = "#ifdef GL_ES\n#define LOWP lowp\nprecision mediump float;\n#else\n#define LOWP \n#endif\nvarying LOWP vec4 v_light;\nvarying LOWP vec4 v_dark;\nuniform float u_pma;\nvarying vec2 v_texCoords;\nuniform sampler2D u_texture;\nvoid main()\n{\n  vec4 texColor = texture2D(u_texture, v_texCoords);\n  gl_FragColor.a = texColor.a * v_light.a;\n  gl_FragColor.rgb = ((texColor.a - 1.0) * u_pma + 1.0 - texColor.rgb) * v_dark.rgb + texColor.rgb * v_light.rgb;\n}";
        ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) {
            throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        }
        return shader;
    }
}

