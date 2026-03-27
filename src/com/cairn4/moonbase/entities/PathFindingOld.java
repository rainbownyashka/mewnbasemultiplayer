/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.PathNodeOld;
import com.cairn4.moonbase.tiles.Tile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PathFindingOld {
    public static boolean DRAWDEBUG = false;
    private int searchRadius;
    private ArrayList<PathNodeOld> allNodes = new ArrayList();
    private ArrayList<PathNodeOld> openNodes = new ArrayList();
    private ArrayList<PathNodeOld> closedNodes = new ArrayList();
    private ArrayList<PathNodeOld> finalPath = new ArrayList();
    public World level;
    private PathNodeOld startNode;
    public PathNodeOld targetNode;
    private PathNodeOld currentNode;
    ArrayList<Vector2> pathDirections;
    ShapeRenderer renderer;
    boolean finished;

    public PathFindingOld(World l) {
        this.level = l;
        this.searchRadius = 10;
        this.renderer = new ShapeRenderer();
    }

    private void addAllTiles(int startx, int starty) {
        this.allNodes.clear();
        ArrayList<GridPoint2> coordList = Tile.GET_NEARBY_COORDS(startx, starty, this.searchRadius);
        for (GridPoint2 gp : coordList) {
            this.allNodes.add(new PathNodeOld(this.level, gp.x, gp.y, this));
        }
    }

    public void resetNodes() {
        this.startNode = null;
        this.targetNode = null;
        this.openNodes.clear();
        this.closedNodes.clear();
        this.finalPath.clear();
        for (PathNodeOld p : this.allNodes) {
            p.reset();
        }
    }

    public ArrayList<PathNodeOld> getPath(GridPoint2 start, GridPoint2 target) {
        this.addAllTiles(start.x, start.y);
        this.finished = false;
        this.resetNodes();
        this.startNode = this.getNode(start.x, start.y);
        this.targetNode = this.getNode(target.x, target.y);
        if (this.targetNode == null) {
            System.out.println("NO TARGET TILE!");
            return null;
        }
        this.openNodes.add(this.startNode);
        this.currentNode = this.getNode(this.startNode.x, this.startNode.y);
        this.startNode.setG(0);
        this.startNode.setF(this.startNode.calcH());
        this.addToClosed(this.startNode);
        ArrayList<Object> adjacent = new ArrayList();
        adjacent = this.getAdjacentNodes(this.startNode);
        for (PathNodeOld pathNodeOld : adjacent) {
            this.addToOpen(pathNodeOld);
            pathNodeOld.parentTile = this.currentNode;
            pathNodeOld.setG(this.startNode.getG() + 1);
            pathNodeOld.setF(pathNodeOld.getG() + pathNodeOld.calcH());
        }
        while (!this.finished) {
            this.step();
        }
        System.out.println("Finished!\n-------------------");
        this.finalPath = this.generateFinalPath();
        return this.finalPath;
    }

    public void printPath(ArrayList<PathNodeOld> path) {
        for (int i = 0; i < path.size(); ++i) {
            System.out.println("path: " + i + ": " + path.get((int)i).x + "," + path.get((int)i).y);
        }
    }

    private ArrayList<PathNodeOld> generateFinalPath() {
        ArrayList<PathNodeOld> path = new ArrayList<PathNodeOld>();
        PathNodeOld n = this.getNode(this.targetNode.x, this.targetNode.y);
        path.add(n);
        while (n.parentTile != null) {
            n = this.getNode(n.parentTile.x, n.parentTile.y);
            path.add(n);
        }
        return path;
    }

    private ArrayList<DIRECTIONS> convertToDirectionList(ArrayList<PathNodeOld> revPath) {
        ArrayList<DIRECTIONS> directionPath = new ArrayList<DIRECTIONS>();
        Collections.reverse(revPath);
        for (int i = 0; i < revPath.size(); ++i) {
            Vector2 cur;
            Vector2 x2 = Vector2.Zero;
            if (i >= revPath.size() - 1) continue;
            x2 = new Vector2(revPath.get(i + 1).getX(), revPath.get(i + 1).getY());
            Vector2 dir = x2.sub(cur = new Vector2(revPath.get(i).getX(), revPath.get(i).getY()));
            if (dir.equals(new Vector2(0.0f, 1.0f))) {
                directionPath.add(DIRECTIONS.up);
                continue;
            }
            if (dir.equals(new Vector2(0.0f, -1.0f))) {
                directionPath.add(DIRECTIONS.down);
                continue;
            }
            if (dir.equals(new Vector2(1.0f, 0.0f))) {
                directionPath.add(DIRECTIONS.right);
                continue;
            }
            if (dir.equals(new Vector2(-1.0f, 0.0f))) {
                directionPath.add(DIRECTIONS.left);
                continue;
            }
            Gdx.app.log("digger", " --- bad direction: " + dir + " ---");
        }
        return directionPath;
    }

    public void step() {
        this.currentNode = this.getLowestOpenF();
        ArrayList<Object> adjacent = new ArrayList();
        adjacent = this.getAdjacentNodes(this.currentNode);
        adjacent.clear();
        adjacent = this.getAdjacentNodes(this.currentNode);
        this.addToClosed(this.currentNode);
        for (PathNodeOld pathNodeOld : adjacent) {
            if (this.nodeInClosedList(pathNodeOld)) continue;
            if (!this.nodeInOpenList(pathNodeOld)) {
                this.addToOpen(pathNodeOld);
                pathNodeOld.parentTile = this.getNode(this.currentNode.x, this.currentNode.y);
                pathNodeOld.setG(this.currentNode.getG() + 1);
                pathNodeOld.setF(pathNodeOld.getG() + pathNodeOld.calcH());
                continue;
            }
            int newG = this.currentNode.getG() + 1;
            if (newG >= pathNodeOld.getG()) continue;
            pathNodeOld.parentTile = this.getNode(this.currentNode.x, this.currentNode.y);
            pathNodeOld.setG(newG);
            pathNodeOld.setF(pathNodeOld.getG() + pathNodeOld.calcH());
        }
        if (this.openNodes.size() == 0 || this.nodeInClosedList(this.targetNode) || this.currentNode.calcH() == 0) {
            this.finished = true;
        }
    }

    private PathNodeOld getLowestOpenF() {
        int l = 99999;
        PathNodeOld lowest = null;
        long seed = System.nanoTime();
        Collections.shuffle(this.openNodes, new Random(seed));
        for (PathNodeOld n : this.openNodes) {
            if (n.getF() >= l) continue;
            lowest = n;
            l = n.f;
        }
        return lowest;
    }

    private ArrayList<PathNodeOld> getAdjacentNodes(PathNodeOld current) {
        ArrayList<PathNodeOld> adjacent = new ArrayList<PathNodeOld>();
        adjacent.clear();
        int x = current.getX();
        int y = current.getY();
        PathNodeOld pn = this.getNode(x, y + 1);
        if (pn != null && pn.walkable && !pn.closed) {
            adjacent.add(pn);
        } else {
            this.addToClosed(pn);
        }
        PathNodeOld ps = this.getNode(x, y - 1);
        if (ps != null && ps.walkable && !ps.closed) {
            adjacent.add(ps);
        } else {
            this.addToClosed(ps);
        }
        PathNodeOld pe = this.getNode(x + 1, y);
        if (pe != null && pe.walkable && !pe.closed) {
            adjacent.add(pe);
        } else {
            this.addToClosed(pe);
        }
        PathNodeOld pw = this.getNode(x - 1, y);
        if (pw != null && pw.walkable && !pw.closed) {
            adjacent.add(pw);
        } else {
            this.addToClosed(pw);
        }
        return adjacent;
    }

    public PathNodeOld getNode(int x, int y) {
        for (PathNodeOld p : this.allNodes) {
            if (p.x != x || p.y != y) continue;
            return p;
        }
        return null;
    }

    private boolean nodeInOpenList(PathNodeOld p) {
        boolean inList = false;
        for (PathNodeOld pn : this.openNodes) {
            if (!p.equals(pn)) continue;
            inList = true;
        }
        return inList;
    }

    private boolean nodeInClosedList(PathNodeOld p) {
        boolean inList = false;
        for (PathNodeOld pn : this.closedNodes) {
            if (!p.equals(pn)) continue;
            inList = true;
        }
        return inList;
    }

    private void addToClosed(PathNodeOld node) {
        if (node != null) {
            this.openNodes.remove(node);
            this.closedNodes.add(node);
            node.closed = true;
        }
    }

    private boolean addToOpen(PathNodeOld node) {
        if (!this.openNodes.contains(node)) {
            this.openNodes.add(node);
            return false;
        }
        return true;
    }

    public void render() {
        this.renderer.setProjectionMatrix(this.level.gameScreen.camera.combined);
        this.renderer.begin(ShapeRenderer.ShapeType.Filled);
        this.renderer.setColor(Color.RED);
        if (this.level.gameScreen.world.player != null) {
            this.renderer.circle(this.level.gameScreen.world.player.getXPos(), this.level.gameScreen.world.player.getYPos(), 20.0f);
        } else {
            System.out.println("no player");
        }
        for (int i = 0; i < this.finalPath.size(); ++i) {
            PathNodeOld node = this.finalPath.get(i);
            Vector2 pos = new Vector2((float)node.getX() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)node.getY() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
            this.renderer.rect(pos.x, pos.y, 15.0f, 15.0f);
        }
        this.renderer.end();
        this.renderer.begin(ShapeRenderer.ShapeType.Line);
        if (this.finalPath.size() > 0) {
            PathNodeOld prevNode = this.finalPath.get(0);
            Vector2 prevNodePos = new Vector2((float)this.finalPath.get(0).getX() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)this.finalPath.get(0).getY() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
            for (int i = 1; i < this.finalPath.size(); ++i) {
                PathNodeOld node = this.finalPath.get(i);
                Vector2 pos = new Vector2((float)node.getX() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)node.getY() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
                this.renderer.line(prevNodePos.x, prevNodePos.y, pos.x, pos.y);
                prevNode = node;
                prevNodePos = pos;
            }
        }
        this.renderer.end();
    }

    public static enum DIRECTIONS {
        up,
        down,
        left,
        right;

    }
}

