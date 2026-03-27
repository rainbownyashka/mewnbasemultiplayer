/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.BlendMode;
import com.esotericsoftware.spine.BoneData;
import com.esotericsoftware.spine.ConstraintData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.EventData;
import com.esotericsoftware.spine.IkConstraintData;
import com.esotericsoftware.spine.PathConstraintData;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.SlotData;
import com.esotericsoftware.spine.TransformConstraintData;
import com.esotericsoftware.spine.attachments.AtlasAttachmentLoader;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.AttachmentLoader;
import com.esotericsoftware.spine.attachments.AttachmentType;
import com.esotericsoftware.spine.attachments.BoundingBoxAttachment;
import com.esotericsoftware.spine.attachments.ClippingAttachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.PathAttachment;
import com.esotericsoftware.spine.attachments.PointAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.esotericsoftware.spine.attachments.VertexAttachment;
import com.esotericsoftware.spine.utils.SpineUtils;

public class SkeletonJson {
    private final AttachmentLoader attachmentLoader;
    private float scale = 1.0f;
    private Array<LinkedMesh> linkedMeshes = new Array();

    public SkeletonJson(TextureAtlas atlas) {
        this.attachmentLoader = new AtlasAttachmentLoader(atlas);
    }

    public SkeletonJson(AttachmentLoader attachmentLoader) {
        if (attachmentLoader == null) {
            throw new IllegalArgumentException("attachmentLoader cannot be null.");
        }
        this.attachmentLoader = attachmentLoader;
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(float scale) {
        if (scale == 0.0f) {
            throw new IllegalArgumentException("scale cannot be 0.");
        }
        this.scale = scale;
    }

    protected JsonValue parse(FileHandle file) {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null.");
        }
        return new JsonReader().parse(file);
    }

