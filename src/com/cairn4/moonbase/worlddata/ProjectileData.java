/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.worlddata.DamageDef;
import java.util.ArrayList;

public class ProjectileData {
    public String id;
    public boolean bullet;
    public String drawable;
    public float imageWidth;
    public float imageHeight;
    public String bulletParticles;
    public float bulletParticleScale;
    public float velocity;
    public float radius;
    public float rotationSpeed;
    public boolean alignToVelocity;
    public boolean attachLight;
    public String lightHexColor;
    public float lightRadius;
    public float lightDecayLength;
    public float decayLength;
    public float arcMultiplier;
    public ArrayList<String> impactDecals = new ArrayList();
    public float impactDecalScale;
    public String impactParticles;
    public float impactParticleScale;
    public DamageDef damageDef;

    public void loadParticles(ParticleEffectLoader.ParticleEffectParameter pel) {
        if (this.bulletParticles != null && !this.bulletParticles.equals("")) {
            AssetManagerSingleton.getInstance().load(this.bulletParticles, ParticleEffect.class, pel);
        }
        if (this.impactParticles != null && !this.impactParticles.equals("")) {
            AssetManagerSingleton.getInstance().load(this.impactParticles, ParticleEffect.class, pel);
        }
    }

    public void loadSoundFx() {
    }
}

