/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities.btree;

import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.NpcFriendState;
import com.cairn4.moonbase.tiles.Tile;

public class PathToHomeTask
extends LeafTask<Npc> {
    @Override
    public void start() {
        Npc npc = (Npc)this.getObject();
        npc.pathToHome();
        npc.spineActor.state.setAnimation(0, "walk_right", true);
    }

    @Override
    public Task.Status execute() {
        Npc npc = (Npc)this.getObject();
        if (npc.home != null && npc.pathFinding.smoothablePath != null) {
            Vector2 npcPos = new Vector2(npc.getXPos(), npc.getYPos());
            if (npcPos.dst(npc.home.getXCenter(), npc.home.getYCenter()) < Tile.TILE_SIZE * 2.0f) {
                System.out.println("Got close enough! SUCCESS");
                npc.pathFinding.cancel();
                npc.friendStateMachine.changeState(NpcFriendState.FRIEND_IDLE);
                return Task.Status.SUCCEEDED;
            }
            System.out.println("Pathing Running");
            return Task.Status.RUNNING;
        }
        System.out.println("Failed, no home to path to.");
        return Task.Status.FAILED;
    }

    @Override
    protected Task<Npc> copyTo(Task<Npc> task) {
        return null;
    }
}

