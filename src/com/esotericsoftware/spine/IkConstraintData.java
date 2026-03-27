/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.BoneData;
import com.esotericsoftware.spine.ConstraintData;

public class IkConstraintData
extends ConstraintData {
    final Array<BoneData> bones = new Array();
    BoneData target;
    int bendDirection = 1;
    boolean compress;
    boolean stretch;
    boolean uniform;
    float mix = 1.0f;
    float softness;

    public IkConstraintData(String name) {
        super(name);
    }

    public Array<BoneData> getBones() {
        return this.bones;
    }

    public BoneData getTarget() {
        return this.target;
    }

    public void setTarget(BoneData target) {
        if (target == null) {
            throw new IllegalArgumentException("target cannot be null.");
        }
        this.target = target;
    }

    public float getMix() {
        return this.mix;
    }

    public void setMix(float mix) {
        this.mix = mix;
    }

    public float getSoftness() {
        return this.softness;
    }

    public void setSoftness(float softness) {
        this.softness = softness;
    }

    public int getBendDirection() {
        return this.bendDirection;
    }

    public void setBendDirection(int bendDirection) {
        this.bendDirection = bendDirection;
    }

    public boolean getCompress() {
        return this.compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public boolean getStretch() {
        return this.stretch;
    }

    public void setStretch(boolean stretch) {
        this.stretch = stretch;
    }

    public boolean getUniform() {
        return this.uniform;
    }

    public void setUniform(boolean uniform) {
        this.uniform = uniform;
    }
}

