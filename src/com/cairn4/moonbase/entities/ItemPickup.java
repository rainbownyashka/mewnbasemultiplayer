/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.Tank;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.Localization;

public class ItemPickup
extends Entity {
    public Item item;
    boolean doneSpawning = false;
    float linearVelocity = 5.0f;
    float acceleration = 25.0f;
    Vector2 velocity;
    private static final float PICKUP_DISTANCE = 40.0f;
    private static final float VACUUM_DISTANCE = 230.0f;
    private float riskTimer = 0.0f;
    private float riskTime = 1.0f;
    private static final float riskOfLoss = 0.05f;
    private static final float baseRotation = -18.0f;
    private float baseXPos = 0.0f;
    private float riskAnimOffset = MathUtils.random(0, 3);
    private Vector2 targetPos;
    private Vector2 currentPos;

    public ItemPickup(World world, Chunk chunk, float xPos, float yPos, Item item) {
        super(world, xPos, yPos);
        this.item = item;
        this.velocity = new Vector2(0.0f, 0.0f);
        this.linearVelocity = 0.0f;
        this.createDrawable(item.getSprite());
    }

    public ItemPickup(World world, float spawnX, float spawnY, Item item) {
        super(world, spawnX, spawnY);
        this.item = item;
        this.doneSpawning = false;
        this.velocity = new Vector2(0.0f, 0.0f);
        this.linearVelocity = 0.0f;
        this.createDrawable(item.getSprite());
    }

    private void createDrawable(String sprite) {
        this.group = new Group();
        this.group.setPosition(this.spawnPosX, this.spawnPosY);
        this.world.gameScreen.mainGroup.addActor(this.group);
        try {
            this.image = new Image(this.world.gameScreen.skin.getDrawable(sprite));
        }
        catch (GdxRuntimeException e) {
            Gdx.app.error("MewnBase", "ItemPickup sprite is missing or we're trying to look for an item that isn't defined in items.json");
            this.image = new Image(this.world.gameScreen.skin.getDrawable("missing"));
        }
        this.image.setSize(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f);
        this.image.setOrigin(1);
        this.image.setPosition(0.0f, 0.0f, 1);
        this.group.addActor(this.image);
        this.animateSpawn();
    }

    private void animateSpawn() {
        float distance = MathUtils.random(Tile.TILE_SIZE * 0.3f, Tile.TILE_SIZE * 0.6f);
        float randomAngle = MathUtils.random(0, 359);
        Vector2 moveOffset = new Vector2(distance, 0.0f);
        moveOffset.setAngle(randomAngle);
        this.group.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.5f, 0.5f), (Action)Actions.parallel((Action)Actions.moveBy(moveOffset.x, 0.0f, 0.36f), (Action)Actions.moveBy(0.0f, moveOffset.y, 0.36f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.36f), (Action)Actions.fadeIn(0.36f)), (Action)Actions.delay(0.1f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                ItemPickup.this.doneSpawning = true;
            }
        })));
        this.image.addAction(Actions.sequence((Action)Actions.moveTo(0.0f - this.image.getOriginX(), Tile.TILE_SIZE / 2.0f - this.image.getOriginY() + 20.0f, 0.18f, Interpolation.sineOut), (Action)Actions.moveTo(0.0f - this.image.getOriginX(), 0.0f - this.image.getOriginY(), 0.18f, Interpolation.sineIn)));
    }

    @Override
    public void update(float delta) {
        if (this.doneSpawning && this.world.player != null) {
            boolean movingTowardsPlayer = false;
            if (!this.world.player.isDrivingVehicle() && !this.world.player.isFlyingRocket()) {
                if (this.world.player.playerInventory.hasSlotAvailable(this.item.id)) {
                    float playerDist = this.distanceToTarget(this.world.player.getXPos(), this.world.player.getYPos());
                    if (playerDist < 40.0f) {
                        this.collect();
                    } else if (playerDist < 230.0f) {
                        this.moveTowardsTarget(this.world.player.getXPos(), this.world.player.getYPos(), delta);
                    }
                    movingTowardsPlayer = true;
                }
            } else {
                Vehicle v = this.world.player.getVehicle();
                if (v != null && v instanceof Tank) {
                    Tank t = (Tank)v;
                    if (t.trunk.getSpaceAvailableFor(this.item.id) > 0) {
                        float tankDist = this.distanceToTarget(t.getXPos(), t.getYPos());
                        if (tankDist < 40.0f) {
                            this.vehicleCollect(t);
                        } else if (tankDist < 230.0f) {
                            this.moveTowardsTarget(v.getXPos(), v.getYPos(), delta);
                        }
                        movingTowardsPlayer = true;
                    }
                }
            }
            if (movingTowardsPlayer) {
                this.blowAwayUpdate(delta);
            }
        }
    }

    private void blowAwayUpdate(float delta) {
        if (this.world.weatherManager.getCurrentData().riskToItemPiles) {
            this.riskAnimation(delta);
            this.riskTimer += delta;
            if (this.riskTimer > this.riskTime) {
                this.riskTimer = 0.0f;
                float r = MathUtils.random();
                if (r < 0.05f) {
                    this.blowAwayParticleFX();
                    this.image.setVisible(false);
                    this.blowAwayAnim();
                    this.readyToRemove = true;
                }
            }
        }
    }

    private void blowAwayParticleFX() {
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/itemBlowAway.p", ParticleEffect.class));
        ParticleActor dirtFx = new ParticleActor(p, true);
        dirtFx.setPosition(this.getXPos(), this.getYPos());
        this.world.gameScreen.topGroup.addActor(dirtFx);
        dirtFx.pfx.start();
    }

    private void riskAnimation(float delta) {
        float v1 = MathUtils.sin((this.riskTimer + -1.52f + this.riskAnimOffset) * 3.0f);
        float v2 = MathUtils.sin(this.riskTimer * 24.0f) * (2.0f * v1);
        this.image.setRotation(-18.0f + v2);
        this.image.setX(-this.image.getOriginX() + v1);
    }

    private void blowAwayAnim() {
        Image i = new Image(this.image.getDrawable());
        i.setSize(this.image.getWidth(), this.image.getHeight());
        i.setOrigin(this.image.getOriginX(), this.image.getOriginY());
        i.setPosition(this.getXPos(), this.getYPos(), 1);
        i.setRotation(this.image.getRotation());
        this.world.gameScreen.topGroup.addActor(i);
        i.addAction(Actions.sequence((Action)Actions.parallel((Action)Actions.moveBy(100.0f, 20.0f, 0.5f, Interpolation.sineIn), (Action)Actions.rotateBy(-40.0f, 0.5f, Interpolation.sineIn), (Action)Actions.scaleTo(0.25f, 0.25f, 0.5f, Interpolation.sineIn)), (Action)Actions.removeActor()));
    }

    private void collect() {
        this.readyToRemove = true;
        // notify network about pickup so other clients remove the item pickup
        try {
            if (this.world != null && !this.world.suppressNetworkTileEvents && this.world.gameScreen != null) {
                int wx = (int)this.getXPos();
                int wy = (int)this.getYPos();
                String payload = "ITEM_PICKUP:" + wx + ":" + wy + ":" + this.item.id;
                com.cairn4.moonbase.NetworkHelper.sendPayload(this.world.gameScreen, payload);
            }
        } catch (Exception e) {
            Gdx.app.error("ItemPickup", "Failed to broadcast item pickup", e);
        }
        this.world.player.collect(this.item);
        this.group.remove();
        float volume = MathUtils.random(0.45f, 0.55f);
        float pitch = MathUtils.random(0.85f, 1.1f);
        Audio.getInstance().playSound("sounds/pop.mp3", volume, pitch);
    }

    private void vehicleCollect(Vehicle v) {
        this.readyToRemove = true;
        // notify network about pickup so other clients remove the item pickup
        try {
            if (this.world != null && !this.world.suppressNetworkTileEvents && this.world.gameScreen != null) {
                int wx = (int)this.getXPos();
                int wy = (int)this.getYPos();
                String payload = "ITEM_PICKUP:" + wx + ":" + wy + ":" + this.item.id;
                com.cairn4.moonbase.NetworkHelper.sendPayload(this.world.gameScreen, payload);
            }
        } catch (Exception e) {
            Gdx.app.error("ItemPickup", "Failed to broadcast item pickup", e);
        }
        if (v.trunk != null) {
            v.trunk.addSingle(this.item);
            if (this.world.gameScreen.hud != null) {
                this.world.gameScreen.hud.hudNotifications.newPickupMessage(this.item, 1, Localization.get("item_" + v.vd.id) + ": ");
            }
        }
        this.group.remove();
        float volume = MathUtils.random(0.45f, 0.55f);
        float pitch = MathUtils.random(0.85f, 1.1f);
        Audio.getInstance().playSound("sounds/pop.mp3", volume, pitch);
    }

    private float distanceToTarget(float x, float y) {
        if (this.targetPos == null) {
            this.targetPos = new Vector2(x, y);
        } else {
            this.targetPos.set(x, y);
        }
        return this.targetPos.dst(this.getXPos(), this.getYPos());
    }

    public void moveTowardsPlayer(float delta) {
        this.moveTowardsTarget(this.world.player.getXPos(), this.world.player.getYPos(), delta);
    }

    private void moveTowardsTarget(float x, float y, float delta) {
        if (this.targetPos == null) {
            this.targetPos = new Vector2(x, y);
        } else {
            this.targetPos.set(x, y);
        }
        if (this.currentPos == null) {
            this.currentPos = new Vector2(this.group.getX(), this.group.getY());
        } else {
            this.currentPos.set(this.group.getX(), this.group.getY());
        }
        this.linearVelocity += this.acceleration * delta;
        this.velocity.set(this.linearVelocity, 0.0f);
        float angle = this.targetPos.sub(this.currentPos.cpy()).angle();
        this.velocity.setAngle(angle);
        this.currentPos.add(this.velocity);
        this.group.setPosition(this.currentPos.x, this.currentPos.y);
    }
}

