/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.ItemActions;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.BuggieTrunkUI;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import java.util.ArrayList;
import java.util.HashMap;

public class VehicleTrailer
extends Vehicle {
    public Image imageBase;
    RevoluteJoint joint;

    public VehicleTrailer(World world, float xPos, float yPos, float rotation) {
        super(world, xPos, yPos, rotation, "trailer");
        this.setState(Vehicle.STATES.empty);
    }

    @Override
    public void doneLoading() {
        Entity e;
        this.setColor(this.paintColorIndex);
        for (InventoryItemData iid : this.inventoryItemDataList) {
            try {
                ItemStack newStack = new ItemStack(iid.itemId, iid.amount, iid.durability);
                int durabilityForThisType = ItemFactory.getDurability(iid.itemId);
                if (durabilityForThisType > 0 && iid.durability == 0) {
                    iid.durability = durabilityForThisType;
                }
                this.trunkItems.add(newStack);
            }
            catch (Exception e2) {
                MoonBase.error("VehicleTrailer: Couldn't create item: " + iid.itemId);
            }
        }
        this.inventoryItemDataList.clear();
        Gdx.app.debug("MewnBase", "Buggie: has data items: " + this.inventoryItemDataList.size);
        if (this.parentVehicleId != -1L && (e = this.world.getEntityById(this.parentVehicleId)) != null && e instanceof Vehicle) {
            this.setAttachment((Vehicle)e);
        }
    }

    @Override
    public void createDrawable(String sprite) {
        super.createDrawable(sprite);
        this.imageBase = new Image(this.world.gameScreen.skin.getDrawable("trailer-base-default"));
        this.imageBase.setSize(150.0f, 128.0f);
        this.imageBase.setOrigin(1);
        this.imageBase.setPosition(0.0f, 0.0f, 1);
        this.chassisGroup.addActor(this.imageBase);
        this.image = new Image(this.world.gameScreen.skin.getDrawable(sprite));
        this.image.setSize(150.0f, 128.0f);
        this.image.setOrigin(1);
        this.image.setPosition(0.0f, 0.0f, 1);
        this.chassisGroup.addActor(this.image);
        this.image.setTouchable(Touchable.enabled);
        this.image.addListener(new ClickListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.debug("MewnBase", "Trailer image click touchdown");
                VehicleTrailer.this.setClicked();
                return false;
            }
        });
        this.tailLightSprite = new AdditiveImage(this.world.gameScreen.skin.getDrawable("buggie-taillight"));
        this.tailLightSprite.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.tailLightSprite.setOrigin(1);
        this.tailLightSprite.setPosition(0.0f, 0.0f, 1);
        this.tailLightSprite.setTouchable(Touchable.disabled);
        this.chassisGroup.addActor(this.tailLightSprite);
    }

    @Override
    public HashMap<String, Object> getProperties() {
        this.inventoryItemDataList.clear();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("health", Float.valueOf(this.health));
        properties.put("id", this.id);
        properties.put("paintColorIndex", this.paintColorIndex);
        if (this.parentVehicle != null) {
            this.parentVehicleId = this.parentVehicle.id;
            properties.put("parentVehicleId", this.parentVehicleId);
        }
        for (ItemStack s : this.trunkItems) {
            InventoryItemData iid = new InventoryItemData();
            iid.itemId = s.getId();
            iid.amount = s.getAmount();
            iid.durability = s.item.durability;
            this.inventoryItemDataList.add(iid);
        }
        properties.put("inventoryItemDataList", this.inventoryItemDataList);
        Gdx.app.debug("MewnBase", "Buggie inventoryItemDataList: " + this.inventoryItemDataList.size);
        return properties;
    }

    @Override
    public void setState(Vehicle.STATES newState) {
    }

    @Override
    public void playerAction(Player player) {
        boolean usingTool = false;
        boolean didRepair = false;
        ItemStack itemStack = player.playerInventory.getEquippedItem();
        if (itemStack != null) {
            ItemData id = ItemFactory.getItemData(itemStack.getId());
            for (ItemActions ia : id.actions) {
                if (ia.type.equals("paint")) {
                    usingTool = true;
                    player.playerAnimController.paint();
                    this.setColor(player.getPaintColorIndex());
                    this.paintPuffFx();
                }
                if (!ia.type.equals("repair")) continue;
                usingTool = true;
                didRepair = this.repair(itemStack);
                if (!didRepair) continue;
                player.playerInventory.consumeItem(itemStack, 1);
            }
        }
        if (!usingTool && this.trunkUI == null) {
            this.trunkUI = new BuggieTrunkUI(this.world.gameScreen, this.trunk, player.playerInventory);
            this.trunkUI.storageLabel.setText(Localization.get("item_trailer"));
            this.world.gameScreen.showMenu(this.trunkUI);
            this.trunkUI = null;
        }
    }

    @Override
    public void playerActionSecondary(Player player) {
        Vector2 worldPos = new Vector2(player.getXPos(), player.getYPos());
        float distance = worldPos.dst(this.getXPos(), this.getYPos());
        if (distance <= interactDistance) {
            Gdx.app.debug("MewnBase", "Trailer right clicked : " + this.clicked);
            this.toggleAttachment();
        }
    }

    public void toggleAttachment() {
        if (this.parentVehicle == null) {
            Vehicle closestVehicle = null;
            float minDistance = 200.0f;
            for (Entity e : this.world.entityList) {
                if (!(e instanceof Vehicle) || e == this) continue;
                Vehicle v = (Vehicle)e;
                if (v.childVehicle != null || !v.vd.trailerHitch) continue;
                Vector2 worldPos = new Vector2(v.getXPos(), v.getYPos());
                float distance = worldPos.dst(this.getXPos(), this.getYPos());
                Gdx.app.debug("MewnBase", "Trailer: Found vehicle, distance: " + distance);
                if (!(distance < minDistance)) continue;
                boolean parentIsSelf = false;
                ArrayList<Vehicle> allParents = this.getAllParents(v);
                for (Vehicle p : allParents) {
                    if (this != p) continue;
                    parentIsSelf = true;
                    MoonBase.log("can't connect to self");
                    break;
                }
                if (parentIsSelf) continue;
                minDistance = distance;
                closestVehicle = v;
            }
            if (closestVehicle != null) {
                this.setAttachment(closestVehicle);
            }
        } else {
            this.parentVehicle.childVehicle = null;
            this.parentVehicle = null;
            this.parentVehicleId = -1L;
            if (this.joint != null) {
                this.world.b2dWorld.destroyJoint(this.joint);
            }
        }
        this.tailLightSprite.setVisible(this.parentVehicle != null);
    }

    private ArrayList<Vehicle> getAllParents(Vehicle v) {
        ArrayList<Vehicle> list = new ArrayList<Vehicle>();
        while (v.parentVehicle != null) {
            list.add(v.parentVehicle);
            v = v.parentVehicle;
        }
        return list;
    }

    public void setAttachment(Vehicle v) {
        Gdx.app.log("MewnBase", "VehicleTrailer: attaching to vehicle id : " + v.id);
        this.parentVehicle = v;
        this.parentVehicle.childVehicle = this;
        this.parentVehicleId = this.parentVehicle.id;
        RevoluteJointDef jointDef = new RevoluteJointDef();
        if (this.body != null && this.parentVehicle.body != null) {
            if (this.body != this.parentVehicle.body) {
                jointDef.bodyA = this.body;
                jointDef.bodyB = this.parentVehicle.body;
                jointDef.localAnchorA.set(0.234375f, 0.0f);
                jointDef.localAnchorB.set(-0.29296875f, 0.0f);
                jointDef.collideConnected = true;
                this.joint = (RevoluteJoint)this.world.b2dWorld.createJoint(jointDef);
            } else {
                Gdx.app.error("MewnBase", "VehicleTrailer: Error, can't attach to itself.");
            }
        } else {
            Gdx.app.error("MewnBase", "VehicleTrailer: Error finding vehicle or trailer body to attach");
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.dustTrail.setPosition(this.group.getX(), this.group.getY());
        if (this.health < this.vd.damagedThreshold) {
            this.damageSmoke.setPosition(this.group.getX(), this.group.getY());
        }
    }

    @Override
    public void updateDampening() {
        if (this.parentVehicle != null) {
            this.body.setLinearDamping(this.parentVehicle.getDampening());
        } else {
            this.body.setLinearDamping(4.0f);
        }
    }

    @Override
    public void remove() {
        this.damageSmoke.pfx.allowCompletion();
        Group fxGroup = new Group();
        this.world.gameScreen.topGroup.addActor(fxGroup);
        fxGroup.setPosition(this.getXPos(), this.getYPos());
        fxGroup.addAction(Actions.sequence((Action)Actions.delay(3.0f), (Action)Actions.removeActor()));
        ParticleActor particleExplosion = new ParticleActor(Gdx.files.internal("particles/meteor-explosion.p"), this.world.gameScreen.atlas, true);
        particleExplosion.pfx.start();
        fxGroup.addActor(particleExplosion);
        ParticleActor fireParticle = new ParticleActor(Gdx.files.internal("particles/base-fire.p"), this.world.gameScreen.atlas, true);
        fireParticle.pfx.start();
        fxGroup.addActor(fireParticle);
        this.world.gameScreen.cameraShake.shake(10.0f, 60.0f, 0.4f);
        super.remove();
    }

    @Override
    public void setColor(int colorIndex) {
        if (colorIndex == 0) {
            this.imageBase.setDrawable(this.world.gameScreen.skin.getDrawable("trailer-base-default"));
        } else {
            this.imageBase.setDrawable(this.world.gameScreen.skin.getDrawable("trailer-base"));
        }
        Color c = Vars.vehicleColors[colorIndex];
        float tintStrength = 0.25f;
        float r = MathUtils.lerp(c.r, 1.0f, tintStrength);
        float g = MathUtils.lerp(c.g, 1.0f, tintStrength);
        float b = MathUtils.lerp(c.b, 1.0f, tintStrength);
        this.imageBase.setColor(new Color(r, g, b, 1.0f));
        this.paintColorIndex = colorIndex;
    }
}

