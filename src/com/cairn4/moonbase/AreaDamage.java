/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.cairn4.moonbase.DamageTaker;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Entity;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.Damageable;
import com.cairn4.moonbase.worlddata.DamageDef;
import java.util.ArrayList;

public abstract class AreaDamage {
    public static DamageDef defaultDamageDef = new DamageDef();

    public static void damage(World world, float worldXPos, float worldYPos, float radius, float maxDamage, DamageDef damageDef) {
        float damage;
        float dist;
        ArrayList<GridPoint2> tileList = Tile.GET_NEARBY_COORDS(worldXPos, worldYPos, radius);
        Vector2 tileCenter = new Vector2(0.0f, 0.0f);
        float distance = 0.0f;
        for (GridPoint2 gp : tileList) {
            Behavior b;
            Tile t = world.getTile(gp.x, gp.y);
            if (t == null || (b = t.findBehavior(Damageable.class)) == null) continue;
            tileCenter.set(t.getXCenter(), t.getYCenter());
            distance = tileCenter.dst(worldXPos, worldYPos);
            if (!(distance < radius)) continue;
            float d = AreaDamage.calcDamage(distance, radius, maxDamage);
            MoonBase.debug("damage at distance: " + distance + " = " + d);
            if (!(d >= 1.0f)) continue;
            ((Damageable)b).takeDamage(d, damageDef);
        }
        for (int i = 0; i < world.entityList.size(); ++i) {
            float damage2;
            float dist2;
            Entity e = world.entityList.get(i);
            if (!(e instanceof DamageTaker) || !((dist2 = tileCenter.set(e.getXPos(), e.getYPos()).dst(worldXPos, worldYPos)) < radius) || !((damage2 = AreaDamage.calcDamage(dist2, radius, maxDamage)) >= 1.0f)) continue;
            ((DamageTaker)((Object)e)).takeDamage(damage2, damageDef);
        }
        if (world.getPlayer() != null && !world.player.isDrivingVehicle() && !world.player.isFlyingRocket() && (dist = tileCenter.set(world.player.getXPos(), world.player.getYPos()).dst(worldXPos, worldYPos)) < radius && (damage = AreaDamage.calcDamage(dist, radius, maxDamage)) >= 1.0f) {
            if (damageDef.knockBack) {
                world.player.applyKnockback(worldXPos, worldYPos, damage);
            }
            world.player.playerStatus.takeHitDamage(damage);
        }
    }

    private static float calcDamage(float distance, float radius, float maxDamage) {
        float perc = distance / radius;
        return maxDamage - perc * maxDamage;
    }

    public static void continualDamage(World world, float worldXPos, float worldYPos, float radius, float maxDamagePerSec, float delta, DamageDef damageDef) {
        Vector2 tileCenter = new Vector2(0.0f, 0.0f);
        for (Entity e : world.entityList) {
            float dist;
            if (!(e instanceof DamageTaker) || !((dist = tileCenter.set(e.getXPos(), e.getYPos()).dst(worldXPos, worldYPos)) < radius)) continue;
            float damage = AreaDamage.calcDamage(dist, radius, maxDamagePerSec * delta);
            ((DamageTaker)((Object)e)).takeDamage(damage, damageDef);
        }
        float dist = tileCenter.set(world.player.getXPos(), world.player.getYPos()).dst(worldXPos, worldYPos);
        if (dist < radius) {
            float damage = AreaDamage.calcDamage(dist, radius, maxDamagePerSec * delta);
            world.player.playerStatus.takeHitDamage(damage);
        }
    }
}

