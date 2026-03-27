/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemActions;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerStatusEffect;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.achievements.PlayerStatusEffectFactory;
import com.cairn4.moonbase.tiles.ProtoBase;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.worlddata.ItemDropperFactory;
import java.util.ArrayList;

public class Item {
    public String id;
    public ArrayList<ItemActions> actions = new ArrayList();
    private String sprite;
    public int maxDurability;
    public int durability;
    private boolean rotatable;

    public String getSprite() {
        return this.sprite;
    }

    public void setSprite(String sprite) {
        this.sprite = sprite;
    }

    public boolean hasActions() {
        return this.actions.size() > 0;
    }

    public String getName() {
        return Localization.get("item_" + this.id);
    }

    public String getDesc() {
        return Localization.get("item_desc_" + this.id);
    }

    public static String getDesc(String itemId) {
        return Localization.get("item_desc_" + itemId);
    }

    public static String getName(String itemId) {
        return Localization.get("item_" + itemId);
    }

    public boolean getRotatable() {
        return this.rotatable;
    }

    public Item() {
    }

    public Item(String id, String sprite, ArrayList<ItemActions> actions, boolean rotatable, int maxDurability, int durability) {
        this.id = id;
        this.sprite = sprite;
        this.actions = actions;
        this.rotatable = rotatable;
        this.maxDurability = maxDurability;
        this.durability = durability;
    }

    @Deprecated
    public void use(Player player) {
        for (ItemActions action : this.actions) {
            Gdx.app.log("MewnBase", "Using item: " + this.getName() + ": action: " + action.type + " - " + action.value);
            boolean result = this.actionHandler(action, player);
            if (!result) continue;
        }
    }

    public void reduceDurability(int d) {
        this.durability -= d;
        Gdx.app.log("MewnBase", "Item durability now: " + this.durability);
    }

    public void resetDurability() {
        this.durability = this.maxDurability;
    }

    public boolean canStackMultiples() {
        return this.getData().canStackMultiples();
    }

