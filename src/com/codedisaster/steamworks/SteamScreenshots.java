/*
 * Decompiled with CFR 0.152.
 */
package com.codedisaster.steamworks;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamInterface;
import com.codedisaster.steamworks.SteamPublishedFileID;
import com.codedisaster.steamworks.SteamScreenshotHandle;
import com.codedisaster.steamworks.SteamScreenshotsCallback;
import com.codedisaster.steamworks.SteamScreenshotsCallbackAdapter;
import java.nio.ByteBuffer;

public class SteamScreenshots
extends SteamInterface {
    public SteamScreenshots(SteamScreenshotsCallback callback) {
        super(SteamAPI.getSteamScreenshotsPointer(), SteamScreenshots.createCallback(new SteamScreenshotsCallbackAdapter(callback)));
    }

    public SteamScreenshotHandle writeScreenshot(ByteBuffer rgb, int width, int height) {
        return new SteamScreenshotHandle(SteamScreenshots.writeScreenshot(this.pointer, rgb, rgb.remaining(), width, height));
    }

    public SteamScreenshotHandle addScreenshotToLibrary(String file, String thumbnail, int width, int height) {
        return new SteamScreenshotHandle(SteamScreenshots.addScreenshotToLibrary(this.pointer, file, thumbnail, width, height));
    }

    public void triggerScreenshot() {
        SteamScreenshots.triggerScreenshot(this.pointer);
    }

    public void hookScreenshots(boolean hook) {
        SteamScreenshots.hookScreenshots(this.pointer, hook);
    }

    public boolean setLocation(SteamScreenshotHandle screenshot, String location) {
        return SteamScreenshots.setLocation(this.pointer, screenshot.handle, location);
    }

    public boolean tagUser(SteamScreenshotHandle screenshot, SteamID steamID) {
        return SteamScreenshots.tagUser(this.pointer, screenshot.handle, steamID.handle);
    }

    public boolean tagPublishedFile(SteamScreenshotHandle screenshot, SteamPublishedFileID publishedFileID) {
        return SteamScreenshots.tagPublishedFile(this.pointer, screenshot.handle, publishedFileID.handle);
    }

    public boolean isScreenshotsHooked() {
        return SteamScreenshots.isScreenshotsHooked(this.pointer);
    }

    private static native long createCallback(SteamScreenshotsCallbackAdapter var0);

    private static native int writeScreenshot(long var0, ByteBuffer var2, int var3, int var4, int var5);

    private static native int addScreenshotToLibrary(long var0, String var2, String var3, int var4, int var5);

    private static native void triggerScreenshot(long var0);

    private static native void hookScreenshots(long var0, boolean var2);

    private static native boolean setLocation(long var0, int var2, String var3);

    private static native boolean tagUser(long var0, int var2, long var3);

    private static native boolean tagPublishedFile(long var0, int var2, long var3);

    private static native boolean isScreenshotsHooked(long var0);
}

