/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Gate;
import com.cairn4.moonbase.tiles.Tile;

public class BarrierWall
extends BaseModule {
    protected Image cornerEast;
    protected Image cornerWest;
    protected Image cornerNorth;
    protected static final float drawableWidth = 283.0f * Tile.SCALE;
    protected static final float drawableHeight = 411.0f * Tile.SCALE;
    protected boolean useRandomColorTinting = false;
    protected int variation = 1;
    protected boolean northWall = false;
    protected boolean eastWall = false;
    protected boolean southWall = false;
    protected boolean westWall = false;
    protected Fixture wall_c_fix;
    protected Fixture wall_h_fix;
    protected Fixture wall_v_fix;
    protected Fixture wall_ce_fix;
    protected Fixture wall_cw_fix;
    public WallTypes wallType;

    protected String getBaseDrawableName() {
        return "stonewall-";
    }

    public BarrierWall(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.blocker = true;
        this.setupPhysics("wall-corner");
        this.updateBases();
    }

    @Override
    public Color getMinimapColor() {
        return Color.valueOf("332e2c");
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
        this.world.bodyEditorLoader.attachFixture(this.body, "wall-corner", fd, 0.5f);
        this.wall_c_fix = this.body.getFixtureList().get(0);
        this.world.bodyEditorLoader.attachFixture(this.body, "wall-horz", fd, 0.5f);
        this.wall_h_fix = this.body.getFixtureList().get(1);
        this.world.bodyEditorLoader.attachFixture(this.body, "wall-vert", fd, 0.5f);
        this.wall_v_fix = this.body.getFixtureList().get(2);
        this.world.bodyEditorLoader.attachFixture(this.body, "wall-corner-east", fd, 0.5f);
        this.wall_ce_fix = this.body.getFixtureList().get(3);
        this.world.bodyEditorLoader.attachFixture(this.body, "wall-corner-west", fd, 0.5f);
        this.wall_cw_fix = this.body.getFixtureList().get(4);
    }

    @Override
    protected void createDrawables() {
        String spriteName = this.getBaseDrawableName();
        this.variation = 1;
        if (this.x % 2 == 0) {
            if (this.y % 2 != 0) {
                this.variation = 2;
            }
        } else if (this.y % 2 == 0) {
            this.variation = 2;
        }
        super.createDrawables(spriteName + "corner" + this.variation, this.world.gameScreen.mainGroup);
        float yOffset = -(15.0f * Tile.SCALE);
        this.cornerNorth = new Image(this.world.gameScreen.skin.getDrawable(this.getBaseDrawableName() + "vert"));
        this.cornerNorth.setSize(drawableWidth, drawableHeight);
        this.cornerNorth.setPosition(Tile.TILE_SIZE / 2.0f, yOffset, 4);
        this.group.addActor(this.cornerNorth);
        this.cornerEast = new Image(this.world.gameScreen.skin.getDrawable(spriteName + "corner-east"));
        this.cornerEast.setSize(drawableWidth, drawableHeight);
        this.cornerEast.setPosition(Tile.TILE_SIZE / 2.0f, yOffset, 4);
        this.group.addActor(this.cornerEast);
        this.cornerWest = new Image(this.world.gameScreen.skin.getDrawable(spriteName + "corner-west"));
        this.cornerWest.setSize(drawableWidth, drawableHeight);
        this.cornerWest.setPosition(Tile.TILE_SIZE / 2.0f, yOffset, 4);
        this.group.addActor(this.cornerWest);
        this.image.setSize(drawableWidth, drawableHeight);
        this.image.setOrigin(1);
        this.image.setPosition(Tile.TILE_SIZE / 2.0f, yOffset, 4);
        this.image.toFront();
        if (MathUtils.randomBoolean()) {
            this.image.setScaleX(-1.0f);
        }
        this.group.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + TILE_SIZE / 2.0f));
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
        this.wall_c_fix.setSensor(false);
        this.wall_h_fix.setSensor(true);
        this.wall_v_fix.setSensor(!this.northWall);
        this.wall_ce_fix.setSensor(!this.eastWall);
        this.wall_cw_fix.setSensor(!this.westWall);
        WallTypes newType = WallTypes.corner;
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable(spriteName + "corner" + this.variation));
        this.cornerEast.setVisible(this.eastWall);
        this.cornerWest.setVisible(this.westWall);
        this.cornerNorth.setVisible(this.northWall);
        if (this.northWall && this.southWall && !this.eastWall && !this.westWall) {
            this.image.setDrawable(this.world.gameScreen.skin.getDrawable(spriteName + "vert"));
            this.cornerEast.setVisible(false);
            this.cornerWest.setVisible(false);
            this.wall_c_fix.setSensor(true);
            this.wall_h_fix.setSensor(true);
            this.wall_v_fix.setSensor(false);
            this.wall_ce_fix.setSensor(true);
            this.wall_cw_fix.setSensor(true);
            newType = WallTypes.vert;
        }
        if (!this.northWall && !this.southWall && this.eastWall && this.westWall) {
            this.image.setDrawable(this.world.gameScreen.skin.getDrawable(spriteName + "horz"));
            this.cornerEast.setVisible(false);
            this.cornerWest.setVisible(false);
            this.wall_c_fix.setSensor(true);
            this.wall_h_fix.setSensor(false);
            this.wall_v_fix.setSensor(true);
            this.wall_ce_fix.setSensor(true);
            this.wall_cw_fix.setSensor(true);
            newType = WallTypes.horz;
        }
        this.changeType(newType);
    }

    protected boolean isNeighborWall(int x, int y) {
        Tile t = this.world.getTile(x, y);
        if (t != null) {
            return t instanceof BarrierWall && !(t instanceof Gate);
        }
        return false;
    }

    protected void changeType(WallTypes newType) {
        this.wallType = newType;
        if (MathUtils.randomBoolean()) {
            this.image.setColor(0.95f, 0.95f, 0.95f, 1.0f);
        }
    }

    @Override
    public void setupLight() {
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "stonewall";
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

    @Override
    public boolean canInteract(Player player) {
        return false;
    }

    public static enum WallTypes {
        corner,
        horz,
        vert;

    }
}

