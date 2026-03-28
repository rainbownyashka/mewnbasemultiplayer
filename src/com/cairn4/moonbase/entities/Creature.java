/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.DamageTaker;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.ProjectileFactory;
import com.cairn4.moonbase.ShaderHelper;
import com.cairn4.moonbase.SpineActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.creatures.SpineActorDraw;
import com.cairn4.moonbase.entities.CreatureLoader;
import com.cairn4.moonbase.entities.CreatureState;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.ImpactPuff;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.entities.LightAnimator;
import com.cairn4.moonbase.entities.PathFinding;
import com.cairn4.moonbase.entities.PathFindingAction;
import com.cairn4.moonbase.entities.Projectile;
import com.cairn4.moonbase.entities.TileNode;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.worlddata.CreatureAttackData;
import com.cairn4.moonbase.worlddata.CreatureData;
import com.cairn4.moonbase.worlddata.DamageDef;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import com.cairn4.moonbase.worlddata.ItemDropperItemData;
import com.cairn4.moonbase.worlddata.SoundDef;
import com.cairn4.moonbase.worlddata.TileInteractionAction;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Event;
import java.util.ArrayList;
import java.util.HashMap;

public class Creature
extends Entity
implements DamageTaker {
    public String id;
    public CreatureData creatureData;
    public StateMachine<Creature, CreatureState> stateMachine;
    public Vector2 savePosition;
    public float health;
    public float maxHealth;
    protected PointLight light;
    protected LightAnimator lightAnimator;
    private boolean enclosed = false;
    SpineActor spineActor;
    public int spawnXTile;
    public int spawnYTile;
    public Label debugLabel;
    public PathFinding pathFinding;
    public ArrayList<PathFindingAction> pathFindingQueue = new ArrayList();
    private Vector2 walkTarget;
    private Vector2 currentPos;
    private Vector2 pathTargetPos;
    private Vector2 targetPos;
    private Vector2 velocity;
    private float walkSpeedMultiplier = 1.0f;
    private static float windSpeedSlowWalkThreshold = 0.9f;
    protected short categoryBits;
    protected short maskBits;
    protected Body body;
    private float idleTimer = 0.0f;
    private float idleTimerDelay = 0.0f;
    private static final float idleTimerMaxDelay = 5.0f;
    private float searchTimer = 0.0f;
    private float searchTimerDelay = 1.0f;
    private float repathTimer = 0.0f;
    private float repathTimerDelay = 0.5f;
    public float preAttackTimer;
    public final float attackCooldown = 1.0f;
    public float attackCooldownTimer = 0.0f;
    private float distanceToAttack = 60.0f;
    public Player targetPlayer;
    public BaseModule targetBase;
    private Image targetIndicator;
    public static float colorFlashLength = 0.08f;
    public float colorFlashTimer = 0.0f;
    private float attackRadiusEllipseHeight = 0.8f;
    private int footstep = 0;
    private static final float aggroTimerDelay = 1.0f;
    private float aggroTimer = 0.0f;
    private ArrayList<CreatureAttackData> usableAttacks = new ArrayList();
    public CreatureAttackData currentAttack;
    public boolean tooCloseToAttack = false;
    private float chaseAttackCheckTimer = 0.0f;
    private static final float chaseAttackCheckDelay = 0.25f;
    float lurchTimer = 0.0f;
    float lurchAlpha = 0.0f;
    Vector2 lurchStartPos;
    Vector2 lurchTargetPos;
    static final float SLEEP_LENGTH = 10.0f;
    float sleepTimer = 10.0f;

    public boolean isAlive() {
        return this.health > 0.0f;
    }

    public Creature(World world, float xPos, float yPos, float rotation) {
        super(world, xPos, yPos);
        this.group = new Group();
        this.group.setPosition(this.spawnPosX, this.spawnPosY);
        this.velocity = new Vector2(0.0f, 0.0f);
    }

    public Creature(World world, float xPos, float yPos, float rotation, String creatureId) {
        this(world, xPos, yPos, creatureId);
    }

    public Creature(World world, float xPos, float yPos, String creatureId) {
        super(world, xPos, yPos);
        MoonBase.log("Spawning: " + creatureId);
        this.group = new Group();
        this.group.setPosition(this.spawnPosX, this.spawnPosY);
        this.velocity = new Vector2(0.0f, 0.0f);
        this.id = creatureId;
        try {
            this.creatureData = CreatureLoader.getInstance().getData(creatureId);
            this.init();
            this.health = this.maxHealth;
        }
        catch (Exception e) {
            MoonBase.error("Error spawning creature: \n");
            MoonBase.error(e.getLocalizedMessage());
            this.readyToRemove = true;
        }
        if (this.creatureData != null) {
            this.stateMachine = new DefaultStateMachine<Creature, CreatureState>(this, CreatureState.SPAWN);
            this.setupAnimEventListeners();
        }
    }

    @Override
    public void doneLoading() {
        try {
            this.creatureData = CreatureLoader.getInstance().getData(this.id);
            if (this.creatureData != null) {
                this.init();
                if (this.health < 0.0f) {
                    this.readyToRemove = true;
                } else {
                    this.setWorldPos(this.spawnPosX, this.spawnPosY);
                    if (this.creatureData != null) {
                        this.stateMachine = new DefaultStateMachine<Creature, CreatureState>(this, CreatureState.SPAWN);
                        this.setupAnimEventListeners();
                    }
                }
            } else {
                this.readyToRemove = true;
            }
        }
        catch (Exception e) {
            this.readyToRemove = true;
        }
    }

    public void init() {
        this.maxHealth = this.creatureData.maxHealth;
        this.createDrawables();
        this.createLight();
        this.setupPhysics();
        this.spawnXTile = this.getWorldXTile();
        this.spawnYTile = this.getWorldYTile();
        this.pathFinding = new PathFinding(this.world);
    }

    private void createLight() {
        if (this.creatureData.lightRadius > 0.0f && this.creatureData.lightColor != null) {
            Color lightColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
            try {
                lightColor = Color.valueOf(this.creatureData.lightColor);
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.light = new PointLight(this.world.rayHandler, 10, lightColor, this.creatureData.lightRadius / 256.0f, this.spawnPosX / 256.0f, this.spawnPosY / 256.0f);
            this.light.setXray(true);
            this.lightAnimator = new LightAnimator(this.world, this.getXPos(), this.getYPos());
            this.lightAnimator.setLight(this.light);
        }
    }

    protected void createDrawables() {
        float scale = 0.27f;
        if (this.creatureData.spineBaseScale != 0.0f) {
            scale = this.creatureData.spineBaseScale;
        }
        this.spineActor = new SpineActor(this.creatureData.spineActor, scale, this.world.gameScreen.skeletonRenderer);
        this.spineActor.setPosition(0.0f, 0.0f);
        if (this.creatureData.spineSkin != "") {
            this.spineActor.skeleton.setSkin(this.creatureData.spineSkin);
        }
        this.group.setDebug(false);
        this.group.addActor(this.spineActor);
        this.spineActor.state.setAnimation(0, "idle", true);
        this.group.addListener(new ClickListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
        this.world.gameScreen.mainGroup.addActor(this.group);
        this.targetIndicator = new AdditiveImage(this.world.gameScreen.skin.getDrawable("target-circle"));
        this.world.gameScreen.worldUiGroup.addActor(this.targetIndicator);
        this.targetIndicator.setColor(1.0f, 0.0f, 0.0f, 0.55f);
        this.targetIndicator.setVisible(false);
        this.debugLabel = new Label((CharSequence)"", this.world.gameScreen.labelStyle);
        this.debugLabel.setFontScale(0.25f);
        this.debugLabel.setPosition(0.0f, 200.0f);
        this.group.addActor(this.debugLabel);
        this.debugLabel.setVisible(true);
    }

    protected void setupPhysics() {
        this.categoryBits = (short)8;
        this.maskBits = (short)66;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        CircleShape shape = new CircleShape();
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = this.categoryBits;
        fdef.filter.maskBits = (short)(this.maskBits + this.categoryBits);
        fdef.density = 1.0f;
        fdef.friction = 0.1f;
        fdef.restitution = 0.1f;
        Vector2 pos = new Vector2(((float)this.getXTile() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f) / 256.0f, ((float)this.getYTile() * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f) / 256.0f);
        this.body = this.world.b2dWorld.createBody(bodyDef);
        this.body.setTransform(new Vector2(pos.x, pos.y), 0.0f);
        this.body.setFixedRotation(true);
        this.body.setLinearDamping(0.5f);
        this.body.setUserData(this);
        shape.setRadius(this.creatureData.physicsRadius / 256.0f);
        this.body.createFixture(fdef);
    }

    @Override
    protected int getXTile() {
        double xd = Math.floor(this.getXPos() / Tile.GRID_SIZE);
        return Math.round((float)xd);
    }

    @Override
    protected int getYTile() {
        double yd = Math.floor(this.getYPos() / Tile.GRID_SIZE);
        return Math.round((float)yd);
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

    public void setupAnimEventListeners() {
        this.spineActor.state.addListener(new AnimationState.AnimationStateAdapter(){

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                if (event.getData().getName().equals("footstep") && Creature.this.creatureData.footStepSoundFx != null) {
                    Creature.this.playFootstep(Creature.this.creatureData.footStepSoundFx);
                }
                if (event.getData().getName().equals("takeHitSound")) {
                    float pitch = MathUtils.random(0.95f, 1.05f);
                    Audio.getInstance().playSound("sounds/interact_hit2.ogg", 0.7f, pitch);
                }
                if (event.getData().getName().equals("aggroSound") && Creature.this.creatureData.aggroSoundFx != null) {
                    Creature.this.playSoundDef(Creature.this.creatureData.aggroSoundFx);
                }
            }

            @Override
            public void complete(AnimationState.TrackEntry entry) {
            }
        });
    }

    private void playSoundDef(SoundDef soundDef) {
        float distVol = Audio.getInstance().playerDistanceMultiplier(this.world, this.getXPos(), this.getYPos(), 900.0f, soundDef.volume);
        float pan = Audio.getInstance().playerDistancePan(this.world, this.getXPos(), this.getYPos());
        pan *= 0.5f;
        float pitch = 1.0f;
        if (soundDef.minPitch > 0.0f && soundDef.maxPitch > 0.0f) {
            pitch = MathUtils.random(soundDef.minPitch, soundDef.maxPitch);
        }
        Audio.getInstance().playSound(soundDef.path, distVol, pitch, pan);
    }

    private void playFootstep(SoundDef soundDef) {
        float distVol = Audio.getInstance().playerDistanceMultiplier(this.world, this.getXPos(), this.getYPos(), 1000.0f, soundDef.volume);
        float pan = Audio.getInstance().playerDistancePan(this.world, this.getXPos(), this.getYPos());
        float pitch = 1.0f;
        if (soundDef.minPitch > 0.0f && soundDef.maxPitch > 0.0f) {
            pitch = MathUtils.random(soundDef.minPitch, soundDef.maxPitch);
            if (this.footstep == 0) {
                pitch *= 1.2f;
                this.footstep = 1;
            } else if (this.footstep == 1) {
                this.footstep = 0;
            }
        }
        Audio.getInstance().playSound(soundDef.path, distVol, pitch, pan);
    }

    @Override
    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("id", this.id);
        properties.put("health", Float.valueOf(this.health));
        properties.put("spawnXTile", this.spawnXTile);
        properties.put("spawnYTile", this.spawnYTile);
        return properties;
    }

    @Override
    public void remove() {
        if (this.light != null) {
            this.light.remove();
            this.light = null;
        }
        this.group.remove();
    }

    @Override
    public void playerAction(Player player) {
        if (this.stateMachine.getCurrentState() != CreatureState.DEAD) {
            // empty if block
        }
    }

    @Override
    public void takeDamage(float amount, DamageDef damageDef) {
        float originalAmount = amount;
        if (this.isAlive()) {
            if (damageDef.damageType == DamageDef.DamageTypes.fire) {
                amount = this.creatureData.fireDamageArmor * amount;
            }
            if (amount > 0.0f) {
                this.world.gameScreen.hud.newDamageFlyoff(amount, this.getXPos(), this.getYPos() + this.creatureData.physicsRadius, Color.WHITE);
                this.spineActor.state.setAnimation(2, "takeHit", false);
                this.spineActor.state.addEmptyAnimation(2, 0.05f, 0.0f);
                this.colorFlashTimer = colorFlashLength;
                this.spineActor.spineActorDraw = new SpineActorDraw(){

                    @Override
                    public void shaderStart(Batch batch, float value) {
                        batch.setShader(ShaderHelper.colorFlashShader);
                        ShaderHelper.colorFlashShader.setUniformf("u_color", 1.0f, 0.8f, 0.8f, 1.0f);
                        ShaderHelper.colorFlashShader.setUniformf("u_blend", 0.6f);
                        Creature.this.spineActor.shaderBlend = 1.0f;
                    }

                    @Override
                    public void shaderEnd(Batch batch) {
                        batch.setShader(null);
                    }
                };
                MoonBase.debug("Creature taking damage : " + amount);
                this.health -= amount;
            } else {
                MoonBase.debug("Creature mitigated incoming damage: " + originalAmount);
            }
        }
    }

    private void die() {
        MoonBase.log(this.creatureData.id + " dead!");
        this.targetIndicator.remove();
        this.idleMove();
        this.getBody().setActive(false);
        this.stateMachine.changeState(CreatureState.DEAD);
        if (this.creatureData.deathParticles != null) {
            ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get(this.creatureData.deathParticles, ParticleEffect.class));
            ParticleActor hitFx = new ParticleActor(p, true);
            hitFx.setPosition(this.getXPos(), this.getYPos());
            hitFx.pfx.start();
            this.world.gameScreen.topGroup.addActor(hitFx);
        }
        new ImpactPuff(this.world, this.getXPos(), this.getYPos() + 5.0f, this.creatureData.hitRadius * 2.0f);
        for (TileInteractionAction tia : this.creatureData.itemDrops) {
            this.dropItems(tia);
        }
        if (this.creatureData.deathSoundFx != null) {
            this.playSoundDef(this.creatureData.deathSoundFx);
        }
    }

    @Override
    public boolean isSaved() {
        return this.isAlive();
    }

    public void deathFinished() {
        this.readyToRemove = true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.health <= 0.0f && this.stateMachine.getCurrentState() != CreatureState.DEAD) {
            this.die();
        }
        this.group.setPosition(this.getXPos(), this.getYPos());
        this.group.setUserObject(Float.valueOf(this.getYPos()));
        if (this.light != null) {
            this.light.setPosition(this.group.getX() / 256.0f, this.group.getY() / 256.0f);
        }
        if (this.stateMachine != null) {
            this.stateMachine.update();
            this.spineActor.update(delta);
        }
        if (this.attackCooldownTimer > 0.0f) {
            this.attackCooldownTimer -= delta;
        }
        if (this.colorFlashTimer > 0.0f) {
            this.colorFlashTimer -= delta;
            this.spineActor.shaderBlend = this.colorFlashTimer / colorFlashLength;
            if (this.colorFlashTimer <= 0.0f) {
                this.spineActor.spineActorDraw = null;
            }
        }
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

    public void idleMove() {
        this.move(0.0f, 0.0f);
    }

    public void resetIdleTimer() {
        this.idleTimer = 0.0f;
        this.idleTimerDelay = MathUtils.random(3.0f, 5.0f);
    }

    public boolean updateIdleTimer(float delta) {
        this.idleTimer += delta;
        return this.idleTimer > this.idleTimerDelay;
    }

    public void resetRepathTimer() {
        this.repathTimer = 0.0f;
    }

    public boolean updateRepathTimer(float delta) {
        this.repathTimer += delta;
        return this.repathTimer > this.repathTimerDelay;
    }

    public void resetAggroTimer() {
        this.aggroTimer = 1.0f;
    }

    public boolean updateAggroTimer() {
        this.aggroTimer -= Gdx.graphics.getDeltaTime();
        return this.aggroTimer <= 0.0f;
    }

    public boolean findTarget(float delta) {
        if (this.creatureData.aggressive && MoonBase.getCurrentMission().creatureMode == Mission.creatureModes.hostile) {
            if (this.targetPos == null) {
                this.targetPos = new Vector2(0.0f, 0.0f);
            }
            this.searchTimer += delta;
            if (this.searchTimer > this.searchTimerDelay) {
                this.searchTimer = 0.0f;
                Player candidate = this.findNearestPlayer(this.creatureData.aggroDistance);
                if (candidate != null) {
                    this.targetPlayer = candidate;
                    this.targetPos.set(candidate.getXPos(), candidate.getYPos());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean targetFarEnoughToIgnore() {
        if (this.targetPlayer == null) {
            return true;
        }
        this.targetPos.set(this.targetPlayer.getXPos(), this.targetPlayer.getYPos());
        if (this.targetPlayer.isOnHabitat()) {
            return true;
        }
        if (this.targetBase != null) {
            this.targetPos.set(this.targetBase.getXCenter(), this.targetBase.getYCenter());
        }
        Vector2 currentPos = new Vector2(this.getXPos(), this.getYPos());
        Vector2 diff = this.targetPos.cpy().sub(currentPos);
        return diff.len() > this.creatureData.giveUpDistance;
    }

    public boolean readyToAttack() {
        return this.attackCooldownTimer <= 0.0f;
    }

    public boolean closeEnoughToAttack() {
        if (this.targetPlayer != null) {
            this.targetPos.set(this.targetPlayer.getXPos(), this.targetPlayer.getYPos());
        }
        if (this.targetPlayer.isFlyingRocket()) {
            return false;
        }
        Vector2 currentPos = new Vector2(this.getXPos(), this.getYPos());
        Vector2 diff = this.targetPos.cpy().sub(currentPos);
        MoonBase.debug("Creature target dist: " + diff.len());
        this.usableAttacks.clear();
        for (CreatureAttackData cad : this.creatureData.attackDataList) {
            if (!(diff.len() <= cad.maxDistanceFromPlayer)) continue;
            if (diff.len() >= cad.minDistanceFromPlayer) {
                this.usableAttacks.add(cad);
                this.tooCloseToAttack = false;
                continue;
            }
            this.tooCloseToAttack = true;
        }
        MoonBase.debug("usableAttacks: " + this.usableAttacks.size());
        this.currentAttack = null;
        if (this.usableAttacks.size() > 1) {
            float roll = MathUtils.random(0.0f, 1.0f);
            System.out.println("roll: " + roll);
            float base = 0.0f;
            for (CreatureAttackData cad : this.usableAttacks) {
                System.out.println("checking roll >= " + base + " && roll < " + (cad.chance + base));
                if (roll >= base && roll < cad.chance + base) {
                    this.currentAttack = cad;
                    break;
                }
                base += cad.chance;
            }
        } else if (this.usableAttacks.size() == 1) {
            this.currentAttack = this.usableAttacks.get(0);
        }
        if (this.currentAttack != null) {
            MoonBase.log("" + this.creatureData.id + " attacking with " + this.currentAttack.id);
        }
        return this.currentAttack != null;
    }

    public Vector2 getTargetPlayerBodyPosition() {
        return new Vector2(this.targetPlayer.getXPos() / 256.0f, this.targetPlayer.getYPos() / 256.0f);
    }

    private Player findNearestPlayer(float maxDist) {
        Player best = null;
        float bestDist = maxDist;
        try {
            if (this.world != null && this.world.player != null) {
                Player p = this.world.player;
                if (!p.isOnHabitat() && !p.isFlyingRocket()) {
                    float d = Vector2.dst(this.getXPos(), this.getYPos(), p.getXPos(), p.getYPos());
                    if (d < bestDist) {
                        bestDist = d;
                        best = p;
                    }
                }
            }
        } catch (Exception ignored) {}
        try {
            if (this.world != null && this.world.gameScreen != null) {
                for (Player rp : this.world.gameScreen.getRemotePlayers()) {
                    if (rp == null) continue;
                    if (rp.isOnHabitat() || rp.isFlyingRocket()) continue;
                    float d = Vector2.dst(this.getXPos(), this.getYPos(), rp.getXPos(), rp.getYPos());
                    if (d < bestDist) {
                        bestDist = d;
                        best = rp;
                    }
                }
            }
        } catch (Exception ignored) {}
        return best;
    }

    public void resetAtackCheckTimer() {
        this.chaseAttackCheckTimer = 0.0f;
    }

    public boolean isChaseAttackCheckReady(float delta) {
        this.chaseAttackCheckTimer += delta;
        if (this.chaseAttackCheckTimer > 0.25f) {
            this.chaseAttackCheckTimer = 0.0f;
            return true;
        }
        return false;
    }

    public void preAttack() {
        Vector2 targetPos = new Vector2();
        if (targetPos == null) {
            targetPos = new Vector2(0.0f, 0.0f);
        }
        targetPos.set(this.targetPlayer.getXPos(), this.targetPlayer.getYPos() - 20.0f);
        Vector2 currentPos = new Vector2(this.getXPos(), this.getYPos());
        Vector2 diff = targetPos.cpy().sub(currentPos);
        diff.limit(this.currentAttack.maxLurchDistance);
        targetPos.set(currentPos.x + diff.x, currentPos.y + diff.y);
        this.targetIndicator.setSize(this.currentAttack.attackRadius * 2.0f, this.currentAttack.attackRadius * 2.0f);
        this.targetIndicator.setPosition(targetPos.x, targetPos.y, 1);
        this.targetIndicator.setVisible(this.currentAttack.showTargetIndicator);
        this.faceDirection(targetPos.x, targetPos.y);
        if (this.currentAttack.soundPreAttack != null) {
            this.playSoundDef(this.currentAttack.soundPreAttack);
        }
        if (this.currentAttack.attackWindupParticles != null) {
            ParticleEffect hitFx = new ParticleEffect(AssetManagerSingleton.getInstance().get(this.currentAttack.attackWindupParticles, ParticleEffect.class));
            ParticleActor pa = new ParticleActor(hitFx, true);
            pa.setPosition(currentPos.x, currentPos.y);
            pa.pfx.start();
            this.world.gameScreen.floorGroup.addActor(pa);
        }
    }

    public void attackTarget() {
        Vector2 currentPos = new Vector2(this.getXPos(), this.getYPos());
        Vector2 diff = this.targetPos.cpy().sub(currentPos);
        this.startLurchAtTarget();
        this.attackCooldownTimer = this.currentAttack.attackCooldown;
        if (this.targetBase != null) {
            // empty if block
        }
    }

    public void startLurchAtTarget() {
        float minDist = this.creatureData.physicsRadius + 25.0f;
        this.lurchAlpha = 0.0f;
        if (this.lurchStartPos == null) {
            this.lurchStartPos = new Vector2(0.0f, 0.0f);
        }
        this.lurchStartPos.set(this.getXPos(), this.getYPos());
        if (this.lurchTargetPos == null) {
            this.lurchTargetPos = new Vector2(0.0f, 0.0f);
        }
        this.lurchTargetPos.set(this.targetPos.x, this.targetPos.y);
        Vector2 lurch = this.lurchTargetPos.cpy().sub(this.lurchStartPos);
        lurch.limit(this.currentAttack.maxLurchDistance);
        float lurchDist = lurch.len();
        lurch.limit(lurchDist - 25.0f);
        this.lurchTargetPos.set(this.lurchStartPos.x + lurch.x, this.lurchStartPos.y + lurch.y);
        this.lurchTimer = 0.0f;
        this.faceDirection(this.lurchTargetPos.x, this.lurchTargetPos.y);
        if (this.currentAttack != null && this.currentAttack.soundStart != null) {
            this.playSoundDef(this.currentAttack.soundStart);
        }
    }

    public void lurchAtTarget() {
        float delta = Gdx.graphics.getDeltaTime();
        this.lurchTimer += delta;
        this.lurchAlpha = this.lurchTimer / this.currentAttack.attackDuration;
        if (this.lurchAlpha < 1.0f) {
            float x = Interpolation.exp5In.apply(this.lurchStartPos.x, this.lurchTargetPos.x, this.lurchAlpha);
            float y = Interpolation.exp5In.apply(this.lurchStartPos.y, this.lurchTargetPos.y, this.lurchAlpha);
            this.setWorldPos(x, y);
        } else {
            MoonBase.log("attack type: " + (Object)((Object)this.currentAttack.attackType));
            switch (this.currentAttack.attackType) {
                case melee: {
                    this.meleeAttack();
                    break;
                }
                case aoe: {
                    this.aoeAttack();
                    break;
                }
                case projectile: {
                    this.fireProjectile();
                    break;
                }
                default: {
                    MoonBase.error("Unhandled attack type: " + (Object)((Object)this.currentAttack.attackType));
                    this.stateMachine.changeState(CreatureState.ATTACK_COOLDOWN);
                }
            }
        }
    }

    private void aoeAttack() {
        this.spineActor.state.setAnimation(0, "attackLand", false);
        Ellipse c = new Ellipse(this.getXPos(), this.getYPos(), this.currentAttack.attackRadius * 2.0f, this.currentAttack.attackRadius * 2.0f);
        Circle attackCircle = new Circle(this.getXPos(), this.getYPos(), this.currentAttack.attackRadius);
        Circle playerCircle = new Circle(this.targetPlayer.getXPos(), this.targetPlayer.getYPos(), Player.BODY_RADIUS);
        if (this.targetPlayer.isDrivingVehicle()) {
            playerCircle.radius *= 2.0f;
        }
        this.meleeParticles();
        this.pulseLight();
        if (attackCircle.overlaps(playerCircle)) {
            if (this.targetPlayer != null) {
                float attackDmg = MathUtils.random((float)this.currentAttack.minDamage, (float)this.currentAttack.maxDamage);
                if (this.targetPlayer.isDrivingVehicle()) {
                    this.targetPlayer.getVehicle().takeDamage(attackDmg, false);
                } else {
                    this.targetPlayer.playerStatus.takeHitDamage(attackDmg);
                    this.targetPlayer.applyKnockback(this.getXPos(), this.getYPos(), this.currentAttack.knockback);
                }
                if (this.currentAttack != null && this.currentAttack.soundHit != null) {
                    this.playSoundDef(this.currentAttack.soundHit);
                }
            }
        } else {
            if (this.currentAttack != null && this.currentAttack.soundMiss != null) {
                this.playSoundDef(this.currentAttack.soundMiss);
            }
            MoonBase.log("No target in radius. attackFinished");
        }
        this.targetIndicator.setVisible(false);
        this.stateMachine.changeState(CreatureState.ATTACK_COOLDOWN);
    }

    private void pulseLight() {
        if (this.light != null && this.lightAnimator != null) {
            this.lightAnimator.animDistance(this.creatureData.lightRadius * 4.0f, this.creatureData.lightRadius, 0.6f);
        }
    }

    private void fireProjectile() {
        Bone projectileBone;
        if (this.currentAttack.projectileSeq > 0) {
            // empty if block
        }
        this.targetPos.set(this.targetPlayer.getXPos(), this.targetPlayer.getYPos());
        Vector2 targetVelocity = this.targetPlayer.getVelocity().scl(0.5f);
        this.targetPos.add(targetVelocity);
        MoonBase.log(targetVelocity.angle() + "");
        Vector2 currentPos = new Vector2(this.getXPos(), this.getYPos());
        MoonBase.log("creature: " + this.creatureData.id + " projectile bone: " + this.currentAttack.projectileSpawnBone);
        if (this.currentAttack.projectileSpawnBone != null && (projectileBone = this.spineActor.skeleton.findBone(this.currentAttack.projectileSpawnBone)) != null) {
            currentPos.x += projectileBone.getWorldX();
            currentPos.y += projectileBone.getWorldY();
        }
        Vector2 diff = this.targetPos.cpy().sub(currentPos);
        diff.limit(this.currentAttack.maxDistanceFromPlayer);
        this.targetPos.set(currentPos.x + diff.x, currentPos.y + diff.y);
        this.spineActor.state.setAnimation(0, "attackLand", false);
        this.playSoundDef(this.currentAttack.soundHit);
        this.stateMachine.changeState(CreatureState.ATTACK_COOLDOWN);
        float minAngle = 0.0f;
        float angleDiffBetween = 0.0f;
        if (this.currentAttack.projectileSeq > 1 && this.currentAttack.projectileArc > 0.0f) {
            minAngle = diff.angle() - 180.0f - 0.5f * this.currentAttack.projectileArc;
            angleDiffBetween = 0.0f;
            angleDiffBetween = this.currentAttack.projectileArc < 360.0f ? this.currentAttack.projectileArc / (float)(this.currentAttack.projectileSeq - 1) : this.currentAttack.projectileArc / (float)this.currentAttack.projectileSeq;
        } else if (this.currentAttack.projectileSeq > 1 || this.currentAttack.projectileArc > 0.0f) {
            // empty if block
        }
        for (int i = 0; i < this.currentAttack.projectileSeq; ++i) {
            System.out.println("pew");
            Projectile p = ProjectileFactory.getInstance().newProjectile(this.world, this.currentAttack.projectileDataId);
            p.setSpawnPosition(currentPos.x, currentPos.y);
            p.worldTargetPos = new Vector2(this.targetPos.x, this.targetPos.y);
            p.minHitDamage = this.currentAttack.minDamage;
            p.maxHitDamage = this.currentAttack.maxDamage;
            p.setAngleOffest(minAngle + (float)i * angleDiffBetween);
            p.init();
        }
    }

    public void meleeAttack() {
        this.spineActor.state.setAnimation(0, "attackLand", false);
        Ellipse c = new Ellipse(this.getXPos(), this.getYPos(), this.currentAttack.attackRadius * 2.0f, this.currentAttack.attackRadius * 2.0f);
        Circle attackCircle = new Circle(this.getXPos(), this.getYPos(), this.currentAttack.attackRadius);
        Circle playerCircle = new Circle(this.targetPlayer.getXPos(), this.targetPlayer.getYPos(), Player.BODY_RADIUS);
        if (this.targetPlayer.isDrivingVehicle()) {
            playerCircle.radius *= 2.0f;
        }
        this.meleeParticles();
        if (attackCircle.overlaps(playerCircle)) {
            if (this.targetPlayer != null) {
                float attackDmg = MathUtils.random((float)this.currentAttack.minDamage, (float)this.currentAttack.maxDamage);
                if (this.targetPlayer.isDrivingVehicle()) {
                    this.targetPlayer.getVehicle().takeDamage(attackDmg, false);
                } else {
                    this.targetPlayer.playerStatus.takeHitDamage(attackDmg);
                    this.targetPlayer.applyKnockback(this.getXPos(), this.getYPos(), this.currentAttack.knockback);
                }
                if (this.currentAttack != null && this.currentAttack.soundHit != null) {
                    this.playSoundDef(this.currentAttack.soundHit);
                }
            }
        } else {
            if (this.currentAttack != null && this.currentAttack.soundMiss != null) {
                this.playSoundDef(this.currentAttack.soundMiss);
            }
            MoonBase.log("No target in radius. attackFinished");
        }
        this.targetIndicator.setVisible(false);
        this.stateMachine.changeState(CreatureState.ATTACK_COOLDOWN);
    }

    private void meleeParticles() {
        ParticleEffect hitFx = new ParticleEffect(AssetManagerSingleton.getInstance().get(this.currentAttack.hitParticles, ParticleEffect.class));
        ParticleActor pa = new ParticleActor(hitFx, true);
        pa.setPosition(this.lurchTargetPos.x, this.lurchTargetPos.y);
        pa.pfx.start();
        this.world.gameScreen.mainGroup.addActor(pa);
        new ImpactPuff(this.world, this.getXPos(), this.getYPos(), this.creatureData.hitRadius * 2.0f);
    }

    @Override
    public void setWorldPos(float x, float y) {
        this.body.setTransform(x / 256.0f, y / 256.0f, 0.0f);
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
        if (diff.len() > this.creatureData.physicsRadius + 50.0f) {
            float angleToTarget = diff.angle();
            Vector2 v = new Vector2(1.0f, 0.0f);
            v.setAngle(angleToTarget);
            this.move(v.x, v.y);
        }
    }

    @Deprecated
    public void searchForPlayer(float delta) {
        this.searchTimer += delta;
        if (this.searchTimer > this.searchTimerDelay) {
            this.searchTimer = 0.0f;
            if (this.world.player != null) {
                GridPoint2 gridPoint2 = new GridPoint2(this.getXTile(), this.getYTile());
            }
        }
    }

    private void move(float vX, float vY) {
        this.walkSpeedMultiplier = 1.0f;
        if (this.world.weatherManager.getWindSpeed() > windSpeedSlowWalkThreshold) {
            this.walkSpeedMultiplier *= 0.66f;
        }
        if (vX == 0.0f && vY == 0.0f) {
            this.velocity.set(0.0f, 0.0f);
            this.body.setLinearDamping(1.0f);
        } else {
            this.body.setLinearDamping(0.5f);
        }
        if (vX > 0.0f) {
            this.spineActor.setFlip(true, false);
        } else if (vY < 0.0f) {
            this.spineActor.setFlip(false, false);
        }
        float delta = Gdx.graphics.getDeltaTime();
        this.velocity.add(vX * this.creatureData.walkAcceleration * this.walkSpeedMultiplier * delta, vY * this.creatureData.walkAcceleration * this.walkSpeedMultiplier * delta);
        this.velocity.limit(this.creatureData.maxWalkSpeedTilesPerSec * Tile.TILE_SIZE * this.walkSpeedMultiplier);
        this.body.setLinearVelocity(this.velocity.x / 256.0f, this.velocity.y / 256.0f);
    }

    @Override
    public Body getBody() {
        return this.body;
    }

    public void cancelLurch() {
        if (this.stateMachine.getCurrentState() == CreatureState.ATTACK) {
            this.lurchTargetPos.set(this.getXPos(), this.getYPos());
            MoonBase.debug("Creature: Cancelling lurch");
        }
    }

    public void setGroupAlpha(float a) {
        this.group.getColor().a = MathUtils.clamp(a, 0.0f, 1.0f);
    }

    private void faceDirection(float tileXCenter, float tileYCenter) {
        if (this.getXPos() < tileXCenter) {
            this.spineActor.setFlip(true, false);
        } else if (this.getXPos() > tileXCenter) {
            this.spineActor.setFlip(false, false);
        }
    }

    public void dropItems(TileInteractionAction action) {
        for (InventoryItemData specialDrop : action.guaranteedDrops) {
            for (int i = 0; i < specialDrop.amount; ++i) {
                Item newItem = ItemFactory.getInstance().createItem(specialDrop.itemId);
                new ItemPickup(this.world, this.getXPos(), this.getYPos(), newItem);
            }
        }
        int quantity = MathUtils.random(action.minDrop, action.maxDrop);
        block2: for (int i = 0; i < quantity; ++i) {
            float base = 0.0f;
            float rand = MathUtils.random();
            int itemIndex = 0;
            for (ItemDropperItemData item : action.itemList) {
                if (rand > base && rand <= item.dropChance + base) {
                    Item newItem = ItemFactory.getInstance().createItem(item.itemId);
                    new ItemPickup(this.world, this.getXPos(), this.getYPos(), newItem);
                    continue block2;
                }
                base += action.itemList.get((int)itemIndex).dropChance;
                ++itemIndex;
            }
        }
    }

    public void pathToPlayer() {
        GraphPath<TileNode> path = null;
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
            path = this.pathFinding.getPath(npcPos, this.pathFindingQueue.get((int)0).destination, 10);
            this.walkTarget = new Vector2((float)this.pathFindingQueue.get((int)0).destination.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)this.pathFindingQueue.get((int)0).destination.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
            if (path != null) {
                this.walkTarget.set((float)path.get((int)(path.getCount() - 1)).x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)path.get((int)(path.getCount() - 1)).y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
                MoonBase.debug("Creature: path to player: " + path.getCount());
            }
            World.gridPointPool.free(playerPos);
            World.gridPointPool.free(npcPos);
        }
    }

    public GridPoint2 getRandomPatrolTile() {
        boolean foundTile = false;
        int maxDist = 2;
        int x = this.getWorldXTile();
        int y = this.getWorldYTile();
        GridPoint2 destGp = World.gridPointPool.obtain();
        destGp.set(0, 0);
        for (int tries = 20; !foundTile && tries > 0; --tries) {
            int rY;
            int rX = MathUtils.random(x - maxDist, x + maxDist);
            if (!(Vector2.dst(this.spawnXTile, this.spawnYTile, rX, rY = MathUtils.random(y - maxDist, y + maxDist)) < (float)this.creatureData.maxPatrolTileDistanceFromSpawn)) continue;
            Tile t = this.world.getTile(rX, rY);
            if (t == null) {
                destGp.set(rX, rY);
                foundTile = true;
                continue;
            }
            if (t.hasPhysicsBody()) continue;
            destGp.set(rX, rY);
            foundTile = true;
        }
        if (destGp.x == 0 && destGp.y == 0) {
            destGp = null;
        }
        MoonBase.debug("Creature: path to: " + destGp);
        return destGp;
    }

    public void pathToTile(int worldX, int worldY) {
        GridPoint2 dest = World.gridPointPool.obtain();
        dest.set(worldX, worldY);
        this.pathFindingQueue.clear();
        PathFindingAction pfa = new PathFindingAction();
        pfa.setDestination(dest);
        this.pathFindingQueue.add(pfa);
        boolean canPath = false;
        this.walkTarget = new Vector2((float)this.pathFindingQueue.get((int)0).destination.x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)this.pathFindingQueue.get((int)0).destination.y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
        GraphPath<TileNode> path = this.pathFinding.getPath(dest, this.pathFindingQueue.get((int)0).destination, 10);
        if (path != null) {
            this.walkTarget.set((float)path.get((int)(path.getCount() - 1)).x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)path.get((int)(path.getCount() - 1)).y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
            MoonBase.debug("Creature: path: " + path.getCount());
        }
        World.gridPointPool.free(dest);
    }

    public boolean followPath() {
        if (this.currentPos == null) {
            this.currentPos = new Vector2(0.0f, 0.0f);
        }
        if (this.pathTargetPos == null) {
            this.pathTargetPos = new Vector2(0.0f, 0.0f);
        }
        float closeEnoughToPlayer = 20.0f;
        if (this.pathFinding.smoothablePath != null) {
            Array nodes = this.pathFinding.smoothablePath.nodes;
            if (nodes.size > 0) {
                if (nodes.size == 1) {
                    this.pathTargetPos = new Vector2(this.walkTarget.x, this.walkTarget.y);
                    this.currentPos.set(this.getXPos(), this.getYPos());
                    Vector2 diff = this.pathTargetPos.cpy().sub(this.currentPos);
                    if (diff.len() < closeEnoughToPlayer) {
                        nodes.removeIndex(0);
                        MoonBase.debug("Creature: Reached player pos");
                        this.reachedEndOfPath();
                    } else {
                        float angleToTarget = diff.angle();
                        Vector2 v = new Vector2(1.0f, 0.0f);
                        v.setAngle(angleToTarget);
                        this.move(v.x, v.y);
                    }
                } else {
                    if (((TileNode)nodes.get((int)0)).x == this.getXTile() && ((TileNode)nodes.get((int)0)).y == this.getYTile()) {
                        nodes.removeIndex(0);
                    }
                    this.pathTargetPos = new Vector2((float)((TileNode)nodes.get((int)0)).x * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f, (float)((TileNode)nodes.get((int)0)).y * Tile.TILE_SIZE + Tile.TILE_SIZE / 2.0f);
                    Vector2 currentPos2 = new Vector2(this.getXPos(), this.getYPos());
                    Vector2 diff = this.pathTargetPos.cpy().sub(currentPos2);
                    if (diff.len() < 30.0f) {
                        nodes.removeIndex(0);
                        MoonBase.debug("Creature: Reached node");
                    } else {
                        float angleToTarget = diff.angle();
                        Vector2 v = new Vector2(1.0f, 0.0f);
                        v.setAngle(angleToTarget);
                        this.move(v.x, v.y);
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
        MoonBase.debug("Creature: removing pathing queue item");
        this.pathFindingQueue.remove(0);
        this.velocity.set(0.0f, 0.0f);
        this.move(this.velocity.x, this.velocity.y);
    }

    public void cancelPathing() {
        this.pathFinding.cancel();
        this.pathFindingQueue.clear();
        this.idleMove();
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

    public void setEnclosed(boolean enclosed) {
        this.enclosed = enclosed;
        if (enclosed) {
            this.resetSleep();
        }
    }

    public boolean getEnclosed() {
        return this.enclosed;
    }

    public void resetSleep() {
        this.sleepTimer = 10.0f;
    }

    public void sleep() {
        this.sleepTimer -= Gdx.graphics.getDeltaTime();
        if (this.sleepTimer <= 0.0f) {
            MoonBase.log("Creature slept for 10sec, removing");
            this.readyToRemove = true;
        }
    }
}
