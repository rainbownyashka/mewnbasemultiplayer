/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.GameScreen;
import java.util.ArrayList;

public class UIEdit {
    EditModes currentMode;
    Actor currentActor;
    int positionAlign = 1;
    Vector2 touchPos = new Vector2(0.0f, 0.0f);
    ArrayList<Actor> actorsAtPos = new ArrayList();
    Array<Actor> allActors = new Array();
    int depth = 0;
    BaseScreen baseScreen;
    SpriteBatch spriteBatch;
    public OrthographicCamera camera;
    public FitViewport viewport;
    public Stage editStage;
    public Stage stage;
    public TextureAtlas atlas;
    public TextureAtlas menuAtlas;
    public Skin skin;
    BitmapFont font;
    Label.LabelStyle labelStyle;
    Label modeLabel;
    private Group handleGroup;
    private ScrollPane scroller;
    private Tree hierarchyTree;
    Button btnHandleTL;
    Button btnHandleTR;
    Button btnHandleBL;
    Button btnHandleBR;
    Button btnHandleR;
    Button btnHandleL;
    Button btnHandleT;
    Button BtnHandleB;
    Vector2 actorLocalPos = new Vector2(0.0f, 0.0f);
    Vector2 actorStagePos = new Vector2(0.0f, 0.0f);

    public UIEdit(BaseScreen baseScreen, Stage stage) {
        this.baseScreen = baseScreen;
        this.currentMode = EditModes.off;
        this.editStage = stage;
        this.skin = baseScreen.skin;
        this.labelStyle = baseScreen.labelStyle;
        this.spriteBatch = new SpriteBatch();
        this.camera = baseScreen.camera;
        this.viewport = new FitViewport(MoonBase.SCREEN_WIDTH, (float)MoonBase.SCREEN_HEIGHT, this.camera);
        this.stage = new Stage();
        this.setupUI();
    }

    protected void setInputProcessor() {
        InputMultiplexer inputMulti = new InputMultiplexer();
        inputMulti.addProcessor(this.stage);
        Gdx.input.setInputProcessor(inputMulti);
        this.baseScreen.game.console.resetInputProcessing();
    }

    public boolean isOn() {
        return this.currentMode != EditModes.off;
    }

    public String editActor(String actorName, Stage stage) {
        String response = "";
        Actor newActor = null;
        newActor = (Actor)this.editStage.getRoot().findActor(actorName);
        if (newActor != null) {
            response = "Editing actor";
            this.setActor(newActor);
        } else {
            response = "Failed to find actor";
        }
        System.out.println(response);
        return response;
    }

    public void startEditMode() {
        this.currentMode = EditModes.position;
        this.setInputProcessor();
    }

    public void endEditMode() {
        if (this.currentActor != null) {
            this.saveEdit();
        }
        this.currentMode = EditModes.off;
        if (this.baseScreen instanceof GameScreen) {
            ((GameScreen)this.baseScreen).setInputProcessor();
        } else {
            this.baseScreen.setInputProcessor();
        }
    }

    public String saveEdit() {
        String response = "--------\n" + this.currentActor + "\n";
        float posX = this.currentActor.getX(this.positionAlign);
        float posY = this.currentActor.getY(this.positionAlign);
        float width = this.currentActor.getWidth();
        float height = this.currentActor.getHeight();
        response = response + this.currentActor.getName() + "\n";
        response = response + "  x: " + posX + "\n";
        response = response + "  y: " + posY + "\n";
        response = response + "  W" + width + "\n";
        response = response + "  H" + height + "\n";
        this.currentActor.setDebug(false);
        this.currentActor = null;
        System.out.println(response);
        return response;
    }

