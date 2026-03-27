/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.tiles.StorageColorOptions;
import com.cairn4.moonbase.ui.BaseScreen;

public class CrateColorTest {
    private Image colorSwatch;
    private Label colorLabel;
    Color crateTestColor = new Color(Color.WHITE);

    public CrateColorTest(BaseScreen baseScreen, Label.LabelStyle labelStyle, Group hudGroup) {
        NinePatch sliderBackground = new NinePatch(baseScreen.skin.getRegion("slider-bg"), 0, 0, 4, 4);
        NinePatch sliderKnobBefore = new NinePatch(baseScreen.skin.getRegion("slider-bg-on"), 0, 0, 4, 4);
        Drawable sliderThumb = baseScreen.skin.getDrawable("slider-thumb");
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = new NinePatchDrawable(sliderBackground);
        sliderStyle.knobBefore = new NinePatchDrawable(sliderKnobBefore);
        sliderStyle.knob = sliderThumb;
        Table table = new Table();
        hudGroup.addActor(table);
        table.setSize(600.0f, 300.0f);
        table.setPosition(100.0f, 300.0f);
        final Slider redSlider = new Slider(0.0f, 1.0f, 0.01f, false, sliderStyle);
        redSlider.setHeight(20.0f);
        redSlider.addListener(new DragListener(){

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                CrateColorTest.this.crateTestColor.r = redSlider.getValue();
                CrateColorTest.this.colorSliderChange();
            }
        });
        table.add(redSlider).width(400.0f).height(20.0f).space(5.0f).fill();
        table.row();
        final Slider greenSlider = new Slider(0.0f, 1.0f, 0.01f, false, sliderStyle);
        greenSlider.setHeight(20.0f);
        greenSlider.addListener(new DragListener(){

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                CrateColorTest.this.crateTestColor.g = greenSlider.getValue();
                CrateColorTest.this.colorSliderChange();
            }
        });
        table.add(greenSlider).width(400.0f).height(20.0f).space(5.0f).fill();
        table.row();
        final Slider blueSlider = new Slider(0.0f, 1.0f, 0.01f, false, sliderStyle);
        blueSlider.setHeight(20.0f);
        blueSlider.addListener(new DragListener(){

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                CrateColorTest.this.crateTestColor.b = blueSlider.getValue();
                CrateColorTest.this.colorSliderChange();
            }
        });
        table.add(blueSlider).width(400.0f).height(20.0f).space(5.0f).fill();
        table.row();
        final Slider alphaSlider = new Slider(0.0f, 1.0f, 0.01f, false, sliderStyle);
        alphaSlider.setHeight(20.0f);
        alphaSlider.addListener(new DragListener(){

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                CrateColorTest.this.crateTestColor.a = alphaSlider.getValue();
                CrateColorTest.this.colorSliderChange();
            }
        });
        table.add(alphaSlider).width(400.0f).height(20.0f).space(5.0f).fill();
        table.row();
        this.colorSwatch = new Image(baseScreen.skin.getDrawable("rect"));
        this.colorSwatch.setColor(Color.WHITE);
        table.add(this.colorSwatch).width(70.0f).height(70.0f);
        table.row();
        this.colorLabel = new Label((CharSequence)"XXX ", labelStyle);
        this.colorLabel.setFontScale(0.35f);
        table.add(this.colorLabel);
    }

    private void colorSliderChange() {
        this.colorSwatch.setColor(this.crateTestColor);
        StorageColorOptions.overrideColor(StorageColorOptions.editColorIndex, this.crateTestColor);
        this.colorLabel.setText("" + this.crateTestColor.toString());
    }
}

