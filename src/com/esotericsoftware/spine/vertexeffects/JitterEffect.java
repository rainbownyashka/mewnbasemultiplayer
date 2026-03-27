/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.vertexeffects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonRenderer;

public class JitterEffect
implements SkeletonRenderer.VertexEffect {
    private float x;
    private float y;

    public JitterEffect(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void begin(Skeleton skeleton) {
    }

    @Override
    public void transform(Vector2 position, Vector2 uv, Color light, Color dark) {
        position.x += MathUtils.randomTriangular(-this.x, this.y);
        position.y += MathUtils.randomTriangular(-this.x, this.y);
    }

    @Override
    public void end() {
    }

    public void setJitter(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setJitterX(float x) {
        this.x = x;
    }

    public void setJitterY(float y) {
        this.y = y;
    }
}

