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
                Gdx.app.log("MewnBase", "Preparing to create server on port 7777 and load save...");

                // Do UI/game work on the render thread
                Gdx.app.postRunnable(() -> {
                    try {
                        String saveFolder = com.cairn4.moonbase.MoonBase.currentSaveFolder;
                        if (saveFolder == null || saveFolder.trim().length() == 0) {
                            saveFolder = "multiplayer_host";
                        }
                        com.badlogic.gdx.files.FileHandle dir = Gdx.files.local("saves/" + saveFolder);
                        if (!dir.exists()) dir.mkdirs();
                        com.cairn4.moonbase.MoonBase.currentSaveFolder = saveFolder;

                        // Ensure assets are loaded
                        baseScreen.game.loadGameAssets();
                        com.cairn4.moonbase.AssetManagerSingleton.getInstance().finishLoading();

                        // Create GameScreen without auto-connecting client (temporarily disable multiplayer flag)
                        boolean prevMult = com.cairn4.moonbase.MoonBase.isMultiplayer;
                        com.cairn4.moonbase.MoonBase.isMultiplayer = false;
                        com.cairn4.moonbase.ui.GameScreen gs = new com.cairn4.moonbase.ui.GameScreen(baseScreen.game, false);
                        baseScreen.game.setScreen(gs);

                        // Start server using ConsoleExecutor which will attach the GameScreen to the server when possible
                        com.cairn4.moonbase.ConsoleExecutor ce = new com.cairn4.moonbase.ConsoleExecutor(gs);
                        ce.createserver(7777);

                        // Keep multiplayer client disabled locally: user requested server-only behavior.
                        // Expose host/port so external clients can connect if desired.
                        try {
                            com.cairn4.moonbase.MoonBase.multiplayerHost = "127.0.0.1";
                            com.cairn4.moonbase.MoonBase.multiplayerPort = 7777;
                        } catch (Exception ignored) {}
                        Gdx.app.log("MewnBase", "Hosted server started on port 7777. Not starting local client.");

                    } catch (Exception e) {
                        Gdx.app.error("MewnBase", "Failed to create server or load save", e);
                    }
                });
            }
        });
        table.row();
        table.add(createServerButton).pad(20.0f);
    }

    @Override
    public void show() {
        super.show();
        this.baseScreen.mainGroup.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        this.scheduleAutoConnect();
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

        // Perform a synchronous initial-data fetch from the server before creating GameScreen.
        // This avoids racing to load a world before the server has sent the save blobs.
        final boolean prevIsMultiplayer = com.cairn4.moonbase.MoonBase.isMultiplayer;
        final String prevHost = com.cairn4.moonbase.MoonBase.multiplayerHost;
        final int prevPort = com.cairn4.moonbase.MoonBase.multiplayerPort;
        final String prevNick = com.cairn4.moonbase.MoonBase.multiplayerNick;

        new Thread(() -> {
            boolean success = false;
            try {
                String saveDir = "saves/multiplayer_received/";
                try { com.badlogic.gdx.Gdx.files.local(saveDir).deleteDirectory(); } catch (Exception ignored) {}
                try { com.badlogic.gdx.Gdx.files.local(saveDir).mkdirs(); } catch (Exception ignored) {}

                java.net.Socket tempSock = new Socket();
                tempSock.connect(new InetSocketAddress(ip, port), 5000);
                try { tempSock.setSoTimeout(15000); } catch (Exception ignored) {}
                java.io.DataInputStream in = new java.io.DataInputStream(tempSock.getInputStream());
                try { int assignedId = in.readInt(); } catch (Exception ignored) {}

                // Read gameSave blob
                try {
                    int gameSaveLen = in.readInt();
                    if (gameSaveLen > 0) {
                        byte[] gameSaveBytes = new byte[gameSaveLen];
                        in.readFully(gameSaveBytes);
                        com.badlogic.gdx.files.FileHandle fh = com.badlogic.gdx.Gdx.files.local(saveDir + "gameSave.json");
                        fh.writeBytes(gameSaveBytes, false);
                        com.badlogic.gdx.Gdx.app.log("ClientSync", "wrote gameSave.json, len=" + gameSaveLen);
                        com.cairn4.moonbase.MoonBase.currentSaveFolder = "multiplayer_received";
                    }
                } catch (Exception e) {
                    com.badlogic.gdx.Gdx.app.error("ClientSync", "Failed reading gameSave blob", e);
                }

                // Read worldData blob
                try {
                    int worldDataLen = in.readInt();
                    if (worldDataLen > 0) {
                        byte[] worldDataBytes = new byte[worldDataLen];
                        in.readFully(worldDataBytes);
                        com.badlogic.gdx.files.FileHandle fh2 = com.badlogic.gdx.Gdx.files.local(saveDir + "worldData.json");
                        fh2.writeBytes(worldDataBytes, false);
                        com.badlogic.gdx.Gdx.app.log("ClientSync", "wrote worldData.json, len=" + worldDataLen);
                        com.cairn4.moonbase.MoonBase.currentSaveFolder = "multiplayer_received";
                    }
                } catch (Exception e) {
                    com.badlogic.gdx.Gdx.app.error("ClientSync", "Failed reading worldData blob", e);
                }

                try { in.close(); } catch (Exception ignored) {}
                try { tempSock.close(); } catch (Exception ignored) {}
                success = true;
            } catch (Exception e) {
                com.badlogic.gdx.Gdx.app.error("ClientSync", "Failed to fetch initial data synchronously", e);
                success = false;
            }

            if (success) {
                com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                    try {
                        com.cairn4.moonbase.MoonBase.isMultiplayer = true;
                        com.cairn4.moonbase.MoonBase.multiplayerHost = ip;
                        com.cairn4.moonbase.MoonBase.multiplayerPort = port;
                        com.cairn4.moonbase.MoonBase.multiplayerNick = nick;
                        baseScreen.game.loadGameAssets();
                        com.cairn4.moonbase.AssetManagerSingleton.getInstance().finishLoading();
                        baseScreen.game.setScreen(new com.cairn4.moonbase.ui.GameScreen(baseScreen.game, false));
                    } catch (Exception e) {
                        com.badlogic.gdx.Gdx.app.error("ClientSync", "Failed to start GameScreen after sync fetch", e);
                    }
                });
            } else {
                com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                    com.cairn4.moonbase.MoonBase.isMultiplayer = prevIsMultiplayer;
                    com.cairn4.moonbase.MoonBase.multiplayerHost = prevHost;
                    com.cairn4.moonbase.MoonBase.multiplayerPort = prevPort;
                    com.cairn4.moonbase.MoonBase.multiplayerNick = prevNick;
                    baseScreen.menuBackSound();
                    com.badlogic.gdx.Gdx.app.log("ClientSync", "Connection failed, returning to menu");
                });
            }
        }, "MewnBase-ClientSync").start();
    }
}
