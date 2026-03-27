/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.assets.AssetManager;

public class AssetManagerSingleton {
    private static AssetManagerSingleton instance;
    private static AssetManager assetManager;

    private AssetManagerSingleton() {
        assetManager = new AssetManager();
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManagerSingleton();
        }
        return assetManager;
    }
}

