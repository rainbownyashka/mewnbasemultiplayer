/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.desktop.DesktopAdapter;
import com.cairn4.moonbase.desktop.achievements.SteamAchievementAdapter;
import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DesktopLauncher {
    public static DesktopAdapter desktopAdapter;
    public static SteamAchievementAdapter achievementAdapter;

    public static void main(String[] arg) {
        desktopAdapter = new DesktopAdapter();
        DesktopLauncher.desktopAdapter.achivementManager = achievementAdapter = new SteamAchievementAdapter(desktopAdapter);
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        boolean serverOnly = "1".equals(System.getProperty("mewnbase.serverOnly"));
        if (serverOnly) {
            config.setWindowedMode(1, 1);
            config.setWindowPosition(-10000, -10000);
            config.setDecorated(false);
            config.setResizable(false);
            config.setAutoIconify(false);
            config.setInitialVisible(false);
            config.setForegroundFPS(30);
            config.setIdleFPS(30);
            config.useVsync(false);
            config.disableAudio(true);
            config.setTitle("MewnBase Server");
        } else {
            config.setWindowedMode(1280, 720);
            config.setHdpiMode(HdpiMode.Logical);
            config.setForegroundFPS(60);
            config.useVsync(true);
            config.setResizable(true);
            config.setTitle("MewnBase");
            config.setWindowIcon(com.badlogic.gdx.Files.FileType.Local, "app_icon_16x16.png", "app_icon_32x32.png");
            config.setAutoIconify(true);
        }
        String coreFolder = "data/";
        System.out.println("MewnBase v1.0.1 | OS: " + System.getProperty("os.name"));
        System.out.println("MewnBase launch args: ");
        for (int a = 0; a < arg.length; ++a) {
            System.out.println(arg[a] + " ");
            String[] split = arg[a].split("=");
            if (!split[0].equals("coreFolder")) continue;
            coreFolder = split[1];
        }
        try {
            new Lwjgl3Application(new MoonBase(desktopAdapter, achievementAdapter, coreFolder), config);
        }
        catch (Throwable e) {
            e.printStackTrace();
            String result = "";
            result = result + "MewnBase Crashlog v1.0.1\n";
            result = result + "Build 1\n";
            result = result + new SimpleDateFormat("yyyy-M-d_h.mm.ssa").format(new Date()) + "\n";
            result = MoonBase.STEAM_VERSION ? result + "Steam Version\n" : result + "Itch.io Version\n";
            result = result + System.getProperty("os.arch") + " | " + System.getProperty("os.name") + " | " + System.getProperty("os.version") + "\n\n";
            result = result + coreFolder + "\n\n";
            if (MoonBase.currentSaveFolder != null && !MoonBase.currentSaveFolder.equals("")) {
                result = result + "Current save: " + MoonBase.currentSaveFolder + "\n";
            }
            result = result + coreFolder + "\n\n";
            String errorMsg = DesktopLauncher.parseException(e, true);
            boolean failed = false;
            result = result + errorMsg;
            String filename = "";
            try {
                filename = "crashlog_v1.0.1_" + new SimpleDateFormat("yyyy-M-d_h.mm.ssa").format(new Date()) + ".txt";
                java.nio.file.Files.write(Paths.get(filename, new String[0]), result.getBytes(), new OpenOption[0]);
            }
            catch (IOException i) {
                i.printStackTrace();
                failed = true;
            }
        }
    }

    public static String parseException(Throwable e, boolean stacktrace) {
        StringBuilder build = new StringBuilder();
        while (e.getCause() != null) {
            e = e.getCause();
        }
        String name = ClassReflection.getSimpleName(e.getClass()).replace("Exception", "");
        build.append(name);
        if (e.getMessage() != null) {
            build.append(": ");
            build.append(e.getMessage());
        }
        if (stacktrace) {
            for (StackTraceElement s : e.getStackTrace()) {
                build.append("\n");
                build.append(s.toString());
            }
        }
        return build.toString();
    }
}
