/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.math.GridPoint2;
import com.cairn4.moonbase.BaseGroup;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.CommsTower;
import com.cairn4.moonbase.tiles.GreenHouse;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.LaunchPad;
import com.cairn4.moonbase.tiles.ProxyTile;
import com.cairn4.moonbase.tiles.SolarPanel;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.WoodSign;
import com.cairn4.moonbase.tiles.behaviors.NavLocator;
import java.util.ArrayList;
import java.util.Collections;

public class BaseManager {
    World world;
    ArrayList<Tile> masterList = new ArrayList();
    ArrayList<ArrayList> baseLists = new ArrayList();
    ArrayList<BaseGroup> baseGroupList = new ArrayList();
    private ArrayList<GridPoint2> navBeaconLocations = new ArrayList();
    private ArrayList<GridPoint2> navBeaconOnList = new ArrayList();
    private static float NAV_BEACON_SEARCH_FREQ = 0.5f;
    private float navBeaconSearchTimer = 0.0f;
    private ArrayList<LaunchPad> launchPadList = new ArrayList();
    private ArrayList<WoodSign> signList = new ArrayList();

    public void updateBases(World world) {
        if (world.gameState == World.GameStates.playing) {
            MoonBase.log("Updating Bases!");
            this.world = world;
            this.masterList.clear();
            this.baseLists.clear();
            this.navBeaconLocations.clear();
            this.navBeaconOnList.clear();
            this.launchPadList.clear();
            this.getAllBaseModules();
            for (Tile b : this.masterList) {
                this.checkForNavBeacon(b);
                this.updateLaunchPadList(b);
            }
            this.updateNavBeaconLists();
            while (this.masterList.size() > 0) {
                Tile b;
                ArrayList<Tile> newList = new ArrayList<Tile>();
                this.baseLists.add(newList);
                b = this.masterList.get(0);
                newList.add(this.masterList.get(0));
                this.masterList.remove(b);
                this.checkForNeighbors(b, newList);
            }
            MoonBase.debug("BaseManager: Number of bases: " + this.baseLists.size());
            this.baseGroupList.clear();
            for (ArrayList list : this.baseLists) {
                BaseGroup bg = new BaseGroup(list);
                this.baseGroupList.add(bg);
            }
            world.updateAllWalls();
        }
    }

    public ArrayList<BaseGroup> getBaseGroupList() {
        return this.baseGroupList;
    }

    private void checkForNavBeacon(Tile b) {
        GridPoint2 worldPos = new GridPoint2(b.worldX, b.worldY);
        if (b.findBehavior(NavLocator.class) != null) {
            boolean alreadyInList = false;
            for (GridPoint2 gp : this.navBeaconLocations) {
                if (worldPos.x != gp.x || worldPos.y != gp.y) continue;
                alreadyInList = true;
                break;
            }
            if (!alreadyInList) {
                this.navBeaconLocations.add(worldPos);
            }
        }
    }

    public ArrayList<GridPoint2> getNavBeacons() {
        return this.navBeaconOnList;
    }

    public void update(float delta) {
        for (BaseGroup bg : this.baseGroupList) {
            bg.update(delta);
        }
        this.navBeaconSearchTimer += delta;
        if (this.navBeaconSearchTimer > NAV_BEACON_SEARCH_FREQ) {
            this.navBeaconSearchTimer = 0.0f;
            this.updateNavBeaconLists();
        }
    }

    private void updateNavBeaconLists() {
        for (GridPoint2 gp2 : this.navBeaconLocations) {
            Tile t = this.world.getTile(gp2.x, gp2.y);
            if (!(t instanceof CommsTower)) continue;
            if (this.navBeaconOnList.contains(gp2)) {
                if (t.hasPower) continue;
                this.navBeaconOnList.remove(gp2);
                continue;
            }
            if (!t.hasPower) continue;
            this.navBeaconOnList.add(gp2);
        }
    }

    private void updateLaunchPadList(Tile b) {
        LaunchPad l;
        if (b instanceof LaunchPad && !this.launchPadList.contains(l = (LaunchPad)b)) {
            this.launchPadList.add(l);
        }
        MoonBase.debug("Launchpads: " + this.launchPadList);
    }

    public ArrayList<LaunchPad> getLaunchPads() {
        return this.launchPadList;
    }

    public ArrayList<WoodSign> getSigns() {
        return this.signList;
    }

    public void addSignToList(WoodSign s) {
        this.signList.add(s);
        MoonBase.log("Wood Signs: " + this.signList.size());
    }

    public void removeSignFromList(WoodSign s) {
        this.signList.remove(s);
    }

