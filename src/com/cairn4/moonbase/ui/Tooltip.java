/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.ToolTipWatchable;

public class Tooltip
extends Actor {
    BaseScreen baseScreen;
    Stage stage;
    Group group;
    Image panel;
    Table table;
    Label name;
    Label secondaryLabel;
    Vector2 offset;
    Group innerGroup;
    public ToolTipWatchable watchable;

    public Tooltip(BaseScreen baseScreen, Stage stage, String tip) {
        this(baseScreen, stage, tip, "");
    }

    public Tooltip(BaseScreen baseScreen, Stage stage, String tip, String secondaryTip) {
        this(baseScreen, stage, tip, secondaryTip, new TooltipStyle());
    }

    public Tooltip(BaseScreen baseScreen, Stage stage, String tip, String secondaryTip, TooltipStyle style) {
        this.baseScreen = baseScreen;
        this.stage = stage;
        this.group = new Group();
        this.group.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        this.innerGroup = new Group();
        this.innerGroup.setPosition(15.0f, 15.0f);
        this.group.addActor(this.innerGroup);
        NinePatch roundedbox = new NinePatch(baseScreen.skin.getRegion("roundedbox"), 18, 18, 18, 18);
        this.panel = new Image(roundedbox);
        this.panel.setColor(0.0f, 0.0f, 0.0f, 0.8f);
        this.innerGroup.addActor(this.panel);
        this.panel.setTouchable(Touchable.disabled);
        this.table = new Table();
        this.table.setPosition(0.0f, 0.0f);
        this.table.left().bottom();
        this.innerGroup.addActor(this.table);
        this.name = new Label((CharSequence)tip, baseScreen.labelStyle);
        this.name.setFontScale(style.headingScale);
        this.name.setColor(1.0f, 1.0f, 1.0f, 0.8f);
        this.name.setAlignment(8);
        this.name.setTouchable(Touchable.disabled);
        this.table.add(this.name).fillX();
        if (!secondaryTip.equals("")) {
            this.table.row();
            NinePatch divNinePatch = new NinePatch(baseScreen.skin.getRegion("credits-divider"), 10, 10, 0, 0);
            Image horz = new Image(divNinePatch);
            horz.setColor(1.0f, 1.0f, 1.0f, 0.35f);
            this.table.add(horz).fillX().height(2.0f).space(6.0f).padLeft(-14.0f).padRight(-14.0f);
            this.table.row();
            this.secondaryLabel = new Label((CharSequence)secondaryTip, baseScreen.labelStyle);
            this.secondaryLabel.setFontScale(style.secondaryScale);
            this.secondaryLabel.setWrap(true);
            this.secondaryLabel.setColor(1.0f, 1.0f, 1.0f, 0.85f);
            this.secondaryLabel.setAlignment(10);
            this.secondaryLabel.setTouchable(Touchable.disabled);
            this.table.add(this.secondaryLabel).minWidth(225.0f).maxWidth(500.0f).fillX();
        }
        this.table.layout();
        this.offset = new Vector2(0.0f, 0.0f);
        this.panel.setSize(this.table.getPrefWidth() + 30.0f, this.table.getPrefHeight() + 20.0f);
        this.panel.setPosition(-15.0f, -10.0f);
    }

    @Override
    protected void setParent(Group parent) {
        super.setParent(parent);
        if (this.getParent() != null) {
            this.group.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.delay(0.01f), (Action)Actions.fadeIn(0.2f)));
            this.getParent().addActor(this.group);
        }
    }

    public void updateSecondaryLabel(String body) {
        this.secondaryLabel.setText(body);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Vector2 mouse = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        mouse = this.stage.screenToStageCoordinates(mouse);
        if (mouse.x > (float)MoonBase.SCREEN_WIDTH - this.panel.getWidth() - 20.0f) {
            this.offset.x = (float)MoonBase.SCREEN_WIDTH - this.panel.getWidth() - 20.0f - mouse.x;
        }
        if (mouse.y > (float)MoonBase.SCREEN_HEIGHT - this.panel.getHeight() - 20.0f) {
            this.offset.y = (float)MoonBase.SCREEN_HEIGHT - this.panel.getHeight() - 20.0f - mouse.y;
        }
        this.group.setPosition(mouse.x + 10.0f + this.offset.x, mouse.y + 5.0f + this.offset.y);
        if (this.watchable != null) {
            this.name.setText(this.watchable.getTooltipText());
            this.panel.setSize(this.name.getPrefWidth() + 30.0f, this.name.getPrefHeight() + 20.0f);
        }
    }

    @Override
    public boolean remove() {
        this.group.remove();
        return super.remove();
    }

    public static class TooltipStyle {
        float headingScale = 0.35f;
        float secondaryScale = 0.25f;

        public TooltipStyle() {
        }

        public TooltipStyle(float headingScale, float secondary) {
            this.headingScale = headingScale;
            this.secondaryScale = secondary;
        }
    }
}

