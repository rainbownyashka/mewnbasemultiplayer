/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tests;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SnapshotArray;
import com.cairn4.moonbase.GameLoader;
import com.cairn4.moonbase.SaveFixer;
import com.cairn4.moonbase.SettingsData;
import com.cairn4.moonbase.UpdateCheck;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tests.ZSortTestCompareTest;
import com.cairn4.moonbase.tests.ZTestObj;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.worlddata.WeatherData;
import com.cairn4.moonbase.worlddata.WeatherDataList;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import junit.framework.TestCase;
import org.junit.Test;

public class MoonTest {
    boolean newWeather = true;
    ArrayList<WeatherData> weatherDataList = new ArrayList();
    WeatherData currentData;
    WeatherData nextData;
    float rollStartingBase = 0.0f;
    int maxConsequtiveStreak = 4;
    int consequtiveStreak = 1;
    int defaultWeather = 0;
    int rain = 0;
    int thunder = 0;
    int sandstorm = 0;

    @Test
    public void entityFindReplaceTest() {
        String expected = "com.cairn4.moonbase.entities.Buggie\ncom.cairn4.moonbase.entities.RepairDrone\ncom.cairn4.moonbase.entities.VehicleTrailer";
        String fileText = "com.cairn4.moonbase.Buggie\ncom.cairn4.moonbase.RepairDrone\ncom.cairn4.moonbase.VehicleTrailer";
        fileText = fileText.replaceAll("com.cairn4.moonbase.ItemPickup", "com.cairn4.moonbase.entities.ItemPickup");
        fileText = fileText.replaceAll("com.cairn4.moonbase.ItemDropper", "com.cairn4.moonbase.entities.ItemDropper");
        fileText = fileText.replaceAll("com.cairn4.moonbase.RepairDrone", "com.cairn4.moonbase.entities.RepairDrone");
        fileText = fileText.replaceAll("com.cairn4.moonbase.Buggie", "com.cairn4.moonbase.entities.Buggie");
        fileText = fileText.replaceAll("com.cairn4.moonbase.Tank", "com.cairn4.moonbase.entities.Tank");
        fileText = fileText.replaceAll("com.cairn4.moonbase.VehicleTrailer", "com.cairn4.moonbase.entities.VehicleTrailer");
        fileText = fileText.replaceAll("com.cairn4.moonbase.LightningStrike", "com.cairn4.moonbase.entities.LightningStrike");
        TestCase.assertEquals("Entity classes have been renamed.", expected, fileText);
    }

    @Test
    public void weatherTest() {
        FileHandle fileHandle = new FileHandle("../../gameData/data/weather.json");
        if (fileHandle.exists()) {
            Json json = new Json();
            String fileText = fileHandle.readString();
            this.weatherDataList = json.fromJson(WeatherDataList.class, (String)fileText).weatherDataList;
        }
        this.currentData = this.weatherDataList.get(0);
        this.nextData = this.weatherDataList.get(0);
        this.doWeatherTest(4.0f, 6);
        this.doWeatherTest(1.0f, 5);
        this.doWeatherTest(0.0f, 3);
    }

    private void doWeatherTest(float rollStart, int max) {
        System.out.println("rollStartingBase = " + rollStart + " - maxConsec: " + max);
        this.rollStartingBase = rollStart;
        this.consequtiveStreak = 1;
        this.maxConsequtiveStreak = max;
        this.defaultWeather = 0;
        this.rain = 0;
        this.thunder = 0;
        this.sandstorm = 0;
        for (int i = 0; i < 100; ++i) {
            if (this.newWeather) {
                this.nextWeather();
            } else {
                this.nextWeatherOld();
            }
            if (!this.nextData.id.equals("clear")) {
                System.out.print(this.nextData.id.charAt(0));
            } else {
                System.out.print("-");
            }
            switch (this.nextData.id) {
                case "clear": {
                    ++this.defaultWeather;
                    break;
                }
                case "rain": {
                    ++this.rain;
                    break;
                }
                case "thunder": {
                    ++this.thunder;
                    break;
                }
                case "sandstorm": {
                    ++this.sandstorm;
                }
            }
            this.currentData = this.nextData;
        }
        System.out.println("\nDefault: " + this.defaultWeather);
        System.out.println("Rain: " + this.rain);
        System.out.println("Thunder: " + this.thunder);
        System.out.println("Sandstorm: " + this.sandstorm);
        System.out.println("");
        System.out.println("");
    }

