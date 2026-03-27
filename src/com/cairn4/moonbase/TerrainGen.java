/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.DistanceModifier;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.SimplexNoise;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.NpcManager;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.LavaGeyser;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.Volcano;
import com.cairn4.moonbase.worlddata.FeatureGenData;
import com.cairn4.moonbase.worlddata.FeatureTileFactory;
import com.cairn4.moonbase.worlddata.ItemDropperData;
import com.cairn4.moonbase.worlddata.ItemDropperFactory;
import com.cairn4.moonbase.worlddata.ItemDropperSpawnBiome;
import com.cairn4.moonbase.worlddata.ItemDropperVariationData;
import com.cairn4.moonbase.worlddata.LavaFlowGenerator;
import java.util.ArrayList;
import java.util.List;
import za.co.luma.geom.Vector2DDouble;
import za.co.luma.math.sampling.UniformPoissonDiskSampler;

public class TerrainGen {
    private SimplexNoise simplexNoiseAlt;
    private SimplexNoise simplexNoiseWet;
    private int seed;
    private int seedWet;
    private final float altitudeNoise = 0.015f;
    private final float altitudeSmoothing = 1.2f;
    private final float wetnessNoise = 0.02f;
    private final float wetnessSmoothing = 1.2f;
    World world;
    float[][] noise;
    float[][] altitude;
    float[][] wetness;
    float x;
    float y = 10.0f;
    float z;
    float lx;
    float ly = -1.0f;
    float lz;
    Array<GridPoint2> chunkCoords = new Array();
    private static final int minDistBetweenVolcanos = 18;
    public ArrayList<GridPoint2> volcanoGridPoints = new ArrayList();
    private static final int minDistBetweenLavaFlows = 20;
    public ArrayList<GridPoint2> lavaFlowGridPoints = new ArrayList();
    private static final int minDistBetweenResearchObjects = 18;
    public ArrayList<GridPoint2> artifactGridPoints = new ArrayList();
    private static final int minDistBetweenNPCs = 18;
    public ArrayList<GridPoint2> npcGridPoints = new ArrayList();
    ArrayList<GridPoint2> lavaFlowNeighborCoordList = new ArrayList();
    ArrayList<GridPoint2> volcanoNeighborCoordList = new ArrayList();
    float mingen = 99999.0f;
    float maxgen = -99999.0f;

