/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tests;

public class LambdaTest {
    public static void main(String[] arg) {
        Doer adder = (a, b) -> a + b;
        int r = adder.takeTwo(4, 5);
        adder = (a, b) -> a - b;
        r = adder.takeTwo(4, 5);
        System.out.println(r);
    }

    static interface Doer {
        public int takeTwo(int var1, int var2);
    }
}

