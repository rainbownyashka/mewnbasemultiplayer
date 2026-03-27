/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AreaDamage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.TemperatureDealer;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.CooldownTimer;
import com.cairn4.moonbase.tiles.behaviors.TileSpriteAnim;
import com.cairn4.moonbase.worlddata.DamageDef;

public class LavaGeyser
extends Tile {
    private static float maxTemp = 30.0f;
    public static final float sleepDistance = Tile.TILE_SIZE * 20.0f;
    public static final float sleepDelay = 2.0f;
    public float sleepTimer = 0.0f;
    private static final float damage = 20.0f;
    private static final float damageRadius = Tile.TILE_SIZE * 1.5f;
    private final TileSpriteAnim tileSpriteAnim;
    private static final float maxLightRadius = 300.0f;
    private boolean onFire = false;
    CooldownTimer cooldownTimer;
    ParticleActor lavaSprayParticles;
    ParticleActor smokeParticles;
    Image flame;
    Image glow;
    private float shootTimer = 0.0f;
    private float shootDelay = 3.0f;
    private float minShootDelay = 4.0f;
    private float maxShootDelay = 7.0f;
    private PointLight light;
    private Color lightOnColor = new Color(1.0f, 0.45f, 0.1f, 1.0f);
    private float damageTimer = 0.0f;
    private float damageDelay = 0.5f;
    private float minLength = 1.2f;
    private float maxLength = 2.0f;
    public TemperatureDealer temperatureDealer;
    public geyserStates currentState;
    private float stateTimer = 0.0f;
    private final float anticipationFadeTime = 0.5f;
    private final float anticipationWaitTime = 0.3f;
    private final float fadeInTime = 0.2f;
    private float onFireDuration = 0.0f;
    private final float fadeOutTime = 0.3f;
    private int xOffset = MathUtils.random(-20, 20);
    private int yOffset = MathUtils.random(-20, 20);
    public DamageDef damageDef;

    public LavaGeyser(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.smokeParticles = new ParticleActor(new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/geyser-smoke.p", ParticleEffect.class)), true);
        this.smokeParticles.setPosition(this.getXCenter(), this.getYCenter() + 10.0f);
        this.smokeParticles.pfx.start();
        world.gameScreen.mainGroup.addActor(this.smokeParticles);
        this.lavaSprayParticles = new ParticleActor(new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/lava-spray.p", ParticleEffect.class)), false);
        this.lavaSprayParticles.setPosition(this.getXCenter(), this.getYCenter() + 20.0f);
        this.lavaSprayParticles.pfx.allowCompletion();
        world.gameScreen.mainGroup.addActor(this.lavaSprayParticles);
        this.setupLight();
        this.temperatureDealer = new TemperatureDealer(this.world, this.getXCenter(), this.getYCenter());
        this.temperatureDealer.radius = damageRadius;
        this.temperatureDealer.temp = 0.0f;
        this.temperatureDealer.duration = 0.0f;
        this.tileSpriteAnim = new TileSpriteAnim();
        this.tileSpriteAnim.fps = 15.0f;
        this.tileSpriteAnim.spriteList.add("volcano/firegeyser1");
        this.tileSpriteAnim.spriteList.add("volcano/firegeyser2");
        this.tileSpriteAnim.spriteList.add("volcano/firegeyser3");
        this.tileSpriteAnim.spriteList.add("volcano/firegeyser4");
        this.tileSpriteAnim.skinRef = world.gameScreen.skin;
        this.tileSpriteAnim.image = this.flame;
        this.behaviorList.add(this.tileSpriteAnim);
        this.currentState = geyserStates.off;
        this.damageDef = new DamageDef();
        this.damageDef.damageType = DamageDef.DamageTypes.fire;
    }

    public void setupLight() {
        this.light = new PointLight(this.world.rayHandler, 24, this.lightOnColor, 1.171875f, this.getXCenter() / 256.0f, this.getYCenter() / 256.0f);
        this.light.setSoftnessLength(1.5625f);
        this.light.attachToBody(this.body);
        this.light.setIgnoreAttachedBody(true);
        this.light.setXray(true);
        this.light.setStaticLight(true);
    }

    @Override
    public void createDrawables() {
        super.createDrawables();
        int r = MathUtils.randomBoolean() ? 1 : 2;
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable("lavageyser" + r));
        this.image.setSize(230.0f * Tile.SCALE, 230.0f * Tile.SCALE);
        this.image.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.glow = new AdditiveImage(this.world.gameScreen.skin.getDrawable("small-gradient-circle"));
        this.glow.setColor(1.0f, 0.45f, 0.2f, 0.0f);
        this.glow.setSize(Tile.TILE_SIZE * 2.0f, Tile.TILE_SIZE * 2.0f);
        this.glow.setOrigin(1);
        this.glow.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f + 20.0f, 1);
        this.group.addActor(this.glow);
        this.flame = new AdditiveImage(this.world.gameScreen.skin.getDrawable("volcano/firegeyser1"));
        this.flame.setSize(110.0f * Tile.SCALE, 227.0f * Tile.SCALE);
        this.flame.setPosition(this.getXCenter() + 3.0f, this.getYCenter() + 50.0f, 1);
        this.flame.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        this.flame.setOrigin(55.0f * Tile.SCALE, 5.0f * Tile.SCALE);
        this.flame.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + TILE_SIZE / 2.0f + 20.0f));
        this.world.gameScreen.addToStage(this.flame, this.world.gameScreen.mainGroup);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        if (this.group != null && this.group.isVisible()) {
            this.stateTimer += delta;
            float a = 0.001f;
            switch (this.currentState) {
                case sleep: {
                    this.sleepTimer += delta;
                    if (!(this.sleepTimer > 2.0f)) break;
                    this.sleepTimer = 0.0f;
                    this.checkForSleep();
                    break;
                }
                case off: {
                    this.flame.setColor(1.0f, 1.0f, 1.0f, 0.0f);
                    this.flame.setScale(0.25f, 0.0f);
                    this.light.setDistance(0.0f);
                    this.light.setActive(false);
                    if (!(this.stateTimer > this.shootDelay)) break;
                    this.nextState();
                    break;
                }
                case anticipationFade: {
                    a = this.stateTimer / 0.5f;
                    a = MathUtils.clamp(a, 1.0E-4f, 1.0f);
                    this.light.setActive(true);
                    this.light.setColor(this.lightOnColor.r, this.lightOnColor.g, this.lightOnColor.b, a);
                    this.light.setDistance(300.0f * a * 0.5f / 256.0f);
                    this.flame.setColor(1.0f, 1.0f, 1.0f, a);
                    this.flame.setScale(0.35f * a + 0.25f, 0.3f * a);
                    if (!(this.stateTimer >= 0.5f)) break;
                    this.nextState();
                    break;
                }
                case anticipationWait: {
                    this.flame.setScale(0.6f, 0.3f);
                    this.flame.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    if (!(this.stateTimer >= 0.3f)) break;
                    this.nextState();
                    break;
                }
                case fadein: {
                    a = this.stateTimer / 0.2f;
                    a = MathUtils.clamp(a, 1.0E-4f, 1.0f);
                    this.flame.setScale(0.4f * a + 0.6f, 0.7f * a + 0.3f);
                    this.flame.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    this.light.setDistance((0.5f * a + 0.5f) * 300.0f / 256.0f);
                    if (!(this.stateTimer >= 0.2f)) break;
                    this.nextState();
                    break;
                }
                case onfire: {
                    this.flame.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    this.flame.setScale(1.0f, 1.0f);
                    if (!(this.stateTimer >= this.onFireDuration)) break;
                    this.nextState();
                    break;
                }
                case fadeout: {
                    a = this.stateTimer / 0.3f;
                    if (a <= 0.0f) {
                        a = 1.0E-4f;
                    }
                    this.flame.setColor(1.0f, 1.0f, 1.0f, -a + 1.0f);
                    this.flame.setScale(1.0f - 0.5f * a, 0.5f - 0.5f * a);
                    if (!(this.stateTimer >= 0.3f)) break;
                    this.nextState();
                }
            }
            if (this.currentState != geyserStates.sleep) {
                this.light.setColor(this.lightOnColor.r, this.lightOnColor.g, this.lightOnColor.b, this.flame.getColor().a);
                if (this.onFire) {
                    this.damageTimer += delta;
                    if (this.damageTimer > this.damageDelay) {
                        this.damageTimer = 0.0f;
                        AreaDamage.damage(this.world, this.getXCenter(), this.getYCenter(), damageRadius, 20.0f, this.damageDef);
                    }
                }
            }
        }
    }

    private void checkForSleep() {
        Player p = this.world.getPlayer();
        if (p != null) {
            float distToPlayer = Vector2.dst(this.getXCenter(), this.getYCenter(), p.getXPos(), p.getYPos());
            if (distToPlayer > sleepDistance) {
                if (this.currentState != geyserStates.sleep) {
                    this.setState(geyserStates.sleep);
                }
            } else if (this.currentState == geyserStates.sleep) {
                this.setState(geyserStates.off);
            }
        }
    }

    private void nextState() {
        this.stateTimer = 0.0f;
        switch (this.currentState) {
            case sleep: {
                break;
            }
            case off: {
                this.setState(geyserStates.anticipationFade);
                break;
            }
            case anticipationFade: {
                this.setState(geyserStates.anticipationWait);
                break;
            }
            case anticipationWait: {
                this.setState(geyserStates.fadein);
                break;
            }
            case fadein: {
                this.setState(geyserStates.onfire);
                break;
            }
            case onfire: {
                this.setState(geyserStates.fadeout);
                break;
            }
            case fadeout: {
                this.setState(geyserStates.off);
                this.checkForSleep();
            }
        }
    }

    private void setState(geyserStates newState) {
        switch (newState) {
            case sleep: {
                this.setOnFire(false);
                this.light.setActive(false);
                this.lavaSprayParticles.pfx.allowCompletion();
                this.smokeParticles.pfx.allowCompletion();
                break;
            }
            case off: {
                this.shootDelay = MathUtils.random(this.minShootDelay, this.maxShootDelay);
                this.setOnFire(false);
                this.tileSpriteAnim.stop();
                this.flame.setVisible(false);
                this.group.setVisible(true);
                this.smokeParticles.pfx.start();
                break;
            }
            case anticipationFade: {
                this.flame.setDrawable(this.world.gameScreen.skin.getDrawable("volcano/firegeyser_small"));
                this.flame.setVisible(true);
                break;
            }
            case anticipationWait: {
                break;
            }
            case fadein: {
                this.lavaSprayParticles.pfx.start();
                this.tileSpriteAnim.play();
                this.setOnFire(true);
                break;
            }
            case onfire: {
                this.onFireDuration = MathUtils.random(this.minLength, this.maxLength);
                break;
            }
            case fadeout: {
                this.tileSpriteAnim.stop();
                this.flame.setDrawable(this.world.gameScreen.skin.getDrawable("volcano/firegeyser_small"));
                this.lavaSprayParticles.pfx.allowCompletion();
            }
        }
        this.currentState = newState;
    }

    private void setOnFire(boolean on) {
        this.onFire = on;
        this.damageTimer = 0.0f;
        if (on) {
            // empty if block
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        this.smokeParticles.remove();
        this.light.remove();
    }

    @Override
    public Color getMinimapColor() {
        return null;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            this.world.gameScreen.addToStage(this.group, this.world.gameScreen.floorGroup);
            this.world.gameScreen.addToStage(this.flame, this.world.gameScreen.mainGroup);
            this.light.setActive(true);
            if (this.currentState == geyserStates.sleep) {
                this.setState(geyserStates.off);
            }
        } else {
            this.group.remove();
            this.flame.remove();
            this.light.setActive(false);
            if (this.currentState != geyserStates.sleep) {
                this.setState(geyserStates.sleep);
            }
        }
    }

    private static enum geyserStates {
        sleep,
        off,
        anticipationFade,
        anticipationWait,
        fadein,
        onfire,
        fadeout;

    }
}

