/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.ui.FrontendScreen;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import com.cairn4.moonbase.ui.QuitWithoutSavePopup;
import com.cairn4.moonbase.ui.SettingsPopup;
import com.cairn4.utils.ScreenshotGrabber;

public class PausePopup
extends Popup {
    GameScreen gameScreen;
    Label headingLabel;
    TextButton btnResume;
    TextButton btnOptions;
    TextButton btnControls;
    TextButton btnSave;
    TextButton btnExit;
    Button btnFullScreen;
    Table buttonTable;
    private Group constellations;
    private ParticleActor stars;
    private Texture cloud1;
    private Texture cloud2;
    private boolean saved = false;

    public PausePopup(GameScreen gameScreen) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        this.setup();
        this.show();
        gameScreen.hud.hudNotifications.reparentGroup(this.stage.getRoot());
    }

    @Override
    public void setup() {
        super.setup();
        this.setSize(700.0f, 600.0f);
        Mission mission = MoonBase.getCurrentMission();
        String missionDayGoal = "" + mission.getMissionDayLength();
        if (mission.missionType == Mission.MissionTypes.tutorial) {
            missionDayGoal = missionDayGoal + " - " + Localization.get("newgame_tutorial");
        } else if (mission.missionType == Mission.MissionTypes.creative) {
            missionDayGoal = missionDayGoal + " - " + Localization.get("newgame_creative");
        }
        this.titleLabel.setText(Localization.format("dayHud", this.gameScreen.world.dayCycle.getDay(), missionDayGoal));
        this.buttonTable = new Table();
        this.buttonTable.align(1);
        this.buttonTable.center();
        this.buttonTable.setFillParent(true);
        this.buttonTable.pad(220.0f, 30.0f, 150.0f, 30.0f);
        this.popupGroup.addActor(this.buttonTable);
        int vSpacing = 7;
        int buttonHeight = 70;
        float fontScale = 0.5f;
        this.headingLabel = new Label((CharSequence)Localization.get("paused"), this.headingStyle);
        this.headingLabel.setAlignment(1);
        this.headingLabel.setFontScale(0.9f);
        this.headingLabel.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.headingLabel.setTouchable(Touchable.disabled);
        this.buttonTable.add(this.headingLabel).colspan(1).fill().width(450.0f).spaceBottom(10.0f);
        this.buttonTable.row();
        this.btnResume = new TextButton(Localization.get("resume").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnResume.getLabel().setFontScale(fontScale);
        this.buttonTable.add(this.btnResume).align(1).height(buttonHeight).space(vSpacing).colspan(1).fill();
        this.btnResume.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                PausePopup.this.back();
            }
        });
        this.buttonTable.row();
        this.btnControls = new TextButton(Localization.get("options").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnControls.getLabel().setFontScale(fontScale);
        this.buttonTable.add(this.btnControls).height(buttonHeight).space(vSpacing).fill().uniform();
        this.btnControls.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                PausePopup.this.baseScreen.menuForwardSound();
                PausePopup.this.baseScreen.showMenu(new SettingsPopup(PausePopup.this.baseScreen));
            }
        });
        this.buttonTable.row();
        this.btnSave = new TextButton(Localization.get("save").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnSave.getLabel().setFontScale(fontScale);
        this.buttonTable.add(this.btnSave).align(1).height(buttonHeight).space(vSpacing).colspan(1).fill();
        this.btnSave.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                PausePopup.this.baseScreen.menuForwardSound();
                PausePopup.this.onButtonSave();
            }
        });
        this.buttonTable.row();
        this.btnExit = new TextButton(Localization.get("quit").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnExit.getLabel().setFontScale(fontScale);
        this.buttonTable.add(this.btnExit).align(1).height(buttonHeight).space(vSpacing).colspan(1).fill();
        this.btnExit.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                PausePopup.this.baseScreen.menuForwardSound();
                PausePopup.this.onButtonExit();
            }
        });
        Label saveLabel = new Label((CharSequence)("(" + MoonBase.getCurrentMission().playerName + ")"), this.labelStyle);
        saveLabel.setFontScale(0.4f);
        saveLabel.setWidth(this.panelBg.getWidth());
        saveLabel.setHeight(70.0f);
        saveLabel.setAlignment(1);
        saveLabel.setColor(Menu.MENU_COLOR);
        saveLabel.getColor().a = 0.8f;
        saveLabel.setPosition(0.0f, 20.0f, 12);
        this.popupGroup.addActor(saveLabel);
        saveLabel.setTouchable(Touchable.disabled);
    }

    private void onButtonSave() {
        this.gameScreen.gameLoader.saveGame(this.gameScreen.world, false);
        ScreenshotGrabber.takeScreenshot(this.gameScreen.stage, this.gameScreen);
        this.saved = true;
        this.btnSave.setText(Localization.get("saved").toUpperCase());
        this.gameScreen.hud.hudNotifications.newMessage("hud/save-icon", Localization.get("autoSaveNotification"));
    }

    private void onButtonExit() {
        if (this.saved) {
            this.gameScreen.dispose();
            this.gameScreen.game.loadFrontendAssets();
            this.gameScreen.game.setScreen(new FrontendScreen(this.gameScreen.game));
        } else {
            this.baseScreen.showMenu(new QuitWithoutSavePopup(this.gameScreen));
        }
    }

    @Override
    protected void setupTintBg() {
        super.setupTintBg();
        this.addWaves();
        this.stars = new ParticleActor(Gdx.files.internal("particles/pause-stars2.p"), this.baseScreen.menuAtlas, false);
        this.stars.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2);
        this.menuGroup.addActor(this.stars);
        this.stars.setScale(MoonBase.GET_SCREENSIZE_MULTIPLIER());
        this.constellations = new Group();
        this.constellations.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 75);
        this.menuGroup.addActor(this.constellations);
        this.constellations.setScale(MoonBase.GET_SCREENSIZE_MULTIPLIER());
        this.addConstellations();
    }

    private void addWaves() {
        Group waves = new Group();
        waves.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 - 50);
        this.menuGroup.addActor(waves);
        waves.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.fadeIn(1.5f)));
        float c1Scale = 1.52f;
        this.cloud1 = (Texture)AssetManagerSingleton.getInstance().get("bg-cloud1.png");
        this.cloud1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.MirroredRepeat);
        TextureRegion imgTextureRegion = new TextureRegion(this.cloud1);
        imgTextureRegion.setRegion(0, 0, this.cloud1.getWidth() * 6, this.cloud1.getHeight());
        AdditiveImage i = new AdditiveImage(imgTextureRegion);
        i.setColor(new Color(0.2509804f, 0.0f, 1.0f, 0.18f));
        i.setScale(c1Scale, 1.25f);
        waves.addActor(i);
        float startX1 = -100.0f;
        int align1 = 2;
        i.addAction(Actions.forever(Actions.sequence((Action)Actions.moveToAligned(startX1, 0.0f, align1), (Action)Actions.moveToAligned(startX1 + (float)this.cloud1.getWidth() * c1Scale, 0.0f, align1, 18.315f), (Action)Actions.moveToAligned(startX1, 0.0f, align1))));
        this.cloud2 = (Texture)AssetManagerSingleton.getInstance().get("bg-cloud1.png");
        this.cloud2.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.MirroredRepeat);
        TextureRegion imgTextureRegion2 = new TextureRegion(this.cloud1);
        imgTextureRegion2.setRegion(0, 0, this.cloud1.getWidth() * 5, this.cloud1.getHeight());
        float c2Scale = 1.93f;
        float startX2 = -200.0f;
        AdditiveImage i2 = new AdditiveImage(imgTextureRegion2);
        i2.setScale(c2Scale, 1.36f);
        i2.setColor(new Color(0.2901961f, 0.18431373f, 0.9490196f, 0.2f));
        waves.addActor(i2);
        i2.addAction(Actions.forever(Actions.sequence((Action)Actions.moveToAligned(startX2, 0.0f, align1), (Action)Actions.moveToAligned(startX2 - (float)this.cloud2.getWidth() * c2Scale, 0.0f, align1, 22.0f), (Action)Actions.moveToAligned(startX2, 0.0f, align1))));
    }

    private void addConstellations() {
        this.constellations.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.rotateTo(-1.0f), (Action)Actions.parallel((Action)Actions.fadeIn(3.0f), (Action)Actions.forever(Actions.sequence((Action)Actions.rotateTo(2.0f, 20.0f, Interpolation.sine), (Action)Actions.rotateTo(-2.0f, 20.0f, Interpolation.sine))))));
        AdditiveImage const1 = new AdditiveImage(this.skin.getDrawable("const-cat1"));
        const1.setOrigin(1);
        const1.setRotation(8.0f);
        const1.setPosition(-477.0f, 80.0f, 1);
        this.constellations.addActor(const1);
        AdditiveImage const2 = new AdditiveImage(this.skin.getDrawable("const-food"));
        const2.setOrigin(1);
        const2.setRotation(-12.0f);
        const2.setScale(1.2f);
        const2.setPosition(461.0f, 70.0f, 1);
        this.constellations.addActor(const2);
        AdditiveImage const3 = new AdditiveImage(this.skin.getDrawable("const-rocket"));
        const3.setOrigin(1);
        const3.setRotation(33.0f);
        const3.setScale(0.8f);
        const3.setPosition(367.0f, -187.0f, 1);
        this.constellations.addActor(const3);
        AdditiveImage const4 = new AdditiveImage(this.skin.getDrawable("const-bell"));
        const4.setOrigin(1);
        const4.setRotation(10.0f);
        const4.setScale(0.85f);
        const4.setPosition(-356.0f, -193.0f, 1);
        this.constellations.addActor(const4);
        const1.addAction(Actions.sequence((Action)Actions.alpha(0.25f), (Action)Actions.delay(0.0f), (Action)Actions.forever(Actions.sequence((Action)Actions.parallel((Action)Actions.alpha(1.0f, 2.0f)), (Action)Actions.parallel((Action)Actions.alpha(0.25f, 2.0f))))));
        const2.addAction(Actions.sequence((Action)Actions.alpha(0.25f), (Action)Actions.delay(1.0f), (Action)Actions.forever(Actions.sequence((Action)Actions.parallel((Action)Actions.alpha(1.0f, 2.0f)), (Action)Actions.parallel((Action)Actions.alpha(0.25f, 2.0f))))));
        const3.addAction(Actions.sequence((Action)Actions.alpha(0.25f), (Action)Actions.delay(2.0f), (Action)Actions.forever(Actions.sequence((Action)Actions.parallel((Action)Actions.alpha(1.0f, 2.0f)), (Action)Actions.parallel((Action)Actions.alpha(0.25f, 2.0f))))));
        const4.addAction(Actions.sequence((Action)Actions.alpha(0.25f), (Action)Actions.delay(3.0f), (Action)Actions.forever(Actions.sequence((Action)Actions.parallel((Action)Actions.alpha(1.0f, 2.0f)), (Action)Actions.parallel((Action)Actions.alpha(0.25f, 2.0f))))));
    }

    @Override
    public void back() {
        this.gameScreen.world.pauseSimulation(false);
        this.gameScreen.hud.hudNotifications.resetGroup();
        super.back();
    }
}

