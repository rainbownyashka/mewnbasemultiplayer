/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.NpcNeeds;
import com.cairn4.moonbase.tiles.NpcHome;

public class HomeIsNiceTask
extends LeafTask<Npc> {
    @Override
    public Task.Status execute() {
        NpcHome home = ((Npc)this.getObject()).home;
        if (home.hasAir && home.hasPower) {
            return Task.Status.SUCCEEDED;
        }
        ((Npc)this.getObject()).npcStatusIcon.changeIcon(NpcNeeds.sadHome);
        return Task.Status.FAILED;
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return null;
    }
}

