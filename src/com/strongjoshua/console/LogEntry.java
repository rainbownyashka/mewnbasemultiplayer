/*
 * Decompiled with CFR 0.152.
 */
package com.strongjoshua.console;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.TimeUtils;
import com.strongjoshua.console.LogLevel;

public class LogEntry {
    private String text;
    private LogLevel level;
    private long timeStamp;

    protected LogEntry(String msg, LogLevel level) {
        this.text = msg;
        this.level = level;
        this.timeStamp = TimeUtils.millis();
    }

    public Color getColor() {
        return this.level.getColor();
    }

    protected String toConsoleString() {
        String r = "";
        if (this.level.equals((Object)LogLevel.COMMAND)) {
            r = r + this.level.getIdentifier();
        }
        r = r + this.text;
        return r;
    }

    public String toString() {
        return this.timeStamp + ": " + this.level.getIdentifier() + this.text;
    }
}

