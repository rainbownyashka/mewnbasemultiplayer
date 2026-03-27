/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.tiles.Lander;
import com.cairn4.moonbase.tiles.LaunchPad;
import com.cairn4.moonbase.ui.CameraLag;
import com.cairn4.moonbase.ui.Hud;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;

public class LaunchControls {
    private Hud hud;
    private Group parentGroup;
    private TextButton.TextButtonStyle launchButtonStyle;
    public Group group;
    public Image bgPanel;
    private Table table;
    public TextButton btnLaunch;
    public TextButton btnCancelLaunch;
    public Label launchLabel;
    private final Vector2 normalPosition;
    private boolean visible;
    public Lander lander;
    private Group countdownGroup;
    private Image countdownGlow;
    private Label countdownTimerShadow;
    private Label countdownTimer;
    private int countdown = 6;
    private final Image warningLines;
    private boolean countingDown = false;
    private float countdownTimerDelay = 0.0f;
    private final Color sysColor;
    private final Table systemsTable;
    public LaunchPad launchPad;
    private Sound ambientRocketSound;
    private long ambientRocketSoundId;

    public LaunchControls(final Hud hud) {
        this.hud = hud;
        NinePatch np_launch_up = new NinePatch(hud.skin.getRegion("hud/btn-launch"), 16, 16, 16, 16);
        NinePatchDrawable npd_launch_up = new NinePatchDrawable(np_launch_up);
        NinePatch np_launch_active = new NinePatch(hud.skin.getRegion("hud/btn-launch-hover"), 16, 16, 16, 16);
        NinePatchDrawable npd_launch_active = new NinePatchDrawable(np_launch_active);
        NinePatch np_launch_down = new NinePatch(hud.skin.getRegion("hud/btn-launch-pressed"), 16, 16, 16, 16);
        NinePatchDrawable npd_launch_down = new NinePatchDrawable(np_launch_down);
        this.launchButtonStyle = new TextButton.TextButtonStyle();
        this.launchButtonStyle.up = npd_launch_up;
        this.launchButtonStyle.over = npd_launch_active;
        this.launchButtonStyle.down = npd_launch_down;
        this.launchButtonStyle.unpressedOffsetY = 2.0f;
        this.launchButtonStyle.pressedOffsetY = -1.0f;
        this.launchButtonStyle.font = hud.baseScreen.font;
        this.group = new Group();
        this.group.setPosition(MoonBase.SCREEN_WIDTH / 2, 134.0f, 1);
        this.normalPosition = new Vector2(this.group.getX(), this.group.getY());
        this.countdownGlow = new AdditiveImage(hud.skin.getDrawable("small-gradient-circle"));
        this.countdownGlow.setSize(500.0f, 420.0f);
        this.countdownGlow.setOrigin(1);
        this.countdownGlow.setTouchable(Touchable.disabled);
        this.countdownGlow.setPosition(0.0f, 15.0f, 1);
        this.countdownGlow.setColor(1.0f, 0.0f, 0.0f, 0.5f);
        this.group.addActor(this.countdownGlow);
        this.countdownGlow.addAction(Actions.forever(Actions.sequence((Action)Actions.alpha(0.7f, 1.0f), (Action)Actions.alpha(0.0f, 1.0f))));
        NinePatch panelPatch = new NinePatch(hud.baseScreen.skin.getRegion("hud/launch-panel"), 60, 60, 60, 60);
        this.bgPanel = new Image(panelPatch);
        this.bgPanel.getColor().a = 1.0f;
        this.group.addActor(this.bgPanel);
        this.warningLines = new Image(hud.baseScreen.skin.getDrawable("hud/launch-warning"));
        this.warningLines.setColor(1.0f, 1.0f, 1.0f, 0.2f);
        this.warningLines.setPosition(0.0f, 31.0f, 1);
        this.warningLines.setName("warningLines");
        this.group.addActor(this.warningLines);
        this.table = new Table();
        this.table.center();
        this.group.addActor(this.table);
        this.btnLaunch = new TextButton("LAUNCH", this.launchButtonStyle);
        this.btnLaunch.getLabel().setFontScale(0.5f);
        this.btnLaunch.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                LaunchControls.this.startCountdown();
            }
        });
        this.table.add(this.btnLaunch).width(170.0f).height(85.0f).expandX().spaceBottom(15.0f).padTop(10.0f);
        this.table.row();
        this.btnCancelLaunch = new TextButton(Localization.get("cancel"), hud.baseScreen.altTextBtnStyle);
        this.btnCancelLaunch.getLabel().setFontScale(0.35f);
        this.btnCancelLaunch.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                LaunchControls.this.launchPad.rocketDrawable.openDoor();
                LaunchControls.this.launchPad.launching = false;
                hud.hideLaunchControls();
            }
        });
        this.table.add(this.btnCancelLaunch).width(170.0f).height(60.0f).expandX();
        this.bgPanel.setSize(353.0f, 231.0f);
        this.group.setSize(this.bgPanel.getWidth(), this.bgPanel.getHeight());
        this.bgPanel.setOrigin(1);
        this.bgPanel.setPosition(0.0f, 0.0f, 1);
        this.systemsTable = new Table();
        this.systemsTable.setSize(254.0f, 120.0f);
        this.systemsTable.center();
        this.group.addActor(this.systemsTable);
        this.systemsTable.setPosition(0.0f, 0.0f, 1);
        this.sysColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
        this.countdownGroup = new Group();
        this.countdownGroup.setPosition(0.0f, 0.0f, 1);
        this.group.addActor(this.countdownGroup);
        this.countdownTimerShadow = new Label((CharSequence)"5", hud.baseScreen.headingStyle);
        this.countdownTimerShadow.setWidth(170.0f);
        this.countdownTimerShadow.setColor(Color.BLACK);
        this.countdownTimerShadow.setAlignment(1, 1);
        this.countdownTimerShadow.setPosition(0.0f, -10.0f, 1);
        this.countdownGroup.addActor(this.countdownTimerShadow);
        this.countdownTimer = new Label((CharSequence)"5", hud.baseScreen.headingStyle);
        this.countdownTimer.setWidth(170.0f);
        this.countdownTimer.setAlignment(1, 1);
        this.countdownTimer.setPosition(0.0f, -5.0f, 1);
        this.countdownGroup.addActor(this.countdownTimer);
        this.countdownGroup.setTouchable(Touchable.disabled);
        this.countdownGroup.setVisible(false);
    }

    private void addRows() {
        String[] stats = new String[]{Localization.get("countdown_propulsion").toUpperCase() + "...", Localization.get("countdown_retro").toUpperCase() + "...", Localization.get("countdown_eecom").toUpperCase() + "...", Localization.get("countdown_guidance").toUpperCase() + "...", Localization.get("countdown_telemetry").toUpperCase() + "...", Localization.get("countdown_toys").toUpperCase() + "...", Localization.get("countdown_snacks").toUpperCase() + "..."};
        float delayBetween = 5.0f / (float)stats.length;
        int row = 0;
        for (String s : stats) {
            Label stat = new Label((CharSequence)s, this.hud.baseScreen.labelStyle);
            stat.setFontScale(0.3f);
            stat.setAlignment(8);
            stat.setColor(this.sysColor);
            this.systemsTable.add(stat).expandX().left().space(3.0f);
            Label statGo = new Label((CharSequence)"GO", this.hud.baseScreen.labelStyle);
            statGo.setFontScale(0.3f);
            statGo.setColor(this.sysColor);
            this.systemsTable.add(statGo).space(3.0f);
            this.systemsTable.row();
            stat.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.delay(0.25f), (Action)Actions.delay(delayBetween * (float)row), (Action)Actions.alpha(1.0f), (Action)Actions.alpha(0.7f, 0.25f)));
            statGo.addAction(Actions.sequence(Actions.alpha(0.0f), Actions.delay(0.25f), Actions.delay(delayBetween * (float)row), Actions.delay(0.5f), Actions.alpha(1.0f), Actions.alpha(0.7f, 0.25f)));
            ++row;
        }
    }

    public void show(LaunchPad launchPad) {
        this.ambient_sfx(true);
        this.launchPad = launchPad;
        this.visible = true;
        this.hud.gameScreen.cameraLag.panTo(this.launchPad.getXCenter(), this.launchPad.getYCenter() + 40.0f, 2.0f, Interpolation.sine);
        this.countdownGroup.setVisible(false);
        this.group.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.moveTo(this.normalPosition.x, this.normalPosition.y - 10.0f), (Action)Actions.scaleTo(0.96f, 0.96f), (Action)Actions.visible(true), (Action)Actions.parallel((Action)Actions.fadeIn(0.25f), (Action)Actions.sequence((Action)Actions.scaleTo(1.03f, 1.03f, 0.18f, Interpolation.sineOut), (Action)Actions.scaleTo(1.0f, 1.0f, 0.07f, Interpolation.sineOut)))));
    }

    private void ambient_sfx(boolean on) {
        if (on) {
            AssetManagerSingleton.getInstance().finishLoading();
            this.ambientRocketSound = Audio.getInstance().getSound("music/rocket_cockpit.ogg");
            this.ambientRocketSoundId = Audio.getInstance().playSoundLoop(this.ambientRocketSound, 0.5f, 1.0f, 0.0f);
        } else {
            Audio.getInstance().stopLoopSound(this.ambientRocketSound, this.ambientRocketSoundId);
        }
    }

    public void show(Lander lander) {
        this.lander = lander;
        this.visible = true;
        this.countdownGroup.setVisible(false);
        this.group.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.moveTo(this.normalPosition.x, this.normalPosition.y - 10.0f), (Action)Actions.scaleTo(0.96f, 0.96f), (Action)Actions.visible(true), (Action)Actions.parallel((Action)Actions.fadeIn(0.25f), (Action)Actions.sequence((Action)Actions.scaleTo(1.03f, 1.03f, 0.18f, Interpolation.sineOut), (Action)Actions.scaleTo(1.0f, 1.0f, 0.07f, Interpolation.sineOut)))));
    }

    public void hide() {
        this.ambient_sfx(false);
        this.visible = false;
        this.hud.gameScreen.cameraLag.setZoom(CameraLag.WALKZOOM, 2.0f, Interpolation.sine);
        this.group.addAction(Actions.sequence((Action)Actions.parallel((Action)Actions.fadeOut(0.2f), (Action)Actions.scaleTo(0.96f, 0.96f, 0.2f)), (Action)Actions.visible(false)));
    }

    public void startCountdown() {
        this.launchPad.openTowerAnim();
        this.launchPad.rocketDrawable.closeDoor();
        this.tickCountdown();
        this.table.setVisible(false);
        this.warningLines.setVisible(false);
        this.countdownGroup.setVisible(true);
        this.countingDown = true;
        this.countdownTimerDelay = 0.0f;
        this.addRows();
    }

    public void resetCountdown() {
    }

    public void update(float delta) {
        if (this.countingDown) {
            this.countdownTimerDelay += delta;
            if (this.countdownTimerDelay > 1.0f) {
                this.countdownTimerDelay = 0.0f;
                if (this.countdown > 0) {
                    this.tickCountdown();
                } else {
                    this.countdownFinished();
                    this.countingDown = false;
                }
            }
        }
    }

    public void tickCountdown() {
        --this.countdown;
        String c = "" + this.countdown;
        if (this.countdown > 0) {
            Audio.getInstance().playSound("sounds/countdown-blip.ogg", 0.3f, 0.65f);
        } else {
            Audio.getInstance().playSound("sounds/countdown-blip.ogg", 0.3f, 0.9f);
        }
        if (this.countdown == 1) {
            this.launchPad.rocketDrawable.startEngine();
        }
        if (this.countdown == 0) {
            c = "GO";
        }
        this.countdownTimer.setText("" + c);
        this.countdownTimerShadow.setText("" + c);
        this.countdownGlow.clearActions();
        this.countdownGlow.addAction(Actions.sequence((Action)Actions.alpha(0.7f), (Action)Actions.fadeOut(0.8f)));
        this.countdownGroup.clearActions();
        this.countdownGroup.addAction(Actions.sequence((Action)Actions.scaleTo(1.4f, 1.4f), (Action)Actions.scaleTo(1.2f, 1.2f, 0.8f)));
        this.countdownTimer.clearActions();
        this.countdownTimer.addAction(Actions.sequence((Action)Actions.color(Color.WHITE), (Action)Actions.color(Menu.MENU_COLOR, 0.5f), (Action)Actions.color(new Color(1.0f, 0.0f, 0.0f, 0.0f), 0.3f)));
        this.countdownTimerShadow.clearActions();
        this.countdownTimerShadow.addAction(Actions.sequence((Action)Actions.color(new Color(0.0f, 0.0f, 0.0f, 0.8f)), (Action)Actions.delay(0.5f), (Action)Actions.fadeOut(0.3f)));
    }

    private void countdownFinished() {
        this.hide();
        this.launchPad.startLaunch();
    }
}

