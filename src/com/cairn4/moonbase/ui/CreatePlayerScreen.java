/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.SpineActor;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.LoadingScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.MissionBriefing;
import com.cairn4.moonbase.ui.Popup;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;

public class CreatePlayerScreen
extends Popup {
    Button.ButtonStyle avatarArrowBtn;
    Button.ButtonStyle avatarArrowBtnR;
    Button btnClose;
    TextButton btnTutorial;
    TextButton btnNormal;
    TextButton btnCreative;
    TextButton btnWorldSettings;
    Table table;
    private BitmapFont nameFieldFont;
    private TextField nameField;
    private Label.LabelStyle nameFieldLabelStyle;
    private Label nameFieldLabel;
    public SpineActor spineActor;
    private int colorIndex = 0;
    private Slot visorSlot;
    private Slot chestColorSlot;
    private int faceIndex = 0;
    private Slot faceSlot;
    private Label colorLabel;
    private Label faceLabel;
    private TextButton btnBack;
    private TextButton btnLoad;
    SkeletonRenderer skeletonRenderer = new SkeletonRenderer();
    float timer = 0.0f;
    float delay = 0.2f;

    public CreatePlayerScreen(BaseScreen baseScreen) {
        super(baseScreen);
        this.skeletonRenderer.setPremultipliedAlpha(false);
        this.tintBgAlpha = 0.0f;
        this.setup();
        this.show();
        this.nameField.getStyle().font.getData().setScale(0.45f);
    }

    @Override
    public void setup() {
        super.setup();
        this.setSize(800.0f, 580.0f);
        this.setTitle("character_customization");
        this.avatarArrowBtn = new Button.ButtonStyle();
        this.avatarArrowBtn.up = this.skin.getDrawable("btn-arrow-left");
        this.avatarArrowBtn.over = this.skin.getDrawable("btn-arrow-left-hover");
        this.avatarArrowBtn.down = this.skin.getDrawable("btn-arrow-left-pressed");
        this.avatarArrowBtnR = new Button.ButtonStyle();
        this.avatarArrowBtnR.up = this.skin.getDrawable("btn-arrow-right");
        this.avatarArrowBtnR.over = this.skin.getDrawable("btn-arrow-right-hover");
        this.avatarArrowBtnR.down = this.skin.getDrawable("btn-arrow-right-pressed");
        this.nameFieldFont = AssetManagerSingleton.getInstance().get("smallfont1.fnt", BitmapFont.class);
        this.nameFieldFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        String cs = "0123456789";
        this.nameFieldFont.setFixedWidthGlyphs(cs);
        this.nameFieldFont.getData().markupEnabled = true;
        this.nameFieldFont.getData().setScale(0.4f, 0.4f);
        this.nameFieldLabelStyle = new Label.LabelStyle(this.nameFieldFont, Color.WHITE);
        this.addWidgets();
        this.changeFace(0);
        this.changeColor(0);
    }

    public void addWidgets() {
        float basicRowHeight = 80.0f;
        float spacing = 10.0f;
        this.spineActor = new SpineActor("player", 0.8f, this.skeletonRenderer);
        this.spineActor.setPosition(200.0f, 210.0f);
        this.popupGroup.addActor(this.spineActor);
        this.spineActor.state.setAnimation(0, "idle", true);
        this.spineActor.state.setAnimation(1, "blink", true);
        this.spineActor.state.setAnimation(2, "lookAround", true);
        this.spineActor.skeleton.setSkin("mk1");
        Slot slot = this.spineActor.skeleton.findSlot("player/item_slot");
        slot.setAttachment(null);
        this.visorSlot = this.spineActor.skeleton.findSlot("visor");
        this.visorSlot.getColor().set(Color.valueOf(Player.suitColorList[this.colorIndex]));
        this.chestColorSlot = this.spineActor.skeleton.findSlot("torso-color");
        this.chestColorSlot.getColor().set(Color.valueOf(Player.suitColorList[this.colorIndex]));
        this.faceSlot = this.spineActor.skeleton.findSlot("face");
        Attachment a = this.spineActor.skeleton.getAttachment("face", "player/face" + this.faceIndex);
        this.faceSlot.setAttachment(a);
        Slot eyeSlot = this.spineActor.skeleton.findSlot("eyes-slot");
        eyeSlot.getColor().set(Color.BLACK);
        Table optionsTable = new Table();
        optionsTable.setSize(370.0f, 200.0f);
        optionsTable.setPosition(this.panelBg.getWidth() - 75.0f, 410.0f, 18);
        optionsTable.center();
        this.popupGroup.addActor(optionsTable);
        Label enterName = new Label((CharSequence)Localization.get("newgame_characternameLabel"), this.labelStyle);
        enterName.setFontScale(0.4f);
        enterName.setAlignment(1);
        optionsTable.add(enterName).expandX().fillX().colspan(3);
        optionsTable.row();
        NinePatch alt_np_up = new NinePatch(this.skin.getRegion("btn-minor"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_up = new NinePatchDrawable(alt_np_up);
        NinePatch alt_np_active = new NinePatch(this.skin.getRegion("btn-minor-pressed"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_active = new NinePatchDrawable(alt_np_active);
        NinePatch selected_np = new NinePatch(this.skin.getRegion("slider-bg"), 5, 5, 5, 5);
        NinePatchDrawable selected_npd = new NinePatchDrawable(selected_np);
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.background = alt_npd_up;
        textFieldStyle.focusedBackground = alt_npd_active;
        textFieldStyle.font = this.nameFieldFont;
        textFieldStyle.font.getData().setScale(0.45f);
        textFieldStyle.cursor = this.skin.getDrawable("hud-progress-knob");
        textFieldStyle.selection = selected_npd;
        textFieldStyle.fontColor = Menu.MENU_COLOR;
        textFieldStyle.focusedFontColor = Color.WHITE;
        textFieldStyle.messageFontColor = new Color(Menu.MENU_COLOR).sub(0.0f, 0.0f, 0.0f, 0.7f);
        textFieldStyle.messageFont = this.nameFieldFont;
        Stack s = new Stack();
        this.nameField = new TextField("", textFieldStyle);
        this.nameField.setMaxLength(16);
        this.nameField.setMessageText(Localization.get("newgame_entername"));
        this.nameField.setAlignment(1);
        this.nameField.setTextFieldListener(new TextField.TextFieldListener(){

            @Override
            public void keyTyped(TextField textField, char c) {
                CreatePlayerScreen.this.setButtonsEnabled(textField.getText().length() > 0);
            }
        });
        s.add(this.nameField);
        s.pack();
        optionsTable.add(s).colspan(3).fillX().height(70.0f).spaceBottom(15.0f);
        optionsTable.row();
        Button colorLeft = new Button(this.avatarArrowBtn);
        colorLeft.setOrigin(1);
        colorLeft.setScaleX(-1.0f);
        optionsTable.add(colorLeft).padLeft(25.0f);
        colorLeft.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                CreatePlayerScreen.this.changeColor(-1);
            }
        });
        this.colorLabel = new Label((CharSequence)(Localization.get("newgame_color") + " " + (this.colorIndex + 1)), this.labelStyle);
        this.colorLabel.setFontScale(0.5f);
        this.colorLabel.setAlignment(1);
        this.colorLabel.setColor(Menu.MENU_COLOR);
        this.colorLabel.getColor().a = 0.5f;
        optionsTable.add(this.colorLabel).fillX().expandX();
        Button colorRight = new Button(this.avatarArrowBtnR);
        colorRight.setOrigin(1);
        optionsTable.add(colorRight).padRight(25.0f);
        colorRight.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                CreatePlayerScreen.this.changeColor(1);
            }
        });
        optionsTable.row();
        optionsTable.add().colspan(3).height(15.0f);
        optionsTable.row();
        Button faceLeft = new Button(this.avatarArrowBtn);
        faceLeft.setOrigin(1);
        faceLeft.setScaleX(-1.0f);
        optionsTable.add(faceLeft).padLeft(25.0f);
        faceLeft.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                CreatePlayerScreen.this.changeFace(-1);
            }
        });
        this.faceLabel = new Label((CharSequence)(Localization.get("newgame_face") + " " + (this.faceIndex + 1)), this.labelStyle);
        this.faceLabel.setFontScale(0.5f);
        this.faceLabel.setAlignment(1);
        this.faceLabel.setColor(Menu.MENU_COLOR);
        this.faceLabel.getColor().a = 0.5f;
        optionsTable.add(this.faceLabel).fillX().expandX();
        Button faceRight = new Button(this.avatarArrowBtnR);
        faceRight.setOrigin(1);
        optionsTable.add(faceRight).padRight(25.0f);
        faceRight.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                CreatePlayerScreen.this.changeFace(1);
            }
        });
        optionsTable.row();
        Table botRowTable = new Table();
        botRowTable.setHeight(90.0f);
        botRowTable.setWidth(this.panelBg.getWidth() - 150.0f);
        botRowTable.setPosition(this.panelBg.getWidth() / 2.0f, 110.0f, 1);
        this.popupGroup.addActor(botRowTable);
        this.btnBack = new TextButton(Localization.get("back").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnBack.getLabel().setFontScale(0.6f);
        this.btnBack.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                CreatePlayerScreen.this.back();
            }
        });
        botRowTable.add(this.btnBack).left().width(220.0f).height(90.0f).expandX();
        this.btnLoad = new TextButton(Localization.get("start").toUpperCase(), this.baseScreen.textBtnStyle);
        this.btnLoad.getLabel().setFontScale(0.6f);
        this.btnLoad.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                CreatePlayerScreen.this.nextMenu();
            }
        });
        botRowTable.add(this.btnLoad).width(220.0f).height(90.0f).right().expandX();
    }

    private void setupCharacter() {
        String s = this.nameField.getText();
        Mission m = MoonBase.getCurrentMission();
        m.setName(s);
        m.characterFaceOption = this.faceIndex;
        m.characterSuitColor = Player.suitColorList[this.colorIndex];
    }

    private void changeColor(int direction) {
        if (direction == -1) {
            this.colorIndex = this.colorIndex == 0 ? Player.suitColorList.length - 1 : --this.colorIndex;
        } else if (direction == 1) {
            this.colorIndex = this.colorIndex == Player.suitColorList.length - 1 ? 0 : ++this.colorIndex;
        }
        this.visorSlot.getColor().set(Color.valueOf(Player.suitColorList[this.colorIndex]));
        this.chestColorSlot.getColor().set(Color.valueOf(Player.suitColorList[this.colorIndex]));
        this.colorLabel.setText(Localization.get("newgame_color") + " " + (this.colorIndex + 1) + " / " + Player.suitColorList.length);
    }

    private void changeFace(int direction) {
        if (direction == -1) {
            this.faceIndex = this.faceIndex == 0 ? Player.faceOptions - 1 : --this.faceIndex;
        } else if (direction == 1) {
            this.faceIndex = this.faceIndex == Player.faceOptions - 1 ? 0 : ++this.faceIndex;
        }
        Attachment a = this.spineActor.skeleton.getAttachment("face", "player/face" + this.faceIndex);
        this.faceSlot.setAttachment(a);
        this.faceLabel.setText(Localization.get("newgame_face") + " " + (this.faceIndex + 1) + " / " + Player.faceOptions);
    }

    private void setButtonsEnabled(boolean enabled) {
        this.btnBack.setDisabled(!enabled);
        this.btnLoad.setDisabled(!enabled);
        if (enabled) {
            this.btnBack.setTouchable(Touchable.enabled);
            this.btnLoad.setTouchable(Touchable.enabled);
        } else {
            this.btnBack.setTouchable(Touchable.disabled);
            this.btnLoad.setTouchable(Touchable.disabled);
        }
        if (this.nameField.getText().length() == 0) {
            this.btnLoad.setDisabled(true);
            this.btnLoad.setTouchable(Touchable.disabled);
        }
    }

    private void nextMenu() {
        this.baseScreen.menuForwardSound();
        MoonBase.achievementAdapter.startNewGame();
        final Mission mission = MoonBase.getCurrentMission();
        String nameString = this.nameField.getText();
        mission.setName(nameString);
        mission.characterFaceOption = this.faceIndex;
        mission.characterSuitColor = Player.suitColorList[this.colorIndex];
        if (mission.missionType == Mission.MissionTypes.normal) {
            this.font.getData().setScale(1.0f);
            this.baseScreen.showMenu(new MissionBriefing(this.baseScreen));
        } else {
            this.menuGroup.addAction(Actions.sequence((Action)Actions.parallel((Action)Actions.moveBy(0.0f, 200.0f, 0.5f, Interpolation.sineIn), (Action)Actions.fadeOut(0.5f))));
            Image fadeOut = new Image(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
            fadeOut.setSize(MoonBase.SCREEN_WIDTH, MoonBase.SCREEN_HEIGHT);
            fadeOut.setColor(0.0f, 0.0f, 0.0f, 0.0f);
            this.stage.addActor(fadeOut);
            fadeOut.addAction(Actions.sequence((Action)Actions.fadeIn(0.5f), (Action)Actions.delay(0.3f), (Action)Actions.run(new Runnable(){

                @Override
                public void run() {
                    CreatePlayerScreen.this.font.getData().setScale(1.0f);
                    switch (mission.missionType) {
                        case tutorial: {
                            CreatePlayerScreen.this.baseScreen.game.setScreen(new LoadingScreen(CreatePlayerScreen.this.baseScreen.game, true));
                            break;
                        }
                        case normal: {
                            break;
                        }
                        case creative: {
                            CreatePlayerScreen.this.baseScreen.game.setScreen(new LoadingScreen(CreatePlayerScreen.this.baseScreen.game, true));
                            break;
                        }
                        case endless: {
                            CreatePlayerScreen.this.baseScreen.game.setScreen(new LoadingScreen(CreatePlayerScreen.this.baseScreen.game, true));
                        }
                    }
                }
            })));
        }
    }

    @Override
    public void show() {
        this.setButtonsEnabled(false);
        super.show();
    }

    @Override
    protected void finishedShowAnim() {
        this.setButtonsEnabled(true);
        this.stage.setKeyboardFocus(this.nameField);
    }

    private void demoSkins(float delta) {
        this.timer += delta;
        if (this.timer > this.delay) {
            this.timer = 0.0f;
            this.changeColor(1);
            this.changeFace(1);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.spineActor.update(delta);
    }

    @Override
    public void back() {
        super.back();
        this.stage.setKeyboardFocus(null);
        this.baseScreen.menuBackSound();
    }

    @Override
    public void returned() {
        this.font.getData().setScale(0.45f);
        this.show();
        this.menuGroup.addAction(Actions.sequence((Action)Actions.fadeIn(0.3f)));
    }
}

