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

public class DirectionalLight
extends Light {
    protected final Vector2[] start;
    protected final Vector2[] end;
    protected float sin;
    protected float cos;
    protected Body body;

    public DirectionalLight(RayHandler rayHandler, int rays, Color color, float directionDegree) {
        super(rayHandler, rays, color, Float.POSITIVE_INFINITY, directionDegree);
        this.vertexNum = (this.vertexNum - 1) * 2;
        this.start = new Vector2[this.rayNum];
        this.end = new Vector2[this.rayNum];
        for (int i = 0; i < this.rayNum; ++i) {
            this.start[i] = new Vector2();
            this.end[i] = new Vector2();
        }
        this.lightMesh = new Mesh(Mesh.VertexDataType.VertexArray, this.staticLight, this.vertexNum, 0, new VertexAttribute(1, 2, "vertex_positions"), new VertexAttribute(4, 4, "quad_colors"), new VertexAttribute(32, 1, "s"));
        this.softShadowMesh = new Mesh(Mesh.VertexDataType.VertexArray, this.staticLight, this.vertexNum, 0, new VertexAttribute(1, 2, "vertex_positions"), new VertexAttribute(4, 4, "quad_colors"), new VertexAttribute(32, 1, "s"));
        this.update();
    }

    @Override
    public void setDirection(float direction) {
        this.direction = direction;
        this.sin = MathUtils.sinDeg(direction);
        this.cos = MathUtils.cosDeg(direction);
        if (this.staticLight) {
            this.dirty = true;
        }
    }

    @Override
    void update() {
        int i;
        if (this.staticLight && !this.dirty) {
            return;
        }
        this.dirty = false;
        float width = this.rayHandler.x2 - this.rayHandler.x1;
        float height = this.rayHandler.y2 - this.rayHandler.y1;
        float sizeOfScreen = width > height ? width : height;
        float xAxelOffSet = sizeOfScreen * this.cos;
        float yAxelOffSet = sizeOfScreen * this.sin;
        if (xAxelOffSet * xAxelOffSet < 0.1f && yAxelOffSet * yAxelOffSet < 0.1f) {
            xAxelOffSet = 1.0f;
            yAxelOffSet = 1.0f;
        }
        float widthOffSet = sizeOfScreen * -this.sin;
        float heightOffSet = sizeOfScreen * this.cos;
        float x = (this.rayHandler.x1 + this.rayHandler.x2) * 0.5f - widthOffSet;
        float y = (this.rayHandler.y1 + this.rayHandler.y2) * 0.5f - heightOffSet;
        float portionX = 2.0f * widthOffSet / (float)(this.rayNum - 1);
        x = (float)MathUtils.floor(x / (portionX * 2.0f)) * portionX * 2.0f;
        float portionY = 2.0f * heightOffSet / (float)(this.rayNum - 1);
        y = (float)MathUtils.ceil(y / (portionY * 2.0f)) * portionY * 2.0f;
        for (int i2 = 0; i2 < this.rayNum; ++i2) {
            float steppedX = (float)i2 * portionX + x;
            float steppedY = (float)i2 * portionY + y;
            this.m_index = i2;
            this.start[i2].x = steppedX - xAxelOffSet;
            this.start[i2].y = steppedY - yAxelOffSet;
            this.mx[i2] = this.end[i2].x = steppedX + xAxelOffSet;
            this.my[i2] = this.end[i2].y = steppedY + yAxelOffSet;
            if (this.rayHandler.world == null || this.xray) continue;
            this.rayHandler.world.rayCast(this.ray, this.start[i2], this.end[i2]);
        }
        int size = 0;
        int arraySize = this.rayNum;
        for (i = 0; i < arraySize; ++i) {
            this.segments[size++] = this.start[i].x;
            this.segments[size++] = this.start[i].y;
            this.segments[size++] = this.colorF;
            this.segments[size++] = 1.0f;
            this.segments[size++] = this.mx[i];
            this.segments[size++] = this.my[i];
            this.segments[size++] = this.colorF;
            this.segments[size++] = 1.0f;
        }
        this.lightMesh.setVertices(this.segments, 0, size);
        if (!this.soft || this.xray) {
            return;
        }
        size = 0;
        for (i = 0; i < arraySize; ++i) {
            this.segments[size++] = this.mx[i];
            this.segments[size++] = this.my[i];
            this.segments[size++] = this.colorF;
            this.segments[size++] = 1.0f;
            this.segments[size++] = this.mx[i] + this.softShadowLength * this.cos;
            this.segments[size++] = this.my[i] + this.softShadowLength * this.sin;
            this.segments[size++] = zeroColorBits;
            this.segments[size++] = 1.0f;
        }
        this.softShadowMesh.setVertices(this.segments, 0, size);
    }

    @Override
    void render() {
        ++this.rayHandler.lightRenderedLastFrame;
        this.lightMesh.render(this.rayHandler.lightShader, 5, 0, this.vertexNum);
        if (this.soft && !this.xray) {
            this.softShadowMesh.render(this.rayHandler.lightShader, 5, 0, this.vertexNum);
        }
    }

    @Override
    public boolean contains(float x, float y) {
        float y1;
        float x1;
        int i;
        boolean oddNodes = false;
        float x2 = this.mx[this.rayNum] = this.start[0].x;
        float y2 = this.my[this.rayNum] = this.start[0].y;
        for (i = 0; i <= this.rayNum; ++i) {
            x1 = this.mx[i];
            y1 = this.my[i];
            if ((y1 < y && y2 >= y || y1 >= y && y2 < y) && (y - y1) / (y2 - y1) * (x2 - x1) < x - x1) {
                oddNodes = !oddNodes;
            }
            x2 = x1;
            y2 = y1;
        }
        for (i = 0; i < this.rayNum; ++i) {
            x1 = this.start[i].x;
            y1 = this.start[i].y;
            if ((y1 < y && y2 >= y || y1 >= y && y2 < y) && (y - y1) / (y2 - y1) * (x2 - x1) < x - x1) {
                oddNodes = !oddNodes;
            }
            x2 = x1;
            y2 = y1;
        }
        return oddNodes;
    }

    @Override
    @Deprecated
    public void attachToBody(Body body) {
    }

    @Override
    @Deprecated
    public void setPosition(float x, float y) {
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    @Override
    @Deprecated
    public float getX() {
        return 0.0f;
    }

    @Override
    @Deprecated
    public float getY() {
        return 0.0f;
    }

    @Override
    @Deprecated
    public void setPosition(Vector2 position) {
    }

    @Override
    @Deprecated
    public void setDistance(float dist) {
    }

    @Override
    @Deprecated
    public void setIgnoreAttachedBody(boolean flag) {
    }

    @Override
    @Deprecated
    public boolean getIgnoreAttachedBody() {
        return false;
    }

    public void setIgnoreBody(Body body) {
        this.body = body;
        this.ignoreBody = body != null;
    }
}

