/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.ui.GameScreen;
import java.nio.Buffer;

public class ScreenshotGrabber {
    public static void takeScreenshot(Stage stage, GameScreen gameScreen) {
        int width = Gdx.graphics.getBackBufferWidth() - (stage.getViewport().getLeftGutterWidth() + stage.getViewport().getRightGutterWidth());
        int height = Gdx.graphics.getBackBufferHeight() - (stage.getViewport().getBottomGutterHeight() + stage.getViewport().getTopGutterHeight());
        int squareW = (int)((float)width * 0.4f);
        int squareH = (int)((double)height * 0.651);
        int offsetX = (width - squareW) / 2 + stage.getViewport().getLeftGutterWidth();
        int offsetY = (height - squareH) / 2 + stage.getViewport().getBottomGutterHeight();
        gameScreen.hudStage.getRoot().setVisible(false);
        gameScreen.render(0.0f);
        byte[] pixels = ScreenUtils.getFrameBufferPixels(offsetX, offsetY, squareW, squareH, true);
        for (int i = 4; i < pixels.length; i += 4) {
            pixels[i - 1] = -1;
        }
        Pixmap pixmap = new Pixmap(squareW, squareH, Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, (Buffer)pixmap.getPixels(), pixels.length);
        try {
            PixmapIO.writePNG(Gdx.files.local("saves/" + MoonBase.currentSaveFolder + "/screenshot.png"), pixmap);
            pixmap.dispose();
        }
        catch (Throwable e) {
            e.printStackTrace();
            MessageManager.getInstance().dispatchMessage(1000, e);
        }
        gameScreen.hudStage.getRoot().setVisible(true);
        MoonBase.log("Saved screenshot");
    }
}

