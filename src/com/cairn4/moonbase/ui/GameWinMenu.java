/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
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
import com.bitfire.postprocessing.effects.Bloom;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.GameOverReasons;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.MultiplyImage;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.ui.FrontendScreen;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;

public class GameWinMenu
extends Menu {
    GameScreen gameScreen;
    private GameOverReasons.REASONS gameOverReason;
    private Bloom blur;
    private static float preBlurTimer = 0.0f;
    private static float preBlurLength = 1.0f;
    private static float blurLength = 2.5f;
    private float blurTimer = 0.0f;
    private float blurValue;
    private Image colorTint;
    private Image overlayTint;
    private Group topGroup;
    private Image clouds;
    private Label headingLabel;
    private Label gameOverReasonLabel;
    private Image fadeOut;
    private Table buttonTable;
    TextButton btnContinue;
    private Group statsGroup;
    private Label statsLabel;
    private Group gameOverGroup;
    private Image statsPanel;
    private ParticleActor stars;

    public GameWinMenu(GameScreen gameScreen) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        this.gameOverReason = this.gameOverReason;
        AssetManagerSingleton.getInstance().load("gamewin.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().finishLoading();
        this.gameScreen.skin.addRegions((TextureAtlas)AssetManagerSingleton.getInstance().get("gamewin.atlas"));
        gameScreen.hud.hideLetterbox();
        this.setup();
        this.show();
    }

    @Override
    public void setup() {
        this.overlayTint = new MultiplyImage(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
        this.overlayTint.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        this.overlayTint.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.menuGroup.addActor(this.overlayTint);
        this.stars = new ParticleActor(Gdx.files.internal("particles/gamewin-stars.p"), this.baseScreen.menuAtlas, false);
        this.stars.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2);
        this.menuGroup.addActor(this.stars);
        String message = Localization.get("gameWin");
        String reason = Localization.get("gameOver_success");
        this.gameOverGroup = new Group();
        this.gameOverGroup.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 35);
        this.menuGroup.addActor(this.gameOverGroup);
        this.topGroup = new Group();
        this.topGroup.setPosition(0.0f, -40.0f);
        this.gameOverGroup.addActor(this.topGroup);
        AdditiveImage glow = new AdditiveImage(this.gameScreen.skin.getDrawable("small-gradient-circle"));
        glow.setColor(0.0f, 0.05f, 1.0f, 0.45f);
        glow.setSize(620.0f, 482.0f);
        glow.setOrigin(1);
        glow.setPosition(0.0f, 125.0f, 1);
        this.topGroup.addActor(glow);
        this.clouds = new AdditiveImage(this.gameScreen.skin.getDrawable("const-cat1"));
        this.clouds.setSize(300.0f, 272.0f);
        this.clouds.setPosition(0.0f, 185.0f, 1);
        this.topGroup.addActor(this.clouds);
        this.headingLabel = new Label((CharSequence)message, this.gameScreen.headingStyle);
        this.headingLabel.setWidth(MoonBase.SCREEN_WIDTH);
        this.headingLabel.setAlignment(1);
        this.headingLabel.setFontScale(1.0f);
        this.headingLabel.setPosition(0.0f, 70.0f, 1);
        this.topGroup.addActor(this.headingLabel);
        this.gameOverReasonLabel = new Label((CharSequence)reason, this.gameScreen.labelStyle);
        this.gameOverReasonLabel.setFontScale(0.7f);
        this.gameOverReasonLabel.setWidth(MoonBase.SCREEN_WIDTH);
        this.gameOverReasonLabel.setAlignment(1);
        this.gameOverReasonLabel.setPosition(0.0f, 10.0f, 1);
        this.topGroup.addActor(this.gameOverReasonLabel);
        this.statsGroup = new Group();
        this.gameOverGroup.addActor(this.statsGroup);
        this.statsGroup.setPosition(0.0f, -145.0f);
        NinePatch statsPanel9P = new NinePatch(this.baseScreen.skin.getRegion("btn-item-active"), 30, 30, 30, 30);
        this.statsPanel = new Image(statsPanel9P);
        this.statsPanel.setSize(600.0f, 150.0f);
        this.statsPanel.setOrigin(1);
        this.statsPanel.setPosition(0.0f, 0.0f, 1);
        this.statsPanel.setColor(1.0f, 1.0f, 1.0f, 0.3f);
        this.statsGroup.addActor(this.statsPanel);
        String statsText = "";
        statsText = statsText + MoonBase.getCurrentMission().getMissionTypeName();
        if (MoonBase.getCurrentMission().missionType == Mission.MissionTypes.creative) {
            statsText = statsText + " [" + MoonBase.getCurrentMission().getMissionTypeName() + "] ";
        } else if (MoonBase.getCurrentMission().techTreeMode == Mission.techTreeModes.allTechUnlocked) {
            statsText = statsText + " [" + Localization.get("worldGen_techTree") + ": " + Localization.get("worldGen_allTechUnlocked") + "] ";
        }
        if (MoonBase.getCurrentMission().dayCycleMode != Mission.dayCycleModes.defaultDay) {
            statsText = statsText + " [" + Localization.get("worldGen_" + MoonBase.getCurrentMission().dayCycleMode.toString()) + "] ";
        }
        statsText = statsText + "\n";
        statsText = statsText + Localization.format("gameStats_daysSurvived", this.gameScreen.world.dayCycle.getDay()) + "\n";
        statsText = statsText + Localization.format("gameStats_artifactsResearched", this.gameScreen.world.player.researchObjectsDiscovered.size());
        this.statsLabel = new Label((CharSequence)statsText, this.labelStyle);
        this.statsGroup.addActor(this.statsLabel);
        this.statsLabel.setFontScale(0.4f);
        this.statsLabel.setColor(Menu.MENU_COLOR);
        this.statsLabel.setAlignment(1, 1);
        this.statsLabel.setPosition(0.0f, 0.0f, 1);
        this.buttonTable = new Table();
        this.buttonTable.setPosition(0.0f, -300.0f, 2);
        this.gameOverGroup.addActor(this.buttonTable);
        this.btnContinue = new TextButton(Localization.get("continueGame").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnContinue.getLabel().setFontScale(0.6f);
        this.buttonTable.add(this.btnContinue).align(1).width(380.0f).height(80.0f).space(10.0f).colspan(1).fill();
        this.btnContinue.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameWinMenu.this.nextMenu(true);
            }
        });
        this.buttonTable.row();
        this.fadeOut = new Image(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
        this.fadeOut.setSize(MoonBase.SCREEN_WIDTH + 10, MoonBase.SCREEN_HEIGHT + 10);
        this.fadeOut.setPosition(-5.0f, -5.0f);
        this.fadeOut.setTouchable(Touchable.disabled);
        this.fadeOut.setColor(0.0f, 0.0f, 0.0f, 0.0f);
        this.gameScreen.hudStage.addActor(this.fadeOut);
    }

    @Override
    public void show() {
        this.addFx();
        float delayToHeading = 1.5f;
        float delayToStats = 3.0f;
        this.overlayTint.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.color(new Color(0.2f, 0.2f, 0.3f, 0.5f), 1.0f)));
        this.gameOverGroup.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.alpha(1.0f, 1.0f)));
        this.topGroup.addAction(Actions.sequence(Actions.alpha(0.0f), Actions.moveTo(0.0f, -150.0f), Actions.delay(0.7f), Actions.scaleTo(0.5f, 0.5f), Actions.parallel((Action)Actions.alpha(1.0f, 0.3f), (Action)Actions.scaleTo(1.05f, 1.05f, 0.3f, Interpolation.sineOut)), Actions.scaleTo(1.0f, 1.0f, 0.05f, Interpolation.linear), Actions.delay(2.0f), Actions.moveTo(0.0f, -20.0f, 0.7f, Interpolation.sine)));
        this.statsGroup.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.delay(3.5f), (Action)Actions.fadeIn(0.8f)));
        this.statsPanel.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(1.0f, 0.25f), (Action)Actions.delay(3.0f), (Action)Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, 0.4f, Interpolation.sine), (Action)Actions.alpha(1.0f, 0.4f))));
        this.buttonTable.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.delay(3.5f), (Action)Actions.fadeIn(0.8f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                GameWinMenu.this.gameScreen.hud.stage.getRoot().setTouchable(Touchable.enabled);
            }
        })));
    }

    private void hideHud() {
        this.gameScreen.hud.hudGroup.addAction(Actions.sequence((Action)Actions.alpha(1.0f), (Action)Actions.delay(1.0f), (Action)Actions.alpha(0.0f, 2.0f)));
        this.gameScreen.hud.hudInventory.hide();
    }

    private void nextMenu(boolean contGame) {
        this.gameScreen.menuForwardSound();
        boolean continueGame = contGame;
        AssetManagerSingleton.getInstance().load("loading.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("loading-bg.png", Texture.class);
        this.fadeOut.addAction(Actions.sequence((Action)Actions.fadeIn(1.0f), (Action)Actions.delay(0.25f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                GameWinMenu.this.back();
                GameWinMenu.this.gameScreen.game.loadFrontendAssets();
                GameWinMenu.this.gameScreen.game.setScreen(new FrontendScreen(GameWinMenu.this.gameScreen.game));
                GameWinMenu.this.gameScreen.dispose();
            }
        })));
        if (contGame) {
            // empty if block
        }
    }

    private void addFx() {
        this.blur = new Bloom(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        this.blur.setSettings(new Bloom.Settings("healthblur", 2, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f));
        this.blur.setBlurAmount(0.0f);
        this.gameScreen.postProcessor.addEffect(this.blur);
    }

    private void animateFx(float delta) {
        this.blurTimer += delta;
        if (this.blurTimer >= blurLength) {
            this.blurTimer = blurLength;
        }
        this.blurValue = this.blurTimer / blurLength;
        this.blur.setBaseIntesity(1.0f - this.blurValue);
        this.blur.setBaseSaturation(1.0f - this.blurValue);
        this.blur.setBloomIntesity(this.blurValue);
        this.blur.setBloomSaturation(1.0f - this.blurValue);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.blurTimer < blurLength && (preBlurTimer += delta) >= preBlurLength && this.blurTimer < blurLength) {
            this.animateFx(delta);
        }
    }

    @Override
    public void back() {
        super.back();
        this.blur.setBaseSaturation(1.0f);
        this.blur.setBaseIntesity(1.0f);
        this.gameScreen.postProcessor.removeEffect(this.blur);
        this.menuGroup.remove();
    }

    @Override
    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(46)) {
            this.back();
            this.gameScreen.world.gameWin();
        }
    }
}

