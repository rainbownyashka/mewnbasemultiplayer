/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ProjectileFactory;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Creature;
import com.cairn4.moonbase.entities.CreatureRayCastCallback;
import com.cairn4.moonbase.worlddata.CreatureAttackData;
import com.cairn4.moonbase.worlddata.ProjectileData;

public enum CreatureState implements State<Creature>
{
    SPAWN{

        @Override
        public void enter(Creature creature) {
            super.enter(creature);
            creature.spineActor.state.setAnimation(0, "idle", true);
        }

        @Override
        public void update(Creature creature) {
            creature.stateMachine.changeState(IDLE);
        }
    }
    ,
    IDLE{

        @Override
        public void enter(Creature creature) {
            super.enter(creature);
            creature.spineActor.state.setAnimation(0, "idle", true);
            creature.resetIdleTimer();
            creature.idleMove();
        }

        @Override
        public void update(Creature creature) {
            creature.idleMove();
            if (creature.findTarget(Gdx.graphics.getDeltaTime())) {
                creature.stateMachine.changeState(AGGRO);
            } else if (creature.updateIdleTimer(Gdx.graphics.getDeltaTime())) {
                creature.stateMachine.changeState(PATROL);
            }
        }
    }
    ,
    PATROL{

        @Override
        public void enter(Creature creature) {
            super.enter(creature);
            creature.cancelPathing();
            GridPoint2 gp = creature.getRandomPatrolTile();
            if (gp != null) {
                creature.pathToTile(gp.x, gp.y);
                World cfr_ignored_0 = creature.world;
                World.gridPointPool.free(gp);
                creature.spineActor.state.setAnimation(0, "walk", true);
            } else {
                System.out.println("No tile to path to.");
                creature.stateMachine.changeState(IDLE);
            }
        }

        @Override
        public void update(Creature creature) {
            if (creature.findTarget(Gdx.graphics.getDeltaTime())) {
                creature.stateMachine.changeState(AGGRO);
            } else if (creature.followPath()) {
                creature.stateMachine.changeState(IDLE);
            }
        }
    }
    ,
    AGGRO{

        @Override
        public void enter(Creature creature) {
            super.enter(creature);
            creature.cancelPathing();
            creature.resetAggroTimer();
            creature.spineActor.state.setAnimation(0, "aggro", false);
            creature.spineActor.state.addAnimation(0, "idle", true, 0.0f);
        }

        @Override
        public void update(Creature creature) {
            creature.idleMove();
            if (creature.updateAggroTimer()) {
                creature.stateMachine.changeState(CHASE);
            }
        }
    }
    ,
    RETREAT{

        @Override
        public void enter(Creature creature) {
            super.enter(creature);
            creature.cancelPathing();
            creature.resetAggroTimer();
            creature.resetRepathTimer();
            creature.resetAtackCheckTimer();
            creature.spineActor.state.setAnimation(0, "walk", true);
        }

        @Override
        public void update(Creature creature) {
            if (creature.targetPlayer != null) {
                if (creature.closeEnoughToAttack() && creature.readyToAttack()) {
                    // empty if block
                }
            } else {
                creature.stateMachine.changeState(IDLE);
            }
        }
    }
    ,
    CHASE{

        @Override
        public void enter(Creature creature) {
            super.enter(creature);
            creature.spineActor.state.setAnimation(0, "walk", true);
            creature.resetRepathTimer();
            creature.pathToPlayer();
            creature.followPath();
            creature.resetAtackCheckTimer();
        }

        @Override
        public void update(Creature creature) {
            if (creature.targetFarEnoughToIgnore()) {
                creature.targetPlayer = null;
                creature.targetBase = null;
                creature.stateMachine.changeState(IDLE);
            }
            if (creature.targetPlayer != null) {
                if (creature.updateRepathTimer(Gdx.graphics.getDeltaTime())) {
                    creature.pathToPlayer();
                    creature.resetRepathTimer();
                }
                if (creature.isChaseAttackCheckReady(Gdx.graphics.getDeltaTime())) {
                    if (creature.closeEnoughToAttack() && creature.readyToAttack()) {
                        Vector2 playerPos;
                        Vector2 creaturePos;
                        boolean ignoreRaycast = false;
                        if (creature.currentAttack.attackType == CreatureAttackData.AttackTypes.projectile) {
                            ProjectileData projectileData = ProjectileFactory.getInstance().getData(creature.currentAttack.projectileDataId);
                            if (!projectileData.bullet) {
                                ignoreRaycast = true;
                            }
                        }
                        if ((creaturePos = creature.getBody().getTransform().getPosition()).dst(playerPos = creature.getTargetPlayerBodyPosition()) <= creature.creatureData.hitRadius + 2.0f) {
                            ignoreRaycast = true;
                        }
                        CreatureRayCastCallback crcc = new CreatureRayCastCallback();
                        creature.body.getWorld().rayCast(crcc, creaturePos, playerPos);
                        if (creature.targetPlayer.isDrivingVehicle()) {
                            if (crcc.hitVehicleFirst() || ignoreRaycast) {
                                creature.stateMachine.changeState(PREATTACK);
                                creature.pathFinding.cancel();
                            } else {
                                MoonBase.debug("raycast hitting something");
                            }
                        } else if (crcc.hitPlayerFirst() || ignoreRaycast) {
                            creature.stateMachine.changeState(PREATTACK);
                            creature.pathFinding.cancel();
                        } else {
                            MoonBase.debug("raycast hitting something");
                        }
                    } else if (creature.tooCloseToAttack) {
                        // empty if block
                    }
                }
                creature.followPath();
            }
        }
    }
    ,
    PREATTACK{
        private float timer;

        @Override
        public void enter(Creature creature) {
            super.enter(creature);
            creature.cancelPathing();
            creature.preAttack();
            if (!creature.currentAttack.preAttackAnim.equals("")) {
                creature.spineActor.state.setAnimation(0, creature.currentAttack.preAttackAnim, true);
            }
            this.timer = creature.currentAttack.attackWindup;
        }

        @Override
        public void update(Creature creature) {
            this.timer -= Gdx.graphics.getDeltaTime();
            if (this.timer <= 0.0f) {
                creature.stateMachine.changeState(ATTACK);
            }
            creature.idleMove();
        }
    }
    ,
    ATTACK{
        float timer = 0.0f;

        @Override
        public void enter(Creature creature) {
            super.enter(creature);
            this.timer = creature.currentAttack.attackDuration;
            if (!creature.currentAttack.attackAnim.equals("")) {
                creature.spineActor.state.setAnimation(0, creature.currentAttack.attackAnim, false);
            }
            creature.attackTarget();
        }

        @Override
        public void update(Creature creature) {
            creature.lurchAtTarget();
        }

        @Override
        public void exit(Creature creature) {
            super.exit(creature);
        }
    }
    ,
    ATTACK_COOLDOWN{
        private float timer;

        @Override
        public void enter(Creature creature) {
            super.enter(creature);
        }

        @Override
        public void update(Creature creature) {
            creature.idleMove();
            if (creature.targetFarEnoughToIgnore()) {
                creature.targetPlayer = null;
                creature.targetBase = null;
                creature.stateMachine.changeState(IDLE);
            } else if (creature.readyToAttack()) {
                if (creature.closeEnoughToAttack()) {
                    Vector2 playerPos;
                    Vector2 creaturePos;
                    boolean ignoreRaycast = false;
                    if (creature.currentAttack.attackType == CreatureAttackData.AttackTypes.projectile) {
                        ProjectileData projectileData = ProjectileFactory.getInstance().getData(creature.currentAttack.projectileDataId);
                        if (!projectileData.bullet) {
                            ignoreRaycast = true;
                        }
                    }
                    if ((creaturePos = creature.getBody().getTransform().getPosition()).dst(playerPos = creature.getTargetPlayerBodyPosition()) <= creature.creatureData.hitRadius + 2.0f) {
                        ignoreRaycast = true;
                    }
                    CreatureRayCastCallback crcc = new CreatureRayCastCallback();
                    creature.body.getWorld().rayCast(crcc, creature.getBody().getTransform().getPosition(), creature.getTargetPlayerBodyPosition());
                    if (creature.targetPlayer.isDrivingVehicle()) {
                        if (crcc.hitVehicleFirst() || ignoreRaycast) {
                            creature.stateMachine.changeState(PREATTACK);
                        } else {
                            System.out.println("raycast hitting something");
                        }
                    } else if (crcc.hitPlayerFirst() || ignoreRaycast) {
                        creature.stateMachine.changeState(PREATTACK);
                    } else {
                        System.out.println("raycast hitting something");
                    }
                } else {
                    System.out.println("cooldown -> not close enough to attack");
                    creature.stateMachine.changeState(CHASE);
                }
            }
        }
    }
    ,
    DEAD{
        float fadeoutTimer = 0.0f;
        float fadeoutStart = 0.5f;
        float fadeoutEnd = 2.0f;
        Color lightColor = Color.WHITE;

        @Override
        public void enter(Creature creature) {
            super.enter(creature);
            this.fadeoutTimer = 0.0f;
            creature.spineActor.state.clearTracks();
            creature.spineActor.state.setAnimation(0, "dead", false);
            if (creature.light != null) {
                this.lightColor = creature.light.getColor();
            }
        }

        @Override
        public void update(Creature creature) {
            this.fadeoutTimer += Gdx.graphics.getDeltaTime();
            if (this.fadeoutTimer > this.fadeoutStart) {
                float alpha = 1.0f - (this.fadeoutTimer - this.fadeoutStart) / (this.fadeoutEnd - this.fadeoutStart);
                creature.setGroupAlpha(alpha);
                if (creature.light != null) {
                    creature.light.setColor(this.lightColor.r, this.lightColor.g, this.lightColor.b, alpha);
                }
                if (this.fadeoutTimer > this.fadeoutEnd) {
                    creature.deathFinished();
                }
            }
        }
    };


    @Override
    public void enter(Creature creature) {
    }

    @Override
    public void update(Creature creature) {
        creature.debugLabel.setText(creature.stateMachine.getCurrentState().name());
    }

    @Override
    public void exit(Creature entity) {
    }

    @Override
    public boolean onMessage(Creature entity, Telegram telegram) {
        return false;
    }
}

