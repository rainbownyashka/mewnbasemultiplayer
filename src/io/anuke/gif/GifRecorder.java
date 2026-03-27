/*
 * Decompiled with CFR 0.152.
 */
package io.anuke.gif;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import io.anuke.gif.GifSequenceWriter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.Buffer;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

public class GifRecorder {
    private static final float defaultSize = 300.0f;
    private int resizeKey = 129;
    private int openKey = 33;
    private int recordKey = 48;
    private int shiftKey = 59;
    private RecorderController controller = new DefaultController();
    private Batch batch;
    private Matrix4 matrix = new Matrix4();
    private TextureRegion region;
    private boolean skipAlpha = false;
    private int recordfps = 30;
    private float gifx;
    private float gify;
    private float gifwidth;
    private float gifheight;
    private float giftime;
    private float offsetx;
    private float offsety;
    private FileHandle exportdirectory;
    private FileHandle workdirectory;
    private boolean disableGUI;
    private float speedMultiplier = 1.0f;
    private Array<byte[]> frames = new Array();
    private File lastRecording;
    private float frametime;
    private boolean recording;
    private boolean open;
    private boolean saving;
    private float saveprogress;

    public GifRecorder(Batch batch) {
        this(batch, Gdx.files.local("gifexport"), Gdx.files.local(".gifimages"));
    }

    public GifRecorder(Batch batch, FileHandle exportdirectory, FileHandle workdirectory) {
        this.batch = batch;
        this.gifx = -150.0f;
        this.gify = -150.0f;
        this.gifwidth = 300.0f;
        this.gifheight = 300.0f;
        this.workdirectory = workdirectory;
        this.exportdirectory = exportdirectory;
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        this.region = new TextureRegion(new Texture(pixmap));
    }

    protected void doInput() {
        if (this.controller.openKeyPressed() && !this.saving) {
            if (this.recording) {
                this.finishRecording();
                this.clearFrames();
            }
            boolean bl = this.open = !this.open;
        }
        if (this.open && this.controller.recordKeyPressed() && !this.saving) {
            if (!this.recording) {
                this.startRecording();
            } else {
                this.finishRecording();
                this.writeGIF(this.workdirectory, this.exportdirectory);
            }
        }
    }

