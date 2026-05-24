package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MultiplayerConfigMenu extends Menu {
    private TextField ipField;
    private TextField portField;
    private TextField nickField;
    private TextButton connectButton;

    public MultiplayerConfigMenu(BaseScreen baseScreen) {
        super(baseScreen);
        this.setup();
        this.scheduleAutoConnect();
    }

    @Override
    protected void setup() {
        this.menuGroup.setTransform(false);
        this.menuGroup.setSize(this.stage.getWidth(), this.stage.getHeight());
        Table table = new Table();
        table.setFillParent(true);
        this.menuGroup.addActor(table);
        NinePatchDrawable alt_npd_up = new NinePatchDrawable(new NinePatch(this.skin.getRegion("btn-minor"), 16, 16, 16, 16));
        NinePatchDrawable alt_npd_active = new NinePatchDrawable(new NinePatch(this.skin.getRegion("btn-minor-pressed"), 16, 16, 16, 16));
        
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = new BitmapFont();
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.background = alt_npd_up;
        textFieldStyle.focusedBackground = alt_npd_active;
        textFieldStyle.cursor = this.skin.getDrawable("hud-progress-knob");
        textFieldStyle.selection = new NinePatchDrawable(new NinePatch(this.skin.getRegion("slider-bg"), 5, 5, 5, 5));

        this.ipField = new TextField("127.0.0.1", textFieldStyle);
        this.portField = new TextField("7777", textFieldStyle);
        this.nickField = new TextField(System.getProperty("user.name", "Player"), textFieldStyle);
        
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = alt_npd_up;
        buttonStyle.down = alt_npd_active;

        this.connectButton = new TextButton("Connect", buttonStyle);
        this.connectButton.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MultiplayerConfigMenu.this.baseScreen.menuForwardSound();
                final String ip = MultiplayerConfigMenu.this.ipField.getText();
                final int port;
                try {
                    port = Integer.parseInt(MultiplayerConfigMenu.this.portField.getText());
                    if (port < 1 || port > 65535) {
                        throw new IllegalArgumentException("port out of range");
                    }
                } catch (Exception e) {
                    Gdx.app.error("MewnBase", "Invalid port: " + MultiplayerConfigMenu.this.portField.getText(), e);
                    MultiplayerConfigMenu.this.baseScreen.menuBackSound();
                    return;
                }
                MultiplayerConfigMenu.this.connectWithParams(ip, port, MultiplayerConfigMenu.this.nickField.getText());
            }
        });
        TextButton exitButton = new TextButton("Exit", buttonStyle);
        exitButton.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MultiplayerConfigMenu.this.baseScreen.menuBackSound();
                MultiplayerConfigMenu.this.back();
            }
        });
        table.add(new Label("Enter IP:", new Label.LabelStyle(new BitmapFont(), Color.WHITE))).pad(10.0f);
        table.add(this.ipField).width(200.0f).pad(10.0f);
        table.row();
        table.add(new Label("Enter Port:", new Label.LabelStyle(new BitmapFont(), Color.WHITE))).pad(10.0f);
        table.add(this.portField).width(200.0f).pad(10.0f);
        table.row();
    table.add(new Label("Nickname:", new Label.LabelStyle(new BitmapFont(), Color.WHITE))).pad(10.0f);
    table.add(this.nickField).width(200.0f).pad(10.0f);
    table.row();
        table.add(this.connectButton).pad(20.0f);
        table.add(exitButton).pad(20.0f);

        // Create Server button: load a save (or create one) and start an in-process server on port 7777
        TextButton createServerButton = new TextButton("Create Server (7777)", buttonStyle);
        createServerButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MultiplayerConfigMenu.this.baseScreen.menuForwardSound();
                Gdx.app.log("MewnBase", "Open save list for hosting...");
                MultiplayerConfigMenu.this.baseScreen.showMenu(new HostServerSaveMenu(MultiplayerConfigMenu.this.baseScreen, 7777));
            }
        });
        table.row();
        table.add(createServerButton).colspan(2).pad(20.0f);
        table.row();
        TextButton browseButton = new TextButton("Browse online list", buttonStyle);
        browseButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MultiplayerConfigMenu.this.baseScreen.menuForwardSound();
                String reg = com.cairn4.moonbase.MpServerRegistry.getRegistryBaseUrl();
                if (reg == null || reg.length() == 0) {
                    Gdx.app.log("MewnBase", "Set -Dmewnbase.mp.registry=http://your-vps:8080");
                }
                MultiplayerConfigMenu.this.baseScreen.showMenu(new MultiplayerServerListMenu(MultiplayerConfigMenu.this.baseScreen, MultiplayerConfigMenu.this));
            }
        });
        table.add(browseButton).colspan(2).pad(10.0f);
    }

    public void applyServer(String host, int port) {
        if (host != null) {
            this.ipField.setText(host.trim());
        }
        this.portField.setText(Integer.toString(port));
    }

    @Override
    public void show() {
        super.show();
        this.baseScreen.mainGroup.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
    }

    @Override
    public void hide() {
        super.hide();
        this.baseScreen.mainGroup.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    private void scheduleAutoConnect() {
        String hostProp = System.getProperty("mewnbase.mp.host");
        String portProp = System.getProperty("mewnbase.mp.port");
        String nickProp = System.getProperty("mewnbase.mp.nick");
        String autoProp = System.getProperty("mewnbase.mp.autoconnect");
        boolean hasAny = (hostProp != null && hostProp.trim().length() > 0)
                || (portProp != null && portProp.trim().length() > 0)
                || (nickProp != null && nickProp.trim().length() > 0)
                || (autoProp != null && autoProp.trim().length() > 0);
        if (!hasAny) {
            return;
        }

        Gdx.app.postRunnable(() -> {
            if (hostProp != null && hostProp.trim().length() > 0) {
                MultiplayerConfigMenu.this.ipField.setText(hostProp.trim());
            }
            if (portProp != null && portProp.trim().length() > 0) {
                MultiplayerConfigMenu.this.portField.setText(portProp.trim());
            }
            if (nickProp != null && nickProp.trim().length() > 0) {
                MultiplayerConfigMenu.this.nickField.setText(nickProp.trim());
            }

            boolean autoConnect = false;
            if (autoProp != null) {
                String v = autoProp.trim().toLowerCase();
                autoConnect = v.equals("1") || v.equals("true") || v.equals("yes");
            }
            if (!autoConnect) {
                Gdx.app.log("MewnBase", "Auto-connect properties present but autoconnect disabled.");
                return;
            }

            final String ip = MultiplayerConfigMenu.this.ipField.getText();
            final int port;
            try {
                port = Integer.parseInt(MultiplayerConfigMenu.this.portField.getText());
                if (port < 1 || port > 65535) {
                    throw new IllegalArgumentException("port out of range");
                }
            } catch (Exception e) {
                Gdx.app.error("MewnBase", "Auto-connect invalid port: " + MultiplayerConfigMenu.this.portField.getText(), e);
                return;
            }
            MultiplayerConfigMenu.this.baseScreen.menuForwardSound();
            MultiplayerConfigMenu.this.connectWithParams(ip, port, MultiplayerConfigMenu.this.nickField.getText());
        });
    }

    private void connectWithParams(final String ip, final int port, final String nick) {
        Gdx.app.log("MewnBase", "Preparing to connect to " + ip + ":" + port);

        com.cairn4.moonbase.MoonBase.isMultiplayer = true;
        com.cairn4.moonbase.MoonBase.multiplayerHost = ip;
        com.cairn4.moonbase.MoonBase.multiplayerPort = port;
        com.cairn4.moonbase.MoonBase.multiplayerNick = nick;
        // Use vanilla loading pipeline; the Client will fetch blobs on connect.
        com.cairn4.moonbase.MoonBase.currentSaveFolder = "multiplayer_received";
        baseScreen.game.setScreen(new com.cairn4.moonbase.ui.LoadingScreen(baseScreen.game, false));
    }
}
