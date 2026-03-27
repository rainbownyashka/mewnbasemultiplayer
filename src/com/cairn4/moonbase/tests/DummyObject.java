/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tests;

import com.badlogic.gdx.math.MathUtils;

public class DummyObject {
    public String name;
    public int value;
    String[] letters = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public DummyObject() {
        for (int i = 0; i < 3; ++i) {
            this.name = this.name + this.letters[MathUtils.random(0, this.letters.length - 1)];
        }
        this.value = MathUtils.random(9000);
    }
}

