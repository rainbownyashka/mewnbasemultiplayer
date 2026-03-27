/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.BoneData;
import com.esotericsoftware.spine.IkConstraintData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Updatable;
import com.esotericsoftware.spine.utils.SpineUtils;

public class IkConstraint
implements Updatable {
    final IkConstraintData data;
    final Array<Bone> bones;
    Bone target;
    int bendDirection;
    boolean compress;
    boolean stretch;
    float mix = 1.0f;
    float softness;
    boolean active;

    public IkConstraint(IkConstraintData data, Skeleton skeleton) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null.");
        }
        if (skeleton == null) {
            throw new IllegalArgumentException("skeleton cannot be null.");
        }
        this.data = data;
        this.mix = data.mix;
        this.softness = data.softness;
        this.bendDirection = data.bendDirection;
        this.compress = data.compress;
        this.stretch = data.stretch;
        this.bones = new Array(data.bones.size);
        for (BoneData boneData : data.bones) {
            this.bones.add(skeleton.findBone(boneData.name));
        }
        this.target = skeleton.findBone(data.target.name);
    }

    public IkConstraint(IkConstraint constraint, Skeleton skeleton) {
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
        this.mix = constraint.mix;
        this.softness = constraint.softness;
        this.bendDirection = constraint.bendDirection;
        this.compress = constraint.compress;
        this.stretch = constraint.stretch;
    }

    public void apply() {
        this.update();
    }

    @Override
    public void update() {
        Bone target = this.target;
        Array<Bone> bones = this.bones;
        switch (bones.size) {
            case 1: {
                IkConstraint.apply(bones.first(), target.worldX, target.worldY, this.compress, this.stretch, this.data.uniform, this.mix);
                break;
            }
            case 2: {
                IkConstraint.apply(bones.first(), bones.get(1), target.worldX, target.worldY, this.bendDirection, this.stretch, this.softness, this.mix);
            }
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

    @Override
    public boolean isActive() {
        return this.active;
    }

    public IkConstraintData getData() {
        return this.data;
    }

    public String toString() {
        return this.data.name;
    }

    public static void apply(Bone bone, float targetX, float targetY, boolean compress, boolean stretch, boolean uniform, float alpha) {
        if (bone == null) {
            throw new IllegalArgumentException("bone cannot be null.");
        }
        if (!bone.appliedValid) {
            bone.updateAppliedTransform();
        }
        Bone p = bone.parent;
        float id = 1.0f / (p.a * p.d - p.b * p.c);
        float x = targetX - p.worldX;
        float y = targetY - p.worldY;
        float tx = (x * p.d - y * p.b) * id - bone.ax;
        float ty = (y * p.a - x * p.c) * id - bone.ay;
        float rotationIK = SpineUtils.atan2(ty, tx) * 57.295776f - bone.ashearX - bone.arotation;
        if (bone.ascaleX < 0.0f) {
            rotationIK += 180.0f;
        }
        if (rotationIK > 180.0f) {
            rotationIK -= 360.0f;
        } else if (rotationIK < -180.0f) {
            rotationIK += 360.0f;
        }
        float sx = bone.ascaleX;
        float sy = bone.ascaleY;
        if (compress || stretch) {
            float b = bone.data.length * sx;
            float dd = (float)Math.sqrt(tx * tx + ty * ty);
            if (compress && dd < b || stretch && dd > b && b > 1.0E-4f) {
                float s = (dd / b - 1.0f) * alpha + 1.0f;
                sx *= s;
                if (uniform) {
                    sy *= s;
                }
            }
        }
        bone.updateWorldTransform(bone.ax, bone.ay, bone.arotation + rotationIK * alpha, sx, sy, bone.ashearX, bone.ashearY);
    }

    /*
     * Unable to fully structure code
     */
    public static void apply(Bone parent, Bone child, float targetX, float targetY, int bendDir, boolean stretch, float softness, float alpha) {
        block34: {
            block33: {
                if (parent == null) {
                    throw new IllegalArgumentException("parent cannot be null.");
                }
                if (child == null) {
                    throw new IllegalArgumentException("child cannot be null.");
                }
                if (alpha == 0.0f) {
                    child.updateWorldTransform();
                    return;
                }
                if (!parent.appliedValid) {
                    parent.updateAppliedTransform();
                }
                if (!child.appliedValid) {
                    child.updateAppliedTransform();
                }
                px = parent.ax;
                py = parent.ay;
                sx = psx = parent.ascaleX;
                psy = parent.ascaleY;
                csx = child.ascaleX;
                if (psx < 0.0f) {
                    psx = -psx;
                    os1 = 180;
                    s2 = -1;
                } else {
                    os1 = 0;
                    s2 = 1;
                }
                if (psy < 0.0f) {
                    psy = -psy;
                    s2 = -s2;
                }
                if (csx < 0.0f) {
                    csx = -csx;
                    os2 = 180;
                } else {
                    os2 = 0;
                }
                cx = child.ax;
                a = parent.a;
                b = parent.b;
                c = parent.c;
                d = parent.d;
                v0 = u = Math.abs(psx - psy) <= 1.0E-4f;
                if (!u) {
                    cy = 0.0f;
                    cwx = a * cx + parent.worldX;
                    cwy = c * cx + parent.worldY;
                } else {
                    cy = child.ay;
                    cwx = a * cx + b * cy + parent.worldX;
                    cwy = c * cx + d * cy + parent.worldY;
                }
                pp = parent.parent;
                a = pp.a;
                b = pp.b;
                c = pp.c;
                d = pp.d;
                id = 1.0f / (a * d - b * c);
                x = cwx - pp.worldX;
                y = cwy - pp.worldY;
                dx = (x * d - y * b) * id - px;
                dy = (y * a - x * c) * id - py;
                l1 = (float)Math.sqrt(dx * dx + dy * dy);
                l2 = child.data.length * csx;
                if (l1 < 1.0E-4f) {
                    IkConstraint.apply(parent, targetX, targetY, false, stretch, false, alpha);
                    child.updateWorldTransform(cx, cy, 0.0f, child.ascaleX, child.ascaleY, child.ashearX, child.ashearY);
                    return;
                }
                x = targetX - pp.worldX;
                y = targetY - pp.worldY;
                tx = (x * d - y * b) * id - px;
                ty = (y * a - x * c) * id - py;
                dd = tx * tx + ty * ty;
                if (softness != 0.0f) {
                    softness *= psx * (csx + 1.0f) / 2.0f;
                    td = (float)Math.sqrt(dd);
                    sd = td - l1 - l2 * psx + softness;
                    if (sd > 0.0f) {
                        p = Math.min(1.0f, sd / (softness * 2.0f)) - 1.0f;
                        p = (sd - softness * (1.0f - p * p)) / td;
                        tx -= p * tx;
                        ty -= p * ty;
                        dd = tx * tx + ty * ty;
                    }
                }
                if (!u) break block33;
                if ((cos = (dd - l1 * l1 - (l2 *= psx) * l2) / (2.0f * l1 * l2)) < -1.0f) {
                    cos = -1.0f;
                } else if (cos > 1.0f) {
                    cos = 1.0f;
                    if (stretch) {
                        sx *= ((float)Math.sqrt(dd) / (l1 + l2) - 1.0f) * alpha + 1.0f;
                    }
                }
                a2 = (float)Math.acos(cos) * (float)bendDir;
                a = l1 + l2 * cos;
                b = l2 * SpineUtils.sin(a2);
                a1 = SpineUtils.atan2(ty * a - tx * b, tx * a + ty * b);
                break block34;
            }
            a = psx * l2;
            b = psy * l2;
            aa = a * a;
            bb = b * b;
            ta = SpineUtils.atan2(ty, tx);
            c1 = -2.0f * bb * l1;
            c2 = bb - aa;
            c = bb * l1 * l1 + aa * dd - aa * bb;
            d = c1 * c1 - 4.0f * c2 * c;
            if (!(d >= 0.0f)) ** GOTO lbl-1000
            q = (float)Math.sqrt(d);
            if (c1 < 0.0f) {
                q = -q;
            }
            q = -(c1 + q) / 2.0f;
            r0 = q / c2;
            r1 = c / q;
            v1 = r = Math.abs(r0) < Math.abs(r1) ? r0 : r1;
            if (r * r <= dd) {
                y = (float)Math.sqrt(dd - r * r) * (float)bendDir;
                a1 = ta - SpineUtils.atan2(y, r);
                a2 = SpineUtils.atan2(y / psy, (r - l1) / psx);
            } else lbl-1000:
            // 2 sources

            {
                minAngle = 3.1415927f;
                minX = l1 - a;
                minDist = minX * minX;
                minY = 0.0f;
                maxAngle = 0.0f;
                maxX = l1 + a;
                maxDist = maxX * maxX;
                maxY = 0.0f;
                c = -a * l1 / (aa - bb);
                if (c >= -1.0f && c <= 1.0f) {
                    x = a * SpineUtils.cos(c = (float)Math.acos(c)) + l1;
                    d = x * x + (y = b * SpineUtils.sin(c)) * y;
                    if (d < minDist) {
                        minAngle = c;
                        minDist = d;
                        minX = x;
                        minY = y;
                    }
                    if (d > maxDist) {
                        maxAngle = c;
                        maxDist = d;
                        maxX = x;
                        maxY = y;
                    }
                }
                if (dd <= (minDist + maxDist) / 2.0f) {
                    a1 = ta - SpineUtils.atan2(minY * (float)bendDir, minX);
                    a2 = minAngle * (float)bendDir;
                } else {
                    a1 = ta - SpineUtils.atan2(maxY * (float)bendDir, maxX);
                    a2 = maxAngle * (float)bendDir;
                }
            }
        }
        os = SpineUtils.atan2(cy, cx) * (float)s2;
        rotation = parent.arotation;
        a1 = (a1 - os) * 57.295776f + (float)os1 - rotation;
        if (a1 > 180.0f) {
            a1 -= 360.0f;
        } else if (a1 < -180.0f) {
            a1 += 360.0f;
        }
        parent.updateWorldTransform(px, py, rotation + a1 * alpha, sx, parent.ascaleY, 0.0f, 0.0f);
        rotation = child.arotation;
        a2 = ((a2 + os) * 57.295776f - child.ashearX) * (float)s2 + (float)os2 - rotation;
        if (a2 > 180.0f) {
            a2 -= 360.0f;
        } else if (a2 < -180.0f) {
            a2 += 360.0f;
        }
        child.updateWorldTransform(cx, cy, rotation + a2 * alpha, child.ascaleX, child.ascaleY, child.ashearX, child.ashearY);
    }
}

