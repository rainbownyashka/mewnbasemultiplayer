/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.tiles.LaunchPad;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;

public class RocketCrafter
extends ItemCrafter {
    Chunk chunk;
    int localX;
    int localY;

    @Override
    public void addItemToBuildQueue(ItemData itemData, boolean spendResources) {
        super.addItemToBuildQueue(itemData, spendResources);
        LaunchPad l = (LaunchPad)this.baseModule;
        if (l != null) {
            l.construction_start_sfx();
        }
    }

    @Override
    public void finishedBuilding() {
        MoonBase.log("RocketCrafter: Done building " + this.getCurrentBuildItem().getName());
        this.building = false;
        switch (this.getCurrentBuildItem().id) {
            case "rocket": {
                LaunchPad l = (LaunchPad)this.baseModule;
                if (l == null) break;
                l.addRocket();
                l.construction_finished_sfx();
                break;
            }
            default: {
                MoonBase.error("RocketCrafter: unknown vehicle");
            }
        }
        this.setChanged();
        this.notifyObservers("finishAndAutoClose");
        this.buildQueue.removeIndex(0);
        this.spentItemBufferList.removeIndex(0);
        this.buildTimer = 0.0f;
    }

    public void setSpawnTile(Chunk c, int localX, int localY) {
        this.chunk = c;
        this.localX = localX;
        this.localY = localY;
    }
}

