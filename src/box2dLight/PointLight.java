/*
 * Decompiled with CFR 0.152.
 */
package box2dLight;

import box2dLight.Light;
import box2dLight.PositionalLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class PointLight
extends PositionalLight {
    public PointLight(RayHandler rayHandler, int rays) {
        this(rayHandler, rays, Light.DefaultColor, 15.0f, 0.0f, 0.0f);
    }

    public PointLight(RayHandler rayHandler, int rays, Color color, float distance, float x, float y) {
        super(rayHandler, rays, color, distance, x, y, 0.0f);
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
    public void setDistance(float dist) {
        this.distance = (dist *= RayHandler.gammaCorrectionParameter) < 0.01f ? 0.01f : dist;
        this.dirty = true;
    }

    void setEndPoints() {
        float angleNum = 360.0f / (float)(this.rayNum - 1);
        for (int i = 0; i < this.rayNum; ++i) {
            float angle = angleNum * (float)i;
            this.sin[i] = MathUtils.sinDeg(angle);
            this.cos[i] = MathUtils.cosDeg(angle);
            this.endX[i] = this.distance * this.cos[i];
            this.endY[i] = this.distance * this.sin[i];
        }
    }

    @Override
    @Deprecated
    public void setDirection(float directionDegree) {
    }
}

