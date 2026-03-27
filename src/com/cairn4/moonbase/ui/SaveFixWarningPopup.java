/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.SaveFixer;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;
import java.util.ArrayList;

public class SaveFixWarningPopup
extends Popup {
    private Label dialog;
    ArrayList<String> saveFilesThatMightBeFixable;

    public SaveFixWarningPopup(BaseScreen baseScreen, ArrayList<String> saveFilesThatMightBeFixable) {
        super(baseScreen);
        MoonBase.log("init SaveFixWarningPopup");
        this.saveFilesThatMightBeFixable = saveFilesThatMightBeFixable;
        this.setup();
        this.show();
    }

    @Override
    public void setup() {
        super.setup();
        this.tintBgAlpha = 0.8f;
        this.setSize(900.0f, 650.0f);
        this.setTitle("saveFixer_popup_title");
        this.tintBg.clearListeners();
        String msg = "";
        msg = msg + "[" + MENU_COLOR_HEX + "]" + Localization.format("saveFixerSaveFilesWithIssues", this.saveFilesThatMightBeFixable.size()) + "[]\n";
        for (String s : this.saveFilesThatMightBeFixable) {
            msg = msg + "     " + s + "\n";
        }
        msg = msg + "\n";
        msg = msg + Localization.get("saveFixerEntityFix");
        this.dialog = new Label((CharSequence)msg, this.baseScreen.labelStyle);
        this.dialog.setFontScale(0.35f);
        this.dialog.setAlignment(10);
        this.dialog.setWrap(true);
        this.dialog.setWidth(this.panelBg.getWidth() - 200.0f);
        this.dialog.setHeight(this.panelBg.getHeight() - 280.0f);
        this.dialog.setPosition(this.panelBg.getWidth() / 2.0f, 150.0f, 4);
        this.popupGroup.addActor(this.dialog);
        Table t = new Table();
        t.setPosition(this.panelBg.getWidth() / 2.0f, 60.0f, 4);
        t.bottom();
        this.popupGroup.addActor(t);
        TextButton btnCancel = new TextButton(Localization.get("saveFixerNotNow"), this.baseScreen.textBtnStyle);
        btnCancel.getLabel().setFontScale(0.5f);
        btnCancel.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                SaveFixWarningPopup.this.back();
            }
        });
        t.add(btnCancel).size(300.0f, 75.0f).padRight(20.0f);
        TextButton btnAccept = new TextButton(Localization.get("saveFixerUpdates"), this.baseScreen.textBtnStyle);
        btnAccept.getLabel().setFontScale(0.5f);
        btnAccept.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                SaveFixWarningPopup.this.fixSaves();
            }
        });
        t.add(btnAccept).size(300.0f, 75.0f).padLeft(20.0f);
    }

    private void fixSaves() {
        SaveFixer.entityNameFix(this.saveFilesThatMightBeFixable);
        this.back();
    }
}

