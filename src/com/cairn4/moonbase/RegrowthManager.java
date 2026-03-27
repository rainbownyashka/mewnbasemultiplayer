/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.RespawningItemDropper;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.worlddata.RespawningItemDropperData;
import java.util.ArrayList;

public class RegrowthManager {
    World world;
    Array<RespawningItemDropper> respawnList = new Array();

    public RegrowthManager(World world) {
        this.world = world;
    }

    public void addRespawnable(String dropperId, int worldX, int worldY) {
        Gdx.app.log("MewnBase", "RegrowthManager: adding new respawnable - " + dropperId);
        this.respawnList.add(new RespawningItemDropper(this.world, dropperId, worldX, worldY));
    }

    public void update(float delta) {
        for (int i = this.respawnList.size - 1; i >= 0; --i) {
            this.respawnList.get(i).update(delta);
            if (!this.respawnList.get((int)i).readyToRemove) continue;
            this.respawnList.removeIndex(i);
        }
    }

    public ArrayList<RespawningItemDropperData> saveData() {
        ArrayList<RespawningItemDropperData> data = new ArrayList<RespawningItemDropperData>();
        for (RespawningItemDropper r : this.respawnList) {
            data.add(r.getData());
        }
        return data;
    }

    public void loadData(ArrayList<RespawningItemDropperData> loadedData) {
        for (RespawningItemDropperData r : loadedData) {
            RespawningItemDropper rid = new RespawningItemDropper(this.world, r.dropperId, r.originalWorldX, r.originalWorldY);
            rid.timer = rid.waitTime * r.timerPercent;
            this.respawnList.add(rid);
        }
        Gdx.app.log("MewnBase", "Loaded " + this.respawnList.size + " respawning item droppers.");
        loadedData.clear();
    }
}

