package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

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
                com.cairn4.moonbase.MoonBase.currentSaveFolder = saveFolderFinal;

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
                    com.cairn4.moonbase.MoonBase.multiplayerHost = "127.0.0.1";
                    com.cairn4.moonbase.MoonBase.multiplayerPort = this.port;
                } catch (Exception ignored) {}
                Gdx.app.log("MewnBase", "Hosted server started on port " + this.port + ". Not starting local client.");
            } catch (Exception e) {
                Gdx.app.error("MewnBase", "Failed to host server for save: " + saveFolderFinal, e);
            }
        });
    }
}
