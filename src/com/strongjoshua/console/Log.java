/*
 * Decompiled with CFR 0.152.
 */
package com.strongjoshua.console;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.strongjoshua.console.LogEntry;
import com.strongjoshua.console.LogLevel;
import java.io.IOException;
import java.io.Writer;

public class Log {
    private Array<LogEntry> logEntries = new Array();
    private int numEntries = -1;

    protected Log() {
    }

    public void setMaxEntries(int numEntries) {
        this.numEntries = numEntries;
    }

    protected void addEntry(String msg, LogLevel level) {
        this.logEntries.add(new LogEntry(msg, level));
        if (this.logEntries.size > this.numEntries && this.numEntries != -1) {
            this.logEntries.removeIndex(0);
        }
    }

    protected Array<LogEntry> getLogEntries() {
        return this.logEntries;
    }

    public boolean printToFile(FileHandle fh) {
        if (fh.isDirectory()) {
            throw new IllegalArgumentException("File cannot be a directory!");
        }
        Writer out = null;
        try {
            out = fh.writer(false);
        }
        catch (Exception e) {
            return false;
        }
        String toWrite = "";
        for (LogEntry l : this.logEntries) {
            toWrite = toWrite + l.toString() + "\n";
        }
        try {
            out.write(toWrite);
            out.close();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

