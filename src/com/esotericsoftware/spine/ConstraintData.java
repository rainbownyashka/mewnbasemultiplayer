/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

public abstract class ConstraintData {
    final String name;
    int order;
    boolean skinRequired;

    public ConstraintData(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null.");
        }
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean getSkinRequired() {
        return this.skinRequired;
    }

    public void setSkinRequired(boolean skinRequired) {
        this.skinRequired = skinRequired;
    }

    public String toString() {
        return this.name;
    }
}

