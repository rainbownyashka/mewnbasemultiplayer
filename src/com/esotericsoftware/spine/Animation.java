/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.IkConstraint;
import com.esotericsoftware.spine.PathConstraint;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.TransformConstraint;
import com.esotericsoftware.spine.TransformConstraintData;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.VertexAttachment;
import com.esotericsoftware.spine.utils.SpineUtils;

public class Animation {
    final String name;
    final Array<Timeline> timelines;
    float duration;

    public Animation(String name, Array<Timeline> timelines, float duration) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null.");
        }
        if (timelines == null) {
            throw new IllegalArgumentException("timelines cannot be null.");
        }
        this.name = name;
        this.timelines = timelines;
        this.duration = duration;
    }

    public Array<Timeline> getTimelines() {
        return this.timelines;
    }

    public float getDuration() {
        return this.duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public void apply(Skeleton skeleton, float lastTime, float time, boolean loop, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
        if (skeleton == null) {
            throw new IllegalArgumentException("skeleton cannot be null.");
        }
        if (loop && this.duration != 0.0f) {
            time %= this.duration;
            if (lastTime > 0.0f) {
                lastTime %= this.duration;
            }
        }
        Array<Timeline> timelines = this.timelines;
        int n = timelines.size;
        for (int i = 0; i < n; ++i) {
            timelines.get(i).apply(skeleton, lastTime, time, events, alpha, blend, direction);
        }
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }

    static int binarySearch(float[] values, float target, int step) {
        int low = 0;
        int high = values.length / step - 2;
        if (high == 0) {
            return step;
        }
        int current = high >>> 1;
        while (true) {
            if (values[(current + 1) * step] <= target) {
                low = current + 1;
            } else {
                high = current;
            }
            if (low == high) {
                return (low + 1) * step;
            }
            current = low + high >>> 1;
        }
    }

    static int binarySearch(float[] values, float target) {
        int low = 0;
        int high = values.length - 2;
        if (high == 0) {
            return 1;
        }
        int current = high >>> 1;
        while (true) {
            if (values[current + 1] <= target) {
                low = current + 1;
            } else {
                high = current;
            }
            if (low == high) {
                return low + 1;
            }
            current = low + high >>> 1;
        }
    }

    static int linearSearch(float[] values, float target, int step) {
        int last = values.length - step;
        for (int i = 0; i <= last; i += step) {
            if (!(values[i] > target)) continue;
            return i;
        }
        return -1;
    }

    public static class PathConstraintMixTimeline
    extends CurveTimeline {
        public static final int ENTRIES = 3;
        private static final int PREV_TIME = -3;
        private static final int PREV_ROTATE = -2;
        private static final int PREV_TRANSLATE = -1;
        private static final int ROTATE = 1;
        private static final int TRANSLATE = 2;
        int pathConstraintIndex;
        private final float[] frames;

        public PathConstraintMixTimeline(int frameCount) {
            super(frameCount);
            this.frames = new float[frameCount * 3];
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.pathConstraintMix.ordinal() << 24) + this.pathConstraintIndex;
        }

        public void setPathConstraintIndex(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("index must be >= 0.");
            }
            this.pathConstraintIndex = index;
        }

        public int getPathConstraintIndex() {
            return this.pathConstraintIndex;
        }

        public float[] getFrames() {
            return this.frames;
        }

        public void setFrame(int frameIndex, float time, float rotateMix, float translateMix) {
            this.frames[frameIndex *= 3] = time;
            this.frames[frameIndex + 1] = rotateMix;
            this.frames[frameIndex + 2] = translateMix;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            float translate;
            float rotate;
            PathConstraint constraint = skeleton.pathConstraints.get(this.pathConstraintIndex);
            if (!constraint.active) {
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                switch (blend) {
                    case setup: {
                        constraint.rotateMix = constraint.data.rotateMix;
                        constraint.translateMix = constraint.data.translateMix;
                        return;
                    }
                    case first: {
                        constraint.rotateMix += (constraint.data.rotateMix - constraint.rotateMix) * alpha;
                        constraint.translateMix += (constraint.data.translateMix - constraint.translateMix) * alpha;
                    }
                }
                return;
            }
            if (time >= frames[frames.length - 3]) {
                rotate = frames[frames.length + -2];
                translate = frames[frames.length + -1];
            } else {
                int frame = Animation.binarySearch(frames, time, 3);
                rotate = frames[frame + -2];
                translate = frames[frame + -1];
                float frameTime = frames[frame];
                float percent = this.getCurvePercent(frame / 3 - 1, 1.0f - (time - frameTime) / (frames[frame + -3] - frameTime));
                rotate += (frames[frame + 1] - rotate) * percent;
                translate += (frames[frame + 2] - translate) * percent;
            }
            if (blend == MixBlend.setup) {
                constraint.rotateMix = constraint.data.rotateMix + (rotate - constraint.data.rotateMix) * alpha;
                constraint.translateMix = constraint.data.translateMix + (translate - constraint.data.translateMix) * alpha;
            } else {
                constraint.rotateMix += (rotate - constraint.rotateMix) * alpha;
                constraint.translateMix += (translate - constraint.translateMix) * alpha;
            }
        }
    }

    public static class PathConstraintSpacingTimeline
    extends PathConstraintPositionTimeline {
        public PathConstraintSpacingTimeline(int frameCount) {
            super(frameCount);
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.pathConstraintSpacing.ordinal() << 24) + this.pathConstraintIndex;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            float spacing;
            PathConstraint constraint = skeleton.pathConstraints.get(this.pathConstraintIndex);
            if (!constraint.active) {
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                switch (blend) {
                    case setup: {
                        constraint.spacing = constraint.data.spacing;
                        return;
                    }
                    case first: {
                        constraint.spacing += (constraint.data.spacing - constraint.spacing) * alpha;
                    }
                }
                return;
            }
            if (time >= frames[frames.length - 2]) {
                spacing = frames[frames.length + -1];
            } else {
                int frame = Animation.binarySearch(frames, time, 2);
                spacing = frames[frame + -1];
                float frameTime = frames[frame];
                float percent = this.getCurvePercent(frame / 2 - 1, 1.0f - (time - frameTime) / (frames[frame + -2] - frameTime));
                spacing += (frames[frame + 1] - spacing) * percent;
            }
            constraint.spacing = blend == MixBlend.setup ? constraint.data.spacing + (spacing - constraint.data.spacing) * alpha : (constraint.spacing += (spacing - constraint.spacing) * alpha);
        }
    }

    public static class PathConstraintPositionTimeline
    extends CurveTimeline {
        public static final int ENTRIES = 2;
        static final int PREV_TIME = -2;
        static final int PREV_VALUE = -1;
        static final int VALUE = 1;
        int pathConstraintIndex;
        final float[] frames;

        public PathConstraintPositionTimeline(int frameCount) {
            super(frameCount);
            this.frames = new float[frameCount * 2];
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.pathConstraintPosition.ordinal() << 24) + this.pathConstraintIndex;
        }

        public void setPathConstraintIndex(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("index must be >= 0.");
            }
            this.pathConstraintIndex = index;
        }

        public int getPathConstraintIndex() {
            return this.pathConstraintIndex;
        }

        public float[] getFrames() {
            return this.frames;
        }

        public void setFrame(int frameIndex, float time, float position) {
            this.frames[frameIndex *= 2] = time;
            this.frames[frameIndex + 1] = position;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            float position;
            PathConstraint constraint = skeleton.pathConstraints.get(this.pathConstraintIndex);
            if (!constraint.active) {
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                switch (blend) {
                    case setup: {
                        constraint.position = constraint.data.position;
                        return;
                    }
                    case first: {
                        constraint.position += (constraint.data.position - constraint.position) * alpha;
                    }
                }
                return;
            }
            if (time >= frames[frames.length - 2]) {
                position = frames[frames.length + -1];
            } else {
                int frame = Animation.binarySearch(frames, time, 2);
                position = frames[frame + -1];
                float frameTime = frames[frame];
                float percent = this.getCurvePercent(frame / 2 - 1, 1.0f - (time - frameTime) / (frames[frame + -2] - frameTime));
                position += (frames[frame + 1] - position) * percent;
            }
            constraint.position = blend == MixBlend.setup ? constraint.data.position + (position - constraint.data.position) * alpha : (constraint.position += (position - constraint.position) * alpha);
        }
    }

    public static class TransformConstraintTimeline
    extends CurveTimeline {
        public static final int ENTRIES = 5;
        private static final int PREV_TIME = -5;
        private static final int PREV_ROTATE = -4;
        private static final int PREV_TRANSLATE = -3;
        private static final int PREV_SCALE = -2;
        private static final int PREV_SHEAR = -1;
        private static final int ROTATE = 1;
        private static final int TRANSLATE = 2;
        private static final int SCALE = 3;
        private static final int SHEAR = 4;
        int transformConstraintIndex;
        private final float[] frames;

        public TransformConstraintTimeline(int frameCount) {
            super(frameCount);
            this.frames = new float[frameCount * 5];
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.transformConstraint.ordinal() << 24) + this.transformConstraintIndex;
        }

        public void setTransformConstraintIndex(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("index must be >= 0.");
            }
            this.transformConstraintIndex = index;
        }

        public int getTransformConstraintIndex() {
            return this.transformConstraintIndex;
        }

        public float[] getFrames() {
            return this.frames;
        }

        public void setFrame(int frameIndex, float time, float rotateMix, float translateMix, float scaleMix, float shearMix) {
            this.frames[frameIndex *= 5] = time;
            this.frames[frameIndex + 1] = rotateMix;
            this.frames[frameIndex + 2] = translateMix;
            this.frames[frameIndex + 3] = scaleMix;
            this.frames[frameIndex + 4] = shearMix;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            float shear;
            float scale;
            float translate;
            float rotate;
            TransformConstraint constraint = skeleton.transformConstraints.get(this.transformConstraintIndex);
            if (!constraint.active) {
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                TransformConstraintData data = constraint.data;
                switch (blend) {
                    case setup: {
                        constraint.rotateMix = data.rotateMix;
                        constraint.translateMix = data.translateMix;
                        constraint.scaleMix = data.scaleMix;
                        constraint.shearMix = data.shearMix;
                        return;
                    }
                    case first: {
                        constraint.rotateMix += (data.rotateMix - constraint.rotateMix) * alpha;
                        constraint.translateMix += (data.translateMix - constraint.translateMix) * alpha;
                        constraint.scaleMix += (data.scaleMix - constraint.scaleMix) * alpha;
                        constraint.shearMix += (data.shearMix - constraint.shearMix) * alpha;
                    }
                }
                return;
            }
            if (time >= frames[frames.length - 5]) {
                int i = frames.length;
                rotate = frames[i + -4];
                translate = frames[i + -3];
                scale = frames[i + -2];
                shear = frames[i + -1];
            } else {
                int frame = Animation.binarySearch(frames, time, 5);
                rotate = frames[frame + -4];
                translate = frames[frame + -3];
                scale = frames[frame + -2];
                shear = frames[frame + -1];
                float frameTime = frames[frame];
                float percent = this.getCurvePercent(frame / 5 - 1, 1.0f - (time - frameTime) / (frames[frame + -5] - frameTime));
                rotate += (frames[frame + 1] - rotate) * percent;
                translate += (frames[frame + 2] - translate) * percent;
                scale += (frames[frame + 3] - scale) * percent;
                shear += (frames[frame + 4] - shear) * percent;
            }
            if (blend == MixBlend.setup) {
                TransformConstraintData data = constraint.data;
                constraint.rotateMix = data.rotateMix + (rotate - data.rotateMix) * alpha;
                constraint.translateMix = data.translateMix + (translate - data.translateMix) * alpha;
                constraint.scaleMix = data.scaleMix + (scale - data.scaleMix) * alpha;
                constraint.shearMix = data.shearMix + (shear - data.shearMix) * alpha;
            } else {
                constraint.rotateMix += (rotate - constraint.rotateMix) * alpha;
                constraint.translateMix += (translate - constraint.translateMix) * alpha;
                constraint.scaleMix += (scale - constraint.scaleMix) * alpha;
                constraint.shearMix += (shear - constraint.shearMix) * alpha;
            }
        }
    }

    public static class IkConstraintTimeline
    extends CurveTimeline {
        public static final int ENTRIES = 6;
        private static final int PREV_TIME = -6;
        private static final int PREV_MIX = -5;
        private static final int PREV_SOFTNESS = -4;
        private static final int PREV_BEND_DIRECTION = -3;
        private static final int PREV_COMPRESS = -2;
        private static final int PREV_STRETCH = -1;
        private static final int MIX = 1;
        private static final int SOFTNESS = 2;
        private static final int BEND_DIRECTION = 3;
        private static final int COMPRESS = 4;
        private static final int STRETCH = 5;
        int ikConstraintIndex;
        private final float[] frames;

        public IkConstraintTimeline(int frameCount) {
            super(frameCount);
            this.frames = new float[frameCount * 6];
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.ikConstraint.ordinal() << 24) + this.ikConstraintIndex;
        }

        public void setIkConstraintIndex(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("index must be >= 0.");
            }
            this.ikConstraintIndex = index;
        }

        public int getIkConstraintIndex() {
            return this.ikConstraintIndex;
        }

        public float[] getFrames() {
            return this.frames;
        }

        public void setFrame(int frameIndex, float time, float mix, float softness, int bendDirection, boolean compress, boolean stretch) {
            this.frames[frameIndex *= 6] = time;
            this.frames[frameIndex + 1] = mix;
            this.frames[frameIndex + 2] = softness;
            this.frames[frameIndex + 3] = bendDirection;
            this.frames[frameIndex + 4] = compress ? 1.0f : 0.0f;
            this.frames[frameIndex + 5] = stretch ? 1.0f : 0.0f;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            IkConstraint constraint = skeleton.ikConstraints.get(this.ikConstraintIndex);
            if (!constraint.active) {
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                switch (blend) {
                    case setup: {
                        constraint.mix = constraint.data.mix;
                        constraint.softness = constraint.data.softness;
                        constraint.bendDirection = constraint.data.bendDirection;
                        constraint.compress = constraint.data.compress;
                        constraint.stretch = constraint.data.stretch;
                        return;
                    }
                    case first: {
                        constraint.mix += (constraint.data.mix - constraint.mix) * alpha;
                        constraint.softness += (constraint.data.softness - constraint.softness) * alpha;
                        constraint.bendDirection = constraint.data.bendDirection;
                        constraint.compress = constraint.data.compress;
                        constraint.stretch = constraint.data.stretch;
                    }
                }
                return;
            }
            if (time >= frames[frames.length - 6]) {
                if (blend == MixBlend.setup) {
                    constraint.mix = constraint.data.mix + (frames[frames.length + -5] - constraint.data.mix) * alpha;
                    constraint.softness = constraint.data.softness + (frames[frames.length + -4] - constraint.data.softness) * alpha;
                    if (direction == MixDirection.out) {
                        constraint.bendDirection = constraint.data.bendDirection;
                        constraint.compress = constraint.data.compress;
                        constraint.stretch = constraint.data.stretch;
                    } else {
                        constraint.bendDirection = (int)frames[frames.length + -3];
                        constraint.compress = frames[frames.length + -2] != 0.0f;
                        constraint.stretch = frames[frames.length + -1] != 0.0f;
                    }
                } else {
                    constraint.mix += (frames[frames.length + -5] - constraint.mix) * alpha;
                    constraint.softness += (frames[frames.length + -4] - constraint.softness) * alpha;
                    if (direction == MixDirection.in) {
                        constraint.bendDirection = (int)frames[frames.length + -3];
                        constraint.compress = frames[frames.length + -2] != 0.0f;
                        constraint.stretch = frames[frames.length + -1] != 0.0f;
                    }
                }
                return;
            }
            int frame = Animation.binarySearch(frames, time, 6);
            float mix = frames[frame + -5];
            float softness = frames[frame + -4];
            float frameTime = frames[frame];
            float percent = this.getCurvePercent(frame / 6 - 1, 1.0f - (time - frameTime) / (frames[frame + -6] - frameTime));
            if (blend == MixBlend.setup) {
                constraint.mix = constraint.data.mix + (mix + (frames[frame + 1] - mix) * percent - constraint.data.mix) * alpha;
                constraint.softness = constraint.data.softness + (softness + (frames[frame + 2] - softness) * percent - constraint.data.softness) * alpha;
                if (direction == MixDirection.out) {
                    constraint.bendDirection = constraint.data.bendDirection;
                    constraint.compress = constraint.data.compress;
                    constraint.stretch = constraint.data.stretch;
                } else {
                    constraint.bendDirection = (int)frames[frame + -3];
                    constraint.compress = frames[frame + -2] != 0.0f;
                    constraint.stretch = frames[frame + -1] != 0.0f;
                }
            } else {
                constraint.mix += (mix + (frames[frame + 1] - mix) * percent - constraint.mix) * alpha;
                constraint.softness += (softness + (frames[frame + 2] - softness) * percent - constraint.softness) * alpha;
                if (direction == MixDirection.in) {
                    constraint.bendDirection = (int)frames[frame + -3];
                    constraint.compress = frames[frame + -2] != 0.0f;
                    constraint.stretch = frames[frame + -1] != 0.0f;
                }
            }
        }
    }

    public static class DrawOrderTimeline
    implements Timeline {
        private final float[] frames;
        private final int[][] drawOrders;

        public DrawOrderTimeline(int frameCount) {
            if (frameCount <= 0) {
                throw new IllegalArgumentException("frameCount must be > 0: " + frameCount);
            }
            this.frames = new float[frameCount];
            this.drawOrders = new int[frameCount][];
        }

        @Override
        public int getPropertyId() {
            return TimelineType.drawOrder.ordinal() << 24;
        }

        public int getFrameCount() {
            return this.frames.length;
        }

        public float[] getFrames() {
            return this.frames;
        }

        public int[][] getDrawOrders() {
            return this.drawOrders;
        }

        public void setFrame(int frameIndex, float time, int[] drawOrder) {
            this.frames[frameIndex] = time;
            this.drawOrders[frameIndex] = drawOrder;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            Array<Slot> drawOrder = skeleton.drawOrder;
            Array<Slot> slots = skeleton.slots;
            if (direction == MixDirection.out && blend == MixBlend.setup) {
                SpineUtils.arraycopy(slots.items, 0, drawOrder.items, 0, slots.size);
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                if (blend == MixBlend.setup || blend == MixBlend.first) {
                    SpineUtils.arraycopy(slots.items, 0, drawOrder.items, 0, slots.size);
                }
                return;
            }
            int frame = time >= frames[frames.length - 1] ? frames.length - 1 : Animation.binarySearch(frames, time) - 1;
            int[] drawOrderToSetupIndex = this.drawOrders[frame];
            if (drawOrderToSetupIndex == null) {
                SpineUtils.arraycopy(slots.items, 0, drawOrder.items, 0, slots.size);
            } else {
                int n = drawOrderToSetupIndex.length;
                for (int i = 0; i < n; ++i) {
                    drawOrder.set(i, slots.get(drawOrderToSetupIndex[i]));
                }
            }
        }
    }

    public static class EventTimeline
    implements Timeline {
        private final float[] frames;
        private final Event[] events;

        public EventTimeline(int frameCount) {
            if (frameCount <= 0) {
                throw new IllegalArgumentException("frameCount must be > 0: " + frameCount);
            }
            this.frames = new float[frameCount];
            this.events = new Event[frameCount];
        }

        @Override
        public int getPropertyId() {
            return TimelineType.event.ordinal() << 24;
        }

        public int getFrameCount() {
            return this.frames.length;
        }

        public float[] getFrames() {
            return this.frames;
        }

        public Event[] getEvents() {
            return this.events;
        }

        public void setFrame(int frameIndex, Event event) {
            this.frames[frameIndex] = event.time;
            this.events[frameIndex] = event;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> firedEvents, float alpha, MixBlend blend, MixDirection direction) {
            int frame;
            if (firedEvents == null) {
                return;
            }
            float[] frames = this.frames;
            int frameCount = frames.length;
            if (lastTime > time) {
                this.apply(skeleton, lastTime, 2.1474836E9f, firedEvents, alpha, blend, direction);
                lastTime = -1.0f;
            } else if (lastTime >= frames[frameCount - 1]) {
                return;
            }
            if (time < frames[0]) {
                return;
            }
            if (lastTime < frames[0]) {
                frame = 0;
            } else {
                float frameTime = frames[frame];
                for (frame = Animation.binarySearch(frames, lastTime); frame > 0 && frames[frame - 1] == frameTime; --frame) {
                }
            }
            while (frame < frameCount && time >= frames[frame]) {
                firedEvents.add(this.events[frame]);
                ++frame;
            }
        }
    }

    public static class DeformTimeline
    extends CurveTimeline
    implements SlotTimeline {
        int slotIndex;
        VertexAttachment attachment;
        private final float[] frames;
        private final float[][] frameVertices;

        public DeformTimeline(int frameCount) {
            super(frameCount);
            this.frames = new float[frameCount];
            this.frameVertices = new float[frameCount][];
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.deform.ordinal() << 27) + this.attachment.getId() + this.slotIndex;
        }

        @Override
        public void setSlotIndex(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("index must be >= 0.");
            }
            this.slotIndex = index;
        }

        @Override
        public int getSlotIndex() {
            return this.slotIndex;
        }

        public void setAttachment(VertexAttachment attachment) {
            this.attachment = attachment;
        }

        public VertexAttachment getAttachment() {
            return this.attachment;
        }

        public float[] getFrames() {
            return this.frames;
        }

        public float[][] getVertices() {
            return this.frameVertices;
        }

        public void setFrame(int frameIndex, float time, float[] vertices) {
            this.frames[frameIndex] = time;
            this.frameVertices[frameIndex] = vertices;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            Slot slot = skeleton.slots.get(this.slotIndex);
            if (!slot.bone.active) {
                return;
            }
            Attachment slotAttachment = slot.attachment;
            if (!(slotAttachment instanceof VertexAttachment) || ((VertexAttachment)slotAttachment).getDeformAttachment() != this.attachment) {
                return;
            }
            FloatArray deformArray = slot.getDeform();
            if (deformArray.size == 0) {
                blend = MixBlend.setup;
            }
            float[][] frameVertices = this.frameVertices;
            int vertexCount = frameVertices[0].length;
            float[] frames = this.frames;
            if (time < frames[0]) {
                VertexAttachment vertexAttachment = (VertexAttachment)slotAttachment;
                switch (blend) {
                    case setup: {
                        deformArray.clear();
                        return;
                    }
                    case first: {
                        if (alpha == 1.0f) {
                            deformArray.clear();
                            return;
                        }
                        float[] deform = deformArray.setSize(vertexCount);
                        if (vertexAttachment.getBones() == null) {
                            float[] setupVertices = vertexAttachment.getVertices();
                            for (int i = 0; i < vertexCount; ++i) {
                                int n = i;
                                deform[n] = deform[n] + (setupVertices[i] - deform[i]) * alpha;
                            }
                        } else {
                            alpha = 1.0f - alpha;
                            int i = 0;
                            while (i < vertexCount) {
                                int n = i++;
                                deform[n] = deform[n] * alpha;
                            }
                        }
                        break;
                    }
                }
                return;
            }
            float[] deform = deformArray.setSize(vertexCount);
            if (time >= frames[frames.length - 1]) {
                float[] lastVertices = frameVertices[frames.length - 1];
                if (alpha == 1.0f) {
                    if (blend == MixBlend.add) {
                        VertexAttachment vertexAttachment = (VertexAttachment)slotAttachment;
                        if (vertexAttachment.getBones() == null) {
                            float[] setupVertices = vertexAttachment.getVertices();
                            for (int i = 0; i < vertexCount; ++i) {
                                int n = i;
                                deform[n] = deform[n] + (lastVertices[i] - setupVertices[i]);
                            }
                        } else {
                            for (int i = 0; i < vertexCount; ++i) {
                                int n = i;
                                deform[n] = deform[n] + lastVertices[i];
                            }
                        }
                    } else {
                        SpineUtils.arraycopy(lastVertices, 0, deform, 0, vertexCount);
                    }
                } else {
                    switch (blend) {
                        case setup: {
                            VertexAttachment vertexAttachment = (VertexAttachment)slotAttachment;
                            if (vertexAttachment.getBones() == null) {
                                float[] setupVertices = vertexAttachment.getVertices();
                                for (int i = 0; i < vertexCount; ++i) {
                                    float setup = setupVertices[i];
                                    deform[i] = setup + (lastVertices[i] - setup) * alpha;
                                }
                            } else {
                                for (int i = 0; i < vertexCount; ++i) {
                                    deform[i] = lastVertices[i] * alpha;
                                }
                            }
                            break;
                        }
                        case first: 
                        case replace: {
                            for (int i = 0; i < vertexCount; ++i) {
                                int n = i;
                                deform[n] = deform[n] + (lastVertices[i] - deform[i]) * alpha;
                            }
                            break;
                        }
                        case add: {
                            VertexAttachment vertexAttachment = (VertexAttachment)slotAttachment;
                            if (vertexAttachment.getBones() == null) {
                                float[] setupVertices = vertexAttachment.getVertices();
                                for (int i = 0; i < vertexCount; ++i) {
                                    int n = i;
                                    deform[n] = deform[n] + (lastVertices[i] - setupVertices[i]) * alpha;
                                }
                            } else {
                                for (int i = 0; i < vertexCount; ++i) {
                                    int n = i;
                                    deform[n] = deform[n] + lastVertices[i] * alpha;
                                }
                            }
                            break;
                        }
                    }
                }
                return;
            }
            int frame = Animation.binarySearch(frames, time);
            float[] prevVertices = frameVertices[frame - 1];
            float[] nextVertices = frameVertices[frame];
            float frameTime = frames[frame];
            float percent = this.getCurvePercent(frame - 1, 1.0f - (time - frameTime) / (frames[frame - 1] - frameTime));
            if (alpha == 1.0f) {
                if (blend == MixBlend.add) {
                    VertexAttachment vertexAttachment = (VertexAttachment)slotAttachment;
                    if (vertexAttachment.getBones() == null) {
                        float[] setupVertices = vertexAttachment.getVertices();
                        for (int i = 0; i < vertexCount; ++i) {
                            float prev = prevVertices[i];
                            int n = i;
                            deform[n] = deform[n] + (prev + (nextVertices[i] - prev) * percent - setupVertices[i]);
                        }
                    } else {
                        for (int i = 0; i < vertexCount; ++i) {
                            float prev = prevVertices[i];
                            int n = i;
                            deform[n] = deform[n] + (prev + (nextVertices[i] - prev) * percent);
                        }
                    }
                } else {
                    for (int i = 0; i < vertexCount; ++i) {
                        float prev = prevVertices[i];
                        deform[i] = prev + (nextVertices[i] - prev) * percent;
                    }
                }
            } else {
                switch (blend) {
                    case setup: {
                        VertexAttachment vertexAttachment = (VertexAttachment)slotAttachment;
                        if (vertexAttachment.getBones() == null) {
                            float[] setupVertices = vertexAttachment.getVertices();
                            for (int i = 0; i < vertexCount; ++i) {
                                float prev = prevVertices[i];
                                float setup = setupVertices[i];
                                deform[i] = setup + (prev + (nextVertices[i] - prev) * percent - setup) * alpha;
                            }
                        } else {
                            for (int i = 0; i < vertexCount; ++i) {
                                float prev = prevVertices[i];
                                deform[i] = (prev + (nextVertices[i] - prev) * percent) * alpha;
                            }
                        }
                        break;
                    }
                    case first: 
                    case replace: {
                        for (int i = 0; i < vertexCount; ++i) {
                            float prev = prevVertices[i];
                            int n = i;
                            deform[n] = deform[n] + (prev + (nextVertices[i] - prev) * percent - deform[i]) * alpha;
                        }
                        break;
                    }
                    case add: {
                        VertexAttachment vertexAttachment = (VertexAttachment)slotAttachment;
                        if (vertexAttachment.getBones() == null) {
                            float[] setupVertices = vertexAttachment.getVertices();
                            for (int i = 0; i < vertexCount; ++i) {
                                float prev = prevVertices[i];
                                int n = i;
                                deform[n] = deform[n] + (prev + (nextVertices[i] - prev) * percent - setupVertices[i]) * alpha;
                            }
                        } else {
                            for (int i = 0; i < vertexCount; ++i) {
                                float prev = prevVertices[i];
                                int n = i;
                                deform[n] = deform[n] + (prev + (nextVertices[i] - prev) * percent) * alpha;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    public static class AttachmentTimeline
    implements SlotTimeline {
        int slotIndex;
        final float[] frames;
        final String[] attachmentNames;

        public AttachmentTimeline(int frameCount) {
            if (frameCount <= 0) {
                throw new IllegalArgumentException("frameCount must be > 0: " + frameCount);
            }
            this.frames = new float[frameCount];
            this.attachmentNames = new String[frameCount];
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.attachment.ordinal() << 24) + this.slotIndex;
        }

        public int getFrameCount() {
            return this.frames.length;
        }

        @Override
        public void setSlotIndex(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("index must be >= 0.");
            }
            this.slotIndex = index;
        }

        @Override
        public int getSlotIndex() {
            return this.slotIndex;
        }

        public float[] getFrames() {
            return this.frames;
        }

        public String[] getAttachmentNames() {
            return this.attachmentNames;
        }

        public void setFrame(int frameIndex, float time, String attachmentName) {
            this.frames[frameIndex] = time;
            this.attachmentNames[frameIndex] = attachmentName;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            Slot slot = skeleton.slots.get(this.slotIndex);
            if (!slot.bone.active) {
                return;
            }
            if (direction == MixDirection.out && blend == MixBlend.setup) {
                String attachmentName = slot.data.attachmentName;
                slot.setAttachment(attachmentName == null ? null : skeleton.getAttachment(this.slotIndex, attachmentName));
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                if (blend == MixBlend.setup || blend == MixBlend.first) {
                    String attachmentName = slot.data.attachmentName;
                    slot.setAttachment(attachmentName == null ? null : skeleton.getAttachment(this.slotIndex, attachmentName));
                }
                return;
            }
            int frameIndex = time >= frames[frames.length - 1] ? frames.length - 1 : Animation.binarySearch(frames, time) - 1;
            String attachmentName = this.attachmentNames[frameIndex];
            slot.setAttachment(attachmentName == null ? null : skeleton.getAttachment(this.slotIndex, attachmentName));
        }
    }

    public static class TwoColorTimeline
    extends CurveTimeline
    implements SlotTimeline {
        public static final int ENTRIES = 8;
        private static final int PREV_TIME = -8;
        private static final int PREV_R = -7;
        private static final int PREV_G = -6;
        private static final int PREV_B = -5;
        private static final int PREV_A = -4;
        private static final int PREV_R2 = -3;
        private static final int PREV_G2 = -2;
        private static final int PREV_B2 = -1;
        private static final int R = 1;
        private static final int G = 2;
        private static final int B = 3;
        private static final int A = 4;
        private static final int R2 = 5;
        private static final int G2 = 6;
        private static final int B2 = 7;
        int slotIndex;
        private final float[] frames;

        public TwoColorTimeline(int frameCount) {
            super(frameCount);
            this.frames = new float[frameCount * 8];
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.twoColor.ordinal() << 24) + this.slotIndex;
        }

        @Override
        public void setSlotIndex(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("index must be >= 0.");
            }
            this.slotIndex = index;
        }

        @Override
        public int getSlotIndex() {
            return this.slotIndex;
        }

        public float[] getFrames() {
            return this.frames;
        }

        public void setFrame(int frameIndex, float time, float r, float g, float b, float a, float r2, float g2, float b2) {
            this.frames[frameIndex *= 8] = time;
            this.frames[frameIndex + 1] = r;
            this.frames[frameIndex + 2] = g;
            this.frames[frameIndex + 3] = b;
            this.frames[frameIndex + 4] = a;
            this.frames[frameIndex + 5] = r2;
            this.frames[frameIndex + 6] = g2;
            this.frames[frameIndex + 7] = b2;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            float b2;
            float g2;
            float r2;
            float a;
            float b;
            float g;
            float r;
            Slot slot = skeleton.slots.get(this.slotIndex);
            if (!slot.bone.active) {
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                switch (blend) {
                    case setup: {
                        slot.color.set(slot.data.color);
                        slot.darkColor.set(slot.data.darkColor);
                        return;
                    }
                    case first: {
                        Color light = slot.color;
                        Color dark = slot.darkColor;
                        Color setupLight = slot.data.color;
                        Color setupDark = slot.data.darkColor;
                        light.add((setupLight.r - light.r) * alpha, (setupLight.g - light.g) * alpha, (setupLight.b - light.b) * alpha, (setupLight.a - light.a) * alpha);
                        dark.add((setupDark.r - dark.r) * alpha, (setupDark.g - dark.g) * alpha, (setupDark.b - dark.b) * alpha, 0.0f);
                    }
                }
                return;
            }
            if (time >= frames[frames.length - 8]) {
                int i = frames.length;
                r = frames[i + -7];
                g = frames[i + -6];
                b = frames[i + -5];
                a = frames[i + -4];
                r2 = frames[i + -3];
                g2 = frames[i + -2];
                b2 = frames[i + -1];
            } else {
                int frame = Animation.binarySearch(frames, time, 8);
                r = frames[frame + -7];
                g = frames[frame + -6];
                b = frames[frame + -5];
                a = frames[frame + -4];
                r2 = frames[frame + -3];
                g2 = frames[frame + -2];
                b2 = frames[frame + -1];
                float frameTime = frames[frame];
                float percent = this.getCurvePercent(frame / 8 - 1, 1.0f - (time - frameTime) / (frames[frame + -8] - frameTime));
                r += (frames[frame + 1] - r) * percent;
                g += (frames[frame + 2] - g) * percent;
                b += (frames[frame + 3] - b) * percent;
                a += (frames[frame + 4] - a) * percent;
                r2 += (frames[frame + 5] - r2) * percent;
                g2 += (frames[frame + 6] - g2) * percent;
                b2 += (frames[frame + 7] - b2) * percent;
            }
            if (alpha == 1.0f) {
                slot.color.set(r, g, b, a);
                slot.darkColor.set(r2, g2, b2, 1.0f);
            } else {
                Color light = slot.color;
                Color dark = slot.darkColor;
                if (blend == MixBlend.setup) {
                    light.set(slot.data.color);
                    dark.set(slot.data.darkColor);
                }
                light.add((r - light.r) * alpha, (g - light.g) * alpha, (b - light.b) * alpha, (a - light.a) * alpha);
                dark.add((r2 - dark.r) * alpha, (g2 - dark.g) * alpha, (b2 - dark.b) * alpha, 0.0f);
            }
        }
    }

    public static class ColorTimeline
    extends CurveTimeline
    implements SlotTimeline {
        public static final int ENTRIES = 5;
        private static final int PREV_TIME = -5;
        private static final int PREV_R = -4;
        private static final int PREV_G = -3;
        private static final int PREV_B = -2;
        private static final int PREV_A = -1;
        private static final int R = 1;
        private static final int G = 2;
        private static final int B = 3;
        private static final int A = 4;
        int slotIndex;
        private final float[] frames;

        public ColorTimeline(int frameCount) {
            super(frameCount);
            this.frames = new float[frameCount * 5];
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.color.ordinal() << 24) + this.slotIndex;
        }

        @Override
        public void setSlotIndex(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("index must be >= 0.");
            }
            this.slotIndex = index;
        }

        @Override
        public int getSlotIndex() {
            return this.slotIndex;
        }

        public float[] getFrames() {
            return this.frames;
        }

        public void setFrame(int frameIndex, float time, float r, float g, float b, float a) {
            this.frames[frameIndex *= 5] = time;
            this.frames[frameIndex + 1] = r;
            this.frames[frameIndex + 2] = g;
            this.frames[frameIndex + 3] = b;
            this.frames[frameIndex + 4] = a;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            float a;
            float b;
            float g;
            float r;
            Slot slot = skeleton.slots.get(this.slotIndex);
            if (!slot.bone.active) {
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                switch (blend) {
                    case setup: {
                        slot.color.set(slot.data.color);
                        return;
                    }
                    case first: {
                        Color color = slot.color;
                        Color setup = slot.data.color;
                        color.add((setup.r - color.r) * alpha, (setup.g - color.g) * alpha, (setup.b - color.b) * alpha, (setup.a - color.a) * alpha);
                    }
                }
                return;
            }
            if (time >= frames[frames.length - 5]) {
                int i = frames.length;
                r = frames[i + -4];
                g = frames[i + -3];
                b = frames[i + -2];
                a = frames[i + -1];
            } else {
                int frame = Animation.binarySearch(frames, time, 5);
                r = frames[frame + -4];
                g = frames[frame + -3];
                b = frames[frame + -2];
                a = frames[frame + -1];
                float frameTime = frames[frame];
                float percent = this.getCurvePercent(frame / 5 - 1, 1.0f - (time - frameTime) / (frames[frame + -5] - frameTime));
                r += (frames[frame + 1] - r) * percent;
                g += (frames[frame + 2] - g) * percent;
                b += (frames[frame + 3] - b) * percent;
                a += (frames[frame + 4] - a) * percent;
            }
            if (alpha == 1.0f) {
                slot.color.set(r, g, b, a);
            } else {
                Color color = slot.color;
                if (blend == MixBlend.setup) {
                    color.set(slot.data.color);
                }
                color.add((r - color.r) * alpha, (g - color.g) * alpha, (b - color.b) * alpha, (a - color.a) * alpha);
            }
        }
    }

    public static class ShearTimeline
    extends TranslateTimeline {
        public ShearTimeline(int frameCount) {
            super(frameCount);
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.shear.ordinal() << 24) + this.boneIndex;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            float y;
            float x;
            Bone bone = skeleton.bones.get(this.boneIndex);
            if (!bone.active) {
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                switch (blend) {
                    case setup: {
                        bone.shearX = bone.data.shearX;
                        bone.shearY = bone.data.shearY;
                        return;
                    }
                    case first: {
                        bone.shearX += (bone.data.shearX - bone.shearX) * alpha;
                        bone.shearY += (bone.data.shearY - bone.shearY) * alpha;
                    }
                }
                return;
            }
            if (time >= frames[frames.length - 3]) {
                x = frames[frames.length + -2];
                y = frames[frames.length + -1];
            } else {
                int frame = Animation.binarySearch(frames, time, 3);
                x = frames[frame + -2];
                y = frames[frame + -1];
                float frameTime = frames[frame];
                float percent = this.getCurvePercent(frame / 3 - 1, 1.0f - (time - frameTime) / (frames[frame + -3] - frameTime));
                x += (frames[frame + 1] - x) * percent;
                y += (frames[frame + 2] - y) * percent;
            }
            switch (blend) {
                case setup: {
                    bone.shearX = bone.data.shearX + x * alpha;
                    bone.shearY = bone.data.shearY + y * alpha;
                    break;
                }
                case first: 
                case replace: {
                    bone.shearX += (bone.data.shearX + x - bone.shearX) * alpha;
                    bone.shearY += (bone.data.shearY + y - bone.shearY) * alpha;
                    break;
                }
                case add: {
                    bone.shearX += x * alpha;
                    bone.shearY += y * alpha;
                }
            }
        }
    }

    public static class ScaleTimeline
    extends TranslateTimeline {
        public ScaleTimeline(int frameCount) {
            super(frameCount);
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.scale.ordinal() << 24) + this.boneIndex;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            float by;
            float y;
            float x;
            Bone bone = skeleton.bones.get(this.boneIndex);
            if (!bone.active) {
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                switch (blend) {
                    case setup: {
                        bone.scaleX = bone.data.scaleX;
                        bone.scaleY = bone.data.scaleY;
                        return;
                    }
                    case first: {
                        bone.scaleX += (bone.data.scaleX - bone.scaleX) * alpha;
                        bone.scaleY += (bone.data.scaleY - bone.scaleY) * alpha;
                    }
                }
                return;
            }
            if (time >= frames[frames.length - 3]) {
                x = frames[frames.length + -2] * bone.data.scaleX;
                y = frames[frames.length + -1] * bone.data.scaleY;
            } else {
                int frame = Animation.binarySearch(frames, time, 3);
                x = frames[frame + -2];
                y = frames[frame + -1];
                float frameTime = frames[frame];
                float percent = this.getCurvePercent(frame / 3 - 1, 1.0f - (time - frameTime) / (frames[frame + -3] - frameTime));
                x = (x + (frames[frame + 1] - x) * percent) * bone.data.scaleX;
                y = (y + (frames[frame + 2] - y) * percent) * bone.data.scaleY;
            }
            if (alpha == 1.0f) {
                if (blend == MixBlend.add) {
                    bone.scaleX += x - bone.data.scaleX;
                    bone.scaleY += y - bone.data.scaleY;
                } else {
                    bone.scaleX = x;
                    bone.scaleY = y;
                }
            } else if (direction == MixDirection.out) {
                switch (blend) {
                    case setup: {
                        float bx = bone.data.scaleX;
                        by = bone.data.scaleY;
                        bone.scaleX = bx + (Math.abs(x) * Math.signum(bx) - bx) * alpha;
                        bone.scaleY = by + (Math.abs(y) * Math.signum(by) - by) * alpha;
                        break;
                    }
                    case first: 
                    case replace: {
                        float bx = bone.scaleX;
                        by = bone.scaleY;
                        bone.scaleX = bx + (Math.abs(x) * Math.signum(bx) - bx) * alpha;
                        bone.scaleY = by + (Math.abs(y) * Math.signum(by) - by) * alpha;
                        break;
                    }
                    case add: {
                        float bx = bone.scaleX;
                        by = bone.scaleY;
                        bone.scaleX = bx + (Math.abs(x) * Math.signum(bx) - bone.data.scaleX) * alpha;
                        bone.scaleY = by + (Math.abs(y) * Math.signum(by) - bone.data.scaleY) * alpha;
                    }
                }
            } else {
                switch (blend) {
                    case setup: {
                        float bx = Math.abs(bone.data.scaleX) * Math.signum(x);
                        by = Math.abs(bone.data.scaleY) * Math.signum(y);
                        bone.scaleX = bx + (x - bx) * alpha;
                        bone.scaleY = by + (y - by) * alpha;
                        break;
                    }
                    case first: 
                    case replace: {
                        float bx = Math.abs(bone.scaleX) * Math.signum(x);
                        by = Math.abs(bone.scaleY) * Math.signum(y);
                        bone.scaleX = bx + (x - bx) * alpha;
                        bone.scaleY = by + (y - by) * alpha;
                        break;
                    }
                    case add: {
                        float bx = Math.signum(x);
                        by = Math.signum(y);
                        bone.scaleX = Math.abs(bone.scaleX) * bx + (x - Math.abs(bone.data.scaleX) * bx) * alpha;
                        bone.scaleY = Math.abs(bone.scaleY) * by + (y - Math.abs(bone.data.scaleY) * by) * alpha;
                    }
                }
            }
        }
    }

    public static class TranslateTimeline
    extends CurveTimeline
    implements BoneTimeline {
        public static final int ENTRIES = 3;
        static final int PREV_TIME = -3;
        static final int PREV_X = -2;
        static final int PREV_Y = -1;
        static final int X = 1;
        static final int Y = 2;
        int boneIndex;
        final float[] frames;

        public TranslateTimeline(int frameCount) {
            super(frameCount);
            this.frames = new float[frameCount * 3];
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.translate.ordinal() << 24) + this.boneIndex;
        }

        @Override
        public void setBoneIndex(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("index must be >= 0.");
            }
            this.boneIndex = index;
        }

        @Override
        public int getBoneIndex() {
            return this.boneIndex;
        }

        public float[] getFrames() {
            return this.frames;
        }

        public void setFrame(int frameIndex, float time, float x, float y) {
            this.frames[frameIndex *= 3] = time;
            this.frames[frameIndex + 1] = x;
            this.frames[frameIndex + 2] = y;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            float y;
            float x;
            Bone bone = skeleton.bones.get(this.boneIndex);
            if (!bone.active) {
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                switch (blend) {
                    case setup: {
                        bone.x = bone.data.x;
                        bone.y = bone.data.y;
                        return;
                    }
                    case first: {
                        bone.x += (bone.data.x - bone.x) * alpha;
                        bone.y += (bone.data.y - bone.y) * alpha;
                    }
                }
                return;
            }
            if (time >= frames[frames.length - 3]) {
                x = frames[frames.length + -2];
                y = frames[frames.length + -1];
            } else {
                int frame = Animation.binarySearch(frames, time, 3);
                x = frames[frame + -2];
                y = frames[frame + -1];
                float frameTime = frames[frame];
                float percent = this.getCurvePercent(frame / 3 - 1, 1.0f - (time - frameTime) / (frames[frame + -3] - frameTime));
                x += (frames[frame + 1] - x) * percent;
                y += (frames[frame + 2] - y) * percent;
            }
            switch (blend) {
                case setup: {
                    bone.x = bone.data.x + x * alpha;
                    bone.y = bone.data.y + y * alpha;
                    break;
                }
                case first: 
                case replace: {
                    bone.x += (bone.data.x + x - bone.x) * alpha;
                    bone.y += (bone.data.y + y - bone.y) * alpha;
                    break;
                }
                case add: {
                    bone.x += x * alpha;
                    bone.y += y * alpha;
                }
            }
        }
    }

    public static class RotateTimeline
    extends CurveTimeline
    implements BoneTimeline {
        public static final int ENTRIES = 2;
        static final int PREV_TIME = -2;
        static final int PREV_ROTATION = -1;
        static final int ROTATION = 1;
        int boneIndex;
        final float[] frames;

        public RotateTimeline(int frameCount) {
            super(frameCount);
            this.frames = new float[frameCount << 1];
        }

        @Override
        public int getPropertyId() {
            return (TimelineType.rotate.ordinal() << 24) + this.boneIndex;
        }

        @Override
        public void setBoneIndex(int index) {
            if (index < 0) {
                throw new IllegalArgumentException("index must be >= 0.");
            }
            this.boneIndex = index;
        }

        @Override
        public int getBoneIndex() {
            return this.boneIndex;
        }

        public float[] getFrames() {
            return this.frames;
        }

        public void setFrame(int frameIndex, float time, float degrees) {
            this.frames[frameIndex <<= 1] = time;
            this.frames[frameIndex + 1] = degrees;
        }

        @Override
        public void apply(Skeleton skeleton, float lastTime, float time, Array<Event> events, float alpha, MixBlend blend, MixDirection direction) {
            Bone bone = skeleton.bones.get(this.boneIndex);
            if (!bone.active) {
                return;
            }
            float[] frames = this.frames;
            if (time < frames[0]) {
                switch (blend) {
                    case setup: {
                        bone.rotation = bone.data.rotation;
                        return;
                    }
                    case first: {
                        float r = bone.data.rotation - bone.rotation;
                        bone.rotation += (r - (float)((16384 - (int)(16384.499999999996 - (double)(r / 360.0f))) * 360)) * alpha;
                    }
                }
                return;
            }
            if (time >= frames[frames.length - 2]) {
                float r = frames[frames.length + -1];
                switch (blend) {
                    case setup: {
                        bone.rotation = bone.data.rotation + r * alpha;
                        break;
                    }
                    case first: 
                    case replace: {
                        r += bone.data.rotation - bone.rotation;
                        r -= (float)((16384 - (int)(16384.499999999996 - (double)(r / 360.0f))) * 360);
                    }
                    case add: {
                        bone.rotation += r * alpha;
                    }
                }
                return;
            }
            int frame = Animation.binarySearch(frames, time, 2);
            float prevRotation = frames[frame + -1];
            float frameTime = frames[frame];
            float percent = this.getCurvePercent((frame >> 1) - 1, 1.0f - (time - frameTime) / (frames[frame + -2] - frameTime));
            float r = frames[frame + 1] - prevRotation;
            r = prevRotation + (r - (float)((16384 - (int)(16384.499999999996 - (double)(r / 360.0f))) * 360)) * percent;
            switch (blend) {
                case setup: {
                    bone.rotation = bone.data.rotation + (r - (float)((16384 - (int)(16384.499999999996 - (double)(r / 360.0f))) * 360)) * alpha;
                    break;
                }
                case first: 
                case replace: {
                    r += bone.data.rotation - bone.rotation;
                }
                case add: {
                    bone.rotation += (r - (float)((16384 - (int)(16384.499999999996 - (double)(r / 360.0f))) * 360)) * alpha;
                }
            }
        }
    }

    public static abstract class CurveTimeline
    implements Timeline {
        public static final float LINEAR = 0.0f;
        public static final float STEPPED = 1.0f;
        public static final float BEZIER = 2.0f;
        private static final int BEZIER_SIZE = 19;
        private final float[] curves;

        public CurveTimeline(int frameCount) {
            if (frameCount <= 0) {
                throw new IllegalArgumentException("frameCount must be > 0: " + frameCount);
            }
            this.curves = new float[(frameCount - 1) * 19];
        }

        public int getFrameCount() {
            return this.curves.length / 19 + 1;
        }

        public void setLinear(int frameIndex) {
            this.curves[frameIndex * 19] = 0.0f;
        }

        public void setStepped(int frameIndex) {
            this.curves[frameIndex * 19] = 1.0f;
        }

        public float getCurveType(int frameIndex) {
            int index = frameIndex * 19;
            if (index == this.curves.length) {
                return 0.0f;
            }
            float type = this.curves[index];
            if (type == 0.0f) {
                return 0.0f;
            }
            if (type == 1.0f) {
                return 1.0f;
            }
            return 2.0f;
        }

        public void setCurve(int frameIndex, float cx1, float cy1, float cx2, float cy2) {
            float tmpx = (-cx1 * 2.0f + cx2) * 0.03f;
            float tmpy = (-cy1 * 2.0f + cy2) * 0.03f;
            float dddfx = ((cx1 - cx2) * 3.0f + 1.0f) * 0.006f;
            float dddfy = ((cy1 - cy2) * 3.0f + 1.0f) * 0.006f;
            float ddfx = tmpx * 2.0f + dddfx;
            float ddfy = tmpy * 2.0f + dddfy;
            float dfx = cx1 * 0.3f + tmpx + dddfx * 0.16666667f;
            float dfy = cy1 * 0.3f + tmpy + dddfy * 0.16666667f;
            int i = frameIndex * 19;
            float[] curves = this.curves;
            curves[i++] = 2.0f;
            float x = dfx;
            float y = dfy;
            int n = i + 19 - 1;
            while (i < n) {
                curves[i] = x;
                curves[i + 1] = y;
                x += (dfx += (ddfx += dddfx));
                y += (dfy += (ddfy += dddfy));
                i += 2;
            }
        }

        public float getCurvePercent(int frameIndex, float percent) {
            percent = MathUtils.clamp(percent, 0.0f, 1.0f);
            float[] curves = this.curves;
            int i = frameIndex * 19;
            float type = curves[i];
            if (type == 0.0f) {
                return percent;
            }
            if (type == 1.0f) {
                return 0.0f;
            }
            float x = 0.0f;
            int start = ++i;
            int n = i + 19 - 1;
            while (i < n) {
                x = curves[i];
                if (x >= percent) {
                    if (i == start) {
                        return curves[i + 1] * percent / x;
                    }
                    float prevX = curves[i - 2];
                    float prevY = curves[i - 1];
                    return prevY + (curves[i + 1] - prevY) * (percent - prevX) / (x - prevX);
                }
                i += 2;
            }
            float y = curves[i - 1];
            return y + (1.0f - y) * (percent - x) / (1.0f - x);
        }
    }

    public static interface SlotTimeline
    extends Timeline {
        public void setSlotIndex(int var1);

        public int getSlotIndex();
    }

    public static interface BoneTimeline
    extends Timeline {
        public void setBoneIndex(int var1);

        public int getBoneIndex();
    }

    private static enum TimelineType {
        rotate,
        translate,
        scale,
        shear,
        attachment,
        color,
        deform,
        event,
        drawOrder,
        ikConstraint,
        transformConstraint,
        pathConstraintPosition,
        pathConstraintSpacing,
        pathConstraintMix,
        twoColor;

    }

    public static enum MixDirection {
        in,
        out;

    }

    public static enum MixBlend {
        setup,
        first,
        replace,
        add;

    }

    public static interface Timeline {
        public void apply(Skeleton var1, float var2, float var3, Array<Event> var4, float var5, MixBlend var6, MixDirection var7);

        public int getPropertyId();
    }
}

