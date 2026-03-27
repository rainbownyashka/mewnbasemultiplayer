/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.esotericsoftware.spine.BoneData;
import com.esotericsoftware.spine.ConstraintData;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;

public class Skin {
    final String name;
    final OrderedMap<SkinEntry, Attachment> attachments = new OrderedMap();
    final Array<BoneData> bones = new Array();
    final Array<ConstraintData> constraints = new Array();
    private final SkinEntry lookup = new SkinEntry();

    public Skin(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null.");
        }
        this.name = name;
        this.attachments.orderedKeys().ordered = false;
    }

    public void setAttachment(int slotIndex, String name, Attachment attachment) {
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must be >= 0.");
        }
        if (attachment == null) {
            throw new IllegalArgumentException("attachment cannot be null.");
        }
        this.attachments.put(new SkinEntry(slotIndex, name, attachment), attachment);
    }

    public void addSkin(Skin skin) {
        if (skin == null) {
            throw new IllegalArgumentException("skin cannot be null.");
        }
        for (BoneData boneData : skin.bones) {
            if (this.bones.contains(boneData, true)) continue;
            this.bones.add(boneData);
        }
        for (ConstraintData constraintData : skin.constraints) {
            if (this.constraints.contains(constraintData, true)) continue;
            this.constraints.add(constraintData);
        }
        for (SkinEntry skinEntry : skin.attachments.keys()) {
            this.setAttachment(skinEntry.slotIndex, skinEntry.name, skinEntry.attachment);
        }
    }

    public void copySkin(Skin skin) {
        if (skin == null) {
            throw new IllegalArgumentException("skin cannot be null.");
        }
        for (BoneData boneData : skin.bones) {
            if (this.bones.contains(boneData, true)) continue;
            this.bones.add(boneData);
        }
        for (ConstraintData constraintData : skin.constraints) {
            if (this.constraints.contains(constraintData, true)) continue;
            this.constraints.add(constraintData);
        }
        for (SkinEntry skinEntry : skin.attachments.keys()) {
            if (skinEntry.attachment instanceof MeshAttachment) {
                this.setAttachment(skinEntry.slotIndex, skinEntry.name, ((MeshAttachment)skinEntry.attachment).newLinkedMesh());
                continue;
            }
            this.setAttachment(skinEntry.slotIndex, skinEntry.name, skinEntry.attachment != null ? skinEntry.attachment.copy() : null);
        }
    }

    public Attachment getAttachment(int slotIndex, String name) {
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must be >= 0.");
        }
        this.lookup.set(slotIndex, name);
        return (Attachment)this.attachments.get(this.lookup);
    }

    public void removeAttachment(int slotIndex, String name) {
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must be >= 0.");
        }
        this.lookup.set(slotIndex, name);
        this.attachments.remove(this.lookup);
    }

    public Array<SkinEntry> getAttachments() {
        return this.attachments.orderedKeys();
    }

    public void getAttachments(int slotIndex, Array<SkinEntry> attachments) {
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must be >= 0.");
        }
        if (attachments == null) {
            throw new IllegalArgumentException("attachments cannot be null.");
        }
        for (SkinEntry entry : this.attachments.keys()) {
            if (entry.slotIndex != slotIndex) continue;
            attachments.add(entry);
        }
    }

    public void clear() {
        this.attachments.clear(1024);
        this.bones.clear();
        this.constraints.clear();
    }

    public Array<BoneData> getBones() {
        return this.bones;
    }

    public Array<ConstraintData> getConstraints() {
        return this.constraints;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }

    void attachAll(Skeleton skeleton, Skin oldSkin) {
        for (SkinEntry entry : oldSkin.attachments.keys()) {
            Attachment attachment;
            int slotIndex = entry.slotIndex;
            Slot slot = skeleton.slots.get(slotIndex);
            if (slot.attachment != entry.attachment || (attachment = this.getAttachment(slotIndex, entry.name)) == null) continue;
            slot.setAttachment(attachment);
        }
    }

    public static class SkinEntry {
        int slotIndex;
        String name;
        Attachment attachment;
        private int hashCode;

        SkinEntry() {
            this.set(0, "");
        }

        SkinEntry(int slotIndex, String name, Attachment attachment) {
            this.set(slotIndex, name);
            this.attachment = attachment;
        }

        void set(int slotIndex, String name) {
            if (name == null) {
                throw new IllegalArgumentException("name cannot be null.");
            }
            this.slotIndex = slotIndex;
            this.name = name;
            this.hashCode = name.hashCode() + slotIndex * 37;
        }

        public int getSlotIndex() {
            return this.slotIndex;
        }

        public String getName() {
            return this.name;
        }

        public Attachment getAttachment() {
            return this.attachment;
        }

        public int hashCode() {
            return this.hashCode;
        }

        public boolean equals(Object object) {
            if (object == null) {
                return false;
            }
            SkinEntry other = (SkinEntry)object;
            if (this.slotIndex != other.slotIndex) {
                return false;
            }
            return this.name.equals(other.name);
        }

        public String toString() {
            return this.slotIndex + ":" + this.name;
        }
    }
}

