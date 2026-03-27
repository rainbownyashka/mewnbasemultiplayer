/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.disasters;

import box2dLight.PointLight;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.BaseResources;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.Damageable;
import com.cairn4.moonbase.tiles.disasters.BaseDisaster;
import com.cairn4.moonbase.worlddata.DamageDef;
import java.util.ArrayList;
import java.util.Collections;

public class BaseFire
extends BaseDisaster {
    ParticleActor fireParticle;
    Sound sound;
    Long soundId;
    PointLight glowLight;
    public static float OXYGEN_RATE = 1.0f;
    public static float SPREAD_SPEED = 5.0f;
    public int spreadTiles = 2;
    public float spreadTimer;
    private DamageDef damageDef;

    public BaseFire(BaseModule base) {
        super(base);
        Audio.getInstance().playSound("sounds/firestart.ogg", 0.35f);
        this.fireParticle = new ParticleActor(Gdx.files.internal("particles/base-fire.p"), base.world.gameScreen.atlas, true);
        this.fireParticle.pfx.start();
        base.world.gameScreen.topGroup.addActor(this.fireParticle);
        this.fireParticle.setPosition(base.getXCenter(), base.getYCenter());
        this.glowLight = new PointLight(base.world.rayHandler, 16, new Color(1.0f, 0.3f, 0.2f, 0.5f), 1.171875f, base.getXCenter() / 256.0f, base.getYCenter() / 256.0f);
        this.glowLight.setSoftnessLength(0.0390625f);
        this.glowLight.setXray(true);
        this.glowLight.setStaticLight(true);
        this.spreadTimer = 0.0f;
        this.damageDef = new DamageDef();
        this.damageDef.damageType = DamageDef.DamageTypes.fire;
    }

    @Override
    public void update(float delta) {
        Behavior callback;
        super.update(delta);
        if (this.base instanceof HabitatModule) {
            this.base.getBaseGroup().consumeResource(OXYGEN_RATE * delta, BaseResources.oxygen, Float.valueOf(this.base.getBaseGroup().getTotalOxygenStored()));
        }
        this.spreadTimer += delta;
        if (this.spreadTimer > SPREAD_SPEED && this.spreadTiles > 0) {
            this.spreadTimer = 0.0f;
            this.spread();
        }
        if ((callback = this.base.findBehavior(Damageable.class)) != null && callback instanceof Damageable) {
            ((Damageable)callback).takeDamage(1.0f * delta, this.damageDef);
        }
    }

    private void spread() {
        int numSpread = 0;
        int spreadMax = 2;
        this.reduceSpreadChance();
        ArrayList<GridPoint2> adjacent = Tile.GET_ADJACENT_COORDS(this.base.worldX, this.base.worldY, false);
        Collections.shuffle(adjacent);
        for (GridPoint2 gp : adjacent) {
            if (numSpread >= spreadMax) break;
            Tile t = this.base.world.getTile(gp.x, gp.y);
            if (t == null || !(t instanceof BaseModule)) continue;
            BaseModule b = (BaseModule)this.base.world.getTile(gp.x, gp.y);
            if (b.baseDisaster != null) continue;
            BaseFire fire = new BaseFire(b);
            fire.spreadTiles = this.spreadTiles;
            b.triggerDisaster(fire);
            ++numSpread;
        }
    }

    private void reduceSpreadChance() {
        --this.spreadTiles;
        if (this.spreadTiles < 0) {
            this.spreadTiles = 0;
        }
    }

    @Override
    public void fix() {
        super.fix();
        this.fireParticle.pfx.allowCompletion();
    }

    @Override
    public void dispose() {
        this.fireParticle.pfx.allowCompletion();
        super.dispose();
    }
}

