/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tests;

import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.tests.GdxTest;
import java.util.HashMap;

public class HashMapTest
extends GdxTest {
    HashMap<String, Integer> hashmap1 = new HashMap();
    private static StringBuilder stringBuilder = new StringBuilder();

    public HashMapTest() {
        int i;
        for (i = 0; i < 1000000; ++i) {
            Integer x = MathUtils.random(0, 1000000);
            stringBuilder.setLength(0);
            stringBuilder.append("").append(i);
            this.hashmap1.put(stringBuilder.toString(), x);
        }
        for (i = 0; i < 1000000; ++i) {
            Integer n = this.getIntegerSB(i);
        }
    }

    private Integer getInteger(int i) {
        return this.hashmap1.get("" + i);
    }

    private Integer getIntegerSB(int i) {
        stringBuilder.setLength(0);
        stringBuilder.append("").append(i);
        return this.hashmap1.get(stringBuilder.toString());
    }

    @Override
    public void create() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void render() {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void dispose() {
    }
}

