/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.StreamUtils;
import com.cairn4.utils.TextureDownloaderCallback;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TextureDownloader {
    TextureRegion region;
    TextureDownloaderCallback callback;

    public void get(final TextureDownloaderCallback callback, final TextureRegion regionHolder, final String url) {
        this.callback = callback;
        String fUrl = url;
        new Thread(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            private int download(byte[] out, String fUrl) {
                InputStream in = null;
                try {
                    int length;
                    HttpURLConnection conn = null;
                    conn = (HttpURLConnection)new URL(url).openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(false);
                    conn.setUseCaches(true);
                    conn.connect();
                    in = conn.getInputStream();
                    int readBytes = 0;
                    while ((length = in.read(out, readBytes, out.length - readBytes)) != -1) {
                        readBytes += length;
                    }
                    int n = readBytes;
                    StreamUtils.closeQuietly(in);
                    return n;
                }
                catch (Exception ex) {
                    int n = 0;
                    return n;
                }
                finally {
                    StreamUtils.closeQuietly(in);
                }
            }

            @Override
            public void run() {
                byte[] bytes = new byte[204800];
                int numBytes = this.download(bytes, "http://www.badlogicgames.com/wordpress/wp-content/uploads/2012/01/badlogic-new.png");
                if (numBytes != 0) {
                    Pixmap pixmap = new Pixmap(bytes, 0, numBytes);
                    final int originalWidth = pixmap.getWidth();
                    final int originalHeight = pixmap.getHeight();
                    int width = MathUtils.nextPowerOfTwo(pixmap.getWidth());
                    int height = MathUtils.nextPowerOfTwo(pixmap.getHeight());
                    final Pixmap potPixmap = new Pixmap(width, height, pixmap.getFormat());
                    potPixmap.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
                    pixmap.dispose();
                    Gdx.app.postRunnable(new Runnable(){

                        @Override
                        public void run() {
                            Texture t = new Texture(potPixmap);
                            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                            TextureDownloader.this.region = new TextureRegion(t, 0, 0, originalWidth, originalHeight);
                            callback.returnTexture(regionHolder, TextureDownloader.this.region);
                        }
                    });
                }
            }
        }).start();
    }
}

