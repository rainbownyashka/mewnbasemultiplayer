/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.disasters;

import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.WindTurbine;
import com.cairn4.moonbase.tiles.disasters.BaseDisaster;

public class BrokenWindTurbine
extends BaseDisaster {
    WindTurbine windTurbine;

    public BrokenWindTurbine(BaseModule base) {
        super(base);
        this.windTurbine = (WindTurbine)base;
        this.windTurbine.setBroken();
    }

    @Override
    public void fix() {
        super.fix();
        this.windTurbine.setFixed();
    }
}

