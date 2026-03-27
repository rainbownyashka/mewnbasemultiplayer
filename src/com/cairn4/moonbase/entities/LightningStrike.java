/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.LightningCollector;
import com.cairn4.moonbase.tiles.Tile;

public class LightningStrike
extends Entity
implements Pool.Poolable {
    public static final float PLAYER_DMG = 80.0f;
    public static final float DMG_RANGE = Tile.TILE_SIZE * 2.0f - 20.0f;
    public static final float MAX_DMG_RANGE = Tile.TILE_SIZE / 2.0f;
    private static final float minDelay = 1.5f;
    private static final float maxDelay = 1.5f;
    private boolean timerDone = false;
    private boolean decoy = true;
    private float delayTimer = 0.0f;
    private AdditiveImage preStrikeGlow;
    private AdditiveImage strikeGround;
    private ParticleActor preStrikeParticles;
    private ParticleActor strikeParticles;
    private AdditiveImage bolt;
    private AdditiveImage boltGlow;
    private Array<Drawable> strikeFrames = new Array();

    public void setDecoy(boolean isDecoy) {
        this.decoy = isDecoy;
    }

    public LightningStrike(World world, float xPos, float yPos, float rotation) {
        this(world, xPos, yPos);
        this.readyToRemove = true;
    }

    public LightningStrike(World world, float xPos, float yPos) {
        super(world, xPos, yPos);
        this.group = new Group();
        this.group.setPosition(this.spawnPosX, this.spawnPosY);
        this.world.gameScreen.worldUIStage.addActor(this.group);
        this.zSort = true;
        this.setup();
        this.startPrestrike();
    }

    @Override
    public void remove() {
        this.group.remove();
    }

    private void setup() {
        this.preStrikeGlow = new AdditiveImage(this.world.gameScreen.skin.getDrawable("lightning-bolt-ground"));
        this.preStrikeGlow.setSize(195.0f, 172.5f);
        this.preStrikeGlow.setOrigin(1);
        this.preStrikeGlow.setScale(0.75f);
        this.preStrikeGlow.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        this.preStrikeGlow.setPosition(0.0f, 0.0f, 1);
        this.group.addActor(this.preStrikeGlow);
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/lightning-prestrike.p", ParticleEffect.class));
        this.preStrikeParticles = new ParticleActor(p, false);
        this.preStrikeParticles.setPosition(0.0f, 20.0f);
        this.group.addActor(this.preStrikeParticles);
    }

    private void startPrestrike() {
        this.delayTimer = 0.0f;
        this.preStrikeGlow.addAction(Actions.sequence((Action)Actions.scaleTo(2.0f, 2.0f), (Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.parallel((Action)Actions.alpha(0.25f, 1.0f), (Action)Actions.scaleTo(3.0f, 3.0f, 1.5f))));
        this.preStrikeParticles.pfx.start();
    }

    @Override
    public void update(float delta) {
        this.delayTimer += delta;
        if (this.delayTimer > 1.5f && !this.timerDone) {
            this.timerDone = true;
            if (this.decoy) {
                this.fizzle();
            } else {
                this.doStrike();
            }
        }
    }

    private void fizzle() {
        this.preStrikeParticles.pfx.allowCompletion();
        this.preStrikeGlow.addAction(Actions.sequence((Action)Actions.fadeOut(0.5f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                LightningStrike.this.readyToRemove = true;
            }
        })));
    }

    private void doStrike() {
        Vector2 playerPos = new Vector2(this.world.player.getXPos(), this.world.player.getYPos());
        float distanceToPlayer = playerPos.dst(this.group.getX(), this.group.getY());
        float flashStrength = 1.0f - distanceToPlayer / 2000.0f;
        this.world.weatherManager.doLightningFlash(flashStrength);
        this.doSound(distanceToPlayer);
        boolean hitCollector = this.zapBases();
        if (!hitCollector) {
            this.dealDamage(distanceToPlayer);
        }
        this.preStrikeGlow.setVisible(false);
        this.preStrikeParticles.pfx.allowCompletion();
        Group boltGroup = new Group();
        this.group.addActor(boltGroup);
        this.strikeGround = new AdditiveImage(this.world.gameScreen.skin.getDrawable("lightning-bolt-ground"));
        this.strikeGround.setSize(130.0f, 115.0f);
        this.strikeGround.setOrigin(1);
        this.strikeGround.setPosition(0.0f, 0.0f, 1);
        boltGroup.addActor(this.strikeGround);
        this.bolt = new AdditiveImage(this.world.gameScreen.skin.getDrawable("lightning-bolt1"));
        this.bolt.setOrigin(4);
        this.bolt.setSize(64.0f, 800.0f);
        this.bolt.setPosition(0.0f, -5.0f, 4);
        boltGroup.addActor(this.bolt);
        this.boltGlow = new AdditiveImage(this.world.gameScreen.skin.getDrawable("lightning-bolt-glow2"));
        this.boltGlow.setOrigin(4);
        this.boltGlow.setSize(800.0f, 1200.0f);
        this.boltGlow.setPosition(0.0f, -60.0f, 4);
        this.boltGlow.setColor(1.0f, 1.0f, 1.0f, 0.6f);
        boltGroup.addActor(this.boltGlow);
        boltGroup.addAction(Actions.sequence(Actions.alpha(0.0f), Actions.alpha(1.0f, 0.05f), Actions.delay(0.05f), Actions.alpha(0.25f), Actions.delay(0.06f), Actions.alpha(1.0f, 0.025f), Actions.fadeOut(1.0f), Actions.removeActor(), Actions.run(new Runnable(){

            @Override
            public void run() {
                LightningStrike.this.readyToRemove = true;
            }
        })));
    }

    private boolean zapBases() {
        Tile t;
        int worldY;
        boolean hitCollector = false;
        int worldX = this.getWorldXTile();
        if (!this.world.isTileEmpty(worldX, worldY = this.getWorldYTile()) && (t = this.world.getTile(worldX, worldY)) instanceof BaseModule) {
            if (t instanceof LightningCollector) {
                LightningCollector lc = (LightningCollector)this.world.getTile(worldX, worldY);
                lc.hitByLightning();
                hitCollector = true;
            } else {
                BaseModule b = (BaseModule)this.world.getTile(worldX, worldY);
                if (b.usesBaseGroup()) {
                    if (b.getBaseGroup() != null) {
                        b.getBaseGroup().consumeResource(1000.0f, BaseResources.power, Float.valueOf(b.getBaseGroup().getTotalPowerStored()));
                        MoonBase.log("LightningStrike: Hit a base (" + b.getClass().getSimpleName() + ")");
                    } else {
                        MoonBase.error("LightningStrike: Hit a base (" + b.getClass().getSimpleName() + ") - but has null baseGroup");
                    }
                }
            }
        }
        return hitCollector;
    }

    private void dealDamage(float distanceToPlayer) {
        float damageAmount = 0.0f;
        if (distanceToPlayer < MAX_DMG_RANGE) {
            damageAmount = 80.0f;
        } else if (distanceToPlayer <= DMG_RANGE) {
            double distPercent = distanceToPlayer / DMG_RANGE;
            damageAmount = (float)(80.0 * (1.0 - Math.pow(distPercent, 2.0)));
        }
        if (this.world.player != null && damageAmount > 0.0f) {
            MoonBase.log("Lightning: Dist: " + distanceToPlayer + " - damage: " + damageAmount);
            if (!this.world.player.isIndoors()) {
                this.world.player.playerStatus.changeHealth(-damageAmount);
                this.zapPlayerSoundFx();
                this.world.player.playerAnimController.zapAnimation();
            }
        }
    }

    private void zapPlayerSoundFx() {
        float pitch = MathUtils.random(0.8f, 1.1f);
        Audio.getInstance().playSound("sounds/electric-zap.ogg", 0.5f, pitch);
    }

    private void doSound(float distanceToPlayer) {
        float pan = Audio.getInstance().playerDistancePan(this.world, new Vector2(this.spawnPosX, this.spawnPosY));
        System.out.println("pan: " + pan);
        if (distanceToPlayer < 700.0f) {
            float pitch = MathUtils.random(0.9f, 1.1f);
            Audio.getInstance().playSound("sounds/thunder-near1.ogg", 1.0f, pitch, pan);
            this.world.gameScreen.cameraShake.shake(10.0f, 50.0f, 0.25f);
        } else {
            float pitch = MathUtils.random(0.7f, 1.1f);
            float v = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.spawnPosX, this.spawnPosY), 2000.0f, 1.0f);
            Audio.getInstance().playSound("sounds/thunder-distant1.ogg", v, pitch, pan);
        }
    }

    @Override
    public void reset() {
    }
}

