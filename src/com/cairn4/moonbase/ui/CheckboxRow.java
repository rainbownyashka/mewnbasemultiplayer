/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.cairn4.moonbase.ui.Menu;

public class CheckboxRow
extends Widget {
    Menu menu;
    public Table table;
    String text;
    protected Button.ButtonStyle buttonStyle;
    boolean checked = false;
    private Image box;
    public Label label;
    private float scale = 1.0f;

    public CheckboxRow(Menu menu, String text, boolean initialState) {
        this.menu = menu;
        this.table = new Table();
        this.text = text;
        this.checked = initialState;
        this.createButton();
        this.setChecked(this.checked);
        this.validate();
    }

    protected void createButton() {
        this.table = new Table();
        this.table.setTouchable(Touchable.enabled);
        this.label = new Label((CharSequence)this.text, this.menu.labelStyle);
        this.label.setFontScale(0.4f);
        this.label.setAlignment(8);
        this.table.add(this.label).fill().expandX().space(5.0f).left();
        this.box = new Image(this.menu.skin.getDrawable("checkbox-off"));
        this.table.add(this.box).minWidth(55.0f);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if (this.checked) {
            this.box.setDrawable(this.menu.skin.getDrawable("checkbox-on"));
        } else {
            this.box.setDrawable(this.menu.skin.getDrawable("checkbox-off"));
        }
    }

    @Override
    public void clearListeners() {
        this.table.clearListeners();
    }

    @Override
    public boolean addListener(EventListener listener) {
        return this.table.addListener(listener);
    }

    @Override
    public float getPrefWidth() {
        return this.table.getPrefWidth();
    }

    @Override
    public float getPrefHeight() {
        return this.table.getPrefHeight();
    }

    @Override
    public float getX() {
        return this.table.getX();
    }

    @Override
    public float getY() {
        return this.table.getY();
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public void setPosition(float x, float y) {
        this.table.setPosition(x, y);
    }

    @Override
    public float getWidth() {
        return this.table.getWidth() * this.scale;
    }

    @Override
    public float getHeight() {
        return this.table.getHeight() * this.scale;
    }

    @Override
    public void setVisible(boolean visible) {
        this.table.setVisible(visible);
    }

    @Override
    public void layout() {
    }
}

