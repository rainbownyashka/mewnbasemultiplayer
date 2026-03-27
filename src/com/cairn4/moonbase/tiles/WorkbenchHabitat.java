/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.ui.CraftingUI;

public class WorkbenchHabitat
extends HabitatModule {
    Image workbenchImage;
    Player player;
    PlayerInventory playerInventory;
    private Image buildFinishedIcon;
    private ItemCrafter itemCrafter;
    private boolean animating;

    public WorkbenchHabitat(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 3.0f;
        this.componentGroup.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.componentGroup.setOrigin(0.0f, -50.0f);
        this.workbenchImage = new Image(world.gameScreen.skin.getDrawable("workbench"));
        this.workbenchImage.setSize(TILE_SIZE * 0.85f, TILE_SIZE * 0.85f);
        this.workbenchImage.setPosition(0.0f, 0.0f, 1);
        this.componentGroup.addActor(this.workbenchImage);
        this.itemCrafter = new ItemCrafter();
        this.itemCrafter.baseModule = this;
        this.itemCrafter.buildQueueSizeLimit = 4;
        this.itemCrafter.requirePowerToCraft = false;
        this.itemCrafter.setupBuildables("crafting-station");
        this.itemCrafter.soundFinishBuilding = "sounds/footstep1.ogg";
        this.behaviorList.add(this.itemCrafter);
        this.buildFinishedIcon = new Image(world.gameScreen.skin.getDrawable("crafting-notification"));
        this.buildFinishedIcon.setOrigin(4);
        this.buildFinishedIcon.setScale(0.3f);
        this.buildFinishedIcon.setPosition(this.getWorldXPos() + TILE_SIZE / 2.0f, this.getWorldYPos() + TILE_SIZE * 0.7f, 4);
        world.gameScreen.worldUiGroup.addActor(this.buildFinishedIcon);
        this.itemCrafter.setFinishedIcon(this.buildFinishedIcon);
    }

    @Override
    public void startBehaviors() {
        super.startBehaviors();
        this.itemCrafter.start();
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "workbench-habitat";
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        if (this.itemCrafter.building) {
            if (!this.animating) {
                this.startBuildAnim();
            }
        } else if (this.animating) {
            this.stopBuildAnim();
        }
    }

    private void startBuildAnim() {
        this.animating = true;
        this.componentGroup.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.02f, 0.98f, 0.55f, Interpolation.sine), (Action)Actions.scaleTo(0.98f, 1.02f, 0.55f, Interpolation.sine))));
    }

    private void stopBuildAnim() {
        this.animating = false;
        this.componentGroup.clearActions();
        this.componentGroup.setScale(1.0f, 1.0f);
    }

    @Override
    public void interact(Player player) {
        if (this.hasDisaster()) {
            this.fixDisaster();
        } else if (player.playerInventory.getEquippedItemId().equals("paintbrush")) {
            this.changeColor(player.getPaintColorIndex());
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
                    this.world.gameScreen.showMenu(craftingUI);
                }
                super.interact(player);
            } else {
                MoonBase.error("CraftingStation: no ItemCrafter behavior");
            }
        }
    }

    @Override
    public void dropPickup() {
        this.itemCrafter.dropItems(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos());
        super.dropPickup();
    }

    @Override
    public void setPower(boolean on) {
        if (this.hasPower != on && this.workbenchImage != null) {
            if (!this.hasPower) {
                this.animateColor(this.workbenchImage, Color.WHITE, 0.25f);
            } else {
                this.animateColor(this.workbenchImage, POWER_OFF_COLOR, 0.25f);
                this.oxygenGenRate = 0.0f;
            }
        }
        super.setPower(on);
    }
}

