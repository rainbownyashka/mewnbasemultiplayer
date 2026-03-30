/*
 * Planet generation settings (stored per-planet in saves/<save>/planet_<id>.json)
 */
package com.cairn4.moonbase.worlddata;

import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.Mission;

public class PlanetGenConfig {
    public int seed = 0;
    public String planetType = null;
    public String dayCycleMode = null;
    public String weatherMode = null;
    public int missionDayLength = 0;

    // Terrain noise + bias
    public float altitudeNoise = 0.0f;
    public float altitudeSmoothing = 0.0f;
    public float wetnessNoise = 0.0f;
    public float wetnessSmoothing = 0.0f;
    public float temperatureNoise = 0.0f;
    public float temperatureSmoothing = 0.0f;
    public float altitudeBias = 0.0f;
    public float wetnessBias = 0.0f;
    public float temperatureBias = 0.0f;

    public boolean enableVolcanos = true;
    public boolean enableSamples = true;
    public boolean enableNpcs = true;

    public static PlanetGenConfig randomConfig(Mission m) {
        PlanetGenConfig c = new PlanetGenConfig();
        c.seed = MathUtils.random(10000);
        c.planetType = (m != null && m.planetType != null) ? m.planetType : "default";
        c.dayCycleMode = pickDayCycleMode();
        c.weatherMode = pickWeatherMode();
        c.missionDayLength = MathUtils.random(25, 40);

        c.altitudeNoise = MathUtils.random(0.009f, 0.02f);
        c.altitudeSmoothing = MathUtils.random(0.8f, 1.8f);
        c.wetnessNoise = MathUtils.random(0.01f, 0.03f);
        c.wetnessSmoothing = MathUtils.random(0.6f, 1.6f);
        c.temperatureNoise = MathUtils.random(0.007f, 0.02f);
        c.temperatureSmoothing = MathUtils.random(0.6f, 1.6f);
        c.altitudeBias = MathUtils.random(-0.35f, 0.35f);
        c.wetnessBias = MathUtils.random(-0.45f, 0.45f);
        c.temperatureBias = MathUtils.random(-0.45f, 0.45f);

        c.enableVolcanos = MathUtils.randomBoolean(0.8f);
        c.enableSamples = MathUtils.randomBoolean(0.9f);
        c.enableNpcs = MathUtils.randomBoolean(0.7f);
        return c;
    }

    private static String pickDayCycleMode() {
        int r = MathUtils.random(99);
        if (r < 60) return Mission.dayCycleModes.defaultDay.toString();
        if (r < 80) return Mission.dayCycleModes.longNight.toString();
        if (r < 90) return Mission.dayCycleModes.onlyNight.toString();
        return Mission.dayCycleModes.onlyDay.toString();
    }

    private static String pickWeatherMode() {
        int r = MathUtils.random(99);
        if (r < 10) return Mission.weatherModes.none.toString();
        if (r < 35) return Mission.weatherModes.low.toString();
        if (r < 85) return Mission.weatherModes.normal.toString();
        return Mission.weatherModes.high.toString();
    }
}
