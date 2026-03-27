/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;
import com.cairn4.moonbase.Light;
import com.cairn4.moonbase.ui.GameScreen;
import java.util.ArrayList;
import java.util.List;

public class Lights
implements Disposable {
    private SpriteBatch lightBatch;
    public Texture lightTexture;
    private OrthographicCamera lightBufferCamera;
    private FrameBuffer lightBuffer;
    private Batch lightBufferBatch;
    private List<Light> lights = new ArrayList<Light>(10);
    private Color defaultAmbientColor = new Color(0.3f, 0.3f, 0.3f, 1.0f);
    private Color ambientColor = new Color(0.3f, 0.3f, 0.3f, 1.0f);
    private Color targetAmbientColor = new Color(0.3f, 0.3f, 0.3f, 1.0f);
    private float globalLightStrength = 1.0f;
    private float targetGlobalLightStrengh = 1.0f;
    public GameScreen gameScreen;
    private Color tempColor = new Color();

    public Lights(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.lightBufferCamera = new OrthographicCamera(1280.0f, 720.0f);
        this.lightBufferBatch = new SpriteBatch();
        this.lightBatch = new SpriteBatch();
        this.lightTexture = new Texture("light2.png");
        this.lightBatch.setBlendFunction(770, 1);
        this.lightBatch.enableBlending();
    }

    public void resetColor() {
        this.targetAmbientColor.set(this.defaultAmbientColor);
        this.targetGlobalLightStrengh = 1.0f;
    }

    public void fadeTo(Color color) {
        this.targetAmbientColor.set(color);
    }

    public void fadeTo(float r, float g, float b, float a, float globalLightStrength) {
        this.targetAmbientColor.set(r, g, b, a);
        this.targetGlobalLightStrengh = globalLightStrength;
    }

    @Override
    public void dispose() {
        this.lightBatch.dispose();
        this.lightTexture.dispose();
        this.lightBufferBatch.dispose();
        this.lightBuffer.dispose();
    }

    public void resize(int width, int height) {
        this.lightBufferCamera.viewportHeight = height;
        this.lightBufferCamera.viewportWidth = width;
        this.lightBufferCamera.position.set(width / 2, height / 2, 0.0f);
        this.lightBufferCamera.update();
        if (this.lightBuffer != null) {
            this.lightBuffer.dispose();
        }
        this.lightBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        ((Texture)this.lightBuffer.getColorBufferTexture()).setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private float moveTowards(float from, float to, float increment) {
        if (Math.abs(from - to) < increment * 2.0f) {
            return to;
        }
        if (from < to) {
            return from + increment;
        }
        if (from > to) {
            return from - increment;
        }
        return from;
    }

    public void render(Camera camera) {
        if (this.globalLightStrength != this.targetGlobalLightStrengh) {
            this.globalLightStrength = this.moveTowards(this.globalLightStrength, this.targetGlobalLightStrengh, 0.01f);
        }
        if (!this.targetAmbientColor.equals(this.ambientColor)) {
            this.ambientColor.r = this.moveTowards(this.ambientColor.r, this.targetAmbientColor.r, 0.004f);
            this.ambientColor.g = this.moveTowards(this.ambientColor.g, this.targetAmbientColor.g, 0.004f);
            this.ambientColor.b = this.moveTowards(this.ambientColor.b, this.targetAmbientColor.b, 0.004f);
            this.ambientColor.a = this.moveTowards(this.ambientColor.a, this.targetAmbientColor.a, 0.004f);
        }
        this.lightBuffer.begin();
        Gdx.gl.glClearColor(this.ambientColor.r, this.ambientColor.g, this.ambientColor.b, this.ambientColor.a);
        Gdx.gl.glClear(16384);
        this.lightBatch.setProjectionMatrix(camera.combined);
        this.lightBatch.begin();
        for (int i = this.lights.size() - 1; i >= 0; --i) {
            Light light = this.lights.get(i);
            if (light.isOn()) {
                light.update();
                this.tempColor.set(light.getColor());
                if (this.globalLightStrength < this.tempColor.a) {
                    this.tempColor.a = this.globalLightStrength;
                }
                this.lightBatch.setColor(this.tempColor);
                light.draw(this.lightBatch);
                continue;
            }
            this.lights.remove(i);
        }
        this.lightBatch.end();
        this.lightBuffer.end();
        this.lightBufferBatch.setProjectionMatrix(this.lightBufferCamera.combined);
        this.lightBufferBatch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        this.lightBufferBatch.setBlendFunction(774, 0);
        this.lightBufferBatch.begin();
        this.lightBufferBatch.draw((Texture)this.lightBuffer.getColorBufferTexture(), 0.0f, 0.0f, (float)this.lightBuffer.getWidth(), (float)this.lightBuffer.getHeight(), 0, 0, this.lightBuffer.getWidth(), this.lightBuffer.getHeight(), false, true);
        this.lightBufferBatch.end();
    }

    public void addLight(Light light) {
        this.lights.add(light);
    }

    public void removeLight(Light light) {
        this.lights.remove(light);
    }

    public void setDefaultAmbientColor(Color color) {
        this.defaultAmbientColor.set(color);
        this.targetAmbientColor.set(color);
        this.ambientColor.set(color);
    }

    public void fadeIn() {
        this.ambientColor.set(0.0f, 0.0f, 0.0f, 0.0f);
        this.globalLightStrength = 0.0f;
    }

    public void fadeOut() {
        this.targetAmbientColor.set(0.0f, 0.0f, 0.0f, 1.0f);
        this.targetGlobalLightStrengh = 0.0f;
    }
}

