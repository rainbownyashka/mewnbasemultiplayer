/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.SnapshotArray;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.HudItemPickupNotification;
import com.cairn4.moonbase.ui.HudNotification;
import com.cairn4.moonbase.ui.HudNotificationData;
import java.util.ArrayList;

public class HudNotifications
implements Telegraph {
    Hud hud;
    private Group mainParentGroup;
    private Group messageLogGroup;
    ArrayList<HudNotification> notificationList = new ArrayList();
    private static final float xPos = 30.0f;
    private static final float yPos = 160.0f;
    public static Pool<HudNotificationData> hudNotificationDataPool = Pools.get(HudNotificationData.class);

    public HudNotifications(Hud hud, Group parentGroup) {
        this.hud = hud;
        this.mainParentGroup = parentGroup;
        this.messageLogGroup = new Group();
        this.mainParentGroup.addActor(this.messageLogGroup);
        this.messageLogGroup.setPosition(30.0f, 160.0f);
        MessageManager.getInstance().addListener(this, 3);
    }

    public void reparentGroup(Group newParent) {
        this.messageLogGroup.remove();
        this.messageLogGroup.setPosition(-newParent.getX() + 30.0f, -newParent.getY() + 160.0f);
        newParent.addActor(this.messageLogGroup);
    }

    public void resetGroup() {
        this.messageLogGroup.remove();
        this.mainParentGroup.addActor(this.messageLogGroup);
        this.messageLogGroup.setPosition(30.0f, 160.0f);
    }

    public void newMessage(String icon, String text) {
        this.newMessage(icon, text, Color.WHITE);
    }

    public void newMessage(String text, Color color) {
        this.newMessage(null, text, color);
    }

    public void newMessage(String text) {
        this.newMessage(null, text, Color.WHITE);
    }

    public void newMessage(String icon, String text, Color color) {
        this.pushMessagesDown();
        HudNotification n = new HudNotification(this.hud, this.messageLogGroup);
        n.set(icon, text, color);
        this.notificationList.add(n);
    }

    /** Message without typewriter animation. */
    public void newMessageInstant(String icon, String text, Color color) {
        this.pushMessagesDown();
        HudNotification n = new HudNotification(this.hud, this.messageLogGroup);
        n.setInstant(icon, text, color);
        this.notificationList.add(n);
    }

    public void newMessageInstant(String text) {
        this.newMessageInstant(null, text, Color.WHITE);
    }

    public void newPickupMessage(Item item, int count, String prefix) {
        boolean existingMessageForItem = false;
        for (HudNotification hn : this.notificationList) {
            if (!(hn instanceof HudItemPickupNotification)) continue;
            HudItemPickupNotification hipn = (HudItemPickupNotification)hn;
            if (!hipn.itemId.equals(item.id)) continue;
            existingMessageForItem = true;
            hipn.add(count);
            break;
        }
        if (!existingMessageForItem) {
            this.pushMessagesDown();
            HudItemPickupNotification n = new HudItemPickupNotification(this.hud, this.messageLogGroup);
            n.setPrefix(prefix);
            n.set(item, count);
            this.notificationList.add(n);
        }
    }

    /** Create a single HUD entry for a chat message with colored nick and message. */
    public void newChatMessage(String nick, String message) {
        this.pushMessagesDown();
        HudNotification n = new HudNotification(this.hud, this.messageLogGroup);
        try {
            com.badlogic.gdx.graphics.Color nickColor = com.badlogic.gdx.graphics.Color.valueOf("25addb");
            com.badlogic.gdx.graphics.Color msgColor = com.badlogic.gdx.graphics.Color.WHITE;
            n.setChat(nick, message, nickColor, msgColor);
        } catch (Exception e) {
            // fallback
            n.set(null, nick + ": " + message, com.badlogic.gdx.graphics.Color.WHITE);
        }
        this.notificationList.add(n);
    }

    /** Chat message without typewriter animation. */
    public void newChatMessageInstant(String nick, String message) {
        this.pushMessagesDown();
        HudNotification n = new HudNotification(this.hud, this.messageLogGroup);
        try {
            com.badlogic.gdx.graphics.Color nickColor = com.badlogic.gdx.graphics.Color.valueOf("25addb");
            com.badlogic.gdx.graphics.Color msgColor = com.badlogic.gdx.graphics.Color.WHITE;
            n.setChatInstant(nick, message, nickColor, msgColor);
        } catch (Exception e) {
            n.set(null, nick + ": " + message, com.badlogic.gdx.graphics.Color.WHITE);
        }
        this.notificationList.add(n);
    }

    private void pushMessagesDown() {
        SnapshotArray<Actor> child = this.messageLogGroup.getChildren();
        for (Actor a : child) {
            a.addAction(Actions.sequence((Action)Actions.moveBy(0.0f, 48.0f, 0.25f, Interpolation.sine)));
        }
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        if (msg.message == 3) {
            HudNotificationData note = (HudNotificationData)msg.extraInfo;
            if (note.icon.equals("")) {
                this.newMessage(note.message, note.textColor);
            } else {
                this.newMessage(note.icon, note.message, note.textColor);
            }
        }
        return false;
    }
}
