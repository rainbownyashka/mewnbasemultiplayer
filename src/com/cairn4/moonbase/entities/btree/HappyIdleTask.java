/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.cairn4.moonbase.entities.Npc;

public class HappyIdleTask
extends LeafTask<Npc> {
    @Override
    public void start() {
        ((Npc)this.getObject()).spineActor.setAnimation(0, "eat", false);
    }

    @Override
    public Task.Status execute() {
        System.out.println("HAPPY IDLE");
        return Task.Status.SUCCEEDED;
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

