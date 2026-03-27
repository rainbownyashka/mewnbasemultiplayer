/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.worlddata.ItemDropperSpawnBiome;
import com.cairn4.moonbase.worlddata.ItemDropperVariationData;
import com.cairn4.moonbase.worlddata.TileInteractData;
import java.util.ArrayList;

public class ItemDropperData {
    public String id;
    public String locid;
    public boolean naturallyOccurring;
    public int maxPerChunk;
    public float minDistanceBetween;
    public float noiseFeature;
    public float noiseSmooth;
    public ArrayList<ItemDropperSpawnBiome> spawnBiomes = new ArrayList();
    public boolean zSort;
    public ArrayList<String> sprites;
    public ArrayList<String> interactSounds = new ArrayList();
    public ArrayList<String> regrowSprites;
    public String spinePath;
    public float spineBaseScale;
    public ArrayList<String> spineAnimations;
    public String spineRegrowAnim;
    public String physicsBody;
    public String minimapHexColor;
    public float regrowTime;
    public String respawnId;
    public float daysToRespawn;
    public String lightColor;
    public float lightRadius;
    public String toolTypeRequired;
    public int toolLevelRequired;
    public ArrayList<TileInteractData> interactWith = new ArrayList();
    public float minScale;
    public float maxScale;
    public Vector2 offset;
    public float transformTime;
    public String transformNewId;
    public ArrayList<ItemDropperVariationData> dropperVariations = new ArrayList();
    public float zSortOffset = 0.0f;
}

