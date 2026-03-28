/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.techtree;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.NetworkHelper;
import com.cairn4.moonbase.Server;
import com.cairn4.moonbase.PlayerStatus;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.techtree.TechTree;
import com.cairn4.moonbase.techtree.TechUpgrade;
import java.util.ArrayList;
import java.util.Observable;

public class TechManager
extends Observable {
    public static final int NUM_RESEARCH_OBJECTS = 18;
    public static final String RESEARCHOBJECT_NAME = "researchObject";
    public static int NUM_TECH_UPGRADES = 0;
    FileHandle techTreeData;
    public TechTree techTree;
    public int samples;
    private World world;

    public TechManager(World world) {
        this.world = world;
        this.load();
        this.notifyHud();
    }

    private void load() {
        this.techTreeData = Gdx.files.local("techTree.json");
        if (!this.techTreeData.exists()) {
            Gdx.app.error("MewnBase", "TechManager: techTree.json does not exist");
        }
        Json json = new Json();
        String fileText = this.techTreeData.readString();
        try {
            this.techTree = json.fromJson(TechTree.class, fileText);
            NUM_TECH_UPGRADES = this.techTree.upgrades.size();
            MoonBase.log("TechManager: Loaded " + NUM_TECH_UPGRADES + " tech upgrades");
        }
        catch (SerializationException s) {
            MoonBase.error("TechManager: there was an error reading the techTree.json file.");
            MoonBase.error(s.getMessage().toString());
        }
    }

    public boolean canResearch(String id) {
        TechUpgrade tech = this.getTech(id);
        if (tech != null) {
            return !tech.unlocked && this.meetsPreReqs(id) && this.hasEnoughSamples(tech);
        }
        Gdx.app.error("MewnBase", "TechManager: Tech is null " + tech.id);
        return false;
    }

    public boolean hasEnoughSamples(TechUpgrade upgrade) {
        return this.samples >= upgrade.cost;
    }

    public boolean meetsPreReqs(String id) {
        TechUpgrade tech = this.getTech(id);
        int numReqs = tech.preReqTech.size();
        for (String r : tech.preReqTech) {
            TechUpgrade req = this.getTech(r);
            if (req != null) {
                if (!req.unlocked) continue;
                --numReqs;
                continue;
            }
            MoonBase.error("TechManager: TechUpgrade is null " + tech.id);
        }
        return numReqs == 0;
    }

    public void research(String id) {
        try {
            if (this.world != null && this.world.gameScreen != null && this.world.gameScreen.client != null) {
                NetworkHelper.sendPayload(this.world.gameScreen, "TECH_RESEARCH:" + id);
                return;
            }
        } catch (Exception ignored) {}
        TechUpgrade tech = this.getTech(id);
        tech.unlocked = true;
        this.samples -= tech.cost;
        Gdx.app.log("MewnBase", "TechManager: researched " + tech.id);
        if (tech.suitLevel != 0 && this.world.player != null) {
            Gdx.app.log("MewnBase", "Upgrading suit level, maxing all suit values.");
            this.world.player.setSuitLevel(tech.suitLevel, true);
        }
        if (tech.baseUpgrade) {
            this.world.baseManager.baseUpgrade(tech.id, false);
        }
        MoonBase cfr_ignored_0 = this.world.gameScreen.game;
        MoonBase.achievementAdapter.techUnlocked(this.getNumberOfTechsUnlocked());
        this.notifyHud();
        try {
            Server s = Server.getActiveServer();
            if (s != null && s.isTechSyncEnabled()) {
                try { s.broadcastFromServer("TECH_SYNC:" + this.samples + ":" + java.net.URLEncoder.encode(String.join(",", this.getSaveData()), "UTF-8")); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    public int getNumberOfTechsUnlocked() {
        int unlocked = 0;
        for (TechUpgrade t : this.techTree.upgrades) {
            if (!t.unlocked) continue;
            ++unlocked;
        }
        return unlocked;
    }

    public TechUpgrade getTech(String id) {
        for (TechUpgrade t : this.techTree.upgrades) {
            if (!t.id.equals(id)) continue;
            return t;
        }
        MoonBase.error("TechManager: Can't find tech with id : " + id);
        return null;
    }

    public ArrayList<String> getSaveData() {
        ArrayList<String> unlocked = new ArrayList<String>();
        for (TechUpgrade t : this.techTree.upgrades) {
            if (!t.unlocked) continue;
            unlocked.add(t.id);
        }
        return unlocked;
    }

    public void unlockAll() {
        Gdx.app.log("TechManager", "Making sure all tech is unlocked");
        for (TechUpgrade tech : this.techTree.upgrades) {
            tech.unlocked = true;
        }
        if (this.world.player != null) {
            Gdx.app.log("MewnBase", "Upgrading suit to max level.");
            this.world.player.setSuitLevel(PlayerStatus.suitUpgradeData.suitLevels.size() - 1, false);
        }
    }

    public void addSamples(int amount) {
        try {
            if (this.world != null && this.world.gameScreen != null && this.world.gameScreen.client != null) {
                NetworkHelper.sendPayload(this.world.gameScreen, "TECH_SAMPLES_ADD:" + amount);
                return;
            }
        } catch (Exception ignored) {}
        this.samples += amount;
        this.notifyHud();
        try {
            Server s = Server.getActiveServer();
            if (s != null && s.isTechSyncEnabled()) {
                try { s.broadcastFromServer("TECH_SYNC:" + this.samples + ":" + java.net.URLEncoder.encode(String.join(",", this.getSaveData()), "UTF-8")); } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    public void setSamples(int samples) {
        this.samples = samples;
        this.notifyHud();
    }

    public void notifyHud() {
        this.setChanged();
        this.notifyObservers("researchSamplesUpdate");
    }
}
