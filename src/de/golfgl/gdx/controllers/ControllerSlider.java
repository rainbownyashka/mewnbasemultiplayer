/*
 * Decompiled with CFR 0.152.
 */
package de.golfgl.gdx.controllers;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import de.golfgl.gdx.controllers.ControllerMenuStage;
import de.golfgl.gdx.controllers.IControllerActable;
import de.golfgl.gdx.controllers.IControllerScrollable;

public class ControllerSlider
extends Slider
implements IControllerActable,
IControllerScrollable {
    protected boolean isVertical;

    public ControllerSlider(float min, float max, float stepSize, boolean vertical, Skin skin) {
        super(min, max, stepSize, vertical, skin);
        this.isVertical = vertical;
    }

    public ControllerSlider(float min, float max, float stepSize, boolean vertical, Skin skin, String styleName) {
        super(min, max, stepSize, vertical, skin, styleName);
        this.isVertical = vertical;
    }

    public ControllerSlider(float min, float max, float stepSize, boolean vertical, Slider.SliderStyle style) {
        super(min, max, stepSize, vertical, style);
        this.isVertical = vertical;
    }

    @Override
    public boolean onControllerDefaultKeyDown() {
        return true;
    }

    @Override
    public boolean onControllerScroll(ControllerMenuStage.MoveFocusDirection direction) {
        if (this.isDisabled()) {
            return false;
        }
        switch (direction) {
            case north: {
                if (!this.isVertical) {
                    return false;
                }
                return this.setValue(this.getValue() - this.getControllerScrollStepSize());
            }
            case south: {
                if (!this.isVertical) {
                    return false;
                }
                return this.setValue(this.getValue() + this.getControllerScrollStepSize());
            }
            case east: {
                if (this.isVertical) {
                    return false;
                }
                return this.setValue(this.getValue() + this.getControllerScrollStepSize());
            }
            case west: {
                if (this.isVertical) {
                    return false;
                }
                return this.setValue(this.getValue() - this.getControllerScrollStepSize());
            }
        }
        return false;
    }

    @Override
    public boolean onControllerDefaultKeyUp() {
        return true;
    }

    protected float getControllerScrollStepSize() {
        return this.getStepSize();
    }
}

