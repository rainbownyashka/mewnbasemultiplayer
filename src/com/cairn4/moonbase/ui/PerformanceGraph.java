/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.FloatArray;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.ui.GameScreen;
import java.util.ArrayList;

public class PerformanceGraph {
    GameScreen gameScreen;
    Stage stage;
    Group group;
    Pixmap pixmap;
    Texture texture;
    Image image;
    private static final int GRAPH_LENGTH = 480;
    private static int GRAPHS = 5;
    private static int GRAPH_HEIGHT = 380 / GRAPHS;
    ArrayList<FloatArray> floatArrays = new ArrayList();
    private static final Color[] GRAPH_COLORS = new Color[]{Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.GREEN, Color.PURPLE, Color.TEAL, Color.PINK};
    private final Label renderLabel;
    private final Table table;
    StringBuilder statSB;
    private float resetTimer;
    private float stepTimer = 0.0f;

    public PerformanceGraph(Stage stage, GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.group = new Group();
        stage.addActor(this.group);
        this.group.setPosition(20.0f, 200.0f);
        for (int i = 0; i < GRAPHS; ++i) {
            this.floatArrays.add(new FloatArray(true, 480));
        }
        this.stage = stage;
        this.table = new Table();
        this.table.setWidth(400.0f);
        this.table.setHeight(330.0f);
        this.table.left().bottom();
        this.group.addActor(this.table);
        Drawable d = gameScreen.skin.getDrawable("black");
        this.table.setColor(1.0f, 1.0f, 1.0f, 0.6f);
        this.table.setBackground(d);
        this.table.pad(20.0f);
        this.table.setPosition(0.0f, 0.0f, 12);
        this.renderLabel = new Label((CharSequence)"PerfGraph...", gameScreen.labelStyle);
        this.renderLabel.setFontScale(0.3f);
        this.renderLabel.setAlignment(12, 12);
        this.table.add(this.renderLabel).expand().left().bottom();
        this.group.setTouchable(Touchable.disabled);
        this.renderLabel.setTouchable(Touchable.disabled);
        this.statSB = new StringBuilder();
    }

    public void setVisible(boolean visible) {
        this.group.setVisible(visible);
    }

    public void update(float delta) {
        this.resetTimer += delta;
        if (this.resetTimer > 5.0f) {
            this.gameScreen.updateCounter.reset();
            this.gameScreen.worldRenderCounter.reset();
            this.gameScreen.perfCounter.reset();
            this.gameScreen.uiRenderCounter.reset();
            this.gameScreen.physicsCounter.reset();
            this.resetTimer = 0.0f;
        }
        this.stepTimer += delta;
        if (this.stepTimer > 0.5f) {
            this.stepTimer = 0.0f;
            this.statSB.setLength(0);
            this.statSB.append("Mem: ");
            this.statSB.append(Vars.threeDecimals.format((float)Gdx.app.getNativeHeap() * Vars.BYTES_TO_MEGABYTES));
            this.statSB.append(" MB\n");
            this.statSB.append("RenderWorld: ");
            this.statSB.append(Vars.threeDecimals.format(this.gameScreen.worldRenderCounter.time.average * 1000.0f));
            this.statSB.append("ms - Load:");
            this.statSB.append(Vars.threeDecimals.format(this.gameScreen.worldRenderCounter.load.average));
            this.statSB.append("%\n");
            this.statSB.append("RenderUI: ");
            this.statSB.append(Vars.threeDecimals.format(this.gameScreen.uiRenderCounter.time.average * 1000.0f));
            this.statSB.append("ms - Load:");
            this.statSB.append(Vars.threeDecimals.format(this.gameScreen.uiRenderCounter.load.average));
            this.statSB.append("%\n");
            this.statSB.append("Update: ");
            this.statSB.append(Vars.threeDecimals.format(this.gameScreen.updateCounter.time.average * 1000.0f));
            this.statSB.append("ms - Load:");
            this.statSB.append(Vars.threeDecimals.format(this.gameScreen.updateCounter.load.average));
            this.statSB.append("%\n");
            this.statSB.append("FPS: ");
            this.statSB.append(Vars.singleDecimal.format(Gdx.graphics.getFramesPerSecond()));
            this.statSB.append("\n");
            this.statSB.append("Perf: ");
            this.statSB.append(Vars.threeDecimals.format(this.gameScreen.perfCounter.time.average * 1000.0f));
            this.statSB.append("ms - Load:");
            this.statSB.append(Vars.threeDecimals.format(this.gameScreen.perfCounter.load.average));
            this.statSB.append("%\n");
            this.statSB.append("Physics: ");
            this.statSB.append(Vars.threeDecimals.format(this.gameScreen.physicsCounter.time.average * 1000.0f));
            this.statSB.append("ms - Load:");
            this.statSB.append(Vars.threeDecimals.format(this.gameScreen.physicsCounter.load.average));
            this.statSB.append("%\n");
            this.statSB.append("Player: ");
            this.statSB.append(Vars.threeDecimals.format(this.gameScreen.playerUpdateCounter.time.average * 1000.0f));
            this.statSB.append("ms - Load:");
            this.statSB.append(Vars.threeDecimals.format(this.gameScreen.playerUpdateCounter.load.average));
            this.statSB.append("\n");
            this.statSB.append("Bodies: ");
            this.statSB.append(Vars.bigNum.format(this.gameScreen.world.b2dWorld.getBodyCount()));
            this.statSB.append("\n");
            this.statSB.append("World Actors: ");
            this.statSB.append(Vars.bigNum.format(this.getStageActors(this.gameScreen.stage)));
            this.statSB.append("\n");
            this.renderLabel.setText(this.statSB.toString());
        }
        for (FloatArray f : this.floatArrays) {
            f.truncate(480);
        }
    }

    private int getStageActors(Stage stage) {
        int c = 0;
        for (int i = 0; i < stage.getActors().size; ++i) {
            Actor a = stage.getActors().get(i);
            c += this.getChildren(a);
        }
        return c;
    }

    private int getChildren(Actor a) {
        int c = 1;
        if (a instanceof Group) {
            Group g = (Group)a;
            for (int i = 0; i < g.getChildren().size; ++i) {
                Actor a2 = (Actor)g.getChildren().get(i);
                c += this.getChildren(a2);
            }
        }
        return c;
    }

    public void render(SpriteBatch spriteBatch) {
    }
}

