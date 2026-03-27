/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.Hud;

public class TileProgressBar {
    Hud hud;
    Tile tile;
    Group group;
    Image progressBar;
    Image border;
    private static final float barWidth = Tile.TILE_SIZE - 30.0f - 14.0f;

    public TileProgressBar(Hud hud, Tile t) {
        this.tile = t;
        this.group = new Group();
        hud.gameScreen.worldUIStage.addActor(this.group);
        this.group.setPosition((float)t.worldX * Tile.GRID_SIZE, (float)t.worldY * Tile.GRID_SIZE + Tile.TILE_SIZE - 3.0f);
        this.group.setVisible(false);
        NinePatch borderNinePatch = new NinePatch(hud.skin.getRegion("progress-border"), 8, 8, 8, 8);
        this.border = new Image(borderNinePatch);
        this.border.setWidth(Tile.TILE_SIZE - 30.0f);
        this.border.setHeight(18.0f);
        this.border.setColor(Color.BLACK);
        this.border.setPosition(15.0f, 0.0f);
        this.group.addActor(this.border);
        NinePatch progressBarNinePatch = new NinePatch(hud.skin.getRegion("tile-progress-bar"), 3, 3, 3, 3);
        this.progressBar = new Image(progressBarNinePatch);
        this.progressBar.setColor(Color.RED);
        this.progressBar.setWidth(barWidth);
        this.progressBar.setHeight(6.0f);
        this.progressBar.setPosition(22.0f, 6.0f);
        this.group.addActor(this.progressBar);
        this.group.setOrigin(this.border.getX(1), this.border.getY(1));
        this.group.addAction(Actions.sequence((Action)Actions.scaleTo(0.8f, 0.8f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.15f)));
    }

    public void set(float progress) {
        this.group.setVisible(true);
        this.progressBar.setWidth(progress * barWidth);
    }

    public void remove() {
        Gdx.app.debug("MewnBase", "TileProgressBar: remove()");
        this.group.clearActions();
        this.group.addAction(Actions.sequence((Action)Actions.fadeOut(0.2f), (Action)Actions.removeActor()));
    }

    public void update() {
        if (this.tile.getInteractProgress() > 0.0f) {
            this.set(this.tile.getInteractProgress());
        } else {
            this.group.setVisible(false);
        }
    }
}

