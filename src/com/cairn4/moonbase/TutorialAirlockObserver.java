/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.Tutorial;
import com.cairn4.moonbase.tiles.Airlock;
import com.cairn4.moonbase.tiles.behaviors.AirlockObserverBehavior;
import java.util.Observable;
import java.util.Observer;

public class TutorialAirlockObserver
implements Observer {
    Airlock airlock;
    Tutorial tutorial;

    public TutorialAirlockObserver(Tutorial t, Airlock a) {
        this.tutorial = t;
        this.airlock = a;
        AirlockObserverBehavior aob = new AirlockObserverBehavior(this.airlock);
        aob.addObserver(this);
        this.airlock.behaviorList.add(aob);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (this.tutorial.stage == Tutorial.STAGES.goIntoBase) {
            this.tutorial.advance();
        }
    }
}

