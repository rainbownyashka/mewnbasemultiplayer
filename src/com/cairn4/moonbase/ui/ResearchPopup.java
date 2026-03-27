/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
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
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.tiles.behaviors.ScienceLabBehavior;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.ItemButton;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import com.cairn4.moonbase.ui.TechTreePopup;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class ResearchPopup
extends Popup
implements Observer {
    GameScreen gameScreen;
    ScienceLabBehavior scienceLabBehavior;
    Player player;
    private Table table;
    private Label inventoryLabel;
    private Label researchSideLabel;
    private Table inventoryTable;
    private Table detailsTable;
    private Image largeIcon;
    private Label nameLabel;
    private TextButton btnResearch;
    String noPowerText;
    private DecimalFormat dfMinute;
    private DecimalFormat dfSecond;
    private Image largeIconProgress;
    private Group progressTableWrapper;
    private Table progressTable;
    private Label progressTopLabel;
    private ProgressBar.ProgressBarStyle progressBarStyle;
    private ProgressBar progressBar;
    private Label progressTimeLabel;
    private Image magGlass;
    private Group resultsWrapperGroup;
    private Table resultsTable;
    private Label resultsHeadingLabel;
    private Image resultsIcon;
    private Label resultsNameLabel;
    private Label resultsPointsLabel;
    private TextButton resultsViewLogBtn;
    private TextButton resultsContinueBtn;
    private TextButton techTreeBtn;
    private PlayerInventory playerInventory;
    private ArrayList<ItemButton> inventoryButtonList = new ArrayList();
    private static final Color disabledIconColor = new Color(0.7f, 0.7f, 0.7f, 0.7f);
    private static final Color disabledBorderColor = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    float secondsLeft;
    float minutes;
    float seconds;
    String sec;
    float magGlassTimer = 0.0f;

    public ResearchPopup(GameScreen baseScreen, ScienceLabBehavior scienceLabBehavior, Player player) {
        super(baseScreen);
        this.gameScreen = baseScreen;
        this.scienceLabBehavior = scienceLabBehavior;
        this.scienceLabBehavior.addObserver(this);
        this.player = player;
        this.playerInventory = this.player.getPlayerInventory();
        this.noPowerText = "[#ff0000]" + Localization.get("no_power") + "[]";
        this.dfMinute = new DecimalFormat("#");
        this.dfSecond = new DecimalFormat("#00");
        this.setup();
        this.show();
        if (scienceLabBehavior.readyToCollect != -1) {
            this.showResults(scienceLabBehavior.readyToCollect);
        }
        if (scienceLabBehavior.researching) {
            this.showProgress();
        }
    }

    @Override
    protected void setup() {
        super.setup();
        this.setSize(900.0f, 600.0f);
        this.setTitle("item_sciencelab-builder");
        this.addMainTable();
        this.addProgressTable();
        this.addResultsTable();
        this.resetSelection();
        this.progressTableWrapper.setVisible(false);
        this.resultsWrapperGroup.setVisible(false);
    }

    private void showProgress() {
        this.detailsTable.setVisible(false);
        this.progressTableWrapper.setVisible(true);
        this.resultsWrapperGroup.setVisible(false);
    }

    private void showResults(int researchObjectId) {
        Audio.getInstance().playSound("sounds/research_complete.ogg", 0.4f);
        this.resultsIcon.setDrawable(this.skin.getDrawable("ro" + researchObjectId));
        this.resultsNameLabel.setText(Localization.get("item_researchObject") + " " + (researchObjectId + 1));
        this.resultsPointsLabel.setText(Localization.format("research_earnedPoints", ItemFactory.getInstance().getResearchPoints("researchObject")));
        this.table.setVisible(false);
        this.progressTableWrapper.setVisible(false);
        this.detailsTable.setVisible(false);
        this.resultsWrapperGroup.clearActions();
        this.resultsWrapperGroup.addAction(Actions.sequence((Action)Actions.scaleTo(0.8f, 0.8f), (Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.parallel((Action)Actions.scaleTo(1.025f, 1.025f, 0.25f, Interpolation.sineOut), (Action)Actions.alpha(1.0f, 0.25f)), (Action)Actions.scaleTo(1.0f, 1.0f, 0.05f, Interpolation.sine)));
    }

    private void addMainTable() {
        this.table = new Table();
        this.table.top();
        this.table.setPosition(75.0f, 70.0f);
        this.table.setSize(300.0f, this.panelBg.getHeight() - 200.0f);
        this.table.padTop(20.0f);
        this.popupGroup.addActor(this.table);
        this.inventoryLabel = new Label((CharSequence)Localization.get("inventory"), this.labelStyle);
        this.inventoryLabel.setFontScale(0.4f);
        this.inventoryLabel.setColor(Menu.MENU_COLOR);
        this.table.add(this.inventoryLabel);
        this.table.row();
        this.setupInventorySide();
        this.setupDetailsTable();
    }

    private void setupInventorySide() {
        float spacing = 3.0f;
        float iconScaling = 0.8f;
        this.inventoryTable = new Table();
        this.inventoryTable.top().left();
        this.table.add(this.inventoryTable).pad(20.0f);
        for (int y = 0; y < 4; ++y) {
            this.inventoryTable.row();
            for (int x = 0; x < 3; ++x) {
                ItemButton i = new ItemButton((Menu)this, null);
                this.inventoryButtonList.add(i);
                i.setScale(0.8f);
                i.disable();
                i.hideIcon();
                this.inventoryTable.add(i.group).width(i.getWidth()).pad(spacing);
            }
        }
        this.updateInventory();
    }

    private void updateInventory() {
        int buildableIndex = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 3; ++x) {
                ItemButton b = this.inventoryButtonList.get(buildableIndex);
                if (buildableIndex < this.playerInventory.getItemList().size()) {
                    b.setIcon(this.playerInventory.getItemList().get(buildableIndex).getSprite());
                    b.setCount(this.playerInventory.getItemList().get(buildableIndex).getAmount());
                    b.clearListeners();
                    b.icon.clearActions();
                    if (this.playerInventory.getItemList().get(buildableIndex).getId().contains("researchObject")) {
                        b.enable();
                        b.resetColors();
                        b.icon.setOrigin(1);
                        b.icon.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.2f, 1.2f, 0.5f, Interpolation.sine), (Action)Actions.scaleTo(1.0f, 1.0f, 0.5f, Interpolation.sine))));
                        b.showTutorialArrow();
                    } else {
                        b.disable();
                        b.setBorderColor(disabledBorderColor);
                        b.setIconColor(disabledIconColor);
                        b.removeTutorialArrow();
                        b.button.setChecked(false);
                    }
                    final int invToResearchIndex = buildableIndex;
                    Button selectedButton = b.button;
                    b.addListener(new ClickListener(){

                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            ResearchPopup.this.selectObject(invToResearchIndex);
                            ResearchPopup.this.selectButton(invToResearchIndex);
                        }

                        @Override
                        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                            ResearchPopup.this.createTooltip(ResearchPopup.this.playerInventory.getItemList().get(invToResearchIndex).getName());
                        }

                        @Override
                        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                            ResearchPopup.this.removeTooltip();
                        }
                    });
                } else {
                    b.setEquippedIcon(false);
                    b.disable();
                    b.hideIcon();
                    b.setCount(0);
                    b.setBorderColor(disabledBorderColor);
                    b.setIconColor(disabledIconColor);
                    b.removeTutorialArrow();
                    b.button.setChecked(false);
                }
                ++buildableIndex;
            }
        }
    }

    private void selectButton(int index) {
        for (int i = 0; i < this.inventoryButtonList.size(); ++i) {
            this.inventoryButtonList.get((int)i).button.setChecked(i == index);
        }
    }

    private void startResearchingItem(int inventoryIndex) {
        Audio.getInstance().playSound("sounds/research_start.ogg", 0.5f);
        this.progressTableWrapper.setVisible(true);
        this.detailsTable.setVisible(false);
        this.resultsWrapperGroup.setVisible(false);
        String id = this.playerInventory.getItemList().get(inventoryIndex).getId();
        if (id.equals("researchObject")) {
            ItemStack stack = this.playerInventory.getItemList().get(inventoryIndex);
            this.playerInventory.consumeItem(stack, 1);
            this.updateInventory();
            this.scienceLabBehavior.startResearch();
        } else {
            Gdx.app.log("MewnBase", "Invalid research object");
        }
    }

    private void setupDetailsTable() {
        this.detailsTable = new Table();
        this.detailsTable.top();
        this.detailsTable.padTop(20.0f);
        this.detailsTable.setSize(this.panelBg.getWidth() - 150.0f - this.table.getWidth(), this.table.getHeight());
        this.detailsTable.setPosition(this.table.getX(16), this.table.getY());
        this.popupGroup.addActor(this.detailsTable);
        this.researchSideLabel = new Label((CharSequence)Localization.get("research_selectObject"), this.labelStyle);
        this.researchSideLabel.setFontScale(0.4f);
        this.researchSideLabel.setColor(Menu.MENU_COLOR);
        this.detailsTable.add(this.researchSideLabel).padBottom(20.0f);
        this.detailsTable.row();
        Stack iconStack = new Stack();
        NinePatch alt_np_up = new NinePatch(this.skin.getRegion("btn-thin"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_up = new NinePatchDrawable(alt_np_up);
        Image iconBorder = new Image(alt_npd_up);
        Table iconOverlay = new Table();
        this.largeIcon = new Image(this.skin.getDrawable("white"));
        iconOverlay.add(this.largeIcon).pad(8.0f).size(150.0f, 150.0f);
        iconStack.add(iconBorder);
        iconStack.add(iconOverlay);
        this.detailsTable.add(iconStack).space(10.0f);
        this.detailsTable.row();
        this.nameLabel = new Label((CharSequence)"Solar Panels", this.labelStyle);
        this.nameLabel.setFontScale(0.55f);
        this.nameLabel.setColor(Color.WHITE);
        this.nameLabel.setAlignment(1);
        this.nameLabel.setWrap(false);
        this.nameLabel.setTouchable(Touchable.disabled);
        this.detailsTable.add(this.nameLabel).fillX().expandX().space(10.0f);
        this.detailsTable.row();
        this.detailsTable.row();
        Table bottomRow = new Table();
        this.btnResearch = new TextButton(Localization.get("research"), this.baseScreen.textBtnStyle);
        this.btnResearch.getLabel().setFontScale(0.5f);
        bottomRow.add(this.btnResearch).width(204.0f).height(75.0f).expandY();
        this.btnResearch.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
            }
        });
        this.detailsTable.add(bottomRow).expandX().fillX().bottom();
    }

    private void selectObject(int inventoryIndex) {
        this.largeIcon.setVisible(false);
        this.disableButton(this.btnResearch);
        this.btnResearch.clearListeners();
        String id = this.playerInventory.getItemList().get(inventoryIndex).getId();
        final int fIndex = inventoryIndex;
        System.out.println("selected: " + fIndex + " of " + this.playerInventory.getItemList().size());
        if (id.contains("researchObject")) {
            this.largeIcon.setVisible(true);
            String sprite = this.playerInventory.getItemList().get(inventoryIndex).getSprite();
            this.largeIcon.setDrawable(this.skin.getDrawable(sprite));
            this.enableButton(this.btnResearch);
            this.nameLabel.setText(Localization.get("item_researchObject_unknown"));
            this.btnResearch.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent e, float x, float y) {
                    ResearchPopup.this.startResearchingItem(fIndex);
                }
            });
        }
    }

    private void resetSelection() {
        this.largeIcon.setVisible(false);
        this.nameLabel.setText("-");
        this.disableButton(this.btnResearch);
    }

    private void addProgressTable() {
        this.progressTableWrapper = new Group();
        this.popupGroup.addActor(this.progressTableWrapper);
        this.progressTable = new Table();
        this.progressTable.top();
        this.progressTable.padTop(20.0f);
        this.progressTable.setSize(this.panelBg.getWidth() - 150.0f - this.table.getWidth(), this.table.getHeight());
        this.progressTable.setPosition(this.table.getX(16), this.table.getY());
        this.progressTableWrapper.addActor(this.progressTable);
        this.progressTopLabel = new Label((CharSequence)Localization.get("research_inProgress"), this.labelStyle);
        this.progressTopLabel.setFontScale(0.5f);
        this.progressTable.add(this.progressTopLabel).padBottom(20.0f);
        this.progressTable.row();
        this.largeIconProgress = new Image(this.skin.getDrawable("artifact"));
        this.progressTable.add(this.largeIconProgress).pad(25.0f).padBottom(55.0f).center().height(170.0f).width(170.0f);
        this.progressTable.row();
        this.progressBarStyle = new ProgressBar.ProgressBarStyle();
        this.progressBarStyle.knobAfter = this.skin.getDrawable("hud-progress-empty");
        this.progressBarStyle.knobBefore = this.skin.getDrawable("hud-progress-filled");
        this.progressBarStyle.knob = this.skin.getDrawable("hud-progress-knob");
        this.progressBar = new ProgressBar(0.0f, 1.0f, 0.01f, false, this.progressBarStyle);
        this.progressBar.setValue(0.5f);
        this.progressTable.add(this.progressBar).minHeight(15.0f).fillX().spaceLeft(4.0f).spaceRight(4.0f).padLeft(30.0f).padRight(30.0f);
        this.progressTable.row();
        this.progressTimeLabel = new Label((CharSequence)"Time Left: 1:30", this.labelStyle);
        this.progressTimeLabel.setFontScale(0.55f);
        this.progressTimeLabel.setColor(MENU_COLOR);
        this.progressTimeLabel.setAlignment(1);
        this.progressTimeLabel.setWrap(false);
        this.progressTable.add(this.progressTimeLabel).pad(20.0f);
        Group magGlassOffset = new Group();
        this.progressTableWrapper.addActor(magGlassOffset);
        Image researchSpinner = new Image(this.skin.getDrawable("research-spinner"));
        researchSpinner.setSize(220.0f, 220.0f);
        researchSpinner.setPosition(595.0f, 285.0f, 1);
        researchSpinner.setOrigin(1);
        researchSpinner.setColor(MENU_COLOR);
        researchSpinner.getColor().a = 0.3f;
        researchSpinner.addAction(Actions.forever(Actions.sequence((Action)Actions.rotateBy(-360.0f, 10.0f))));
        this.progressTableWrapper.addActor(researchSpinner);
        researchSpinner.toBack();
        Image researchSpinner2 = new Image(this.skin.getDrawable("research-spinner"));
        researchSpinner2.setSize(250.0f, 250.0f);
        researchSpinner2.setPosition(595.0f, 285.0f, 1);
        researchSpinner2.setOrigin(1);
        researchSpinner2.setColor(MENU_COLOR);
        researchSpinner2.getColor().a = 0.1f;
        researchSpinner2.addAction(Actions.forever(Actions.sequence((Action)Actions.rotateBy(360.0f, 13.0f))));
        this.progressTableWrapper.addActor(researchSpinner2);
        researchSpinner2.toBack();
        magGlassOffset.setPosition(595.0f, 285.0f);
        this.magGlass = new Image(this.skin.getDrawable("magnifying-glass"));
        this.magGlass.setColor(MENU_COLOR);
        this.magGlass.getColor().a = 0.0f;
        this.magGlass.setSize(50.0f, 50.0f);
        this.magGlass.setOrigin(1);
        this.magGlass.setPosition(0.0f, 70.0f, 1);
        magGlassOffset.addActor(this.magGlass);
        this.magGlass.addAction(Actions.forever(Actions.sequence((Action)Actions.sequence((Action)Actions.alpha(1.0f, 1.0f), (Action)Actions.alpha(0.5f, 1.0f)))));
    }

    private void addResultsTable() {
        this.resultsWrapperGroup = new Group();
        this.resultsWrapperGroup.setSize(this.panelBg.getWidth() - 150.0f, this.panelBg.getHeight() - 200.0f);
        this.resultsWrapperGroup.setOrigin(1);
        this.resultsWrapperGroup.setPosition(75.0f, 70.0f);
        this.popupGroup.addActor(this.resultsWrapperGroup);
        this.resultsTable = new Table();
        this.resultsTable.top();
        this.resultsTable.setFillParent(true);
        this.resultsTable.padTop(10.0f);
        this.resultsWrapperGroup.addActor(this.resultsTable);
        this.resultsHeadingLabel = new Label((CharSequence)Localization.get("research_resultsHeading"), this.labelStyle);
        this.resultsHeadingLabel.setFontScale(0.4f);
        this.resultsHeadingLabel.setColor(Menu.MENU_COLOR);
        this.resultsTable.add(this.resultsHeadingLabel).space(15.0f);
        this.resultsTable.row();
        Stack iconStack = new Stack();
        NinePatch alt_np_up = new NinePatch(this.skin.getRegion("btn-thin"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_up = new NinePatchDrawable(alt_np_up);
        Image iconBorder = new Image(alt_npd_up);
        Table iconOverlay = new Table();
        this.resultsIcon = new Image(this.skin.getDrawable("white"));
        iconOverlay.add(this.resultsIcon).pad(8.0f).size(150.0f, 150.0f);
        iconStack.add(iconBorder);
        iconStack.add(iconOverlay);
        this.resultsTable.add(iconStack).space(10.0f);
        this.resultsTable.row();
        this.resultsNameLabel = new Label((CharSequence)"Solar Panels", this.labelStyle);
        this.resultsNameLabel.setFontScale(0.55f);
        this.resultsNameLabel.setColor(Color.WHITE);
        this.resultsNameLabel.setAlignment(1);
        this.resultsNameLabel.setWrap(false);
        this.resultsNameLabel.setTouchable(Touchable.disabled);
        this.resultsTable.add(this.resultsNameLabel).fillX().expandX().space(10.0f);
        this.resultsTable.row();
        this.resultsPointsLabel = new Label((CharSequence)Localization.format("research_earnedPoints", "XX"), this.labelStyle);
        this.resultsPointsLabel.setFontScale(0.4f);
        this.resultsPointsLabel.setColor(MENU_COLOR);
        this.resultsPointsLabel.setWrap(true);
        this.resultsPointsLabel.setAlignment(1);
        this.resultsTable.add(this.resultsPointsLabel).fillX().expandX().space(10.0f);
        this.resultsTable.row();
        Table buttonRowTable = new Table();
        buttonRowTable.center();
        this.resultsTable.add(buttonRowTable).height(75.0f).expandY().fillX();
        this.resultsContinueBtn = new TextButton(Localization.get("research_continue"), this.baseScreen.textBtnStyle);
        this.resultsContinueBtn.getLabel().setFontScale(0.5f);
        this.resultsContinueBtn.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                ResearchPopup.this.resultsWrapperGroup.setVisible(false);
                ResearchPopup.this.table.setVisible(true);
                ResearchPopup.this.detailsTable.setVisible(true);
                ResearchPopup.this.updateInventory();
                ResearchPopup.this.resetSelection();
                ResearchPopup.this.scienceLabBehavior.collect();
            }
        });
        buttonRowTable.add(this.resultsContinueBtn).width(300.0f).height(75.0f).expandY().space(20.0f);
        this.techTreeBtn = new TextButton(Localization.get("controls_techUpgrades"), this.baseScreen.textBtnStyle);
        this.techTreeBtn.getLabel().setFontScale(0.5f);
        this.techTreeBtn.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                ResearchPopup.this.resultsWrapperGroup.setVisible(false);
                ResearchPopup.this.table.setVisible(true);
                ResearchPopup.this.detailsTable.setVisible(true);
                ResearchPopup.this.updateInventory();
                ResearchPopup.this.resetSelection();
                ResearchPopup.this.scienceLabBehavior.collect();
                ResearchPopup.this.back();
                ResearchPopup.this.gameScreen.showMenu(new TechTreePopup(ResearchPopup.this.gameScreen));
            }
        });
        buttonRowTable.add(this.techTreeBtn).width(300.0f).height(75.0f).expandY().space(20.0f);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o == "done") {
            this.showResults(this.scienceLabBehavior.readyToCollect);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.scienceLabBehavior.researching) {
            this.magGlassTimer += delta;
            float x = MathUtils.cos(this.magGlassTimer * 2.0f) * 50.0f;
            float y = -MathUtils.sin(this.magGlassTimer * 2.0f) * 50.0f;
            this.magGlass.setPosition(x, y, 1);
            this.progressBar.setValue(this.scienceLabBehavior.getProgress());
            this.secondsLeft = 120.0f - this.scienceLabBehavior.timer;
            this.minutes = MathUtils.floor(this.secondsLeft / 60.0f);
            this.seconds = this.secondsLeft % 60.0f;
            if (this.seconds > 59.0f) {
                this.minutes += 1.0f;
                this.seconds = 0.0f;
            }
            this.sec = "" + this.dfSecond.format(this.seconds);
            if (this.scienceLabBehavior.baseModule.hasPower) {
                this.progressBar.setColor(Color.WHITE);
                this.progressTimeLabel.setText(Localization.format("research_timeLeft", this.dfMinute.format(this.minutes), this.sec));
            } else {
                this.progressTimeLabel.setText(Localization.format("research_timeLeftOffline", this.noPowerText));
                this.progressBar.setColor(Color.RED);
            }
        } else {
            this.progressBar.setValue(0.0f);
        }
    }

    @Override
    public void hide() {
        this.scienceLabBehavior.deleteObserver(this);
        super.hide();
    }
}

