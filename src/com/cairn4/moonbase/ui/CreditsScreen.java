/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.FrontendScreen;
import com.cairn4.moonbase.ui.Localization;

public class CreditsScreen
extends BaseScreen {
    Texture bgTex;
    Image background;
    Label headingLabel;
    Label createdBy;
    Label name;
    Label menuMusic;
    Label createdWithLibgdx;
    TextButton btnContinue;
    Group missionGroup;

    public CreditsScreen(MoonBase game) {
        super(game);
    }

    @Override
    public void show() {
        this.bgTex = AssetManagerSingleton.getInstance().get("MissionBg.png", Texture.class);
        this.bgTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.background = new Image(this.bgTex);
        this.background.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_WIDTH);
        this.mainGroup.addActor(this.background);
        this.missionGroup = new Group();
        this.missionGroup.getColor().a = 0.0f;
        this.mainGroup.addActor(this.missionGroup);
        this.missionGroup.addAction(Actions.fadeIn(0.3f));
        this.headingLabel = new Label((CharSequence)Localization.get("credits_heading"), this.headingStyle);
        this.headingLabel.setWidth(MoonBase.SCREEN_WIDTH);
        this.headingLabel.setAlignment(1);
        this.headingLabel.setFontScale(0.7f);
        this.headingLabel.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 200, 1);
        this.headingLabel.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.missionGroup.addActor(this.headingLabel);
        this.createdBy = new Label((CharSequence)Localization.get("credits_createdBy"), this.labelStyle);
        this.createdBy.setWrap(true);
        this.createdBy.setWidth(600.0f);
        this.createdBy.setAlignment(1);
        this.createdBy.setFontScale(0.6f);
        this.createdBy.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 100, 1);
        this.createdBy.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.missionGroup.addActor(this.createdBy);
        this.name = new Label((CharSequence)Localization.get("credits_steveForde"), this.labelStyle);
        this.name.setWrap(true);
        this.name.setWidth(1000.0f);
        this.name.setAlignment(1);
        this.name.setFontScale(0.85f);
        this.name.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 55, 1);
        this.name.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.missionGroup.addActor(this.name);
        this.createdWithLibgdx = new Label((CharSequence)Localization.get("credits_libgdx"), this.labelStyle);
        this.createdWithLibgdx.setWrap(true);
        this.createdWithLibgdx.setWidth(1000.0f);
        this.createdWithLibgdx.setAlignment(1);
        this.createdWithLibgdx.setFontScale(0.6f);
        this.createdWithLibgdx.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 - 20, 1);
        this.createdWithLibgdx.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.missionGroup.addActor(this.createdWithLibgdx);
        this.menuMusic = new Label((CharSequence)Localization.get("credits_menuMusic"), this.labelStyle);
        this.menuMusic.setWrap(true);
        this.menuMusic.setWidth(1000.0f);
        this.menuMusic.setAlignment(1);
        this.menuMusic.setFontScale(0.6f);
        this.menuMusic.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 - 55, 1);
        this.menuMusic.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.missionGroup.addActor(this.menuMusic);
        this.btnContinue = new TextButton(Localization.get("back").toUpperCase(), this.textBtnStyle);
        this.btnContinue.setSize(250.0f, 100.0f);
        this.btnContinue.getLabel().setFontScale(0.85f);
        this.btnContinue.setPosition(MoonBase.SCREEN_WIDTH / 2, 80.0f, 4);
        this.missionGroup.addActor(this.btnContinue);
        this.btnContinue.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                CreditsScreen.this.nextMenu(new FrontendScreen(CreditsScreen.this.game));
            }
        });
    }

    private void nextMenu(Screen screen) {
        final Screen finalScreen = screen;
        this.mainGroup.addAction(Actions.sequence((Action)Actions.parallel(), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                CreditsScreen.this.game.setScreen(finalScreen);
            }
        })));
    }

    @Override
    public void dispose() {
        this.bgTex.dispose();
        AssetManagerSingleton.getInstance().unload("MissionBg.png");
        this.stage.dispose();
        super.dispose();
    }
}

