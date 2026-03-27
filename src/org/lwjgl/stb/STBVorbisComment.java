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
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

@NativeType(value="struct stb_vorbis_comment")
public class STBVorbisComment
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int VENDOR;
    public static final int COMMENT_LIST_LENGTH;
    public static final int COMMENT_LIST;

    public STBVorbisComment(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), STBVorbisComment.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="char *")
    public ByteBuffer vendor() {
        return STBVorbisComment.nvendor(this.address());
    }

    @NativeType(value="char *")
    public String vendorString() {
        return STBVorbisComment.nvendorString(this.address());
    }

    public int comment_list_length() {
        return STBVorbisComment.ncomment_list_length(this.address());
    }

    @NativeType(value="char **")
    public PointerBuffer comment_list() {
        return STBVorbisComment.ncomment_list(this.address());
    }

    public static STBVorbisComment malloc() {
        return STBVorbisComment.wrap(STBVorbisComment.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static STBVorbisComment calloc() {
        return STBVorbisComment.wrap(STBVorbisComment.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static STBVorbisComment create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return STBVorbisComment.wrap(STBVorbisComment.class, MemoryUtil.memAddress(container), container);
    }

    public static STBVorbisComment create(long address) {
        return STBVorbisComment.wrap(STBVorbisComment.class, address);
    }

    @Nullable
    public static STBVorbisComment createSafe(long address) {
        return address == 0L ? null : STBVorbisComment.wrap(STBVorbisComment.class, address);
    }

    public static Buffer malloc(int capacity) {
        return STBVorbisComment.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(STBVorbisComment.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return STBVorbisComment.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = STBVorbisComment.__create(capacity, SIZEOF);
        return STBVorbisComment.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return STBVorbisComment.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : STBVorbisComment.wrap(Buffer.class, address, capacity);
    }

    public static STBVorbisComment malloc(MemoryStack stack) {
        return STBVorbisComment.wrap(STBVorbisComment.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static STBVorbisComment calloc(MemoryStack stack) {
        return STBVorbisComment.wrap(STBVorbisComment.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return STBVorbisComment.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return STBVorbisComment.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static ByteBuffer nvendor(long struct) {
        return MemoryUtil.memByteBufferNT1(MemoryUtil.memGetAddress(struct + (long)VENDOR));
    }

    public static String nvendorString(long struct) {
        return MemoryUtil.memASCII(MemoryUtil.memGetAddress(struct + (long)VENDOR));
    }

    public static int ncomment_list_length(long struct) {
        return UNSAFE.getInt(null, struct + (long)COMMENT_LIST_LENGTH);
    }

    public static PointerBuffer ncomment_list(long struct) {
        return MemoryUtil.memPointerBuffer(MemoryUtil.memGetAddress(struct + (long)COMMENT_LIST), STBVorbisComment.ncomment_list_length(struct));
    }

    static {
        Struct.Layout layout = STBVorbisComment.__struct(STBVorbisComment.__member(POINTER_SIZE), STBVorbisComment.__member(4), STBVorbisComment.__member(POINTER_SIZE));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        VENDOR = layout.offsetof(0);
        COMMENT_LIST_LENGTH = layout.offsetof(1);
        COMMENT_LIST = layout.offsetof(2);
    }

    public static class Buffer
    extends StructBuffer<STBVorbisComment, Buffer>
    implements NativeResource {
        private static final STBVorbisComment ELEMENT_FACTORY = STBVorbisComment.create(-1L);

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
        protected STBVorbisComment getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="char *")
        public ByteBuffer vendor() {
            return STBVorbisComment.nvendor(this.address());
        }

        @NativeType(value="char *")
        public String vendorString() {
            return STBVorbisComment.nvendorString(this.address());
        }

        public int comment_list_length() {
            return STBVorbisComment.ncomment_list_length(this.address());
        }

        @NativeType(value="char **")
        public PointerBuffer comment_list() {
            return STBVorbisComment.ncomment_list(this.address());
        }
    }
}

