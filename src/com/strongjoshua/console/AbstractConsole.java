/*
 * Decompiled with CFR 0.152.
 */
package com.strongjoshua.console;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.strongjoshua.console.CommandExecutor;
import com.strongjoshua.console.Console;
import com.strongjoshua.console.ConsoleUtils;
import com.strongjoshua.console.Log;
import com.strongjoshua.console.LogLevel;
import com.strongjoshua.console.annotation.ConsoleDoc;
import java.util.ArrayList;
import java.util.Collections;

public abstract class AbstractConsole
implements Console,
Disposable {
    protected final Log log = new Log();
    protected CommandExecutor exec;
    protected boolean logToSystem;
    protected boolean disabled;
    protected boolean executeHiddenCommands = true;
    protected boolean displayHiddenCommands = false;
    protected boolean consoleTrace = false;

    @Override
    public void setLoggingToSystem(Boolean log) {
        this.logToSystem = log;
    }

    @Override
    public void log(String msg, LogLevel level) {
        this.log.addEntry(msg, level);
        if (this.logToSystem) {
            switch (level) {
                case ERROR: {
                    System.err.println("> " + msg);
                    break;
                }
                default: {
                    System.out.println("> " + msg);
                }
            }
        }
    }

    @Override
    public void log(String msg) {
        this.log(msg, LogLevel.DEFAULT);
    }

    @Override
    public void log(Throwable exception, LogLevel level) {
        this.log(ConsoleUtils.exceptionToString(exception), level);
    }

    @Override
    public void log(Throwable exception) {
        this.log(exception, LogLevel.ERROR);
    }

    @Override
    public void printLogToFile(String file) {
        this.printLogToFile(Gdx.files.local(file));
    }

    @Override
    public void printLogToFile(FileHandle fh) {
        if (this.log.printToFile(fh)) {
            this.log("Successfully wrote logs to file.", LogLevel.SUCCESS);
        } else {
            this.log("Unable to write logs to file.", LogLevel.ERROR);
        }
    }

    @Override
    public boolean isDisabled() {
        return this.disabled;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public void setCommandExecutor(CommandExecutor commandExec) {
        this.exec = commandExec;
        this.exec.setConsole(this);
    }

    @Override
    public void execCommand(String command) {
        if (this.disabled) {
            return;
        }
        this.log(command, LogLevel.COMMAND);
        String[] parts = command.split(" ");
        String methodName = parts[0];
        String[] sArgs = null;
        if (parts.length > 1) {
            sArgs = new String[parts.length - 1];
            for (int i = 1; i < parts.length; ++i) {
                sArgs[i - 1] = parts[i];
            }
        }
        Class<?> clazz = this.exec.getClass();
        Method[] methods = ClassReflection.getMethods(clazz);
        Array<Integer> possible = new Array<Integer>();
        for (int i = 0; i < methods.length; ++i) {
            Method method = methods[i];
            if (!method.getName().equalsIgnoreCase(methodName) || !ConsoleUtils.canExecuteCommand(this, method)) continue;
            possible.add(i);
        }
        if (possible.size <= 0) {
            this.log("No such method found.", LogLevel.ERROR);
            return;
        }
        int size = possible.size;
        int numArgs = sArgs == null ? 0 : sArgs.length;
        for (int i = 0; i < size; ++i) {
            Method m = methods[(Integer)possible.get(i)];
            Class[] params = m.getParameterTypes();
            if (numArgs != params.length) continue;
            try {
                Object[] args;
                block20: {
                    args = null;
                    try {
                        if (sArgs == null) break block20;
                        args = new Object[numArgs];
                        for (int j = 0; j < params.length; ++j) {
                            Class param = params[j];
                            String value = sArgs[j];
                            if (param.equals(String.class)) {
                                args[j] = value;
                                continue;
                            }
                            if (param.equals(Boolean.class) || param.equals(Boolean.TYPE)) {
                                args[j] = Boolean.parseBoolean(value);
                                continue;
                            }
                            if (param.equals(Byte.class) || param.equals(Byte.TYPE)) {
                                args[j] = Byte.parseByte(value);
                                continue;
                            }
                            if (param.equals(Short.class) || param.equals(Short.TYPE)) {
                                args[j] = Short.parseShort(value);
                                continue;
                            }
                            if (param.equals(Integer.class) || param.equals(Integer.TYPE)) {
                                args[j] = Integer.parseInt(value);
                                continue;
                            }
                            if (param.equals(Long.class) || param.equals(Long.TYPE)) {
                                args[j] = Long.parseLong(value);
                                continue;
                            }
                            if (param.equals(Float.class) || param.equals(Float.TYPE)) {
                                args[j] = Float.valueOf(Float.parseFloat(value));
                                continue;
                            }
                            if (!param.equals(Double.class) && !param.equals(Double.TYPE)) continue;
                            args[j] = Double.parseDouble(value);
                        }
                    }
                    catch (Exception e) {
                        continue;
                    }
                }
                m.setAccessible(true);
                m.invoke(this.exec, args);
                return;
            }
            catch (ReflectionException e) {
                String msg = e.getMessage();
                if (msg == null || msg.length() <= 0) {
                    msg = "Unknown Error";
                    e.printStackTrace();
                }
                this.log(msg, LogLevel.ERROR);
                if (this.consoleTrace) {
                    this.log(e, LogLevel.ERROR);
                }
                return;
            }
        }
        // Fallback: if no exact arg match, but a single-String method exists, join all args into one
        if (numArgs > 1) {
            for (int i = 0; i < size; ++i) {
                Method m = methods[(Integer)possible.get(i)];
                Class[] params = m.getParameterTypes();
                if (params.length != 1 || !params[0].equals(String.class)) continue;
                try {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < numArgs; ++j) {
                        if (j > 0) sb.append(' ');
                        sb.append(sArgs[j]);
                    }
                    Object[] args = new Object[]{sb.toString()};
                    m.setAccessible(true);
                    m.invoke(this.exec, args);
                    return;
                } catch (ReflectionException e) {
                    String msg = e.getMessage();
                    if (msg == null || msg.length() <= 0) {
                        msg = "Unknown Error";
                        e.printStackTrace();
                    }
                    this.log(msg, LogLevel.ERROR);
                    if (this.consoleTrace) {
                        this.log(e, LogLevel.ERROR);
                    }
                    return;
                }
            }
        }
        this.log("Bad parameters. Check your code.", LogLevel.ERROR);
    }

    private ArrayList<Method> getAllMethods() {
        ArrayList<Method> methods = new ArrayList<Method>();
        for (Class<?> c = this.exec.getClass(); c != Object.class; c = c.getSuperclass()) {
            Collections.addAll(methods, ClassReflection.getDeclaredMethods(c));
        }
        return methods;
    }

    @Override
    public void printCommands() {
        for (Method m : this.getAllMethods()) {
            if (!m.isPublic() || !ConsoleUtils.canDisplayCommand(this, m)) continue;
            String s = "";
            s = s + m.getName();
            s = s + " : ";
            Class[] params = m.getParameterTypes();
            for (int i = 0; i < params.length; ++i) {
                s = s + params[i].getSimpleName();
                if (i >= params.length - 1) continue;
                s = s + ", ";
            }
            this.log(s);
        }
    }

    @Override
    public void printHelp(String command) {
        boolean found = false;
        for (Method m : this.getAllMethods()) {
            if (!m.getName().equals(command)) continue;
            found = true;
            StringBuilder sb = new StringBuilder();
            sb.append(m.getName()).append(": ");
            Annotation annotation = m.getDeclaredAnnotation(ConsoleDoc.class);
            if (annotation != null) {
                ConsoleDoc doc = annotation.getAnnotation(ConsoleDoc.class);
                sb.append(doc.description());
                Class[] params = m.getParameterTypes();
                for (int i = 0; i < params.length; ++i) {
                    sb.append("\n");
                    for (int j = 0; j < m.getName().length() + 2; ++j) {
                        sb.append(" ");
                    }
                    sb.append(params[i].getSimpleName()).append(": ");
                    if (i >= doc.paramDescriptions().length) continue;
                    sb.append(doc.paramDescriptions()[i]);
                }
            } else {
                Class[] params = m.getParameterTypes();
                for (int i = 0; i < params.length; ++i) {
                    sb.append(params[i].getSimpleName());
                    if (i >= params.length - 1) continue;
                    sb.append(", ");
                }
            }
            this.log(sb.toString());
        }
        if (!found) {
            this.log("Command does not exist.");
        }
    }

    @Override
    public void setExecuteHiddenCommands(boolean enabled) {
        this.executeHiddenCommands = enabled;
    }

    @Override
    public boolean isExecuteHiddenCommandsEnabled() {
        return this.executeHiddenCommands;
    }

    @Override
    public void setDisplayHiddenCommands(boolean enabled) {
        this.displayHiddenCommands = enabled;
    }

    @Override
    public boolean isDisplayHiddenCommandsEnabled() {
        return this.displayHiddenCommands;
    }

    @Override
    public void setConsoleStackTrace(boolean enabled) {
        this.consoleTrace = enabled;
    }

    @Override
    public void setMaxEntries(int numEntries) {
    }

    @Override
    public void clear() {
    }

    @Override
    public void setSize(int width, int height) {
    }

    @Override
    public void setSizePercent(float wPct, float hPct) {
    }

    @Override
    public void setPosition(int x, int y) {
    }

    @Override
    public void setPositionPercent(float xPosPct, float yPosPct) {
    }

    @Override
    public void resetInputProcessing() {
    }

    @Override
    public InputProcessor getInputProcessor() {
        return null;
    }

    @Override
    public void draw() {
    }

    @Override
    public void refresh() {
    }

    @Override
    public void refresh(boolean retain) {
    }

    @Override
    public int getDisplayKeyID() {
        return 0;
    }

    @Override
    public void setDisplayKeyID(int code) {
    }

    @Override
    public boolean hitsConsole(float screenX, float screenY) {
        return false;
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
    }

    @Override
    public void select() {
    }

    @Override
    public void deselect() {
    }

    @Override
    public void setTitle(String title) {
    }

    @Override
    public void setHoverAlpha(float alpha) {
    }

    @Override
    public void setNoHoverAlpha(float alpha) {
    }

    @Override
    public void setHoverColor(Color color) {
    }

    @Override
    public void setNoHoverColor(Color color) {
    }

    @Override
    public void enableSubmitButton(boolean enable) {
    }

    @Override
    public void setSubmitText(String text) {
    }

    @Override
    public Window getWindow() {
        return null;
    }
}
