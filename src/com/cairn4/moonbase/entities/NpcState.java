/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.dialog.DialogController;
import com.cairn4.moonbase.dialog.DialogNode;
import com.cairn4.moonbase.dialog.DialogSequence;
import com.cairn4.moonbase.dialog.Quest;
import com.cairn4.moonbase.dialog.QuestController;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.NpcBonuses;
import com.cairn4.moonbase.entities.NpcFriendState;
import com.cairn4.moonbase.ui.HudNotificationData;
import com.cairn4.moonbase.ui.HudNotifications;

public enum NpcState implements State<Npc>
{
    PRESPAWN{}
    ,
    SPAWN{

        @Override
        public void update(Npc npc) {
            super.update(npc);
            if (npc.getCurrentChunk() != null) {
                npc.stateMachine.changeState(INIT_IDLE);
            }
        }
    }
    ,
    INIT_IDLE{

        @Override
        public void enter(Npc npc) {
            super.enter(npc);
            Quest firstQuest = QuestController.getInstance().getQuest(npc.npcId);
            if (firstQuest != null) {
                QuestController.getInstance().initQuest(firstQuest, npc);
                npc.setQuestNotification(true);
            }
        }

        @Override
        public void interact(Npc npc, Player player) {
            super.interact(npc, player);
            Gdx.app.log("MewnBase", "NpcState: Interact");
            npc.discovered = true;
            npc.stateMachine.changeState(INTRO_DIALOG);
        }
    }
    ,
    INTRO_DIALOG{

        @Override
        public void enter(Npc npc) {
            super.enter(npc);
            npc.setQuestNotification(false);
            MessageManager.getInstance().dispatchMessage(2, true);
            DialogController.getInstance().currentNpc = npc;
            this.dialogSequence = DialogController.getInstance().getSequence(npc.npcId, INTRO_DIALOG);
            this.dialogSequence.currentNodeIndex = 0;
            DialogNode node = this.dialogSequence.getNode(0);
            DialogController.getInstance().displayNpcDialog(npc, node);
            ++this.dialogSequence.currentNodeIndex;
        }

        @Override
        public void interact(Npc npc, Player player) {
            super.interact(npc, player);
            if (this.dialogSequence.currentNodeIndex >= this.dialogSequence.messages.size()) {
                npc.stateMachine.changeState(QUEST_ACTIVE);
                DialogController.getInstance().removeDialogBubble();
            } else {
                DialogNode node = this.dialogSequence.getNode(this.dialogSequence.currentNodeIndex);
                if (node.speaker.equals("player")) {
                    DialogController.getInstance().displayPlayerDialog(npc, player, node);
                } else {
                    DialogController.getInstance().displayNpcDialog(npc, node);
                }
                ++this.dialogSequence.currentNodeIndex;
            }
        }

        @Override
        public void exit(Npc npc) {
            DialogController.getInstance().currentNpc = null;
            MessageManager.getInstance().dispatchMessage(2, false);
        }
    }
    ,
    QUEST_ACTIVE{

        @Override
        public void enter(Npc npc) {
            super.enter(npc);
            npc.currentQuest = QuestController.getInstance().getQuest(npc.npcId);
            this.dialogSequence = null;
        }

        @Override
        public void interact(Npc npc, Player player) {
            super.interact(npc, player);
            Gdx.app.log("MewnBase", "Quest check here...");
            boolean questComplete = QuestController.getInstance().isQuestComplete(npc.currentQuest, player);
            if (questComplete) {
                QuestController.getInstance().handleQuestComplete(npc.currentQuest, player);
                npc.stateMachine.changeState(QUEST_COMPLETE);
            } else if (this.dialogSequence == null) {
                this.dialogSequence = DialogController.getInstance().getSequence(npc.npcId, QUEST_ACTIVE);
                this.dialogSequence.currentNodeIndex = 0;
                DialogNode node = this.dialogSequence.getNode(0);
                DialogController.getInstance().displayNpcDialog(npc, node, true);
                ++this.dialogSequence.currentNodeIndex;
            } else {
                DialogController.getInstance().removeDialogBubble();
                this.dialogSequence = null;
            }
        }
    }
    ,
    QUEST_COMPLETE{

        @Override
        public void enter(Npc npc) {
            super.enter(npc);
            DialogController.getInstance().currentNpc = npc;
            MessageManager.getInstance().dispatchMessage(2, true);
            this.dialogSequence = DialogController.getInstance().getSequence(npc.npcId, QUEST_COMPLETE);
            this.dialogSequence.currentNodeIndex = 0;
            DialogNode node = this.dialogSequence.getNode(0);
            DialogController.getInstance().displayNpcDialog(npc, node);
            ++this.dialogSequence.currentNodeIndex;
        }

        @Override
        public void interact(Npc npc, Player player) {
            if (this.dialogSequence.currentNodeIndex >= this.dialogSequence.messages.size()) {
                npc.stateMachine.changeState(FINISHED);
                npc.friendStateMachine.changeState(NpcFriendState.FRIEND_IDLE);
            } else {
                DialogNode node = this.dialogSequence.getNode(this.dialogSequence.currentNodeIndex);
                if (node.speaker.equals("player")) {
                    DialogController.getInstance().displayPlayerDialog(npc, player, node);
                } else {
                    DialogController.getInstance().displayNpcDialog(npc, node);
                }
                ++this.dialogSequence.currentNodeIndex;
            }
        }

        @Override
        public void exit(Npc npc) {
            MessageManager.getInstance().dispatchMessage(2, false);
            npc.dropLoot();
            Gdx.app.log("MewnBase", "Quest Complete!");
            HudNotificationData note = HudNotifications.hudNotificationDataPool.obtain();
            note.message = "Quest Complete!";
            MessageManager.getInstance().dispatchMessage(3, note);
            HudNotifications.hudNotificationDataPool.free(note);
            if (npc.currentQuest.rewardBonus) {
                npc.bonusEarned = true;
                NpcBonuses.getInstance().addBonus(npc.npcDef.rewardBonus);
                HudNotificationData note2 = HudNotifications.hudNotificationDataPool.obtain();
                note2.message = "Unlocked new Bonus: " + NpcBonuses.getInstance().getBonusName(npc.npcDef.rewardBonus);
                MessageManager.getInstance().dispatchMessage(3, note2);
                HudNotifications.hudNotificationDataPool.free(note2);
            }
            npc.currentQuest = null;
            DialogController.getInstance().currentNpc = null;
            DialogController.getInstance().removeDialogBubble();
        }
    }
    ,
    FINISHED{};

    DialogSequence dialogSequence;

    @Override
    public void enter(Npc npc) {
        npc.savedStateMachineState = npc.stateMachine.getCurrentState();
        Gdx.app.log("MewnBase", "Changed NpcState->" + npc.stateMachine.getCurrentState().toString());
    }

    public void interact(Npc npc, Player player) {
        Gdx.app.log("MewnBase", "NpcState->" + npc.stateMachine.getCurrentState().toString() + "->interact");
    }

    @Override
    public void update(Npc npc) {
        if (npc.friendStateMachine.getCurrentState() != NpcFriendState.NOT_FRIEND) {
            npc.debugLabel.setText(npc.stateMachine.getCurrentState().name());
        }
    }

    @Override
    public void exit(Npc npc) {
    }

    @Override
    public boolean onMessage(Npc npc, Telegram telegram) {
        return false;
    }
}

