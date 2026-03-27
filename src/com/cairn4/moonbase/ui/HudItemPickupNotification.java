/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.HudNotification;

public class HudItemPickupNotification
extends HudNotification {
    public String itemId;
    Item item;
    public int count;
    private String prefix = "";

    public HudItemPickupNotification(Hud hud, Group parentGroup) {
        super(hud, parentGroup);
    }

    public void set(Item item, int count) {
        String icon = item.getSprite();
        String text = "+" + count + " " + item.getName();
        this.count = count;
        this.itemId = item.id;
        this.item = item;
        super.set(icon, this.prefix + text, Color.WHITE);
    }

    public void add(int amount) {
        this.count += amount;
        String text = "+" + this.count + " " + this.item.getName();
        this.updateText(this.prefix + text);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        if (prefix == null) {
            this.prefix = "";
        }
    }
}

