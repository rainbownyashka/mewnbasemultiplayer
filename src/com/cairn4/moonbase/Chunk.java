/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.StringBuilder;
import com.cairn4.moonbase.WeatherParticleFx;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BarrierWall;
import com.cairn4.moonbase.tiles.Conduit;
import com.cairn4.moonbase.tiles.Gate;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.LavaFlow;
import com.cairn4.moonbase.tiles.ProxyTile;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.TileFactory;
import com.cairn4.moonbase.worlddata.ChunkData;
import java.net.URLEncoder;
import com.cairn4.moonbase.worlddata.GroundTileData;
import com.cairn4.moonbase.worlddata.TileData;
import com.cairn4.moonbase.worlddata.WeatherData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Chunk {
    public static final int WIDTH = 10;
    public static final int HEIGHT = 10;
    public World world;
    public int chunkX;
    public int chunkY;
    public boolean visible;
    public ArrayList<Tile> tilesThatNeedProxies = new ArrayList();
    public HashMap<String, GroundTile> groundTiles = new HashMap(100);
    public HashMap<String, Tile> floorTiles = new HashMap();
    public HashMap<String, Tile> tiles = new HashMap();
    public GroundTile.Biomes[] gtBiomeArray = new GroundTile.Biomes[100];
    public boolean[] gtDiscoveryArray = new boolean[100];
    private boolean mapDirty;
    private static StringBuilder stringBuilder = new StringBuilder();
    WeatherParticleFx weatherFx;
    private Iterator<Map.Entry<String, Tile>> tileIterator;
    private Iterator<Map.Entry<String, Tile>> floorTileIterator;
    private Iterator<Map.Entry<String, GroundTile>> groundTileIterator;
    private Map.Entry<String, Tile> tileEntry;
    private Map.Entry<String, Tile> floorTileEntry;

    public boolean isMapDirty() {
        return this.mapDirty;
    }

    public void setMapDirty(boolean mapDirty) {
        this.mapDirty = mapDirty;
    }

    public Chunk(World world, int chunkX, int chunkY) {
        this.world = world;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.visible = true;
        this.generate();
        this.setMapDirty(true);
    }

    public Chunk(World world, int chunkX, int chunkY, boolean empty) {
        this.world = world;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.visible = false;
        this.setMapDirty(true);
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void generate() {
        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                GroundTile g = this.world.groundTilePool.obtain();
                g.init(this.world, this, x, y);
                this.setGroundTile(g, x, y);
            }
        }
        this.world.terrainGen.createChunkTerrain(this);
        this.addEnvironmentFx();
    }

    public void parseGroundTileData(ChunkData cd) {
        for (GroundTileData gtd : cd.groundTiles.values()) {
            GroundTile gt = TileFactory.getInstance().createGroundTile(this, gtd);
            this.setGroundTile(gt, gt.x, gt.y);
        }
        // Ensure ice/volcanic separation for loaded chunks too
        try {
            if (this.world != null && this.world.terrainGen != null) {
                this.world.terrainGen.resolveIceVolcanicAdjacency(this);
            }
        } catch (Exception ignored) {}
    }

    public void parseFloorTileData(ChunkData cd) {
        for (TileData td : cd.floorTiles.values()) {
            try {
                Tile tile = TileFactory.getInstance().createTile(this, td);
            }
            catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void parseTileData(ChunkData cd) {
        for (TileData td : cd.tiles.values()) {
            try {
                Tile tile = TileFactory.getInstance().createTile(this, td);
            }
            catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void addEnvironmentFx() {
        if (this.weatherFx != null) {
            this.weatherFx.currentFx.remove();
        }
        this.weatherFx = new WeatherParticleFx(this);
    }

    public void setWeatherFx(WeatherData newWeatherData) {
        if (this.weatherFx != null) {
            this.weatherFx.changeWeather(newWeatherData);
        }
    }

    public void setTile(Tile tile, int x, int y) {
        stringBuilder.setLength(0);
        stringBuilder.append("t_").append(x).append(",").append(y);
        if (this.tiles.containsKey(stringBuilder.toString())) {
            this.getTile(x, y).destroy();
            if (this.getTile(x, y).hasPhysicsBody()) {
                this.world.b2dWorld.destroyBody(this.getTile(x, y).getBody());
            }
        }
        stringBuilder.setLength(0);
        stringBuilder.append("t_").append(x).append(",").append(y);
        this.tiles.put(stringBuilder.toString(), tile);
        this.setMapDirty(true);
        
    }

    public void setFloorTile(Tile tile, int x, int y) {
        stringBuilder.setLength(0);
        stringBuilder.append("t_").append(x).append(",").append(y);
        if (this.tiles.containsKey(stringBuilder.toString())) {
            this.getTile(x, y).destroy();
            if (this.getTile(x, y).hasPhysicsBody()) {
                this.world.b2dWorld.destroyBody(this.getTile(x, y).getBody());
            }
        }
        stringBuilder.setLength(0);
        stringBuilder.append("t_").append(x).append(",").append(y);
        this.floorTiles.put(stringBuilder.toString(), tile);
        try {
            if (this.world != null && !this.world.suppressNetworkTileEvents && this.world.gameScreen != null) {
                int worldX = this.chunkX * 10 + x;
                int worldY = this.chunkY * 10 + y;
                String className = tile != null ? tile.getClass().getName() : "";
                String encName = "";
                try { encName = className != null ? URLEncoder.encode(className, "UTF-8") : ""; } catch (Exception _e) {}
                String payload = "TILE_SET:" + worldX + ":" + worldY + ":" + encName;
                try {
                    if (this.world.gameScreen.client != null) {
                        Class<?> clientClass = this.world.gameScreen.client.getClass();
                        java.lang.reflect.Method send = clientClass.getMethod("sendMessage", String.class);
                        send.invoke(this.world.gameScreen.client, payload);
                    } else {
                        // If no client (we're running as an authoritative in-process server), forward via Server broadcast
                        com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                        if (s != null) s.broadcastFromServer(payload);
                    }
                } catch (NoSuchMethodException nsme) {
                    // ignore
                }
            }
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.error("Chunk", "Failed to broadcast floor tile set", e);
        }
    }

    public void setGroundTile(GroundTile groundTile, int x, int y) {
        this.groundTiles.put("gt_" + x + "," + y, groundTile);
        this.gtBiomeArray[y * 10 + x] = groundTile.getBiome();
        this.gtDiscoveryArray[y * 10 + x] = groundTile.isDiscovered();
    }

    public Tile getTile(int x, int y) {
        return this.getTile(x, y, false);
    }

    public Tile getTile(int x, int y, boolean includeProxyTiles) {
        stringBuilder.setLength(0);
        stringBuilder.append("t_").append(x).append(",").append(y);
        Tile tile = this.tiles.get(stringBuilder.toString());
        if (includeProxyTiles) {
            return tile;
        }
        if (tile instanceof ProxyTile) {
            return ((ProxyTile)tile).parent;
        }
        return tile;
    }

    public Tile getFloorTile(int x, int y) {
        stringBuilder.setLength(0);
        stringBuilder.append("t_").append(x).append(",").append(y);
        return this.floorTiles.get(stringBuilder.toString());
    }

    public GroundTile getGroundTile(int x, int y) {
        stringBuilder.setLength(0);
        stringBuilder.append("gt_").append(x).append(",").append(y);
        return this.groundTiles.get(stringBuilder.toString());
    }

    public boolean isTileEmpty(int x, int y) {
        return this.getTile(x, y) == null;
    }

    public void removeTile(int x, int y) {
        stringBuilder.setLength(0);
        stringBuilder.append("t_").append(x).append(",").append(y);
        this.tiles.remove(stringBuilder.toString());
        try {
            if (this.world != null && !this.world.suppressNetworkTileEvents && this.world.gameScreen != null) {
                int worldX = this.chunkX * 10 + x;
                int worldY = this.chunkY * 10 + y;
                try {
                    if (this.world.gameScreen.client != null) {
                        Class<?> clientClass = this.world.gameScreen.client.getClass();
                        java.lang.reflect.Method send = clientClass.getMethod("sendMessage", String.class);
                        send.invoke(this.world.gameScreen.client, "TILE_REMOVE:" + worldX + ":" + worldY);
                    } else {
                        com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                        if (s != null) s.broadcastFromServer("TILE_REMOVE:" + worldX + ":" + worldY);
                    }
                } catch (NoSuchMethodException nsme) {
                    // ignore
                }
            }
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.error("Chunk", "Failed to broadcast tile remove", e);
        }
    }

    public void updateTiles(float delta) {
        boolean floorRemoved = false;
        // iterate with explicit iterator so we can remove safely
        this.floorTileIterator = this.floorTiles.entrySet().iterator();
        while (this.floorTileIterator.hasNext()) {
            Map.Entry<String, Tile> floorTileEntry = this.floorTileIterator.next();
            floorTileEntry.getValue().update(delta);
            if (!floorTileEntry.getValue().readyToRemove) continue;
            // capture world coords before destroying
            int fx = floorTileEntry.getValue().x;
            int fy = floorTileEntry.getValue().y;
            int worldX = this.chunkX * 10 + fx;
            int worldY = this.chunkY * 10 + fy;
            floorTileEntry.getValue().destroy();
            if (floorTileEntry.getValue().hasPhysicsBody()) {
                this.world.b2dWorld.destroyBody(floorTileEntry.getValue().getBody());
            }
            this.floorTileIterator.remove();
            // notify network about floor removal (migrated behaviour from mewnbase_dc)
                try {
                    if (this.world != null && !this.world.suppressNetworkTileEvents && this.world.gameScreen != null) {
                        try {
                            if (this.world.gameScreen.client != null) {
                                Class<?> clientClass = this.world.gameScreen.client.getClass();
                                java.lang.reflect.Method send = clientClass.getMethod("sendMessage", String.class);
                                send.invoke(this.world.gameScreen.client, "TILE_REMOVE:" + worldX + ":" + worldY);
                            } else {
                                com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                                if (s != null) s.broadcastFromServer("TILE_REMOVE:" + worldX + ":" + worldY);
                            }
                        } catch (NoSuchMethodException nsme) {
                            // ignore
                        }
                    }
                } catch (Exception e) {
                    Gdx.app.error("Chunk", "Failed to broadcast floor tile remove", e);
                }
            floorRemoved = true;
        }
        boolean proxiesAdded = false;
        for (int t = this.tilesThatNeedProxies.size() - 1; t >= 0; --t) {
            this.tilesThatNeedProxies.get(t).addProxies();
            this.tilesThatNeedProxies.remove(t);
            proxiesAdded = true;
        }
        boolean tileRemoved = false;
        // iterate tiles with iterator to allow removal during iteration
        this.tileIterator = this.tiles.entrySet().iterator();
        while (this.tileIterator.hasNext()) {
            Map.Entry<String, Tile> tileEntry = this.tileIterator.next();
            tileEntry.getValue().update(delta);
            if (tileEntry.getValue().readyToRemove) {
                // capture world coords before destroying
                int tx = tileEntry.getValue().x;
                int ty = tileEntry.getValue().y;
                int worldX = this.chunkX * 10 + tx;
                int worldY = this.chunkY * 10 + ty;
                tileEntry.getValue().destroy();
                if (tileEntry.getValue().hasPhysicsBody()) {
                    this.world.b2dWorld.destroyBody(tileEntry.getValue().getBody());
                }
                this.tileIterator.remove();
                // notify network about tile removal
                try {
                    if (this.world != null && !this.world.suppressNetworkTileEvents && this.world.gameScreen != null) {
                        try {
                            if (this.world.gameScreen.client != null) {
                                Class<?> clientClass = this.world.gameScreen.client.getClass();
                                java.lang.reflect.Method send = clientClass.getMethod("sendMessage", String.class);
                                send.invoke(this.world.gameScreen.client, "TILE_REMOVE:" + worldX + ":" + worldY);
                            } else {
                                com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                                if (s != null) s.broadcastFromServer("TILE_REMOVE:" + worldX + ":" + worldY);
                            }
                        } catch (NoSuchMethodException nsme) {
                            // ignore
                        }
                    }
                } catch (Exception e) {
                    Gdx.app.error("Chunk", "Failed to broadcast tile remove", e);
                }
                tileRemoved = true;
                continue;
            }
            if (tileEntry.getValue() instanceof ProxyTile) {
                ProxyTile proxy = (ProxyTile)tileEntry.getValue();
                if (proxy.parent == null || proxy.parent.readyToRemove) {
                    tileEntry.getValue().destroy();
                    this.tileIterator.remove();
                    tileRemoved = true;
                }
            }
        }
        if (tileRemoved || floorRemoved || proxiesAdded) {
            this.world.baseManager.updateBases(this.world);
            this.setMapDirty(true);
        }
    }

    public static int getChunkX(int worldX) {
        return MathUtils.floor((float)worldX / 10.0f);
    }

    public static int getChunkY(int worldY) {
        return MathUtils.floor((float)worldY / 10.0f);
    }

    public void dispose() {
        // destroy all tiles and clear maps
        for (Tile t : this.tiles.values()) {
            try { t.destroy(); } catch (Exception ignored) {}
        }
        this.tiles.clear();
        for (GroundTile gt : this.groundTiles.values()) {
            try { gt.destroy(); } catch (Exception ignored) {}
            try { this.world.groundTilePool.free(gt); } catch (Exception ignored) {}
        }
        this.groundTiles.clear();
        this.world = null;
    }

    public void updateAllWalls() {
        for (Tile t : this.tiles.values()) {
            if (t.type == Tile.types.habitat) {
                ((HabitatModule)t).updateWalls();
            }
            if (t instanceof BarrierWall) {
                ((BarrierWall)t).updateWalls();
            }
            if (t instanceof Gate) {
                ((Gate)t).updateWalls();
            }
            if (t instanceof LavaFlow) {
                ((LavaFlow)t).autoTile();
            }
            if (!(t instanceof Conduit)) continue;
            ((Conduit)t).autoTile();
        }
    }

    public void hide() {
        if (this.weatherFx != null) {
            this.weatherFx.setVisible(false);
        }
        this.showTiles(false);
    }

    public void show() {
        if (this.weatherFx != null) {
            this.weatherFx.setVisible(true);
        }
        this.showTiles(true);
    }

    public void showTiles(boolean visible) {
        this.visible = visible;
        for (Map.Entry<String, Tile> entry : this.tiles.entrySet()) {
            entry.getValue().setVisible(visible);
        }
        for (Map.Entry<String, Tile> entry : this.floorTiles.entrySet()) {
            entry.getValue().setVisible(visible);
        }
        for (Map.Entry<String, GroundTile> entry : this.groundTiles.entrySet()) {
            entry.getValue().setVisible(visible);
        }
    }

    public String getKey() {
        return "" + this.chunkX + "," + this.chunkY;
    }

    public void updateAutoTile() {
        for (GroundTile gt : this.groundTiles.values()) {
            gt.autoTile();
        }
    }

    public Vector2 getOriginPos() {
        return new Vector2((float)(this.chunkX * 10) * Tile.TILE_SIZE, (float)(this.chunkY * 10) * Tile.TILE_SIZE);
    }

    public void clearTiles() {
        for (Tile t : this.tiles.values()) {
            t.readyToRemove = true;
        }
        this.updateTiles(Gdx.graphics.getDeltaTime());
        this.setMapDirty(true);
    }

    public void unloadGroundTiles() {
        if (this.groundTiles == null || this.groundTiles.isEmpty()) return;
        java.util.Iterator<Map.Entry<String, GroundTile>> it = this.groundTiles.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, GroundTile> entry = it.next();
            GroundTile gt = entry.getValue();
            try {
                if (gt != null) gt.destroy();
            } catch (Exception ignored) {}
            try {
                if (this.world != null && this.world.groundTilePool != null && gt != null) this.world.groundTilePool.free(gt);
            } catch (Exception ignored) {}
            it.remove();
        }
    }

    public void clearBlockingTerrain() {
        for (Tile t : this.tiles.values()) {
            if (t.type != Tile.types.blockingTerrain) continue;
            t.readyToRemove = true;
        }
        this.updateTiles(Gdx.graphics.getDeltaTime());
    }

    public void updateTileVisibility() {
        for (Map.Entry<String, Tile> entry : this.tiles.entrySet()) {
            entry.getValue().canPlayerSee();
        }
        for (Map.Entry<String, GroundTile> entry : this.groundTiles.entrySet()) {
            entry.getValue().canPlayerSee();
            this.gtDiscoveryArray[entry.getValue().y * 10 + entry.getValue().x] = entry.getValue().isDiscovered();
        }
    }
}
