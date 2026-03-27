/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.LavaFlow;
import com.cairn4.moonbase.tiles.Tile;
import java.util.ArrayList;

public class LavaFlowGenerator {
    World world;
    private boolean turnedLeft = false;
    private boolean turnedRight = false;

    public void spawnLavaFlow(World world, int x, int y) {
        GridPoint2 next;
        this.world = world;
        this.turnedLeft = false;
        this.turnedRight = false;
        int maxLength = 5;
        GridPoint2 currentPos = new GridPoint2(x, y);
        ArrayList<LavaFlow> flow = new ArrayList<LavaFlow>();
        GridPoint2 local = World.gridPointPool.obtain();
        World.convertWorldToLocal(local, x, y);
        Chunk chunk = this.getChunk(world, x, y);
        LavaFlow start = new LavaFlow(world, chunk, local.x, local.y);
        flow.add(start);
        GridPoint2 prev = World.getGridPointFromPoolAndSet(x, y);
        for (int i = maxLength; i > 0 && (next = this.getNextDirection(currentPos.x, currentPos.y, prev.x, prev.y)) != null; --i) {
            chunk = this.getChunk(world, next.x, next.y);
            World.convertWorldToLocal(local, next.x, next.y);
            LavaFlow l = new LavaFlow(world, chunk, local.x, local.y);
            flow.add(l);
            MoonBase.log("Next lava flow at : " + next.x + ", " + next.y);
            prev.set(currentPos.x, currentPos.y);
            currentPos.set(next.x, next.y);
        }
        World.gridPointPool.free(local);
    }

    private Chunk getChunk(World world, int worldX, int worldY) {
        GridPoint2 chunkGp = World.gridPointPool.obtain();
        World.convertWorldPosToChunkCoord(chunkGp, worldX, worldY);
        Chunk chunk = world.getChunk(chunkGp.x, chunkGp.y);
        World.gridPointPool.free(chunkGp);
        return chunk;
    }

    private GridPoint2 getNextDirection(int x, int y, int prevX, int prevY) {
        GridPoint2 north = World.getGridPointFromPoolAndSet(x, y + 1);
        GridPoint2 east = World.getGridPointFromPoolAndSet(x + 1, y);
        GridPoint2 south = World.getGridPointFromPoolAndSet(x, y - 1);
        GridPoint2 west = World.getGridPointFromPoolAndSet(x - 1, y);
        Tile northT = this.world.getTile(north.x, north.y);
        Tile eastT = this.world.getTile(east.x, east.y);
        Tile southT = this.world.getTile(south.x, south.y);
        Tile westT = this.world.getTile(west.x, west.y);
        ArrayList<GridPoint2> options = new ArrayList<GridPoint2>();
        if (this.adjacentLavaCheck(north.x, north.y) && (north.x != prevX || north.y != prevY)) {
            options.add(north);
        }
        if (this.adjacentLavaCheck(east.x, east.y) && (east.x != prevX || east.y != prevY)) {
            options.add(east);
        }
        if (this.adjacentLavaCheck(south.x, south.y) && (south.x != prevX || south.y != prevY)) {
            options.add(south);
        }
        if (this.adjacentLavaCheck(west.x, west.y) && (west.x != prevX || west.y != prevY)) {
            options.add(west);
        }
        MoonBase.log("-- valid options: " + options.size());
        if (!options.isEmpty()) {
            int r = MathUtils.random(0, options.size() - 1);
            MoonBase.log("-- r: " + r);
            return (GridPoint2)options.get(r);
        }
        return null;
    }

    private boolean adjacentLavaCheck(int worldX, int worldY) {
        ArrayList<GridPoint2> adjacent = Tile.GET_ADJACENT_COORDS(worldX, worldY, false);
        int lavaTouches = 0;
        for (GridPoint2 gp : adjacent) {
            Tile t = this.world.getTile(gp.x, gp.y);
            if (t == null || !(t instanceof LavaFlow)) continue;
            ++lavaTouches;
        }
        return lavaTouches <= 2;
    }

