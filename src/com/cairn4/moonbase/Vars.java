/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.graphics.Color;
import java.text.DecimalFormat;

public class Vars {
    public static DecimalFormat threeDecimal = new DecimalFormat("#.000");
    public static DecimalFormat singleDecimal = new DecimalFormat("0.0");
    public static DecimalFormat threeDecimals = new DecimalFormat("0.000");
    public static DecimalFormat wholeNum = new DecimalFormat("0");
    public static DecimalFormat bigNum = new DecimalFormat("###,###,###,##0");
    public static final Color WARNING_RED = new Color(0.96f, 0.2f, 0.11f, 1.0f);
    public static final String WARNING_RED_HEX = "#f5331c";
    public static Color[] habColors = new Color[]{Color.WHITE, Color.valueOf("e51600ff"), Color.valueOf("fc8a25ff"), Color.valueOf("f9dd07ff"), Color.valueOf("5dd332ff"), Color.valueOf("289fffff"), Color.valueOf("8c00c9f2"), Color.valueOf("ffaac58c"), Color.valueOf("6d430cff"), Color.valueOf("10110fff")};
    public static Color[] vehicleColors = new Color[]{Color.WHITE, Color.valueOf("f73e27ff"), Color.valueOf("fc7300ff"), Color.valueOf("ffb336ff"), Color.valueOf("75d34dff"), Color.valueOf("579cffff"), Color.valueOf("8d3ef9ff"), Color.valueOf("ff5196ff"), Color.valueOf("7a3a1bff"), Color.valueOf("10110fff")};
    public static Color[] paintbrushUIColors = new Color[]{Color.WHITE, Color.valueOf("d1533fff"), Color.valueOf("d36f36ff"), Color.valueOf("d59a41ff"), Color.valueOf("79ad4eff"), Color.valueOf("6286c7ff"), Color.valueOf("8b4ec2ff"), Color.valueOf("d66381ff"), Color.valueOf("774a33ff"), Color.valueOf("2d2a28ff")};
    public static float BYTES_TO_MEGABYTES = 1.0E-6f;
    public static final int craftingBaseModulePowerPrio = 4;
    public static final float waterPerBottle = 60.0f;
    public static final float waterPerIce = 30.0f;
    public static final Color markedLocationColor = Color.valueOf("1db9ed");
}

