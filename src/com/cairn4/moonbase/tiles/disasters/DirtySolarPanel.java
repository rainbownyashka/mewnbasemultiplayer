/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.disasters;

import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.SolarPanel;
import com.cairn4.moonbase.tiles.disasters.BaseDisaster;

public class DirtySolarPanel
extends BaseDisaster {
    SolarPanel solarPanel;

    public DirtySolarPanel(BaseModule base) {
        super(base);
        this.solarPanel = (SolarPanel)base;
        this.solarPanel.setDirtySprite();
    }

    @Override
    public void fix() {
        super.fix();
        this.solarPanel.setCleanSprite();
    }
}

