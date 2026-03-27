/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.desktop.DesktopAdapter;
import com.cairn4.moonbase.desktop.achievements.SteamAchievementAdapter;

public class DesktopLauncherMac {
    public static DesktopAdapter desktopAdapter;
    public static SteamAchievementAdapter achievementAdapter;

    public static void main(String[] arg) {
        desktopAdapter = new DesktopAdapter();
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1280, 720);
        config.setHdpiMode(HdpiMode.Logical);
        config.useVsync(true);
        config.setResizable(true);
        config.setTitle("MewnBase");
        config.setWindowIcon(Files.FileType.Local, "app_icon_16x16.png", "app_icon_32x32.png");
        new Lwjgl3Application(new MoonBase(desktopAdapter, achievementAdapter, ""), config);
    }
}

