/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.tiles.Tile;

public class Meteor
extends Entity {
    Group parentGroup;
    Group fallingGroup;
    ParticleActor particleFlames;
    ParticleActor particleLines;
    ParticleActor particleExplosion;
    Chunk chunk;
    int localX;
    int localY;
    Image shadow;
    float fallTime = 1.5f;

    public Meteor(World world, Group parentGroup, int worldX, int worldY) {
        super(world, (float)worldX * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)worldY * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
        this.parentGroup = parentGroup;
        this.setup();
    }

    public Meteor(World world, Group parentGroup, Chunk chunk, int x, int y) {
        super(world, (float)(chunk.chunkX * 10 + x) * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)(chunk.chunkY * 10 + y) * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
        this.world = world;
        this.chunk = chunk;
        this.localX = x;
        this.localY = y;
        this.parentGroup = parentGroup;
        this.setup();
    }

    private void setup() {
        this.group = new Group();
        this.group.setPosition(this.spawnPosX, this.spawnPosY);
        this.parentGroup.addActor(this.group);
        this.fallingGroup = new Group();
        this.group.addActor(this.fallingGroup);
        this.image = new Image(this.world.gameScreen.skin.getDrawable("meteor"));
        this.image.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.image.setOrigin(1);
        this.image.setPosition(0.0f, 0.0f, 1);
        this.fallingGroup.addActor(this.image);
        this.image.addAction(Actions.forever(Actions.sequence((Action)Actions.rotateBy(360.0f, 1.0f))));
        this.addParticles();
        this.fallingGroup.addAction(Actions.sequence((Action)Actions.moveTo(0.0f, 1000.0f), (Action)Actions.alpha(0.0f), (Action)Actions.parallel((Action)Actions.moveBy(0.0f, -1000.0f, this.fallTime), (Action)Actions.fadeIn(this.fallTime / 2.0f)), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                Meteor.this.impact();
            }
        })));
        this.shadow = new Image(this.world.gameScreen.skin.getDrawable("lander-shadow"));
        this.shadow.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE * 0.7f);
        this.shadow.setOrigin(1);
        this.shadow.setPosition(0.0f, 0.0f, 1);
        this.shadow.setColor(1.0f, 1.0f, 1.0f, 0.3f);
        this.group.addActor(this.shadow);
        this.shadow.addAction(Actions.sequence((Action)Actions.scaleTo(0.2f, 0.2f), (Action)Actions.alpha(0.0f), (Action)Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, this.fallTime), (Action)Actions.alpha(0.3f, this.fallTime))));
    }

    private void impact() {
        this.image.clearActions();
        this.image.addAction(Actions.parallel((Action)Actions.scaleTo(0.0f, 0.0f, 0.2f), (Action)Actions.fadeOut(0.2f), (Action)Actions.delay(0.5f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                Meteor.this.readyToRemove = true;
            }
        })));
        this.particleFlames.remove();
        this.particleLines.remove();
        this.shadow.addAction(Actions.parallel((Action)Actions.scaleTo(0.0f, 0.0f, 0.2f), (Action)Actions.fadeOut(0.2f)));
        this.particleExplosion = new ParticleActor(Gdx.files.internal("particles/meteor-explosion.p"), this.world.gameScreen.atlas, false);
        this.particleExplosion.pfx.start();
        this.fallingGroup.addActor(this.particleExplosion);
        this.particleExplosion.toFront();
        this.world.gameScreen.cameraShake.shake(10.0f, 60.0f, 0.25f);
    }

    private void addParticles() {
        this.particleFlames = new ParticleActor(Gdx.files.internal("particles/meteor-flames.p"), this.world.gameScreen.atlas, false);
        this.particleFlames.setPosition(0.0f, 10.0f);
        this.particleFlames.pfx.start();
        this.fallingGroup.addActor(this.particleFlames);
        this.particleFlames.toBack();
        this.particleLines = new ParticleActor(Gdx.files.internal("particles/meteor-lines.p"), this.world.gameScreen.atlas, false);
        this.particleLines.pfx.start();
        this.fallingGroup.addActor(this.particleLines);
        this.particleLines.toFront();
    }
}

