/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.RichPresenceState;

public interface PlatformAdapter {
    public void packTextures();

    public void writeErrorLog(Throwable var1);

    public void steamInit();

    public void steamAPIupdate();

    public void dispose();

    public void initRPC();

    public void updateRPC(RichPresenceState var1);

    public void requestUserStats();

    public void requestGlobalStats();

    public boolean isAchievementEarned(String var1);

    public boolean earnAchievement(String var1);

    public void updateStat(String var1, int var2);

    public void resetStats();

    public void resetAchievementsAndStats();

    public void clearAchievement(String var1);
}

