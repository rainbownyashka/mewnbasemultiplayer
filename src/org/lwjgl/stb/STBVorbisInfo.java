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

@NativeType(value="struct stb_vorbis_info")
public class STBVorbisInfo
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int SAMPLE_RATE;
    public static final int CHANNELS;
    public static final int SETUP_MEMORY_REQUIRED;
    public static final int SETUP_TEMP_MEMORY_REQUIRED;
    public static final int TEMP_MEMORY_REQUIRED;
    public static final int MAX_FRAME_SIZE;

    public STBVorbisInfo(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), STBVorbisInfo.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="unsigned int")
    public int sample_rate() {
        return STBVorbisInfo.nsample_rate(this.address());
    }

    public int channels() {
        return STBVorbisInfo.nchannels(this.address());
    }

    @NativeType(value="unsigned int")
    public int setup_memory_required() {
        return STBVorbisInfo.nsetup_memory_required(this.address());
    }

    @NativeType(value="unsigned int")
    public int setup_temp_memory_required() {
        return STBVorbisInfo.nsetup_temp_memory_required(this.address());
    }

    @NativeType(value="unsigned int")
    public int temp_memory_required() {
        return STBVorbisInfo.ntemp_memory_required(this.address());
    }

    public int max_frame_size() {
        return STBVorbisInfo.nmax_frame_size(this.address());
    }

    public static STBVorbisInfo malloc() {
        return STBVorbisInfo.wrap(STBVorbisInfo.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static STBVorbisInfo calloc() {
        return STBVorbisInfo.wrap(STBVorbisInfo.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static STBVorbisInfo create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return STBVorbisInfo.wrap(STBVorbisInfo.class, MemoryUtil.memAddress(container), container);
    }

    public static STBVorbisInfo create(long address) {
        return STBVorbisInfo.wrap(STBVorbisInfo.class, address);
    }

    @Nullable
    public static STBVorbisInfo createSafe(long address) {
        return address == 0L ? null : STBVorbisInfo.wrap(STBVorbisInfo.class, address);
    }

    public static Buffer malloc(int capacity) {
        return STBVorbisInfo.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(STBVorbisInfo.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return STBVorbisInfo.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = STBVorbisInfo.__create(capacity, SIZEOF);
        return STBVorbisInfo.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return STBVorbisInfo.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : STBVorbisInfo.wrap(Buffer.class, address, capacity);
    }

    @Deprecated
    public static STBVorbisInfo mallocStack() {
        return STBVorbisInfo.malloc(MemoryStack.stackGet());
    }

    @Deprecated
    public static STBVorbisInfo callocStack() {
        return STBVorbisInfo.calloc(MemoryStack.stackGet());
    }

    @Deprecated
    public static STBVorbisInfo mallocStack(MemoryStack stack) {
        return STBVorbisInfo.malloc(stack);
    }

    @Deprecated
    public static STBVorbisInfo callocStack(MemoryStack stack) {
        return STBVorbisInfo.calloc(stack);
    }

    @Deprecated
    public static Buffer mallocStack(int capacity) {
        return STBVorbisInfo.malloc(capacity, MemoryStack.stackGet());
    }

    @Deprecated
    public static Buffer callocStack(int capacity) {
        return STBVorbisInfo.calloc(capacity, MemoryStack.stackGet());
    }

    @Deprecated
    public static Buffer mallocStack(int capacity, MemoryStack stack) {
        return STBVorbisInfo.malloc(capacity, stack);
    }

    @Deprecated
    public static Buffer callocStack(int capacity, MemoryStack stack) {
        return STBVorbisInfo.calloc(capacity, stack);
    }

    public static STBVorbisInfo malloc(MemoryStack stack) {
        return STBVorbisInfo.wrap(STBVorbisInfo.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static STBVorbisInfo calloc(MemoryStack stack) {
        return STBVorbisInfo.wrap(STBVorbisInfo.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return STBVorbisInfo.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return STBVorbisInfo.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nsample_rate(long struct) {
        return UNSAFE.getInt(null, struct + (long)SAMPLE_RATE);
    }

    public static int nchannels(long struct) {
        return UNSAFE.getInt(null, struct + (long)CHANNELS);
    }

    public static int nsetup_memory_required(long struct) {
        return UNSAFE.getInt(null, struct + (long)SETUP_MEMORY_REQUIRED);
    }

    public static int nsetup_temp_memory_required(long struct) {
        return UNSAFE.getInt(null, struct + (long)SETUP_TEMP_MEMORY_REQUIRED);
    }

    public static int ntemp_memory_required(long struct) {
        return UNSAFE.getInt(null, struct + (long)TEMP_MEMORY_REQUIRED);
    }

    public static int nmax_frame_size(long struct) {
        return UNSAFE.getInt(null, struct + (long)MAX_FRAME_SIZE);
    }

    static {
        Struct.Layout layout = STBVorbisInfo.__struct(STBVorbisInfo.__member(4), STBVorbisInfo.__member(4), STBVorbisInfo.__member(4), STBVorbisInfo.__member(4), STBVorbisInfo.__member(4), STBVorbisInfo.__member(4));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        SAMPLE_RATE = layout.offsetof(0);
        CHANNELS = layout.offsetof(1);
        SETUP_MEMORY_REQUIRED = layout.offsetof(2);
        SETUP_TEMP_MEMORY_REQUIRED = layout.offsetof(3);
        TEMP_MEMORY_REQUIRED = layout.offsetof(4);
        MAX_FRAME_SIZE = layout.offsetof(5);
    }

    public static class Buffer
    extends StructBuffer<STBVorbisInfo, Buffer>
    implements NativeResource {
        private static final STBVorbisInfo ELEMENT_FACTORY = STBVorbisInfo.create(-1L);

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
        protected STBVorbisInfo getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="unsigned int")
        public int sample_rate() {
            return STBVorbisInfo.nsample_rate(this.address());
        }

        public int channels() {
            return STBVorbisInfo.nchannels(this.address());
        }

        @NativeType(value="unsigned int")
        public int setup_memory_required() {
            return STBVorbisInfo.nsetup_memory_required(this.address());
        }

        @NativeType(value="unsigned int")
        public int setup_temp_memory_required() {
            return STBVorbisInfo.nsetup_temp_memory_required(this.address());
        }

        @NativeType(value="unsigned int")
        public int temp_memory_required() {
            return STBVorbisInfo.ntemp_memory_required(this.address());
        }

        public int max_frame_size() {
            return STBVorbisInfo.nmax_frame_size(this.address());
        }
    }
}

