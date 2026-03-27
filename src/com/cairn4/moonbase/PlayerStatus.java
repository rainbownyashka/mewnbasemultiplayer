/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BaseGroup;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.GameOverReasons;
import com.cairn4.moonbase.HeatStatusEffect;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerStatusEffect;
import com.cairn4.moonbase.StarvingStatusEffect;
import com.cairn4.moonbase.SuffocationStatusEffect;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Tank;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.entities.VehicleCabin;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.CommsTower;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.worlddata.SuitLevel;
import com.cairn4.moonbase.worlddata.SuitUpgradeData;
import java.util.ArrayList;

public class PlayerStatus {
    Player player;
    public static SuitUpgradeData suitUpgradeData;
    float air;
    float maxAir = 200.0f;
    boolean suitHasPower;
    float suitPower;
    float maxSuitPower = 60.0f;
    float hunger;
    float maxHunger = 240.0f;
    float foodConsumptionRate = 0.5f;
    float thirst;
    float maxThirst = 100.0f;
    float health;
    float maxHealth = 100.0f;
    public HealthState healthState;
    private float oldHealth;
    String healthChange = "";
    public int healthChanging = 0;
    private StringBuilder statusStringBuilder;
    private boolean flashlight;
    private String playerStatusText;
    private Color playerStatusColor = new Color(Menu.MENU_COLOR);
    private HabitatModule currentHabitatModule;
    public HungerStates hungerState;
    public ArrayList<BaseGroup> baseGroupsInRange = new ArrayList();
    public ArrayList<BaseGroup> baseGroupsInVisibleRange = new ArrayList();
    public ArrayList<PlayerStatusEffect> statusEffects = new ArrayList();
    private float tempUpdateTimer = 0.0f;
    private float tempUpdateDelay = 0.1f;
    private float temperature = 0.0f;
    Vector2 worldPos;

    public boolean getFlashlight() {
        return this.flashlight;
    }

    public float getAir() {
        return this.air;
    }

    public float getMaxAir() {
        return this.maxAir;
    }

    public float getAirPercent() {
        return this.air / this.maxAir;
    }

    public float getSuitPower() {
        return this.suitPower;
    }

    public float getMaxSuitPower() {
        return this.maxSuitPower;
    }

    public float getHunger() {
        return this.hunger;
    }

    public float getMaxHunger() {
        return this.maxHunger;
    }

    public HungerStates getHungerState() {
        return this.hungerState;
    }

    public float getHungerPercent() {
        return this.hunger / this.maxHunger;
    }

    public float getHealth() {
        return this.health;
    }

    public float getMaxHealth() {
        return this.maxHealth;
    }

    public float getHealthPercent() {
        return this.health / this.maxHealth;
    }

    public float getTemperature() {
        return this.temperature;
    }

    public void replaceStatusEffect(PlayerStatusEffect se) {
        for (PlayerStatusEffect pse : this.statusEffects) {
            if (!pse.getClass().equals(se.getClass())) continue;
            pse.readyToRemove = true;
        }
        this.newStatusEffect(se);
    }

    public PlayerStatus(Player p) {
        this.player = p;
        this.load();
        this.suitHasPower = true;
        this.setSuitLevel(0, true);
        this.setFlashlight(false);
        this.statusStringBuilder = new StringBuilder();
    }

    private void load() {
        FileHandle file = Gdx.files.local(MoonBase.coreFolder + "suit_upgrades.json");
        if (!file.exists()) {
            Gdx.app.error("MewnBase", "PlayerStatus: suit_upgrades.json does not exist");
        }
        Gdx.app.debug("MewnBase", "PlayerStatus: Reading suit upgrade data");
        Json json = new Json();
        String fileText = file.readString();
        try {
            suitUpgradeData = json.fromJson(SuitUpgradeData.class, fileText);
        }
        catch (SerializationException s) {
            Gdx.app.error("MewnBase", "PlayerStatus: there was an error reading the suitUpgrades.json file.");
            Gdx.app.error("MewnBase", s.getMessage().toString());
        }
    }

