/*
 * Decompiled with CFR 0.152.
 */
package box2dLight;

import box2dLight.Light;
import box2dLight.RayHandler;
import box2dLight.Spinor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pools;

public class ChainLight
extends Light {
    public static float defaultRayStartOffset = 0.001f;
    public float rayStartOffset;
    public final FloatArray chain;
    protected int rayDirection;
    protected float bodyAngle;
    protected float bodyAngleOffset;
    protected Body body;
    protected final FloatArray segmentAngles = new FloatArray();
    protected final FloatArray segmentLengths = new FloatArray();
    protected final float[] startX;
    protected final float[] startY;
    protected final float[] endX;
    protected final float[] endY;
    protected final Vector2 bodyPosition = new Vector2();
    protected final Vector2 tmpEnd = new Vector2();
    protected final Vector2 tmpStart = new Vector2();
    protected final Vector2 tmpPerp = new Vector2();
    protected final Vector2 tmpVec = new Vector2();
    protected final Matrix3 zeroPosition = new Matrix3();
    protected final Matrix3 rotateAroundZero = new Matrix3();
    protected final Matrix3 restorePosition = new Matrix3();
    protected final Rectangle chainLightBounds = new Rectangle();
    protected final Rectangle rayHandlerBounds = new Rectangle();

    public ChainLight(RayHandler rayHandler, int rays, Color color, float distance, int rayDirection) {
        this(rayHandler, rays, color, distance, rayDirection, null);
    }

    public ChainLight(RayHandler rayHandler, int rays, Color color, float distance, int rayDirection, float[] chain) {
        super(rayHandler, rays, color, distance, 0.0f);
        this.rayStartOffset = defaultRayStartOffset;
        this.rayDirection = rayDirection;
        this.vertexNum = (this.vertexNum - 1) * 2;
        this.endX = new float[rays];
        this.endY = new float[rays];
        this.startX = new float[rays];
        this.startY = new float[rays];
        this.chain = chain != null ? new FloatArray(chain) : new FloatArray();
        this.lightMesh = new Mesh(Mesh.VertexDataType.VertexArray, false, this.vertexNum, 0, new VertexAttribute(1, 2, "vertex_positions"), new VertexAttribute(4, 4, "quad_colors"), new VertexAttribute(32, 1, "s"));
        this.softShadowMesh = new Mesh(Mesh.VertexDataType.VertexArray, false, this.vertexNum * 2, 0, new VertexAttribute(1, 2, "vertex_positions"), new VertexAttribute(4, 4, "quad_colors"), new VertexAttribute(32, 1, "s"));
        this.setMesh();
    }

    @Override
    public void update() {
        if (this.dirty) {
            this.updateChain();
            this.applyAttachment();
        } else {
            this.updateBody();
        }
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
    public void render() {
        if (this.rayHandler.culling && this.culled) {
            return;
        }
        ++this.rayHandler.lightRenderedLastFrame;
        this.lightMesh.render(this.rayHandler.lightShader, 5, 0, this.vertexNum);
        if (this.soft && !this.xray) {
            this.softShadowMesh.render(this.rayHandler.lightShader, 5, 0, this.vertexNum);
        }
    }

    public void debugRender(ShapeRenderer shapeRenderer) {
        int i;
        shapeRenderer.setColor(Color.YELLOW);
        FloatArray vertices = Pools.obtain(FloatArray.class);
        vertices.clear();
        for (i = 0; i < this.rayNum; ++i) {
            vertices.addAll(this.mx[i], this.my[i]);
        }
        for (i = this.rayNum - 1; i > -1; --i) {
            vertices.addAll(this.startX[i], this.startY[i]);
        }
        shapeRenderer.polygon(vertices.shrink());
        Pools.free(vertices);
    }

    @Override
    public void attachToBody(Body body) {
        this.attachToBody(body, 0.0f);
    }

    public void attachToBody(Body body, float degrees) {
        this.body = body;
        this.bodyPosition.set(body.getPosition());
        this.bodyAngleOffset = (float)Math.PI / 180 * degrees;
        this.bodyAngle = body.getAngle();
        this.applyAttachment();
        if (this.staticLight) {
            this.dirty = true;
        }
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    @Override
    public float getX() {
        return this.tmpPosition.x;
    }

    @Override
    public float getY() {
        return this.tmpPosition.y;
    }

    @Override
    public void setPosition(float x, float y) {
        this.tmpPosition.x = x;
        this.tmpPosition.y = y;
        if (this.staticLight) {
            this.dirty = true;
        }
    }

    @Override
    public void setPosition(Vector2 position) {
        this.tmpPosition.x = position.x;
        this.tmpPosition.y = position.y;
        if (this.staticLight) {
            this.dirty = true;
        }
    }

    @Override
    public boolean contains(float x, float y) {
        int i;
        if (!this.chainLightBounds.contains(x, y)) {
            return false;
        }
        FloatArray vertices = Pools.obtain(FloatArray.class);
        vertices.clear();
        for (i = 0; i < this.rayNum; ++i) {
            vertices.addAll(this.mx[i], this.my[i]);
        }
        for (i = this.rayNum - 1; i > -1; --i) {
            vertices.addAll(this.startX[i], this.startY[i]);
        }
        int intersects = 0;
        for (int i2 = 0; i2 < vertices.size; i2 += 2) {
            float x1 = vertices.items[i2];
            float y1 = vertices.items[i2 + 1];
            float x2 = vertices.items[(i2 + 2) % vertices.size];
            float y2 = vertices.items[(i2 + 3) % vertices.size];
            if (!(y1 <= y && y < y2) && (!(y2 <= y) || !(y < y1)) || !(x < (x2 - x1) / (y2 - y1) * (y - y1) + x1)) continue;
            ++intersects;
        }
        boolean result = intersects & true;
        Pools.free(vertices);
        return result;
    }

    @Override
    public void setDistance(float dist) {
        this.distance = (dist *= RayHandler.gammaCorrectionParameter) < 0.01f ? 0.01f : dist;
        this.dirty = true;
    }

    @Override
    @Deprecated
    public void setDirection(float directionDegree) {
    }

    public void updateChain() {
        Vector2 v1 = Pools.obtain(Vector2.class);
        Vector2 v2 = Pools.obtain(Vector2.class);
        Vector2 vSegmentStart = Pools.obtain(Vector2.class);
        Vector2 vDirection = Pools.obtain(Vector2.class);
        Vector2 vRayOffset = Pools.obtain(Vector2.class);
        Spinor tmpAngle = Pools.obtain(Spinor.class);
        Spinor previousAngle = Pools.obtain(Spinor.class);
        Spinor currentAngle = Pools.obtain(Spinor.class);
        Spinor nextAngle = Pools.obtain(Spinor.class);
        Spinor startAngle = Pools.obtain(Spinor.class);
        Spinor endAngle = Pools.obtain(Spinor.class);
        Spinor rayAngle = Pools.obtain(Spinor.class);
        int segmentCount = this.chain.size / 2 - 1;
        this.segmentAngles.clear();
        this.segmentLengths.clear();
        float remainingLength = 0.0f;
        int i = 0;
        int j = 0;
        while (i < this.chain.size - 2) {
            v1.set(this.chain.items[i + 2], this.chain.items[i + 3]).sub(this.chain.items[i], this.chain.items[i + 1]);
            this.segmentLengths.add(v1.len());
            this.segmentAngles.add(v1.rotate90(this.rayDirection).angle() * ((float)Math.PI / 180));
            remainingLength += this.segmentLengths.items[j];
            i += 2;
            ++j;
        }
        int rayNumber = 0;
        int remainingRays = this.rayNum;
        for (int i2 = 0; i2 < segmentCount; ++i2) {
            previousAngle.set(i2 == 0 ? this.segmentAngles.items[i2] : this.segmentAngles.items[i2 - 1]);
            currentAngle.set(this.segmentAngles.items[i2]);
            nextAngle.set(i2 == this.segmentAngles.size - 1 ? this.segmentAngles.items[i2] : this.segmentAngles.items[i2 + 1]);
            startAngle.set(previousAngle).slerp(currentAngle, 0.5f);
            endAngle.set(currentAngle).slerp(nextAngle, 0.5f);
            int segmentVertex = i2 * 2;
            vSegmentStart.set(this.chain.items[segmentVertex], this.chain.items[segmentVertex + 1]);
            vDirection.set(this.chain.items[segmentVertex + 2], this.chain.items[segmentVertex + 3]).sub(vSegmentStart).nor();
            float raySpacing = remainingLength / (float)remainingRays;
            int segmentRays = i2 == segmentCount - 1 ? remainingRays : (int)(this.segmentLengths.items[i2] / remainingLength * (float)remainingRays);
            for (int j2 = 0; j2 < segmentRays; ++j2) {
                float position = (float)j2 * raySpacing;
                rayAngle.set(startAngle).slerp(endAngle, position / this.segmentLengths.items[i2]);
                float angle = rayAngle.angle();
                vRayOffset.set(this.rayStartOffset, 0.0f).rotateRad(angle);
                v1.set(vDirection).scl(position).add(vSegmentStart).add(vRayOffset);
                this.startX[rayNumber] = v1.x;
                this.startY[rayNumber] = v1.y;
                v2.set(this.distance, 0.0f).rotateRad(angle).add(v1);
                this.endX[rayNumber] = v2.x;
                this.endY[rayNumber] = v2.y;
                ++rayNumber;
            }
            remainingRays -= segmentRays;
            remainingLength -= this.segmentLengths.items[i2];
        }
        Pools.free(v1);
        Pools.free(v2);
        Pools.free(vSegmentStart);
        Pools.free(vDirection);
        Pools.free(vRayOffset);
        Pools.free(previousAngle);
        Pools.free(currentAngle);
        Pools.free(nextAngle);
        Pools.free(startAngle);
        Pools.free(endAngle);
        Pools.free(rayAngle);
        Pools.free(tmpAngle);
    }

    void applyAttachment() {
        if (this.body == null || this.staticLight) {
            return;
        }
        this.restorePosition.setToTranslation(this.bodyPosition);
        this.rotateAroundZero.setToRotationRad(this.bodyAngle + this.bodyAngleOffset);
        for (int i = 0; i < this.rayNum; ++i) {
            this.tmpVec.set(this.startX[i], this.startY[i]).mul(this.rotateAroundZero).mul(this.restorePosition);
            this.startX[i] = this.tmpVec.x;
            this.startY[i] = this.tmpVec.y;
            this.tmpVec.set(this.endX[i], this.endY[i]).mul(this.rotateAroundZero).mul(this.restorePosition);
            this.endX[i] = this.tmpVec.x;
            this.endY[i] = this.tmpVec.y;
        }
    }

    protected boolean cull() {
        if (!this.rayHandler.culling) {
            this.culled = false;
        } else {
            this.updateBoundingRects();
            this.culled = this.chainLightBounds.width > 0.0f && this.chainLightBounds.height > 0.0f && !this.chainLightBounds.overlaps(this.rayHandlerBounds);
        }
        return this.culled;
    }

    void updateBody() {
        if (this.body == null || this.staticLight) {
            return;
        }
        Vector2 vec = this.body.getPosition();
        this.tmpVec.set(0.0f, 0.0f).sub(this.bodyPosition);
        this.bodyPosition.set(vec);
        this.zeroPosition.setToTranslation(this.tmpVec);
        this.restorePosition.setToTranslation(this.bodyPosition);
        this.rotateAroundZero.setToRotationRad(this.bodyAngle).inv().rotateRad(this.body.getAngle());
        this.bodyAngle = this.body.getAngle();
        for (int i = 0; i < this.rayNum; ++i) {
            this.tmpVec.set(this.startX[i], this.startY[i]).mul(this.zeroPosition).mul(this.rotateAroundZero).mul(this.restorePosition);
            this.startX[i] = this.tmpVec.x;
            this.startY[i] = this.tmpVec.y;
            this.tmpVec.set(this.endX[i], this.endY[i]).mul(this.zeroPosition).mul(this.rotateAroundZero).mul(this.restorePosition);
            this.endX[i] = this.tmpVec.x;
            this.endY[i] = this.tmpVec.y;
        }
    }

    protected void updateMesh() {
        for (int i = 0; i < this.rayNum; ++i) {
            this.m_index = i;
            this.f[i] = 1.0f;
            this.mx[i] = this.tmpEnd.x = this.endX[i];
            this.my[i] = this.tmpEnd.y = this.endY[i];
            this.tmpStart.x = this.startX[i];
            this.tmpStart.y = this.startY[i];
            if (this.rayHandler.world == null || this.xray) continue;
            this.rayHandler.world.rayCast(this.ray, this.tmpStart, this.tmpEnd);
        }
        this.setMesh();
    }

    protected void setMesh() {
        int i;
        int size = 0;
        for (i = 0; i < this.rayNum; ++i) {
            this.segments[size++] = this.startX[i];
            this.segments[size++] = this.startY[i];
            this.segments[size++] = this.colorF;
            this.segments[size++] = 1.0f;
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
            this.tmpPerp.set(this.mx[i], this.my[i]).sub(this.startX[i], this.startY[i]).nor().scl(this.softShadowLength * s).add(this.mx[i], this.my[i]);
            this.segments[size++] = this.tmpPerp.x;
            this.segments[size++] = this.tmpPerp.y;
            this.segments[size++] = zeroColorBits;
            this.segments[size++] = 0.0f;
        }
        this.softShadowMesh.setVertices(this.segments, 0, size);
    }

    protected void updateBoundingRects() {
        float maxX = this.startX[0];
        float minX = this.startX[0];
        float maxY = this.startY[0];
        float minY = this.startY[0];
        for (int i = 0; i < this.rayNum; ++i) {
            maxX = maxX > this.startX[i] ? maxX : this.startX[i];
            maxX = maxX > this.mx[i] ? maxX : this.mx[i];
            minX = minX < this.startX[i] ? minX : this.startX[i];
            minX = minX < this.mx[i] ? minX : this.mx[i];
            maxY = maxY > this.startY[i] ? maxY : this.startY[i];
            maxY = maxY > this.my[i] ? maxY : this.my[i];
            minY = minY < this.startY[i] ? minY : this.startY[i];
            minY = minY < this.my[i] ? minY : this.my[i];
        }
        this.chainLightBounds.set(minX, minY, maxX - minX, maxY - minY);
        this.rayHandlerBounds.set(this.rayHandler.x1, this.rayHandler.y1, this.rayHandler.x2 - this.rayHandler.x1, this.rayHandler.y2 - this.rayHandler.y1);
    }
}

