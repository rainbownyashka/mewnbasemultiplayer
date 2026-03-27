/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.controllers.desktop.support;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.desktop.JamepadControllerManager;
import com.badlogic.gdx.controllers.desktop.support.JamepadController;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;

public class JamepadControllerMonitor
implements Runnable {
    private final ControllerManager controllerManager;
    private final ControllerListener listener;
    private final IntMap<Tuple> indexToController = new IntMap();
    private final IntArray disconnectedControllers = new IntArray();

    public JamepadControllerMonitor(ControllerManager controllerManager, ControllerListener listener) {
        this.controllerManager = controllerManager;
        this.listener = listener;
    }

    @Override
    public void run() {
        this.controllerManager.update();
        this.checkForNewControllers();
        this.update();
        Gdx.app.postRunnable(this);
    }

    private void checkForNewControllers() {
        int numControllers = JamepadControllerManager.jamepadConfiguration.maxNumControllers;
        for (int i = 0; i < numControllers; ++i) {
            try {
                ControllerIndex controllerIndex = this.controllerManager.getControllerIndex(i);
                if (this.indexToController.containsKey(controllerIndex.getIndex()) || !controllerIndex.isConnected()) continue;
                Tuple tuple1 = new Tuple(controllerIndex);
                tuple1.controller.addListener(this.listener);
                this.indexToController.put(controllerIndex.getIndex(), tuple1);
                this.listener.connected(tuple1.controller);
                continue;
            }
            catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                // empty catch block
            }
        }
    }

    private void update() {
        this.disconnectedControllers.clear();
        for (Tuple tuple : this.indexToController.values()) {
            JamepadController controller = tuple.controller;
            boolean connected = controller.update();
            if (connected) continue;
            this.disconnectedControllers.add(tuple.index.getIndex());
        }
        for (int i = 0; i < this.disconnectedControllers.size; ++i) {
            this.indexToController.remove(this.disconnectedControllers.get(i));
        }
    }

    private class Tuple {
        public final ControllerIndex index;
        public final JamepadController controller;

        public Tuple(ControllerIndex index) {
            this.index = index;
            this.controller = new JamepadController(index);
        }
    }
}

