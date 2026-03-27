/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Buggie;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.Tank;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.tiles.ItemDropper;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.Hud;
import java.util.ArrayList;

public class HudWaypoints {
    public final Pool<Image> imagePool = Pools.get(Image.class);
    private static final float updateDelay = 0.25f;
    private float updateTimer = 0.0f;
    private final float radius = 258.0f;
    private final float maxTileDistance = 150.0f;
    private static final float minVehicleDistance = Tile.TILE_SIZE * 2.5f;
    private static final float maxVehicleDistance = Tile.TILE_SIZE * 30.0f;
    private static final float maxCrateDistance = Tile.TILE_SIZE * 15.0f;
    private static final float maxMarkedLocationDistance = Tile.TILE_SIZE * 500.0f;
    private static final float maxNodeAlpha = 0.7f;
    private final Vector2 screenCenter = new Vector2(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2);
    private World world;
    private Player player;
    private Vector2 playerPos;
    private Hud hud;
    private Group baseGroup;
    private Image ringL;
    private Image ringR;
    private Image atWaypointRingL;
    private Image atWaypointRingR;
    private Vector2 diff;
    private ArrayList<Vehicle> vehicleList = new ArrayList();
    private ArrayList<Group> vehicleIconList = new ArrayList();
    private ArrayList<GridPoint2> commsTowerList = new ArrayList();
    private ArrayList<Image> nodeList = new ArrayList();
    private ArrayList<ItemDropper> crateList = new ArrayList();
    private ArrayList<Group> crateIconList = new ArrayList();
    public ArrayList<GridPoint2> markedLocationsList = new ArrayList();
    public ArrayList<Group> markedLocationsIconList = new ArrayList();
    int availableComms = 0;
    int availableVehicles = 0;
    int availableCrates = 0;
    int availableMarkedLocations = 0;
    private boolean waypointsOn = false;
    private float crateListUpdateTimer = 0.0f;
    private static final float crateListUpdateDelay = 1.0f;
    private ArrayList<GridPoint2> nearbyCrateCoords = new ArrayList();
    private static final String supplyCrate1id = "supplycrate1";
    private static final String supplyCrate2id = "supplycrate2";

