/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.utils.ShaderLoader;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ConsoleExecutor;
import com.cairn4.moonbase.HeatDistortEffect;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.NpcBonuses;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.tiles.ItemDropper;
import com.cairn4.moonbase.tiles.SolarPanel;
import com.cairn4.moonbase.tiles.StorageColorOptions;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.GameScreen;

public class ConsoleTestExecutor
extends ConsoleExecutor {
    public ConsoleTestExecutor(GameScreen gameScreen) {
        super(gameScreen);
    }

    public void setImagePos(String name, float x, float y, String align) {
        int actorAlignment = 12;
        switch (align) {
            case "center": {
                actorAlignment = 1;
                break;
            }
            case "bottomLeft": {
                actorAlignment = 12;
                break;
            }
            case "bottom": {
                actorAlignment = 4;
                break;
            }
            case "bottomRight": {
                actorAlignment = 20;
                break;
            }
            case "right": {
                actorAlignment = 16;
                break;
            }
            case "topRight": {
                actorAlignment = 18;
                break;
            }
            case "top": {
                actorAlignment = 2;
                break;
            }
            case "topLeft": {
                actorAlignment = 10;
                break;
            }
            case "left": {
                actorAlignment = 8;
            }
        }
        Object a = this.gameScreen.hudStage.getRoot().findActor(name);
        ((Actor)a).setPosition(x, y, actorAlignment);
    }

    public void uiEdit(String name) {
        this.uiEdit(name, "center");
    }

    public void uiEdit() {
        if (!this.gameScreen.uiEdit.isOn()) {
            this.gameScreen.uiEdit.startEditMode();
        } else {
            this.gameScreen.uiEdit.endEditMode();
        }
        this.gameScreen.game.console.log("Edit mode: " + this.gameScreen.uiEdit.isOn());
    }

    public void uiEdit(String name, String align) {
        String response = this.gameScreen.uiEdit.editActor(name, this.gameScreen.hudStage);
        this.gameScreen.uiEdit.setAlign(align);
        this.gameScreen.game.console.log(response);
    }

    public void uiEditOff() {
        String response = this.gameScreen.uiEdit.saveEdit();
        this.gameScreen.game.console.log(response);
    }

    public void restart() {
        this.gameScreen.dispose();
        this.gameScreen.game.setScreen(new GameScreen(this.gameScreen.game, true));
    }

    public void disaster() {
        this.world.baseManager.triggerBaseDisaster();
    }

    public void disaster(int worldX, int worldY) {
        Tile t = this.world.getTile(worldX, worldY);
        if (t != null && t instanceof BaseModule) {
            ((BaseModule)t).triggerDisaster();
            if (t instanceof SolarPanel) {
                SolarPanel s = (SolarPanel)t;
                s.dustTriggerBehavior.dustAmount = s.dustTriggerBehavior.limit + 1.0f;
            }
        }
    }

    public void disasterTest() {
        this.disaster(503, 507);
        this.disaster(504, 507);
        this.disaster(506, 507);
        this.disaster(507, 507);
        this.disaster(503, 508);
        this.disaster(504, 508);
        this.disaster(506, 508);
        this.disaster(507, 508);
    }

    public void gc() {
        System.gc();
    }

    public void testMessages() {
        this.gameScreen.hud.hudNotifications.newMessage("plant", "You have collected 5 Plants", Color.WHITE);
        this.gameScreen.hud.hudNotifications.newMessage("Warning: Fire in habitat module!", Color.RED);
        this.gameScreen.hud.hudNotifications.newMessage("Alert: Incoming Sand Storm", Color.WHITE);
        this.gameScreen.hud.hudNotifications.newMessage("scrap", "Picked up 2 Scrap", Color.WHITE);
    }

    public void testSuffocate(boolean on) {
        this.gameScreen.world.player.playerAnimController.suffocateAnim(on);
    }

    public void setStarving() {
        this.gameScreen.world.player.playerStatus.setHunger(0.0f);
    }

    public void pulseResearchIcon() {
        this.gameScreen.hud.pulseResearchIcon();
    }

    public void checkComms() {
        this.world.player.playerStatus.getCommsTowersInRange();
    }

    public void updateBases() {
        this.world.baseManager.updateBases(this.world);
    }

    public void updateBaseWalls() {
        this.world.updateAllWalls();
    }

    public void clearInventory() {
        this.world.player.playerInventory.itemList.clear();
        this.world.player.inventoryUpdate();
    }

    public void listNpcBonuses() {
        this.gameScreen.game.console.log("NPC Bonuses: ");
        for (NpcBonuses.bonusTypes bonus : NpcBonuses.getInstance().getAllBonuses()) {
            this.gameScreen.game.console.log(" - " + (Object)((Object)bonus));
        }
    }

    public void clearCreatures() {
        for (Entity e : this.world.entityList) {
            e.readyToRemove = true;
        }
    }

    public void clearEntities() {
        int count = this.world.entityList.size();
        for (Entity e : this.world.entityList) {
            e.readyToRemove = true;
        }
        this.gameScreen.game.console.log("Removed " + count + " entities from the world.");
    }

    public void debugWorldStage(boolean act, boolean draw) {
        this.gameScreen.debugToggleWorldStageOptions(act, draw);
    }

    public void debugWorldPhysics(boolean on) {
        this.gameScreen.togglePhysics(on);
    }

    public void debugWorldUpdate(boolean updateWorld) {
        this.gameScreen.debugToggleWorldUpdate(updateWorld);
    }

    public void clearAllGroundTiles() {
        for (Chunk c : this.gameScreen.world.worldChunks.values()) {
            for (GroundTile g : c.groundTiles.values()) {
                g.group.remove();
            }
        }
    }

    public void removeAllItemDroppers() {
        for (Chunk c : this.gameScreen.world.worldChunks.values()) {
            for (Tile t : c.tiles.values()) {
                if (!(t instanceof ItemDropper)) continue;
                t.readyToRemove = true;
            }
        }
    }

    public void editColorIndex(int i) {
        StorageColorOptions.editColorIndex = i;
    }

    public void shaderReload() {
        try {
            ShaderLoader.BasePath = "shaders/bitfire_shaders/";
            this.world.gameScreen.postProcessor = new PostProcessor(false, false, true);
            this.world.gameScreen.postProcessor.setEnabled(true);
            this.world.gameScreen.heatFx = new HeatDistortEffect();
            Texture heatTex = new Texture(Gdx.files.internal("heat.png"));
            heatTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            this.world.gameScreen.heatFx.setDistortTex(heatTex);
            this.world.gameScreen.postProcessor.addEffect(this.world.gameScreen.heatFx);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetSteamStats() {
        this.world.gameScreen.game.platformAdapter.resetStats();
    }

    @Override
    public void resetSteamAchievementsStats(String pass) {
        if (pass.equals("please")) {
            this.world.gameScreen.game.platformAdapter.resetAchievementsAndStats();
            this.world.gameScreen.game.console.log("Resetting stats and achievements.");
        } else {
            this.world.gameScreen.game.console.log("Wrong password");
        }
    }

    public void clearAchievement(String id) {
        this.world.gameScreen.game.platformAdapter.clearAchievement(id);
    }

    public void hidePlayer() {
        Player p = this.world.getPlayer();
        if (p != null) {
            p.group.setVisible(false);
        }
    }

    public void showPlayer() {
        Player p = this.world.getPlayer();
        if (p != null) {
            p.group.setVisible(true);
        }
    }
}

