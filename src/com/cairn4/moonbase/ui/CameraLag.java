/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.Speaker;
import com.cairn4.utils.RollingAverage;

public class CameraLag {
    OrthographicCamera camera;
    Vector2 offset;
    float xThreshold;
    float yThreshold;
    Player player;
    float playerX;
    float playerY;
    private RollingAverage buggieRollingAverage;
    private static final float DRIVE_OFFSET = 250.0f;
    private static Vector2 driveOffsetVector = new Vector2(200.0f, 0.0f);
    public static float WALKZOOM = 1.0f;
    public static float DRIVEZOOM = 1.25f;
    public static float HDMODEMUL = 0.75f;
    private float zoomDuration;
    private static final float ZOOMTIME = 0.5f;
    private float zoom;
    private float targetZoom;
    private static float zoomTimer;
    private Interpolation zoomInterpolation = Interpolation.sine;
    private boolean panning = false;
    private Vector2 panTarget;
    private Vector2 panTargetStartPos;
    private float panTimer = 1.0f;
    private float panDuration = 1.0f;
    private Interpolation panInterpolation = Interpolation.sine;
    private Vector2 toggleBuggieStartPos;
    private static final float toggleBuggieTime = 0.5f;
    private static float toggleBuggieTimer;
    private Vector2 prevCameraPos;
    public Npc followingNpc;
    public float focusZoomTimer = 0.0f;
    public final float focusZoomTime = 1.0f;
    public Speaker focusSpeaker;
    float panLength = 0.66f;
    Vector2 prevSpeakerFocus;
    Vector2 targetSpeakerFocus;

    public CameraLag(Player player, OrthographicCamera camera, float xThreshold, float yThreshold) {
        this.player = player;
        this.camera = camera;
        this.xThreshold = xThreshold;
        this.yThreshold = yThreshold;
        this.offset = new Vector2(0.0f, 0.0f);
        this.camera.position.set(this.player.getXPos(), this.player.getYPos(), 0.0f);
        this.playerX = this.player.getXPos();
        this.playerY = this.player.getYPos();
        this.zoom = 1.0f;
        this.targetZoom = 1.0f;
        this.panTargetStartPos = new Vector2(0.0f, 0.0f);
        this.panTarget = new Vector2(0.0f, 0.0f);
        if (SettingsLoader.getInstance().settingsData.FULLHD) {
            this.zoom *= HDMODEMUL;
            this.targetZoom *= HDMODEMUL;
            zoomTimer = 0.0f;
        }
        Gdx.app.debug("MewnBase", "CameraLag: zoom set to: " + camera.zoom);
        this.prevCameraPos = new Vector2(camera.position.x, camera.position.y);
        this.toggleBuggieStartPos = this.prevCameraPos.cpy();
        this.buggieRollingAverage = new RollingAverage(30);
    }

    public void center() {
        this.offset.set(0.0f, 0.0f);
        this.playerX = this.player.getXPos();
        this.playerY = this.player.getYPos();
        this.zoom = 1.0f;
        this.targetZoom = 1.0f;
        if (SettingsLoader.getInstance().settingsData.FULLHD) {
            this.zoom *= HDMODEMUL;
            this.targetZoom *= HDMODEMUL;
        }
        this.prevCameraPos = new Vector2(this.camera.position.x, this.camera.position.y);
        this.toggleBuggieStartPos = this.prevCameraPos.cpy();
        this.camera.update();
        Gdx.app.debug("MewnBase", "CameraLag: zoom set to: " + this.camera.zoom);
    }

    public void focusOnLocation(float xPos, float yPos) {
        this.camera.position.set(xPos, yPos, 0.0f);
        this.camera.update();
    }

    public void panTo(float targetX, float targetY, float duration, Interpolation interpolation) {
        this.panTo(this.camera.position.x, this.camera.position.y, targetX, targetY, duration, interpolation);
    }

    public void panTo(float startX, float startY, float targetX, float targetY, float duration, Interpolation interpolation) {
        if (this.panTargetStartPos == null) {
            this.panTargetStartPos = new Vector2(startX, startY);
        } else {
            this.panTargetStartPos.set(startX, startY);
        }
        if (this.panTarget == null) {
            this.panTarget = new Vector2(targetX, targetY);
        } else {
            this.panTarget.set(targetX, targetY);
        }
        this.panTimer = 0.0f;
        this.panDuration = duration;
        this.panInterpolation = interpolation;
        this.panning = true;
    }

