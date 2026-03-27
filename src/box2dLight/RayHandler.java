/*
 * Decompiled with CFR 0.152.
 */
package box2dLight;

import box2dLight.BlendFunc;
import box2dLight.Light;
import box2dLight.LightMap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import shaders.LightShader;

public class RayHandler
implements Disposable {
    static final float GAMMA_COR = 0.625f;
    static boolean gammaCorrection = false;
    static float gammaCorrectionParameter = 1.0f;
    public static boolean isDiffuse = false;
    public final BlendFunc diffuseBlendFunc = new BlendFunc(774, 0);
    public final BlendFunc shadowBlendFunc = new BlendFunc(1, 771);
    public final BlendFunc simpleBlendFunc = new BlendFunc(770, 1);
    final Matrix4 combined = new Matrix4();
    final Color ambientLight = new Color();
    final Array<Light> lightList = new Array(false, 16);
    final Array<Light> disabledLights = new Array(false, 16);
    LightMap lightMap;
    final ShaderProgram lightShader;
    ShaderProgram customLightShader = null;
    boolean culling = true;
    boolean shadows = true;
    boolean blur = true;
    int blurNum = 1;
    boolean customViewport = false;
    int viewportX = 0;
    int viewportY = 0;
    int viewportWidth = Gdx.graphics.getWidth();
    int viewportHeight = Gdx.graphics.getHeight();
    int lightRenderedLastFrame = 0;
    float x1;
    float x2;
    float y1;
    float y2;
    World world;

    public RayHandler(World world) {
        this(world, Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 4);
    }

    public RayHandler(World world, int fboWidth, int fboHeight) {
        this.world = world;
        this.resizeFBO(fboWidth, fboHeight);
        this.lightShader = LightShader.createLightShader();
    }

    public void resizeFBO(int fboWidth, int fboHeight) {
        if (this.lightMap != null) {
            this.lightMap.dispose();
        }
        this.lightMap = new LightMap(this, fboWidth, fboHeight);
    }

    public void setCombinedMatrix(OrthographicCamera camera) {
        this.setCombinedMatrix(camera.combined, camera.position.x, camera.position.y, camera.viewportWidth * camera.zoom, camera.viewportHeight * camera.zoom);
    }

    @Deprecated
    public void setCombinedMatrix(Matrix4 combined) {
        System.arraycopy(combined.val, 0, this.combined.val, 0, 16);
        float invWidth = combined.val[0];
        float halfViewPortWidth = 1.0f / invWidth;
        float x = -halfViewPortWidth * combined.val[12];
        this.x1 = x - halfViewPortWidth;
        this.x2 = x + halfViewPortWidth;
        float invHeight = combined.val[5];
        float halfViewPortHeight = 1.0f / invHeight;
        float y = -halfViewPortHeight * combined.val[13];
        this.y1 = y - halfViewPortHeight;
        this.y2 = y + halfViewPortHeight;
    }

    public void setCombinedMatrix(Matrix4 combined, float x, float y, float viewPortWidth, float viewPortHeight) {
        System.arraycopy(combined.val, 0, this.combined.val, 0, 16);
        float halfViewPortWidth = viewPortWidth * 0.5f;
        this.x1 = x - halfViewPortWidth;
        this.x2 = x + halfViewPortWidth;
        float halfViewPortHeight = viewPortHeight * 0.5f;
        this.y1 = y - halfViewPortHeight;
        this.y2 = y + halfViewPortHeight;
    }

    boolean intersect(float x, float y, float radius) {
        return this.x1 < x + radius && this.x2 > x - radius && this.y1 < y + radius && this.y2 > y - radius;
    }

    public void updateAndRender() {
        this.update();
        this.render();
    }

    public void update() {
        for (Light light : this.lightList) {
            light.update();
        }
    }

    public void prepareRender() {
        boolean useLightMap;
        this.lightRenderedLastFrame = 0;
        Gdx.gl.glDepthMask(false);
        Gdx.gl.glEnable(3042);
        this.simpleBlendFunc.apply();
        boolean bl = useLightMap = this.shadows || this.blur;
        if (useLightMap) {
            this.lightMap.frameBuffer.begin();
            Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            Gdx.gl.glClear(16384);
        }
        ShaderProgram shader = this.customLightShader != null ? this.customLightShader : this.lightShader;
        shader.begin();
        shader.setUniformMatrix("u_projTrans", this.combined);
        if (this.customLightShader != null) {
            this.updateLightShader();
        }
        for (Light light : this.lightList) {
            if (this.customLightShader != null) {
                this.updateLightShaderPerLight(light);
            }
            light.render();
        }
        shader.end();
        if (useLightMap) {
            boolean needed;
            if (this.customViewport) {
                this.lightMap.frameBuffer.end(this.viewportX, this.viewportY, this.viewportWidth, this.viewportHeight);
            } else {
                this.lightMap.frameBuffer.end();
            }
            boolean bl2 = needed = this.lightRenderedLastFrame > 0;
            if (needed && this.blur) {
                this.lightMap.gaussianBlur();
            }
        }
    }

    public void render() {
        this.prepareRender();
        this.lightMap.render();
    }

    public void renderOnly() {
        this.lightMap.render();
    }

    protected void updateLightShader() {
    }

    protected void updateLightShaderPerLight(Light light) {
    }

    public boolean pointAtLight(float x, float y) {
        for (Light light : this.lightList) {
            if (!light.contains(x, y)) continue;
            return true;
        }
        return false;
    }

    public boolean pointAtShadow(float x, float y) {
        for (Light light : this.lightList) {
            if (!light.contains(x, y)) continue;
            return false;
        }
        return true;
    }

    @Override
    public void dispose() {
        this.removeAll();
        if (this.lightMap != null) {
            this.lightMap.dispose();
        }
        if (this.lightShader != null) {
            this.lightShader.dispose();
        }
    }

    public void removeAll() {
        for (Light light : this.lightList) {
            light.dispose();
        }
        this.lightList.clear();
        for (Light light : this.disabledLights) {
            light.dispose();
        }
        this.disabledLights.clear();
    }

    public void setLightShader(ShaderProgram customLightShader) {
        this.customLightShader = customLightShader;
    }

    public void setCulling(boolean culling) {
        this.culling = culling;
    }

    public void setBlur(boolean blur) {
        this.blur = blur;
    }

    public void setBlurNum(int blurNum) {
        this.blurNum = blurNum;
    }

    public void setShadows(boolean shadows) {
        this.shadows = shadows;
    }

    public void setAmbientLight(float ambientLight) {
        this.ambientLight.a = MathUtils.clamp(ambientLight, 0.0f, 1.0f);
    }

    public void setAmbientLight(float r, float g, float b, float a) {
        this.ambientLight.set(r, g, b, a);
    }

    public void setAmbientLight(Color ambientLightColor) {
        this.ambientLight.set(ambientLightColor);
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public static boolean getGammaCorrection() {
        return gammaCorrection;
    }

    public static void setGammaCorrection(boolean gammaCorrectionWanted) {
        gammaCorrection = gammaCorrectionWanted;
        gammaCorrectionParameter = gammaCorrection ? 0.625f : 1.0f;
    }

    public static void useDiffuseLight(boolean useDiffuse) {
        isDiffuse = useDiffuse;
    }

    public void useCustomViewport(int x, int y, int width, int height) {
        this.customViewport = true;
        this.viewportX = x;
        this.viewportY = y;
        this.viewportWidth = width;
        this.viewportHeight = height;
    }

    public void useDefaultViewport() {
        this.customViewport = false;
    }

    public void setLightMapRendering(boolean isAutomatic) {
        this.lightMap.lightMapDrawingDisabled = !isAutomatic;
    }

    public Texture getLightMapTexture() {
        return (Texture)this.lightMap.frameBuffer.getColorBufferTexture();
    }

    public FrameBuffer getLightMapBuffer() {
        return this.lightMap.frameBuffer;
    }
}

