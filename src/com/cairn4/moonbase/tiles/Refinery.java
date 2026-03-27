/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;

public class Refinery
extends BaseModule {
    public boolean makingFuel;
    private Image backing;
    private Image piston;
    private Image needle;
    private float operatingPowerRate;

    public Refinery(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = this.operatingPowerRate = 20.0f;
        this.makingFuel = false;
        this.setupPhysics("solar");
        this.updateBases();
    }

    @Override
    public void startBehaviors() {
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "refinery";
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("refinery", this.world.gameScreen.mainGroup);
        this.image.setSize(TILE_SIZE + 40.0f, TILE_SIZE + 40.0f);
        this.image.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f + 20.0f, 1);
        this.backing = new Image(this.world.gameScreen.skin.getDrawable("refinery-rear"));
        this.backing.setPosition(68.0f, 33.0f);
        this.backing.setScale(1.3f);
        this.group.addActor(this.backing);
        this.piston = new Image(this.world.gameScreen.skin.getDrawable("refinery-piston"));
        this.piston.setPosition(74.0f, 20.0f);
        this.piston.setOrigin(4);
        this.group.addActor(this.piston);
        this.needle = new Image(this.world.gameScreen.skin.getDrawable("refinery-needle"));
        this.needle.setPosition(83.0f, 71.0f);
        this.needle.setOrigin(3.0f, 3.0f);
        this.needle.setScale(1.2f);
        this.group.addActor(this.needle);
        this.piston.toBack();
        this.backing.toBack();
    }

    @Override
    public float getOxygenDrawRate() {
        if (this.makingFuel) {
            return 4.0f;
        }
        return 0.0f;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        boolean roomForFuel = this.getBaseGroup().getTotalFuelStored() < this.getBaseGroup().getTotalMaxFuelStored();
        this.updatePump(this.hasAir && this.hasPower && roomForFuel);
        this.fuelGenRate = this.makingFuel ? 1.0f : 0.0f;
    }

    private void updatePump(boolean on) {
        if (!this.makingFuel && on) {
            this.startPumpAnim();
            this.makingFuel = true;
        } else if (this.makingFuel && !on) {
            this.stopPumpAnim();
            this.makingFuel = false;
        }
    }

    @Override
    protected void setOxygen(boolean on) {
        super.setOxygen(on);
    }

    private void stopPumpAnim() {
        this.piston.clearActions();
        this.needle.clearActions();
        this.piston.addAction(Actions.sequence((Action)Actions.scaleTo(1.2f, 1.0f, 1.0f, Interpolation.sine)));
        this.needle.addAction(Actions.sequence((Action)Actions.rotateTo(45.0f, 1.0f, Interpolation.sine)));
    }

    private void startPumpAnim() {
        this.piston.clearActions();
        this.needle.clearActions();
        this.piston.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.4f, 1.0f, 0.5f, Interpolation.sine), (Action)Actions.scaleTo(1.0f, 1.2f, 0.5f, Interpolation.sine))));
        this.needle.addAction(Actions.forever(Actions.sequence((Action)Actions.rotateTo(0.0f, 0.08f, Interpolation.sine), (Action)Actions.rotateTo(8.0f, 0.11f, Interpolation.sine))));
    }

    @Override
    public void setPower(boolean on) {
        if (this.hasPower != on) {
            if (this.piston != null) {
                if (!this.hasPower) {
                    this.animateColor(this.piston, Color.WHITE, 0.25f);
                } else {
                    this.animateColor(this.piston, POWER_OFF_COLOR, 0.25f);
                }
            }
            if (this.needle != null) {
                if (!this.hasPower) {
                    this.animateColor(this.needle, Color.WHITE, 0.25f);
                } else {
                    this.animateColor(this.needle, POWER_OFF_COLOR, 0.25f);
                }
            }
        }
        super.setPower(on);
    }
}

