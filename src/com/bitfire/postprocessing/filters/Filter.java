/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.filters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.bitfire.postprocessing.utils.FullscreenQuad;

public abstract class Filter<T> {
    protected static final FullscreenQuad quad = new FullscreenQuad();
    protected static final int u_texture0 = 0;
    protected static final int u_texture1 = 1;
    protected static final int u_texture2 = 2;
    protected static final int u_texture3 = 3;
    protected Texture inputTexture = null;
    protected FrameBuffer outputBuffer = null;
    protected ShaderProgram program = null;
    private boolean programBegan = false;

    public Filter(ShaderProgram program) {
        this.program = program;
    }

    public T setInput(Texture input) {
        this.inputTexture = input;
        return (T)this;
    }

    public T setInput(FrameBuffer input) {
        return this.setInput((Texture)input.getColorBufferTexture());
    }

    public T setOutput(FrameBuffer output) {
        this.outputBuffer = output;
        return (T)this;
    }

    public void dispose() {
        this.program.dispose();
    }

    public abstract void rebind();

    protected void setParam(Parameter param, int value) {
        this.program.begin();
        this.program.setUniformi(param.mnemonic(), value);
        this.program.end();
    }

    protected void setParam(Parameter param, float value) {
        this.program.begin();
        this.program.setUniformf(param.mnemonic(), value);
        this.program.end();
    }

    protected void setParam(Parameter param, Vector2 value) {
        this.program.begin();
        this.program.setUniformf(param.mnemonic(), value);
        this.program.end();
    }

    protected void setParam(Parameter param, Vector3 value) {
        this.program.begin();
        this.program.setUniformf(param.mnemonic(), value);
        this.program.end();
    }

    protected T setParam(Parameter param, Matrix3 value) {
        this.program.begin();
        this.program.setUniformMatrix(param.mnemonic(), value);
        this.program.end();
        return (T)this;
    }

    protected T setParam(Parameter param, Matrix4 value) {
        this.program.begin();
        this.program.setUniformMatrix(param.mnemonic(), value);
        this.program.end();
        return (T)this;
    }

    protected T setParamv(Parameter param, float[] values, int offset, int length) {
        this.program.begin();
        switch (param.arrayElementSize()) {
            case 4: {
                this.program.setUniform4fv(param.mnemonic(), values, offset, length);
                break;
            }
            case 3: {
                this.program.setUniform3fv(param.mnemonic(), values, offset, length);
                break;
            }
            case 2: {
                this.program.setUniform2fv(param.mnemonic(), values, offset, length);
                break;
            }
            default: {
                this.program.setUniform1fv(param.mnemonic(), values, offset, length);
            }
        }
        this.program.end();
        return (T)this;
    }

    protected T setParams(Parameter param, float value) {
        if (!this.programBegan) {
            this.programBegan = true;
            this.program.begin();
        }
        this.program.setUniformf(param.mnemonic(), value);
        return (T)this;
    }

    protected T setParams(Parameter param, int value) {
        if (!this.programBegan) {
            this.programBegan = true;
            this.program.begin();
        }
        this.program.setUniformi(param.mnemonic(), value);
        return (T)this;
    }

    protected T setParams(Parameter param, Vector2 value) {
        if (!this.programBegan) {
            this.programBegan = true;
            this.program.begin();
        }
        this.program.setUniformf(param.mnemonic(), value);
        return (T)this;
    }

    protected T setParams(Parameter param, Vector3 value) {
        if (!this.programBegan) {
            this.programBegan = true;
            this.program.begin();
        }
        this.program.setUniformf(param.mnemonic(), value);
        return (T)this;
    }

    protected T setParams(Parameter param, Matrix3 value) {
        if (!this.programBegan) {
            this.programBegan = true;
            this.program.begin();
        }
        this.program.setUniformMatrix(param.mnemonic(), value);
        return (T)this;
    }

    protected T setParams(Parameter param, Matrix4 value) {
        if (!this.programBegan) {
            this.programBegan = true;
            this.program.begin();
        }
        this.program.setUniformMatrix(param.mnemonic(), value);
        return (T)this;
    }

    protected T setParamsv(Parameter param, float[] values, int offset, int length) {
        if (!this.programBegan) {
            this.programBegan = true;
            this.program.begin();
        }
        switch (param.arrayElementSize()) {
            case 4: {
                this.program.setUniform4fv(param.mnemonic(), values, offset, length);
                break;
            }
            case 3: {
                this.program.setUniform3fv(param.mnemonic(), values, offset, length);
                break;
            }
            case 2: {
                this.program.setUniform2fv(param.mnemonic(), values, offset, length);
                break;
            }
            default: {
                this.program.setUniform1fv(param.mnemonic(), values, offset, length);
            }
        }
        return (T)this;
    }

    protected void endParams() {
        if (this.programBegan) {
            this.program.end();
            this.programBegan = false;
        }
    }

    protected abstract void onBeforeRender();

    public final void render() {
        if (this.outputBuffer != null) {
            this.outputBuffer.begin();
            this.realRender();
            this.outputBuffer.end();
        } else {
            this.realRender();
        }
    }

    private void realRender() {
        this.onBeforeRender();
        this.program.begin();
        quad.render(this.program);
        this.program.end();
    }

    public static interface Parameter {
        public String mnemonic();

        public int arrayElementSize();
    }
}

