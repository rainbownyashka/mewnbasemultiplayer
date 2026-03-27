/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.dialog;

import com.badlogic.gdx.Gdx;
import com.cairn4.moonbase.dialog.DialogNode;
import com.cairn4.moonbase.entities.NpcState;
import java.util.ArrayList;

public class DialogSequence {
    public String npcId;
    public NpcState npcState;
    public ArrayList<DialogNode> messages = new ArrayList();
    public int currentNodeIndex = 0;

    public DialogNode getNode(int index) {
        if (index < this.messages.size() && index >= 0) {
            return this.messages.get(index);
        }
        Gdx.app.error("MewnBase", "DialogSequence index out of range: " + index + " / " + (this.messages.size() - 1));
        return null;
    }
}

