/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.DamageCallback;

public class BaseDamageCallback
implements DamageCallback {
    public BaseModule baseModule;

    public BaseDamageCallback(BaseModule b) {
        this.baseModule = b;
    }

    @Override
    public void damageResult(float healthLeft) {
    }

    @Override
    public void destroyed() {
    }

    @Override
    public void normalDamage() {
    }

    @Override
    public void moderateDamage() {
    }

    @Override
    public void criticalDamage() {
    }

    @Override
    public void repaired() {
    }
}

