/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.HealingCooldownStatusEffect;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.behaviors.MedBaySpriteBehavior;
import com.cairn4.moonbase.ui.Localization;

public class MedBay
extends HabitatModule {
    Image medbayImage;
    Image pulseImage;
    MedBaySpriteBehavior medBaySpriteBehavior;

    public MedBay(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerPriority = 3;
        this.powerDrawRate = 3.0f;
        this.medbayImage = new Image(world.gameScreen.skin.getDrawable("medbay"));
        this.medbayImage.setSize(TILE_SIZE * 0.7f, TILE_SIZE * 0.7f);
        this.medbayImage.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f, 1);
        this.componentGroup.addActor(this.medbayImage);
        this.interactDuration = 2.5f;
        this.pulseImage = new Image(world.gameScreen.skin.getDrawable("medbay-pulse-0"));
        this.pulseImage.setSize(32.0f, 24.0f);
        this.pulseImage.setPosition(TILE_SIZE / 2.0f + 20.0f, TILE_SIZE / 2.0f + 25.0f, 1);
        this.componentGroup.addActor(this.pulseImage);
        this.medBaySpriteBehavior = new MedBaySpriteBehavior();
        this.medBaySpriteBehavior.pulseImage = this.pulseImage;
        this.medBaySpriteBehavior.gameScreeSkin = world.gameScreen.skin;
        this.medBaySpriteBehavior.baseModule = this;
        this.behaviorList.add(this.medBaySpriteBehavior);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "medbay";
    }

    @Override
    public void finishInteraction(Player player) {
        if (!player.playerStatus.hasStatusEffectType(HealingCooldownStatusEffect.class) && this.hasPower) {
            player.playerStatus.setHealthFull();
            player.playerStatus.newStatusEffect(new HealingCooldownStatusEffect());
            this.world.gameScreen.hud.hudNotifications.newMessage(Localization.get("medbay_interact"));
        }
        super.finishInteraction(player);
    }

    @Override
    public void setPower(boolean on) {
        if (this.hasPower != on && this.pulseImage != null) {
            if (!this.hasPower) {
                this.animateColor(this.medbayImage, Color.WHITE, 0.25f);
                this.animateColor(this.pulseImage, Color.WHITE, 0.25f);
            } else {
                this.animateColor(this.pulseImage, Color.BLACK, 0.25f);
                this.animateColor(this.medbayImage, POWER_OFF_COLOR, 0.25f);
            }
        }
        super.setPower(on);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }
}

