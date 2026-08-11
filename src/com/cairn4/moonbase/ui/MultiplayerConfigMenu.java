package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
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

public class MultiplayerConfigMenu extends Menu {
    private TextButton lanButton;
    private TextButton onlineButton;
    private TextField ipField;
    private TextField portField;
    private TextField nickField;
    private TextField passwordField;
    private TextButton connectButton;
    private TextButton browseButton;
    private TextButton createServerButton;
    private Label targetLabel;
    private ScrollPane scroller;
    private Table lanSection;
    private Table onlineSection;
    private boolean onlineMode;
    private boolean onlineSelectionReady;
    private String targetName = "No online server selected";
    private String targetHost = "";
    private int targetPort = -1;

    public MultiplayerConfigMenu(BaseScreen baseScreen) {
        super(baseScreen);
        this.setup();
        this.scheduleAutoConnect();
    }

    @Override
    protected void setup() {
        this.menuGroup.setTransform(false);
        this.menuGroup.setSize(this.stage.getWidth(), this.stage.getHeight());
        Table root = new Table();
        root.setFillParent(true);
        root.pad(26.0f, 56.0f, 18.0f, 56.0f);
        this.menuGroup.addActor(root);

        NinePatchDrawable altNpdUp = new NinePatchDrawable(new NinePatch(this.skin.getRegion("btn-minor"), 16, 16, 16, 16));
        NinePatchDrawable altNpdActive = new NinePatchDrawable(new NinePatch(this.skin.getRegion("btn-minor-pressed"), 16, 16, 16, 16));

        BitmapFont uiFont = this.createScaledFont(0.45f);
        BitmapFont inputFont = this.createScaledFont(0.50f);

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = inputFont;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.background = altNpdUp;
        textFieldStyle.focusedBackground = altNpdActive;
        textFieldStyle.cursor = this.skin.getDrawable("hud-progress-knob");
        textFieldStyle.selection = new NinePatchDrawable(new NinePatch(this.skin.getRegion("slider-bg"), 5, 5, 5, 5));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = uiFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = altNpdUp;
        buttonStyle.down = altNpdActive;
        buttonStyle.checked = altNpdActive;

        Label.LabelStyle labelStyle = new Label.LabelStyle(uiFont, Color.WHITE);

        this.ipField = new TextField("127.0.0.1", textFieldStyle);
        this.portField = new TextField("7777", textFieldStyle);
        this.nickField = new TextField(System.getProperty("user.name", "Player"), textFieldStyle);
        this.passwordField = new TextField("", textFieldStyle);
        this.passwordField.setPasswordMode(true);
        this.passwordField.setPasswordCharacter('*');

        Table content = new Table();
        content.top().left();
        content.defaults().pad(8.0f).fillX();
        content.setWidth(560.0f);

        Label title = new Label("Multiplayer", this.headingStyle);
        title.setFontScale(0.80f);
        content.add(title).colspan(2).center().padBottom(18.0f);
        content.row();

        content.add(new Label("Network mode", labelStyle)).left().padBottom(2.0f);
        content.row();
        Table modeRow = new Table();
        this.lanButton = new TextButton("LAN", buttonStyle);
        this.onlineButton = new TextButton("Online", buttonStyle);
        this.lanButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MultiplayerConfigMenu.this.baseScreen.menuForwardSound();
                MultiplayerConfigMenu.this.setOnlineMode(false);
            }
        });
        this.onlineButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MultiplayerConfigMenu.this.baseScreen.menuForwardSound();
                MultiplayerConfigMenu.this.setOnlineMode(true);
            }
        });
        modeRow.add(this.lanButton).width(170.0f).height(42.0f).padRight(10.0f);
        modeRow.add(this.onlineButton).width(170.0f).height(42.0f);
        content.add(modeRow).left().width(500.0f);
        content.row();

        content.add(new Label("Nickname:", labelStyle)).right();
        content.add(this.nickField).width(260.0f).left();
        content.row();

        content.add(new Label("Password:", labelStyle)).right();
        content.add(this.passwordField).width(260.0f).left();
        content.row();

        this.lanSection = new Table();
        this.lanSection.defaults().pad(8.0f).fillX();
        this.lanSection.add(new Label("LAN / direct connect", labelStyle)).colspan(2).left().padBottom(4.0f);
        this.lanSection.row();
        this.lanSection.add(new Label("LAN IP / Host:", labelStyle)).right();
        this.lanSection.add(this.ipField).width(260.0f).left();
        this.lanSection.row();
        this.lanSection.add(new Label("LAN Port:", labelStyle)).right();
        this.lanSection.add(this.portField).width(260.0f).left();
        this.lanSection.row();
        Label lanNote = new Label("Use this for local network or a direct IP.", labelStyle);
        lanNote.setWrap(true);
        this.lanSection.add(lanNote).colspan(2).width(480.0f).left().padTop(4.0f);
        this.lanSection.row();
        content.add(this.lanSection).colspan(2).left().width(520.0f);
        content.row();

        this.onlineSection = new Table();
        this.onlineSection.defaults().pad(8.0f).fillX();
        this.targetLabel = new Label(this.getOnlineTargetText(), labelStyle);
        this.onlineSection.add(new Label("Online relay", labelStyle)).colspan(2).left().padBottom(4.0f);
        this.onlineSection.row();
        this.onlineSection.add(this.targetLabel).colspan(2).left();
        this.onlineSection.row();
        this.browseButton = new TextButton("Browse online list", buttonStyle);
        this.browseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MultiplayerConfigMenu.this.baseScreen.menuForwardSound();
                String reg = com.cairn4.moonbase.MpServerRegistry.getRegistryBaseUrl();
                if (reg == null || reg.length() == 0) {
                    Gdx.app.log("MewnBase", "Online list registry not configured");
                }
                MultiplayerConfigMenu.this.baseScreen.showMenu(new MultiplayerServerListMenu(MultiplayerConfigMenu.this.baseScreen, MultiplayerConfigMenu.this));
            }
        });
        this.createServerButton = new TextButton("Create Server (7777)", buttonStyle);
        this.createServerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MultiplayerConfigMenu.this.baseScreen.menuForwardSound();
                Gdx.app.log("MewnBase", "Open host setup...");
                MultiplayerConfigMenu.this.baseScreen.showMenu(new HostServerSetupMenu(MultiplayerConfigMenu.this.baseScreen));
            }
        });
        Table onlineActions = new Table();
        onlineActions.defaults().pad(6.0f);
        onlineActions.add(this.browseButton).width(240.0f).height(44.0f);
        this.onlineSection.add(onlineActions).colspan(2).center().padTop(8.0f);
        this.onlineSection.row();
        Label onlineNote = new Label("Choose an online relay session from the list, then connect.", labelStyle);
        onlineNote.setWrap(true);
        this.onlineSection.add(onlineNote).colspan(2).width(480.0f).left().padTop(4.0f);
        this.onlineSection.row();
        content.add(this.onlineSection).colspan(2).left().width(520.0f);
        content.row();

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.vScrollKnob = altNpdActive;
        scrollStyle.vScroll = altNpdUp;
        this.scroller = new ScrollPane(content, scrollStyle);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setFadeScrollBars(false);
        this.scroller.setOverscroll(false, false);
        root.add(this.scroller).expand().fill().padTop(6.0f);
        root.row();

        this.connectButton = new TextButton("Connect LAN", buttonStyle);
        this.connectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MultiplayerConfigMenu.this.baseScreen.menuForwardSound();
                if (MultiplayerConfigMenu.this.onlineMode) {
                    if (!MultiplayerConfigMenu.this.onlineSelectionReady || MultiplayerConfigMenu.this.targetPort <= 0 || MultiplayerConfigMenu.this.targetHost == null || MultiplayerConfigMenu.this.targetHost.trim().isEmpty()) {
                        Gdx.app.log("MewnBase", "No online server selected; opening online list.");
                        MultiplayerConfigMenu.this.baseScreen.showMenu(new MultiplayerServerListMenu(MultiplayerConfigMenu.this.baseScreen, MultiplayerConfigMenu.this));
                        return;
                    }
                    MultiplayerConfigMenu.this.connectWithParams(MultiplayerConfigMenu.this.targetHost, MultiplayerConfigMenu.this.targetPort, MultiplayerConfigMenu.this.nickField.getText(), MultiplayerConfigMenu.this.passwordField.getText());
                    return;
                }

                int port = MultiplayerConfigMenu.this.parsePort(MultiplayerConfigMenu.this.portField.getText());
                if (port <= 0) {
                    Gdx.app.error("MewnBase", "Invalid port: " + MultiplayerConfigMenu.this.portField.getText(), null);
                    MultiplayerConfigMenu.this.baseScreen.menuBackSound();
                    return;
                }
                MultiplayerConfigMenu.this.connectWithParams(MultiplayerConfigMenu.this.ipField.getText(), port, MultiplayerConfigMenu.this.nickField.getText(), MultiplayerConfigMenu.this.passwordField.getText());
            }
        });
        TextButton exitButton = new TextButton("Exit", buttonStyle);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MultiplayerConfigMenu.this.baseScreen.menuBackSound();
                MultiplayerConfigMenu.this.back();
            }
        });

        Table footer = new Table();
        footer.defaults().pad(8.0f);
        footer.add(this.connectButton).width(240.0f).height(48.0f);
        footer.add(exitButton).width(240.0f).height(48.0f);
        footer.add(this.createServerButton).width(240.0f).height(48.0f);
        root.add(footer).padTop(12.0f).padBottom(8.0f);
        root.row();

        this.setOnlineMode(false);
    }

    public void applyServer(String name, String host, int port) {
        if (name != null && name.trim().length() > 0) {
            this.targetName = name.trim();
        } else {
            this.targetName = "Online session";
        }
        if (host != null && host.trim().length() > 0) {
            this.targetHost = host.trim();
        }
        if (port > 0 && port <= 65535) {
            this.targetPort = port;
        }
        this.onlineSelectionReady = true;
        this.setOnlineMode(true);
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

    private void scheduleAutoConnect() {
        String hostProp = System.getProperty("mewnbase.mp.host");
        String portProp = System.getProperty("mewnbase.mp.port");
        String nickProp = System.getProperty("mewnbase.mp.nick");
        String passProp = System.getProperty("mewnbase.mp.password");
        String autoProp = System.getProperty("mewnbase.mp.autoconnect");
        boolean hasAny = (hostProp != null && hostProp.trim().length() > 0)
                || (portProp != null && portProp.trim().length() > 0)
                || (nickProp != null && nickProp.trim().length() > 0)
                || (passProp != null && passProp.trim().length() > 0)
                || (autoProp != null && autoProp.trim().length() > 0);
        if (!hasAny) {
            return;
        }

        Gdx.app.postRunnable(() -> {
            if (hostProp != null && hostProp.trim().length() > 0) {
                MultiplayerConfigMenu.this.ipField.setText(hostProp.trim());
            }
            if (portProp != null && portProp.trim().length() > 0) {
                try {
                    int parsed = Integer.parseInt(portProp.trim());
                    if (parsed > 0 && parsed <= 65535) {
                        MultiplayerConfigMenu.this.portField.setText(Integer.toString(parsed));
                    }
                } catch (Exception e) {
                    Gdx.app.error("MewnBase", "Auto-connect invalid port: " + portProp, e);
                }
            }
            if (nickProp != null && nickProp.trim().length() > 0) {
                MultiplayerConfigMenu.this.nickField.setText(nickProp.trim());
            }
            if (passProp != null) {
                MultiplayerConfigMenu.this.passwordField.setText(passProp);
            }
            MultiplayerConfigMenu.this.setOnlineMode(false);

            boolean autoConnect = false;
            if (autoProp != null) {
                String v = autoProp.trim().toLowerCase();
                autoConnect = v.equals("1") || v.equals("true") || v.equals("yes");
            }
            if (!autoConnect) {
                Gdx.app.log("MewnBase", "Auto-connect properties present but autoconnect disabled.");
                return;
            }

            MultiplayerConfigMenu.this.baseScreen.menuForwardSound();
            int port = MultiplayerConfigMenu.this.parsePort(MultiplayerConfigMenu.this.portField.getText());
            if (port <= 0) {
                Gdx.app.error("MewnBase", "Auto-connect invalid port: " + MultiplayerConfigMenu.this.portField.getText(), null);
                return;
            }
            MultiplayerConfigMenu.this.connectWithParams(MultiplayerConfigMenu.this.ipField.getText(), port, MultiplayerConfigMenu.this.nickField.getText(), MultiplayerConfigMenu.this.passwordField.getText());
        });
    }

    private void setOnlineMode(boolean online) {
        this.onlineMode = online;
        if (this.lanButton != null) {
            this.lanButton.setChecked(!online);
        }
        if (this.onlineButton != null) {
            this.onlineButton.setChecked(online);
        }
        if (this.lanSection != null) {
            this.lanSection.setVisible(!online);
        }
        if (this.onlineSection != null) {
            this.onlineSection.setVisible(online);
        }
        if (this.browseButton != null) {
            this.browseButton.setVisible(online);
        }
        if (this.createServerButton != null) {
            this.createServerButton.setVisible(true);
        }
        if (this.targetLabel != null) {
            this.targetLabel.setText(this.getOnlineTargetText());
            this.targetLabel.setVisible(online);
        }
        if (this.connectButton != null) {
            if (online) {
                this.connectButton.setText(this.onlineSelectionReady ? "Connect Online" : "Browse Servers");
            } else {
                this.connectButton.setText("Connect LAN");
            }
        }
    }

    private String getOnlineTargetText() {
        if (!this.onlineSelectionReady || this.targetPort <= 0) {
            return "Online target: no server selected";
        }
        return "Online target: " + this.targetName + " (" + this.targetPort + ")";
    }

    private int parsePort(String text) {
        try {
            int port = Integer.parseInt(text == null ? "" : text.trim());
            if (port > 0 && port <= 65535) {
                return port;
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private void connectWithParams(final String ip, final int port, final String nick, final String password) {
        Gdx.app.log("MewnBase", "Preparing to connect to " + ip + ":" + port);

        com.cairn4.moonbase.MoonBase.isMultiplayer = true;
        com.cairn4.moonbase.MoonBase.multiplayerHost = ip;
        com.cairn4.moonbase.MoonBase.multiplayerPort = port;
        com.cairn4.moonbase.MoonBase.multiplayerNick = nick;
        com.cairn4.moonbase.MoonBase.multiplayerPassword = password == null ? "" : password;
        try { System.setProperty("mewnbase.mp.password", com.cairn4.moonbase.MoonBase.multiplayerPassword); } catch (Exception ignored) {}
        com.cairn4.moonbase.MoonBase.currentSaveFolder = "multiplayer_received";
        baseScreen.game.setScreen(new com.cairn4.moonbase.ui.LoadingScreen(baseScreen.game, false));
    }
}
