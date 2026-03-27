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
import com.cairn4.moonbase.tiles.Tile;

public class StonePath
extends BaseModule {
    private static final String baseDrawableName = "stonepath";

    public StonePath(World world, Chunk chunk, int x, int y) {
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
        return 1.15f;
    }

    @Override
    protected void createDrawables() {
        String spriteName = baseDrawableName;
        int variation = 1;
        boolean alt = false;
        if (this.x % 2 == 0) {
            if (this.y % 2 != 0) {
                alt = true;
            }
        } else if (this.y % 2 == 0) {
            alt = true;
        }
        boolean alt2 = MathUtils.randomBoolean();
        variation = alt ? (alt2 ? 3 : 4) : (alt2 ? 1 : 2);
        super.createDrawables(spriteName + variation);
        float rotationOffset = MathUtils.random() * 4.0f - 2.0f;
        this.image.setSize(Tile.TILE_SIZE + 6.0f, Tile.TILE_SIZE + 6.0f);
        this.image.setOrigin(1);
        this.image.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.image.setRotation(rotationOffset);
        if (MathUtils.randomBoolean()) {
            this.image.setScaleX(-1.0f);
        }
    }

    @Override
    public void setupLight() {
    }

    @Override
    protected void setBuilderId() {
        this.builderId = baseDrawableName;
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
}

