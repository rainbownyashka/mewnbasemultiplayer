/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.cairn4.moonbase.SettingsData;
import com.cairn4.moonbase.ui.Localization;

public class SettingsLoader {
    private static SettingsLoader instance;
    private FileHandle settingsDataFile;
    public SettingsData settingsData;
    private static final String SETTINGS_FILE_NAME = "settings.json";

    public static synchronized SettingsLoader getInstance() {
        if (instance == null) {
            instance = new SettingsLoader();
        }
        return instance;
    }

    public SettingsLoader() {
        FileHandle oldSettingsFile = Gdx.files.local("moonbase_settings.json");
        this.settingsDataFile = Gdx.files.local(SETTINGS_FILE_NAME);
        if (oldSettingsFile.exists()) {
            Gdx.app.log("MewnBase", "SettingsLoader: Copying over settings to new file");
            oldSettingsFile.copyTo(this.settingsDataFile);
            oldSettingsFile.delete();
        }
        if (!this.settingsDataFile.exists()) {
            Gdx.app.log("MewnBase", "SettingsLoader: Creating default settings file");
            this.createDefaults();
        }
        Json json = new Json();
        String fileText = this.settingsDataFile.readString();
        try {
            this.settingsData = json.fromJson(SettingsData.class, fileText);
        }
        catch (SerializationException s) {
            Gdx.app.error("MewnBase", "SettingsLoader, there was an error reading the settings json file, rebuild it with defaults");
            this.createDefaults();
        }
        this.validateData(this.settingsData);
    }

    private void validateData(SettingsData sd) {
        Gdx.app.log("MewnBase", "SettingsLoader: validating settings file");
        boolean dirty = false;
        if (sd.LANGUAGE == null) {
            sd.LANGUAGE = Localization.getSystemLanguageOrDefault();
            dirty = true;
        }
        if (sd.LAST_WHATSNEW_VERSION == null) {
            sd.LAST_WHATSNEW_VERSION = "0.0";
            dirty = true;
        }
        if (sd.DISPLAY_MODE_DETAILS == null) {
            this.resetDisplaySettings(sd);
            dirty = true;
        } else if (!sd.DISPLAY_MODE_DETAILS.equals(Gdx.graphics.getDisplayMode(Gdx.graphics.getPrimaryMonitor()).toString())) {
            this.resetDisplaySettings(sd);
            dirty = true;
        }
        if (sd.VIRTUALJOYSTICK_POS == null) {
            sd.VIRTUALJOYSTICK_POS = new Vector2(0.992f, 0.1388f);
        }
        if (sd.SOUNDFX_VOLUME == null) {
            sd.SOUNDFX_VOLUME = Float.valueOf(1.0f);
            dirty = true;
        }
        if (sd.MUSIC_VOLUME == null) {
            sd.MUSIC_VOLUME = Float.valueOf(1.0f);
            dirty = true;
        }
        if (sd.KEYS_UP == null) {
            sd.KEYS_UP = Input.Keys.toString(51);
            dirty = true;
        }
        if (sd.KEYS_DOWN == null) {
            sd.KEYS_DOWN = Input.Keys.toString(47);
            dirty = true;
        }
        if (sd.KEYS_LEFT == null) {
            sd.KEYS_LEFT = Input.Keys.toString(29);
            dirty = true;
        }
        if (sd.KEYS_RIGHT == null) {
            sd.KEYS_RIGHT = Input.Keys.toString(32);
            dirty = true;
        }
        if (sd.KEYS_INVENTORY == null) {
            sd.KEYS_INVENTORY = Input.Keys.toString(37);
            dirty = true;
        }
        if (sd.KEYS_FLASHLIGHT == null) {
            sd.KEYS_FLASHLIGHT = Input.Keys.toString(34);
            dirty = true;
        }
        if (sd.KEYS_DROP_ITEM == null) {
            sd.KEYS_DROP_ITEM = Input.Keys.toString(45);
            dirty = true;
        }
        if (sd.KEYS_MENU_BACK == null) {
            sd.KEYS_MENU_BACK = Input.Keys.toString(111);
            dirty = true;
        }
        if (sd.KEYS_CONSOLE == null) {
            sd.KEYS_CONSOLE = Input.Keys.toString(131);
            dirty = true;
        }
        if (sd.KEYS_MAP == null) {
            sd.KEYS_MAP = Input.Keys.toString(41);
            dirty = true;
        }
        if (sd.KEYS_ROTATE_TILE == null) {
            sd.KEYS_ROTATE_TILE = Input.Keys.toString(46);
            dirty = true;
        }
        if (sd.KEYS_ENTER_VEHICLE == null) {
            sd.KEYS_ENTER_VEHICLE = Input.Keys.toString(66);
            dirty = true;
        }
        if (sd.KEYS_VEHICLE_DRIFT == null) {
            sd.KEYS_VEHICLE_DRIFT = Input.Keys.toString(62);
            dirty = true;
        }
        if (sd.KEYS_FULLSCREEN == null) {
            sd.KEYS_FULLSCREEN = Input.Keys.toString(141);
            dirty = true;
        }
        if (sd.KEYS_RESEARCH == null) {
            sd.KEYS_RESEARCH = Input.Keys.toString(61);
            dirty = true;
        }
        if (dirty) {
            Gdx.app.log("MewnBase", "SettingsLoader: found 1 or more missing settings entries, adding...");
            this.writeFile(sd);
        }
    }

