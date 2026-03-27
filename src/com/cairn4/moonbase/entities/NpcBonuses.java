/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.cairn4.moonbase.ui.Localization;
import java.util.ArrayList;

public class NpcBonuses {
    private static NpcBonuses instance;
    private ArrayList<bonusTypes> activeBonuses = new ArrayList();

    private NpcBonuses() {
    }

    public static void init() {
        instance = new NpcBonuses();
    }

    public static NpcBonuses getInstance() {
        if (instance == null) {
            NpcBonuses.init();
        }
        return instance;
    }

    public void loadBonus(bonusTypes b) {
        this.activeBonuses.add(b);
    }

    public void addBonus(bonusTypes rewardBonus) {
        if (!this.activeBonuses.contains((Object)rewardBonus)) {
            this.activeBonuses.add(rewardBonus);
            Gdx.app.log("MewnBase", "Adding npc bonus: " + (Object)((Object)rewardBonus));
        }
    }

    public void removeBonus(bonusTypes bonusTypeToRemove) {
        if (this.activeBonuses.contains((Object)bonusTypeToRemove)) {
            this.activeBonuses.remove((Object)bonusTypeToRemove);
            Gdx.app.log("MewnBase", "Removing npc bonus: " + (Object)((Object)bonusTypeToRemove));
        }
    }

    public void start() {
        instance = new NpcBonuses();
    }

    public boolean hasBonus(bonusTypes bonusType) {
        return this.activeBonuses.contains((Object)bonusType);
    }

    public ArrayList<bonusTypes> getAllBonuses() {
        ArrayList<bonusTypes> allBonuses = new ArrayList<bonusTypes>();
        allBonuses.addAll(this.activeBonuses);
        return allBonuses;
    }

    public Color getBonusColor(bonusTypes b) {
        switch (b) {
            case gardener: {
                return Color.valueOf("6be21b");
            }
            case mechanic: {
                return Color.valueOf("00ffff");
            }
            case powerBoost: {
                return Color.valueOf("e1e328");
            }
            case scientist: {
                return Color.valueOf("e128e3");
            }
        }
        return Color.WHITE;
    }

    public String getBonusName(bonusTypes b) {
        return Localization.get("bonus_" + (Object)((Object)b));
    }

    public String getBonusDesc(bonusTypes b) {
        return Localization.format("bonus_" + (Object)((Object)b) + "_desc", this.getBonusValue(b));
    }

    private String getBonusValue(bonusTypes b) {
        switch (b) {
            case gardener: {
                return "20%";
            }
            case mechanic: {
                return "";
            }
            case powerBoost: {
                return "5%";
            }
            case scientist: {
                return "10%";
            }
        }
        return "";
    }

    public static enum bonusTypes {
        gardener,
        mechanic,
        powerBoost,
        scientist;

    }
}