    public boolean actionHandler(ItemActions action, Player player) {
        boolean removeAfterUse = false;
        ActionTypes actionType = ActionTypes.test;
        try {
            actionType = ActionTypes.valueOf(action.type);
        }
        catch (Exception exception) {
            // empty catch block
        }
        switch (actionType) {
            case eat: {
                player.playerStatus.changeHunger(Integer.parseInt(action.value));
                if (action.consumeLocMessage != null && !action.consumeLocMessage.equals("")) {
                    String eatString = Localization.format(action.consumeLocMessage, this.getName());
                    player.world.gameScreen.hud.hudNotifications.newMessage(this.getSprite(), eatString);
                }
                player.playerAnimController.eat();
                removeAfterUse = true;
                break;
            }
            case paint: {
                player.playerAnimController.paint();
                break;
            }
            case applyStatusEffect: {
                PlayerStatusEffect se = PlayerStatusEffectFactory.getInstance().createStatusEffect(action.value);
                if (!player.playerStatus.hasStatusEffectType(se.getClass())) {
                    player.playerStatus.newStatusEffect(se);
                } else {
                    player.playerStatus.replaceStatusEffect(se);
                }
                removeAfterUse = true;
                break;
            }
            case drink: {
                player.playerStatus.changeThirst(Integer.parseInt(action.value));
                Audio.getInstance().playSound("sounds/refillWaterSupply.ogg", 0.85f);
                removeAfterUse = true;
                break;
            }
            case scanning: {
                player.scan();
                Gdx.app.log("MewnBase", "Item: Player scanning stuff!");
                removeAfterUse = false;
                break;
            }
            case heal: {
                player.playerStatus.changeHealth(Integer.parseInt(action.value));
                player.world.gameScreen.hud.newDamageFlyoff(Integer.parseInt(action.value), player.getXPos(), player.getYPos(), Color.GREEN);
                removeAfterUse = true;
                break;
            }
            case test: {
                Gdx.app.log("MewnBase", "Item: Player uses equipped item " + this.getName() + "!");
                removeAfterUse = false;
                break;
            }
            case mine: {
                removeAfterUse = false;
                break;
            }
            case gainOxygen: {
                player.playerStatus.changeAir(Float.parseFloat(action.value));
                Audio.getInstance().playSound("sounds/gameOverButton.ogg", 0.3f, 1.4f);
                removeAfterUse = true;
                break;
            }
            case createBuilding: {
                int worldX = player.getInteractPoint2().x;
                int worldY = player.getInteractPoint2().y;
                Gdx.app.debug("MewnBase", "Item: checking world for empty at : " + worldX + ", " + worldY + " for " + action.value);
                if (player.world.isTileEmpty(worldX, worldY)) {
                    float temp = player.world.getTempAtTile(worldX, worldY);
                    if (temp <= 2.0f) {
                        World cfr_ignored_0 = player.world;
                        String chunkKey = World.convertWorldTileToChunkKey(worldX, worldY);
                        Chunk chunk = player.world.worldChunks.get(chunkKey);
                        GridPoint2 local = new GridPoint2(0, 0);
                        World cfr_ignored_1 = player.world;
                        local = World.convertWorldToLocal(local, worldX, worldY);
                        new ProtoBase(player.world, chunk, local.x, local.y, this.id, action.value, action.buildTime, player.playerInventory.placementOrientation);
                        Audio.getInstance().playSound("sounds/placeObject.ogg", 0.5f);
                        // Diagnostic: print environment info so we can see why server/client path is chosen
                        try {
                            try { System.out.println("Item:createBuilding detected at " + worldX + "," + worldY + " for " + action.value); } catch (Exception ignored) {}
                            com.badlogic.gdx.Gdx.app.log("Item", "createBuilding: coords=" + worldX + "," + worldY + " class=" + action.value);
                            boolean hasWorld = (player.world != null);
                            boolean hasGameScreen = (player.world != null && player.world.gameScreen != null);
                            boolean hasClientObj = (player.world != null && player.world.gameScreen != null && player.world.gameScreen.client != null);
                            boolean hasActiveServer = (com.cairn4.moonbase.Server.getActiveServer() != null);
                            try { System.out.println("Item: hasWorld=" + hasWorld + " hasGameScreen=" + hasGameScreen + " hasClientObj=" + hasClientObj + " hasActiveServer=" + hasActiveServer); } catch (Exception ignored) {}
                            com.badlogic.gdx.Gdx.app.log("Item", "createBuilding flags: hasWorld=" + hasWorld + " hasGameScreen=" + hasGameScreen + " hasClientObj=" + hasClientObj + " hasActiveServer=" + hasActiveServer);
                            // If we're in multiplayer, explicitly notify server about the new tile (send TILE_BUILD_START so peers simulate the action-build)
                            {
                                int wx = worldX;
                                int wy = worldY;
                                com.cairn4.moonbase.Chunk c = player.world.worldChunks.get(World.convertWorldTileToChunkKey(wx, wy));
                                if (c == null) c = player.world.getChunk(Chunk.getChunkX(wx), Chunk.getChunkY(wy));
                                // Use the action.value (the requested class name) as the canonical class identifier
                                String className = action.value != null ? action.value : "";
                                String encName = "";
                                String encItemId = "";
                                try { encName = className != null ? java.net.URLEncoder.encode(className, "UTF-8") : ""; } catch (Exception _e) {}
                                try { encItemId = this.id != null ? java.net.URLEncoder.encode(this.id, "UTF-8") : ""; } catch (Exception _e) {}
                                int itemNum = 1;
                                int owner = 0;
                                int buildTime = (int)action.buildTime;
                                String orientationEnc = "";
                                try {
                                    if (player.playerInventory != null && player.playerInventory.placementOrientation != null) {
                                        orientationEnc = java.net.URLEncoder.encode(player.playerInventory.placementOrientation.toString(), "UTF-8");
                                    } else {
                                        orientationEnc = "";
                                    }
                                } catch (Exception _e) { orientationEnc = ""; }
                                // Format: TILE_BUILD_START:wx:wy:encName:encItemId:buildTime:orientation:owner
                                String payload = "TILE_BUILD_START:" + wx + ":" + wy + ":" + encName + ":" + encItemId + ":" + buildTime + ":" + orientationEnc + ":" + owner;
                                try { System.out.println("Item: prepared payload=" + payload); } catch (Exception ignored) {}
                                com.badlogic.gdx.Gdx.app.log("Item", "Prepared TILE_BUILD_START payload: " + payload);
                                try {
                                    if (player.world != null && player.world.gameScreen != null && player.world.gameScreen.client != null) {
                                        // Inform console that we detected a building and will attempt to send via client
                                        System.out.println("Успешно обнаружил постройку, отправка через client: " + payload);
                                        com.badlogic.gdx.Gdx.app.log("Item", "Успешно обнаружил постройку, отправка через client: " + payload);
                                        Class<?> clientClass = player.world.gameScreen.client.getClass();
                                        java.lang.reflect.Method send = clientClass.getMethod("sendMessage", String.class);
                                        send.invoke(player.world.gameScreen.client, payload);
                                        com.badlogic.gdx.Gdx.app.log("Item", "Sent TILE_BUILD_START for created building via client: " + payload);
                                    } else {
                                        // Inform console that we detected a building and will attempt to broadcast via server
                                        System.out.println("Успешно обнаружил постройку, отправка через server: " + payload);
                                        com.badlogic.gdx.Gdx.app.log("Item", "Успешно обнаружил постройку, отправка через server: " + payload);
                                        // No client object (likely running as in-process server) - broadcast via active server
                                        try {
                                            com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                                            try { System.out.println("Item: activeServer instance = " + s); } catch (Exception ignored) {}
                                            if (s != null) {
                                                com.badlogic.gdx.Gdx.app.log("Item", "Calling Server.broadcastFromServer for payload");
                                                try { System.out.println("Item: calling broadcastFromServer..."); } catch (Exception ignored) {}
                                                s.broadcastFromServer(payload);
                                                com.badlogic.gdx.Gdx.app.log("Item", "Sent TILE_BUILD_START for created building via server: " + payload);
                                                try { System.out.println("Item: broadcastFromServer returned successfully"); } catch (Exception ignored) {}
                                            } else {
                                                com.badlogic.gdx.Gdx.app.log("Item", "No client or server to send TILE_BUILD_START: " + payload);
                                            }
                                        } catch (Exception e) {
                                            com.badlogic.gdx.Gdx.app.error("Item", "Failed to broadcast TILE_BUILD_START via server", e);
                                            try { System.out.println("Item: exception while broadcasting via server: " + e); e.printStackTrace(); } catch (Exception ignored) {}
                                        }
                                    }
                                } catch (Exception sendEx) {
                                    // Reflection send failed; attempt to broadcast via active server as a fallback
                                    try {
                                        try { System.out.println("Item: reflection send failed: " + sendEx); sendEx.printStackTrace(); } catch (Exception ignored) {}
                                        com.cairn4.moonbase.Server s = com.cairn4.moonbase.Server.getActiveServer();
                                        try { System.out.println("Item: fallback activeServer = " + s); } catch (Exception ignored) {}
                                        if (s != null) {
                                            try { System.out.println("Item: calling broadcastFromServer in fallback..."); } catch (Exception ignored) {}
                                            s.broadcastFromServer(payload);
                                            com.badlogic.gdx.Gdx.app.log("Item", "Sent TILE_BUILD_START for created building via server (fallback): " + payload);
                                            try { System.out.println("Item: fallback broadcastFromServer returned"); } catch (Exception ignored) {}
                                        } else {
                                            com.badlogic.gdx.Gdx.app.log("Item", "No client or server to send TILE_BUILD_START: " + payload);
                                        }
                                    } catch (Exception e) {
                                        com.badlogic.gdx.Gdx.app.error("Item", "Failed to broadcast TILE_BUILD_START via fallback", e);
                                        try { System.out.println("Item: exception while broadcasting fallback: " + e); e.printStackTrace(); } catch (Exception ignored) {}
                                    }
                                }
                            }
                        } catch (Exception e) {
                            com.badlogic.gdx.Gdx.app.error("Item", "Failed to notify server of created building", e);
                            try { System.out.println("Item: outer exception in createBuilding: " + e); e.printStackTrace(); } catch (Exception ignored) {}
                        }
                        removeAfterUse = true;
                        break;
                    }
                    MoonBase.log("Item: too hot to build here");
                    player.world.gameScreen.hud.hudNotifications.newMessage(Localization.get("tooHotToBuildHere"));
                    removeAfterUse = false;
                    break;
                }
                MoonBase.log("Item: Can't place protoBase here, tile isn't empty");
                player.world.gameScreen.hud.hudNotifications.newMessage(Localization.get("cantBuildHere"));
                removeAfterUse = false;
                break;
            }
            case createFloor: {
                int worldX = player.getInteractPoint2().x;
                int worldY = player.getInteractPoint2().y;
                MoonBase.debug("Item: checking world floor for empty at : " + worldX + ", " + worldY + " for " + action.value);
                if (!player.world.isFloorEmpty(worldX, worldY) || !player.world.isTileEmpty(worldX, worldY)) break;
                float temp = player.world.getTempAtTile(worldX, worldY);
                if (temp <= 2.0f) {
                    World cfr_ignored_2 = player.world;
                    String chunkKey = World.convertWorldTileToChunkKey(worldX, worldY);
                    Chunk chunk = player.world.worldChunks.get(chunkKey);
                    GridPoint2 local = new GridPoint2(0, 0);
                    World cfr_ignored_3 = player.world;
                    local = World.convertWorldToLocal(local, worldX, worldY);
                    new ProtoBase(player.world, chunk, local.x, local.y, this.id, action.value, action.buildTime, player.playerInventory.placementOrientation);
                    Audio.getInstance().playSound("sounds/placeObject.ogg", 0.5f);
                    removeAfterUse = true;
                    break;
                }
                MoonBase.log("Item: too hot to build here");
                player.world.gameScreen.hud.hudNotifications.newMessage(Localization.get("tooHotToBuildHere"));
                removeAfterUse = false;
                break;
            }
            case createItemDropper: {
                int worldX = player.getInteractPoint2().x;
                int worldY = player.getInteractPoint2().y;
                MoonBase.log("Placing an item dropper:" + action.value);
                if (player.world.isTileEmpty(worldX, worldY) && player.world.isFloorEmpty(worldX, worldY)) {
                    float temp = player.world.getTempAtTile(worldX, worldY);
                    if (temp <= 2.0f) {
                        World cfr_ignored_4 = player.world;
                        String chunkKey = World.convertWorldTileToChunkKey(worldX, worldY);
                        Chunk chunk = player.world.worldChunks.get(chunkKey);
                        GridPoint2 local = new GridPoint2(0, 0);
                        World cfr_ignored_5 = player.world;
                        local = World.convertWorldToLocal(local, worldX, worldY);
                        ItemDropperFactory.getInstance().newDropper(player.world, chunk, local.x, local.y, action.value);
                        Audio.getInstance().playSound("sounds/placeObject.ogg", 0.5f);
                        removeAfterUse = true;
                        break;
                    }
                    MoonBase.log("Item: Can't place item dropper here, too hot!");
                    player.world.gameScreen.hud.hudNotifications.newMessage(Localization.get("cantPlaceHere"));
                    removeAfterUse = false;
                    break;
                }
                MoonBase.log("Item: Can't place item dropper here, tile isn't empty");
                player.world.gameScreen.hud.hudNotifications.newMessage(Localization.get("cantBuildHere"));
                removeAfterUse = false;
                break;
            }
            case suitRecharge: {
                float newPower = Float.parseFloat(action.value) * player.playerStatus.getMaxSuitPower();
                player.playerStatus.changeSuitPower(newPower);
                if (action.consumeLocMessage != null && !action.consumeLocMessage.equals("")) {
                    String rechargeString = Localization.format(action.consumeLocMessage, this.getName());
                    player.world.gameScreen.hud.hudNotifications.newMessage(this.getSprite(), rechargeString);
                }
                removeAfterUse = true;
                break;
            }
            default: {
                MoonBase.log("Item: This item (" + this.getName() + " has an unassigned action");
                player.world.gameScreen.hud.hudNotifications.newMessage("Error: Unknown item action");
                removeAfterUse = false;
            }
        }
        return removeAfterUse;
    }

