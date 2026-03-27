/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

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
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.ui.GameScreen;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class PaintSelector
implements Observer {
    GameScreen gameScreen;
    Player player;
    ButtonGroup colorButtonGroup;
    ArrayList<Button> buttonList = new ArrayList();
    Group group;
    Label label;
    Table table;

    public PaintSelector(final GameScreen gameScreen, Group parentGroup) {
        this.gameScreen = gameScreen;
        this.player = gameScreen.world.getPlayer();
        this.player.addObserver(this);
        this.group = new Group();
        parentGroup.addActor(this.group);
        this.group.setPosition(MoonBase.SCREEN_WIDTH / 2, 110.0f);
        this.colorButtonGroup = new ButtonGroup();
        this.colorButtonGroup.setMaxCheckCount(1);
        this.table = new Table();
        this.table.setFillParent(true);
        this.table.bottom();
        this.table.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        for (int ci = 0; ci < Vars.paintbrushUIColors.length; ++ci) {
            Stack stack = new Stack();
            Button cb = new Button(gameScreen.minorButtonStyle);
            this.colorButtonGroup.add(cb);
            this.buttonList.add(cb);
            final int index = ci;
            cb.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    gameScreen.world.getPlayer().setPaintColorIndex(index);
                }
            });
            stack.add(cb);
            Table inner = new Table();
            Image swatch = new Image(gameScreen.skin.getDrawable("roundedbox"));
            swatch.setColor(Vars.paintbrushUIColors[ci]);
            swatch.setTouchable(Touchable.disabled);
            inner.add(swatch).pad(8.0f);
            stack.add(inner);
            this.table.add(stack).width(36.0f).height(36.0f).space(3.0f);
            cb.setChecked(this.player.getPaintColorIndex() == ci);
        }
        this.table.layout();
        this.group.addActor(this.table);
        this.table.setPosition(0.0f, 0.0f, 4);
    }

    private void show() {
    }

    public void remove() {
        MoonBase.log("Removing paint selector ui");
        this.gameScreen.hud.showingPaintSelector = false;
        this.player.deleteObserver(this);
        this.group.remove();
    }

    @Override
    public void update(Observable observable, Object o) {
        if (!(o != "inventorySelectionChanged" && o != "updateInventory" || this.player.playerInventory.getEquippedItemId().equals("paintbrush"))) {
            this.remove();
        }
    }
}

