/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.ItemPickup;
import java.util.HashMap;

public class EntityData {
    public String className;
    public float worldXPos;
    public float worldYPos;
    public float rotation;
    public String itemId;
    public int itemAmount;
    public HashMap<String, Object> properties = new HashMap();

    public EntityData() {
    }

    public EntityData(Entity e) {
        this.className = e.getClass().getName();
        this.worldXPos = e.getXPos();
        this.worldYPos = e.getYPos();
        this.rotation = e.getRotation();
        this.properties = e.getProperties();
        if (e instanceof ItemPickup) {
            ItemPickup i = (ItemPickup)e;
            this.itemId = i.item.id;
        }
    }
}

