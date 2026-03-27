/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.math.GridPoint2;

class PathFindingAction {
    public GridPoint2 destination;
    public GridPoint2 interactTile;
    public float waitBeforeContinuing = 2.0f;

    PathFindingAction() {
    }

    public void setDestination(GridPoint2 gp) {
        this.destination = new GridPoint2(gp.x, gp.y);
    }

    public void setInterActTile(GridPoint2 gp) {
        this.interactTile = new GridPoint2(gp.x, gp.y);
    }

    public void setInterActTile(int worldX, int worldY) {
        this.interactTile = new GridPoint2(worldX, worldY);
    }
}

