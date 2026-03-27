/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.BoneData;
import com.esotericsoftware.spine.IkConstraint;
import com.esotericsoftware.spine.IkConstraintData;
import com.esotericsoftware.spine.PathConstraint;
import com.esotericsoftware.spine.PathConstraintData;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.SlotData;
import com.esotericsoftware.spine.TransformConstraint;
import com.esotericsoftware.spine.TransformConstraintData;
import com.esotericsoftware.spine.Updatable;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.PathAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.esotericsoftware.spine.utils.SpineUtils;

public class Skeleton {
    final SkeletonData data;
    final Array<Bone> bones;
    final Array<Slot> slots;
    Array<Slot> drawOrder;
    final Array<IkConstraint> ikConstraints;
    final Array<TransformConstraint> transformConstraints;
    final Array<PathConstraint> pathConstraints;
    final Array<Updatable> updateCache = new Array();
    final Array<Bone> updateCacheReset = new Array();
    Skin skin;
    final Color color;
    float time;
    float scaleX = 1.0f;
    float scaleY = 1.0f;
    float x;
    float y;

    public Skeleton(SkeletonData data) {
        Bone bone;
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null.");
        }
        this.data = data;
        this.bones = new Array(data.bones.size);
        for (BoneData boneData : data.bones) {
            if (boneData.parent == null) {
                bone = new Bone(boneData, this, null);
            } else {
                Bone parent = this.bones.get(boneData.parent.index);
                bone = new Bone(boneData, this, parent);
                parent.children.add(bone);
            }
            this.bones.add(bone);
        }
        this.slots = new Array(data.slots.size);
        this.drawOrder = new Array(data.slots.size);
        for (SlotData slotData : data.slots) {
            bone = this.bones.get(slotData.boneData.index);
            Slot slot = new Slot(slotData, bone);
            this.slots.add(slot);
            this.drawOrder.add(slot);
        }
        this.ikConstraints = new Array(data.ikConstraints.size);
        for (IkConstraintData ikConstraintData : data.ikConstraints) {
            this.ikConstraints.add(new IkConstraint(ikConstraintData, this));
        }
        this.transformConstraints = new Array(data.transformConstraints.size);
        for (TransformConstraintData transformConstraintData : data.transformConstraints) {
            this.transformConstraints.add(new TransformConstraint(transformConstraintData, this));
        }
        this.pathConstraints = new Array(data.pathConstraints.size);
        for (PathConstraintData pathConstraintData : data.pathConstraints) {
            this.pathConstraints.add(new PathConstraint(pathConstraintData, this));
        }
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.updateCache();
    }

    public Skeleton(Skeleton skeleton) {
        if (skeleton == null) {
            throw new IllegalArgumentException("skeleton cannot be null.");
        }
        this.data = skeleton.data;
        this.bones = new Array(skeleton.bones.size);
        for (Bone bone : skeleton.bones) {
            Bone newBone;
            if (bone.parent == null) {
                newBone = new Bone(bone, this, null);
            } else {
                Bone parent = this.bones.get(bone.parent.data.index);
                newBone = new Bone(bone, this, parent);
                parent.children.add(newBone);
            }
            this.bones.add(newBone);
        }
        this.slots = new Array(skeleton.slots.size);
        for (Slot slot : skeleton.slots) {
            Bone bone = this.bones.get(slot.bone.data.index);
            this.slots.add(new Slot(slot, bone));
        }
        this.drawOrder = new Array(this.slots.size);
        for (Slot slot : skeleton.drawOrder) {
            this.drawOrder.add(this.slots.get(slot.data.index));
        }
        this.ikConstraints = new Array(skeleton.ikConstraints.size);
        for (IkConstraint ikConstraint : skeleton.ikConstraints) {
            this.ikConstraints.add(new IkConstraint(ikConstraint, this));
        }
        this.transformConstraints = new Array(skeleton.transformConstraints.size);
        for (TransformConstraint transformConstraint : skeleton.transformConstraints) {
            this.transformConstraints.add(new TransformConstraint(transformConstraint, this));
        }
        this.pathConstraints = new Array(skeleton.pathConstraints.size);
        for (PathConstraint pathConstraint : skeleton.pathConstraints) {
            this.pathConstraints.add(new PathConstraint(pathConstraint, this));
        }
        this.skin = skeleton.skin;
        this.color = new Color(skeleton.color);
        this.time = skeleton.time;
        this.scaleX = skeleton.scaleX;
        this.scaleY = skeleton.scaleY;
        this.updateCache();
    }

    public void updateCache() {
        int i;
        Array<Updatable> updateCache = this.updateCache;
        updateCache.clear();
        this.updateCacheReset.clear();
        int boneCount = this.bones.size;
        T[] bones = this.bones.items;
        for (int i2 = 0; i2 < boneCount; ++i2) {
            Bone bone = (Bone)bones[i2];
            bone.sorted = bone.data.skinRequired;
            bone.active = !bone.sorted;
        }
        if (this.skin != null) {
            T[] skinBones = this.skin.bones.items;
            int n = this.skin.bones.size;
            for (int i3 = 0; i3 < n; ++i3) {
                Bone bone = (Bone)bones[((BoneData)skinBones[i3]).index];
                do {
                    bone.sorted = false;
                    bone.active = true;
                } while ((bone = bone.parent) != null);
            }
        }
        int ikCount = this.ikConstraints.size;
        int transformCount = this.transformConstraints.size;
        int pathCount = this.pathConstraints.size;
        T[] ikConstraints = this.ikConstraints.items;
        T[] transformConstraints = this.transformConstraints.items;
        T[] pathConstraints = this.pathConstraints.items;
        int constraintCount = ikCount + transformCount + pathCount;
        block3: for (i = 0; i < constraintCount; ++i) {
            Updatable constraint;
            int ii;
            for (ii = 0; ii < ikCount; ++ii) {
                constraint = (IkConstraint)ikConstraints[ii];
                if (constraint.data.order != i) continue;
                this.sortIkConstraint((IkConstraint)constraint);
                continue block3;
            }
            for (ii = 0; ii < transformCount; ++ii) {
                constraint = (TransformConstraint)transformConstraints[ii];
                if (((TransformConstraint)constraint).data.order != i) continue;
                this.sortTransformConstraint((TransformConstraint)constraint);
                continue block3;
            }
            for (ii = 0; ii < pathCount; ++ii) {
                constraint = (PathConstraint)pathConstraints[ii];
                if (((PathConstraint)constraint).data.order != i) continue;
                this.sortPathConstraint((PathConstraint)constraint);
                continue block3;
            }
        }
        for (i = 0; i < boneCount; ++i) {
            this.sortBone((Bone)bones[i]);
        }
    }

    private void sortIkConstraint(IkConstraint constraint) {
        Bone child;
        boolean bl = constraint.active = constraint.target.active && (!constraint.data.skinRequired || this.skin != null && this.skin.constraints.contains(constraint.data, true));
        if (!constraint.active) {
            return;
        }
        Bone target = constraint.target;
        this.sortBone(target);
        Array<Bone> constrained = constraint.bones;
        Bone parent = constrained.first();
        this.sortBone(parent);
        if (constrained.size > 1 && !this.updateCache.contains(child = constrained.peek(), true)) {
            this.updateCacheReset.add(child);
        }
        this.updateCache.add(constraint);
        this.sortReset(parent.children);
        constrained.peek().sorted = true;
    }

    private void sortPathConstraint(PathConstraint constraint) {
        int i;
        Attachment attachment;
        boolean bl = constraint.active = constraint.target.bone.active && (!constraint.data.skinRequired || this.skin != null && this.skin.constraints.contains(constraint.data, true));
        if (!constraint.active) {
            return;
        }
        Slot slot = constraint.target;
        int slotIndex = slot.getData().index;
        Bone slotBone = slot.bone;
        if (this.skin != null) {
            this.sortPathConstraintAttachment(this.skin, slotIndex, slotBone);
        }
        if (this.data.defaultSkin != null && this.data.defaultSkin != this.skin) {
            this.sortPathConstraintAttachment(this.data.defaultSkin, slotIndex, slotBone);
        }
        if ((attachment = slot.attachment) instanceof PathAttachment) {
            this.sortPathConstraintAttachment(attachment, slotBone);
        }
        Array<Bone> constrained = constraint.bones;
        int boneCount = constrained.size;
        for (i = 0; i < boneCount; ++i) {
            this.sortBone(constrained.get(i));
        }
        this.updateCache.add(constraint);
        for (i = 0; i < boneCount; ++i) {
            this.sortReset(constrained.get((int)i).children);
        }
        for (i = 0; i < boneCount; ++i) {
            constrained.get((int)i).sorted = true;
        }
    }

    private void sortTransformConstraint(TransformConstraint constraint) {
        int i;
        boolean bl = constraint.active = constraint.target.active && (!constraint.data.skinRequired || this.skin != null && this.skin.constraints.contains(constraint.data, true));
        if (!constraint.active) {
            return;
        }
        this.sortBone(constraint.target);
        Array<Bone> constrained = constraint.bones;
        int boneCount = constrained.size;
        if (constraint.data.local) {
            for (i = 0; i < boneCount; ++i) {
                Bone child = constrained.get(i);
                this.sortBone(child.parent);
                if (this.updateCache.contains(child, true)) continue;
                this.updateCacheReset.add(child);
            }
        } else {
            for (i = 0; i < boneCount; ++i) {
                this.sortBone(constrained.get(i));
            }
        }
        this.updateCache.add(constraint);
        for (i = 0; i < boneCount; ++i) {
            this.sortReset(constrained.get((int)i).children);
        }
        for (i = 0; i < boneCount; ++i) {
            constrained.get((int)i).sorted = true;
        }
    }

    private void sortPathConstraintAttachment(Skin skin, int slotIndex, Bone slotBone) {
        for (Skin.SkinEntry entry : skin.attachments.keys()) {
            if (entry.getSlotIndex() != slotIndex) continue;
            this.sortPathConstraintAttachment(entry.getAttachment(), slotBone);
        }
    }

    private void sortPathConstraintAttachment(Attachment attachment, Bone slotBone) {
        if (!(attachment instanceof PathAttachment)) {
            return;
        }
        int[] pathBones = ((PathAttachment)attachment).getBones();
        if (pathBones == null) {
            this.sortBone(slotBone);
        } else {
            Array<Bone> bones = this.bones;
            int i = 0;
            int n = pathBones.length;
            while (i < n) {
                int nn = pathBones[i++];
                nn += i;
                while (i < nn) {
                    this.sortBone(bones.get(pathBones[i++]));
                }
            }
        }
    }

    private void sortBone(Bone bone) {
        if (bone.sorted) {
            return;
        }
        Bone parent = bone.parent;
        if (parent != null) {
            this.sortBone(parent);
        }
        bone.sorted = true;
        this.updateCache.add(bone);
    }

    private void sortReset(Array<Bone> bones) {
        int n = bones.size;
        for (int i = 0; i < n; ++i) {
            Bone bone = bones.get(i);
            if (!bone.active) continue;
            if (bone.sorted) {
                this.sortReset(bone.children);
            }
            bone.sorted = false;
        }
    }

    public void updateWorldTransform() {
        Array<Bone> updateCacheReset = this.updateCacheReset;
        int n = updateCacheReset.size;
        for (int i = 0; i < n; ++i) {
            Bone bone = updateCacheReset.get(i);
            bone.ax = bone.x;
            bone.ay = bone.y;
            bone.arotation = bone.rotation;
            bone.ascaleX = bone.scaleX;
            bone.ascaleY = bone.scaleY;
            bone.ashearX = bone.shearX;
            bone.ashearY = bone.shearY;
            bone.appliedValid = true;
        }
        Array<Updatable> updateCache = this.updateCache;
        int n2 = updateCache.size;
        for (int i = 0; i < n2; ++i) {
            updateCache.get(i).update();
        }
    }

    public void updateWorldTransform(Bone parent) {
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null.");
        }
        Array<Bone> updateCacheReset = this.updateCacheReset;
        int n = updateCacheReset.size;
        for (int i = 0; i < n; ++i) {
            Bone bone = updateCacheReset.get(i);
            bone.ax = bone.x;
            bone.ay = bone.y;
            bone.arotation = bone.rotation;
            bone.ascaleX = bone.scaleX;
            bone.ascaleY = bone.scaleY;
            bone.ashearX = bone.shearX;
            bone.ashearY = bone.shearY;
            bone.appliedValid = true;
        }
        Bone rootBone = this.getRootBone();
        float pa = parent.a;
        float pb = parent.b;
        float pc = parent.c;
        float pd = parent.d;
        rootBone.worldX = pa * this.x + pb * this.y + parent.worldX;
        rootBone.worldY = pc * this.x + pd * this.y + parent.worldY;
        float rotationY = rootBone.rotation + 90.0f + rootBone.shearY;
        float la = SpineUtils.cosDeg(rootBone.rotation + rootBone.shearX) * rootBone.scaleX;
        float lb = SpineUtils.cosDeg(rotationY) * rootBone.scaleY;
        float lc = SpineUtils.sinDeg(rootBone.rotation + rootBone.shearX) * rootBone.scaleX;
        float ld = SpineUtils.sinDeg(rotationY) * rootBone.scaleY;
        rootBone.a = (pa * la + pb * lc) * this.scaleX;
        rootBone.b = (pa * lb + pb * ld) * this.scaleX;
        rootBone.c = (pc * la + pd * lc) * this.scaleY;
        rootBone.d = (pc * lb + pd * ld) * this.scaleY;
        Array<Updatable> updateCache = this.updateCache;
        int n2 = updateCache.size;
        for (int i = 0; i < n2; ++i) {
            Updatable updatable = updateCache.get(i);
            if (updatable == rootBone) continue;
            updatable.update();
        }
    }

    public void setToSetupPose() {
        this.setBonesToSetupPose();
        this.setSlotsToSetupPose();
    }

    public void setBonesToSetupPose() {
        Array<Bone> bones = this.bones;
        int n = bones.size;
        for (int i = 0; i < n; ++i) {
            bones.get(i).setToSetupPose();
        }
        Array<IkConstraint> ikConstraints = this.ikConstraints;
        int n2 = ikConstraints.size;
        for (int i = 0; i < n2; ++i) {
            IkConstraint constraint = ikConstraints.get(i);
            constraint.mix = constraint.data.mix;
            constraint.softness = constraint.data.softness;
            constraint.bendDirection = constraint.data.bendDirection;
            constraint.compress = constraint.data.compress;
            constraint.stretch = constraint.data.stretch;
        }
        Array<TransformConstraint> transformConstraints = this.transformConstraints;
        int n3 = transformConstraints.size;
        for (int i = 0; i < n3; ++i) {
            TransformConstraint constraint = transformConstraints.get(i);
            TransformConstraintData data = constraint.data;
            constraint.rotateMix = data.rotateMix;
            constraint.translateMix = data.translateMix;
            constraint.scaleMix = data.scaleMix;
            constraint.shearMix = data.shearMix;
        }
        Array<PathConstraint> pathConstraints = this.pathConstraints;
        int n4 = pathConstraints.size;
        for (int i = 0; i < n4; ++i) {
            PathConstraint constraint = pathConstraints.get(i);
            PathConstraintData data = constraint.data;
            constraint.position = data.position;
            constraint.spacing = data.spacing;
            constraint.rotateMix = data.rotateMix;
            constraint.translateMix = data.translateMix;
        }
    }

    public void setSlotsToSetupPose() {
        Array<Slot> slots = this.slots;
        SpineUtils.arraycopy(slots.items, 0, this.drawOrder.items, 0, slots.size);
        int n = slots.size;
        for (int i = 0; i < n; ++i) {
            slots.get(i).setToSetupPose();
        }
    }

    public SkeletonData getData() {
        return this.data;
    }

    public Array<Bone> getBones() {
        return this.bones;
    }

    public Array<Updatable> getUpdateCache() {
        return this.updateCache;
    }

    public Bone getRootBone() {
        if (this.bones.size == 0) {
            return null;
        }
        return this.bones.first();
    }

    public Bone findBone(String boneName) {
        if (boneName == null) {
            throw new IllegalArgumentException("boneName cannot be null.");
        }
        Array<Bone> bones = this.bones;
        int n = bones.size;
        for (int i = 0; i < n; ++i) {
            Bone bone = bones.get(i);
            if (!bone.data.name.equals(boneName)) continue;
            return bone;
        }
        return null;
    }

    public Array<Slot> getSlots() {
        return this.slots;
    }

    public Slot findSlot(String slotName) {
        if (slotName == null) {
            throw new IllegalArgumentException("slotName cannot be null.");
        }
        Array<Slot> slots = this.slots;
        int n = slots.size;
        for (int i = 0; i < n; ++i) {
            Slot slot = slots.get(i);
            if (!slot.data.name.equals(slotName)) continue;
            return slot;
        }
        return null;
    }

    public Array<Slot> getDrawOrder() {
        return this.drawOrder;
    }

    public void setDrawOrder(Array<Slot> drawOrder) {
        if (drawOrder == null) {
            throw new IllegalArgumentException("drawOrder cannot be null.");
        }
        this.drawOrder = drawOrder;
    }

    public Skin getSkin() {
        return this.skin;
    }

    public void setSkin(String skinName) {
        Skin skin = this.data.findSkin(skinName);
        if (skin == null) {
            throw new IllegalArgumentException("Skin not found: " + skinName);
        }
        this.setSkin(skin);
    }

    public void setSkin(Skin newSkin) {
        if (newSkin == this.skin) {
            return;
        }
        if (newSkin != null) {
            if (this.skin != null) {
                newSkin.attachAll(this, this.skin);
            } else {
                Array<Slot> slots = this.slots;
                int n = slots.size;
                for (int i = 0; i < n; ++i) {
                    Attachment attachment;
                    Slot slot = slots.get(i);
                    String name = slot.data.attachmentName;
                    if (name == null || (attachment = newSkin.getAttachment(i, name)) == null) continue;
                    slot.setAttachment(attachment);
                }
            }
        }
        this.skin = newSkin;
        this.updateCache();
    }

    public Attachment getAttachment(String slotName, String attachmentName) {
        SlotData slot = this.data.findSlot(slotName);
        if (slot == null) {
            throw new IllegalArgumentException("Slot not found: " + slotName);
        }
        return this.getAttachment(slot.getIndex(), attachmentName);
    }

    public Attachment getAttachment(int slotIndex, String attachmentName) {
        Attachment attachment;
        if (attachmentName == null) {
            throw new IllegalArgumentException("attachmentName cannot be null.");
        }
        if (this.skin != null && (attachment = this.skin.getAttachment(slotIndex, attachmentName)) != null) {
            return attachment;
        }
        if (this.data.defaultSkin != null) {
            return this.data.defaultSkin.getAttachment(slotIndex, attachmentName);
        }
        return null;
    }

    public void setAttachment(String slotName, String attachmentName) {
        if (slotName == null) {
            throw new IllegalArgumentException("slotName cannot be null.");
        }
        Slot slot = this.findSlot(slotName);
        if (slot == null) {
            throw new IllegalArgumentException("Slot not found: " + slotName);
        }
        Attachment attachment = null;
        if (attachmentName != null && (attachment = this.getAttachment(slot.data.index, attachmentName)) == null) {
            throw new IllegalArgumentException("Attachment not found: " + attachmentName + ", for slot: " + slotName);
        }
        slot.setAttachment(attachment);
    }

    public Array<IkConstraint> getIkConstraints() {
        return this.ikConstraints;
    }

    public IkConstraint findIkConstraint(String constraintName) {
        if (constraintName == null) {
            throw new IllegalArgumentException("constraintName cannot be null.");
        }
        Array<IkConstraint> ikConstraints = this.ikConstraints;
        int n = ikConstraints.size;
        for (int i = 0; i < n; ++i) {
            IkConstraint ikConstraint = ikConstraints.get(i);
            if (!ikConstraint.data.name.equals(constraintName)) continue;
            return ikConstraint;
        }
        return null;
    }

    public Array<TransformConstraint> getTransformConstraints() {
        return this.transformConstraints;
    }

    public TransformConstraint findTransformConstraint(String constraintName) {
        if (constraintName == null) {
            throw new IllegalArgumentException("constraintName cannot be null.");
        }
        Array<TransformConstraint> transformConstraints = this.transformConstraints;
        int n = transformConstraints.size;
        for (int i = 0; i < n; ++i) {
            TransformConstraint constraint = transformConstraints.get(i);
            if (!constraint.data.name.equals(constraintName)) continue;
            return constraint;
        }
        return null;
    }

    public Array<PathConstraint> getPathConstraints() {
        return this.pathConstraints;
    }

    public PathConstraint findPathConstraint(String constraintName) {
        if (constraintName == null) {
            throw new IllegalArgumentException("constraintName cannot be null.");
        }
        Array<PathConstraint> pathConstraints = this.pathConstraints;
        int n = pathConstraints.size;
        for (int i = 0; i < n; ++i) {
            PathConstraint constraint = pathConstraints.get(i);
            if (!constraint.data.name.equals(constraintName)) continue;
            return constraint;
        }
        return null;
    }

    public void getBounds(Vector2 offset, Vector2 size, FloatArray temp) {
        if (offset == null) {
            throw new IllegalArgumentException("offset cannot be null.");
        }
        if (size == null) {
            throw new IllegalArgumentException("size cannot be null.");
        }
        if (temp == null) {
            throw new IllegalArgumentException("temp cannot be null.");
        }
        Array<Slot> drawOrder = this.drawOrder;
        float minX = 2.1474836E9f;
        float minY = 2.1474836E9f;
        float maxX = -2.1474836E9f;
        float maxY = -2.1474836E9f;
        int n = drawOrder.size;
        for (int i = 0; i < n; ++i) {
            Slot slot = drawOrder.get(i);
            if (!slot.bone.active) continue;
            int verticesLength = 0;
            float[] vertices = null;
            Attachment attachment = slot.attachment;
            if (attachment instanceof RegionAttachment) {
                verticesLength = 8;
                vertices = temp.setSize(8);
                ((RegionAttachment)attachment).computeWorldVertices(slot.getBone(), vertices, 0, 2);
            } else if (attachment instanceof MeshAttachment) {
                MeshAttachment mesh = (MeshAttachment)attachment;
                verticesLength = mesh.getWorldVerticesLength();
                vertices = temp.setSize(verticesLength);
                mesh.computeWorldVertices(slot, 0, verticesLength, vertices, 0, 2);
            }
            if (vertices == null) continue;
            for (int ii = 0; ii < verticesLength; ii += 2) {
                float x = vertices[ii];
                float y = vertices[ii + 1];
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        offset.set(minX, minY);
        size.set(maxX - minX, maxY - minY);
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color cannot be null.");
        }
        this.color.set(color);
    }

    public float getScaleX() {
        return this.scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return this.scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getTime() {
        return this.time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public void update(float delta) {
        this.time += delta;
    }

    public String toString() {
        return this.data.name != null ? this.data.name : super.toString();
    }
}

