/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.worlddata.StatusEffectData;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PlayerStatusEffect {
    public boolean timeBased = false;
    public boolean readyToRemove = false;
    public float maxTime;
    public float time;
    public boolean showWarning = false;
    public boolean alertSound = false;
    public String iconDrawable;

    public void update(float delta) {
        if (this.timeBased) {
            this.updateTimer(delta);
        }
    }

    private void updateTimer(float delta) {
        this.time -= delta;
        if (this.time <= 0.0f) {
            this.readyToRemove = true;
            System.out.println("ready to remove status effect");
        }
    }

    public void loadProperties(HashMap<String, Object> p) {
        for (Map.Entry<String, Object> entry : p.entrySet()) {
            Class<?> aClass = this.getClass();
            Field field = null;
            try {
                field = aClass.getField(entry.getKey());
                field.setAccessible(true);
                field.setAccessible(true);
                field.set(this, entry.getValue());
            }
            catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public StatusEffectData getData() {
        return null;
    }

    public float applyWalkSpeedModifier(float walkSpeedMultiplier) {
        return walkSpeedMultiplier;
    }
}

