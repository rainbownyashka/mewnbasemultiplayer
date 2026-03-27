/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AreaDamage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.worlddata.DamageDef;

public class Explosive
extends BaseModule {
    private static final String particleFxAsset = "particles/meteor-explosion.p";
    private float timer = 4.0f;
    private Image radiusImage;
    private static final float DAMAGE = 100.0f;
    private static final float RADIUS = Tile.TILE_SIZE * 3.0f;
    private DamageDef damageDef;

    public Explosive(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.radiusImage = new AdditiveImage(world.gameScreen.skin.getDrawable("target-circle"));
        world.gameScreen.worldUiGroup.addActor(this.radiusImage);
        this.radiusImage.setSize(RADIUS * 2.0f, RADIUS * 2.0f);
        this.radiusImage.setColor(1.0f, 0.0f, 0.0f, 0.25f);
        this.radiusImage.setPosition(this.getXCenter(), this.getYCenter(), 1);
        this.damageDef = new DamageDef();
        this.damageDef.damageType = DamageDef.DamageTypes.explosive;
    }

    @Override
    public void createDrawables() {
        super.createDrawables("tnt", this.world.gameScreen.mainGroup);
        this.image.addAction(Actions.sequence((Action)Actions.scaleTo(0.6f, 0.6f, 4.0f)));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.timer -= delta;
        if (this.timer <= 0.0f) {
            MoonBase.log("Boom!");
            this.radiusImage.remove();
            this.explosionFx();
            AreaDamage.damage(this.world, this.getXCenter(), this.getYCenter(), RADIUS, 100.0f, this.damageDef);
            this.readyToRemove = true;
        }
    }

    @Override
    public boolean usesBaseGroup() {
        return false;
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    public boolean visuallyConnectToConduits() {
        return false;
    }

    @Override
    public boolean blocksWind() {
        return false;
    }

    @Override
    public void interact(Player player) {
    }

    @Override
    public boolean canInteract(Player player) {
        return false;
    }

    private void explosionFx() {
        ParticleEffect p = new ParticleEffect(AssetManagerSingleton.getInstance().get(particleFxAsset, ParticleEffect.class));
        ParticleActor dropParticles = new ParticleActor(p, true);
        this.world.gameScreen.topGroup.addActor(dropParticles);
        dropParticles.setPosition(this.getXCenter(), this.getYCenter());
        dropParticles.pfx.start();
    }
}

