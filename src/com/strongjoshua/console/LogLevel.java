/*
 * Decompiled with CFR 0.152.
 */
package com.strongjoshua.console;

import com.badlogic.gdx.graphics.Color;

public enum LogLevel {
    DEFAULT(new Color(1.0f, 1.0f, 1.0f, 1.0f), ""),
    ERROR(new Color(0.8509804f, 0.0f, 0.0f, 1.0f), "Error: "),
    SUCCESS(new Color(0.0f, 0.8509804f, 0.0f, 1.0f), "Success! "),
    COMMAND(new Color(1.0f, 1.0f, 1.0f, 1.0f), "> ");

    private Color color;
    private String identifier;

    private LogLevel(Color c, String identity) {
        this.color = c;
        this.identifier = identity;
    }

    Color getColor() {
        return this.color;
    }

    String getIdentifier() {
        return this.identifier;
    }
}