    public void nextWeatherOld() {
        if (this.currentData.defaultWeather) {
            float max = 1.0f;
            for (WeatherData w : this.weatherDataList) {
                max += w.chance;
            }
            float r = MathUtils.random(0.0f, max);
            float baseR = 0.0f;
            for (WeatherData w : this.weatherDataList) {
                if (r <= w.chance + baseR) {
                    this.nextData = w;
                    break;
                }
                baseR += w.chance;
            }
        } else {
            for (WeatherData w : this.weatherDataList) {
                if (!w.defaultWeather) continue;
                this.nextData = w;
                break;
            }
        }
    }

    public void nextWeather() {
        block2: {
            block3: {
                block4: {
                    if (!this.currentData.defaultWeather) break block3;
                    if (this.consequtiveStreak < this.maxConsequtiveStreak) break block4;
                    this.consequtiveStreak = 1;
                    int maxTries = 100;
                    for (int tries = 0; this.nextData.id.equals(this.currentData.id) && tries < maxTries; ++tries) {
                        this.randomWeatherRoll();
                    }
                    break block2;
                }
                this.randomWeatherRoll();
                if (this.nextData != this.currentData) break block2;
                ++this.consequtiveStreak;
                break block2;
            }
            for (WeatherData w : this.weatherDataList) {
                if (!w.defaultWeather) continue;
                this.nextData = w;
                break;
            }
        }
    }

    private void randomWeatherRoll() {
        float max = this.rollStartingBase;
        for (WeatherData w : this.weatherDataList) {
            max += w.chance;
        }
        float r = MathUtils.random(0.0f, max);
        float baseR = 0.0f;
        for (WeatherData w : this.weatherDataList) {
            if (r <= w.chance + baseR) {
                this.nextData = w;
                break;
            }
            baseR += w.chance;
        }
    }

    @Test
    public void zSortTest() {
        ZTestObj z1 = new ZTestObj();
        z1.ypos = 10.0f;
        ZTestObj z2 = new ZTestObj();
        z2.ypos = 20.0f;
        ZTestObj z3 = new ZTestObj();
        z3.ypos = 30.0f;
        ZTestObj z4 = new ZTestObj();
        z4.ypos = 40.0f;
        ZTestObj z5 = new ZTestObj();
        z5.ypos = 50.0f;
        ZTestObj z6 = new ZTestObj();
        z6.ypos = 60.0f;
        ZTestObj z7 = new ZTestObj();
        z7.ypos = 20.0f;
        SnapshotArray<ZTestObj> zList = new SnapshotArray<ZTestObj>(true, 10, ZTestObj.class);
        zList.add(z6);
        zList.add(z1);
        zList.add(z3);
        zList.add(z2);
        zList.add(z5);
        zList.add(z4);
        zList.add(z7);
        ZSortTestCompareTest zsc = new ZSortTestCompareTest();
        for (ZTestObj z : zList) {
            System.out.println(z.ypos);
        }
        System.out.println("\n");
        Arrays.sort((ZTestObj[])zList.begin(), zsc);
        zList.end();
        for (ZTestObj z : zList) {
            System.out.println(z.ypos);
        }
        TestCase.assertEquals("z1 is first 6", 6, zList.indexOf(z1, true));
        TestCase.assertEquals("z3 is index 3", 3, zList.indexOf(z3, true));
        TestCase.assertEquals("z4 is index 2", 2, zList.indexOf(z4, true));
        TestCase.assertEquals("z5 is index 1", 1, zList.indexOf(z5, true));
        TestCase.assertEquals("z6 is index 0", 0, zList.indexOf(z6, true));
    }

