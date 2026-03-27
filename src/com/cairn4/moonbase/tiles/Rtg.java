/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.disasters.BaseFire;
import com.cairn4.moonbase.ui.BasePowerPopup;

public class Rtg
extends BaseModule {
    protected Color lightOnColor = new Color(1.0f, 0.15f, 0.15f, 0.5f);

    public Rtg(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.hasAir = false;
        this.hasPower = true;
        this.powerGenRate = this.maxPowerGen = 8.01f;
        this.powerDrawRate = 0.0f;
        this.setupPhysics("rtg");
        this.updateBases();
        this.light.setColor(this.lightOnColor);
        this.light.setStaticLight(true);
        this.light.setXray(true);
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("rtg", this.world.gameScreen.mainGroup);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "rtg";
    }

    @Override
    public void setPower(boolean on) {
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.powerGenRate = this.baseDisaster != null ? 0.0f : this.maxPowerGen;
        this.hasPower = this.powerGenRate > 0.0f;
        this.setPower(this.hasPower);
    }

    @Override
    public void interact(Player player) {
        if (this.baseDisaster != null) {
            this.baseDisaster.fix();
        } else {
            BasePowerPopup basePowerPopup = new BasePowerPopup(this.world.gameScreen, this);
            this.world.gameScreen.showMenu(basePowerPopup);
            super.interact(player);
        }
    }

    @Override
    public void triggerDisaster() {
        this.baseDisaster = new BaseFire(this);
    }
}

