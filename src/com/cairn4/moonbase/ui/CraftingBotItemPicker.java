/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.techtree.TechUpgrade;
import com.cairn4.moonbase.tiles.behaviors.CraftingBotBehavior;
import com.cairn4.moonbase.tiles.behaviors.ItemCrafter;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.CraftingBotPickerCallback;
import com.cairn4.moonbase.ui.ItemButton;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.Popup;
import java.util.ArrayList;

public class CraftingBotItemPicker
extends Popup {
    CraftingBotBehavior botBehavior;
    CraftingBotBehavior.WorkOrder workOrder;
    private Table itemTable;
    private ScrollPane scroller;
    private ArrayList<String> allBuildables = new ArrayList();
    public static int ROWS = 6;
    private CraftingBotPickerCallback callback;

    public CraftingBotItemPicker(BaseScreen baseScreen, CraftingBotBehavior botBehavior, CraftingBotBehavior.WorkOrder workOrder) {
        super(baseScreen);
        this.botBehavior = botBehavior;
        this.workOrder = workOrder;
        this.setup();
        this.setSize(600.0f, 400.0f);
        this.setTitle("autocrafter_selectItem");
        this.getAllBuildables();
        this.itemTable = new Table();
        this.popupGroup.addActor(this.itemTable);
        this.addItems();
        this.setupScroller();
        this.show();
    }

    @Override
    protected void setup() {
        super.setup();
    }

    public void addCallback(CraftingBotPickerCallback callback) {
        this.callback = callback;
    }

    private void setupScroller() {
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.baseScreen.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.baseScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)this.itemTable, scrollStyle);
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setBounds(0.0f, 0.0f, this.panelBg.getWidth() - 120.0f, this.panelBg.getHeight() - 180.0f);
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        this.scroller.setPosition(55.0f, 60.0f);
        this.popupGroup.addActor(this.scroller);
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.scroller);
    }

    private void addItems() {
        int rowCounter = 1;
        for (final String itemId : this.allBuildables) {
            ItemButton ib = new ItemButton((Menu)this, 0.7f);
            ib.setIcon(ItemFactory.getItemSprite(itemId));
            ib.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    CraftingBotItemPicker.this.workOrder.change(itemId, 0);
                    CraftingBotItemPicker.this.callback.itemPicked(itemId);
                    CraftingBotItemPicker.this.back();
                }
            });
            ib.button.setFillParent(true);
            this.itemTable.add(ib.group).width(70.0f).height(70.0f).space(5.0f);
            ib.icon.setSize(56.0f, 56.0f);
            ib.icon.setPosition(35.0f, 35.0f, 1);
            ib.layout();
            if (++rowCounter <= ROWS) continue;
            rowCounter = 1;
            this.itemTable.row();
        }
    }

    private void getAllBuildables() {
        for (ItemCrafter ic : this.botBehavior.availableCrafters) {
            ArrayList<String> buildables = ic.getBuildables();
            for (String b : buildables) {
                if (!this.isItemUnlocked(b) || this.allBuildables.contains(b)) continue;
                this.allBuildables.add(b);
            }
        }
    }

    private boolean isItemUnlocked(String itemId) {
        boolean canBuild = false;
        String techReq = ItemFactory.getInstance().getItemTechReq(itemId);
        if (techReq != null) {
            TechUpgrade tech = this.botBehavior.baseModule.world.techManager.getTech(techReq);
            if (tech != null) {
                canBuild = tech.unlocked;
            }
        } else {
            canBuild = true;
        }
        return canBuild;
    }

    private void setupIconGrid() {
    }
}

