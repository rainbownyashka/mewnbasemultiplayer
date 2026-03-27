/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;

public class DiscoveriesPopup
extends Popup {
    GameScreen gameScreen;
    Player player;
    Table table;
    ScrollPane scroller;

    public DiscoveriesPopup(GameScreen gameScreen, Player player) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        this.player = player;
        this.setup();
        this.show();
    }

    @Override
    protected void setup() {
        super.setup();
        this.setSize(900.0f, 600.0f);
        this.setTitle("discoveries");
        this.setupScroller();
        this.addItems();
    }

    private void setupScroller() {
        Image scrollBackground = new Image(new NinePatch(this.gameScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4));
        scrollBackground.setSize(10.0f, this.panelBg.getHeight() - 195.0f);
        scrollBackground.setPosition(this.panelBg.getWidth() - 75.0f, 60.0f, 20);
        scrollBackground.getColor().a = 0.5f;
        this.popupGroup.addActor(scrollBackground);
        this.table = new Table();
        this.table.top().left();
        this.popupGroup.addActor(this.table);
        this.table.setSize(this.panelBg.getWidth() - 150.0f, this.panelBg.getHeight() - 175.0f);
        this.table.setPosition(75.0f, 150.0f);
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.baseScreen.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.baseScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)this.table, scrollStyle);
        this.scroller.setWidth(this.table.getWidth());
        this.scroller.setHeight(this.table.getHeight());
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setBounds(0.0f, 0.0f, this.panelBg.getWidth() - 150.0f, this.panelBg.getHeight() - 195.0f);
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        this.scroller.setPosition(75.0f, 60.0f);
        this.popupGroup.addActor(this.scroller);
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.scroller);
    }

    private void addItems() {
        this.table.clearChildren();
        int cols = 6;
        int c = 0;
        for (int i = 0; i < 18; ++i) {
            Stack stack = new Stack();
            Table overlay = new Table();
            Image icon = new Image(this.skin.getDrawable("ro" + i));
            icon.setTouchable(Touchable.enabled);
            overlay.add(icon).pad(12.0f);
            stack.add(overlay);
            String toolTipText = Localization.get("item_researchObject") + (i + 1);
            if (this.researchItemFound(i)) {
                icon.setColor(Color.WHITE);
            } else {
                toolTipText = Localization.get("item_researchObject_unknown");
                icon.setColor(0.0f, 0.0f, 0.0f, 0.25f);
            }
            final String fToolTipText = toolTipText;
            icon.addListener(new ClickListener(){

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    DiscoveriesPopup.this.createTooltip(fToolTipText);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    DiscoveriesPopup.this.removeTooltip();
                }
            });
            this.table.add(stack).height(114.0f).width(114.0f).space(8.0f);
            if (++c != cols) continue;
            c = 0;
            this.table.row();
        }
    }

    private boolean researchItemFound(int id) {
        for (Integer i : this.player.researchObjectsDiscovered) {
            if (!i.equals(id)) continue;
            return true;
        }
        return false;
    }
}

