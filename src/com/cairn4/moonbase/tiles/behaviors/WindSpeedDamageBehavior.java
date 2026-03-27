/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.WindDamageCallback;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class WindSpeedDamageBehavior
implements Behavior {
    public boolean loaded = false;
    public BaseModule baseModule;
    public float windHealthMax;
    public float windHealth;
    public float dangerSpeed;
    private float damageSpeed;
    private boolean inDanger = false;
    public WindDamageCallback windDamageCallback;

    public WindSpeedDamageBehavior() {
        this.updateDamageSpeed();
    }

    private void updateDamageSpeed() {
        this.damageSpeed = MathUtils.random(0.6f, 1.05f);
    }

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
        this.updateDamageSpeed();
        this.windHealth = this.windHealthMax;
    }

    @Override
    public void update(float delta) {
        if (!this.baseModule.hasDisaster()) {
            float windSpeed = this.baseModule.world.weatherManager.getWindSpeed();
            if (windSpeed > this.dangerSpeed) {
                if (!this.inDanger) {
                    this.windDamageCallback.windSpeedDanger(true);
                    this.inDanger = true;
                }
                this.windHealth -= this.damageSpeed * delta;
                if (this.windHealth < 0.0f) {
                    this.windDamageCallback.damageBase();
                }
            } else if (this.inDanger) {
                this.windDamageCallback.windSpeedDanger(false);
                this.inDanger = false;
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

