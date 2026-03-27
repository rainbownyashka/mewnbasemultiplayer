/*
 * Decompiled with CFR 0.152.
 */
package com.codedisaster.steamworks;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAPICall;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamInterface;
import com.codedisaster.steamworks.SteamLeaderboardEntriesHandle;
import com.codedisaster.steamworks.SteamLeaderboardEntry;
import com.codedisaster.steamworks.SteamLeaderboardHandle;
import com.codedisaster.steamworks.SteamUserStatsCallback;
import com.codedisaster.steamworks.SteamUserStatsCallbackAdapter;

public class SteamUserStats
extends SteamInterface {
    public SteamUserStats(SteamUserStatsCallback callback) {
        super(SteamAPI.getSteamUserStatsPointer(), SteamUserStats.createCallback(new SteamUserStatsCallbackAdapter(callback)));
    }

    public boolean requestCurrentStats() {
        return SteamUserStats.requestCurrentStats(this.pointer);
    }

    public int getStatI(String name, int defaultValue) {
        int[] values = new int[1];
        if (SteamUserStats.getStat(this.pointer, name, values)) {
            return values[0];
        }
        return defaultValue;
    }

    public boolean setStatI(String name, int value) {
        return SteamUserStats.setStat(this.pointer, name, value);
    }

    public float getStatF(String name, float defaultValue) {
        float[] values = new float[1];
        if (SteamUserStats.getStat(this.pointer, name, values)) {
            return values[0];
        }
        return defaultValue;
    }

    public boolean setStatF(String name, float value) {
        return SteamUserStats.setStat(this.pointer, name, value);
    }

    public boolean isAchieved(String name, boolean defaultValue) {
        boolean[] achieved = new boolean[1];
        if (SteamUserStats.getAchievement(this.pointer, name, achieved)) {
            return achieved[0];
        }
        return defaultValue;
    }

    public boolean setAchievement(String name) {
        return SteamUserStats.setAchievement(this.pointer, name);
    }

    public boolean clearAchievement(String name) {
        return SteamUserStats.clearAchievement(this.pointer, name);
    }

    public boolean storeStats() {
        return SteamUserStats.storeStats(this.pointer);
    }

    public boolean indicateAchievementProgress(String name, int curProgress, int maxProgress) {
        return SteamUserStats.indicateAchievementProgress(this.pointer, name, curProgress, maxProgress);
    }

    public int getNumAchievements() {
        return SteamUserStats.getNumAchievements(this.pointer);
    }

    public String getAchievementName(int index) {
        return SteamUserStats.getAchievementName(this.pointer, index);
    }

    public boolean resetAllStats(boolean achievementsToo) {
        return SteamUserStats.resetAllStats(this.pointer, achievementsToo);
    }

    public SteamAPICall findOrCreateLeaderboard(String leaderboardName, LeaderboardSortMethod leaderboardSortMethod, LeaderboardDisplayType leaderboardDisplayType) {
        return new SteamAPICall(SteamUserStats.findOrCreateLeaderboard(this.pointer, this.callback, leaderboardName, leaderboardSortMethod.ordinal(), leaderboardDisplayType.ordinal()));
    }

    public SteamAPICall findLeaderboard(String leaderboardName) {
        return new SteamAPICall(SteamUserStats.findLeaderboard(this.pointer, this.callback, leaderboardName));
    }

    public String getLeaderboardName(SteamLeaderboardHandle leaderboard) {
        return SteamUserStats.getLeaderboardName(this.pointer, leaderboard.handle);
    }

    public int getLeaderboardEntryCount(SteamLeaderboardHandle leaderboard) {
        return SteamUserStats.getLeaderboardEntryCount(this.pointer, leaderboard.handle);
    }

    public LeaderboardSortMethod getLeaderboardSortMethod(SteamLeaderboardHandle leaderboard) {
        return LeaderboardSortMethod.values()[SteamUserStats.getLeaderboardSortMethod(this.pointer, leaderboard.handle)];
    }

    public LeaderboardDisplayType getLeaderboardDisplayType(SteamLeaderboardHandle leaderboard) {
        return LeaderboardDisplayType.values()[SteamUserStats.getLeaderboardDisplayType(this.pointer, leaderboard.handle)];
    }

    public SteamAPICall downloadLeaderboardEntries(SteamLeaderboardHandle leaderboard, LeaderboardDataRequest leaderboardDataRequest, int rangeStart, int rangeEnd) {
        return new SteamAPICall(SteamUserStats.downloadLeaderboardEntries(this.pointer, this.callback, leaderboard.handle, leaderboardDataRequest.ordinal(), rangeStart, rangeEnd));
    }

    public SteamAPICall downloadLeaderboardEntriesForUsers(SteamLeaderboardHandle leaderboard, SteamID[] users) {
        int count = users.length;
        long[] handles = new long[count];
        for (int i = 0; i < count; ++i) {
            handles[i] = users[i].handle;
        }
        return new SteamAPICall(SteamUserStats.downloadLeaderboardEntriesForUsers(this.pointer, this.callback, leaderboard.handle, handles, count));
    }

    public boolean getDownloadedLeaderboardEntry(SteamLeaderboardEntriesHandle leaderboardEntries, int index, SteamLeaderboardEntry entry, int[] details) {
        return details == null ? SteamUserStats.getDownloadedLeaderboardEntry(this.pointer, leaderboardEntries.handle, index, entry) : SteamUserStats.getDownloadedLeaderboardEntry(this.pointer, leaderboardEntries.handle, index, entry, details, details.length);
    }

    public SteamAPICall uploadLeaderboardScore(SteamLeaderboardHandle leaderboard, LeaderboardUploadScoreMethod method, int score, int[] scoreDetails) {
        return new SteamAPICall(scoreDetails == null ? SteamUserStats.uploadLeaderboardScore(this.pointer, this.callback, leaderboard.handle, method.ordinal(), score) : SteamUserStats.uploadLeaderboardScore(this.pointer, this.callback, leaderboard.handle, method.ordinal(), score, scoreDetails, scoreDetails.length));
    }

    public SteamAPICall requestGlobalStats(int historyDays) {
        return new SteamAPICall(SteamUserStats.requestGlobalStats(this.pointer, this.callback, historyDays));
    }

    public long getGlobalStat(String name, long defaultValue) {
        long[] values = new long[1];
        if (SteamUserStats.getGlobalStat(this.pointer, name, values)) {
            return values[0];
        }
        return defaultValue;
    }

    public double getGlobalStat(String name, double defaultValue) {
        double[] values = new double[1];
        if (SteamUserStats.getGlobalStat(this.pointer, name, values)) {
            return values[0];
        }
        return defaultValue;
    }

    public int getGlobalStatHistory(String name, long[] data) {
        return SteamUserStats.getGlobalStatHistory(this.pointer, name, data, data.length);
    }

    public int getGlobalStatHistory(String name, double[] data) {
        return SteamUserStats.getGlobalStatHistory(this.pointer, name, data, data.length);
    }

    private static native long createCallback(SteamUserStatsCallbackAdapter var0);

    private static native boolean requestCurrentStats(long var0);

    private static native boolean getStat(long var0, String var2, int[] var3);

    private static native boolean setStat(long var0, String var2, int var3);

    private static native boolean getStat(long var0, String var2, float[] var3);

    private static native boolean setStat(long var0, String var2, float var3);

    private static native boolean getAchievement(long var0, String var2, boolean[] var3);

    private static native boolean setAchievement(long var0, String var2);

    private static native boolean clearAchievement(long var0, String var2);

    private static native boolean storeStats(long var0);

    private static native boolean indicateAchievementProgress(long var0, String var2, int var3, int var4);

    private static native int getNumAchievements(long var0);

    private static native String getAchievementName(long var0, int var2);

    private static native boolean resetAllStats(long var0, boolean var2);

    private static native long findOrCreateLeaderboard(long var0, long var2, String var4, int var5, int var6);

    private static native long findLeaderboard(long var0, long var2, String var4);

    private static native String getLeaderboardName(long var0, long var2);

    private static native int getLeaderboardEntryCount(long var0, long var2);

    private static native int getLeaderboardSortMethod(long var0, long var2);

    private static native int getLeaderboardDisplayType(long var0, long var2);

    private static native long downloadLeaderboardEntries(long var0, long var2, long var4, int var6, int var7, int var8);

    private static native long downloadLeaderboardEntriesForUsers(long var0, long var2, long var4, long[] var6, int var7);

    private static native boolean getDownloadedLeaderboardEntry(long var0, long var2, int var4, SteamLeaderboardEntry var5, int[] var6, int var7);

    private static native boolean getDownloadedLeaderboardEntry(long var0, long var2, int var4, SteamLeaderboardEntry var5);

    private static native long uploadLeaderboardScore(long var0, long var2, long var4, int var6, int var7, int[] var8, int var9);

    private static native long uploadLeaderboardScore(long var0, long var2, long var4, int var6, int var7);

    private static native long requestGlobalStats(long var0, long var2, int var4);

    private static native boolean getGlobalStat(long var0, String var2, long[] var3);

    private static native boolean getGlobalStat(long var0, String var2, double[] var3);

    private static native int getGlobalStatHistory(long var0, String var2, long[] var3, int var4);

    private static native int getGlobalStatHistory(long var0, String var2, double[] var3, int var4);

    public static enum LeaderboardUploadScoreMethod {
        None,
        KeepBest,
        ForceUpdate;

    }

    public static enum LeaderboardSortMethod {
        None,
        Ascending,
        Descending;

    }

    public static enum LeaderboardDisplayType {
        None,
        Numeric,
        TimeSeconds,
        TimeMilliSeconds;

    }

    public static enum LeaderboardDataRequest {
        Global,
        GlobalAroundUser,
        Friends,
        Users;

    }
}

