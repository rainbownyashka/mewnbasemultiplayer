/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Pool;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.Tile;

public class GroundTile
implements Pool.Poolable {
    public boolean readyToRemove;
    public World world;
    public Chunk chunk;
    public int x = -9999;
    public int y = -9999;
    public int worldX;
    public int worldY;
    private static final String altEdge = "-alt1";
    public Group group;
    public Group autoTileGroup;
    private Image image;
    private Biomes biome;
    public boolean edge = false;
    public float altitude = 0.5f;
    public float wetness = 0.5f;
    public float temperature = 0.0f;
    public int altIndex = 0;
    public int wetIndex = 0;
    public int tempIndex = 0;
    public AltitudeTypes altitudeType;
    public WetnessTypes wetnessType;
    private String spriteName;
    protected String walkSoundFx;
    public static float[] altThresholds = new float[]{0.15f, 0.3f, 0.45f, 0.7f, 0.82f, 0.92f};
    public static float[] wetThresholds = new float[]{0.2f, 0.5f, 0.75f, 0.9f};
    public static float[] tempThresholds = new float[]{0.25f, 0.45f, 0.6f, 0.75f};
    public static Biomes[][] biomeTable = new Biomes[][]{{Biomes.ground, Biomes.ground, Biomes.mud, Biomes.mud, Biomes.mud}, {Biomes.ground, Biomes.ground, Biomes.ground, Biomes.mud, Biomes.mud}, {Biomes.ground, Biomes.ground, Biomes.ground, Biomes.mud, Biomes.ground}, {Biomes.rock, Biomes.rock, Biomes.ground, Biomes.ground, Biomes.ground}, {Biomes.rock, Biomes.ground, Biomes.ground, Biomes.ground, Biomes.ground}, {Biomes.ice, Biomes.ice, Biomes.ground, Biomes.ground, Biomes.volcanic}, {Biomes.ice, Biomes.ice, Biomes.volcanic, Biomes.volcanic, Biomes.volcanic}};
    public static float ICE_TINT_R = 1.0f;
    public static float ICE_TINT_G = 1.0f;
    public static float ICE_TINT_B = 1.0f;
    private static int[] rockTileAlternates = new int[]{0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 2};
    boolean resources = false;
    boolean discovered;
    private Vector2 cameraPos = new Vector2(0.0f, 0.0f);
    private Vector2 tileWorldPos = new Vector2(0.0f, 0.0f);
    private static com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable missingDrawable;

    public String getSprite() {
        return this.spriteName;
    }

    @Override
    public void reset() {
    }

    public GroundTile() {
    }

    public GroundTile(World world, Chunk chunk, int x, int y) {
        this.init(world, chunk, x, y);
    }

    public void init(World world, Chunk chunk, int x, int y) {
        this.readyToRemove = false;
        this.world = world;
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.worldX = this.chunk.chunkX * 10 + this.x;
        this.worldY = this.chunk.chunkY * 10 + this.y;
        this.group = new Group();
        this.createDrawables();
        this.setDiscovered(false);
    }

    public static int calcWorldX(int chunkX, int tileX) {
        int wX = chunkX * 10 + tileX;
        return wX;
    }

    public void destroy() {
        this.group.clearChildren();
        this.group.remove();
        this.readyToRemove = true;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            this.world.gameScreen.bgGroup.addActor(this.group);
            this.group.setVisible(true);
        } else {
            this.group.remove();
            this.group.setVisible(false);
        }
    }

    public float getWorldXPos() {
        return (float)this.worldX * Tile.TILE_SIZE;
    }

    public float getWorldYPos() {
        return (float)this.worldY * Tile.TILE_SIZE;
    }

    public float getXCenter() {
        return (float)this.worldX * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f;
    }

    public float getYCenter() {
        return (float)this.worldY * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f;
    }

    public void canPlayerSee() {
        this.cameraPos.set(this.world.gameScreen.camera.position.x, this.world.gameScreen.camera.position.y);
        this.tileWorldPos.set(this.getWorldXPos(), this.getWorldYPos());
        if (this.tileWorldPos.dst(this.cameraPos) < MoonBase.DRAW_DISTANCE) {
            this.setVisible(true);
        } else {
            this.setVisible(false);
        }
    }

    public void createDrawables() {
        this.world.gameScreen.bgGroup.addActor(this.group);
        this.group.setPosition(this.getWorldXPos(), this.getWorldYPos());
        if (this.image != null) {
            this.image.remove();
        }
        switch (this.getBiome()) {
            case water: {
                this.spriteName = "test/water-15";
                break;
            }
            case mud: {
                this.spriteName = "test/mud-15";
                break;
            }
            case ground: {
                this.spriteName = "test/ground-15";
                float r = MathUtils.random();
                if (r > 0.4f && r <= 0.9f) {
                    int rAlt = MathUtils.random(1, 5);
                    this.spriteName = this.spriteName + "-alt" + rAlt;
                    break;
                }
                if (!(r > 0.9f)) break;
                int rAlt = MathUtils.random(6, 9);
                this.spriteName = this.spriteName + "-alt" + rAlt;
                break;
            }
            case green: {
                this.spriteName = "test/green-15";
                break;
            }
            case sand: {
                this.spriteName = "test/sand-15";
                float rS = MathUtils.random();
                if (rS > 0.4f && rS <= 0.9f) {
                    int rAlt = MathUtils.random(1, 3);
                    this.spriteName = this.spriteName + "-alt" + rAlt;
                    break;
                }
                if (!(rS > 0.9f)) break;
                int rAlt = MathUtils.random(4, 6);
                this.spriteName = this.spriteName + "-alt" + rAlt;
                break;
            }
            case rock: {
                this.spriteName = "test/rock-15";
                float r2 = MathUtils.random();
                if (!(r2 > 0.7f)) break;
                int rAlt = MathUtils.random(1, 5);
                this.spriteName = this.spriteName + "-alt" + rAlt;
                break;
            }
            case swamp: {
                this.spriteName = "test/swamp-15";
                float s = MathUtils.random();
                if (!(s > 0.7f)) break;
                int rAlt = MathUtils.random(1, 4);
                this.spriteName = this.spriteName + "-alt" + rAlt;
                break;
            }
            case volcanic: {
                float randomV;
                this.spriteName = "test/volcanic-15";
                if ((this.x % 2 != 0 || this.y % 2 == 0) && (this.x % 2 == 0 || this.y % 2 != 0) || !((randomV = MathUtils.random()) > 0.7f)) break;
                int rAlt = MathUtils.random(1, 3);
                this.spriteName = this.spriteName + "-alt" + rAlt;
                break;
            }
            case ice: {
                this.spriteName = resolveIceSpriteName("modded/ice-15", "test/ground-15");
                float rI = MathUtils.random();
                if (rI > 0.4f && rI <= 0.9f) {
                    int rAlt = MathUtils.random(1, 5);
                    this.spriteName = resolveIceSpriteName(this.spriteName + "-alt" + rAlt, this.spriteName);
                    break;
                }
                if (!(rI > 0.9f)) break;
                int rAlt = MathUtils.random(6, 9);
                this.spriteName = resolveIceSpriteName(this.spriteName + "-alt" + rAlt, this.spriteName);
            }
        }
        Image baseImage = null;
        try {
            baseImage = new Image(this.world.gameScreen.skin.getDrawable(this.spriteName));
        } catch (Exception e) {
            MoonBase.log("GroundTile: missing drawable " + this.spriteName + ", fallback to test/ground-15");
            try {
                baseImage = new Image(this.world.gameScreen.skin.getDrawable("test/ground-15"));
            } catch (Exception e2) {
                MoonBase.log("GroundTile: missing fallback drawable test/ground-15, using empty placeholder");
                baseImage = new Image(getMissingDrawable());
                baseImage.setVisible(false);
            }
        }
        this.image = baseImage;
        this.image.setSize(Tile.TILE_SIZE + 0.02f, Tile.TILE_SIZE + 0.02f);
        this.image.setPosition(-0.01f, -0.01f);
        if (this.getBiome() == Biomes.ice) {
            this.image.setName("iceBase");
            this.image.setColor(ICE_TINT_R, ICE_TINT_G, ICE_TINT_B, 1.0f);
        }
        this.group.addActor(this.image);
        if (this.autoTileGroup != null) {
            this.autoTileGroup.clearChildren();
        } else {
            this.autoTileGroup = new Group();
            this.group.addActor(this.autoTileGroup);
        }
    }

    public void autoTileLayer(Biomes biomeLayer) {
        GroundTile north = this.world.getGroundTile(this.worldX, this.worldY + 1);
        GroundTile south = this.world.getGroundTile(this.worldX, this.worldY - 1);
        GroundTile east = this.world.getGroundTile(this.worldX + 1, this.worldY);
        GroundTile west = this.world.getGroundTile(this.worldX - 1, this.worldY);
        GroundTile northEast = this.world.getGroundTile(this.worldX + 1, this.worldY + 1);
        GroundTile northWest = this.world.getGroundTile(this.worldX - 1, this.worldY + 1);
        GroundTile southEast = this.world.getGroundTile(this.worldX + 1, this.worldY - 1);
        GroundTile southWest = this.world.getGroundTile(this.worldX - 1, this.worldY - 1);
        boolean bSW = true;
        int bSE = 2;
        int bNE = 4;
        int bNW = 8;
        int SW = 0;
        int SE = 0;
        int NE = 0;
        int NW = 0;
        if (north != null && north.getBiome() == biomeLayer) {
            NE = 4;
            NW = 8;
        }
        if (northEast != null && northEast.getBiome() == biomeLayer) {
            NE = 4;
        }
        if (northWest != null && northWest.getBiome() == biomeLayer) {
            NW = 8;
        }
        if (south != null && south.getBiome() == biomeLayer) {
            SE = 2;
            SW = 1;
        }
        if (southWest != null && southWest.getBiome() == biomeLayer) {
            SW = 1;
        }
        if (southEast != null && southEast.getBiome() == biomeLayer) {
            SE = 2;
        }
        if (east != null && east.getBiome() == biomeLayer) {
            NE = 4;
            SE = 2;
        }
        if (west != null && west.getBiome() == biomeLayer) {
            NW = 8;
            SW = 1;
        }
        int total = SW + SE + NE + NW;
        if (SW > 0 && SE > 0 && NW > 0 && NE > 0) {
            total = 15;
        }
        String baseName = this.getBiomeSpriteName(biomeLayer);
        StringBuilder sb = new StringBuilder();
        sb.append("test/");
        sb.append(baseName);
        sb.append("-");
        sb.append(total);
        String fileName = sb.toString();
        if (total > 0) {
            if ((this.x % 2 == 0 && this.y % 2 != 0 || this.x % 2 != 0 && this.y % 2 == 0) && (biomeLayer == Biomes.ground || biomeLayer == Biomes.rock)) {
                TiledDrawable d = null;
                try {
                    d = this.world.gameScreen.skin.getTiledDrawable(fileName + altEdge);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                if (d != null) {
                    fileName = fileName + altEdge;
                }
            }
            Image testLayer;
            try {
                testLayer = new Image(this.world.gameScreen.skin.getDrawable(fileName));
            } catch (Exception e) {
                MoonBase.log("GroundTile: missing layer drawable " + fileName + ", skipping layer");
                return;
            }
            testLayer.setSize(Tile.TILE_SIZE + 0.02f, Tile.TILE_SIZE + 0.02f);
            testLayer.setPosition(-0.01f, -0.01f);
            if (biomeLayer == Biomes.ice) {
                testLayer.setName("iceLayer");
                testLayer.setColor(ICE_TINT_R, ICE_TINT_G, ICE_TINT_B, 1.0f);
            }
            this.group.addActor(testLayer);
        }
    }

    public void autoTile() {
        for (Actor a : this.group.getChildren()) {
            if (a == this.image) continue;
            a.remove();
        }
        if (this.autoTileGroup == null) {
            this.autoTileGroup = new Group();
            this.group.addActor(this.autoTileGroup);
        } else {
            this.autoTileGroup.clearChildren();
            this.autoTileGroup.toFront();
        }
        if (this.getBiome() != Biomes.mud) {
            this.autoTileLayer(Biomes.mud);
        } else {
            this.image.toFront();
        }
        if (this.getBiome() != Biomes.ground) {
            this.autoTileLayer(Biomes.ground);
        } else {
            this.image.toFront();
        }
        if (this.getBiome() != Biomes.swamp) {
            this.autoTileLayer(Biomes.swamp);
        } else {
            this.image.toFront();
        }
        if (this.getBiome() != Biomes.green) {
            this.autoTileLayer(Biomes.green);
        } else {
            this.image.toFront();
        }
        if (this.getBiome() != Biomes.sand) {
            this.autoTileLayer(Biomes.sand);
        } else {
            this.image.toFront();
        }
        if (this.getBiome() != Biomes.rock) {
            this.autoTileLayer(Biomes.rock);
        } else {
            this.image.toFront();
        }
        if (this.getBiome() != Biomes.volcanic) {
            this.autoTileLayer(Biomes.volcanic);
        } else {
            this.image.toFront();
        }
        if (this.getBiome() != Biomes.ice) {
            this.autoTileLayer(Biomes.ice);
        } else {
            this.image.toFront();
        }
    }

    protected String getBiomeSpriteName(Biomes b) {
        if (b == Biomes.ice) return "ground";
        return b.toString();
    }

    private String resolveIceSpriteName(String preferred, String fallback) {
        try {
            if (this.world != null && this.world.gameScreen != null && this.world.gameScreen.skin != null) {
                if (this.world.gameScreen.skin.has(preferred, com.badlogic.gdx.graphics.g2d.TextureRegion.class)) {
                    return preferred;
                }
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    protected int getBiomeGroupLayer(Biomes b) {
        switch (b) {
            case water: {
                return 0;
            }
            case ground: {
                return 1;
            }
            case swamp: {
                return 2;
            }
            case sand: {
                return 3;
            }
            case rock: {
                return 4;
            }
            case ice: {
                return 1;
            }
        }
        return 1;
    }

    private static com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable getMissingDrawable() {
        if (missingDrawable != null) {
            return missingDrawable;
        }
        com.badlogic.gdx.graphics.Pixmap pm = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        com.badlogic.gdx.graphics.Texture tex = new com.badlogic.gdx.graphics.Texture(pm);
        pm.dispose();
        missingDrawable = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(tex));
        return missingDrawable;
    }

    public static void setIceTint(float r, float g, float b) {
        ICE_TINT_R = MathUtils.clamp(r, 0.0f, 1.0f);
        ICE_TINT_G = MathUtils.clamp(g, 0.0f, 1.0f);
        ICE_TINT_B = MathUtils.clamp(b, 0.0f, 1.0f);
    }

    public void refreshIceTint() {
        if (this.getBiome() != Biomes.ice) {
            return;
        }
        if (this.image != null) {
            this.image.setColor(ICE_TINT_R, ICE_TINT_G, ICE_TINT_B, 1.0f);
        }
        if (this.group != null) {
            for (Actor a : this.group.getChildren()) {
                if (a != null && "iceLayer".equals(a.getName())) {
                    a.setColor(ICE_TINT_R, ICE_TINT_G, ICE_TINT_B, 1.0f);
                }
            }
        }
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
        if (this.discovered) {
            // empty if block
        }
        this.chunk.setMapDirty(true);
    }

    public boolean isDiscovered() {
        return this.discovered;
    }

    private static int toAltIndex(float a) {
        if (a < altThresholds[0]) {
            return 0;
        }
        if (a >= altThresholds[0] && a < altThresholds[1]) {
            return 1;
        }
        if (a >= altThresholds[1] && a < altThresholds[2]) {
            return 2;
        }
        if (a >= altThresholds[2] && a < altThresholds[3]) {
            return 3;
        }
        if (a >= altThresholds[3] && a < altThresholds[4]) {
            return 4;
        }
        if (a >= altThresholds[4] && a < altThresholds[5]) {
            return 5;
        }
        if (a >= altThresholds[5]) {
            return 6;
        }
        System.out.println("error: " + a);
        return -1;
    }

    public static int toWetIndex(float w) {
        if (w < wetThresholds[0]) {
            return 0;
        }
        if (w >= wetThresholds[0] && w < wetThresholds[1]) {
            return 1;
        }
        if (w >= wetThresholds[1] && w < wetThresholds[2]) {
            return 2;
        }
        if (w >= wetThresholds[2] && w < wetThresholds[3]) {
            return 3;
        }
        if (w >= wetThresholds[3]) {
            return 4;
        }
        System.out.println("error: " + w);
        return 0;
    }

    public void calcBiome(float alt, float wet) {
        this.calcBiome(alt, wet, 0.0f);
    }

    public void calcBiome(float alt, float wet, float temp) {
        this.altitude = alt;
        this.wetness = wet;
        this.temperature = temp;
        float fixedAlt = (alt + 1.0f) / 2.0f;
        this.altIndex = GroundTile.toAltIndex(fixedAlt);
        float fixedWet = (wet + 1.0f) / 2.0f;
        this.wetIndex = GroundTile.toWetIndex(fixedWet);
        float fixedTemp = (temp + 1.0f) / 2.0f;
        this.tempIndex = GroundTile.toTempIndex(fixedTemp);
        Biomes base = biomeTable[this.altIndex][this.wetIndex];
        if (this.tempIndex <= 2 && base == Biomes.volcanic) {
            base = Biomes.rock;
        }
        try {
            float dist = Vector2.dst(this.worldX, this.worldY, 500.0f, 500.0f);
            if (dist < 160.0f && base == Biomes.volcanic) {
                base = Biomes.ground;
            }
        } catch (Exception ignored) {}
        if (this.tempIndex <= 1 && base != Biomes.water && base != Biomes.volcanic) {
            this.biome = Biomes.ice;
        } else {
            this.biome = base;
        }
    }

    public static Biomes calcBiomeTest(float alt, float wet) {
        return calcBiomeTest(alt, wet, 0.0f);
    }

    public static Biomes calcBiomeTest(float alt, float wet, float temp) {
        float fixedAlt = (alt + 1.0f) / 2.0f;
        int altIndex = GroundTile.toAltIndex(fixedAlt);
        float fixedWet = (wet + 1.0f) / 2.0f;
        int wetIndex = GroundTile.toWetIndex(fixedWet);
        float fixedTemp = (temp + 1.0f) / 2.0f;
        int tempIndex = GroundTile.toTempIndex(fixedTemp);
        Biomes base = biomeTable[altIndex][wetIndex];
        if (tempIndex <= 2 && base == Biomes.volcanic) {
            base = Biomes.rock;
        }
        if (tempIndex <= 1 && base != Biomes.water && base != Biomes.volcanic) {
            return Biomes.ice;
        }
        return base;
    }

    public static int toTempIndex(float t) {
        if (t < tempThresholds[0]) {
            return 0;
        }
        if (t >= tempThresholds[0] && t < tempThresholds[1]) {
            return 1;
        }
        if (t >= tempThresholds[1] && t < tempThresholds[2]) {
            return 2;
        }
        if (t >= tempThresholds[2] && t < tempThresholds[3]) {
            return 3;
        }
        if (t >= tempThresholds[3]) {
            return 4;
        }
        return 0;
    }

    public void setBiome(String biome) {
        try {
            this.biome = Biomes.valueOf(biome);
        }
        catch (Exception e) {
            this.biome = Biomes.ground;
        }
    }

    public void setBiome(Biomes biome) {
        this.biome = biome;
    }

    public Biomes getBiome() {
        if (this.biome == null) {
            this.biome = Biomes.ground;
        }
        return this.biome;
    }

    public float getMovementMultiplier() {
        return 1.0f;
    }

    public void setResources(boolean r) {
        this.resources = r;
    }

    public boolean hasResources() {
        return this.resources;
    }

    public void interact(Player player) {
        if (this.hasResources()) {
            System.out.println("Do resource minigame, or something");
            this.setResources(false);
            this.world.gameScreen.hud.removeResourceIndicator(this);
        }
    }

    public void removeResources() {
        this.setResources(false);
    }

    public String getWalkSoundFx() {
        switch (this.biome) {
            case water: {
                return "sounds/footstep-mud";
            }
            case ground: {
                return null;
            }
            case green: {
                return null;
            }
            case sand: {
                return null;
            }
            case rock: {
                return null;
            }
            case volcanic: {
                return null;
            }
        }
        return null;
    }

    public float getTemperature() {
        return 0.0f;
    }

    public static enum WetnessTypes {
        verydry,
        dry,
        normal,
        damp,
        wet;

    }

    public static enum AltitudeTypes {
        water,
        low,
        normal,
        raised,
        high,
        veryhigh;

    }

    public static enum Biomes {
        water,
        swamp,
        mud,
        ground,
        green,
        sand,
        rock,
        volcanic,
        ice,
        lava;

    }
}
