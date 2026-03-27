/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Creature;
import com.cairn4.moonbase.entities.CreatureLoader;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.worlddata.CreatureData;
import java.util.ArrayList;

public class EnemySpawner {
    public World world;
    public static final float spawnSearchRadius = Tile.TILE_SIZE * 20.0f;
    public static final float sleepDistance = Tile.TILE_SIZE * 20.0f;
    public static final int minPlayerTileDistance = 15;
    public static final int maxPlayerTileDistance = 20;
    public static final float spawnTimerDelay = 4.0f;
    private static float spawnTimer;
    public static final int maxEnemiesNearPlayer = 4;
    public int enemiesNearPlayer;
    private Vector2 playerPos;
    private float enclosureTimer;
    private final float ENCLOSURE_DELAY = 1.0f;
    ArrayList<GridPoint2> enclosedGps = new ArrayList();
    ArrayList<GridPoint2> checkGps = new ArrayList();
    ArrayList<GridPoint2> perimeterGps = new ArrayList();

    public EnemySpawner(World world) {
        this.world = world;
        spawnTimer = 0.0f;
    }

    public void update(float delta) {
        if (this.world.dayCycle.getDay() >= 0) {
            this.updatePlayerPosition();
            spawnTimer += delta;
            if (spawnTimer > 4.0f) {
                int enemyCount;
                spawnTimer = 0.0f;
                if (MoonBase.SPAWN_CREATURES && (enemyCount = this.countEnemiesNearPlayer()) < 4) {
                    this.tryToSpawnEnemy();
                }
            }
            this.enclosureTimer += Gdx.graphics.getDeltaTime();
            if (this.enclosureTimer > 1.0f) {
                this.enclosureTimer = 0.0f;
                for (Entity e : this.world.entityList) {
                    if (!(e instanceof Creature)) continue;
                    this.creatureEnclosed(e);
                }
            }
            this.sleepDistantEnemies();
        }
    }

    private void sleepDistantEnemies() {
        for (Entity e : this.world.entityList) {
            Creature c;
            if (!(e instanceof Creature) || (c = (Creature)e).getEnclosed() || !(this.enemyToPlayerDistance((Creature)e, this.playerPos) > sleepDistance)) continue;
            ((Creature)e).sleep();
        }
    }

    public boolean creatureEnclosed(Entity c) {
        int x = c.getWorldXTile();
        int y = c.getWorldYTile();
        this.enclosedGps.clear();
        this.checkGps.clear();
        this.perimeterGps.clear();
        GridPoint2 startGp = World.gridPointPool.obtain();
        startGp.set(x, y);
        this.enclosedGps.add(startGp);
        int maxEnclosureSize = 25;
        this.checkGps.addAll(Tile.GET_ADJACENT_COORDS(startGp.x, startGp.y, false));
        while (!this.checkGps.isEmpty() && this.enclosedGps.size() < maxEnclosureSize) {
            for (int i = this.checkGps.size() - 1; i >= 0; --i) {
                GridPoint2 gp = this.checkGps.get(i);
                this.checkEnclosureGps(gp);
            }
        }
        boolean enclosed = this.enclosedGps.size() < maxEnclosureSize;
        ((Creature)c).setEnclosed(enclosed);
        return enclosed;
    }

    private void checkEnclosureGps(GridPoint2 gp) {
        if (this.world.isTileEmpty(gp.x, gp.y)) {
            this.addToEnclosedList(gp);
        } else {
            Tile t = this.world.getTile(gp.x, gp.y);
            if (!t.hasPhysicsBody()) {
                this.addToEnclosedList(gp);
            } else {
                this.perimeterGps.add(gp);
                this.checkGps.remove(gp);
            }
        }
    }

    private void addToEnclosedList(GridPoint2 gp) {
        if (!this.gpInEnclosedList(gp)) {
            this.enclosedGps.add(gp);
            ArrayList<GridPoint2> adjacent = Tile.GET_ADJACENT_COORDS(gp.x, gp.y, false);
            for (GridPoint2 adj : adjacent) {
                boolean alreadyAdded = false;
                if (this.gpInEnclosedList(adj)) continue;
                this.checkGps.add(adj);
            }
        }
        this.checkGps.remove(gp);
    }

