/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.CheckboxRow;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;

public class WorldGenSettings
extends Popup {
    private Mission tempMission;
    private Button.ButtonStyle arrowBtnLeftStyle;
    private Button.ButtonStyle arrowBtnRightStyle;
    private NinePatch divNinePatch;
    Table table;
    private BitmapFont nameFieldFont;
    private TextField seedField;
    private Label nameFieldLabel;
    private Button btnDayLeft;
    private Button btnDayRight;
    private Button btnWaterLeft;
    private Button btnWaterRight;
    private Button btnTechTreeLeft;
    private Button btnTechTreeRight;
    private Button btnWeatherLeft;
    private Button btnWeatherRight;
    private Mission.dayCycleModes[] dayCycleModes = Mission.dayCycleModes.values();
    private Mission.waterModes[] waterModes = Mission.waterModes.values();
    private Mission.weatherModes[] weatherModes = Mission.weatherModes.values();
    private Mission.techTreeModes[] techTreeModes = Mission.techTreeModes.values();
    private int dayCycleIndex = 2;
    private int waterIndex = 0;
    private int weatherIndex = 2;
    private int techIndex = 0;
    private Mission.creatureModes hostileCreatures = Mission.creatureModes.hostile;
    private Label dayLabel;
    private Label waterLabel;
    private Label weatherLabel;
    private Label techLabel;
    private CheckboxRow cbtnTechTree;
    private CheckboxRow cbtnHostileCreatures;

    public WorldGenSettings(BaseScreen baseScreen, Mission tempMission) {
        super(baseScreen);
        int i;
        this.tempMission = tempMission;
        for (i = 0; i < this.dayCycleModes.length; ++i) {
            if (tempMission.dayCycleMode != this.dayCycleModes[i]) continue;
            this.dayCycleIndex = i;
            break;
        }
        for (i = 0; i < this.techTreeModes.length; ++i) {
            if (tempMission.techTreeMode != this.techTreeModes[i]) continue;
            this.techIndex = i;
            break;
        }
        for (i = 0; i < this.waterModes.length; ++i) {
            if (tempMission.waterMode != this.waterModes[i]) continue;
            this.waterIndex = i;
            break;
        }
        for (i = 0; i < this.weatherModes.length; ++i) {
            if (tempMission.weatherMode != this.weatherModes[i]) continue;
            this.weatherIndex = i;
            break;
        }
        this.setup();
        this.show();
        if (this.tempMission.seed != null && this.tempMission.seed != "") {
            this.seedField.setText(this.tempMission.seed);
        }
        this.cycleDayModes(0);
        this.cycleWeatherModes(0);
        boolean techChecked = this.techIndex == 1;
        this.cbtnTechTree.setChecked(techChecked);
        this.cbtnHostileCreatures.setChecked(this.tempMission.creatureMode == Mission.creatureModes.hostile);
    }

    @Override
    public void setup() {
        super.setup();
        this.tintBgAlpha = 0.9f;
        this.setSize(780.0f, 580.0f);
        this.setTitle("worldGen_worldSettings");
        this.nameFieldFont = AssetManagerSingleton.getInstance().get("smallfont1.fnt", BitmapFont.class);
        this.nameFieldFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.arrowBtnLeftStyle = new Button.ButtonStyle();
        this.arrowBtnLeftStyle.up = this.skin.getDrawable("btn-arrow-left");
        this.arrowBtnLeftStyle.over = this.skin.getDrawable("btn-arrow-left-hover");
        this.arrowBtnLeftStyle.down = this.skin.getDrawable("btn-arrow-left-pressed");
        this.arrowBtnLeftStyle.disabled = this.skin.getDrawable("btn-arrow-left-disabled");
        this.arrowBtnRightStyle = new Button.ButtonStyle();
        this.arrowBtnRightStyle.up = this.skin.getDrawable("btn-arrow-right");
        this.arrowBtnRightStyle.over = this.skin.getDrawable("btn-arrow-right-hover");
        this.arrowBtnRightStyle.down = this.skin.getDrawable("btn-arrow-right-pressed");
        this.arrowBtnRightStyle.disabled = this.skin.getDrawable("btn-arrow-right-disabled");
        this.divNinePatch = new NinePatch(this.skin.getRegion("credits-divider"), 10, 10, 0, 0);
        this.addWidgets();
    }

    public void addWidgets() {
        this.table = new Table();
        this.table.center();
        this.table.setSize(this.panelBg.getWidth() - 200.0f, this.panelBg.getHeight() - 175.0f);
        this.table.setPosition((this.panelBg.getWidth() - this.table.getWidth()) / 2.0f, 60.0f);
        this.popupGroup.addActor(this.table);
        NinePatch alt_np_up = new NinePatch(this.skin.getRegion("btn-minor"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_up = new NinePatchDrawable(alt_np_up);
        NinePatch alt_np_active = new NinePatch(this.skin.getRegion("btn-minor-pressed"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_active = new NinePatchDrawable(alt_np_active);
        NinePatch selected_np = new NinePatch(this.skin.getRegion("slider-bg"), 5, 5, 5, 5);
        NinePatchDrawable selected_npd = new NinePatchDrawable(selected_np);
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.background = alt_npd_up;
        textFieldStyle.focusedBackground = alt_npd_active;
        textFieldStyle.font = this.nameFieldFont;
        textFieldStyle.font.getData().setScale(0.5f);
        textFieldStyle.cursor = this.skin.getDrawable("hud-progress-knob");
        textFieldStyle.selection = selected_npd;
        textFieldStyle.fontColor = Menu.MENU_COLOR;
        textFieldStyle.focusedFontColor = Color.WHITE;
        textFieldStyle.messageFontColor = new Color(Menu.MENU_COLOR).sub(0.0f, 0.0f, 0.0f, 0.7f);
        textFieldStyle.messageFont = this.font;
        Table seedTable = new Table();
        seedTable.left();
        Label seedLabel = new Label((CharSequence)Localization.get("worldGen_seed"), this.labelStyle);
        seedLabel.setFontScale(0.45f);
        seedLabel.setAlignment(8);
        seedTable.add(seedLabel).fillX().spaceRight(15.0f);
        Stack s = new Stack();
        this.seedField = new TextField("", textFieldStyle);
        this.seedField.setMaxLength(25);
        this.seedField.setMessageText(Localization.get("worldGen_blankSeed"));
        this.seedField.setAlignment(1);
        this.seedField.setTextFieldListener(new TextField.TextFieldListener(){

            @Override
            public void keyTyped(TextField textField, char c) {
            }
        });
        s.add(this.seedField);
        s.pack();
        seedTable.add(s).fillX().height(70.0f).expandX();
        this.table.add(seedTable).spaceBottom(20.0f).colspan(3).fillX();
        this.table.row();
        this.btnDayLeft = new Button(this.arrowBtnLeftStyle);
        this.btnDayLeft.setOrigin(1);
        this.btnDayLeft.setScaleX(-1.0f);
        this.table.add(this.btnDayLeft);
        this.btnDayLeft.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                WorldGenSettings.this.cycleDayModes(-1);
            }
        });
        this.dayLabel = new Label((CharSequence)(Localization.get("worldGen_dayNight") + ": " + Localization.get("worldGen_" + (Object)((Object)this.dayCycleModes[this.dayCycleIndex]))), this.labelStyle);
        this.dayLabel.setFontScale(0.45f);
        this.table.add(this.dayLabel).width(400.0f).height(45.0f);
        this.btnDayRight = new Button(this.arrowBtnRightStyle);
        this.btnDayRight.setOrigin(1);
        this.table.add(this.btnDayRight);
        this.btnDayRight.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                WorldGenSettings.this.cycleDayModes(1);
            }
        });
        this.table.row();
        this.addDivider();
        this.table.row();
        this.btnWeatherLeft = new Button(this.arrowBtnLeftStyle);
        this.btnWeatherLeft.setOrigin(1);
        this.btnWeatherLeft.setScaleX(-1.0f);
        this.table.add(this.btnWeatherLeft);
        this.btnWeatherLeft.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                WorldGenSettings.this.cycleWeatherModes(-1);
            }
        });
        this.weatherLabel = new Label((CharSequence)(Localization.get("worldGen_weather") + ": " + Localization.get("worldGen_" + (Object)((Object)this.weatherModes[this.weatherIndex]))), this.labelStyle);
        this.weatherLabel.setFontScale(0.45f);
        this.table.add(this.weatherLabel).width(400.0f).height(45.0f);
        this.btnWeatherRight = new Button(this.arrowBtnRightStyle);
        this.btnWeatherRight.setOrigin(1);
        this.table.add(this.btnWeatherRight);
        this.btnWeatherRight.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                WorldGenSettings.this.cycleWeatherModes(1);
            }
        });
        this.table.row();
        this.addDivider();
        this.table.row();
        this.cbtnTechTree = new CheckboxRow(this, Localization.get("worldGen_techTree") + ": " + Localization.get("worldGen_" + (Object)((Object)this.techTreeModes[1])), false);
        this.cbtnTechTree.label.setFontScale(0.45f);
        this.cbtnTechTree.label.setWrap(true);
        this.table.add(this.cbtnTechTree.table).fillX().expandX().space(10.0f).colspan(3);
        this.cbtnTechTree.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                WorldGenSettings.this.baseScreen.menuForwardSound();
                WorldGenSettings.this.cbtnTechTree.setChecked(!((WorldGenSettings)WorldGenSettings.this).cbtnTechTree.checked);
                WorldGenSettings.this.techIndex = ((WorldGenSettings)WorldGenSettings.this).cbtnTechTree.checked ? 1 : 0;
            }
        });
        this.table.row();
        this.addDivider();
        this.table.row();
        this.cbtnHostileCreatures = new CheckboxRow(this, Localization.get("worldGen_hostileCreatures"), true);
        this.cbtnHostileCreatures.label.setFontScale(0.45f);
        this.cbtnHostileCreatures.label.setWrap(true);
        this.table.add(this.cbtnHostileCreatures.table).fillX().expandX().space(10.0f).colspan(3);
        this.cbtnHostileCreatures.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                WorldGenSettings.this.baseScreen.menuForwardSound();
                WorldGenSettings.this.cbtnHostileCreatures.setChecked(!((WorldGenSettings)WorldGenSettings.this).cbtnHostileCreatures.checked);
                WorldGenSettings.this.hostileCreatures = ((WorldGenSettings)WorldGenSettings.this).cbtnHostileCreatures.checked ? Mission.creatureModes.hostile : Mission.creatureModes.passive;
            }
        });
    }

    private void addDivider() {
        Image horz = new Image(this.divNinePatch);
        horz.setColor(1.0f, 1.0f, 1.0f, 0.3f);
        this.table.add(horz).fillX().height(5.0f).space(10.0f).colspan(3);
    }

    @Override
    public void back() {
        this.tempMission.waterMode = this.waterModes[this.waterIndex];
        this.tempMission.dayCycleMode = this.dayCycleModes[this.dayCycleIndex];
        this.tempMission.techTreeMode = this.techTreeModes[this.techIndex];
        this.tempMission.weatherMode = this.weatherModes[this.weatherIndex];
        this.tempMission.creatureMode = this.hostileCreatures;
        this.tempMission.seed = this.seedField.getText();
        this.baseScreen.menuBackSound();
        super.back();
    }

    private void cycleDayModes(int dir) {
        this.dayCycleIndex += dir;
        this.btnDayLeft.setDisabled(false);
        this.btnDayRight.setDisabled(false);
        if (this.dayCycleIndex >= this.dayCycleModes.length - 1) {
            this.dayCycleIndex = this.dayCycleModes.length - 1;
            this.btnDayRight.setDisabled(true);
        }
        if (this.dayCycleIndex <= 0) {
            this.dayCycleIndex = 0;
            this.btnDayLeft.setDisabled(true);
        }
        this.dayLabel.setText(Localization.get("worldGen_dayNight") + ": " + Localization.get("worldGen_" + (Object)((Object)this.dayCycleModes[this.dayCycleIndex])));
    }

    private void cycleWaterModes(int dir) {
        this.waterIndex += dir;
        this.btnWaterLeft.setDisabled(false);
        this.btnWaterRight.setDisabled(false);
        if (this.waterIndex >= this.waterModes.length - 1) {
            this.waterIndex = this.waterModes.length - 1;
            this.btnWaterRight.setDisabled(true);
        }
        if (this.waterIndex <= 0) {
            this.waterIndex = 0;
            this.btnWaterLeft.setDisabled(true);
        }
        this.waterLabel.setText(Localization.get("worldGen_water") + ": " + Localization.get("worldGen_" + (Object)((Object)this.waterModes[this.waterIndex])));
    }

    private void cycleWeatherModes(int dir) {
        this.weatherIndex += dir;
        this.btnWeatherLeft.setDisabled(false);
        this.btnWeatherRight.setDisabled(false);
        if (this.weatherIndex >= this.weatherModes.length - 1) {
            this.weatherIndex = this.weatherModes.length - 1;
            this.btnWeatherRight.setDisabled(true);
        }
        if (this.weatherIndex <= 0) {
            this.weatherIndex = 0;
            this.btnWeatherLeft.setDisabled(true);
        }
        this.weatherLabel.setText(Localization.get("worldGen_weather") + ": " + Localization.get("worldGen_" + (Object)((Object)this.weatherModes[this.weatherIndex])));
    }

    private void cycleTechTreeModes(int dir) {
        this.techIndex += dir;
        this.btnTechTreeLeft.setDisabled(false);
        this.btnTechTreeRight.setDisabled(false);
        if (this.techIndex >= this.techTreeModes.length - 1) {
            this.techIndex = this.techTreeModes.length - 1;
            this.btnTechTreeRight.setDisabled(true);
        }
        if (this.techIndex <= 0) {
            this.techIndex = 0;
            this.btnTechTreeLeft.setDisabled(true);
        }
        this.techLabel.setText(Localization.get("worldGen_techTree") + ":\n" + Localization.get("worldGen_" + (Object)((Object)this.techTreeModes[this.techIndex])));
    }
}

