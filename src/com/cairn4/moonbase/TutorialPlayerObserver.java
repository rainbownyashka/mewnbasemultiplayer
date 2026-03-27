/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.Tutorial;
import java.util.Observable;
import java.util.Observer;

public class TutorialPlayerObserver
implements Observer {
    Tutorial tutorial;
    Player p;

    public TutorialPlayerObserver(Tutorial t, Player p) {
        this.tutorial = t;
        this.p = p;
        this.p.addObserver(this);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o == "playerMoved") {
            // empty if block
        }
        if (o == "droppedItem" && this.tutorial.stage == Tutorial.STAGES.droppingItems && this.p.playerInventory.getAmountOfItem("scrap") == 0) {
            this.tutorial.advance();
        }
        if (o == "updateInventory") {
            switch (this.tutorial.stage) {
                case movement: {
                    if (!this.p.playerInventory.hasResources("plant", 1)) break;
                    this.tutorial.advance();
                    break;
                }
                case oxygen: {
                    if (!this.p.playerInventory.hasResources("plant", 1)) break;
                    this.tutorial.advance();
                    break;
                }
                case hunger: {
                    if (!this.p.playerInventory.hasResources("plant", 1)) break;
                    this.tutorial.advance();
                    break;
                }
                case collectResources: {
                    if (!this.p.playerInventory.hasResources("plant", 3)) break;
                    this.tutorial.advance();
                    break;
                }
                case craftFood: {
                    if (this.p.playerInventory.getAmountOfItem("food") <= this.tutorial.foodQuantityBeforeCrafting) break;
                    this.tutorial.advance();
                    break;
                }
                case eatFood: {
                    if (this.p.playerInventory.getAmountOfItem("food") > this.tutorial.foodQuantityBeforeEating) {
                        this.tutorial.foodQuantityBeforeEating = this.p.playerInventory.getAmountOfItem("food");
                    }
                    if (this.p.playerInventory.getAmountOfItem("food") >= this.tutorial.foodQuantityBeforeEating) break;
                    this.tutorial.advance();
                    break;
                }
                case craftShovel: {
                    if (this.p.playerInventory.getAmountOfItem("shovel") <= 0) break;
                    this.tutorial.advance();
                    break;
                }
                case pickupBaseModules2: {
                    boolean pickedUpModule = false;
                    if (this.p.playerInventory.hasResources("habitat-builder", 1)) {
                        pickedUpModule = true;
                    }
                    if (this.p.playerInventory.hasResources("airlock-builder", 1)) {
                        pickedUpModule = true;
                    }
                    if (this.p.playerInventory.hasResources("solar-builder", 1)) {
                        pickedUpModule = true;
                    }
                    if (this.p.playerInventory.hasResources("aircleaner-builder", 1)) {
                        pickedUpModule = true;
                    }
                    if (this.p.playerInventory.hasResources("crafting-station-builder", 1)) {
                        pickedUpModule = true;
                    }
                    if (!pickedUpModule) break;
                    this.tutorial.advance();
                }
            }
        }
        if (o == "placedBuilding" && this.tutorial.stage == Tutorial.STAGES.placeBaseModules) {
            this.tutorial.advance();
        }
    }

    public void remove() {
        this.p.deleteObserver(this);
    }
}

