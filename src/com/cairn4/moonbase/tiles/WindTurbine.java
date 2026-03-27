/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.WindDamageCallback;
import com.cairn4.moonbase.tiles.behaviors.WindSpeedDamageBehavior;
import com.cairn4.moonbase.tiles.disasters.BrokenWindTurbine;
import com.cairn4.moonbase.ui.BasePowerPopup;
import com.cairn4.moonbase.ui.HudNotificationData;
import com.cairn4.moonbase.ui.Localization;
import java.util.ArrayList;

public class WindTurbine
extends BaseModule {
    public WindSpeedDamageBehavior windSpeedDamageBehavior;
    private float availableMaxPowerGen;
    protected Color lightOnColor;
    private Image blades;
    private float bladeSpinRate;
    private ArrayList<GridPoint2> neighborCoords;
    private float neighborCheckDelay;
    private float neighborCheckTimer;
    private static final String baseSprite = "windturbine";
    private static final String bladeSprite = "windturbine-blades";
    private static final String frontSprite = "windturbine-front";
    private int numObstructions;
    private Image frontImage;
    private ParticleActor brokenParticles;
    float spinDownTimer;

    public WindTurbine(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.availableMaxPowerGen = this.maxPowerGen;
        this.lightOnColor = new Color(0.8f, 0.4f, 0.0f, 0.6f);
        this.bladeSpinRate = 200.0f;
        this.neighborCoords = new ArrayList();
        this.neighborCheckDelay = 2.0f;
        this.neighborCheckTimer = 0.0f;
        this.hasAir = false;
        this.hasPower = true;
        this.powerGenRate = this.maxPowerGen = 20.01f;
        this.powerDrawRate = 0.0f;
        this.setupPhysics("rtg");
        this.updateBases();
        this.light.setColor(this.lightOnColor);
        this.light.setDistance(0.78125f);
        this.light.setXray(true);
        this.windSpeedDamageBehavior = new WindSpeedDamageBehavior();
        this.windSpeedDamageBehavior.baseModule = this;
        this.windSpeedDamageBehavior.dangerSpeed = 0.7f;
        this.windSpeedDamageBehavior.windHealthMax = 240.0f;
        this.windSpeedDamageBehavior.windHealth = 10.0f;
        this.windSpeedDamageBehavior.windDamageCallback = new WindDamageCallback(){

            @Override
            public void windSpeedDanger(boolean danger) {
                if (danger) {
                    // empty if block
                }
            }

            @Override
            public void damageBase() {
                MoonBase.debug("trigger wind turbine disaster");
                WindTurbine.this.triggerDisaster();
            }
        };
        this.behaviorList.add(this.windSpeedDamageBehavior);
        this.neighborCoords = Tile.GET_ADJACENT_COORDS(this.worldX, this.worldY, false);
    }

    @Override
    public void startBehaviors() {
    }

    @Override
    protected void setBuilderId() {
        this.builderId = baseSprite;
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    protected void createDrawables() {
        super.createDrawables(baseSprite, this.world.gameScreen.mainGroup);
        this.image.setSize(TILE_SIZE, 379.0f * SCALE);
        this.image.setOrigin(1);
        this.image.setPosition(0.0f, 0.0f);
        this.group.setUserObject(Float.valueOf(this.getYCenter()));
        this.blades = new Image(this.world.gameScreen.skin.getDrawable(bladeSprite));
        this.blades.setOrigin(1);
        this.blades.setPosition(this.image.getWidth() / 2.0f - 3.0f, 260.0f * SCALE + 3.0f, 1);
        this.blades.setRotation(MathUtils.random(0, 270));
        this.group.addActor(this.blades);
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/windturbine-broken.p", ParticleEffect.class));
        this.brokenParticles = new ParticleActor(p, false);
        this.brokenParticles.setPosition(this.image.getWidth() / 2.0f - 3.0f, 266.0f * SCALE, 1);
        this.brokenParticles.pfx.allowCompletion();
        this.group.addActor(this.brokenParticles);
        this.frontImage = new Image(this.world.gameScreen.skin.getDrawable(frontSprite));
        this.frontImage.setOrigin(1);
        this.frontImage.setPosition(this.image.getWidth() / 2.0f - 3.0f, 266.0f * SCALE, 1);
        this.group.addActor(this.frontImage);
    }

    public int getNumObstructions() {
        return this.numObstructions;
    }

    @Override
    public void setPower(boolean on) {
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        if (this.baseDisaster == null) {
            this.spinBlades(delta);
        } else {
            this.spinDownBlades(delta);
        }
        this.powerGenRate = this.baseDisaster != null ? 0.0f : this.availableMaxPowerGen * this.world.weatherManager.getWindSpeed();
        this.neighborCheckTimer += delta;
        if (this.neighborCheckTimer > this.neighborCheckDelay) {
            this.neighborCheckTimer = 0.0f;
            this.numObstructions = this.checkForObstructions();
        }
        this.availableMaxPowerGen = this.maxPowerGen * this.calcObstructionMul();
        this.hasPower = this.powerGenRate > 0.0f;
    }

    private int checkForObstructions() {
        this.numObstructions = 0;
        for (GridPoint2 gp : this.neighborCoords) {
            if (this.world.isTileEmpty(gp.x, gp.y) || !this.world.getTile(gp.x, gp.y).blocksWind()) continue;
            ++this.numObstructions;
        }
        return this.numObstructions;
    }

    private float calcObstructionMul() {
        float reduction = 1.0f;
        for (int i = 0; i < this.numObstructions; ++i) {
            reduction *= 0.6f;
        }
        return reduction;
    }

    private void spinBlades(float delta) {
        float mul = this.powerGenRate / this.maxPowerGen;
        this.blades.rotateBy(-this.bladeSpinRate * delta * mul);
        if (this.blades.getRotation() >= 360.0f) {
            this.blades.rotateBy(-360.0f);
        }
    }

    private void spinDownBlades(float delta) {
        this.spinDownTimer -= delta;
        if (this.spinDownTimer < 0.0f) {
            this.spinDownTimer = 0.0f;
        } else {
            this.blades.rotateBy(-this.bladeSpinRate * delta * this.spinDownTimer);
            if (this.blades.getRotation() >= 360.0f) {
                this.blades.rotateBy(-360.0f);
            }
        }
    }

    @Override
    public void interact(Player player) {
        if (this.hasDisaster()) {
            this.fixDisaster();
        } else {
            this.interactMain(player);
        }
    }

    @Override
    public void interactMain(Player player) {
        BasePowerPopup basePowerPopup = new BasePowerPopup(this.world.gameScreen, this);
        this.world.gameScreen.showMenu(basePowerPopup);
        super.interactMain(player);
    }

    @Override
    public void fixDisaster() {
        super.fixDisaster();
        this.windSpeedDamageBehavior.start();
        this.hasPower = true;
        this.resetPowerState();
    }

    @Override
    public void triggerDisaster() {
        this.baseDisaster = new BrokenWindTurbine(this);
        if (this.world.player.playerStatus.baseGroupsInRange.contains(this.getBaseGroup())) {
            HudNotificationData msg = new HudNotificationData();
            msg.icon = "windturbine-builder";
            msg.message = Localization.get("disaster_brokenWindTurbine");
            msg.textColor = Vars.WARNING_RED;
            MessageManager.getInstance().dispatchMessage(3, msg);
        } else {
            MoonBase.debug("Base too far to notify about broken wind turbine.");
        }
    }

    public void setBroken() {
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable("windturbine-damaged"));
        this.blades.setDrawable(this.world.gameScreen.skin.getDrawable("windturbine-blades-damaged"));
        this.frontImage.setDrawable(this.world.gameScreen.skin.getDrawable("windturbine-front-damaged"));
        this.brokenParticles.pfx.start();
        this.spinDownTimer = 1.0f;
    }

    public void setFixed() {
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable(baseSprite));
        this.blades.setDrawable(this.world.gameScreen.skin.getDrawable(bladeSprite));
        this.frontImage.setDrawable(this.world.gameScreen.skin.getDrawable(frontSprite));
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/solarpanel-clean.p", ParticleEffect.class));
        ParticleActor cleanFx = new ParticleActor(p, true);
        cleanFx.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.group.addActor(cleanFx);
        cleanFx.pfx.start();
        this.brokenParticles.pfx.allowCompletion();
    }
}

