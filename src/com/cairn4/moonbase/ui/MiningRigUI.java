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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.NetworkHelper;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.tiles.MiningRig;
import com.cairn4.moonbase.tiles.behaviors.ItemStorageBehavior;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.ItemButton;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import com.cairn4.moonbase.MultiplayerNetworkHelper;
import com.cairn4.moonbase.worlddata.ItemDropperFactory;
import com.cairn4.moonbase.worlddata.ItemDropperItemData;
import com.cairn4.moonbase.worlddata.MiningResourceData;
import java.util.ArrayList;

public class MiningRigUI
extends Popup
implements Telegraph {
    private static MiningRigUI active = null;
    public GameScreen gameScreen;
    private MiningRig miningRig;
    private ItemStorageBehavior itemStorageBehavior;
    private PlayerInventory playerInventory;
    private ArrayList<ItemButton> inventoryButtonList = new ArrayList();
    private ArrayList<ItemButton> storageButtonList = new ArrayList();
    private Group inventoryButtonGroup;
    private Group storageButtonGroup;
    private Label progressTopLabel;
    private Label miningSpeedLabel;
    private ProgressBar.ProgressBarStyle progressBarStyle;
    private ProgressBar progressBar;
    private Label progressTimeLabel;
    private static final Color DISABLED_ICON_COLOR = new Color(1.0f, 1.0f, 1.0f, 0.5f);

    public MiningRigUI(GameScreen gameScreen, MiningRig miningRig, ItemStorageBehavior itemStorageBehavior, PlayerInventory playerInventory) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        MessageManager.getInstance().addListener(this, 32);
        MessageManager.getInstance().addListener(this, 49);
        this.miningRig = miningRig;
        this.itemStorageBehavior = itemStorageBehavior;
        this.playerInventory = playerInventory;
        this.setup();
        this.show();
        active = this;
        try {
            if (this.itemStorageBehavior != null && this.itemStorageBehavior.baseModule != null) {
                NetworkHelper.sendPayload(this.gameScreen, "BASE_LOCK:" + this.itemStorageBehavior.baseModule.worldX + ":" + this.itemStorageBehavior.baseModule.worldY);
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
    public void update(float delta) {
        super.update(delta);
        this.progressBar.setValue(this.miningRig.resourceCollectorBehavior.getProgress());
        String miningSpeed = Vars.singleDecimal.format(this.miningRig.getMiningRate());
        this.miningSpeedLabel.setText(Localization.format("miningRig_speed", miningSpeed));
        if (!this.miningRig.resourceCollectorBehavior.inventorySlotAvailable) {
            this.progressTopLabel.setText(Localization.get("miningRig_full"));
        } else if (this.miningRig.hasPower) {
            this.progressBar.setColor(Color.WHITE);
            this.progressTopLabel.setText(Localization.get("miningRig_extracting"));
        } else {
            this.progressTopLabel.setText("[#ff0000]" + Localization.get("no_power") + "[]");
            this.progressBar.setColor(Color.RED);
        }
    }

    @Override
    public void back() {
        try {
            if (this.itemStorageBehavior != null && this.itemStorageBehavior.baseModule != null) {
                NetworkHelper.sendPayload(this.gameScreen, "BASE_UNLOCK:" + this.itemStorageBehavior.baseModule.worldX + ":" + this.itemStorageBehavior.baseModule.worldY);
            }
        } catch (Exception ignored) {}
        active = null;
        super.back();
    }

    @Override
    public void hide() {
        MessageManager.getInstance().removeListener((Telegraph)this, 32);
        MessageManager.getInstance().removeListener((Telegraph)this, 49);
        super.hide();
        this.gameScreen.hud.hudNotifications.resetGroup();
    }

    private boolean isLockedByOther() {
        try {
            int ownerId = (this.gameScreen != null && this.gameScreen.world != null && this.gameScreen.world.player != null) ? this.gameScreen.world.player.ownerId : -1;
            return this.itemStorageBehavior != null && this.itemStorageBehavior.inventoryLockOwnerId >= 0 && this.itemStorageBehavior.inventoryLockOwnerId != ownerId;
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
            if (this.itemStorageBehavior != null && this.itemStorageBehavior.baseModule != null) {
                String payload = MultiplayerNetworkHelper.buildBaseInvSync(this.itemStorageBehavior.baseModule, this.itemStorageBehavior);
                if (payload != null) NetworkHelper.sendPayload(this.gameScreen, payload);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        this.updateGrid();
        if (this.miningRig.resourceCollectorBehavior.inventorySlotAvailable) {
            // empty if block
        }
        return true;
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
        Label storageLabel = new Label((CharSequence)Localization.get("item_miningrig-builder"), this.labelStyle);
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
                i.setIconColor(DISABLED_ICON_COLOR);
                inventoryTable.add(i.group).width(i.getWidth()).pad(spacing);
            }
        }
        Table storageTable = new Table();
        storageTable.setFillParent(true);
        storageTable.left();
        this.addProgressMeter(storageTable);
        storageTable.row();
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
        storageTable.row();
        Label shiftClickLabel = new Label((CharSequence)Localization.get("storage_shiftClick_hint2"), this.labelStyle);
        shiftClickLabel.setFontScale(0.28f);
        shiftClickLabel.setAlignment(1, 4);
        shiftClickLabel.setWidth(250.0f);
        shiftClickLabel.setWrap(true);
        shiftClickLabel.setTouchable(Touchable.disabled);
        shiftClickLabel.setPosition(203.0f, 34.0f, 4);
        shiftClickLabel.setColor(Menu.MENU_COLOR);
        shiftClickLabel.getColor().a = 0.6f;
        storageTable.add(shiftClickLabel).colspan(4).fillX().padTop(10.0f);
        this.updateGrid();
    }

    private void addProgressMeter(Table parentTable) {
        Table progressTable = new Table();
        progressTable.top();
        progressTable.padTop(20.0f);
        parentTable.add(progressTable).colspan(4).padBottom(10.0f).fillX().expandX();
        Table resourceIcons = new Table();
        progressTable.add(resourceIcons).fillX().padBottom(20.0f);
        for (MiningResourceData msd : ItemDropperFactory.getInstance().miningResources) {
            if (!msd.biome.equals(this.miningRig.getBiome().toString())) continue;
            for (ItemDropperItemData idid : msd.items) {
                Image icon = new Image(this.gameScreen.skin.getDrawable(ItemFactory.getItemSprite(idid.itemId)));
                resourceIcons.add(icon).spaceRight(5.0f).width(36.0f).height(36.0f);
            }
        }
        progressTable.row();
        this.progressTopLabel = new Label((CharSequence)Localization.get("miningRig_extracting"), this.labelStyle);
        this.progressTopLabel.setAlignment(1);
        this.progressTopLabel.setFontScale(0.4f);
        progressTable.add(this.progressTopLabel).padBottom(5.0f).fillX().expandX();
        progressTable.row();
        this.miningSpeedLabel = new Label((CharSequence)Localization.format("miningRig_speed", Float.valueOf(this.miningRig.getMiningRate())), this.labelStyle);
        this.miningSpeedLabel.setAlignment(1);
        this.miningSpeedLabel.setFontScale(0.35f);
        progressTable.add(this.miningSpeedLabel).padBottom(20.0f).fillX().expandX();
        progressTable.row();
        progressTable.row();
        ProgressBar.ProgressBarStyle progressBarStyle = new ProgressBar.ProgressBarStyle();
        progressBarStyle.knobAfter = this.skin.getDrawable("hud-progress-empty");
        progressBarStyle.knobBefore = this.skin.getDrawable("hud-progress-filled");
        progressBarStyle.knob = this.skin.getDrawable("hud-progress-knob");
        this.progressBar = new ProgressBar(0.0f, 1.0f, 0.01f, false, progressBarStyle);
        this.progressBar.setValue(0.5f);
        progressTable.add(this.progressBar).minHeight(15.0f).fillX().spaceLeft(4.0f).spaceRight(4.0f).padLeft(30.0f).padRight(30.0f).padBottom(30.0f);
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
                    b.setIconColor(DISABLED_ICON_COLOR);
                    b.progressBar.getColor().a = 0.6f;
                    b.clearListeners();
                    final int invToStorageIndex = buildableIndex;
                    b.addListener(new ClickListener(){

                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            if (MoonBase.DEBUG_INFO) {
                                if (Gdx.input.isKeyPressed(59) || Gdx.input.isKeyPressed(60)) {
                                    MiningRigUI.this.moveToStorage(invToStorageIndex, false);
                                } else {
                                    MiningRigUI.this.moveToStorage(invToStorageIndex, true);
                                }
                            }
                        }

                        @Override
                        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                            MiningRigUI.this.createTooltip(((MiningRigUI)MiningRigUI.this).playerInventory.getItemList().get((int)invToStorageIndex).item.getName());
                        }

                        @Override
                        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                            MiningRigUI.this.removeTooltip();
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
                b.addListener(new ClickListener(){

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (Gdx.input.isKeyPressed(59) || Gdx.input.isKeyPressed(60)) {
                            MiningRigUI.this.moveToInventory(storageToInvIndex, false);
                        } else {
                            MiningRigUI.this.moveToInventory(storageToInvIndex, true);
                        }
                    }

                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        if (storageToInvIndex < MiningRigUI.this.itemStorageBehavior.getItemList().size()) {
                            MiningRigUI.this.createTooltip(((MiningRigUI)MiningRigUI.this).itemStorageBehavior.getItemList().get((int)storageToInvIndex).item.getName());
                        }
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        MiningRigUI.this.removeTooltip();
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

    public static void refreshIfActive(int wx, int wy) {
        try {
            if (active != null && active.itemStorageBehavior != null && active.itemStorageBehavior.baseModule != null) {
                if (active.itemStorageBehavior.baseModule.worldX == wx && active.itemStorageBehavior.baseModule.worldY == wy) {
                    active.updateGrid();
                }
            }
        } catch (Exception ignored) {}
    }

    private void moveToInventory(int storageIndex, boolean moveStack) {
        if (isLockedByOther()) {
            notifyLocked();
            return;
        }
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
            this.sendInvSync();
        } else {
            this.gameScreen.hud.hudNotifications.newMessage(Localization.format("inventory_full", new Object[0]), Color.RED);
        }
    }

    private void moveToStorage(int inventoryIndex, boolean moveStack) {
        if (isLockedByOther()) {
            notifyLocked();
            return;
        }
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
            this.sendInvSync();
        } else {
            this.gameScreen.hud.hudNotifications.newMessage(Localization.format("storage_full", new Object[0]), Color.RED);
        }
    }

    public static void handleLockDenied(int wx, int wy, int ownerId, GameScreen gs) {
        try {
            if (active != null && active.itemStorageBehavior != null && active.itemStorageBehavior.baseModule != null) {
                if (active.itemStorageBehavior.baseModule.worldX == wx && active.itemStorageBehavior.baseModule.worldY == wy) {
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
            }
        } catch (Exception ignored) {}
    }
}
