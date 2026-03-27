/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

public class EventData {
    final String name;
    int intValue;
    float floatValue;
    String stringValue;
    String audioPath;
    float volume;
    float balance;

    public EventData(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null.");
        }
        this.name = name;
    }

    public int getInt() {
        return this.intValue;
    }

    public void setInt(int intValue) {
        this.intValue = intValue;
    }

    public float getFloat() {
        return this.floatValue;
    }

    public void setFloat(float floatValue) {
        this.floatValue = floatValue;
    }

    public String getString() {
        return this.stringValue;
    }

    public void setString(String stringValue) {
        if (stringValue == null) {
            throw new IllegalArgumentException("stringValue cannot be null.");
        }
        this.stringValue = stringValue;
    }

    public String getAudioPath() {
        return this.audioPath;
    }

    public void setAudioPath(String audioPath) {
        if (audioPath == null) {
            throw new IllegalArgumentException("audioPath cannot be null.");
        }
        this.audioPath = audioPath;
    }

    public float getVolume() {
        return this.volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getBalance() {
        return this.balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }
}

