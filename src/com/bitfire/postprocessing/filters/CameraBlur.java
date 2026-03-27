/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class CameraBlur
extends Filter<CameraBlur> {
    private Texture normaldepth = null;
    private Vector2 viewport = new Vector2();

    public CameraBlur() {
        super(ShaderLoader.fromFile("screenspace", "camerablur"));
        this.rebind();
    }

    public void setNormalDepthMap(Texture texture) {
        this.normaldepth = texture;
    }

    public void setCurrentToPrevious(Matrix4 ctp) {
        this.setParams((Filter.Parameter)Param.CurrentToPrevious, ctp);
        this.endParams();
    }

    public void setInverseProj(Matrix4 invProj) {
        this.setParams((Filter.Parameter)Param.InvProj, invProj);
        this.endParams();
    }

    public void setBlurPasses(int passes) {
        this.setParams((Filter.Parameter)Param.BlurPasses, passes);
        this.endParams();
    }

    public void setBlurScale(float blurScale) {
        this.setParams((Filter.Parameter)Param.BlurScale, blurScale);
        this.endParams();
    }

    public void setNearFarPlanes(float near, float far) {
        this.setParams((Filter.Parameter)Param.Near, near);
        this.setParams((Filter.Parameter)Param.Far, far);
        this.endParams();
    }

    public void setViewport(float width, float height) {
        this.viewport.set(width, height);
        this.setParams((Filter.Parameter)Param.Viewport, this.viewport);
    }

    public void setDepthScale(float scale) {
        this.setParams((Filter.Parameter)Param.DepthScale, scale);
        this.endParams();
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.InputScene, 0);
        this.setParams((Filter.Parameter)Param.DepthMap, 1);
        this.endParams();
    }

    @Override
    protected void onBeforeRender() {
        this.rebind();
        this.inputTexture.bind(0);
        this.normaldepth.bind(1);
    }

    public static enum Param implements Filter.Parameter
    {
        InputScene("u_texture0", 0),
        DepthMap("u_texture1", 0),
        CurrentToPrevious("ctp", 0),
        Near("near", 0),
        Far("far", 0),
        BlurPasses("blur_passes", 0),
        BlurScale("blur_scale", 0),
        DepthScale("depth_scale", 0),
        InvProj("inv_proj", 0),
        Viewport("viewport", 0);

        private final String mnemonic;
        private int elementSize;

        private Param(String m, int elementSize) {
            this.mnemonic = m;
            this.elementSize = elementSize;
        }

        @Override
        public String mnemonic() {
            return this.mnemonic;
        }

        @Override
        public int arrayElementSize() {
            return this.elementSize;
        }
    }
}

