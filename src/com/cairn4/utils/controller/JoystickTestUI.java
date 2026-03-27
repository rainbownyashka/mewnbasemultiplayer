/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.utils.controller;

import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.PlayerInputProcessor;

public class JoystickTestUI {
    Group group = new Group();
    Image base;
    Image stickL;
    Image stickR;
    Group ltGroup;
    Group rtGroup;
    Image rtBase;
    Image ltBase;
    Image lt;
    Image rt;
    Label cname;

    public JoystickTestUI(Skin skin, Label.LabelStyle labelStyle, Group parentGroup) {
        this.group.setVisible(false);
        parentGroup.addActor(this.group);
        this.group.setPosition(200.0f, 200.0f);
        this.cname = new Label((CharSequence)"Controller", labelStyle);
        this.cname.setFontScale(0.3f);
        this.cname.setAlignment(1);
        this.cname.setColor(1.0f, 1.0f, 1.0f, 0.8f);
        this.cname.setOrigin(1);
        this.cname.setPosition(0.0f, 80.0f, 4);
        this.group.addActor(this.cname);
        this.base = new Image(skin.getDrawable("joystick-base"));
        this.base.setSize(150.0f, 150.0f);
        this.base.setColor(1.0f, 1.0f, 1.0f, 0.5f);
        this.base.setPosition(0.0f, 0.0f, 1);
        this.group.addActor(this.base);
        this.stickL = new Image(skin.getDrawable("joystick"));
        this.stickL.setSize(40.0f, 40.0f);
        this.stickL.setColor(0.0f, 1.0f, 0.0f, 0.5f);
        this.stickL.setOrigin(1);
        this.stickL.setPosition(0.0f, 0.0f, 1);
        this.group.addActor(this.stickL);
        this.stickR = new Image(skin.getDrawable("joystick"));
        this.stickR.setSize(40.0f, 40.0f);
        this.stickR.setColor(1.0f, 0.0f, 0.0f, 0.5f);
        this.stickR.setOrigin(1);
        this.stickR.setPosition(0.0f, 0.0f, 1);
        this.group.addActor(this.stickR);
        this.ltGroup = new Group();
        this.ltGroup.setSize(20.0f, 120.0f);
        this.ltGroup.setPosition(-80.0f, 0.0f, 16);
        this.group.addActor(this.ltGroup);
        this.ltBase = new Image(skin.getDrawable("white"));
        this.ltBase.setSize(20.0f, 120.0f);
        this.ltBase.setColor(0.2f, 0.2f, 0.2f, 0.5f);
        this.ltBase.setPosition(0.0f, 0.0f);
        this.ltGroup.addActor(this.ltBase);
        this.lt = new Image(skin.getDrawable("white"));
        this.lt.setColor(0.0f, 1.0f, 0.0f, 0.7f);
        this.lt.setSize(20.0f, 40.0f);
        this.lt.setPosition(0.0f, 0.0f);
        this.ltGroup.addActor(this.lt);
        this.rtGroup = new Group();
        this.rtGroup.setSize(20.0f, 120.0f);
        this.rtGroup.setPosition(80.0f, 0.0f, 8);
        this.group.addActor(this.rtGroup);
        this.rtBase = new Image(skin.getDrawable("white"));
        this.rtBase.setSize(20.0f, 120.0f);
        this.rtBase.setColor(0.2f, 0.2f, 0.2f, 0.5f);
        this.rtBase.setPosition(0.0f, 0.0f);
        this.rtGroup.addActor(this.rtBase);
        this.rt = new Image(skin.getDrawable("white"));
        this.rt.setColor(1.0f, 0.0f, 0.0f, 0.7f);
        this.rt.setPosition(0.0f, 0.0f);
        this.rt.setSize(20.0f, 40.0f);
        this.rtGroup.addActor(this.rt);
    }

    public void setVisible(boolean v) {
        this.group.setVisible(v);
    }

    public void update() {
        if (Controllers.getControllers().size > 0 && PlayerInputProcessor.controllerMapping != null) {
            this.cname.setText(Controllers.getControllers().first().getName() + "\n" + PlayerInputProcessor.controllerMapping.getClass().getSimpleName());
        } else {
            this.cname.setText("No controller found.");
        }
        this.stickL.setPosition(PlayerInput.stickMoveX * 35.0f, PlayerInput.stickMoveY * 35.0f, 1);
        this.stickR.setPosition(PlayerInput.stick2MoveX * 35.0f, PlayerInput.stick2MoveY * 35.0f, 1);
        float rTvalue = 0.0f;
        if (Controllers.getControllers().size > 0) {
            rTvalue = Controllers.getControllers().first().getAxis(5);
        }
        this.lt.setPosition(0.0f, (PlayerInput.leftTriggerValue + 1.0f) * 40.0f);
        this.rt.setPosition(0.0f, (rTvalue + 1.0f) * 40.0f);
    }
}