    private void resetDisplaySettings(SettingsData sd) {
        sd.DISPLAY_MODE = 0;
        sd.DISPLAY_MODE_DETAILS = Gdx.graphics.getDisplayMode(Gdx.graphics.getPrimaryMonitor()).toString();
        sd.FULL_SCREEN = false;
        sd.FULLHD = false;
        sd.VSYNC = true;
    }

    public void createDefaults() {
        this.settingsData = new SettingsData();
        this.settingsData.LANGUAGE = Localization.getSystemLanguageOrDefault();
        this.settingsData.LAST_WHATSNEW_VERSION = "0.0";
        this.settingsData.FULL_SCREEN = false;
        this.settingsData.FULLHD = false;
        this.settingsData.VSYNC = true;
        this.settingsData.DISPLAY_MODE = 0;
        this.settingsData.DISPLAY_MODE_DETAILS = Gdx.graphics.getDisplayMode(Gdx.graphics.getPrimaryMonitor()).toString();
        this.settingsData.USE_CONTROLLER = false;
        this.settingsData.VIRTUALJOYSTICK = false;
        this.settingsData.VIRTUALJOYSTICK_POS = new Vector2(0.992f, 0.1388f);
        this.settingsData.SOUNDFX_VOLUME = Float.valueOf(1.0f);
        this.settingsData.MUSIC_VOLUME = Float.valueOf(1.0f);
        this.settingsData.KEYS_UP = Input.Keys.toString(51);
        this.settingsData.KEYS_DOWN = Input.Keys.toString(47);
        this.settingsData.KEYS_LEFT = Input.Keys.toString(29);
        this.settingsData.KEYS_RIGHT = Input.Keys.toString(32);
        this.settingsData.KEYS_INVENTORY = Input.Keys.toString(61);
        this.settingsData.KEYS_DROP_ITEM = Input.Keys.toString(45);
        this.settingsData.KEYS_FLASHLIGHT = Input.Keys.toString(34);
        this.settingsData.KEYS_MENU_BACK = Input.Keys.toString(111);
        this.settingsData.KEYS_CONSOLE = Input.Keys.toString(131);
        this.settingsData.KEYS_MAP = Input.Keys.toString(41);
        this.settingsData.KEYS_ROTATE_TILE = Input.Keys.toString(46);
        this.settingsData.KEYS_ENTER_VEHICLE = Input.Keys.toString(66);
        this.settingsData.KEYS_VEHICLE_DRIFT = Input.Keys.toString(62);
        this.settingsData.KEYS_FULLSCREEN = Input.Keys.toString(141);
        this.settingsData.KEYS_RESEARCH = Input.Keys.toString(61);
        this.writeFile(this.settingsData);
    }

    public void writeFile(SettingsData s) {
        Gdx.app.log("MewnBase", "SettingsLoader: Writing settings to disk.");
        this.settingsDataFile = Gdx.files.local(SETTINGS_FILE_NAME);
        Json json = new Json();
        json.setUsePrototypes(false);
        this.settingsDataFile.writeString(json.prettyPrint(s), false);
        this.settingsData = s;
    }

    public String getFullscreenResolutionString() {
        Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayModes(Gdx.graphics.getPrimaryMonitor())[SettingsLoader.getInstance().settingsData.DISPLAY_MODE];
        return displayMode.width + "x" + displayMode.height + " - " + displayMode.refreshRate + "Hz";
    }

    public void save() {
        if (instance != null) {
            this.writeFile(SettingsLoader.instance.settingsData);
        }
    }
}

