/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine.utils;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonRenderer;

public class SkeletonDrawable
extends BaseDrawable {
    private SkeletonRenderer renderer;
    private Skeleton skeleton;
    AnimationState state;
    private boolean resetBlendFunction = true;

    public SkeletonDrawable() {
    }

    public SkeletonDrawable(SkeletonRenderer renderer, Skeleton skeleton, AnimationState state) {
        this.renderer = renderer;
        this.skeleton = skeleton;
        this.state = state;
    }

    public void update(float delta) {
        this.state.update(delta);
        this.state.apply(this.skeleton);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        int blendSrc = batch.getBlendSrcFunc();
        int blendDst = batch.getBlendDstFunc();
        int blendSrcAlpha = batch.getBlendSrcFuncAlpha();
        int blendDstAlpha = batch.getBlendDstFuncAlpha();
        this.skeleton.setPosition(x, y);
        this.skeleton.updateWorldTransform();
        this.renderer.draw(batch, this.skeleton);
        if (this.resetBlendFunction) {
            batch.setBlendFunctionSeparate(blendSrc, blendDst, blendSrcAlpha, blendDstAlpha);
        }
    }

    public SkeletonRenderer getRenderer() {
        return this.renderer;
    }

    public void setRenderer(SkeletonRenderer renderer) {
        this.renderer = renderer;
    }

    public Skeleton getSkeleton() {
        return this.skeleton;
    }

    public void setSkeleton(Skeleton skeleton) {
        this.skeleton = skeleton;
    }

    public AnimationState getAnimationState() {
        return this.state;
    }

    public void setAnimationState(AnimationState state) {
        this.state = state;
    }

    public boolean getResetBlendFunction() {
        return this.resetBlendFunction;
    }

    public void setResetBlendFunction(boolean resetBlendFunction) {
        this.resetBlendFunction = resetBlendFunction;
    }
}

