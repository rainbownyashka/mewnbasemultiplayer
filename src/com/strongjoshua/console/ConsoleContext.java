/*
 * Decompiled with CFR 0.152.
 */
package com.strongjoshua.console;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ConsoleContext {
    private Table root;
    private Label label;
    private Label copy;
    private InputListener stageListener;

    ConsoleContext(Class<? extends Table> tableClass, Class<? extends Label> labelClass, Skin skin, String background) {
        try {
            this.root = tableClass.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException("Table class does not support empty constructor.");
        }
        try {
            this.copy = labelClass.getConstructor(CharSequence.class, Skin.class).newInstance("Copy", skin);
        }
        catch (Exception e) {
            try {
                this.copy = labelClass.getConstructor(CharSequence.class).newInstance("Copy");
            }
            catch (Exception e2) {
                throw new RuntimeException("Label class does not support either (<CharSequence>, <Skin>) or (<CharSequence>) constructors.");
            }
        }
        this.copy.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.getPointer() != 0) {
                    return;
                }
                if (ConsoleContext.this.label == null) {
                    throw new RuntimeException("Trying to copy a null label (this should never happen).");
                }
                Gdx.app.getClipboard().setContents(ConsoleContext.this.label.getText().toString().trim());
                ConsoleContext.this.remove();
            }
        });
        this.copy.addListener(new HoverListener(this.copy));
        this.root.add(this.copy);
        this.root.pad(5.0f);
        this.root.setBackground(skin.getDrawable(background));
        this.root.setSize(this.root.getPrefWidth(), this.root.getPrefHeight());
        this.stageListener = new InputListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (ConsoleContext.this.root.hit(x, y, false) == null) {
                    ConsoleContext.this.remove();
                }
                return true;
            }
        };
    }

    void setLabel(Label l) {
        this.label = l;
    }

    protected void setStage(Stage stage) {
        if (stage != null) {
            stage.addListener(this.stageListener);
            stage.addActor(this.root);
        }
    }

    protected boolean remove() {
        if (this.root.getStage() != null) {
            this.root.getStage().removeListener(this.stageListener);
        }
        return this.root.remove();
    }

    protected void setPosition(float x, float y) {
        this.root.setPosition(x, y);
    }

    private class HoverListener
    extends InputListener {
        private Label label;

        HoverListener(Label l) {
            this.label = l;
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            this.label.setColor(Color.CYAN);
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            this.label.setColor(Color.WHITE);
        }
    }
}

