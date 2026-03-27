/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.worlddata.WeatherData;

public class WeatherParticleFx {
    Chunk chunk;
    Vector2 centerPos;
    ParticleActor currentFx;
    ParticleActor nextFx;

    public WeatherParticleFx(Chunk chunk) {
        this.chunk = chunk;
        float x = (float)(this.chunk.chunkX * 10) * Tile.TILE_SIZE + 5.0f * Tile.TILE_SIZE;
        float y = (float)(this.chunk.chunkY * 10) * Tile.TILE_SIZE + 5.0f * Tile.TILE_SIZE;
        this.centerPos = new Vector2(x, y);
        this.changeWeather(chunk.world.weatherManager.getCurrentData());
    }

    public void setVisible(boolean visible) {
        if (this.currentFx != null) {
            if (visible) {
                this.currentFx.pfx.start();
            } else {
                this.currentFx.pfx.allowCompletion();
            }
        }
    }

    private void allowFinish() {
        if (this.currentFx != null) {
            this.currentFx.pfx.allowCompletion();
        }
    }

    public void changeWeather(WeatherData weatherData) {
        this.allowFinish();
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get(weatherData.particleFx, ParticleEffect.class));
        p.reset();
        this.nextFx = new ParticleActor(p, false);
        this.nextFx.setPosition(this.centerPos.x, this.centerPos.y);
        if (this.nextFx != null) {
            this.nextFx.pfx.start();
            this.chunk.world.gameScreen.topGroup.addActor(this.nextFx);
            this.currentFx = this.nextFx;
        }
    }
}

