/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.PlayerInputProcessor;
import com.cairn4.moonbase.ui.AutoScalingText;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.Tooltip;
import java.util.ArrayList;
import java.util.HashMap;

public class Menu {
    public static Color MENU_COLOR = new Color(1.0f, 0.69f, 0.23f, 1.0f);
    public static String MENU_COLOR_HEX = "#ffaf3a";
    Stage stage;
    BaseScreen baseScreen;
    Skin skin;
    BitmapFont font;
    Label.LabelStyle labelStyle;
    Label.LabelStyle headingStyle;
    Group menuGroup;
    Tooltip tooltip;
    SnapshotArray<AutoScalingText> textToAutoScale = new SnapshotArray();
    HashMap<Button, ArrayList<Button>> buttonNavMap = new HashMap();

    public void resize(int width, int height) {
    }

    public Menu(BaseScreen baseScreen) {
        this.baseScreen = baseScreen;
        Viewport v = baseScreen.viewport;
        this.stage = baseScreen.stage;
        if (baseScreen instanceof GameScreen) {
            v = ((GameScreen)baseScreen).hudViewport;
            this.stage = ((GameScreen)baseScreen).hudStage;
        }
        this.setInputFocus();
        this.skin = this.baseScreen.skin;
        this.font = this.baseScreen.font;
        this.labelStyle = this.baseScreen.labelStyle;
        this.headingStyle = this.baseScreen.headingStyle;
        this.menuGroup = new Group();
        this.stage.addActor(this.menuGroup);
    }

    public void addAutoScaleText(AutoScalingText button) {
        MoonBase.debug("adding button to autoscale");
        this.textToAutoScale.add(button);
    }

    public static void updateAutoScaleText(SnapshotArray<AutoScalingText> textObjects) {
        for (AutoScalingText a : textObjects) {
            a.autoSize();
        }
        textObjects.clear();
    }

    public void updateAutoScaleText() {
        for (AutoScalingText a : this.textToAutoScale) {
            a.autoSize();
            MoonBase.debug("autoscaling " + this.textToAutoScale.size);
        }
        this.textToAutoScale.clear();
    }

    public void setInputFocus() {
        InputMultiplexer inputMulti = new InputMultiplexer();
        PlayerInputProcessor input = new PlayerInputProcessor();
        inputMulti.addProcessor(this.stage);
        inputMulti.addProcessor(input);
        Gdx.input.setInputProcessor(inputMulti);
        if (this.baseScreen.game.console != null) {
            this.baseScreen.game.console.resetInputProcessing();
        }
    }

    protected void setup() {
    }

    public void update(float delta) {
    }

    public void render(float delta) {
        this.updateAutoScaleText();
    }

    public void show() {
        this.menuGroup.setVisible(true);
    }

    public void hide() {
        this.menuGroup.setVisible(false);
    }

    public void handleInput() {
        if (PlayerInput.isPressed(0)) {
            this.focus(DIRECTIONS.left);
        }
        if (PlayerInput.isPressed(1)) {
            this.focus(DIRECTIONS.right);
        }
        if (PlayerInput.isPressed(2)) {
            this.focus(DIRECTIONS.up);
        }
        if (PlayerInput.isPressed(3)) {
            this.focus(DIRECTIONS.down);
        }
        if (PlayerInput.wasPressed(5)) {
            this.select();
        }
        if (PlayerInput.wasPressed(6)) {
            this.back();
        }
        if (PlayerInput.wasPressed(26)) {
            this.baseScreen.game.toggleFullScreen();
        }
        PlayerInput.update();
    }

    public void back() {
        MoonBase.log("Back");
        this.hide();
        this.baseScreen.exitMenu();
    }

    public void returned() {
        this.show();
    }

    protected void focus(DIRECTIONS dir) {
        MoonBase.log("focus: " + (Object)((Object)dir));
    }

    protected void select() {
    }

    protected void enableButton(Button button) {
        button.setTouchable(Touchable.enabled);
        button.setDisabled(false);
    }

    protected void disableButton(Button button) {
        button.setTouchable(Touchable.disabled);
        button.setDisabled(true);
    }

    public void createTooltip(String tip) {
        if (this.tooltip != null) {
            this.removeTooltip();
        }
        this.tooltip = new Tooltip(this.baseScreen, this.stage, tip);
        this.stage.addActor(this.tooltip);
    }

    public void removeTooltip() {
        if (this.tooltip != null) {
            this.tooltip.remove();
        }
    }

    protected void finishedShowAnim() {
    }

    public static enum DIRECTIONS {
        up,
        down,
        left,
        right;

    }
}

