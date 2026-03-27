/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.entities.TileNode;
import com.cairn4.moonbase.tiles.Tile;

public class TileNodeConnection
implements Connection<TileNode> {
    TileNode fromNode;
    TileNode toNode;
    float cost;

    public TileNodeConnection(TileNode fromNode, TileNode toNode) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.cost = Vector2.dst(fromNode.x, fromNode.y, toNode.x, toNode.y);
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
        shapeRenderer.rectLine((float)this.fromNode.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)this.fromNode.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)this.toNode.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)this.toNode.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, 4.0f);
        shapeRenderer.end();
    }

    @Override
    public float getCost() {
        return this.cost;
    }

    @Override
    public TileNode getFromNode() {
        return this.fromNode;
    }

    @Override
    public TileNode getToNode() {
        return this.toNode;
    }
}

