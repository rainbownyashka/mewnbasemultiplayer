/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Scaling;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BaseGroup;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.WorldDebugMap;
import com.cairn4.moonbase.entities.Buggie;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.Tank;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.ItemDropper;
import com.cairn4.moonbase.tiles.LaunchPad;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.WoodSign;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MiniMap
extends Menu
implements Telegraph {
    private static int CHUNKS_RADIUS_FROM_CURRENT = 4;
    private static float MAX_TILE_DISTANCE = 150.0f;
    private final int chunkDrawSize = 100;
    private final int tilePixelSize = 10;
    private static int MEGA_CHUNK = 10;
    public static float savedZoom = 1.0f;
    private Group groupOffset;
    private Group chunkGroup;
    private Group mapZoomHolder;
    private Group tileIconGroup;
    Label navBeacons;
    GameScreen gameScreen;
    private FileHandle chunkFile;
    private ArrayList<Texture> disposableTextures = new ArrayList();
    private HashMap<String, Texture> chunkTextures = new HashMap();
    private HashMap<Texture, Color> chunkColorState = new HashMap();
    private Array<Image> playerMarks = new Array();
    private int minChunkX;
    private int maxChunkX;
    private int minChunkY;
    private int maxChunkY;
    int minMegaChunkX;
    int maxMegaChunkX;
    int minMegaChunkY;
    int maxMegaChunkY;
    public Vector2 mapCenterOffeset = new Vector2(0.0f, 0.0f);
    public Vector2 dragStarPos = new Vector2(0.0f, 0.0f);
    public Vector2 dragOffset = new Vector2(0.0f, 0.0f);
    public Vector2 dragGroupStartPos = new Vector2(0.0f, 0.0f);
    private Vector2 minDragPos = new Vector2(0.0f, 0.0f);
    private Vector2 maxDragPos = new Vector2(0.0f, 0.0f);
    private boolean showMapPopup = false;
    private Image bg;
    private Group uiGroup;
    private Image border;
    private Image innerBorder;
    private Button btnClose;
    private Button btnRecenter;
    private Button btnZoomIn;
    private Image zoomDiv;
    private Button btnZoomOut;
    private Label hoverLabel;
    private Group hoverTooltipGroup;
    private Group hoverInnerGroup;
    private Image hoverPanel;
    private Table hoverTooltipTable;
    private Label pauseLabel;
    private static float PAN_SPEED = 500.0f;
    private Pixmap chunkPixmap;
    private float soundPitch;
    private float soundBasePitch = 1.5f;
    private float soundPitchVar = 0.115f;
    private float soundVolume = 0.035f;
    private Vector2 screenMouse = new Vector2(0.0f, 0.0f);
    private Vector2 mapMouse = new Vector2(0.0f, 0.0f);
    public int zoomDir = 0;

    public MiniMap(GameScreen gameScreen) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        gameScreen.world.pauseSimulation(true);
        MessageManager.getInstance().addListener(this, 40);
        MessageManager.getInstance().addListener(this, 41);
        super.setup();
        this.stage.setScrollFocus(null);
        this.show();
        this.addMapListeners();
        this.finishedShowAnim();
    }

    private void addMapListeners() {
        this.chunkGroup.addListener(new DragListener(){

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                MiniMap.this.dragStarPos.set(x, y);
                MiniMap.this.dragGroupStartPos.set(MiniMap.this.groupOffset.getX(), MiniMap.this.groupOffset.getY());
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                Vector2 mouseDistance = new Vector2(x, y).sub(MiniMap.this.dragStarPos);
                Vector2 newGroupPos = MiniMap.this.dragGroupStartPos.cpy().add(mouseDistance);
                MiniMap.this.groupOffset.setPosition(newGroupPos.x, newGroupPos.y);
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                Vector2 mouseDistance = new Vector2(x, y).sub(MiniMap.this.dragStarPos);
                Vector2 newGroupPos = MiniMap.this.dragGroupStartPos.cpy().add(mouseDistance);
                MiniMap.this.groupOffset.setPosition(newGroupPos.x, newGroupPos.y);
                MiniMap.this.updateOffsetOrigin();
            }
        });
        this.chunkGroup.addListener(new ClickListener(1){

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                MiniMap.this.markLocation();
            }
        });
    }

    private void generateMap() {
        Gdx.app.log("MewnBase", "Minimap: generating map for mega chunks [" + this.minMegaChunkX + "," + this.minMegaChunkY + "]..[" + this.maxMegaChunkX + "," + this.maxMegaChunkY + "]");
        long startTime = System.currentTimeMillis();
        int renderedMapChunks = 0;
        int cachedMapChunks = 0;
        for (int mX = this.minMegaChunkX; mX <= this.maxMegaChunkX; ++mX) {
            for (int mY = this.minMegaChunkY; mY <= this.maxMegaChunkY; ++mY) {
                this.chunkPixmap = new Pixmap(10 * MEGA_CHUNK, 10 * MEGA_CHUNK, Pixmap.Format.RGBA8888);
                Texture t = new Texture(WorldDebugMap.renderMegaChunk(this.chunkPixmap, this.gameScreen.world, mX, mY));
                this.disposableTextures.add(t);
                this.chunkColorState.put(t, Color.RED);
                ++renderedMapChunks;
                this.chunkPixmap.dispose();
                if (t != null) {
                    t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                }
                this.chunkTextures.put("" + mX + "," + mY, t);
            }
        }
        long endTime = System.currentTimeMillis();
        float amount = endTime - startTime;
        Gdx.app.log("MewnBase", "Map took " + (amount /= 1000.0f) + " seconds to be created.");
        Gdx.app.log("MewnBase", "Minimap: " + renderedMapChunks + " mega chunks rendered, " + cachedMapChunks + " chunks from cache.");
    }

    private void showMap() {
        this.playMapTileLoadSound();
        for (Map.Entry<String, Texture> entry : this.chunkTextures.entrySet()) {
            String[] coord = entry.getKey().split(",");
            int mX = Integer.parseInt(coord[0]);
            int mY = Integer.parseInt(coord[1]);
            Image i = new Image(entry.getValue());
            i.setSize(100 * MEGA_CHUNK, 100 * MEGA_CHUNK);
            int xPos = 100 * MEGA_CHUNK * (mX - this.minMegaChunkX);
            int yPos = 100 * MEGA_CHUNK * (mY - this.minMegaChunkY);
            i.setPosition(xPos, yPos);
            this.groupOffset.addActor(i);
        }
    }

    private void addTileIcons(int superChunkX, int superChunkY) {
        for (int cx = superChunkX * 10; cx < superChunkX * 10 + 10; ++cx) {
            for (int cy = superChunkX * 10; cy < superChunkY * 10 + 10; ++cy) {
                Chunk c = this.gameScreen.world.getChunk(cx, cy);
                if (c == null) continue;
                for (int x = 0; x < 10; ++x) {
                    for (int y = 0; y < 10; ++y) {
                        float ty;
                        float tx;
                        Tile t;
                        if (!this.gameScreen.world.chunkExists(cx, cy) || (t = c.getTile(x, y)) == null) continue;
                        if (t instanceof HabitatModule) {
                            Image baseIcon = new Image(this.skin.getDrawable("map/icons/habitat"));
                            baseIcon.setSize(14.0f, 14.0f);
                            float tx2 = (float)t.worldX / 10.0f * 100.0f - (float)(this.minMegaChunkX * 10 * 100 + 2);
                            float ty2 = (float)t.worldY / 10.0f * 100.0f - (float)(this.minMegaChunkY * 10 * 100 + 2);
                            System.out.println("base: " + t.worldX + "," + t.worldY + "  --> " + tx2 + "," + ty2);
                            baseIcon.setPosition(tx2, ty2);
                            this.tileIconGroup.addActor(baseIcon);
                        }
                        if (!(t instanceof ItemDropper)) continue;
                        ItemDropper id = (ItemDropper)t;
                        if (id.rdData.id.equals("plant")) {
                            Image baseIcon = new Image(this.skin.getDrawable("map/icons/plant"));
                            baseIcon.setSize(14.0f, 14.0f);
                            tx = (float)t.worldX / 10.0f * 100.0f - (float)(this.minMegaChunkX * 10 * 100 + 2);
                            ty = (float)t.worldY / 10.0f * 100.0f - (float)(this.minMegaChunkY * 10 * 100 + 2);
                            System.out.println("plant: " + t.worldX + "," + t.worldY + "  --> " + tx + "," + ty);
                            baseIcon.setPosition(tx, ty);
                            this.tileIconGroup.addActor(baseIcon);
                        }
                        if (!id.rdData.id.equals("rock")) continue;
                        Image baseIcon = new Image(this.skin.getDrawable("map/icons/big-rock"));
                        baseIcon.setSize(14.0f, 14.0f);
                        tx = (float)t.worldX / 10.0f * 100.0f - (float)(this.minMegaChunkX * 10 * 100 + 2);
                        ty = (float)t.worldY / 10.0f * 100.0f - (float)(this.minMegaChunkY * 10 * 100 + 2);
                        System.out.println("rock: " + t.worldX + "," + t.worldY + "  --> " + tx + "," + ty);
                        baseIcon.setPosition(tx, ty);
                        this.tileIconGroup.addActor(baseIcon);
                    }
                }
            }
        }
    }

    private void playMapTileLoadSound() {
        this.soundPitch = MathUtils.random(this.soundBasePitch - this.soundPitchVar, this.soundBasePitch + this.soundPitchVar);
        Audio.getInstance().playSound("sounds/textAnimator2.ogg", this.soundVolume, this.soundPitch);
    }

    private void showDisasters(int minChunkX, int minChunkY, int horzChunks, int vertChunks) {
        Rectangle mapLimitCoords = new Rectangle(minChunkX * 10 - 1, minChunkY * 10 - 1, horzChunks * 10 + 1, vertChunks * 10 + 1);
        for (BaseGroup g : this.gameScreen.world.baseManager.getBaseGroupList()) {
            for (BaseModule b : g.baseModuleList) {
                if (b.baseDisaster == null || !mapLimitCoords.contains(b.worldX, b.worldY)) continue;
                System.out.println("Found disaster at : " + b.worldX + ", " + b.worldY);
                Image disasterBase = new Image(this.gameScreen.skin.getDrawable("white"));
                disasterBase.setSize(10.0f, 10.0f);
                disasterBase.setOrigin(1);
                disasterBase.setColor(Color.RED);
                disasterBase.setRotation(45.0f);
                this.groupOffset.addActor(disasterBase);
                float tX = (float)b.worldX / 10.0f - (float)minChunkX;
                float drawX = tX * 100.0f;
                float tY = (float)b.worldY / 10.0f - (float)minChunkY;
                float drawY = tY * 100.0f;
                disasterBase.setPosition(drawX, drawY, 12);
                disasterBase.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.0f, 1.0f), (Action)Actions.alpha(1.0f), (Action)Actions.parallel((Action)Actions.scaleTo(8.0f, 8.0f, 0.4f, Interpolation.sine), (Action)Actions.fadeOut(0.4f)), (Action)Actions.delay(0.4f))));
            }
        }
    }

    private void showIcons() {
        for (Entity e : this.gameScreen.world.entityList) {
            if (e instanceof Buggie || e instanceof Tank) {
                Vehicle vehicle = (Vehicle)e;
                Gdx.app.debug("MewnBase", "Adding buggie/tank to map at: " + vehicle.getWorldXTile() + ", " + vehicle.getWorldYTile());
                this.addIcon("map/icons/map-buggie-icon", vehicle.getWorldXTile(), vehicle.getWorldYTile(), vehicle.getRotation(), 32, 32, null);
                continue;
            }
            if (!(e instanceof Npc)) continue;
            Npc n = (Npc)e;
            if (!n.discovered) continue;
            Gdx.app.debug("MewnBase", "Adding npc to map at: " + n.getWorldXTile() + ", " + n.getWorldYTile());
            this.addIcon("flow-arrow", n.getWorldXTile(), n.getWorldYTile(), 0.0f, 32, 32, null);
        }
    }

    private Image addIcon(String iconName, int worldTileX, int worldTileY, float rotation, int sizeW, int sizeH, ClickListener clickListener) {
        Group iconGroup = new Group();
        this.groupOffset.addActor(iconGroup);
        Image icon = new Image(this.gameScreen.skin.getDrawable(iconName));
        icon.setSize(sizeW, sizeH);
        icon.setOrigin(1);
        icon.setRotation(rotation);
        icon.setColor(Color.WHITE);
        icon.setPosition(0.0f, 0.0f, 1);
        iconGroup.addActor(icon);
        Vector2 mapPos = this.worldTileToMapPos(worldTileX, worldTileY);
        iconGroup.setPosition(mapPos.x + 5.0f, mapPos.y + 5.0f, 1);
        if (clickListener != null) {
            icon.addListener(clickListener);
        }
        icon.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.1f, 0.1f), (Action)Actions.parallel((Action)Actions.alpha(1.0f, 0.15f), (Action)Actions.scaleTo(1.1f, 1.1f, 0.15f)), (Action)Actions.scaleTo(1.0f, 1.0f, 0.05f)));
        return icon;
    }

    private void showLander(float minChunkX, float minChunkY) {
        Rectangle screenLimitCoords = new Rectangle(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2, MoonBase.SCREEN_WIDTH - 90, MoonBase.SCREEN_HEIGHT - 90);
        Image poi = new Image(this.gameScreen.skin.getDrawable("map/icons/map-lander-icon"));
        poi.setSize(50.0f, 50.0f);
        poi.setOrigin(1);
        this.groupOffset.addActor(poi);
        World cfr_ignored_0 = this.gameScreen.world;
        GridPoint2 gp = World.getGridPointFromPoolAndSet(this.gameScreen.world.lander.worldX, this.gameScreen.world.lander.worldY);
        Vector2 landerMapPos = this.worldTileToMapPos(gp.x, gp.y);
        poi.setPosition(landerMapPos.x + 5.0f, landerMapPos.y + 5.0f, 1);
    }

    private void showLaunchpads(float minChunkX, float minChunkY) {
        String navText = "Launchpads:\n";
        for (LaunchPad lp : this.gameScreen.world.baseManager.getLaunchPads()) {
            GridPoint2 gp = new GridPoint2(lp.worldX, lp.worldY);
            navText = navText + "(" + gp.x + ", " + gp.y + ")\n";
            Image launchPadIcon = new Image(this.gameScreen.skin.getDrawable("map/icons/map-launchpad-icon"));
            launchPadIcon.setSize(42.0f, 57.0f);
            launchPadIcon.setScaling(Scaling.fit);
            launchPadIcon.setOrigin(1);
            this.groupOffset.addActor(launchPadIcon);
            if (lp.hasRocket) {
                launchPadIcon.setDrawable(this.gameScreen.skin.getDrawable("map/icons/map-launchpad-rocket-icon"));
            }
            float tX = (float)gp.x / 10.0f - minChunkX;
            float drawX = tX * 100.0f;
            float tY = (float)gp.y / 10.0f - minChunkY;
            float drawY = tY * 100.0f;
            Vector2 navBeaconMapPos = this.worldTileToMapPos(gp.x, gp.y);
            Vector2 navBeaconStagePos = this.groupOffset.localToStageCoordinates(navBeaconMapPos.cpy());
            launchPadIcon.setPosition(navBeaconMapPos.x + 5.0f, navBeaconMapPos.y + 5.0f, 1);
        }
    }

    private void showNavBeacons(float minChunkX, float minChunkY, int horzChunks, int vertChunks) {
        Rectangle screenLimitCoords = new Rectangle(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2, MoonBase.SCREEN_WIDTH - 90, MoonBase.SCREEN_HEIGHT - 90);
        String navText = "Nav Beacons:\n";
        for (GridPoint2 gp : this.gameScreen.world.baseManager.getNavBeacons()) {
            navText = navText + "(" + gp.x + ", " + gp.y + ")\n";
            AdditiveImage navBeacon = new AdditiveImage(this.gameScreen.skin.getDrawable("white"));
            navBeacon.setSize(10.0f, 10.0f);
            navBeacon.setOrigin(1);
            navBeacon.setRotation(45.0f);
            navBeacon.setColor(Color.CYAN);
            this.groupOffset.addActor(navBeacon);
            float tX = (float)gp.x / 10.0f - minChunkX;
            float drawX = tX * 100.0f;
            float tY = (float)gp.y / 10.0f - minChunkY;
            float drawY = tY * 100.0f;
            Vector2 navBeaconMapPos = this.worldTileToMapPos(gp.x, gp.y);
            Vector2 navBeaconStagePos = this.groupOffset.localToStageCoordinates(navBeaconMapPos.cpy());
            System.out.println("nav: " + navBeaconStagePos);
            System.out.println("nav: inside rect");
            navBeacon.setPosition(navBeaconMapPos.x + 5.0f, navBeaconMapPos.y + 5.0f, 1);
            navBeacon.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.0f, 1.0f), (Action)Actions.alpha(1.0f), (Action)Actions.parallel((Action)Actions.scaleTo(8.0f, 8.0f, 0.7f, Interpolation.sine), (Action)Actions.parallel((Action)Actions.fadeOut(0.1f), (Action)Actions.fadeOut(0.6f))), (Action)Actions.delay(0.4f))));
        }
    }

    @Override
    public void update(float delta) {
        boolean scrolling = false;
        if (PlayerInput.scrolledDown()) {
            this.zoomMap(-1, delta);
            scrolling = true;
        }
        if (PlayerInput.scrolledUp()) {
            this.zoomMap(1, delta);
            scrolling = true;
        }
        if (!scrolling) {
            this.zoomMap(0, delta);
        }
        scrolling = false;
        this.hoverUpdate();
        Vector2 panDir = new Vector2(0.0f, 0.0f);
        if (PlayerInput.isDown(0)) {
            panDir.x = -1.0f;
        }
        if (PlayerInput.isDown(1)) {
            panDir.x = 1.0f;
        }
        if (PlayerInput.isDown(2)) {
            panDir.y = 1.0f;
        }
        if (PlayerInput.isDown(3)) {
            panDir.y = -1.0f;
        }
        this.panMap(panDir.x, panDir.y);
    }

    private void panMap(float x, float y) {
        float curX = this.groupOffset.getX();
        float curY = this.groupOffset.getY();
        x *= Gdx.graphics.getDeltaTime() * PAN_SPEED;
        y *= Gdx.graphics.getDeltaTime() * PAN_SPEED;
        this.groupOffset.setPosition(curX - (x /= savedZoom), curY - (y /= savedZoom));
    }

    private void hoverUpdate() {
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();
        this.screenMouse.set(x, y);
        this.mapMouse.set(this.groupOffset.screenToLocalCoordinates(this.screenMouse));
        int tileX = MathUtils.floor(this.mapMouse.x / 10.0f);
        int tileY = MathUtils.floor(this.mapMouse.y / 10.0f);
        String info = "";
        Tile t = this.gameScreen.world.getTile(tileX += this.minMegaChunkX * 100, tileY += this.minMegaChunkY * 100);
        if (t != null && t.chunk.gtDiscoveryArray[t.y * 10 + t.x]) {
            if (t.getMapName().length() > 0) {
                info = info + t.getMapName();
                this.hoverLabel.setText(info);
                this.hoverPanel.setSize(this.hoverLabel.getPrefWidth() + 30.0f, this.hoverLabel.getPrefHeight() + 20.0f);
                this.hoverPanel.setPosition(0.0f, 0.0f, 1);
                this.hoverTooltipGroup.setVisible(true);
            } else {
                this.hoverTooltipGroup.setVisible(false);
            }
        } else {
            Tile ft = this.gameScreen.world.getFloorTile(tileX, tileY);
            if (ft != null && ft.chunk.gtDiscoveryArray[ft.y * 10 + ft.x]) {
                info = info + ft.getMapName();
                this.hoverLabel.setText(info);
                this.hoverPanel.setSize(this.hoverLabel.getPrefWidth() + 30.0f, this.hoverLabel.getPrefHeight() + 20.0f);
                this.hoverPanel.setPosition(0.0f, 0.0f, 1);
                this.hoverTooltipGroup.setVisible(true);
            } else {
                this.hoverTooltipGroup.setVisible(false);
            }
        }
    }

    private void limitDrag() {
        System.out.println(this.groupOffset.getX() + ", " + this.groupOffset.getY());
    }

    private void zoomMap(int dir, float delta) {
        if (dir != 0) {
            float currentScale = this.mapZoomHolder.getScaleX();
            if (dir == 1) {
                currentScale -= 1.0f * delta;
            } else if (dir == -1) {
                currentScale += 1.0f * delta;
            }
            if (currentScale < 0.25f) {
                currentScale = 0.25f;
            }
            if (currentScale > 2.0f) {
                currentScale = 2.0f;
            }
            savedZoom = currentScale;
            this.updateOffsetOrigin();
            this.mapZoomHolder.setScale(currentScale);
        } else if (this.zoomDir != 0) {
            float currentScale = this.mapZoomHolder.getScaleX();
            if (this.zoomDir == 1) {
                currentScale -= 1.0f * delta;
            } else if (this.zoomDir == -1) {
                currentScale += 1.0f * delta;
            }
            if (currentScale < 0.25f) {
                currentScale = 0.25f;
            }
            if (currentScale > 2.0f) {
                currentScale = 2.0f;
            }
            savedZoom = currentScale;
            this.updateOffsetOrigin();
            this.mapZoomHolder.setScale(currentScale);
        }
    }

    @Override
    public void show() {
        super.show();
        this.chunkGroup = new Group();
        this.menuGroup.addActor(this.chunkGroup);
        this.chunkGroup.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.fadeIn(0.25f)));
        this.bg = new Image(this.skin.getDrawable("map/map-bg"));
        this.bg.setColor(0.0f, 0.7f, 1.0f, 1.0f);
        this.bg.setSize(MoonBase.SCREEN_WIDTH + 10, MoonBase.SCREEN_HEIGHT + 10);
        this.bg.setPosition(-5.0f, -5.0f);
        this.chunkGroup.addActor(this.bg);
        this.bg.toBack();
        this.mapZoomHolder = new Group();
        this.mapZoomHolder.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2);
        this.chunkGroup.addActor(this.mapZoomHolder);
        this.groupOffset = new Group();
        this.mapZoomHolder.addActor(this.groupOffset);
        this.tileIconGroup = new Group();
        this.groupOffset.addActor(this.tileIconGroup);
        this.uiGroup = new Group();
        this.menuGroup.addActor(this.uiGroup);
        NinePatch borderPatch = new NinePatch(this.baseScreen.skin.getRegion("map/map-border"), 100, 100, 120, 120);
        this.border = new Image(borderPatch);
        this.border.setSize(MoonBase.SCREEN_WIDTH + 4, MoonBase.SCREEN_HEIGHT + 4);
        this.border.setPosition(-2.0f, -2.0f);
        this.border.setTouchable(Touchable.disabled);
        this.uiGroup.addActor(this.border);
        this.border.addAction(Actions.sequence((Action)Actions.color(new Color(0.0f, 0.0f, 0.0f, 0.0f)), (Action)Actions.delay(0.2f), (Action)Actions.color(Color.WHITE, 0.2f)));
        NinePatch innerBorderPatch = new NinePatch(this.baseScreen.skin.getRegion("map/map-inner-border"), 200, 200, 105, 10);
        this.innerBorder = new Image(innerBorderPatch);
        this.innerBorder.setSize(MoonBase.SCREEN_WIDTH + 4 - 88, MoonBase.SCREEN_HEIGHT + 4 - 100);
        this.innerBorder.setPosition(44.0f, 50.0f);
        this.innerBorder.setTouchable(Touchable.disabled);
        this.uiGroup.addActor(this.innerBorder);
        this.innerBorder.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.delay(0.25f), (Action)Actions.alpha(1.0f, 0.25f)));
        NinePatch introBorderPatch = new NinePatch(this.baseScreen.skin.getRegion("map/map-border-intro"), 50, 50, 50, 50);
        Image introBorder = new Image(introBorderPatch);
        introBorder.setSize(MoonBase.SCREEN_WIDTH - 60, MoonBase.SCREEN_HEIGHT - 60);
        introBorder.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2, 1);
        introBorder.setOrigin(1);
        introBorder.setTouchable(Touchable.disabled);
        this.uiGroup.addActor(introBorder);
        introBorder.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.88f, 0.88f), (Action)Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, 0.3f, Interpolation.sineOut), (Action)Actions.sequence((Action)Actions.alpha(0.8f, 0.2f), (Action)Actions.fadeOut(0.1f))), (Action)Actions.delay(0.5f)));
        Group widgetGroup = new Group();
        this.uiGroup.addActor(widgetGroup);
        widgetGroup.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.delay(0.2f), (Action)Actions.fadeIn(0.25f)));
        Button.ButtonStyle bs = new Button.ButtonStyle();
        bs.up = this.skin.getDrawable("map/map-close-x");
        bs.over = this.skin.getDrawable("map/map-close-x-hover");
        bs.down = this.skin.getDrawable("map/map-close-x-pressed");
        this.btnClose = new Button(bs);
        this.btnClose.setPosition(MoonBase.SCREEN_WIDTH - 70, MoonBase.SCREEN_HEIGHT - 68, 18);
        this.btnClose.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MiniMap.this.back();
            }
        });
        widgetGroup.addActor(this.btnClose);
        Group zoomGroup = new Group();
        zoomGroup.setPosition(174.0f, 71.0f);
        widgetGroup.addActor(zoomGroup);
        Button.ButtonStyle bstyleZoomIn = new Button.ButtonStyle();
        bstyleZoomIn.up = this.skin.getDrawable("map/map-zoom-in");
        bstyleZoomIn.over = this.skin.getDrawable("map/map-zoom-in-hover");
        bstyleZoomIn.down = this.skin.getDrawable("map/map-zoom-in");
        this.btnZoomIn = new Button(bstyleZoomIn);
        this.btnZoomIn.addListener(new ClickListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                MiniMap.this.zoomDir = -1;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                MiniMap.this.zoomDir = 0;
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                MiniMap.this.createTooltip(Localization.get("map_zoomIn"));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                MiniMap.this.removeTooltip();
            }
        });
        zoomGroup.addActor(this.btnZoomIn);
        this.zoomDiv = new Image(this.baseScreen.skin.getDrawable("map/map-zoom-div"));
        this.zoomDiv.setPosition(62.0f, -5.0f);
        zoomGroup.addActor(this.zoomDiv);
        Button.ButtonStyle bstyleZoomOut = new Button.ButtonStyle();
        bstyleZoomOut.up = this.skin.getDrawable("map/map-zoom-out");
        bstyleZoomOut.over = this.skin.getDrawable("map/map-zoom-out-hover");
        bstyleZoomOut.down = this.skin.getDrawable("map/map-zoom-out");
        this.btnZoomOut = new Button(bstyleZoomOut);
        this.btnZoomOut.setPosition(62.0f + this.zoomDiv.getWidth() - 5.0f + 3.0f, 16.0f);
        this.btnZoomOut.addListener(new ClickListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                MiniMap.this.zoomDir = 1;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                MiniMap.this.zoomDir = 0;
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                MiniMap.this.createTooltip(Localization.get("map_zoomOut"));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                MiniMap.this.removeTooltip();
            }
        });
        zoomGroup.addActor(this.btnZoomOut);
        Button.ButtonStyle bstyleCenter = new Button.ButtonStyle();
        bstyleCenter.up = this.skin.getDrawable("map/map-locate");
        bstyleCenter.over = this.skin.getDrawable("map/map-locate-hover");
        bstyleCenter.down = this.skin.getDrawable("map/map-locate");
        this.btnRecenter = new Button(bstyleCenter);
        this.btnRecenter.setPosition(65.0f, 59.0f, 12);
        this.btnRecenter.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MiniMap.this.centerOnPlayer(true);
                MiniMap.this.updateOffsetOrigin();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                MiniMap.this.createTooltip(Localization.get("map_recenterOnPlayer"));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                MiniMap.this.removeTooltip();
            }
        });
        widgetGroup.addActor(this.btnRecenter);
        NinePatch roundedbox = new NinePatch(this.baseScreen.skin.getRegion("roundedbox"), 18, 18, 18, 18);
        NinePatchDrawable npd = new NinePatchDrawable(roundedbox);
        roundedbox.setColor(new Color(0.0f, 0.0f, 0.0f, 0.6f));
        Image bg = new Image(npd);
        bg.setPosition(132.0f, MoonBase.SCREEN_HEIGHT - 97, 10);
        widgetGroup.addActor(bg);
        Image planetBase = new Image(this.baseScreen.skin.getDrawable("small-circle"));
        planetBase.setColor(Color.BLACK);
        planetBase.setSize(112.0f, 112.0f);
        planetBase.setOrigin(1);
        planetBase.setPosition(45.0f, MoonBase.SCREEN_HEIGHT - 26 - 15, 10);
        planetBase.setTouchable(Touchable.disabled);
        widgetGroup.addActor(planetBase);
        Image planet = new Image(this.baseScreen.skin.getDrawable("map/map-planet"));
        planet.setOrigin(1);
        planet.setPosition(30.0f, MoonBase.SCREEN_HEIGHT - 26, 10);
        planet.setTouchable(Touchable.disabled);
        widgetGroup.addActor(planet);
        Label planetSurvey = new Label((CharSequence)Localization.get("map_planetSurvey"), this.labelStyle);
        planetSurvey.setFontScale(0.35f);
        planetSurvey.setColor(Menu.MENU_COLOR);
        planetSurvey.setPosition(172.0f, MoonBase.SCREEN_HEIGHT - 85, 8);
        widgetGroup.addActor(planetSurvey);
        GameScreen gameScreen = this.gameScreen;
        Label planetName = new Label((CharSequence)gameScreen.game.getCurrentMission().getPlanetName(), this.labelStyle);
        planetName.setFontScale(0.5f);
        planetName.setColor(Menu.MENU_COLOR);
        planetName.setPosition(172.0f, MoonBase.SCREEN_HEIGHT - 113, 8);
        widgetGroup.addActor(planetName);
        this.pauseLabel = new Label((CharSequence)("- " + Localization.get("paused") + " -"), this.labelStyle);
        this.pauseLabel.setFontScale(0.5f);
        this.pauseLabel.setAlignment(1);
        this.pauseLabel.setColor(Menu.MENU_COLOR);
        this.pauseLabel.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT - 85, 1);
        widgetGroup.addActor(this.pauseLabel);
        bg.setWidth(planetSurvey.getPrefWidth() + 100.0f);
        bg.setHeight(planetSurvey.getPrefHeight() + planetName.getPrefHeight() + 18.0f);
        this.setupHoverLabel();
        Group wayPointGroup = new Group();
        wayPointGroup.debug();
        wayPointGroup.setPosition(MoonBase.SCREEN_WIDTH - 75, 80.0f);
        NinePatchDrawable npd2 = new NinePatchDrawable(roundedbox);
        roundedbox.setColor(new Color(0.0f, 0.0f, 0.0f, 0.6f));
        Image bg2 = new Image(npd2);
        wayPointGroup.addActor(bg2);
        Label placeWaypointHint = new Label((CharSequence)Localization.get("map_placeWaypointHint"), this.labelStyle);
        placeWaypointHint.setFontScale(0.35f);
        placeWaypointHint.setColor(Menu.MENU_COLOR);
        placeWaypointHint.getColor().a = 0.75f;
        placeWaypointHint.setAlignment(16);
        placeWaypointHint.setTouchable(Touchable.disabled);
        placeWaypointHint.setPosition(-20.0f, -12.0f, 20);
        wayPointGroup.addActor(placeWaypointHint);
        bg2.setWidth(placeWaypointHint.getPrefWidth() + 40.0f);
        bg2.setHeight(placeWaypointHint.getPrefHeight() + 20.0f);
        bg2.setPosition(0.0f, 0.0f, 20);
        widgetGroup.addActor(wayPointGroup);
    }

    private void setupHoverLabel() {
        this.hoverTooltipGroup = new Group();
        this.hoverTooltipGroup.setPosition(MoonBase.SCREEN_WIDTH / 2, 85.0f);
        this.uiGroup.addActor(this.hoverTooltipGroup);
        this.hoverInnerGroup = new Group();
        this.hoverInnerGroup.setPosition(15.0f, 15.0f);
        this.hoverTooltipGroup.addActor(this.hoverInnerGroup);
        NinePatch roundedbox = new NinePatch(this.baseScreen.skin.getRegion("roundedbox"), 18, 18, 18, 18);
        this.hoverPanel = new Image(roundedbox);
        this.hoverPanel.setColor(0.0f, 0.0f, 0.0f, 0.8f);
        this.hoverPanel.setPosition(0.0f, 0.0f, 1);
        this.hoverInnerGroup.addActor(this.hoverPanel);
        this.hoverPanel.setTouchable(Touchable.disabled);
        this.hoverTooltipTable = new Table();
        this.hoverTooltipTable.setPosition(0.0f, 0.0f);
        this.hoverTooltipTable.center();
        this.hoverInnerGroup.addActor(this.hoverTooltipTable);
        this.hoverLabel = new Label((CharSequence)"---", this.baseScreen.labelStyle);
        this.hoverLabel.setFontScale(0.35f);
        this.hoverLabel.setColor(1.0f, 1.0f, 1.0f, 0.8f);
        this.hoverLabel.setAlignment(1, 1);
        this.hoverLabel.setTouchable(Touchable.disabled);
        this.hoverTooltipTable.add(this.hoverLabel).fillX();
        this.hoverTooltipTable.layout();
        this.hoverPanel.setSize(this.hoverTooltipTable.getPrefWidth() + 30.0f, this.hoverTooltipTable.getPrefHeight() + 20.0f);
        this.hoverPanel.setPosition(-15.0f, -10.0f);
    }

    @Override
    protected void finishedShowAnim() {
        int[] lim = this.gameScreen.world.chunkLoader.getMinMaxChunkCoords();
        int rawMinX = lim[0];
        int rawMinY = lim[1];
        int rawMaxX = lim[2];
        int rawMaxY = lim[3];
        this.minChunkX = rawMinX;
        this.minChunkY = rawMinY;
        this.maxChunkX = rawMaxX;
        this.maxChunkY = rawMaxY;
        int spanX = this.maxChunkX - this.minChunkX;
        int spanY = this.maxChunkY - this.minChunkY;
        int maxSpan = CHUNKS_RADIUS_FROM_CURRENT * 2 + 1;
        boolean clamped = false;
        if (spanX > maxSpan || spanY > maxSpan) {
            int centerChunkX = Chunk.getChunkX(this.gameScreen.world.player.getX());
            int centerChunkY = Chunk.getChunkY(this.gameScreen.world.player.getY());
            int targetMinX = centerChunkX - CHUNKS_RADIUS_FROM_CURRENT;
            int targetMaxX = centerChunkX + CHUNKS_RADIUS_FROM_CURRENT;
            int targetMinY = centerChunkY - CHUNKS_RADIUS_FROM_CURRENT;
            int targetMaxY = centerChunkY + CHUNKS_RADIUS_FROM_CURRENT;
            if (targetMinX < rawMinX) {
                targetMaxX += rawMinX - targetMinX;
                targetMinX = rawMinX;
            }
            if (targetMaxX > rawMaxX) {
                targetMinX -= targetMaxX - rawMaxX;
                targetMaxX = rawMaxX;
            }
            if (targetMinY < rawMinY) {
                targetMaxY += rawMinY - targetMinY;
                targetMinY = rawMinY;
            }
            if (targetMaxY > rawMaxY) {
                targetMinY -= targetMaxY - rawMaxY;
                targetMaxY = rawMaxY;
            }
            this.minChunkX = targetMinX;
            this.maxChunkX = targetMaxX;
            this.minChunkY = targetMinY;
            this.maxChunkY = targetMaxY;
            clamped = true;
        }
        Gdx.app.log("MewnBase", "Minimap: bounds chunks [" + this.minChunkX + "," + this.minChunkY + "]..[" + this.maxChunkX + "," + this.maxChunkY + "] (spanX=" + (this.maxChunkX - this.minChunkX) + ", spanY=" + (this.maxChunkY - this.minChunkY) + ", clamped=" + clamped + ")");
        this.minMegaChunkX = MathUtils.floor((float)this.minChunkX / 10.0f);
        this.maxMegaChunkX = MathUtils.floor((float)this.maxChunkX / 10.0f);
        this.minMegaChunkY = MathUtils.floor((float)this.minChunkY / 10.0f);
        this.maxMegaChunkY = MathUtils.floor((float)this.maxChunkY / 10.0f);
        this.minDragPos.set(this.minMegaChunkX * 100, this.minMegaChunkY * 100);
        this.maxDragPos.set(this.maxMegaChunkX * 100, this.maxMegaChunkY * 100);
        this.generateMap();
        this.showMap();
        this.showNavBeacons(this.minChunkX, this.minChunkY, CHUNKS_RADIUS_FROM_CURRENT * 2 + 1, CHUNKS_RADIUS_FROM_CURRENT * 2 + 1);
        this.showLaunchpads(this.minChunkX, this.minChunkY);
        this.showSignIcons(this.minChunkX, this.minChunkY);
        this.showIcons();
        this.showLander(this.minChunkX, this.minChunkY);
        this.addPlayerMarkedLocations();
        this.addPlayerMarkers();
        this.centerOnPlayer(false);
        this.updateOffsetOrigin();
    }

    private void showSignIcons(int minChunkX, int minChunkY) {
        for (WoodSign sign : this.gameScreen.world.baseManager.getSigns()) {
            GridPoint2 gp = new GridPoint2(sign.worldX, sign.worldY);
            if (sign.getIcon().length() == 0) continue;
            Image signIcon = new Image(this.gameScreen.skin.getDrawable(sign.getIcon()));
            signIcon.setSize(60.0f, 40.0f);
            signIcon.setScaling(Scaling.fit);
            signIcon.setOrigin(1);
            this.groupOffset.addActor(signIcon);
            float tX = (float)gp.x / 10.0f - (float)minChunkX;
            float drawX = tX * 100.0f;
            float tY = (float)gp.y / 10.0f - (float)minChunkY;
            float drawY = tY * 100.0f;
            Vector2 navBeaconMapPos = this.worldTileToMapPos(gp.x, gp.y);
            signIcon.setPosition(navBeaconMapPos.x + 5.0f, navBeaconMapPos.y + 5.0f, 1);
        }
    }

    private void updatePosLabel(Vector2 offset) {
        this.pauseLabel.setText(offset.x + ", " + offset.y);
    }

    private void updateOffsetOrigin() {
    }

    private Vector2 worldTileToMapPos(int worldX, int worldY) {
        Vector2 mapPos = new Vector2(0.0f, 0.0f);
        float pX = (float)worldX / (10.0f * (float)MEGA_CHUNK) - (float)this.minMegaChunkX;
        float pY = (float)worldY / (10.0f * (float)MEGA_CHUNK) - (float)this.minMegaChunkY;
        mapPos.set(pX *= (float)(100 * MEGA_CHUNK), pY *= (float)(100 * MEGA_CHUNK));
        return mapPos;
    }

    private void addPlayerMarkers() {
        addPlayerMarkerFor(this.gameScreen.world.player);
        for (Player remote : this.gameScreen.getRemotePlayers()) {
            if (remote == null) continue;
            addPlayerMarkerFor(remote);
        }
    }

    private void addPlayerMarkerFor(Player player) {
        int tileX = MathUtils.floor(player.getXPos() / Tile.TILE_SIZE);
        int tileY = MathUtils.floor(player.getYPos() / Tile.TILE_SIZE);
        Vector2 playerMapPos = this.worldTileToMapPos(tileX, tileY);

        Group markerGroup = new Group();
        this.groupOffset.addActor(markerGroup);
        markerGroup.setPosition(playerMapPos.x, playerMapPos.y, 1);

        Image playerPos = new Image(this.gameScreen.skin.getDrawable("equippedFlag"));
        playerPos.setSize(48.0f, 48.0f);
        playerPos.setOrigin(1);
        playerPos.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(0.9f, 0.9f, 1.0f, Interpolation.sine), (Action)Actions.scaleTo(1.1f, 1.1f, 1.0f, Interpolation.sine))));
        playerPos.setPosition(0.0f, 0.0f, 1);
        markerGroup.addActor(playerPos);

        String name = (player.name != null && !player.name.isEmpty()) ? player.name : "Player";
        Label nameLabel = new Label((CharSequence)name, this.baseScreen.labelStyle);
        nameLabel.setFontScale(0.35f);
        nameLabel.setColor(Color.WHITE);
        nameLabel.setAlignment(1);
        nameLabel.setPosition(52.0f, 14.0f, 1);
        markerGroup.addActor(nameLabel);
    }

    private void centerOnPlayer(boolean animate) {
        Vector2 playerMapPos = this.worldTileToMapPos(this.gameScreen.world.player.getX(), this.gameScreen.world.player.getY());
        System.out.println("Center map offset " + playerMapPos.x + ", " + playerMapPos.y);
        if (!animate) {
            this.groupOffset.setPosition(-playerMapPos.x, -playerMapPos.y);
            this.groupOffset.setScale(1.0f);
        } else {
            this.groupOffset.clearActions();
            this.groupOffset.addAction(Actions.parallel((Action)Actions.moveTo(-playerMapPos.x, -playerMapPos.y, 0.5f, Interpolation.sine), (Action)Actions.scaleTo(1.0f, 1.0f, 0.5f, Interpolation.sine)));
        }
    }

    @Override
    public void back() {
        this.gameScreen.world.pauseSimulation(false);
        MessageManager.getInstance().removeListener((Telegraph)this, 40);
        MessageManager.getInstance().removeListener((Telegraph)this, 41);
        for (Texture t : this.disposableTextures) {
            t.dispose();
        }
        this.disposableTextures.clear();
        this.chunkColorState.clear();
        this.chunkTextures.clear();
        this.gameScreen.world.player.playerAnimController.closeTablet();
        super.back();
    }

    @Override
    public void handleInput() {
        if (PlayerInput.wasPressed(22)) {
            this.back();
        }
        super.handleInput();
    }

    public void markLocation() {
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();
        this.screenMouse.set(x, y);
        this.mapMouse.set(this.groupOffset.screenToLocalCoordinates(this.screenMouse));
        int tileX = MathUtils.floor(this.mapMouse.x / 10.0f);
        int tileY = MathUtils.floor(this.mapMouse.y / 10.0f);
        if (!this.doesMarkExist(tileX += this.minMegaChunkX * 100, tileY += this.minMegaChunkY * 100)) {
            this.gameScreen.world.player.addMarkedLocation(tileX, tileY);
            this.addMarkedLocation(new GridPoint2(tileX, tileY));
        } else {
            this.gameScreen.world.player.removeMarkedLocation(tileX, tileY);
        }
    }

    private boolean doesMarkExist(int worldX, int worldY) {
        GridPoint2 gp = World.getGridPointFromPoolAndSet(worldX, worldY);
        boolean b = this.gameScreen.world.player.markedMapLocations.contains(gp, false);
        World.gridPointPool.free(gp);
        return b;
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        switch (msg.message) {
            case 40: 
            case 41: {
                for (Image i : this.playerMarks) {
                    i.addAction(Actions.rotateBy(90.0f, 1.0f));
                    i.remove();
                }
                this.playerMarks.clear();
                this.addPlayerMarkedLocations();
                return true;
            }
        }
        return false;
    }

    private void addPlayerMarkedLocations() {
        for (GridPoint2 gp : this.gameScreen.world.player.markedMapLocations) {
            this.addMarkedLocation(gp);
        }
    }

    public void addMarkedLocation(final GridPoint2 gp) {
        Image i = this.addIcon("marked-map-location", gp.x, gp.y, 0.0f, 34, 34, new ClickListener(1){
            int worldX1 = gp.x;
            int worldY1 = gp.y;
        });
        i.setColor(Vars.markedLocationColor);
        this.playerMarks.add(i);
    }
}
