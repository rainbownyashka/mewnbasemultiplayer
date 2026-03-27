/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.BaseResourceStorageBehavior;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class ResourceMeterBehavior
implements Behavior {
    public boolean loaded = false;
    public BaseModule baseModule;
    public Image meterSprite;
    public BaseResourceStorageBehavior resourceStorageBehavior;
    public boolean vertical;

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
    }

    @Override
    public void update(float delta) {
        if (this.meterSprite != null) {
            if (this.resourceStorageBehavior != null) {
                this.meterSprite.setVisible(true);
                float value = this.resourceStorageBehavior.amount / this.resourceStorageBehavior.capacity;
                if (this.vertical) {
                    this.meterSprite.setScaleY(value);
                } else {
                    this.meterSprite.setScaleX(value);
                }
            } else {
                this.meterSprite.setVisible(false);
            }
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

