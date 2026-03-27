/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
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
import com.cairn4.moonbase.KeyboardMapAdapter;
import com.cairn4.moonbase.SettingsData;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ControlMapPopup
extends Popup {
    private static String CONTROLS_WAITING = "?";
    private ScrollPane scroller;
    private Table keyTable;
    TextButton.TextButtonStyle keyButtonStyle;
    private TextButton btnDefaults;
    private TextButton btnAccept;
    private int waitingForIndex;
    private String waitingForFieldName;
    private modes mode;
    private SettingsData settingsDataCopy;
    private boolean savedChanges;
    ArrayList<TextButton> kmbList = new ArrayList();
    ArrayList<Label> keyLabelList = new ArrayList();

    public ControlMapPopup(BaseScreen baseScreen) {
        super(baseScreen);
        this.keyButtonStyle = new TextButton.TextButtonStyle();
        this.keyButtonStyle.font = this.font;
        this.keyButtonStyle.up = this.skin.getDrawable("btn-item");
        this.keyButtonStyle.over = this.skin.getDrawable("btn-item-active");
        this.keyButtonStyle.down = this.skin.getDrawable("btn-item-pressed");
        this.keyButtonStyle.disabled = this.skin.getDrawable("btn-item-disabled");
        this.settingsDataCopy = new SettingsData(SettingsLoader.getInstance().settingsData);
        this.setup();
        this.show();
    }

    private void setSavedChanges(boolean saved) {
        this.savedChanges = saved;
        this.btnAccept.setDisabled(saved);
        if (this.btnAccept.isDisabled()) {
            this.btnAccept.setTouchable(Touchable.disabled);
        } else {
            this.btnAccept.setTouchable(Touchable.enabled);
        }
    }

    private void setupKeyButtons() {
        this.keyTable.clear();
        this.kmbList.clear();
        this.keyLabelList.clear();
        this.createMappingRow("controls_moveLeft", this.settingsDataCopy.KEYS_LEFT, 0, "KEYS_LEFT");
        this.createMappingRow("controls_moveRight", this.settingsDataCopy.KEYS_RIGHT, 1, "KEYS_RIGHT");
        this.createMappingRow("controls_moveUp", this.settingsDataCopy.KEYS_UP, 2, "KEYS_UP");
        this.createMappingRow("controls_moveDown", this.settingsDataCopy.KEYS_DOWN, 3, "KEYS_DOWN");
        this.createMappingRow("controls_dropItem", this.settingsDataCopy.KEYS_DROP_ITEM, 4, "KEYS_DROP_ITEM");
        this.createMappingRow("controls_flashlight", this.settingsDataCopy.KEYS_FLASHLIGHT, 5, "KEYS_FLASHLIGHT");
        this.createMappingRow("controls_map", this.settingsDataCopy.KEYS_MAP, 6, "KEYS_MAP");
        this.createMappingRow("controls_techUpgrades", this.settingsDataCopy.KEYS_RESEARCH, 7, "KEYS_RESEARCH");
        this.createMappingRow("controls_pauseBack", this.settingsDataCopy.KEYS_MENU_BACK, 8, "KEYS_MENU_BACK");
        this.createMappingRow("controls_rotateTile", this.settingsDataCopy.KEYS_ROTATE_TILE, 9, "KEYS_ROTATE_TILE");
        this.createMappingRow("controls_enterVehicle", this.settingsDataCopy.KEYS_ENTER_VEHICLE, 10, "KEYS_ENTER_VEHICLE");
        this.createMappingRow("controls_vehicleDrift", this.settingsDataCopy.KEYS_VEHICLE_DRIFT, 11, "KEYS_VEHICLE_DRIFT");
        this.createMappingRow("controls_console", this.settingsDataCopy.KEYS_CONSOLE, 12, "KEYS_CONSOLE");
        this.createMappingRow("options_fullscreen", this.settingsDataCopy.KEYS_FULLSCREEN, 13, "KEYS_FULLSCREEN");
        this.setSavedChanges(true);
    }

    @Override
    public void setup() {
        super.setup();
        this.setSize(900.0f, 612.0f);
        this.setTitle("options_controls");
        Table bottomTable = new Table();
        bottomTable.setSize(this.panelBg.getWidth() - 150.0f, 75.0f);
        bottomTable.setPosition(75.0f, 59.0f, 12);
        this.popupGroup.addActor(bottomTable);
        this.btnDefaults = new TextButton(Localization.get("options_controls_defaults"), this.baseScreen.textBtnStyle);
        this.btnDefaults.setPosition(75.0f, 59.0f, 12);
        this.btnDefaults.getLabel().setFontScale(0.5f);
        this.btnDefaults.pad(0.0f, 30.0f, 0.0f, 30.0f);
        this.btnDefaults.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                ControlMapPopup.this.baseScreen.menuForwardSound();
                SettingsLoader.getInstance().createDefaults();
                ControlMapPopup.this.settingsDataCopy = new SettingsData(SettingsLoader.getInstance().settingsData);
                ControlMapPopup.this.setupKeyButtons();
                ControlMapPopup.this.setSavedChanges(false);
            }
        });
        bottomTable.add(this.btnDefaults).height(75.0f).space(100.0f).left().expandX().uniform();
        this.btnAccept = new TextButton(Localization.get("options_controls_accept"), this.baseScreen.textBtnStyle);
        this.btnAccept.setPosition(this.panelBg.getWidth() - 75.0f, 59.0f, 20);
        this.btnAccept.getLabel().setFontScale(0.5f);
        this.btnAccept.pad(0.0f, 30.0f, 0.0f, 30.0f);
        this.btnAccept.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                ControlMapPopup.this.save();
            }
        });
        bottomTable.add(this.btnAccept).height(75.0f).space(100.0f).right().expandX().uniform();
        this.keyTable = new Table();
        this.keyTable.top().center();
        this.setupKeyButtons();
        this.setupScroller();
        this.setMode(modes.normal, -1);
    }

    private void save() {
        System.out.println("Saving controller settings");
        SettingsLoader.getInstance().writeFile(this.settingsDataCopy);
        this.back();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    private void setupScroller() {
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.baseScreen.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.baseScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)this.keyTable, scrollStyle);
        this.scroller.setWidth(this.keyTable.getWidth());
        this.scroller.setHeight(this.keyTable.getHeight());
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setBounds(0.0f, 0.0f, this.panelBg.getWidth() - 150.0f, 320.0f);
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        this.scroller.setPosition(75.0f, 160.0f);
        this.popupGroup.addActor(this.scroller);
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.scroller);
    }

    private void createMappingRow(String locText, String key, int num, final String fieldName) {
        this.keyTable.row().colspan(2).space(10.0f);
        Image div = new Image(this.skin.getDrawable("rect"));
        div.setColor(Menu.MENU_COLOR);
        div.getColor().a = 0.1f;
        this.keyTable.add(div).fillX().height(3.0f).space(10.0f);
        this.keyTable.row();
        Label l = new Label((CharSequence)Localization.format(locText, new Object[0]), this.labelStyle);
        l.setAlignment(8, 8);
        l.setFontScale(0.4f);
        l.setPosition(0.0f, 0.0f);
        this.keyLabelList.add(l);
        this.keyTable.add(l).padLeft(20.0f).fillX().expandX().height(40.0f);
        TextButton kmb = new TextButton(key.toUpperCase(), this.keyButtonStyle);
        kmb.getLabel().setFontScale(0.4f);
        final int rowIndex = num;
        kmb.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.debug("MewnBase", "ControlMapPopup: Waiting for input: " + rowIndex);
                ControlMapPopup.this.waitingForFieldName = fieldName;
                ControlMapPopup.this.setMode(modes.waitingForInput, rowIndex);
            }
        });
        this.kmbList.add(kmb);
        this.keyTable.add(kmb).right().padRight(20.0f).expandX().width(130.0f).height(40.0f);
    }

    private void setMode(modes newMode, int rowIndex) {
        if (this.mode == modes.normal && newMode == modes.waitingForInput) {
            this.mode = modes.waitingForInput;
            this.setWaiting();
            this.waitingForIndex = rowIndex;
            this.keyLabelList.get(rowIndex).setColor(Color.RED);
            this.kmbList.get(rowIndex).getLabel().setColor(Color.RED);
            this.kmbList.get(rowIndex).getLabel().setText(CONTROLS_WAITING);
        } else {
            this.mode = modes.normal;
            this.setInputFocus();
            for (Label l : this.keyLabelList) {
                l.setColor(Color.WHITE);
            }
            for (TextButton kmb : this.kmbList) {
                kmb.getLabel().setColor(Color.WHITE);
            }
        }
    }

    public void setWaiting() {
        InputMultiplexer inputMulti = new InputMultiplexer();
        KeyboardMapAdapter input = new KeyboardMapAdapter(this);
        inputMulti.addProcessor(input);
        inputMulti.addProcessor(this.stage);
        Gdx.input.setInputProcessor(inputMulti);
        if (this.baseScreen.game.console != null) {
            this.baseScreen.game.console.resetInputProcessing();
        }
    }

    public void catchKey(String s) {
        Field f = null;
        try {
            f = this.settingsDataCopy.getClass().getField(this.waitingForFieldName);
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        if (f != null) {
            try {
                f.set(this.settingsDataCopy, s);
                Gdx.app.log("MewnBase", "ControlMapPopup: Value for " + f.getName() + " set to " + s);
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            this.setMode(modes.normal, -1);
            this.kmbList.get(this.waitingForIndex).getLabel().setText(s);
            this.setSavedChanges(false);
        }
    }

    @Override
    public void back() {
        this.baseScreen.menuBackSound();
        super.back();
    }

    private static enum modes {
        normal,
        waitingForInput;

    }
}

