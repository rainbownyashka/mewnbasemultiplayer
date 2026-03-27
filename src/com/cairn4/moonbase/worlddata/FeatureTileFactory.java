/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.worlddata.FeatureGenData;
import com.cairn4.moonbase.worlddata.FeatureGenDataSet;
import java.util.ArrayList;

public class FeatureTileFactory {
    private static FeatureTileFactory instance;
    private FileHandle file;
    public ArrayList<FeatureGenData> featureGenDataList = new ArrayList();

    private FeatureTileFactory() {
        this.load();
    }

    public static synchronized FeatureTileFactory getInstance() {
        if (instance == null) {
            instance = new FeatureTileFactory();
        }
        return instance;
    }

    public void load() {
        this.file = Gdx.files.local(MoonBase.coreFolder + "feature_tiles.json");
        if (this.file.exists()) {
            MoonBase.log("FeatureTileFactory: Loading...");
            Json json = new Json();
            String fileText = this.file.readString();
            FeatureGenDataSet dataSet = json.fromJson(FeatureGenDataSet.class, fileText);
            this.featureGenDataList = dataSet.featureTileList;
        } else {
            MoonBase.error("FeatureTileFactory: feature_tiles.json not found!");
        }
        MoonBase.log("FeatureTileFactory loaded " + this.featureGenDataList.size() + " items");
    }
}

