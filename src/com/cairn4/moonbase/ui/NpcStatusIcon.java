/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.cairn4.moonbase.entities.NpcNeeds;

public class NpcStatusIcon {
    private Skin skin;
    private Group group;
    private Image icon;
    private int currentNeed = 999999999;

    public NpcStatusIcon(Skin skin, Stage stage) {
        this.skin = skin;
        this.icon = new Image(skin.getDrawable("npc-notification"));
        this.icon.setOrigin(4);
        this.icon.setScale(0.3f);
        this.icon.setPosition(0.0f, 0.0f, 4);
        this.group = new Group();
        this.group.setVisible(false);
        this.group.setTouchable(Touchable.disabled);
        this.group.addActor(this.icon);
        stage.addActor(this.group);
    }

    public void changeIcon(int newNeed) {
        boolean alreadyVisible = this.group.isVisible();
        String drawable = "npc-notification";
        if (newNeed == NpcNeeds.hasQuest) {
            drawable = "npc-notification";
        } else if (newNeed == NpcNeeds.wantsHome) {
            drawable = "npc-nohome";
        } else if (newNeed == NpcNeeds.sadHome) {
            drawable = "npc-sadhome";
        } else if (newNeed == NpcNeeds.wantsFood) {
            drawable = "npc-notification";
        }
        this.icon.setDrawable(this.skin.getDrawable(drawable));
        this.icon.clearActions();
        this.icon.setOrigin(4);
        this.icon.setDrawable(this.skin.getDrawable(drawable));
        this.group.setVisible(true);
        if (!alreadyVisible) {
            this.group.setVisible(true);
        } else if (newNeed < this.currentNeed) {
            this.currentNeed = newNeed;
            this.icon.setDrawable(this.skin.getDrawable(drawable));
        }
        Gdx.app.log("MewnBase", "change icon");
        this.icon.addAction(Actions.sequence((Action)Actions.scaleTo(0.0f, 0.0f), (Action)Actions.alpha(0.0f), (Action)Actions.parallel((Action)Actions.fadeIn(0.25f), (Action)Actions.scaleTo(0.32f, 0.32f, 0.25f, Interpolation.sineOut)), (Action)Actions.forever(Actions.sequence((Action)Actions.scaleTo(0.3f, 0.3f, 0.5f, Interpolation.sine), (Action)Actions.scaleTo(0.32f, 0.32f, 0.5f, Interpolation.sine)))));
    }

    public void hide() {
        this.group.setVisible(false);
        this.icon.clearActions();
        this.currentNeed = 99999999;
    }

    public void update(float worldXPos, float worldYPos, float npcHeight) {
        this.group.setPosition(worldXPos, worldYPos + npcHeight - 30.0f, 4);
    }
}

