/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.cairn4.moonbase.entities.Npc;

public class CanPathToHomeTask
extends LeafTask<Npc> {
    @Override
    public Task.Status execute() {
        Npc npc = (Npc)this.getObject();
        if (npc.home != null) {
            // empty if block
        }
        return null;
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return null;
    }
}

