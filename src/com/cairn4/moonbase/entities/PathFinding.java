/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.PathSmoother;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.TileNode;
import com.cairn4.moonbase.entities.TiledRaycastCollisionDetector;
import com.cairn4.moonbase.entities.TiledSmoothableGraphPath;
import com.cairn4.moonbase.entities.WorldGraph;
import com.cairn4.moonbase.tiles.Gate;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.GameScreen;
import java.util.ArrayList;

public class PathFinding {
    WorldGraph worldGraph;
    World world;
    TileNode startNode;
    TileNode endNode;
    GraphPath<TileNode> finalPath;
    public TiledSmoothableGraphPath<TileNode> smoothablePath;
    PathSmoother<TileNode, Vector2> pathSmoother;
    int pathStartX;
    int pathStartY;
    int searchRadius;
    ArrayList<GridPoint2> coordList = new ArrayList();
    ArrayList<GridPoint2> validList = new ArrayList();
    ArrayList<GridPoint2> checkList = new ArrayList();

    public PathFinding(World world) {
        this.world = world;
        this.worldGraph = new WorldGraph();
    }

    public GraphPath<TileNode> getPath(GridPoint2 startTile, GridPoint2 endTile, int searchRadius) {
        this.addNearbyTilesToGraph(startTile.x, startTile.y, endTile.x, endTile.y, searchRadius);
        if (this.smoothablePath != null) {
            this.smoothablePath.clear();
        }
        if (this.worldGraph == null) {
            MoonBase.error("Null world graph");
        }
        if (this.endNode == null) {
            MoonBase.error("Null end node");
            return null;
        }
        if (this.worldGraph.getNode(this.endNode.x, this.endNode.y) != null && this.worldGraph.getNode(startTile.x, startTile.y) != null) {
            this.makeConnections();
            this.smoothablePath = this.worldGraph.findPath(this.startNode, this.endNode);
            if (this.smoothablePath != null && this.smoothablePath.nodes.size > 0) {
                this.pathSmoother = new PathSmoother(new TiledRaycastCollisionDetector(this.worldGraph));
                this.pathSmoother.smoothPath(this.smoothablePath);
                return this.smoothablePath;
            }
            MoonBase.error("No smooothable path found");
        }
        return null;
    }

    private void checkListNeighbors(GridPoint2 parent) {
        MoonBase.log("Testing: " + parent.toString() + " (" + this.checkList.size() + ")");
        this.validList.add(parent);
        this.checkNeighbor(parent.x, parent.y, 0, 1);
        this.checkNeighbor(parent.x, parent.y, 0, -1);
        this.checkNeighbor(parent.x, parent.y, 1, 0);
        this.checkNeighbor(parent.x, parent.y, -1, 0);
    }

    private void checkNeighbor(int x, int y, int xDir, int yDir) {
        GridPoint2 gp = World.getGridPointFromPoolAndSet(x + xDir, y + yDir);
        if (this.isGridPointInList(gp.x, gp.y, this.coordList) && !this.isGridPointInList(gp.x, gp.y, this.checkList) && this.isWalkable(gp)) {
            this.checkList.add(gp);
            if (!this.isGridPointInList(gp.x, gp.y, this.validList)) {
                this.validList.add(gp);
            }
        }
    }

    private boolean isGridPointInList(int x, int y, ArrayList<GridPoint2> list) {
        for (GridPoint2 gp : list) {
            if (gp.x != x || gp.y != y) continue;
            return true;
        }
        return false;
    }

    private boolean isWalkable(GridPoint2 gp) {
        boolean tileIsWalkable = true;
        Tile t = this.world.getTile(gp.x, gp.y);
        if (t != null) {
            if (t instanceof Gate) {
                Gate g = (Gate)t;
                if (!g.toggleBehavior.on) {
                    tileIsWalkable = false;
                }
            } else if (t.hasPhysicsBody()) {
                tileIsWalkable = false;
            }
        }
        return tileIsWalkable;
    }

