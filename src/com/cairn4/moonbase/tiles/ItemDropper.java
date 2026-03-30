/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Item;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.SpineActor;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.ItemPickup;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.CooldownTimer;
import com.cairn4.moonbase.tiles.behaviors.HarvestCallback;
import com.cairn4.moonbase.tiles.behaviors.HarvestableBehavior;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.worlddata.InventoryItemData;
import com.cairn4.moonbase.worlddata.ItemDropperData;
import com.cairn4.moonbase.worlddata.ItemDropperFactory;
import com.cairn4.moonbase.worlddata.ItemDropperItemData;
import com.cairn4.moonbase.worlddata.TileInteractData;
import com.cairn4.moonbase.worlddata.TileInteractionAction;
import com.esotericsoftware.spine.AnimationState;

public class ItemDropper
extends Tile {
    boolean flipped;
    public SpineActor spineActor;
    public ItemDropperData rdData;
    private int randomSprite;
    private int randomRegrowSprite;
    private boolean regrowing;
    CooldownTimer cooldownTimerBehavior;
    CooldownTimer transformCooldownTimer;
    public HarvestableBehavior harvestableBehavior;
    public TileInteractData currentPlayerInteractionData;
    public TileInteractData harvestInteractionData;
    public PointLight light;
    private boolean usingToolToCollect = false;
    private static String harvesterId = "harvester";

    public ItemDropper(World world, Chunk chunk, int x, int y, String itemDropperId) {
        this(world, chunk, x, y, ItemDropperFactory.getInstance().getItemDropperData(itemDropperId));
    }

    public ItemDropper(World world, Chunk chunk, int x, int y, ItemDropperData rdData) {
        super(world, chunk, x, y);
        this.rdData = rdData;
        this.interactDuration = 1.0f;
        for (TileInteractData tid : this.rdData.interactWith) {
            this.validInteractItemIds.addAll(tid.equippedIds);
        }
        this.setupImage();
        this.harvestableBehavior = new HarvestableBehavior();
        this.harvestableBehavior.health = 1.0f;
        this.harvestableBehavior.harvestCallback = new HarvestCallback(){

            @Override
            public void harvestFinished() {
                ItemDropper.this.dropHarvestLoot();
            }
        };
        this.behaviorList.add(this.harvestableBehavior);
        if (rdData.regrowTime != 0.0f) {
            this.cooldownTimerBehavior = new CooldownTimer();
            this.cooldownTimerBehavior.length = rdData.regrowTime;
            this.cooldownTimerBehavior.start();
            this.behaviorList.add(this.cooldownTimerBehavior);
        }
        if (rdData.transformNewId != null && !rdData.transformNewId.equals("") && rdData.transformTime > 0.0f) {
            this.transformCooldownTimer = new CooldownTimer();
            this.transformCooldownTimer.length = rdData.transformTime;
            this.transformCooldownTimer.trigger();
            this.behaviorList.add(this.transformCooldownTimer);
        }
        if (this.rdData.physicsBody != null) {
            if (this.rdData.physicsSensor) {
                this.setupPhysics(this.rdData.physicsBody, 0.5f, true);
                this.walkable = true;
            } else {
                this.setupPhysics(this.rdData.physicsBody);
            }
        } else {
            this.walkable = true;
        }
        if (this.rdData.lightColor != null) {
            this.setupLight();
        }
    }

    @Override
    public String getMapName() {
        return Localization.get(this.rdData.locid);
    }

    @Override
    public void startBehaviors() {
        super.startBehaviors();
        if (this.cooldownTimerBehavior != null) {
            if (!this.cooldownTimerBehavior.isReady()) {
                this.setupRegrowingImage();
                this.regrowing = true;
            } else {
                this.regrowing = false;
            }
        }
    }

    public void setupLight() {
        Color color = Color.valueOf("ffffffff");
        try {
            color.set(Color.valueOf(this.rdData.lightColor));
        }
        catch (Exception e) {
            Gdx.app.error("MewnBase", "ItemDropper: error parsing hex color");
        }
        this.light = new PointLight(this.world.rayHandler, 12, color, this.rdData.lightRadius / 256.0f, this.getXCenter() / 256.0f, this.getYCenter() / 256.0f);
        this.light.setXray(true);
    }

    private Group getStageGroup() {
        Group g = this.world.gameScreen.mainGroup;
        if (!this.rdData.zSort) {
            g = this.world.gameScreen.floorGroup;
        }
        return g;
    }

    private void setupRegrowingImage() {
        if (this.rdData.spineRegrowAnim == "" || this.rdData.spineRegrowAnim == null) {
            this.randomRegrowSprite = 0;
            if (this.rdData.regrowSprites.size() > 1) {
                this.randomRegrowSprite = MathUtils.random(0, this.rdData.regrowSprites.size() - 1);
            }
            this.createDrawables(this.rdData.regrowSprites.get(this.randomRegrowSprite), this.getStageGroup());
            if (this.flipped) {
                this.image.setOrigin(1);
                this.image.setScaleX(-1.0f);
            }
        } else if (this.spineActor != null) {
            this.spineActor.state.setAnimation(0, this.rdData.spineRegrowAnim, true);
        }
    }

    protected void setupImage() {
        if (this.rdData.spinePath == "" || this.rdData.spinePath == null) {
            this.randomSprite = 0;
            if (this.rdData.sprites.size() > 1) {
                this.randomSprite = MathUtils.random(0, this.rdData.sprites.size() - 1);
            }
            this.createDrawables(this.rdData.sprites.get(this.randomSprite), this.getStageGroup());
            this.group.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + TILE_SIZE / 3.0f + this.rdData.zSortOffset));
            this.image.setOrigin(4);
            if (this.rdData.offset != null) {
                this.image.moveBy(this.rdData.offset.x, this.rdData.offset.y);
            }
            float scale = 1.0f;
            if (this.rdData.minScale > 0.0f && this.rdData.maxScale > 0.0f) {
                scale = MathUtils.random(this.rdData.minScale, this.rdData.maxScale);
            }
            if (MathUtils.randomBoolean()) {
                this.image.setOrigin(1);
                this.flipped = true;
                this.image.setScaleX(-1.0f * scale);
                this.image.setScaleY(scale);
            }
        } else {
            this.group.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + TILE_SIZE / 3.0f + this.rdData.zSortOffset));
            this.setupSpineActor();
        }
    }

    public void setupSpineActor() {
        this.group.setPosition(this.getWorldXPos(), this.getWorldYPos());
        this.world.gameScreen.mainGroup.addActor(this.group);
        this.group.clearChildren();
        if (this.spineActor != null) {
            this.spineActor.remove();
        }
        this.spineActor = new SpineActor("item_droppers/" + this.rdData.spinePath, this.rdData.spineBaseScale, this.world.gameScreen.skeletonRenderer);
        this.spineActor.setPosition(TILE_SIZE / 2.0f, 0.0f);
        this.group.addActor(this.spineActor);
        if (this.rdData.sprites != null) {
            this.spineActor.setSlotAttachment(this.rdData.spinePath, this.randomSprite());
        }
        if (MathUtils.randomBoolean()) {
            this.spineActor.setFlip(true, false);
        }
        try {
            for (String anim : this.rdData.spineAnimations) {
                this.spineActor.addAnimation(0, anim, true, 0.0f);
            }
        }
        catch (Exception e) {
            MoonBase.error("Item dropper [" + this.rdData.id + "] has a spine rig with no animations set in json file!");
        }
        final AnimationState fState = this.spineActor.state;
        this.spineActor.state.addListener(new AnimationState.AnimationStateAdapter(){

            public void complete(int trackIndex, int loopCount) {
                String animName = ItemDropper.this.rdData.spineAnimations.get(MathUtils.random(0, ItemDropper.this.rdData.spineAnimations.size() - 1));
                fState.setAnimation(0, animName, true);
            }
        });
    }

    private String randomSprite() {
        return this.rdData.sprites.get(MathUtils.random(0, this.rdData.sprites.size() - 1));
    }

    @Override
    public Color getMinimapColor() {
        if (!this.rdData.minimapHexColor.equals("")) {
            try {
                return Color.valueOf(this.rdData.minimapHexColor);
            }
            catch (Exception e) {
                Gdx.app.error("MewnBase", "Invalid map hex color in resources_tiles.json");
                return Color.RED;
            }
        }
        return Color.WHITE;
    }

    @Override
    public void update(float delta) {
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
        if (this.cooldownTimerBehavior != null) {
            if (this.regrowing && this.cooldownTimerBehavior.isReady()) {
                this.setupImage();
                this.regrowing = false;
            }
            if (!this.regrowing && !this.cooldownTimerBehavior.isReady()) {
                this.setupImage();
                this.regrowing = true;
            }
        }
        if (this.transformCooldownTimer != null && this.transformCooldownTimer.isReady()) {
            this.transformTile();
        }
        if (this.group.isVisible() && this.spineActor != null) {
            this.spineActor.update(delta);
        }
    }

    private void transformTile() {
        this.destroy();
        ItemDropperFactory.getInstance().newDropper(this.world, this.chunk, this.x, this.y, this.rdData.transformNewId);
        Gdx.app.log("MewnBase", "ItemDropper: transforming into " + this.rdData.transformNewId);
    }

    @Override
    public boolean canInteract(Player player) {
        boolean canPickup = false;
        boolean cantPickupWhenRegrowing = false;
        this.usingToolToCollect = false;
        ItemStack equippedItem = player.getPlayerInventory().getEquippedItem();
        if (equippedItem != null) {
            String equippedItemId = equippedItem.getId();
            block0: for (TileInteractData tid : this.rdData.interactWith) {
                for (String validId : tid.equippedIds) {
                    if (!validId.equals(equippedItemId)) continue;
                    if (this.regrowing) {
                        if (!tid.disabledWhenGrowing) {
                            canPickup = true;
                            this.usingToolToCollect = true;
                            this.currentPlayerInteractionData = tid;
                            this.setInteractionDuration(tid.interactionDuration);
                            continue block0;
                        }
                        cantPickupWhenRegrowing = true;
                        continue;
                    }
                    canPickup = true;
                    this.usingToolToCollect = true;
                    this.currentPlayerInteractionData = tid;
                    this.setInteractionDuration(tid.interactionDuration);
                    continue block0;
                }
            }
        }
        if (!canPickup) {
            for (TileInteractData tid : this.rdData.interactWith) {
                if (!tid.withAnyItem) continue;
                if (this.regrowing) {
                    if (!tid.disabledWhenGrowing) {
                        canPickup = true;
                        this.currentPlayerInteractionData = tid;
                        this.setInteractionDuration(tid.interactionDuration);
                        break;
                    }
                    cantPickupWhenRegrowing = true;
                    continue;
                }
                canPickup = true;
                this.currentPlayerInteractionData = tid;
                this.setInteractionDuration(tid.interactionDuration);
                break;
            }
        }
        if (!canPickup) {
            String spriteName = null;
            if (this.rdData.sprites != null && this.rdData.sprites.size() > 0) {
                spriteName = this.rdData.sprites.get(this.randomSprite);
            }
            if (cantPickupWhenRegrowing) {
                this.world.gameScreen.hud.hudNotifications.newMessage(spriteName, Localization.get("cantPickUpRegrowing"));
            } else {
                this.world.gameScreen.hud.hudNotifications.newMessage(spriteName, Localization.get("cantPickUpNeedItem"));
            }
        }
        return canPickup;
    }

    public boolean canHarvest() {
        boolean canHarvest = false;
        block0: for (TileInteractData tid : this.rdData.interactWith) {
            for (String validId : tid.equippedIds) {
                if (!validId.equals(harvesterId)) continue;
                canHarvest = true;
                this.harvestInteractionData = tid;
                continue block0;
            }
        }
        return canHarvest;
    }

    @Override
    public void interact(Player player) {
        this.harvestInteractionData = null;
    }

    @Override
    protected void finishInteraction(Player player) {
        if (this.currentPlayerInteractionData != null) {
            if (this.usingToolToCollect) {
                player.getPlayerInventory().checkToolDurability();
            }
            this.handleActions(this.currentPlayerInteractionData);
        }
        super.finishInteraction(player);
    }

    private void handleActions(TileInteractData tid) {
        Object action = null;
        for (TileInteractionAction a : tid.actions) {
            if (a.type.equals("lootDrop")) {
                Gdx.app.log("MewnBase", "ItemDropper: Dropping loot action");
                if (!this.regrowing || this.regrowing && !a.onlyIfNotRegrowing) {
                    this.dropItems(a);
                }
                if (this.rdData.regrowTime == 0.0f) {
                    this.respawn();
                    this.readyToRemove = true;
                } else {
                    this.regrowing = true;
                    this.cooldownTimerBehavior.trigger();
                    if (this.rdData.regrowTime != 0.0f) {
                        this.setupRegrowingImage();
                    }
                }
            }
            if (!a.type.equals("destroyTile")) continue;
            this.respawn();
            this.readyToRemove = true;
        }
        this.currentPlayerInteractionData = null;
    }

    public void dropHarvestLoot() {
        for (TileInteractionAction a : this.harvestInteractionData.actions) {
            a.type = "lootDrop";
            if (!this.regrowing || this.regrowing && !a.onlyIfNotRegrowing) {
                this.dropItems(a);
            }
            if (this.rdData.regrowTime != 0.0f) continue;
            this.respawn();
        }
        this.readyToRemove = true;
    }

    public void respawn() {
        if (this.rdData.daysToRespawn > 0.0f || this.rdData.respawnId != null) {
            if (this.rdData.respawnId != null) {
                if (!this.rdData.respawnId.equals("")) {
                    this.world.regrowthManager.addRespawnable(this.rdData.respawnId, this.worldX, this.worldY);
                }
            } else {
                this.world.regrowthManager.addRespawnable(this.rdData.id, this.worldX, this.worldY);
            }
        }
    }

    public void dropItems(TileInteractionAction action) {
        for (InventoryItemData specialDrop : action.guaranteedDrops) {
            for (int i = 0; i < specialDrop.amount; ++i) {
                Item newItem = ItemFactory.getInstance().createItem(specialDrop.itemId);
                new ItemPickup(this.world, this.chunk, this.getWorldXPos() + TILE_SIZE / 2.0f, this.getWorldYPos() + TILE_SIZE / 2.0f, newItem);
            }
        }
        int quantity = MathUtils.random(action.minDrop, action.maxDrop);
        block2: for (int i = 0; i < quantity; ++i) {
            float base = 0.0f;
            float rand = MathUtils.random();
            int itemIndex = 0;
            for (ItemDropperItemData item : action.itemList) {
                if (rand > base && rand <= item.dropChance + base) {
                    Item newItem = ItemFactory.getInstance().createItem(item.itemId);
                    new ItemPickup(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos(), newItem);
                    continue block2;
                }
                base += action.itemList.get((int)itemIndex).dropChance;
                ++itemIndex;
            }
        }
        this.dropFx();
    }

    protected void dropFx() {
        ParticleActor dropParticles;
        if (this.currentPlayerInteractionData != null) {
            if (this.currentPlayerInteractionData.dropParticleSprite != null) {
                ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/item-dropper.p", ParticleEffect.class));
                dropParticles = new ParticleActor(p, true);
                Array<Sprite> spriteArray = new Array<Sprite>();
                spriteArray.add(new Sprite(this.world.gameScreen.atlas.findRegion(this.currentPlayerInteractionData.dropParticleSprite)));
                dropParticles.pfx.findEmitter("dropper").setSprites(spriteArray);
            } else {
                ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/item-dropper-generic.p", ParticleEffect.class));
                dropParticles = new ParticleActor(p, true);
            }
        } else if (this.harvestInteractionData != null) {
            if (this.harvestInteractionData.dropParticleSprite != null) {
                ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/item-dropper.p", ParticleEffect.class));
                dropParticles = new ParticleActor(p, true);
                Array<Sprite> spriteArray = new Array<Sprite>();
                spriteArray.add(new Sprite(this.world.gameScreen.atlas.findRegion(this.harvestInteractionData.dropParticleSprite)));
                dropParticles.pfx.findEmitter("dropper").setSprites(spriteArray);
            } else {
                ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/item-dropper-generic.p", ParticleEffect.class));
                dropParticles = new ParticleActor(p, true);
            }
        } else {
            ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/item-dropper-generic.p", ParticleEffect.class));
            dropParticles = new ParticleActor(p, true);
        }
        this.world.gameScreen.topGroup.addActor(dropParticles);
        dropParticles.setPosition(this.getXCenter(), this.getYCenter() + 5.0f);
        dropParticles.pfx.start();
    }

    @Override
    public void doInteractPunchAnim() {
        if (this.currentPlayerInteractionData != null && this.currentPlayerInteractionData.dropParticleSprite != null) {
            ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get("particles/item-dropper-hit.p", ParticleEffect.class));
            ParticleActor punchParticles = new ParticleActor(p, true);
            Array<Sprite> spriteArray = new Array<Sprite>();
            spriteArray.add(new Sprite(this.world.gameScreen.atlas.findRegion(this.currentPlayerInteractionData.dropParticleSprite)));
            punchParticles.pfx.findEmitter("dropper").setSprites(spriteArray);
            this.world.gameScreen.topGroup.addActor(punchParticles);
            punchParticles.setPosition(this.getXCenter(), this.getYCenter() + 5.0f);
            punchParticles.pfx.start();
        }
        this.group.addAction(Actions.parallel((Action)Actions.sequence((Action)Actions.scaleTo(0.95f, 0.95f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.08f))));
    }

    @Override
    public void playInteractSound() {
        if (!this.rdData.interactSounds.isEmpty()) {
            float pitch = MathUtils.random(0.95f, 1.05f);
            int randSound = MathUtils.random(0, this.rdData.interactSounds.size() - 1);
            Audio.getInstance().playSound(this.rdData.interactSounds.get(randSound), 0.7f, pitch);
        } else {
            super.playInteractSound();
        }
    }

    @Override
    public void destroy() {
        try {
            if (this.light != null) {
                this.light.remove();
            }
        }
        catch (NullPointerException npe) {
            MoonBase.error("Error removing light for item dropper: " + this.rdData.id);
        }
        super.destroy();
    }

    public void startHarvest() {
        if (!this.harvestableBehavior.harvesting) {
            this.group.clearActions();
            this.group.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(0.95f, 0.95f, 0.07f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.07f))));
        }
        this.harvestableBehavior.startHarvesting();
    }

    public void stopHarvesting() {
        this.harvestableBehavior.stopHarvesting();
        this.group.clearActions();
        this.group.setScale(1.0f, 1.0f);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            this.world.gameScreen.addToStage(this.group, this.getStageGroup());
            this.group.setVisible(true);
        } else {
            this.group.remove();
            this.group.setVisible(false);
        }
        if (this.hasPhysicsBody()) {
            if (visible) {
                this.body.setActive(true);
            } else {
                this.body.setActive(false);
            }
        }
    }
}
