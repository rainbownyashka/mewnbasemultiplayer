/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

public interface DamageCallback {
    public void damageResult(float var1);

    public void destroyed();

    public void normalDamage();

    public void moderateDamage();

    public void criticalDamage();

    public void repaired();
}

