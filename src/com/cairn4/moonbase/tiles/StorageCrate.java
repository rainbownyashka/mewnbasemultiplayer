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
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.MultiplyImage2;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.StorageColorOptions;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.IntValueBehavior;
import com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior;
import com.cairn4.moonbase.tiles.behaviors.StorageCrateCloseCallback;
import com.cairn4.moonbase.ui.StorageCrateColorCallback;
import com.cairn4.moonbase.ui.StorageUI;

public class StorageCrate
extends BaseModule {
    private static float COLOR_TINT_ALPHA = 1.0f;
    public ItemStorageBehavior itemStorageBehavior;
    public IntValueBehavior intValueBehavior;
    StorageUI storageUI;
    private Image colorTint;
    private Image lid;
    private String normalSprite = "storage";
    private String openSprite = "storage-open";

    public StorageCrate(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.addBehaviors();
        this.setupPhysics("storage");
        MoonBase.debug("Listing items in StorageCrate:");
        for (ItemStack stack : this.itemStorageBehavior.getItemList()) {
            MoonBase.debug("-- " + stack.getId());
        }
        this.updateBases();
    }

    protected void addBehaviors() {
        this.itemStorageBehavior = new ItemStorageBehavior();
        this.itemStorageBehavior.baseModule = this;
        this.itemStorageBehavior.maxItems = 8;
        this.behaviorList.add(this.itemStorageBehavior);
        this.intValueBehavior = new IntValueBehavior();
        this.behaviorList.add(this.intValueBehavior);
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "storage-crate";
    }

    @Override
    public void startBehaviors() {
        this.itemStorageBehavior.start();
        this.setCrateColor(StorageColorOptions.colors[this.intValueBehavior.value], this.intValueBehavior.value);
    }

    @Override
    public void setupLight() {
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("storage");
        this.group.setOrigin(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f);
        this.image.setOrigin(1);
        this.image.setSize(100.0f, 100.0f);
        this.image.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.colorTint = new MultiplyImage2(this.world.gameScreen.skin.getDrawable("storage-tint"));
        this.colorTint.setSize(99.0f, 99.0f);
        this.colorTint.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.colorTint.setVisible(false);
        this.group.addActor(this.colorTint);
        this.lid = new Image(this.world.gameScreen.skin.getDrawable("storage-open-lid"));
        this.lid.setSize(102.0f, 32.0f);
        this.lid.setOrigin(51.0f, 3.0f);
        this.lid.setPosition(Tile.TILE_SIZE / 2.0f, 90.0f, 4);
        this.group.addActor(this.lid);
        this.lid.setVisible(false);
    }

    @Override
    public void interact(Player player) {
        if (this.hasDisaster()) {
            this.fixDisaster();
        } else {
            this.openAnim(player);
            super.interact(player);
        }
    }

    private void finishOpenAnim(Player player) {
        this.storageUI = new StorageUI(this.world.gameScreen, this.itemStorageBehavior, player.playerInventory);
        this.storageUI.closeCallback = new StorageCrateCloseCallback(){

            @Override
            public void closeAnim() {
                StorageCrate.this.closeLidAnim();
            }
        };
        this.storageUI.storageCrateColorCallback = new StorageCrateColorCallback(){

            @Override
            public void setColor(Color c, int index) {
                StorageCrate.this.setCrateColor(c, index);
            }

            @Override
            public int getColorIndex() {
                return StorageCrate.this.intValueBehavior.value;
            }
        };
        this.world.gameScreen.showMenu(this.storageUI);
        this.storageUI.updateColorSwatch();
    }

    public void setCrateColor(Color c, int index) {
        if (index == 0) {
            this.image.setDrawable(this.world.gameScreen.skin.getDrawable("storage"));
            this.image.setColor(c);
            this.colorTint.setVisible(false);
        } else {
            this.colorTint.setVisible(true);
            this.colorTint.setColor(c);
        }
        this.intValueBehavior.value = index;
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

    public void openAnim(final Player player) {
        this.group.addAction(Actions.sequence((Action)Actions.scaleTo(0.9f, 0.9f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.1f)));
        Audio.getInstance().playSound("sounds/storage-open.ogg", 1.2f, 0.8f);
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable(this.openSprite));
        this.lid.clearActions();
        this.lid.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.scaleTo(1.0f, -1.5f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.12f, Interpolation.sineOut), (Action)Actions.delay(0.05f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                StorageCrate.this.finishOpenAnim(player);
            }
        })));
    }

    public void closeLidAnim() {
        Audio.getInstance().playSound("sounds/storage-close.ogg", 1.25f, 0.8f);
        this.lid.clearActions();
        this.lid.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.scaleTo(1.0f, 1.0f), (Action)Actions.scaleTo(1.0f, -1.5f, 0.12f), (Action)Actions.visible(false), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                StorageCrate.this.image.setDrawable(StorageCrate.this.world.gameScreen.skin.getDrawable(StorageCrate.this.normalSprite));
                StorageCrate.this.group.addAction(Actions.sequence((Action)Actions.scaleTo(0.98f, 0.98f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.1f)));
            }
        })));
    }
}