    public SkeletonData readSkeletonData(FileHandle file) {
        String targetName;
        BoneData bone;
        JsonValue entry;
        ConstraintData data;
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null.");
        }
        float scale = this.scale;
        SkeletonData skeletonData = new SkeletonData();
        skeletonData.name = file.nameWithoutExtension();
        JsonValue root = this.parse(file);
        JsonValue skeletonMap = root.get("skeleton");
        if (skeletonMap != null) {
            skeletonData.hash = skeletonMap.getString("hash", null);
            skeletonData.version = skeletonMap.getString("spine", null);
            skeletonData.x = skeletonMap.getFloat("x", 0.0f);
            skeletonData.y = skeletonMap.getFloat("y", 0.0f);
            skeletonData.width = skeletonMap.getFloat("width", 0.0f);
            skeletonData.height = skeletonMap.getFloat("height", 0.0f);
            skeletonData.fps = skeletonMap.getFloat("fps", 30.0f);
            skeletonData.imagesPath = skeletonMap.getString("images", null);
            skeletonData.audioPath = skeletonMap.getString("audio", null);
        }
        JsonValue boneMap = root.getChild("bones");
        while (boneMap != null) {
            BoneData parent = null;
            String parentName = boneMap.getString("parent", null);
            if (parentName != null && (parent = skeletonData.findBone(parentName)) == null) {
                throw new SerializationException("Parent bone not found: " + parentName);
            }
            BoneData data2 = new BoneData(skeletonData.bones.size, boneMap.getString("name"), parent);
            data2.length = boneMap.getFloat("length", 0.0f) * scale;
            data2.x = boneMap.getFloat("x", 0.0f) * scale;
            data2.y = boneMap.getFloat("y", 0.0f) * scale;
            data2.rotation = boneMap.getFloat("rotation", 0.0f);
            data2.scaleX = boneMap.getFloat("scaleX", 1.0f);
            data2.scaleY = boneMap.getFloat("scaleY", 1.0f);
            data2.shearX = boneMap.getFloat("shearX", 0.0f);
            data2.shearY = boneMap.getFloat("shearY", 0.0f);
            data2.transformMode = BoneData.TransformMode.valueOf(boneMap.getString("transform", BoneData.TransformMode.normal.name()));
            data2.skinRequired = boneMap.getBoolean("skin", false);
            String color = boneMap.getString("color", null);
            if (color != null) {
                data2.getColor().set(Color.valueOf(color));
            }
            skeletonData.bones.add(data2);
            boneMap = boneMap.next;
        }
        JsonValue slotMap = root.getChild("slots");
        while (slotMap != null) {
            String dark;
            String slotName = slotMap.getString("name");
            String boneName = slotMap.getString("bone");
            BoneData boneData = skeletonData.findBone(boneName);
            if (boneData == null) {
                throw new SerializationException("Slot bone not found: " + boneName);
            }
            SlotData data3 = new SlotData(skeletonData.slots.size, slotName, boneData);
            String color = slotMap.getString("color", null);
            if (color != null) {
                data3.getColor().set(Color.valueOf(color));
            }
            if ((dark = slotMap.getString("dark", null)) != null) {
                data3.setDarkColor(Color.valueOf(dark));
            }
            data3.attachmentName = slotMap.getString("attachment", null);
            data3.blendMode = BlendMode.valueOf(slotMap.getString("blend", BlendMode.normal.name()));
            skeletonData.slots.add(data3);
            slotMap = slotMap.next;
        }
        JsonValue constraintMap = root.getChild("ik");
        while (constraintMap != null) {
            data = new IkConstraintData(constraintMap.getString("name"));
            data.order = constraintMap.getInt("order", 0);
            data.skinRequired = constraintMap.getBoolean("skin", false);
            entry = constraintMap.getChild("bones");
            while (entry != null) {
                bone = skeletonData.findBone(entry.asString());
                if (bone == null) {
                    throw new SerializationException("IK bone not found: " + entry);
                }
                data.bones.add(bone);
                entry = entry.next;
            }
            targetName = constraintMap.getString("target");
            data.target = skeletonData.findBone(targetName);
            if (data.target == null) {
                throw new SerializationException("IK target bone not found: " + targetName);
            }
            data.mix = constraintMap.getFloat("mix", 1.0f);
            data.softness = constraintMap.getFloat("softness", 0.0f) * scale;
            data.bendDirection = constraintMap.getBoolean("bendPositive", true) ? 1 : -1;
            data.compress = constraintMap.getBoolean("compress", false);
            data.stretch = constraintMap.getBoolean("stretch", false);
            data.uniform = constraintMap.getBoolean("uniform", false);
            skeletonData.ikConstraints.add((IkConstraintData)data);
            constraintMap = constraintMap.next;
        }
        constraintMap = root.getChild("transform");
        while (constraintMap != null) {
            data = new TransformConstraintData(constraintMap.getString("name"));
            ((TransformConstraintData)data).order = constraintMap.getInt("order", 0);
            ((TransformConstraintData)data).skinRequired = constraintMap.getBoolean("skin", false);
            entry = constraintMap.getChild("bones");
            while (entry != null) {
                bone = skeletonData.findBone(entry.asString());
                if (bone == null) {
                    throw new SerializationException("Transform constraint bone not found: " + entry);
                }
                ((TransformConstraintData)data).bones.add(bone);
                entry = entry.next;
            }
            targetName = constraintMap.getString("target");
            ((TransformConstraintData)data).target = skeletonData.findBone(targetName);
            if (((TransformConstraintData)data).target == null) {
                throw new SerializationException("Transform constraint target bone not found: " + targetName);
            }
            ((TransformConstraintData)data).local = constraintMap.getBoolean("local", false);
            ((TransformConstraintData)data).relative = constraintMap.getBoolean("relative", false);
            ((TransformConstraintData)data).offsetRotation = constraintMap.getFloat("rotation", 0.0f);
            ((TransformConstraintData)data).offsetX = constraintMap.getFloat("x", 0.0f) * scale;
            ((TransformConstraintData)data).offsetY = constraintMap.getFloat("y", 0.0f) * scale;
            ((TransformConstraintData)data).offsetScaleX = constraintMap.getFloat("scaleX", 0.0f);
            ((TransformConstraintData)data).offsetScaleY = constraintMap.getFloat("scaleY", 0.0f);
            ((TransformConstraintData)data).offsetShearY = constraintMap.getFloat("shearY", 0.0f);
            ((TransformConstraintData)data).rotateMix = constraintMap.getFloat("rotateMix", 1.0f);
            ((TransformConstraintData)data).translateMix = constraintMap.getFloat("translateMix", 1.0f);
            ((TransformConstraintData)data).scaleMix = constraintMap.getFloat("scaleMix", 1.0f);
            ((TransformConstraintData)data).shearMix = constraintMap.getFloat("shearMix", 1.0f);
            skeletonData.transformConstraints.add((TransformConstraintData)data);
            constraintMap = constraintMap.next;
        }
        constraintMap = root.getChild("path");
        while (constraintMap != null) {
            data = new PathConstraintData(constraintMap.getString("name"));
            ((PathConstraintData)data).order = constraintMap.getInt("order", 0);
            ((PathConstraintData)data).skinRequired = constraintMap.getBoolean("skin", false);
            entry = constraintMap.getChild("bones");
            while (entry != null) {
                bone = skeletonData.findBone(entry.asString());
                if (bone == null) {
                    throw new SerializationException("Path bone not found: " + entry);
                }
                ((PathConstraintData)data).bones.add(bone);
                entry = entry.next;
            }
            targetName = constraintMap.getString("target");
            ((PathConstraintData)data).target = skeletonData.findSlot(targetName);
            if (((PathConstraintData)data).target == null) {
                throw new SerializationException("Path target slot not found: " + targetName);
            }
            ((PathConstraintData)data).positionMode = PathConstraintData.PositionMode.valueOf(constraintMap.getString("positionMode", "percent"));
            ((PathConstraintData)data).spacingMode = PathConstraintData.SpacingMode.valueOf(constraintMap.getString("spacingMode", "length"));
            ((PathConstraintData)data).rotateMode = PathConstraintData.RotateMode.valueOf(constraintMap.getString("rotateMode", "tangent"));
            ((PathConstraintData)data).offsetRotation = constraintMap.getFloat("rotation", 0.0f);
            ((PathConstraintData)data).position = constraintMap.getFloat("position", 0.0f);
            if (((PathConstraintData)data).positionMode == PathConstraintData.PositionMode.fixed) {
                ((PathConstraintData)data).position *= scale;
            }
            ((PathConstraintData)data).spacing = constraintMap.getFloat("spacing", 0.0f);
            if (((PathConstraintData)data).spacingMode == PathConstraintData.SpacingMode.length || ((PathConstraintData)data).spacingMode == PathConstraintData.SpacingMode.fixed) {
                ((PathConstraintData)data).spacing *= scale;
            }
            ((PathConstraintData)data).rotateMix = constraintMap.getFloat("rotateMix", 1.0f);
            ((PathConstraintData)data).translateMix = constraintMap.getFloat("translateMix", 1.0f);
            skeletonData.pathConstraints.add((PathConstraintData)data);
            constraintMap = constraintMap.next;
        }
        JsonValue skinMap = root.getChild("skins");
        while (skinMap != null) {
            ConstraintData constraint;
            Skin skin = new Skin(skinMap.getString("name"));
            entry = skinMap.getChild("bones");
            while (entry != null) {
                bone = skeletonData.findBone(entry.asString());
                if (bone == null) {
                    throw new SerializationException("Skin bone not found: " + entry);
                }
                skin.bones.add(bone);
                entry = entry.next;
            }
            entry = skinMap.getChild("ik");
            while (entry != null) {
                constraint = skeletonData.findIkConstraint(entry.asString());
                if (constraint == null) {
                    throw new SerializationException("Skin IK constraint not found: " + entry);
                }
                skin.constraints.add(constraint);
                entry = entry.next;
            }
            entry = skinMap.getChild("transform");
            while (entry != null) {
                constraint = skeletonData.findTransformConstraint(entry.asString());
                if (constraint == null) {
                    throw new SerializationException("Skin transform constraint not found: " + entry);
                }
                skin.constraints.add(constraint);
                entry = entry.next;
            }
            entry = skinMap.getChild("path");
            while (entry != null) {
                constraint = skeletonData.findPathConstraint(entry.asString());
                if (constraint == null) {
                    throw new SerializationException("Skin path constraint not found: " + entry);
                }
                skin.constraints.add(constraint);
                entry = entry.next;
            }
            JsonValue slotEntry = skinMap.getChild("attachments");
            while (slotEntry != null) {
                SlotData slot = skeletonData.findSlot(slotEntry.name);
                if (slot == null) {
                    throw new SerializationException("Slot not found: " + slotEntry.name);
                }
                JsonValue entry2 = slotEntry.child;
                while (entry2 != null) {
                    try {
                        Attachment attachment = this.readAttachment(entry2, skin, slot.index, entry2.name, skeletonData);
                        if (attachment != null) {
                            skin.setAttachment(slot.index, entry2.name, attachment);
                        }
                    }
                    catch (Throwable ex) {
                        throw new SerializationException("Error reading attachment: " + entry2.name + ", skin: " + skin, ex);
                    }
                    entry2 = entry2.next;
                }
                slotEntry = slotEntry.next;
            }
            skeletonData.skins.add(skin);
            if (skin.name.equals("default")) {
                skeletonData.defaultSkin = skin;
            }
            skinMap = skinMap.next;
        }
        int n = this.linkedMeshes.size;
        for (int i = 0; i < n; ++i) {
            Skin skin;
            LinkedMesh linkedMesh = this.linkedMeshes.get(i);
            Skin skin2 = skin = linkedMesh.skin == null ? skeletonData.getDefaultSkin() : skeletonData.findSkin(linkedMesh.skin);
            if (skin == null) {
                throw new SerializationException("Skin not found: " + linkedMesh.skin);
            }
            Attachment parent = skin.getAttachment(linkedMesh.slotIndex, linkedMesh.parent);
            if (parent == null) {
                throw new SerializationException("Parent mesh not found: " + linkedMesh.parent);
            }
            linkedMesh.mesh.setDeformAttachment(linkedMesh.inheritDeform ? (VertexAttachment)parent : linkedMesh.mesh);
            linkedMesh.mesh.setParentMesh((MeshAttachment)parent);
            linkedMesh.mesh.updateUVs();
        }
        this.linkedMeshes.clear();
        JsonValue eventMap = root.getChild("events");
        while (eventMap != null) {
            EventData data4 = new EventData(eventMap.name);
            data4.intValue = eventMap.getInt("int", 0);
            data4.floatValue = eventMap.getFloat("float", 0.0f);
            data4.stringValue = eventMap.getString("string", "");
            data4.audioPath = eventMap.getString("audio", null);
            if (data4.audioPath != null) {
                data4.volume = eventMap.getFloat("volume", 1.0f);
                data4.balance = eventMap.getFloat("balance", 0.0f);
            }
            skeletonData.events.add(data4);
            eventMap = eventMap.next;
        }
        JsonValue animationMap = root.getChild("animations");
        while (animationMap != null) {
            try {
                this.readAnimation(animationMap, animationMap.name, skeletonData);
            }
            catch (Throwable ex) {
                throw new SerializationException("Error reading animation: " + animationMap.name, ex);
            }
            animationMap = animationMap.next;
        }
        skeletonData.bones.shrink();
        skeletonData.slots.shrink();
        skeletonData.skins.shrink();
        skeletonData.events.shrink();
        skeletonData.animations.shrink();
        skeletonData.ikConstraints.shrink();
        return skeletonData;
    }

    private Attachment readAttachment(JsonValue map, Skin skin, int slotIndex, String name, SkeletonData skeletonData) {
        float scale = this.scale;
        name = map.getString("name", name);
        String type = map.getString("type", AttachmentType.region.name());
        switch (AttachmentType.valueOf(type)) {
            case region: {
                String path = map.getString("path", name);
                RegionAttachment region = this.attachmentLoader.newRegionAttachment(skin, name, path);
                if (region == null) {
                    return null;
                }
                region.setPath(path);
                region.setX(map.getFloat("x", 0.0f) * scale);
                region.setY(map.getFloat("y", 0.0f) * scale);
                region.setScaleX(map.getFloat("scaleX", 1.0f));
                region.setScaleY(map.getFloat("scaleY", 1.0f));
                region.setRotation(map.getFloat("rotation", 0.0f));
                region.setWidth(map.getFloat("width") * scale);
                region.setHeight(map.getFloat("height") * scale);
                String color = map.getString("color", null);
                if (color != null) {
                    region.getColor().set(Color.valueOf(color));
                }
                region.updateOffset();
                return region;
            }
            case boundingbox: {
                BoundingBoxAttachment box = this.attachmentLoader.newBoundingBoxAttachment(skin, name);
                if (box == null) {
                    return null;
                }
                this.readVertices(map, box, map.getInt("vertexCount") << 1);
                String color = map.getString("color", null);
                if (color != null) {
                    box.getColor().set(Color.valueOf(color));
                }
                return box;
            }
            case mesh: 
            case linkedmesh: {
                String path = map.getString("path", name);
                MeshAttachment mesh = this.attachmentLoader.newMeshAttachment(skin, name, path);
                if (mesh == null) {
                    return null;
                }
                mesh.setPath(path);
                String color = map.getString("color", null);
                if (color != null) {
                    mesh.getColor().set(Color.valueOf(color));
                }
                mesh.setWidth(map.getFloat("width", 0.0f) * scale);
                mesh.setHeight(map.getFloat("height", 0.0f) * scale);
                String parent = map.getString("parent", null);
                if (parent != null) {
                    this.linkedMeshes.add(new LinkedMesh(mesh, map.getString("skin", null), slotIndex, parent, map.getBoolean("deform", true)));
                    return mesh;
                }
                float[] uvs = map.require("uvs").asFloatArray();
                this.readVertices(map, mesh, uvs.length);
                mesh.setTriangles(map.require("triangles").asShortArray());
                mesh.setRegionUVs(uvs);
                mesh.updateUVs();
                if (map.has("hull")) {
                    mesh.setHullLength(map.require("hull").asInt() * 2);
                }
                if (map.has("edges")) {
                    mesh.setEdges(map.require("edges").asShortArray());
                }
                return mesh;
            }
            case path: {
                PathAttachment path = this.attachmentLoader.newPathAttachment(skin, name);
                if (path == null) {
                    return null;
                }
                path.setClosed(map.getBoolean("closed", false));
                path.setConstantSpeed(map.getBoolean("constantSpeed", true));
                int vertexCount = map.getInt("vertexCount");
                this.readVertices(map, path, vertexCount << 1);
                float[] lengths = new float[vertexCount / 3];
                int i = 0;
                JsonValue curves = map.require((String)"lengths").child;
                while (curves != null) {
                    lengths[i++] = curves.asFloat() * scale;
                    curves = curves.next;
                }
                path.setLengths(lengths);
                String color = map.getString("color", null);
                if (color != null) {
                    path.getColor().set(Color.valueOf(color));
                }
                return path;
            }
            case point: {
                PointAttachment point = this.attachmentLoader.newPointAttachment(skin, name);
                if (point == null) {
                    return null;
                }
                point.setX(map.getFloat("x", 0.0f) * scale);
                point.setY(map.getFloat("y", 0.0f) * scale);
                point.setRotation(map.getFloat("rotation", 0.0f));
                String color = map.getString("color", null);
                if (color != null) {
                    point.getColor().set(Color.valueOf(color));
                }
                return point;
            }
            case clipping: {
                ClippingAttachment clip = this.attachmentLoader.newClippingAttachment(skin, name);
                if (clip == null) {
                    return null;
                }
                String end = map.getString("end", null);
                if (end != null) {
                    SlotData slot = skeletonData.findSlot(end);
                    if (slot == null) {
                        throw new SerializationException("Clipping end slot not found: " + end);
                    }
                    clip.setEndSlot(slot);
                }
                this.readVertices(map, clip, map.getInt("vertexCount") << 1);
                String color = map.getString("color", null);
                if (color != null) {
                    clip.getColor().set(Color.valueOf(color));
                }
                return clip;
            }
        }
        return null;
    }

    private void readVertices(JsonValue map, VertexAttachment attachment, int verticesLength) {
        attachment.setWorldVerticesLength(verticesLength);
        float[] vertices = map.require("vertices").asFloatArray();
        if (verticesLength == vertices.length) {
            if (this.scale != 1.0f) {
                int i = 0;
                int n = vertices.length;
                while (i < n) {
                    int n2 = i++;
                    vertices[n2] = vertices[n2] * this.scale;
                }
            }
            attachment.setVertices(vertices);
            return;
        }
        FloatArray weights = new FloatArray(verticesLength * 3 * 3);
        IntArray bones = new IntArray(verticesLength * 3);
        int i = 0;
        int n = vertices.length;
        while (i < n) {
            int boneCount = (int)vertices[i++];
            bones.add(boneCount);
            int nn = i + boneCount * 4;
            while (i < nn) {
                bones.add((int)vertices[i]);
                weights.add(vertices[i + 1] * this.scale);
                weights.add(vertices[i + 2] * this.scale);
                weights.add(vertices[i + 3]);
                i += 4;
            }
        }
        attachment.setBones(bones.toArray());
        attachment.setVertices(weights.toArray());
    }

    private void readAnimation(JsonValue map, String name, SkeletonData skeletonData) {
        JsonValue eventsMap;
        JsonValue valueMap;
        Animation.CurveTimeline timeline;
        ConstraintData constraint;
        JsonValue valueMap2;
        Animation.Timeline timeline2;
        String timelineName;
        JsonValue timelineMap;
        float scale = this.scale;
        Array<Animation.Timeline> timelines = new Array<Animation.Timeline>();
        float duration = 0.0f;
        JsonValue slotMap = map.getChild("slots");
        while (slotMap != null) {
            SlotData slot = skeletonData.findSlot(slotMap.name);
            if (slot == null) {
                throw new SerializationException("Slot not found: " + slotMap.name);
            }
            timelineMap = slotMap.child;
            while (timelineMap != null) {
                JsonValue valueMap3;
                int frameIndex;
                timelineName = timelineMap.name;
                if (timelineName.equals("attachment")) {
                    timeline2 = new Animation.AttachmentTimeline(timelineMap.size);
                    ((Animation.AttachmentTimeline)timeline2).slotIndex = slot.index;
                    frameIndex = 0;
                    valueMap3 = timelineMap.child;
                    while (valueMap3 != null) {
                        ((Animation.AttachmentTimeline)timeline2).setFrame(frameIndex++, valueMap3.getFloat("time", 0.0f), valueMap3.getString("name"));
                        valueMap3 = valueMap3.next;
                    }
                    timelines.add(timeline2);
                    duration = Math.max(duration, ((Animation.AttachmentTimeline)timeline2).getFrames()[((Animation.AttachmentTimeline)timeline2).getFrameCount() - 1]);
                } else if (timelineName.equals("color")) {
                    timeline2 = new Animation.ColorTimeline(timelineMap.size);
                    ((Animation.ColorTimeline)timeline2).slotIndex = slot.index;
                    frameIndex = 0;
                    valueMap3 = timelineMap.child;
                    while (valueMap3 != null) {
                        Color color = Color.valueOf(valueMap3.getString("color"));
                        ((Animation.ColorTimeline)timeline2).setFrame(frameIndex, valueMap3.getFloat("time", 0.0f), color.r, color.g, color.b, color.a);
                        this.readCurve(valueMap3, (Animation.CurveTimeline)timeline2, frameIndex);
                        ++frameIndex;
                        valueMap3 = valueMap3.next;
                    }
                    timelines.add(timeline2);
                    duration = Math.max(duration, ((Animation.ColorTimeline)timeline2).getFrames()[(((Animation.CurveTimeline)timeline2).getFrameCount() - 1) * 5]);
                } else if (timelineName.equals("twoColor")) {
                    timeline2 = new Animation.TwoColorTimeline(timelineMap.size);
                    ((Animation.TwoColorTimeline)timeline2).slotIndex = slot.index;
                    frameIndex = 0;
                    valueMap3 = timelineMap.child;
                    while (valueMap3 != null) {
                        Color light = Color.valueOf(valueMap3.getString("light"));
                        Color dark = Color.valueOf(valueMap3.getString("dark"));
                        ((Animation.TwoColorTimeline)timeline2).setFrame(frameIndex, valueMap3.getFloat("time", 0.0f), light.r, light.g, light.b, light.a, dark.r, dark.g, dark.b);
                        this.readCurve(valueMap3, (Animation.CurveTimeline)timeline2, frameIndex);
                        ++frameIndex;
                        valueMap3 = valueMap3.next;
                    }
                    timelines.add(timeline2);
                    duration = Math.max(duration, ((Animation.TwoColorTimeline)timeline2).getFrames()[(((Animation.CurveTimeline)timeline2).getFrameCount() - 1) * 8]);
                } else {
                    throw new RuntimeException("Invalid timeline type for a slot: " + timelineName + " (" + slotMap.name + ")");
                }
                timelineMap = timelineMap.next;
            }
            slotMap = slotMap.next;
        }
        JsonValue boneMap = map.getChild("bones");
        while (boneMap != null) {
            BoneData bone = skeletonData.findBone(boneMap.name);
            if (bone == null) {
                throw new SerializationException("Bone not found: " + boneMap.name);
            }
            timelineMap = boneMap.child;
            while (timelineMap != null) {
                timelineName = timelineMap.name;
                if (timelineName.equals("rotate")) {
                    timeline2 = new Animation.RotateTimeline(timelineMap.size);
                    ((Animation.RotateTimeline)timeline2).boneIndex = bone.index;
                    int frameIndex = 0;
                    JsonValue valueMap4 = timelineMap.child;
                    while (valueMap4 != null) {
                        ((Animation.RotateTimeline)timeline2).setFrame(frameIndex, valueMap4.getFloat("time", 0.0f), valueMap4.getFloat("angle", 0.0f));
                        this.readCurve(valueMap4, (Animation.CurveTimeline)timeline2, frameIndex);
                        ++frameIndex;
                        valueMap4 = valueMap4.next;
                    }
                    timelines.add(timeline2);
                    duration = Math.max(duration, ((Animation.RotateTimeline)timeline2).getFrames()[(((Animation.CurveTimeline)timeline2).getFrameCount() - 1) * 2]);
                } else if (timelineName.equals("translate") || timelineName.equals("scale") || timelineName.equals("shear")) {
                    float timelineScale = 1.0f;
                    float defaultValue = 0.0f;
                    if (timelineName.equals("scale")) {
                        timeline2 = new Animation.ScaleTimeline(timelineMap.size);
                        defaultValue = 1.0f;
                    } else if (timelineName.equals("shear")) {
                        timeline2 = new Animation.ShearTimeline(timelineMap.size);
                    } else {
                        timeline2 = new Animation.TranslateTimeline(timelineMap.size);
                        timelineScale = scale;
                    }
                    ((Animation.TranslateTimeline)timeline2).boneIndex = bone.index;
                    int frameIndex = 0;
                    valueMap2 = timelineMap.child;
                    while (valueMap2 != null) {
                        float x = valueMap2.getFloat("x", defaultValue);
                        float y = valueMap2.getFloat("y", defaultValue);
                        ((Animation.TranslateTimeline)timeline2).setFrame(frameIndex, valueMap2.getFloat("time", 0.0f), x * timelineScale, y * timelineScale);
                        this.readCurve(valueMap2, (Animation.CurveTimeline)timeline2, frameIndex);
                        ++frameIndex;
                        valueMap2 = valueMap2.next;
                    }
                    timelines.add(timeline2);
                    duration = Math.max(duration, ((Animation.TranslateTimeline)timeline2).getFrames()[(((Animation.CurveTimeline)timeline2).getFrameCount() - 1) * 3]);
                } else {
                    throw new RuntimeException("Invalid timeline type for a bone: " + timelineName + " (" + boneMap.name + ")");
                }
                timelineMap = timelineMap.next;
            }
            boneMap = boneMap.next;
        }
        JsonValue constraintMap = map.getChild("ik");
        while (constraintMap != null) {
            constraint = skeletonData.findIkConstraint(constraintMap.name);
            timeline = new Animation.IkConstraintTimeline(constraintMap.size);
            ((Animation.IkConstraintTimeline)timeline).ikConstraintIndex = skeletonData.getIkConstraints().indexOf((IkConstraintData)constraint, true);
            int frameIndex = 0;
            valueMap = constraintMap.child;
            while (valueMap != null) {
                ((Animation.IkConstraintTimeline)timeline).setFrame(frameIndex, valueMap.getFloat("time", 0.0f), valueMap.getFloat("mix", 1.0f), valueMap.getFloat("softness", 0.0f) * scale, valueMap.getBoolean("bendPositive", true) ? 1 : -1, valueMap.getBoolean("compress", false), valueMap.getBoolean("stretch", false));
                this.readCurve(valueMap, timeline, frameIndex);
                ++frameIndex;
                valueMap = valueMap.next;
            }
            timelines.add(timeline);
            duration = Math.max(duration, ((Animation.IkConstraintTimeline)timeline).getFrames()[(timeline.getFrameCount() - 1) * 6]);
            constraintMap = constraintMap.next;
        }
        constraintMap = map.getChild("transform");
        while (constraintMap != null) {
            constraint = skeletonData.findTransformConstraint(constraintMap.name);
            timeline = new Animation.TransformConstraintTimeline(constraintMap.size);
            ((Animation.TransformConstraintTimeline)timeline).transformConstraintIndex = skeletonData.getTransformConstraints().indexOf((TransformConstraintData)constraint, true);
            int frameIndex = 0;
            valueMap = constraintMap.child;
            while (valueMap != null) {
                ((Animation.TransformConstraintTimeline)timeline).setFrame(frameIndex, valueMap.getFloat("time", 0.0f), valueMap.getFloat("rotateMix", 1.0f), valueMap.getFloat("translateMix", 1.0f), valueMap.getFloat("scaleMix", 1.0f), valueMap.getFloat("shearMix", 1.0f));
                this.readCurve(valueMap, timeline, frameIndex);
                ++frameIndex;
                valueMap = valueMap.next;
            }
            timelines.add(timeline);
            duration = Math.max(duration, ((Animation.TransformConstraintTimeline)timeline).getFrames()[(timeline.getFrameCount() - 1) * 5]);
            constraintMap = constraintMap.next;
        }
        constraintMap = map.getChild("path");
        while (constraintMap != null) {
            PathConstraintData data = skeletonData.findPathConstraint(constraintMap.name);
            if (data == null) {
                throw new SerializationException("Path constraint not found: " + constraintMap.name);
            }
            int index = skeletonData.pathConstraints.indexOf(data, true);
            JsonValue timelineMap2 = constraintMap.child;
            while (timelineMap2 != null) {
                Animation.CurveTimeline timeline3;
                String timelineName2 = timelineMap2.name;
                if (timelineName2.equals("position") || timelineName2.equals("spacing")) {
                    float timelineScale = 1.0f;
                    if (timelineName2.equals("spacing")) {
                        timeline3 = new Animation.PathConstraintSpacingTimeline(timelineMap2.size);
                        if (data.spacingMode == PathConstraintData.SpacingMode.length || data.spacingMode == PathConstraintData.SpacingMode.fixed) {
                            timelineScale = scale;
                        }
                    } else {
                        timeline3 = new Animation.PathConstraintPositionTimeline(timelineMap2.size);
                        if (data.positionMode == PathConstraintData.PositionMode.fixed) {
                            timelineScale = scale;
                        }
                    }
                    ((Animation.PathConstraintPositionTimeline)timeline3).pathConstraintIndex = index;
                    int frameIndex = 0;
                    valueMap2 = timelineMap2.child;
                    while (valueMap2 != null) {
                        ((Animation.PathConstraintPositionTimeline)timeline3).setFrame(frameIndex, valueMap2.getFloat("time", 0.0f), valueMap2.getFloat(timelineName2, 0.0f) * timelineScale);
                        this.readCurve(valueMap2, timeline3, frameIndex);
                        ++frameIndex;
                        valueMap2 = valueMap2.next;
                    }
                    timelines.add(timeline3);
                    duration = Math.max(duration, ((Animation.PathConstraintPositionTimeline)timeline3).getFrames()[(timeline3.getFrameCount() - 1) * 2]);
                } else if (timelineName2.equals("mix")) {
                    timeline3 = new Animation.PathConstraintMixTimeline(timelineMap2.size);
                    ((Animation.PathConstraintMixTimeline)timeline3).pathConstraintIndex = index;
                    int frameIndex = 0;
                    JsonValue valueMap5 = timelineMap2.child;
                    while (valueMap5 != null) {
                        ((Animation.PathConstraintMixTimeline)timeline3).setFrame(frameIndex, valueMap5.getFloat("time", 0.0f), valueMap5.getFloat("rotateMix", 1.0f), valueMap5.getFloat("translateMix", 1.0f));
                        this.readCurve(valueMap5, timeline3, frameIndex);
                        ++frameIndex;
                        valueMap5 = valueMap5.next;
                    }
                    timelines.add(timeline3);
                    duration = Math.max(duration, ((Animation.PathConstraintMixTimeline)timeline3).getFrames()[(timeline3.getFrameCount() - 1) * 3]);
                }
                timelineMap2 = timelineMap2.next;
            }
            constraintMap = constraintMap.next;
        }
        JsonValue deformMap = map.getChild("deform");
        while (deformMap != null) {
            Skin skin = skeletonData.findSkin(deformMap.name);
            if (skin == null) {
                throw new SerializationException("Skin not found: " + deformMap.name);
            }
            JsonValue slotMap2 = deformMap.child;
            while (slotMap2 != null) {
                SlotData slot = skeletonData.findSlot(slotMap2.name);
                if (slot == null) {
                    throw new SerializationException("Slot not found: " + slotMap2.name);
                }
                JsonValue timelineMap3 = slotMap2.child;
                while (timelineMap3 != null) {
                    VertexAttachment attachment = (VertexAttachment)skin.getAttachment(slot.index, timelineMap3.name);
                    if (attachment == null) {
                        throw new SerializationException("Deform attachment not found: " + timelineMap3.name);
                    }
                    boolean weighted = attachment.getBones() != null;
                    float[] vertices = attachment.getVertices();
                    int deformLength = weighted ? vertices.length / 3 * 2 : vertices.length;
                    Animation.DeformTimeline timeline4 = new Animation.DeformTimeline(timelineMap3.size);
                    timeline4.slotIndex = slot.index;
                    timeline4.attachment = attachment;
                    int frameIndex = 0;
                    JsonValue valueMap6 = timelineMap3.child;
                    while (valueMap6 != null) {
                        float[] deform;
                        JsonValue verticesValue = valueMap6.get("vertices");
                        if (verticesValue == null) {
                            deform = weighted ? new float[deformLength] : vertices;
                        } else {
                            int i;
                            deform = new float[deformLength];
                            int start = valueMap6.getInt("offset", 0);
                            SpineUtils.arraycopy(verticesValue.asFloatArray(), 0, deform, start, verticesValue.size);
                            if (scale != 1.0f) {
                                i = start;
                                int n = i + verticesValue.size;
                                while (i < n) {
                                    int n2 = i++;
                                    deform[n2] = deform[n2] * scale;
                                }
                            }
                            if (!weighted) {
                                for (i = 0; i < deformLength; ++i) {
                                    int n = i;
                                    deform[n] = deform[n] + vertices[i];
                                }
                            }
                        }
                        timeline4.setFrame(frameIndex, valueMap6.getFloat("time", 0.0f), deform);
                        this.readCurve(valueMap6, timeline4, frameIndex);
                        ++frameIndex;
                        valueMap6 = valueMap6.next;
                    }
                    timelines.add(timeline4);
                    duration = Math.max(duration, timeline4.getFrames()[timeline4.getFrameCount() - 1]);
                    timelineMap3 = timelineMap3.next;
                }
                slotMap2 = slotMap2.next;
            }
            deformMap = deformMap.next;
        }
        JsonValue drawOrdersMap = map.get("drawOrder");
        if (drawOrdersMap == null) {
            drawOrdersMap = map.get("draworder");
        }
        if (drawOrdersMap != null) {
            Animation.DrawOrderTimeline timeline5 = new Animation.DrawOrderTimeline(drawOrdersMap.size);
            int slotCount = skeletonData.slots.size;
            int frameIndex = 0;
            JsonValue drawOrderMap = drawOrdersMap.child;
            while (drawOrderMap != null) {
                int[] drawOrder = null;
                JsonValue offsets = drawOrderMap.get("offsets");
                if (offsets != null) {
                    drawOrder = new int[slotCount];
                    for (int i = slotCount - 1; i >= 0; --i) {
                        drawOrder[i] = -1;
                    }
                    int[] unchanged = new int[slotCount - offsets.size];
                    int originalIndex = 0;
                    int unchangedIndex = 0;
                    JsonValue offsetMap = offsets.child;
                    while (offsetMap != null) {
                        SlotData slot = skeletonData.findSlot(offsetMap.getString("slot"));
                        if (slot == null) {
                            throw new SerializationException("Slot not found: " + offsetMap.getString("slot"));
                        }
                        while (originalIndex != slot.index) {
                            unchanged[unchangedIndex++] = originalIndex++;
                        }
                        drawOrder[originalIndex + offsetMap.getInt((String)"offset")] = originalIndex++;
                        offsetMap = offsetMap.next;
                    }
                    while (originalIndex < slotCount) {
                        unchanged[unchangedIndex++] = originalIndex++;
                    }
                    for (int i = slotCount - 1; i >= 0; --i) {
                        if (drawOrder[i] != -1) continue;
                        drawOrder[i] = unchanged[--unchangedIndex];
                    }
                }
                timeline5.setFrame(frameIndex++, drawOrderMap.getFloat("time", 0.0f), drawOrder);
                drawOrderMap = drawOrderMap.next;
            }
            timelines.add(timeline5);
            duration = Math.max(duration, timeline5.getFrames()[timeline5.getFrameCount() - 1]);
        }
        if ((eventsMap = map.get("events")) != null) {
            Animation.EventTimeline timeline6 = new Animation.EventTimeline(eventsMap.size);
            int frameIndex = 0;
            JsonValue eventMap = eventsMap.child;
            while (eventMap != null) {
                EventData eventData = skeletonData.findEvent(eventMap.getString("name"));
                if (eventData == null) {
                    throw new SerializationException("Event not found: " + eventMap.getString("name"));
                }
                Event event = new Event(eventMap.getFloat("time", 0.0f), eventData);
                event.intValue = eventMap.getInt("int", eventData.intValue);
                event.floatValue = eventMap.getFloat("float", eventData.floatValue);
                event.stringValue = eventMap.getString("string", eventData.stringValue);
                if (event.getData().audioPath != null) {
                    event.volume = eventMap.getFloat("volume", eventData.volume);
                    event.balance = eventMap.getFloat("balance", eventData.balance);
                }
                timeline6.setFrame(frameIndex++, event);
                eventMap = eventMap.next;
            }
            timelines.add(timeline6);
            duration = Math.max(duration, timeline6.getFrames()[timeline6.getFrameCount() - 1]);
        }
        timelines.shrink();
        skeletonData.animations.add(new Animation(name, timelines, duration));
    }

    void readCurve(JsonValue map, Animation.CurveTimeline timeline, int frameIndex) {
        JsonValue curve = map.get("curve");
        if (curve == null) {
            return;
        }
        if (curve.isString()) {
            timeline.setStepped(frameIndex);
        } else {
            timeline.setCurve(frameIndex, curve.asFloat(), map.getFloat("c2", 0.0f), map.getFloat("c3", 1.0f), map.getFloat("c4", 1.0f));
        }
    }

    static class LinkedMesh {
        String parent;
        String skin;
        int slotIndex;
        MeshAttachment mesh;
        boolean inheritDeform;

        public LinkedMesh(MeshAttachment mesh, String skin, int slotIndex, String parent, boolean inheritDeform) {
            this.mesh = mesh;
            this.skin = skin;
            this.slotIndex = slotIndex;
            this.parent = parent;
            this.inheritDeform = inheritDeform;
        }
    }
}

