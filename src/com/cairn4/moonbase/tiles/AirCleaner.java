/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.HabitatModule;

public class AirCleaner
extends HabitatModule {
    public static final float OXYGEN_REGEN_RATE = 2.0f;
    Image fanBase;
    Image fan;
    private boolean shouldBeMakingOxygen;
    private boolean fansOn;
    private float fanSpeed;
    private static float fanSpeedNormal = 97.0f;
    private static float fanSpeedSlow = 36.0f;

    public AirCleaner(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerPriority = 10;
        this.powerDrawRate = 3.0f;
        this.oxygenGenRate = 2.0f;
        this.fanBase = new Image(world.gameScreen.skin.getDrawable("aircleaner"));
        this.fanBase.setSize(TILE_SIZE * 0.7f, TILE_SIZE * 0.7f);
        this.fanBase.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f, 1);
        this.componentGroup.addActor(this.fanBase);
        this.fan = new Image(world.gameScreen.skin.getDrawable("aircleaner-fan"));
        this.fan.setSize(TILE_SIZE * 0.4f, TILE_SIZE * 0.4f);
        this.fan.setOrigin(1);
        this.fan.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f, 1);
        this.componentGroup.addActor(this.fan);
        this.fansOn = true;
        this.updateBases();
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "aircleaner";
    }

    private void spinFan(float delta) {
        this.fan.rotateBy(-this.fanSpeed * delta);
        if (this.fan.getRotation() >= 360.0f) {
            this.fan.rotateBy(-360.0f);
        }
    }

    @Override
    public void interact(Player player) {
        super.interact(player);
    }

    @Override
    public void update(float delta) {
        this.shouldBeMakingOxygen = this.getBaseGroup() != null ? this.getBaseGroup().getTotalMaxOxygenStored() - this.getBaseGroup().getTotalOxygenStored() > 0.3f : false;
        if (this.hasPower) {
            if (this.getBaseGroup() != null) {
                this.fanSpeed = this.shouldBeMakingOxygen ? fanSpeedNormal : fanSpeedSlow;
            }
        } else {
            this.fanSpeed = 0.0f;
        }
        this.spinFan(delta);
        super.update(delta);
    }

    @Override
    public float getPowerDrawRate() {
        if (this.shouldBeMakingOxygen) {
            return this.powerDrawRate;
        }
        return this.powerDrawRate / 2.0f;
    }

    @Override
    public void setPower(boolean on) {
        if (this.hasPower != on && this.fan != null) {
            if (!this.hasPower) {
                this.animateColor(this.fanBase, Color.WHITE, 0.25f);
                this.fansOn = true;
            } else {
                this.animateColor(this.fanBase, POWER_OFF_COLOR, 0.25f);
                this.fansOn = false;
                this.oxygenGenRate = 0.0f;
            }
        }
        super.setPower(on);
    }

    @Override
    public float getOxygenGenRate() {
        this.oxygenGenRate = this.hasPower ? 2.0f : 0.0f;
        return this.oxygenGenRate;
    }
}

