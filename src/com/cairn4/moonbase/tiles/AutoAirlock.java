/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Timer;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.Airlock;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.StepTrigger;
import com.cairn4.moonbase.tiles.behaviors.StepTriggerEvent;

public class AutoAirlock
extends Airlock {
    StepTrigger stepTrigger;

    public AutoAirlock(World world, Chunk chunk, int x, int y, BaseModule.ORIENTATIONS o) {
        super(world, chunk, x, y, o);
        this.powerDrawRate = 4.0f;
        this.stepTrigger = new StepTrigger();
        this.stepTrigger.baseModule = this;
        this.stepTrigger.world = world;
        this.stepTrigger.triggerArea = new Rectangle(this.getWorldXPos() + 30.0f, this.getWorldYPos() + 30.0f, TILE_SIZE - 60.0f, TILE_SIZE - 60.0f);
        this.stepTrigger.stepTriggerEvent = new StepTriggerEvent(){

            @Override
            public void entered() {
                AutoAirlock.this.enteredAirlock();
            }

            @Override
            public void exit() {
                AutoAirlock.this.exitAirlock();
            }
        };
        this.behaviorList.add(this.stepTrigger);
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("autoairlock-floor");
        this.group.setOrigin(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "autoairlock";
    }

    public void enteredAirlock() {
        Gdx.app.debug("MewnBase", "Stepped into auto-airlock");
        if (this.hasPower) {
            Timer timer = new Timer();
            final AutoAirlock a = this;
            Timer.schedule(new Timer.Task(){

                @Override
                public void run() {
                    a.interactWithoutRepair(AutoAirlock.this.stepTrigger.world.player);
                }
            }, 0.1f);
        } else {
            Gdx.app.debug("MewnBase", "No power to trigger auto-airlock!");
        }
    }

    public void interactWithoutRepair(Player player) {
        this.interactMain(player);
    }

    public void exitAirlock() {
        System.out.println("exited airlock");
    }
}

