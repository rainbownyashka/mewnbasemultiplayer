/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.dialog.DialogController;
import com.cairn4.moonbase.dialog.DialogNode;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.NpcNeeds;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.HudNotificationData;
import com.cairn4.moonbase.ui.HudNotifications;
import com.cairn4.moonbase.ui.Localization;

public enum NpcFriendState implements State<Npc>
{
    NOT_FRIEND{}
    ,
    NO_HOUSE_IDLE{

        @Override
        public void enter(Npc npc) {
            super.enter(npc);
            npc.npcStatusIcon.changeIcon(NpcNeeds.wantsHome);
        }

        @Override
        public void interact(Npc npc, Player player) {
            super.interact(npc, player);
            npc.idleMove();
            if (npc.home == null) {
                DialogNode dn = new DialogNode();
                dn.message = Localization.getInstance().get("npc_complain_noHome");
                DialogController.getInstance().displayNpcDialog(npc, dn, true);
                this.changeIfDiffState(npc, RESPONSE_DIALOG);
            }
        }

        @Override
        public void update(Npc npc) {
            super.update(npc);
            this.homeCheck(npc);
        }
    }
    ,
    NO_HOUSE_CLOSEENOUGH{

        @Override
        public void enter(Npc npc) {
            super.enter(npc);
            npc.npcStatusIcon.changeIcon(NpcNeeds.wantsHome);
        }

        @Override
        public void update(Npc npc) {
            super.update(npc);
            this.homeCheck(npc);
        }

        @Override
        public void interact(Npc npc, Player player) {
            super.interact(npc, player);
            DialogNode dn = new DialogNode();
            dn.message = Localization.getInstance().get("npc_complain_homeTooFar");
            DialogController.getInstance().displayNpcDialog(npc, dn, true);
            this.changeIfDiffState(npc, RESPONSE_DIALOG);
        }
    }
    ,
    PATHING_TO_HOUSE{

        @Override
        public void update(Npc npc) {
            super.update(npc);
            npc.followPath();
        }
    }
    ,
    HAVE_HOUSE{

        @Override
        public void update(Npc npc) {
        }
    }
    ,
    HOME_SAD{

        @Override
        public void enter(Npc npc) {
            super.enter(npc);
            npc.npcStatusIcon.changeIcon(NpcNeeds.sadHome);
        }

        @Override
        public void update(Npc npc) {
            super.update(npc);
            this.homeCheck(npc);
        }

        @Override
        public void interact(Npc npc, Player player) {
            super.interact(npc, player);
            if (npc.home != null) {
                DialogNode dn;
                if (!npc.home.hasAir) {
                    dn = new DialogNode();
                    dn.message = Localization.getInstance().get("npc_complain_noAir");
                    DialogController.getInstance().displayNpcDialog(npc, dn, true);
                }
                if (!npc.home.hasPower) {
                    dn = new DialogNode();
                    dn.message = Localization.getInstance().get("npc_complain_noPower");
                    DialogController.getInstance().displayNpcDialog(npc, dn, true);
                }
                this.changeIfDiffState(npc, RESPONSE_DIALOG);
            }
        }
    }
    ,
    RESPONSE_DIALOG{
        float timer = 0.0f;
        float delay = 4.0f;

        @Override
        public void enter(Npc npc) {
            super.enter(npc);
            npc.npcStatusIcon.hide();
            npc.idleMove();
            this.timer = 0.0f;
        }

        @Override
        public void update(Npc npc) {
            super.update(npc);
            this.timer += GdxAI.getTimepiece().getDeltaTime();
            if (this.timer > this.delay) {
                this.homeCheck(npc);
            }
        }
    }
    ,
    FRIEND_IDLE{

        @Override
        public void enter(Npc npc) {
            super.enter(npc);
            System.out.println("friend idle enter");
            npc.spineActor.state.setAnimation(0, "idle", true);
        }

        @Override
        public void update(Npc npc) {
            super.update(npc);
            this.repeatedHomeCheck(npc);
            npc.idleMove();
        }

        @Override
        public void interact(Npc npc, Player player) {
            super.interact(npc, player);
        }
    }
    ,
    FOLLOW_MOVING{
        float repathTimer = 0.0f;
        float repathDelay = 1.0f;

        @Override
        public void enter(Npc npc) {
            super.enter(npc);
            npc.cancelPathing();
            npc.pathToPlayer(Gdx.graphics.getDeltaTime());
            npc.setSearchTimerDelay(npc.fastSearchDelay);
            npc.spineActor.state.setAnimation(0, "walk_right", true);
        }

        @Override
        public void update(Npc npc) {
            super.update(npc);
            this.repathTimer += GdxAI.getTimepiece().getDeltaTime();
            if (this.repathTimer > this.repathDelay) {
                this.repathTimer = 0.0f;
                if (npc.tileDistanceToPlayer(10.0f)) {
                    npc.pathToPlayer(0.0f);
                } else {
                    HudNotificationData note = HudNotifications.hudNotificationDataPool.obtain();
                    note.message = npc.npcDef.name + ": I can't follow that far!";
                    MessageManager.getInstance().dispatchMessage(3, note);
                    HudNotifications.hudNotificationDataPool.free(note);
                    this.changeIfDiffState(npc, FRIEND_IDLE);
                }
            }
            if (npc.followPath()) {
                npc.friendStateMachine.changeState(FOLLOW_IDLE);
            }
        }

        @Override
        public void interact(Npc npc, Player player) {
            super.interact(npc, player);
            npc.cancelPathing();
            npc.friendStateMachine.changeState(FOLLOW_IDLE);
        }

        @Override
        public void exit(Npc npc) {
        }
    }
    ,
    FOLLOW_IDLE{
        float repathTimer = 0.0f;
        float repathDelay = 1.0f;

        @Override
        public void enter(Npc npc) {
            super.enter(npc);
            this.repathTimer = 0.0f;
            npc.cancelPathing();
            npc.pathToPlayer(Gdx.graphics.getDeltaTime());
            npc.setSearchTimerDelay(npc.fastSearchDelay);
            npc.spineActor.state.setAnimation(0, "idle", true);
        }

        @Override
        public void update(Npc npc) {
            super.update(npc);
            this.repathTimer += GdxAI.getTimepiece().getDeltaTime();
            if (this.repathTimer > this.repathDelay) {
                this.repathTimer = 0.0f;
                npc.pathToPlayer(0.0f);
                npc.friendStateMachine.changeState(FOLLOW_MOVING);
            }
        }

        @Override
        public void interact(Npc npc, Player player) {
            super.interact(npc, player);
        }

        @Override
        public void exit(Npc npc) {
        }
    };

