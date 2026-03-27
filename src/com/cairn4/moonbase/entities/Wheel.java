/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.entities.BuggieTracks;
import com.cairn4.moonbase.entities.Vehicle;

public class Wheel {
    public Vehicle vehicle;
    public boolean functioning;
    public boolean turning;
    public boolean isPowered;
    public boolean reversedTurning;
    public Body wheel;
    Vector2 offset;
    Image wheelImage1;
    int imageNum = 1;
    BuggieTracks tracks;

    public Wheel(Vehicle vehicle, Vector2 posOffset, boolean revolving, boolean powered, boolean reversedTurning) {
        this.vehicle = vehicle;
        this.turning = revolving;
        this.isPowered = powered;
        this.reversedTurning = reversedTurning;
        this.functioning = true;
        BodyDef wheelBodyDef = new BodyDef();
        wheelBodyDef.type = BodyDef.BodyType.DynamicBody;
        wheelBodyDef.angle = vehicle.body.getAngle();
        this.offset = vehicle.body.getWorldPoint(new Vector2(posOffset.x / 256.0f, posOffset.y / 256.0f));
        wheelBodyDef.position.set(this.offset.x, this.offset.y);
        this.wheel = vehicle.world.b2dWorld.createBody(wheelBodyDef);
        this.wheel.setUserData(vehicle);
        this.tracks = new BuggieTracks(vehicle.world.gameScreen, this);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.09765625f, 0.0390625f);
        FixtureDef wheelFixDef = new FixtureDef();
        wheelFixDef.density = 1.0f;
        wheelFixDef.isSensor = true;
        wheelFixDef.shape = shape;
        this.wheel.createFixture(wheelFixDef);
        if (revolving) {
            RevoluteJointDef jointDef = new RevoluteJointDef();
            jointDef.initialize(vehicle.body, this.wheel, this.offset);
            jointDef.collideConnected = false;
            jointDef.enableMotor = false;
            vehicle.world.b2dWorld.createJoint(jointDef);
        } else {
            PrismaticJointDef jointDef = new PrismaticJointDef();
            jointDef.initialize(vehicle.body, this.wheel, vehicle.body.getWorldCenter(), new Vector2(1.0f, 0.0f));
            jointDef.enableLimit = true;
            jointDef.lowerTranslation = 0.0f;
            jointDef.upperTranslation = 0.0f;
            jointDef.collideConnected = false;
            vehicle.world.b2dWorld.createJoint(jointDef);
        }
        this.wheelImage1 = new Image(vehicle.world.gameScreen.skin.getDrawable("buggie-wheel"));
        this.wheelImage1.setSize(this.wheelImage1.getWidth() / 1.5f, this.wheelImage1.getHeight() / 1.5f);
        this.wheelImage1.setOrigin(1);
        this.wheelImage1.setPosition(posOffset.x, posOffset.y, 1);
        vehicle.group.addActor(this.wheelImage1);
        this.wheelImage1.toBack();
    }

    public void setAngle(float angle) {
        if (this.reversedTurning) {
            angle *= -1.0f;
        }
        float newAngle = this.vehicle.body.getAngle() + angle * ((float)Math.PI / 180);
        Vector2 pos = this.wheel.getTransform().getPosition();
        this.wheel.setTransform(pos.x, pos.y, newAngle);
        this.wheelImage1.setRotation(this.wheel.getAngle() * 57.295776f - this.vehicle.group.getRotation());
    }

    public float getWorldRotation() {
        return this.vehicle.group.getRotation() + this.wheelImage1.getRotation();
    }

    public Vector2 getLocalVelocity() {
        Vector2 res = this.vehicle.body.getLocalVector(this.wheel.getLinearVelocityFromLocalPoint(new Vector2(0.0f, 0.0f)));
        return new Vector2(res.x, res.y);
    }

    public Vector2 getDirectionVector() {
        Vector2 vel = this.getLocalVelocity();
        if (vel.x > 0.0f) {
            vel.set(1.0f, 0.0f);
        } else {
            vel.set(-1.0f, 0.0f);
        }
        return vel.rotateRad(this.wheel.getAngle());
    }

    public void killOrthoVelocity() {
        Vector2 localP = new Vector2(0.0f, 0.0f);
        Vector2 velocity = this.wheel.getLinearVelocityFromLocalPoint(localP);
        float r = this.wheel.getTransform().getRotation();
        Vector2 sideways = new Vector2((float)(-Math.sin(r)), (float)Math.cos(r));
        sideways.scl(velocity.dot(sideways));
        this.wheel.setLinearVelocity(sideways);
    }

    public void killSidewaysVelocity(float amount) {
        Vector2 vel = this.wheel.getLinearVelocity();
        Vector2 sideways_axis = this.getDirectionVector();
        float dotprod = Vector2.dot(vel.x, vel.y, sideways_axis.x, sideways_axis.y);
        Vector2 kv = new Vector2(sideways_axis.x * dotprod * amount, sideways_axis.y * dotprod * amount);
        this.wheel.setLinearVelocity(kv.x, kv.y);
    }

    public float getXPos() {
        return this.wheel.getWorldCenter().x * 256.0f;
    }

    public float getYPos() {
        return this.wheel.getWorldCenter().y * 256.0f;
    }

    public void changeWheelFrame() {
        --this.imageNum;
        if (this.imageNum < 1) {
            this.imageNum = 3;
        }
        this.wheelImage1.setDrawable(this.vehicle.world.gameScreen.skin.getDrawable("buggie-wheel-" + this.imageNum));
    }

    public void damage() {
        this.functioning = false;
        this.wheelImage1.setDrawable(this.vehicle.world.gameScreen.skin.getDrawable("buggie-wheel-broken"));
    }

    public void fix() {
        this.functioning = true;
        this.wheelImage1.setDrawable(this.vehicle.world.gameScreen.skin.getDrawable("buggie-wheel-" + this.imageNum));
    }

    public void destroy() {
        this.vehicle.world.b2dWorld.destroyBody(this.wheel);
    }
}

