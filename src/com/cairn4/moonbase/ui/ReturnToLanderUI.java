/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Tutorial;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.Localization;

public class ReturnToLanderUI {
    public Hud menu;
    Group parentGroup;
    Tutorial tutorial;
    Group group;
    Table table;
    Label text;
    TextButton textButton;
    private Image tutorialArrow;
    private static float PANEL_SMALL = 146.0f;
    private static float PANEL_LARGE = 220.0f;

    public ReturnToLanderUI(Hud hud, Group parentGroup) {
        this.menu = hud;
        this.parentGroup = parentGroup;
        this.tutorial = this.tutorial;
        this.group = new Group();
        this.parentGroup.addActor(this.group);
        this.setup();
    }

    private void setup() {
        this.group.setPosition(MoonBase.SCREEN_WIDTH - 12, 110.0f);
        this.group.setVisible(false);
        NinePatch panelBg = new NinePatch(this.menu.skin.getRegion("textbox-mini"), 20, 20, 20, 20);
        NinePatchDrawable bg = new NinePatchDrawable(panelBg);
        this.table = new Table();
        this.table.pad(20.0f);
        this.table.setBackground(bg);
        this.group.addActor(this.table);
        this.table.row();
        this.text = new Label((CharSequence)"do some stuff! do some stuff! do some stuff! do some stuff!", this.menu.labelStyle);
        this.text.setFontScale(0.35f);
        this.text.setWrap(true);
        this.text.setAlignment(1, 1);
        this.table.add(this.text).space(10.0f).center().width(360.0f).expandY().fill();
        this.table.row();
        this.textButton = new TextButton(Localization.get("common_ok"), this.menu.baseScreen.textBtnStyle);
        this.textButton.getLabel().setFontScale(0.35f);
        this.textButton.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                ReturnToLanderUI.this.hide();
            }
        });
        this.table.add(this.textButton).space(10.0f).minWidth(160.0f).height(65.0f).center();
        this.updateTable();
    }

    public void setText(String message) {
        this.text.setText(message);
        this.updateTable();
    }

    public void updateTable() {
        this.table.pack();
        this.table.bottom().right();
        this.table.setPosition(0.0f, 0.0f, 20);
    }

    public void show() {
        this.group.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.fadeIn(0.25f)));
    }

    public void hide() {
        this.group.setTouchable(Touchable.disabled);
        this.group.addAction(Actions.sequence((Action)Actions.fadeOut(0.3f), (Action)Actions.visible(false), (Action)Actions.removeActor()));
    }

    public void showButton(boolean buttonVisible) {
        this.textButton.setVisible(buttonVisible);
        if (!buttonVisible) {
            if (this.table.getChildren().contains(this.textButton, true)) {
                this.table.getCell(this.textButton).reset();
                this.table.removeActor(this.textButton);
            }
        } else if (!this.table.getChildren().contains(this.textButton, true)) {
            this.table.row();
            this.table.add(this.textButton).space(10.0f).minWidth(160.0f).height(65.0f).center();
        }
        this.updateTable();
    }
}