    @Test
    public void playerNameSantize() {
        TestCase.assertEquals("BiggusDickus!!@$*&% equals BiggusDickus", "BiggusDickus", GameLoader.sanitizeString("BiggusDickus!!@$*&%"));
        TestCase.assertEquals("Steve Forde equals SteveForde", "SteveForde", GameLoader.sanitizeString("SteveForde"));
        TestCase.assertEquals("asdf equals asdf", "asdf", GameLoader.sanitizeString("asdf"));
        TestCase.assertEquals("[]87!%<.>z equals 87z", "87z", GameLoader.sanitizeString("[]87!%<.>z"));
        TestCase.assertEquals("Steve-forde doesn't remove the hyphen", "Steve-forde", GameLoader.sanitizeString("Steve-forde"));
    }

    @Test
    public void testWorldTileToChunkKey() {
        TestCase.assertEquals("World coord (0, 0) should be chunk 0,0", "0,0", World.convertWorldTileToChunkKey(0, 0));
        TestCase.assertEquals("World coord (5, 5) should be chunk 0,0", "0,0", World.convertWorldTileToChunkKey(5, 5));
        TestCase.assertEquals("World coord (-2, 2) should be chunk -1,0", "-1,0", World.convertWorldTileToChunkKey(-2, 2));
        TestCase.assertEquals("World coord (-5, 5) should be chunk -1,0", "-1,0", World.convertWorldTileToChunkKey(-5, 5));
        TestCase.assertEquals("World coord (-15, -15) should be chunk -2,-2", "-2,-2", World.convertWorldTileToChunkKey(-15, -15));
        TestCase.assertEquals("World coord (-1, 0) should be chunk -1,-0", "-1,0", World.convertWorldTileToChunkKey(-1, 0));
        TestCase.assertEquals("World coord (-15, -1) should be chunk -2,0", "-2,-1", World.convertWorldTileToChunkKey(-15, -1));
        TestCase.assertEquals("World coord (15, -5) should be chunk 1,-1", "1,-1", World.convertWorldTileToChunkKey(15, -5));
        TestCase.assertEquals("World coord (99, 99) should be chunk 9,9", "9,9", World.convertWorldTileToChunkKey(99, 99));
        TestCase.assertEquals("World coord (6, -3) should be chunk 0, -1", "0,-1", World.convertWorldTileToChunkKey(6, -3));
    }

    @Test
    public void testUpdateCheck() {
        TestCase.assertEquals("local 1.0.0 vs server 0.0.0: ahead", false, UpdateCheck.isUpdateAvailable("1.0.0", "0.0.0"));
        TestCase.assertEquals("local 1.0.0 vs server 1.0.0: up to date", false, UpdateCheck.isUpdateAvailable("1.0.0", "1.0.0"));
        TestCase.assertEquals("local 1.0.0 vs server 2.0.0: needs to be updated", true, UpdateCheck.isUpdateAvailable("1.0.0", "2.0.0"));
        TestCase.assertEquals("local 0.1.0 vs server 0.0.0: ahead", false, UpdateCheck.isUpdateAvailable("0.1.1", "0.1.0"));
        TestCase.assertEquals("local 0.1.0 vs server 0.1.0: up to date", false, UpdateCheck.isUpdateAvailable("0.1.1", "0.1.0"));
        TestCase.assertEquals("local 0.1.0 vs server 0.2.0: needs to be updated", true, UpdateCheck.isUpdateAvailable("0.1.0", "0.2.0"));
        TestCase.assertEquals("local 0.0.1 vs server 0.0.0: ahead", false, UpdateCheck.isUpdateAvailable("0.0.1", "0.0.0"));
        TestCase.assertEquals("local 0.0.1 vs server 0.0.1: up to date", false, UpdateCheck.isUpdateAvailable("0.0.1", "0.0.1"));
        TestCase.assertEquals("local 0.0.1 vs server 0.0.2: needs to be updated", true, UpdateCheck.isUpdateAvailable("0.0.1", "0.0.2"));
        TestCase.assertEquals("local 2.5.2 vs server 2.4.2: ahead", false, UpdateCheck.isUpdateAvailable("2.5.2", "2.4.2"));
        TestCase.assertEquals("local 2.5.2 vs server 2.5.2: up to date", false, UpdateCheck.isUpdateAvailable("2.5.2", "2.5.2"));
        TestCase.assertEquals("local 2.5.2 vs server 2.5.3: needs to be updated", true, UpdateCheck.isUpdateAvailable("2.5.2", "2.5.3"));
        TestCase.assertEquals("local 0.5.10 vs server 0.50.10: needs to be updated", true, UpdateCheck.isUpdateAvailable("0.5.10", "0.50.10"));
        TestCase.assertEquals("local 2.49.2 vs server 2.50.2: needs to be updated", true, UpdateCheck.isUpdateAvailable("2.49.2", "2.50.2"));
        TestCase.assertEquals("local 0.6.0 vs server 0.60.0: needs to be updated", true, UpdateCheck.isUpdateAvailable("0.6.0", "0.60.0"));
        TestCase.assertEquals("local 0.29 vs server 1: needs to be updated", true, UpdateCheck.isUpdateAvailable("0.29", "1"));
        TestCase.assertEquals("local 1.2.x vs server 1.3.x: needs to be updated", true, UpdateCheck.isUpdateAvailable("0.6.0", "0.60.0"));
    }

