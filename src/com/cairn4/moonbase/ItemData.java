/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.ItemActions;
import com.cairn4.moonbase.RecipieRequirement;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.worlddata.CraftingBonusData;
import java.util.ArrayList;
import java.util.MissingResourceException;

public class ItemData {
    public String id;
    public String sprite;
    public int maxCarry = 99;
    private boolean notStackable;
    public ArrayList<String> craftedIn = new ArrayList();
    public int craftingQuantity;
    public float craftTime;
    public boolean recyclable = true;
    public boolean rotatable;
    public String useAnimName;
    public int durability;
    public ArrayList<RecipieRequirement> requires = new ArrayList();
    public ArrayList<ItemActions> actions = new ArrayList();
    public ArrayList<CraftingBonusData> craftingBonusItems = new ArrayList();
    public String techReq;
    public int researchPoints;
    public String growsInto;
    public ArrayList<String> growSprites;
    public int minMeleeDamage;
    public int maxMeleeDamage;
    public float placementSpriteScale = 1.0f;
    public Vector2 placementSpriteOffset = new Vector2(1.0f, 1.0f);
    public ArrayList<GridPoint2> proxyTiles = new ArrayList();
    @Deprecated
    public float pickupTime;

    public String getName() {
        try {
            return Localization.get("item_" + this.id);
        }
        catch (MissingResourceException e) {
            Gdx.app.error("MewnBase", "Missing item name in loc file for " + this.id);
            return this.id;
        }
    }

    public String getDesc() {
        try {
            return Localization.get("item_desc_" + this.id);
        }
        catch (MissingResourceException e) {
            Gdx.app.error("MewnBase", "Missing item description in loc file for " + this.id);
            return this.id + " desc here...";
        }
    }

    public boolean canStackMultiples() {
        return this.durability <= 0 && !this.notStackable;
    }
}

