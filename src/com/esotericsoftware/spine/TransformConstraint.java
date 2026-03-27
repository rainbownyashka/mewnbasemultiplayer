/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.BoneData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.TransformConstraintData;
import com.esotericsoftware.spine.Updatable;
import com.esotericsoftware.spine.utils.SpineUtils;

public class TransformConstraint
implements Updatable {
    final TransformConstraintData data;
    final Array<Bone> bones;
    Bone target;
    float rotateMix;
    float translateMix;
    float scaleMix;
    float shearMix;
    boolean active;
    final Vector2 temp = new Vector2();

    public TransformConstraint(TransformConstraintData data, Skeleton skeleton) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null.");
        }
        if (skeleton == null) {
            throw new IllegalArgumentException("skeleton cannot be null.");
        }
        this.data = data;
        this.rotateMix = data.rotateMix;
        this.translateMix = data.translateMix;
        this.scaleMix = data.scaleMix;
        this.shearMix = data.shearMix;
        this.bones = new Array(data.bones.size);
        for (BoneData boneData : data.bones) {
            this.bones.add(skeleton.findBone(boneData.name));
        }
        this.target = skeleton.findBone(data.target.name);
    }

    public TransformConstraint(TransformConstraint constraint, Skeleton skeleton) {
        if (constraint == null) {
            throw new IllegalArgumentException("constraint cannot be null.");
        }
        if (skeleton == null) {
            throw new IllegalArgumentException("skeleton cannot be null.");
        }
        this.data = constraint.data;
        this.bones = new Array(constraint.bones.size);
        for (Bone bone : constraint.bones) {
            this.bones.add(skeleton.bones.get(bone.data.index));
        }
        this.target = skeleton.bones.get(constraint.target.data.index);
        this.rotateMix = constraint.rotateMix;
        this.translateMix = constraint.translateMix;
        this.scaleMix = constraint.scaleMix;
        this.shearMix = constraint.shearMix;
    }

    public void apply() {
        this.update();
    }

    @Override
    public void update() {
        if (this.data.local) {
            if (this.data.relative) {
                this.applyRelativeLocal();
            } else {
                this.applyAbsoluteLocal();
            }
        } else if (this.data.relative) {
            this.applyRelativeWorld();
        } else {
            this.applyAbsoluteWorld();
        }
    }

    private void applyAbsoluteWorld() {
        float rotateMix = this.rotateMix;
        float translateMix = this.translateMix;
        float scaleMix = this.scaleMix;
        float shearMix = this.shearMix;
        Bone target = this.target;
        float ta = target.a;
        float td = target.d;
        float tb = target.b;
        float tc = target.c;
        float degRadReflect = ta * td - tb * tc > 0.0f ? (float)Math.PI / 180 : (float)(-Math.PI) / 180;
        float offsetRotation = this.data.offsetRotation * degRadReflect;
        float offsetShearY = this.data.offsetShearY * degRadReflect;
        Array<Bone> bones = this.bones;
        int n = bones.size;
        for (int i = 0; i < n; ++i) {
            Bone bone = bones.get(i);
            boolean modified = false;
            if (rotateMix != 0.0f) {
                float a = bone.a;
                float b = bone.b;
                float c = bone.c;
                float d = bone.d;
                float r = SpineUtils.atan2(tc, ta) - SpineUtils.atan2(c, a) + offsetRotation;
                if (r > (float)Math.PI) {
                    r -= (float)Math.PI * 2;
                } else if (r < (float)(-Math.PI)) {
                    r += (float)Math.PI * 2;
                }
                float cos = SpineUtils.cos(r *= rotateMix);
                float sin = SpineUtils.sin(r);
                bone.a = cos * a - sin * c;
                bone.b = cos * b - sin * d;
                bone.c = sin * a + cos * c;
                bone.d = sin * b + cos * d;
                modified = true;
            }
            if (translateMix != 0.0f) {
                Vector2 temp = this.temp;
                target.localToWorld(temp.set(this.data.offsetX, this.data.offsetY));
                bone.worldX += (temp.x - bone.worldX) * translateMix;
                bone.worldY += (temp.y - bone.worldY) * translateMix;
                modified = true;
            }
            if (scaleMix > 0.0f) {
                float s = (float)Math.sqrt(bone.a * bone.a + bone.c * bone.c);
                if (s != 0.0f) {
                    s = (s + ((float)Math.sqrt(ta * ta + tc * tc) - s + this.data.offsetScaleX) * scaleMix) / s;
                }
                bone.a *= s;
                bone.c *= s;
                s = (float)Math.sqrt(bone.b * bone.b + bone.d * bone.d);
                if (s != 0.0f) {
                    s = (s + ((float)Math.sqrt(tb * tb + td * td) - s + this.data.offsetScaleY) * scaleMix) / s;
                }
                bone.b *= s;
                bone.d *= s;
                modified = true;
            }
            if (shearMix > 0.0f) {
                float b = bone.b;
                float d = bone.d;
                float by = SpineUtils.atan2(d, b);
                float r = SpineUtils.atan2(td, tb) - SpineUtils.atan2(tc, ta) - (by - SpineUtils.atan2(bone.c, bone.a));
                if (r > (float)Math.PI) {
                    r -= (float)Math.PI * 2;
                } else if (r < (float)(-Math.PI)) {
                    r += (float)Math.PI * 2;
                }
                r = by + (r + offsetShearY) * shearMix;
                float s = (float)Math.sqrt(b * b + d * d);
                bone.b = SpineUtils.cos(r) * s;
                bone.d = SpineUtils.sin(r) * s;
                modified = true;
            }
            if (!modified) continue;
            bone.appliedValid = false;
        }
    }

    private void applyRelativeWorld() {
        float rotateMix = this.rotateMix;
        float translateMix = this.translateMix;
        float scaleMix = this.scaleMix;
        float shearMix = this.shearMix;
        Bone target = this.target;
        float ta = target.a;
        float td = target.d;
        float tb = target.b;
        float tc = target.c;
        float degRadReflect = ta * td - tb * tc > 0.0f ? (float)Math.PI / 180 : (float)(-Math.PI) / 180;
        float offsetRotation = this.data.offsetRotation * degRadReflect;
        float offsetShearY = this.data.offsetShearY * degRadReflect;
        Array<Bone> bones = this.bones;
        int n = bones.size;
        for (int i = 0; i < n; ++i) {
            float b;
            Bone bone = bones.get(i);
            boolean modified = false;
            if (rotateMix != 0.0f) {
                float a = bone.a;
                b = bone.b;
                float c = bone.c;
                float d = bone.d;
                float r = SpineUtils.atan2(tc, ta) + offsetRotation;
                if (r > (float)Math.PI) {
                    r -= (float)Math.PI * 2;
                } else if (r < (float)(-Math.PI)) {
                    r += (float)Math.PI * 2;
                }
                float cos = SpineUtils.cos(r *= rotateMix);
                float sin = SpineUtils.sin(r);
                bone.a = cos * a - sin * c;
                bone.b = cos * b - sin * d;
                bone.c = sin * a + cos * c;
                bone.d = sin * b + cos * d;
                modified = true;
            }
            if (translateMix != 0.0f) {
                Vector2 temp = this.temp;
                target.localToWorld(temp.set(this.data.offsetX, this.data.offsetY));
                bone.worldX += temp.x * translateMix;
                bone.worldY += temp.y * translateMix;
                modified = true;
            }
            if (scaleMix > 0.0f) {
                float s = ((float)Math.sqrt(ta * ta + tc * tc) - 1.0f + this.data.offsetScaleX) * scaleMix + 1.0f;
                bone.a *= s;
                bone.c *= s;
                s = ((float)Math.sqrt(tb * tb + td * td) - 1.0f + this.data.offsetScaleY) * scaleMix + 1.0f;
                bone.b *= s;
                bone.d *= s;
                modified = true;
            }
            if (shearMix > 0.0f) {
                float r = SpineUtils.atan2(td, tb) - SpineUtils.atan2(tc, ta);
                if (r > (float)Math.PI) {
                    r -= (float)Math.PI * 2;
                } else if (r < (float)(-Math.PI)) {
                    r += (float)Math.PI * 2;
                }
                b = bone.b;
                float d = bone.d;
                r = SpineUtils.atan2(d, b) + (r - 1.5707964f + offsetShearY) * shearMix;
                float s = (float)Math.sqrt(b * b + d * d);
                bone.b = SpineUtils.cos(r) * s;
                bone.d = SpineUtils.sin(r) * s;
                modified = true;
            }
            if (!modified) continue;
            bone.appliedValid = false;
        }
    }

    private void applyAbsoluteLocal() {
        float rotateMix = this.rotateMix;
        float translateMix = this.translateMix;
        float scaleMix = this.scaleMix;
        float shearMix = this.shearMix;
        Bone target = this.target;
        if (!target.appliedValid) {
            target.updateAppliedTransform();
        }
        Array<Bone> bones = this.bones;
        int n = bones.size;
        for (int i = 0; i < n; ++i) {
            Bone bone = bones.get(i);
            if (!bone.appliedValid) {
                bone.updateAppliedTransform();
            }
            float rotation = bone.arotation;
            if (rotateMix != 0.0f) {
                float r = target.arotation - rotation + this.data.offsetRotation;
                r -= (float)((16384 - (int)(16384.499999999996 - (double)(r / 360.0f))) * 360);
                rotation += r * rotateMix;
            }
            float x = bone.ax;
            float y = bone.ay;
            if (translateMix != 0.0f) {
                x += (target.ax - x + this.data.offsetX) * translateMix;
                y += (target.ay - y + this.data.offsetY) * translateMix;
            }
            float scaleX = bone.ascaleX;
            float scaleY = bone.ascaleY;
            if (scaleMix != 0.0f) {
                if (scaleX != 0.0f) {
                    scaleX = (scaleX + (target.ascaleX - scaleX + this.data.offsetScaleX) * scaleMix) / scaleX;
                }
                if (scaleY != 0.0f) {
                    scaleY = (scaleY + (target.ascaleY - scaleY + this.data.offsetScaleY) * scaleMix) / scaleY;
                }
            }
            float shearY = bone.ashearY;
            if (shearMix != 0.0f) {
                float r = target.ashearY - shearY + this.data.offsetShearY;
                r -= (float)((16384 - (int)(16384.499999999996 - (double)(r / 360.0f))) * 360);
                shearY += r * shearMix;
            }
            bone.updateWorldTransform(x, y, rotation, scaleX, scaleY, bone.ashearX, shearY);
        }
    }

    private void applyRelativeLocal() {
        float rotateMix = this.rotateMix;
        float translateMix = this.translateMix;
        float scaleMix = this.scaleMix;
        float shearMix = this.shearMix;
        Bone target = this.target;
        if (!target.appliedValid) {
            target.updateAppliedTransform();
        }
        Array<Bone> bones = this.bones;
        int n = bones.size;
        for (int i = 0; i < n; ++i) {
            Bone bone = bones.get(i);
            if (!bone.appliedValid) {
                bone.updateAppliedTransform();
            }
            float rotation = bone.arotation;
            if (rotateMix != 0.0f) {
                rotation += (target.arotation + this.data.offsetRotation) * rotateMix;
            }
            float x = bone.ax;
            float y = bone.ay;
            if (translateMix != 0.0f) {
                x += (target.ax + this.data.offsetX) * translateMix;
                y += (target.ay + this.data.offsetY) * translateMix;
            }
            float scaleX = bone.ascaleX;
            float scaleY = bone.ascaleY;
            if (scaleMix != 0.0f) {
                scaleX *= (target.ascaleX - 1.0f + this.data.offsetScaleX) * scaleMix + 1.0f;
                scaleY *= (target.ascaleY - 1.0f + this.data.offsetScaleY) * scaleMix + 1.0f;
            }
            float shearY = bone.ashearY;
            if (shearMix != 0.0f) {
                shearY += (target.ashearY + this.data.offsetShearY) * shearMix;
            }
            bone.updateWorldTransform(x, y, rotation, scaleX, scaleY, bone.ashearX, shearY);
        }
    }

    public Array<Bone> getBones() {
        return this.bones;
    }

    public Bone getTarget() {
        return this.target;
    }

    public void setTarget(Bone target) {
        if (target == null) {
            throw new IllegalArgumentException("target cannot be null.");
        }
        this.target = target;
    }

    public float getRotateMix() {
        return this.rotateMix;
    }

    public void setRotateMix(float rotateMix) {
        this.rotateMix = rotateMix;
    }

    public float getTranslateMix() {
        return this.translateMix;
    }

    public void setTranslateMix(float translateMix) {
        this.translateMix = translateMix;
    }

    public float getScaleMix() {
        return this.scaleMix;
    }

    public void setScaleMix(float scaleMix) {
        this.scaleMix = scaleMix;
    }

    public float getShearMix() {
        return this.shearMix;
    }

    public void setShearMix(float shearMix) {
        this.shearMix = shearMix;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    public TransformConstraintData getData() {
        return this.data;
    }

    public String toString() {
        return this.data.name;
    }
}

