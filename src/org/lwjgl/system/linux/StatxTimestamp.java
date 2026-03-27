/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package org.lwjgl.system.linux;

import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct statx_timestamp")
public class StatxTimestamp
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TV_SEC;
    public static final int TV_NSEC;
    public static final int __RESERVED;

    public StatxTimestamp(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), StatxTimestamp.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="__s64")
    public long tv_sec() {
        return StatxTimestamp.ntv_sec(this.address());
    }

    @NativeType(value="__u32")
    public int tv_nsec() {
        return StatxTimestamp.ntv_nsec(this.address());
    }

    public StatxTimestamp tv_sec(@NativeType(value="__s64") long value) {
        StatxTimestamp.ntv_sec(this.address(), value);
        return this;
    }

    public StatxTimestamp tv_nsec(@NativeType(value="__u32") int value) {
        StatxTimestamp.ntv_nsec(this.address(), value);
        return this;
    }

    public StatxTimestamp set(long tv_sec, int tv_nsec) {
        this.tv_sec(tv_sec);
        this.tv_nsec(tv_nsec);
        return this;
    }

    public StatxTimestamp set(StatxTimestamp src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static StatxTimestamp malloc() {
        return StatxTimestamp.wrap(StatxTimestamp.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static StatxTimestamp calloc() {
        return StatxTimestamp.wrap(StatxTimestamp.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static StatxTimestamp create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return StatxTimestamp.wrap(StatxTimestamp.class, MemoryUtil.memAddress(container), container);
    }

    public static StatxTimestamp create(long address) {
        return StatxTimestamp.wrap(StatxTimestamp.class, address);
    }

    @Nullable
    public static StatxTimestamp createSafe(long address) {
        return address == 0L ? null : StatxTimestamp.wrap(StatxTimestamp.class, address);
    }

    public static Buffer malloc(int capacity) {
        return StatxTimestamp.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(StatxTimestamp.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return StatxTimestamp.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = StatxTimestamp.__create(capacity, SIZEOF);
        return StatxTimestamp.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return StatxTimestamp.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : StatxTimestamp.wrap(Buffer.class, address, capacity);
    }

    public static StatxTimestamp malloc(MemoryStack stack) {
        return StatxTimestamp.wrap(StatxTimestamp.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static StatxTimestamp calloc(MemoryStack stack) {
        return StatxTimestamp.wrap(StatxTimestamp.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return StatxTimestamp.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return StatxTimestamp.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static long ntv_sec(long struct) {
        return UNSAFE.getLong(null, struct + (long)TV_SEC);
    }

    public static int ntv_nsec(long struct) {
        return UNSAFE.getInt(null, struct + (long)TV_NSEC);
    }

    public static int n__reserved(long struct) {
        return UNSAFE.getInt(null, struct + (long)__RESERVED);
    }

    public static void ntv_sec(long struct, long value) {
        UNSAFE.putLong(null, struct + (long)TV_SEC, value);
    }

    public static void ntv_nsec(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)TV_NSEC, value);
    }

    public static void n__reserved(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)__RESERVED, value);
    }

    static {
        Struct.Layout layout = StatxTimestamp.__struct(StatxTimestamp.__member(8), StatxTimestamp.__member(4), StatxTimestamp.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TV_SEC = layout.offsetof(0);
        TV_NSEC = layout.offsetof(1);
        __RESERVED = layout.offsetof(2);
    }

    public static class Buffer
    extends StructBuffer<StatxTimestamp, Buffer>
    implements NativeResource {
        private static final StatxTimestamp ELEMENT_FACTORY = StatxTimestamp.create(-1L);

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
        protected StatxTimestamp getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="__s64")
        public long tv_sec() {
            return StatxTimestamp.ntv_sec(this.address());
        }

        @NativeType(value="__u32")
        public int tv_nsec() {
            return StatxTimestamp.ntv_nsec(this.address());
        }

        public Buffer tv_sec(@NativeType(value="__s64") long value) {
            StatxTimestamp.ntv_sec(this.address(), value);
            return this;
        }

        public Buffer tv_nsec(@NativeType(value="__u32") int value) {
            StatxTimestamp.ntv_nsec(this.address(), value);
            return this;
        }
    }
}

