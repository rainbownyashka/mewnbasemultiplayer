/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.tiles.NpcHome;

public class HomeHasPowerTask
extends LeafTask<Npc> {
    @Override
    public Task.Status execute() {
        NpcHome home = ((Npc)this.getObject()).home;
        if (home != null && home.hasPower && home.hasAir) {
            return Task.Status.SUCCEEDED;
        }
        return Task.Status.FAILED;
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        HomeHasPowerTask newTask = (HomeHasPowerTask)task;
        return newTask;
    }
}

