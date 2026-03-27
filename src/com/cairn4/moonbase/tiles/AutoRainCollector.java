/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.RainCollectorBehavior;

public class AutoRainCollector
extends BaseModule {
    private Image pumpImage;
    private Group waterGroup;
    public RainCollectorBehavior rainCollectorBehavior;
    private Image buildFinishedIcon;
    public Label debugLabel;
    protected Color lightOnColor = new Color(0.15f, 0.15f, 0.15f, 0.3f);

    public AutoRainCollector(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 2.0f;
        this.powerGenRate = 0.0f;
        this.waterGenRate = 0.0f;
        this.setupPhysics("rtg");
        this.updateBases();
        this.rainCollectorBehavior = new RainCollectorBehavior();
        this.rainCollectorBehavior.baseModule = this;
        this.rainCollectorBehavior.threshold = 60.0f;
        this.rainCollectorBehavior.maxItems = 3;
        this.rainCollectorBehavior.pumped = true;
        this.rainCollectorBehavior.waterGroup = this.waterGroup;
        this.behaviorList.add(this.rainCollectorBehavior);
        this.buildFinishedIcon = new Image(world.gameScreen.skin.getDrawable("crafting-notification"));
        this.buildFinishedIcon.setOrigin(4);
        this.buildFinishedIcon.setScale(0.3f);
        this.buildFinishedIcon.setPosition(this.getWorldXPos() + TILE_SIZE / 2.0f, this.getWorldYPos() + TILE_SIZE * 0.7f, 4);
        world.gameScreen.worldUiGroup.addActor(this.buildFinishedIcon);
        this.rainCollectorBehavior.setFinishedIcon(this.buildFinishedIcon);
        this.light.setColor(this.lightOnColor);
        this.debugLabel = new Label((CharSequence)"000", world.gameScreen.labelStyle);
        this.debugLabel.setFontScale(0.3f);
        this.group.addActor(this.debugLabel);
        this.debugLabel.setPosition(0.0f, -80.0f);
        this.debugLabel.setVisible(false);
    }

    @Override
    public void startBehaviors() {
        this.rainCollectorBehavior.start();
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("raincollector", this.world.gameScreen.mainGroup);
        this.waterGroup = new Group();
        this.group.addActor(this.waterGroup);
        this.waterGroup.setPosition(Tile.TILE_SIZE / 2.0f + 1.0f, 150.0f * SCALE - 14.0f, 1);
        Image waterImage = new Image(this.world.gameScreen.skin.getDrawable("raincollector-water"));
        waterImage.getColor().a = 0.65f;
        waterImage.setOrigin(1);
        waterImage.setScale(0.9f);
        waterImage.setPosition(0.0f, 13.0f, 1);
        this.waterGroup.addActor(waterImage);
        waterImage.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(0.85f, 0.85f, 1.5f, Interpolation.sine), (Action)Actions.scaleTo(0.95f, 0.95f, 1.5f, Interpolation.sine))));
        Image waterRipple = new Image(this.world.gameScreen.skin.getDrawable("raincollector-water-ripples"));
        waterRipple.getColor().a = 0.4f;
        waterRipple.setOrigin(1);
        waterRipple.setScale(0.9f);
        waterRipple.setPosition(0.0f, 12.0f, 1);
        this.waterGroup.addActor(waterRipple);
        this.pumpImage = new Image(this.world.gameScreen.skin.getDrawable("raincollector-pump"));
        this.pumpImage.setPosition(Tile.TILE_SIZE / 2.0f, -9.0f, 4);
        this.group.addActor(this.pumpImage);
        this.group.setUserObject(Float.valueOf(this.getYCenter() - 40.0f));
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "autoraincollector";
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
        for (Behavior b : this.behaviorList) {
            b.interact(player);
        }
    }

    @Override
    public void destroy() {
        this.buildFinishedIcon.remove();
        super.destroy();
    }
}

