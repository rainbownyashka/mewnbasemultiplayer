/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.LoadingErrors;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.PlayerInputProcessor;
import com.cairn4.moonbase.ui.DestroyableUI;
import com.cairn4.moonbase.ui.Menu;
import java.util.ArrayList;

public class BaseScreen
implements Screen {
    public MoonBase game;
    public SpriteBatch spriteBatch;
    public OrthographicCamera camera;
    public FitViewport viewport;
    public Stage stage;
    public TextureAtlas atlas;
    public TextureAtlas menuAtlas;
    public Skin skin;
    public BitmapFont font;
    public Label.LabelStyle labelStyle;
    BitmapFont headingFont;
    Label.LabelStyle headingStyle;
    public TextButton.TextButtonStyle textBtnStyle;
    public TextButton.TextButtonStyle altTextBtnStyle;
    public TextButton.TextButtonStyle tabBtnStyle;
    public Button.ButtonStyle minorButtonStyle;
    public ArrayList<Menu> menuStack = new ArrayList();
    public Menu menuFocus;
    public ArrayList<DestroyableUI> destroyableUIList = new ArrayList();
    static Color FONT_COLOR = new Color(0.33333334f, 0.29411766f, 0.39215687f, 1.0f);
    public Group bgGroup;
    public Group mainGroup;
    public Group floorGroup;
    public Group wallGroup;
    public Group topGroup;
    public Group worldUiGroup;
    Group persistentUIGroup;
    Group debugGroup;
    Label fpsLabel;

    public BaseScreen(MoonBase game) {
        this.game = game;
        this.spriteBatch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(MoonBase.SCREEN_WIDTH, (float)MoonBase.SCREEN_HEIGHT, this.camera);
        this.stage = new Stage(this.viewport);
        this.skin = new Skin();
        this.menuAtlas = AssetManagerSingleton.getInstance().get("menu.atlas", TextureAtlas.class);
        this.skin.addRegions(this.menuAtlas);
        this.font = AssetManagerSingleton.getInstance().get("smallfont1.fnt", BitmapFont.class);
        this.font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        String cs = "0123456789";
        this.font.setFixedWidthGlyphs(cs);
        this.font.getData().markupEnabled = true;
        this.labelStyle = new Label.LabelStyle(this.font, Color.WHITE);
        this.headingFont = AssetManagerSingleton.getInstance().get("headingfont1.fnt", BitmapFont.class);
        this.headingFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Nearest);
        this.headingStyle = new Label.LabelStyle(this.headingFont, Color.WHITE);
        NinePatch np_up = new NinePatch(this.skin.getRegion("btn-orange"), 16, 16, 16, 16);
        NinePatchDrawable npd_up = new NinePatchDrawable(np_up);
        NinePatch np_active = new NinePatch(this.skin.getRegion("btn-orange-hover"), 16, 16, 16, 16);
        NinePatchDrawable npd_active = new NinePatchDrawable(np_active);
        NinePatch np_down = new NinePatch(this.skin.getRegion("btn-orange-pressed"), 16, 16, 16, 16);
        NinePatchDrawable npd_down = new NinePatchDrawable(np_down);
        NinePatch np_disabled = new NinePatch(this.skin.getRegion("btn-orange-disabled"), 16, 16, 16, 16);
        NinePatchDrawable npd_disabled = new NinePatchDrawable(np_disabled);
        this.textBtnStyle = new TextButton.TextButtonStyle();
        this.textBtnStyle.font = this.font;
        this.textBtnStyle.up = npd_up;
        this.textBtnStyle.over = npd_active;
        this.textBtnStyle.down = npd_down;
        this.textBtnStyle.disabled = npd_disabled;
        this.textBtnStyle.disabledFontColor = new Color(1.0f, 1.0f, 1.0f, 0.5f);
        this.textBtnStyle.fontColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.textBtnStyle.downFontColor = new Color(1.0f, 0.42f, 0.03f, 1.0f);
        this.textBtnStyle.unpressedOffsetY = 2.0f;
        this.textBtnStyle.pressedOffsetY = -1.0f;
        NinePatch alt_np_up = new NinePatch(this.skin.getRegion("btn-minor"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_up = new NinePatchDrawable(alt_np_up);
        NinePatch alt_np_active = new NinePatch(this.skin.getRegion("btn-minor-hover"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_active = new NinePatchDrawable(alt_np_active);
        NinePatch alt_np_down = new NinePatch(this.skin.getRegion("btn-minor-pressed"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_down = new NinePatchDrawable(alt_np_down);
        NinePatch alt_np_disabled = new NinePatch(this.skin.getRegion("btn-minor-disabled"), 16, 16, 16, 16);
        NinePatchDrawable alt_npd_disabled = new NinePatchDrawable(alt_np_disabled);
        this.altTextBtnStyle = new TextButton.TextButtonStyle();
        this.altTextBtnStyle.font = this.font;
        this.altTextBtnStyle.up = alt_npd_up;
        this.altTextBtnStyle.over = alt_npd_active;
        this.altTextBtnStyle.down = alt_npd_down;
        this.altTextBtnStyle.disabled = alt_npd_disabled;
        this.altTextBtnStyle.disabledFontColor = new Color(1.0f, 1.0f, 1.0f, 0.5f);
        this.altTextBtnStyle.fontColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.altTextBtnStyle.downFontColor = new Color(1.0f, 0.42f, 0.03f, 1.0f);
        this.altTextBtnStyle.unpressedOffsetY = 2.0f;
        this.altTextBtnStyle.pressedOffsetY = -1.0f;
        NinePatch thin_np_up = new NinePatch(this.skin.getRegion("btn-thin"), 7, 6, 6, 6);
        NinePatchDrawable thin_npd_up = new NinePatchDrawable(thin_np_up);
        NinePatch thin_np_active = new NinePatch(this.skin.getRegion("btn-thin-hover"), 6, 6, 6, 6);
        NinePatchDrawable thin_npd_active = new NinePatchDrawable(thin_np_active);
        NinePatch thin_np_down = new NinePatch(this.skin.getRegion("btn-thin-pressed"), 6, 6, 6, 6);
        NinePatchDrawable thin_npd_down = new NinePatchDrawable(thin_np_down);
        NinePatch thin_np_disabled = new NinePatch(this.skin.getRegion("btn-thin-disabled"), 6, 6, 6, 6);
        NinePatchDrawable thin_npd_disabled = new NinePatchDrawable(thin_np_disabled);
        this.minorButtonStyle = new Button.ButtonStyle();
        this.minorButtonStyle.up = thin_npd_disabled;
        this.minorButtonStyle.over = thin_npd_up;
        this.minorButtonStyle.down = thin_npd_down;
        this.minorButtonStyle.checked = thin_npd_active;
        this.minorButtonStyle.disabled = thin_npd_disabled;
        NinePatch np_tab_up = new NinePatch(this.skin.getRegion("tab-off"), 16, 16, 16, 1);
        NinePatchDrawable np_tab_upd = new NinePatchDrawable(np_tab_up);
        NinePatch np_tab_over = new NinePatch(this.skin.getRegion("tab-hover"), 16, 16, 16, 1);
        NinePatchDrawable np_tab_overd = new NinePatchDrawable(np_tab_over);
        NinePatch np_tab_down = new NinePatch(this.skin.getRegion("tab-off"), 16, 16, 16, 1);
        NinePatchDrawable np_tab_downd = new NinePatchDrawable(np_tab_down);
        NinePatch np_tab_checked = new NinePatch(this.skin.getRegion("tab-active"), 16, 16, 16, 1);
        NinePatchDrawable np_tab_checkedd = new NinePatchDrawable(np_tab_checked);
        this.tabBtnStyle = new TextButton.TextButtonStyle();
        this.tabBtnStyle.font = this.font;
        this.tabBtnStyle.up = np_tab_upd;
        this.tabBtnStyle.over = np_tab_overd;
        this.tabBtnStyle.down = np_tab_downd;
        this.tabBtnStyle.checked = np_tab_checkedd;
        this.tabBtnStyle.fontColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.tabBtnStyle.downFontColor = new Color(1.0f, 0.42f, 0.03f, 1.0f);
        this.tabBtnStyle.unpressedOffsetY = 0.0f;
        this.tabBtnStyle.pressedOffsetY = 0.0f;
        this.bgGroup = new Group();
        this.stage.addActor(this.bgGroup);
        this.floorGroup = new Group();
        this.stage.addActor(this.floorGroup);
        this.wallGroup = new Group();
        this.stage.addActor(this.wallGroup);
        this.mainGroup = new Group();
        this.stage.addActor(this.mainGroup);
        this.topGroup = new Group();
        this.stage.addActor(this.topGroup);
        this.worldUiGroup = new Group();
        this.stage.addActor(this.worldUiGroup);
        this.persistentUIGroup = new Group();
        this.stage.addActor(this.persistentUIGroup);
        this.debugGroup = new Group();
        this.stage.addActor(this.debugGroup);
    }

    public void returned() {
    }

    @Override
    public void show() {
        this.fpsLabel = new Label((CharSequence)"FPS", this.labelStyle);
        this.fpsLabel.setVisible(false);
        this.fpsLabel.setFontScale(0.5f);
        this.fpsLabel.setColor(1.0f, 1.0f, 1.0f, 0.5f);
        this.fpsLabel.setWidth(MoonBase.SCREEN_WIDTH);
        this.fpsLabel.setAlignment(1);
        this.fpsLabel.setPosition(0.0f, MoonBase.SCREEN_HEIGHT - 20, 10);
        this.fpsLabel.setTouchable(Touchable.disabled);
        this.debugGroup.addActor(this.fpsLabel);
    }

    public void update(float delta) {
        for (int i = this.destroyableUIList.size() - 1; i >= 0; --i) {
            if (!this.destroyableUIList.get(i).isReadyToDestroy()) continue;
            this.destroyableUIList.remove(i);
        }
    }

    @Override
    public void render(float delta) {
        this.update(delta);
        this.spriteBatch.begin();
        this.stage.act();
        this.stage.draw();
        if (!this.menuStack.isEmpty()) {
            for (Menu m : this.menuStack) {
                m.render(delta);
            }
        }
        this.spriteBatch.end();
    }

    protected void setInputProcessor() {
        InputMultiplexer inputMulti = new InputMultiplexer();
        inputMulti.addProcessor(this.stage);
        inputMulti.addProcessor(new PlayerInputProcessor());
        Gdx.input.setInputProcessor(inputMulti);
    }

    @Override
    public void resize(int width, int height) {
        this.viewport.update(width, height, true);
        this.camera.update();
        for (Menu m : this.menuStack) {
            m.resize(width, height);
        }
    }

    public void showMenu(Menu menu) {
        Gdx.app.log("MewnBase", "Showing menu: " + menu.getClass().getSimpleName());
        this.menuStack.add(menu);
    }

    public void exitMenu() {
        if (!this.menuStack.isEmpty()) {
            this.menuStack.remove(this.menuStack.size() - 1);
        }
        MoonBase.debug("BaseScreen exitMenu(): menu stack size is now " + this.menuStack.size());
        if (this.menuStack.isEmpty()) {
            this.setInputProcessor();
            this.returned();
        } else {
            this.menuStack.get(this.menuStack.size() - 1).setInputFocus();
            this.menuStack.get(this.menuStack.size() - 1).returned();
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        this.stage.dispose();
        this.spriteBatch.dispose();
        this.skin.dispose();
    }

    public void menuForwardSound() {
        Audio.getInstance().playSound("sounds/gameStart.ogg", 0.25f);
    }

    public void menuBackSound() {
        Audio.getInstance().playSound("sounds/menuBack.ogg", 0.5f);
    }

    public void errorReturnToMainMenu(LoadingErrors e) {
    }
}

