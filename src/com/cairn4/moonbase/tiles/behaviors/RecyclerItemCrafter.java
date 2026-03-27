/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;

public class RecyclerItemCrafter
extends ItemCrafter {
    @Override
    public boolean collectItem() {
        ItemStack i = (ItemStack)this.itemsToCollect.get(0);
        boolean collected = super.collectItem();
        if (collected) {
            // empty if block
        }
        return collected;
    }
}

