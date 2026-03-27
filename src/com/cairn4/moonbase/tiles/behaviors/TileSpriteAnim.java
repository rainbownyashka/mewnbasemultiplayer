/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;
import java.util.ArrayList;

public class TileSpriteAnim
implements Behavior {
    public boolean loaded;
    public Skin skinRef;
    public Image image;
    public ArrayList<String> spriteList = new ArrayList();
    public float fps = 20.0f;
    private float fpsTimer = 0.0f;
    private int frameIndex = 0;
    private boolean playing = false;

    public void play() {
        this.playing = true;
    }

    public void stop() {
        this.playing = false;
    }

    @Override
    public void setLoaded(boolean loaded) {
        loaded = true;
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
    }

    @Override
    public void update(float delta) {
        if (this.playing) {
            this.fpsTimer += delta;
            if (this.fpsTimer > 1.0f / this.fps) {
                this.fpsTimer = 0.0f;
                this.nextFrame();
            }
        }
    }

    private void nextFrame() {
        ++this.frameIndex;
        if (this.frameIndex >= this.spriteList.size() - 1) {
            this.frameIndex = 0;
        }
        if (this.image != null) {
            this.image.setDrawable(this.skinRef.getDrawable(this.spriteList.get(this.frameIndex)));
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