    public void setSuitLevel(int suitLevel, boolean maxValues) {
        if (suitLevel < 0 || suitLevel >= PlayerStatus.suitUpgradeData.suitLevels.size()) {
            MoonBase.error("PlayerStatus: SuitSuitLevel: Invalid suit level int");
            suitLevel = 0;
        }
        SuitLevel defaultSuit = PlayerStatus.suitUpgradeData.suitLevels.get(suitLevel);
        if (MoonBase.getCurrentMission().missionType == Mission.MissionTypes.creative) {
            maxValues = true;
        }
        this.maxSuitPower = defaultSuit.maxSuitPower;
        if (maxValues) {
            this.suitPower = this.maxSuitPower;
        }
        this.maxAir = defaultSuit.maxAir;
        if (maxValues) {
            this.air = this.maxAir;
        }
        this.maxHunger = defaultSuit.maxHunger;
        if (maxValues) {
            this.setHunger(this.maxHunger);
        }
        this.maxHealth = defaultSuit.maxHealth;
        if (maxValues) {
            this.setHealth(this.maxHealth);
        }
        if (maxValues) {
            this.setMaxValues();
        }
        this.foodConsumptionRate = defaultSuit.foodConsumptionRate;
    }

    public void setMaxValues() {
        SuitLevel currentSuit = PlayerStatus.suitUpgradeData.suitLevels.get(this.player.suitLevel);
        this.setSuitPower(currentSuit.maxSuitPower);
        this.setAir(currentSuit.maxAir);
        this.setHunger(currentSuit.maxHunger);
        this.setHealth(currentSuit.maxHealth);
    }

    public void newStatusEffect(PlayerStatusEffect statusEffect) {
        this.statusEffects.add(statusEffect);
        this.player.notifyUpdate("addStatusEffects");
    }

    public boolean hasStatusEffectType(Class eClass) {
        for (PlayerStatusEffect e : this.statusEffects) {
            if (!e.getClass().equals(eClass)) continue;
            return true;
        }
        return false;
    }

    public void setHealth(float newHealth) {
        this.health = newHealth;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
        if (this.health < 0.0f) {
            // empty if block
        }
        this.updateHealthState();
        this.player.notifyUpdate("statusUpdate");
    }

    public void setHealthFull() {
        this.health = this.maxHealth;
        this.setHealth(this.health);
        this.healFx();
    }

    private void healFx() {
        ParticleActor p = new ParticleActor(Gdx.files.internal("particles/heal.p"), this.player.world.gameScreen.atlas, true);
        p.setPosition(this.player.getXPos(), this.player.getYPos());
        p.pfx.start();
        this.player.world.gameScreen.topGroup.addActor(p);
        p.toFront();
    }

    public void setSuitPower(float suitPower) {
        this.suitPower = suitPower;
        this.updateHealthState();
        this.player.notifyUpdate("statusUpdate");
    }

    public void changeAir(float amount) {
        this.air += amount;
        this.setAir(this.air);
    }

    public void setAir(float air) {
        if (air > this.maxAir) {
            air = this.maxAir;
        }
        if (air < 0.0f) {
            air = 0.0f;
        }
        this.air = air;
        this.player.notifyUpdate("statusUpdate");
    }

    public void takeHitDamage(float amount) {
        this.player.world.gameScreen.hud.newDamageFlyoff(amount, this.player.getXPos(), this.player.getYPos() + 40.0f, Color.RED);
        this.changeHealth(-amount);
    }

    public void changeHealth(float amount) {
        if (MoonBase.GOD_MODE && amount < 0.0f) {
            amount = 0.0f;
        }
        this.health += amount;
        this.setHealth(this.health);
    }

    public void updateHealthState() {
        this.healthState = this.health >= 90.0f ? HealthState.healthy : (this.health > 70.0f ? HealthState.minor : (this.health > 40.0f ? HealthState.injured : (this.health > 20.0f ? HealthState.severe : HealthState.mortally)));
    }

    public String getHealthStateText() {
        return Localization.get("playerHealth_" + (Object)((Object)this.healthState));
    }

    public void setHunger(float newHunger) {
        this.hunger = newHunger;
        if (this.hunger > this.maxHunger) {
            this.hunger = this.maxHunger;
        }
        if (this.hunger < 0.0f) {
            this.hunger = 0.0f;
        }
        this.updateHungerState();
        this.player.notifyUpdate("statusUpdate");
    }

