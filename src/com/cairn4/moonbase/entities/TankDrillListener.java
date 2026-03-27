/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.cairn4.moonbase.entities.Creature;
import com.cairn4.moonbase.entities.TankDrill;
import com.cairn4.moonbase.tiles.ItemDropper;

class TankDrillListener
implements ContactListener {
    public boolean inContact = false;

    TankDrillListener() {
    }

    @Override
    public void endContact(Contact contact) {
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
        if (tankDrill != null && o instanceof ItemDropper) {
            ItemDropper dropper = (ItemDropper)o;
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

    @Override
    public void beginContact(Contact contact) {
        System.out.println("Tank sensor crashed into " + contact.toString());
        this.inContact = true;
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
        if (tankDrill != null) {
            Creature creature;
            ItemDropper dropper;
            if (o instanceof ItemDropper && (dropper = (ItemDropper)o).canHarvest() && !tankDrill.tank.tilesDrilling.contains(dropper)) {
                tankDrill.tank.tilesDrilling.add(dropper);
            }
            if (o instanceof Creature && !tankDrill.tank.creaturesDrilling.contains(creature = (Creature)o)) {
                tankDrill.tank.creaturesDrilling.add(creature);
            }
        }
    }
}

