/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class CheckboxRow extends Table {
    Menu menu;
    public Table table;
    String text;
    boolean checked = false;
    private Image box;
    public Label label;

    public CheckboxRow(Menu menu, String text, boolean initialState) {
        this.menu = menu;
        this.text = text;
        this.checked = initialState;
        this.table = this;
        this.createButton();
        this.setChecked(this.checked);
        this.validate();
    }

    protected void createButton() {
        this.clearChildren();
        this.defaults().left();
        this.label = new Label((CharSequence)this.text, this.menu.labelStyle);
        this.label.setFontScale(0.4f);
        this.label.setAlignment(8);
        this.add(this.label).fill().expandX().space(5.0f).left();
        this.box = new Image(this.menu.skin.getDrawable("checkbox-off"));
        this.add(this.box).minWidth(55.0f);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if (this.box == null) {
            return;
        }
        if (this.checked) {
            this.box.setDrawable(this.menu.skin.getDrawable("checkbox-on"));
        } else {
            this.box.setDrawable(this.menu.skin.getDrawable("checkbox-off"));
        }
    }

    @Override
    public void setScale(float scale) {
        super.setScale(scale);
    }
}
