/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package org.lwjgl.system.linux.liburing;

import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct io_uring_notification_register")
public class IOUringNotificationRegister
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int NR_SLOTS;
    public static final int RESV;
    public static final int RESV2;
    public static final int DATA;
    public static final int RESV3;

    public IOUringNotificationRegister(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), IOUringNotificationRegister.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__u32")
    public int nr_slots() {
        return IOUringNotificationRegister.nnr_slots(this.address());
    }

    @NativeType(value="__u32")
    public int resv() {
        return IOUringNotificationRegister.nresv(this.address());
    }

    @NativeType(value="__u64")
    public long resv2() {
        return IOUringNotificationRegister.nresv2(this.address());
    }

    @NativeType(value="__u64")
    public long data() {
        return IOUringNotificationRegister.ndata(this.address());
    }

    @NativeType(value="__u64")
    public long resv3() {
        return IOUringNotificationRegister.nresv3(this.address());
    }

    public IOUringNotificationRegister nr_slots(@NativeType(value="__u32") int value) {
        IOUringNotificationRegister.nnr_slots(this.address(), value);
        return this;
    }

    public IOUringNotificationRegister resv(@NativeType(value="__u32") int value) {
        IOUringNotificationRegister.nresv(this.address(), value);
        return this;
    }

    public IOUringNotificationRegister resv2(@NativeType(value="__u64") long value) {
        IOUringNotificationRegister.nresv2(this.address(), value);
        return this;
    }

    public IOUringNotificationRegister data(@NativeType(value="__u64") long value) {
        IOUringNotificationRegister.ndata(this.address(), value);
        return this;
    }

    public IOUringNotificationRegister resv3(@NativeType(value="__u64") long value) {
        IOUringNotificationRegister.nresv3(this.address(), value);
        return this;
    }

    public IOUringNotificationRegister set(int nr_slots, int resv, long resv2, long data, long resv3) {
        this.nr_slots(nr_slots);
        this.resv(resv);
        this.resv2(resv2);
        this.data(data);
        this.resv3(resv3);
        return this;
    }

    public IOUringNotificationRegister set(IOUringNotificationRegister src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static IOUringNotificationRegister malloc() {
        return IOUringNotificationRegister.wrap(IOUringNotificationRegister.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static IOUringNotificationRegister calloc() {
        return IOUringNotificationRegister.wrap(IOUringNotificationRegister.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static IOUringNotificationRegister create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return IOUringNotificationRegister.wrap(IOUringNotificationRegister.class, MemoryUtil.memAddress(container), container);
    }

    public static IOUringNotificationRegister create(long address) {
        return IOUringNotificationRegister.wrap(IOUringNotificationRegister.class, address);
    }

    @Nullable
    public static IOUringNotificationRegister createSafe(long address) {
        return address == 0L ? null : IOUringNotificationRegister.wrap(IOUringNotificationRegister.class, address);
    }

    public static Buffer malloc(int capacity) {
        return IOUringNotificationRegister.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(IOUringNotificationRegister.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return IOUringNotificationRegister.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = IOUringNotificationRegister.__create(capacity, SIZEOF);
        return IOUringNotificationRegister.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return IOUringNotificationRegister.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : IOUringNotificationRegister.wrap(Buffer.class, address, capacity);
    }

    public static IOUringNotificationRegister malloc(MemoryStack stack) {
        return IOUringNotificationRegister.wrap(IOUringNotificationRegister.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static IOUringNotificationRegister calloc(MemoryStack stack) {
        return IOUringNotificationRegister.wrap(IOUringNotificationRegister.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return IOUringNotificationRegister.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return IOUringNotificationRegister.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nnr_slots(long struct) {
        return UNSAFE.getInt(null, struct + (long)NR_SLOTS);
    }

    public static int nresv(long struct) {
        return UNSAFE.getInt(null, struct + (long)RESV);
    }

    public static long nresv2(long struct) {
        return UNSAFE.getLong(null, struct + (long)RESV2);
    }

    public static long ndata(long struct) {
        return UNSAFE.getLong(null, struct + (long)DATA);
    }

    public static long nresv3(long struct) {
        return UNSAFE.getLong(null, struct + (long)RESV3);
    }

    public static void nnr_slots(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)NR_SLOTS, value);
    }

    public static void nresv(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)RESV, value);
    }

    public static void nresv2(long struct, long value) {
        UNSAFE.putLong(null, struct + (long)RESV2, value);
    }

    public static void ndata(long struct, long value) {
        UNSAFE.putLong(null, struct + (long)DATA, value);
    }

    public static void nresv3(long struct, long value) {
        UNSAFE.putLong(null, struct + (long)RESV3, value);
    }

    static {
        Struct.Layout layout = IOUringNotificationRegister.__struct(IOUringNotificationRegister.__member(4), IOUringNotificationRegister.__member(4), IOUringNotificationRegister.__member(8), IOUringNotificationRegister.__member(8), IOUringNotificationRegister.__member(8));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        NR_SLOTS = layout.offsetof(0);
        RESV = layout.offsetof(1);
        RESV2 = layout.offsetof(2);
        DATA = layout.offsetof(3);
        RESV3 = layout.offsetof(4);
    }

    public static class Buffer
    extends StructBuffer<IOUringNotificationRegister, Buffer>
    implements NativeResource {
        private static final IOUringNotificationRegister ELEMENT_FACTORY = IOUringNotificationRegister.create(-1L);

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
        protected IOUringNotificationRegister getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__u32")
        public int nr_slots() {
            return IOUringNotificationRegister.nnr_slots(this.address());
        }

        @NativeType(value="__u32")
        public int resv() {
            return IOUringNotificationRegister.nresv(this.address());
        }

        @NativeType(value="__u64")
        public long resv2() {
            return IOUringNotificationRegister.nresv2(this.address());
        }

        @NativeType(value="__u64")
        public long data() {
            return IOUringNotificationRegister.ndata(this.address());
        }

        @NativeType(value="__u64")
        public long resv3() {
            return IOUringNotificationRegister.nresv3(this.address());
        }

        public Buffer nr_slots(@NativeType(value="__u32") int value) {
            IOUringNotificationRegister.nnr_slots(this.address(), value);
            return this;
        }

        public Buffer resv(@NativeType(value="__u32") int value) {
            IOUringNotificationRegister.nresv(this.address(), value);
            return this;
        }

        public Buffer resv2(@NativeType(value="__u64") long value) {
            IOUringNotificationRegister.nresv2(this.address(), value);
            return this;
        }

        public Buffer data(@NativeType(value="__u64") long value) {
            IOUringNotificationRegister.ndata(this.address(), value);
            return this;
        }

        public Buffer resv3(@NativeType(value="__u64") long value) {
            IOUringNotificationRegister.nresv3(this.address(), value);
            return this;
        }
    }
}

