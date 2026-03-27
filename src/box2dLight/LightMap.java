/*
 * Decompiled with CFR 0.152.
 */
package box2dLight;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import shaders.DiffuseShader;
import shaders.Gaussian;
import shaders.ShadowShader;
import shaders.WithoutShadowShader;

class LightMap {
    private ShaderProgram shadowShader;
    FrameBuffer frameBuffer;
    private Mesh lightMapMesh;
    private FrameBuffer pingPongBuffer;
    private RayHandler rayHandler;
    private ShaderProgram withoutShadowShader;
    private ShaderProgram blurShader;
    private ShaderProgram diffuseShader;
    boolean lightMapDrawingDisabled;
    public static final int VERT_SIZE = 16;
    public static final int X1 = 0;
    public static final int Y1 = 1;
    public static final int U1 = 2;
    public static final int V1 = 3;
    public static final int X2 = 4;
    public static final int Y2 = 5;
    public static final int U2 = 6;
    public static final int V2 = 7;
    public static final int X3 = 8;
    public static final int Y3 = 9;
    public static final int U3 = 10;
    public static final int V3 = 11;
    public static final int X4 = 12;
    public static final int Y4 = 13;
    public static final int U4 = 14;
    public static final int V4 = 15;

    public void render() {
        boolean needed;
        boolean bl = needed = this.rayHandler.lightRenderedLastFrame > 0;
        if (this.lightMapDrawingDisabled) {
            return;
        }
        ((Texture)this.frameBuffer.getColorBufferTexture()).bind(0);
        if (this.rayHandler.shadows) {
            Color c = this.rayHandler.ambientLight;
            ShaderProgram shader = this.shadowShader;
            if (RayHandler.isDiffuse) {
                shader = this.diffuseShader;
                shader.begin();
                this.rayHandler.diffuseBlendFunc.apply();
                shader.setUniformf("ambient", c.r, c.g, c.b, c.a);
            } else {
                shader.begin();
                this.rayHandler.shadowBlendFunc.apply();
                shader.setUniformf("ambient", c.r * c.a, c.g * c.a, c.b * c.a, 1.0f - c.a);
            }
            this.lightMapMesh.render(shader, 6);
            shader.end();
        } else if (needed) {
            this.rayHandler.simpleBlendFunc.apply();
            this.withoutShadowShader.begin();
            this.lightMapMesh.render(this.withoutShadowShader, 6);
            this.withoutShadowShader.end();
        }
        Gdx.gl20.glDisable(3042);
    }

    public void gaussianBlur() {
        Gdx.gl20.glDisable(3042);
        for (int i = 0; i < this.rayHandler.blurNum; ++i) {
            ((Texture)this.frameBuffer.getColorBufferTexture()).bind(0);
            this.pingPongBuffer.begin();
            this.blurShader.begin();
            this.blurShader.setUniformf("dir", 1.0f, 0.0f);
            this.lightMapMesh.render(this.blurShader, 6, 0, 4);
            this.blurShader.end();
            this.pingPongBuffer.end();
            ((Texture)this.pingPongBuffer.getColorBufferTexture()).bind(0);
            this.frameBuffer.begin();
            this.blurShader.begin();
            this.blurShader.setUniformf("dir", 0.0f, 1.0f);
            this.lightMapMesh.render(this.blurShader, 6, 0, 4);
            this.blurShader.end();
            if (this.rayHandler.customViewport) {
                this.frameBuffer.end(this.rayHandler.viewportX, this.rayHandler.viewportY, this.rayHandler.viewportWidth, this.rayHandler.viewportHeight);
                continue;
            }
            this.frameBuffer.end();
        }
        Gdx.gl20.glEnable(3042);
    }

    public LightMap(RayHandler rayHandler, int fboWidth, int fboHeight) {
        this.rayHandler = rayHandler;
        if (fboWidth <= 0) {
            fboWidth = 1;
        }
        if (fboHeight <= 0) {
            fboHeight = 1;
        }
        this.frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, fboWidth, fboHeight, false);
        this.pingPongBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, fboWidth, fboHeight, false);
        this.lightMapMesh = this.createLightMapMesh();
        this.shadowShader = ShadowShader.createShadowShader();
        this.diffuseShader = DiffuseShader.createShadowShader();
        this.withoutShadowShader = WithoutShadowShader.createShadowShader();
        this.blurShader = Gaussian.createBlurShader(fboWidth, fboHeight);
    }

    void dispose() {
        this.shadowShader.dispose();
        this.blurShader.dispose();
        this.lightMapMesh.dispose();
        this.frameBuffer.dispose();
        this.pingPongBuffer.dispose();
    }

    private Mesh createLightMapMesh() {
        float[] verts = new float[16];
        verts[0] = -1.0f;
        verts[1] = -1.0f;
        verts[4] = 1.0f;
        verts[5] = -1.0f;
        verts[8] = 1.0f;
        verts[9] = 1.0f;
        verts[12] = -1.0f;
        verts[13] = 1.0f;
        verts[2] = 0.0f;
        verts[3] = 0.0f;
        verts[6] = 1.0f;
        verts[7] = 0.0f;
        verts[10] = 1.0f;
        verts[11] = 1.0f;
        verts[14] = 0.0f;
        verts[15] = 1.0f;
        Mesh tmpMesh = new Mesh(true, 4, 0, new VertexAttribute(1, 2, "a_position"), new VertexAttribute(16, 2, "a_texCoord"));
        tmpMesh.setVertices(verts);
        return tmpMesh;
    }
}

