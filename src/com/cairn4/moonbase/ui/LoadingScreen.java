/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ProjectileFactory;
import com.cairn4.moonbase.entities.CreatureLoader;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;

public class LoadingScreen
extends BaseScreen {
    MoonBase game;
    boolean newGame;
    Label loadingLabel;
    boolean ready = false;
    Label planetName;
    Group planetGroup;
    Image planet;
    Image clouds;
    Image path;
    Image dataLeft;
    Group dataRightGroup;
    Image dataRight;
    Image barGraph;
    Image dataRightText;
    private float barTimer;
    private final float barDelay = 0.5f;
    private int barIndex;
    private String[] barFrames = new String[]{"bars1", "bars2", "bars3", "bars2"};
    private int statIndex;
    private String[] statFrames = new String[]{"stats1", "stats2"};

    public LoadingScreen(MoonBase game, boolean newGame) {
        super(game);
        this.game = game;
        this.newGame = newGame;
        this.skin.addRegions(AssetManagerSingleton.getInstance().get("loading.atlas", TextureAtlas.class));
        this.setup();
        CreatureLoader.getInstance();
        ProjectileFactory.getInstance();
        game.loadGameAssets();
    }

    private void setup() {
        float scale = 0.6666667f;
        Group bgGroup = new Group();
        this.stage.addActor(bgGroup);
        Image bg = new Image(AssetManagerSingleton.getInstance().get("loading-bg.png", Texture.class));
        bg.setSize(MoonBase.SCREEN_WIDTH, (float)MoonBase.SCREEN_HEIGHT * 1.15f);
        bg.setPosition(0.0f, -30.0f);
        bgGroup.addActor(bg);
        this.planetGroup = new Group();
        this.planetGroup.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 10);
        bgGroup.addActor(this.planetGroup);
        this.planet = new Image(this.skin.getDrawable("loading-planet"));
        this.planet.setOrigin(1);
        this.planet.setScale(2.0f * scale);
        this.planet.setPosition(0.0f, 0.0f, 1);
        this.planetGroup.addActor(this.planet);
        this.clouds = new Image(this.skin.getDrawable("loading-clouds"));
        this.clouds.setOrigin(1);
        this.clouds.setPosition(0.0f, 0.0f, 1);
        this.clouds.setColor(1.0f, 1.0f, 1.0f, 0.4f);
        this.clouds.setScale(2.0f * scale);
        this.clouds.addAction(Actions.forever(Actions.sequence((Action)Actions.rotateBy(360.0f, 100.0f))));
        this.planetGroup.addActor(this.clouds);
        this.dataLeft = new Image(this.skin.getDrawable("planet-stats-left"));
        this.dataLeft.setOrigin(18);
        this.dataLeft.setScale(scale);
        this.dataLeft.setPosition(-160.0f, -45.0f, 18);
        this.planetGroup.addActor(this.dataLeft);
        this.dataRightGroup = new Group();
        this.dataRightGroup.setScale(scale);
        this.dataRightGroup.setPosition(157.0f, -148.0f);
        this.planetGroup.addActor(this.dataRightGroup);
        this.dataRight = new Image(this.skin.getDrawable("planet-stats-right"));
        this.dataRight.setOrigin(10);
        this.dataRight.setPosition(0.0f, 0.0f, 10);
        this.dataRightGroup.addActor(this.dataRight);
        this.barGraph = new Image(this.skin.getDrawable("bars1"));
        this.barGraph.setPosition(130.0f, -100.0f, 12);
        this.dataRightGroup.addActor(this.barGraph);
        this.dataRightText = new Image(this.skin.getDrawable("stats1"));
        this.dataRightText.setPosition(130.0f, -121.0f, 10);
        this.dataRightGroup.addActor(this.dataRightText);
        this.path = new Image(this.skin.getDrawable("loading-path"));
        bg.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.fadeIn(1.0f)));
        this.loadingLabel = new Label((CharSequence)Localization.format("loadingSimple", 0), this.labelStyle);
        this.loadingLabel.setWrap(true);
        this.loadingLabel.setWidth(660.0f);
        this.loadingLabel.setColor(Menu.MENU_COLOR);
        this.loadingLabel.setAlignment(1);
        this.loadingLabel.setFontScale(0.5f);
        this.loadingLabel.setPosition(MoonBase.SCREEN_WIDTH / 2, 50.0f, 1);
        bgGroup.addActor(this.loadingLabel);
        this.path = new Image(this.skin.getDrawable("loading-path"));
        this.path.setOrigin(650.0f, 160.0f);
        this.path.setScale(2.1f * scale);
        this.path.setRotation(19.0f);
        this.path.setPosition(-200.0f, -30.0f, 1);
        this.planetGroup.addActor(this.path);
        this.barTimer = 0.0f;
        this.barIndex = 1;
        this.planetGroup.addAction(Actions.sequence((Action)Actions.scaleTo(0.9f, 0.9f), (Action)Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, 0.25f, Interpolation.sineOut))));
        this.dataLeft.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.9f * scale, 0.9f * scale), (Action)Actions.delay(0.25f), (Action)Actions.parallel((Action)Actions.fadeIn(0.25f), (Action)Actions.scaleTo(scale, scale, 0.25f))));
        this.dataRightGroup.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.9f * scale, 0.9f * scale), (Action)Actions.delay(0.25f), (Action)Actions.parallel((Action)Actions.fadeIn(0.25f), (Action)Actions.scaleTo(scale, scale, 0.25f))));
        this.path.addAction(Actions.sequence(Actions.alpha(0.0f), Actions.delay(0.5f), Actions.alpha(0.5f), Actions.delay(0.05f), Actions.alpha(0.0f, 0.025f), Actions.delay(0.05f), Actions.alpha(0.5f), Actions.delay(0.05f), Actions.alpha(0.0f, 0.025f), Actions.delay(0.05f), Actions.alpha(1.0f)));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.cycleBarGraph(delta);
    }

    private void cycleBarGraph(float delta) {
        this.barTimer += delta;
        if (this.barTimer > 0.5f) {
            this.barTimer = 0.0f;
            this.barGraph.setDrawable(this.skin.getDrawable(this.barFrames[this.barIndex]));
            ++this.barIndex;
            if (this.barIndex >= this.barFrames.length - 1) {
                this.barIndex = 0;
            }
        }
    }

    @Override
    public void render(float delta) {
        this.update(delta);
        if (AssetManagerSingleton.getInstance().update() && !this.ready) {
            this.fadeOut();
        }
        super.render(delta);
    }

    private void fadeOut() {
        this.ready = true;
        Image fadeOut = new Image(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
        fadeOut.setSize(MoonBase.SCREEN_WIDTH + 40, MoonBase.SCREEN_HEIGHT + 40);
        fadeOut.setColor(0.0f, 0.0f, 0.0f, 0.0f);
        fadeOut.setPosition(-20.0f, -20.0f);
        this.stage.addActor(fadeOut);
        fadeOut.addAction(Actions.sequence((Action)Actions.fadeIn(0.4f), (Action)Actions.delay(0.1f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                LoadingScreen.this.finished();
            }
        })));
    }

    public void finished() {
        Gdx.app.log("MewnBase", "Done loading game assets.");
        System.out.println("\n----------\n");
        this.game.setScreen(new GameScreen(this.game, this.newGame));
        AssetManagerSingleton.getInstance().unload("loading-bg.png");
        AssetManagerSingleton.getInstance().unload("loading.atlas");
        this.dispose();
    }
}

