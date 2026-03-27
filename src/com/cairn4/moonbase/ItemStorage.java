/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemStack;
import java.util.ArrayList;

public interface ItemStorage {
    public ArrayList<ItemStack> getItemList();

    public boolean isFull();

    public void addTo(ItemStack var1);

    public void addSingle(Item var1);

    public void remove(int var1);

    public void removeSingle(int var1);

    public void removeEmpties();

    public boolean hasSlotAvailable(String var1);
}

