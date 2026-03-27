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

@NativeType(value="struct flock64")
public class Flock
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int L_TYPE;
    public static final int L_WHENCE;
    public static final int L_START;
    public static final int L_LEN;
    public static final int L_PID;

    public Flock(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), Flock.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public short l_type() {
        return Flock.nl_type(this.address());
    }

    public short l_whence() {
        return Flock.nl_whence(this.address());
    }

    @NativeType(value="off_t")
    public long l_start() {
        return Flock.nl_start(this.address());
    }

    @NativeType(value="off_t")
    public long l_len() {
        return Flock.nl_len(this.address());
    }

    @NativeType(value="pid_t")
    public int l_pid() {
        return Flock.nl_pid(this.address());
    }

    public Flock l_type(short value) {
        Flock.nl_type(this.address(), value);
        return this;
    }

    public Flock l_whence(short value) {
        Flock.nl_whence(this.address(), value);
        return this;
    }

    public Flock l_start(@NativeType(value="off_t") long value) {
        Flock.nl_start(this.address(), value);
        return this;
    }

    public Flock l_len(@NativeType(value="off_t") long value) {
        Flock.nl_len(this.address(), value);
        return this;
    }

    public Flock l_pid(@NativeType(value="pid_t") int value) {
        Flock.nl_pid(this.address(), value);
        return this;
    }

    public Flock set(short l_type, short l_whence, long l_start, long l_len, int l_pid) {
        this.l_type(l_type);
        this.l_whence(l_whence);
        this.l_start(l_start);
        this.l_len(l_len);
        this.l_pid(l_pid);
        return this;
    }

    public Flock set(Flock src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static Flock malloc() {
        return Flock.wrap(Flock.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static Flock calloc() {
        return Flock.wrap(Flock.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static Flock create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return Flock.wrap(Flock.class, MemoryUtil.memAddress(container), container);
    }

    public static Flock create(long address) {
        return Flock.wrap(Flock.class, address);
    }

    @Nullable
    public static Flock createSafe(long address) {
        return address == 0L ? null : Flock.wrap(Flock.class, address);
    }

    public static Buffer malloc(int capacity) {
        return Flock.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(Flock.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return Flock.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = Flock.__create(capacity, SIZEOF);
        return Flock.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return Flock.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : Flock.wrap(Buffer.class, address, capacity);
    }

    public static Flock malloc(MemoryStack stack) {
        return Flock.wrap(Flock.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static Flock calloc(MemoryStack stack) {
        return Flock.wrap(Flock.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return Flock.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return Flock.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static short nl_type(long struct) {
        return UNSAFE.getShort(null, struct + (long)L_TYPE);
    }

    public static short nl_whence(long struct) {
        return UNSAFE.getShort(null, struct + (long)L_WHENCE);
    }

    public static long nl_start(long struct) {
        return UNSAFE.getLong(null, struct + (long)L_START);
    }

    public static long nl_len(long struct) {
        return UNSAFE.getLong(null, struct + (long)L_LEN);
    }

    public static int nl_pid(long struct) {
        return UNSAFE.getInt(null, struct + (long)L_PID);
    }

    public static void nl_type(long struct, short value) {
        UNSAFE.putShort(null, struct + (long)L_TYPE, value);
    }

    public static void nl_whence(long struct, short value) {
        UNSAFE.putShort(null, struct + (long)L_WHENCE, value);
    }

    public static void nl_start(long struct, long value) {
        UNSAFE.putLong(null, struct + (long)L_START, value);
    }

    public static void nl_len(long struct, long value) {
        UNSAFE.putLong(null, struct + (long)L_LEN, value);
    }

    public static void nl_pid(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)L_PID, value);
    }

    static {
        Struct.Layout layout = Flock.__struct(Flock.__member(2), Flock.__member(2), Flock.__member(8), Flock.__member(8), Flock.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        L_TYPE = layout.offsetof(0);
        L_WHENCE = layout.offsetof(1);
        L_START = layout.offsetof(2);
        L_LEN = layout.offsetof(3);
        L_PID = layout.offsetof(4);
    }

    public static class Buffer
    extends StructBuffer<Flock, Buffer>
    implements NativeResource {
        private static final Flock ELEMENT_FACTORY = Flock.create(-1L);

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
        protected Flock getElementFactory() {
            return ELEMENT_FACTORY;
        }

        public short l_type() {
            return Flock.nl_type(this.address());
        }

        public short l_whence() {
            return Flock.nl_whence(this.address());
        }

        @NativeType(value="off_t")
        public long l_start() {
            return Flock.nl_start(this.address());
        }

        @NativeType(value="off_t")
        public long l_len() {
            return Flock.nl_len(this.address());
        }

        @NativeType(value="pid_t")
        public int l_pid() {
            return Flock.nl_pid(this.address());
        }

        public Buffer l_type(short value) {
            Flock.nl_type(this.address(), value);
            return this;
        }

        public Buffer l_whence(short value) {
            Flock.nl_whence(this.address(), value);
            return this;
        }

        public Buffer l_start(@NativeType(value="off_t") long value) {
            Flock.nl_start(this.address(), value);
            return this;
        }

        public Buffer l_len(@NativeType(value="off_t") long value) {
            Flock.nl_len(this.address(), value);
            return this;
        }

        public Buffer l_pid(@NativeType(value="pid_t") int value) {
            Flock.nl_pid(this.address(), value);
            return this;
        }
    }
}

