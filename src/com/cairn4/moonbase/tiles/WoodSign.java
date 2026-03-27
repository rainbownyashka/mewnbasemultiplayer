/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.IntValueBehavior;
import com.cairn4.moonbase.tiles.behaviors.StringValueBehavior;
import java.util.ArrayList;
import java.util.Arrays;

public class WoodSign
extends BaseModule {
    Image itemIcon;
    Item itemHolding;
    private static final ArrayList<String> signIcons = new ArrayList<String>(Arrays.asList("sign-warning", "sign-arrow-right", "sign-arrow-down", "sign-arrow-left", "sign-arrow-up", "sign-home", "sign-heart", "sign-paw"));
    public StringValueBehavior stringValueBehavior;
    public IntValueBehavior intValueBehavior;

    public WoodSign(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.setupPhysics("tree");
        this.stringValueBehavior = new StringValueBehavior();
        this.stringValueBehavior.value = "";
        this.behaviorList.add(this.stringValueBehavior);
        this.intValueBehavior = new IntValueBehavior();
        this.intValueBehavior.value = 0;
        this.behaviorList.add(this.intValueBehavior);
        this.world.baseManager.addSignToList(this);
    }

    public String getIcon() {
        return this.stringValueBehavior.value;
    }

    @Override
    public void startBehaviors() {
        if (this.stringValueBehavior.value != null && this.stringValueBehavior.value.length() > 0) {
            this.setIcon(this.stringValueBehavior.value);
        }
    }

    private void setIcon(String drawableName) {
        if (drawableName != null) {
            this.itemIcon.setVisible(true);
            try {
                this.itemIcon.setDrawable(this.world.gameScreen.skin.getDrawable(drawableName));
            }
            catch (Exception e) {
                MoonBase.error("Sign: Can't find sprite: " + drawableName);
                this.itemIcon.setDrawable(this.world.gameScreen.skin.getDrawable("missing"));
                drawableName = "missing";
            }
            this.stringValueBehavior.value = drawableName;
        } else {
            this.itemIcon.setVisible(false);
        }
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("wood-sign", this.world.gameScreen.mainGroup);
        this.group.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + (TILE_SIZE / 2.0f + 0.0f)));
        this.itemIcon = new Image(this.world.gameScreen.skin.getDrawable("metal-refined"));
        this.itemIcon.setSize(60.0f, 44.0f);
        this.itemIcon.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f + 13.0f, 1);
        this.itemIcon.setScaling(Scaling.fit);
        this.itemIcon.setVisible(false);
        this.group.addActor(this.itemIcon);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "woodsign";
    }

    @Override
    public boolean usesBaseGroup() {
        return false;
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    public boolean visuallyConnectToConduits() {
        return false;
    }

    @Override
    public boolean blocksWind() {
        return false;
    }

    @Override
    public void setupLight() {
    }

    @Override
    public void interact(Player player) {
        ItemStack itemStack = player.playerInventory.getEquippedItem();
        this.itemIcon.setVisible(true);
        if (itemStack != null) {
            this.intValueBehavior.value = 0;
            this.setIcon(itemStack.item.getSprite());
        } else {
            ++this.intValueBehavior.value;
            if (this.intValueBehavior.value >= signIcons.size()) {
                this.intValueBehavior.value = 0;
            }
            this.setIcon(signIcons.get(this.intValueBehavior.value));
        }
    }

    @Override
    public boolean canInteract(Player player) {
        return true;
    }

    @Override
    public void destroy() {
        this.world.baseManager.removeSignFromList(this);
        super.destroy();
    }
}

