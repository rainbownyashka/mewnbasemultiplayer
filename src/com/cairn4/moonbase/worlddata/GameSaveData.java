/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.entities.NpcBonuses;
import com.cairn4.moonbase.worlddata.EntityData;
import com.cairn4.moonbase.worlddata.PlanetData;
import com.cairn4.moonbase.worlddata.PlayerData;
import com.cairn4.moonbase.worlddata.RespawningItemDropperData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class GameSaveData
implements Comparable<GameSaveData> {
    public boolean readOnly;
    public boolean autoSave;
    public int saveDataVersion;
    public String saveFolder;
    @Deprecated
    public String lastModified;
    public long lastUpdated;
    public String playerName;
    public String missionType;
    public String missionPlanetName;
    public int missionDayGoal;
    public boolean missionCompleteReady;
    public boolean missionComplete;
    public int terrainGenSeed;
    public int currentDayNum;
    public String currentPeriod;
    public float currentPeriodTime;
    public String currentWeatherId;
    public float currentWeatherTime;
    public float currentWeatherDuration;
    public String dayCycleModes;
    public String waterMode;
    public String techTreeMode;
    public String weatherMode;
    public String creatureMode;
    public boolean creativeMode;
    public PlayerData playerData;
    public int currentPlanetId = 0;
    public ArrayList<PlanetData> planets = new ArrayList();
    public boolean tutorialFinished;
    public String tutorialStage;
    public ArrayList<String> unlockedTech = new ArrayList();
    public int techSamples;
    public ArrayList<NpcBonuses.bonusTypes> npcBonuses = new ArrayList();
    public ArrayList<EntityData> entityDataList = new ArrayList();
    public ArrayList<RespawningItemDropperData> regrowthManagerList = new ArrayList();

    public String getLastDateModified() {
        String result = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss  dd/MMM/yyyy", Locale.getDefault());
            GregorianCalendar calendar = new GregorianCalendar(TimeZone.getDefault());
            if (this.lastUpdated != 0L) {
                calendar.setTimeInMillis(this.lastUpdated);
            } else {
                FileHandle dataFile = Gdx.files.local("saves/" + this.saveFolder + "/gameSave.json");
                calendar.setTimeInMillis(dataFile.lastModified());
            }
            result = sdf.format(calendar.getTime());
        }
        catch (Exception e) {
            MoonBase.error(e.getLocalizedMessage());
        }
        return result;
    }

    @Override
    public int compareTo(GameSaveData g2) {
        if (this.lastUpdated > g2.lastUpdated) {
            return -1;
        }
        if (g2.lastUpdated > this.lastUpdated) {
            return 1;
        }
        return 0;
    }
}
