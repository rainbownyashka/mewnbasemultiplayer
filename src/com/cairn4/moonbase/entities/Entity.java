/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.Tile;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Entity {
    protected World world;
    public boolean readyToRemove;
    public long id;
    protected boolean requiresFinalSetupAfterLoad = false;
    public Group group;
    protected Image image;
    protected float spawnPosX;
    protected float spawnPosY;
    protected boolean canClick;
    protected boolean clicked;
    public boolean zSort;

    public Entity(World world, float xPos, float yPos) {
        world.entityList.add(this);
        this.readyToRemove = false;
        this.world = world;
        this.id = world.getEntityId();
        this.spawnPosX = xPos;
        this.spawnPosY = yPos;
    }

    public void update(float delta) {
        if (this.requiresFinalSetupAfterLoad) {
            this.doneLoading();
            this.requiresFinalSetupAfterLoad = false;
        }
        if (this.zSort) {
            this.updateZsort();
        }
    }

    protected void updateZsort() {
        if (this.group != null) {
            this.group.setUserObject(Float.valueOf(this.getYPos()));
        }
    }

    public void remove() {
    }

    public void dispose() {
    }

    public void setWorldPos(float wX, float wY) {
        this.group.setPosition(wX, wY);
    }

    public float getXPos() {
        if (this.group != null) {
            return this.group.getX();
        }
        MoonBase.error("Missing entity group for " + this.getClass().getSimpleName());
        return 0.0f;
    }

    public float getYPos() {
        if (this.group != null) {
            return this.group.getY();
        }
        MoonBase.error("Missing entity group for " + this.getClass().getSimpleName());
        return 0.0f;
    }

    public float getRotation() {
        return this.group.getRotation();
    }

    public boolean canClick() {
        return this.canClick;
    }

    public void resetClicked() {
        if (this.clicked) {
            this.clicked = false;
            System.out.println("reset click");
        }
    }

    public boolean isClicked() {
        return this.clicked;
    }

    protected void setClicked() {
        System.out.println("setClicked");
        this.clicked = true;
    }

    public void playerAction(Player player) {
    }

    public void playerActionSecondary(Player player) {
    }

    public boolean isSaved() {
        return true;
    }

    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        return properties;
    }

    public void loadProperties(HashMap<String, Object> p) {
        for (Map.Entry<String, Object> entry : p.entrySet()) {
            Class<?> aClass = this.getClass();
            Field field = null;
            try {
                field = aClass.getField(entry.getKey());
                field.setAccessible(true);
                field.set(this, entry.getValue());
            }
            catch (NoSuchFieldException e) {
                MoonBase.error("Unknown field in entity save data: " + e.getMessage());
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        this.requiresFinalSetupAfterLoad = true;
    }

    public boolean distanceToPlayer(float threshold) {
        Vector2 playerPos = new Vector2(this.world.player.getXPos(), this.world.player.getYPos());
        float diff = playerPos.dst(this.getXPos(), this.getYPos());
        return diff <= threshold;
    }

    public boolean tileDistanceToPlayer(float threshold) {
        GridPoint2 playerPos = World.gridPointPool.obtain();
        playerPos.set(this.world.player.getX(), this.world.player.getY());
        float diff = playerPos.dst(this.getXTile(), this.getYTile());
        World.gridPointPool.free(playerPos);
        return diff < threshold;
    }

    public float distanceToTile(int worldX, int worldY) {
        GridPoint2 tilePos = World.gridPointPool.obtain();
        tilePos.set(worldX, worldY);
        float dist = tilePos.dst(this.getXTile(), this.getYTile());
        World.gridPointPool.free(tilePos);
        return dist;
    }

    public void doneLoading() {
    }

    public int getWorldXTile() {
        double xd = Math.floor(this.getXPos() / Tile.TILE_SIZE);
        return Math.round((float)xd);
    }

    public int getWorldYTile() {
        double yd = Math.floor(this.getYPos() / Tile.TILE_SIZE);
        return Math.round((float)yd);
    }

    public Chunk getCurrentChunk() {
        GridPoint2 gp = World.gridPointPool.obtain();
        gp = World.convertWorldPosToChunkCoord(gp, this.getWorldXTile(), this.getWorldYTile());
        return this.world.getChunk(gp.x, gp.y);
    }

    public Body getBody() {
        return null;
    }

    protected int getXTile() {
        double xd = Math.floor(this.getXPos() / Tile.GRID_SIZE);
        return Math.round((float)xd);
    }

    protected int getYTile() {
        double yd = Math.floor(this.getYPos() / Tile.GRID_SIZE);
        return Math.round((float)yd);
    }
}