    public void changeHunger(float amount) {
        this.hunger += amount;
        this.setHunger(this.hunger);
    }

    public void updateHungerState() {
        this.hungerState = this.hunger >= this.maxHunger * 0.7f ? HungerStates.full : (this.hunger > this.maxHunger * 0.4f ? HungerStates.normal : (this.hunger > this.maxHunger * 0.2f ? HungerStates.hungry : (this.hunger > 0.0f ? HungerStates.very : HungerStates.starving)));
    }

    public String getHungerStateText() {
        return Localization.get("playerHunger_" + (Object)((Object)this.hungerState));
    }

    private void updateAir(float delta) {
        VehicleCabin vc;
        Vehicle v;
        boolean airDraining = true;
        if (this.currentHabitatModule != null && this.currentHabitatModule.hasOxygen()) {
            airDraining = false;
            if (this.air < this.maxAir) {
                float consumeAmount = 15.0f * delta;
                if (!(this.currentHabitatModule.getBaseGroup().getTotalOxygenStored() >= consumeAmount)) {
                    consumeAmount = this.currentHabitatModule.getBaseGroup().getTotalOxygenStored() * delta;
                }
                this.currentHabitatModule.getBaseGroup().consumeResource(consumeAmount, BaseResources.oxygen, Float.valueOf(this.currentHabitatModule.getBaseGroup().getTotalOxygenStored()));
                this.air += consumeAmount;
                if (this.air > this.maxAir) {
                    this.air = this.maxAir;
                }
            } else {
                this.currentHabitatModule.getBaseGroup().consumeResource(delta, BaseResources.oxygen, Float.valueOf(this.currentHabitatModule.getBaseGroup().getTotalOxygenStored()));
            }
        }
        if (this.player.isDrivingVehicle() && (v = this.player.getVehicle()) instanceof VehicleCabin && (vc = (VehicleCabin)((Object)v)).hasOxygen()) {
            airDraining = false;
            this.air += 15.0f * delta;
            if (this.air > this.maxAir) {
                this.air = this.maxAir;
            }
        }
        if (airDraining) {
            this.air -= delta;
            if (this.air <= 0.0f) {
                this.air = 0.0f;
                this.changeHealth(-5.0f * delta);
                this.updateHealth(delta);
                if (!this.hasStatusEffectType(SuffocationStatusEffect.class)) {
                    SuffocationStatusEffect s = new SuffocationStatusEffect();
                    s.playerStatus = this;
                    s.showWarning = true;
                    s.alertSound = true;
                    this.newStatusEffect(s);
                    this.player.playerAnimController.suffocateAnim(true);
                }
            }
        }
        if (this.hasStatusEffectType(SuffocationStatusEffect.class) && this.air > 0.0f) {
            this.player.playerAnimController.suffocateAnim(false);
        }
        this.player.notifyUpdate("updateAir");
    }

    public void updateCurrentHabitatModule() {
        this.currentHabitatModule = this.player.getCurrentBase() != null ? this.player.getHabitatModule() : null;
    }

    public void update(float delta) {
        this.updateTemperature(delta);
        this.updateAir(delta);
        this.updateSuitPower(delta);
        this.updateHunger(delta);
        this.updateHealth(delta);
    }

    private void updateTemperature(float delta) {
        this.tempUpdateTimer += delta;
        if (this.tempUpdateTimer > this.tempUpdateDelay) {
            this.tempUpdateTimer = 0.0f;
            this.temperature = this.player.world.getTempAtPosition(this.player.getXPos(), this.player.getYPos());
        }
        if (this.temperature > 2.0f) {
            if (!MoonBase.GOD_MODE && !this.inDozer()) {
                this.player.playerAnimController.setOnFire(true);
                if (!this.hasStatusEffectType(HeatStatusEffect.class)) {
                    HeatStatusEffect hse = new HeatStatusEffect();
                    hse.alertSound = false;
                    hse.playerStatus = this;
                    this.newStatusEffect(hse);
                }
                float d = this.temperature - 2.0f;
                this.changeHealth(-d * 2.0f * delta);
                this.updateHealth(delta);
            }
        } else {
            this.player.playerAnimController.setOnFire(false);
        }
    }

