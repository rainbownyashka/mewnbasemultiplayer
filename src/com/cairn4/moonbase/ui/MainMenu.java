/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
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
import com.cairn4.moonbase.LoadingErrors;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.RichPresenceState;
import com.cairn4.moonbase.SpineActor;
import com.cairn4.moonbase.UpdateCheck;
import com.cairn4.moonbase.ui.AutoScaleTextButton;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.ErrorLoadingPopup;
import com.cairn4.moonbase.ui.LoadGameMenu;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.MoreMenu;
import com.cairn4.moonbase.ui.NewGameConfirm;
import com.cairn4.moonbase.ui.NewGameScreen;
import com.cairn4.moonbase.ui.WhatsNewPopup;
import com.esotericsoftware.spine.SkeletonRenderer;

import java.net.Socket;
import java.net.InetSocketAddress;

public class MainMenu
extends Menu {
    Label credit;
    Label status;
    Label build;
    Label updateAvailable;
    private static String creditText = "Steve Forde @cairn4";
    AutoScaleTextButton.AutoScaleTextButtonStyle autoScaleTextButtonStyle;
    AutoScaleTextButton btnNewGame;
    AutoScaleTextButton btnContinue;
    AutoScaleTextButton btnMore;
    AutoScaleTextButton btnExit;
    Button btnFullScreen;
    Group socialButtonGroup;
    Button btnTwitter;
    Button btnDiscord;
    float socialMoveTime;
    boolean newGame;
    SkeletonRenderer skeletonRenderer;
    private SpineActor logoSpineActor;
    private AdditiveImage creditHoverGlow;
    private AdditiveImage buildHoverGlow;
    private Table buttonTable;
    private Group logoGroup;
    private AdditiveImage titleGlow;

    public MainMenu(BaseScreen baseScreen, boolean newGame) {
        super(baseScreen);
        this.newGame = newGame;
        this.skeletonRenderer = new SkeletonRenderer();
        AssetManagerSingleton.getInstance().load("playerheads.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("loading.atlas", TextureAtlas.class);
        AssetManagerSingleton.getInstance().load("loading-bg.png", Texture.class);
        AssetManagerSingleton.getInstance().finishLoading();
        this.autoScaleTextButtonStyle = new AutoScaleTextButton.AutoScaleTextButtonStyle(baseScreen.textBtnStyle);
        this.autoScaleTextButtonStyle.maxScale = 0.6f;
        this.autoScaleTextButtonStyle.minScale = 0.2f;
        this.autoScaleTextButtonStyle.horzPadding = 40.0f;
        this.setup();
        this.show(true);
    }

    @Override
    public void hide() {
        this.menuGroup.addAction(Actions.sequence((Action)Actions.fadeOut(0.3f)));
    }

    @Override
    public void returned() {
        this.font.getData().setScale(1.0f);
        this.show(false);
        this.menuGroup.addAction(Actions.sequence((Action)Actions.fadeIn(0.3f)));
    }

    public MainMenu(BaseScreen baseScreen, LoadingErrors error) {
        this(baseScreen, false);
        this.baseScreen.showMenu(new ErrorLoadingPopup(this.baseScreen, error));
    }

    private void addWaves() {
        Group waves = new Group();
        waves.setPosition(MoonBase.SCREEN_WIDTH / 2, (float)(MoonBase.SCREEN_HEIGHT / 2) - 60.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER());
        this.menuGroup.addActor(waves);
        waves.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.fadeIn(1.5f)));
        float c1Scale = 1.52f;
        Texture cloud1 = (Texture)AssetManagerSingleton.getInstance().get("bg-cloud1.png");
        cloud1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.MirroredRepeat);
        TextureRegion imgTextureRegion = new TextureRegion(cloud1);
        imgTextureRegion.setRegion(0, 0, cloud1.getWidth() * 6, cloud1.getHeight());
        AdditiveImage i = new AdditiveImage(imgTextureRegion);
        i.setColor(new Color(0.2509804f, 0.0f, 1.0f, 0.18f));
        i.setScale(c1Scale, 1.25f);
        waves.addActor(i);
        float startX1 = -100.0f;
        int align1 = 2;
        i.addAction(Actions.forever(Actions.sequence((Action)Actions.moveToAligned(startX1, 0.0f, align1), (Action)Actions.moveToAligned(startX1 + (float)cloud1.getWidth() * c1Scale, 0.0f, align1, 18.315f), (Action)Actions.moveToAligned(startX1, 0.0f, align1))));
        Texture cloud2 = (Texture)AssetManagerSingleton.getInstance().get("bg-cloud1.png");
        cloud2.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.MirroredRepeat);
        TextureRegion imgTextureRegion2 = new TextureRegion(cloud1);
        imgTextureRegion2.setRegion(0, 0, cloud1.getWidth() * 5, cloud1.getHeight());
        float c2Scale = 1.93f;
        float startX2 = -200.0f;
        AdditiveImage i2 = new AdditiveImage(imgTextureRegion2);
        i2.setScale(c2Scale, 1.36f);
        i2.setColor(new Color(0.2901961f, 0.18431373f, 0.9490196f, 0.2f));
        waves.addActor(i2);
        i2.addAction(Actions.forever(Actions.sequence((Action)Actions.moveToAligned(startX2, 0.0f, align1), (Action)Actions.moveToAligned(startX2 - (float)cloud2.getWidth() * c2Scale, 0.0f, align1, 22.0f), (Action)Actions.moveToAligned(startX2, 0.0f, align1))));
    }

    @Override
    public void setup() {
        RichPresenceState rpstate = new RichPresenceState();
        rpstate.state = RichPresenceState.States.menu;
        this.baseScreen.game.platformAdapter.updateRPC(rpstate);
        this.menuGroup.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        this.addWaves();
        this.logoGroup = new Group();
        this.menuGroup.addActor(this.logoGroup);
        this.logoGroup.setPosition(MoonBase.SCREEN_WIDTH / 2, (float)(MoonBase.SCREEN_HEIGHT / 2) + 74.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER(), 1);
        this.logoGroup.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        this.titleGlow = new AdditiveImage(this.skin.getDrawable("small-gradient-circle"));
        this.titleGlow.setSize(500.0f, 500.0f);
        this.titleGlow.setColor(0.5f, 0.3f, 0.8f, 0.0f);
        this.titleGlow.setOrigin(250.0f, 125.0f);
        this.titleGlow.setPosition(-360.0f, -120.0f);
        this.titleGlow.setScale(MoonBase.GET_SCREENSIZE_MULTIPLIER());
        this.logoGroup.addActor(this.titleGlow);
        float logoScale = 0.82f * (float)MoonBase.SCREEN_HEIGHT / 720.0f;
        this.logoSpineActor = new SpineActor("logo", logoScale, this.skeletonRenderer);
        this.logoSpineActor.setPosition(0.0f, 0.0f);
        this.logoGroup.addActor(this.logoSpineActor);
        this.logoSpineActor.state.getData().setDefaultMix(0.0f);
        this.logoSpineActor.setMix("show", "idle", 0.16f);
        float buttonWidth = 210.0f;
        float buttonHeight = 91.0f;
        float buttonSpacing = 5.0f;
        this.buttonTable = new Table();
        this.menuGroup.addActor(this.buttonTable);
        this.buttonTable.bottom();
        this.buttonTable.setPosition(MoonBase.SCREEN_WIDTH / 2, 100.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER());
        this.btnNewGame = new AutoScaleTextButton(Localization.get("newGame").toUpperCase(), this.autoScaleTextButtonStyle);
        this.btnNewGame.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MainMenu.this.baseScreen.menuForwardSound();
                MainMenu.this.newGameAction();
            }
        });
        this.buttonTable.add(this.btnNewGame).fill().minWidth(buttonWidth).height(buttonHeight).space(buttonSpacing);

        this.btnContinue = new AutoScaleTextButton(Localization.get("continue").toUpperCase(), this.autoScaleTextButtonStyle);
        this.btnContinue.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MainMenu.this.baseScreen.menuForwardSound();
                MainMenu.this.baseScreen.showMenu(new LoadGameMenu(MainMenu.this.baseScreen));
            }
        });
        this.buttonTable.add(this.btnContinue).fill().minWidth(buttonWidth).height(buttonHeight).space(buttonSpacing);

        this.btnMore = new AutoScaleTextButton(Localization.get("mainmenu_more").toUpperCase(), this.autoScaleTextButtonStyle);
        this.btnMore.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MainMenu.this.baseScreen.menuForwardSound();
                MainMenu.this.baseScreen.showMenu(new MoreMenu(MainMenu.this.baseScreen));
            }
        });
        this.buttonTable.add(this.btnMore).fill().minWidth(buttonWidth).height(buttonHeight).space(buttonSpacing);

        this.btnExit = new AutoScaleTextButton(Localization.get("exit").toUpperCase(), this.autoScaleTextButtonStyle);
        this.btnExit.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        this.buttonTable.add(this.btnExit).fill().minWidth(buttonWidth).height(buttonHeight).space(buttonSpacing);

        // Multiplayer button (модовый стиль)
        NinePatchDrawable alt_npd_up = new NinePatchDrawable(new NinePatch(this.skin.getRegion("btn-minor"), 16, 16, 16, 16));
        NinePatchDrawable alt_npd_active = new NinePatchDrawable(new NinePatch(this.skin.getRegion("btn-minor-pressed"), 16, 16, 16, 16));
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = alt_npd_up;
        buttonStyle.down = alt_npd_active;
        TextButton btnMultiplayer = new TextButton("MULTIPLAYER", buttonStyle);
        btnMultiplayer.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MainMenu.this.baseScreen.menuForwardSound();
                MainMenu.this.baseScreen.showMenu(new MultiplayerConfigMenu(MainMenu.this.baseScreen));
            }
        });
        this.buttonTable.add(btnMultiplayer).fill().minWidth(buttonWidth).height(buttonHeight).space(buttonSpacing);
        this.buttonTable.layout();
        this.maybeAutoOpenMultiplayer();
        this.credit = new Label((CharSequence)creditText, this.labelStyle);
        this.credit.setAlignment(10);
        this.credit.setFontScale(0.5f);
        this.credit.setPosition(30.0f, -15.0f);
        this.credit.setColor(1.0f, 1.0f, 1.0f, 0.8f);
        this.credit.setVisible(true);
        this.credit.setWidth(this.credit.getPrefWidth());
        this.menuGroup.addActor(this.credit);
        String version_suffix = "";
        this.build = new Label((CharSequence)("v1.0.1" + version_suffix + " - Nov 2023"), this.labelStyle);
        this.build.setFontScale(0.45f);
        this.build.setAlignment(18);
        this.build.setWidth(this.build.getPrefWidth());
        this.build.setPosition(MoonBase.SCREEN_WIDTH - 30, -15.0f, 20);
        this.build.setColor(1.0f, 1.0f, 1.0f, 0.8f);
        this.build.setVisible(false);
        this.build.setTouchable(Touchable.enabled);
        this.menuGroup.addActor(this.build);
        this.buildHoverGlow = new AdditiveImage(this.skin.getDrawable("small-gradient-circle"));
        this.buildHoverGlow.setSize(700.0f, 200.0f);
        this.buildHoverGlow.setColor(0.5f, 0.3f, 0.8f, 0.5f);
        this.buildHoverGlow.setVisible(false);
        this.buildHoverGlow.setOrigin(1);
        this.buildHoverGlow.setPosition(this.build.getX(1), 0.0f, 1);
        this.menuGroup.addActor(this.buildHoverGlow);
        this.buildHoverGlow.toBack();
        this.build.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MainMenu.this.baseScreen.menuForwardSound();
                MainMenu.this.baseScreen.showMenu(new WhatsNewPopup(MainMenu.this.baseScreen));
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                MainMenu.this.buildHoverGlow.setVisible(true);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                MainMenu.this.buildHoverGlow.setVisible(false);
            }
        });
        TextButton updateButton = new TextButton("", this.baseScreen.altTextBtnStyle);
        updateButton.setSize(252.0f, 50.0f);
        updateButton.setColor(1.0f, 1.0f, 1.0f, 0.15f);
        updateButton.setPosition(MoonBase.SCREEN_WIDTH - 10, 48.0f, 20);
        updateButton.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (MoonBase.STEAM_VERSION) {
                    Gdx.net.openURI("https://store.steampowered.com/app/743130/MewnBase/");
                } else {
                    Gdx.net.openURI("https://cairn4.itch.io/mewnbase");
                }
            }
        });
        this.menuGroup.addActor(updateButton);
        updateButton.setVisible(false);
        this.updateAvailable = new Label((CharSequence)Localization.get("updateAvailable"), this.labelStyle);
        this.updateAvailable.setWidth(MoonBase.SCREEN_WIDTH / 2);
        this.updateAvailable.setAlignment(16);
        this.updateAvailable.setFontScale(0.4f);
        this.updateAvailable.setPosition(MoonBase.SCREEN_WIDTH - 30, 39.0f, 20);
        this.updateAvailable.setColor(Menu.MENU_COLOR);
        this.updateAvailable.setTouchable(Touchable.disabled);
        this.updateAvailable.setVisible(false);
        this.menuGroup.addActor(this.updateAvailable);
        if (UpdateCheck.UPDATE_AVAILABLE) {
            updateButton.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.delay(1.0f), (Action)Actions.alpha(0.15f, 1.0f)));
            this.updateAvailable.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.delay(1.0f), (Action)Actions.fadeIn(1.0f)));
        }
        Button.ButtonStyle bs = new Button.ButtonStyle();
        bs.up = this.skin.getDrawable("btn-fullscreen");
        bs.over = this.skin.getDrawable("btn-fullscreen-hover");
        bs.down = this.skin.getDrawable("btn-fullscreen-pressed");
        this.btnFullScreen = new Button(bs);
        this.btnFullScreen.setPosition(MoonBase.SCREEN_WIDTH - 15, MoonBase.SCREEN_HEIGHT - 15, 18);
        this.menuGroup.addActor(this.btnFullScreen);
        this.btnFullScreen.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MainMenu.this.baseScreen.game.toggleFullScreen();
            }
        });
        this.socialButtonGroup = new Group();
        this.menuGroup.addActor(this.socialButtonGroup);
        AdditiveImage glow = new AdditiveImage(this.skin.getDrawable("small-gradient-circle"));
        glow.setColor(Color.valueOf("5204f8b0"));
        glow.setSize(250.0f, 250.0f);
        glow.setOrigin(1);
        glow.setPosition(144.0f, MoonBase.SCREEN_HEIGHT - 89, 1);
        this.socialButtonGroup.addActor(glow);
        this.addDiscordButton();
        this.addTwitterButton();
        this.textToAutoScale.add(this.btnNewGame);
        this.textToAutoScale.add(this.btnContinue);
        this.textToAutoScale.add(this.btnMore);
        this.textToAutoScale.add(this.btnExit);
        this.setupHoverImages(this.btnNewGame, "new-lander");
        this.setupHoverImages(this.btnContinue, "load-fish");
        this.setupHoverImages(this.btnMore, "more-ball");
        this.setupHoverImages(this.btnExit, "exit-sign");
    }

    public void show(boolean introAnim) {
        super.show();
        if (introAnim) {
            this.doIntroAnim();
        }
    }

    private void doIntroAnim() {
        this.menuGroup.addAction(Actions.sequence((Action)Actions.color(Color.WHITE, 0.05f)));
        float spinSpeed = 5.0f;
        float spinAlpha = 0.3f;
        this.titleGlow.addAction(Actions.sequence((Action)Actions.delay(1.8f), (Action)Actions.forever(Actions.sequence((Action)Actions.parallel((Action)Actions.rotateTo(180.0f, spinSpeed), (Action)Actions.color(new Color(1.0f, 0.7f, 0.0f, spinAlpha), spinSpeed)), (Action)Actions.parallel((Action)Actions.rotateTo(360.0f, spinSpeed), (Action)Actions.color(new Color(0.5f, 0.3f, 0.8f, spinAlpha), spinSpeed)), (Action)Actions.rotateTo(0.0f)))));
        this.logoSpineActor.state.clearTracks();
        this.logoSpineActor.state.addAnimation(0, "hidden", false, 0.0f);
        this.logoSpineActor.state.addAnimation(0, "show", false, 0.2f);
        this.logoSpineActor.state.addAnimation(0, "idle", true, 0.0f);
        this.logoGroup.addAction(Actions.sequence((Action)Actions.color(new Color(1.0f, 1.0f, 1.0f, 1.0f), 0.05f), (Action)Actions.moveBy(0.0f, -10.0f), (Action)Actions.delay(0.2f), (Action)Actions.parallel((Action)Actions.moveBy(0.0f, 10.0f, 1.8f, Interpolation.sineOut)), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                MainMenu.this.finishedShowAnim();
            }
        })));
        this.buttonTable.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.alpha(1.0f, 1.0f)));
        this.socialButtonGroup.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.delay(1.25f), (Action)Actions.alpha(1.0f, 0.5f)));
        this.credit.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.delay(1.25f), (Action)Actions.alpha(0.8f, 0.5f)));
        this.build.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.delay(1.25f), (Action)Actions.alpha(0.8f, 0.5f)));
    }

    @Override
    protected void finishedShowAnim() {
        MoonBase.log("finished showing main menu");
    }

    private void maybeAutoOpenMultiplayer() {
        String autoMenu = System.getProperty("mewnbase.autoMenu");
        String autoConnect = System.getProperty("mewnbase.mp.autoconnect");
        String host = System.getProperty("mewnbase.mp.host");
        String port = System.getProperty("mewnbase.mp.port");
        String nick = System.getProperty("mewnbase.mp.nick");

        boolean shouldOpen = false;
        if (autoMenu != null && autoMenu.trim().length() > 0) {
            String v = autoMenu.trim().toLowerCase();
            shouldOpen = v.equals("mp") || v.equals("multiplayer");
        }
        if (!shouldOpen) {
            shouldOpen = (autoConnect != null && autoConnect.trim().length() > 0)
                    || (host != null && host.trim().length() > 0)
                    || (port != null && port.trim().length() > 0)
                    || (nick != null && nick.trim().length() > 0);
        }

        if (!shouldOpen) {
            return;
        }
        Gdx.app.postRunnable(() -> MainMenu.this.baseScreen.showMenu(new MultiplayerConfigMenu(MainMenu.this.baseScreen)));
    }

    private void addTwitterButton() {
        final Group twitterGroup = new Group();
        twitterGroup.setPosition(72.0f, MoonBase.SCREEN_HEIGHT - 177);
        this.socialButtonGroup.addActor(twitterGroup);
        Button.ButtonStyle bs = new Button.ButtonStyle();
        bs.up = this.skin.getDrawable("mm-mastodon");
        bs.over = this.skin.getDrawable("mm-mastodon-hover");
        bs.down = this.skin.getDrawable("mm-mastodon-pressed");
        this.btnTwitter = new Button(bs);
        this.btnTwitter.setPosition(0.0f, 0.0f, 1);
        this.btnTwitter.setOrigin(1);
        twitterGroup.addActor(this.btnTwitter);
        this.btnTwitter.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MainMenu.this.baseScreen.menuForwardSound();
                Gdx.net.openURI("https://mastodon.social/@cairn4");
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                twitterGroup.clearActions();
                twitterGroup.addAction(Actions.sequence((Action)Actions.scaleTo(1.1f, 1.1f, 0.025f, Interpolation.sine)));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                twitterGroup.clearActions();
                twitterGroup.addAction(Actions.sequence((Action)Actions.scaleTo(1.0f, 1.0f, 0.025f, Interpolation.sine)));
            }
        });
    }

    private void addDiscordButton() {
        final Group discordGroup = new Group();
        discordGroup.setPosition(148.0f, MoonBase.SCREEN_HEIGHT - 87);
        this.socialButtonGroup.addActor(discordGroup);
        Button.ButtonStyle bs = new Button.ButtonStyle();
        bs.up = this.skin.getDrawable("mm-discord");
        bs.over = this.skin.getDrawable("mm-discord-hover");
        bs.down = this.skin.getDrawable("mm-discord-pressed");
        this.btnDiscord = new Button(bs);
        this.btnDiscord.setPosition(0.0f, 0.0f, 1);
        this.btnDiscord.setOrigin(1);
        discordGroup.addActor(this.btnDiscord);
        this.btnDiscord.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MainMenu.this.baseScreen.menuForwardSound();
                Gdx.net.openURI("https://discord.gg/mewnbase");
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                discordGroup.clearActions();
                discordGroup.addAction(Actions.sequence((Action)Actions.scaleTo(1.1f, 1.1f, 0.05f, Interpolation.sineIn)));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                discordGroup.clearActions();
                discordGroup.addAction(Actions.sequence((Action)Actions.scaleTo(1.0f, 1.0f, 0.05f, Interpolation.sineIn)));
            }
        });
    }

    private void setupHoverImages(TextButton button, String baseIconName) {
        final float yPos = 100.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER() + 140.0f;
        float p = button.getX(1);
        final Group loadHoverGroup = new Group();
        loadHoverGroup.setScale(0.6f);
        loadHoverGroup.setPosition((float)(MoonBase.SCREEN_WIDTH / 2) + p, yPos);
        loadHoverGroup.setTouchable(Touchable.disabled);
        this.menuGroup.addActor(loadHoverGroup);
        loadHoverGroup.toBack();
        final AdditiveImage bgGlow = new AdditiveImage(this.skin.getDrawable("small-gradient-circle"));
        bgGlow.setTouchable(Touchable.disabled);
        bgGlow.setSize(700.0f, 700.0f);
        bgGlow.setPosition(0.0f, -150.0f, 1);
        bgGlow.setColor(1.0f, 0.36078432f, 0.22745098f, 0.7f);
        loadHoverGroup.addActor(bgGlow);
        final AdditiveImage loadHoverGlow = new AdditiveImage(this.skin.getDrawable(baseIconName + "-glow"));
        loadHoverGlow.setPosition(0.0f, 0.0f, 1);
        loadHoverGlow.getColor().a = 0.35f;
        loadHoverGlow.setOrigin(1);
        loadHoverGlow.setTouchable(Touchable.disabled);
        loadHoverGroup.addActor(loadHoverGlow);
        final AdditiveImage loadHoverStats = new AdditiveImage(this.skin.getDrawable(baseIconName + "-stats"));
        loadHoverStats.setPosition(0.0f, 0.0f, 1);
        loadHoverStats.setOrigin(1);
        loadHoverStats.getColor().a = 0.0f;
        loadHoverStats.setTouchable(Touchable.disabled);
        loadHoverGroup.addActor(loadHoverStats);
        AdditiveImage loadHover = new AdditiveImage(this.skin.getDrawable(baseIconName));
        loadHover.setPosition(0.0f, 0.0f, 1);
        loadHover.setOrigin(1);
        loadHover.setTouchable(Touchable.disabled);
        loadHoverGroup.addActor(loadHover);
        loadHoverGroup.getColor().a = 0.0f;
        button.getLabel().setTouchable(Touchable.disabled);
        button.addListener(new ClickListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                loadHoverGroup.clearActions();
                loadHoverGroup.addAction(Actions.sequence((Action)Actions.moveTo(loadHoverGroup.getX(), yPos - 10.0f), (Action)Actions.scaleTo(0.55f, 0.55f), (Action)Actions.parallel((Action)Actions.moveBy(0.0f, 10.0f, 0.23f, Interpolation.sineOut), (Action)Actions.scaleTo(0.6f, 0.6f, 0.23f), (Action)Actions.fadeIn(0.23f))));
                bgGlow.clearActions();
                bgGlow.addAction(Actions.sequence((Action)Actions.alpha(0.7f), (Action)Actions.forever(Actions.sequence((Action)Actions.alpha(0.9f, 0.7f, Interpolation.sine), (Action)Actions.alpha(0.7f, 0.7f, Interpolation.sine)))));
                loadHoverGlow.clearActions();
                loadHoverGlow.addAction(Actions.sequence((Action)Actions.scaleTo(0.7f, 0.7f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.3f, Interpolation.sineOut)));
                loadHoverStats.clearActions();
                loadHoverStats.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.alpha(0.5f, 0.8f)));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                loadHoverGroup.clearActions();
                loadHoverGroup.addAction(Actions.sequence((Action)Actions.fadeOut(0.2f)));
            }
        });
    }

    private void addBgBoxes() {
        int number = 7;
        float d = 6.0f;
        Color startColor = new Color(0.5f, 0.3f, 0.8f, 0.0f);
        startColor.set(0.06666667f, 0.0f, 0.8039216f, 0.0f);
        for (int i = 0; i < number; ++i) {
            NinePatch ninepatch = new NinePatch(this.skin.getRegion("mm-box"), 50, 50, 50, 50);
            AdditiveImage box = new AdditiveImage(ninepatch);
            box.setSize(1000.0f, 400.0f);
            box.setOrigin(1);
            box.setColor(1.0f, 1.0f, 1.0f, 0.0f);
            box.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 60, 1);
            box.addAction(Actions.sequence((Action)Actions.delay((float)i * (d / (float)number)), (Action)Actions.forever(Actions.sequence((Action)Actions.scaleTo(0.25f, 0.25f), (Action)Actions.color(startColor), (Action)Actions.moveToAligned(MoonBase.SCREEN_WIDTH / 2, 300.0f, 1), (Action)Actions.parallel((Action)Actions.moveBy(0.0f, 100.0f, d, Interpolation.sineOut), (Action)Actions.scaleTo(1.5f, 1.5f, d, Interpolation.sineIn), (Action)Actions.sequence((Action)Actions.fadeIn(d / 2.0f), (Action)Actions.fadeOut(d / 2.0f)))))));
        }
    }

    private void newGameAction() {
        boolean hasOldSave = false;
        if (Gdx.files.local("world/").exists() && Gdx.files.local("world/gameSave.json").exists()) {
            hasOldSave = true;
        }
        if (hasOldSave) {
            this.baseScreen.showMenu(new NewGameConfirm(this.baseScreen));
        } else {
            this.baseScreen.showMenu(new NewGameScreen(this.baseScreen));
        }
    }

    public void nextMenu(Screen screen) {
        this.baseScreen.game.setScreen(screen);
    }

    @Override
    public void update(float delta) {
        this.animateSocialButtons(delta);
        this.logoSpineActor.update(delta);
        super.update(delta);
    }

    private void animateSocialButtons(float delta) {
        this.socialMoveTime += delta;
        float tX = MathUtils.sin(this.socialMoveTime / 3.0f) * 5.0f;
        float tY = MathUtils.cos(this.socialMoveTime / 2.0f) * 6.0f;
        this.btnTwitter.setPosition(tX, tY, 1);
        this.btnTwitter.getParent().setRotation(tX);
        float dX = MathUtils.sin(this.socialMoveTime / 2.5f) * 5.0f;
        float dY = MathUtils.cos(this.socialMoveTime / 1.5f) * 6.0f;
        this.btnDiscord.setPosition(dX, dY, 1);
        this.btnDiscord.getParent().setRotation(-dX);
    }

    @Override
    public void back() {
    }

    @Override
    public void handleInput() {
        if (PlayerInput.isPressed(0)) {
            this.focus(Menu.DIRECTIONS.left);
        }
        if (PlayerInput.isPressed(1)) {
            this.focus(Menu.DIRECTIONS.right);
        }
        if (PlayerInput.isPressed(2)) {
            this.focus(Menu.DIRECTIONS.up);
        }
        if (PlayerInput.isPressed(3)) {
            this.focus(Menu.DIRECTIONS.down);
        }
        if (PlayerInput.wasPressed(5)) {
            this.select();
        }
        if (PlayerInput.wasPressed(6)) {
            // empty if block
        }
        if (PlayerInput.wasPressed(26)) {
            this.baseScreen.game.toggleFullScreen();
        }
        PlayerInput.update();
    }
}
