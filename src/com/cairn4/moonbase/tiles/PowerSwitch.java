/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.Conduit;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.ToggleBehavior;

public class PowerSwitch
extends Conduit {
    Image switchImage;
    ToggleBehavior toggleBehavior = new ToggleBehavior();

    public PowerSwitch(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.toggleBehavior.on = true;
        this.behaviorList.add(this.toggleBehavior);
    }

    @Override
    protected void createDrawables() {
        super.createDrawables();
        this.switchImage = new Image(this.world.gameScreen.skin.getDrawable("powerSwitchOn"));
        this.switchImage.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.switchImage.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.group.addActor(this.switchImage);
    }

    @Override
    public void startBehaviors() {
        if (this.toggleBehavior.on) {
            this.switchImage.setDrawable(this.world.gameScreen.skin.getDrawable("powerSwitchOn"));
        } else {
            this.switchImage.setDrawable(this.world.gameScreen.skin.getDrawable("powerSwitchOff"));
        }
        this.updateBases();
    }

    @Override
    public void interact(Player player) {
        this.toggle();
    }

    private void toggle() {
        boolean bl = this.toggleBehavior.on = !this.toggleBehavior.on;
        if (this.toggleBehavior.on) {
            this.switchImage.setDrawable(this.world.gameScreen.skin.getDrawable("powerSwitchOn"));
        } else {
            this.switchImage.setDrawable(this.world.gameScreen.skin.getDrawable("powerSwitchOff"));
        }
        this.updateBases();
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "powerswitch";
    }

    @Override
    public boolean usesBaseGroup() {
        if (this.toggleBehavior != null) {
            return this.toggleBehavior.on;
        }
        return true;
    }

    @Override
    public boolean canInteract(Player player) {
        return true;
    }
}

