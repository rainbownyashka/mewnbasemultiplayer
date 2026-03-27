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
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct stbtt_vertex")
public class STBTTVertex
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int X;
    public static final int Y;
    public static final int CX;
    public static final int CY;
    public static final int CX1;
    public static final int CY1;
    public static final int TYPE;

    public STBTTVertex(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), STBTTVertex.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="stbtt_vertex_type")
    public short x() {
        return STBTTVertex.nx(this.address());
    }

    @NativeType(value="stbtt_vertex_type")
    public short y() {
        return STBTTVertex.ny(this.address());
    }

    @NativeType(value="stbtt_vertex_type")
    public short cx() {
        return STBTTVertex.ncx(this.address());
    }

    @NativeType(value="stbtt_vertex_type")
    public short cy() {
        return STBTTVertex.ncy(this.address());
    }

    @NativeType(value="stbtt_vertex_type")
    public short cx1() {
        return STBTTVertex.ncx1(this.address());
    }

    @NativeType(value="stbtt_vertex_type")
    public short cy1() {
        return STBTTVertex.ncy1(this.address());
    }

    @NativeType(value="unsigned char")
    public byte type() {
        return STBTTVertex.ntype(this.address());
    }

    public static STBTTVertex malloc() {
        return STBTTVertex.wrap(STBTTVertex.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static STBTTVertex calloc() {
        return STBTTVertex.wrap(STBTTVertex.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static STBTTVertex create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return STBTTVertex.wrap(STBTTVertex.class, MemoryUtil.memAddress(container), container);
    }

    public static STBTTVertex create(long address) {
        return STBTTVertex.wrap(STBTTVertex.class, address);
    }

    @Nullable
    public static STBTTVertex createSafe(long address) {
        return address == 0L ? null : STBTTVertex.wrap(STBTTVertex.class, address);
    }

    public static Buffer malloc(int capacity) {
        return STBTTVertex.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(STBTTVertex.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return STBTTVertex.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = STBTTVertex.__create(capacity, SIZEOF);
        return STBTTVertex.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return STBTTVertex.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : STBTTVertex.wrap(Buffer.class, address, capacity);
    }

    @Deprecated
    public static STBTTVertex mallocStack() {
        return STBTTVertex.malloc(MemoryStack.stackGet());
    }

    @Deprecated
    public static STBTTVertex callocStack() {
        return STBTTVertex.calloc(MemoryStack.stackGet());
    }

    @Deprecated
    public static STBTTVertex mallocStack(MemoryStack stack) {
        return STBTTVertex.malloc(stack);
    }

    @Deprecated
    public static STBTTVertex callocStack(MemoryStack stack) {
        return STBTTVertex.calloc(stack);
    }

    @Deprecated
    public static Buffer mallocStack(int capacity) {
        return STBTTVertex.malloc(capacity, MemoryStack.stackGet());
    }

    @Deprecated
    public static Buffer callocStack(int capacity) {
        return STBTTVertex.calloc(capacity, MemoryStack.stackGet());
    }

    @Deprecated
    public static Buffer mallocStack(int capacity, MemoryStack stack) {
        return STBTTVertex.malloc(capacity, stack);
    }

    @Deprecated
    public static Buffer callocStack(int capacity, MemoryStack stack) {
        return STBTTVertex.calloc(capacity, stack);
    }

    public static STBTTVertex malloc(MemoryStack stack) {
        return STBTTVertex.wrap(STBTTVertex.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static STBTTVertex calloc(MemoryStack stack) {
        return STBTTVertex.wrap(STBTTVertex.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return STBTTVertex.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return STBTTVertex.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static short nx(long struct) {
        return UNSAFE.getShort(null, struct + (long)X);
    }

    public static short ny(long struct) {
        return UNSAFE.getShort(null, struct + (long)Y);
    }

    public static short ncx(long struct) {
        return UNSAFE.getShort(null, struct + (long)CX);
    }

    public static short ncy(long struct) {
        return UNSAFE.getShort(null, struct + (long)CY);
    }

    public static short ncx1(long struct) {
        return UNSAFE.getShort(null, struct + (long)CX1);
    }

    public static short ncy1(long struct) {
        return UNSAFE.getShort(null, struct + (long)CY1);
    }

    public static byte ntype(long struct) {
        return UNSAFE.getByte(null, struct + (long)TYPE);
    }

    static {
        Struct.Layout layout = STBTTVertex.__struct(STBTTVertex.__member(2), STBTTVertex.__member(2), STBTTVertex.__member(2), STBTTVertex.__member(2), STBTTVertex.__member(2), STBTTVertex.__member(2), STBTTVertex.__member(1));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        X = layout.offsetof(0);
        Y = layout.offsetof(1);
        CX = layout.offsetof(2);
        CY = layout.offsetof(3);
        CX1 = layout.offsetof(4);
        CY1 = layout.offsetof(5);
        TYPE = layout.offsetof(6);
    }

    public static class Buffer
    extends StructBuffer<STBTTVertex, Buffer>
    implements NativeResource {
        private static final STBTTVertex ELEMENT_FACTORY = STBTTVertex.create(-1L);

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
        protected STBTTVertex getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="stbtt_vertex_type")
        public short x() {
            return STBTTVertex.nx(this.address());
        }

        @NativeType(value="stbtt_vertex_type")
        public short y() {
            return STBTTVertex.ny(this.address());
        }

        @NativeType(value="stbtt_vertex_type")
        public short cx() {
            return STBTTVertex.ncx(this.address());
        }

        @NativeType(value="stbtt_vertex_type")
        public short cy() {
            return STBTTVertex.ncy(this.address());
        }

        @NativeType(value="stbtt_vertex_type")
        public short cx1() {
            return STBTTVertex.ncx1(this.address());
        }

        @NativeType(value="stbtt_vertex_type")
        public short cy1() {
            return STBTTVertex.ncy1(this.address());
        }

        @NativeType(value="unsigned char")
        public byte type() {
            return STBTTVertex.ntype(this.address());
        }
    }
}

