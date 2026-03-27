/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.PlayerStatus;
import com.cairn4.moonbase.PlayerStatusEffect;

public class HeatStatusEffect
extends PlayerStatusEffect {
    public PlayerStatus playerStatus;

    public HeatStatusEffect() {
        this.timeBased = false;
        this.iconDrawable = "status-heat";
        this.showWarning = true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.playerStatus != null && this.playerStatus.getTemperature() <= 1.0f) {
            this.readyToRemove = true;
        }
    }
}

