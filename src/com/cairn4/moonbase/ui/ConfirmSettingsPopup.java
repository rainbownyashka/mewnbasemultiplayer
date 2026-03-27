/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;
import com.cairn4.moonbase.ui.ResolutionPopup;
import java.text.DecimalFormat;

public class ConfirmSettingsPopup
extends Popup {
    private float revertDelay = 12.0f;
    private float revertTimer;
    private Label dialog;
    DecimalFormat df;
    private ResolutionPopup resolutionPopup;

    public ConfirmSettingsPopup(BaseScreen baseScreen, ResolutionPopup resolutionPopup) {
        super(baseScreen);
        this.resolutionPopup = resolutionPopup;
        this.setup();
        this.show();
    }

    @Override
    public void setup() {
        super.setup();
        this.tintBgAlpha = 0.8f;
        this.setSize(700.0f, 500.0f);
        this.setTitle("options_confirm");
        this.df = new DecimalFormat("#");
        this.dialog = new Label((CharSequence)Localization.format("options_confirmDialog", Float.valueOf(this.revertDelay)), this.baseScreen.labelStyle);
        this.dialog.setFontScale(0.45f);
        this.dialog.setAlignment(1);
        this.dialog.setWrap(true);
        this.dialog.setWidth(500.0f);
        this.dialog.setPosition(this.panelBg.getWidth() / 2.0f, this.panelBg.getHeight() / 2.0f, 1);
        this.popupGroup.addActor(this.dialog);
        TextButton btnCancel = new TextButton(Localization.get("options_displayRevert"), this.baseScreen.textBtnStyle);
        btnCancel.setSize(204.0f, 75.0f);
        btnCancel.setPosition(75.0f, 59.0f, 12);
        btnCancel.getLabel().setFontScale(0.5f);
        this.popupGroup.addActor(btnCancel);
        btnCancel.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                ConfirmSettingsPopup.this.revert();
            }
        });
        TextButton btnAccept = new TextButton(Localization.get("options_displayAccept"), this.baseScreen.textBtnStyle);
        btnAccept.setSize(204.0f, 75.0f);
        btnAccept.setPosition(this.panelBg.getWidth() - 75.0f, 59.0f, 20);
        btnAccept.getLabel().setFontScale(0.5f);
        this.popupGroup.addActor(btnAccept);
        btnAccept.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                ConfirmSettingsPopup.this.confirm();
            }
        });
    }

    @Override
    public void update(float delta) {
        this.revertTimer += delta;
        if (this.revertTimer > this.revertDelay) {
            this.revert();
        }
        float timeLeft = this.revertDelay - this.revertTimer;
        this.dialog.setText(Localization.format("options_confirmDialog", this.df.format(timeLeft)));
    }

    private void confirm() {
        this.resolutionPopup.confirm();
        this.back();
    }

    private void revert() {
        this.resolutionPopup.revert();
        this.back();
    }
}

