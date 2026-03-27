/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Json;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.NpcDef;
import com.cairn4.moonbase.entities.NpcDefList;
import com.cairn4.moonbase.tiles.NpcHome;
import java.util.ArrayList;

public class NpcManager {
    private static NpcManager instance;
    public ArrayList<NpcDef> npcDefList = new ArrayList();
    public ArrayList<Npc> activeNpcList = new ArrayList();

    public static NpcManager getInstance() {
        if (instance == null) {
            instance = new NpcManager();
        }
        return instance;
    }

    private NpcManager() {
        this.loadData();
    }

    private void loadData() {
        if (this.npcDefList.size() == 0) {
            FileHandle fileHandle = Gdx.files.local(MoonBase.coreFolder + "npcs.json");
            if (!fileHandle.exists()) {
                Gdx.app.error("MewnBase", "npcs.json does not exist");
            } else {
                Gdx.app.log("MewnBase", "NpcManager: Reading data");
                Json json = new Json();
                String fileText = fileHandle.readString();
                this.npcDefList = json.fromJson(NpcDefList.class, (String)fileText).npcDefs;
                Gdx.app.log("MewnBase", "NpcManager: List size: " + this.npcDefList.size());
            }
        }
    }

    public String getAvailableNpcId(int x, int y) {
        String npcId = null;
        if (this.npcDefList.size() == this.activeNpcList.size()) {
            System.out.println("No more NPCs to spawn");
            return null;
        }
        GridPoint2 testPoint = new GridPoint2(x, y);
        float distanceToSpawn = testPoint.dst(500, 505);
        for (NpcDef npcDef : this.npcDefList) {
            boolean npcAlreadyExists = this.npcAlreadySpawned(npcDef.npcId);
            if (npcAlreadyExists || !((float)npcDef.minDistFromSpawn < distanceToSpawn) || !(distanceToSpawn < (float)npcDef.maxDistFromSpawn)) continue;
            System.out.println("Can spawn " + npcDef.npcId + " at distance " + distanceToSpawn);
            return npcDef.npcId;
        }
        if (npcId == null) {
            System.out.println("Couldn't find any available npcs to spawn at distance: " + distanceToSpawn);
        }
        return npcId;
    }

    private boolean npcAlreadySpawned(String newId) {
        for (Npc npc : this.activeNpcList) {
            if (!npc.npcId.equals(newId)) continue;
            return true;
        }
        return false;
    }

    public NpcDef getNpcDef(String npcId) {
        for (NpcDef npcDef : this.npcDefList) {
            if (!npcDef.npcId.equals(npcId)) continue;
            return npcDef;
        }
        Gdx.app.error("MewnBase", "Can'd find npc def with id: " + npcId);
        return null;
    }

    public void assignNpcToHome(Npc npc, NpcHome npcHome) {
        for (Npc n : this.activeNpcList) {
            if (n == npc || n.home != npcHome) continue;
            npc.removeHome();
            npcHome.assignNpc(null);
            break;
        }
        npc.assignHome(npcHome);
        npcHome.assignNpc(npc);
        MessageManager.getInstance().dispatchMessage(4);
    }

    public void homeRemoved(NpcHome npcHome) {
        for (Npc n : this.activeNpcList) {
            if (n.home != npcHome) continue;
            n.assignHome(null);
            break;
        }
        npcHome.assignNpc(null);
        MessageManager.getInstance().dispatchMessage(4);
    }
}

