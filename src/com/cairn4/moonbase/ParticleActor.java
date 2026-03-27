/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ParticleActor
extends Actor {
    public ParticleEffect pfx;
    private ParticleEffectPool.PooledEffect pooledFx;
    private ParticleEffectPool pfxPool;
    private boolean removeWhenComplete;

    public ParticleActor(ParticleEffect pfx, boolean remove) {
        this.pfx = pfx;
        this.removeWhenComplete = remove;
        if (this.removeWhenComplete) {
            pfx.allowCompletion();
        }
        this.pfxPool = null;
    }

    public ParticleActor(FileHandle particlePath, TextureAtlas textureAtlas, Boolean remove) {
        this.pfx = new ParticleEffect();
        this.pfx.load(particlePath, textureAtlas);
        this.removeWhenComplete = remove;
        if (this.removeWhenComplete) {
            this.pfx.allowCompletion();
        }
        this.pfxPool = null;
    }

    public ParticleActor(FileHandle particlePath, TextureAtlas textureAtlas, Vector2 position, Boolean remove, ParticleEffectPool pool) {
        this.pfx = new ParticleEffect();
        this.pfx.load(particlePath, textureAtlas);
        this.setPosition(position.x, position.y);
        this.removeWhenComplete = remove;
        if (this.removeWhenComplete) {
            this.pfx.allowCompletion();
        }
        this.pfxPool = pool;
    }

    @Override
    public void setScale(float scale) {
        this.pfx.scaleEffect(scale);
    }

    @Override
    public void act(float delta) {
        if (this.pooledFx != null) {
            if (this.pooledFx.isComplete()) {
                this.pooledFx.free();
                this.remove();
            } else {
                this.pooledFx.setPosition(this.getX(), this.getY());
            }
        }
        if (this.pfx != null) {
            if (this.removeWhenComplete && this.pfx.isComplete()) {
                this.remove();
            } else {
                this.pfx.setPosition(this.getX(), this.getY());
            }
        }
        super.act(delta);
    }

    @Override
    public void draw(Batch spriteBatch, float delta) {
        if (this.isVisible()) {
            if (this.pfx != null) {
                this.pfx.draw(spriteBatch, Gdx.graphics.getDeltaTime());
            } else {
                this.pooledFx.draw(spriteBatch, Gdx.graphics.getDeltaTime());
            }
        }
    }
}