    public TerrainGen(World world) {
        this.world = world;
        this.seed = MathUtils.random(10000);
        this.simplexNoiseAlt = new SimplexNoise(this.seed);
        this.seedWet = this.seed + 1;
        this.simplexNoiseWet = new SimplexNoise(this.seedWet);
        this.chunkCoords.clear();
        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                this.chunkCoords.add(new GridPoint2(x, y));
            }
        }
        this.chunkCoords.shuffle();
        this.setupUniformPoissonPositions(800, this.volcanoGridPoints, 18);
        this.setupUniformPoissonPositions(800, this.artifactGridPoints, 18);
        this.setupUniformPoissonPositions(800, this.npcGridPoints, 18);
    }

    public int getSeed() {
        return this.seed;
    }

    public int getSeedWet() {
        return this.seedWet;
    }

    public void setSeed(int seedAlt) {
        this.seed = seedAlt;
        this.simplexNoiseAlt = new SimplexNoise(this.seed);
        MoonBase.log("TerrainGen: Setting terrain seed to " + this.seed);
        this.seedWet = seedAlt + 1;
        this.simplexNoiseWet = new SimplexNoise(this.seedWet);
        MoonBase.log("TerrainGen: Setting terrain wet seed to " + this.seedWet);
    }

    public void newSeed() {
        this.seed = MathUtils.random(10000);
        this.seedWet = MathUtils.random(10000);
        this.simplexNoiseAlt = new SimplexNoise(this.seed);
        this.simplexNoiseWet = new SimplexNoise(this.seedWet);
        Gdx.app.log("MoonBase", "TerrainGen: creating new seed for old game save: " + this.seed);
    }

    private void setupUniformPoissonPositions(int radius, ArrayList<GridPoint2> featureGridPointList, int distanceBetweenObjects) {
        UniformPoissonDiskSampler sampler = new UniformPoissonDiskSampler(500 - radius, 500 - radius, 500 + radius, 500 + radius, distanceBetweenObjects);
        List pointList = sampler.sample();
        featureGridPointList.clear();
        for (Vector2DDouble v : pointList) {
            featureGridPointList.add(new GridPoint2((int)v.x, (int)v.y));
        }
    }

    public void createChunkTerrain(Chunk chunk) {
        this.paintGround(chunk);
        for (FeatureGenData fd : FeatureTileFactory.getInstance().featureGenDataList) {
            this.placeFeatures(chunk, fd);
        }
        for (ItemDropperData idd : ItemDropperFactory.getInstance().itemDropperDataList) {
            if (!idd.naturallyOccurring) continue;
            this.placeItemDropper(chunk, idd);
        }
        this.placeSamples(chunk);
        this.placeVolcanos(chunk);
    }

    private void placeSamples(Chunk chunk) {
        int chunkMinX = chunk.chunkX * 10;
        int chunkMaxX = chunkMinX + 10;
        int chunkMinY = chunk.chunkY * 10;
        int chunkMaxY = chunkMinY + 10;
        ItemDropperData researchObjectData = ItemDropperFactory.getInstance().getItemDropperData("researchObject");
        boolean sampleInThisChunk = false;
        for (int i = this.artifactGridPoints.size() - 1; i >= 0; --i) {
            GridPoint2 s = this.artifactGridPoints.get(i);
            if (sampleInThisChunk || s.x < chunkMinX || s.x > chunkMaxX || s.y < chunkMinY || s.y > chunkMaxY) continue;
            GridPoint2 gp = new GridPoint2(0, 0);
            gp = World.convertWorldToLocal(gp, s.x, s.y);
            if (this.world.isTileEmpty(s.x, s.y)) {
                ItemDropperFactory.getInstance().newDropper(this.world, chunk, gp.x, gp.y, "researchObject");
                sampleInThisChunk = true;
            }
            this.artifactGridPoints.remove(i);
        }
    }

    private void placeLavaFlows(Chunk chunk) {
        int chunkMinX = chunk.chunkX * 10;
        int chunkMaxX = chunkMinX + 10;
        int chunkMinY = chunk.chunkY * 10;
        int chunkMaxY = chunkMinY + 10;
        boolean existsInThisChunk = false;
        for (int i = this.lavaFlowGridPoints.size() - 1; i >= 0; --i) {
            GridPoint2 s = this.lavaFlowGridPoints.get(i);
            if (existsInThisChunk || s.x < chunkMinX || s.x > chunkMaxX || s.y < chunkMinY || s.y > chunkMaxY) continue;
            GridPoint2 gp = new GridPoint2(0, 0);
            gp = World.convertWorldToLocal(gp, s.x, s.y);
            if (gp.x == 0) {
                ++gp.x;
            }
            if (gp.x == 9) {
                --gp.x;
            }
            if (gp.y == 0) {
                ++gp.y;
            }
            if (gp.y == 9) {
                --gp.y;
            }
            MoonBase.error("------------------\nSPAWNING LAVA FLOW: " + s.x + ", " + s.y);
            this.spawnLavaFlow(s.x, s.y);
            existsInThisChunk = true;
            this.lavaFlowGridPoints.remove(i);
        }
    }

    private boolean canPlaceLavaFlowHere(Chunk chunk, int localX, int localY) {
        this.lavaFlowNeighborCoordList.clear();
        this.lavaFlowNeighborCoordList = Tile.GET_NEARBY_COORDS(localX, localY, 3);
        int i = 0;
        for (GridPoint2 gp : this.lavaFlowNeighborCoordList) {
            GroundTile gt = chunk.getGroundTile(localX, localY);
            if (gt != null) {
                if (gt.getBiome() != GroundTile.Biomes.volcanic) {
                    return false;
                }
            } else {
                return false;
            }
            ++i;
        }
        return true;
    }

    private void placeVolcanos(Chunk chunk) {
        int chunkMinX = chunk.chunkX * 10;
        int chunkMaxX = chunkMinX + 10;
        int chunkMinY = chunk.chunkY * 10;
        int chunkMaxY = chunkMinY + 10;
        boolean volcanosInThisChunk = false;
        for (int i = this.volcanoGridPoints.size() - 1; i >= 0; --i) {
            GridPoint2 s = this.volcanoGridPoints.get(i);
            if (volcanosInThisChunk || s.x < chunkMinX || s.x > chunkMaxX || s.y < chunkMinY || s.y > chunkMaxY) continue;
            GridPoint2 gp = new GridPoint2(0, 0);
            gp = World.convertWorldToLocal(gp, s.x, s.y);
            if (gp.x == 0) {
                ++gp.x;
            }
            if (gp.x == 9) {
                --gp.x;
            }
            if (gp.y == 0) {
                ++gp.y;
            }
            if (gp.y == 9) {
                --gp.y;
            }
            if (this.canPlaceVolcanoHere(chunk, gp.x, gp.y)) {
                MoonBase.log("Spawning volcano at " + s.x + ", " + s.y);
                Volcano v = new Volcano(this.world, chunk, gp.x, gp.y);
                volcanosInThisChunk = true;
            }
            this.volcanoGridPoints.remove(i);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private boolean canPlaceVolcanoHere(Chunk chunk, int localX, int localY) {
        this.volcanoNeighborCoordList.clear();
        this.volcanoNeighborCoordList = Tile.GET_ADJACENT_COORDS(localX, localY, true);
        int i = 0;
        for (GridPoint2 gp : this.volcanoNeighborCoordList) {
            if (!chunk.isTileEmpty(localX, localY)) return false;
            GroundTile gt = chunk.getGroundTile(localX, localY);
            if (gt == null) return false;
            if (gt.getBiome() != GroundTile.Biomes.volcanic) {
                return false;
            }
            ++i;
        }
        return true;
    }

    private void placeNpcs(Chunk chunk) {
        int chunkMinX = chunk.chunkX * 10;
        int chunkMaxX = chunkMinX + 10;
        int chunkMinY = chunk.chunkY * 10;
        int chunkMaxY = chunkMinY + 10;
        boolean npcInThisChunk = false;
        for (int i = this.npcGridPoints.size() - 1; i >= 0; --i) {
            GridPoint2 s = this.npcGridPoints.get(i);
            if (npcInThisChunk || s.x < chunkMinX || s.x > chunkMaxX || s.y < chunkMinY || s.y > chunkMaxY) continue;
            if (this.world.isTileEmpty(s.x, s.y)) {
                String npcId = NpcManager.getInstance().getAvailableNpcId(s.x, s.y);
                if (npcId != null) {
                    float xPos = (float)s.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f;
                    float yPos = (float)s.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f;
                    Npc n = new Npc(this.world, xPos, yPos);
                    n.npcId = npcId;
                    n.init();
                    GridPoint2 npcLocalPos = World.gridPointPool.obtain();
                    npcLocalPos = World.convertWorldToLocal(npcLocalPos, s.x, s.y);
                    Gdx.app.log("MewnBase", "Spawning NPC in chunk " + chunk.chunkX + ", " + chunk.chunkY + " -- local: " + npcLocalPos);
                    npcInThisChunk = true;
                } else {
                    System.out.println("Couldn't spawn an NPC here.");
                }
            }
            this.npcGridPoints.remove(i);
        }
    }

    public void paintGround(final Chunk chunk) {
        this.altitude = this.generateNoiseAlt(0.015f, 1.2f, chunk.chunkX * 10, chunk.chunkY * 10, new DistanceModifier(){

            @Override
            public float distanceMod(float x, float y, float value) {
                return TerrainGen.this.safeSpawnAltitude(x += (float)(chunk.chunkX * 10), y += (float)(chunk.chunkY * 10), value);
            }
        });
        this.wetness = this.generateNoiseWet(0.02f, 1.2f, chunk.chunkX * 10, chunk.chunkY * 10, new DistanceModifier(){

            @Override
            public float distanceMod(float x, float y, float value) {
                return TerrainGen.this.safeSpawnWetness(x += (float)(chunk.chunkX * 10), y += (float)(chunk.chunkY * 10), value);
            }
        });
        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                GroundTile gt = chunk.getGroundTile(x, y);
                gt.calcBiome(this.altitude[x][y], this.wetness[x][y]);
                gt.createDrawables();
                chunk.gtBiomeArray[y * 10 + x] = gt.getBiome();
                chunk.gtDiscoveryArray[y * 10 + x] = false;
            }
        }
    }

    private float safeSpawnAltitude(float x, float y, float value) {
        float endValue = value;
        float distFromCenter = Vector2.dst(x, y, 500.0f, 500.0f);
        if (distFromCenter < 100.0f) {
            endValue = MathUtils.clamp(endValue, -2.0f, GroundTile.altThresholds[2]);
        } else if (distFromCenter >= 100.0f && distFromCenter < 300.0f) {
            float alpha = (float)Math.pow((distFromCenter - 100.0f) / 300.0f, 4.0);
            endValue += alpha * 0.35f;
        } else {
            endValue += 0.35f;
        }
        return endValue;
    }

    private float safeSpawnWetness(float x, float y, float value) {
        float endValue = value;
        float distFromCenter = Vector2.dst(x, y, 500.0f, 500.0f);
        if (distFromCenter < 100.0f) {
            endValue = MathUtils.clamp(endValue, -2.0f, GroundTile.wetThresholds[0]);
        } else if (!(distFromCenter >= 100.0f) || distFromCenter < 600.0f) {
            // empty if block
        }
        return endValue;
    }

    private void placeItemDropper(Chunk chunk, ItemDropperData idd) {
        this.noise = this.generateNoiseAlt(idd.noiseFeature, idd.noiseSmooth, chunk.chunkX * 10, chunk.chunkY * 10, null);
        int numInChunk = 1;
        int limit = idd.maxPerChunk == 0 ? 100 : idd.maxPerChunk;
        this.chunkCoords.shuffle();
        block0: for (GridPoint2 gp : this.chunkCoords) {
            int x = gp.x;
            int y = gp.y;
            if (!chunk.isTileEmpty(x, y)) continue;
            for (ItemDropperSpawnBiome biome : idd.spawnBiomes) {
                GroundTile.Biomes b = GroundTile.Biomes.valueOf(biome.biome);
                if (chunk.getGroundTile(x, y).getBiome() != b || !(this.noise[x][y] > biome.threshold)) continue;
                if (numInChunk <= limit) {
                    boolean usingVariation = false;
                    if (idd.dropperVariations.size() > 0) {
                        for (ItemDropperVariationData idvd : idd.dropperVariations) {
                            if (!(MathUtils.random() < idvd.chance)) continue;
                            MoonBase.debug("TerrainGen: using dropper variation for " + idd.id + " -> " + idvd.id);
                            ItemDropperFactory.getInstance().newDropper(this.world, chunk, x, y, idvd.id);
                            ++numInChunk;
                            usingVariation = true;
                            break;
                        }
                    }
                    if (usingVariation) continue;
                    ItemDropperFactory.getInstance().newDropper(this.world, chunk, x, y, idd.id);
                    ++numInChunk;
                    continue;
                }
                MoonBase.debug("TerrainGen: Reached chunk limit for " + idd.id + " (" + limit + ")");
                continue block0;
            }
        }
    }

    private void placeFeatures(Chunk chunk, FeatureGenData idd) {
        this.noise = this.generateNoiseAlt(idd.noiseFeature, idd.noiseSmooth, chunk.chunkX * 10, chunk.chunkY * 10, null);
        int numInChunk = 1;
        int limit = idd.maxPerChunk == 0 ? 100 : idd.maxPerChunk;
        this.chunkCoords.shuffle();
        block0: for (GridPoint2 gp : this.chunkCoords) {
            int x = gp.x;
            int y = gp.y;
            if (!chunk.isTileEmpty(x, y)) continue;
            for (ItemDropperSpawnBiome biome : idd.spawnBiomes) {
                GroundTile.Biomes b = GroundTile.Biomes.valueOf(biome.biome);
                GroundTile gt = chunk.getGroundTile(x, y);
                if (gt.getBiome() != b || !(this.noise[x][y] > biome.threshold) || !(gt.altitude >= idd.minAltitude)) continue;
                if (numInChunk <= limit) {
                    this.spawnFeature(idd.id, chunk, x, y);
                    ++numInChunk;
                    continue;
                }
                System.out.println("Reached chunk limit for " + idd.id + "(" + limit + ")");
                continue block0;
            }
        }
    }

    private GroundTile.Biomes getBiome(float val) {
        if (val >= -1.0f && val <= -0.6f) {
            return GroundTile.Biomes.water;
        }
        if (val > -0.6f && val <= -0.4f) {
            return GroundTile.Biomes.swamp;
        }
        if (val > -0.4f && val <= 0.7f) {
            return GroundTile.Biomes.ground;
        }
        return null;
    }

    private float[][] generateNoiseAlt(float features, float smoothing, int startX, int startY, DistanceModifier distanceMod) {
        this.simplexNoiseAlt = new SimplexNoise(this.seed);
        int width = 10;
        int height = 10;
        float layerF = features;
        float weight = smoothing;
        float[][] noise = new float[width][height];
        for (int i = 0; i < 3; ++i) {
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    float[] fArray = noise[x];
                    int n = y;
                    fArray[n] = fArray[n] + (float)SimplexNoise.noise((float)(x + startX) * layerF, (float)(y + startY) * layerF) * weight;
                    if (distanceMod != null) {
                        noise[x][y] = distanceMod.distanceMod(x, y, noise[x][y]);
                    }
                    noise[x][y] = MathUtils.clamp(noise[x][y], -1.0f, 1.0f);
                    if (noise[x][y] < this.mingen) {
                        this.mingen = noise[x][y];
                    }
                    if (!(noise[x][y] > this.maxgen)) continue;
                    this.maxgen = noise[x][y];
                }
            }
            layerF *= 3.5f;
            weight *= 0.5f;
        }
        return noise;
    }

    private float[][] generateNoiseWet(float features, float smoothing, int startX, int startY, DistanceModifier distanceMod) {
        this.simplexNoiseWet = new SimplexNoise(this.seedWet);
        int width = 10;
        int height = 10;
        float layerF = features;
        float weight = smoothing;
        float[][] noise = new float[width][height];
        for (int i = 0; i < 3; ++i) {
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    float[] fArray = noise[x];
                    int n = y;
                    fArray[n] = fArray[n] + (float)SimplexNoise.noise((float)(x + startX) * layerF, (float)(y + startY) * layerF) * weight;
                    if (distanceMod != null) {
                        noise[x][y] = distanceMod.distanceMod(x, y, noise[x][y]);
                    }
                    noise[x][y] = MathUtils.clamp(noise[x][y], -1.0f, 1.0f);
                    if (noise[x][y] < this.mingen) {
                        this.mingen = noise[x][y];
                    }
                    if (!(noise[x][y] > this.maxgen)) continue;
                    this.maxgen = noise[x][y];
                }
            }
            layerF *= 3.5f;
            weight *= 0.25f;
        }
        return noise;
    }

    public void spawnFeature(String featureId, Chunk chunk, int x, int y) {
        MoonBase.debug("Feature spawn: " + featureId);
        switch (featureId) {
            case "lava-geyser": {
                new LavaGeyser(this.world, chunk, x, y);
            }
        }
    }

    public void spawnLavaFlow(int worldX, int worldY) {
        LavaFlowGenerator lvg = new LavaFlowGenerator();
        lvg.newGen(this.world, worldX, worldY);
        this.world.updateAllWalls();
    }
}

