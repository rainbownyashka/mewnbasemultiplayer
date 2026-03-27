/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.desktop.achievements;

import com.cairn4.moonbase.AchievementAdapter;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlatformAdapter;
import com.cairn4.moonbase.desktop.achievements.Stat;
import com.cairn4.moonbase.techtree.TechManager;
import com.codedisaster.steamworks.SteamUserStats;
import java.util.ArrayList;

public class SteamAchievementAdapter
implements AchievementAdapter {
    private ArrayList<Stat> gameStats = new ArrayList();
    private PlatformAdapter platform;

    public SteamAchievementAdapter(PlatformAdapter pa) {
        this.platform = pa;
        this.gameStats.add(new Stat("artifacts_logged", Stat.statTypes.stat_int));
        this.gameStats.add(new Stat("total_artifacts_researched", Stat.statTypes.stat_int));
        this.gameStats.add(new Stat("techs_unlocked", Stat.statTypes.stat_int));
        this.gameStats.add(new Stat("habitats_crafted", Stat.statTypes.stat_int));
        this.gameStats.add(new Stat("missions_completed", Stat.statTypes.stat_int));
        this.gameStats.add(new Stat("grown_plants_collected", Stat.statTypes.stat_int));
        this.gameStats.add(new Stat("habitats_crafted", Stat.statTypes.stat_int));
        this.gameStats.add(new Stat("metal_plates_crafted", Stat.statTypes.stat_int));
        this.gameStats.add(new Stat("air_leaks_repaired", Stat.statTypes.stat_int));
        this.gameStats.add(new Stat("new_game_started", Stat.statTypes.stat_int));
        this.gameStats.add(new Stat("game_loaded", Stat.statTypes.stat_int));
    }

    @Override
    public void startNewGame() {
        this.incrementStat("new_game_started", 1);
    }

    @Override
    public void loadedGame() {
        this.incrementStat("game_loaded", 1);
    }

    @Override
    public void daySurvived(Mission mission, int dayNum) {
        if (mission.missionType == Mission.MissionTypes.normal || mission.missionType == Mission.MissionTypes.endless) {
            if (!MoonBase.GOD_MODE) {
                if (MoonBase.getCurrentMission().techTreeMode == Mission.techTreeModes.defaultTech) {
                    if (dayNum >= 25) {
                        this.earnAchievement("survive_25");
                    }
                    if (dayNum >= 50) {
                        this.earnAchievement("survive_50");
                    }
                }
            } else {
                MoonBase.log("No survival achievements with god mode enabled");
            }
        }
    }

    @Override
    public void habitatCrafted() {
        this.incrementStat("habitats_crafted", 1);
    }

    @Override
    public void missionCompleted(Mission mission) {
        if ((mission.missionType == Mission.MissionTypes.normal || mission.missionType == Mission.MissionTypes.endless) && mission.techTreeMode == Mission.techTreeModes.defaultTech) {
            this.incrementStat("missions_completed", 1);
            if (mission.dayCycleMode == Mission.dayCycleModes.longNight) {
                this.earnAchievement("mission_longnight");
            }
            if (mission.dayCycleMode == Mission.dayCycleModes.onlyNight) {
                this.earnAchievement("mission_onlynight");
            }
        }
    }

    @Override
    public void grownPlantCollected() {
        this.incrementStat("grown_plants_collected", 1);
    }

    @Override
    public void artifactResearched(int newLogTotal) {
        this.incrementStat("total_artifacts_researched", 1);
        if (newLogTotal == 18) {
            this.earnAchievement("artifact_all");
        }
    }

    @Override
    public void techUnlocked(int newTotal) {
        this.incrementStat("techs_unlocked", 1);
        if (newTotal == 1) {
            this.earnAchievement("tech_unlock_1");
        } else if (newTotal == 5) {
            this.earnAchievement("tech_unlock_5");
        } else if (newTotal >= TechManager.NUM_TECH_UPGRADES) {
            this.earnAchievement("tech_unlock_all");
        }
    }

    @Override
    public void collectCraftedItem(String itemId) {
        if (itemId.equals("habitat-builder")) {
            this.incrementStat("habitats_crafted", 1);
        }
        if (itemId.equals("metal")) {
            this.incrementStat("metal_plates_crafted", 1);
        }
        if (itemId.equals("plant") || itemId.equals("root") || itemId.equals("blueshroom")) {
            this.incrementStat("grown_plants_collected", 1);
        }
    }

    @Override
    public void buildBuggie() {
        this.earnAchievement("build_buggie");
    }

    @Override
    public void repairAirLeak() {
        this.incrementStat("air_leaks_repaired", 1);
    }

    @Override
    public void updatePowerGen(float powerGen) {
        if (powerGen >= 200.0f && MoonBase.STEAM_VERSION && !this.platform.isAchievementEarned("base_power_gen")) {
            this.earnAchievement("base_power_gen");
        }
    }

    @Override
    public void updateWaterStored(float waterStored) {
        if (waterStored >= 1800.0f && MoonBase.STEAM_VERSION && !this.platform.isAchievementEarned("base_water_stored")) {
            this.earnAchievement("base_water_stored");
        }
    }

    public void setAllStats(SteamUserStats userStats) {
        for (Stat stat : this.gameStats) {
            switch (stat.type) {
                case stat_int: {
                    stat.iValue = userStats.getStatI(stat.name, -1);
                    System.out.println("Stat recieved: " + stat.name + ": " + stat.iValue);
                    break;
                }
                case stat_float: {
                    stat.fValue = userStats.getStatF(stat.name, -1.0f);
                    System.out.println("Stat recieved: " + stat.name + ": " + stat.fValue);
                }
            }
        }
    }

    @Override
    public boolean earnAchievement(String achievementId) {
        Mission mission = MoonBase.getCurrentMission();
        if (!(mission == null || mission.missionType != Mission.MissionTypes.endless && mission.missionType != Mission.MissionTypes.normal || mission.techTreeMode != Mission.techTreeModes.defaultTech || MoonBase.GOD_MODE)) {
            return this.platform.earnAchievement(achievementId);
        }
        return false;
    }

    public void setStat(String name, int newValue) {
        Stat stat = this.getLocalStat(name);
        stat.iValue = newValue;
        MoonBase.debug("Stat " + name + " is now " + stat.iValue);
        this.platform.updateStat(stat.name, stat.iValue);
    }

    private void incrementStat(String name, int amount) {
        Stat stat = this.getLocalStat(name);
        if (stat != null) {
            stat.iValue += amount;
        }
        MoonBase.debug("Stat " + name + " is now " + stat.iValue);
        this.platform.updateStat(stat.name, stat.iValue);
    }

    private Stat getLocalStat(String name) {
        for (Stat s : this.gameStats) {
            if (!s.name.equals(name)) continue;
            return s;
        }
        MoonBase.error("Can't find stat: " + name);
        return null;
    }
}

