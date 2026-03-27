/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.BaseGroup;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.SpineActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.dialog.NpcData;
import com.cairn4.moonbase.dialog.Quest;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.entities.LootDropper;
import com.cairn4.moonbase.entities.NpcBonuses;
import com.cairn4.moonbase.entities.NpcDef;
import com.cairn4.moonbase.entities.NpcFriendState;
import com.cairn4.moonbase.entities.NpcManager;
import com.cairn4.moonbase.entities.NpcNeeds;
import com.cairn4.moonbase.entities.NpcState;
import com.cairn4.moonbase.entities.PathFinding;
import com.cairn4.moonbase.entities.PathFindingAction;
import com.cairn4.moonbase.entities.Speaker;
import com.cairn4.moonbase.entities.SteerableEntity;
import com.cairn4.moonbase.entities.TileNode;
import com.cairn4.moonbase.tiles.Airlock;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.NpcHome;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.NpcStatusIcon;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import java.util.ArrayList;
import java.util.HashMap;

public class Npc
extends SteerableEntity
implements Speaker,
Steerable<Vector2>,
LootDropper {
    public String npcId;
    public NpcDef npcDef;
    public GridPoint2 homeLocation;
    public NpcHome home = null;
    private Image shadow;
    private Image image;
    private PointLight light;
    public StateMachine<Npc, NpcState> stateMachine;
    public NpcState savedStateMachineState;
    public StateMachine<Npc, NpcFriendState> friendStateMachine;
    public NpcFriendState savedFriendStateMachineState;
    public NpcData npcData;
    public Quest currentQuest;
    public boolean discovered = false;
    public boolean bonusEarned = false;
    public boolean bonusActive = false;
    public SpineActor spineActor;
    public Player targetPlayer;
    public PathFinding pathFinding;
    public ArrayList<PathFindingAction> pathFindingQueue = new ArrayList();
    private Vector2 currentPos = new Vector2(0.0f, 0.0f);
    private Vector2 targetPos = new Vector2(0.0f, 0.0f);
    private Vector2 velocity;
    public NpcStatusIcon npcStatusIcon;
    public Label debugLabel;
    private float searchTimer = 0.0f;
    private float searchTimerDelay = 3.0f;
    protected float idleSearchDelay = 1.0f;
    protected float fastSearchDelay = 0.5f;
    private float walkSpeedMultiplier = 1.0f;
    public static final float maxFollowPlayerTileDistance = 10.0f;
    private float bTreeTimer = 0.0f;
    private float bTreeDelay = 0.25f;
    private Vector2 walkTarget;
    public static boolean clickToStepBT = false;
    String treeString = "";

    public Npc(World world, float xPos, float yPos, float rotation) {
        this(world, xPos, yPos);
    }

    public Npc(World world, float xPos, float yPos) {
        super(world, xPos, yPos);
        NpcManager.getInstance().activeNpcList.add(this);
    }

    public void init() {
        this.npcDef = NpcManager.getInstance().getNpcDef(this.npcId);
        this.createDrawable(this.npcDef.spine);
        this.npcData = new NpcData();
        this.npcData.idleMinTime = 1.0f;
        this.npcData.idleMaxTime = 2.0f;
        this.npcData.patrolAccel = 700.0f;
        this.npcData.maxPatrolSpeed = 70.0f;
        this.npcData.chaseAccel = 900.0f;
        this.npcData.maxChaseSpeed = 102.0f;
        this.npcData.targetSearchTileDist = 4.0f;
        this.velocity = new Vector2(0.0f, 0.0f);
        this.stateMachine = new DefaultStateMachine<Npc, NpcState>(this, NpcState.PRESPAWN);
        this.friendStateMachine = new DefaultStateMachine<Npc, NpcFriendState>(this, NpcFriendState.NOT_FRIEND);
        this.setupPhysics();
        this.light = new PointLight(this.world.rayHandler, 100, new Color(1.0f, 0.9f, 0.8f, 0.55f), 1.171875f, 0.0f, -0.078125f);
        this.light.setSoft(true);
        this.light.setXray(true);
        this.pathFinding = new PathFinding(this.world);
    }

    private void createDrawable(String spineFile) {
        this.group = new Group();
        this.group.setSize(60.0f, 90.0f);
        this.group.setDebug(false);
        this.group.setUserObject(Float.valueOf(this.getYPos()));
        this.group.setPosition(this.spawnPosX, this.spawnPosY, 4);
        this.world.gameScreen.mainGroup.addActor(this.group);
        this.group.addListener(new ClickListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Npc.this.setClicked();
                return true;
            }
        });
        this.setupSpineActor();
        this.npcStatusIcon = new NpcStatusIcon(this.world.gameScreen.skin, this.world.gameScreen.worldUIStage);
        this.debugLabel = new Label((CharSequence)"", this.world.gameScreen.labelStyle);
        this.debugLabel.setFontScale(0.25f);
        this.debugLabel.setPosition(0.0f, 200.0f);
        this.group.addActor(this.debugLabel);
        this.debugLabel.setVisible(false);
        this.updateQuestIconPos();
    }

    private void updateQuestIconPos() {
        if (this.currentQuest != null) {
            // empty if block
        }
        this.npcStatusIcon.update(this.getXPos(), this.getYPos(), this.getHeight());
    }

    private void setupSpineActor() {
        this.spineActor = new SpineActor("mouse", 0.22f, this.world.gameScreen.skeletonRenderer);
        this.spineActor.setPosition(30.0f, 10.0f);
        this.group.addActor(this.spineActor);
        this.spineActor.stateData = new AnimationStateData(this.spineActor.skeletonData);
        this.spineActor.stateData.setMix("idle", "walk_right", 0.2f);
        this.spineActor.stateData.setMix("walk_right", "idle", 0.2f);
        this.spineActor.state = new AnimationState(this.spineActor.stateData);
        this.spineActor.skeleton.setSkin("mouse");
        this.spineActor.state.addAnimation(0, "idle", true, 0.0f);
        this.spineActor.state.addAnimation(3, "lookAround", true, 12.0f);
        this.spineActor.state.addAnimation(2, "blink", true, 5.5f);
    }

    @Override
    protected void setupPhysics() {
        this.categoryBits = (short)4;
        this.maskBits = (short)2;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        CircleShape shape = new CircleShape();
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = this.categoryBits;
        fdef.filter.maskBits = this.maskBits;
        fdef.density = 10.0f;
        fdef.friction = 0.1f;
        fdef.restitution = 0.1f;
        Vector2 pos = new Vector2(this.spawnPosX / 256.0f, this.spawnPosY / 256.0f);
        this.body = this.world.b2dWorld.createBody(bodyDef);
        this.body.setTransform(new Vector2(pos.x, pos.y), 0.0f);
        this.body.setFixedRotation(true);
        this.body.setLinearDamping(0.5f);
        this.body.setUserData(this);
        shape.setRadius(0.09375f);
        this.body.createFixture(fdef);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.stateMachine.getCurrentState() == NpcState.PRESPAWN) {
            this.stateMachine.changeState(NpcState.SPAWN);
        }
        if (this.stateMachine.getCurrentState() != NpcState.FINISHED) {
            this.stateMachine.update();
        } else {
            this.friendStateMachine.update();
        }
        this.light.setPosition(this.getXPos() / 256.0f, (this.getYPos() - 20.0f) / 256.0f);
        if (this.bonusEarned) {
            if (this.home != null) {
                this.bonusActive = true;
                NpcBonuses.getInstance().addBonus(this.npcDef.rewardBonus);
            } else {
                this.bonusActive = false;
                NpcBonuses.getInstance().removeBonus(this.npcDef.rewardBonus);
            }
        } else {
            this.bonusActive = false;
        }
        this.group.setPosition(this.getXPos() - 30.0f, this.getYPos() - 40.0f);
        this.group.setUserObject(Float.valueOf(this.getYPos()));
        this.updateQuestIconPos();
        this.spineActor.update(delta);
        if (this.pathFinding != null && this.pathFinding.smoothablePath != null && this.pathFinding.smoothablePath.nodes.size >= 1) {
            TileNode next = (TileNode)this.pathFinding.smoothablePath.nodes.get(0);
            GameScreen cfr_ignored_0 = this.world.gameScreen;
            GameScreen.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            GameScreen cfr_ignored_1 = this.world.gameScreen;
            GameScreen.shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
            GameScreen cfr_ignored_2 = this.world.gameScreen;
            GameScreen.shapeRenderer.rectLine(this.getXPos(), this.getYPos(), (float)next.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)next.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, 4.0f);
            GameScreen cfr_ignored_3 = this.world.gameScreen;
            GameScreen.shapeRenderer.end();
        }
    }

    @Override
    public void playerAction(Player player) {
        Gdx.app.debug("MewnBase", "Interacted with NPC");
        this.handleInteraction(player);
    }

    @Override
    public void playerActionSecondary(Player player) {
        if (this.stateMachine.getCurrentState() == NpcState.FINISHED) {
            this.friendStateMachine.getCurrentState().interactSecondary(this, player);
        }
    }

    public void handleInteraction(Player player) {
        if (this.stateMachine.getCurrentState() == NpcState.FINISHED) {
            this.friendStateMachine.getCurrentState().interact(this, player);
        } else {
            this.stateMachine.getCurrentState().interact(this, player);
        }
    }

    @Override
    public HashMap<String, Object> getProperties() {
        this.homeLocation = this.home != null ? new GridPoint2(this.home.worldX, this.home.worldY) : null;
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("npcId", this.npcId);
        properties.put("savedStateMachineState", this.stateMachine.getCurrentState());
        properties.put("savedFriendStateMachineState", this.friendStateMachine.getCurrentState());
        properties.put("discovered", this.discovered);
        properties.put("currentQuest", this.currentQuest);
        properties.put("homeLocation", this.homeLocation);
        properties.put("bonusEarned", this.bonusEarned);
        properties.put("bonusActive", this.bonusActive);
        return properties;
    }

    @Override
    public void loadProperties(HashMap<String, Object> p) {
        Tile t;
        super.loadProperties(p);
        if (this.homeLocation != null && (t = this.world.getTile(this.homeLocation.x, this.homeLocation.y)) != null && t instanceof NpcHome) {
            this.home = (NpcHome)t;
            this.home.assignNpc(this);
        }
        this.fetchData();
    }

    public void fetchData() {
        this.npcDef = NpcManager.getInstance().getNpcDef(this.npcId);
        if (this.npcDef != null) {
            this.init();
        } else {
            this.readyToRemove = true;
            Gdx.app.error("MewnBase", "Npc: error finding mobDef " + this.npcId + ", removing from world.");
        }
    }

    @Override
    public void doneLoading() {
        this.stateMachine.changeState(this.savedStateMachineState);
        this.friendStateMachine.changeState(this.savedFriendStateMachineState);
    }

    @Override
    public void setWorldPos(float wX, float wY) {
        this.body.setTransform(wX / 256.0f, wY / 256.0f, 0.0f);
    }

    @Override
    public float getXPos() {
        if (this.body != null) {
            return this.body.getPosition().x * 256.0f;
        }
        return this.spawnPosX;
    }

    @Override
    public float getYPos() {
        if (this.body != null) {
            return this.body.getPosition().y * 256.0f;
        }
        return this.spawnPosY;
    }

    @Override
    public float getHeight() {
        return 70.0f;
    }

    @Override
    public void dropLoot() {
        for (InventoryItemData rewardData : this.currentQuest.rewardItems) {
            for (int i = 0; i < rewardData.amount; ++i) {
                Item newItem = ItemFactory.getInstance().createItem(rewardData.itemId);
                new ItemPickup(this.world, this.getCurrentChunk(), this.getXPos(), this.getYPos() - 40.0f, newItem);
            }
        }
    }

    public void setQuestNotification(boolean b) {
        if (this.npcStatusIcon != null) {
            if (b) {
                this.npcStatusIcon.changeIcon(NpcNeeds.hasQuest);
            } else {
                this.npcStatusIcon.hide();
            }
        }
    }

    public void setSearchTimerDelay(float n) {
        this.searchTimerDelay = n;
    }

    public void pathToPlayer(float delta) {
        if (this.world.player != null) {
            GridPoint2 playerPos = World.gridPointPool.obtain();
            playerPos.set(this.world.player.getX(), this.world.player.getY());
            GridPoint2 npcPos = World.gridPointPool.obtain();
            npcPos.set(this.getWorldXTile(), this.getWorldYTile());
            this.pathFindingQueue.clear();
            PathFindingAction pfa = new PathFindingAction();
            pfa.setDestination(playerPos);
            this.pathFindingQueue.add(pfa);
            boolean canPath = false;
            Tile t = this.world.getTile(playerPos.x, playerPos.y);
            if (t instanceof HabitatModule) {
                canPath = this.pathToInsideBase((HabitatModule)t);
            }
            this.walkTarget = new Vector2((float)this.pathFindingQueue.get((int)0).destination.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)this.pathFindingQueue.get((int)0).destination.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
            GraphPath<TileNode> path = this.pathFinding.getPath(npcPos, this.pathFindingQueue.get((int)0).destination, 5);
            if (path != null) {
                System.out.println("path: " + path.getCount());
            }
            World.gridPointPool.free(playerPos);
            World.gridPointPool.free(npcPos);
        }
    }

    private boolean pathToInsideBase(HabitatModule h) {
        BaseGroup bg = h.getBaseGroup();
        float closestDist = 9999.0f;
        Airlock enterance = null;
        for (BaseModule b : bg.baseModuleList) {
            float dist;
            if (!(b instanceof Airlock) || !((dist = this.distanceToTile(b.worldX, b.worldY)) < closestDist)) continue;
            closestDist = dist;
            enterance = (Airlock)b;
        }
        if (enterance != null) {
            GridPoint2 enteranceOpening = new GridPoint2(enterance.worldX, enterance.worldY);
            switch (enterance.getOuterDoorDirection()) {
                case north: {
                    ++enteranceOpening.y;
                    break;
                }
                case east: {
                    ++enteranceOpening.x;
                    break;
                }
                case south: {
                    --enteranceOpening.y;
                    break;
                }
                case west: {
                    --enteranceOpening.x;
                }
            }
            PathFindingAction pfa = new PathFindingAction();
            pfa.setDestination(enteranceOpening);
            pfa.setInterActTile(enterance.worldX, enterance.worldY);
            this.pathFindingQueue.add(0, pfa);
            return true;
        }
        return false;
    }

    private void test() {
        ArrayList<GridPoint2> destinationList = new ArrayList<GridPoint2>();
        destinationList.add(0, new GridPoint2(0, 0));
    }

    public void pathToHome() {
        if (this.home != null) {
            GridPoint2 homePos = new GridPoint2(this.home.worldX, this.home.worldY);
            GridPoint2 enemyPos = new GridPoint2(this.getWorldXTile(), this.getWorldYTile());
            System.out.println("npc tile pos = " + enemyPos);
            this.walkTarget = new Vector2(this.home.getXCenter(), this.home.getYCenter());
            GraphPath<TileNode> path = this.pathFinding.getPath(enemyPos, homePos, 6);
            if (path != null) {
                System.out.println("path: " + path.getCount());
                this.friendStateMachine.changeState(NpcFriendState.PATHING_TO_HOUSE);
            } else {
                Gdx.app.error("MewnBase", "CANT PATH");
            }
        }
    }

    public boolean followPath() {
        float closeEnoughToPlayer = 60.0f;
        if (this.pathFinding.smoothablePath != null) {
            Array nodes = this.pathFinding.smoothablePath.nodes;
            if (nodes.size > 0) {
                if (nodes.size == 1) {
                    this.targetPos = new Vector2(this.walkTarget.x, this.walkTarget.y);
                    this.currentPos.set(this.getXPos(), this.getYPos());
                    Vector2 diff = this.targetPos.cpy().sub(this.currentPos);
                    if (diff.len() < closeEnoughToPlayer) {
                        nodes.removeIndex(0);
                        System.out.println("Reached player pos");
                        this.reachedEndOfPath();
                    } else {
                        float angleToTarget = diff.angle();
                        Vector2 v = new Vector2(1.0f, 0.0f);
                        v.setAngle(angleToTarget);
                        this.move(v);
                    }
                } else {
                    if (((TileNode)nodes.get((int)0)).x == this.getXTile() && ((TileNode)nodes.get((int)0)).y == this.getYTile()) {
                        nodes.removeIndex(0);
                    }
                    this.targetPos = new Vector2((float)((TileNode)nodes.get((int)0)).x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)((TileNode)nodes.get((int)0)).y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
                    Vector2 currentPos = new Vector2(this.getXPos(), this.getYPos());
                    Vector2 diff = this.targetPos.cpy().sub(currentPos);
                    if (diff.len() < 30.0f) {
                        nodes.removeIndex(0);
                        System.out.println("Reached node");
                    } else {
                        float angleToTarget = diff.angle();
                        Vector2 v = new Vector2(1.0f, 0.0f);
                        v.setAngle(angleToTarget);
                        this.move(v);
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private void reachedEndOfPath() {
        GridPoint2 npcPos = World.gridPointPool.obtain();
        npcPos.set(this.getWorldXTile(), this.getWorldYTile());
        if (this.pathFindingQueue.size() >= 2) {
            this.walkTarget = new Vector2((float)this.pathFindingQueue.get((int)1).destination.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)this.pathFindingQueue.get((int)1).destination.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
            GraphPath<TileNode> graphPath = this.pathFinding.getPath(npcPos, this.pathFindingQueue.get((int)1).destination, 10);
        }
        System.out.println("removing pathing queue item");
        this.pathFindingQueue.remove(0);
    }

    private void move(Vector2 v) {
        if (v.x == 0.0f && v.y == 0.0f) {
            this.velocity.set(0.0f, 0.0f);
        }
        if (v.x > 0.0f) {
            this.spineActor.setFlip(false, false);
        } else if (v.x < 0.0f) {
            this.spineActor.setFlip(true, false);
        }
        float delta = Gdx.graphics.getDeltaTime();
        float accel = this.getWalkAccel();
        this.velocity.add(v.x * accel * this.walkSpeedMultiplier * delta, v.y * accel * this.walkSpeedMultiplier * delta);
        this.velocity.limit(this.getMaxWalkSpeed() * this.walkSpeedMultiplier);
        this.body.setLinearVelocity(this.velocity.x / 256.0f, this.velocity.y / 256.0f);
    }

    public void idleMove() {
        this.move(Vector2.Zero);
    }

    public float getWalkAccel() {
        return this.npcData.patrolAccel;
    }

    public float getMaxWalkSpeed() {
        return this.npcData.maxPatrolSpeed;
    }

    public boolean aggroCheck() {
        if (this.targetPos == null) {
            this.targetPos = new Vector2(0.0f, 0.0f);
        }
        if (this.currentPos == null) {
            this.currentPos = new Vector2(0.0f, 0.0f);
        }
        this.searchTimer += GdxAI.getTimepiece().getDeltaTime();
        if (this.searchTimer > this.searchTimerDelay) {
            this.searchTimer = 0.0f;
            if (this.world.player != null) {
                this.targetPos.set(this.world.player.getXPos(), this.world.player.getYPos());
                this.currentPos.set(this.getXPos(), this.getYPos());
                float diff = this.targetPos.cpy().sub(this.currentPos).len();
                if (diff < this.npcData.targetSearchTileDist * Tile.TILE_SIZE) {
                    this.targetPlayer = this.world.player;
                    return true;
                }
            }
        }
        return false;
    }

    public void assignHome(NpcHome npcHome) {
        this.home = npcHome;
    }

    public void removeHome() {
        this.home = null;
    }

    public void interactWithTile() {
        this.spineActor.state.setAnimation(1, "interact", false);
    }

    public void renderPathFinding() {
        if (this.pathFinding != null) {
            this.pathFinding.render();
            if (this.pathFinding.smoothablePath != null && this.pathFinding.smoothablePath.nodes.size >= 1) {
                TileNode next = (TileNode)this.pathFinding.smoothablePath.nodes.get(0);
                GameScreen cfr_ignored_0 = this.world.gameScreen;
                GameScreen.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                GameScreen cfr_ignored_1 = this.world.gameScreen;
                GameScreen.shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
                GameScreen cfr_ignored_2 = this.world.gameScreen;
                GameScreen.shapeRenderer.rectLine(this.getXPos(), this.getYPos(), (float)next.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)next.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, 4.0f);
                GameScreen cfr_ignored_3 = this.world.gameScreen;
                GameScreen.shapeRenderer.end();
            }
        }
    }

    public void cancelPathing() {
        this.pathFinding.cancel();
        this.pathFindingQueue.clear();
        this.idleMove();
    }
}

