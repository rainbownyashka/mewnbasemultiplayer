/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;

public class Rocket
extends Entity {
    float fuel;
    static final float maxFuel = 50.0f;
    public int worldX;
    public int worldY;

    public Rocket(World world, float xPos, float yPos, float rotation) {
        this(world, xPos, yPos);
    }

    public Rocket(World world, float xPos, float yPos) {
        super(world, xPos, yPos);
        this.createDrawables();
    }

    private void createDrawables() {
        this.group = new Group();
        this.group.setPosition(this.spawnPosX, this.spawnPosY);
        this.world.gameScreen.mainGroup.addActor(this.group);
    }

    public float getFuel() {
        return this.fuel;
    }

    public void setFuel(float f) {
        this.fuel = f;
        MoonBase.log("new fuel amount: " + this.fuel);
    }

    public float addFuel(float amount) {
        this.fuel += amount;
        if (this.fuel > 50.0f) {
            float overhead = this.fuel - 50.0f;
            this.fuel = 50.0f;
            return overhead;
        }
        return 0.0f;
    }

    public float getMaxFuel() {
        return 50.0f;
    }

    public boolean isFull() {
        return this.fuel >= 50.0f;
    }

    public float getFuelRoom() {
        return 50.0f - this.fuel;
    }
}