    private void addNearbyTilesToGraph(int startX, int startY, int endX, int endY, int searchRadius) {
        this.pathStartX = startX;
        this.pathStartY = startY;
        this.searchRadius = searchRadius;
        this.worldGraph.clearGraph();
        this.coordList.clear();
        this.coordList = Tile.GET_NEARBY_COORDS(startX, startY, searchRadius);
        this.validList.clear();
        this.checkList.clear();
        GridPoint2 q = new GridPoint2(startX, startY);
        this.checkList.add(q);
        int tests = 0;
        while (!this.checkList.isEmpty()) {
            GridPoint2 n = this.checkList.get(0);
            this.checkList.remove(0);
            if (this.isWalkable(n)) {
                this.checkNeighbor(n.x, n.y, 0, 1);
                this.checkNeighbor(n.x, n.y, 0, -1);
                this.checkNeighbor(n.x, n.y, 1, 0);
                this.checkNeighbor(n.x, n.y, -1, 0);
            }
            if (++tests <= this.coordList.size()) continue;
            break;
        }
        float closestDist = 9999999.0f;
        GridPoint2 closestGp = World.getGridPointFromPoolAndSet(startX, startY);
        for (GridPoint2 gp : this.validList) {
            this.worldGraph.addTileNode(new TileNode(this.world, gp.x, gp.y));
            float dist = gp.dst(endX, endY);
            if (!(dist < closestDist)) continue;
            closestDist = dist;
            closestGp = gp;
        }
        this.startNode = this.worldGraph.getNode(startX, startY);
        this.endNode = this.worldGraph.getNode(closestGp.x, closestGp.y);
        for (int i = this.coordList.size() - 1; i >= 0; --i) {
            World.gridPointPool.free(this.coordList.get(i));
        }
        World.gridPointPool.free(closestGp);
    }

    private void makeConnections() {
        for (int i = 0; i < this.worldGraph.tileNodes.size; ++i) {
            TileNode t = this.worldGraph.tileNodes.get(i);
            int conns = 0;
            TileNode north = this.worldGraph.getNode(t.x, t.y + 1);
            TileNode south = this.worldGraph.getNode(t.x, t.y - 1);
            TileNode east = this.worldGraph.getNode(t.x + 1, t.y);
            TileNode west = this.worldGraph.getNode(t.x - 1, t.y);
            if (!(north == null || north.tileRef != null && north.tileRef.blocksSouth)) {
                this.worldGraph.connectPathNodes(t, north);
                ++conns;
            }
            if (!(south == null || south.tileRef != null && south.tileRef.blocksNorth)) {
                this.worldGraph.connectPathNodes(t, south);
                ++conns;
            }
            if (!(east == null || east.tileRef != null && east.tileRef.blocksWest)) {
                this.worldGraph.connectPathNodes(t, east);
                ++conns;
            }
            if (west == null || west.tileRef != null && west.tileRef.blocksEast) continue;
            this.worldGraph.connectPathNodes(t, west);
            ++conns;
        }
    }

    public void render() {
        if (this.smoothablePath != null && this.smoothablePath.nodes.size > 0) {
            for (int i = 0; i < this.smoothablePath.nodes.size; ++i) {
                if (i >= this.smoothablePath.nodes.size - 1) continue;
                TileNode first = (TileNode)this.smoothablePath.nodes.get(i);
                TileNode next = (TileNode)this.smoothablePath.nodes.get(i + 1);
                GameScreen cfr_ignored_0 = this.world.gameScreen;
                GameScreen.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                GameScreen cfr_ignored_1 = this.world.gameScreen;
                GameScreen.shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
                GameScreen cfr_ignored_2 = this.world.gameScreen;
                GameScreen.shapeRenderer.rectLine((float)first.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)first.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)next.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)next.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, 4.0f);
                GameScreen cfr_ignored_3 = this.world.gameScreen;
                GameScreen.shapeRenderer.end();
            }
            for (TileNode n : this.smoothablePath.nodes) {
                GameScreen cfr_ignored_4 = this.world.gameScreen;
                n.render(GameScreen.shapeRenderer, true);
            }
        }
    }

    public void cancel() {
        MoonBase.debug("Pathfinding: cancelling pathing");
        this.smoothablePath = null;
    }
}

