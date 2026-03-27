/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.SmoothableGraphPath;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.entities.TileNode;

public class TiledSmoothableGraphPath<N extends TileNode>
extends DefaultGraphPath<N>
implements SmoothableGraphPath<N, Vector2> {
    private Vector2 tmpPosition = new Vector2();

    @Override
    public Vector2 getNodePosition(int index) {
        TileNode node = (TileNode)this.nodes.get(index);
        return this.tmpPosition.set(node.x, node.y);
    }

    @Override
    public void swapNodes(int index1, int index2) {
        this.nodes.set(index1, (TileNode)this.nodes.get(index2));
    }

    @Override
    public void truncatePath(int newLength) {
        this.nodes.truncate(newLength);
    }
}

