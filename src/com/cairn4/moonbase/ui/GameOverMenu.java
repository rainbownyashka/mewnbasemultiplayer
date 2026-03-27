/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.GameLoader;
import com.cairn4.moonbase.GameOverReasons;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.MultiplyImage;
import com.cairn4.moonbase.ui.FrontendScreen;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.LoadGameMenu;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.worlddata.GameSaveData;

public class GameOverMenu
extends Menu {
    GameScreen gameScreen;
    private GameOverReasons.REASONS gameOverReason;
    private Bloom blur;
    private static float preBlurTimer = 0.0f;
    private static float preBlurLength = 1.0f;
    private static float blurLength = 4.5f;
    private float blurTimer = 0.0f;
    private float blurValue;
    private Image colorTint;
    private Image overlayTint;
    private Image clouds;
    private Image head;
    private Label gameOverLabel;
    private Label gameOverReasonLabel;
    private Image fadeOut;
    TextButton btnContinue;
    TextButton btnExit;

    public GameOverMenu(GameScreen gameScreen, GameOverReasons.REASONS gameOverReason) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        this.gameOverReason = gameOverReason;
        this.blinkOutHud();
        this.addFx();
        this.addUI();
    }

    private void addUI() {
        this.overlayTint = new MultiplyImage(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
        this.overlayTint.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        this.overlayTint.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.overlayTint.addAction(Actions.sequence((Action)Actions.color(new Color(0.7f, 0.2f, 0.2f, 1.0f), 3.0f)));
        this.menuGroup.addActor(this.overlayTint);
        String message = Localization.get("gameOver");
        String reason = Localization.get("gameOver_" + this.gameOverReason.toString());
        Group gameOverGroup = new Group();
        gameOverGroup.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 35);
        gameOverGroup.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.delay(2.5f), (Action)Actions.alpha(1.0f, 1.5f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                GameOverMenu.this.btnContinue.setTouchable(Touchable.enabled);
                GameOverMenu.this.btnExit.setTouchable(Touchable.enabled);
            }
        })));
        this.menuGroup.addActor(gameOverGroup);
        this.clouds = new Image(this.gameScreen.skin.getDrawable("gameover-clouds"));
        this.clouds.setPosition(0.0f, 145.0f, 1);
        this.clouds.setColor(Color.valueOf("745144"));
        this.clouds.getColor().a = 0.5f;
        gameOverGroup.addActor(this.clouds);
        this.head = new Image(this.gameScreen.skin.getDrawable("gameover-head"));
        this.head.setSize(131.0f, 111.0f);
        this.head.setPosition(0.0f, 145.0f, 1);
        gameOverGroup.addActor(this.head);
        this.head.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.delay(3.5f), (Action)Actions.fadeIn(1.0f)));
        this.gameOverLabel = new Label((CharSequence)message, this.gameScreen.headingStyle);
        this.gameOverLabel.setWidth(MoonBase.SCREEN_WIDTH);
        this.gameOverLabel.setAlignment(1);
        this.gameOverLabel.setFontScale(1.0f);
        this.gameOverLabel.setPosition(0.0f, 20.0f, 1);
        gameOverGroup.addActor(this.gameOverLabel);
        this.gameOverReasonLabel = new Label((CharSequence)reason, this.gameScreen.labelStyle);
        this.gameOverReasonLabel.setFontScale(0.7f);
        this.gameOverReasonLabel.setWidth(MoonBase.SCREEN_WIDTH);
        this.gameOverReasonLabel.setAlignment(1);
        this.gameOverReasonLabel.setPosition(0.0f, -40.0f, 1);
        gameOverGroup.addActor(this.gameOverReasonLabel);
        Table buttonTable = new Table();
        buttonTable.setPosition(0.0f, -180.0f, 2);
        gameOverGroup.addActor(buttonTable);
        buttonTable.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.delay(3.5f), (Action)Actions.fadeIn(1.0f)));
        this.btnContinue = new TextButton(Localization.get("continueGame").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnContinue.getLabel().setFontScale(0.6f);
        buttonTable.add(this.btnContinue).align(1).width(380.0f).height(80.0f).space(10.0f).colspan(1).fill();
        this.btnContinue.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameOverMenu.this.nextMenu(true);
            }
        });
        buttonTable.row();
        this.btnExit = new TextButton(Localization.get("quit").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnExit.getLabel().setFontScale(0.6f);
        buttonTable.add(this.btnExit).align(1).width(380.0f).height(80.0f).space(10.0f).colspan(1).fill();
        this.btnExit.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameOverMenu.this.nextMenu(false);
            }
        });
        this.fadeOut = new Image(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
        this.fadeOut.setSize(MoonBase.SCREEN_WIDTH + 10, MoonBase.SCREEN_HEIGHT + 10);
        this.fadeOut.setPosition(-5.0f, -5.0f);
        this.fadeOut.setTouchable(Touchable.disabled);
        this.fadeOut.setColor(0.0f, 0.0f, 0.0f, 0.0f);
        this.gameScreen.hudStage.addActor(this.fadeOut);
        this.btnContinue.setTouchable(Touchable.disabled);
        this.btnExit.setTouchable(Touchable.disabled);
    }

    private void nextMenu(boolean contGame) {
        this.gameScreen.menuForwardSound();
        final boolean continueGame = contGame;
        AssetManagerSingleton.getInstance().load("loading.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("loading-bg.png", Texture.class);
        this.fadeOut.addAction(Actions.sequence((Action)Actions.fadeIn(1.0f), (Action)Actions.delay(0.25f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                if (continueGame) {
                    GameOverMenu.this.gameScreen.dispose();
                    AssetManagerSingleton.getInstance().finishLoading();
                    GameOverMenu.this.gameScreen.game.loadFrontendAssets();
                    GameSaveData gsd = GameLoader.getGameSaveData(MoonBase.currentSaveFolder);
                    FrontendScreen frontendScreen = new FrontendScreen(GameOverMenu.this.gameScreen.game);
                    GameOverMenu.this.gameScreen.game.setScreen(frontendScreen);
                    frontendScreen.showMenu(new LoadGameMenu(frontendScreen, gsd));
                } else {
                    GameOverMenu.this.gameScreen.dispose();
                    AssetManagerSingleton.getInstance().finishLoading();
                    GameOverMenu.this.gameScreen.game.loadFrontendAssets();
                    GameOverMenu.this.gameScreen.game.setScreen(new FrontendScreen(GameOverMenu.this.gameScreen.game));
                }
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

    private void blinkOutHud() {
        this.gameScreen.hud.hudGroup.addAction(Actions.sequence(Actions.alpha(0.0f), Actions.delay(0.05f), Actions.alpha(0.7f), Actions.delay(0.1f), Actions.alpha(0.0f), Actions.delay(0.15f), Actions.alpha(0.5f), Actions.delay(0.15f), Actions.alpha(0.0f), Actions.delay(0.15f), Actions.alpha(0.25f), Actions.delay(0.05f), Actions.alpha(0.0f)));
        this.gameScreen.hud.hudInventory.hide();
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
    }
}

