/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.CreatePlayerScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;

public class NewGameConfirm
extends Popup {
    Label headingLabel;
    Label dialogText;
    TextButton btnCancel;
    TextButton btnNewGame;
    Group missionGroup;

    public NewGameConfirm(BaseScreen baseScreen) {
        super(baseScreen);
    }

    @Override
    public void setup() {
    }

    @Override
    public void show() {
        this.missionGroup = new Group();
        this.missionGroup.getColor().a = 0.0f;
        this.popupGroup.addActor(this.missionGroup);
        this.headingLabel = new Label((CharSequence)Localization.get("newGameConfirmTitle"), this.headingStyle);
        this.headingLabel.setWidth(MoonBase.SCREEN_WIDTH);
        this.headingLabel.setAlignment(1);
        this.headingLabel.setFontScale(1.0f);
        this.headingLabel.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 130, 1);
        this.headingLabel.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.missionGroup.addActor(this.headingLabel);
        this.dialogText = new Label((CharSequence)Localization.get("newGameConfirm"), this.labelStyle);
        this.dialogText.setWrap(true);
        this.dialogText.setWidth(660.0f);
        this.dialogText.setAlignment(1);
        this.dialogText.setFontScale(0.5f);
        this.dialogText.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 15, 1);
        this.dialogText.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.missionGroup.addActor(this.dialogText);
        this.btnCancel = new TextButton(Localization.get("newGameConfirmCancel").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnCancel.setSize(300.0f, 90.0f);
        this.btnCancel.getLabel().setFontScale(0.6f);
        this.btnCancel.setPosition(MoonBase.SCREEN_WIDTH / 2 - 20, 155.0f, 20);
        this.missionGroup.addActor(this.btnCancel);
        this.btnCancel.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                NewGameConfirm.this.baseScreen.menuBackSound();
                NewGameConfirm.this.back();
            }
        });
        this.btnNewGame = new TextButton(Localization.get("newGameConfirmYes").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnNewGame.setSize(300.0f, 90.0f);
        this.btnNewGame.getLabel().setFontScale(0.6f);
        this.btnNewGame.setPosition(MoonBase.SCREEN_WIDTH / 2 + 20, 155.0f, 12);
        this.missionGroup.addActor(this.btnNewGame);
        this.btnNewGame.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                NewGameConfirm.this.baseScreen.menuForwardSound();
                NewGameConfirm.this.back();
                NewGameConfirm.this.baseScreen.showMenu(new CreatePlayerScreen(NewGameConfirm.this.baseScreen));
            }
        });
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.handleInput();
    }

    private void nextMenu(Screen screen) {
        this.baseScreen.game.setScreen(screen);
    }
}

