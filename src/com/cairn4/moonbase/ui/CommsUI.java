/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;

public class CommsUI {
    private Hud hud;
    private Group parentGroup;
    public Group group;
    Table table;
    Image bgPanel;
    Image icon;
    Label label;
    private Vector2 normalPosition;
    public boolean visible = false;
    private float commsUpdateTimer = 0.0f;
    private final float commsUpdateRate = 2.0f;

    public CommsUI(final Hud hud, Group parentGroup) {
        this.hud = hud;
        this.parentGroup = parentGroup;
        this.group = new Group();
        this.parentGroup.addActor(this.group);
        this.group.setVisible(false);
        NinePatch panelPatch = new NinePatch(hud.baseScreen.skin.getRegion("hud/comms-panel"), 18, 18, 18, 18);
        this.bgPanel = new Image(panelPatch);
        this.bgPanel.getColor().a = 0.6f;
        this.group.addActor(this.bgPanel);
        this.table = new Table();
        this.table.bottom().left();
        this.table.padLeft(15.0f);
        this.group.addActor(this.table);
        this.icon = new Image(hud.baseScreen.skin.getDrawable("hud/base-comms-3"));
        this.icon.setTouchable(Touchable.disabled);
        this.table.add(this.icon).width(36.0f).height(36.0f).padRight(10.0f).padTop(2.0f);
        this.label = new Label((CharSequence)Localization.get("hud_comms"), hud.baseScreen.labelStyle);
        this.label.setFontScale(0.32f);
        this.label.setColor(Menu.MENU_COLOR);
        this.label.setAlignment(8, 8);
        this.table.add(this.label).fillX().padBottom(3.0f);
        this.label.addListener(new ClickListener(0){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                String tooltip = Localization.get("hud_comms_tooltip");
                hud.createTooltip(tooltip);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hud.removeTooltip();
            }
        });
        this.bgPanel.setPosition(-2.0f, 0.0f);
        this.bgPanel.setSize(196.0f, this.table.getPrefHeight());
        this.group.setSize(this.bgPanel.getWidth(), this.bgPanel.getHeight());
        this.group.setPosition(16.0f, MoonBase.SCREEN_HEIGHT / 2 + 30 + 110, 8);
        this.normalPosition = new Vector2(this.group.getX(), this.group.getY());
    }

    public void update(float delta, Player player) {
        this.commsUpdateTimer += delta;
        if (this.commsUpdateTimer > 2.0f) {
            this.commsUpdateTimer = 0.0f;
            int r = player.playerStatus.updateCommsRange(delta);
            this.icon.setDrawable(this.hud.baseScreen.skin.getDrawable("hud/base-comms-" + r));
            if (r == 1) {
                this.label.getColor().a = 0.5f;
                if (this.visible) {
                    this.hide();
                }
            } else {
                this.label.getColor().a = 1.0f;
                if (!this.visible) {
                    this.show();
                }
            }
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
}

