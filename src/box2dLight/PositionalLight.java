/*
 * Decompiled with CFR 0.152.
 */
package box2dLight;

import box2dLight.Light;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public abstract class PositionalLight
extends Light {
    protected final Vector2 tmpEnd = new Vector2();
    protected final Vector2 start = new Vector2();
    protected Body body;
    protected float bodyOffsetX;
    protected float bodyOffsetY;
    protected float bodyAngleOffset;
    protected float[] sin;
    protected float[] cos;
    protected float[] endX;
    protected float[] endY;

    public PositionalLight(RayHandler rayHandler, int rays, Color color, float distance, float x, float y, float directionDegree) {
        super(rayHandler, rays, color, distance, directionDegree);
        this.start.x = x;
        this.start.y = y;
        this.lightMesh = new Mesh(Mesh.VertexDataType.VertexArray, false, this.vertexNum, 0, new VertexAttribute(1, 2, "vertex_positions"), new VertexAttribute(4, 4, "quad_colors"), new VertexAttribute(32, 1, "s"));
        this.softShadowMesh = new Mesh(Mesh.VertexDataType.VertexArray, false, this.vertexNum * 2, 0, new VertexAttribute(1, 2, "vertex_positions"), new VertexAttribute(4, 4, "quad_colors"), new VertexAttribute(32, 1, "s"));
        this.setMesh();
    }

    @Override
    void update() {
        this.updateBody();
        if (this.cull()) {
            return;
        }
        if (this.staticLight && !this.dirty) {
            return;
        }
        this.dirty = false;
        this.updateMesh();
    }

    @Override
    void render() {
        if (this.rayHandler.culling && this.culled) {
            return;
        }
        ++this.rayHandler.lightRenderedLastFrame;
        this.lightMesh.render(this.rayHandler.lightShader, 6, 0, this.vertexNum);
        if (this.soft && !this.xray) {
            this.softShadowMesh.render(this.rayHandler.lightShader, 5, 0, (this.vertexNum - 1) * 2);
        }
    }

    @Override
    public void attachToBody(Body body) {
        this.attachToBody(body, 0.0f, 0.0f, 0.0f);
    }

    public void attachToBody(Body body, float offsetX, float offsetY) {
        this.attachToBody(body, offsetX, offsetY, 0.0f);
    }

    public void attachToBody(Body body, float offsetX, float offsetY, float degrees) {
        this.body = body;
        this.bodyOffsetX = offsetX;
        this.bodyOffsetY = offsetY;
        this.bodyAngleOffset = degrees;
        if (this.staticLight) {
            this.dirty = true;
        }
    }

    @Override
    public Vector2 getPosition() {
        this.tmpPosition.x = this.start.x;
        this.tmpPosition.y = this.start.y;
        return this.tmpPosition;
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    @Override
    public float getX() {
        return this.start.x;
    }

    @Override
    public float getY() {
        return this.start.y;
    }

    @Override
    public void setPosition(float x, float y) {
        this.start.x = x;
        this.start.y = y;
        if (this.staticLight) {
            this.dirty = true;
        }
    }

    @Override
    public void setPosition(Vector2 position) {
        this.start.x = position.x;
        this.start.y = position.y;
        if (this.staticLight) {
            this.dirty = true;
        }
    }

    @Override
    public boolean contains(float x, float y) {
        float x_d = this.start.x - x;
        float y_d = this.start.y - y;
        float dst2 = x_d * x_d + y_d * y_d;
        if (this.distance * this.distance <= dst2) {
            return false;
        }
        boolean oddNodes = false;
        float x2 = this.mx[this.rayNum] = this.start.x;
        float y2 = this.my[this.rayNum] = this.start.y;
        for (int i = 0; i <= this.rayNum; ++i) {
            float x1 = this.mx[i];
            float y1 = this.my[i];
            if ((y1 < y && y2 >= y || y1 >= y && y2 < y) && (y - y1) / (y2 - y1) * (x2 - x1) < x - x1) {
                oddNodes = !oddNodes;
            }
            x2 = x1;
            y2 = y1;
        }
        return oddNodes;
    }

    @Override
    protected void setRayNum(int rays) {
        super.setRayNum(rays);
        this.sin = new float[rays];
        this.cos = new float[rays];
        this.endX = new float[rays];
        this.endY = new float[rays];
    }

    protected boolean cull() {
        this.culled = this.rayHandler.culling && !this.rayHandler.intersect(this.start.x, this.start.y, this.distance + this.softShadowLength);
        return this.culled;
    }

    protected void updateBody() {
        if (this.body == null || this.staticLight) {
            return;
        }
        Vector2 vec = this.body.getPosition();
        float angle = this.body.getAngle();
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);
        float dX = this.bodyOffsetX * cos - this.bodyOffsetY * sin;
        float dY = this.bodyOffsetX * sin + this.bodyOffsetY * cos;
        this.start.x = vec.x + dX;
        this.start.y = vec.y + dY;
        this.setDirection(this.bodyAngleOffset + angle * 57.295776f);
    }

    protected void updateMesh() {
        for (int i = 0; i < this.rayNum; ++i) {
            this.m_index = i;
            this.f[i] = 1.0f;
            this.mx[i] = this.tmpEnd.x = this.endX[i] + this.start.x;
            this.my[i] = this.tmpEnd.y = this.endY[i] + this.start.y;
            if (this.rayHandler.world == null || this.xray) continue;
            this.rayHandler.world.rayCast(this.ray, this.start, this.tmpEnd);
        }
        this.setMesh();
    }

    protected void setMesh() {
        int i;
        int size = 0;
        this.segments[size++] = this.start.x;
        this.segments[size++] = this.start.y;
        this.segments[size++] = this.colorF;
        this.segments[size++] = 1.0f;
        for (i = 0; i < this.rayNum; ++i) {
            this.segments[size++] = this.mx[i];
            this.segments[size++] = this.my[i];
            this.segments[size++] = this.colorF;
            this.segments[size++] = 1.0f - this.f[i];
        }
        this.lightMesh.setVertices(this.segments, 0, size);
        if (!this.soft || this.xray) {
            return;
        }
        size = 0;
        for (i = 0; i < this.rayNum; ++i) {
            this.segments[size++] = this.mx[i];
            this.segments[size++] = this.my[i];
            this.segments[size++] = this.colorF;
            float s = 1.0f - this.f[i];
            this.segments[size++] = s;
            this.segments[size++] = this.mx[i] + s * this.softShadowLength * this.cos[i];
            this.segments[size++] = this.my[i] + s * this.softShadowLength * this.sin[i];
            this.segments[size++] = zeroColorBits;
            this.segments[size++] = 0.0f;
        }
        this.softShadowMesh.setVertices(this.segments, 0, size);
    }
}

