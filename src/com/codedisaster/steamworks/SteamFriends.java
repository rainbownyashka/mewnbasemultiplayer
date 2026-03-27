/*
 * Decompiled with CFR 0.152.
 */
package com.codedisaster.steamworks;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAPICall;
import com.codedisaster.steamworks.SteamFriendsCallback;
import com.codedisaster.steamworks.SteamFriendsCallbackAdapter;
import com.codedisaster.steamworks.SteamID;
import com.codedisaster.steamworks.SteamInterface;
import java.util.Collection;

public class SteamFriends
extends SteamInterface {
    public SteamFriends(SteamFriendsCallback callback) {
        super(SteamAPI.getSteamFriendsPointer(), SteamFriends.createCallback(new SteamFriendsCallbackAdapter(callback)));
    }

    public String getPersonaName() {
        return SteamFriends.getPersonaName(this.pointer);
    }

    public SteamAPICall setPersonaName(String personaName) {
        return new SteamAPICall(SteamFriends.setPersonaName(this.pointer, this.callback, personaName));
    }

    public PersonaState getPersonaState() {
        return PersonaState.byOrdinal(SteamFriends.getPersonaState(this.pointer));
    }

    public int getFriendCount(FriendFlags friendFlag) {
        return SteamFriends.getFriendCount(this.pointer, friendFlag.bits);
    }

    public int getFriendCount(Collection<FriendFlags> friendFlags) {
        return SteamFriends.getFriendCount(this.pointer, FriendFlags.asBits(friendFlags));
    }

    public SteamID getFriendByIndex(int friend, FriendFlags friendFlag) {
        return new SteamID(SteamFriends.getFriendByIndex(this.pointer, friend, friendFlag.bits));
    }

    public SteamID getFriendByIndex(int friend, Collection<FriendFlags> friendFlags) {
        return new SteamID(SteamFriends.getFriendByIndex(this.pointer, friend, FriendFlags.asBits(friendFlags)));
    }

    public FriendRelationship getFriendRelationship(SteamID steamIDFriend) {
        return FriendRelationship.byOrdinal(SteamFriends.getFriendRelationship(this.pointer, steamIDFriend.handle));
    }

    public PersonaState getFriendPersonaState(SteamID steamIDFriend) {
        return PersonaState.byOrdinal(SteamFriends.getFriendPersonaState(this.pointer, steamIDFriend.handle));
    }

    public String getFriendPersonaName(SteamID steamIDFriend) {
        return SteamFriends.getFriendPersonaName(this.pointer, steamIDFriend.handle);
    }

    public boolean getFriendGamePlayed(SteamID steamIDFriend, FriendGameInfo friendGameInfo) {
        return SteamFriends.getFriendGamePlayed(this.pointer, steamIDFriend.handle, friendGameInfo);
    }

    public void setInGameVoiceSpeaking(SteamID steamID, boolean speaking) {
        SteamFriends.setInGameVoiceSpeaking(this.pointer, steamID.handle, speaking);
    }

    public int getSmallFriendAvatar(SteamID steamID) {
        return SteamFriends.getSmallFriendAvatar(this.pointer, steamID.handle);
    }

    public int getMediumFriendAvatar(SteamID steamID) {
        return SteamFriends.getMediumFriendAvatar(this.pointer, steamID.handle);
    }

    public int getLargeFriendAvatar(SteamID steamID) {
        return SteamFriends.getLargeFriendAvatar(this.pointer, steamID.handle);
    }

    public boolean requestUserInformation(SteamID steamID, boolean requireNameOnly) {
        return SteamFriends.requestUserInformation(this.pointer, steamID.handle, requireNameOnly);
    }

    public void activateGameOverlay(OverlayDialog dialog) {
        SteamFriends.activateGameOverlay(this.pointer, dialog.id);
    }

    public void activateGameOverlayToUser(OverlayToUserDialog dialog, SteamID steamID) {
        SteamFriends.activateGameOverlayToUser(this.pointer, dialog.id, steamID.handle);
    }

    public void activateGameOverlayToWebPage(String url) {
        SteamFriends.activateGameOverlayToWebPage(this.pointer, url);
    }

    public void activateGameOverlayToStore(int appID, OverlayToStoreFlag flag) {
        SteamFriends.activateGameOverlayToStore(this.pointer, appID, flag.ordinal());
    }

    public void activateGameOverlayInviteDialog(SteamID steamIDLobby) {
        SteamFriends.activateGameOverlayInviteDialog(this.pointer, steamIDLobby.handle);
    }

    public boolean setRichPresence(String key, String value) {
        return SteamFriends.setRichPresence(this.pointer, key, value != null ? value : "");
    }

    public void clearRichPresence() {
        SteamFriends.clearRichPresence(this.pointer);
    }

    public String getFriendRichPresence(SteamID steamIDFriend, String key) {
        return SteamFriends.getFriendRichPresence(this.pointer, steamIDFriend.handle, key);
    }

    public int getFriendRichPresenceKeyCount(SteamID steamIDFriend) {
        return SteamFriends.getFriendRichPresenceKeyCount(this.pointer, steamIDFriend.handle);
    }

    public String getFriendRichPresenceKeyByIndex(SteamID steamIDFriend, int index) {
        return SteamFriends.getFriendRichPresenceKeyByIndex(this.pointer, steamIDFriend.handle, index);
    }

    public void requestFriendRichPresence(SteamID steamIDFriend) {
        SteamFriends.requestFriendRichPresence(this.pointer, steamIDFriend.handle);
    }

    public boolean inviteUserToGame(SteamID steamIDFriend, String connectString) {
        return SteamFriends.inviteUserToGame(this.pointer, steamIDFriend.handle, connectString);
    }

    private static native long createCallback(SteamFriendsCallbackAdapter var0);

    private static native String getPersonaName(long var0);

    private static native long setPersonaName(long var0, long var2, String var4);

    private static native int getPersonaState(long var0);

    private static native int getFriendCount(long var0, int var2);

    private static native long getFriendByIndex(long var0, int var2, int var3);

    private static native int getFriendRelationship(long var0, long var2);

    private static native int getFriendPersonaState(long var0, long var2);

    private static native String getFriendPersonaName(long var0, long var2);

    private static native boolean getFriendGamePlayed(long var0, long var2, FriendGameInfo var4);

    private static native void setInGameVoiceSpeaking(long var0, long var2, boolean var4);

    private static native int getSmallFriendAvatar(long var0, long var2);

    private static native int getMediumFriendAvatar(long var0, long var2);

    private static native int getLargeFriendAvatar(long var0, long var2);

    private static native boolean requestUserInformation(long var0, long var2, boolean var4);

    private static native void activateGameOverlay(long var0, String var2);

    private static native void activateGameOverlayToUser(long var0, String var2, long var3);

    private static native void activateGameOverlayToWebPage(long var0, String var2);

    private static native void activateGameOverlayToStore(long var0, int var2, int var3);

    private static native void activateGameOverlayInviteDialog(long var0, long var2);

    private static native boolean setRichPresence(long var0, String var2, String var3);

    private static native void clearRichPresence(long var0);

    private static native String getFriendRichPresence(long var0, long var2, String var4);

    private static native int getFriendRichPresenceKeyCount(long var0, long var2);

    private static native String getFriendRichPresenceKeyByIndex(long var0, long var2, int var4);

    private static native void requestFriendRichPresence(long var0, long var2);

    private static native boolean inviteUserToGame(long var0, long var2, String var4);

    public static enum OverlayToStoreFlag {
        None,
        AddToCart,
        AddToCartAndShow;

    }

    public static enum OverlayToUserDialog {
        SteamID("steamid"),
        Chat("chat"),
        JoinTrade("jointrade"),
        Stats("stats"),
        Achievements("achievements"),
        FriendAdd("friendadd"),
        FriendRemove("friendremove"),
        FriendRequestAccept("friendrequestaccept"),
        FriendRequestIgnore("friendrequestignore");

        private final String id;

        private OverlayToUserDialog(String id) {
            this.id = id;
        }
    }

    public static enum OverlayDialog {
        Friends("Friends"),
        Community("Community"),
        Players("Players"),
        Settings("Settings"),
        OfficialGameGroup("OfficialGameGroup"),
        Stats("Stats"),
        Achievements("Achievements");

        private final String id;

        private OverlayDialog(String id) {
            this.id = id;
        }
    }

    public static class FriendGameInfo {
        private long gameID;
        private int gameIP;
        private short gamePort;
        private short queryPort;
        private long steamIDLobby;

        public long getGameID() {
            return this.gameID;
        }

        public int getGameIP() {
            return this.gameIP;
        }

        public short getGamePort() {
            return this.gamePort;
        }

        public short getQueryPort() {
            return this.queryPort;
        }

        public SteamID getSteamIDLobby() {
            return new SteamID(this.steamIDLobby);
        }
    }

    public static enum PersonaChange {
        Name(1),
        Status(2),
        ComeOnline(4),
        GoneOffline(8),
        GamePlayed(16),
        GameServer(32),
        Avatar(64),
        JoinedSource(128),
        LeftSource(256),
        RelationshipChanged(512),
        NameFirstSet(1024),
        FacebookInfo(2048),
        Nickname(4096),
        SteamLevel(8192);

        private final int bits;

        private PersonaChange(int bits) {
            this.bits = bits;
        }

        static boolean isSet(PersonaChange value, int bitMask) {
            return (value.bits & bitMask) == value.bits;
        }
    }

    public static enum FriendFlags {
        None(0),
        Blocked(1),
        FriendshipRequested(2),
        Immediate(4),
        ClanMember(8),
        OnGameServer(16),
        RequestingFriendship(128),
        RequestingInfo(256),
        Ignored(512),
        IgnoredFriend(1024),
        ChatMember(4096),
        All(65535);

        private final int bits;

        private FriendFlags(int bits) {
            this.bits = bits;
        }

        static int asBits(Collection<FriendFlags> friendFlags) {
            int bits = 0;
            for (FriendFlags flags : friendFlags) {
                bits |= flags.bits;
            }
            return bits;
        }
    }

    public static enum PersonaState {
        Offline,
        Online,
        Busy,
        Away,
        Snooze,
        LookingToTrade,
        LookingToPlay;

        private static final PersonaState[] values;

        static PersonaState byOrdinal(int personaState) {
            return values[personaState];
        }

        static {
            values = PersonaState.values();
        }
    }

    public static enum FriendRelationship {
        None,
        Blocked,
        Recipient,
        Friend,
        RequestInitiator,
        Ignored,
        IgnoredFriend,
        Suggested_DEPRECATED,
        Max;

        private static final FriendRelationship[] values;

        static FriendRelationship byOrdinal(int friendRelationship) {
            return values[friendRelationship];
        }

        static {
            values = FriendRelationship.values();
        }
    }
}

