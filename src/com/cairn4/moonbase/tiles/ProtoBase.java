/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.SpineActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.ProtoBaseBehavior;
import com.cairn4.moonbase.ui.BuildingPlacementCursor;
import com.cairn4.moonbase.ui.TileProgressBar;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class ProtoBase extends BaseModule {
    public ProtoBaseBehavior protoBaseBehavior;
  
    Rectangle collision;
  
    TileProgressBar tileProgressBar;
  
    int buildStage = 1;
  
    private SpineActor spineActor;
  
    private ArrayList<GridPoint2> checkTiles = new ArrayList<>();
  
    private ArrayList<SpineActor> proxySpineActors = new ArrayList<>();
  
    public ProtoBase(World world, Chunk chunk, int x, int y, String itemId, String base, float buildTime, BuildingPlacementCursor.ORIENTATIONS orientation) {
        super(world, chunk, x, y);
        this.protoBaseBehavior = new ProtoBaseBehavior();
        this.protoBaseBehavior.itemId = itemId;
        this.protoBaseBehavior.base = base;
        this.protoBaseBehavior.timer = 0.0F;
        this.protoBaseBehavior.buildTime = buildTime;
        this.protoBaseBehavior.orientation = orientation;
        this.protoBaseBehavior.playerClear = false;
        this.behaviorList.add(this.protoBaseBehavior);
        this.type = Tile.types.base;
        this.blocker = false;
        this.hasAir = false;
        this.hasPower = false;
        this.disablePickup = true;
        clearGroundTile();
        ItemData data = ItemFactory.getItemData(itemId);
        for (GridPoint2 pt : data.proxyTiles) {
            int px = this.worldX + pt.x;
            int py = this.worldY + pt.y;
            this.checkTiles.add(new GridPoint2(px, py));
            addProxyTileImage(pt.x, pt.y);
        } 
        this.collision = new Rectangle(this.x * Tile.GRID_SIZE, this.y * Tile.GRID_SIZE, GRID_SIZE, GRID_SIZE);
        this.image.setColor(1.0F, 1.0F, 1.0F, 0.2F);
        this.spineActor.skeleton.setColor(new Color(1.0F, 1.0F, 1.0F, 0.5F));
        if (world.gameScreen.hud != null)
            this.tileProgressBar = new TileProgressBar(world.gameScreen.hud, this); 
    }
  
    public ProtoBase(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.protoBaseBehavior = new ProtoBaseBehavior();
        this.protoBaseBehavior.base = "";
        this.protoBaseBehavior.timer = 0.0F;
        this.protoBaseBehavior.buildTime = 0.0F;
        this.protoBaseBehavior.orientation = BuildingPlacementCursor.ORIENTATIONS.north;
        this.protoBaseBehavior.playerClear = false;
        this.behaviorList.add(this.protoBaseBehavior);
        this.type = Tile.types.base;
        this.blocker = false;
        this.hasAir = false;
        this.hasPower = false;
        this.disablePickup = true;
        clearGroundTile();
        this.collision = new Rectangle(this.x * Tile.GRID_SIZE, this.y * Tile.GRID_SIZE, GRID_SIZE, GRID_SIZE);
        this.image.setColor(1.0F, 1.0F, 1.0F, 0.5F);
        this.spineActor.skeleton.setColor(new Color(1.0F, 1.0F, 1.0F, 0.5F));
        if (world.gameScreen.hud != null)
            this.tileProgressBar = new TileProgressBar(world.gameScreen.hud, this); 
    }
  
    public boolean usesComms() {
        return false;
    }
  
    public boolean usesBaseGroup() {
        return false;
    }
  
    public void setupLight() {}
  
    protected void createDrawables() {
        createDrawables("proto-building");
        this.image.setVisible(false);
        setupSpineActor();
    }
  
    public void setupSpineActor() {
        this.spineActor = new SpineActor("protobase", 0.5F, this.world.gameScreen.skeletonRenderer);
        this.spineActor.setPosition(TILE_SIZE / 2.0F, TILE_SIZE / 2.0F);
        this.group.addActor((com.badlogic.gdx.scenes.scene2d.Actor)this.spineActor);
        this.spineActor.addAnimation(0, "start", false, 0.0F);
        this.spineActor.addAnimation(0, "idle", true, 0.0F);
    }
  
    private void addProxyTileImage(int xTileOffset, int yTileOffset) {
        SpineActor sa = new SpineActor("protobase", 0.5F, this.world.gameScreen.skeletonRenderer);
        sa.setPosition(TILE_SIZE / 2.0F + TILE_SIZE * xTileOffset, TILE_SIZE / 2.0F + Tile.TILE_SIZE * yTileOffset);
        this.group.addActor((com.badlogic.gdx.scenes.scene2d.Actor)sa);
        sa.addAnimation(0, "start", false, 0.0F);
        sa.addAnimation(0, "idle", true, 0.0F);
        this.proxySpineActors.add(sa);
    }
  
    public void update(float delta) {
        this.spineActor.update(delta);
        for (SpineActor sa : this.proxySpineActors)
            sa.update(delta); 
        if (this.tileProgressBar != null) {
            if (this.protoBaseBehavior.playerClear) {
                if (this.protoBaseBehavior.timer < this.protoBaseBehavior.buildTime) {
                    this.protoBaseBehavior.timer += delta;
                    this.tileProgressBar.set(this.protoBaseBehavior.timer / this.protoBaseBehavior.buildTime);
                } else {
                    this.tileProgressBar.remove();
                    destroy();
                    createBase();
                } 
            } else {
                int playerWorldX = this.world.player.getX();
                int playerWorldY = this.world.player.getY();
                if (playerWorldX != this.worldX || playerWorldY != this.worldY) {
                    boolean clearOfProxies = true;
                    for (GridPoint2 proxyTile : this.checkTiles) {
                        if (playerWorldX == proxyTile.x && playerWorldY == proxyTile.y)
                            clearOfProxies = false; 
                    } 
                    if (clearOfProxies) {
                        this.protoBaseBehavior.playerClear = true;
                        this.blocker = true;
                        this.spineActor.skeleton.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
                        this.tileProgressBar.set(0.0F);
                    } 
                } 
            } 
        } else if (this.world.gameScreen.hud != null) {
            this.tileProgressBar = new TileProgressBar(this.world.gameScreen.hud, this);
        } 
    }
  
    private void halfDone() {
        if (this.protoBaseBehavior.timer / this.protoBaseBehavior.buildTime > 0.5F && this.buildStage == 1) {
            this.image.setDrawable(this.world.gameScreen.skin.getDrawable("proto-habitat"));
            this.buildStage = 2;
        } 
    }
  
    private void createBase() {
        Class[] classArgs;
        Object arguments[];
        Object object = null;
        if (this.protoBaseBehavior.base.equals("Airlock") || this.protoBaseBehavior.base.equals("AutoAirlock") || this.protoBaseBehavior.base.equals("WallLight")) {
            classArgs = new Class[] { World.class, Chunk.class, int.class, int.class, BaseModule.ORIENTATIONS.class };
            BaseModule.ORIENTATIONS orientation = BaseModule.ORIENTATIONS.valueOf(this.protoBaseBehavior.orientation.toString());
            arguments = new Object[] { this.chunk.world, this.chunk, new Integer(this.x), new Integer(this.y), orientation };
        } else {
            classArgs = new Class[] { World.class, Chunk.class, int.class, int.class };
            arguments = new Object[] { this.chunk.world, this.chunk, new Integer(this.x), new Integer(this.y) };
        } 
        try {
            String rawBase = this.protoBaseBehavior.base == null ? "" : this.protoBaseBehavior.base.trim();
            String tilesPkg = "com.cairn4.moonbase.tiles.";
            String[] candidates;
            if (rawBase.contains(".")) {
                // user supplied fully-qualified name first
                candidates = new String[] { rawBase, tilesPkg + rawBase.substring(rawBase.lastIndexOf('.') + 1) };
            } else {
                candidates = new String[] { tilesPkg + rawBase, rawBase };
            }
            Class<?> aClass = null;
            for (String cand : candidates) {
                if (cand == null || cand.length() == 0) continue;
                try {
                    aClass = Class.forName(cand);
                    break;
                } catch (ClassNotFoundException cnf) {
                    // try next candidate
                }
            }
            if (aClass == null) {
                com.badlogic.gdx.Gdx.app.error("ProtoBase", "createBase: class not found for base='" + this.protoBaseBehavior.base + "'");
            } else {
                Constructor<?> constructor = aClass.getConstructor(classArgs);
                object = constructor.newInstance(arguments);
                Audio.getInstance().playSound("sounds/interact_hit2.ogg", 1.0F, 0.6F);
            } 
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        } catch (InvocationTargetException e) {
            e.getCause();
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } 
        BaseModule b = (BaseModule)object;
        if (b == null) {
            com.badlogic.gdx.Gdx.app.error("ProtoBase", "createBase: failed to construct base for '" + this.protoBaseBehavior.base + "'");
            return;
        }
        b.animateSpawn();
        System.out.println("Creating new base module at " + this.x + ", " + this.y);
    // No automatic network broadcast here. Network messages for builds are
    // sent when the action-build is started (TILE_BUILD_START). This avoids
    // duplicate application when the originator also completes the build.
    }
}

