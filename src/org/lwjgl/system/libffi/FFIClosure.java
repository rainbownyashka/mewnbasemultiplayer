/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package org.lwjgl.system.libffi;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javax.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.system.libffi.FFICIF;

@NativeType(value="struct ffi_closure")
public class FFIClosure
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int CIF;
    public static final int FUN;
    public static final int USER_DATA;

    private static native int offsets(long var0);

    public FFIClosure(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), FFIClosure.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="ffi_cif *")
    public FFICIF cif() {
        return FFIClosure.ncif(this.address());
    }

    @NativeType(value="void (*)(ffi_cif*,void*,void**,void*)")
    public long fun() {
        return FFIClosure.nfun(this.address());
    }

    @NativeType(value="void *")
    public long user_data() {
        return FFIClosure.nuser_data(this.address());
    }

    public static FFIClosure malloc() {
        return FFIClosure.wrap(FFIClosure.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static FFIClosure calloc() {
        return FFIClosure.wrap(FFIClosure.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static FFIClosure create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return FFIClosure.wrap(FFIClosure.class, MemoryUtil.memAddress(container), container);
    }

    public static FFIClosure create(long address) {
        return FFIClosure.wrap(FFIClosure.class, address);
    }

    @Nullable
    public static FFIClosure createSafe(long address) {
        return address == 0L ? null : FFIClosure.wrap(FFIClosure.class, address);
    }

    public static Buffer malloc(int capacity) {
        return FFIClosure.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(FFIClosure.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return FFIClosure.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = FFIClosure.__create(capacity, SIZEOF);
        return FFIClosure.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return FFIClosure.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : FFIClosure.wrap(Buffer.class, address, capacity);
    }

    public static FFIClosure malloc(MemoryStack stack) {
        return FFIClosure.wrap(FFIClosure.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static FFIClosure calloc(MemoryStack stack) {
        return FFIClosure.wrap(FFIClosure.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return FFIClosure.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return FFIClosure.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static FFICIF ncif(long struct) {
        return FFICIF.create(MemoryUtil.memGetAddress(struct + (long)CIF));
    }

    public static long nfun(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)FUN);
    }

    public static long nuser_data(long struct) {
        return MemoryUtil.memGetAddress(struct + (long)USER_DATA);
    }

    static {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer offsets = stack.mallocInt(4);
            SIZEOF = FFIClosure.offsets(MemoryUtil.memAddress(offsets));
            CIF = offsets.get(0);
            FUN = offsets.get(1);
            USER_DATA = offsets.get(2);
            ALIGNOF = offsets.get(3);
        }
    }

    public static class Buffer
    extends StructBuffer<FFIClosure, Buffer>
    implements NativeResource {
        private static final FFIClosure ELEMENT_FACTORY = FFIClosure.create(-1L);

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
        protected FFIClosure getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="ffi_cif *")
        public FFICIF cif() {
            return FFIClosure.ncif(this.address());
        }

        @NativeType(value="void (*)(ffi_cif*,void*,void**,void*)")
        public long fun() {
            return FFIClosure.nfun(this.address());
        }

        @NativeType(value="void *")
        public long user_data() {
            return FFIClosure.nuser_data(this.address());
        }
    }
}

