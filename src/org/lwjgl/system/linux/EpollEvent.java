/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package org.lwjgl.system.linux;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.system.linux.EpollData;

@NativeType(value="struct epoll_event")
public class EpollEvent
extends Struct
implements NativeResource {
    public static final int SIZEOF;
    public static final int ALIGNOF;
    public static final int EVENTS;
    public static final int DATA;

    public EpollEvent(ByteBuffer container) {
        super(MemoryUtil.memAddress(container), EpollEvent.__checkContainer(container, SIZEOF));
    }

    @Override
    public int sizeof() {
        return SIZEOF;
    }

    @NativeType(value="uint32_t")
    public int events() {
        return EpollEvent.nevents(this.address());
    }

    @NativeType(value="epoll_data_t")
    public EpollData data() {
        return EpollEvent.ndata(this.address());
    }

    public EpollEvent events(@NativeType(value="uint32_t") int value) {
        EpollEvent.nevents(this.address(), value);
        return this;
    }

    public EpollEvent data(@NativeType(value="epoll_data_t") EpollData value) {
        EpollEvent.ndata(this.address(), value);
        return this;
    }

    public EpollEvent data(Consumer<EpollData> consumer) {
        consumer.accept(this.data());
        return this;
    }

    public EpollEvent set(int events, EpollData data) {
        this.events(events);
        this.data(data);
        return this;
    }

    public EpollEvent set(EpollEvent src) {
        MemoryUtil.memCopy(src.address(), this.address(), SIZEOF);
        return this;
    }

    public static EpollEvent malloc() {
        return EpollEvent.wrap(EpollEvent.class, MemoryUtil.nmemAllocChecked(SIZEOF));
    }

    public static EpollEvent calloc() {
        return EpollEvent.wrap(EpollEvent.class, MemoryUtil.nmemCallocChecked(1L, SIZEOF));
    }

    public static EpollEvent create() {
        ByteBuffer container = BufferUtils.createByteBuffer(SIZEOF);
        return EpollEvent.wrap(EpollEvent.class, MemoryUtil.memAddress(container), container);
    }

    public static EpollEvent create(long address) {
        return EpollEvent.wrap(EpollEvent.class, address);
    }

    @Nullable
    public static EpollEvent createSafe(long address) {
        return address == 0L ? null : EpollEvent.wrap(EpollEvent.class, address);
    }

    public static Buffer malloc(int capacity) {
        return EpollEvent.wrap(Buffer.class, MemoryUtil.nmemAllocChecked(EpollEvent.__checkMalloc(capacity, SIZEOF)), capacity);
    }

    public static Buffer calloc(int capacity) {
        return EpollEvent.wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, SIZEOF), capacity);
    }

    public static Buffer create(int capacity) {
        ByteBuffer container = EpollEvent.__create(capacity, SIZEOF);
        return EpollEvent.wrap(Buffer.class, MemoryUtil.memAddress(container), capacity, container);
    }

    public static Buffer create(long address, int capacity) {
        return EpollEvent.wrap(Buffer.class, address, capacity);
    }

    @Nullable
    public static Buffer createSafe(long address, int capacity) {
        return address == 0L ? null : EpollEvent.wrap(Buffer.class, address, capacity);
    }

    public static EpollEvent malloc(MemoryStack stack) {
        return EpollEvent.wrap(EpollEvent.class, stack.nmalloc(ALIGNOF, SIZEOF));
    }

    public static EpollEvent calloc(MemoryStack stack) {
        return EpollEvent.wrap(EpollEvent.class, stack.ncalloc(ALIGNOF, 1, SIZEOF));
    }

    public static Buffer malloc(int capacity, MemoryStack stack) {
        return EpollEvent.wrap(Buffer.class, stack.nmalloc(ALIGNOF, capacity * SIZEOF), capacity);
    }

    public static Buffer calloc(int capacity, MemoryStack stack) {
        return EpollEvent.wrap(Buffer.class, stack.ncalloc(ALIGNOF, capacity, SIZEOF), capacity);
    }

    public static int nevents(long struct) {
        return UNSAFE.getInt(null, struct + (long)EVENTS);
    }

    public static EpollData ndata(long struct) {
        return EpollData.create(struct + (long)DATA);
    }

    public static void nevents(long struct, int value) {
        UNSAFE.putInt(null, struct + (long)EVENTS, value);
    }

    public static void ndata(long struct, EpollData value) {
        MemoryUtil.memCopy(value.address(), struct + (long)DATA, EpollData.SIZEOF);
    }

    static {
        Struct.Layout layout = EpollEvent.__struct(EpollEvent.__member(4), EpollEvent.__member(EpollData.SIZEOF, EpollData.ALIGNOF));
        SIZEOF = layout.getSize();
        ALIGNOF = layout.getAlignment();
        EVENTS = layout.offsetof(0);
        DATA = layout.offsetof(1);
    }

    public static class Buffer
    extends StructBuffer<EpollEvent, Buffer>
    implements NativeResource {
        private static final EpollEvent ELEMENT_FACTORY = EpollEvent.create(-1L);

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
        protected EpollEvent getElementFactory() {
            return ELEMENT_FACTORY;
        }

        @NativeType(value="uint32_t")
        public int events() {
            return EpollEvent.nevents(this.address());
        }

        @NativeType(value="epoll_data_t")
        public EpollData data() {
            return EpollEvent.ndata(this.address());
        }

        public Buffer events(@NativeType(value="uint32_t") int value) {
            EpollEvent.nevents(this.address(), value);
            return this;
        }

        public Buffer data(@NativeType(value="epoll_data_t") EpollData value) {
            EpollEvent.ndata(this.address(), value);
            return this;
        }

        public Buffer data(Consumer<EpollData> consumer) {
            consumer.accept(this.data());
            return this;
        }
    }
}

