/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.cairn4.moonbase.ui.ControlMapPopup;

public class KeyboardMapAdapter
extends InputAdapter {
    ControlMapPopup controlMapPopup;

    public KeyboardMapAdapter(ControlMapPopup controlMapPopup) {
        this.controlMapPopup = controlMapPopup;
    }

    @Override
    public boolean keyUp(int k) {
        System.out.println("KMA: " + Input.Keys.toString(k));
        this.getAnyKey(k);
        return true;
    }

    public void getAnyKey(int k) {
        this.controlMapPopup.catchKey(Input.Keys.toString(k));
    }
}

