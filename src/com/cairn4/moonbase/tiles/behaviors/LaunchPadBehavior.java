/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.math.GridPoint2;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.entities.Rocket;
import com.cairn4.moonbase.tiles.LaunchPad;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class LaunchPadBehavior
implements Behavior {
    public String launchPadName;
    public LaunchPad launchPad;
    public boolean hasRocket;
    public GridPoint2 destinationPad;
    public boolean loaded;
    public float rocketFuel;

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public void setId(String s) {
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void start() {
        Tile t;
        this.launchPad.setName(this.launchPadName);
        if (this.hasRocket) {
            this.launchPad.addRocket();
            this.getRocket().setFuel(this.rocketFuel);
        }
        if (this.destinationPad != null && (t = this.launchPad.world.getTile(this.destinationPad.x, this.destinationPad.y)) != null && t instanceof LaunchPad) {
            this.launchPad.setDestination((LaunchPad)t);
        }
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void interact(Player player) {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public BehaviorData getData() {
        BehaviorData bd = new BehaviorData();
        bd.behaviorClass = this.getClass().getName();
        bd.properties.put("launchPadName", this.getLaunchPadName());
        bd.properties.put("destinationPad", this.getDestination());
        bd.properties.put("hasRocket", this.getRocket() != null);
        bd.properties.put("rocketFuel", Float.valueOf(this.getRocketFuel()));
        return bd;
    }

    private String getLaunchPadName() {
        if (this.launchPad != null) {
            return this.launchPad.getName();
        }
        return null;
    }

    private GridPoint2 getDestination() {
        if (this.launchPad != null && this.launchPad.targetDestination != null) {
            return new GridPoint2(this.launchPad.targetDestination.worldX, this.launchPad.targetDestination.worldY);
        }
        return null;
    }

    private Rocket getRocket() {
        if (this.launchPad != null && this.launchPad.rocket != null) {
            return this.launchPad.rocket;
        }
        return null;
    }

    public float getRocketFuel() {
        Rocket r = this.getRocket();
        if (r != null) {
            return r.getFuel();
        }
        return 0.0f;
    }
}

