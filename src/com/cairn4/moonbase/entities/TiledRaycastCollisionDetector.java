/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.entities.TileNode;
import com.cairn4.moonbase.entities.WorldGraph;

public class TiledRaycastCollisionDetector<N extends TileNode>
implements RaycastCollisionDetector<Vector2> {
    WorldGraph worldMap;

    public TiledRaycastCollisionDetector(WorldGraph worldMap) {
        this.worldMap = worldMap;
    }

    @Override
    public boolean collides(Ray<Vector2> ray) {
        int tmp;
        boolean steep;
        int x0 = (int)((Vector2)ray.start).x;
        int y0 = (int)((Vector2)ray.start).y;
        int x1 = (int)((Vector2)ray.end).x;
        int y1 = (int)((Vector2)ray.end).y;
        boolean bl = steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
        if (steep) {
            tmp = x0;
            x0 = y0;
            y0 = tmp;
            tmp = x1;
            x1 = y1;
            y1 = tmp;
        }
        if (x0 > x1) {
            tmp = x0;
            x0 = x1;
            x1 = tmp;
            tmp = y0;
            y0 = y1;
            y1 = tmp;
        }
        int deltax = x1 - x0;
        int deltay = Math.abs(y1 - y0);
        int error = 0;
        int y = y0;
        int ystep = y0 < y1 ? 1 : -1;
        for (int x = x0; x <= x1; ++x) {
            TileNode tile = steep ? this.worldMap.getNode(y, x) : this.worldMap.getNode(x, y);
            boolean check = this.checkTileCollision(tile);
            if (check) {
                return true;
            }
            TileNode tileN = steep ? this.worldMap.getNode(y + 1, x) : this.worldMap.getNode(x, y + 1);
            boolean checkN = this.checkTileCollision(tileN);
            if (checkN) {
                return true;
            }
            TileNode tileE = steep ? this.worldMap.getNode(y, x + 1) : this.worldMap.getNode(x + 1, y);
            boolean checkE = this.checkTileCollision(tileE);
            if (checkE) {
                return true;
            }
            if ((error += deltay) + error < deltax) continue;
            y += ystep;
            error -= deltax;
        }
        return false;
    }

    @Override
    public boolean findCollision(Collision<Vector2> outputCollision, Ray<Vector2> inputRay) {
        throw new UnsupportedOperationException();
    }

    private boolean checkTileCollision(TileNode tileNode) {
        if (tileNode == null) {
            return true;
        }
        return tileNode.tileRef != null && tileNode.tileRef.hasPhysicsBody();
    }
}

