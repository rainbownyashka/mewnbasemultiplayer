/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.utils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public final class PingPongBuffer {
    public FrameBuffer buffer1;
    public FrameBuffer buffer2;
    public Texture texture1;
    public Texture texture2;
    public int width;
    public int height;
    public final boolean ownResources;
    private Texture texResult;
    private Texture texSrc;
    private FrameBuffer bufResult;
    private FrameBuffer bufSrc;
    private boolean writeState;
    private boolean pending1;
    private boolean pending2;
    private final FrameBuffer owned1;
    private final FrameBuffer owned2;
    private FrameBuffer ownedResult;
    private FrameBuffer ownedSource;
    private int ownedW;
    private int ownedH;

    public PingPongBuffer(int width, int height, Pixmap.Format frameBufferFormat, boolean hasDepth) {
        this.ownResources = true;
        this.owned1 = new FrameBuffer(frameBufferFormat, width, height, hasDepth);
        this.owned2 = new FrameBuffer(frameBufferFormat, width, height, hasDepth);
        this.set(this.owned1, this.owned2);
    }

    public PingPongBuffer(FrameBuffer buffer1, FrameBuffer buffer2) {
        this.ownResources = false;
        this.owned1 = null;
        this.owned2 = null;
        this.set(buffer1, buffer2);
    }

    public void set(FrameBuffer buffer1, FrameBuffer buffer2) {
        if (this.ownResources) {
            this.ownedResult = this.bufResult;
            this.ownedSource = this.bufSrc;
            this.ownedW = this.width;
            this.ownedH = this.height;
        }
        this.buffer1 = buffer1;
        this.buffer2 = buffer2;
        this.width = this.buffer1.getWidth();
        this.height = this.buffer1.getHeight();
        this.rebind();
    }

    public void reset() {
        if (this.ownResources) {
            this.buffer1 = this.owned1;
            this.buffer2 = this.owned2;
            this.width = this.ownedW;
            this.height = this.ownedH;
            this.bufResult = this.ownedResult;
            this.bufSrc = this.ownedSource;
        }
    }

    public void dispose() {
        if (this.ownResources) {
            this.owned1.dispose();
            this.owned2.dispose();
        }
    }

    public void rebind() {
        this.texture1 = (Texture)this.buffer1.getColorBufferTexture();
        this.texture2 = (Texture)this.buffer2.getColorBufferTexture();
    }

    public void begin() {
        this.pending1 = false;
        this.pending2 = false;
        this.writeState = true;
        this.texSrc = this.texture1;
        this.bufSrc = this.buffer1;
        this.texResult = this.texture2;
        this.bufResult = this.buffer2;
    }

    public Texture capture() {
        this.endPending();
        if (this.writeState) {
            this.texSrc = this.texture1;
            this.bufSrc = this.buffer1;
            this.texResult = this.texture2;
            this.bufResult = this.buffer2;
            this.pending2 = true;
            this.buffer2.begin();
        } else {
            this.texSrc = this.texture2;
            this.bufSrc = this.buffer2;
            this.texResult = this.texture1;
            this.bufResult = this.buffer1;
            this.pending1 = true;
            this.buffer1.begin();
        }
        this.writeState = !this.writeState;
        return this.texSrc;
    }

    public void end() {
        this.endPending();
    }

    public Texture getSouceTexture() {
        return this.texSrc;
    }

    public FrameBuffer getSourceBuffer() {
        return this.bufSrc;
    }

    public Texture getResultTexture() {
        return this.texResult;
    }

    public FrameBuffer getResultBuffer() {
        return this.bufResult;
    }

    private void endPending() {
        if (this.pending1) {
            this.buffer1.end();
            this.pending1 = false;
        }
        if (this.pending2) {
            this.buffer2.end();
            this.pending2 = false;
        }
    }
}

