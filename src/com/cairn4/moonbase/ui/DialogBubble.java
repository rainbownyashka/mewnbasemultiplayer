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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cairn4.moonbase.entities.Speaker;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.ToolTipWatchable;

public class DialogBubble
extends Actor {
    BaseScreen baseScreen;
    Stage stage;
    Group group;
    Image panelBg;
    Table table;
    Label name;
    Label secondaryLabel;
    Vector2 offset;
    Group innerGroup;
    public ToolTipWatchable watchable;
    Speaker currentSpeaker;
    public Vector2 speakerPosition;
    float timeout = 3.0f;
    float timer;
    public boolean autoDestroy = false;
    public boolean fadingOut = false;

    public DialogBubble(BaseScreen baseScreen, Stage stage, Vector2 speakerPos, String dialog) {
        this(baseScreen, stage, speakerPos, dialog, "");
    }

    public DialogBubble(BaseScreen baseScreen, Stage stage, Speaker speaker, String dialog) {
        this(baseScreen, stage, new Vector2(speaker.getXPos(), speaker.getYPos()), dialog, "");
        this.currentSpeaker = speaker;
    }

    public DialogBubble(BaseScreen baseScreen, Stage stage, Vector2 speakerPos, String dialog, String secondaryTip) {
        this.baseScreen = baseScreen;
        this.stage = stage;
        this.speakerPosition = new Vector2(speakerPos.x, speakerPos.y);
        this.group = new Group();
        this.innerGroup = new Group();
        this.innerGroup.setPosition(0.0f, 0.0f);
        this.group.addActor(this.innerGroup);
        Image arrow = new Image(baseScreen.skin.getDrawable("dialog-bubble-arrow"));
        arrow.setOrigin(1);
        arrow.setScaleX(0.8f);
        arrow.setPosition(0.0f, 0.0f, 2);
        this.innerGroup.addActor(arrow);
        NinePatch roundedbox = new NinePatch(baseScreen.skin.getRegion("roundedbox"), 18, 18, 18, 18);
        this.panelBg = new Image(roundedbox);
        this.panelBg.setColor(0.0f, 0.0f, 0.0f, 0.8f);
        this.innerGroup.addActor(this.panelBg);
        this.panelBg.setTouchable(Touchable.disabled);
        this.table = new Table();
        this.table.setPosition(0.0f, 0.0f);
        this.table.left().bottom();
        this.innerGroup.addActor(this.table);
        this.name = new Label((CharSequence)dialog, baseScreen.labelStyle);
        this.name.setFontScale(0.35f);
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
            this.secondaryLabel.setFontScale(0.25f);
            this.secondaryLabel.setWrap(true);
            this.secondaryLabel.setColor(1.0f, 1.0f, 1.0f, 0.85f);
            this.secondaryLabel.setAlignment(10);
            this.secondaryLabel.setTouchable(Touchable.disabled);
            this.table.add(this.secondaryLabel).minWidth(225.0f).maxWidth(500.0f).fillX();
        }
        this.table.layout();
        this.offset = new Vector2(0.0f, 0.0f);
        this.panelBg.setSize(this.table.getPrefWidth() + 30.0f, this.table.getPrefHeight() + 20.0f);
        this.panelBg.setPosition(-15.0f, -10.0f);
        this.innerGroup.setPosition(-this.panelBg.getWidth() / 2.0f, 0.0f);
        arrow.setPosition(this.panelBg.getWidth() / 2.0f - 15.0f, -8.0f, 2);
        arrow.setColor(0.0f, 0.0f, 0.0f, 0.8f);
        this.group.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.25f, 0.25f), (Action)Actions.parallel((Action)Actions.sequence((Action)Actions.scaleTo(1.1f, 1.1f, 0.15f, Interpolation.sineOut), (Action)Actions.scaleTo(1.0f, 1.0f, 0.05f)), (Action)Actions.fadeIn(0.2f))));
    }

    @Override
    protected void setParent(Group parent) {
        super.setParent(parent);
        if (this.getParent() != null) {
            this.group.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.fadeIn(0.2f)));
            this.getParent().addActor(this.group);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (this.currentSpeaker != null) {
            this.speakerPosition.set(this.currentSpeaker.getXPos(), this.currentSpeaker.getYPos() + this.currentSpeaker.getHeight());
        }
        Vector2 screenPos = this.speakerPosition.cpy();
        this.group.setPosition(screenPos.x + 10.0f + this.offset.x, screenPos.y + 5.0f + this.offset.y);
        if (this.watchable != null) {
            this.name.setText(this.watchable.getTooltipText());
            this.panelBg.setSize(this.name.getPrefWidth() + 30.0f, this.name.getPrefHeight() + 20.0f);
        }
        if (this.autoDestroy) {
            this.timer += delta;
            if (this.timer > this.timeout && !this.fadingOut) {
                this.fadeOut();
            }
        }
    }

    public void fadeOut() {
        this.group.addAction(Actions.sequence((Action)Actions.fadeOut(0.5f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                DialogBubble.this.remove();
            }
        })));
    }

    @Override
    public boolean remove() {
        this.group.remove();
        return super.remove();
    }
}

