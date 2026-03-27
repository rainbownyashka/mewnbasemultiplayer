/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.desktop.achievements;

public class Stat {
    public String name;
    public statTypes type;
    public int iValue;
    public float fValue;
    public float fAvgNumerator;
    public float fAvgDenominator;

    public Stat(String name, statTypes t) {
        this.name = name;
        this.type = t;
    }

    public static enum statTypes {
        stat_int,
        stat_float,
        stat_avgrate;

    }
}

