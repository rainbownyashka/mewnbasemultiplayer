/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.math.MathUtils;
import com.bitfire.postprocessing.effects.Bloom;

public class BloomLerp {
    public static void TRANSITION(Bloom bloom, Bloom.Settings settings1, Bloom.Settings settings2, float progress) {
        bloom.setBaseIntesity(MathUtils.lerp(settings1.baseIntensity, settings2.baseIntensity, progress));
        bloom.setBloomIntesity(MathUtils.lerp(settings1.bloomIntensity, settings2.bloomIntensity, progress));
        bloom.setBaseSaturation(MathUtils.lerp(settings1.baseSaturation, settings2.baseSaturation, progress));
        bloom.setBloomSaturation(MathUtils.lerp(settings1.bloomSaturation, settings2.bloomSaturation, progress));
        bloom.setThreshold(MathUtils.lerp(settings1.bloomThreshold, settings2.bloomThreshold, progress));
        bloom.setBlurAmount(MathUtils.lerp(settings1.blurAmount, settings2.blurAmount, progress));
    }
}

