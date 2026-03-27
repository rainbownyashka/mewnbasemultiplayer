/*
 * Decompiled with CFR 0.152.
 */
package de.golfgl.gdx.controllers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import de.golfgl.gdx.controllers.ControllerMenuStage;

public interface IControllerManageFocus {
    public Actor getNextFocusableActor(ControllerMenuStage.MoveFocusDirection var1);

    public Actor getNextFocusableActor(boolean var1);
}

