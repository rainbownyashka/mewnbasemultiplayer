/*
 * Decompiled with CFR 0.152.
 */
package com.codedisaster.steamworks;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAPICall;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamInterface;
import com.codedisaster.steamworks.SteamMatchmakingCallback;
import com.codedisaster.steamworks.SteamMatchmakingCallbackAdapter;
import com.codedisaster.steamworks.SteamMatchmakingKeyValuePair;
import java.nio.ByteBuffer;

public class SteamMatchmaking
extends SteamInterface {
    public SteamMatchmaking(SteamMatchmakingCallback callback) {
        super(SteamAPI.getSteamMatchmakingPointer(), SteamMatchmaking.createCallback(new SteamMatchmakingCallbackAdapter(callback)));
    }

    public int getFavoriteGameCount() {
        return SteamMatchmaking.getFavoriteGameCount(this.pointer);
    }

    public boolean getFavoriteGame(int game, int[] appID, int[] ip, short[] connPort, short[] queryPort, int[] flags, int[] lastPlayedOnServer) {
        return SteamMatchmaking.getFavoriteGame(this.pointer, game, appID, ip, connPort, queryPort, flags, lastPlayedOnServer);
    }

    public int addFavoriteGame(int appID, int ip, short connPort, short queryPort, int flags, int lastPlayedOnServer) {
        return SteamMatchmaking.addFavoriteGame(this.pointer, appID, ip, connPort, queryPort, flags, lastPlayedOnServer);
    }

    public boolean removeFavoriteGame(int appID, int ip, short connPort, short queryPort, int flags) {
        return SteamMatchmaking.removeFavoriteGame(this.pointer, appID, ip, connPort, queryPort, flags);
    }

    public SteamAPICall requestLobbyList() {
        return new SteamAPICall(SteamMatchmaking.requestLobbyList(this.pointer, this.callback));
    }

    public void addRequestLobbyListStringFilter(String keyToMatch, String valueToMatch, LobbyComparison comparisonType) {
        SteamMatchmaking.addRequestLobbyListStringFilter(this.pointer, keyToMatch, valueToMatch, comparisonType.value);
    }

    public void addRequestLobbyListNumericalFilter(String keyToMatch, int valueToMatch, LobbyComparison comparisonType) {
        SteamMatchmaking.addRequestLobbyListNumericalFilter(this.pointer, keyToMatch, valueToMatch, comparisonType.value);
    }

    public void addRequestLobbyListNearValueFilter(String keyToMatch, int valueToBeCloseTo) {
        SteamMatchmaking.addRequestLobbyListNearValueFilter(this.pointer, keyToMatch, valueToBeCloseTo);
    }

    public void addRequestLobbyListFilterSlotsAvailable(int slotsAvailable) {
        SteamMatchmaking.addRequestLobbyListFilterSlotsAvailable(this.pointer, slotsAvailable);
    }

    public void addRequestLobbyListDistanceFilter(LobbyDistanceFilter lobbyDistanceFilter) {
        SteamMatchmaking.addRequestLobbyListDistanceFilter(this.pointer, lobbyDistanceFilter.ordinal());
    }

    public void addRequestLobbyListResultCountFilter(int maxResults) {
        SteamMatchmaking.addRequestLobbyListResultCountFilter(this.pointer, maxResults);
    }

    public void addRequestLobbyListCompatibleMembersFilter(SteamID steamIDLobby) {
        SteamMatchmaking.addRequestLobbyListCompatibleMembersFilter(this.pointer, steamIDLobby.handle);
    }

    public SteamID getLobbyByIndex(int lobby) {
        return new SteamID(SteamMatchmaking.getLobbyByIndex(this.pointer, lobby));
    }

    public SteamAPICall createLobby(LobbyType lobbyType, int maxMembers) {
        return new SteamAPICall(SteamMatchmaking.createLobby(this.pointer, this.callback, lobbyType.ordinal(), maxMembers));
    }

    public SteamAPICall joinLobby(SteamID steamIDLobby) {
        return new SteamAPICall(SteamMatchmaking.joinLobby(this.pointer, this.callback, steamIDLobby.handle));
    }

    public void leaveLobby(SteamID steamIDLobby) {
        SteamMatchmaking.leaveLobby(this.pointer, steamIDLobby.handle);
    }

    public boolean inviteUserToLobby(SteamID steamIDLobby, SteamID steamIDInvitee) {
        return SteamMatchmaking.inviteUserToLobby(this.pointer, steamIDLobby.handle, steamIDInvitee.handle);
    }

    public int getNumLobbyMembers(SteamID steamIDLobby) {
        return SteamMatchmaking.getNumLobbyMembers(this.pointer, steamIDLobby.handle);
    }

    public SteamID getLobbyMemberByIndex(SteamID steamIDLobby, int memberIndex) {
        return new SteamID(SteamMatchmaking.getLobbyMemberByIndex(this.pointer, steamIDLobby.handle, memberIndex));
    }

    public String getLobbyData(SteamID steamIDLobby, String key) {
        return SteamMatchmaking.getLobbyData(this.pointer, steamIDLobby.handle, key);
    }

    public boolean setLobbyData(SteamID steamIDLobby, String key, String value) {
        return SteamMatchmaking.setLobbyData(this.pointer, steamIDLobby.handle, key, value);
    }

    public boolean setLobbyData(SteamID steamIDLobby, SteamMatchmakingKeyValuePair keyValuePair) {
        return SteamMatchmaking.setLobbyData(this.pointer, steamIDLobby.handle, keyValuePair.getKey(), keyValuePair.getValue());
    }

    public String getLobbyMemberData(SteamID steamIDLobby, SteamID steamIDUser, String key) {
        return SteamMatchmaking.getLobbyMemberData(this.pointer, steamIDLobby.handle, steamIDUser.handle, key);
    }

    public void setLobbyMemberData(SteamID steamIDLobby, String key, String value) {
        SteamMatchmaking.setLobbyMemberData(this.pointer, steamIDLobby.handle, key, value);
    }

    public void setLobbyMemberData(SteamID steamIDLobby, SteamMatchmakingKeyValuePair keyValuePair) {
        SteamMatchmaking.setLobbyMemberData(this.pointer, steamIDLobby.handle, keyValuePair.getKey(), keyValuePair.getValue());
    }

    public int getLobbyDataCount(SteamID steamIDLobby) {
        return SteamMatchmaking.getLobbyDataCount(this.pointer, steamIDLobby.handle);
    }

    public boolean getLobbyDataByIndex(SteamID steamIDLobby, int lobbyDataIndex, SteamMatchmakingKeyValuePair keyValuePair) {
        return SteamMatchmaking.getLobbyDataByIndex(this.pointer, steamIDLobby.handle, lobbyDataIndex, keyValuePair);
    }

    public boolean deleteLobbyData(SteamID steamIDLobby, String key) {
        return SteamMatchmaking.deleteLobbyData(this.pointer, steamIDLobby.handle, key);
    }

    public boolean sendLobbyChatMsg(SteamID steamIDLobby, ByteBuffer data) throws SteamException {
        if (!data.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        return SteamMatchmaking.sendLobbyChatMsg(this.pointer, steamIDLobby.handle, data, data.position(), data.remaining());
    }

    public boolean sendLobbyChatMsg(SteamID steamIDLobby, String data) {
        return SteamMatchmaking.sendLobbyChatMsg(this.pointer, steamIDLobby.handle, data);
    }

    public int getLobbyChatEntry(SteamID steamIDLobby, int chatID, ChatEntry chatEntry, ByteBuffer dest) throws SteamException {
        if (!dest.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        return SteamMatchmaking.getLobbyChatEntry(this.pointer, steamIDLobby.handle, chatID, chatEntry, dest, dest.position(), dest.remaining());
    }

    public boolean requestLobbyData(SteamID steamIDLobby) {
        return SteamMatchmaking.requestLobbyData(this.pointer, steamIDLobby.handle);
    }

    public void setLobbyGameServer(SteamID steamIDLobby, int gameServerIP, short gameServerPort, SteamID steamIDGameServer) {
        SteamMatchmaking.setLobbyGameServer(this.pointer, steamIDLobby.handle, gameServerIP, gameServerPort, steamIDGameServer.handle);
    }

    public boolean getLobbyGameServer(SteamID steamIDLobby, int[] gameServerIP, short[] gameServerPort, SteamID steamIDGameServer) {
        long[] id = new long[1];
        if (SteamMatchmaking.getLobbyGameServer(this.pointer, steamIDLobby.handle, gameServerIP, gameServerPort, id)) {
            steamIDGameServer.handle = id[0];
            return true;
        }
        return false;
    }

    public boolean setLobbyMemberLimit(SteamID steamIDLobby, int maxMembers) {
        return SteamMatchmaking.setLobbyMemberLimit(this.pointer, steamIDLobby.handle, maxMembers);
    }

    public int getLobbyMemberLimit(SteamID steamIDLobby) {
        return SteamMatchmaking.getLobbyMemberLimit(this.pointer, steamIDLobby.handle);
    }

    public boolean setLobbyType(SteamID steamIDLobby, LobbyType lobbyType) {
        return SteamMatchmaking.setLobbyType(this.pointer, steamIDLobby.handle, lobbyType.ordinal());
    }

    public boolean setLobbyJoinable(SteamID steamIDLobby, boolean joinable) {
        return SteamMatchmaking.setLobbyJoinable(this.pointer, steamIDLobby.handle, joinable);
    }

    public SteamID getLobbyOwner(SteamID steamIDLobby) {
        return new SteamID(SteamMatchmaking.getLobbyOwner(this.pointer, steamIDLobby.handle));
    }

    public boolean setLobbyOwner(SteamID steamIDLobby, SteamID steamIDNewOwner) {
        return SteamMatchmaking.setLobbyOwner(this.pointer, steamIDLobby.handle, steamIDNewOwner.handle);
    }

    public boolean setLinkedLobby(SteamID steamIDLobby, SteamID steamIDLobbyDependent) {
        return SteamMatchmaking.setLinkedLobby(this.pointer, steamIDLobby.handle, steamIDLobbyDependent.handle);
    }

    private static native long createCallback(SteamMatchmakingCallbackAdapter var0);

    private static native int getFavoriteGameCount(long var0);

    private static native boolean getFavoriteGame(long var0, int var2, int[] var3, int[] var4, short[] var5, short[] var6, int[] var7, int[] var8);

    private static native int addFavoriteGame(long var0, int var2, int var3, short var4, short var5, int var6, int var7);

    private static native boolean removeFavoriteGame(long var0, int var2, int var3, short var4, short var5, int var6);

    private static native long requestLobbyList(long var0, long var2);

    private static native void addRequestLobbyListStringFilter(long var0, String var2, String var3, int var4);

    private static native void addRequestLobbyListNumericalFilter(long var0, String var2, int var3, int var4);

    private static native void addRequestLobbyListNearValueFilter(long var0, String var2, int var3);

    private static native void addRequestLobbyListFilterSlotsAvailable(long var0, int var2);

    private static native void addRequestLobbyListDistanceFilter(long var0, int var2);

    private static native void addRequestLobbyListResultCountFilter(long var0, int var2);

    private static native void addRequestLobbyListCompatibleMembersFilter(long var0, long var2);

    private static native long getLobbyByIndex(long var0, int var2);

    private static native long createLobby(long var0, long var2, int var4, int var5);

    private static native long joinLobby(long var0, long var2, long var4);

    private static native void leaveLobby(long var0, long var2);

    private static native boolean inviteUserToLobby(long var0, long var2, long var4);

    private static native int getNumLobbyMembers(long var0, long var2);

    private static native long getLobbyMemberByIndex(long var0, long var2, int var4);

    private static native String getLobbyData(long var0, long var2, String var4);

    private static native boolean setLobbyData(long var0, long var2, String var4, String var5);

    private static native String getLobbyMemberData(long var0, long var2, long var4, String var6);

    private static native void setLobbyMemberData(long var0, long var2, String var4, String var5);

    private static native int getLobbyDataCount(long var0, long var2);

    private static native boolean getLobbyDataByIndex(long var0, long var2, int var4, SteamMatchmakingKeyValuePair var5);

    private static native boolean deleteLobbyData(long var0, long var2, String var4);

    private static native boolean sendLobbyChatMsg(long var0, long var2, ByteBuffer var4, int var5, int var6);

    private static native boolean sendLobbyChatMsg(long var0, long var2, String var4);

    private static native int getLobbyChatEntry(long var0, long var2, int var4, ChatEntry var5, ByteBuffer var6, int var7, int var8);

    private static native boolean requestLobbyData(long var0, long var2);

    private static native void setLobbyGameServer(long var0, long var2, int var4, short var5, long var6);

    private static native boolean getLobbyGameServer(long var0, long var2, int[] var4, short[] var5, long[] var6);

    private static native boolean setLobbyMemberLimit(long var0, long var2, int var4);

    private static native int getLobbyMemberLimit(long var0, long var2);

    private static native boolean setLobbyType(long var0, long var2, int var4);

    private static native boolean setLobbyJoinable(long var0, long var2, boolean var4);

    private static native long getLobbyOwner(long var0, long var2);

    private static native boolean setLobbyOwner(long var0, long var2, long var4);

    private static native boolean setLinkedLobby(long var0, long var2, long var4);

    public static class ChatEntry {
        private long steamIDUser;
        private int chatEntryType;

        public SteamID getSteamIDUser() {
            return new SteamID(this.steamIDUser);
        }

        public ChatEntryType getChatEntryType() {
            return ChatEntryType.byCode(this.chatEntryType);
        }
    }

    public static enum ChatEntryType {
        Invalid(0),
        ChatMsg(1),
        Typing(2),
        InviteGame(3),
        Emote(4),
        LeftConversation(6),
        Entered(7),
        WasKicked(8),
        WasBanned(9),
        Disconnected(10),
        HistoricalChat(11),
        Reserved1(12),
        Reserved2(13),
        LinkBlocked(14);

        private final int code;
        private static final ChatEntryType[] values;

        private ChatEntryType(int code) {
            this.code = code;
        }

        static ChatEntryType byCode(int code) {
            for (ChatEntryType value : values) {
                if (value.code != code) continue;
                return value;
            }
            return Invalid;
        }

        static {
            values = ChatEntryType.values();
        }
    }

    public static enum ChatMemberStateChange {
        Entered(1),
        Left(2),
        Disconnected(4),
        Kicked(8),
        Banned(16);

        private final int bits;

        private ChatMemberStateChange(int bits) {
            this.bits = bits;
        }

        static boolean isSet(ChatMemberStateChange value, int bitMask) {
            return (value.bits & bitMask) == value.bits;
        }
    }

    public static enum ChatRoomEnterResponse {
        Success(1),
        DoesntExist(2),
        NotAllowed(3),
        Full(4),
        Error(5),
        Banned(6),
        Limited(7),
        ClanDisabled(8),
        CommunityBan(9),
        MemberBlockedYou(10),
        YouBlockedMember(11),
        RatelimitExceeded(15);

        private final int code;
        private static final ChatRoomEnterResponse[] values;

        private ChatRoomEnterResponse(int code) {
            this.code = code;
        }

        static ChatRoomEnterResponse byCode(int code) {
            for (ChatRoomEnterResponse value : values) {
                if (value.code != code) continue;
                return value;
            }
            return Error;
        }

        static {
            values = ChatRoomEnterResponse.values();
        }
    }

    public static enum LobbyDistanceFilter {
        Close,
        Default,
        Far,
        Worldwide;

    }

    public static enum LobbyComparison {
        EqualToOrLessThan(-2),
        LessThan(-1),
        Equal(0),
        GreaterThan(1),
        EqualToOrGreaterThan(2),
        NotEqual(3);

        private final int value;

        private LobbyComparison(int value) {
            this.value = value;
        }
    }

    public static enum LobbyType {
        Private,
        FriendsOnly,
        Public,
        Invisible;

    }
}

