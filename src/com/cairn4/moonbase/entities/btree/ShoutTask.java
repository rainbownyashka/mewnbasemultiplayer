/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.cairn4.moonbase.entities.BtreeBlob;

public class ShoutTask
extends LeafTask<BtreeBlob> {
    @TaskAttribute
    public float thingy = 0.0f;

    @Override
    public void start() {
        super.start();
    }

    @Override
    public Task.Status execute() {
        BtreeBlob blob = (BtreeBlob)this.getObject();
        blob.shout();
        return Task.Status.SUCCEEDED;
    }

    @Override
    protected Task<BtreeBlob> copyTo(Task<BtreeBlob> task) {
        ShoutTask shout = (ShoutTask)task;
        return task;
    }
}

