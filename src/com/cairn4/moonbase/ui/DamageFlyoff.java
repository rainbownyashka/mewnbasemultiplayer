/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.DestroyableUI;

public class DamageFlyoff
implements DestroyableUI {
    private static final float lifetime = 1.2f;
    private static final float driftUp = 100.0f;
    Label label;
    public boolean readyToRemove;

    public void play(BaseScreen baseScreen, Group parentGroup, float posX, float posY, String text, Color color) {
        this.readyToRemove = false;
        this.label = new Label((CharSequence)text, baseScreen.labelStyle);
        this.label.setColor(color);
        this.label.setFontScale(0.6f);
        this.label.setOrigin(4);
        this.label.setAlignment(1);
        this.label.setPosition(posX, posY, 4);
        this.label.setTouchable(Touchable.disabled);
        parentGroup.addActor(this.label);
        this.label.addAction(Actions.sequence((Action)Actions.scaleTo(0.25f, 0.25f), (Action)Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, 0.15f), (Action)Actions.moveBy(0.0f, 100.0f, 1.2f), (Action)Actions.sequence((Action)Actions.delay(0.2f), (Action)Actions.fadeOut(1.0f, Interpolation.circleOut))), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                DamageFlyoff.this.destroy();
            }
        })));
    }

    private void destroy() {
        this.label.remove();
        this.readyToRemove = true;
    }

    @Override
    public boolean isReadyToDestroy() {
        return this.readyToRemove;
    }
}

