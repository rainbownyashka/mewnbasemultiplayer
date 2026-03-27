/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.BoneData;
import com.esotericsoftware.spine.EventData;
import com.esotericsoftware.spine.IkConstraintData;
import com.esotericsoftware.spine.PathConstraintData;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.SlotData;
import com.esotericsoftware.spine.TransformConstraintData;

public class SkeletonData {
    String name;
    final Array<BoneData> bones = new Array();
    final Array<SlotData> slots = new Array();
    final Array<Skin> skins = new Array();
    Skin defaultSkin;
    final Array<EventData> events = new Array();
    final Array<Animation> animations = new Array();
    final Array<IkConstraintData> ikConstraints = new Array();
    final Array<TransformConstraintData> transformConstraints = new Array();
    final Array<PathConstraintData> pathConstraints = new Array();
    float x;
    float y;
    float width;
    float height;
    String version;
    String hash;
    float fps = 30.0f;
    String imagesPath;
    String audioPath;

    public Array<BoneData> getBones() {
        return this.bones;
    }

    public BoneData findBone(String boneName) {
        if (boneName == null) {
            throw new IllegalArgumentException("boneName cannot be null.");
        }
        Array<BoneData> bones = this.bones;
        int n = bones.size;
        for (int i = 0; i < n; ++i) {
            BoneData bone = bones.get(i);
            if (!bone.name.equals(boneName)) continue;
            return bone;
        }
        return null;
    }

    public Array<SlotData> getSlots() {
        return this.slots;
    }

    public SlotData findSlot(String slotName) {
        if (slotName == null) {
            throw new IllegalArgumentException("slotName cannot be null.");
        }
        Array<SlotData> slots = this.slots;
        int n = slots.size;
        for (int i = 0; i < n; ++i) {
            SlotData slot = slots.get(i);
            if (!slot.name.equals(slotName)) continue;
            return slot;
        }
        return null;
    }

    public Skin getDefaultSkin() {
        return this.defaultSkin;
    }

    public void setDefaultSkin(Skin defaultSkin) {
        this.defaultSkin = defaultSkin;
    }

    public Skin findSkin(String skinName) {
        if (skinName == null) {
            throw new IllegalArgumentException("skinName cannot be null.");
        }
        for (Skin skin : this.skins) {
            if (!skin.name.equals(skinName)) continue;
            return skin;
        }
        return null;
    }

    public Array<Skin> getSkins() {
        return this.skins;
    }

    public EventData findEvent(String eventDataName) {
        if (eventDataName == null) {
            throw new IllegalArgumentException("eventDataName cannot be null.");
        }
        for (EventData eventData : this.events) {
            if (!eventData.name.equals(eventDataName)) continue;
            return eventData;
        }
        return null;
    }

    public Array<EventData> getEvents() {
        return this.events;
    }

    public Array<Animation> getAnimations() {
        return this.animations;
    }

    public Animation findAnimation(String animationName) {
        if (animationName == null) {
            throw new IllegalArgumentException("animationName cannot be null.");
        }
        Array<Animation> animations = this.animations;
        int n = animations.size;
        for (int i = 0; i < n; ++i) {
            Animation animation = animations.get(i);
            if (!animation.name.equals(animationName)) continue;
            return animation;
        }
        return null;
    }

    public Array<IkConstraintData> getIkConstraints() {
        return this.ikConstraints;
    }

    public IkConstraintData findIkConstraint(String constraintName) {
        if (constraintName == null) {
            throw new IllegalArgumentException("constraintName cannot be null.");
        }
        Array<IkConstraintData> ikConstraints = this.ikConstraints;
        int n = ikConstraints.size;
        for (int i = 0; i < n; ++i) {
            IkConstraintData constraint = ikConstraints.get(i);
            if (!constraint.name.equals(constraintName)) continue;
            return constraint;
        }
        return null;
    }

    public Array<TransformConstraintData> getTransformConstraints() {
        return this.transformConstraints;
    }

    public TransformConstraintData findTransformConstraint(String constraintName) {
        if (constraintName == null) {
            throw new IllegalArgumentException("constraintName cannot be null.");
        }
        Array<TransformConstraintData> transformConstraints = this.transformConstraints;
        int n = transformConstraints.size;
        for (int i = 0; i < n; ++i) {
            TransformConstraintData constraint = transformConstraints.get(i);
            if (!constraint.name.equals(constraintName)) continue;
            return constraint;
        }
        return null;
    }

    public Array<PathConstraintData> getPathConstraints() {
        return this.pathConstraints;
    }

    public PathConstraintData findPathConstraint(String constraintName) {
        if (constraintName == null) {
            throw new IllegalArgumentException("constraintName cannot be null.");
        }
        Array<PathConstraintData> pathConstraints = this.pathConstraints;
        int n = pathConstraints.size;
        for (int i = 0; i < n; ++i) {
            PathConstraintData constraint = pathConstraints.get(i);
            if (!constraint.name.equals(constraintName)) continue;
            return constraint;
        }
        return null;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public float getWidth() {
        return this.width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return this.height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getImagesPath() {
        return this.imagesPath;
    }

    public void setImagesPath(String imagesPath) {
        this.imagesPath = imagesPath;
    }

    public String getAudioPath() {
        return this.audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public float getFps() {
        return this.fps;
    }

    public void setFps(float fps) {
        this.fps = fps;
    }

    public String toString() {
        return this.name != null ? this.name : super.toString();
    }
}

