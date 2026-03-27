/*
 * Decompiled with CFR 0.152.
 */
package com.strongjoshua.console;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragScrollListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.strongjoshua.console.AbstractConsole;
import com.strongjoshua.console.CommandCompleter;
import com.strongjoshua.console.CommandHistory;
import com.strongjoshua.console.ConsoleContext;
import com.strongjoshua.console.LogEntry;
import com.strongjoshua.console.LogLevel;

public class GUIConsole
extends AbstractConsole {
    private int keyID;
    private ConsoleDisplay display;
    private boolean hidden = true;
    private boolean usesMultiplexer;
    private InputProcessor appInput;
    private InputMultiplexer multiplexer;
    private Stage stage;
    private CommandHistory commandHistory;
    private CommandCompleter commandCompleter;
    private Window consoleWindow;
    private boolean hasHover;
    private Color hoverColor;
    private Color noHoverColor;
    private Vector3 stageCoords = new Vector3();
    private ScrollPane scroll;
    private Class<? extends Table> tableClass;
    private String tableBackground;
    private Class<? extends TextField> textFieldClass;
    private Class<? extends TextButton> textButtonClass;
    private Class<? extends Label> labelClass;
    private Class<? extends ScrollPane> scrollPaneClass;

    public GUIConsole() {
        this(new Skin(Gdx.files.classpath("default_skin/uiskin.json")));
    }

    public GUIConsole(Skin skin) {
        this(skin, true);
    }

    public GUIConsole(boolean useMultiplexer) {
        this(new Skin(Gdx.files.classpath("default_skin/uiskin.json")), useMultiplexer);
    }

    public GUIConsole(Skin skin, boolean useMultiplexer) {
        this(skin, useMultiplexer, 75);
    }

    public GUIConsole(Skin skin, boolean useMultiplexer, int keyID) {
        this(skin, useMultiplexer, keyID, Window.class, Table.class, "default-rect", TextField.class, TextButton.class, Label.class, ScrollPane.class);
    }

    public GUIConsole(Skin skin, boolean useMultiplexer, int keyID, Class<? extends Window> windowClass, Class<? extends Table> tableClass, String tableBackground, Class<? extends TextField> textFieldClass, Class<? extends TextButton> textButtonClass, Class<? extends Label> labelClass, Class<? extends ScrollPane> scrollPaneClass) {
        this.tableClass = tableClass;
        this.tableBackground = tableBackground;
        this.textFieldClass = textFieldClass;
        this.textButtonClass = textButtonClass;
        this.labelClass = labelClass;
        this.scrollPaneClass = scrollPaneClass;
        this.keyID = keyID;
        this.stage = new Stage();
        this.display = new ConsoleDisplay(skin);
        this.commandHistory = new CommandHistory();
        this.commandCompleter = new CommandCompleter();
        this.logToSystem = false;
        this.usesMultiplexer = useMultiplexer;
        if (useMultiplexer) {
            this.resetInputProcessing();
        }
        this.display.root.pad(4.0f);
        this.display.root.padTop(22.0f);
        this.display.root.setFillParent(true);
        this.display.showSubmit(false);
        try {
            this.consoleWindow = windowClass.getConstructor(String.class, Skin.class).newInstance("Console", skin);
        }
        catch (Exception e) {
            try {
                this.consoleWindow = windowClass.getConstructor(String.class).newInstance("Console");
            }
            catch (Exception e2) {
                throw new RuntimeException("Window class does not support either (<String>, <Skin>) or (<String>) constructors.");
            }
        }
        this.consoleWindow.setMovable(true);
        this.consoleWindow.setResizable(true);
        this.consoleWindow.setKeepWithinStage(true);
        this.consoleWindow.addActor(this.display.root);
        this.consoleWindow.setTouchable(Touchable.disabled);
        this.hoverColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.noHoverColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.stage.addListener(new DisplayListener());
        this.stage.addActor(this.consoleWindow);
        this.stage.setKeyboardFocus(this.display.root);
        this.setSizePercent(50.0f, 50.0f);
        this.setPositionPercent(50.0f, 50.0f);
    }

    @Override
    public void setMaxEntries(int numEntries) {
        if (numEntries <= 0 && numEntries != -1) {
            throw new IllegalArgumentException("Maximum entries must be greater than 0 or use Console.UNLIMITED_ENTRIES.");
        }
        this.log.setMaxEntries(numEntries);
    }

    @Override
    public void clear() {
        this.log.getLogEntries().clear();
        this.display.refresh();
    }

    @Override
    public void setSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Pixel size must be greater than 0.");
        }
        this.consoleWindow.setSize(width, height);
    }

    @Override
    public void setSizePercent(float wPct, float hPct) {
        if (wPct <= 0.0f || hPct <= 0.0f) {
            throw new IllegalArgumentException("Size percentage must be greater than 0.");
        }
        if (wPct > 100.0f || hPct > 100.0f) {
            throw new IllegalArgumentException("Size percentage cannot be greater than 100.");
        }
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        this.consoleWindow.setSize(w * wPct / 100.0f, h * hPct / 100.0f);
    }

    @Override
    public void setPosition(int x, int y) {
        this.consoleWindow.setPosition(x, y);
    }

    @Override
    public void setPositionPercent(float xPosPct, float yPosPct) {
        if (xPosPct > 100.0f || yPosPct > 100.0f) {
            throw new IllegalArgumentException("Error: The console would be drawn outside of the screen.");
        }
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        this.consoleWindow.setPosition(w * xPosPct / 100.0f, h * yPosPct / 100.0f);
    }

    @Override
    public void resetInputProcessing() {
        this.usesMultiplexer = true;
        this.appInput = Gdx.input.getInputProcessor();
        if (this.appInput != null) {
            if (this.hasStage(this.appInput)) {
                this.log("Console already added to input processor!", LogLevel.ERROR);
                Gdx.app.log("Console", "Already added to input processor!");
                return;
            }
            this.multiplexer = new InputMultiplexer();
            this.multiplexer.addProcessor(this.stage);
            this.multiplexer.addProcessor(this.appInput);
            Gdx.input.setInputProcessor(this.multiplexer);
        } else {
            Gdx.input.setInputProcessor(this.stage);
        }
    }

    private boolean hasStage(InputProcessor processor) {
        if (!(processor instanceof InputMultiplexer)) {
            return processor == this.stage;
        }
        InputMultiplexer im = (InputMultiplexer)processor;
        SnapshotArray<InputProcessor> ips = im.getProcessors();
        for (InputProcessor ip : ips) {
            if (!this.hasStage(ip)) continue;
            return true;
        }
        return false;
    }

    @Override
    public InputProcessor getInputProcessor() {
        return this.stage;
    }

    @Override
    public void draw() {
        if (this.disabled) {
            return;
        }
        this.stage.act();
        if (this.hidden) {
            return;
        }
        this.stage.draw();
    }

    @Override
    public void refresh() {
        this.refresh(true);
    }

    @Override
    public void refresh(boolean retain) {
        float oldWPct = 0.0f;
        float oldHPct = 0.0f;
        float oldXPosPct = 0.0f;
        float oldYPosPct = 0.0f;
        if (retain) {
            oldWPct = this.consoleWindow.getWidth() / this.stage.getWidth() * 100.0f;
            oldHPct = this.consoleWindow.getHeight() / this.stage.getHeight() * 100.0f;
            oldXPosPct = this.consoleWindow.getX() / this.stage.getWidth() * 100.0f;
            oldYPosPct = this.consoleWindow.getY() / this.stage.getHeight() * 100.0f;
        }
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        this.stage.getViewport().setWorldSize(width, height);
        this.stage.getViewport().update(width, height, true);
        if (retain) {
            this.setSizePercent(oldWPct, oldHPct);
            this.setPositionPercent(oldXPosPct, oldYPosPct);
        }
    }

    @Override
    public void log(String msg, LogLevel level) {
        super.log(msg, level);
        this.display.refresh();
    }

    @Override
    public void setDisabled(boolean disabled) {
        if (disabled) {
            this.display.setHidden(true);
        }
        this.disabled = disabled;
    }

    @Override
    public int getDisplayKeyID() {
        return this.keyID;
    }

    @Override
    public void setDisplayKeyID(int code) {
        if (code == 66) {
            return;
        }
        this.keyID = code;
    }

    @Override
    public boolean hitsConsole(float screenX, float screenY) {
        if (this.disabled || this.hidden) {
            return false;
        }
        this.stage.getCamera().unproject(this.stageCoords.set(screenX, screenY, 0.0f));
        return this.stage.hit(this.stageCoords.x, this.stageCoords.y, true) != null;
    }

    @Override
    public void dispose() {
        if (this.usesMultiplexer && this.appInput != null) {
            Gdx.input.setInputProcessor(this.appInput);
        }
        this.stage.dispose();
    }

    @Override
    public boolean isVisible() {
        return !this.hidden;
    }

    @Override
    public void setVisible(boolean visible) {
        this.display.setHidden(!visible);
    }

    @Override
    public void select() {
        this.display.select();
    }

    @Override
    public void deselect() {
        this.display.deselect();
    }

    @Override
    public void setTitle(String title) {
        this.consoleWindow.getTitleLabel().setText(title);
    }

    private void refreshWindowColor() {
        this.consoleWindow.setColor(this.hasHover ? this.hoverColor : this.noHoverColor);
    }

    @Override
    public void setHoverAlpha(float alpha) {
        this.hoverColor.a = alpha;
        this.refreshWindowColor();
    }

    @Override
    public void setNoHoverAlpha(float alpha) {
        this.noHoverColor.a = alpha;
        this.refreshWindowColor();
    }

    @Override
    public void setHoverColor(Color color) {
        this.hoverColor = color;
        this.refreshWindowColor();
    }

    @Override
    public void setNoHoverColor(Color color) {
        this.noHoverColor = color;
        this.refreshWindowColor();
    }

    @Override
    public void enableSubmitButton(boolean enable) {
        this.display.showSubmit(enable);
    }

    @Override
    public void setSubmitText(String text) {
        this.display.setSubmitText(text);
    }

    @Override
    public Window getWindow() {
        return this.consoleWindow;
    }

    private class LogListener
    extends ClickListener {
        private Label self;
        private Drawable highlighted;

        LogListener(Label label, Drawable highlighted) {
            this.self = label;
            this.highlighted = highlighted;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            Vector2 pos = this.self.localToStageCoordinates(new Vector2(x, y));
            GUIConsole.this.display.openContext(this.self, pos.x, pos.y);
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (pointer != -1) {
                return;
            }
            this.self.getStyle().background = this.highlighted;
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (pointer != -1) {
                return;
            }
            this.self.getStyle().background = null;
        }
    }

    private class DisplayListener
    extends InputListener {
        private DisplayListener() {
        }

        @Override
        public boolean keyDown(InputEvent event, int keycode) {
            if (GUIConsole.this.disabled) {
                return false;
            }
            if (keycode == GUIConsole.this.keyID) {
                GUIConsole.this.display.setHidden(!GUIConsole.this.hidden);
                return true;
            }
            return false;
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (pointer != -1) {
                return;
            }
            GUIConsole.this.hasHover = true;
            GUIConsole.this.refreshWindowColor();
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (pointer != -1) {
                return;
            }
            GUIConsole.this.hasHover = false;
            GUIConsole.this.refreshWindowColor();
        }
    }

    private class KeyListener
    extends InputListener {
        private TextField input;

        protected KeyListener(TextField tf) {
            this.input = tf;
        }

        @Override
        public boolean keyDown(InputEvent event, int keycode) {
            if (GUIConsole.this.disabled) {
                return false;
            }
            if (keycode != 61) {
                GUIConsole.this.commandCompleter.reset();
            }
            if (keycode == 66 && !GUIConsole.this.hidden) {
                GUIConsole.this.commandHistory.getNextCommand();
                return GUIConsole.this.display.submit();
            }
            if (keycode == 19 && !GUIConsole.this.hidden) {
                this.input.setText(GUIConsole.this.commandHistory.getPreviousCommand());
                this.input.setCursorPosition(this.input.getText().length());
                return true;
            }
            if (keycode == 20 && !GUIConsole.this.hidden) {
                this.input.setText(GUIConsole.this.commandHistory.getNextCommand());
                this.input.setCursorPosition(this.input.getText().length());
                return true;
            }
            if (keycode == 61 && !GUIConsole.this.hidden) {
                String s = this.input.getText();
                if (s.length() == 0) {
                    return false;
                }
                if (GUIConsole.this.commandCompleter.isNew()) {
                    GUIConsole.this.commandCompleter.set(GUIConsole.this.exec, s);
                }
                this.input.setText(GUIConsole.this.commandCompleter.next());
                this.input.setCursorPosition(this.input.getText().length());
                return true;
            }
            return false;
        }
    }

    private class FieldListener
    implements TextField.TextFieldListener {
        private FieldListener() {
        }

        @Override
        public void keyTyped(TextField textField, char c) {
            if (("" + c).equalsIgnoreCase(Input.Keys.toString(GUIConsole.this.keyID))) {
                String s = textField.getText();
                textField.setText(s.substring(0, s.length() - 1));
            }
        }
    }

    private class ConsoleDisplay {
        private Table root;
        private Table logEntries;
        private TextField input;
        private TextButton submit;
        private Skin skin;
        private Array<Label> labels;
        private String fontName;
        private boolean selected = true;
        private ConsoleContext context;
        private Cell<TextButton> submitCell;

        ConsoleDisplay(Skin skin) {
            try {
                this.root = (Table)GUIConsole.this.tableClass.newInstance();
            }
            catch (Exception e) {
                throw new RuntimeException("Table class does not support empty constructor.");
            }
            this.skin = skin;
            this.context = new ConsoleContext(GUIConsole.this.tableClass, GUIConsole.this.labelClass, skin, GUIConsole.this.tableBackground);
            this.fontName = skin.has("console-font", BitmapFont.class) ? "console-font" : "default-font";
            TextField.TextFieldStyle tfs = skin.get(TextField.TextFieldStyle.class);
            tfs.font = skin.getFont(this.fontName);
            this.labels = new Array();
            try {
                this.logEntries = (Table)GUIConsole.this.tableClass.newInstance();
            }
            catch (Exception e) {
                throw new RuntimeException("Table class does not support empty constructor.");
            }
            try {
                this.input = (TextField)GUIConsole.this.textFieldClass.getConstructor(String.class, TextField.TextFieldStyle.class).newInstance("", tfs);
            }
            catch (Exception e) {
                throw new RuntimeException("TextField class does not support (<String>, <Skin>) constructor.");
            }
            this.input.setTextFieldListener(new FieldListener());
            try {
                this.submit = (TextButton)GUIConsole.this.textButtonClass.getConstructor(String.class, Skin.class).newInstance("Submit", skin);
            }
            catch (Exception e) {
                try {
                    this.submit = (TextButton)GUIConsole.this.textButtonClass.getConstructor(String.class).newInstance("Submit");
                }
                catch (Exception e2) {
                    throw new RuntimeException("TextButton class does not support either (<String>, <Skin>) or (<String>) constructors.");
                }
            }
            this.submit.addListener(new ClickListener(){

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    ConsoleDisplay.this.submit();
                }
            });
            try {
                GUIConsole.this.scroll = (ScrollPane)GUIConsole.this.scrollPaneClass.getConstructor(Actor.class, Skin.class).newInstance(this.logEntries, skin);
            }
            catch (Exception e) {
                try {
                    GUIConsole.this.scroll = (ScrollPane)GUIConsole.this.scrollPaneClass.getConstructor(Actor.class).newInstance(this.logEntries);
                }
                catch (Exception e2) {
                    throw new RuntimeException("ScrollPane class does not support either (<Actor>, <Skin>) or (<Actor>) constructors.");
                }
            }
            GUIConsole.this.scroll.setFadeScrollBars(false);
            GUIConsole.this.scroll.setScrollbarsOnTop(false);
            GUIConsole.this.scroll.setOverscroll(false, false);
            GUIConsole.this.scroll.addListener(new DragScrollListener(GUIConsole.this.scroll){

                public boolean scrolled(InputEvent event, float x, float y, int amount) {
                    ConsoleDisplay.this.closeContext();
                    return super.scrolled(event, x, y, amount);
                }
            });
            this.root.add(GUIConsole.this.scroll).colspan(2).expand().fill().pad(4.0f).row();
            this.root.add(this.input).expandX().fillX().pad(4.0f);
            this.submitCell = this.root.add(this.submit);
            this.root.addListener(new KeyListener(this.input));
        }

        void refresh() {
            Array<LogEntry> entries = GUIConsole.this.log.getLogEntries();
            this.logEntries.clear();
            this.logEntries.add().expand().fill().row();
            int size = entries.size;
            for (int i = 0; i < size; ++i) {
                Label l;
                LogEntry le = entries.get(i);
                if (this.labels.size > i) {
                    l = this.labels.get(i);
                } else {
                    try {
                        l = (Label)GUIConsole.this.labelClass.getConstructor(CharSequence.class, Skin.class, String.class, Color.class).newInstance("", this.skin, this.fontName, LogLevel.DEFAULT.getColor());
                    }
                    catch (Exception e) {
                        try {
                            l = (Label)GUIConsole.this.labelClass.getConstructor(CharSequence.class, String.class, Color.class).newInstance("", this.fontName, LogLevel.DEFAULT.getColor());
                        }
                        catch (Exception e2) {
                            throw new RuntimeException("Label class does not support either (<String>, <Skin>, <String>, <Color>) or (<String>, <String>, <Color>) constructors.");
                        }
                    }
                    l.setWrap(true);
                    this.labels.add(l);
                    l.addListener(new LogListener(l, this.skin.getDrawable(GUIConsole.this.tableBackground)));
                }
                l.setText(" " + le.toConsoleString());
                l.setColor(le.getColor());
                this.logEntries.add(l).expandX().fillX().top().left().row();
            }
            GUIConsole.this.scroll.validate();
            GUIConsole.this.scroll.setScrollPercentY(1.0f);
        }

        private void setHidden(boolean h) {
            GUIConsole.this.hidden = h;
            if (GUIConsole.this.hidden) {
                GUIConsole.this.consoleWindow.setTouchable(Touchable.disabled);
                GUIConsole.this.stage.setKeyboardFocus(null);
                GUIConsole.this.stage.setScrollFocus(null);
            } else {
                this.input.setText("");
                GUIConsole.this.consoleWindow.setTouchable(Touchable.enabled);
                if (this.selected) {
                    this.select();
                }
            }
        }

        void select() {
            this.selected = true;
            if (!GUIConsole.this.hidden) {
                GUIConsole.this.stage.setKeyboardFocus(this.input);
                GUIConsole.this.stage.setScrollFocus(GUIConsole.this.scroll);
            }
        }

        void deselect() {
            this.selected = false;
            GUIConsole.this.stage.setKeyboardFocus(null);
            GUIConsole.this.stage.setScrollFocus(null);
        }

        void openContext(Label label, float x, float y) {
            this.context.setLabel(label);
            this.context.setPosition(x, y);
            this.context.setStage(GUIConsole.this.stage);
        }

        void closeContext() {
            this.context.remove();
        }

        boolean submit() {
            String s = this.input.getText();
            if (s.length() == 0 || s.split(" ").length == 0) {
                return false;
            }
            if (GUIConsole.this.exec != null) {
                GUIConsole.this.commandHistory.store(s);
                GUIConsole.this.execCommand(s);
            } else {
                GUIConsole.this.log("No command executor has been set. Please call setCommandExecutor for this console in your code and restart.", LogLevel.ERROR);
            }
            this.input.setText("");
            return true;
        }

        void showSubmit(boolean show) {
            this.submit.setVisible(show);
            this.submitCell.size(show ? this.submit.getPrefWidth() : 0.0f, show ? this.submit.getPrefHeight() : 0.0f);
        }

        void setSubmitText(String text) {
            this.submit.setText(text);
            this.showSubmit(this.submit.isVisible());
        }
    }
}

