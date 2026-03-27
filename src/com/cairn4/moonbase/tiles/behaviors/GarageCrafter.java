/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.Gdx;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.entities.Buggie;
import com.cairn4.moonbase.entities.Tank;
import com.cairn4.moonbase.entities.Vehicle2;
import com.cairn4.moonbase.entities.VehicleTrailer;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;

public class GarageCrafter
extends ItemCrafter {
    Chunk chunk;
    int localX;
    int localY;

    @Override
    public void finishedBuilding() {
        Gdx.app.log("MewnBase", "GarageCrafter: Done building " + this.getCurrentBuildItem().getName());
        this.building = false;
        float spawnX = (float)(this.chunk.chunkX * 10 + this.localX) * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f;
        float spawnY = (float)(this.chunk.chunkY * 10 + this.localY) * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f;
        switch (this.getCurrentBuildItem().id) {
            case "buggie": {
                MoonBase.achievementAdapter.buildBuggie();
                Buggie v = new Buggie(this.chunk.world, spawnX, spawnY, -90.0f);
                v.spawnAnim();
                break;
            }
            case "tank": {
                MoonBase.achievementAdapter.buildBuggie();
                Tank tank = new Tank(this.chunk.world, spawnX, spawnY, -90.0f);
                tank.spawnAnim();
                break;
            }
            case "rover": {
                new Vehicle2(this.chunk.world, spawnX, spawnY, -90.0f);
                break;
            }
            case "trailer": {
                VehicleTrailer t = new VehicleTrailer(this.chunk.world, spawnX, spawnY, -90.0f);
                t.spawnAnim();
                break;
            }
            default: {
                Gdx.app.error("MewnBase", "GarageCrafter: unknown vehicle");
            }
        }
        this.setChanged();
        this.notifyObservers("finishAndAutoClose");
        this.buildQueue.removeIndex(0);
        this.spentItemBufferList.removeIndex(0);
        this.buildTimer = 0.0f;
    }

    public void setSpawnTile(Chunk c, int localX, int localY) {
        this.chunk = c;
        this.localX = localX;
        this.localY = localY;
    }
}

