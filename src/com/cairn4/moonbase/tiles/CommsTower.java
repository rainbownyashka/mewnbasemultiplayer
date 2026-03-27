/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.NavLocator;
import java.util.ArrayList;

public class CommsTower
extends BaseModule {
    AdditiveImage glow;
    NavLocator navLocator;
    Sound sound;
    long soundId;
    float volume;
    float pan;
    public static int commsRangeTiles = 30;
    public static float commsRange = (float)commsRangeTiles * Tile.TILE_SIZE;

    public CommsTower(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 3.0f;
        this.powerGenRate = 0.0f;
        this.createDrawables("commstower");
        this.setupPhysics("commstower");
        this.navLocator = new NavLocator();
        this.navLocator.baseModule = this;
        this.behaviorList.add(this.navLocator);
        this.interactDuration = 0.0f;
        this.updateBases();
        this.sound = Audio.getInstance().getSound("sounds/commstower.ogg");
        this.soundId = Audio.getInstance().playSoundLoop(this.sound, 0.0f, 1.0f, 0.0f);
    }

    private void discoverSurroundingArea() {
        this.world.updateDiscovery(this.worldX, this.worldY, commsRangeTiles);
    }

    @Override
    public void setupLight() {
        this.lightOnColor = new Color(1.0f, 0.3f, 0.3f, 1.0f);
        super.setupLight();
        this.light.setDistance(0.78125f);
        this.light.setPosition(this.getXCenter() / 256.0f, (this.getYCenter() + 20.0f) / 256.0f);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.hasPower) {
            this.volume = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 300.0f, 0.05f);
            this.pan = Audio.getInstance().playerDistancePan(this.world, new Vector2(this.getXCenter(), this.getYCenter()));
        } else {
            this.volume = 0.0f;
            this.pan = 0.0f;
        }
        if (this.volume <= 0.0f) {
            this.sound.pause(this.soundId);
        } else {
            this.sound.resume(this.soundId);
            Audio.getInstance().updateLoopingSoundVolume(this.sound, this.soundId, this.volume, this.pan);
        }
    }

    @Override
    public Color getMinimapColor() {
        return Color.CYAN;
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "commstower";
    }

    @Override
    public void createDrawables(String drawable) {
        super.createDrawables(drawable, this.world.gameScreen.mainGroup);
        this.glow = new AdditiveImage(this.world.gameScreen.skin.getDrawable("commstower-glow"));
        this.glow.setPosition(78.0f, 114.0f, 1);
        this.glow.addAction(Actions.forever(Actions.sequence((Action)Actions.fadeOut(2.0f), (Action)Actions.fadeIn(3.0f))));
        this.group.addActor(this.glow);
    }

    @Override
    public void setPower(boolean on) {
        super.setPower(on);
        this.glow.setVisible(on);
    }

    @Override
    public void destroy() {
        this.sound.stop(this.soundId);
        super.destroy();
    }

    public ArrayList<CommsTower> getOtherCommsTowersInRange() {
        ArrayList<CommsTower> nearby = new ArrayList<CommsTower>();
        Vector2 thisCenter = new Vector2(this.getWorldXPos(), this.getWorldYPos());
        ArrayList<GridPoint2> gpList = Tile.GET_NEARBY_COORDS(this.worldX, this.worldY, commsRangeTiles);
        for (GridPoint2 gp : gpList) {
            float dst;
            Tile t = this.world.getTile(gp.x, gp.y);
            if (t == null || !(t instanceof CommsTower) || t == this || !((CommsTower)t).hasPower || !((dst = thisCenter.dst((float)gp.x * Tile.TILE_SIZE, (float)gp.y * Tile.TILE_SIZE)) < commsRange)) continue;
            nearby.add((CommsTower)t);
        }
        return nearby;
    }
}

