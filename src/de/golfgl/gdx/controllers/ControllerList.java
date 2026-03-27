/*
 * Decompiled with CFR 0.152.
 */
package de.golfgl.gdx.controllers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.IControllerActable;
import de.golfgl.gdx.controllers.IControllerScrollable;

public class ControllerList<T>
extends List<T>
implements IControllerActable,
IControllerScrollable {
    public ControllerList(Skin skin) {
        super(skin);
        this.setup();
    }

    public ControllerList(Skin skin, String styleName) {
        super(skin, styleName);
        this.setup();
    }

    public ControllerList(List.ListStyle style) {
        super(style);
        this.setup();
    }

    protected void setup() {
        this.addListener(new InputListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (ControllerList.this.getSelectedIndex() < 0 && ControllerList.this.getItems().size > 0) {
                    ControllerList.this.setSelectedIndex(0);
                }
            }
        });
    }

    @Override
    public boolean onControllerDefaultKeyDown() {
        return true;
    }

    @Override
    public boolean onControllerDefaultKeyUp() {
        return true;
    }

    @Override
    public boolean onControllerScroll(ControllerMenuStage.MoveFocusDirection direction) {
        if (!this.isTouchable()) {
            return false;
        }
        switch (direction) {
            case north: {
                if (this.getSelectedIndex() > 0) {
                    this.setSelectedIndex(this.getSelectedIndex() - 1);
                    return true;
                }
                return false;
            }
            case south: {
                if (this.getSelectedIndex() < this.getItems().size - 1) {
                    this.setSelectedIndex(this.getSelectedIndex() + 1);
                    return true;
                }
                return false;
            }
            case east: 
            case west: {
                return false;
            }
        }
        return false;
    }
}

