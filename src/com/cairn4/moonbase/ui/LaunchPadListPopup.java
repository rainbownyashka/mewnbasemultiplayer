/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.WorldDebugMap;
import com.cairn4.moonbase.tiles.LaunchPad;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.LaunchPadMenu;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;
import java.util.ArrayList;

public class LaunchPadListPopup
extends Popup
implements Telegraph {
    private GameScreen gameScreen;
    private Table table;
    private ScrollPane scroller;
    private Button.ButtonStyle buttonStyle;
    private LaunchPad localLaunchPad;
    NinePatchDrawable panelDrawable;
    LaunchPadMenu launchPadMenu;
    private ArrayList<Texture> disposableTextures = new ArrayList();
    private Button.ButtonStyle deleteButtonStyle;

    public LaunchPadListPopup(GameScreen gameScreen, LaunchPad launchPad, LaunchPadMenu launchPadMenu) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        this.localLaunchPad = launchPad;
        this.launchPadMenu = launchPadMenu;
        this.setup();
        this.setSize(860.0f, 600.0f);
        this.show();
        MessageManager.getInstance().addListener(this, 42);
    }

    @Override
    protected void setup() {
        NinePatchDrawable npd_up;
        super.setup();
        this.setTitle("selectDestination");
        NinePatch np_up = new NinePatch(this.skin.getRegion("btn-item"), 16, 16, 16, 16);
        this.panelDrawable = npd_up = new NinePatchDrawable(np_up);
        NinePatch np_active = new NinePatch(this.skin.getRegion("btn-item-active"), 16, 16, 16, 16);
        NinePatchDrawable npd_active = new NinePatchDrawable(np_active);
        NinePatch np_down = new NinePatch(this.skin.getRegion("btn-item-pressed"), 16, 16, 16, 16);
        NinePatchDrawable npd_down = new NinePatchDrawable(np_down);
        this.buttonStyle = new Button.ButtonStyle();
        this.buttonStyle.up = npd_up;
        this.buttonStyle.over = npd_active;
        this.buttonStyle.down = npd_down;
        this.deleteButtonStyle = new Button.ButtonStyle();
        this.deleteButtonStyle.up = this.skin.getDrawable("clear-launchpad");
        this.deleteButtonStyle.over = this.skin.getDrawable("clear-launchpad-hover");
        this.deleteButtonStyle.down = this.skin.getDrawable("clear-launchpad-pressed");
        this.setupTable();
        this.addListOptions();
        this.addEndGameDestination();
    }

    private void setupTable() {
        this.table = new Table();
        this.table.padRight(15.0f);
        this.table.top().left();
        this.popupGroup.addActor(this.table);
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)this.table, scrollStyle);
        this.scroller.setWidth(this.table.getWidth());
        this.scroller.setHeight(this.table.getHeight());
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setBounds(0.0f, 0.0f, this.panelBg.getWidth() - 80.0f, this.panelBg.getHeight() - 195.0f);
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        this.scroller.setPosition(80.0f, 60.0f);
        this.popupGroup.addActor(this.scroller);
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.scroller);
    }

    private void addListOptions() {
        for (LaunchPad lpad : this.gameScreen.world.baseManager.getLaunchPads()) {
            if (lpad == this.localLaunchPad) continue;
            final LaunchPad destinationPad = lpad;
            Table rowTable = new Table();
            rowTable.setBackground(this.panelDrawable);
            Group head = new Group();
            rowTable.add(head).width(130.0f).fillY().height(100.0f).padRight(20.0f).top();
            Image preview = this.addMapScreenshot(lpad);
            preview.setSize(130.0f, 100.0f);
            head.addActor(preview);
            Table infoTable = new Table();
            infoTable.left();
            rowTable.add(infoTable).expandX().fill().left().minWidth(300.0f);
            String lpadName = lpad.getName();
            if (lpadName.length() > 10) {
                lpadName = lpadName.substring(0, 10) + "...";
            }
            Label nameLabel = new Label((CharSequence)Localization.format("launchpad_name", lpadName), this.labelStyle);
            nameLabel.setFontScale(0.5f);
            nameLabel.setAlignment(8);
            nameLabel.setTouchable(Touchable.disabled);
            infoTable.add(nameLabel).fillX();
            infoTable.row();
            Label txtDistance = new Label((CharSequence)Localization.format("launchpad_distance", this.getDistance(lpad)), this.labelStyle);
            txtDistance.setFontScale(0.32f);
            txtDistance.setColor(MENU_COLOR);
            txtDistance.getColor().a = 0.5f;
            txtDistance.setAlignment(8);
            txtDistance.setTouchable(Touchable.disabled);
            infoTable.add(txtDistance).fillX();
            infoTable.row();
            if (lpad.hasRocket) {
                Label txtStatus = new Label((CharSequence)Localization.get("launchpad_occupied"), this.labelStyle);
                txtStatus.setFontScale(0.32f);
                txtStatus.setColor(Color.valueOf("fc3c3c"));
                txtStatus.setAlignment(8);
                txtStatus.setTouchable(Touchable.disabled);
                infoTable.add(txtStatus).fillX();
            }
            Button deleteButton = new Button(this.deleteButtonStyle);
            rowTable.add(deleteButton).width(50.0f).height(53.0f).spaceRight(5.0f).right().padLeft(10.0f);
            deleteButton.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    destinationPad.clearLaunchPad();
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    LaunchPadListPopup.this.createTooltip(Localization.get("deconstructRocket"));
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    LaunchPadListPopup.this.removeTooltip();
                }
            });
            if (!lpad.hasRocket) {
                deleteButton.setVisible(false);
                deleteButton.setTouchable(Touchable.disabled);
            }
            TextButton btnSelect = new TextButton(Localization.get("select"), this.baseScreen.textBtnStyle);
            btnSelect.getLabel().setFontScale(0.4f);
            btnSelect.padLeft(10.0f).padRight(10.0f);
            btnSelect.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent e, float x, float y) {
                    LaunchPadListPopup.this.setTarget(destinationPad);
                    Audio.getInstance().playSound("sounds/menuForward_gameStart.ogg", 0.7f);
                }
            });
            if (lpad.hasRocket) {
                btnSelect.setDisabled(true);
                btnSelect.setTouchable(Touchable.disabled);
            } else {
                btnSelect.setDisabled(false);
                btnSelect.setTouchable(Touchable.enabled);
            }
            rowTable.add(btnSelect).right().fillX().padLeft(5.0f).height(70.0f).width(140.0f);
            this.table.add(rowTable).fillX().expandX().spaceBottom(10.0f);
            this.table.row();
        }
        MoonBase.log("Scroller W: " + this.scroller.getWidth());
        this.table.layout();
        this.scroller.layout();
    }

    private void addEndGameDestination() {
        Table rowTable = new Table();
        rowTable.left();
        rowTable.setBackground(this.panelDrawable);
        Group head = new Group();
        rowTable.add(head).width(130.0f).fillY().height(100.0f).padRight(20.0f);
        Image preview = new Image(this.gameScreen.skin.getDrawable("map/map-planet"));
        preview.setScaling(Scaling.fit);
        preview.setSize(130.0f, 100.0f);
        head.addActor(preview);
        Image star = new Image(this.gameScreen.skin.getDrawable("equippedFlag"));
        star.setPosition(95.0f, 25.0f, 1);
        head.addActor(star);
        Table infoTable = new Table();
        infoTable.left();
        rowTable.add(infoTable).fill().expandX().left();
        Label nameLabel = new Label((CharSequence)Localization.get("launchpad_leavePlanet"), this.labelStyle);
        nameLabel.setFontScale(0.5f);
        nameLabel.setAlignment(8);
        nameLabel.setTouchable(Touchable.disabled);
        infoTable.add(nameLabel).fillX().expandX();
        infoTable.row();
        infoTable.row();
        Label txtDistance = new Label((CharSequence)Localization.get("missionNotFinished"), this.labelStyle);
        txtDistance.setFontScale(0.32f);
        txtDistance.setColor(Vars.WARNING_RED);
        txtDistance.setAlignment(8);
        txtDistance.setTouchable(Touchable.disabled);
        infoTable.add(txtDistance).fillX();
        TextButton btnSelect = new TextButton("Select", this.baseScreen.textBtnStyle);
        btnSelect.getLabel().setFontScale(0.4f);
        btnSelect.padLeft(10.0f).padRight(10.0f);
        btnSelect.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                LaunchPadListPopup.this.setTargetEndGame();
            }
        });
        if (MoonBase.getCurrentMission().missionCompleteReady && !this.gameScreen.world.player.isFlyingRocket()) {
            txtDistance.setText(Localization.get("launchpad_completeYourMission"));
            txtDistance.setColor(MENU_COLOR);
            btnSelect.setDisabled(false);
            btnSelect.setTouchable(Touchable.enabled);
            rowTable.setColor(Color.WHITE);
        } else {
            txtDistance.setText(Localization.get("missionNotFinished"));
            txtDistance.setColor(Vars.WARNING_RED);
            btnSelect.setDisabled(true);
            btnSelect.setTouchable(Touchable.disabled);
            rowTable.setColor(1.0f, 1.0f, 1.0f, 0.5f);
        }
        rowTable.add(btnSelect).right().fillX().padLeft(15.0f).height(70.0f).width(140.0f);
        this.table.add(rowTable).fillX().expandX().spaceBottom(10.0f);
        this.table.layout();
    }

    private Image addMapScreenshot(LaunchPad lpad) {
        Texture t = WorldDebugMap.getLaunchPadMap(lpad);
        Image i = new Image(t);
        this.disposableTextures.add(t);
        return i;
    }

    private void setTarget(LaunchPad l) {
        this.localLaunchPad.setDestination(l);
        if (this.launchPadMenu != null) {
            this.launchPadMenu.updateDestination();
        }
        this.back();
    }

    private void setTargetEndGame() {
        if (MoonBase.isMultiplayer) {
            try {
                this.localLaunchPad.notifyMissionComplete(this.gameScreen.world != null ? this.gameScreen.world.player : null);
            } catch (Exception ignored) {}
            this.back();
            return;
        }
        this.localLaunchPad.setDestinationEndGame();
        if (this.launchPadMenu != null) {
            this.launchPadMenu.updateDestination();
        }
        this.back();
    }

    private String getDistance(LaunchPad dest) {
        float dist = Vector2.dst(this.localLaunchPad.worldX, this.localLaunchPad.worldY, dest.worldX, dest.worldY);
        return Vars.wholeNum.format(dist);
    }

    @Override
    public void back() {
        for (Texture t : this.disposableTextures) {
            t.dispose();
        }
        this.disposableTextures.clear();
        MessageManager.getInstance().removeListener((Telegraph)this, 42);
        super.back();
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        if (msg.message == 42) {
            this.table.clearChildren(true);
            this.addListOptions();
            this.addEndGameDestination();
            return true;
        }
        return false;
    }
}
