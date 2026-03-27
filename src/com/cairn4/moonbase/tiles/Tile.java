/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.worlddata.BehaviorData;
import com.cairn4.moonbase.worlddata.TileOwners;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

public class Tile {
    public static final float PICK_UP_TIME = 1.0f;
    protected float pickupTimer;
    public ArrayList<Behavior> behaviorList = new ArrayList();
    public types type = types.empty;
    protected short categoryBits;
    protected short maskBits;
    protected Body body;
    private static int nullPosition = -9999;
    public static float SCALE = 0.5f;
    public static float GRID_SIZE = 256.0f * SCALE;
    public static float TILE_SIZE = 256.0f * SCALE;
    public static float TILE_OUTER = 266.0f * SCALE;
    public static float TILE_SPACING = 0.0f * SCALE;
    public World world;
    public Chunk chunk;
    public int x = nullPosition;
    public int y = nullPosition;
    public int worldX;
    public int worldY;
    public int owner;
    public boolean blocker;
    public boolean blocksNorth;
    public boolean blocksEast;
    public boolean blocksSouth;
    public boolean blocksWest;
    public boolean hasAir;
    protected boolean hasWater;
    public boolean hasPower;
    public boolean readyToRemove = false;
    protected float interactDuration;
    protected float interactTimer;
    protected Group group;
    Image image;
    ArrayList<String> validInteractItemIds = new ArrayList();
    public float health;
    public float maxHealth;
    public boolean floorTile = false;
    protected String walkSoundFx;
    public boolean blockInteract = false;
    public boolean walkable = false;
    private Vector2 cameraPos = new Vector2(0.0f, 0.0f);
    private Vector2 tileWorldPos = new Vector2(0.0f, 0.0f);
    private Group parentGroup;

    public Body getBody() {
        return this.body;
    }

    public String getMapName() {
        return "";
    }

    public void addProxies() {
    }

    public boolean isWatered() {
        return this.hasWater;
    }

    public Tile(World world, Chunk chunk, int x, int y) {
        this.world = world;
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.worldX = this.chunk.chunkX * 10 + this.x;
        this.worldY = this.chunk.chunkY * 10 + this.y;
        this.group = new Group();
        this.hasAir = false;
        this.hasPower = false;
        this.interactDuration = 0.0f;
        this.interactTimer = 0.0f;
        this.owner = TileOwners.ANYONE;
        this.createDrawables();
        this.addToChunk();
    }

    public float getMovementMultiplier() {
        return 1.0f;
    }

    public Color getMinimapColor() {
        return Color.WHITE;
    }

