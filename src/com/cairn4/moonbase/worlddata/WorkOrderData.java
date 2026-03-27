/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.cairn4.moonbase.tiles.behaviors.CraftingBotBehavior;

public class WorkOrderData {
    public String itemId;
    public int quantity;

    public WorkOrderData() {
    }

    public WorkOrderData(CraftingBotBehavior.WorkOrder wo) {
        this.itemId = wo.itemId;
        this.quantity = wo.quantity;
    }
}

