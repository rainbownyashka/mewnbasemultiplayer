/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.utils.BehaviorTreeParser;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.SpineActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.BtreeBlob;
import com.cairn4.moonbase.entities.Creature;
import com.cairn4.moonbase.entities.CreatureState;
import com.cairn4.moonbase.entities.PathFinding;
import com.cairn4.moonbase.entities.SteerableEntity;
import com.cairn4.moonbase.entities.TileNode;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.GameScreen;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Event;
import java.io.Reader;
import java.util.HashMap;

@Deprecated
public class Creature2
extends SteerableEntity
implements Telegraph,
Steerable<Vector2> {
    public String mobDefId;
    public float health;
    public float maxHealth;
    SpineActor spineActor;
    PathFinding pathFinding;
    private Vector2 currentPos = new Vector2(0.0f, 0.0f);
    private Vector2 targetPos = new Vector2(0.0f, 0.0f);
    private Vector2 velocity;
    private float walkSpeedMultiplier = 1.0f;
    private float searchTimer = 0.0f;
    private float searchTimerDelay = 3.0f;
    protected float idleSearchDelay = 1.0f;
    protected float fastSearchDelay = 0.5f;
    private float distanceToAttack = 60.0f;
    public Label stateDebugLabel;
    public StateMachine<Creature, CreatureState> stateMachine;
    private BehaviorTree<BtreeBlob> behaviorTree;
    public Player targetPlayer;
    public BaseModule targetBase;
    public float attackCooldownTimer = 0.0f;
    private int spawnXTile;
    private int spawnYTile;
    Array<TileNode> pathNodes = new Array();
    Polygon meleeAttackArea;
    private Vector2 moveVector;
    private float despawnCheckTimer = 0.0f;
    private final float despawnCheckDelay = 0.5f;

    public Creature2(World world, float xPos, float yPos, float rotation) {
        this(world, xPos, yPos);
    }

    public Creature2(World world, float xPos, float yPos) {
        super(world, xPos, yPos);
        this.group = new Group();
        this.group.setPosition(this.spawnPosX, this.spawnPosY);
    }

    @Override
    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("health", Float.valueOf(this.health));
        properties.put("mobDefId", this.mobDefId);
        properties.put("spawnXTile", this.spawnXTile);
        properties.put("spawnYTile", this.spawnYTile);
        return properties;
    }

    @Override
    public void loadProperties(HashMap<String, Object> p) {
        super.loadProperties(p);
    }

    public void init() {
        this.health = this.maxHealth = 100.0f;
        MessageManager.getInstance().addListener(this, 1);
        this.velocity = new Vector2(0.0f, 0.0f);
        this.createDrawables2();
        this.setupPhysics();
        this.stateDebugLabel = new Label((CharSequence)"", this.world.gameScreen.labelStyle);
        this.stateDebugLabel.setFontScale(0.4f);
        this.stateDebugLabel.setPosition(0.0f, 100.0f);
        this.group.addActor(this.stateDebugLabel);
        this.stateDebugLabel.setVisible(MoonBase.DEBUG_INFO);
        this.pathFinding = new PathFinding(this.world);
        Reader reader = null;
        reader = Gdx.files.local(MoonBase.coreFolder + "creature.tree").reader();
        BehaviorTreeParser<Object> parser = new BehaviorTreeParser<Object>(1);
        this.behaviorTree = parser.parse(reader, null);
        this.spineActor.stateData.setMix("idle", "walk", 0.1f);
        this.spineActor.stateData.setMix("walk", "idle", 0.1f);
        this.spineActor.stateData.setMix("idle", "preStrike", 0.1f);
        this.spineActor.stateData.setMix("attack", "idle", 0.1f);
        this.spineActor.stateData.setMix("takeHit", "idle", 0.1f);
        this.setupAnimEventListeners();
        this.updateSteererSpeedAccel(0.0f, 0.0f);
        this.spawnXTile = this.getWorldXTile();
        this.spawnYTile = this.getWorldYTile();
        Rectangle meleeRect = new Rectangle(0.0f, 0.0f, 100.0f, 100.0f);
        this.meleeAttackArea = new Polygon(new float[]{0.0f, -meleeRect.height / 2.0f, 0.0f, meleeRect.height / 2.0f, meleeRect.width, meleeRect.height / 2.0f, meleeRect.width, -meleeRect.height / 2.0f});
        this.meleeAttackArea.setOrigin(0.0f, 0.0f);
    }

    @Override
    protected void setupPhysics() {
        this.categoryBits = (short)8;
        this.maskBits = (short)2;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        CircleShape shape = new CircleShape();
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = this.categoryBits;
        fdef.filter.maskBits = (short)(this.maskBits + this.categoryBits);
        fdef.density = 1.0f;
        fdef.friction = 1.0f;
        fdef.restitution = 1.0f;
        Vector2 pos = new Vector2(((float)this.getXTile() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f) / 256.0f, ((float)this.getYTile() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f) / 256.0f);
        this.body = this.world.b2dWorld.createBody(bodyDef);
        this.body.setTransform(new Vector2(pos.x, pos.y), 0.0f);
        this.body.setFixedRotation(true);
        this.body.setUserData(this);
        this.body.createFixture(fdef);
    }

    public void updateSteererSpeedAccel(float maxLinearSpeed, float maxLinearAccel) {
        this.setMaxLinearSpeed(maxLinearSpeed);
        this.setMaxLinearAcceleration(maxLinearAccel);
    }

    public void renderPathfinding() {
        GameScreen cfr_ignored_0 = this.world.gameScreen;
        GameScreen.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        GameScreen cfr_ignored_1 = this.world.gameScreen;
        GameScreen.shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 0.5f);
        GameScreen cfr_ignored_2 = this.world.gameScreen;
        GameScreen.shapeRenderer.polygon(this.meleeAttackArea.getTransformedVertices());
        GameScreen cfr_ignored_3 = this.world.gameScreen;
        GameScreen.shapeRenderer.end();
        if (this.pathFinding != null) {
            this.pathFinding.render();
            if (this.pathFinding.smoothablePath != null && this.pathFinding.smoothablePath.nodes.size >= 1) {
                TileNode next = (TileNode)this.pathFinding.smoothablePath.nodes.get(0);
                GameScreen cfr_ignored_4 = this.world.gameScreen;
                GameScreen.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                GameScreen cfr_ignored_5 = this.world.gameScreen;
                GameScreen.shapeRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
                GameScreen cfr_ignored_6 = this.world.gameScreen;
                GameScreen.shapeRenderer.rectLine(this.getXPos(), this.getYPos(), (float)next.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)next.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, 4.0f);
                GameScreen cfr_ignored_7 = this.world.gameScreen;
                GameScreen.shapeRenderer.end();
            }
        }
    }

    public void setPlayerAsTarget(Player player) {
        Location<Vector2> playerLoc = player.getLocation();
        Seek<Vector2> seekBehavior = new Seek<Vector2>(this, playerLoc);
        this.setSteeringBehavior(seekBehavior);
    }

    @Override
    public float getXPos() {
        if (this.body != null) {
            return this.body.getPosition().x * 256.0f;
        }
        return this.group.getX();
    }

    @Override
    public float getYPos() {
        if (this.body != null) {
            return this.body.getPosition().y * 256.0f;
        }
        return this.group.getY();
    }

    protected void createDrawables2() {
    }

    public void setupAnimEventListeners() {
        this.spineActor.state.addListener(new AnimationState.AnimationStateAdapter(){

            public void event(int trackerIndex, Event event) {
                float pitch;
                if (event.getData().getName().equals("takeHitSound")) {
                    pitch = MathUtils.random(0.95f, 1.05f);
                    Audio.getInstance().playSound("sounds/interact_hit2.ogg", 0.7f, pitch);
                }
                if (event.getData().getName().equals("attackSound")) {
                    pitch = MathUtils.random(0.95f, 1.05f);
                    Audio.getInstance().playSound("sounds/interact_hit2.ogg", 0.7f, pitch);
                }
            }

            public void complete(int trackIndex, int loopCount) {
                if (Creature2.this.stateMachine.isInState(CreatureState.ATTACK)) {
                    // empty if block
                }
            }
        });
    }

    protected void createDrawables() {
        this.image = new Image(this.world.gameScreen.skin.getDrawable("zombie1"));
        this.image.setSize(100.0f, 100.0f);
        this.image.setOrigin(4);
        this.image.setPosition(0.0f, 0.0f, 1);
        this.group.addActor(this.image);
        this.image.setColor(MathUtils.random(0.8f, 1.0f), MathUtils.random(0.8f, 1.0f), MathUtils.random(0.8f, 1.0f), 1.0f);
        this.world.gameScreen.mainGroup.addActor(this.group);
        this.image.setTouchable(Touchable.enabled);
        this.image.addListener(new ClickListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Creature2.this.image.addAction(Actions.sequence((Action)Actions.color(Color.RED), (Action)Actions.color(Color.WHITE, 0.1f)));
                Creature2.this.setClicked();
                return true;
            }
        });
        float bounceDelay = MathUtils.random(0.04f, 0.07f);
        this.image.addAction(Actions.sequence((Action)Actions.delay(MathUtils.random(0.0f, 0.75f)), (Action)Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.1f, 0.8f, 0.1f), (Action)Actions.parallel((Action)Actions.scaleTo(0.8f, 1.1f, 0.05f), (Action)Actions.moveBy(0.0f, 25.0f, 0.3f, Interpolation.sineOut)), (Action)Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, 0.2f), (Action)Actions.moveBy(0.0f, -25.0f, 0.3f, Interpolation.sineIn)), (Action)Actions.delay(bounceDelay)))));
    }

    @Override
    public void remove() {
        this.group.remove();
    }

    @Override
    public void playerAction(Player player) {
        if (this.stateMachine.getCurrentState() != CreatureState.DEAD) {
            String itemId = player.getPlayerInventory().getEquippedItemId();
            if (itemId.equals("shovel")) {
                this.spineActor.state.setAnimation(1, "takeHit", false);
                this.takeDamage(5.0f);
            } else if (itemId.equals("axe")) {
                this.spineActor.state.setAnimation(1, "takeHit", false);
                this.takeDamage(5.0f);
            }
        }
    }

    public void takeDamage(float amount) {
        this.health -= amount;
        if (this.health <= 0.0f) {
            this.die();
        }
    }

    private void die() {
        this.stateMachine.changeState(CreatureState.DEAD);
        float delay = 2.0f;
        Timer.schedule(new Timer.Task(){

            @Override
            public void run() {
                Creature2.this.dieFinished();
            }
        }, delay);
        Gdx.app.log("MewnBase", "Enemy dead!");
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/bloodsplat.p", ParticleEffect.class));
        ParticleActor hitFx = new ParticleActor(p, true);
        hitFx.setPosition(this.getXPos(), this.getYPos());
        hitFx.pfx.start();
        this.world.gameScreen.topGroup.addActor(hitFx);
    }

    public void dieFinished() {
        this.readyToRemove = true;
    }

    @Override
    public void update(float delta) {
        this.group.setPosition(this.getXPos() - 50.0f, this.getYPos() - 40.0f);
        this.group.setUserObject(Float.valueOf(this.getYPos()));
        if (this.stateMachine != null) {
            this.stateMachine.update();
            this.spineActor.update(delta);
            if (this.attackCooldownTimer > 0.0f) {
                this.attackCooldownTimer -= delta;
            }
        }
    }

    protected void applySteering(SteeringAcceleration<Vector2> steering2, float deltaTime) {
        Vector2 linVel;
        boolean anyAccelerations = false;
        if (!((Vector2)Creature2.steeringOutput.linear).isZero()) {
            this.body.applyForceToCenter((Vector2)Creature2.steeringOutput.linear, true);
            anyAccelerations = true;
        }
        if (!(linVel = this.getLinearVelocity()).isZero(this.getZeroLinearSpeedThreshold())) {
            float newOrientation = this.vectorToAngle(linVel);
            this.body.setAngularVelocity((newOrientation - this.getAngularVelocity()) * deltaTime);
            this.body.setTransform(this.body.getPosition(), newOrientation);
        }
        if (anyAccelerations) {
            float maxLinearSpeed;
            Vector2 velocity = this.body.getLinearVelocity();
            float currentSpeedSquare = velocity.len2();
            if (currentSpeedSquare > (maxLinearSpeed = this.getMaxLinearSpeed()) * maxLinearSpeed) {
                this.body.setLinearVelocity(velocity.scl(maxLinearSpeed / (float)Math.sqrt(currentSpeedSquare)));
            }
            float maxAngVelocity = this.getMaxAngularSpeed();
            if (this.body.getAngularVelocity() > maxAngVelocity) {
                this.body.setAngularVelocity(maxAngVelocity);
            }
        }
    }

    public float angleToTarget(Vector2 targetPos) {
        this.currentPos.set(this.getXPos(), this.getYPos());
        return this.targetPos.sub(this.currentPos).angle();
    }

    public void aimAttackZone() {
        this.meleeAttackArea.setPosition(this.getXPos(), this.getYPos());
        this.targetPos.set(this.targetPlayer.getXPos(), this.targetPlayer.getYPos());
        float a = this.angleToTarget(this.targetPos);
        this.meleeAttackArea.setRotation(a);
    }

    public float getDistanceToTarget() {
        this.currentPos.set(this.getXTile(), this.getYTile());
        return this.currentPos.dst(this.targetPlayer.getX(), this.targetPlayer.getY());
    }

    public void idleMove() {
        this.move(Vector2.Zero);
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
                float f = this.targetPos.cpy().sub(this.currentPos).len();
            }
        }
        return false;
    }

    public boolean targetFarEnoughToIgnore() {
        if (this.targetPlayer != null) {
            this.targetPos.set(this.world.player.getXPos(), this.world.player.getYPos());
        }
        if (this.targetBase != null) {
            this.targetPos.set(this.targetBase.getXCenter(), this.targetBase.getYCenter());
        }
        Vector2 currentPos = new Vector2(this.getXPos(), this.getYPos());
        Vector2 diff = this.targetPos.cpy().sub(currentPos);
        return diff.len() > 400.0f;
    }

    public boolean readyToAttack() {
        return this.attackCooldownTimer <= 0.0f;
    }

    public boolean inAttackZone() {
        if (this.targetPlayer != null) {
            if (this.targetPos == null) {
                this.targetPos = new Vector2(0.0f, 0.0f);
            }
            this.targetPos.set(this.world.player.getXPos(), this.world.player.getYPos());
        }
        return this.meleeAttackArea.contains(this.targetPos);
    }

    public void attackTarget() {
        this.attackCooldownTimer = 1.0f;
        if (this.targetPlayer != null) {
            this.targetPos.set(this.world.player.getXPos(), this.world.player.getYPos());
            if (this.meleeAttackArea.contains(this.targetPos)) {
                float maxDist;
                float damageMul = 1.0f;
                float dist = this.getDistanceToTarget();
                if (dist > (maxDist = 200.0f) / 2.0f) {
                    damageMul = 1.0f - dist / (maxDist / 2.0f) + 0.5f;
                }
                System.out.println("damage: " + 10.0f * damageMul);
                this.targetPlayer.playerStatus.changeHealth(-(10.0f * damageMul));
            }
        }
        if (this.targetBase != null) {
            // empty if block
        }
    }

    public void pursueTarget() {
        if (this.targetPlayer != null) {
            this.targetPos.set(this.world.player.getXPos(), this.world.player.getYPos());
        }
        if (this.targetBase != null) {
            this.targetPos.set(this.targetBase.getXCenter(), this.targetBase.getYCenter());
        }
        Vector2 currentPos = new Vector2(this.getXPos(), this.getYPos());
        Vector2 diff = this.targetPos.cpy().sub(currentPos);
        float angleToTarget = diff.angle();
        Vector2 v = new Vector2(1.0f, 0.0f);
        v.setAngle(angleToTarget);
        this.move(v);
    }

    public void setSearchTimerDelay(float n) {
        this.searchTimerDelay = n;
    }

    public void clearPath() {
        this.pathFinding.smoothablePath.clear();
    }

    public void pathToRandomTile() {
        boolean walkable = false;
        GridPoint2 destinationPos = new GridPoint2(0, 0);
        for (int tries = 0; !walkable && tries < 10; ++tries) {
            float distFromSpawn = destinationPos.dst(this.spawnXTile, this.spawnYTile) * Tile.TILE_SIZE;
        }
        if (walkable) {
            GridPoint2 selfPos = new GridPoint2(this.getWorldXTile(), this.getWorldYTile());
            GraphPath<TileNode> path = this.pathFinding.getPath(selfPos, destinationPos, 10);
            this.trimPatrolPath();
        } else {
            Gdx.app.log("MewnBase", "Enemy: failed to find somewhere to patrol to, back to idle");
        }
    }

    public void pathToPlayer(float delta) {
        if (this.world.player != null) {
            GridPoint2 playerPos = new GridPoint2(this.world.player.getX(), this.world.player.getY());
            GridPoint2 enemyPos = new GridPoint2(this.getWorldXTile(), this.getWorldYTile());
            Vector2 diff = new Vector2(playerPos.x, playerPos.y).sub(enemyPos.x, enemyPos.y);
            if (diff.len() < 6.0f) {
                this.targetPlayer = this.world.player;
                GraphPath<TileNode> path = this.pathFinding.getPath(enemyPos, playerPos, 6);
                if (path != null) {
                    System.out.println("path: " + path.getCount());
                }
            }
        }
    }

    public void followPatrolPath() {
        if (this.moveVector == null) {
            this.moveVector = new Vector2(0.0f, 0.0f);
        }
        if (this.pathFinding.smoothablePath != null) {
            this.pathNodes = this.pathFinding.smoothablePath.nodes;
            if (this.pathNodes.size > 0) {
                if (this.targetPos == null) {
                    this.targetPos = new Vector2(0.0f, 0.0f);
                }
                this.targetPos.set((float)this.pathNodes.get((int)0).x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)this.pathNodes.get((int)0).y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
                this.currentPos = new Vector2(this.getXPos(), this.getYPos());
                this.moveVector.set(this.targetPos.cpy().sub(this.currentPos));
                float distToTarget = this.moveVector.len();
                float angleToTarget = this.moveVector.angle();
                if (distToTarget < 30.0f) {
                    this.pathNodes.removeIndex(0);
                    Gdx.app.debug("MewnBase", "Enemy: Reached node");
                } else {
                    this.moveVector.set(1.0f, 0.0f);
                    this.moveVector.setAngle(angleToTarget);
                    this.move(this.moveVector);
                }
            } else {
                this.stateMachine.changeState(CreatureState.IDLE);
            }
        } else {
            this.stateMachine.changeState(CreatureState.IDLE);
        }
    }

    private void trimPatrolPath() {
        if (this.pathFinding.smoothablePath != null) {
            int n = this.pathFinding.smoothablePath.nodes.size;
        }
    }

    public void followPath() {
        if (this.pathFinding.smoothablePath != null) {
            Array nodes = this.pathFinding.smoothablePath.nodes;
            if (nodes.size > 0) {
                if (nodes.size == 1) {
                    this.targetPos = new Vector2(this.targetPlayer.getXPos(), this.targetPlayer.getYPos());
                    this.currentPos.set(this.getXPos(), this.getYPos());
                    Vector2 diff = this.targetPos.cpy().sub(this.currentPos);
                    if (diff.len() < 10.0f) {
                        nodes.removeIndex(0);
                        System.out.println("Reached player pos");
                    } else {
                        float angleToTarget = diff.angle();
                        Vector2 v = new Vector2(1.0f, 0.0f);
                        v.setAngle(angleToTarget);
                        this.move(v);
                    }
                } else {
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
                this.stateMachine.changeState(CreatureState.IDLE);
            }
        }
    }

    private void move(Vector2 v) {
        if (v.x == 0.0f && v.y == 0.0f) {
            this.velocity.set(0.0f, 0.0f);
        }
        if (v.x > 0.0f) {
            this.spineActor.setFlip(true, false);
        } else if (v.x < 0.0f) {
            this.spineActor.setFlip(false, false);
        }
        float delta = Gdx.graphics.getDeltaTime();
        this.body.setLinearVelocity(this.velocity.x / 256.0f, this.velocity.y / 256.0f);
    }

    public void despawn() {
        this.readyToRemove = true;
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        boolean debug = (Boolean)msg.extraInfo;
        this.stateDebugLabel.setVisible(debug);
        return false;
    }
}

