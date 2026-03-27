/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Pool;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.GameScreen;
import java.util.ArrayList;

public class Footprints {
    protected GameScreen gameScreen;
    private Player player;
    Vector2 playerPos;
    float playerDirection;
    Vector2 lastPrint;
    int lastPrintSide = -1;
    float stepDistance = Tile.GRID_SIZE / 5.0f;
    float minStepDistance = Tile.GRID_SIZE / 8.0f;
    private final Vector2 offset = new Vector2(0.0f, -24.0f);
    private String side;
    ArrayList<Image> footPrintList = new ArrayList();
    private float velocityMul;
    private final Pool<Image> footPrintPool = new Pool<Image>(){

        @Override
        protected Image newObject() {
            return new Image();
        }
    };
    private static final float dustLifespan = 0.8f;

    public Footprints(GameScreen gameScreen, Player player) {
        this.gameScreen = gameScreen;
        this.player = player;
        this.playerPos = new Vector2(player.getXPos(), player.getYPos());
        this.lastPrint = new Vector2(player.getXPos(), player.getYPos());
    }

    public void update(Vector2 velocity) {
        this.playerDirection = velocity.angle();
        this.playerPos.set(this.player.getXPos() + this.offset.x, this.player.getYPos() + this.offset.y);
        this.velocityMul = velocity.len() / Player.MAX_WALK_SPEED;
        if (this.playerPos.dst(this.lastPrint) >= this.stepDistance * this.velocityMul + this.minStepDistance) {
            this.addStep();
            this.lastPrint.set(this.player.getXPos() + this.offset.x, this.player.getYPos() + this.offset.y);
            this.lastPrintSide *= -1;
        }
    }

    public void addStep() {
        this.side = this.lastPrintSide > 0 ? "right" : "left";
        Image i = this.footPrintPool.obtain();
        i.setDrawable(this.gameScreen.skin.getDrawable("footprint-" + this.side));
        i.setSize(Tile.TILE_SIZE / 4.0f * Tile.SCALE, Tile.TILE_SIZE / 4.0f);
        i.setOrigin(1);
        i.setPosition(this.playerPos.x, this.playerPos.y, 1);
        i.setRotation(this.playerDirection);
        i.setColor(1.0f, 1.0f, 1.0f, 0.25f);
        this.gameScreen.bgGroup.addActor(i);
        this.footPrintList.add(i);
        i.clearActions();
        i.addAction(Actions.sequence((Action)Actions.delay(2.0f), (Action)Actions.fadeOut(3.0f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                if (!Footprints.this.footPrintList.isEmpty() && Footprints.this.footPrintList.get(0) != null) {
                    Footprints.this.footPrintPool.free(Footprints.this.footPrintList.get(0));
                    Footprints.this.footPrintList.remove(0);
                }
            }
        }), (Action)Actions.removeActor()));
        this.addDustPoof();
    }

    private void addDustPoof() {
        Image i = this.footPrintPool.obtain();
        i.setDrawable(this.gameScreen.skin.getDrawable("dust"));
        float size = MathUtils.random(60, 80);
        float startRotation = MathUtils.random(-180, 180);
        i.setSize(size, size);
        i.setOrigin(1);
        i.setPosition(this.playerPos.x, this.playerPos.y, 1);
        i.setRotation(startRotation);
        i.setColor(Color.valueOf("decdad00"));
        this.gameScreen.floorGroup.addActor(i);
        this.footPrintList.add(i);
        float rotationDir = MathUtils.randomBoolean() ? -1.0f : 1.0f;
        i.clearActions();
        i.addAction(Actions.sequence((Action)Actions.scaleTo(0.8f, 0.8f), (Action)Actions.parallel((Action)Actions.sequence((Action)Actions.alpha(0.3f, 0.24000001f), (Action)Actions.alpha(0.0f, 0.56f)), (Action)Actions.rotateBy(50.0f * rotationDir, 0.8f), (Action)Actions.scaleTo(1.2f, 1.2f, 0.8f, Interpolation.sineOut), (Action)Actions.moveBy(0.0f, 15.0f, 0.8f, Interpolation.sine)), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                Footprints.this.footPrintPool.free(Footprints.this.footPrintList.get(0));
                Footprints.this.footPrintList.remove(0);
            }
        }), (Action)Actions.removeActor()));
    }
}

