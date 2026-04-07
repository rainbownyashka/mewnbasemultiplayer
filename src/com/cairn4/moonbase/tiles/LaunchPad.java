/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.audio.Sound;
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
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.RecipieRequirement;
import com.cairn4.moonbase.RocketDrawable;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.entities.Rocket;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.ProxyTile;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.LaunchPadBehavior;
import com.cairn4.moonbase.tiles.behaviors.RocketCrafter;
import com.cairn4.moonbase.tiles.behaviors.TeleportBehavior;
import com.cairn4.moonbase.ui.LaunchPadMenu;
import java.util.ArrayList;

public class LaunchPad
extends BaseModule {
    public LaunchPadBehavior launchPadBehavior;
    public TeleportBehavior tb;
    public Rocket rocket;
    public RocketDrawable rocketDrawable;
    public boolean hasRocket;
    public boolean launching;
    public LaunchPad targetDestination;
    public boolean leavePlanet = false;
    public static final float fuelToLaunch = 50.0f;
    Boolean proxyTilesPlaced = false;
    Group topGroup;
    private Image topSection;
    private Image leftTower;
    private Image rightTower;
    private Group buildGroup;
    private Image rocketBuildImage;
    private ParticleActor buildingParticles1;
    private ParticleActor buildingParticles2;
    private PointLight spotlight1;
    private PointLight spotlight2;
    private PointLight spotlight3;
    private PointLight spotlight4;
    public static float towerPos = 190.0f * Tile.SCALE;
    private ParticleActor fuelingParticles;
    public RocketCrafter itemCrafter;
    private Image buildFinishedIcon;
    boolean fuelToRocket;
    boolean fuelToStorage;
    private ParticleEffect fuelingEffect;
    private String name;
    private static final float towerMoveBy = 20.0f;
    private String[] nonDirectionNames = new String[]{"Alpha", "Beta", "Delta", "Echo", "Gamma"};
    private Sound rocketBuildSoundLoop;
    private long rocketBuildSoundLoopId;
    private float build_volume = 1.0f;
    private float build_pan = 1.0f;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LaunchPad(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.tb = new TeleportBehavior();
        this.tb.baseModule = this;
        this.behaviorList.add(this.tb);
        this.powerDrawRate = 20.0f;
        this.powerGenRate = 0.0f;
        this.setupPhysics("launchpad", 1.5f);
        this.randomName();
        this.powerDrawRate = 5.0f;
        chunk.tilesThatNeedProxies.add(this);
        this.itemCrafter = new RocketCrafter();
        this.itemCrafter.setSpawnTile(this.chunk, this.x, this.y);
        this.itemCrafter.baseModule = this;
        this.itemCrafter.setupBuildables(this.builderId);
        this.itemCrafter.buildQueueSizeLimit = 1;
        this.itemCrafter.requirePowerToCraft = true;
        this.behaviorList.add(this.itemCrafter);
        this.rocketDrawable = new RocketDrawable(this.world, this, this.worldX, this.worldY);
        this.rocketDrawable.hide();
        this.buildFinishedIcon = new Image(world.gameScreen.skin.getDrawable("crafting-notification"));
        this.buildFinishedIcon.setOrigin(4);
        this.buildFinishedIcon.setScale(0.3f);
        this.buildFinishedIcon.setPosition(this.getWorldXPos() + TILE_SIZE / 2.0f, this.getWorldYPos() + TILE_SIZE * 0.7f, 4);
        world.gameScreen.worldUiGroup.addActor(this.buildFinishedIcon);
        this.itemCrafter.setFinishedIcon(this.buildFinishedIcon);
        this.group.removeActor(this.statusGroup);
        this.statusGroup.setPosition(this.group.getX(), this.group.getY());
        this.statusGroup.toFront();
        world.gameScreen.mainGroup.addActor(this.statusGroup);
        this.launchPadBehavior = new LaunchPadBehavior();
        this.launchPadBehavior.launchPad = this;
        this.behaviorList.add(this.launchPadBehavior);
    }

    @Override
    public String getMapName() {
        return "Pad " + this.getName();
    }

    @Override
    public void startBehaviors() {
        MoonBase.log("Loading launchpad behaviors");
        this.launchPadBehavior.start();
    }

    @Override
    public void animateSpawn() {
        this.group.addAction(LaunchPad.getSpawnAnimation());
        this.topGroup.addAction(LaunchPad.getSpawnAnimation());
        ParticleEffect dustFx = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/build-dust.p", ParticleEffect.class));
        ParticleActor pa = new ParticleActor(dustFx, true);
        pa.setPosition(this.getXCenter(), this.getYCenter() - 5.0f);
        pa.pfx.start();
        this.world.gameScreen.mainGroup.addActor(pa);
    }

    public static Action getSpawnAnimation() {
        float t = 0.12f;
        return Actions.sequence(Actions.alpha(0.0f), Actions.scaleTo(0.8f, 0.8f), Actions.parallel((Action)Actions.scaleTo(1.05f, 1.05f, t), (Action)Actions.alpha(1.0f, t), (Action)Actions.moveBy(0.0f, 10.0f, t, Interpolation.sineOut)), Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, t), (Action)Actions.moveBy(0.0f, -10.0f, t, Interpolation.sineIn)), Actions.moveBy(0.0f, 2.0f, t * 0.4f), Actions.moveBy(0.0f, -2.0f, t * 0.2f));
    }

    private void randomName() {
        String n = "";
        int num = MathUtils.random(1, 15);
        GridPoint2 relativeToCenter = new GridPoint2(this.worldX - 500, this.worldY - 500);
        int absx = Math.abs(relativeToCenter.x);
        int absy = Math.abs(relativeToCenter.y);
        String direction = "";
        if ((float)absx > (float)absy * 1.5f) {
            direction = relativeToCenter.x > 0 ? "East" : "West";
        } else if ((float)absy > (float)absx * 1.5f) {
            direction = relativeToCenter.y > 0 ? "North" : "South";
        } else {
            int r = MathUtils.random(0, this.nonDirectionNames.length - 1);
            direction = this.nonDirectionNames[r];
        }
        this.name = n = n + direction + "-0" + num;
    }

    @Override
    public void setupLight() {
        float x1 = this.getXCenter() - Tile.TILE_SIZE;
        float y1 = this.getYCenter() - Tile.TILE_SIZE;
        float x2 = this.getXCenter() + Tile.TILE_SIZE;
        float y2 = this.getYCenter() + Tile.TILE_SIZE + 40.0f;
        this.light = new PointLight(this.world.rayHandler, 16, this.getLightOnColor(), 1.171875f, this.getXCenter() / 256.0f, (this.getYCenter() + 50.0f) / 256.0f);
        this.light.setIgnoreAttachedBody(true);
        this.light.setXray(true);
        this.light.setStaticLight(true);
        this.light.setContactFilter(this.categoryBits, this.categoryBits, this.maskBits);
        this.lightOnColor.set(0.8f, 0.8f, 1.0f, 1.0f);
        this.spotlight1 = new PointLight(this.world.rayHandler, 16, this.getLightOnColor(), 0.7421875f, x1 / 256.0f, y1 / 256.0f);
        this.spotlight1.setIgnoreAttachedBody(true);
        this.spotlight1.setXray(true);
        this.spotlight1.setStaticLight(true);
        this.spotlight1.setContactFilter(this.categoryBits, this.categoryBits, this.maskBits);
        this.spotlight2 = new PointLight(this.world.rayHandler, 16, this.getLightOnColor(), 0.7421875f, x2 / 256.0f, y1 / 256.0f);
        this.spotlight2.setIgnoreAttachedBody(true);
        this.spotlight2.setXray(true);
        this.spotlight2.setStaticLight(true);
        this.spotlight2.setContactFilter(this.categoryBits, this.categoryBits, this.maskBits);
        this.spotlight3 = new PointLight(this.world.rayHandler, 16, this.getLightOnColor(), 0.390625f, (x1 - 40.0f) / 256.0f, y2 / 256.0f);
        this.spotlight3.setIgnoreAttachedBody(true);
        this.spotlight3.setXray(true);
        this.spotlight3.setStaticLight(true);
        this.spotlight3.setContactFilter(this.categoryBits, this.categoryBits, this.maskBits);
        this.spotlight4 = new PointLight(this.world.rayHandler, 16, this.getLightOnColor(), 0.390625f, (x2 + 40.0f) / 256.0f, y2 / 256.0f);
        this.spotlight4.setIgnoreAttachedBody(true);
        this.spotlight4.setXray(true);
        this.spotlight4.setStaticLight(true);
        this.spotlight4.setContactFilter(this.categoryBits, this.categoryBits, this.maskBits);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "launchpad";
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("launchpad-base", this.world.gameScreen.floorGroup);
        this.image.setSize(812.0f * Tile.SCALE, 812.0f * Tile.SCALE);
        this.image.setOrigin(1);
        this.image.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f + 8.0f, 1);
        this.topGroup = new Group();
        this.topGroup.setPosition(this.group.getX() + Tile.TILE_SIZE / 2.0f, this.group.getY() + Tile.TILE_SIZE / 2.0f + 8.0f);
        this.world.gameScreen.mainGroup.addActor(this.topGroup);
        this.buildGroup = new Group();
        this.buildGroup.setVisible(false);
        this.topGroup.addActor(this.buildGroup);
        this.rocketBuildImage = new Image(this.world.gameScreen.skin.getDrawable("rocket-progress1"));
        this.rocketBuildImage.setSize(186.0f, 266.25f);
        this.rocketBuildImage.setPosition(0.0f, -25.0f, 4);
        this.buildGroup.addActor(this.rocketBuildImage);
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/construction-sparks.p", ParticleEffect.class));
        this.buildingParticles1 = new ParticleActor(p, false);
        this.buildingParticles1.setPosition(70.0f, 110.0f);
        this.buildingParticles1.pfx.start();
        this.buildGroup.addActor(this.buildingParticles1);
        ParticleEffect p2 = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/construction-sparks.p", ParticleEffect.class));
        this.buildingParticles2 = new ParticleActor(p2, false);
        this.buildingParticles2.setPosition(-70.0f, 60.0f);
        this.buildingParticles2.pfx.start();
        this.buildGroup.addActor(this.buildingParticles2);
        this.leftTower = new Image(this.world.gameScreen.skin.getDrawable("launchpad-tower"));
        this.leftTower.setSize(194.0f * Tile.SCALE, 430.0f * Tile.SCALE);
        this.leftTower.setOrigin(4);
        this.leftTower.setPosition(-towerPos, 0.0f, 4);
        this.topGroup.addActor(this.leftTower);
        this.rightTower = new Image(this.world.gameScreen.skin.getDrawable("launchpad-tower"));
        this.rightTower.setSize(194.0f * Tile.SCALE, 430.0f * Tile.SCALE);
        this.rightTower.setOrigin(4);
        this.rightTower.setScale(-1.0f, 1.0f);
        this.rightTower.setPosition(towerPos, 0.0f, 4);
        this.topGroup.addActor(this.rightTower);
        this.topSection = new Image(this.world.gameScreen.skin.getDrawable("launchpad-front"));
        this.topSection.setSize(800.0f * Tile.SCALE, 800.0f * Tile.SCALE);
        this.topSection.setOrigin(1);
        this.topSection.setPosition(0.0f, 0.0f, 1);
        this.topGroup.addActor(this.topSection);
        this.fuelingEffect = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/rocket-fueled.p", ParticleEffect.class));
        this.fuelingParticles = new ParticleActor(this.fuelingEffect, false);
        this.fuelingParticles.setPosition(0.0f, 15.0f);
        this.fuelingParticles.pfx.allowCompletion();
        this.topGroup.addActor(this.fuelingParticles);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.rocket != null && this.rocketDrawable != null && this.launching) {
            this.rocketDrawable.update(delta);
        }
        this.updateBuildRocketImage();
        this.update_construction_sfx();
        if (this.hasPower) {
            for (Behavior b : this.behaviorList) {
                b.update(delta);
            }
        }
    }

    public void construction_start_sfx() {
        this.rocketBuildSoundLoop = Gdx.audio.newSound(Gdx.files.internal("sounds/launchpad-build-loop.ogg"));
        this.rocketBuildSoundLoopId = Audio.getInstance().playSoundLoop(this.rocketBuildSoundLoop, 0.0f, 0.8f, 0.0f);
    }

    public void update_construction_sfx() {
        if (this.itemCrafter.isBuilding()) {
            this.build_volume = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 900.0f, 0.6f);
            this.build_pan = Audio.getInstance().playerDistancePan(this.world, new Vector2(this.getXCenter(), this.getYCenter()));
            if (!this.hasPower) {
                this.build_volume = 0.0f;
            }
            if (this.rocketBuildSoundLoop == null) {
                this.construction_start_sfx();
            }
            if (this.build_volume <= 0.0f) {
                this.rocketBuildSoundLoop.pause(this.rocketBuildSoundLoopId);
            } else {
                this.rocketBuildSoundLoop.resume(this.rocketBuildSoundLoopId);
                Audio.getInstance().updateLoopingSoundVolume(this.rocketBuildSoundLoop, this.rocketBuildSoundLoopId, this.build_volume, this.build_pan);
            }
        }
    }

    public void construction_finished_sfx() {
        this.rocketBuildSoundLoop.stop(this.rocketBuildSoundLoopId);
        float world_volume = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 2000.0f, 1.0f);
        Audio.getInstance().playSound("sounds/launchpad-build-complete.ogg", world_volume, 1.0f);
    }

    private void updateBuildRocketImage() {
        if (this.itemCrafter.isBuilding()) {
            this.buildGroup.setVisible(true);
            float progress = this.itemCrafter.getBuildProgress();
            if (progress < 0.4f) {
                this.rocketBuildImage.setDrawable(this.world.gameScreen.skin.getDrawable("rocket-progress1"));
            } else {
                this.rocketBuildImage.setDrawable(this.world.gameScreen.skin.getDrawable("rocket-progress2"));
            }
        } else {
            this.buildGroup.setVisible(false);
        }
    }

    @Override
    public void interact(Player player) {
        super.interact(player);
        LaunchPadMenu lpm = new LaunchPadMenu(this.world.gameScreen, this, player);
        this.world.gameScreen.showMenu(lpm);
    }

    public void startLaunch() {
        this.fuelingParticles.pfx.allowCompletion();
        if (!this.leavePlanet && this.targetDestination != null) {
            this.targetDestination.setTowerOpen();
        }
        if (this.leavePlanet) {
            if (MoonBase.isMultiplayer) {
                try {
                    this.notifyMissionComplete(this.world != null ? this.world.player : null);
                } catch (Exception ignored) {}
                this.launching = false;
                try { this.world.getPlayer().setFlyingRocket(false); } catch (Exception ignored) {}
                return;
            }
            this.world.gameScreen.gameLoader.saveGame(this.world, false);
        }
    }

    public void notifyMissionComplete(Player player) {
        try {
            String nick = "";
            try {
                if (player != null && player.name != null && player.name.length() > 0) nick = player.name;
            } catch (Exception ignored) {}
            if (nick == null || nick.trim().length() == 0) {
                try { nick = com.cairn4.moonbase.MoonBase.multiplayerNick; } catch (Exception ignored) {}
            }
            if (nick == null || nick.trim().length() == 0) nick = "Player";
            String msg = "Миссия завершена! (" + nick + ")";
            String encNick = java.net.URLEncoder.encode("", "UTF-8");
            String encMsg = java.net.URLEncoder.encode(msg, "UTF-8");
            com.cairn4.moonbase.NetworkHelper.sendPayload(this.world.gameScreen, "CHATRAW:" + encNick + ":" + encMsg);
        } catch (Exception ignored) {}
    }

    private void setTowerOpen() {
        this.leftTower.addAction(Actions.parallel((Action)Actions.rotateTo(10.0f, 0.5f), (Action)Actions.moveToAligned(-towerPos - 20.0f, 0.0f, 4, 0.1f)));
        this.rightTower.addAction(Actions.parallel((Action)Actions.rotateTo(-10.0f, 0.5f), (Action)Actions.moveToAligned(towerPos + 20.0f, 0.0f, 4, 0.1f)));
    }

    public void takeOff() {
        if (this.getBaseGroup().getTotalFuelStored() >= 0.0f) {
            this.getBaseGroup().consumeResource(50.0f, BaseResources.fuel, Float.valueOf(this.getBaseGroup().getTotalFuelStored()));
            this.rocketDrawable.takeOff();
        } else {
            this.world.gameScreen.hud.hudNotifications.newMessage("Not enough rocket fuel to launch!");
        }
    }

    private void tower_open_sfx() {
        float world_volume = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 2000.0f, 1.0f);
        Audio.getInstance().playSound("sounds/launchpad-tower2.ogg", world_volume, 0.8f);
    }

    private void tower_open_sfx_end() {
        float world_volume = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 2000.0f, 0.7f);
        Audio.getInstance().playSound("sounds/launchpad-tower2.ogg", world_volume, 0.6f);
    }

    public void openTowerAnim() {
        float world_volume = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 2000.0f, 0.75f);
        Audio.getInstance().playSound("sounds/launchpad-tower1.ogg", world_volume, 0.6f);
        this.leftTower.addAction(Actions.sequence(Actions.rotateTo(0.0f), Actions.moveToAligned(-towerPos, 0.0f, 4), Actions.moveToAligned(-towerPos, 1.0f, 4, 0.06f), Actions.moveToAligned(-towerPos, -1.0f, 4, 0.07f), Actions.moveToAligned(-towerPos, 1.0f, 4, 0.06f), Actions.moveToAligned(-towerPos, -1.0f, 4, 0.1f), Actions.delay(0.1f), Actions.moveToAligned(-towerPos - 20.0f - 2.0f, 0.0f, 4, 1.0f, Interpolation.linear), Actions.run(new Runnable(){

            @Override
            public void run() {
                LaunchPad.this.tower_open_sfx();
            }
        }), Actions.moveToAligned(-towerPos - 20.0f + 1.0f, 0.0f, 4, 0.1f), Actions.moveToAligned(-towerPos - 20.0f, 0.0f, 4, 0.1f), Actions.delay(0.5f), Actions.rotateTo(10.0f, 1.5f, Interpolation.sine), Actions.run(new Runnable(){

            @Override
            public void run() {
                LaunchPad.this.tower_open_sfx_end();
            }
        }), Actions.rotateTo(11.0f, 0.07f), Actions.rotateTo(10.0f, 0.07f)));
        this.rightTower.addAction(Actions.sequence(Actions.rotateTo(0.0f), Actions.moveToAligned(towerPos, 0.0f, 4), Actions.moveToAligned(towerPos, -1.0f, 4, 0.06f), Actions.moveToAligned(towerPos, 1.0f, 4, 0.07f), Actions.moveToAligned(towerPos, -1.0f, 4, 0.06f), Actions.moveToAligned(towerPos, 1.0f, 4, 0.1f), Actions.delay(0.1f), Actions.moveToAligned(towerPos + 20.0f + 2.0f, 0.0f, 4, 1.0f, Interpolation.linear), Actions.moveToAligned(towerPos + 20.0f - 1.0f, 0.0f, 4, 0.1f), Actions.moveToAligned(towerPos + 20.0f, 0.0f, 4, 0.1f), Actions.delay(0.5f), Actions.rotateTo(-10.0f, 1.5f, Interpolation.sine), Actions.rotateTo(-11.0f, 0.07f), Actions.rotateTo(-10.0f, 0.07f), Actions.delay(2.0f), Actions.run(new Runnable(){

            @Override
            public void run() {
                LaunchPad.this.takeOff();
            }
        }), Actions.delay(5.0f), Actions.run(new Runnable(){

            @Override
            public void run() {
                LaunchPad.this.resetTowerAnim();
            }
        })));
    }

    public void resetTowerAnim() {
        MoonBase.log("Reset tower anim");
        this.leftTower.addAction(Actions.parallel((Action)Actions.moveToAligned(-towerPos, 0.0f, 4, 1.0f, Interpolation.sine), (Action)Actions.rotateTo(0.0f, 1.0f, Interpolation.sine)));
        this.rightTower.addAction(Actions.sequence((Action)Actions.parallel((Action)Actions.moveToAligned(towerPos, 0.0f, 4, 1.0f, Interpolation.sine), (Action)Actions.rotateTo(0.0f, 1.0f, Interpolation.sine)), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                MoonBase.log("Launching = false");
                LaunchPad.this.launching = false;
            }
        })));
    }

    public void finishTakeOff() {
        if (this.leavePlanet) {
            try {
                this.setHasRocket(false);
                this.rocket = null;
                this.launching = false;
            } catch (Exception ignored) {}
            if (this.world != null && this.world.gameScreen != null) {
                this.world.gameScreen.requestPlanetTravel();
            }
        } else {
            this.setHasRocket(false);
            this.rocket = null;
            this.resetTowerAnim();
            this.tb.teleport(this.targetDestination.worldX, this.targetDestination.worldY);
            this.world.gameScreen.cameraLag.center();
            this.world.gameScreen.cameraLag.panTo(this.targetDestination.getXCenter(), this.targetDestination.getYCenter() + 40.0f, 0.0f, Interpolation.linear);
            LaunchPad lpTarget = this.targetDestination;
            if (lpTarget != null) {
                lpTarget.addRocket();
                lpTarget.rocketDrawable.land();
            }
        }
    }

    public void setHasRocket(boolean hasRocket) {
        this.hasRocket = hasRocket;
        if (hasRocket) {
            this.rocketDrawable.show();
        } else {
            this.rocketDrawable.hide();
        }
    }

    @Override
    public void addProxies() {
        ArrayList<GridPoint2> pCoords = new ArrayList<GridPoint2>();
        pCoords.add(new GridPoint2(-1, -1));
        pCoords.add(new GridPoint2(-1, 0));
        pCoords.add(new GridPoint2(-1, 1));
        pCoords.add(new GridPoint2(0, -1));
        pCoords.add(new GridPoint2(0, 1));
        pCoords.add(new GridPoint2(1, -1));
        pCoords.add(new GridPoint2(1, 0));
        pCoords.add(new GridPoint2(1, 1));
        if (!this.proxyTilesPlaced.booleanValue()) {
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
    public void setPower(boolean p) {
        if (!this.launching) {
            if (p && !this.hasPower) {
                this.animateColor(this.topSection, this.lightOnColor, 0.25f);
                this.animateColor(this.leftTower, this.lightOnColor, 0.25f);
                this.animateColor(this.rightTower, this.lightOnColor, 0.25f);
                this.spotlight1.setActive(true);
                this.spotlight2.setActive(true);
                this.spotlight3.setActive(true);
                this.spotlight4.setActive(true);
                this.rocketDrawable.setPower(true);
            } else if (!p && this.hasPower) {
                this.animateColor(this.topSection, BaseModule.POWER_OFF_COLOR, 0.25f);
                this.animateColor(this.leftTower, BaseModule.POWER_OFF_COLOR, 0.25f);
                this.animateColor(this.rightTower, BaseModule.POWER_OFF_COLOR, 0.25f);
                this.spotlight1.setActive(false);
                this.spotlight2.setActive(false);
                this.spotlight3.setActive(false);
                this.spotlight4.setActive(false);
                this.rocketDrawable.setPower(false);
            }
            super.setPower(p);
        }
    }

    @Override
    public void destroy() {
        if (this.rocketBuildSoundLoop != null) {
            this.rocketBuildSoundLoop.stop(this.rocketBuildSoundLoopId);
        }
        this.statusGroup.remove();
        this.topGroup.remove();
        if (this.rocket != null) {
            this.rocket.remove();
        }
        if (this.spotlight1 != null) {
            this.spotlight1.remove();
        }
        if (this.spotlight2 != null) {
            this.spotlight2.remove();
        }
        if (this.spotlight3 != null) {
            this.spotlight3.remove();
        }
        if (this.spotlight4 != null) {
            this.spotlight4.remove();
        }
        super.destroy();
    }

    public void addRocket(Rocket r) {
        this.rocket = r;
        this.setHasRocket(true);
    }

    public void addRocket() {
        float spawnX = (float)this.worldX * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f;
        float spawnY = (float)this.worldY * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f;
        Rocket r = new Rocket(this.chunk.world, spawnX, spawnY);
        this.addRocket(r);
    }

    public void fuelToRocket() {
        float fuelStored = this.getBaseGroup().getTotalFuelStored();
        if (this.rocket != null) {
            if (fuelStored > 0.0f) {
                if (!this.rocket.isFull()) {
                    float a = this.rocket.getFuelRoom();
                    if (fuelStored > a) {
                        this.getBaseGroup().consumeResource(a, BaseResources.fuel, Float.valueOf(this.getBaseGroup().getTotalFuelStored()));
                        this.getBaseGroup().updateResource(BaseResources.fuel, 0.0f);
                        this.rocket.addFuel(a);
                    } else {
                        this.getBaseGroup().consumeResource(fuelStored, BaseResources.fuel, Float.valueOf(fuelStored));
                        this.getBaseGroup().updateResource(BaseResources.fuel, 0.0f);
                        this.rocket.addFuel(fuelStored);
                    }
                    this.fuelingParticles.pfx.start();
                    Audio.getInstance().playSound("sounds/launchpad-fuelup.ogg", 0.6f, 1.0f);
                }
            } else {
                MoonBase.log("No fuel stored to transfer to rocket");
            }
        }
    }

    public void drainRocket() {
        if (this.rocket != null) {
            float maxFuel;
            float rocketFuel = this.rocket.getFuel();
            float fuelStored = this.getBaseGroup().getTotalFuelStored();
            if (fuelStored < (maxFuel = this.getBaseGroup().getTotalMaxFuelStored())) {
                float room = maxFuel - fuelStored;
                if (room >= rocketFuel) {
                    MoonBase.log("Setting rocket to: 0 -- storing " + rocketFuel);
                    this.rocket.setFuel(0.0f);
                    this.getBaseGroup().addResource(BaseResources.fuel, rocketFuel);
                    this.getBaseGroup().updateResource(BaseResources.fuel, 0.0f);
                } else {
                    MoonBase.log("Setting rocket to : " + (rocketFuel - room));
                    this.rocket.setFuel(rocketFuel - room);
                    this.getBaseGroup().addResource(BaseResources.fuel, room);
                    this.getBaseGroup().updateResource(BaseResources.fuel, 0.0f);
                }
                this.fuelingParticles.pfx.allowCompletion();
                float world_volume = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 2000.0f, 0.6f);
                Audio.getInstance().playSound("sounds/launchpad-fuelup.ogg", world_volume, 0.8f);
            } else {
                MoonBase.log("No room to drain rocket");
            }
        }
    }

    @Override
    public void dropPickup() {
        this.itemCrafter.dropItems(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos());
        if (this.rocket != null) {
            this.clearLaunchPad();
        }
        super.dropPickup();
    }

    public void clearLaunchPad() {
        MoonBase.log("Clearing launchpad");
        if (this.rocket != null) {
            this.drainRocket();
            this.rocket.readyToRemove = true;
            this.rocket = null;
            this.setHasRocket(false);
            this.dropRocketParts();
            MessageManager.getInstance().dispatchMessage(42, this);
        }
    }

    private void dropRocketParts() {
        ItemData itemData = ItemFactory.getItemData("rocket");
        for (RecipieRequirement r : itemData.requires) {
            for (int c = 0; c < r.quantity; ++c) {
                Item i = ItemFactory.getInstance().createItem(r.id);
                ItemPickup itemPickup = new ItemPickup(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos(), i);
            }
        }
    }

    public void setDestination(LaunchPad newDest) {
        this.targetDestination = newDest;
        this.leavePlanet = false;
    }

    public void setDestinationEndGame() {
        this.targetDestination = null;
        this.leavePlanet = true;
    }
}
