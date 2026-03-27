/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Box2dLocation;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.utils.C4Utils;

public class SteerableEntity
extends Entity
implements Steerable<Vector2> {
    protected short categoryBits;
    protected short maskBits;
    protected Body body;
    protected SteeringBehavior<Vector2> steeringBehavior;
    public static final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());
    float boundingRadius;
    boolean tagged;
    float maxLinearSpeed;
    float maxLinearAcceleration;
    float maxAngularSpeed;
    float maxAngularAcceleration;
    boolean independentFacing;

    public SteerableEntity(World world, float xPos, float yPos) {
        super(world, xPos, yPos);
    }

    public void setSteeringBehavior(SteeringBehavior<Vector2> steeringBehavior) {
        this.steeringBehavior = steeringBehavior;
    }

    protected void setupPhysics() {
        this.categoryBits = (short)8;
        this.maskBits = (short)6;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        CircleShape shape = new CircleShape();
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = this.categoryBits;
        fdef.filter.maskBits = (short)(this.maskBits + this.categoryBits);
        fdef.density = 0.1f;
        fdef.friction = 0.2f;
        fdef.restitution = 0.0f;
        Vector2 pos = new Vector2(((float)this.getXTile() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f) / 256.0f, ((float)this.getYTile() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f) / 256.0f);
        this.body = this.world.b2dWorld.createBody(bodyDef);
        this.body.setTransform(new Vector2(pos.x, pos.y), 0.0f);
        this.body.setFixedRotation(true);
        this.body.setLinearDamping(0.5f);
        this.body.setUserData(this);
        shape.setRadius(0.13671875f);
        this.body.createFixture(fdef);
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    @Override
    public Vector2 getPosition() {
        return this.body.getPosition();
    }

    @Override
    public float getOrientation() {
        return this.body.getAngle();
    }

    @Override
    public void setOrientation(float orientation) {
        this.body.setTransform(this.getPosition(), orientation);
    }

    @Override
    public Vector2 getLinearVelocity() {
        return this.body.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity() {
        return this.body.getAngularVelocity();
    }

    @Override
    public float getBoundingRadius() {
        return this.boundingRadius;
    }

    @Override
    public boolean isTagged() {
        return this.tagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return C4Utils.vectorToAngle(vector);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        return C4Utils.angleToVector(outVector, angle);
    }

    @Override
    public Location<Vector2> newLocation() {
        return new Box2dLocation();
    }

    @Override
    public float getMaxLinearSpeed() {
        return this.maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return this.maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return this.maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return this.maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcceleration = maxAngularAcceleration;
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0.001f;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        throw new UnsupportedOperationException();
    }
}