    public ItemData getData() {
        return ItemFactory.getItemData(this.id);
    }

    public static Item copy(Item i1) {
        Item i2 = new Item();
        i2.id = i1.id;
        i2.sprite = i1.sprite;
        i2.actions = i1.actions;
        i2.rotatable = i1.rotatable;
        i2.durability = i1.durability;
        return i2;
    }

    public boolean canUseFromInventoryBar() {
        for (ItemActions ia : this.actions) {
            ActionTypes actionType = ActionTypes.test;
            try {
                actionType = ActionTypes.valueOf(ia.type);
            }
            catch (Exception e) {
                MoonBase.error("invalid item action string " + ia.type + " - Item.canUseFromInventoryBar()");
                return false;
            }
            switch (actionType) {
                case eat: {
                    return true;
                }
                case drink: {
                    return true;
                }
                case scanning: {
                    return false;
                }
                case heal: {
                    return true;
                }
                case mine: {
                    return false;
                }
                case gainOxygen: {
                    return true;
                }
                case createBuilding: {
                    return false;
                }
                case createFloor: {
                    return false;
                }
                case createItemDropper: {
                    return false;
                }
                case suitRecharge: {
                    return true;
                }
            }
        }
        return false;
    }

    public static enum ActionTypes {
        test,
        eat,
        drink,
        scanning,
        heal,
        mine,
        gainOxygen,
        createBuilding,
        createFloor,
        createItemDropper,
        applyStatusEffect,
        repair,
        rechargeVehicle,
        suitRecharge,
        paint;

    }
}

