/*
 * Decompiled with CFR 0.152.
 */
package de.golfgl.gdx.controllers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.IControllerScrollable;

public class ControllerScrollPane
extends ScrollPane
implements IControllerScrollable {
    public ControllerScrollPane(Actor widget) {
        super(widget);
    }

    public ControllerScrollPane(Actor widget, Skin skin) {
        super(widget, skin);
    }

    public ControllerScrollPane(Actor widget, Skin skin, String styleName) {
        super(widget, skin, styleName);
    }

    public ControllerScrollPane(Actor widget, ScrollPane.ScrollPaneStyle style) {
        super(widget, style);
    }

    @Override
    public boolean onControllerScroll(ControllerMenuStage.MoveFocusDirection direction) {
        switch (direction) {
            case south: {
                if (!this.isScrollY() || this.getScrollY() >= this.getMaxY()) {
                    return false;
                }
                this.setScrollY(this.getScrollY() + this.getMouseWheelY() * this.getScrollAmount());
                return true;
            }
            case north: {
                if (!this.isScrollY() || this.getScrollY() <= 0.0f) {
                    return false;
                }
                this.setScrollY(this.getScrollY() - this.getMouseWheelY() * this.getScrollAmount());
                return true;
            }
            case west: {
                if (!this.isScrollX() || this.getScrollX() <= 0.0f) {
                    return false;
                }
                this.setScrollX(this.getScrollX() - this.getMouseWheelX() * this.getScrollAmount());
                return true;
            }
            case east: {
                if (!this.isScrollX() || this.getScrollX() >= this.getMaxX()) {
                    return false;
                }
                this.setScrollX(this.getScrollX() + this.getMouseWheelX() * this.getScrollAmount());
                return true;
            }
        }
        return false;
    }

    protected float getScrollAmount() {
        return 1.0f;
    }
}

