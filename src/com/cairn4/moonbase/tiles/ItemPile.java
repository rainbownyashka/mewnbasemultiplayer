/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.ItemPileBehavior;
import com.cairn4.moonbase.ui.Localization;

public class ItemPile
extends Tile {
    ItemStack itemStack;
    private float riskTimer = 0.0f;
    private float riskTime = 1.0f;
    private static final float riskOfLoss = 0.02f;
    private static final float baseRotation = -18.0f;
    private float baseXPos = 0.0f;
    private float riskAnimOffset = MathUtils.random(0, 3);
    private Label quantityLabel;
    public ItemPileBehavior itemPileBehavior;
    private static final Action addAction = Actions.sequence((Action)Actions.scaleTo(1.05f, 0.95f, 0.08f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.15f));
    private static final Action removeAction = Actions.sequence((Action)Actions.scaleTo(0.95f, 0.95f, 0.08f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.15f));

    public ItemPile(World world, Chunk chunk, int x, int y, ItemStack itemStack) {
        super(world, chunk, x, y);
        this.type = Tile.types.pile;
        this.itemStack = itemStack;
        this.itemPileBehavior = new ItemPileBehavior();
        this.itemPileBehavior.itemPile = this;
        this.itemPileBehavior.itemId = this.itemStack.getId();
        this.itemPileBehavior.itemDurability = this.itemStack.item.durability;
        this.itemPileBehavior.maxDurability = this.itemStack.item.maxDurability;
        this.behaviorList.add(this.itemPileBehavior);
        this.createPileDrawable();
        this.updateLabel();
    }

    @Override
    public void startBehaviors() {
        this.itemStack.item.durability = this.itemPileBehavior.itemDurability;
        if (this.itemStack.item.durability == 0 && this.itemStack.item.maxDurability != 0) {
            this.itemStack.item.durability = this.itemStack.item.maxDurability;
        }
        this.itemStack.item.maxDurability = this.itemPileBehavior.maxDurability;
    }

    protected void createPileDrawable() {
        String spriteName = "cargo";
        if (this.itemStack != null) {
            spriteName = ItemFactory.getItemSprite(this.itemStack.getId());
        }
        super.createDrawables(spriteName, this.world.gameScreen.floorGroup);
        this.image.setSize(Tile.TILE_SIZE * 0.7f, Tile.TILE_SIZE * 0.7f);
        this.image.setOrigin(1);
        this.image.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.image.setRotation(-18.0f);
        this.image.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.baseXPos = this.image.getX();
        this.quantityLabel = new Label((CharSequence)"100", this.world.gameScreen.labelStyle);
        this.quantityLabel.setFontScale(0.4f);
        this.quantityLabel.setAlignment(20, 20);
        this.quantityLabel.setColor(1.0f, 1.0f, 1.0f, 0.4f);
        this.quantityLabel.setPosition(this.group.getX() + Tile.TILE_SIZE - 20.0f, this.group.getY() + 20.0f, 20);
        this.world.gameScreen.topGroup.addActor(this.quantityLabel);
    }

    @Override
    public void destroy() {
        super.destroy();
        this.quantityLabel.remove();
    }

    @Override
    public boolean canInteract(Player player) {
        int spaceAvailable = player.playerInventory.getSpaceAvailableFor(this.getItemId());
        if (spaceAvailable > 0) {
            return true;
        }
        this.world.gameScreen.hud.hudNotifications.newMessage(Localization.get("inventory_full"), Color.RED);
        return false;
    }

    @Override
    public void interact(Player player) {
    }

    @Override
    protected void finishInteraction(Player player) {
        if (this.canInteract(player)) {
            int spaceAvailable = player.playerInventory.getSpaceAvailableFor(this.getItemId());
            int amount = Math.min(this.itemStack.getAmount(), spaceAvailable);
            ItemStack newStack = new ItemStack(this.getItemId(), amount);
            if (newStack.item.durability != 0) {
                MoonBase.debug("Setting pile durability to : " + this.itemPileBehavior.itemDurability);
                newStack.item.durability = this.itemPileBehavior.itemDurability;
            }
            this.getItem().remove(amount);
            player.collect(newStack);
            this.updateLabel();
            this.animateRemove();
            if (this.itemStack.getAmount() == 0) {
                this.readyToRemove = true;
            }
        }
        super.finishInteraction(player);
    }

    public boolean addTo(int amount) {
        if (this.itemStack.getAmount() < this.itemStack.getMaxCarry()) {
            this.itemStack.add(amount);
            this.animateAdd();
            this.updateLabel();
            return true;
        }
        return false;
    }

    public boolean addTo(ItemStack newStack) {
        if (this.itemStack.getId().equals(newStack.getId())) {
            this.itemStack.add(newStack.getAmount());
            this.animateAdd();
            this.updateLabel();
            return true;
        }
        return false;
    }

    private void updateLabel() {
        this.quantityLabel.setVisible(this.itemStack.getAmount() > 1);
        this.quantityLabel.setText(this.itemStack.getAmount());
    }

    private void animateAdd() {
        this.image.addAction(addAction);
    }

    private void animateRemove() {
        this.image.addAction(removeAction);
    }

    public String getItemId() {
        return this.itemStack.getId();
    }

    public ItemStack getItem() {
        return this.itemStack;
    }

    // Called from network event handler to reflect that a remote client picked up one item
    public void markPickedUpByNetwork(String itemId) {
        try {
            if (this.itemStack == null) {
                return;
            }
            if (!this.itemStack.getId().equals(itemId)) {
                return;
            }
            if (this.itemStack.getAmount() <= 0) {
                return;
            }
            this.itemStack.remove(1);
            this.updateLabel();
            this.animateRemove();
            if (this.itemStack.getAmount() <= 0) {
                if (this.image != null) {
                    this.image.setVisible(false);
                }
                this.readyToRemove = true;
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.blowAwayUpdate(delta);
    }

    private void blowAwayUpdate(float delta) {
        if (this.world.weatherManager.getCurrentData().riskToItemPiles && this.itemStack.getAmount() > 0) {
            this.riskAnimation(delta);
            this.riskTimer += delta;
            if (this.riskTimer > this.riskTime) {
                this.riskTimer = 0.0f;
                float r = MathUtils.random();
                if (r < 0.02f) {
                    this.itemStack.remove(1);
                    this.updateLabel();
                    this.blowAwayParticleFX();
                    if (this.itemStack.getAmount() <= 0) {
                        this.image.setVisible(false);
                        this.blowAwayAnim();
                        this.readyToRemove = true;
                    }
                }
            }
        }
    }

    private void blowAwayParticleFX() {
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/itemBlowAway.p", ParticleEffect.class));
        ParticleActor dirtFx = new ParticleActor(p, true);
        dirtFx.setPosition(this.getXCenter(), this.getYCenter());
        this.world.gameScreen.topGroup.addActor(dirtFx);
        dirtFx.pfx.start();
    }

    private void riskAnimation(float delta) {
        float v1 = MathUtils.sin((this.riskTimer + -1.52f + this.riskAnimOffset) * 3.0f);
        float v2 = MathUtils.sin(this.riskTimer * 24.0f) * (2.0f * v1);
        this.image.setRotation(-18.0f + v2);
        this.image.setX(this.baseXPos + v1);
    }

    private void blowAwayAnim() {
        Image i = new Image(this.image.getDrawable());
        i.setSize(this.image.getWidth(), this.image.getHeight());
        i.setOrigin(this.image.getOriginX(), this.image.getOriginY());
        i.setPosition(this.getXCenter(), this.getYCenter(), 1);
        i.setRotation(this.image.getRotation());
        this.world.gameScreen.topGroup.addActor(i);
        i.addAction(Actions.sequence((Action)Actions.parallel((Action)Actions.moveBy(100.0f, 20.0f, 0.5f, Interpolation.sineIn), (Action)Actions.rotateBy(-40.0f, 0.5f, Interpolation.sineIn), (Action)Actions.scaleTo(0.25f, 0.25f, 0.5f, Interpolation.sineIn)), (Action)Actions.removeActor()));
    }

    @Override
    public boolean blocksWind() {
        return false;
    }
}

