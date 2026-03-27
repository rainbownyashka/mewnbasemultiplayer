/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.SettingsPopup;
import java.util.ArrayList;

public class MoreMenu
extends Menu {
    private SelectBox languageBox;
    private Label createdBy;
    private Label name;
    private Label website;
    private Label moreCredits;
    private Label specialThanks;
    private TextButton btnBack;
    private Label copiedNotification;
    private Image tintBg;
    private Table buttonTable;
    private Table rightTable;
    private float translationTimer = 6.0f;
    private float translationDelay = 4.0f;
    private int translatorIndex = 0;
    private ArrayList<String> translatorStrings = new ArrayList();
    private Stack translatorStack;
    private ScrollPane scroller;

    public MoreMenu(BaseScreen baseScreen) {
        super(baseScreen);
        this.setup();
        this.show();
    }

    @Override
    public void show() {
        super.show();
        this.tintBg.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.alpha(0.4f, 0.5f)));
        this.buttonTable.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.moveBy(-40.0f, 0.0f), (Action)Actions.parallel((Action)Actions.moveBy(40.0f, 0.0f, 0.5f, Interpolation.sineOut), (Action)Actions.fadeIn(0.5f))));
        this.rightTable.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.moveBy(40.0f, 0.0f), (Action)Actions.parallel((Action)Actions.moveBy(-40.0f, 0.0f, 0.5f, Interpolation.sineOut), (Action)Actions.fadeIn(0.5f))));
        this.btnBack.addAction(Actions.sequence((Action)Actions.fadeIn(0.5f)));
    }

    @Override
    public void setup() {
        this.tintBg = new Image(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
        this.tintBg.setColor(0.0f, 0.0f, 0.0f, 0.0f);
        this.tintBg.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        this.menuGroup.addActor(this.tintBg);
        float buttonFontScale = 0.45f;
        float buttonWidth = 340.0f;
        float buttonHeight = 72.0f;
        float buttonSpacing = 5.0f;
        this.buttonTable = new Table();
        this.menuGroup.addActor(this.buttonTable);
        this.buttonTable.setVisible(false);
        this.buttonTable.left();
        this.buttonTable.setPosition(90.0f, MoonBase.SCREEN_HEIGHT / 2 + 50);
        this.buttonTable.setWidth(buttonWidth);
        this.buttonTable.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        this.buttonTable.row();
        TextButton btnOptions = new TextButton(Localization.get("options"), this.baseScreen.textBtnStyle);
        btnOptions.getLabel().setFontScale(buttonFontScale);
        btnOptions.padLeft(25.0f);
        btnOptions.getLabel().setAlignment(8);
        btnOptions.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MoreMenu.this.baseScreen.menuForwardSound();
                MoreMenu.this.baseScreen.showMenu(new SettingsPopup(MoreMenu.this.baseScreen));
            }
        });
        this.buttonTable.add(btnOptions).fill().width(buttonWidth).height(90.0f).space(buttonSpacing).padBottom(3.0f);
        this.buttonTable.row();
        TextButton btnForums = new TextButton(Localization.get("forums"), this.baseScreen.altTextBtnStyle);
        btnForums.getLabel().setAlignment(8);
        btnForums.padLeft(25.0f);
        btnForums.getLabel().setFontScale(buttonFontScale);
        btnForums.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MoreMenu.this.baseScreen.menuForwardSound();
                Gdx.net.openURI("https://cairn4.itch.io/mewnbase/community");
            }
        });
        this.buttonTable.add(btnForums).fill().width(buttonWidth).height(buttonHeight).space(buttonSpacing);
        this.buttonTable.row();
        TextButton btnDevblog = new TextButton(Localization.get("devblog"), this.baseScreen.altTextBtnStyle);
        btnDevblog.getLabel().setAlignment(8);
        btnDevblog.padLeft(25.0f);
        btnDevblog.getLabel().setFontScale(buttonFontScale);
        btnDevblog.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MoreMenu.this.baseScreen.menuForwardSound();
                Gdx.net.openURI("https://cairn4.itch.io/mewnbase/devlog");
            }
        });
        this.buttonTable.add(btnDevblog).fill().width(buttonWidth).height(buttonHeight).space(buttonSpacing);
        this.buttonTable.row();
        Table discordBtnTable = new Table();
        discordBtnTable.left();
        Group textGroup = new Group();
        textGroup.setPosition(570.0f, 0.0f);
        textGroup.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        this.menuGroup.addActor(textGroup);
        this.rightTable = new Table();
        this.rightTable.left();
        this.rightTable.setPosition(520.0f, MoonBase.SCREEN_HEIGHT / 2 + 50);
        this.rightTable.setWidth(MoonBase.SCREEN_WIDTH - 530);
        this.rightTable.setVisible(false);
        this.menuGroup.addActor(this.rightTable);
        this.createdBy = new Label((CharSequence)Localization.get("credits_createdBy"), this.labelStyle);
        this.createdBy.setWrap(true);
        this.createdBy.setWidth(600.0f);
        this.createdBy.setAlignment(8);
        this.createdBy.setFontScale(0.45f);
        this.createdBy.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.rightTable.add(this.createdBy).left().width(600.0f).spaceBottom(5.0f);
        this.rightTable.row();
        this.name = new Label((CharSequence)"Steve Forde", this.headingStyle);
        this.name.setWrap(true);
        this.name.setAlignment(8);
        this.name.setFontScale(0.7f);
        this.name.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.rightTable.add(this.name).left().width(600.0f);
        this.rightTable.row();
        NinePatch divNinePatch = new NinePatch(this.skin.getRegion("credits-divider"), 10, 10, 0, 0);
        Image horz = new Image(divNinePatch);
        this.rightTable.add(horz).padTop(20.0f).padBottom(20.0f).width(1500.0f).height(5.0f).left();
        this.rightTable.row();
        this.moreCredits = new Label((CharSequence)Localization.get("credits_additional"), this.labelStyle);
        this.moreCredits.setWrap(true);
        this.moreCredits.setAlignment(10);
        this.moreCredits.setFontScale(0.35f);
        this.moreCredits.setDebug(false);
        this.moreCredits.setColor(1.0f, 1.0f, 1.0f, 0.8f);
        this.rightTable.add(this.moreCredits).width(580.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER()).left();
        this.rightTable.row();
        this.specialThanks = new Label((CharSequence)(Localization.get("credits_specialthanks") + "Sarah Forde and \"M\""), this.labelStyle);
        this.specialThanks.setWrap(true);
        this.specialThanks.setAlignment(10);
        this.specialThanks.setFontScale(0.35f);
        this.specialThanks.setDebug(false);
        this.specialThanks.setColor(1.0f, 1.0f, 1.0f, 0.8f);
        this.rightTable.add(this.specialThanks).width(580.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER()).left().spaceTop(15.0f);
        this.rightTable.row();
        Label translatorHeader = new Label((CharSequence)Localization.get("credits_translationHelp"), this.labelStyle);
        translatorHeader.setFontScale(0.35f);
        translatorHeader.setAlignment(10);
        translatorHeader.setColor(1.0f, 1.0f, 1.0f, 0.8f);
        this.rightTable.add(translatorHeader).width(580.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER()).left().spaceTop(25.0f);
        this.rightTable.row();
        this.setupTranslatorScroller();
        this.rightTable.add(this.scroller).width(730.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER()).height(180.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER()).left().spaceTop(5.0f);
        this.rightTable.layout();
        this.btnBack = new TextButton(Localization.get("back").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnBack.setSize(200.0f, 91.0f);
        this.btnBack.getLabel().setFontScale(0.6f);
        this.btnBack.setPosition(MoonBase.SCREEN_WIDTH / 2, 40.0f, 4);
        this.menuGroup.addActor(this.btnBack);
        this.btnBack.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MoreMenu.this.baseScreen.menuBackSound();
                MoreMenu.this.back();
            }
        });
        this.btnBack.setColor(1.0f, 1.0f, 1.0f, 0.0f);
    }

    private void setupTranslatorScroller() {
        Table transTable = new Table();
        transTable.left();
        transTable.setBackground(this.skin.getDrawable("dark-edge"));
        transTable.pad(8.0f);
        String s = new String("Vakoss, bugra00748, Minopharyngite, animator.758, AlisAlia, mewnstrelkin, xars, Mirashi, divinataba, krencl, rapdia, woofiramy, skitou, Olga_pl3, spektro, Aaron100, jocsencat, tiwi90, orioncafune_, valeriia.kornuta, vinzleage, Fwefwefqwd, ldiogotrindade, lampovyy, filipcuk2, HRL, Kloido, arusatava, Ghooz, playsirtimebog, Mk1315, efedesu, isaacrafael_, pacpac0812, nordblom1, EzMi1207, gilp, maestroh, vikjITA, Bemos, Xernito, cos007kun2, trarg53, Ailaa, Mqro, PrinceNorris, bLuRY, cZeler, Dimmimix, ");
        s = s + "spacedog, cmoa, nfnd07, MrFoxon, tertip4123, meikopfaffmann03, QuestyNesty, AndSilenXer, lxc126221, Ronegrim, sangotxd, Foxpile, bohdanlet, PyroTechnist, Emu-Phoenix, jantibey, atempro22, MrPinata, Francescoinno, sachagirvan7, Charlongolo, OsoPooky, msx80, tankionlayn2281337, LuuzViir, tealena, tiagomesquita, Tarum, gavnoyouyou, palhex, Nitrine, Tyln, animatic, Origami, pasiasta_zebra, CactusBro, mburakeker, rusana, boterpoes, jim1240, SUPP3, Arrhytmic_Cat, norwoks, micahdgameplays, Blueberryy, LMG, KacperPolak, owo_whatsThis, Jamaro, magngut, ";
        s = s + "lgds778, -Stefano, inoromi245, amfbpourjeux, TARS, LukasHotarek7cz, Bonnie20402, finxn77, winchan, BlindBear, 123xiaochuang, AidenRomand, flatron4eg, dafe1304.hs, Nico_ITP6, ivan123-ru, Delofon, EpSerChan, DenielF, IanPolarys, cHyper, play8utuy, ladenthinxenia, Tillikum, WolfieTundra, Cheburiek, WolfernGames_YT, M4rg0t, EdSkellington, Nazarenus, LufBR, pozitivoss, avpetr, Dr_Hristov, GiorgioHerbie, Beshaba, LolCatZerg, Davizinhn, DexterIt, lira7725, gorlov.kotyara2008, Lobanych, Annelotte, donutcake1, Girlin, shinijindesuzero, arturp11, stourouchristina, Krzeszny, ";
        s = s + "Irakli1811, Danielaco23, ruttkaym, mainusone, JVC, Reals, IkanRasa, rogepix, ivanbt007, gykorx, MotteKosinus, vitek07k, yerayrh, Scabo, diavolo44, vinzv, pietersmamax2005, schamane, yoshinoda93, Meuhmeuh, Soqods, Syks, luismaruccio, fzhovo, Denkon, alexismainonis, rathauslevel091, LordAtomiiX, tizl1ght, CallMeFoxie, bethybee, kal11, harrison.bleckmann, spotduster, giorgiomorera, lel_game, gavincrumpfff, ek20082011, Karol.A, NyaX3M, axelgreenkp, Salmonella2, Mopsgamer";
        this.translatorStrings.add(s);
        for (String tString : this.translatorStrings) {
            Label translatorsLabel = new Label((CharSequence)tString, this.labelStyle);
            translatorsLabel.setWrap(true);
            translatorsLabel.setAlignment(10);
            translatorsLabel.setFontScale(0.25f);
            translatorsLabel.setColor(1.0f, 1.0f, 1.0f, 0.8f);
            transTable.add(translatorsLabel).expandY().width(670.0f * MoonBase.GET_SCREENSIZE_MULTIPLIER());
            transTable.row();
        }
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.baseScreen.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.baseScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)transTable, scrollStyle);
        this.scroller.setWidth(transTable.getWidth());
        this.scroller.setHeight(transTable.getHeight());
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        scrollbar.setColor(new Color(1.0f, 1.0f, 1.0f, 0.4f));
        this.scroller.setPosition(75.0f, 60.0f);
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
    }

    @Override
    public void update(float delta) {
    }

    private void nextMenu(Screen screen) {
        final Screen finalScreen = screen;
        this.menuGroup.addAction(Actions.sequence((Action)Actions.parallel(), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                MoreMenu.this.baseScreen.game.setScreen(finalScreen);
            }
        })));
    }
}

