/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.GameLoader;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.LoadGameMenu;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;

public class LoadOldSaveFilePopup
extends Popup {
    Table table;
    Label dialogText;
    TextButton btnCancel;
    TextButton btnLoad;
    String saveFolder;
    int saveSlot;
    LoadGameMenu loadGameMenu;

    public LoadOldSaveFilePopup(LoadGameMenu loadGameMenu, String saveFolder, int saveSlot) {
        super(loadGameMenu.baseScreen);
        this.loadGameMenu = loadGameMenu;
        this.saveFolder = saveFolder;
        this.saveSlot = saveSlot;
        this.setup();
        this.show();
    }

    @Override
    public void setup() {
        super.setup();
        this.tintBgAlpha = 0.8f;
        this.setSize(700.0f, 440.0f);
        this.setTitle("newGameConfirmTitle");
        this.table = new Table();
        this.table.bottom().left();
        this.popupGroup.addActor(this.table);
        this.table.setSize(this.panelBg.getWidth() - 150.0f, this.panelBg.getHeight() - 200.0f);
        this.table.setPosition(75.0f, 75.0f);
        this.panelBg.setColor(Color.RED);
        this.dialogText = new Label((CharSequence)Localization.get("loadOldSaveConfirm"), this.labelStyle);
        this.dialogText.setWrap(true);
        this.dialogText.setAlignment(1);
        this.dialogText.setFontScale(0.5f);
        this.dialogText.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 15, 1);
        this.dialogText.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.table.add(this.dialogText).fillX().expandX().spaceBottom(40.0f).colspan(2).expandY().fillY();
        this.table.row();
        this.btnCancel = new TextButton(Localization.get("newGameConfirmCancel").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnCancel.getLabel().setFontScale(0.5f);
        this.table.add(this.btnCancel).width(250.0f).height(90.0f).spaceRight(10.0f);
        this.btnCancel.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                LoadOldSaveFilePopup.this.baseScreen.menuBackSound();
                LoadOldSaveFilePopup.this.back();
            }
        });
        this.btnLoad = new TextButton(Localization.get("continue").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnLoad.getLabel().setFontScale(0.5f);
        this.table.add(this.btnLoad).width(250.0f).height(90.0f).spaceLeft(10.0f);
        this.btnLoad.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                LoadOldSaveFilePopup.this.loadGameMenu.baseScreen.menuForwardSound();
                GameLoader.cycleAutoSavesBeforeLoad(LoadOldSaveFilePopup.this.saveFolder, LoadOldSaveFilePopup.this.saveSlot);
                LoadOldSaveFilePopup.this.loadGameMenu.loadGame(LoadOldSaveFilePopup.this.saveFolder);
            }
        });
    }
}