    public void loadBehaviorData(ArrayList<BehaviorData> bdList) {
        block3: for (int i = bdList.size() - 1; i >= 0; --i) {
            for (Behavior b : this.behaviorList) {
                if (!b.getClass().getName().equals(bdList.get((int)i).behaviorClass) || b.isLoaded()) continue;
                if (b.getId() != null) {
                    String fromGame;
                    String fromSaveFile = fromGame = b.getId();
                    if (bdList.get((int)i).properties.containsKey("id")) {
                        fromSaveFile = bdList.get((int)i).properties.get("id").toString();
                    } else {
                        Gdx.app.error("MewnBase", "Tile.loadBehaviorData: missing behavior id in save file");
                    }
                    if (!fromGame.equals(fromSaveFile)) {
                        Gdx.app.debug("MewnBase", "Tile.loadBehaviorData: fromGame " + fromGame + " - doesn't equal - fromSaveFile " + fromSaveFile);
                        continue;
                    }
                    Gdx.app.debug("MewnBase", "Tile.loadBehaviorData: Behavior " + b.getClass().getSimpleName() + " id " + fromGame + " matches!");
                }
                Gdx.app.debug("MewnBase", "Tile.loadBehaviorData: Loading behavior data for " + b.getClass().getName() + " -- " + this.chunk.chunkX + ", " + this.chunk.chunkY + ": " + this.worldX + ", " + this.worldY);
                for (Map.Entry<String, Object> entry : bdList.get((int)i).properties.entrySet()) {
                    Class<?> aClass = b.getClass();
                    Field field = null;
                    try {
                        field = aClass.getField(entry.getKey());
                        field.setAccessible(true);
                        field.set(b, entry.getValue());
                    }
                    catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                    catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                b.setLoaded(true);
                continue block3;
            }
        }
        this.startBehaviors();
    }

    public void startBehaviors() {
    }

    protected void addToChunk() {
        if (!this.isFloorTile()) {
            this.chunk.setTile(this, this.x, this.y);
        } else {
            this.chunk.setFloorTile(this, this.x, this.y);
        }
    }

    public boolean isFloorTile() {
        return false;
    }

    public float getWorldXPos() {
        return (float)this.worldX * TILE_SIZE;
    }

    public float getWorldYPos() {
        return (float)this.worldY * TILE_SIZE;
    }

    public float getInteractDuration() {
        return this.interactDuration;
    }

    protected void setupPhysics(String loaderObject) {
        this.setupPhysics(loaderObject, 0.5f);
    }

    protected void setupPhysics(String loaderObject, float scale) {
        this.categoryBits = (short)2;
        this.maskBits = (short)92;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        FixtureDef fd = new FixtureDef();
        fd.density = 1.0f;
        fd.friction = 0.5f;
        fd.restitution = 0.3f;
        fd.filter.categoryBits = this.categoryBits;
        fd.filter.maskBits = (short)(this.maskBits + this.categoryBits);
        this.body = this.world.b2dWorld.createBody(bodyDef);
        this.body.setTransform(new Vector2(this.getXCenter() / 256.0f, this.getYCenter() / 256.0f), 0.0f);
        this.body.setFixedRotation(true);
        this.body.setUserData(this);
        if (loaderObject != null) {
            this.world.bodyEditorLoader.attachFixture(this.body, loaderObject, fd, scale);
        }
    }

    public boolean hasPhysicsBody() {
        return this.body != null;
    }

    protected void createDrawables() {
        this.createDrawables("room", this.world.gameScreen.floorGroup);
    }

    protected void createDrawables(String drawableName) {
        this.createDrawables(drawableName, this.world.gameScreen.floorGroup);
    }

    protected void createDrawables(String drawableName, Group layerGroup) {
        Drawable drawable;
        this.parentGroup = layerGroup;
        this.group.clearChildren();
        this.group.remove();
        this.group = new Group();
        this.group.setOrigin(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.group.setPosition(this.getWorldXPos(), this.getWorldYPos());
        this.group.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + TILE_SIZE / 3.0f));
        this.world.gameScreen.addToStage(this.group, layerGroup);
        try {
            drawable = this.world.gameScreen.skin.getDrawable(drawableName);
        }
        catch (GdxRuntimeException e) {
            MoonBase.error(e.getLocalizedMessage());
            drawable = this.world.gameScreen.skin.getDrawable("missing");
        }
        this.image = new Image(drawable);
        this.image.setSize(TILE_SIZE, TILE_SIZE);
        this.image.setOrigin(1);
        this.image.setPosition(TILE_SPACING, 0.0f);
        this.group.addActor(this.image);
    }

