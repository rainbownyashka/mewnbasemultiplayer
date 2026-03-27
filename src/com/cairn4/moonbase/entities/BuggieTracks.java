/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.cairn4.moonbase.entities.Wheel;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.GameScreen;
import java.util.ArrayList;

public class BuggieTracks {
    GameScreen gameScreen;
    private Wheel wheel;
    Vector2 playerPos;
    Vector2 lastPrint;
    static final float stepDistance = Tile.GRID_SIZE / 10.0f;
    static final float minStepDistance = 10.0f;
    private final Vector2 offset = new Vector2(0.0f, 0.0f);
    ArrayList<Image> footPrintList = new ArrayList();
    private final Pool<Image> footPrintPool = Pools.get(Image.class);
    float velocityMul = 1.0f;

    public BuggieTracks(GameScreen gameScreen, Wheel wheel) {
        this.gameScreen = gameScreen;
        this.wheel = wheel;
        this.playerPos = new Vector2(wheel.getXPos(), wheel.getYPos());
        this.lastPrint = new Vector2(wheel.getXPos(), wheel.getYPos());
    }

    public void update(float rotation) {
        this.playerPos.set(this.wheel.getXPos() + this.offset.x, this.wheel.getYPos() + this.offset.y);
        if (this.playerPos.dst(this.lastPrint) >= stepDistance * this.velocityMul + 10.0f) {
            this.addStep(rotation);
            this.lastPrint.set(this.wheel.getXPos() + this.offset.x, this.wheel.getYPos() + this.offset.y);
        }
    }

    public void addStep(float rotation) {
        Image i = this.footPrintPool.obtain();
        i.setDrawable(this.gameScreen.skin.getDrawable("buggie-track"));
        i.setSize(Tile.TILE_SIZE / 5.0f * Tile.SCALE, Tile.TILE_SIZE / 5.0f);
        i.setOrigin(1);
        i.setPosition(this.playerPos.x, this.playerPos.y, 1);
        i.setRotation(rotation);
        i.setColor(1.0f, 1.0f, 1.0f, 0.15f);
        this.gameScreen.bgGroup.addActor(i);
        this.footPrintList.add(i);
        i.addAction(Actions.sequence((Action)Actions.delay(1.0f), (Action)Actions.fadeOut(3.0f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                BuggieTracks.this.footPrintPool.free(BuggieTracks.this.footPrintList.get(0));
                BuggieTracks.this.footPrintList.remove(0);
            }
        }), (Action)Actions.removeActor()));
    }
}

