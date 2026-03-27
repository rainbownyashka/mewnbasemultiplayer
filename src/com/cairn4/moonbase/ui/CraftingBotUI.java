/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.RecipieRequirement;
import com.cairn4.moonbase.tiles.CraftingBot;
import com.cairn4.moonbase.tiles.behaviors.CraftingBotBehavior;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.CraftingBotItemPicker;
import com.cairn4.moonbase.ui.CraftingBotPickerCallback;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.ItemButton;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import com.cairn4.moonbase.ui.Tooltip;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class CraftingBotUI
extends Popup
implements Observer {
    CraftingBot bot;
    ScrollPane scroller;
    Table jobTable;
    ArrayList<WorkOrderUI> workOrderUIList = new ArrayList();
    Button.ButtonStyle deleteButtonStyle;
    BitmapFont nameFieldFont;
    Label.LabelStyle inputFieldLabelStyle;
    TextField.TextFieldStyle textFieldStyle;
    private Button.ButtonStyle minusBtnStyle;
    private Button.ButtonStyle plusBtnStyle;
    private final NinePatchDrawable alt_npd_up;
    private final NinePatchDrawable input_npd;
    private TextButton addJobButton;

    public CraftingBotUI(BaseScreen baseScreen, CraftingBot bot) {
        super(baseScreen);
        this.bot = bot;
        this.deleteButtonStyle = new Button.ButtonStyle();
        this.deleteButtonStyle.up = this.skin.getDrawable("remove-icon");
        this.deleteButtonStyle.over = this.skin.getDrawable("remove-icon-hover");
        this.deleteButtonStyle.down = this.skin.getDrawable("remove-icon-pressed");
        this.minusBtnStyle = new Button.ButtonStyle();
        this.minusBtnStyle.up = this.skin.getDrawable("map/map-zoom-out");
        this.minusBtnStyle.over = this.skin.getDrawable("map/map-zoom-out-hover");
        this.minusBtnStyle.down = this.skin.getDrawable("map/map-zoom-out-pressed");
        this.plusBtnStyle = new Button.ButtonStyle();
        this.plusBtnStyle.up = this.skin.getDrawable("map/map-zoom-in");
        this.plusBtnStyle.over = this.skin.getDrawable("map/map-zoom-in-hover");
        this.plusBtnStyle.down = this.skin.getDrawable("map/map-zoom-in-pressed");
        NinePatch selected_np = new NinePatch(this.skin.getRegion("slider-bg"), 5, 5, 5, 5);
        NinePatchDrawable selected_npd = new NinePatchDrawable(selected_np);
        NinePatch alt_np_up = new NinePatch(this.skin.getRegion("btn-item"), 16, 16, 16, 16);
        this.alt_npd_up = new NinePatchDrawable(alt_np_up);
        NinePatch input_np = new NinePatch(this.skin.getRegion("btn-item"), 16, 16, 16, 16);
        input_np.scale(0.8f, 0.8f);
        this.input_npd = new NinePatchDrawable(input_np);
        this.input_npd.setLeftWidth(18.0f);
        NinePatch alt_np_active = new NinePatch(this.skin.getRegion("btn-item-active"), 16, 16, 16, 16);
        alt_np_active.scale(0.8f, 0.8f);
        NinePatchDrawable alt_npd_active = new NinePatchDrawable(alt_np_active);
        alt_npd_active.setLeftWidth(18.0f);
        this.nameFieldFont = AssetManagerSingleton.getInstance().get("smallfont1.fnt", BitmapFont.class);
        this.nameFieldFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        String cs = "0123456789";
        this.nameFieldFont.setFixedWidthGlyphs(cs);
        this.nameFieldFont.getData().markupEnabled = true;
        this.inputFieldLabelStyle = new Label.LabelStyle(this.nameFieldFont, Color.WHITE);
        this.textFieldStyle = new TextField.TextFieldStyle();
        this.textFieldStyle.background = this.input_npd;
        this.textFieldStyle.focusedBackground = alt_npd_active;
        this.textFieldStyle.font = this.nameFieldFont;
        this.textFieldStyle.font.getData().setScale(0.6f);
        this.textFieldStyle.cursor = this.skin.getDrawable("hud-progress-knob");
        this.textFieldStyle.selection = selected_npd;
        this.textFieldStyle.fontColor = Menu.MENU_COLOR;
        this.textFieldStyle.focusedFontColor = Color.WHITE;
        this.textFieldStyle.messageFontColor = Color.WHITE;
        this.textFieldStyle.messageFont = this.nameFieldFont;
        this.setup();
        this.show();
    }

    @Override
    public void setup() {
        super.setup();
        this.setSize(930.0f, 610.0f);
        this.setTitle("item_autocrafter-builder");
        this.bot.craftingBotBehavior.addObserver(this);
        this.jobTable = new Table();
        this.jobTable.top();
        this.addWorkOrders();
        this.setupScroller();
        if (this.bot.craftingBotBehavior.workQueue.size() <= 2) {
            this.scroller.scrollTo(0.0f, 0.0f, this.jobTable.getWidth(), 0.0f, true, false);
        }
        this.addJobButton = new TextButton(Localization.get("autocrafter_addJob"), this.baseScreen.textBtnStyle);
        this.addJobButton.getLabel().setFontScale(0.5f);
        this.addJobButton.setSize(304.0f, 75.0f);
        this.addJobButton.setPosition(this.scroller.getX(1), 45.0f, 4);
        this.popupGroup.addActor(this.addJobButton);
        this.addJobButton.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                CraftingBotBehavior.WorkOrder newWo = CraftingBotUI.this.bot.craftingBotBehavior.addWorkJob("metal", 0);
                CraftingBotUI.this.scroller.scrollTo(CraftingBotUI.this.jobTable.getWidth(), 0.0f, CraftingBotUI.this.jobTable.getWidth(), 0.0f, false, false);
                if (newWo != null) {
                    CraftingBotItemPicker picker = new CraftingBotItemPicker(CraftingBotUI.this.baseScreen, CraftingBotUI.this.bot.craftingBotBehavior, newWo);
                    picker.addCallback(new CraftingBotPickerCallback(){

                        @Override
                        public void itemPicked(String id) {
                            WorkOrderUI woui = CraftingBotUI.this.workOrderUIList.get(CraftingBotUI.this.workOrderUIList.size() - 1);
                        }
                    });
                    CraftingBotUI.this.baseScreen.showMenu(picker);
                }
            }
        });
    }

    private void addWorkOrders() {
        for (CraftingBotBehavior.WorkOrder wo : this.bot.craftingBotBehavior.workQueue) {
            WorkOrderUI woui = new WorkOrderUI(this, wo);
            this.workOrderUIList.add(woui);
        }
    }

    private void setupScroller() {
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.baseScreen.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.baseScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        scrollStyle.hScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.hScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)this.jobTable, scrollStyle);
        this.scroller.setWidth(this.jobTable.getWidth());
        this.scroller.setHeight(this.jobTable.getHeight());
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setBounds(0.0f, 0.0f, this.panelBg.getWidth() - 150.0f, 360.0f);
        this.scroller.setFlingTime(1.0f);
        this.scroller.setScrollingDisabled(false, true);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(true);
        this.scroller.setSmoothScrolling(true);
        this.scroller.setPosition(85.0f, 125.0f);
        this.popupGroup.addActor(this.scroller);
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.scroller);
    }

    @Override
    public void update(float delta) {
        for (WorkOrderUI woui : this.workOrderUIList) {
            woui.update(delta);
        }
        if (this.bot.craftingBotBehavior.workQueue.size() < CraftingBotBehavior.maxJobs - 1) {
            this.addJobButton.setDisabled(false);
            this.addJobButton.setTouchable(Touchable.enabled);
        } else {
            this.addJobButton.setDisabled(true);
            this.addJobButton.setTouchable(Touchable.disabled);
        }
    }

    @Override
    public void back() {
        this.stage.setKeyboardFocus(null);
        this.textFieldStyle.font.getData().setScale(1.0f);
        this.bot.craftingBotBehavior.deleteObserver(this);
        super.back();
    }

    @Override
    public void returned() {
        this.popupGroup.setVisible(true);
        this.tintBg.setVisible(true);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o.equals(CraftingBotBehavior.ADD_REMOVE_JOB_MSG)) {
            System.out.println("craftingbotui received ADD_REMOVE_JOB_MSG");
            this.jobTable.clear();
            this.addWorkOrders();
            this.scroller.layout();
        }
    }

    class WorkOrderUI {
        CraftingBotUI baseUI;
        CraftingBotBehavior.WorkOrder workOrder;
        Table table;
        Label nameLabel;
        TextField quantityInput;
        Label statusLabel;
        Label totalStoredLabel;
        Button deleteButton;
        private Image statusIcon;
        public final ItemButton itemButton;

        public WorkOrderUI(CraftingBotUI baseUI, CraftingBotBehavior.WorkOrder workOrder) {
            this.baseUI = baseUI;
            this.workOrder = workOrder;
            this.table = new Table();
            this.table.pad(8.0f);
            this.table.top();
            this.table.setBackground(CraftingBotUI.this.alt_npd_up);
            final CraftingBotBehavior.WorkOrder finalWo = workOrder;
            this.deleteButton = new Button(CraftingBotUI.this.deleteButtonStyle);
            this.table.add(this.deleteButton).width(24.0f).height(24.0f).top().right().fillX().expandX();
            this.deleteButton.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    finalWo.setReadyToRemove(true);
                }
            });
            this.table.row();
            Stack iconStack = new Stack();
            final CraftingBotBehavior.WorkOrder woFinal = workOrder;
            this.itemButton = new ItemButton((Menu)this.baseUI, 0.8f);
            this.itemButton.setIcon(ItemFactory.getItemSprite(workOrder.itemId));
            final ItemButton finalItemButton = this.itemButton;
            this.itemButton.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    CraftingBotItemPicker picker = new CraftingBotItemPicker(CraftingBotUI.this.baseScreen, CraftingBotUI.this.bot.craftingBotBehavior, woFinal);
                    picker.addCallback(new CraftingBotPickerCallback(){

                        @Override
                        public void itemPicked(String id) {
                            finalItemButton.setIcon(ItemFactory.getItemSprite(id));
                            WorkOrderUI.this.quantityInput.setText("0");
                        }
                    });
                    CraftingBotUI.this.baseScreen.showMenu(picker);
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    WorkOrderUI.this.createItemTooltip(woFinal);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    ((GameScreen)CraftingBotUI.this.baseScreen).hud.removeTooltip();
                    ((GameScreen)CraftingBotUI.this.baseScreen).hud.hideProgressBarIncrease();
                }
            });
            this.itemButton.button.setFillParent(true);
            this.table.add(this.itemButton.group).width(120.0f).height(120.0f).spaceBottom(10.0f);
            this.itemButton.icon.setSize(100.0f, 100.0f);
            this.itemButton.icon.setPosition(60.0f, 60.0f, 1);
            this.itemButton.layout();
            this.table.row();
            Table quantityTable = new Table();
            Button minusButton = new Button(CraftingBotUI.this.minusBtnStyle);
            minusButton.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    CraftingBotUI.this.bot.craftingBotBehavior.changeQuantity(woFinal, -10);
                }
            });
            quantityTable.add(minusButton).width(30.0f).height(15.0f).center();
            Stack s = new Stack();
            this.quantityInput = new TextField("", CraftingBotUI.this.textFieldStyle);
            this.quantityInput.setMaxLength(4);
            this.quantityInput.setText("" + workOrder.quantity);
            this.quantityInput.setAlignment(1);
            this.quantityInput.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
            this.quantityInput.setFocusTraversal(true);
            this.quantityInput.setTextFieldListener((textField, c) -> {
                int originalQuantity = workOrder.quantity;
                try {
                    workOrder.quantity = Integer.valueOf(textField.getText());
                }
                catch (Exception e) {
                    textField.setText("" + originalQuantity);
                }
            });
            s.add(this.quantityInput);
            s.pack();
            quantityTable.add(s).width(100.0f).height(55.0f).space(5.0f);
            Button plusButton = new Button(CraftingBotUI.this.plusBtnStyle);
            plusButton.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    CraftingBotUI.this.bot.craftingBotBehavior.changeQuantity(woFinal, 10);
                }
            });
            quantityTable.add(plusButton).width(30.0f).height(30.0f).center();
            this.table.add(quantityTable).fillX().center().padLeft(10.0f).padRight(10.0f);
            this.table.row();
            this.totalStoredLabel = new Label((CharSequence)"10 Stored", CraftingBotUI.this.labelStyle);
            this.totalStoredLabel.setWrap(true);
            this.totalStoredLabel.setAlignment(1);
            this.totalStoredLabel.setFontScale(0.4f);
            this.totalStoredLabel.setTouchable(Touchable.disabled);
            this.table.add(this.totalStoredLabel).fillX().space(5.0f).spaceBottom(10.0f);
            this.table.row();
            Image div = new Image(baseUI.skin.getDrawable("credits-divider"));
            div.setColor(1.0f, 1.0f, 1.0f, 0.4f);
            this.table.add(div).fillX().height(4.0f).space(5.0f);
            this.table.row();
            this.statusLabel = new Label((CharSequence)"Status", CraftingBotUI.this.labelStyle);
            this.statusLabel.setWrap(true);
            this.statusLabel.setAlignment(1);
            this.statusLabel.setFontScale(0.3f);
            this.statusLabel.setTouchable(Touchable.disabled);
            this.table.add(this.statusLabel).center().space(5.0f).fillY().fillX().expandY().padBottom(10.0f);
            baseUI.jobTable.add(this.table).width(230.0f).height(340.0f).left().spaceRight(10.0f);
        }

        private void createItemTooltip(CraftingBotBehavior.WorkOrder woFinal) {
            String statusText = Localization.get("item_" + woFinal.itemId);
            boolean sad = false;
            String ingredients = Localization.get("requires");
            ItemData id = ItemFactory.getItemData(woFinal.itemId);
            for (RecipieRequirement r : id.requires) {
                ingredients = ingredients + "\n";
                String i = Localization.get("item_" + r.id) + " x" + r.quantity + "";
                int total = CraftingBotUI.this.bot.craftingBotBehavior.getTotalStored(r.id);
                if (total < r.quantity) {
                    i = "[#ff0000]" + i + "[]";
                }
                ingredients = ingredients + i;
            }
            Tooltip.TooltipStyle style = new Tooltip.TooltipStyle(0.4f, 0.4f);
            ((GameScreen)CraftingBotUI.this.baseScreen).hud.createTooltip(statusText, ingredients, style);
        }

        public void update(float delta) {
            if (this.itemButton != null) {
                this.itemButton.setIcon(ItemFactory.getItemSprite(this.workOrder.itemId));
            }
            boolean done = false;
            boolean sad = false;
            String statusText = "";
            this.totalStoredLabel.setText(Localization.format("storedQuantity", this.workOrder.totalStored));
            if (this.workOrder.satisfied) {
                statusText = statusText + Localization.get("autocrafter_done");
                done = true;
            } else {
                if (this.workOrder.inProgress) {
                    statusText = statusText + Localization.get("autocrafter_working");
                }
                if (this.workOrder.notEnoughIngredients) {
                    statusText = Localization.get("autocrafter_missingIngredients");
                    sad = true;
                }
                if (this.workOrder.blockedByStorage) {
                    statusText = Localization.get("autocrafter_noStorage");
                    sad = true;
                }
                if (this.workOrder.blockedByNoAvailableCrafter) {
                    statusText = Localization.get("autocrafter_missingCrafter");
                    sad = true;
                }
            }
            this.statusLabel.setText(statusText);
            if (done) {
                this.statusLabel.setColor(Color.GREEN);
            }
            if (sad) {
                this.statusLabel.setColor(Color.RED);
            } else {
                this.statusLabel.setColor(Color.WHITE);
            }
            this.table.layout();
        }

        public void remove() {
            this.table.remove();
        }
    }
}

