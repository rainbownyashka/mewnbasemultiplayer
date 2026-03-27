/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.PlayerStatus;
import com.cairn4.moonbase.PlayerStatusEffect;
import com.cairn4.moonbase.worlddata.StatusEffectData;

public class SpeedBoostStatusEffect
extends PlayerStatusEffect {
    public float speedMultiplier = 2.0f;
    public PlayerStatus playerStatus;

    public SpeedBoostStatusEffect() {
        this.timeBased = true;
        this.iconDrawable = "status-suffocating";
        this.maxTime = 50.0f;
        this.time = 50.0f;
        this.iconDrawable = "status-speedboost";
    }

    @Override
    public StatusEffectData getData() {
        StatusEffectData sed = new StatusEffectData();
        sed.statusEffectClass = this.getClass().getName();
        sed.properties.put("speedMultiplier", Float.valueOf(this.speedMultiplier));
        sed.properties.put("timeBased", true);
        sed.properties.put("maxTime", Float.valueOf(this.maxTime));
        sed.properties.put("time", Float.valueOf(this.time));
        sed.properties.put("iconDrawable", this.iconDrawable);
        return sed;
    }

    @Override
    public float applyWalkSpeedModifier(float walkSpeedMultiplier) {
        return walkSpeedMultiplier * this.speedMultiplier;
    }
}