    public void update() {
        if (this.currentMode != EditModes.off) {
            this.modeLabel.setText("Mode:" + this.currentMode.name());
            if (this.currentActor != null) {
                this.handleInput();
            }
            if (Gdx.input.isKeyJustPressed(62)) {
                float mouseX = Gdx.input.getX();
                float mouseY = Gdx.input.getY();
                Vector2 mouse = this.editStage.screenToStageCoordinates(new Vector2(mouseX, mouseY));
                System.out.println(mouse);
                Actor a = this.editStage.getRoot().hit(mouse.x, mouse.y, true);
                if (a != null) {
                    this.setActor(a);
                }
                this.touchPos.set(mouse.x, mouse.y);
            }
        }
    }

    private void setupUI() {
        this.modeLabel = new Label((CharSequence)("Mode:" + this.currentMode.name()), this.labelStyle);
        this.modeLabel.setFontScale(0.4f);
        this.modeLabel.setColor(Color.WHITE);
        this.modeLabel.setPosition(135.0f, 8.0f);
        this.modeLabel.setTouchable(Touchable.disabled);
        this.stage.addActor(this.modeLabel);
        this.handleGroup = new Group();
        this.stage.addActor(this.handleGroup);
        float handleSize = 14.0f;
        this.btnHandleBL = new Button(this.skin.getDrawable("roundedbox"));
        this.btnHandleBL.setSize(handleSize, handleSize);
        this.btnHandleBL.setPosition(0.0f, 0.0f, 1);
        this.handleGroup.addActor(this.btnHandleBL);
        this.btnHandleBR = new Button(this.skin.getDrawable("roundedbox"));
        this.btnHandleBR.setSize(handleSize, handleSize);
        this.btnHandleBR.setPosition(this.handleGroup.getWidth(), 0.0f, 1);
        this.handleGroup.addActor(this.btnHandleBR);
        this.btnHandleTL = new Button(this.skin.getDrawable("roundedbox"));
        this.btnHandleTL.setSize(handleSize, handleSize);
        this.btnHandleTL.setPosition(0.0f, this.handleGroup.getHeight(), 1);
        this.handleGroup.addActor(this.btnHandleTL);
        this.btnHandleTR = new Button(this.skin.getDrawable("roundedbox"));
        this.btnHandleTR.setSize(handleSize, handleSize);
        this.btnHandleTR.setPosition(this.handleGroup.getWidth(), this.handleGroup.getHeight(), 1);
        this.handleGroup.addActor(this.btnHandleTR);
        this.showTree(this.currentActor);
    }

    private void updateHandles() {
        this.btnHandleBL.setPosition(0.0f, 0.0f, 1);
        this.btnHandleBR.setPosition(this.handleGroup.getWidth(), 0.0f, 1);
        this.btnHandleTL.setPosition(0.0f, this.handleGroup.getHeight(), 1);
        this.btnHandleTR.setPosition(this.handleGroup.getWidth(), this.handleGroup.getHeight(), 1);
    }

    public void setActor(Actor a) {
        if (this.currentActor != null) {
            this.currentActor.setDebug(false);
        }
        this.currentActor = a;
        this.currentActor.setDebug(true);
        this.currentMode = EditModes.position;
        this.actorLocalPos.set(this.currentActor.getX(), this.currentActor.getY());
        this.actorStagePos = this.currentActor.localToStageCoordinates(new Vector2(0.0f, 0.0f));
        this.handleGroup.setSize(this.currentActor.getWidth(), this.currentActor.getHeight());
        this.handleGroup.setPosition(this.actorStagePos.x, this.actorStagePos.y);
        this.updateHandles();
        this.setupHandleListeners();
    }

    private void getChildNodes(Actor actor, Tree tree, Tree.Node node) {
        ActorNode currNode = new ActorNode(actor);
        node.add(currNode);
        currNode.setExpanded(true);
        currNode.setSelectable(true);
        if (actor instanceof Group) {
            Group group = (Group)actor;
            for (Actor c : group.getChildren()) {
                this.getChildNodes(c, tree, currNode);
            }
        }
    }

