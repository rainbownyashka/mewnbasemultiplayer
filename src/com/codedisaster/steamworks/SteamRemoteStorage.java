/*
 * Decompiled with CFR 0.152.
 */
package com.codedisaster.steamworks;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamAPICall;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamInterface;
import com.codedisaster.steamworks.SteamPublishedFileID;
import com.codedisaster.steamworks.SteamPublishedFileUpdateHandle;
import com.codedisaster.steamworks.SteamRemoteStorageCallback;
import com.codedisaster.steamworks.SteamRemoteStorageCallbackAdapter;
import com.codedisaster.steamworks.SteamUGCFileWriteStreamHandle;
import com.codedisaster.steamworks.SteamUGCHandle;
import java.nio.ByteBuffer;

public class SteamRemoteStorage
extends SteamInterface {
    public SteamRemoteStorage(SteamRemoteStorageCallback callback) {
        super(SteamAPI.getSteamRemoteStoragePointer(), SteamRemoteStorage.createCallback(new SteamRemoteStorageCallbackAdapter(callback)));
    }

    public boolean fileWrite(String file, ByteBuffer data) throws SteamException {
        if (!data.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        return SteamRemoteStorage.fileWrite(this.pointer, file, data, data.position(), data.remaining());
    }

    public boolean fileRead(String file, ByteBuffer buffer) throws SteamException {
        if (!buffer.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        return SteamRemoteStorage.fileRead(this.pointer, file, buffer, buffer.position(), buffer.remaining());
    }

    public SteamAPICall fileWriteAsync(String file, ByteBuffer data) throws SteamException {
        if (!data.isDirect()) {
            throw new SteamException("Direct buffer required!");
        }
        return new SteamAPICall(SteamRemoteStorage.fileWriteAsync(this.pointer, this.callback, file, data, data.position(), data.remaining()));
    }

    public SteamAPICall fileReadAsync(String file, int offset, int toRead) {
        return new SteamAPICall(SteamRemoteStorage.fileReadAsync(this.pointer, this.callback, file, offset, toRead));
    }

    public boolean fileReadAsyncComplete(SteamAPICall readCall, ByteBuffer buffer, int toRead) {
        return SteamRemoteStorage.fileReadAsyncComplete(this.pointer, readCall.handle, buffer, buffer.position(), toRead);
    }

    public boolean fileForget(String file) {
        return SteamRemoteStorage.fileForget(this.pointer, file);
    }

    public boolean fileDelete(String file) {
        return SteamRemoteStorage.fileDelete(this.pointer, file);
    }

    public SteamAPICall fileShare(String file) {
        return new SteamAPICall(SteamRemoteStorage.fileShare(this.pointer, this.callback, file));
    }

    public boolean setSyncPlatforms(String file, RemoteStoragePlatform remoteStoragePlatform) {
        return SteamRemoteStorage.setSyncPlatforms(this.pointer, file, remoteStoragePlatform.mask);
    }

    public SteamUGCFileWriteStreamHandle fileWriteStreamOpen(String name) {
        return new SteamUGCFileWriteStreamHandle(SteamRemoteStorage.fileWriteStreamOpen(this.pointer, name));
    }

    public boolean fileWriteStreamWriteChunk(SteamUGCFileWriteStreamHandle stream, ByteBuffer data) {
        return SteamRemoteStorage.fileWriteStreamWriteChunk(this.pointer, stream.handle, data, data.position(), data.remaining());
    }

    public boolean fileWriteStreamClose(SteamUGCFileWriteStreamHandle stream) {
        return SteamRemoteStorage.fileWriteStreamClose(this.pointer, stream.handle);
    }

    public boolean fileWriteStreamCancel(SteamUGCFileWriteStreamHandle stream) {
        return SteamRemoteStorage.fileWriteStreamCancel(this.pointer, stream.handle);
    }

    public boolean fileExists(String file) {
        return SteamRemoteStorage.fileExists(this.pointer, file);
    }

    public boolean filePersisted(String file) {
        return SteamRemoteStorage.filePersisted(this.pointer, file);
    }

    public int getFileSize(String file) {
        return SteamRemoteStorage.getFileSize(this.pointer, file);
    }

    public long getFileTimestamp(String file) {
        return SteamRemoteStorage.getFileTimestamp(this.pointer, file);
    }

    public RemoteStoragePlatform[] getSyncPlatforms(String file) {
        int mask = SteamRemoteStorage.getSyncPlatforms(this.pointer, file);
        return RemoteStoragePlatform.byMask(mask);
    }

    public int getFileCount() {
        return SteamRemoteStorage.getFileCount(this.pointer);
    }

    public String getFileNameAndSize(int index, int[] sizes) {
        return SteamRemoteStorage.getFileNameAndSize(this.pointer, index, sizes);
    }

    public boolean getQuota(long[] totalBytes, long[] availableBytes) {
        return SteamRemoteStorage.getQuota(this.pointer, totalBytes, availableBytes);
    }

    public boolean isCloudEnabledForAccount() {
        return SteamRemoteStorage.isCloudEnabledForAccount(this.pointer);
    }

    public boolean isCloudEnabledForApp() {
        return SteamRemoteStorage.isCloudEnabledForApp(this.pointer);
    }

    public void setCloudEnabledForApp(boolean enabled) {
        SteamRemoteStorage.setCloudEnabledForApp(this.pointer, enabled);
    }

    public SteamAPICall ugcDownload(SteamUGCHandle fileHandle, int priority) {
        return new SteamAPICall(SteamRemoteStorage.ugcDownload(this.pointer, this.callback, fileHandle.handle, priority));
    }

    public boolean getUGCDownloadProgress(SteamUGCHandle fileHandle, int[] bytesDownloaded, int[] bytesExpected) {
        return SteamRemoteStorage.getUGCDownloadProgress(this.pointer, fileHandle.handle, bytesDownloaded, bytesExpected);
    }

    public int ugcRead(SteamUGCHandle fileHandle, ByteBuffer buffer, int dataToRead, int offset, UGCReadAction action) {
        return SteamRemoteStorage.ugcRead(this.pointer, fileHandle.handle, buffer, buffer.position(), dataToRead, offset, action.ordinal());
    }

    public int getCachedUGCCount() {
        return SteamRemoteStorage.getCachedUGCCount(this.pointer);
    }

    public SteamUGCHandle getCachedUGCHandle(int cachedContent) {
        return new SteamUGCHandle(SteamRemoteStorage.getCachedUGCHandle(this.pointer, cachedContent));
    }

    public SteamAPICall publishWorkshopFile(String file, String previewFile, int consumerAppID, String title, String description, PublishedFileVisibility visibility, String[] tags, WorkshopFileType workshopFileType) {
        return new SteamAPICall(SteamRemoteStorage.publishWorkshopFile(this.pointer, this.callback, file, previewFile, consumerAppID, title, description, visibility.ordinal(), tags, tags != null ? tags.length : 0, workshopFileType.ordinal()));
    }

    public SteamPublishedFileUpdateHandle createPublishedFileUpdateRequest(SteamPublishedFileID publishedFileID) {
        return new SteamPublishedFileUpdateHandle(SteamRemoteStorage.createPublishedFileUpdateRequest(this.pointer, publishedFileID.handle));
    }

    public boolean updatePublishedFileFile(SteamPublishedFileUpdateHandle updateHandle, String file) {
        return SteamRemoteStorage.updatePublishedFileFile(this.pointer, updateHandle.handle, file);
    }

    public boolean updatePublishedFilePreviewFile(SteamPublishedFileUpdateHandle updateHandle, String previewFile) {
        return SteamRemoteStorage.updatePublishedFilePreviewFile(this.pointer, updateHandle.handle, previewFile);
    }

    public boolean updatePublishedFileTitle(SteamPublishedFileUpdateHandle updateHandle, String title) {
        return SteamRemoteStorage.updatePublishedFileTitle(this.pointer, updateHandle.handle, title);
    }

    public boolean updatePublishedFileDescription(SteamPublishedFileUpdateHandle updateHandle, String description) {
        return SteamRemoteStorage.updatePublishedFileDescription(this.pointer, updateHandle.handle, description);
    }

    public boolean updatePublishedFileVisibility(SteamPublishedFileUpdateHandle updateHandle, PublishedFileVisibility visibility) {
        return SteamRemoteStorage.updatePublishedFileVisibility(this.pointer, updateHandle.handle, visibility.ordinal());
    }

    public boolean updatePublishedFileTags(SteamPublishedFileUpdateHandle updateHandle, String[] tags) {
        return SteamRemoteStorage.updatePublishedFileTags(this.pointer, updateHandle.handle, tags, tags != null ? tags.length : 0);
    }

    public SteamAPICall commitPublishedFileUpdate(SteamPublishedFileUpdateHandle updateHandle) {
        return new SteamAPICall(SteamRemoteStorage.commitPublishedFileUpdate(this.pointer, this.callback, updateHandle.handle));
    }

    private static native long createCallback(SteamRemoteStorageCallbackAdapter var0);

    private static native boolean fileWrite(long var0, String var2, ByteBuffer var3, int var4, int var5);

    private static native boolean fileRead(long var0, String var2, ByteBuffer var3, int var4, int var5);

    private static native long fileWriteAsync(long var0, long var2, String var4, ByteBuffer var5, int var6, int var7);

    private static native long fileReadAsync(long var0, long var2, String var4, int var5, int var6);

    private static native boolean fileReadAsyncComplete(long var0, long var2, ByteBuffer var4, long var5, int var7);

    private static native boolean fileForget(long var0, String var2);

    private static native boolean fileDelete(long var0, String var2);

    private static native long fileShare(long var0, long var2, String var4);

    private static native boolean setSyncPlatforms(long var0, String var2, int var3);

    private static native long fileWriteStreamOpen(long var0, String var2);

    private static native boolean fileWriteStreamWriteChunk(long var0, long var2, ByteBuffer var4, int var5, int var6);

    private static native boolean fileWriteStreamClose(long var0, long var2);

    private static native boolean fileWriteStreamCancel(long var0, long var2);

    private static native boolean fileExists(long var0, String var2);

    private static native boolean filePersisted(long var0, String var2);

    private static native int getFileSize(long var0, String var2);

    private static native long getFileTimestamp(long var0, String var2);

    private static native int getSyncPlatforms(long var0, String var2);

    private static native int getFileCount(long var0);

    private static native String getFileNameAndSize(long var0, int var2, int[] var3);

    private static native boolean getQuota(long var0, long[] var2, long[] var3);

    private static native boolean isCloudEnabledForAccount(long var0);

    private static native boolean isCloudEnabledForApp(long var0);

    private static native void setCloudEnabledForApp(long var0, boolean var2);

    private static native long ugcDownload(long var0, long var2, long var4, int var6);

    private static native boolean getUGCDownloadProgress(long var0, long var2, int[] var4, int[] var5);

    private static native int ugcRead(long var0, long var2, ByteBuffer var4, int var5, int var6, int var7, int var8);

    private static native int getCachedUGCCount(long var0);

    private static native long getCachedUGCHandle(long var0, int var2);

    private static native long publishWorkshopFile(long var0, long var2, String var4, String var5, int var6, String var7, String var8, int var9, String[] var10, int var11, int var12);

    private static native long createPublishedFileUpdateRequest(long var0, long var2);

    private static native boolean updatePublishedFileFile(long var0, long var2, String var4);

    private static native boolean updatePublishedFilePreviewFile(long var0, long var2, String var4);

    private static native boolean updatePublishedFileTitle(long var0, long var2, String var4);

    private static native boolean updatePublishedFileDescription(long var0, long var2, String var4);

    private static native boolean updatePublishedFileVisibility(long var0, long var2, int var4);

    private static native boolean updatePublishedFileTags(long var0, long var2, String[] var4, int var5);

    private static native long commitPublishedFileUpdate(long var0, long var2, long var4);

    public static enum WorkshopFileType {
        Community,
        Microtransaction,
        Collection,
        Art,
        Video,
        Screenshot,
        Game,
        Software,
        Concept,
        WebGuide,
        IntegratedGuide,
        Merch,
        ControllerBinding,
        SteamworksAccessInvite,
        SteamVideo,
        GameManagedItem;

        private static final WorkshopFileType[] values;

        static WorkshopFileType byOrdinal(int ordinal) {
            return values[ordinal];
        }

        static {
            values = WorkshopFileType.values();
        }
    }

    public static enum PublishedFileVisibility {
        Public,
        FriendsOnly,
        Private;

    }

    public static enum UGCReadAction {
        ContinueReadingUntilFinished,
        ContinueReading,
        Close;

    }

    public static enum RemoteStoragePlatform {
        None(0),
        Windows(1),
        OSX(2),
        PS3(4),
        Linux(8),
        Reserved2(16),
        All(-1);

        private final int mask;
        private static final RemoteStoragePlatform[] values;

        private RemoteStoragePlatform(int mask) {
            this.mask = mask;
        }

        static RemoteStoragePlatform[] byMask(int mask) {
            int bits = Integer.bitCount(mask);
            RemoteStoragePlatform[] result = new RemoteStoragePlatform[bits];
            int idx = 0;
            for (RemoteStoragePlatform value : values) {
                if ((value.mask & mask) == 0) continue;
                result[idx++] = value;
            }
            return result;
        }

        static {
            values = RemoteStoragePlatform.values();
        }
    }
}

