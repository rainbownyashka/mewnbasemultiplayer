/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.cairn4.moonbase.entities.TileNode;
import com.cairn4.moonbase.entities.TileNodeConnection;
import com.cairn4.moonbase.entities.TiledSmoothableGraphPath;
import com.cairn4.moonbase.entities.WorldHeuristic;

public class WorldGraph
implements IndexedGraph<TileNode> {
    WorldHeuristic worldHeuristic = new WorldHeuristic();
    Array<TileNode> tileNodes = new Array();
    Array<TileNodeConnection> tileNodeConnections = new Array();
    ObjectMap<TileNode, Array<Connection<TileNode>>> connectionMap = new ObjectMap();
    private int lastNodeIndex = 0;

    public void addTileNode(TileNode tileNode) {
        tileNode.index = this.lastNodeIndex++;
        this.tileNodes.add(tileNode);
    }

    public TileNode getNode(int worldX, int worldY) {
        for (TileNode t : this.tileNodes) {
            if (t.x != worldX || t.y != worldY) continue;
            return t;
        }
        return null;
    }

    public void connectPathNodes(TileNode fromNode, TileNode toNode) {
        TileNodeConnection connection = new TileNodeConnection(fromNode, toNode);
        if (!this.connectionMap.containsKey(fromNode)) {
            this.connectionMap.put(fromNode, new Array());
        }
        this.connectionMap.get(fromNode).add(connection);
        this.tileNodeConnections.add(connection);
    }

    public TiledSmoothableGraphPath<TileNode> findPath(TileNode startNode, TileNode goalNode) {
        TiledSmoothableGraphPath<TileNode> nodePath = new TiledSmoothableGraphPath<TileNode>();
        new IndexedAStarPathFinder<TileNode>(this).searchNodePath(startNode, goalNode, this.worldHeuristic, nodePath);
        return nodePath;
    }

    public void clearGraph() {
        this.tileNodes.clear();
        this.tileNodeConnections.clear();
        this.connectionMap.clear();
    }

    @Override
    public int getIndex(TileNode node) {
        if (node == null) {
            System.out.println("node is null");
        }
        return node.index;
    }

    @Override
    public int getNodeCount() {
        return this.lastNodeIndex;
    }

    @Override
    public Array<Connection<TileNode>> getConnections(TileNode fromNode) {
        if (this.connectionMap.containsKey(fromNode)) {
            return this.connectionMap.get(fromNode);
        }
        return new Array<Connection<TileNode>>(0);
    }

    public TileNode getClosetToTarget(int targetX, int targetY) {
        float closestDist = 999999.0f;
        TileNode closestNode = null;
        for (TileNode n : this.tileNodes) {
            float d = n.distanceToTarget(targetX, targetY);
            if (!(d < closestDist)) continue;
            closestNode = n;
            closestDist = d;
        }
        return closestNode;
    }
}

