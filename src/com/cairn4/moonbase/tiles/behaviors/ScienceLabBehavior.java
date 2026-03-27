/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.worlddata.BehaviorData;
import java.util.Observable;

public class ScienceLabBehavior
extends Observable
implements Behavior {
    public boolean loaded = false;
    public boolean researching = false;
    public int readyToCollect = -1;
    public BaseModule baseModule;
    private Image buildFinishedIcon;
    public float timer;
    public static final float researchTime = 120.0f;

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = true;
    }

    @Override
    public boolean isLoaded() {
        return false;
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
        if (this.readyToCollect != -1) {
            this.showFinishedIcon(true);
        }
    }

    @Override
    public void update(float delta) {
        if (this.researching) {
            this.timer += delta;
            if (this.timer > 120.0f) {
                this.showFinishedIcon(true);
                this.researching = false;
                this.readyToCollect = MathUtils.random(0, 17);
                this.setChanged();
                this.notifyObservers("done");
                if (this.baseModule.world.player.playerStatus.baseGroupsInRange.contains(this.baseModule.getBaseGroup())) {
                    if (this.baseModule.world.gameScreen.hud.hudNotifications != null) {
                        this.baseModule.world.gameScreen.hud.hudNotifications.newMessage("artifact", Localization.get("research_completeNotification"));
                    }
                } else {
                    Gdx.app.log("MewnBase", "ScienceLabBehavior: too far awawy to notify.");
                }
            }
        }
    }

    @Override
    public void interact(Player player) {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public BehaviorData getData() {
        BehaviorData bd = new BehaviorData();
        bd.behaviorClass = this.getClass().getName();
        bd.properties.put("researching", this.researching);
        bd.properties.put("timer", Float.valueOf(this.timer));
        bd.properties.put("readyToCollect", this.readyToCollect);
        return bd;
    }

    public void startResearch() {
        this.timer = 0.0f;
        this.researching = true;
        System.out.println("Starting Research in Behavior");
        this.setChanged();
        this.notifyObservers("startResearch");
    }

    public float getProgress() {
        float p = 0.0f;
        if (this.researching) {
            p = this.timer / 120.0f;
        }
        return p;
    }

    public void setFinishedIcon(Image i) {
        this.buildFinishedIcon = i;
        this.buildFinishedIcon.setVisible(false);
    }

    public void showFinishedIcon(boolean visible) {
        if (this.buildFinishedIcon != null) {
            this.buildFinishedIcon.clearActions();
            this.buildFinishedIcon.setVisible(visible);
            if (visible) {
                this.buildFinishedIcon.addAction(Actions.sequence((Action)Actions.scaleTo(0.0f, 0.0f), (Action)Actions.alpha(0.0f), (Action)Actions.parallel((Action)Actions.fadeIn(0.25f), (Action)Actions.scaleTo(0.32f, 0.32f, 0.25f, Interpolation.sineOut)), (Action)Actions.forever(Actions.sequence((Action)Actions.scaleTo(0.3f, 0.3f, 0.5f, Interpolation.sine), (Action)Actions.scaleTo(0.32f, 0.32f, 0.5f, Interpolation.sine)))));
            }
        }
    }

    public void collect() {
        if (this.readyToCollect != -1) {
            this.baseModule.world.player.discoverResearchObject(this.readyToCollect);
            this.readyToCollect = -1;
            this.showFinishedIcon(false);
        }
    }

    public void dropItems(World world, Chunk chunk, float worldXPos, float worldYPos) {
        if (this.researching) {
            new ItemPickup(world, chunk, worldXPos, worldYPos, ItemFactory.getInstance().createItem("researchObject"));
        }
    }
}

