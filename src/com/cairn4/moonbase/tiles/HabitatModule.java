/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.NpcBonuses;
import com.cairn4.moonbase.tiles.Airlock;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.BaseDamageCallback;
import com.cairn4.moonbase.tiles.behaviors.BaseDisasterTimer;
import com.cairn4.moonbase.tiles.behaviors.BaseResourceStorageBehavior;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.Damageable;
import com.cairn4.moonbase.tiles.behaviors.IntValueBehavior;
import com.cairn4.moonbase.tiles.disasters.AirLeak;
import com.cairn4.moonbase.tiles.disasters.BaseFire;
import com.cairn4.moonbase.ui.HudNotificationData;
import com.cairn4.moonbase.ui.Localization;

public class HabitatModule
extends BaseModule {
    String[] roomDrawableNames;
    Image shadow;
    Image corners;
    Image corner_floor_tr;
    Image corner_floor_br;
    Image corner_floor_tl;
    Image corner_floor_bl;
    Image corner_tr;
    Image corner_br;
    Image corner_tl;
    Image corner_bl;
    Image wall_n;
    Image wall_e;
    Image wall_s;
    Image wall_w;
    Image connector_n;
    Image connector_e;
    Image connector_s;
    Image connector_w;
    protected Group wallsGroup;
    Fixture wall_n_fix;
    Fixture wall_e_fix;
    Fixture wall_s_fix;
    Fixture wall_w_fix;
    Fixture corner_tr_fix;
    Fixture corner_br_fix;
    Fixture corner_tl_fix;
    Fixture corner_bl_fix;
    public BaseResourceStorageBehavior oxygenStorageBehavior;
    public Damageable damageableBehavior;
    public IntValueBehavior intColorBehavior;
    public IntValueBehavior intFloorStyle;
    private BaseDisasterTimer baseDisasterTimer;
    public static Color[] habColors = new Color[]{Color.WHITE, Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.PINK, Color.PURPLE, Color.GRAY};

    public HabitatModule(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.type = Tile.types.habitat;
        this.powerPriority = 2;
        this.powerDrawRate = 1.5f;
        this.walkable = true;
        this.blocker = false;
        this.hasAir = true;
        this.hasPower = true;
        this.roomDrawableNames = new String[]{"room", "room-end", "room-hall", "room-3way", "room-4way"};
        this.roomDrawableNames[0] = "room";
        this.clearGroundTile();
        this.addOxygenStorageBehavior();
        this.setupPhysics("habitat");
        this.createWalls();
        this.statusGroup.toFront();
        this.updateBases();
        this.setupBaseDiasterTimer();
        this.intColorBehavior = new IntValueBehavior();
        this.intColorBehavior.setId("intColorBehavior");
        this.intColorBehavior.value = 0;
        this.behaviorList.add(this.intColorBehavior);
        this.intFloorStyle = new IntValueBehavior();
        this.intFloorStyle.setId("intFloorStyle");
        this.intFloorStyle.value = 0;
        this.behaviorList.add(this.intFloorStyle);
        this.setFloorStyle();
    }

    @Override
    public void startBehaviors() {
        this.updateHabColor();
        this.updateFloorSprite();
        super.startBehaviors();
    }

    protected void setFloorStyle() {
        String floorSprite = "habitat-floor";
        MoonBase.log("hab : " + this.x + ", " + this.y);
        if (this.x % 2 == 0 && this.y % 2 != 0 || this.x % 2 != 0 && this.y % 2 == 0) {
            this.image.setOrigin(1);
            MoonBase.log("variation 1 mod 0");
            if (MathUtils.random() > 0.6f) {
                int random = MathUtils.random(1, 4);
                floorSprite = floorSprite + "-alt" + random;
                this.intFloorStyle.value = random;
            }
        } else {
            MoonBase.log("variation 2");
            if (MathUtils.random() > 0.6f) {
                int random = MathUtils.random(3, 6);
                floorSprite = floorSprite + "-alt" + random;
                this.intFloorStyle.value = random;
            }
        }
        if (MathUtils.randomBoolean()) {
            this.image.setScaleX(-1.0f);
        }
        this.image.setDrawable(this.world.gameScreen.skin.getDrawable(floorSprite));
    }

    protected void updateFloorSprite() {
        if (this.intFloorStyle.value == 0) {
            this.image.setDrawable(this.world.gameScreen.skin.getDrawable("habitat-floor"));
        } else {
            this.image.setDrawable(this.world.gameScreen.skin.getDrawable("habitat-floor-alt" + this.intFloorStyle.value));
        }
    }

    @Override
    public void animateSpawn() {
        super.animateSpawn();
        this.wallsGroup.addAction(HabitatModule.getSpawnAnimation());
    }

    public void addDamageableBehavior() {
        this.damageableBehavior = new Damageable();
        this.damageableBehavior.setMaxHealth(20.0f);
        this.damageableBehavior.thresholdModerateDamage = 0.9f;
        this.damageableBehavior.thresholdCritical = 0.85f;
        this.damageableBehavior.callback = new BaseDamageCallback(this){

            @Override
            public void damageResult(float healthPercent) {
                MoonBase.log("Damage result: " + healthPercent);
            }

            @Override
            public void destroyed() {
                System.out.println("Hab destroyed!");
                HabitatModule.this.readyToRemove = true;
            }

            @Override
            public void normalDamage() {
            }

            @Override
            public void moderateDamage() {
                HabitatModule.this.baseDisaster = new AirLeak(this.baseModule);
            }

            @Override
            public void criticalDamage() {
                HabitatModule.this.baseDisaster = new BaseFire(this.baseModule);
            }

            @Override
            public void repaired() {
            }
        };
        this.behaviorList.add(this.damageableBehavior);
    }

    public void addOxygenStorageBehavior() {
        this.oxygenStorageBehavior = new BaseResourceStorageBehavior();
        this.oxygenStorageBehavior.setId("habOxygen");
        this.oxygenStorageBehavior.amount = 0.0f;
        this.oxygenStorageBehavior.capacity = 25.0f;
        this.oxygenStorageBehavior.type = BaseResources.oxygen;
        this.behaviorList.add(this.oxygenStorageBehavior);
    }

    public void setupBaseDiasterTimer() {
        this.baseDisasterTimer = new BaseDisasterTimer();
        this.baseDisasterTimer.minDelay = 500.0f;
        this.baseDisasterTimer.maxDelay = 1000.0f;
        this.baseDisasterTimer.chance = 0.5f;
        if (this.world.techManager.getTech((String)"lessLeaks").unlocked) {
            this.upgradeLessLeaks(false);
        }
        this.baseDisasterTimer.baseModule = this;
        this.baseDisasterTimer.start();
        this.behaviorList.add(this.baseDisasterTimer);
    }

    public void upgradeLessLeaks(boolean updateCurrentDelay) {
        this.baseDisasterTimer.minDelay = 1000.0f;
        this.baseDisasterTimer.maxDelay = 1500.0f;
        this.baseDisasterTimer.chance = 0.75f;
        if (updateCurrentDelay) {
            this.baseDisasterTimer.newDelay();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.wallsGroup.setVisible(visible);
    }

    @Override
    public Color getMinimapColor() {
        if (this.intColorBehavior.value == 0) {
            return Color.GOLD;
        }
        return Vars.habColors[this.intColorBehavior.value];
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "habitat";
    }

    protected void createWalls() {
        this.wallsGroup = new Group();
        this.wallsGroup.setOrigin(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f);
        this.wallsGroup.setPosition(this.group.getX(), this.group.getY());
        this.world.gameScreen.wallGroup.addActor(this.wallsGroup);
        float offset = -6.0f;
        this.connector_w = new Image(this.world.gameScreen.skin.getDrawable("habitat-connector-n"));
        this.connector_w.setSize(200.0f * SCALE, 128.0f * SCALE);
        this.connector_w.setOrigin(4);
        this.connector_w.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f, 4);
        this.connector_w.setRotation(90.0f);
        this.group.addActor(this.connector_w);
        this.connector_n = new Image(this.world.gameScreen.skin.getDrawable("habitat-connector-n"));
        this.connector_n.setSize(200.0f * SCALE, 128.0f * SCALE);
        this.connector_n.setOrigin(4);
        this.connector_n.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f, 4);
        this.group.addActor(this.connector_n);
        this.connector_e = new Image(this.world.gameScreen.skin.getDrawable("habitat-connector-n"));
        this.connector_e.setSize(200.0f * SCALE, 128.0f * SCALE);
        this.connector_e.setOrigin(4);
        this.connector_e.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f, 4);
        this.connector_e.setRotation(270.0f);
        this.group.addActor(this.connector_e);
        this.connector_s = new Image(this.world.gameScreen.skin.getDrawable("habitat-connector-n"));
        this.connector_s.setSize(200.0f * SCALE, 128.0f * SCALE);
        this.connector_s.setOrigin(4);
        this.connector_s.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f, 4);
        this.connector_s.setRotation(180.0f);
        this.group.addActor(this.connector_s);
        this.wall_w = new Image(this.world.gameScreen.skin.getDrawable("habitat-wall"));
        this.wall_w.setSize(38.0f * SCALE, 170.0f * SCALE);
        this.wall_w.setOrigin(1);
        this.wall_w.setPosition(6.0f, TILE_SIZE / 2.0f, 1);
        this.wallsGroup.addActor(this.wall_w);
        this.wall_n = new Image(this.world.gameScreen.skin.getDrawable("habitat-wall"));
        this.wall_n.setSize(38.0f * SCALE, 170.0f * SCALE);
        this.wall_n.setOrigin(1);
        this.wall_n.setRotation(-90.0f);
        this.wall_n.setPosition(TILE_SIZE / 2.0f, TILE_SIZE + -6.0f, 1);
        this.wallsGroup.addActor(this.wall_n);
        this.wall_e = new Image(this.world.gameScreen.skin.getDrawable("habitat-wall"));
        this.wall_e.setSize(38.0f * SCALE, 170.0f * SCALE);
        this.wall_e.setOrigin(1);
        this.wall_e.setRotation(180.0f);
        this.wall_e.setPosition(TILE_SIZE + -6.0f, TILE_SIZE / 2.0f, 1);
        this.wallsGroup.addActor(this.wall_e);
        this.wall_s = new Image(this.world.gameScreen.skin.getDrawable("habitat-wall"));
        this.wall_s.setSize(38.0f * SCALE, 170.0f * SCALE);
        this.wall_s.setOrigin(1);
        this.wall_s.setRotation(90.0f);
        this.wall_s.setPosition(TILE_SIZE / 2.0f, 6.0f, 1);
        this.wallsGroup.addActor(this.wall_s);
        float scalingCorner = 1.0f;
        float nearCornerOffset = -((Tile.TILE_OUTER - Tile.TILE_SIZE) / 2.0f);
        float farCornerOffset = TILE_SIZE + (Tile.TILE_OUTER - Tile.TILE_SIZE) / 2.0f;
        this.corner_floor_tl = new Image(this.world.gameScreen.skin.getDrawable("habitat-corner-floor"));
        this.corner_floor_tl.setOrigin(12);
        this.corner_floor_tl.setScale(scalingCorner);
        this.corner_floor_tl.setRotation(270.0f);
        this.corner_floor_tl.setPosition(nearCornerOffset, farCornerOffset);
        this.group.addActor(this.corner_floor_tl);
        this.corner_floor_tr = new Image(this.world.gameScreen.skin.getDrawable("habitat-corner-floor"));
        this.corner_floor_tr.setOrigin(12);
        this.corner_floor_tr.setScale(scalingCorner);
        this.corner_floor_tr.setRotation(180.0f);
        this.corner_floor_tr.setPosition(farCornerOffset, farCornerOffset);
        this.group.addActor(this.corner_floor_tr);
        this.corner_floor_bl = new Image(this.world.gameScreen.skin.getDrawable("habitat-corner-floor"));
        this.corner_floor_bl.setRotation(0.0f);
        this.corner_floor_bl.setScale(scalingCorner);
        this.corner_floor_bl.setPosition(nearCornerOffset, nearCornerOffset);
        this.group.addActor(this.corner_floor_bl);
        this.corner_floor_br = new Image(this.world.gameScreen.skin.getDrawable("habitat-corner-floor"));
        this.corner_floor_br.setRotation(90.0f);
        this.corner_floor_br.setScale(scalingCorner);
        this.corner_floor_br.setPosition(farCornerOffset, nearCornerOffset);
        this.group.addActor(this.corner_floor_br);
        float scaling = 0.76f;
        this.corner_tl = new Image(this.world.gameScreen.skin.getDrawable("habitat-corner"));
        this.corner_tl.setOrigin(12);
        this.corner_tl.setScale(scaling);
        this.corner_tl.setRotation(270.0f);
        this.corner_tl.setPosition(nearCornerOffset, farCornerOffset);
        this.wallsGroup.addActor(this.corner_tl);
        this.corner_tr = new Image(this.world.gameScreen.skin.getDrawable("habitat-corner"));
        this.corner_tr.setOrigin(12);
        this.corner_tr.setScale(scaling);
        this.corner_tr.setRotation(180.0f);
        this.corner_tr.setPosition(farCornerOffset, farCornerOffset);
        this.wallsGroup.addActor(this.corner_tr);
        this.corner_bl = new Image(this.world.gameScreen.skin.getDrawable("habitat-corner"));
        this.corner_bl.setRotation(0.0f);
        this.corner_bl.setScale(scaling);
        this.corner_bl.setPosition(nearCornerOffset, nearCornerOffset);
        this.wallsGroup.addActor(this.corner_bl);
        this.corner_br = new Image(this.world.gameScreen.skin.getDrawable("habitat-corner"));
        this.corner_br.setRotation(90.0f);
        this.corner_br.setScale(scaling);
        this.corner_br.setPosition(farCornerOffset, nearCornerOffset);
        this.wallsGroup.addActor(this.corner_br);
    }

    @Override
    protected void setupPhysics(String loader) {
        super.setupPhysics(null);
        FixtureDef fd = new FixtureDef();
        fd.density = 1.0f;
        fd.friction = 0.5f;
        fd.restitution = 0.3f;
        fd.filter.categoryBits = this.categoryBits;
        fd.filter.maskBits = (short)(this.maskBits + this.categoryBits);
        this.world.bodyEditorLoader.attachFixture(this.body, "habitat_wall_n", fd, 0.5f);
        this.wall_n_fix = this.body.getFixtureList().get(0);
        this.world.bodyEditorLoader.attachFixture(this.body, "habitat_wall_s", fd, 0.5f);
        this.wall_s_fix = this.body.getFixtureList().get(1);
        this.world.bodyEditorLoader.attachFixture(this.body, "habitat_wall_e", fd, 0.5f);
        this.wall_e_fix = this.body.getFixtureList().get(2);
        this.world.bodyEditorLoader.attachFixture(this.body, "habitat_wall_w", fd, 0.5f);
        this.wall_w_fix = this.body.getFixtureList().get(3);
        this.world.bodyEditorLoader.attachFixture(this.body, "habitat_corner_tl", fd, 0.5f);
        this.corner_tl_fix = this.body.getFixtureList().get(4);
        this.world.bodyEditorLoader.attachFixture(this.body, "habitat_corner_tr", fd, 0.5f);
        this.corner_tr_fix = this.body.getFixtureList().get(5);
        this.world.bodyEditorLoader.attachFixture(this.body, "habitat_corner_bl", fd, 0.5f);
        this.corner_bl_fix = this.body.getFixtureList().get(6);
        this.world.bodyEditorLoader.attachFixture(this.body, "habitat_corner_br", fd, 0.5f);
        this.corner_br_fix = this.body.getFixtureList().get(7);
    }

    @Override
    public void update(float delta) {
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        super.update(delta);
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("habitat-floor");
        this.shadow = new Image(this.world.gameScreen.skin.getDrawable("habitat-shadow"));
        this.shadow.setSize(TILE_SIZE + SCALE * 110.0f, TILE_SIZE + SCALE * 110.0f);
        this.shadow.setPosition(this.getXCenter(), this.getYCenter(), 1);
        this.world.gameScreen.floorGroup.addActor(this.shadow);
        this.shadow.toBack();
    }

    private boolean checkDirectionForAirlock(int wX, int wY, BaseModule.ORIENTATIONS aOrientation) {
        if (this.world.getTile(wX, wY) instanceof Airlock) {
            Airlock a = (Airlock)this.world.getTile(wX, wY);
            return a.getOuterDoorDirection() == aOrientation;
        }
        return true;
    }

    public void updateWalls() {
        this.blocksEast = true;
        this.blocksNorth = true;
        this.blocksSouth = true;
        this.blocksWest = true;
        boolean northTile = this.isNeighborBase(this.worldX, this.worldY + 1);
        boolean southTile = this.isNeighborBase(this.worldX, this.worldY - 1);
        boolean eastTile = this.isNeighborBase(this.worldX + 1, this.worldY);
        boolean westTile = this.isNeighborBase(this.worldX - 1, this.worldY);
        boolean northEastTile = this.isNeighborBase(this.worldX + 1, this.worldY + 1);
        boolean southEastTile = this.isNeighborBase(this.worldX + 1, this.worldY - 1);
        boolean northWestTile = this.isNeighborBase(this.worldX - 1, this.worldY + 1);
        boolean southWestTile = this.isNeighborBase(this.worldX - 1, this.worldY - 1);
        if (northTile) {
            northTile = this.checkDirectionForAirlock(this.worldX, this.worldY + 1, BaseModule.ORIENTATIONS.north);
        }
        if (southTile) {
            southTile = this.checkDirectionForAirlock(this.worldX, this.worldY - 1, BaseModule.ORIENTATIONS.south);
        }
        if (eastTile) {
            eastTile = this.checkDirectionForAirlock(this.worldX + 1, this.worldY, BaseModule.ORIENTATIONS.east);
        }
        if (westTile) {
            westTile = this.checkDirectionForAirlock(this.worldX - 1, this.worldY, BaseModule.ORIENTATIONS.west);
        }
        this.blocksNorth = !northTile;
        this.blocksSouth = !southTile;
        this.blocksEast = !eastTile;
        this.blocksWest = !westTile;
        this.wall_n.setVisible(!northTile);
        this.connector_n.setVisible(northTile);
        this.wall_n_fix.setSensor(northTile);
        this.wall_s.setVisible(!southTile);
        this.connector_s.setVisible(southTile);
        this.wall_s_fix.setSensor(southTile);
        this.wall_e.setVisible(!eastTile);
        this.connector_e.setVisible(eastTile);
        this.wall_e_fix.setSensor(eastTile);
        this.wall_w.setVisible(!westTile);
        this.connector_w.setVisible(westTile);
        this.wall_w_fix.setSensor(westTile);
        this.updateCorner(this.corner_bl, this.corner_floor_bl, this.corner_bl_fix, southTile, southWestTile, westTile);
        this.updateCorner(this.corner_br, this.corner_floor_br, this.corner_br_fix, southTile, southEastTile, eastTile);
        this.updateCorner(this.corner_tl, this.corner_floor_tl, this.corner_tl_fix, northTile, northWestTile, westTile);
        this.updateCorner(this.corner_tr, this.corner_floor_tr, this.corner_tr_fix, northTile, northEastTile, eastTile);
        Filter fn = new Filter();
        fn.categoryBits = !northTile ? (short)2 : (short)4;
        this.wall_n_fix.setFilterData(fn);
        Filter fw = new Filter();
        fw.categoryBits = !westTile ? (short)2 : (short)4;
        this.wall_w_fix.setFilterData(fw);
        Filter fe = new Filter();
        fe.categoryBits = !eastTile ? (short)2 : (short)4;
        this.wall_e_fix.setFilterData(fe);
        Filter fs = new Filter();
        fs.categoryBits = !southTile ? (short)2 : (short)4;
        this.wall_s_fix.setFilterData(fs);
    }

    private void updateCorner(Image wallCorner, Image floorCorner, Fixture fixture, boolean side1, boolean side2, boolean side3) {
        Filter filter = new Filter();
        if (side1 && side2 && side3) {
            wallCorner.setVisible(false);
            floorCorner.setVisible(true);
            fixture.setSensor(true);
            filter.categoryBits = (short)4;
        } else {
            wallCorner.setVisible(true);
            floorCorner.setVisible(false);
            fixture.setSensor(false);
            filter.categoryBits = (short)2;
        }
        fixture.setFilterData(filter);
    }

    private boolean isNeighborBase(int x, int y) {
        Tile t = this.world.getTile(x, y);
        return t != null && t.type == Tile.types.habitat;
    }

    @Override
    public void destroy() {
        Gdx.app.debug("MoonBase", "HabitatModule: Destroying " + this.builderId);
        if (this.shadow != null) {
            this.shadow.remove();
        }
        this.wallsGroup.remove();
        this.componentGroup.remove();
        super.destroy();
        this.updateBases();
    }

    @Override
    public void triggerDisaster() {
        if (!this.hasDisaster() && !NpcBonuses.getInstance().hasBonus(NpcBonuses.bonusTypes.mechanic)) {
            this.baseDisaster = new AirLeak(this);
            if (this.world.player.playerStatus.baseGroupsInRange.contains(this.getBaseGroup())) {
                HudNotificationData msg = new HudNotificationData();
                msg.icon = "base-warning-icon";
                msg.message = Localization.get("disaster_loosingOxygen");
                msg.textColor = Vars.WARNING_RED;
                MessageManager.getInstance().dispatchMessage(3, msg);
            } else {
                MoonBase.debug("Base too far to notify about airleak");
            }
        }
    }

    @Override
    public void interact(Player player) {
        MoonBase.debug("Habitat disaster timer: min " + this.baseDisasterTimer.minDelay + " - max " + this.baseDisasterTimer.maxDelay + " : current " + this.baseDisasterTimer.currentDelay + " : timer " + this.baseDisasterTimer.timer);
        if (this.hasDisaster()) {
            this.fixDisaster();
        } else if (player.playerInventory.getEquippedItemId().equals("paintbrush")) {
            this.changeColor(player.getPaintColorIndex());
        } else {
            super.interact(player);
        }
    }

    @Override
    public void fixDisaster() {
        super.fixDisaster();
        float volume = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 1200.0f, 1.0f);
        if (volume > 0.0f) {
            Audio.getInstance().playSound("sounds/repair.ogg", volume);
        }
        this.getBehavior(BaseDisasterTimer.class).start();
    }

    public void changeColor(int colorIndex) {
        this.intColorBehavior.value = colorIndex;
        if (this.intColorBehavior.value >= Vars.habColors.length || this.intColorBehavior.value < 0) {
            MoonBase.error("Invalid hab color index (max = " + (Vars.habColors.length - 1) + ")");
            this.intColorBehavior.value = 0;
        }
        this.paintbrushParticleFx();
        this.updateHabColor();
    }

    public void paintbrushParticleFx() {
        Audio.getInstance().playSound("sounds/paintbrush-splat.ogg", 0.5f, 1.2f);
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/paintbrush-splat.p", ParticleEffect.class));
        ParticleActor paintPuff = new ParticleActor(p, true);
        paintPuff.setPosition(this.getXCenter(), this.getYCenter());
        this.world.gameScreen.topGroup.addActor(paintPuff);
        for (ParticleEmitter e : paintPuff.pfx.getEmitters()) {
            ParticleEmitter.GradientColorValue gradient = e.getTint();
            Color newColor = Vars.habColors[this.intColorBehavior.value];
            gradient.getColors()[0] = newColor.r;
            gradient.getColors()[1] = newColor.g;
            gradient.getColors()[2] = newColor.b;
        }
        paintPuff.pfx.start();
    }

    @Override
    public Color getPoweredColor() {
        Color c = Vars.habColors[this.intColorBehavior.value];
        float tintStrength = 0.45f;
        float r = MathUtils.lerp(c.r, 1.0f, tintStrength);
        float g = MathUtils.lerp(c.g, 1.0f, tintStrength);
        float b = MathUtils.lerp(c.b, 1.0f, tintStrength);
        return new Color(r, g, b, 1.0f);
    }

    public void updateHabColor() {
        float tintStrength = 0.45f;
        Color c = Vars.habColors[this.intColorBehavior.value];
        float r = MathUtils.lerp(c.r, 1.0f, tintStrength);
        float g = MathUtils.lerp(c.g, 1.0f, tintStrength);
        float b = MathUtils.lerp(c.b, 1.0f, tintStrength);
        if (!this.hasPower) {
            r *= HabitatModule.POWER_OFF_COLOR.r;
            g *= HabitatModule.POWER_OFF_COLOR.g;
            b *= HabitatModule.POWER_OFF_COLOR.b;
        }
        this.image.setColor(new Color(r, g, b, 1.0f));
    }

    @Override
    public Color getPoweredOffColor() {
        float tintStrength = 0.45f;
        Color c = Vars.habColors[this.intColorBehavior.value];
        float r = MathUtils.lerp(c.r, 1.0f, tintStrength) * HabitatModule.POWER_OFF_COLOR.r;
        float g = MathUtils.lerp(c.g, 1.0f, tintStrength) * HabitatModule.POWER_OFF_COLOR.g;
        float b = MathUtils.lerp(c.b, 1.0f, tintStrength) * HabitatModule.POWER_OFF_COLOR.b;
        return new Color(r, g, b, 1.0f);
    }
}

