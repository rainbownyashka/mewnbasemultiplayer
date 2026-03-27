/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.NpcBonuses;
import com.cairn4.moonbase.entities.NpcFriendState;
import com.cairn4.moonbase.entities.NpcManager;
import com.cairn4.moonbase.tiles.NpcHome;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;

public class NpcAssignmentPopup
extends Popup
implements Telegraph {
    private NpcHome npcHome;
    private Table table;
    private ScrollPane scroller;
    private Button.ButtonStyle buttonStyle;
    private Button.ButtonStyle assignHomeButtonStyle;
    private Button.ButtonStyle removeHomeButtonStyle;

    public NpcAssignmentPopup(BaseScreen baseScreen, NpcHome npcHome) {
        super(baseScreen);
        MessageManager.getInstance().addListener(this, 4);
        this.npcHome = npcHome;
        this.setup();
        this.setSize(900.0f, 600.0f);
        this.show();
    }

    @Override
    protected void setup() {
        super.setup();
        this.setTitle("select_friends_menu");
        NinePatch np_up = new NinePatch(this.skin.getRegion("btn-item"), 16, 16, 16, 16);
        NinePatchDrawable npd_up = new NinePatchDrawable(np_up);
        NinePatch np_active = new NinePatch(this.skin.getRegion("btn-item-active"), 16, 16, 16, 16);
        NinePatchDrawable npd_active = new NinePatchDrawable(np_active);
        NinePatch np_down = new NinePatch(this.skin.getRegion("btn-item-pressed"), 16, 16, 16, 16);
        NinePatchDrawable npd_down = new NinePatchDrawable(np_down);
        this.buttonStyle = new Button.ButtonStyle();
        this.buttonStyle.up = npd_up;
        this.buttonStyle.over = npd_active;
        this.buttonStyle.down = npd_down;
        this.assignHomeButtonStyle = new Button.ButtonStyle();
        this.assignHomeButtonStyle.up = this.skin.getDrawable("assign-home");
        this.assignHomeButtonStyle.over = this.skin.getDrawable("assign-home-hover");
        this.assignHomeButtonStyle.down = this.skin.getDrawable("assign-home-pressed");
        this.removeHomeButtonStyle = new Button.ButtonStyle();
        this.removeHomeButtonStyle.up = this.skin.getDrawable("delete-icon");
        this.removeHomeButtonStyle.over = this.skin.getDrawable("delete-icon-hover");
        this.removeHomeButtonStyle.down = this.skin.getDrawable("delete-icon-pressed");
        this.setupTable();
        this.addListOptions();
    }

    private void setupTable() {
        this.table = new Table();
        this.table.top().left();
        this.popupGroup.addActor(this.table);
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)this.table, scrollStyle);
        this.scroller.setWidth(this.table.getWidth());
        this.scroller.setHeight(this.table.getHeight());
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setBounds(0.0f, 0.0f, this.panelBg.getWidth() - 50.0f, this.panelBg.getHeight() - 195.0f);
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        this.scroller.setPosition(80.0f, 60.0f);
        this.popupGroup.addActor(this.scroller);
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.scroller);
    }

    private void addListOptions() {
        for (final Npc npc : NpcManager.getInstance().activeNpcList) {
            if (!npc.discovered) continue;
            Group g = new Group();
            Button button = new Button(this.buttonStyle);
            g.addActor(button);
            button.setFillParent(true);
            button.setTouchable(Touchable.childrenOnly);
            button.setColor(1.0f, 1.0f, 1.0f, 0.6f);
            button.layout();
            Group head = new Group();
            head.setPosition(30.0f, 16.0f);
            head.setScale(0.5f, 0.5f);
            g.addActor(head);
            Image visor = new Image(this.skin.getDrawable("visor"));
            visor.setPosition(32.0f, 72.0f);
            head.addActor(visor);
            Image face = new Image();
            face = new Image(this.skin.getDrawable("face0"));
            face.setPosition(58.0f, 30.0f);
            head.addActor(face);
            Image playerHead = new Image(this.skin.getDrawable("player-head-1-side"));
            head.addActor(playerHead);
            Label nameLabel = new Label((CharSequence)npc.npcId, this.labelStyle);
            nameLabel.setFontScale(0.5f);
            nameLabel.setPosition(140.0f, 45.0f);
            nameLabel.setTouchable(Touchable.disabled);
            g.addActor(nameLabel);
            String warningText = "";
            NpcFriendState friendState = npc.friendStateMachine.getCurrentState();
            switch (friendState) {
                case NOT_FRIEND: {
                    break;
                }
                case NO_HOUSE_IDLE: {
                    warningText = Localization.get("npc_status_noHome");
                    break;
                }
                case NO_HOUSE_CLOSEENOUGH: {
                    warningText = Localization.get("npc_status_homeTooFar");
                    break;
                }
                case PATHING_TO_HOUSE: {
                    break;
                }
                case HAVE_HOUSE: {
                    break;
                }
                case HOME_SAD: {
                    warningText = Localization.get("npc_status_homeProblems");
                    break;
                }
                case RESPONSE_DIALOG: {
                    break;
                }
                case FRIEND_IDLE: {
                    break;
                }
                case FOLLOW_MOVING: {
                    break;
                }
            }
            Label warningStatus = new Label((CharSequence)warningText, this.labelStyle);
            warningStatus.setFontScale(0.32f);
            warningStatus.setColor(Color.valueOf("fc3c3c"));
            warningStatus.setPosition(nameLabel.getX() + nameLabel.getPrefWidth() + 20.0f, 45.0f);
            warningStatus.setTouchable(Touchable.disabled);
            g.addActor(warningStatus);
            NpcBonuses.bonusTypes npcBonus = npc.npcDef.rewardBonus;
            Label job = new Label((CharSequence)Localization.get("bonus_" + (Object)((Object)npcBonus)), this.labelStyle);
            job.setFontScale(0.32f);
            job.setColor(Color.valueOf("00ffff"));
            job.setPosition(141.0f, 41.0f);
            job.setAlignment(12, 12);
            job.setTouchable(Touchable.disabled);
            g.addActor(job);
            if (npc.bonusEarned) {
                Label perk = new Label((CharSequence)NpcBonuses.getInstance().getBonusDesc(npc.npcDef.rewardBonus), this.labelStyle);
                perk.setFontScale(0.32f);
                perk.setColor(Color.valueOf("00ffff"));
                perk.setPosition(141.0f, 22.0f);
                perk.setAlignment(12, 12);
                perk.setTouchable(Touchable.disabled);
                g.addActor(perk);
                perk.getColor().a = !npc.bonusActive ? 0.5f : 1.0f;
            }
            if (npc.home == this.npcHome) {
                Button btnRemoveHome = new Button(this.removeHomeButtonStyle);
                btnRemoveHome.setSize(75.0f, 75.0f);
                btnRemoveHome.setPosition(685.0f, 97.0f, 18);
                g.addActor(btnRemoveHome);
                Npc removeNpc = npc;
                btnRemoveHome.addListener(new ClickListener(){

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        NpcManager.getInstance().homeRemoved(NpcAssignmentPopup.this.npcHome);
                    }
                });
            } else {
                Button btnAssignHome = new Button(this.assignHomeButtonStyle);
                btnAssignHome.setSize(75.0f, 75.0f);
                btnAssignHome.setPosition(685.0f, 97.0f, 18);
                g.addActor(btnAssignHome);
                Npc assignee = npc;
                btnAssignHome.addListener(new ClickListener(){

                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        NpcManager.getInstance().assignNpcToHome(npc, NpcAssignmentPopup.this.npcHome);
                    }
                });
            }
            this.table.add(g).height(120.0f).expandX().fillX().padRight(18.0f).spaceBottom(10.0f);
            this.table.row();
        }
    }

    @Override
    public boolean handleMessage(Telegram msg) {
        if (msg.message == 4) {
            System.out.println("Got npc home assignment update telegram");
            this.table.clearChildren();
            this.addListOptions();
        }
        return false;
    }
}

