/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.utils;

import com.badlogic.gdx.files.FileHandle;
import java.util.Comparator;

public class FileDateModifiedComparator
implements Comparator {
    public int compare(Object o1, Object o2) {
        if (o1 instanceof FileHandle && o2 instanceof FileHandle) {
            FileHandle f1 = (FileHandle)o1;
            FileHandle f2 = (FileHandle)o2;
            if (f1.lastModified() > f2.lastModified()) {
                return -1;
            }
            if (f1.lastModified() < f2.lastModified()) {
                return 1;
            }
            return 0;
        }
        return 0;
    }
}

