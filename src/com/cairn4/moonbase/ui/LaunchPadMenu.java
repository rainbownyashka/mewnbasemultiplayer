/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.WorldDebugMap;
import com.cairn4.moonbase.tiles.LaunchPad;
import com.cairn4.moonbase.ui.BaseStatResourceBar;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.LaunchPadListPopup;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import com.cairn4.moonbase.ui.RenameLaunchpadPopup;
import com.cairn4.moonbase.ui.RocketCraftingUI;
import java.util.ArrayList;

public class LaunchPadMenu
extends Popup {
    private GameScreen gameScreen;
    public LaunchPad launchPad;
    Table table;
    Table fuelTable;
    Table innerFuelTable;
    Label txtFuelStorageTitle;
    Label txtFuelStored;
    TextButton btnFuelRocket;
    TextButton btnUnfuelRocket;
    Label errorMessage;
    TextButton btnRename;
    Table vehicleTable;
    private Table noVehicleTable;
    Image noVehicleImage;
    TextButton btnCreateRocket;
    private Table craftingVehicleTable;
    Image vehicleConstructionImage;
    ProgressBar.ProgressBarStyle progressBarStyle;
    ProgressBar buildProgressBar;
    Label txtBuildProgress;
    private Table vehicleIdleTable;
    private Button deleteButton;
    Label txtVehicleTitle;
    Label txtRocketFuel;
    Image vehicleImage;
    Table destinationTable;
    Table innerDestTable;
    Image destinationImage;
    Drawable finalDestDrawable;
    Label txtDestinationTitle;
    Label txtDestination;
    TextButton btnDestination;
    TextButton btnEnterRocket;
    private NinePatchDrawable panelDrawable;
    private NinePatchDrawable panelDrawableDark;
    private Button.ButtonStyle deleteButtonStyle;
    BaseStatResourceBar fuelResourceBar;
    BaseStatResourceBar rocketFuelResourceBar;
    private ArrayList<Texture> disposableTextures = new ArrayList();
    Player player;
    private Table iconTable;
    public ArrayList<String> spriteList = new ArrayList();
    public float fps = 5.0f;
    private float fpsTimer = 0.0f;
    private int frameIndex = 0;
    private boolean playingStatic = false;

    public LaunchPadMenu(GameScreen gameScreen, LaunchPad launchPad, Player player) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        this.launchPad = launchPad;
        this.player = player;
        this.setup();
        this.setSize(1024.0f, 612.0f);
        this.addTables();
        this.setupStaticAnim();
        this.updateDestination();
        this.updateRocketImage();
        this.show();
        gameScreen.hud.hudNotifications.reparentGroup(this.stage.getRoot());
        AssetManagerSingleton.getInstance().load("music/rocket_cockpit.ogg", Sound.class);
    }

    @Override
    protected void setup() {
        super.setup();
        this.setTitleNonLoc("Pad " + this.launchPad.getName());
        NinePatch np_up = new NinePatch(this.skin.getRegion("btn-item"), 16, 16, 16, 16);
        this.panelDrawable = new NinePatchDrawable(np_up);
        NinePatch np_pressed = new NinePatch(this.skin.getRegion("launchpad-mapbg"), 16, 16, 16, 16);
        this.panelDrawableDark = new NinePatchDrawable(np_pressed);
        this.deleteButtonStyle = new Button.ButtonStyle();
        this.deleteButtonStyle.up = this.skin.getDrawable("remove-icon");
        this.deleteButtonStyle.over = this.skin.getDrawable("remove-icon-hover");
        this.deleteButtonStyle.down = this.skin.getDrawable("remove-icon-pressed");
    }

    private void addTables() {
        MoonBase.log("popup group size: " + this.popupGroup.getWidth() + ", " + this.popupGroup.getHeight());
        float sideColumnWidth = 270.0f;
        this.table = new Table();
        this.popupGroup.addActor(this.table);
        this.table.setFillParent(true);
        this.table.pad(130.0f, 75.0f, 60.0f, 75.0f);
        this.fuelTable = new Table();
        this.table.add(this.fuelTable).width(sideColumnWidth).fillY().top().spaceRight(20.0f);
        this.txtFuelStorageTitle = new Label((CharSequence)"Fuel Tanks", this.gameScreen.labelStyle);
        this.txtFuelStorageTitle.setFontScale(0.35f);
        this.txtFuelStorageTitle.setAlignment(1);
        this.fuelTable.add(this.txtFuelStorageTitle).spaceBottom(10.0f);
        this.fuelTable.row();
        this.innerFuelTable = new Table();
        this.innerFuelTable.setBackground(this.panelDrawable);
        this.innerFuelTable.pad(15.0f);
        this.innerFuelTable.top();
        this.fuelTable.add(this.innerFuelTable).fillX().fillY().expandY().expandX().top();
        this.fuelResourceBar = new BaseStatResourceBar(this.gameScreen.hud, null, null, BaseResources.fuel);
        this.innerFuelTable.add(this.fuelResourceBar.group).width(200.0f).height(44.0f).padBottom(15.0f).top();
        this.fuelResourceBar.setWidth(200.0f);
        this.fuelResourceBar.setIconSize(48.0f);
        this.innerFuelTable.row();
        this.btnFuelRocket = new TextButton("Fuel Vehicle", this.baseScreen.altTextBtnStyle);
        this.btnFuelRocket.getLabel().setFontScale(0.3f);
        this.innerFuelTable.add(this.btnFuelRocket).height(60.0f).fillX().expandX().spaceBottom(5.0f).top();
        this.btnFuelRocket.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                MoonBase.log("Fuel Rocket");
                LaunchPadMenu.this.launchPad.fuelToRocket();
                LaunchPadMenu.this.updateEnterButton();
            }
        });
        this.innerFuelTable.row();
        this.btnUnfuelRocket = new TextButton("Empty Vehicle", this.baseScreen.altTextBtnStyle);
        this.btnUnfuelRocket.getLabel().setFontScale(0.3f);
        this.innerFuelTable.add(this.btnUnfuelRocket).height(60.0f).fillX().expandX().top();
        this.btnUnfuelRocket.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                MoonBase.log("Drain Rocket");
                LaunchPadMenu.this.launchPad.drainRocket();
                LaunchPadMenu.this.updateEnterButton();
            }
        });
        this.innerFuelTable.row();
        this.errorMessage = new Label((CharSequence)("- " + Localization.get("no_power") + " -"), this.labelStyle);
        this.errorMessage.setColor(Vars.WARNING_RED);
        this.errorMessage.setFontScale(0.35f);
        this.errorMessage.setAlignment(1);
        this.innerFuelTable.add(this.errorMessage).fillX().spaceTop(15.0f);
        this.errorMessage.addAction(Actions.forever(Actions.sequence((Action)Actions.alpha(0.25f, 0.875f), (Action)Actions.alpha(1.0f, 0.125f))));
        this.fuelTable.row();
        this.btnRename = new TextButton(Localization.get("rename_launchpad"), this.baseScreen.altTextBtnStyle);
        this.btnRename.getLabel().setFontScale(0.3f);
        this.fuelTable.add(this.btnRename).fillX().height(70.0f).spaceTop(10.0f);
        this.btnRename.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                MoonBase.log("Rename Launchpad");
                LaunchPadMenu.this.showRenamePopup();
            }
        });
        this.vehicleTable = new Table();
        this.table.add(this.vehicleTable).fill().top();
        this.txtVehicleTitle = new Label((CharSequence)Localization.get("launchVehicle"), this.gameScreen.labelStyle);
        this.txtVehicleTitle.setFontScale(0.35f);
        this.txtVehicleTitle.setAlignment(1);
        this.vehicleTable.add(this.txtVehicleTitle).spaceBottom(10.0f);
        this.vehicleTable.row();
        Table innerVehicle = new Table();
        innerVehicle.setBackground(this.panelDrawable);
        this.vehicleTable.add(innerVehicle).fill().expand();
        Stack vehicleStack = new Stack();
        Image gridBg = new Image(this.skin.getDrawable("powerpopup-grid"));
        gridBg.setScaling(Scaling.fill);
        gridBg.setColor(Menu.MENU_COLOR);
        gridBg.getColor().a = 0.2f;
        vehicleStack.add(gridBg);
        innerVehicle.add(vehicleStack).fill().expand();
        this.iconTable = new Table();
        vehicleStack.add(this.iconTable);
        this.iconTable.pad(40.0f).padBottom(150.0f);
        this.iconTable.center();
        Image buildIcon = new Image(this.skin.getDrawable("construction-icon"));
        buildIcon.setColor(MENU_COLOR);
        buildIcon.getColor().a = 0.4f;
        buildIcon.setScaling(Scaling.fit);
        this.iconTable.add(buildIcon).center();
        this.noVehicleTable = new Table();
        this.noVehicleTable.pad(15.0f);
        vehicleStack.add(this.noVehicleTable);
        this.btnCreateRocket = new TextButton("Build...", this.baseScreen.textBtnStyle);
        this.btnCreateRocket.getLabel().setFontScale(0.35f);
        this.btnCreateRocket.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                LaunchPadMenu.this.launchPad.itemCrafter.player = LaunchPadMenu.this.player;
                LaunchPadMenu.this.launchPad.itemCrafter.playerInventory = LaunchPadMenu.this.player.playerInventory;
                MoonBase.log("clicked btnCreateRocket");
                RocketCraftingUI craftingUI = new RocketCraftingUI(LaunchPadMenu.this.gameScreen, LaunchPadMenu.this.launchPad.itemCrafter, LaunchPadMenu.this.player.playerInventory);
                craftingUI.setTitle("launchVehicle");
                LaunchPadMenu.this.gameScreen.showMenu(craftingUI);
            }
        });
        this.noVehicleTable.add(this.btnCreateRocket).width(230.0f).height(80.0f).center().padTop(150.0f);
        this.craftingVehicleTable = new Table();
        this.craftingVehicleTable.pad(15.0f);
        vehicleStack.add(this.craftingVehicleTable);
        this.vehicleConstructionImage = new Image(this.gameScreen.skin.getDrawable("rocket2"));
        this.vehicleConstructionImage.setScaling(Scaling.fill);
        this.craftingVehicleTable.add(this.vehicleConstructionImage).width(182.0f).height(260.0f).center();
        this.craftingVehicleTable.row();
        this.txtBuildProgress = new Label((CharSequence)"Building...", this.gameScreen.labelStyle);
        this.txtBuildProgress.setFontScale(0.35f);
        this.txtBuildProgress.setAlignment(1);
        this.craftingVehicleTable.add(this.txtBuildProgress).spaceBottom(10.0f);
        this.craftingVehicleTable.row();
        this.progressBarStyle = new ProgressBar.ProgressBarStyle();
        this.progressBarStyle.knobAfter = this.skin.getDrawable("hud-progress-empty");
        this.progressBarStyle.knobBefore = this.skin.getDrawable("hud-progress-filled");
        this.progressBarStyle.knob = this.skin.getDrawable("hud-progress-knob");
        this.buildProgressBar = new ProgressBar(0.0f, 1.0f, 0.01f, false, this.progressBarStyle);
        this.buildProgressBar.setValue(0.0f);
        this.craftingVehicleTable.add(this.buildProgressBar).fillX().center();
        this.vehicleIdleTable = new Table();
        this.vehicleIdleTable.pad(15.0f);
        vehicleStack.add(this.vehicleIdleTable);
        this.deleteButton = new Button(this.deleteButtonStyle);
        this.vehicleIdleTable.add(this.deleteButton).width(24.0f).height(24.0f).top().right().fillX().expandX().padRight(-18.0f).padTop(-23.0f);
        this.deleteButton.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                LaunchPadMenu.this.launchPad.clearLaunchPad();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                LaunchPadMenu.this.createTooltip(Localization.get("deconstructRocket"));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                LaunchPadMenu.this.removeTooltip();
            }
        });
        this.vehicleIdleTable.row();
        this.vehicleImage = new Image(this.gameScreen.skin.getDrawable("rocket2"));
        this.vehicleImage.setScaling(Scaling.fill);
        this.vehicleIdleTable.add(this.vehicleImage).width(182.0f).height(270.0f).center();
        this.vehicleIdleTable.row();
        this.rocketFuelResourceBar = new BaseStatResourceBar(this.gameScreen.hud, null, null, BaseResources.fuel);
        this.vehicleIdleTable.add(this.rocketFuelResourceBar.group).width(200.0f).height(44.0f).padBottom(0.0f).bottom();
        this.rocketFuelResourceBar.setWidth(200.0f);
        this.rocketFuelResourceBar.setIconSize(48.0f);
        this.vehicleIdleTable.row();
        this.destinationTable = new Table();
        this.table.add(this.destinationTable).width(sideColumnWidth).top().fill().spaceLeft(20.0f);
        this.txtDestinationTitle = new Label((CharSequence)"Destination", this.gameScreen.labelStyle);
        this.txtDestinationTitle.setFontScale(0.35f);
        this.txtDestinationTitle.setAlignment(1);
        this.destinationTable.add(this.txtDestinationTitle).spaceBottom(10.0f);
        this.destinationTable.row();
        this.innerDestTable = new Table();
        this.innerDestTable.setBackground(this.panelDrawable);
        this.innerDestTable.pad(15.0f);
        this.destinationTable.add(this.innerDestTable).fill().expand();
        Table destImageTable = new Table();
        destImageTable.setBackground(this.panelDrawableDark);
        destImageTable.pad(16.0f);
        this.innerDestTable.add(destImageTable).fillX().expandX().pad(4.0f);
        this.destinationImage = new Image(this.gameScreen.skin.getDrawable("pavement-striped2"));
        this.destinationImage.setScaling(Scaling.fit);
        destImageTable.add(this.destinationImage).fillX().fillY().expand().height(134.0f);
        this.innerDestTable.row();
        this.txtDestination = new Label((CharSequence)"Western Base\nLanding Pad", this.baseScreen.labelStyle);
        this.txtDestination.setFontScale(0.3f);
        this.txtDestination.setAlignment(1);
        this.innerDestTable.add(this.txtDestination).fill().expandY().pad(10.0f);
        this.innerDestTable.row();
        this.btnDestination = new TextButton("Set", this.baseScreen.altTextBtnStyle);
        this.btnDestination.getLabel().setFontScale(0.3f);
        this.innerDestTable.add(this.btnDestination).fillX().height(60.0f);
        final LaunchPadMenu lpmenu = this;
        this.btnDestination.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                LaunchPadListPopup p = new LaunchPadListPopup(LaunchPadMenu.this.gameScreen, LaunchPadMenu.this.launchPad, lpmenu);
                Audio.getInstance().playSound("sounds/gameStart.ogg", 0.25f);
                LaunchPadMenu.this.gameScreen.showMenu(p);
            }
        });
        this.destinationTable.row();
        this.btnEnterRocket = new TextButton("Enter Vehicle", this.baseScreen.textBtnStyle);
        this.btnEnterRocket.getLabel().setFontScale(0.35f);
        this.destinationTable.add(this.btnEnterRocket).fillX().height(70.0f).spaceTop(10.0f);
        this.btnEnterRocket.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                MoonBase.log("Launching rocket");
                ((LaunchPadMenu)LaunchPadMenu.this).gameScreen.world.getPlayer().setFlyingRocket(true);
                LaunchPadMenu.this.back();
                ((LaunchPadMenu)LaunchPadMenu.this).gameScreen.hud.showLaunchControls(LaunchPadMenu.this.launchPad);
                LaunchPadMenu.this.launchPad.launching = true;
            }
        });
    }

    @Override
    public void hide() {
        super.hide();
        this.gameScreen.hud.hudNotifications.resetGroup();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.fuelResourceBar.setAmount(this.launchPad.getBaseGroup().getTotalFuelStored(), this.launchPad.getBaseGroup().getTotalMaxFuelStored(), false);
        float fuelStored = this.launchPad.getBaseGroup().getTotalFuelStored();
        float fuelMax = this.launchPad.getBaseGroup().getTotalMaxFuelStored();
        this.fuelResourceBar.setAmount(fuelStored, fuelMax, false);
        if (this.playingStatic) {
            this.updateStaticAnim(delta);
        }
        this.errorMessage.setVisible(!this.launchPad.hasPower);
        if (this.launchPad.rocket != null) {
            this.noVehicleTable.setVisible(false);
            this.craftingVehicleTable.setVisible(false);
            this.vehicleIdleTable.setVisible(true);
            this.iconTable.setVisible(false);
            this.vehicleImage.setDrawable(this.gameScreen.skin.getDrawable("rocket2"));
            this.rocketFuelResourceBar.group.setVisible(true);
            this.rocketFuelResourceBar.setAmount(this.launchPad.rocket.getFuel(), this.launchPad.rocket.getMaxFuel(), false);
            boolean toRocketEnabled = true;
            boolean toStorageEnabled = true;
            if (this.launchPad.rocket.isFull()) {
                toRocketEnabled = false;
            }
            if (fuelStored >= fuelMax) {
                toStorageEnabled = false;
            }
            if (this.launchPad.rocket.getFuel() == 0.0f) {
                toStorageEnabled = false;
            }
            this.innerFuelTable.setColor(Color.WHITE);
            this.btnFuelRocket.setTouchable(toRocketEnabled ? Touchable.enabled : Touchable.disabled);
            this.btnFuelRocket.setDisabled(!toRocketEnabled);
            this.btnFuelRocket.setColor(Color.WHITE);
            this.btnUnfuelRocket.setTouchable(toStorageEnabled ? Touchable.enabled : Touchable.disabled);
            this.btnUnfuelRocket.setDisabled(!toStorageEnabled);
            this.btnUnfuelRocket.setColor(Color.WHITE);
        } else {
            this.noVehicleTable.setVisible(!this.launchPad.itemCrafter.isBuilding());
            this.craftingVehicleTable.setVisible(this.launchPad.itemCrafter.isBuilding());
            this.vehicleIdleTable.setVisible(false);
            this.iconTable.setVisible(!this.launchPad.itemCrafter.isBuilding());
            this.updateRocketImage();
            this.rocketFuelResourceBar.setAmount(0.0f, 0.0f, false);
            this.innerFuelTable.setColor(1.0f, 1.0f, 1.0f, 0.5f);
            this.btnFuelRocket.setTouchable(Touchable.disabled);
            this.btnFuelRocket.setDisabled(true);
            this.btnFuelRocket.setColor(1.0f, 1.0f, 1.0f, 0.3f);
            this.btnUnfuelRocket.setTouchable(Touchable.disabled);
            this.btnUnfuelRocket.setDisabled(true);
            this.btnUnfuelRocket.setColor(1.0f, 1.0f, 1.0f, 0.3f);
        }
    }

    public void showRenamePopup() {
        RenameLaunchpadPopup popup = new RenameLaunchpadPopup(this, this.gameScreen);
        this.gameScreen.showMenu(popup);
    }

    private void updateRocketImage() {
        if (this.launchPad.rocket == null && this.launchPad.itemCrafter.isBuilding()) {
            this.rocketFuelResourceBar.group.setVisible(false);
            float progress = this.launchPad.itemCrafter.getBuildProgress();
            this.buildProgressBar.setValue(progress);
            if (progress < 0.4f) {
                this.vehicleConstructionImage.setDrawable(this.gameScreen.skin.getDrawable("rocket-progress1"));
            } else {
                this.vehicleConstructionImage.setDrawable(this.gameScreen.skin.getDrawable("rocket-progress2"));
            }
        }
    }

    @Override
    public void returned() {
        this.updateEnterButton();
        this.setTitleNonLoc("Pad " + this.launchPad.getName());
    }

    public void updateDestination() {
        if (this.launchPad.targetDestination != null) {
            String lpadName = this.launchPad.targetDestination.getName();
            if (lpadName.length() > 12) {
                lpadName = lpadName.substring(0, 10) + "...";
            }
            this.txtDestination.setText(Localization.format("launchpad_name", lpadName));
            this.finalDestDrawable = new TextureRegionDrawable(WorldDebugMap.getLaunchPadMap(this.launchPad.targetDestination));
            this.playStatic();
        } else if (this.launchPad.leavePlanet) {
            this.txtDestination.setText("- " + Localization.get("launchpad_leavePlanet") + " -");
            this.finalDestDrawable = this.gameScreen.skin.getDrawable("map/map-planet");
            this.destinationImage.setDrawable(this.gameScreen.skin.getDrawable("lpad-static5"));
            this.destinationImage.setColor(1.0f, 1.0f, 1.0f, 0.25f);
            this.playStatic();
        } else {
            this.txtDestination.setText("- " + Localization.get("launchpad_notset") + " -");
            this.destinationImage.setDrawable(this.gameScreen.skin.getDrawable("lpad-static5"));
            this.destinationImage.setColor(1.0f, 1.0f, 1.0f, 0.25f);
        }
        this.updateEnterButton();
    }

    private void updateEnterButton() {
        if ((this.launchPad.targetDestination != null || this.launchPad.leavePlanet) && this.launchPad.rocket != null && this.launchPad.hasPower && this.launchPad.rocket.getFuel() >= 50.0f) {
            MoonBase.debug("Enter button: on");
            this.btnEnterRocket.setTouchable(Touchable.enabled);
            this.btnEnterRocket.setDisabled(false);
        } else {
            MoonBase.debug("Enter button: disabled");
            this.btnEnterRocket.setTouchable(Touchable.disabled);
            this.btnEnterRocket.setDisabled(true);
        }
    }

    @Override
    public void back() {
        for (Texture t : this.disposableTextures) {
            t.dispose();
        }
        this.disposableTextures.clear();
        super.back();
    }

    private void setupStaticAnim() {
        this.spriteList.clear();
        this.spriteList.add("lpad-static1");
        this.spriteList.add("lpad-static2");
        this.spriteList.add("lpad-static3");
        this.spriteList.add("lpad-static4");
        this.spriteList.add("lpad-static5");
    }

    private void playStatic() {
        this.playingStatic = true;
        this.fpsTimer = 0.0f;
        this.frameIndex = 0;
    }

    private void updateStaticAnim(float delta) {
        this.fpsTimer += delta;
        if (this.fpsTimer > 1.0f / this.fps) {
            this.fpsTimer = 0.0f;
            ++this.frameIndex;
            this.destinationImage.setColor(MENU_COLOR);
            if (this.frameIndex >= this.spriteList.size() - 1) {
                this.playingStatic = false;
                this.destinationImage.setDrawable(this.finalDestDrawable);
                this.destinationImage.setColor(Color.WHITE);
            } else {
                this.destinationImage.setDrawable(this.gameScreen.skin.getDrawable(this.spriteList.get(this.frameIndex)));
            }
        }
    }
}

