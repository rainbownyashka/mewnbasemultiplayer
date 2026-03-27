/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.CooldownCallback;
import com.cairn4.moonbase.tiles.behaviors.CooldownTimer;
import com.cairn4.moonbase.tiles.behaviors.ToggleBehavior;

public class Torch
extends BaseModule {
    ParticleActor fire;
    AdditiveImage glow;
    private final float maxPowerDraw = 1.0f;
    public ToggleBehavior toggleBehavior;
    public CooldownTimer cooldownTimer;
    public boolean burning = true;
    protected Color lightOnColor;

    public Torch(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 1.0f;
        this.powerGenRate = 0.0f;
        this.updateBases();
        ParticleEffect fireEffect = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/torch.p", ParticleEffect.class));
        this.fire = new ParticleActor(fireEffect, false);
        this.fire.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f + 17.0f);
        this.group.addActor(this.fire);
        this.glow = new AdditiveImage(world.gameScreen.skin.getDrawable("small-gradient-circle"));
        this.glow.setColor(Color.valueOf("e47715cc"));
        this.glow.setSize(200.0f, 200.0f);
        this.glow.setOrigin(1);
        this.world.gameScreen.topGroup.addActor(this.glow);
        this.glow.setPosition(this.getXCenter() - 5.0f, this.getYCenter() + 15.0f, 1);
        this.toggleBehavior = new ToggleBehavior();
        this.toggleBehavior.on = true;
        this.behaviorList.add(this.toggleBehavior);
        this.cooldownTimer = new CooldownTimer();
        CooldownCallback cooldownCallback = new CooldownCallback(){

            @Override
            public void callback() {
                Torch.this.extinguish();
            }
        };
        this.cooldownTimer.length = 600.0f;
        this.cooldownTimer.callback = cooldownCallback;
        this.cooldownTimer.start();
        this.behaviorList.add(this.cooldownTimer);
    }

    @Override
    public void startBehaviors() {
        if (this.toggleBehavior.on) {
            this.relight();
        } else {
            this.extinguish();
        }
    }

    @Override
    public void setupLight() {
        this.lightOnColor = new Color(1.0f, 0.9f, 0.0f, 0.75f);
        this.light = new PointLight(this.world.rayHandler, 24, this.lightOnColor, 1.40625f, this.getXCenter() / 256.0f, this.getYCenter() / 256.0f);
        this.light.setStaticLight(true);
        this.light.setXray(true);
        this.light.setIgnoreAttachedBody(true);
    }

    public void relight() {
        this.cooldownTimer.trigger();
        this.burning = true;
        this.light.setActive(true);
        this.glow.setVisible(true);
        this.fire.pfx.start();
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable("torch"));
        this.toggleBehavior.on = true;
    }

    public void extinguish() {
        this.cooldownTimer.running = false;
        this.burning = false;
        this.light.setActive(false);
        this.glow.setVisible(false);
        this.fire.pfx.allowCompletion();
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable("torch-extinguished"));
        this.toggleBehavior.on = false;
    }

    @Override
    public Color getLightOnColor() {
        return this.lightOnColor;
    }

    @Override
    public Color getMinimapColor() {
        return Color.valueOf("ffee33");
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("torch", this.world.gameScreen.mainGroup);
        this.group.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + (TILE_SIZE / 2.0f + 20.0f)));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.burning) {
            if (this.world.weatherManager.getRainRate() > 0.4f) {
                this.extinguish();
            }
            if (this.world.weatherManager.getWindSpeed() > 0.75f) {
                this.extinguish();
            }
        }
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "torch";
    }

    @Override
    public void destroy() {
        this.glow.remove();
        super.destroy();
    }

    @Override
    public boolean usesBaseGroup() {
        return false;
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    public boolean visuallyConnectToConduits() {
        return false;
    }

    @Override
    public boolean blocksWind() {
        return false;
    }

    @Override
    public void interact(Player player) {
        if (!this.burning) {
            this.relight();
        } else {
            this.extinguish();
        }
    }

    @Override
    public boolean canInteract(Player player) {
        return true;
    }
}

