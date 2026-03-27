/*
 * Decompiled with CFR 0.152.
 */
package com.codedisaster.steamworks;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAPICall;
import com.codedisaster.steamworks.SteamAuth;
import com.codedisaster.steamworks.SteamAuthTicket;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamInterface;
import com.codedisaster.steamworks.SteamUserCallback;
import com.codedisaster.steamworks.SteamUserCallbackAdapter;
import java.nio.ByteBuffer;

public class SteamUser
extends SteamInterface {
    public SteamUser(SteamUserCallback callback) {
        super(SteamAPI.getSteamUserPointer(), SteamUser.createCallback(new SteamUserCallbackAdapter(callback)));
    }

    public SteamID getSteamID() {
        return new SteamID(SteamUser.getSteamID(this.pointer));
    }

    public int initiateGameConnection(ByteBuffer authBlob, SteamID steamIDGameServer, int serverIP, short serverPort, boolean secure) throws SteamException {
        if (!authBlob.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        int bytesWritten = SteamUser.initiateGameConnection(this.pointer, authBlob, authBlob.position(), authBlob.remaining(), steamIDGameServer.handle, serverIP, serverPort, secure);
        if (bytesWritten > 0) {
            authBlob.limit(bytesWritten);
        }
        return bytesWritten;
    }

    public void terminateGameConnection(int serverIP, short serverPort) {
        SteamUser.terminateGameConnection(this.pointer, serverIP, serverPort);
    }

    public void startVoiceRecording() {
        SteamUser.startVoiceRecording(this.pointer);
    }

    public void stopVoiceRecording() {
        SteamUser.stopVoiceRecording(this.pointer);
    }

    public VoiceResult getAvailableVoice(int[] bytesAvailable) {
        int result = SteamUser.getAvailableVoice(this.pointer, bytesAvailable);
        return VoiceResult.byOrdinal(result);
    }

    public VoiceResult getVoice(ByteBuffer voiceData, int[] bytesWritten) throws SteamException {
        if (!voiceData.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        int result = SteamUser.getVoice(this.pointer, voiceData, voiceData.position(), voiceData.remaining(), bytesWritten);
        return VoiceResult.byOrdinal(result);
    }

    public VoiceResult decompressVoice(ByteBuffer voiceData, ByteBuffer audioData, int[] bytesWritten, int desiredSampleRate) throws SteamException {
        if (!voiceData.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        if (!audioData.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        int result = SteamUser.decompressVoice(this.pointer, voiceData, voiceData.position(), voiceData.remaining(), audioData, audioData.position(), audioData.remaining(), bytesWritten, desiredSampleRate);
        return VoiceResult.byOrdinal(result);
    }

    public int getVoiceOptimalSampleRate() {
        return SteamUser.getVoiceOptimalSampleRate(this.pointer);
    }

    public SteamAuthTicket getAuthSessionTicket(ByteBuffer authTicket, int[] sizeInBytes) throws SteamException {
        if (!authTicket.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        int ticket = SteamUser.getAuthSessionTicket(this.pointer, authTicket, authTicket.position(), authTicket.remaining(), sizeInBytes);
        if ((long)ticket != 0L) {
            authTicket.limit(sizeInBytes[0]);
        }
        return new SteamAuthTicket(ticket);
    }

    public SteamAuth.BeginAuthSessionResult beginAuthSession(ByteBuffer authTicket, SteamID steamID) throws SteamException {
        if (!authTicket.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        int result = SteamUser.beginAuthSession(this.pointer, authTicket, authTicket.position(), authTicket.remaining(), steamID.handle);
        return SteamAuth.BeginAuthSessionResult.byOrdinal(result);
    }

    public void endAuthSession(SteamID steamID) {
        SteamUser.endAuthSession(this.pointer, steamID.handle);
    }

    public void cancelAuthTicket(SteamAuthTicket authTicket) {
        SteamUser.cancelAuthTicket(this.pointer, (int)authTicket.handle);
    }

    public SteamAuth.UserHasLicenseForAppResult userHasLicenseForApp(SteamID steamID, int appID) {
        return SteamAuth.UserHasLicenseForAppResult.byOrdinal(SteamUser.userHasLicenseForApp(this.pointer, steamID.handle, appID));
    }

    public SteamAPICall requestEncryptedAppTicket(ByteBuffer dataToInclude) throws SteamException {
        if (!dataToInclude.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        return new SteamAPICall(SteamUser.requestEncryptedAppTicket(this.pointer, this.callback, dataToInclude, dataToInclude.position(), dataToInclude.remaining()));
    }

    public boolean getEncryptedAppTicket(ByteBuffer ticket, int[] sizeInBytes) throws SteamException {
        if (!ticket.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        return SteamUser.getEncryptedAppTicket(this.pointer, ticket, ticket.position(), ticket.remaining(), sizeInBytes);
    }

    public boolean isBehindNAT() {
        return SteamUser.isBehindNAT(this.pointer);
    }

    public void advertiseGame(SteamID steamIDGameServer, int serverIP, short serverPort) {
        SteamUser.advertiseGame(this.pointer, steamIDGameServer.handle, serverIP, serverPort);
    }

    private static native long createCallback(SteamUserCallbackAdapter var0);

    private static native long getSteamID(long var0);

    private static native int initiateGameConnection(long var0, ByteBuffer var2, int var3, int var4, long var5, int var7, short var8, boolean var9);

    private static native void terminateGameConnection(long var0, int var2, short var3);

    private static native void startVoiceRecording(long var0);

    private static native void stopVoiceRecording(long var0);

    private static native int getAvailableVoice(long var0, int[] var2);

    private static native int getVoice(long var0, ByteBuffer var2, int var3, int var4, int[] var5);

    private static native int decompressVoice(long var0, ByteBuffer var2, int var3, int var4, ByteBuffer var5, int var6, int var7, int[] var8, int var9);

    private static native int getVoiceOptimalSampleRate(long var0);

    private static native int getAuthSessionTicket(long var0, ByteBuffer var2, int var3, int var4, int[] var5);

    private static native int beginAuthSession(long var0, ByteBuffer var2, int var3, int var4, long var5);

    private static native void endAuthSession(long var0, long var2);

    private static native void cancelAuthTicket(long var0, int var2);

    private static native int userHasLicenseForApp(long var0, long var2, int var4);

    private static native long requestEncryptedAppTicket(long var0, long var2, ByteBuffer var4, int var5, int var6);

    private static native boolean getEncryptedAppTicket(long var0, ByteBuffer var2, int var3, int var4, int[] var5);

    private static native boolean isBehindNAT(long var0);

    private static native void advertiseGame(long var0, long var2, int var4, short var5);

    public static enum VoiceResult {
        OK,
        NotInitialized,
        NotRecording,
        NoData,
        BufferTooSmall,
        DataCorrupted,
        Restricted,
        UnsupportedCodec,
        ReceiverOutOfDate,
        ReceiverDidNotAnswer;

        private static final VoiceResult[] values;

        static VoiceResult byOrdinal(int voiceResult) {
            return values[voiceResult];
        }

        static {
            values = VoiceResult.values();
        }
    }
}

