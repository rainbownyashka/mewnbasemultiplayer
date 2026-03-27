/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.entities.Creature;
import com.cairn4.moonbase.entities.LightAnimator;
import com.cairn4.moonbase.tiles.StorageColorOptions;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.ui.GameScreen;
import com.cairn4.moonbase.worlddata.DamageDef;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;

public class PlayerAnimController {
    Player player;
    Skeleton skeleton;
    AnimationState state;
    public WALK_DIRS walkDir;
    private static int walkTrack = 0;
    private static int interactTrack = 1;
    private static int emoteTrack = 2;
    private static int blinkTrack = 3;
    private static int attackTrack = 4;
    private static int suffocatingTrack = 9;
    private static int windyWalkTrack = 10;
    LightAnimator playerTabletLightAnimator;
    private Slot flashlightSlot;
    private Slot itemSlot;
    private Slot itemSlotAlt;
    private Slot painttoolColor;
    private Attachment paintbrushColorAttachment;
    private Slot paintBucketColor;
    private Attachment bucketColorAttachment;
    private Attachment bucketAttachment;
    private DamageDef damageDef;
    private AnimationState.AnimationStateAdapter footprintAnimListener;
    private boolean fireParticlesPlaying = false;
    // When a remote flip is enforced by network, keep it persistent so updateWalkDirection doesn't overwrite it
    private float forcedScale = Float.NaN;

    public PlayerAnimController(Player player) {
        this.player = player;
        this.skeleton = player.spineActor.skeleton;
        this.state = player.spineActor.state;
        this.playerTabletLightAnimator = new LightAnimator(player.world, player.getXPos(), player.getYPos());
        this.playerTabletLightAnimator.setLight(player.tabletLight);
        this.playerTabletLightAnimator.setAlpha(0.0f);
        this.flashlightSlot = this.skeleton.findSlot("player/player-headlamp");
        this.itemSlot = this.skeleton.findSlot("player/item_slot");
        this.itemSlotAlt = this.skeleton.findSlot("player/item_slot_left");
        this.itemSlotAlt.setAttachment(null);
        this.painttoolColor = this.skeleton.findSlot("player/paintbrush-color");
        this.painttoolColor.setAttachment(null);
        this.paintBucketColor = this.skeleton.findSlot("player/paintbucket-color");
        this.paintBucketColor.setAttachment(null);
        this.paintbrushColorAttachment = this.skeleton.getAttachment("player/paintbrush-color", "player/items/paintbrush-color");
        this.bucketAttachment = this.skeleton.getAttachment("player/item_slot_left", "player/items/paintbucket");
        this.bucketColorAttachment = this.skeleton.getAttachment("player/paintbucket-color", "player/items/paintbucket-color");
        this.damageDef = new DamageDef();
        this.damageDef.damageType = DamageDef.DamageTypes.physical;
    }