    @Test
    public void showWhatsNew() {
        TestCase.assertEquals("LAST_WHATS_NEW null -- App version 0.2", true, UpdateCheck.whatsNewCheckTest(null, "0.2"));
        TestCase.assertEquals("LAST_WHATS_NEW 0.1 -- App version 1.0.10", true, UpdateCheck.whatsNewCheckTest("0.1", "1.0.10"));
        TestCase.assertEquals("LAST_WHATS_NEW 1.2.95 -- App version 1.0.10", false, UpdateCheck.whatsNewCheckTest("1.2.95", "1.0.10"));
        TestCase.assertEquals("LAST_WHATS_NEW 0.1 -- App version 0.2", true, UpdateCheck.whatsNewCheckTest("0.1", "0.2"));
        TestCase.assertEquals("LAST_WHATS_NEW 0.3 -- App version 0.3", false, UpdateCheck.whatsNewCheckTest("0.3", "0.3"));
        TestCase.assertEquals("LAST_WHATS_NEW 0.43 -- App version 0.44", true, UpdateCheck.whatsNewCheckTest("0.43", "0.44"));
        TestCase.assertEquals("LAST_WHATS_NEW 0.5 -- App version 0.4", false, UpdateCheck.whatsNewCheckTest("0.5", "0.4"));
    }

    @Test
    public void craftingGrid() {
        float GRID_COLUMNS = 4.0f;
        TestCase.assertEquals("if 23 items, should be 6 rows", 6, MathUtils.ceil(23.0f / GRID_COLUMNS));
        TestCase.assertEquals("if 24 items, should be 6 rows", 6, MathUtils.ceil(24.0f / GRID_COLUMNS));
        TestCase.assertEquals("if 25 items, should be 7 rows", 7, MathUtils.ceil(25.0f / GRID_COLUMNS));
        TestCase.assertEquals("if 26 items, should be 6 rows", 7, MathUtils.ceil(26.0f / GRID_COLUMNS));
        TestCase.assertEquals("if 27 items, should be 6 rows", 7, MathUtils.ceil(27.0f / GRID_COLUMNS));
        TestCase.assertEquals("if 28 items, should be 7 rows", 7, MathUtils.ceil(28.0f / GRID_COLUMNS));
        TestCase.assertEquals("if 29 items, should be 8 rows", 8, MathUtils.ceil(29.0f / GRID_COLUMNS));
        TestCase.assertEquals("if 30 items, should be 8 rows", 8, MathUtils.ceil(30.0f / GRID_COLUMNS));
    }

