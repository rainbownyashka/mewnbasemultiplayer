/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.cairn4.moonbase.BaseGroup;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.techtree.TechManager;
import com.cairn4.moonbase.techtree.TechUpgrade;
import com.cairn4.moonbase.ui.BaseStatResourceBar;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;

public class BaseStatsInfo
implements Telegraph {
    private Hud hud;
    private Group parentGroup;
    public Group group;
    Image bgPanel;
    Label heading;
    Label numPartsLabel;
    Table table;
    Label oxygenLabel;
    Label powerLabel;
    Label waterLabel;
    Image oxygenIcon;
    Image powerIcon;
    Image waterIcon;
    Label debugLabel;
    NinePatch progressBarBgPatch;
    NinePatch progressBarPatch;
    BaseStatResourceBar oxygenStats;
    BaseStatResourceBar powerStats;
    BaseStatResourceBar waterStats;
    BaseStatResourceBar fuelStats;
    private Vector2 normalPosition;
    public boolean visible = false;
    private boolean fuelVisible = false;
    private boolean waterVisible = false;
    private BaseGroup baseGroup;
    TechUpgrade rocketTech;
    TechUpgrade waterTech;
    private final VerticalGroup verticalGroup;

    public BaseStatsInfo(Hud hud, Group parentGroup) {
        this.hud = hud;
        this.parentGroup = parentGroup;
        this.progressBarBgPatch = new NinePatch(hud.baseScreen.skin.getRegion("hud/basestats-bar-bg"), 4, 4, 4, 4);
        this.progressBarPatch = new NinePatch(hud.baseScreen.skin.getRegion("hud/basestats-bar"), 3, 3, 3, 3);
        this.group = new Group();
        this.parentGroup.addActor(this.group);
        this.group.setVisible(false);
        NinePatch panelPatch = new NinePatch(hud.baseScreen.skin.getRegion("hud/basestats-panel"), 25, 25, 25, 25);
        this.bgPanel = new Image(panelPatch);
        this.bgPanel.getColor().a = 0.6f;
        this.group.addActor(this.bgPanel);
        this.table = new Table();
        this.table.top().left();
        this.group.addActor(this.table);
        this.table.setPosition(0.0f, 0.0f, 10);
        this.oxygenStats = new BaseStatResourceBar(hud, this.group, this, BaseResources.oxygen);
        this.oxygenStats.setAmount(50.0f, 102.5f, false);
        this.powerStats = new BaseStatResourceBar(hud, this.group, this, BaseResources.power);
        this.powerStats.setAmount(102.5f, 102.5f, false);
        this.waterStats = new BaseStatResourceBar(hud, this.group, this, BaseResources.water);
        this.waterStats.setAmount(10.0f, 102.5f, false);
        this.fuelStats = new BaseStatResourceBar(hud, this.group, this, BaseResources.fuel);
        this.fuelStats.setAmount(10.0f, 102.5f, false);
        this.heading = new Label((CharSequence)Localization.get("baseStats"), hud.baseScreen.labelStyle);
        this.heading.setColor(Menu.MENU_COLOR);
        this.heading.setFontScale(0.4f);
        this.heading.setAlignment(8, 8);
        this.table.add(this.heading).fillX().expandX().left().padLeft(5.0f).spaceBottom(0.0f);
        this.table.row();
        this.numPartsLabel = new Label((CharSequence)Localization.format("baseStatus_numParts", 0), hud.baseScreen.labelStyle);
        this.numPartsLabel.setFontScale(0.3f);
        this.numPartsLabel.setColor(Menu.MENU_COLOR);
        this.numPartsLabel.getColor().a = 0.5f;
        this.table.add(this.numPartsLabel).fillX().expandX().left().padLeft(5.0f).spaceBottom(0.0f);
        this.verticalGroup = new VerticalGroup();
        this.verticalGroup.addActor(this.oxygenStats.group);
        this.verticalGroup.addActor(this.powerStats.group);
        this.verticalGroup.addActor(this.waterStats.group);
        this.verticalGroup.addActor(this.fuelStats.group);
        this.table.row();
        this.table.add(this.verticalGroup).expand().left();
        this.table.layout();
        this.bgPanel.setPosition(-15.0f, -15.0f, 10);
        this.updatePanelSize();
        this.group.setPosition(30.0f, MoonBase.SCREEN_HEIGHT / 2 + 94, 10);
        this.normalPosition = new Vector2(this.group.getX(), this.group.getY());
        MessageManager.getInstance().addListener(this, 50);
    }

    private void updatePanelSize() {
        this.bgPanel.setSize(196.0f, this.table.getPrefHeight() + 30.0f);
        this.bgPanel.setPosition(-15.0f, 15.0f, 10);
    }

    public void update(BaseGroup baseGroup) {
        this.baseGroup = baseGroup;
        boolean depletingOxygen = baseGroup.getTotalOxygenGen() < baseGroup.getTotalOxygenDraw();
        boolean depletingPower = baseGroup.getTotalPowerGen() < baseGroup.getTotalPowerDraw();
        boolean depletingFuel = baseGroup.getTotalFuelGen() < baseGroup.getTotalFuelDraw();
        this.numPartsLabel.setText(Localization.format("baseStatus_numParts", baseGroup.getNumBaseParts()));
        this.oxygenStats.setAmount(baseGroup.getTotalOxygenStored(), baseGroup.getTotalMaxOxygenStored(), depletingOxygen);
        this.powerStats.setAmount(baseGroup.getTotalPowerStored(), baseGroup.getTotalMaxPowerStored(), depletingPower);
        this.waterStats.setAmount(baseGroup.getTotalWaterStored(), baseGroup.getTotalMaxWaterStored(), false);
        this.fuelStats.setAmount(baseGroup.getTotalFuelStored(), baseGroup.getTotalMaxFuelStored(), depletingFuel);
        this.oxygenStats.updateTooltip();
        this.powerStats.updateTooltip();
        this.waterStats.updateTooltip();
        this.fuelStats.updateTooltip();
    }

    public void setVisible(TechManager techManager, boolean on) {
        if (this.rocketTech == null) {
            this.rocketTech = techManager.getTech("rocketry");
        }
        if (this.waterTech == null) {
            this.waterTech = techManager.getTech("food2");
        }
        if (this.waterTech != null) {
            if (this.waterTech.unlocked) {
                if (!this.waterVisible && on) {
                    this.waterStats.group.setVisible(true);
                    this.verticalGroup.addActorAt(3, this.waterStats.group);
                    this.waterVisible = true;
                    this.updatePanelSize();
                }
            } else {
                this.waterStats.group.setVisible(false);
                this.verticalGroup.removeActor(this.waterStats.group);
                this.waterVisible = false;
                this.updatePanelSize();
            }
        } else {
            this.waterStats.group.setVisible(false);
            this.verticalGroup.removeActor(this.waterStats.group);
            this.waterVisible = false;
            this.updatePanelSize();
        }
        if (this.rocketTech != null) {
            if (this.rocketTech.unlocked) {
                if (!this.fuelVisible && on) {
                    this.fuelStats.group.setVisible(true);
                    this.verticalGroup.addActorAt(4, this.fuelStats.group);
                    this.fuelVisible = true;
                    this.updatePanelSize();
                }
            } else {
                this.fuelStats.group.setVisible(false);
                this.verticalGroup.removeActor(this.fuelStats.group);
                this.fuelVisible = false;
                this.updatePanelSize();
            }
        } else {
            this.fuelStats.group.setVisible(false);
            this.verticalGroup.removeActor(this.fuelStats.group);
            this.fuelVisible = false;
            this.updatePanelSize();
        }
        this.table.layout();
        if (!this.visible && on) {
            this.show();
        }
        if (!on && this.visible) {
            this.hide();
        }
    }

    public void show() {
        this.visible = true;
        this.group.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.moveTo(this.normalPosition.x - 5.0f, this.normalPosition.y), (Action)Actions.visible(true), (Action)Actions.parallel((Action)Actions.fadeIn(0.25f), (Action)Actions.moveTo(this.normalPosition.x, this.normalPosition.y, 0.25f, Interpolation.sineOut))));
    }

    public void hide() {
        this.visible = false;
        this.group.addAction(Actions.sequence((Action)Actions.parallel((Action)Actions.fadeOut(0.25f), (Action)Actions.moveTo(this.normalPosition.x - 5.0f, this.normalPosition.y, 0.25f, Interpolation.sineIn)), (Action)Actions.visible(false)));
    }

    public void showBasePowerPopup() {
        MoonBase.log("Show base power popup?");
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        if (msg.message == 50) {
            this.baseGroup = this.hud.gameScreen.world.getPlayer().getCurrentBase();
            if (this.baseGroup != null) {
                this.oxygenStats.setBaseGroup(this.baseGroup);
                this.powerStats.setBaseGroup(this.baseGroup);
                this.waterStats.setBaseGroup(this.baseGroup);
                this.fuelStats.setBaseGroup(this.baseGroup);
                return true;
            }
            return false;
        }
        return false;
    }
}