    public void update() {
        float ys;
        float xs;
        this.doInput();
        float delta = Gdx.graphics.getDeltaTime();
        if (!this.open) {
            return;
        }
        this.matrix.set(this.batch.getProjectionMatrix());
        this.batch.getProjectionMatrix().setToOrtho2D(0.0f, 0.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        boolean wasDrawing = this.batch.isDrawing();
        if (wasDrawing) {
            this.batch.end();
        }
        this.batch.begin();
        float wx = Gdx.graphics.getWidth() / 2;
        float wy = Gdx.graphics.getHeight() / 2;
        if (!this.disableGUI) {
            this.batch.setColor(Color.YELLOW);
        }
        if (this.controller.resizeKeyPressed()) {
            if (!this.disableGUI) {
                this.batch.setColor(Color.GREEN);
            }
            xs = Math.abs((float)(Gdx.graphics.getWidth() / 2) + this.offsetx - (float)Gdx.input.getX());
            ys = Math.abs((float)(Gdx.graphics.getHeight() / 2) + this.offsety - (float)(Gdx.graphics.getHeight() - Gdx.input.getY()));
            this.gifx = -xs;
            this.gify = -ys;
            this.gifwidth = xs * 2.0f;
            this.gifheight = ys * 2.0f;
        }
        if (this.controller.shiftKeyPressed()) {
            if (!this.disableGUI) {
                this.batch.setColor(Color.ORANGE);
            }
            xs = Gdx.graphics.getWidth() / 2 - Gdx.input.getX();
            ys = Gdx.graphics.getHeight() / 2 - (Gdx.graphics.getHeight() - Gdx.input.getY());
            this.offsetx = -xs;
            this.offsety = -ys;
        }
        if (!this.disableGUI) {
            if (this.recording) {
                this.batch.setColor(Color.RED);
            }
            if (this.region != null) {
                this.batch.draw(this.region, this.gifx + wx + this.offsetx, this.gify + wy + this.offsety, this.gifwidth, 1.0f);
                this.batch.draw(this.region, this.gifx + wx + this.offsetx, this.gify + wy + this.gifheight + this.offsety, this.gifwidth, 1.0f);
                this.batch.draw(this.region, this.gifx + wx + this.offsetx, this.gify + wy + this.offsety, 1.0f, this.gifheight);
                this.batch.draw(this.region, this.gifx + wx + this.offsetx + this.gifwidth, this.gify + wy + this.offsety, 1.0f, this.gifheight + 1.0f);
            }
            if (this.saving) {
                if (!this.disableGUI) {
                    this.batch.setColor(Color.BLACK);
                }
                float w = 200.0f;
                float h = 50.0f;
                this.batch.draw(this.region, (float)(Gdx.graphics.getWidth() / 2) - w / 2.0f, (float)(Gdx.graphics.getHeight() / 2) - h / 2.0f, w, h);
                Color a = Color.RED;
                Color b = Color.GREEN;
                float s = this.saveprogress;
                float i = 1.0f - this.saveprogress;
                this.batch.setColor(a.r * i + b.r * s, a.g * i + b.g * s, a.b * i + b.b * s, 1.0f);
                this.batch.draw(this.region, (float)(Gdx.graphics.getWidth() / 2) - w / 2.0f, (float)(Gdx.graphics.getHeight() / 2) - h / 2.0f, w * this.saveprogress, h);
            }
            this.batch.setColor(Color.WHITE);
        }
        if (this.recording) {
            this.giftime += delta;
            this.frametime += delta * 61.0f * this.speedMultiplier;
            if (this.frametime >= (float)(60 / this.recordfps)) {
                byte[] pix = ScreenUtils.getFrameBufferPixels((int)(this.gifx + this.offsetx) + 1 + Gdx.graphics.getWidth() / 2, (int)(this.gify + this.offsety) + 1 + Gdx.graphics.getHeight() / 2, (int)this.gifwidth - 2, (int)this.gifheight - 2, true);
                this.frames.add(pix);
                this.frametime = 0.0f;
            }
        }
        this.batch.end();
        this.batch.getProjectionMatrix().set(this.matrix);
        if (wasDrawing) {
            this.batch.begin();
        }
    }

    public void setSpeedMultiplier(float m) {
        this.speedMultiplier = m;
    }

    public void setGUIDisabled(boolean disabled) {
        this.disableGUI = true;
    }

    public void setController(RecorderController controller) {
        this.controller = controller;
    }

    public boolean isSaving() {
        return this.saving;
    }

    public boolean isOpen() {
        return this.open;
    }

    public void open() {
        this.open = true;
    }

    public void close() {
        this.open = false;
    }

    public boolean isRecording() {
        return this.recording;
    }

    public void startRecording() {
        this.clearFrames();
        this.recording = true;
    }

    public float getTime() {
        return this.giftime;
    }

    public void finishRecording() {
        this.recording = false;
        this.giftime = 0.0f;
    }

    public void clearFrames() {
        this.frames.clear();
        this.giftime = 0.0f;
        this.recording = false;
    }

    public void setExportDirectory(FileHandle handle) {
        this.exportdirectory = handle;
    }

    public void setWorkingDirectory(FileHandle handle) {
        this.workdirectory = handle;
    }

    public void setResizeKey(int key) {
        this.resizeKey = key;
    }

    public void setOpenKey(int key) {
        this.openKey = key;
    }

    public void setRecordKey(int key) {
        this.recordKey = key;
    }

    public void setFPS(int fps) {
        this.recordfps = fps;
    }

    public File getLastRecording() {
        return this.lastRecording;
    }

    public void setSkipAlpha(boolean skipAlpha) {
        this.skipAlpha = skipAlpha;
    }

    public void setBounds(float x, float y, float width, float height) {
        this.gifx = x;
        this.gify = y;
        this.gifwidth = width;
        this.gifheight = height;
    }

    public void setBounds(Rectangle rect) {
        this.setBounds(rect.x, rect.y, rect.width, rect.height);
    }

    public FileHandle takeScreenshot() {
        return this.takeScreenshot(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public FileHandle takeScreenshot(int x, int y, int width, int height) {
        byte[] pix = ScreenUtils.getFrameBufferPixels(x, y, width, height, true);
        Pixmap pixmap = this.createPixmap(pix, width, height);
        FileHandle file = this.exportdirectory.child("screenshot-" + TimeUtils.millis() + ".png");
        PixmapIO.writePNG(file, pixmap);
        pixmap.dispose();
        return file;
    }

    public void writeGIF() {
        this.writeGIF(this.workdirectory, this.exportdirectory);
    }

    private void writeGIF(FileHandle directory, FileHandle writedirectory) {
        if (this.saving) {
            return;
        }
        this.saving = true;
        Array strings = new Array();
        Array<Pixmap> pixmaps = new Array<Pixmap>();
        for (byte[] bytes : this.frames) {
            Pixmap pixmap = this.createPixmap(bytes);
            pixmaps.add(pixmap);
        }
        new Thread(() -> {
            this.saveprogress = 0.0f;
            int i = 0;
            for (Pixmap pixmap : pixmaps) {
                PixmapIO.writePNG(Gdx.files.absolute(directory.file().getAbsolutePath() + "/frame" + i + ".png"), pixmap);
                strings.add("frame" + i + ".png");
                this.saveprogress += 0.5f / (float)pixmaps.size;
                ++i;
            }
            this.lastRecording = this.compileGIF(strings, directory, writedirectory);
            directory.deleteDirectory();
            for (Pixmap pixmap : pixmaps) {
                pixmap.dispose();
            }
            this.saving = false;
        }).start();
    }

    private File compileGIF(Array<String> strings, FileHandle inputdirectory, FileHandle directory) {
        if (strings.size == 0) {
            new RuntimeException("No strings!");
            return null;
        }
        try {
            String time = "" + (int)(System.currentTimeMillis() / 1000L);
            String dirstring = inputdirectory.file().getAbsolutePath();
            new File(directory.file().getAbsolutePath()).mkdir();
            BufferedImage firstImage = ImageIO.read(new File(dirstring + "/" + strings.get(0)));
            File file = new File(directory.file().getAbsolutePath() + "/recording" + time + ".gif");
            FileImageOutputStream output = new FileImageOutputStream(file);
            GifSequenceWriter writer = new GifSequenceWriter(output, firstImage.getType(), (int)(1.0f / (float)this.recordfps * 1000.0f), true);
            writer.writeToSequence(firstImage);
            for (int i = 1; i < strings.size; ++i) {
                BufferedImage after = ImageIO.read(new File(dirstring + "/" + strings.get(i)));
                this.saveprogress += 0.5f / (float)this.frames.size;
                writer.writeToSequence(after);
            }
            writer.close();
            output.close();
            return file;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Pixmap createPixmap(byte[] pixels, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, (Buffer)pixmap.getPixels(), pixels.length);
        Color color = new Color();
        if (!this.skipAlpha) {
            for (int x = 0; x < pixmap.getWidth(); ++x) {
                for (int y = 0; y < pixmap.getHeight(); ++y) {
                    color.set(pixmap.getPixel(x, y));
                    if (!(color.a <= 0.999f)) continue;
                    color.a = 1.0f;
                    pixmap.setColor(color);
                    pixmap.drawPixel(x, y);
                }
            }
        }
        return pixmap;
    }

    private Pixmap createPixmap(byte[] pixels) {
        return this.createPixmap(pixels, (int)this.gifwidth - 2, (int)this.gifheight - 2);
    }

    public static interface RecorderController {
        public boolean openKeyPressed();

        public boolean recordKeyPressed();

        public boolean resizeKeyPressed();

        public boolean shiftKeyPressed();
    }

    class DefaultController
    implements RecorderController {
        DefaultController() {
        }

        @Override
        public boolean openKeyPressed() {
            return Gdx.input.isKeyJustPressed(GifRecorder.this.openKey);
        }

        @Override
        public boolean recordKeyPressed() {
            return Gdx.input.isKeyJustPressed(GifRecorder.this.recordKey);
        }

        @Override
        public boolean resizeKeyPressed() {
            return Gdx.input.isButtonPressed(0) && Gdx.input.isKeyPressed(GifRecorder.this.resizeKey);
        }

        @Override
        public boolean shiftKeyPressed() {
            return Gdx.input.isButtonPressed(0) && Gdx.input.isKeyPressed(GifRecorder.this.shiftKey);
        }
    }
}

