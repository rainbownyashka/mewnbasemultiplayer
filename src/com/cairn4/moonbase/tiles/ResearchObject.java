/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.tiles.ItemDropper;
import com.cairn4.moonbase.tiles.behaviors.ResearchDropperBehavior;
import com.cairn4.moonbase.worlddata.ItemDropperData;
import com.cairn4.moonbase.worlddata.ItemDropperFactory;

public class ResearchObject
extends ItemDropper {
    public ResearchDropperBehavior researchDropperBehavior;

    public ResearchObject(World world, Chunk chunk, int x, int y, String itemDropperId) {
        this(world, chunk, x, y, ItemDropperFactory.getInstance().getItemDropperData(itemDropperId));
    }

    public ResearchObject(World world, Chunk chunk, int x, int y, ItemDropperData data) {
        super(world, chunk, x, y, data);
    }

    @Override
    public void startBehaviors() {
        super.startBehaviors();
        this.setupImage();
    }

    @Override
    protected void setupImage() {
        this.addROBehavior();
        Group g = this.world.gameScreen.mainGroup;
        if (!this.rdData.zSort) {
            g = this.world.gameScreen.floorGroup;
        }
        this.createDrawables("ro" + this.researchDropperBehavior.skinNum, g);
        this.image.setOrigin(4);
        if (MathUtils.randomBoolean()) {
            this.image.setOrigin(1);
            this.flipped = true;
            this.image.setScaleX(-1.0f);
        }
        this.group.setUserObject(Float.valueOf(this.getYCenter()));
    }

    private void addROBehavior() {
        this.researchDropperBehavior = new ResearchDropperBehavior();
        this.researchDropperBehavior.skinNum = MathUtils.random(0, 17);
        this.behaviorList.add(this.researchDropperBehavior);
    }

    public void dropItems(Player player) {
        this.dropFx();
        String itemId = "researchObject" + this.researchDropperBehavior.skinNum;
        Item newItem = ItemFactory.getInstance().createItem(itemId);
        newItem.setSprite("ro" + this.researchDropperBehavior.skinNum);
        new ItemPickup(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos(), newItem);
    }
}

