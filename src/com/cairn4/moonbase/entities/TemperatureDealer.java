/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;

public class TemperatureDealer
extends Entity {
    public float temp;
    public float radius;
    public float duration;
    public float timer;

    public TemperatureDealer(World world, float xPos, float yPos) {
        super(world, xPos, yPos);
        this.group = new Group();
        this.group.setPosition(xPos, yPos);
    }

    @Override
    public boolean isSaved() {
        return false;
    }

    @Override
    public void update(float delta) {
        if (this.duration != 0.0f) {
            this.timer += delta;
            if (this.timer > this.duration) {
                this.readyToRemove = true;
            }
        }
    }

    public float getTemp(float distanceToDealer) {
        float perc = distanceToDealer / this.radius;
        return this.temp - perc * this.temp;
    }
}

