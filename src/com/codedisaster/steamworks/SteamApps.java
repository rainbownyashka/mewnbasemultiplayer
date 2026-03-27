/*
 * Decompiled with CFR 0.152.
 */
package com.codedisaster.steamworks;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamInterface;

public class SteamApps
extends SteamInterface {
    public SteamApps() {
        super(SteamAPI.getSteamAppsPointer());
    }

    public boolean isSubscribed() {
        return SteamApps.isSubscribed(this.pointer);
    }

    public boolean isLowViolence() {
        return SteamApps.isLowViolence(this.pointer);
    }

    public boolean isCybercafe() {
        return SteamApps.isCybercafe(this.pointer);
    }

    public boolean isVACBanned() {
        return SteamApps.isVACBanned(this.pointer);
    }

    public String getCurrentGameLanguage() {
        return SteamApps.getCurrentGameLanguage(this.pointer);
    }

    public String getAvailableGameLanguages() {
        return SteamApps.getAvailableGameLanguages(this.pointer);
    }

    public boolean isSubscribedApp(int appID) {
        return SteamApps.isSubscribedApp(this.pointer, appID);
    }

    public boolean isDlcInstalled(int appID) {
        return SteamApps.isDlcInstalled(this.pointer, appID);
    }

    public int getEarliestPurchaseUnixTime(int appID) {
        return SteamApps.getEarliestPurchaseUnixTime(this.pointer, appID);
    }

    public boolean isSubscribedFromFreeWeekend() {
        return SteamApps.isSubscribedFromFreeWeekend(this.pointer);
    }

    public int getDLCCount() {
        return SteamApps.getDLCCount(this.pointer);
    }

    public void installDLC(int appID) {
        SteamApps.installDLC(this.pointer, appID);
    }

    public void uninstallDLC(int appID) {
        SteamApps.uninstallDLC(this.pointer, appID);
    }

    public SteamID getAppOwner() {
        return new SteamID(SteamApps.getAppOwner(this.pointer));
    }

    public int getAppBuildId() {
        return SteamApps.getAppBuildId(this.pointer);
    }

    private static native boolean isSubscribed(long var0);

    private static native boolean isLowViolence(long var0);

    private static native boolean isCybercafe(long var0);

    private static native boolean isVACBanned(long var0);

    private static native String getCurrentGameLanguage(long var0);

    private static native String getAvailableGameLanguages(long var0);

    private static native boolean isSubscribedApp(long var0, int var2);

    private static native boolean isDlcInstalled(long var0, int var2);

    private static native int getEarliestPurchaseUnixTime(long var0, int var2);

    private static native boolean isSubscribedFromFreeWeekend(long var0);

    private static native int getDLCCount(long var0);

    private static native void installDLC(long var0, int var2);

    private static native void uninstallDLC(long var0, int var2);

    private static native long getAppOwner(long var0);

    private static native int getAppBuildId(long var0);
}

