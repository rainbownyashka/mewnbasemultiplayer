/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.LoadingScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;

public class MissionBriefing
extends Menu {
    Texture bgTex;
    Image background;
    Label headingLabel;
    Label missionLabel;
    TextButton btnContinue;
    Group missionGroup;

    public MissionBriefing(BaseScreen baseScreen) {
        super(baseScreen);
        this.setup();
        this.show();
    }

    @Override
    public void show() {
        super.show();
        this.missionGroup = new Group();
        this.missionGroup.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2, 1);
        NinePatch panelBg2 = new NinePatch(this.skin.getRegion("popup"), 90, 90, 110, 80);
        Image panelBg = new Image(panelBg2);
        panelBg.setSize(850.0f, 612.0f);
        panelBg.setOrigin(1);
        this.missionGroup.addActor(panelBg);
        panelBg.setPosition(0.0f, 0.0f, 1);
        this.missionGroup.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.8f, 0.8f), (Action)Actions.parallel((Action)Actions.fadeIn(0.25f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.25f, Interpolation.sine)), (Action)Actions.delay(0.15f)));
        this.menuGroup.addActor(this.missionGroup);
        this.headingLabel = new Label((CharSequence)Localization.get("missionHeading"), this.headingStyle);
        this.headingLabel.setWidth(MoonBase.SCREEN_WIDTH);
        this.headingLabel.setAlignment(1);
        this.headingLabel.setFontScale(1.0f);
        this.headingLabel.setPosition(0.0f, 120.0f, 1);
        this.headingLabel.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.missionGroup.addActor(this.headingLabel);
        BaseScreen baseScreen = this.baseScreen;
        int missionLength = baseScreen.game.getCurrentMission().getMissionDayLength();
        String missionInfo = "Error formatting this language string. Survive for " + missionLength + " days.";
        try {
            Object[] objectArray = new Object[2];
            BaseScreen baseScreen2 = this.baseScreen;
            objectArray[0] = baseScreen2.game.getCurrentMission().getPlanetName();
            objectArray[1] = missionLength;
            missionInfo = Localization.format("missionIntro", objectArray);
        }
        catch (IllegalArgumentException e) {
            Gdx.app.error("MewnBase", "Mission intro text isn't formatted properly.");
        }
        this.missionLabel = new Label((CharSequence)missionInfo, this.labelStyle);
        this.missionLabel.setWrap(true);
        this.missionLabel.setWidth(600.0f);
        this.missionLabel.setAlignment(1);
        this.missionLabel.setFontScale(0.6f);
        this.missionLabel.setPosition(0.0f, 15.0f, 1);
        this.missionLabel.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.missionGroup.addActor(this.missionLabel);
        this.btnContinue = new TextButton(Localization.get("begin").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnContinue.setSize(350.0f, 90.0f);
        this.btnContinue.getLabel().setFontScale(0.7f);
        this.btnContinue.setPosition(0.0f, -195.0f, 4);
        this.missionGroup.addActor(this.btnContinue);
        this.btnContinue.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MissionBriefing.this.nextMenu();
            }
        });
    }

    private void nextMenu() {
        this.baseScreen.menuForwardSound();
        Audio.getInstance().playSound("sounds/menuForward_gameStart.ogg", 0.7f);
        this.menuGroup.addAction(Actions.sequence((Action)Actions.parallel((Action)Actions.moveBy(0.0f, 200.0f, 0.5f, Interpolation.sineIn), (Action)Actions.fadeOut(0.5f))));
        Image fadeOut = new Image(this.skin.getDrawable("rect"));
        fadeOut.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        fadeOut.setColor(0.0f, 0.0f, 0.0f, 0.0f);
        this.stage.addActor(fadeOut);
        fadeOut.addAction(Actions.sequence((Action)Actions.fadeIn(0.5f), (Action)Actions.delay(0.3f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                MissionBriefing.this.baseScreen.game.setScreen(new LoadingScreen(MissionBriefing.this.baseScreen.game, true));
            }
        })));
    }

    @Override
    public void back() {
        this.font.getData().setScale(1.0f);
        super.back();
    }
}

