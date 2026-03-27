/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.LoadingErrors;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class ErrorLoadingPopup
extends Popup {
    private Image gradient;
    private Table table;
    private TextButton btnOpenFolder;
    private Label desc;
    private Table scrollTable;
    private ScrollPane scroller;

    public ErrorLoadingPopup(BaseScreen baseScreen, LoadingErrors error) {
        super(baseScreen);
        this.setup();
        this.addScrollContent();
        this.show();
    }

    @Override
    public void setup() {
        super.setup();
        this.setSize(700.0f, 550.0f);
        this.popupGroup.moveBy(0.0f, -5.0f);
        this.setTitle("error");
        this.table = new Table();
        this.table.align(1);
        this.table.center();
        this.table.setFillParent(true);
        this.table.pad(130.0f, 65.0f, 60.0f, 65.0f);
        this.popupGroup.addActor(this.table);
        this.scrollTable = new Table();
        this.scrollTable.top();
        this.setupScroller();
        this.table.add(this.scroller).expand().fill().padBottom(20.0f);
        this.table.row();
        this.btnOpenFolder = new TextButton(Localization.get("openSavesFolder"), this.baseScreen.textBtnStyle);
        this.btnOpenFolder.getLabel().setFontScale(0.4f);
        this.table.add(this.btnOpenFolder).height(70.0f).width(400.0f).expandX().padLeft(10.0f).padRight(10.0f).uniformX();
        this.btnOpenFolder.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                ErrorLoadingPopup.this.openGameFolder();
            }
        });
    }

    @Override
    protected void setupTintBg() {
        super.setupTintBg();
        this.gradient = new Image(new Texture(Gdx.files.internal("circle-gradient.png")));
        this.gradient.setColor(Color.valueOf("551100"));
        this.gradient.setSize(1800.0f, 1600.0f);
        this.gradient.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2, 1);
        this.gradient.setTouchable(Touchable.disabled);
        this.menuGroup.addActor(this.gradient);
    }

    private void setupScroller() {
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.baseScreen.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.baseScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)this.scrollTable, scrollStyle);
        this.scroller.setWidth(this.scrollTable.getWidth());
        this.scroller.setHeight(this.scrollTable.getHeight());
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.scroller);
    }

    private void addScrollContent() {
        this.scrollTable.row();
        String errorMessage = "Sorry. There was an error while trying to load your game.\n\nIt might be too old to load, or a bug preventing it from loading.\n\nIf you want, zip up your saves folder along with any crashlog files and email it over to [" + Menu.MENU_COLOR_HEX + "]support@cairn4.com[] so we can try and figure out what went wrong. :(";
        this.desc = new Label((CharSequence)errorMessage, this.labelStyle);
        this.desc.setFontScale(0.4f);
        this.desc.setAlignment(10);
        this.desc.setColor(1.0f, 1.0f, 1.0f, 0.9f);
        this.desc.setWrap(true);
        this.scrollTable.add(this.desc).fill().expand().padLeft(10.0f).padRight(10.0f).padBottom(20.0f).top();
        this.scroller.setWidth(this.scrollTable.getWidth());
        this.scroller.setHeight(this.scrollTable.getHeight());
        this.scrollTable.layout();
    }

    private void openGameFolder() {
        String path = Gdx.files.local("saves").path();
        Desktop desktop = Desktop.getDesktop();
        File dirToOpen = null;
        System.out.println(path);
        try {
            dirToOpen = new File(path);
            try {
                desktop.open(dirToOpen);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        catch (IllegalArgumentException iae) {
            Gdx.app.error("MewnBase", "Path not found: " + path);
        }
    }

    @Override
    public void back() {
        this.gradient.remove();
        super.back();
    }
}

