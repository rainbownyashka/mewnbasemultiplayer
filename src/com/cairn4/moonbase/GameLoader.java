/*
 * Decompiled with CFR 0.152. в
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.LoadingErrors;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerStatusEffect;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.NpcBonuses;
import com.cairn4.moonbase.techtree.TechUpgrade;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.worlddata.ChunkData;
import com.cairn4.moonbase.worlddata.EntityData;
import com.cairn4.moonbase.worlddata.GameSaveData;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import com.cairn4.moonbase.worlddata.PlayerData;
import com.cairn4.moonbase.worlddata.StatusEffectData;
import com.cairn4.moonbase.worlddata.WorldData;
import com.cairn4.utils.FileDateModifiedComparator;
import com.cairn4.utils.ScreenshotGrabber;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

public class GameLoader {
    private FileHandle file;
    private World world;
    public WorldData worldData;
    public static Pool<Json> jsonPool = Pools.get(Json.class);
    public static ArrayList<String> saveFilesWithErrors = new ArrayList();
    public static final String SAVEFILE_CHARSET = "ISO-8859-1";
    public static final String prevSlot1 = "prevSave1/";

    public static ArrayList<GameSaveData> getSavedGames() {
        ArrayList<GameSaveData> gameSaveDataList = new ArrayList<GameSaveData>();
        ArrayList<FileHandle> l = new ArrayList<FileHandle>();
        FileHandle[] directories = Gdx.files.local("saves/").list();
        for (FileHandle dir : directories) {
            if (!dir.isDirectory()) continue;
            FileHandle saveData = Gdx.files.local("saves/" + dir.name() + "/gameSave.data");
            if (saveData.exists()) {
                l.add(saveData);
                continue;
            }
            FileHandle saveDataOld = Gdx.files.local("saves/" + dir.name() + "/gameSave.json");
            if (!saveDataOld.exists()) continue;
            l.add(saveDataOld);
        }
        Collections.sort(l, new FileDateModifiedComparator());
        saveFilesWithErrors.clear();
        for (FileHandle s : l) {
            String fileText = "";
            if (s.extension().equals("json")) {
                fileText = s.readString(SAVEFILE_CHARSET);
            } else if (s.extension().equals("data")) {
                fileText = s.readString(SAVEFILE_CHARSET);
                byte[] bytes = Base64.getDecoder().decode(fileText.toString());
                fileText = new String(bytes);
            }
            try {
                Json json = new Json();
                json.setIgnoreDeprecated(true);
                json.setIgnoreUnknownFields(true);
                GameSaveData gsd = json.fromJson(GameSaveData.class, fileText);
                gameSaveDataList.add(gsd);
            }
            catch (Exception e) {
                saveFilesWithErrors.add(s.path());
                MoonBase.error("Error parsing save file " + s.path());
                MoonBase.error(e.getMessage() + "\n");
            }
        }
        return gameSaveDataList;
    }

    public static ArrayList<String> getSavesWithErrors() {
        GameLoader.getSavedGames();
        if (saveFilesWithErrors.size() > 0) {
            MoonBase.error("Found " + saveFilesWithErrors.size() + " save folders with errors.");
        } else {
            MoonBase.log("Found no save files with errors.");
        }
        return saveFilesWithErrors;
    }

    public void autoSave(World w) {
        this.world = w;
        new Thread(new Runnable(){

            @Override
            public void run() {
                GameLoader.this.saveGame(GameLoader.this.world, true);
                Gdx.app.postRunnable(new Runnable(){

                    @Override
                    public void run() {
                    }
                });
            }
        }).start();
        ScreenshotGrabber.takeScreenshot(this.world.gameScreen.stage, this.world.gameScreen);
    }

    public void saveGame(World world, boolean autoSave) {
        boolean readOnly = false;
        GameSaveData oldGsd = null;
        oldGsd = GameLoader.getGameSaveData(MoonBase.currentSaveFolder);
        if (oldGsd != null) {
            readOnly = oldGsd.readOnly;
        }
        if (!readOnly) {
            this.cycleAutoSavesBeforeSaving(MoonBase.currentSaveFolder);
            GameSaveData gsd = new GameSaveData();
            GameScreen gameScreen = world.gameScreen;
            Mission mission = gameScreen.game.getCurrentMission();
            gsd.autoSave = autoSave;
            this.worldData = this.updateWorldData(world);
            this.writeWorldDataFile("");
            for (Entity e : world.entityList) {
                if (!e.isSaved()) continue;
                gsd.entityDataList.add(new EntityData(e));
            }
            gsd.lastUpdated = new Date().getTime();
            gsd.saveDataVersion = 9;
            GameScreen gameScreen2 = world.gameScreen;
            gsd.tutorialFinished = gameScreen2.game.getCurrentMission().tutorialFinished;
            gsd.tutorialStage = world.gameScreen.tutorial != null ? world.gameScreen.tutorial.getStage().toString() : "start";
            gsd.saveFolder = "" + MoonBase.currentSaveFolder;
            gsd.playerName = "" + mission.playerName;
            gsd.missionType = "" + (Object)((Object)mission.missionType);
            gsd.missionPlanetName = mission.planetName;
            gsd.missionDayGoal = mission.getMissionDayLength();
            gsd.missionCompleteReady = mission.missionCompleteReady;
            gsd.missionComplete = mission.missionComplete;
            gsd.terrainGenSeed = world.terrainGen.getSeed();
            gsd.currentDayNum = world.dayCycle.getDay();
            gsd.currentPeriod = world.dayCycle.currentPeriod.toString();
            gsd.currentPeriodTime = world.dayCycle.timer;
            gsd.currentWeatherId = world.weatherManager.getCurrentData().id;
            gsd.currentWeatherTime = world.weatherManager.getTimer();
            gsd.currentWeatherDuration = world.weatherManager.getDuration();
            gsd.dayCycleModes = mission.dayCycleMode.toString();
            gsd.waterMode = mission.waterMode.toString();
            gsd.techTreeMode = mission.techTreeMode.toString();
            gsd.weatherMode = mission.weatherMode.toString();
            gsd.creatureMode = mission.creatureMode.toString();
            gsd.playerData = new PlayerData(world.player);
            gsd.regrowthManagerList = world.regrowthManager.saveData();
            gsd.unlockedTech = world.techManager.getSaveData();
            gsd.techSamples = world.techManager.samples;
            gsd.npcBonuses = NpcBonuses.getInstance().getAllBonuses();
            GameLoader.writeGameSaveData(gsd, "");
            gsd = null;
        } else {
            Gdx.app.log("MewnBase", "Can't save, game is set to read-only");
        }
    }

    private WorldData updateWorldData(World world) {
        WorldData wd = new WorldData();
        for (Chunk c : world.worldChunks.values()) {
            ChunkData chunkData = null;
            chunkData = new ChunkData(c);
            if (!c.isVisible()) {
                ChunkData cd_GroundTiles = this.worldData.chunkDataMap.get("chunk_" + c.chunkX + "," + c.chunkY);
                chunkData.groundTiles.putAll(cd_GroundTiles.groundTiles);
            }
            if (chunkData == null) continue;
            wd.chunkDataMap.put("chunk_" + chunkData.x + "," + chunkData.y, chunkData);
        }
        return wd;
    }

    private void writeWorldDataFile(String subfolder) {
        Json json = new Json();
        this.file = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/" + subfolder + "worldData.json");
        this.file.writeString(json.toJson(this.worldData), false);
        Gdx.app.debug("MoonBase", "WorldData: Writing file to disk.");
    }

    public static void writeWorldDataFile(WorldData worldData) {
        Json json = new Json();
        FileHandle file = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/worldData.json");
        file.writeString(json.toJson(worldData), false);
        MoonBase.log("GameLoader: Writing world data to disk.");
    }

    private static WorldData loadWorldDataFile() {
        FileHandle file = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/worldData.json");
        Json json = jsonPool.obtain();
        json.setIgnoreDeprecated(true);
        String fileText = file.readString();
        WorldData wd = json.fromJson(WorldData.class, fileText);
        jsonPool.free(json);
        return wd;
    }

    public static void writeGameSaveData(GameSaveData gsd, String subfolder) {
        Gdx.app.log("MewnBase", "GameLoader: Writing gameSaveData: " + gsd.saveFolder);
        Json json = jsonPool.obtain();
        json.setIgnoreDeprecated(true);
        json.setUsePrototypes(false);
        FileHandle bin = Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/" + subfolder + "gameSave.json");
        String jsonString = json.prettyPrint(gsd);
        bin.writeString(jsonString, false);
        jsonPool.free(json);
    }

    public static GameSaveData getGameSaveData(String folder) {
        Gdx.app.log("MewnBase", "GameLoader: Loading game from folder " + folder);
        FileHandle dataFile = Gdx.files.local("saves/" + folder + "/gameSave.json");
        if (dataFile.exists()) {
            try {
                String fileText = dataFile.readString(SAVEFILE_CHARSET);
                Json json = jsonPool.obtain();
                json.setIgnoreUnknownFields(true);
                json.setIgnoreDeprecated(true);
                GameSaveData gsd = json.fromJson(GameSaveData.class, fileText);
                jsonPool.free(json);
                return gsd;
            }
            catch (Exception e) {
                Gdx.app.error("MewnBase", "GameLoader: Error parsing gameSave data");
                return null;
            }
        }
        Gdx.app.error("MewnBase", "GameLoader: can't find save data for folder " + folder);
        return null;
    }

    public void loadGame(World world) {
        GameSaveData gsd = GameLoader.getGameSaveData(MoonBase.currentSaveFolder);
        if (gsd != null) {
            Mission m = new Mission();
            m.saveFolder = MoonBase.currentSaveFolder;
            m.playerName = gsd.playerName;
            try {
                m.missionType = Mission.MissionTypes.valueOf(gsd.missionType);
            }
            catch (NullPointerException n) {
                Gdx.app.error("MewnBase", "GameLoader: null or invalid mission type, setting it to normal");
                m.missionType = Mission.MissionTypes.normal;
            }
            m.tutorialStage = gsd.tutorialStage;
            m.tutorialFinished = gsd.tutorialFinished;
            m.missionDayLength = gsd.missionDayGoal;
            m.planetName = gsd.missionPlanetName;
            m.missionCompleteReady = gsd.missionCompleteReady;
            m.missionComplete = gsd.missionComplete;
            world.gameScreen.game.setMission(m);
            try {
                m.dayCycleMode = Mission.dayCycleModes.valueOf(gsd.dayCycleModes);
            }
            catch (Exception e) {
                m.dayCycleMode = Mission.dayCycleModes.defaultDay;
            }
            try {
                m.waterMode = Mission.waterModes.valueOf(gsd.waterMode);
            }
            catch (Exception e) {
                m.waterMode = Mission.waterModes.defaultWater;
            }
            try {
                m.techTreeMode = Mission.techTreeModes.valueOf(gsd.techTreeMode);
            }
            catch (Exception e) {
                m.techTreeMode = Mission.techTreeModes.allTechUnlocked;
            }
            try {
                m.weatherMode = Mission.weatherModes.valueOf(gsd.weatherMode);
            }
            catch (Exception e) {
                m.weatherMode = Mission.weatherModes.normal;
            }
            try {
                m.creatureMode = Mission.creatureModes.valueOf(gsd.creatureMode);
            }
            catch (Exception e) {
                m.creatureMode = Mission.creatureModes.hostile;
            }
            if (m.missionType == Mission.MissionTypes.creative) {
                MoonBase.GOD_MODE = true;
                m.techTreeMode = Mission.techTreeModes.allTechUnlocked;
            } else {
                MoonBase.GOD_MODE = false;
            }
            int chunkX = gsd.playerData.chunkX;
            int chunkY = gsd.playerData.chunkY;
            this.worldData = GameLoader.loadWorldDataFile();
            world.worldChunks = world.chunkLoader.loadAllChunks(this.worldData);
            if (gsd.terrainGenSeed != 0) {
                world.terrainGen.setSeed(gsd.terrainGenSeed);
            } else {
                world.terrainGen.newSeed();
            }
            world.dayCycle.setDayCycleMode(m.dayCycleMode);
            world.dayCycle.setDay(gsd.currentDayNum);
            world.dayCycle.setPeriod(gsd.currentPeriod);
            world.dayCycle.setTime(gsd.currentPeriodTime);
            world.weatherManager.setMode(m.weatherMode);
            if (m.weatherMode == Mission.weatherModes.none && !gsd.currentWeatherId.equals("clear")) {
                MoonBase.error("Weather mode is none, current weather should be clear.");
                gsd.currentWeatherId = "clear";
            }
            world.weatherManager.setWeather(gsd.currentWeatherId);
            world.weatherManager.setTimer(gsd.currentWeatherTime);
            world.weatherManager.setDuration(gsd.currentWeatherDuration);
            world.spawnPlayer(world.getChunk(chunkX, chunkY), gsd.playerData.localX, gsd.playerData.localY, false);
            for (InventoryItemData invItemData : gsd.playerData.inventoryItemDataList) {
                try {
                    ItemStack stack = new ItemStack(invItemData.itemId, invItemData.amount);
                    int durabilityForThisType = ItemFactory.getDurability(invItemData.itemId);
                    if (durabilityForThisType > 0 && invItemData.durability == 0) {
                        invItemData.durability = durabilityForThisType;
                    }
                    stack.item.durability = invItemData.durability;
                    world.player.playerInventory.addItemNoOrg(stack);
                }
                catch (NullPointerException e) {
                    Gdx.app.error("MewnBase", "Error loading player inventory item " + invItemData.itemId + ", skipping.");
                }
            }
            world.player.setCustomizationOptions(gsd.playerData.characterFaceOption, gsd.playerData.characterSuitColor);
            this.loadPlayerStatusEffects(world.player, gsd.playerData);
            world.player.markedMapLocations = gsd.playerData.markedMapLocations;
            MoonBase.log("Game Loader: Upgrading suit level to " + gsd.playerData.suitLevel);
            world.player.setSuitLevel(gsd.playerData.suitLevel, false);
            world.player.playerStatus.setHunger(gsd.playerData.hunger);
            world.player.playerStatus.setSuitPower(gsd.playerData.suitPower);
            world.player.playerStatus.setAir(gsd.playerData.air);
            world.player.playerStatus.setFlashlight(gsd.playerData.flashlight);
            if (gsd.playerData.health == 0.0f) {
                gsd.playerData.health = 100.0f;
                Gdx.app.log("MewnBase", "GameLoader: health was zero due to not being saved, so restoring to 100%");
            }
            world.player.playerStatus.setHealth(gsd.playerData.health);
            world.chunkLoader.changeChunks(world.player.chunkX, world.player.chunkY);
            world.baseManager.updateBases(world);
            for (String s : gsd.unlockedTech) {
                TechUpgrade t = world.techManager.getTech(s);
                if (t == null) continue;
                world.techManager.getTech((String)s).unlocked = true;
                if (t.suitLevel != 0 && world.player != null) {
                    Gdx.app.log("MewnBase", "Game Loader: Upgrading suit level!");
                    world.player.setSuitLevel(t.suitLevel, false);
                }
                if (!t.baseUpgrade) continue;
                world.baseManager.baseUpgrade(t.id, true);
            }
            world.techManager.setSamples(gsd.techSamples);
            Gdx.app.log("MewnBase", "GameLoader: " + world.techManager.samples + " science samples");
            if (m.techTreeMode == Mission.techTreeModes.allTechUnlocked) {
                world.techManager.unlockAll();
            }
            for (NpcBonuses.bonusTypes b : gsd.npcBonuses) {
                NpcBonuses.getInstance().loadBonus(b);
            }
            for (Integer i : gsd.playerData.researchItemsDiscovered) {
                world.player.researchObjectsDiscovered.add(i);
            }
            world.regrowthManager.loadData(gsd.regrowthManagerList);
            this.loadEntities(world, gsd);
            long highestId = 0L;
            for (Entity e : world.entityList) {
                if (e.id <= highestId) continue;
                highestId = e.id;
            }
            Gdx.app.log("MewnBase", "GameLoader: Highest entity id is now: " + highestId);
            world.nextEntityId = highestId;
            FileHandle path = Gdx.files.local("world/map/");
            if (!path.exists()) {
                // empty if block
            }
            if (m.missionType == Mission.MissionTypes.creative) {
                world.player.playerStatus.setMaxValues();
            }
        } else {
            Gdx.app.error("MewnBase", "Game failed to load, returning to main menu.");
            this.world.gameScreen.errorReturnToMainMenu(LoadingErrors.parsingGameSave);
        }
    }

    private void loadEntities(World world, GameSaveData gsd) {
        for (EntityData ed : gsd.entityDataList) {
            Object[] arguments;
            Class[] classArgs;
            Gdx.app.debug("MewnBase", "GameLoader: trying to create entity " + ed.className);
            Object object = null;
            if (ed.className.equals("com.cairn4.moonbase.entities.ItemPickup")) {
                classArgs = new Class[]{World.class, Float.TYPE, Float.TYPE, Item.class};
                Item i = ItemFactory.getInstance().createItem(ed.itemId);
                arguments = new Object[]{world, Float.valueOf(ed.worldXPos), Float.valueOf(ed.worldYPos), i};
            } else {
                classArgs = new Class[]{World.class, Float.TYPE, Float.TYPE, Float.TYPE};
                arguments = new Object[]{world, Float.valueOf(ed.worldXPos), Float.valueOf(ed.worldYPos), Float.valueOf(ed.rotation)};
            }
            try {
                Class<?> aClass = Class.forName(ed.className);
                Constructor<?> constructor = null;
                constructor = aClass.getConstructor(classArgs);
                object = constructor.newInstance(arguments);
                ((Entity)object).loadProperties(ed.properties);
            }
            catch (InstantiationException e) {
                System.out.println(e);
            }
            catch (IllegalAccessException e) {
                System.out.println(e);
            }
            catch (InvocationTargetException e) {
                e.getCause();
                e.printStackTrace();
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPlayerStatusEffects(Player player, PlayerData playerData) {
        for (StatusEffectData sed : playerData.statusEffeectDataList) {
            Gdx.app.debug("MewnBase", "GameLoader: trying to load player status effect:  " + sed.statusEffectClass);
            Object object = null;
            Class[] classArgs = new Class[]{};
            Object[] arguments = new Object[]{};
            try {
                Class<?> aClass = Class.forName(sed.statusEffectClass);
                Constructor<?> constructor = null;
                constructor = aClass.getConstructor(classArgs);
                object = constructor.newInstance(arguments);
                ((PlayerStatusEffect)object).loadProperties(sed.properties);
                player.playerStatus.newStatusEffect(object);
            }
            catch (InstantiationException e) {
                System.out.println(e);
            }
            catch (IllegalAccessException e) {
                System.out.println(e);
            }
            catch (InvocationTargetException e) {
                e.getCause();
                e.printStackTrace();
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void eraseSave(String saveFolder) {
        MoonBase.log("GameLoader: Erasing save folder " + saveFolder);
        FileHandle path = Gdx.files.local("saves/" + saveFolder + "/");
        if (path.exists()) {
            path.emptyDirectory();
            path.deleteDirectory();
        }
    }

    public static String sanitizeString(String s1) {
        return s1.replaceAll("[^a-zA-Z0-9\\-]+", "");
    }

    public static String createSaveFolder(String playerName) {
        String sanitizedPlayerName = GameLoader.sanitizeString(playerName);
        String saveFolderName = sanitizedPlayerName;
        if (saveFolderName.equals("")) {
            sanitizedPlayerName = "save";
            saveFolderName = "save";
        }
        int i = 1;
        while (GameLoader.doesSaveFolderExist(saveFolderName)) {
            saveFolderName = sanitizedPlayerName + "_" + i;
            ++i;
        }
        return saveFolderName;
    }

    @Deprecated
    public static String nameGenerator() {
        long time = System.currentTimeMillis();
        int rand = MathUtils.random(100, 999);
        String save = "save_" + rand + "_" + time;
        System.out.println(save);
        return save;
    }

    public static boolean doesSaveFolderExist(String saveFolder) {
        FileHandle path = Gdx.files.local("saves/" + saveFolder + "/");
        return path.exists();
    }

    private void makeSaveVersionFolders(String saveFolder) {
        FileHandle path2;
        FileHandle path = Gdx.files.local("saves/" + saveFolder + "/prevSave1");
        if (!path.exists()) {
            path.mkdirs();
        }
        if (!(path2 = Gdx.files.local("saves/" + saveFolder + "/prevSave2")).exists()) {
            path2.mkdirs();
        }
    }

    private void cycleSaveVersions(String saveFolder) {
        Gdx.app.log("MewnBase", "Cycling save files...");
        FileHandle path2 = Gdx.files.local("saves/" + saveFolder + "/prevSave2");
        if (path2.exists()) {
            path2.deleteDirectory();
        }
        path2.mkdirs();
        FileHandle path1 = Gdx.files.local("saves/" + saveFolder + "/prevSave1");
        path2 = Gdx.files.local("saves/" + saveFolder + "/prevSave2/");
        if (path1.exists()) {
            for (FileHandle f : path1.list()) {
                f.moveTo(path2);
            }
        }
        FileHandle path = Gdx.files.local("saves/" + saveFolder + "/prevSave1/");
        path.mkdirs();
        FileHandle gameSave = Gdx.files.local("saves/" + saveFolder + "/gameSave.json");
        FileHandle worldData = Gdx.files.local("saves/" + saveFolder + "/worldData.json");
        gameSave.moveTo(path);
        worldData.moveTo(path);
    }

    private void cycleAutoSavesBeforeSaving(String saveFolder) {
        FileHandle path0 = Gdx.files.local("saves/" + saveFolder);
        FileHandle path1 = Gdx.files.local("saves/" + saveFolder + "/prevSave1");
        FileHandle path1destFolder = Gdx.files.local("saves/" + saveFolder + "/prevSave1/");
        FileHandle path2 = Gdx.files.local("saves/" + saveFolder + "/prevSave2");
        FileHandle path2destFolder = Gdx.files.local("saves/" + saveFolder + "/prevSave2/");
        if (path2.exists()) {
            path2.deleteDirectory();
        }
        if (path1destFolder.exists()) {
            path2destFolder.mkdirs();
            for (FileHandle f : path1.list()) {
                f.moveTo(path2destFolder);
            }
        }
        if (path1destFolder.isDirectory() && path1destFolder.exists()) {
            MoonBase.log("path1destfolder is a directory and exists.");
        } else {
            MoonBase.log("path1destfolder doesn't exist, creating");
            path1destFolder.mkdirs();
        }
        path1.emptyDirectory();
        for (FileHandle f : path0.list()) {
            if (f.isDirectory()) continue;
            MoonBase.log("Moving save file: " + f.name() + " to " + path1.name() + " folder");
            f.moveTo(path1destFolder);
        }
    }

    public static void cycleAutoSavesBeforeLoad(String saveFolder, int saveVersion) {
        FileHandle prevSave1temp;
        FileHandle prevSave1;
        if (saveVersion == 1) {
            Gdx.app.log("MewnBase", "GameLoader: restoring autosave 1 to main slot.");
            prevSave1 = Gdx.files.local("saves/" + saveFolder + "/prevSave1");
            prevSave1temp = Gdx.files.local("saves/" + saveFolder + "/prevSave1temp/");
            prevSave1.mkdirs();
            prevSave1temp.mkdirs();
            for (FileHandle f : prevSave1.list()) {
                f.moveTo(prevSave1temp);
            }
            FileHandle gameSave = Gdx.files.local("saves/" + saveFolder + "/gameSave.json");
            FileHandle worldData = Gdx.files.local("saves/" + saveFolder + "/worldData.json");
            FileHandle screenshot = Gdx.files.local("saves/" + saveFolder + "/screenshot.png");
            gameSave.moveTo(prevSave1);
            worldData.moveTo(prevSave1);
            screenshot.moveTo(prevSave1);
            FileHandle mainFolder = Gdx.files.local("saves/" + saveFolder + "/");
            prevSave1temp = Gdx.files.local("saves/" + saveFolder + "/prevSave1temp");
            for (FileHandle file : prevSave1temp.list()) {
                file.moveTo(mainFolder);
            }
            prevSave1temp.delete();
        }
        if (saveVersion == 2) {
            prevSave1 = Gdx.files.local("saves/" + saveFolder + "/prevSave1");
            prevSave1temp = Gdx.files.local("saves/" + saveFolder + "/prevSave1temp/");
            prevSave1.mkdirs();
            prevSave1temp.mkdirs();
            for (FileHandle f : prevSave1.list()) {
                f.moveTo(prevSave1temp);
            }
            FileHandle prevSave2 = Gdx.files.local("saves/" + saveFolder + "/prevSave2");
            FileHandle prevSave2temp = Gdx.files.local("saves/" + saveFolder + "/prevSave2temp/");
            prevSave2.mkdirs();
            prevSave2temp.mkdirs();
            for (FileHandle f : prevSave2.list()) {
                f.moveTo(prevSave2temp);
            }
            FileHandle gameSave = Gdx.files.local("saves/" + saveFolder + "/gameSave.json");
            FileHandle worldData = Gdx.files.local("saves/" + saveFolder + "/worldData.json");
            FileHandle screenshot = Gdx.files.local("saves/" + saveFolder + "/screenshot.png");
            gameSave.moveTo(prevSave1);
            worldData.moveTo(prevSave1);
            screenshot.moveTo(prevSave1);
            FileHandle mainFolder = Gdx.files.local("saves/" + saveFolder + "/");
            prevSave2temp = Gdx.files.local("saves/" + saveFolder + "/prevSave2temp");
            for (FileHandle file : prevSave2temp.list()) {
                file.moveTo(mainFolder);
            }
            prevSave1temp = Gdx.files.local("saves/" + saveFolder + "/prevSave1temp");
            for (FileHandle file : prevSave1temp.list()) {
                file.moveTo(prevSave2);
            }
            prevSave1temp.delete();
            prevSave2temp.delete();
        }
    }
}

