/*
 * Decompiled with CFR 0.152.
 */
package com.codedisaster.steamworks;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamInterface;
import com.codedisaster.steamworks.SteamMatchmakingGameServerItem;
import com.codedisaster.steamworks.SteamMatchmakingKeyValuePair;
import com.codedisaster.steamworks.SteamMatchmakingPingResponse;
import com.codedisaster.steamworks.SteamMatchmakingPlayersResponse;
import com.codedisaster.steamworks.SteamMatchmakingRulesResponse;
import com.codedisaster.steamworks.SteamMatchmakingServerListResponse;
import com.codedisaster.steamworks.SteamServerListRequest;
import com.codedisaster.steamworks.SteamServerQuery;

public class SteamMatchmakingServers
extends SteamInterface {
    public SteamMatchmakingServers() {
        super(SteamAPI.getSteamMatchmakingServersPointer());
    }

    public SteamServerListRequest requestInternetServerList(int appID, SteamMatchmakingKeyValuePair[] filters, SteamMatchmakingServerListResponse requestServersResponse) {
        return new SteamServerListRequest(SteamMatchmakingServers.requestInternetServerList(this.pointer, appID, filters, filters.length, requestServersResponse.callback));
    }

    public SteamServerListRequest requestLANServerList(int appID, SteamMatchmakingServerListResponse requestServersResponse) {
        return new SteamServerListRequest(SteamMatchmakingServers.requestLANServerList(this.pointer, appID, requestServersResponse.callback));
    }

    public SteamServerListRequest requestFriendsServerList(int appID, SteamMatchmakingKeyValuePair[] filters, SteamMatchmakingServerListResponse requestServersResponse) {
        return new SteamServerListRequest(SteamMatchmakingServers.requestFriendsServerList(this.pointer, appID, filters, filters.length, requestServersResponse.callback));
    }

    public SteamServerListRequest requestFavoritesServerList(int appID, SteamMatchmakingKeyValuePair[] filters, SteamMatchmakingServerListResponse requestServersResponse) {
        return new SteamServerListRequest(SteamMatchmakingServers.requestFavoritesServerList(this.pointer, appID, filters, filters.length, requestServersResponse.callback));
    }

    public SteamServerListRequest requestHistoryServerList(int appID, SteamMatchmakingKeyValuePair[] filters, SteamMatchmakingServerListResponse requestServersResponse) {
        return new SteamServerListRequest(SteamMatchmakingServers.requestHistoryServerList(this.pointer, appID, filters, filters.length, requestServersResponse.callback));
    }

    public SteamServerListRequest requestSpectatorServerList(int appID, SteamMatchmakingKeyValuePair[] filters, SteamMatchmakingServerListResponse requestServersResponse) {
        return new SteamServerListRequest(SteamMatchmakingServers.requestSpectatorServerList(this.pointer, appID, filters, filters.length, requestServersResponse.callback));
    }

    public void releaseRequest(SteamServerListRequest request) {
        SteamMatchmakingServers.releaseRequest(this.pointer, request.handle);
    }

    public boolean getServerDetails(SteamServerListRequest request, int server, SteamMatchmakingGameServerItem details) {
        return SteamMatchmakingServers.getServerDetails(this.pointer, request.handle, server, details);
    }

    public void cancelQuery(SteamServerListRequest request) {
        SteamMatchmakingServers.cancelQuery(this.pointer, request.handle);
    }

    public void refreshQuery(SteamServerListRequest request) {
        SteamMatchmakingServers.refreshQuery(this.pointer, request.handle);
    }

    public boolean isRefreshing(SteamServerListRequest request) {
        return SteamMatchmakingServers.isRefreshing(this.pointer, request.handle);
    }

    public int getServerCount(SteamServerListRequest request) {
        return SteamMatchmakingServers.getServerCount(this.pointer, request.handle);
    }

    public void refreshServer(SteamServerListRequest request, int server) {
        SteamMatchmakingServers.refreshServer(this.pointer, request.handle, server);
    }

    public SteamServerQuery pingServer(int ip, short port, SteamMatchmakingPingResponse requestServersResponse) {
        return new SteamServerQuery(SteamMatchmakingServers.pingServer(this.pointer, ip, port, requestServersResponse.callback));
    }

    public SteamServerQuery playerDetails(int ip, short port, SteamMatchmakingPlayersResponse requestServersResponse) {
        return new SteamServerQuery(SteamMatchmakingServers.playerDetails(this.pointer, ip, port, requestServersResponse.callback));
    }

    public SteamServerQuery serverRules(int ip, short port, SteamMatchmakingRulesResponse requestServersResponse) {
        return new SteamServerQuery(SteamMatchmakingServers.serverRules(this.pointer, ip, port, requestServersResponse.callback));
    }

    public void cancelServerQuery(SteamServerQuery serverQuery) {
        SteamMatchmakingServers.cancelServerQuery(this.pointer, serverQuery.handle);
    }

    private static native long requestInternetServerList(long var0, int var2, SteamMatchmakingKeyValuePair[] var3, int var4, long var5);

    private static native long requestLANServerList(long var0, int var2, long var3);

    private static native long requestFriendsServerList(long var0, int var2, SteamMatchmakingKeyValuePair[] var3, int var4, long var5);

    private static native long requestFavoritesServerList(long var0, int var2, SteamMatchmakingKeyValuePair[] var3, int var4, long var5);

    private static native long requestHistoryServerList(long var0, int var2, SteamMatchmakingKeyValuePair[] var3, int var4, long var5);

    private static native long requestSpectatorServerList(long var0, int var2, SteamMatchmakingKeyValuePair[] var3, int var4, long var5);

    private static native void releaseRequest(long var0, long var2);

    private static native boolean getServerDetails(long var0, long var2, int var4, SteamMatchmakingGameServerItem var5);

    private static native void cancelQuery(long var0, long var2);

    private static native void refreshQuery(long var0, long var2);

    private static native boolean isRefreshing(long var0, long var2);

    private static native int getServerCount(long var0, long var2);

    private static native void refreshServer(long var0, long var2, int var4);

    private static native int pingServer(long var0, int var2, short var3, long var4);

    private static native int playerDetails(long var0, int var2, short var3, long var4);

    private static native int serverRules(long var0, int var2, short var3, long var4);

    private static native void cancelServerQuery(long var0, int var2);
}

