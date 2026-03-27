/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.BaseGroup;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;

public class RainCollectorBehavior
implements Behavior {
    public BaseModule baseModule;
    public Image buildFinishedIcon;
    private float waterScale;
    public boolean pumped;
    public float stored;
    public float threshold;
    public int storedItems;
    public int maxItems;
    public Group waterGroup;
    private float transferGenRate = 0.0f;
    private static final float RAIN_COLLECT_MULTIPLIER = 2.4f;
    public float pumpedWaterGenRate = 5.0f;

    public void setFinishedIcon(Image i) {
        this.buildFinishedIcon = i;
        this.buildFinishedIcon.setVisible(false);
    }

    @Override
    public void setLoaded(boolean loaded) {
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
        if (this.storedItems > 0) {
            this.showFinishedIcon(true);
        }
    }

    @Override
    public void update(float delta) {
        if (this.baseModule != null) {
            if (this.pumped) {
                this.updatePumped(delta);
            } else {
                this.updateNormal(delta);
            }
            this.scaleWaterFx();
            this.waterFxOpacity();
        }
    }

    private void handleRemainingWater() {
        if (this.stored > this.threshold) {
            ++this.storedItems;
            this.stored = 0.0f;
            this.showFinishedIcon(true);
        }
    }

    private void updatePumped(float delta) {
        this.transferGenRate = 0.0f;
        float newWater = this.baseModule.world.weatherManager.getRainRate() * 2.4f;
        newWater += this.threshold / 720.0f;
        if (this.storedItems < this.maxItems) {
            this.stored += newWater * delta;
        }
        if (this.baseModule.hasPower) {
            float waterStorageAvailable = this.getWaterStorageAvailable();
            if (waterStorageAvailable > 0.0f && (this.stored > 0.0f || this.storedItems > 0)) {
                float totalStored = (float)this.storedItems * this.threshold;
                if (waterStorageAvailable > this.pumpedWaterGenRate * delta) {
                    this.baseModule.setWaterGenRate(this.pumpedWaterGenRate);
                    this.stored -= this.pumpedWaterGenRate * delta;
                    if (this.stored <= 0.0f && this.storedItems > 0) {
                        --this.storedItems;
                        this.stored += this.threshold;
                        if (this.storedItems < 1) {
                            this.showFinishedIcon(false);
                        }
                    }
                } else {
                    this.baseModule.setWaterGenRate(0.0f);
                }
            } else {
                this.baseModule.setWaterGenRate(0.0f);
            }
        } else {
            this.baseModule.setWaterGenRate(0.0f);
        }
        this.handleRemainingWater();
    }

    private void updateNormal(float delta) {
        if (this.storedItems < this.maxItems) {
            float newWater = this.baseModule.world.weatherManager.getRainRate() * delta * 2.4f;
            if ((newWater += delta * (this.threshold / 720.0f)) > 0.0f) {
                this.stored += newWater;
                if (this.stored > this.threshold) {
                    ++this.storedItems;
                    this.stored = 0.0f;
                    this.showFinishedIcon(true);
                }
            }
        }
    }

    private void toggleWaterFx(boolean on) {
        if (this.waterGroup != null) {
            if (this.pumped && this.storedItems == 0) {
                on = false;
            }
            this.waterGroup.setVisible(on);
        }
    }

    private void waterFxOpacity() {
        this.waterGroup.getColor().a = this.storedItems < 1 ? MathUtils.clamp(this.stored / this.threshold, 0.0f, 1.0f) : 1.0f;
    }

    private void scaleWaterFx() {
        if (this.waterGroup != null) {
            this.waterScale = this.storedItems < 1 ? (float)MathUtils.clamp((double)(this.stored / this.threshold), 0.25, 1.0) : 1.0f;
            this.waterGroup.setScale(this.waterScale);
        }
    }

    private float getWaterStorageAvailable() {
        BaseGroup bg = this.baseModule.getBaseGroup();
        if (bg != null && bg.getTotalMaxWaterStored() > 0.0f) {
            return bg.getTotalMaxWaterStored() - bg.getTotalWaterStored();
        }
        return 0.0f;
    }

    @Override
    public void interact(Player player) {
        this.dropItems();
        this.showFinishedIcon(false);
    }

    public void dropItems() {
        while (this.storedItems > 0) {
            ItemPickup pickup = new ItemPickup(this.baseModule.world, this.baseModule.chunk, this.baseModule.getWorldXPos(), this.baseModule.getWorldYPos(), ItemFactory.getInstance().createItem("drink-water"));
            --this.storedItems;
        }
    }

    @Override
    public void onDestroy() {
        this.dropItems();
    }

    @Override
    public BehaviorData getData() {
        BehaviorData bd = new BehaviorData();
        bd.behaviorClass = this.getClass().getName();
        bd.properties.put("stored", Float.valueOf(this.stored));
        bd.properties.put("storedItems", this.storedItems);
        return bd;
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
}

