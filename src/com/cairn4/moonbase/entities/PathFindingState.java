/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.GridPoint2;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.NpcFriendState;

public enum PathFindingState implements State<Npc>
{
    INIT{

        @Override
        public void enter(Npc npc) {
        }
    }
    ,
    PATHING_TO_STEP{

        @Override
        public void update(Npc npc) {
            super.update(npc);
        }
    }
    ,
    ARRIVED_AT_STEP{

        @Override
        public void enter(Npc npc) {
        }
    }
    ,
    WAITING_AT_STEP{

        @Override
        public void update(Npc npc) {
            super.update(npc);
        }
    }
    ,
    REACHED_DESTINATINO{

        @Override
        public void update(Npc npc) {
            super.update(npc);
        }
    };

    public StateMachine<Npc, NpcFriendState> friendMachine;
    public GridPoint2 finalDestination = new GridPoint2(0, 0);

    protected void changeState(PathFindingState newState) {
    }

    @Override
    public void enter(Npc entity) {
    }

    @Override
    public void update(Npc entity) {
    }

    @Override
    public void exit(Npc entity) {
    }

    @Override
    public boolean onMessage(Npc entity, Telegram telegram) {
        return false;
    }
}

