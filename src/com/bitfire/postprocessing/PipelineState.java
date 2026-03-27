/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import java.nio.ByteBuffer;

public final class PipelineState
implements Disposable {
    private ByteBuffer byteBuffer = BufferUtils.newByteBuffer(32);

    protected PipelineState() {
    }

    public boolean isEnabled(int pname) {
        boolean ret = false;
        switch (pname) {
            case 3042: {
                Gdx.gl20.glGetBooleanv(3042, this.byteBuffer);
                ret = this.byteBuffer.get() == 1;
                this.byteBuffer.clear();
                break;
            }
            default: {
                ret = false;
            }
        }
        return ret;
    }

    @Override
    public void dispose() {
    }
}

