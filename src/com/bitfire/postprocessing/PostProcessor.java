/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.bitfire.postprocessing.PipelineState;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.PostProcessorListener;
import com.bitfire.postprocessing.utils.PingPongBuffer;
import com.bitfire.utils.ItemsManager;

public final class PostProcessor
implements Disposable {
    public static boolean EnableQueryStates = false;
    private static PipelineState pipelineState = null;
    private static Pixmap.Format fbFormat;
    private final PingPongBuffer composite;
    private Texture.TextureWrap compositeWrapU;
    private Texture.TextureWrap compositeWrapV;
    private final ItemsManager<PostProcessorEffect> effectsManager = new ItemsManager();
    private static final Array<PingPongBuffer> buffers;
    private final Color clearColor = Color.CLEAR;
    private int clearBits = 16384;
    private float clearDepth = 1.0f;
    private static Rectangle viewport;
    private static boolean hasViewport;
    private boolean enabled = true;
    private boolean capturing = false;
    private boolean hasCaptured = false;
    private boolean useDepth = false;
    private PostProcessorListener listener = null;
    private Array<PostProcessorEffect> enabledEffects = new Array(5);

    public PostProcessor(boolean useDepth, boolean useAlphaChannel, boolean use32Bits) {
        this(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), useDepth, useAlphaChannel, use32Bits);
    }

    public PostProcessor(int fboWidth, int fboHeight, boolean useDepth, boolean useAlphaChannel, boolean use32Bits) {
        this(fboWidth, fboHeight, useDepth, useAlphaChannel, use32Bits, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
    }

    public PostProcessor(Rectangle viewport, boolean useDepth, boolean useAlphaChannel, boolean use32Bits) {
        this((int)viewport.width, (int)viewport.height, useDepth, useAlphaChannel, use32Bits, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        this.setViewport(viewport);
    }

    public PostProcessor(Rectangle viewport, boolean useDepth, boolean useAlphaChannel, boolean use32Bits, Texture.TextureWrap u, Texture.TextureWrap v) {
        this((int)viewport.width, (int)viewport.height, useDepth, useAlphaChannel, use32Bits, u, v);
        this.setViewport(viewport);
    }

    public PostProcessor(int fboWidth, int fboHeight, boolean useDepth, boolean useAlphaChannel, boolean use32Bits, Texture.TextureWrap u, Texture.TextureWrap v) {
        fbFormat = use32Bits ? (useAlphaChannel ? Pixmap.Format.RGBA8888 : Pixmap.Format.RGB888) : (useAlphaChannel ? Pixmap.Format.RGBA4444 : Pixmap.Format.RGB565);
        this.composite = PostProcessor.newPingPongBuffer(fboWidth, fboHeight, fbFormat, useDepth);
        this.setBufferTextureWrap(u, v);
        pipelineState = new PipelineState();
        this.capturing = false;
        this.hasCaptured = false;
        this.enabled = true;
        this.useDepth = useDepth;
        if (useDepth) {
            this.clearBits |= 0x100;
        }
        this.setViewport(null);
    }

    public static PingPongBuffer newPingPongBuffer(int width, int height, Pixmap.Format frameBufferFormat, boolean hasDepth) {
        PingPongBuffer buffer = new PingPongBuffer(width, height, frameBufferFormat, hasDepth);
        buffers.add(buffer);
        return buffer;
    }

    public static boolean isStateEnabled(int pname) {
        if (EnableQueryStates) {
            return pipelineState.isEnabled(pname);
        }
        return false;
    }

    public void setViewport(Rectangle viewport) {
        boolean bl = hasViewport = viewport != null;
        if (hasViewport) {
            PostProcessor.viewport.set(viewport);
        }
    }

    @Override
    public void dispose() {
        this.effectsManager.dispose();
        for (int i = 0; i < PostProcessor.buffers.size; ++i) {
            buffers.get(i).dispose();
        }
        buffers.clear();
        if (this.enabledEffects != null) {
            this.enabledEffects.clear();
        }
        pipelineState.dispose();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isReady() {
        boolean hasEffects = false;
        for (PostProcessorEffect e : this.effectsManager) {
            if (!e.isEnabled()) continue;
            hasEffects = true;
            break;
        }
        return this.enabled && !this.capturing && hasEffects;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getEnabledEffectsCount() {
        return this.enabledEffects.size;
    }

    public void setListener(PostProcessorListener listener) {
        this.listener = listener;
    }

    public void addEffect(PostProcessorEffect effect) {
        this.effectsManager.add(effect);
    }

    public void removeEffect(PostProcessorEffect effect) {
        this.effectsManager.remove(effect);
    }

    public static Pixmap.Format getFramebufferFormat() {
        return fbFormat;
    }

    public void setClearColor(Color color) {
        this.clearColor.set(color);
    }

    public void setClearColor(float r, float g, float b, float a) {
        this.clearColor.set(r, g, b, a);
    }

    public void setClearBits(int bits) {
        this.clearBits = bits;
    }

    public void setClearDepth(float depth) {
        this.clearDepth = depth;
    }

    public void setBufferTextureWrap(Texture.TextureWrap u, Texture.TextureWrap v) {
        this.compositeWrapU = u;
        this.compositeWrapV = v;
        this.composite.texture1.setWrap(this.compositeWrapU, this.compositeWrapV);
        this.composite.texture2.setWrap(this.compositeWrapU, this.compositeWrapV);
    }

    public boolean capture() {
        this.hasCaptured = false;
        if (this.enabled && !this.capturing) {
            if (this.buildEnabledEffectsList() == 0) {
                return false;
            }
            this.capturing = true;
            this.composite.begin();
            this.composite.capture();
            if (this.useDepth) {
                Gdx.gl.glClearDepthf(this.clearDepth);
            }
            Gdx.gl.glClearColor(this.clearColor.r, this.clearColor.g, this.clearColor.b, this.clearColor.a);
            Gdx.gl.glClear(this.clearBits);
            return true;
        }
        return false;
    }

    public boolean captureNoClear() {
        this.hasCaptured = false;
        if (this.enabled && !this.capturing) {
            if (this.buildEnabledEffectsList() == 0) {
                return false;
            }
            this.capturing = true;
            this.composite.begin();
            this.composite.capture();
            return true;
        }
        return false;
    }

    public FrameBuffer captureEnd() {
        if (this.enabled && this.capturing) {
            this.capturing = false;
            this.hasCaptured = true;
            this.composite.end();
            return this.composite.getResultBuffer();
        }
        return null;
    }

    public PingPongBuffer getCombinedBuffer() {
        return this.composite;
    }

    public FrameBuffer captured() {
        if (this.enabled && this.hasCaptured) {
            return this.composite.getResultBuffer();
        }
        return null;
    }

    public void rebind() {
        this.composite.texture1.setWrap(this.compositeWrapU, this.compositeWrapV);
        this.composite.texture2.setWrap(this.compositeWrapU, this.compositeWrapV);
        for (int i = 0; i < PostProcessor.buffers.size; ++i) {
            buffers.get(i).rebind();
        }
        for (PostProcessorEffect e : this.effectsManager) {
            e.rebind();
        }
    }

    public void render(FrameBuffer dest) {
        this.captureEnd();
        if (!this.hasCaptured) {
            return;
        }
        Array<PostProcessorEffect> items = this.enabledEffects;
        int count = items.size;
        if (count > 0) {
            Gdx.gl.glDisable(2884);
            Gdx.gl.glDisable(2929);
            if (count > 1) {
                for (int i = 0; i < count - 1; ++i) {
                    PostProcessorEffect e = items.get(i);
                    this.composite.capture();
                    e.render(this.composite.getSourceBuffer(), this.composite.getResultBuffer());
                }
                this.composite.end();
            }
            if (this.listener != null && dest == null) {
                this.listener.beforeRenderToScreen();
            }
            items.get(count - 1).render(this.composite.getResultBuffer(), dest);
            Gdx.gl.glActiveTexture(33984);
        } else {
            Gdx.app.log("PostProcessor", "No post-processor effects enabled, aborting render");
        }
    }

    public void render() {
        this.render(null);
    }

    private int buildEnabledEffectsList() {
        this.enabledEffects.clear();
        for (PostProcessorEffect e : this.effectsManager) {
            if (!e.isEnabled()) continue;
            this.enabledEffects.add(e);
        }
        return this.enabledEffects.size;
    }

    protected static void restoreViewport(FrameBuffer dest) {
        if (hasViewport && dest == null) {
            Gdx.gl.glViewport((int)PostProcessor.viewport.x, (int)PostProcessor.viewport.y, (int)PostProcessor.viewport.width, (int)PostProcessor.viewport.height);
        }
    }

    static {
        buffers = new Array(5);
        viewport = new Rectangle();
        hasViewport = false;
    }
}

