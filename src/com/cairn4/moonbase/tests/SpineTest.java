/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tests;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;

public class SpineTest
extends Game {
    OrthographicCamera camera;
    SpriteBatch batch;
    SkeletonRenderer renderer;
    SkeletonRendererDebug debugRenderer;
    TextureAtlas atlas;
    Skeleton skeleton;
    AnimationState state;

    @Override
    public void create() {
        this.camera = new OrthographicCamera();
        this.batch = new SpriteBatch();
        this.renderer = new SkeletonRenderer();
        this.renderer.setPremultipliedAlpha(true);
        this.debugRenderer = new SkeletonRendererDebug();
        this.debugRenderer.setBoundingBoxes(false);
        this.debugRenderer.setRegionAttachments(false);
        this.atlas = new TextureAtlas(Gdx.files.internal("player/skeleton.atlas"));
        SkeletonJson json = new SkeletonJson(this.atlas);
        json.setScale(0.6f);
        SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal("player/skeleton.json"));
        this.skeleton = new Skeleton(skeletonData);
        this.skeleton.setPosition(250.0f, 20.0f);
        AnimationStateData stateData = new AnimationStateData(skeletonData);
        stateData.setMix("idle", "walk_right", 0.2f);
        stateData.setMix("walk_right", "idle", 0.2f);
        this.state = new AnimationState(stateData);
        this.state.setTimeScale(1.0f);
        this.state.addAnimation(0, "walk_right", true, 0.0f);
    }

    @Override
    public void render() {
        this.state.update(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClear(16384);
        this.state.apply(this.skeleton);
        this.skeleton.updateWorldTransform();
        this.camera.update();
        this.batch.getProjectionMatrix().set(this.camera.combined);
        this.debugRenderer.getShapeRenderer().setProjectionMatrix(this.camera.combined);
        this.batch.begin();
        this.renderer.draw(this.batch, this.skeleton);
        this.batch.end();
        this.debugRenderer.draw(this.skeleton);
    }

    @Override
    public void resize(int width, int height) {
        this.camera.setToOrtho(false);
    }

    @Override
    public void dispose() {
        this.atlas.dispose();
    }
}

