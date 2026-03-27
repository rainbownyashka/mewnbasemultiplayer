/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class CameraShake {
    OrthographicCamera camera;
    Vector3 target;
    Vector3 offset;
    float startMagnitude;
    float magnitude;
    float frequency;
    float duration;
    float timer;
    boolean shaking;
    private boolean endless;

    public CameraShake(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void endlessShake(float mag, float freq) {
        this.startMagnitude = mag;
        this.magnitude = mag;
        this.frequency = freq;
        this.endless = true;
        this.timer = 0.0f;
        this.shaking = true;
        this.offset = new Vector3(0.0f, 0.0f, 0.0f);
    }

    public void shake(float mag, float freq, float dur) {
        this.startMagnitude = mag;
        this.magnitude = mag;
        this.frequency = freq;
        this.duration = dur;
        this.endless = false;
        this.timer = 0.0f;
        this.shaking = true;
        this.offset = new Vector3(0.0f, 0.0f, 0.0f);
    }

    public void update(float delta) {
        if (this.shaking) {
            this.timer += delta;
            if (this.endless) {
                this.updateMagnitude(delta);
                this.moveCamera(delta);
            } else if (this.timer >= this.duration) {
                this.shaking = false;
            } else {
                this.updateMagnitude(delta);
                this.moveCamera(delta);
            }
        }
    }

    private void updateMagnitude(float delta) {
        this.magnitude = this.endless ? this.startMagnitude * MathUtils.random(0.5f, 2.0f) : this.startMagnitude - this.timer * this.startMagnitude / this.duration;
    }

    private void moveCamera(float delta) {
        this.offset.y = this.magnitude * (float)Math.sin(this.frequency * this.timer);
        this.camera.position.add(this.offset);
    }
}

