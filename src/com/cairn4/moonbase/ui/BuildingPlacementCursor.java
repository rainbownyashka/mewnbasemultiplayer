/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.Hud;

public class BuildingPlacementCursor {
    Hud hud;
    private Group group;
    private Image interactCursor;
    private Image invalidX;
    public ItemData currentItemData;
    public boolean warnAdjacent;
    private Group adjacentGroup;
    private Image invalidN;
    private Image invalidE;
    private Image invalidS;
    private Image invalidW;
    private Image invalidNE;
    private Image invalidSE;
    private Image invalidSW;
    private Image invalidNW;
    private ORIENTATIONS orientation;

    public void setWarnAdjacent(boolean warnAdjacent) {
        this.warnAdjacent = warnAdjacent;
    }

    public BuildingPlacementCursor(Hud hud) {
        this.hud = hud;
        this.group = new Group();
        hud.gameScreen.worldUIStage.addActor(this.group);
        this.adjacentGroup = new Group();
        this.adjacentGroup.setColor(1.0f, 1.0f, 1.0f, 0.15f);
        this.group.addActor(this.adjacentGroup);
        this.setupAdjacentImages();
        this.interactCursor = new Image(hud.gameScreen.skin.getDrawable("white"));
        this.interactCursor.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.interactCursor.setOrigin(1);
        this.interactCursor.setColor(0.5f, 0.5f, 1.0f, 0.1f);
        this.interactCursor.setPosition(0.0f, 0.0f, 1);
        this.group.addActor(this.interactCursor);
        this.invalidX = new Image(hud.gameScreen.skin.getDrawable("invalid-build-x"));
        this.invalidX.setVisible(false);
        this.invalidX.setOrigin(1);
        this.invalidX.setScale(0.4f);
        this.invalidX.setPosition(0.0f, 0.0f, 1);
        this.invalidX.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.group.addActor(this.invalidX);
        this.setOrientation(ORIENTATIONS.west);
    }

    private void setupAdjacentImages() {
        this.invalidN = new Image(this.hud.gameScreen.skin.getDrawable("hud/build-warning2"));
        this.invalidN.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.invalidN.setOrigin(1);
        this.invalidN.setPosition(0.0f, Tile.TILE_SIZE, 1);
        this.adjacentGroup.addActor(this.invalidN);
        this.invalidNE = new Image(this.hud.gameScreen.skin.getDrawable("hud/build-warning2"));
        this.invalidNE.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.invalidNE.setOrigin(1);
        this.invalidNE.setPosition(Tile.TILE_SIZE, Tile.TILE_SIZE, 1);
        this.adjacentGroup.addActor(this.invalidNE);
        this.invalidE = new Image(this.hud.gameScreen.skin.getDrawable("hud/build-warning2"));
        this.invalidE.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.invalidE.setOrigin(1);
        this.invalidE.setPosition(Tile.TILE_SIZE, 0.0f, 1);
        this.adjacentGroup.addActor(this.invalidE);
        this.invalidSE = new Image(this.hud.gameScreen.skin.getDrawable("hud/build-warning2"));
        this.invalidSE.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.invalidSE.setOrigin(1);
        this.invalidSE.setPosition(Tile.TILE_SIZE, -Tile.TILE_SIZE, 1);
        this.adjacentGroup.addActor(this.invalidSE);
        this.invalidS = new Image(this.hud.gameScreen.skin.getDrawable("hud/build-warning2"));
        this.invalidS.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.invalidS.setOrigin(1);
        this.invalidS.setPosition(0.0f, -Tile.TILE_SIZE, 1);
        this.adjacentGroup.addActor(this.invalidS);
        this.invalidSW = new Image(this.hud.gameScreen.skin.getDrawable("hud/build-warning2"));
        this.invalidSW.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.invalidSW.setOrigin(1);
        this.invalidSW.setPosition(-Tile.TILE_SIZE, -Tile.TILE_SIZE, 1);
        this.invalidSW.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.adjacentGroup.addActor(this.invalidSW);
        this.invalidW = new Image(this.hud.gameScreen.skin.getDrawable("hud/build-warning2"));
        this.invalidW.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.invalidW.setOrigin(1);
        this.invalidW.setPosition(-Tile.TILE_SIZE, 0.0f, 1);
        this.adjacentGroup.addActor(this.invalidW);
        this.invalidNW = new Image(this.hud.gameScreen.skin.getDrawable("hud/build-warning2"));
        this.invalidNW.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.invalidNW.setOrigin(1);
        this.invalidNW.setPosition(-Tile.TILE_SIZE, Tile.TILE_SIZE, 1);
        this.adjacentGroup.addActor(this.invalidNW);
    }

