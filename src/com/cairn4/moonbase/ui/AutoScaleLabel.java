/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cairn4.moonbase.ui.AutoScalingText;

public class AutoScaleLabel
extends Label
implements AutoScalingText {
    private AutoScaleLabelStyle style;

    public AutoScaleLabel(CharSequence text, AutoScaleLabelStyle style) {
        super(text, style);
        this.setStyle(style);
        this.style = style;
    }

    @Override
    public void autoSize() {
        if (this.getGlyphLayout().width > this.getWidth() - this.getStyle().horzPadding) {
            float newScale = (this.getWidth() - this.getStyle().horzPadding) / this.getGlyphLayout().width;
            if (newScale > this.getStyle().maxScale) {
                newScale = this.getStyle().maxScale;
            } else if (newScale < this.getStyle().minScale) {
                newScale = this.getStyle().minScale;
            }
            this.setFontScale(newScale);
        } else {
            this.setFontScale(this.getStyle().maxScale);
        }
    }

    public void setStyle(AutoScaleLabelStyle style) {
        if (style == null) {
            throw new NullPointerException("style cannot be null");
        }
        if (!(style instanceof AutoScaleLabelStyle)) {
            throw new IllegalArgumentException("style must be a AutoScaleLabel.AutoScaleLabelStyle.");
        }
        super.setStyle(style);
    }

    @Override
    public AutoScaleLabelStyle getStyle() {
        return this.style;
    }

    public static class AutoScaleLabelStyle
    extends Label.LabelStyle {
        public BitmapFont font;
        public Color fontColor;
        public float horzPadding;
        public float minScale = 0.01f;
        public float maxScale = 1.0f;

        public AutoScaleLabelStyle() {
        }

        public AutoScaleLabelStyle(BitmapFont font, Color fontColor) {
            super(font, fontColor);
        }

        public AutoScaleLabelStyle(Label.LabelStyle style) {
            super(style);
            this.font = style.font;
            if (style.fontColor != null) {
                this.fontColor = new Color(style.fontColor);
            }
        }

        public AutoScaleLabelStyle(AutoScaleLabelStyle style) {
            super(style);
            this.font = style.font;
            if (style.fontColor != null) {
                this.fontColor = new Color(style.fontColor);
            }
            this.horzPadding = style.horzPadding;
            this.minScale = style.minScale;
            this.maxScale = style.maxScale;
        }
    }
}

