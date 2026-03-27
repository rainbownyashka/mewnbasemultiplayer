/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BarrierWall;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.ToggleBehavior;

public class Gate
extends BaseModule {
    public ToggleBehavior toggleBehavior = new ToggleBehavior();
    private Group floorPartGroup;
    private Image floorPlateHorzLeft;
    private Image floorPlateHorzRight;
    private Image floorPlateVert;
    private Group horzDoorGroup;
    private Image horzDoorLeft;
    private Image horzDoorRight;
    private Image vertDoor;
    private Group eastGroup;
    private Image eastCap;
    private Group westp;
    private Image westCap;
    private Group northGroup;
    private Image northCap;
    private Group southGroup;
    private Image southCap;
    protected static final float drawableWidth = 424.0f * Tile.SCALE;
    protected static final float drawableHeight = 528.0f * Tile.SCALE;
    public BarrierWall.WallTypes wallType;
    protected boolean northWall = false;
    protected boolean eastWall = false;
    protected boolean southWall = false;
    protected boolean westWall = false;
    protected Fixture wall_h_fix;
    protected Fixture wall_v_fix;

    public Gate(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.toggleBehavior.on = true;
        this.behaviorList.add(this.toggleBehavior);
        this.setupPhysics(null);
        world.updateAllWalls();
    }

    @Override
    protected void createDrawables() {
        String spriteName = this.getBaseDrawableName();
        float yOffset = -(75.0f * Tile.SCALE);
        super.createDrawables(spriteName + "horz-closed", this.world.gameScreen.mainGroup);
        this.floorPartGroup = new Group();
        this.floorPartGroup.setOrigin(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f);
        this.floorPartGroup.setPosition(this.getWorldXPos(), this.getWorldYPos());
        this.floorPartGroup.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + TILE_SIZE / 3.0f - 3.0f));
        this.world.gameScreen.addToStage(this.floorPartGroup, this.world.gameScreen.floorGroup);
        this.floorPlateHorzLeft = new Image(this.world.gameScreen.skin.getDrawable("gate-horz-base-left"));
        this.floorPlateHorzLeft.setSize(drawableWidth / 2.0f, 64.0f);
        this.floorPlateHorzLeft.setOrigin(1);
        this.floorPlateHorzLeft.setPosition(Tile.TILE_SIZE / 2.0f + 26.0f, 11.0f, 20);
        this.floorPartGroup.addActor(this.floorPlateHorzLeft);
        this.floorPlateHorzRight = new Image(this.world.gameScreen.skin.getDrawable("gate-horz-base-right"));
        this.floorPlateHorzRight.setSize(drawableWidth / 2.0f, 64.0f);
        this.floorPlateHorzRight.setOrigin(1);
        this.floorPlateHorzRight.setPosition(Tile.TILE_SIZE / 2.0f - 26.0f, 11.0f, 12);
        this.floorPartGroup.addActor(this.floorPlateHorzRight);
        this.eastCap = new Image(this.world.gameScreen.skin.getDrawable("gate-horz-end"));
        this.eastCap.setSize(52.0f * Tile.SCALE, 150.0f * Tile.SCALE);
        this.eastCap.setOrigin(4);
        this.eastCap.setPosition(Tile.TILE_SIZE + 25.0f, 30.0f, 4);
        this.group.addActor(this.eastCap);
        this.westCap = new Image(this.world.gameScreen.skin.getDrawable("gate-horz-end"));
        this.westCap.setSize(52.0f * Tile.SCALE, 150.0f * Tile.SCALE);
        this.westCap.setOrigin(4);
        this.westCap.setPosition(-25.0f, 30.0f, 4);
        this.group.addActor(this.westCap);
        this.northGroup = new Group();
        this.northGroup.setPosition(this.getWorldXPos(), this.getWorldYPos());
        this.northGroup.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + Tile.TILE_SIZE + 20.0f));
        this.world.gameScreen.addToStage(this.northGroup, this.world.gameScreen.mainGroup);
        this.northCap = new Image(this.world.gameScreen.skin.getDrawable("gate-vert-top"));
        this.northCap.setSize(140.0f * Tile.SCALE, 190.0f * Tile.SCALE);
        this.northCap.setOrigin(4);
        this.northCap.setPosition(Tile.TILE_SIZE / 2.0f, 224.0f * Tile.SCALE, 4);
        this.northGroup.addActor(this.northCap);
        this.horzDoorGroup = new Group();
        this.horzDoorGroup.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f - 21.0f);
        this.group.addActor(this.horzDoorGroup);
        this.horzDoorLeft = new Image(this.world.gameScreen.skin.getDrawable("gate-horz-door-left"));
        this.horzDoorLeft.setSize(164.0f * Tile.SCALE, 109.0f * Tile.SCALE);
        this.horzDoorLeft.setOrigin(4);
        this.horzDoorLeft.setPosition(1.0f, 0.0f, 20);
        this.horzDoorGroup.addActor(this.horzDoorLeft);
        this.horzDoorRight = new Image(this.world.gameScreen.skin.getDrawable("gate-horz-door-right"));
        this.horzDoorRight.setSize(164.0f * Tile.SCALE, 109.0f * Tile.SCALE);
        this.horzDoorRight.setOrigin(4);
        this.horzDoorRight.setPosition(-1.0f, 0.0f, 12);
        this.horzDoorGroup.addActor(this.horzDoorRight);
        this.vertDoor = new Image(this.world.gameScreen.skin.getDrawable("gate-vert-door"));
        this.vertDoor.setSize(90.0f * Tile.SCALE, 429.0f * Tile.SCALE);
        this.vertDoor.setOrigin(4);
        this.vertDoor.setPosition(Tile.TILE_SIZE / 2.0f, -87.0f * Tile.SCALE, 4);
        this.group.addActor(this.vertDoor);
        this.floorPlateVert = new Image(this.world.gameScreen.skin.getDrawable("gate-vert-middle"));
        this.floorPlateVert.setSize(147.0f * Tile.SCALE, 350.0f * Tile.SCALE);
        this.floorPlateVert.setOrigin(1);
        this.floorPlateVert.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.floorPartGroup.addActor(this.floorPlateVert);
        this.southGroup = new Group();
        this.southGroup.setPosition(this.getWorldXPos(), this.getWorldYPos());
        this.southGroup.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE));
        this.world.gameScreen.addToStage(this.southGroup, this.world.gameScreen.mainGroup);
        this.southCap = new Image(this.world.gameScreen.skin.getDrawable("gate-vert-top"));
        this.southCap.setSize(140.0f * Tile.SCALE, 190.0f * Tile.SCALE);
        this.southCap.setOrigin(4);
        this.southCap.setPosition(Tile.TILE_SIZE / 2.0f, -162.0f * Tile.SCALE, 4);
        this.southGroup.addActor(this.southCap);
        this.image.setSize(drawableWidth, drawableHeight);
        this.image.setOrigin(1);
        this.image.setPosition(Tile.TILE_SIZE / 2.0f, yOffset, 4);
        this.image.toFront();
        this.group.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + TILE_SIZE / 2.0f + 3.0f));
    }

    @Override
    protected void setupPhysics(String loader) {
        super.setupPhysics(null);
        FixtureDef fd = new FixtureDef();
        fd.density = 1.0f;
        fd.friction = 0.5f;
        fd.restitution = 0.3f;
        fd.filter.categoryBits = this.categoryBits;
        fd.filter.maskBits = (short)(this.maskBits + this.categoryBits);
        this.world.bodyEditorLoader.attachFixture(this.body, "gate-horz", fd, 0.5f);
        this.wall_h_fix = this.body.getFixtureList().get(0);
        this.world.bodyEditorLoader.attachFixture(this.body, "gate-vert", fd, 0.5f);
        this.wall_v_fix = this.body.getFixtureList().get(1);
    }

    @Override
    public void setupLight() {
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "gate";
    }

    @Override
    public boolean usesBaseGroup() {
        return false;
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    public boolean visuallyConnectToConduits() {
        return false;
    }

    protected String getBaseDrawableName() {
        return "gate-";
    }

    @Override
    public boolean canInteract(Player player) {
        return true;
    }

    private void setVertical() {
        this.image.setVisible(false);
        this.horzDoorGroup.setVisible(false);
        this.floorPlateVert.setVisible(true);
        this.floorPlateHorzLeft.setVisible(false);
        this.floorPlateHorzRight.setVisible(false);
        this.northGroup.setVisible(true);
        this.southGroup.setVisible(true);
        this.eastCap.setVisible(false);
        this.westCap.setVisible(false);
        this.wall_h_fix.setSensor(true);
        this.wall_v_fix.setSensor(false);
    }

    private void setHorizontal() {
        this.image.setVisible(false);
        this.horzDoorGroup.setVisible(true);
        this.floorPlateVert.setVisible(false);
        this.floorPlateHorzLeft.setVisible(true);
        this.floorPlateHorzRight.setVisible(true);
        this.northGroup.setVisible(false);
        this.southGroup.setVisible(false);
        this.eastCap.setVisible(true);
        this.westCap.setVisible(true);
        this.wall_h_fix.setSensor(false);
        this.wall_v_fix.setSensor(true);
    }

    public void updateWalls() {
        String spriteName = this.getBaseDrawableName();
        boolean checkNorth = this.isNeighborWall(this.worldX, this.worldY + 1);
        boolean checkSouth = this.isNeighborWall(this.worldX, this.worldY - 1);
        boolean checkEast = this.isNeighborWall(this.worldX + 1, this.worldY);
        boolean checkWest = this.isNeighborWall(this.worldX - 1, this.worldY);
        this.northWall = checkNorth;
        this.eastWall = checkEast;
        this.southWall = checkSouth;
        this.westWall = checkWest;
        boolean eastGate = this.isNeighborGate(this.worldX + 1, this.worldY);
        boolean westGate = this.isNeighborGate(this.worldX - 1, this.worldY);
        boolean northGate = this.isNeighborGate(this.worldX, this.worldY + 1);
        boolean southGate = this.isNeighborGate(this.worldX, this.worldY - 1);
        this.wall_h_fix.setSensor(true);
        this.wall_v_fix.setSensor(!this.northWall);
        BarrierWall.WallTypes newType = BarrierWall.WallTypes.horz;
        int horzPoints = 0;
        int vertPoints = 0;
        int wallPoint = 1;
        int gatePoint = 5;
        if (northGate) {
            vertPoints += gatePoint;
        } else if (this.northWall) {
            vertPoints += wallPoint;
        }
        if (southGate) {
            vertPoints += gatePoint;
        } else if (this.southWall) {
            vertPoints += wallPoint;
        }
        if (eastGate) {
            horzPoints += gatePoint;
        } else if (this.eastWall) {
            horzPoints += wallPoint;
        }
        if (westGate) {
            horzPoints += gatePoint;
        } else if (this.westWall) {
            horzPoints += wallPoint;
        }
        if (horzPoints >= vertPoints) {
            this.setHorizontal();
            newType = BarrierWall.WallTypes.horz;
        } else {
            this.setVertical();
            newType = BarrierWall.WallTypes.vert;
        }
        if (newType == BarrierWall.WallTypes.vert) {
            this.northGroup.setVisible(!northGate);
            this.southGroup.setVisible(!southGate);
            if (northGate) {
                this.northGroup.setVisible(false);
            } else {
                this.northGroup.setVisible(true);
            }
            if (southGate) {
                this.southGroup.setVisible(false);
            } else {
                this.southGroup.setVisible(true);
            }
        }
        if (newType == BarrierWall.WallTypes.horz) {
            this.eastCap.setVisible(!eastGate);
            this.westCap.setVisible(!westGate);
            if (eastGate) {
                this.horzDoorRight.setX(-28.0f * Tile.SCALE, 12);
                this.floorPlateHorzRight.setX(TILE_SIZE / 2.0f - 26.0f, 12);
            } else {
                this.horzDoorRight.setX(-1.0f, 12);
                this.floorPlateHorzRight.setX(TILE_SIZE / 2.0f - 26.0f, 12);
            }
            if (westGate) {
                this.horzDoorLeft.setX(29.0f * Tile.SCALE, 20);
                this.floorPlateHorzLeft.setX(TILE_SIZE / 2.0f + 26.0f, 20);
            } else {
                this.horzDoorLeft.setX(1.0f, 20);
                this.floorPlateHorzLeft.setX(TILE_SIZE / 2.0f + 26.0f, 20);
            }
        }
        this.changeType(newType);
        this.updateState(null);
    }

    protected boolean isNeighborWall(int x, int y) {
        Tile t = this.world.getTile(x, y);
        if (t != null) {
            return t instanceof BarrierWall && !(t instanceof Gate);
        }
        return false;
    }

    protected boolean isNeighborGate(int x, int y) {
        Tile t = this.world.getTile(x, y);
        if (t != null) {
            return t instanceof Gate;
        }
        return false;
    }

    protected void changeType(BarrierWall.WallTypes newType) {
        this.wallType = newType;
        MoonBase.log("Gate at " + this.worldX + ", " + this.worldY + " - " + (Object)((Object)this.wallType));
    }

    @Override
    public void interactMain(Player player) {
        this.toggleBehavior.interact(player);
        this.updateState(null);
        this.soundFx(this.toggleBehavior.on);
    }

    public void setState(boolean state, Gate parent) {
        this.toggleBehavior.on = state;
        this.updateState(parent);
    }

    protected void updateState(Gate parent) {
        if (this.toggleBehavior != null) {
            if (this.wallType == BarrierWall.WallTypes.horz) {
                this.wall_h_fix.setSensor(this.toggleBehavior.on);
                this.wall_v_fix.setSensor(true);
                this.horzDoorGroup.setVisible(!this.toggleBehavior.on);
                this.vertDoor.setVisible(false);
                this.image.setVisible(false);
                this.updateNeighbors(true, false, parent, this.toggleBehavior.on);
            } else if (this.wallType == BarrierWall.WallTypes.vert) {
                this.wall_h_fix.setSensor(true);
                this.wall_v_fix.setSensor(this.toggleBehavior.on);
                this.horzDoorGroup.setVisible(false);
                this.vertDoor.setVisible(!this.toggleBehavior.on);
                this.image.setVisible(false);
                this.updateNeighbors(false, true, parent, this.toggleBehavior.on);
            }
        }
    }

    private void updateNeighbors(boolean horz, boolean vert, Gate parent, boolean state) {
        if (horz) {
            Tile e = this.world.getTile(this.worldX + 1, this.worldY);
            Tile w = this.world.getTile(this.worldX - 1, this.worldY);
            this.updateNeighbor(e, parent, state);
            this.updateNeighbor(w, parent, state);
        }
        if (vert) {
            Tile n = this.world.getTile(this.worldX, this.worldY + 1);
            Tile s = this.world.getTile(this.worldX, this.worldY - 1);
            this.updateNeighbor(n, parent, state);
            this.updateNeighbor(s, parent, state);
        }
    }

    private void updateNeighbor(Tile t, Gate parent, boolean state) {
        if (t != null && t instanceof Gate && t != parent) {
            Gate g = (Gate)t;
            if (g.toggleBehavior.on != state) {
                g.setState(state, this);
            }
        }
    }

    private void soundFx(boolean open) {
        float distVol = Audio.getInstance().playerDistanceMultiplier(this.world, this.getXCenter(), this.getYCenter(), 900.0f, 0.4f);
        float pan = Audio.getInstance().playerDistancePan(this.world, this.getXCenter(), this.getYCenter());
        pan *= 0.5f;
        if (open) {
            Audio.getInstance().playSound("sounds/gate-open.ogg", distVol, 0.8f, pan);
        } else {
            Audio.getInstance().playSound("sounds/gate-open.ogg", distVol, 1.0f, pan);
        }
    }

    @Override
    public void destroy() {
        this.floorPartGroup.remove();
        this.northGroup.remove();
        this.southGroup.remove();
        super.destroy();
    }
}

