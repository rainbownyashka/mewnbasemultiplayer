/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.Hud;

public class ResourceIndicator {
    Hud hud;
    Group parentGroup;
    Group group;
    public GroundTile groundTile;
    int x;
    int y;
    Image crosshair;
    Image crossTL;
    Image crossBL;
    Image crossTR;
    Image crossBR;

    public ResourceIndicator(Hud hud, Group parentGroup, GroundTile groundTile) {
        this.hud = hud;
        this.parentGroup = parentGroup;
        this.groundTile = groundTile;
        this.x = this.groundTile.worldX;
        this.y = this.groundTile.worldY;
        this.createDrawables();
    }

    public void createDrawables() {
        this.group = new Group();
        Vector2 worldPos = new Vector2((float)this.x * Tile.GRID_SIZE, (float)this.y * Tile.GRID_SIZE);
        this.group.setPosition(worldPos.x, worldPos.y);
        this.parentGroup.addActor(this.group);
        this.crosshair = new Image(this.hud.gameScreen.skin.getDrawable("target-crosshair"));
        this.crosshair.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.group.addActor(this.crosshair);
        this.crossTL = new Image(this.hud.gameScreen.skin.getDrawable("target-corner-tl"));
        this.crossTL.setPosition(0.0f, Tile.TILE_SIZE / 2.0f, 10);
        this.group.addActor(this.crossTL);
        this.crossTL.addAction(Actions.forever(Actions.sequence((Action)Actions.moveBy(-6.0f, 6.0f, 0.5f, Interpolation.sine), (Action)Actions.moveBy(6.0f, -6.0f, 0.5f, Interpolation.sine))));
        this.crossBL = new Image(this.hud.gameScreen.skin.getDrawable("target-corner-bl"));
        this.crossBL.setPosition(0.0f, 0.0f, 12);
        this.group.addActor(this.crossBL);
        this.crossBL.addAction(Actions.forever(Actions.sequence((Action)Actions.moveBy(-6.0f, -6.0f, 0.5f, Interpolation.sine), (Action)Actions.moveBy(6.0f, 6.0f, 0.5f, Interpolation.sine))));
        this.crossTR = new Image(this.hud.gameScreen.skin.getDrawable("target-corner-tr"));
        this.crossTR.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 18);
        this.group.addActor(this.crossTR);
        this.crossTR.addAction(Actions.forever(Actions.sequence((Action)Actions.moveBy(6.0f, 6.0f, 0.5f, Interpolation.sine), (Action)Actions.moveBy(-6.0f, -6.0f, 0.5f, Interpolation.sine))));
        this.crossBR = new Image(this.hud.gameScreen.skin.getDrawable("target-corner-br"));
        this.crossBR.setPosition(Tile.TILE_SIZE / 2.0f, 0.0f, 20);
        this.group.addActor(this.crossBR);
        this.crossBR.addAction(Actions.forever(Actions.sequence((Action)Actions.moveBy(6.0f, -6.0f, 0.5f, Interpolation.sine), (Action)Actions.moveBy(-6.0f, 6.0f, 0.5f, Interpolation.sine))));
    }

    public void destroy() {
        this.group.clearChildren();
        this.group.remove();
    }
}

