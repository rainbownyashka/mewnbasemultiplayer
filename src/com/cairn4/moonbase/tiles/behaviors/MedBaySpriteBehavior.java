/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class MedBaySpriteBehavior
implements Behavior {
    public BaseModule baseModule;
    public boolean loaded = false;
    private static final int NUM_SPRITES = 7;
    private int frameIndex;
    private float timer;
    private static final float DELAY = 0.1f;
    public Image pulseImage;
    public Skin gameScreeSkin;

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public void setId(String s) {
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void start() {
        this.frameIndex = 0;
        this.timer = 0.0f;
    }

    @Override
    public void update(float delta) {
        if (this.baseModule.hasPower) {
            this.timer += delta;
            if (this.timer >= 0.1f) {
                this.timer = 0.0f;
                this.changeFrame();
            }
        }
    }

    private void changeFrame() {
        ++this.frameIndex;
        if (this.frameIndex >= 7) {
            this.frameIndex = 0;
        }
        if (this.pulseImage != null) {
            this.pulseImage.setDrawable(this.gameScreeSkin.getDrawable("medbay-pulse-" + this.frameIndex));
        }
    }

    @Override
    public void interact(Player player) {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public BehaviorData getData() {
        return null;
    }
}

