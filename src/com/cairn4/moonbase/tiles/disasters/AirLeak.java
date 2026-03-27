/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.disasters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.disasters.BaseDisaster;

public class AirLeak
extends BaseDisaster {
    ParticleActor leakParticles;
    Sound sound;
    Long leakSound;
    float volume = 0.0f;
    float pan = 0.0f;
    public static float OXYGEN_RATE = 3.0f;

    public AirLeak(BaseModule base) {
        super(base);
        this.originalColor = this.base.getColor().cpy();
        this.base.setColor(1.0f, 0.0f, 0.0f, 1.0f);
        if (this.base.light != null) {
            this.originalLightColor = this.base.light.getColor().cpy();
            this.base.light.setColor(1.0f, 0.0f, 0.0f, this.originalLightColor.a);
        }
        this.leakParticles = new ParticleActor(new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/airleak.p", ParticleEffect.class)), true);
        this.leakParticles.setPosition(base.getXCenter(), base.getYCenter());
        this.leakParticles.pfx.start();
        base.world.gameScreen.topGroup.addActor(this.leakParticles);
        this.sound = Audio.getInstance().getSound("sounds/airleak.ogg");
        this.leakSound = Audio.getInstance().playSoundLoop(this.sound, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.base.setOxygenDrawRate(OXYGEN_RATE);
        this.volume = Audio.getInstance().playerDistanceMultiplier(this.base.world, new Vector2(this.base.getXCenter(), this.base.getYCenter()), 1200.0f, 0.22f);
        this.pan = Audio.getInstance().playerDistancePan(this.base.world, new Vector2(this.base.getXCenter(), this.base.getYCenter()));
        if (this.volume <= 0.0f) {
            this.sound.pause(this.leakSound);
        } else {
            this.sound.resume(this.leakSound);
            Audio.getInstance().updateLoopingSoundVolume(this.sound, this.leakSound, this.volume, this.pan);
        }
    }

    @Override
    public void fix() {
        this.base.resetOxygenDrawRate();
        this.base.setColor(this.originalColor.r, this.originalColor.g, this.originalColor.b, this.originalColor.a);
        if (this.base.light != null) {
            this.base.light.setColor(this.originalLightColor.r, this.originalLightColor.g, this.originalLightColor.b, this.originalLightColor.a);
        }
        this.leakParticles.pfx.allowCompletion();
        MoonBase.achievementAdapter.repairAirLeak();
        super.fix();
    }

    @Override
    public void dispose() {
        this.leakParticles.pfx.allowCompletion();
        this.sound.stop(this.leakSound);
        Gdx.app.log("MewnBase", "Stop airleak sound!");
        super.dispose();
    }
}

