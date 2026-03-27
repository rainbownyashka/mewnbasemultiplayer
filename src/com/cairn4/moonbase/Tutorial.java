/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.RecipieRequirement;
import com.cairn4.moonbase.SettingsData;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.TutorialAirlockObserver;
import com.cairn4.moonbase.TutorialPlayerObserver;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.AirCleaner;
import com.cairn4.moonbase.tiles.Airlock;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.CraftingStation;
import com.cairn4.moonbase.tiles.Generator;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.SolarPanel;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.TutorialUI;
import com.cairn4.moonbase.worlddata.ItemDropperFactory;
import java.util.ArrayList;

public class Tutorial {
    TutorialUI tutorialUI;
    Player player;
    TutorialPlayerObserver playerObserver;
    TutorialAirlockObserver airlockObserver;
    public STAGES stage = STAGES.start;
    static ArrayList<BaseModule> lockedBaseModules = new ArrayList();
    String hintText;
    int foodQuantityBeforeCrafting;
    int foodQuantityBeforeEating;

    public Tutorial(Player player) {
        this.player = player;
    }

    public STAGES getStage() {
        return this.stage;
    }

    public static void addTutorialBase(World world) {
        Chunk startingChunk = world.getChunk(50, 50);
        ItemDropperFactory.getInstance().newDropper(world, startingChunk, 4, 6, "plant");
        ItemDropperFactory.getInstance().newDropper(world, startingChunk, 4, 7, "plant");
        ItemDropperFactory.getInstance().newDropper(world, startingChunk, 3, 7, "plant");
        CraftingStation cs = new CraftingStation(world, startingChunk, 2, 6);
        Airlock airlock = new Airlock(world, startingChunk, 4, 5, BaseModule.ORIENTATIONS.west);
        HabitatModule hab1 = new HabitatModule(world, startingChunk, 5, 5);
        HabitatModule hab2 = new HabitatModule(world, startingChunk, 5, 4);
        AirCleaner airCleaner = new AirCleaner(world, startingChunk, 5, 6);
        SolarPanel solar = new SolarPanel(world, startingChunk, 6, 5);
        Generator gen = new Generator(world, startingChunk, 6, 6);
        Tutorial.addLockableBaseModule(cs);
        Tutorial.addLockableBaseModule(airlock);
        Tutorial.addLockableBaseModule(hab1);
        Tutorial.addLockableBaseModule(hab2);
        Tutorial.addLockableBaseModule(airCleaner);
        Tutorial.addLockableBaseModule(solar);
        Tutorial.addLockableBaseModule(gen);
    }

    public static void addLockableBaseModule(BaseModule base) {
        base.disablePickup(true);
        lockedBaseModules.add(base);
    }

    public void enableBasePickup() {
        for (BaseModule base : lockedBaseModules) {
            base.disablePickup(false);
        }
        lockedBaseModules.clear();
    }

    public void registerUI(TutorialUI ui) {
        this.tutorialUI = ui;
        GameScreen gameScreen = this.player.world.gameScreen;
        if (gameScreen.game.getCurrentMission().tutorialStage != "start") {
            GameScreen gameScreen2 = this.player.world.gameScreen;
            this.skipTo(STAGES.valueOf(gameScreen2.game.getCurrentMission().tutorialStage));
            this.advance();
        } else {
            this.advance();
        }
        this.playerObserver = new TutorialPlayerObserver(this, this.player);
    }

    public boolean registerAirlockObserver() {
        Tile tile = this.player.world.getChunk(50, 50).getTile(4, 5);
        if (tile != null && tile instanceof Airlock) {
            Airlock airlock = (Airlock)tile;
            MoonBase.log("Tutorial: Found airlock where it should be.");
            this.airlockObserver = new TutorialAirlockObserver(this, airlock);
            return true;
        }
        Airlock airlock = this.findAirlockInChunk(50, 50);
        if (airlock == null) {
            MoonBase.log("Tutorial: Couldn't find airlock!");
            return false;
        }
        MoonBase.log("Tutorial: Found airlock but it had moved");
        this.airlockObserver = new TutorialAirlockObserver(this, airlock);
        return true;
    }