    private void checkForNeighbors(Tile b, ArrayList<Tile> baseList) {
        ArrayList<Tile> neighbors = new ArrayList<Tile>();
        ArrayList<Tile> masterClone = new ArrayList<Tile>();
        masterClone.addAll(this.masterList);
        for (Tile n1 : masterClone) {
            if (!this.isAdjacent(b, n1)) continue;
            if (n1 instanceof ProxyTile) {
                ProxyTile proxy = (ProxyTile)n1;
                if (proxy.parent != null) {
                    neighbors.add(proxy.parent);
                    neighbors.add(n1);
                    if (!baseList.contains(proxy.parent)) {
                        baseList.add(proxy.parent);
                    }
                    this.masterList.remove(n1);
                    this.masterList.remove(proxy.parent);
                    continue;
                }
                MoonBase.error("Proxy has no parent tile yet");
                continue;
            }
            neighbors.add(n1);
            baseList.add(n1);
            this.masterList.remove(n1);
        }
        for (Tile n2 : neighbors) {
            this.checkForNeighbors(n2, baseList);
        }
    }

    private boolean isAdjacent(Tile b1, Tile b2) {
        return Math.abs(b1.worldX - b2.worldX) == 1 && Math.abs(b1.worldY - b2.worldY) == 0 || Math.abs(b1.worldX - b2.worldX) == 0 && Math.abs(b1.worldY - b2.worldY) == 1;
    }

    private void getAllBaseModules() {
        for (Chunk chunk : this.world.worldChunks.values()) {
            for (int x = 0; x < 10; ++x) {
                for (int y = 0; y < 10; ++y) {
                    Tile t = chunk.getTile(x, y, true);
                    if (t == null || !(t instanceof BaseModule) && !(t instanceof ProxyTile)) continue;
                    if (t instanceof BaseModule && ((BaseModule)t).usesBaseGroup()) {
                        this.masterList.add(t);
                    }
                    if (!(t instanceof ProxyTile)) continue;
                    this.masterList.add(t);
                }
            }
        }
        MoonBase.debug("BaseManager: Master list size: " + this.masterList.size());
    }

    public ArrayList<BaseModule> getMasterListCopy() {
        this.getAllBaseModules();
        ArrayList<BaseModule> list = new ArrayList<BaseModule>();
        Collections.copy(this.masterList, list);
        this.masterList.clear();
        MoonBase.debug("BaseManager: Master list size: " + this.masterList.size());
        return list;
    }

    public void triggerBaseDisaster() {
        BaseModule b = this.getRandomBaseModule();
        if (b != null) {
            b.triggerDisaster();
            if (b instanceof SolarPanel) {
                SolarPanel s = (SolarPanel)b;
                s.dustTriggerBehavior.dustAmount = s.dustTriggerBehavior.limit + 1.0f;
            }
        }
    }

    public BaseModule getRandomBaseModule() {
        this.getAllBaseModules();
        for (int i = this.masterList.size() - 1; i >= 0; --i) {
            if (!(this.masterList.get(i) instanceof BaseModule)) continue;
            BaseModule b = (BaseModule)this.masterList.get(i);
            if (b.baseDisaster == null) continue;
            this.masterList.remove(i);
        }
        if (this.masterList.size() >= 0) {
            BaseModule b = null;
            if (b == null) {
                int r = Math.round((float)Math.random() * (float)this.masterList.size() - 1.0f);
                if (this.masterList.get(r) instanceof BaseModule) {
                    return (BaseModule)this.masterList.get(r);
                }
                return null;
            }
            return b;
        }
        return null;
    }

    public void baseUpgrade(String techId, boolean loadGame) {
        MoonBase.log("BaseManager: applying base upgrade " + techId);
        switch (techId) {
            case "lessLeaks": {
                this.upgradeLessLeaks(loadGame);
                break;
            }
            case "saveWater": {
                this.upgradeSaveWater();
                break;
            }
            default: {
                MoonBase.error("BaseManager doesn't know how to handle techId \"" + techId + "\", typo?");
            }
        }
    }

    public void upgradeLessLeaks(boolean loadGame) {
        for (BaseGroup bg : this.baseGroupList) {
            for (BaseModule b : bg.baseModuleList) {
                if (!(b instanceof HabitatModule)) continue;
                HabitatModule h = (HabitatModule)b;
                h.upgradeLessLeaks(!loadGame);
            }
        }
    }

    public void upgradeSaveWater() {
        for (BaseGroup bg : this.baseGroupList) {
            for (BaseModule b : bg.baseModuleList) {
                if (!(b instanceof GreenHouse)) continue;
                GreenHouse g = (GreenHouse)b;
                MoonBase.debug("Upgrading green house to use less water.");
                g.upgradeWaterUsage();
            }
        }
    }
}

