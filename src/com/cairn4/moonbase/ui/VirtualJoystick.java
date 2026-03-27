/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.ui.Hud;

public class VirtualJoystick {
    protected Hud hud;
    private Boolean moveable = false;
    private Group touchpadGroup;
    private Touchpad touchpad;
    private Image touchpadMover;
    private Button touchpadLock;
    private Vector2 touchpadPosition = new Vector2(MoonBase.SCREEN_WIDTH - 10, 100.0f);
    private Vector2 touchpadStartPosition = new Vector2(MoonBase.SCREEN_WIDTH - 10, 100.0f);
    public Vector2 dragStarPos = new Vector2(0.0f, 0.0f);
    public Vector2 dragGroupStartPos = new Vector2(0.0f, 0.0f);
    public Vector2 dragOffset = new Vector2(0.0f, 0.0f);

    public VirtualJoystick(Hud h, Group parentGroup) {
        this.hud = h;
        Vector2 posRatio = SettingsLoader.getInstance().settingsData.VIRTUALJOYSTICK_POS;
        this.touchpadPosition.set(posRatio.x * (float)MoonBase.SCREEN_WIDTH, posRatio.y * (float)MoonBase.SCREEN_HEIGHT);
        this.touchpadGroup = new Group();
        this.touchpadGroup.setSize(200.0f, 200.0f);
        this.touchpadGroup.setPosition(this.touchpadPosition.x, this.touchpadPosition.y, 20);
        parentGroup.addActor(this.touchpadGroup);
        Touchpad.TouchpadStyle tps = new Touchpad.TouchpadStyle();
        tps.background = this.hud.skin.getDrawable("hud/touchpad-bg");
        tps.knob = this.hud.skin.getDrawable("hud/touchpad-stick2");
        this.touchpad = new Touchpad(5.0f, tps);
        this.touchpad.setSize(200.0f, 200.0f);
        this.touchpad.setColor(1.0f, 1.0f, 1.0f, 0.5f);
        this.touchpadGroup.addActor(this.touchpad);
        this.touchpad.layout();
        this.touchpad.addListener(new ChangeListener(){

            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Touchpad tp = (Touchpad)actor;
                PlayerInput.stickMoveX = tp.getKnobPercentX();
                PlayerInput.stickMoveY = tp.getKnobPercentY();
            }
        });
        NinePatch moverPatch = new NinePatch(this.hud.skin.getRegion("hud/touchpad-mover"), 30, 30, 30, 30);
        this.touchpadMover = new Image(moverPatch);
        this.touchpadMover.setSize(200.0f, 200.0f);
        this.touchpadMover.setColor(0.0f, 0.5f, 0.5f, 0.15f);
        this.touchpadMover.setTouchable(Touchable.disabled);
        this.touchpadMover.setVisible(false);
        this.touchpadGroup.addActor(this.touchpadMover);
        Button.ButtonStyle bs = new Button.ButtonStyle();
        bs.up = this.hud.skin.getDrawable("hud/touchpad-lock");
        bs.down = this.hud.skin.getDrawable("hud/touchpad-lock");
        bs.checked = this.hud.skin.getDrawable("hud/touchpad-unlocked");
        this.touchpadLock = new Button(bs);
        this.touchpadLock.setPosition(188.0f, 7.0f, 20);
        this.touchpadLock.setColor(1.0f, 1.0f, 1.0f, 0.25f);
        this.touchpadGroup.addActor(this.touchpadLock);
        this.touchpadLock.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (VirtualJoystick.this.moveable.booleanValue()) {
                    VirtualJoystick.this.touchpadLock.setChecked(false);
                    VirtualJoystick.this.touchpadMover.setVisible(false);
                    VirtualJoystick.this.touchpadMover.setTouchable(Touchable.disabled);
                    VirtualJoystick.this.touchpadMover.clearListeners();
                    VirtualJoystick.this.savePosition();
                    VirtualJoystick.this.moveable = false;
                } else {
                    VirtualJoystick.this.touchpadLock.setChecked(true);
                    VirtualJoystick.this.touchpadMover.setVisible(true);
                    VirtualJoystick.this.touchpadMover.setTouchable(Touchable.enabled);
                    VirtualJoystick.this.addDragListener();
                    VirtualJoystick.this.moveable = true;
                }
            }
        });
    }

    private void savePosition() {
        float x = this.touchpadGroup.getX(20) / (float)MoonBase.SCREEN_WIDTH;
        float y = this.touchpadGroup.getY(20) / (float)MoonBase.SCREEN_HEIGHT;
        SettingsLoader.getInstance().settingsData.VIRTUALJOYSTICK_POS.set(x, y);
        SettingsLoader.getInstance().save();
    }

    private void addDragListener() {
        this.touchpadMover.addListener(new DragListener(){

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                float mouseX = Gdx.input.getX();
                float mouseY = Gdx.input.getY();
                Vector2 mouseScreenCoord = VirtualJoystick.this.hud.gameScreen.hudStage.screenToStageCoordinates(new Vector2(mouseX, mouseY));
                VirtualJoystick.this.dragStarPos.set(VirtualJoystick.this.touchpadMover.stageToLocalCoordinates(mouseScreenCoord));
                System.out.println("dragStarPos: " + VirtualJoystick.this.dragStarPos);
                VirtualJoystick.this.dragGroupStartPos.set(VirtualJoystick.this.touchpadGroup.getX(), VirtualJoystick.this.touchpadGroup.getY());
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                float mouseX = Gdx.input.getX();
                float mouseY = Gdx.input.getY();
                Vector2 mouseScreenCoord = VirtualJoystick.this.hud.gameScreen.hudStage.screenToStageCoordinates(new Vector2(mouseX, mouseY));
                VirtualJoystick.this.touchpadGroup.setPosition(mouseScreenCoord.x - VirtualJoystick.this.dragStarPos.x, mouseScreenCoord.y - VirtualJoystick.this.dragStarPos.y);
                if (VirtualJoystick.this.touchpadGroup.getX() < 10.0f) {
                    VirtualJoystick.this.touchpadGroup.setX(10.0f);
                }
                if (VirtualJoystick.this.touchpadGroup.getX() > (float)MoonBase.SCREEN_WIDTH - VirtualJoystick.this.touchpadGroup.getWidth() - 10.0f) {
                    VirtualJoystick.this.touchpadGroup.setX((float)MoonBase.SCREEN_WIDTH - VirtualJoystick.this.touchpadGroup.getWidth() - 10.0f);
                }
                if (VirtualJoystick.this.touchpadGroup.getY() < 10.0f) {
                    VirtualJoystick.this.touchpadGroup.setY(10.0f);
                }
                if (VirtualJoystick.this.touchpadGroup.getY() > (float)MoonBase.SCREEN_HEIGHT - VirtualJoystick.this.touchpadGroup.getHeight() - 10.0f) {
                    VirtualJoystick.this.touchpadGroup.setY((float)MoonBase.SCREEN_HEIGHT - VirtualJoystick.this.touchpadGroup.getHeight() - 10.0f);
                }
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
            }
        });
    }

    public void update(float delta) {
        this.touchpadGroup.setVisible(SettingsLoader.getInstance().settingsData.VIRTUALJOYSTICK);
        if (this.hud.player.isDrivingVehicle()) {
            this.touchpadGroup.setTouchable(Touchable.disabled);
            this.touchpadGroup.setColor(1.0f, 1.0f, 1.0f, 0.25f);
        } else {
            this.touchpadGroup.setTouchable(Touchable.enabled);
            this.touchpadGroup.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}

