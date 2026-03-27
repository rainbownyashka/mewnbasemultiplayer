/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;

public class PavementStripedFloor
extends BaseModule {
    public PavementStripedFloor(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.blocker = false;
        this.walkSoundFx = "sounds/footstep-pavement";
    }

    @Override
    public Color getMinimapColor() {
        return Color.valueOf("3c3324");
    }

    @Override
    public boolean isFloorTile() {
        return true;
    }

    @Override
    public float getMovementMultiplier() {
        return 1.3f;
    }

    @Override
    protected void createDrawables() {
        boolean alt = false;
        if (this.x % 2 == 0) {
            if (this.y % 2 != 0) {
                alt = true;
            }
        } else if (this.y % 2 == 0) {
            alt = true;
        }
        if (alt) {
            super.createDrawables("pavement-striped1");
        } else {
            super.createDrawables("pavement-striped2");
        }
        this.image.setSize(this.image.getWidth() + 4.0f, this.image.getHeight() + 4.0f);
        this.image.setOrigin(1);
        this.image.setPosition(-2.0f, -2.0f);
    }

    @Override
    public void setupLight() {
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "pavement-striped";
    }

    @Override
    public boolean usesBaseGroup() {
        return false;
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    public boolean visuallyConnectToConduits() {
        return false;
    }

    @Override
    public boolean canInteract(Player player) {
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            this.world.gameScreen.addToStage(this.group, this.world.gameScreen.floorGroup);
            this.group.setVisible(true);
            this.group.toBack();
        } else {
            this.group.remove();
            this.group.setVisible(false);
        }
    }
}

