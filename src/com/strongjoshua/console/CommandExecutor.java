/*
 * Decompiled with CFR 0.152.
 */
package com.strongjoshua.console;

import com.badlogic.gdx.Gdx;
import com.strongjoshua.console.Console;
import com.strongjoshua.console.annotation.ConsoleDoc;

public class CommandExecutor {
    protected Console console;

    protected void setConsole(Console c) {
        this.console = c;
    }

    public final void printLog(String path) {
        this.console.printLogToFile(path);
    }

    @ConsoleDoc(description="Exits the application.")
    public final void exitApp() {
        Gdx.app.exit();
    }

    @ConsoleDoc(description="Shows all available methods.")
    public final void help() {
        this.console.printCommands();
    }

    @ConsoleDoc(description="Prints console docs for the given command.")
    public final void help(String command) {
        this.console.printHelp(command);
    }

    @ConsoleDoc(description="Deselects the console text field. Click to re-select.")
    public final void logView() {
        this.console.deselect();
    }
}

