/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.cairn4.moonbase.entities.Npc;

public class SadTask
extends LeafTask<Npc> {
    int step;
    int stepLimit = 10;

    @Override
    public void start() {
        this.step = 0;
    }

    @Override
    public Task.Status execute() {
        ++this.step;
        if (this.step < this.stepLimit) {
            System.out.println("Sad " + this.step);
            return Task.Status.RUNNING;
        }
        System.out.println("Sad finished");
        return Task.Status.SUCCEEDED;
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return null;
    }
}

