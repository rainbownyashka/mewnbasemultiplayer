/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.achievements;

import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlayerStatusEffect;
import com.cairn4.moonbase.SpeedBoostStatusEffect;

public class PlayerStatusEffectFactory {
    private static PlayerStatusEffectFactory instance;

    public static synchronized PlayerStatusEffectFactory getInstance() {
        if (instance == null) {
            instance = new PlayerStatusEffectFactory();
        }
        return instance;
    }

    public PlayerStatusEffect createStatusEffect(String classname) {
        switch (classname) {
            case "statusFactory_seedBar": {
                float time;
                SpeedBoostStatusEffect speedboost = new SpeedBoostStatusEffect();
                speedboost.speedMultiplier = 1.15f;
                speedboost.time = time = 180.0f;
                speedboost.maxTime = time;
                return speedboost;
            }
        }
        MoonBase.error("Can't find status effect: " + classname);
        return null;
    }
}

