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
    private static final float ICE_SPAWN_SAFE_RADIUS = 360.0f;
    private static final int ICE_VOLCANIC_BUFFER_TILES = 4;
    private static final int ICE_GROWTH_NEIGHBOR_THRESHOLD = 2;
    private SimplexNoise simplexNoiseAlt;
    private SimplexNoise simplexNoiseWet;
    private SimplexNoise simplexNoiseTemp;
    private int seed;
    private int seedWet;
    private int seedTemp;
    private float altitudeNoise = 0.015f;
    private float altitudeSmoothing = 1.2f;
    private float wetnessNoise = 0.02f;
    private float wetnessSmoothing = 1.2f;
    private float temperatureNoise = 0.01f;
    private float temperatureSmoothing = 1.0f;
    private float altitudeBias = 0.0f;
    private float wetnessBias = 0.0f;
    private float temperatureBias = 0.0f;
    private boolean enableVolcanos = true;
    private boolean enableSamples = true;
    private boolean enableNpcs = true;
    private String planetType = "default";
    World world;
    float[][] noise;
    float[][] altitude;
    float[][] wetness;
    float[][] temperature;
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
        try {
            if (MoonBase.getCurrentMission() != null && MoonBase.getCurrentMission().planetType != null) {
                this.planetType = MoonBase.getCurrentMission().planetType;
            }
        } catch (Exception ignored) {}
        this.applyPlanetProfile(this.planetType);
        this.seed = MathUtils.random(10000);
        this.simplexNoiseAlt = new SimplexNoise(this.seed);
        this.seedWet = this.seed + 1;
        this.simplexNoiseWet = new SimplexNoise(this.seedWet);
        this.seedTemp = this.seed + 2;
        this.simplexNoiseTemp = new SimplexNoise(this.seedTemp);
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

    private void applyPlanetProfile(String type) {
        String t = type == null ? "default" : type.trim();
        this.planetType = t;
        // Defaults (original)
        altitudeNoise = 0.015f;
        altitudeSmoothing = 1.2f;
        wetnessNoise = 0.02f;
        wetnessSmoothing = 1.2f;
        temperatureNoise = 0.01f;
        temperatureSmoothing = 1.0f;
        altitudeBias = 0.0f;
        wetnessBias = 0.0f;
        temperatureBias = 0.0f;
        enableVolcanos = true;
        enableSamples = true;
        enableNpcs = true;
        if ("crimson".equalsIgnoreCase(t) || "red".equalsIgnoreCase(t)) {
            altitudeNoise = 0.01f;
            altitudeSmoothing = 1.6f;
            wetnessNoise = 0.012f;
            wetnessSmoothing = 0.6f;
            altitudeBias = 0.15f;
            wetnessBias = -0.35f;
            temperatureBias = 0.25f;
            enableVolcanos = true;
            enableSamples = true;
            enableNpcs = false;
        } else if ("ash".equalsIgnoreCase(t)) {
            altitudeNoise = 0.02f;
            altitudeSmoothing = 1.0f;
            wetnessNoise = 0.01f;
            wetnessSmoothing = 0.4f;
            altitudeBias = 0.25f;
            wetnessBias = -0.5f;
            temperatureBias = 0.35f;
            enableVolcanos = true;
            enableSamples = true;
            enableNpcs = false;
        }
    }

    public void setPlanetType(String type) {
        this.applyPlanetProfile(type);
    }

    public void applyPlanetGenConfig(com.cairn4.moonbase.worlddata.PlanetGenConfig cfg) {
        if (cfg == null) return;
        if (cfg.planetType != null && cfg.planetType.length() > 0) {
            this.applyPlanetProfile(cfg.planetType);
        }
        if (cfg.altitudeNoise > 0.0f) this.altitudeNoise = cfg.altitudeNoise;
        if (cfg.altitudeSmoothing > 0.0f) this.altitudeSmoothing = cfg.altitudeSmoothing;
        if (cfg.wetnessNoise > 0.0f) this.wetnessNoise = cfg.wetnessNoise;
        if (cfg.wetnessSmoothing > 0.0f) this.wetnessSmoothing = cfg.wetnessSmoothing;
        if (cfg.temperatureNoise > 0.0f) this.temperatureNoise = cfg.temperatureNoise;
        if (cfg.temperatureSmoothing > 0.0f) this.temperatureSmoothing = cfg.temperatureSmoothing;
        this.altitudeBias = cfg.altitudeBias;
        this.wetnessBias = cfg.wetnessBias;
        this.temperatureBias = cfg.temperatureBias;
        this.enableVolcanos = cfg.enableVolcanos;
        this.enableSamples = cfg.enableSamples;
        this.enableNpcs = cfg.enableNpcs;
        if (cfg.seed > 0) {
            this.setSeed(cfg.seed);
        }
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
        this.seedTemp = seedAlt + 2;
        this.simplexNoiseTemp = new SimplexNoise(this.seedTemp);
        MoonBase.log("TerrainGen: Setting terrain temp seed to " + this.seedTemp);
    }

    public void newSeed() {
        this.seed = MathUtils.random(10000);
        this.seedWet = MathUtils.random(10000);
        this.simplexNoiseAlt = new SimplexNoise(this.seed);
        this.simplexNoiseWet = new SimplexNoise(this.seedWet);
        this.seedTemp = MathUtils.random(10000);
        this.simplexNoiseTemp = new SimplexNoise(this.seedTemp);
        Gdx.app.log("MoonBase", "TerrainGen: creating new seed for old game save: " + this.seed);
    }

    private void setupUniformPoissonPositions(int radius, ArrayList<GridPoint2> featureGridPointList, int distanceBetweenObjects) {
        UniformPoissonDiskSampler sampler = new UniformPoissonDiskSampler(500 - radius, 500 - radius, 500 + radius, 500 + radius, distanceBetweenObjects);
        List pointList = sampler.sample();
        featureGridPointList.clear();
        for (Object obj : pointList) {
            Vector2DDouble v = (Vector2DDouble)obj;
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
        if (enableSamples) this.placeSamples(chunk);
        if (enableVolcanos) this.placeVolcanos(chunk);
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
        this.altitude = this.generateNoiseAlt(this.altitudeNoise, this.altitudeSmoothing, chunk.chunkX * 10, chunk.chunkY * 10, new DistanceModifier(){

            @Override
            public float distanceMod(float x, float y, float value) {
                return TerrainGen.this.safeSpawnAltitude(x += (float)(chunk.chunkX * 10), y += (float)(chunk.chunkY * 10), value);
            }
        });
        this.wetness = this.generateNoiseWet(this.wetnessNoise, this.wetnessSmoothing, chunk.chunkX * 10, chunk.chunkY * 10, new DistanceModifier(){

            @Override
            public float distanceMod(float x, float y, float value) {
                return TerrainGen.this.safeSpawnWetness(x += (float)(chunk.chunkX * 10), y += (float)(chunk.chunkY * 10), value);
            }
        });
        this.temperature = this.generateNoiseTemp(this.temperatureNoise, this.temperatureSmoothing, chunk.chunkX * 10, chunk.chunkY * 10, new DistanceModifier(){

            @Override
            public float distanceMod(float x, float y, float value) {
                return TerrainGen.this.safeSpawnTemperature(x += (float)(chunk.chunkX * 10), y += (float)(chunk.chunkY * 10), value);
            }
        });
        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                GroundTile gt = chunk.getGroundTile(x, y);
                gt.calcBiome(this.altitude[x][y], this.wetness[x][y], this.temperature[x][y]);
                gt.createDrawables();
                chunk.gtBiomeArray[y * 10 + x] = gt.getBiome();
                chunk.gtDiscoveryArray[y * 10 + x] = false;
            }
        }
        this.refineIceBiome(chunk);
        // Enforce separation between ice and volcanic after initial paint
        this.resolveIceVolcanicAdjacency(chunk);
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

    private float safeSpawnTemperature(float x, float y, float value) {
        float endValue = value;
        float distFromCenter = Vector2.dst(x, y, 500.0f, 500.0f);
        // Smoothly bias temperature towards neutral around spawn to avoid lava/ice there
        float radius = 360.0f;
        if (distFromCenter < radius) {
            float t = 1.0f - (distFromCenter / radius);
            // ease curve for softer falloff
            t = t * t * (3.0f - 2.0f * t);
            endValue = endValue * (1.0f - t);
        }
        return endValue;
    }

    private void refineIceBiome(Chunk chunk) {
        java.util.ArrayList<GridPoint2> toIce = new java.util.ArrayList<>();
        java.util.ArrayList<GridPoint2> toBase = new java.util.ArrayList<>();
        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                GroundTile gt = chunk.getGroundTile(x, y);
                if (gt == null) continue;
                int worldX = chunk.chunkX * 10 + x;
                int worldY = chunk.chunkY * 10 + y;
                if (gt.getBiome() == GroundTile.Biomes.ice) {
                    if (!this.isIceAllowedAt(worldX, worldY, gt)) {
                        toBase.add(new GridPoint2(x, y));
                    }
                    continue;
                }
                if (this.canGrowIceAt(worldX, worldY, gt) && this.countIceNeighbors(worldX, worldY) >= ICE_GROWTH_NEIGHBOR_THRESHOLD) {
                    toIce.add(new GridPoint2(x, y));
                }
            }
        }
        boolean changedAny = false;
        for (GridPoint2 gp : toBase) {
            GroundTile gt = chunk.getGroundTile(gp.x, gp.y);
            if (gt == null) continue;
            GroundTile.Biomes baseBiome = GroundTile.calcBaseBiomeTest(gt.altitude, gt.wetness, gt.temperature);
            if (baseBiome != gt.getBiome()) {
                gt.setBiome(baseBiome);
                gt.createDrawables();
                chunk.gtBiomeArray[gp.y * 10 + gp.x] = gt.getBiome();
                changedAny = true;
            }
        }
        for (GridPoint2 gp : toIce) {
            GroundTile gt = chunk.getGroundTile(gp.x, gp.y);
            if (gt == null || gt.getBiome() == GroundTile.Biomes.ice) continue;
            gt.setBiome(GroundTile.Biomes.ice);
            gt.createDrawables();
            chunk.gtBiomeArray[gp.y * 10 + gp.x] = gt.getBiome();
            changedAny = true;
        }
        if (changedAny) {
            try { chunk.updateAutoTile(); } catch (Exception ignored) {}
            try { chunk.setMapDirty(true); } catch (Exception ignored) {}
        }
    }

    private boolean canGrowIceAt(int worldX, int worldY, GroundTile gt) {
        if (gt == null || gt.getBiome() == GroundTile.Biomes.ice) {
            return false;
        }
        if (!this.isIceAllowedAt(worldX, worldY, gt)) {
            return false;
        }
        return gt.tempIndex <= 1;
    }

    private boolean isIceAllowedAt(int worldX, int worldY, GroundTile gt) {
        if (gt == null) {
            return false;
        }
        if (Vector2.dst(worldX, worldY, 500.0f, 500.0f) < ICE_SPAWN_SAFE_RADIUS) {
            return false;
        }
        GroundTile.Biomes baseBiome = GroundTile.calcBaseBiomeTest(gt.altitude, gt.wetness, gt.temperature);
        if (baseBiome == GroundTile.Biomes.water || baseBiome == GroundTile.Biomes.volcanic) {
            return false;
        }
        return !this.isBiomeWithinRadius(worldX, worldY, GroundTile.Biomes.volcanic, ICE_VOLCANIC_BUFFER_TILES);
    }

    private int countIceNeighbors(int worldX, int worldY) {
        int count = 0;
        if (this.isBiomeAt(worldX + 1, worldY, GroundTile.Biomes.ice)) ++count;
        if (this.isBiomeAt(worldX - 1, worldY, GroundTile.Biomes.ice)) ++count;
        if (this.isBiomeAt(worldX, worldY + 1, GroundTile.Biomes.ice)) ++count;
        if (this.isBiomeAt(worldX, worldY - 1, GroundTile.Biomes.ice)) ++count;
        return count;
    }

    private boolean isBiomeWithinRadius(int worldX, int worldY, GroundTile.Biomes biome, int radius) {
        for (int ox = -radius; ox <= radius; ++ox) {
            for (int oy = -radius; oy <= radius; ++oy) {
                if (Math.abs(ox) + Math.abs(oy) > radius) continue;
                if (this.isBiomeAt(worldX + ox, worldY + oy, biome)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBiomeAt(int worldX, int worldY, GroundTile.Biomes biome) {
        GroundTile gt = this.world.getGroundTile(worldX, worldY);
        return gt != null && gt.getBiome() == biome;
    }

    private boolean usesSparsePeakPlacement(String id) {
        if (id == null) {
            return false;
        }
        String lower = id.toLowerCase();
        return lower.equals("ore")
                || lower.equals("ore-big")
                || lower.equals("crystal")
                || lower.equals("special-ore")
                || lower.equals("rubble")
                || lower.equals("rock")
                || lower.equals("rock_volcanic")
                || lower.equals("rock-rad")
                || lower.equals("rock-sp");
    }

    private int getSparsePeakRadius(String id) {
        if (id == null) {
            return 1;
        }
        String lower = id.toLowerCase();
        if (lower.startsWith("rock")) {
            return 2;
        }
        return 1;
    }

    private int getSparseSpacingRadius(String id) {
        if (id == null) {
            return 2;
        }
        String lower = id.toLowerCase();
        if (lower.equals("crystal") || lower.equals("special-ore") || lower.equals("rock_volcanic")) {
            return 3;
        }
        if (lower.startsWith("rock")) {
            return 2;
        }
        return 2;
    }

    private boolean isSameSparseResourceFamily(String idA, String idB) {
        if (idA == null || idB == null) {
            return false;
        }
        String a = idA.toLowerCase();
        String b = idB.toLowerCase();
        if (a.startsWith("ore") && b.startsWith("ore")) {
            return true;
        }
        if (a.startsWith("rock") && b.startsWith("rock")) {
            return true;
        }
        return a.equals(b);
    }

    private float sampleDropperNoise(ItemDropperData idd, int worldX, int worldY) {
        float layerF = idd.noiseFeature;
        float weight = idd.noiseSmooth;
        float value = 0.0f;
        for (int i = 0; i < 3; ++i) {
            value += (float)SimplexNoise.noise((float)worldX * layerF, (float)worldY * layerF) * weight;
            value = MathUtils.clamp(value + this.altitudeBias, -1.0f, 1.0f);
            layerF *= 3.5f;
            weight *= 0.5f;
        }
        return value;
    }

    private float sparseNoiseScore(ItemDropperData idd, int worldX, int worldY) {
        long hash = (long)worldX * 73428767L ^ (long)worldY * 912931L ^ (long)this.seed * 31L;
        float jitter = (float)(hash & 1023L) / 1023.0f * 0.0005f;
        return this.sampleDropperNoise(idd, worldX, worldY) + jitter;
    }

    private boolean isSparsePeak(ItemDropperData idd, int worldX, int worldY) {
        int radius = this.getSparsePeakRadius(idd.id);
        float score = this.sparseNoiseScore(idd, worldX, worldY);
        for (int ox = -radius; ox <= radius; ++ox) {
            for (int oy = -radius; oy <= radius; ++oy) {
                if (ox == 0 && oy == 0) {
                    continue;
                }
                if (Math.max(Math.abs(ox), Math.abs(oy)) > radius) {
                    continue;
                }
                if (this.sparseNoiseScore(idd, worldX + ox, worldY + oy) >= score) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasNearbySparseResourceFamily(int worldX, int worldY, String id) {
        int radius = this.getSparseSpacingRadius(id);
        for (int ox = -radius; ox <= radius; ++ox) {
            for (int oy = -radius; oy <= radius; ++oy) {
                if (ox == 0 && oy == 0) {
                    continue;
                }
                if (Math.abs(ox) + Math.abs(oy) > radius) {
                    continue;
                }
                Tile tile = this.world.getTile(worldX + ox, worldY + oy);
                if (!(tile instanceof com.cairn4.moonbase.tiles.ItemDropper)) {
                    continue;
                }
                com.cairn4.moonbase.tiles.ItemDropper dropper = (com.cairn4.moonbase.tiles.ItemDropper)tile;
                if (dropper.rdData != null && this.isSameSparseResourceFamily(id, dropper.rdData.id)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldPlaceSparseResource(Chunk chunk, int localX, int localY, ItemDropperData idd) {
        if (idd == null || !this.usesSparsePeakPlacement(idd.id)) {
            return true;
        }
        int worldX = chunk.chunkX * 10 + localX;
        int worldY = chunk.chunkY * 10 + localY;
        return this.isSparsePeak(idd, worldX, worldY) && !this.hasNearbySparseResourceFamily(worldX, worldY, idd.id);
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
            GroundTile gt = chunk.getGroundTile(x, y);
            if (gt != null && gt.getBiome() == GroundTile.Biomes.ice && this.isIceBiomeVegetation(idd.id)) {
                continue;
            }
            for (ItemDropperSpawnBiome biome : idd.spawnBiomes) {
                GroundTile.Biomes b = GroundTile.Biomes.valueOf(biome.biome);
                if (gt.getBiome() != b || !(this.noise[x][y] > biome.threshold)) continue;
                if (!this.shouldPlaceSparseResource(chunk, x, y, idd)) continue;
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
                continue block0;
            }
        }
    }

    private boolean isIceBiomeVegetation(String id) {
        if (id == null) {
            return false;
        }
        String lower = id.toLowerCase();
        return lower.contains("plant") || lower.contains("tree") || lower.contains("shroom") || lower.contains("sprout");
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
                    noise[x][y] = MathUtils.clamp(noise[x][y] + altitudeBias, -1.0f, 1.0f);
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
                    noise[x][y] = MathUtils.clamp(noise[x][y] + wetnessBias, -1.0f, 1.0f);
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

    private float[][] generateNoiseTemp(float features, float smoothing, int startX, int startY, DistanceModifier distanceMod) {
        this.simplexNoiseTemp = new SimplexNoise(this.seedTemp);
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
                    fArray[n] = fArray[n] + (float)SimplexNoise.noise((float)(x + startX) * layerF + 2000.0f, (float)(y + startY) * layerF + 2000.0f) * weight;
                    if (distanceMod != null) {
                        noise[x][y] = distanceMod.distanceMod(x, y, noise[x][y]);
                    }
                    noise[x][y] = MathUtils.clamp(noise[x][y] + temperatureBias, -1.0f, 1.0f);
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

    // Deterministic temperature value for a single world tile (used when loading older saves)
    public float getTemperatureAt(int worldX, int worldY) {
        this.simplexNoiseTemp = new SimplexNoise(this.seedTemp);
        float layerF = this.temperatureNoise;
        float weight = this.temperatureSmoothing;
        float value = 0.0f;
        for (int i = 0; i < 3; ++i) {
            value += (float)SimplexNoise.noise((float)worldX * layerF + 2000.0f, (float)worldY * layerF + 2000.0f) * weight;
            value = this.safeSpawnTemperature(worldX, worldY, value);
            value = MathUtils.clamp(value + temperatureBias, -1.0f, 1.0f);
            layerF *= 3.5f;
            weight *= 0.25f;
        }
        return value;
    }

    // Resolve any direct adjacency between ice and volcanic by inserting a rock buffer.
    public void resolveIceVolcanicAdjacency(Chunk chunk) {
        java.util.HashSet<Chunk> changed = new java.util.HashSet<>();
        boolean changedAny = false;
        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                GroundTile gt = chunk.getGroundTile(x, y);
                if (gt == null) continue;
                int worldX = chunk.chunkX * 10 + x;
                int worldY = chunk.chunkY * 10 + y;
                GroundTile.Biomes b = gt.getBiome();
                if (b == GroundTile.Biomes.ice) {
                    if (this.isBiomeWithinRadius(worldX, worldY, GroundTile.Biomes.volcanic, ICE_VOLCANIC_BUFFER_TILES)) {
                        GroundTile.Biomes baseBiome = GroundTile.calcBaseBiomeTest(gt.altitude, gt.wetness, gt.temperature);
                        gt.setBiome(baseBiome);
                        gt.createDrawables();
                        chunk.gtBiomeArray[y * 10 + x] = gt.getBiome();
                        changed.add(chunk);
                        changedAny = true;
                        continue;
                    }
                    changedAny |= convertNeighborVolcanicToRock(chunk, x, y, changed);
                } else if (b == GroundTile.Biomes.volcanic) {
                    if (isNeighborIce(chunk, x, y) || this.isBiomeWithinRadius(worldX, worldY, GroundTile.Biomes.ice, ICE_VOLCANIC_BUFFER_TILES)) {
                        gt.setBiome(GroundTile.Biomes.rock);
                        gt.createDrawables();
                        chunk.gtBiomeArray[y * 10 + x] = gt.getBiome();
                        changed.add(chunk);
                        changedAny = true;
                    }
                }
            }
        }
        if (changedAny) {
            for (Chunk c : changed) {
                try { c.updateAutoTile(); } catch (Exception ignored) {}
                try { c.setMapDirty(true); } catch (Exception ignored) {}
            }
        }
    }

    private boolean isNeighborIce(Chunk chunk, int localX, int localY) {
        GroundTile n1 = chunk.world.getGroundTile(chunk.chunkX * 10 + localX + 1, chunk.chunkY * 10 + localY);
        GroundTile n2 = chunk.world.getGroundTile(chunk.chunkX * 10 + localX - 1, chunk.chunkY * 10 + localY);
        GroundTile n3 = chunk.world.getGroundTile(chunk.chunkX * 10 + localX, chunk.chunkY * 10 + localY + 1);
        GroundTile n4 = chunk.world.getGroundTile(chunk.chunkX * 10 + localX, chunk.chunkY * 10 + localY - 1);
        return (n1 != null && n1.getBiome() == GroundTile.Biomes.ice) ||
               (n2 != null && n2.getBiome() == GroundTile.Biomes.ice) ||
               (n3 != null && n3.getBiome() == GroundTile.Biomes.ice) ||
               (n4 != null && n4.getBiome() == GroundTile.Biomes.ice);
    }

    private boolean convertNeighborVolcanicToRock(Chunk chunk, int localX, int localY, java.util.HashSet<Chunk> changed) {
        boolean changedAny = false;
        changedAny |= convertIfVolcanic(chunk.world.getGroundTile(chunk.chunkX * 10 + localX + 1, chunk.chunkY * 10 + localY), changed);
        changedAny |= convertIfVolcanic(chunk.world.getGroundTile(chunk.chunkX * 10 + localX - 1, chunk.chunkY * 10 + localY), changed);
        changedAny |= convertIfVolcanic(chunk.world.getGroundTile(chunk.chunkX * 10 + localX, chunk.chunkY * 10 + localY + 1), changed);
        changedAny |= convertIfVolcanic(chunk.world.getGroundTile(chunk.chunkX * 10 + localX, chunk.chunkY * 10 + localY - 1), changed);
        return changedAny;
    }

    private boolean convertIfVolcanic(GroundTile gt, java.util.HashSet<Chunk> changed) {
        if (gt != null && gt.getBiome() == GroundTile.Biomes.volcanic) {
            gt.setBiome(GroundTile.Biomes.rock);
            gt.createDrawables();
            if (gt.chunk != null) {
                gt.chunk.gtBiomeArray[gt.y * 10 + gt.x] = gt.getBiome();
                changed.add(gt.chunk);
            }
            return true;
        }
        return false;
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
