/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.ui.HudNotificationData;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.worlddata.CraftingBonusData;
import java.util.ArrayList;

public class GreenHouseItemCrafter
extends ItemCrafter {
    public ParticleActor finishedParticles;

    @Override
    public boolean collectItem() {
        ItemStack i = (ItemStack)this.itemsToCollect.get(0);
        boolean collected = super.collectItem();
        if (collected) {
            ArrayList<CraftingBonusData> bonusItemList = ItemFactory.getInstance().getCraftingBonus(i.getId());
            for (CraftingBonusData iid : bonusItemList) {
                for (int count = 0; count < iid.amount; ++count) {
                    if (iid.probability != 0.0f && !(MathUtils.random() > iid.probability) || !(MathUtils.random() > iid.probability)) continue;
                    Item newItem = ItemFactory.getInstance().createItem(iid.itemId);
                    new ItemPickup(this.baseModule.world, this.baseModule.chunk, this.baseModule.getWorldXPos(), this.baseModule.getWorldYPos(), newItem);
                }
            }
        } else {
            HudNotificationData msg = new HudNotificationData();
            msg.message = Localization.get("cantCollectNotification");
            MessageManager.getInstance().dispatchMessage(3, msg);
        }
        return collected;
    }

    @Override
    public void showFinishedIcon(boolean visible) {
        if (this.finishedParticles != null) {
            if (visible) {
                this.finishedParticles.pfx.start();
            } else {
                this.finishedParticles.pfx.allowCompletion();
            }
        }
    }
}

