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

@NativeType(value="struct stbtt_kerningentry")
public class STBTTKerningentry
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int GLYPH1;
    public static final int GLYPH2;
    public static final int ADVANCE;

    public STBTTKerningentry(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), STBTTKerningentry.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    public int glyph1() {
        return STBTTKerningentry.nglyph1(this.address());
    }

    public int glyph2() {
        return STBTTKerningentry.nglyph2(this.address());
    }

    public int advance() {
        return STBTTKerningentry.nadvance(this.address());
    }

    public static STBTTKerningentry malloc() {
        return STBTTKerningentry.wrap(STBTTKerningentry.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static STBTTKerningentry calloc() {
        return STBTTKerningentry.wrap(STBTTKerningentry.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static STBTTKerningentry create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return STBTTKerningentry.wrap(STBTTKerningentry.class, MemoryUtil.memAddress(container), container);
    }

    public static STBTTKerningentry create(long address) {
        return STBTTKerningentry.wrap(STBTTKerningentry.class, address);
    }

    @Nullable
    public static STBTTKerningentry createSafe(long address) {
        return address == 0L ? null : STBTTKerningentry.wrap(STBTTKerningentry.class, address);
    }

    public static Buffer malloc(int capacity) {
        return STBTTKerningentry.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(STBTTKerningentry.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return STBTTKerningentry.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = STBTTKerningentry.__create(capacity, SIZEOF);
        return STBTTKerningentry.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return STBTTKerningentry.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : STBTTKerningentry.wrap(Buffer.class, address, capacity);
    }

    public static STBTTKerningentry malloc(MemoryStack stack) {
        return STBTTKerningentry.wrap(STBTTKerningentry.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static STBTTKerningentry calloc(MemoryStack stack) {
        return STBTTKerningentry.wrap(STBTTKerningentry.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return STBTTKerningentry.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return STBTTKerningentry.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nglyph1(long struct) {
        return UNSAFE.getInt(null, struct + (long)GLYPH1);
    }

    public static int nglyph2(long struct) {
        return UNSAFE.getInt(null, struct + (long)GLYPH2);
    }

    public static int nadvance(long struct) {
        return UNSAFE.getInt(null, struct + (long)ADVANCE);
    }

    static {
        Struct.Layout layout = STBTTKerningentry.__struct(STBTTKerningentry.__member(4), STBTTKerningentry.__member(4), STBTTKerningentry.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        GLYPH1 = layout.offsetof(0);
        GLYPH2 = layout.offsetof(1);
        ADVANCE = layout.offsetof(2);
    }

    public static class Buffer
    extends StructBuffer<STBTTKerningentry, Buffer>
    implements NativeResource {
        private static final STBTTKerningentry ELEMENT_FACTORY = STBTTKerningentry.create(-1L);

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
        protected STBTTKerningentry getElementFactory() {
            return ELEMENT_FACTORY;
        }

        public int glyph1() {
            return STBTTKerningentry.nglyph1(this.address());
        }

        public int glyph2() {
            return STBTTKerningentry.nglyph2(this.address());
        }

        public int advance() {
            return STBTTKerningentry.nadvance(this.address());
        }
    }
}

