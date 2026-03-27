/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.techtree;

import java.util.ArrayList;

public class TechUpgrade {
    public String id;
    public boolean unlocked = false;
    public int cost;
    public ArrayList<String> preReqTech = new ArrayList();
    public int suitLevel;
    public boolean baseUpgrade = false;
}

