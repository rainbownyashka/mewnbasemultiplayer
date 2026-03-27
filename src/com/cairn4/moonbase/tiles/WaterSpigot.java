/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.Conduit;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.Localization;

public class WaterSpigot
extends Conduit {
    private static final Color COLOR_ON = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Color COLOR_OFF = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    Group spigotGroup;
    Image tapImage;

    public WaterSpigot(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 0.0f;
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("conduit-builder", this.world.gameScreen.floorGroup);
        this.caps = new Group();
        this.group.addActor(this.caps);
        this.spigotGroup = new Group();
        this.world.gameScreen.mainGroup.addActor(this.spigotGroup);
        this.spigotGroup.setPosition(this.group.getX(), this.group.getY());
        this.spigotGroup.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + TILE_SIZE / 2.0f + 10.0f));
        this.tapImage = new Image(this.world.gameScreen.skin.getDrawable("waterspigot"));
        this.tapImage.setSize(Tile.TILE_SIZE * 0.8f, Tile.TILE_SIZE * 0.8f);
        this.tapImage.setOrigin(this.tapImage.getWidth() / 2.0f, 20.0f);
        this.tapImage.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.spigotGroup.addActor(this.tapImage);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "waterspigot";
    }

    @Override
    public void setupLight() {
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.getBaseGroup().getTotalWaterStored() >= 60.0f) {
            this.tapImage.setColor(COLOR_ON);
        } else {
            this.tapImage.setColor(COLOR_OFF);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        this.spigotGroup.remove();
    }

    @Override
    public boolean canInteract(Player player) {
        return this.getBaseGroup().getTotalWaterStored() >= 60.0f;
    }

    @Override
    public void interact(Player player) {
        super.interact(player);
        if (this.getBaseGroup().getTotalWaterStored() >= 60.0f) {
            Gdx.app.debug("MewnBase", "WaterSpigot: base had " + this.getBaseGroup().getTotalWaterStored() + " water");
            if (player.playerInventory.hasSlotAvailable("drink-water")) {
                this.getBaseGroup().consumeResource(60.0f, BaseResources.water, Float.valueOf(this.getBaseGroup().getTotalWaterStored()));
                player.collect(ItemFactory.getInstance().createItem("drink-water"));
                this.getWaterAnim();
            } else {
                this.world.gameScreen.hud.hudNotifications.newMessage("drinkable-water", Localization.get("cantCollectNotification"));
            }
        }
    }

    private void getWaterAnim() {
        this.tapImage.addAction(Actions.sequence((Action)Actions.scaleTo(1.05f, 0.95f, 0.08f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.15f)));
        float pitch = MathUtils.random(1.0f, 1.2f);
        Audio.getInstance().playSound("sounds/refillWaterSupply.ogg", 0.8f, pitch);
    }

    @Override
    public void setPower(boolean on) {
    }

    @Override
    protected void setConduitImage(int num) {
        String suffix = "";
        if (num == 0) {
            this.image.setVisible(false);
            num = 15;
        } else {
            this.image.setVisible(true);
        }
        if (num == 1 || num == 2 || num == 4 || num == 8) {
            suffix = "-noplug";
        }
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable("test/conduit-" + num + suffix));
    }
}

