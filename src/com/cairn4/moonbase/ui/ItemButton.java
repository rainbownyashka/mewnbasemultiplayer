/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.ui.Menu;

public class ItemButton
extends Widget {
    Menu menu;
    Group group;
    protected static Button.ButtonStyle buttonStyle;
    protected static Button.ButtonStyle emptyButtonStyle;
    float baseSizeW = 97.0f;
    float baseSizeH = 96.0f;
    private Button emptyButton;
    Button button;
    Image icon;
    Label count;
    Image equippedIcon;
    private float scale = 1.0f;
    private Image tutorialArrow;
    private static final float barWidth = 64.0f;
    Group progressGroup;
    Image progressBarBg;
    Image progressBar;

    public ItemButton(Menu menu, Button.ButtonStyle buttonStyle, Group parent) {
        this.menu = menu;
        this.group = new Group();
        this.group.setTouchable(Touchable.enabled);
        parent.addActor(this.group);
        ItemButton.buttonStyle = buttonStyle;
        this.createButton();
        this.setupIcon();
    }

    public ItemButton(Menu menu, Group parent) {
        this(menu);
        if (parent != null) {
            parent.addActor(this.group);
        }
    }

    public ItemButton(Menu menu) {
        this(menu, 1.0f);
    }

    public ItemButton(Menu menu, float ninePatchScale) {
        this.menu = menu;
        this.group = new Group();
        NinePatch np_up = new NinePatch(menu.skin.getRegion("btn-item"), 16, 16, 16, 16);
        np_up.scale(ninePatchScale, ninePatchScale);
        NinePatchDrawable npd_up = new NinePatchDrawable(np_up);
        NinePatch np_active = new NinePatch(menu.skin.getRegion("btn-item-active"), 16, 16, 16, 16);
        np_active.scale(ninePatchScale, ninePatchScale);
        NinePatchDrawable npd_active = new NinePatchDrawable(np_active);
        NinePatch np_down = new NinePatch(menu.skin.getRegion("btn-item-pressed"), 16, 16, 16, 16);
        np_down.scale(ninePatchScale, ninePatchScale);
        NinePatchDrawable npd_pressed = new NinePatchDrawable(np_down);
        NinePatch np_disabled = new NinePatch(menu.skin.getRegion("btn-item-disabled"), 16, 16, 16, 16);
        np_disabled.scale(ninePatchScale, ninePatchScale);
        NinePatchDrawable npd_disabled = new NinePatchDrawable(np_disabled);
        buttonStyle = new Button.ButtonStyle();
        ItemButton.buttonStyle.up = npd_up;
        ItemButton.buttonStyle.over = npd_active;
        ItemButton.buttonStyle.down = npd_pressed;
        ItemButton.buttonStyle.disabled = npd_disabled;
        ItemButton.buttonStyle.checked = npd_active;
        emptyButtonStyle = new Button.ButtonStyle();
        ItemButton.emptyButtonStyle.up = npd_disabled;
        ItemButton.emptyButtonStyle.over = npd_up;
        ItemButton.emptyButtonStyle.down = npd_pressed;
        ItemButton.emptyButtonStyle.disabled = npd_disabled;
        ItemButton.emptyButtonStyle.checked = npd_active;
        this.createButton();
        this.setEmpty(false);
        this.setupIcon();
        this.setupProgressBar();
        this.validate();
    }

    public void setEmpty(boolean empty) {
        this.emptyButton.setVisible(empty);
        if (empty) {
            this.button.setColor(1.0f, 1.0f, 1.0f, 0.01f);
        } else {
            this.button.setColor(Color.WHITE);
        }
    }

    protected void createButton() {
        this.group.setSize(this.baseSizeW, this.baseSizeH);
        this.emptyButton = new Button(emptyButtonStyle);
        this.emptyButton.setSize(this.baseSizeW, this.baseSizeH);
        this.emptyButton.setPosition(0.0f, 0.0f);
        this.group.addActor(this.emptyButton);
        this.button = new Button(buttonStyle);
        this.button.setSize(this.baseSizeW, this.baseSizeH);
        this.button.setPosition(0.0f, 0.0f);
        this.group.addActor(this.button);
    }

    protected void setupIcon() {
        this.icon = new Image(this.menu.skin.getDrawable("white"));
        this.icon.setPosition(this.button.getX(1), this.button.getY(1), 1);
        this.icon.setColor(1.0f, 1.0f, 1.0f, 0.65f);
        this.icon.setTouchable(Touchable.disabled);
        this.group.addActor(this.icon);
        this.count = new Label((CharSequence)"", this.menu.labelStyle);
        this.count.setFontScale(0.5f);
        this.count.setWidth(this.button.getWidth());
        this.count.setAlignment(20);
        this.count.setPosition(11.0f, 0.0f);
        this.group.addActor(this.count);
        this.count.setTouchable(Touchable.disabled);
        this.equippedIcon = new Image(this.menu.skin.getDrawable("hud-equippedBorder"));
        this.equippedIcon.setSize(119.0f, 117.0f);
        this.equippedIcon.setPosition(this.button.getX(1) - 2.0f, this.button.getY(1), 1);
        this.equippedIcon.setOrigin(1);
        this.equippedIcon.setVisible(false);
        this.group.addActor(this.equippedIcon);
        this.equippedIcon.setTouchable(Touchable.disabled);
        this.setIconPadding(25.0f);
        this.setCountOffset(4.0f, -7.0f);
    }

    private void setupProgressBar() {
        this.progressGroup = new Group();
        this.progressGroup.setVisible(false);
        this.progressGroup.setTouchable(Touchable.disabled);
        this.progressGroup.setPosition(this.baseSizeW / 2.0f, 3.0f, 1);
        this.group.addActor(this.progressGroup);
        NinePatch progressBarNinePatch = new NinePatch(this.menu.skin.getRegion("tile-progress-bar"), 3, 3, 3, 3);
        this.progressBarBg = new Image(progressBarNinePatch);
        this.progressBarBg.setColor(new Color(0.0f, 0.0f, 0.0f, 0.65f));
        this.progressBarBg.setWidth(64.0f);
        this.progressBarBg.setHeight(8.0f);
        this.progressBarBg.setPosition(0.0f, 0.0f, 1);
        this.progressGroup.addActor(this.progressBarBg);
        this.progressBar = new Image(progressBarNinePatch);
        this.progressBar.setColor(Menu.MENU_COLOR);
        this.progressBar.setWidth(64.0f);
        this.progressBar.setHeight(8.0f);
        this.progressBar.setPosition(0.0f, 0.0f, 1);
        this.progressGroup.addActor(this.progressBar);
        this.progressGroup.setOrigin(this.progressBarBg.getX(1), this.progressBarBg.getY(1));
    }

    public void setIconPadding(float padding) {
        this.icon.setSize(this.button.getWidth() - padding, this.button.getHeight() - padding);
        this.icon.setPosition(this.button.getX(1), this.button.getY(1), 1);
    }

    public void setCountOffset(float x, float y) {
        this.count.setPosition(this.icon.getX(20) + x, this.icon.getY(20) + y, 20);
    }

    public void enable() {
        this.button.setDisabled(false);
        this.button.setTouchable(Touchable.enabled);
        this.icon.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void disable() {
        this.button.setDisabled(true);
        this.button.setTouchable(Touchable.disabled);
        this.icon.setColor(0.5f, 0.5f, 0.5f, 1.0f);
    }

    public void showIcon() {
        this.icon.setVisible(true);
    }

    public void hideIcon() {
        this.icon.setVisible(false);
    }

    public void setCount(int amount) {
        this.count.setText("" + amount);
        this.count.setVisible(amount > 1);
    }

    public void setIcon(String sprite) {
        if (sprite != null) {
            this.icon.setVisible(true);
            try {
                this.icon.setDrawable(this.menu.skin.getDrawable(sprite));
            }
            catch (GdxRuntimeException e) {
                Gdx.app.error("MewnBase", "Sprite is missing or we're trying to look for an item that isn't defined in items.json");
                this.icon.setDrawable(this.menu.skin.getDrawable("missing"));
            }
        } else {
            this.icon.setVisible(false);
        }
    }

    public void setEquippedIcon(boolean equipped) {
        boolean prevVisible = this.equippedIcon.isVisible();
        this.equippedIcon.setVisible(equipped);
        if (equipped && !prevVisible) {
            this.equippedIcon.clearActions();
            this.equippedIcon.addAction(Actions.sequence((Action)Actions.alpha(0.5f), (Action)Actions.scaleTo(0.9f, 0.9f), (Action)Actions.parallel((Action)Actions.fadeIn(0.15f), (Action)Actions.sequence((Action)Actions.scaleTo(1.02f, 1.02f, 0.1f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.07f))), (Action)Actions.forever(Actions.sequence((Action)Actions.scaleTo(0.97f, 0.97f, 1.0f, Interpolation.sine), (Action)Actions.scaleTo(1.0f, 1.0f, 1.0f, Interpolation.sine)))));
        }
    }

    public void setBorderColor(Color color) {
        this.button.setColor(color);
    }

    public void setIconColor(Color color) {
        this.icon.setColor(color);
    }

    public void resetColors() {
        this.button.setColor(Color.WHITE);
        this.icon.setColor(Color.WHITE);
    }

    public void showTutorialArrow() {
        if (this.tutorialArrow != null) {
            this.removeTutorialArrow();
        }
        this.tutorialArrow = new Image(this.menu.skin.getDrawable("flow-arrow"));
        this.tutorialArrow.setOrigin(1);
        this.tutorialArrow.setTouchable(Touchable.disabled);
        this.tutorialArrow.setPosition(this.button.getX(1), this.button.getY(4) - 5.0f, 1);
        this.group.addActor(this.tutorialArrow);
        this.tutorialArrow.addAction(Actions.forever(Actions.sequence((Action)Actions.moveBy(0.0f, 8.0f, 0.5f, Interpolation.sine), (Action)Actions.moveBy(0.0f, -8.0f, 0.5f, Interpolation.sine))));
    }

    public void removeTutorialArrow() {
        if (this.tutorialArrow != null) {
            this.tutorialArrow.remove();
            this.tutorialArrow = null;
        }
    }

    public void updateDurability(Item i) {
        if (i != null) {
            int maxDurability = i.getData().durability;
            if (maxDurability > 0 && i.durability < maxDurability) {
                this.progressGroup.setVisible(true);
                float d = (float)i.durability / (float)maxDurability;
                this.setDurability(d);
                if (d < 0.2f) {
                    this.progressBar.setColor(Vars.WARNING_RED);
                } else {
                    this.progressBar.setColor(Menu.MENU_COLOR);
                }
            } else {
                this.progressGroup.setVisible(false);
            }
        } else {
            this.progressGroup.setVisible(false);
        }
    }

    private void setDurability(float progress) {
        this.progressGroup.setVisible(true);
        this.progressBar.setWidth(progress * 64.0f);
        if (this.progressBar.getWidth() < 6.0f) {
            this.progressBar.setWidth(6.0f);
        }
    }

    @Override
    public void clearListeners() {
        this.button.clearListeners();
    }

    @Override
    public boolean addListener(EventListener listener) {
        return this.button.addListener(listener);
    }

    @Override
    public float getPrefWidth() {
        return this.baseSizeW;
    }

    @Override
    public float getPrefHeight() {
        return this.baseSizeH;
    }

    @Override
    public float getX() {
        return this.group.getX();
    }

    @Override
    public float getY() {
        return this.group.getY();
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
        this.group.setScale(this.scale);
        this.group.setSize(this.group.getWidth() * this.scale, this.group.getHeight() * this.scale);
    }

    @Override
    public void setPosition(float x, float y) {
        this.group.setPosition(x, y);
    }

    @Override
    public float getWidth() {
        return this.button.getWidth() * this.scale;
    }

    @Override
    public float getHeight() {
        return this.button.getHeight() * this.scale;
    }

    @Override
    public void setVisible(boolean visible) {
        this.group.setVisible(visible);
    }

    @Override
    public void layout() {
    }

    public void showCount(boolean visible) {
        this.count.setVisible(visible);
    }
}