    private boolean inDozer() {
        if (this.player.getVehicle() != null) {
            return this.player.getVehicle() instanceof Tank;
        }
        return false;
    }

    protected void updateStatusEffects(float delta) {
        boolean removing = false;
        for (int i = this.statusEffects.size() - 1; i >= 0; --i) {
            this.statusEffects.get(i).update(delta);
            if (!this.statusEffects.get((int)i).readyToRemove) continue;
            System.out.println("removing status effect: " + this.statusEffects.get(i).getClass().getSimpleName());
            this.statusEffects.remove(i);
            removing = true;
        }
        if (removing) {
            this.player.notifyUpdate("removeStatusEffects");
        }
    }

    private void updateHunger(float delta) {
        this.changeHunger(-this.foodConsumptionRate * delta);
        if (this.hungerState == HungerStates.starving) {
            this.changeHealth(-2.0f * delta);
            if (!this.hasStatusEffectType(StarvingStatusEffect.class)) {
                StarvingStatusEffect s = new StarvingStatusEffect();
                s.playerStatus = this;
                s.showWarning = true;
                s.alertSound = true;
                this.newStatusEffect(s);
                this.player.playerAnimController.hungryAnim();
            }
        }
        if (this.air > 0.0f && this.hungerState == HungerStates.full && this.health < this.maxHealth) {
            this.changeHealth(1.0f * delta);
        }
    }

    public void changeThirst(int i) {
        System.out.println("PlayerStatus: Change thirst hasn't been implemented");
    }

    private void updateHealth(float delta) {
        this.healthChange = "";
        this.healthChanging = 0;
        if (this.health > this.oldHealth) {
            this.healthChange = "+";
            this.healthChanging = 1;
        } else if (this.health < this.oldHealth) {
            this.healthChange = "-";
            this.healthChanging = -1;
        }
        if (this.health <= 0.0f) {
            this.player.world.gameOver(GameOverReasons.REASONS.health);
            this.player.playerAnimController.deathAnim();
        }
        this.oldHealth = this.health;
    }

    public void changeSuitPower(float amount) {
        this.suitPower += amount;
        if (this.suitPower < 0.0f) {
            this.suitPower = 0.0f;
        } else if (this.suitPower > this.maxSuitPower) {
            this.suitPower = this.maxSuitPower;
        }
    }

    private void updateSuitPower(float delta) {
        boolean hasPower;
        boolean bl = hasPower = this.currentHabitatModule != null && this.currentHabitatModule.hasPower;
        if (hasPower) {
            if (this.suitPower < this.maxSuitPower) {
                this.suitPower += delta * 15.0f;
                if (this.suitPower > this.maxSuitPower) {
                    this.suitPower = this.maxSuitPower;
                }
            }
        } else if (this.flashlight) {
            this.suitPower -= delta;
            if (this.suitPower <= 0.0f) {
                this.suitPower = 0.0f;
            }
        }
        if (this.suitPower == 0.0f) {
            if (this.suitHasPower) {
                this.setFlashlight(false);
                this.player.light.setActive(false);
                this.suitHasPower = false;
                this.player.notifyUpdate("suitLostPower");
            }
        } else if (!this.suitHasPower) {
            this.suitHasPower = true;
            this.player.notifyUpdate("suitPowerRestored");
        }
        this.player.notifyUpdate("updateSuitPower");
    }

    public void setFlashlight(boolean on) {
        if (this.suitHasPower && on) {
            this.flashlight = true;
            if (this.player != null && this.player.light != null) {
                this.player.light.setActive(this.flashlight);
                this.player.flashlightGlow.setActive(this.flashlight);
                this.player.playerAnimController.setFlashlight(this.flashlight);
            }
            if (this.player.world.gameState == World.GameStates.playing) {
                Audio.getInstance().playSound("sounds/poweroff.ogg", 0.1f, 6.0f);
            }
        } else {
            this.flashlight = false;
            if (this.player != null && this.player.light != null) {
                this.player.light.setActive(false);
                this.player.flashlightGlow.setActive(false);
                this.player.playerAnimController.setFlashlight(false);
            }
            if (this.player.world.gameState == World.GameStates.playing) {
                Audio.getInstance().playSound("sounds/poweroff.ogg", 0.1f, 4.0f);
            }
        }
        this.player.notifyUpdate("flashlightUpdate");
    }

