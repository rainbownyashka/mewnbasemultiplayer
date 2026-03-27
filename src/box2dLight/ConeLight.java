/*
 * Decompiled with CFR 0.152.
 */
package box2dLight;

import box2dLight.PositionalLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class ConeLight
extends PositionalLight {
    float coneDegree;

    public ConeLight(RayHandler rayHandler, int rays, Color color, float distance, float x, float y, float directionDegree, float coneDegree) {
        super(rayHandler, rays, color, distance, x, y, directionDegree);
        this.setConeDegree(coneDegree);
    }

    @Override
    public void update() {
        this.updateBody();
        if (this.dirty) {
            this.setEndPoints();
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
    public void setDirection(float direction) {
        this.direction = direction;
        this.dirty = true;
    }

    public float getConeDegree() {
        return this.coneDegree;
    }

    public void setConeDegree(float coneDegree) {
        this.coneDegree = MathUtils.clamp(coneDegree, 0.0f, 180.0f);
        this.dirty = true;
    }

    @Override
    public void setDistance(float dist) {
        this.distance = (dist *= RayHandler.gammaCorrectionParameter) < 0.01f ? 0.01f : dist;
        this.dirty = true;
    }

    protected void setEndPoints() {
        for (int i = 0; i < this.rayNum; ++i) {
            float angle = this.direction + this.coneDegree - 2.0f * this.coneDegree * (float)i / ((float)this.rayNum - 1.0f);
            float s = this.sin[i] = MathUtils.sinDeg(angle);
            float c = this.cos[i] = MathUtils.cosDeg(angle);
            this.endX[i] = this.distance * c;
            this.endY[i] = this.distance * s;
        }
    }
}

