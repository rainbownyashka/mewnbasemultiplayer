/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.utils.controller;

import com.cairn4.utils.controller.ControllerMapping;

public class UsbSnes
extends ControllerMapping {
    public UsbSnes() {
        this.id = "generic";
        this.BUTTON_A = 0;
        this.BUTTON_B = 1;
        this.BUTTON_X = 2;
        this.BUTTON_Y = 3;
        this.BUTTON_LB = 4;
        this.BUTTON_RB = 5;
        this.BUTTON_BACK = 6;
        this.BUTTON_START = 7;
        this.BUTTON_LS = 9;
        this.BUTTON_RS = 10;
        this.BUTTON_DPAD_UP = -9999;
        this.BUTTON_DPAD_DOWN = -9999;
        this.BUTTON_DPAD_LEFT = -9999;
        this.BUTTON_DPAD_RIGHT = -9999;
        this.POV = 0;
        this.AXIS_LX = 0;
        this.AXIS_LY = 1;
        this.AXIS_RX = 3;
        this.AXIS_RY = 2;
        this.AXIS_LT = 4;
        this.AXIS_RT = 4;
        this.reverseXAxis = false;
        this.reverseYAxis = true;
    }
}

