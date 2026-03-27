/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class GreenHouseSpriteBehavior
implements Behavior {
    public boolean loaded = false;
    private GameScreen gameScreen;
    public ItemCrafter itemCrafter;
    Group parentGroup;
    Image plant1;
    Image plant2;
    Image plant3;
    private float timer;
    public GrowStates growState;
    private static float speed = 1.5707964f;
    private static float amp = 4.5f;

    public GreenHouseSpriteBehavior(GameScreen gameScreen, ItemCrafter itemCrafter, Group group) {
        this.gameScreen = gameScreen;
        this.itemCrafter = itemCrafter;
        this.parentGroup = group;
        this.plant1 = new Image(gameScreen.skin.getDrawable("greenhouse-plant-start"));
        this.plant1.setSize(this.plant1.getWidth() * 0.55f, this.plant1.getHeight() * 0.55f);
        this.plant1.setPosition(0.0f, 2.0f, 1);
        this.plant1.setOrigin(4);
        this.plant1.setOriginY(10.0f);
        this.parentGroup.addActor(this.plant1);
        this.plant1.setVisible(false);
        this.plant2 = new Image(gameScreen.skin.getDrawable("greenhouse-plant-middle"));
        this.plant2.setSize(Tile.TILE_SIZE * 0.55f, Tile.TILE_SIZE * 0.55f);
        this.plant2.setPosition(0.0f, 4.0f, 1);
        this.plant2.setOrigin(4);
        this.parentGroup.addActor(this.plant2);
        this.plant2.setOriginY(10.0f);
        this.plant2.setVisible(false);
        this.plant3 = new Image(gameScreen.skin.getDrawable("greenhouse-plant-full"));
        this.plant3.setSize(Tile.TILE_SIZE * 0.6f, Tile.TILE_SIZE * 0.6f);
        this.plant3.setPosition(0.0f, 6.0f, 1);
        this.plant3.setOrigin(4);
        this.parentGroup.addActor(this.plant3);
        this.plant3.setOriginY(10.0f);
        this.plant3.setVisible(false);
        this.setGrowState(GrowStates.idle);
    }

    @Override
    public BehaviorData getData() {
        BehaviorData bd = new BehaviorData();
        bd.behaviorClass = this.getClass().getName();
        bd.properties.put("growState", (Object)this.growState);
        return bd;
    }

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public void setId(String s) {
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void start() {
        if (this.itemCrafter.itemsToCollect.size == 0 && this.growState == GrowStates.readyToHarvest) {
            Gdx.app.log("MewnBase", "GreenhouseSpriteBehavior: ReadyToHarvest, but no items to collect, set to idle");
            this.setGrowState(GrowStates.idle);
        } else {
            this.setGrowState(this.growState);
        }
    }

    @Override
    public void update(float delta) {
        if (this.itemCrafter.isBuilding()) {
            this.timer += delta;
            float a = this.itemCrafter.getBuildProgress();
            if (this.growState == GrowStates.idle) {
                this.setGrowState(GrowStates.sprout);
            } else if (a > 0.03f && this.growState == GrowStates.sprout) {
                this.setGrowState(GrowStates.growing);
                this.plant1.setVisible(false);
            }
            float windSpeed = this.gameScreen.world.weatherManager.getWindSpeed();
            this.plant2.setRotation(MathUtils.sin(this.timer * speed) * amp * windSpeed);
            if (this.timer >= 4.0f) {
                this.timer = 0.0f;
            }
        } else if (this.itemCrafter.itemsToCollect.size > 0 && this.growState == GrowStates.growing) {
            this.setGrowState(GrowStates.readyToHarvest);
            this.plant2.setVisible(false);
            this.plant2.setRotation(0.0f);
        } else if (this.itemCrafter.itemsToCollect.size == 0 && this.growState == GrowStates.readyToHarvest) {
            this.setGrowState(GrowStates.idle);
            this.plant2.setRotation(0.0f);
        } else if (this.growState != GrowStates.idle && this.growState != GrowStates.readyToHarvest) {
            this.setGrowState(GrowStates.idle);
        }
    }

    @Override
    public void interact(Player player) {
    }

    @Override
    public void onDestroy() {
    }

    private void setGrowState(GrowStates newState) {
        this.growState = newState;
        Image sprite = null;
        String drawable = "";
        ItemData growItemData = null;
        if (this.itemCrafter.buildQueue.size > 0) {
            growItemData = ItemFactory.getItemData(this.itemCrafter.buildQueue.get((int)0).id);
        }
        ItemStack finishedItem = null;
        if (this.itemCrafter.itemsToCollect.size > 0) {
            finishedItem = this.itemCrafter.itemsToCollect.get(0);
        }
        switch (this.growState) {
            case idle: {
                sprite = null;
                break;
            }
            case sprout: {
                if (growItemData != null) {
                    drawable = growItemData.growSprites.get(0);
                    this.plant1.setDrawable(this.gameScreen.skin.getDrawable(drawable));
                } else {
                    Gdx.app.error("MewnBase", "GreenHouseSpriteBehavior: Grow state is sprout, but no item in build queue");
                }
                sprite = this.plant1;
                break;
            }
            case growing: {
                this.timer = MathUtils.random(0, 2);
                if (growItemData != null) {
                    drawable = growItemData.growSprites.get(1);
                    this.plant2.setDrawable(this.gameScreen.skin.getDrawable(drawable));
                } else {
                    Gdx.app.error("MewnBase", "GreenHouseSpriteBehavior: Grow state is growing, but no item in build queue");
                }
                sprite = this.plant2;
                break;
            }
            case readyToHarvest: {
                if (this.itemCrafter.itemsToCollect.size > 0) {
                    drawable = finishedItem.item.getData().growSprites.get(2);
                    this.plant3.setDrawable(this.gameScreen.skin.getDrawable(drawable));
                }
                sprite = this.plant3;
            }
        }
        this.stateChangeAnim(sprite);
    }

    private void stateChangeAnim(Image image) {
        if (image != null) {
            image.setVisible(true);
            image.addAction(Actions.sequence((Action)Actions.scaleTo(0.5f, 0.5f), (Action)Actions.scaleTo(0.9f, 1.2f, 0.1f, Interpolation.sineOut), (Action)Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.sine)));
        } else {
            this.plant1.setVisible(false);
            this.plant2.setVisible(false);
            this.plant3.setVisible(false);
        }
    }

    public static enum GrowStates {
        idle,
        sprout,
        growing,
        readyToHarvest;

    }
}

