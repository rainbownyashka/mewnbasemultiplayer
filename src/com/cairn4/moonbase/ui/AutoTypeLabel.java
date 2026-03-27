/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cairn4.moonbase.Audio;

public class AutoTypeLabel
extends Label {
    float timer;
    float delay = 0.013f;
    int index;
    CharSequence origText;
    CharSequence inputChars;
    CharSequence outputChars;
    public String sound;
    private float soundPitch;
    public float soundBasePitch = 0.9f;
    public float soundPitchVar = 0.115f;
    public float soundVolume = 0.032f;

    public AutoTypeLabel(String text, Label.LabelStyle labelStyle) {
        super((CharSequence)text, labelStyle);
        this.changeText(text);
    }

    public void changeText(CharSequence text) {
        this.inputChars = this.origText = text;
        this.outputChars = "";
        this.setText(this.outputChars);
    }

    public void setFinalText(CharSequence text) {
        this.origText = text;
        this.inputChars = text;
        this.outputChars = text;
        if (this.index > this.inputChars.length()) {
            this.index = this.inputChars.length();
        }
        this.outputChars = this.inputChars.subSequence(0, this.index);
        this.setText(this.outputChars);
    }

    public void animate() {
        this.index = 0;
        this.timer = 0.0f;
    }

    public void animate(float speed) {
        this.delay = speed;
        this.animate();
    }

    // Returns the current width of the rendered text using a GlyphLayout. Useful for positioning adjacent labels.
    public float getRenderedWidth() {
        try {
            com.badlogic.gdx.graphics.g2d.GlyphLayout gl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(this.getStyle().font, this.getText());
            return gl.width;
        } catch (Exception e) {
            return this.getPrefWidth();
        }
    }

    @Override
    public void act(float delta) {
        if (this.index <= this.origText.length()) {
            this.timer += delta;
            if (this.timer >= this.delay) {
                this.timer = 0.0f;
                this.outputChars = this.inputChars.subSequence(0, this.index);
                this.setText(this.outputChars);
                this.playSoundFx();
                ++this.index;
            }
        }
        super.act(delta);
    }

    private void playSoundFx() {
        if (this.sound != null) {
            this.soundPitch = MathUtils.random(this.soundBasePitch - this.soundPitchVar, this.soundBasePitch + this.soundPitchVar);
            Audio.getInstance().playSound(this.sound, this.soundVolume, this.soundPitch);
        }
    }
}

