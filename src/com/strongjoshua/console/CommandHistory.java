/*
 * Decompiled with CFR 0.152.
 */
package com.strongjoshua.console;

import com.badlogic.gdx.utils.Array;

public class CommandHistory {
    private final Array<String> commands = new Array(true, 20);
    private int index;

    public void store(String command) {
        if (this.commands.size > 0 && this.isLastCommand(command)) {
            return;
        }
        this.commands.insert(0, command);
        this.indexAtBeginning();
    }

    public String getPreviousCommand() {
        ++this.index;
        if (this.commands.size == 0) {
            this.indexAtBeginning();
            return "";
        }
        if (this.index >= this.commands.size) {
            this.index = 0;
        }
        return this.commands.get(this.index);
    }

    public String getNextCommand() {
        --this.index;
        if (this.commands.size <= 1 || this.index < 0) {
            this.indexAtBeginning();
            return "";
        }
        return this.commands.get(this.index);
    }

    private boolean isLastCommand(String command) {
        return command.equals(this.commands.first());
    }

    private void indexAtBeginning() {
        this.index = -1;
    }
}

