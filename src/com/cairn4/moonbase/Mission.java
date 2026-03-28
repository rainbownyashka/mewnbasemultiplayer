/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.GameLoader;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.Localization;
import java.util.Observable;

public class Mission
extends Observable {
    public String saveFolder;
    public String playerName;
    public boolean missionCompleteReady = false;
    public boolean missionComplete = false;
    public String characterSuitColor;
    public int characterFaceOption;
    public MissionTypes missionType;
    public String tutorialStage;
    public boolean tutorialFinished;
    public String seed;
    public dayCycleModes dayCycleMode;
    public weatherModes weatherMode;
    public waterModes waterMode;
    public techTreeModes techTreeMode;
    public creatureModes creatureMode;
    int missionDayLength;
    String planetName;
    public int planetId = 0;
    public String planetType = "default";

    public void finishedTutorial() {
        this.tutorialFinished = true;
    }

    public int getMissionDayLength() {
        return this.missionDayLength;
    }

    public String getPlanetName() {
        return this.planetName;
    }

    public void setPlanetName(String name) {
        this.planetName = name;
    }

    public Mission(String planetName, int missionDayLength) {
        this.planetName = planetName;
        this.missionDayLength = missionDayLength;
    }

    public Mission() {
    }

    public Mission(MissionTypes type, String playerName) {
        MoonBase.currentSaveFolder = this.saveFolder = GameLoader.createSaveFolder(playerName);
        this.missionType = type;
        this.playerName = playerName;
        this.tutorialStage = "start";
        this.missionDayLength = MathUtils.random(25, 35);
        this.planetName = this.generatePlanetName();
        this.planetId = 0;
        this.planetType = "default";
    }

    public Mission(MissionTypes type) {
        this.missionType = type;
        this.tutorialStage = "start";
        this.missionDayLength = MathUtils.random(30, 35);
        this.planetName = this.generatePlanetName();
        this.planetId = 0;
        this.planetType = "default";
    }

    public void setName(String playerName) {
        MoonBase.currentSaveFolder = this.saveFolder = GameLoader.createSaveFolder(playerName);
        this.playerName = playerName;
    }

    public String generatePlanetName() {
        String[] letters = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        String name = "";
        String c1 = letters[MathUtils.random(25)];
        String c2 = letters[MathUtils.random(25)];
        String c3 = letters[MathUtils.random(25)];
        int n1 = MathUtils.random(9);
        int n2 = MathUtils.random(9);
        int n3 = MathUtils.random(9);
        name = "" + c1 + c2 + c3 + "-" + n1 + n2 + n3;
        return name;
    }

    public String getMissionTypeName() {
        switch (this.missionType) {
            case tutorial: {
                return Localization.get("newgame_tutorial");
            }
            case normal: {
                return Localization.get("newgame_normal");
            }
            case endless: {
                return Localization.get("newgame_endless");
            }
            case creative: {
                return Localization.get("newgame_creative");
            }
        }
        Gdx.app.error("MoonBase", "Mission: no mission type found");
        return "No mission type!?";
    }

    public void setMissionCompleteReady() {
        this.missionCompleteReady = true;
        this.setChanged();
        this.notifyObservers("mission_complete_ready");
    }

    public void setMissionComplete() {
        if (!this.missionComplete) {
            MoonBase.log("Mission completed!");
            this.missionComplete = true;
            MoonBase.achievementAdapter.missionCompleted(this);
        } else {
            MoonBase.log("Mission had already been completed before.");
        }
        this.setChanged();
        this.notifyObservers("mission_complete");
    }

    public static enum creatureModes {
        off,
        passive,
        hostile;

    }

    public static enum techTreeModes {
        defaultTech,
        allTechUnlocked;

    }

    public static enum waterModes {
        defaultWater,
        noWater;

    }

    public static enum weatherModes {
        none,
        low,
        normal,
        high;

    }

    public static enum dayCycleModes {
        onlyDay,
        defaultDay,
        longNight,
        onlyNight;

    }

    public static enum MissionTypes {
        tutorial,
        normal,
        endless,
        creative;

    }
}
