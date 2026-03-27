/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.LoadingErrors;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.SaveFixer;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.LoadingScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;
import java.util.Observable;
import java.util.Observer;

public class UpdateSaveWarningScreen
extends Popup
implements Observer {
    Table table;
    Label headingLabel;
    Label dialogText;
    TextButton btnCancel;
    TextButton btnUpdate;
    private SaveFixer sf;

    public UpdateSaveWarningScreen(BaseScreen baseScreen) {
        super(baseScreen);
        this.setup();
        this.show();
    }

    @Override
    public void setup() {
        super.setup();
        this.tintBgAlpha = 0.8f;
        this.setSize(800.0f, 600.0f);
        this.setTitle("");
        this.panelBg.setColor(Color.RED);
        this.table = new Table();
        this.table.bottom().left();
        this.popupGroup.addActor(this.table);
        this.table.setSize(this.panelBg.getWidth() - 150.0f, this.panelBg.getHeight() - 200.0f);
        this.table.setPosition(75.0f, 75.0f);
        this.dialogText = new Label((CharSequence)Localization.get("saveFixerText"), this.labelStyle);
        this.dialogText.setWrap(true);
        this.dialogText.setAlignment(1);
        this.dialogText.setFontScale(0.5f);
        this.dialogText.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.table.add(this.dialogText).fillX().expandX().spaceBottom(20.0f).colspan(2).expandY().fillY();
        this.table.row();
        this.btnCancel = new TextButton(Localization.get("saveFixerCancel"), this.baseScreen.textBtnStyle);
        this.btnCancel.getLabel().setFontScale(0.5f);
        this.btnCancel.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                UpdateSaveWarningScreen.this.baseScreen.menuBackSound();
                UpdateSaveWarningScreen.this.back();
            }
        });
        this.table.add(this.btnCancel).width(250.0f).height(90.0f).spaceRight(10.0f);
        this.btnUpdate = new TextButton(Localization.get("saveFixerUpdate"), this.baseScreen.textBtnStyle);
        this.btnUpdate.getLabel().setFontScale(0.5f);
        this.btnUpdate.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                UpdateSaveWarningScreen.this.startFixing();
            }
        });
        this.table.add(this.btnUpdate).width(250.0f).height(90.0f).spaceLeft(10.0f);
    }

    private void nextMenu(Screen screen) {
        this.baseScreen.game.setScreen(screen);
    }

    private void startFixing() {
        this.panelBg.setVisible(false);
        this.btnCancel.setVisible(false);
        this.btnCancel.setTouchable(Touchable.disabled);
        this.btnUpdate.setVisible(false);
        this.btnUpdate.setTouchable(Touchable.disabled);
        this.btnClose.setVisible(false);
        this.btnClose.setTouchable(Touchable.disabled);
        this.dialogText.setText("");
        final UpdateSaveWarningScreen usws = this;
        new Thread(new Runnable(){

            @Override
            public void run() {
                UpdateSaveWarningScreen.this.sf = new SaveFixer();
                UpdateSaveWarningScreen.this.sf.addObserver(usws);
                UpdateSaveWarningScreen.this.sf.fixGame();
                Gdx.app.postRunnable(new Runnable(){

                    @Override
                    public void run() {
                        usws.done();
                    }
                });
            }
        }).start();
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o.equals("startFixing")) {
            this.dialogText.setText(Localization.get("saveFixerFixing"));
        }
        if (o.equals("done")) {
            this.dialogText.setText(Localization.get("saveFixerDone"));
        }
        if (o.equals("failedFixing")) {
            Gdx.app.error("MewnBase", "Game failed to load, returning to main menu.");
            this.baseScreen.errorReturnToMainMenu(LoadingErrors.upgradingSaveData);
        }
    }

    public void done() {
        MoonBase cfr_ignored_0 = this.baseScreen.game;
        MoonBase.achievementAdapter.loadedGame();
        this.dialogText.setText(Localization.get("saveFixerDone"));
        this.nextMenu(new LoadingScreen(this.baseScreen.game, false));
    }
}

