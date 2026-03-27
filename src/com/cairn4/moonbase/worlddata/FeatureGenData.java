/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.cairn4.moonbase.worlddata.ItemDropperSpawnBiome;
import java.util.ArrayList;

public class FeatureGenData {
    public String id;
    public int maxPerChunk;
    public float minDistanceBetween;
    public float minAltitude;
    public float noiseFeature;
    public float noiseSmooth;
    public ArrayList<ItemDropperSpawnBiome> spawnBiomes = new ArrayList();
    public boolean surroundingTilesNeedToMatch;
}

