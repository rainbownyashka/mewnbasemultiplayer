/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.creatures.SpineActorDraw;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;

public class SpineActor
extends Widget {
    SkeletonRenderer renderer;
    TextureAtlas atlas;
    public Skeleton skeleton;
    public SkeletonData skeletonData;
    public AnimationState state;
    float baseScale;
    public AnimationStateData stateData;
    private Color calculatedColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    public SpineActorDraw spineActorDraw;
    public float shaderBlend;

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        this.skeleton.setPosition(x, y);
    }

    public SpineActor(String spinePath, boolean useAssetManager, float baseScale, SkeletonRenderer renderer) {
        this.baseScale = baseScale;
        this.setupRenderer();
        this.atlas = AssetManagerSingleton.getInstance().get(spinePath + "/skeleton.atlas", TextureAtlas.class);
        SkeletonJson json = new SkeletonJson(this.atlas);
        json.setScale(this.baseScale);
        this.skeletonData = json.readSkeletonData(Gdx.files.internal(spinePath + "/skeleton.json"));
        this.skeleton = new Skeleton(this.skeletonData);
        this.skeleton.setPosition(0.0f, 0.0f);
        this.stateData = new AnimationStateData(this.skeletonData);
        this.state = new AnimationState(this.stateData);
        this.state.getData().setDefaultMix(0.5f);
    }

    public SpineActor(String spinePath, float baseScale, SkeletonRenderer renderer) {
        this.baseScale = baseScale;
        this.setupRenderer();
        this.atlas = new TextureAtlas(Gdx.files.internal(spinePath + "/skeleton.atlas"));
        SkeletonJson json = new SkeletonJson(this.atlas);
        json.setScale(this.baseScale);
        this.skeletonData = json.readSkeletonData(Gdx.files.internal(spinePath + "/skeleton.json"));
        this.skeleton = new Skeleton(this.skeletonData);
        this.skeleton.setPosition(0.0f, 0.0f);
        this.stateData = new AnimationStateData(this.skeletonData);
        this.stateData.setDefaultMix(0.0f);
        this.state = new AnimationState(this.stateData);
    }

    private void setupRenderer() {
        if (this.renderer == null) {
            this.renderer = new SkeletonRenderer();
            this.renderer.setPremultipliedAlpha(false);
        } else {
            this.renderer = this.renderer;
        }
    }

    public void setFlip(boolean x, boolean y) {
        float sX = x ? -1.0f : 1.0f;
        float sY = y ? -1.0f : 1.0f;
        this.skeleton.setScale(sX, sY);
    }

    public void setSlotAttachment(String slotName, String image) {
        Slot slot = this.skeleton.findSlot(slotName);
        if (image != null) {
            Attachment a = this.skeleton.getAttachment(slotName, image);
            slot.setAttachment(a);
        } else {
            slot.setAttachment(null);
        }
    }

    public void setMix(String anim1, String anim2, float time) {
        this.stateData.setMix(anim1, anim2, time);
    }

    public void setAnimation(int track, String name, boolean loop) {
        this.state.setAnimation(track, name, loop);
    }

    public void addAnimation(int track, String name, boolean loop, float delay) {
        this.state.addAnimation(track, name, loop, delay);
    }

    public void update(float delta) {
        this.state.update(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        this.state.apply(this.skeleton);
        Color color = this.getColor();
        this.calculatedColor.set(color.r, color.g, color.b, color.a * parentAlpha);
        this.skeleton.setColor(this.calculatedColor);
        this.skeleton.updateWorldTransform();
        batch.getProjectionMatrix().set(batch.getProjectionMatrix());
        if (this.spineActorDraw != null) {
            this.spineActorDraw.shaderStart(batch, this.shaderBlend);
        }
        if (this.renderer != null) {
            this.renderer.draw(batch, this.skeleton);
        }
        batch.setBlendFunction(770, 771);
        if (this.spineActorDraw != null) {
            this.spineActorDraw.shaderEnd(batch);
        }
    }
}

