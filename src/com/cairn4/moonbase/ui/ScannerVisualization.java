/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.Scanner;
import com.cairn4.moonbase.ui.Hud;
import java.util.ArrayList;

public class ScannerVisualization {
    private Hud hud;
    private Player player;
    private Scanner scanner;
    private int nodes = 30;
    private float radius = 130.0f;
    private float maxLength = 80.0f;
    private float minLength = 5.0f;
    float arc = 360 / this.nodes;
    Group group;
    ArrayList<Image> nodeList = new ArrayList();

    public ScannerVisualization(Hud hud, Player player, Scanner scanner) {
        this.hud = hud;
        this.player = player;
        this.scanner = scanner;
        this.group = new Group();
        this.hud.gameScreen.worldUIStage.addActor(this.group);
        this.group.setPosition(player.getXPos(), player.getYPos());
        this.group.setRotation(-90.0f);
        this.group.getColor().a = 0.0f;
        for (int i = 0; i < this.nodes; ++i) {
            this.createNode(this.arc * (float)i);
        }
    }

    void createNode(float angle) {
        Image i = new Image(this.hud.skin.getDrawable("white"));
        i.setSize(20.0f, this.minLength);
        i.setColor(Color.GREEN);
        i.setOrigin(10.0f, -this.radius);
        i.setRotation(angle);
        i.setPosition(0.0f, this.radius, 1);
        this.group.addActor(i);
        this.nodeList.add(i);
        this.addJitter(i);
    }

    private void addJitter(Image i) {
        i.clearActions();
        i.addAction(Actions.forever(Actions.sequence((Action)Actions.sizeBy(0.0f, 10.0f, 0.5f, Interpolation.sine), (Action)Actions.delay((float)Math.random() * 0.1f), (Action)Actions.sizeBy(0.0f, -10.0f, 0.5f, Interpolation.sine))));
    }

    public void update(float delta) {
        this.group.setPosition(this.player.getXPos(), this.player.getYPos());
    }

    public void updateNodes(ArrayList<GridPoint2> detectedCoords) {
        this.resetNodes();
        for (GridPoint2 coord : detectedCoords) {
            int a3;
            int a2;
            Vector2 playerPos = new Vector2(this.player.getX(), this.player.getY());
            Vector2 coord2 = new Vector2(coord.x, coord.y);
            float angle = coord2.cpy().sub(playerPos).angle();
            int a = MathUtils.round(angle / this.arc);
            float dist = playerPos.dst(coord2);
            float length = this.maxLength - dist * this.maxLength / (float)this.scanner.getRange();
            Image i = this.nodeList.get(a);
            float newHeight = i.getHeight() + length;
            if (newHeight > this.maxLength) {
                newHeight = this.maxLength;
            }
            i.setHeight(newHeight);
            i.setColor(Color.GREEN);
            float alpha = i.getColor().a = newHeight / this.maxLength;
            if (i.getColor().a < 0.5f) {
                i.setColor(Color.YELLOW);
                i.getColor().a = alpha;
            }
            if ((a2 = a + 1) >= this.nodeList.size()) {
                a2 -= this.nodes;
            }
            if ((a3 = a - 1) >= 0) continue;
            a3 += this.nodes;
        }
    }

    private void resetNodes() {
        for (Image i : this.nodeList) {
            i.setHeight(this.minLength);
            i.setColor(Color.RED);
            i.getColor().a = 0.3f;
        }
    }

    public void turnOn() {
        this.group.addAction(Actions.sequence((Action)Actions.fadeIn(0.25f)));
    }

    public void turnOff() {
        this.group.addAction(Actions.sequence((Action)Actions.fadeOut(0.5f)));
    }
}

