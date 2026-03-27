/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.ui.AutoScaleTextButton;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;
import java.text.DecimalFormat;
import java.util.MissingResourceException;

public class LanguagePopup
extends Popup {
    ScrollPane scroller;
    Table table;
    Table detailsTable;
    Label lang;
    Label info;
    Label percentComplete;
    TextButton btnSelect;
    TextButton btnHelp;
    String selectedLanguage = "";
    private DecimalFormat df_tenth;

    public LanguagePopup(BaseScreen baseScreen) {
        super(baseScreen);
        this.setup();
        this.show();
    }

    @Override
    public void setup() {
        super.setup();
        this.tintBgAlpha = 0.8f;
        this.df_tenth = new DecimalFormat("0.0");
        this.setSize(900.0f, 650.0f);
        this.setTitle("options_language");
        this.table = new Table();
        this.table.bottom().left();
        this.popupGroup.addActor(this.table);
        this.table.setSize(this.panelBg.getWidth() / 2.0f - 100.0f, this.panelBg.getHeight() - 275.0f);
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
        this.scroller.setBounds(0.0f, 0.0f, this.panelBg.getWidth() / 2.0f - 100.0f, this.panelBg.getHeight() - 195.0f);
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        this.scroller.setPosition(75.0f, 60.0f);
        this.popupGroup.addActor(this.scroller);
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.scroller);
        this.detailsTable = new Table();
        this.detailsTable.bottom().left();
        this.detailsTable.setSize(this.panelBg.getWidth() / 2.0f - 80.0f, this.panelBg.getHeight() - 195.0f);
        this.detailsTable.setPosition(this.scroller.getX(20) + 30.0f, 60.0f);
        this.popupGroup.addActor(this.detailsTable);
        this.addListOptions();
        this.addDetailsTable();
    }

    private void addDetailsTable() {
        this.lang = new Label((CharSequence)"French", this.labelStyle);
        this.lang.setAlignment(8);
        this.lang.setFontScale(0.55f);
        this.lang.setColor(Color.WHITE);
        this.detailsTable.add(this.lang).expandX().fill().height(70.0f).padLeft(5.0f);
        this.detailsTable.row();
        this.info = new Label((CharSequence)Localization.get("language_community"), this.labelStyle);
        this.info.setFontScale(0.4f);
        this.info.setAlignment(10);
        this.info.setColor(MENU_COLOR);
        this.info.getColor().a = 0.65f;
        this.info.setWrap(true);
        this.detailsTable.add(this.info).top().expand().fill().space(10.0f).padLeft(5.0f);
        this.detailsTable.row();
        this.percentComplete = new Label((CharSequence)"--", this.baseScreen.labelStyle);
        this.percentComplete.setFontScale(0.4f);
        this.percentComplete.setColor(MENU_COLOR);
        this.percentComplete.getColor().a = 0.45f;
        this.detailsTable.add(this.percentComplete).top().expand().fill().space(10.0f).padLeft(5.0f);
        this.detailsTable.row();
        this.btnSelect = new TextButton(Localization.get("options_apply"), this.baseScreen.textBtnStyle);
        this.btnSelect.getLabel().setFontScale(0.5f);
        this.btnSelect.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                LanguagePopup.this.baseScreen.menuForwardSound();
                LanguagePopup.this.changeLanguage();
            }
        });
        this.detailsTable.add(this.btnSelect).height(80.0f).spaceTop(20.0f).left().fillX();
        this.detailsTable.row();
        Label restartRequired = new Label((CharSequence)Localization.get("options_restartRequired"), this.labelStyle);
        restartRequired.setFontScale(0.4f);
        restartRequired.setAlignment(1);
        restartRequired.setColor(MENU_COLOR);
        restartRequired.getColor().a = 0.65f;
        restartRequired.setWrap(true);
        this.detailsTable.add(restartRequired).top().expandX().fill().space(5.0f).padLeft(5.0f);
        this.detailsTable.row();
        AutoScaleTextButton.AutoScaleTextButtonStyle astbStyle = new AutoScaleTextButton.AutoScaleTextButtonStyle(this.baseScreen.altTextBtnStyle);
        astbStyle.horzPadding = 30.0f;
        astbStyle.minScale = 0.1f;
        astbStyle.maxScale = 0.4f;
        this.btnHelp = new TextButton(Localization.format("language_help", Localization.get("game")), this.baseScreen.altTextBtnStyle);
        this.btnHelp.getLabel().setFontScale(0.33f);
        this.btnHelp.getLabel().setWrap(true);
        this.btnHelp.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                LanguagePopup.this.baseScreen.menuForwardSound();
                Gdx.net.openURI("https://crowdin.com/project/mewnbase/invite");
            }
        });
        this.detailsTable.add(this.btnHelp).height(80.0f).space(20.0f).bottom().expandY().fillX();
        this.showLanguageDetails(SettingsLoader.getInstance().settingsData.LANGUAGE);
    }

    private void addListOptions() {
        for (int i = 0; i < Localization.supportedLocales.length; ++i) {
            TextButton.TextButtonStyle style = this.baseScreen.altTextBtnStyle;
            if (Localization.supportedLocales[i].equals(SettingsLoader.getInstance().settingsData.LANGUAGE)) {
                style = this.baseScreen.textBtnStyle;
            }
            final String locale = Localization.supportedLocales[i];
            TextButton btnLanguage = new TextButton(Localization.getLangName("language_" + Localization.supportedLocales[i]), style);
            btnLanguage.padLeft(35.0f);
            btnLanguage.getLabel().setAlignment(8);
            btnLanguage.setChecked(true);
            btnLanguage.getLabel().setFontScale(0.4f);
            btnLanguage.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent e, float x, float y) {
                    LanguagePopup.this.baseScreen.menuForwardSound();
                    LanguagePopup.this.showLanguageDetails(locale);
                }
            });
            this.table.add(btnLanguage).height(70.0f).width(300.0f);
            this.table.row();
        }
    }

    private void changeLanguage() {
        Localization.changeLanguage(this.selectedLanguage);
        SettingsLoader.getInstance().settingsData.LANGUAGE = this.selectedLanguage;
        SettingsLoader.getInstance().writeFile(SettingsLoader.getInstance().settingsData);
        this.table.clearChildren();
        this.detailsTable.clearChildren();
        this.addListOptions();
        this.addDetailsTable();
    }

    private void showLanguageDetails(String locale) {
        this.selectedLanguage = locale;
        try {
            this.lang.setText(Localization.getLangName("language_" + locale));
        }
        catch (MissingResourceException e) {
            this.lang.setText("Error");
        }
        if (locale.equals("en")) {
            this.info.setText(Localization.get("language_original"));
            this.percentComplete.setText("--");
        } else {
            this.info.setText(Localization.get("language_community"));
            String percentString = "--";
            if (Localization.translationProgress.containsKey(locale)) {
                float percent = Localization.translationProgress.get(locale).floatValue();
                String percentText = this.df_tenth.format(percent *= 100.0f) + "%";
                percentString = Localization.format("language_complete", percentText);
            }
            this.percentComplete.setText(percentString);
        }
        if (locale.equals(SettingsLoader.getInstance().settingsData.LANGUAGE)) {
            this.btnSelect.setDisabled(true);
            this.btnSelect.setTouchable(Touchable.disabled);
        } else {
            this.btnSelect.setDisabled(false);
            this.btnSelect.setTouchable(Touchable.enabled);
        }
    }

    @Override
    public void back() {
        this.baseScreen.menuBackSound();
        super.back();
    }
}

