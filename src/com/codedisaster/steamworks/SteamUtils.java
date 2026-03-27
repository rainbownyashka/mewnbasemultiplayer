/*
 * Decompiled with CFR 0.152.
 */
package com.codedisaster.steamworks;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAPICall;
import com.codedisaster.steamworks.SteamAPIWarningMessageHook;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamInterface;
import com.codedisaster.steamworks.SteamUniverse;
import com.codedisaster.steamworks.SteamUtilsCallback;
import com.codedisaster.steamworks.SteamUtilsCallbackAdapter;
import java.nio.ByteBuffer;

public class SteamUtils
extends SteamInterface {
    private SteamUtilsCallbackAdapter callbackAdapter;

    public SteamUtils(SteamUtilsCallback callback) {
        super(SteamAPI.getSteamUtilsPointer());
        this.callbackAdapter = new SteamUtilsCallbackAdapter(callback);
        this.setCallback(SteamUtils.createCallback(this.callbackAdapter));
    }

    public int getSecondsSinceAppActive() {
        return SteamUtils.getSecondsSinceAppActive(this.pointer);
    }

    public int getSecondsSinceComputerActive() {
        return SteamUtils.getSecondsSinceComputerActive(this.pointer);
    }

    public SteamUniverse getConnectedUniverse() {
        return SteamUniverse.byValue(SteamUtils.getConnectedUniverse(this.pointer));
    }

    public int getServerRealTime() {
        return SteamUtils.getServerRealTime(this.pointer);
    }

    public int getImageWidth(int image) {
        return SteamUtils.getImageWidth(this.pointer, image);
    }

    public int getImageHeight(int image) {
        return SteamUtils.getImageHeight(this.pointer, image);
    }

    public boolean getImageSize(int image, int[] size) {
        return SteamUtils.getImageSize(this.pointer, image, size);
    }

    public boolean getImageRGBA(int image, ByteBuffer dest) throws SteamException {
        if (!dest.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        return SteamUtils.getImageRGBA(this.pointer, image, dest, dest.position(), dest.remaining());
    }

    public int getAppID() {
        return SteamUtils.getAppID(this.pointer);
    }

    public void setOverlayNotificationPosition(NotificationPosition position) {
        SteamUtils.setOverlayNotificationPosition(this.pointer, position.ordinal());
    }

    public boolean isAPICallCompleted(SteamAPICall handle, boolean[] result) {
        return SteamUtils.isAPICallCompleted(this.pointer, handle.handle, result);
    }

    public SteamAPICallFailure getAPICallFailureReason(SteamAPICall handle) {
        return SteamAPICallFailure.byValue(SteamUtils.getAPICallFailureReason(this.pointer, handle.handle));
    }

    public void setWarningMessageHook(SteamAPIWarningMessageHook messageHook) {
        this.callbackAdapter.setWarningMessageHook(messageHook);
        SteamUtils.enableWarningMessageHook(this.callback, messageHook != null);
    }

    public boolean isOverlayEnabled() {
        return SteamUtils.isOverlayEnabled(this.pointer);
    }

    private static native long createCallback(SteamUtilsCallbackAdapter var0);

    private static native int getSecondsSinceAppActive(long var0);

    private static native int getSecondsSinceComputerActive(long var0);

    private static native int getConnectedUniverse(long var0);

    private static native int getServerRealTime(long var0);

    private static native String getIPCountry(long var0);

    private static native int getImageWidth(long var0, int var2);

    private static native int getImageHeight(long var0, int var2);

    private static native boolean getImageSize(long var0, int var2, int[] var3);

    private static native boolean getImageRGBA(long var0, int var2, ByteBuffer var3, int var4, int var5);

    private static native int getAppID(long var0);

    private static native void setOverlayNotificationPosition(long var0, int var2);

    private static native boolean isAPICallCompleted(long var0, long var2, boolean[] var4);

    private static native int getAPICallFailureReason(long var0, long var2);

    private static native void enableWarningMessageHook(long var0, boolean var2);

    private static native boolean isOverlayEnabled(long var0);

    public static enum NotificationPosition {
        TopLeft,
        TopRight,
        BottomLeft,
        BottomRight;

    }

    public static enum SteamAPICallFailure {
        None(-1),
        SteamGone(0),
        NetworkFailure(1),
        InvalidHandle(2),
        MismatchedCallback(3);

        private final int code;
        private static final SteamAPICallFailure[] values;

        private SteamAPICallFailure(int code) {
            this.code = code;
        }

        static SteamAPICallFailure byValue(int code) {
            for (SteamAPICallFailure value : values) {
                if (value.code != code) continue;
                return value;
            }
            return None;
        }

        static {
            values = SteamAPICallFailure.values();
        }
    }
}

