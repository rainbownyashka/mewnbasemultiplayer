/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.dialog.DialogData;
import com.cairn4.moonbase.dialog.DialogNode;
import com.cairn4.moonbase.dialog.DialogSequence;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.NpcState;
import com.cairn4.moonbase.entities.Speaker;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.DialogBubble;
import com.cairn4.moonbase.ui.GameScreen;
import java.util.ArrayList;

public class DialogController {
    GameScreen gameScreen;
    DialogBubble dialogBubble;
    public ArrayList<DialogSequence> dialogSequences = new ArrayList();
    public Npc currentNpc;
    private static DialogController instance;

    public static DialogController getInstance() {
        if (instance == null) {
            instance = new DialogController();
        }
        return instance;
    }

    private DialogController() {
        this.loadData();
    }

    private void loadData() {
        if (this.dialogSequences.size() == 0) {
            FileHandle fileHandle = Gdx.files.local(MoonBase.coreFolder + "dialog.json");
            if (!fileHandle.exists()) {
                Gdx.app.error("MewnBase", "dialog.json does not exist");
            } else {
                Gdx.app.log("MewnBase", "DialogController: Reading data");
                Json json = new Json();
                String fileText = fileHandle.readString();
                this.dialogSequences = json.fromJson(DialogData.class, (String)fileText).dialogSequences;
                Gdx.app.log("MewnBase", "DialogController: List size: " + this.dialogSequences.size());
            }
        }
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    public void testNpcInteract(Npc npc) {
        this.createDialogBubble(npc, "Hello there!", true);
    }

    public DialogSequence getSequence(String npcName, NpcState npcState) {
        for (DialogSequence ds : this.dialogSequences) {
            if (!ds.npcId.equals(npcName) || npcState != ds.npcState) continue;
            return ds;
        }
        Gdx.app.error("MewnBase", "DialogController: couldn't find dialog for npc: " + npcName + " and state : " + npcState);
        return null;
    }

    public void displayNpcDialog(Npc npc, DialogNode node, boolean autoDestroy) {
        if (npc != null) {
            this.createDialogBubble(npc, node.message, autoDestroy);
        }
    }

    public void displayNpcDialog(Npc npc, DialogNode node) {
        if (npc != null) {
            this.createDialogBubble(npc, node.message, false);
        }
    }

    public void displayPlayerDialog(Npc npc, Player player, DialogNode node) {
        if (npc != null) {
            this.createDialogBubble(npc, node.message, false);
        }
        if (player != null) {
            this.createDialogBubble(player, node.message, false);
        }
    }

    private void createDialogBubble(Speaker speaker, String dialog, boolean autoDestroy) {
        if (this.dialogBubble != null) {
            this.removeDialogBubble();
        }
        if (!autoDestroy) {
            this.gameScreen.cameraLag.focusOnSpeaker(speaker);
        }
        this.dialogBubble = new DialogBubble((BaseScreen)this.gameScreen, this.gameScreen.worldUIStage, speaker, dialog);
        this.dialogBubble.autoDestroy = autoDestroy;
        this.gameScreen.worldUIStage.addActor(this.dialogBubble);
    }

    public void removeDialogBubble() {
        if (this.dialogBubble != null) {
            this.dialogBubble.remove();
        }
    }

    public void forward(Player player) {
        this.currentNpc.handleInteraction(player);
    }
}