    private void showTree(Actor currentActor) {
        Table table = new Table();
        table.top().left();
        this.stage.addActor(table);
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = this.baseScreen.skin.getDrawable("dark-edge");
        NinePatch scrollbar = new NinePatch(this.baseScreen.skin.getRegion("scrollbar"), 4, 4, 4, 4);
        NinePatch scrollbarBg = new NinePatch(this.baseScreen.skin.getRegion("scrollbar-bg"), 4, 4, 4, 4);
        scrollStyle.vScrollKnob = new NinePatchDrawable(scrollbar);
        scrollStyle.vScroll = new NinePatchDrawable(scrollbarBg);
        this.scroller = new ScrollPane((Actor)table, scrollStyle);
        this.scroller.setWidth(table.getWidth());
        this.scroller.setHeight(table.getHeight());
        this.scroller.setLayoutEnabled(true);
        this.scroller.setVariableSizeKnobs(true);
        this.scroller.setFadeScrollBars(false);
        this.scroller.layout();
        this.scroller.setBounds(0.0f, 0.0f, 300.0f, 500.0f);
        this.scroller.setFlingTime(0.1f);
        this.scroller.setScrollingDisabled(true, false);
        this.scroller.setOverscroll(false, false);
        this.scroller.setFlickScroll(false);
        this.scroller.setSmoothScrolling(true);
        this.scroller.setPosition(20.0f, 100.0f);
        this.stage.addActor(this.scroller);
        this.stage.setScrollFocus(this.scroller);
        this.scroller.layout();
        Tree.TreeStyle ts = new Tree.TreeStyle();
        ts.plus = this.skin.getDrawable("tree-closed");
        ts.minus = this.skin.getDrawable("tree-open");
        this.hierarchyTree = new Tree(ts);
        this.hierarchyTree.setIndentSpacing(20.0f);
        this.hierarchyTree.setFillParent(true);
        table.add(this.hierarchyTree).expandY().fill();
        Group actor = this.editStage.getRoot();
        ActorNode currNode = new ActorNode(actor);
        this.hierarchyTree.add(currNode);
        this.getChildNodes(actor, this.hierarchyTree, currNode);
        this.hierarchyTree.expandAll();
    }

