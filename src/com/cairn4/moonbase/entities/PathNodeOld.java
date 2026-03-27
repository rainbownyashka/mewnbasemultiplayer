/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.graphics.Color;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.PathFindingOld;
import com.cairn4.moonbase.tiles.Tile;

public class PathNodeOld {
    World world;
    public int index;
    PathFindingOld pathFindingOld;
    public Tile tile;
    public PathNodeOld parentTile;
    public int f = -1;
    public int g = -1;
    public int h = 99999;
    public boolean closed = false;
    public boolean walkable = false;
    public int moveCost = 1;
    int x;
    int y;
    private static final Color debugIdleColor = new Color(1.0f, 1.0f, 1.0f, 0.0f);

    public PathNodeOld(World world, int worldX, int worldY, PathFindingOld pf) {
        this.world = world;
        this.x = worldX;
        this.y = worldY;
        this.pathFindingOld = pf;
        this.tile = world.getTile(this.x, this.y);
        this.reset();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void reset() {
        this.f = -1;
        this.g = -1;
        this.h = -1;
        this.tile = this.world.getTile(this.x, this.y);
        this.parentTile = null;
        this.closed = false;
        this.walkable = true;
        if (this.tile != null && this.tile.getBody() != null) {
            this.walkable = false;
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getG() {
        return this.g;
    }

    public int getF() {
        return this.f;
    }

    public void setF(int newF) {
        this.f = newF;
    }

    public void setG(int newG) {
        this.g = newG;
    }

    public int calcH() {
        PathNodeOld target = this.pathFindingOld.targetNode;
        int xDist = 0;
        int yDist = 0;
        xDist = Math.abs(target.getX() - this.getX());
        yDist = Math.abs(target.getY() - this.getY());
        this.h = xDist + yDist;
        return this.h;
    }

    public void animateDebugColor(float r, float g, float b) {
    }
}

