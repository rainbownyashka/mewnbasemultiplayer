/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.NavLocator;
import com.cairn4.moonbase.tiles.behaviors.ToggleBehavior;

public class Airlock
extends HabitatModule {
    private static final Color DOOR_CLOSED_LIGHT_COLOR = new Color(0.0f, 1.0f, 1.0f, 0.6f);
    private static final Color DOOR_OPEN_LIGHT_COLOR = new Color(1.0f, 0.0f, 0.0f, 0.6f);
    public ToggleBehavior toggleBehavior;
    Image innerDoor;
    Image outerDoor;
    Image airlockWalls;
    Fixture door_e_fix;
    Fixture door_w_fix;
    AdditiveImage northLight;
    AdditiveImage southLight;
    PointLight northB2dLight;
    PointLight southB2dLight;
    private BaseModule.ORIENTATIONS outerDoorDirection;
    NavLocator navLocator;
    boolean airSealWithBase = true;
    private Image airSealWarning;

    public BaseModule.ORIENTATIONS getOuterDoorDirection() {
        return this.outerDoorDirection;
    }

    public Airlock(World world, Chunk chunk, int x, int y, BaseModule.ORIENTATIONS o) {
        super(world, chunk, x, y);
        this.type = Tile.types.habitat;
        this.blocker = false;
        this.powerPriority = 8;
        this.powerDrawRate = 3.0f;
        this.powerGenRate = 0.0f;
        this.toggleBehavior = new ToggleBehavior();
        this.toggleBehavior.on = true;
        this.behaviorList.add(this.toggleBehavior);
        this.setRotation(o);
        this.updateState();
        this.interactDuration = 0.5f;
        this.updateBases();
        this.checkConnectionForOxygen();
    }

    private void fixLight() {
        this.northB2dLight.attachToBody(this.body, 0.0f, 0.09765625f);
        this.southB2dLight.attachToBody(this.body, 0.0f, -0.09765625f);
    }

    @Override
    public void startBehaviors() {
        this.updateState();
        super.startBehaviors();
    }

    @Override
    protected void setFloorStyle() {
    }

    @Override
    protected void updateFloorSprite() {
    }

    @Override
    public boolean hasOxygen() {
        if (this.toggleBehavior != null && this.toggleBehavior.on) {
            return false;
        }
        if (this.airSealWithBase) {
            return super.hasOxygen();
        }
        return false;
    }

    private boolean checkConnectionForOxygen() {
        boolean foundHabModule = false;
        Tile t = null;
        switch (this.outerDoorDirection) {
            case north: {
                t = this.world.getTile(this.worldX, this.worldY - 1);
                break;
            }
            case east: {
                t = this.world.getTile(this.worldX - 1, this.worldY);
                break;
            }
            case south: {
                t = this.world.getTile(this.worldX, this.worldY + 1);
                break;
            }
            case west: {
                t = this.world.getTile(this.worldX + 1, this.worldY);
            }
        }
        if (t != null && t.getType() == Tile.types.habitat && !(t instanceof Airlock)) {
            foundHabModule = true;
        }
        this.airSealWithBase = foundHabModule;
        this.updateDoorLights();
        return foundHabModule;
    }

    @Override
    protected void setLight(boolean on) {
        super.setLight(on);
        if (this.northB2dLight != null && this.southB2dLight != null) {
            this.northB2dLight.setActive(on);
            this.southB2dLight.setActive(on);
        }
    }

    private void updateDoorLights() {
        if (this.airSealWithBase) {
            if (!this.toggleBehavior.on) {
                this.southLight.setColor(Color.CYAN);
                this.northLight.setColor(Color.CYAN);
                this.northB2dLight.setColor(DOOR_CLOSED_LIGHT_COLOR);
                this.southB2dLight.setColor(DOOR_CLOSED_LIGHT_COLOR);
            } else {
                this.southLight.setColor(Color.RED);
                this.northLight.setColor(Color.RED);
                this.northB2dLight.setColor(DOOR_OPEN_LIGHT_COLOR);
                this.southB2dLight.setColor(DOOR_OPEN_LIGHT_COLOR);
            }
        } else {
            this.southLight.setColor(Color.CLEAR);
            this.northLight.setColor(Color.CLEAR);
            this.northB2dLight.setColor(Color.CLEAR);
            this.southB2dLight.setColor(Color.CLEAR);
        }
        if (!this.airSealWithBase) {
            if (this.airSealWarning != null) {
                this.airSealWarning.remove();
            }
            this.componentGroup.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
            this.airSealWarning = new Image(this.world.gameScreen.skin.getDrawable("base-warning-airlock"));
            this.airSealWarning.setSize(69.0f, 57.0f);
            this.airSealWarning.setOrigin(1);
            this.airSealWarning.setColor(1.0f, 1.0f, 1.0f, 0.5f);
            this.componentGroup.addActor(this.airSealWarning);
            this.airSealWarning.setPosition(0.0f, 0.0f, 1);
            this.setWarningRotation();
        } else if (this.airSealWarning != null) {
            this.airSealWarning.remove();
        }
    }

    private void setWarningRotation() {
        float d = 0.0f;
        switch (this.outerDoorDirection) {
            case north: {
                d = 90.0f;
                break;
            }
            case south: {
                d = 270.0f;
                break;
            }
            case east: {
                d = 180.0f;
                break;
            }
            case west: {
                d = 0.0f;
            }
        }
        if (this.airSealWarning != null) {
            this.airSealWarning.setRotation(d);
        }
    }

    @Override
    protected void setupPhysics(String loaderName) {
        loaderName = "airlock";
        this.categoryBits = (short)2;
        this.maskBits = (short)92;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        FixtureDef fd = new FixtureDef();
        fd.density = 1.0f;
        fd.friction = 0.5f;
        fd.restitution = 0.3f;
        fd.filter.categoryBits = this.categoryBits;
        fd.filter.maskBits = (short)(this.maskBits + this.categoryBits);
        this.body = this.world.b2dWorld.createBody(bodyDef);
        this.body.setUserData(this);
        this.body.setTransform(new Vector2(this.getXCenter() / 256.0f, this.getYCenter() / 256.0f), 0.0f);
        this.world.bodyEditorLoader.attachFixture(this.body, loaderName, fd, 0.5f);
        int fixIndex = this.body.getFixtureList().size - 1;
        this.world.bodyEditorLoader.attachFixture(this.body, "habitat_wall_e", fd, 0.5f);
        this.door_e_fix = this.body.getFixtureList().get(++fixIndex);
        this.world.bodyEditorLoader.attachFixture(this.body, "habitat_wall_w", fd, 0.5f);
        this.door_w_fix = this.body.getFixtureList().get(++fixIndex);
        this.fixLight();
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "airlock";
    }

    @Override
    protected void createWalls() {
        this.wallsGroup = new Group();
        this.wallsGroup.setPosition(this.group.getX(), this.group.getY());
        this.wallsGroup.setOrigin(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.world.gameScreen.wallGroup.addActor(this.wallsGroup);
        this.connector_e = new Image(this.world.gameScreen.skin.getDrawable("habitat-connector-n"));
        this.connector_e.setSize(200.0f * SCALE, 128.0f * SCALE);
        this.connector_e.setOrigin(4);
        this.connector_e.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f, 4);
        this.connector_e.setRotation(270.0f);
        this.group.addActor(this.connector_e);
        this.airlockWalls = new Image(this.world.gameScreen.skin.getDrawable("airlock-walls"));
        this.airlockWalls.setSize(this.image.getWidth(), this.image.getHeight());
        this.airlockWalls.setPosition(this.image.getX(), this.image.getY());
        this.wallsGroup.addActor(this.airlockWalls);
        this.innerDoor = new Image(this.world.gameScreen.skin.getDrawable("airlock-door"));
        this.innerDoor.setSize(62.0f * SCALE, 154.0f * SCALE);
        this.innerDoor.setScaleX(-1.0f);
        this.innerDoor.setPosition(TILE_SIZE + 25.0f * SCALE, TILE_SIZE / 2.0f, 8);
        this.wallsGroup.addActor(this.innerDoor);
        this.outerDoor = new Image(this.world.gameScreen.skin.getDrawable("airlock-door"));
        this.outerDoor.setSize(62.0f * SCALE, 154.0f * SCALE);
        this.outerDoor.setPosition(0.0f - 25.0f * SCALE, TILE_SIZE / 2.0f, 8);
        this.outerDoor.setOrigin(2);
        this.wallsGroup.addActor(this.outerDoor);
        this.northLight = new AdditiveImage(this.world.gameScreen.skin.getDrawable("airlock-light"));
        this.northLight.setOrigin(1);
        this.northLight.setScale(1.0f);
        this.northLight.setColor(Color.CYAN);
        this.northLight.setPosition(TILE_SIZE / 2.0f, TILE_SIZE - 25.0f, 1);
        this.wallsGroup.addActor(this.northLight);
        this.southLight = new AdditiveImage(this.world.gameScreen.skin.getDrawable("airlock-light"));
        this.southLight.setOrigin(1);
        this.southLight.setScale(1.0f);
        this.southLight.setColor(Color.CYAN);
        this.southLight.setPosition(TILE_SIZE / 2.0f, 25.0f, 1);
        this.wallsGroup.addActor(this.southLight);
    }

    @Override
    public void setupLight() {
        super.setupLight();
        this.northB2dLight = new PointLight(this.world.rayHandler, 8, DOOR_CLOSED_LIGHT_COLOR, 0.234375f, this.getXCenter() / 256.0f, ((float)this.worldY * Tile.TILE_SIZE + (TILE_SIZE - 25.0f)) / 256.0f);
        this.northB2dLight.setXray(true);
        this.northB2dLight.setIgnoreAttachedBody(true);
        this.southB2dLight = new PointLight(this.world.rayHandler, 8, DOOR_CLOSED_LIGHT_COLOR, 0.234375f, this.getXCenter() / 256.0f, ((float)this.worldY * Tile.TILE_SIZE + 25.0f) / 256.0f);
        this.southB2dLight.setXray(true);
        this.southB2dLight.setIgnoreAttachedBody(true);
    }

    public void setRotation(BaseModule.ORIENTATIONS o) {
        this.outerDoorDirection = o;
        switch (this.outerDoorDirection) {
            case north: {
                this.body.setTransform(this.body.getPosition(), -1.5707964f);
                this.wallsGroup.setRotation(-90.0f);
                this.group.setRotation(-90.0f);
                break;
            }
            case east: {
                this.body.setTransform(this.body.getPosition(), (float)Math.PI);
                this.wallsGroup.setRotation(180.0f);
                this.group.setRotation(180.0f);
                break;
            }
            case south: {
                this.body.setTransform(this.body.getPosition(), 1.5707964f);
                this.wallsGroup.setRotation(90.0f);
                this.group.setRotation(90.0f);
                break;
            }
            case west: {
                this.body.setTransform(this.body.getPosition(), 0.0f);
                this.wallsGroup.setRotation(0.0f);
                this.group.setRotation(0.0f);
            }
        }
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("airlock-floor");
        this.group.setOrigin(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
    }

    @Override
    public void updateWalls() {
        if (this.toggleBehavior != null) {
            this.blocksEast = this.toggleBehavior.on;
            this.blocksWest = !this.toggleBehavior.on;
        } else {
            this.blocksEast = true;
            this.blocksWest = false;
        }
        this.blocksNorth = true;
        this.blocksSouth = true;
    }

    @Override
    public void update(float delta) {
        this.checkConnectionForOxygen();
        super.update(delta);
    }

    @Override
    public void interactMain(Player player) {
        this.toggleBehavior.interact(player);
        this.updateState();
    }

    protected void updateState() {
        float v;
        float pitch;
        float f = pitch = this.toggleBehavior.on ? 0.85f : 1.3f;
        if (this.airSealWithBase) {
            v = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 500.0f, 0.4f);
            Audio.getInstance().playSound("sounds/gameOverButton.ogg", v, pitch);
        } else {
            v = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 500.0f, 0.4f);
            Audio.getInstance().playSound("sounds/textAnimator2.ogg", v, pitch);
        }
        this.innerDoor.setVisible(this.toggleBehavior.on);
        if (this.toggleBehavior.on) {
            this.blocksEast = true;
            this.blocksWest = false;
            this.hasAir = false;
            this.outerDoor.clearActions();
            this.outerDoor.addAction(Actions.sequence((Action)Actions.rotateTo(-110.0f, 0.5f, Interpolation.sine)));
        } else {
            this.blocksEast = false;
            this.blocksWest = true;
            this.outerDoor.clearActions();
            this.outerDoor.addAction(Actions.sequence((Action)Actions.rotateTo(0.0f, 0.5f, Interpolation.sine)));
        }
        this.updateDoorLights();
        this.door_e_fix.setSensor(!this.blocksEast);
        this.door_w_fix.setSensor(!this.blocksWest);
        Filter fw = new Filter();
        fw.categoryBits = this.blocksWest ? (short)2 : (short)0;
        this.door_w_fix.setFilterData(fw);
        Filter fe = new Filter();
        fe.categoryBits = this.blocksEast ? (short)2 : (short)0;
        this.door_e_fix.setFilterData(fe);
    }

    @Override
    public void destroy() {
        this.northB2dLight.remove();
        this.southB2dLight.remove();
        super.destroy();
    }
}