    public void update(float delta) {
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

    public void setInteractionDuration(float newTime) {
        this.interactTimer = 0.0f;
        this.interactDuration = newTime;
    }

    public void interact(Player player) {
    }

    public boolean canInteract(Player player) {
        return true;
    }

    public void updateInteract(Player player, float delta) {
        if (player.ownerId == this.owner || this.owner == TileOwners.ANYONE) {
            this.interactTimer += delta;
            if (this.interactTimer >= this.interactDuration) {
                this.finishInteraction(player);
            }
        } else {
            this.interactTimer = 0.0f;
            player.clearInteractingTile(true);
            System.out.println("not owner");
        }
    }

    protected void finishInteraction(Player player) {
        player.clearInteractingTile(false);
        this.interact(player);
        this.interactTimer = 0.0f;
    }

    public void destroy() {
        this.group.remove();
        this.readyToRemove = true;
        for (Behavior b : this.behaviorList) {
            b.onDestroy();
        }
    }

    public Color getColor() {
        if (this.image != null) {
            return this.image.getColor();
        }
        return Color.WHITE;
    }

    public void setColor(Color color) {
        if (this.image != null) {
            this.image.setColor(color);
        }
    }

    public void setColor(float r, float g, float b, float a) {
        this.setColor(new Color(r, g, b, a));
    }

    public types getType() {
        if (this.type != null) {
            return this.type;
        }
        return null;
    }

    public static ArrayList<GridPoint2> GET_ADJACENT_COORDS(int x, int y) {
        return Tile.GET_ADJACENT_COORDS(x, y, true);
    }

    public static ArrayList<GridPoint2> GET_ADJACENT_COORDS(int x, int y, boolean includeMiddle) {
        ArrayList<GridPoint2> gridPoints = new ArrayList<GridPoint2>();
        if (includeMiddle) {
            gridPoints.add(new GridPoint2(x + 0, y + 0));
        }
        gridPoints.add(World.getGridPointFromPoolAndSet(x + 0, y + 1));
        gridPoints.add(World.getGridPointFromPoolAndSet(x + 1, y + 1));
        gridPoints.add(World.getGridPointFromPoolAndSet(x + 1, y + 0));
        gridPoints.add(World.getGridPointFromPoolAndSet(x + 1, y - 1));
        gridPoints.add(World.getGridPointFromPoolAndSet(x + 0, y - 1));
        gridPoints.add(World.getGridPointFromPoolAndSet(x - 1, y - 1));
        gridPoints.add(World.getGridPointFromPoolAndSet(x - 1, y + 0));
        gridPoints.add(World.getGridPointFromPoolAndSet(x - 1, y + 1));
        return gridPoints;
    }

    public static ArrayList<GridPoint2> GET_NEARBY_COORDS(int xCenter, int yCenter, int radius) {
        ArrayList<GridPoint2> gridPoints = new ArrayList<GridPoint2>();
        int minX = xCenter - radius;
        int maxX = xCenter + radius;
        int minY = yCenter - radius;
        int maxY = yCenter + radius;
        Vector2 center = new Vector2(xCenter, yCenter);
        Vector2 test = new Vector2(0.0f, 0.0f);
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                test.set(x, y);
                if (!(test.dst(center) < (float)radius)) continue;
                GridPoint2 gp = World.gridPointPool.obtain();
                gp.set(x, y);
                gridPoints.add(gp);
            }
        }
        return gridPoints;
    }

    public static ArrayList<GridPoint2> GET_NEARBY_COORDS(ArrayList<GridPoint2> list, int xCenter, int yCenter, int radius) {
        list.clear();
        int minX = xCenter - radius;
        int maxX = xCenter + radius;
        int minY = yCenter - radius;
        int maxY = yCenter + radius;
        Vector2 center = World.vector2Pool.obtain();
        center.set(xCenter, yCenter);
        Vector2 test = World.vector2Pool.obtain();
        test.set(0.0f, 0.0f);
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                test.set(x, y);
                if (!(test.dst(center) < (float)radius)) continue;
                GridPoint2 gp = World.gridPointPool.obtain();
                gp.set(x, y);
                list.add(gp);
            }
        }
        World.vector2Pool.free(center);
        World.vector2Pool.free(test);
        return list;
    }

    public static ArrayList<GridPoint2> GET_NEARBY_COORDS(float worldXPos, float worldYPos, float radius) {
        ArrayList<GridPoint2> gridPoints = new ArrayList<GridPoint2>();
        float minX = worldXPos - radius;
        float maxX = worldXPos + radius;
        float minY = worldYPos - radius;
        float maxY = worldYPos + radius;
        int minXT = MathUtils.floor(minX / TILE_SIZE);
        int maxXT = MathUtils.round(maxX / TILE_SIZE);
        int minYT = MathUtils.floor(minY / TILE_SIZE);
        int maxYT = MathUtils.round(maxY / TILE_SIZE);
        Vector2 test = new Vector2(0.0f, 0.0f);
        for (int x = minXT; x <= maxXT; ++x) {
            for (int y = minYT; y <= maxYT; ++y) {
                test.set(x, y);
                gridPoints.add(new GridPoint2(x, y));
            }
        }
        return gridPoints;
    }

    public Behavior findBehavior(Class bClass) {
        for (Behavior b : this.behaviorList) {
            if (!b.getClass().equals(bClass)) continue;
            return b;
        }
        return null;
    }

    public float getXCenter() {
        return (float)this.worldX * GRID_SIZE + GRID_SIZE / 2.0f;
    }

    public float getYCenter() {
        return (float)this.worldY * GRID_SIZE + GRID_SIZE / 2.0f;
    }

    public float getInteractProgress() {
        return this.interactTimer / this.interactDuration;
    }

    public void setVisible(boolean visible) {
        this.group.setVisible(visible);
    }

    public float getPickupProgress() {
        return this.pickupTimer / 1.0f;
    }

    public boolean isValidInteractItem(String id) {
        for (String s : this.validInteractItemIds) {
            if (!id.equals(s)) continue;
            return true;
        }
        return false;
    }

    public void doInteractPunchAnim() {
    }

    public String getWalkSoundFx() {
        if (this.walkSoundFx != null) {
            return this.walkSoundFx;
        }
        return null;
    }

    public boolean blocksWind() {
        return true;
    }

    public void playInteractSound() {
        float pitch = MathUtils.random(0.95f, 1.05f);
        Audio.getInstance().playSound("sounds/interact_hit2.ogg", 0.7f, pitch);
    }

    public static enum types {
        empty,
        blockingTerrain,
        base,
        habitat,
        pile;

    }
}

