/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.cairn4.moonbase.entities.Tank;

public class TankDrill {
    public Tank tank;
    public boolean isInContactWithDrillable = false;

    public TankDrill(Tank owner) {
        this.tank = owner;
    }
}

