/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.badlogic.gdx.graphics.Color;
import com.cairn4.moonbase.tiles.GroundTile;

public class GroundTileData {
    public int x;
    public int y;
    public String biome;
    public boolean disovered;
    public String sprite;
    // Added for ice overlay / temp-based biomes; older saves will have default values.
    public float temperature;
    public boolean hasTemperature;
    @Deprecated
    public Color color;

    public GroundTileData() {
    }

    public GroundTileData(GroundTile gt) {
        this.x = gt.x;
        this.y = gt.y;
        this.biome = gt.getBiome().toString();
        this.disovered = gt.isDiscovered();
        this.temperature = gt.temperature;
        this.hasTemperature = true;
    }
}
