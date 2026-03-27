/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.ai.btree;

import com.badlogic.gdx.ai.btree.Task;

public interface TaskCloner {
    public <T> Task<T> cloneTask(Task<T> var1);

    public <T> void freeTask(Task<T> var1);
}