    public void newGen(World world, int startX, int startY) {
        int turnDir;
        boolean totalLength = false;
        boolean turnedLeft = false;
        boolean turnedRight = false;
        GridPoint2 localPos = World.getGridPointFromPoolAndSet(0, 0);
        GridPoint2 tilePos = new GridPoint2(startX, startY);
        this.createFlow(world, tilePos.x, tilePos.y);
        DIRECTION nextDir = this.getRandomDirection();
        int r = MathUtils.random(0, 1);
        for (r = 0; r < 1; ++r) {
            tilePos = this.moveDirection(tilePos, nextDir);
            this.createFlow(world, tilePos.x, tilePos.y);
        }
        int n = turnDir = MathUtils.randomBoolean() ? -1 : 1;
        if (turnDir == -1) {
            turnedLeft = true;
        } else {
            turnedRight = true;
        }
        nextDir = this.turnDirection(nextDir, turnDir);
        tilePos = this.moveDirection(tilePos, nextDir);
        this.createFlow(world, tilePos.x, tilePos.y);
        r = MathUtils.random(0, 2);
        for (r = 0; r < 1; ++r) {
            tilePos = this.moveDirection(tilePos, nextDir);
            this.createFlow(world, tilePos.x, tilePos.y);
        }
        turnDir = turnedLeft ? 1 : -1;
        nextDir = this.turnDirection(nextDir, turnDir);
        tilePos = this.moveDirection(tilePos, nextDir);
        this.createFlow(world, tilePos.x, tilePos.y);
        r = MathUtils.random(0, 2);
        for (r = 0; r < 1; ++r) {
            tilePos = this.moveDirection(tilePos, nextDir);
            this.createFlow(world, tilePos.x, tilePos.y);
        }
    }

    private boolean createFlow(World world, int worldX, int worldY) {
        Chunk chunk = this.getChunk(world, worldX, worldY);
        if (chunk != null) {
            GridPoint2 localPos = World.getGridPointFromPoolAndSet(0, 0);
            World.convertWorldToLocal(localPos, worldX, worldY);
            MoonBase.log("Creating lava flow at: " + worldX + ", " + worldY);
            LavaFlow l = new LavaFlow(world, chunk, localPos.x, localPos.y);
            return true;
        }
        return false;
    }

    private DIRECTION getRandomDirection() {
        int r = MathUtils.random(0, 3);
        switch (r) {
            case 0: {
                return DIRECTION.north;
            }
            case 1: {
                return DIRECTION.east;
            }
            case 2: {
                return DIRECTION.south;
            }
            case 3: {
                return DIRECTION.west;
            }
        }
        return DIRECTION.north;
    }

    private GridPoint2 moveDirection(GridPoint2 pos, DIRECTION dir) {
        GridPoint2 nextPos = new GridPoint2(pos);
        switch (dir) {
            case north: {
                nextPos.add(0, 1);
                break;
            }
            case east: {
                nextPos.add(1, 0);
                break;
            }
            case south: {
                nextPos.add(0, -1);
                break;
            }
            case west: {
                nextPos.add(-1, 0);
            }
        }
        return nextPos;
    }

    private DIRECTION turnDirection(DIRECTION dir, int turn) {
        int d = 0;
        switch (dir) {
            case north: {
                d = 0;
                break;
            }
            case east: {
                d = 1;
                break;
            }
            case south: {
                d = 2;
                break;
            }
            case west: {
                d = 3;
            }
        }
        if ((d += turn) < 0) {
            d = 3;
        } else if (d > 3) {
            d = 0;
        }
        switch (d) {
            case 0: {
                return DIRECTION.north;
            }
            case 1: {
                return DIRECTION.east;
            }
            case 2: {
                return DIRECTION.south;
            }
            case 3: {
                return DIRECTION.west;
            }
        }
        return DIRECTION.north;
    }

    private static enum DIRECTION {
        north,
        east,
        south,
        west;

    }
}