    public void update(float delta) {
        float y;
        float x;
        if (this.focusSpeaker != null) {
            if (this.panTimer < this.panLength) {
                this.panTimer += delta;
                x = Interpolation.sine.apply(this.prevSpeakerFocus.x, this.targetSpeakerFocus.x, this.panTimer / this.panLength);
                y = Interpolation.sine.apply(this.prevSpeakerFocus.y, this.targetSpeakerFocus.y, this.panTimer / this.panLength);
                this.camera.position.set(x, y, 0.0f);
                this.camera.update();
            }
            if (zoomTimer <= 0.5f) {
                this.camera.zoom = Interpolation.sine.apply(this.zoom, this.targetZoom, (zoomTimer += delta) / 0.5f);
                this.camera.update();
            }
        } else if (this.player != null) {
            if (this.player.isDrivingVehicle()) {
                this.buggieCamera();
            } else if (!this.player.isFlyingRocket()) {
                float dX = 0.0f;
                float dY = 0.0f;
                dX = this.playerX - this.player.getXPos();
                dY = this.playerY - this.player.getYPos();
                this.move(dX, dY);
                this.playerX = this.player.getXPos();
                this.playerY = this.player.getYPos();
            }
        }
        if (this.panning) {
            if (this.panTimer <= this.panDuration) {
                this.panTimer += delta;
                if (this.panTimer > this.panDuration) {
                    this.camera.position.set(this.panTarget.x, this.panTarget.y, 0.0f);
                    this.panning = false;
                } else {
                    x = Interpolation.sine.apply(this.panTargetStartPos.x, this.panTarget.x, this.panTimer / this.panDuration);
                    y = Interpolation.sine.apply(this.panTargetStartPos.y, this.panTarget.y, this.panTimer / this.panDuration);
                    this.camera.position.set(x, y, 0.0f);
                }
                this.camera.update();
            } else {
                this.panning = false;
            }
        }
        if (zoomTimer <= this.zoomDuration) {
            this.camera.zoom = (zoomTimer += delta) > this.zoomDuration ? this.targetZoom : this.zoomInterpolation.apply(this.zoom, this.targetZoom, MathUtils.clamp(zoomTimer / this.zoomDuration, 0.001f, 1000.0f));
            this.camera.update();
        }
        if (toggleBuggieTimer < 0.5f) {
            x = Interpolation.sine.apply(this.toggleBuggieStartPos.x, this.camera.position.x, (toggleBuggieTimer += delta) / 0.5f);
            y = Interpolation.sine.apply(this.toggleBuggieStartPos.y, this.camera.position.y, toggleBuggieTimer / 0.5f);
            this.camera.position.set(x, y, 0.0f);
            this.camera.update();
        }
        this.prevCameraPos.set(this.camera.position.x, this.camera.position.y);
    }

    public void toggleBuggie() {
        if (this.player != null) {
            this.toggleBuggieStartPos.set(this.camera.position.x, this.camera.position.y);
            toggleBuggieTimer = 0.0f;
        }
    }

    public void setZoom(float z) {
        this.zoom = this.camera.zoom;
        this.targetZoom = z;
        this.zoomInterpolation = Interpolation.sine;
        this.zoomDuration = 0.5f;
        if (SettingsLoader.getInstance().settingsData.FULLHD) {
            this.targetZoom *= HDMODEMUL;
        }
        zoomTimer = 0.0f;
    }

    public void setZoom(float z, float duration, Interpolation interpolation) {
        this.zoom = this.camera.zoom;
        this.targetZoom = z;
        this.zoomDuration = duration;
        this.zoomInterpolation = interpolation;
        if (SettingsLoader.getInstance().settingsData.FULLHD) {
            this.targetZoom *= HDMODEMUL;
        }
        zoomTimer = 0.0f;
    }

    public void move(float xDir, float yDir) {
        this.offset.x += xDir;
        this.offset.y += yDir;
        if (this.offset.x > this.xThreshold) {
            this.offset.x = this.xThreshold;
        } else if (this.offset.x < -this.xThreshold) {
            this.offset.x = -this.xThreshold;
        }
        if (this.offset.y > this.yThreshold) {
            this.offset.y = this.yThreshold;
        } else if (this.offset.y < -this.yThreshold) {
            this.offset.y = -this.yThreshold;
        }
        this.camera.position.set(this.player.getXPos() + this.offset.x, this.player.getYPos() + this.offset.y, 0.0f);
        this.camera.position.x = MathUtils.round(this.camera.position.x);
        this.camera.position.y = MathUtils.round(this.camera.position.y);
        this.camera.update();
    }

    private void buggieCamera() {
        this.buggieRollingAverage.add(250.0f * this.player.getVehicle().getSpeedFraction());
        this.offset.set(this.buggieRollingAverage.getAverage(), 0.0f);
        this.offset.rotate(this.player.getVehicle().getRotation());
        this.camera.position.set(this.player.getXPos() + this.offset.x, this.player.getYPos() + this.offset.y, 0.0f);
        this.camera.update();
    }

    public void focusOnSpeaker(Speaker speaker) {
        this.prevSpeakerFocus = new Vector2(this.camera.position.x, this.camera.position.y);
        float x = Interpolation.linear.apply(this.prevSpeakerFocus.x, speaker.getXPos(), 0.5f);
        float y = Interpolation.linear.apply(this.prevSpeakerFocus.y, speaker.getYPos(), 0.5f);
        this.targetSpeakerFocus = new Vector2(x, y);
        this.focusSpeaker = speaker;
        this.panTimer = 0.0f;
    }

    public void clearSpeakerFocus() {
        this.focusSpeaker = null;
        this.center();
    }
}

