/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.RepairDrone;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RepairDroneBase
extends BaseModule {
    private static ArrayList<BaseModule> basesBeingFixed = new ArrayList();
    private ArrayList<GridPoint2> tilesInRange = new ArrayList();
    private ArrayList<BaseModule> basesWithDisasters = new ArrayList();
    private static final float SEARCH_DELAY = 1.0f;
    private float searchTimer;
    public RepairDrone drone;

    public RepairDroneBase(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.walkable = true;
        this.powerPriority = 7;
        this.powerDrawRate = 4.0f;
        this.powerGenRate = 0.0f;
        this.setupPhysics("rtg");
        this.tilesInRange = Tile.GET_NEARBY_COORDS(this.worldX, this.worldY, 12);
        this.updateBases();
        this.statusGroup.toFront();
    }

    public void registerDrone(RepairDrone repairDrone) {
        Gdx.app.debug("MewnBase", "Registering drone entity with this base.");
        this.drone = repairDrone;
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "repairdronebase";
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("repairdronebase");
        this.image.setOrigin(1);
        this.image.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.image.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.drone == null) {
            this.drone = new RepairDrone(this.world, this.getXCenter(), this.getYCenter(), 0.0f);
            this.drone.homeBase = this;
        }
        if (this.hasPower) {
            this.searchTimer += delta;
            if (this.searchTimer > 1.0f) {
                this.searchTimer = 0.0f;
                this.sendDisasterToDrone();
            }
        }
    }

    public BaseModule getNextDisaster() {
        this.findDisasters();
        for (int i = 0; i < this.basesWithDisasters.size(); ++i) {
            if (basesBeingFixed.contains(this.basesWithDisasters.get(i))) continue;
            return this.basesWithDisasters.get(i);
        }
        return null;
    }

    public void sendDisasterToDrone() {
        this.basesWithDisasters.clear();
        this.findDisasters();
        if (this.drone.targetBase == null && this.basesWithDisasters.size() > 0) {
            if (!basesBeingFixed.contains(this.basesWithDisasters.get(0))) {
                this.drone.setTarget(this.basesWithDisasters.get(0));
                basesBeingFixed.add(this.basesWithDisasters.get(0));
                MoonBase.debug("RepairDroneBase: Setting drone target");
            } else {
                MoonBase.log("RepairDroneBase: another drone already repairing this.");
            }
        }
        for (int i = basesBeingFixed.size() - 1; i >= 0; --i) {
            if (RepairDroneBase.basesBeingFixed.get((int)i).baseDisaster != null) continue;
            basesBeingFixed.remove(i);
        }
    }

    private void findDisasters() {
        for (GridPoint2 a : this.tilesInRange) {
            Tile t = this.world.getTile(a.x, a.y);
            if (t == null || !(t instanceof BaseModule)) continue;
            BaseModule base = (BaseModule)t;
            if (base.baseDisaster == null || basesBeingFixed.contains(base)) continue;
            this.basesWithDisasters.add(base);
        }
        this.sortByClosestToDrone(this.basesWithDisasters);
    }

    public void sortByClosestToDrone(ArrayList<BaseModule> baseList) {
        final Vector2 dronePos = new Vector2(this.drone.getXPos(), this.drone.getYPos());
        Collections.sort(this.basesWithDisasters, new Comparator<BaseModule>(){

            @Override
            public int compare(BaseModule base1, BaseModule base2) {
                float dist2;
                float dist1 = dronePos.dst(base1.getXCenter(), base1.getYCenter());
                if (dist1 > (dist2 = dronePos.dst(base2.getXCenter(), base2.getYCenter()))) {
                    return 1;
                }
                if (dist1 < dist2) {
                    return -1;
                }
                return 0;
            }
        });
    }

    @Override
    public void dropPickup() {
        super.dropPickup();
        this.drone.selfDestruct();
    }

    @Override
    public void setPower(boolean on) {
        super.setPower(on);
        if (this.drone != null) {
            this.drone.setPowered(on);
        }
    }
}

