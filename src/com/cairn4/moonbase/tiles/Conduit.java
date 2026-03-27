/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;

public class Conduit
extends BaseModule {
    Group caps;
    final int bS = 1;
    final int bE = 2;
    final int bN = 4;
    final int bW = 8;

    public Conduit(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 0.1f;
        this.hasAir = false;
        this.hasPower = true;
        this.blocker = false;
        this.updateBases();
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("conduit-builder");
        this.caps = new Group();
        this.group.addActor(this.caps);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "conduit";
    }

    @Override
    public boolean usesComms() {
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
        return false;
    }

    public void autoTile() {
        this.caps.clearChildren();
        Tile north = this.world.getTile(this.worldX, this.worldY + 1);
        Tile south = this.world.getTile(this.worldX, this.worldY - 1);
        Tile east = this.world.getTile(this.worldX + 1, this.worldY);
        Tile west = this.world.getTile(this.worldX - 1, this.worldY);
        int S = 0;
        int E = 0;
        int N = 0;
        int W = 0;
        N = this.checkTile(north, 4);
        S = this.checkTile(south, 1);
        E = this.checkTile(east, 2);
        W = this.checkTile(west, 8);
        int total = S + E + N + W;
        this.setConduitImage(total);
    }

    protected void setConduitImage(int num) {
        if (num == 0) {
            num = 15;
        }
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable("test/conduit-" + num));
    }

    private int checkTile(Tile t, int directionBit) {
        BaseModule base;
        if (t != null && t instanceof BaseModule && (base = (BaseModule)t).visuallyConnectToConduits()) {
            if (!(base instanceof Conduit)) {
                Image capN = new Image(this.world.gameScreen.skin.getDrawable("conduit-cap"));
                capN.setSize(37.0f, 59.0f);
                capN.setOrigin(1);
                capN.setScale(0.5f);
                capN.setRotation(0.0f);
                this.caps.addActor(capN);
                switch (directionBit) {
                    case 1: {
                        capN.setRotation(90.0f);
                        capN.setPosition(Tile.TILE_SIZE / 2.0f, 0.0f, 1);
                        break;
                    }
                    case 2: {
                        capN.setPosition(Tile.TILE_SIZE, Tile.TILE_SIZE / 2.0f, 1);
                        break;
                    }
                    case 4: {
                        capN.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE, 1);
                        capN.setRotation(90.0f);
                        break;
                    }
                    case 8: {
                        capN.setPosition(0.0f, Tile.TILE_SIZE / 2.0f, 1);
                    }
                }
            }
            return directionBit;
        }
        return 0;
    }

    @Override
    protected void noPowerIcon(boolean show) {
    }

    @Override
    public void setPower(boolean on) {
        if (on != this.hasPower) {
            this.hasPower = on;
        }
    }
}

