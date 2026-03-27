/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BaseGroup;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.PlayerInventory;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.ResourceMeterBehavior;
import com.cairn4.moonbase.tiles.disasters.BaseDisaster;
import com.cairn4.moonbase.ui.Localization;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class BaseModule
extends Tile {
    public float health;
    public float maxHealth;
    public int powerPriority = 0;
    public String builderId;
    private BaseGroup baseGroup;
    protected float lightSize = 400.0f;
    public Image noPower;
    public Image noWaterIcon;
    public Image noOxygenIcon;
    public PointLight light;
    protected float oxygen;
    public static final float maxOxygen = 25.0f;
    public Group componentGroup;
    public Group statusGroup;
    protected float powerGenRate;
    protected float maxPowerGen;
    protected float powerDrawRate;
    protected float fuelGenRate;
    protected float fuelDrawRate;
    protected float oxygenGenRate;
    protected float oxygenDrawRate;
    protected float waterGenRate;
    protected float waterDrawRate;
    protected float waterConsumptionRate;
    protected float waterStored;
    public BaseDisaster baseDisaster;
    protected static Color POWER_OFF_COLOR = new Color(0.3f, 0.3f, 0.3f, 1.0f);
    protected static Color POWER_OFF_DISASTER_COLOR = new Color(0.5f, 0.0f, 0.0f, 1.0f);
    protected Color lightOnColor = new Color(1.0f, 1.0f, 0.95f, 1.0f);
    boolean disablePickup;
    boolean beingPickedUp;
    boolean prevBeingPickedUp;
    ResourceMeterBehavior debugWaterMeter;
    ResourceMeterBehavior debugOxygenMeter;
    ResourceMeterBehavior debugPowerMeter;

    public float getPowerGenRate() {
        return this.powerGenRate;
    }

    public float getPowerDrawRate() {
        return this.powerDrawRate;
    }

    public void setPowerDrawRate(float p) {
        this.powerDrawRate = p;
    }

    public float getMaxPowerGen() {
        return this.maxPowerGen;
    }

    public void setOxygenDrawRate(float oxygenRate) {
        this.oxygenDrawRate = oxygenRate;
    }

    public void resetOxygenDrawRate() {
        this.oxygenDrawRate = 0.0f;
    }

    public float getOxygenGenRate() {
        return this.oxygenGenRate;
    }

    public float getOxygenDrawRate() {
        return this.oxygenDrawRate;
    }

    public float getFuelGenRate() {
        return this.fuelGenRate;
    }

    public float getFuelDrawRate() {
        return this.fuelDrawRate;
    }

    public void setWaterGenRate(float w) {
        this.waterGenRate = w;
    }

    public float getWaterGenRate() {
        return this.waterGenRate;
    }

    public float getWaterDrawRate() {
        return this.waterDrawRate;
    }

    public float getWaterConsumptionRate() {
        return this.waterConsumptionRate;
    }

    public float getWaterStored() {
        return this.waterStored;
    }

    public void consumeWater(float amount) {
        this.waterStored -= amount;
    }

    public boolean usesBaseGroup() {
        return true;
    }

    public boolean usesComms() {
        return true;
    }

    public boolean visuallyConnectToConduits() {
        return true;
    }

    public float getResourceGenRate(BaseResources type) {
        switch (type) {
            case power: {
                return this.getPowerGenRate();
            }
            case water: {
                return this.getWaterGenRate();
            }
            case oxygen: {
                return this.getOxygenGenRate();
            }
            case fuel: {
                return this.getFuelGenRate();
            }
        }
        return 0.0f;
    }

    public float getResourceDrawRate(BaseResources type) {
        switch (type) {
            case power: {
                return this.getPowerDrawRate();
            }
            case water: {
                return this.getWaterConsumptionRate();
            }
            case oxygen: {
                return this.getOxygenDrawRate();
            }
            case fuel: {
                return this.getFuelDrawRate();
            }
        }
        return 0.0f;
    }

    public void disablePickup(boolean disabled) {
        this.disablePickup = disabled;
    }

    public BaseModule(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.type = Tile.types.base;
        this.blocker = true;
        this.hasAir = false;
        this.hasPower = true;
        this.powerGenRate = 0.0f;
        this.powerDrawRate = 0.0f;
        this.waterConsumptionRate = 0.0f;
        this.oxygen = 25.0f;
        this.clearGroundTile();
        this.setupLight();
        this.componentGroup = new Group();
        this.group.addActor(this.componentGroup);
        this.statusGroup = new Group();
        this.group.addActor(this.statusGroup);
        this.noPower = new Image(world.gameScreen.skin.getDrawable("no-power"));
        this.noPower.setSize(TILE_SIZE, TILE_SIZE);
        this.noPower.setOrigin(1);
        this.noPower.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.noPower.setVisible(false);
        this.statusGroup.addActor(this.noPower);
        this.noOxygenIcon = new Image(world.gameScreen.skin.getDrawable("no-oxygen"));
        this.noOxygenIcon.setSize(TILE_SIZE * 0.75f, TILE_SIZE * 0.75f);
        this.noOxygenIcon.setOrigin(1);
        this.noOxygenIcon.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.noOxygenIcon.setVisible(false);
        this.statusGroup.addActor(this.noOxygenIcon);
        this.noWaterIcon = new Image(world.gameScreen.skin.getDrawable("no-water"));
        this.noWaterIcon.setSize(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f);
        this.noWaterIcon.setOrigin(1);
        this.noWaterIcon.setPosition(TILE_SIZE + 2.0f, TILE_SIZE - 5.0f, 18);
        this.noWaterIcon.setVisible(false);
        this.statusGroup.addActor(this.noWaterIcon);
        ArrayList<GridPoint2> adjacent = BaseModule.GET_ADJACENT_COORDS(this.worldX, this.worldY, true);
        for (GridPoint2 gp : adjacent) {
            if (world.getGroundTile(gp.x, gp.y) != null) {
                world.getGroundTile(gp.x, gp.y).setDiscovered(true);
            }
            World.gridPointPool.free(gp);
        }
    this.setBuilderId();
    }

    public void setIconOverlayPosition(float x, float y) {
        this.noOxygenIcon.setPosition(x, y, 1);
        this.noWaterIcon.setPosition(x, y - 5.0f, 1);
        this.noOxygenIcon.setPosition(x, y, 1);
    }

    public void animateSpawn() {
        this.group.addAction(BaseModule.getSpawnAnimation());
        ParticleEffect dustFx = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/build-dust.p", ParticleEffect.class));
        ParticleActor pa = new ParticleActor(dustFx, true);
        pa.setPosition(this.getXCenter(), this.getYCenter() - 5.0f);
        pa.pfx.start();
        this.world.gameScreen.mainGroup.addActor(pa);
    }

    public static Action getSpawnAnimation() {
        float t = 0.12f;
        return Actions.sequence(Actions.alpha(0.0f), Actions.scaleTo(0.8f, 0.8f), Actions.parallel((Action)Actions.scaleTo(1.05f, 1.05f, t), (Action)Actions.alpha(1.0f, t), (Action)Actions.moveBy(0.0f, 10.0f, t, Interpolation.sineOut)), Actions.parallel((Action)Actions.scaleTo(1.0f, 1.0f, t), (Action)Actions.moveBy(0.0f, -10.0f, t, Interpolation.sineIn)), Actions.moveBy(0.0f, 2.0f, t * 0.4f), Actions.moveBy(0.0f, -2.0f, t * 0.2f));
    }

    @Override
    public String getMapName() {
        return Localization.get("item_" + this.builderId + "-builder");
    }

    public void loadDisasters(ArrayList<String> disasterList) {
        for (int i = disasterList.size() - 1; i >= 0; --i) {
            BaseDisaster bd;
            MoonBase.debug("BaseModule: Creating disaster " + disasterList.get(i));
            Object object = null;
            Class[] classArgs = new Class[]{BaseModule.class};
            Object[] arguments = new Object[]{this};
            try {
                Class<?> aClass = Class.forName(disasterList.get(i));
                Constructor<?> constructor = null;
                constructor = aClass.getConstructor(classArgs);
                object = constructor.newInstance(arguments);
            }
            catch (InstantiationException e) {
                System.out.println(e);
            }
            catch (IllegalAccessException e) {
                System.out.println(e);
            }
            catch (InvocationTargetException e) {
                e.getCause();
                e.printStackTrace();
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            this.baseDisaster = bd = (BaseDisaster)object;
        }
    }

    public void setupLight() {
        this.light = new PointLight(this.world.rayHandler, 16, this.getLightOnColor(), 0.46875f, this.getXCenter() / 256.0f, this.getYCenter() / 256.0f);
        this.light.setSoftnessLength(0.0390625f);
        this.light.attachToBody(this.body);
        this.light.setIgnoreAttachedBody(true);
        this.light.setStaticLight(true);
        this.light.setContactFilter(this.categoryBits, this.categoryBits, this.maskBits);
    }

    public Color getLightOnColor() {
        return this.lightOnColor;
    }

    public boolean canPickup() {
        return !this.disablePickup;
    }

    @Override
    public Color getMinimapColor() {
        return Color.GOLD;
    }

    protected void setBuilderId() {
    }

    protected void updateBases() {
        this.world.baseManager.updateBases(this.world);
    }

    public void setBaseGroup(BaseGroup group) {
        this.baseGroup = group;
    }

    public BaseGroup getBaseGroup() {
        return this.baseGroup;
    }

    private void clearSurrounding() {
        for (GridPoint2 adjacent : Tile.GET_ADJACENT_COORDS(this.worldX, this.worldY, true)) {
            Tile t = this.world.getTile(adjacent.x, adjacent.y);
            if (t == null || t.type != Tile.types.blockingTerrain) continue;
            t.destroy();
        }
    }

    protected void clearGroundTile() {
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("room");
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.baseDisaster != null) {
            this.baseDisaster.update(delta);
        }
        this.updatePickup(delta);
    }

    @Override
    public void destroy() {
        if (this.light != null) {
            // empty if block
        }
        this.removeDisaster();
        super.destroy();
    }

    protected void updatePickup(float delta) {
        if (this.prevBeingPickedUp && !this.beingPickedUp) {
            this.pickupTimer = 0.0f;
            Gdx.app.debug("MewnBase", "BaseModule: Cancel pickup");
            this.world.gameScreen.hud.removeTileProgressBar(this);
        }
        this.prevBeingPickedUp = this.beingPickedUp;
        this.beingPickedUp = false;
    }

    public void pickingUpProgress() {
        if (!this.beingPickedUp && this.pickupTimer == 0.0f) {
            this.world.gameScreen.hud.addTilePickupProgressBar(this);
        }
        this.pickupTimer += Gdx.graphics.getDeltaTime();
        this.beingPickedUp = true;
        if (this.pickupTimer > 1.0f) {
            this.dropPickup();
            this.readyToRemove = true;
            this.world.gameScreen.hud.removeTileProgressBar(this);
        }
    }

    @Override
    public void interact(Player player) {
        if (this.baseDisaster != null) {
            this.baseDisaster.fix();
        } else {
            this.interactMain(player);
        }
    }

    public boolean hasDisaster() {
        return this.baseDisaster != null;
    }

    public void interactMain(Player player) {
    }

    public void toggleResource(BaseResources type, boolean on) {
        switch (type) {
            case power: {
                this.setPower(on);
                break;
            }
            case water: {
                this.setWater(on);
                break;
            }
            case oxygen: {
                this.setOxygen(on);
            }
        }
    }

    public void setWater(boolean on) {
        if (on != this.hasWater) {
            if (on) {
                this.hasWater = true;
                this.noWaterIcon(false);
            } else {
                this.hasWater = false;
                this.noWaterIcon(true);
            }
        }
    }

    public void resetPowerState() {
        if (this.hasPower) {
            this.image.clearActions();
            this.setLight(true);
            if (this.baseDisaster != null) {
                this.animateColor(this.image, Color.RED, 0.25f);
                this.setLightColor(1.0f, 0.0f, 0.0f, 1.0f);
            } else {
                this.animateColor(this.image, this.getPoweredColor(), 0.25f);
                this.setLightColor(this.getLightOnColor());
            }
            this.setSpinePowerState(this.hasPower);
            float pitch = MathUtils.random(1.2f, 1.3f);
            float v = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 600.0f, 0.25f);
            Audio.getInstance().playSound("sounds/poweroff.ogg", v, pitch);
            this.noPowerIcon(false);
        } else {
            if (this.baseDisaster != null) {
                this.animateColor(this.image, POWER_OFF_DISASTER_COLOR, 0.25f);
            } else {
                this.animateColor(this.image, this.getPoweredOffColor(), 0.25f);
            }
            this.setSpinePowerState(false);
            Gdx.app.debug("MewnBase", "Base loose power : " + this.builderId);
            this.setLight(false);
            this.noPowerIcon(true);
            float pitch = MathUtils.random(0.95f, 1.05f);
            float v = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 600.0f, 0.25f);
            Audio.getInstance().playSound("sounds/poweroff.ogg", v, pitch);
        }
    }

    public Color getPoweredColor() {
        return Color.WHITE;
    }

    public Color getPoweredOffColor() {
        return POWER_OFF_COLOR;
    }

    public void setPower(boolean on) {
        if (on != this.hasPower) {
            if (on) {
                this.image.clearActions();
                this.setLight(true);
                if (this.baseDisaster != null) {
                    this.animateColor(this.image, Color.RED, 0.25f);
                    this.setLightColor(1.0f, 0.0f, 0.0f, 1.0f);
                } else {
                    this.animateColor(this.image, this.getPoweredColor(), 0.25f);
                    this.setLightColor(this.getLightOnColor());
                }
                this.setSpinePowerState(on);
                float pitch = MathUtils.random(1.2f, 1.3f);
                float v = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 600.0f, 0.25f);
                Audio.getInstance().playSound("sounds/poweroff.ogg", v, pitch);
                this.noPowerIcon(false);
            } else {
                if (this.baseDisaster != null) {
                    this.animateColor(this.image, POWER_OFF_DISASTER_COLOR, 0.25f);
                } else {
                    this.animateColor(this.image, this.getPoweredOffColor(), 0.25f);
                }
                this.setSpinePowerState(false);
                Gdx.app.debug("MewnBase", "Base loose power : " + this.builderId);
                this.setLight(false);
                this.noPowerIcon(true);
                float pitch = MathUtils.random(0.95f, 1.05f);
                float v = Audio.getInstance().playerDistanceMultiplier(this.world, new Vector2(this.getXCenter(), this.getYCenter()), 600.0f, 0.25f);
                Audio.getInstance().playSound("sounds/poweroff.ogg", v, pitch);
            }
            this.hasPower = on;
        }
    }

    public void setSpinePowerState(boolean on) {
    }

    protected void setLight(boolean on) {
        if (this.light != null) {
            this.light.setActive(on);
        }
    }

    private void setLightColor(Color c) {
        this.setLightColor(c.r, c.g, c.b, c.a);
    }

    protected void setLightColor(float r, float g, float b, float a) {
        if (this.light != null) {
            this.light.setColor(r, g, b, a);
        }
    }

    protected void animateColor(Image i, Color c, float speed) {
        i.clearActions();
        i.addAction(Actions.sequence((Action)Actions.color(c, speed)));
    }

    protected void noPowerIcon(boolean show) {
        this.noPower.clearActions();
        this.noPower.setRotation(-this.group.getRotation());
        if (show) {
            this.noPower.addAction(Actions.forever(Actions.sequence((Action)Actions.visible(true), (Action)Actions.delay(0.75f), (Action)Actions.visible(false), (Action)Actions.delay(0.75f))));
        } else {
            this.noPower.setVisible(false);
        }
    }

    protected void noOxygenIcon(boolean show) {
        this.noOxygenIcon.clearActions();
        this.noOxygenIcon.setRotation(-this.group.getRotation());
        if (show) {
            this.noOxygenIcon.addAction(Actions.forever(Actions.sequence((Action)Actions.visible(true), (Action)Actions.delay(0.75f), (Action)Actions.visible(false), (Action)Actions.delay(0.75f))));
        } else {
            this.noOxygenIcon.setVisible(false);
        }
    }

    public void noWaterIcon(boolean show) {
        this.noWaterIcon.clearActions();
        this.noWaterIcon.setRotation(-this.group.getRotation());
        if (show) {
            this.noWaterIcon.addAction(Actions.forever(Actions.sequence((Action)Actions.visible(true), (Action)Actions.delay(0.75f), (Action)Actions.visible(false), (Action)Actions.delay(0.75f))));
        } else {
            this.noWaterIcon.setVisible(false);
        }
    }

    private void animateColorFlicker() {
        float delay1 = (float)Math.random() * 0.22f;
        float delay2 = (float)Math.random() * 0.5f;
        float delay3 = (float)Math.random() * 0.15f;
        float delay4 = (float)Math.random() * 0.25f;
        Color colorOn = new Color(0.4f, 0.4f, 0.4f, 1.0f);
        Color colorOff = new Color(0.3f, 0.3f, 0.3f, 1.0f);
        this.image.clearActions();
        this.image.addAction(Actions.sequence((Action)Actions.forever(Actions.sequence(Actions.color(colorOn), Actions.delay(delay1), Actions.color(colorOff), Actions.delay(delay2), Actions.color(colorOn), Actions.delay(delay3), Actions.color(colorOff), Actions.delay(delay4), Actions.color(colorOn), Actions.delay(delay3), Actions.color(colorOff), Actions.delay(delay4), Actions.color(colorOn), Actions.delay(delay1), Actions.color(colorOff), Actions.delay(delay2)))));
    }

    public void triggerDisaster() {
        if (this.baseDisaster == null) {
            this.baseDisaster = new BaseDisaster(this);
        }
    }

    public void triggerDisaster(BaseDisaster bd) {
        if (this.baseDisaster != null) {
            this.baseDisaster.fix();
        }
        this.baseDisaster = bd;
    }

    public void removeDisaster() {
        if (this.baseDisaster != null) {
            this.baseDisaster.dispose();
            this.baseDisaster = null;
            this.resetPowerState();
        }
    }

    protected void setOxygen(boolean on) {
        this.hasAir = on;
    }

    public boolean hasOxygen() {
        return this.hasAir;
    }

    public void fixDisaster() {
        this.baseDisaster.fix();
    }

    public void dropPickup() {
        if (this.baseDisaster != null) {
            this.baseDisaster.fix();
        }
        if (this.light != null) {
            this.light.remove();
        }
        Audio.getInstance().playSound("sounds/placeObject.ogg", 1.0f, 1.5f);
        Gdx.app.debug("MoonBase", "BaseModule: Picking up " + this.getClass().getSimpleName());
        Item baseBuilder = ItemFactory.getInstance().createItem(this.builderId + "-builder");
        ItemPickup pickup = new ItemPickup(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos(), baseBuilder);
        // Do NOT broadcast module builder drop to all clients. The breaker (local player) receives the module builder locally.
        // Historical behaviour: only the player who broke the module should get the builder item. Keep it local.
    }

    public void damage(float diff) {
        Gdx.app.debug("MewnBase", "Crashed buggie into module " + this.getClass().getSimpleName() + ": " + diff);
    }

    public void tutorialLock(boolean b) {
        this.disablePickup = b;
    }

    public void handleDroppedItem(PlayerInventory playerInventory, ItemStack stack) {
    }

    public Behavior getBehavior(Class behavior) {
        for (Behavior b : this.behaviorList) {
            if (!behavior.isInstance(b)) continue;
            return b;
        }
        return null;
    }

    public static enum ORIENTATIONS {
        north,
        east,
        south,
        west;

    }
}

