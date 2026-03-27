/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
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

public class TutorialUI {
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

    public TutorialUI(Hud hud, Group parentGroup, Tutorial tutorial) {
        this.menu = hud;
        this.parentGroup = parentGroup;
        this.tutorial = tutorial;
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
        this.textButton = new TextButton(Localization.get("tutorial_next"), this.menu.baseScreen.textBtnStyle);
        this.textButton.getLabel().setFontScale(0.35f);
        this.textButton.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                TutorialUI.this.tutorial.advance();
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
        this.group.setVisible(false);
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

    public void showTutorialArrow(Tutorial.STAGES stage, int inventoryItem) {
        if (this.tutorialArrow != null) {
            this.tutorialArrow.remove();
        }
        Vector2 screenPos = new Vector2(0.0f, 0.0f);
        this.tutorialArrow = new Image(this.menu.skin.getDrawable("flow-arrow"));
        this.tutorialArrow.setOrigin(1);
        this.menu.hudGroup.addActor(this.tutorialArrow);
        this.tutorialArrow.setRotation(0.0f);
        if (stage == Tutorial.STAGES.droppingItems) {
            this.tutorialArrow.setRotation(180.0f);
            Image icon = this.menu.hudInventory.itemButtonList.get((int)inventoryItem).icon;
            screenPos = icon.localToStageCoordinates(new Vector2(icon.getWidth() / 2.0f, icon.getHeight()));
            this.tutorialArrow.setPosition(screenPos.x, screenPos.y + 25.0f, 1);
        }
        if (stage == Tutorial.STAGES.research2) {
            screenPos = this.menu.researchButton.localToStageCoordinates(Vector2.Zero);
            this.tutorialArrow.setPosition(screenPos.x + this.menu.researchButton.getWidth() / 2.0f, screenPos.y - 25.0f, 1);
        }
        this.tutorialArrow.addAction(Actions.forever(Actions.sequence((Action)Actions.moveBy(0.0f, 8.0f, 0.5f, Interpolation.sine), (Action)Actions.moveBy(0.0f, -8.0f, 0.5f, Interpolation.sine))));
    }

    public void removeTutorialArrow() {
        if (this.tutorialArrow != null) {
            this.tutorialArrow.remove();
        }
    }
}

