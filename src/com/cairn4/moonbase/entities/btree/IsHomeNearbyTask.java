/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.entities.Npc;

public class IsHomeNearbyTask
extends LeafTask<Npc> {
    @Override
    public Task.Status execute() {
        Vector2 homePos;
        float tileDist;
        Npc npc = (Npc)this.getObject();
        if (npc.home != null && (tileDist = (homePos = new Vector2(npc.home.worldX, npc.home.worldY)).dst(npc.getWorldXTile(), npc.getWorldYTile())) < 9.0f) {
            return Task.Status.SUCCEEDED;
        }
        return Task.Status.FAILED;
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return null;
    }
}

