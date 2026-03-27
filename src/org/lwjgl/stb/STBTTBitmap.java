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

@NativeType(value="struct stbtt__bitmap")
public class STBTTBitmap
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int W;
    public static final int H;
    public static final int STRIDE;
    public static final int PIXELS;

    public STBTTBitmap(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), STBTTBitmap.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public int w() {
        return STBTTBitmap.nw(this.address());
    }

    public int h() {
        return STBTTBitmap.nh(this.address());
    }

    public int stride() {
        return STBTTBitmap.nstride(this.address());
    }

    @NativeType(value="unsigned char *")
    public ByteBuffer pixels(int capacity) {
        return STBTTBitmap.npixels(this.address(), capacity);
    }

    public STBTTBitmap w(int value) {
        STBTTBitmap.nw(this.address(), value);
        return this;
    }

    public STBTTBitmap h(int value) {
        STBTTBitmap.nh(this.address(), value);
        return this;
    }

    public STBTTBitmap stride(int value) {
        STBTTBitmap.nstride(this.address(), value);
        return this;
    }

    public STBTTBitmap pixels(@NativeType(value="unsigned char *") ByteBuffer value) {
        STBTTBitmap.npixels(this.address(), value);
        return this;
    }

    public STBTTBitmap set(int w, int h, int stride, ByteBuffer pixels) {
        this.w(w);
        this.h(h);
        this.stride(stride);
        this.pixels(pixels);
        return this;
    }

    public STBTTBitmap set(STBTTBitmap src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static STBTTBitmap malloc() {
        return STBTTBitmap.wrap(STBTTBitmap.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static STBTTBitmap calloc() {
        return STBTTBitmap.wrap(STBTTBitmap.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static STBTTBitmap create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return STBTTBitmap.wrap(STBTTBitmap.class, MemoryUtil.memAddress(container), container);
    }

    public static STBTTBitmap create(long address) {
        return STBTTBitmap.wrap(STBTTBitmap.class, address);
    }

    @Nullable
    public static STBTTBitmap createSafe(long address) {
        return address == 0L ? null : STBTTBitmap.wrap(STBTTBitmap.class, address);
    }

    public static Buffer malloc(int capacity) {
        return STBTTBitmap.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(STBTTBitmap.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return STBTTBitmap.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = STBTTBitmap.__create(capacity, SIZEOF);
        return STBTTBitmap.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return STBTTBitmap.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : STBTTBitmap.wrap(Buffer.class, address, capacity);
    }

    @Deprecated
    public static STBTTBitmap mallocStack() {
        return STBTTBitmap.malloc(MemoryStack.stackGet());
    }

    @Deprecated
    public static STBTTBitmap callocStack() {
        return STBTTBitmap.calloc(MemoryStack.stackGet());
    }

    @Deprecated
    public static STBTTBitmap mallocStack(MemoryStack stack) {
        return STBTTBitmap.malloc(stack);
    }

    @Deprecated
    public static STBTTBitmap callocStack(MemoryStack stack) {
        return STBTTBitmap.calloc(stack);
    }

    @Deprecated
    public static Buffer mallocStack(int capacity) {
        return STBTTBitmap.malloc(capacity, MemoryStack.stackGet());
    }

    @Deprecated
    public static Buffer callocStack(int capacity) {
        return STBTTBitmap.calloc(capacity, MemoryStack.stackGet());
    }

    @Deprecated
    public static Buffer mallocStack(int capacity, MemoryStack stack) {
        return STBTTBitmap.malloc(capacity, stack);
    }

    @Deprecated
    public static Buffer callocStack(int capacity, MemoryStack stack) {
        return STBTTBitmap.calloc(capacity, stack);
    }

    public static STBTTBitmap malloc(MemoryStack stack) {
        return STBTTBitmap.wrap(STBTTBitmap.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static STBTTBitmap calloc(MemoryStack stack) {
        return STBTTBitmap.wrap(STBTTBitmap.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return STBTTBitmap.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return STBTTBitmap.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nw(long struct) {
        return UNSAFE.getInt(null, struct + (long)W);
    }

    public static int nh(long struct) {
        return UNSAFE.getInt(null, struct + (long)H);
    }

    public static int nstride(long struct) {
        return UNSAFE.getInt(null, struct + (long)STRIDE);
    }

    public static ByteBuffer npixels(long struct, int capacity) {
        return MemoryUtil.memByteBuffer(MemoryUtil.memGetAddress(struct + (long)PIXELS), capacity);
    }

    public static void nw(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)W, value);
    }

    public static void nh(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)H, value);
    }

    public static void nstride(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)STRIDE, value);
    }

    public static void npixels(long struct, ByteBuffer value) {
        MemoryUtil.memPutAddress(struct + (long)PIXELS, MemoryUtil.memAddress(value));
    }

    public static void validate(long struct) {
        Checks.check(MemoryUtil.memGetAddress(struct + (long)PIXELS));
    }

    static {
        Struct.Layout layout = STBTTBitmap.__struct(STBTTBitmap.__member(4), STBTTBitmap.__member(4), STBTTBitmap.__member(4), STBTTBitmap.__member(POINTER_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        W = layout.offsetof(0);
        H = layout.offsetof(1);
        STRIDE = layout.offsetof(2);
        PIXELS = layout.offsetof(3);
    }

    public static class Buffer
    extends StructBuffer<STBTTBitmap, Buffer>
    implements NativeResource {
        private static final STBTTBitmap ELEMENT_FACTORY = STBTTBitmap.create(-1L);

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
        protected STBTTBitmap getElementFactory() {
            return ELEMENT_FACTORY;
        }

        public int w() {
            return STBTTBitmap.nw(this.address());
        }

        public int h() {
            return STBTTBitmap.nh(this.address());
        }

        public int stride() {
            return STBTTBitmap.nstride(this.address());
        }

        @NativeType(value="unsigned char *")
        public ByteBuffer pixels(int capacity) {
            return STBTTBitmap.npixels(this.address(), capacity);
        }

        public Buffer w(int value) {
            STBTTBitmap.nw(this.address(), value);
            return this;
        }

        public Buffer h(int value) {
            STBTTBitmap.nh(this.address(), value);
            return this;
        }

        public Buffer stride(int value) {
            STBTTBitmap.nstride(this.address(), value);
            return this;
        }

        public Buffer pixels(@NativeType(value="unsigned char *") ByteBuffer value) {
            STBTTBitmap.npixels(this.address(), value);
            return this;
        }
    }
}

