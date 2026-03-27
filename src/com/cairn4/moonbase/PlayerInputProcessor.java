/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.SettingsData;
import com.cairn4.moonbase.SettingsLoader;

public class PlayerInputProcessor
extends InputAdapter
implements ControllerListener {
    private SettingsData sd;
    public static ControllerMapping controllerMapping = null;
    public static final boolean is_win = System.getProperty("os.name").toLowerCase().contains("windows");
    public static final boolean is_linux = System.getProperty("os.name").toLowerCase().contains("linux");
    public static final boolean is_mac = System.getProperty("os.name").toLowerCase().contains("mac");
    public static float stickDeadZone = 0.2f;

    public PlayerInputProcessor() {
        this.sd = SettingsLoader.getInstance().settingsData;
        if (this.sd.USE_CONTROLLER) {
            this.setupControllers();
            Controllers.addListener(this);
        }
    }

    @Override
    public boolean keyDown(int k) {
        if (k == SettingsData.getValue(this.sd.KEYS_LEFT)) {
            PlayerInput.setKey(0, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_RIGHT)) {
            PlayerInput.setKey(1, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_UP)) {
            PlayerInput.setKey(2, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_DOWN)) {
            PlayerInput.setKey(3, true);
        }
        if (k == 62) {
            PlayerInput.setKey(4, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_INVENTORY)) {
            PlayerInput.setKey(5, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_MENU_BACK)) {
            PlayerInput.setKey(6, true);
        }
        if (k == 71) {
            PlayerInput.setKey(7, true);
        }
        if (k == 72) {
            PlayerInput.setKey(8, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_DROP_ITEM)) {
            PlayerInput.setKey(9, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_FLASHLIGHT)) {
            PlayerInput.setKey(11, true);
        }
        if (k == 8) {
            PlayerInput.setKey(12, true);
        }
        if (k == 9) {
            PlayerInput.setKey(13, true);
        }
        if (k == 10) {
            PlayerInput.setKey(14, true);
        }
        if (k == 11) {
            PlayerInput.setKey(15, true);
        }
        if (k == 12) {
            PlayerInput.setKey(16, true);
        }
        if (k == 13) {
            PlayerInput.setKey(17, true);
        }
        if (k == 14) {
            PlayerInput.setKey(18, true);
        }
        if (k == 15) {
            PlayerInput.setKey(19, true);
        }
        if (k == 16) {
            PlayerInput.setKey(20, true);
        }
        if (k == 7) {
            PlayerInput.setKey(21, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_MAP)) {
            PlayerInput.setKey(22, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_ROTATE_TILE)) {
            PlayerInput.setKey(23, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_ENTER_VEHICLE)) {
            PlayerInput.setKey(24, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_VEHICLE_DRIFT)) {
            PlayerInput.setKey(25, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_FULLSCREEN)) {
            PlayerInput.setKey(26, true);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_RESEARCH)) {
            PlayerInput.setKey(27, true);
        }
        return true;
    }

    @Override
    public boolean keyUp(int k) {
        if (k == SettingsData.getValue(this.sd.KEYS_LEFT)) {
            PlayerInput.setKey(0, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_RIGHT)) {
            PlayerInput.setKey(1, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_UP)) {
            PlayerInput.setKey(2, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_DOWN)) {
            PlayerInput.setKey(3, false);
        }
        if (k == 62) {
            PlayerInput.setKey(4, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_INVENTORY)) {
            PlayerInput.setKey(5, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_MENU_BACK)) {
            PlayerInput.setKey(6, false);
        }
        if (k == 71) {
            PlayerInput.setKey(7, false);
        }
        if (k == 72) {
            PlayerInput.setKey(8, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_DROP_ITEM)) {
            PlayerInput.setKey(9, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_FLASHLIGHT)) {
            PlayerInput.setKey(11, false);
        }
        if (k == 8) {
            PlayerInput.setKey(12, false);
        }
        if (k == 9) {
            PlayerInput.setKey(13, false);
        }
        if (k == 10) {
            PlayerInput.setKey(14, false);
        }
        if (k == 11) {
            PlayerInput.setKey(15, false);
        }
        if (k == 12) {
            PlayerInput.setKey(16, false);
        }
        if (k == 13) {
            PlayerInput.setKey(17, false);
        }
        if (k == 14) {
            PlayerInput.setKey(18, false);
        }
        if (k == 15) {
            PlayerInput.setKey(19, false);
        }
        if (k == 16) {
            PlayerInput.setKey(20, false);
        }
        if (k == 7) {
            PlayerInput.setKey(21, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_MAP)) {
            PlayerInput.setKey(22, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_ROTATE_TILE)) {
            PlayerInput.setKey(23, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_ENTER_VEHICLE)) {
            PlayerInput.setKey(24, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_VEHICLE_DRIFT)) {
            PlayerInput.setKey(25, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_FULLSCREEN)) {
            PlayerInput.setKey(26, false);
        }
        if (k == SettingsData.getValue(this.sd.KEYS_RESEARCH)) {
            PlayerInput.setKey(27, false);
        }
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (amountY > 0.0f) {
            PlayerInput.SCROLLED_UP = true;
        } else if (amountY < 0.0f) {
            PlayerInput.SCROLLED_DOWN = true;
        }
        Gdx.app.debug("MoonBase", "PlayerInputProcessor: Scrolled " + amountY);
        return amountX != 0.0f || amountY != 0.0f;
    }

    public void setupControllers() {
        if (Controllers.getControllers().size > 0) {
            Controller controller = Controllers.getControllers().first();
            System.out.println("1st controller connected: " + controller.getName());
            controllerMapping = controller.getMapping();
        } else {
            Gdx.app.log("Controllers", "No controllers found.");
        }
        if (controllerMapping != null) {
            Gdx.app.log("Controllers", "Setup controller mapping : " + controllerMapping.getClass().getSimpleName());
        }
    }

    @Override
    public void connected(Controller controller) {
        Gdx.app.log("Controllers", "Controller " + controller.getName() + " connected.");
        if (Controllers.getControllers().first().equals(controller)) {
            this.setupControllers();
        } else {
            Gdx.app.log("Controllers", "Not first controller, ignoring");
        }
    }

    @Override
    public void disconnected(Controller controller) {
        Gdx.app.log("Controllers", "Controller " + controller.getName() + " disconnected.");
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonBack) {
            PlayerInput.setKey(22, true);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonStart) {
            PlayerInput.setKey(6, true);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonY) {
            PlayerInput.setKey(24, true);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonX) {
            PlayerInput.setKey(28, true);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonA) {
            PlayerInput.setKey(29, true);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonB) {
            PlayerInput.setKey(25, true);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonDpadUp) {
            PlayerInput.setKey(11, true);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonDpadDown) {
            PlayerInput.setKey(9, true);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonDpadLeft) {
            PlayerInput.setKey(23, true);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonDpadRight) {
            PlayerInput.setKey(27, true);
        }
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        System.out.println("button up: " + buttonCode);
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonBack) {
            PlayerInput.setKey(22, false);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonStart) {
            PlayerInput.setKey(6, false);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonY) {
            PlayerInput.setKey(24, false);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonX) {
            PlayerInput.setKey(28, false);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonA) {
            PlayerInput.setKey(29, false);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonB) {
            PlayerInput.setKey(25, false);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonL1) {
            this.scrolled(0.0f, -1.0f);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonR1) {
            this.scrolled(0.0f, 1.0f);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonDpadUp) {
            PlayerInput.setKey(11, false);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonDpadDown) {
            PlayerInput.setKey(9, false);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonDpadLeft) {
            PlayerInput.setKey(23, false);
        }
        if (buttonCode == PlayerInputProcessor.controllerMapping.buttonDpadRight) {
            PlayerInput.setKey(27, false);
        }
        return false;
    }

    private void turnOffCursor() {
        if (PlayerInput.lastUsedMouse) {
            PlayerInput.lastUsedMouse = false;
            MoonBase.setCursorVisible(false);
        }
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        if (axisCode == PlayerInputProcessor.controllerMapping.axisLeftX) {
            PlayerInput.rawStickX = value;
            if (value > stickDeadZone || value < -stickDeadZone) {
                PlayerInput.stickMoveX = value;
                this.turnOffCursor();
            } else {
                PlayerInput.stickMoveX = 0.0f;
            }
        }
        if (axisCode == PlayerInputProcessor.controllerMapping.axisLeftY) {
            PlayerInput.rawStickY = value;
            if (value > stickDeadZone || value < -stickDeadZone) {
                PlayerInput.stickMoveY = -value;
                this.turnOffCursor();
            } else {
                PlayerInput.stickMoveY = 0.0f;
            }
        }
        if (axisCode == PlayerInputProcessor.controllerMapping.axisRightX) {
            if (value > stickDeadZone || value < -stickDeadZone) {
                PlayerInput.stick2MoveX = value;
                this.turnOffCursor();
            } else {
                PlayerInput.stick2MoveX = 0.0f;
            }
        }
        if (axisCode == PlayerInputProcessor.controllerMapping.axisRightY) {
            if (value > stickDeadZone || value < -stickDeadZone) {
                PlayerInput.stick2MoveY = -value;
                this.turnOffCursor();
            } else {
                PlayerInput.stick2MoveY = 0.0f;
            }
        }
        if (axisCode == 4) {
            PlayerInput.leftTriggerValue = Math.abs(value);
            if (value > stickDeadZone && !PlayerInput.isDown(29)) {
                PlayerInput.setKey(29, true);
            } else if (value <= stickDeadZone && PlayerInput.isDown(29)) {
                PlayerInput.setKey(29, false);
            }
        }
        if (axisCode == 5) {
            PlayerInput.rightTriggerValue = Math.abs(value);
            if (value > stickDeadZone && !PlayerInput.isDown(28)) {
                PlayerInput.setKey(28, true);
            } else if (value <= stickDeadZone && PlayerInput.isDown(28)) {
                PlayerInput.setKey(28, false);
            }
        }
        return false;
    }
}

