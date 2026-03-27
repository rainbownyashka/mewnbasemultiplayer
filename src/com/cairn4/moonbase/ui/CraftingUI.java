/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.RecipieRequirement;
import com.cairn4.moonbase.techtree.TechUpgrade;
import com.cairn4.moonbase.tiles.ConstructionYard;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.ItemButton;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class CraftingUI
extends Popup
implements Observer {
    GameScreen gameScreen;
    ItemCrafter itemCrafter;
    ItemData selectedItemData;
    public ArrayList<String> unlockedBuildables = new ArrayList();
    private static final float GRID_COLUMNS = 4.0f;
    ArrayList<ItemButton> itemButtonList = new ArrayList();
    private static final Color disabledIconColor = new Color(0.7f, 0.7f, 0.7f, 0.7f);
    private static final Color disabledBorderColor = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    private static final Color redWarningColor = new Color(0.96f, 0.2f, 0.11f, 1.0f);
    Group detailGroup;
    Image selectedIcon;
    Label nameLabel;
    Label descLabel;
    Label buildTimeLabel;
    Label requiresLabel;
    Group requiresGroup;
    ArrayList<Image> requireBorderList = new ArrayList();
    ArrayList<Image> requireIconList = new ArrayList();
    ArrayList<Label> requireLabelList = new ArrayList();
    TextButton btnCraft;
    Group queueGroup;
    Label buildQueueLabel;
    ArrayList<ItemButton> queueButtonList = new ArrayList();
    ArrayList<Label> queueButtonLabelList = new ArrayList();
    ProgressBar.ProgressBarStyle progressBarStyle;
    Image progressBarBg;
    ProgressBar progressBar;
    ArrayList<ItemButton> queueItemButtonList = new ArrayList();
    private ArrayList<Label> queueItemButtonSlotNumbers = new ArrayList();
    Group progressGroup;
    Label progressLabel;
    TextButton btnCollect;
    TextButton btnCancel;
    PlayerInventory playerInventory;
    private Group scrollGroup;
    private ScrollPane scroller;
    private Label categoryLabel;
    private Label inventoryFullLabel;
    private boolean showTutorialArrows = true;

    public CraftingUI(GameScreen gameScreen, ItemCrafter itemCrafter, PlayerInventory playerInventory) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        this.itemCrafter = itemCrafter;
        this.unlockedBuildables.clear();
        boolean inTutorial = false;
        BaseScreen baseScreen = this.baseScreen;
        inTutorial = baseScreen.game.getCurrentMission().missionType == Mission.MissionTypes.tutorial && gameScreen.tutorial != null ? !MoonBase.getCurrentMission().tutorialFinished : false;
        if (inTutorial) {
            for (String id : itemCrafter.buildables) {
                if (!this.isItemUnlocked(id) || !id.equals("food") && !id.equals("shovel")) continue;
                this.unlockedBuildables.add(id);
            }
        } else {
            for (String id : itemCrafter.buildables) {
                if (!this.isItemUnlocked(id)) continue;
                this.unlockedBuildables.add(id);
            }
        }
        this.setup();
        this.setSize(1024.0f, 612.0f);
        itemCrafter.addObserver(this);
        this.playerInventory = playerInventory;
        int startingIndex = this.itemIdToBuildIndex(itemCrafter.getLastSelectedItemId());
        int startingButtonIndex = this.itemIdToButtonIndex(itemCrafter.getLastSelectedItemId());
        if (startingIndex == -1) {
            this.selectNone();
        } else {
            this.selectRecipie(startingIndex, startingButtonIndex);
        }
        this.show();
        itemCrafter.collectItem();
        gameScreen.hud.hudNotifications.reparentGroup(this.stage.getRoot());
    }

    private void selectFirst() {
    }

    @Override
    public void hide() {
        this.itemCrafter.deleteObservers();
        super.hide();
        this.gameScreen.hud.hudNotifications.resetGroup();
    }

    @Override
    protected void setup() {
        super.setup();
        this.setTitle("workbench");
        if (this.itemCrafter.baseModule instanceof ConstructionYard) {
            this.setTitle("item_constructionyard-builder");
        }
        this.showCraftGrid();
        this.detailGroup = new Group();
        this.detailGroup.setPosition(460.0f, 60.0f);
        this.detailGroup.setSize(490.0f, 418.0f);
        this.popupGroup.addActor(this.detailGroup);
        this.detailGroup.setDebug(false);
        this.nameLabel = new Label((CharSequence)"Solar Panels", this.labelStyle);
        this.nameLabel.setFontScale(0.55f);
        this.nameLabel.setColor(Color.WHITE);
        this.nameLabel.setWrap(false);
        this.nameLabel.setPosition(0.0f, this.detailGroup.getHeight() - 63.0f);
        this.detailGroup.addActor(this.nameLabel);
        this.nameLabel.setTouchable(Touchable.disabled);
        this.descLabel = new Label((CharSequence)"Generates base power lorem ipsum dolor sit amet, consectetur.", this.labelStyle);
        this.descLabel.setFontScale(0.4f);
        this.descLabel.setColor(MENU_COLOR);
        this.descLabel.getColor().a = 0.65f;
        this.descLabel.setWrap(true);
        this.descLabel.setWidth(this.detailGroup.getWidth());
        this.descLabel.setAlignment(10);
        this.descLabel.setPosition(0.0f, this.detailGroup.getHeight() - 50.0f, 10);
        this.detailGroup.addActor(this.descLabel);
        this.buildTimeLabel = new Label((CharSequence)Localization.format("buildTime", "100"), this.labelStyle);
        this.buildTimeLabel.setFontScale(0.4f);
        this.buildTimeLabel.setColor(MENU_COLOR);
        this.buildTimeLabel.setPosition(0.0f, 240.0f);
        this.showRequirements();
        this.setupBuildQueue();
        this.updateBuildQueue();
    }

    protected void craft() {
        this.showTutorialArrows = false;
        if (MoonBase.getCurrentMission().missionType == Mission.MissionTypes.tutorial) {
            for (ItemButton b : this.itemButtonList) {
                b.removeTutorialArrow();
            }
        }
        if (this.selectedItemData != null) {
            this.craftButtonSoundFx();
            this.itemCrafter.addItemToBuildQueue(this.selectedItemData, true);
        }
        this.updateBuildQueue();
    }

    private void craftButtonSoundFx() {
        int i = this.itemCrafter.buildQueue.size;
        float pitch = 0.5f;
        float ramp = 0.15f;
        Audio.getInstance().playSound("sounds/pop.mp3", 0.5f, pitch += ramp * (float)i);
    }

    private void showRequirements() {
        this.requiresGroup = new Group();
        this.requiresGroup.setPosition(0.0f, 114.0f);
        this.detailGroup.addActor(this.requiresGroup);
        this.requiresLabel = new Label((CharSequence)Localization.get("requires"), this.labelStyle);
        this.requiresLabel.setFontScale(0.4f);
        this.requiresLabel.setColor(MENU_COLOR);
        this.requiresLabel.setPosition(0.0f, 135.0f);
        this.requiresGroup.addActor(this.requiresLabel);
        float spacing = 5.0f;
        for (int x = 0; x < 3; ++x) {
            Group g = new Group();
            g.setPosition((float)(80 * x) + (float)x * spacing, 70.0f);
            this.requiresGroup.addActor(g);
            Image b = new Image(this.skin.getDrawable("btn-item"));
            b.setPosition(0.0f, 0.0f);
            b.setSize(b.getWidth() * 0.75f, b.getHeight() * 0.75f);
            g.addActor(b);
            this.requireBorderList.add(b);
            Image icon = new Image(this.skin.getDrawable("white"));
            icon.setSize(60.0f, 60.0f);
            icon.setPosition(b.getX(1), b.getY(1), 1);
            icon.setColor(1.0f, 1.0f, 1.0f, 0.85f);
            icon.setTouchable(Touchable.disabled);
            g.addActor(icon);
            this.requireIconList.add(icon);
            Label needs = new Label((CharSequence)"9", this.labelStyle);
            needs.setFontScale(0.5f);
            needs.setColor(MENU_COLOR);
            needs.setWidth(76.0f);
            needs.setAlignment(1);
            needs.setPosition(0.0f, -55.0f);
            g.addActor(needs);
            this.requireLabelList.add(needs);
        }
        this.btnCraft = new TextButton(Localization.get("craft"), this.gameScreen.textBtnStyle);
        this.btnCraft.setSize(204.0f, 75.0f);
        this.btnCraft.setPosition(this.detailGroup.getWidth(), 70.0f, 20);
        this.btnCraft.getLabel().setFontScale(0.5f);
        this.requiresGroup.addActor(this.btnCraft);
        this.btnCraft.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                CraftingUI.this.craft();
            }
        });
        this.inventoryFullLabel = new Label((CharSequence)Localization.get("craftingInventoryFull"), this.labelStyle);
        this.inventoryFullLabel.setFontScale(0.4f);
        this.inventoryFullLabel.setColor(1.0f, 0.1f, 0.1f, 1.0f);
        this.inventoryFullLabel.setWidth(this.btnCraft.getWidth());
        this.inventoryFullLabel.setAlignment(1);
        this.inventoryFullLabel.setPosition(this.btnCraft.getX(1), this.btnCraft.getY() + 17.0f, 2);
        this.requiresGroup.addActor(this.inventoryFullLabel);
        Image divider = new Image(this.skin.getDrawable("rect"));
        divider.setColor(MENU_COLOR);
        divider.getColor().a = 0.25f;
        divider.setHeight(3.0f);
        divider.setWidth(this.detailGroup.getWidth());
        divider.setPosition(0.0f, 10.0f);
        this.requiresGroup.addActor(divider);
    }

    private void resetButtonChecks() {
        for (ItemButton i : this.itemButtonList) {
            i.button.setChecked(false);
        }
    }

    private void selectRecipie(int buildIndex, int buttonIndex) {
        this.detailGroup.setVisible(true);
        this.resetButtonChecks();
        Gdx.app.debug("MewnBase", "CraftingUI: Select recipie " + buildIndex);
        this.itemButtonList.get((int)buttonIndex).button.setChecked(true);
        this.selectedItemData = this.itemCrafter.getBuildableItem(buildIndex);
        boolean hasResources = this.itemCrafter.hasResources(this.selectedItemData);
        this.itemCrafter.rememberSelectedBuildable(this.selectedItemData.id);
        if (hasResources && !this.itemCrafter.isQueueFull() && this.itemCrafter.itemsToCollect.size < this.itemCrafter.buildQueueSizeLimit) {
            this.enableButton(this.btnCraft);
        } else {
            this.disableButton(this.btnCraft);
        }
        if (this.selectedItemData != null) {
            String craftingQuantity = "";
            int amount = this.selectedItemData.craftingQuantity;
            if (amount > 1) {
                craftingQuantity = " (x" + amount + ")";
            }
            this.nameLabel.setText(this.selectedItemData.getName() + craftingQuantity);
            String desc = this.selectedItemData.getDesc();
            if (this.selectedItemData.minMeleeDamage != 0 && this.selectedItemData.maxMeleeDamage != 0) {
                desc = desc + "  [#dc884f]" + Localization.get("damage") + ": " + this.selectedItemData.minMeleeDamage + " - " + this.selectedItemData.maxMeleeDamage + "[]";
            }
            this.descLabel.setText(desc);
            this.buildTimeLabel.setText(Localization.format("buildTime", Float.valueOf(this.selectedItemData.craftTime)));
            for (int i = 0; i < 3; ++i) {
                if (i < this.selectedItemData.requires.size()) {
                    RecipieRequirement r = this.selectedItemData.requires.get(i);
                    this.requireBorderList.get(i).setVisible(true);
                    this.requireBorderList.get((int)i).getColor().a = 1.0f;
                    this.requireIconList.get(i).setTouchable(Touchable.enabled);
                    this.requireIconList.get(i).setVisible(true);
                    ItemFactory.getInstance();
                    this.requireIconList.get(i).setDrawable(this.skin.getDrawable(ItemFactory.getItemSprite(r.id)));
                    final String tooltipTextTemp = Item.getName(r.id);
                    this.requireIconList.get(i).addListener(new ClickListener(){

                        @Override
                        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                            CraftingUI.this.gameScreen.hud.createTooltip(tooltipTextTemp);
                        }

                        @Override
                        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                            CraftingUI.this.gameScreen.hud.removeTooltip();
                        }
                    });
                    this.requireLabelList.get(i).setVisible(true);
                    this.requireLabelList.get(i).setText("" + r.quantity);
                    if (MoonBase.getCurrentMission().missionType != Mission.MissionTypes.creative) {
                        int quantity = this.itemCrafter.getPlayerQuantity(r);
                        if (quantity < r.quantity) {
                            this.requireLabelList.get(i).setColor(1.0f, 0.0f, 0.0f, 0.75f);
                        } else {
                            this.requireLabelList.get(i).setColor(MENU_COLOR);
                        }
                    }
                } else {
                    this.requireBorderList.get(i).setVisible(true);
                    this.requireBorderList.get((int)i).getColor().a = 0.5f;
                    this.requireIconList.get(i).setVisible(false);
                    this.requireLabelList.get(i).setVisible(false);
                    this.requireIconList.get(i).setTouchable(Touchable.disabled);
                    this.requireIconList.get(i).clearListeners();
                }
                this.inventoryFullLabel.setVisible(false);
            }
        } else {
            Gdx.app.debug("MewnBase", "CraftingUI: No recipie found?");
        }
    }

    private void selectNone() {
        this.detailGroup.setVisible(false);
        this.disableButton(this.btnCraft);
    }

    private void setupBuildQueue() {
        Table table = new Table();
        table.left().bottom();
        table.setSize(this.detailGroup.getWidth(), 100.0f);
        this.detailGroup.addActor(table);
        this.buildQueueLabel = new Label((CharSequence)Localization.get("buildQueue"), this.labelStyle);
        this.buildQueueLabel.setFontScale(0.4f);
        this.buildQueueLabel.setWrap(true);
        this.buildQueueLabel.setColor(MENU_COLOR);
        this.buildQueueLabel.setAlignment(8, 8);
        table.add(this.buildQueueLabel).fill().width(200.0f).padRight(10.0f);
        Table innerTable = new Table();
        innerTable.add(this.createQueueButton(0, 0.95f, 0.5f)).width(98.0f).pad(8.0f);
        Table queueTable = new Table();
        queueTable.padTop(10.0f);
        queueTable.padBottom(12.0f);
        queueTable.row();
        queueTable.add(this.createQueueButton(1, 0.5f, 0.25f)).minSize(48.0f, 48.0f).space(8.0f).expand().top();
        queueTable.add(this.createQueueButton(2, 0.5f, 0.25f)).minSize(48.0f, 48.0f).space(8.0f).expand().top();
        queueTable.add(this.createQueueButton(3, 0.5f, 0.25f)).minSize(48.0f, 48.0f).space(8.0f).expand().top();
        queueTable.row().colspan(3);
        this.progressBarStyle = new ProgressBar.ProgressBarStyle();
        this.progressBarStyle.knobAfter = this.skin.getDrawable("hud-progress-empty");
        this.progressBarStyle.knobBefore = this.skin.getDrawable("hud-progress-filled");
        this.progressBarStyle.knob = this.skin.getDrawable("hud-progress-knob");
        this.progressBar = new ProgressBar(0.0f, 1.0f, 0.01f, false, this.progressBarStyle);
        this.progressBar.setValue(0.0f);
        queueTable.add(this.progressBar).minHeight(15.0f).fillX().expandY().spaceLeft(4.0f).spaceRight(4.0f);
        innerTable.add(queueTable).fill();
        innerTable.layout();
        table.add(innerTable).expand().right().padRight(10.0f);
        table.layout();
    }

    private Stack createQueueButton(int index, float buttonScale, float fontScale) {
        final int buttonIndex = index;
        ItemButton itemButton = new ItemButton((Menu)this, null);
        itemButton.setScale(buttonScale);
        itemButton.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                CraftingUI.this.cancelBuild(buttonIndex);
            }
        });
        this.queueItemButtonList.add(itemButton);
        Label number = this.addQueueNumber(index + 1, fontScale, itemButton.getWidth(), itemButton.getHeight());
        this.queueItemButtonSlotNumbers.add(number);
        return new Stack(number, itemButton.group);
    }

    private Label addQueueNumber(int num, float fontScale, float width, float height) {
        Label queueNumber = new Label((CharSequence)("" + num), this.headingStyle);
        queueNumber.setFontScale(fontScale);
        queueNumber.setAlignment(1);
        queueNumber.setColor(MENU_COLOR);
        queueNumber.getColor().a = 0.2f;
        queueNumber.setSize(width, height);
        this.queueButtonLabelList.add(queueNumber);
        return queueNumber;
    }

    private void cancelBuild(int buildQueueIndex) {
        this.itemCrafter.cancel(buildQueueIndex);
    }

    private void showCraftGrid() {
        Image scrollBackground = new Image(new NinePatch(this.gameScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4));
        scrollBackground.setSize(12.0f, 420.0f);
        scrollBackground.setPosition(436.0f, 60.0f, 20);
        scrollBackground.getColor().a = 0.5f;
        this.popupGroup.addActor(scrollBackground);
        this.scrollGroup = new Group();
        this.popupGroup.addActor(this.scrollGroup);
        float spacing = 3.0f;
        float iconScaling = 0.8f;
        ItemButton sizeRef = new ItemButton((Menu)this, this.scrollGroup);
        sizeRef.setScale(0.8f);
        int rows = MathUtils.ceil((float)this.unlockedBuildables.size() / 4.0f);
        if (rows < 5) {
            rows = 5;
        }
        sizeRef.group.remove();
        Table table = new Table();
        for (int y = 0; y < rows; ++y) {
            table.row();
            int x = 0;
            while ((float)x < 4.0f) {
                ItemButton i = new ItemButton((Menu)this, null);
                this.itemButtonList.add(i);
                i.setScale(0.8f);
                i.disable();
                i.hideIcon();
                table.add(i.group).width(i.getWidth()).pad(3.0f);
                ++x;
            }
        }
        table.left().bottom();
        this.popupGroup.addActor(table);
        this.updateGrid();
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.baseScreen.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.baseScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)table, scrollStyle);
        this.scroller.setWidth(table.getWidth());
        this.scroller.setHeight(table.getHeight());
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setBounds(0.0f, 0.0f, 360.0f, 420.0f);
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        this.scroller.setPosition(75.0f, 60.0f);
        this.popupGroup.addActor(this.scroller);
        this.scrollGroup.toBack();
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.scroller);
    }

    private void updateGrid() {
        for (int buildableIndex = 0; buildableIndex < this.itemButtonList.size(); ++buildableIndex) {
            ItemButton i = this.itemButtonList.get(buildableIndex);
            i.disable();
            i.hideIcon();
            i.setBorderColor(disabledBorderColor);
        }
        int buildIndex = 0;
        int buttonIndex = 0;
        for (String buildable : this.itemCrafter.buildables) {
            final int fBuildIndex = buildIndex;
            final int fButtonIndex = buttonIndex;
            String itemId = buildable;
            if (this.unlockedBuildables.contains(buildable)) {
                ItemButton i = this.itemButtonList.get(buttonIndex);
                i.clearListeners();
                i.addListener(new ClickListener(){

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        CraftingUI.this.selectRecipie(fBuildIndex, fButtonIndex);
                    }
                });
                i.enable();
                ItemFactory.getInstance();
                String iconName = ItemFactory.getItemSprite(buildable);
                i.setIcon(iconName);
                i.showIcon();
                i.button.setChecked(false);
                this.selectedItemData = this.itemCrafter.getBuildableItem(buildable);
                boolean hasResources = this.itemCrafter.hasResources(this.selectedItemData);
                if (!hasResources) {
                    i.setBorderColor(disabledBorderColor);
                    i.setIconColor(disabledIconColor);
                } else {
                    i.resetColors();
                }
                BaseScreen baseScreen = this.baseScreen;
                if (baseScreen.game.getCurrentMission().missionType == Mission.MissionTypes.tutorial && this.gameScreen.tutorial != null && this.showTutorialArrows) {
                    switch (this.gameScreen.tutorial.getStage()) {
                        case craftFood: {
                            if (!buildable.equals("food")) break;
                            i.showTutorialArrow();
                            break;
                        }
                        case craftShovel: {
                            if (!buildable.equals("shovel")) break;
                            i.showTutorialArrow();
                        }
                    }
                }
                ++buttonIndex;
            }
            ++buildIndex;
        }
    }

    private int getNumOfUnlockedBuildables() {
        int unlockedItems = 0;
        for (String s : this.itemCrafter.buildables) {
            if (!this.isItemUnlocked(s)) continue;
            ++unlockedItems;
        }
        return unlockedItems;
    }

    private boolean isItemUnlocked(String itemId) {
        boolean canBuild = false;
        String techReq = ItemFactory.getInstance().getItemTechReq(itemId);
        if (techReq != null) {
            TechUpgrade tech = this.gameScreen.world.techManager.getTech(techReq);
            if (tech != null) {
                canBuild = tech.unlocked;
            }
        } else {
            canBuild = true;
        }
        return canBuild;
    }

    private int itemIdToBuildIndex(String itemId) {
        for (int i = 0; i < this.itemCrafter.getBuildables().size(); ++i) {
            if (!this.itemCrafter.getBuildableItem((int)i).id.equals(itemId)) continue;
            return i;
        }
        return -1;
    }

    private int itemIdToButtonIndex(String itemId) {
        int buttonIndex = 0;
        for (int i = 0; i < this.itemCrafter.getBuildables().size(); ++i) {
            if (!this.isItemUnlocked(this.itemCrafter.buildables.get(i))) continue;
            if (this.itemCrafter.buildables.get(i).equals(itemId)) {
                return buttonIndex;
            }
            ++buttonIndex;
        }
        return 0;
    }

    public void showProgress() {
        this.progressGroup.setVisible(true);
        this.btnCollect.setDisabled(true);
        this.btnCollect.setTouchable(Touchable.disabled);
        this.btnCancel.setVisible(true);
        this.btnCancel.setDisabled(false);
        this.btnCancel.setTouchable(Touchable.enabled);
        this.scrollGroup.setVisible(false);
        this.requiresGroup.setVisible(false);
        this.detailGroup.setVisible(false);
        this.btnCraft.setVisible(false);
    }

    public void finishAndAutoClose() {
        this.back();
    }

    protected void updateBuildQueue() {
        if (this.itemCrafter.buildQueue.size > 0) {
            this.buildQueueLabel.setText(Localization.format("buildingProgress", this.itemCrafter.getCurrentBuildItem().getName()));
        } else {
            this.buildQueueLabel.setText(Localization.get("buildQueue"));
        }
        this.buildQueueLabel.setColor(Menu.MENU_COLOR);
        if (this.itemCrafter.itemsToCollect.size > 0 && !this.itemCrafter.playerInventory.hasSlotAvailable(this.itemCrafter.itemsToCollect.get(0).getId())) {
            this.buildQueueLabel.setText(Localization.format("cantCollect", this.itemCrafter.itemsToCollect.size));
            this.buildQueueLabel.setColor(redWarningColor);
        }
        for (int i = 0; i < this.queueItemButtonList.size(); ++i) {
            ItemButton ib = this.queueItemButtonList.get(i);
            this.queueButtonLabelList.get(i).setVisible(true);
            ib.setVisible(true);
            ib.icon.setColor(Color.WHITE);
            ib.button.setColor(Color.WHITE);
            ib.setCount(0);
            int availableSlots = this.queueItemButtonList.size() - this.itemCrafter.itemsToCollect.size;
            if (i < availableSlots) {
                if (i < this.itemCrafter.buildQueue.size) {
                    ib.enable();
                    ib.setIcon(this.itemCrafter.buildQueue.get((int)i).sprite);
                    ib.icon.setVisible(true);
                    this.queueItemButtonSlotNumbers.get(i).setVisible(false);
                    continue;
                }
                ib.disable();
                ib.icon.setVisible(false);
                if (i >= this.itemCrafter.buildQueueSizeLimit) {
                    this.queueButtonLabelList.get(i).setVisible(false);
                    ib.setVisible(false);
                }
                this.queueItemButtonSlotNumbers.get(i).setVisible(true);
                continue;
            }
            ib.disable();
            ib.icon.setVisible(false);
            ib.button.setColor(Color.RED);
            ib.setCount(0);
            int collectIndex = Math.abs(i - 3);
            if (collectIndex <= this.itemCrafter.itemsToCollect.size - 1) {
                ib.setIcon(this.itemCrafter.itemsToCollect.get(collectIndex).getSprite());
                ib.icon.setColor(Color.RED);
                ib.icon.setVisible(true);
                ib.enable();
                ib.button.setTouchable(Touchable.disabled);
                ib.setCount(this.itemCrafter.itemsToCollect.get(collectIndex).getAmount());
                this.queueButtonLabelList.get(i).setVisible(false);
            }
            if (i < this.itemCrafter.buildQueueSizeLimit) continue;
            this.queueButtonLabelList.get(i).setVisible(false);
            ib.setVisible(false);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.itemCrafter.isBuilding()) {
            this.progressBar.setValue(this.itemCrafter.getBuildProgress());
        } else {
            this.progressBar.setValue(0.0f);
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        this.updateGrid();
        int startingIndex = this.itemIdToBuildIndex(this.itemCrafter.getLastSelectedItemId());
        int startingButtonIndex = this.itemIdToButtonIndex(this.itemCrafter.getLastSelectedItemId());
        if (startingIndex == -1) {
            this.selectNone();
        } else {
            this.selectRecipie(startingIndex, startingButtonIndex);
        }
        if (o == "updatedBuildQueue") {
            // empty if block
        }
        if (o == "finishedBuilding") {
            this.itemCrafter.collectItem();
        }
        if (o == "finishAndAutoClose") {
            this.finishAndAutoClose();
        }
        this.updateBuildQueue();
    }
}

