/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.Localization;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Lander
extends Tile
implements Observer {
    Group landerGroup;
    Image landerImage;
    Image shadow;
    Image engine;
    Image engineGlow;
    ParticleActor groundDust;
    ParticleActor readyDust;
    ParticleActor pFlameEffect;
    PointLight light;
    PointLight takeOffLight;
    private boolean landing = false;
    private int flameFrame = 1;
    private int flameFrameMax = 4;
    private float flameTimer;
    private float flameDelay = 0.05f;

    public Lander(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.type = Tile.types.blockingTerrain;
        this.blocker = true;
        this.setupPhysics("lander");
        this.addLight();
        MoonBase.getCurrentMission().addObserver(this);
        this.world.lander = this;
    }

    private void readyForTakeoffFx() {
    }

    public void openDoor() {
        this.landerImage.setDrawable(this.world.gameScreen.skin.getDrawable("lander-open"));
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/lander-ready.p", ParticleEffect.class));
        if (this.readyDust == null) {
            this.readyDust = new ParticleActor(p, false);
            this.readyDust.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 5.0f);
            this.group.addActor(this.readyDust);
            this.readyDust.toBack();
        }
        this.readyDust.pfx.start();
    }

    private void closeDoor() {
        this.landerImage.setDrawable(this.world.gameScreen.skin.getDrawable("lander"));
        float v = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 500.0f, 0.4f);
        Audio.getInstance().playSound("sounds/gameOverButton.ogg", v, 1.0f);
        this.readyDust.pfx.allowCompletion();
    }

    @Override
    public Color getMinimapColor() {
        return Color.GOLD;
    }

    @Override
    public String getMapName() {
        return Localization.get("lander");
    }

    @Override
    protected void createDrawables() {
        this.group.setPosition(this.getWorldXPos(), this.getWorldYPos());
        this.group.setUserObject(Float.valueOf(this.getWorldYPos() + 17.0f));
        this.world.gameScreen.addToStage(this.group, this.world.gameScreen.mainGroup);
        this.shadow = new Image(this.world.gameScreen.skin.getDrawable("lander-shadow"));
        this.shadow.setSize(this.shadow.getWidth() * Tile.SCALE, this.shadow.getHeight() * Tile.SCALE);
        this.shadow.setPosition(TILE_SIZE / 2.0f, -15.0f, 4);
        this.shadow.setOrigin(1);
        this.shadow.setColor(0.0f, 0.0f, 0.0f, 0.3f);
        this.group.addActor(this.shadow);
        this.landerGroup = new Group();
        this.landerGroup.setPosition(TILE_SIZE / 2.0f, 0.0f);
        this.group.addActor(this.landerGroup);
        this.landerImage = new Image(this.world.gameScreen.skin.getDrawable("lander"));
        this.landerImage.setSize(348.0f * Tile.SCALE, 314.0f * Tile.SCALE);
        this.landerImage.setPosition(0.0f, -15.0f, 4);
        this.landerGroup.addActor(this.landerImage);
        this.engine = new Image(this.world.gameScreen.skin.getDrawable("lander-flame1"));
        this.engine.setOrigin(2);
        this.engine.setSize(126.0f * SCALE, 108.0f * SCALE);
        this.engine.setPosition(this.landerImage.getX(1), 15.0f, 2);
        this.engine.setVisible(false);
        this.landerGroup.addActor(this.engine);
        this.engine.toBack();
        this.engineGlow = new Image(this.world.gameScreen.skin.getDrawable("lander-engine-glow"));
        this.engineGlow.setSize(258.0f * SCALE, 50.0f * SCALE);
        this.engineGlow.setPosition(this.landerImage.getX(1), this.landerImage.getY(4) + 10.0f, 4);
        this.engineGlow.setVisible(false);
        this.landerGroup.addActor(this.engineGlow);
    }

    public void showLandingSite() {
        GroundTile g;
        Tile t;
        ArrayList<GridPoint2> coords = Tile.GET_ADJACENT_COORDS(this.worldX, this.worldY);
        for (GridPoint2 coord : coords) {
            this.world.updateDiscovery(coord.x, coord.y, 6);
        }
        for (GridPoint2 adjacent : Tile.GET_ADJACENT_COORDS(this.worldX, this.worldY)) {
            t = this.world.getTile(adjacent.x, adjacent.y);
            if (t != null && t != this) {
                t.readyToRemove = true;
                Gdx.app.log("MewnBase", "Removing tile from landing site: " + t.getClass().getSimpleName());
            }
            if ((g = this.world.getGroundTile(adjacent.x, adjacent.y)) == null) continue;
            g.setDiscovered(true);
        }
        for (GridPoint2 adjacent : Tile.GET_ADJACENT_COORDS(this.worldX, this.worldY - 1, true)) {
            t = this.world.getTile(adjacent.x, adjacent.y);
            if (t != null && t != this) {
                t.readyToRemove = true;
                Gdx.app.log("MewnBase", "Removing tile from landing site: " + t.getClass().getSimpleName());
            }
            if ((g = this.world.getGroundTile(adjacent.x, adjacent.y)) == null) continue;
            g.setDiscovered(true);
        }
    }

    public void land() {
        this.landing = true;
        Audio.getInstance().playSound("sounds/lander-rocketdown.ogg");
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/lander-smoke.p", ParticleEffect.class));
        this.groundDust = new ParticleActor(p, false);
        this.groundDust.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 5.0f);
        this.group.addActor(this.groundDust);
        this.groundDust.toBack();
        this.groundDust.pfx.start();
        this.shadow.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.2f, 0.2f), (Action)Actions.parallel((Action)Actions.alpha(0.3f, 2.0f), (Action)Actions.scaleTo(1.0f, 1.0f, 2.0f))));
        this.landerGroup.setY(1000.0f);
        this.landerGroup.addAction(Actions.sequence((Action)Actions.moveTo(TILE_SIZE / 2.0f, 50.0f, 1.5f, Interpolation.sineOut), (Action)Actions.moveTo(TILE_SIZE / 2.0f, 0.0f, 0.25f, Interpolation.sine), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                Lander.this.finishLanding();
            }
        })));
        this.engine.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.delay(1.5f), (Action)Actions.parallel((Action)Actions.fadeOut(0.5f), (Action)Actions.scaleTo(1.0f, 0.0f, 0.5f))));
        this.engineGlow.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.delay(1.5f), (Action)Actions.fadeOut(1.0f)));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.landing) {
            this.flameTimer += delta;
            if (this.flameTimer > this.flameDelay) {
                this.flameTimer = 0.0f;
                ++this.flameFrame;
                if (this.flameFrame > this.flameFrameMax) {
                    this.flameFrame = 1;
                }
                this.engine.setDrawable(this.world.gameScreen.skin.getDrawable("lander-flame" + this.flameFrame));
            }
        }
        Vector2 lPos = new Vector2(0.0f, 0.0f);
        if (this.pFlameEffect != null) {
            lPos = this.landerGroup.localToParentCoordinates(new Vector2(0.0f, 0.0f));
            this.pFlameEffect.setPosition(lPos.x, lPos.y);
        }
        if (this.takeOffLight != null) {
            this.takeOffLight.setPosition(this.getWorldXPos() / 256.0f, (this.getWorldYPos() + lPos.y) / 256.0f);
        }
    }

    private void finishLanding() {
        this.landing = false;
        Audio.getInstance().playSound("sounds/lander-hitground.ogg");
        this.groundDust.pfx.allowCompletion();
        this.world.gameScreen.cameraShake.shake(20.0f, 40.0f, 0.35f);
        this.world.spawnPlayer(this.chunk, this.x, this.y - 1, true);
    }

    private void addLight() {
        this.light = new PointLight(this.world.rayHandler, 64, new Color(1.0f, 1.0f, 0.5f, 0.8f), 0.78125f, this.getXCenter() / 256.0f, (this.getYCenter() + 30.0f) / 256.0f);
        this.light.attachToBody(this.body);
        this.light.setContactFilter(this.categoryBits, this.categoryBits, this.maskBits);
        this.light.setStaticLight(true);
        this.light.getIgnoreAttachedBody();
    }

    @Override
    public void interact(Player player) {
        if (MoonBase.getCurrentMission().missionCompleteReady && !player.isFlyingRocket()) {
            this.world.gameScreen.hud.showMissionReadyMsg();
        } else {
            this.world.gameScreen.hud.hudNotifications.newMessage(Localization.get("missionNotFinished"));
        }
    }

    public void startLaunch(Player player) {
        MoonBase.getCurrentMission().setMissionComplete();
        this.world.gameScreen.gameLoader.saveGame(this.world, false);
        this.takeOff();
    }

    private void takeOff() {
        this.landing = true;
        Audio.getInstance().playSound("sounds/lander-rocketdown.ogg");
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/lander-smoke.p", ParticleEffect.class));
        this.groundDust = new ParticleActor(p, false);
        this.groundDust.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 5.0f);
        this.group.addActor(this.groundDust);
        this.groundDust.toBack();
        this.groundDust.pfx.start();
        ParticleEffect pFlames = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/lander-takeoff.p", ParticleEffect.class));
        this.pFlameEffect = new ParticleActor(pFlames, false);
        this.pFlameEffect.setPosition(0.0f, -TILE_SIZE / 5.0f);
        this.group.addActor(this.pFlameEffect);
        this.pFlameEffect.toBack();
        this.pFlameEffect.pfx.start();
        this.engineGlow.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.alpha(1.0f, 0.5f)));
        this.engine.addAction(Actions.sequence((Action)Actions.scaleTo(1.0f, 0.0f), (Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, 1.0f), (Action)Actions.fadeIn(0.5f))));
        this.world.gameScreen.cameraShake.shake(10.0f, 40.0f, 1.5f);
        this.landerGroup.addAction(Actions.sequence((Action)Actions.delay(1.0f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                Lander.this.world.gameScreen.cameraLag.setZoom(1.7f, 3.0f, Interpolation.sineIn);
                Lander.this.world.gameScreen.cameraLag.panTo(Lander.this.getXCenter(), Lander.this.getYCenter() + 300.0f, 3.0f, Interpolation.sineIn);
            }
        }), (Action)Actions.moveTo(TILE_SIZE / 2.0f, 1000.0f, 2.0f, Interpolation.circleIn), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                Lander.this.groundDust.pfx.allowCompletion();
                Lander.this.readyToRemove = true;
                Lander.this.world.gameWin();
            }
        })));
        this.shadow.addAction(Actions.sequence((Action)Actions.delay(1.0f), (Action)Actions.parallel((Action)Actions.scaleTo(0.2f, 0.2f, 1.5f), (Action)Actions.fadeOut(1.5f))));
        this.takeOffLight = new PointLight(this.world.rayHandler, 64, new Color(1.0f, 0.5f, 0.5f, 0.8f), 2.34375f, this.getXCenter() / 256.0f, (this.getYCenter() + 30.0f) / 256.0f);
        this.takeOffLight.setContactFilter(this.categoryBits, this.categoryBits, this.maskBits);
        this.takeOffLight.setStaticLight(true);
        this.takeOffLight.setXray(true);
        this.takeOffLight.getIgnoreAttachedBody();
    }

    @Override
    public void update(Observable observable, Object o) {
    }
}

