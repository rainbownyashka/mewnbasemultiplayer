/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemActions;
import java.util.ArrayList;

public class Tool
extends Item {
    public int maxDurability;
    public int durability;

    public Tool(String id, String sprite, ArrayList<ItemActions> actions, boolean rotatable, String useAnimName, int amount, int maxDurability, int durability) {
        super(id, sprite, actions, rotatable, maxDurability, durability);
        this.durability = durability;
        this.maxDurability = maxDurability;
    }

    @Override
    public void reduceDurability(int d) {
        this.durability -= d;
        Gdx.app.log("MewnBase", "Item durability now : " + this.durability);
    }

    @Override
    public void resetDurability() {
        this.durability = this.maxDurability;
    }
}

