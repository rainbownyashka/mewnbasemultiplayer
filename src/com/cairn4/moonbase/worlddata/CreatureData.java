/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.worlddata.CreatureAttackData;
import com.cairn4.moonbase.worlddata.SoundDef;
import com.cairn4.moonbase.worlddata.TileInteractionAction;
import java.util.ArrayList;

public class CreatureData {
    public String id;
    public boolean enabled;
    public String spineActor;
    public String spineSkin;
    public float spineBaseScale;
    public float maxHealth;
    public float walkAcceleration;
    public SoundDef footStepSoundFx;
    public float maxWalkSpeedTilesPerSec;
    public float lightRadius;
    public String lightColor;
    public float physicsRadius;
    public float hitRadius;
    public boolean aggressive;
    public boolean aggressiveIfAttacked;
    public int maxPatrolTileDistanceFromSpawn = 10;
    public float searchTimerDelay;
    public float aggroDistance;
    public float giveUpDistance;
    public SoundDef aggroSoundFx;
    public SoundDef deathSoundFx;
    public String deathParticles;
    public ArrayList<String> spawnBiomes = new ArrayList();
    public int minSpawnTileDistFromOrigin = 15;
    public int minTileDistFromOtherCreatures = 15;
    public ArrayList<CreatureAttackData> attackDataList = new ArrayList();
    public Array<TileInteractionAction> itemDrops = new Array();
    public float fireDamageArmor = 1.0f;

    public void loadParticles(ParticleEffectLoader.ParticleEffectParameter pel) {
        if (this.deathParticles != null && !this.deathParticles.equals("")) {
            AssetManagerSingleton.getInstance().load(this.deathParticles, ParticleEffect.class, pel);
        }
        for (CreatureAttackData cad : this.attackDataList) {
            cad.loadParticles(pel);
        }
    }

    public void loadSoundFx() {
        if (this.footStepSoundFx != null) {
            AssetManagerSingleton.getInstance().load(this.footStepSoundFx.path, Sound.class);
        }
        if (this.aggroSoundFx != null) {
            AssetManagerSingleton.getInstance().load(this.aggroSoundFx.path, Sound.class);
        }
        if (this.deathSoundFx != null) {
            AssetManagerSingleton.getInstance().load(this.deathSoundFx.path, Sound.class);
        }
        for (CreatureAttackData cad : this.attackDataList) {
            cad.loadSoundFx();
        }
    }
}

