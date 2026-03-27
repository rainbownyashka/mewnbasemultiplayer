/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.effects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.bitfire.postprocessing.PostProcessorEffect;
import com.bitfire.postprocessing.filters.CameraBlur;

public final class CameraMotion
extends PostProcessorEffect {
    private CameraBlur camblur;
    private Matrix4 ctp = new Matrix4();
    private float width;
    private float height;

    public CameraMotion(int width, int height) {
        this.width = width;
        this.height = height;
        this.camblur = new CameraBlur();
        this.camblur.setNormalDepthMap(null);
    }

    @Override
    public void dispose() {
        this.camblur.dispose();
    }

    public void setNormalDepthMap(Texture normalDepthMap) {
        this.camblur.setNormalDepthMap(normalDepthMap);
    }

    public void setMatrices(Matrix4 inv_view, Matrix4 prevViewProj, Matrix4 inv_proj) {
        this.ctp.set(prevViewProj).mul(inv_view);
        this.camblur.setCurrentToPrevious(this.ctp);
        this.camblur.setInverseProj(inv_proj);
    }

    public void setBlurPasses(int passes) {
        this.camblur.setBlurPasses(passes);
    }

    public void setBlurScale(float scale) {
        this.camblur.setBlurScale(scale);
    }

    public void setNearFar(float near, float far) {
        this.camblur.setNearFarPlanes(near, far);
    }

    public void setDepthScale(float scale) {
        this.camblur.setDepthScale(scale);
    }

    @Override
    public void rebind() {
        this.camblur.rebind();
    }

    @Override
    public void render(FrameBuffer src, FrameBuffer dest) {
        if (dest != null) {
            this.camblur.setViewport(dest.getWidth(), dest.getHeight());
        } else {
            this.camblur.setViewport(this.width, this.height);
        }
        this.restoreViewport(dest);
        ((CameraBlur)((CameraBlur)this.camblur.setInput(src)).setOutput(dest)).render();
    }
}

