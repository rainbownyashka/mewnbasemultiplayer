/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;

public class PavementFloor
extends BaseModule {
    public PavementFloor(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.blocker = false;
        this.walkSoundFx = "sounds/footstep-pavement";
    }

    @Override
    public Color getMinimapColor() {
        return Color.valueOf("332e2c");
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
        String drawableName = "pavement";
        if (MathUtils.random() > 0.5f) {
            drawableName = "pavement2";
        }
        super.createDrawables(drawableName);
        this.image.setSize(this.image.getWidth() + 4.0f, this.image.getHeight() + 4.0f);
        this.image.setOrigin(1);
        this.image.setPosition(-2.0f, -2.0f);
        if (MathUtils.randomBoolean()) {
            this.image.setScaleX(-1.0f);
        }
    }

    @Override
    public void setupLight() {
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "pavement";
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

