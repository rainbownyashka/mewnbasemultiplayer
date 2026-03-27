/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.Mission;

public interface AchievementAdapter {
    public void startNewGame();

    public void loadedGame();

    public void daySurvived(Mission var1, int var2);

    public void habitatCrafted();

    public void missionCompleted(Mission var1);

    public void grownPlantCollected();

    public void artifactResearched(int var1);

    public void techUnlocked(int var1);

    public void buildBuggie();

    public void collectCraftedItem(String var1);

    public boolean earnAchievement(String var1);

    public void repairAirLeak();

    public void updatePowerGen(float var1);

    public void updateWaterStored(float var1);
}

