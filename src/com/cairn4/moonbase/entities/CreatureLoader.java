/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.worlddata.CreatureData;
import com.cairn4.moonbase.worlddata.CreatureDataList;
import java.util.ArrayList;

public class CreatureLoader {
    private static CreatureLoader instance;
    private FileHandle fileHandle;
    public ArrayList<CreatureData> creatureDataList = new ArrayList();

    public static synchronized CreatureLoader getInstance() {
        if (instance == null) {
            instance = new CreatureLoader();
        }
        return instance;
    }

    private CreatureLoader() {
        this.loadData();
        this.loadAssets();
    }

    private void loadData() {
        this.fileHandle = Gdx.files.local(MoonBase.coreFolder + "creatures.json");
        if (!this.fileHandle.exists()) {
            MoonBase.error("CreatureManager: creatures.json does not exist");
        } else {
            MoonBase.log("CreatureManager: Reading creature data");
            Json json = new Json();
            String fileText = this.fileHandle.readString();
            CreatureDataList allData = json.fromJson(CreatureDataList.class, fileText);
            this.creatureDataList = allData.creatureDataList;
            MoonBase.debug("CreatureManager: list size: " + this.creatureDataList.size());
        }
    }

    public void loadAssets() {
        ParticleEffectLoader.ParticleEffectParameter p = new ParticleEffectLoader.ParticleEffectParameter();
        p.atlasFile = "game.atlas";
        for (CreatureData cd : this.creatureDataList) {
            try {
                cd.loadParticles(p);
                cd.loadSoundFx();
            }
            catch (Exception e) {
                MoonBase.error("Error loading assets for " + cd.id);
                e.printStackTrace();
            }
            MoonBase.log("Loading assets for " + cd.id);
        }
        AssetManagerSingleton.getInstance().finishLoading();
    }

    public void spawn(String id, float worldX, float worldY) {
    }

    public CreatureData getData(String creatureId) {
        for (CreatureData cd : this.creatureDataList) {
            if (!cd.id.equals(creatureId)) continue;
            return cd;
        }
        MoonBase.error("CreatureLoader: Can't find creature id: " + creatureId);
        return null;
    }
}

