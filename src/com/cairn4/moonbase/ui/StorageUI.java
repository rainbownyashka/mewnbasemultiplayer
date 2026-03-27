/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.tiles.StorageColorOptions;
import com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior;
import com.cairn4.moonbase.tiles.behaviors.StorageCrateCloseCallback;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.ItemButton;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import com.cairn4.moonbase.ui.StorageCrateColorCallback;
import java.util.ArrayList;

public class StorageUI
extends Popup
implements Telegraph {
    public GameScreen gameScreen;
    private ItemStorageBehavior itemStorageBehavior;
    private PlayerInventory playerInventory;
    private ArrayList<ItemButton> inventoryButtonList = new ArrayList();
    private ArrayList<ItemButton> storageButtonList = new ArrayList();
    private Group inventoryButtonGroup;
    private Group storageButtonGroup;
    public StorageCrateCloseCallback closeCallback;
    public StorageCrateColorCallback storageCrateColorCallback;
    private ButtonGroup colorButtonGroup;
    private Array<Button> buttonList = new Array();

    public StorageUI(GameScreen gameScreen, ItemStorageBehavior itemStorageBehavior, PlayerInventory playerInventory) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        MessageManager.getInstance().addListener(this, 32);
        MessageManager.getInstance().addListener(this, 49);
        this.itemStorageBehavior = itemStorageBehavior;
        this.playerInventory = playerInventory;
        this.setup();
        this.show();
        gameScreen.hud.hudNotifications.reparentGroup(this.stage.getRoot());
    }

    @Override
    public void back() {
        super.back();
        if (this.closeCallback != null) {
            this.closeCallback.closeAnim();
        }
    }

    @Override
    public void hide() {
        MessageManager.getInstance().removeListener((Telegraph)this, 32);
        MessageManager.getInstance().removeListener((Telegraph)this, 49);
        super.hide();
        this.gameScreen.hud.hudNotifications.resetGroup();
    }

    @Override
    protected void setup() {
        super.setup();
        this.panelBg.setSize(794.0f, 560.0f);
        float xOffset = ((float)MoonBase.SCREEN_WIDTH - this.panelBg.getWidth()) / 2.0f;
        float yOffset = ((float)MoonBase.SCREEN_HEIGHT - this.panelBg.getHeight()) / 2.0f + 20.0f;
        this.popupGroup.setOrigin(this.panelBg.getWidth() / 2.0f, this.panelBg.getHeight() / 2.0f + 15.0f);
        this.popupGroup.setPosition(xOffset, yOffset);
        this.btnClose.setPosition(this.panelBg.getWidth() - 35.0f, this.panelBg.getHeight() - 45.0f, 18);
        this.titleLabel.setText(Localization.get("inventory"));
        this.titleLabel.setFontScale(0.5f);
        this.titleLabel.setTouchable(Touchable.disabled);
        this.titleLabel.setPosition(78.0f, this.panelBg.getHeight() - 43.0f, 10);
        this.popupGroup.addActor(this.titleLabel);
        Label storageLabel = new Label((CharSequence)Localization.get("storage"), this.labelStyle);
        storageLabel.setFontScale(0.5f);
        storageLabel.setPosition(384.0f, this.panelBg.getHeight() - 43.0f, 10);
        storageLabel.setTouchable(Touchable.disabled);
        this.popupGroup.addActor(storageLabel);
        this.inventoryButtonGroup = new Group();
        this.storageButtonGroup = new Group();
        this.inventoryButtonGroup.setPosition(75.0f, 90.0f);
        this.inventoryButtonGroup.setSize(300.0f, 340.0f);
        this.popupGroup.addActor(this.inventoryButtonGroup);
        this.storageButtonGroup.setPosition(380.0f, 90.0f);
        this.storageButtonGroup.setSize(334.0f, 340.0f);
        this.popupGroup.addActor(this.storageButtonGroup);
        this.setupButtonGrids();
        this.addColorButtons();
        Label shiftClickLabel = new Label((CharSequence)Localization.get("storage_shiftClick_hint2"), this.labelStyle);
        shiftClickLabel.setFontScale(0.28f);
        shiftClickLabel.setAlignment(1, 4);
        shiftClickLabel.setWidth(250.0f);
        shiftClickLabel.setWrap(true);
        shiftClickLabel.setTouchable(Touchable.disabled);
        shiftClickLabel.setPosition(203.0f, 34.0f, 4);
        shiftClickLabel.setColor(Menu.MENU_COLOR);
        shiftClickLabel.getColor().a = 0.6f;
        this.popupGroup.addActor(shiftClickLabel);
    }

    private void setupButtonGrids() {
        ItemButton i;
        float spacing = 3.0f;
        float iconScaling = 0.8f;
        Table inventoryTable = new Table();
        inventoryTable.setFillParent(true);
        inventoryTable.left();
        this.inventoryButtonGroup.addActor(inventoryTable);
        for (int y = 0; y < 4; ++y) {
            inventoryTable.row();
            for (int x = 0; x < 3; ++x) {
                i = new ItemButton((Menu)this, null);
                this.inventoryButtonList.add(i);
                i.setScale(0.8f);
                i.disable();
                i.hideIcon();
                inventoryTable.add(i.group).width(i.getWidth()).pad(spacing);
            }
        }
        Table storageTable = new Table();
        storageTable.setFillParent(true);
        storageTable.left();
        this.storageButtonGroup.addActor(storageTable);
        for (int s = 0; s < this.itemStorageBehavior.maxItems; ++s) {
            if (s % 4 == 0) {
                storageTable.row();
            }
            i = new ItemButton((Menu)this, null);
            this.storageButtonList.add(i);
            i.setScale(0.8f);
            i.disable();
            i.hideIcon();
            storageTable.add(i.group).width(i.getWidth()).pad(spacing);
        }
        this.updateGrid();
    }

    private void updateGrid() {
        int buildableIndex = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 3; ++x) {
                ItemButton b = this.inventoryButtonList.get(buildableIndex);
                if (buildableIndex < this.playerInventory.getItemList().size()) {
                    b.enable();
                    String id = this.playerInventory.getItemList().get(buildableIndex).getId();
                    b.setIcon(ItemFactory.getItemSprite(id));
                    b.setCount(this.playerInventory.getItemList().get(buildableIndex).getAmount());
                    b.updateDurability(this.playerInventory.getItemList().get((int)buildableIndex).item);
                    b.clearListeners();
                    final int invToStorageIndex = buildableIndex;
                    b.addListener(new ClickListener(0){

                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            if (Gdx.input.isKeyPressed(59) || Gdx.input.isKeyPressed(60)) {
                                StorageUI.this.moveToStorage(invToStorageIndex, false);
                            } else {
                                StorageUI.this.moveToStorage(invToStorageIndex, true);
                            }
                        }

                        @Override
                        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                            StorageUI.this.createTooltip(((StorageUI)StorageUI.this).playerInventory.getItemList().get((int)invToStorageIndex).item.getName());
                        }

                        @Override
                        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                            StorageUI.this.removeTooltip();
                        }
                    });
                    b.addListener(new ClickListener(1){

                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            if (invToStorageIndex < StorageUI.this.playerInventory.getItemList().size()) {
                                ItemStack i = StorageUI.this.playerInventory.getItemList().get(invToStorageIndex);
                                if (i.item.canUseFromInventoryBar()) {
                                    i.use(StorageUI.this.playerInventory.getPlayer());
                                    StorageUI.this.playerInventory.removeEmpties();
                                    StorageUI.this.playerInventory.getPlayer().inventoryUpdate();
                                    StorageUI.this.updateGrid();
                                }
                            }
                        }
                    });
                } else {
                    b.setEquippedIcon(false);
                    b.disable();
                    b.hideIcon();
                    b.setCount(0);
                    b.updateDurability(null);
                }
                ++buildableIndex;
            }
        }
        buildableIndex = 0;
        for (int s = 0; s < this.itemStorageBehavior.maxItems; ++s) {
            ItemButton b = this.storageButtonList.get(buildableIndex);
            if (buildableIndex < this.itemStorageBehavior.getItemList().size()) {
                b.clearListeners();
                b.enable();
                b.setIcon(this.itemStorageBehavior.getItemList().get((int)buildableIndex).item.getSprite());
                b.setCount(this.itemStorageBehavior.getItemList().get(buildableIndex).getAmount());
                b.updateDurability(this.itemStorageBehavior.getItemList().get((int)buildableIndex).item);
                final int storageToInvIndex = buildableIndex;
                b.addListener(new ClickListener(0){

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (Gdx.input.isKeyPressed(59) || Gdx.input.isKeyPressed(60)) {
                            StorageUI.this.moveToInventory(storageToInvIndex, false);
                        } else {
                            StorageUI.this.moveToInventory(storageToInvIndex, true);
                        }
                    }

                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        if (storageToInvIndex < StorageUI.this.itemStorageBehavior.getItemList().size()) {
                            StorageUI.this.createTooltip(((StorageUI)StorageUI.this).itemStorageBehavior.getItemList().get((int)storageToInvIndex).item.getName());
                        }
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        StorageUI.this.removeTooltip();
                    }
                });
                b.addListener(new ClickListener(1){

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (storageToInvIndex < StorageUI.this.itemStorageBehavior.getItemList().size()) {
                            ItemStack i = StorageUI.this.itemStorageBehavior.getItemList().get(storageToInvIndex);
                            if (i.item.canUseFromInventoryBar()) {
                                i.use(StorageUI.this.playerInventory.getPlayer());
                                StorageUI.this.itemStorageBehavior.removeEmpties();
                                StorageUI.this.updateGrid();
                            }
                        } else {
                            MoonBase.error("storageToInvIndex out of bounds");
                        }
                    }
                });
            } else {
                b.setEquippedIcon(false);
                b.disable();
                b.hideIcon();
                b.setCount(0);
                b.updateDurability(null);
            }
            ++buildableIndex;
        }
    }

    private void addColorButtons() {
        final Table colorButtonTable = new Table();
        colorButtonTable.setFillParent(true);
        colorButtonTable.bottom();
        colorButtonTable.setColor(1.0f, 1.0f, 1.0f, 0.5f);
        colorButtonTable.addListener(new ClickListener(){

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                colorButtonTable.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                colorButtonTable.setColor(1.0f, 1.0f, 1.0f, 0.5f);
            }
        });
        this.colorButtonGroup = new ButtonGroup();
        this.colorButtonGroup.setMaxCheckCount(1);
        for (int ci = 0; ci < StorageColorOptions.colors.length; ++ci) {
            Stack stack = new Stack();
            Button cb = new Button(this.baseScreen.minorButtonStyle);
            this.colorButtonGroup.add(cb);
            this.buttonList.add(cb);
            final int index = ci;
            cb.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (StorageUI.this.storageCrateColorCallback != null) {
                        StorageUI.this.storageCrateColorCallback.setColor(StorageColorOptions.colors[index], index);
                    }
                }
            });
            stack.add(cb);
            Table inner = new Table();
            Image swatch = new Image(this.skin.getDrawable("rect"));
            swatch.setColor(StorageColorOptions.colors[ci]);
            swatch.setTouchable(Touchable.disabled);
            inner.add(swatch).pad(8.0f);
            stack.add(inner);
            colorButtonTable.add(stack).width(30.0f).height(30.0f).space(2.0f);
        }
        colorButtonTable.layout();
        this.storageButtonGroup.addActor(colorButtonTable);
        colorButtonTable.setY(-35.0f);
    }

    public void updateColorSwatch() {
        if (this.storageCrateColorCallback != null) {
            int colorIndex = this.storageCrateColorCallback.getColorIndex();
            for (int c = 0; c < StorageColorOptions.colors.length; ++c) {
                if (colorIndex != c) continue;
                this.buttonList.get(c).setChecked(true);
            }
        }
    }

    private void moveToInventory(int storageIndex, boolean moveStack) {
        if (storageIndex < this.itemStorageBehavior.getItemList().size()) {
            int spaceAvailableForItem = this.playerInventory.getSpaceAvailableFor(this.itemStorageBehavior.getItemList().get(storageIndex).getId());
            if (spaceAvailableForItem > 0) {
                if (moveStack) {
                    ItemStack stored = this.itemStorageBehavior.getItemList().get(storageIndex);
                    if (stored.getAmount() <= spaceAvailableForItem) {
                        this.playerInventory.add(this.itemStorageBehavior.getItemList().get(storageIndex), false);
                        this.itemStorageBehavior.remove(storageIndex);
                    } else {
                        int moveAmount = spaceAvailableForItem;
                        ItemStack newStack = new ItemStack(stored.getId(), moveAmount);
                        this.playerInventory.add(newStack, false);
                        stored.remove(moveAmount);
                    }
                    this.itemStorageBehavior.removeEmpties();
                } else {
                    Item tempItem = Item.copy(this.itemStorageBehavior.getItemList().get((int)storageIndex).item);
                    this.playerInventory.add(new ItemStack(tempItem), false);
                    this.itemStorageBehavior.removeSingle(storageIndex);
                }
                this.updateGrid();
            } else {
                this.gameScreen.hud.hudNotifications.newMessage(Localization.format("inventory_full", new Object[0]), Color.RED);
            }
        }
    }

    private void moveToStorage(int inventoryIndex, boolean moveStack) {
        if (inventoryIndex < this.playerInventory.getItemList().size()) {
            int spaceAvailableForItem = this.itemStorageBehavior.getSpaceAvailableFor(this.playerInventory.getItemList().get(inventoryIndex).getId());
            if (spaceAvailableForItem > 0) {
                if (moveStack) {
                    ItemStack playerItem = this.playerInventory.getItemList().get(inventoryIndex);
                    if (playerItem.getAmount() <= spaceAvailableForItem) {
                        this.itemStorageBehavior.addTo(this.playerInventory.getItemList().get(inventoryIndex));
                        this.playerInventory.removeItem(inventoryIndex);
                    } else {
                        int moveAmount = spaceAvailableForItem;
                        ItemStack newStack = new ItemStack(playerItem.getId(), moveAmount);
                        this.itemStorageBehavior.addTo(newStack);
                        playerItem.remove(moveAmount);
                    }
                    this.playerInventory.removeEmpties();
                } else {
                    this.itemStorageBehavior.addSingle(this.playerInventory.getItemList().get((int)inventoryIndex).item);
                    this.playerInventory.consumeItem(this.playerInventory.getItemList().get(inventoryIndex), 1);
                }
                this.updateGrid();
            } else {
                this.gameScreen.hud.hudNotifications.newMessage(Localization.format("storage_full", new Object[0]), Color.RED);
            }
        }
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        this.updateGrid();
        return false;
    }
}

