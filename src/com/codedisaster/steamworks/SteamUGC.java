/*
 * Decompiled with CFR 0.152.
 */
package com.codedisaster.steamworks;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAPICall;
import com.codedisaster.steamworks.SteamInterface;
import com.codedisaster.steamworks.SteamPublishedFileID;
import com.codedisaster.steamworks.SteamRemoteStorage;
import com.codedisaster.steamworks.SteamUGCCallback;
import com.codedisaster.steamworks.SteamUGCCallbackAdapter;
import com.codedisaster.steamworks.SteamUGCDetails;
import com.codedisaster.steamworks.SteamUGCQuery;
import com.codedisaster.steamworks.SteamUGCUpdateHandle;
import java.util.Collection;
import java.util.EnumSet;

public class SteamUGC
extends SteamInterface {
    public SteamUGC(SteamUGCCallback callback) {
        super(SteamAPI.getSteamUGCPointer(), SteamUGC.createCallback(new SteamUGCCallbackAdapter(callback)));
    }

    public SteamUGCQuery createQueryUserUGCRequest(int accountID, UserUGCList listType, MatchingUGCType matchingType, UserUGCListSortOrder sortOrder, int creatorAppID, int consumerAppID, int page) {
        return new SteamUGCQuery(SteamUGC.createQueryUserUGCRequest(this.pointer, accountID, listType.ordinal(), matchingType.value, sortOrder.ordinal(), creatorAppID, consumerAppID, page));
    }

    public SteamUGCQuery createQueryAllUGCRequest(UGCQueryType queryType, MatchingUGCType matchingType, int creatorAppID, int consumerAppID, int page) {
        return new SteamUGCQuery(SteamUGC.createQueryAllUGCRequest(this.pointer, queryType.ordinal(), matchingType.value, creatorAppID, consumerAppID, page));
    }

    public SteamUGCQuery createQueryUGCDetailsRequest(SteamPublishedFileID publishedFileID) {
        long[] fileIDs = new long[]{publishedFileID.handle};
        return new SteamUGCQuery(SteamUGC.createQueryUGCDetailsRequest(this.pointer, fileIDs, 1));
    }

    public SteamUGCQuery createQueryUGCDetailsRequest(Collection<SteamPublishedFileID> publishedFileIDs) {
        int size = publishedFileIDs.size();
        long[] fileIDs = new long[size];
        int index = 0;
        for (SteamPublishedFileID fileID : publishedFileIDs) {
            fileIDs[index++] = fileID.handle;
        }
        return new SteamUGCQuery(SteamUGC.createQueryUGCDetailsRequest(this.pointer, fileIDs, size));
    }

    public SteamAPICall sendQueryUGCRequest(SteamUGCQuery query) {
        return new SteamAPICall(SteamUGC.sendQueryUGCRequest(this.pointer, this.callback, query.handle));
    }

    public boolean getQueryUGCResult(SteamUGCQuery query, int index, SteamUGCDetails details) {
        return SteamUGC.getQueryUGCResult(this.pointer, query.handle, index, details);
    }

    public String getQueryUGCPreviewURL(SteamUGCQuery query, int index) {
        return SteamUGC.getQueryUGCPreviewURL(this.pointer, query.handle, index);
    }

    public String getQueryUGCMetadata(SteamUGCQuery query, int index) {
        return SteamUGC.getQueryUGCMetadata(this.pointer, query.handle, index);
    }

    public long getQueryUGCStatistic(SteamUGCQuery query, int index, ItemStatistic statType) {
        return SteamUGC.getQueryUGCStatistic(this.pointer, query.handle, index, statType.ordinal());
    }

    public int getQueryUGCNumAdditionalPreviews(SteamUGCQuery query, int index) {
        return SteamUGC.getQueryUGCNumAdditionalPreviews(this.pointer, query.handle, index);
    }

    public boolean getQueryUGCAdditionalPreview(SteamUGCQuery query, int index, int previewIndex, ItemAdditionalPreview previewInfo) {
        return SteamUGC.getQueryUGCAdditionalPreview(this.pointer, query.handle, index, previewIndex, previewInfo);
    }

    public int getQueryUGCNumKeyValueTags(SteamUGCQuery query, int index) {
        return SteamUGC.getQueryUGCNumKeyValueTags(this.pointer, query.handle, index);
    }

    public boolean getQueryUGCKeyValueTag(SteamUGCQuery query, int index, int keyValueTagIndex, String[] keyAndValue) {
        return SteamUGC.getQueryUGCKeyValueTag(this.pointer, query.handle, index, keyValueTagIndex, keyAndValue);
    }

    public boolean releaseQueryUserUGCRequest(SteamUGCQuery query) {
        return SteamUGC.releaseQueryUserUGCRequest(this.pointer, query.handle);
    }

    public boolean addRequiredTag(SteamUGCQuery query, String tagName) {
        return SteamUGC.addRequiredTag(this.pointer, query.handle, tagName);
    }

    public boolean addExcludedTag(SteamUGCQuery query, String tagName) {
        return SteamUGC.addExcludedTag(this.pointer, query.handle, tagName);
    }

    public boolean setReturnOnlyIDs(SteamUGCQuery query, boolean returnOnlyIDs) {
        return SteamUGC.setReturnOnlyIDs(this.pointer, query.handle, returnOnlyIDs);
    }

    public boolean setReturnKeyValueTags(SteamUGCQuery query, boolean returnKeyValueTags) {
        return SteamUGC.setReturnKeyValueTags(this.pointer, query.handle, returnKeyValueTags);
    }

    public boolean setReturnLongDescription(SteamUGCQuery query, boolean returnLongDescription) {
        return SteamUGC.setReturnLongDescription(this.pointer, query.handle, returnLongDescription);
    }

    public boolean setReturnMetadata(SteamUGCQuery query, boolean returnMetadata) {
        return SteamUGC.setReturnMetadata(this.pointer, query.handle, returnMetadata);
    }

    public boolean setReturnChildren(SteamUGCQuery query, boolean returnChildren) {
        return SteamUGC.setReturnChildren(this.pointer, query.handle, returnChildren);
    }

    public boolean setReturnAdditionalPreviews(SteamUGCQuery query, boolean returnAdditionalPreviews) {
        return SteamUGC.setReturnAdditionalPreviews(this.pointer, query.handle, returnAdditionalPreviews);
    }

    public boolean setReturnTotalOnly(SteamUGCQuery query, boolean returnTotalOnly) {
        return SteamUGC.setReturnTotalOnly(this.pointer, query.handle, returnTotalOnly);
    }

    public boolean setReturnPlaytimeStats(SteamUGCQuery query, int days) {
        return SteamUGC.setReturnPlaytimeStats(this.pointer, query.handle, days);
    }

    public boolean setLanguage(SteamUGCQuery query, String language) {
        return SteamUGC.setLanguage(this.pointer, query.handle, language);
    }

    public boolean setAllowCachedResponse(SteamUGCQuery query, int maxAgeSeconds) {
        return SteamUGC.setAllowCachedResponse(this.pointer, query.handle, maxAgeSeconds);
    }

    public boolean setCloudFileNameFilter(SteamUGCQuery query, String matchCloudFileName) {
        return SteamUGC.setCloudFileNameFilter(this.pointer, query.handle, matchCloudFileName);
    }

    public boolean setMatchAnyTag(SteamUGCQuery query, boolean matchAnyTag) {
        return SteamUGC.setMatchAnyTag(this.pointer, query.handle, matchAnyTag);
    }

    public boolean setSearchText(SteamUGCQuery query, String searchText) {
        return SteamUGC.setSearchText(this.pointer, query.handle, searchText);
    }

    public boolean setRankedByTrendDays(SteamUGCQuery query, int days) {
        return SteamUGC.setRankedByTrendDays(this.pointer, query.handle, days);
    }

    public boolean addRequiredKeyValueTag(SteamUGCQuery query, String key, String value) {
        return SteamUGC.addRequiredKeyValueTag(this.pointer, query.handle, key, value);
    }

    @Deprecated
    public SteamAPICall requestUGCDetails(SteamPublishedFileID publishedFileID, int maxAgeSeconds) {
        return new SteamAPICall(SteamUGC.requestUGCDetails(this.pointer, this.callback, publishedFileID.handle, maxAgeSeconds));
    }

    public SteamAPICall createItem(int consumerAppID, SteamRemoteStorage.WorkshopFileType fileType) {
        return new SteamAPICall(SteamUGC.createItem(this.pointer, this.callback, consumerAppID, fileType.ordinal()));
    }

    public SteamUGCUpdateHandle startItemUpdate(int consumerAppID, SteamPublishedFileID publishedFileID) {
        return new SteamUGCUpdateHandle(SteamUGC.startItemUpdate(this.pointer, consumerAppID, publishedFileID.handle));
    }

    public boolean setItemTitle(SteamUGCUpdateHandle update, String title) {
        return SteamUGC.setItemTitle(this.pointer, update.handle, title);
    }

    public boolean setItemDescription(SteamUGCUpdateHandle update, String description) {
        return SteamUGC.setItemDescription(this.pointer, update.handle, description);
    }

    public boolean setItemUpdateLanguage(SteamUGCUpdateHandle update, String language) {
        return SteamUGC.setItemUpdateLanguage(this.pointer, update.handle, language);
    }

    public boolean setItemMetadata(SteamUGCUpdateHandle update, String metaData) {
        return SteamUGC.setItemMetadata(this.pointer, update.handle, metaData);
    }

    public boolean setItemVisibility(SteamUGCUpdateHandle update, SteamRemoteStorage.PublishedFileVisibility visibility) {
        return SteamUGC.setItemVisibility(this.pointer, update.handle, visibility.ordinal());
    }

    public boolean setItemTags(SteamUGCUpdateHandle update, String[] tags) {
        return SteamUGC.setItemTags(this.pointer, update.handle, tags, tags.length);
    }

    public boolean setItemContent(SteamUGCUpdateHandle update, String contentFolder) {
        return SteamUGC.setItemContent(this.pointer, update.handle, contentFolder);
    }

    public boolean setItemPreview(SteamUGCUpdateHandle update, String previewFile) {
        return SteamUGC.setItemPreview(this.pointer, update.handle, previewFile);
    }

    public boolean removeItemKeyValueTags(SteamUGCUpdateHandle update, String key) {
        return SteamUGC.removeItemKeyValueTags(this.pointer, update.handle, key);
    }

    public boolean addItemKeyValueTag(SteamUGCUpdateHandle update, String key, String value) {
        return SteamUGC.addItemKeyValueTag(this.pointer, update.handle, key, value);
    }

    public SteamAPICall submitItemUpdate(SteamUGCUpdateHandle update, String changeNote) {
        return new SteamAPICall(SteamUGC.submitItemUpdate(this.pointer, this.callback, update.handle, changeNote));
    }

    public ItemUpdateStatus getItemUpdateProgress(SteamUGCUpdateHandle update, ItemUpdateInfo updateInfo) {
        long[] values = new long[2];
        ItemUpdateStatus status = ItemUpdateStatus.byOrdinal(SteamUGC.getItemUpdateProgress(this.pointer, update.handle, values));
        updateInfo.bytesProcessed = values[0];
        updateInfo.bytesTotal = values[1];
        return status;
    }

    public SteamAPICall setUserItemVote(SteamPublishedFileID publishedFileID, boolean voteUp) {
        return new SteamAPICall(SteamUGC.setUserItemVote(this.pointer, this.callback, publishedFileID.handle, voteUp));
    }

    public SteamAPICall getUserItemVote(SteamPublishedFileID publishedFileID) {
        return new SteamAPICall(SteamUGC.getUserItemVote(this.pointer, this.callback, publishedFileID.handle));
    }

    public SteamAPICall addItemToFavorites(int appID, SteamPublishedFileID publishedFileID) {
        return new SteamAPICall(SteamUGC.addItemToFavorites(this.pointer, this.callback, appID, publishedFileID.handle));
    }

    public SteamAPICall removeItemFromFavorites(int appID, SteamPublishedFileID publishedFileID) {
        return new SteamAPICall(SteamUGC.removeItemFromFavorites(this.pointer, this.callback, appID, publishedFileID.handle));
    }

    public SteamAPICall subscribeItem(SteamPublishedFileID publishedFileID) {
        return new SteamAPICall(SteamUGC.subscribeItem(this.pointer, this.callback, publishedFileID.handle));
    }

    public SteamAPICall unsubscribeItem(SteamPublishedFileID publishedFileID) {
        return new SteamAPICall(SteamUGC.unsubscribeItem(this.pointer, this.callback, publishedFileID.handle));
    }

    public int getNumSubscribedItems() {
        return SteamUGC.getNumSubscribedItems(this.pointer);
    }

    public int getSubscribedItems(SteamPublishedFileID[] publishedFileIds) {
        long[] ids = new long[publishedFileIds.length];
        int nb = SteamUGC.getSubscribedItems(this.pointer, ids, publishedFileIds.length);
        for (int i = 0; i < nb; ++i) {
            publishedFileIds[i] = new SteamPublishedFileID(ids[i]);
        }
        return nb;
    }

    public Collection<ItemState> getItemState(SteamPublishedFileID publishedFileID) {
        return ItemState.fromBits(SteamUGC.getItemState(this.pointer, publishedFileID.handle));
    }

    public boolean getItemInstallInfo(SteamPublishedFileID publishedFileID, ItemInstallInfo installInfo) {
        return SteamUGC.getItemInstallInfo(this.pointer, publishedFileID.handle, installInfo);
    }

    public boolean getItemDownloadInfo(SteamPublishedFileID publishedFileID, ItemDownloadInfo downloadInfo) {
        long[] values = new long[2];
        if (SteamUGC.getItemDownloadInfo(this.pointer, publishedFileID.handle, values)) {
            downloadInfo.bytesDownloaded = values[0];
            downloadInfo.bytesTotal = values[1];
            return true;
        }
        return false;
    }

    public SteamAPICall deleteItem(SteamPublishedFileID publishedFileID) {
        return new SteamAPICall(SteamUGC.deleteItem(this.pointer, this.callback, publishedFileID.handle));
    }

    public boolean downloadItem(SteamPublishedFileID publishedFileID, boolean highPriority) {
        return SteamUGC.downloadItem(this.pointer, publishedFileID.handle, highPriority);
    }

    public boolean initWorkshopForGameServer(int workshopDepotID, String folder) {
        return SteamUGC.initWorkshopForGameServer(this.pointer, workshopDepotID, folder);
    }

    public void suspendDownloads(boolean suspend) {
        SteamUGC.suspendDownloads(this.pointer, suspend);
    }

    public SteamAPICall startPlaytimeTracking(SteamPublishedFileID[] publishedFileIDs) {
        long[] ids = new long[publishedFileIDs.length];
        for (int i = 0; i < ids.length; ++i) {
            ids[i] = publishedFileIDs[i].handle;
        }
        return new SteamAPICall(SteamUGC.startPlaytimeTracking(this.pointer, this.callback, ids, ids.length));
    }

    public SteamAPICall stopPlaytimeTracking(SteamPublishedFileID[] publishedFileIDs) {
        long[] ids = new long[publishedFileIDs.length];
        for (int i = 0; i < ids.length; ++i) {
            ids[i] = publishedFileIDs[i].handle;
        }
        return new SteamAPICall(SteamUGC.stopPlaytimeTracking(this.pointer, this.callback, ids, ids.length));
    }

    public SteamAPICall stopPlaytimeTrackingForAllItems() {
        return new SteamAPICall(SteamUGC.stopPlaytimeTrackingForAllItems(this.pointer, this.callback));
    }

    private static native long createCallback(SteamUGCCallbackAdapter var0);

    private static native long createQueryUserUGCRequest(long var0, int var2, int var3, int var4, int var5, int var6, int var7, int var8);

    private static native long createQueryAllUGCRequest(long var0, int var2, int var3, int var4, int var5, int var6);

    private static native long createQueryUGCDetailsRequest(long var0, long[] var2, int var3);

    private static native long sendQueryUGCRequest(long var0, long var2, long var4);

    private static native boolean getQueryUGCResult(long var0, long var2, int var4, SteamUGCDetails var5);

    private static native String getQueryUGCPreviewURL(long var0, long var2, int var4);

    private static native String getQueryUGCMetadata(long var0, long var2, int var4);

    private static native long getQueryUGCStatistic(long var0, long var2, int var4, int var5);

    private static native int getQueryUGCNumAdditionalPreviews(long var0, long var2, int var4);

    private static native boolean getQueryUGCAdditionalPreview(long var0, long var2, int var4, int var5, ItemAdditionalPreview var6);

    private static native int getQueryUGCNumKeyValueTags(long var0, long var2, int var4);

    private static native boolean getQueryUGCKeyValueTag(long var0, long var2, int var4, int var5, String[] var6);

    private static native boolean releaseQueryUserUGCRequest(long var0, long var2);

    private static native boolean addRequiredTag(long var0, long var2, String var4);

    private static native boolean addExcludedTag(long var0, long var2, String var4);

    private static native boolean setReturnOnlyIDs(long var0, long var2, boolean var4);

    private static native boolean setReturnKeyValueTags(long var0, long var2, boolean var4);

    private static native boolean setReturnLongDescription(long var0, long var2, boolean var4);

    private static native boolean setReturnMetadata(long var0, long var2, boolean var4);

    private static native boolean setReturnChildren(long var0, long var2, boolean var4);

    private static native boolean setReturnAdditionalPreviews(long var0, long var2, boolean var4);

    private static native boolean setReturnTotalOnly(long var0, long var2, boolean var4);

    private static native boolean setReturnPlaytimeStats(long var0, long var2, int var4);

    private static native boolean setLanguage(long var0, long var2, String var4);

    private static native boolean setAllowCachedResponse(long var0, long var2, int var4);

    private static native boolean setCloudFileNameFilter(long var0, long var2, String var4);

    private static native boolean setMatchAnyTag(long var0, long var2, boolean var4);

    private static native boolean setSearchText(long var0, long var2, String var4);

    private static native boolean setRankedByTrendDays(long var0, long var2, int var4);

    private static native boolean addRequiredKeyValueTag(long var0, long var2, String var4, String var5);

    private static native long requestUGCDetails(long var0, long var2, long var4, int var6);

    private static native long createItem(long var0, long var2, int var4, int var5);

    private static native long startItemUpdate(long var0, int var2, long var3);

    private static native boolean setItemTitle(long var0, long var2, String var4);

    private static native boolean setItemDescription(long var0, long var2, String var4);

    private static native boolean setItemUpdateLanguage(long var0, long var2, String var4);

    private static native boolean setItemMetadata(long var0, long var2, String var4);

    private static native boolean setItemVisibility(long var0, long var2, int var4);

    private static native boolean setItemTags(long var0, long var2, String[] var4, int var5);

    private static native boolean setItemContent(long var0, long var2, String var4);

    private static native boolean setItemPreview(long var0, long var2, String var4);

    private static native boolean removeItemKeyValueTags(long var0, long var2, String var4);

    private static native boolean addItemKeyValueTag(long var0, long var2, String var4, String var5);

    private static native long submitItemUpdate(long var0, long var2, long var4, String var6);

    private static native int getItemUpdateProgress(long var0, long var2, long[] var4);

    private static native long setUserItemVote(long var0, long var2, long var4, boolean var6);

    private static native long getUserItemVote(long var0, long var2, long var4);

    private static native long addItemToFavorites(long var0, long var2, int var4, long var5);

    private static native long removeItemFromFavorites(long var0, long var2, int var4, long var5);

    private static native long subscribeItem(long var0, long var2, long var4);

    private static native long unsubscribeItem(long var0, long var2, long var4);

    private static native int getNumSubscribedItems(long var0);

    private static native int getSubscribedItems(long var0, long[] var2, int var3);

    private static native int getItemState(long var0, long var2);

    private static native boolean getItemInstallInfo(long var0, long var2, ItemInstallInfo var4);

    private static native boolean getItemDownloadInfo(long var0, long var2, long[] var4);

    private static native long deleteItem(long var0, long var2, long var4);

    private static native boolean downloadItem(long var0, long var2, boolean var4);

    private static native boolean initWorkshopForGameServer(long var0, int var2, String var3);

    private static native void suspendDownloads(long var0, boolean var2);

    private static native long startPlaytimeTracking(long var0, long var2, long[] var4, int var5);

    private static native long stopPlaytimeTracking(long var0, long var2, long[] var4, int var5);

    private static native long stopPlaytimeTrackingForAllItems(long var0, long var2);

    public static class ItemAdditionalPreview {
        private String urlOrVideoID;
        private String originalFileName;
        private int previewType;

        public String getUrlOrVideoID() {
            return this.urlOrVideoID;
        }

        public String getOriginalFileName() {
            return this.originalFileName;
        }

        public ItemPreviewType getPreviewType() {
            return ItemPreviewType.byValue(this.previewType);
        }
    }

    public static class ItemDownloadInfo {
        long bytesDownloaded;
        long bytesTotal;

        public long getBytesDownloaded() {
            return this.bytesDownloaded;
        }

        public long getBytesTotal() {
            return this.bytesTotal;
        }
    }

    public static class ItemInstallInfo {
        private String folder;
        private int sizeOnDisk;

        public String getFolder() {
            return this.folder;
        }

        public int getSizeOnDisk() {
            return this.sizeOnDisk;
        }
    }

    public static enum ItemPreviewType {
        Image(0),
        YouTubeVideo(1),
        Sketchfab(2),
        EnvironmentMap_HorizontalCross(3),
        EnvironmentMap_LatLong(4),
        ReservedMax(255),
        UnknownPreviewType_NotImplementedByAPI(-1);

        private final int value;
        private static final ItemPreviewType[] values;

        private ItemPreviewType(int value) {
            this.value = value;
        }

        static ItemPreviewType byValue(int value) {
            for (ItemPreviewType type : values) {
                if (type.value != value) continue;
                return type;
            }
            return UnknownPreviewType_NotImplementedByAPI;
        }

        static {
            values = ItemPreviewType.values();
        }
    }

    public static enum ItemStatistic {
        NumSubscriptions,
        NumFavorites,
        NumFollowers,
        NumUniqueSubscriptions,
        NumUniqueFavorites,
        NumUniqueFollowers,
        NumUniqueWebsiteViews,
        ReportScore,
        NumSecondsPlayed,
        NumPlaytimeSessions,
        NumComments,
        NumSecondsPlayedDuringTimePeriod,
        NumPlaytimeSessionsDuringTimePeriod;

    }

    public static enum ItemState {
        None(0),
        Subscribed(1),
        LegacyItem(2),
        Installed(4),
        NeedsUpdate(8),
        Downloading(16),
        DownloadPending(32);

        private final int bits;
        private static final ItemState[] values;

        private ItemState(int bits) {
            this.bits = bits;
        }

        static Collection<ItemState> fromBits(int bits) {
            EnumSet<ItemState> itemStates = EnumSet.noneOf(ItemState.class);
            for (ItemState itemState : values) {
                if ((bits & itemState.bits) != itemState.bits) continue;
                itemStates.add(itemState);
            }
            return itemStates;
        }

        static {
            values = ItemState.values();
        }
    }

    public static class ItemUpdateInfo {
        long bytesProcessed;
        long bytesTotal;

        public long getBytesProcessed() {
            return this.bytesProcessed;
        }

        public long getBytesTotal() {
            return this.bytesTotal;
        }
    }

    public static enum ItemUpdateStatus {
        Invalid,
        PreparingConfig,
        PreparingContent,
        UploadingContent,
        UploadingPreviewFile,
        CommittingChanges;

        private static final ItemUpdateStatus[] values;

        static ItemUpdateStatus byOrdinal(int value) {
            return values[value];
        }

        static {
            values = ItemUpdateStatus.values();
        }
    }

    public static enum UGCQueryType {
        RankedByVote,
        RankedByPublicationDate,
        AcceptedForGameRankedByAcceptanceDate,
        RankedByTrend,
        FavoritedByFriendsRankedByPublicationDate,
        CreatedByFriendsRankedByPublicationDate,
        RankedByNumTimesReported,
        CreatedByFollowedUsersRankedByPublicationDate,
        NotYetRated,
        RankedByTotalVotesAsc,
        RankedByVotesUp,
        RankedByTextSearch,
        RankedByTotalUniqueSubscriptions,
        RankedByPlaytimeTrend,
        RankedByTotalPlaytime,
        RankedByAveragePlaytimeTrend,
        RankedByLifetimeAveragePlaytime,
        RankedByPlaytimeSessionsTrend,
        RankedByLifetimePlaytimeSessions;

    }

    public static enum UserUGCListSortOrder {
        CreationOrderDesc,
        CreationOrderAsc,
        TitleAsc,
        LastUpdatedDesc,
        SubscriptionDateDesc,
        VoteScoreDesc,
        ForModeration;

    }

    public static enum MatchingUGCType {
        Items(0),
        ItemsMtx(1),
        ItemsReadyToUse(2),
        Collections(3),
        Artwork(4),
        Videos(5),
        Screenshots(6),
        AllGuides(7),
        WebGuides(8),
        IntegratedGuides(9),
        UsableInGame(10),
        ControllerBindings(11),
        GameManagedItems(12),
        All(-1);

        private final int value;

        private MatchingUGCType(int value) {
            this.value = value;
        }
    }

    public static enum UserUGCList {
        Published,
        VotedOn,
        VotedUp,
        VotedDown,
        WillVoteLater,
        Favorited,
        Subscribed,
        UsedOrPlayed,
        Followed;

    }
}

