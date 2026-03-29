/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.AssetLoaderParameters;

public class AssetManagerSingleton {
    private static AssetManagerSingleton instance;
    private static AssetManager assetManager;

    private AssetManagerSingleton() {
        assetManager = new AssetManager() {
            @Override
            public synchronized <T> void load(String fileName, Class<T> type) {
                if (fileName != null && fileName.startsWith("Tiles/test/")) {
                    System.out.println("[AssetManager] load " + fileName + " type=" + type);
                    new Exception("Tiles/test load stack").printStackTrace(System.out);
                }
                super.load(fileName, type);
            }

            @Override
            public synchronized <T> void load(String fileName, Class<T> type, AssetLoaderParameters<T> parameter) {
                if (fileName != null && fileName.startsWith("Tiles/test/")) {
                    System.out.println("[AssetManager] load " + fileName + " type=" + type);
                    new Exception("Tiles/test load stack").printStackTrace(System.out);
                }
                super.load(fileName, type, parameter);
            }
        };
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManagerSingleton();
        }
        return assetManager;
    }
}
