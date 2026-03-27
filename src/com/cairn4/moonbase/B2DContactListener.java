/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.entities.Creature;
import com.cairn4.moonbase.entities.Projectile;
import com.cairn4.moonbase.entities.TankDrill;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.tiles.ItemDropper;

public class B2DContactListener
implements ContactListener {
    public boolean hit = false;

    @Override
    public void beginContact(Contact contact) {
        ItemDropper dropper;
        Creature creature = null;
        if (contact.getFixtureA().getBody().getUserData() instanceof Creature) {
            creature = (Creature)contact.getFixtureA().getBody().getUserData();
        }
        if (contact.getFixtureB().getBody().getUserData() instanceof Creature) {
            creature = (Creature)contact.getFixtureB().getBody().getUserData();
        }
        if (creature != null) {
            creature.cancelLurch();
        }
        this.hit = true;
        Vehicle b = null;
        if (contact.getFixtureA().getBody().getUserData() instanceof Vehicle) {
            b = (Vehicle)contact.getFixtureA().getBody().getUserData();
        }
        if (contact.getFixtureB().getBody().getUserData() instanceof Vehicle) {
            b = (Vehicle)contact.getFixtureB().getBody().getUserData();
        }
        if (b != null) {
            b.startCollision();
        }
        TankDrill tankDrill = null;
        Object o = null;
        if (contact.getFixtureA().getUserData() instanceof TankDrill) {
            tankDrill = (TankDrill)contact.getFixtureA().getUserData();
            o = contact.getFixtureB().getBody().getUserData();
        }
        if (contact.getFixtureB().getUserData() instanceof TankDrill) {
            tankDrill = (TankDrill)contact.getFixtureB().getUserData();
            o = contact.getFixtureA().getBody().getUserData();
        }
        if (tankDrill != null && o instanceof ItemDropper && (dropper = (ItemDropper)o).canHarvest() && !tankDrill.tank.tilesDrilling.contains(dropper)) {
            tankDrill.tank.tilesDrilling.add(dropper);
        }
        Projectile p = null;
        Fixture projectileFixture = null;
        if (contact.getFixtureA().getBody().getUserData() instanceof Projectile) {
            p = (Projectile)contact.getFixtureA().getBody().getUserData();
            projectileFixture = contact.getFixtureB();
        }
        if (contact.getFixtureB().getBody().getUserData() instanceof Projectile) {
            p = (Projectile)contact.getFixtureB().getBody().getUserData();
            projectileFixture = contact.getFixtureA();
        }
        Projectile fP = p;
        Fixture fFixture = projectileFixture;
        if (fP != null && projectileFixture != null) {
            MoonBase.debug("B2DContactListener: bullet hit something");
            fP.hit(fFixture);
        }
    }

    @Override
    public void endContact(Contact contact) {
        Vehicle b = null;
        Object o = null;
        if (contact.getFixtureA().getBody().getUserData() instanceof Vehicle) {
            b = (Vehicle)contact.getFixtureA().getBody().getUserData();
            o = contact.getFixtureB().getBody().getUserData();
        }
        if (contact.getFixtureB().getBody().getUserData() instanceof Vehicle) {
            b = (Vehicle)contact.getFixtureB().getBody().getUserData();
            o = contact.getFixtureA().getBody().getUserData();
        }
        if (o instanceof Player) {
            b = null;
        }
        if (b != null && this.hit) {
            b.finishCrash(o);
            this.hit = false;
        }
        TankDrill tankDrill = null;
        Object oT = null;
        if (contact.getFixtureA().getUserData() instanceof TankDrill) {
            tankDrill = (TankDrill)contact.getFixtureA().getUserData();
            oT = contact.getFixtureB().getBody().getUserData();
        }
        if (contact.getFixtureB().getUserData() instanceof TankDrill) {
            tankDrill = (TankDrill)contact.getFixtureB().getUserData();
            oT = contact.getFixtureA().getBody().getUserData();
        }
        if (tankDrill != null && oT instanceof ItemDropper) {
            ItemDropper dropper = (ItemDropper)oT;
            dropper.stopHarvesting();
            if (tankDrill.tank.tilesDrilling.contains(dropper)) {
                tankDrill.tank.tilesDrilling.remove(dropper);
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}

