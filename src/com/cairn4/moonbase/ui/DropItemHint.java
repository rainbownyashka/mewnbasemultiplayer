/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.tiles.behaviors.DropItemOnBaseBehavior;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.DestroyableUI;

public class DropItemHint
implements DestroyableUI {
    private static final float DURATION = 1.0f;
    private static final int ICON_SIZE = 50;
    private boolean readyToRemove;
    private DropItemOnBaseBehavior behavior;
    BaseScreen baseScreen;
    private Group group;
    private Table table;
    private Image panelBg;

    public DropItemHint(DropItemOnBaseBehavior behavior) {
        this.behavior = behavior;
        this.baseScreen = behavior.baseModule.world.gameScreen;
        this.group = new Group();
        this.group.setPosition(behavior.baseModule.getXCenter(), behavior.baseModule.getYCenter() + 100.0f);
        behavior.baseModule.world.gameScreen.worldUIStage.addActor(this.group);
        this.table = new Table();
        this.table.setPosition(0.0f, 5.0f, 4);
        this.table.bottom();
        this.setupFrame();
        this.group.addActor(this.table);
        for (String s : behavior.acceptedItemIds) {
            Skin skin = this.baseScreen.skin;
            ItemFactory.getInstance();
            Image i = new Image(skin.getDrawable(ItemFactory.getItemSprite(s)));
            i.setSize(50.0f, 50.0f);
            this.table.add(i).pad(5.0f).width(50.0f).height(50.0f);
        }
        this.group.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(0.8f, 0.8f), (Action)Actions.parallel((Action)Actions.fadeIn(0.25f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.25f)), (Action)Actions.delay(1.0f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                DropItemHint.this.fadeOut();
            }
        })));
    }

    public void setupFrame() {
        NinePatch panelBg2 = new NinePatch(this.behavior.baseModule.world.gameScreen.skin.getRegion("dropItemHint"), 20, 20, 20, 20);
        this.table.layout();
        float width = this.behavior.acceptedItemIds.size() * 60 + 20;
        float height = 70.0f;
        this.panelBg = new Image(panelBg2);
        this.panelBg.setSize(width, height);
        this.panelBg.setOrigin(4);
        this.panelBg.setPosition(0.0f, 0.0f, 4);
        this.group.addActor(this.panelBg);
        Image arrow = new Image(this.behavior.baseModule.world.gameScreen.skin.getDrawable("bottomBoxArrow"));
        arrow.setPosition(0.0f, 8.0f, 2);
        this.group.addActor(arrow);
    }

    public void fadeOut() {
        this.group.addAction(Actions.sequence((Action)Actions.fadeOut(0.25f), (Action)Actions.run(new Runnable(){

            @Override
            public void run() {
                DropItemHint.this.destroy();
            }
        })));
    }

    private void destroy() {
        this.readyToRemove = true;
        this.behavior.dropItemHint = null;
        System.out.println("destroying ui");
    }

    @Override
    public boolean isReadyToDestroy() {
        return this.readyToRemove;
    }
}

