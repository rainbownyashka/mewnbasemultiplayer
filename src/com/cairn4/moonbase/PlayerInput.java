/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.cairn4.moonbase.MoonBase;

public class PlayerInput {
    public static boolean[] keys;
    public static boolean[] prevKeys;
    public static final int NUM_KEYS = 30;
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int UP = 2;
    public static final int DOWN = 3;
    public static final int BUTTON5 = 4;
    public static final int INVENTORY = 5;
    public static final int MENU_BACK = 6;
    public static final int BUTTON8 = 7;
    public static final int BUTTON9 = 8;
    public static final int DROP_ITEM = 9;
    public static final int FLASHLIGHT = 11;
    public static final int INVENTORY_1 = 12;
    public static final int INVENTORY_2 = 13;
    public static final int INVENTORY_3 = 14;
    public static final int INVENTORY_4 = 15;
    public static final int INVENTORY_5 = 16;
    public static final int INVENTORY_6 = 17;
    public static final int INVENTORY_7 = 18;
    public static final int INVENTORY_8 = 19;
    public static final int INVENTORY_9 = 20;
    public static final int INVENTORY_10 = 21;
    public static final int MAP = 22;
    public static final int ROTATE_TILE = 23;
    public static final int ENTER_VEHICLE = 24;
    public static final int VEHICLE_DRIFT = 25;
    public static final int FULLSCREEN = 26;
    public static final int RESEARCH = 27;
    public static final int INTERACT = 28;
    public static final int INTERACT_SECONDARY = 29;
    public static float rawStickX;
    public static float rawStickY;
    public static float stickMoveX;
    public static float stickMoveY;
    public static float stick2MoveX;
    public static float stick2MoveY;
    public static float leftTriggerValue;
    public static float rightTriggerValue;
    public static boolean lastUsedMouse;
    private static float lastMouseX;
    private static float lastMouseY;
    public static boolean SCROLLED_DOWN;
    public static boolean SCROLLED_UP;

    public static void update() {
        for (int i = 0; i < 30; ++i) {
            PlayerInput.prevKeys[i] = keys[i];
        }
        SCROLLED_DOWN = false;
        SCROLLED_UP = false;
        if (!(lastUsedMouse || (float)Gdx.input.getX() == lastMouseX && (float)Gdx.input.getY() == lastMouseY)) {
            lastUsedMouse = true;
            lastMouseX = Gdx.input.getX();
            lastMouseY = Gdx.input.getY();
            MoonBase.setCursorVisible(true);
        }
    }

    public static void clearAll() {
        for (int i = 0; i < 30; ++i) {
            PlayerInput.prevKeys[i] = false;
            PlayerInput.keys[i] = false;
            SCROLLED_DOWN = false;
            SCROLLED_UP = false;
        }
    }

    public static void setKey(int i, boolean b) {
        PlayerInput.keys[i] = b;
    }

    public static boolean isDown(int i) {
        return keys[i];
    }

    public static boolean isPressed(int i) {
        return keys[i] && !prevKeys[i];
    }

    public static boolean wasPressed(int i) {
        return !keys[i] && prevKeys[i];
    }

    public static boolean scrolledUp() {
        return SCROLLED_UP;
    }

    public static boolean scrolledDown() {
        return SCROLLED_DOWN;
    }

    static {
        rawStickX = 0.0f;
        rawStickY = 0.0f;
        stickMoveX = 0.0f;
        stickMoveY = 0.0f;
        stick2MoveX = 0.0f;
        stick2MoveY = 0.0f;
        leftTriggerValue = 0.0f;
        rightTriggerValue = 0.0f;
        lastUsedMouse = true;
        lastMouseX = -9999.0f;
        lastMouseY = -9999.0f;
        SCROLLED_DOWN = false;
        SCROLLED_UP = true;
        keys = new boolean[30];
        prevKeys = new boolean[30];
    }
}

