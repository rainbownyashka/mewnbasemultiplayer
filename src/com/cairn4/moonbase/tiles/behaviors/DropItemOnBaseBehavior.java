/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.ui.DropItemHint;
import com.cairn4.moonbase.worlddata.BehaviorData;
import java.util.ArrayList;

public class DropItemOnBaseBehavior
implements Behavior {
    public boolean loaded = false;
    public ArrayList<String> acceptedItemIds = new ArrayList();
    public BaseModule baseModule;
    public DropItemHint dropItemHint;

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public void setId(String s) {
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void start() {
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void interact(Player player) {
        if (this.isAcceptedItem(player.playerInventory.getEquippedItemId())) {
            if (this.baseModule != null) {
                System.out.println("Item dropping onto base...");
                this.baseModule.handleDroppedItem(player.playerInventory, player.playerInventory.getEquippedItem());
            }
        } else {
            System.out.println("Not accepted.");
            if (this.dropItemHint == null) {
                this.dropItemHint = new DropItemHint(this);
                this.baseModule.world.gameScreen.destroyableUIList.add(this.dropItemHint);
            }
        }
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public BehaviorData getData() {
        return null;
    }

    public boolean isAcceptedItem(String id) {
        for (String s : this.acceptedItemIds) {
            if (!id.equals(s)) continue;
            return true;
        }
        return false;
    }
}

