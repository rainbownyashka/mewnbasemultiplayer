/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.Tile;

public class TileNode {
    Tile tileRef;
    int x;
    int y;
    int index = 0;

    public TileNode(World world, int worldX, int worldY) {
        this.x = worldX;
        this.y = worldY;
        this.tileRef = world.getTile(this.x, this.y);
    }

    public float distanceToTarget(int targetX, int targetY) {
        return new Vector2(this.x, this.y).dst(targetX, targetY);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void render(ShapeRenderer shapeRenderer, boolean inPath) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (inPath) {
            shapeRenderer.setColor(0.0f, 1.0f, 0.0f, 1.0f);
        } else {
            shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 1.0f);
        }
        shapeRenderer.circle((float)this.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)this.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, 20.0f);
        shapeRenderer.end();
    }
}