    public void setPosition(int worldTileX, float worldTileY) {
        this.group.setPosition((float)worldTileX * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, worldTileY * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
    }

    public void clearAdjacentValid() {
        this.adjacentGroup.setVisible(false);
    }

    public void setAdjacenteValid(boolean n, boolean ne, boolean e, boolean se, boolean s, boolean sw, boolean w, boolean nw) {
        this.adjacentGroup.setVisible(true);
        this.invalidN.setVisible(n);
        this.invalidNE.setVisible(ne);
        this.invalidE.setVisible(e);
        this.invalidSE.setVisible(se);
        this.invalidS.setVisible(s);
        this.invalidSW.setVisible(sw);
        this.invalidW.setVisible(w);
        this.invalidNW.setVisible(nw);
    }

    public void setValid(boolean b) {
        if (b) {
            this.interactCursor.setColor(1.0f, 1.0f, 1.0f, 0.35f);
            this.invalidX.setVisible(false);
        } else {
            this.interactCursor.setColor(1.0f, 0.2f, 0.2f, 0.15f);
            this.invalidX.setVisible(true);
        }
    }

    public void show() {
        this.group.setVisible(true);
    }

    public void hide() {
        this.group.setVisible(false);
    }

    public void setSprite(String drawableName) {
        Drawable d;
        if (drawableName == null) {
            drawableName = "white";
        }
        try {
            d = this.hud.gameScreen.skin.getDrawable(drawableName);
        }
        catch (Exception e) {
            MoonBase.error("BuildingPlacementCursor: missing sprite in atlas: " + drawableName);
            d = this.hud.gameScreen.skin.getDrawable("missing");
        }
        this.interactCursor.setDrawable(d);
        this.interactCursor.setScale(0.95f, 0.95f);
        this.resetOrientation();
    }

    public void resetOrientation() {
        this.interactCursor.clearActions();
        this.orientation = ORIENTATIONS.west;
        this.interactCursor.setRotation(0.0f);
    }

    public void setOrientation(ORIENTATIONS o) {
        this.orientation = o;
        this.interactCursor.clearActions();
        switch (o) {
            case north: {
                this.interactCursor.addAction(Actions.sequence((Action)Actions.rotateTo(0.0f), (Action)Actions.rotateTo(-90.0f, 0.1f, Interpolation.sine)));
                break;
            }
            case east: {
                this.interactCursor.addAction(Actions.sequence((Action)Actions.rotateTo(270.0f), (Action)Actions.rotateTo(180.0f, 0.1f, Interpolation.sine)));
                break;
            }
            case south: {
                this.interactCursor.addAction(Actions.sequence((Action)Actions.rotateTo(180.0f), (Action)Actions.rotateTo(90.0f, 0.1f, Interpolation.sine)));
                break;
            }
            case west: {
                this.interactCursor.addAction(Actions.sequence((Action)Actions.rotateTo(90.0f), (Action)Actions.rotateTo(0.0f, 0.1f, Interpolation.sine)));
            }
        }
    }

    public void setSpriteScale(float scale) {
        this.interactCursor.setScale(scale);
    }

    public void setSpriteOffset(Vector2 placementSpriteOffset) {
        this.interactCursor.setPosition(placementSpriteOffset.x, placementSpriteOffset.y, 1);
    }

    public boolean proxyPlacementInRange(World world, int placementX, int placementY) {
        if (this.currentItemData != null && this.currentItemData.proxyTiles.size() > 0) {
            for (GridPoint2 gp : this.currentItemData.proxyTiles) {
                int x = placementX + gp.x;
                int y = placementY + gp.y;
                int diffX = Math.abs(x - world.getPlayer().getX());
                int diffY = Math.abs(y - world.getPlayer().getY());
                if (diffX > 1 || diffY > 1) continue;
                return true;
            }
        }
        return false;
    }

    public boolean checkProxyChecks(World world, int worldX, int worldY) {
        if (this.currentItemData != null) {
            for (GridPoint2 gp : this.currentItemData.proxyTiles) {
                int x = worldX + gp.x;
                int y = worldY + gp.y;
                if (world.isTileEmpty(x, y)) continue;
                return false;
            }
        }
        return true;
    }

    public static enum ORIENTATIONS {
        north,
        east,
        south,
        west;

    }
}

