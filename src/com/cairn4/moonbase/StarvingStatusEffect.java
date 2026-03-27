/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.PlayerStatus;
import com.cairn4.moonbase.PlayerStatusEffect;

public class StarvingStatusEffect
extends PlayerStatusEffect {
    public PlayerStatus playerStatus;

    public StarvingStatusEffect() {
        this.timeBased = false;
        this.iconDrawable = "status-starving";
        this.showWarning = true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.playerStatus != null && this.playerStatus.hungerState != PlayerStatus.HungerStates.starving) {
            this.readyToRemove = true;
        }
    }
}