    private Airlock findAirlockInChunk(int chunkX, int chunkY) {
        Chunk c = this.player.world.getChunk(chunkX, chunkY);
        if (c != null) {
            for (Tile t : c.tiles.values()) {
                if (!(t instanceof Airlock)) continue;
                return (Airlock)t;
            }
            return null;
        }
        MoonBase.error("Tutorial: can't find chunk for searching for airlock");
        return null;
    }

    private void rebuildAirlock() {
        new Airlock(this.player.world, this.player.world.getChunk(50, 50), 4, 5, BaseModule.ORIENTATIONS.west);
        int index = this.player.getPlayerInventory().getItemIndex("airlock-builder");
        if (index != -1) {
            MoonBase.log("Tutorial: removing airlock from player inventory");
            this.player.getPlayerInventory().removeItem(index);
        }
    }

    public void changeStage(STAGES newStage) {
        this.stage = newStage;
        boolean buttonVisible = false;
        switch (this.stage) {
            case start: {
                break;
            }
            case movement: {
                SettingsData sd = SettingsLoader.getInstance().settingsData;
                String moveControls = "" + sd.KEYS_UP + sd.KEYS_LEFT + sd.KEYS_DOWN + sd.KEYS_RIGHT;
                Object[] objectArray = new Object[2];
                GameScreen gameScreen = this.player.world.gameScreen;
                objectArray[0] = gameScreen.game.getCurrentMission().getPlanetName();
                objectArray[1] = moveControls;
                this.hintText = Localization.format("tutorial_movement", objectArray);
                buttonVisible = true;
                break;
            }
            case oxygen: {
                this.hintText = Localization.get("tutorial_oxygen");
                buttonVisible = true;
                break;
            }
            case hunger: {
                this.hintText = Localization.get("tutorial_hunger");
                buttonVisible = true;
                break;
            }
            case collectResources: {
                this.hintText = Localization.get("tutorial_collectResources");
                buttonVisible = false;
                break;
            }
            case craftFood: {
                this.hintText = Localization.get("tutorial_craftFood");
                this.foodQuantityBeforeCrafting = this.player.playerInventory.getAmountOfItem("food");
                buttonVisible = false;
                break;
            }
            case eatFood: {
                this.hintText = Localization.get("tutorial_eatFood");
                this.foodQuantityBeforeEating = this.player.playerInventory.getAmountOfItem("food");
                buttonVisible = false;
                int foodIndex = this.player.playerInventory.getItemIndex("food");
                if (foodIndex == -1) break;
                this.tutorialUI.showTutorialArrow(STAGES.droppingItems, foodIndex);
                break;
            }
            case craftShovel: {
                this.tutorialUI.removeTutorialArrow();
                this.giveIngredientsFor("shovel");
                this.hintText = Localization.get("tutorial_craftShovel");
                buttonVisible = false;
                break;
            }
            case goIntoBase: {
                this.hintText = Localization.get("tutorial_goIntoBase");
                buttonVisible = false;
                break;
            }
            case fixAirlock: {
                this.hintText = Localization.get("tutorial_fixAirlock");
                buttonVisible = true;
                break;
            }
            case airCleaners: {
                this.hintText = Localization.get("tutorial_airCleaners");
                buttonVisible = true;
                break;
            }
            case supplyPowerGen: {
                String bioGenName = Item.getName("generator-builder");
                this.hintText = Localization.format("tutorial_supplyPowerGen", bioGenName);
                buttonVisible = true;
                break;
            }
            case pickupBaseModules: {
                this.hintText = Localization.get("tutorial_pickupBaseModules");
                buttonVisible = true;
                this.enableBasePickup();
                break;
            }
            case pickupBaseModules2: {
                this.hintText = Localization.get("tutorial_pickupBaseModules2");
                buttonVisible = false;
                break;
            }
            case placeBaseModules: {
                this.hintText = Localization.get("tutorial_placeBaseModules");
                buttonVisible = false;
                break;
            }
            case basePower: {
                this.hintText = Localization.get("tutorial_basePower");
                buttonVisible = true;
                break;
            }
            case droppingItems: {
                ItemStack newStack = new ItemStack(ItemFactory.getInstance().createItem("scrap"));
                this.player.playerInventory.add(newStack, true);
                String dropKey = SettingsLoader.getInstance().settingsData.KEYS_DROP_ITEM;
                this.hintText = Localization.format("tutorial_dropItem", dropKey);
                int scrapIndex = this.player.playerInventory.getItemIndex("scrap");
                if (scrapIndex != -1) {
                    this.tutorialUI.showTutorialArrow(STAGES.droppingItems, scrapIndex);
                }
                buttonVisible = false;
                break;
            }
            case research1: {
                this.tutorialUI.removeTutorialArrow();
                this.hintText = Localization.get("tutorial_research1");
                buttonVisible = true;
                break;
            }
            case research2: {
                this.tutorialUI.showTutorialArrow(STAGES.research2, -1);
                this.tutorialUI.menu.pulseResearchIcon();
                this.hintText = Localization.get("tutorial_research2");
                buttonVisible = true;
                break;
            }
            case end: {
                this.tutorialUI.removeTutorialArrow();
                this.hintText = Localization.get("tutorial_end");
                buttonVisible = true;
            }
        }
        this.tutorialUI.setText(this.hintText);
        this.tutorialUI.showButton(buttonVisible);
        System.out.println("Tutorial: changeState -> " + newStage.toString());
        this.tutorialUI.show();
    }

