/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AreaDamage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.worlddata.ProjectileData;

public class Projectile
extends Entity {
    private states currentState;
    private ProjectileData data;
    public Vector2 velocity = new Vector2(0.0f, 0.0f);
    public Vector2 position = new Vector2(this.spawnPosX, this.spawnPosY);
    public float maxDistance = 1000.0f;
    private float angleOffset;
    public float minHitDamage;
    public float maxHitDamage;
    public float splashDamageRadius = 100.0f;
    public float splashDamage = 10.0f;
    public Vector2 worldTargetPos = new Vector2(0.0f, 0.0f);
    public float rotationSpin = 0.0f;
    public float arcHeight = 0.0f;
    public float arcOffset = 0.0f;
    private Image shadowImage;
    private float travelTime;
    private PointLight light;
    private float lightAlpha;
    private float lightDecayLength = 0.3f;
    private float lightDecayTimer;
    private float decayLength = 0.1f;
    private float decayTimer;
    private ParticleActor bulletParticles;
    protected short categoryBits;
    protected short maskBits;
    public Body body;

    public Projectile(World world) {
        super(world, 0.0f, 0.0f);
        this.currentState = states.start;
    }

    public Projectile(World world, float xPos, float yPos, float rotation) {
        super(world, xPos, yPos);
        this.currentState = states.start;
    }

    public void setData(ProjectileData pd) {
        this.data = pd;
    }

    public void setSpawnPosition(float xCenter, float yCenter) {
        this.spawnPosX = xCenter;
        this.spawnPosY = yCenter;
    }

    public void init() {
        this.position.set(this.spawnPosX, this.spawnPosY);
        this.arcHeight = 200.0f;
        this.group = new Group();
        this.world.gameScreen.addToStage(this.group, this.world.gameScreen.mainGroup);
        this.shadowImage = new Image(this.world.gameScreen.skin.getDrawable("small-gradient-circle"));
        this.shadowImage.setSize(80.0f, 50.0f);
        this.shadowImage.setColor(0.0f, 0.0f, 0.0f, 0.5f);
        this.shadowImage.setPosition(0.0f, -30.0f, 1);
        this.group.addActor(this.shadowImage);
        this.image = new Image(this.world.gameScreen.skin.getDrawable(this.data.drawable));
        this.image.setSize(this.data.imageWidth, this.data.imageHeight);
        this.image.setOrigin(1);
        this.image.setPosition(0.0f, 0.0f, 1);
        this.group.addActor(this.image);
        this.image.setRotation(this.velocity.angle());
        if (!this.data.bulletParticles.equals("")) {
            this.addBulletParticles();
        }
        this.maxDistance = 600.0f;
        this.maxDistance = this.position.dst(this.worldTargetPos);
        this.arcHeight = this.maxDistance * this.data.arcMultiplier;
        this.velocity = new Vector2(this.data.velocity, 0.0f);
        float angle = this.worldTargetPos.cpy().sub(this.spawnPosX, this.spawnPosY).angle();
        this.velocity.rotate(angle);
        this.velocity.rotate(this.angleOffset);
        this.worldTargetPos.rotateAround(this.position, this.angleOffset);
        if (this.data.bullet) {
            this.maxDistance = 600.0f;
            this.setupPhysics();
        }
        if (this.data.attachLight) {
            Color lightColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
            try {
                lightColor = Color.valueOf(this.data.lightHexColor);
                this.lightAlpha = lightColor.a;
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.light = new PointLight(this.world.rayHandler, 10, lightColor, this.data.lightRadius / 256.0f, this.spawnPosX / 256.0f, this.spawnPosY / 256.0f);
            this.light.setXray(true);
        }
    }

    public void setupPhysics() {
        this.categoryBits = (short)16;
        this.maskBits = (short)70;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        CircleShape shape = new CircleShape();
        shape.setRadius(this.data.radius);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = this.categoryBits;
        fdef.filter.maskBits = (short)(this.maskBits + this.categoryBits);
        fdef.density = 1.0f;
        fdef.friction = 0.1f;
        fdef.restitution = 0.1f;
        Vector2 pos = new Vector2(this.position.x / 256.0f, this.position.y / 256.0f);
        this.body = this.world.b2dWorld.createBody(bodyDef);
        this.body.setTransform(new Vector2(pos.x, pos.y), 0.0f);
        this.body.setFixedRotation(true);
        this.body.setUserData(this);
        this.body.setBullet(true);
        shape.setRadius(this.data.radius / 256.0f);
        this.body.createFixture(fdef);
        this.body.setLinearVelocity(this.velocity.x / 256.0f, this.velocity.y / 256.0f);
    }

    public void setAngleOffest(float a) {
        System.out.println("setAngleOffset: " + a);
        this.angleOffset = a;
    }

    private void addBulletParticles() {
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get(this.data.bulletParticles, ParticleEffect.class));
        this.bulletParticles = new ParticleActor(p, false);
        this.world.gameScreen.topGroup.addActor(this.bulletParticles);
        this.bulletParticles.setPosition(this.position.x, this.position.y);
        this.bulletParticles.pfx.start();
    }

    @Override
    public void update(float delta) {
        switch (this.currentState) {
            case start: {
                if (this.data.bullet) {
                    Vector2 pos = new Vector2(this.position.x / 256.0f, this.position.y / 256.0f);
                    this.body.setTransform(new Vector2(pos.x, pos.y), 0.0f);
                }
                this.currentState = states.flying;
                break;
            }
            case flying: {
                float dist;
                if (this.bulletParticles != null) {
                    this.bulletParticles.setPosition(this.position.x, this.position.y);
                }
                if (this.data.bullet) {
                    if (this.body != null) {
                        float progress;
                        this.position.set(this.body.getPosition().x * 256.0f, this.body.getPosition().y * 256.0f);
                        this.group.setPosition(this.position.x, this.position.y);
                        if (this.data.alignToVelocity) {
                            this.image.setRotation(this.velocity.angle());
                        }
                        if ((progress = (dist = new Vector2(this.spawnPosX, this.spawnPosY).dst(this.position)) / this.maxDistance) > 1.0f) {
                            this.startDecay();
                        }
                    }
                } else {
                    float yV;
                    this.position.add(this.velocity.x * delta, this.velocity.y * delta);
                    this.group.setPosition(this.position.x, this.position.y);
                    dist = new Vector2(this.spawnPosX, this.spawnPosY).dst(this.position);
                    float progress = dist / this.maxDistance;
                    this.arcOffset = progress < 0.5f ? Interpolation.circleOut.apply(0.0f, this.arcHeight, progress * 2.0f) : Interpolation.sineIn.apply(this.arcHeight, 0.0f, (progress - 0.5f) * 2.0f);
                    Vector2 rV = new Vector2(this.velocity.x, this.velocity.y);
                    rV.nor();
                    rV.y = yV = progress - 0.5f;
                    if (this.data.alignToVelocity) {
                        this.image.setRotation(-rV.angle());
                    } else {
                        float dir = this.velocity.x > 0.0f ? -1.0f : 1.0f;
                        this.image.rotateBy(this.data.rotationSpeed * delta * dir);
                    }
                    this.image.setPosition(0.0f, this.arcOffset, 1);
                    if (this.position.dst(this.worldTargetPos) < 10.0f) {
                        this.hit(null);
                    }
                }
                if (this.light != null) {
                    this.light.setPosition(this.position.x / 256.0f, (this.position.y + this.arcOffset) / 256.0f);
                }
                if (this.bulletParticles == null) break;
                Vector2 bulletPos = this.group.localToStageCoordinates(new Vector2(this.image.getX(1), this.image.getY(1)));
                this.bulletParticles.setPosition(bulletPos.x, bulletPos.y);
                break;
            }
            case decay: {
                this.decayTimer += delta;
                if (this.decayTimer > this.decayLength) {
                    this.setFinished();
                }
                if (!this.data.bullet || this.body == null) break;
                this.group.setPosition(this.body.getPosition().x * 256.0f, this.body.getPosition().y * 256.0f);
                break;
            }
            case finished: {
                if (this.body != null && this.body.isActive()) {
                    this.body.setActive(false);
                }
                this.lightDecayTimer += delta;
                if (this.lightDecayTimer > this.lightDecayLength) {
                    this.readyToRemove = true;
                }
                this.position.add(this.velocity.x * delta, this.velocity.y * delta);
                this.group.setPosition(this.position.x, this.position.y);
                if (this.group != null) {
                    this.group.getColor().a = 1.0f - this.lightDecayTimer / this.lightDecayLength;
                }
                float a = (1.0f - this.lightDecayTimer / this.lightDecayLength) * this.lightAlpha;
                if (this.light == null) break;
                this.light.setColor(this.light.getColor().r, this.light.getColor().g, this.light.getColor().b, a);
                break;
            }
        }
    }

    public void hit(Fixture fixture) {
        if (this.currentState == states.flying) {
            if (this.body != null) {
                this.position.set(this.body.getPosition().x * 256.0f, this.body.getPosition().y * 256.0f);
                this.body.setLinearVelocity(Vector2.Zero);
            }
            this.velocity.set(0.0f, 0.0f);
            this.setFinished();
            this.group.clearChildren();
            if (this.bulletParticles != null) {
                this.bulletParticles.pfx.allowCompletion();
            }
            if (!this.data.impactParticles.equals("")) {
                this.hitFx();
            }
            if (fixture != null) {
                MoonBase.log("Bullet Hit");
                if (fixture.getUserData() instanceof Player) {
                    Player player = (Player)fixture.getUserData();
                    player.playerStatus.takeHitDamage(MathUtils.random(this.minHitDamage, this.maxHitDamage));
                }
                if (fixture.getUserData() instanceof Vehicle) {
                    Vehicle v = (Vehicle)fixture.getUserData();
                    v.takeDamage(MathUtils.random(this.minHitDamage, this.maxHitDamage), this.data.damageDef);
                }
            }
            if (this.splashDamage > 0.0f) {
                this.dealSplashDamage();
            }
        }
    }

    private void addWarningZone() {
    }

    public void startDecay() {
        MoonBase.debug("projectile state -> decay");
        this.currentState = states.decay;
        this.decayTimer = 0.0f;
        this.decayLength = this.data.decayLength;
        if (this.bulletParticles != null) {
            this.bulletParticles.pfx.allowCompletion();
        }
    }

    public void setFinished() {
        MoonBase.debug("projectile state -> finished");
        this.currentState = states.finished;
        this.lightDecayTimer = 0.0f;
        this.lightDecayLength = this.data.lightDecayLength;
    }

    private void dealBulletDamage(Contact contact) {
    }

    private void dealSplashDamage() {
        AreaDamage.damage(this.world, this.position.x, this.position.y, this.splashDamageRadius, this.splashDamage, this.data.damageDef);
    }

    public void hitFx() {
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get(this.data.impactParticles, ParticleEffect.class));
        ParticleActor dropParticles = new ParticleActor(p, true);
        this.world.gameScreen.topGroup.addActor(dropParticles);
        dropParticles.setPosition(this.position.x, this.position.y);
        dropParticles.pfx.start();
        if (this.data.impactParticleScale != 0.0f) {
            dropParticles.setScale(this.data.impactParticleScale);
        }
        if (!this.data.impactDecals.isEmpty()) {
            float scaleX;
            int decalIndex = MathUtils.random(0, this.data.impactDecals.size() - 1);
            Image impactDecal = new Image(this.world.gameScreen.skin.getDrawable(this.data.impactDecals.get(decalIndex)));
            impactDecal.setOrigin(1);
            this.world.gameScreen.floorGroup.addActor(impactDecal);
            impactDecal.setPosition(this.position.x, this.position.y, 1);
            float f = scaleX = MathUtils.randomBoolean() ? 1.0f : -1.0f;
            if (this.data.impactDecalScale == 0.0f) {
                this.data.impactDecalScale = 1.0f;
            }
            impactDecal.setScale(0.5f * scaleX * this.data.impactDecalScale, 0.5f * this.data.impactDecalScale);
            impactDecal.addAction(Actions.parallel((Action)Actions.scaleTo(1.0f * scaleX * this.data.impactDecalScale, 1.0f * this.data.impactDecalScale, 0.2f, Interpolation.circleOut), (Action)Actions.fadeOut(3.0f)));
        }
    }

    @Override
    public void remove() {
        if (this.light != null) {
            this.light.remove();
        }
        this.group.remove();
        if (this.bulletParticles != null) {
            this.bulletParticles.remove();
        }
    }

    @Override
    public boolean isSaved() {
        return false;
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    private static enum states {
        start,
        flying,
        decay,
        finished;

    }
}

