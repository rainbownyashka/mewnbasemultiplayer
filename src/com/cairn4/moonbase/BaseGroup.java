/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.CommsTower;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.BaseResourceStorageBehavior;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.ui.Localization;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BaseGroup {
    public ArrayList<BaseModule> baseModuleList = new ArrayList();
    private int numBaseParts = 0;
    private float totalPowerGen;
    private float totalPowerDraw;
    private float totalPowerStored;
    private float totalMaxPowerStorage;
    private float totalWaterGen;
    private float totalWaterDraw;
    private float totalWaterStored;
    private float totalMaxWaterStorage;
    private float totalOxygenGen;
    private float totalOxygenDraw;
    private float totalOxygenStored;
    private float totalMaxOxygenStorage;
    private float totalFuelGen;
    private float totalFuelDraw;
    private float totalFuelStored;
    private float totalMaxFuelStorage;
    private DecimalFormat df;
    private DecimalFormat df_tenth;
    private String baseInfoText;
    ArrayList<CommsTower> commsTowerList = new ArrayList();

    public float getTotalPowerGen() {
        return this.totalPowerGen;
    }

    public float getTotalPowerDraw() {
        return this.totalPowerDraw;
    }

    public float getTotalPowerStored() {
        return this.totalPowerStored;
    }

    public float getTotalMaxPowerStored() {
        return this.totalMaxPowerStorage;
    }

    public float getTotalWaterGen() {
        return this.totalWaterGen;
    }

    public float getTotalWaterDraw() {
        return this.totalWaterDraw;
    }

    public float getTotalWaterStored() {
        return this.totalWaterStored;
    }

    public float getTotalMaxWaterStored() {
        return this.totalMaxWaterStorage;
    }

    public float getTotalOxygenGen() {
        return this.totalOxygenGen;
    }

    public float getTotalOxygenDraw() {
        return this.totalOxygenDraw;
    }

    public float getTotalOxygenStored() {
        return this.totalOxygenStored;
    }

    public float getTotalMaxOxygenStored() {
        return this.totalMaxOxygenStorage;
    }

    public float getTotalFuelGen() {
        return this.totalFuelGen;
    }

    public float getTotalFuelDraw() {
        return this.totalFuelDraw;
    }

    public float getTotalFuelStored() {
        return this.totalFuelStored;
    }

    public float getTotalMaxFuelStored() {
        return this.totalMaxFuelStorage;
    }

    public BaseGroup(ArrayList<Tile> tileList) {
        for (Tile t : tileList) {
            if (!(t instanceof BaseModule)) continue;
            this.baseModuleList.add((BaseModule)t);
        }
        this.baseModuleList = this.baseModuleList;
        this.df = new DecimalFormat("#");
        this.df_tenth = new DecimalFormat("#.#");
        this.assignBaseGroup();
        this.powerSortBaseModules();
    }

    public float addResource(BaseResources resourceType, float amount) {
        block0: for (BaseModule b : this.baseModuleList) {
            for (Behavior behavior : b.behaviorList) {
                if (!(behavior instanceof BaseResourceStorageBehavior)) continue;
                BaseResourceStorageBehavior brsb = (BaseResourceStorageBehavior)behavior;
                if (brsb.type != resourceType) continue block0;
                brsb.amount += amount;
                continue block0;
            }
        }
        return 0.0f;
    }

    public void update(float delta) {
        this.updateResource(BaseResources.power, delta);
        this.updateResource(BaseResources.water, delta);
        this.updateResource(BaseResources.oxygen, delta);
        this.updateResource(BaseResources.fuel, delta);
        this.redistributeResource(BaseResources.oxygen);
        this.redistributeResource(BaseResources.water);
        this.redistributeResource(BaseResources.fuel);
    }

    public void powerSortBaseModules() {
        Collections.sort(this.baseModuleList, new Comparator<BaseModule>(){

            @Override
            public int compare(BaseModule base1, BaseModule base2) {
                if (base1.powerPriority > base2.powerPriority) {
                    return -1;
                }
                if (base1.powerPriority < base2.powerPriority) {
                    return 1;
                }
                return 0;
            }
        });
        int i = 1;
        for (BaseModule b : this.baseModuleList) {
            ++i;
        }
    }

    public void updateResource(BaseResources type, float delta) {
        float totalDraw = 0.0f;
        Float totalStored = Float.valueOf(0.0f);
        Float totalMaxCapacity = Float.valueOf(0.0f);
        for (BaseModule baseModule : this.baseModuleList) {
            for (Behavior behavior : baseModule.behaviorList) {
                if (!(behavior instanceof BaseResourceStorageBehavior)) continue;
                BaseResourceStorageBehavior brsb = (BaseResourceStorageBehavior)behavior;
                if (brsb.type != type) continue;
                totalStored = Float.valueOf(totalStored.floatValue() + brsb.amount);
                totalMaxCapacity = Float.valueOf(totalMaxCapacity.floatValue() + brsb.capacity);
            }
        }
        float totalResourceGen = 0.0f;
        for (BaseModule b : this.baseModuleList) {
            totalResourceGen += b.getResourceGenRate(type) * delta;
        }
        float f = totalResourceGen;
        for (BaseModule b : this.baseModuleList) {
            if (b.getResourceDrawRate(type) * delta <= f) {
                b.toggleResource(type, true);
                f -= b.getResourceDrawRate(type) * delta;
            } else if (b.getResourceDrawRate(type) * delta <= totalStored.floatValue()) {
                b.toggleResource(type, true);
                this.consumeResource(b.getResourceDrawRate(type) * delta, type, totalStored);
            } else {
                this.consumeResource(b.getResourceDrawRate(type) * delta, type, totalStored);
                b.toggleResource(type, false);
                f = 0.0f;
                totalStored = Float.valueOf(0.0f);
            }
            totalDraw += b.getResourceDrawRate(type) * delta;
        }
        for (BaseModule b : this.baseModuleList) {
            for (Behavior behavior : b.behaviorList) {
                if (!(behavior instanceof BaseResourceStorageBehavior)) continue;
                BaseResourceStorageBehavior brsb = (BaseResourceStorageBehavior)behavior;
                if (brsb.type != type || !(brsb.amount < brsb.capacity)) continue;
                f = brsb.store(f);
            }
        }
        switch (type) {
            case power: {
                this.totalPowerDraw = totalDraw;
                this.totalPowerGen = totalResourceGen;
                this.totalPowerStored = totalStored.floatValue();
                this.totalMaxPowerStorage = totalMaxCapacity.floatValue();
                MoonBase.achievementAdapter.updatePowerGen(this.totalPowerGen / delta);
                break;
            }
            case water: {
                this.totalWaterDraw = totalDraw;
                this.totalWaterGen = totalResourceGen;
                this.totalWaterStored = totalStored.floatValue();
                this.totalMaxWaterStorage = totalMaxCapacity.floatValue();
                MoonBase.achievementAdapter.updateWaterStored(this.totalWaterStored);
                break;
            }
            case oxygen: {
                this.totalOxygenDraw = totalDraw;
                this.totalOxygenGen = totalResourceGen;
                this.totalOxygenStored = totalStored.floatValue();
                this.totalMaxOxygenStorage = totalMaxCapacity.floatValue();
                break;
            }
            case fuel: {
                this.totalFuelDraw = totalDraw;
                this.totalFuelGen = totalResourceGen;
                this.totalFuelStored = totalStored.floatValue();
                this.totalMaxFuelStorage = totalMaxCapacity.floatValue();
            }
        }
    }

    public void consumeResource(float neededLeft, BaseResources type, Float resourcePool) {
        float consumed = 0.0f;
        block0: for (BaseModule b : this.baseModuleList) {
            for (Behavior behavior : b.behaviorList) {
                if (!(behavior instanceof BaseResourceStorageBehavior)) continue;
                BaseResourceStorageBehavior brsb = (BaseResourceStorageBehavior)behavior;
                if (brsb.type != type || !(brsb.amount > 0.0f)) continue;
                if (brsb.amount > neededLeft) {
                    brsb.consume(neededLeft);
                    consumed += neededLeft;
                    neededLeft = 0.0f;
                    continue block0;
                }
                neededLeft -= brsb.amount;
                brsb.consume(brsb.amount);
            }
        }
        resourcePool = Float.valueOf(resourcePool.floatValue() - consumed);
    }

    private void assignBaseGroup() {
        for (int i = this.baseModuleList.size() - 1; i >= 0; --i) {
            if (this.baseModuleList.get(i) instanceof BaseModule) continue;
            this.baseModuleList.remove(i);
        }
        this.numBaseParts = 0;
        for (BaseModule b : this.baseModuleList) {
            if (!(b instanceof BaseModule)) continue;
            ++this.numBaseParts;
            b.setBaseGroup(this);
        }
    }

    public int getNumBaseParts() {
        return this.numBaseParts;
    }

    public String getBaseInfo() {
        String oxy = this.df.format(this.getTotalOxygenStored());
        this.baseInfoText = Localization.get("baseStatusLabel") + "\n";
        this.baseInfoText = this.baseInfoText + Localization.get("baseResource_oxygen") + ": " + oxy + " / " + this.df.format(this.getTotalMaxOxygenStored()) + "\n";
        this.baseInfoText = this.baseInfoText + Localization.get("baseResource_water") + ": " + this.df.format(this.getTotalWaterStored()) + "/" + this.df.format(this.getTotalMaxWaterStored()) + "\n";
        return this.baseInfoText;
    }

    private void redistributeResource(BaseResources type) {
        Float totalStored = Float.valueOf(0.0f);
        Float totalMaxCapacity = Float.valueOf(0.0f);
        for (BaseModule b : this.baseModuleList) {
            for (Behavior behavior : b.behaviorList) {
                if (!(behavior instanceof BaseResourceStorageBehavior)) continue;
                BaseResourceStorageBehavior brsb = (BaseResourceStorageBehavior)behavior;
                if (brsb.type != type) continue;
                totalStored = Float.valueOf(totalStored.floatValue() + brsb.amount);
                totalMaxCapacity = Float.valueOf(totalMaxCapacity.floatValue() + brsb.capacity);
            }
        }
        float fillPercentPerModule = totalStored.floatValue() / totalMaxCapacity.floatValue();
        for (BaseModule b : this.baseModuleList) {
            for (Behavior behavior : b.behaviorList) {
                if (!(behavior instanceof BaseResourceStorageBehavior)) continue;
                BaseResourceStorageBehavior brsb = (BaseResourceStorageBehavior)behavior;
                if (brsb.type != type) continue;
                brsb.amount = brsb.capacity * fillPercentPerModule;
                boolean on = brsb.amount > 0.001f;
                b.toggleResource(type, on);
                if (!(brsb.amount > brsb.capacity)) continue;
                brsb.amount = brsb.capacity;
            }
        }
    }

    public ArrayList<CommsTower> getAllWorkingCommsTowers() {
        this.commsTowerList.clear();
        for (BaseModule b : this.baseModuleList) {
            if (!(b instanceof CommsTower) || !b.hasPower) continue;
            this.commsTowerList.add((CommsTower)b);
        }
        return this.commsTowerList;
    }
}

