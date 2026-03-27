/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.worlddata.ItemDropperData;
import com.cairn4.moonbase.worlddata.ItemDropperFactory;
import com.cairn4.moonbase.worlddata.RespawningItemDropperData;
import java.util.ArrayList;

class RespawningItemDropper {
    private World world;
    boolean readyToRemove = false;
    String dropperId;
    int originalWorldX;
    int originalWorldY;
    private ArrayList<GridPoint2> allTilesNearby = new ArrayList();
    private static int searchRadius = 4;
    private static int searchAttempts = 10;
    private static float minPlayerDistance = Tile.TILE_SIZE * 10.0f;
    float waitTime;
    float timer;
    private Vector2 regrowthPlayerPos;

    public RespawningItemDropper(World world, String dropperId, int worldX, int worldY) {
        this.world = world;
        this.dropperId = dropperId;
        this.originalWorldX = worldX;
        this.originalWorldY = worldY;
        this.timer = 0.0f;
        ItemDropperData idd = ItemDropperFactory.getInstance().getItemDropperData(dropperId);
        if (idd != null) {
            this.waitTime = idd.daysToRespawn * 360.0f;
        } else {
            Gdx.app.error("MewnBase", "RegrowthManager: can't find item dropper id");
            this.readyToRemove = true;
        }
    }

    public void update(float delta) {
        this.timer += delta;
        if (this.timer > this.waitTime && !this.readyToRemove) {
            this.finished();
        }
    }

    private void finished() {
        this.allTilesNearby = Tile.GET_NEARBY_COORDS(this.originalWorldX, this.originalWorldY, searchRadius);
        boolean playerFarEnough = this.checkPlayerDistance();
        boolean clearOfBases = this.clearOfNearbyBases();
        if (playerFarEnough && clearOfBases) {
            GridPoint2 newLocation = this.pickNewLocation();
            if (newLocation != null) {
                Gdx.app.debug("MewnBase", "RegrowthManager: adding new dropper (" + this.dropperId + ") to: " + newLocation.x + ", " + newLocation.y);
                ItemDropperFactory.getInstance().newDropper(this.world, newLocation.x, newLocation.y, this.dropperId);
            }
            this.readyToRemove = true;
        } else {
            this.tryAgainLater();
        }
    }

    private boolean checkPlayerDistance() {
        if (this.world.player != null) {
            if (this.regrowthPlayerPos == null) {
                this.regrowthPlayerPos = new Vector2(0.0f, 0.0f);
            }
            this.regrowthPlayerPos.set(this.world.player.getXPos(), this.world.player.getYPos());
            float distance = this.regrowthPlayerPos.dst((float)this.originalWorldX * Tile.TILE_SIZE, (float)this.originalWorldY * Tile.TILE_SIZE);
            if (distance < minPlayerDistance) {
                Gdx.app.debug("MewnBase", "RegrowthManager. Player too close to regrow: " + distance);
            }
            return distance >= minPlayerDistance;
        }
        return true;
    }

    private boolean clearOfNearbyBases() {
        for (GridPoint2 gp : this.allTilesNearby) {
            Tile t;
            if (this.world.isTileEmpty(gp.x, gp.y) || !((t = this.world.getTile(gp.x, gp.y)) instanceof BaseModule)) continue;
            Gdx.app.debug("MewnBase", "RegrowthManager: Base too close to regrow");
            return false;
        }
        return true;
    }

    private GridPoint2 pickNewLocation() {
        for (int attempts = 0; attempts < searchAttempts; ++attempts) {
            int r = MathUtils.random(0, this.allTilesNearby.size() - 1);
            int x = this.allTilesNearby.get((int)r).x;
            int y = this.allTilesNearby.get((int)r).y;
            if (this.world.isTileEmpty(x, y) && this.world.isFloorEmpty(x, y)) {
                return this.allTilesNearby.get(r);
            }
            this.allTilesNearby.remove(r);
        }
        return null;
    }

    private void tryAgainLater() {
        Gdx.app.debug("MewnBase", "Regrowth item: trying again later");
        this.timer = 0.0f;
    }

    public RespawningItemDropperData getData() {
        RespawningItemDropperData d = new RespawningItemDropperData();
        d.dropperId = this.dropperId;
        d.originalWorldX = this.originalWorldX;
        d.originalWorldY = this.originalWorldY;
        d.timerPercent = this.timer / this.waitTime;
        return d;
    }
}

