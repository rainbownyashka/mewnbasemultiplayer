/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.ui.CraftingUI;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.ItemButton;

public class RocketCraftingUI
extends CraftingUI {
    public RocketCraftingUI(GameScreen gameScreen, ItemCrafter itemCrafter, PlayerInventory playerInventory) {
        super(gameScreen, itemCrafter, playerInventory);
    }

    @Override
    protected void craft() {
        if (MoonBase.getCurrentMission().missionType == Mission.MissionTypes.tutorial) {
            for (ItemButton b : this.itemButtonList) {
                b.removeTutorialArrow();
            }
        }
        if (this.selectedItemData != null) {
            this.itemCrafter.addItemToBuildQueue(this.selectedItemData, true);
        }
        this.updateBuildQueue();
        this.back();
    }
}

