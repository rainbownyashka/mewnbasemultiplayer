/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.utils.controller;

import com.cairn4.utils.controller.ControllerMapping;

public class XboxLinux
extends ControllerMapping {
    public XboxLinux() {
        this.id = "xbox";
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
        this.BUTTON_DPAD_UP = 13;
        this.BUTTON_DPAD_DOWN = 14;
        this.BUTTON_DPAD_LEFT = 11;
        this.BUTTON_DPAD_RIGHT = 12;
        this.POV = 0;
        this.AXIS_LX = 0;
        this.AXIS_LY = 1;
        this.AXIS_RX = 3;
        this.AXIS_RY = 4;
        this.AXIS_LT = 2;
        this.AXIS_RT = 5;
        this.reverseXAxis = false;
        this.reverseYAxis = true;
    }
}

