/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SerializationException;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.UpdateCheck;
import com.cairn4.moonbase.WhatsNewData;
import com.cairn4.moonbase.WhatsNewSection;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;

public class WhatsNewPopup
extends Popup {
    private static final int POPUP_HEIGHT = 690;
    private static final String whatsNewPath = "whats_new.json";
    private static final String devblogURL = "https://cairn4.itch.io/mewnbase/devlog/";
    private WhatsNewData data;
    String updateNotes = Localization.get("loadingSimple");
    private Image gradient;
    private Table table;
    private TextButton btnClose;
    private TextButton btnMore;
    private Label heading;
    private Label versionHeadingLabel;
    private Label desc;
    private Table iconTable;
    private Table scrollTable;
    private ScrollPane scroller;
    TextureRegion downloadTest;

    public WhatsNewPopup(BaseScreen baseScreen) {
        super(baseScreen);
        this.getJson();
        this.setup();
        this.show();
        if (UpdateCheck.SHOW_WHATSNEW) {
            SettingsLoader.getInstance().settingsData.LAST_WHATSNEW_VERSION = "1.0.1";
            SettingsLoader.getInstance().save();
            UpdateCheck.SHOW_WHATSNEW = false;
            Gdx.app.log("MewnBase", "Saving LAST_WHATSNEW_VERSION to " + SettingsLoader.getInstance().settingsData.LAST_WHATSNEW_VERSION);
        }
    }

    private void getJson() {
        try {
            FileHandle json = Gdx.files.local(MoonBase.coreFolder + whatsNewPath);
            String jsonString = json.readString();
            Gdx.app.postRunnable(() -> this.parseData(jsonString));
        }
        catch (GdxRuntimeException e) {
            this.data = new WhatsNewData();
            this.data.txt = "ERROR MESSAGE";
            this.data.title = "ERROR TITLE";
        }
    }

    private void parseData(String result) {
        Json json = new Json();
        try {
            this.data = json.fromJson(WhatsNewData.class, result);
        }
        catch (SerializationException e) {
            Gdx.app.error("MewnBase", "" + e.getLocalizedMessage());
            this.data = new WhatsNewData();
            this.data.title = "Uh oh!";
            this.data.txt = "Problem loading the What's New info.";
        }
        this.addScrollContent();
    }

    @Override
    protected void setupTintBg() {
        super.setupTintBg();
        this.gradient = new Image(new Texture(Gdx.files.internal("circle-gradient.png")));
        this.gradient.setColor(Color.valueOf("002c53"));
        this.gradient.setSize(1800.0f, 1600.0f);
        this.gradient.setPosition(MoonBase.SCREEN_WIDTH / 2, MoonBase.SCREEN_HEIGHT / 2, 1);
        this.gradient.setTouchable(Touchable.disabled);
        this.menuGroup.addActor(this.gradient);
    }

    @Override
    public void setup() {
        super.setup();
        this.setSize(860.0f, 690.0f);
        this.setTitle("whatsnew");
        this.table = new Table();
        this.table.align(1);
        this.table.center();
        this.table.setFillParent(true);
        this.table.pad(130.0f, 65.0f, 60.0f, 65.0f);
        this.popupGroup.addActor(this.table);
        this.scrollTable = new Table();
        this.scrollTable.padRight(10.0f);
        this.scrollTable.top();
        this.setupScroller();
        this.table.add(this.scroller).expand().fill().padBottom(20.0f);
        this.table.row();
        this.btnMore = new TextButton(Localization.get("devblog"), this.baseScreen.textBtnStyle);
        this.btnMore.getLabel().setFontScale(0.5f);
        this.table.add(this.btnMore).height(70.0f).width(300.0f).expandX().padLeft(10.0f).padRight(10.0f).uniformX();
        this.btnMore.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                Gdx.net.openURI(WhatsNewPopup.devblogURL);
            }
        });
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

    @Override
    public void back() {
        this.gradient.remove();
        super.back();
    }

    private void addScrollContent() {
        if (!this.data.title.equals("")) {
            this.versionHeadingLabel = new Label((CharSequence)this.data.title, this.labelStyle);
            this.versionHeadingLabel.setAlignment(10);
            this.versionHeadingLabel.setFontScale(0.5f);
            this.versionHeadingLabel.setColor(Menu.MENU_COLOR);
            this.versionHeadingLabel.setWrap(true);
            this.scrollTable.add(this.versionHeadingLabel).fillX().padLeft(10.0f).padRight(10.0f).padBottom(10.0f);
            this.scrollTable.row();
        }
        if (this.data.image != null && this.data.image != "") {
            this.addImage();
        }
        if (this.data.txt != null) {
            this.desc = new Label((CharSequence)this.data.txt, this.labelStyle);
            this.desc.setFontScale(0.3f);
            this.desc.setAlignment(10);
            this.desc.setColor(1.0f, 1.0f, 1.0f, 0.8f);
            this.desc.setWrap(true);
            this.scrollTable.add(this.desc).fill().expand().padLeft(10.0f).padRight(10.0f).padBottom(20.0f).top();
            this.scrollTable.row();
        }
        for (WhatsNewSection section : this.data.sections) {
            if (section.header != null) {
                Label header = new Label((CharSequence)section.header, this.labelStyle);
                header.setFontScale(0.45f);
                header.setAlignment(10);
                if (section.centerHeader) {
                    header.setAlignment(2);
                    header.setAlignment(2, 1);
                }
                header.setColor(Menu.MENU_COLOR);
                header.setWrap(true);
                this.scrollTable.add(header).fillX().padLeft(10.0f).padRight(10.0f).padBottom(10.0f);
                this.scrollTable.row();
            }
            if (section.txt != null) {
                Label t = new Label((CharSequence)section.txt, this.labelStyle);
                t.setFontScale(0.3f);
                t.setAlignment(10);
                t.setColor(1.0f, 1.0f, 1.0f, 0.75f);
                t.setWrap(true);
                this.scrollTable.add(t).fill().expand().padLeft(10.0f).padRight(10.0f).padBottom(20.0f).top();
                this.scrollTable.row();
            }
            if (section.list == null || section.list.size() <= 0) continue;
            Table listTable = new Table();
            for (String s : section.list) {
                listTable.add(this.addBullet()).left().top().padLeft(10.0f).padRight(8.0f);
                listTable.add(this.addBulletItem(s)).left().top().expand().fill().spaceBottom(6.0f);
                listTable.row();
            }
            listTable.layout();
            this.scrollTable.add(listTable).fill().expand().padLeft(10.0f).padRight(10.0f).padBottom(30.0f).top();
            this.scrollTable.row();
        }
        this.scroller.setWidth(this.scrollTable.getWidth());
        this.scroller.setHeight(this.scrollTable.getHeight());
        this.scrollTable.layout();
    }

    private Label addBullet() {
        Label b = new Label((CharSequence)"\u2022", this.labelStyle);
        b.setFontScale(0.3f);
        b.setAlignment(10);
        b.setColor(1.0f, 1.0f, 1.0f, 0.5f);
        return b;
    }

    private Label addBulletItem(String s) {
        Label txt = new Label((CharSequence)s, this.labelStyle);
        txt.setFontScale(0.3f);
        txt.setAlignment(10);
        txt.setColor(1.0f, 1.0f, 1.0f, 0.75f);
        txt.setWrap(true);
        return txt;
    }

    private void addImage() {
        try {
            Texture tex = new Texture(Gdx.files.local(MoonBase.coreFolder + this.data.image));
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Image img = new Image(tex);
            img.setScaling(Scaling.fit);
            int imageHeight = 200;
            if (this.data.imageHeight != 0) {
                imageHeight = this.data.imageHeight;
            }
            this.scrollTable.add(img).center().padBottom(15.0f).height(imageHeight).padRight(10.0f).padBottom(20.0f);
            this.scrollTable.row();
        }
        catch (GdxRuntimeException e) {
            Gdx.app.error("MewnBase", e.getLocalizedMessage());
            Label errorLabel = new Label((CharSequence)"[Missing Image]", this.labelStyle);
            errorLabel.setFontScale(0.4f);
            errorLabel.setAlignment(1, 1);
            errorLabel.setColor(Color.RED);
            this.scrollTable.add(errorLabel).fillX().expandX().padLeft(10.0f).padRight(10.0f).padBottom(20.0f).top();
            this.scrollTable.row();
        }
    }
}

