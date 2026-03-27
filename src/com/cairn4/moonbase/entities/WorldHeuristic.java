/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.entities.TileNode;

public class WorldHeuristic
implements Heuristic<TileNode> {
    @Override
    public float estimate(TileNode node, TileNode endNode) {
        return Vector2.dst(node.x, node.y, endNode.x, endNode.y);
    }
}

