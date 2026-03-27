/*
 * Decompiled with CFR 0.152.
 */
package com.bitfire.postprocessing.utils;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class FullscreenQuad {
    private Mesh quad = this.createFullscreenQuad();
    private static final int VERT_SIZE = 16;
    private static float[] verts = new float[16];
    private static final int X1 = 0;
    private static final int Y1 = 1;
    private static final int U1 = 2;
    private static final int V1 = 3;
    private static final int X2 = 4;
    private static final int Y2 = 5;
    private static final int U2 = 6;
    private static final int V2 = 7;
    private static final int X3 = 8;
    private static final int Y3 = 9;
    private static final int U3 = 10;
    private static final int V3 = 11;
    private static final int X4 = 12;
    private static final int Y4 = 13;
    private static final int U4 = 14;
    private static final int V4 = 15;

    public void dispose() {
        this.quad.dispose();
    }

    public void render(ShaderProgram program) {
        this.quad.render(program, 6, 0, 4);
    }

    private Mesh createFullscreenQuad() {
        FullscreenQuad.verts[0] = -1.0f;
        FullscreenQuad.verts[1] = -1.0f;
        FullscreenQuad.verts[4] = 1.0f;
        FullscreenQuad.verts[5] = -1.0f;
        FullscreenQuad.verts[8] = 1.0f;
        FullscreenQuad.verts[9] = 1.0f;
        FullscreenQuad.verts[12] = -1.0f;
        FullscreenQuad.verts[13] = 1.0f;
        FullscreenQuad.verts[2] = 0.0f;
        FullscreenQuad.verts[3] = 0.0f;
        FullscreenQuad.verts[6] = 1.0f;
        FullscreenQuad.verts[7] = 0.0f;
        FullscreenQuad.verts[10] = 1.0f;
        FullscreenQuad.verts[11] = 1.0f;
        FullscreenQuad.verts[14] = 0.0f;
        FullscreenQuad.verts[15] = 1.0f;
        Mesh tmpMesh = new Mesh(Mesh.VertexDataType.VertexArray, true, 4, 0, new VertexAttribute(1, 2, "a_position"), new VertexAttribute(16, 2, "a_texCoord0"));
        tmpMesh.setVertices(verts);
        return tmpMesh;
    }
}

