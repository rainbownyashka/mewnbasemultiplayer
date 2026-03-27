/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.ItemPile;
import com.cairn4.moonbase.tiles.LaunchPad;
import com.cairn4.moonbase.tiles.Tile;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class WorldDebugMap {
    private static int RENDER_WIDTH = 1024;
    private static int RENDER_HEIGHT = 1024;
    private static final Color groundColor = Color.valueOf("675348");
    private static final Color rockColor = Color.valueOf("4e413b");
    private static final Color swampColor = Color.valueOf("585841");
    private static final Color sandColor = Color.valueOf("745f48");
    private static final Color volcanicColor = Color.valueOf("302121");
    private static final Color genericColor = Color.valueOf("675348");

    public static Texture makeMap(HashMap<String, Chunk> chunks) {
        Pixmap map = new Pixmap(RENDER_WIDTH, RENDER_HEIGHT, Pixmap.Format.RGBA8888);
        map.fill();
        for (Chunk c : chunks.values()) {
            WorldDebugMap.renderChunk(map, c);
        }
        WorldDebugMap.writeMapTexture(map, "world");
        return new Texture(map);
    }

    public static Pixmap renderChunk2(Pixmap map, Chunk c) {
        WorldDebugMap.renderChunk(map, c);
        return map;
    }

    private static void renderChunk(Pixmap map, Chunk c) {
        if (c != null) {
            Gdx.app.debug("MewnBase", "Rendering chunk " + c.chunkX + ", " + c.chunkY);
            for (int x = 0; x < 10; ++x) {
                for (int y = 0; y < 10; ++y) {
                    Color drawColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
                    drawColor = WorldDebugMap.getTileColor(c, x, y);
                    map.setColor(drawColor);
                    int pixelX = x;
                    int pixelY = 9 - y;
                    map.drawPixel(pixelX, pixelY);
                }
            }
            c.setMapDirty(false);
        }
    }

    private static Color getTileColor(Chunk c, int x, int y) {
        GroundTile gt;
        Color tileColor;
        Tile t;
        Color drawColor = new Color(0.0f, 0.0f, 0.0f, 0.0f);
        GroundTile.Biomes biome = c.gtBiomeArray[y * 10 + x];
        if (biome == null) {
            Gdx.app.error("MewnBase", "MISSING BIOME AT CHUNK " + c.chunkX + ", " + c.chunkY + " -- " + x + ", " + y);
        }
        switch (biome) {
            case water: {
                drawColor.set(Color.valueOf("08548d"));
                break;
            }
            case mud: {
                drawColor.set(Color.valueOf("443026"));
                break;
            }
            case ground: {
                drawColor.set(groundColor);
                break;
            }
            case green: 
            case swamp: {
                drawColor.set(swampColor);
                break;
            }
            case sand: {
                drawColor.set(sandColor);
                break;
            }
            case rock: {
                drawColor.set(rockColor);
                break;
            }
            case volcanic: {
                drawColor.set(volcanicColor);
                break;
            }
            default: {
                drawColor.set(genericColor);
            }
        }
        Tile ft = c.getFloorTile(x, y);
        if (ft != null) {
            drawColor.set(ft.getMinimapColor());
        }
        if ((t = c.getTile(x, y)) != null && !(t instanceof ItemPile) && (tileColor = t.getMinimapColor()) != null) {
            drawColor.set(tileColor);
        }
        if (!c.gtDiscoveryArray[y * 10 + x] && !MoonBase.DEBUG_INFO) {
            drawColor.set(0.0f, 0.0f, 0.0f, 0.0f);
        }
        if ((gt = c.getGroundTile(x, y)) != null && gt.hasResources()) {
            drawColor = Color.MAGENTA;
        }
        return drawColor;
    }

    private static boolean isBiomeEdge(Chunk c, int x, int y) {
        GroundTile.Biomes biome = c.gtBiomeArray[y * 10 + x];
        int worldX = c.chunkX * 10 + x;
        int worldY = c.chunkY * 10 + y;
        GroundTile north = c.world.getGroundTile(worldX, worldY + 1);
        GroundTile south = c.world.getGroundTile(worldX, worldY - 1);
        GroundTile east = c.world.getGroundTile(worldX + 1, worldY);
        GroundTile west = c.world.getGroundTile(worldX - 1, worldY);
        boolean same = true;
        if (north != null && north.getBiome() != biome) {
            return true;
        }
        if (south != null && south.getBiome() != biome) {
            return true;
        }
        if (east != null && east.getBiome() != biome) {
            return true;
        }
        return west != null && west.getBiome() != biome;
    }

    public static Pixmap renderMegaChunk(Pixmap map, World world, int mX, int mY) {
        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                Chunk c;
                int cX = mX * 10 + x;
                int cY = mY * 10 + y;
                int pxStart = x * 10;
                int pyStart = y * 10;
                if (!world.chunkExists(cX, cY) || (c = world.getChunk(cX, cY)) == null) continue;
                for (int px = 0; px < 10; ++px) {
                    for (int py = 0; py < 10; ++py) {
                        Color drawColor = new Color(0.0f, 0.0f, 0.0f, 0.0f);
                        drawColor = WorldDebugMap.getTileColor(c, px, py);
                        map.setColor(drawColor);
                        int pixelX = pxStart + px;
                        int pixelY = 99 - py - pyStart;
                        map.drawPixel(pixelX, pixelY);
                    }
                }
            }
        }
        return map;
    }

    private static void writeMapTexture(Pixmap map, String fileName) {
        FileHandle image = Gdx.files.local("world/map/" + fileName + ".png");
        OutputStream stream = image.write(false);
        try {
            PixmapIO.writePNG(image, map);
            stream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Pixmap renderBlankChunk() {
        Pixmap map = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                Color drawColor = new Color(0.0f, 0.0f, 0.0f, 0.5f);
                if (x == 0 && y == 0) {
                    drawColor = new Color(0.5f, 0.5f, 1.0f, 0.25f);
                }
                map.setColor(drawColor);
                int pixelX = x;
                int pixelY = 9 - y;
                map.drawPixel(pixelX, pixelY);
            }
        }
        return map;
    }

    public static Texture getLaunchPadMap(LaunchPad launchPad) {
        Pixmap pixmap = new Pixmap(30, 20, Pixmap.Format.RGB888);
        Texture t = new Texture(WorldDebugMap.renderLaunchPad(pixmap, launchPad, 30, 20));
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }

    private static Pixmap renderLaunchPad(Pixmap map, LaunchPad lpad, int w, int h) {
        int rX = MathUtils.floor((float)w / 2.0f);
        int rY = MathUtils.floor((float)h / 2.0f);
        int startX = lpad.worldX - rX;
        int startY = lpad.worldY - rY;
        MoonBase.log("Launchpad " + lpad.worldX + ", " + lpad.worldY);
        MoonBase.log("map startx: " + startX);
        MoonBase.log("map starty: " + startY);
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                int worldX = startX + x;
                int worldY = startY + y;
                GridPoint2 chunkCoord = World.gridPointPool.obtain();
                World.convertWorldPosToChunkCoord(chunkCoord, worldX, worldY);
                Chunk chunk = lpad.world.getChunk(chunkCoord.x, chunkCoord.y);
                if (chunk == null) continue;
                Color drawColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
                GridPoint2 localPos = World.gridPointPool.obtain();
                World.convertWorldToLocal(localPos, worldX, worldY);
                drawColor = WorldDebugMap.getTileColor(chunk, localPos.x, localPos.y);
                map.setColor(drawColor);
                int pixelX = x;
                int pixelY = h - 1 - y;
                map.drawPixel(pixelX, pixelY);
            }
        }
        return map;
    }
}

