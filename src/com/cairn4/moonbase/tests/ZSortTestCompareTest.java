/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tests;

import com.cairn4.moonbase.tests.ZTestObj;
import java.util.Comparator;

public class ZSortTestCompareTest
implements Comparator<ZTestObj> {
    @Override
    public int compare(ZTestObj a1, ZTestObj a2) {
        if (a1 != null && a1.getUserObject() != null && a1.getUserObject() instanceof Float && a2 != null && a2.getUserObject() != null && a2.getUserObject() instanceof Float) {
            float z2;
            float z1 = a1.getUserObject().floatValue();
            if (z1 > (z2 = a2.getUserObject().floatValue())) {
                return -1;
            }
            if (z1 < z2) {
                return 1;
            }
            return 0;
        }
        if (a1 != null && a2 != null) {
            System.out.println("HUH??");
            return 0;
        }
        if (a1 != null) {
            return -1;
        }
        if (a2 != null) {
            return 1;
        }
        return 0;
    }
}

