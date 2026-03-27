/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.BaseGroup;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.ui.BaseStatsInfo;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Tooltip;
import java.text.DecimalFormat;

public class BaseStatResourceBar {
    private Hud hud;
    BaseGroup currentBaseGroup;
    private Tooltip tooltip;
    public BaseResources resourceType;
    public Group group;
    private Image icon;
    private Label label;
    private float percent;
    NinePatch progressBarBgPatch;
    NinePatch progressBarPatch;
    float barSize;
    Image barBg;
    Image progressMeter;
    private static DecimalFormat df = new DecimalFormat("#");
    private static DecimalFormat df_tenth = new DecimalFormat("0.00");
    private static Color barColor = Color.valueOf("27caff");
    private static Color barColorWarning = Color.valueOf("ef3b11");

    public BaseStatResourceBar(Hud hud, Group parentGroup, BaseStatsInfo bsi, BaseResources resourceType) {
        MoonBase.log("creating resource bar");
        this.hud = hud;
        this.resourceType = resourceType;
        this.progressBarBgPatch = new NinePatch(hud.baseScreen.skin.getRegion("hud/basestats-bar-bg"), 4, 4, 4, 4);
        this.progressBarPatch = new NinePatch(hud.baseScreen.skin.getRegion("hud/basestats-bar"), 3, 3, 3, 3);
        this.group = new Group();
        this.group.setHeight(38.0f);
        this.group.setPosition(0.0f, 0.0f);
        this.group.setTouchable(Touchable.enabled);
        if (parentGroup != null) {
            parentGroup.addActor(this.group);
        }
        String iconName = "cargo";
        switch (resourceType) {
            case power: {
                iconName = "hud/basestats-power";
                break;
            }
            case water: {
                iconName = "hud/basestats-water";
                break;
            }
            case biofuel: {
                iconName = "cargo";
                break;
            }
            case oxygen: {
                iconName = "hud/basestats-o2";
                break;
            }
            case fuel: {
                iconName = "hud/basestats-rocketfuel";
            }
        }
        this.icon = new Image(hud.baseScreen.skin.getDrawable(iconName));
        this.icon.setSize(36.0f, 36.0f);
        this.icon.setPosition(0.0f, -2.0f);
        this.icon.setTouchable(Touchable.disabled);
        this.group.addActor(this.icon);
        this.label = new Label((CharSequence)"500/500", hud.baseScreen.labelStyle);
        this.label.setFontScale(0.32f);
        this.label.setPosition(45.0f, 10.0f);
        this.label.setAlignment(12, 8);
        this.group.addActor(this.label);
        this.label.setTouchable(Touchable.disabled);
        this.barBg = new Image(this.progressBarBgPatch);
        this.barBg.setPosition(40.0f, 0.0f);
        this.barBg.setSize(120.0f, 11.0f);
        this.barBg.setColor(0.0f, 0.0f, 0.0f, 0.4f);
        this.barBg.setTouchable(Touchable.disabled);
        this.group.addActor(this.barBg);
        this.progressMeter = new Image(this.progressBarPatch);
        this.progressMeter.setColor(Color.CYAN);
        this.progressMeter.setPosition(this.barBg.getX() + 2.0f, this.barBg.getY() + 2.0f);
        this.progressMeter.setSize(this.barBg.getWidth() - 4.0f, this.barBg.getHeight() - 4.0f);
        this.progressMeter.setTouchable(Touchable.disabled);
        this.barSize = this.progressMeter.getWidth();
        this.group.addActor(this.progressMeter);
        this.setWidth(160.0f);
    }

    public void setPosition(float x, float y) {
        this.group.setPosition(x, y);
    }

    public void setWidth(float w) {
        this.group.setWidth(w);
        this.barBg.setWidth(w - 40.0f);
        this.progressMeter.setSize(this.barBg.getWidth() - 4.0f, this.barBg.getHeight() - 4.0f);
        this.barSize = w - 40.0f - 4.0f;
    }

