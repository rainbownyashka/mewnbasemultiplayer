/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Vars;
import com.cairn4.moonbase.ui.BaseScreen;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.ui.Menu;

public class Popup
extends Menu {
    protected Label titleLabel;
    protected Image glowPanelBg;
    protected Image panelBg;
    protected Image tintBg;
    protected Button btnClose;
    protected Group popupGroup;
    static final float DEFAULT_WIDTH = 794.0f;
    static final float DEFAULT_HEIGHT = 612.0f;
    public float tintBgAlpha = 0.7f;
    private Image edgeShineL;
    private Image edgeShineR;
    private Image edgeShineT;
    private Image edgeShineB;
    private final float shineThickness = 50.0f;
    private final float shineLength = 180.0f;
    private final Color shineColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
    private final float edgeShineTopOffset = 33.0f;
    private final float edgeShineBottomOffset = 40.0f;
    private final float edgeShineXOffset = 36.0f;
    public boolean useShowAnim = true;

    public Popup(BaseScreen baseScreen) {
        super(baseScreen);
    }

    @Override
    protected void setup() {
        super.setup();
        this.setupTintBg();
        this.menuGroup.setVisible(false);
        this.popupGroup = new Group();
        this.menuGroup.addActor(this.popupGroup);
        this.setupBackground();
        Button.ButtonStyle bs = new Button.ButtonStyle();
        bs.up = this.skin.getDrawable("btn-close");
        bs.over = this.skin.getDrawable("btn-close-over");
        bs.down = this.skin.getDrawable("btn-close-pressed");
        this.btnClose = new Button(bs);
        this.btnClose.setPosition(this.panelBg.getWidth() - 35.0f, this.panelBg.getHeight() - 43.0f, 18);
        this.btnClose.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent event, float x, float y) {
                Popup.this.back();
            }
        });
        this.popupGroup.addActor(this.btnClose);
        this.setSize(794.0f, 612.0f);
    }

    protected void setupTintBg() {
        this.tintBg = new Image(AssetManagerSingleton.getInstance().get("largeQuad.png", Texture.class));
        this.tintBg.setColor(0.0f, 0.0f, 0.0f, this.tintBgAlpha);
        this.tintBg.setSize(MoonBase.SCREEN_WIDTH + 10, MoonBase.SCREEN_HEIGHT + 10);
        this.tintBg.setPosition(-5.0f, -5.0f);
        this.menuGroup.getParent().addActor(this.tintBg);
        this.tintBg.setVisible(false);
        this.tintBg.addListener(new ClickListener(){

            @Override
            public void clicked(InputEvent e, float x, float y) {
                Popup.this.back();
            }
        });
        this.menuGroup.toFront();
    }

    protected void setupBackground() {
        NinePatch npGlowBg = new NinePatch(this.baseScreen.skin.getRegion("popup-glow"), 90, 90, 110, 80);
        this.glowPanelBg = new Image(npGlowBg);
        this.glowPanelBg.setSize(794.0f, 612.0f);
        this.glowPanelBg.setPosition(0.0f, -10.0f);
        this.glowPanelBg.setOrigin(1);
        this.glowPanelBg.setColor(Vars.WARNING_RED);
        this.popupGroup.addActor(this.glowPanelBg);
        NinePatch npPanelBg = new NinePatch(this.baseScreen.skin.getRegion("popup"), 90, 90, 110, 80);
        this.panelBg = new Image(npPanelBg);
        this.panelBg.setSize(794.0f, 612.0f);
        this.panelBg.setOrigin(1);
        this.panelBg.setPosition(0.0f, -10.0f);
        this.popupGroup.addActor(this.panelBg);
        this.popupGroup.setOrigin(this.panelBg.getWidth() / 2.0f, this.panelBg.getHeight() / 2.0f + 15.0f);
        this.centerPopupGroup();
        this.edgeShineL = new AdditiveImage(this.baseScreen.skin.getDrawable("small-gradient-circle"));
        this.edgeShineL.setSize(50.0f, 180.0f);
        this.edgeShineL.setColor(this.shineColor);
        this.edgeShineL.setPosition(36.0f, this.panelBg.getHeight() / 2.0f, 1);
        this.popupGroup.addActor(this.edgeShineL);
        this.edgeShineR = new AdditiveImage(this.baseScreen.skin.getDrawable("small-gradient-circle"));
        this.edgeShineR.setSize(50.0f, 180.0f);
        this.edgeShineR.setColor(this.shineColor);
        this.edgeShineR.setPosition(this.panelBg.getWidth() - 36.0f, this.panelBg.getHeight() / 2.0f, 1);
        this.popupGroup.addActor(this.edgeShineR);
        this.edgeShineT = new AdditiveImage(this.baseScreen.skin.getDrawable("small-gradient-circle"));
        this.edgeShineT.setSize(180.0f, 50.0f);
        this.edgeShineT.setColor(this.shineColor);
        this.edgeShineT.setPosition(this.panelBg.getWidth() / 2.0f, this.panelBg.getHeight() - 33.0f, 1);
        this.popupGroup.addActor(this.edgeShineT);
        this.edgeShineB = new AdditiveImage(this.baseScreen.skin.getDrawable("small-gradient-circle"));
        this.edgeShineB.setSize(180.0f, 50.0f);
        this.edgeShineB.setColor(this.shineColor);
        this.edgeShineB.setPosition(this.panelBg.getWidth() / 2.0f, 40.0f, 1);
        this.popupGroup.addActor(this.edgeShineB);
        this.titleLabel = new Label((CharSequence)" ", this.labelStyle);
        this.titleLabel.setFontScale(0.5f);
        this.titleLabel.setPosition(78.0f, this.panelBg.getHeight() - 43.0f, 10);
        this.popupGroup.addActor(this.titleLabel);
        this.titleLabel.setTouchable(Touchable.disabled);
        this.titleLabel.setHeight(70.0f);
        this.glowPanelBg.toFront();
    }

    protected void centerPopupGroup() {
        float xOffset = ((float)MoonBase.SCREEN_WIDTH - this.panelBg.getWidth()) / 2.0f;
        float yOffset = ((float)MoonBase.SCREEN_HEIGHT - this.panelBg.getHeight()) / 2.0f + 20.0f;
        this.popupGroup.setPosition(xOffset, yOffset);
    }

    public void setSize(float width, float height) {
        this.glowPanelBg.setSize(width, height);
        this.panelBg.setSize(width, height);
        this.glowPanelBg.setOrigin(1);
        this.panelBg.setOrigin(1);
        float xOffset = ((float)MoonBase.SCREEN_WIDTH - this.panelBg.getWidth()) / 2.0f;
        float yOffset = ((float)MoonBase.SCREEN_HEIGHT - this.panelBg.getHeight()) / 2.0f + 20.0f;
        this.popupGroup.setOrigin(this.panelBg.getWidth() / 2.0f, this.panelBg.getHeight() / 2.0f + 15.0f);
        this.popupGroup.setSize(width, height);
        this.popupGroup.setPosition(xOffset, yOffset);
        this.titleLabel.setPosition(78.0f, this.panelBg.getHeight() - 43.0f, 10);
        this.btnClose.setPosition(this.panelBg.getWidth() - 35.0f, this.panelBg.getHeight() - 45.0f, 18);
        this.edgeShineL.setPosition(36.0f, this.panelBg.getHeight() / 2.0f, 1);
        this.edgeShineR.setPosition(this.panelBg.getWidth() - 36.0f, this.panelBg.getHeight() / 2.0f, 1);
        this.edgeShineT.setPosition(this.panelBg.getWidth() / 2.0f, this.panelBg.getHeight() - 33.0f - 10.0f, 1);
        this.edgeShineB.setPosition(this.panelBg.getWidth() / 2.0f, 30.0f, 1);
    }

    @Override
    public void show() {
        if (this.useShowAnim) {
            this.tintBg.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.visible(true), (Action)Actions.alpha(this.tintBgAlpha, 0.15f)));
            this.glowPanelBg.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.scaleTo(1.0f, 1.0f), (Action)Actions.delay(0.16f), (Action)Actions.parallel((Action)Actions.sequence((Action)Actions.alpha(0.4f, 0.01f), (Action)Actions.fadeOut(0.24f, Interpolation.sineOut)), (Action)Actions.scaleTo(1.08f, 1.08f, 0.25f, Interpolation.sineOut))));
            this.popupGroup.addAction(Actions.sequence(Actions.scaleTo(0.8f, 0.8f), Actions.alpha(0.0f), Actions.run(new Runnable(){

                @Override
                public void run() {
                    Popup.this.menuGroup.setVisible(true);
                }
            }), Actions.visible(true), Actions.parallel((Action)Actions.fadeIn(0.15f), (Action)Actions.scaleTo(1.025f, 1.025f, 0.15f, Interpolation.sineOut)), Actions.scaleTo(1.0f, 1.0f, 0.05f), Actions.run(new Runnable(){

                @Override
                public void run() {
                    Popup.this.finishedShowAnim();
                }
            })));
            float horzMove = this.panelBg.getWidth() / 2.0f - 120.0f;
            float vertMove = this.panelBg.getHeight() / 2.0f - 150.0f;
            float glintAlpha = 0.75f;
            float animHalfTime = 0.15f;
            float startDelay = 0.05f;
            this.edgeShineL.addAction(Actions.sequence((Action)Actions.moveToAligned(36.0f, this.panelBg.getHeight() / 2.0f - vertMove, 1), (Action)Actions.alpha(0.0f), (Action)Actions.delay(startDelay), (Action)Actions.parallel((Action)Actions.sequence((Action)Actions.alpha(glintAlpha, animHalfTime), (Action)Actions.alpha(0.0f, animHalfTime)), (Action)Actions.moveBy(0.0f, vertMove * 2.0f, animHalfTime * 2.0f, Interpolation.exp5))));
            this.edgeShineR.addAction(Actions.sequence((Action)Actions.moveToAligned(this.panelBg.getWidth() - 36.0f, this.panelBg.getHeight() / 2.0f - vertMove, 1), (Action)Actions.alpha(0.0f), (Action)Actions.delay(startDelay + animHalfTime), (Action)Actions.parallel((Action)Actions.sequence((Action)Actions.alpha(glintAlpha, animHalfTime), (Action)Actions.alpha(0.0f, animHalfTime)), (Action)Actions.moveBy(0.0f, vertMove * 2.0f, animHalfTime * 2.0f, Interpolation.exp5))));
            this.edgeShineT.addAction(Actions.sequence((Action)Actions.moveToAligned(this.panelBg.getWidth() / 2.0f - horzMove, this.panelBg.getHeight() - 33.0f - 10.0f, 1), (Action)Actions.alpha(0.0f), (Action)Actions.delay(startDelay + animHalfTime), (Action)Actions.parallel((Action)Actions.sequence((Action)Actions.alpha(glintAlpha, animHalfTime), (Action)Actions.alpha(0.0f, animHalfTime)), (Action)Actions.moveBy(horzMove * 2.0f, 0.0f, animHalfTime * 2.0f, Interpolation.exp5))));
            this.edgeShineB.addAction(Actions.sequence((Action)Actions.moveToAligned(this.panelBg.getWidth() / 2.0f - horzMove, 30.0f, 1), (Action)Actions.alpha(0.0f), (Action)Actions.delay(startDelay), (Action)Actions.parallel((Action)Actions.sequence((Action)Actions.alpha(glintAlpha, animHalfTime), (Action)Actions.alpha(0.0f, animHalfTime)), (Action)Actions.moveBy(horzMove * 2.0f, 0.0f, animHalfTime * 2.0f, Interpolation.exp5))));
        } else {
            super.show();
            this.finishedShowAnim();
        }
    }

    @Override
    public void returned() {
        this.useShowAnim = false;
        this.show();
    }

    @Override
    public void back() {
        this.tintBg.remove();
        this.baseScreen.menuBackSound();
        super.back();
    }

    public void setTitle(String loc_key) {
        if (loc_key != "" && loc_key != null) {
            this.titleLabel.setText(Localization.get(loc_key));
        } else {
            this.titleLabel.setText("");
        }
    }

    public void setTitleNonLoc(String title) {
        this.titleLabel.setText(title);
    }
}

