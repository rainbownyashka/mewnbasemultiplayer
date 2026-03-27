/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlatformAdapter;
import com.cairn4.moonbase.RichPresenceState;
import com.cairn4.moonbase.desktop.DesktopLauncher;
import com.cairn4.moonbase.desktop.achievements.SteamAchievementAdapter;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamApps;
import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamLeaderboardEntriesHandle;
import com.codedisaster.steamworks.SteamLeaderboardHandle;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUser;
import com.codedisaster.steamworks.SteamUserCallback;
import com.codedisaster.steamworks.SteamUserStats;
import com.codedisaster.steamworks.SteamUserStatsCallback;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

public class DesktopAdapter
implements PlatformAdapter {
    private long rpcStartTime = 0L;
    private Thread discordCallbackThread;
    private float steamCallbackTimer = 0.0f;
    private static final float STEAM_CALLBACK_DELAY = 0.125f;
    private SteamApps apps;
    private SteamUser user;
    private SteamUserStats userStats;
    public SteamAchievementAdapter achivementManager;
    private boolean steamRunning = false;
    private SteamUserCallback userCallback = new SteamUserCallback(){

        @Override
        public void onValidateAuthTicket(SteamID steamID, SteamAuth.AuthSessionResponse authSessionResponse, SteamID ownerSteamID) {
        }

        @Override
        public void onMicroTxnAuthorization(int appID, long orderID, boolean authorized) {
        }

        @Override
        public void onEncryptedAppTicket(SteamResult result) {
        }
    };
    private SteamUserStatsCallback userStatsCallback = new SteamUserStatsCallback(){

        @Override
        public void onUserStatsReceived(long gameId, SteamID steamIDUser, SteamResult result) {
            System.out.println("Steam User stats received: userId=" + steamIDUser.getAccountID() + ", result=" + result.toString());
            if (MoonBase.DEBUG_INFO) {
                int numAchievements = DesktopAdapter.this.userStats.getNumAchievements();
                System.out.println("Num of achievements: " + numAchievements);
                for (int i = 0; i < numAchievements; ++i) {
                    String name = DesktopAdapter.this.userStats.getAchievementName(i);
                    boolean achieved = DesktopAdapter.this.userStats.isAchieved(name, false);
                    System.out.println("# " + i + " : name=" + name + ", achieved=" + (achieved ? "yes" : "no"));
                }
            }
            if (result == SteamResult.OK) {
                DesktopAdapter.this.achivementManager.setAllStats(DesktopAdapter.this.userStats);
            }
        }

        @Override
        public void onUserStatsStored(long gameId, SteamResult result) {
        }

        @Override
        public void onUserStatsUnloaded(SteamID steamIDUser) {
        }

        @Override
        public void onUserAchievementStored(long gameId, boolean isGroupAchievement, String achievementName, int curProgress, int maxProgress) {
            System.out.println("User achievement stored: " + achievementName + ", progress=" + curProgress + "/" + maxProgress);
        }

        @Override
        public void onLeaderboardFindResult(SteamLeaderboardHandle leaderboard, boolean found) {
        }

        @Override
        public void onLeaderboardScoresDownloaded(SteamLeaderboardHandle leaderboard, SteamLeaderboardEntriesHandle entries, int numEntries) {
        }

        @Override
        public void onLeaderboardScoreUploaded(boolean success, SteamLeaderboardHandle leaderboard, int score, boolean scoreChanged, int globalRankNew, int globalRankPrevious) {
        }

        @Override
        public void onGlobalStatsReceived(long gameId, SteamResult result) {
        }
    };

    @Override
    public void packTextures() {
    }

    @Override
    public void writeErrorLog(Throwable e) {
        String result = "";
        result = result + "MewnBase Crashlog v1.0.1\n";
        result = result + "Build 1\n";
        result = result + new SimpleDateFormat("yyyy-M-d_h.mm.ssa").format(new Date()) + "\n";
        result = MoonBase.STEAM_VERSION ? result + "Steam Version\n" : result + "Itch.io Version\n";
        result = result + System.getProperty("os.arch") + " | " + System.getProperty("os.name") + " | " + System.getProperty("os.version") + "\n\n";
        result = result + MoonBase.coreFolder + "\n\n";
        if (!MoonBase.currentSaveFolder.equals("")) {
            result = result + "Current save: " + MoonBase.currentSaveFolder + "\n";
        }
        result = result + MoonBase.coreFolder + "\n\n";
        String errorMsg = DesktopLauncher.parseException(e, true);
        boolean failed = false;
        result = result + errorMsg;
        String filename = "";
        try {
            filename = "errorlog_v1.0.1_" + new SimpleDateFormat("yyyy-M-d_h.mm.ssa").format(new Date()) + ".txt";
            Files.write(Paths.get(filename, new String[0]), result.getBytes(), new OpenOption[0]);
        }
        catch (IOException i) {
            i.printStackTrace();
            failed = true;
        }
    }

    @Override
    public void steamInit() {
        try {
            SteamAPI.loadLibraries();
            if (!SteamAPI.init()) {
                SteamAPI.printDebugInfo(System.err);
            }
        }
        catch (SteamException e) {
            String errorMsg = DesktopLauncher.parseException(e, true);
            JOptionPane.showMessageDialog(null, "Steam Error!: \n\n" + errorMsg);
        }
        if (SteamAPI.isSteamRunning()) {
            SteamAPI.printDebugInfo(System.out);
            System.out.println("Register user ...");
            this.user = new SteamUser(this.userCallback);
            System.out.println("Register user stats callback ...");
            this.userStats = new SteamUserStats(this.userStatsCallback);
            this.userStats.requestCurrentStats();
            System.out.println("Register SteamApp ... ");
            this.apps = new SteamApps();
            System.out.println("App build ID: " + this.apps.getAppBuildId());
            this.steamRunning = true;
        }
    }

    @Override
    public void steamAPIupdate() {
        if (MoonBase.STEAM_VERSION && this.steamRunning) {
            this.steamCallbackTimer += Gdx.graphics.getDeltaTime();
            if (this.steamCallbackTimer > 0.125f) {
                this.steamCallbackTimer = 0.0f;
                SteamAPI.runCallbacks();
            }
        }
    }

    @Override
    public boolean earnAchievement(String achievementId) {
        if (MoonBase.STEAM_VERSION && this.userStats != null) {
            this.userStats.setAchievement(achievementId);
            this.userStats.storeStats();
            boolean yes = this.userStats.isAchieved(achievementId, false);
            System.out.println("Achievement " + achievementId + " = " + yes);
            return yes;
        }
        return false;
    }

    @Override
    public void requestUserStats() {
        if (MoonBase.STEAM_VERSION && this.userStats != null) {
            this.userStats.requestCurrentStats();
        }
    }

    @Override
    public void requestGlobalStats() {
        if (MoonBase.STEAM_VERSION && this.userStats != null) {
            this.userStats.requestGlobalStats(30);
        }
    }

    @Override
    public boolean isAchievementEarned(String id) {
        if (MoonBase.STEAM_VERSION && this.userStats != null) {
            return this.userStats.isAchieved(id, false);
        }
        return false;
    }

    @Override
    public void updateStat(String name, int amount) {
        if (MoonBase.STEAM_VERSION && this.userStats != null) {
            this.userStats.setStatI(name, amount);
            this.userStats.storeStats();
        }
    }

    @Override
    public void resetStats() {
        if (MoonBase.STEAM_VERSION && this.userStats != null) {
            this.userStats.resetAllStats(false);
        }
    }

    @Override
    public void resetAchievementsAndStats() {
        if (MoonBase.STEAM_VERSION && this.userStats != null) {
            this.userStats.resetAllStats(true);
        }
    }

    @Override
    public void clearAchievement(String id) {
        if (MoonBase.STEAM_VERSION && this.userStats != null) {
            this.userStats.clearAchievement(id);
            System.out.println("Clearing achievement: " + id);
        }
    }

    @Override
    public void initRPC() {
    }

    @Override
    public void updateRPC(RichPresenceState state) {
    }

    @Override
    public void dispose() {
        System.out.println("PlatformAdapter:dispose()");
        System.out.println(Timer.instance().isEmpty());
        Timer.instance().clear();
        if (MoonBase.STEAM_VERSION) {
            Gdx.app.log("MewnBase", "SteamAPI.shutdown");
            if (this.userStats != null) {
                this.userStats.dispose();
            }
            SteamAPI.shutdown();
        }
        if (MoonBase.DISCORD_ON) {
            // empty if block
        }
    }
}

