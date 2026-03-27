/*
 * Decompiled with CFR 0.152.
 */
package com.strongjoshua.console;

import com.badlogic.gdx.utils.reflect.Method;
import com.strongjoshua.console.Console;
import com.strongjoshua.console.annotation.HiddenCommand;

public final class ConsoleUtils {
    public static boolean canExecuteCommand(Console console, Method method) {
        return console.isExecuteHiddenCommandsEnabled() || !method.isAnnotationPresent(HiddenCommand.class);
    }

    public static boolean canDisplayCommand(Console console, Method method) {
        return console.isDisplayHiddenCommandsEnabled() || !method.isAnnotationPresent(HiddenCommand.class);
    }

    public static String exceptionToString(Throwable throwable) {
        StringBuilder result = new StringBuilder();
        for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
            if (result.length() == 0) {
                result.append("\nException in thread \"").append(Thread.currentThread().getName()).append("\" ");
            } else {
                result.append("\nCaused by: ");
            }
            result.append(cause.getClass().getCanonicalName()).append(": ").append(cause.getMessage());
            for (StackTraceElement traceElement : cause.getStackTrace()) {
                result.append("\n\tat ").append(traceElement.toString());
            }
        }
        return result.toString();
    }
}

