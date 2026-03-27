/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.PlayerStatus;
import com.cairn4.moonbase.PlayerStatusEffect;

public class SuffocationStatusEffect
extends PlayerStatusEffect {
    public PlayerStatus playerStatus;

    public SuffocationStatusEffect() {
        this.timeBased = false;
        this.iconDrawable = "status-suffocating";
        this.showWarning = true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.playerStatus != null && this.playerStatus.getAir() > 0.0f) {
            this.readyToRemove = true;
        }
    }
}

