/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Json;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.filters.Blur;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BloomLerp;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.MultiplyImage;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.LightningStrike;
import com.cairn4.moonbase.tiles.LightningCollector;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.worlddata.WeatherData;
import com.cairn4.moonbase.worlddata.WeatherDataList;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.math.sampling.Sampler;
import za.co.luma.math.sampling.UniformPoissonDiskSampler;

public class WeatherManager
extends Observable {
    private World world;
    private FileHandle fileHandle;
    private int maxConsecutiveWeather;
    private Mission.weatherModes weatherMode;
    private float rollStartingBase = 0.0f;
    private ArrayList<WeatherData> weatherDataList = new ArrayList();
    private WeatherData currentData;
    private WeatherData nextData;
    private float timer;
    private float delay;
    private boolean notifiedForNext;
    private static float warningTime = 2.0f;
    private Color lerpdColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    private Color outputColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    private Color ambientWeatherColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    private Color ambientTargetColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    private static final float transitionLength = 5.0f;
    private float transitionTimer;
    private boolean transitioningColor = false;
    public MultiplyImage weatherColorTint;
    public AdditiveImage lightningFlash;
    private Bloom bloom;
    private Bloom.Settings currentBloomSettings;
    private Bloom.Settings targetBloomSettings;
    private float solarEfficiency = 1.0f;
    private float prevSolarEfficiency = 1.0f;
    private float rainRate = 0.0f;
    private float prevRainRate = 0.0f;
    private float dustRate = 0.0f;
    private float prevDustRate = 0.0f;
    private float windSpeed = 0.0f;
    private float prevWindSpeed = -1.0f;
    private float targetWindSpeed = 0.0f;
    private float windChangeTimer = 0.0f;
    private float windChangeDelay = 20.0f;
    private float windChangeDelayBase = 20.0f;
    private float windTransitionLength = 5.0f;
    private float windTransitionTimer = 0.0f;
    private Sound prevSoundFx;
    private long prevSoundFxId;
    private Sound targetSoundFx;
    private long targetSoundFxId;
    private final float maxLightningDelay = 12.0f;
    private float lightningDelay = 10.0f;
    private float lightningTimer = 0.0f;
    private int numLightningSpots = 12;
    private static int lightningSpawnRadius = 6;
    Sampler<Vector2DDouble> lightningSampler;
    List<Vector2DDouble> lightningPointList;
    public ArrayList<GridPoint2> lightningGridPoints = new ArrayList();
    int consequtiveStreak = 1;
    private float chanceMultiplier = 1.0f;
    private float lightningDelayMultiplier = 1.0f;

    public WeatherManager(World world) {
        this.loadData();
        this.world = world;
        this.notifiedForNext = false;
        this.bloom = new Bloom(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        this.currentBloomSettings = new Bloom.Settings("initial", 1, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f);
        this.bloom.setSettings(this.currentBloomSettings);
        this.world.gameScreen.postProcessor.addEffect(this.bloom);
        this.currentData = this.weatherDataList.get(0);
        this.nextData = this.weatherDataList.get(0);
        this.setWeatherPostFx(this.nextData);
        this.changeWeather();
        this.weatherColorTint = new MultiplyImage(world.gameScreen.skin.getDrawable("white"));
        this.weatherColorTint.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        this.weatherColorTint.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.weatherColorTint.setTouchable(Touchable.disabled);
        world.gameScreen.hudStage.addActor(this.weatherColorTint);
        this.lightningFlash = new AdditiveImage(world.gameScreen.skin.getDrawable("white"));
        this.lightningFlash.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        this.lightningFlash.setColor(Color.valueOf("0084ff00"));
        this.lightningFlash.setTouchable(Touchable.disabled);
        world.gameScreen.hudStage.addActor(this.lightningFlash);
    }

    private void loadData() {
        this.fileHandle = Gdx.files.local(MoonBase.coreFolder + "weather.json");
        if (!this.fileHandle.exists()) {
            Gdx.app.error("MewnBase", "WeatherManager: weather.json does not exist");
        } else {
            Gdx.app.debug("MewnBase", "WeatherManager: Reading tile data");
            Json json = new Json();
            String fileText = this.fileHandle.readString();
            WeatherDataList weatherDataListAll = json.fromJson(WeatherDataList.class, fileText);
            this.weatherDataList = weatherDataListAll.weatherDataList;
            Gdx.app.debug("MewnBase", "WeatherManager: list size: " + this.weatherDataList.size());
        }
    }

    public void setMode(Mission.weatherModes mode) {
        this.weatherMode = mode;
        this.weatherMode = Mission.weatherModes.normal;
        try {
            this.weatherMode = MoonBase.getCurrentMission().weatherMode;
        }
        catch (Exception e) {
            MoonBase.error(e.getLocalizedMessage());
            e.printStackTrace();
        }
        switch (this.weatherMode) {
            case none: {
                break;
            }
            case low: {
                this.rollStartingBase = 4.0f;
                this.maxConsecutiveWeather = 6;
                break;
            }
            case normal: {
                this.rollStartingBase = 1.0f;
                this.maxConsecutiveWeather = 5;
                break;
            }
            case high: {
                this.rollStartingBase = 0.0f;
                this.maxConsecutiveWeather = 3;
            }
        }
        MoonBase.log("WeatherManager: weather mode: " + (Object)((Object)this.weatherMode));
    }

    public void setChanceMultiplier(float mul) {
        if (mul <= 0.0f) mul = 1.0f;
        this.chanceMultiplier = mul;
    }

    public void setLightningDelayMultiplier(float mul) {
        if (mul <= 0.0f) mul = 1.0f;
        this.lightningDelayMultiplier = mul;
    }

    public float getTimer() {
        return this.timer;
    }

    public float getDuration() {
        return this.delay;
    }

    public WeatherData getCurrentData() {
        return this.currentData;
    }

    public float getSolarEfficiency() {
        return this.solarEfficiency;
    }

    public float getRainRate() {
        return this.rainRate;
    }

    public float getDustRate() {
        return this.dustRate;
    }

    public float getWindSpeed() {
        return this.windSpeed;
    }

    public float getEventTime(WeatherData weatherData) {
        return MathUtils.random(weatherData.minDuration, weatherData.maxDuration);
    }

    public void update(float delta) {
        if (this.weatherMode != Mission.weatherModes.none) {
            this.timer += delta;
            if (this.timer > this.delay - warningTime && !this.notifiedForNext) {
                this.setupNext();
                this.weatherAlert();
            }
            if (this.timer > this.delay) {
                this.changeWeather();
            }
            this.updateWeatherTransition(delta);
            if (this.currentData.hazards != null && this.currentData.hazards.length() != 0) {
                this.doHazard(delta, this.currentData.hazards);
            }
        }
        this.windChangeTimer += delta;
        if (this.windChangeTimer > this.windChangeDelay) {
            this.changeWindSpeed();
            this.windChangeTimer = 0.0f;
        }
        if (this.windTransitionTimer < this.windTransitionLength) {
            this.windTransitionTimer += delta;
            float wa = this.windTransitionTimer / this.windTransitionLength;
            this.windSpeed = MathUtils.lerp(this.prevWindSpeed, this.targetWindSpeed, wa);
        } else {
            this.prevWindSpeed = this.windSpeed;
        }
    }

    private void changeWindSpeed() {
        this.targetWindSpeed = MathUtils.random(this.currentData.windMin, this.currentData.windMax);
        Gdx.app.debug("MewnBase", "Target wind speed :" + this.targetWindSpeed);
        this.windChangeTimer = 0.0f;
        this.windChangeDelay = MathUtils.random(this.windChangeDelayBase * 0.7f, this.windChangeDelayBase * 1.5f);
        this.windTransitionTimer = 0.0f;
        if (this.prevWindSpeed == -1.0f) {
            this.prevWindSpeed = MathUtils.random(this.currentData.windMin, this.currentData.windMax);
        }
    }

    private void doHazard(float delta, String hazard) {
        if (hazard.equals("lightning")) {
            this.lightningTimer += delta;
            if (this.lightningTimer > this.lightningDelay) {
                this.lightningTimer = 0.0f;
                this.lightningDelay = (12.0f + (float)MathUtils.random(-5, 5)) * this.lightningDelayMultiplier;
                this.spawnLightningBolts();
            }
        }
    }

    private void spawnLightningBolts() {
        GridPoint2 gp;
        int playerX = this.world.player.getX();
        int playerY = this.world.player.getY();
        this.lightningSampler = new UniformPoissonDiskSampler(playerX - lightningSpawnRadius, playerY - lightningSpawnRadius, playerX + lightningSpawnRadius, playerY + lightningSpawnRadius, 3.5);
        this.lightningPointList = this.lightningSampler.sample();
        this.lightningGridPoints.clear();
        for (Vector2DDouble v : this.lightningPointList) {
            gp = World.getGridPointFromPoolAndSet((int)v.x, (int)v.y);
            this.lightningGridPoints.add(gp);
        }
        for (int i = 0; i < this.numLightningSpots && !this.lightningGridPoints.isEmpty(); ++i) {
            int randomIndex = MathUtils.random(this.lightningGridPoints.size() - 1);
            gp = World.gridPointPool.obtain();
            gp.set(this.lightningGridPoints.get(randomIndex));
            this.lightningGridPoints.remove(randomIndex);
            LightningStrike ls = new LightningStrike(this.world, (float)gp.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)gp.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
            if (i == 0) {
                ls.setDecoy(false);
                LightningCollector lc = this.findLightningCollector(gp.x, gp.y);
                if (lc != null) {
                    ls.setWorldPos(lc.getXCenter(), lc.getYCenter());
                    System.out.println("hitting the lightning collector! " + lc.worldX + ", " + lc.worldY);
                }
            }
            World.gridPointPool.free(gp);
        }
        for (GridPoint2 gp2 : this.lightningGridPoints) {
            World.gridPointPool.free(gp2);
        }
    }

    private LightningCollector findLightningCollector(int worldX, int worldY) {
        GridPoint2 strikeGp = new GridPoint2(worldX, worldY);
        ArrayList<LightningCollector> lightningCollectorsList = new ArrayList<LightningCollector>();
        LightningCollector closest = null;
        float closestDistance = 99999.0f;
        ArrayList<GridPoint2> nearbyTiles = Tile.GET_NEARBY_COORDS(worldX, worldY, LightningCollector.TILERADIUS);
        for (GridPoint2 gp : nearbyTiles) {
            Tile t = this.world.getTile(gp.x, gp.y);
            if (t == null || !(t instanceof LightningCollector)) continue;
            lightningCollectorsList.add((LightningCollector)t);
            float dst = gp.dst(strikeGp);
            if (!(dst < closestDistance)) continue;
            closestDistance = dst;
            closest = (LightningCollector)t;
        }
        if (closestDistance <= 5.0f) {
            return closest;
        }
        if (closestDistance > 5.0f && closestDistance <= 8.0f ? MathUtils.random() > 0.75f : MathUtils.random() > 0.5f) {
            return closest;
        }
        return null;
    }

    public void doLightningFlash(float strength) {
        this.lightningFlash.clearActions();
        this.lightningFlash.addAction(Actions.sequence(Actions.alpha(0.0f), Actions.alpha(strength *= 0.5f, 0.05f), Actions.delay(0.05f), Actions.alpha(strength * 0.25f), Actions.delay(0.06f), Actions.alpha(strength, 0.025f), Actions.fadeOut(0.25f)));
    }

    private void updateWeatherTransition(float delta) {
        if (this.transitionTimer < 5.0f) {
            this.transitionTimer += delta;
            float a = this.transitionTimer / 5.0f;
            this.outputColor = this.lerpColor(this.ambientWeatherColor, this.ambientTargetColor, a);
            this.weatherColorTint.setColor(this.outputColor);
            BloomLerp.TRANSITION(this.bloom, this.currentBloomSettings, this.targetBloomSettings, a);
            this.solarEfficiency = MathUtils.lerp(this.prevSolarEfficiency, this.currentData.solarEfficency, a);
            this.rainRate = MathUtils.lerp(this.prevRainRate, this.currentData.rainRate, a);
            this.dustRate = MathUtils.lerp(this.prevDustRate, this.currentData.dustRate, a);
            if (this.targetSoundFx != null) {
                Audio.getInstance().updateLoopingSoundVolume(this.targetSoundFx, this.targetSoundFxId, a, 0.0f);
            }
            if (this.prevSoundFx != null) {
                Audio.getInstance().updateLoopingSoundVolume(this.prevSoundFx, this.prevSoundFxId, 1.0f - a, 0.0f);
            }
        } else if (this.transitioningColor) {
            this.transitioningColor = false;
            this.ambientWeatherColor = this.ambientTargetColor.cpy();
            this.weatherColorTint.setColor(this.currentData.color);
            this.currentBloomSettings = this.targetBloomSettings;
            this.prevSolarEfficiency = this.solarEfficiency = this.currentData.solarEfficency;
            this.prevRainRate = this.rainRate = this.currentData.rainRate;
            this.prevDustRate = this.dustRate = this.currentData.dustRate;
        }
    }

    private void weatherAlert() {
        this.notifiedForNext = true;
    }

    public void setupNext() {
        block3: {
            block4: {
                block7: {
                    block5: {
                        block6: {
                            if (this.weatherMode == Mission.weatherModes.none) break block4;
                            if (!this.currentData.defaultWeather) break block5;
                            if (this.consequtiveStreak < this.maxConsecutiveWeather) break block6;
                            this.consequtiveStreak = 1;
                            int maxTries = 100;
                            for (int tries = 0; this.nextData == this.currentData && tries < maxTries; ++tries) {
                                this.randomRoll();
                            }
                            break block3;
                        }
                        this.randomRoll();
                        if (this.nextData != this.currentData) break block3;
                        ++this.consequtiveStreak;
                        break block3;
                    }
                    if (MoonBase.getCurrentMission().weatherMode != Mission.weatherModes.high) break block7;
                    this.randomRoll();
                    if (this.nextData != this.currentData) break block3;
                    ++this.consequtiveStreak;
                    break block3;
                }
                for (WeatherData w : this.weatherDataList) {
                    if (!w.defaultWeather) continue;
                    this.nextData = w;
                    break block3;
                }
                break block3;
            }
            for (WeatherData w : this.weatherDataList) {
                if (!w.defaultWeather) continue;
                this.nextData = w;
                break;
            }
        }
    }

    private void randomRoll() {
        float max = this.rollStartingBase;
        for (WeatherData w : this.weatherDataList) {
            max += w.chance * this.chanceMultiplier;
        }
        float r = MathUtils.random(0.0f, max);
        float baseR = 0.0f;
        for (WeatherData w : this.weatherDataList) {
            float weighted = w.chance * this.chanceMultiplier;
            if (r <= weighted + baseR) {
                this.nextData = w;
                break;
            }
            baseR += weighted;
        }
    }

    public void setWeather(String dataId) {
        for (WeatherData w : this.weatherDataList) {
            if (!w.id.equals(dataId)) continue;
            this.nextData = w;
            break;
        }
        this.changeWeather();
    }

    public void changeWeather() {
        Gdx.app.log("MewnBase", "change weather :" + this.nextData.id);
        if (this.currentData.rainRate == 0.0f && this.nextData.rainRate > 0.0f) {
            this.setChanged();
            this.notifyObservers("rainStart");
        }
        if (this.currentData.rainRate > 0.0f && this.nextData.rainRate == 0.0f) {
            this.setChanged();
            this.notifyObservers("rainStop");
        }
        if (this.currentData.id != this.nextData.id) {
            this.currentData = this.nextData;
            Gdx.app.log("MewnBase", "Weather changed, now: " + this.currentData.id);
            this.world.changeWeather(this.currentData);
            this.setWeatherPostFx(this.currentData);
            this.setWeatherSound(this.currentData.soundFile);
            this.transitionTimer = 0.0f;
            this.transitioningColor = true;
        }
        this.changeWindSpeed();
        this.timer = 0.0f;
        this.delay = this.getEventTime(this.currentData);
        this.notifiedForNext = false;
    }

    private void setWeatherSound(String soundFile) {
        this.prevSoundFx = this.targetSoundFx;
        this.prevSoundFxId = this.targetSoundFxId;
        if (soundFile != null) {
            this.targetSoundFx = AssetManagerSingleton.getInstance().get(soundFile, Sound.class);
            this.targetSoundFxId = Audio.getInstance().playSoundLoop(this.targetSoundFx, 0.0f, 1.0f, 0.0f);
        } else {
            this.targetSoundFx = null;
            this.targetSoundFxId = -1L;
        }
    }

    private void setWeatherPostFx(WeatherData data) {
        this.ambientTargetColor.set(data.color);
        this.targetBloomSettings = new Bloom.Settings("clearBlur", Blur.BlurType.Gaussian5x5b, 1, data.bloom_blurAmount, data.bloom_threshold, data.bloom_baseIntensity, data.bloom_baseSaturation, data.bloom_bloomIntensity, data.bloom_bloomSaturation);
    }

    private Color lerpColor(Color c1, Color c2, float alpha) {
        float invAlpha = 1.0f - alpha;
        float r = c1.r * invAlpha + c2.r * alpha;
        float g = c1.g * invAlpha + c2.g * alpha;
        float b = c1.b * invAlpha + c2.b * alpha;
        float a = c1.a * invAlpha + c2.a * alpha;
        this.lerpdColor.set(r, g, b, a);
        return this.lerpdColor;
    }

    public void setTimer(float timer) {
        this.timer = timer;
    }

    public void setDuration(float duration) {
        this.delay = duration;
    }

    public void skipTransition() {
        this.transitionTimer = 5.0f;
    }

    public void dispose() {
        if (this.targetSoundFx != null) {
            this.targetSoundFx.dispose();
        }
        if (this.prevSoundFx != null) {
            this.prevSoundFx.dispose();
        }
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }
}
