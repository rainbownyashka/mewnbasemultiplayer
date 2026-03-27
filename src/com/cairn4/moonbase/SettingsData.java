/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

public class SettingsData {
    public String LANGUAGE;
    public String LAST_WHATSNEW_VERSION;
    public boolean FULLHD;
    public boolean VSYNC;
    public boolean FULL_SCREEN;
    public int DISPLAY_MODE;
    public String DISPLAY_MODE_DETAILS;
    public boolean USE_CONTROLLER;
    public boolean VIRTUALJOYSTICK;
    public Vector2 VIRTUALJOYSTICK_POS;
    public Float SOUNDFX_VOLUME;
    public Float MUSIC_VOLUME;
    public String KEYS_UP;
    public String KEYS_DOWN;
    public String KEYS_LEFT;
    public String KEYS_RIGHT;
    public String KEYS_INVENTORY;
    public String KEYS_DROP_ITEM;
    public String KEYS_FLASHLIGHT;
    public String KEYS_MAP;
    public String KEYS_ROTATE_TILE;
    public String KEYS_MENU_BACK;
    public String KEYS_CONSOLE;
    public String KEYS_ENTER_VEHICLE;
    public String KEYS_VEHICLE_DRIFT;
    public String KEYS_FULLSCREEN;
    public String KEYS_RESEARCH;

    public static int getValue(String key) {
        return Input.Keys.valueOf(key);
    }

    public SettingsData() {
    }

    public SettingsData(SettingsData another) {
        this.LANGUAGE = another.LANGUAGE;
        this.LAST_WHATSNEW_VERSION = another.LAST_WHATSNEW_VERSION;
        this.FULLHD = another.FULLHD;
        this.VSYNC = another.VSYNC;
        this.FULL_SCREEN = another.FULL_SCREEN;
        this.DISPLAY_MODE = another.DISPLAY_MODE;
        this.DISPLAY_MODE_DETAILS = another.DISPLAY_MODE_DETAILS;
        this.USE_CONTROLLER = another.USE_CONTROLLER;
        this.VIRTUALJOYSTICK = another.VIRTUALJOYSTICK;
        this.VIRTUALJOYSTICK_POS = another.VIRTUALJOYSTICK_POS;
        this.SOUNDFX_VOLUME = another.SOUNDFX_VOLUME;
        this.MUSIC_VOLUME = another.MUSIC_VOLUME;
        this.KEYS_UP = another.KEYS_UP;
        this.KEYS_DOWN = another.KEYS_DOWN;
        this.KEYS_LEFT = another.KEYS_LEFT;
        this.KEYS_RIGHT = another.KEYS_RIGHT;
        this.KEYS_INVENTORY = another.KEYS_INVENTORY;
        this.KEYS_DROP_ITEM = another.KEYS_DROP_ITEM;
        this.KEYS_FLASHLIGHT = another.KEYS_FLASHLIGHT;
        this.KEYS_MAP = another.KEYS_MAP;
        this.KEYS_ROTATE_TILE = another.KEYS_ROTATE_TILE;
        this.KEYS_MENU_BACK = another.KEYS_MENU_BACK;
        this.KEYS_CONSOLE = another.KEYS_CONSOLE;
        this.KEYS_ENTER_VEHICLE = another.KEYS_ENTER_VEHICLE;
        this.KEYS_VEHICLE_DRIFT = another.KEYS_VEHICLE_DRIFT;
        this.KEYS_FULLSCREEN = another.KEYS_FULLSCREEN;
        this.KEYS_RESEARCH = another.KEYS_RESEARCH;
    }
}

