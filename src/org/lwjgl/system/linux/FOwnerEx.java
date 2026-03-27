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

@NativeType(value="struct f_owner_ex")
public class FOwnerEx
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int TYPE;
    public static final int PID;

    public FOwnerEx(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FOwnerEx.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public int type() {
        return FOwnerEx.ntype(this.address());
    }

    @NativeType(value="pid_t")
    public int pid() {
        return FOwnerEx.npid(this.address());
    }

    public FOwnerEx type(int value) {
        FOwnerEx.ntype(this.address(), value);
        return this;
    }

    public FOwnerEx pid(@NativeType(value="pid_t") int value) {
        FOwnerEx.npid(this.address(), value);
        return this;
    }

    public FOwnerEx set(int type, int pid) {
        this.type(type);
        this.pid(pid);
        return this;
    }

    public FOwnerEx set(FOwnerEx src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static FOwnerEx malloc() {
        return FOwnerEx.wrap(FOwnerEx.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static FOwnerEx calloc() {
        return FOwnerEx.wrap(FOwnerEx.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static FOwnerEx create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return FOwnerEx.wrap(FOwnerEx.class, MemoryUtil.memAddress(container), container);
    }

    public static FOwnerEx create(long address) {
        return FOwnerEx.wrap(FOwnerEx.class, address);
    }

    @Nullable
    public static FOwnerEx createSafe(long address) {
        return address == 0L ? null : FOwnerEx.wrap(FOwnerEx.class, address);
    }

    public static Buffer malloc(int capacity) {
        return FOwnerEx.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(FOwnerEx.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return FOwnerEx.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = FOwnerEx.__create(capacity, SIZEOF);
        return FOwnerEx.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return FOwnerEx.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : FOwnerEx.wrap(Buffer.class, address, capacity);
    }

    public static FOwnerEx malloc(MemoryStack stack) {
        return FOwnerEx.wrap(FOwnerEx.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static FOwnerEx calloc(MemoryStack stack) {
        return FOwnerEx.wrap(FOwnerEx.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return FOwnerEx.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return FOwnerEx.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int ntype(long struct) {
        return UNSAFE.getInt(null, struct + (long)TYPE);
    }

    public static int npid(long struct) {
        return UNSAFE.getInt(null, struct + (long)PID);
    }

    public static void ntype(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)TYPE, value);
    }

    public static void npid(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)PID, value);
    }

    static {
        Struct.Layout layout = FOwnerEx.__struct(FOwnerEx.__member(4), FOwnerEx.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        TYPE = layout.offsetof(0);
        PID = layout.offsetof(1);
    }

    public static class Buffer
    extends StructBuffer<FOwnerEx, Buffer>
    implements NativeResource {
        private static final FOwnerEx ELEMENT_FACTORY = FOwnerEx.create(-1L);

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
        protected FOwnerEx getElementFactory() {
            return ELEMENT_FACTORY;
        }

        public int type() {
            return FOwnerEx.ntype(this.address());
        }

        @NativeType(value="pid_t")
        public int pid() {
            return FOwnerEx.npid(this.address());
        }

        public Buffer type(int value) {
            FOwnerEx.ntype(this.address(), value);
            return this;
        }

        public Buffer pid(@NativeType(value="pid_t") int value) {
            FOwnerEx.npid(this.address(), value);
            return this;
        }
    }
}

