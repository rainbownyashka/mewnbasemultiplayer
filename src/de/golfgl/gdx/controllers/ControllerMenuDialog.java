/*
 * Decompiled with CFR 0.152.
 */
package de.golfgl.gdx.controllers;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import de.golfgl.gdx.controllers.ControllerMenuStage;

public class ControllerMenuDialog
extends Dialog {
    protected Array<Actor> buttonsToAdd = new Array();
    protected Actor previousFocusedActor;
    protected Actor previousEscapeActor;

    public ControllerMenuDialog(String title, Skin skin) {
        super(title, skin);
    }

    public ControllerMenuDialog(String title, Skin skin, String windowStyleName) {
        super(title, skin, windowStyleName);
    }

    public ControllerMenuDialog(String title, Window.WindowStyle windowStyle) {
        super(title, windowStyle);
    }

    @Override
    public Dialog button(Button button, Object object) {
        this.addFocusableActor(button);
        if (this.getStage() != null && this.getStage() instanceof ControllerMenuStage) {
            ((ControllerMenuStage)this.getStage()).addFocusableActor(button);
        }
        return super.button(button, object);
    }

    @Override
    protected void setStage(Stage stage) {
        if (stage == null && this.getStage() != null && this.getStage() instanceof ControllerMenuStage) {
            ((ControllerMenuStage)this.getStage()).removeFocusableActors(this.buttonsToAdd);
        }
        super.setStage(stage);
        if (stage != null && stage instanceof ControllerMenuStage) {
            ((ControllerMenuStage)stage).addFocusableActors(this.buttonsToAdd);
        }
    }

    @Override
    public Dialog show(Stage stage, Action action) {
        this.previousFocusedActor = null;
        this.previousEscapeActor = null;
        super.show(stage, action);
        if (stage instanceof ControllerMenuStage) {
            this.previousFocusedActor = ((ControllerMenuStage)stage).getFocusedActor();
            this.previousEscapeActor = ((ControllerMenuStage)stage).getEscapeActor();
            ((ControllerMenuStage)stage).setFocusedActor(this.getConfiguredDefaultActor());
            ((ControllerMenuStage)stage).setEscapeActor(this.getConfiguredEscapeActor());
        }
        return this;
    }

    protected Actor getConfiguredDefaultActor() {
        return this.buttonsToAdd.size >= 1 ? this.buttonsToAdd.get(0) : null;
    }

    protected Actor getConfiguredEscapeActor() {
        return this.buttonsToAdd.size == 1 ? this.buttonsToAdd.get(0) : null;
    }

    @Override
    public void hide(Action action) {
        if (this.getStage() != null && this.getStage() instanceof ControllerMenuStage) {
            Actor currentFocusedActor = ((ControllerMenuStage)this.getStage()).getFocusedActor();
            if (this.previousFocusedActor != null && this.previousFocusedActor.getStage() == this.getStage() && (currentFocusedActor == null || !currentFocusedActor.hasParent() || currentFocusedActor.isDescendantOf(this))) {
                ((ControllerMenuStage)this.getStage()).setFocusedActor(this.previousFocusedActor);
            }
            Actor currentEscapeActor = ((ControllerMenuStage)this.getStage()).getEscapeActor();
            if (this.previousEscapeActor != null && this.previousEscapeActor.getStage() == this.getStage() && (currentEscapeActor == null || !currentEscapeActor.hasParent() || currentEscapeActor.isDescendantOf(this))) {
                ((ControllerMenuStage)this.getStage()).setEscapeActor(this.previousEscapeActor);
            }
        }
        super.hide(action);
    }

    public void addFocusableActor(Actor actor) {
        this.buttonsToAdd.add(actor);
        if (this.getStage() != null && this.getStage() instanceof ControllerMenuStage) {
            ((ControllerMenuStage)this.getStage()).addFocusableActor(actor);
        }
    }
}

