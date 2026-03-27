/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cairn4.moonbase.ui.Menu;

public class KeyboardMapButton {
    Menu menu;
    Group group;
    protected Button.ButtonStyle buttonStyle;
    Button button;
    String letter;
    Label keyLabel;

    public KeyboardMapButton(Menu menu, Group parent, String letter) {
        this.menu = menu;
        this.group = new Group();
        parent.addActor(this.group);
        this.letter = letter;
        this.buttonStyle = new Button.ButtonStyle();
        this.buttonStyle.up = menu.skin.getDrawable("btn-item");
        this.buttonStyle.over = menu.skin.getDrawable("btn-item-active");
        this.buttonStyle.down = menu.skin.getDrawable("btn-item-pressed");
        this.buttonStyle.disabled = menu.skin.getDrawable("btn-item-disabled");
        this.createButton();
        this.setupIcon();
    }

    protected void createButton() {
        this.button = new Button(this.buttonStyle);
        this.button.setSize(this.button.getWidth(), this.button.getWidth());
        this.button.setPosition(0.0f, 0.0f);
        this.group.addActor(this.button);
    }

    protected void setupIcon() {
        this.keyLabel = new Label((CharSequence)this.letter, this.menu.labelStyle);
        this.keyLabel.setFontScale(1.0f);
        this.keyLabel.setWidth(this.button.getWidth());
        this.keyLabel.setAlignment(1);
        this.keyLabel.setPosition(this.button.getWidth() / 2.0f, this.button.getHeight() / 2.0f, 1);
        this.group.addActor(this.keyLabel);
        this.keyLabel.setTouchable(Touchable.disabled);
    }

    public void setScale(float scale) {
        this.group.setScale(scale);
    }

    public void setPosition(float x, float y) {
        this.group.setPosition(x, y);
    }

    public float getWidth() {
        return this.button.getWidth();
    }

    public float getX() {
        return this.group.getX();
    }

    public void enable() {
        this.button.setDisabled(false);
        this.button.setTouchable(Touchable.enabled);
        this.keyLabel.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void disable() {
        this.button.setDisabled(true);
        this.button.setTouchable(Touchable.disabled);
        this.keyLabel.setColor(0.5f, 0.5f, 0.5f, 1.0f);
    }
}