    public int updateCommsRange(float delta) {
        float closest = CommsTower.commsRange + 100.0f;
        this.baseGroupsInVisibleRange.clear();
        this.baseGroupsInRange.clear();
        ArrayList<CommsTower> ctsInRange = this.getCommsTowersInRange();
        for (CommsTower ct : ctsInRange) {
            float dst = this.distanceToTile(ct.worldX, ct.worldY);
            if (dst < closest) {
                closest = dst;
            }
            if (this.baseGroupsInRange.contains(ct.getBaseGroup())) continue;
            this.baseGroupsInRange.add(ct.getBaseGroup());
        }
        this.baseGroupsInVisibleRange.addAll(this.getVisualBaseGroupsForComms());
        int r = 1;
        if (this.baseGroupsInRange.size() > 0) {
            r = closest < CommsTower.commsRange / 2.0f ? 3 : 2;
        } else if (this.baseGroupsInVisibleRange.size() > 0) {
            r = 2;
        }
        this.baseGroupsInRange.addAll(this.baseGroupsInVisibleRange);
        return r;
    }

    private ArrayList<BaseGroup> getVisualBaseGroupsForComms() {
        ArrayList<BaseGroup> baseGroupList = new ArrayList<BaseGroup>();
        ArrayList<GridPoint2> gpList = Tile.GET_NEARBY_COORDS(this.player.getX(), this.player.getY(), 5);
        for (GridPoint2 gp : gpList) {
            Tile t = this.player.world.getTile(gp.x, gp.y);
            if (t != null && t instanceof BaseModule) {
                BaseModule b = (BaseModule)t;
                if (b.hasPower && b.usesComms() && b.usesBaseGroup() && !baseGroupList.contains(b.getBaseGroup())) {
                    baseGroupList.add(b.getBaseGroup());
                }
            }
            World.gridPointPool.free(gp);
        }
        return baseGroupList;
    }

    private ArrayList<CommsTower> getClosestCommsTowers() {
        ArrayList<CommsTower> commsInRangeList = new ArrayList<CommsTower>();
        ArrayList<CommsTower> commsList = new ArrayList<CommsTower>();
        for (BaseGroup bg : this.player.world.baseManager.getBaseGroupList()) {
            commsList.clear();
            commsList.addAll(bg.getAllWorkingCommsTowers());
            for (CommsTower c : commsList) {
                if (!(this.distanceToTile(c.worldX, c.worldY) < CommsTower.commsRange)) continue;
                commsInRangeList.add(c);
            }
        }
        return commsInRangeList;
    }

    public ArrayList<CommsTower> getCommsTowersInRange() {
        ArrayList<CommsTower> masterList = this.getClosestCommsTowers();
        ArrayList<CommsTower> ctsToCheck = new ArrayList<CommsTower>();
        ctsToCheck.addAll(masterList);
        int checks = 0;
        while (ctsToCheck.size() > 0) {
            for (int i = ctsToCheck.size() - 1; i >= 0; --i) {
                CommsTower ct = (CommsTower)ctsToCheck.get(i);
                if (!masterList.contains(ct)) {
                    masterList.add(ct);
                }
                ArrayList<CommsTower> newCts = ct.getOtherCommsTowersInRange();
                for (CommsTower nct : newCts) {
                    if (masterList.contains(nct)) continue;
                    ctsToCheck.add(nct);
                }
                ctsToCheck.remove(ct);
            }
            ++checks;
        }
        return masterList;
    }

    private float distanceToTile(int worldX, int worldY) {
        if (this.worldPos == null) {
            this.worldPos = new Vector2(0.0f, 0.0f);
        }
        this.worldPos.set((float)worldX * Tile.TILE_SIZE, (float)worldY * Tile.TILE_SIZE);
        return this.worldPos.dst(this.player.getXPos(), this.player.getYPos());
    }

    public static enum HealthState {
        healthy,
        minor,
        injured,
        severe,
        mortally;

    }

    public static enum ThirstStates {
        full,
        normal,
        parched;

    }

    public static enum HungerStates {
        full,
        normal,
        hungry,
        very,
        starving;

    }
}

