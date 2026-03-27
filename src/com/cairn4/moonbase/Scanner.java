/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.tiles.GroundTile;
import com.cairn4.moonbase.ui.ScannerVisualization;
import java.util.ArrayList;

public class Scanner {
    Player player;
    private int detectorPowerCost;
    private int range;
    ArrayList<GridPoint2> detectedCoords = new ArrayList();
    private ScannerVisualization scannerVisualization;
    private boolean active;
    private float scanTime = 5.0f;
    private float timer;
    private boolean scanning = false;
    private float scanTimeRequirement = 1.0f;
    private float scanTimer = 0.0f;

    public int getRange() {
        return this.range;
    }

    public Scanner(Player player) {
        this.player = player;
        this.scanTimer = 0.0f;
        this.detectorPowerCost = 0;
        this.range = 7;
    }

    private void setupVisualization() {
        this.scannerVisualization = new ScannerVisualization(this.player.world.gameScreen.hud, this.player, this);
    }

    public void update(float delta) {
        if (this.scannerVisualization != null) {
            this.scannerVisualization.update(delta);
        } else {
            this.setupVisualization();
        }
        if (this.active) {
            this.timer += delta;
            if (this.timer < this.scanTime) {
                this.detect();
            } else {
                this.active = false;
                this.scannerVisualization.turnOff();
            }
        }
        if (this.scanning) {
            this.scanTimer += delta;
            if (this.scanTimer > this.scanTimeRequirement) {
                this.detectResources();
                System.out.println("Detecting Resources");
                this.scanning = false;
                this.player.finishedScanning();
            }
        }
    }

    public void startScan() {
        Gdx.app.log("MewnBase", "Scanner: starting scan");
        this.scanTimer = 0.0f;
        this.scanning = true;
    }

    public void detectResources() {
        this.timer = 0.0f;
        this.active = true;
        this.scannerVisualization.turnOn();
        if (this.player.playerStatus.suitPower >= (float)this.detectorPowerCost) {
            this.player.world.gameScreen.hud.detectResources();
            System.out.println("detectedCoords: " + this.detectedCoords.size());
            this.player.playerStatus.changeSuitPower(-this.detectorPowerCost);
            this.detect();
            GroundTile gt = this.player.world.getGroundTile(this.player.getX(), this.player.getY());
            if (gt.hasResources()) {
                this.newDiscovery();
                gt.removeResources();
            }
        }
    }

    private void newDiscovery() {
        this.player.world.gameScreen.hud.foundScience(1);
        ++this.player.world.techManager.samples;
    }

    private void detect() {
        this.detectedCoords.clear();
        int x = this.player.x;
        int y = this.player.y;
        for (int j = x - this.range; j <= x + this.range; ++j) {
            for (int k = y - this.range; k <= y + this.range; ++k) {
                if (this.player.world.getGroundTile(j, k) == null || !this.player.world.getGroundTile(j, k).hasResources()) continue;
                this.detectedCoords.add(new GridPoint2(j, k));
            }
        }
        this.scannerVisualization.updateNodes(this.detectedCoords);
    }

    public boolean isScanning() {
        return this.scanning;
    }

    public void cancel() {
        this.scanning = false;
        this.active = false;
        this.scanTimer = 0.0f;
    }
}

