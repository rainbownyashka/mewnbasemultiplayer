/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.cairn4.moonbase.ui.AutoScalingText;

public class AutoScaleTextButton
extends TextButton
implements AutoScalingText {
    private AutoScaleTextButtonStyle style;

    public AutoScaleTextButton(String text, AutoScaleTextButtonStyle style) {
        super(text, style);
        this.setStyle(style);
        this.style = style;
    }

    public void setStyle(AutoScaleTextButtonStyle style) {
        if (style == null) {
            throw new NullPointerException("style cannot be null");
        }
        if (!(style instanceof TextButton.TextButtonStyle)) {
            throw new IllegalArgumentException("style must be a TextButtonStyle.");
        }
        super.setStyle(style);
        this.style = style;
        if (this.getLabel() != null) {
            AutoScaleTextButtonStyle textButtonStyle = style;
            Label.LabelStyle labelStyle = this.getLabel().getStyle();
            labelStyle.font = ((TextButton.TextButtonStyle)textButtonStyle).font;
            labelStyle.fontColor = ((TextButton.TextButtonStyle)textButtonStyle).fontColor;
            this.getLabel().setStyle(labelStyle);
        }
    }

    @Override
    public AutoScaleTextButtonStyle getStyle() {
        return this.style;
    }

    @Override
    public void autoSize() {
        if (this.getLabel().getGlyphLayout().width > this.getLabel().getParent().getWidth() - this.getStyle().horzPadding) {
            float newScale = (this.getLabel().getParent().getWidth() - this.getStyle().horzPadding) / this.getLabel().getGlyphLayout().width;
            if (newScale > this.getStyle().maxScale) {
                newScale = this.getStyle().maxScale;
            } else if (newScale < this.getStyle().minScale) {
                newScale = this.getStyle().minScale;
            }
            this.getLabel().setFontScale(newScale);
        } else {
            this.getLabel().setFontScale(this.getStyle().maxScale);
        }
    }

    public static class AutoScaleTextButtonStyle
    extends TextButton.TextButtonStyle {
        public BitmapFont font;
        public Color fontColor;
        public Color downFontColor;
        public Color overFontColor;
        public Color checkedFontColor;
        public Color checkedOverFontColor;
        public Color disabledFontColor;
        public float horzPadding;
        public float minScale = 0.01f;
        public float maxScale = 1.0f;

        public AutoScaleTextButtonStyle() {
        }

        public AutoScaleTextButtonStyle(Drawable up, Drawable down, Drawable checked, BitmapFont font) {
            super(up, down, checked, font);
        }

        public AutoScaleTextButtonStyle(TextButton.TextButtonStyle style) {
            super(style);
            this.font = style.font;
            if (style.fontColor != null) {
                this.fontColor = new Color(style.fontColor);
            }
            if (style.downFontColor != null) {
                this.downFontColor = new Color(style.downFontColor);
            }
            if (style.overFontColor != null) {
                this.overFontColor = new Color(style.overFontColor);
            }
            if (style.checkedFontColor != null) {
                this.checkedFontColor = new Color(style.checkedFontColor);
            }
            if (style.checkedOverFontColor != null) {
                this.checkedOverFontColor = new Color(style.checkedOverFontColor);
            }
            if (style.disabledFontColor != null) {
                this.disabledFontColor = new Color(style.disabledFontColor);
            }
        }

        public AutoScaleTextButtonStyle(AutoScaleTextButtonStyle style) {
            super(style);
            this.font = style.font;
            if (style.fontColor != null) {
                this.fontColor = new Color(style.fontColor);
            }
            if (style.downFontColor != null) {
                this.downFontColor = new Color(style.downFontColor);
            }
            if (style.overFontColor != null) {
                this.overFontColor = new Color(style.overFontColor);
            }
            if (style.checkedFontColor != null) {
                this.checkedFontColor = new Color(style.checkedFontColor);
            }
            if (style.checkedOverFontColor != null) {
                this.checkedOverFontColor = new Color(style.checkedOverFontColor);
            }
            if (style.disabledFontColor != null) {
                this.disabledFontColor = new Color(style.disabledFontColor);
            }
            this.horzPadding = style.horzPadding;
            this.minScale = style.minScale;
            this.maxScale = style.maxScale;
        }
    }
}

