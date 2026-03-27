/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import java.util.ArrayList;

public class DropDown
extends Widget
implements Disableable {
    public TextButton textButton;
    public Table table;
    public Image background;
    TextButton.TextButtonStyle mainButtonStyle;
    TextButton.TextButtonStyle optionButtonStyle;
    private ArrayList<String> options = new ArrayList();
    public InputListener hideListener;

    public DropDown(Drawable background, TextButton.TextButtonStyle mainButtonStyle, TextButton.TextButtonStyle optionButtonStyle) {
        this.mainButtonStyle = mainButtonStyle;
        this.optionButtonStyle = optionButtonStyle;
        this.table = new Table();
        this.hideListener = new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Actor target = event.getTarget();
                if (DropDown.this.isAscendantOf(target)) {
                    return false;
                }
                DropDown.this.hide();
                return false;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == 111) {
                    DropDown.this.hide();
                }
                return false;
            }
        };
    }

    public void show() {
    }

    public void hide() {
    }

    public void addOptions(ArrayList<String> newItems) {
        this.options.addAll(newItems);
        for (String s : this.options) {
            TextButton tb = new TextButton(s, this.optionButtonStyle);
            this.table.add(tb);
            this.table.row();
        }
    }

    @Override
    public void setDisabled(boolean isDisabled) {
    }

    @Override
    public boolean isDisabled() {
        return false;
    }
}

