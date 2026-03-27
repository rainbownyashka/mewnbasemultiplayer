/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.TileProgressBar;

public class TilePickupProgressBar
extends TileProgressBar {
    public TilePickupProgressBar(Hud hud, Tile t) {
        super(hud, t);
    }

    @Override
    public void update() {
        this.set(this.tile.getPickupProgress());
    }
}

