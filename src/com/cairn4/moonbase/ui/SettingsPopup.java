/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.SettingsData;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.CheckboxRow;
import com.cairn4.moonbase.ui.ControlMapPopup;
import com.cairn4.moonbase.ui.LanguagePopup;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;

public class SettingsPopup
extends Popup {
    private SettingsData settingsDataCopy;
    private TextButton btnCancel;
    private TextButton btnAccept;
    private Table table;
    private TextButton btnLanguage;
    private TextButton btnControls;
    private TextButton btnFullScreen;
    private TextButton btnHighRes;
    private TextButton btnResolution;
    private CheckboxRow cbtnFullScreen;
    private CheckboxRow cbtnHighRes;
    private CheckboxRow cbtnVsync;
    private CheckboxRow cbtnVirtualJoystick;
    private CheckboxRow cbtnController;
    private Slider volumeSlider;
    private Slider musicSlider;
    Slider.SliderStyle sliderStyle;
    private Label restartRequiredLabel;
    private Image soundIcon;
    private Image musicIcon;

    public SettingsPopup(BaseScreen baseScreen) {
        super(baseScreen);
        this.settingsDataCopy = new SettingsData(SettingsLoader.getInstance().settingsData);
        this.setup();
        this.show();
    }

    @Override
    public void returned() {
        super.returned();
        System.out.println("returned to settings popup");
        this.settingsDataCopy = new SettingsData(SettingsLoader.getInstance().settingsData);
        this.cbtnFullScreen.setChecked(Gdx.graphics.isFullscreen());
    }

    @Override
    public void setup() {
        super.setup();
        this.tintBgAlpha = 0.8f;
        this.setSize(900.0f, 630.0f);
        this.setTitle("options");
        NinePatch sliderBackground = new NinePatch(this.baseScreen.skin.getRegion("slider-bg"), 0, 0, 4, 4);
        NinePatch sliderKnobBefore = new NinePatch(this.baseScreen.skin.getRegion("slider-bg-on"), 0, 0, 4, 4);
        Drawable sliderThumb = this.baseScreen.skin.getDrawable("slider-thumb");
        this.sliderStyle = new Slider.SliderStyle();
        this.sliderStyle.background = new NinePatchDrawable(sliderBackground);
        this.sliderStyle.knobBefore = new NinePatchDrawable(sliderKnobBefore);
        this.sliderStyle.knob = sliderThumb;
        float basicRowHeight = 80.0f;
        float spacing = 5.0f;
        this.table = new Table();
        this.table.center();
        this.popupGroup.addActor(this.table);
        this.table.setSize(this.panelBg.getWidth() - 150.0f, this.panelBg.getHeight() - 195.0f);
        this.table.setPosition(75.0f, 60.0f);
        Table sliderTable = new Table();
        this.table.add(sliderTable).fillX().colspan(2).space(40.0f);
        this.soundIcon = new Image(this.baseScreen.skin.getDrawable("icon-sound"));
        sliderTable.add(this.soundIcon).width(50.0f).height(50.0f).space(15.0f).spaceBottom(30.0f).right().expandX();
        this.volumeSlider = new Slider(0.0f, 1.0f, 0.05f, false, this.sliderStyle);
        this.volumeSlider.setHeight(20.0f);
        this.volumeSlider.setValue(this.settingsDataCopy.SOUNDFX_VOLUME.floatValue());
        this.updateSoundIcon(this.volumeSlider.getValue());
        this.volumeSlider.addListener(new DragListener(){

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                ((SettingsPopup)SettingsPopup.this).settingsDataCopy.SOUNDFX_VOLUME = Float.valueOf(SettingsPopup.this.volumeSlider.getValue());
                Audio.getInstance().setSoundVolume(SettingsPopup.this.volumeSlider.getValue());
                Audio.getInstance().playSound("sounds/menubutton_up.ogg", 0.7f, 1.0f);
                SettingsPopup.this.updateSoundIcon(SettingsPopup.this.volumeSlider.getValue());
            }
        });
        this.volumeSlider.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((SettingsPopup)SettingsPopup.this).settingsDataCopy.SOUNDFX_VOLUME = Float.valueOf(SettingsPopup.this.volumeSlider.getValue());
                Audio.getInstance().setSoundVolume(SettingsPopup.this.volumeSlider.getValue());
                Audio.getInstance().playSound("sounds/menubutton_up.ogg", 0.7f, 1.0f);
                SettingsPopup.this.updateSoundIcon(SettingsPopup.this.volumeSlider.getValue());
            }
        });
        sliderTable.add(this.volumeSlider).height(20.0f).width(370.0f).left().space(15.0f).fill().expandX().spaceBottom(30.0f);
        sliderTable.row();
        this.musicIcon = new Image(this.baseScreen.skin.getDrawable("icon-music"));
        sliderTable.add(this.musicIcon).width(50.0f).height(50.0f).space(15.0f).spaceBottom(40.0f).right().expandX();
        this.musicSlider = new Slider(0.0f, 1.0f, 0.05f, false, this.sliderStyle);
        this.musicSlider.setHeight(20.0f);
        this.musicSlider.setValue(this.settingsDataCopy.MUSIC_VOLUME.floatValue());
        this.updateMusicIcon(this.musicSlider.getValue());
        this.musicSlider.addListener(new DragListener(){

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                ((SettingsPopup)SettingsPopup.this).settingsDataCopy.MUSIC_VOLUME = Float.valueOf(SettingsPopup.this.musicSlider.getValue());
                Audio.getInstance().setMusicVolume(SettingsPopup.this.musicSlider.getValue());
                SettingsPopup.this.updateMusicIcon(SettingsPopup.this.musicSlider.getValue());
            }
        });
        this.musicSlider.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                ((SettingsPopup)SettingsPopup.this).settingsDataCopy.MUSIC_VOLUME = Float.valueOf(SettingsPopup.this.musicSlider.getValue());
                Audio.getInstance().setMusicVolume(SettingsPopup.this.musicSlider.getValue());
                SettingsPopup.this.updateMusicIcon(SettingsPopup.this.musicSlider.getValue());
            }
        });
        sliderTable.add(this.musicSlider).height(20.0f).width(370.0f).left().space(15.0f).fill().expandX().spaceBottom(20.0f);
        this.table.row();
        Table splitLeft = new Table();
        this.table.add(splitLeft).fillX().width(300.0f).space(40.0f).spaceTop(30.0f);
        this.btnLanguage = new TextButton(Localization.get("options_language"), this.baseScreen.textBtnStyle);
        this.btnLanguage.getLabel().setFontScale(0.45f);
        this.btnLanguage.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                SettingsPopup.this.baseScreen.menuForwardSound();
                SettingsPopup.this.baseScreen.showMenu(new LanguagePopup(SettingsPopup.this.baseScreen));
            }
        });
        splitLeft.add(this.btnLanguage).height(basicRowHeight).space(spacing).fillX().expandX();
        splitLeft.row();
        this.btnControls = new TextButton(Localization.get("options_controls"), this.baseScreen.textBtnStyle);
        this.btnControls.setChecked(true);
        this.btnControls.getLabel().setFontScale(0.45f);
        this.btnControls.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                SettingsPopup.this.baseScreen.menuForwardSound();
                SettingsPopup.this.baseScreen.showMenu(new ControlMapPopup(SettingsPopup.this.baseScreen));
            }
        });
        splitLeft.add(this.btnControls).height(basicRowHeight).space(spacing).fillX().expandX();
        Table splitRight = new Table();
        this.table.add(splitRight).uniformX().fillX().expandX().space(40.0f).spaceTop(30.0f);
        this.cbtnFullScreen = new CheckboxRow(this, Localization.get("options_fullscreen"), this.settingsDataCopy.FULL_SCREEN);
        splitRight.add(this.cbtnFullScreen.table).fillX().expandX().space(spacing);
        this.cbtnFullScreen.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                SettingsPopup.this.baseScreen.menuForwardSound();
                SettingsPopup.this.baseScreen.game.toggleFullScreen();
                SettingsPopup.this.settingsDataCopy = new SettingsData(SettingsLoader.getInstance().settingsData);
                SettingsPopup.this.cbtnFullScreen.setChecked(((SettingsPopup)SettingsPopup.this).settingsDataCopy.FULL_SCREEN);
            }
        });
        splitRight.row();
        this.cbtnHighRes = new CheckboxRow(this, Localization.get("options_HighRes"), this.settingsDataCopy.FULLHD);
        splitRight.add(this.cbtnHighRes.table).fillX().expandX().space(spacing);
        this.cbtnHighRes.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                SettingsPopup.this.baseScreen.menuForwardSound();
                ((SettingsPopup)SettingsPopup.this).settingsDataCopy.FULLHD = !((SettingsPopup)SettingsPopup.this).settingsDataCopy.FULLHD;
                SettingsPopup.this.baseScreen.game.saveHdMode(((SettingsPopup)SettingsPopup.this).settingsDataCopy.FULLHD);
                SettingsPopup.this.settingsDataCopy = new SettingsData(SettingsLoader.getInstance().settingsData);
                SettingsPopup.this.cbtnHighRes.setChecked(((SettingsPopup)SettingsPopup.this).settingsDataCopy.FULLHD);
                SettingsPopup.this.restartRequiredLabel.setVisible(true);
            }
        });
        splitRight.row();
        String warning = !this.settingsDataCopy.VSYNC ? " [#ff2200](physics bugs!)[]" : "";
        this.cbtnVsync = new CheckboxRow(this, Localization.get("options_vsync") + warning, this.settingsDataCopy.VSYNC);
        splitRight.add(this.cbtnVsync.table).fillX().expandX().space(spacing);
        this.cbtnVsync.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                SettingsPopup.this.baseScreen.menuForwardSound();
                ((SettingsPopup)SettingsPopup.this).settingsDataCopy.VSYNC = !((SettingsPopup)SettingsPopup.this).settingsDataCopy.VSYNC;
                SettingsPopup.this.baseScreen.game.setVsync(((SettingsPopup)SettingsPopup.this).settingsDataCopy.VSYNC);
                SettingsPopup.this.settingsDataCopy = new SettingsData(SettingsLoader.getInstance().settingsData);
                String warning = !((SettingsPopup)SettingsPopup.this).settingsDataCopy.VSYNC ? " [#ff2200](physics bugs!)[]" : "";
                ((SettingsPopup)SettingsPopup.this).cbtnVsync.label.setText(Localization.get("options_vsync") + warning);
                SettingsPopup.this.cbtnVsync.setChecked(((SettingsPopup)SettingsPopup.this).settingsDataCopy.VSYNC);
            }
        });
        splitRight.row();
        this.cbtnVirtualJoystick = new CheckboxRow(this, Localization.get("options_virtualjoystick"), this.settingsDataCopy.VIRTUALJOYSTICK);
        splitRight.add(this.cbtnVirtualJoystick.table).fillX().expandX().space(spacing);
        this.cbtnVirtualJoystick.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                SettingsPopup.this.baseScreen.menuForwardSound();
                ((SettingsPopup)SettingsPopup.this).settingsDataCopy.VIRTUALJOYSTICK = !((SettingsPopup)SettingsPopup.this).settingsDataCopy.VIRTUALJOYSTICK;
                System.out.println("joystick: " + ((SettingsPopup)SettingsPopup.this).settingsDataCopy.VIRTUALJOYSTICK);
                SettingsPopup.this.save();
                SettingsPopup.this.cbtnVirtualJoystick.setChecked(((SettingsPopup)SettingsPopup.this).settingsDataCopy.VIRTUALJOYSTICK);
            }
        });
        this.restartRequiredLabel = new Label((CharSequence)Localization.get("options_restartRequired"), this.labelStyle);
        this.restartRequiredLabel.setFontScale(0.4f);
        this.restartRequiredLabel.setColor(Color.RED);
        this.restartRequiredLabel.setAlignment(2, 1);
        this.restartRequiredLabel.setPosition(this.panelBg.getX(1), this.panelBg.getY() + 50.0f, 1);
        this.popupGroup.addActor(this.restartRequiredLabel);
        this.restartRequiredLabel.setVisible(MoonBase.NEEDS_RESTART);
    }

    private void updateSoundIcon(float v) {
        this.soundIcon.getColor().a = v == 0.0f ? 0.4f : 1.0f;
    }

    private void updateMusicIcon(float v) {
        this.musicIcon.getColor().a = v == 0.0f ? 0.4f : 1.0f;
    }

    @Override
    public void back() {
        this.save();
        this.baseScreen.menuBackSound();
        super.back();
    }

    private void save() {
        Gdx.app.debug("MewnBase", "SettingsPopup: Saving...");
        SettingsLoader.getInstance().writeFile(this.settingsDataCopy);
        this.settingsDataCopy = new SettingsData(SettingsLoader.getInstance().settingsData);
    }
}

