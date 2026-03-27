/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Scaling;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.DayCycle;
import com.cairn4.moonbase.GameLoader;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.LoadOldSaveFilePopup;
import com.cairn4.moonbase.ui.LoadingScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import com.cairn4.moonbase.ui.SaveDeletePopup;
import com.cairn4.moonbase.ui.UpdateSaveWarningScreen;
import com.cairn4.moonbase.worlddata.GameSaveData;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;

public class LoadGameMenu
extends Popup {
    Label missionLabel;
    Label errorLabel;
    NinePatch completePanelPatch;
    ScrollPane scroller;
    Table table;
    TextButton btnBackToList;
    Group screenshotHolder;
    Image primaryScreenshot;
    Image prev1Screenshot;
    Image prev2Screenshot;
    Table detailsTable;
    Button.ButtonStyle buttonStyle;
    Button.ButtonStyle solidButtonStyle;
    Button.ButtonStyle backButtonStyle;
    Button.ButtonStyle deleteButtonStyle;
    Button.ButtonStyle folderButtonStyle;
    private ArrayList<GameSaveData> gsdList;
    TextureAtlas faceAtlas;
    private NinePatch np_up;
    private modes currentMode;
    private Group detailGroup;
    private Button folderButton;
    private Table saveRevTable;
    private Image staticImage;
    private final float screenshotWidth = 340.0f;
    private final float screenshotHeight = 310.0f;
    public ArrayList<String> staticSpriteList = new ArrayList();
    public float fps = 8.0f;
    private float fpsTimer = 0.0f;
    private int frameIndex = 0;
    private boolean playingStatic = false;

    public LoadGameMenu(BaseScreen baseScreen) {
        super(baseScreen);
        this.skin = baseScreen.skin;
        this.faceAtlas = AssetManagerSingleton.getInstance().get("playerheads.atlas", TextureAtlas.class);
        this.skin.addRegions(this.faceAtlas);
        TextureAtlas staticAtlas = AssetManagerSingleton.getInstance().get("static.atlas", TextureAtlas.class);
        this.skin.addRegions(staticAtlas);
        this.np_up = new NinePatch(this.skin.getRegion("btn-item"), 16, 16, 16, 16);
        NinePatchDrawable npd_up = new NinePatchDrawable(this.np_up);
        NinePatch np_active = new NinePatch(this.skin.getRegion("btn-item-active"), 16, 16, 16, 16);
        NinePatchDrawable npd_active = new NinePatchDrawable(np_active);
        NinePatch np_down = new NinePatch(this.skin.getRegion("btn-item-pressed"), 16, 16, 16, 16);
        NinePatchDrawable npd_down = new NinePatchDrawable(np_down);
        NinePatch np_orange_up = new NinePatch(this.skin.getRegion("btn-orange"), 16, 16, 16, 16);
        NinePatchDrawable npd_orange_up = new NinePatchDrawable(np_orange_up);
        NinePatch np_orange_active = new NinePatch(this.skin.getRegion("btn-orange-hover"), 16, 16, 16, 16);
        NinePatchDrawable npd_orange_active = new NinePatchDrawable(np_orange_active);
        NinePatch np_orange_down = new NinePatch(this.skin.getRegion("btn-orange-pressed"), 16, 16, 16, 16);
        NinePatchDrawable npd_orange_down = new NinePatchDrawable(np_orange_down);
        this.buttonStyle = new Button.ButtonStyle();
        this.buttonStyle.up = npd_up;
        this.buttonStyle.over = npd_active;
        this.buttonStyle.down = npd_down;
        this.buttonStyle.checked = npd_active;
        this.solidButtonStyle = new Button.ButtonStyle();
        this.solidButtonStyle.up = npd_orange_up;
        this.solidButtonStyle.over = npd_orange_active;
        this.solidButtonStyle.down = npd_orange_down;
        this.solidButtonStyle.checked = npd_active;
        this.backButtonStyle = new Button.ButtonStyle();
        this.backButtonStyle.up = this.skin.getDrawable("btn-back2");
        this.backButtonStyle.over = this.skin.getDrawable("btn-back2-hover");
        this.backButtonStyle.down = this.skin.getDrawable("btn-back2-pressed");
        this.deleteButtonStyle = new Button.ButtonStyle();
        this.deleteButtonStyle.up = this.skin.getDrawable("remove-icon");
        this.deleteButtonStyle.over = this.skin.getDrawable("remove-icon-hover");
        this.deleteButtonStyle.down = this.skin.getDrawable("remove-icon-pressed");
        this.folderButtonStyle = new Button.ButtonStyle();
        this.folderButtonStyle.up = this.skin.getDrawable("btn-folder");
        this.folderButtonStyle.over = this.skin.getDrawable("btn-folder-hover");
        this.folderButtonStyle.down = this.skin.getDrawable("btn-folder-pressed");
        this.folderButtonStyle.disabled = this.skin.getDrawable("btn-folder-disabled");
        this.tintBgAlpha = 0.0f;
        this.currentMode = modes.list;
        this.setup();
        this.show();
    }

    public LoadGameMenu(BaseScreen baseScreen, GameSaveData gsd) {
        this(baseScreen);
        this.tintBgAlpha = 0.0f;
        this.showDetailInfo(gsd);
    }

    private boolean canGetDesktopAPI() {
        String OS = System.getProperty("os.name");
        if (OS.equals("Mac OS X")) {
            return false;
        }
        if (OS.equals("Linux") || OS.contains("Windows")) {
            Desktop desktop = null;
            try {
                desktop = Desktop.getDesktop();
            }
            catch (Exception e) {
                MoonBase.error(e.getLocalizedMessage());
            }
            return desktop != null;
        }
        return false;
    }

    private void openSaveFolder() {
        String path = Gdx.files.local("saves").path();
        Desktop desktop = null;
        try {
            desktop = Desktop.getDesktop();
        }
        catch (Exception e) {
            MoonBase.error(e.getLocalizedMessage());
        }
        if (desktop != null) {
            File dirToOpen = null;
            System.out.println(path);
            try {
                dirToOpen = new File(path);
                try {
                    desktop.open(dirToOpen);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            catch (IllegalArgumentException iae) {
                Gdx.app.error("MewnBase", "Path not found: " + path);
            }
        }
    }

    @Override
    protected void setup() {
        super.setup();
        this.setTitle("loadGame");
        this.completePanelPatch = new NinePatch(this.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        this.setSize(1000.0f, 680.0f);
        this.setTitle("loadGame");
        if (this.canGetDesktopAPI()) {
            this.folderButton = new Button(this.folderButtonStyle);
            this.folderButton.setSize(46.0f, 46.0f);
            float x = this.btnClose.getX() - 45.0f;
            float y = this.btnClose.getY(1) - 1.0f;
            this.folderButton.setColor(1.0f, 1.0f, 1.0f, 0.8f);
            this.folderButton.setPosition(x, y, 1);
            this.popupGroup.addActor(this.folderButton);
            this.folderButton.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    LoadGameMenu.this.openSaveFolder();
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    super.enter(event, x, y, pointer, fromActor);
                    LoadGameMenu.this.createTooltip(Localization.get("openSavesFolder"));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    super.enter(event, x, y, pointer, toActor);
                    LoadGameMenu.this.removeTooltip();
                }
            });
        }
        this.setupTable();
        this.addListOptions();
        this.setupSaveRevTable();
        if (!Gdx.files.local("saves").exists() && this.folderButton != null) {
            this.folderButton.setDisabled(true);
            this.folderButton.setTouchable(Touchable.disabled);
        }
    }

    private void setupTable() {
        this.table = new Table();
        this.table.top().left();
        this.popupGroup.addActor(this.table);
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)this.table, scrollStyle);
        this.scroller.setWidth(this.table.getWidth());
        this.scroller.setHeight(this.table.getHeight());
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setBounds(0.0f, 0.0f, this.panelBg.getWidth() - 155.0f, this.panelBg.getHeight() - 195.0f);
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        this.scroller.setPosition(80.0f, 60.0f);
        this.popupGroup.addActor(this.scroller);
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.scroller);
    }

    private void setupSaveRevTable() {
        this.detailGroup = new Group();
        this.detailGroup.setTouchable(Touchable.childrenOnly);
        this.popupGroup.addActor(this.detailGroup);
        this.detailGroup.setSize(this.detailGroup.getParent().getWidth(), this.detailGroup.getParent().getHeight());
        this.detailGroup.setOrigin(1);
        this.detailsTable = new Table();
        this.detailsTable.top().left();
        this.detailsTable.setVisible(false);
        this.detailsTable.setSize(this.panelBg.getWidth() - 155.0f, this.panelBg.getHeight() - 205.0f);
        this.detailsTable.setPosition(75.0f, 60.0f);
        this.detailGroup.addActor(this.detailsTable);
        this.screenshotHolder = new Group();
        this.screenshotHolder.setSize(360.0f, 330.0f);
        this.screenshotHolder.setOrigin(1);
        this.detailGroup.addActor(this.screenshotHolder);
        this.screenshotHolder.setPosition(this.popupGroup.getWidth() - 80.0f, this.popupGroup.getHeight() - 140.0f, 18);
        this.detailGroup.setVisible(false);
    }

    private void addListOptions() {
        this.gsdList = GameLoader.getSavedGames();
        for (final GameSaveData gsd : this.gsdList) {
            final String saveFolderPath = gsd.saveFolder;
            Group g = new Group();
            this.table.add(g).height(100.0f).expandX().fillX().padRight(18.0f).spaceBottom(10.0f);
            this.table.layout();
            Button button = new Button(this.buttonStyle);
            g.addActor(button);
            button.setFillParent(true);
            button.layout();
            button.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    LoadGameMenu.this.baseScreen.menuForwardSound();
                    Audio.getInstance().playSound("sounds/menuForward1.ogg", 0.2f);
                    LoadGameMenu.this.showDetailInfo(gsd);
                }
            });
            Group head = new Group();
            head.setTouchable(Touchable.disabled);
            head.setPosition(25.0f, 14.0f);
            head.setScale(0.42f, 0.42f);
            g.addActor(head);
            Image visor = new Image(this.skin.getDrawable("visor"));
            visor.setPosition(32.0f, 72.0f);
            Color visorColor = Color.WHITE;
            try {
                visorColor = Color.valueOf(gsd.playerData.characterSuitColor);
                visor.setColor(visorColor);
            }
            catch (Exception e) {
                visor.setColor(Color.valueOf(Player.suitColorList[0]));
                MoonBase.error("Invalid suit color in gameSave.json (" + gsd.playerData.characterSuitColor + "), using default.");
            }
            head.addActor(visor);
            Image face = new Image();
            try {
                face = new Image(this.skin.getDrawable("face" + gsd.playerData.characterFaceOption));
            }
            catch (GdxRuntimeException e) {
                face = new Image(this.skin.getDrawable("face0"));
                Gdx.app.error("MewnBase", "Invalid character face option in gameSave.json, using default.");
            }
            face.setPosition(58.0f, 30.0f);
            head.addActor(face);
            Image playerHead = new Image(this.skin.getDrawable("player-head-1-side"));
            head.addActor(playerHead);
            Label nameLabel = new Label((CharSequence)gsd.playerName, this.labelStyle);
            nameLabel.setFontScale(0.5f);
            nameLabel.setPosition(120.0f, 30.0f);
            nameLabel.setTouchable(Touchable.disabled);
            g.addActor(nameLabel);
            if (gsd.readOnly) {
                nameLabel.setColor(Color.RED);
                nameLabel.setText(nameLabel.getText() + " (READ ONLY)");
            }
            Mission.MissionTypes t = Mission.MissionTypes.normal;
            try {
                t = Mission.MissionTypes.valueOf(gsd.missionType);
            }
            catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid value for mission type: " + e.getLocalizedMessage());
            }
            String missionDayGoal = "" + gsd.missionDayGoal;
            if (t == Mission.MissionTypes.tutorial) {
                missionDayGoal = missionDayGoal + " - " + Localization.get("newgame_tutorial");
            } else if (t == Mission.MissionTypes.creative) {
                missionDayGoal = missionDayGoal + " - " + Localization.get("newgame_creative");
            }
            Label dayLabel = new Label((CharSequence)Localization.format("dayHud", gsd.currentDayNum, missionDayGoal), this.labelStyle);
            dayLabel.setFontScale(0.4f);
            dayLabel.setColor(Menu.MENU_COLOR);
            dayLabel.setPosition(120.0f, -3.0f);
            dayLabel.setTouchable(Touchable.disabled);
            g.addActor(dayLabel);
            if (gsd.saveDataVersion <= 3) {
                dayLabel.setText(Localization.get("unsupported_version"));
                dayLabel.setColor(Color.RED);
            }
            if (gsd.missionComplete) {
                Group completeGroup = this.createMissionCompleteGroup(false);
                g.addActor(completeGroup);
                completeGroup.setPosition(g.getWidth() - 25.0f, 66.0f, 18);
            }
            String dateModifiedString = gsd.getLastDateModified();
            Label dateModified = new Label((CharSequence)dateModifiedString, this.labelStyle);
            dateModified.setFontScale(0.4f);
            dateModified.setColor(Menu.MENU_COLOR);
            dateModified.getColor().a = 0.35f;
            dateModified.setAlignment(16, 18);
            dateModified.setPosition(g.getWidth() - 25.0f - 35.0f, -4.0f, 20);
            dateModified.setTouchable(Touchable.disabled);
            g.addActor(dateModified);
            Button delete = new Button(this.deleteButtonStyle);
            delete.setSize(24.0f, 24.0f);
            delete.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    LoadGameMenu.this.delete(saveFolderPath);
                }
            });
            if (gsd.readOnly) {
                delete.setTouchable(Touchable.disabled);
                delete.setDisabled(true);
            }
            g.addActor(delete);
            delete.setPosition(g.getWidth() - 25.0f, 18.0f, 20);
            this.table.row();
        }
    }

    private Group createMissionCompleteGroup(boolean centered) {
        Group completeGroup = new Group();
        completeGroup.setTouchable(Touchable.disabled);
        Image completePanel = new Image(this.completePanelPatch);
        completeGroup.addActor(completePanel);
        completePanel.setColor(1.0f, 1.0f, 1.0f, 0.3f);
        Label textLabel = new Label((CharSequence)Localization.get("missionComplete"), this.labelStyle);
        textLabel.setFontScale(0.4f);
        textLabel.setColor(Menu.MENU_COLOR);
        textLabel.setPosition(-15.0f, 0.0f, 16);
        textLabel.setAlignment(16);
        textLabel.setTouchable(Touchable.disabled);
        completeGroup.addActor(textLabel);
        textLabel.layout();
        Image star = new Image(this.skin.getDrawable("equippedFlag"));
        star.setSize(45.0f, 45.0f);
        star.setPosition(-textLabel.getPrefWidth() - 40.0f, 0.0f, 1);
        star.setTouchable(Touchable.disabled);
        completeGroup.addActor(star);
        completePanel.setSize(textLabel.getPrefWidth() + 40.0f, textLabel.getPrefHeight());
        completePanel.setPosition(0.0f, 0.0f, 16);
        if (centered) {
            textLabel.setAlignment(1);
            textLabel.setPosition(14.0f, 0.0f, 1);
            completePanel.setPosition(10.0f, 0.0f, 1);
            star.setPosition(-(textLabel.getPrefWidth() / 2.0f) - 10.0f, 0.0f, 1);
        }
        return completeGroup;
    }

    private void showDetailInfo(final GameSaveData gsd) {
        Object prevSave2;
        FileHandle prevSave2Folder;
        GameSaveData prevSave1;
        this.currentMode = modes.details;
        this.setupStaticAnim();
        this.scroller.setVisible(false);
        this.detailsTable.clearChildren(true);
        this.detailsTable.setVisible(true);
        this.detailGroup.setVisible(true);
        this.detailGroup.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        ArrayList saveDataList = new ArrayList();
        HashMap<Object, Integer> saveDatas = new HashMap<Object, Integer>();
        saveDatas.put(gsd, 0);
        MoonBase.log("\n----------\n");
        FileHandle prevSave1Folder = Gdx.files.local("saves/" + gsd.saveFolder + "/prevSave1");
        if (prevSave1Folder.exists() && (prevSave1 = GameLoader.getGameSaveData(gsd.saveFolder + "/prevSave1")) != null) {
            saveDatas.put(prevSave1, 1);
        }
        if ((prevSave2Folder = Gdx.files.local("saves/" + gsd.saveFolder + "/prevSave2")).exists() && (prevSave2 = GameLoader.getGameSaveData(gsd.saveFolder + "/prevSave2")) != null) {
            saveDatas.put(prevSave2, 2);
        }
        MoonBase.debug("unsorted:");
        for (Object key : saveDatas.keySet()) {
            GameSaveData key0 = (GameSaveData)key;
            MoonBase.debug("Save " + key0.autoSave + "\t\t  day: " + key0.currentDayNum + " \t\t time: " + this.getTimeString(key0) + " \t\t lastUpdated: " + key0.lastUpdated);
        }
        ArrayList sortedKeys = new ArrayList(saveDatas.keySet());
        Collections.sort(sortedKeys);
        MoonBase.debug("sorted:");
        for (GameSaveData key : sortedKeys) {
            MoonBase.debug("Save " + key.autoSave + "\t\t  day: " + key.currentDayNum + " \t\t time: " + this.getTimeString(key) + " \t\t lastUpdated: " + key.lastUpdated);
        }
        GameSaveData latestData = (GameSaveData)sortedKeys.get(0);
        this.setupScreenshot(latestData);
        this.setupTopDetailRow(latestData);
        this.detailsTable.row();
        this.saveRevTable = new Table();
        this.saveRevTable.top();
        this.saveRevTable.padRight(390.0f);
        this.detailsTable.add(this.saveRevTable).fill().expand().colspan(3).top();
        if (sortedKeys.size() > 1) {
            Label prevVersions = new Label((CharSequence)(Localization.get("restorePrevSaveVersion") + ":"), this.labelStyle);
            prevVersions.setColor(Menu.MENU_COLOR);
            prevVersions.getColor().a = 0.36f;
            prevVersions.setFontScale(0.35f);
            this.saveRevTable.add(prevVersions).padLeft(18.0f).spaceBottom(5.0f).padTop(0.0f).expandX().left().height(25.0f);
            this.saveRevTable.row();
        }
        int gIndex = 0;
        for (GameSaveData g : sortedKeys) {
            if (gIndex == 0) {
                ++gIndex;
                continue;
            }
            MoonBase.log("Save " + g.autoSave + "\t\t  day: " + g.currentDayNum + " \t\t time: " + this.getTimeString(g) + " \t\t lastUpdated: " + g.lastUpdated);
            this.addSaveRevRow(g, (Integer)saveDatas.get(g));
            this.saveRevTable.row();
            ++gIndex;
        }
        this.detailsTable.row();
        this.detailsTable.row();
        Table botRowTable = new Table();
        this.detailsTable.add(botRowTable).colspan(3).bottom().expand().height(90.0f).fill();
        this.btnBackToList = new TextButton(Localization.get("back").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnBackToList.getLabel().setFontScale(0.6f);
        this.btnBackToList.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                LoadGameMenu.this.returnToList();
            }
        });
        botRowTable.add(this.btnBackToList).left().width(220.0f).height(90.0f).expandX();
        if (gsd.missionComplete) {
            Group missionComplete = this.createMissionCompleteGroup(true);
            missionComplete.setDebug(true);
            botRowTable.add(missionComplete).right();
        }
        TextButton btnLoad = new TextButton(Localization.get("continue").toUpperCase(), this.baseScreen.textBtnStyle);
        btnLoad.getLabel().setFontScale(0.6f);
        btnLoad.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                LoadGameMenu.this.loadGame(gsd.saveFolder);
            }
        });
        botRowTable.add(btnLoad).width(220.0f).height(90.0f).right().expandX();
        this.detailGroup.addAction(Actions.sequence((Action)Actions.scaleTo(0.85f, 0.85f), (Action)Actions.alpha(0.0f), (Action)Actions.parallel((Action)Actions.sequence((Action)Actions.scaleTo(1.02f, 1.02f, 0.2f, Interpolation.sine), (Action)Actions.scaleTo(1.0f, 1.0f, 0.05f)), (Action)Actions.alpha(1.0f, 0.2f))));
    }

    private Image makeScreenshot(FileHandle screenshotFile, GameSaveData gsd) {
        Image screenshotImage = null;
        if (screenshotFile.exists()) {
            try {
                Texture screenshotTexture = new Texture(screenshotFile);
                screenshotTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                screenshotImage = new Image(screenshotTexture);
                screenshotImage.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                screenshotImage.setSize(340.0f, 310.0f);
                this.screenshotHolder.addActor(screenshotImage);
                screenshotImage.setPosition(10.0f, 10.0f, 12);
            }
            catch (Exception e) {
                MoonBase.error("Problem reading screenshot.png for: " + gsd.saveFolder);
                screenshotImage = new Image(this.skin.getDrawable("no-screenshot"));
                screenshotImage.setSize(340.0f, 310.0f);
                this.screenshotHolder.addActor(screenshotImage);
                screenshotImage.setPosition(10.0f, 10.0f, 12);
            }
        } else {
            screenshotImage = new Image(this.skin.getDrawable("no-screenshot"));
            screenshotImage.setSize(340.0f, 310.0f);
            this.screenshotHolder.addActor(screenshotImage);
            screenshotImage.setPosition(10.0f, 10.0f, 12);
        }
        return screenshotImage;
    }

    private void setupScreenshot(GameSaveData gsd) {
        FileHandle screenshotFile = Gdx.files.local("saves/" + gsd.saveFolder + "/screenshot.png");
        this.primaryScreenshot = this.makeScreenshot(screenshotFile, gsd);
        FileHandle screenshotFile1 = Gdx.files.local("saves/" + gsd.saveFolder + "/prevSave1/screenshot.png");
        this.prev1Screenshot = this.makeScreenshot(screenshotFile1, gsd);
        if (this.prev1Screenshot != null) {
            this.prev1Screenshot.setVisible(false);
        }
        FileHandle screenshotFile2 = Gdx.files.local("saves/" + gsd.saveFolder + "/prevSave2/screenshot.png");
        this.prev2Screenshot = this.makeScreenshot(screenshotFile2, gsd);
        if (this.prev2Screenshot != null) {
            this.prev2Screenshot.setVisible(false);
        }
        this.staticImage = new Image(this.skin.getDrawable("lpad-static1"));
        this.staticImage.setColor(MENU_COLOR);
        this.staticImage.setScaling(Scaling.stretch);
        this.staticImage.setSize(340.0f, 310.0f);
        this.screenshotHolder.addActor(this.staticImage);
        this.staticImage.setPosition(10.0f, 10.0f, 12);
        this.playStatic();
        AdditiveImage grid = new AdditiveImage(this.skin.getDrawable("map-grid"));
        grid.setSize(360.0f, 330.0f);
        grid.setColor(1.0f, 1.0f, 1.0f, 0.2f);
        grid.setPosition(-2.0f, -2.0f, 12);
        this.screenshotHolder.addActor(grid);
        Image frame = new Image(this.np_up);
        frame.setSize(360.0f, 330.0f);
        frame.setPosition(0.0f, 0.0f, 12);
        this.screenshotHolder.addActor(frame);
    }

    private void returnToList() {
        this.currentMode = modes.list;
        this.screenshotHolder.clearChildren();
        this.scroller.setVisible(true);
        this.detailGroup.setVisible(false);
    }

    private void setupTopDetailRow(GameSaveData gsd) {
        Group g = new Group();
        this.detailsTable.add(g).height(120.0f).width(130.0f).align(10);
        Group head = new Group();
        head.setPosition(5.0f, 20.0f);
        head.setScale(0.6f, 0.6f);
        g.addActor(head);
        Image visor = new Image(this.skin.getDrawable("visor"));
        visor.setPosition(32.0f, 68.0f);
        Color visorColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        try {
            visorColor = Color.valueOf(gsd.playerData.characterSuitColor);
            visor.setColor(visorColor);
        }
        catch (Exception e) {
            visor.setColor(Color.valueOf(Player.suitColorList[0]));
            MoonBase.error("Invalid suit color in gameSave.json (" + gsd.playerData.characterSuitColor + "), using default.");
        }
        head.addActor(visor);
        Image face = new Image();
        try {
            face = new Image(this.skin.getDrawable("face" + gsd.playerData.characterFaceOption));
        }
        catch (GdxRuntimeException e) {
            face = new Image(this.skin.getDrawable("face0"));
            MoonBase.error("Invalid character face option in gameSave.json, using default.");
        }
        face.setPosition(58.0f, 30.0f);
        head.addActor(face);
        Image playerHead = new Image(this.skin.getDrawable("player-head-1-side"));
        head.addActor(playerHead);
        Table detailStatsTable = new Table();
        detailStatsTable.top();
        this.detailsTable.add(detailStatsTable).expandX().fillX().padBottom(15.0f);
        detailStatsTable.setTouchable(Touchable.disabled);
        Label nameLabel = new Label((CharSequence)gsd.playerName, this.labelStyle);
        nameLabel.setFontScale(0.65f);
        detailStatsTable.add(nameLabel).expandX().left().spaceBottom(5.0f);
        detailStatsTable.row();
        if (gsd.readOnly) {
            nameLabel.setColor(Color.RED);
            nameLabel.setText(nameLabel.getText() + " (READ ONLY)");
        }
        String missionMeta = "";
        String worldTime = "";
        worldTime = gsd.autoSave ? worldTime + " [" + Localization.get("autosave") + "]" : worldTime + " " + this.getTimeString(gsd);
        missionMeta = missionMeta + Localization.format("dayHud", gsd.currentDayNum, gsd.missionDayGoal) + "  " + worldTime;
        Mission.MissionTypes missionType = null;
        try {
            missionType = Mission.MissionTypes.valueOf(gsd.missionType);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid value for mission type: " + e.getLocalizedMessage());
        }
        String gameMode = "";
        if (missionType == Mission.MissionTypes.tutorial) {
            gameMode = Localization.get("newgame_tutorial");
            missionMeta = missionMeta + "\n" + gameMode;
        } else if (missionType == Mission.MissionTypes.creative) {
            gameMode = Localization.get("newgame_creative");
            missionMeta = missionMeta + "\n" + gameMode;
        }
        String nightMode = "";
        Mission.dayCycleModes mode = Mission.dayCycleModes.defaultDay;
        try {
            mode = Mission.dayCycleModes.valueOf(gsd.dayCycleModes);
        }
        catch (Exception e) {
            MoonBase.error("Issue parsing day cycle mode in " + gsd.saveFolder + " (" + gsd.dayCycleModes + ")");
        }
        if (mode != Mission.dayCycleModes.defaultDay) {
            nightMode = "\n" + Localization.get("worldGen_" + mode.toString());
            missionMeta = missionMeta + nightMode;
        }
        String weatherMode = "";
        Mission.weatherModes wMode = Mission.weatherModes.normal;
        try {
            wMode = Mission.weatherModes.valueOf(gsd.weatherMode);
        }
        catch (Exception e) {
            MoonBase.error("Issue parsing weather mode in " + gsd.saveFolder + " (" + gsd.weatherMode + ")");
        }
        if (wMode != Mission.weatherModes.normal) {
            weatherMode = weatherMode + "\n" + Localization.get("worldGen_weather") + ": " + Localization.get("worldGen_" + (Object)((Object)wMode));
            missionMeta = missionMeta + weatherMode;
        }
        Label missionInfo = new Label((CharSequence)missionMeta, this.labelStyle);
        missionInfo.setFontScale(0.4f);
        missionInfo.setColor(Menu.MENU_COLOR);
        detailStatsTable.add(missionInfo).left().expandX();
        detailStatsTable.row();
        Label lastModifiedLabel = new Label((CharSequence)gsd.getLastDateModified(), this.labelStyle);
        lastModifiedLabel.setFontScale(0.35f);
        lastModifiedLabel.setColor(Menu.MENU_COLOR);
        lastModifiedLabel.getColor().a = 0.3f;
        detailStatsTable.add(lastModifiedLabel).left().expandX().fillY().spaceTop(3.0f);
    }

    private String getTimeString(GameSaveData gsd) {
        String time = "";
        try {
            time = DayCycle.convertToClockString(Mission.dayCycleModes.valueOf(gsd.dayCycleModes), DayCycle.Periods.valueOf(gsd.currentPeriod), gsd.currentPeriodTime);
        }
        catch (Exception e) {
            Gdx.app.error("MewnBase", "LoadGameScreen: Problem formatting time string from save.");
        }
        return time;
    }

    private void addSaveRevRow(final GameSaveData gsd, final int slot) {
        Group g = new Group();
        this.saveRevTable.add(g).fillY().minHeight(51.0f).expandX().fillX().spaceBottom(10.0f).padLeft(0.0f).padLeft(0.0f).top();
        Button button = null;
        button = new Button(this.buttonStyle);
        button.addListener(new ClickListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (slot == 1) {
                    LoadGameMenu.this.prev1Screenshot.setVisible(true);
                } else if (slot == 2) {
                    LoadGameMenu.this.prev2Screenshot.setVisible(true);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                LoadGameMenu.this.prev1Screenshot.setVisible(false);
                LoadGameMenu.this.prev2Screenshot.setVisible(false);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                LoadGameMenu.this.loadOldSaveVersion(gsd.saveFolder, slot);
            }
        });
        g.addActor(button);
        button.setFillParent(true);
        button.layout();
        String saveType = "";
        saveType = gsd.autoSave ? saveType + " [" + Localization.get("autosave") + "]" : saveType + " " + this.getTimeString(gsd);
        Label dayLabel = new Label((CharSequence)(Localization.format("dayHudEndless", gsd.currentDayNum) + "  " + saveType), this.labelStyle);
        dayLabel.setFontScale(0.35f);
        dayLabel.setColor(Menu.MENU_COLOR);
        dayLabel.getColor().a = 0.75f;
        dayLabel.setPosition(20.0f, -8.0f);
        dayLabel.setTouchable(Touchable.disabled);
        g.addActor(dayLabel);
        Label dateStamp = new Label((CharSequence)gsd.getLastDateModified(), this.labelStyle);
        dateStamp.setFontScale(0.28f);
        dateStamp.setAlignment(16);
        dateStamp.setColor(Menu.MENU_COLOR);
        dateStamp.getColor().a = 0.4f;
        dateStamp.setPosition(-17.0f, 0.0f);
        dateStamp.setFillParent(true);
        dateStamp.setTouchable(Touchable.disabled);
        g.addActor(dateStamp);
    }

    private void delete(String saveFolder) {
        this.baseScreen.showMenu(new SaveDeletePopup(this.baseScreen, saveFolder));
    }

    private void loadOldSaveVersion(String saveFolder, int saveSlot) {
        this.baseScreen.showMenu(new LoadOldSaveFilePopup(this, saveFolder, saveSlot));
    }

    @Override
    public void returned() {
        super.returned();
        System.out.println("returned to settings popup");
        this.table.clear();
        this.addListOptions();
    }

    @Override
    protected void finishedShowAnim() {
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.staticImage != null && this.playingStatic) {
            this.updateStaticAnim(delta);
        }
    }

    @Override
    public void back() {
        super.back();
    }

    protected void loadGame(String saveFolder) {
        FileHandle file2;
        String fileText;
        boolean fixRequired = false;
        FileHandle file = Gdx.files.local("saves/" + saveFolder + "/gameSave.json");
        if (file.exists()) {
            Json json = new Json();
            json.setIgnoreDeprecated(true);
            json.setIgnoreUnknownFields(true);
            fileText = file.readString("ISO-8859-1");
            GameSaveData gsd = json.fromJson(GameSaveData.class, fileText);
            boolean bl = fixRequired = gsd.saveDataVersion < 9;
        }
        if ((file2 = Gdx.files.local("saves/" + saveFolder + "/gameSave.data")).exists()) {
            fileText = file2.readString("ISO-8859-1");
            byte[] bytes = Base64.getDecoder().decode(fileText);
            Json json = new Json();
            json.setIgnoreDeprecated(true);
            json.setIgnoreUnknownFields(true);
            fileText = new String(bytes);
            GameSaveData gsd = json.fromJson(GameSaveData.class, fileText);
            fixRequired = gsd.saveDataVersion < 9;
        }
        MoonBase.currentSaveFolder = saveFolder;
        if (fixRequired) {
            this.baseScreen.menuForwardSound();
            Audio.getInstance().playSound("sounds/menuForward_gameStart.ogg", 0.2f);
            this.baseScreen.showMenu(new UpdateSaveWarningScreen(this.baseScreen));
        } else {
            this.baseScreen.menuForwardSound();
            Audio.getInstance().playSound("sounds/menuForward_gameStart.ogg", 0.2f);
            this.nextMenu(new LoadingScreen(this.baseScreen.game, false));
            MoonBase.achievementAdapter.loadedGame();
        }
    }

    private void nextMenu(Screen screen) {
        final Screen finalScreen = screen;
        this.menuGroup.addAction(Actions.sequence((Action)Actions.parallel(), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                LoadGameMenu.this.baseScreen.game.setScreen(finalScreen);
            }
        })));
    }

    @Override
    public void handleInput() {
        if (this.currentMode == modes.list) {
            super.handleInput();
        } else {
            if (PlayerInput.wasPressed(6)) {
                this.returnToList();
            }
            if (PlayerInput.wasPressed(26)) {
                this.baseScreen.game.toggleFullScreen();
            }
            PlayerInput.update();
        }
    }

    private void setupStaticAnim() {
        this.staticSpriteList.clear();
        this.staticSpriteList.add("lpad-static1");
        this.staticSpriteList.add("lpad-static2");
        this.staticSpriteList.add("lpad-static3");
        this.staticSpriteList.add("lpad-static4");
        this.staticSpriteList.add("lpad-static5");
    }

    private void playStatic() {
        this.playingStatic = true;
        this.fpsTimer = 0.0f;
        this.frameIndex = 0;
        this.staticImage.addAction(Actions.sequence((Action)Actions.fadeOut(1.0f / this.fps * (float)this.staticSpriteList.size())));
    }

    private void updateStaticAnim(float delta) {
        this.fpsTimer += delta;
        if (this.fpsTimer > 1.0f / this.fps) {
            this.fpsTimer = 0.0f;
            ++this.frameIndex;
            this.staticImage.setColor(MENU_COLOR);
            if (this.frameIndex >= this.staticSpriteList.size() - 1) {
                this.playingStatic = false;
                this.staticImage.setVisible(false);
            } else {
                this.staticImage.setVisible(true);
                this.staticImage.setDrawable(this.skin.getDrawable(this.staticSpriteList.get(this.frameIndex)));
            }
        }
    }

    private static enum modes {
        list,
        details;

    }
}

