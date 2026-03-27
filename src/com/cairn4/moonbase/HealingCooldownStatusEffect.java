/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.PlayerStatusEffect;
import com.cairn4.moonbase.worlddata.StatusEffectData;

public class HealingCooldownStatusEffect
extends PlayerStatusEffect {
    public HealingCooldownStatusEffect() {
        this.timeBased = true;
        this.maxTime = 50.0f;
        this.time = 50.0f;
        this.iconDrawable = "status-healing";
    }

    @Override
    public StatusEffectData getData() {
        StatusEffectData sed = new StatusEffectData();
        sed.statusEffectClass = this.getClass().getName();
        sed.properties.put("timeBased", true);
        sed.properties.put("maxTime", Float.valueOf(this.maxTime));
        sed.properties.put("time", Float.valueOf(this.time));
        sed.properties.put("iconDrawable", this.iconDrawable);
        return sed;
    }
}

