/*
 * Decompiled with CFR 0.152.
 */
package com.strongjoshua.console;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.strongjoshua.console.CommandExecutor;
import com.strongjoshua.console.LogLevel;

public interface Console {
    public static final int UNLIMITED_ENTRIES = -1;

    public void setMaxEntries(int var1);

    public void clear();

    public void setSize(int var1, int var2);

    public void setLoggingToSystem(Boolean var1);

    public void setSizePercent(float var1, float var2);

    public void setPosition(int var1, int var2);

    public void setPositionPercent(float var1, float var2);

    public void resetInputProcessing();

    public InputProcessor getInputProcessor();

    public void draw();

    public void refresh();

    public void refresh(boolean var1);

    public void log(String var1, LogLevel var2);

    public void log(String var1);

    public void log(Throwable var1);

    public void log(Throwable var1, LogLevel var2);

    public void printLogToFile(String var1);

    public void printLogToFile(FileHandle var1);

    public void printCommands();

    public void printHelp(String var1);

    public boolean isDisabled();

    public void setDisabled(boolean var1);

    public int getDisplayKeyID();

    public void setDisplayKeyID(int var1);

    public void setCommandExecutor(CommandExecutor var1);

    public void execCommand(String var1);

    public boolean hitsConsole(float var1, float var2);

    public void dispose();

    public boolean isVisible();

    public void setVisible(boolean var1);

    public void setExecuteHiddenCommands(boolean var1);

    public void setDisplayHiddenCommands(boolean var1);

    public boolean isExecuteHiddenCommandsEnabled();

    public boolean isDisplayHiddenCommandsEnabled();

    public void setConsoleStackTrace(boolean var1);

    public void select();

    public void deselect();

    public void setTitle(String var1);

    public void setHoverAlpha(float var1);

    public void setNoHoverAlpha(float var1);

    public void setHoverColor(Color var1);

    public void setNoHoverColor(Color var1);

    public void enableSubmitButton(boolean var1);

    public void setSubmitText(String var1);

    public Window getWindow();
}

