/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.cairn4.moonbase.GameLoader;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.FrontendScreen;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;
import com.cairn4.moonbase.worlddata.GameSaveData;
import java.util.Date;

public class QuitWithoutSavePopup
extends Popup {
    public QuitWithoutSavePopup(GameScreen gameScreen) {
        super(gameScreen);
        this.setup();
        this.show();
    }

    @Override
    public void setup() {
        super.setup();
        this.tintBgAlpha = 0.8f;
        this.setSize(750.0f, 500.0f);
        this.setTitle("");
        this.panelBg.setColor(Color.RED);
        Table table = new Table();
        table.bottom().left();
        this.popupGroup.addActor(table);
        table.setSize(this.panelBg.getWidth() - 150.0f, this.panelBg.getHeight() - 200.0f);
        table.setPosition(75.0f, 75.0f);
        Image warningIcon = new Image(this.baseScreen.skin.getDrawable("base-warning"));
        warningIcon.setOrigin(1);
        warningIcon.setScaling(Scaling.fit);
        table.add(warningIcon).colspan(2).padTop(20.0f).height(100.0f).center();
        warningIcon.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.rotateTo(-10.0f), (Action)Actions.scaleTo(1.9f, 1.9f), (Action)Actions.parallel((Action)Actions.fadeIn(0.3f), (Action)Actions.forever(Actions.parallel((Action)Actions.sequence((Action)Actions.rotateTo(10.0f, 0.6f, Interpolation.sineIn), (Action)Actions.rotateTo(-10.0f, 0.6f, Interpolation.sineIn)), (Action)Actions.sequence((Action)Actions.scaleTo(1.1f, 1.1f, 0.3f, Interpolation.sine), (Action)Actions.scaleTo(1.0f, 1.0f, 0.3f, Interpolation.sine), (Action)Actions.scaleTo(1.1f, 1.1f, 0.3f, Interpolation.sine), (Action)Actions.scaleTo(1.0f, 1.0f, 0.3f, Interpolation.sine)))))));
        table.row();
        Label headingLabel = new Label((CharSequence)Localization.get("areYouSureQuit"), this.labelStyle);
        headingLabel.setAlignment(1);
        headingLabel.setFontScale(0.5f);
        headingLabel.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        headingLabel.setWrap(true);
        table.add(headingLabel).colspan(2).fillX().spaceBottom(15.0f).padTop(10.0f);
        table.row();
        int minSinceLastSave = this.getMinutesSinceLastSave();
        Label dialogText = new Label((CharSequence)Localization.format("lastSaveWas", minSinceLastSave), this.labelStyle);
        dialogText.setWrap(true);
        dialogText.setAlignment(1);
        dialogText.setFontScale(0.4f);
        dialogText.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        table.add(dialogText).fillX().expandX().spaceBottom(40.0f).colspan(2).expandY().fillY();
        table.row();
        TextButton btnQuitAnyways = new TextButton(Localization.get("quit").toUpperCase(), this.baseScreen.textBtnStyle);
        btnQuitAnyways.getLabel().setFontScale(0.5f);
        btnQuitAnyways.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameScreen gameScreen = (GameScreen)QuitWithoutSavePopup.this.baseScreen;
                gameScreen.dispose();
                gameScreen.game.loadFrontendAssets();
                gameScreen.game.setScreen(new FrontendScreen(gameScreen.game));
            }
        });
        table.add(btnQuitAnyways).width(250.0f).height(70.0f).spaceLeft(10.0f);
        TextButton btnCancel = new TextButton(Localization.get("cancel").toUpperCase(), this.baseScreen.textBtnStyle);
        btnCancel.getLabel().setFontScale(0.5f);
        btnCancel.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                QuitWithoutSavePopup.this.baseScreen.menuBackSound();
                QuitWithoutSavePopup.this.back();
            }
        });
        table.add(btnCancel).width(250.0f).height(70.0f).spaceRight(10.0f);
    }

    private GameSaveData getGameSaveData() {
        return GameLoader.getGameSaveData(MoonBase.currentSaveFolder);
    }

    private int getMinutesSinceLastSave() {
        long lastLoad = ((GameScreen)this.baseScreen).getLastLoad();
        long currentTime = new Date().getTime();
        long diff = currentTime - lastLoad;
        int minutes = (int)((diff /= 1000L) / 60L);
        return minutes;
    }
}

