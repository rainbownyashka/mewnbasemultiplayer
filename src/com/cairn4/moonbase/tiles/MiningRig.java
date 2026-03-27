/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.SpineActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.ProxyTile;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.CooldownCallback;
import com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior;
import com.cairn4.moonbase.tiles.behaviors.ResourceCollectorBehavior;
import com.cairn4.moonbase.ui.MiningRigUI;
import com.cairn4.moonbase.worlddata.ItemDropperFactory;
import com.cairn4.moonbase.worlddata.MiningResourceData;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Slot;
import java.util.ArrayList;

public class MiningRig
extends BaseModule
implements Telegraph {
    public ItemStorageBehavior itemStorageBehavior;
    public ResourceCollectorBehavior resourceCollectorBehavior;
    private boolean proxyTilesPlaced = false;
    private boolean fetchedGroundBiomeInfo = false;
    private Image resourcePileImage;
    ParticleActor sparkParticles;
    protected SpineActor spineActor;
    protected Slot itemSlot;
    protected Bone fxBone;
    protected boolean buildParticlesOn = false;
    private static int COLLECT_TRACK = 2;
    private Sound sound;
    private long soundId;
    private float volume;
    private float pan;
    private static final float MAX_VOLUME = 0.4f;
    private Label debugLabel;
    private GroundTile.Biomes biome;
    private static final float BASE_RATE = 5.0f;

    public GroundTile.Biomes getBiome() {
        return this.biome;
    }

    public MiningRig(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.group.setOrigin(Tile.TILE_SIZE, Tile.TILE_SIZE / 2.0f);
        this.group.removeActor(this.statusGroup);
        this.statusGroup.setPosition(this.group.getX() + Tile.TILE_SIZE / 2.0f, this.group.getY());
        this.statusGroup.toFront();
        world.gameScreen.mainGroup.addActor(this.statusGroup);
        this.powerDrawRate = 24.0f;
        this.biome = chunk.getGroundTile(x, y).getBiome();
        if (this.biome == null) {
            MoonBase.error("BIOME IS NULL");
            this.biome = GroundTile.Biomes.ground;
        }
        MessageManager.getInstance().addListener(this, 32);
        this.itemStorageBehavior = new ItemStorageBehavior();
        this.itemStorageBehavior.baseModule = this;
        this.itemStorageBehavior.maxItems = 4;
        this.itemStorageBehavior.setCanDeposit(false);
        this.behaviorList.add(this.itemStorageBehavior);
        this.resourceCollectorBehavior = new ResourceCollectorBehavior();
        this.resourceCollectorBehavior.itemStorageBehavior = this.itemStorageBehavior;
        this.resourceCollectorBehavior.baseModule = this;
        this.resourceCollectorBehavior.timePerCollect = 10.0f;
        this.resourceCollectorBehavior.requirePower = true;
        this.resourceCollectorBehavior.biome = this.biome;
        this.resourceCollectorBehavior.collectCallback = new CooldownCallback(){

            @Override
            public void callback() {
                MiningRig.this.harvestResource();
            }
        };
        this.behaviorList.add(this.resourceCollectorBehavior);
        chunk.tilesThatNeedProxies.add(this);
        this.addParticles();
        this.buildParticlesOn = false;
        this.toggleCraftingFx(false);
        this.setupPhysics("miningrig");
        this.updateBases();
        this.startBehaviors();
        this.sound = Gdx.audio.newSound(Gdx.files.internal("sounds/miningrig.ogg"));
        this.soundId = Audio.getInstance().playSoundLoop(this.sound, 0.0f, 0.4f, 0.0f);
    }

    private void calcMiningSpeed() {
        MoonBase.debug("Mining biome " + this.worldX + ", " + this.worldY + " : " + this.biome.toString());
        for (MiningResourceData mrd : ItemDropperFactory.getInstance().miningResources) {
            if (!mrd.biome.equals(this.biome.toString())) continue;
            this.fetchedGroundBiomeInfo = true;
            this.resourceCollectorBehavior.timePerCollect = 72.0f * mrd.collectTimeMultiplier;
        }
        if (!this.fetchedGroundBiomeInfo) {
            this.resourceCollectorBehavior.timePerCollect = 72.0f;
        }
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("miningrig", this.world.gameScreen.mainGroup);
        this.group.setUserObject(Float.valueOf(this.getYCenter() - 15.0f));
        this.image.setVisible(false);
        this.setupSpineActor();
    }

    public void setupSpineActor() {
        this.spineActor = new SpineActor("miningrig", true, 0.85f, this.world.gameScreen.skeletonRenderer);
        this.spineActor.setPosition(TILE_SIZE / 2.0f + 15.0f, TILE_SIZE / 2.0f);
        this.group.addActor(this.spineActor);
        this.spineActor.setDebug(true);
        this.spineActor.stateData.setDefaultMix(0.0f);
        this.spineActor.addAnimation(0, "working", true, 0.0f);
        this.spineActor.stateData.setMix("idle", "working", 0.1f);
        this.spineActor.stateData.setMix("working", "idle", 0.1f);
        this.spineActor.stateData.setMix("powerOff", "powerOn", 0.25f);
        this.spineActor.stateData.setMix("powerOn", "powerOff", 0.25f);
        this.itemSlot = this.spineActor.skeleton.findSlot("resource");
        this.spineActor.state.addListener(new AnimationState.AnimationStateAdapter(){

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                if (event.getData().getName().equals("collectFinished")) {
                    MiningRig.this.updatePile();
                    MiningRig.this.checkFull();
                }
            }
        });
    }

    private void checkFull() {
        boolean full = true;
        if (this.itemStorageBehavior.isFull()) {
            for (ItemStack is : this.itemStorageBehavior.getItemList()) {
                if (is.getAmount() >= is.getMaxCarry()) continue;
                full = false;
                break;
            }
        } else {
            full = false;
        }
        MoonBase.debug("Mining rig full: " + full);
        if (full) {
            this.resourceCollectorBehavior.setStorageAvailable(false);
            this.toggleCraftingFx(false);
        } else {
            this.resourceCollectorBehavior.setStorageAvailable(true);
            this.toggleCraftingFx(true);
        }
    }

    private void harvestResource() {
        this.playHarvestSound();
        String itemId = this.resourceCollectorBehavior.getItemId();
        if (this.itemStorageBehavior != null && itemId != null) {
            if (this.itemStorageBehavior.getSpaceAvailableFor(itemId) > 0) {
                Item i = ItemFactory.getInstance().createItem(itemId);
                this.itemStorageBehavior.addSingle(i);
                MoonBase.debug("MiningRig +1 " + itemId);
                this.collectResourceAnim(itemId);
            } else {
                this.resourceCollectorBehavior.skipItem();
                MoonBase.debug("MiningRig: no room for " + itemId + ", skipping...");
            }
        } else {
            MoonBase.error("Mining rig storage or itemId is null");
        }
    }

    private void collectResourceAnim(String itemId) {
        this.spineActor.setSlotAttachment("resource", ItemFactory.getItemSprite(itemId));
        this.spineActor.setAnimation(COLLECT_TRACK, "collect", false);
    }

    public void addParticles() {
        this.buildParticlesOn = false;
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/miningrig-sparks.p", ParticleEffect.class));
        this.sparkParticles = new ParticleActor(p, false);
        this.sparkParticles.setPosition(this.getXCenter() + 13.0f, this.getYCenter() - 20.0f);
        this.sparkParticles.pfx.allowCompletion();
        this.world.gameScreen.mainGroup.addActor(this.sparkParticles);
    }

    @Override
    public void startBehaviors() {
        this.itemStorageBehavior.start();
        this.updatePile();
        this.checkFull();
        this.calcMiningSpeed();
    }

    @Override
    public void setSpinePowerState(boolean on) {
        if (this.spineActor != null) {
            if (on) {
                this.spineActor.setAnimation(1, "powerOn", false);
            } else {
                this.spineActor.setAnimation(1, "powerOff", false);
            }
        }
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "miningrig";
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.spineActor.update(delta);
        if (!this.fetchedGroundBiomeInfo) {
            this.calcMiningSpeed();
        }
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        if (this.sound != null) {
            this.updateVolume();
        }
        this.toggleCraftingFx(this.resourceCollectorBehavior.isRunning());
    }

    private void updateVolume() {
        if (this.hasPower && this.resourceCollectorBehavior.isRunning()) {
            this.volume = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), Tile.TILE_SIZE * 4.0f, 0.4f);
            this.pan = Audio.getInstance().playerDistancePan(this.world, new Vector2(this.getXCenter(), this.getYCenter()));
        } else {
            this.volume = 0.0f;
        }
        if (this.volume <= 0.0f) {
            this.sound.pause(this.soundId);
        } else {
            this.sound.resume(this.soundId);
            Audio.getInstance().updateLoopingSoundVolume(this.sound, this.soundId, this.volume, this.pan);
        }
    }

    @Override
    public void interact(Player player) {
        if (this.hasDisaster()) {
            this.fixDisaster();
        } else {
            this.openStorage(player);
            super.interact(player);
        }
    }

    @Override
    public float getPowerDrawRate() {
        if (this.resourceCollectorBehavior != null && this.resourceCollectorBehavior.getStorageAvailable()) {
            return this.powerDrawRate;
        }
        return 2.0f;
    }

    private void toggleCraftingFx(boolean on) {
        if (on && !this.buildParticlesOn) {
            this.sparkParticles.pfx.reset();
            this.sparkParticles.pfx.start();
            this.buildParticlesOn = true;
            this.spineActor.setAnimation(0, "working", true);
            MoonBase.debug("Mining rig running...");
        } else if (!on && this.buildParticlesOn) {
            this.sparkParticles.pfx.allowCompletion();
            this.buildParticlesOn = false;
            this.spineActor.setAnimation(0, "idle", false);
            MoonBase.debug("Mining rig stopping.");
        }
    }

    private void openStorage(Player player) {
        MiningRigUI storageUI = new MiningRigUI(this.world.gameScreen, this, this.itemStorageBehavior, player.playerInventory);
        this.world.gameScreen.showMenu(storageUI);
    }

    private void updatePile() {
        MoonBase.debug("MiningRig: updatePile");
        for (int i = 0; i < this.itemStorageBehavior.maxItems; ++i) {
            if (i < this.itemStorageBehavior.getItemList().size()) {
                String itemId = this.itemStorageBehavior.getItemList().get(i).getId();
                this.spineActor.setSlotAttachment("icon" + (i + 1), itemId);
                continue;
            }
            this.spineActor.setSlotAttachment("icon" + (i + 1), null);
        }
    }

    @Override
    public void addProxies() {
        ArrayList<GridPoint2> pCoords = new ArrayList<GridPoint2>();
        pCoords.add(new GridPoint2(1, 0));
        if (!this.proxyTilesPlaced) {
            this.proxyTilesPlaced = true;
            for (GridPoint2 gp : pCoords) {
                GridPoint2 cGp2 = World.gridPointPool.obtain();
                World.convertWorldPosToChunkCoord(cGp2, this.worldX + gp.x, this.worldY + gp.y);
                Chunk proxyChunk = this.world.getChunk(cGp2.x, cGp2.y);
                GridPoint2 localPos = World.gridPointPool.obtain();
                World.convertWorldToLocal(localPos, this.worldX + gp.x, this.worldY + gp.y);
                MoonBase.debug("Adding proxy from (" + this.x + ", " + this.y + ")  to -> " + localPos);
                ProxyTile proxyTile = new ProxyTile(this.world, proxyChunk, localPos.x, localPos.y, this);
            }
        }
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        if (msg.message == 32 && msg.extraInfo == this) {
            this.checkFull();
            this.updatePile();
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        MessageManager.getInstance().removeListener((Telegraph)this, 32);
        if (this.sound != null) {
            this.sound.stop(this.soundId);
        }
        this.sparkParticles.remove();
        this.statusGroup.remove();
        super.destroy();
    }

    @Override
    public void dropPickup() {
        for (ItemStack stack : this.itemStorageBehavior.getItemList()) {
            for (int i = 0; i < stack.getAmount(); ++i) {
                new ItemPickup(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos(), stack.item);
            }
        }
        super.dropPickup();
    }

    private void playHarvestSound() {
        float distVol = Audio.getInstance().playerDistanceMultiplier(this.world, this.getXCenter(), this.getYCenter(), 1000.0f, 0.4f);
        float pan = Audio.getInstance().playerDistancePan(this.world, this.getXCenter(), this.getYCenter());
        float pitch = 1.0f;
        pitch = MathUtils.random(0.7f, 0.9f);
        Audio.getInstance().playSound("sounds/miningrig-collected.ogg", distVol, pitch, pan);
    }

    public float getMiningRate() {
        return 360.0f / this.resourceCollectorBehavior.timePerCollect;
    }
}

