/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.ItemButton;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import java.util.ArrayList;

public class InventoryUI
extends Popup {
    GameScreen gameScreen;
    Group iconGroup;
    Group detailGroup;
    Label nameLabel;
    Label descLabel;
    TextButton btnDrop;
    PlayerInventory playerInventory;
    ItemData selectedItemData;
    int lastItemIndex = 0;
    ArrayList<Image> itemIconList = new ArrayList();
    ArrayList<Label> itemCountList = new ArrayList();
    ArrayList<Image> equippedIconList = new ArrayList();
    ArrayList<ItemButton> itemButtonList = new ArrayList();

    public InventoryUI(GameScreen gameScreen, PlayerInventory playerInventory) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        this.playerInventory = playerInventory;
        this.setup();
        this.updateGrid();
        this.selectItem(playerInventory.getSelectedIndex());
        this.show();
    }

    @Override
    protected void setup() {
        super.setup();
        this.titleLabel.setText(Localization.get("inventory"));
        super.setSize(794.0f, 530.0f);
        this.iconGroup = new Group();
        this.iconGroup.setPosition(75.0f, 70.0f);
        this.popupGroup.addActor(this.iconGroup);
        this.setupItemGrid();
        this.detailGroup = new Group();
        this.detailGroup.setPosition(360.0f, 120.0f);
        this.detailGroup.setSize(360.0f, 270.0f);
        this.popupGroup.addActor(this.detailGroup);
        this.nameLabel = new Label((CharSequence)"Solar Panels", this.labelStyle);
        this.nameLabel.setFontScale(0.55f);
        this.nameLabel.setWrap(false);
        this.nameLabel.setPosition(0.0f, this.detailGroup.getHeight() - 48.0f);
        this.detailGroup.addActor(this.nameLabel);
        this.nameLabel.setTouchable(Touchable.disabled);
        this.descLabel = new Label((CharSequence)"Generates base power lorem ipsum dolor sit amet, consectetur.", this.labelStyle);
        this.descLabel.setFontScale(0.4f);
        this.descLabel.setColor(MENU_COLOR);
        this.descLabel.getColor().a = 0.65f;
        this.descLabel.setWrap(true);
        this.descLabel.setWidth(this.detailGroup.getWidth());
        this.descLabel.setAlignment(10);
        this.descLabel.setPosition(0.0f, this.nameLabel.getY() - 60.0f);
        this.detailGroup.addActor(this.descLabel);
        this.btnDrop = new TextButton(Localization.get("inventory_drop"), this.gameScreen.textBtnStyle);
        this.btnDrop.setSize(194.0f, 75.0f);
        this.btnDrop.setPosition(this.panelBg.getWidth() - 75.0f, 59.0f, 20);
        this.btnDrop.getLabel().setFontScale(0.5f);
        this.popupGroup.addActor(this.btnDrop);
        this.btnDrop.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                InventoryUI.this.dropItem();
            }
        });
    }

    private void setupItemGrid() {
        float spacing = 3.0f;
        Table table = new Table();
        table.bottom().left();
        this.iconGroup.addActor(table);
        for (int y = 0; y < 4; ++y) {
            table.row();
            for (int x = 0; x < 3; ++x) {
                ItemButton i = new ItemButton((Menu)this, null);
                i.setScale(0.8f);
                this.itemButtonList.add(i);
                i.disable();
                table.add(i.group).width(i.getWidth()).pad(spacing);
            }
        }
    }

    private void updateGrid() {
        int buildableIndex = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 3; ++x) {
                ItemButton b = this.itemButtonList.get(buildableIndex);
                if (buildableIndex < this.playerInventory.getItemList().size()) {
                    b.enable();
                    b.setIcon(this.playerInventory.getItemList().get(buildableIndex).getSprite());
                    b.setCount(this.playerInventory.getItemList().get(buildableIndex).getAmount());
                    final int buildIndex = buildableIndex;
                    b.addListener(new ClickListener(){

                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            InventoryUI.this.selectItem(buildIndex);
                        }

                        @Override
                        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                            InventoryUI.this.createTooltip(InventoryUI.this.playerInventory.getItemList().get(buildIndex).getName());
                        }

                        @Override
                        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                            InventoryUI.this.removeTooltip();
                        }
                    });
                    if (this.playerInventory.getItemList().get(buildableIndex) == this.playerInventory.getEquippedItem()) {
                        b.setEquippedIcon(true);
                    } else {
                        b.setEquippedIcon(false);
                    }
                } else {
                    b.setEquippedIcon(false);
                    b.disable();
                    b.hideIcon();
                    b.setCount(0);
                }
                ++buildableIndex;
            }
        }
        this.selectItem(this.playerInventory.getSelectedIndex());
    }

    private void selectItem(int buildIndex) {
        if (buildIndex < this.playerInventory.getItemList().size()) {
            System.out.println("Select item " + buildIndex);
            this.lastItemIndex = buildIndex;
            this.playerInventory.setSelection(buildIndex);
            ItemStack stack = this.playerInventory.getItemList().get(buildIndex);
            String name = stack.getName();
            String desc = stack.item.getDesc();
            this.nameLabel.setText(name);
            this.descLabel.setText(desc);
            for (int ib = 0; ib < this.itemButtonList.size(); ++ib) {
                this.itemButtonList.get(ib).setEquippedIcon(ib == buildIndex);
            }
            this.enableButton(this.btnDrop);
        } else {
            this.nameLabel.setText("Empty");
            this.descLabel.setText("Go find some stuff!");
            this.disableButton(this.btnDrop);
        }
    }

    public void useItem() {
        this.playerInventory.useItem();
        this.updateGrid();
    }

    private void dropItem() {
        this.playerInventory.dropItem();
        this.updateGrid();
    }

    private void equipItem() {
        this.playerInventory.equipItem();
        this.updateGrid();
    }

    @Override
    public void handleInput() {
        if (PlayerInput.wasPressed(5)) {
            this.back();
        }
        super.handleInput();
    }
}

