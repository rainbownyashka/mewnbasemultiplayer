/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.ui.CraftingUI;

public class Kitchen
extends HabitatModule {
    Player player;
    PlayerInventory playerInventory;
    private Image buildFinishedIcon;
    private ItemCrafter itemCrafter;
    ParticleActor bubblefx;
    private Image componentImage;

    public Kitchen(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 5.0f;
        this.powerGenRate = 0.0f;
        this.componentGroup.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.componentGroup.setOrigin(0.0f, -50.0f);
        this.componentImage = new Image(world.gameScreen.skin.getDrawable("kitchen"));
        this.componentImage.setOrigin(1);
        this.componentImage.setScale(0.9f);
        this.componentImage.setPosition(0.0f, 0.0f, 1);
        this.componentGroup.addActor(this.componentImage);
        ParticleEffect bubbleEffectSource = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/chemistrylab.p", ParticleEffect.class));
        this.bubblefx = new ParticleActor(bubbleEffectSource, false);
        this.bubblefx.setPosition(-20.0f, 0.0f);
        this.componentGroup.addActor(this.bubblefx);
        this.itemCrafter = new ItemCrafter();
        this.itemCrafter.baseModule = this;
        this.itemCrafter.setupBuildables(this.builderId);
        this.itemCrafter.requirePowerToCraft = true;
        this.itemCrafter.soundFinishBuilding = "sounds/footstep1.ogg";
        this.behaviorList.add(this.itemCrafter);
        this.buildFinishedIcon = new Image(world.gameScreen.skin.getDrawable("crafting-notification"));
        this.buildFinishedIcon.setOrigin(4);
        this.buildFinishedIcon.setScale(0.3f);
        this.buildFinishedIcon.setPosition(this.getWorldXPos() + TILE_SIZE / 2.0f, this.getWorldYPos() + TILE_SIZE * 0.7f, 4);
        world.gameScreen.worldUiGroup.addActor(this.buildFinishedIcon);
        this.itemCrafter.setFinishedIcon(this.buildFinishedIcon);
        this.updateBases();
    }

    @Override
    public void startBehaviors() {
        this.itemCrafter.start();
    }

    @Override
    public void setPower(boolean on) {
        if (this.hasPower != on && this.componentImage != null) {
            if (!this.hasPower) {
                this.animateColor(this.componentImage, Color.WHITE, 0.25f);
                this.animateColor(this.componentImage, Color.WHITE, 0.25f);
            } else {
                this.animateColor(this.componentImage, POWER_OFF_COLOR, 0.25f);
                this.animateColor(this.componentImage, POWER_OFF_COLOR, 0.25f);
            }
        }
        super.setPower(on);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "kitchen";
    }

    @Override
    public float getPowerDrawRate() {
        if (this.itemCrafter != null && this.itemCrafter.isBuilding()) {
            return this.powerDrawRate;
        }
        return this.powerDrawRate * 0.3f;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        this.toggleFx(this.itemCrafter.isBuilding() && this.hasPower);
    }

    @Override
    public void interact(Player player) {
        if (this.hasDisaster()) {
            this.fixDisaster();
        } else {
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
                    craftingUI.setTitle("item_chemistrylab-builder");
                    this.world.gameScreen.showMenu(craftingUI);
                }
                super.interact(player);
            } else {
                MoonBase.error("Kitchen: no ItemCrafter behavior");
            }
        }
    }

    private void toggleFx(boolean building) {
        if (building) {
            this.componentImage.setDrawable(this.world.gameScreen.skin.getDrawable("kitchen"));
            if (this.bubblefx.pfx.isComplete()) {
                this.bubblefx.pfx.start();
            }
        } else {
            this.componentImage.setDrawable(this.world.gameScreen.skin.getDrawable("kitchen"));
            this.bubblefx.pfx.allowCompletion();
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
        super.destroy();
    }
}

