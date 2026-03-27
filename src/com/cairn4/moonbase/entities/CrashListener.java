/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.entities.Vehicle;

class CrashListener
implements ContactListener {
    public boolean hit = false;

    CrashListener() {
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
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    @Override
    public void beginContact(Contact contact) {
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
    }
}

