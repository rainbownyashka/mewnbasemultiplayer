/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package org.lwjgl.system.linux.liburing;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import javax.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct io_uring_notification_slot")
public class IOUringNotificationSlot
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TAG;
    public static final int RESV;

    public IOUringNotificationSlot(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOUringNotificationSlot.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u64")
    public long tag() {
        return IOUringNotificationSlot.ntag(this.address());
    }

    @NativeType(value="__u64[3]")
    public LongBuffer resv() {
        return IOUringNotificationSlot.nresv(this.address());
    }

    @NativeType(value="__u64")
    public long resv(int index) {
        return IOUringNotificationSlot.nresv(this.address(), index);
    }

    public IOUringNotificationSlot tag(@NativeType(value="__u64") long value) {
        IOUringNotificationSlot.ntag(this.address(), value);
        return this;
    }

    public IOUringNotificationSlot resv(@NativeType(value="__u64[3]") LongBuffer value) {
        IOUringNotificationSlot.nresv(this.address(), value);
        return this;
    }

    public IOUringNotificationSlot resv(int index, @NativeType(value="__u64") long value) {
        IOUringNotificationSlot.nresv(this.address(), index, value);
        return this;
    }

    public IOUringNotificationSlot set(long tag, LongBuffer resv) {
        this.tag(tag);
        this.resv(resv);
        return this;
    }

    public IOUringNotificationSlot set(IOUringNotificationSlot src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOUringNotificationSlot malloc() {
        return IOUringNotificationSlot.wrap(IOUringNotificationSlot.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static IOUringNotificationSlot calloc() {
        return IOUringNotificationSlot.wrap(IOUringNotificationSlot.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static IOUringNotificationSlot create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return IOUringNotificationSlot.wrap(IOUringNotificationSlot.class, MemoryUtil.memAddress(container), container);
    }

    public static IOUringNotificationSlot create(long address) {
        return IOUringNotificationSlot.wrap(IOUringNotificationSlot.class, address);
    }

    @Nullable
    public static IOUringNotificationSlot createSafe(long address) {
        return address == 0L ? null : IOUringNotificationSlot.wrap(IOUringNotificationSlot.class, address);
    }

    public static Buffer malloc(int capacity) {
        return IOUringNotificationSlot.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(IOUringNotificationSlot.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return IOUringNotificationSlot.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOUringNotificationSlot.__create(capacity, SIZEOF);
        return IOUringNotificationSlot.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return IOUringNotificationSlot.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : IOUringNotificationSlot.wrap(Buffer.class, address, capacity);
    }

    public static IOUringNotificationSlot malloc(MemoryStack stack) {
        return IOUringNotificationSlot.wrap(IOUringNotificationSlot.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static IOUringNotificationSlot calloc(MemoryStack stack) {
        return IOUringNotificationSlot.wrap(IOUringNotificationSlot.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return IOUringNotificationSlot.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return IOUringNotificationSlot.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static long ntag(long struct) {
        return UNSAFE.getLong(null, struct + (long)TAG);
    }

    public static LongBuffer nresv(long struct) {
        return MemoryUtil.memLongBuffer(struct + (long)RESV, 3);
    }

    public static long nresv(long struct, int index) {
        return UNSAFE.getLong(null, struct + (long)RESV + Checks.check(index, 3) * 8L);
    }

    public static void ntag(long struct, long value) {
        UNSAFE.putLong(null, struct + (long)TAG, value);
    }

    public static void nresv(long struct, LongBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkGT(value, 3);
        }
        MemoryUtil.memCopy(MemoryUtil.memAddress(value), struct + (long)RESV, value.remaining() * 8);
    }

    public static void nresv(long struct, int index, long value) {
        UNSAFE.putLong(null, struct + (long)RESV + Checks.check(index, 3) * 8L, value);
    }

    static {
        Struct.Layout layout = IOUringNotificationSlot.__struct(IOUringNotificationSlot.__member(8), IOUringNotificationSlot.__array(8, 3));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TAG = layout.offsetof(0);
        RESV = layout.offsetof(1);
    }

    public static class Buffer
    extends StructBuffer<IOUringNotificationSlot, Buffer>
    implements NativeResource {
        private static final IOUringNotificationSlot ELEMENT_FACTORY = IOUringNotificationSlot.create(-1L);

        public Buffer(ByteBuffer container) {
            super(container, container.remaining() / SIZEOF);
        }

        public Buffer(long address, int cap) {
            super(address, null, -1, 0, cap, cap);
        }

        Buffer(long address, @Nullable ByteBuffer container, int mark, int pos, int lim, int cap) {
            super(address, container, mark, pos, lim, cap);
        }

        @Override
        protected Buffer self() {
            return this;
        }

        @Override
        protected IOUringNotificationSlot getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u64")
        public long tag() {
            return IOUringNotificationSlot.ntag(this.address());
        }

        @NativeType(value="__u64[3]")
        public LongBuffer resv() {
            return IOUringNotificationSlot.nresv(this.address());
        }

        @NativeType(value="__u64")
        public long resv(int index) {
            return IOUringNotificationSlot.nresv(this.address(), index);
        }

        public Buffer tag(@NativeType(value="__u64") long value) {
            IOUringNotificationSlot.ntag(this.address(), value);
            return this;
        }

        public Buffer resv(@NativeType(value="__u64[3]") LongBuffer value) {
            IOUringNotificationSlot.nresv(this.address(), value);
            return this;
        }

        public Buffer resv(int index, @NativeType(value="__u64") long value) {
            IOUringNotificationSlot.nresv(this.address(), index, value);
            return this;
        }
    }
}

