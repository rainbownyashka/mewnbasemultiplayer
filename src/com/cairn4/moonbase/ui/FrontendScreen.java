/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.UpdateCheck;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.MainMenu;
import com.cairn4.moonbase.ui.Menu;
import com.cairn4.moonbase.ui.MenuBackground;
import com.cairn4.moonbase.ui.SaveFixWarningPopup;
import com.cairn4.moonbase.ui.WhatsNewPopup;

public class FrontendScreen
extends BaseScreen {
    private MenuBackground menuBackground;

    public FrontendScreen(MoonBase game) {
        super(game);
        if (!AssetManagerSingleton.getInstance().isLoaded("frontend.atlas")) {
            MoonBase.log("Frontend wasn't finished loading, load it.");
            AssetManagerSingleton.getInstance().load("frontend.atlas", TextureAtlas.class);
            AssetManagerSingleton.getInstance().finishLoading();
        }
        TextureAtlas frontendAtlas = AssetManagerSingleton.getInstance().get("frontend.atlas", TextureAtlas.class);
        this.skin.addRegions(frontendAtlas);
        this.game.clearColor = Color.BLACK;
        Audio.getInstance().startMusic("music/enchanted_tiki_86_0.mp3", 0.5f, true);
        this.menuBackground = new MenuBackground(this);
        this.showMenu(new MainMenu((BaseScreen)this, false));
        if (game.prevScreenClass == null) {
            if (UpdateCheck.SHOW_WHATSNEW) {
                this.showMenu(new WhatsNewPopup(this));
            }
            if (game.saveFilesThatMightBeFixable.size() > 0) {
                this.showMenu(new SaveFixWarningPopup(this, game.saveFilesThatMightBeFixable));
            }
        }
        this.setInputProcessor();
        this.font.getData().setScale(1.0f);
    }

    @Override
    public void update(float delta) {
        if (!this.menuStack.isEmpty()) {
            ((Menu)this.menuStack.get(this.menuStack.size() - 1)).update(delta);
            ((Menu)this.menuStack.get(this.menuStack.size() - 1)).handleInput();
        }
    }

    @Override
    public void showMenu(Menu menu) {
        MoonBase.log("Showing menu: " + menu.getClass().getSimpleName());
        if (this.menuStack.size() > 0) {
            ((Menu)this.menuStack.get(this.menuStack.size() - 1)).hide();
        }
        this.menuStack.add(menu);
    }

    protected void focus(Menu.DIRECTIONS dir) {
        MoonBase.debug("Focus: " + (Object)((Object)dir));
    }

    protected void select() {
    }

    @Override
    public void render(float delta) {
        this.menuBackground.render(delta);
        super.render(delta);
    }

    @Override
    public void dispose() {
        this.menuBackground.dispose();
        super.dispose();
    }
}

