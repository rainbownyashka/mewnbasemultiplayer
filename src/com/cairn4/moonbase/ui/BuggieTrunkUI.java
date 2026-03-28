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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.NetworkHelper;
import com.cairn4.moonbase.entities.BuggieTrunk;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.ItemButton;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import com.cairn4.moonbase.MultiplayerNetworkHelper;
import java.util.ArrayList;

public class BuggieTrunkUI
extends Popup
implements Telegraph {
    private static BuggieTrunkUI active = null;
    public GameScreen gameScreen;
    private BuggieTrunk itemStorage;
    private PlayerInventory playerInventory;
    private Vehicle vehicle;
    private ArrayList<ItemButton> inventoryButtonList = new ArrayList();
    private ArrayList<ItemButton> storageButtonList = new ArrayList();
    private Group inventoryButtonGroup;
    private Group storageButtonGroup;
    public Label storageLabel;

    public BuggieTrunkUI(GameScreen gameScreen, BuggieTrunk itemStorage, PlayerInventory playerInventory) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        this.itemStorage = itemStorage;
        this.playerInventory = playerInventory;
        try { this.vehicle = (itemStorage != null) ? itemStorage.buggie : null; } catch (Exception ignored) {}
        MessageManager.getInstance().addListener(this, 31);
        this.setup();
        this.show();
        active = this;
        try {
            if (this.vehicle != null) {
                NetworkHelper.sendPayload(this.gameScreen, "VEH_LOCK:" + this.vehicle.id);
            }
        } catch (Exception ignored) {}
        try {
            if (isLockedByOther()) {
                notifyLocked();
                this.back();
                return;
            }
        } catch (Exception ignored) {}
        gameScreen.hud.hudNotifications.reparentGroup(this.stage.getRoot());
    }

    @Override
    protected void setup() {
        super.setup();
        this.panelBg.setSize(710.0f, 532.0f);
        float xOffset = ((float)MoonBase.SCREEN_WIDTH - this.panelBg.getWidth()) / 2.0f;
        float yOffset = ((float)MoonBase.SCREEN_HEIGHT - this.panelBg.getHeight()) / 2.0f + 20.0f;
        this.popupGroup.setOrigin(this.panelBg.getWidth() / 2.0f, this.panelBg.getHeight() / 2.0f + 15.0f);
        this.popupGroup.setPosition(xOffset, yOffset);
        this.btnClose.setPosition(this.panelBg.getWidth() - 35.0f, this.panelBg.getHeight() - 45.0f, 18);
        this.titleLabel.setText(Localization.get("inventory"));
        this.titleLabel.setFontScale(0.5f);
        this.titleLabel.setPosition(78.0f, this.panelBg.getHeight() - 43.0f, 10);
        this.popupGroup.addActor(this.titleLabel);
        this.storageLabel = new Label((CharSequence)Localization.get("item_buggie"), this.labelStyle);
        this.storageLabel.setFontScale(0.5f);
        this.storageLabel.setPosition(385.0f, this.panelBg.getHeight() - 43.0f, 10);
        this.popupGroup.addActor(this.storageLabel);
        this.inventoryButtonGroup = new Group();
        this.storageButtonGroup = new Group();
        this.inventoryButtonGroup.setPosition(75.0f, 60.0f);
        this.inventoryButtonGroup.setSize(280.0f, 335.0f);
        this.popupGroup.addActor(this.inventoryButtonGroup);
        this.storageButtonGroup.setPosition(380.0f, 60.0f);
        this.storageButtonGroup.setSize(280.0f, 335.0f);
        this.popupGroup.addActor(this.storageButtonGroup);
        Label shiftClickLabel = new Label((CharSequence)Localization.get("storage_shiftClick_hint2"), this.labelStyle);
        shiftClickLabel.setFontScale(0.28f);
        shiftClickLabel.setAlignment(1, 4);
        shiftClickLabel.setWidth(220.0f);
        shiftClickLabel.setWrap(true);
        shiftClickLabel.setTouchable(Touchable.disabled);
        shiftClickLabel.setPosition(508.0f, 65.0f, 4);
        shiftClickLabel.setColor(Menu.MENU_COLOR);
        shiftClickLabel.getColor().a = 0.6f;
        this.popupGroup.addActor(shiftClickLabel);
        this.setupButtonGrids();
    }

    private void setupButtonGrids() {
        float spacing = 3.0f;
        float iconScaling = 0.8f;
        Table inventoryTable = new Table();
        inventoryTable.setFillParent(true);
        inventoryTable.left();
        inventoryTable.top();
        this.inventoryButtonGroup.addActor(inventoryTable);
        int x = 0;
        for (int i = 0; i < 12; ++i) {
            ItemButton ib = new ItemButton((Menu)this, null);
            this.inventoryButtonList.add(ib);
            ib.setScale(0.8f);
            ib.disable();
            ib.hideIcon();
            inventoryTable.add(ib.group).width(ib.getWidth()).pad(spacing);
            if (++x < 3) continue;
            x = 0;
            inventoryTable.row();
        }
        Table storageTable = new Table();
        storageTable.setFillParent(true);
        storageTable.left().top();
        this.storageButtonGroup.addActor(storageTable);
        int bx = 0;
        for (int j = 0; j < this.itemStorage.maxItems; ++j) {
            ItemButton i = new ItemButton((Menu)this, null);
            this.storageButtonList.add(i);
            i.setScale(0.8f);
            i.disable();
            i.hideIcon();
            storageTable.add(i.group).width(i.getWidth()).pad(spacing);
            if (++bx < 3) continue;
            bx = 0;
            storageTable.row();
        }
        this.updateGrid();
    }

    private boolean isLockedByOther() {
        try {
            if (this.vehicle == null) return false;
            int ownerId = (this.gameScreen != null && this.gameScreen.world != null && this.gameScreen.world.player != null) ? this.gameScreen.world.player.ownerId : -1;
            return this.vehicle.inventoryLockOwnerId >= 0 && this.vehicle.inventoryLockOwnerId != ownerId;
        } catch (Exception e) {
            return false;
        }
    }

    private void notifyLocked() {
        try {
            if (this.gameScreen != null && this.gameScreen.hud != null) {
                this.gameScreen.hud.hudNotifications.newMessage(null, Localization.get("storage_in_use"), Color.valueOf("ff6b6b"));
            }
        } catch (Exception ignored) {}
    }

    private void sendInvSync() {
        try {
            if (this.vehicle != null) {
                String payload = MultiplayerNetworkHelper.buildVehicleInvSync(this.vehicle);
                if (payload != null) NetworkHelper.sendPayload(this.gameScreen, payload);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void back() {
        try {
            if (this.vehicle != null) {
                NetworkHelper.sendPayload(this.gameScreen, "VEH_UNLOCK:" + this.vehicle.id);
            }
        } catch (Exception ignored) {}
        active = null;
        super.back();
    }

    public static void handleLockDenied(int vehId, int ownerId, GameScreen gs) {
        try {
            if (active != null && active.vehicle != null && active.vehicle.id == vehId) {
                int localId = (gs != null && gs.world != null && gs.world.player != null) ? gs.world.player.ownerId : -1;
                if (localId == ownerId) {
                    try {
                        if (gs != null && gs.hud != null) {
                            gs.hud.hudNotifications.newMessage(null, Localization.get("storage_in_use"), Color.valueOf("ff6b6b"));
                        }
                    } catch (Exception ignored) {}
                    active.back();
                }
            }
        } catch (Exception ignored) {}
    }

    private void updateGrid() {
        ItemButton b;
        int buildableIndex = 0;
        for (int pi = 0; pi < this.inventoryButtonList.size(); ++pi) {
            b = this.inventoryButtonList.get(pi);
            if (buildableIndex < this.playerInventory.getItemList().size()) {
                b.enable();
                String spriteId = ItemFactory.getItemSprite(this.playerInventory.getItemList().get(buildableIndex).getId());
                b.setIcon(spriteId);
                b.setCount(this.playerInventory.getItemList().get(buildableIndex).getAmount());
                b.updateDurability(this.playerInventory.getItemList().get((int)buildableIndex).item);
                b.clearListeners();
                final int invToStorageIndex = buildableIndex;
                if (invToStorageIndex > this.playerInventory.getItemList().size() - 1) {
                    MoonBase.log("OVERFLOWING INVENTORY!?");
                }
                b.addListener(new ClickListener(0){

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (Gdx.input.isKeyPressed(59) || Gdx.input.isKeyPressed(60)) {
                            BuggieTrunkUI.this.moveToStorage(invToStorageIndex, false);
                        } else {
                            BuggieTrunkUI.this.moveToStorage(invToStorageIndex, true);
                        }
                    }

                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        if (invToStorageIndex < BuggieTrunkUI.this.playerInventory.getItemList().size()) {
                            BuggieTrunkUI.this.createTooltip(((BuggieTrunkUI)BuggieTrunkUI.this).playerInventory.getItemList().get((int)invToStorageIndex).item.getName());
                        } else {
                            MoonBase.error("NOOOO, WHERES MY TOOL TIP!?");
                        }
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        BuggieTrunkUI.this.removeTooltip();
                    }
                });
                b.addListener(new ClickListener(1){

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        ItemStack i = BuggieTrunkUI.this.playerInventory.getItemList().get(invToStorageIndex);
                        if (i.item.canUseFromInventoryBar()) {
                            i.use(BuggieTrunkUI.this.playerInventory.getPlayer());
                            BuggieTrunkUI.this.playerInventory.removeEmpties();
                            BuggieTrunkUI.this.updateGrid();
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
        buildableIndex = 0;
        for (int bi = 0; bi < this.storageButtonList.size(); ++bi) {
            b = this.storageButtonList.get(bi);
            if (buildableIndex < this.itemStorage.getItemList().size()) {
                b.clearListeners();
                b.enable();
                b.setIcon(this.itemStorage.getItemList().get(buildableIndex).getSprite());
                b.setCount(this.itemStorage.getItemList().get(buildableIndex).getAmount());
                b.updateDurability(this.itemStorage.getItemList().get((int)buildableIndex).item);
                final int storageToInvIndex = buildableIndex;
                b.addListener(new ClickListener(0){

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (Gdx.input.isKeyPressed(59) || Gdx.input.isKeyPressed(60)) {
                            BuggieTrunkUI.this.moveToInventory(storageToInvIndex, false);
                        } else {
                            BuggieTrunkUI.this.moveToInventory(storageToInvIndex, true);
                        }
                    }

                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        if (storageToInvIndex < BuggieTrunkUI.this.itemStorage.getItemList().size()) {
                            BuggieTrunkUI.this.createTooltip(((BuggieTrunkUI)BuggieTrunkUI.this).itemStorage.getItemList().get((int)storageToInvIndex).item.getName());
                        }
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        BuggieTrunkUI.this.removeTooltip();
                    }
                });
                b.addListener(new ClickListener(1){

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (storageToInvIndex < BuggieTrunkUI.this.itemStorage.getItemList().size()) {
                            ItemStack i = BuggieTrunkUI.this.itemStorage.getItemList().get(storageToInvIndex);
                            if (i.item.canUseFromInventoryBar()) {
                                i.use(BuggieTrunkUI.this.playerInventory.getPlayer());
                                BuggieTrunkUI.this.itemStorage.removeEmpties();
                                BuggieTrunkUI.this.updateGrid();
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

    private void moveToInventory(int storageIndex, boolean moveStack) {
        if (isLockedByOther()) {
            notifyLocked();
            return;
        }
        if (storageIndex < this.itemStorage.getItemList().size()) {
            int spaceAvailableForItem = this.playerInventory.getSpaceAvailableFor(this.itemStorage.getItemList().get(storageIndex).getId());
            if (spaceAvailableForItem > 0) {
                if (moveStack) {
                    ItemStack stored = this.itemStorage.getItemList().get(storageIndex);
                    if (stored.getAmount() <= spaceAvailableForItem) {
                        this.playerInventory.add(this.itemStorage.getItemList().get(storageIndex), false);
                        this.itemStorage.remove(storageIndex);
                    } else {
                        int moveAmount = spaceAvailableForItem;
                        ItemStack newStack = new ItemStack(stored.getId(), moveAmount);
                        if (newStack.item.durability != 0) {
                            stored.item.durability = newStack.item.durability;
                        }
                        this.playerInventory.add(newStack, false);
                        stored.remove(moveAmount);
                    }
                    this.itemStorage.removeEmpties();
                } else {
                    Item tempItem = Item.copy(this.itemStorage.getItemList().get((int)storageIndex).item);
                    this.playerInventory.add(new ItemStack(tempItem), false);
                    this.itemStorage.removeSingle(storageIndex);
                }
                this.updateGrid();
                this.sendInvSync();
            } else {
                this.gameScreen.hud.hudNotifications.newMessage(Localization.format("inventory_full", new Object[0]), Color.RED);
            }
        } else {
            MoonBase.error("storageIndex is out of bounds");
        }
    }

    private void moveToStorage(int inventoryIndex, boolean moveStack) {
        if (isLockedByOther()) {
            notifyLocked();
            return;
        }
        if (inventoryIndex < this.playerInventory.getItemList().size()) {
            int spaceAvailableForItem = this.itemStorage.getSpaceAvailableFor(this.playerInventory.getItemList().get(inventoryIndex).getId());
            if (spaceAvailableForItem > 0) {
                if (moveStack) {
                    ItemStack playerItem = this.playerInventory.getItemList().get(inventoryIndex);
                    if (playerItem.getAmount() <= spaceAvailableForItem) {
                        this.itemStorage.addTo(this.playerInventory.getItemList().get(inventoryIndex));
                        this.playerInventory.removeItem(inventoryIndex);
                    } else {
                        int moveAmount = spaceAvailableForItem;
                        ItemStack newStack = new ItemStack(playerItem.getId(), moveAmount);
                        this.itemStorage.addTo(newStack);
                        playerItem.remove(moveAmount);
                    }
                    this.playerInventory.removeEmpties();
                } else {
                    this.itemStorage.addSingle(this.playerInventory.getItemList().get((int)inventoryIndex).item);
                    this.playerInventory.consumeItem(this.playerInventory.getItemList().get(inventoryIndex), 1);
                }
                this.updateGrid();
                this.sendInvSync();
            } else {
                this.gameScreen.hud.hudNotifications.newMessage(Localization.format("storage_full", new Object[0]), Color.RED);
            }
        } else {
            MoonBase.error("inventoryIndex out of bounds");
        }
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        if (msg.message == 31) {
            MoonBase.log("trunk ui handling change message");
            this.updateGrid();
            return true;
        }
        return false;
    }

    @Override
    public void hide() {
        this.gameScreen.hud.hudNotifications.resetGroup();
        MessageManager.getInstance().removeListener((Telegraph)this, 31);
        super.hide();
    }
}
