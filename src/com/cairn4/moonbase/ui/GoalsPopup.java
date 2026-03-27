/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;

public class GoalsPopup
extends Popup {
    private Table goalTable;

    public GoalsPopup(BaseScreen baseScreen) {
        super(baseScreen);
        this.setup();
        this.show();
    }

    @Override
    public void setup() {
        super.setup();
        this.setSize(700.0f, 550.0f);
        this.titleLabel.setText(Localization.get("missionHeading"));
        this.goalTable = new Table();
        this.goalTable.align(1);
        this.goalTable.center();
        this.goalTable.setFillParent(true);
        this.goalTable.pad(220.0f, 30.0f, 150.0f, 30.0f);
        this.popupGroup.addActor(this.goalTable);
    }
}

