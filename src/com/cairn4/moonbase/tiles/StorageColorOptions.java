/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;

public class StorageColorOptions {
    public static Color[] colors = new Color[]{Color.WHITE, Color.valueOf("cc0000ff"), Color.valueOf("bf4c00ff"), Color.valueOf("e5b200ff"), Color.valueOf("197200cc"), Color.valueOf("1972ffcc"), Color.valueOf("8c00c9f2"), Color.valueOf("cc26cc8c"), Color.valueOf("4f1e00ff"), Color.BLACK};
    public static int editColorIndex = 0;

    public static void overrideColor(int index, Color c) {
        StorageColorOptions.colors[index] = c;
    }
}

