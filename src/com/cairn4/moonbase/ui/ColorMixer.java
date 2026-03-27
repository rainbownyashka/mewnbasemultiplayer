/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Tutorial;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.Localization;

public class ColorMixer {
    public Hud menu;
    Group parentGroup;
    Tutorial tutorial;
    Group group;
    Table table;
    Label text;
    TextButton textButton;
    private Slider.SliderStyle sliderStyle;
    private static float PANEL_SMALL = 146.0f;
    private static float PANEL_LARGE = 220.0f;
    private Color[] colorTable;
    private String set;
    private int colorNum;
    private Image colorSwatch;

    public ColorMixer(Hud hud, Group parentGroup, String set, int colorNum) {
        this.menu = hud;
        this.parentGroup = parentGroup;
        this.group = new Group();
        this.parentGroup.addActor(this.group);
        NinePatch sliderBackground = new NinePatch(hud.gameScreen.skin.getRegion("slider-bg"), 0, 0, 4, 4);
        NinePatch sliderKnobBefore = new NinePatch(hud.gameScreen.skin.getRegion("slider-bg-on"), 0, 0, 4, 4);
        Drawable sliderThumb = hud.gameScreen.skin.getDrawable("slider-thumb");
        this.sliderStyle = new Slider.SliderStyle();
        this.sliderStyle.background = new NinePatchDrawable(sliderBackground);
        this.sliderStyle.knobBefore = new NinePatchDrawable(sliderKnobBefore);
        this.sliderStyle.knob = sliderThumb;
        this.set = set;
        this.colorNum = colorNum;
        String rgb = "";
        switch (set) {
            case "hab": {
                this.colorTable = Vars.habColors;
                rgb = this.colorTable[colorNum].r + ", " + this.colorTable[colorNum].b + ", " + this.colorTable[colorNum].g;
                hud.gameScreen.game.console.log("Hab color " + colorNum + " - " + rgb);
                break;
            }
            case "veh": {
                this.colorTable = Vars.vehicleColors;
                rgb = this.colorTable[colorNum].r + ", " + this.colorTable[colorNum].b + ", " + this.colorTable[colorNum].g;
                hud.gameScreen.game.console.log("Vehicle color " + colorNum + " - #" + rgb);
            }
        }
        this.setup();
        this.updateSwatch();
    }

    private void setup() {
        this.group.setPosition(MoonBase.SCREEN_WIDTH - 12, 110.0f);
        this.group.setVisible(false);
        NinePatch panelBg = new NinePatch(this.menu.skin.getRegion("textbox-mini"), 20, 20, 20, 20);
        NinePatchDrawable bg = new NinePatchDrawable(panelBg);
        this.table = new Table();
        this.table.pad(20.0f);
        this.table.setBackground(bg);
        this.group.addActor(this.table);
        this.table.row();
        this.text = new Label((CharSequence)("" + this.set + " - Color: " + this.colorNum), this.menu.labelStyle);
        this.text.setFontScale(0.35f);
        this.text.setWrap(true);
        this.text.setAlignment(1, 1);
        this.table.add(this.text).space(10.0f).center().width(360.0f).expandY().fill();
        this.table.row();
        this.colorSwatch = new Image(this.menu.skin.getDrawable("white"));
        this.table.add(this.colorSwatch).fillX().height(100.0f);
        this.table.row();
        final Color editColor = this.colorTable[this.colorNum];
        final Slider redSlider = new Slider(0.0f, 1.0f, 0.0039f, false, this.sliderStyle);
        redSlider.setHeight(20.0f);
        redSlider.setValue(editColor.r);
        redSlider.addListener(new DragListener(){

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                editColor.r = redSlider.getValue();
                ColorMixer.this.updateSwatch();
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                editColor.r = redSlider.getValue();
                ColorMixer.this.updateSwatch();
            }
        });
        redSlider.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                editColor.r = redSlider.getValue();
                ColorMixer.this.updateSwatch();
            }
        });
        this.table.add(redSlider).height(20.0f).width(370.0f).left().space(15.0f).fill().expandX().spaceBottom(30.0f);
        this.table.row();
        final Slider greenSlider = new Slider(0.0f, 1.0f, 0.0039f, false, this.sliderStyle);
        greenSlider.setHeight(20.0f);
        greenSlider.setValue(editColor.g);
        greenSlider.addListener(new DragListener(){

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                editColor.g = greenSlider.getValue();
                ColorMixer.this.updateSwatch();
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                editColor.g = greenSlider.getValue();
                ColorMixer.this.updateSwatch();
            }
        });
        greenSlider.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                editColor.g = greenSlider.getValue();
                ColorMixer.this.updateSwatch();
            }
        });
        this.table.add(greenSlider).height(20.0f).width(370.0f).left().space(15.0f).fill().expandX().spaceBottom(30.0f);
        this.table.row();
        final Slider blueSlider = new Slider(0.0f, 1.0f, 0.0039f, false, this.sliderStyle);
        blueSlider.setHeight(20.0f);
        blueSlider.setValue(editColor.b);
        blueSlider.addListener(new DragListener(){

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                editColor.b = blueSlider.getValue();
                ColorMixer.this.updateSwatch();
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                editColor.b = blueSlider.getValue();
                ColorMixer.this.updateSwatch();
            }
        });
        blueSlider.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                editColor.b = blueSlider.getValue();
                ColorMixer.this.updateSwatch();
            }
        });
        this.table.add(blueSlider).height(20.0f).width(370.0f).left().space(15.0f).fill().expandX().spaceBottom(30.0f);
        this.table.row();
        final Slider alphaSlider = new Slider(0.0f, 1.0f, 0.0039f, false, this.sliderStyle);
        alphaSlider.setHeight(20.0f);
        alphaSlider.setValue(editColor.a);
        alphaSlider.addListener(new DragListener(){

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                editColor.a = alphaSlider.getValue();
                ColorMixer.this.updateSwatch();
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                editColor.a = alphaSlider.getValue();
                ColorMixer.this.updateSwatch();
            }
        });
        alphaSlider.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                editColor.a = alphaSlider.getValue();
                ColorMixer.this.updateSwatch();
            }
        });
        this.table.add(alphaSlider).height(20.0f).width(370.0f).left().space(15.0f).fill().expandX().spaceBottom(30.0f);
        this.table.row();
        this.textButton = new TextButton(Localization.get("common_ok"), this.menu.baseScreen.textBtnStyle);
        this.textButton.getLabel().setFontScale(0.35f);
        this.textButton.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                int i = 0;
                for (Color c : ColorMixer.this.colorTable) {
                    System.out.println(i + " Color " + c);
                    ++i;
                }
                ColorMixer.this.hide();
            }
        });
        this.table.add(this.textButton).space(10.0f).minWidth(160.0f).height(65.0f).center();
        this.updateTable();
    }

    private void updateSwatch() {
        this.colorSwatch.setColor(this.colorTable[this.colorNum]);
    }

    public void updateTable() {
        this.table.pack();
        this.table.bottom().right();
        this.table.setPosition(0.0f, 0.0f, 20);
    }

    public void show() {
        this.group.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.fadeIn(0.25f)));
    }

    public void hide() {
        this.group.setTouchable(Touchable.disabled);
        this.group.addAction(Actions.sequence((Action)Actions.fadeOut(0.3f), (Action)Actions.visible(false), (Action)Actions.removeActor()));
    }
}

