/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.DropItemOnBaseBehavior;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.ui.Localization;

public class RecyclerOrig
extends BaseModule {
    Player player;
    PlayerInventory playerInventory;
    private Image buildFinishedIcon;
    private ItemCrafter itemCrafter;
    private DropItemOnBaseBehavior dropItemOnBaseBehavior;
    private AdditiveImage glow;
    private ParticleActor smoke;
    private Group animGroup;

    public RecyclerOrig(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 5.0f;
        this.powerGenRate = 0.0f;
        this.setupPhysics("smelter");
        this.addSmoke();
        this.itemCrafter = new ItemCrafter();
        this.itemCrafter.buildQueueSizeLimit = 1;
        this.itemCrafter.requirePowerToCraft = true;
        this.itemCrafter.baseModule = this;
        this.itemCrafter.setupBuildables(this.builderId);
        this.behaviorList.add(this.itemCrafter);
        this.buildFinishedIcon = new Image(world.gameScreen.skin.getDrawable("crafting-notification"));
        this.buildFinishedIcon.setOrigin(4);
        this.buildFinishedIcon.setScale(0.3f);
        this.buildFinishedIcon.setPosition(this.getWorldXPos() + TILE_SIZE / 2.0f, this.getWorldYPos() + TILE_SIZE * 0.7f, 4);
        world.gameScreen.worldUiGroup.addActor(this.buildFinishedIcon);
        this.itemCrafter.setFinishedIcon(this.buildFinishedIcon);
        this.dropItemOnBaseBehavior = new DropItemOnBaseBehavior();
        this.dropItemOnBaseBehavior.baseModule = this;
        this.dropItemOnBaseBehavior.acceptedItemIds.add("refined-scrap");
        this.behaviorList.add(this.dropItemOnBaseBehavior);
        this.updateBases();
    }

    @Override
    protected void createDrawables() {
        this.animGroup = new Group();
        this.animGroup.setOrigin(Tile.TILE_SIZE / 2.0f, 0.0f);
        super.createDrawables("recycler", this.world.gameScreen.mainGroup);
        this.image.setPosition(-3.0f, 0.0f);
        this.group.addActor(this.animGroup);
        this.animGroup.addActor(this.image);
        this.group.removeActor(this.image);
        this.group.setOrigin(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f - 30.0f);
        this.glow = new AdditiveImage(this.world.gameScreen.skin.getDrawable("small-gradient-circle"));
        this.glow.setSize(100.0f, 40.0f);
        this.glow.setPosition(TILE_SIZE / 2.0f, 40.0f, 1);
        this.glow.setColor(Color.RED);
        this.glow.setVisible(false);
        this.group.addActor(this.glow);
        this.glow.addAction(Actions.forever(Actions.sequence((Action)Actions.alpha(0.5f, 1.0f), (Action)Actions.alpha(1.0f, 1.0f))));
        this.glow.setVisible(false);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "recycler-orig";
    }

    @Override
    public void startBehaviors() {
        this.itemCrafter.start();
        if (this.itemCrafter.isBuilding() && this.animGroup != null) {
            this.animGroup.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.0f, 0.98f, 0.13f), (Action)Actions.scaleTo(1.0f, 1.01f, 0.13f))));
        }
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
    public float getPowerDrawRate() {
        if (this.itemCrafter != null && this.itemCrafter.isBuilding()) {
            return this.powerDrawRate;
        }
        return this.powerDrawRate * 0.1f;
    }

    public void addSmoke() {
        this.smoke = new ParticleActor(Gdx.files.internal("particles/smelter.p"), this.world.gameScreen.atlas, false);
        this.smoke.setPosition(TILE_SIZE / 2.0f - 15.0f, TILE_SIZE - 40.0f);
        this.group.addActor(this.smoke);
    }

    private void toggleFx(boolean building) {
        if (building) {
            this.image.setDrawable(this.world.gameScreen.skin.getDrawable("recycler"));
            if (this.smoke.pfx.isComplete()) {
                this.smoke.pfx.start();
                if (!this.animGroup.hasActions()) {
                    this.animGroup.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.0f, 0.98f, 0.13f), (Action)Actions.scaleTo(1.0f, 1.01f, 0.13f))));
                }
            }
        } else {
            this.image.setDrawable(this.world.gameScreen.skin.getDrawable("recycler-off"));
            this.smoke.pfx.allowCompletion();
            if (this.animGroup.hasActions()) {
                this.animGroup.clearActions();
            }
        }
    }

    @Override
    public void dropPickup() {
        this.itemCrafter.dropItems(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos());
        super.dropPickup();
    }

    @Override
    public void interact(Player player) {
        super.interact(player);
        this.player = player;
        this.playerInventory = player.getPlayerInventory();
        this.itemCrafter.player = player;
        this.itemCrafter.playerInventory = this.playerInventory;
        if (this.itemCrafter != null) {
            if (this.itemCrafter.itemsToCollect.size > 0) {
                boolean collected = this.itemCrafter.collectItem();
                if (!collected) {
                    String message = Localization.get("cantCollectNotification");
                    this.world.gameScreen.hud.hudNotifications.newMessage(message, Color.RED);
                }
            } else {
                for (Behavior b : this.behaviorList) {
                    b.interact(player);
                }
            }
        } else {
            Gdx.app.error("MoonBase", "Recycler: no ItemCrafter behavior");
        }
    }

    @Override
    public void handleDroppedItem(PlayerInventory playerInventory, ItemStack stack) {
        if (!this.itemCrafter.isQueueFull() && stack.getId().equals("refined-scrap")) {
            playerInventory.consumeItem(stack, 1);
            this.handleDropItemAnimation();
            this.itemCrafter.playerInventory = playerInventory;
            this.itemCrafter.player = this.player;
            this.itemCrafter.addItemToBuildQueue(this.itemCrafter.getBuildableItem("metal"), false);
        }
    }

    private void handleDropItemAnimation() {
        this.animGroup.addAction(Actions.sequence((Action)Actions.scaleTo(1.05f, 0.95f, 0.08f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.15f), (Action)Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.0f, 0.98f, 0.13f), (Action)Actions.scaleTo(1.0f, 1.01f, 0.13f)))));
    }

    @Override
    public void destroy() {
        this.buildFinishedIcon.remove();
        super.destroy();
    }
}

