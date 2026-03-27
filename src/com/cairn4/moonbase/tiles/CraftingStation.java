/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.ui.CraftingUI;

public class CraftingStation
extends BaseModule {
    Player player;
    PlayerInventory playerInventory;
    private Image buildFinishedIcon;
    private ItemCrafter itemCrafter;
    private boolean animating = false;
    protected Color lightOnColor = new Color(0.25f, 0.25f, 0.25f, 0.3f);

    public CraftingStation(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.itemCrafter = new ItemCrafter();
        this.itemCrafter.baseModule = this;
        this.itemCrafter.setupBuildables(this.builderId);
        this.itemCrafter.soundFinishBuilding = "sounds/footstep1.ogg";
        this.behaviorList.add(this.itemCrafter);
        this.powerDrawRate = 0.0f;
        this.powerGenRate = 0.0f;
        this.setupPhysics("crafting-station");
        this.updateBases();
        this.buildFinishedIcon = new Image(world.gameScreen.skin.getDrawable("crafting-notification"));
        this.buildFinishedIcon.setOrigin(4);
        this.buildFinishedIcon.setScale(0.3f);
        this.buildFinishedIcon.setPosition(this.getWorldXPos() + TILE_SIZE / 2.0f, this.getWorldYPos() + TILE_SIZE * 0.7f, 4);
        world.gameScreen.worldUiGroup.addActor(this.buildFinishedIcon);
        this.itemCrafter.setFinishedIcon(this.buildFinishedIcon);
        this.light.setColor(this.lightOnColor);
    }

    @Override
    public void startBehaviors() {
        this.itemCrafter.start();
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    public boolean usesBaseGroup() {
        return false;
    }

    @Override
    public boolean visuallyConnectToConduits() {
        return false;
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "crafting-station";
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("workbench", this.world.gameScreen.mainGroup);
        this.group.setOrigin(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f - 50.0f);
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
        this.group.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.02f, 0.98f, 0.55f, Interpolation.sine), (Action)Actions.scaleTo(0.98f, 1.02f, 0.55f, Interpolation.sine))));
    }

    private void stopBuildAnim() {
        this.animating = false;
        this.group.clearActions();
        this.group.setScale(1.0f, 1.0f);
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
    public void setPower(boolean on) {
    }

    @Override
    public void destroy() {
        this.buildFinishedIcon.remove();
        super.destroy();
    }
}