    @Test
    public void testCopyingSettingsData() throws NoSuchFieldException {
        SettingsData dataOrig = new SettingsData();
        dataOrig.LANGUAGE = "en";
        dataOrig.LAST_WHATSNEW_VERSION = "0.0";
        dataOrig.FULL_SCREEN = false;
        dataOrig.DISPLAY_MODE = 0;
        dataOrig.DISPLAY_MODE_DETAILS = "Can't init libGDX stuff...";
        dataOrig.USE_CONTROLLER = false;
        dataOrig.VIRTUALJOYSTICK = false;
        dataOrig.VIRTUALJOYSTICK_POS = new Vector2(0.0f, 0.0f);
        dataOrig.SOUNDFX_VOLUME = Float.valueOf(1.0f);
        dataOrig.MUSIC_VOLUME = Float.valueOf(1.0f);
        dataOrig.KEYS_UP = Input.Keys.toString(51);
        dataOrig.KEYS_DOWN = Input.Keys.toString(47);
        dataOrig.KEYS_LEFT = Input.Keys.toString(29);
        dataOrig.KEYS_RIGHT = Input.Keys.toString(32);
        dataOrig.KEYS_INVENTORY = Input.Keys.toString(61);
        dataOrig.KEYS_DROP_ITEM = Input.Keys.toString(45);
        dataOrig.KEYS_FLASHLIGHT = Input.Keys.toString(34);
        dataOrig.KEYS_MENU_BACK = Input.Keys.toString(111);
        dataOrig.KEYS_CONSOLE = Input.Keys.toString(131);
        dataOrig.KEYS_MAP = Input.Keys.toString(41);
        dataOrig.KEYS_ROTATE_TILE = Input.Keys.toString(46);
        dataOrig.KEYS_ENTER_VEHICLE = Input.Keys.toString(66);
        dataOrig.KEYS_VEHICLE_DRIFT = Input.Keys.toString(62);
        dataOrig.KEYS_FULLSCREEN = Input.Keys.toString(141);
        dataOrig.KEYS_RESEARCH = Input.Keys.toString(61);
        SettingsData dataCopy = new SettingsData(dataOrig);
        Field[] fields = SettingsData.class.getFields();
        for (int i = 0; i < fields.length; ++i) {
            Field field = fields[i];
            TestCase.assertEquals("Comparing field " + field.getName(), true, this.checkFieldsMatch(dataOrig, dataCopy, field));
        }
    }

    private boolean checkFieldsMatch(SettingsData d1, SettingsData d2, Field fieldToCheck) {
        try {
            Object value1 = fieldToCheck.get(d1);
            Object value2 = fieldToCheck.get(d2);
            if (value1 == null || value2 == null) {
                System.out.println("Value wasn't copied, or one is null. Also check that the test has been updated with all the fields.");
                return false;
            }
            if (value1.equals(value2)) {
                return true;
            }
            System.out.println("Values don't match: " + value1.toString() + " vs " + value2.toString());
            return false;
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
            return true;
        }
    }

