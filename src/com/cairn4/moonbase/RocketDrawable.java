/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.LaunchPad;
import com.cairn4.moonbase.ui.CameraLag;

public class RocketDrawable {
    private boolean moving = false;
    private World world;
    private LaunchPad launchPad;
    private int worldX;
    private int worldY;
    private Group group;
    private Group movingGroup;
    private Image image;
    private Image engine;
    private ParticleActor groundDust;
    private PointLight light;
    private Image doorOpen;
    private AdditiveImage engineGlow;
    ParticleActor takeoffFlames;
    protected static Color POWER_OFF_COLOR = new Color(0.3f, 0.3f, 0.3f, 1.0f);

    public boolean isMoving() {
        return this.moving;
    }

    public RocketDrawable(World world, LaunchPad launchPad, int worldX, int worldY) {
        this.world = world;
        this.launchPad = launchPad;
        this.worldX = worldX;
        this.worldY = worldY;
        this.createDrawables();
        this.addLight();
    }

    private void addLight() {
        this.light = new PointLight(this.world.rayHandler, 12, new Color(0.5f, 0.8f, 1.0f, 1.0f), 1.5625f, this.group.getX() / 256.0f, (this.group.getY() + 30.0f) / 256.0f);
        this.light.setXray(true);
        this.light.setActive(false);
    }

    public void update(float delta) {
        Vector2 moveGroupPos = this.movingGroup.localToStageCoordinates(Vector2.Zero);
        this.light.setPosition(moveGroupPos.x / 256.0f, (moveGroupPos.y - 30.0f) / 256.0f);
    }

    public void createDrawables() {
        this.group = new Group();
        this.world.gameScreen.mainGroup.addActor(this.group);
        this.group.setPosition(this.launchPad.getXCenter(), this.launchPad.getYCenter() + 50.0f);
        ParticleEffect f = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/launchpad-flames.p", ParticleEffect.class));
        this.takeoffFlames = new ParticleActor(f, false);
        this.takeoffFlames.setPosition(0.0f, -25.0f);
        this.takeoffFlames.pfx.allowCompletion();
        this.group.addActor(this.takeoffFlames);
        this.movingGroup = new Group();
        this.group.addActor(this.movingGroup);
        this.engine = new Image(this.world.gameScreen.skin.getDrawable("lander-engine"));
        this.engine.setOrigin(2);
        this.engine.setSize(70.0f, 66.0f);
        this.engine.setPosition(0.0f, -35.0f, 2);
        this.engine.setVisible(false);
        this.movingGroup.addActor(this.engine);
        this.image = new Image(this.world.gameScreen.skin.getDrawable("rocket2"));
        this.image.setSize(186.0f, 266.25f);
        this.image.setPosition(0.0f, -75.0f, 4);
        this.group.setUserObject(Float.valueOf(this.group.getY(1)));
        this.movingGroup.addActor(this.image);
        this.doorOpen = new Image(this.world.gameScreen.skin.getDrawable("rocket-dooropen"));
        this.doorOpen.setSize(116.25f, 72.75f);
        this.doorOpen.setPosition(0.0f, 57.0f, 4);
        this.movingGroup.addActor(this.doorOpen);
        this.engineGlow = new AdditiveImage(this.world.gameScreen.skin.getDrawable("rocket-engineglow"));
        this.engineGlow.setSize(155.25f, 117.0f);
        this.engineGlow.setPosition(0.0f, -75.0f, 4);
        this.movingGroup.addActor(this.engineGlow);
        this.engineGlow.setVisible(false);
    }

