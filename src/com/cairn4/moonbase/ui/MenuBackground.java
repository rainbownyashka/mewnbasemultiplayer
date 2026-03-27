/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.ShaderHelper;
import com.cairn4.moonbase.ui.BaseScreen;

public class MenuBackground {
    private float scanlineTime = 1.0f;
    private BaseScreen baseScreen;
    private Skin skin;
    private Stage bgStage;
    private SpriteBatch shaderBatch;
    private Stage shaderStage;
    private Group constellations;
    private ParticleActor stars;

    public MenuBackground(BaseScreen baseScreen) {
        this.baseScreen = baseScreen;
        this.skin = baseScreen.skin;
        this.bgStage = new Stage(baseScreen.viewport);
        this.shaderBatch = new SpriteBatch();
        this.shaderStage = new Stage(baseScreen.viewport, this.shaderBatch);
        ShaderHelper.setShader(ShaderHelper.scanlineShader, this.shaderBatch);
        Texture bgTex = AssetManagerSingleton.getInstance().get("menubg/space-bg.png", Texture.class);
        bgTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Image background = new Image(bgTex);
        background.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_WIDTH);
        background.setPosition(0.0f, -80.0f);
        this.bgStage.addActor(background);
        background.setColor(0.0f, 0.0f, 0.0f, 1.0f);
        background.addAction(Actions.parallel((Action)Actions.color(new Color(1.0f, 1.0f, 1.0f, 1.0f), 1.5f), (Action)Actions.moveTo(0.0f, -45.0f, 2.0f, Interpolation.sineOut)));
        AdditiveImage sunGlow = new AdditiveImage(AssetManagerSingleton.getInstance().get("light.png", Texture.class));
        sunGlow.setColor(Color.valueOf("e36402bb"));
        sunGlow.setSize(800.0f, 500.0f);
        sunGlow.setOrigin(1);
        sunGlow.setPosition(MoonBase.SCREEN_WIDTH / 2, 250.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER(), 1);
        sunGlow.setScale(MoonBase.GET_SCREENSIZE_MULTIPLIER());
        this.bgStage.addActor(sunGlow);
        Group planetGroup = new Group();
        planetGroup.setPosition(MoonBase.SCREEN_WIDTH / 2, 300.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER());
        planetGroup.setScale(MoonBase.GET_SCREENSIZE_MULTIPLIER());
        this.bgStage.addActor(planetGroup);
        Texture planetTex = AssetManagerSingleton.getInstance().get("menubg/planet.png", Texture.class);
        planetTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        Image planet = new Image(planetTex);
        planet.setPosition(0.0f, 0.0f, 2);
        planetGroup.addActor(planet);
        Texture planetAtmoTex = AssetManagerSingleton.getInstance().get("menubg/planet-atmosphere.png", Texture.class);
        planetAtmoTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        AdditiveImage planetAtmo = new AdditiveImage(planetAtmoTex);
        planetAtmo.setPosition(0.0f, 39.0f, 2);
        planetGroup.addActor(planetAtmo);
        AdditiveImage sunGlow2 = new AdditiveImage(AssetManagerSingleton.getInstance().get("light.png", Texture.class));
        sunGlow2.setColor(Color.valueOf("e3ab0266"));
        sunGlow2.setSize(300.0f, 150.0f);
        sunGlow2.setOrigin(1);
        sunGlow2.setPosition(MoonBase.SCREEN_WIDTH / 2, 260.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER(), 1);
        sunGlow2.setScale(MoonBase.GET_SCREENSIZE_MULTIPLIER());
        this.bgStage.addActor(sunGlow2);
        sunGlow.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.parallel((Action)Actions.alpha(0.73f, 1.5f))));
        sunGlow2.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.parallel((Action)Actions.alpha(0.4f, 1.5f))));
        planetGroup.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.moveBy(0.0f, -15.0f), (Action)Actions.parallel((Action)Actions.moveBy(0.0f, 15.0f, 2.0f, Interpolation.sineOut), (Action)Actions.alpha(1.0f, 1.0f))));
        Image crt = new Image(this.skin.getDrawable("mainmenu-scanlines"));
        crt.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        crt.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2, 1);
        crt.setColor(1.0f, 1.0f, 1.0f, 0.5f);
        this.shaderStage.addActor(crt);
        this.stars = new ParticleActor(Gdx.files.internal("particles/mainmenu-stars.p"), baseScreen.menuAtlas, false);
        this.stars.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 65);
        this.bgStage.addActor(this.stars);
        this.stars.addAction(Actions.sequence((Action)Actions.moveTo(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 100, 2.0f, Interpolation.sineOut)));
        this.constellations = new Group();
        this.constellations.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 75);
        this.bgStage.addActor(this.constellations);
        this.constellations.setScale(MoonBase.GET_SCREENSIZE_MULTIPLIER());
        this.addConstellations();
    }

    private void addConstellations() {
        this.constellations.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.rotateTo(-1.0f), (Action)Actions.parallel((Action)Actions.fadeIn(2.0f), (Action)Actions.forever(Actions.sequence((Action)Actions.rotateTo(2.0f, 20.0f, Interpolation.sine), (Action)Actions.rotateTo(-2.0f, 20.0f, Interpolation.sine))))));
        AdditiveImage const1 = new AdditiveImage(this.skin.getDrawable("const-cat1"));
        const1.setOrigin(1);
        const1.setRotation(8.0f);
        const1.setPosition(-477.0f, 80.0f, 1);
        this.constellations.addActor(const1);
        AdditiveImage const2 = new AdditiveImage(this.skin.getDrawable("const-food"));
        const2.setOrigin(1);
        const2.setRotation(-12.0f);
        const2.setScale(0.8f);
        const2.setPosition(511.0f, 130.0f, 1);
        this.constellations.addActor(const2);
        AdditiveImage const3 = new AdditiveImage(this.skin.getDrawable("const-rocket"));
        const3.setOrigin(1);
        const3.setRotation(33.0f);
        const3.setScale(0.8f);
        const3.setPosition(497.0f, -137.0f, 1);
        this.constellations.addActor(const3);
        AdditiveImage const4 = new AdditiveImage(this.skin.getDrawable("const-bell"));
        const4.setOrigin(1);
        const4.setRotation(10.0f);
        const4.setScale(0.7f);
        const4.setPosition(-566.0f, -153.0f, 1);
        this.constellations.addActor(const4);
        const1.addAction(Actions.sequence((Action)Actions.alpha(0.25f), (Action)Actions.delay(0.0f), (Action)Actions.forever(Actions.sequence((Action)Actions.parallel((Action)Actions.alpha(1.0f, 2.0f)), (Action)Actions.parallel((Action)Actions.alpha(0.25f, 2.0f))))));
        const2.addAction(Actions.sequence((Action)Actions.alpha(0.25f), (Action)Actions.delay(1.0f), (Action)Actions.forever(Actions.sequence((Action)Actions.parallel((Action)Actions.alpha(1.0f, 2.0f)), (Action)Actions.parallel((Action)Actions.alpha(0.25f, 2.0f))))));
        const3.addAction(Actions.sequence((Action)Actions.alpha(0.25f), (Action)Actions.delay(2.0f), (Action)Actions.forever(Actions.sequence((Action)Actions.parallel((Action)Actions.alpha(1.0f, 2.0f)), (Action)Actions.parallel((Action)Actions.alpha(0.25f, 2.0f))))));
        const4.addAction(Actions.sequence((Action)Actions.alpha(0.25f), (Action)Actions.delay(3.0f), (Action)Actions.forever(Actions.sequence((Action)Actions.parallel((Action)Actions.alpha(1.0f, 2.0f)), (Action)Actions.parallel((Action)Actions.alpha(0.25f, 2.0f))))));
    }

    public void render(float delta) {
        this.scanlineTime += delta;
        if (this.scanlineTime >= 1.0f) {
            this.scanlineTime = 0.0f;
        }
        this.baseScreen.spriteBatch.begin();
        this.bgStage.act();
        this.bgStage.draw();
        this.baseScreen.spriteBatch.end();
        this.shaderStage.act();
        ShaderHelper.scanlineShader.bind();
        this.shaderBatch.getShader().setUniformf("u_gameTime", this.scanlineTime);
        this.shaderStage.draw();
    }

    public void dispose() {
        ShaderHelper.dispose();
        this.bgStage.dispose();
        this.stars.pfx.dispose();
        this.shaderStage.dispose();
    }
}