    public void setAmount(float amount, float max, boolean depleting) {
        String normalC = Menu.MENU_COLOR_HEX + "";
        String fadedC = Menu.MENU_COLOR_HEX + "20";
        String numberColor = "[#ffaf3aff]";
        if (depleting) {
            numberColor = "[#" + Vars.WARNING_RED + "]";
        }
        this.label.setText(numberColor + df.format(amount) + "[][#ffaf3a65]/" + df.format(max) + "[]");
        this.percent = amount / max;
        this.progressMeter.setWidth(this.percent * this.barSize);
        this.updateState();
        this.progressMeter.setVisible(amount > 1.0f);
        this.progressMeter.setVisible(this.progressMeter.getWidth() > 5.0f);
        if (max <= 0.0f) {
            this.group.setColor(1.0f, 1.0f, 1.0f, 0.5f);
        } else {
            this.group.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    public void setBarColor(Color color) {
        barColor = color;
        this.updateState();
    }

    private void updateState() {
        if (this.percent < 0.2f) {
            this.progressMeter.setColor(barColorWarning);
        } else {
            this.progressMeter.setColor(barColor);
        }
    }

    public float getWidth() {
        return this.icon.getWidth() + this.barBg.getWidth();
    }

    public void setIconSize(float size) {
        this.icon.setSize(size, size);
        float sizeDiff = size - 36.0f;
        if (sizeDiff > 0.0f) {
            this.label.moveBy(sizeDiff, 0.0f);
            this.barBg.moveBy(sizeDiff, 0.0f);
            this.barBg.setSize(this.barBg.getWidth() - sizeDiff, this.barBg.getHeight());
            this.progressMeter.moveBy(sizeDiff, 0.0f);
            this.barSize -= sizeDiff;
            this.icon.moveBy(0.0f, -sizeDiff / 2.0f);
        }
    }

    public void setBaseGroup(BaseGroup baseGroup) {
        this.currentBaseGroup = baseGroup;
        this.setupTooltip();
    }

    private void setupTooltip() {
        String toolTipText = "Tooltip!";
        switch (this.resourceType) {
            case power: {
                toolTipText = Localization.get("baseResource_power");
                break;
            }
            case water: {
                toolTipText = Localization.get("baseResource_water");
                break;
            }
            case oxygen: {
                toolTipText = Localization.get("baseResource_oxygen");
                break;
            }
            case fuel: {
                toolTipText = Localization.get("baseResource_fuel");
            }
        }
        final String fTip = toolTipText;
        this.group.addListener(new ClickListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                BaseStatResourceBar.this.tooltip = BaseStatResourceBar.this.hud.createTooltip(fTip, BaseStatResourceBar.this.getStatInfo());
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                BaseStatResourceBar.this.hud.removeTooltip();
                BaseStatResourceBar.this.tooltip = null;
            }

            @Override
            public boolean isOver() {
                boolean over = super.isOver();
                if (over) {
                    // empty if block
                }
                return over;
            }
        });
    }

    private String getStatInfo() {
        String stats = "";
        switch (this.resourceType) {
            case power: {
                stats = this.formatBaseResource(this.currentBaseGroup.getTotalPowerGen(), this.currentBaseGroup.getTotalPowerDraw());
                break;
            }
            case water: {
                stats = this.formatBaseResource(this.currentBaseGroup.getTotalWaterGen(), this.currentBaseGroup.getTotalWaterDraw());
                break;
            }
            case oxygen: {
                stats = this.formatBaseResource(this.currentBaseGroup.getTotalOxygenGen(), this.currentBaseGroup.getTotalOxygenDraw());
                break;
            }
            case fuel: {
                stats = this.formatBaseResource(this.currentBaseGroup.getTotalFuelGen(), this.currentBaseGroup.getTotalFuelDraw());
            }
        }
        return stats;
    }

    public String formatBaseResource(float totalGen, float totalDraw) {
        String stats = "";
        float delta = Gdx.graphics.getDeltaTime();
        float gen = totalGen / delta;
        float use = totalDraw / delta;
        stats = stats + Localization.get("power_gen") + ": " + df_tenth.format(gen) + "\n";
        String colorStart = "[#ffffff]";
        if (totalDraw > totalGen) {
            colorStart = "[#f5331c]";
        }
        stats = stats + colorStart + Localization.get("power_use") + ": " + df_tenth.format(use) + "[]";
        return stats;
    }

    public void updateTooltip() {
        if (this.tooltip != null) {
            this.tooltip.updateSecondaryLabel(this.getStatInfo());
        }
    }
}

