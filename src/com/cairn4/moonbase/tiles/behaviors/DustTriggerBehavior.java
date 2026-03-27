/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.SolarPanel;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.disasters.DirtySolarPanel;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class DustTriggerBehavior
implements Behavior {
    public boolean loaded = false;
    public SolarPanel solarPanel;
    public float dustAmount;
    public float limit = 90.0f;

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
        this.dustAmount = 0.0f;
        this.limit = MathUtils.random(this.limit - 5.0f, this.limit + 5.0f);
    }

    @Override
    public void update(float delta) {
        if (this.solarPanel != null) {
            this.dustAmount += this.solarPanel.world.weatherManager.getDustRate() * delta;
            if (this.dustAmount < 0.0f) {
                this.dustAmount = 0.0f;
            }
            if (this.solarPanel.baseDisaster == null) {
                if (this.dustAmount >= this.limit) {
                    this.solarPanel.triggerDisaster(new DirtySolarPanel(this.solarPanel));
                }
            } else if (this.solarPanel.baseDisaster instanceof DirtySolarPanel && this.dustAmount < this.limit / 2.0f) {
                this.solarPanel.baseDisaster.fix();
                this.dustAmount = 0.0f;
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
        BehaviorData bd = new BehaviorData();
        bd.behaviorClass = this.getClass().getName();
        bd.properties.put("dustAmount", Float.valueOf(this.dustAmount));
        bd.properties.put("limit", Float.valueOf(this.limit));
        return bd;
    }
}

