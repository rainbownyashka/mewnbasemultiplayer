/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlayerInput;
import com.cairn4.moonbase.PlayerStatus;
import com.cairn4.moonbase.techtree.TechManager;
import com.cairn4.moonbase.techtree.TechUpgrade;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Popup;
import com.cairn4.moonbase.worlddata.SuitLevel;
import java.util.ArrayList;

public class TechTreePopup
extends Popup {
    private static final int NUM_UNLOCK_ICONS = 10;
    GameScreen gameScreen;
    Group upgradesGroup;
    Group artifactLogGroup;
    TechManager techManager;
    TextButton tabUpgrades;
    TextButton tabDiscoveries;
    Label balanceLabel;
    Table upgradesTable;
    ScrollPane scroller;
    Table detailsTable;
    Image largeIcon;
    Label nameLabel;
    Label costLabel;
    Label unlocksLabel;
    Table unlocksTable;
    ArrayList<Image> unlockBorders = new ArrayList();
    ArrayList<Image> unlockIcons = new ArrayList();
    ArrayList<Image> checkmarkList = new ArrayList();
    ArrayList<Label> questionMarkList = new ArrayList();
    Label requiresLabel;
    Label statusLabel;
    Label unlockStatsLabel;
    TextButton btnResearch;
    private String selectedTechId;
    private Table logTable;
    ScrollPane logScroller;

    public TechTreePopup(GameScreen gameScreen) {
        super(gameScreen);
        this.gameScreen = gameScreen;
        this.techManager = this.gameScreen.world.techManager;
        this.selectedTechId = "";
        this.setup();
        this.setupTechTree();
        this.setupLog();
        this.changeTabs(0);
        this.show();
    }

    @Override
    protected void setup() {
        super.setup();
        this.setSize(1014.0f, 652.0f);
        this.setTitle("");
        this.balanceLabel = new Label((CharSequence)"Samples: XX", this.labelStyle);
        this.balanceLabel.setFontScale(0.45f);
        this.balanceLabel.setWrap(false);
        this.balanceLabel.setAlignment(16);
        this.popupGroup.addActor(this.balanceLabel);
        this.balanceLabel.setPosition(this.btnClose.getX() - 30.0f, this.panelBg.getHeight() - 43.0f, 18);
        this.updateSampleBalanceLabel();
        Table tabTable = new Table();
        tabTable.top().left();
        tabTable.setPosition(73.0f, this.panelBg.getHeight() - 53.0f, 10);
        this.popupGroup.addActor(tabTable);
        this.tabUpgrades = new TextButton(Localization.get("techUpgrades"), this.baseScreen.tabBtnStyle);
        this.tabUpgrades.getLabel().setFontScale(0.4f);
        this.tabUpgrades.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                TechTreePopup.this.changeTabs(0);
            }
        });
        tabTable.add(this.tabUpgrades).width(220.0f).height(56.0f);
        this.tabDiscoveries = new TextButton(Localization.get("research_researchLog"), this.baseScreen.tabBtnStyle);
        this.tabDiscoveries.getLabel().setFontScale(0.4f);
        this.tabDiscoveries.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                TechTreePopup.this.changeTabs(1);
            }
        });
        tabTable.add(this.tabDiscoveries).width(220.0f).height(56.0f);
        this.upgradesGroup = new Group();
        this.popupGroup.addActor(this.upgradesGroup);
        this.artifactLogGroup = new Group();
        this.popupGroup.addActor(this.artifactLogGroup);
    }

    private void setupTechTree() {
        this.setupScroller();
        this.setupDetailsTable();
        this.addDetailsContent(false, false);
        this.addTechButtons();
        this.selectUpgrade(this.techManager.techTree.upgrades.get((int)0).id);
    }

    private void setupDetailsTable() {
        this.detailsTable = new Table();
        this.detailsTable.top().left();
        this.detailsTable.setSize(this.panelBg.getWidth() - 120.0f - this.scroller.getWidth() - this.scroller.getX(), this.panelBg.getHeight() - 195.0f);
        this.detailsTable.setPosition(this.scroller.getWidth() + this.scroller.getX() + 38.0f, 60.0f);
        this.upgradesGroup.addActor(this.detailsTable);
    }

    private void changeTabs(int newTab) {
        this.tabUpgrades.setChecked(false);
        this.upgradesGroup.setVisible(false);
        this.tabDiscoveries.setChecked(false);
        this.artifactLogGroup.setVisible(false);
        switch (newTab) {
            case 0: {
                this.tabUpgrades.setChecked(true);
                this.upgradesGroup.setVisible(true);
                this.stage.setScrollFocus(this.scroller);
                break;
            }
            case 1: {
                this.tabDiscoveries.setChecked(true);
                this.artifactLogGroup.setVisible(true);
                this.stage.setScrollFocus(this.logScroller);
            }
        }
    }

    private void updateSampleBalanceLabel() {
        this.balanceLabel.setText(Localization.format("research_balance", this.techManager.samples));
    }

    private void setupScroller() {
        Image scrollBackground = new Image(new NinePatch(this.gameScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4));
        scrollBackground.setSize(12.0f, 460.0f);
        scrollBackground.setPosition(436.0f, 60.0f, 20);
        scrollBackground.getColor().a = 0.5f;
        this.upgradesGroup.addActor(scrollBackground);
        this.upgradesTable = new Table();
        this.upgradesTable.top().left();
        this.upgradesGroup.addActor(this.upgradesTable);
        this.upgradesTable.setSize(this.panelBg.getWidth() / 2.0f - 100.0f, this.panelBg.getHeight() - 275.0f);
        this.upgradesTable.setPosition(75.0f, 150.0f);
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.baseScreen.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.baseScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)this.upgradesTable, scrollStyle);
        this.scroller.setWidth(this.upgradesTable.getWidth());
        this.scroller.setHeight(this.upgradesTable.getHeight());
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setBounds(0.0f, 0.0f, this.panelBg.getWidth() / 2.0f - 147.0f, this.panelBg.getHeight() - 195.0f);
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        this.scroller.setPosition(75.0f, 60.0f);
        this.upgradesGroup.addActor(this.scroller);
        this.scroller.layout();
        this.scroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.scroller);
    }

    private void addDetailsContent(boolean suitUpgrade, boolean baseUpgrade) {
        this.detailsTable.clearChildren();
        this.unlockBorders.clear();
        this.unlockIcons.clear();
        Table topDetails = new Table();
        this.detailsTable.add(topDetails).expandX().fillX().colspan(2);
        Stack iconStack = new Stack();
        NinePatch alt_np_up = new NinePatch(this.skin.getRegion("btn-thin"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_up = new NinePatchDrawable(alt_np_up);
        Image iconBorder = new Image(alt_npd_up);
        Table iconOverlay = new Table();
        this.largeIcon = new Image(this.skin.getDrawable("white"));
        iconOverlay.add(this.largeIcon).pad(8.0f).size(100.0f, 100.0f);
        iconStack.add(iconBorder);
        iconStack.add(iconOverlay);
        topDetails.add(iconStack).space(10.0f);
        Table topTextTable = new Table();
        topDetails.add(topTextTable).fill().expand().padLeft(10.0f);
        this.nameLabel = new Label((CharSequence)"Solar Panels", this.labelStyle);
        this.nameLabel.setFontScale(0.55f);
        this.nameLabel.setColor(Color.WHITE);
        this.nameLabel.setWrap(false);
        this.nameLabel.setTouchable(Touchable.disabled);
        topTextTable.add(this.nameLabel).width(325.0f).space(10.0f);
        topTextTable.row();
        this.requiresLabel = new Label((CharSequence)Localization.get("research_requires"), this.labelStyle);
        this.requiresLabel.setFontScale(0.4f);
        this.requiresLabel.setColor(MENU_COLOR);
        this.requiresLabel.setWrap(true);
        this.requiresLabel.setAlignment(10);
        topTextTable.add(this.requiresLabel).fillX().expandX().space(10.0f);
        this.detailsTable.row();
        this.unlocksLabel = new Label((CharSequence)Localization.get("research_unlocks"), this.labelStyle);
        this.unlocksLabel.setFontScale(0.4f);
        this.unlocksLabel.setColor(MENU_COLOR);
        this.unlocksLabel.getColor().a = 0.65f;
        this.unlocksLabel.setWrap(true);
        this.unlocksLabel.setAlignment(10);
        this.detailsTable.add(this.unlocksLabel).fillX().expandX().space(10.0f).colspan(2).padLeft(5.0f);
        this.detailsTable.row();
        if (!suitUpgrade && !baseUpgrade) {
            this.unlocksTable = new Table();
            this.unlocksTable.top().left();
            this.detailsTable.add(this.unlocksTable).top().left().colspan(2).expand();
            for (int i = 0; i < 10; ++i) {
                Stack stack = new Stack();
                Image border = new Image(this.skin.getDrawable("btn-item"));
                border.setPosition(0.0f, 0.0f);
                stack.add(border);
                this.unlockBorders.add(border);
                Table overlay = new Table();
                Image icon = new Image(this.skin.getDrawable("white"));
                overlay.add(icon).pad(10.0f).fill();
                this.unlockIcons.add(icon);
                stack.add(overlay);
                this.unlocksTable.add(stack).width(85.0f).height(85.0f).expand().fill().pad(3.0f);
                if (i != 4) continue;
                this.unlocksTable.row();
            }
            this.detailsTable.row();
        } else {
            this.unlockStatsLabel = new Label((CharSequence)"", this.labelStyle);
            this.unlockStatsLabel.setFontScale(0.4f);
            this.unlockStatsLabel.setColor(MENU_COLOR);
            this.unlockStatsLabel.getColor().a = 0.65f;
            this.unlockStatsLabel.setWrap(true);
            this.unlockStatsLabel.setAlignment(10);
            this.detailsTable.add(this.unlockStatsLabel).top().left().fillX().expand().space(10.0f).colspan(2).padLeft(5.0f);
            this.detailsTable.row();
        }
        Table bottomRow = new Table();
        this.costLabel = new Label((CharSequence)"Cost: 5", this.labelStyle);
        this.costLabel.setFontScale(0.45f);
        this.costLabel.setWrap(true);
        this.costLabel.setAlignment(16);
        bottomRow.add(this.costLabel).fill().expand().space(15.0f).padBottom(3.0f);
        this.btnResearch = new TextButton(Localization.get("research"), this.gameScreen.textBtnStyle);
        this.btnResearch.getLabel().setFontScale(0.5f);
        bottomRow.add(this.btnResearch).width(204.0f).height(75.0f).expandY();
        this.btnResearch.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                TechTreePopup.this.techManager.research(TechTreePopup.this.selectedTechId);
                TechTreePopup.this.updateSampleBalanceLabel();
                TechTreePopup.this.addTechButtons();
                Audio.getInstance().playSound("sounds/techUnlocked.ogg", 0.75f);
                TechTreePopup.this.selectUpgrade(TechTreePopup.this.selectedTechId);
            }
        });
        this.detailsTable.add(bottomRow).expandX().fillX().bottom();
    }

    private void selectUpgrade(String id) {
        if (id == null) {
            this.detailsTable.setVisible(false);
        } else {
            TechUpgrade tech = this.techManager.getTech(id);
            boolean isSuitUpgrade = tech.suitLevel != 0;
            boolean isBaseUpgrade = tech.baseUpgrade;
            this.addDetailsContent(isSuitUpgrade, isBaseUpgrade);
            this.selectedTechId = id;
            this.largeIcon.setDrawable(this.skin.getDrawable("cargo"));
            if (this.skin.has("tech_" + tech.id, TextureRegion.class)) {
                this.largeIcon.setDrawable(this.skin.getDrawable("tech_" + tech.id));
            } else {
                Gdx.app.error("MewnBase", "Missing tech upgrade icon: tech_" + tech.id);
            }
            this.detailsTable.setVisible(true);
            this.nameLabel.setText(Localization.get("techtree_" + tech.id));
            if (tech.cost > 0) {
                this.costLabel.setText(Localization.format("research_cost", tech.cost));
            } else {
                this.costLabel.setText(Localization.get("research_cost_free"));
            }
            if (!tech.unlocked) {
                this.costLabel.setVisible(true);
                this.requiresLabel.setColor(Color.WHITE);
                this.listRequirements(tech);
                if (!this.techManager.hasEnoughSamples(tech)) {
                    this.costLabel.setColor(Color.RED);
                } else {
                    this.costLabel.setColor(Color.WHITE);
                }
            } else {
                this.requiresLabel.setText(Localization.get("research_alreadyResearched"));
                this.requiresLabel.setColor(Color.LIME);
                this.costLabel.setVisible(false);
            }
            if (tech.suitLevel == 0 && !tech.baseUpgrade) {
                this.listUnlockedItems(tech);
            } else if (tech.suitLevel == 0 && tech.baseUpgrade) {
                this.listBaseUpgradeStats(tech);
            } else {
                this.listUnlockStats(tech);
            }
            if (this.techManager.canResearch(tech.id)) {
                this.enableButton(this.btnResearch);
            } else {
                this.disableButton(this.btnResearch);
            }
            this.detailsTable.invalidateHierarchy();
        }
    }

    private void listBaseUpgradeStats(TechUpgrade tech) {
        String desc = "";
        if (tech.id.equals("saveWater")) {
            desc = desc + Localization.format("techtree_saveWater_desc", "25");
        } else if (tech.id.equals("lessLeaks")) {
            desc = desc + Localization.get("techtree_lessLeaks_desc");
        }
        this.unlockStatsLabel.setText(desc);
        this.unlocksTable.invalidate();
    }

    private void listUnlockStats(TechUpgrade tech) {
        SuitLevel base = PlayerStatus.suitUpgradeData.suitLevels.get(0);
        SuitLevel upgrade = PlayerStatus.suitUpgradeData.suitLevels.get(tech.suitLevel);
        float oxygenBoost = (upgrade.maxAir - base.maxAir) / base.maxAir * 100.0f;
        float suitPowerBoost = (upgrade.maxSuitPower - base.maxSuitPower) / base.maxSuitPower * 100.0f;
        String test = Localization.get("suitUpgradeBoost_desc") + "\n\n" + Localization.format("suitUpgradeBoost_maxOxygen", Float.valueOf(oxygenBoost)) + "\n" + Localization.format("suitUpgradeBoost_maxSuitPower", Float.valueOf(suitPowerBoost)) + "\n" + Localization.get("suitUpgradeBoost_lessHungry");
        this.unlockStatsLabel.setText(test);
        this.unlocksTable.invalidate();
    }

    private void listUnlockedItems(TechUpgrade tech) {
        boolean canResearch = this.techManager.meetsPreReqs(tech.id);
        ArrayList<String> unlockedItems = ItemFactory.getInstance().getResearchUnlocksFor(tech.id);
        for (int i = 0; i < 10; ++i) {
            Image border = this.unlockBorders.get(i);
            Image icon = this.unlockIcons.get(i);
            if (i < unlockedItems.size()) {
                border.setVisible(true);
                icon.setVisible(true);
                ItemFactory.getInstance();
                String spriteName = ItemFactory.getItemSprite(unlockedItems.get(i));
                try {
                    icon.setDrawable(this.skin.getDrawable(spriteName));
                }
                catch (GdxRuntimeException e) {
                    MoonBase.error("TechTreePopup: Can't find item sprite: " + spriteName);
                    icon.setDrawable(this.skin.getDrawable("missing"));
                }
                icon.clearListeners();
                final String tooltipTextTemp = Item.getName(unlockedItems.get(i));
                final String tooltipDescText = Item.getDesc(unlockedItems.get(i));
                if (canResearch) {
                    icon.addListener(new ClickListener(){

                        @Override
                        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                            TechTreePopup.this.gameScreen.hud.createTooltip(tooltipTextTemp, tooltipDescText);
                        }

                        @Override
                        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                            TechTreePopup.this.gameScreen.hud.removeTooltip();
                        }
                    });
                    continue;
                }
                icon.clearListeners();
                icon.addListener(new ClickListener(){

                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        TechTreePopup.this.gameScreen.hud.createTooltip(tooltipTextTemp, tooltipDescText);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        TechTreePopup.this.gameScreen.hud.removeTooltip();
                    }
                });
                continue;
            }
            if (i < 10) {
                border.setVisible(true);
                border.getColor().a = 0.35f;
            } else {
                border.setVisible(false);
            }
            icon.setVisible(false);
            icon.clearListeners();
        }
    }

    private void listRequirements(TechUpgrade tech) {
        if (!this.techManager.meetsPreReqs(tech.id) && !tech.unlocked) {
            this.requiresLabel.setColor(MENU_COLOR);
            String req = Localization.get("research_requires");
            req = req + " ";
            ArrayList<String> missingTechs = new ArrayList<String>();
            for (String s : tech.preReqTech) {
                if (this.techManager.getTech((String)s).unlocked) continue;
                missingTechs.add(s);
            }
            for (int i = 0; i < missingTechs.size(); ++i) {
                if (this.techManager.getTech((String)((String)missingTechs.get((int)i))).unlocked) continue;
                req = req + "[#ff0000]";
                req = req + Localization.get("techtree_" + (String)missingTechs.get(i));
                req = req + "[]";
                if (i >= missingTechs.size() - 1) continue;
                req = req + ", ";
            }
            this.requiresLabel.setText(req);
        } else {
            this.requiresLabel.setText("");
        }
    }

    private void addTechButtons() {
        this.upgradesTable.clearChildren();
        NinePatch alt_np_up = new NinePatch(this.skin.getRegion("btn-thin"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_up = new NinePatchDrawable(alt_np_up);
        NinePatch alt_np_active = new NinePatch(this.skin.getRegion("btn-thin-hover"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_active = new NinePatchDrawable(alt_np_active);
        NinePatch alt_np_down = new NinePatch(this.skin.getRegion("btn-thin-pressed"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_down = new NinePatchDrawable(alt_np_down);
        Button.ButtonStyle style = new Button.ButtonStyle();
        style.up = alt_npd_up;
        style.over = alt_npd_active;
        style.down = alt_npd_down;
        int cols = 3;
        int c = 0;
        for (int i = 0; i < this.techManager.techTree.upgrades.size(); ++i) {
            TechUpgrade tech = this.techManager.getTech(this.techManager.techTree.upgrades.get((int)i).id);
            Stack stack = new Stack();
            Button btnTech = new Button(style);
            Table overlay = new Table();
            overlay.setTouchable(Touchable.disabled);
            Image icon = new Image(this.skin.getDrawable("missing"));
            if (this.skin.has("tech_" + tech.id, TextureRegion.class)) {
                icon.setDrawable(this.skin.getDrawable("tech_" + tech.id));
            } else {
                Gdx.app.error("MewnBase", "Missing tech upgrade icon");
            }
            icon.setTouchable(Touchable.disabled);
            overlay.add(icon).pad(12.0f);
            stack.add(btnTech);
            stack.add(overlay);
            Table checkmarkLayer = new Table();
            checkmarkLayer.bottom().right();
            Image checkmark = new Image(this.skin.getDrawable("checkmark"));
            checkmark.setTouchable(Touchable.disabled);
            checkmark.setColor(Color.valueOf("1bba0f"));
            checkmark.setVisible(false);
            checkmarkLayer.add(checkmark).width(44.0f).height(40.0f).bottom().right().pad(10.0f);
            stack.add(checkmarkLayer);
            this.checkmarkList.add(checkmark);
            final String fId = tech.id;
            btnTech.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent e, float x, float y) {
                    TechTreePopup.this.selectUpgrade(fId);
                }
            });
            this.upgradesTable.add(stack).height(100.0f).width(104.0f).space(7.0f);
            if (++c == cols) {
                c = 0;
                this.upgradesTable.row();
            }
            btnTech.setColor(Color.WHITE);
            icon.setColor(Color.WHITE);
            if (this.techManager.canResearch(tech.id)) {
                btnTech.getColor().a = 1.0f;
            }
            if (!this.techManager.meetsPreReqs(tech.id)) {
                icon.setColor(1.0f, 1.0f, 1.0f, 0.25f);
                btnTech.setColor(Color.RED);
                btnTech.getColor().a = 0.5f;
            }
            if (!tech.unlocked) continue;
            btnTech.setColor(Color.GREEN);
            btnTech.getColor().a = 1.0f;
            icon.getColor().a = 1.0f;
            checkmark.setVisible(true);
        }
    }

    private void setupLog() {
        this.setupLogScroller();
        this.addLogItems();
    }

    private void setupLogScroller() {
        Image scrollBackground = new Image(new NinePatch(this.gameScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4));
        scrollBackground.setSize(10.0f, this.panelBg.getHeight() - 195.0f);
        scrollBackground.setPosition(this.panelBg.getWidth() - 75.0f, 60.0f, 20);
        scrollBackground.getColor().a = 0.5f;
        this.artifactLogGroup.addActor(scrollBackground);
        this.logTable = new Table();
        this.logTable.top().left();
        this.artifactLogGroup.addActor(this.logTable);
        this.logTable.setSize(this.panelBg.getWidth() - 150.0f, this.panelBg.getHeight() - 175.0f);
        this.logTable.setPosition(90.0f, 150.0f);
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        NinePatch scrollbar = new NinePatch(this.baseScreen.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.baseScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.logScroller = new ScrollPane((Actor)this.logTable, scrollStyle);
        this.logScroller.setWidth(this.logTable.getWidth());
        this.logScroller.setHeight(this.logTable.getHeight());
        this.logScroller.setLayoutEnabled(true);
        this.logScroller.setVariableSizeKnobs(true);
        this.logScroller.setFadeScrollBars(false);
        this.logScroller.layout();
        this.logScroller.setBounds(0.0f, 0.0f, this.panelBg.getWidth() - 150.0f, this.panelBg.getHeight() - 195.0f);
        this.logScroller.setFlingTime(0.1f);
        this.logScroller.setScrollingDisabled(true, false);
        this.logScroller.setOverscroll(false, false);
        this.logScroller.setFlickScroll(false);
        this.logScroller.setSmoothScrolling(true);
        this.logScroller.setPosition(75.0f, 60.0f);
        this.artifactLogGroup.addActor(this.logScroller);
        this.logScroller.layout();
        this.logScroller.cancelTouchFocus();
        this.stage.setScrollFocus(this.logScroller);
    }

    private void addLogItems() {
        this.logTable.clearChildren();
        int cols = 6;
        int c = 0;
        for (int i = 0; i < 18; ++i) {
            Stack stack = new Stack();
            Table overlay = new Table();
            Image icon = new Image(this.skin.getDrawable("ro" + i));
            icon.setTouchable(Touchable.enabled);
            overlay.add(icon).pad(3.0f);
            stack.add(overlay);
            String toolTipText = Localization.get("item_researchObject") + " " + (i + 1);
            if (this.researchItemFound(i)) {
                icon.setColor(Color.WHITE);
            } else {
                toolTipText = Localization.get("item_researchObject_unknown");
                icon.setColor(0.0f, 0.0f, 0.0f, 0.5f);
            }
            final String fToolTipText = toolTipText;
            icon.addListener(new ClickListener(){

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    TechTreePopup.this.createTooltip(fToolTipText);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    TechTreePopup.this.removeTooltip();
                }
            });
            this.logTable.add(stack).height(130.0f).width(130.0f).space(7.0f);
            if (++c != cols) continue;
            c = 0;
            this.logTable.row();
        }
    }

    private boolean researchItemFound(int id) {
        for (Integer i : this.gameScreen.world.player.researchObjectsDiscovered) {
            if (!i.equals(id)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void handleInput() {
        if (PlayerInput.wasPressed(27)) {
            this.back();
        }
        super.handleInput();
    }

    @Override
    public void back() {
        super.back();
        this.gameScreen.world.player.playerAnimController.closeTablet();
    }
}

