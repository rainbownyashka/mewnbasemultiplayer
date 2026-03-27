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
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Light;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.GameScreen;
import java.util.ArrayList;
import java.util.List;

public class Fog
implements Disposable {
    public World world;
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
    public static int FOG_TEXTURE_SIZE = 256;

    public Fog(GameScreen gameScreen, World world) {
        this.gameScreen = gameScreen;
        this.world = world;
        this.lightBufferCamera = new OrthographicCamera(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        this.lightBufferBatch = new SpriteBatch();
        this.lightBatch = new SpriteBatch();
        this.lightTexture = new Texture("fog.png");
        this.lightTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.lightBatch.setBlendFunction(770, 1);
        this.lightBatch.enableBlending();
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
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(16384);
        this.lightBatch.setProjectionMatrix(camera.combined);
        this.lightBatch.begin();
        for (Chunk chunk : this.world.worldChunks.values()) {
            for (int x = 0; x < 10; ++x) {
                for (int y = 0; y < 10; ++y) {
                    GroundTile g = chunk.getGroundTile(x, y);
                    if (!g.isDiscovered()) continue;
                    this.lightBatch.draw(this.lightTexture, g.getXCenter() - Tile.GRID_SIZE * 2.0f, g.getYCenter() - Tile.GRID_SIZE * 2.0f, Tile.GRID_SIZE * 4.0f, Tile.GRID_SIZE * 4.0f, 0, 0, FOG_TEXTURE_SIZE, FOG_TEXTURE_SIZE, false, false);
                }
            }
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
}

