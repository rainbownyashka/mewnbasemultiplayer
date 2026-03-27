/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.SpineActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.RepairDroneBase;
import com.cairn4.moonbase.tiles.Tile;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import java.util.HashMap;

public class RepairDrone
extends Entity {
    public RepairDroneBase homeBase;
    public GridPoint2 homeBaseCoord;
    public BaseModule targetBase;
    public GridPoint2 targetBaseCoord;
    public Vector2 targetPos;
    private Vector2 _groupPos;
    private PointLight light;
    private Image shadow;
    private float linearVelocity = 0.0f;
    private static final float maxVelocity = 3.0f;
    private static final float acceleration = 5.0f;
    Vector2 velocity;
    public float fixTimer;
    private static final float FIX_TIME = 3.0f;
    public STATES droneState;
    private Group spineGroup;
    private SpineActor spineActor;
    private Bone eyeBone;
    private final Vector2 eyeBoneBasePos = new Vector2(-0.11935999f, -9.9744f);
    private ParticleActor repairParticles;
    private static final float zSortOverride = -1000000.0f;

    public RepairDrone(World world, float xPos, float yPos, float rotation) {
        super(world, xPos, yPos);
        this.createDrawable("drone");
        this.velocity = new Vector2(0.0f, 0.0f);
        this.targetPos = new Vector2(0.0f, 0.0f);
        this._groupPos = new Vector2(0.0f, 0.0f);
        this.setState(STATES.atHome);
    }

    @Override
    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("homeBaseCoord", this.makeWorldPosGridPoint2(this.homeBase.worldX, this.homeBase.worldY));
        if (this.targetBase != null) {
            properties.put("targetBaseCoord", this.makeWorldPosGridPoint2(this.targetBase.worldX, this.targetBase.worldY));
        }
        properties.put("droneState", (Object)this.droneState);
        properties.put("fixTimer", Float.valueOf(this.fixTimer));
        return properties;
    }

    @Override
    public void loadProperties(HashMap<String, Object> p) {
        super.loadProperties(p);
        if (this.world.getTile(this.homeBaseCoord.x, this.homeBaseCoord.y) != null && this.world.getTile(this.homeBaseCoord.x, this.homeBaseCoord.y) instanceof RepairDroneBase) {
            this.homeBase = (RepairDroneBase)this.world.getTile(this.homeBaseCoord.x, this.homeBaseCoord.y);
            this.homeBase.registerDrone(this);
            this.targetPos.set(this.homeBase.getXCenter(), this.homeBase.getYCenter());
        }
        if (this.homeBase == null) {
            Gdx.app.error("MewnBase", "Can't find repair drone's home base. Destroying drone...");
            this.selfDestruct();
        }
        if (this.targetBaseCoord != null && this.world.getTile(this.targetBaseCoord.x, this.targetBaseCoord.y) != null) {
            this.setTarget((BaseModule)this.world.getTile(this.targetBaseCoord.x, this.targetBaseCoord.y));
        }
        this.setState(this.droneState);
    }

    @Override
    public void remove() {
        super.remove();
    }

    private void createRepairParticles() {
        if (this.repairParticles == null) {
            ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/repair-bolts.p", ParticleEffect.class));
            this.repairParticles = new ParticleActor(p, false);
            this.repairParticles.setPosition(0.0f, 15.0f);
            this.group.addActor(this.repairParticles);
        }
        this.repairParticles.pfx.start();
    }

    private void endRepairParticles() {
        if (this.repairParticles != null) {
            this.repairParticles.pfx.allowCompletion();
        }
    }

    private GridPoint2 makeWorldPosGridPoint2(int x, int y) {
        return new GridPoint2(x, y);
    }

    private void setState(STATES newState) {
        Gdx.app.debug("MewnBase", "Drone state changed to " + (Object)((Object)newState));
        this.droneState = newState;
        switch (this.droneState) {
            case atHome: {
                this.doHomeSleep();
                this.linearVelocity = 0.0f;
                break;
            }
            case wakeUp: {
                this.startWakeUp();
                this.linearVelocity = 0.0f;
                break;
            }
            case movingToTarget: {
                this.endRepairParticles();
                this.spineActor.addAnimation(0, "flying", true, 0.0f);
                this.spineGroup.addAction(Actions.forever(Actions.sequence((Action)Actions.moveToAligned(0.0f, 65.0f, 1, 0.35f, Interpolation.sine), (Action)Actions.moveToAligned(0.0f, 75.0f, 1, 0.35f, Interpolation.sine))));
                break;
            }
            case atTarget: {
                this.spineActor.state.setAnimation(0, "repairing", true);
                this.createRepairParticles();
                float volume = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXPos(), this.getYPos()), 1000.0f, 0.8f);
                if (volume > 0.0f) {
                    Audio.getInstance().playSound("sounds/repair.ogg", volume);
                }
                this.linearVelocity = 0.0f;
                this.spineGroup.clearActions();
                this.spineGroup.addAction(Actions.moveToAligned(0.0f, 40.0f, 1, 0.1f, Interpolation.sine));
                break;
            }
            case returnHome: {
                this.linearVelocity = 0.0f;
                this.endRepairParticles();
                this.targetBase = null;
                this.targetPos.set(this.homeBase.getXCenter(), this.homeBase.getYCenter());
                this.spineActor.state.setAnimation(0, "flying", true);
                this.spineGroup.addAction(Actions.forever(Actions.sequence((Action)Actions.moveToAligned(0.0f, 65.0f, 1, 0.35f, Interpolation.sine), (Action)Actions.moveToAligned(0.0f, 75.0f, 1, 0.35f, Interpolation.sine))));
            }
        }
    }

    private void createDrawable(String sprite) {
        this.group = new Group();
        this.group.setPosition(this.spawnPosX, this.spawnPosY);
        this.group.setUserObject(Float.valueOf(this.getYPos()));
        this.world.gameScreen.mainGroup.addActor(this.group);
        this.shadow = new Image(this.world.gameScreen.skin.getDrawable("lander-shadow"));
        this.world.gameScreen.floorGroup.addActor(this.shadow);
        this.shadow.setColor(0.0f, 0.0f, 0.0f, 0.3f);
        this.shadow.setSize(Tile.TILE_SIZE * 0.5f, Tile.TILE_SIZE * 0.3f);
        this.shadow.setPosition(this.group.getX(), this.group.getY() - 5.0f, 1);
        this.spineGroup = new Group();
        this.group.addActor(this.spineGroup);
        this.setupSpineActor();
        this.light = new PointLight(this.world.rayHandler, 100, new Color(1.0f, 0.9f, 0.8f, 0.55f), 1.171875f, this.group.getX() / 256.0f, (this.group.getY() - 20.0f) / 256.0f);
        this.light.setSoft(true);
        this.light.setIgnoreAttachedBody(true);
        this.light.setXray(true);
    }

    public void setupSpineActor() {
        this.spineActor = new SpineActor("repairdrone", 0.32f, this.world.gameScreen.skeletonRenderer);
        this.spineGroup.addActor(this.spineActor);
        this.spineGroup.setPosition(0.0f, 20.0f);
        this.spineActor.stateData.setMix("flying", "repairing", 0.1f);
        this.spineActor.stateData.setMix("repairing", "flying", 0.1f);
        this.spineActor.stateData.setMix("wakeUp", "flying", 0.1f);
        this.spineActor.stateData.setMix("flying", "beginSleep", 0.1f);
        this.spineActor.stateData.setMix("sleeping", "wakeUp", 0.15f);
        this.spineActor.stateData.setMix("beginSleep", "sleeping", 0.15f);
        this.spineActor.addAnimation(0, "sleeping", true, 0.0f);
        this.eyeBone = this.spineActor.skeleton.findBone("eye");
        AnimationState fState = this.spineActor.state;
    }

    public void setTarget(BaseModule baseModule) {
        this.targetBase = baseModule;
        this.targetPos.set(this.targetBase.getXCenter(), this.targetBase.getYCenter());
    }

    @Override
    public void update(float delta) {
        switch (this.droneState) {
            case atHome: {
                this.group.setUserObject(Float.valueOf(this.getYPos()));
                if (this.targetBase == null) break;
                this.setState(STATES.wakeUp);
                break;
            }
            case wakeUp: {
                break;
            }
            case movingToTarget: {
                this.group.setUserObject(Float.valueOf(-1000000.0f));
                this.group.toFront();
                if (this.targetBase != null) {
                    if (!this.targetBase.hasDisaster()) {
                        this.setState(STATES.returnHome);
                        break;
                    }
                    if (this.distanceCheck(this.targetBase.getXCenter(), this.targetBase.getYCenter())) {
                        this.fixTimer = 0.0f;
                        this.setState(STATES.atTarget);
                        break;
                    }
                    this.moveTo(this.targetBase.getXCenter(), this.targetBase.getYCenter(), delta);
                    break;
                }
                this.setState(STATES.returnHome);
                break;
            }
            case atTarget: {
                this.group.setUserObject(Float.valueOf(-1000000.0f));
                this.doRepairs(delta);
                break;
            }
            case returnHome: {
                this.group.setUserObject(Float.valueOf(-1000000.0f));
                this.group.toFront();
                this.targetPos.set(this.homeBase.getXCenter(), this.homeBase.getYCenter());
                if (this.distanceCheck(this.homeBase.getXCenter(), this.homeBase.getYCenter())) {
                    this.setState(STATES.atHome);
                    break;
                }
                this.moveTo(this.homeBase.getXCenter(), this.homeBase.getYCenter(), delta);
            }
        }
        this.spineActor.update(delta);
        this.shadow.setPosition(this.group.getX(), this.group.getY() - 5.0f, 1);
        this.light.setPosition(this.group.getX() / 256.0f, (this.group.getY() - 20.0f) / 256.0f);
    }

    private void startWakeUp() {
        float volume = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXPos(), this.getYPos()), 1600.0f, 1.0f);
        if (volume > 0.0f) {
            Audio.getInstance().playSound("sounds/drone-wakeup.ogg", volume);
        }
        Gdx.app.debug("MewnBase", "RepairDrone: starting wakeup");
        this.spineActor.state.setAnimation(0, "wakeUp", false);
        this.spineGroup.clearActions();
        this.spineGroup.addAction(Actions.sequence((Action)Actions.delay(0.25f), (Action)Actions.parallel((Action)Actions.moveToAligned(0.0f, 70.0f, 1, 0.35f, Interpolation.sine)), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                RepairDrone.this.setState(STATES.movingToTarget);
            }
        })));
        this.light.setActive(true);
    }

    private void doHomeSleep() {
        this.spineGroup.clearActions();
        this.eyeBone.setPosition(this.eyeBoneBasePos.x, this.eyeBoneBasePos.y);
        this.spineActor.state.setAnimation(0, "beginSleep", false);
        this.spineActor.state.addAnimation(0, "sleeping", true, 0.0f);
        this.spineGroup.addAction(Actions.parallel((Action)Actions.moveToAligned(0.0f, 20.0f, 1, 0.35f, Interpolation.sine)));
        this.light.setActive(false);
        if (this.homeBase != null) {
            this.setPowered(this.homeBase.hasPower);
        }
    }

    private void doRepairs(float delta) {
        this.fixTimer += delta;
        if (this.fixTimer > 3.0f) {
            if (this.targetBase.baseDisaster != null && this.targetBase.hasDisaster()) {
                this.doHappyAnim();
                this.targetBase.fixDisaster();
                this.targetBase = null;
                this.endRepairParticles();
                this.getNextTarget();
            }
            if (this.targetBase == null) {
                this.setState(STATES.returnHome);
            } else {
                this.setState(STATES.movingToTarget);
            }
        }
    }

    public void getNextTarget() {
        this.homeBase.sendDisasterToDrone();
    }

    private void doHappyAnim() {
        this.spineActor.state.setAnimation(2, "happy", false);
        this.spineActor.state.addEmptyAnimation(2, 0.0f, 0.0f);
    }

    private void moveTo(float x, float y, float delta) {
        this._groupPos.set(this.group.getX(), this.group.getY());
        this.linearVelocity += 5.0f * delta;
        this.linearVelocity = MathUtils.clamp(this.linearVelocity, 0.0f, 3.0f);
        this.velocity.set(this.linearVelocity, 0.0f);
        float angle = new Vector2(x, y).sub(this._groupPos.cpy()).angle();
        this.velocity.setAngle(angle);
        this.velocity.limit(5.0f);
        this._groupPos.add(this.velocity);
        this.group.setPosition(this._groupPos.x, this._groupPos.y);
        this.eyeBone.setPosition(this.eyeBoneBasePos.x + this.velocity.x * 2.0f, this.eyeBoneBasePos.y + this.velocity.y * 1.5f);
    }

    public void selfDestruct() {
        this.shadow.remove();
        this.readyToRemove = true;
        this.group.remove();
    }

    private boolean distanceCheck(float x, float y) {
        this._groupPos.set(this.group.getX(), this.group.getY());
        if (this._groupPos.dst(x, y) < 25.0f) {
            this.group.addAction(Actions.sequence((Action)Actions.moveTo(x, y, 0.2f, Interpolation.sineOut)));
            return true;
        }
        return false;
    }

    public void setPowered(boolean hasPower) {
        if (this.droneState == STATES.atHome) {
            if (!hasPower) {
                this.spineActor.state.setAnimation(1, "noPower", true);
                this.spineActor.state.getCurrent(1).setAlpha(1.0f);
            } else {
                this.spineActor.state.clearTrack(1);
            }
        }
    }

    private static enum STATES {
        atHome,
        wakeUp,
        movingToTarget,
        atTarget,
        returnHome;

    }
}

