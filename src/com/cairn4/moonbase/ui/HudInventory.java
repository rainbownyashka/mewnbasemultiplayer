/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemActions;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.RecipieRequirement;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.ItemButton;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import java.util.ArrayList;

public class HudInventory {
    private Hud hud;
    ArrayList<ItemButton> itemButtonList = new ArrayList();
    Group baseGroup;
    private Table table;
    DragAndDrop dragAndDrop;
    Button btnCraftWorkbench;
    ItemData workbenchItemData;

    public HudInventory(Hud hud) {
        this.hud = hud;
        this.setup();
    }

    public void show() {
        this.baseGroup.setVisible(true);
    }

    public void hide() {
        this.baseGroup.setVisible(false);
    }

    public void bringToFront(Group parentGroup) {
        parentGroup.addActor(this.baseGroup);
    }

    public void resetGroup() {
        this.hud.inventoryGroup.addActor(this.baseGroup);
    }

    private void setup() {
        this.dragAndDrop = new DragAndDrop();
        this.dragAndDrop.setKeepWithinStage(true);
        this.baseGroup = new Group();
        this.baseGroup.setPosition(0.0f, 0.0f);
        this.baseGroup.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
        this.baseGroup.setTouchable(Touchable.childrenOnly);
        this.hud.inventoryGroup.addActor(this.baseGroup);
        Button.ButtonStyle bs = new Button.ButtonStyle();
        bs.up = this.hud.skin.getDrawable("hud/btn-makeworkbench");
        bs.over = this.hud.skin.getDrawable("hud/btn-makeworkbench-hover");
        bs.down = this.hud.skin.getDrawable("hud/btn-makeworkbench-pressed");
        bs.disabled = this.hud.skin.getDrawable("hud/btn-makeworkbench-disabled");
        this.btnCraftWorkbench = new Button(bs);
        this.btnCraftWorkbench.setSize(57.0f, 52.0f);
        this.btnCraftWorkbench.setOrigin(1);
        this.btnCraftWorkbench.setPosition(-575.0f, 41.0f, 1);
        this.baseGroup.addActor(this.btnCraftWorkbench);
        this.btnCraftWorkbench.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                HudInventory.this.craftWorkbenchHandler();
            }
        });
        this.workbenchItemData = ItemFactory.getItemData("crafting-station-builder");
        this.table = new Table();
        this.table.center().bottom();
        this.baseGroup.addActor(this.table);
        this.baseGroup.setPosition(MoonBase.SCREEN_WIDTH / 2, 12.0f);
        this.table.row();
        for (int i = 0; i < 12; ++i) {
            ItemButton ib = new ItemButton(this.hud);
            ib.hideIcon();
            ib.setScale(0.85f);
            ib.setCount(0);
            ib.setEquippedIcon(false);
            this.itemButtonList.add(ib);
            this.table.add(ib.group).space(3.0f);
        }
    }

    private void craftWorkbenchHandler() {
        if (this.canMakeWorkbench()) {
            ItemStack workbench = new ItemStack(ItemFactory.getInstance().createItem(this.workbenchItemData), 1);
            this.hud.player.playerInventory.spendResources(this.workbenchItemData);
            this.hud.player.playerInventory.add(workbench, true);
            this.hud.player.inventoryUpdate();
        } else {
            boolean hasResourcesForWorkbench = this.hud.player.playerInventory.hasResources(this.workbenchItemData);
            boolean slotAvailable = this.hud.player.playerInventory.hasSlotAvailableAfterCrafting(this.workbenchItemData);
            if (!slotAvailable && this.hud.player.playerInventory.hasSlotAvailable("crafting-station")) {
                slotAvailable = true;
            }
            String errorMessage = "";
            if (!hasResourcesForWorkbench) {
                errorMessage = Localization.get("requires");
                for (RecipieRequirement r : this.workbenchItemData.requires) {
                    errorMessage = errorMessage + " " + r.quantity + " ";
                    errorMessage = errorMessage + Localization.get("item_" + r.id);
                }
            }
            if (hasResourcesForWorkbench && !slotAvailable) {
                errorMessage = Localization.get("craftingInventoryFull");
            }
            this.hud.hudNotifications.newMessage("scrap", errorMessage);
        }
    }

    public void changeSelection(PlayerInventory playerInventory) {
        for (int i = 0; i < this.itemButtonList.size(); ++i) {
            ItemButton ib = this.itemButtonList.get(i);
            if (playerInventory.itemList.size() > 0 && i == playerInventory.getSelectedIndex()) {
                ib.setEquippedIcon(true);
                continue;
            }
            ib.setEquippedIcon(false);
        }
        if (!this.hud.showingPaintSelector && playerInventory.getEquippedItemId().equals("paintbrush")) {
            this.hud.showPaintSelector();
        }
    }

    public void update(final PlayerInventory playerInventory) {
        final PlayerInventory inventory = playerInventory;
        this.dragAndDrop.clear();
        this.updateWorkbenchButton();
        for (int i = 0; i < this.itemButtonList.size(); ++i) {
            final ItemButton ib = this.itemButtonList.get(i);
            ib.clearListeners();
            if (playerInventory.itemList.size() > 0 && i == playerInventory.getSelectedIndex()) {
                ib.setEquippedIcon(true);
            } else {
                ib.setEquippedIcon(false);
            }
            if (i < playerInventory.itemList.size()) {
                ib.enable();
                ib.setEmpty(false);
                ib.setBorderColor(Color.WHITE);
                ib.showIcon();
                String iconName = ItemFactory.getItemSprite(playerInventory.itemList.get(i).getId());
                ib.setIcon(iconName);
                ib.setCount(playerInventory.itemList.get(i).getAmount());
                final int index = i;
                ItemStack itemStack = playerInventory.itemList.get(i);
                String tooltipTextTemp = itemStack.getName();
                String secondaryTextTemp = Item.getDesc(itemStack.getId());
                int minMeleeDamage = ItemFactory.getItemData((String)itemStack.getId()).minMeleeDamage;
                int maxMeleeDamage = ItemFactory.getItemData((String)itemStack.getId()).maxMeleeDamage;
                boolean consumeHint = false;
                if (minMeleeDamage > 0) {
                    secondaryTextTemp = secondaryTextTemp + "\n";
                    secondaryTextTemp = secondaryTextTemp + "[#dc884f]" + Localization.get("damage") + ": " + minMeleeDamage + " - " + maxMeleeDamage + "[]";
                }
                for (ItemActions action : playerInventory.itemList.get((int)i).item.actions) {
                    if (action.type.equals("eat")) {
                        secondaryTextTemp = secondaryTextTemp + "\n";
                        secondaryTextTemp = secondaryTextTemp + "[#dc884f]" + Localization.format("inventory_hint_plusHunger", action.value) + "[]";
                        consumeHint = true;
                    }
                    if (action.type.equals("heal")) {
                        secondaryTextTemp = secondaryTextTemp + "\n";
                        secondaryTextTemp = secondaryTextTemp + "[#ed454d]" + Localization.format("inventory_hint_plusHealth", action.value);
                        consumeHint = true;
                    }
                    if (action.type.equals("gainOxygen")) {
                        secondaryTextTemp = secondaryTextTemp + "\n";
                        secondaryTextTemp = secondaryTextTemp + "[#56a3ea]" + Localization.format("inventory_hint_plusOxygen", action.value);
                        consumeHint = true;
                    }
                    if (action.type.equals("createBuilding")) {
                        secondaryTextTemp = secondaryTextTemp + "\n";
                        secondaryTextTemp = secondaryTextTemp + "[#9aafb2]" + Localization.get("inventory_hint_placeBuilding") + "[]";
                    }
                    if (!action.type.equals("createItemDropper")) continue;
                    secondaryTextTemp = secondaryTextTemp + "\n";
                    secondaryTextTemp = secondaryTextTemp + "[#9aafb2]" + Localization.get("inventory_hint_placePlant") + "[]";
                }
                if (consumeHint) {
                    secondaryTextTemp = secondaryTextTemp + "\n";
                    secondaryTextTemp = secondaryTextTemp + "[#9aafb2]" + Localization.get("inventory_hint_consume") + "[]";
                }
                ib.updateDurability(playerInventory.itemList.get((int)i).item);
                if (playerInventory.itemList.get((int)i).item.getRotatable()) {
                    tooltipTextTemp = tooltipTextTemp + "\n";
                    tooltipTextTemp = tooltipTextTemp + Localization.format("inventory_hint_rotateButton", SettingsLoader.getInstance().settingsData.KEYS_ROTATE_TILE);
                }
                final String tooltipText = tooltipTextTemp;
                final String secondaryText = secondaryTextTemp;
                ib.button.setUserObject(i);
                if (index < playerInventory.getItemList().size()) {
                    ib.addListener(new ClickListener(0){

                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            inventory.setSelection(index);
                            inventory.equipItem();
                        }

                        @Override
                        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                            HudInventory.this.hud.createTooltip(tooltipText, secondaryText);
                            HudInventory.this.addStatBarBoost(playerInventory.itemList.get(index), ((HudInventory)HudInventory.this).hud.gameScreen.world.player);
                        }

                        @Override
                        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                            HudInventory.this.hud.removeTooltip();
                            HudInventory.this.hud.hideProgressBarIncrease();
                        }
                    });
                } else {
                    MoonBase.log("hud inventory index out of bounds.");
                }
                ib.addListener(new ClickListener(1){

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (index != playerInventory.getSelectedIndex()) {
                            inventory.setSelection(index);
                            inventory.equipItem();
                        }
                        if (playerInventory.getSelectedItem().item.canUseFromInventoryBar() && playerInventory.getSelectedItem().item.hasActions()) {
                            playerInventory.useItem();
                        }
                        if (playerInventory.getSelectedItem() != null && playerInventory.getSelectedItem().item.id.equals("painttool")) {
                            HudInventory.this.hud.showPaintSelector();
                        }
                    }
                });
                ib.setTouchable(Touchable.enabled);
                this.dragAndDrop.addSource(new DragAndDrop.Source(ib.group){

                    @Override
                    public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                        DragAndDrop.Payload payload = new DragAndDrop.Payload();
                        payload.setObject(index);
                        Group payloadGroup = new Group();
                        Image dragIcon = new Image(ib.icon.getDrawable());
                        dragIcon.setSize(ib.icon.getWidth(), ib.icon.getHeight());
                        payloadGroup.addActor(dragIcon);
                        dragIcon.setPosition(5.0f, -5.0f, 1);
                        int amount = inventory.getItemList().get(index).getAmount();
                        Label count = new Label((CharSequence)("" + amount), ((HudInventory)HudInventory.this).hud.labelStyle);
                        count.setFontScale(0.45f);
                        count.setWidth(100.0f);
                        count.setAlignment(20);
                        count.setPosition(43.0f, -35.0f, 20);
                        count.setTouchable(Touchable.disabled);
                        payloadGroup.addActor(count);
                        count.setVisible(amount > 1);
                        payload.setDragActor(payloadGroup);
                        ib.icon.setVisible(false);
                        ib.showCount(false);
                        return payload;
                    }

                    @Override
                    public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                        if (index < playerInventory.itemList.size()) {
                            ib.icon.setVisible(true);
                            ib.setCount(playerInventory.itemList.get(index).getAmount());
                        }
                    }
                });
                this.dragAndDrop.addTarget(new DragAndDrop.Target(ib.button){

                    @Override
                    public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                        this.getActor().setColor(Menu.MENU_COLOR);
                        return true;
                    }

                    @Override
                    public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                        Gdx.app.debug("MewnBase", "HudInventory: accepted payload " + payload.getObject() + " onto " + this.getActor().getUserObject());
                        inventory.swapSlots((Integer)payload.getObject(), (Integer)this.getActor().getUserObject());
                        Audio.getInstance().playSound("sounds/pop.mp3", 0.35f, 1.4f);
                    }

                    @Override
                    public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload) {
                        this.getActor().setColor(Color.WHITE);
                    }
                });
                continue;
            }
            ib.hideIcon();
            ib.setCount(0);
            ib.setEmpty(true);
            if (i == playerInventory.getSelectedIndex()) {
                ib.setEquippedIcon(true);
            } else {
                ib.setEquippedIcon(false);
            }
            ib.setTouchable(Touchable.enabled);
            ib.updateDurability(null);
            final int index = i;
            ib.addListener(new ClickListener(0){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    inventory.setSelection(index);
                    inventory.equipItem();
                }
            });
        }
        this.dragAndDrop.addTarget(new DragAndDrop.Target(this.hud.vignette){

            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                MoonBase.log("Dropping item via drag");
                Audio.getInstance().playSound("sounds/pop.mp3", 0.35f, 1.4f);
                inventory.setSelection((Integer)payload.getObject());
                inventory.dropStack();
            }
        });
        this.changeSelection(playerInventory);
    }

    private void addStatBarBoost(ItemStack itemStack, Player player) {
        for (ItemActions ia : itemStack.item.actions) {
            float newValue;
            float boost;
            if (ia.type.equals("eat")) {
                boost = Float.valueOf(ia.value).floatValue();
                float hunger = player.playerStatus.getHunger();
                newValue = (hunger + boost) / player.playerStatus.getMaxHunger();
                System.out.println("boost: " + boost + " + hunger " + hunger + " = " + newValue);
                this.hud.updateHungerProgressBarIncrease(newValue);
                continue;
            }
            if (ia.type.equals("gainOxygen")) {
                boost = Float.valueOf(ia.value).floatValue();
                float oxygen = player.playerStatus.getAir();
                newValue = (oxygen + boost) / player.playerStatus.getMaxAir();
                System.out.println("boost: " + boost + " + hunger " + oxygen + " = " + newValue);
                this.hud.updateAirProgressBarIncrease(newValue);
                continue;
            }
            if (!ia.type.equals("heal")) continue;
            boost = Float.valueOf(ia.value).floatValue();
            float health = player.playerStatus.getHealth();
            newValue = (health + boost) / player.playerStatus.getMaxHealth();
            System.out.println("boost: " + boost + " + hunger " + health + " = " + newValue);
            this.hud.updateHealthProgressBarIncrease(newValue);
        }
    }

    private boolean canMakeWorkbench() {
        boolean canMake = false;
        boolean hasResourcesForWorkbench = this.hud.player.playerInventory.hasResources(this.workbenchItemData);
        if (hasResourcesForWorkbench) {
            if (this.hud.player.playerInventory.hasSlotAvailable("crafting-station")) {
                canMake = true;
            } else if (this.hud.player.playerInventory.hasSlotAvailableAfterCrafting(this.workbenchItemData)) {
                canMake = true;
            }
        }
        return canMake;
    }

    private void updateWorkbenchButton() {
        if (this.canMakeWorkbench()) {
            this.btnCraftWorkbench.setDisabled(false);
        } else {
            this.btnCraftWorkbench.setDisabled(true);
        }
    }
}

