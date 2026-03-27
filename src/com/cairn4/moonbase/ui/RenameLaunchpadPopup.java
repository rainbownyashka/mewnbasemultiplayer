/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.LaunchPadMenu;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;

public class RenameLaunchpadPopup
extends Popup {
    LaunchPadMenu launchPadMenu;
    TextButton btnCancel;
    TextButton btnSet;
    private BitmapFont nameFieldFont;
    private TextField nameField;
    private Label.LabelStyle nameFieldLabelStyle;
    private Label nameFieldLabel;

    public RenameLaunchpadPopup(LaunchPadMenu launchpadMenu, BaseScreen baseScreen) {
        super(baseScreen);
        this.launchPadMenu = launchpadMenu;
        this.setup();
        this.setSize(700.0f, 400.0f);
        this.show();
    }

    @Override
    public void setup() {
        super.setup();
        this.setTitle("rename_launchpad");
        Table popupTable = new Table();
        popupTable.setFillParent(true);
        popupTable.pad(75.0f).padTop(135.0f);
        popupTable.center();
        this.popupGroup.addActor(popupTable);
        this.nameFieldFont = AssetManagerSingleton.getInstance().get("smallfont1.fnt", BitmapFont.class);
        this.nameFieldFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        String cs = "0123456789";
        this.nameFieldFont.setFixedWidthGlyphs(cs);
        this.nameFieldFont.getData().markupEnabled = true;
        this.nameFieldFont.getData().setScale(0.4f, 0.4f);
        this.nameFieldLabelStyle = new Label.LabelStyle(this.nameFieldFont, Color.WHITE);
        NinePatch alt_np_up = new NinePatch(this.skin.getRegion("btn-minor"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_up = new NinePatchDrawable(alt_np_up);
        NinePatch alt_np_active = new NinePatch(this.skin.getRegion("btn-minor-pressed"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_active = new NinePatchDrawable(alt_np_active);
        NinePatch selected_np = new NinePatch(this.skin.getRegion("slider-bg"), 5, 5, 5, 5);
        NinePatchDrawable selected_npd = new NinePatchDrawable(selected_np);
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.background = alt_npd_up;
        textFieldStyle.focusedBackground = alt_npd_active;
        textFieldStyle.font = this.nameFieldFont;
        textFieldStyle.font.getData().setScale(0.45f);
        textFieldStyle.cursor = this.skin.getDrawable("hud-progress-knob");
        textFieldStyle.selection = selected_npd;
        textFieldStyle.fontColor = Menu.MENU_COLOR;
        textFieldStyle.focusedFontColor = Color.WHITE;
        textFieldStyle.messageFontColor = new Color(Menu.MENU_COLOR).sub(0.0f, 0.0f, 0.0f, 0.7f);
        textFieldStyle.messageFont = this.nameFieldFont;
        this.nameField = new TextField("", textFieldStyle);
        this.nameField.setMaxLength(18);
        this.nameField.setText(this.launchPadMenu.launchPad.getName());
        this.nameField.setAlignment(1);
        this.nameField.setTextFieldListener(new TextField.TextFieldListener(){

            @Override
            public void keyTyped(TextField textField, char c) {
            }
        });
        popupTable.add(this.nameField).height(70.0f).width(500.0f).spaceBottom(30.0f);
        popupTable.row();
        Table botRowTable = new Table();
        popupTable.add(botRowTable).height(80.0f).bottom().fillX().expandX();
        this.btnCancel = new TextButton(Localization.get("cancel"), this.baseScreen.textBtnStyle);
        this.btnCancel.getLabel().setFontScale(0.6f);
        this.btnCancel.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                RenameLaunchpadPopup.this.back();
            }
        });
        botRowTable.add(this.btnCancel).left().width(220.0f).height(80.0f).expandX();
        this.btnSet = new TextButton(Localization.get("common_ok"), this.baseScreen.textBtnStyle);
        this.btnSet.getLabel().setFontScale(0.6f);
        this.btnSet.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                RenameLaunchpadPopup.this.rename();
            }
        });
        botRowTable.add(this.btnSet).width(220.0f).height(90.0f).right().expandX();
    }

    private void rename() {
        this.nameField.clearListeners();
        MoonBase.log("Renaming launchpad to : " + this.nameField.getText());
        this.launchPadMenu.launchPad.setName(this.nameField.getText());
        this.font.getData().setScale(1.0f);
        this.back();
    }
}

