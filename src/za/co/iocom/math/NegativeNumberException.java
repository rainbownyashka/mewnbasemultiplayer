/*
 * Decompiled with CFR 0.152.
 */
package za.co.iocom.math;

class NegativeNumberException
extends Exception {
    int num;

    NegativeNumberException() {
        this.num = 0;
    }

    NegativeNumberException(int n) {
        this.num = n;
    }
}

