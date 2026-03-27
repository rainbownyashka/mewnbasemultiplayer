/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.worlddata.SoundDef;

public class CreatureAttackData {
    public String id;
    public AttackTypes attackType;
    public float chance;
    public String projectileSpawnBone;
    public boolean showTargetIndicator;
    public String preAttackAnim;
    public String attackAnim;
    public int minDamage;
    public int maxDamage;
    public float knockback;
    public float attackWindup;
    public String attackWindupParticles;
    public float attackDuration;
    public float attackCooldown;
    public float minDistanceFromPlayer;
    public float maxDistanceFromPlayer;
    public float maxLurchDistance;
    public float attackRadius;
    public SoundDef soundPreAttack;
    public SoundDef soundStart;
    public SoundDef soundMiss;
    public SoundDef soundHit;
    public String hitParticles;
    public float maxProjectileDistance;
    public int projectileSeq;
    public float projectileSeqDelay;
    public int projectileShots;
    public float projectileArc;
    public String projectileDataId;

    public void loadParticles(ParticleEffectLoader.ParticleEffectParameter pel) {
        if (this.attackWindupParticles != null) {
            AssetManagerSingleton.getInstance().load(this.attackWindupParticles, ParticleEffect.class, pel);
        }
        if (this.hitParticles != null) {
            AssetManagerSingleton.getInstance().load(this.hitParticles, ParticleEffect.class, pel);
        }
    }

    public void loadSoundFx() {
        if (this.soundPreAttack != null) {
            AssetManagerSingleton.getInstance().load(this.soundPreAttack.path, Sound.class);
        }
        if (this.soundStart != null) {
            AssetManagerSingleton.getInstance().load(this.soundStart.path, Sound.class);
        }
        if (this.soundMiss != null) {
            AssetManagerSingleton.getInstance().load(this.soundMiss.path, Sound.class);
        }
        if (this.soundHit != null) {
            AssetManagerSingleton.getInstance().load(this.soundHit.path, Sound.class);
        }
    }

    public static enum AttackTypes {
        melee,
        aoe,
        projectile;

    }
}

