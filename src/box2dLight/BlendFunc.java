/*
 * Decompiled with CFR 0.152.
 */
package box2dLight;

import com.badlogic.gdx.Gdx;

public class BlendFunc {
    final int default_sfactor;
    final int default_dfactor;
    int sfactor;
    int dfactor;

    public BlendFunc(int sfactor, int dfactor) {
        this.default_sfactor = sfactor;
        this.default_dfactor = dfactor;
        this.sfactor = sfactor;
        this.dfactor = dfactor;
    }

    public void set(int sfactor, int dfactor) {
        this.sfactor = sfactor;
        this.dfactor = dfactor;
    }

    public void reset() {
        this.sfactor = this.default_sfactor;
        this.dfactor = this.default_dfactor;
    }

    public void apply() {
        Gdx.gl20.glBlendFunc(this.sfactor, this.dfactor);
    }
}