    public HudWaypoints(World world, Hud hud, Group parentGroup) {
        this.world = world;
        this.hud = hud;
        this.waypointsOn = false;
        this.playerPos = new Vector2(0.0f, 0.0f);
        this.diff = new Vector2(0.0f, 0.0f);
        this.baseGroup = new Group();
        this.baseGroup.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2);
        this.baseGroup.setVisible(false);
        parentGroup.addActor(this.baseGroup);
        this.baseGroup.setTouchable(Touchable.disabled);
        this.ringL = new Image(hud.skin.getDrawable("waypoint-ring"));
        this.ringL.setPosition(0.0f, 0.0f, 16);
        this.ringL.setColor(1.0f, 0.65f, 0.2f, 0.08f);
        this.baseGroup.addActor(this.ringL);
        this.ringR = new Image(hud.skin.getDrawable("waypoint-ring"));
        this.ringR.setOrigin(16);
        this.ringR.setRotation(180.0f);
        this.ringR.setPosition(0.0f, 0.0f, 16);
        this.ringR.setColor(1.0f, 0.65f, 0.2f, 0.08f);
        this.baseGroup.addActor(this.ringR);
        this.atWaypointRingL = new Image(hud.skin.getDrawable("waypoint-ring"));
        this.atWaypointRingL.setPosition(0.0f, 0.0f, 16);
        this.atWaypointRingL.setColor(Vars.markedLocationColor);
        this.atWaypointRingL.getColor().a = 0.35f;
        this.baseGroup.addActor(this.atWaypointRingL);
        this.atWaypointRingR = new Image(hud.skin.getDrawable("waypoint-ring"));
        this.atWaypointRingR.setOrigin(16);
        this.atWaypointRingR.setRotation(180.0f);
        this.atWaypointRingR.setPosition(0.0f, 0.0f, 16);
        this.atWaypointRingR.setColor(Vars.markedLocationColor);
        this.atWaypointRingR.getColor().a = 0.35f;
        this.baseGroup.addActor(this.atWaypointRingR);
    }

    public void update(float delta) {
        Vector2 offset = this.screenCenter.cpy().sub(this.world.gameScreen.cameraLag.offset);
        this.baseGroup.setPosition(offset.x, offset.y);
        if (this.world.player != null) {
            this.player = this.world.player;
            this.playerPos.set(this.player.getXPos(), this.player.getYPos());
        }
        this.updateMarkedLocations();
        this.updateTowerList();
        this.updateVehicleList();
        this.updateCrateList(delta);
        if (!(this.availableComms <= 0 && this.availableVehicles <= 0 && this.availableCrates <= 0 && this.availableMarkedLocations <= 0 || this.waypointsOn)) {
            this.showRing();
        }
        if (this.availableComms == 0 && this.availableVehicles == 0 && this.availableCrates == 0 && this.availableMarkedLocations == 0 && this.waypointsOn) {
            this.hideRing();
        }
        this.compareLists();
    }

    private void updateCrateList(float delta) {
        this.crateListUpdateTimer += delta;
        if (this.crateListUpdateTimer > 1.0f) {
            GridPoint2 gp;
            int i;
            this.crateListUpdateTimer = 0.0f;
            this.availableCrates = 0;
            this.crateList.clear();
            this.nearbyCrateCoords = Tile.GET_NEARBY_COORDS(this.nearbyCrateCoords, this.world.getPlayer().getX(), this.world.getPlayer().getY(), 15);
            for (i = 0; i < this.nearbyCrateCoords.size(); ++i) {
                gp = this.nearbyCrateCoords.get(i);
                Tile t = this.world.getTile(gp.x, gp.y);
                if (t == null || !(t instanceof ItemDropper)) continue;
                ItemDropper id = (ItemDropper)t;
                if (!id.rdData.id.equals(supplyCrate1id) && !id.rdData.id.equals(supplyCrate2id)) continue;
                if (!this.crateList.contains(id)) {
                    this.crateList.add(id);
                }
                ++this.availableCrates;
            }
            for (i = this.nearbyCrateCoords.size() - 1; i >= 0; --i) {
                gp = this.nearbyCrateCoords.get(i);
                if (gp == null) continue;
                World.gridPointPool.free(gp);
            }
        }
    }

    private void updateVehicleList() {
        for (int i = this.vehicleList.size() - 1; i >= 0; --i) {
            if (this.vehicleList.get(i) != null) continue;
            this.vehicleList.remove(i);
            MoonBase.log("removing vehicle from list. now " + this.vehicleList.size());
        }
        this.availableVehicles = 0;
        for (Entity e : this.world.entityList) {
            float dist;
            if (!(e instanceof Buggie) && !(e instanceof Tank)) continue;
            if (!this.vehicleList.contains(e)) {
                this.vehicleList.add((Vehicle)e);
                MoonBase.log("adding vehicle to list. now " + this.vehicleList.size());
            }
            if (!((dist = Vector2.dst(e.getXPos(), e.getYPos(), this.playerPos.x, this.playerPos.y)) < maxVehicleDistance)) continue;
            ++this.availableVehicles;
        }
    }

    private void updateTowerList() {
        this.commsTowerList = this.world.baseManager.getNavBeacons();
        this.availableComms = 0;
        for (GridPoint2 gp : this.commsTowerList) {
            float tile_distance = 100.0f;
            if (this.player != null) {
                tile_distance = Vector2.dst(gp.x, gp.y, this.world.player.getX(), this.world.player.getY());
            }
            if (!(tile_distance < 150.0f)) continue;
            ++this.availableComms;
        }
    }

    private void updateMarkedLocations() {
        this.availableMarkedLocations = 0;
        for (int i = this.markedLocationsList.size() - 1; i >= 0; --i) {
            GridPoint2 gp = this.markedLocationsList.get(i);
            if (this.hud.gameScreen.world.player.markedMapLocations.contains(gp, false)) continue;
            this.markedLocationsList.remove(i);
        }
        if (this.hud.gameScreen.world.player != null) {
            for (GridPoint2 gp : this.hud.gameScreen.world.player.markedMapLocations) {
                float dist;
                if (!this.listHasGridPoint2(this.markedLocationsList, gp)) {
                    this.markedLocationsList.add(gp);
                    MoonBase.log("adding location to list. now " + this.markedLocationsList.size());
                }
                if (!((dist = Vector2.dst((float)gp.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)gp.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, this.playerPos.x, this.playerPos.y)) < maxMarkedLocationDistance)) continue;
                ++this.availableMarkedLocations;
            }
        }
    }

    private boolean listHasGridPoint2(ArrayList<GridPoint2> list, GridPoint2 gp2) {
        for (GridPoint2 gp : list) {
            if (!gp.equals(gp2)) continue;
            return true;
        }
        return false;
    }

    private void showRing() {
        this.waypointsOn = true;
        this.baseGroup.clearActions();
        this.baseGroup.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.scaleTo(0.9f, 0.9f), (Action)Actions.parallel((Action)Actions.fadeIn(0.25f), (Action)Actions.scaleTo(1.025f, 1.025f, 0.25f, Interpolation.sineOut)), (Action)Actions.scaleTo(1.0f, 1.0f, 0.05f, Interpolation.sine)));
    }

    private void hideRing() {
        this.waypointsOn = false;
        this.baseGroup.clearActions();
        this.baseGroup.addAction(Actions.sequence((Action)Actions.scaleTo(1.05f, 1.05f, 0.05f, Interpolation.sine), (Action)Actions.parallel((Action)Actions.fadeOut(0.25f), (Action)Actions.scaleTo(0.9f, 0.9f, 0.25f, Interpolation.sineIn)), (Action)Actions.visible(false)));
    }

    private void compareLists() {
        if (this.nodeList.size() != this.commsTowerList.size()) {
            while (this.nodeList.size() < this.commsTowerList.size()) {
                this.addNode();
            }
            while (this.nodeList.size() > this.commsTowerList.size()) {
                this.removeNode();
            }
        }
        if (this.vehicleIconList.size() != this.vehicleList.size()) {
            while (this.vehicleIconList.size() < this.vehicleList.size()) {
                this.addVehicle();
            }
            while (this.vehicleIconList.size() > this.vehicleList.size()) {
                this.removeVehicle();
            }
        }
        if (this.crateList.size() != this.crateIconList.size()) {
            while (this.crateIconList.size() < this.availableCrates) {
                this.addCrate();
            }
            while (this.crateIconList.size() > this.availableCrates) {
                this.removeCrate();
            }
        }
        if (this.markedLocationsList.size() != this.markedLocationsIconList.size()) {
            while (this.markedLocationsIconList.size() < this.availableMarkedLocations) {
                MoonBase.log("adding mark");
                this.addMarked();
            }
            while (this.markedLocationsIconList.size() > this.availableMarkedLocations) {
                MoonBase.log("removing mark");
                this.removeMarked();
            }
        }
    }

    public void updatePositions() {
        int index = 0;
        for (GridPoint2 gp : this.commsTowerList) {
            if (index < this.nodeList.size()) {
                this.updateNode(gp, this.nodeList.get(index));
            }
            ++index;
        }
        for (int v = 0; v < this.vehicleList.size(); ++v) {
            this.updateVehicle(this.vehicleList.get(v), this.vehicleIconList.get(v));
        }
        for (int c = 0; c < this.crateList.size(); ++c) {
            this.updateCrate(this.crateList.get(c), this.crateIconList.get(c));
        }
        this.showArrivedAtMark(false);
        for (int m = 0; m < this.markedLocationsList.size(); ++m) {
            if (this.markedLocationsIconList.size() - 1 < m) continue;
            this.updateMarked(this.markedLocationsList.get(m), this.markedLocationsIconList.get(m));
        }
    }

    private void updateNode(GridPoint2 tileCoord, Image node) {
        float tile_distance = 100.0f;
        if (this.player != null) {
            tile_distance = Vector2.dst(tileCoord.x, tileCoord.y, this.world.player.getX(), this.world.player.getY());
        }
        float alpha = 1.0f - tile_distance / 150.0f;
        float maxAlpha = MathUtils.clamp(alpha *= 0.7f, 0.0f, 1.0f);
        node.setScale(maxAlpha * 0.7f);
        node.setColor(1.0f, 1.0f, 1.0f, maxAlpha);
        this.diff.set((float)tileCoord.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)tileCoord.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
        this.diff.sub(this.playerPos.x, this.playerPos.y);
        float dist = this.diff.len();
        Vector2 offset = new Vector2(258.0f, 0.0f);
        offset.rotate(this.diff.angle());
        node.setPosition(offset.x, offset.y, 1);
    }

    private void updateVehicle(Vehicle vehicle, Group groupNode) {
        float dist = Vector2.dst(vehicle.getXPos(), vehicle.getYPos(), this.playerPos.x, this.playerPos.y);
        float alpha = 1.0f - dist / maxVehicleDistance;
        float maxAlpha = MathUtils.clamp(alpha *= 0.7f, 0.0f, 1.0f);
        if (dist < minVehicleDistance) {
            maxAlpha = 0.0f;
        }
        groupNode.setScale(maxAlpha * 0.7f);
        groupNode.setColor(1.0f, 1.0f, 1.0f, maxAlpha);
        groupNode.setRotation(vehicle.getRotation());
        this.diff.set(vehicle.getXPos(), vehicle.getYPos());
        this.diff.sub(this.playerPos.x, this.playerPos.y);
        Vector2 offset = new Vector2(258.0f, 0.0f);
        offset.rotate(this.diff.angle());
        groupNode.setPosition(offset.x, offset.y, 1);
        if (vehicle.currentState == Vehicle.STATES.empty) {
            if (!groupNode.isVisible()) {
                groupNode.setVisible(true);
            }
        } else if (groupNode.isVisible()) {
            groupNode.setVisible(false);
        }
    }

    private void updateCrate(ItemDropper crate, Group groupNode) {
        float dist = Vector2.dst(crate.getXCenter(), crate.getYCenter(), this.playerPos.x, this.playerPos.y);
        float alpha = 1.0f - dist / maxCrateDistance;
        float maxAlpha = MathUtils.clamp(alpha *= 0.7f, 0.0f, 1.0f);
        if (dist < minVehicleDistance) {
            maxAlpha = 0.0f;
        }
        groupNode.setScale(maxAlpha * 0.7f);
        groupNode.setColor(1.0f, 1.0f, 1.0f, maxAlpha);
        this.diff.set(crate.getXCenter(), crate.getYCenter());
        this.diff.sub(this.playerPos.x, this.playerPos.y);
        Vector2 offset = World.vector2Pool.obtain();
        offset.set(258.0f, 0.0f);
        offset.rotate(this.diff.angle());
        groupNode.setPosition(offset.x, offset.y, 1);
        World.vector2Pool.free(offset);
    }

    private void addNode() {
        MoonBase.debug("adding waypoint node");
        Image i = new Image(this.hud.skin.getDrawable("waypoint-node"));
        i.setScale(0.6f, 0.6f);
        i.setOrigin(1);
        this.baseGroup.addActor(i);
        this.nodeList.add(i);
    }

    private void removeNode() {
        MoonBase.debug("removing waypoint node");
        this.nodeList.get(0).remove();
        this.nodeList.remove(0);
    }

    private void addVehicle() {
        MoonBase.log("adding waypoint node");
        Group g = new Group();
        Image i = new Image(this.hud.skin.getDrawable("map/icons/map-buggie-icon"));
        i.setPosition(0.0f, 0.0f, 1);
        i.setOrigin(1);
        g.addActor(i);
        g.setScale(0.6f, 0.6f);
        this.baseGroup.addActor(g);
        this.vehicleIconList.add(g);
    }

    private void removeVehicle() {
        MoonBase.log("removing waypoint node");
        this.vehicleIconList.get(0).remove();
        this.vehicleIconList.remove(0);
    }

    private void addCrate() {
        MoonBase.log("adding crate radar node");
        Group g = new Group();
        Image i = new Image(this.hud.skin.getDrawable("map/icons/waypoint-node-crate"));
        i.setColor(Color.valueOf("1adb00"));
        i.setPosition(0.0f, 0.0f, 1);
        i.setOrigin(1);
        g.addActor(i);
        g.setScale(0.6f, 0.6f);
        this.baseGroup.addActor(g);
        i.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.0f, 1.0f, 1.0f, Interpolation.sine), (Action)Actions.scaleTo(1.5f, 1.5f, 1.0f, Interpolation.sine))));
        Image ring = new Image(this.hud.skin.getDrawable("waypoint-node-circle"));
        ring.setColor(i.getColor());
        ring.setSize(80.0f, 80.0f);
        ring.setOrigin(1);
        ring.setPosition(0.0f, 0.0f, 1);
        g.addActor(ring);
        ring.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(0.75f, 0.75f), (Action)Actions.alpha(0.0f), (Action)Actions.parallel((Action)Actions.scaleTo(2.0f, 2.0f, 1.0f, Interpolation.sine), (Action)Actions.sequence((Action)Actions.alpha(0.5f, 0.25f), (Action)Actions.fadeOut(0.75f))), (Action)Actions.delay(1.0f))));
        this.crateIconList.add(g);
    }

    private void removeCrate() {
        MoonBase.log("radar: remove crate");
        this.crateIconList.get(0).remove();
        this.crateIconList.remove(0);
    }

    private void addMarked() {
        Group g = new Group();
        this.baseGroup.addActor(g);
        Image i = new Image(this.hud.skin.getDrawable("marked-map-location"));
        i.setColor(Vars.markedLocationColor);
        g.setScale(0.7f, 0.7f);
        i.setOrigin(1);
        i.setPosition(0.0f, 0.0f, 1);
        g.addActor(i);
        this.markedLocationsIconList.add(g);
    }

    private void updateMarked(GridPoint2 gp, Group groupNode) {
        Vector2 markedPos = new Vector2((float)gp.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)gp.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
        float dist = Vector2.dst(markedPos.x, markedPos.y, this.playerPos.x, this.playerPos.y);
        if (this.hud.gameScreen.world.player != null && this.hud.gameScreen.world.player.getX() == gp.x && this.hud.gameScreen.world.player.getY() == gp.y) {
            this.showArrivedAtMark(true);
        }
        float alpha = 1.0f - dist / maxMarkedLocationDistance;
        float maxAlpha = MathUtils.clamp(alpha *= 0.7f, 0.2f, 1.0f);
        if (dist < Tile.TILE_SIZE / 2.0f) {
            maxAlpha = 0.0f;
        }
        groupNode.setScale(maxAlpha * 0.7f);
        groupNode.setColor(1.0f, 1.0f, 1.0f, maxAlpha);
        this.diff.set(markedPos.x, markedPos.y);
        this.diff.sub(this.playerPos.x, this.playerPos.y);
        Vector2 offset = World.vector2Pool.obtain();
        offset.set(258.0f, 0.0f);
        offset.rotate(this.diff.angle());
        groupNode.setPosition(offset.x, offset.y, 1);
        World.vector2Pool.free(offset);
    }

    private void showArrivedAtMark(boolean visible) {
        this.atWaypointRingL.setVisible(visible);
        this.atWaypointRingR.setVisible(visible);
    }

    private void removeMarked() {
        this.markedLocationsIconList.get(0).remove();
        this.markedLocationsIconList.remove(0);
        if (this.markedLocationsList.size() > 0) {
            this.markedLocationsList.remove(0);
        }
    }
}

