/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.worlddata.BehaviorData;

public interface Behavior {
    public void setLoaded(boolean var1);

    public boolean isLoaded();

    public void setId(String var1);

    public String getId();

    public void start();

    public void update(float var1);

    public void interact(Player var1);

    public void onDestroy();

    public BehaviorData getData();
}

