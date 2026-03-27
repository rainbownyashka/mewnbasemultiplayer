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
import org.lwjgl.stb.STBIEOFCallback;
import org.lwjgl.stb.STBIEOFCallbackI;
import org.lwjgl.stb.STBIReadCallback;
import org.lwjgl.stb.STBIReadCallbackI;
import org.lwjgl.stb.STBISkipCallback;
import org.lwjgl.stb.STBISkipCallbackI;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct stbi_io_callbacks")
public class STBIIOCallbacks
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int READ;
    public static final int SKIP;
    public static final int EOF;

    public STBIIOCallbacks(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), STBIIOCallbacks.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="int (*) (void *, char *, int)")
    public STBIReadCallback read() {
        return STBIIOCallbacks.nread(this.address());
    }

    @NativeType(value="void (*) (void *, int)")
    public STBISkipCallback skip() {
        return STBIIOCallbacks.nskip(this.address());
    }

    @NativeType(value="int (*) (void *)")
    public STBIEOFCallback eof() {
        return STBIIOCallbacks.neof(this.address());
    }

    public STBIIOCallbacks read(@NativeType(value="int (*) (void *, char *, int)") STBIReadCallbackI value) {
        STBIIOCallbacks.nread(this.address(), value);
        return this;
    }

    public STBIIOCallbacks skip(@NativeType(value="void (*) (void *, int)") STBISkipCallbackI value) {
        STBIIOCallbacks.nskip(this.address(), value);
        return this;
    }

    public STBIIOCallbacks eof(@NativeType(value="int (*) (void *)") STBIEOFCallbackI value) {
        STBIIOCallbacks.neof(this.address(), value);
        return this;
    }

    public STBIIOCallbacks set(STBIReadCallbackI read, STBISkipCallbackI skip, STBIEOFCallbackI eof) {
        this.read(read);
        this.skip(skip);
        this.eof(eof);
        return this;
    }

    public STBIIOCallbacks set(STBIIOCallbacks src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static STBIIOCallbacks malloc() {
        return STBIIOCallbacks.wrap(STBIIOCallbacks.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static STBIIOCallbacks calloc() {
        return STBIIOCallbacks.wrap(STBIIOCallbacks.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static STBIIOCallbacks create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return STBIIOCallbacks.wrap(STBIIOCallbacks.class, MemoryUtil.memAddress(container), container);
    }

    public static STBIIOCallbacks create(long address) {
        return STBIIOCallbacks.wrap(STBIIOCallbacks.class, address);
    }

    @Nullable
    public static STBIIOCallbacks createSafe(long address) {
        return address == 0L ? null : STBIIOCallbacks.wrap(STBIIOCallbacks.class, address);
    }

    public static Buffer malloc(int capacity) {
        return STBIIOCallbacks.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(STBIIOCallbacks.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return STBIIOCallbacks.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = STBIIOCallbacks.__create(capacity, SIZEOF);
        return STBIIOCallbacks.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return STBIIOCallbacks.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : STBIIOCallbacks.wrap(Buffer.class, address, capacity);
    }

    @Deprecated
    public static STBIIOCallbacks mallocStack() {
        return STBIIOCallbacks.malloc(MemoryStack.stackGet());
    }

    @Deprecated
    public static STBIIOCallbacks callocStack() {
        return STBIIOCallbacks.calloc(MemoryStack.stackGet());
    }

    @Deprecated
    public static STBIIOCallbacks mallocStack(MemoryStack stack) {
        return STBIIOCallbacks.malloc(stack);
    }

    @Deprecated
    public static STBIIOCallbacks callocStack(MemoryStack stack) {
        return STBIIOCallbacks.calloc(stack);
    }

    @Deprecated
    public static Buffer mallocStack(int capacity) {
        return STBIIOCallbacks.malloc(capacity, MemoryStack.stackGet());
    }

    @Deprecated
    public static Buffer callocStack(int capacity) {
        return STBIIOCallbacks.calloc(capacity, MemoryStack.stackGet());
    }

    @Deprecated
    public static Buffer mallocStack(int capacity, MemoryStack stack) {
        return STBIIOCallbacks.malloc(capacity, stack);
    }

    @Deprecated
    public static Buffer callocStack(int capacity, MemoryStack stack) {
        return STBIIOCallbacks.calloc(capacity, stack);
    }

    public static STBIIOCallbacks malloc(MemoryStack stack) {
        return STBIIOCallbacks.wrap(STBIIOCallbacks.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static STBIIOCallbacks calloc(MemoryStack stack) {
        return STBIIOCallbacks.wrap(STBIIOCallbacks.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return STBIIOCallbacks.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return STBIIOCallbacks.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static STBIReadCallback nread(long struct) {
        return STBIReadCallback.create(MemoryUtil.memGetAddress(struct + (long)READ));
    }

    public static STBISkipCallback nskip(long struct) {
        return STBISkipCallback.create(MemoryUtil.memGetAddress(struct + (long)SKIP));
    }

    public static STBIEOFCallback neof(long struct) {
        return STBIEOFCallback.create(MemoryUtil.memGetAddress(struct + (long)EOF));
    }

    public static void nread(long struct, STBIReadCallbackI value) {
        MemoryUtil.memPutAddress(struct + (long)READ, value.address());
    }

    public static void nskip(long struct, STBISkipCallbackI value) {
        MemoryUtil.memPutAddress(struct + (long)SKIP, value.address());
    }

    public static void neof(long struct, STBIEOFCallbackI value) {
        MemoryUtil.memPutAddress(struct + (long)EOF, value.address());
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)READ));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)SKIP));
        Checks.check(MemoryUtil.memGetAddress(struct + (long)EOF));
    }

    static {
        Struct.Layout layout = STBIIOCallbacks.__struct(STBIIOCallbacks.__member(POINTER_SIZE), STBIIOCallbacks.__member(POINTER_SIZE), STBIIOCallbacks.__member(POINTER_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        READ = layout.offsetof(0);
        SKIP = layout.offsetof(1);
        EOF = layout.offsetof(2);
    }

    public static class Buffer
    extends StructBuffer<STBIIOCallbacks, Buffer>
    implements NativeResource {
        private static final STBIIOCallbacks ELEMENT_FACTORY = STBIIOCallbacks.create(-1L);

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
        protected STBIIOCallbacks getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="int (*) (void *, char *, int)")
        public STBIReadCallback read() {
            return STBIIOCallbacks.nread(this.address());
        }

        @NativeType(value="void (*) (void *, int)")
        public STBISkipCallback skip() {
            return STBIIOCallbacks.nskip(this.address());
        }

        @NativeType(value="int (*) (void *)")
        public STBIEOFCallback eof() {
            return STBIIOCallbacks.neof(this.address());
        }

        public Buffer read(@NativeType(value="int (*) (void *, char *, int)") STBIReadCallbackI value) {
            STBIIOCallbacks.nread(this.address(), value);
            return this;
        }

        public Buffer skip(@NativeType(value="void (*) (void *, int)") STBISkipCallbackI value) {
            STBIIOCallbacks.nskip(this.address(), value);
            return this;
        }

        public Buffer eof(@NativeType(value="int (*) (void *)") STBIEOFCallbackI value) {
            STBIIOCallbacks.neof(this.address(), value);
            return this;
        }
    }
}

