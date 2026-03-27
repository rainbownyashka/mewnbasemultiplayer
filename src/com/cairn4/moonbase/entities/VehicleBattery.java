/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.Player;

public interface VehicleBattery {
    public void recharge(float var1);

    public boolean isRecharging();

    public float getChargePercent();

    public boolean recharge(ItemStack var1, Player var2);

    public void instantRecharge(float var1);
}

