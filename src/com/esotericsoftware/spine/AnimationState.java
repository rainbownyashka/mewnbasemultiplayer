/*
 * Decompiled with CFR 0.152.
 */
package com.esotericsoftware.spine;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.Pool;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;

public class AnimationState {
    private static final Animation emptyAnimation = new Animation("<empty>", new Array<Animation.Timeline>(0), 0.0f);
    private static final int SUBSEQUENT = 0;
    private static final int FIRST = 1;
    private static final int HOLD = 2;
    private static final int HOLD_MIX = 3;
    private static final int NOT_LAST = 4;
    private AnimationStateData data;
    final Array<TrackEntry> tracks = new Array();
    private final Array<Event> events = new Array();
    final Array<AnimationStateListener> listeners = new Array();
    private final EventQueue queue = new EventQueue();
    private final IntSet propertyIDs = new IntSet();
    boolean animationsChanged;
    private float timeScale = 1.0f;
    final Pool<TrackEntry> trackEntryPool = new Pool(){

        protected Object newObject() {
            return new TrackEntry();
        }
    };

    public AnimationState() {
    }

    public AnimationState(AnimationStateData data) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null.");
        }
        this.data = data;
    }

    public void update(float delta) {
        delta *= this.timeScale;
        int n = this.tracks.size;
        for (int i = 0; i < n; ++i) {
            TrackEntry next;
            TrackEntry current = this.tracks.get(i);
            if (current == null) continue;
            current.animationLast = current.nextAnimationLast;
            current.trackLast = current.nextTrackLast;
            float currentDelta = delta * current.timeScale;
            if (current.delay > 0.0f) {
                current.delay -= currentDelta;
                if (current.delay > 0.0f) continue;
                currentDelta = -current.delay;
                current.delay = 0.0f;
            }
            if ((next = current.next) != null) {
                float nextTime = current.trackLast - next.delay;
                if (nextTime >= 0.0f) {
                    next.delay = 0.0f;
                    next.trackTime = current.timeScale == 0.0f ? 0.0f : (nextTime / current.timeScale + delta) * next.timeScale;
                    current.trackTime += currentDelta;
                    this.setCurrent(i, next, true);
                    while (next.mixingFrom != null) {
                        next.mixTime += delta;
                        next = next.mixingFrom;
                    }
                    continue;
                }
            } else if (current.trackLast >= current.trackEnd && current.mixingFrom == null) {
                this.tracks.set(i, null);
                this.queue.end(current);
                this.disposeNext(current);
                continue;
            }
            if (current.mixingFrom != null && this.updateMixingFrom(current, delta)) {
                TrackEntry from = current.mixingFrom;
                current.mixingFrom = null;
                if (from != null) {
                    from.mixingTo = null;
                }
                while (from != null) {
                    this.queue.end(from);
                    from = from.mixingFrom;
                }
            }
            current.trackTime += currentDelta;
        }
        this.queue.drain();
    }

    private boolean updateMixingFrom(TrackEntry to, float delta) {
        TrackEntry from = to.mixingFrom;
        if (from == null) {
            return true;
        }
        boolean finished = this.updateMixingFrom(from, delta);
        from.animationLast = from.nextAnimationLast;
        from.trackLast = from.nextTrackLast;
        if (to.mixTime > 0.0f && to.mixTime >= to.mixDuration) {
            if (from.totalAlpha == 0.0f || to.mixDuration == 0.0f) {
                to.mixingFrom = from.mixingFrom;
                if (from.mixingFrom != null) {
                    from.mixingFrom.mixingTo = to;
                }
                to.interruptAlpha = from.interruptAlpha;
                this.queue.end(from);
            }
            return finished;
        }
        from.trackTime += delta * from.timeScale;
        to.mixTime += delta;
        return false;
    }

    public boolean apply(Skeleton skeleton) {
        if (skeleton == null) {
            throw new IllegalArgumentException("skeleton cannot be null.");
        }
        if (this.animationsChanged) {
            this.animationsChanged();
        }
        Array<Event> events = this.events;
        boolean applied = false;
        int n = this.tracks.size;
        for (int i = 0; i < n; ++i) {
            TrackEntry current = this.tracks.get(i);
            if (current == null || current.delay > 0.0f) continue;
            applied = true;
            Animation.MixBlend blend = i == 0 ? Animation.MixBlend.first : current.mixBlend;
            float mix = current.alpha;
            if (current.mixingFrom != null) {
                mix *= this.applyMixingFrom(current, skeleton, blend);
            } else if (current.trackTime >= current.trackEnd && current.next == null) {
                mix = 0.0f;
            }
            float animationLast = current.animationLast;
            float animationTime = current.getAnimationTime();
            int timelineCount = current.animation.timelines.size;
            T[] timelines = current.animation.timelines.items;
            if (i == 0 && mix == 1.0f || blend == Animation.MixBlend.add) {
                for (int ii = 0; ii < timelineCount; ++ii) {
                    ((Animation.Timeline)timelines[ii]).apply(skeleton, animationLast, animationTime, events, mix, blend, Animation.MixDirection.in);
                }
            } else {
                boolean firstFrame;
                int[] timelineMode = current.timelineMode.items;
                boolean bl = firstFrame = current.timelinesRotation.size != timelineCount << 1;
                if (firstFrame) {
                    current.timelinesRotation.setSize(timelineCount << 1);
                }
                float[] timelinesRotation = current.timelinesRotation.items;
                for (int ii = 0; ii < timelineCount; ++ii) {
                    Animation.MixBlend timelineBlend;
                    Animation.Timeline timeline = (Animation.Timeline)timelines[ii];
                    Animation.MixBlend mixBlend = timelineBlend = (timelineMode[ii] & 3) == 0 ? blend : Animation.MixBlend.setup;
                    if (timeline instanceof Animation.RotateTimeline) {
                        this.applyRotateTimeline((Animation.RotateTimeline)timeline, skeleton, animationTime, mix, timelineBlend, timelinesRotation, ii << 1, firstFrame);
                        continue;
                    }
                    timeline.apply(skeleton, animationLast, animationTime, events, mix, timelineBlend, Animation.MixDirection.in);
                }
            }
            this.queueEvents(current, animationTime);
            events.clear();
            current.nextAnimationLast = animationTime;
            current.nextTrackLast = current.trackTime;
        }
        this.queue.drain();
        return applied;
    }

    private float applyMixingFrom(TrackEntry to, Skeleton skeleton, Animation.MixBlend blend) {
        float mix;
        TrackEntry from = to.mixingFrom;
        if (from.mixingFrom != null) {
            this.applyMixingFrom(from, skeleton, blend);
        }
        if (to.mixDuration == 0.0f) {
            mix = 1.0f;
            if (blend == Animation.MixBlend.first) {
                blend = Animation.MixBlend.setup;
            }
        } else {
            mix = to.mixTime / to.mixDuration;
            if (mix > 1.0f) {
                mix = 1.0f;
            }
            if (blend != Animation.MixBlend.first) {
                blend = from.mixBlend;
            }
        }
        Array<Event> events = mix < from.eventThreshold ? this.events : null;
        boolean attachments = mix < from.attachmentThreshold;
        boolean drawOrder = mix < from.drawOrderThreshold;
        float animationLast = from.animationLast;
        float animationTime = from.getAnimationTime();
        int timelineCount = from.animation.timelines.size;
        T[] timelines = from.animation.timelines.items;
        float alphaHold = from.alpha * to.interruptAlpha;
        float alphaMix = alphaHold * (1.0f - mix);
        if (blend == Animation.MixBlend.add) {
            for (int i = 0; i < timelineCount; ++i) {
                ((Animation.Timeline)timelines[i]).apply(skeleton, animationLast, animationTime, events, alphaMix, blend, Animation.MixDirection.out);
            }
        } else {
            boolean firstFrame;
            int[] timelineMode = from.timelineMode.items;
            T[] timelineHoldMix = from.timelineHoldMix.items;
            boolean bl = firstFrame = from.timelinesRotation.size != timelineCount << 1;
            if (firstFrame) {
                from.timelinesRotation.setSize(timelineCount << 1);
            }
            float[] timelinesRotation = from.timelinesRotation.items;
            from.totalAlpha = 0.0f;
            block6: for (int i = 0; i < timelineCount; ++i) {
                float alpha;
                Animation.MixBlend timelineBlend;
                Animation.Timeline timeline = (Animation.Timeline)timelines[i];
                Animation.MixDirection direction = Animation.MixDirection.out;
                switch (timelineMode[i] & 3) {
                    case 0: {
                        if (!attachments && timeline instanceof Animation.AttachmentTimeline) {
                            if ((timelineMode[i] & 4) == 4) continue block6;
                            blend = Animation.MixBlend.setup;
                        }
                        if (!drawOrder && timeline instanceof Animation.DrawOrderTimeline) continue block6;
                        timelineBlend = blend;
                        alpha = alphaMix;
                        break;
                    }
                    case 1: {
                        timelineBlend = Animation.MixBlend.setup;
                        alpha = alphaMix;
                        break;
                    }
                    case 2: {
                        timelineBlend = Animation.MixBlend.setup;
                        alpha = alphaHold;
                        break;
                    }
                    default: {
                        timelineBlend = Animation.MixBlend.setup;
                        TrackEntry holdMix = (TrackEntry)timelineHoldMix[i];
                        alpha = alphaHold * Math.max(0.0f, 1.0f - holdMix.mixTime / holdMix.mixDuration);
                    }
                }
                from.totalAlpha += alpha;
                if (timeline instanceof Animation.RotateTimeline) {
                    this.applyRotateTimeline((Animation.RotateTimeline)timeline, skeleton, animationTime, alpha, timelineBlend, timelinesRotation, i << 1, firstFrame);
                    continue;
                }
                if (timelineBlend == Animation.MixBlend.setup) {
                    if (timeline instanceof Animation.AttachmentTimeline) {
                        if (attachments || (timelineMode[i] & 4) == 4) {
                            direction = Animation.MixDirection.in;
                        }
                    } else if (timeline instanceof Animation.DrawOrderTimeline && drawOrder) {
                        direction = Animation.MixDirection.in;
                    }
                }
                timeline.apply(skeleton, animationLast, animationTime, events, alpha, timelineBlend, direction);
            }
        }
        if (to.mixDuration > 0.0f) {
            this.queueEvents(from, animationTime);
        }
        this.events.clear();
        from.nextAnimationLast = animationTime;
        from.nextTrackLast = from.trackTime;
        return mix;
    }

    private void applyRotateTimeline(Animation.RotateTimeline timeline, Skeleton skeleton, float time, float alpha, Animation.MixBlend blend, float[] timelinesRotation, int i, boolean firstFrame) {
        float total;
        float r2;
        float r1;
        if (firstFrame) {
            timelinesRotation[i] = 0.0f;
        }
        if (alpha == 1.0f) {
            timeline.apply(skeleton, 0.0f, time, null, 1.0f, blend, Animation.MixDirection.in);
            return;
        }
        Bone bone = skeleton.bones.get(timeline.boneIndex);
        if (!bone.active) {
            return;
        }
        float[] frames = timeline.frames;
        if (time < frames[0]) {
            switch (blend) {
                case setup: {
                    bone.rotation = bone.data.rotation;
                }
                default: {
                    return;
                }
                case first: 
            }
            r1 = bone.rotation;
            r2 = bone.data.rotation;
        } else {
            float f = r1 = blend == Animation.MixBlend.setup ? bone.data.rotation : bone.rotation;
            if (time >= frames[frames.length - 2]) {
                r2 = bone.data.rotation + frames[frames.length + -1];
            } else {
                int frame = Animation.binarySearch(frames, time, 2);
                float prevRotation = frames[frame + -1];
                float frameTime = frames[frame];
                float percent = timeline.getCurvePercent((frame >> 1) - 1, 1.0f - (time - frameTime) / (frames[frame + -2] - frameTime));
                r2 = frames[frame + 1] - prevRotation;
                r2 -= (float)((16384 - (int)(16384.499999999996 - (double)(r2 / 360.0f))) * 360);
                r2 = prevRotation + r2 * percent + bone.data.rotation;
                r2 -= (float)((16384 - (int)(16384.499999999996 - (double)(r2 / 360.0f))) * 360);
            }
        }
        float diff = r2 - r1;
        diff -= (float)((16384 - (int)(16384.499999999996 - (double)(diff / 360.0f))) * 360);
        if (diff == 0.0f) {
            total = timelinesRotation[i];
        } else {
            boolean dir;
            float lastDiff;
            float lastTotal;
            if (firstFrame) {
                lastTotal = 0.0f;
                lastDiff = diff;
            } else {
                lastTotal = timelinesRotation[i];
                lastDiff = timelinesRotation[i + 1];
            }
            boolean current = diff > 0.0f;
            boolean bl = dir = lastTotal >= 0.0f;
            if (Math.signum(lastDiff) != Math.signum(diff) && Math.abs(lastDiff) <= 90.0f) {
                if (Math.abs(lastTotal) > 180.0f) {
                    lastTotal += 360.0f * Math.signum(lastTotal);
                }
                dir = current;
            }
            total = diff + lastTotal - lastTotal % 360.0f;
            if (dir != current) {
                total += 360.0f * Math.signum(lastTotal);
            }
            timelinesRotation[i] = total;
        }
        timelinesRotation[i + 1] = diff;
        bone.rotation = (r1 += total * alpha) - (float)((16384 - (int)(16384.499999999996 - (double)(r1 / 360.0f))) * 360);
    }

    private void queueEvents(TrackEntry entry, float animationTime) {
        boolean complete;
        int i;
        float animationStart = entry.animationStart;
        float animationEnd = entry.animationEnd;
        float duration = animationEnd - animationStart;
        float trackLastWrapped = entry.trackLast % duration;
        Array<Event> events = this.events;
        int n = events.size;
        for (i = 0; i < n; ++i) {
            Event event = events.get(i);
            if (event.time < trackLastWrapped) break;
            if (event.time > animationEnd) continue;
            this.queue.event(entry, event);
        }
        if (entry.loop) {
            complete = duration == 0.0f || trackLastWrapped > entry.trackTime % duration;
        } else {
            boolean bl = complete = animationTime >= animationEnd && entry.animationLast < animationEnd;
        }
        if (complete) {
            this.queue.complete(entry);
        }
        while (i < n) {
            Event event = events.get(i);
            if (!(event.time < animationStart)) {
                this.queue.event(entry, events.get(i));
            }
            ++i;
        }
    }

    public void clearTracks() {
        boolean oldDrainDisabled = this.queue.drainDisabled;
        this.queue.drainDisabled = true;
        int n = this.tracks.size;
        for (int i = 0; i < n; ++i) {
            this.clearTrack(i);
        }
        this.tracks.clear();
        this.queue.drainDisabled = oldDrainDisabled;
        this.queue.drain();
    }

    public void clearTrack(int trackIndex) {
        TrackEntry from;
        if (trackIndex < 0) {
            throw new IllegalArgumentException("trackIndex must be >= 0.");
        }
        if (trackIndex >= this.tracks.size) {
            return;
        }
        TrackEntry current = this.tracks.get(trackIndex);
        if (current == null) {
            return;
        }
        this.queue.end(current);
        this.disposeNext(current);
        TrackEntry entry = current;
        while ((from = entry.mixingFrom) != null) {
            this.queue.end(from);
            entry.mixingFrom = null;
            entry.mixingTo = null;
            entry = from;
        }
        this.tracks.set(current.trackIndex, null);
        this.queue.drain();
    }

    private void setCurrent(int index, TrackEntry current, boolean interrupt) {
        TrackEntry from = this.expandToIndex(index);
        this.tracks.set(index, current);
        if (from != null) {
            if (interrupt) {
                this.queue.interrupt(from);
            }
            current.mixingFrom = from;
            from.mixingTo = current;
            current.mixTime = 0.0f;
            if (from.mixingFrom != null && from.mixDuration > 0.0f) {
                current.interruptAlpha *= Math.min(1.0f, from.mixTime / from.mixDuration);
            }
            from.timelinesRotation.clear();
        }
        this.queue.start(current);
    }

    public TrackEntry setAnimation(int trackIndex, String animationName, boolean loop) {
        Animation animation = this.data.skeletonData.findAnimation(animationName);
        if (animation == null) {
            throw new IllegalArgumentException("Animation not found: " + animationName);
        }
        return this.setAnimation(trackIndex, animation, loop);
    }

    public TrackEntry setAnimation(int trackIndex, Animation animation, boolean loop) {
        if (trackIndex < 0) {
            throw new IllegalArgumentException("trackIndex must be >= 0.");
        }
        if (animation == null) {
            throw new IllegalArgumentException("animation cannot be null.");
        }
        boolean interrupt = true;
        TrackEntry current = this.expandToIndex(trackIndex);
        if (current != null) {
            if (current.nextTrackLast == -1.0f) {
                this.tracks.set(trackIndex, current.mixingFrom);
                this.queue.interrupt(current);
                this.queue.end(current);
                this.disposeNext(current);
                current = current.mixingFrom;
                interrupt = false;
            } else {
                this.disposeNext(current);
            }
        }
        TrackEntry entry = this.trackEntry(trackIndex, animation, loop, current);
        this.setCurrent(trackIndex, entry, interrupt);
        this.queue.drain();
        return entry;
    }

    public TrackEntry addAnimation(int trackIndex, String animationName, boolean loop, float delay) {
        Animation animation = this.data.skeletonData.findAnimation(animationName);
        if (animation == null) {
            throw new IllegalArgumentException("Animation not found: " + animationName);
        }
        return this.addAnimation(trackIndex, animation, loop, delay);
    }

    public TrackEntry addAnimation(int trackIndex, Animation animation, boolean loop, float delay) {
        if (trackIndex < 0) {
            throw new IllegalArgumentException("trackIndex must be >= 0.");
        }
        if (animation == null) {
            throw new IllegalArgumentException("animation cannot be null.");
        }
        TrackEntry last = this.expandToIndex(trackIndex);
        if (last != null) {
            while (last.next != null) {
                last = last.next;
            }
        }
        TrackEntry entry = this.trackEntry(trackIndex, animation, loop, last);
        if (last == null) {
            this.setCurrent(trackIndex, entry, true);
            this.queue.drain();
        } else {
            last.next = entry;
            if (delay <= 0.0f) {
                float duration = last.animationEnd - last.animationStart;
                if (duration != 0.0f) {
                    delay = last.loop ? (delay += duration * (float)(1 + (int)(last.trackTime / duration))) : (delay += Math.max(duration, last.trackTime));
                    delay -= this.data.getMix(last.animation, animation);
                } else {
                    delay = last.trackTime;
                }
            }
        }
        entry.delay = delay;
        return entry;
    }

    public TrackEntry setEmptyAnimation(int trackIndex, float mixDuration) {
        TrackEntry entry = this.setAnimation(trackIndex, emptyAnimation, false);
        entry.mixDuration = mixDuration;
        entry.trackEnd = mixDuration;
        return entry;
    }

    public TrackEntry addEmptyAnimation(int trackIndex, float mixDuration, float delay) {
        if (delay <= 0.0f) {
            delay -= mixDuration;
        }
        TrackEntry entry = this.addAnimation(trackIndex, emptyAnimation, false, delay);
        entry.mixDuration = mixDuration;
        entry.trackEnd = mixDuration;
        return entry;
    }

    public void setEmptyAnimations(float mixDuration) {
        boolean oldDrainDisabled = this.queue.drainDisabled;
        this.queue.drainDisabled = true;
        int n = this.tracks.size;
        for (int i = 0; i < n; ++i) {
            TrackEntry current = this.tracks.get(i);
            if (current == null) continue;
            this.setEmptyAnimation(current.trackIndex, mixDuration);
        }
        this.queue.drainDisabled = oldDrainDisabled;
        this.queue.drain();
    }

    private TrackEntry expandToIndex(int index) {
        if (index < this.tracks.size) {
            return this.tracks.get(index);
        }
        this.tracks.ensureCapacity(index - this.tracks.size + 1);
        this.tracks.size = index + 1;
        return null;
    }

    private TrackEntry trackEntry(int trackIndex, Animation animation, boolean loop, TrackEntry last) {
        TrackEntry entry = this.trackEntryPool.obtain();
        entry.trackIndex = trackIndex;
        entry.animation = animation;
        entry.loop = loop;
        entry.holdPrevious = false;
        entry.eventThreshold = 0.0f;
        entry.attachmentThreshold = 0.0f;
        entry.drawOrderThreshold = 0.0f;
        entry.animationStart = 0.0f;
        entry.animationEnd = animation.getDuration();
        entry.animationLast = -1.0f;
        entry.nextAnimationLast = -1.0f;
        entry.delay = 0.0f;
        entry.trackTime = 0.0f;
        entry.trackLast = -1.0f;
        entry.nextTrackLast = -1.0f;
        entry.trackEnd = Float.MAX_VALUE;
        entry.timeScale = 1.0f;
        entry.alpha = 1.0f;
        entry.interruptAlpha = 1.0f;
        entry.mixTime = 0.0f;
        entry.mixDuration = last == null ? 0.0f : this.data.getMix(last.animation, animation);
        return entry;
    }

    private void disposeNext(TrackEntry entry) {
        TrackEntry next = entry.next;
        while (next != null) {
            this.queue.dispose(next);
            next = next.next;
        }
        entry.next = null;
    }

    private void animationsChanged() {
        int i;
        this.animationsChanged = false;
        this.propertyIDs.clear(2048);
        int n = this.tracks.size;
        for (i = 0; i < n; ++i) {
            TrackEntry entry = this.tracks.get(i);
            if (entry == null) continue;
            while (entry.mixingFrom != null) {
                entry = entry.mixingFrom;
            }
            do {
                if (entry.mixingTo != null && entry.mixBlend == Animation.MixBlend.add) continue;
                this.computeHold(entry);
            } while ((entry = entry.mixingTo) != null);
        }
        this.propertyIDs.clear(2048);
        for (i = this.tracks.size - 1; i >= 0; --i) {
            TrackEntry entry = this.tracks.get(i);
            while (entry != null) {
                this.computeNotLast(entry);
                entry = entry.mixingFrom;
            }
        }
    }

    private void computeHold(TrackEntry entry) {
        TrackEntry to = entry.mixingTo;
        T[] timelines = entry.animation.timelines.items;
        int timelinesCount = entry.animation.timelines.size;
        int[] timelineMode = entry.timelineMode.setSize(timelinesCount);
        entry.timelineHoldMix.clear();
        TrackEntry[] timelineHoldMix = entry.timelineHoldMix.setSize(timelinesCount);
        IntSet propertyIDs = this.propertyIDs;
        if (to != null && to.holdPrevious) {
            for (int i = 0; i < timelinesCount; ++i) {
                propertyIDs.add(((Animation.Timeline)timelines[i]).getPropertyId());
                timelineMode[i] = 2;
            }
            return;
        }
        block1: for (int i = 0; i < timelinesCount; ++i) {
            Animation.Timeline timeline = (Animation.Timeline)timelines[i];
            int id = timeline.getPropertyId();
            if (!propertyIDs.add(id)) {
                timelineMode[i] = 0;
                continue;
            }
            if (to == null || timeline instanceof Animation.AttachmentTimeline || timeline instanceof Animation.DrawOrderTimeline || timeline instanceof Animation.EventTimeline || !this.hasTimeline(to, id)) {
                timelineMode[i] = 1;
                continue;
            }
            TrackEntry next = to.mixingTo;
            while (next != null) {
                if (!this.hasTimeline(next, id)) {
                    if (!(next.mixDuration > 0.0f)) break;
                    timelineMode[i] = 3;
                    timelineHoldMix[i] = next;
                    continue block1;
                }
                next = next.mixingTo;
            }
            timelineMode[i] = 2;
        }
    }

    private void computeNotLast(TrackEntry entry) {
        T[] timelines = entry.animation.timelines.items;
        int timelinesCount = entry.animation.timelines.size;
        int[] timelineMode = entry.timelineMode.items;
        IntSet propertyIDs = this.propertyIDs;
        for (int i = 0; i < timelinesCount; ++i) {
            if (!(timelines[i] instanceof Animation.AttachmentTimeline)) continue;
            Animation.AttachmentTimeline timeline = (Animation.AttachmentTimeline)timelines[i];
            if (propertyIDs.add(timeline.slotIndex)) continue;
            int n = i;
            timelineMode[n] = timelineMode[n] | 4;
        }
    }

    private boolean hasTimeline(TrackEntry entry, int id) {
        T[] timelines = entry.animation.timelines.items;
        int n = entry.animation.timelines.size;
        for (int i = 0; i < n; ++i) {
            if (((Animation.Timeline)timelines[i]).getPropertyId() != id) continue;
            return true;
        }
        return false;
    }

    public TrackEntry getCurrent(int trackIndex) {
        if (trackIndex < 0) {
            throw new IllegalArgumentException("trackIndex must be >= 0.");
        }
        if (trackIndex >= this.tracks.size) {
            return null;
        }
        return this.tracks.get(trackIndex);
    }

    public void addListener(AnimationStateListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null.");
        }
        this.listeners.add(listener);
    }

    public void removeListener(AnimationStateListener listener) {
        this.listeners.removeValue(listener, true);
    }

    public void clearListeners() {
        this.listeners.clear();
    }

    public void clearListenerNotifications() {
        this.queue.clear();
    }

    public float getTimeScale() {
        return this.timeScale;
    }

    public void setTimeScale(float timeScale) {
        this.timeScale = timeScale;
    }

    public AnimationStateData getData() {
        return this.data;
    }

    public void setData(AnimationStateData data) {
        if (data == null) {
            throw new IllegalArgumentException("data cannot be null.");
        }
        this.data = data;
    }

    public Array<TrackEntry> getTracks() {
        return this.tracks;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder(64);
        int n = this.tracks.size;
        for (int i = 0; i < n; ++i) {
            TrackEntry entry = this.tracks.get(i);
            if (entry == null) continue;
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(entry.toString());
        }
        if (buffer.length() == 0) {
            return "<none>";
        }
        return buffer.toString();
    }

    public static abstract class AnimationStateAdapter
    implements AnimationStateListener {
        @Override
        public void start(TrackEntry entry) {
        }

        @Override
        public void interrupt(TrackEntry entry) {
        }

        @Override
        public void end(TrackEntry entry) {
        }

        @Override
        public void dispose(TrackEntry entry) {
        }

        @Override
        public void complete(TrackEntry entry) {
        }

        @Override
        public void event(TrackEntry entry, Event event) {
        }
    }

    public static interface AnimationStateListener {
        public void start(TrackEntry var1);

        public void interrupt(TrackEntry var1);

        public void end(TrackEntry var1);

        public void dispose(TrackEntry var1);

        public void complete(TrackEntry var1);

        public void event(TrackEntry var1, Event var2);
    }

    private static enum EventType {
        start,
        interrupt,
        end,
        dispose,
        complete,
        event;

    }

    class EventQueue {
        private final Array objects = new Array();
        boolean drainDisabled;

        EventQueue() {
        }

        void start(TrackEntry entry) {
            this.objects.add(EventType.start);
            this.objects.add(entry);
            AnimationState.this.animationsChanged = true;
        }

        void interrupt(TrackEntry entry) {
            this.objects.add(EventType.interrupt);
            this.objects.add(entry);
        }

        void end(TrackEntry entry) {
            this.objects.add(EventType.end);
            this.objects.add(entry);
            AnimationState.this.animationsChanged = true;
        }

        void dispose(TrackEntry entry) {
            this.objects.add(EventType.dispose);
            this.objects.add(entry);
        }

        void complete(TrackEntry entry) {
            this.objects.add(EventType.complete);
            this.objects.add(entry);
        }

        void event(TrackEntry entry, Event event) {
            this.objects.add(EventType.event);
            this.objects.add(entry);
            this.objects.add(event);
        }

        void drain() {
            if (this.drainDisabled) {
                return;
            }
            this.drainDisabled = true;
            Array objects = this.objects;
            Array<AnimationStateListener> listeners = AnimationState.this.listeners;
            block8: for (int i = 0; i < objects.size; i += 2) {
                EventType type = (EventType)((Object)objects.get(i));
                TrackEntry entry = (TrackEntry)objects.get(i + 1);
                switch (type) {
                    case start: {
                        if (entry.listener != null) {
                            entry.listener.start(entry);
                        }
                        for (int ii = 0; ii < listeners.size; ++ii) {
                            listeners.get(ii).start(entry);
                        }
                        continue block8;
                    }
                    case interrupt: {
                        if (entry.listener != null) {
                            entry.listener.interrupt(entry);
                        }
                        for (int ii = 0; ii < listeners.size; ++ii) {
                            listeners.get(ii).interrupt(entry);
                        }
                        continue block8;
                    }
                    case end: {
                        if (entry.listener != null) {
                            entry.listener.end(entry);
                        }
                        for (int ii = 0; ii < listeners.size; ++ii) {
                            listeners.get(ii).end(entry);
                        }
                    }
                    case dispose: {
                        if (entry.listener != null) {
                            entry.listener.dispose(entry);
                        }
                        for (int ii = 0; ii < listeners.size; ++ii) {
                            listeners.get(ii).dispose(entry);
                        }
                        AnimationState.this.trackEntryPool.free(entry);
                        continue block8;
                    }
                    case complete: {
                        if (entry.listener != null) {
                            entry.listener.complete(entry);
                        }
                        for (int ii = 0; ii < listeners.size; ++ii) {
                            listeners.get(ii).complete(entry);
                        }
                        continue block8;
                    }
                    case event: {
                        Event event = (Event)objects.get(i++ + 2);
                        if (entry.listener != null) {
                            entry.listener.event(entry, event);
                        }
                        for (int ii = 0; ii < listeners.size; ++ii) {
                            listeners.get(ii).event(entry, event);
                        }
                        continue block8;
                    }
                }
            }
            this.clear();
            this.drainDisabled = false;
        }

        void clear() {
            this.objects.clear();
        }
    }

    public static class TrackEntry
    implements Pool.Poolable {
        Animation animation;
        TrackEntry next;
        TrackEntry mixingFrom;
        TrackEntry mixingTo;
        AnimationStateListener listener;
        int trackIndex;
        boolean loop;
        boolean holdPrevious;
        float eventThreshold;
        float attachmentThreshold;
        float drawOrderThreshold;
        float animationStart;
        float animationEnd;
        float animationLast;
        float nextAnimationLast;
        float delay;
        float trackTime;
        float trackLast;
        float nextTrackLast;
        float trackEnd;
        float timeScale;
        float alpha;
        float mixTime;
        float mixDuration;
        float interruptAlpha;
        float totalAlpha;
        Animation.MixBlend mixBlend = Animation.MixBlend.replace;
        final IntArray timelineMode = new IntArray();
        final Array<TrackEntry> timelineHoldMix = new Array();
        final FloatArray timelinesRotation = new FloatArray();

        @Override
        public void reset() {
            this.next = null;
            this.mixingFrom = null;
            this.mixingTo = null;
            this.animation = null;
            this.listener = null;
            this.timelineMode.clear();
            this.timelineHoldMix.clear();
            this.timelinesRotation.clear();
        }

        public int getTrackIndex() {
            return this.trackIndex;
        }

        public Animation getAnimation() {
            return this.animation;
        }

        public void setAnimation(Animation animation) {
            if (animation == null) {
                throw new IllegalArgumentException("animation cannot be null.");
            }
            this.animation = animation;
        }

        public boolean getLoop() {
            return this.loop;
        }

        public void setLoop(boolean loop) {
            this.loop = loop;
        }

        public float getDelay() {
            return this.delay;
        }

        public void setDelay(float delay) {
            this.delay = delay;
        }

        public float getTrackTime() {
            return this.trackTime;
        }

        public void setTrackTime(float trackTime) {
            this.trackTime = trackTime;
        }

        public float getTrackEnd() {
            return this.trackEnd;
        }

        public void setTrackEnd(float trackEnd) {
            this.trackEnd = trackEnd;
        }

        public float getAnimationStart() {
            return this.animationStart;
        }

        public void setAnimationStart(float animationStart) {
            this.animationStart = animationStart;
        }

        public float getAnimationEnd() {
            return this.animationEnd;
        }

        public void setAnimationEnd(float animationEnd) {
            this.animationEnd = animationEnd;
        }

        public float getAnimationLast() {
            return this.animationLast;
        }

        public void setAnimationLast(float animationLast) {
            this.animationLast = animationLast;
            this.nextAnimationLast = animationLast;
        }

        public float getAnimationTime() {
            if (this.loop) {
                float duration = this.animationEnd - this.animationStart;
                if (duration == 0.0f) {
                    return this.animationStart;
                }
                return this.trackTime % duration + this.animationStart;
            }
            return Math.min(this.trackTime + this.animationStart, this.animationEnd);
        }

        public float getTimeScale() {
            return this.timeScale;
        }

        public void setTimeScale(float timeScale) {
            this.timeScale = timeScale;
        }

        public AnimationStateListener getListener() {
            return this.listener;
        }

        public void setListener(AnimationStateListener listener) {
            this.listener = listener;
        }

        public float getAlpha() {
            return this.alpha;
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
        }

        public float getEventThreshold() {
            return this.eventThreshold;
        }

        public void setEventThreshold(float eventThreshold) {
            this.eventThreshold = eventThreshold;
        }

        public float getAttachmentThreshold() {
            return this.attachmentThreshold;
        }

        public void setAttachmentThreshold(float attachmentThreshold) {
            this.attachmentThreshold = attachmentThreshold;
        }

        public float getDrawOrderThreshold() {
            return this.drawOrderThreshold;
        }

        public void setDrawOrderThreshold(float drawOrderThreshold) {
            this.drawOrderThreshold = drawOrderThreshold;
        }

        public TrackEntry getNext() {
            return this.next;
        }

        public boolean isComplete() {
            return this.trackTime >= this.animationEnd - this.animationStart;
        }

        public float getMixTime() {
            return this.mixTime;
        }

        public void setMixTime(float mixTime) {
            this.mixTime = mixTime;
        }

        public float getMixDuration() {
            return this.mixDuration;
        }

        public void setMixDuration(float mixDuration) {
            this.mixDuration = mixDuration;
        }

        public Animation.MixBlend getMixBlend() {
            return this.mixBlend;
        }

        public void setMixBlend(Animation.MixBlend mixBlend) {
            if (mixBlend == null) {
                throw new IllegalArgumentException("mixBlend cannot be null.");
            }
            this.mixBlend = mixBlend;
        }

        public TrackEntry getMixingFrom() {
            return this.mixingFrom;
        }

        public TrackEntry getMixingTo() {
            return this.mixingTo;
        }

        public void setHoldPrevious(boolean holdPrevious) {
            this.holdPrevious = holdPrevious;
        }

        public boolean getHoldPrevious() {
            return this.holdPrevious;
        }

        public void resetRotationDirections() {
            this.timelinesRotation.clear();
        }

        public String toString() {
            return this.animation == null ? "<none>" : this.animation.name;
        }
    }
}

