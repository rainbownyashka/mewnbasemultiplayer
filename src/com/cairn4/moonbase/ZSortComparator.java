/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.cairn4.moonbase.MoonBase;
import java.util.Comparator;

public class ZSortComparator
implements Comparator<Actor> {
    @Override
    public int compare(Actor a1, Actor a2) {
        if (a1 != null && a2 != null) {
            if (a1.getUserObject() == null) {
                a1.setUserObject(Float.valueOf(a1.getY(1)));
            }
            if (a2.getUserObject() == null) {
                a2.setUserObject(Float.valueOf(a2.getY(1)));
            }
            float z1 = 0.0f;
            float z2 = 0.0f;
            try {
                z1 = ((Float)a1.getUserObject()).floatValue();
            }
            catch (Exception e) {
                MoonBase.error("ZSortComparator: a1 obj isn't able to cast " + a1.getUserObject().getClass().getSimpleName() + " to float");
                a1.setUserObject(Float.valueOf(a1.getY(1)));
            }
            try {
                z2 = ((Float)a2.getUserObject()).floatValue();
            }
            catch (Exception e) {
                MoonBase.error("ZSortComparator: a2 obj isn't able to cast " + a2.getUserObject().getClass().getSimpleName() + " to float");
                a2.setUserObject(Float.valueOf(a2.getY(1)));
            }
            return Float.compare(z2, z1);
        }
        return 0;
    }
}

