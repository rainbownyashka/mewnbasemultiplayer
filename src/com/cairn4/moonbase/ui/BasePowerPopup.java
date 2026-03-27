/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cairn4.moonbase.BaseGroup;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.WindTurbine;
import com.cairn4.moonbase.tiles.behaviors.BaseResourceStorageBehavior;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import java.text.DecimalFormat;

public class BasePowerPopup
extends Popup {
    BaseModule baseModule;
    Group genGroup;
    Group useGroup;
    Group batGroup;
    Group leftFlowGroup;
    Group rightFlowGroup;
    Image flowLeft;
    Image flowRight;
    Label genLabel;
    Label genValueLabel;
    Label useLabel;
    Label useValueLabel;
    Label batLabel;
    Label batValueLabel;
    Label problemLabel;
    private DecimalFormat df = new DecimalFormat("0");
    private DecimalFormat df_tenth = new DecimalFormat("0.0");
    private ProgressBar.ProgressBarStyle progressBarStyle;
    private ProgressBar batteryProgressBar;
    private Label baseStat;

    public BasePowerPopup(GameScreen gameScreen, BaseModule baseModule) {
        super(gameScreen);
        this.baseModule = baseModule;
        this.setup();
        this.show();
    }

    @Override
    public void setup() {
        super.setup();
        this.setSize(800.0f, 460.0f);
        this.setTitle("power_popup_title");
        NinePatch numBox = new NinePatch(this.baseScreen.skin.getRegion("textbox-mini"), 15, 15, 15, 15);
        Image baseModuleIcon = new Image(this.skin.getDrawable(ItemFactory.getItemSprite(this.baseModule.builderId + "-builder")));
        Label baseNameLabel = new Label((CharSequence)this.baseModule.getMapName(), this.labelStyle);
        baseNameLabel.setFontScale(0.45f);
        baseNameLabel.setAlignment(1, 1);
        this.baseStat = new Label((CharSequence)"9999/9999", this.labelStyle);
        this.baseStat.setFontScale(0.35f);
        this.baseStat.setColor(Menu.MENU_COLOR);
        this.baseStat.setAlignment(1, 1);
        this.problemLabel = new Label((CharSequence)Localization.format("turbineObstructions", 1), this.labelStyle);
        this.problemLabel.setFontScale(0.35f);
        this.problemLabel.setAlignment(1, 1);
        this.problemLabel.setColor(Vars.WARNING_RED);
        Image baseInfoBg = new Image(this.skin.getDrawable("powerpopup-grid"));
        baseInfoBg.setColor(1.0f, 1.0f, 0.0f, 0.3f);
        baseInfoBg.setSize(293.0f, 342.0f);
        baseInfoBg.setPosition(this.panelBg.getWidth() / 2.0f - 205.0f, this.panelBg.getHeight() / 2.0f - 30.0f, 1);
        this.popupGroup.addActor(baseInfoBg);
        Table baseInfoTable = new Table();
        baseInfoTable.center();
        baseInfoTable.setWidth(255.0f);
        baseInfoTable.add(baseModuleIcon).width(170.0f).height(170.0f).fillX().padBottom(10.0f);
        baseInfoTable.row();
        baseInfoTable.add(baseNameLabel).spaceBottom(5.0f);
        baseInfoTable.row();
        baseInfoTable.add(this.baseStat);
        baseInfoTable.row();
        baseInfoTable.add(this.problemLabel);
        baseInfoTable.setPosition(this.panelBg.getWidth() / 2.0f - 205.0f, this.panelBg.getHeight() / 2.0f - 40.0f, 1);
        this.popupGroup.addActor(baseInfoTable);
        Image divider = new Image(this.skin.getDrawable("powerpopup-vert-divider"));
        this.popupGroup.addActor(divider);
        divider.setPosition(this.panelBg.getWidth() / 2.0f - 60.0f, this.panelBg.getHeight() / 2.0f - 45.0f, 1);
        this.progressBarStyle = new ProgressBar.ProgressBarStyle();
        this.progressBarStyle.knobAfter = this.skin.getDrawable("powerpopup-batterybar-empty");
        this.progressBarStyle.knobBefore = this.skin.getDrawable("powerpopup-batterybar-filled");
        this.progressBarStyle.knob = this.skin.getDrawable("powerpopup-batterybar-knob");
        Label baseTotalsLabel = new Label((CharSequence)Localization.get("power_basetotals"), this.labelStyle);
        baseTotalsLabel.setFontScale(0.4f);
        baseTotalsLabel.setAlignment(1, 1);
        this.genGroup = new Group();
        this.genGroup.setSize(170.0f, 100.0f);
        this.genLabel = new Label((CharSequence)Localization.get("power_gen"), this.labelStyle);
        this.genLabel.setFontScale(0.4f);
        this.genLabel.setAlignment(4, 4);
        this.genLabel.setColor(MENU_COLOR);
        this.genLabel.setWidth(170.0f);
        this.genLabel.setPosition(0.0f, 70.0f);
        this.genGroup.addActor(this.genLabel);
        Stack genStack = new Stack();
        Image genBox = new Image(numBox);
        genStack.add(genBox);
        this.genValueLabel = new Label((CharSequence)"9999", this.labelStyle);
        this.genValueLabel.setFontScale(0.6f);
        this.genValueLabel.setAlignment(1);
        this.genValueLabel.setColor(Color.WHITE);
        genStack.add(this.genValueLabel);
        this.genGroup.addActor(genStack);
        genStack.setSize(170.0f, 70.0f);
        this.useGroup = new Group();
        this.useGroup.setSize(170.0f, 100.0f);
        this.genLabel = new Label((CharSequence)Localization.get("power_use"), this.labelStyle);
        this.genLabel.setFontScale(0.4f);
        this.genLabel.setAlignment(4, 4);
        this.genLabel.setColor(MENU_COLOR);
        this.genLabel.setWidth(170.0f);
        this.genLabel.setPosition(0.0f, 70.0f);
        this.useGroup.addActor(this.genLabel);
        Stack useStack = new Stack();
        Image useBox = new Image(numBox);
        useStack.add(useBox);
        this.useValueLabel = new Label((CharSequence)"9999", this.labelStyle);
        this.useValueLabel.setFontScale(0.6f);
        this.useValueLabel.setAlignment(1);
        this.useValueLabel.setColor(Color.WHITE);
        useStack.add(this.useValueLabel);
        this.useGroup.addActor(useStack);
        useStack.setSize(170.0f, 70.0f);
        this.batGroup = new Group();
        this.batGroup.setSize(350.0f, 100.0f);
        this.genLabel = new Label((CharSequence)Localization.get("power_battery"), this.labelStyle);
        this.genLabel.setFontScale(0.4f);
        this.genLabel.setAlignment(4, 4);
        this.genLabel.setColor(MENU_COLOR);
        this.genLabel.setWidth(350.0f);
        this.genLabel.setPosition(0.0f, 70.0f);
        this.batGroup.addActor(this.genLabel);
        Stack batStack = new Stack();
        this.batteryProgressBar = new ProgressBar(0.0f, 1.0f, 0.01f, false, this.progressBarStyle);
        this.batteryProgressBar.setColor(1.0f, 1.0f, 1.0f, 0.9f);
        this.batteryProgressBar.setValue(0.5f);
        this.batteryProgressBar.setSize(350.0f, 70.0f);
        this.batGroup.addActor(this.batteryProgressBar);
        Image batBox = new Image(numBox);
        batStack.add(batBox);
        this.batValueLabel = new Label((CharSequence)"9999", this.labelStyle);
        this.batValueLabel.setFontScale(0.6f);
        this.batValueLabel.setAlignment(1);
        this.batValueLabel.setColor(Color.WHITE);
        batStack.add(this.batValueLabel);
        this.batGroup.addActor(batStack);
        batStack.setSize(350.0f, 70.0f);
        Table baseTotalTable = new Table();
        baseTotalTable.add(baseTotalsLabel).colspan(2);
        baseTotalTable.row();
        baseTotalTable.add(this.genGroup).space(10.0f);
        baseTotalTable.add(this.useGroup).space(10.0f);
        baseTotalTable.row().colspan(2);
        baseTotalTable.add(this.batGroup).space(10.0f);
        this.popupGroup.addActor(baseTotalTable);
        baseTotalTable.setPosition(this.panelBg.getWidth() / 2.0f + 148.0f, this.panelBg.getHeight() / 2.0f - 30.0f, 1);
    }

    @Override
    public void update(float delta) {
        if (this.baseModule != null) {
            Behavior behavior = this.baseModule.findBehavior(BaseResourceStorageBehavior.class);
            if (behavior != null) {
                BaseResourceStorageBehavior batteryStorageBehavior;
                if (behavior instanceof BaseResourceStorageBehavior && (batteryStorageBehavior = (BaseResourceStorageBehavior)behavior) != null) {
                    String batAmount = this.df.format(batteryStorageBehavior.amount);
                    String batCapacity = this.df.format(batteryStorageBehavior.capacity);
                    this.baseStat.setText(batAmount + "/" + batCapacity);
                }
                this.problemLabel.setVisible(false);
            } else {
                String powerGen = this.df_tenth.format(this.baseModule.getPowerGenRate());
                String maxPowerGen = this.df_tenth.format(this.baseModule.getMaxPowerGen());
                this.baseStat.setText(Localization.get("power_gen") + ": " + powerGen + "/" + maxPowerGen);
                if (this.baseModule instanceof WindTurbine) {
                    int obstructions = ((WindTurbine)this.baseModule).getNumObstructions();
                    if (obstructions > 0) {
                        this.problemLabel.setVisible(true);
                        this.problemLabel.setText(Localization.format("turbineObstructions", obstructions));
                    } else {
                        this.problemLabel.setVisible(false);
                    }
                } else {
                    this.problemLabel.setVisible(false);
                }
            }
            BaseGroup baseGroup = this.baseModule.getBaseGroup();
            float gen = baseGroup.getTotalPowerGen() / delta;
            float use = baseGroup.getTotalPowerDraw() / delta;
            this.genValueLabel.setText("" + this.df_tenth.format(gen));
            if (baseGroup.getTotalPowerGen() == 0.0f) {
                this.genValueLabel.setColor(Vars.WARNING_RED);
            } else {
                this.genValueLabel.setColor(Color.WHITE);
            }
            this.useValueLabel.setText("" + this.df_tenth.format(use));
            if (baseGroup.getTotalPowerDraw() > baseGroup.getTotalPowerGen()) {
                this.useValueLabel.setColor(Vars.WARNING_RED);
            } else {
                this.useValueLabel.setColor(Color.WHITE);
            }
            this.batValueLabel.setText(this.df.format(baseGroup.getTotalPowerStored()) + "/" + this.df.format(baseGroup.getTotalMaxPowerStored()));
            this.batteryProgressBar.setValue(baseGroup.getTotalPowerStored() / baseGroup.getTotalMaxPowerStored());
        } else {
            this.genValueLabel.setText("---");
            this.useValueLabel.setText("---");
            this.batValueLabel.setText("---");
        }
    }
}

