package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.cairn4.moonbase.MoonBase;
import java.io.OutputStream;
import java.util.Properties;

public class HostServerSaveMenu extends LoadGameMenu {
    private final int port;

    public HostServerSaveMenu(BaseScreen baseScreen, int port) {
        super(baseScreen);
        this.port = port;
    }

    @Override
    protected void loadGame(String saveFolder) {
        Gdx.app.log("MewnBase", "Hosting server on port " + this.port + " with save: " + saveFolder);
        final String saveFolderFinal = (saveFolder == null || saveFolder.trim().length() == 0) ? "multiplayer_host" : saveFolder;
        Gdx.app.postRunnable(() -> {
            try {
                FileHandle dir = Gdx.files.local("saves/" + saveFolderFinal);
                if (!dir.exists()) dir.mkdirs();
                MoonBase.currentSaveFolder = saveFolderFinal;
                this.applyHostOptions(saveFolderFinal);

                // Ensure assets are loaded
                baseScreen.game.loadGameAssets();
                com.cairn4.moonbase.AssetManagerSingleton.getInstance().finishLoading();

                // Create GameScreen without auto-connecting client
                boolean prevMult = com.cairn4.moonbase.MoonBase.isMultiplayer;
                com.cairn4.moonbase.MoonBase.isMultiplayer = false;
                com.cairn4.moonbase.ui.GameScreen gs = new com.cairn4.moonbase.ui.GameScreen(baseScreen.game, false);
                baseScreen.game.setScreen(gs);
                com.cairn4.moonbase.MoonBase.isMultiplayer = prevMult;

                // Start server using ConsoleExecutor which will attach the GameScreen
                com.cairn4.moonbase.ConsoleExecutor ce = new com.cairn4.moonbase.ConsoleExecutor(gs);
                ce.createserver(this.port);

                try {
                    MoonBase.multiplayerHost = "127.0.0.1";
                    MoonBase.multiplayerPort = this.port;
                } catch (Exception ignored) {}
                Gdx.app.log("MewnBase", "Hosted server started on port " + this.port + ". Not starting local client.");
            } catch (Exception e) {
                Gdx.app.error("MewnBase", "Failed to host server for save: " + saveFolderFinal, e);
            }
        });
    }

    private void applyHostOptions(String saveFolderFinal) {
        MoonBase.PendingHostServerOptions opt = MoonBase.pendingHostServerOptions;
        if (opt == null) {
            opt = new MoonBase.PendingHostServerOptions();
            MoonBase.pendingHostServerOptions = opt;
        }

        Properties props = new Properties();
        props.setProperty("syncTech", Boolean.toString(opt.syncTech));
        props.setProperty("pvp", Boolean.toString(opt.pvp));
        props.setProperty("pvpMelee", Boolean.toString(opt.pvpMelee));
        props.setProperty("pvpVehicle", Boolean.toString(opt.pvpVehicle));
        props.setProperty("allowTp", Boolean.toString(opt.allowTp));
        props.setProperty("allowTphere", Boolean.toString(opt.allowTphere));
        props.setProperty("allowTransfer", Boolean.toString(opt.allowTransfer));
        props.setProperty("password", opt.password == null ? "" : opt.password);

        FileHandle propsFile = Gdx.files.local("saves/" + saveFolderFinal + "/server.properties");
        try (OutputStream out = propsFile.write(false)) {
            props.store(out, "MewnBase multiplayer host settings");
        } catch (Exception e) {
            Gdx.app.error("MewnBase", "Failed to write server.properties for " + saveFolderFinal, e);
        }

        if (opt.networkMode == MoonBase.PendingHostServerOptions.NetworkMode.ONLINE) {
            System.setProperty("mewnbase.mp.registry", opt.registryUrl);
            System.clearProperty("mewnbase.mp.serverName");
            System.clearProperty("mewnbase.mp.advertiseHost");
            System.clearProperty("mewnbase.mp.advertisePort");
            Gdx.app.log("MewnBase", "Online host mode enabled: registry=" + opt.registryUrl + " relay session will be created automatically");
        } else {
            System.clearProperty("mewnbase.mp.registry");
            System.clearProperty("mewnbase.mp.advertiseHost");
            System.clearProperty("mewnbase.mp.advertisePort");
            System.clearProperty("mewnbase.mp.serverName");
            Gdx.app.log("MewnBase", "LAN host mode enabled: registry heartbeat disabled");
        }
    }
}