    public void interactAnim(final Tile tile) {
        this.faceDirection(tile.getXCenter(), tile.getYCenter());
        this.player.busyInteracting = true;
        String useAnimName = "interact";
        if (this.player.playerInventory.getEquippedItem() != null && tile.isValidInteractItem(this.player.getPlayerInventory().getEquippedItem().getId())) {
            useAnimName = ItemFactory.getUseAnimName(this.player.playerInventory.getEquippedItem().getId());
        }
        MoonBase.debug("PlayerAnimController: starting interaction: " + useAnimName);
        if (interactTrack < this.state.getTracks().size && this.state.getTracks().get(interactTrack) != null) {
            this.state.getTracks().get(interactTrack).setListener(null);
        }
        this.state.setAnimation(interactTrack, useAnimName, true);
        final Tile interactTile = tile;
        this.state.addListener(new AnimationState.AnimationStateAdapter(){

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                if (event.getData().getName().equals("hit")) {
                    tile.playInteractSound();
                    interactTile.doInteractPunchAnim();
                }
                if (event.getData().getName().equals("hitTool")) {
                    tile.playInteractSound();
                    interactTile.doInteractPunchAnim();
                    PlayerAnimController.this.player.getPlayerInventory().reduceToolDurability(1);
                }
                if (event.getData().getName().equals("jackhammer")) {
                    float pitch = MathUtils.random(0.95f, 1.15f);
                    Audio.getInstance().playSound("sounds/jackhammer.ogg", 0.7f, pitch);
                    interactTile.doInteractPunchAnim();
                    PlayerAnimController.this.player.getPlayerInventory().reduceToolDurability(1);
                }
            }

            @Override
            public void complete(AnimationState.TrackEntry entry) {
                super.complete(entry);
                PlayerAnimController.this.player.busyInteracting = false;
            }
        });
    }

    public void zapAnimation() {
        this.state.setAnimation(emoteTrack, "zap", false);
        this.state.addEmptyAnimation(emoteTrack, 0.0f, 0.0f);
    }

    private void faceDirection(float tileXCenter, float tileYCenter) {
        if (this.player.getXPos() < tileXCenter) {
            this.player.spineActor.setFlip(false, false);
        } else if (this.player.getXPos() > tileXCenter) {
            this.player.spineActor.setFlip(true, false);
        }
        float angle = new Vector2(tileXCenter, tileYCenter).sub(this.player.getXPos(), this.player.getYPos()).angle();
        this.player.setFlashlightDirection(angle);
    }

    public void clearInteractionAnim(boolean instantStop) {
        if (this.state.getCurrent(interactTrack) != null) {
            this.state.clearListeners();
            this.addFootstepSfxListener();
            if (instantStop) {
                this.state.clearTrack(interactTrack);
            } else {
                this.state.getCurrent(interactTrack).setLoop(false);
                this.state.addEmptyAnimation(interactTrack, 0.01f, 0.0f);
            }
        }
    }

    public void setStun() {
        this.player.attacking = false;
        MoonBase.log("Attacking = false");
        this.clearInteractionAnim(true);
        this.state.clearTrack(blinkTrack);
        this.state.setAnimation(emoteTrack, "stun", false);
        this.state.addEmptyAnimation(emoteTrack, 0.0f, 0.0f);
        this.setupBlink();
    }

    public void setupBlink() {
        this.state.addAnimation(blinkTrack, "blink", true, 4.5f);
    }

    public void attackSwing(Creature creature) {
        final Creature creatureToHit = creature;
        this.faceDirection(creatureToHit.getXPos(), creatureToHit.getYPos());
        this.state.clearListeners();
        this.addFootstepSfxListener();
        MoonBase.log("Attack Swing");
        this.clearInteractionAnim(true);
        this.state.setAnimation(attackTrack, "attack", false);
        this.state.addEmptyAnimation(attackTrack, 0.1f, 0.0f);
        this.state.addListener(new AnimationState.AnimationStateAdapter(){

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                if (event.getData().getName().equals("attackSwing")) {
                    float pitch = MathUtils.random(0.8f, 1.1f);
                    Audio.getInstance().playSound("sounds/shovel-swing.ogg", 0.3f, pitch);
                }
                if (event.getData().getName().equals("attackHit")) {
                    float dmg = 0.0f;
                    if (PlayerAnimController.this.player.playerInventory.hasWeaponEquipped()) {
                        ItemData id = ItemFactory.getItemData(PlayerAnimController.this.player.getPlayerInventory().getEquippedItem().getId());
                        dmg = MathUtils.random(id.minMeleeDamage, id.maxMeleeDamage);
                    } else {
                        dmg = MathUtils.random(PlayerAnimController.this.player.minMeleeDamage, PlayerAnimController.this.player.maxMeleeDamage);
                    }
                    creatureToHit.takeDamage(dmg, PlayerAnimController.this.damageDef);
                    float pitch2 = MathUtils.random(0.9f, 1.1f);
                    Audio.getInstance().playSound("sounds/shovel-hit.ogg", 0.4f, pitch2);
                }
            }
        });
    }

    public void paint() {
        this.state.setAnimation(emoteTrack, "interact", false);
        this.state.addEmptyAnimation(emoteTrack, 0.0f, 0.0f);
    }

    public void toolBrokeAnim() {
        this.state.setAnimation(emoteTrack, "toolbreak", false);
        this.state.addEmptyAnimation(emoteTrack, 0.0f, 0.0f);
    }

    public void showEquippedItem(ItemStack itemStack) {
        this.itemSlotAlt.setAttachment(null);
        this.paintBucketColor.setAttachment(null);
        this.painttoolColor.setAttachment(null);
        String itemid = "";
        if (itemStack != null) {
            itemid = itemStack.getId();
        }
        if (itemid.equals("shovel")) {
            Attachment a = this.skeleton.getAttachment("player/item_slot", "player/items/shovel1");
            this.itemSlot.setAttachment(a);
        } else if (itemid.equals("shovel2")) {
            Attachment a = this.skeleton.getAttachment("player/item_slot", "player/items/shovel2");
            this.itemSlot.setAttachment(a);
        } else if (itemid.equals("axe")) {
            Attachment a = this.skeleton.getAttachment("player/item_slot", "player/items/axe");
            this.itemSlot.setAttachment(a);
        } else if (itemid.equals("jackhammer")) {
            Attachment a = this.skeleton.getAttachment("player/item_slot", "player/items/jackhammer");
            this.itemSlot.setAttachment(a);
        } else if (itemid.equals("pickaxe")) {
            Attachment a = this.skeleton.getAttachment("player/item_slot", "player/items/pickaxe");
            this.itemSlot.setAttachment(a);
        } else if (itemid.equals("paintbrush")) {
            Attachment a = this.skeleton.getAttachment("player/item_slot", "player/items/paintbrush");
            this.itemSlot.setAttachment(a);
            this.painttoolColor.getColor().set(StorageColorOptions.colors[this.player.getPaintColorIndex()]);
            this.painttoolColor.setAttachment(this.paintbrushColorAttachment);
            this.itemSlotAlt.setAttachment(this.bucketAttachment);
            this.paintBucketColor.setAttachment(this.bucketColorAttachment);
            this.paintBucketColor.getColor().set(StorageColorOptions.colors[this.player.getPaintColorIndex()]);
        } else {
            this.itemSlot.setAttachment(null);
        }
    }

    public void updatePainttoolColor(Color newColor) {
        this.painttoolColor.getColor().set(newColor);
        this.paintBucketColor.getColor().set(newColor);
    }

    public void setFlashlight(boolean on) {
        this.flashlightSlot.getColor().a = on ? 1.0f : 0.0f;
    }

    private WALK_DIRS getWalkDir(float dX, float dY) {
        if (dX == 0.0f && dY > 0.0f) {
            return WALK_DIRS.up;
        }
        if (dX == 0.0f && dY < 0.0f) {
            return WALK_DIRS.down;
        }
        if (dX < 0.0f && dY == 0.0f) {
            return WALK_DIRS.left;
        }
        if (dX > 0.0f && dY == 0.0f) {
            return WALK_DIRS.right;
        }
        if (dX < 0.0f && dY > 0.0f) {
            return WALK_DIRS.upLeft;
        }
        if (dX > 0.0f && dY > 0.0f) {
            return WALK_DIRS.upRight;
        }
        if (dX < 0.0f && dY < 0.0f) {
            return WALK_DIRS.downLeft;
        }
        if (dX > 0.0f && dY < 0.0f) {
            return WALK_DIRS.downRight;
        }
        return WALK_DIRS.idle;
    }

    public void setDirection(float dX, float dY) {
        WALK_DIRS newWalk = this.getWalkDir(dX, dY);
        if (this.walkDir != newWalk) {
            // determine scaleX based on new walk direction (flip horizontally for left-facing dirs)
            float scale = 1.0f;
            if (newWalk == WALK_DIRS.left || newWalk == WALK_DIRS.upLeft || newWalk == WALK_DIRS.downLeft) {
                scale = -1.0f;
            }
            if ((this.walkDir == WALK_DIRS.idle || this.walkDir == null) && newWalk != WALK_DIRS.idle) {
                this.changeWalk("walk_right", scale);
                this.state.clearListeners();
                this.addFootstepSfxListener();
            }
            this.walkDir = newWalk;
            if (this.walkDir == WALK_DIRS.idle) {
                // keep existing scale when going idle
                float curScale = 1.0f;
                try { curScale = this.skeleton.getScaleX(); } catch (Exception ignored) {}
                this.changeWalk("idle", curScale);
            } else {
                this.updateWalkDirection(this.walkDir);
            }
        }
    }

    public void addFootstepSfxListener() {
        this.state.addListener(new AnimationState.AnimationStateAdapter(){

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
                if (event.getData().getName().equals("footstep")) {
                    PlayerAnimController.this.footStepSoundFx(event);
                }
            }
        });
    }

    public void updateWalkDirection(float dX, float dY) {
        WALK_DIRS newWalk = this.getWalkDir(dX, dY);
        this.updateWalkDirection(newWalk);
    }

    public void updateWalkDirection(WALK_DIRS walkDir) {
        switch (walkDir) {
            case up: {
                break;
            }
            case down: {
                break;
            }
            case left: {
                this.skeleton.setScaleX(-1.0f);
                break;
            }
            case right: {
                this.skeleton.setScaleX(1.0f);
                break;
            }
            case upLeft: {
                this.skeleton.setScaleX(-1.0f);
                break;
            }
            case upRight: {
                this.skeleton.setScaleX(1.0f);
                break;
            }
            case downLeft: {
                this.skeleton.setScaleX(-1.0f);
                break;
            }
            case downRight: {
                this.skeleton.setScaleX(1.0f);
            }
        }
        // Re-apply any network-enforced forcedScale so local updates don't overwrite it
        try { this.applyForcedScale(); } catch (Exception ignored) {}
    }

    private void footStepSoundFx(Event event) {
        float pitch = MathUtils.random(0.95f, 1.05f);
        Tile t = this.player.world.getTile(this.player.getX(), this.player.getY());
        if (t != null && t.type == Tile.types.habitat) {
            Audio.getInstance().playSound("sounds/footstep_indoors" + event.getInt() + ".ogg", 0.62f, pitch);
        } else {
            String sfx = null;
            Tile floor = this.player.world.getFloorTile(this.player.getX(), this.player.getY());
            if (floor != null) {
                sfx = floor.getWalkSoundFx();
            }
            if (sfx != null) {
                Audio.getInstance().playSound(sfx + ".ogg", 0.4f, pitch += event.getInt() == 1 ? -0.2f : 0.2f);
            } else {
                Audio.getInstance().playSound("sounds/footstep" + event.getInt() + ".ogg", 0.4f, pitch);
            }
        }
    }

    public void updateWindyWeatherPose(float windSpeed, float walkSpeedMul) {
        if (windSpeed > 0.9f && !this.player.isIndoors() && this.player.interactTile == null) {
            if (this.state.getCurrent(windyWalkTrack) == null) {
                this.state.setAnimation(10, "shield_eyes", true);
            }
            this.state.getCurrent(windyWalkTrack).setAlpha(0.9f);
            this.state.getCurrent(0).setTimeScale(0.66f * walkSpeedMul);
        } else {
            this.state.clearTrack(windyWalkTrack);
            this.state.getCurrent(0).setTimeScale(walkSpeedMul);
        }
    }

    private void changeWalk(String animName) {
        this.changeWalk(animName, this.skeleton.getScaleX());
    }

    // Overloaded changeWalk that accepts scaleX so remote players can mirror flip direction
    private void changeWalk(String animName, float scaleX) {
        this.state.setAnimation(walkTrack, animName, true);
        // Apply forcedScale if present so local visuals respect network-enforced flip
        try { this.applyForcedScale(); } catch (Exception ignored) {}
        // Broadcast walk animation changes to other peers so remote players can mirror walking/idle states.
        try {
            if (this.player != null && this.player.world != null && this.player.world.gameScreen != null) {
                try {
                    com.cairn4.moonbase.ui.GameScreen gs = this.player.world.gameScreen;
                    boolean hasNet = false;
                    try { hasNet = (gs.client != null) || (com.cairn4.moonbase.Server.getActiveServer() != null); } catch (Exception ignored) {}
                    if (!hasNet) {
                        return;
                    }
                    int owner = this.player.ownerId;
                    // decide effective scale to send: prefer forcedScale if set
                    float effectiveScale = !Float.isNaN(this.forcedScale) ? this.forcedScale : scaleX;
                    String flipOverride = "FLIP_OVERRIDE:PLAYER:" + owner + ":" + effectiveScale;
                    String flipPayload = "FLIP:PLAYER:" + owner + ":" + effectiveScale;
                    String animPayload = "ANIMPLAY:PLAYER:" + owner + ":" + animName + ":true:" + effectiveScale;
                    com.cairn4.moonbase.NetworkHelper.sendPayload(gs, flipOverride);
                    com.cairn4.moonbase.NetworkHelper.sendPayload(gs, flipPayload);
                    com.cairn4.moonbase.NetworkHelper.sendPayload(gs, animPayload);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    // Allow external code (client/server receiver) to enforce a persistent skeleton scale
    public void setForcedScale(float s) {
        this.forcedScale = s;
        try { this.applyForcedScale(); } catch (Exception ignored) {}
    }

    private void applyForcedScale() {
        if (!Float.isNaN(this.forcedScale)) {
            try {
                this.skeleton.setScaleX(this.forcedScale);
            } catch (Exception ignored) {}
        }
    }

    public void hungryAnim() {
        this.state.setAnimation(emoteTrack, "hungry", false);
        this.state.addEmptyAnimation(emoteTrack, 0.0f, 0.0f);
        float pitch = MathUtils.random(0.9f, 1.0f);
        Audio.getInstance().playSound("sounds/hungry.ogg", 1.0f, pitch);
    }

    void eat() {
        this.itemSlotAlt.setAttachment(null);
        this.paintBucketColor.setAttachment(null);
        this.painttoolColor.setAttachment(null);
        this.state.setAnimation(emoteTrack, "eat", false);
        this.state.addEmptyAnimation(emoteTrack, 0.0f, 0.0f);
        float randomVol = MathUtils.random(0.8f, 0.9f);
        float randomPitch = MathUtils.random(0.95f, 1.1f);
        Audio.getInstance().playSound("sounds/food_chew.ogg", randomVol, randomPitch);
        this.player.eatParticles.pfx.reset();
        this.player.eatParticles.pfx.start();
    }

    public void setOnFire(boolean onFire) {
        if (!this.fireParticlesPlaying && onFire) {
            this.player.fireParticles.pfx.start();
            this.fireParticlesPlaying = true;
        } else if (this.fireParticlesPlaying && !onFire) {
            this.player.fireParticles.pfx.allowCompletion();
            this.fireParticlesPlaying = false;
        }
    }

    public void openTablet() {
        this.player.world.gameScreen.inputMode = GameScreen.inputModes.menu;
        this.state.setAnimation(emoteTrack, "openTablet", false);
        this.playerTabletLightAnimator.setAlpha(0.0f);
        this.playerTabletLightAnimator.animFade(1.0f, 0.5f);
        this.playerTabletLightAnimator.animDistance(10.0f, 60.0f, 0.5f);
    }

    public void closeTablet() {
        this.state.setAnimation(emoteTrack, "closeTablet", false);
        this.state.addEmptyAnimation(emoteTrack, 0.0f, 0.0f);
        this.state.clearTrack(emoteTrack);
        this.showEquippedItem(this.player.playerInventory.getEquippedItem());
        this.playerTabletLightAnimator.animFade(0.0f, 0.25f);
        this.playerTabletLightAnimator.animDistance(60.0f, 10.0f, 0.25f);
    }

    public void deathAnim() {
        this.skeleton.setScaleX(1.0f);
        this.skeleton.setBonesToSetupPose();
        this.state.clearTracks();
        this.state.clearListeners();
        this.state.setAnimation(0, "death", false);
    }

    public void testAnim(String name, boolean loop) {
        this.skeleton.setScaleX(1.0f);
        this.skeleton.setBonesToSetupPose();
        this.state.setAnimation(0, name, loop);
    }

    public void suffocateAnim(boolean suffocating) {
        if (suffocating) {
            this.state.setAnimation(suffocatingTrack, "suffocateOn", false);
        } else {
            this.state.setAnimation(suffocatingTrack, "suffocateOff", false);
        }
    }

    public static enum WALK_DIRS {
        idle,
        up,
        down,
        left,
        right,
        upLeft,
        upRight,
        downLeft,
        downRight;

    }
}
