/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.SpineActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.ui.CraftingUI;
import com.esotericsoftware.spine.Bone;

public class AdvancedFabricator
extends BaseModule {
    Player player;
    PlayerInventory playerInventory;
    private Image buildFinishedIcon;
    private ItemCrafter itemCrafter;
    private ParticleActor buildingParticles;
    private boolean buildParticlesOn = false;
    protected SpineActor spineActor;
    protected Bone fxBone;

    public AdvancedFabricator(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/construction-sparks.p", ParticleEffect.class));
        this.buildingParticles = new ParticleActor(p, false);
        this.buildingParticles.setPosition(this.getXCenter() + 10.0f, this.getYCenter() - 15.0f);
        this.buildingParticles.pfx.allowCompletion();
        this.world.gameScreen.topGroup.addActor(this.buildingParticles);
        this.buildParticlesOn = false;
        this.toggleCraftingFx(false);
        this.itemCrafter = new ItemCrafter();
        this.itemCrafter.baseModule = this;
        this.itemCrafter.setupBuildables(this.builderId);
        this.itemCrafter.requirePowerToCraft = true;
        this.behaviorList.add(this.itemCrafter);
        this.powerPriority = 4;
        this.powerDrawRate = 5.0f;
        this.powerGenRate = 0.0f;
        this.setupPhysics("crafting-station");
        this.updateBases();
        this.buildFinishedIcon = new Image(world.gameScreen.skin.getDrawable("crafting-notification"));
        this.buildFinishedIcon.setOrigin(4);
        this.buildFinishedIcon.setScale(0.3f);
        this.buildFinishedIcon.setPosition(this.getWorldXPos() + TILE_SIZE / 2.0f, this.getWorldYPos() + TILE_SIZE * 0.7f, 4);
        world.gameScreen.worldUiGroup.addActor(this.buildFinishedIcon);
        this.itemCrafter.setFinishedIcon(this.buildFinishedIcon);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "advancedfabricator";
    }

    @Override
    public float getPowerDrawRate() {
        if (this.itemCrafter != null && this.itemCrafter.isBuilding()) {
            return this.powerDrawRate;
        }
        return 2.0f;
    }

    @Override
    public void startBehaviors() {
        this.itemCrafter.start();
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("advancedfabricator", this.world.gameScreen.mainGroup);
        this.group.setUserObject(Float.valueOf(this.getYCenter() - 15.0f));
        this.image.setVisible(false);
        this.setupSpineActor();
    }

    public void setupSpineActor() {
        this.spineActor = new SpineActor("advancedfabricator", true, 0.5f, this.world.gameScreen.skeletonRenderer);
        this.spineActor.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.group.addActor(this.spineActor);
        this.spineActor.addAnimation(0, "idle", false, 0.0f);
        this.spineActor.stateData.setMix("idle", "working", 0.1f);
        this.spineActor.stateData.setMix("working", "idle", 0.3f);
        this.spineActor.stateData.setMix("powerOff", "powerOn", 0.25f);
        this.spineActor.stateData.setMix("powerOn", "powerOff", 0.25f);
        this.fxBone = this.spineActor.skeleton.findBone("printer-fx");
    }

    @Override
    public void update(float delta) {
        this.spineActor.update(delta);
        super.update(delta);
        if (this.hasPower) {
            for (Behavior b : this.behaviorList) {
                b.update(delta);
            }
            this.buildingParticles.setX(this.fxBone.getWorldX() - Tile.TILE_SIZE / 2.0f + this.getXCenter());
            this.toggleCraftingFx(this.itemCrafter.isBuilding());
        } else {
            this.toggleCraftingFx(false);
        }
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
                    craftingUI.setTitle("item_advancedfabricator-builder");
                    this.world.gameScreen.showMenu(craftingUI);
                }
            } else {
                CraftingUI craftingUI = new CraftingUI(this.world.gameScreen, ic, player.playerInventory);
                craftingUI.setTitle("item_advancedfabricator-builder");
                this.world.gameScreen.showMenu(craftingUI);
            }
            super.interact(player);
        } else {
            MoonBase.error("AdvancedFabricator: no ItemCrafter behavior");
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
        this.buildingParticles.remove();
        super.destroy();
    }

    private void toggleCraftingFx(boolean on) {
        if (on && !this.buildParticlesOn) {
            this.buildingParticles.pfx.reset();
            this.buildingParticles.pfx.start();
            this.buildParticlesOn = true;
            this.spineActor.setAnimation(0, "working", true);
        } else if (!on && this.buildParticlesOn) {
            this.buildingParticles.pfx.allowCompletion();
            this.buildParticlesOn = false;
            this.spineActor.setAnimation(0, "idle", false);
        }
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
}

