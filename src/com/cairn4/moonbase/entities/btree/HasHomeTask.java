/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.NpcNeeds;

public class HasHomeTask
extends LeafTask<Npc> {
    boolean hasHome = false;

    @Override
    public void start() {
        System.out.println("Start hasHome");
    }

    @Override
    public Task.Status execute() {
        if (((Npc)this.getObject()).home != null) {
            ((Npc)this.getObject()).npcStatusIcon.hide();
            System.out.println("Has home");
            this.hasHome = true;
        } else {
            ((Npc)this.getObject()).npcStatusIcon.changeIcon(NpcNeeds.wantsHome);
            System.out.println("NO HOME :(");
            this.hasHome = false;
        }
        if (this.hasHome) {
            return Task.Status.SUCCEEDED;
        }
        return Task.Status.FAILED;
    }

    @Override
    public void end() {
        System.out.println("End Has Home\n");
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return null;
    }
}

