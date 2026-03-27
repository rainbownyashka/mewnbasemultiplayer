/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import java.nio.Buffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotTaker {
    public static void capture() {
        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
        Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, (Buffer)pixmap.getPixels(), pixels.length);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_kk-mm-ss");
        Date date = new Date();
        PixmapIO.writePNG(Gdx.files.external("screenshot_" + sdf.format(date) + ".png"), pixmap);
        pixmap.dispose();
    }
}

