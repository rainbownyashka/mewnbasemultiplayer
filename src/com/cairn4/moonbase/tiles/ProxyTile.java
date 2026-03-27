/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.Tile;

public class ProxyTile
extends Tile {
    public Tile parent;

    public ProxyTile(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
    }

    public ProxyTile(World world, Chunk chunk, int x, int y, Tile parentTile) {
        super(world, chunk, x, y);
        this.parent = parentTile;
    }

    @Override
    protected void createDrawables() {
        this.group.remove();
        this.group = new Group();
        this.group.clearChildren();
        this.group.setOrigin(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f);
        this.group.setPosition(this.getWorldXPos(), this.getWorldYPos());
        this.group.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + TILE_SIZE / 3.0f));
        this.world.gameScreen.addToStage(this.group, this.world.gameScreen.mainGroup);
        this.image = new Image(this.world.gameScreen.skin.getDrawable("white"));
        this.image.setColor(0.0f, 0.2f, 1.0f, 0.1f);
        this.image.setSize(TILE_SIZE, TILE_SIZE);
        this.image.setOrigin(1);
        this.image.setPosition(TILE_SPACING, 0.0f);
        this.group.addActor(this.image);
        this.image.setVisible(false);
    }
}

