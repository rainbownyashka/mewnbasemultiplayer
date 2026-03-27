/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.Gdx;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.GarageCrafter;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.ui.CraftingUI;

public class Garage
extends BaseModule {
    Player player;
    PlayerInventory playerInventory;
    GarageCrafter garageCrafter;

    public Garage(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 3.0f;
        this.blocker = false;
        this.walkable = true;
        this.garageCrafter = new GarageCrafter();
        this.garageCrafter.setSpawnTile(this.chunk, this.x, this.y);
        this.garageCrafter.buildQueueSizeLimit = 1;
        this.garageCrafter.requirePowerToCraft = true;
        this.garageCrafter.baseModule = this;
        this.garageCrafter.soundFinishBuilding = "sounds/footstep1.ogg";
        this.garageCrafter.setupBuildables(this.builderId);
        this.behaviorList.add(this.garageCrafter);
        this.updateBases();
    }

    @Override
    public void startBehaviors() {
        this.garageCrafter.start();
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("garage");
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "garage";
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
    }

    @Override
    public void interact(Player player) {
        super.interact(player);
        this.player = player;
        this.playerInventory = player.getPlayerInventory();
        ItemCrafter ic = null;
        for (Behavior b : this.behaviorList) {
            b.interact(player);
            if (!(b instanceof ItemCrafter)) continue;
            ic = (ItemCrafter)b;
        }
        if (ic != null) {
            CraftingUI craftingUI = new CraftingUI(this.world.gameScreen, ic, this.playerInventory);
            this.world.gameScreen.showMenu(craftingUI);
            craftingUI.setTitle("item_garage-builder");
            super.interact(player);
        } else {
            Gdx.app.error("MoonBase", "CraftingStation: no ItemCrafter behavior");
        }
    }

    @Override
    public void dropPickup() {
        this.garageCrafter.dropItems(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos());
        super.dropPickup();
    }
}

