/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool;

public class HudNotificationData
implements Pool.Poolable {
    public String icon = "";
    public String message = "";
    public Color textColor = Color.WHITE;

    @Override
    public void reset() {
        this.icon = "";
        this.message = "";
        this.textColor = Color.WHITE;
    }
}

