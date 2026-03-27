/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Meteor;

public class MeteorTest {
    World world;
    float timer;
    float time = 0.9f;
    float maxTime = 0.9f;
    float minTime = 0.5f;

    public MeteorTest(World world) {
        this.world = world;
        this.timer = 0.0f;
    }

    public void update(float delta) {
        this.timer += delta;
        if (this.timer > this.time) {
            this.timer = 0.0f;
            this.time = MathUtils.random(this.minTime, this.maxTime);
            int x = MathUtils.random(500, 506);
            int y = MathUtils.random(503, 507);
            new Meteor(this.world, this.world.gameScreen.mainGroup, x, y);
        }
    }
}

