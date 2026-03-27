/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.cairn4.moonbase.entities.Npc;

public class InteractTask
extends LeafTask<Npc> {
    @Override
    public Task.Status execute() {
        System.out.println("Victory punch");
        ((Npc)this.getObject()).interactWithTile();
        return Task.Status.SUCCEEDED;
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return null;
    }
}

