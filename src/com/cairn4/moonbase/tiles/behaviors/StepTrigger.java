/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.math.Rectangle;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.StepTriggerEvent;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class StepTrigger
implements Behavior {
    public World world;
    public BaseModule baseModule;
    public StepTriggerEvent stepTriggerEvent;
    public Rectangle triggerArea;
    public boolean playerOn;

    @Override
    public void setLoaded(boolean loaded) {
    }

    @Override
    public boolean isLoaded() {
        return false;
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
    public void update(float v) {
        if (this.checkPlayerOn() != this.playerOn) {
            boolean bl = this.playerOn = !this.playerOn;
            if (this.stepTriggerEvent != null) {
                if (this.playerOn) {
                    this.stepTriggerEvent.entered();
                } else {
                    this.stepTriggerEvent.exit();
                }
            }
        }
    }

    private boolean checkPlayerOn() {
        if (this.baseModule != null && this.world.player != null) {
            if (this.triggerArea != null) {
                return this.triggerArea.contains(this.world.player.getXPos(), this.world.player.getYPos());
            }
            return this.world.player.getX() == this.baseModule.worldX && this.world.player.getY() == this.baseModule.worldY;
        }
        return false;
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

