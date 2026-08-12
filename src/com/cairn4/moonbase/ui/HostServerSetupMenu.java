/*
 * Host-server setup before picking a save.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.MoonBase;

public class HostServerSetupMenu extends Menu {
    private TextButton lanButton;
    private TextButton onlineButton;
    private CheckboxRow syncTechRow;
    private CheckboxRow pvpRow;
    private CheckboxRow pvpMeleeRow;
    private CheckboxRow pvpVehicleRow;
    private CheckboxRow allowTpRow;
    private CheckboxRow allowTphereRow;
    private CheckboxRow allowTransferRow;
    private CheckboxRow disableIceRow;
    private TextField passwordField;

    public HostServerSetupMenu(BaseScreen baseScreen) {
        super(baseScreen);
        this.setup();
    }

    @Override
    protected void setup() {
        this.menuGroup.setTransform(false);
        this.menuGroup.setSize(this.stage.getWidth(), this.stage.getHeight());

        Table root = new Table();
        root.setFillParent(true);
        root.pad(14.0f, 56.0f, 10.0f, 56.0f);
        this.menuGroup.addActor(root);

        NinePatchDrawable altNpdUp = new NinePatchDrawable(new NinePatch(this.skin.getRegion("btn-minor"), 16, 16, 16, 16));
        NinePatchDrawable altNpdActive = new NinePatchDrawable(new NinePatch(this.skin.getRegion("btn-minor-pressed"), 16, 16, 16, 16));
        BitmapFont uiFont = this.createScaledFont(0.45f);
        BitmapFont inputFont = this.createScaledFont(0.50f);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = uiFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = altNpdUp;
        buttonStyle.down = altNpdActive;
        buttonStyle.checked = altNpdActive;

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = inputFont;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.background = altNpdUp;
        textFieldStyle.focusedBackground = altNpdActive;
        textFieldStyle.cursor = this.skin.getDrawable("hud-progress-knob");
        textFieldStyle.selection = new NinePatchDrawable(new NinePatch(this.skin.getRegion("slider-bg"), 5, 5, 5, 5));

        Label.LabelStyle labelStyle = new Label.LabelStyle(uiFont, Color.WHITE);

        Label title = new Label("Host server setup", this.headingStyle);
        title.setFontScale(0.80f);
        root.add(title).top().center().padBottom(14.0f);
        root.row();

        Table content = new Table();
        content.top().left();
        content.defaults().pad(4.0f).left();
        content.pad(16.0f, 30.0f, 12.0f, 30.0f);
        content.setBackground(new NinePatchDrawable(new NinePatch(this.skin.getRegion("textbox-mini"), 20, 20, 20, 20)));
        content.setWidth(940.0f);

        Table modeRow = new Table();
        this.lanButton = new TextButton("LAN", buttonStyle);
        this.onlineButton = new TextButton("Online", buttonStyle);
        this.lanButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HostServerSetupMenu.this.setNetworkMode(MoonBase.PendingHostServerOptions.NetworkMode.LAN);
            }
        });
        this.onlineButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HostServerSetupMenu.this.setNetworkMode(MoonBase.PendingHostServerOptions.NetworkMode.ONLINE);
            }
        });
        modeRow.add(this.lanButton).width(155.0f).height(38.0f).padRight(8.0f);
        modeRow.add(this.onlineButton).width(155.0f).height(38.0f);
        content.add(modeRow).colspan(2).center().padBottom(8.0f);
        content.row();

        content.add(new Label("Server options", labelStyle)).left().padTop(8.0f);
        content.row();

        this.syncTechRow = new CheckboxRow(this, "Sync tech tree", getOption().syncTech);
        this.pvpRow = new CheckboxRow(this, "Enable PvP", getOption().pvp);
        this.pvpMeleeRow = new CheckboxRow(this, "PvP melee", getOption().pvpMelee);
        this.pvpVehicleRow = new CheckboxRow(this, "PvP vehicle damage", getOption().pvpVehicle);
        this.allowTpRow = new CheckboxRow(this, "Allow tp", getOption().allowTp);
        this.allowTphereRow = new CheckboxRow(this, "Allow tphere", getOption().allowTphere);
        this.allowTransferRow = new CheckboxRow(this, "Allow item transfer", getOption().allowTransfer);
        this.disableIceRow = new CheckboxRow(this, "Disable ice biome (mod add-on)", getOption().disableIce);

        this.pvpRow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HostServerSetupMenu.this.setPvpEnabled(!HostServerSetupMenu.this.pvpRow.checked);
            }
        });
        this.pvpMeleeRow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HostServerSetupMenu.this.pvpMeleeRow.setChecked(!HostServerSetupMenu.this.pvpMeleeRow.checked);
                HostServerSetupMenu.this.applyPvpVisibility();
            }
        });
        this.pvpVehicleRow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HostServerSetupMenu.this.pvpVehicleRow.setChecked(!HostServerSetupMenu.this.pvpVehicleRow.checked);
                HostServerSetupMenu.this.applyPvpVisibility();
            }
        });
        this.syncTechRow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HostServerSetupMenu.this.syncTechRow.setChecked(!HostServerSetupMenu.this.syncTechRow.checked);
            }
        });
        this.allowTpRow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HostServerSetupMenu.this.allowTpRow.setChecked(!HostServerSetupMenu.this.allowTpRow.checked);
            }
        });
        this.allowTphereRow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HostServerSetupMenu.this.allowTphereRow.setChecked(!HostServerSetupMenu.this.allowTphereRow.checked);
            }
        });
        this.allowTransferRow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HostServerSetupMenu.this.allowTransferRow.setChecked(!HostServerSetupMenu.this.allowTransferRow.checked);
            }
        });
        this.disableIceRow.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HostServerSetupMenu.this.disableIceRow.setChecked(!HostServerSetupMenu.this.disableIceRow.checked);
            }
        });

        this.syncTechRow.label.setFontScale(0.38f);
        this.pvpRow.label.setFontScale(0.38f);
        this.pvpMeleeRow.label.setFontScale(0.38f);
        this.pvpVehicleRow.label.setFontScale(0.38f);
        this.allowTpRow.label.setFontScale(0.38f);
        this.allowTphereRow.label.setFontScale(0.38f);
        this.allowTransferRow.label.setFontScale(0.38f);
        this.disableIceRow.label.setFontScale(0.38f);

        content.add(this.syncTechRow).left().width(520.0f);
        content.row();
        content.add(this.pvpRow).left().width(520.0f);
        content.row();
        content.add(this.pvpMeleeRow).left().width(520.0f);
        content.row();
        content.add(this.pvpVehicleRow).left().width(520.0f);
        content.row();
        content.add(this.allowTpRow).left().width(520.0f);
        content.row();
        content.add(this.allowTphereRow).left().width(520.0f);
        content.row();
        content.add(this.allowTransferRow).left().width(520.0f);
        content.row();
        content.add(this.disableIceRow).left().width(720.0f);
        content.row();

        content.add(new Label("Password (optional)", labelStyle)).left().padTop(8.0f);
        content.row();
        this.passwordField = new TextField(getOption().password != null ? getOption().password : "", textFieldStyle);
        this.passwordField.setPasswordMode(true);
        this.passwordField.setPasswordCharacter('*');
        content.add(this.passwordField).left().width(360.0f);
        content.row();

        Label note = new Label("LAN uses a direct local/network connection. Online publishes the save for public listing.", labelStyle);
        note.setWrap(true);
        content.add(note).width(860.0f).padTop(8.0f);
        content.row();

        TextButton chooseSave = new TextButton("Continue", buttonStyle);
        chooseSave.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HostServerSetupMenu.this.applyToPendingOptions();
                HostServerSetupMenu.this.baseScreen.menuForwardSound();
                HostServerSetupMenu.this.baseScreen.showMenu(new HostServerSaveMenu(HostServerSetupMenu.this.baseScreen, 7777));
            }
        });
        TextButton back = new TextButton("Back", buttonStyle);
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HostServerSetupMenu.this.baseScreen.menuBackSound();
                HostServerSetupMenu.this.back();
            }
        });

        Table footer = new Table();
        footer.add(chooseSave).width(220.0f).height(44.0f).padRight(10.0f);
        footer.add(back).width(220.0f).height(44.0f);

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.vScrollKnob = altNpdActive;
        scrollStyle.vScroll = altNpdUp;
        ScrollPane scroll = new ScrollPane(content, scrollStyle);
        scroll.setScrollingDisabled(true, false);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        root.add(scroll).width(1000.0f).expandY().fillY().center().padTop(6.0f);
        root.row();
        root.add(footer).bottom().center().padTop(16.0f);
        this.syncUiFromOptions();
    }

    private MoonBase.PendingHostServerOptions getOption() {
        if (MoonBase.pendingHostServerOptions == null) {
            MoonBase.pendingHostServerOptions = new MoonBase.PendingHostServerOptions();
        }
        return MoonBase.pendingHostServerOptions;
    }

    private void syncUiFromOptions() {
        MoonBase.PendingHostServerOptions opt = this.getOption();
        this.setNetworkMode(opt.networkMode);
        this.syncTechRow.setChecked(opt.syncTech);
        this.setPvpEnabled(opt.pvp);
        this.pvpMeleeRow.setChecked(opt.pvpMelee);
        this.pvpVehicleRow.setChecked(opt.pvpVehicle);
        this.allowTpRow.setChecked(opt.allowTp);
        this.allowTphereRow.setChecked(opt.allowTphere);
        this.allowTransferRow.setChecked(opt.allowTransfer);
        this.disableIceRow.setChecked(opt.disableIce);
        if (this.passwordField != null) {
            this.passwordField.setText(opt.password != null ? opt.password : "");
        }
        this.applyPvpVisibility();
    }

    private void setNetworkMode(MoonBase.PendingHostServerOptions.NetworkMode mode) {
        MoonBase.PendingHostServerOptions opt = this.getOption();
        opt.networkMode = mode;
        this.lanButton.setChecked(mode == MoonBase.PendingHostServerOptions.NetworkMode.LAN);
        this.onlineButton.setChecked(mode == MoonBase.PendingHostServerOptions.NetworkMode.ONLINE);
    }

    private void setPvpEnabled(boolean enabled) {
        this.pvpRow.setChecked(enabled);
        this.applyPvpVisibility();
    }

    private void applyPvpVisibility() {
        boolean enabled = this.pvpRow.checked;
        this.pvpMeleeRow.setVisible(enabled);
        this.pvpVehicleRow.setVisible(enabled);
        if (!enabled) {
            this.pvpMeleeRow.setChecked(false);
            this.pvpVehicleRow.setChecked(false);
        }
    }

    private void applyToPendingOptions() {
        MoonBase.PendingHostServerOptions opt = this.getOption();
        opt.syncTech = this.syncTechRow.checked;
        opt.pvp = this.pvpRow.checked;
        opt.pvpMelee = this.pvpMeleeRow.checked;
        opt.pvpVehicle = this.pvpVehicleRow.checked;
        opt.allowTp = this.allowTpRow.checked;
        opt.allowTphere = this.allowTphereRow.checked;
        opt.allowTransfer = this.allowTransferRow.checked;
        opt.disableIce = this.disableIceRow.checked;
        opt.password = this.passwordField != null ? this.passwordField.getText() : "";
    }

    @Override
    public void show() {
        super.show();
        this.baseScreen.mainGroup.setTouchable(Touchable.disabled);
    }

    @Override
    public void hide() {
        super.hide();
        this.baseScreen.mainGroup.setTouchable(Touchable.enabled);
    }
}
