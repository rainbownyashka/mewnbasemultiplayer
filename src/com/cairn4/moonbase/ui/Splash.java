/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.FrontendScreen;

public class Splash
extends BaseScreen {
    private Image background;
    private Image logo;
    private Image subtitle;
    private Label debugLabel;
    private Image cover;
    private Image fader;
    private Image bigCover;
    private boolean readyToExit = false;
    private boolean exiting = false;

    public Splash(MoonBase game) {
        super(game);
        AssetManagerSingleton.getInstance().load("splash.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().finishLoading();
        TextureAtlas textureAtlas = (TextureAtlas)AssetManagerSingleton.getInstance().get("splash.atlas");
        this.skin.addRegions(textureAtlas);
        Audio.getInstance().startMusic("music/enchanted_tiki_86_0.mp3", 0.5f, true);
        this.setInputProcessor();
    }

    @Override
    public void show() {
        this.background = new Image(this.skin.getDrawable("splash-bg"));
        this.background.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        this.stage.addActor(this.background);
        this.logo = new Image(this.skin.getDrawable("cairn4"));
        this.logo.setOrigin(1);
        this.logo.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 15, 1);
        this.stage.addActor(this.logo);
        this.cover = new Image(this.skin.getDrawable("splash-coverup"));
        this.cover.setPosition(this.logo.getX() - 150.0f, this.logo.getY() - 50.0f);
        this.stage.addActor(this.cover);
        this.bigCover = new Image(this.skin.getDrawable("splash-coverup"));
        this.bigCover.setSize(MoonBase.SCREEN_WIDTH * 2, MoonBase.SCREEN_HEIGHT);
        this.bigCover.setOrigin(8);
        this.bigCover.setScale(-1.0f, -1.0f);
        this.bigCover.setColor(Color.BLACK);
        this.stage.addActor(this.bigCover);
        this.fader = new Image(this.skin.getDrawable("splash-bg"));
        this.fader.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        this.fader.setColor(Color.BLACK);
        this.stage.addActor(this.fader);
        String debugText = MoonBase.coreFolder;
        this.debugLabel = new Label((CharSequence)("" + debugText), this.labelStyle);
        this.debugLabel.setFontScale(0.3f);
        this.debugLabel.setAlignment(1);
        this.debugLabel.setPosition(640.0f, 80.0f, 1);
        this.debugLabel.setVisible(false);
        this.animate();
    }

    private void animate() {
        this.fader.addAction(Actions.sequence((Action)Actions.fadeOut(0.5f), (Action)Actions.delay(2.0f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                Splash.this.readyToExit = true;
            }
        })));
        this.cover.addAction(Actions.sequence((Action)Actions.delay(0.2f), (Action)Actions.moveBy(this.cover.getWidth(), 0.0f, 0.5f, Interpolation.sineIn)));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.readyToExit && !this.exiting) {
            this.animateOff();
            this.exiting = true;
        } else if (!this.readyToExit && !this.exiting && Gdx.input.isTouched()) {
            this.readyToExit = true;
        }
    }

    private void animateOff() {
        this.bigCover.addAction(Actions.sequence((Action)Actions.delay(0.35f), (Action)Actions.moveBy(2000.0f, 0.0f, 0.5f)));
        this.fader.clearActions();
        this.fader.addAction(Actions.sequence((Action)Actions.fadeIn(0.85f), (Action)Actions.delay(0.15f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                Splash.this.dispose();
                Splash.this.game.setScreen(new FrontendScreen(Splash.this.game));
            }
        })));
    }

    @Override
    public void dispose() {
        Gdx.app.debug("MewnBase", "Unloading splash");
        AssetManagerSingleton.getInstance().unload("splash.atlas");
        super.dispose();
    }
}

