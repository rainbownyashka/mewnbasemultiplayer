/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package org.lwjgl.stb;

import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct stb_vorbis_alloc")
public class STBVorbisAlloc
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int ALLOC_BUFFER;
    public static final int ALLOC_BUFFER_LENGTH_IN_BYTES;

    public STBVorbisAlloc(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), STBVorbisAlloc.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="char *")
    public ByteBuffer alloc_buffer() {
        return STBVorbisAlloc.nalloc_buffer(this.address());
    }

    public int alloc_buffer_length_in_bytes() {
        return STBVorbisAlloc.nalloc_buffer_length_in_bytes(this.address());
    }

    public STBVorbisAlloc alloc_buffer(@NativeType(value="char *") ByteBuffer value) {
        STBVorbisAlloc.nalloc_buffer(this.address(), value);
        return this;
    }

    public STBVorbisAlloc set(STBVorbisAlloc src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static STBVorbisAlloc malloc() {
        return STBVorbisAlloc.wrap(STBVorbisAlloc.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static STBVorbisAlloc calloc() {
        return STBVorbisAlloc.wrap(STBVorbisAlloc.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static STBVorbisAlloc create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return STBVorbisAlloc.wrap(STBVorbisAlloc.class, MemoryUtil.memAddress(container), container);
    }

    public static STBVorbisAlloc create(long address) {
        return STBVorbisAlloc.wrap(STBVorbisAlloc.class, address);
    }

    @Nullable
    public static STBVorbisAlloc createSafe(long address) {
        return address == 0L ? null : STBVorbisAlloc.wrap(STBVorbisAlloc.class, address);
    }

    public static Buffer malloc(int capacity) {
        return STBVorbisAlloc.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(STBVorbisAlloc.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return STBVorbisAlloc.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = STBVorbisAlloc.__create(capacity, SIZEOF);
        return STBVorbisAlloc.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return STBVorbisAlloc.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : STBVorbisAlloc.wrap(Buffer.class, address, capacity);
    }

    @Deprecated
    public static STBVorbisAlloc mallocStack() {
        return STBVorbisAlloc.malloc(MemoryStack.stackGet());
    }

    @Deprecated
    public static STBVorbisAlloc callocStack() {
        return STBVorbisAlloc.calloc(MemoryStack.stackGet());
    }

    @Deprecated
    public static STBVorbisAlloc mallocStack(MemoryStack stack) {
        return STBVorbisAlloc.malloc(stack);
    }

    @Deprecated
    public static STBVorbisAlloc callocStack(MemoryStack stack) {
        return STBVorbisAlloc.calloc(stack);
    }

    @Deprecated
    public static Buffer mallocStack(int capacity) {
        return STBVorbisAlloc.malloc(capacity, MemoryStack.stackGet());
    }

    @Deprecated
    public static Buffer callocStack(int capacity) {
        return STBVorbisAlloc.calloc(capacity, MemoryStack.stackGet());
    }

    @Deprecated
    public static Buffer mallocStack(int capacity, MemoryStack stack) {
        return STBVorbisAlloc.malloc(capacity, stack);
    }

    @Deprecated
    public static Buffer callocStack(int capacity, MemoryStack stack) {
        return STBVorbisAlloc.calloc(capacity, stack);
    }

    public static STBVorbisAlloc malloc(MemoryStack stack) {
        return STBVorbisAlloc.wrap(STBVorbisAlloc.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static STBVorbisAlloc calloc(MemoryStack stack) {
        return STBVorbisAlloc.wrap(STBVorbisAlloc.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return STBVorbisAlloc.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return STBVorbisAlloc.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static ByteBuffer nalloc_buffer(long struct) {
        return MemoryUtil.memByteBuffer(MemoryUtil.memGetAddress(struct + (long)ALLOC_BUFFER), STBVorbisAlloc.nalloc_buffer_length_in_bytes(struct));
    }

    public static int nalloc_buffer_length_in_bytes(long struct) {
        return UNSAFE.getInt(null, struct + (long)ALLOC_BUFFER_LENGTH_IN_BYTES);
    }

    public static void nalloc_buffer(long struct, ByteBuffer value) {
        MemoryUtil.memPutAddress(struct + (long)ALLOC_BUFFER, MemoryUtil.memAddress(value));
        STBVorbisAlloc.nalloc_buffer_length_in_bytes(struct, value.remaining());
    }

    public static void nalloc_buffer_length_in_bytes(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)ALLOC_BUFFER_LENGTH_IN_BYTES, value);
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)ALLOC_BUFFER));
    }

    static {
        Struct.Layout layout = STBVorbisAlloc.__struct(STBVorbisAlloc.__member(POINTER_SIZE), STBVorbisAlloc.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        ALLOC_BUFFER = layout.offsetof(0);
        ALLOC_BUFFER_LENGTH_IN_BYTES = layout.offsetof(1);
    }

    public static class Buffer
    extends StructBuffer<STBVorbisAlloc, Buffer>
    implements NativeResource {
        private static final STBVorbisAlloc ELEMENT_FACTORY = STBVorbisAlloc.create(-1L);

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
        protected STBVorbisAlloc getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="char *")
        public ByteBuffer alloc_buffer() {
            return STBVorbisAlloc.nalloc_buffer(this.address());
        }

        public int alloc_buffer_length_in_bytes() {
            return STBVorbisAlloc.nalloc_buffer_length_in_bytes(this.address());
        }

        public Buffer alloc_buffer(@NativeType(value="char *") ByteBuffer value) {
            STBVorbisAlloc.nalloc_buffer(this.address(), value);
            return this;
        }
    }
}

