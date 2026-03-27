/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import box2dLight.Light;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class LightPulseBehavior
implements Behavior {
    public boolean loaded = false;
    public Light light;
    public float speed = 2.0f;
    public float max = 0.5f;
    public float min = 0.3f;
    float time;

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public void setId(String s) {
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void start() {
    }

    @Override
    public void update(float delta) {
        this.time += delta;
        float a = MathUtils.sin(this.time * this.speed) / 2.0f + 0.5f;
        a *= this.max;
        Color c = this.light.getColor();
        c.a = a += this.min;
        this.light.setColor(c);
        this.light.setDistance(0.3125f * a);
    }

    @Override
    public void interact(Player player) {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public BehaviorData getData() {
        return null;
    }
}

