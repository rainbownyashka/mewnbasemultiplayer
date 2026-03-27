/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.cairn4.moonbase.ui.AutoTypeLabel;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.HudNotifications;

public class HudNotification {
    Hud hud;
    HudNotifications hudNotifications;
    Group group;
    private Image bg;
    Group innerGroup;
    AutoTypeLabel label;
    Image icon;

    public HudNotification(Hud hud, Group parentGroup) {
        this.hud = hud;
        this.hudNotifications = this.hud.hudNotifications;
        this.group = new Group();
        this.group.setTouchable(Touchable.disabled);
        parentGroup.addActor(this.group);
        this.innerGroup = new Group();
        this.group.addActor(this.innerGroup);
        this.fadeOut();
    }

    private void fadeOut() {
        this.innerGroup.addAction(Actions.sequence(Actions.alpha(0.0f), Actions.fadeIn(0.25f), Actions.delay(1.8f), Actions.fadeOut(1.2f), Actions.run(new Runnable(){

            @Override
            public void run() {
                HudNotification.this.removeFromList();
            }
        }), Actions.removeActor()));
    }

    private void fadeOutFast() {
        this.innerGroup.addAction(Actions.sequence((Action)Actions.delay(1.8f), (Action)Actions.fadeOut(1.2f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                HudNotification.this.removeFromList();
            }
        }), (Action)Actions.removeActor()));
    }

    private void removeFromList() {
        this.hudNotifications.notificationList.remove(this);
    }

    public void set(String iconName, String text, Color color) {
        this.bg = new Image(this.hud.gameScreen.skin.getDrawable("itempickup-bg"));
        this.bg.setPosition(0.0f, 0.0f, 8);
        this.bg.setColor(0.7f, 0.7f, 0.7f, 0.5f);
        this.innerGroup.addActor(this.bg);
        this.label = new AutoTypeLabel(text, this.hud.labelStyle);
        this.label.setFontScale(0.4f);
        this.label.setAlignment(8, 8);
        this.label.setWidth(600.0f);
        this.label.setColor(color);
        this.label.sound = "sounds/textAnimator2.ogg";
        this.label.animate(0.01f);
        this.label.setPosition(67.0f, 0.0f, 8);
        this.innerGroup.addActor(this.label);
        if (iconName != null) {
            try {
                this.icon = new Image(this.hud.gameScreen.skin.getDrawable(iconName));
            }
            catch (GdxRuntimeException e) {
                Gdx.app.error("MewnBase", "HudNotification icon is missing");
                this.icon = new Image(this.hud.gameScreen.skin.getDrawable("missing"));
            }
            this.icon.setSize(40.0f, 40.0f);
            this.icon.setPosition(15.0f, 0.0f, 8);
            this.innerGroup.addActor(this.icon);
        } else {
            this.label.setX(20.0f);
        }
    }

    /**
     * Set a chat-style notification where the nick is shown in nickColor and the message in msgColor on the same row.
     */
    public void setChat(String nick, String text, Color nickColor, Color msgColor) {
        this.bg = new Image(this.hud.gameScreen.skin.getDrawable("itempickup-bg"));
        this.bg.setPosition(0.0f, 0.0f, 8);
        this.bg.setColor(0.7f, 0.7f, 0.7f, 0.5f);
        this.innerGroup.addActor(this.bg);
        // Nick label
        AutoTypeLabel nickLabel = new AutoTypeLabel(nick + ": ", this.hud.labelStyle);
        nickLabel.setFontScale(0.4f);
        nickLabel.setAlignment(8, 8);
        nickLabel.setColor(nickColor);
        nickLabel.setPosition(20.0f, 0.0f, 8);
        nickLabel.sound = null;
        this.innerGroup.addActor(nickLabel);
        // Message label placed after measuring nick width so the white text aligns nicely
        this.label = new AutoTypeLabel(text == null ? "" : text, this.hud.labelStyle);
        this.label.setFontScale(0.4f);
        this.label.setAlignment(8, 8);
        this.label.setColor(msgColor);
        this.label.sound = "sounds/textAnimator2.ogg";
        this.label.animate(0.01f);
        // Compute nick rendered width and position message just after it. Also prefix message with
        // floor(nick.length() * 1.1) spaces as requested (rounded down).
        try {
            int spaces = 0;
            try { spaces = (int)Math.floor((double)nick.length() * 1.1); } catch (Exception ignored) { spaces = nick.length(); }
            if (spaces < 0) spaces = 0;
            StringBuilder sb = new StringBuilder(spaces + (text == null ? 0 : text.length()));
            for (int i = 0; i < spaces; i++) sb.append(' ');
            if (text != null) sb.append(text);
            this.label.setText(sb.toString());
            float nickWidth = nickLabel.getRenderedWidth();
            // add a small padding
            float pad = 8.0f;
            this.label.setPosition(20.0f + nickWidth + pad, 0.0f, 8);
        } catch (Exception ignored) {
            // fallback: place message at a generous offset
            this.label.setPosition(120.0f, 0.0f, 8);
        }
        this.innerGroup.addActor(this.label);
    }

    public void updateText(String text) {
        this.label.setFinalText("" + text);
        this.innerGroup.clearActions();
        this.innerGroup.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.fadeOutFast();
    }

    public void moveToBottom() {
        this.group.setPosition(0.0f, 0.0f);
    }
}

