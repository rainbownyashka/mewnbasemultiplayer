/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.PlayerStatusEffect;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.ToolTipWatchable;
import java.text.DecimalFormat;

public class StatusEffectUI
implements ToolTipWatchable {
    public static final float WIDTH = 50.0f;
    public static final float SPACING = 10.0f;
    private BaseScreen baseScreen;
    private Group group;
    private Image image;
    private Image timerOverlay;
    public PlayerStatusEffect statusEffect;
    public String tooltipText;
    public String tooltipTime;
    Hud hud;
    DecimalFormat df;
    private Image warningIcon;

    public StatusEffectUI(PlayerStatusEffect s, Group parentGroup, BaseScreen baseScreen, Hud hud) {
        this.hud = hud;
        this.baseScreen = baseScreen;
        this.statusEffect = s;
        this.group = new Group();
        this.group.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        parentGroup.addActor(this.group);
        this.image = new Image(baseScreen.skin.getDrawable(s.iconDrawable));
        this.image.setSize(40.0f, 40.0f);
        this.image.setOrigin(1);
        this.image.setPosition(0.0f, 0.0f, 1);
        this.group.addActor(this.image);
        this.setupTooltip();
        if (s.timeBased) {
            this.timerOverlay = new Image(baseScreen.skin.getDrawable(s.iconDrawable));
            this.timerOverlay.setColor(0.0f, 0.0f, 0.0f, 1.0f);
            this.timerOverlay.setSize(36.0f, 36.0f);
            this.timerOverlay.setOrigin(2);
            this.timerOverlay.getColor().a = 0.6f;
            this.timerOverlay.setPosition(0.0f, 0.0f, 1);
            this.timerOverlay.setTouchable(Touchable.disabled);
            this.group.addActor(this.timerOverlay);
        }
        this.group.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.5f, 0.5f), (Action)Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, 0.15f, Interpolation.sine), (Action)Actions.alpha(0.76f, 0.2f))));
        this.df = new DecimalFormat("#");
        if (this.statusEffect.showWarning) {
            this.showWarningIcon();
        }
        if (this.statusEffect.alertSound) {
            this.playWarningSound();
        }
    }

    private void playWarningSound() {
        Audio.getInstance().playSound("sounds/warningPulse.ogg", 0.3f);
    }

    private void showWarningIcon() {
        this.warningIcon = new Image(this.baseScreen.skin.getDrawable("base-warning"));
        this.warningIcon.setSize(68.0f, 60.0f);
        this.warningIcon.setOrigin(1);
        this.warningIcon.setPosition(0.0f, 0.0f, 1);
        this.warningIcon.setTouchable(Touchable.disabled);
        this.group.addActor(this.warningIcon);
        this.warningIcon.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.parallel((Action)Actions.fadeIn(0.15f), (Action)Actions.scaleTo(1.5f, 1.5f)), (Action)Actions.scaleTo(1.0f, 1.0f, 0.8f), (Action)Actions.parallel((Action)Actions.fadeOut(0.5f), (Action)Actions.scaleTo(0.75f, 0.75f, 0.5f)), (Action)Actions.removeActor()));
    }

    private void setupTooltip() {
        this.tooltipText = Localization.get("playerSE_" + this.statusEffect.getClass().getSimpleName());
        final StatusEffectUI t = this;
        this.image.addListener(new ClickListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                StatusEffectUI.this.hud.createTooltip(StatusEffectUI.this.tooltipText, t);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                StatusEffectUI.this.hud.removeTooltip();
            }
        });
    }

    public void update(float delta) {
        if (this.statusEffect.timeBased) {
            float yScale = 1.0f - this.statusEffect.time / this.statusEffect.maxTime;
            yScale = MathUtils.clamp(yScale, 0.0f, 1.0f);
            this.timerOverlay.setScale(1.0f, yScale);
        }
    }

    public void setPosition(float x, float y) {
        this.group.setPosition(x, y);
    }

    public void remove() {
        this.group.remove();
    }

    @Override
    public String getTooltipText() {
        this.tooltipTime = this.df.format(this.statusEffect.time);
        if (this.statusEffect.timeBased && this.statusEffect.time > 0.0f) {
            return this.tooltipText + ": " + this.tooltipTime;
        }
        return this.tooltipText;
    }
}