    @Test
    public void testSaveItemStripperExpression() {
        String originalJsonText = "t_4,9: {\n\t\tx: 4\n\t\ty: 9\n\t\tname: com.cairn4.moonbase.tiles.Smelter\n\t\tbehaviorDataList: [\n\t\t\t{\n\t\t\t\tbehaviorClass: com.cairn4.moonbase.tiles.behaviors.ItemCrafter\n\t\t\t\tproperties: {\n\t\t\t\t\tbuildTimer: {\n\t\t\t\t\t\tclass: java.lang.Float\n\t\t\t\t\t\tvalue: 8.544072\n\t\t\t\t\t}\n\t\t\t\t\tbuildQueue: [\n\t\t\t\t\t\t{\n\t\t\t\t\t\t\tclass: com.cairn4.moonbase.ItemData\n\t\t\t\t\t\t\tid: refined-scrap\n\t\t\t\t\t\t\tsprite: refined-scrap\n\t\t\t\t\t\t\tbuildable: true\n\t\t\t\t\t\t\tcraftTime: 10\n\t\t\t\t\t\t\trequires: [\n\t\t\t\t\t\t\t\t{\n\t\t\t\t\t\t\t\t\tid: scrap\n\t\t\t\t\t\t\t\t\tquantity: 3\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t]\n\t\t\t\t\t\t\ttechReq: recycling\n\t\t\t\t\t\t}\n\t\t\t\t\t]\t\n\t\t\t\t\tspentItemBufferList: [\n\t\t\t\t\t\t[\n\t\t\t\t\t\t\t{\n\t\t\t\t\t\t\t\tclass: com.cairn4.moonbase.Item\n\t\t\t\t\t\t\t\tid: scrap\n\t\t\t\t\t\t\t\tname: Scrap Metal\n\t\t\t\t\t\t\t\tdesc: Scattered debris from a crashed ship.\n\t\t\t\t\t\t\t\tamount: 3\n\t\t\t\t\t\t\t\tsprite: scrap\n\t\t\t\t\t\t\t\tpickupWith: [\n\t\t\t\t\t\t\t\t\tshovel\n\t\t\t\t\t\t\t\t]\n\t\t\t\t\t\t\t\tpickupTime: 1\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t]\n\t\t\t\t\t]\n\t\t\t\t\tlastSelectedItemId: {\n\t\t\t\t\t\tclass: java.lang.String\n\t\t\t\t\t\tvalue: refined-scrap\n\t\t\t\t\t}\n\t\t\t\t\tbuilding: {\n\t\t\t\t\t\tclass: java.lang.Boolean\n\t\t\t\t\t\tvalue: true\n\t\t\t\t\t}\n\t\t\t\t\titemsToCollect: [\n\t\t\t\t\t{\n\t\t\t\t\t\t\tclass: com.cairn4.moonbase.Item\n\t\t\t\t\t\t\tid: scrap\n\t\t\t\t\t\t\tname: Scrap Metal\n\t\t\t\t\t\t\tdesc: Scattered debris from a crashed ship.\n\t\t\t\t\t\t\tamount: 3\n\t\t\t\t\t\t\tsprite: scrap\n\t\t\t\t\t\t\tpickupTime: 1\n\t\t\t\t\t\t},\n\t\t\t\t\t\t{\n\t\t\t\t\t\t\tclass: com.cairn4.moonbase.Item\n\t\t\t\t\t\t\tid: scrap\n\t\t\t\t\t\t\tname: Scrap Metal\n\t\t\t\t\t\t\tdesc: Scattered debris from a crashed ship.\n\t\t\t\t\t\t\tamount: 3\n\t\t\t\t\t\t\tsprite: scrap\n\t\t\t\t\t\t\tpickupTime: 1\n\t\t\t\t\t\t}\n\t\t\t\t\t]\n\t\t\t\t}\n\t\t\t}\n\t\t]\n\t}";
        String expected = "t_4,9: {\n\t\tx: 4\n\t\ty: 9\n\t\tname: com.cairn4.moonbase.tiles.Smelter\n\t\tbehaviorDataList: [\n\t\t\t{\n\t\t\t\tbehaviorClass: com.cairn4.moonbase.tiles.behaviors.ItemCrafter\n\t\t\t\tproperties: {\n\t\t\t\t\tbuildTimer: {\n\t\t\t\t\t\tclass: java.lang.Float\n\t\t\t\t\t\tvalue: 8.544072\n\t\t\t\t\t}\n\t\t\t\t\tbuildQueue: [\n\t\t\t\t\t\t{\n\t\t\t\t\t\t\tclass: com.cairn4.moonbase.ItemData\n\t\t\t\t\t\t\tid: refined-scrap\n\t\t\t\t\t\t\tsprite: refined-scrap\n\t\t\t\t\t\t\tcraftTime: 10\n\t\t\t\t\t\t\trequires: [\n\t\t\t\t\t\t\t\t{\n\t\t\t\t\t\t\t\t\tid: scrap\n\t\t\t\t\t\t\t\t\tquantity: 3\n\t\t\t\t\t\t\t\t}\n\t\t\t\t\t\t\t]\n\t\t\t\t\t\t\ttechReq: recycling\n\t\t\t\t\t\t}\n\t\t\t\t\t]\t\n\t\t\t\t\tspentItemBufferList: [\n\t\t\t\t\t\t[]\n\t\t\t\t\t]\n\t\t\t\t\tlastSelectedItemId: {\n\t\t\t\t\t\tclass: java.lang.String\n\t\t\t\t\t\tvalue: refined-scrap\n\t\t\t\t\t}\n\t\t\t\t\tbuilding: {\n\t\t\t\t\t\tclass: java.lang.Boolean\n\t\t\t\t\t\tvalue: true\n\t\t\t\t\t}\n\t\t\t\t\titemsToCollect: []\n\t\t\t\t}\n\t\t\t}\n\t\t]\n\t}";
        String updated = SaveFixer.stripItemsToCollect(originalJsonText);
        TestCase.assertEquals("Save fixer should strip out old item class stuff from ItemCrafters", expected, updated);
    }