    public void takeOff() {
        this.moving = true;
        this.group.setVisible(true);
        this.world.gameScreen.cameraShake.shake(5.0f, 90.0f, 2.0f);
        this.world.gameScreen.hud.simpleFadeOut(1.0f, 1.0f);
        this.world.gameScreen.cameraLag.setZoom(CameraLag.DRIVEZOOM, 1.75f, Interpolation.sine);
        if (this.groundDust == null) {
            ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/lander-smoke.p", ParticleEffect.class));
            this.groundDust = new ParticleActor(p, false);
        }
        this.groundDust.setPosition(0.0f, -50.0f);
        this.groundDust.pfx.start();
        this.groundDust.toBack();
        this.group.addActor(this.groundDust);
        this.movingGroup.addAction(Actions.sequence((Action)Actions.parallel((Action)Actions.moveBy(0.0f, 1000.0f, 2.0f, Interpolation.circleIn)), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                RocketDrawable.this.finishLaunch();
            }
        })));
    }

    private void finishLaunch() {
        MoonBase.debug("Rocket Drawable: finishLaunch()");
        this.takeoffFlames.pfx.allowCompletion();
        this.launchPad.finishTakeOff();
        this.light.setActive(false);
        this.groundDust.pfx.allowCompletion();
    }

    public void land() {
        MoonBase.debug("Rocket Drawable: land()");
        this.world.gameScreen.cameraShake.shake(5.0f, 90.0f, 1.75f);
        this.world.gameScreen.cameraLag.setZoom(CameraLag.WALKZOOM, 1.75f, Interpolation.sine);
        this.world.gameScreen.hud.simpleFadeIn(0.0f, 0.25f);
        this.reset();
        this.show();
        Audio.getInstance().playSound("sounds/lander-rocketdown.ogg");
        this.closeDoor();
        if (this.groundDust == null) {
            ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/lander-smoke.p", ParticleEffect.class));
            this.groundDust = new ParticleActor(p, false);
        }
        this.groundDust.setPosition(0.0f, -50.0f);
        this.group.addActor(this.groundDust);
        this.groundDust.toBack();
        this.groundDust.pfx.start();
        this.engine.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.delay(1.5f), (Action)Actions.parallel((Action)Actions.fadeOut(0.5f), (Action)Actions.scaleTo(1.0f, 0.0f, 0.5f))));
        this.engineGlow.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.delay(1.5f), (Action)Actions.fadeOut(3.5f), (Action)Actions.visible(false)));
        this.movingGroup.addAction(Actions.sequence((Action)Actions.moveBy(0.0f, 1000.0f), (Action)Actions.moveBy(0.0f, -900.0f, 1.5f, Interpolation.sineOut), (Action)Actions.moveBy(0.0f, -50.0f, 0.25f, Interpolation.sine), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                RocketDrawable.this.finishLanding();
            }
        })));
    }

    private void finishLanding() {
        this.light.setActive(false);
        this.world.gameScreen.cameraShake.shake(15.0f, 40.0f, 0.25f);
        this.world.gameScreen.hud.showHudAfterLaunch();
        this.doorOpen.setVisible(true);
        this.moving = false;
        this.launchPad.setHasRocket(true);
        this.launchPad.resetTowerAnim();
        this.world.player.setFlyingRocket(false);
        this.world.player.exitRocket();
        this.groundDust.pfx.allowCompletion();
        Audio.getInstance().playSound("sounds/lander-hitground.ogg");
    }

    public void reset() {
        this.group.setPosition(this.launchPad.getXCenter(), this.launchPad.getYCenter());
        this.group.setVisible(this.launchPad.hasRocket);
        this.light.setActive(this.launchPad.hasRocket && this.launchPad.hasPower);
        this.movingGroup.setPosition(0.0f, 0.0f);
    }

    public void hide() {
        this.group.setVisible(false);
        this.light.setActive(false);
    }

    public void show() {
        this.group.setVisible(true);
        this.light.setActive(true);
    }

    public void remove() {
        this.group.remove();
        this.light.remove();
    }

    public void setPower(boolean b) {
        if (this.launchPad.hasRocket) {
            this.light.setActive(b);
        } else {
            this.light.setActive(false);
        }
        if (b) {
            this.animateColor(this.image, Color.WHITE, 0.5f);
        } else {
            this.animateColor(this.image, POWER_OFF_COLOR, 0.5f);
        }
    }

    protected void animateColor(Image i, Color c, float speed) {
        i.clearActions();
        i.addAction(Actions.sequence((Action)Actions.color(c, speed)));
    }

    public void closeDoor() {
        this.doorOpen.setVisible(false);
        Audio.getInstance().playSound("sounds/rocket-door-close.ogg", 0.6f, 1.0f);
    }

    public void openDoor() {
        this.doorOpen.setVisible(true);
    }

    public void startEngine() {
        Audio.getInstance().playSound("sounds/rocket-launch.ogg");
        this.light.setActive(true);
        this.takeoffFlames.pfx.start();
        this.engineGlow.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.fadeIn(0.25f)));
        this.engine.addAction(Actions.sequence((Action)Actions.visible(true), (Action)Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, 0.25f), (Action)Actions.fadeIn(0.25f)), (Action)Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.0f, 1.5f, 0.05f), (Action)Actions.scaleTo(1.0f, 0.95f, 0.05f)))));
    }
}

