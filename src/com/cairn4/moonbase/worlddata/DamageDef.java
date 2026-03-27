/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

public class DamageDef {
    public DamageTypes damageType;
    public float maxDamage;
    public boolean knockBack = false;
    public float knockBackMul = 1.0f;

    public static enum DamageTypes {
        physical,
        fire,
        chemical,
        electric,
        explosive;

    }
}

