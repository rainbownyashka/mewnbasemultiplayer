/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.ui.CraftingUI;

public class Smelter
extends BaseModule {
    private ItemCrafter itemCrafter;
    private Image buildFinishedIcon;
    Player player;
    PlayerInventory playerInventory;
    private AdditiveImage glow;
    private PointLight glowLight;
    ParticleActor smoke;
    ParticleActor fire;

    @Override
    public float getPowerDrawRate() {
        if (this.itemCrafter != null && this.itemCrafter.isBuilding()) {
            return this.powerDrawRate;
        }
        return this.powerDrawRate / 2.0f;
    }

    public Smelter(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerPriority = 4;
        this.powerDrawRate = 4.0f;
        this.powerGenRate = 0.0f;
        this.setupPhysics("smelter");
        this.addParticles();
        this.itemCrafter = new ItemCrafter();
        this.itemCrafter.requirePowerToCraft = true;
        this.itemCrafter.baseModule = this;
        this.itemCrafter.setupBuildables(this.builderId);
        this.itemCrafter.soundFinishBuilding = "sounds/crafter-metal.ogg";
        this.behaviorList.add(this.itemCrafter);
        this.updateBases();
        this.buildFinishedIcon = new Image(world.gameScreen.skin.getDrawable("crafting-notification"));
        this.buildFinishedIcon.setOrigin(4);
        this.buildFinishedIcon.setScale(0.3f);
        this.buildFinishedIcon.setPosition(this.getWorldXPos() + TILE_SIZE / 2.0f, this.getWorldYPos() + TILE_SIZE * 0.7f, 4);
        world.gameScreen.worldUiGroup.addActor(this.buildFinishedIcon);
        this.itemCrafter.setFinishedIcon(this.buildFinishedIcon);
    }

    @Override
    public void startBehaviors() {
        this.itemCrafter.start();
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("smelter", this.world.gameScreen.mainGroup);
        this.image.setPosition(-3.0f, 0.0f);
        this.glow = new AdditiveImage(this.world.gameScreen.skin.getDrawable("small-gradient-circle"));
        this.glow.setSize(100.0f, 40.0f);
        this.glow.setPosition(TILE_SIZE / 2.0f, 40.0f, 1);
        this.glow.setColor(Color.RED);
        this.group.addActor(this.glow);
        this.glow.addAction(Actions.forever(Actions.sequence((Action)Actions.alpha(0.5f, 1.0f), (Action)Actions.alpha(1.0f, 1.0f))));
        this.glow.setVisible(false);
        this.glowLight = new PointLight(this.world.rayHandler, 8, new Color(1.0f, 0.0f, 0.0f, 1.0f), 0.3125f, this.getXCenter() / 256.0f, ((float)this.worldY * Tile.TILE_SIZE + 40.0f) / 256.0f);
        this.glowLight.setXray(true);
        this.glowLight.setActive(false);
        this.glowLight.setStaticLight(true);
        this.glowLight.setIgnoreAttachedBody(true);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "smelter";
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        this.toggleFx(this.itemCrafter.isBuilding());
    }

    private void toggleFx(boolean building) {
        this.glow.setVisible(building);
        this.glowLight.setActive(building);
        if (building && this.hasPower) {
            if (this.smoke.pfx.isComplete()) {
                this.smoke.pfx.start();
                this.fire.pfx.start();
            }
        } else {
            this.smoke.pfx.allowCompletion();
            this.fire.pfx.allowCompletion();
        }
    }

    public void addParticles() {
        ParticleEffect smokeEffect = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/smelter.p", ParticleEffect.class));
        this.smoke = new ParticleActor(smokeEffect, false);
        this.smoke.setPosition(TILE_SIZE / 2.0f - 25.0f, TILE_SIZE - 20.0f);
        this.group.addActor(this.smoke);
        ParticleEffect fireEffect = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/smelter-fire.p", ParticleEffect.class));
        this.fire = new ParticleActor(fireEffect, false);
        this.fire.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f - 32.0f);
        this.group.addActor(this.fire);
    }

    @Override
    public void interact(Player player) {
        this.player = player;
        this.playerInventory = player.getPlayerInventory();
        ItemCrafter ic = null;
        for (Behavior b : this.behaviorList) {
            b.interact(player);
            if (!(b instanceof ItemCrafter)) continue;
            ic = (ItemCrafter)b;
        }
        if (ic != null) {
            if (ic.itemsToCollect.size > 0) {
                boolean collected = ic.collectItem();
                if (!collected) {
                    CraftingUI craftingUI = new CraftingUI(this.world.gameScreen, ic, player.playerInventory);
                    this.world.gameScreen.showMenu(craftingUI);
                }
            } else {
                CraftingUI craftingUI = new CraftingUI(this.world.gameScreen, ic, player.playerInventory);
                craftingUI.setTitle("item_smelter-builder");
                this.world.gameScreen.showMenu(craftingUI);
            }
            super.interact(player);
        } else {
            Gdx.app.error("MoonBase", "CraftingStation: no ItemCrafter behavior");
        }
    }

    @Override
    public void dropPickup() {
        this.itemCrafter.dropItems(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos());
        super.dropPickup();
    }

    @Override
    public void destroy() {
        this.buildFinishedIcon.remove();
        this.glowLight.remove();
        super.destroy();
    }
}