    @Test
    public void testSpawningPositions() {
        TestCase.assertEquals("World coord (0, 0) should be chunk 0,0", "0,0", World.convertWorldTileToChunkKey(0, 0));
    }

    @Test
    public void negativeChunkCoords() {
        GridPoint2 gp1 = new GridPoint2(5, 7);
        TestCase.assertEquals("Converting worldPos (" + gp1.x + "," + gp1.y + ") to chunk coord should be 0,0", "0,0", World.convertWorldTileToChunkKey(gp1.x, gp1.y));
        gp1.set(0, 3);
        TestCase.assertEquals("Converting worldPos (" + gp1.x + "," + gp1.y + ") to chunk coord should be 0,0", "0,0", World.convertWorldTileToChunkKey(gp1.x, gp1.y));
        gp1.set(-6, 3);
        TestCase.assertEquals("Converting worldPos (" + gp1.x + "," + gp1.y + ") to chunk coord should be -1,0", "-1,0", World.convertWorldTileToChunkKey(gp1.x, gp1.y));
        gp1.set(-14, -27);
        TestCase.assertEquals("Converting worldPos (" + gp1.x + "," + gp1.y + ") to chunk coord should be -2,-3", "-2,-3", World.convertWorldTileToChunkKey(gp1.x, gp1.y));
    }

    @Test
    public void negativeWorldToLocalCoords() {
        GridPoint2 result = new GridPoint2(0, 0);
        GridPoint2 gp1 = new GridPoint2(505, 509);
        GridPoint2 expected = new GridPoint2(5, 9);
        TestCase.assertEquals("Converting world tile (" + gp1.x + "," + gp1.y + ") to local should be " + expected.toString(), expected, World.convertWorldToLocal(result, gp1.x, gp1.y));
        gp1 = new GridPoint2(-15, -17);
        expected = new GridPoint2(5, 3);
        TestCase.assertEquals("Converting world tile (" + gp1.x + "," + gp1.y + ") to local should be " + expected.toString(), expected, World.convertWorldToLocal(result, gp1.x, gp1.y));
    }

    @Test
    public void negativeWorldTilePos() {
        GridPoint2 result = new GridPoint2(0, 0);
        GridPoint2 chunkCoord = new GridPoint2(50, 50);
        GridPoint2 gp1 = new GridPoint2(2, 7);
        GridPoint2 expected = new GridPoint2(502, 507);
        TestCase.assertEquals("World tile x for " + gp1.toString() + " should be " + expected.x, expected.x, GroundTile.calcWorldX(chunkCoord.x, gp1.x));
        chunkCoord = new GridPoint2(-2, 0);
        gp1 = new GridPoint2(2, 7);
        expected = new GridPoint2(-18, 7);
        TestCase.assertEquals("World tile x for " + gp1.toString() + " should be " + expected.x, expected.x, GroundTile.calcWorldX(chunkCoord.x, gp1.x));
    }
}

