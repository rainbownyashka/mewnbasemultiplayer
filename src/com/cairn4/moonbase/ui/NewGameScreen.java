/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.CreatePlayerScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.WorldGenSettings;

public class NewGameScreen
extends Menu {
    Label headingLabel;
    Label missionLabel;
    TextButton btnTutorial;
    TextButton btnNormal;
    TextButton btnEndless;
    Table table;
    TextureAtlas frontendAtlas;
    Button.ButtonStyle buttonStyle;
    TextButton btnBack;
    TextButton btnWorldSettings;
    protected Mission tempMission;
    private Mission.dayCycleModes dayCycleMode = Mission.dayCycleModes.defaultDay;
    private Mission.waterModes waterMode = Mission.waterModes.defaultWater;
    private Mission.techTreeModes techTreeMode = Mission.techTreeModes.defaultTech;
    private Mission.weatherModes weatherMode = Mission.weatherModes.normal;
    private Mission.creatureModes creatureMode = Mission.creatureModes.hostile;
    private String seed;

    public NewGameScreen(BaseScreen baseScreen) {
        super(baseScreen);
        this.tempMission = new Mission();
        this.tempMission.dayCycleMode = this.dayCycleMode;
        this.tempMission.waterMode = this.waterMode;
        this.tempMission.techTreeMode = this.techTreeMode;
        this.tempMission.weatherMode = this.weatherMode;
        this.tempMission.creatureMode = this.creatureMode;
        int pad = 45;
        this.frontendAtlas = AssetManagerSingleton.getInstance().get("frontend.atlas", TextureAtlas.class);
        this.skin.addRegions(this.frontendAtlas);
        NinePatch np_up = new NinePatch(this.skin.getRegion("btnpanel"), pad, pad, pad, pad);
        NinePatchDrawable npd_up = new NinePatchDrawable(np_up);
        NinePatch np_active = new NinePatch(this.skin.getRegion("btnpanel-active"), pad, pad, pad, pad);
        NinePatchDrawable npd_active = new NinePatchDrawable(np_active);
        NinePatch np_down = new NinePatch(this.skin.getRegion("btnpanel-pressed"), pad, pad, pad, pad);
        NinePatchDrawable npd_down = new NinePatchDrawable(np_down);
        this.buttonStyle = new Button.ButtonStyle();
        this.buttonStyle.up = npd_up;
        this.buttonStyle.over = npd_active;
        this.buttonStyle.down = npd_down;
        this.setup();
        this.show();
    }

    @Override
    public void setup() {
        this.headingLabel = new Label((CharSequence)Localization.get("newgame_title"), this.headingStyle);
        this.headingLabel.setWidth(MoonBase.SCREEN_WIDTH);
        this.headingLabel.setAlignment(1);
        this.headingLabel.setFontScale(0.9f);
        this.headingLabel.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2 + 258, 1);
        this.headingLabel.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.menuGroup.addActor(this.headingLabel);
        this.table = new Table();
        this.table.setSize(840.0f, 500.0f);
        this.table.center();
        this.table.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2, 1);
        this.menuGroup.addActor(this.table);
        this.addButton(0, Localization.get("newgame_tutorial"), Localization.get("newgame_tutorial_desc"), "newgame-tutorial", Color.valueOf("794cc3"), new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                NewGameScreen.this.setupMission(Mission.MissionTypes.tutorial);
                NewGameScreen.this.nextMenu(Mission.MissionTypes.tutorial);
            }
        });
        this.table.row();
        this.addButton(1, Localization.get("newgame_normal"), Localization.get("newgame_normal_desc"), "newgame-mission", Menu.MENU_COLOR, new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                NewGameScreen.this.setupMission(Mission.MissionTypes.normal);
                NewGameScreen.this.nextMenu(Mission.MissionTypes.normal);
            }
        });
        this.table.row();
        this.addButton(2, Localization.get("newgame_creative"), Localization.get("newgame_creative_desc"), "newgame-creative", Color.valueOf("45bbff"), new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                NewGameScreen.this.setupMission(Mission.MissionTypes.creative);
                NewGameScreen.this.nextMenu(Mission.MissionTypes.creative);
            }
        });
        this.btnWorldSettings = new TextButton(Localization.get("worldGen_worldSettings"), this.baseScreen.altTextBtnStyle);
        final BaseScreen bs = this.baseScreen;
        this.btnWorldSettings.getLabel().setFontScale(0.38f);
        this.btnWorldSettings.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                bs.showMenu(new WorldGenSettings(bs, NewGameScreen.this.tempMission));
            }
        });
        this.btnWorldSettings.setSize(270.0f, 64.0f);
        this.menuGroup.addActor(this.btnWorldSettings);
        this.btnWorldSettings.setPosition(MoonBase.SCREEN_WIDTH / 2 + 408, 55.0f, 20);
        this.btnBack = new TextButton(Localization.get("back").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnBack.setSize(200.0f, 91.0f);
        this.btnBack.getLabel().setFontScale(0.6f);
        this.btnBack.setPosition(MoonBase.SCREEN_WIDTH / 2, 40.0f, 4);
        this.menuGroup.addActor(this.btnBack);
        this.btnBack.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                NewGameScreen.this.baseScreen.menuBackSound();
                NewGameScreen.this.back();
            }
        });
        this.btnBack.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        this.btnBack.addAction(Actions.sequence((Action)Actions.fadeIn(0.5f)));
    }

    @Override
    public void show() {
        super.show();
    }

    private void addButton(int i, String title, String desc, String icon, Color color, ClickListener clickListener) {
        Group g = new Group();
        Button button = new Button(this.buttonStyle);
        g.addActor(button);
        button.setFillParent(true);
        button.layout();
        button.addListener(clickListener);
        Image modeIcon = new Image(this.skin.getDrawable(icon));
        modeIcon.setPosition(22.0f, 58.0f, 8);
        g.addActor(modeIcon);
        Label nameLabel = new Label((CharSequence)title, this.headingStyle);
        nameLabel.setFontScale(0.44f);
        nameLabel.setPosition(135.0f, 31.0f);
        nameLabel.setTouchable(Touchable.disabled);
        g.addActor(nameLabel);
        Label dayLabel = new Label((CharSequence)desc, this.labelStyle);
        dayLabel.setFontScale(0.4f);
        dayLabel.setColor(color);
        dayLabel.setPosition(135.0f, 8.0f);
        dayLabel.setTouchable(Touchable.disabled);
        g.addActor(dayLabel);
        this.table.add(g).height(120.0f).expandX().fillX().padRight(18.0f).spaceBottom(10.0f);
        float delay = 0.18f;
        g.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.moveBy(0.0f, -12.0f), (Action)Actions.delay((float)i * delay), (Action)Actions.parallel((Action)Actions.fadeIn(0.25f), (Action)Actions.moveBy(0.0f, 12.0f, 0.35f, Interpolation.sineOut))));
    }

    private void nextMenu(Mission.MissionTypes type) {
        this.baseScreen.menuForwardSound();
        this.font.getData().setScale(1.0f);
        this.baseScreen.showMenu(new CreatePlayerScreen(this.baseScreen));
    }

    private void nextMenu(Screen screen) {
        this.baseScreen.menuForwardSound();
        this.font.getData().setScale(1.0f);
        final Screen finalScreen = screen;
        this.menuGroup.addAction(Actions.sequence((Action)Actions.parallel(), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                NewGameScreen.this.baseScreen.game.setScreen(finalScreen);
            }
        })));
    }

    private void setupMission(Mission.MissionTypes type) {
        Mission m = new Mission(type);
        m.waterMode = this.waterMode;
        m.techTreeMode = this.techTreeMode;
        m.dayCycleMode = this.dayCycleMode;
        m.weatherMode = this.weatherMode;
        m.creatureMode = this.creatureMode;
        if (this.seed == null) {
            this.seed = "";
        }
        m.seed = this.seed;
        if (type == Mission.MissionTypes.creative) {
            MoonBase.GOD_MODE = true;
            m.techTreeMode = Mission.techTreeModes.allTechUnlocked;
        } else {
            MoonBase.GOD_MODE = false;
        }
        this.baseScreen.game.setMission(m);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.handleInput();
    }

    @Override
    public void back() {
        super.back();
    }

    @Override
    public void returned() {
        this.dayCycleMode = this.tempMission.dayCycleMode;
        this.waterMode = this.tempMission.waterMode;
        this.techTreeMode = this.tempMission.techTreeMode;
        this.weatherMode = this.tempMission.weatherMode;
        this.creatureMode = this.tempMission.creatureMode;
        this.seed = this.tempMission.seed;
        String log = "NewGameScreen:returned with settings:\n";
        log = log + " -- Creature mode: " + (Object)((Object)this.creatureMode) + "\n";
        log = log + " -- Day cycle: " + (Object)((Object)this.dayCycleMode) + "\n";
        log = log + " -- water mode: " + (Object)((Object)this.waterMode) + "\n";
        log = log + " -- Tech tree mode: " + (Object)((Object)this.techTreeMode) + "\n";
        log = log + " -- Weather mode: " + (Object)((Object)this.weatherMode);
        MoonBase.log(log);
        this.show();
        this.menuGroup.addAction(Actions.sequence((Action)Actions.fadeIn(0.3f)));
    }
}

