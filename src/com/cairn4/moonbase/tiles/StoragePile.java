/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MultiplyImage2;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.StorageColorOptions;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.IntValueBehavior;
import com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior;
import com.cairn4.moonbase.tiles.behaviors.StorageCrateCloseCallback;
import com.cairn4.moonbase.ui.StorageCrateColorCallback;
import com.cairn4.moonbase.ui.StorageUI;

public class StoragePile
extends HabitatModule {
    public ItemStorageBehavior itemStorageBehavior = new ItemStorageBehavior();
    public IntValueBehavior intValueBehavior;
    Image storageImage;
    private Image colorTint;
    private Image lid;
    private String normalSprite = "storage";
    private String openSprite = "storage-open";
    StorageUI storageUI;

    public StoragePile(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.itemStorageBehavior.baseModule = this;
        this.itemStorageBehavior.maxItems = 16;
        this.behaviorList.add(this.itemStorageBehavior);
        this.intValueBehavior = new IntValueBehavior();
        this.intValueBehavior.setId("intValueBehavior");
        this.behaviorList.add(this.intValueBehavior);
        this.componentGroup.setOrigin(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f);
        this.storageImage = new Image(world.gameScreen.skin.getDrawable("storage"));
        this.storageImage.setSize(TILE_SIZE * 0.7f, TILE_SIZE * 0.7f);
        this.storageImage.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f, 1);
        this.componentGroup.addActor(this.storageImage);
        this.colorTint = new MultiplyImage2(world.gameScreen.skin.getDrawable("storage-tint"));
        this.colorTint.setSize(this.storageImage.getWidth() - 1.0f, this.storageImage.getHeight() - 1.0f);
        this.colorTint.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.colorTint.setVisible(false);
        this.componentGroup.addActor(this.colorTint);
        this.lid = new Image(world.gameScreen.skin.getDrawable("storage-open-lid"));
        this.lid.setSize(102.0f, 32.0f);
        this.lid.setOrigin(51.0f, 3.0f);
        this.lid.setPosition(Tile.TILE_SIZE / 2.0f, 90.0f, 4);
        this.componentGroup.addActor(this.lid);
        this.lid.setVisible(false);
        this.updateBases();
    }

    @Override
    public void startBehaviors() {
        super.startBehaviors();
        this.itemStorageBehavior.start();
        this.setCrateColor(StorageColorOptions.colors[this.intValueBehavior.value], this.intValueBehavior.value);
    }

    public void setCrateColor(Color c, int index) {
        if (index == 0) {
            this.storageImage.setDrawable(this.world.gameScreen.skin.getDrawable("storage"));
            this.storageImage.setColor(c);
            this.colorTint.setVisible(false);
        } else {
            this.colorTint.setVisible(true);
            this.colorTint.setColor(c);
        }
        this.intValueBehavior.value = index;
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "storage-container";
    }

    @Override
    public void interact(Player player) {
        if (this.hasDisaster()) {
            this.fixDisaster();
        } else if (player.playerInventory.getEquippedItemId().equals("paintbrush")) {
            this.changeColor(player.getPaintColorIndex());
        } else {
            super.interact(player);
            this.openAnim(player);
        }
    }

    private void finishOpenAnim(Player player) {
        this.storageUI = new StorageUI(this.world.gameScreen, this.itemStorageBehavior, player.playerInventory);
        this.storageUI.closeCallback = new StorageCrateCloseCallback(){

            @Override
            public void closeAnim() {
                StoragePile.this.closeLidAnim();
            }
        };
        this.storageUI.storageCrateColorCallback = new StorageCrateColorCallback(){

            @Override
            public void setColor(Color c, int index) {
                StoragePile.this.setCrateColor(c, index);
            }

            @Override
            public int getColorIndex() {
                return StoragePile.this.intValueBehavior.value;
            }
        };
        this.world.gameScreen.showMenu(this.storageUI);
        this.storageUI.updateColorSwatch();
    }

    public void openAnim(final Player player) {
        this.componentGroup.addAction(Actions.sequence((Action)Actions.scaleTo(0.9f, 0.9f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.1f)));
        Audio.getInstance().playSound("sounds/storage-open.ogg", 1.2f, 0.8f);
        this.storageImage.setDrawable(this.world.gameScreen.skin.getDrawable(this.openSprite));
        this.lid.clearActions();
        this.lid.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.scaleTo(1.0f, -1.5f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.12f, Interpolation.sineOut), (Action)Actions.delay(0.05f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                StoragePile.this.finishOpenAnim(player);
            }
        })));
    }

    public void closeLidAnim() {
        Audio.getInstance().playSound("sounds/storage-close.ogg", 1.25f, 0.8f);
        this.lid.clearActions();
        this.lid.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.scaleTo(1.0f, 1.0f), (Action)Actions.scaleTo(1.0f, -1.5f, 0.12f), (Action)Actions.visible(false), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                StoragePile.this.storageImage.setDrawable(StoragePile.this.world.gameScreen.skin.getDrawable(StoragePile.this.normalSprite));
                StoragePile.this.componentGroup.addAction(Actions.sequence((Action)Actions.scaleTo(0.98f, 0.98f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.1f)));
            }
        })));
    }

    @Override
    public void dropPickup() {
        for (ItemStack stack : this.itemStorageBehavior.getItemList()) {
            for (int i = 0; i < stack.getAmount(); ++i) {
                new ItemPickup(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos(), stack.item);
            }
        }
        super.dropPickup();
    }
}