    private boolean gpInEnclosedList(GridPoint2 gp) {
        boolean alreadyAdded = false;
        for (GridPoint2 enclosedGp : this.enclosedGps) {
            if (!gp.equals(enclosedGp)) continue;
            alreadyAdded = true;
            break;
        }
        return alreadyAdded;
    }

    private void tryToSpawnEnemy() {
        int playerXTile = this.world.player.getX();
        int playerYTile = this.world.player.getY();
        boolean spawned = false;
        int maxAttempts = 100;
        for (int attempts = 0; !spawned && attempts < maxAttempts; ++attempts) {
            GroundTile gt;
            int randomY;
            int randomX = MathUtils.random(playerXTile - 20, playerXTile + 20);
            float dist = Vector2.dst(playerXTile, playerYTile, randomX, randomY = MathUtils.random(playerYTile - 20, playerYTile + 20));
            if (!(dist > 15.0f) || !(dist < 20.0f) || !this.world.isTileEmpty(randomX, randomY) || (gt = this.world.getGroundTile(randomX, randomY)) == null) continue;
            ArrayList<String> validCreatureIds = new ArrayList<String>();
            for (CreatureData cd : CreatureLoader.getInstance().creatureDataList) {
                float tileDistToOrigin;
                if (!cd.enabled || !this.isValidSpawnBiome(cd, gt) || !((tileDistToOrigin = Vector2.dst(500.0f, 500.0f, randomX, randomY)) > (float)cd.minSpawnTileDistFromOrigin) || this.tooCloseToOtherCreature(cd, randomX, randomY)) continue;
                validCreatureIds.add(cd.id);
            }
            if (validCreatureIds.size() <= 0) continue;
            int randomCreature = MathUtils.random(0, validCreatureIds.size() - 1);
            this.spawn(randomX, randomY, (String)validCreatureIds.get(randomCreature));
            spawned = true;
        }
        if (!spawned) {
            // empty if block
        }
    }

    private boolean tooCloseToOtherCreature(CreatureData cd, int tileX, int tileY) {
        for (Entity e : this.world.entityList) {
            if (!(e instanceof Creature)) continue;
            Creature c = (Creature)e;
            int x = c.getWorldXTile();
            int y = c.getWorldYTile();
            GridPoint2 gp = World.gridPointPool.obtain();
            gp.set(x, y);
            float dst = gp.dst(tileX, tileY);
            if (dst < (float)cd.minTileDistFromOtherCreatures) {
                MoonBase.debug("Creature can't spawn this close to another creature: " + dst);
                World.gridPointPool.free(gp);
                return true;
            }
            World.gridPointPool.free(gp);
        }
        return false;
    }

    private boolean isValidSpawnBiome(CreatureData cd, GroundTile gt) {
        for (String s : cd.spawnBiomes) {
            if (!s.equals(gt.getBiome().toString())) continue;
            return true;
        }
        return false;
    }

    private void spawn(int tileX, int tileY, String creatureId) {
        new Creature(this.world, (float)tileX * Tile.TILE_SIZE, (float)tileY * Tile.TILE_SIZE, 0.0f, creatureId);
    }

    private int countEnemiesNearPlayer() {
        this.enemiesNearPlayer = 0;
        for (Entity e : this.world.entityList) {
            if (!(e instanceof Creature) || !(this.enemyToPlayerDistance((Creature)e, this.playerPos) < spawnSearchRadius)) continue;
            ++this.enemiesNearPlayer;
        }
        return this.enemiesNearPlayer;
    }

    private void updatePlayerPosition() {
        if (this.playerPos == null) {
            this.playerPos = new Vector2(0.0f, 0.0f);
        }
        if (this.world.player != null) {
            this.playerPos.set(this.world.player.getXPos(), this.world.player.getYPos());
        }
    }

    private float enemyToPlayerDistance(Creature e, Vector2 p) {
        return p.dst(e.getXPos(), e.getYPos());
    }

    public void renderCreatureEnclosures() {
        for (GridPoint2 gp : this.enclosedGps) {
            GameScreen cfr_ignored_0 = this.world.gameScreen;
            ShapeRenderer sr = GameScreen.shapeRenderer;
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(0.0f, 1.0f, 0.0f, 0.25f);
            sr.rect((float)gp.x * Tile.TILE_SIZE, (float)gp.y * Tile.TILE_SIZE, Tile.TILE_SIZE, Tile.TILE_SIZE);
            sr.end();
        }
    }
}