    private void setupHandleListeners() {
        this.btnHandleBL.clearListeners();
        this.btnHandleBR.clearListeners();
        this.btnHandleTL.clearListeners();
        this.btnHandleTR.clearListeners();
        this.btnHandleTR.addListener(new DragListener(){
            float startingW;
            float startingH;
            float newWidth;
            float newHeight;
            float totalDeltaX;
            float totalDeltaY;
            {
                this.startingW = UIEdit.this.currentActor.getWidth();
                this.startingH = UIEdit.this.currentActor.getHeight();
                this.totalDeltaX = 0.0f;
                this.totalDeltaY = 0.0f;
            }

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                this.totalDeltaX = 0.0f;
                this.totalDeltaY = 0.0f;
                UIEdit.this.handleGroup.setColor(Color.RED);
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                this.totalDeltaX -= this.getDeltaX();
                this.totalDeltaY -= this.getDeltaY();
                UIEdit.this.currentActor.setSize(this.startingW + this.totalDeltaX, this.startingH + this.totalDeltaY);
                UIEdit.this.actorLocalPos.set(UIEdit.this.currentActor.getX(), UIEdit.this.currentActor.getY());
                UIEdit.this.actorStagePos = UIEdit.this.currentActor.localToStageCoordinates(new Vector2(0.0f, 0.0f));
                UIEdit.this.handleGroup.setSize(UIEdit.this.currentActor.getWidth(), UIEdit.this.currentActor.getHeight());
                UIEdit.this.handleGroup.setPosition(UIEdit.this.actorStagePos.x, UIEdit.this.actorStagePos.y);
                UIEdit.this.handleGroup.setDebug(true);
                UIEdit.this.updateHandles();
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                UIEdit.this.handleGroup.setColor(Color.WHITE);
                System.out.println(this.totalDeltaX + ", " + this.totalDeltaY);
                UIEdit.this.updateHandles();
            }
        });
    }

    public void handleInput() {
        float dX = 0.0f;
        float dY = 0.0f;
        if (Gdx.input.isKeyJustPressed(21)) {
            dX -= 1.0f;
        }
        if (Gdx.input.isKeyJustPressed(22)) {
            dX += 1.0f;
        }
        if (Gdx.input.isKeyJustPressed(19)) {
            dY += 1.0f;
        }
        if (Gdx.input.isKeyJustPressed(20)) {
            dY -= 1.0f;
        }
        if (Gdx.input.isKeyJustPressed(132)) {
            this.currentMode = this.currentMode == EditModes.position ? EditModes.size : EditModes.position;
        }
        if (Gdx.input.isKeyPressed(59)) {
            dX *= 10.0f;
            dY *= 10.0f;
        }
        switch (this.currentMode) {
            case position: {
                this.moveActor(dX, dY);
                break;
            }
            case size: {
                this.sizeActor(dX, dY);
            }
        }
    }

    private void sizeActor(float dX, float dY) {
        float w = this.currentActor.getWidth();
        float h = this.currentActor.getHeight();
        this.currentActor.setSize(w += dX, h += dY);
    }

    private void moveActor(float dX, float dY) {
        float x = this.currentActor.getX(this.positionAlign);
        float y = this.currentActor.getY(this.positionAlign);
        this.currentActor.setPosition(x += dX, y += dY, this.positionAlign);
    }

    public void setAlign(String align) {
        int positionAlign = 12;
        switch (align) {
            case "center": {
                positionAlign = 1;
                break;
            }
            case "bottomLeft": {
                positionAlign = 12;
                break;
            }
            case "bottom": {
                positionAlign = 4;
                break;
            }
            case "bottomRight": {
                positionAlign = 20;
                break;
            }
            case "right": {
                positionAlign = 16;
                break;
            }
            case "topRight": {
                positionAlign = 18;
                break;
            }
            case "top": {
                positionAlign = 2;
                break;
            }
            case "topLeft": {
                positionAlign = 10;
                break;
            }
            case "left": {
                positionAlign = 8;
            }
        }
    }

    public void render(float delta) {
        if (this.isOn()) {
            this.spriteBatch.begin();
            this.stage.act();
            this.stage.draw();
            this.spriteBatch.end();
        }
    }

    class ActorNode
    extends Tree.Node<Tree.Node, Actor, Label> {
        public ActorNode(final Actor actor) {
            Actor i;
            String labelText = "";
            if (actor instanceof Group) {
                labelText = labelText + "Grp";
            }
            if (actor instanceof Image) {
                labelText = labelText + "Img - ";
                i = (Image)actor;
                labelText = labelText + ((Image)i).getDrawable();
            }
            if (actor instanceof Label) {
                labelText = labelText + "Label - ";
                i = (Label)actor;
                labelText = labelText + ((Label)i).getText().substring(0, Math.min(10, ((Label)i).getText().length));
            }
            if (actor instanceof TextButton) {
                labelText = labelText + "TxtBtn - ";
                i = (TextButton)actor;
                String btnText = ((TextButton)i).getText().toString();
                int length = btnText.length();
                labelText = labelText + btnText.subSequence(0, Math.min(10, length));
            }
            if (actor.getName() != null) {
                labelText = labelText + " (" + actor.getName() + ")";
            }
            Label l = new Label((CharSequence)labelText, UIEdit.this.labelStyle);
            l.setHeight(10.0f);
            l.setFontScale(0.2f);
            this.setActor(l);
            this.setValue(actor);
            l.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    MoonBase.log("Changing to actor: " + actor.getName());
                    UIEdit.this.setActor(actor);
                }
            });
        }

        public void selected() {
        }
    }

    static enum EditModes {
        off,
        position,
        size;

    }
}

