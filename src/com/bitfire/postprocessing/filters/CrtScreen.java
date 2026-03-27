/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.bitfire.postprocessing.filters.Filter;
import com.bitfire.utils.ShaderLoader;

public final class CrtScreen
extends Filter<CrtScreen> {
    private float elapsedSecs;
    private float offset;
    private float zoom;
    private float cdRedCyan;
    private float cdBlueYellow;
    private Vector2 chromaticDispersion;
    private final Vector3 vtint;
    private final Color tint;
    private float distortion;
    private boolean dodistortion;
    private RgbMode mode;

    private static boolean isSet(int flag, int flags) {
        return (flags & flag) == flag;
    }

    public CrtScreen(boolean barrelDistortion, RgbMode mode, int effectsSupport) {
        super(ShaderLoader.fromFile("screenspace", "crt-screen", (barrelDistortion ? "#define ENABLE_BARREL_DISTORTION\n" : "") + (mode == RgbMode.RgbShift ? "#define ENABLE_RGB_SHIFT\n" : "") + (mode == RgbMode.ChromaticAberrations ? "#define ENABLE_CHROMATIC_ABERRATIONS\n" : "") + (CrtScreen.isSet(Effect.TweakContrast.v, effectsSupport) ? "#define ENABLE_TWEAK_CONTRAST\n" : "") + (CrtScreen.isSet(Effect.Vignette.v, effectsSupport) ? "#define ENABLE_VIGNETTE\n" : "") + (CrtScreen.isSet(Effect.Tint.v, effectsSupport) ? "#define ENABLE_TINT\n" : "") + (CrtScreen.isSet(Effect.Scanlines.v, effectsSupport) ? "#define ENABLE_SCANLINES\n" : "") + (CrtScreen.isSet(Effect.PhosphorVibrance.v, effectsSupport) ? "#define ENABLE_PHOSPHOR_VIBRANCE\n" : "") + (CrtScreen.isSet(Effect.ScanDistortion.v, effectsSupport) ? "#define ENABLE_SCAN_DISTORTION\n" : "")));
        this.dodistortion = barrelDistortion;
        this.vtint = new Vector3();
        this.tint = new Color();
        this.chromaticDispersion = new Vector2();
        this.setTime(0.0f);
        this.setTint(1.0f, 1.0f, 0.85f);
        this.setDistortion(0.3f);
        this.setZoom(1.0f);
        this.setRgbMode(mode);
        switch (mode) {
            case ChromaticAberrations: {
                this.setChromaticDispersion(-0.1f, -0.1f);
                break;
            }
            case RgbShift: {
                this.setColorOffset(0.003f);
                break;
            }
            case None: {
                break;
            }
            default: {
                throw new GdxRuntimeException("Unsupported RGB mode");
            }
        }
    }

    public void setTime(float elapsedSecs) {
        this.elapsedSecs = elapsedSecs;
        this.setParam((Filter.Parameter)Param.Time, elapsedSecs % (float)Math.PI);
    }

    public void setRgbMode(RgbMode mode) {
        this.mode = mode;
    }

    public void setColorOffset(float offset) {
        this.offset = offset;
        if (this.mode == RgbMode.RgbShift) {
            this.setParam((Filter.Parameter)Param.ColorOffset, this.offset);
        }
    }

    public void setChromaticDispersion(Vector2 dispersion) {
        this.setChromaticDispersion(dispersion.x, dispersion.y);
    }

    public void setChromaticDispersion(float redCyan, float blueYellow) {
        this.cdRedCyan = redCyan;
        this.cdBlueYellow = blueYellow;
        this.chromaticDispersion.x = this.cdRedCyan;
        this.chromaticDispersion.y = this.cdBlueYellow;
        if (this.mode == RgbMode.ChromaticAberrations) {
            this.setParam((Filter.Parameter)Param.ChromaticDispersion, this.chromaticDispersion);
        }
    }

    public void setChromaticDispersionRC(float redCyan) {
        this.chromaticDispersion.x = this.cdRedCyan = redCyan;
        if (this.mode == RgbMode.ChromaticAberrations) {
            this.setParam((Filter.Parameter)Param.ChromaticDispersion, this.chromaticDispersion);
        }
    }

    public void setChromaticDispersionBY(float blueYellow) {
        this.chromaticDispersion.y = this.cdBlueYellow = blueYellow;
        if (this.mode == RgbMode.ChromaticAberrations) {
            this.setParam((Filter.Parameter)Param.ChromaticDispersion, this.chromaticDispersion);
        }
    }

    public void setTint(Color color) {
        this.tint.set(color);
        this.vtint.set(this.tint.r, this.tint.g, this.tint.b);
        this.setParam((Filter.Parameter)Param.Tint, this.vtint);
    }

    public void setTint(float r, float g, float b) {
        this.tint.set(r, g, b, 1.0f);
        this.vtint.set(this.tint.r, this.tint.g, this.tint.b);
        this.setParam((Filter.Parameter)Param.Tint, this.vtint);
    }

    public void setDistortion(float distortion) {
        this.distortion = distortion;
        if (this.dodistortion) {
            this.setParam((Filter.Parameter)Param.Distortion, this.distortion);
        }
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        if (this.dodistortion) {
            this.setParam((Filter.Parameter)Param.Zoom, this.zoom);
        }
    }

    public RgbMode getRgbMode() {
        return this.mode;
    }

    public float getOffset() {
        return this.offset;
    }

    public Vector2 getChromaticDispersion() {
        return this.chromaticDispersion;
    }

    public float getZoom() {
        return this.zoom;
    }

    public Color getTint() {
        return this.tint;
    }

    @Override
    protected void onBeforeRender() {
        this.inputTexture.bind(0);
    }

    @Override
    public void rebind() {
        this.setParams((Filter.Parameter)Param.Texture0, 0);
        this.setParams((Filter.Parameter)Param.Time, this.elapsedSecs);
        if (this.mode == RgbMode.RgbShift) {
            this.setParams((Filter.Parameter)Param.ColorOffset, this.offset);
        }
        if (this.mode == RgbMode.ChromaticAberrations) {
            this.setParams((Filter.Parameter)Param.ChromaticDispersion, this.chromaticDispersion);
        }
        this.setParams((Filter.Parameter)Param.Tint, this.vtint);
        if (this.dodistortion) {
            this.setParams((Filter.Parameter)Param.Distortion, this.distortion);
            this.setParams((Filter.Parameter)Param.Zoom, this.zoom);
        }
        this.endParams();
    }

    public static enum Param implements Filter.Parameter
    {
        Texture0("u_texture0", 0),
        Time("time", 0),
        Tint("tint", 3),
        ColorOffset("offset", 0),
        ChromaticDispersion("chromaticDispersion", 2),
        Distortion("Distortion", 0),
        Zoom("zoom", 0);

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

    public static enum Effect {
        None(0),
        TweakContrast(1),
        Vignette(2),
        Tint(4),
        Scanlines(8),
        PhosphorVibrance(16),
        ScanDistortion(32);

        public int v;

        private Effect(int value) {
            this.v = value;
        }
    }

    public static enum RgbMode {
        None(0),
        RgbShift(1),
        ChromaticAberrations(2);

        public int v;

        private RgbMode(int value) {
            this.v = value;
        }
    }
}