    private float homeCheckTimer = 0.0f;
    private float homeCheckDelay = 1.0f;

    public void homeCheck(Npc npc) {
        if (npc.home == null) {
            this.changeIfDiffState(npc, NO_HOUSE_IDLE);
        } else {
            Vector2 homePos = new Vector2(npc.home.getXCenter(), npc.home.getYCenter());
            float dist = homePos.dst(npc.getXPos(), npc.getYPos());
            if (dist > 10.0f * Tile.TILE_SIZE) {
                this.changeIfDiffState(npc, NO_HOUSE_CLOSEENOUGH);
            } else if (!npc.home.hasPower || !npc.home.hasAir) {
                this.changeIfDiffState(npc, HOME_SAD);
            } else {
                this.changeIfDiffState(npc, FRIEND_IDLE);
            }
        }
    }

    protected void repeatedHomeCheck(Npc npc) {
        this.homeCheckTimer += GdxAI.getTimepiece().getDeltaTime();
        if (this.homeCheckTimer > this.homeCheckDelay) {
            this.homeCheckTimer = 0.0f;
            this.homeCheck(npc);
        }
    }

    protected void changeIfDiffState(Npc npc, NpcFriendState s) {
        if (s != npc.friendStateMachine.getCurrentState()) {
            npc.friendStateMachine.changeState(s);
        }
    }

    protected void followPlayer(Npc npc) {
        DialogNode dn = new DialogNode();
        int randResponse = MathUtils.random(1, 3);
        dn.message = Localization.getInstance().get("npc_startFollowing" + randResponse);
        DialogController.getInstance().displayNpcDialog(npc, dn, true);
        npc.friendStateMachine.changeState(FOLLOW_IDLE);
    }

    protected void stopFollowing(Npc npc) {
        DialogNode dn = new DialogNode();
        int randResponse = MathUtils.random(1, 3);
        dn.message = Localization.getInstance().get("npc_stopFollowing" + randResponse);
        DialogController.getInstance().displayNpcDialog(npc, dn, true);
        this.changeIfDiffState(npc, FRIEND_IDLE);
    }

    @Override
    public void enter(Npc npc) {
        npc.savedFriendStateMachineState = npc.friendStateMachine.getCurrentState();
        Gdx.app.log("MewnBase", "Changed NpcFriendState->" + npc.friendStateMachine.getCurrentState().toString());
    }

    public void interact(Npc npc, Player player) {
        Gdx.app.log("MewnBase", "NpcFriendState->" + npc.friendStateMachine.getCurrentState().toString() + "->interact");
    }

    @Override
    public void update(Npc npc) {
        npc.debugLabel.setText(npc.friendStateMachine.getCurrentState().name());
    }

    @Override
    public void exit(Npc npc) {
        npc.npcStatusIcon.hide();
    }

    @Override
    public boolean onMessage(Npc npc, Telegram telegram) {
        return false;
    }

    public void interactSecondary(Npc npc, Player player) {
        Gdx.app.log("MewnBase", "NpcFriendState->" + npc.friendStateMachine.getCurrentState().toString() + "->secondary interact");
        NpcFriendState state = npc.friendStateMachine.getCurrentState();
        if (state != FOLLOW_IDLE && state != FOLLOW_MOVING) {
            this.followPlayer(npc);
        }
        if (state == FOLLOW_IDLE || state == FOLLOW_MOVING) {
            this.stopFollowing(npc);
        }
    }
}

