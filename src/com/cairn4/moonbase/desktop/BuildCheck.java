/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.desktop;

import com.cairn4.moonbase.MoonBase;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class BuildCheck {
    public static void main(String[] arg) {
        boolean problem = false;
        SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy", Locale.getDefault());
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getDefault());
        String today = sdf.format(calendar.getTime());
        System.out.println("--------------------------");
        System.out.println("Build Check: 1.0.1");
        System.out.println("" + today);
        System.out.println("--------------------------");
        if (!"11/19/2023".equals(today)) {
            System.out.println("MoonBase.BUILD_DATE isn't set to today: 11/19/2023 vs " + today);
            problem = true;
        }
        if (MoonBase.GOD_MODE) {
            System.out.println("MoonBase.GOD_MODE needs to be set to FALSE");
            problem = true;
        }
        if (MoonBase.DEBUG_INFO) {
            System.out.println("MoonBase.DEBUG_INFO needs to be set to FALSE");
            problem = true;
        }
        if (MoonBase.PERF_GRAPH) {
            System.out.println("MoonBase.PERF_GRAPH needs to be set to FALSE");
            problem = true;
        }
        if (MoonBase.INSTANT_RUN) {
            System.out.println("MoonBase.INSTANT_RUN needs to be set to FALSE");
            problem = true;
        }
        System.out.println("--------------------------");
        if (problem) {
            System.out.println("THERE WAS A PROBLEM!");
        } else {
            System.out.println("All set!");
        }
    }
}