    private void giveIngredientsFor(String itemId) {
        ItemData id = ItemFactory.getItemData(itemId);
        for (RecipieRequirement req : id.requires) {
            ItemStack scrapStack = new ItemStack(ItemFactory.getInstance().createItem(req.id), req.quantity);
            this.player.playerInventory.add(scrapStack, true);
        }
    }

    public void advance() {
        switch (this.stage) {
            case start: {
                this.changeStage(STAGES.movement);
                break;
            }
            case movement: {
                this.changeStage(STAGES.oxygen);
                break;
            }
            case oxygen: {
                this.changeStage(STAGES.hunger);
                break;
            }
            case hunger: {
                this.changeStage(STAGES.collectResources);
                break;
            }
            case collectResources: {
                this.changeStage(STAGES.craftFood);
                break;
            }
            case craftFood: {
                this.changeStage(STAGES.eatFood);
                break;
            }
            case eatFood: {
                this.changeStage(STAGES.craftShovel);
                break;
            }
            case craftShovel: {
                this.registerAirlockObserver();
                this.changeStage(STAGES.goIntoBase);
                break;
            }
            case goIntoBase: {
                this.changeStage(STAGES.airCleaners);
                break;
            }
            case fixAirlock: {
                this.rebuildAirlock();
                this.registerAirlockObserver();
                this.changeStage(STAGES.goIntoBase);
                break;
            }
            case airCleaners: {
                this.changeStage(STAGES.supplyPowerGen);
                break;
            }
            case supplyPowerGen: {
                this.changeStage(STAGES.pickupBaseModules);
                break;
            }
            case pickupBaseModules: {
                this.changeStage(STAGES.pickupBaseModules2);
                break;
            }
            case pickupBaseModules2: {
                this.changeStage(STAGES.placeBaseModules);
                break;
            }
            case placeBaseModules: {
                this.changeStage(STAGES.basePower);
                break;
            }
            case basePower: {
                this.changeStage(STAGES.droppingItems);
                break;
            }
            case droppingItems: {
                this.changeStage(STAGES.research1);
                break;
            }
            case research1: {
                this.changeStage(STAGES.research2);
                break;
            }
            case research2: {
                this.changeStage(STAGES.end);
                break;
            }
            case end: {
                this.finishTutorial();
            }
        }
    }

    public void skipTo(STAGES newStage) {
        this.changeStage(newStage);
    }

    private void finishTutorial() {
        this.tutorialUI.hide();
        GameScreen gameScreen = this.player.world.gameScreen;
        gameScreen.game.getCurrentMission().finishedTutorial();
    }

    public static enum STAGES {
        start,
        movement,
        oxygen,
        hunger,
        collectResources,
        craftFood,
        eatFood,
        craftShovel,
        goIntoBase,
        fixAirlock,
        airCleaners,
        supplyPowerGen,
        pickupBaseModules,
        pickupBaseModules2,
        placeBaseModules,
        basePower,
        droppingItems,
        research1,
        research2,
        end;

    }
}

