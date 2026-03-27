/*
 * Decompiled with CFR 0.152.
 */
package com.strongjoshua.console;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.strongjoshua.console.CommandExecutor;
import com.strongjoshua.console.ConsoleUtils;

public class CommandCompleter {
    private ObjectSet<String> possibleCommands = new ObjectSet();
    private ObjectSet.ObjectSetIterator<String> iterator;
    private String setString = "";

    public void set(CommandExecutor ce, String s) {
        this.reset();
        this.setString = s.toLowerCase();
        Array<Method> methods = this.getAllMethods(ce);
        for (Method m : methods) {
            String name = m.getName();
            if (!name.toLowerCase().startsWith(this.setString) || !ConsoleUtils.canDisplayCommand(ce.console, m)) continue;
            this.possibleCommands.add(name);
        }
        this.iterator = new ObjectSet.ObjectSetIterator<String>(this.possibleCommands);
    }

    public void reset() {
        this.possibleCommands.clear();
        this.setString = "";
        this.iterator = null;
    }

    public boolean isNew() {
        return this.possibleCommands.size == 0;
    }

    public boolean wasSetWith(String s) {
        return this.setString.equalsIgnoreCase(s);
    }

    public String next() {
        if (!this.iterator.hasNext) {
            this.iterator.reset();
            return this.setString;
        }
        return this.iterator.next();
    }

    private Array<Method> getAllMethods(CommandExecutor ce) {
        Method[] ms;
        Array<Method> methods = new Array<Method>();
        for (Method m : ms = ClassReflection.getDeclaredMethods(ce.getClass())) {
            if (!m.isPublic()) continue;
            methods.add(m);
        }
        for (Method m : ms = ClassReflection.getDeclaredMethods(ce.getClass().getSuperclass())) {
            if (!m.isPublic()) continue;
            methods.add(m);
        }
        return methods;
    }
}

