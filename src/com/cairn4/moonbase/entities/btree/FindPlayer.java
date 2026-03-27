/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.cairn4.moonbase.entities.BtreeBlob;
import com.cairn4.moonbase.entities.btree.ShoutTask;

public class FindPlayer
extends LeafTask<BtreeBlob> {
    @Override
    public void start() {
        super.start();
    }

    @Override
    public Task.Status execute() {
        BtreeBlob blob = (BtreeBlob)this.getObject();
        boolean foundTarget = blob.findTarget();
        return foundTarget ? Task.Status.SUCCEEDED : Task.Status.FAILED;
    }

    @Override
    protected Task<BtreeBlob> copyTo(Task<BtreeBlob> task) {
        ShoutTask shout = (ShoutTask)task;
        return task;
    }
}

