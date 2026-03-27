/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.ConfirmSettingsPopup;
import com.cairn4.moonbase.ui.Popup;

public class ResolutionPopup
extends Popup {
    ScrollPane scroller;
    Table table;
    Graphics.DisplayMode prevDisplayMode = Gdx.graphics.getDisplayMode();
    boolean prevFullScreen = Gdx.graphics.isFullscreen();
    int selectedDisplayMode;

    public ResolutionPopup(BaseScreen baseScreen) {
        super(baseScreen);
        this.setup();
        this.show();
    }

    @Override
    public void returned() {
        this.table.clearChildren();
        this.addResolutionOptions();
    }

    @Override
    public void setup() {
        super.setup();
        this.tintBgAlpha = 0.8f;
        this.setSize(850.0f, 640.0f);
        this.setTitle("options_fullscreenResolution");
        this.table = new Table();
        this.menuGroup.addActor(this.table);
        this.table.setSize(this.panelBg.getWidth() - 150.0f, this.panelBg.getHeight() - 275.0f);
        this.table.setPosition(75.0f, 150.0f);
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.baseScreen.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.baseScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)this.table, scrollStyle);
        this.scroller.setWidth(this.table.getWidth());
        this.scroller.setHeight(this.table.getHeight());
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setBounds(0.0f, 0.0f, this.panelBg.getWidth() - 150.0f, this.panelBg.getHeight() - 195.0f);
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        this.scroller.setPosition(75.0f, 60.0f);
        this.menuGroup.addActor(this.scroller);
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.scroller);
        this.addResolutionOptions();
    }

    private void addResolutionOptions() {
        Graphics.DisplayMode[] displayModes = Gdx.graphics.getDisplayModes(Gdx.graphics.getPrimaryMonitor());
        for (int i = 0; i < displayModes.length; ++i) {
            final int displayModeNum = i;
            TextButton.TextButtonStyle style = this.baseScreen.altTextBtnStyle;
            final ResolutionPopup resolutionPopup = this;
            if (i == SettingsLoader.getInstance().settingsData.DISPLAY_MODE) {
                style = this.baseScreen.textBtnStyle;
            }
            TextButton btnResolution = new TextButton(displayModes[i].width + "x" + displayModes[i].height + " - " + displayModes[i].refreshRate + "Hz", style);
            btnResolution.setChecked(true);
            btnResolution.getLabel().setFontScale(0.4f);
            btnResolution.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent e, float x, float y) {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayModes(Gdx.graphics.getPrimaryMonitor())[displayModeNum]);
                    ResolutionPopup.this.baseScreen.showMenu(new ConfirmSettingsPopup(ResolutionPopup.this.baseScreen, resolutionPopup));
                    ResolutionPopup.this.selectedDisplayMode = displayModeNum;
                }
            });
            this.table.add(btnResolution).height(70.0f).width(300.0f);
            this.table.row();
        }
    }

    public void revert() {
        Gdx.app.log("MewnBase", "Reverted display");
        if (this.prevFullScreen) {
            Gdx.graphics.setFullscreenMode(this.prevDisplayMode);
        } else {
            Gdx.graphics.setWindowedMode(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        }
    }

    public void confirm() {
        Gdx.app.log("MewnBase", "Confirmed new display setting: mode " + this.selectedDisplayMode);
        SettingsLoader.getInstance().settingsData.DISPLAY_MODE = this.selectedDisplayMode;
        SettingsLoader.getInstance().settingsData.DISPLAY_MODE_DETAILS = Gdx.graphics.getDisplayModes(Gdx.graphics.getPrimaryMonitor())[this.selectedDisplayMode].toString();
        SettingsLoader.getInstance().settingsData.FULL_SCREEN = Gdx.graphics.isFullscreen();
        SettingsLoader.getInstance().save();
    }
}

