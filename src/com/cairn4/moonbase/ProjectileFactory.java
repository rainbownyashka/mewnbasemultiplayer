/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Projectile;
import com.cairn4.moonbase.worlddata.ProjectileData;
import java.util.ArrayList;

public class ProjectileFactory {
    private static ProjectileFactory instance;
    public static ArrayList<ProjectileData> PROJECTILELIST;

    public static synchronized ProjectileFactory getInstance() {
        if (instance == null) {
            instance = new ProjectileFactory();
        }
        return instance;
    }

    private ProjectileFactory() {
        FileHandle itemDataFile = Gdx.files.local(MoonBase.coreFolder + "projectiles.json");
        if (!itemDataFile.exists()) {
            MoonBase.error("ProjectileFactory: .json file does not exist");
        } else {
            Json json = new Json();
            String fileText = itemDataFile.readString();
            ProjectileDataList list = json.fromJson(ProjectileDataList.class, fileText);
            PROJECTILELIST = list.projectileDataList;
            MoonBase.debug("ProjectileFactory: found " + PROJECTILELIST.size() + " projectile definitions");
            this.loadAssets();
        }
    }

    public Projectile newProjectile(World world, String id) {
        ProjectileData pd = this.getData(id);
        if (pd != null) {
            Projectile p = new Projectile(world);
            p.setData(pd);
            return p;
        }
        Projectile p = new Projectile(world);
        p.readyToRemove = true;
        return null;
    }

    public ProjectileData getData(String id) {
        for (ProjectileData pd : PROJECTILELIST) {
            if (!id.equals(pd.id)) continue;
            return pd;
        }
        MoonBase.error("Couldn't find projectile def id: " + id);
        return null;
    }

    private void loadAssets() {
        ParticleEffectLoader.ParticleEffectParameter p = new ParticleEffectLoader.ParticleEffectParameter();
        p.atlasFile = "game.atlas";
        for (ProjectileData pd : PROJECTILELIST) {
            MoonBase.debug("Loading particle: " + pd.id);
            try {
                pd.loadParticles(p);
                pd.loadSoundFx();
                MoonBase.debug("Loading assets for " + pd.id);
            }
            catch (Exception e) {
                MoonBase.error("Error loading assets for " + pd.id);
                e.printStackTrace();
            }
        }
        AssetManagerSingleton.getInstance().finishLoading();
    }

    static {
        PROJECTILELIST = new ArrayList();
    }

    public static class ProjectileDataList {
        public ArrayList<ProjectileData> projectileDataList = new ArrayList();
    }
}

