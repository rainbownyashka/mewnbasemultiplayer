/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.DamageCallback;
import com.cairn4.moonbase.worlddata.BehaviorData;
import com.cairn4.moonbase.worlddata.DamageDef;

public class Damageable
implements Behavior {
    public float maxHealth;
    public float health;
    public DamageCallback callback;
    public float thresholdModerateDamage;
    public float thresholdCritical;
    private dStates damageState;
    public boolean loaded = false;

    public void setMaxHealth(float m) {
        this.maxHealth = m;
        this.health = m;
        this.damageState = dStates.normal;
    }

    public void takeDamage(float dmg, DamageDef damageDef) {
        this.health -= dmg;
        if (this.callback != null) {
            if (this.health <= 0.0f) {
                this.callback.destroyed();
            } else {
                this.callback.damageResult(this.health);
                this.updateState();
            }
        }
    }

    private boolean isNowModeratelyDamaged() {
        if (this.damageState != dStates.moderately) {
            float r = this.health / this.maxHealth;
            return r < this.thresholdModerateDamage && r >= this.thresholdCritical;
        }
        return false;
    }

    private boolean isNowCriticallyDamaged() {
        if (this.damageState != dStates.critically) {
            float r = this.health / this.maxHealth;
            return r < this.thresholdCritical;
        }
        return false;
    }

    public void updateState() {
        float r = this.health / this.maxHealth;
        if (r >= this.thresholdModerateDamage) {
            if (this.damageState != dStates.normal) {
                this.damageState = dStates.normal;
            }
        } else if (r < this.thresholdModerateDamage && r >= this.thresholdCritical) {
            if (this.damageState != dStates.moderately) {
                this.damageState = dStates.moderately;
                this.callback.moderateDamage();
            }
        } else if (this.damageState != dStates.critically) {
            this.damageState = dStates.critically;
            this.callback.criticalDamage();
        }
    }

    public void repair() {
        this.health = this.maxHealth;
        this.damageState = dStates.normal;
        if (this.callback != null) {
            this.callback.repaired();
        }
    }

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public void setId(String s) {
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void start() {
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void interact(Player player) {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public BehaviorData getData() {
        BehaviorData bd = new BehaviorData();
        bd.properties.put("health", Float.valueOf(this.health));
        return null;
    }

    private static enum dStates {
        normal,
        moderately,
        critically;

    }
}

