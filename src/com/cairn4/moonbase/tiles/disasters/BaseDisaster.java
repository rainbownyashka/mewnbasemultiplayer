/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.disasters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.cairn4.moonbase.tiles.BaseModule;

public class BaseDisaster {
    BaseModule base;
    float repairTime;
    public Types type;
    Color originalColor;
    Color originalLightColor;

    public BaseDisaster(BaseModule base) {
        this.base = base;
    }

    public void update(float delta) {
    }

    public void fix() {
        Gdx.app.log("MewnBase", this.getClass().getSimpleName() + ".fix() on " + this.base.getClass().getSimpleName());
        this.base.removeDisaster();
    }

    public void dispose() {
    }

    public static enum Types {
        airLoss,
        fire,
        electrical,
        solar;

    }
}

