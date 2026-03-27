/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.tiles.Tile;
import java.io.Reader;

public class BtreeBlob
extends Entity {
    private Label debugLabel;
    private Image shadow;
    private BehaviorTree<BtreeBlob> behaviorTree;
    private float updateTimer = 0.0f;
    private float updateDelay = 1.0f;
    private Vector2 currentPos;
    private Vector2 targetPos;
    private Player targetPlayer;

    public BtreeBlob(World world, float xPos, float yPos, float rotation) {
        this(world, xPos, yPos);
    }

    public BtreeBlob(World world, float xPos, float yPos) {
        super(world, xPos, yPos);
        this.createDrawable("zombie1");
        Reader reader = null;
        reader = Gdx.files.local(MoonBase.coreFolder + "creature.tree").reader();
        BehaviorTreeParser<Object> parser = new BehaviorTreeParser<Object>(1);
        this.behaviorTree = parser.parse(reader, null);
        this.behaviorTree.setObject(this);
        this.behaviorTree.run();
    }

    private void createDrawable(String sprite) {
        this.group = new Group();
        this.group.setSize(60.0f, 90.0f);
        this.group.setDebug(false);
        this.group.setUserObject(Float.valueOf(this.getYPos()));
        this.group.setPosition(this.spawnPosX, this.spawnPosY, 4);
        this.world.gameScreen.mainGroup.addActor(this.group);
        this.group.addListener(new ClickListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                BtreeBlob.this.setClicked();
                return true;
            }
        });
        this.shadow = new Image(this.world.gameScreen.skin.getDrawable("lander-shadow"));
        this.world.gameScreen.floorGroup.addActor(this.shadow);
        this.shadow.setColor(0.0f, 0.0f, 0.0f, 0.3f);
        this.shadow.setSize(Tile.TILE_SIZE * 0.4f, Tile.TILE_SIZE * 0.2f);
        this.shadow.setPosition(this.group.getX(), this.group.getY() - 30.0f, 1);
        this.image = new Image(this.world.gameScreen.skin.getDrawable(sprite));
        this.group.addActor(this.image);
        this.image.setSize(80.0f, 80.0f);
        this.image.setOrigin(1);
        this.image.setPosition(0.0f, 0.0f, 1);
        this.debugLabel = new Label((CharSequence)"", this.world.gameScreen.labelStyle);
        this.debugLabel.setFontScale(0.4f);
        this.debugLabel.setPosition(0.0f, 100.0f);
        this.group.addActor(this.debugLabel);
    }

    public void shout() {
        Audio.getInstance().playSound("sounds/interact_hit2.ogg", 0.7f, 1.0f);
        this.image.clearActions();
        this.image.addAction(Actions.sequence((Action)Actions.scaleTo(2.0f, 2.0f), (Action)Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, 1.0f))));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.behaviorTree != null) {
            this.behaviorTree.step();
        }
    }

    public boolean findTarget() {
        if (this.targetPos == null) {
            this.targetPos = new Vector2(0.0f, 0.0f);
        }
        if (this.currentPos == null) {
            this.currentPos = new Vector2(0.0f, 0.0f);
        }
        if (this.world.player != null) {
            this.targetPos.set(this.world.player.getXPos(), this.world.player.getYPos());
            this.currentPos.set(this.getXPos(), this.getYPos());
            float diff = this.targetPos.cpy().sub(this.currentPos).len();
            if (diff < 3.0f * Tile.TILE_SIZE) {
                this.targetPlayer = this.world.player;
                this.image.setColor(Color.RED);
                return true;
            }
        }
        this.image.setColor(Color.WHITE);
        return false;
    }

    public void btreeMessage(String message) {
        Gdx.app.log("MewnBase", message);
        this.debugLabel.setText(message);
    }
}

