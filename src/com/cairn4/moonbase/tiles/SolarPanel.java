/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.DustTriggerBehavior;
import com.cairn4.moonbase.tiles.disasters.DirtySolarPanel;
import com.cairn4.moonbase.ui.BasePowerPopup;
import com.cairn4.moonbase.ui.HudNotificationData;
import com.cairn4.moonbase.ui.Localization;

public class SolarPanel
extends BaseModule {
    public DustTriggerBehavior dustTriggerBehavior;

    public SolarPanel(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.hasAir = false;
        this.hasPower = true;
        this.powerGenRate = this.maxPowerGen = 10.01f;
        this.powerDrawRate = 0.0f;
        this.lightOnColor = new Color(0.1f, 0.2f, 1.0f, 0.3f);
        this.setupPhysics("solar");
        this.updateBases();
        this.dustTriggerBehavior = new DustTriggerBehavior();
        this.dustTriggerBehavior.solarPanel = this;
        this.dustTriggerBehavior.start();
        this.behaviorList.add(this.dustTriggerBehavior);
        this.light.setColor(this.lightOnColor);
        this.light.setXray(true);
    }

    @Override
    public void startBehaviors() {
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "solar";
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    public boolean blocksWind() {
        return false;
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("solar", this.world.gameScreen.mainGroup);
        this.group.setUserObject(Float.valueOf(this.getYCenter()));
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
        this.powerGenRate = this.baseDisaster != null ? 0.0f : this.world.dayCycle.getDayLight() * this.maxPowerGen * this.world.weatherManager.getSolarEfficiency();
        this.hasPower = this.powerGenRate > 0.0f;
    }

    @Override
    public void noPowerIcon(boolean show) {
        if (!show) {
            this.noPower.clearActions();
            this.noPower.setVisible(false);
        }
    }

    @Override
    public void fixDisaster() {
        super.fixDisaster();
        this.getBehavior(DustTriggerBehavior.class).start();
        this.hasPower = true;
        this.resetPowerState();
    }

    @Override
    public void triggerDisaster() {
        this.baseDisaster = new DirtySolarPanel(this);
        if (this.world.player.playerStatus.baseGroupsInRange.contains(this.getBaseGroup())) {
            HudNotificationData msg = new HudNotificationData();
            msg.icon = "solar";
            msg.message = Localization.get("solarpanel_needscleaning");
            msg.textColor = Color.ORANGE;
            MessageManager.getInstance().dispatchMessage(3, msg);
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

    public void setDirtySprite() {
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable("solar-dirty"));
    }

    public void setCleanSprite() {
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable("solar"));
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/solarpanel-clean.p", ParticleEffect.class));
        ParticleActor cleanFx = new ParticleActor(p, true);
        cleanFx.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.group.addActor(cleanFx);
        cleanFx.pfx.start();
    }
}

