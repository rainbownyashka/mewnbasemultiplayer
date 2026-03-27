/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MultiplyImage2;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.StorageColorOptions;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.IntValueBehavior;
import com.cairn4.moonbase.tiles.behaviors.TileSpriteAnim;

public class FlagPole
extends BaseModule {
    private Image flag;
    private MultiplyImage2 colorTint;
    int colorIndex;
    int frameIndex = 1;
    int frames = 4;
    float timePerFrame = 0.2f;
    float timer = 0.0f;
    public IntValueBehavior colorIndexBehavior;
    public TileSpriteAnim tileSpriteAnim;

    public FlagPole(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.setupPhysics("flag");
        this.colorIndexBehavior = new IntValueBehavior();
        this.colorIndexBehavior.value = 2;
        this.behaviorList.add(this.colorIndexBehavior);
        this.setColor(this.colorIndexBehavior.value);
    }

    @Override
    public void startBehaviors() {
        this.setColor(this.colorIndexBehavior.value);
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("flagpole", this.world.gameScreen.mainGroup);
        this.image.setSize(Tile.TILE_SIZE, 347.0f * Tile.SCALE);
        this.image.setPosition(0.0f, 32.0f * Tile.SCALE);
        this.group.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + (TILE_SIZE / 2.0f + 20.0f)));
        this.flag = new Image(this.world.gameScreen.skin.getDrawable("flag-anim1"));
        this.flag.setSize(161.0f * Tile.SCALE, 136.0f * Tile.SCALE);
        this.flag.setOrigin(8);
        this.flag.setPosition(Tile.TILE_SIZE / 2.0f - 15.0f, TILE_SIZE - 10.0f, 12);
        this.group.addActor(this.flag);
        this.colorTint = new MultiplyImage2(this.world.gameScreen.skin.getDrawable("flag-anim1-tint"));
        this.colorTint.setSize(this.flag.getWidth() - 0.5f, this.flag.getHeight() - 0.5f);
        this.colorTint.setOrigin(8);
        this.colorTint.setPosition(this.flag.getX(1), this.flag.getY(1) - 0.5f, 1);
        this.group.addActor(this.colorTint);
        this.colorTint.setColor(Color.GREEN);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "flagpole";
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
    public boolean canInteract(Player player) {
        return true;
    }

    @Override
    public void interact(Player player) {
        if (player.getPlayerInventory().getEquippedItemId().equals("paintbrush")) {
            this.setColor(player.getPaintColorIndex());
            this.paintbrushParticleFx();
        } else {
            super.interact(player);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        this.timer += delta;
        if (this.timer > this.timePerFrame) {
            this.timer = 0.0f;
            ++this.frameIndex;
            if (this.frameIndex > this.frames) {
                this.frameIndex = 1;
            }
            this.flag.setDrawable(this.world.gameScreen.skin.getDrawable("flag-anim" + this.frameIndex));
            this.colorTint.setDrawable(this.world.gameScreen.skin.getDrawable("flag-anim" + this.frameIndex + "-tint"));
        }
    }

    public void setColor(int newIndex) {
        this.colorIndex = newIndex;
        this.colorTint.setColor(StorageColorOptions.colors[this.colorIndex]);
        this.colorIndexBehavior.value = this.colorIndex;
    }

    public void paintbrushParticleFx() {
        Audio.getInstance().playSound("sounds/paintbrush-splat.ogg", 0.5f, 1.2f);
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/paintbrush-splat.p", ParticleEffect.class));
        ParticleActor paintPuff = new ParticleActor(p, true);
        paintPuff.setPosition(this.getXCenter(), this.getYCenter());
        this.world.gameScreen.topGroup.addActor(paintPuff);
        for (ParticleEmitter e : paintPuff.pfx.getEmitters()) {
            ParticleEmitter.GradientColorValue gradient = e.getTint();
            Color newColor = Vars.habColors[this.colorIndexBehavior.value];
            gradient.getColors()[0] = newColor.r;
            gradient.getColors()[1] = newColor.g;
            gradient.getColors()[2] = newColor.b;
        }
        paintPuff.pfx.start();
    }
}

