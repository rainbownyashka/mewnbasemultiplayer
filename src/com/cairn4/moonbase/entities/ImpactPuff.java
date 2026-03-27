/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;

public class ImpactPuff
extends Entity {
    private static float duration = 0.5f;
    private float size;

    public ImpactPuff(World world, float xPos, float yPos, float size) {
        super(world, xPos, yPos);
        this.size = size;
        this.createDrawable("impact-puff");
        this.image.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.5f, 0.5f), (Action)Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, duration, Interpolation.exp5Out), (Action)Actions.sequence((Action)Actions.alpha(0.5f, duration * 0.2f), (Action)Actions.fadeOut(duration * 0.8f))), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                ImpactPuff.this.readyToRemove = true;
            }
        })));
    }

    private void createDrawable(String sprite) {
        this.group = new Group();
        this.group.setPosition(this.spawnPosX, this.spawnPosY);
        this.world.gameScreen.mainGroup.addActor(this.group);
        try {
            this.image = new Image(this.world.gameScreen.skin.getDrawable(sprite));
        }
        catch (GdxRuntimeException e) {
            Gdx.app.error("MewnBase", "Missing drawable: " + sprite);
            this.image = new Image(this.world.gameScreen.skin.getDrawable("missing"));
        }
        this.image.setSize(this.size, this.size);
        this.image.setColor(Color.valueOf("aca07f"));
        this.image.setOrigin(1);
        this.image.setPosition(0.0f, 0.0f, 1);
        this.group.addActor(this.image);
    }

    @Override
    public void remove() {
        this.group.remove();
    }
}

