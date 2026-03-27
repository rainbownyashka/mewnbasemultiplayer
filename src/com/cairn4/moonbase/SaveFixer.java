/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.GameLoader;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.worlddata.BehaviorData;
import com.cairn4.moonbase.worlddata.ChunkData;
import com.cairn4.moonbase.worlddata.GameSaveData;
import com.cairn4.moonbase.worlddata.GroundTileData;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import com.cairn4.moonbase.worlddata.TileData;
import com.cairn4.moonbase.worlddata.WorldData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SaveFixer
extends Observable {
    public int processedChunks;

    public static void moveSaveFolder() {
        FileHandle oldFolder = Gdx.files.local("world");
        FileHandle newDestination = Gdx.files.local("saves/");
        if (oldFolder.exists() && oldFolder.isDirectory()) {
            Gdx.app.log("MewnBase", "Found old \"world\" folder, moving it into the Saves folder.");
            oldFolder.moveTo(newDestination);
            FileHandle gameSaveFile = Gdx.files.local("saves/world/gameSave.json");
            if (gameSaveFile.exists()) {
                Json gameJson = new Json();
                gameJson.setIgnoreDeprecated(true);
                gameJson.setIgnoreUnknownFields(true);
                String fileTextGame = gameSaveFile.readString();
                GameSaveData gsd = gameJson.fromJson(GameSaveData.class, fileTextGame);
                if (gsd.saveFolder == null) {
                    gsd.saveFolder = "world";
                }
                if (gsd.playerName == null) {
                    gsd.playerName = "Cmdr. Cat";
                }
                if (gsd.playerData.characterSuitColor == null) {
                    gsd.playerData.characterSuitColor = "ee662f";
                }
                gameSaveFile.writeString(gameJson.prettyPrint(gsd), false);
            }
        }
    }

    public boolean fixGame() {
        this.backup();
        boolean success = true;
        this.processedChunks = 0;
        SaveFixer.entityClassRenamingFix("saves/" + MoonBase.currentSaveFolder + "/gameSave.json", false);
        GameSaveData gsd = GameLoader.getGameSaveData(MoonBase.currentSaveFolder);
        if (gsd != null) {
            Gdx.app.log("MewnBase", "SaveFixer: Starting to update game save: " + MoonBase.currentSaveFolder);
            this.setChanged();
            this.notifyObservers("startFixing");
            if (gsd.saveDataVersion < 7) {
                ArrayList<FileHandle> chunkFiles = SaveFixer.getAllChunkFiles();
                for (FileHandle chunkFile : chunkFiles) {
                    String chunkFileText = chunkFile.readString("ISO-8859-1");
                    if (gsd.saveDataVersion < 5) {
                        chunkFileText = SaveFixer.stripItemsToCollect(chunkFileText);
                    }
                    chunkFileText = this.preloadFix(chunkFileText);
                    chunkFile.writeString(chunkFileText, false);
                }
                this.convertToSingleWorldData(gsd);
            }
            FileHandle worldDataFile = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/worldData.json");
            String worldDataString = worldDataFile.readString("ISO-8859-1");
            Json worldDataJson = new Json();
            worldDataJson.setIgnoreDeprecated(true);
            worldDataJson.setIgnoreUnknownFields(true);
            WorldData wd = worldDataJson.fromJson(WorldData.class, worldDataString);
            for (ChunkData cd : wd.chunkDataMap.values()) {
                this.fixChunk(gsd.saveDataVersion, cd);
            }
            GameLoader.writeWorldDataFile(wd);
            if (gsd.saveDataVersion < 6) {
                SaveFixer.unlockAdvancedPowerTech(gsd);
            }
            gsd.saveDataVersion = 9;
            GameLoader.writeGameSaveData(gsd, "");
            this.setChanged();
            this.notifyObservers("done");
        } else {
            success = false;
        }
        return true;
    }

    public static ArrayList<String> testEntityNameFix(ArrayList<String> saveFilesWithErrors) {
        ArrayList<String> possiblyFixable = new ArrayList<String>();
        MoonBase.log("-----------------");
        MoonBase.log("SaveFixer: Testing entity fix for " + saveFilesWithErrors.size() + " saves.");
        for (String gameSaveFile : saveFilesWithErrors) {
            FileHandle gameData = Gdx.files.local(gameSaveFile);
            String fileText = gameData.readString("ISO-8859-1");
            int dataVersion = SaveFixer.findSaveDataVersionNumber(fileText);
            if (dataVersion == -1 || dataVersion >= 8) continue;
            MoonBase.log("SaveFixer: " + gameSaveFile + " might be a candidate to fix.");
            boolean couldFix = SaveFixer.entityClassRenamingFix(gameSaveFile, true);
            if (!couldFix) continue;
            possiblyFixable.add(gameSaveFile);
        }
        return possiblyFixable;
    }

    public static void entityNameFix(ArrayList<String> saveFiles) {
        MoonBase.log("-----------------");
        MoonBase.log("SaveFixer: Performing entity fix for " + saveFiles.size() + " saves.");
        for (String gameSaveFile : saveFiles) {
            FileHandle saveRev2;
            FileHandle saveRev1;
            SaveFixer.backup(gameSaveFile);
            FileHandle gameData = Gdx.files.local(gameSaveFile);
            String fileText = gameData.readString("ISO-8859-1");
            SaveFixer.entityClassRenamingFix(gameSaveFile, false);
            FileHandle folder = gameData.parent();
            FileHandle prevSave1Folder = Gdx.files.local(folder.path() + "/prevSave1");
            if (prevSave1Folder.exists() && (saveRev1 = Gdx.files.local(prevSave1Folder.path() + "gameSave.json")).exists()) {
                MoonBase.log("SaveFixer -> save revision 1 might have bad entities...");
                SaveFixer.entityClassRenamingFix(saveRev1.path(), false);
            }
            FileHandle prevSave2Folder = Gdx.files.local(folder.path() + "/prevSave2");
            if (!prevSave1Folder.exists() || !(saveRev2 = Gdx.files.local(prevSave1Folder.path() + "gameSave.json")).exists()) continue;
            MoonBase.log("SaveFixer -> save revision 2 might have bad entities...");
            SaveFixer.entityClassRenamingFix(saveRev2.path(), false);
        }
        MoonBase.log("-----------------");
    }

    private static int findSaveDataVersionNumber(String fileText) {
        int dataVersion = -1;
        String pattern = "saveDataVersion: \\d*";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(fileText);
        if (m.find()) {
            MatchResult match = m.toMatchResult();
            String versionLine = m.group();
            try {
                dataVersion = Integer.parseInt(versionLine.replaceAll("saveDataVersion: ", ""));
            }
            catch (Exception e) {
                MoonBase.error("Erorr parsing version number from save file.");
            }
        } else {
            MoonBase.error("Can't find version number in the gameSave.json file");
        }
        return dataVersion;
    }

    public static boolean entityClassRenamingFix(String saveFile, boolean dryRun) {
        block8: {
            FileHandle gameData = Gdx.files.local(saveFile);
            if (gameData.exists()) {
                String fileText;
                MoonBase.log("SaveFixer: entityClassRenamingFix: " + saveFile + " -- dryRun: " + dryRun);
                String fileTextBackup = fileText = gameData.readString("ISO-8859-1");
                try {
                    fileText = fileText.replaceAll("com.cairn4.moonbase.ItemPickup", "com.cairn4.moonbase.entities.ItemPickup");
                    fileText = fileText.replaceAll("com.cairn4.moonbase.ItemDropper", "com.cairn4.moonbase.entities.ItemDropper");
                    fileText = fileText.replaceAll("com.cairn4.moonbase.RepairDrone", "com.cairn4.moonbase.entities.RepairDrone");
                    fileText = fileText.replaceAll("com.cairn4.moonbase.Buggie", "com.cairn4.moonbase.entities.Buggie");
                    fileText = fileText.replaceAll("com.cairn4.moonbase.Tank", "com.cairn4.moonbase.entities.Tank");
                    fileText = fileText.replaceAll("com.cairn4.moonbase.VehicleTrailer", "com.cairn4.moonbase.entities.VehicleTrailer");
                    fileText = fileText.replaceAll("com.cairn4.moonbase.LightningStrike", "com.cairn4.moonbase.entities.LightningStrike");
                    boolean success = SaveFixer.testJson(fileText);
                    if (success) {
                        if (!fileText.equals(fileTextBackup)) {
                            if (!dryRun) {
                                MoonBase.log("Save file entity fix applied to: " + saveFile);
                                gameData.writeString(fileText, false);
                            } else {
                                MoonBase.log("Entity test fix successful for " + saveFile);
                            }
                            return true;
                        }
                        MoonBase.log("Modified text is the same as the original.");
                        break block8;
                    }
                    MoonBase.error("Still couldn't Couldn't parse the generated json");
                }
                catch (Exception e) {
                    MoonBase.error("Error find/replacing entity classes in SaveFixer.");
                }
            } else {
                MoonBase.error("Save file (" + saveFile + ") doesn't exist");
            }
        }
        return false;
    }

    private static boolean testJson(String fileText) {
        try {
            Json json = new Json();
            json.setIgnoreDeprecated(true);
            json.setIgnoreUnknownFields(true);
            GameSaveData gsd = json.fromJson(GameSaveData.class, fileText);
            return true;
        }
        catch (Exception e) {
            Gdx.app.error("MewnBase", "Error parsing test json");
            Gdx.app.error("MewnBase", e.getMessage() + "\n");
            return false;
        }
    }

    private static ArrayList<FileHandle> getAllChunkFiles() {
        FileHandle[] allFilesInFolder;
        ArrayList<FileHandle> chunkFiles = new ArrayList<FileHandle>();
        for (FileHandle f : allFilesInFolder = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/").list()) {
            if (!SaveFixer.isChunkFile(f)) continue;
            chunkFiles.add(f);
        }
        return chunkFiles;
    }

    private static boolean isChunkFile(FileHandle f) {
        if (!f.extension().equals("json")) {
            return false;
        }
        return f.nameWithoutExtension().contains("chunk");
    }

    private void checkForAndUpgradeSaveFile() {
        FileHandle oldGameSaveFile = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/gameSave.json");
        if (oldGameSaveFile.exists()) {
            String oldFileText = oldGameSaveFile.readString("ISO-8859-1");
            byte[] bytes = Base64.getEncoder().encode(oldFileText.getBytes());
            String newFileText = new String(bytes);
            FileHandle bin = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/gameSave.data");
            boolean successWriting = false;
            try {
                bin.writeString(newFileText, false);
                successWriting = true;
            }
            catch (Exception e) {
                Gdx.app.error("MewnBase", "SaveFixer: error converting to .data file");
            }
            if (successWriting) {
                oldGameSaveFile.delete();
            }
        }
    }

    private void convertToSingleWorldData(GameSaveData gsd) {
        FileHandle[] files;
        WorldData wd = new WorldData();
        for (FileHandle file : files = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/").list()) {
            Gdx.app.log("MewnBase", "Checking file " + file.nameWithoutExtension());
            if (!file.nameWithoutExtension().substring(0, 5).equals("chunk")) continue;
            Json json = new Json();
            try {
                ChunkData cd = json.fromJson(ChunkData.class, file.readString());
                wd.chunkDataMap.put("chunk_" + cd.x + "," + cd.y, cd);
                Gdx.app.log("MewnBase", "Moving chunk " + cd.x + "," + cd.y + " into single file.");
            }
            catch (Exception e) {
                Gdx.app.postRunnable(new Runnable(){

                    @Override
                    public void run() {
                        SaveFixer.this.setChanged();
                        SaveFixer.this.notifyObservers("failedFixing");
                    }
                });
                return;
            }
        }
        Json wdJson = new Json();
        FileHandle wdFile = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/worldData.json");
        wdFile.writeString(wdJson.toJson(wd), false);
        Gdx.app.log("MoonBase", "SaveFixer: Writing single world data file to disk.");
        for (FileHandle file : files = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/").list()) {
            if (!file.nameWithoutExtension().substring(0, 5).equals("chunk")) continue;
            file.delete();
        }
    }

    private static void unlockAdvancedPowerTech(GameSaveData gsd) {
        try {
            int i;
            boolean unlockedPower3 = false;
            boolean unlockedEngines = false;
            for (i = 0; i < gsd.unlockedTech.size(); ++i) {
                if (gsd.unlockedTech.get(i).equals("power3")) {
                    unlockedPower3 = true;
                }
                if (!gsd.unlockedTech.get(i).equals("engines")) continue;
                unlockedEngines = true;
            }
            for (i = 0; i < gsd.unlockedTech.size(); ++i) {
                if (gsd.unlockedTech.get(i).equals("power2") && !unlockedPower3) {
                    gsd.unlockedTech.add("power3");
                    Gdx.app.log("MewnBase", "SaveFixer: unlocking Power 3 tech");
                }
                if (!gsd.unlockedTech.get(i).equals("transportation") || unlockedEngines) continue;
                gsd.unlockedTech.add("engines");
                Gdx.app.log("MewnBase", "SaveFixer: unlocking Engines tech");
            }
        }
        catch (Exception e) {
            Gdx.app.error("MewnBase", "Savefixer: error in unlocking tech");
            Gdx.app.error("MewnBase", "\n" + e.getMessage());
        }
    }

    public static String stripItemsToCollect(String chunkFileText) {
        chunkFileText = chunkFileText.replaceAll("(\\t*)buildable:.*\\n", "");
        Pattern expPattern = Pattern.compile("(\\n*)(\\t*)\\{\n(\t*)class: (.*)\n(\t*)id: (.*)\n(\t*)name: (.*)\n(\t*)desc: (.*)\n(\t*)amount: (.*)\n(\t*)sprite: (.*)\n(\t*)pickupTime: (.*)\n(\t*)}(,*)\n(\t*)");
        chunkFileText = expPattern.matcher(chunkFileText).replaceAll("");
        Pattern expPatternNoPickupTime = Pattern.compile("(\\n*)(\\t*)\\{\n(\t*)class: (.*)\n(\t*)id: (.*)\n(\t*)name: (.*)\n(\t*)desc: (.*)\n(\t*)amount: (.*)\n(\t*)sprite: (.*)\n(\t*)}(,*)\n(\t*)");
        chunkFileText = expPatternNoPickupTime.matcher(chunkFileText).replaceAll("");
        Pattern expPatternPickupWith = Pattern.compile("(\\n*)(\\t*)\\{\n(\t*)class: (.*)\n(\t*)id: (.*)\n(\t*)name: (.*)\n(\t*)desc: (.*)\n(\t*)amount: (.*)\n(\t*)sprite: (.*)\n(\t*)pickupWith: \\[\n(\t*)(.*)\n(\t*)\\]\n(\t*)pickupTime: (.*)\n(\t*)}(,*)\n(\t*)");
        chunkFileText = expPatternPickupWith.matcher(chunkFileText).replaceAll("");
        return chunkFileText;
    }

    public static boolean stripItemsToCollectFromFiles(ArrayList<FileHandle> files) {
        boolean succesful = true;
        for (FileHandle f : files) {
            if (!SaveFixer.isChunkFile(f)) continue;
            try {
                String chunkFileText = f.readString("ISO-8859-1");
                SaveFixer.stripItemsToCollect(chunkFileText);
            }
            catch (Exception e) {
                succesful = false;
                Gdx.app.error("MewnBase", e.getMessage());
            }
        }
        return succesful;
    }

    public void backup() {
        this.setChanged();
        this.notifyObservers("backup");
        FileHandle folder = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/");
        String backupName = folder.name();
        String dateStamp = SaveFixer.getTodaysDate();
        int i = 1;
        String backupNameToUse = backupName + " " + dateStamp;
        while (SaveFixer.doesBackupFolderExist(backupNameToUse)) {
            backupNameToUse = backupName + " " + dateStamp + " " + i;
            ++i;
        }
        FileHandle backup = Gdx.files.local("backupSaves/" + backupNameToUse + "/");
        folder.copyTo(backup);
        MoonBase.log("Backing up folder to " + backup.path());
    }

    public static void backup(String saveFileName) {
        FileHandle folder = Gdx.files.local(saveFileName).parent();
        String backupName = folder.name();
        String dateStamp = SaveFixer.getTodaysDate();
        int i = 1;
        String backupNameToUse = backupName + " " + dateStamp;
        while (SaveFixer.doesBackupFolderExist(backupNameToUse)) {
            backupNameToUse = backupName + " " + dateStamp + " " + i;
            ++i;
        }
        FileHandle backup = Gdx.files.local("backupSaves/" + backupNameToUse + "/");
        folder.copyTo(backup);
        MoonBase.log("Backing up folder to " + backup.path());
    }

    private static String getTodaysDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    public static boolean doesBackupFolderExist(String saveFolder) {
        FileHandle path = Gdx.files.local("backupSaves/" + saveFolder + "/");
        return path.exists();
    }

    private String preloadFix(String chunkFileText) {
        return chunkFileText.replace("com.cairn4.moonbase.tiles.GreenHouseSpriteBehavior$GrowStates", "com.cairn4.moonbase.tiles.behaviors.GreenHouseSpriteBehavior$GrowStates");
    }

    private boolean fixChunk(int saveVersion, ChunkData cd) {
        MoonBase.log("SaveFixer: Fixing chunk " + cd.x + "-" + cd.y);
        if (saveVersion < 9) {
            for (GroundTile.Biomes biomes : cd.gtBiomeArray) {
                if (biomes != GroundTile.Biomes.water) continue;
                GroundTile.Biomes biomes2 = GroundTile.Biomes.mud;
            }
            int fixed = 0;
            for (GroundTileData gtd : cd.groundTiles.values()) {
                if (!gtd.biome.equals("water")) continue;
                gtd.biome = "mud";
                ++fixed;
            }
        }
        for (TileData td : cd.tiles.values()) {
            for (BehaviorData behaviorData : td.behaviorDataList) {
                this.fixBehaviorClasses(behaviorData);
                this.fixResourceStorage(behaviorData);
            }
            if (td.storageDataList == null || td.storageDataList.size() <= 0) continue;
            Array<InventoryItemData> dbStorageArray = new Array<InventoryItemData>();
            for (InventoryItemData i : td.storageDataList) {
                InventoryItemData iid = new InventoryItemData();
                iid.itemId = i.itemId;
                iid.amount = i.amount;
                dbStorageArray.add(iid);
            }
            BehaviorData behaviorData = new BehaviorData();
            behaviorData.behaviorClass = "com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior";
            behaviorData.properties.put("storageDataList", dbStorageArray);
            td.behaviorDataList.add(behaviorData);
        }
        Iterator<Map.Entry<String, TileData>> tileIterator = cd.tiles.entrySet().iterator();
        while (tileIterator.hasNext()) {
            Map.Entry<String, TileData> entry = tileIterator.next();
            if (entry.getValue().name.equals("com.cairn4.moonbase.tiles.Crater")) {
                Gdx.app.debug("MewnBase", "SaveFixer: removing old crater tile");
                tileIterator.remove();
            }
            if (!entry.getValue().name.equals("com.cairn4.moonbase.tiles.Well")) continue;
            entry.getValue().name = "com.cairn4.moonbase.tiles.RainCollector";
        }
        return true;
    }

    private void fixBehaviorClasses(BehaviorData bd) {
        String bClass = bd.behaviorClass;
        if (!bClass.contains("com.cairn4.moonbase.tiles.behaviors.")) {
            bd.behaviorClass = bClass = bClass.replace("com.cairn4.moonbase.tiles.", "com.cairn4.moonbase.tiles.behaviors.");
        }
        for (String key : bd.properties.keySet()) {
            key.replace("com.cairn4.moonbase.tiles.", "com.cairn4.moonbase.tiles.behaviors.");
        }
    }

    private void fixResourceStorage(BehaviorData bd) {
        boolean needsFixing = false;
        float amount = 0.0f;
        float capacity = 0.0f;
        BaseResources type = null;
        String id = null;
        if (bd.behaviorClass.contains("PowerStorageBehavior")) {
            needsFixing = true;
            bd.behaviorClass = "com.cairn4.moonbase.tiles.behaviors.BaseResourceStorageBehavior";
            amount = Float.valueOf(bd.properties.get("powerStored").toString()).floatValue();
            capacity = Float.valueOf(bd.properties.get("maxPowerStored").toString()).floatValue();
            type = BaseResources.power;
        }
        if (bd.behaviorClass.contains("WaterStorageBehavior")) {
            needsFixing = true;
            bd.behaviorClass = "com.cairn4.moonbase.tiles.behaviors.BaseResourceStorageBehavior";
            amount = Float.valueOf(bd.properties.get("waterStored").toString()).floatValue();
            capacity = Float.valueOf(bd.properties.get("maxWaterStored").toString()).floatValue();
            type = BaseResources.water;
            id = "waterStorage";
        }
        if (needsFixing) {
            Gdx.app.log("MewnBase", "SaveFixer: Converting old resource storage to " + bd.behaviorClass + " (" + type + ")");
            bd.properties.clear();
            bd.properties.put("type", (Object)type);
            bd.properties.put("amount", Float.valueOf(amount));
            bd.properties.put("capacity", Float.valueOf(capacity));
            bd.properties.put("id", id);
        }
    }
}

